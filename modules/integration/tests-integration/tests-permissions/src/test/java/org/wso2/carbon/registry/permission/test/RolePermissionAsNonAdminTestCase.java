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

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.FrameworkConstants;
import org.wso2.carbon.automation.engine.configurations.UrlGenerationUtil;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.integration.common.admin.client.UserManagementClient;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.registry.permission.test.utils.PermissionTestConstants;
import org.wso2.carbon.registry.permission.test.utils.PermissionTestUtil;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceResourceServiceExceptionException;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;

import javax.activation.DataHandler;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class RolePermissionAsNonAdminTestCase extends GREGIntegrationBaseTest{

    private static final String NEW_RESOURCE_PATH = "/_system/config/testPermissionNonAdminDummy.txt";
    private ResourceAdminServiceClient adminResourceAdminClient;
    private ResourceAdminServiceClient nonAdminResourceAdminClient;
    private ResourceAdminServiceClient nonAdminResourceAdminClient2;
    private String serverUrl;
    private UserManagementClient userManagementClient;
    private String [] userNames = {"permissionUN5", "permissionUN6"};

    @BeforeClass(alwaysRun = true)
    public void initialize() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String sessionCookie = getSessionCookie();

        PermissionTestUtil.setUpTestRoles(automationContext);
        //Setup environments

        AutomationContext automationContextUser1 = new AutomationContext("GREG", "greg001",
                FrameworkConstants.SUPER_TENANT_KEY,
                "permissionUN5");

        AutomationContext automationContextUser2 = new AutomationContext("GREG", "greg001",
                FrameworkConstants.SUPER_TENANT_KEY,
                "permissionUN6");

        userManagementClient = new UserManagementClient(backendURL, sessionCookie);

        userManagementClient.updateUserListOfRole(FrameworkConstants.ADMIN_ROLE, null,  userNames);

        userManagementClient.updateUserListOfRole(PermissionTestConstants.NON_ADMIN_TEST_ROLE,
                userNames, null);

//        if(userManagementClient.roleNameExists(PermissionTestConstants.NON_ADMIN_TEST_ROLE)){
//            userManagementClient.deleteRole(PermissionTestConstants.NON_ADMIN_TEST_ROLE);
//        }

//        userManagementClient.addRole(PermissionTestConstants.NON_ADMIN_TEST_ROLE,
//                userNames, PermissionTestConstants.NON_ADMIN_PERMISSION);

        adminResourceAdminClient =
                new ResourceAdminServiceClient(backendURL,sessionCookie);

        nonAdminResourceAdminClient =
                new ResourceAdminServiceClient(automationContextUser1.getContextUrls()
                        .getBackEndUrl(), new LoginLogoutClient(automationContextUser1).login());

        nonAdminResourceAdminClient2 =
                new ResourceAdminServiceClient(automationContextUser2.getContextUrls()
                        .getBackEndUrl(), new LoginLogoutClient(automationContextUser2).login());

        serverUrl = UrlGenerationUtil.getRemoteRegistryURL(automationContext.getDefaultInstance());

        //Add a new resource
        String resourcePath = getTestArtifactLocation() + "artifacts" + File.separator
                              + "GREG" + File.separator + "resource.txt";

        DataHandler dataHandler = new DataHandler(new URL("file:///" + resourcePath));

        adminResourceAdminClient.addResource(NEW_RESOURCE_PATH, "text/plain", "", dataHandler);

        adminResourceAdminClient.addResourcePermission(NEW_RESOURCE_PATH,
                                                       PermissionTestConstants.EVERYONE_ROLE,
                                                       PermissionTestConstants.READ_ACTION,
                                                       PermissionTestConstants.PERMISSION_DISABLED);
    }

    @Test(groups = "wso2.greg", expectedExceptions = IOException.class,
            description = "Test deny access to new resources")
    public void testUserDenyAccessToNewResource()
            throws ResourceAdminServiceResourceServiceExceptionException, IOException,
                   ResourceAdminServiceExceptionException {
        adminResourceAdminClient.addResourcePermission(NEW_RESOURCE_PATH,
                                                       PermissionTestConstants.NON_ADMIN_TEST_ROLE,
                                                       PermissionTestConstants.READ_ACTION,
                                                       PermissionTestConstants.PERMISSION_ENABLED);

        assertNotNull(nonAdminResourceAdminClient2.getResource(NEW_RESOURCE_PATH));

        adminResourceAdminClient.addResourcePermission(NEW_RESOURCE_PATH,
                                                          PermissionTestConstants.NON_ADMIN_TEST_ROLE,
                                                          PermissionTestConstants.READ_ACTION,
                                                          PermissionTestConstants.PERMISSION_DISABLED);

        nonAdminResourceAdminClient2.getResource(NEW_RESOURCE_PATH);

        assertNull(nonAdminResourceAdminClient2.getResource(NEW_RESOURCE_PATH));

        //test anonymous access. should give an exception
        URL resourceURL = new URL(serverUrl + "resource" + NEW_RESOURCE_PATH);
        InputStream resourceStream = resourceURL.openStream();
    }

    @Test(groups = "wso2.greg", expectedExceptions = IOException.class,
            description = "Test allow access to new resources", dependsOnMethods = "testUserDenyAccessToNewResource")
    public void testUserAllowAccessToNewResource()
            throws ResourceAdminServiceResourceServiceExceptionException, IOException,
                   ResourceAdminServiceExceptionException {

        adminResourceAdminClient.addResourcePermission(NEW_RESOURCE_PATH,
                                                   PermissionTestConstants.NON_ADMIN_TEST_ROLE,
                                                   PermissionTestConstants.READ_ACTION,
                                                   PermissionTestConstants.PERMISSION_DISABLED);

        assertNull(nonAdminResourceAdminClient2.getResource(NEW_RESOURCE_PATH));

        adminResourceAdminClient.addResourcePermission(NEW_RESOURCE_PATH,
                                                          PermissionTestConstants.NON_ADMIN_TEST_ROLE,
                                                          PermissionTestConstants.READ_ACTION,
                                                          PermissionTestConstants.PERMISSION_ENABLED);

        assertNotNull(nonAdminResourceAdminClient2.getResource(NEW_RESOURCE_PATH));

        //test anonymous access. should give an exception
        URL resourceURL = new URL(serverUrl + "resource" + NEW_RESOURCE_PATH);
        InputStream resourceStream = resourceURL.openStream();
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws Exception {
        PermissionTestUtil.resetTestRoles(automationContext);
        adminResourceAdminClient.deleteResource(NEW_RESOURCE_PATH);

        adminResourceAdminClient = null;
        nonAdminResourceAdminClient = null;
        nonAdminResourceAdminClient2 = null;
    }
}
