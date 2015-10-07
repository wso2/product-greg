/*
* Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


package org.wso2.carbon.registry.permission.test;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.integration.common.admin.client.UserManagementClient;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;

import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;


public class GRegLoginPermissionServiceTestCase extends GREGIntegrationBaseTest{

    private static final Log log = LogFactory.getLog(GRegLoginPermissionServiceTestCase.class);
    private UserManagementClient userManagementClient;
    private String roleName;
    private String userName;
    private String userPassword;
    private String sessionCookie;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        sessionCookie = getSessionCookie();

        userManagementClient = new UserManagementClient(backendURL, sessionCookie);

        roleName = "login_role";
        userName = "greg_login_user";
        userPassword = "welcome";

        if (userManagementClient.roleNameExists(roleName)) {  //delete the role if exists
            userManagementClient.deleteRole(roleName);
        }

        if (userManagementClient.userNameExists(roleName, userName)) { //delete user if exists
            userManagementClient.deleteUser(userName);
        }
    }


    @Test(groups = {"wso2.greg"}, description = "test add a role with login permission",
            expectedExceptions = AxisFault.class)
    public void testAddLoginPermissionUser() throws Exception {

        String permission[] = {"/permission/admin/login"};
        String userList[] = {userName};
        String sessionCookieUser = null;
        boolean status;

        addRoleWithUser(permission, userList);
        //sessionCookieUser = authenticatorClient.login(userName, userPassword, FrameworkSettings.HOST_NAME);
        log.info("Newly Created User Logged in :" + userName);

        try {
            status = false;
            ResourceAdminServiceClient resourceAdminServiceClient = new
                    ResourceAdminServiceClient(backendURL, userName, userPassword);
            // greg_login_user does not have permission to add a Text resource
            resourceAdminServiceClient.addTextResource("/", "login.txt", "", "", "");
        } catch (RemoteException e) {
            status = true;
            assertTrue(e.getMessage().contains("Access Denied"), "Access Denied Remote" +
                    " Exception assertion Failed :");
            log.info("greg_login_user does not have permission to add a text resource :");
            throw new AxisFault("AxisFault");
        }
        assertTrue(status, "User can't upload resource having only login permission");
        //authenticatorClient.logOut();
    }

    private void addRoleWithUser(String[] permission, String[] userList) throws Exception {
        userManagementClient.addRole(roleName, null, permission);
        log.info("Successfully added Role :" + roleName);
        String roles[] = {roleName};
        userManagementClient.addUser(userName, userPassword, roles, null);
        log.info("Successfully User Created :" + userName);
    }

    @AfterClass(alwaysRun = true)
    public void deleteRoleAndUsers() throws Exception {
        userManagementClient.deleteRole(roleName);
        log.info("Role " + roleName + " deleted successfully");
        userManagementClient.deleteUser(userName);

    }

}
