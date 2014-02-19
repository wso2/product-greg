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
import org.wso2.carbon.automation.api.clients.registry.SearchAdminServiceClient;
import org.wso2.carbon.automation.api.clients.user.mgt.UserManagementClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.registry.permission.test.utils.PermissionTestConstants;
import org.wso2.carbon.registry.search.metadata.test.bean.SearchParameterBean;
import org.wso2.carbon.registry.search.stub.SearchAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.search.stub.beans.xsd.AdvancedSearchResultsBean;
import org.wso2.carbon.registry.search.stub.beans.xsd.ArrayOfString;
import org.wso2.carbon.registry.search.stub.beans.xsd.CustomSearchParameterBean;

import java.rmi.RemoteException;

public class PermissionForSearchTestCase {

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

    private static final String[] SEARCH_ENABLED_USERS = {"testuser2"};
    private static final String[] SEARCH_DISABLED_USERS = {"testuser3"};
    private static final String SEARCH_ENABLED_ROLE = "searchEnabledRole";
    private static final String SEARCH_DISABLED_ROLE = "searchDisabledRole";

    private SearchAdminServiceClient searchEnabledAdminServiceClient;
    private SearchAdminServiceClient searchDisabledAdminServiceClient;
    private static UserManagementClient userManagementClient;

    @BeforeClass(alwaysRun = true)
    public void initialize() throws Exception, RemoteException {
        EnvironmentBuilder builderAdmin = new EnvironmentBuilder().greg(ProductConstant.ADMIN_USER_ID);
        ManageEnvironment adminEnvironment = builderAdmin.build();
        EnvironmentBuilder builderNonAdmin1 = new EnvironmentBuilder().greg(2);
        ManageEnvironment nonAdminEnvironment1 = builderNonAdmin1.build();
        EnvironmentBuilder builderNonAdmin2 = new EnvironmentBuilder().greg(3);
        ManageEnvironment nonAdminEnvironment2 = builderNonAdmin2.build();


        userManagementClient = new UserManagementClient(adminEnvironment.getGreg().getBackEndUrl(),
                                                        adminEnvironment.getGreg().getSessionCookie());
        userManagementClient.addRole(SEARCH_ENABLED_ROLE, SEARCH_ENABLED_USERS,
                                     PERMISSION_SEARCH_ENABLED);
        userManagementClient.addRole(SEARCH_DISABLED_ROLE, SEARCH_DISABLED_USERS,
                                     PERMISSION_SEARCH_DISABLED);
        userManagementClient.updateUserListOfRole(PermissionTestConstants.NON_ADMIN_ROLE,
                                                  new String[]{}, SEARCH_DISABLED_USERS);
        userManagementClient.updateUserListOfRole(PermissionTestConstants.NON_ADMIN_ROLE,
                                                  new String[]{}, SEARCH_ENABLED_USERS);
        searchEnabledAdminServiceClient =
                new SearchAdminServiceClient(nonAdminEnvironment1.getGreg().getBackEndUrl()
                        , nonAdminEnvironment1.getGreg().getSessionCookie());
        searchDisabledAdminServiceClient =
                new SearchAdminServiceClient(nonAdminEnvironment2.getGreg().getBackEndUrl
                        (), nonAdminEnvironment2.getGreg().getSessionCookie());
    }

    @Test(groups = "wso2.greg", description = "Test deny permission for search", expectedExceptions = AxisFault.class)
    public void testDenySearch()
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setResourceName(EXISTING_RESOURCE_NAME);
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        paramList = null;
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
        userManagementClient.updateUserListOfRole(PermissionTestConstants.NON_ADMIN_ROLE, SEARCH_ENABLED_USERS,
                                                  new String[]{});
        userManagementClient.updateUserListOfRole(PermissionTestConstants.NON_ADMIN_ROLE, SEARCH_DISABLED_USERS,
                                                  new String[]{});

        searchEnabledAdminServiceClient = null;
        searchDisabledAdminServiceClient = null;
        userManagementClient = null;
    }
}
