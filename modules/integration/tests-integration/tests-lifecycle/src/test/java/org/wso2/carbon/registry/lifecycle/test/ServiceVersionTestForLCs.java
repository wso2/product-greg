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


package org.wso2.carbon.registry.lifecycle.test;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.beans.xsd.LifecycleBean;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.LifecycleActions;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.integration.common.admin.client.UserManagementClient;
import org.wso2.carbon.registry.activities.stub.RegistryExceptionException;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.lifecycle.test.utils.LifeCycleUtils;
import org.wso2.carbon.registry.lifecycle.test.bean.SearchParameterBean;
import org.wso2.carbon.registry.search.stub.SearchAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.search.stub.beans.xsd.AdvancedSearchResultsBean;
import org.wso2.carbon.registry.search.stub.beans.xsd.ArrayOfString;
import org.wso2.carbon.registry.search.stub.beans.xsd.CustomSearchParameterBean;
import org.wso2.carbon.registry.search.stub.common.xsd.ResourceData;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.carbon.user.mgt.common.UserAdminException;
import org.wso2.greg.integration.common.clients.LifeCycleAdminServiceClient;
import org.wso2.greg.integration.common.clients.LifeCycleManagementClient;
import org.wso2.greg.integration.common.clients.SearchAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;
import java.io.IOException;
import java.rmi.RemoteException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class ServiceVersionTestForLCs extends GREGIntegrationBaseTest{

    private WSRegistryServiceClient wsRegistryServiceClient;
    private RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();

    private LifeCycleAdminServiceClient lifeCycleAdminServiceClient;
    private LifeCycleManagementClient lifeCycleManagementClient;
    private SearchAdminServiceClient searchAdminService;
    private Registry governance;
    private String servicePathDev;
    private UserManagementClient userManger;

    private final String ASPECT_NAME = "IntergalacticServiceLC";
    private final String ACTION_PROMOTE = "Promote";
    private final String ACTION_ITEM_CLICK = "itemClick";

    private String userNameWithoutDomain;

    @BeforeClass (alwaysRun = true)
    public void init() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_USER);
        String sessionCookie = getSessionCookie();

        lifeCycleAdminServiceClient =
                new LifeCycleAdminServiceClient(backendURL,sessionCookie);


        lifeCycleManagementClient =
                new LifeCycleManagementClient(backendURL,sessionCookie);
        wsRegistryServiceClient =
                registryProviderUtil.getWSRegistry(automationContext);

        governance = registryProviderUtil.getGovernanceRegistry(wsRegistryServiceClient, automationContext);

        searchAdminService = new SearchAdminServiceClient(backendURL, sessionCookie);

        userManger = new UserManagementClient( backendURL, sessionCookie);

        String userName = automationContext.getContextTenant().getContextUser().getUserName();
        userNameWithoutDomain = userName.substring(0, userName.indexOf('@'));

    }

    @Test(groups = "wso2.greg", description = "Add Service with service_version")
    public void testAddService() throws Exception {
        String serviceName = "CustomLifeCycleTestService";
        if(!lifeCycleManagementClient.isLifecycleNameInUse(ASPECT_NAME)) {
            LifeCycleUtils.deleteLifeCycleIfExist(ASPECT_NAME, lifeCycleManagementClient);
        }
        servicePathDev = "/_system/governance" + LifeCycleUtils.addService("sns", serviceName, "1.2.3", governance);

    }

    @Test(groups = "wso2.greg", description = "Add new Life Cycle", dependsOnMethods = "testAddService")
    public void createNewLifeCycle()
            throws IOException, LifeCycleManagementServiceExceptionException, InterruptedException,
                   SearchAdminServiceRegistryExceptionException {
        LifeCycleUtils.createNewLifeCycle("IntergalacticServiceLC", lifeCycleManagementClient);

        //Metadata Search By Life Cycle Name
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setResourceName(ASPECT_NAME);
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminService.getAdvancedSearchResults(searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length == 1), "No Record Found for Life Cycle " +
                                                                      "Name or more record found");
        for (ResourceData resource : result.getResourceDataList()) {
            Assert.assertEquals(resource.getName(), ASPECT_NAME,
                                "Life Cycle Name mismatched :" + resource.getResourcePath());
            Assert.assertTrue(resource.getResourcePath().contains("lifecycles"),
                              "Life Cycle Path does not contain lifecycles collection :" + resource.getResourcePath());
        }
    }


    @Test(groups = "wso2.greg", description = "Add LifeCycle to a service", dependsOnMethods = "createNewLifeCycle")
    public void addLifeCycleToService()
            throws RegistryException, InterruptedException,
                   CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException,
                   RegistryExceptionException {
        wsRegistryServiceClient.associateAspect(servicePathDev, ASPECT_NAME);

        LifecycleBean lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(servicePathDev);
        Resource service = wsRegistryServiceClient.get(servicePathDev);
        Assert.assertNotNull(service, "Service Not found on registry path " + servicePathDev);
        assertEquals(service.getPath(), servicePathDev, "Service path changed after adding life cycle. " + servicePathDev);
        assertEquals(getLifeCycleState(lifeCycle), "Commencement",
                     "LifeCycle State Mismatched");

        //life cycle check list
        Assert.assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(),
                                                                "registry.custom_lifecycle.checklist.option.0.item")[1],
                            "name:Requirements Gathered", "Requirements Gathered Check List Item Not Found");
        Assert.assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(),
                                                                "registry.custom_lifecycle.checklist.option.1.item")[1],
                            "name:Document Requirements", "Document Requirements Check List Item Not Found");
        Assert.assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(),
                                                                "registry.custom_lifecycle.checklist.option.2.item")[1],
                            "name:Architecture Diagram Finalized", "Architecture Diagram Finalize Check List Item Not Found");
        Assert.assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(),
                                                                "registry.custom_lifecycle.checklist.option.3.item")[1],
                            "name:Design UML Diagrams", "Design UML Diagrams Check List Item Not Found");
        Assert.assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(),
                                                                "registry.custom_lifecycle.checklist.option.4.item")[1]
                , "name:High Level Design Completed", "High Level Design Completed Check List Item Not Found");
        Assert.assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(),
                                                                "registry.custom_lifecycle.checklist.option.5.item")[1]
                , "name:Completion of Commencement", "Completion of Commencement  Check List Item Not Found");

    }

    @Test(groups = "wso2.greg", description = "Click Check List Item", dependsOnMethods = {"addLifeCycleToService"})
    public void clickCommencementCheckList()
            throws Exception,
                   UserAdminException {
        LifecycleBean lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(servicePathDev);
        String[] actions;
        LifecycleActions[] availableActions = lifeCycle.getAvailableActions();
        Assert.assertEquals(availableActions.length, 1, "Available Action count mismatched");
        actions = availableActions[0].getActions();
        Assert.assertNull(actions, "Available Action found");

        addRole("archrole");
        lifeCycleAdminServiceClient.invokeAspect(servicePathDev, ASPECT_NAME, ACTION_ITEM_CLICK,
                                                 new String[]{"true", "false", "false", "false", "false"});
        lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(servicePathDev);
        Assert.assertEquals(availableActions.length, 1, "Available Action count mismatched");
        availableActions = lifeCycle.getAvailableActions();
        actions = availableActions[0].getActions();
        Assert.assertNull(actions, "Available Action found");

        lifeCycleAdminServiceClient.invokeAspect(servicePathDev, ASPECT_NAME, ACTION_ITEM_CLICK,
                                                 new String[]{"true", "true", "true", "true", "true", "true"});
        lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(servicePathDev);
        availableActions = lifeCycle.getAvailableActions();
        actions = availableActions[0].getActions();
        Assert.assertNotNull(actions, "Available Action Not found");
        Assert.assertEquals(actions.length, 1, "Action not found");
        Assert.assertEquals(actions[0], "Abort", "Abort Action not found");

        addRole("managerrole");
        lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(servicePathDev);
        availableActions = lifeCycle.getAvailableActions();
        actions = availableActions[0].getActions();
        Assert.assertNotNull(actions, "Available Action Not found");
        Assert.assertEquals(actions.length, 2, "Action not found");
        Assert.assertEquals(actions[0], "Promote", "Promote Action not found");
        Assert.assertEquals(actions[1], "Abort", "Abort Action not found");

    }

    @Test(groups = "wso2.greg", description = "Promote Service to Creation", dependsOnMethods = {"clickCommencementCheckList"})
    public void promoteServiceToCreation()
            throws InterruptedException, CustomLifecyclesChecklistAdminServiceExceptionException,
                   RemoteException, RegistryException, RegistryExceptionException {
        Thread.sleep(1000);
        org.wso2.carbon.governance.custom.lifecycles.checklist.stub.services.ArrayOfString[] parameters =
                new org.wso2.carbon.governance.custom.lifecycles.checklist.stub.services.ArrayOfString[2];
        parameters[0] = new org.wso2.carbon.governance.custom.lifecycles.checklist.stub.services.ArrayOfString();
        parameters[0].setArray(new String[]{servicePathDev, "1.0.0"});

        parameters[1] = new org.wso2.carbon.governance.custom.lifecycles.checklist.stub.services.ArrayOfString();
        parameters[1].setArray(new String[]{"preserveOriginal", "true"});

        lifeCycleAdminServiceClient.invokeAspectWithParams(servicePathDev, ASPECT_NAME,
                                                           ACTION_PROMOTE, null, parameters);


        LifecycleBean lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(servicePathDev);
        Resource service = wsRegistryServiceClient.get(servicePathDev);
        Assert.assertNotNull(service, "Service Not found on registry path " + servicePathDev);
        Assert.assertEquals(service.getPath(), servicePathDev, "Service not in branches/testing. " + servicePathDev);


        Assert.assertEquals(wsRegistryServiceClient.get(servicePathDev).getPath(), servicePathDev, "Preserve original failed");

        //life cycle check list
        Assert.assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(),
                                                                "registry.custom_lifecycle.checklist.option.0.item")[1],
                            "name:Code Completed", "Code Completed Check List Item Not Found");
        Assert.assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(),
                                                                "registry.custom_lifecycle.checklist.option.1.item")[1],
                            "name:WSDL Created", "WSDL Created Check List Item Not Found");
        Assert.assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(),
                                                                "registry.custom_lifecycle.checklist.option.2.item")[1],
                            "name:QoS Created", "QoS Created Check List Item Not Found");
        Assert.assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(),
                                                                "registry.custom_lifecycle.checklist.option.3.item")[1],
                            "name:Schema Created", "Schema Created Check List Item Not Found");
        Assert.assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(),
                                                                "registry.custom_lifecycle.checklist.option.4.item")[1],
                            "name:Services Created", "Services Created Check List Item Not Found");
        Assert.assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(),
                                                                "registry.custom_lifecycle.checklist.option.5.item")[1],
                            "name:Completion of Creation", "Completion of Creation  Check List Item Not Found");
    }


    @Test(groups = "wso2.greg", description = "Test Service Version After Promoting",
          dependsOnMethods = {"promoteServiceToCreation"})
    public void testServiceVersion() throws RegistryException {
        ServiceManager serviceManager = new ServiceManager(governance);
        Service[] service;
        service = serviceManager.getAllServices();
        Boolean versionIsSet = false;
        for (Service s : service) {
            if (s.getAttribute("overview_version").equalsIgnoreCase("1.2.3")) {
                versionIsSet = true;
            }
        }
        assertTrue(versionIsSet = true, "versionIsSet is not set");


    }


    @AfterClass
    public void deleteLifeCycle() throws Exception {
        if (servicePathDev != null) {
            wsRegistryServiceClient.delete(servicePathDev);
        }
        if(!lifeCycleManagementClient.isLifecycleNameInUse(ASPECT_NAME)) {
            Assert.assertTrue(lifeCycleManagementClient.deleteLifeCycle(ASPECT_NAME),
                    "Life Cycle Deleted failed");
        }
        Thread.sleep(2000);
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setResourceName(ASPECT_NAME);
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminService.getAdvancedSearchResults(searchQuery);
        Assert.assertNull(result.getResourceDataList(), "Life Cycle Record Found even if it is deleted");

        deleteRolesIfExist();
        wsRegistryServiceClient = null;
        lifeCycleAdminServiceClient = null;
        userManger = null;
        lifeCycleManagementClient = null;
    }

    public static String getLifeCycleState(LifecycleBean lifeCycle) {
        Assert.assertTrue((lifeCycle.getLifecycleProperties().length > 0), "LifeCycle properties missing some properties");
        String state = null;
        boolean stateFound = false;
        for (Property prop : lifeCycle.getLifecycleProperties()) {
            if ("registry.lifecycle.IntergalacticServiceLC.state".equalsIgnoreCase(prop.getKey())) {
                stateFound = true;
                Assert.assertNotNull(prop.getValues(), "State Value Not Found");
                state = prop.getValues()[0];

            }
        }
        Assert.assertTrue(stateFound, "LifeCycle State property not found");
        return state;
    }

    private void deleteRolesIfExist()
            throws Exception {

        if (userManger.roleNameExists("archrole")) {
            userManger.deleteRole("archrole");
        }

        if (userManger.roleNameExists("managerrole")) {
            userManger.deleteRole("managerrole");
        }


    }

    private void addRole(String roleName) throws Exception {
        String[] permissions = {"/permission/admin/manage/resources"};
        userManger.addRole(roleName, new String[]{userNameWithoutDomain}, permissions);

    }
}
