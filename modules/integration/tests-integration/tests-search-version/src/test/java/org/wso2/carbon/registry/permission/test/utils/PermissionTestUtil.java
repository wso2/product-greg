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
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.greg.integration.common.clients.UserManagementClient;

import javax.xml.xpath.XPathExpressionException;
import java.rmi.RemoteException;

public class PermissionTestUtil {

    private static UserManagementClient userManagementClient;
    private static AutomationContext automationContext;

    private static void setupManageEnvironments()
            throws LoginAuthenticationExceptionException, RemoteException, XPathExpressionException {

        automationContext = new AutomationContext("GREG", TestUserMode.SUPER_TENANT_ADMIN);
    }

    public static void setUpTestRoles() throws Exception {
        setupManageEnvironments();

        LoginLogoutClient loginLogoutClient = new LoginLogoutClient(automationContext);
        String sessionCookie =  loginLogoutClient.login();

        userManagementClient = new UserManagementClient(automationContext.getContextUrls().getBackEndUrl(),
                sessionCookie);
        userManagementClient.updateUserListOfRole(PermissionTestConstants.NON_ADMIN_ROLE, new String[]{},
                                                  PermissionTestConstants.NON_ADMIN_ROLE_2_USERS);
        userManagementClient.addRole(PermissionTestConstants.NON_ADMIN_ROLE_2,
                                     PermissionTestConstants.NON_ADMIN_ROLE_2_USERS, PermissionTestConstants.NON_ADMIN_PERMISSION);
    }

    public static void resetTestRoles() throws Exception {
        setupManageEnvironments();

        LoginLogoutClient loginLogoutClient = new LoginLogoutClient(automationContext);
        String sessionCookie =  loginLogoutClient.login();

        userManagementClient = new UserManagementClient( automationContext.getContextUrls().getBackEndUrl(),
                                                        sessionCookie);
        userManagementClient.updateUserListOfRole(PermissionTestConstants.NON_ADMIN_ROLE,
                                                  PermissionTestConstants.NON_ADMIN_ROLE_2_USERS, new String[]{});
        userManagementClient.deleteRole(PermissionTestConstants.NON_ADMIN_ROLE_2);
    }


}
