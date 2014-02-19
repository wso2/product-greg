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
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.lifecycle.test.utils.LifeCycleUtils;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import java.rmi.RemoteException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public class PreserveOriginalWithDependencyLCTestCase {

    private int userId = 1;
    private UserInfo userInfo = UserListCsvReader.getUserInfo(userId);

    private WSRegistryServiceClient wsRegistry;
    private LifeCycleAdminServiceClient lifeCycleAdminServiceClient;

    private final String serviceName = "servicePreserveFalseOriginalWithDependency";
    private final String serviceNamePreserve = "servicePreserveOriginalWithDependency";
    private final String serviceDependencyName = "PreserveFalseUTPolicyDependency.xml";
    private final String serviceDependencyNamePreserve = "PreserveUTPolicyDependency.xml";

    private final String aspectName = "ServiceLifeCycle";
    private final String ACTION_PROMOTE = "Promote";
    private final String ASS_TYPE_DEPENDS = "depends";
    private String servicePathDev;
    private String servicePathDevPreserve;
    private String servicePathTestPreserve;
    private String servicePathTest;
    private String servicePathProd;
    private String proBranch;

    private String policyPathDev;
    private String policyPathDevPreserve;
    private String policyPathTestPreserve;
    private String policyPathTest;
    private String policyPathProdPreserve;
    private String policyPathProd;

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

        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        wsRegistry = registryProviderUtil.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        Registry governance = registryProviderUtil.getGovernanceRegistry(wsRegistry, userId);

        servicePathDev = "/_system/governance" + LifeCycleUtils.addService("sns", serviceName, governance);
        policyPathDev = "/_system/governance" + LifeCycleUtils.addPolicy(serviceDependencyName, governance);
        addDependency(servicePathDev, policyPathDev);
        Thread.sleep(1000);
        Association[] dependency = wsRegistry.getAssociations(servicePathDev, ASS_TYPE_DEPENDS);
        assertNotNull(dependency, "Dependency Not Found.");
        assertTrue(dependency.length > 0, "Dependency list empty");
        assertTrue(dependency.length == 1, "Additional dependency found");
        assertEquals(dependency[0].getDestinationPath(), policyPathDev, "Dependency Name mismatched");
        addLifecycle(servicePathDev);

        servicePathDevPreserve = "/_system/governance" + LifeCycleUtils.addService("sns", serviceNamePreserve, governance);
        policyPathDevPreserve = "/_system/governance" + LifeCycleUtils.addPolicy(serviceDependencyNamePreserve, governance);
        addDependency(servicePathDevPreserve, policyPathDevPreserve);
        addLifecycle(servicePathDevPreserve);

    }


    /**
     * @throws org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException
     *
     * @throws java.rmi.RemoteException
     * @throws InterruptedException
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
     *
     */
    @Test(groups = "wso2.greg", description = "Promote Service with dependency")
    public void preserveOriginalFalseAndPromoteToTestingWithDependency()
            throws CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException,
                   InterruptedException, RegistryException {
        Thread.sleep(1000);
        ArrayOfString[] parameters = new ArrayOfString[3];
        parameters[0] = new ArrayOfString();
        parameters[0].setArray(new String[]{servicePathDev, "1.0.0"});

        parameters[1] = new ArrayOfString();
        parameters[1].setArray(new String[]{policyPathDev, "1.0.0"});

        parameters[2] = new ArrayOfString();
        parameters[2].setArray(new String[]{"preserveOriginal", "false"});

        lifeCycleAdminServiceClient.invokeAspectWithParams(servicePathDev, aspectName,
                                                           ACTION_PROMOTE, null, parameters);
        servicePathTest = "/_system/governance/branches/testing/services/sns/1.0.0/" + serviceName;
        policyPathTest = "/_system/governance/branches/testing/policies/1.0.0/" + serviceDependencyName;

        verifyPromotedServiceToTest(servicePathTest, policyPathTest);

        Thread.sleep(5000);
        try {
            wsRegistry.get(servicePathDev);
            fail(servicePathDev + "Preserve original failed for service. Resource exist");
        } catch (RegistryException e) {
            assertTrue(e.getCause().getMessage().contains("Resource does not exist at path /_system/governance/trunk/services/"));
        }

        assertEquals(wsRegistry.get(policyPathDev).getPath(), policyPathDev, "Dependency also deleted.");

    }

    /**
     * @throws org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException
     *
     * @throws java.rmi.RemoteException
     * @throws InterruptedException
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
     *
     */
    @Test(groups = "wso2.greg", dependsOnMethods = {"preserveOriginalFalseAndPromoteToTestingWithDependency"},
          description = "Promote Service to production with dependency")
    public void preserveOriginalFalseAndPromoteToProductionWithDependency()
            throws CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException,
                   InterruptedException, RegistryException {
        Thread.sleep(1000);
        ArrayOfString[] parameters = new ArrayOfString[3];
        parameters[0] = new ArrayOfString();
        parameters[0].setArray(new String[]{servicePathTest, "1.0.0"});

        parameters[1] = new ArrayOfString();
        parameters[1].setArray(new String[]{policyPathTest, "1.0.0"});

        parameters[2] = new ArrayOfString();
        parameters[2].setArray(new String[]{"preserveOriginal", "false"});

        lifeCycleAdminServiceClient.invokeAspectWithParams(servicePathTest, aspectName,
                                                           ACTION_PROMOTE, null, parameters);

        servicePathProd = "/_system/governance/branches/production/services/sns/1.0.0/" + serviceName;
        policyPathProd = "/_system/governance/branches/production/policies/1.0.0/" + serviceDependencyName;

        verifyPromotedServiceToProduction(servicePathProd, policyPathProd);

        Thread.sleep(5000);
        try {
            wsRegistry.get(servicePathTest);
            fail(servicePathTest + "Preserve original failed for service. Resource exist");
        } catch (RegistryException e) {
            assertTrue(e.getCause().getMessage().contains("Resource does not exist at path /_system/governance/branches/testing/services/"));
        }

        assertEquals(wsRegistry.get(policyPathTest).getPath(), policyPathTest, "Dependency also deleted.");
    }

    /**
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Promote Service and preserve original", dependsOnMethods = "preserveOriginalFalseAndPromoteToProductionWithDependency")
    public void preserveOriginalAndPromoteToTestingWithDependency() throws Exception {
        Thread.sleep(1000);

        ArrayOfString[] parameters = new ArrayOfString[3];
        parameters[0] = new ArrayOfString();
        parameters[0].setArray(new String[]{servicePathDevPreserve, "1.0.0"});

        parameters[1] = new ArrayOfString();
        parameters[1].setArray(new String[]{policyPathDevPreserve, "1.0.0"});

        parameters[2] = new ArrayOfString();
        parameters[2].setArray(new String[]{"preserveOriginal", "true"});

        lifeCycleAdminServiceClient.invokeAspectWithParams(servicePathDevPreserve, aspectName,
                                                           ACTION_PROMOTE, null, parameters);
        servicePathTestPreserve = "/_system/governance/branches/testing/services/sns/1.0.0/" + serviceNamePreserve;
        policyPathTestPreserve = "/_system/governance/branches/testing/policies/1.0.0/" + serviceDependencyNamePreserve;

        verifyPromotedServiceToTest(servicePathTestPreserve, policyPathTestPreserve);
        // test preserve
        assertEquals(wsRegistry.get(servicePathDevPreserve).getPath(), servicePathDevPreserve, "Resource not exist on trunk");
        assertEquals(wsRegistry.get(policyPathDevPreserve).getPath(), policyPathDevPreserve, "Dependency also not exist on trunk.");

    }

    /**
     * @throws org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException
     *
     * @throws java.rmi.RemoteException
     * @throws InterruptedException
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
     *
     */
    @Test(groups = "wso2.greg", dependsOnMethods = {"preserveOriginalAndPromoteToTestingWithDependency"},
          description = "Promote Service and preserve original")
    public void preserveOriginalAndPromoteToProductionWithDependency()
            throws CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException,
                   InterruptedException, RegistryException {
        Thread.sleep(1000);
        ArrayOfString[] parameters = new ArrayOfString[3];
        parameters[0] = new ArrayOfString();
        parameters[0].setArray(new String[]{servicePathTestPreserve, "1.0.0"});

        parameters[1] = new ArrayOfString();
        parameters[1].setArray(new String[]{policyPathTestPreserve, "1.0.0"});

        parameters[2] = new ArrayOfString();
        parameters[2].setArray(new String[]{"preserveOriginal", "true"});

        lifeCycleAdminServiceClient.invokeAspectWithParams(servicePathTestPreserve, aspectName,
                                                           ACTION_PROMOTE, null, parameters);
        proBranch = "/_system/governance/branches/production/services/sns/1.0.0/" + serviceNamePreserve;
        policyPathProdPreserve = "/_system/governance/branches/production/policies/1.0.0/" + serviceDependencyNamePreserve;

        verifyPromotedServiceToProduction(proBranch, policyPathProdPreserve);

        assertEquals(wsRegistry.get(servicePathTestPreserve).getPath(), servicePathTestPreserve, "Resource not exist on branch");
        assertEquals(wsRegistry.get(policyPathTestPreserve).getPath(), policyPathTestPreserve, "Dependency also not exist on brunch.");
    }

    @AfterClass()
    public void cleanup()
            throws RegistryException, LifeCycleManagementServiceExceptionException,
                   RemoteException {
        if (servicePathProd != null) {
            wsRegistry.delete(servicePathProd);
        }
        if (policyPathDev != null) {
            wsRegistry.delete(policyPathDev);
        }
        if (policyPathTest != null) {
            wsRegistry.delete(policyPathTest);
        }
        if (policyPathProd != null) {
            wsRegistry.delete(policyPathProd);
        }
        if (servicePathTestPreserve != null) {
            wsRegistry.delete(servicePathTestPreserve);
        }
        if (policyPathTestPreserve != null) {
            wsRegistry.delete(policyPathTestPreserve);
        }
        if (servicePathDevPreserve != null) {
            wsRegistry.delete(servicePathDevPreserve);
        }
        if (policyPathDevPreserve != null) {
            wsRegistry.delete(policyPathDevPreserve);
        }
        if (proBranch != null) {
            wsRegistry.delete(proBranch);
        }
        if (policyPathProdPreserve != null) {
            wsRegistry.delete(policyPathProdPreserve);
        }
    }


    /**
     * @param servicePtah path of the resource
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
     *
     * @throws org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException
     *
     * @throws java.rmi.RemoteException
     * @throws InterruptedException
     */
    private void addLifecycle(String servicePtah)
            throws RegistryException, CustomLifecyclesChecklistAdminServiceExceptionException,
                   RemoteException, InterruptedException {
        wsRegistry.associateAspect(servicePtah, aspectName);
        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(servicePtah);
        Resource service = wsRegistry.get(servicePtah);
        assertNotNull(service, "Service Not found on registry path " + servicePtah);
        assertTrue(service.getPath().contains("trunk"), "Service not in trunk. " + servicePtah);
        assertTrue((lifeCycle.getLifecycleProperties().length > 5), "LifeCycle properties missing some properties");
        assertNotNull(lifeCycle.getLifecycleProperties()[4], "LifeCycle State property not found");
        assertEquals(lifeCycle.getLifecycleProperties()[4].getKey(), "registry.lifecycle.ServiceLifeCycle.state",
                     "LifeCycle State property not found");
        assertNotNull(lifeCycle.getLifecycleProperties()[4].getValues(), "State Value Not Found");
        assertEquals(lifeCycle.getLifecycleProperties()[4].getValues()[0], "Development",
                     "LifeCycle State Mismatched");

        assertTrue((lifeCycleAdminServiceClient.getAllDependencies(servicePtah).length == 2),
                   "Dependency Count mismatched");


    }

    /**
     * @param servicePath    path of the resource
     * @param dependencyPath path of the dependency
     * @throws InterruptedException
     * @throws org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException
     *
     * @throws java.rmi.RemoteException
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
     *
     */
    private void verifyPromotedServiceToTest(String servicePath, String dependencyPath)
            throws InterruptedException, CustomLifecyclesChecklistAdminServiceExceptionException,
                   RemoteException, RegistryException {
        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(servicePath);
        Resource service = wsRegistry.get(servicePath);
        assertNotNull(service, "Service Not found on registry path " + servicePath);
        assertTrue(service.getPath().contains("branches/testing"), "Service not in branches/testing. " + servicePath);

        assertEquals(LifeCycleUtils.getLifeCycleState(lifeCycle), "Testing", "LifeCycle State Mismatched");

        Association[] dependency = wsRegistry.getAssociations(servicePath, ASS_TYPE_DEPENDS);
        assertNotNull(dependency, "Dependency Not Found.");
        assertTrue(dependency.length > 0, "Dependency list empty");

        assertEquals(dependency[0].getDestinationPath(), dependencyPath, "Dependency Name mismatched");

        assertTrue((lifeCycleAdminServiceClient.getAllDependencies(servicePath).length == 2),
                   "Dependency Count mismatched");
    }

    /**
     * @param servicePath    path of the resource
     * @param dependencyPath path of the dependency
     * @throws InterruptedException
     * @throws org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException
     *
     * @throws java.rmi.RemoteException
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
     *
     */
    private void verifyPromotedServiceToProduction(String servicePath, String dependencyPath)
            throws InterruptedException, CustomLifecyclesChecklistAdminServiceExceptionException,
                   RemoteException, RegistryException {
        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(servicePath);
        Resource service = wsRegistry.get(servicePath);
        assertNotNull(service, "Service Not found on registry path " + servicePath);
        assertTrue(service.getPath().contains("branches/production"), "Service not in branches/production. " + servicePath);

        assertEquals(LifeCycleUtils.getLifeCycleState(lifeCycle), "Production", "LifeCycle State Mismatched");

        Association[] dependency = wsRegistry.getAssociations(servicePath, ASS_TYPE_DEPENDS);
        assertNotNull(dependency, "Dependency Not Found.");
        assertTrue(dependency.length > 0, "Dependency list empty");

        assertEquals(dependency[0].getDestinationPath(), dependencyPath, "Dependency Name mismatched");

        assertTrue((lifeCycleAdminServiceClient.getAllDependencies(servicePath).length == 2),
                   "Dependency Count mismatched");
    }

    /**
     * @param resourcePath   path of the resources
     * @param dependencyPath path of the dependency
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
     *
     */
    private void addDependency(String resourcePath, String dependencyPath)
            throws RegistryException {
        wsRegistry.addAssociation(resourcePath, dependencyPath, ASS_TYPE_DEPENDS);
    }
}
