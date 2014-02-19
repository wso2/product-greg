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

package org.wso2.carbon.registry.app.test;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.registry.app.RemoteRegistry;
import org.wso2.carbon.registry.core.Comment;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.Tag;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import static org.testng.Assert.*;

/**
 * A test case which tests registry resource handling
 */
public class ResourceHandlingTestCase {
    public RemoteRegistry registry;

    @BeforeClass(groups = {"wso2.greg"})
    public void init() {
        InitializeAPI initializeAPI = new InitializeAPI();
        registry = initializeAPI.getRegistry(FrameworkSettings.CARBON_HOME, FrameworkSettings.HTTPS_PORT, FrameworkSettings.HTTP_PORT);
    }

    @Test(groups = {"wso2.greg"})
    public void ResourceCopyTest() {

        try {
            String path = "/f95/f2/r1";
            String commentPath = path + RegistryConstants.URL_SEPARATOR + "comments";
            String new_path = "/f96/f2/r1";
            String commentPathNew = new_path + RegistryConstants.URL_SEPARATOR + "comments";
            Resource r1 = registry.newResource();
            r1.setDescription("This is a file to be renamed");
            byte[] r1content = "R2 content".getBytes();
            r1.setContent(r1content);
            r1.setMediaType("txt");

            Comment c1 = new Comment();
            c1.setResourcePath(path);
            c1.setText("This is a test comment1");

            Comment c2 = new Comment();
            c2.setResourcePath(path);
            c2.setText("This is a test comment2");

            r1.setProperty("key1", "value1");
            r1.setProperty("key2", "value2");

            registry.put(path, r1);
            registry.addComment(path, c1);
            registry.addComment(path, c2);
            registry.applyTag(path, "tag1");
            registry.applyTag(path, "tag2");
            registry.applyTag(path, "tag3");
            registry.rateResource(path, 4);

            Resource r2 = registry.get(path);

            assertEquals(r2.getProperty("key1"), r1.getProperty("key1"), "Properties are not equal");
            assertEquals(r2.getProperty("key2"), r1.getProperty("key2"), "Properties are not equal");
            assertEquals(new String((byte[]) r2.getContent()),
                    new String((byte[]) r1.getContent()), "File content is not matching");
            System.out.println(commentPath + " " + c1.getText());
            assertTrue(containsComment(path, c1.getText()), c1.getText() + " is not associated for resource" + path);
            assertTrue(containsComment(path, c2.getText()), c2.getText() + " is not associated for resource" + path);
            assertTrue(containsTag(path, "tag1"), "Tag1 is not exist");
            assertTrue(containsTag(path, "tag2"), "Tag2 is not exist");
            assertTrue(containsTag(path, "tag3"), "Tag3 is not exist");
            float rating = registry.getAverageRating(path);
            assertEquals(rating, (float) 4.0, (float) 0.01, "Rating is not mathching");
            assertEquals(r2.getMediaType(), r1.getMediaType(), "Media type not exist");
            assertEquals(r2.getAuthorUserName(), r1.getAuthorUserName(), "Authour name is not exist");
            assertEquals(r2.getDescription(), r1.getDescription(), "Description is not exist");

            String new_path_returned;
            new_path_returned = registry.rename(path, new_path);

            assertEquals(new_path_returned, new_path, "New resource path is not equal");

            /*get renamed resource details*/

            Resource r1Renamed = registry.get(new_path);

            assertEquals(new String((byte[]) r1Renamed.getContent()), new String((byte[]) r2.getContent()), "File content is not matching");
            assertEquals(r1Renamed.getProperty("key1"),
                    r2.getProperty("key1"), "Properties are not equal");
            assertEquals(r1Renamed.getProperty("key2"),
                    r2.getProperty("key2"), "Properties are not equal");
            assertTrue(containsComment(new_path, c1.getText()),
                    c1.getText() + " is not associated for resource" + new_path);
            assertTrue(containsComment(new_path, c2.getText()),
                    c2.getText() + " is not associated for resource" + new_path);
            assertTrue(containsTag(new_path, "tag1"), "Tag1 is not copied");
            assertTrue(containsTag(new_path, "tag2"), "Tag2 is not copied");
            assertTrue(containsTag(new_path, "tag3"), "Tag3 is not copied");

            float rating1 = registry.getAverageRating(new_path);
            assertEquals(rating1, (float) 4.0, (float) 0.01, "Rating is not copied");
            assertEquals(r1Renamed.getMediaType(), r2.getMediaType(), "Media type not copied");
            assertEquals(r1Renamed.getAuthorUserName(), r2.getAuthorUserName(), "Authour Name is not copied");
            assertEquals(r1Renamed.getDescription(), r2.getDescription(), "Description is not exist");

        } catch (RegistryException e) {
            e.printStackTrace();
        }
    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"ResourceCopyTest"})
    public void CollectionCopyTest() {

        try {

            String path = "/c9011/c1/c2";
            String commentPath = path + RegistryConstants.URL_SEPARATOR + "comments";
            String new_path = "/c9111/c1/c3";
            String commentPathNew = new_path + RegistryConstants.URL_SEPARATOR + "comments";
            Resource r1 = registry.newCollection();
            r1.setDescription("This is a file to be renamed");

            Comment c1 = new Comment();
            c1.setResourcePath(path);
            c1.setText("This is first test comment");

            Comment c2 = new Comment();
            c2.setResourcePath(path);
            c2.setText("This is secound test comment");

            r1.setProperty("key1", "value1");
            r1.setProperty("key2", "value2");

            registry.put(path, r1);
            registry.addComment(path, c1);
            registry.addComment(path, c2);
            registry.applyTag(path, "tag1");
            registry.applyTag(path, "tag2");
            registry.applyTag(path, "tag3");
            registry.rateResource(path, 4);

            Resource r2 = registry.get(path);

            assertEquals(r2.getProperty("key1"),
                    r1.getProperty("key1"), "Properties are not equal");
            assertEquals(r2.getProperty("key2"),
                    r1.getProperty("key2"), "Properties are not equal");
            assertTrue(containsComment(path, c1.getText()),
                    c1.getText() + " is not associated for resource" + path);
            assertTrue(containsComment(path, c2.getText()),
                    c2.getText() + " is not associated for resource" + path);
            assertTrue(containsTag(path, "tag1"), "Tag1 is not copied");
            assertTrue(containsTag(path, "tag2"), "Tag2 is not copied");
            assertTrue(containsTag(path, "tag3"), "Tag3 is not copied");

            float rating = registry.getAverageRating(path);
            assertEquals(rating, (float) 4.0, (float) 0.01, "Rating is not mathching");
            assertEquals(r2.getAuthorUserName(), r1.getAuthorUserName(), "Authour name is not exist");

            String new_path_returned;
            new_path_returned = registry.rename(path, new_path);

            assertEquals(new_path_returned, new_path, "New resource path is not equal");

            /*get renamed resource details*/

            Resource r1Renamed = registry.get(new_path);

            assertEquals(r1Renamed.getProperty("key1"), r2.getProperty("key1"), "Properties are not equal");
            assertEquals(r1Renamed.getProperty("key2"),
                    r2.getProperty("key2"), "Properties are not equal");
            assertTrue(containsComment(new_path, c1.getText()),
                    c1.getText() + " is not associated for resource" + new_path);
            assertTrue(containsComment(new_path, c2.getText()),
                    c2.getText() + " is not associated for resource" + new_path);
            assertTrue(containsTag(new_path, "tag1"), "Tag1 is not copied");
            assertTrue(containsTag(new_path, "tag2"), "Tag2 is not copied");
            assertTrue(containsTag(new_path, "tag3"), "Tag3 is not copied");

            float rating1 = registry.getAverageRating(new_path);
            assertEquals(rating1, (float) 4.0, (float) 0.01, "Rating is not copied");

            assertEquals(r2.getAuthorUserName(), r1.getAuthorUserName(), "Author Name is not copied");

        } catch (RegistryException e) {
            e.printStackTrace();
        }
    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"CollectionCopyTest"})
    public void GetResourceoperationTest() throws RegistryException {

        Resource r2 = registry.newResource();
        String path = "/testk/testa/derby.log";
        r2.setContent(new String("this is the content").getBytes());
        r2.setDescription("this is test desc this is test desc this is test desc this is test desc this is test desc " +
                "this is test desc this is test desc this is test desc this is test descthis is test desc ");
        r2.setMediaType("plain/text");
        registry.put(path, r2);

        r2.discard();

        Resource r3 = registry.newResource();

        assertEquals(r3.getAuthorUserName(), "admin", "Author names are not Equal");

        r3 = registry.get(path);

        assertEquals(r3.getAuthorUserName(), "admin", "Author User names are not Equal");
        assertNotNull(r3.getCreatedTime(), "Created time is null");
        assertEquals(r3.getAuthorUserName(), "admin", "Author User names are not Equal");
        assertEquals(r3.getDescription(), "this is test desc this is test desc this is test desc this is test" +
                " desc this is test desc this is test desc this is test desc this is test desc this is test descthis is " +
                "test desc ", "Description is not Equal");
        assertNotNull(r3.getId(), "Get Id is null");
        assertNotNull(r3.getLastModified(), "LastModifiedDate is null");
        assertEquals(r3.getLastUpdaterUserName(), "admin", "Last Updated names are not Equal");
        //System.out.println(r3.getMediaType());
        assertEquals(r3.getMediaType(), "plain/text", "Media Type is not equal");
        assertEquals(r3.getParentPath(), "/testk/testa", "parent Path is not equal");
        assertEquals(r3.getPath(), path, "parent Path is not equal");
        assertEquals(r3.getState(), 0, "Get stated wrong");

        String st = r3.getPermanentPath();
        //  assertTrue("Permenent path contanin the string" + path + " verion", st.contains("/testk/testa/derby.log;version:"));
    }


    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"GetResourceoperationTest"})
    public void GetCollectionoperationTest() throws RegistryException {

        Resource r2 = registry.newCollection();
        String path = "/testk2/testa/testc";
        r2.setDescription("this is test desc");
        r2.setProperty("test2", "value2");
        registry.put(path, r2);

        r2.discard();

        Resource r3 = registry.get(path);

        assertEquals(r3.getAuthorUserName(), "admin", "Author names are not Equal");
        assertEquals(r3.getAuthorUserName(), "admin", "Author User names are not Equal");
        // System.out.println(r3.getCreatedTime());
        //assertNotNull("Created time is null", r3.getCreatedTime());
        assertEquals(r3.getAuthorUserName(), "admin", "Author User names are not Equal");
        //System.out.println("Desc" + r3.getDescription());
        //assertEquals("Description is not Equal", "this is test desc", r3.getDescription());
        assertNotNull(r3.getId(), "Get Id is null");
        assertNotNull(r3.getLastModified(), "LastModifiedDate is null");
        assertEquals(r3.getLastUpdaterUserName(), "admin", "Last Updated names are not Equal");
        //System.out.println("Media Type:" + r3.getMediaType());
        //assertEquals("Media Type is not equal","unknown",r3.getMediaType());
        assertEquals(r3.getParentPath(), "/testk2/testa", "parent Path is not equal");
        assertEquals(r3.getState(), 0, "Get stated wrong");

        registry.createVersion(path);

//         System.out.println(r3.getParentPath());
//      System.out.println(r3.getPath());

        assertEquals("/testk2/testa", r3.getParentPath(), "Permenent path doesn't contanin the string");
        assertEquals(path, r3.getPath(), "Path doesn't contanin the string");

//        String st = r3.getPermanentPath();
//        assertTrue("Permenent path contanin the string" + path +" verion", st.contains("/testk2/testa/testc;version:"));


    }


    private boolean containsComment(String pathValue, String commentText) {
        try {
            Comment[] comments = registry.getComments(pathValue);
            for (Comment comment : comments) {
                if (commentText.equals(comment.getText())) {
                    return true;
                }
            }
            return false;
        } catch (RegistryException e) {
            fail("Remote registry tests failed!" + e.getMessage());
            return false;
        }

        /*Comment[] commentsArray = null;
        List commentTexts = new ArrayList();

        try {
            Resource commentsResource = registry.get(pathValue);
            commentsArray = (Comment[]) commentsResource.getContent();
            for (Comment comment : commentsArray) {
                Resource commentResource = registry.get(comment.getPath());
                commentTexts.add(commentResource.getContent());
            }
        } catch (RegistryException e) {
            e.printStackTrace();
        }

        boolean found = false;
        System.out.println(commentTexts.toString() + " " + commentText);

        for (Object e : commentTexts)
      {
          System.out.println(e.toString());
      }

        if (commentTexts.contains(commentText)) {
            found = true;
        }
        return found;*/

    }

    private boolean containsTag(String tagPath, String tagText) {

        Tag[] tags = null;

        try {
            tags = registry.getTags(tagPath);
        } catch (RegistryException e) {
            e.printStackTrace();
        }

        boolean tagFound = false;
        for (int i = 0; i < tags.length; i++) {
            if (tags[i].getTagName().equals(tagText)) {
                tagFound = true;
                break;
            }
        }

        return tagFound;
    }

    private boolean containsString(String[] array, String value) {

        boolean found = false;
        for (String anArray : array) {
            if (anArray.startsWith(value)) {
                found = true;
                break;
            }
        }

        return found;
    }
}
