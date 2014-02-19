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
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.user.mgt.UserManagementClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceResourceServiceExceptionException;

import java.rmi.RemoteException;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class Carbon6012 {

    private UserManagementClient userManagementClient;
    private String ROLE_NAME = "testRoleMultipleUsers";


    @BeforeClass(groups = "wso2.greg", description = "Create a role with Delete permission denied")
    public void init() throws Exception, LoginAuthenticationExceptionException,
                              ResourceAdminServiceResourceServiceExceptionException {

        int adminId = ProductConstant.ADMIN_USER_ID;
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(adminId);
        ManageEnvironment environment = builder.build();
        userManagementClient = new UserManagementClient(environment.getGreg().getBackEndUrl(),
                                                        environment.getGreg().getSessionCookie());

    }

    @Test(groups = "wso2.greg", description = "Create a role with Delete permission denied")
    public void addRolesAndAssignUsers() throws Exception, RemoteException {

        userManagementClient.updateUserListOfRole("testRole", new String[]{},
                                                  new String[]{"testuser2", "testuser3"});
        userManagementClient.addRole(ROLE_NAME,
                                     new String[]{"testuser2", "testuser3"}, new String[]{"/permission/admin/login",
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
                                                                                          "/permission/admin/manage/resources/ws-api"
        });

        assertTrue(userManagementClient.userNameExists(ROLE_NAME, "testuser2"), "testuser2 is not added to the new " +
                                                                                "Role");
        assertTrue(userManagementClient.userNameExists(ROLE_NAME, "testuser3"), "testuser3 is not added to the new " +
                                                                                "Role");

    }

    @AfterClass(alwaysRun = true)
    public void restoreArtifacts() throws Exception {

        try {
            userManagementClient.updateUserListOfRole("testRole", new String[]{"testuser2", "testuser3"},
                    new String[]{});
            userManagementClient.deleteRole(ROLE_NAME);
            userManagementClient = null;
        } catch (Exception e) {
            assertFalse(true, "Failed clean up operation" + e.getMessage());
        }
    }
}
