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

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.greg.integration.common.clients.UserManagementClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class Carbon6012TestCase extends GREGIntegrationBaseTest {

    private UserManagementClient userManagementClient;
    private String ROLE_NAME = "testRoleMultipleUsers";
    private String[] userNames = {"carbon6012user1", "carbon6012user2"};

    @BeforeClass(groups = "wso2.greg", description = "Create a role with Delete permission denied")
    public void init() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String session = getSessionCookie();
        userManagementClient = new UserManagementClient(backendURL, session);

    }
    @Test(groups = "wso2.greg", description = "Create a role with Delete permission denied")
    public void addRolesAndAssignUsers() throws Exception {
        String passsword = "password";
        userManagementClient.addUser(userNames[0], passsword, null, null);
        userManagementClient.addUser(userNames[1], passsword, null, null);

        userManagementClient.addRole(ROLE_NAME, userNames,
                new String[]{"/permission/admin/login",
                        "/permission/admin/manage/resources/browse",
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
                        "/permission/admin/manage/resources/ws-api"});

        assertTrue(userManagementClient.userNameExists(ROLE_NAME, userNames[0]), "user is not added to the new " + "Role");

        assertTrue(userManagementClient.userNameExists(ROLE_NAME, userNames[1]),
                "user is not added to the new " + "Role");

    }
    @AfterClass(alwaysRun = true)
    public void restoreArtifacts() throws Exception {
            userManagementClient.deleteRole(ROLE_NAME);
            userManagementClient.deleteUser(userNames[0]);
            userManagementClient.deleteUser(userNames[1]);
    }
}
