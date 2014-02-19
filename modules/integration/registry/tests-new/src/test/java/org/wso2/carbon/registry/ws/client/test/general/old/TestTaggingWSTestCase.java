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
import org.wso2.carbon.registry.core.CollectionImpl;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.Tag;
import org.wso2.carbon.registry.core.TaggedResourcePath;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * A test case which tests registry  tagging operation
 */
public class TestTaggingWSTestCase {

    private WSRegistryServiceClient registry;

    @BeforeClass(groups = {"wso2.greg"})
    public void initTest() throws RegistryException, AxisFault {
        int userId = 0;
        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        registry = registryProviderUtil.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);
    }


    @Test(groups = {"wso2.greg"})
    public void runSuccessCase() {

        try {
            addTagging();

        } catch (Exception e) {
            e.printStackTrace();
            fail("The Tagging Test for WS-API failed");
        }

        try {
            duplicateTagging();

        } catch (Exception e) {
            e.printStackTrace();
            fail("The Tagging Test for WS-API failed");
        }

        try {
            addTaggingCollection();

        } catch (Exception e) {
            e.printStackTrace();
            fail("The Tagging Test for WS-API failed");
        }

        try {
            editTagging();

        } catch (Exception e) {
            e.printStackTrace();
            fail("The Tagging Test for WS-API failed");
        }

        try {
            removeResourceTagging();

        } catch (Exception e) {
            e.printStackTrace();
            fail("The Tagging Test for WS-API failed");
        }

        try {
            removeCollectionTagging();

        } catch (Exception e) {
            e.printStackTrace();
            fail("The Tagging Test for WS-API failed");
        }

        try {
            tagging();

        } catch (Exception e) {
            e.printStackTrace();
            fail("The Tagging Test for WS-API failed");
        }
    }

    private void addTagging() throws Exception {
        // add a resources
        Resource r1 = registry.newResource();
        byte[] r1content = "q1 content".getBytes();
        r1.setContent(r1content);
        registry.put("/d11/r1", r1);

        Resource r2 = registry.newResource();
        byte[] r2content = "q2 content".getBytes();
        r2.setContent(r2content);
        registry.put("/d11/r2", r2);

        Resource r3 = registry.newResource();
        byte[] r3content = "q3 content".getBytes();
        r3.setContent(r3content);
        registry.put("/d11/r3", r3);
        registry.applyTag("/d11/r1", "jsp");
        registry.applyTag("/d11/r2", "jsp");
        registry.applyTag("/d11/r3", "java long tag");

        TaggedResourcePath[] paths = registry.getResourcePathsWithTag("jsp");
        boolean artifactFound1 = false;
        boolean artifactFound2 = false;

        for (TaggedResourcePath path : paths) {

            if (path.getResourcePath().equals("/d11/r1")) {
                artifactFound1 = true;
            }
            if (path.getResourcePath().equals("/d11/r2")) {
                artifactFound2 = true;
            }
        }
        assertTrue(artifactFound1, "/d11/r1 is not tagged with the tag \"jsp\"");
        assertTrue(artifactFound2, "/d11/r2 is not tagged with the tag \"jsp\"");

        Tag[] tags = null;
        try {
            tags = registry.getTags("/d11/r1");
        } catch (Exception e) {
            fail("Failed to get tags for the resource /d11/r1");
        }

        boolean tagFound = false;
        for (Tag tag : tags) {
            if (tag.getTagName().equals("jsp")) {
                tagFound = true;
                break;
            }
        }
        assertTrue(tagFound, "tag 'jsp' is not associated with the artifact /d11/r1");
    }

    private void duplicateTagging() throws Exception {
        Resource r1 = registry.newResource();
        byte[] r1content = "q1 content".getBytes();
        r1.setContent(r1content);
        registry.put("/d12/r1", r1);

        registry.applyTag("/d12/r1", "tag1");
        registry.applyTag("/d12/r1", "tag2");

        Tag[] tags = registry.getTags("/d12/r1");

        boolean tagFound = false;
        for (Tag tag : tags) {
            if (tag.getTagName().equals("tag1")) {
                tagFound = true;
                break;
            }
        }
        assertTrue(tagFound, "tag 'tag1' is not associated with the artifact /d12/r1");
    }

    private void addTaggingCollection() throws Exception {
        Collection r1 = registry.newCollection();
        registry.put("/d13/d14", r1);
        registry.applyTag("/d13/d14", "col_tag1");

        Tag[] tags = registry.getTags("/d13/d14");

        boolean tagFound = false;
        for (Tag tag : tags) {
            if (tag.getTagName().equals("col_tag1")) {
                tagFound = true;
                break;
            }
        }
        assertTrue(tagFound, "tag 'col_tag1' is not associated with the artifact /d13/d14");
    }

    private void editTagging() throws Exception {
        Resource r1 = registry.newResource();
        byte[] r1content = "q1 content".getBytes();
        r1.setContent(r1content);
        registry.put("/d14/d13/r1", r1);

        registry.applyTag("/d14/d13/r1", "tag1");
        registry.applyTag("/d14/d13/r1", "tag2");

        Tag[] tags = registry.getTags("/d14/d13/r1");


        boolean tagFound = false;
        for (Tag tag : tags) {
            if (tag.getTagName().equals("tag1")) {
                tagFound = true;
                assertEquals(tag.getTagName(), "tag1", "Tag names are not equals");
                assertEquals(tag.getCategory(), 1, "Tag category not equals");
                assertEquals((int) (tag.getTagCount()), 1, "Tag count not equals");
                assertEquals(tags.length, 2, "Tag length not equals");
                tag.setTagName("tag1_updated");
                break;

            }
        }

        TaggedResourcePath[] paths = null;
        try {

            paths = registry.getResourcePathsWithTag("tag1");

        } catch (Exception e) {
            fail("Failed to get resources with tag 'tag1'");
        }
        boolean artifactFound = false;
        for (TaggedResourcePath path : paths) {
            if (path.getResourcePath().equals("/d14/d13/r1")) {
                assertEquals(path.getResourcePath(), "/d14/d13/r1", "Path are not matching");
                assertEquals((int) (paths[0].getTagCount()), 1, "Tag count not equals");
                artifactFound = true;
            }
        }
        assertTrue(artifactFound, "/d14/d13/r1 is not tagged with the tag \"tag1\"");
        assertTrue(tagFound, "tag 'tag1' is not associated with the artifact /d14/d13/r1");
    }

    private void removeResourceTagging() throws Exception {
        Resource r1 = registry.newResource();
        byte[] r1content = "q1 content".getBytes();
        r1.setContent(r1content);
        registry.put("/d15/d14/r1", r1);

        registry.applyTag("/d15/d14/r1", "tag1");
        registry.applyTag("/d15/d14/r1", "tag2");

        Tag[] tags = registry.getTags("/d15/d14/r1");

        boolean tagFound = false;
        for (Tag tag : tags) {
            if (tag.getTagName().equals("tag1")) {
                tagFound = true;
            }

        }

        assertTrue(tagFound, "tag 'tag1' is not associated with the artifact /d15/d14/r1");

        /*remove tag goes here*/

        registry.removeTag("/d15/d14/r1", "tag1");

        TaggedResourcePath[] paths = registry.getResourcePathsWithTag("tag1");
        boolean artifactFound = false;
        for (TaggedResourcePath path : paths) {
            if (path.getResourcePath().equals("/d15/d14/r1")) {
                artifactFound = true;
            }
        }
        assertFalse(artifactFound, "/d15/d14/r1 is not tagged with the tag \"tag1\"");
        assertTrue(tagFound, "tag 'tag1' is not associated with the artifact /d15/d14/r1");
    }

    private void removeCollectionTagging() throws Exception {
        CollectionImpl r1 = new CollectionImpl();
        r1.setAuthorUserName("Author q1 remove");
        registry.put("/d15/d14/d13/d12", r1);

        registry.applyTag("/d15/d14/d13", "tag1");
        registry.applyTag("/d15/d14/d13", "tag2");
        registry.applyTag("/d15/d14/d13", "tag3");

        Tag[] tags = registry.getTags("/d15/d14/d13");
        boolean tagFound = false;
        for (Tag tag : tags) {
            if (tag.getTagName().equals("tag1")) {
                tagFound = true;


            }
        }

        assertTrue(tagFound, "tag 'tag1' is not associated with the artifact /d15/d14/d13");

        /*remove tag goes here*/

        registry.removeTag("/d15/d14/d13", "tag1");

        TaggedResourcePath[] paths = registry.getResourcePathsWithTag("tag1");
        boolean artifactFound = false;
        for (TaggedResourcePath path : paths) {
            if (path.getResourcePath().equals("/d15/d14/d13")) {
                artifactFound = true;
            }
        }
        assertFalse(artifactFound, "/d15/d14/d13 is not tagged with the tag \"tag1\"");
        assertTrue(tagFound, "tag 'tag1' is not associated with the artifact /d15/d14/d13");
    }

    private void tagging() throws Exception {
        // add a resource
        Resource r1 = registry.newResource();
        byte[] r1content = "R1 content".getBytes();
        r1.setContent(r1content);
        registry.put("/d11/r1", r1);

        Resource r2 = registry.newResource();
        byte[] r2content = "R2 content".getBytes();
        r2.setContent(r2content);
        registry.put("/d11/r2", r2);

        Resource r3 = registry.newResource();
        byte[] r3content = "R3 content".getBytes();
        r3.setContent(r3content);
        registry.put("/d11/r3", r3);

        registry.applyTag("/d11/r1", "JSP");
        registry.applyTag("/d11/r2", "jsp");
        registry.applyTag("/d11/r3", "jaVa");

        registry.applyTag("/d11/r1", "jsp");
        Tag[] r11Tags = registry.getTags("/d11/r1");
        assertEquals(r11Tags.length, 1);

        TaggedResourcePath[] paths = registry.getResourcePathsWithTag("jsp");
        boolean artifactFound = false;
        for (TaggedResourcePath path : paths) {
            if (path.getResourcePath().equals("/d11/r1")) {
                artifactFound = true;
                break;
            }
        }
        assertTrue(artifactFound, "/d11/r1 is not tagged with the tag \"jsp\"");
        Tag[] tags = registry.getTags("/d11/r1");
        boolean tagFound = false;
        for (Tag tag : tags) {
            if (tag.getTagName().equalsIgnoreCase("jsp")) {
                tagFound = true;
                break;
            }
        }
        assertTrue(tagFound, "tag 'jsp' is not associated with the artifact /d11/r1");
        registry.delete("/d11");
        TaggedResourcePath[] paths2 = registry.getResourcePathsWithTag("jsp");
        assertNull(paths2, "Tag based search should not return paths of deleted resources.");
    }


    @AfterClass
    public void cleanup() throws RegistryException {
        registry.delete("/d12");
        registry.delete("/d13");
        registry.delete("/d14");
        registry.delete("/d15");
    }
}
