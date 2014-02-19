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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.governance.LifeCycleAdminServiceClient;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.beans.xsd.LifecycleBean;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.services.ArrayOfString;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.lifecycle.test.utils.Utils;
import org.wso2.carbon.registry.search.metadata.test.utils.GregTestUtils;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import java.rmi.RemoteException;

public class DefaultServiceLifeCycleTestWithAllDependencyTestCase {
    private String sessionCookie;

    private WSRegistryServiceClient registry;
    private LifeCycleAdminServiceClient lifeCycleAdminService;

    private final String serviceName = "echoServiceTA";
    private final String ASPECT_NAME = "ServiceLifeCycle";
    private final String ACTION_PROMOTE = "Promote";
    private final String ASS_TYPE_DEPENDS = "depends";
    //    private final String ACTION_DEMOTE = "Demote";
    private String servicePathTrunk = null;
    private String servicePathTest;
    private String[] dependencyList = null;


    @BeforeClass
    public void init() throws Exception {
        ClientConnectionUtil.waitForPort(Integer.parseInt(FrameworkSettings.HTTP_PORT));
        sessionCookie = new LoginLogoutUtil().login();
        final String SERVER_URL = GregTestUtils.getServerUrl();
        lifeCycleAdminService = new LifeCycleAdminServiceClient(SERVER_URL, sessionCookie);
        registry = GregTestUtils.getRegistry();
        Registry governance = GregTestUtils.getGovernanceRegistry(registry);

        String wsdlPath = "/_system/governance" + Utils.addWSDL("echoDependency.wsdl", governance, serviceName);
        Association[] usedBy = registry.getAssociations(wsdlPath, "usedBy");
        Assert.assertNotNull(usedBy, "WSDL usedBy Association type not found");
        for (Association association : usedBy) {
            if (association.getSourcePath().equalsIgnoreCase(wsdlPath)) {
                servicePathTrunk = association.getDestinationPath();
            }
        }
        Assert.assertNotNull(servicePathTrunk, "Service Not Found associate with WSDL");
        String policyPath = "/_system/governance" + Utils.addPolicy("UTPolicyDependency.xml", governance);
        String schemaPath = "/_system/governance" + Utils.addSchema("PersonDependency.xsd", governance);

        addDependency(servicePathTrunk, schemaPath);
        addDependency(servicePathTrunk, policyPath);

        Thread.sleep(5000);
        Association[] dependency = registry.getAssociations(servicePathTrunk, ASS_TYPE_DEPENDS);

        Assert.assertNotNull(dependency, "Dependency Not Found.");
        Assert.assertTrue(dependency.length > 0, "Dependency list empty");
        Assert.assertEquals(dependency.length, 9, "some dependency missing or additional dependency found.");

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

        Assert.assertEquals(Utils.getLifeCycleState(lifeCycle), "Development",
                            "LifeCycle State Mismatched");
        dependencyList = lifeCycleAdminService.getAllDependencies(servicePathTrunk);
        Assert.assertNotNull(dependencyList, "Dependency List Not Found");
        Assert.assertEquals(dependencyList.length, 10, "Dependency Count mismatched");

    }

    @Test(priority = 2, dependsOnMethods = {"addLifecycle"}, description = "Promote service to Test")
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

        lifeCycleAdminService.invokeAspectWithParams(servicePathTrunk, ASPECT_NAME,
                                                     ACTION_PROMOTE, null, parameters);
        Thread.sleep(2000);
        servicePathTest = "/_system/governance/branches/testing/services/org/wso2/carbon/core/services/echo/2.0.0/" + serviceName;

        verifyPromotedServiceToTest(servicePathTest);
        //dependency promoting test
        dependencyList = lifeCycleAdminService.getAllDependencies(servicePathTest);
        for (String dependency : dependencyList) {
            Assert.assertTrue(dependency.contains("branches/testing"), "dependency not created on test brunch. " + dependency);
            Assert.assertTrue(dependency.contains("2.0.0"), "dependency version mismatched" + dependency);
            registry.get(dependency);
        }

        Assert.assertEquals(registry.get(servicePathTrunk).getPath(), servicePathTrunk,
                            "Resource not exist on trunk. Preserve original not working fine");
        for (String dependency : lifeCycleAdminService.getAllDependencies(servicePathTrunk)) {
            Assert.assertTrue(dependency.contains("/trunk/"), "dependency not preserved on trunk. " + dependency);

            try {
                registry.get(dependency);
            } catch (RegistryException e) {
                Assert.fail("dependency not preserved on trunk.");
            }
        }

    }

    @Test(priority = 2, dependsOnMethods = {"promoteToTesting"}, description = "Promote service to Production")
    public void promoteToProduction()
            throws CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException,
                   RegistryException, InterruptedException {
        Thread.sleep(3000);
        dependencyList = lifeCycleAdminService.getAllDependencies(servicePathTest);
        ArrayOfString[] parameters = new ArrayOfString[11];
        for (int i = 0; i < dependencyList.length; i++) {
            parameters[i] = new ArrayOfString();
            parameters[i].setArray(new String[]{dependencyList[i], "2.0.0"});

        }

        parameters[10] = new ArrayOfString();
        parameters[10].setArray(new String[]{"preserveOriginal", "true"});

        lifeCycleAdminService.invokeAspectWithParams(servicePathTest, ASPECT_NAME,
                                                     ACTION_PROMOTE, null, parameters);
        Thread.sleep(2000);
        String servicePathProd = "/_system/governance/branches/production/services/org/wso2/carbon/core/services/echo/2.0.0/" + serviceName;

        verifyPromotedServiceToProduction(servicePathProd);
        dependencyList = lifeCycleAdminService.getAllDependencies(servicePathProd);
        for (String dependency : dependencyList) {
            Assert.assertTrue(dependency.contains("branches/production"), "dependency not created on production brunch. " + dependency);
            Assert.assertTrue(dependency.contains("2.0.0"), "dependency version mismatched" + dependency);
            registry.get(dependency);
        }

        Assert.assertEquals(registry.get(servicePathTest).getPath(), servicePathTest,
                            "Resource not exist on branch. Preserve original not working fine");

        for (String dependency : lifeCycleAdminService.getAllDependencies(servicePathTest)) {
            Assert.assertTrue(dependency.contains("branches/testing"), "dependency not preserved on trunk. " + dependency);
            Assert.assertTrue(dependency.contains("2.0.0"), "dependency version mismatched" + dependency);

            try {
                registry.get(dependency);
            } catch (RegistryException e) {
                Assert.fail("dependency not preserved on trunk.");
            }
        }
    }

    private void verifyPromotedServiceToTest(String servicePath)
            throws InterruptedException, CustomLifecyclesChecklistAdminServiceExceptionException,
                   RemoteException, RegistryException {
        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePath);
        Resource service = registry.get(servicePath);
        Assert.assertNotNull(service, "Service Not found on registry path " + servicePath);
        Assert.assertTrue(service.getPath().contains("branches/testing"), "Service not in branches/testing. " + servicePath);

        Assert.assertEquals(Utils.getLifeCycleState(lifeCycle), "Testing", "LifeCycle State Mismatched");

        Association[] dependency = registry.getAssociations(servicePath, ASS_TYPE_DEPENDS);
        Assert.assertNotNull(dependency, "Dependency Not Found.");
        Assert.assertTrue(dependency.length > 0, "Dependency list empty");

        Assert.assertEquals(dependency.length, 9, "some dependency missing");

        Assert.assertEquals(lifeCycleAdminService.getAllDependencies(servicePath).length, 10,
                            "Dependency Count mismatched");
    }

    private void verifyPromotedServiceToProduction(String servicePath)
            throws InterruptedException, CustomLifecyclesChecklistAdminServiceExceptionException,
                   RemoteException, RegistryException {
        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePath);
        Resource service = registry.get(servicePath);
        Assert.assertNotNull(service, "Service Not found on registry path " + servicePath);
        Assert.assertTrue(service.getPath().contains("branches/production"), "Service not in branches/production. " + servicePath);

        Assert.assertEquals(Utils.getLifeCycleState(lifeCycle), "Production",
                            "LifeCycle State Mismatched");

        Association[] dependency = registry.getAssociations(servicePath, ASS_TYPE_DEPENDS);
        Assert.assertNotNull(dependency, "Dependency Not Found.");
        Assert.assertTrue(dependency.length > 0, "Dependency list empty");

        Assert.assertEquals(dependency.length, 9, "Some Dependency missing");

        Assert.assertEquals(lifeCycleAdminService.getAllDependencies(servicePath).length, 10,
                            "Dependency Count mismatched");
    }

    private void addDependency(String resourcePath, String dependencyPath)
            throws RegistryException {
        registry.addAssociation(resourcePath, dependencyPath, ASS_TYPE_DEPENDS);
    }
}
