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
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.beans.xsd.LifecycleBean;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.services.ArrayOfString;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.registry.activities.stub.RegistryExceptionException;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.lifecycle.test.utils.LifeCycleUtils;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import java.rmi.RemoteException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class DefaultServiceLCTestCase {

    private int userId = 1;
    private UserInfo userInfo = UserListCsvReader.getUserInfo(userId);

    private WSRegistryServiceClient wsRegistry;
    private LifeCycleAdminServiceClient lifeCycleAdminServiceClient;

    private final String serviceName = "serviceForLifeCycleTest";
    private final String aspectName = "ServiceLifeCycle";
    private final String ACTION_PROMOTE = "Promote";
    private String servicePathDev;
    private String servicePathTest;
    private String servicePathProd;
    private String prodBranch;
    private String testBranch;

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

        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        wsRegistry = registryProviderUtil.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        Registry governance = registryProviderUtil.getGovernanceRegistry(wsRegistry, userId);

        servicePathDev = "/_system/governance" + LifeCycleUtils.addService("sns", serviceName, governance);
        Thread.sleep(1000);

    }

    @Test(groups = "wso2.greg", description = "Add lifecycle to a service")
    public void addLifecycle()
            throws RegistryException, CustomLifecyclesChecklistAdminServiceExceptionException,
                   RemoteException, InterruptedException, RegistryExceptionException {
        wsRegistry.associateAspect(servicePathDev, aspectName);
        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(servicePathDev);
        Resource service = wsRegistry.get(servicePathDev);
        assertNotNull(service, "Service Not found on wsRegistry path " + servicePathDev);
        assertEquals(service.getPath(), servicePathDev, "Service path changed after adding life cycle. " + servicePathDev);
        assertEquals(LifeCycleUtils.getLifeCycleState(lifeCycle), "Development", "LifeCycle State Mismatched");

        //life cycle check list
        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.0.item")[1],
                     "name:Code Completed", "Code Completed Check List Item Not Found");
        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.1.item")[1],
                     "name:WSDL, Schema Created", "WSDL, Schema Created Check List Item Not Found");
        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.2.item")[1],
                     "name:QoS Created", "QoS Created Check List Item Not Found");
    }

    @Test(groups = "wso2.greg", dependsOnMethods = "addLifecycle", description = "Promote Service")
    public void promoteServiceToTesting()
            throws CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException,
                   InterruptedException, RegistryException, RegistryExceptionException {
        Thread.sleep(1000);
        lifeCycleAdminServiceClient.invokeAspect(servicePathDev, aspectName, ACTION_PROMOTE, null);
        servicePathTest = "/_system/governance/branches/testing/services/sns/1.0.0-SNAPSHOT/" + serviceName;
        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(servicePathTest);
        Resource service = wsRegistry.get(servicePathTest);
        assertNotNull(service, "Service Not found on wsRegistry path " + servicePathTest);
        assertEquals(service.getPath(), servicePathTest, "Service not in branches/testing. " + servicePathTest);

        assertEquals(LifeCycleUtils.getLifeCycleState(lifeCycle), "Testing",
                     "LifeCycle State Mismatched");

        assertEquals(wsRegistry.get(servicePathDev).getPath(), servicePathDev, "Preserve original failed");

        //life cycle check list
        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.0.item")[1],
                     "name:Effective Inspection Completed", "Effective Inspection Completed Check List Item Not Found");
        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.1.item")[1],
                     "name:Test Cases Passed", "Test Cases Passed  Check List Item Not Found");
        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.2.item")[1],
                     "name:Smoke Test Passed", "Smoke Test Passed Check List Item Not Found");
    }

    @Test(groups = "wso2.greg", dependsOnMethods = "promoteServiceToTesting", description = "Promote Service")
    public void promoteServiceToProduction()
            throws CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException,
                   InterruptedException, RegistryException, RegistryExceptionException {
        Thread.sleep(1000);
        lifeCycleAdminServiceClient.invokeAspect(servicePathTest, aspectName, ACTION_PROMOTE, null);
        servicePathProd = "/_system/governance/branches/production/services/sns/1.0.0-SNAPSHOT/" + serviceName;
        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(servicePathProd);

        Resource service = wsRegistry.get(servicePathProd);
        assertNotNull(service, "Service Not found on wsRegistry path " + servicePathProd);
        assertEquals(service.getPath(), servicePathProd, "Service not in branches/production. " + servicePathProd);

        assertEquals(LifeCycleUtils.getLifeCycleState(lifeCycle), "Production",
                     "LifeCycle State Mismatched");

        assertEquals(wsRegistry.get(servicePathTest).getPath(), servicePathTest, "Preserve original failed");
    }

    @Test(groups = "wso2.greg", dependsOnMethods = "promoteServiceToProduction", description = "Promote Service")
    public void demoteServiceToTesting()
            throws CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException,
                   InterruptedException, RegistryException, RegistryExceptionException {
        Thread.sleep(1000);
        String ACTION_DEMOTE = "Demote";
        lifeCycleAdminServiceClient.invokeAspect(servicePathProd, aspectName, ACTION_DEMOTE, null);
        servicePathTest = "/_system/governance/branches/testing/services/sns/1.0.0-SNAPSHOT/" + serviceName;
        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(servicePathTest);
        Resource service = wsRegistry.get(servicePathTest);
        assertNotNull(service, "Service Not found on wsRegistry path " + servicePathTest);
        assertEquals(service.getPath(), servicePathTest, "Service not in branches/testing. " + servicePathTest);

        assertEquals(LifeCycleUtils.getLifeCycleState(lifeCycle), "Testing",
                     "LifeCycle State Mismatched");
        //life cycle check list
        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.0.item")[1],
                     "name:Effective Inspection Completed", "Effective Inspection Completed Check List Item Not Found");
        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.1.item")[1],
                     "name:Test Cases Passed", "Test Cases Passed  Check List Item Not Found");
        assertEquals(LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.2.item")[1],
                     "name:Smoke Test Passed", "Smoke Test Passed Check List Item Not Found");
    }

    @Test(groups = "wso2.greg", dependsOnMethods = "addLifecycle", description = "Promote Service to testing with new version")
    public void promoteServiceToTestingWithNewVersion()
            throws CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException,
                   InterruptedException, RegistryException, RegistryExceptionException {
        Thread.sleep(1000);
        ArrayOfString[] parameters = new ArrayOfString[1];
        parameters[0] = new ArrayOfString();
        parameters[0].setArray(new String[]{servicePathDev, "1.0.0"});

        lifeCycleAdminServiceClient.invokeAspectWithParams(servicePathDev, aspectName,
                                                           ACTION_PROMOTE, null, parameters);
        testBranch = "/_system/governance/branches/testing/services/sns/1.0.0/" + serviceName;
        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(testBranch);
        Resource service = wsRegistry.get(testBranch);
        assertNotNull(service, "Service Not found on wsRegistry path " + testBranch);
        assertEquals(service.getPath(), testBranch, "Service not in branches/testing. " + testBranch);

        assertEquals(LifeCycleUtils.getLifeCycleState(lifeCycle), "Testing",
                     "LifeCycle State Mismatched");

        assertEquals(wsRegistry.get(servicePathDev).getPath(), servicePathDev, "Preserve original failed");
    }

    @Test(groups = "wso2.greg", dependsOnMethods = "promoteServiceToTestingWithNewVersion",
          description = "Promote Service to production with new version")
    public void promoteServiceToProductionWithNewVersion()
            throws CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException,
                   InterruptedException, RegistryException, RegistryExceptionException {
        Thread.sleep(1000);
        assertNotNull(testBranch, "test brunch path not found");
        ArrayOfString[] parameters = new ArrayOfString[1];
        parameters[0] = new ArrayOfString();
        parameters[0].setArray(new String[]{testBranch, "1.0.0"});

        lifeCycleAdminServiceClient.invokeAspectWithParams(testBranch, aspectName, ACTION_PROMOTE
                , null, parameters);
        prodBranch = "/_system/governance/branches/production/services/sns/1.0.0/" + serviceName;
        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(prodBranch);

        Resource service = wsRegistry.get(prodBranch);
        assertNotNull(service, "Service Not found on wsRegistry path " + prodBranch);
        assertEquals(service.getPath(), prodBranch, "Service not in branches/production. " + prodBranch);

        assertEquals(LifeCycleUtils.getLifeCycleState(lifeCycle), "Production",
                     "LifeCycle State Mismatched");

        assertEquals(wsRegistry.get(testBranch).getPath(), testBranch, "Preserve original failed");
    }

    /**
     * @throws RegistryException
     * @throws LifeCycleManagementServiceExceptionException
     *
     * @throws RemoteException
     */
    @AfterClass()
    public void cleanup()
            throws RegistryException, LifeCycleManagementServiceExceptionException,
                   RemoteException {
        if (servicePathDev != null) {
            wsRegistry.delete(servicePathDev);
        }
        if (servicePathTest != null) {
            wsRegistry.delete(servicePathTest);
        }
        if (servicePathProd != null) {
            wsRegistry.delete(servicePathProd);
        }
        if (testBranch != null) {
            wsRegistry.delete(testBranch);
        }
        if (prodBranch != null) {
            wsRegistry.delete(prodBranch);
        }

    }
}
