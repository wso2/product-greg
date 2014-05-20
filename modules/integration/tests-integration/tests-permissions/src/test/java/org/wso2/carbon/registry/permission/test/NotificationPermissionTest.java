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
import org.wso2.carbon.automation.engine.FrameworkConstants;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.integration.common.admin.client.UserManagementClient;
import org.wso2.carbon.registry.permission.test.utils.PermissionTestConstants;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.GREGTestConstants;
import org.wso2.greg.integration.common.utils.subscription.JMXSubscription;

import javax.activation.DataHandler;
import java.io.File;
import java.net.URL;
import java.rmi.RemoteException;

public class NotificationPermissionTest extends GREGIntegrationBaseTest {

    private static final String NEW_RESOURCE_PATH = "/_system/config/test.txt";
    private static final String[] PERMISSION_LIST = {
            "/permission/admin/login",
            "/permission/admin/manage/resources/browse",
            "/permission/admin/manage/resources/notifications",
            "/permission/admin/manage/resources/community-features",
            "/permission/protected/server-admin"
    };
    private static final String ROLE_NAME = "notificationtestrole";
    private static final String[] ROLE_USERS = {"notificationUM1"};

    private ResourceAdminServiceClient adminResourceAdminClient;
    private UserManagementClient userManagementClient;
    private JMXSubscription jmxSubscription = new JMXSubscription();
    private AutomationContext automationContextUser;

    @BeforeClass(alwaysRun = true)
    @SetEnvironment (executionEnvironments = {ExecutionEnvironment.STANDALONE})
    public void initialize() throws Exception {

        //Setup environments
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String sessionCookie = getSessionCookie();

        automationContextUser = new AutomationContext("GREG", "greg001",
                GREGTestConstants.SUPER_TENANT_DOMAIN_KEY, ROLE_USERS[0]);

        userManagementClient = new UserManagementClient(backendURL, sessionCookie);

        //remove user from admin role
        userManagementClient.updateUserListOfRole(FrameworkConstants.ADMIN_ROLE, null, ROLE_USERS);

        userManagementClient.addRole(ROLE_NAME, ROLE_USERS, PERMISSION_LIST);

        userManagementClient.updateUserListOfRole(PermissionTestConstants.NON_ADMIN_TEST_ROLE,
                                                  new String[]{}, ROLE_USERS);

        adminResourceAdminClient =
                new ResourceAdminServiceClient(backendURL, sessionCookie);

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

    @Test(groups = "wso2.greg", description = "Test access to resource notifications")
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    public void testAccessToNotifications() throws Exception {
        adminResourceAdminClient.addResourcePermission(NEW_RESOURCE_PATH, ROLE_NAME,
                                                       PermissionTestConstants.READ_ACTION,
                                                       PermissionTestConstants.PERMISSION_ENABLED);

        adminResourceAdminClient.addResourcePermission(NEW_RESOURCE_PATH, ROLE_NAME,
                                                       PermissionTestConstants.WRITE_ACTION,
                                                       PermissionTestConstants.PERMISSION_ENABLED);

        Assert.assertTrue(jmxSubscription.init(NEW_RESOURCE_PATH, "ResourceUpdated",
                automationContext));
    }

    @Test(groups = "wso2.greg", description = "Test deny to resource notifications",
           dependsOnMethods = "testAccessToNotifications")
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    public void testDenyNotifications() throws Exception {

        adminResourceAdminClient.addResourcePermission(NEW_RESOURCE_PATH, ROLE_NAME,
                                                       PermissionTestConstants.READ_ACTION,
                                                       PermissionTestConstants.PERMISSION_DISABLED);

        adminResourceAdminClient.addResourcePermission(NEW_RESOURCE_PATH, ROLE_NAME,
                                                       PermissionTestConstants.WRITE_ACTION,
                                                       PermissionTestConstants.PERMISSION_DISABLED);

        //userId 2 has read and write permission to NEW_RESOURCE_PATH, therefore disable.
        adminResourceAdminClient.addResourcePermission(
                NEW_RESOURCE_PATH,
                PermissionTestConstants.NON_ADMIN_TEST_ROLE,
                PermissionTestConstants.WRITE_ACTION,
                PermissionTestConstants.PERMISSION_DISABLED);

        adminResourceAdminClient.addResourcePermission(
                NEW_RESOURCE_PATH ,
                PermissionTestConstants.NON_ADMIN_TEST_ROLE,
                PermissionTestConstants.READ_ACTION,
                PermissionTestConstants.PERMISSION_DISABLED);

       boolean status = jmxSubscription.init(NEW_RESOURCE_PATH, "ResourceUpdated", automationContext);
    }


    @AfterClass(alwaysRun = true)
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    public void cleanUp() throws Exception {
        userManagementClient.deleteRole(ROLE_NAME);
        userManagementClient.updateUserListOfRole(PermissionTestConstants.NON_ADMIN_TEST_ROLE,
                                                  ROLE_USERS, new String[]{});
        adminResourceAdminClient.deleteResource(NEW_RESOURCE_PATH);

        //Re-set permission created at testDenyNotifications().
        adminResourceAdminClient.addResourcePermission(NEW_RESOURCE_PATH ,PermissionTestConstants.NON_ADMIN_TEST_ROLE
                ,PermissionTestConstants.WRITE_ACTION,PermissionTestConstants.PERMISSION_ENABLED);
        adminResourceAdminClient.addResourcePermission(NEW_RESOURCE_PATH ,PermissionTestConstants.NON_ADMIN_TEST_ROLE
                ,PermissionTestConstants.READ_ACTION,PermissionTestConstants.PERMISSION_ENABLED);
        jmxSubscription.disconnect();

        adminResourceAdminClient = null;
        userManagementClient = null;
        jmxSubscription = null;
    }
}
