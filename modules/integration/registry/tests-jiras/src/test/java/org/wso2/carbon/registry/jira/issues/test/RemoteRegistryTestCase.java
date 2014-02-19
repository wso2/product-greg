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
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.registry.app.RemoteRegistry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;

import javax.activation.DataHandler;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;

public class RemoteRegistryTestCase {

    private static final String PATH = "/c4/";
    private static final String RES_NAME = "testResource.txt";
    private static final String RES_DESC = "A test resource";

    private ResourceAdminServiceClient resourceAdminClient;
    private UserInfo userInfo;


    private RemoteRegistry remoteRegistry;

    @BeforeClass
    public void initialize()
            throws MalformedURLException, RegistryException, LoginAuthenticationExceptionException,
                   RemoteException {

        int userId = ProductConstant.ADMIN_USER_ID;

        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        ManageEnvironment environment = builder.build();

        //TODO- What about https ?
        String registryUrl = "http://" + environment.getGreg().getProductVariables().getHostName() +
                             ":" + Integer.parseInt(environment.getGreg()
                                                            .getProductVariables()
                                                            .getHttpPort()) + "/registry/";

        userInfo = UserListCsvReader.getUserInfo(userId);

        resourceAdminClient =
                new ResourceAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                               environment.getGreg().getSessionCookie());

        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        remoteRegistry = registryProviderUtil.getRemoteRegistry(userId, ProductConstant.GREG_SERVER_NAME);
    }

    @Test
    public void testAddResourceUsingAdminClient()
            throws MalformedURLException, ResourceAdminServiceExceptionException, RemoteException {

        String path = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator
                      + "GREG" + File.separator + "testresource.txt";
        DataHandler dataHandler = new DataHandler(new URL("file:///" + path));

        String fileType = "plain/text";
        resourceAdminClient.addResource(PATH + RES_NAME, fileType, RES_DESC, dataHandler);

        String authorUserName = resourceAdminClient.getResource(PATH + RES_NAME)[0].getAuthorUserName();
        assertTrue(userInfo.getUserNameWithoutDomain().equalsIgnoreCase(authorUserName), "Resource creation failure");

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
        userInfo = null;
        remoteRegistry = null;
    }
}
