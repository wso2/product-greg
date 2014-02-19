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

public class PreserveOriginalWithDependencyTestCase {
    private String sessionCookie;

    private WSRegistryServiceClient registry;
    private LifeCycleAdminServiceClient lifeCycleAdminService;

    private final String serviceName = "servicePreserveFalseOriginalWithDependency";
    private final String serviceNamePreserve = "servicePreserveOriginalWithDependency";
    private final String serviceDependencyName = "PreserveFalseUTPolicyDependency.xml";
    private final String serviceDependencyNamePreserve = "PreserveUTPolicyDependency.xml";

    private final String aspectName = "ServiceLifeCycle";
    private final String ACTION_PROMOTE = "Promote";
    private final String ASS_TYPE_DEPENDS = "depends";
    //    private final String ACTION_DEMOTE = "Demote";
    private String servicePathDev;
    private String servicePathDevPreserve;
    private String servicePathTest;

    private String policyPathDev;
    private String policyPathDevPreserve;
    private String policyPathTest;

    @BeforeClass
    public void init() throws Exception {
        ClientConnectionUtil.waitForPort(Integer.parseInt(FrameworkSettings.HTTP_PORT));
        sessionCookie = new LoginLogoutUtil().login();
        final String SERVER_URL = GregTestUtils.getServerUrl();
        lifeCycleAdminService = new LifeCycleAdminServiceClient(SERVER_URL, sessionCookie);
        registry = GregTestUtils.getRegistry();
        Registry governance = GregTestUtils.getGovernanceRegistry(registry);

        servicePathDev = "/_system/governance" + Utils.addService("sns", serviceName, governance);
        policyPathDev = "/_system/governance" + Utils.addPolicy(serviceDependencyName, governance);
        addDependency(servicePathDev, policyPathDev);
        Thread.sleep(1000);
        Association[] dependency = registry.getAssociations(servicePathDev, ASS_TYPE_DEPENDS);
        Assert.assertNotNull(dependency, "Dependency Not Found.");
        Assert.assertTrue(dependency.length > 0, "Dependency list empty");
        Assert.assertTrue(dependency.length == 1, "Additional dependency found");
        Assert.assertEquals(dependency[0].getDestinationPath(), policyPathDev, "Dependency Name mismatched");
        addLifecycle(servicePathDev);

        servicePathDevPreserve = "/_system/governance" + Utils.addService("sns", serviceNamePreserve, governance);
        policyPathDevPreserve = "/_system/governance" + Utils.addPolicy(serviceDependencyNamePreserve, governance);
        addDependency(servicePathDevPreserve, policyPathDevPreserve);
        addLifecycle(servicePathDevPreserve);

    }


    @Test(priority = 1, description = "Promote Service with dependency")
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

        lifeCycleAdminService.invokeAspectWithParams(servicePathDev, aspectName,
                                                     ACTION_PROMOTE, null, parameters);
        servicePathTest = "/_system/governance/branches/testing/services/sns/1.0.0/" + serviceName;
        policyPathTest = "/_system/governance/branches/testing/policies/1.0.0/" + serviceDependencyName;

        verifyPromotedServiceToTest(servicePathTest, policyPathTest);

        Thread.sleep(5000);
        try {
            registry.get(servicePathDev);
            Assert.fail(servicePathDev + "Preserve original failed for service. Resource exist");
        } catch (RegistryException e) {
            Assert.assertTrue(e.getCause().getMessage().contains("Resource does not exist at path /_system/governance/trunk/services/"));
        }

        Assert.assertEquals(registry.get(policyPathDev).getPath(), policyPathDev, "Dependency also deleted.");

    }


    @Test(priority = 2, dependsOnMethods = {"preserveOriginalFalseAndPromoteToTestingWithDependency"},
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

        lifeCycleAdminService.invokeAspectWithParams(servicePathTest, aspectName,
                                                     ACTION_PROMOTE, null, parameters);

        String servicePathProd = "/_system/governance/branches/production/services/sns/1.0.0/" + serviceName;
        String policyPathProd = "/_system/governance/branches/production/policies/1.0.0/" + serviceDependencyName;

        verifyPromotedServiceToProduction(servicePathProd, policyPathProd);

        Thread.sleep(5000);
        try {
            registry.get(servicePathTest);
            Assert.fail(servicePathTest + "Preserve original failed for service. Resource exist");
        } catch (RegistryException e) {
            Assert.assertTrue(e.getCause().getMessage().contains("Resource does not exist at path /_system/governance/branches/testing/services/"));
        }

        Assert.assertEquals(registry.get(policyPathTest).getPath(), policyPathTest, "Dependency also deleted.");
    }

    @Test(priority = 3, description = "Promote Service and preserve original")
    public void preserveOriginalAndPromoteToTestingWithDependency() throws Exception {
        Thread.sleep(1000);

        ArrayOfString[] parameters = new ArrayOfString[3];
        parameters[0] = new ArrayOfString();
        parameters[0].setArray(new String[]{servicePathDevPreserve, "1.0.0"});

        parameters[1] = new ArrayOfString();
        parameters[1].setArray(new String[]{policyPathDevPreserve, "1.0.0"});

        parameters[2] = new ArrayOfString();
        parameters[2].setArray(new String[]{"preserveOriginal", "true"});

        lifeCycleAdminService.invokeAspectWithParams(servicePathDevPreserve, aspectName,
                                                     ACTION_PROMOTE, null, parameters);
        servicePathTest = "/_system/governance/branches/testing/services/sns/1.0.0/" + serviceNamePreserve;
        policyPathTest = "/_system/governance/branches/testing/policies/1.0.0/" + serviceDependencyNamePreserve;

        verifyPromotedServiceToTest(servicePathTest, policyPathTest);
        // test preserve
        Assert.assertEquals(registry.get(servicePathDevPreserve).getPath(), servicePathDevPreserve, "Resource not exist on trunk");
        Assert.assertEquals(registry.get(policyPathDevPreserve).getPath(), policyPathDevPreserve, "Dependency also not exist on trunk.");

    }

    @Test(priority = 4, dependsOnMethods = {"preserveOriginalAndPromoteToTestingWithDependency"},
          description = "Promote Service and preserve original")
    public void preserveOriginalAndPromoteToProductionWithDependency()
            throws CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException,
                   InterruptedException, RegistryException {
        Thread.sleep(1000);
        ArrayOfString[] parameters = new ArrayOfString[3];
        parameters[0] = new ArrayOfString();
        parameters[0].setArray(new String[]{servicePathTest, "1.0.0"});

        parameters[1] = new ArrayOfString();
        parameters[1].setArray(new String[]{policyPathTest, "1.0.0"});

        parameters[2] = new ArrayOfString();
        parameters[2].setArray(new String[]{"preserveOriginal", "true"});

        lifeCycleAdminService.invokeAspectWithParams(servicePathTest, aspectName,
                                                     ACTION_PROMOTE, null, parameters);
        String proBranch = "/_system/governance/branches/production/services/sns/1.0.0/" + serviceNamePreserve;
        String policyPathProd = "/_system/governance/branches/production/policies/1.0.0/" + serviceDependencyNamePreserve;

        verifyPromotedServiceToProduction(proBranch, policyPathProd);

        Assert.assertEquals(registry.get(servicePathTest).getPath(), servicePathTest, "Resource not exist on branch");
        Assert.assertEquals(registry.get(policyPathTest).getPath(), policyPathTest, "Dependency also not exist on brunch.");
    }

    @AfterClass
    public void destroy() {
        servicePathDev = null;
        servicePathTest = null;

    }


    private void addLifecycle(String servicePtah)
            throws RegistryException, CustomLifecyclesChecklistAdminServiceExceptionException,
                   RemoteException, InterruptedException {
        registry.associateAspect(servicePtah, aspectName);
        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePtah);
        Resource service = registry.get(servicePtah);
        Assert.assertNotNull(service, "Service Not found on registry path " + servicePtah);
        Assert.assertTrue(service.getPath().contains("trunk"), "Service not in trunk. " + servicePtah);
        Assert.assertTrue((lifeCycle.getLifecycleProperties().length > 5), "LifeCycle properties missing some properties");

        int statePropIndex = -1;
        for (int i = 0; i < lifeCycle.getLifecycleProperties().length; i++) {
            if (lifeCycle.getLifecycleProperties()[i].getKey().equals("registry.lifecycle.ServiceLifeCycle.state")) {
                statePropIndex = i;
                break;
            }
        }
        Assert.assertNotEquals(statePropIndex, -1, "LifeCycle State property not found");
        Assert.assertNotNull(lifeCycle.getLifecycleProperties()[statePropIndex].getValues(), "State Value Not Found");
        Assert.assertEquals(lifeCycle.getLifecycleProperties()[statePropIndex].getValues()[0], "Development",
                            "LifeCycle State Mismatched");

        Assert.assertTrue((lifeCycleAdminService.getAllDependencies(servicePtah).length == 2),
                          "Dependency Count mismatched");


    }

    private void verifyPromotedServiceToTest(String servicePath, String dependencyPath)
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

        Assert.assertEquals(dependency[0].getDestinationPath(), dependencyPath, "Dependency Name mismatched");

        Assert.assertTrue((lifeCycleAdminService.getAllDependencies(servicePath).length == 2),
                          "Dependency Count mismatched");
    }

    private void verifyPromotedServiceToProduction(String servicePath, String dependencyPath)
            throws InterruptedException, CustomLifecyclesChecklistAdminServiceExceptionException,
                   RemoteException, RegistryException {
        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePath);
        Resource service = registry.get(servicePath);
        Assert.assertNotNull(service, "Service Not found on registry path " + servicePath);
        Assert.assertTrue(service.getPath().contains("branches/production"), "Service not in branches/production. " + servicePath);

        Assert.assertEquals(Utils.getLifeCycleState(lifeCycle), "Production", "LifeCycle State Mismatched");

        Association[] dependency = registry.getAssociations(servicePath, ASS_TYPE_DEPENDS);
        Assert.assertNotNull(dependency, "Dependency Not Found.");
        Assert.assertTrue(dependency.length > 0, "Dependency list empty");

        Assert.assertEquals(dependency[0].getDestinationPath(), dependencyPath, "Dependency Name mismatched");

        Assert.assertTrue((lifeCycleAdminService.getAllDependencies(servicePath).length == 2),
                          "Dependency Count mismatched");
    }

    private void addDependency(String resourcePath, String dependencyPath)
            throws RegistryException {
        registry.addAssociation(resourcePath, dependencyPath, ASS_TYPE_DEPENDS);
    }


}
