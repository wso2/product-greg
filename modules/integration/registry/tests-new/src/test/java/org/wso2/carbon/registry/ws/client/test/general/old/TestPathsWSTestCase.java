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

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * A test case which tests registry paths
 */
public class TestPathsWSTestCase {

    private WSRegistryServiceClient registry;

    @BeforeClass(groups = {"wso2.greg"})
    public void initTest() throws RegistryException, AxisFault {
        int userId = 0;
        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        registry = registryProviderUtil.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);
    }

    @Test(groups = {"wso2.greg"})
    public void getOnPaths() throws Exception {

        Resource r1 = registry.newResource();
        registry.put("/testkrishantha/paths/r1", r1);

        assertTrue(registry.resourceExists("/testkrishantha"), "Resource not found.");
        assertTrue(registry.resourceExists("/testkrishantha/"), "Resource not found.");
        assertTrue(registry.resourceExists("/testkrishantha/paths/r1"), "Resource not found.");
        assertTrue(registry.resourceExists("/testkrishantha/paths/r1/"), "Resource not found.");

        assertNotNull(registry.get("/testkrishantha"));
        assertNotNull(registry.get("/testkrishantha/"));
        assertNotNull(registry.get("/testkrishantha/paths/r1"));
        assertNotNull(registry.get("/testkrishantha/paths/r1/"));

    }

    @Test(groups = {"wso2.greg"})
    public void putOnPaths() throws Exception {

        Resource r1 = registry.newResource();
        r1.setContent("some content");
        registry.put("/testkrishantha1/paths2/r1", r1);

        Resource r2 = registry.newResource();
        r2.setContent("another content");
        registry.put("/testkrishantha1/paths2/r2", r2);

        Collection c1 = registry.newCollection();
        registry.put("/testkrishantha1/paths2/c1", c1);

        Collection c2 = registry.newCollection();
        registry.put("/testkrishantha1/paths2/c2", c2);

        assertTrue(registry.resourceExists("/testkrishantha1/paths2/r1"), "Resource not found.");
        assertTrue(registry.resourceExists("/testkrishantha1/paths2/r2"), "Resource not found.");
        assertTrue(registry.resourceExists("/testkrishantha1/paths2/c1"), "Resource not found.");
        assertTrue(registry.resourceExists("/testkrishantha1/paths2/c2"), "Resource not found.");
    }


    @AfterClass
    public void cleanup() throws RegistryException {
        registry.delete("/testkrishantha");
        registry.delete("/testkrishantha1");

    }
}
