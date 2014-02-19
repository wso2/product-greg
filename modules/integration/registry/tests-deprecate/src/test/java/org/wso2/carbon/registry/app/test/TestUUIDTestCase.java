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

import java.util.UUID;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

/**
 * A test case which tests registry copy operations
 */

public class TestUUIDTestCase {
    public RemoteRegistry registry;

    @BeforeClass(groups = {"wso2.greg"})
    public void init() {
        InitializeAPI initializeAPI = new InitializeAPI();
        registry = initializeAPI.getRegistry(FrameworkSettings.CARBON_HOME, FrameworkSettings.HTTPS_PORT, FrameworkSettings.HTTP_PORT);
    }

    @Test(groups = {"wso2.greg"})
    public void ResourceUUIDTest() throws RegistryException {
        String resourcePath = "/test1/uuid/c1/uuid1";
        String uuid = UUID.randomUUID().toString();

        Resource r1 = registry.newResource();
        r1.setContent("c");
        r1.setUUID(uuid);
        registry.put(resourcePath, r1);

        Resource getResource = registry.get(resourcePath);

        assertEquals(getResource.getUUID(),uuid,"Incorrect UUID returned");

    }
    @Test(groups = {"wso2.greg"})
    public void ResourceUUIDUpdateTest() throws RegistryException {
        String resourcePath = "/test1/uuid/c1/uuid2";
        String uuid = UUID.randomUUID().toString();

        Resource r1 = registry.newResource();
        r1.setContent("c");
        registry.put(resourcePath, r1);

        Resource getResource = registry.get(resourcePath);
        assertNotNull(getResource.getUUID(),"UUID was null");

        getResource.setUUID(uuid);
        registry.put(resourcePath,getResource);

        Resource anotherResource = registry.get(resourcePath);
        assertEquals(anotherResource.getUUID(),uuid,"Incorrect UUID returned");

    }

    @Test(groups = {"wso2.greg"})
    public void CollectionUUIDTest() throws RegistryException {
        String collectionPath = "/test1/uuid/c2";
        String uuid = UUID.randomUUID().toString();

        Collection c1 = registry.newCollection();
        c1.setContent("c");
        c1.setUUID(uuid);
        registry.put(collectionPath, c1);

        Collection getResource = (Collection) registry.get(collectionPath);

        assertEquals(getResource.getUUID(),uuid,"Incorrect UUID returned");

    }
    @Test(groups = {"wso2.greg"})
    public void CollectionUUIDUpdateTest() throws RegistryException {
        String collectionPath = "/test1/uuid/c3";
        String uuid = UUID.randomUUID().toString();

        Collection c1 = registry.newCollection();
        c1.setContent("c");
        registry.put(collectionPath, c1);

        Collection getResource = (Collection) registry.get(collectionPath);
        assertNotNull(getResource.getUUID(),"UUID was null");

//        Adding a new UUID and doing an update
        getResource.setUUID(uuid);
        registry.put(collectionPath,getResource);

//        Checking whether it persisted
        Collection anotherOne = (Collection) registry.get(collectionPath);
        assertEquals(anotherOne.getUUID(),uuid,"Incorrect UUID returned");

    }
}
