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

public class GRegPromoteLifeCycleWithResourceTestCase {

    private String sessionCookie;

    private WSRegistryServiceClient registry;
    private LifeCycleAdminServiceClient lifeCycleAdminService;

    private final String serviceName = "serviceForLifeCycleWithResource";
    private final String serviceDependencyName = "UTPolicyDependency.xml";
    private final String aspectName = "ServiceLifeCycle";
    private final String ACTION_PROMOTE = "Promote";
    private final String ASS_TYPE_DEPENDS = "depends";
    //    private final String ACTION_DEMOTE = "Demote";
    private String servicePathDev;
    private String servicePathTest;
    private String servicePathProd;

    private String policyPathDev;
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
    }

    @Test(priority = 1, description = "Add lifecycle to a service")
    public void addLifecycle()
            throws RegistryException, CustomLifecyclesChecklistAdminServiceExceptionException,
                   RemoteException, InterruptedException {
        registry.associateAspect(servicePathDev, aspectName);
        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathDev);
        Resource service = registry.get(servicePathDev);
        Assert.assertNotNull(service, "Service Not found on registry path " + servicePathDev);
        Assert.assertTrue(service.getPath().contains("trunk"), "Service not in trunk. " + servicePathDev);

        Assert.assertEquals(Utils.getLifeCycleState(lifeCycle), "Development",
                            "LifeCycle State Mismatched");

        Assert.assertTrue((lifeCycleAdminService.getAllDependencies(servicePathDev).length == 2),
                          "Dependency Count mismatched");


    }

    @Test(priority = 2, dependsOnMethods = {"addLifecycle"}, description = "Promote Service")
    public void promoteServiceToTesting()
            throws CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException,
                   InterruptedException, RegistryException {
        Thread.sleep(1000);
        ArrayOfString[] parameters = new ArrayOfString[2];
        parameters[0] = new ArrayOfString();
        parameters[0].setArray(new String[]{servicePathDev, "1.0.0"});

        parameters[1] = new ArrayOfString();
        parameters[1].setArray(new String[]{policyPathDev, "1.0.0"});

        lifeCycleAdminService.invokeAspectWithParams(servicePathDev, aspectName,
                                                     ACTION_PROMOTE, null, parameters);
        servicePathTest = "/_system/governance/branches/testing/services/sns/1.0.0/" + serviceName;
        policyPathTest = "/_system/governance/branches/testing/policies/1.0.0/" + serviceDependencyName;
        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathTest);
        Resource service = registry.get(servicePathTest);
        Assert.assertNotNull(service, "Service Not found on registry path " + servicePathTest);
        Assert.assertTrue(service.getPath().contains("branches/testing"), "Service not in branches/testing. " + servicePathTest);

        Assert.assertEquals(Utils.getLifeCycleState(lifeCycle), "Testing", "LifeCycle State Mismatched");

        Association[] dependency = registry.getAssociations(servicePathTest, ASS_TYPE_DEPENDS);
        Assert.assertNotNull(dependency, "Dependency Not Found.");
        Assert.assertTrue(dependency.length > 0, "Dependency list empty");
        Assert.assertEquals(dependency[0].getDestinationPath(), policyPathTest, "Dependency Name mismatched");

        Assert.assertTrue((lifeCycleAdminService.getAllDependencies(servicePathTest).length == 2),
                          "Dependency Count mismatched");

        Assert.assertEquals(registry.get(servicePathDev).getPath(), servicePathDev, "Preserve original failed for service");
        Assert.assertEquals(registry.get(policyPathDev).getPath(), policyPathDev, "Preserve original failed for dependency");

    }

    @Test(priority = 3, dependsOnMethods = {"promoteServiceToTesting"}, description = "Promote Service")
    public void promoteServiceToProduction()
            throws CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException,
                   InterruptedException, RegistryException {
        Thread.sleep(1000);
        ArrayOfString[] parameters = new ArrayOfString[2];
        parameters[0] = new ArrayOfString();
        parameters[0].setArray(new String[]{servicePathTest, "1.0.0"});

        parameters[1] = new ArrayOfString();
        parameters[1].setArray(new String[]{policyPathTest, "1.0.0"});
        lifeCycleAdminService.invokeAspectWithParams(servicePathTest, aspectName,
                                                     ACTION_PROMOTE, null, parameters);

        servicePathProd = "/_system/governance/branches/production/services/sns/1.0.0/" + serviceName;
        String policyPathProd = "/_system/governance/branches/production/policies/1.0.0/" + serviceDependencyName;
        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathProd);

        Resource service = registry.get(servicePathProd);
        Assert.assertNotNull(service, "Service Not found on registry path " + servicePathProd);
        Assert.assertTrue(service.getPath().contains("branches/production"), "Service not in branches/production. " + servicePathProd);

        Assert.assertEquals(Utils.getLifeCycleState(lifeCycle), "Production", "LifeCycle State Mismatched");

        Association[] dependency = registry.getAssociations(servicePathProd, ASS_TYPE_DEPENDS);
        Assert.assertNotNull(dependency, "Dependency Not Found.");
        Assert.assertTrue(dependency.length > 0, "Dependency list empty");
        Assert.assertEquals(dependency[0].getDestinationPath(), policyPathProd, "Dependency Name mismatched");

        Assert.assertTrue((lifeCycleAdminService.getAllDependencies(servicePathProd).length == 2),
                          "Dependency Count mismatched");

        Assert.assertEquals(registry.get(servicePathTest).getPath(), servicePathTest, "Preserve original failed for service");
        Assert.assertEquals(registry.get(policyPathTest).getPath(), policyPathTest, "Preserve original failed for dependency");

    }

    @AfterClass
    public void destroy() {
        servicePathDev = null;
        servicePathTest = null;
        servicePathProd = null;
    }


    private void addDependency(String resourcePath, String dependencyPath)
            throws RegistryException {
        registry.addAssociation(resourcePath, dependencyPath, ASS_TYPE_DEPENDS);
    }

}
