/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.registry.jira.issues.test;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.integration.common.admin.client.UserManagementClient;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.search.metadata.test.bean.SearchParameterBean;
import org.wso2.carbon.registry.search.metadata.test.utils.PermissionTestConstants;
import org.wso2.carbon.registry.search.stub.SearchAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.search.stub.beans.xsd.AdvancedSearchResultsBean;
import org.wso2.carbon.registry.search.stub.beans.xsd.ArrayOfString;
import org.wso2.carbon.registry.search.stub.beans.xsd.CustomSearchParameterBean;
import org.wso2.carbon.registry.search.stub.common.xsd.ResourceData;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.clients.SearchAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import javax.xml.namespace.QName;
import java.rmi.RemoteException;

public class SaveSearchTestCase extends GREGIntegrationBaseTest{

    private static final String USER_NAME = "testUser00001";
    private SearchAdminServiceClient searchAdminServiceClient;
    private Registry governance;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private UserManagementClient userManagementClient;
    private final String FILTER_NAME = "serviceSearch";
    private WSRegistryServiceClient wsRegistryServiceClient;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String session = getSessionCookie();

        userManagementClient = new UserManagementClient(backendURL, session);
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(backendURL, session);
        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        wsRegistryServiceClient =
                registryProviderUtil.getWSRegistry(automationContext);
        governance = registryProviderUtil.getGovernanceRegistry(wsRegistryServiceClient, automationContext);

    }

    @Test(groups = {"wso2.greg"}, description = "Add user", dependsOnMethods = "testAddRole")
    public void testAddUser() throws Exception {

        userManagementClient.addUser(USER_NAME, USER_NAME + "pass", new String[]{"test_Role"}, USER_NAME);

        Assert.assertTrue(userManagementClient.userNameExists("test_Role", USER_NAME));
    }

    @Test(groups = {"wso2.greg"}, description = "Add role")
    public void testAddRole() throws Exception {

        userManagementClient.addRole("test_Role", new String[]{}, new String[]{"/permission/admin/manage",
                                                                  "/permission/admin/login"});
        String PERMISSION_PATH = "/_system/config/users";
        resourceAdminServiceClient.addResourcePermission(PERMISSION_PATH, "test_Role",
                                                         PermissionTestConstants.READ_ACTION,
                                                         PermissionTestConstants.PERMISSION_ENABLED);
        resourceAdminServiceClient.addResourcePermission(PERMISSION_PATH, "test_Role",
                                                         PermissionTestConstants.WRITE_ACTION,
                                                         PermissionTestConstants.PERMISSION_ENABLED);
        resourceAdminServiceClient.addResourcePermission(PERMISSION_PATH, "test_Role",
                                                         PermissionTestConstants.DELETE_ACTION,
                                                         PermissionTestConstants.PERMISSION_ENABLED);
        resourceAdminServiceClient.addResourcePermission(PERMISSION_PATH, "test_Role",
                                                         PermissionTestConstants.AUTHORIZE_ACTION,
                                                         PermissionTestConstants.PERMISSION_ENABLED);
        Assert.assertTrue(userManagementClient.roleNameExists("test_Role"));

    }

    @Test(groups = {"wso2.greg"}, description = "Add filter", dependsOnMethods = "testAddUser")
    public void testAddFilter() throws Exception {
        addService();
        searchAdminServiceClient =
                new SearchAdminServiceClient(backendURL, USER_NAME, USER_NAME + "pass");

        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setResourceName("testService");

        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        searchAdminServiceClient.saveAdvancedSearchFilter(searchQuery, FILTER_NAME);
        String[] filters = searchAdminServiceClient.getSavedFilters();

        boolean filterFound = false;
        for (String filter : filters) {

            if (filter.equals(FILTER_NAME)) {
                filterFound = true;
                break;
            }
        }
        Assert.assertTrue(filterFound);

    }

    @Test(groups = {"wso2.greg"}, description = "search from filter", dependsOnMethods = "testAddFilter")
    public void testSearchViaFilter() throws SearchAdminServiceRegistryExceptionException,
                                             RemoteException, RegistryException {
        AdvancedSearchResultsBean result =
                searchAdminServiceClient.getAdvancedSearchResults(searchAdminServiceClient.getAdvancedSearchFilter
                        (FILTER_NAME));

        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid property name");
        for (ResourceData resource : result.getResourceDataList()) {
            boolean resultFound = false;

            if (resource.getName().contains("testService")) {
                resultFound = true;
            }

            Assert.assertTrue(resultFound);
        }

    }

    @Test(groups = {"wso2.greg"}, description = "load filter", dependsOnMethods = "testAddFilter")
    public void testLoadFilter()
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        CustomSearchParameterBean bean = searchAdminServiceClient.getAdvancedSearchFilter(FILTER_NAME);
        ArrayOfString[] arrayOfString = bean.getParameterValues();
        boolean nullFound = false;
        boolean paramSet = false;
        for (ArrayOfString arr : arrayOfString) {
            if (arr.getArray()[1] == null || arr.getArray()[1].equals("null")) {
                nullFound = true;
            } else if ((arr.getArray()[0].equals("resourcePath")) && arr.getArray()[1].equals("testService")) {
                paramSet = true;
            }
        }
        Assert.assertFalse(nullFound);
        Assert.assertTrue(paramSet);
    }

    public String addService() throws Exception {

        ServiceManager serviceManager = new ServiceManager(governance);
        Service service;
        service = serviceManager.newService(new QName("https://contoso.accesscontrol.windows.net",
                                                      "testService"));

        serviceManager.addService(service);
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);

        for (String serviceId : serviceManager.getAllServiceIds()) {
            service = serviceManager.getService(serviceId);
            if (service.getPath().endsWith("testService") && service.getPath().contains("trunk")) {
                return service.getPath();

            }

        }
        throw new Exception("Getting Service path failed");

    }

    @AfterClass(alwaysRun = true)
    public void clean() throws Exception {
        searchAdminServiceClient.deleteFilter(FILTER_NAME);

        userManagementClient = new UserManagementClient(backendURL, getSessionCookie());

        delete("/_system/governance/trunk/services/net/windows/accesscontrol/contoso/testService");
        userManagementClient.deleteRole("test_Role");
        userManagementClient.deleteUser(USER_NAME);

        searchAdminServiceClient = null;
        userManagementClient = null;
        resourceAdminServiceClient = null;
        governance = null;

    }

    public void delete(String destPath)
            throws ResourceAdminServiceExceptionException, RemoteException, RegistryException {
        if (wsRegistryServiceClient.resourceExists(destPath)) {
            resourceAdminServiceClient.deleteResource(destPath);
        }
    }


}
