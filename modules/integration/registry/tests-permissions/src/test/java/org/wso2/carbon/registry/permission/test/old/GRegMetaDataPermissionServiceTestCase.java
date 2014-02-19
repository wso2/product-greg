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
package org.wso2.carbon.registry.permission.test.old;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.authenticator.stub.LogoutAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.api.clients.user.mgt.UserManagementClient;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;

import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;


public class GRegMetaDataPermissionServiceTestCase {

    private static final Log log = LogFactory.getLog(GRegMetaDataPermissionServiceTestCase.class);
    private int userId = 1;
    UserInfo userInfo = UserListCsvReader.getUserInfo(userId);
    private EnvironmentBuilder builder;
    private ManageEnvironment environment;
    private UserManagementClient userManagementClient;
    private static ResourceAdminServiceClient resourceAdminServiceClient;
    private String roleName;
    private String userName;
    private String userPassword;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        builder = new EnvironmentBuilder().greg(userId);
        environment = builder.build();

        userManagementClient = new UserManagementClient(environment.getGreg().getProductVariables().getBackendUrl(),
                userInfo.getUserName(), userInfo.getPassword());

        roleName = "meta_role";
        userName = "greg_meta_user";

        if (userManagementClient.roleNameExists(roleName)) {  //delete the role if exists
            userManagementClient.deleteRole(roleName);
        }

        if (userManagementClient.userNameExists(roleName, userName)) { //delete user if exists
            userManagementClient.deleteUser(userName);
        }
    }


    @Test(groups = {"wso2.greg"}, description = "test add a role with login permission")
    public void testAddMetaDataPermissionUser()
            throws Exception, RemoteException, ResourceAdminServiceExceptionException,
            LoginAuthenticationExceptionException, LogoutAuthenticationExceptionException {

        userPassword = "welcome";
        String permission1[] = {"/permission/admin/login"};
//                                "/permission/admin/manage/resources/govern/metadata"};
        String permission2[] = {"/permission/admin/login",
                "/permission/admin/manage/resources"};
        String sessionCookieUser;
        boolean status;
        String resourceName = "echo.wsdl";
        String fetchUrl = "https://svn.wso2.org/repos/wso2/carbon/platform/branches/4.0.0/platform-integration/" +
                "clarity-tests/1.0.1/org.wso2.carbon.automation.test.repo/src/main/resources/artifacts/GREG/wsdl/echo.wsdl";
        addRolewithUser(permission1);


        try {
            status = false;
            resourceAdminServiceClient = new ResourceAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                    userName, userPassword);
            // greg_meta_user does not have permission to add a Text resource
            resourceAdminServiceClient.addTextResource("/", "resource.txt",
                    "", "", "");
        } catch (RemoteException e) {
            status = true;
            assertTrue(e.getMessage().contains("Access Denied."), "Access Denied Remote" +
                    " Exception assertion Failed :");
            log.info("greg_login_user does not have permission to add a text resource :");

        }
        assertTrue(status, "Only user with write permission can put text resource");
        builder = new EnvironmentBuilder().greg(userId);
        environment = builder.build();

        deleteRoleAndUsers(roleName, userName);
        addRolewithUser(permission2);

        resourceAdminServiceClient = new ResourceAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                userName, userPassword);
        log.info("Newly Created User Loged in :" + userName);
        resourceAdminServiceClient.addWSDL(resourceName, "", fetchUrl);
        resourceAdminServiceClient.deleteResource(
                "/_system/governance/trunk/services/" +
                        "org/wso2/carbon/core/services/echo/");
        resourceAdminServiceClient.deleteResource(
                "/_system/governance/trunk/wsdls/org/" +
                        "wso2/carbon/core/services/echo/");
        deleteRoleAndUsers(roleName, userName);
        log.info("*********GReg Metadata Permission Asigning Scenario test - Passed**********");
    }

    @AfterClass
    public void clean() {
        userManagementClient = null;
        resourceAdminServiceClient = null;
        builder = null;
        environment = null;

    }

    private void addRolewithUser(String[] permission) throws
            Exception {
        userManagementClient.addRole(roleName, null, permission);
        log.info("Successfully added Role :" + roleName);
        String roles[] = {roleName};
        userManagementClient.addUser(userName, userPassword, roles, null);
        log.info("Successfully User Crated :" + userName);
    }


    private void deleteRoleAndUsers(String roleName, String userName) throws Exception {
        userManagementClient.deleteRole(roleName);
        log.info("Role " + roleName + " deleted successfully");
        userManagementClient.deleteUser(userName);
    }
}
