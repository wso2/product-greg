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
import org.wso2.carbon.automation.api.clients.governance.LifeCycleAdminServiceClient;
import org.wso2.carbon.automation.api.clients.governance.LifeCycleManagementClient;
import org.wso2.carbon.automation.api.clients.registry.ActivityAdminServiceClient;
import org.wso2.carbon.automation.api.clients.user.mgt.UserManagementClient;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.beans.xsd.LifecycleBean;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.services.ArrayOfString;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.LifecycleActions;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.registry.activities.stub.RegistryExceptionException;
import org.wso2.carbon.registry.activities.stub.beans.xsd.ActivityBean;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.lifecycle.test.utils.Utils;
import org.wso2.carbon.registry.search.metadata.test.utils.GregTestUtils;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.carbon.user.mgt.common.UserAdminException;

import java.rmi.RemoteException;
import java.util.Calendar;

public class CustomLifeCycleDemoteForWSDLTestCase {
    private String sessionCookie;

    private WSRegistryServiceClient registry;
    private LifeCycleAdminServiceClient lifeCycleAdminService;
    private LifeCycleManagementClient lifeCycleManagerAdminService;
    private ActivityAdminServiceClient activitySearch;
    private UserManagementClient userManger;
    private String userName;
    private String serviceName = "echoServiceForDemote.wsdl";
    private final String ASPECT_NAME = "CustomServiceLifeCycle";
    private final String ACTION_PROMOTE = "Promote";
    private final String ACTION_DEMOTE = "Demote";
    private final String ACTION_ITEM_CLICK = "itemClick";
    private final String ASS_TYPE_DEPENDS = "depends";
    private String servicePathTrunk;
    private String servicePathTrunkDemote;
    private String servicePathBranchDev;
    private String[] dependencyList = null;

    @BeforeClass
    public void init() throws Exception {
        ClientConnectionUtil.waitForPort(Integer.parseInt(FrameworkSettings.HTTP_PORT));
        sessionCookie = new LoginLogoutUtil().login();
        final String SERVER_URL = GregTestUtils.getServerUrl();
        userName = FrameworkSettings.USER_NAME;
        lifeCycleAdminService = new LifeCycleAdminServiceClient(SERVER_URL, sessionCookie);
        activitySearch = new ActivityAdminServiceClient(SERVER_URL, sessionCookie);
        lifeCycleManagerAdminService = new LifeCycleManagementClient(SERVER_URL, sessionCookie);
        userManger = new UserManagementClient(SERVER_URL, sessionCookie);
        registry = GregTestUtils.getRegistry();
        Registry governance = GregTestUtils.getGovernanceRegistry(registry);
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);

        deleteRolesIfExist();

        Utils.deleteLifeCycleIfExist(sessionCookie, ASPECT_NAME, lifeCycleManagerAdminService);
        String wsdlPath = "/_system/governance" + Utils.addWSDL("echoServiceWsdl.wsdl", governance, serviceName);
        Association[] usedBy = registry.getAssociations(wsdlPath, "usedBy");
        Assert.assertNotNull(usedBy, "WSDL usedBy Association type not found");
        for (Association association : usedBy) {
            if (association.getSourcePath().equalsIgnoreCase(wsdlPath)) {
                servicePathTrunk = association.getDestinationPath();
            }
        }
        Thread.sleep(1000);

        Utils.createNewLifeCycle(ASPECT_NAME, lifeCycleManagerAdminService);

        Assert.assertNotNull(servicePathTrunk, "Service Not Found associate with WSDL");

        Thread.sleep(1000);
        Association[] dependency = registry.getAssociations(servicePathTrunk, ASS_TYPE_DEPENDS);

        Assert.assertNotNull(dependency, "Dependency Not Found.");
        Assert.assertTrue(dependency.length > 0, "Dependency list empty");
        Assert.assertEquals(dependency.length, 7, "some dependency missing or additional dependency found.");


    }

    @Test(priority = 1, description = "Add lifecycle to a service")
    public void addLifecycle()
            throws RegistryException, CustomLifecyclesChecklistAdminServiceExceptionException,
                   RemoteException, InterruptedException {
        registry.associateAspect(servicePathTrunk, ASPECT_NAME);
        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathTrunk);
        Resource service = registry.get(servicePathTrunk);
        Assert.assertNotNull(service, "Service Not found on registry path " + servicePathTrunk);
        Assert.assertTrue(service.getPath().contains("trunk"), "Service not in trunk. " + servicePathTrunk);

        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties()
                , "registry.lifecycle.CustomServiceLifeCycle.state")[0], "Commencement",
                            "LifeCycle State Mismatched");
        dependencyList = lifeCycleAdminService.getAllDependencies(servicePathTrunk);
        Assert.assertNotNull(dependencyList, "Dependency List Not Found");
        Assert.assertEquals(dependencyList.length, 8, "Dependency Count mismatched");

    }

    @Test(description = "Click Check List Item", dependsOnMethods = {"addLifecycle"})
    public void clickCommencementCheckList()
            throws Exception, RemoteException,
                   UserAdminException {
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathTrunk);
        String[] actions;
        LifecycleActions[] availableActions = lifeCycle.getAvailableActions();
        Assert.assertEquals(availableActions.length, 1, "Available Action count mismatched");
        actions = availableActions[0].getActions();
        Assert.assertNull(actions, "Available Action found");

        addRole("archrole");
        lifeCycleAdminService.invokeAspect(servicePathTrunk, ASPECT_NAME, ACTION_ITEM_CLICK,
                                           new String[]{"true", "false", "false", "false", "false"});
        lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathTrunk);
        Assert.assertEquals(availableActions.length, 1, "Available Action count mismatched");
        availableActions = lifeCycle.getAvailableActions();
        actions = availableActions[0].getActions();
        Assert.assertNull(actions, "Available Action found");

        lifeCycleAdminService.invokeAspect(servicePathTrunk, ASPECT_NAME, ACTION_ITEM_CLICK,
                                           new String[]{"true", "true", "true", "true", "true", "true"});
        lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathTrunk);
        availableActions = lifeCycle.getAvailableActions();
        actions = availableActions[0].getActions();
        Assert.assertNotNull(actions, "Available Action Not found");
        Assert.assertEquals(actions.length, 1, "Action not found");
        Assert.assertEquals(actions[0], "Abort", "Abort Action not found");

        addRole("managerrole");
        lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathTrunk);
        availableActions = lifeCycle.getAvailableActions();
        actions = availableActions[0].getActions();
        Assert.assertNotNull(actions, "Available Action Not found");
        Assert.assertEquals(actions.length, 2, "Action not found");
        Assert.assertEquals(actions[0], "Promote", "Promote Action not found");
        Assert.assertEquals(actions[1], "Abort", "Abort Action not found");

    }

    @Test(description = "Promote Service to Creation", dependsOnMethods = {"clickCommencementCheckList"})
    public void promoteServiceToCreation()
            throws InterruptedException, CustomLifecyclesChecklistAdminServiceExceptionException,
                   RemoteException, RegistryException, RegistryExceptionException {
        Thread.sleep(1000);
        ArrayOfString[] parameters = new ArrayOfString[8];

        for (int i = 0; i < dependencyList.length; i++) {
            parameters[i] = new ArrayOfString();
            parameters[i].setArray(new String[]{dependencyList[i], "1.0.0"});

        }

        parameters[7] = new ArrayOfString();
        parameters[7].setArray(new String[]{"preserveOriginal", "true"});

        lifeCycleAdminService.invokeAspectWithParams(servicePathTrunk, ASPECT_NAME,
                                                     ACTION_PROMOTE, null, parameters);

        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathTrunk);
        Resource service = registry.get(servicePathTrunk);
        Assert.assertNotNull(service, "Service Not found on registry path " + servicePathTrunk);
        Assert.assertEquals(service.getPath(), servicePathTrunk, "Service not in branches/testing. " + servicePathTrunk);

        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.lifecycle.CustomServiceLifeCycle.state")[0]
                , "Creation", "LifeCycle State Mismatched");

        Assert.assertEquals(registry.get(servicePathTrunk).getPath(), servicePathTrunk, "Preserve original failed");

        //life cycle check list
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.0.item")[1],
                            "name:Code Completed", "Code Completed Check List Item Not Found");
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.1.item")[1],
                            "name:WSDL Created", "WSDL Created Check List Item Not Found");
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.2.item")[1],
                            "name:QoS Created", "QoS Created Check List Item Not Found");
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.3.item")[1],
                            "name:Schema Created", "Schema Created Check List Item Not Found");
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.4.item")[1],
                            "name:Services Created", "Services Created Check List Item Not Found");
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.5.item")[1],
                            "name:Completion of Creation", "Completion of Creation  Check List Item Not Found");

        //activity search for trunk
        Thread.sleep(1000 * 10);
        ActivityBean activityObjTrunk = activitySearch.getActivities(sessionCookie, userName
                , servicePathTrunk, Utils.formatDate(Calendar.getInstance().getTime())
                , "", ActivityAdminServiceClient.FILTER_RESOURCE_UPDATE, 1);
        Assert.assertNotNull(activityObjTrunk, "Activity object null in trunk");
        Assert.assertNotNull(activityObjTrunk.getActivity(), "Activity list object null");
        Assert.assertTrue((activityObjTrunk.getActivity().length > 0), "Activity list object null");
        String activity = activityObjTrunk.getActivity()[0];
        Assert.assertTrue(activity.contains(userName), "Activity not found. User name not found on last activity. " + activity);
        Assert.assertTrue(activity.contains("has updated the resource"),
                          "Activity not found. has updated not contain in last activity. " + activity);
        Assert.assertTrue(activity.contains("0m ago"), "Activity not found. current time not found on last activity. " + activity);


    }

    @Test(description = "click check list in creation stage", dependsOnMethods = {"promoteServiceToCreation"})
    public void clickCreationCheckListToEnableDemote()
            throws CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException,
                   UserAdminException {
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathTrunk);
        String[] actions;
        LifecycleActions[] availableActions = lifeCycle.getAvailableActions();
        Assert.assertEquals(availableActions.length, 1, "Available Action count mismatched");
        actions = availableActions[0].getActions();
        Assert.assertNull(actions, "Available Action found");

        lifeCycleAdminService.invokeAspect(servicePathTrunk, ASPECT_NAME, ACTION_ITEM_CLICK,
                                           new String[]{"true", "false", "false", "false", "false"});
        lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathTrunk);
        Assert.assertEquals(availableActions.length, 1, "Available Action count mismatched");
        availableActions = lifeCycle.getAvailableActions();
        actions = availableActions[0].getActions();
        Assert.assertNull(actions, "Available Action found");

        //check demote action
        lifeCycleAdminService.invokeAspect(servicePathTrunk, ASPECT_NAME, ACTION_ITEM_CLICK,
                                           new String[]{"true", "true", "true", "true", "true", "false"});
        lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathTrunk);
        availableActions = lifeCycle.getAvailableActions();
        actions = availableActions[0].getActions();
        Assert.assertNotNull(actions, "Available Action Not found");
        Assert.assertEquals(actions.length, 1, "Action not found");
        Assert.assertEquals(actions[0], "Demote", "Demote Action not found");
    }

    @Test(description = "Demote Service to Commencement", dependsOnMethods = {"clickCreationCheckListToEnableDemote"})
    public void demoteServiceToCommencement()
            throws InterruptedException, CustomLifecyclesChecklistAdminServiceExceptionException,
                   RemoteException, RegistryException, RegistryExceptionException {
        Thread.sleep(1000);

        lifeCycleAdminService.invokeAspect(servicePathTrunk, ASPECT_NAME,
                                           ACTION_DEMOTE, null);
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathTrunk);
        Resource service = registry.get(servicePathTrunk);
        Assert.assertNotNull(service, "Service Not found on registry path " + servicePathTrunk);
        Assert.assertTrue(service.getPath().contains("trunk"), "Service not in trunk. " + servicePathTrunk);

        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties()
                , "registry.lifecycle.CustomServiceLifeCycle.state")[0], "Commencement",
                            "LifeCycle State Mismatched");

        dependencyList = lifeCycleAdminService.getAllDependencies(servicePathTrunk);
        Assert.assertNotNull(dependencyList, "Dependency List Not Found");
        Assert.assertEquals(dependencyList.length, 8, "Dependency Count mismatched");

        //life cycle check list
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.0.item")[1],
                            "name:Requirements Gathered", "Requirements Gathered Check List Item Not Found");
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.0.item")[2],
                            "value:false", "Requirements Gathered Check List checked");

        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.1.item")[1],
                            "name:Document Requirements", "Document Requirements Check List Item Not Found");
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.1.item")[2],
                            "value:false", "Document Requirements Check List checked");

        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.2.item")[1],
                            "name:Architecture Diagram Finalized", "Architecture Diagram Finalize Check List Item Not Found");
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.2.item")[2],
                            "value:false", "Architecture Diagram Finalize Check List checked");

        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.3.item")[1],
                            "name:Design UML Diagrams", "Design UML Diagrams Check List Item Not Found");
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.3.item")[2],
                            "value:false", "Design UML Diagrams Check List checked");

        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.4.item")[1]
                , "name:High Level Design Completed", "High Level Design Completed Check List Item Not Found");
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.4.item")[2]
                , "value:false", "High Level Design Completed Check List checked");

        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.5.item")[1]
                , "name:Completion of Commencement", "Completion of Commencement  Check List Item Not Found");
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.5.item")[2]
                , "value:false", "Completion of Commencement  Check List checked");

        //Activity search
        Thread.sleep(1000 * 10);
        ActivityBean activityObjTrunk = activitySearch.getActivities(sessionCookie, userName
                , servicePathTrunk, Utils.formatDate(Calendar.getInstance().getTime())
                , "", ActivityAdminServiceClient.FILTER_RESOURCE_UPDATE, 1);
        Assert.assertNotNull(activityObjTrunk, "Activity object null in trunk");
        Assert.assertNotNull(activityObjTrunk.getActivity(), "Activity list object null");
        Assert.assertTrue((activityObjTrunk.getActivity().length > 0), "Activity list object null");
        String activity = activityObjTrunk.getActivity()[0];
        Assert.assertTrue(activity.contains(userName), "Activity not found. User name not found on last activity. " + activity);
        Assert.assertTrue(activity.contains("has updated the resource"),
                          "Activity not found. has updated not contain in last activity. " + activity);
        Assert.assertTrue(activity.contains("0m ago"), "Activity not found. current time not found on last activity. " + activity);
    }

    @Test(description = "Promote Service to Creation", dependsOnMethods = {"demoteServiceToCommencement"})
    public void promoteServiceAgainToCreation()
            throws InterruptedException, CustomLifecyclesChecklistAdminServiceExceptionException,
                   RemoteException, RegistryException, RegistryExceptionException {
        Thread.sleep(1000);
        lifeCycleAdminService.invokeAspect(servicePathTrunk, ASPECT_NAME, ACTION_ITEM_CLICK,
                                           new String[]{"true", "true", "true", "true", "true", "true"});
        Thread.sleep(1000);
        lifeCycleAdminService.invokeAspect(servicePathTrunk, ASPECT_NAME,
                                           ACTION_PROMOTE, null);

        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathTrunk);
        Resource service = registry.get(servicePathTrunk);
        Assert.assertNotNull(service, "Service Not found on registry path " + servicePathTrunk);
        Assert.assertEquals(service.getPath(), servicePathTrunk, "Service not in branches/testing. " + servicePathTrunk);

        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.lifecycle.CustomServiceLifeCycle.state")[0]
                , "Creation", "LifeCycle State Mismatched");

        dependencyList = lifeCycleAdminService.getAllDependencies(servicePathTrunk);
        Assert.assertNotNull(dependencyList, "Dependency List Not Found");
        Assert.assertEquals(dependencyList.length, 8, "Dependency Count mismatched");

        Assert.assertEquals(registry.get(servicePathTrunk).getPath(), servicePathTrunk, "Preserve original failed");

        //life cycle check list
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.0.item")[1],
                            "name:Code Completed", "Code Completed Check List Item Not Found");
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.1.item")[1],
                            "name:WSDL Created", "WSDL Created Check List Item Not Found");
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.2.item")[1],
                            "name:QoS Created", "QoS Created Check List Item Not Found");
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.3.item")[1],
                            "name:Schema Created", "Schema Created Check List Item Not Found");
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.4.item")[1],
                            "name:Services Created", "Services Created Check List Item Not Found");
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.5.item")[1],
                            "name:Completion of Creation", "Completion of Creation  Check List Item Not Found");

        //activity search for trunk
        Thread.sleep(1000 * 10);
        ActivityBean activityObjTrunk = activitySearch.getActivities(sessionCookie, userName
                , servicePathTrunk, Utils.formatDate(Calendar.getInstance().getTime())
                , "", ActivityAdminServiceClient.FILTER_RESOURCE_UPDATE, 1);
        Assert.assertNotNull(activityObjTrunk, "Activity object null in trunk");
        Assert.assertNotNull(activityObjTrunk.getActivity(), "Activity list object null");
        Assert.assertTrue((activityObjTrunk.getActivity().length > 0), "Activity list object null");
        String activity = activityObjTrunk.getActivity()[0];
        Assert.assertTrue(activity.contains(userName), "Activity not found. User name not found on last activity. " + activity);
        Assert.assertTrue(activity.contains("has updated the resource"),
                          "Activity not found. has updated not contain in last activity. " + activity);
        Assert.assertTrue(activity.contains("0m ago"), "Activity not found. current time not found on last activity. " + activity);


    }


    @Test(description = "Promote Service to Development", dependsOnMethods = {"promoteServiceAgainToCreation"})
    public void promoteServiceToDevelopment()
            throws InterruptedException, CustomLifecyclesChecklistAdminServiceExceptionException,
                   RemoteException, RegistryException, RegistryExceptionException {
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathTrunk);
        String[] actions;
        LifecycleActions[] availableActions = lifeCycle.getAvailableActions();
        Assert.assertEquals(availableActions.length, 1, "Available Action count mismatched");
        actions = availableActions[0].getActions();
        Assert.assertNull(actions, "Available Action found");

        lifeCycleAdminService.invokeAspect(servicePathTrunk, ASPECT_NAME, ACTION_ITEM_CLICK,
                                           new String[]{"true", "false", "false", "false", "false"});
        lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathTrunk);
        Assert.assertEquals(availableActions.length, 1, "Available Action count mismatched");
        availableActions = lifeCycle.getAvailableActions();
        actions = availableActions[0].getActions();
        Assert.assertNull(actions, "Available Action found");

        //check demote action
        lifeCycleAdminService.invokeAspect(servicePathTrunk, ASPECT_NAME, ACTION_ITEM_CLICK,
                                           new String[]{"true", "true", "true", "true", "true", "false"});
        lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathTrunk);
        availableActions = lifeCycle.getAvailableActions();
        actions = availableActions[0].getActions();
        Assert.assertNotNull(actions, "Available Action Not found");
        Assert.assertEquals(actions.length, 1, "Action not found");
        Assert.assertEquals(actions[0], "Demote", "Demote Action not found");

        lifeCycleAdminService.invokeAspect(servicePathTrunk, ASPECT_NAME, ACTION_ITEM_CLICK,
                                           new String[]{"true", "true", "true", "true", "true", "true"});

        lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathTrunk);
        availableActions = lifeCycle.getAvailableActions();
        actions = availableActions[0].getActions();
        Assert.assertNotNull(actions, "Available Action Not found");
        Assert.assertEquals(actions.length, 3, "Action not found");
        Assert.assertEquals(actions[0], "Promote", "Promote Action not found");
        Assert.assertEquals(actions[1], "Demote", "Demote Action not found");
        Assert.assertEquals(actions[2], "Abort", "Abort Action not found");

        dependencyList = lifeCycleAdminService.getAllDependencies(servicePathTrunk);
        Assert.assertNotNull(dependencyList, "Dependency List Not Found");
        Assert.assertEquals(dependencyList.length, 8, "Dependency Count mismatched");

        Thread.sleep(1000);
        ArrayOfString[] parameters = new ArrayOfString[8];

        for (int i = 0; i < dependencyList.length; i++) {
            parameters[i] = new ArrayOfString();
            parameters[i].setArray(new String[]{dependencyList[i], "1.0.0"});

        }

        parameters[7] = new ArrayOfString();
        parameters[7].setArray(new String[]{"preserveOriginal", "true"});

        lifeCycleAdminService.invokeAspectWithParams(servicePathTrunk, ASPECT_NAME,
                                                     ACTION_PROMOTE, null, parameters);

        Thread.sleep(500);
        servicePathBranchDev = "/_system/governance/branches/development/services/org/wso2/carbon/core/services/echo/1.0.0/" + serviceName;

        lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathBranchDev);
        Resource service = registry.get(servicePathBranchDev);
        Assert.assertNotNull(service, "Service Not found on registry path " + servicePathBranchDev);
        Assert.assertEquals(service.getPath(), servicePathBranchDev, "Service not in branches/testing. " + servicePathBranchDev);

        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.lifecycle.CustomServiceLifeCycle.state")[0]
                , "Development", "LifeCycle State Mismatched");

        dependencyList = lifeCycleAdminService.getAllDependencies(servicePathBranchDev);
        Assert.assertNotNull(dependencyList, "Dependency List Not Found");
        Assert.assertEquals(dependencyList.length, 7, "Dependency Count mismatched");
        for (String dependencyPath : dependencyList) {
            Assert.assertTrue(dependencyPath.contains("branches/development"), " Dependency not created on branches/development path");
        }

        Assert.assertEquals(registry.get(servicePathTrunk).getPath(), servicePathTrunk, "Preserve original failed");

        //life cycle check list
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.0.item")[1],
                            "name:Effective Inspection Completed", "Effective Inspection Completed Check List Item Not Found");
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.1.item")[1],
                            "name:Test Cases Passed", "Test Cases Passed Check List Item Not Found");
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.2.item")[1],
                            "name:Smoke Test Passed", "Smoke Test Passed Check List Item Not Found");

        //activity search for trunk
        Thread.sleep(1000 * 10);
        ActivityBean activityObjTrunk = activitySearch.getActivities(sessionCookie, userName
                , servicePathTrunk, Utils.formatDate(Calendar.getInstance().getTime())
                , "", ActivityAdminServiceClient.FILTER_RESOURCE_UPDATE, 1);
        Assert.assertNotNull(activityObjTrunk, "Activity object null in trunk");
        Assert.assertNotNull(activityObjTrunk.getActivity(), "Activity list object null");
        Assert.assertTrue((activityObjTrunk.getActivity().length > 0), "Activity list object null");
        String activity = activityObjTrunk.getActivity()[0];
        Assert.assertTrue(activity.contains(userName), "Activity not found. User name not found on last activity. " + activity);
        Assert.assertTrue(activity.contains("has updated the resource"),
                          "Activity not found. has updated not contain in last activity. " + activity);
        Assert.assertTrue(activity.contains("0m ago"), "Activity not found. current time not found on last activity. " + activity);

        //activity search for branch
        Thread.sleep(1000 * 10);
        activityObjTrunk = activitySearch.getActivities(sessionCookie, userName
                , servicePathBranchDev, Utils.formatDate(Calendar.getInstance().getTime())
                , "", ActivityAdminServiceClient.FILTER_ALL, 1);
        Assert.assertNotNull(activityObjTrunk, "Activity object null in trunk");
        Assert.assertNotNull(activityObjTrunk.getActivity(), "Activity list object null");
        Assert.assertTrue((activityObjTrunk.getActivity().length > 0), "Activity list object null");
        activity = activityObjTrunk.getActivity()[0];
        Assert.assertTrue(activity.contains(userName), "Activity not found. User name not found on last activity. " + activity);
        Assert.assertTrue(activity.contains("has added the resource") || activity.contains("has updated the resource"),
                          "Activity not found. has added not contain in activity. " + activity);
        Assert.assertTrue(activity.contains("0m ago"), "Activity not found. current time not found on last activity. " + activity);

    }

    @Test(description = "Demote Service Creation", dependsOnMethods = {"promoteServiceToDevelopment"})
    public void demoteServiceToCommencementFromDevelopment()
            throws Exception, RemoteException,
                   UserAdminException, InterruptedException, RegistryException,
                   RegistryExceptionException {
        addRole("devrole");
        Thread.sleep(500);
        lifeCycleAdminService.invokeAspect(servicePathBranchDev, ASPECT_NAME, ACTION_ITEM_CLICK,
                                           new String[]{"true", "true", "true"});

        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathBranchDev);
        String[] actions;
        LifecycleActions[] availableActions = lifeCycle.getAvailableActions();
        actions = availableActions[0].getActions();
        Assert.assertNotNull(actions, "Available Action Not found");
        Assert.assertEquals(actions.length, 3, "Action not found");
        Assert.assertEquals(actions[0], "Promote", "Promote Action not found");
        Assert.assertEquals(actions[1], "Demote", "Demote Action not found");
        Assert.assertEquals(actions[2], "Abort", "Abort Action not found");

        dependencyList = lifeCycleAdminService.getAllDependencies(servicePathBranchDev);
        Assert.assertNotNull(dependencyList, "Dependency List Not Found");
        Assert.assertEquals(dependencyList.length, 7, "Dependency Count mismatched");

        ArrayOfString[] parameters = new ArrayOfString[8];

        for (int i = 0; i < dependencyList.length; i++) {
            parameters[i] = new ArrayOfString();
            parameters[i].setArray(new String[]{dependencyList[i], "1.0.0"});

        }

        parameters[7] = new ArrayOfString();
        parameters[7].setArray(new String[]{"preserveOriginal", "true"});

        lifeCycleAdminService.invokeAspectWithParams(servicePathBranchDev, ASPECT_NAME,
                                                     ACTION_DEMOTE, null, parameters);

        servicePathTrunkDemote = "/_system/governance/trunk/services/org/wso2/carbon/core/services/echo/1.0.0/" + serviceName;

        lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathTrunkDemote);
        Resource service = registry.get(servicePathTrunkDemote);
        Assert.assertNotNull(service, "Service Not found on registry path " + servicePathTrunkDemote);
        Assert.assertTrue(service.getPath().contains("trunk"), "Service not in trunk. " + servicePathTrunkDemote);

        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties()
                , "registry.lifecycle.CustomServiceLifeCycle.state")[0], "Commencement",
                            "LifeCycle State Mismatched");

        dependencyList = lifeCycleAdminService.getAllDependencies(servicePathTrunkDemote);
        Assert.assertNotNull(dependencyList, "Dependency List Not Found");
        Assert.assertEquals(dependencyList.length, 7, "Dependency Count mismatched");
        for (String dependencyPath : dependencyList) {
            Assert.assertTrue(dependencyPath.contains("/trunk/"), " Dependency not created on trunk");
        }

        //life cycle check list
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.0.item")[1],
                            "name:Requirements Gathered", "Requirements Gathered Check List Item Not Found");
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.0.item")[2],
                            "value:false", "Requirements Gathered Check List checked");

        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.1.item")[1],
                            "name:Document Requirements", "Document Requirements Check List Item Not Found");
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.1.item")[2],
                            "value:false", "Document Requirements Check List checked");

        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.2.item")[1],
                            "name:Architecture Diagram Finalized", "Architecture Diagram Finalize Check List Item Not Found");
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.2.item")[2],
                            "value:false", "Architecture Diagram Finalize Check List checked");

        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.3.item")[1],
                            "name:Design UML Diagrams", "Design UML Diagrams Check List Item Not Found");
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.3.item")[2],
                            "value:false", "Design UML Diagrams Check List checked");

        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.4.item")[1]
                , "name:High Level Design Completed", "High Level Design Completed Check List Item Not Found");
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.4.item")[2]
                , "value:false", "High Level Design Completed Check List checked");

        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.5.item")[1]
                , "name:Completion of Commencement", "Completion of Commencement  Check List Item Not Found");
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.5.item")[2]
                , "value:false", "Completion of Commencement  Check List checked");


        //Activity search
        Thread.sleep(1000 * 10);
        ActivityBean activityObjTrunk = activitySearch.getActivities(sessionCookie, userName
                , servicePathTrunkDemote, Utils.formatDate(Calendar.getInstance().getTime())
                , "", ActivityAdminServiceClient.FILTER_RESOURCE_UPDATE, 1);
        Assert.assertNotNull(activityObjTrunk, "Activity object null in trunk");
        Assert.assertNotNull(activityObjTrunk.getActivity(), "Activity list object null");
        Assert.assertTrue((activityObjTrunk.getActivity().length > 0), "Activity list object null");
        String activity = activityObjTrunk.getActivity()[0];
        Assert.assertTrue(activity.contains(userName), "Activity not found. User name not found on last activity. " + activity);
        Assert.assertTrue(activity.contains("has updated the resource"),
                          "Activity not found. has updated not contain in last activity. " + activity);
        Assert.assertTrue(activity.contains("0m ago"), "Activity not found. current time not found on last activity. " + activity);

    }


    @AfterClass
    public void deleteLifeCycle()
            throws RegistryException, LifeCycleManagementServiceExceptionException,
                   RemoteException {
        if (servicePathTrunk != null) {
            registry.delete(servicePathTrunk);
        }

        if (servicePathBranchDev != null) {
            registry.delete(servicePathBranchDev);
        }
        if (servicePathTrunkDemote != null) {
            registry.delete(servicePathTrunkDemote);
        }
        boolean  isUsed =  lifeCycleManagerAdminService.isLifecycleNameInUse(ASPECT_NAME);
        if(isUsed){
            Assert.assertFalse(lifeCycleManagerAdminService.deleteLifeCycle(ASPECT_NAME),
                    "LC deleted, but resources are contains it's usage ");

        }  else {
            Assert.assertTrue(lifeCycleManagerAdminService.deleteLifeCycle(ASPECT_NAME),
                    "LC delete failed, even without usage");
        }
        registry = null;
        activitySearch = null;
        lifeCycleAdminService = null;
    }

    private void deleteRolesIfExist()
            throws Exception, RemoteException {

        if (userManger.roleNameExists("archrole")) {
            userManger.deleteRole("archrole");
        }

        if (userManger.roleNameExists("managerrole")) {
            userManger.deleteRole("managerrole");
        }

        if (userManger.roleNameExists("devrole")) {
            userManger.deleteRole("devrole");
        }

        if (userManger.roleNameExists("qarole")) {
            userManger.deleteRole("qarole");
        }
        if (userManger.roleNameExists("techoprole")) {
            userManger.deleteRole("techoprole");
        }
    }

    private void addRole(String roleName) throws Exception {
        String[] permissions = {"/permission/"};
        userManger.addRole(roleName, new String[]{userName}, permissions);
    }
}
