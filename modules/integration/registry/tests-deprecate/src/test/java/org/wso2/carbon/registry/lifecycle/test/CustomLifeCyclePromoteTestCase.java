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
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.lifecycle.test.utils.Utils;
import org.wso2.carbon.registry.search.metadata.test.utils.GregTestUtils;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.carbon.user.mgt.common.UserAdminException;

import java.rmi.RemoteException;
import java.util.Calendar;

public class CustomLifeCyclePromoteTestCase {
    private String sessionCookie;

    private WSRegistryServiceClient registry;
    private LifeCycleAdminServiceClient lifeCycleAdminService;
    private LifeCycleManagementClient lifeCycleManagerAdminService;
    private ActivityAdminServiceClient activitySearch;
    private UserManagementClient userManger;
    private String userName;
    private String serviceName = "CustomLCTestService";
    private final String ASPECT_NAME = "CustomServiceLC";
    private final String ACTION_PROMOTE = "Promote";
    private final String ACTION_ITEM_CLICK = "itemClick";
    private String servicePathTrunk;
    private String servicePathBranchDev;
    private String servicePathBranchQA;
    private String servicePathBranchProd;

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

        deleteRolesIfExist();
        Utils.deleteLifeCycleIfExist(sessionCookie, ASPECT_NAME, lifeCycleManagerAdminService);
        servicePathTrunk = "/_system/governance" + Utils.addService("sns", serviceName, governance);
        Thread.sleep(1000);

        Utils.createNewLifeCycle(ASPECT_NAME, lifeCycleManagerAdminService);


    }


    @Test( description = "Add LifeCycle to a service")
    public void addLifeCycleToService()
            throws RegistryException, InterruptedException,
                   CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException,
                   RegistryExceptionException {
        registry.associateAspect(servicePathTrunk, ASPECT_NAME);
        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathTrunk);
        Resource service = registry.get(servicePathTrunk);
        Assert.assertNotNull(service, "Service Not found on registry path " + servicePathTrunk);
        Assert.assertEquals(service.getPath(), servicePathTrunk, "Service path changed after adding life cycle. " + servicePathTrunk);
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.lifecycle.CustomServiceLC.state")[0], "Commencement",
                            "LifeCycle State Mismatched");

        //life cycle check list
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.0.item")[1],
                            "name:Requirements Gathered", "Requirements Gathered Check List Item Not Found");
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.1.item")[1],
                            "name:Document Requirements", "Document Requirements Check List Item Not Found");
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.2.item")[1],
                            "name:Architecture Diagram Finalized", "Architecture Diagram Finalize Check List Item Not Found");
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.3.item")[1],
                            "name:Design UML Diagrams", "Design UML Diagrams Check List Item Not Found");
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.4.item")[1]
                , "name:High Level Design Completed", "High Level Design Completed Check List Item Not Found");
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.5.item")[1]
                , "name:Completion of Commencement", "Completion of Commencement  Check List Item Not Found");

        //Activity search
        Thread.sleep(1000 * 10);
        ActivityBean activityObj = activitySearch.getActivities(sessionCookie, userName
                , servicePathTrunk, Utils.formatDate(Calendar.getInstance().getTime())
                , "", ActivityAdminServiceClient.FILTER_ASSOCIATE_ASPECT, 1);
        Assert.assertNotNull(activityObj, "Activity object null for Associate Aspect");
        Assert.assertNotNull(activityObj.getActivity(), "Activity list object null for Associate Aspect");
        Assert.assertTrue((activityObj.getActivity().length > 0), "Activity list object null");
        String activity = activityObj.getActivity()[0];
        Assert.assertTrue(activity.contains(userName), "User name not found on activity last activity. " + activity);
        Assert.assertTrue(activity.contains("associated the aspect CustomServiceLC"),
                          "associated the aspect ServiceLifeCycle not contain in last activity. " + activity);
        Assert.assertTrue(activity.contains("0m ago"), "current time not found on activity. " + activity);
    }

    @Test(description = "Click Check List Item", dependsOnMethods ="addLifeCycleToService")
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

    @Test(description = "Promote Service to Creation", dependsOnMethods = "clickCommencementCheckList")
    public void promoteServiceToCreation()
            throws InterruptedException, CustomLifecyclesChecklistAdminServiceExceptionException,
                   RemoteException, RegistryException, RegistryExceptionException {
        Thread.sleep(1000);
        ArrayOfString[] parameters = new ArrayOfString[2];
        parameters[0] = new ArrayOfString();
        parameters[0].setArray(new String[]{servicePathTrunk, "1.0.0"});

        parameters[1] = new ArrayOfString();
        parameters[1].setArray(new String[]{"preserveOriginal", "true"});

        lifeCycleAdminService.invokeAspectWithParams(servicePathTrunk, ASPECT_NAME,
                                                     ACTION_PROMOTE, null, parameters);

        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathTrunk);
        Resource service = registry.get(servicePathTrunk);
        Assert.assertNotNull(service, "Service Not found on registry path " + servicePathTrunk);
        Assert.assertEquals(service.getPath(), servicePathTrunk, "Service not in branches/testing. " + servicePathTrunk);

        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.lifecycle.CustomServiceLC.state")[0]
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

    @Test(description = "click check list in creation stage", dependsOnMethods = "promoteServiceToCreation")
    public void clickCreationCheckList()
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

    }

    @Test(description = "Promote Service to Development", dependsOnMethods = "clickCreationCheckList")
    public void promoteServiceToDevelopment()
            throws InterruptedException, CustomLifecyclesChecklistAdminServiceExceptionException,
                   RemoteException, RegistryException, RegistryExceptionException {
        Thread.sleep(1000);
        ArrayOfString[] parameters = new ArrayOfString[2];
        parameters[0] = new ArrayOfString();
        parameters[0].setArray(new String[]{servicePathTrunk, "1.0.0"});

        parameters[1] = new ArrayOfString();
        parameters[1].setArray(new String[]{"preserveOriginal", "true"});

        lifeCycleAdminService.invokeAspectWithParams(servicePathTrunk, ASPECT_NAME,
                                                     ACTION_PROMOTE, null, parameters);

        Thread.sleep(500);
        servicePathBranchDev = "/_system/governance/branches/development/services/sns/1.0.0/" + serviceName;

        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathBranchDev);
        Resource service = registry.get(servicePathBranchDev);
        Assert.assertNotNull(service, "Service Not found on registry path " + servicePathBranchDev);
        Assert.assertEquals(service.getPath(), servicePathBranchDev, "Service not in branches/testing. " + servicePathBranchDev);

        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.lifecycle.CustomServiceLC.state")[0]
                , "Development", "LifeCycle State Mismatched");

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

    @Test(description = "click check list in creation stage", dependsOnMethods = "promoteServiceToDevelopment")
    public void clickDevelopmentCheckList()
            throws Exception, RemoteException,
                   UserAdminException, InterruptedException {
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathBranchDev);
        String[] actions;
        LifecycleActions[] availableActions = lifeCycle.getAvailableActions();
        Assert.assertEquals(availableActions.length, 1, "Available Action count mismatched");
        actions = availableActions[0].getActions();
        Assert.assertNull(actions, "Available Action found");

        addRole("devrole");
        Thread.sleep(500);
        lifeCycleAdminService.invokeAspect(servicePathBranchDev, ASPECT_NAME, ACTION_ITEM_CLICK,
                                           new String[]{"true", "false", "false"});
        lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathBranchDev);
        Assert.assertEquals(availableActions.length, 1, "Available Action count mismatched");
        availableActions = lifeCycle.getAvailableActions();
        actions = availableActions[0].getActions();
        Assert.assertNotNull(actions, "Available Action Not found");
        Assert.assertEquals(actions.length, 1, "Action not found");
        Assert.assertEquals(actions[0], "Abort", "Abort Action not found");


        lifeCycleAdminService.invokeAspect(servicePathBranchDev, ASPECT_NAME, ACTION_ITEM_CLICK,
                                           new String[]{"true", "true", "true"});

        lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathBranchDev);
        availableActions = lifeCycle.getAvailableActions();
        actions = availableActions[0].getActions();
        Assert.assertNotNull(actions, "Available Action Not found");
        Assert.assertEquals(actions.length, 3, "Action not found");
        Assert.assertEquals(actions[0], "Promote", "Promote Action not found");
        Assert.assertEquals(actions[1], "Demote", "Demote Action not found");
        Assert.assertEquals(actions[2], "Abort", "Abort Action not found");

    }

    @Test(description = "Promote Service to QA", dependsOnMethods = "clickDevelopmentCheckList")
    public void promoteServiceToQA()
            throws InterruptedException, CustomLifecyclesChecklistAdminServiceExceptionException,
                   RemoteException, RegistryException, RegistryExceptionException {
        Thread.sleep(1000);
        ArrayOfString[] parameters = new ArrayOfString[2];
        parameters[0] = new ArrayOfString();
        parameters[0].setArray(new String[]{servicePathBranchDev, "1.0.0"});

        parameters[1] = new ArrayOfString();
        parameters[1].setArray(new String[]{"preserveOriginal", "true"});

        lifeCycleAdminService.invokeAspectWithParams(servicePathBranchDev, ASPECT_NAME,
                                                     ACTION_PROMOTE, null, parameters);

        Thread.sleep(500);
        servicePathBranchQA = "/_system/governance/branches/qa/services/sns/1.0.0/" + serviceName;

        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathBranchQA);
        Resource service = registry.get(servicePathBranchQA);
        Assert.assertNotNull(service, "Service Not found on registry path " + servicePathBranchQA);
        Assert.assertEquals(service.getPath(), servicePathBranchQA, "Service not in branches/testing. " + servicePathBranchQA);

        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.lifecycle.CustomServiceLC.state")[0]
                , "QA", "LifeCycle State Mismatched");

        Assert.assertEquals(registry.get(servicePathBranchDev).getPath(), servicePathBranchDev, "Preserve original failed");

        //life cycle check list
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.0.item")[1],
                            "name:Service Configuration", "Service Configuration Check List Item Not Found");

        //activity search for trunk
        Thread.sleep(1000 * 10);
        ActivityBean activityObjTrunk = activitySearch.getActivities(sessionCookie, userName
                , servicePathBranchDev, Utils.formatDate(Calendar.getInstance().getTime())
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
                , servicePathBranchQA, Utils.formatDate(Calendar.getInstance().getTime())
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


    @Test(description = "click check list in creation stage", dependsOnMethods = "promoteServiceToQA")
    public void clickQACheckList()
            throws Exception, RemoteException,
                   UserAdminException, InterruptedException {
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathBranchQA);
        String[] actions;
        LifecycleActions[] availableActions = lifeCycle.getAvailableActions();
        Assert.assertEquals(availableActions.length, 1, "Available Action count mismatched");
        actions = availableActions[0].getActions();
        Assert.assertNull(actions, "Available Action found");

        addRole("qarole");
        Thread.sleep(500);

        lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathBranchQA);
        Assert.assertEquals(availableActions.length, 1, "Available Action count mismatched");
        availableActions = lifeCycle.getAvailableActions();
        actions = availableActions[0].getActions();
        Assert.assertNotNull(actions, "Available Action Not found");
        Assert.assertEquals(actions.length, 1, "Action not found");
        Assert.assertEquals(actions[0], "Abort", "Abort Action not found");


        lifeCycleAdminService.invokeAspect(servicePathBranchQA, ASPECT_NAME, ACTION_ITEM_CLICK,
                                           new String[]{"true"});

        lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathBranchQA);
        availableActions = lifeCycle.getAvailableActions();
        actions = availableActions[0].getActions();
        Assert.assertNotNull(actions, "Available Action Not found");
        Assert.assertEquals(actions.length, 2, "Action not found");
        Assert.assertEquals(actions[0], "Demote", "Demote Action not found");
        Assert.assertEquals(actions[1], "Abort", "Abort Action not found");

        addRole("techoprole");
        Thread.sleep(500);

        lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathBranchQA);
        availableActions = lifeCycle.getAvailableActions();
        actions = availableActions[0].getActions();
        Assert.assertNotNull(actions, "Available Action Not found");
        Assert.assertEquals(actions.length, 3, "Action not found");
        Assert.assertEquals(actions[0], "Promote", "Promote Action not found");
        Assert.assertEquals(actions[1], "Demote", "Demote Action not found");
        Assert.assertEquals(actions[2], "Abort", "Abort Action not found");

    }

    @Test(description = "Promote Service to Production", dependsOnMethods = "clickQACheckList")
    public void promoteServiceToProduction()
            throws InterruptedException, CustomLifecyclesChecklistAdminServiceExceptionException,
                   RemoteException, RegistryException, RegistryExceptionException {
        Thread.sleep(1000);
        ArrayOfString[] parameters = new ArrayOfString[2];
        parameters[0] = new ArrayOfString();
        parameters[0].setArray(new String[]{servicePathBranchQA, "1.0.0"});

        parameters[1] = new ArrayOfString();
        parameters[1].setArray(new String[]{"preserveOriginal", "true"});

        lifeCycleAdminService.invokeAspectWithParams(servicePathBranchQA, ASPECT_NAME,
                                                     ACTION_PROMOTE, null, parameters);

        Thread.sleep(500);
        servicePathBranchProd = "/_system/governance/branches/production/services/sns/1.0.0/" + serviceName;

        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathBranchProd);
        Resource service = registry.get(servicePathBranchProd);
        Assert.assertNotNull(service, "Service Not found on registry path " + servicePathBranchProd);
        Assert.assertEquals(service.getPath(), servicePathBranchProd, "Service not in branches/testing. " + servicePathBranchProd);

        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.lifecycle.CustomServiceLC.state")[0]
                , "Launched", "LifeCycle State Mismatched");

        Assert.assertEquals(registry.get(servicePathBranchQA).getPath(), servicePathBranchQA, "Preserve original failed");

        //life cycle check list
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.0.item")[1],
                            "name:Service Configuration", "Service Configuration Check List Item Not Found");

        //activity search for trunk
        Thread.sleep(1000 * 10);
        ActivityBean activityObjTrunk = activitySearch.getActivities(sessionCookie, userName
                , servicePathBranchQA, Utils.formatDate(Calendar.getInstance().getTime())
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
                , servicePathBranchProd, Utils.formatDate(Calendar.getInstance().getTime())
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

    @Test(description = "click check list in Launched Stage", dependsOnMethods = "promoteServiceToProduction")
    public void clickLaunchedCheckList()
            throws CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException,
                   UserAdminException, InterruptedException {
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathBranchProd);
        String[] actions;
        LifecycleActions[] availableActions = lifeCycle.getAvailableActions();
        Assert.assertEquals(availableActions.length, 1, "Available Action count mismatched");
        availableActions = lifeCycle.getAvailableActions();
        actions = availableActions[0].getActions();
        Assert.assertNotNull(actions, "Available Action Not found");
        Assert.assertEquals(actions.length, 1, "Action not found");
        Assert.assertEquals(actions[0], "Abort", "Abort Action not found");


        lifeCycleAdminService.invokeAspect(servicePathBranchProd, ASPECT_NAME, ACTION_ITEM_CLICK,
                                           new String[]{"true"});

        Thread.sleep(500);
        lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathBranchQA);
        availableActions = lifeCycle.getAvailableActions();
        actions = availableActions[0].getActions();
        Assert.assertNotNull(actions, "Available Action Not found");
        Assert.assertEquals(actions.length, 3, "Action not found");
        Assert.assertEquals(actions[0], "Promote", "Promote Action not found");
        Assert.assertEquals(actions[1], "Demote", "Demote Action not found");
        Assert.assertEquals(actions[2], "Abort", "Abort Action not found");

    }

    @Test(description = "Promote Service to Obsolete", dependsOnMethods = "clickLaunchedCheckList")
    public void promoteServiceToObsolete()
            throws InterruptedException, CustomLifecyclesChecklistAdminServiceExceptionException,
                   RemoteException, RegistryException, RegistryExceptionException {
        Thread.sleep(1000);
        ArrayOfString[] parameters = new ArrayOfString[2];
        parameters[0] = new ArrayOfString();
        parameters[0].setArray(new String[]{servicePathBranchProd, "1.0.0"});

        parameters[1] = new ArrayOfString();
        parameters[1].setArray(new String[]{"preserveOriginal", "true"});

        lifeCycleAdminService.invokeAspectWithParams(servicePathBranchProd, ASPECT_NAME,
                                                     ACTION_PROMOTE, null, parameters);

        Thread.sleep(500);
        servicePathBranchProd = "/_system/governance/branches/production/services/sns/1.0.0/" + serviceName;

        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathBranchProd);
        Resource service = registry.get(servicePathBranchProd);
        Assert.assertNotNull(service, "Service Not found on registry path " + servicePathBranchProd);
        Assert.assertEquals(service.getPath(), servicePathBranchProd, "Service not in branches/testing. " + servicePathBranchProd);

        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.lifecycle.CustomServiceLC.state")[0]
                , "Obsolete", "LifeCycle State Mismatched");

        //activity search for production branch
        Thread.sleep(1000 * 10);
        ActivityBean activityObjTrunk = activitySearch.getActivities(sessionCookie, userName
                , servicePathBranchProd, Utils.formatDate(Calendar.getInstance().getTime())
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

        if (servicePathBranchQA != null) {
            registry.delete(servicePathBranchQA);
        }

        if (servicePathBranchProd != null) {
            registry.delete(servicePathBranchProd);
        }
        Assert.assertTrue(lifeCycleManagerAdminService.deleteLifeCycle(ASPECT_NAME),
                          "Life Cycle Deleted failed");
        registry = null;
        activitySearch = null;
        lifeCycleAdminService = null;
    }

    private void deleteRolesIfExist()
            throws Exception, RemoteException {

        if (userManger.roleNameExists("archrole")) {
            userManger.deleteRole( "archrole");
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
