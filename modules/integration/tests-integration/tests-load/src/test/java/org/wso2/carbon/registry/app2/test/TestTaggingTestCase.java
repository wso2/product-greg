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

package org.wso2.carbon.registry.app2.test;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.registry.app.RemoteRegistry;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;


import static org.testng.Assert.*;

/**
 * A test case which tests registry tagging
 */

public class TestTaggingTestCase extends GREGIntegrationBaseTest{
    public RemoteRegistry registry;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception{

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        registry = new RegistryProviderUtil().getRemoteRegistry(automationContext);
        Thread.sleep(60000);
    }

    @Test(groups = {"wso2.greg"}, priority = 9996)
    public void AddTaggingTest() throws Exception {
        // add a resource
        Resource r1 = registry.newResource();
        byte[] r1content = "q1 content".getBytes();
        r1.setContent(r1content);
        registry.put("/d11/r1", r1);

//        RemoteRegistry q2Registry = new RemoteRegistry(baseURL, "q2", "");
        Resource r2 = registry.newResource();
        byte[] r2content = "q2 content".getBytes();
        r2.setContent(r2content);
        registry.put("/d11/r2", r2);

//        RemoteRegistry q3Registry = new RemoteRegistry(baseURL, "q3", "");
        Resource r3 = registry.newResource();
        byte[] r3content = "q3 content".getBytes();
        r3.setContent(r3content);
        registry.put("/d11/r3", r3);

        registry.applyTag("/d11/r1", "jsp");
        registry.applyTag("/d11/r2", "jsp");
        registry.applyTag("/d11/r3", "java long tag");

        TaggedResourcePath[] paths = registry.getResourcePathsWithTag("jsp");
        boolean artifactFound = false;
        for (TaggedResourcePath path : paths) {
            //System.out.println("Available resource paths:" + path.getResourcePath());

            if (path.getResourcePath().equals("/d11/r1")) {
                assertEquals(path.getResourcePath(), "/d11/r1", "Path are not matching");
                artifactFound = true;
                //break;
            }
        }
        assertTrue(artifactFound, "/d11/r1 is not tagged with the tag \"jsp\"");

        Tag[] tags = null;


        try {
            tags = registry.getTags("/d11/r1");
        } catch (RegistryException e) {
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

        /* try {
           //registry.delete("/d11");
       } catch (RegistryException e) {
           fail("Failed to delete test resources.");
       }
        */

        registry.getResourcePathsWithTag("jsp");

//        assertEquals("Tag based search should not return paths of deleted resources.", paths2.length, 0);
    }

    @Test(groups = {"wso2.greg"})
    public void DuplicateTaggingTest() throws Exception {
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
                //System.out.println(tags[i].getTagName());
                //System.out.println(tags[i].getCategory());
                //System.out.println(tags[i].getTagCount());

                break;

            }
        }
        assertTrue(tagFound, "tag 'tag1' is not associated with the artifact /d12/r1");
    }

    @Test(groups = {"wso2.greg"})
    public void AddTaggingCollectionTest() throws Exception {
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

    @Test(groups = {"wso2.greg"})
    public void EditTaggingTest() throws Exception {
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
                //System.out.println(tag.getTagName());
                assertEquals(tag.getTagName(), "tag1", "Tag names are not equals");
                //System.out.println(tag.getCategory());
                assertEquals(tag.getCategory(), 1, "Tag category not equals");
                //System.out.println(tag.getTagCount());
                assertEquals((int) (tag.getTagCount()), 1, "Tag count not equals");
                //System.out.println(tags.length);
                assertEquals(tags.length, 2, "Tag length not equals");

                tag.setTagName("tag1_updated");
                break;

            }
        }

        TaggedResourcePath[] paths = null;
        try {

            paths = registry.getResourcePathsWithTag("tag1");

        } catch (RegistryException e) {
            fail("Failed to get resources with tag 'tag1'");
        }
        boolean artifactFound = false;
        for (TaggedResourcePath path : paths) {
            if (path.getResourcePath().equals("/d14/d13/r1")) {
                // System.out.println(paths[1].getResourcePath());
                assertEquals(path.getResourcePath(), "/d14/d13/r1", "Path are not matching");
                //System.out.println(paths[1].getTagCount());
                assertEquals((int) (paths[0].getTagCount()), 1, "Tag count not equals");
//                System.out.println(paths[1].getTagCounts());
//                assertEquals("Tag count not equals",0,(paths[0].getTagCounts()));
                artifactFound = true;
                //break;
            }
        }
        assertTrue(artifactFound, "/d11/r1 is not tagged with the tag \"jsp\"");
        assertTrue(tagFound, "tag 'col_tag1' is not associated with the artifact /d14/d13/r1");
    }

    @Test(groups = {"wso2.greg"})
    public void RemoveResourceTaggingTest() throws Exception {
        Resource r1 = registry.newResource();
        byte[] r1content = "q1 content".getBytes();
        r1.setContent(r1content);
        registry.put("/d15/d14/r1", r1);

        registry.applyTag("/d15/d14/r1", "tag1");
        registry.applyTag("/d15/d14/r1", "tag2");

        Tag[] tags = registry.getTags("/d15/d14/r1");

        boolean tagFound = false;
        for (Tag tag : tags) {
            //System.out.println("Available tags:" + tags[i].getTagName());
            if (tag.getTagName().equals("tag1")) {
                tagFound = true;
                //System.out.println(tags[i].getTagName());
                //System.out.println(tags[i].getCategory());
                //System.out.println(tags[i].getTagCount());
                //System.out.println(tags.length);

                //break;

            }

        }

        assertTrue(tagFound, "tag 'tag1' is not associated with the artifact /d15/d14/r1");

        /*remove tag goes here*/

        registry.removeTag("/d15/d14/r1", "tag1");

        TaggedResourcePath[] paths = registry.getResourcePathsWithTag("tag1");

        boolean artifactFound = false;
        for (TaggedResourcePath path : paths) {
            //System.out.println("tag1 Available at:" + paths[i].getResourcePath());
            if (path.getResourcePath().equals("/d15/d14/r1")) {
                //System.out.println(paths[i].getResourcePath());
                //System.out.println(paths[i].getTagCount());
                //System.out.println(paths[i].getTagCounts());
                artifactFound = true;
                //break;
            }
        }
        assertFalse(artifactFound, "/d15/d14/r1 is not tagged with the tag \"tag1\"");
        //assertTrue("/d15/d14/r1 is not tagged with the tag \"tag1\"", artifactFound);
        assertTrue(tagFound, "tag 'tag1' is not associated with the artifact /d15/d14/r1");
    }

    @Test(groups = {"wso2.greg"})
    public void RemoveCollectionTaggingTest() throws Exception {
        CollectionImpl r1 = new CollectionImpl();
        r1.setAuthorUserName("Author q1 remove");
        registry.put("/d15/d14/d13/d12", r1);

        registry.applyTag("/d15/d14/d13", "tag1");
        registry.applyTag("/d15/d14/d13", "tag2");
        registry.applyTag("/d15/d14/d13", "tag3");

        Tag[] tags = registry.getTags("/d15/d14/d13");
        //System.out.println("getTagCount:" + tags[0].getTagCount());

        boolean tagFound = false;
        for (Tag tag : tags) {
            //System.out.println("Available tags:" + tags[i].getTagName());
            //System.out.println("getTagCount for:" + tags[i].getTagCount());
            if (tag.getTagName().equals("tag1")) {
                tagFound = true;
                //System.out.println("getTagName:" + tags[i].getTagName());
                //System.out.println("getCategory:" + tags[i].getCategory());
                //System.out.println("getTagCount:" + tags[i].getTagCount());
                //System.out.println("TagLength:" + tags.length);

                //break;

            }
        }

        assertTrue(tagFound, "tag 'tag1' is not associated with the artifact /d15/d14/d13");

        /*remove tag goes here*/

        registry.removeTag("/d15/d14/d13", "tag1");

        TaggedResourcePath[] paths = registry.getResourcePathsWithTag("tag1");

        //System.out.println("Path tag counts:" + paths.length);
        boolean artifactFound = false;
        for (TaggedResourcePath path : paths) {
            //System.out.println("tag1 Available at:" + paths[i].getResourcePath());
            //System.out.println("getTagCounts:" + paths[i].getTagCounts());
            //System.out.println("getTagCount:" + paths[i].getTagCount());

            if (path.getResourcePath().equals("/d15/d14/d13")) {
                //System.out.println("getResourcePath:" + paths[i].getResourcePath());
                //System.out.println("getTagCount:" + paths[i].getTagCount());
                //System.out.println("getTagCounts:" + paths[i].getTagCounts());
                artifactFound = true;
                //break;
            }
        }
        assertFalse(artifactFound, "/d15/d14/d13 is not tagged with the tag \"tag1\"");
        //assertTrue("/d15/d14/r1 is not tagged with the tag \"tag1\"", artifactFound);
        assertTrue(tagFound, "tag 'tag1' is not associated with the artifact /d15/d14/d13");
    }

    @Test(groups = {"wso2.greg"})
    public void TaggingTest() throws Exception {
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

        assertEquals(paths2.length, 0, "Tag based search should not return paths of deleted resources.");
    }

    @AfterClass(alwaysRun = true)
    public void cleanArtifact() throws RegistryException {
        registry = null;
    }

}
