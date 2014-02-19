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
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.api.clients.registry.SearchAdminServiceClient;
import org.wso2.carbon.automation.api.clients.user.mgt.UserManagementClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkSettings;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.permission.test.utils.PermissionTestConstants;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.search.metadata.test.bean.SearchParameterBean;
import org.wso2.carbon.registry.search.stub.SearchAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.search.stub.beans.xsd.AdvancedSearchResultsBean;
import org.wso2.carbon.registry.search.stub.beans.xsd.ArrayOfString;
import org.wso2.carbon.registry.search.stub.beans.xsd.CustomSearchParameterBean;
import org.wso2.carbon.registry.search.stub.common.xsd.ResourceData;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import javax.xml.namespace.QName;
import java.rmi.RemoteException;

public class SaveSearchTestCase {

    private SearchAdminServiceClient searchAdminServiceClient;
    private Registry governance;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private UserManagementClient userManagementClient;
    private ManageEnvironment environment;
    private UserInfo userInfo;
    private final String FILTER_NAME = "serviceSearch";
    private WSRegistryServiceClient wsRegistryServiceClient;
    private UserInfo newUser;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        int userId = 2;
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        environment = builder.build();
        userInfo = UserListCsvReader.getUserInfo(userId);
        userManagementClient = new UserManagementClient(environment.getGreg().getBackEndUrl(),
                                                        environment.getGreg().getSessionCookie());
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                               environment.getGreg().getSessionCookie());
        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        wsRegistryServiceClient =
                registryProviderUtil.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        governance = registryProviderUtil.getGovernanceRegistry(wsRegistryServiceClient, userId);

    }

    @Test(groups = {"wso2.greg"}, description = "Add user", dependsOnMethods = "testAddRole")
    public void testAddUser() throws Exception {
        EnvironmentBuilder environmentBuilder = new EnvironmentBuilder();
        FrameworkSettings framework = environmentBuilder.getFrameworkSettings();
        if (framework.getEnvironmentSettings().is_runningOnStratos()) {
            newUser = new UserInfo("testUser00001" + '@' + userInfo.getDomain(), "testUser00001",
                                   userInfo.getDomain());
        } else {
            newUser = new UserInfo("testUser00001", "testUser00001", userInfo.getDomain());
        }

        userManagementClient.addUser(newUser.getUserNameWithoutDomain(), newUser.getPassword(),
                                     new String[]{"test_Role"}, "testUser00001");

        Assert.assertTrue(userManagementClient.userNameExists("test_Role", newUser.getUserName()));
    }

    @Test(groups = {"wso2.greg"}, description = "Add role")
    public void testAddRole() throws Exception {
        userManagementClient.addRole("test_Role", new String[]{}, new String[]{"/permission/admin/manage", "/permission/admin/login"});
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
                new SearchAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                             newUser.getUserName(), newUser.getPassword());
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
        userManagementClient = new UserManagementClient(environment.getGreg().getBackEndUrl(),
                                                        userInfo.getUserName(), userInfo.getPassword());
        delete("/_system/governance/trunk/services/net/windows/" +
               "accesscontrol/contoso/testService");
        userManagementClient.deleteRole("test_Role");
        userManagementClient.deleteUser(newUser.getUserNameWithoutDomain());

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
