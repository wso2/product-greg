/*
 *  Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.registry.checkin;

import junit.framework.Assert;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.InMemoryEmbeddedRegistryService;
import org.wso2.carbon.registry.core.jdbc.realm.RegistryRealm;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.registry.checkin.util.BaseTestCase;

import java.io.*;
import java.util.*;

public class BasicTest extends BaseTestCase {

    protected static Registry registry = null;
    protected static RegistryRealm realm = null;

    private static String MYROOT = "/myroot";
    private static String MYCO = "target/myco";
    //private static String RR_URL = "http://localhost:9763/registry";
    private static String RR_URL = null;

    public void setUp() throws Exception {
        super.setUp();
        InMemoryEmbeddedRegistryService embeddedRegistryService = new InMemoryEmbeddedRegistryService();
        EmbeddingRegistryCoreServiceComponent comp = new EmbeddingRegistryCoreServiceComponent();
        comp.setRealmService(embeddedRegistryService.getRealmService());
        comp.registerBuiltInHandlers(embeddedRegistryService);

        RealmConfiguration realmConfig = embeddedRegistryService.getBootstrapRealmConfiguration();
        registry = embeddedRegistryService.getUserRegistry(
                realmConfig.getAdminUserName(), realmConfig.getAdminPassword());

        File mycoFile = new File(MYCO);
        if (mycoFile.exists()) {
            deleteDir(mycoFile);
        }
    }

    void cleanRegistry() throws RegistryException {
        // just to make sure no other stuff are there inside the registry
        if (registry.resourceExists(MYROOT)) {
            registry.delete(MYROOT);
        }
        Collection c = registry.newCollection();
        registry.put(MYROOT, c);
    }

    boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    public void testSimpleCheckout() throws Exception {
        ClientOptions clientOptions = new ClientOptions();
        clientOptions.setUserInteractor(new DefaultUserInteractor());
        clientOptions.setTesting(true);
        Resource r1 = registry.newResource();
        r1.setContent("r1 content");
        r1.setProperty("xxxx", "asdfjaksjdf");
        r1.setProperty("asdjf;k", "asdfjskfjsf");
        registry.put(MYROOT + "/bingbingpeee", r1);

        registry.addComment(MYROOT + "/bingbingpeee", new Comment("comasdfj asf;kajsdf asdf"));
        registry.addComment(MYROOT + "/bingbingpeee", new Comment("aj;lfdkjaskf asjdf;kajsdf;k"));

        registry.rateResource(MYROOT + "/bingbingpeee", 3);
        registry.applyTag(MYROOT + "/bingbingpeee", "abcdasfslapqdejf");
        registry.applyTag(MYROOT + "/bingbingpeee", "pisfsdfosdfasdk");

        Resource r2 = registry.newResource();
        r2.setContent("tare;akjfs;dkfajklfj;akfd sj;lkajs fd;klajsk;dlfj a;dfj");
        registry.put(MYROOT + "/hohohooooo", r2);

        registry.addAssociation(MYROOT + "/bingbingpeee", MYROOT + "/hohohooooo", "peek");


        // now we will get the checkout
        clientOptions.setUserUrl((RR_URL == null ? "" : RR_URL) + MYROOT);
        clientOptions.setUsername(RegistryConstants.ADMIN_USER);
        clientOptions.setPassword(RegistryConstants.ADMIN_PASSWORD);
        clientOptions.setWorkingLocation(MYCO);
        new File(MYCO).mkdirs();

        // after setting the option we will get the checkout
        new Checkout(clientOptions).execute(registry);

        File file1 = new File(MYCO + "/bingbingpeee");
        String content = readFile(file1);
        assertTrue("checkouted file should exist", file1.exists());
        assertEquals("r1 content", content);


        File file2 = new File(MYCO + "/hohohooooo");
        String content2 = readFile(file2);
        assertTrue("checkouted file should exist", file2.exists());
        assertEquals("tare;akjfs;dkfajklfj;akfd sj;lkajs fd;klajsk;dlfj a;dfj", content2);

        clientOptions.setUserUrl((RR_URL == null ? "" : RR_URL) + MYROOT);
        // the answer to only question is yes, so we are putting it here
        new Checkin(clientOptions).execute(registry);

        // checking them back
        Resource r3 = registry.get(MYROOT + "/bingbingpeee");
        assertEquals("asdfjaksjdf", r3.getProperty("xxxx"));
        assertEquals("asdfjskfjsf", r3.getProperty("asdjf;k"));

        assertEquals(3, registry.getRating(MYROOT + "/bingbingpeee", "admin"));

        Comment[] comments = registry.getComments(MYROOT + "/bingbingpeee");
        assertTrue("The commments are not checkedin correctly",
                (comments[0].getText().equals("comasdfj asf;kajsdf asdf") &&
                        comments[1].getText().equals("aj;lfdkjaskf asjdf;kajsdf;k")) ||
                        (comments[1].getText().equals("comasdfj asf;kajsdf asdf") &&
                                comments[0].getText().equals("aj;lfdkjaskf asjdf;kajsdf;k")));

        // tags
        Tag[] tags = registry.getTags(MYROOT + "/bingbingpeee");
        assertTrue("The tags has not ben checkedin correctly",
                (tags[0].getTagName().equals("abcdasfslapqdejf") && tags[1].getTagName().equals("pisfsdfosdfasdk") ||
                        tags[1].getTagName().equals("abcdasfslapqdejf") && tags[0].getTagName().equals("pisfsdfosdfasdk")));

        Association[] assocs = registry.getAllAssociations(MYROOT + "/bingbingpeee");
        assertEquals(assocs[0].getAssociationType(), "peek");
        assertEquals(assocs[0].getSourcePath(), MYROOT + "/bingbingpeee");
        assertEquals(assocs[0].getDestinationPath(), MYROOT + "/hohohooooo");

        // so that has worked, now we gonna delete what we checkined extra
        cleanRegistry();
    }

    public void testSimpleResourceUpdate() throws Exception {
        ClientOptions clientOptions = new ClientOptions();
        clientOptions.setUserInteractor(new DefaultUserInteractor());
        clientOptions.setTesting(true);
        Resource r1 = registry.newResource();
        r1.setContent("r1 content");
        r1.setProperty("xxxx", "asdfjaksjdf");
        r1.setProperty("asdjf;k", "asdfjskfjsf");
        registry.put(MYROOT + "/bingbingpeee", r1);

        registry.addComment(MYROOT + "/bingbingpeee", new Comment("comasdfj asf;kajsdf asdf"));
        registry.addComment(MYROOT + "/bingbingpeee", new Comment("aj;lfdkjaskf asjdf;kajsdf;k"));

        registry.rateResource(MYROOT + "/bingbingpeee", 3);
        registry.applyTag(MYROOT + "/bingbingpeee", "abcdasfslapqdejf");
        registry.applyTag(MYROOT + "/bingbingpeee", "pisfsdfosdfasdk");

        Resource r2 = registry.newResource();
        r2.setContent("tare;akjfs;dkfajklfj;akfd sj;lkajs fd;klajsk;dlfj a;dfj");
        registry.put(MYROOT + "/hohohooooo", r2);

        registry.addAssociation(MYROOT + "/bingbingpeee", MYROOT + "/hohohooooo", "peek");


        // now we will get the checkout
        clientOptions.setUserUrl((RR_URL == null ? "" : RR_URL) + MYROOT);
        clientOptions.setUsername(RegistryConstants.ADMIN_USER);
        clientOptions.setPassword(RegistryConstants.ADMIN_PASSWORD);
        clientOptions.setWorkingLocation(MYCO);

        // after setting the option we will get the checkout
        new Checkout(clientOptions).execute(registry);

        // update the r1
        r1.setContent("r1 content2");
        registry.put(MYROOT + "/bingbingpeee", r1);

        clientOptions.setUserUrl((RR_URL == null ? "" : RR_URL) + MYROOT);
        Update update = new Update(clientOptions);

        update.execute(registry);
        assertEquals(0, update.getAddedCount());
        assertEquals(0, update.getConflictedCount());
        assertEquals(0, update.getDeletedCount());
        assertEquals(0, update.getNotDeletedCount());
        assertEquals(1, update.getUpdatedCount());

        // another update without commiting
        update = new Update(clientOptions);
        update.execute(registry);
        assertEquals(0, update.getAddedCount());
        assertEquals(0, update.getConflictedCount());
        assertEquals(0, update.getDeletedCount());
        assertEquals(0, update.getNotDeletedCount());
        assertEquals(0, update.getUpdatedCount());

        // then we will commit it back and get a new update
        new Checkin(clientOptions).execute(registry);
        // brand new update
        update = new Update(clientOptions);
        update.execute(registry);
        assertEquals(0, update.getAddedCount());
        assertEquals(0, update.getConflictedCount());
        assertEquals(0, update.getDeletedCount());
        assertEquals(0, update.getNotDeletedCount());
        assertEquals(0, update.getUpdatedCount());

        // we will do a put without doing any update, and check whether the update is on;
        registry.put(MYROOT + "/bingbingpeee", r1);
        update = new Update(clientOptions);
        update.execute(registry);
        assertEquals(0, update.getAddedCount());
        assertEquals(0, update.getConflictedCount());
        assertEquals(0, update.getDeletedCount());
        assertEquals(0, update.getNotDeletedCount());
        assertEquals(1, update.getUpdatedCount());

        // now we change comments, ratings, tags and associations

        registry.addComment(MYROOT + "/bingbingpeee", new Comment("asdfj ;dsfj dalala dalaa lsuwersf"));

        registry.rateResource(MYROOT + "/bingbingpeee", 4);
        registry.applyTag(MYROOT + "/bingbingpeee", "hmhmhmpee");

        Resource r6 = registry.newResource();
        r2.setContent("much asfd; mudghaf ;jks;df");
        registry.put(MYROOT + "/kingkingkoo", r6);

        registry.addAssociation(MYROOT + "/bingbingpeee", MYROOT + "/kingkingkoo", "asdfbough");

        update = new Update(clientOptions);
        update.execute(registry);
        assertEquals(1, update.getAddedCount());
        assertEquals(0, update.getConflictedCount());
        assertEquals(0, update.getDeletedCount());
        assertEquals(0, update.getNotDeletedCount());
        // this should be removed when we make updatig comments, ratings, tags as an update to the resource
        //assertEquals(1, update.getUpdatedCount());

        clientOptions = new ClientOptions();
        clientOptions.setUserInteractor(new DefaultUserInteractor());
        clientOptions.setTesting(true);
        clientOptions.setUsername(RegistryConstants.ADMIN_USER);
        clientOptions.setPassword(RegistryConstants.ADMIN_PASSWORD);
        clientOptions.setWorkingLocation(MYCO);
        clientOptions.setUserUrl((RR_URL == null ? "" : RR_URL) + MYROOT);
        new Checkin(clientOptions).execute(registry);

        // checking them back
        Resource r3 = registry.get(MYROOT + "/bingbingpeee");
        assertEquals("asdfjaksjdf", r3.getProperty("xxxx"));
        assertEquals("asdfjskfjsf", r3.getProperty("asdjf;k"));

        assertEquals(4, registry.getRating(MYROOT + "/bingbingpeee", "admin"));

        Comment[] comments = registry.getComments(MYROOT + "/bingbingpeee");
        assertTrue("The commments are not checkedin correctly",
                (comments[0].getText().equals("comasdfj asf;kajsdf asdf") &&
                        comments[1].getText().equals("aj;lfdkjaskf asjdf;kajsdf;k") &&
                        comments[2].getText().equals("asdfj ;dsfj dalala dalaa lsuwersf")) ||
                        (comments[2].getText().equals("comasdfj asf;kajsdf asdf") &&
                                comments[0].getText().equals("aj;lfdkjaskf asjdf;kajsdf;k") &&
                                comments[1].getText().equals("asdfj ;dsfj dalala dalaa lsuwersf")) ||
                        (comments[1].getText().equals("comasdfj asf;kajsdf asdf") &&
                                comments[2].getText().equals("aj;lfdkjaskf asjdf;kajsdf;k") &&
                                comments[0].getText().equals("asdfj ;dsfj dalala dalaa lsuwersf")));

        // tags
        Tag[] tags = registry.getTags(MYROOT + "/bingbingpeee");
        assertTrue("The tags has not ben checkedin correctly",
                (tags[0].getTagName().equals("abcdasfslapqdejf") &&
                        tags[1].getTagName().equals("pisfsdfosdfasdk") &&
                        tags[2].getTagName().equals("hmhmhmpee")) ||
                        (tags[2].getTagName().equals("abcdasfslapqdejf") &&
                                tags[0].getTagName().equals("pisfsdfosdfasdk") &&
                                tags[1].getTagName().equals("hmhmhmpee")) ||
                        (tags[1].getTagName().equals("abcdasfslapqdejf") &&
                                tags[2].getTagName().equals("pisfsdfosdfasdk") &&
                                tags[0].getTagName().equals("hmhmhmpee")));

        Association[] assocs = registry.getAssociations(MYROOT + "/bingbingpeee", "peek");
        assertEquals(assocs[0].getAssociationType(), "peek");
        assertEquals(assocs[0].getSourcePath(), MYROOT + "/bingbingpeee");
        assertEquals(assocs[0].getDestinationPath(), MYROOT + "/hohohooooo");

        assocs = registry.getAssociations(MYROOT + "/bingbingpeee", "asdfbough");
        assertEquals(assocs[0].getAssociationType(), "asdfbough");
        assertEquals(assocs[0].getSourcePath(), MYROOT + "/bingbingpeee");
        assertEquals(assocs[0].getDestinationPath(), MYROOT + "/kingkingkoo");

        // so that has worked, now we gonna delete what we checkined extra
        cleanRegistry();
    }

    public void testSimpleResourceUpdateWithDelete() throws Exception {
        ClientOptions clientOptions = new ClientOptions();
        clientOptions.setUserInteractor(new DefaultUserInteractor());
        clientOptions.setTesting(true);
        Resource r1 = registry.newResource();
        r1.setContent("r1 content");
        r1.setProperty("xxxx", "asdfjaksjdf");
        r1.setProperty("asdjf;k", "asdfjskfjsf");
        registry.put(MYROOT + "/bingbingpeee", r1);

        // now we will get the checkout
        clientOptions.setUserUrl((RR_URL == null ? "" : RR_URL) + MYROOT);
        clientOptions.setUsername(RegistryConstants.ADMIN_USER);
        clientOptions.setPassword(RegistryConstants.ADMIN_PASSWORD);
        clientOptions.setWorkingLocation(MYCO);

        // after setting the option we will get the checkout
        new Checkout(clientOptions).execute(registry);

//        now deleting the resource
        registry.delete(MYROOT + "/bingbingpeee");


//        performing the update to see whether the resource get deleted in the file system
        Update update = new Update(clientOptions);
        update.execute(registry);

        assertEquals(0, update.getAddedCount());
        assertEquals(0, update.getConflictedCount());
        assertEquals(0, update.getUpdatedCount());
        assertEquals(1, update.getDeletedCount());
        cleanRegistry();
    }

    public void testSimpleCollectionUpdateWithDelete() throws Exception {
        ClientOptions clientOptions = new ClientOptions();
        clientOptions.setUserInteractor(new DefaultUserInteractor());
        clientOptions.setTesting(true);
        Resource r1 = registry.newCollection();
        Resource r2 = registry.newResource();
        registry.put(MYROOT + "/foo/bar/bingbingpeee", r1);

        // now we will get the checkout
        clientOptions.setUserUrl((RR_URL == null ? "" : RR_URL) + MYROOT);
        clientOptions.setUsername(RegistryConstants.ADMIN_USER);
        clientOptions.setPassword(RegistryConstants.ADMIN_PASSWORD);
        clientOptions.setWorkingLocation(MYCO);

        // after setting the option we will get the checkout
        new Checkout(clientOptions).execute(registry);

//        now deleting the resource
        registry.delete(MYROOT + "/foo/bar/bingbingpeee");

//        performing the update to see whether the resource get deleted in the file system
        Update update = new Update(clientOptions);
        update.execute(registry);

        assertEquals(0, update.getAddedCount());
        assertEquals(0, update.getConflictedCount());
        assertEquals(0, update.getUpdatedCount());
        assertEquals(1, update.getDeletedCount());
        cleanRegistry();
    }

    public void testSimpleCollectionUpdate() throws Exception {
        ClientOptions clientOptions = new ClientOptions();
        clientOptions.setUserInteractor(new DefaultUserInteractor());
        clientOptions.setTesting(true);
        Resource r1 = registry.newCollection();
        r1.setProperty("xxxx", "asdfjaksjdf");
        r1.setProperty("asdjf;k", "asdfjskfjsf");
        registry.put(MYROOT + "/bingbingpeee", r1);

        registry.addComment(MYROOT + "/bingbingpeee", new Comment("comasdfj asf;kajsdf asdf"));
        registry.addComment(MYROOT + "/bingbingpeee", new Comment("aj;lfdkjaskf asjdf;kajsdf;k"));

        registry.rateResource(MYROOT + "/bingbingpeee", 3);
        registry.applyTag(MYROOT + "/bingbingpeee", "abcdasfslapqdejf");
        registry.applyTag(MYROOT + "/bingbingpeee", "pisfsdfosdfasdk");

        Resource r2 = registry.newCollection();
        registry.put(MYROOT + "/hohohooooo", r2);

        registry.addAssociation(MYROOT + "/bingbingpeee", MYROOT + "/hohohooooo", "peek");


        // now we will get the checkout
        clientOptions.setUserUrl((RR_URL == null ? "" : RR_URL) + MYROOT);
        clientOptions.setUsername(RegistryConstants.ADMIN_USER);
        clientOptions.setPassword(RegistryConstants.ADMIN_PASSWORD);
        clientOptions.setWorkingLocation(MYCO);

        // after setting the option we will get the checkout
        new Checkout(clientOptions).execute(registry);

        // update the r1
        r1.setProperty("xxxx", "r1 value");
        registry.put(MYROOT + "/bingbingpeee", r1);

        clientOptions.setUserUrl((RR_URL == null ? "" : RR_URL) + MYROOT);
        Update update = new Update(clientOptions);

        update.execute(registry);
        assertEquals(0, update.getAddedCount());
        assertEquals(0, update.getConflictedCount());
        assertEquals(0, update.getDeletedCount());
        assertEquals(0, update.getNotDeletedCount());
        assertEquals(1, update.getUpdatedCount());

        // another update without commiting
        update = new Update(clientOptions);
        update.execute(registry);
        assertEquals(0, update.getAddedCount());
        assertEquals(0, update.getConflictedCount());
        assertEquals(0, update.getDeletedCount());
        assertEquals(0, update.getNotDeletedCount());
        assertEquals(0, update.getUpdatedCount());

        // then we will commit it back and get a new update
        new Checkin(clientOptions).execute(registry);
        // brand new update
        update = new Update(clientOptions);
        update.execute(registry);
        assertEquals(0, update.getAddedCount());
        assertEquals(0, update.getConflictedCount());
        assertEquals(0, update.getDeletedCount());
        assertEquals(0, update.getNotDeletedCount());
        assertEquals(0, update.getUpdatedCount());

        // we will do a put without doing any update, and check whether the update is on;
        registry.put(MYROOT + "/bingbingpeee", r1);
        update = new Update(clientOptions);
        update.execute(registry);
        assertEquals(0, update.getAddedCount());
        assertEquals(0, update.getConflictedCount());
        assertEquals(0, update.getDeletedCount());
        assertEquals(0, update.getNotDeletedCount());
        assertEquals(1, update.getUpdatedCount());

        // now we change comments, ratings, tags and associations

        registry.addComment(MYROOT + "/bingbingpeee", new Comment("asdfj ;dsfj dalala dalaa lsuwersf"));

        registry.rateResource(MYROOT + "/bingbingpeee", 4);
        registry.applyTag(MYROOT + "/bingbingpeee", "hmhmhmpee");

        Resource r6 = registry.newCollection();
        registry.put(MYROOT + "/kingkingkoo", r6);

        registry.addAssociation(MYROOT + "/bingbingpeee", MYROOT + "/kingkingkoo", "asdfbough");

        update = new Update(clientOptions);
        update.execute(registry);
        assertEquals(1, update.getAddedCount());
        assertEquals(0, update.getConflictedCount());
        assertEquals(0, update.getDeletedCount());
        assertEquals(0, update.getNotDeletedCount());
        // this should be removed when we make updatig comments, ratings, tags as an update to the resource
        //assertEquals(1, update.getUpdatedCount());

        // to test we will be checkin it back to different url
        clientOptions.setUserUrl((RR_URL == null ? "" : RR_URL) + MYROOT);
        // the answer to only question is yes, so we are putting it here
        new Checkin(clientOptions).execute(registry);

        // checking them back
        Resource r3 = registry.get(MYROOT + "/bingbingpeee");
        assertEquals("r1 value", r3.getProperty("xxxx"));
        assertEquals("asdfjskfjsf", r3.getProperty("asdjf;k"));

        assertEquals(4, registry.getRating(MYROOT + "/bingbingpeee", "admin"));

        Comment[] comments = registry.getComments(MYROOT + "/bingbingpeee");
        assertTrue("The commments are not checkedin correctly",
                (comments[0].getText().equals("comasdfj asf;kajsdf asdf") &&
                        comments[1].getText().equals("aj;lfdkjaskf asjdf;kajsdf;k") &&
                        comments[2].getText().equals("asdfj ;dsfj dalala dalaa lsuwersf")) ||
                        (comments[2].getText().equals("comasdfj asf;kajsdf asdf") &&
                                comments[0].getText().equals("aj;lfdkjaskf asjdf;kajsdf;k") &&
                                comments[1].getText().equals("asdfj ;dsfj dalala dalaa lsuwersf")) ||
                        (comments[1].getText().equals("comasdfj asf;kajsdf asdf") &&
                                comments[2].getText().equals("aj;lfdkjaskf asjdf;kajsdf;k") &&
                                comments[0].getText().equals("asdfj ;dsfj dalala dalaa lsuwersf")));

        // tags
        Tag[] tags = registry.getTags(MYROOT + "/bingbingpeee");
        assertTrue("The tags has not ben checkedin correctly",
                (tags[0].getTagName().equals("abcdasfslapqdejf") &&
                        tags[1].getTagName().equals("pisfsdfosdfasdk") &&
                        tags[2].getTagName().equals("hmhmhmpee")) ||
                        (tags[2].getTagName().equals("abcdasfslapqdejf") &&
                                tags[0].getTagName().equals("pisfsdfosdfasdk") &&
                                tags[1].getTagName().equals("hmhmhmpee")) ||
                        (tags[1].getTagName().equals("abcdasfslapqdejf") &&
                                tags[2].getTagName().equals("pisfsdfosdfasdk") &&
                                tags[0].getTagName().equals("hmhmhmpee")));

        Association[] assocs = registry.getAssociations(MYROOT + "/bingbingpeee", "peek");
        assertEquals(assocs[0].getAssociationType(), "peek");
        assertEquals(assocs[0].getSourcePath(), MYROOT + "/bingbingpeee");
        assertEquals(assocs[0].getDestinationPath(), MYROOT + "/hohohooooo");

        assocs = registry.getAssociations(MYROOT + "/bingbingpeee", "asdfbough");
        assertEquals(assocs[0].getAssociationType(), "asdfbough");
        assertEquals(assocs[0].getSourcePath(), MYROOT + "/bingbingpeee");
        assertEquals(assocs[0].getDestinationPath(), MYROOT + "/kingkingkoo");

        // so that has worked, now we gonna delete what we checkined extra
        cleanRegistry();
    }

    public void testAddDelete() throws Exception {
        ClientOptions clientOptions = new ClientOptions();
        clientOptions.setUserInteractor(new DefaultUserInteractor());
        clientOptions.setTesting(true);

        Resource r1 = registry.newCollection();
        registry.put(MYROOT + "/bingbingpeee", r1);

        // now we will get the checkout
        clientOptions.setUserUrl((RR_URL == null ? "" : RR_URL) + MYROOT);
        clientOptions.setUsername(RegistryConstants.ADMIN_USER);
        clientOptions.setPassword(RegistryConstants.ADMIN_PASSWORD);
        clientOptions.setWorkingLocation(MYCO);
        new File(MYCO).mkdirs();

        // after setting the option we will get the checkout
        new Checkout(clientOptions).execute(registry);

        new File(MYCO + "/bingbingpeee/a/b/c").mkdirs();
        File textFile = new File(MYCO + "/bingbingpeee/a/b/c/a.txt");
        textFile.createNewFile();
        FileWriter fileWriter = new FileWriter(textFile);
        fileWriter.append("This is the content");
        fileWriter.flush();

        clientOptions = new ClientOptions();
        clientOptions.setUserInteractor(new DefaultUserInteractor());
        clientOptions.setTesting(true);
        clientOptions.setTargetResource(MYCO + "/bingbingpeee/a");
        Add add = new Add(clientOptions);
        add.execute();

        clientOptions = new ClientOptions();
        clientOptions.setUserInteractor(new DefaultUserInteractor());
        clientOptions.setTesting(true);
        clientOptions.setUserUrl((RR_URL == null ? "" : RR_URL) + MYROOT);
        clientOptions.setUsername(RegistryConstants.ADMIN_USER);
        clientOptions.setPassword(RegistryConstants.ADMIN_PASSWORD);
        clientOptions.setWorkingLocation(MYCO);
        new Checkin(clientOptions).execute(registry);

        Assert.assertEquals(registry.resourceExists(MYROOT + "/bingbingpeee/a/b/c/a.txt"), true);

        Resource resource = registry.get(MYROOT + "/bingbingpeee/a/b/c/a.txt");
        Assert.assertEquals(new String((byte[]) resource.getContent()), "This is the content");

        clientOptions = new ClientOptions();
        clientOptions.setUserInteractor(new DefaultUserInteractor());
        clientOptions.setTesting(true);
        clientOptions.setTargetResource(MYCO + "/bingbingpeee/a");
        Delete delete = new Delete(clientOptions);
        delete.execute();

        clientOptions = new ClientOptions();
        clientOptions.setUserInteractor(new DefaultUserInteractor());
        clientOptions.setTesting(true);
        clientOptions.setUserUrl((RR_URL == null ? "" : RR_URL) + MYROOT);
        clientOptions.setUsername(RegistryConstants.ADMIN_USER);
        clientOptions.setPassword(RegistryConstants.ADMIN_PASSWORD);
        clientOptions.setWorkingLocation(MYCO);
        new Checkin(clientOptions).execute(registry);

        Assert.assertEquals(registry.resourceExists(MYROOT + "/bingbingpeee/a/b/c/a.txt"), false);

        cleanRegistry();
    }

    public void testUpdate() throws Exception {
        ClientOptions clientOptions = new ClientOptions();
        clientOptions.setUserInteractor(new DefaultUserInteractor());
        clientOptions.setTesting(true);

        Resource r2 = registry.newResource();
        r2.setContent("ABCD");
        registry.put(MYROOT + "/bingbingpeee/abc.txt", r2);

        // now we will get the checkout
        clientOptions.setUserUrl((RR_URL == null ? "" : RR_URL) + MYROOT);
        clientOptions.setUsername(RegistryConstants.ADMIN_USER);
        clientOptions.setPassword(RegistryConstants.ADMIN_PASSWORD);
        clientOptions.setWorkingLocation(MYCO);
        new File(MYCO).mkdirs();

        // after setting the option we will get the checkout
        new Checkout(clientOptions).execute(registry);

        File textFile = new File(MYCO + "/bingbingpeee/abc.txt");
        textFile.createNewFile();
        FileWriter fileWriter = new FileWriter(textFile);
        fileWriter.append("This is the content");
        fileWriter.flush();

        clientOptions = new ClientOptions();
        clientOptions.setUserInteractor(new DefaultUserInteractor());
        clientOptions.setTesting(true);
        clientOptions.setUserUrl((RR_URL == null ? "" : RR_URL) + MYROOT);
        clientOptions.setUsername(RegistryConstants.ADMIN_USER);
        clientOptions.setPassword(RegistryConstants.ADMIN_PASSWORD);
        clientOptions.setWorkingLocation(MYCO);
        new Checkin(clientOptions).execute(registry);

        Resource r = registry.get(MYROOT + "/bingbingpeee/abc.txt");
        assertEquals("This is the content", new String((byte[]) r.getContent()));
    }

    public void testStatus() throws Exception {
        ClientOptions clientOptions = new ClientOptions();
        clientOptions.setUserInteractor(new DefaultUserInteractor());
        clientOptions.setTesting(true);

        Resource r2 = registry.newResource();
        r2.setContent("ABCD");
        registry.put(MYROOT + "/bingbingpeee/abc1.txt", r2);

        r2.setContent("XYZA");
        registry.put(MYROOT + "/bingbingpeee/abc2.txt", r2);

        // now we will get the checkout
        clientOptions.setUserUrl((RR_URL == null ? "" : RR_URL) + MYROOT);
        clientOptions.setUsername(RegistryConstants.ADMIN_USER);
        clientOptions.setPassword(RegistryConstants.ADMIN_PASSWORD);
        clientOptions.setWorkingLocation(MYCO);
        new File(MYCO).mkdirs();

        // after setting the option we will get the checkout
        new Checkout(clientOptions).execute(registry);

        File file = new File(MYCO + "/bingbingpeee/abc2.txt");
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write("This is the content");
        fileWriter.flush();

        clientOptions = new ClientOptions();
        clientOptions.setTargetResource(MYCO + "/bingbingpeee/abc2.txt");
        Status status = new Status(clientOptions);
        status.execute();

        List<String> updatedResourceList = status.getUpdated();
        Assert.assertEquals(updatedResourceList.get(0), MYCO + "/bingbingpeee/abc2.txt");

        File textFile = new File(MYCO + "/bingbingpeee/abc3.txt");
        textFile.createNewFile();
        fileWriter = new FileWriter(textFile);
        fileWriter.append("This is the content");
        fileWriter.flush();

        clientOptions = new ClientOptions();
        clientOptions.setUserInteractor(new DefaultUserInteractor());
        clientOptions.setTesting(true);
        clientOptions.setTargetResource(MYCO + "/bingbingpeee/abc3.txt");
        Add add = new Add(clientOptions);
        add.execute();

        clientOptions = new ClientOptions();
        clientOptions.setTargetResource(MYCO + "/bingbingpeee/abc3.txt");
        status = new Status(clientOptions);
        status.execute();

        List<String> addedResourceList = status.getAdded();
        Assert.assertEquals(addedResourceList.get(0), MYCO + "/bingbingpeee/abc3.txt");

        clientOptions = new ClientOptions();
        clientOptions.setUserInteractor(new DefaultUserInteractor());
        clientOptions.setTesting(true);
        clientOptions.setTargetResource(MYCO + "/bingbingpeee/abc1.txt");
        Delete delete = new Delete(clientOptions);
        delete.execute();

        clientOptions = new ClientOptions();
        clientOptions.setTargetResource(MYCO + "/bingbingpeee/abc1.txt");
        status = new Status(clientOptions);
        status.execute();

        List<String> deletedResourceList = status.getDeleted();
        Assert.assertEquals(deletedResourceList.get(0), MYCO + "/bingbingpeee/abc1.txt");

        cleanRegistry();
    }

    public void testSetProperties() throws Exception {
        ClientOptions clientOptions = new ClientOptions();
        clientOptions.setUserInteractor(new DefaultUserInteractor());
        clientOptions.setTesting(true);

        Resource r1 = registry.newCollection();
        registry.put(MYROOT + "/bingbingpeee", r1);

        // now we will get the checkout
        clientOptions.setUserUrl((RR_URL == null ? "" : RR_URL) + MYROOT);
        clientOptions.setUsername(RegistryConstants.ADMIN_USER);
        clientOptions.setPassword(RegistryConstants.ADMIN_PASSWORD);
        clientOptions.setWorkingLocation(MYCO);
        new File(MYCO).mkdirs();

        // after setting the option we will get the checkout
        new Checkout(clientOptions).execute(registry);

        new File(MYCO + "/bingbingpeee/a/b/c").mkdirs();
        File textFile = new File(MYCO + "/bingbingpeee/a/b/c/a.txt");
        textFile.createNewFile();
        FileWriter fileWriter = new FileWriter(textFile);
        fileWriter.append("This is the content");
        fileWriter.flush();

        clientOptions = new ClientOptions();
        clientOptions.setUserInteractor(new DefaultUserInteractor());
        clientOptions.setTesting(true);
        clientOptions.setTargetResource(MYCO + "/bingbingpeee/a");
        Add add = new Add(clientOptions);
        add.execute();

        clientOptions = new ClientOptions();
        clientOptions.setUserInteractor(new DefaultUserInteractor());
        clientOptions.setTesting(true);
        clientOptions.setUserUrl((RR_URL == null ? "" : RR_URL) + MYROOT);
        clientOptions.setUsername(RegistryConstants.ADMIN_USER);
        clientOptions.setPassword(RegistryConstants.ADMIN_PASSWORD);
        clientOptions.setWorkingLocation(MYCO);
        new Checkin(clientOptions).execute(registry);

        clientOptions = new ClientOptions();
        clientOptions.setUserInteractor(new DefaultUserInteractor());
        clientOptions.setTesting(true);
        clientOptions.setTargetResource(MYCO + "/bingbingpeee/a/b/c/a.txt");
        Map<String, String> propertyMap = new HashMap<String, String>();
        propertyMap.put("a", "b");
        propertyMap.put("x", "y");
        propertyMap.put("n", "m");
        clientOptions.setProperties(propertyMap);
        PropSet propSet = new PropSet(clientOptions);
        propSet.execute();

        clientOptions = new ClientOptions();
        clientOptions.setUserInteractor(new DefaultUserInteractor());
        clientOptions.setTesting(true);
        clientOptions.setUserUrl((RR_URL == null ? "" : RR_URL) + MYROOT);
        clientOptions.setUsername(RegistryConstants.ADMIN_USER);
        clientOptions.setPassword(RegistryConstants.ADMIN_PASSWORD);
        clientOptions.setWorkingLocation(MYCO);
        new Checkin(clientOptions).execute(registry);

        Assert.assertEquals(registry.resourceExists(MYROOT + "/bingbingpeee/a/b/c/a.txt"), true);

        Resource resource = registry.get(MYROOT + "/bingbingpeee/a/b/c/a.txt");
        Assert.assertEquals(resource.getProperty("a"), "b");
        Assert.assertEquals(resource.getProperty("x"), "y");
        Assert.assertEquals(resource.getProperty("n"), "m");

        cleanRegistry();
    }

    public void testDeleteProperties() throws Exception {
        ClientOptions clientOptions = new ClientOptions();
        clientOptions.setUserInteractor(new DefaultUserInteractor());
        clientOptions.setTesting(true);
        Resource resource = registry.newResource();
        resource.setContent("I am the best");
        resource.setUUID(UUID.randomUUID().toString());
        resource.setProperty("a", "b");
        resource.setProperty("x", "y");
        resource.setProperty("c", "d");
        registry.put(MYROOT + "/bingbingpeee/a/b/c/a.txt", resource);

        Assert.assertEquals(registry.resourceExists(MYROOT + "/bingbingpeee/a/b/c/a.txt"), true);
        resource = registry.get(MYROOT + "/bingbingpeee/a/b/c/a.txt");
        Assert.assertEquals(resource.getProperty("a"), "b");
        Assert.assertEquals(resource.getProperty("x"), "y");
        Assert.assertEquals(resource.getProperty("c"), "d");

        // now we will get the checkout
        clientOptions.setUserUrl((RR_URL == null ? "" : RR_URL) + MYROOT);
        clientOptions.setUsername(RegistryConstants.ADMIN_USER);
        clientOptions.setPassword(RegistryConstants.ADMIN_PASSWORD);
        clientOptions.setWorkingLocation(MYCO);
        new File(MYCO).mkdirs();

        // after setting the option we will get the checkout
        new Checkout(clientOptions).execute(registry);

        clientOptions = new ClientOptions();
        clientOptions.setUserInteractor(new DefaultUserInteractor());
        clientOptions.setTesting(true);
        clientOptions.setTargetResource(MYCO + "/bingbingpeee/a/b/c/a.txt");
        Set<String> deletePropertySet = new HashSet<String>();
        deletePropertySet.add("a");
        deletePropertySet.add("x");
        clientOptions.setDeletedProperties(deletePropertySet);
        PropSet propSet = new PropSet(clientOptions);
        propSet.execute();
        clientOptions.setDeletedProperties(deletePropertySet);
        PropDelete propDelete = new PropDelete(clientOptions);
        propDelete.execute();

        clientOptions = new ClientOptions();
        clientOptions.setUserInteractor(new DefaultUserInteractor());
        clientOptions.setTesting(true);
        clientOptions.setUserUrl((RR_URL == null ? "" : RR_URL) + MYROOT);
        clientOptions.setUsername(RegistryConstants.ADMIN_USER);
        clientOptions.setPassword(RegistryConstants.ADMIN_PASSWORD);
        clientOptions.setWorkingLocation(MYCO);
        new Checkin(clientOptions).execute(registry);

        Assert.assertEquals(registry.resourceExists(MYROOT + "/bingbingpeee/a/b/c/a.txt"), true);

        resource = registry.get(MYROOT + "/bingbingpeee/a/b/c/a.txt");
        Assert.assertEquals(resource.getProperty("a"), null);
        Assert.assertEquals(resource.getProperty("x"), null);
        Assert.assertEquals(resource.getProperty("c"), "d");
    }

    public void testConflicts() throws Exception {
        ClientOptions clientOptions = new ClientOptions();
        clientOptions.setUserInteractor(new DefaultUserInteractor());
        clientOptions.setTesting(true);
        Resource r1 = registry.newResource();
        r1.setContent("r1 content");
        r1.setProperty("xxxx", "asdfjaksjdf");
        r1.setProperty("asdjf;k", "asdfjskfjsf");
        registry.put(MYROOT + "/bingbingpeee", r1);

        registry.addComment(MYROOT + "/bingbingpeee", new Comment("comasdfj asf;kajsdf asdf"));
        registry.addComment(MYROOT + "/bingbingpeee", new Comment("aj;lfdkjaskf asjdf;kajsdf;k"));

        registry.rateResource(MYROOT + "/bingbingpeee", 3);
        registry.applyTag(MYROOT + "/bingbingpeee", "abcdasfslapqdejf");
        registry.applyTag(MYROOT + "/bingbingpeee", "pisfsdfosdfasdk");

        Resource r2 = registry.newResource();
        r2.setContent("tare;akjfs;dkfajklfj;akfd sj;lkajs fd;klajsk;dlfj a;dfj");
        registry.put(MYROOT + "/hohohooooo", r2);

        registry.addAssociation(MYROOT + "/bingbingpeee", MYROOT + "/hohohooooo", "peek");

        // now we will get the checkout
        clientOptions.setUserUrl((RR_URL == null ? "" : RR_URL) + MYROOT);
        clientOptions.setUsername(RegistryConstants.ADMIN_USER);
        clientOptions.setPassword(RegistryConstants.ADMIN_PASSWORD);
        clientOptions.setWorkingLocation(MYCO);
        new File(MYCO).mkdirs();

        // after setting the option we will get the checkout
        new Checkout(clientOptions).execute(registry);


        // update the r1 with the same content
        r1.setContent("r1 content - the server is changed");
        registry.put(MYROOT + "/bingbingpeee", r1);

        Thread.currentThread().sleep(1000);

        File file1 = new File(MYCO + "/bingbingpeee");
        // but the client is not changed, just the same string is written.
        writeToFile(file1, "r1 content");

        clientOptions.setUserUrl((RR_URL == null ? "" : RR_URL) + MYROOT);
        Update update = new Update(clientOptions);

        update.execute(registry);
        assertEquals(0, update.getAddedCount());
        assertEquals(0, update.getConflictedCount());
        assertEquals(0, update.getDeletedCount());
        assertEquals(0, update.getNotDeletedCount());
        assertEquals(1, update.getUpdatedCount());


        // update the r1 with the same content
        r1.setContent("r1 content - same content");
        registry.put(MYROOT + "/bingbingpeee", r1);

        Thread.currentThread().sleep(1000);

        file1 = new File(MYCO + "/bingbingpeee");
        writeToFile(file1, "r1 content - same content");

        clientOptions.setUserUrl((RR_URL == null ? "" : RR_URL) + MYROOT);
        update = new Update(clientOptions);

        update.execute(registry);
        assertEquals(0, update.getAddedCount());
        assertEquals(0, update.getConflictedCount());
        assertEquals(0, update.getDeletedCount());
        assertEquals(0, update.getNotDeletedCount());
        assertEquals(1, update.getUpdatedCount());

        // update the r1 -expecting a conflict
        r1.setContent("r1 content - added from remote registry");
        registry.put(MYROOT + "/bingbingpeee", r1);

        Thread.currentThread().sleep(1000);

        file1 = new File(MYCO + "/bingbingpeee");
        writeToFile(file1, "r1 content - added to local file system");

        clientOptions.setUserUrl((RR_URL == null ? "" : RR_URL) + MYROOT);
        update = new Update(clientOptions);

        update.execute(registry);
        assertEquals(0, update.getAddedCount());
        assertEquals(1, update.getConflictedCount());
        assertEquals(0, update.getDeletedCount());
        assertEquals(0, update.getNotDeletedCount());
        assertEquals(0, update.getUpdatedCount());

        cleanRegistry();
    }


    public void testDeleteResources() throws Exception {
        ClientOptions clientOptions = new ClientOptions();
        clientOptions.setUserInteractor(new DefaultUserInteractor());
        clientOptions.setTesting(true);
        Resource r1 = registry.newResource();
        r1.setContent("r1x content");
        r1.setProperty("xxxx", "asdfjaksjdf");
        r1.setProperty("asdjf;k", "asdfjskfjsf");
        registry.put(MYROOT + "/lamblabm", r1);


        Resource r2 = registry.newResource();
        r2.setContent("tare;akjfs;dkfajsfsdfajs fd;klajsk;dlfj a;dfj");
        registry.put(MYROOT + "/dingdingdioo", r2);

        Resource r3 = registry.newResource();
        r3.setContent("taresdfajs fd;klajsk;dlfj a;dfj");
        registry.put(MYROOT + "/ldingdingdioo", r3);


        // now we will get the checkout
        clientOptions.setUserUrl((RR_URL == null ? "" : RR_URL) + MYROOT);
        clientOptions.setUsername(RegistryConstants.ADMIN_USER);
        clientOptions.setPassword(RegistryConstants.ADMIN_PASSWORD);
        clientOptions.setWorkingLocation(MYCO);
        new File(MYCO).mkdirs();

        // after setting the option we will get the checkout
        new Checkout(clientOptions).execute(registry);

        File file1 = new File(MYCO + "/lamblabm");
        String content = readFile(file1);
        assertTrue("checkouted file should exist", file1.exists());
        assertEquals("r1x content", content);

        //lets delete the file1 and checkin
        ClientOptions deleteClientOptions = new ClientOptions();
        deleteClientOptions.setTargetResource(MYCO + "/lamblabm");
        Delete delete = new Delete(deleteClientOptions);
        delete.execute();

        new Checkin(clientOptions).execute(registry);

        // check the thing no exist?
        assertFalse(registry.resourceExists(MYROOT + "/lamblabm"));


        File file2 = new File(MYCO + "/dingdingdioo");
        content = readFile(file2);
        assertTrue("checkouted file should exist", file2.exists());
        assertEquals("tare;akjfs;dkfajsfsdfajs fd;klajsk;dlfj a;dfj", content);

        // doing the other way around. delete the server side and get an update
        registry.delete(MYROOT + "/dingdingdioo");

        // now get an up
        new Update(clientOptions).execute(registry);

        // check whether the file doesn't exist
        File file3 = new File(MYCO + "/dingdingdioo");
        assertFalse(file3.exists());

        // make sure the non deleted file exists
        File file4 = new File(MYCO + "/ldingdingdioo");
        content = readFile(file4);
        assertTrue("checkouted file should exist", file4.exists());
        assertEquals("taresdfajs fd;klajsk;dlfj a;dfj", content);

        // create an empty file and check whether it is get deleted from an update.

        File file5 = new File(MYCO + "/the-local-new-file");
        writeToFile(file5, "some random stuff");

        new Update(clientOptions).execute(registry);

        File file6 = new File(MYCO + "/the-local-new-file");
        content = readFile(file6);
        assertTrue("checkouted file should exist", file6.exists());
        assertEquals("some random stuff", content);

        cleanRegistry();
    }

    public void testDeleteCollections() throws Exception {
        ClientOptions clientOptions = new ClientOptions();
        clientOptions.setUserInteractor(new DefaultUserInteractor());
        clientOptions.setTesting(true);
        Resource r1 = registry.newCollection();
        r1.setProperty("xxxx", "asdfjaksjdf");
        r1.setProperty("asdjf;k", "asdfjskfjsf");
        registry.put(MYROOT + "/lamblabm", r1);


        Resource r2 = registry.newCollection();
        registry.put(MYROOT + "/dingdingdioo", r2);

        Resource r3 = registry.newCollection();
        registry.put(MYROOT + "/ldingdingdioo", r3);

        assertTrue(registry.resourceExists(MYROOT + "/ldingdingdioo"));

        // now we will get the checkout
        clientOptions.setUserUrl((RR_URL == null ? "" : RR_URL) + MYROOT);
        clientOptions.setUsername(RegistryConstants.ADMIN_USER);
        clientOptions.setPassword(RegistryConstants.ADMIN_PASSWORD);
        clientOptions.setWorkingLocation(MYCO);
        new File(MYCO).mkdirs();

        // after setting the option we will get the checkout
        new Checkout(clientOptions).execute(registry);

        File file1 = new File(MYCO + "/lamblabm");
        assertTrue("checkouted file should exist", file1.exists());
        assertTrue(file1.isDirectory());

        ClientOptions deleteClientOptions = new ClientOptions();
        deleteClientOptions.setTargetResource(MYCO + "/lamblabm");
        Delete delete = new Delete(deleteClientOptions);
        delete.execute();

        clientOptions.setUserUrl((RR_URL == null ? "" : RR_URL) + MYROOT);
        new Checkin(clientOptions).execute(registry);

        // check the thing no exist?
        assertFalse(registry.resourceExists(MYROOT + "/lamblabm"));


        File file2 = new File(MYCO + "/dingdingdioo");
        assertTrue("checkouted file should exist", file2.exists());
        assertTrue(file2.isDirectory());

        // doing the other way around. delete the server side and get an update
        registry.delete(MYROOT + "/dingdingdioo");
        assertTrue(registry.resourceExists(MYROOT + "/ldingdingdioo"));

        // now get an up
        clientOptions.setUserUrl((RR_URL == null ? "" : RR_URL) + MYROOT);
        new Update(clientOptions).execute(registry);

        // check whether the file doesn't exist
        File file3 = new File(MYCO + "/dingdingdioo");
        assertFalse(file3.exists());

        // make sure the non deleted file exists
        File file4 = new File(MYCO + "/ldingdingdioo");
        assertTrue("checkouted file should exist", file4.exists());
        assertTrue(file4.isDirectory());

        // create an empty file and check whether it is get deleted from an update.

        File file5 = new File(MYCO + "/the-local-new-file");
        file5.mkdirs();

        new Update(clientOptions).execute(registry);

        File file6 = new File(MYCO + "/the-local-new-file");
        assertTrue("checkouted file should exist", file6.exists());
        assertTrue(file6.isDirectory());

        cleanRegistry();
    }


    String readFile(File file) throws Exception {
        InputStream fileContent = new FileInputStream(file);
        StringBuffer content = new StringBuffer();
        while (fileContent.available() > 0) {
            char c = (char) fileContent.read();
            content.append(c);
        }
        String returnValue = content.toString();
        fileContent.close();
        return returnValue;
    }

    void writeToFile(File file, String content) throws Exception {
        FileOutputStream stream = new FileOutputStream(file);
        for (int i = 0; i < content.length(); i++) {
            char a = content.charAt(i);
            stream.write(a);
        }
        stream.close();
    }

    public void testBuildMessage() {
        String msg = UserInteractor.PARAMETER_PLACE_HOLDER +
                "This is (" + UserInteractor.PARAMETER_PLACE_HOLDER + ", " +
                UserInteractor.PARAMETER_PLACE_HOLDER + ", " +
                UserInteractor.PARAMETER_PLACE_HOLDER + ", " +
                UserInteractor.PARAMETER_PLACE_HOLDER + ")";
        String[] parameters = {"aasdfa", "dsfb", "adfasdf1", "asdfsdf2", "1234"};
        String buildMsg = new DefaultUserInteractor().buildMessage(msg, parameters);
        assertEquals("aasdfaThis is (dsfb, adfasdf1, asdfsdf2, 1234)", buildMsg);
        assertEquals(5, new DefaultUserInteractor().derivePlaceHolderCount(msg));
    }

    // check update without giving url as an option
    public void testUpdateWithoutUrlOption() throws Exception {
        ClientOptions clientOptions = new ClientOptions();
        clientOptions.setUserInteractor(new DefaultUserInteractor());
        clientOptions.setTesting(true);
        Resource r1 = registry.newResource();
        r1.setContent("r1-bang content");
        r1.setProperty("xxxx", "asdfjaksjdf");
        r1.setProperty("asdjf;k", "asdfjskfjsf");
        registry.put(MYROOT + "/asdfxdf", r1);

        // now we will get the checkout
        clientOptions.setUserUrl((RR_URL == null ? "" : RR_URL) + MYROOT);
        clientOptions.setUsername(RegistryConstants.ADMIN_USER);
        clientOptions.setPassword(RegistryConstants.ADMIN_PASSWORD);
        clientOptions.setWorkingLocation(MYCO);
        new File(MYCO).mkdirs();

        // after setting the option we will get the checkout
        new Checkout(clientOptions).execute(registry);

        assertEquals("r1-bang content", readFile(new File(MYCO + "/asdfxdf")));

        // now update the r1
        r1 = registry.newResource();
        r1.setContent("r1-bang bang content");
        r1.setProperty("a", "asdfjaksjdf");
        r1.setProperty("b", "asdfjskfjsf");
        registry.put(MYROOT + "/asdfxdf", r1);

        // create a new client option.
        ClientOptions clientOptions2 = new ClientOptions();
        clientOptions2.setUserInteractor(new DefaultUserInteractor());
        clientOptions2.setTesting(true);
        clientOptions2.setUsername(RegistryConstants.ADMIN_USER);
        clientOptions2.setPassword(RegistryConstants.ADMIN_PASSWORD);
        clientOptions2.setWorkingLocation(MYCO);

        // after setting the option we will get the checkout
        new Update(clientOptions2).execute(registry);

        assertEquals("r1-bang bang content", readFile(new File(MYCO + "/asdfxdf")));

        cleanRegistry();
    }

    public void testWiredResourceTrees() throws Exception {

        ClientOptions clientOptions = new ClientOptions();
        clientOptions.setUserInteractor(new DefaultUserInteractor());
        clientOptions.setTesting(true);
        Resource r1 = registry.newResource();
        r1.setContent("r1-content");
        registry.put(MYROOT + "/a/b/c/d/e/f", r1);

        Resource r2 = registry.newResource();
        r2.setContent("r2-content");
        registry.put(MYROOT + "/a/b/g/h/i", r2);

        Resource r3 = registry.newResource();
        r3.setContent("r3-content");
        registry.put(MYROOT + "/a/j", r3);

        // now get the checkout
        // now we will get the checkout
        clientOptions.setUserUrl((RR_URL == null ? "" : RR_URL) + MYROOT);
        clientOptions.setUsername(RegistryConstants.ADMIN_USER);
        clientOptions.setPassword(RegistryConstants.ADMIN_PASSWORD);
        clientOptions.setWorkingLocation(MYCO);
        new File(MYCO).mkdirs();

        new Checkout(clientOptions).execute(registry);

        assertEquals("r1-content", readFile(new File(MYCO + "/a/b/c/d/e/f")));
        assertEquals("r2-content", readFile(new File(MYCO + "/a/b/g/h/i")));
        assertEquals("r3-content", readFile(new File(MYCO + "/a/j")));

        r1.setContent("r1-up1-content");
        registry.put(MYROOT + "/a/b/c/d/e/f", r1);

        r2.setContent("r2-up1-content");
        registry.put(MYROOT + "/a/b/g/h/i", r2);

        r3.setContent("r3-up1-content");
        registry.put(MYROOT + "/a/j", r3);
        // now get an update
        Update update = new Update(clientOptions);
        update.execute(registry);

        assertEquals("r1-up1-content", readFile(new File(MYCO + "/a/b/c/d/e/f")));
        assertEquals("r2-up1-content", readFile(new File(MYCO + "/a/b/g/h/i")));
        assertEquals("r3-up1-content", readFile(new File(MYCO + "/a/j")));

        writeToFile(new File(MYCO + "/a/b/c/d/e/f"), "r1-boom");
        writeToFile(new File(MYCO + "/a/b/g/h/i"), "r2-boom");
        writeToFile(new File(MYCO + "/a/j"), "r3-boom");

        new Checkin(clientOptions).execute(registry);

        r1 = registry.get(MYROOT + "/a/b/c/d/e/f");
        r2 = registry.get(MYROOT + "/a/b/g/h/i");
        r3 = registry.get(MYROOT + "/a/j");

        assertEquals("r1-boom", new String((byte[]) r1.getContent()));
        assertEquals("r2-boom", new String((byte[]) r2.getContent()));
        assertEquals("r3-boom", new String((byte[]) r3.getContent()));
        // so that has worked, now we gonna delete what we checkined extra
        cleanRegistry();
    }

    public void testWiredCollectionTrees() throws Exception {

        ClientOptions clientOptions = new ClientOptions();
        clientOptions.setUserInteractor(new DefaultUserInteractor());
        clientOptions.setTesting(true);
        Resource r1 = registry.newCollection();
        registry.put(MYROOT + "/a/b/c/d/e/f", r1);

        Resource r2 = registry.newCollection();
        registry.put(MYROOT + "/a/b/g/h/i", r2);

        Resource r3 = registry.newCollection();
        registry.put(MYROOT + "/a/j", r3);

        // now get the checkout
        // now we will get the checkout
        clientOptions.setUserUrl((RR_URL == null ? "" : RR_URL) + MYROOT);
        clientOptions.setUsername(RegistryConstants.ADMIN_USER);
        clientOptions.setPassword(RegistryConstants.ADMIN_PASSWORD);
        clientOptions.setWorkingLocation(MYCO);
        new File(MYCO).mkdirs();

        new Checkout(clientOptions).execute(registry);

        // now get an update
        Update update = new Update(clientOptions);
        update.execute(registry);

        assertEquals(0, update.getAddedCount());

        assertTrue(new File(MYCO + "/a/b/c/d/e/f").exists());
        assertTrue(new File(MYCO + "/a/b/g/h/i").exists());
        assertTrue(new File(MYCO + "/a/j").exists());

        // so that has worked, now we gonna delete what we checkined extra
        cleanRegistry();
    }

    public void testFileCheckinCheckout() throws Exception {
        deleteDir(new File(MYCO));
        cleanRegistry();

        ClientOptions clientOptions = new ClientOptions();
        clientOptions.setUserInteractor(new DefaultUserInteractor());
        clientOptions.setTesting(true);
        Resource r1 = registry.newResource();
        r1.setContent("r1 content");
        r1.setProperty("xxxx", "asdfjaksjdf");
        r1.setProperty("asdjf;k", "asdfjskfjsf");
        registry.put(MYROOT + "/bingbingpeee2", r1);

        registry.addComment(MYROOT + "/bingbingpeee2", new Comment("comasdfj asf;kajsdf asdf"));
        registry.addComment(MYROOT + "/bingbingpeee2", new Comment("aj;lfdkjaskf asjdf;kajsdf;k"));

        registry.rateResource(MYROOT + "/bingbingpeee2", 3);
        registry.applyTag(MYROOT + "/bingbingpeee2", "abcdasfslapqdejf");
        registry.applyTag(MYROOT + "/bingbingpeee2", "pisfsdfosdfasdk");

        Resource r2 = registry.newResource();
        r2.setContent("tare;akjfs;dkfajklfj;akfd sj;lkajs fd;klajsk;dlfj a;dfj");
        registry.put(MYROOT + "/hohohooooo", r2);

        registry.addAssociation(MYROOT + "/bingbingpeee2", MYROOT + "/hohohooooo", "peek");


        // now we will get the checkout
        clientOptions.setUserUrl((RR_URL == null ? "" : RR_URL) + MYROOT);
        clientOptions.setUsername(RegistryConstants.ADMIN_USER);
        clientOptions.setPassword(RegistryConstants.ADMIN_PASSWORD);
        clientOptions.setWorkingLocation(MYCO);
        clientOptions.setOutputFile("myoutput.zip");
        new File(MYCO).mkdirs();

        // after setting the option we will get the checkout
        new Checkout(clientOptions).execute(registry);

        // to test we will be checkin it back to different url
        clientOptions.setUserUrl((RR_URL == null ? "" : RR_URL) + MYROOT);
        // the answer to only question is yes, so we are putting it here
        new Checkin(clientOptions).execute(registry);

        // checking them back
        Resource r3 = registry.get(MYROOT + "/bingbingpeee2");
        assertEquals("asdfjaksjdf", r3.getProperty("xxxx"));
        assertEquals("asdfjskfjsf", r3.getProperty("asdjf;k"));

        assertEquals(3, registry.getRating(MYROOT + "/bingbingpeee2", "admin"));

        Comment[] comments = registry.getComments(MYROOT + "/bingbingpeee2");
        assertTrue("The commments are not checkedin correctly",
                (comments[0].getText().equals("comasdfj asf;kajsdf asdf") &&
                        comments[1].getText().equals("aj;lfdkjaskf asjdf;kajsdf;k")) ||
                        (comments[1].getText().equals("comasdfj asf;kajsdf asdf") &&
                                comments[0].getText().equals("aj;lfdkjaskf asjdf;kajsdf;k")));

        // tags
        Tag[] tags = registry.getTags(MYROOT + "/bingbingpeee2");
        assertTrue("The tags has not ben checkedin correctly",
                (tags[0].getTagName().equals("abcdasfslapqdejf") && tags[1].getTagName().equals("pisfsdfosdfasdk") ||
                        tags[1].getTagName().equals("abcdasfslapqdejf") && tags[0].getTagName().equals("pisfsdfosdfasdk")));

        Association[] assocs = registry.getAllAssociations(MYROOT + "/bingbingpeee2");
        assertEquals(assocs[0].getAssociationType(), "peek");
        assertEquals(assocs[0].getSourcePath(), MYROOT + "/bingbingpeee2");
        assertEquals(assocs[0].getDestinationPath(), MYROOT + "/hohohooooo");

        // so that has worked, now we gonna delete what we checkined extra
        cleanRegistry();
    }

    public void testOnlyOneFileDeleteCi() throws Exception {

        deleteDir(new File(MYCO));
        cleanRegistry();

        Resource r1 = registry.newResource();
        r1.setContent("r1 content");
        registry.put(MYROOT + "/mydir1/lambdapampda", r1);
        assertTrue(registry.resourceExists(MYROOT + "/mydir1/lambdapampda"));

        new File(MYCO).mkdirs();

        ClientOptions clientOptions = new ClientOptions();
        clientOptions.setUserInteractor(new DefaultUserInteractor());
        clientOptions.setTesting(true);

        // now we will get the checkout
        clientOptions.setUserUrl((RR_URL == null ? "" : RR_URL) + MYROOT);
        clientOptions.setUsername(RegistryConstants.ADMIN_USER);
        clientOptions.setPassword(RegistryConstants.ADMIN_PASSWORD);
        clientOptions.setWorkingLocation(MYCO);
        Checkout checkout = new Checkout(clientOptions);
        checkout.execute(registry);

        File file1 = new File(MYCO + "/mydir1/lambdapampda");
        assertTrue("checkouted file should exist", file1.exists());
        String content1 = readFile(file1);
        assertEquals("r1 content", content1);

        clientOptions = new ClientOptions();
        clientOptions.setUserInteractor(new DefaultUserInteractor());
        clientOptions.setTesting(true);
        clientOptions.setTargetResource(MYCO + "/mydir1/lambdapampda");
        Delete delete = new Delete(clientOptions);
        delete.execute();

        clientOptions = new ClientOptions();
        clientOptions.setUserInteractor(new DefaultUserInteractor());
        clientOptions.setTesting(true);
        clientOptions.setUsername(RegistryConstants.ADMIN_USER);
        clientOptions.setPassword(RegistryConstants.ADMIN_PASSWORD);
        clientOptions.setWorkingLocation(MYCO);
        clientOptions.setUserUrl((RR_URL == null ? "" : RR_URL) + MYROOT);
        Checkin checkin = new Checkin(clientOptions);
        checkin.execute(registry);

        assertFalse(registry.resourceExists(MYROOT + "/mydir1/lambdapampda"));

        File file2 = new File(MYCO + "/mydir1/lambdapampda");
        assertFalse("deleted file should not exist", file2.exists());

        checkin = new Checkin(clientOptions);
        checkin.execute(registry);

        assertFalse(registry.resourceExists("/mydir1/lambdapampda"));

        deleteDir(new File(MYCO));
        cleanRegistry();
    }

    public void testDeleteLocally() throws Exception {

        deleteDir(new File(MYCO));
        cleanRegistry();

        ClientOptions clientOptions = new ClientOptions();
        clientOptions.setUserInteractor(new DefaultUserInteractor());
        clientOptions.setTesting(true);
        Resource r1 = registry.newResource();
        r1.setContent("r1 content");
        registry.put(MYROOT + "/mydir1/r1", r1);

        Resource r2 = registry.newResource();
        r1.setContent("r2 content");
        registry.put(MYROOT + "/mydir1/r2", r2);


        // now we will get the checkout
        clientOptions.setUserUrl((RR_URL == null ? "" : RR_URL) + MYROOT);
        clientOptions.setUsername(RegistryConstants.ADMIN_USER);
        clientOptions.setPassword(RegistryConstants.ADMIN_PASSWORD);
        clientOptions.setWorkingLocation(MYCO);
        new File(MYCO).mkdirs();

        Checkout checkout = new Checkout(clientOptions);

        checkout.execute(registry);

        File file1 = new File(MYCO + "/mydir1/r1");
        assertTrue("checkouted file should exist", file1.exists());
        String content1 = readFile(file1);
        assertEquals("r1 content", content1);

        file1.delete();
        assertFalse("deleted file should not be updated", file1.exists());

        Update update = new Update(clientOptions);
        update.execute(registry);

        file1 = new File(MYCO + "/mydir1/r1");
        assertTrue("checkouted file should be deleted", !file1.exists());

        deleteDir(new File(MYCO));
        cleanRegistry();
    }
}
