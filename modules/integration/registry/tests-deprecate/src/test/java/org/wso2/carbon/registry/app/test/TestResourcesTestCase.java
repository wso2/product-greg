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
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.util.List;

import static org.testng.Assert.*;

/**
 * A test case which tests registry resources
 */

public class TestResourcesTestCase {
    public RemoteRegistry registry;

    @BeforeClass(groups = {"wso2.greg"})
    public void init() {
        InitializeAPI initializeAPI = new InitializeAPI();
        registry = initializeAPI.getRegistry(FrameworkSettings.CARBON_HOME, FrameworkSettings.HTTPS_PORT, FrameworkSettings.HTTP_PORT);
    }

    @Test(groups = {"wso2.greg"})
    public void HierachicalResourceTest() throws Exception {
        Resource r1 = registry.newResource();
        String content = "this is my content1";
        r1.setContent(content.getBytes());
        r1.setDescription("This is r1 file description");

        String path = "/d1/d2/d3/r1";
        try {
            registry.put(path, r1);
        } catch (RegistryException e) {
            fail("Couldn't put content to path /d1/d2/d3/r1");
        }

        Resource r1_actual = registry.newResource();
        try {
            r1_actual = registry.get("/d1/d2/d3/r1");
        } catch (RegistryException e) {
            fail("Couldn't get content from path /d1/d2/d3/r1");
        }

        assertEquals(new String((byte[]) r1_actual.getContent()),
                new String((byte[]) r1.getContent()), "Content is not equal.");
        assertEquals(r1_actual.getLastUpdaterUserName(), "admin", "LastUpdatedUser is not Equal");
        assertEquals(r1_actual.getPath(), "/d1/d2/d3/r1", "Can not get Resource path");
        assertEquals(r1_actual.getParentPath(), "/d1/d2/d3", "Can not get Resource parent path");
        assertEquals(r1_actual.getDescription(), r1.getDescription(), "Resource description is not equal");
        assertEquals(r1_actual.getAuthorUserName(), "admin", "Resource description is not equal");

    }

    @Test(groups = {"wso2.greg"})
    public void UpdateResouceContentTest() throws Exception {

        Resource r1 = registry.newResource();
        String content = "this is my content1";
        r1.setContent(content.getBytes());
        r1.setDescription("This is r1 file description");
        r1.setProperty("key1", "value1");
        r1.setProperty("key2", "value2");

        String path = "/d1/d2/d3/d4/r1";
        try {
            registry.put(path, r1);
        } catch (RegistryException e) {
            fail("Couldn't put content to path /d1/d2/d3/d4/r1");
        }

        Resource r1_actual = registry.get("/d1/d2/d3/d4/r1");

        assertEquals(new String((byte[]) r1_actual.getContent()),
                new String((byte[]) r1.getContent()), "Content is not equal.");
        assertEquals(r1_actual.getLastUpdaterUserName(), "admin", "LastUpdatedUser is not Equal");
        assertEquals(r1_actual.getPath(), "/d1/d2/d3/d4/r1", "Can not get Resource path");
        assertEquals(r1_actual.getParentPath(), "/d1/d2/d3/d4", "Can not get Resource parent path");
        assertEquals(r1_actual.getDescription(), r1.getDescription(), "Resource description is not equal");
        assertEquals(r1_actual.getAuthorUserName(), "admin", "Author is not equal");
        assertEquals(r1_actual.getProperty("key1"), r1.getProperty("key1"), "Resource properties are equal");
        assertEquals(r1_actual.getProperty("key2"), r1.getProperty("key2"), "Resource properties are equal");
        assertEquals(r1_actual.getProperty("key3_update"), r1.getProperty("key3_update"),
                "Resource properties are equal");

        String contentUpdated = "this is my content updated";
        r1.setContent(contentUpdated.getBytes());
        r1.setDescription("This is r1 file description updated");
        r1.setProperty("key1", "value1_update");
        r1.setProperty("key2", "value2_update");
        r1.setProperty("key3_update", "value3_update");

        registry.put(path, r1);
        Resource r2_actual = registry.get("/d1/d2/d3/d4/r1");

        assertEquals(new String((byte[]) r2_actual.getContent()),
                new String((byte[]) r1.getContent()), "Content is not equal.");
        assertEquals(r2_actual.getLastUpdaterUserName(), "admin", "LastUpdatedUser is not Equal");
        assertEquals(r2_actual.getPath(), "/d1/d2/d3/d4/r1", "Can not get Resource path");
        assertEquals(r2_actual.getParentPath(), "/d1/d2/d3/d4", "Can not get Resource parent path");
        assertEquals(r2_actual.getDescription(), r1.getDescription(), "Resource description is not equal");
        assertEquals(r2_actual.getAuthorUserName(), "admin", "Author is not equal");
        assertEquals(r2_actual.getProperty("key1"), r1.getProperty("key1"), "Resource properties are equal");
        assertEquals(r2_actual.getProperty("key2"), r1.getProperty("key2"), "Resource properties are equal");
        assertEquals(r2_actual.getProperty("key3_update"), r1.getProperty("key3_update"),
                "Resource properties are equal");
    }


    @Test(groups = {"wso2.greg"})
    public void AddAnotherResourceTest() throws Exception {
        Resource r1 = registry.newResource();
        String content = "this is my content2";
        r1.setContent(content.getBytes());
        r1.setDescription("r2 file description");
        String path = "/d1/d2/r2";

        r1.setProperty("key1", "value1");
        r1.setProperty("key2", "value2");
        try {
            registry.put(path, r1);
        } catch (RegistryException e) {
            fail("Couldn't put content to path /d1/d2/r2");
        }

        Resource r1_actual = registry.newResource();
        try {
            r1_actual = registry.get("/d1/d2/r2");
        } catch (RegistryException e) {
            fail("Couldn't get content from path /d1/d2/r2");
        }

        assertEquals(new String((byte[]) r1_actual.getContent()),
                new String((byte[]) r1.getContent()), "Content is not equal.");
        assertEquals(r1_actual.getLastUpdaterUserName(), "admin", "LastUpdatedUser is not Equal");
        assertEquals(r1_actual.getPath(), "/d1/d2/r2", "Can not get Resource path");
        assertEquals(r1_actual.getParentPath(), "/d1/d2", "Can not get Resource parent path");
        assertEquals(r1_actual.getDescription(), r1.getDescription(), "Resource description is not equal");
        assertEquals(r1_actual.getAuthorUserName(), "admin", "Author is not equal");
        assertEquals(r1_actual.getProperty("key1"), r1.getProperty("key1"), "Resource properties are equal");
        assertEquals(r1_actual.getProperty("key2"), r1.getProperty("key2"), "Resource properties are equal");

    }

    @Test(groups = {"wso2.greg"})
    public void SetResourceDetailsTest() throws Exception {
        Resource r1 = registry.newResource();
        r1.setDescription("R4 collection description");
        r1.setMediaType("jpg/image");
        r1.setContent(new byte[]{(byte) 0xDE, (byte) 0xDE, (byte) 0xDE, (byte) 0xDE});

        r1.setProperty("key1", "value5");
        r1.setProperty("key2", "value3");

        String path_collection = "/c11/c12/c13/c14/r4";
        try {
            registry.put(path_collection, r1);
        } catch (RegistryException e) {
            fail("Couldn't put content to path /c11/c12/c13/c14/r4");
        }

        Resource r1_actual = null;
        try {
            r1_actual = registry.get("/c11/c12/c13/c14/r4");
        } catch (RegistryException e) {
            fail("Couldn't get content from path /c11/c12/c13/c14/r4");
        }

        assertEquals(r1_actual.getLastUpdaterUserName(), "admin", "LastUpdatedUser is not Equal");
        assertEquals(r1_actual.getPath(), path_collection, "Can not get Resource path");
        assertEquals(r1_actual.getParentPath(), "/c11/c12/c13/c14", "Can not get Resource parent path");
        assertEquals(r1_actual.getDescription(), r1.getDescription(), "Resource description is not equal");
        assertEquals(r1_actual.getAuthorUserName(), "admin", "Author is not equal");
        assertEquals(r1_actual.getProperty("key1"), r1.getProperty("key1"), "Resource properties are not equal");
        assertEquals(r1_actual.getProperty("key2"), r1.getProperty("key2"), "Resource properties are not equal");
        assertEquals(r1_actual.getMediaType(), r1.getMediaType(), "Media Types are not equal");

    }

    @Test(groups = {"wso2.greg"})
    public void CollectionDetailsTest() throws Exception {
        Resource r1 = registry.newResource();
        String content = "this is my content4";
        r1.setContent(content.getBytes());
        r1.setDescription("r3 file description");
        String path = "/c1/c2/c3/c4/r3";

        try {
            registry.put(path, r1);
        } catch (RegistryException e) {
            fail("Couldn't put Collection to path /c1/c2/c3/c4/r3");
        }

        try {
            registry.get("/c1/c2/c3");
        } catch (RegistryException e) {
            fail("Couldn't get content from path /c1/c2/c3");
        }

        String path_delete = "/c1/c2/c3";
        try {
            registry.delete(path_delete);
        } catch (RegistryException e) {
            fail("Couldn't delete content resource " + path_delete);
        }

        boolean failed = false;
        try {
            registry.get(path);
        } catch (RegistryException e) {
            failed = true;
        }

        assertTrue(failed, "Deleted resource /r1 is returned on get.");
    }

    @Test(groups = {"wso2.greg"})
    public void SetCollectionDetailsTest() throws Exception {
        Collection r1 = registry.newCollection();
        r1.setDescription("C3 collection description");
        r1.setProperty("key1", "value5");
        r1.setProperty("key2", "value3");

        String path_collection = "/c1/c2/c3";

        registry.put(path_collection, r1);

        Resource r1_actual = registry.get("/c1/c2/c3");

        assertTrue(r1_actual instanceof Collection);
        assertEquals(r1_actual.getLastUpdaterUserName(), "admin", "LastUpdatedUser is not Equal");
        assertEquals(r1_actual.getPath(), path_collection, "Can not get Resource path");
        assertEquals(r1_actual.getParentPath(), "/c1/c2", "Can not get Resource parent path");
        assertEquals(r1_actual.getDescription(), r1.getDescription(), "Resource description is not equal");
        assertEquals(r1_actual.getAuthorUserName(), "admin", "Authour is not equal");
        assertEquals(r1_actual.getProperty("key1"), r1.getProperty("key1"), "Resource properties are not equal");
        assertEquals(r1_actual.getProperty("key2"), r1.getProperty("key2"), "Resource properties are not equal");
    }

    @Test(groups = {"wso2.greg"})
    public void DeleteResourceTest() throws Exception {

        Resource r1 = registry.newResource();
        r1.setContent("this is file for deleting");
        r1.setDescription("this is the description of deleted file");
        r1.setMediaType("text/plain");

        r1.setProperty("key1", "value1");
        r1.setProperty("key2", "value2");

        String path = "/c11/c12/c13/r4";

        registry.put(path, r1);

        String path_delete = "/c11/c12/c13/r4";

        registry.delete(path_delete);

        boolean failed = false;
        try {
            registry.get("/c11/c12/c13/r4");
        } catch (RegistryException e) {
            failed = true;
        }

        assertTrue(failed, "Deleted resource /c11/c12/c13/r4 is returned on get.");

        /*Add deleted resource again in to same path*/

        Resource r2 = registry.newResource();
        r2.setContent("This is new contenet added after deleting");
        r2.setDescription("this is desc for new resource");
        r2.setMediaType("text/plain");

        r2.setProperty("key1", "value5");
        r2.setProperty("key2", "value3");

        String path_new = "/c11/c12/c13/r4";
        try {
            registry.put(path_new, r2);
        } catch (RegistryException e) {
            fail("Couldn't put content to path /c11/c12/c13/r4");
        }

        Resource r1_actual = null;
        try {
            r1_actual = registry.get(path_new);
        } catch (RegistryException e) {
            fail("Couldn't get content of path /c11/c12/c13/r4");
        }
        assertEquals(r1_actual.getLastUpdaterUserName(), "admin", "LastUpdatedUser is not Equal");
        assertEquals(r1_actual.getPath(), path_new, "Can not get Resource path");
        assertEquals(r1_actual.getParentPath(), "/c11/c12/c13", "Can not get Resource parent path");
        assertEquals(r1_actual.getDescription(), r2.getDescription(), "Resource description is not equal");
        assertEquals(r1_actual.getAuthorUserName(), "admin", "Authour is not equal");
        assertEquals(r1_actual.getProperty("key1"), r2.getProperty("key1"), "Resource properties are equal");
        assertEquals(r1_actual.getProperty("key2"), r2.getProperty("key2"), "Resource properties are equal");
        assertEquals(r1_actual.getMediaType(), r2.getMediaType(), "Media Types is not equal");
    }

    @Test(groups = {"wso2.greg"})
    public void DeleteCollectionTest() throws Exception {

        Resource r1 = registry.newCollection();
        r1.setDescription("this is a collection for deleting");
        r1.setMediaType("text/plain");
        r1.setProperty("key1", "value1");
        r1.setProperty("key2", "value2");
        String path = "/c20/c21/c22";

        registry.put(path, r1);
        String path_delete = "/c20/c21/c22";

        registry.delete(path_delete);

        boolean failed = false;
        try {
            registry.get("/c20/c21/c22");
        } catch (RegistryException e) {
            failed = true;
        }

        assertTrue(failed, "Deleted collection /c20/c21/c22 is returned on get.");

        /*Add deleted resource again in to same path*/

        Resource r2 = registry.newCollection();
        r2.setDescription("this is desc for new collection");
        r2.setProperty("key1", "value5");
        r2.setProperty("key2", "value3");
        String path_new = "/c20/c21/c22";

        try {
            registry.put(path_new, r2);
        } catch (RegistryException e) {
            fail("Couldn't put content to path /c20/c21/c22");
        }

        Resource r1_actual = registry.newCollection();
        try {
            r1_actual = registry.get(path_new);
        } catch (RegistryException e) {
            fail("Couldn't get content of path /c20/c21/c22");
        }
        assertEquals(r1_actual.getLastUpdaterUserName(), "admin", "LastUpdatedUser is not Equal");
        assertEquals(r1_actual.getPath(), path_new, "Can not get Resource path");
        assertEquals(r1_actual.getParentPath(), "/c20/c21", "Can not get Resource parent path");
        assertEquals(r1_actual.getDescription(), r2.getDescription(), "Resource description is not equal");
        assertEquals(r1_actual.getAuthorUserName(), "admin", "Authour is not equal");
        assertEquals(r1_actual.getProperty("key1"), r2.getProperty("key1"), "Resource properties are equal");
        assertEquals(r1_actual.getProperty("key2"), r2.getProperty("key2"), "Resource properties are equal");
    }

    @Test(groups = {"wso2.greg"})
    public void AddSpacesinResNameTest() throws Exception {

        Resource r1 = registry.newResource();
        r1.setContent("this is file file content");
        r1.setDescription("this is a file name with spaces");
        r1.setMediaType("text/plain");
        r1.setProperty("key1", "value5");
        r1.setProperty("key2", "value3");

        String path = "/d11/d12/d13/r1 space";
        String actualPath = null;
        try {
            actualPath = registry.put(path, r1);
        } catch (RegistryException e) {
            fail("Couldn't put content to path /d11/d12/d13/r1 space");
        }

        Resource r1_actual = null;
        try {
            r1_actual = registry.get(actualPath);
        } catch (RegistryException e) {
            fail("Couldn't get content of path /d11/d12/d13/r1 space");
        }

        assertEquals(r1_actual.getLastUpdaterUserName(), "admin", "LastUpdatedUser is not Equal");
        assertEquals(r1_actual.getPath(), path, "Can not get Resource path");
        assertEquals(r1_actual.getParentPath(), "/d11/d12/d13", "Can not get Resource parent path");
        assertEquals(r1_actual.getDescription(), r1.getDescription(), "Resource description is not equal");
        assertEquals(r1_actual.getAuthorUserName(), "admin", "Authour is not equal");
        assertEquals(r1_actual.getProperty("key1"), r1.getProperty("key1"), "Resource properties are not equal");
        assertEquals(r1_actual.getProperty("key2"), r1.getProperty("key2"), "Resource properties are not equal");
        assertEquals(r1_actual.getMediaType(), r1.getMediaType(), "Media Types are not equal");


    }

    @Test(groups = {"wso2.greg"})
    public void AddSpacesinCollNameTest() throws Exception {

        Collection c1 = registry.newCollection();
        c1.setDescription("this is a collection name with spaces");
        c1.setProperty("key1", "value5");
        c1.setProperty("key2", "value3");
        String path = "/col1/col2/col30 space45";

        String actualPath = null;
        try {
            actualPath = registry.put(path, c1);
        } catch (RegistryException e) {
            fail("Couldn't put collection /col1/col2/col3 space");
        }

//        Resource r1_actual = null;
//        try {
//            r1_actual = registry.get(path);
//        } catch (RegistryException e) {
//            fail("Couldn't get collecion of path " + path);
//        }
//        assertEquals("LastUpdatedUser is not Equal", "admin", r1_actual.getLastUpdaterUserName());
//        assertEquals("Can not get Resource path", actualPath, r1_actual.getPath());
//        assertEquals("Can not get Resource parent path", "/col1/col2", r1_actual.getParentPath());
//        assertEquals("Resource description is not equal", c1.getDescription(),
//                     r1_actual.getDescription());
//        assertEquals("Authour is not equal", "admin", r1_actual.getAuthorUserName());
//        assertEquals("Resource properties are equal", c1.getProperty("key1"),
//                     r1_actual.getProperty("key1"));
//        assertEquals("Resource properties are equal", c1.getProperty("key2"),
//                     r1_actual.getProperty("key2"));
    }

    @Test(groups = {"wso2.greg"})
    public void AddResourceFromURLTest() throws Exception {

        String path = "/d25/d21/d23/d24/r1";
        String url = "http://shortwaveapp.com/waves.txt";

        Resource r1 = registry.newResource();
        r1.setDescription("this is a file imported from url");
        r1.setMediaType("text/plain");
        r1.setProperty("key1", "value5");
        r1.setProperty("key2", "value3");

        try {
            registry.importResource(path, url, r1);
        } catch (RegistryException e) {
            fail("Couldn't import content to path:" + path);
        }

        Resource r1_actual = registry.newResource();
        try {
            r1_actual = registry.get(path);
        } catch (RegistryException e) {
            fail("Couldn't get content from path" + path);
        }

        boolean content = true;
        if (r1_actual == null) {
            content = false;
        }

        assertTrue(content, "Imported file is empty");
        assertEquals(r1_actual.getLastUpdaterUserName(), "admin", "LastUpdatedUser is not Equal");
        assertEquals(r1_actual.getPath(), path, "Can not get Resource path");
        assertEquals(r1_actual.getParentPath(), "/d25/d21/d23/d24", "Can not get Resource parent path");
        //assertEquals("Resource description is not equal", r1.getDescription(), r1_actual.getDescription());
        assertEquals(r1_actual.getAuthorUserName(), "admin", "Authour is not equal");
        //assertEquals("Resource properties are equal", r1.getProperty("key1"), r1_actual.getProperty("key1"));
        //assertEquals("Resource properties are equal", r1.getProperty("key2"), r1_actual.getProperty("key2"));
    }

    @Test(groups = {"wso2.greg"})
    public void RenameResourceTest() throws Exception {

        Resource r1 = registry.newResource();
        String content = "this is my content";
        r1.setContent(content.getBytes());
        r1.setDescription("This is r1 file description");
        String path = "/d30/d31/r1";

        try {
            registry.put(path, r1);
        } catch (RegistryException e) {
            fail("Couldn't put content to path" + path);
        }

        Resource r1_actual = registry.newResource();
        try {
            r1_actual = registry.get(path);
        } catch (RegistryException e) {
            fail("Couldn't get content from path" + path);
        }

        assertEquals(new String((byte[]) r1_actual.getContent()),
                new String((byte[]) r1.getContent()), "Content is not equal.");

        /*rename the resource*/

        String new_path = "/d33/d34/r1";

        try {
            registry.rename(path, new_path);
        } catch (RegistryException e) {
            fail("Can not rename the path from" + path + "to" + new_path);
        }

        Resource r2_actual = registry.newResource();
        try {
            r2_actual = registry.get(new_path);
        } catch (RegistryException e) {
            fail("Couldn't get content from path" + new_path);
        }
        assertEquals(r2_actual.getLastUpdaterUserName(), "admin", "LastUpdatedUser is not Equal");
        assertEquals(r2_actual.getPath(), new_path, "Can not get Resource path");
        assertEquals(r2_actual.getParentPath(), "/d33/d34", "Can not get Resource parent path");
        assertEquals(r2_actual.getDescription(), r1.getDescription(), "Resource description is not equal");
        assertEquals(r2_actual.getAuthorUserName(), "admin", "Authour is not equal");
        assertEquals(r2_actual.getProperty("key1"), r1.getProperty("key1"), "Resource properties are equal");
        assertEquals(r2_actual.getProperty("key2"), r1.getProperty("key2"), "Resource properties are equal");
    }

    @Test(groups = {"wso2.greg"})
    public void DeleteandUpdateResourceTest() throws Exception {

        Resource r1 = registry.newResource();
        String content = "this is my content";
        r1.setContent(content.getBytes());
        r1.setDescription("This is r1 file description");
        String path = "/d40/d43/r1";

        try {
            registry.put(path, r1);
        } catch (RegistryException e) {
            fail("Couldn't put content to path" + path);
        }

        Resource r1_actual = registry.newResource();
        try {
            r1_actual = registry.get(path);
        } catch (RegistryException e) {
            fail("Couldn't get content from path" + path);
        }

        assertEquals(new String((byte[]) r1_actual.getContent()),
                new String((byte[]) r1.getContent()), "Content is not equal.");

        boolean deleted = true;
        try {
            registry.delete(path);
        } catch (RegistryException e) {
            fail("Couldn't delete the resource from path" + path);
            deleted = false;
        }

        assertTrue(deleted, "Resource not deleted");

        /*add the same resource again*/

        Resource r2 = registry.newResource();
        String content2 = "this is my content updated";
        r2.setContent(content2.getBytes());
        r2.setDescription("This is r1 file description");

        String path_new = "/d40/d43/r1";
        try {
            registry.put(path_new, r2);
        } catch (RegistryException e) {
            fail("Couldn't put content to path" + path_new);
        }

        Resource r1_actual2 = registry.newResource();
        try {
            r1_actual2 = registry.get(path_new);
        } catch (RegistryException e) {
            fail("Couldn't get content from path" + path_new);
        }

        assertEquals(new String((byte[]) r1_actual2.getContent()),
                new String((byte[]) r2.getContent()), "Content is not equal.");
    }

    @Test(groups = {"wso2.greg"})
    public void ResourcemultiplePropertiesTest() throws Exception {

        try {
            String path = "/m11/m12/r1";
            Resource r1 = registry.newResource();
            String content = "this is my content";
            r1.setContent(content.getBytes());
            r1.setDescription("This is r1 file description");

            r1.addProperty("key1", "value1");
            r1.addProperty("key1", "value2");
            r1.addProperty("key1", "value3");
            r1.addProperty("key2", "value1");
            r1.addProperty("key2", "value2");

            registry.put(path, r1);

            Resource r1_actual2 = registry.get(path);

            assertEquals(new String((byte[]) r1_actual2.getContent()),
                    new String((byte[]) r1.getContent()), "Content is not equal.");

            List propertyValues = r1_actual2.getPropertyValues("key1");
            Object[] valueName = propertyValues.toArray();

            List propertyValuesKey2 = r1_actual2.getPropertyValues("key2");
            Object[] valueNameKey2 = propertyValuesKey2.toArray();

            assertTrue(containsString(valueName, "value1"), "value1 is not associated with key1");
            assertTrue(containsString(valueName, "value2"), "value2 is not associated with key1");
            assertTrue(containsString(valueName, "value3"), "value3 is not associated with key1");
            assertTrue(containsString(valueNameKey2, "value1"), "value1 is not associated with key2");
            assertTrue(containsString(valueNameKey2, "value2"), "value2 is not associated with key2");

        } catch (RegistryException e) {
            e.printStackTrace();
        }

    }

    @Test(groups = {"wso2.greg"})
    public void CollectionmultiplePropertiesTest() throws Exception {

        try {
            String path = "/m15/m16/m17";
            Resource r1 = registry.newCollection();

            r1.setDescription("This m17 description");
            r1.addProperty("key1", "value1");
            r1.addProperty("key1", "value2");
            r1.addProperty("key1", "value3");
            r1.addProperty("key2", "value1");
            r1.addProperty("key2", "value2");

            registry.put(path, r1);

            Resource r1_actual2 = registry.get(path);

            List propertyValues = r1_actual2.getPropertyValues("key1");
            Object[] valueName = propertyValues.toArray();

            List propertyValuesKey2 = r1_actual2.getPropertyValues("key2");
            Object[] valueNameKey2 = propertyValuesKey2.toArray();

            assertTrue(containsString(valueName, "value1"), "value1 is not associated with key1");
            assertTrue(containsString(valueName, "value2"), "value2 is not associated with key1");
            assertTrue(containsString(valueName, "value3"), "value3 is not associated with key1");
            assertTrue(containsString(valueNameKey2, "value1"), "value1 is not associated with key2");
            assertTrue(containsString(valueNameKey2, "value2"), "value2 is not associated with key2");

        } catch (RegistryException e) {
            e.printStackTrace();
        }

    }

    private boolean containsString(Object[] array, String value) {

        boolean found = false;
        for (Object anArray : array) {
            String s = anArray.toString();
            if (s.startsWith(value)) {
                found = true;
                break;
            }
        }

        return found;
    }
}
