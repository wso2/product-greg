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

package org.wso2.carbon.registry.ws.client.test.general;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.RegistryUtils;

import static org.testng.Assert.*;

/**
 * A test case which tests registry versioning operation
 */
public class VersionHandlingWSTestCase extends TestSetup {

    @BeforeClass(groups = {"wso2.greg"})
    public void initTest() {
        super.init();
    }

    @Test(groups = {"wso2.greg.ws"})
    public void createVersions() throws Exception {

        Resource r1 = registry.newResource();
        r1.setContent("some content");
        clearResource("/_version/r1");
        registry.put("/_version/r1", r1);

        registry.createVersion("/_version/r1");

        String[] r1Versions = registry.getVersions("/_version/r1");

        assertEquals(r1Versions.length, 1, "/_version/r1 should have 1 version.");

        Resource r1v2 = registry.get("/_version/r1");
        r1v2.setContent("another content");
        registry.put("/_version/r1", r1v2);

        registry.createVersion("/_version/r1");

        r1Versions = registry.getVersions("/_version/r1");
        assertEquals(r1Versions.length, 2, "/_version/r1 should have 2 version.");
        clearResource("/_version/r1");

    }

    @Test(groups = {"wso2.greg.ws"})
    public void resourceContentVersioning() throws Exception {

        Resource r1 = registry.newResource();
        r1.setContent("content 1".getBytes());
        clearResource("/_v2/r1");
        registry.put("/_v2/r1", r1);

        registry.createVersion("/_v2/r1");

        Resource r12 = registry.get("/_v2/r1");
        r12.setContent("content 2".getBytes());
        registry.put("/_v2/r1", r12);

        registry.createVersion("/_v2/r1");

        String[] r1Versions = registry.getVersions("/_v2/r1");

        Resource r1vv1 = registry.get(r1Versions[1]);

        assertEquals(new String((byte[]) r1vv1.getContent()), "content 1",
                "r1's first version's content should be 'content 1'");

        Resource r1vv2 = registry.get(r1Versions[0]);

        assertEquals(new String((byte[]) r1vv2.getContent()), "content 2",
                "r1's second version's content should be 'content 2'");
    }

    @Test(groups = {"wso2.greg.ws"})
    public void resourcePropertyVersioning() throws Exception {

        Resource r1 = registry.newResource();
        r1.setContent("content 1");
        r1.addProperty("p1", "v1");
        clearResource("/_v4/r1");
        registry.put("/_v4/r1", r1);

        registry.createVersion("/_v4/r1");

        Resource r1v2 = registry.get("/_v4/r1");
        r1v2.addProperty("p2", "v2");
        registry.put("/_v4/r1", r1v2);

        registry.createVersion("/_v4/r1");;

        String[] r1Versions = registry.getVersions("/_v4/r1");

        Resource r1vv1 = registry.get(r1Versions[1]);

        assertEquals(r1vv1.getProperty("p1"), "v1",
                "r1's first version should contain a property p1 with value v1");

        Resource r1vv2 = registry.get(r1Versions[0]);

        assertEquals(r1vv2.getProperty("p1"), "v1",
                "r1's second version should contain a property p1 with value v1");

        assertEquals(r1vv2.getProperty("p2"), "v2",
                "r1's second version should contain a property p2 with value v2");
    }

    @Test(groups = {"wso2.greg.ws"})
    public void simpleCollectionVersioning() throws Exception {

        Collection c1 = registry.newCollection();
        clearResource("/_v3/c1");
        registry.put("/_v3/c1", c1);

        registry.createVersion("/_v3/c1");

        Collection c2 = registry.newCollection();
        registry.put("/_v3/c1/c2", c2);

        registry.createVersion("/_v3/c1");

        Collection c3 = registry.newCollection();
        registry.put("/_v3/c1/c3", c3);

        registry.createVersion("/_v3/c1");

        Collection c4 = registry.newCollection();
        registry.put("/_v3/c1/c2/c4", c4);

        registry.createVersion("/_v3/c1");

        Collection c5 = registry.newCollection();
        registry.put("/_v3/c1/c2/c5", c5);

        registry.createVersion("/_v3/c1");

        String[] c1Versions = registry.getVersions("/_v3/c1");

        registry.get(c1Versions[0]);
        registry.get(c1Versions[1]);
        registry.get(c1Versions[2]);
    }

    @Test(groups = {"wso2.greg.ws"})
    public void resourceRestore() throws Exception {

        Resource r1 = registry.newResource();
        r1.setContent("content 1".getBytes());
        clearResource("/_test/v10/r1");
        registry.put("/_test/v10/r1", r1);

        registry.createVersion("/_test/v10/r1");

        Resource r1e1 = registry.get("/_test/v10/r1");
        r1e1.setContent("content 2".getBytes());
        registry.put("/_test/v10/r1", r1e1);

        registry.createVersion("/_test/v10/r1");

        String[] r1Versions = registry.getVersions("/_test/v10/r1");
        registry.restoreVersion(r1Versions[1]);

        Resource r1r1 = registry.get("/_test/v10/r1");

        assertEquals(new String((byte[]) r1r1.getContent()), "content 1",
                "Restored resource should have content 'content 1'");
    }

    @Test(groups = {"wso2.greg.ws"})
    public void simpleCollectionRestore() throws Exception {

        Collection c1 = registry.newCollection();
        clearResource("/_test/v11/c1");
        registry.put("/_test/v11/c1", c1);

        registry.createVersion("/_test/v11/c1");

        Resource r1 = registry.newResource();
        r1.setContent("r1c1");
        registry.put("/_test/v11/c1/r1", r1);

        registry.createVersion("/_test/v11/c1");

        Resource r2 = registry.newResource();
        r2.setContent("r1c1");
        registry.put("/_test/v11/c1/r2", r2);

        registry.createVersion("/_test/v11/c1");

        String[] c1Versions = registry.getVersions("/_test/v11/c1");
        assertEquals(c1Versions.length, 3, "/_test/v11/c1 should have 3 versions.");

//        TODO: seems to be a bug in WS registry
        registry.restoreVersion(c1Versions[c1Versions.length -1]);
        Collection c1r1 = (Collection) registry.get("/_test/v11/c1");
        assertEquals(c1r1.getChildren().length, 0, "version 1 of c1 should not have any children");

        try {
            registry.get("/_test/v11/c1/r1");
            fail("Version 1 of c1 should not have child r1");
        } catch (Exception e) {
        }

        try {
            registry.get("/_test/v11/c1/r2");
            fail("Version 1 of c1 should not have child r2");
        } catch (Exception e) {
        }

        registry.restoreVersion(c1Versions[1]);
        Collection c1r2 = (Collection) registry.get("/_test/v11/c1");
        assertEquals(c1r2.getChildren().length, 1, "version 2 of c1 should have 1 child");

        try {
            registry.get("/_test/v11/c1/r1");
        } catch (Exception e) {
            fail("Version 2 of c1 should have child r1");
        }

        try {
            registry.get("/_test/v11/c1/r2");
            fail("Version 2 of c1 should not have child r2");
        } catch (Exception e) {

        }

        registry.restoreVersion(c1Versions[0]);
        Collection c1r3 = (Collection) registry.get("/_test/v11/c1");
        assertEquals(c1r3.getChildren().length, 2, "version 3 of c1 should have 2 children");

        try {
            registry.get("/_test/v11/c1/r1");
        } catch (Exception e) {
            fail("Version 3 of c1 should have child r1");
        }

        try {
            registry.get("/_test/v11/c1/r2");
        } catch (Exception e) {
            fail("Version 3 of c1 should have child r2");
        }
        clearResource("/_test/v11/c1");

    }

    @Test(groups = {"wso2.greg.ws"})
    public void advancedCollectionRestore() throws Exception {

        Collection c1 = registry.newCollection();
        clearResource("/_test/v12/c1");
        registry.put("/_test/v12/c1", c1);

        registry.createVersion("/_test/v12/c1");

        Resource r1 = registry.newResource();
        r1.setContent("r1c1".getBytes());
        registry.put("/_test/v12/c1/c11/r1", r1);

        registry.createVersion("/_test/v12/c1");

        Collection c2 = registry.newCollection();
        registry.put("/_test/v12/c1/c11/c2", c2);

        registry.createVersion("/_test/v12/c1");

        Resource r1e1 = registry.get("/_test/v12/c1/c11/r1");
        r1e1.setContent("r1c2".getBytes());
        registry.put("/_test/v12/c1/c11/r1", r1e1);

        registry.createVersion("/_test/v12/c1");

        String[] c1Versions = registry.getVersions("/_test/v12/c1");
        assertEquals(c1Versions.length, 4, "c1 should have 4 versions");

        registry.restoreVersion(c1Versions[3]);

        try {
            registry.get("/_test/v12/c1/c11");
            fail("Version 1 of c1 should not have child c11");
        } catch (Exception e) {
        }

        registry.restoreVersion(c1Versions[2]);

        try {
            registry.get("/_test/v12/c1/c11");
        } catch (Exception e) {
            fail("Version 2 of c1 should have child c11");
        }

        try {
            registry.get("/_test/v12/c1/c11/r1");
        } catch (Exception e) {
            fail("Version 2 of c1 should have child c11/r1");
        }

        registry.restoreVersion(c1Versions[1]);

        Resource r1e2 = null;
        try {
            r1e2 = registry.get("/_test/v12/c1/c11/r1");
        } catch (Exception e) {
            fail("Version 2 of c1 should have child c11/r1");
        }

        try {
            registry.get("/_test/v12/c1/c11/c2");
        } catch (Exception e) {
            fail("Version 2 of c1 should have child c11/c2");
        }

        String r1e2Content = new String((byte[]) r1e2.getContent());
        assertEquals(r1e2Content, "r1c1", "c11/r1 content should be 'r1c1");

        registry.restoreVersion(c1Versions[0]);

        Resource r1e3 = registry.get("/_test/v12/c1/c11/r1");
        String r1e3Content = new String((byte[]) r1e3.getContent());
        assertEquals(r1e3Content, "r1c2", "c11/r1 content should be 'r1c2");
        clearResource("/_test/v12/c1");

    }

    @Test(groups = {"wso2.greg.ws"})
    public void permaLinksForResources() throws Exception {

        Resource r1 = registry.newResource();
        r1.setContent("r1c1");
        registry.put("/_test/v13/r1", r1);

        registry.createVersion("/_test/v13/r1");

        String[] r1Versions = registry.getVersions("/_test/v13/r1");

        Resource r1e1 = registry.get(r1Versions[0]);
        assertEquals(r1e1.getPermanentPath(), r1Versions[0], "Permalink incorrect");

        r1e1.setContent("r1c2");
        registry.put("/_test/v13/r1", r1e1);

        registry.createVersion("/_test/v13/r1");

        r1Versions = registry.getVersions("/_test/v13/r1");

        Resource r1e2 = registry.get(r1Versions[0]);
        assertEquals(r1e2.getPermanentPath(), r1Versions[0], "Permalink incorrect");

        registry.restoreVersion(r1Versions[1]);

        Resource r1e3 = registry.get(r1Versions[1]);
        assertEquals(r1e3.getPermanentPath(), r1Versions[1], "Permalink incorrect");
    }

    @Test(groups = {"wso2.greg.ws"})
    public void permaLinksForCollections() throws Exception {

        Collection c1 = registry.newCollection();
        registry.put("/_test/v14/c1", c1);

        registry.createVersion("/_test/v14/c1");

        String[] c1Versions = registry.getVersions("/_test/v14/c1");
        Resource c1e1 = registry.get(c1Versions[0]);
        assertEquals(c1e1.getPermanentPath(), c1Versions[0], "Permalink incorrect");

        Resource r1 = registry.newResource();
        r1.setContent("r1c1");
        registry.put("/_test/v14/c1/r1", r1);

        registry.createVersion("/_test/v14/c1");

        c1Versions = registry.getVersions("/_test/v14/c1");
        Resource c1e2 = registry.get(c1Versions[0]);
        assertEquals(c1e2.getPermanentPath(), c1Versions[0], "Permalink incorrect");

        registry.restoreVersion(c1Versions[1]);

        Resource c1e3 = registry.get(c1Versions[1]);
        assertEquals(c1e3.getPermanentPath(), c1Versions[1], "Permalink incorrect");
    }

    @Test(groups = {"wso2.greg.ws"})
    public void rootLevelVersioning() throws Exception {

        Resource r1 = registry.newResource();
        r1.setContent("r1c1");
        registry.put("/_vtr1", r1);

        registry.createVersion("/");

        Collection c2 = registry.newCollection();
        registry.put("/_vtc2", c2);

        registry.createVersion("/");

        String[] rootVersions = registry.getVersions("/");

        Collection rootv0 = (Collection) registry.get(rootVersions[0]);

//        TODO: seems to be a bug in WS registry
        String[] rootv0Children = rootv0.getChildren();
        assertTrue(RegistryUtils.containsAsSubString("/_vtr1", rootv0Children),
                "Root should have child vtr1");
        assertTrue(RegistryUtils.containsAsSubString("/_vtc2", rootv0Children),
                "Root should have child vtc2");

        Collection rootv1 = (Collection) registry.get(rootVersions[1]);
        String[] rootv1Children = (String[]) rootv1.getContent();
        assertTrue(RegistryUtils.containsAsSubString("/_vtr1", rootv1Children),
                "Root should have child vtr1");
        assertFalse(RegistryUtils.containsAsSubString("/_vtc2", rootv1Children),
                "Root should not have child vtc2");
    }

    @Test(groups = {"wso2.greg.ws"})
    public void testGetContentOfOldData() throws Exception {
        Resource resource = registry.newResource();
        String content = "Hello Out there!";
        resource.setContent(content);

        String resourcePath = "/_abc2";
        registry.put(resourcePath, resource);

        registry.createVersion("/_abc2");

        Resource getResource = registry.get("/_abc2");
        String contentRetrieved = new String((byte[]) getResource.getContent());
        assertEquals(contentRetrieved, content,
                "Content does not match");

        resource = registry.newResource();
        String newContent = "new content";
        resource.setContent(newContent);

        registry.put(resourcePath, resource);

        registry.createVersion("/_abc2");

        String[] versions = registry.getVersions(resourcePath);
        getResource = registry.get(versions[versions.length - 1]);

        contentRetrieved = new String((byte[]) getResource.getContent());

        assertEquals(contentRetrieved, content,
                "Content does not match");

    }

    /**
     * this method will clean the given resource if it already exists
     */
    private void clearResource(String path) throws RegistryException {
        if (registry.resourceExists(path)) {
            registry.delete(path);
        }

    }
}
