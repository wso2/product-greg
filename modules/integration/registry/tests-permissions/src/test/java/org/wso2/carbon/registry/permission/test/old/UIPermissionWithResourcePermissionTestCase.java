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
package org.wso2.carbon.registry.permission.test.old;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.authenticator.stub.LogoutAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.api.clients.user.mgt.UserManagementClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceResourceServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import java.rmi.RemoteException;

/**
 * This  test to validate the Metadata delete for non-admin users
 */
public class UIPermissionWithResourcePermissionTestCase {

    private static final Log log =
            LogFactory.getLog(UIPermissionWithResourcePermissionTestCase.class);
    private int userId = 1;
    UserInfo userInfo = UserListCsvReader.getUserInfo(userId);
    private EnvironmentBuilder builder;
    private ManageEnvironment environment;
    private UserManagementClient userManagementClient;

    private static ResourceAdminServiceClient resourceAdminServiceClient;
    private RegistryProviderUtil registryProviderUtil;
    private String roleName;
    private String userName;
    private String userPassword;
    private WSRegistryServiceClient registry;


    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        builder = new EnvironmentBuilder().greg(userId);
        environment = builder.build();
        userManagementClient = new UserManagementClient(environment.getGreg().getProductVariables().getBackendUrl(),
                userInfo.getUserName(), userInfo.getPassword());


        registryProviderUtil = new RegistryProviderUtil();
        registry = registryProviderUtil.getWSRegistry(userId,
                ProductConstant.GREG_SERVER_NAME);
        roleName = "manager";
        userName = "ajith";
        userPassword = "ajith123";

        if (userManagementClient.roleNameExists(roleName)) {  //delete the role if exists
            userManagementClient.deleteRole(roleName);
        }

        if (userManagementClient.userNameExists(roleName, userName)) { //delete user if exists
            userManagementClient.deleteUser(userName);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Add a wsdl file by Admin user")
    public void testAddMetaDataByAdmin()
            throws RemoteException, LoginAuthenticationExceptionException, RegistryException,
            ResourceAdminServiceExceptionException {
        String resourceName = "echo.wsdl";
        String fetchUrl = "https://svn.wso2.org/repos/wso2/carbon/platform/branches/4.2.0/products/greg/4.6.0/modules/" +
                "integration/registry/tests-metadata/src/test/resources/artifacts/GREG/wsdl/echo.wsdl";


        resourceAdminServiceClient = new ResourceAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                userInfo.getUserName(), userInfo.getPassword());
        log.info("***********Admin user logged********************");
        resourceAdminServiceClient.addWSDL(resourceName, "", fetchUrl);
        log.info("***********echo.wsdl file Added********************");
        String adminWSDLPath = "/_system/governance/trunk/wsdls/org/wso2/carbon/core/services/echo/echo.wsdl";
        Assert.assertTrue(registry.resourceExists(adminWSDLPath),
                "echo.wsdl resource not found");

    }

    @Test(groups = {"wso2.greg"}, description = "Create a role manager and user" +
            " ajith and trying to delete wsdl without granting the resource permission",
          dependsOnMethods = "testAddMetaDataByAdmin")
    public void testDeleteMetaDataPermissionUser()
            throws Exception, RemoteException, LoginAuthenticationExceptionException,
            LogoutAuthenticationExceptionException, RegistryException,
            ResourceAdminServiceResourceServiceExceptionException {

        String permission1[] = {"/permission/admin/login",
                "/permission/admin/manage/resources/browse",
                "/permission/admin/manage/resources/browse/govern/list"};


        addRoleWithUser(permission1);

        log.info("Newly Created User Loged in :" + userName);

        String path = "/_system/governance/trunk/services/org/wso2/carbon/core/services/echo/";
        resourceAdminServiceClient = new ResourceAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                userName, userPassword);
        try {
            resourceAdminServiceClient.deleteResource(path);
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

//        String sessionCookieAdmin = new AuthenticatorClient(SERVER_URL).
//                login("admin", "admin", gregHostName);
//        ResourceAdminServiceStub resourceAdminServiceStub = TestUtils.getResourceAdminServiceStub(sessionCookieAdmin);
        resourceAdminServiceClient = new ResourceAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                userInfo.getUserName(), userInfo.getPassword());
        //Remove delete permission   2 - Deny   , 4 -Delete
        resourceAdminServiceClient.addResourcePermission(path1ToAuth, roleName, "4", "2");
        resourceAdminServiceClient.addResourcePermission(path2ToAuth, roleName, "4", "2");

        try {

            resourceAdminServiceClient = new ResourceAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                    userName, userPassword);
            resourceAdminServiceClient.deleteResource(path);
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

//        String sessionCookieAdmin = new AuthenticatorClient(SERVER_URL).
//                login("admin", "admin", gregHostName);
        resourceAdminServiceClient = new ResourceAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                userInfo.getUserName(), userInfo.getPassword());

        //Grant delete permission   1 - Allow  ,4 -Delete
        resourceAdminServiceClient.addResourcePermission(path1ToAuth, roleName, "4", "1");
        resourceAdminServiceClient.addResourcePermission(path2ToAuth, roleName, "4", "1");

        try {

            resourceAdminServiceClient = new ResourceAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                    userName, userPassword);
            resourceAdminServiceClient.deleteResource(path);
        } catch (AxisFault e) {
            log.info("******Failed to delete having resource delete permissions******");

        } catch (ResourceAdminServiceExceptionException e) {
        }

        Assert.assertFalse(registry.resourceExists(path), "user can't delete wsdl having resource delete permissions ");

        deleteRoleAndUsers(roleName, userName);
        deleteWsdl("/_system/governance/trunk/wsdls/org/wso2/carbon/core/services/echo/echo.wsdl");
    }

    @AfterClass
    public void clean() {
        resourceAdminServiceClient = null;
        builder = null;
        environment = null;
        registryProviderUtil = null;
        userManagementClient = null;
    }

    private void addRoleWithUser(String[] permission) throws
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

    private void deleteWsdl(String resourcePath) throws Exception {
        resourceAdminServiceClient = new ResourceAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                userInfo.getUserName(), userInfo.getPassword());
        resourceAdminServiceClient.deleteResource(resourcePath);
    }
}
