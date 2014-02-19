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
package org.wso2.carbon.registry.lifecycle.test.old;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.governance.LifeCycleAdminServiceClient;
import org.wso2.carbon.automation.api.clients.governance.LifeCycleManagementClient;
import org.wso2.carbon.automation.api.clients.registry.ActivityAdminServiceClient;
import org.wso2.carbon.automation.api.clients.user.mgt.UserManagementClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.beans.xsd.LifecycleBean;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.services.ArrayOfString;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.LifecycleActions;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.registry.activities.stub.RegistryExceptionException;
import org.wso2.carbon.registry.activities.stub.beans.xsd.ActivityBean;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.lifecycle.test.utils.LifeCycleUtils;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.carbon.user.mgt.common.UserAdminException;

import java.rmi.RemoteException;
import java.util.Calendar;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class CustomLifeCyclePromoteLCTestCase {

    private int userId = 1;
    private UserInfo userInfo = UserListCsvReader.getUserInfo(userId);
    private ManageEnvironment environment;

    private LifeCycleAdminServiceClient lifeCycleAdminService;
    private LifeCycleManagementClient lifeCycleManagementClient;
    private ActivityAdminServiceClient activityAdminServiceClient;
    private WSRegistryServiceClient wsRegistry;
    private UserManagementClient userManagementClient;
    private String serviceName = "CustomLCTestService";
    private final String ASPECT_NAME = "CustomServiceLC";
    private final String ACTION_PROMOTE = "Promote";
    private final String ACTION_ITEM_CLICK = "itemClick";
    private String servicePathTrunk;
    private String servicePathBranchDev;
    private String servicePathBranchQA;
    private String servicePathBranchProd;

    /**
     * @throws Exception
     */
    @BeforeClass
    public void init() throws Exception {


        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        environment = builder.build();

        lifeCycleAdminService = new LifeCycleAdminServiceClient(
                environment.getGreg()
                        .getProductVariables()
                        .getBackendUrl(),
                userInfo.getUserName(),
                userInfo.getPassword());

        activityAdminServiceClient = new ActivityAdminServiceClient(environment.getGreg()
                                                                            .getProductVariables()
                                                                            .getBackendUrl(),
                                                                    userInfo.getUserName(),
                                                                    userInfo.getPassword());
        lifeCycleManagementClient = new LifeCycleManagementClient(environment.getGreg()
                                                                          .getProductVariables()
                                                                          .getBackendUrl(),
                                                                  userInfo.getUserName(),
                                                                  userInfo.getPassword());
        userManagementClient = new UserManagementClient(environment.getGreg()
                                                                .getProductVariables()
                                                                .getBackendUrl(),
                                                        userInfo.getUserName(),
                                                        userInfo.getPassword());

        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        wsRegistry = registryProviderUtil.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        Registry governance = registryProviderUtil.getGovernanceRegistry(wsRegistry, userId);

        deleteRolesIfExist();
        LifeCycleUtils.deleteLifeCycleIfExist(ASPECT_NAME, lifeCycleManagementClient);
        servicePathTrunk = "/_system/governance" + LifeCycleUtils.addService("sns", serviceName, governance);
        Thread.sleep(1000);

        LifeCycleUtils.createNewLifeCycle(ASPECT_NAME, lifeCycleManagementClient);


    }


    /**
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
     *
     * @throws InterruptedException
     * @throws org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException
     *
     * @throws java.rmi.RemoteException
     * @throws org.wso2.carbon.registry.activities.stub.RegistryExceptionException
     *
     */
    @Test(groups = "wso2.greg", description = "Add LifeCycle to a service")
    public void addLifeCycleToService()
            throws RegistryException, InterruptedException,
                   CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException,
                   RegistryExceptionException {
        wsRegistry.associateAspect(servicePathTrunk, ASPECT_NAME);
        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathTrunk);
        Resource service = wsRegistry.get(servicePathTrunk);
        assertNotNull(service, "Service Not found on registry path " + servicePathTrunk);
        assertEquals(service.getPath(), servicePathTrunk, "Service path changed after adding life cycle. " + servicePathTrunk);
        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.lifecycle.CustomServiceLC.state")[0], "Commencement",
                     "LifeCycle State Mismatched");

        //life cycle check list
        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.0.item")[1],
                     "name:Requirements Gathered", "Requirements Gathered Check List Item Not Found");
        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.1.item")[1],
                     "name:Document Requirements", "Document Requirements Check List Item Not Found");
        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.2.item")[1],
                     "name:Architecture Diagram Finalized", "Architecture Diagram Finalize Check List Item Not Found");
        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.3.item")[1],
                     "name:Design UML Diagrams", "Design UML Diagrams Check List Item Not Found");
        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.4.item")[1]
                , "name:High Level Design Completed", "High Level Design Completed Check List Item Not Found");
        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.5.item")[1]
                , "name:Completion of Commencement", "Completion of Commencement  Check List Item Not Found");

        //Activity search
        Thread.sleep(1000 * 10);
        ActivityBean activityObj = activityAdminServiceClient.getActivities(environment.getGreg().getSessionCookie(), userInfo.getUserName()
                , servicePathTrunk, LifeCycleUtils.formatDate(Calendar.getInstance().getTime())
                , "", ActivityAdminServiceClient.FILTER_ASSOCIATE_ASPECT, 1);
        assertNotNull(activityObj, "Activity object null for Associate Aspect");
        assertNotNull(activityObj.getActivity(), "Activity list object null for Associate Aspect");
        assertTrue((activityObj.getActivity().length > 0), "Activity list object null");
        String activity = activityObj.getActivity()[0];
        assertTrue(activity.contains(userInfo.getUserName()), "User name not found on activity last activity. " + activity);
        assertTrue(activity.contains("associated the aspect CustomServiceLC"),
                   "associated the aspect ServiceLifeCycle not contain in last activity. " + activity);
        assertTrue(activity.contains("0m ago"), "current time not found on activity. " + activity);
    }

    /**
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Click Check List Item", dependsOnMethods = "addLifeCycleToService")
    public void clickCommencementCheckList()
            throws Exception {
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathTrunk);
        String[] actions;
        LifecycleActions[] availableActions = lifeCycle.getAvailableActions();
        assertEquals(availableActions.length, 1, "Available Action count mismatched");
        actions = availableActions[0].getActions();
        assertNull(actions, "Available Action found");

        addRole("archrole");
        lifeCycleAdminService.invokeAspect(servicePathTrunk, ASPECT_NAME, ACTION_ITEM_CLICK,
                                           new String[]{"true", "false", "false", "false", "false"});
        lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathTrunk);
        assertEquals(availableActions.length, 1, "Available Action count mismatched");
        availableActions = lifeCycle.getAvailableActions();
        actions = availableActions[0].getActions();
        assertNull(actions, "Available Action found");

        lifeCycleAdminService.invokeAspect(servicePathTrunk, ASPECT_NAME, ACTION_ITEM_CLICK,
                                           new String[]{"true", "true", "true", "true", "true", "true"});
        lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathTrunk);
        availableActions = lifeCycle.getAvailableActions();
        actions = availableActions[0].getActions();
        assertNotNull(actions, "Available Action Not found");
        assertEquals(actions.length, 1, "Action not found");
        assertEquals(actions[0], "Abort", "Abort Action not found");

        addRole("managerrole");
        lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathTrunk);
        availableActions = lifeCycle.getAvailableActions();
        actions = availableActions[0].getActions();
        assertNotNull(actions, "Available Action Not found");
        assertEquals(actions.length, 2, "Action not found");
        assertEquals(actions[0], "Promote", "Promote Action not found");
        assertEquals(actions[1], "Abort", "Abort Action not found");

    }

    /**
     * @throws InterruptedException
     * @throws org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException
     *
     * @throws java.rmi.RemoteException
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
     *
     * @throws org.wso2.carbon.registry.activities.stub.RegistryExceptionException
     *
     */
    @Test(groups = "wso2.greg", description = "Promote Service to Creation", dependsOnMethods = "clickCommencementCheckList")
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
        Resource service = wsRegistry.get(servicePathTrunk);
        assertNotNull(service, "Service Not found on registry path " + servicePathTrunk);
        assertEquals(service.getPath(), servicePathTrunk, "Service not in branches/testing. " + servicePathTrunk);

        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.lifecycle.CustomServiceLC.state")[0]
                , "Creation", "LifeCycle State Mismatched");

        assertEquals(wsRegistry.get(servicePathTrunk).getPath(), servicePathTrunk, "Preserve original failed");

        //life cycle check list
        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.0.item")[1],
                     "name:Code Completed", "Code Completed Check List Item Not Found");
        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.1.item")[1],
                     "name:WSDL Created", "WSDL Created Check List Item Not Found");
        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.2.item")[1],
                     "name:QoS Created", "QoS Created Check List Item Not Found");
        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.3.item")[1],
                     "name:Schema Created", "Schema Created Check List Item Not Found");
        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.4.item")[1],
                     "name:Services Created", "Services Created Check List Item Not Found");
        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.5.item")[1],
                     "name:Completion of Creation", "Completion of Creation  Check List Item Not Found");

        //activity search for trunk
        Thread.sleep(1000 * 10);
        ActivityBean activityObjTrunk = activityAdminServiceClient.getActivities(environment.getGreg().getSessionCookie(), userInfo.getUserName()
                , servicePathTrunk, LifeCycleUtils.formatDate(Calendar.getInstance().getTime())
                , "", ActivityAdminServiceClient.FILTER_RESOURCE_UPDATE, 1);
        assertNotNull(activityObjTrunk, "Activity object null in trunk");
        assertNotNull(activityObjTrunk.getActivity(), "Activity list object null");
        assertTrue((activityObjTrunk.getActivity().length > 0), "Activity list object null");
        String activity = activityObjTrunk.getActivity()[0];
        assertTrue(activity.contains(userInfo.getUserName()), "Activity not found. User name not found on last activity. " + activity);
        assertTrue(activity.contains("has updated the resource"),
                   "Activity not found. has updated not contain in last activity. " + activity);
        assertTrue(activity.contains("0m ago"), "Activity not found. current time not found on last activity. " + activity);


    }


    /**
     * @throws org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException
     *
     * @throws java.rmi.RemoteException
     * @throws org.wso2.carbon.user.mgt.common.UserAdminException
     *
     */
    @Test(groups = "wso2.greg", description = "click check list in creation stage", dependsOnMethods = "promoteServiceToCreation")
    public void clickCreationCheckList()
            throws CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException,
                   UserAdminException {
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathTrunk);
        String[] actions;
        LifecycleActions[] availableActions = lifeCycle.getAvailableActions();
        assertEquals(availableActions.length, 1, "Available Action count mismatched");
        actions = availableActions[0].getActions();
        assertNull(actions, "Available Action found");

        lifeCycleAdminService.invokeAspect(servicePathTrunk, ASPECT_NAME, ACTION_ITEM_CLICK,
                                           new String[]{"true", "false", "false", "false", "false"});
        lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathTrunk);
        assertEquals(availableActions.length, 1, "Available Action count mismatched");
        availableActions = lifeCycle.getAvailableActions();
        actions = availableActions[0].getActions();
        assertNull(actions, "Available Action found");

        //check demote action
        lifeCycleAdminService.invokeAspect(servicePathTrunk, ASPECT_NAME, ACTION_ITEM_CLICK,
                                           new String[]{"true", "true", "true", "true", "true", "false"});
        lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathTrunk);
        availableActions = lifeCycle.getAvailableActions();
        actions = availableActions[0].getActions();
        assertNotNull(actions, "Available Action Not found");
        assertEquals(actions.length, 1, "Action not found");
        assertEquals(actions[0], "Demote", "Demote Action not found");

        lifeCycleAdminService.invokeAspect(servicePathTrunk, ASPECT_NAME, ACTION_ITEM_CLICK,
                                           new String[]{"true", "true", "true", "true", "true", "true"});

        lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathTrunk);
        availableActions = lifeCycle.getAvailableActions();
        actions = availableActions[0].getActions();
        assertNotNull(actions, "Available Action Not found");
        assertEquals(actions.length, 3, "Action not found");
        assertEquals(actions[0], "Promote", "Promote Action not found");
        assertEquals(actions[1], "Demote", "Demote Action not found");
        assertEquals(actions[2], "Abort", "Abort Action not found");

    }

    /**
     * @throws InterruptedException
     * @throws org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException
     *
     * @throws java.rmi.RemoteException
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
     *
     * @throws org.wso2.carbon.registry.activities.stub.RegistryExceptionException
     *
     */
    @Test(groups = "wso2.greg", description = "Promote Service to Development", dependsOnMethods = "clickCreationCheckList")
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
        Resource service = wsRegistry.get(servicePathBranchDev);
        assertNotNull(service, "Service Not found on registry path " + servicePathBranchDev);
        assertEquals(service.getPath(), servicePathBranchDev, "Service not in branches/testing. " + servicePathBranchDev);

        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.lifecycle.CustomServiceLC.state")[0]
                , "Development", "LifeCycle State Mismatched");

        assertEquals(wsRegistry.get(servicePathTrunk).getPath(), servicePathTrunk, "Preserve original failed");

        //life cycle check list
        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.0.item")[1],
                     "name:Effective Inspection Completed", "Effective Inspection Completed Check List Item Not Found");
        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.1.item")[1],
                     "name:Test Cases Passed", "Test Cases Passed Check List Item Not Found");
        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.2.item")[1],
                     "name:Smoke Test Passed", "Smoke Test Passed Check List Item Not Found");

        //activity search for trunk
        Thread.sleep(1000 * 10);
        ActivityBean activityObjTrunk = activityAdminServiceClient.getActivities(environment.getGreg().getSessionCookie(), userInfo.getUserName()
                , servicePathTrunk, LifeCycleUtils.formatDate(Calendar.getInstance().getTime())
                , "", ActivityAdminServiceClient.FILTER_RESOURCE_UPDATE, 1);
        assertNotNull(activityObjTrunk, "Activity object null in trunk");
        assertNotNull(activityObjTrunk.getActivity(), "Activity list object null");
        assertTrue((activityObjTrunk.getActivity().length > 0), "Activity list object null");
        String activity = activityObjTrunk.getActivity()[0];
        assertTrue(activity.contains(userInfo.getUserName()), "Activity not found. User name not found on last activity. " + activity);
        assertTrue(activity.contains("has updated the resource"),
                   "Activity not found. has updated not contain in last activity. " + activity);
        assertTrue(activity.contains("0m ago"), "Activity not found. current time not found on last activity. " + activity);

        //activity search for branch
        Thread.sleep(1000 * 10);
        activityObjTrunk = activityAdminServiceClient.getActivities(environment.getGreg().getSessionCookie(), userInfo.getUserName()
                , servicePathBranchDev, LifeCycleUtils.formatDate(Calendar.getInstance().getTime())
                , "", ActivityAdminServiceClient.FILTER_ALL, 1);
        assertNotNull(activityObjTrunk, "Activity object null in trunk");
        assertNotNull(activityObjTrunk.getActivity(), "Activity list object null");
        assertTrue((activityObjTrunk.getActivity().length > 0), "Activity list object null");
        activity = activityObjTrunk.getActivity()[0];
        assertTrue(activity.contains(userInfo.getUserName()), "Activity not found. User name not found on last activity. " + activity);
        assertTrue(activity.contains("has added the resource") || activity.contains("has updated the resource"),
                   "Activity not found. has added not contain in activity. " + activity);
        assertTrue(activity.contains("0m ago"), "Activity not found. current time not found on last activity. " + activity);


    }

    /**
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "click check list in creation stage", dependsOnMethods = "promoteServiceToDevelopment")
    public void clickDevelopmentCheckList()
            throws Exception {
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathBranchDev);
        String[] actions;
        LifecycleActions[] availableActions = lifeCycle.getAvailableActions();
        assertEquals(availableActions.length, 1, "Available Action count mismatched");
        actions = availableActions[0].getActions();
        assertNull(actions, "Available Action found");

        addRole("devrole");
        Thread.sleep(500);
        lifeCycleAdminService.invokeAspect(servicePathBranchDev, ASPECT_NAME, ACTION_ITEM_CLICK,
                                           new String[]{"true", "false", "false"});
        lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathBranchDev);
        assertEquals(availableActions.length, 1, "Available Action count mismatched");
        availableActions = lifeCycle.getAvailableActions();
        actions = availableActions[0].getActions();
        assertNotNull(actions, "Available Action Not found");
        assertEquals(actions.length, 1, "Action not found");
        assertEquals(actions[0], "Abort", "Abort Action not found");


        lifeCycleAdminService.invokeAspect(servicePathBranchDev, ASPECT_NAME, ACTION_ITEM_CLICK,
                                           new String[]{"true", "true", "true"});

        lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathBranchDev);
        availableActions = lifeCycle.getAvailableActions();
        actions = availableActions[0].getActions();
        assertNotNull(actions, "Available Action Not found");
        assertEquals(actions.length, 3, "Action not found");
        assertEquals(actions[0], "Promote", "Promote Action not found");
        assertEquals(actions[1], "Demote", "Demote Action not found");
        assertEquals(actions[2], "Abort", "Abort Action not found");

    }

    /**
     * @throws InterruptedException
     * @throws org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException
     *
     * @throws java.rmi.RemoteException
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
     *
     * @throws org.wso2.carbon.registry.activities.stub.RegistryExceptionException
     *
     */
    @Test(groups = "wso2.greg", description = "Promote Service to QA", dependsOnMethods = "clickDevelopmentCheckList")
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
        Resource service = wsRegistry.get(servicePathBranchQA);
        assertNotNull(service, "Service Not found on registry path " + servicePathBranchQA);
        assertEquals(service.getPath(), servicePathBranchQA, "Service not in branches/testing. " + servicePathBranchQA);

        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.lifecycle.CustomServiceLC.state")[0]
                , "QA", "LifeCycle State Mismatched");

        assertEquals(wsRegistry.get(servicePathBranchDev).getPath(), servicePathBranchDev, "Preserve original failed");

        //life cycle check list
        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.0.item")[1],
                     "name:Service Configuration", "Service Configuration Check List Item Not Found");

        //activity search for trunk
        Thread.sleep(1000 * 10);
        ActivityBean activityObjTrunk = activityAdminServiceClient.getActivities(environment.getGreg().getSessionCookie(), userInfo.getUserName()
                , servicePathBranchDev, LifeCycleUtils.formatDate(Calendar.getInstance().getTime())
                , "", ActivityAdminServiceClient.FILTER_RESOURCE_UPDATE, 1);
        assertNotNull(activityObjTrunk, "Activity object null in trunk");
        assertNotNull(activityObjTrunk.getActivity(), "Activity list object null");
        assertTrue((activityObjTrunk.getActivity().length > 0), "Activity list object null");
        String activity = activityObjTrunk.getActivity()[0];
        assertTrue(activity.contains(userInfo.getUserName()), "Activity not found. User name not found on last activity. " + activity);
        assertTrue(activity.contains("has updated the resource"),
                   "Activity not found. has updated not contain in last activity. " + activity);
        assertTrue(activity.contains("0m ago"), "Activity not found. current time not found on last activity. " + activity);

        //activity search for branch
        Thread.sleep(1000 * 10);
        activityObjTrunk = activityAdminServiceClient.getActivities(environment.getGreg().getSessionCookie(), userInfo.getUserName()
                , servicePathBranchQA, LifeCycleUtils.formatDate(Calendar.getInstance().getTime())
                , "", ActivityAdminServiceClient.FILTER_ALL, 1);
        assertNotNull(activityObjTrunk, "Activity object null in trunk");
        assertNotNull(activityObjTrunk.getActivity(), "Activity list object null");
        assertTrue((activityObjTrunk.getActivity().length > 0), "Activity list object null");
        activity = activityObjTrunk.getActivity()[0];
        assertTrue(activity.contains(userInfo.getUserName()), "Activity not found. User name not found on last activity. " + activity);
        assertTrue(activity.contains("has added the resource") || activity.contains("has updated the resource"),
                   "Activity not found. has added not contain in activity. " + activity);
        assertTrue(activity.contains("0m ago"), "Activity not found. current time not found on last activity. " + activity);


    }


    /**
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "click check list in creation stage", dependsOnMethods = "promoteServiceToQA")
    public void clickQACheckList()
            throws Exception {
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathBranchQA);
        String[] actions;
        LifecycleActions[] availableActions = lifeCycle.getAvailableActions();
        assertEquals(availableActions.length, 1, "Available Action count mismatched");
        actions = availableActions[0].getActions();
        assertNull(actions, "Available Action found");

        addRole("qarole");
        Thread.sleep(500);

        lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathBranchQA);
        assertEquals(availableActions.length, 1, "Available Action count mismatched");
        availableActions = lifeCycle.getAvailableActions();
        actions = availableActions[0].getActions();
        assertNotNull(actions, "Available Action Not found");
        assertEquals(actions.length, 1, "Action not found");
        assertEquals(actions[0], "Abort", "Abort Action not found");


        lifeCycleAdminService.invokeAspect(servicePathBranchQA, ASPECT_NAME, ACTION_ITEM_CLICK,
                                           new String[]{"true"});

        lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathBranchQA);
        availableActions = lifeCycle.getAvailableActions();
        actions = availableActions[0].getActions();
        assertNotNull(actions, "Available Action Not found");
        assertEquals(actions.length, 2, "Action not found");
        assertEquals(actions[0], "Demote", "Demote Action not found");
        assertEquals(actions[1], "Abort", "Abort Action not found");

        addRole("techoprole");
        Thread.sleep(500);

        lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathBranchQA);
        availableActions = lifeCycle.getAvailableActions();
        actions = availableActions[0].getActions();
        assertNotNull(actions, "Available Action Not found");
        assertEquals(actions.length, 3, "Action not found");
        assertEquals(actions[0], "Promote", "Promote Action not found");
        assertEquals(actions[1], "Demote", "Demote Action not found");
        assertEquals(actions[2], "Abort", "Abort Action not found");

    }

    /**
     * @throws InterruptedException
     * @throws org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException
     *
     * @throws java.rmi.RemoteException
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
     *
     * @throws org.wso2.carbon.registry.activities.stub.RegistryExceptionException
     *
     */
    @Test(groups = "wso2.greg", description = "Promote Service to Production", dependsOnMethods = "clickQACheckList")
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
        Resource service = wsRegistry.get(servicePathBranchProd);
        assertNotNull(service, "Service Not found on registry path " + servicePathBranchProd);
        assertEquals(service.getPath(), servicePathBranchProd, "Service not in branches/testing. " + servicePathBranchProd);

        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.lifecycle.CustomServiceLC.state")[0]
                , "Launched", "LifeCycle State Mismatched");

        assertEquals(wsRegistry.get(servicePathBranchQA).getPath(), servicePathBranchQA, "Preserve original failed");

        //life cycle check list
        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.0.item")[1],
                     "name:Service Configuration", "Service Configuration Check List Item Not Found");

        //activity search for trunk
        Thread.sleep(1000 * 10);
        ActivityBean activityObjTrunk = activityAdminServiceClient.getActivities(environment.getGreg().getSessionCookie(), userInfo.getUserName()
                , servicePathBranchQA, LifeCycleUtils.formatDate(Calendar.getInstance().getTime())
                , "", ActivityAdminServiceClient.FILTER_RESOURCE_UPDATE, 1);
        assertNotNull(activityObjTrunk, "Activity object null in trunk");
        assertNotNull(activityObjTrunk.getActivity(), "Activity list object null");
        assertTrue((activityObjTrunk.getActivity().length > 0), "Activity list object null");
        String activity = activityObjTrunk.getActivity()[0];
        assertTrue(activity.contains(userInfo.getUserName()), "Activity not found. User name not found on last activity. " + activity);
        assertTrue(activity.contains("has updated the resource"),
                   "Activity not found. has updated not contain in last activity. " + activity);
        assertTrue(activity.contains("0m ago"), "Activity not found. current time not found on last activity. " + activity);

        //activity search for branch
        Thread.sleep(1000 * 10);
        activityObjTrunk = activityAdminServiceClient.getActivities(environment.getGreg().getSessionCookie(), userInfo.getUserName()
                , servicePathBranchProd, LifeCycleUtils.formatDate(Calendar.getInstance().getTime())
                , "", ActivityAdminServiceClient.FILTER_ALL, 1);
        assertNotNull(activityObjTrunk, "Activity object null in trunk");
        assertNotNull(activityObjTrunk.getActivity(), "Activity list object null");
        assertTrue((activityObjTrunk.getActivity().length > 0), "Activity list object null");
        activity = activityObjTrunk.getActivity()[0];
        assertTrue(activity.contains(userInfo.getUserName()), "Activity not found. User name not found on last activity. " + activity);
        assertTrue(activity.contains("has added the resource") || activity.contains("has updated the resource"),
                   "Activity not found. has added not contain in activity. " + activity);
        assertTrue(activity.contains("0m ago"), "Activity not found. current time not found on last activity. " + activity);


    }

    /**
     * @throws org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException
     *
     * @throws java.rmi.RemoteException
     * @throws org.wso2.carbon.user.mgt.common.UserAdminException
     *
     * @throws InterruptedException
     */
    @Test(groups = "wso2.greg", description = "click check list in Launched Stage", dependsOnMethods = "promoteServiceToProduction")
    public void clickLaunchedCheckList()
            throws CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException,
                   UserAdminException, InterruptedException {
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathBranchProd);
        String[] actions;
        LifecycleActions[] availableActions = lifeCycle.getAvailableActions();
        assertEquals(availableActions.length, 1, "Available Action count mismatched");
        availableActions = lifeCycle.getAvailableActions();
        actions = availableActions[0].getActions();
        assertNotNull(actions, "Available Action Not found");
        assertEquals(actions.length, 1, "Action not found");
        assertEquals(actions[0], "Abort", "Abort Action not found");


        lifeCycleAdminService.invokeAspect(servicePathBranchProd, ASPECT_NAME, ACTION_ITEM_CLICK,
                                           new String[]{"true"});

        Thread.sleep(500);
        lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathBranchQA);
        availableActions = lifeCycle.getAvailableActions();
        actions = availableActions[0].getActions();
        assertNotNull(actions, "Available Action Not found");
        assertEquals(actions.length, 3, "Action not found");
        assertEquals(actions[0], "Promote", "Promote Action not found");
        assertEquals(actions[1], "Demote", "Demote Action not found");
        assertEquals(actions[2], "Abort", "Abort Action not found");

    }

    /**
     * @throws InterruptedException
     * @throws org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException
     *
     * @throws java.rmi.RemoteException
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
     *
     * @throws org.wso2.carbon.registry.activities.stub.RegistryExceptionException
     *
     */
    @Test(groups = "wso2.greg", description = "Promote Service to Obsolete", dependsOnMethods = "clickLaunchedCheckList")
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
        Resource service = wsRegistry.get(servicePathBranchProd);
        assertNotNull(service, "Service Not found on registry path " + servicePathBranchProd);
        assertEquals(service.getPath(), servicePathBranchProd, "Service not in branches/testing. " + servicePathBranchProd);

        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.lifecycle.CustomServiceLC.state")[0]
                , "Obsolete", "LifeCycle State Mismatched");

        //activity search for production branch
        Thread.sleep(1000 * 10);
        ActivityBean activityObjTrunk = activityAdminServiceClient.getActivities(environment.getGreg().getSessionCookie(), userInfo.getUserName()
                , servicePathBranchProd, LifeCycleUtils.formatDate(Calendar.getInstance().getTime())
                , "", ActivityAdminServiceClient.FILTER_RESOURCE_UPDATE, 1);
        assertNotNull(activityObjTrunk, "Activity object null in trunk");
        assertNotNull(activityObjTrunk.getActivity(), "Activity list object null");
        assertTrue((activityObjTrunk.getActivity().length > 0), "Activity list object null");
        String activity = activityObjTrunk.getActivity()[0];
        assertTrue(activity.contains(userInfo.getUserName()), "Activity not found. User name not found on last activity. " + activity);
        assertTrue(activity.contains("has updated the resource"),
                   "Activity not found. has updated not contain in last activity. " + activity);
        assertTrue(activity.contains("0m ago"), "Activity not found. current time not found on last activity. " + activity);


    }

    /**
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
     *
     * @throws org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException
     *
     * @throws java.rmi.RemoteException
     */
    @Test(groups = "wso2.greg", description = "Delete added resources", dependsOnMethods = "promoteServiceToObsolete")
    public void deleteResources()
            throws RegistryException, LifeCycleManagementServiceExceptionException,
                   RemoteException {
        if (servicePathTrunk != null) {
            wsRegistry.delete(servicePathTrunk);
        }
        if (servicePathBranchDev != null) {
            wsRegistry.delete(servicePathBranchDev);
        }
        if (servicePathBranchQA != null) {
            wsRegistry.delete(servicePathBranchQA);
        }
        if (servicePathBranchProd != null) {
            wsRegistry.delete(servicePathBranchProd);
        }
        assertTrue(lifeCycleManagementClient.deleteLifeCycle(ASPECT_NAME),
                   "Life Cycle Deleted failed");
    }


    /**
     * @throws Exception
     */
    @AfterClass()
    public void cleanup() throws Exception {

        if (wsRegistry.resourceExists(servicePathTrunk)) {
            wsRegistry.delete(servicePathTrunk);
        }
        if (wsRegistry.resourceExists(servicePathBranchDev)) {
            wsRegistry.delete(servicePathBranchDev);
        }
        if (wsRegistry.resourceExists(servicePathBranchQA)) {
            wsRegistry.delete(servicePathBranchQA);
        }
        if (wsRegistry.resourceExists(servicePathBranchProd)) {
            wsRegistry.delete(servicePathBranchProd);
        }
        LifeCycleUtils.deleteLifeCycleIfExist(ASPECT_NAME, lifeCycleManagementClient);

        deleteRolesIfExist();
    }

    /**
     * @throws Exception
     */
    private void deleteRolesIfExist()
            throws Exception {

        if (userManagementClient.roleNameExists("archrole")) {
            userManagementClient.deleteRole("archrole");
        }
        if (userManagementClient.roleNameExists("managerrole")) {
            userManagementClient.deleteRole("managerrole");
        }
        if (userManagementClient.roleNameExists("devrole")) {
            userManagementClient.deleteRole("devrole");
        }
        if (userManagementClient.roleNameExists("qarole")) {
            userManagementClient.deleteRole("qarole");
        }
        if (userManagementClient.roleNameExists("techoprole")) {
            userManagementClient.deleteRole("techoprole");
        }
    }

    /**
     * @param roleName role to be added
     * @throws Exception
     */
    private void addRole(String roleName) throws Exception {
        String[] permissions = {"/permission/admin/manage/"};
        userManagementClient.addRole(roleName, new String[]{userInfo.getUserName()}, permissions);
    }

}
