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
package org.wso2.carbon.registry.ws.client.test.general.old;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.user.mgt.UserManagementClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import static org.testng.Assert.fail;

/**
 * A test case which tests registry security
 */
public class UserSecurityWSTestCase {

    private Registry everyOneRegistry = null;
    private WSRegistryServiceClient registry;
    public static String[] USER1 = {"user1"};

    public static final String[] USER1_PERMISSION = {"/permission/admin/login",
                                                     "/permission/admin/manage/resources",
                                                     "/permission/admin/manage/resources/associations",
                                                     "/permission/admin/manage/resources/browse",
                                                     "/permission/admin/manage/resources/community-features",
                                                     "/permission/admin/manage/resources/govern",
                                                     "/permission/admin/manage/resources/govern/api",
                                                     "/permission/admin/manage/resources/govern/api/add",
                                                     "/permission/admin/manage/resources/govern/api/list",
                                                     "/permission/admin/manage/resources/govern/generic",
                                                     "/permission/admin/manage/resources/govern/generic/add",
                                                     "/permission/admin/manage/resources/govern/generic/list",
                                                     "/permission/admin/manage/resources/govern/impactanalysis",
                                                     "/permission/admin/manage/resources/govern/lifecycles",
                                                     "/permission/admin/manage/resources/govern/lifecyclestagemonitor",
                                                     "/permission/admin/manage/resources/govern/metadata",
                                                     "/permission/admin/manage/resources/govern/generic/add",
                                                     "/permission/admin/manage/resources/govern/service/add",
                                                     "/permission/admin/manage/resources/govern/wsdl/add",
                                                     "/permission/admin/manage/resources/govern/schema/add",
                                                     "/permission/admin/manage/resources/govern/policy/add",
                                                     "/permission/admin/manage/resources/govern/generic/list",
                                                     "/permission/admin/manage/resources/govern/service/list",
                                                     "/permission/admin/manage/resources/govern/wsdl/list",
                                                     "/permission/admin/manage/resources/govern/schema/list",
                                                     "/permission/admin/manage/resources/govern/policy/list",
                                                     "/permission/admin/manage/resources/govern/resourceimpact",
                                                     "/permission/admin/manage/resources/govern/uri",
                                                     "/permission/admin/manage/resources/govern/uri/add",
                                                     "/permission/admin/manage/resources/govern/uri/list",
                                                     "/permission/admin/manage/resources/notifications",
                                                     "/permission/admin/manage/resources/ws-api"};

    @BeforeClass(groups = {"wso2.greg"})
    public void init()
            throws Exception {
        // TODO Auto-generated method stub
        int userId = 0;
        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        registry = registryProviderUtil.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);

        EnvironmentBuilder builderAdmin = new EnvironmentBuilder().greg(0);
        ManageEnvironment adminEnvironment = builderAdmin.build();
        UserManagementClient adminUserManagementClient = new UserManagementClient(adminEnvironment.getGreg().getBackEndUrl(),
                                                                                  adminEnvironment.getGreg().getSessionCookie());
        adminUserManagementClient.addUser("user1", "user1", null, "user1");
        adminUserManagementClient.addRole("user1Role", USER1, USER1_PERMISSION);


        everyOneRegistry = registryProviderUtil.getWSRegistry("user1", "user1", ProductConstant.GREG_SERVER_NAME);
    }
    // Since user manager does not have a WS API at the time of this test
    // users and permissions were manually removed/added from resources before this test was conducted
    // /testuser1/testuser1 is created - with /testuser1 having no permissions for the everyone role

    @Test(groups = {"wso2.greg"})
    public void checkEveryoneRoleDeniedPermissions() {

        try {

            Resource onlyAdminAcessResource = registry.newResource();
            registry.put("/testuser1/adminresource", onlyAdminAcessResource);

        } catch (Exception e) {
            // TODO Auto-generated catch block

        }

        try {
            Resource everyoneResource = everyOneRegistry.newResource();
            everyoneResource.setContent("this is a test resource");
            everyOneRegistry.put("/testuser1/everyoneresource", everyoneResource);
            fail("Everyone should not be able to add a resource");
        } catch (Exception e) {

        }

        try {
            everyOneRegistry.move("/testuser1/adminresource", "/newtestuser1");
            fail("Everyone should not be able to move resource");
        } catch (Exception e) {

        }
    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"checkEveryoneRoleDeniedPermissions"})
    public void checkEveryoneRoleAllowedPermissions() {
        try {

            Resource everoneAccessAccessResource = registry.newResource();
            registry.put("/testuser1/testuser1/everyoneAccessResource", everoneAccessAccessResource);

        } catch (Exception e) {

        }

        try {
            Resource everyoneResource = everyOneRegistry.newResource();
            everyoneResource.setContent("this is a test resource");
            everyOneRegistry.put("/testuser1/testuser1/everyoneresource", everyoneResource);
            fail("Everyone should not be able to add a resource");
        } catch (Exception e) {

        }

        try {
            everyOneRegistry.move("/testuser1/testuser1/adminresource", "/newtestuser2");
            fail("Everyone should not be able to move resource");
        } catch (Exception e) {

        }
    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"checkEveryoneRoleAllowedPermissions"})
    public void checkAdminRolePermissions() {
        try {

            Resource everyoneResource = registry.newResource();
            registry.put("/testuser1/testuser1/everyoneresource", everyoneResource);

        } catch (Exception e) {
            fail("everyone role was not able to add resource - please modify permissions");
        }

        try {
            Resource adminResource = registry.newResource();
            adminResource.setContent("this is a test resource");
            registry.put("/testuser1/everyoneresource", adminResource);
        } catch (Exception e) {
            fail("Admin should be able to add any resource");
        }

        try {
            registry.move("/testuser1/everyoneresource", "/newtestuser3");
        } catch (Exception e) {
            fail("Admin should be able to move resource");
        }
    }


}
