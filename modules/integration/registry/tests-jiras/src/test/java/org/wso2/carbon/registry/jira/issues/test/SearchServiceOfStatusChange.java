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
import org.wso2.carbon.automation.api.clients.governance.LifeCycleAdminServiceClient;
import org.wso2.carbon.automation.api.clients.governance.LifeCycleManagementClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.api.clients.registry.SearchAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.core.utils.fileutils.FileManager;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.beans.xsd.LifecycleBean;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.services.ArrayOfString;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.search.metadata.test.bean.SearchParameterBean;
import org.wso2.carbon.registry.search.stub.SearchAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.search.stub.beans.xsd.AdvancedSearchResultsBean;
import org.wso2.carbon.registry.search.stub.beans.xsd.CustomSearchParameterBean;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;

public class SearchServiceOfStatusChange {

    private Registry governance;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private String pathPrefix = "/_system/governance";
    private RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
    private SearchAdminServiceClient searchAdminServiceClient;
    private LifeCycleManagementClient lifeCycleManagementClient;
    private WSRegistryServiceClient wsRegistryServiceClient;
    private LifeCycleAdminServiceClient lifeCycleAdminServiceClient;
    private String servicePath;
    private String newPath;
    private String productionPath;
    private String testingPath;
    private final String SERVICE_NAME = "test_name";
    private final String LC_NAME = "DiffEnvironmentLC";


    @BeforeClass
    public void init() throws Exception {
        int userId = ProductConstant.ADMIN_USER_ID;
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        ManageEnvironment environment = builder.build();
        UserInfo userInfo = UserListCsvReader.getUserInfo(userId);
        lifeCycleManagementClient =
                new LifeCycleManagementClient(environment.getGreg().getBackEndUrl(),
                                              environment.getGreg().getSessionCookie());
        searchAdminServiceClient =
                new SearchAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                             environment.getGreg().getSessionCookie());
        wsRegistryServiceClient =
                registryProviderUtil.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);

        resourceAdminServiceClient =
                new ResourceAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                               environment.getGreg().getSessionCookie());
        lifeCycleAdminServiceClient =
                new LifeCycleAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                                environment.getGreg().getSessionCookie());

        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        governance = registryProviderUtil.getGovernanceRegistry(wsRegistryServiceClient, userId);

    }

    @Test(groups = {"wso2.greg"}, description = "create lifecycle")
    public void testCreateLc() throws GovernanceException, IOException,
                                      LifeCycleManagementServiceExceptionException {

        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
                              File.separator + "GREG" + File.separator + "lifecycle" + File.separator +
                              "MultiplePromoteDemoteLCViewVersionsTrue.xml";
        String lifeCycleContent = FileManager.readFile(resourcePath);
        lifeCycleManagementClient.addLifeCycle(lifeCycleContent);

        String[] lifeClycles = lifeCycleManagementClient.getLifecycleList();
        boolean lcCreated = false;
        for (String lc : lifeClycles) {
            if (lc.equals(LC_NAME)) {
                lcCreated = true;
            }
        }

        Assert.assertTrue(lcCreated);

    }

    @Test(groups = {"wso2.greg"}, description = "add lifecycle to service", dependsOnMethods = "testCreateLc")
    public void testAddLC()
            throws RegistryException, CustomLifecyclesChecklistAdminServiceExceptionException,
                   RemoteException {
        servicePath = addService();
        wsRegistryServiceClient.associateAspect(pathPrefix + servicePath, LC_NAME);
        LifecycleBean lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(pathPrefix + servicePath);
        Property[] properties = lifeCycle.getLifecycleProperties();
        boolean lcAdded = false;
        for (Property prop : properties) {
            if (prop.getKey().contains(LC_NAME)) {
                lcAdded = true;
            }
        }
        Assert.assertTrue(lcAdded);
    }

    @Test(groups = {"wso2.greg"}, description = "promote lifecycle", dependsOnMethods = "testAddLC")
    public void testPromote()
            throws RegistryException, CustomLifecyclesChecklistAdminServiceExceptionException,
                   RemoteException {
        testingPath = promoteService("1.0.1", pathPrefix + servicePath, "testing");
        productionPath = promoteService("1.1.0", testingPath, "production");

    }

    @Test(groups = {"wso2.greg"}, description = "search service", dependsOnMethods = "testAddLC")
    public void testSearchService()
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setResourceName(SERVICE_NAME);
        boolean resourceExists = false;
        boolean testExists = false;
        org.wso2.carbon.registry.search.stub.beans.xsd.ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        paramList = null;
        AdvancedSearchResultsBean result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0));

        for (org.wso2.carbon.registry.search.stub.common.xsd.ResourceData resource : result.getResourceDataList()) {
            if (resource.getResourcePath().equals(productionPath)) {
                resourceExists = true;
                break;
            }
            if (resource.getResourcePath().equals(testingPath)) {
                testExists = true;
            }

        }
        Assert.assertTrue(resourceExists);
        Assert.assertFalse(testExists);
    }

    public String promoteService(String version, String currentPath, String promoteStatus)
            throws CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException,
                   RegistryException {
        ArrayOfString[] parameters = new ArrayOfString[2];
        String[] dependencyList;
        dependencyList = lifeCycleAdminServiceClient.getAllDependencies(currentPath);

        parameters[0] = new ArrayOfString();
        parameters[0].setArray(new String[]{dependencyList[0], version});
        parameters[1] = new ArrayOfString();
        parameters[1].setArray(new String[]{"preserveOriginal", "false"});

        String ACTION_PROMOTE = "Promote";
        String ASPECT_NAME = "DiffEnvironmentLC";
        lifeCycleAdminServiceClient.invokeAspectWithParams(currentPath, ASPECT_NAME, ACTION_PROMOTE, null, parameters);

        newPath = "/_system/governance/branches/" + promoteStatus + "/services/test_namespace/" + version + "/test_name";

        Resource service = wsRegistryServiceClient.get(newPath);
        Assert.assertNotNull(service, "Service Not found on registry path " + newPath);
        Assert.assertEquals(service.getPath(), newPath, "Service not in branches/testing. " + newPath);

        return newPath;
    }


    public String addService() throws GovernanceException {
        ServiceManager serviceManager = new ServiceManager(governance);
        Service service;
        service = serviceManager.newService(new QName("test_namespace", SERVICE_NAME));
        service.addAttribute("overview_version", "1.0.0");
        serviceManager.addService(service);
        return service.getPath();
    }

    @AfterClass
    public void clean() throws ResourceAdminServiceExceptionException, RemoteException,
                               LifeCycleManagementServiceExceptionException, RegistryException {
        delete(newPath);
        lifeCycleManagementClient.deleteLifeCycle(LC_NAME);

        governance = null;
        resourceAdminServiceClient = null;
        registryProviderUtil = null;
        searchAdminServiceClient = null;
        lifeCycleManagementClient = null;
        wsRegistryServiceClient = null;
        lifeCycleAdminServiceClient = null;
    }

    public void delete(String destPath)
            throws ResourceAdminServiceExceptionException, RemoteException, RegistryException {
        if (wsRegistryServiceClient.resourceExists(destPath)) {
            resourceAdminServiceClient.deleteResource(destPath);
        }
    }
}
