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

import org.wso2.carbon.automation.engine.FrameworkConstants;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.integration.common.admin.client.UserManagementClient;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;

public class PermissionTestUtil {

    private static UserManagementClient userManagementClient;

    public static void setUpTestRoles (AutomationContext autoCtx) throws Exception {

        String sessionCookie = new LoginLogoutClient(autoCtx).login();

        userManagementClient =
                new UserManagementClient(autoCtx.getContextUrls().getBackEndUrl(), sessionCookie);

        userManagementClient.updateUserListOfRole(FrameworkConstants.ADMIN_ROLE, null,
                new String[]{PermissionTestConstants.NON_ADMIN_ROLE_USER_1}); //remote user from
                // admin role

        userManagementClient.updateUserListOfRole(PermissionTestConstants.NON_ADMIN_TEST_ROLE, new String[]{},
                new String[]{PermissionTestConstants.NON_ADMIN_ROLE_USER_1});//remote user from
                // NON admin role first

        userManagementClient.addRole(PermissionTestConstants.NON_ADMIN_TEST_ROLE,
                new String[]{PermissionTestConstants.NON_ADMIN_ROLE_USER_1},
                PermissionTestConstants.NON_ADMIN_PERMISSION); //add the user to non admin role

        userManagementClient.updateUserListOfRole(FrameworkConstants.ADMIN_ROLE, null,
                new String[]{PermissionTestConstants.NON_ADMIN_ROLE_USER_2}); //remote user from
        // admin role

        userManagementClient.updateUserListOfRole(PermissionTestConstants.NON_ADMIN_TEST_ROLE,
                new String[]{PermissionTestConstants.NON_ADMIN_ROLE_USER_2}, new String[]{});



    }

    public static void resetTestRoles (AutomationContext autoCtx) throws Exception {

        String sessionCookie = new LoginLogoutClient(autoCtx).login();

        userManagementClient = new UserManagementClient(autoCtx.getContextUrls().getBackEndUrl(),
                sessionCookie);

        userManagementClient.updateUserListOfRole(PermissionTestConstants.NON_ADMIN_TEST_ROLE,
                new String[]{PermissionTestConstants.NON_ADMIN_ROLE_USER_1}, null);

        userManagementClient.updateUserListOfRole(FrameworkConstants.ADMIN_ROLE,
                new String[]{PermissionTestConstants.NON_ADMIN_ROLE_USER_1}, null);//add the user to admin role again

        userManagementClient.updateUserListOfRole(PermissionTestConstants.NON_ADMIN_TEST_ROLE,
                new String[]{PermissionTestConstants.NON_ADMIN_ROLE_USER_2}, null);

        userManagementClient.updateUserListOfRole(FrameworkConstants.ADMIN_ROLE,
                new String[]{PermissionTestConstants.NON_ADMIN_ROLE_USER_2},
                null);//add the user to admin role again

        userManagementClient.deleteRole(PermissionTestConstants.NON_ADMIN_TEST_ROLE);


    }

}
