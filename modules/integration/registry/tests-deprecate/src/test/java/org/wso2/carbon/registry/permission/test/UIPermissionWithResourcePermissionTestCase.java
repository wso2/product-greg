/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.registry.permission.test;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
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
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceResourceServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceStub;
import org.wso2.carbon.registry.resource.test.TestUtils;
import org.wso2.carbon.registry.search.metadata.test.utils.GregTestUtils;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import java.rmi.RemoteException;

/**
 * This  test to validate the Metadata delete for non-admin users
 */
public class UIPermissionWithResourcePermissionTestCase {

    private static final Log log =
            LogFactory.getLog(UIPermissionWithResourcePermissionTestCase.class);
    private UserManagementClient userAdminStub;
    private static AuthenticatorClient userAuthenticationStub;
    private static ResourceAdminServiceClient admin_service_resource_admin;
    private String gregHostName;
    private String sessionCookie;
    private String roleName;
    private String userName;
    private String userPassword;
    private LoginLogoutUtil util = new LoginLogoutUtil();
    private String SERVER_URL;
    private WSRegistryServiceClient registry;
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

        registry = GregTestUtils.getRegistry();
        roleName = "manager";
        userName = "ajith";
        userPassword = "ajith123";

        if (userAdminStub.roleNameExists(roleName)) {  //delete the role if exists
            userAdminStub.deleteRole(roleName);
        }

        if (userAdminStub.userNameExists(roleName, userName)) { //delete user if exists
            userAdminStub.deleteUser(userName);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Add a wsdl file by Admin user")
    public void testAddMetaDataByAdmin()
            throws RemoteException, LoginAuthenticationExceptionException, RegistryException,
                   ResourceAdminServiceExceptionException {
        String resourceName = "echo.wsdl";
        String fetchUrl = repoLocation + "/wsdl/echo.wsdl";

        String sessionCookieAdmin = new AuthenticatorClient(SERVER_URL).
                login("admin", "admin", gregHostName);
        admin_service_resource_admin = new ResourceAdminServiceClient(SERVER_URL, sessionCookieAdmin);
        log.info("***********Admin user logged********************");
        admin_service_resource_admin.addWSDL(resourceName, "", fetchUrl);
        log.info("***********echo.wsdl file Added********************");
        String adminWSDLPath = "/_system/governance/trunk/wsdls/org/wso2/carbon/core/services/echo/echo.wsdl";
        Assert.assertTrue(registry.resourceExists(adminWSDLPath),
                "echo.wsdl resource not found");

    }

    @Test(groups = {"wso2.greg"}, description = "Create a role manager and user" +
            " ajith and trying to delete wsdl without granting the resource permission", dependsOnMethods = "testAddMetaDataByAdmin")
    public void testDeleteMetaDataPermissionUser()
            throws Exception, RemoteException, LoginAuthenticationExceptionException,
                   LogoutAuthenticationExceptionException, RegistryException,
                   ResourceAdminServiceResourceServiceExceptionException {

        String permission1[] = {"/permission/admin/login",
                "/permission/admin/manage/resources/browse",
                "/permission/admin/manage/resources/browse/govern/list"};

        String sessionCookieUser;
        addRoleWithUser(permission1);
        sessionCookieUser = new AuthenticatorClient(SERVER_URL).
                login(userName, userPassword, gregHostName);
        log.info("Newly Created User Loged in :" + userName);

        String path = "/_system/governance/trunk/services/org/wso2/carbon/core/services/echo/";
        admin_service_resource_admin = new ResourceAdminServiceClient(SERVER_URL, sessionCookieUser);
        try {
            admin_service_resource_admin.deleteResource(path);
        } catch (AxisFault e) {
            log.info("********Failed to delete without resource delete permissions***********");
        } catch (ResourceAdminServiceExceptionException e) {

        }
        Assert.assertTrue(registry.resourceExists(path),
                "user has deleted the WSDL without permission ");
    }

    @Test(groups = {"wso2.greg"}, description = "deny delete for /_system/governance/trunk , /_system/governance/branches" +
            "and try to delete wsdl file  ", dependsOnMethods = "testDeleteMetaDataPermissionUser")
    public void testDeleteWithOutResourcePermission() throws RemoteException, LoginAuthenticationExceptionException,
            ResourceAdminServiceResourceServiceExceptionException, RegistryException,
            LogoutAuthenticationExceptionException {

        String path = "/_system/governance/trunk/services/org/wso2/carbon/core/services/echo/";
        String path1ToAuth = "/_system/governance/trunk";
        String path2ToAuth = "/_system/governance/branches";

        String sessionCookieAdmin = new AuthenticatorClient(SERVER_URL).
                login("admin", "admin", gregHostName);
        ResourceAdminServiceStub resourceAdminServiceStub = TestUtils.getResourceAdminServiceStub(sessionCookieAdmin);

        //Remove delete permission   2 - Deny   , 4 -Delete
        resourceAdminServiceStub.addRolePermission(path1ToAuth, roleName, "4", "2");
        resourceAdminServiceStub.addRolePermission(path2ToAuth, roleName, "4", "2");

        try {
            String sessionCookieUser = new AuthenticatorClient(SERVER_URL).
                    login(userName, userPassword, gregHostName);
            admin_service_resource_admin = new ResourceAdminServiceClient(SERVER_URL, sessionCookieUser);
            admin_service_resource_admin.deleteResource(path);
        } catch (AxisFault e) {
            log.info("********Failed to delete without resource delete  permissions***********");

        } catch (ResourceAdminServiceExceptionException e) {
        }

        Assert.assertTrue(registry.resourceExists(path), "user can't delete having resource delete permission ");
    }

    @Test(groups = {"wso2.greg"}, description = "allow delete for /_system/governance/branches /_system/governance/trunk" +
            "and try to delete wsdl", dependsOnMethods = "testDeleteWithOutResourcePermission")
    public void testDeleteWithResourcePermission()
            throws Exception, LoginAuthenticationExceptionException,
                   ResourceAdminServiceResourceServiceExceptionException, RegistryException,
                   LogoutAuthenticationExceptionException {

        String path = "/_system/governance/trunk/services/org/wso2/carbon/core/services/echo/";
        String path1ToAuth = "/_system/governance/trunk";
        String path2ToAuth = "/_system/governance/branches";

        String sessionCookieAdmin = new AuthenticatorClient(SERVER_URL).
                login("admin", "admin", gregHostName);
        ResourceAdminServiceStub resourceAdminServiceStub = TestUtils.getResourceAdminServiceStub(sessionCookieAdmin);

        //Grant delete permission   1 - Allow  ,4 -Delete
        resourceAdminServiceStub.addRolePermission(path1ToAuth, roleName, "4", "1");
        resourceAdminServiceStub.addRolePermission(path2ToAuth, roleName, "4", "1");

        try {
            String sessionCookieUser = new AuthenticatorClient(SERVER_URL).
                    login(userName, userPassword, gregHostName);
            admin_service_resource_admin = new ResourceAdminServiceClient(SERVER_URL, sessionCookieUser);
            admin_service_resource_admin.deleteResource(path);
        } catch (AxisFault e) {
            log.info("******Failed to delete having resource delete permissions******");

        } catch (ResourceAdminServiceExceptionException e) {
        }

        Assert.assertFalse(registry.resourceExists(path), "user can't delete wsdl having resource delete permissions ");
        userAuthenticationStub.logOut();
        deleteRoleAndUsers(roleName, userName);
    }

    private void addRoleWithUser(String[] permission) throws
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
