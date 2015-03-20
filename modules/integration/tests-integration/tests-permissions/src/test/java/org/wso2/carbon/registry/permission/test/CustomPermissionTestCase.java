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
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.integration.common.admin.client.UserManagementClient;
import org.wso2.carbon.registry.permission.test.utils.CustomPermissionTests;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;

public abstract class CustomPermissionTestCase extends GREGIntegrationBaseTest {

    private static final String[][] PERMISSION_LIST = {
            {
                    "/permission/admin/login",
                    "/permission/admin/manage/resources/notifications",
                    "/permission/admin/manage/resources/browse",
                    "/permission/admin/manage/resources/community-features",
                    "/permission/admin/configure/security"
            },
            {
                    "/permission/admin/login",
                    "/permission/admin/manage/resources/browse",
                    "/permission/admin/manage/search/activities",
            },
            {
                    "/permission/admin/login",
                    "/permission/admin/manage/resources/browse"
            },
            {
                    "/permission/admin/login",
                    "/permission/admin/manage/resources/browse",
                    "/permission/admin/manage/resources/associations"
            },
            {
                    "/permission/admin/login",
                    "/permission/admin/manage/resources/browse",
                    "/permission/admin/manage/resources/community-features",
                    "/permission/admin/configure/governance/lifecycles"
            },
    };
    private static final String[] TEST_ROLES = {
            "permissionTestRole1",
            "permissionTestRole2",
            "permissionTestRole3",
            "permissionTestRole4",
            "permissionTestRole5",
    };
    private static final String[] TEST_USERS = {
            "permissionTestUser1",
            "permissionTestUser2",
            "permissionTestUser3",
            "permissionTestUser4",
            "permissionTestUser5",
    };
    private UserManagementClient userManagementClient;

    private void initRoles () throws Exception {

        int roleCount = TEST_ROLES.length;
        for (int i = 0; i < roleCount; i++) {
            userManagementClient.addRole(TEST_ROLES[i], new String[]{TEST_USERS[i]}, PERMISSION_LIST[i]);
        }
    }

    @BeforeClass (alwaysRun = true)
    public void init () throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String sessionCookie = getSessionCookie();
        userManagementClient = new UserManagementClient(backendURL, sessionCookie);
        userManagementClient.updateUserListOfRole(FrameworkConstants.ADMIN_ROLE, null, TEST_USERS);
        initRoles();
    }

    @Test (groups = "wso2.greg", description = "test permission when only activity search permission given")
    public void testActivitySearchOnly () throws Exception {

        AutomationContext automationContext1 = new AutomationContext("GREG", "greg001",
                FrameworkConstants.SUPER_TENANT_DOMAIN_NAME, "permissionTestUser1");
        Assert.assertTrue(CustomPermissionTests.canSearchActivities(automationContext1));
        Assert.assertFalse(CustomPermissionTests.canAddLifecycles(automationContext1));
        Assert.assertFalse(CustomPermissionTests.canAddAssociation(automationContext1));
    }

    @Test (groups = "wso2.greg", description = "test permission when only browse permission given", dependsOnMethods = "testActivitySearchOnly")
    public void testBrowseOnly () throws Exception {

        AutomationContext automationContext2 = new AutomationContext("GREG", "greg001",
                FrameworkConstants.SUPER_TENANT_DOMAIN_NAME, "permissionTestUser2");
        Assert.assertTrue(CustomPermissionTests.canReadResource(automationContext2, "/_system/config"));
        Assert.assertFalse(CustomPermissionTests.canSearchActivities(automationContext2));
        Assert.assertFalse(CustomPermissionTests.canAddLifecycles(automationContext2));
        Assert.assertFalse(CustomPermissionTests.canAddAssociation(automationContext2));
    }

    @Test (groups = "wso2.greg", description = "test permission when only browse and Association permission given", dependsOnMethods = "testBrowseOnly")
    public void testBrowseAndAssociations () throws Exception {

        AutomationContext automationContext3 = new AutomationContext("GREG", "greg001",
                FrameworkConstants.SUPER_TENANT_DOMAIN_NAME, "permissionTestUser3");
        Assert.assertTrue(CustomPermissionTests.canReadResource(automationContext3, "/_system/config"));
        Assert.assertTrue(CustomPermissionTests.canAddAssociation(automationContext3));
        Assert.assertFalse(CustomPermissionTests.canSearchActivities(automationContext3));
        Assert.assertFalse(CustomPermissionTests.canAddLifecycles(automationContext3));
    }

    @Test (groups = "wso2.greg", description = "test permission when only browse and lifecycle permission given", dependsOnMethods = "testBrowseAndAssociations")
    public void testBrowseAndLifeCycles () throws Exception {

        AutomationContext automationContext4 = new AutomationContext("GREG", "greg001",
                FrameworkConstants.SUPER_TENANT_DOMAIN_NAME, "permissionTestUser4");
        Assert.assertTrue(CustomPermissionTests.canReadResource(automationContext4, "/_system/config"));
        Assert.assertTrue(CustomPermissionTests.canAddLifecycles(automationContext4));
        Assert.assertFalse(CustomPermissionTests.canAddAssociation(automationContext4));
        Assert.assertFalse(CustomPermissionTests.canSearchActivities(automationContext4));
    }

    @AfterClass (alwaysRun = true)
    public void cleanup () throws Exception {

        int roleCount = TEST_ROLES.length;
        userManagementClient.updateUserListOfRole(FrameworkConstants.ADMIN_ROLE, TEST_USERS, new String[]{});
        for (int i = 0; i < roleCount; i++) {
            userManagementClient.deleteRole(TEST_ROLES[i]);
        }
        userManagementClient = null;
    }
}
