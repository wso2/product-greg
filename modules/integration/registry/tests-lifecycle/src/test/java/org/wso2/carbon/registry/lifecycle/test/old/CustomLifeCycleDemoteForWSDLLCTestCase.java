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
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.lifecycle.test.utils.LifeCycleUtils;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.carbon.user.mgt.common.UserAdminException;

import java.rmi.RemoteException;
import java.util.Calendar;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class CustomLifeCycleDemoteForWSDLLCTestCase {

    private ManageEnvironment environment;
    private int userId = 1;
    private UserInfo userInfo = UserListCsvReader.getUserInfo(userId);
    private WSRegistryServiceClient wsRegistry;
    private LifeCycleAdminServiceClient lifeCycleAdminService;
    private LifeCycleManagementClient lifeCycleManagementClient;
    private ActivityAdminServiceClient activityAdminServiceClient;
    private UserManagementClient userManger;

    private String serviceName = "echoServiceForDemote.wsdl";
    private final String ASPECT_NAME = "CustomServiceLifeCycle";
    private final String ACTION_PROMOTE = "Promote";
    private final String ACTION_DEMOTE = "Demote";
    private final String ACTION_ITEM_CLICK = "itemClick";
    private String servicePathTrunk;
    private String servicePathTrunkDemote;
    private String servicePathBranchDev;
    private String[] dependencyList = null;

    /**
     * @throws Exception
     */
    @BeforeClass()
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

        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        wsRegistry = registryProviderUtil.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        Registry governance = registryProviderUtil.getGovernanceRegistry(wsRegistry, userId);
        userManger = new UserManagementClient(environment.getGreg()
                                                      .getProductVariables()
                                                      .getBackendUrl(),
                                              userInfo.getUserName(),
                                              userInfo.getPassword());

        deleteRolesIfExist();

        LifeCycleUtils.deleteLifeCycleIfExist(ASPECT_NAME, lifeCycleManagementClient);
        String wsdlPath = "/_system/governance" + LifeCycleUtils.addWSDL("echoServiceWsdl.wsdl", governance, serviceName);
        Association[] usedBy = wsRegistry.getAssociations(wsdlPath, "usedBy");
        assertNotNull(usedBy, "WSDL usedBy Association type not found");
        for (Association association : usedBy) {
            if (association.getSourcePath().equalsIgnoreCase(wsdlPath)) {
                servicePathTrunk = association.getDestinationPath();
            }
        }
        Thread.sleep(1000);

        LifeCycleUtils.createNewLifeCycle(ASPECT_NAME, lifeCycleManagementClient);

        assertNotNull(servicePathTrunk, "Service Not Found associate with WSDL");

        Thread.sleep(1000);
        String ASS_TYPE_DEPENDS = "depends";
        Association[] dependency = wsRegistry.getAssociations(servicePathTrunk, ASS_TYPE_DEPENDS);

        assertNotNull(dependency, "Dependency Not Found.");
        assertTrue(dependency.length > 0, "Dependency list empty");
        assertEquals(dependency.length, 7, "some dependency missing or additional dependency found.");


    }

    /**
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
     *
     * @throws org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException
     *
     * @throws java.rmi.RemoteException
     * @throws InterruptedException
     */
    @Test(groups = "wso2.greg", description = "Add lifecycle to a service")
    public void addLifecycle()
            throws RegistryException, CustomLifecyclesChecklistAdminServiceExceptionException,
                   RemoteException, InterruptedException {
        wsRegistry.associateAspect(servicePathTrunk, ASPECT_NAME);
        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathTrunk);
        Resource service = wsRegistry.get(servicePathTrunk);
        assertNotNull(service, "Service Not found on registry path " + servicePathTrunk);
        assertTrue(service.getPath().contains("trunk"), "Service not in trunk. " + servicePathTrunk);

        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties()
                , "registry.lifecycle.CustomServiceLifeCycle.state")[0], "Commencement",
                     "LifeCycle State Mismatched");
        dependencyList = lifeCycleAdminService.getAllDependencies(servicePathTrunk);
        assertNotNull(dependencyList, "Dependency List Not Found");
        assertEquals(dependencyList.length, 8, "Dependency Count mismatched");

    }

    /**
     * @throws Exception
     * @throws java.rmi.RemoteException
     * @throws org.wso2.carbon.user.mgt.common.UserAdminException
     *
     */
    @Test(groups = "wso2.greg", description = "Click Check List Item", dependsOnMethods = "addLifecycle")
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
        Resource service = wsRegistry.get(servicePathTrunk);
        assertNotNull(service, "Service Not found on registry path " + servicePathTrunk);
        assertEquals(service.getPath(), servicePathTrunk, "Service not in branches/testing. " + servicePathTrunk);

        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.lifecycle.CustomServiceLifeCycle.state")[0]
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


    }

    /**
     * @throws org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException
     *
     * @throws java.rmi.RemoteException
     * @throws org.wso2.carbon.user.mgt.common.UserAdminException
     *
     */
    @Test(groups = "wso2.greg", description = "click check list in creation stage", dependsOnMethods = "promoteServiceToCreation")
    public void clickCreationCheckListToEnableDemote()
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
    @Test(groups = "wso2.greg", description = "Demote Service to Commencement", dependsOnMethods = "clickCreationCheckListToEnableDemote")
    public void demoteServiceToCommencement()
            throws InterruptedException, CustomLifecyclesChecklistAdminServiceExceptionException,
                   RemoteException, RegistryException, RegistryExceptionException {
        Thread.sleep(1000);

        lifeCycleAdminService.invokeAspect(servicePathTrunk, ASPECT_NAME,
                                           ACTION_DEMOTE, null);
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathTrunk);
        Resource service = wsRegistry.get(servicePathTrunk);
        assertNotNull(service, "Service Not found on registry path " + servicePathTrunk);
        assertTrue(service.getPath().contains("trunk"), "Service not in trunk. " + servicePathTrunk);

        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties()
                , "registry.lifecycle.CustomServiceLifeCycle.state")[0], "Commencement",
                     "LifeCycle State Mismatched");

        dependencyList = lifeCycleAdminService.getAllDependencies(servicePathTrunk);
        assertNotNull(dependencyList, "Dependency List Not Found");
        assertEquals(dependencyList.length, 8, "Dependency Count mismatched");

        //life cycle check list
        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.0.item")[1],
                     "name:Requirements Gathered", "Requirements Gathered Check List Item Not Found");
        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.0.item")[2],
                     "value:false", "Requirements Gathered Check List checked");

        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.1.item")[1],
                     "name:Document Requirements", "Document Requirements Check List Item Not Found");
        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.1.item")[2],
                     "value:false", "Document Requirements Check List checked");

        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.2.item")[1],
                     "name:Architecture Diagram Finalized", "Architecture Diagram Finalize Check List Item Not Found");
        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.2.item")[2],
                     "value:false", "Architecture Diagram Finalize Check List checked");

        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.3.item")[1],
                     "name:Design UML Diagrams", "Design UML Diagrams Check List Item Not Found");
        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.3.item")[2],
                     "value:false", "Design UML Diagrams Check List checked");

        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.4.item")[1]
                , "name:High Level Design Completed", "High Level Design Completed Check List Item Not Found");
        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.4.item")[2]
                , "value:false", "High Level Design Completed Check List checked");

        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.5.item")[1]
                , "name:Completion of Commencement", "Completion of Commencement  Check List Item Not Found");
        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.5.item")[2]
                , "value:false", "Completion of Commencement  Check List checked");

        //Activity search
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
    @Test(groups = "wso2.greg", description = "Promote Service to Creation", dependsOnMethods = "demoteServiceToCommencement")
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
        Resource service = wsRegistry.get(servicePathTrunk);
        assertNotNull(service, "Service Not found on registry path " + servicePathTrunk);
        assertEquals(service.getPath(), servicePathTrunk, "Service not in branches/testing. " + servicePathTrunk);

        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.lifecycle.CustomServiceLifeCycle.state")[0]
                , "Creation", "LifeCycle State Mismatched");

        dependencyList = lifeCycleAdminService.getAllDependencies(servicePathTrunk);
        assertNotNull(dependencyList, "Dependency List Not Found");
        assertEquals(dependencyList.length, 8, "Dependency Count mismatched");

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
    @Test(groups = "wso2.greg", description = "Promote Service to Development", dependsOnMethods = "promoteServiceAgainToCreation")
    public void promoteServiceToDevelopment()
            throws InterruptedException, CustomLifecyclesChecklistAdminServiceExceptionException,
                   RemoteException, RegistryException, RegistryExceptionException {
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

        dependencyList = lifeCycleAdminService.getAllDependencies(servicePathTrunk);
        assertNotNull(dependencyList, "Dependency List Not Found");
        assertEquals(dependencyList.length, 8, "Dependency Count mismatched");

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
        Resource service = wsRegistry.get(servicePathBranchDev);
        assertNotNull(service, "Service Not found on registry path " + servicePathBranchDev);
        assertEquals(service.getPath(), servicePathBranchDev, "Service not in branches/testing. " + servicePathBranchDev);

        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.lifecycle.CustomServiceLifeCycle.state")[0]
                , "Development", "LifeCycle State Mismatched");

        dependencyList = lifeCycleAdminService.getAllDependencies(servicePathBranchDev);
        assertNotNull(dependencyList, "Dependency List Not Found");
        assertEquals(dependencyList.length, 7, "Dependency Count mismatched");
        for (String dependencyPath : dependencyList) {
            assertTrue(dependencyPath.contains("branches/development"), " Dependency not created on branches/development path");
        }

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

    }

    /**
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Demote Service Creation", dependsOnMethods = "promoteServiceToDevelopment")
    public void demoteServiceToCommencementFromDevelopment()
            throws Exception {
        addRole("devrole");
        Thread.sleep(500);
        lifeCycleAdminService.invokeAspect(servicePathBranchDev, ASPECT_NAME, ACTION_ITEM_CLICK,
                                           new String[]{"true", "true", "true"});

        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathBranchDev);
        String[] actions;
        LifecycleActions[] availableActions = lifeCycle.getAvailableActions();
        actions = availableActions[0].getActions();
        assertNotNull(actions, "Available Action Not found");
        assertEquals(actions.length, 3, "Action not found");
        assertEquals(actions[0], "Promote", "Promote Action not found");
        assertEquals(actions[1], "Demote", "Demote Action not found");
        assertEquals(actions[2], "Abort", "Abort Action not found");

        dependencyList = lifeCycleAdminService.getAllDependencies(servicePathBranchDev);
        assertNotNull(dependencyList, "Dependency List Not Found");
        assertEquals(dependencyList.length, 7, "Dependency Count mismatched");

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
        Resource service = wsRegistry.get(servicePathTrunkDemote);
        assertNotNull(service, "Service Not found on registry path " + servicePathTrunkDemote);
        assertTrue(service.getPath().contains("trunk"), "Service not in trunk. " + servicePathTrunkDemote);

        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties()
                , "registry.lifecycle.CustomServiceLifeCycle.state")[0], "Commencement",
                     "LifeCycle State Mismatched");

        dependencyList = lifeCycleAdminService.getAllDependencies(servicePathTrunkDemote);
        assertNotNull(dependencyList, "Dependency List Not Found");
        assertEquals(dependencyList.length, 7, "Dependency Count mismatched");
        for (String dependencyPath : dependencyList) {
            assertTrue(dependencyPath.contains("/trunk/"), " Dependency not created on trunk");
        }

        //life cycle check list
        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.0.item")[1],
                     "name:Requirements Gathered", "Requirements Gathered Check List Item Not Found");
        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.0.item")[2],
                     "value:false", "Requirements Gathered Check List checked");

        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.1.item")[1],
                     "name:Document Requirements", "Document Requirements Check List Item Not Found");
        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.1.item")[2],
                     "value:false", "Document Requirements Check List checked");

        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.2.item")[1],
                     "name:Architecture Diagram Finalized", "Architecture Diagram Finalize Check List Item Not Found");
        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.2.item")[2],
                     "value:false", "Architecture Diagram Finalize Check List checked");

        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.3.item")[1],
                     "name:Design UML Diagrams", "Design UML Diagrams Check List Item Not Found");
        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.3.item")[2],
                     "value:false", "Design UML Diagrams Check List checked");

        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.4.item")[1]
                , "name:High Level Design Completed", "High Level Design Completed Check List Item Not Found");
        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.4.item")[2]
                , "value:false", "High Level Design Completed Check List checked");

        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.5.item")[1]
                , "name:Completion of Commencement", "Completion of Commencement  Check List Item Not Found");
        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.5.item")[2]
                , "value:false", "Completion of Commencement  Check List checked");


        //Activity search
        Thread.sleep(1000 * 10);
        ActivityBean activityObjTrunk = activityAdminServiceClient.getActivities(environment.getGreg().getSessionCookie(), userInfo.getUserName()
                , servicePathTrunkDemote, LifeCycleUtils.formatDate(Calendar.getInstance().getTime())
                , "", ActivityAdminServiceClient.FILTER_RESOURCE_UPDATE, 1);
        assertNotNull(activityObjTrunk, "Activity object null in trunk");
        assertNotNull(activityObjTrunk.getActivity(), "Activity list object null");
        assertTrue((activityObjTrunk.getActivity().length > 0), "Activity list object null");
        String activity = activityObjTrunk.getActivity()[0];
        assertTrue(activity.contains(userInfo.getUserName()), "Activity not found. User name not found on last activity. " + activity);
        assertTrue(activity.contains("has updated the resource"),
                   "Activity not found. has updated not contain in last activity. " + activity);

    }


    /**
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
     *
     * @throws org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException
     *
     * @throws java.rmi.RemoteException
     */
    @Test(groups = "wso2.greg", description = "Delete added resources", dependsOnMethods = "demoteServiceToCommencementFromDevelopment")
    public void deleteResources()
            throws RegistryException, LifeCycleManagementServiceExceptionException,
                   RemoteException {
        if (servicePathTrunk != null) {
            wsRegistry.delete(servicePathTrunk);
        }

        if (servicePathBranchDev != null) {
            wsRegistry.delete(servicePathBranchDev);
        }
        if (servicePathTrunkDemote != null) {
            wsRegistry.delete(servicePathTrunkDemote);
        }
        boolean isUsed = lifeCycleManagementClient.isLifecycleNameInUse(ASPECT_NAME);
        if (isUsed) {
            assertFalse(lifeCycleManagementClient.deleteLifeCycle(ASPECT_NAME),
                        "LC deleted, but resources are contains it's usage ");

        } else {
            assertTrue(lifeCycleManagementClient.deleteLifeCycle(ASPECT_NAME),
                       "LC delete failed, even without usage");
        }

    }

    /**
     * @throws Exception
     */
    @AfterClass()
    public void cleanup()
            throws Exception {

        String wsdlPathTrunk1 = "/_system/governance/trunk/wsdls/org/wso2/carbon/core/services/echo/echoServiceWsdl.wsdl";
        String wsdlPathTrunk2 = "/_system/governance/trunk/wsdls/org/wso2/carbon/core/services/echo/1.0.0/echoServiceWsdl.wsdl";
        String wsdlPathDev = "/_system/governance/branches/development/wsdls/org/wso2/carbon/core/services/echo/1.0.0/echoServiceWsdl.wsdl";

        if (wsRegistry.resourceExists(servicePathTrunk)) {
            wsRegistry.delete(servicePathTrunk);
        }
        if (wsRegistry.resourceExists(servicePathBranchDev)) {
            wsRegistry.delete(servicePathBranchDev);
        }
        if (wsRegistry.resourceExists(servicePathTrunkDemote)) {
            wsRegistry.delete(servicePathTrunkDemote);
        }
        if (wsRegistry.resourceExists(wsdlPathTrunk1)) {
            wsRegistry.delete(wsdlPathTrunk1);
        }
        if (wsRegistry.resourceExists(wsdlPathTrunk2)) {
            wsRegistry.delete(wsdlPathTrunk2);
        }
        if (wsRegistry.resourceExists(wsdlPathDev)) {
            wsRegistry.delete(wsdlPathDev);
        }
        LifeCycleUtils.deleteLifeCycleIfExist(ASPECT_NAME, lifeCycleManagementClient);

        deleteRolesIfExist();
    }

    /**
     * @throws Exception
     */
    private void deleteRolesIfExist()
            throws Exception {

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

    /**
     * @param roleName role need to be added
     * @throws Exception
     */
    private void addRole(String roleName) throws Exception {
        String[] permissions = {"/permission/admin/manage/"};
        userManger.addRole(roleName, new String[]{userInfo.getUserName()}, permissions);
    }
}
