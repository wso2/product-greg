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
import org.wso2.carbon.integration.core.TestTemplate;
import org.wso2.carbon.registry.app.RemoteRegistry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import static org.testng.Assert.*;

/**
 * A test case which tests continuous operations
 */
public class ContinuousOperationsTestCase {
    public RemoteRegistry registry;

    @BeforeClass(groups = {"wso2.greg"})
    public void init() {
        InitializeAPI initializeAPI = new InitializeAPI();
        registry = initializeAPI.getRegistry(FrameworkSettings.CARBON_HOME, FrameworkSettings.HTTPS_PORT, FrameworkSettings.HTTP_PORT);
    }

    @Test(groups = {"wso2.greg"})
    public void ContinousDelete() throws RegistryException, InterruptedException {
        int iterations = 100;

        for (int i = 0; i < iterations; i++) {

            Resource res1 = registry.newResource();
            byte[] r1content = "R2 content".getBytes();
            res1.setContent(r1content);
            String path = "/con-delete/test/" + i + 1;

            registry.put(path, res1);

            Resource resource1 = registry.get(path);

            assertEquals(new String((byte[]) resource1.getContent()),
                    new String((byte[]) res1.getContent()), "File content is not matching");

            registry.delete(path);

            boolean value = false;

            if (registry.resourceExists(path)) {
                value = true;
            }

            assertFalse(value, "Resource found at the path");

            res1.discard();
            resource1.discard();
            Thread.sleep(100);
        }
    }

    @Test(groups = {"wso2.greg"})
    public void ContinuousUpdate() throws RegistryException, InterruptedException {

        int iterations = 100;

        for (int i = 0; i < iterations; i++) {

            Resource res1 = registry.newResource();
            byte[] r1content = "R2 content".getBytes();
            res1.setContent(r1content);
            String path = "/con-delete/test-update/" + i + 1;

            registry.put(path, res1);

            Resource resource1 = registry.get(path);

            assertEquals(new String((byte[]) resource1.getContent()),
                    new String((byte[]) res1.getContent()), "File content is not matching");

            Resource resource = new ResourceImpl();
            byte[] r1content1 = "R2 content updated".getBytes();
            resource.setContent(r1content1);
            resource.setProperty("abc", "abc");

            registry.put(path, resource);

            Resource resource2 = registry.get(path);

            assertEquals(new String((byte[]) resource2.getContent()),
                    new String((byte[]) resource.getContent()), "File content is not matching");

            resource.discard();
            res1.discard();
            resource1.discard();
            resource2.discard();
            Thread.sleep(100);
        }
    }
}
