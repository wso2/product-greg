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

package org.wso2.carbon.registry.jira.issues.test;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.user.mgt.UserManagementClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.registry.permission.test.utils.PermissionTestConstants;
import org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName;

public class Carbon8980 {
    private static final String[] SECURITY_PERMISSION_LIST = {
            "/permission/admin/login",
            "/permission/admin/configure/security"
    };
    private static final String[] DUMMY_ROLE_PERMISSION_LIST = {
            "/permission/admin/login"
    };

    private static String[] ROLE_USERS;
    private static final String ROLE_NAME = "c8980";
    private static final String EXISTING_ROLE = ProductConstant.DEFAULT_PRODUCT_ROLE;
    private static final String NEW_USER = "testUserA";
    private static final String NEW_USER_PW = "ABCabc123";
    private static final String NEW_ROLE = "testRoleA";


    private UserManagementClient adminUserManagementClient;
    private UserManagementClient user1UserManagementClient;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        int userId = 1;
        UserInfo userInfo = UserListCsvReader.getUserInfo(userId);
        EnvironmentBuilder builderAdmin = new EnvironmentBuilder().greg(ProductConstant.ADMIN_USER_ID);
        ManageEnvironment adminEnvironment = builderAdmin.build();

        EnvironmentBuilder builderUser1 = new EnvironmentBuilder().greg(userId);
        ManageEnvironment user1Environment = builderUser1.build();

        adminUserManagementClient = new UserManagementClient(adminEnvironment.getGreg().getBackEndUrl(),
                                                             adminEnvironment.getGreg().getSessionCookie());
        user1UserManagementClient = new UserManagementClient(user1Environment.getGreg().getBackEndUrl(),
                                                             user1Environment.getGreg().getSessionCookie());
        ROLE_USERS = new String[]{userInfo.getUserNameWithoutDomain()};
        //setup roles
        adminUserManagementClient.addRole(ROLE_NAME, ROLE_USERS, SECURITY_PERMISSION_LIST);
        adminUserManagementClient.updateUserListOfRole(EXISTING_ROLE, new String[]{}, ROLE_USERS);
    }

    @Test
    public void testCreateUser() throws Exception {
        boolean found = false;
        user1UserManagementClient.addUser(NEW_USER, NEW_USER_PW, new String[]{PermissionTestConstants.EVERYONE_ROLE}, "testUserAProfile");
        FlaggedName[] flaggedNames = adminUserManagementClient.getRolesOfUser(NEW_USER, "*", 100);
        for (FlaggedName name : flaggedNames) {
            if (PermissionTestConstants.EVERYONE_ROLE.equalsIgnoreCase(name.getItemName())) {
                found = true;
            }
        }
        Assert.assertTrue(found, NEW_USER + " is not in internal/everyone");
    }

    @Test(dependsOnMethods = "testCreateUser")
    public void testCreateRole() throws Exception {
        user1UserManagementClient.addRole(NEW_ROLE, new String[]{}, DUMMY_ROLE_PERMISSION_LIST);
        Assert.assertTrue(adminUserManagementClient.roleNameExists(NEW_ROLE));
    }

    @Test(dependsOnMethods = "testCreateRole")
    public void testAddUserToRole() throws Exception {
        user1UserManagementClient.updateUserListOfRole(NEW_ROLE, new String[]{NEW_USER}, new String[]{});
        Assert.assertTrue(adminUserManagementClient.userNameExists(NEW_ROLE, NEW_USER));
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() throws Exception {
        adminUserManagementClient.deleteRole(ROLE_NAME);
        adminUserManagementClient.updateUserListOfRole(EXISTING_ROLE, ROLE_USERS, new String[]{});
        if (adminUserManagementClient.roleNameExists(NEW_ROLE)) {
            adminUserManagementClient.deleteRole(NEW_ROLE);
        }
        FlaggedName[] flaggedNames = adminUserManagementClient.getRolesOfUser(NEW_USER, "*", 100);
        for (FlaggedName name : flaggedNames) {
            if (name.getItemName().equals(PermissionTestConstants.EVERYONE_ROLE)) {
                adminUserManagementClient.deleteUser(NEW_USER);
            }
        }
        adminUserManagementClient = null;
        user1UserManagementClient = null;
    }
}
