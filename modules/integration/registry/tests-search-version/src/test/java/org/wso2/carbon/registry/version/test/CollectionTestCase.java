/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.registry.version.test;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.governance.LifeCycleAdminServiceClient;
import org.wso2.carbon.automation.api.clients.governance.LifeCycleManagementClient;
import org.wso2.carbon.automation.api.clients.registry.InfoServiceAdminClient;
import org.wso2.carbon.automation.api.clients.registry.PropertiesAdminServiceClient;
import org.wso2.carbon.automation.api.clients.registry.RelationAdminServiceClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.api.clients.registry.SearchAdminServiceClient;
import org.wso2.carbon.automation.api.clients.user.mgt.UserManagementClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.core.utils.fileutils.FileManager;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.info.stub.RegistryExceptionException;
import org.wso2.carbon.registry.info.stub.beans.utils.xsd.Tag;
import org.wso2.carbon.registry.info.stub.beans.xsd.CommentBean;
import org.wso2.carbon.registry.info.stub.beans.xsd.RatingBean;
import org.wso2.carbon.registry.info.stub.beans.xsd.TagBean;
import org.wso2.carbon.registry.lifecycle.test.utils.LifeCycleUtils;
import org.wso2.carbon.registry.permission.test.utils.PermissionTestConstants;
import org.wso2.carbon.registry.properties.stub.PropertiesAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.properties.stub.utils.xsd.Property;
import org.wso2.carbon.registry.relations.stub.AddAssociationRegistryExceptionException;
import org.wso2.carbon.registry.relations.stub.beans.xsd.AssociationTreeBean;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.beans.xsd.CollectionContentBean;
import org.wso2.carbon.registry.resource.stub.beans.xsd.PermissionBean;
import org.wso2.carbon.registry.resource.stub.beans.xsd.VersionPath;
import org.wso2.carbon.registry.resource.stub.common.xsd.ResourceData;
import org.wso2.carbon.registry.search.stub.SearchAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.version.test.utils.VersionUtils;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import javax.activation.DataHandler;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

public class CollectionTestCase {

    private static final String COLLECTION_PATH_ROOT = "/";
    private static final String COLLECTION_PATH_LEAF = "/_system/colBranch1/colBranch2/";
    private RelationAdminServiceClient relationAdminServiceClient;
    private PropertiesAdminServiceClient propertiesAdminServiceClient;
    private InfoServiceAdminClient infoServiceAdminClient;
    private LifeCycleManagementClient lifeCycleManagementClient;
    private LifeCycleAdminServiceClient lifeCycleAdminServiceClient;
    private WSRegistryServiceClient wsRegistryServiceClient;
    private ResourceAdminServiceClient resourceAdminClient;
    private ResourceAdminServiceClient resourceAdminClientAdmin;
    private org.wso2.carbon.governance.custom.lifecycles.checklist.stub.beans.xsd.LifecycleBean lifeCycle;
    private static UserManagementClient adminUserManagementClient;
    private static SearchAdminServiceClient searchAdminServiceClient;
    private String ROLE_1;

    int userId = 2;
    UserInfo userInfo;
    UserInfo userInfoAdmin;

    private ManageEnvironment environment;
    private static String LC_NAME = "MultiplePromoteDemoteLC";
    public static String[] COLLECTION_USERS;
    public static final String[] COL_USER1_PERMISSION = {"/permission/admin/login",
                                                         "/permission/admin/manage/resources",
                                                         "/permission/admin/manage/resources/associations",
                                                         "/permission/admin/manage/resources/browse",
                                                         "/permission/admin/manage/resources/community-features"};

    @BeforeClass(alwaysRun = true)
    public void initializeTests() throws LoginAuthenticationExceptionException, RemoteException,
                                         ResourceAdminServiceExceptionException, RegistryException {
        int userId2 = ProductConstant.ADMIN_USER_ID;
        userInfo = UserListCsvReader.getUserInfo(userId);
        COLLECTION_USERS = new String[]{userInfo.getUserNameWithoutDomain()};
        userInfoAdmin = UserListCsvReader.getUserInfo(userId2);

        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        environment = builder.build();

        EnvironmentBuilder builderAdmin = new EnvironmentBuilder().greg(userId2);
        ManageEnvironment adminEnvironment = builderAdmin.build();

        resourceAdminClient =
                new ResourceAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                               environment.getGreg().getSessionCookie());
        resourceAdminClientAdmin =
                new ResourceAdminServiceClient(adminEnvironment.getGreg().getBackEndUrl(),
                                               adminEnvironment.getGreg().getSessionCookie());
        relationAdminServiceClient =
                new RelationAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                               environment.getGreg().getSessionCookie());
        propertiesAdminServiceClient =
                new PropertiesAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                                 environment.getGreg().getSessionCookie());
        infoServiceAdminClient =
                new InfoServiceAdminClient(environment.getGreg().getBackEndUrl(),
                                           environment.getGreg().getSessionCookie());
        lifeCycleManagementClient =
                new LifeCycleManagementClient(environment.getGreg().getBackEndUrl(),
                                              environment.getGreg().getSessionCookie());
        lifeCycleAdminServiceClient =
                new LifeCycleAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                                environment.getGreg().getSessionCookie());
        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        wsRegistryServiceClient =
                registryProviderUtil.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        relationAdminServiceClient =
                new RelationAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                               environment.getGreg().getSessionCookie());
        propertiesAdminServiceClient =
                new PropertiesAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                                 environment.getGreg().getSessionCookie());
        infoServiceAdminClient =
                new InfoServiceAdminClient(environment.getGreg().getBackEndUrl(),
                                           environment.getGreg().getSessionCookie());
        lifeCycleManagementClient =
                new LifeCycleManagementClient(environment.getGreg().getBackEndUrl(),
                                              environment.getGreg().getSessionCookie());
        lifeCycleAdminServiceClient =
                new LifeCycleAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                                environment.getGreg().getSessionCookie());
        searchAdminServiceClient =
                new SearchAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                                environment.getGreg().getSessionCookie());
        registryProviderUtil = new RegistryProviderUtil();
        wsRegistryServiceClient =
                registryProviderUtil.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);

        adminUserManagementClient = new UserManagementClient(adminEnvironment.getGreg().getBackEndUrl(),
                                                             adminEnvironment.getGreg().getSessionCookie());


        resourceAdminClient.addCollection(COLLECTION_PATH_ROOT, "dir1", "text/plain", "Description 1 for collection");
        resourceAdminClient.addCollection(COLLECTION_PATH_ROOT, "dir2", "text/plain", "Description 2 for collection");
        resourceAdminClient.addCollection(COLLECTION_PATH_LEAF, "dir1", "text/plain", "Description 3 for collection");

        VersionUtils.deleteAllVersions(resourceAdminClient, COLLECTION_PATH_ROOT + "dir1");
        VersionUtils.deleteAllVersions(resourceAdminClient, COLLECTION_PATH_ROOT + "dir2");
        VersionUtils.deleteAllVersions(resourceAdminClient, COLLECTION_PATH_LEAF + "dir1");
    }

    @Test(groups = {"wso2.greg"}, description = "create a checkpoint for a collection at root level")
    public void testAddColRoot()
            throws MalformedURLException, ResourceAdminServiceExceptionException, RemoteException {

        resourceAdminClient.createVersion(COLLECTION_PATH_ROOT + "dir1");
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(COLLECTION_PATH_ROOT + "dir1");
        assertEquals(1, vp1.length);
        assertNull(deleteVersion(COLLECTION_PATH_ROOT + "dir1"));

    }

    @Test(groups = {"wso2.greg"}, description = "create a checkpoint for a collection at leaf level")
    public void testAddColLeaf()
            throws MalformedURLException, ResourceAdminServiceExceptionException, RemoteException {

        assertNull(deleteVersion(COLLECTION_PATH_LEAF + "dir1"));
        resourceAdminClient.createVersion(COLLECTION_PATH_LEAF + "dir1");
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(COLLECTION_PATH_LEAF + "dir1");
        assertEquals(1, vp1.length);
        assertNull(deleteVersion(COLLECTION_PATH_LEAF + "dir1"));
    }


    @Test(groups = {"wso2.greg"}, description = "create a checkpoint for a collection. Edit it and " +
                                                "create another checkpoint")
    public void testEditCollectionVer()
            throws ResourceAdminServiceExceptionException, RemoteException, MalformedURLException {

        resourceAdminClient.addCollection(COLLECTION_PATH_ROOT, "dir2", "text/plain", "Desc1");
        assertNull(deleteVersion(COLLECTION_PATH_ROOT + "dir2"));
        resourceAdminClient.createVersion(COLLECTION_PATH_ROOT + "dir2");
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(COLLECTION_PATH_ROOT + "dir2");
        assertEquals(1, vp1.length);

        String path = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator
                      + "GREG" + File.separator + "testresource.txt";
        DataHandler dataHandler = new DataHandler(new URL("file:///" + path));
        resourceAdminClient.addResource(COLLECTION_PATH_ROOT + "dir2/testDir", "text/plain", "desc", dataHandler);

        resourceAdminClient.createVersion(COLLECTION_PATH_ROOT + "dir2");
        VersionPath[] vp2 = resourceAdminClient.getVersionPaths(COLLECTION_PATH_ROOT + "dir2");
        assertEquals(2, vp2.length);
        assertNull(deleteVersion(COLLECTION_PATH_ROOT + "dir2"));
    }


    @Test(groups = {"wso2.greg"}, description = "create a collection at root level and check association with versioning ")
    public void testAssociations() throws ResourceAdminServiceExceptionException, RemoteException,
                                          AddAssociationRegistryExceptionException {

        String PATH1 = COLLECTION_PATH_ROOT + "dir1";
        String PATH2 = COLLECTION_PATH_LEAF + "dir1";
        String PATH3 = COLLECTION_PATH_ROOT + "dir2";
        relationAdminServiceClient.addAssociation(PATH1, "usedBy", PATH2, "add");
        AssociationTreeBean associationTreeBean = relationAdminServiceClient.getAssociationTree(PATH1, "usedBy");

        String resourcePath = associationTreeBean.getAssociationTree();
        assertTrue(resourcePath.contains(PATH2));
        resourceAdminClient.createVersion(PATH1);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATH1);
        String verPath = vp1[0].getCompleteVersionPath();
        assertTrue(relationAdminServiceClient.getAssociationTree(verPath, "usedBy").getAssociationTree().contains(PATH2));
        relationAdminServiceClient.addAssociation(PATH1, "usedBy", PATH3, "add");
        AssociationTreeBean associationTreeBean2 = relationAdminServiceClient.getAssociationTree(PATH1, "usedBy");

        String resourcePath2 = associationTreeBean2.getAssociationTree();
        assertTrue(resourcePath2.contains(PATH3));

        resourceAdminClient.createVersion(PATH1);
        VersionPath[] vp2 = resourceAdminClient.getVersionPaths(PATH1);
        String verPath2 = vp2[0].getCompleteVersionPath();
        assertTrue(relationAdminServiceClient.getAssociationTree(verPath2, "usedBy").getAssociationTree().contains(PATH2));
        assertTrue(relationAdminServiceClient.getAssociationTree(verPath2, "usedBy").getAssociationTree().contains(PATH3));

        resourceAdminClient.restoreVersion(verPath);
        boolean contains;
        contains = !(relationAdminServiceClient.getAssociationTree(PATH1, "usedBy").getAssociationTree().contains(PATH3));
        assertTrue(contains);

        assertNull(deleteVersion(PATH1));


    }

    @Test(groups = {"wso2.greg"}, description = "Create a collection with a dependency, version restore to the previous version ")
    public void testDependencyRestore()
            throws ResourceAdminServiceExceptionException, RemoteException,
                   AddAssociationRegistryExceptionException {
        String PATH1 = COLLECTION_PATH_ROOT + "dir1";
        String PATH2 = COLLECTION_PATH_LEAF + "dir1";
        relationAdminServiceClient.addAssociation(PATH1, "depends", PATH2, "add");
        AssociationTreeBean associationTreeBean = relationAdminServiceClient.getAssociationTree(PATH1, "depends");

        String resourcePath = associationTreeBean.getAssociationTree();
        assertTrue(resourcePath.contains(PATH2));
        resourceAdminClient.createVersion(PATH1);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATH1);
        String verPath = vp1[0].getCompleteVersionPath();
        assertTrue(relationAdminServiceClient.getAssociationTree(verPath, "depends").getAssociationTree().contains(PATH2));
        resourceAdminClient.restoreVersion(verPath);
        assertTrue(relationAdminServiceClient.getAssociationTree(PATH1, "depends").getAssociationTree().contains(PATH2));

        assertNull(deleteVersion(PATH1));

    }


    @Test(groups = {"wso2.greg"}, description = "Create a collection with an association, version and restore to the previous version ")
    public void testAssociationRestore()
            throws ResourceAdminServiceExceptionException, RemoteException,
                   AddAssociationRegistryExceptionException {
        String PATH1 = COLLECTION_PATH_ROOT + "dir1";
        String PATH2 = COLLECTION_PATH_LEAF + "dir1";
        relationAdminServiceClient.addAssociation(PATH1, "usedBy", PATH2, "add");
        AssociationTreeBean associationTreeBean = relationAdminServiceClient.getAssociationTree(PATH1, "usedBy");

        String resourcePath = associationTreeBean.getAssociationTree();
        assertTrue(resourcePath.contains(PATH2));
        resourceAdminClient.createVersion(PATH1);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATH1);
        String verPath = vp1[0].getCompleteVersionPath();
        assertTrue(relationAdminServiceClient.getAssociationTree(verPath, "usedBy").getAssociationTree().contains(PATH2));
        resourceAdminClient.restoreVersion(verPath);
        assertTrue(relationAdminServiceClient.getAssociationTree(PATH1, "usedBy").getAssociationTree().contains(PATH2));

        assertNull(deleteVersion(PATH1));

    }

    @Test(groups = {"wso2.greg"}, description = "Create a collection with an property, version and restore to the previous version ")
    public void testPropertyRestore()
            throws ResourceAdminServiceExceptionException, RemoteException,
                   PropertiesAdminServiceRegistryExceptionException {
        String PATH1 = COLLECTION_PATH_ROOT + "dir1";
        boolean status = false;
        int count = 0;
        propertiesAdminServiceClient.setProperty(PATH1, "name1", "value1");
        Property[] properties1 = propertiesAdminServiceClient.getProperty(PATH1, "true").getProperties();

        for (Property aProperties1 : properties1) {

            if (aProperties1.getKey().equals("name1")) {
                count++;
            }
            if (aProperties1.getValue().equals("value1")) {
                count++;
            }
        }
        if (count == 2) {
            status = true;
        }
        assertTrue(status);
        status = false;
        count = 0;


        resourceAdminClient.createVersion(PATH1);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATH1);
        String verPath = vp1[0].getCompleteVersionPath();
        Property[] properties2 = propertiesAdminServiceClient.getProperty(verPath, "true").getProperties();

        for (Property aProperties2 : properties2) {

            if (aProperties2.getKey().equals("name1")) {
                count++;
            }
            if (aProperties2.getValue().equals("value1")) {
                count++;
            }
        }
        if (count == 2) {
            status = true;
        }
        assertTrue(status);
        status = false;
        count = 0;

        propertiesAdminServiceClient.removeProperty(PATH1, "name1");
        assertNull(resourceAdminClient.getProperty(PATH1, "name1"));
        resourceAdminClient.restoreVersion(verPath);
        Property[] properties3 = propertiesAdminServiceClient.getProperty(PATH1, "true").getProperties();

        for (int i = 0; i < properties3.length; i++) {

            if (properties1[i].getKey().equals("name1")) {
                count++;
            }
            if (properties1[i].getValue().equals("value1")) {
                count++;
            }
        }
        if (count == 2) {
            status = true;
        }
        assertTrue(status);
        assertNull(deleteVersion(PATH1));

    }

    @Test(groups = {"wso2.greg"}, description = "Create a collection with collections," +
                                                " version and restore to the previous version ")
    public void testCollecionTreeRestore()
            throws ResourceAdminServiceExceptionException, RemoteException, MalformedURLException {
        String PATH = COLLECTION_PATH_ROOT + "dir1";
        resourceAdminClient.addCollection(PATH, "testDir1", "text/plain", "Desc1");
        resourceAdminClient.addCollection(PATH, "testDir2", "text/plain", "Desc2");
        CollectionContentBean collectionContent1 = resourceAdminClient.getCollectionContent(PATH);
        assertEquals(2, collectionContent1.getChildCount());
        resourceAdminClient.createVersion(PATH);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATH);
        String verPath = vp1[0].getCompleteVersionPath();
        CollectionContentBean collectionContent2 = resourceAdminClient.getCollectionContent(verPath);
        assertEquals(2, collectionContent2.getChildCount());
        resourceAdminClient.deleteResource((PATH + "/testDir1"));
        CollectionContentBean collectionContent = resourceAdminClient.getCollectionContent(PATH);

        assertEquals(1, collectionContent.getChildCount());
        resourceAdminClient.restoreVersion(verPath);
        CollectionContentBean collectionContent3 = resourceAdminClient.getCollectionContent(PATH);
        assertEquals(2, collectionContent3.getChildCount());
        assertNull(deleteVersion(PATH));


    }


    @Test(groups = {"wso2.greg"}, description = "Create a collection with resource, " +
                                                "version and restore to the previous version ")
    public void testResourceRestore()
            throws ResourceAdminServiceExceptionException, RemoteException, MalformedURLException {
        String PATH = COLLECTION_PATH_ROOT + "dir5";


        String path2 = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator
                       + "GREG" + File.separator + "testresource.txt";
        DataHandler dataHandler2 = new DataHandler(new URL("file:///" + path2));
        resourceAdminClient.addResource(PATH + "/testresource", "text/plain", "desc", dataHandler2);
        UserInfo userInfo = UserListCsvReader.getUserInfo(userId);
        assertTrue(resourceAdminClient.getResource(PATH + "/testresource")[0].getAuthorUserName().contains(userInfo.getUserNameWithoutDomain()));


        resourceAdminClient.createVersion(PATH);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATH);
        String verPath = vp1[0].getCompleteVersionPath();
        assertTrue(resourceAdminClient.getResource(verPath)[0].getAuthorUserName().contains(userInfo.getUserNameWithoutDomain()));

        //delete the resource from the collection after versioning
        resourceAdminClient.deleteResource(PATH + "/testresource");
        ResourceData[] resourceData;
        resourceData = resourceAdminClient.getResource(PATH);
        boolean status;
        if (resourceData == null) {
            status = true;
        } else {
            status = true;
            for (ResourceData aResourceData : resourceData) {
                if (aResourceData.getName().contains("/testresource")) {
                    status = false;
                }
            }
        }
        assertTrue(status);
        resourceAdminClient.restoreVersion(verPath);
        assertTrue(resourceAdminClient.getResource(PATH)[0].getAuthorUserName().contains(userInfo.getUserNameWithoutDomain()));

        assertNull(deleteVersion(PATH));
    }


    @Test(groups = {"wso2.greg"}, description = "Create a collection with tags, version and restore to the previous version ")
    public void testTagRestore()
            throws ResourceAdminServiceExceptionException, RemoteException, MalformedURLException,
                   RegistryException, RegistryExceptionException {
        String PATH = COLLECTION_PATH_ROOT + "dir1";
        boolean status = false;

        infoServiceAdminClient.addTag("testTag", PATH, environment.getGreg().getSessionCookie());
        TagBean tagBean = infoServiceAdminClient.getTags(PATH, environment.getGreg().getSessionCookie());
        Tag[] tags1 = tagBean.getTags();
        for (Tag aTags1 : tags1) {
            if (aTags1.getTagName().equals("testTag")) {
                status = true;
                break;
            }
        }


        assertTrue(status);
        resourceAdminClient.createVersion(PATH);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATH);
        String verPath = vp1[0].getCompleteVersionPath();
        TagBean tagBean2 = infoServiceAdminClient.getTags(verPath, environment.getGreg().getSessionCookie());
        Tag[] tags2 = tagBean2.getTags();
        status = false;
        for (Tag aTags2 : tags2) {
            if (aTags2.getTagName().equals("testTag")) {
                status = true;
                break;
            }
        }
        assertTrue(status);
        //remove the tag from the resource after versioning
        infoServiceAdminClient.removeTag("testTag", PATH, environment.getGreg().getSessionCookie());
        TagBean tagBean3 = infoServiceAdminClient.getTags(PATH, environment.getGreg().getSessionCookie());
        Tag[] tags3;
        tags3 = tagBean3.getTags();

        if (tags3 == null) {
            status = true;
        } else {
            status = true;
            for (Tag aTags3 : tags3) {

                if (aTags3.getTagName().equals("testTag")) {
                    status = false;
                }
            }
        }
        assertTrue(status);
        resourceAdminClient.restoreVersion(verPath);
        TagBean tagBean4 = infoServiceAdminClient.getTags(PATH, environment.getGreg().getSessionCookie());
        Tag[] tags4 = tagBean4.getTags();
        status = false;
        if (tags4 != null) {
            for (Tag aTags4 : tags4) {
                if (aTags4.getTagName().equals("testTag")) {
                    status = true;
                }
            }
        }
        assertTrue(status);
        assertNull(deleteVersion(PATH));
    }


    @Test(groups = {"wso2.greg"}, description = "Create a collection with tags, version and restore to the previous version ")
    public void testColRatingRestore() throws RegistryException, RegistryExceptionException,
                                              ResourceAdminServiceExceptionException,
                                              RemoteException {
        String sessionId = environment.getGreg().getSessionCookie();
        String PATH = COLLECTION_PATH_LEAF + "dir1";
        infoServiceAdminClient.rateResource("2", PATH, sessionId);
        RatingBean ratingBean = infoServiceAdminClient.getRatings(PATH, sessionId);
        assertEquals(2, ratingBean.getUserRating());
        resourceAdminClient.createVersion(PATH);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATH);
        String verPath = vp1[0].getCompleteVersionPath();
        assertEquals(2, infoServiceAdminClient.getRatings(verPath, sessionId).getUserRating());
        infoServiceAdminClient.rateResource("3", PATH, sessionId);
        assertEquals(3, infoServiceAdminClient.getRatings(PATH, sessionId).getUserRating());
        resourceAdminClient.restoreVersion(verPath);

        assertEquals(2, infoServiceAdminClient.getRatings(PATH, sessionId).getUserRating());
        assertNull(deleteVersion(PATH));

    }


    @Test(groups = {"wso2.greg"}, description = "Create a collection with comments, version and restore to the previous version ")
    public void testColCommentRestore()
            throws RegistryException, RemoteException, RegistryExceptionException,
                   ResourceAdminServiceExceptionException {
        String PATH1 = COLLECTION_PATH_LEAF + "dir1";

        infoServiceAdminClient.addComment("This is a comment", PATH1, environment.getGreg().getSessionCookie());
        CommentBean commentBean = infoServiceAdminClient.getComments(PATH1, environment.getGreg().getSessionCookie());
        assertEquals("This is a comment", commentBean.getComments()[0].getContent());
        resourceAdminClient.createVersion(PATH1);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATH1);
        String verPath = vp1[0].getCompleteVersionPath();
        assertEquals("This is a comment",
                     infoServiceAdminClient.getComments(verPath, environment.getGreg().getSessionCookie()).
                             getComments()[0].getContent());

        infoServiceAdminClient.removeComment(infoServiceAdminClient.getComments(PATH1, environment.getGreg().getSessionCookie()).
                getComments()[0].getCommentPath(), environment.getGreg().getSessionCookie());

        assertNull(infoServiceAdminClient.getComments(PATH1, environment.getGreg().getSessionCookie()).getComments());
        resourceAdminClient.restoreVersion(verPath);
        assertEquals("This is a comment", infoServiceAdminClient.getComments(PATH1, environment.getGreg().getSessionCookie()).getComments()[0].getContent());
        assertNull(deleteVersion(PATH1));
    }


    public void createNewLifeCycle()
            throws LifeCycleManagementServiceExceptionException, IOException,
                   SearchAdminServiceRegistryExceptionException, RegistryException {

        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator + "GREG" + File.separator + "lifecycle" + File.separator + "MultiplePromoteDemoteLC.xml";
        String lifeCycleContent = FileManager.readFile(resourcePath);
        LifeCycleUtils.deleteLcUsageResources(searchAdminServiceClient, wsRegistryServiceClient, LC_NAME);
        LifeCycleUtils.deleteLifeCycleIfExist(LC_NAME, lifeCycleManagementClient);
        lifeCycleManagementClient.addLifeCycle(lifeCycleContent);

        String[] lifeCycles = lifeCycleManagementClient.getLifecycleList();
        boolean lcStatus = false;
        for (String lc : lifeCycles) {
            if (lc.equalsIgnoreCase(LC_NAME)) {
                lcStatus = true;
            }
        }
        assertTrue(lcStatus);
    }

    @Test(groups = {"wso2.greg"}, description = "Create a collection with a life cycle, version and restore to the previous version ")
    public void testColLCRestore() throws LifeCycleManagementServiceExceptionException, IOException,
                                          CustomLifecyclesChecklistAdminServiceExceptionException,
                                          RegistryException,
                                          ResourceAdminServiceExceptionException,
                                          SearchAdminServiceRegistryExceptionException {

        String PATH1 = COLLECTION_PATH_LEAF + "dir1";
        createNewLifeCycle();
        wsRegistryServiceClient.associateAspect(PATH1, LC_NAME);
        lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(PATH1);

        org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property[] properties = lifeCycle.getLifecycleProperties();
        boolean lcStatus = false;
        for (org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property prop : properties) {
            prop.getKey();
            if (prop.getKey().contains(LC_NAME)) {
                lcStatus = true;
            }
        }
        assertTrue(lcStatus);
        lcStatus = false;
        resourceAdminClient.createVersion(PATH1);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATH1);
        String verPath = vp1[0].getCompleteVersionPath();
        lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(verPath);

        org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property[] properties2 = lifeCycle.getLifecycleProperties();

        for (org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property prop : properties2) {
            prop.getKey();
            if (prop.getKey().contains(LC_NAME)) {
                lcStatus = true;
            }
        }
        assertTrue(lcStatus);
        resourceAdminClient.restoreVersion(verPath);

        lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(PATH1);
        org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property[] properties4 = lifeCycle.getLifecycleProperties();
        lcStatus = false;

        for (org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property prop : properties4) {
            prop.getKey();
            if (prop.getKey().contains(LC_NAME)) {
                lcStatus = true;
            }
        }


        assertTrue(lcStatus);

        assertNull(deleteVersion(PATH1));
    }


    /*   @Test(groups = {"wso2.greg"}, description = "Create a collection with different roles, version and restore to the previous version ")
public void testColRolesRestore() throws Exception {
   String PATH = COLLECTION_PATH_ROOT + "dir1";
   //create a role with limited permission and assign testuser1 to that role and associate it with a collection
   String ROLE_1 = "collectionRole1";
   adminUserManagementClient.addRole(ROLE_1, COLLECTION_USERS, COL_USER1_PERMISSION);

   //Remove testRole from testuser1
   adminUserManagementClient.updateUserListOfRole("testRole", new String[]{}, new String[]{"testuser1"});
   //allow authorize action to the role
   resourceAdminClient.addResourcePermission(PATH, ROLE_1, PermissionTestConstants.AUTHORIZE_ACTION, PermissionTestConstants.PERMISSION_ENABLED);
   PermissionBean permissionBean = resourceAdminClient.getPermission(PATH);
   assertTrue(permissionBean.getAuthorizeAllowed());
   resourceAdminClient.createVersion(PATH);
   VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATH);
   String verPath = vp1[0].getCompleteVersionPath();
   //deny authorize action from the role and allow read and write access
   resourceAdminClient.addResourcePermission(PATH, ROLE_1, PermissionTestConstants.AUTHORIZE_ACTION, PermissionTestConstants.PERMISSION_DISABLED);
   resourceAdminClient.addResourcePermission(PATH, ROLE_1, PermissionTestConstants.READ_ACTION, PermissionTestConstants.PERMISSION_ENABLED);
   resourceAdminClient.addResourcePermission(PATH, ROLE_1, PermissionTestConstants.WRITE_ACTION, PermissionTestConstants.PERMISSION_ENABLED);

   assertTrue(!(resourceAdminClient.getPermission(PATH).getAuthorizeAllowed()));
   //restore to previous version and see whether the permissions are restored
   resourceAdminClient.restoreVersion(verPath);
   assertTrue(!(resourceAdminClient.getPermission(PATH).getAuthorizeAllowed()));
   //Add testRole to testuser1
   adminUserManagementClient.updateUserListOfRole("testRole", new String[]{"testuser1"}, new String[]{});
   assertNull(deleteVersion(PATH));


}
    */


    @Test(groups = {"wso2.greg"}, description = "Create a collection with different roles, version and restore to the previous version ")
    public void testColRolesRestore() throws Exception {
        String PATH = COLLECTION_PATH_ROOT + "dir1";
        //create a role with limited permission and assign testuser1 to that role and associate it with a collection
        ROLE_1 = "collectionRole1";

        if (adminUserManagementClient.roleNameExists(ROLE_1)) {
            adminUserManagementClient.deleteRole(ROLE_1);
        }
        adminUserManagementClient.addRole(ROLE_1, COLLECTION_USERS, COL_USER1_PERMISSION);

        //Remove testRole from testuser1
        adminUserManagementClient.updateUserListOfRole(ProductConstant.DEFAULT_PRODUCT_ROLE, new String[]{}, new String[]{userInfo.getUserNameWithoutDomain()});
        //allow authorize action to the role
        resourceAdminClientAdmin.addResourcePermission(PATH, ROLE_1, PermissionTestConstants.AUTHORIZE_ACTION, PermissionTestConstants.PERMISSION_ENABLED);
        PermissionBean permissionBean = resourceAdminClient.getPermission(PATH);
        assertTrue(permissionBean.getAuthorizeAllowed());
        resourceAdminClient.createVersion(PATH);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATH);
        String verPath = vp1[0].getCompleteVersionPath();
        //deny authorize action from the role and allow read and write access
        resourceAdminClient.addResourcePermission(PATH, ROLE_1, PermissionTestConstants.AUTHORIZE_ACTION, PermissionTestConstants.PERMISSION_DISABLED);

        assertFalse(resourceAdminClient.getPermission(PATH).getAuthorizeAllowed());
        //restore to previous version and see whether the permissions are restored
        resourceAdminClientAdmin.restoreVersion(verPath);
        assertFalse(resourceAdminClient.getPermission(PATH).getAuthorizeAllowed());
        //Add testRole to testuser1
        adminUserManagementClient.updateUserListOfRole(ProductConstant.DEFAULT_PRODUCT_ROLE, new String[]{userInfo.getUserNameWithoutDomain()}, new String[]{});
        adminUserManagementClient.updateUserListOfRole(ROLE_1, new String[]{}, new String[]{userInfo.getUserNameWithoutDomain()});
        assertNull(deleteVersion(PATH));
    }


    @Test(groups = {"wso2.greg"}, description = "Create a collection with a description, version and restore to the previous version ")
    public void testColDescRestore() throws ResourceAdminServiceExceptionException, RemoteException,
                                            RegistryExceptionException {
        String PATH = COLLECTION_PATH_ROOT + "dir1";

        boolean status = false;
        ResourceData[] resource1 = resourceAdminClient.getResource(PATH);
        for (ResourceData aResource1 : resource1) {
            if (aResource1.getDescription().equals("Description 1 for collection")) {
                status = true;
            }
        }
        assertTrue(status);
        resourceAdminClient.createVersion(PATH);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATH);
        String verPath = vp1[0].getCompleteVersionPath();

        resourceAdminClient.setDescription(PATH, "Edited description");
        status = false;
        ResourceData[] resource2 = resourceAdminClient.getResource(PATH);
        for (ResourceData aResource2 : resource2) {
            if (aResource2.getDescription().equals("Edited description")) {
                status = true;
            }
        }
        assertTrue(status);
        resourceAdminClient.restoreVersion(verPath);
        status = false;
        ResourceData[] resource3 = resourceAdminClient.getResource(PATH);
        for (ResourceData aResource3 : resource3) {
            if (aResource3.getDescription().equals("Description 1 for collection")) {
                status = true;
            }
        }
        assertTrue(status);


    }

    public VersionPath[] deleteVersion(String path)
            throws ResourceAdminServiceExceptionException, RemoteException {
        VersionPath[] versionPaths = resourceAdminClient.getVersionPaths(path);
        if (versionPaths != null) {
            for (VersionPath versionPath : versionPaths) {
                long versionNo = versionPath.getVersionNumber();
                String snapshotId = String.valueOf(versionNo);
                resourceAdminClient.deleteVersionHistory(path, snapshotId);
            }
        }
        return resourceAdminClient.getVersionPaths(path);
    }

    @AfterClass
    public void clear() throws Exception {
        String PATH1 = COLLECTION_PATH_ROOT + "dir1";
        String PATH2 = COLLECTION_PATH_LEAF + "dir1";
        String PATH3 = COLLECTION_PATH_ROOT + "dir2";

        deleteResource(PATH1);
        deleteResource(PATH2);
        deleteResource("/_system/colBranch1");
        deleteResource(PATH3);
        deleteResource(COLLECTION_PATH_ROOT + "dir5");
        if (adminUserManagementClient.roleNameExists(ROLE_1)) {
            adminUserManagementClient.deleteRole(ROLE_1);
        }
        lifeCycleManagementClient.deleteLifeCycle(LC_NAME);
        resourceAdminClient = null;
        lifeCycleManagementClient = null;
        lifeCycleAdminServiceClient = null;
        relationAdminServiceClient = null;
        propertiesAdminServiceClient = null;
        infoServiceAdminClient = null;
        wsRegistryServiceClient = null;
        resourceAdminClientAdmin = null;
    }

    public void deleteResource(String path) throws RegistryException {
        if (wsRegistryServiceClient.resourceExists(path)) {
            wsRegistryServiceClient.delete(path);
        }
    }

}
