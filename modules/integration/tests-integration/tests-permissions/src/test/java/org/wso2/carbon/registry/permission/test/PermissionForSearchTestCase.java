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

package org.wso2.carbon.registry.permission.test;

import org.apache.axis2.AxisFault;

import static org.testng.Assert.assertNotNull;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.FrameworkConstants;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.integration.common.admin.client.UserManagementClient;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.registry.permission.test.utils.PermissionTestConstants;
import org.wso2.carbon.registry.search.metadata.test.bean.SearchParameterBean;
import org.wso2.carbon.registry.search.stub.SearchAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.search.stub.beans.xsd.AdvancedSearchResultsBean;
import org.wso2.carbon.registry.search.stub.beans.xsd.ArrayOfString;
import org.wso2.carbon.registry.search.stub.beans.xsd.CustomSearchParameterBean;
import org.wso2.greg.integration.common.clients.SearchAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;

import java.rmi.RemoteException;

public class PermissionForSearchTestCase extends GREGIntegrationBaseTest{

    private static final String EXISTING_RESOURCE_NAME = "uri";

    private static final String[] PERMISSION_SEARCH_ENABLED = {
            "/permission/admin/login",
            "/permission/admin/manage/search",
            "/permission/admin/manage/search/activities",
            "/permission/admin/manage/search/advanced",
            "/permission/admin/manage/search/basic"
    };
    private static final String[] PERMISSION_SEARCH_DISABLED = {
            "/permission/admin/login",
    };

    private static final String[] SEARCH_ENABLED_USERS = {"searchEnabledUser"};
    private static final String[] SEARCH_DISABLED_USERS = {"searchDisabledUser"};
    private static final String SEARCH_ENABLED_ROLE = "searchEnabledRole";
    private static final String SEARCH_DISABLED_ROLE = "searchDisabledRole";

    private SearchAdminServiceClient searchEnabledAdminServiceClient;
    private SearchAdminServiceClient searchDisabledAdminServiceClient;
    private static UserManagementClient userManagementClient;

    @BeforeClass(alwaysRun = true)
    public void initialize() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String adminSession = getSessionCookie();

        AutomationContext automationContextUser1 = new AutomationContext("GREG", "greg001",
                FrameworkConstants.SUPER_TENANT_KEY, SEARCH_ENABLED_USERS[0]);

        AutomationContext automationContextUser2 = new AutomationContext("GREG", "greg001",
                FrameworkConstants.SUPER_TENANT_KEY, SEARCH_DISABLED_USERS[0]);


        userManagementClient = new UserManagementClient(backendURL, adminSession);

        userManagementClient.addRole(SEARCH_ENABLED_ROLE, SEARCH_ENABLED_USERS,
                                     PERMISSION_SEARCH_ENABLED);

        userManagementClient.addRole(SEARCH_DISABLED_ROLE, SEARCH_DISABLED_USERS,
                                     PERMISSION_SEARCH_DISABLED);

        userManagementClient.updateUserListOfRole(FrameworkConstants.ADMIN_ROLE, null,SEARCH_ENABLED_USERS);
        userManagementClient.updateUserListOfRole(FrameworkConstants.ADMIN_ROLE, null,SEARCH_DISABLED_USERS);


        searchEnabledAdminServiceClient =
                new SearchAdminServiceClient(automationContextUser1.getContextUrls().getBackEndUrl(),
                        new LoginLogoutClient(automationContextUser1).login());

        searchDisabledAdminServiceClient =
                new SearchAdminServiceClient(automationContextUser2.getContextUrls().getBackEndUrl(),
                        new LoginLogoutClient(automationContextUser2).login());
    }

    @Test(groups = "wso2.greg", description = "Test deny permission for search", expectedExceptions = AxisFault.class)
    public void testDenySearch()
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setResourceName(EXISTING_RESOURCE_NAME);
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchDisabledAdminServiceClient.getAdvancedSearchResults(searchQuery);
    }

    @Test(groups = "wso2.greg", description = "Test allow permission for search")
    public void testAllowSearch()
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setResourceName(EXISTING_RESOURCE_NAME);
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        paramList = null;
        AdvancedSearchResultsBean result = searchEnabledAdminServiceClient.getAdvancedSearchResults(searchQuery);
        assertNotNull(result.getResourceDataList());
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() throws Exception {
        userManagementClient.deleteRole(SEARCH_ENABLED_ROLE);
        userManagementClient.deleteRole(SEARCH_DISABLED_ROLE);
        userManagementClient.updateUserListOfRole(PermissionTestConstants.NON_ADMIN_TEST_ROLE, SEARCH_ENABLED_USERS,
                                                  new String[]{});
        userManagementClient.updateUserListOfRole(PermissionTestConstants.NON_ADMIN_TEST_ROLE, SEARCH_DISABLED_USERS,
                                                  new String[]{});

        searchEnabledAdminServiceClient = null;
        searchDisabledAdminServiceClient = null;
        userManagementClient = null;
    }
}
