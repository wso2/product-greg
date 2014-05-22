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
package org.wso2.carbon.registry.jira.issues.test;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;
import java.rmi.RemoteException;

import static junit.framework.Assert.assertEquals;

public class Carbon8421TestCase extends GREGIntegrationBaseTest {


    private WSRegistryServiceClient registry;
    private ResourceAdminServiceClient resourceAdminClient;
    private String RESOURCE_NAME_ROOT = "/getOldVersion";


    @BeforeClass(alwaysRun = true)
    public void initializeTests() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_USER);
        String sessionCookie = getSessionCookie();


        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        registry = registryProviderUtil.getWSRegistry(automationContext);
        resourceAdminClient =
                new ResourceAdminServiceClient(backendURL, sessionCookie);


    }

    @Test(groups = {"wso2.greg"}, description = "Create new resource at root level and get versioned old data")
    public void testVersionedContent() throws RegistryException, InterruptedException,
                                              ResourceAdminServiceExceptionException,
                                              RemoteException {
        Resource r1 = registry.newResource();
        String content = "this is my content1";
        r1.setContent(content.getBytes());
        r1.setDescription("This is a test description");
        r1.setProperty("name1", "value1");
        registry.put(RESOURCE_NAME_ROOT, r1);
        resourceAdminClient.createVersion(RESOURCE_NAME_ROOT);
        //Edit current content
        String editedContent = "this is edited content";

        r1.setContent(editedContent.getBytes());
        registry.put(RESOURCE_NAME_ROOT, r1);

        Resource r2 = registry.get(resourceAdminClient.getVersionPaths(RESOURCE_NAME_ROOT)[0].getCompleteVersionPath());
        assertEquals("this is my content1", new String((byte[]) r2.getContent()));
        Resource r3 = registry.get(RESOURCE_NAME_ROOT);
        assertEquals("this is edited content", new String((byte[]) r3.getContent()));
    }


    @AfterClass
    public void cleanup() throws ResourceAdminServiceExceptionException, RemoteException {
        resourceAdminClient.deleteResource(RESOURCE_NAME_ROOT);
        registry = null;
        resourceAdminClient = null;
    }

}
