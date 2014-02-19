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

package org.wso2.carbon.registry.ws.client.test.general.old;

import org.apache.axis2.AxisFault;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Comment;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.Tag;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * A test case which tests registry resource handling operation
 */
public class ResourceHandlingWSTestCase {

    private WSRegistryServiceClient registry;

    @BeforeClass(groups = {"wso2.greg"})
    public void initTest() throws RegistryException, AxisFault {
        int userId = 0;
        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        registry = registryProviderUtil.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);

    }

    @Test(groups = {"wso2.greg"})
    public void newCollection() throws Exception {
        Collection collection = registry.newCollection();
        assertEquals(collection.getChildCount(), 0, "Invalid Child Count for new collection");
        assertNotNull(collection.getChildren(), "The children for a new collection cannot be null");
        assertEquals(collection.getChildren().length, 0, "Invalid Child Count for new collection");
        registry.put("/f1012/col", collection);
        Collection collection_new = (Collection) registry.get("/f1012/col");
//        TODO:ws registry client seems to be broken
        assertEquals(collection_new.getChildCount(), 0, "Invalid Child Count for new collection");
        assertNotNull(collection_new.getChildren(), "The children for a new collection cannot be null");
        assertEquals(collection_new.getChildren().length, 0, "Invalid Child Count for new collection");
    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"newCollection"})
    public void resourceCopy() throws Exception {

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
            assertEquals(new String((byte[]) r1.getContent()),
                         new String((byte[]) r2.getContent()), "File content is not matching");
            assertTrue(containsComment(commentPath, c1.getText()), c1.getText() + " is not associated for resource" + path);
            assertTrue(containsComment(commentPath, c2.getText()), c2.getText() + " is not associated for resource" + path);
            assertTrue(containsTag(path, "tag1"), "Tag1 is not exist");
            assertTrue(containsTag(path, "tag2"), "Tag2 is not exist");
            assertTrue(containsTag(path, "tag3"), "Tag3 is not exist");

            float rating = registry.getAverageRating(path);
            assertEquals(rating, (float) 4.0, (float) 0.01, "Rating is not mathching");
            assertEquals(r2.getMediaType(), r1.getMediaType(), "Media type not exist");
            assertEquals(r2.getDescription(), r1.getDescription(), "Description is not exist");

            String new_path_returned;
            new_path_returned = registry.rename(path, new_path);

            assertEquals(new_path_returned, new_path, "New resource path is not equal");

            /*get renamed resource details*/

            Resource r1Renamed = registry.get(new_path);

            assertEquals(new String((byte[]) r1Renamed.getContent()),
                         new String((byte[]) r2.getContent()), "File content is not matching");
            assertEquals(r1Renamed.getProperty("key1"),
                         r2.getProperty("key1"), "Properties are not equal");
            assertEquals(r1Renamed.getProperty("key2"),
                         r2.getProperty("key2"), "Properties are not equal");
            assertTrue(containsComment(commentPathNew, c1.getText()),
                       c1.getText() + " is not associated for resource" + new_path);
            assertTrue(containsComment(commentPathNew, c2.getText()),
                       c2.getText() + " is not associated for resource" + new_path);
            assertTrue(containsTag(new_path, "tag1"), "Tag1 is not copied");
            assertTrue(containsTag(new_path, "tag2"), "Tag2 is not copied");
            assertTrue(containsTag(new_path, "tag3"), "Tag3 is not copied");

            float rating1 = registry.getAverageRating(new_path);
            assertEquals(rating1, (float) 4.0, (float) 0.01, "Rating is not copied");
            assertEquals(r1Renamed.getMediaType(), r2.getMediaType(), "Media type not copied");
            assertEquals(r1Renamed.getAuthorUserName(), r2.getAuthorUserName(),
                         "Authour Name is not copied");
            assertEquals(r1Renamed.getDescription(), r2.getDescription(), "Description is not exist");

        } catch (RegistryException e) {
            e.printStackTrace();
        }
    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"resourceCopy"})
    public void collectionCopy() throws Exception {

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
            assertEquals(r2.getProperty("key1"), r1.getProperty("key1"), "Properties are not equal");
            assertEquals(r2.getProperty("key2"),
                         r1.getProperty("key2"), "Properties are not equal");
            assertTrue(containsComment(commentPath, c1.getText()),
                       c1.getText() + " is not associated for resource" + path);
            assertTrue(containsComment(commentPath, c2.getText()),
                       c2.getText() + " is not associated for resource" + path);
            assertTrue(containsTag(path, "tag1"), "Tag1 is not copied");
            assertTrue(containsTag(path, "tag2"), "Tag2 is not copied");
            assertTrue(containsTag(path, "tag3"), "Tag3 is not copied");
            float rating = registry.getAverageRating(path);
            assertEquals(rating, (float) 4.0, (float) 0.01, "Rating is not mathching");
            String new_path_returned;
            new_path_returned = registry.rename(path, new_path);

            assertEquals(new_path_returned, new_path, "New resource path is not equal");

            /*get renamed resource details*/

            Resource r1Renamed = registry.get(new_path);

            assertEquals(r1Renamed.getProperty("key1"),
                         r2.getProperty("key1"), "Properties are not equal");
            assertEquals(r1Renamed.getProperty("key2"),
                         r2.getProperty("key2"), "Properties are not equal");
            assertTrue(containsComment(commentPathNew, c1.getText()),
                       c1.getText() + " is not associated for resource" + new_path);
            assertTrue(containsComment(commentPathNew, c2.getText()),
                       c2.getText() + " is not associated for resource" + new_path);
            assertTrue(containsTag(new_path, "tag1"), "Tag1 is not copied");
            assertTrue(containsTag(new_path, "tag2"), "Tag2 is not copied");
            assertTrue(containsTag(new_path, "tag3"), "Tag3 is not copied");
            float rating1 = registry.getAverageRating(new_path);
            assertEquals(rating1, (float) 4.0, (float) 0.01, "Rating is not copied");
        } catch (RegistryException e) {
            e.printStackTrace();
        }
    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"collectionCopy"})
    public void getResourceoperation() throws Exception {

        Resource r2 = registry.newResource();
        String path = "/testk/testa/derby.log";
        r2.setContent(new String("this is the content").getBytes());
        r2.setDescription("this is test desc this is test desc this is test desc this is test desc this is test desc " +
                          "this is test desc this is test desc this is test desc this is test descthis is test desc ");
        r2.setMediaType("plain/text");
        registry.put(path, r2);
        r2.discard();
        Resource r3;
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
        assertEquals(r3.getMediaType(), "plain/text", "Media Type is not equal");
        assertEquals(r3.getParentPath(), "/testk/testa", "parent Path is not equal");
        assertEquals(r3.getPath(), path, "parent Path is not equal");
        assertEquals(r3.getState(), 0, "Get stated wrong");
    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"getResourceoperation"})
    public void getCollectionoperation() throws Exception {

        Resource r2 = registry.newCollection();
        String path = "/testk2/testa/testc";
        r2.setDescription("this is test desc");
        r2.setProperty("test2", "value2");
        registry.put(path, r2);
        r2.discard();
        Resource r3 = registry.get(path);
        assertNotNull(r3.getId(), "Get Id is null");
        assertNotNull(r3.getLastModified(), "LastModifiedDate is null");
        assertEquals(r3.getLastUpdaterUserName(), "admin", "Last Updated names are not Equal");
        assertEquals(r3.getParentPath(), "/testk2/testa", "parent Path is not equal");
        assertEquals(r3.getState(), 0, "Get stated wrong");
        registry.createVersion(path);
        assertEquals(r3.getParentPath(), "/testk2/testa", "Permenent path doesn't contanin the string");
        assertEquals(r3.getPath(), path, "Path doesn't contanin the string");
    }


    private boolean containsComment(String pathValue, String commentText) throws Exception {

        Comment[] commentsArray;
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
        if (commentTexts.contains(commentText)) {
            found = true;
        }

        return found;
    }

    private boolean containsTag(String tagPath, String tagText) throws Exception {

        Tag[] tags = null;
        try {
            tags = registry.getTags(tagPath);
        } catch (RegistryException e) {
            e.printStackTrace();
        }

        boolean tagFound = false;
        for (Tag tag : tags) {
            if (tag.getTagName().equals(tagText)) {
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


    @AfterClass
    public void cleanup() throws RegistryException {
        registry.delete("/f95");
        registry.delete("/f96");
        registry.delete("/f1012");
        registry.delete("/c9011");
        registry.delete("/c9111");
        registry.delete("/testk");
        registry.delete("/testk2");


    }
}
