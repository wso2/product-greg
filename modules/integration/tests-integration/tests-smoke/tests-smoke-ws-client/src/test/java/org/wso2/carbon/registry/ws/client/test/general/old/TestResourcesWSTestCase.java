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

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import java.util.Date;
import java.util.List;

import static org.testng.Assert.*;

/**
 * A test case which tests registry resources
 */
public class TestResourcesWSTestCase extends GREGIntegrationBaseTest {

    private WSRegistryServiceClient registry;

    @BeforeClass(groups = {"wso2.greg"})
    public void initTest() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        registry = registryProviderUtil.getWSRegistry(automationContext);
    }

    @Test(groups = {"wso2.greg"})
    public void testResources() {
        try {
            hierachicalResource();
            updateResourceContent();
            addAnotherResource();
            setResourceDetails();
            collectionDetails();
            setCollectionDetails();
            deleteResource();
            deleteCollection();
            addSpacesInResName();
            addSpacesInCollName();
            addResourceFromURL();
            renameResource();
            deleteAndUpdateResource();
            resourceMultipleProperties();
            collectionMultipleProperties();
            getMetaData();
            testLastModifiedDateChange();
        } catch(Exception e) {
            e.printStackTrace();
            fail("The Resources Test for WS-API failed");
        }
    }

    private void hierachicalResource() throws Exception {
        Resource r1 = registry.newResource();
        String content = "this is my content1";
        r1.setContent(content.getBytes());
        r1.setDescription("This is r1 file description");
        String path = "/d1/d2/d3/r1";
        try {
            registry.put(path, r1);
        } catch(RegistryException e) {
            fail("Couldn't put content to path /d1/d2/d3/r1");
        }
        Resource r1_actual = registry.newResource();
        try {
            r1_actual = registry.get("/d1/d2/d3/r1");
        } catch(RegistryException e) {
            fail("Couldn't get content from path /d1/d2/d3/r1");
        }
        assertEquals(new String((byte[]) r1_actual.getContent()), new String((byte[]) r1.getContent()), "Content is not equal.");
        assertEquals(r1_actual.getLastUpdaterUserName(), "admin", "LastUpdatedUser is not Equal");
        assertEquals(r1_actual.getPath(), "/d1/d2/d3/r1", "Can not get Resource path");
        assertEquals(r1_actual.getParentPath(), "/d1/d2/d3", "Can not get Resource parent path");
        assertEquals(r1_actual.getDescription(), r1.getDescription(), "Resource description is not equal");
        assertEquals(r1_actual.getAuthorUserName(), "admin", "Resource description is not equal");
    }

    private void updateResourceContent() throws Exception {
        Resource r1 = registry.newResource();
        String content = "this is my content1";
        r1.setContent(content.getBytes());
        r1.setDescription("This is r1 file description");
        r1.setProperty("key1", "value1");
        r1.setProperty("key2", "value2");
        String path = "/d1/d2/d3/d4/r1";
        try {
            registry.put(path, r1);
        } catch(RegistryException e) {
            fail("Couldn't put content to path /d1/d2/d3/d4/r1");
        }
        Resource r1_actual = registry.get("/d1/d2/d3/d4/r1");
        assertEquals(new String((byte[]) r1_actual.getContent()), new String((byte[]) r1.getContent()), "Content is not equal.");
        assertEquals(r1_actual.getLastUpdaterUserName(), "admin", "LastUpdatedUser is not Equal");
        assertEquals(r1_actual.getPath(), "/d1/d2/d3/d4/r1", "Can not get Resource path");
        assertEquals(r1_actual.getParentPath(), "/d1/d2/d3/d4", "Can not get Resource parent path");
        assertEquals(r1_actual.getDescription(), r1.getDescription(), "Resource description is not equal");
        assertEquals(r1_actual.getAuthorUserName(), "admin", "Author is not equal");
        assertEquals(r1_actual.getProperty("key1"), r1.getProperty("key1"), "Resource properties are equal");
        assertEquals(r1_actual.getProperty("key2"), r1.getProperty("key2"), "Resource properties are equal");
        assertEquals(r1_actual.getProperty("key3_update"), r1.getProperty("key3_update"), "Resource properties are equal");
        String contentUpdated = "this is my content updated";
        r1.setContent(contentUpdated.getBytes());
        r1.setDescription("This is r1 file description updated");
        r1.setProperty("key1", "value1_update");
        r1.setProperty("key2", "value2_update");
        r1.setProperty("key3_update", "value3_update");
        registry.put(path, r1);
        Resource r2_actual = registry.get("/d1/d2/d3/d4/r1");
        assertEquals(new String((byte[]) r2_actual.getContent()), new String((byte[]) r1.getContent()), "Content is not equal.");
        assertEquals(r2_actual.getLastUpdaterUserName(), "admin", "LastUpdatedUser is not Equal");
        assertEquals(r2_actual.getPath(), "/d1/d2/d3/d4/r1", "Can not get Resource path");
        assertEquals(r2_actual.getParentPath(), "/d1/d2/d3/d4", "Can not get Resource parent path");
        assertEquals(r2_actual.getDescription(), r1.getDescription(), "Resource description is not equal");
        assertEquals(r2_actual.getAuthorUserName(), "admin", "Author is not equal");
        assertEquals(r2_actual.getProperty("key1"), r1.getProperty("key1"), "Resource properties are equal");
        assertEquals(r2_actual.getProperty("key2"), r1.getProperty("key2"), "Resource properties are equal");
        assertEquals(r2_actual.getProperty("key3_update"), r1.getProperty("key3_update"), "Resource properties are equal");
    }

    private void getMetaData() throws Exception {
        Resource r1 = registry.newResource();
        String content = "this is my content1";
        r1.setContent(content.getBytes());
        r1.setDescription("This is r1 file description");
        r1.setProperty("key1", "value1");
        r1.setProperty("key2", "value2");
        String path = "/d1/d2/d3/d4/r1";
        try {
            registry.put(path, r1);
        } catch(RegistryException e) {
            fail("Couldn't put content to path /d1/d2/d3/d4/r1");
        }
        Resource r1_actual = registry.getMetaData("/d1/d2/d3/d4/r1");
        assertEquals(r1_actual.getLastUpdaterUserName(), "admin", "LastUpdatedUser is not Equal");
        assertEquals(r1_actual.getPath(), "/d1/d2/d3/d4/r1", "Can not get Resource path");
        assertEquals(r1_actual.getParentPath(), "/d1/d2/d3/d4", "Can not get Resource parent path");
        assertEquals(r1_actual.getDescription(), r1.getDescription(), "Resource description is not equal");
        assertEquals(r1_actual.getAuthorUserName(), "admin", "Author is not equal");
        assertEquals(r1_actual.getProperty("key1"), null, "Resource properties cannot be equal");
        assertEquals(r1_actual.getProperty("key2"), null, "Resource properties cannot be equal");
        assertEquals(r1_actual.getProperty("key3_update"), null, "Resource properties cannot be equal");
    }

    private void addAnotherResource() throws Exception {
        Resource r1 = registry.newResource();
        String content = "this is my content2";
        r1.setContent(content.getBytes());
        r1.setDescription("r2 file description");
        String path = "/d1/d2/r2";
        r1.setProperty("key1", "value1");
        r1.setProperty("key2", "value2");
        try {
            registry.put(path, r1);
        } catch(RegistryException e) {
            fail("Couldn't put content to path /d1/d2/r2");
        }
        Resource r1_actual = registry.newResource();
        try {
            r1_actual = registry.get("/d1/d2/r2");
        } catch(RegistryException e) {
            fail("Couldn't get content from path /d1/d2/r2");
        }
        assertEquals(new String((byte[]) r1_actual.getContent()), new String((byte[]) r1.getContent()), "Content is not equal.");
        assertEquals(r1_actual.getLastUpdaterUserName(), "admin", "LastUpdatedUser is not Equal");
        assertEquals(r1_actual.getPath(), "/d1/d2/r2", "Can not get Resource path");
        assertEquals(r1_actual.getParentPath(), "/d1/d2", "Can not get Resource parent path");
        assertEquals(r1_actual.getDescription(), r1.getDescription(), "Resource description is not equal");
        assertEquals(r1_actual.getAuthorUserName(), "admin", "Author is not equal");
        assertEquals(r1_actual.getProperty("key1"), r1.getProperty("key1"), "Resource properties are equal");
        assertEquals(r1_actual.getProperty("key2"), r1.getProperty("key2"), "Resource properties are equal");
    }

    private void setResourceDetails() throws Exception {
        Resource r1 = registry.newResource();
        r1.setDescription("R4 collection description");
        r1.setMediaType("jpg/image");
        r1.setContent(new byte[]{(byte) 0xDE, (byte) 0xDE, (byte) 0xDE, (byte) 0xDE});
        r1.setProperty("key1", "value5");
        r1.setProperty("key2", "value3");
        String path_collection = "/c11/c12/c13/c14/r4";
        try {
            registry.put(path_collection, r1);
        } catch(RegistryException e) {
            fail("Couldn't put content to path /c11/c12/c13/c14/r4");
        }
        Resource r1_actual = null;
        try {
            r1_actual = registry.get("/c11/c12/c13/c14/r4");
        } catch(RegistryException e) {
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

    private void collectionDetails() throws Exception {
        Resource r1 = registry.newResource();
        String content = "this is my content4";
        r1.setContent(content.getBytes());
        r1.setDescription("r3 file description");
        String path = "/c1/c2/c3/c4/r3";
        try {
            registry.put(path, r1);
        } catch(Exception e) {
            fail("Couldn't put Collection to path /c1/c2/c3/c4/r3");
        }
        try {
            registry.get("/c1/c2/c3");
        } catch(Exception e) {
            fail("Couldn't get content from path /c1/c2/c3");
        }
        String path_delete = "/c1/c2/c3";
        try {
            registry.delete(path_delete);
        } catch(Exception e) {
            fail("Couldn't delete content resource " + path_delete);
        }
        boolean failed = false;
        try {
            registry.get(path);
        } catch(Exception e) {
            failed = true;
        }
        assertTrue(failed, "Deleted resource /r1 is returned on get.");
    }

    private void setCollectionDetails() throws Exception {
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

    private void deleteResource() throws Exception {
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
        } catch(Exception e) {
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
        } catch(Exception e) {
            fail("Couldn't put content to path /c11/c12/c13/r4");
        }
        Resource r1_actual = null;
        try {
            r1_actual = registry.get(path_new);
        } catch(Exception e) {
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

    private void deleteCollection() throws Exception {
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
        } catch(Exception e) {
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
        } catch(Exception e) {
            fail("Couldn't put content to path /c20/c21/c22");
        }
        Resource r1_actual = registry.newCollection();
        try {
            r1_actual = registry.get(path_new);
        } catch(Exception e) {
            fail("Couldn't get content of path /c20/c21/c22");
        }
        assertEquals(r1_actual.getLastUpdaterUserName(), "admin", "LastUpdatedUser is not Equal");
        assertEquals(r1_actual.getPath(), path_new, "Can not get Resource path");
        assertEquals("/c20/c21", r1_actual.getParentPath(), "Can not get Resource parent path");
        assertEquals(r1_actual.getDescription(), r2.getDescription(), "Resource description is not equal");
        assertEquals(r1_actual.getAuthorUserName(), "admin", "Authour is not equal");
        assertEquals(r1_actual.getProperty("key1"), r2.getProperty("key1"), "Resource properties are equal");
        assertEquals(r1_actual.getProperty("key2"), r2.getProperty("key2"), "Resource properties are equal");
    }

    private void addSpacesInResName() throws Exception {
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
        } catch(Exception e) {
            fail("Couldn't put content to path /d11/d12/d13/r1 space");
        }
        Resource r1_actual = null;
        try {
            r1_actual = registry.get(actualPath);
        } catch(Exception e) {
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

    private void addSpacesInCollName() throws Exception {
        Collection c1 = registry.newCollection();
        c1.setDescription("this is a collection name with spaces");
        c1.setProperty("key1", "value5");
        c1.setProperty("key2", "value3");
        String path = "/col1/col2/col30 space45";
        String actualPath = null;
        try {
            actualPath = registry.put(path, c1);
        } catch(Exception e) {
            fail("Couldn't put collection /col1/col2/col3 space");
        }
        Resource c1_actual = null;
        try {
            c1_actual = registry.get(actualPath);
        } catch(Exception e) {
            fail("Couldn't get content of path /col1/col2/col30 space45");
        }
        assertEquals(c1_actual.getLastUpdaterUserName(), "admin", "LastUpdatedUser is not Equal");
        assertEquals(c1_actual.getPath(), path, "Can not get Collection path");
        assertEquals(c1_actual.getParentPath(), "/col1/col2", "Can not get Collection parent path");
        assertEquals(c1_actual.getDescription(), c1.getDescription(), "Resource description is not equal");
        assertEquals(c1_actual.getAuthorUserName(), "admin", "Authour is not equal");
        assertEquals(c1_actual.getProperty("key1"), c1.getProperty("key1"), "Resource properties are not equal");
        assertEquals(c1_actual.getProperty("key2"), c1.getProperty("key2"), "Resource properties are not equal");
    }

    private void addResourceFromURL() throws Exception {
        String path = "/d25/d21/d23/d24/r1";
        String url = "http://shortwaveapp.com/waves.txt";
        Resource r1 = registry.newResource();
        r1.setDescription("this is a file imported from url");
        r1.setMediaType("java");
        r1.setProperty("key1", "value5");
        r1.setProperty("key2", "value3");
        try {
            registry.importResource(path, url, r1);
        } catch(Exception e) {
            fail("Couldn't import content to path:" + path);
        }
        Resource r1_actual = registry.newResource();
        try {
            r1_actual = registry.get(path);
        } catch(Exception e) {
            fail("Couldn't get content from path" + path);
        }
        boolean content = true;
        if(r1_actual == null) {
            content = false;
        }
        assertTrue(content, "Imported file is empty");
        assertEquals(r1_actual.getLastUpdaterUserName(), "admin", "LastUpdatedUser is not Equal");
        assertEquals(r1_actual.getPath(), path, "Can not get Resource path");
        assertEquals(r1_actual.getParentPath(), "/d25/d21/d23/d24", "Can not get Resource parent path");
        assertEquals(r1_actual.getAuthorUserName(), "admin", "Authour is not equal");
    }

    private void renameResource() throws Exception {
        Resource r1 = registry.newResource();
        String content = "this is my content";
        r1.setContent(content.getBytes());
        r1.setDescription("This is r1 file description");
        String path = "/d30/d31/r1";
        try {
            registry.put(path, r1);
        } catch(Exception e) {
            fail("Couldn't put content to path" + path);
        }
        Resource r1_actual = registry.newResource();
        try {
            r1_actual = registry.get(path);
        } catch(Exception e) {
            fail("Couldn't get content from path" + path);
        }
        assertEquals(new String((byte[]) r1_actual.getContent()), new String((byte[]) r1.getContent()), "Content is not equal.");

        /*rename the resource*/
        String new_path = "/d33/d34/r1";
        try {
            registry.rename(path, new_path);
        } catch(Exception e) {
            fail("Can not rename the path from" + path + "to" + new_path);
        }
        Resource r2_actual = registry.newResource();
        try {
            r2_actual = registry.get(new_path);
        } catch(Exception e) {
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

    private void deleteAndUpdateResource() throws Exception {
        Resource r1 = registry.newResource();
        String content = "this is my content";
        r1.setContent(content.getBytes());
        r1.setDescription("This is r1 file description");
        String path = "/d40/d43/r1";
        try {
            registry.put(path, r1);
        } catch(Exception e) {
            fail("Couldn't put content to path" + path);
        }
        Resource r1_actual = registry.newResource();
        try {
            r1_actual = registry.get(path);
        } catch(Exception e) {
            fail("Couldn't get content from path" + path);
        }
        assertEquals(new String((byte[]) r1_actual.getContent()), new String((byte[]) r1.getContent()), "Content is not equal.");
        boolean deleted = true;
        try {
            registry.delete(path);
        } catch(Exception e) {
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
        } catch(Exception e) {
            fail("Couldn't put content to path" + path_new);
        }
        Resource r1_actual2 = registry.newResource();
        try {
            r1_actual2 = registry.get(path_new);
        } catch(Exception e) {
            fail("Couldn't get content from path" + path_new);
        }
        assertEquals(new String((byte[]) r1_actual2.getContent()), new String((byte[]) r2.getContent()), "Content is not equal.");
    }

    private void resourceMultipleProperties() throws Exception {
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
            assertEquals(new String((byte[]) r1_actual2.getContent()), new String((byte[]) r1.getContent()), "Content is not equal.");
            List propertyValues = r1_actual2.getPropertyValues("key1");
            Object[] valueName = propertyValues.toArray();
            List propertyValuesKey2 = r1_actual2.getPropertyValues("key2");
            Object[] valueNameKey2 = propertyValuesKey2.toArray();
            assertTrue(containsString(valueName, "value1"), "value1 is not associated with key1");
            assertTrue(containsString(valueName, "value2"), "value2 is not associated with key1");
            assertTrue(containsString(valueName, "value3"), "value3 is not associated with key1");
            assertTrue(containsString(valueNameKey2, "value1"), "value1 is not associated with key2");
            assertTrue(containsString(valueNameKey2, "value2"), "value2 is not associated with key2");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void collectionMultipleProperties() throws Exception {
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
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    //  The below method is used in the above methods.
    private boolean containsString(Object[] array, String value) {
        boolean found = false;
        for(Object anArray : array) {
            String s = anArray.toString();
            if(s.startsWith(value)) {
                found = true;
                break;
            }
        }
        return found;
    }

    private void testLastModifiedDateChange() throws Exception {
        Resource resource = registry.newResource();
        String content = "Hello Out there!";
        resource.setContent(content);
        String resourcePath = "/lastModTest2";
        registry.put(resourcePath, resource);
        Resource getResource = registry.get(resourcePath);
        Date lastMod = getResource.getLastModified();
        //wait for some times
        System.out.println("Sleeping for 5000 milliseconds...");
        Thread.sleep(5000);
        System.out.println("Woke-up after 5000 milliseconds..");
        getResource = registry.get(resourcePath);
        assertEquals(getResource.getLastModified(), lastMod, "Invalid lastModified time.");
    }

    @AfterClass
    public void cleanup() throws RegistryException {
        registry.delete("/d1");
        registry.delete("/c11");
        registry.delete("/c1");
        registry.delete("/c20");
        registry.delete("/d11");
        registry.delete("/col1");
        registry.delete("/d25");
        registry.delete("/d30");
        registry.delete("/d33");
        registry.delete("/d40");
        registry.delete("/lastModTest2");
        registry.delete("/m15");
        registry.delete("/m11");
    }
}
