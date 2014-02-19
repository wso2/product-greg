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
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.beans.xsd.LifecycleBean;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.registry.activities.stub.RegistryExceptionException;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.lifecycle.test.utils.Utils;
import org.wso2.carbon.registry.search.metadata.test.utils.GregTestUtils;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import java.rmi.RemoteException;

public class EditLifeCycleCheckListItemTestCase {

    private WSRegistryServiceClient registry;
    private LifeCycleAdminServiceClient lifeCycleAdminService;
    private LifeCycleManagementClient lifeCycleManagerAdminService;

    private final String ASPECT_NAME = "EditedCheckListServiceLC";
    private String servicePathDev;

    @BeforeClass
    public void init() throws Exception {
        ClientConnectionUtil.waitForPort(Integer.parseInt(FrameworkSettings.HTTP_PORT));
        String sessionCookie = new LoginLogoutUtil().login();
        final String SERVER_URL = GregTestUtils.getServerUrl();
        lifeCycleAdminService = new LifeCycleAdminServiceClient(SERVER_URL, sessionCookie);
        lifeCycleManagerAdminService = new LifeCycleManagementClient(SERVER_URL, sessionCookie);
        registry = GregTestUtils.getRegistry();
        Registry governance = GregTestUtils.getGovernanceRegistry(registry);

        String serviceName = "tmpServiceCustomLC";
        Utils.deleteLifeCycleIfExist(sessionCookie, ASPECT_NAME, lifeCycleManagerAdminService);
        servicePathDev = "/_system/governance" + Utils.addService("sns", serviceName, governance);
        Utils.createNewLifeCycle(ASPECT_NAME, lifeCycleManagerAdminService);
    }


    @Test(priority = 1, description = "Add LifeCycle to a service")
    public void addLifeCycleToService() throws RegistryException, InterruptedException,
                   CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException,
                   RegistryExceptionException {

        registry.associateAspect(servicePathDev, ASPECT_NAME);
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathDev);
        Resource service = registry.get(servicePathDev);
        Assert.assertNotNull(service, "Service Not found on registry path " + servicePathDev);
        Assert.assertEquals(service.getPath(), servicePathDev, "Service path changed after adding life cycle. " + servicePathDev);
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.lifecycle.EditedCheckListServiceLC.state")[0]
                , "Commencement",
                            "LifeCycle State Mismatched");

        lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathDev);

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


    }

    @Test(description = "Find LifeCycle Usage", dependsOnMethods = "addLifeCycleToService")
    public void findLifeCycleUsage() throws LifeCycleManagementServiceExceptionException, RemoteException {

        Assert.assertTrue(lifeCycleManagerAdminService.isLifecycleNameInUse(ASPECT_NAME)
                , "Life Cycle Usage Not Found");

    }


    @Test(description = "Remove LifeCycle from service", dependsOnMethods = "findLifeCycleUsage")
    public void removeLifeCycleFromService()  throws CustomLifecyclesChecklistAdminServiceExceptionException,
            RemoteException {

        lifeCycleAdminService.removeAspect(servicePathDev, ASPECT_NAME);
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathDev);
        Assert.assertNull(lifeCycle.getLifecycleProperties(), "Life Cycle property Object Found");

    }

    @Test(description = "Find LifeCycle Usage after removing life cycle from service"
            , dependsOnMethods = "removeLifeCycleFromService")
    public void findLifeCycleUsageAfterRemovingLifeCycle() throws LifeCycleManagementServiceExceptionException,
            RemoteException {

        Assert.assertFalse(lifeCycleManagerAdminService.isLifecycleNameInUse(ASPECT_NAME)
                , "Life Cycle Usage Found");

    }

    @Test(description = "Edit Life cycle Check list item names"
            , dependsOnMethods = "findLifeCycleUsageAfterRemovingLifeCycle")
    public void editLifeCycleCheckList() throws LifeCycleManagementServiceExceptionException,
            RemoteException,InterruptedException {

        String config = lifeCycleManagerAdminService.getLifecycleConfiguration(ASPECT_NAME);
        Assert.assertTrue(config.contains("aspect name=\"" + ASPECT_NAME + "\""),
                          "LifeCycleName Not Found in lifecycle configuration");
        String newLifeCycleConfiguration = config.replace("item name=\"Requirements Gathered\"", "item name=\"Requirements Gathered New\"");
        newLifeCycleConfiguration = newLifeCycleConfiguration.replace("item name=\"Document Requirements\"", "item name=\"Document Requirements New\"");
        newLifeCycleConfiguration = newLifeCycleConfiguration.replace("item name=\"Design UML Diagrams\"", "item name=\"Design UML Diagrams New\"");
        Assert.assertTrue(lifeCycleManagerAdminService.editLifeCycle(ASPECT_NAME, newLifeCycleConfiguration)
                , "Editing LifeCycle Name Failed");
        Thread.sleep(1000);


        String[] lifeCycleList = lifeCycleManagerAdminService.getLifecycleList();
        Assert.assertNotNull(lifeCycleList);
        Assert.assertTrue(lifeCycleList.length > 0, "Life Cycle List length zero");
        boolean found = false;
        for (String lc : lifeCycleList) {
            if (ASPECT_NAME.equalsIgnoreCase(lc)) {
                found = true;
            }
        }
        Assert.assertTrue(found, "Life Cycle list not contain edited life cycle");
    }

    @Test(description = "Add Edited Life Cycle to a service", dependsOnMethods = "editLifeCycleCheckList")
    public void addServiceLifeCycleAgain() throws RegistryException, CustomLifecyclesChecklistAdminServiceExceptionException,
                   RemoteException, InterruptedException {

        registry.associateAspect(servicePathDev, ASPECT_NAME);
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathDev);
        Resource service = registry.get(servicePathDev);
        Assert.assertNotNull(service, "Service Not found on registry path " + servicePathDev);
        Assert.assertEquals(service.getPath(), servicePathDev, "Service path changed after adding life cycle. " + servicePathDev);
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.lifecycle.EditedCheckListServiceLC.state")[0]
                , "Commencement",
                            "LifeCycle State Mismatched");

        lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathDev);

        //life cycle check list new value
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.0.item")[1],
                            "name:Requirements Gathered New", "Requirements Gathered New Check List Item Not Found");
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.1.item")[1],
                            "name:Document Requirements New", "Document Requirements New Check List Item Not Found");
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.2.item")[1],
                            "name:Architecture Diagram Finalized", "Architecture Diagram Finalize Check List Item Not Found");
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.3.item")[1],
                            "name:Design UML Diagrams New", "Design UML Diagrams New Check List Item Not Found");
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.4.item")[1]
                , "name:High Level Design Completed", "High Level Design Completed Check List Item Not Found");
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.5.item")[1]
                , "name:Completion of Commencement", "Completion of Commencement  Check List Item Not Found");
    }

    @AfterClass
    public void destroy() throws RegistryException, LifeCycleManagementServiceExceptionException,
                   RemoteException {
        if (servicePathDev != null) {
            registry.delete(servicePathDev);
        }
        registry = null;
        lifeCycleAdminService = null;
    }

}
