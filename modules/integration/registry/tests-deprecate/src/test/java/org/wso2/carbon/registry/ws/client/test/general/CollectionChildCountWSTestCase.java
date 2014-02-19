/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import static org.testng.Assert.*;

/**
 * A test case which tests registry collection child count
 */
public class CollectionChildCountWSTestCase extends TestSetup {

    private String path = "/myorg/";

    @BeforeClass(groups = {"wso2.greg"})
    public void initChildCountTest() throws RegistryException {
        super.init();
        Resource resource1 = registry.newResource();
        resource1.setContent("my text1");
        Resource resource2 = registry.newResource();
        resource2.setContent("my text2");
        registry.put("/myorg/mytext1.txt", resource1);
        registry.put("/myorg/mytext2.txt", resource2);
    }

    @Test(groups = {"wso2.greg"})
    public void getChildCountForCollection() throws RegistryException {
        Resource resource = registry.get(path);
        assertTrue((resource instanceof Collection), "resource is not a collection");
        Collection collection = (Collection) resource;
        assertEquals(collection.getChildCount(), 2, "Invalid child count");
    }

    @Test(groups = {"wso2.greg"})
    public void doPagedGet() throws RegistryException {
        String path = "/_system";
        Resource resource = registry.get(path, 1, 1);
        assertTrue((resource instanceof Collection), "resource is not a collection");
        Collection collection = (Collection) resource;
        assertEquals(collection.getChildCount(), 1, "Invalid child count");

    }
}
