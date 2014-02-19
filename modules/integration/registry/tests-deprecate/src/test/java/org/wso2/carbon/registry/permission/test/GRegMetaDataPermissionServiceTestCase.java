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
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.authenticator.stub.LogoutAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.authenticators.AuthenticatorClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.api.clients.user.mgt.UserManagementClient;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;

import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;


public class GRegMetaDataPermissionServiceTestCase {

    private static final Log log = LogFactory.getLog(GRegMetaDataPermissionServiceTestCase.class);
    private UserManagementClient userAdminStub;
    private static AuthenticatorClient userAuthenticationStub;
    private static ResourceAdminServiceClient admin_service_resource_admin ;
    private String gregHostName;
    private String sessionCookie;
    private String roleName;
    private String userName;
    private String userPassword;
    private LoginLogoutUtil util = new LoginLogoutUtil();
    private String SERVER_URL;
    private String repoLocation = "http://svn.wso2.org/repos/wso2/carbon/platform/branches/4.2.0/platform-integration/" +
            "platform-automated-test-suite/1.2.0/org.wso2.carbon.automation.test.repo/src/main/resources/artifacts/GREG";

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        ClientConnectionUtil.waitForPort(Integer.parseInt(FrameworkSettings.HTTP_PORT));
        sessionCookie = util.login();
        SERVER_URL = "https://" + FrameworkSettings.HOST_NAME +
                            ":" + FrameworkSettings.HTTPS_PORT + "/services/";
        gregHostName = FrameworkSettings.HOST_NAME;
        userAdminStub = new UserManagementClient(SERVER_URL, sessionCookie);
        userAuthenticationStub = new AuthenticatorClient(SERVER_URL);

        roleName = "meta_role";
        userName = "PRIMARY/greg_meta_user";

        if (userAdminStub.roleNameExists(roleName)) {  //delete the role if exists
            userAdminStub.deleteRole(roleName);
        }

        if (userAdminStub.userNameExists(roleName, userName)) { //delete user if exists
            userAdminStub.deleteUser(userName);
        }
    }


    @Test(groups = {"wso2.greg"}, description = "test add a role with login permission",
          priority = 1)
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
        String fetchUrl = repoLocation +"/wsdl/echo.wsdl";
        addRolewithUser(permission1);
        sessionCookieUser = new AuthenticatorClient(SERVER_URL).login(userName, userPassword, gregHostName);
        log.info("Newly Created User Loged in :" + userName);


        try {
            status = false;
            admin_service_resource_admin = new ResourceAdminServiceClient(SERVER_URL, sessionCookieUser);
            // greg_meta_user does not have permission to add a Text resource
            admin_service_resource_admin.addTextResource( "/", "resource.txt",
                                                         "", "", "");
        } catch (RemoteException e) {
            status = true;
            assertTrue(e.getMessage().contains("Access Denied."), "Access Denied Remote" +
                                                                     " Exception assertion Failed :");
            log.info("greg_login_user does not have permission to add a text resource :");
        }
        assertTrue(status, "Only user with write permission can put text resource");
        userAuthenticationStub.logOut();
        deleteRoleAndUsers(roleName, userName);
        addRolewithUser(permission2);
        sessionCookieUser = userAuthenticationStub.login(userName, userPassword, gregHostName);
        admin_service_resource_admin = new ResourceAdminServiceClient(SERVER_URL, sessionCookieUser);
        log.info("Newly Created User Loged in :" + userName);
        admin_service_resource_admin.addWSDL(resourceName, "", fetchUrl);
        admin_service_resource_admin.deleteResource(
                                                    "/_system/governance/trunk/services/" +
                                                    "org/wso2/carbon/core/services/echo/");
        admin_service_resource_admin.deleteResource(
                                                    "/_system/governance/trunk/wsdls/org/" +
                                                    "wso2/carbon/core/services/echo/");
        userAuthenticationStub.logOut();
        deleteRoleAndUsers(roleName, userName);
        log.info("*********GReg Metadata Permission Asigning Scenario test - Passed**********");
    }

    private void addRolewithUser(String[] permission) throws
                                                      Exception {
        userAdminStub.addRole(roleName, null, permission);
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
