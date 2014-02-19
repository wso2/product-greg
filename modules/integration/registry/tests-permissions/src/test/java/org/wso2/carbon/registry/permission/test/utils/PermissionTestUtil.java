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

package org.wso2.carbon.registry.permission.test.utils;

import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.user.mgt.UserManagementClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkSettings;

import java.rmi.RemoteException;

public class PermissionTestUtil {
    private static ManageEnvironment adminEnvironment;
    private static UserManagementClient userManagementClient;

    private static void setupManageEnvironments()
            throws LoginAuthenticationExceptionException, RemoteException {
        EnvironmentBuilder builderAdmin;
        builderAdmin = new EnvironmentBuilder().greg(ProductConstant.ADMIN_USER_ID); //tenant 1
        adminEnvironment = builderAdmin.build();
    }

    public static void setUpTestRoles() throws Exception {
        setupManageEnvironments();
        userManagementClient = new UserManagementClient(adminEnvironment.getGreg().getBackEndUrl(),
                                                        adminEnvironment.getGreg().getSessionCookie());
        userManagementClient.updateUserListOfRole(PermissionTestConstants.NON_ADMIN_ROLE, new String[]{},
                                                  PermissionTestConstants.NON_ADMIN_ROLE_2_USERS);
        userManagementClient.addRole(PermissionTestConstants.NON_ADMIN_ROLE_2,
                                     PermissionTestConstants.NON_ADMIN_ROLE_2_USERS, PermissionTestConstants.NON_ADMIN_PERMISSION);
    }

    public static void resetTestRoles() throws Exception {
        setupManageEnvironments();
        userManagementClient = new UserManagementClient(adminEnvironment.getGreg().getBackEndUrl(),
                                                        adminEnvironment.getGreg().getSessionCookie());
        userManagementClient.updateUserListOfRole(PermissionTestConstants.NON_ADMIN_ROLE,
                                                  PermissionTestConstants.NON_ADMIN_ROLE_2_USERS, new String[]{});
        userManagementClient.deleteRole(PermissionTestConstants.NON_ADMIN_ROLE_2);
    }


}
