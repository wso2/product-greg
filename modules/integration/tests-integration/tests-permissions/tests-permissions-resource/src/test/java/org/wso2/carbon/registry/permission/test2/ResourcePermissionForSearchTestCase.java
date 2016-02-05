/*
*Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.permission.test2;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.FrameworkConstants;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.integration.common.admin.client.UserManagementClient;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.logging.view.stub.LogViewerLogViewerException;
import org.wso2.carbon.logging.view.stub.types.carbon.LogEvent;
import org.wso2.carbon.registry.permission.test.utils.PermissionTestConstants;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceResourceServiceExceptionException;
import org.wso2.carbon.registry.search.metadata.test.bean.SearchParameterBean;
import org.wso2.carbon.registry.search.stub.SearchAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.search.stub.beans.xsd.AdvancedSearchResultsBean;
import org.wso2.carbon.registry.search.stub.beans.xsd.ArrayOfString;
import org.wso2.carbon.registry.search.stub.beans.xsd.CustomSearchParameterBean;
import org.wso2.greg.integration.common.clients.LogViewerClient;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.clients.SearchAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;

import javax.activation.DataHandler;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

public class ResourcePermissionForSearchTestCase extends GREGIntegrationBaseTest {
    private static final String RESOURCE_PATH = "/_system/config/testRolePermissionDummy.txt";
    private static final String RESOURCE_NAME = "testRolePermissionDummy.txt";
    private static final String[] PERMISSION_SEARCH_ENABLED = {
            "/permission/admin/login",
            "/permission/admin/manage/search",
            "/permission/admin/manage/search/activities",
            "/permission/admin/manage/search/advanced",
            "/permission/admin/manage/search/basic"
    };
    private static final String[] SEARCH_ENABLED_USERS = {"searchEnabledUser"};
    private static final String SEARCH_ENABLED_ROLE = "searchEnabledRole";
    private ResourceAdminServiceClient adminResourceAdminClient;
    private SearchAdminServiceClient adminSearchAdminClient;
    private SearchAdminServiceClient nonAdminSearchAdminClient;
    private UserManagementClient userManagementClient;
    private LogViewerClient logViewerClient;

    @BeforeClass(alwaysRun = true)
    public void initialize() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        ServerConfigurationManager serverConfigurationManager = new ServerConfigurationManager(automationContext);
        String configPath = getTestArtifactLocation() + "artifacts" + File.separator + "GREG" + File.separator + "log4j" + File.separator + "log4j.properties";
        serverConfigurationManager.applyConfiguration(new File(configPath));
        String sessionCookie = getSessionCookie();
        AutomationContext automationContextUser1 = new AutomationContext("GREG", "greg001",
                FrameworkConstants.SUPER_TENANT_KEY, SEARCH_ENABLED_USERS[0]);

        userManagementClient = new UserManagementClient(backendURL, sessionCookie);
        userManagementClient.addRole(SEARCH_ENABLED_ROLE, SEARCH_ENABLED_USERS,
                PERMISSION_SEARCH_ENABLED);
        userManagementClient.updateUserListOfRole(FrameworkConstants.ADMIN_ROLE, null,SEARCH_ENABLED_USERS);

        adminSearchAdminClient =
                new SearchAdminServiceClient(backendURL, sessionCookie);
        nonAdminSearchAdminClient =
                new SearchAdminServiceClient(automationContextUser1.getContextUrls()
                        .getBackEndUrl(), new LoginLogoutClient(automationContextUser1).login());
        adminResourceAdminClient =
                new ResourceAdminServiceClient(backendURL, sessionCookie);
        logViewerClient = new LogViewerClient(backendURL, sessionCookie);

    }

    @Test(groups = "wso2.greg",
            description = "Test add new resources to the registry")
    public void testAddNewResource() throws LogViewerLogViewerException, RemoteException, ResourceAdminServiceExceptionException, MalformedURLException {
        //Add a new resource
        String resourcePath = getTestArtifactLocation() + "artifacts" + File.separator
                + "GREG" + File.separator + "resource.txt";
        DataHandler dataHandler = new DataHandler(new URL("file:///" + resourcePath));
        adminResourceAdminClient.addResource(RESOURCE_PATH, "text/plain", "", dataHandler);
        boolean result = getLogEvents("Indexing Document in resource path: " +  RESOURCE_PATH, 1);
        Assert.assertTrue(result);
    }

    @Test(groups = "wso2.greg",
            description = "Test change access permission in the resources", dependsOnMethods = "testAddNewResource")
    public void testPermissionChangeToNewResource()
            throws ResourceAdminServiceResourceServiceExceptionException, LogViewerLogViewerException, InterruptedException, RemoteException {
        adminResourceAdminClient.addResourcePermission(RESOURCE_PATH,
                SEARCH_ENABLED_ROLE,
                PermissionTestConstants.READ_ACTION,
                PermissionTestConstants.PERMISSION_DISABLED);
        adminResourceAdminClient.addResourcePermission(RESOURCE_PATH,
                PermissionTestConstants.EVERYONE_ROLE,
                PermissionTestConstants.READ_ACTION,
                PermissionTestConstants.PERMISSION_DISABLED);
        boolean result = getLogEvents("Indexing Document in resource path: " +  RESOURCE_PATH, 2);
        Assert.assertTrue(result);
    }

    @Test(groups = "wso2.greg", description = "Test search resources with allowed user",
            dependsOnMethods = "testPermissionChangeToNewResource")
    public void testUserAllowSearchToResource()
            throws SearchAdminServiceRegistryExceptionException, LogViewerLogViewerException, RemoteException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setResourceName(RESOURCE_NAME);
        ArrayOfString[] paramList = paramBean.getParameterList();
        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = adminSearchAdminClient.getAdvancedSearchResults(searchQuery);
        assertNotNull(result.getResourceDataList());
        boolean logFound1 = getLogEvents("user roles filter query values: (internal/everyone OR admin)", 1);
        boolean logFound2 = getLogEvents("user roles filter query values: (admin OR internal/everyone)", 1);
        Assert.assertTrue(logFound1 || logFound2);
    }

    @Test(groups = "wso2.greg", description = "Test search resources with not allowed user",
            dependsOnMethods = "testUserAllowSearchToResource")
    public void testUserDisallowSearchToResource()
            throws SearchAdminServiceRegistryExceptionException, LogViewerLogViewerException, RemoteException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setResourceName(RESOURCE_NAME);
        ArrayOfString[] paramList = paramBean.getParameterList();
        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = nonAdminSearchAdminClient.getAdvancedSearchResults(searchQuery);
        assertNull(result.getResourceDataList());
        boolean logFound1 = getLogEvents("user roles filter query values: (searchenabledrole OR internal/everyone)", 1);
        boolean logFound2 = getLogEvents("user roles filter query values: (internal/everyone OR searchenabledrole)", 1);
        Assert.assertTrue(logFound1 || logFound2);
    }

    private boolean getLogEvents(String message, int length) throws RemoteException, LogViewerLogViewerException {
        double time1 = System.currentTimeMillis();
        while (true) {
            LogEvent[] logEvents = logViewerClient.getLogs("DEBUG", message, "", "");
            double time2 = System.currentTimeMillis();
            if (logEvents != null && logEvents.length >= length) {
                break;
            } else if ((time2 - time1) > 120000) {
                log.error("Timeout while searching for assets | time waited: " + (time2 - time1));
                return false;
            }
        }
        return true;
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws Exception {
        adminResourceAdminClient.deleteResource(RESOURCE_PATH);

        if( userManagementClient.roleNameExists(SEARCH_ENABLED_ROLE)){
            userManagementClient.deleteRole(SEARCH_ENABLED_ROLE);
        }
    }
}
