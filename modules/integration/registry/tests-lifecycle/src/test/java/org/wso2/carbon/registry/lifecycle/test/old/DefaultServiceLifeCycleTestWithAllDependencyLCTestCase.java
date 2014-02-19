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

public class DefaultServiceLifeCycleTestWithAllDependencyLCTestCase {

    private int userId = 1;
    private UserInfo userInfo = UserListCsvReader.getUserInfo(userId);

    private WSRegistryServiceClient wsRegistry;
    private LifeCycleAdminServiceClient lifeCycleAdminServiceClient;


    private final String serviceName = "echoServiceTA";
    private final String ASPECT_NAME = "ServiceLifeCycle";
    private final String ACTION_PROMOTE = "Promote";
    private final String ASS_TYPE_DEPENDS = "depends";

    private String servicePathTrunk;
    private String servicePathTest;
    private String servicePathProd;
    private String schemaPath;
    private String policyPath;
    private String wsdlPath;
    private String[] dependencyList;


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

        wsdlPath = "/_system/governance" + LifeCycleUtils.addWSDL("echoDependency.wsdl", governance, serviceName);
        Association[] usedBy = wsRegistry.getAssociations(wsdlPath, "usedBy");
        assertNotNull(usedBy, "WSDL usedBy Association type not found");
        for (Association association : usedBy) {
            if (association.getSourcePath().equalsIgnoreCase(wsdlPath)) {
                servicePathTrunk = association.getDestinationPath();
            }
        }
        assertNotNull(servicePathTrunk, "Service Not Found associate with WSDL");
        policyPath = "/_system/governance" + LifeCycleUtils.addPolicy("UTPolicyDependency.xml", governance);
        schemaPath = "/_system/governance" + LifeCycleUtils.addSchema("PersonDependency.xsd", governance);

        addDependency(servicePathTrunk, schemaPath);
        addDependency(servicePathTrunk, policyPath);

        Thread.sleep(5000);
        Association[] dependency = wsRegistry.getAssociations(servicePathTrunk, ASS_TYPE_DEPENDS);

        assertNotNull(dependency, "Dependency Not Found.");
        assertTrue(dependency.length > 0, "Dependency list empty");
        assertEquals(dependency.length, 9, "some dependency missing or additional dependency found.");

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
        LifecycleBean lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(servicePathTrunk);
        Resource service = wsRegistry.get(servicePathTrunk);
        assertNotNull(service, "Service Not found on wsRegistry path " + servicePathTrunk);
        assertTrue(service.getPath().contains("trunk"), "Service not in trunk. " + servicePathTrunk);

        assertEquals(LifeCycleUtils.getLifeCycleState(lifeCycle), "Development",
                     "LifeCycle State Mismatched");
        dependencyList = lifeCycleAdminServiceClient.getAllDependencies(servicePathTrunk);
        assertNotNull(dependencyList, "Dependency List Not Found");
        assertEquals(dependencyList.length, 10, "Dependency Count mismatched");

    }

    /**
     * @throws org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException
     *
     * @throws java.rmi.RemoteException
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
     *
     * @throws InterruptedException
     */
    @Test(groups = "wso2.greg", dependsOnMethods = {"addLifecycle"}, description = "Promote service to Test")
    public void promoteToTesting()
            throws CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException,
                   RegistryException, InterruptedException {
        ArrayOfString[] parameters = new ArrayOfString[11];
        for (int i = 0; i < dependencyList.length; i++) {
            parameters[i] = new ArrayOfString();
            parameters[i].setArray(new String[]{dependencyList[i], "2.0.0"});

        }

        parameters[10] = new ArrayOfString();
        parameters[10].setArray(new String[]{"preserveOriginal", "true"});

        lifeCycleAdminServiceClient.invokeAspectWithParams(servicePathTrunk, ASPECT_NAME,
                                                           ACTION_PROMOTE, null, parameters);
        Thread.sleep(2000);
        servicePathTest = "/_system/governance/branches/testing/services/org/wso2/carbon/core/services/echo/2.0.0/" + serviceName;

        verifyPromotedServiceToTest(servicePathTest);
        //dependency promoting test
        dependencyList = lifeCycleAdminServiceClient.getAllDependencies(servicePathTest);
        for (String dependency : dependencyList) {
            assertTrue(dependency.contains("branches/testing"), "dependency not created on test branch. " + dependency);
            assertTrue(dependency.contains("2.0.0"), "dependency version mismatched" + dependency);
            wsRegistry.get(dependency);
        }

        assertEquals(wsRegistry.get(servicePathTrunk).getPath(), servicePathTrunk,
                     "Resource not exist on trunk. Preserve original not working fine");
        for (String dependency : lifeCycleAdminServiceClient.getAllDependencies(servicePathTrunk)) {
            assertTrue(dependency.contains("/trunk/"), "dependency not preserved on trunk. " + dependency);

            try {
                wsRegistry.get(dependency);
            } catch (RegistryException e) {
                fail("dependency not preserved on trunk.");
            }
        }

    }

    /**
     * @throws org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException
     *
     * @throws java.rmi.RemoteException
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
     *
     * @throws InterruptedException
     */
    @Test(groups = "wso2.greg", dependsOnMethods = "promoteToTesting", description = "Promote service to Production")
    public void promoteToProduction()
            throws CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException,
                   RegistryException, InterruptedException {

        Thread.sleep(3000);
        dependencyList = lifeCycleAdminServiceClient.getAllDependencies(servicePathTest);
        ArrayOfString[] parameters = new ArrayOfString[11];
        for (int i = 0; i < dependencyList.length; i++) {
            parameters[i] = new ArrayOfString();
            parameters[i].setArray(new String[]{dependencyList[i], "2.0.0"});

        }

        parameters[10] = new ArrayOfString();
        parameters[10].setArray(new String[]{"preserveOriginal", "true"});

        lifeCycleAdminServiceClient.invokeAspectWithParams(servicePathTest, ASPECT_NAME,
                                                           ACTION_PROMOTE, null, parameters);
        Thread.sleep(2000);
        servicePathProd = "/_system/governance/branches/production/services/org/wso2/carbon/core/services/echo/2.0.0/" + serviceName;

        verifyPromotedServiceToProduction(servicePathProd);
        dependencyList = lifeCycleAdminServiceClient.getAllDependencies(servicePathProd);
        for (String dependency : dependencyList) {
            assertTrue(dependency.contains("branches/production"), "dependency not created on production brunch. " + dependency);
            assertTrue(dependency.contains("2.0.0"), "dependency version mismatched" + dependency);
            wsRegistry.get(dependency);
        }

        assertEquals(wsRegistry.get(servicePathTest).getPath(), servicePathTest,
                     "Resource not exist on branch. Preserve original not working fine");

        for (String dependency : lifeCycleAdminServiceClient.getAllDependencies(servicePathTest)) {
            assertTrue(dependency.contains("branches/testing"), "dependency not preserved on trunk. " + dependency);
            assertTrue(dependency.contains("2.0.0"), "dependency version mismatched" + dependency);

            try {
                wsRegistry.get(dependency);
            } catch (RegistryException e) {
                fail("dependency not preserved on trunk.");
            }
        }
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
        String policyPathTest = "/_system/governance/branches/testing/policies/2.0.0/UTPolicyDependency.xml";
        String policyPathPro = "/_system/governance/branches/production/policies/2.0.0/UTPolicyDependency.xml";

        if (servicePathTrunk != null) {
            wsRegistry.delete(servicePathTrunk);
        }
        if (servicePathTest != null) {
            wsRegistry.delete(servicePathTest);
        }
        if (servicePathProd != null) {
            wsRegistry.delete(servicePathProd);
        }
        if (schemaPath != null) {
            wsRegistry.delete(schemaPath);
        }
        if (policyPath != null) {
            wsRegistry.delete(policyPath);
        }
        if (wsRegistry.resourceExists(policyPathPro)) {
            wsRegistry.delete(policyPathPro);
        }
        if (wsRegistry.resourceExists(policyPathTest)) {
            wsRegistry.delete(policyPathTest);
        }
        if (wsdlPath != null) {
            wsRegistry.delete(wsdlPath);
        }
    }


    /**
     * @param servicePath path of the service to be promoted
     * @throws InterruptedException
     * @throws org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException
     *
     * @throws java.rmi.RemoteException
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
     *
     */
    private void verifyPromotedServiceToTest(String servicePath)
            throws InterruptedException, CustomLifecyclesChecklistAdminServiceExceptionException,
                   RemoteException, RegistryException {
        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(servicePath);
        Resource service = wsRegistry.get(servicePath);
        assertNotNull(service, "Service Not found on wsRegistry path " + servicePath);
        assertTrue(service.getPath().contains("branches/testing"), "Service not in branches/testing. " + servicePath);

        assertEquals(LifeCycleUtils.getLifeCycleState(lifeCycle), "Testing", "LifeCycle State Mismatched");

        Association[] dependency = wsRegistry.getAssociations(servicePath, ASS_TYPE_DEPENDS);
        assertNotNull(dependency, "Dependency Not Found.");
        assertTrue(dependency.length > 0, "Dependency list empty");

        assertEquals(dependency.length, 9, "some dependency missing");

        assertEquals(lifeCycleAdminServiceClient.getAllDependencies(servicePath).length, 10,
                     "Dependency Count mismatched");
    }

    /**
     * @param servicePath path of the service to be promoted
     * @throws InterruptedException
     * @throws org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException
     *
     * @throws java.rmi.RemoteException
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
     *
     */
    private void verifyPromotedServiceToProduction(String servicePath)
            throws InterruptedException, CustomLifecyclesChecklistAdminServiceExceptionException,
                   RemoteException, RegistryException {
        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(servicePath);
        Resource service = wsRegistry.get(servicePath);
        assertNotNull(service, "Service Not found on wsRegistry path " + servicePath);
        assertTrue(service.getPath().contains("branches/production"), "Service not in branches/production. " + servicePath);

        assertEquals(LifeCycleUtils.getLifeCycleState(lifeCycle), "Production",
                     "LifeCycle State Mismatched");

        Association[] dependency = wsRegistry.getAssociations(servicePath, ASS_TYPE_DEPENDS);
        assertNotNull(dependency, "Dependency Not Found.");
        assertTrue(dependency.length > 0, "Dependency list empty");

        assertEquals(dependency.length, 9, "Some Dependency missing");

        assertEquals(lifeCycleAdminServiceClient.getAllDependencies(servicePath).length, 10,
                     "Dependency Count mismatched");
    }

    /**
     * @param resourcePath   path of the resource
     * @param dependencyPath path of the dependency
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
     *
     */
    private void addDependency(String resourcePath, String dependencyPath)
            throws RegistryException {
        wsRegistry.addAssociation(resourcePath, dependencyPath, ASS_TYPE_DEPENDS);
    }


}
