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
import org.wso2.carbon.automation.engine.FrameworkConstants;
import org.wso2.carbon.automation.engine.configurations.UrlGenerationUtil;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.registry.app.RemoteRegistry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import javax.activation.DataHandler;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;

public class RemoteRegistryTestCase extends GREGIntegrationBaseTest {

    private static final String PATH = "/c4/";
    private static final String RES_NAME = "testResource.txt";
    private static final String RES_DESC = "A test resource";

    private ResourceAdminServiceClient resourceAdminClient;

    private RemoteRegistry remoteRegistry;
    private String userNameWithoutDomain;

    @BeforeClass
    public void initialize() throws Exception{

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String session = getSessionCookie();

        //TODO- What about https ?
        String registryUrl = UrlGenerationUtil.getRemoteRegistryURL(automationContext
                .getDefaultInstance());

        resourceAdminClient =
                new ResourceAdminServiceClient(backendURL, session);

        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        remoteRegistry = registryProviderUtil.getRemoteRegistry(automationContext);

        String userName = automationContext.getContextTenant().getContextUser().getUserName();

        if(userName.contains(FrameworkConstants.SUPER_TENANT_DOMAIN_NAME)) {
            userNameWithoutDomain = userName.substring(0, userName.indexOf('@'));
        }else{
            userNameWithoutDomain = userName;
        }
    }

    @Test
    public void testAddResourceUsingAdminClient()
            throws MalformedURLException, ResourceAdminServiceExceptionException, RemoteException {

        String path = getTestArtifactLocation() + "artifacts" + File.separator
                      + "GREG" + File.separator + "testresource.txt";
        DataHandler dataHandler = new DataHandler(new URL("file:///" + path));

        String fileType = "plain/text";
        resourceAdminClient.addResource(PATH + RES_NAME, fileType, RES_DESC, dataHandler);

        String authorUserName = resourceAdminClient.getResource(PATH + RES_NAME)[0].getAuthorUserName();
        assertTrue(userNameWithoutDomain.equalsIgnoreCase(authorUserName), "Resource creation failure");

    }

    @Test(dependsOnMethods = "testAddResourceUsingAdminClient")
    public void testGetResourceUsingRemoteRegistryClient() throws RegistryException {
        Resource resource = remoteRegistry.get(PATH + RES_NAME);
        String rDesc = resource.getDescription();

        assertTrue(RES_DESC.equalsIgnoreCase(rDesc), "Resources does not match.");

    }

    @AfterClass
    public void testCleanup() throws ResourceAdminServiceExceptionException, RemoteException {

        resourceAdminClient.deleteResource(PATH);
        resourceAdminClient = null;
        remoteRegistry = null;
    }
}
