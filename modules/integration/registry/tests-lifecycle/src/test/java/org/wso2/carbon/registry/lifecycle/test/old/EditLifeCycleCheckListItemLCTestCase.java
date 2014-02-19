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
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.beans.xsd.LifecycleBean;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.registry.activities.stub.RegistryExceptionException;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.lifecycle.test.utils.LifeCycleUtils;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import java.rmi.RemoteException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class EditLifeCycleCheckListItemLCTestCase {

    private int userId = 1;
    private UserInfo userInfo = UserListCsvReader.getUserInfo(userId);

    private WSRegistryServiceClient wsRegistry;
    private LifeCycleAdminServiceClient lifeCycleAdminServiceClient;
    private LifeCycleManagementClient lifeCycleManagementClient;

    private final String ASPECT_NAME = "EditedCheckListServiceLC";
    private String servicePathDev;

    /**
     * @throws Exception
     */
    @BeforeClass
    public void init() throws Exception {

        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        ManageEnvironment environment = builder.build();

        lifeCycleAdminServiceClient = new LifeCycleAdminServiceClient(
                environment.getGreg()
                        .getProductVariables()
                        .getBackendUrl(),
                userInfo.getUserName(),
                userInfo.getPassword());

        lifeCycleManagementClient = new LifeCycleManagementClient(
                environment.getGreg()
                        .getProductVariables()
                        .getBackendUrl(),
                userInfo.getUserName(),
                userInfo.getPassword());

        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        wsRegistry = registryProviderUtil.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        Registry governance = registryProviderUtil.getGovernanceRegistry(wsRegistry, userId);

        String serviceName = "tmpServiceCustomLC";
        LifeCycleUtils.deleteLifeCycleIfExist(ASPECT_NAME, lifeCycleManagementClient);
        servicePathDev = "/_system/governance" + LifeCycleUtils.addService("sns", serviceName, governance);
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
    public void addLifeCycleToService() throws RegistryException, InterruptedException,
                                               CustomLifecyclesChecklistAdminServiceExceptionException,
                                               RemoteException,
                                               RegistryExceptionException {

        wsRegistry.associateAspect(servicePathDev, ASPECT_NAME);
        LifecycleBean lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(servicePathDev);
        Resource service = wsRegistry.get(servicePathDev);
        assertNotNull(service, "Service Not found on wsRegistry path " + servicePathDev);
        assertEquals(service.getPath(), servicePathDev, "Service path changed after adding life cycle. " + servicePathDev);
        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.lifecycle.EditedCheckListServiceLC.state")[0]
                , "Commencement",
                     "LifeCycle State Mismatched");

        lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(servicePathDev);

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


    }

    @Test(groups = "wso2.greg", description = "Find LifeCycle Usage", dependsOnMethods = "addLifeCycleToService")
    public void findLifeCycleUsage()
            throws LifeCycleManagementServiceExceptionException, RemoteException {

        assertTrue(lifeCycleManagementClient.isLifecycleNameInUse(ASPECT_NAME)
                , "Life Cycle Usage Not Found");

    }


    @Test(groups = "wso2.greg", description = "Remove LifeCycle from service", dependsOnMethods = "findLifeCycleUsage")
    public void removeLifeCycleFromService()
            throws CustomLifecyclesChecklistAdminServiceExceptionException,
                   RemoteException {

        lifeCycleAdminServiceClient.removeAspect(servicePathDev, ASPECT_NAME);
        LifecycleBean lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(servicePathDev);
        assertNull(lifeCycle.getLifecycleProperties(), "Life Cycle property Object Found");

    }

    @Test(groups = "wso2.greg", description = "Find LifeCycle Usage after removing life cycle from service"
            , dependsOnMethods = "removeLifeCycleFromService")
    public void findLifeCycleUsageAfterRemovingLifeCycle()
            throws LifeCycleManagementServiceExceptionException,
                   RemoteException {

        assertFalse(lifeCycleManagementClient.isLifecycleNameInUse(ASPECT_NAME)
                , "Life Cycle Usage Found");

    }

    @Test(groups = "wso2.greg", description = "Edit Life cycle Check list item names"
            , dependsOnMethods = "findLifeCycleUsageAfterRemovingLifeCycle")
    public void editLifeCycleCheckList() throws LifeCycleManagementServiceExceptionException,
                                                RemoteException, InterruptedException {

        String config = lifeCycleManagementClient.getLifecycleConfiguration(ASPECT_NAME);
        assertTrue(config.contains("aspect name=\"" + ASPECT_NAME + "\""),
                   "LifeCycleName Not Found in lifecycle configuration");
        String newLifeCycleConfiguration = config.replace("item name=\"Requirements Gathered\"", "item name=\"Requirements Gathered New\"");
        newLifeCycleConfiguration = newLifeCycleConfiguration.replace("item name=\"Document Requirements\"", "item name=\"Document Requirements New\"");
        newLifeCycleConfiguration = newLifeCycleConfiguration.replace("item name=\"Design UML Diagrams\"", "item name=\"Design UML Diagrams New\"");
        assertTrue(lifeCycleManagementClient.editLifeCycle(ASPECT_NAME, newLifeCycleConfiguration)
                , "Editing LifeCycle Name Failed");
        Thread.sleep(1000);


        String[] lifeCycleList = lifeCycleManagementClient.getLifecycleList();
        assertNotNull(lifeCycleList);
        assertTrue(lifeCycleList.length > 0, "Life Cycle List length zero");
        boolean found = false;
        for (String lc : lifeCycleList) {
            if (ASPECT_NAME.equalsIgnoreCase(lc)) {
                found = true;
            }
        }
        assertTrue(found, "Life Cycle list not contain edited life cycle");
    }

    @Test(groups = "wso2.greg", description = "Add Edited Life Cycle to a service", dependsOnMethods = "editLifeCycleCheckList")
    public void addServiceLifeCycleAgain()
            throws RegistryException, CustomLifecyclesChecklistAdminServiceExceptionException,
                   RemoteException, InterruptedException {

        wsRegistry.associateAspect(servicePathDev, ASPECT_NAME);
        LifecycleBean lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(servicePathDev);
        Resource service = wsRegistry.get(servicePathDev);
        assertNotNull(service, "Service Not found on wsRegistry path " + servicePathDev);
        assertEquals(service.getPath(), servicePathDev, "Service path changed after adding life cycle. " + servicePathDev);
        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.lifecycle.EditedCheckListServiceLC.state")[0]
                , "Commencement",
                     "LifeCycle State Mismatched");

        lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(servicePathDev);

        //life cycle check list new value
        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.0.item")[1],
                     "name:Requirements Gathered New", "Requirements Gathered New Check List Item Not Found");
        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.1.item")[1],
                     "name:Document Requirements New", "Document Requirements New Check List Item Not Found");
        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.2.item")[1],
                     "name:Architecture Diagram Finalized", "Architecture Diagram Finalize Check List Item Not Found");
        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.3.item")[1],
                     "name:Design UML Diagrams New", "Design UML Diagrams New Check List Item Not Found");
        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.4.item")[1]
                , "name:High Level Design Completed", "High Level Design Completed Check List Item Not Found");
        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.5.item")[1]
                , "name:Completion of Commencement", "Completion of Commencement  Check List Item Not Found");
    }

    @Test(groups = "wso2.greg", description = "Delete added resources", dependsOnMethods = "addServiceLifeCycleAgain")
    public void deleteResources()
            throws RegistryException, LifeCycleManagementServiceExceptionException,
                   RemoteException {
        if (servicePathDev != null) {
            wsRegistry.delete(servicePathDev);
        }

        assertTrue(lifeCycleManagementClient.deleteLifeCycle(ASPECT_NAME),
                   "Life Cycle Deleted failed");

    }

    @AfterClass
    public void cleanup()
            throws RegistryException, LifeCycleManagementServiceExceptionException,
                   RemoteException {
        if (wsRegistry.resourceExists(servicePathDev)) {
            wsRegistry.delete(servicePathDev);
        }
        LifeCycleUtils.deleteLifeCycleIfExist(ASPECT_NAME, lifeCycleManagementClient);

    }


}
