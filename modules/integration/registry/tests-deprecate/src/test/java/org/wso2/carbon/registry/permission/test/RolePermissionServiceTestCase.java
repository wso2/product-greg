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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.user.mgt.UserManagementClient;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.user.mgt.common.UserAdminException;


public class RolePermissionServiceTestCase {
    private static final Log log = LogFactory.getLog(RolePermissionServiceTestCase.class);
    private UserManagementClient userAdminStub;
    private String sessionCookie;
    private String roleName;
    private String userName;
    private String userPassword;
    private LoginLogoutUtil util = new LoginLogoutUtil();


    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        ClientConnectionUtil.waitForPort(Integer.parseInt(FrameworkSettings.HTTP_PORT));
        sessionCookie = util.login();
        String SERVER_URL = "https://" + FrameworkSettings.HOST_NAME +
                            ":" + FrameworkSettings.HTTPS_PORT + "/services/";
        userAdminStub = new UserManagementClient(SERVER_URL, sessionCookie);
    }

    @Test(groups = {"wso2.greg"}, description = "test add a role with login permission",
          priority = 1)
    public void testAddLoginPermission() throws Exception {
        roleName = "login";
        userName = "greguser1";
        userPassword = "greguser1";


        deleteRolesIfExists();

        String permission[] = {"/permission/admin/login"};

        try {
            addRolewithUser(permission);

            deleteRoleAndUsers(roleName, userName);
            log.info("********GReg Create a Role with only Login privilege test - passed ********");
        } catch (UserAdminException e) {
            log.error("Failed to add login Role with User :" + e);
            throw new UserAdminException("Failed to add login Role with User :" + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "test add a role with configure permission",
          priority = 2)
    public void testAddConfigurePermission() throws Exception {
        roleName = "configure";
        userName = "greguser2";
        userPassword = "greguser2";


        deleteRolesIfExists();

        String permission[] = {"/permission/admin/configure"};

        try {
            addRolewithUser(permission);

            deleteRoleAndUsers(roleName, userName);
            log.info("*******GReg Create a Role with only configure permission  test - passed ***");
        } catch (UserAdminException e) {
            log.error("Failed to add configure permission with User :" + e);
            throw new UserAdminException("Failed to add configure permission with User :" +
                                         e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "test add a role with manage permission",
          priority = 3)
    public void testAddManagePermission() throws Exception {
        roleName = "manage";
        userName = "greguser3";
        userPassword = "greguser3";


        deleteRolesIfExists();

        String permission[] = {"/permission/admin/manage"};

        try {
            addRolewithUser(permission);

            deleteRoleAndUsers(roleName, userName);
            log.info("*******GReg Create a Role with only manage permission  test - passed ***");
        } catch (UserAdminException e) {
            log.error("Failed to add manage permission with User :" + e);
            throw new UserAdminException("Failed to add manage permission with User :" +
                                         e);
        }

    }

    @Test(groups = {"wso2.greg"}, description = "test add a role with monitor permission",
          priority = 4)
    public void testAddMonitorPermission() throws Exception {
        roleName = "monitor";
        userName = "greguser4";
        userPassword = "greguser4";


        deleteRolesIfExists();

        String permission[] = {"/permission/admin/monitor"};

        try {
            addRolewithUser(permission);

            deleteRoleAndUsers(roleName, userName);
            log.info("*******GReg Create a Role with only monitor permission  test - passed ***");
        } catch (UserAdminException e) {
            log.error("Failed to add monitor permission with User :" + e);
            throw new UserAdminException("Failed to add monitor permission with User :" +
                                         e);
        }
    }

    private void deleteRolesIfExists()
            throws Exception {
        if (userAdminStub.roleNameExists(roleName)) {  //delete the role if exists
            userAdminStub.deleteRole( roleName);
        }

        if (userAdminStub.userNameExists(roleName, userName)) { //delete user if exists
            userAdminStub.deleteUser(userName);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "test add a role with multiple permission",
          priority = 5)
    public void testAddMultiplePermissions() throws Exception {
        roleName = "mulitpermission";
        userName = "greguser5";
        userPassword = "greguser5";

        deleteRolesIfExists();

        String permission[] = {"/permission/admin/login", "/permission/admin/configure",
                               "/permission/admin/manage", "/permission/admin/monitor"};
        try {
            addRolewithUser(permission);

            deleteRoleAndUsers(roleName, userName);
            log.info("*******GReg Create a Role with only monitor permission  test - passed ***");
        } catch (UserAdminException e) {
            log.error("Failed to add monitor permission with User :" + e);
            throw new UserAdminException("Failed to add monitor permission with User :" +
                                         e);
        }

    }

    @Test(groups = {"wso2.greg"}, description = "test add a role with super admin permission",
          priority = 6)
    public void testAddSuperAdminPermission() throws Exception {
        roleName = "superadmin";
        userName = "greguser6";
        userPassword = "greguser6";

        deleteRolesIfExists();

        String permission[] = {"/permission/super admin/configure"};

        try {
            addRolewithUser(permission);

            deleteRoleAndUsers(roleName, userName);
            log.info("*******GReg Create a Role with super admin permission  test - passed ***");
        } catch (UserAdminException e) {
            log.error("Failed to add super admin permission with User :" + e);
            throw new UserAdminException("Failed to add super admin permission with User :" +
                                         e);
        }

    }

    public void addRolewithUser(String[] permission) throws Exception {
        userAdminStub.addRole(roleName, null , permission);
        log.info("Successfully added Role :" + roleName);

        String roles[] = {roleName};
        userAdminStub.addUser(userName, userPassword, roles, null);
        log.info("Successfully User Crated :" + userName);
    }


    private void deleteRoleAndUsers(String roleName, String userName) throws Exception {
        userAdminStub.deleteRole(roleName);
        log.info("Role " + roleName + " deleted successfully");
        userAdminStub.deleteUser(userName);
    }


}
