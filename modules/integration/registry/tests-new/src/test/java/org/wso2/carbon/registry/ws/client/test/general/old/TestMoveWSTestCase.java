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
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * A test case which tests registry move operation
 */
public class TestMoveWSTestCase {

    private WSRegistryServiceClient registry;

    @BeforeClass(groups = {"wso2.greg"})
    public void initTest() throws RegistryException, AxisFault {
        int userId = 0;
        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        registry = registryProviderUtil.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);
    }

    @Test(groups = {"wso2.greg"})
    public void resourceMoveFromRoot() throws Exception {

        Resource r1 = registry.newResource();
        r1.setProperty("test", "move");
        r1.setContent("c");
        registry.put("/move1", r1);

        Collection c1 = registry.newCollection();
        registry.put("/test/move", c1);

        registry.move("/move1", "/test/move/move1");

        Resource newR1 = registry.get("/test/move/move1");
        assertEquals(newR1.getProperty("test"), "move",
                     "Moved resource should have a property named 'test' with value 'move'.");

        boolean failed = false;
        try {
            registry.get("/move1");
        } catch (Exception e) {
            failed = true;
        }
        assertTrue(failed, "Moved resource should not be accessible from the old path.");
    }

    @Test(groups = {"wso2.greg"})
    public void resourceMoveToRoot() throws Exception {

        Resource r1 = registry.newResource();
        r1.setProperty("test", "move");
        r1.setContent("c");
        registry.put("/test/move/move2", r1);

        registry.move("/test/move/move2", "/move2");

        Resource newR1 = registry.get("/move2");
        assertEquals(newR1.getProperty("test"), "move",
                     "Moved resource should have a property named 'test' with value 'move'.");

        boolean failed = false;
        try {
            registry.get("/test/move/move2");
        } catch (Exception e) {
            failed = true;
        }
        assertTrue(failed, "Moved resource should not be accessible from the old path.");
    }

    @Test(groups = {"wso2.greg"})
    public void generalResourceMove() throws Exception {

        Resource r1 = registry.newResource();
        r1.setProperty("test", "move");
        r1.setContent("c");
        registry.put("/test/c1/move/move3", r1);

        Collection c2 = registry.newCollection();
        registry.put("/test/c2/move", c2);

        registry.move("/test/c1/move/move3", "/test/c2/move/move3");

        Resource newR1 = registry.get("/test/c2/move/move3");
        assertEquals(newR1.getProperty("test"), "move",
                     "Moved resource should have a property named 'test' with value 'move'.");

        boolean failed = false;
        try {
            registry.get("/test/c1/move/move3");
        } catch (Exception e) {
            failed = true;
        }
        assertTrue(failed, "Moved resource should not be accessible from the old path.");
    }

    @Test(groups = {"wso2.greg"})
    public void generalCollectionMove() throws Exception {

        Resource r1 = registry.newResource();
        r1.setProperty("test", "move");
        r1.setContent("c");
        registry.put("/test/c1/move5/move/dummy", r1);

        Collection c2 = registry.newCollection();
        registry.put("/test/c3", c2);

        registry.move("/test/c1/move5", "/test/c3/move5");

        Resource newR1 = registry.get("/test/c3/move5/move/dummy");
        assertEquals(newR1.getProperty("test"), "move",
                     "Moved resource should have a property named 'test' with value 'move'.");

        boolean failed = false;
        try {
            registry.get("/test/c1/move5/move/dummy");
        } catch (Exception e) {
            failed = true;
        }
        assertTrue(failed, "Moved resource should not be accessible from the old path.");
    }


    @AfterClass
    public void cleanup() throws RegistryException {
        registry.delete("/test");
        registry.delete("/move2");

    }

}
