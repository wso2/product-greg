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
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * A test case which tests registry rename operation
 */
public class RenameWSTestCase {

    private WSRegistryServiceClient registry;

    @BeforeClass(groups = {"wso2.greg"})
    public void initTest() throws RegistryException, AxisFault {
        int userId = 0;
        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        registry = registryProviderUtil.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);

    }

    @Test(groups = {"wso2.greg"})
    public void rootLevelResourceRename() throws Exception {

        Resource r1 = registry.newResource();
        r1.setProperty("test", "rename");
        r1.setContent("some text");
        registry.put("/rename2", r1);

        registry.rename("/rename2", "/rename4");

        boolean failed = false;
        try {
            registry.get("/rename2");
        } catch (Exception e) {
            failed = true;
        }
        assertTrue(failed, "Resource should not be accessible from the old path after renaming.");

        Resource newR1 = registry.get("/rename4");
        assertEquals(newR1.getProperty("test"), "rename",
                     "Resource should contain a property with name test and value rename.");
    }

    @Test(groups = {"wso2.greg"})
    public void generalResourceRename() throws Exception {

        Resource r1 = registry.newResource();
        r1.setProperty("test", "rename");
        r1.setContent("some text");
        registry.put("/tests/rename1", r1);

        registry.rename("/tests/rename1", "rename2");

        boolean failed = false;
        try {
            registry.get("/tests/rename1");
        } catch (Exception e) {
            failed = true;
        }
        assertTrue(failed, "Resource should not be accessible from the old path after renaming.");

        Resource newR1 = registry.get("/tests/rename2");
        assertEquals(newR1.getProperty("test"), "rename",
                     "Resource should contain a property with name test and value rename.");
    }

    @Test(groups = {"wso2.greg"})
    public void rootLevelCollectionRename() throws Exception {

        Resource r1 = registry.newResource();
        r1.setProperty("test", "rename");
        r1.setContent("some text");
        registry.put("/rename34k/c1/dummy", r1);

        registry.rename("/rename34k", "/rename44k");

        boolean failed = false;
        try {
            registry.get("/rename34k/c1/dummy");
        } catch (Exception e) {
            failed = true;
        }
        assertTrue(failed, "Resource should not be " +
                           "accessible from the old path after renaming the parent.");

        Resource newR1 = registry.get("/rename44k/c1/dummy");
        assertEquals(newR1.getProperty("test"), "rename",
                     "Resource should contain a property with name test and value rename.");
    }

    @Test(groups = {"wso2.greg"})
    public void generalCollectionRename() throws Exception {

        Resource r1 = registry.newResource();
        r1.setProperty("test", "rename");
        r1.setContent("some text");
        registry.put("/c2/rename3/c1/dummy", r1);

        registry.rename("/c2/rename3", "rename4");

        boolean failed = false;
        try {
            registry.get("/c2/rename3/c1/dummy");
        } catch (Exception e) {
            failed = true;
        }
        assertTrue(failed, "Resource should not be " +
                           "accessible from the old path after renaming the parent.");

        Resource newR1 = registry.get("/c2/rename4/c1/dummy");
        assertEquals(newR1.getProperty("test"), "rename",
                     "Resource should contain a property with name test and value rename.");
    }


    @AfterClass
    public void cleanup() throws RegistryException {
        registry.delete("/rename4");
        registry.delete("/tests");
        registry.delete("/rename44k");
        registry.delete("/c2");


    }
}
