/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.registry.permission.test;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.api.clients.server.admin.ServerAdminClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.ServerGroupManager;
import org.wso2.carbon.automation.core.utils.ClientConnectionUtil;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkFactory;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkProperties;
import org.wso2.carbon.registry.permission.test.utils.PermissionTestConstants;
import org.wso2.carbon.registry.permission.test.utils.PermissionTestUtil;

import javax.activation.DataHandler;
import java.io.File;
import java.net.URL;

public abstract class PermissionOnServerRestartTestCase {

    private static final String DENY_TEST_RESOURCE_PATH = "/_system/testDeny.txt";
    private static final String ALLOW_TEST_RESOURCE_PATH = "/_system/testAllow.txt";

    private ResourceAdminServiceClient adminResourceAdminClient;
    private ResourceAdminServiceClient nonAdminResourceAdminClient;
    private ServerAdminClient serverAdminClient;
    private ManageEnvironment nonAdminEnvironment;
    private ManageEnvironment adminEnvironment;

    @BeforeClass(alwaysRun = true)
    public void initialize() throws Exception {
        PermissionTestUtil.setUpTestRoles();
        EnvironmentBuilder builderAdmin = new EnvironmentBuilder().greg(ProductConstant.ADMIN_USER_ID);
        adminEnvironment = builderAdmin.build();

        EnvironmentBuilder builderNonAdmin = new EnvironmentBuilder().greg(2);
        nonAdminEnvironment = builderNonAdmin.build();

        adminResourceAdminClient =
                new ResourceAdminServiceClient(adminEnvironment.getGreg().getBackEndUrl(),
                                               adminEnvironment.getGreg().getSessionCookie());
        nonAdminResourceAdminClient =
                new ResourceAdminServiceClient(nonAdminEnvironment.getGreg().getBackEndUrl(),
                                               nonAdminEnvironment.getGreg().getSessionCookie());

        serverAdminClient =
                new ServerAdminClient(adminEnvironment.getGreg().getBackEndUrl(),
                                      adminEnvironment.getGreg().getSessionCookie());

        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator
                              + "GREG" + File.separator + "resource.txt";
        DataHandler dataHandler = new DataHandler(new URL("file:///" + resourcePath));
        adminResourceAdminClient.addResource(DENY_TEST_RESOURCE_PATH, "text/plain", "", dataHandler);
        adminResourceAdminClient.addResource(ALLOW_TEST_RESOURCE_PATH, "text/plain", "", dataHandler);

    }

    @Test(groups = "wso2.greg", description = "Test permission when server is restarted", timeOut = 600000)
    public void testPermissionOnServerRestart() throws Exception {
        adminResourceAdminClient.addResourcePermission(DENY_TEST_RESOURCE_PATH,
                                                       PermissionTestConstants.NON_ADMIN_ROLE_2,
                                                       PermissionTestConstants.READ_ACTION,
                                                       PermissionTestConstants.PERMISSION_DISABLED);

        adminResourceAdminClient.addResourcePermission(DENY_TEST_RESOURCE_PATH,
                                                       PermissionTestConstants.EVERYONE_ROLE,
                                                       PermissionTestConstants.READ_ACTION,
                                                       PermissionTestConstants.PERMISSION_DISABLED);

        adminResourceAdminClient.addResourcePermission(ALLOW_TEST_RESOURCE_PATH,
                                                       PermissionTestConstants.NON_ADMIN_ROLE_2,
                                                       PermissionTestConstants.READ_ACTION,
                                                       PermissionTestConstants.PERMISSION_ENABLED);

        serverRestartGracefully(serverAdminClient);

        EnvironmentBuilder builderAdmin = new EnvironmentBuilder().greg(0);
        adminEnvironment = builderAdmin.build();

        adminResourceAdminClient = new ResourceAdminServiceClient(adminEnvironment.getGreg().getBackEndUrl(),
                                                                  adminEnvironment.getGreg().getSessionCookie());

        EnvironmentBuilder builderNonAdmin = new EnvironmentBuilder().greg(2);
        nonAdminEnvironment = builderNonAdmin.build();
        nonAdminResourceAdminClient =
                new ResourceAdminServiceClient(nonAdminEnvironment.getGreg().getBackEndUrl(),
                                               nonAdminEnvironment.getGreg().getSessionCookie());
        Assert.assertNull(nonAdminResourceAdminClient.getResource(DENY_TEST_RESOURCE_PATH));
        Assert.assertNotNull(nonAdminResourceAdminClient.getResource(ALLOW_TEST_RESOURCE_PATH));
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() throws Exception {
        PermissionTestUtil.resetTestRoles();
        adminResourceAdminClient.deleteResource(DENY_TEST_RESOURCE_PATH);
        adminResourceAdminClient.deleteResource(ALLOW_TEST_RESOURCE_PATH);

        adminResourceAdminClient = null;
        nonAdminResourceAdminClient = null;
        serverAdminClient = null;
        nonAdminEnvironment = null;
        adminEnvironment = null;
    }

    private void serverRestartGracefully(ServerAdminClient serverAdminClient) throws Exception {

        FrameworkProperties frameworkProperties =
                FrameworkFactory.getFrameworkProperties(ProductConstant.GREG_SERVER_NAME);
        ServerGroupManager.getServerUtils().restartGracefully(serverAdminClient, frameworkProperties);
    }
}
