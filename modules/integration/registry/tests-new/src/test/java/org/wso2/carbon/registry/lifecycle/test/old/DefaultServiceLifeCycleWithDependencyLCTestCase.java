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

public class DefaultServiceLifeCycleWithDependencyLCTestCase {

    private int userId = 1;
    private UserInfo userInfo = UserListCsvReader.getUserInfo(userId);

    private WSRegistryServiceClient wsRegistry;
    private LifeCycleAdminServiceClient lifeCycleAdminServiceClient;

    private final String serviceName = "serviceForLifeCycleWithDependency";
    private final String serviceDependencyName = "UTPolicyDependency.xml";
    private final String aspectName = "ServiceLifeCycle";
    private final String ACTION_PROMOTE = "Promote";
    private final String ACTION_DEMOTE = "Demote";
    private final String ASS_TYPE_DEPENDS = "depends";
    private String servicePathDev;
    private String servicePathTest;
    private String servicePathProd;

    private String policyPathDev;
    private String policyPathTest;
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
        wsRegistry.associateAspect(servicePathDev, aspectName);
        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(servicePathDev);
        Resource service = wsRegistry.get(servicePathDev);
        assertNotNull(service, "Service Not found on wsRegistry path " + servicePathDev);
        assertTrue(service.getPath().contains("trunk"), "Service not in trunk. " + servicePathDev);

        assertEquals(LifeCycleUtils.getLifeCycleState(lifeCycle), "Development",
                     "LifeCycle State Mismatched");

        assertTrue((lifeCycleAdminServiceClient.getAllDependencies(servicePathDev).length == 2),
                   "Dependency Count mismatched");


    }

    /**
     * @throws org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException
     *
     * @throws java.rmi.RemoteException
     * @throws InterruptedException
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
     *
     */
    @Test(groups = "wso2.greg", dependsOnMethods = {"addLifecycle"}, description = "Promote Service")
    public void promoteServiceToTesting()
            throws CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException,
                   InterruptedException, RegistryException {
        Thread.sleep(1000);
        ArrayOfString[] parameters = new ArrayOfString[2];
        parameters[0] = new ArrayOfString();
        parameters[0].setArray(new String[]{servicePathDev, "1.0.0"});

        parameters[1] = new ArrayOfString();
        parameters[1].setArray(new String[]{policyPathDev, "1.0.0"});

        lifeCycleAdminServiceClient.invokeAspectWithParams(servicePathDev, aspectName,
                                                           ACTION_PROMOTE, null, parameters);
        servicePathTest = "/_system/governance/branches/testing/services/sns/1.0.0/" + serviceName;
        policyPathTest = "/_system/governance/branches/testing/policies/1.0.0/" + serviceDependencyName;
        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(servicePathTest);
        Resource service = wsRegistry.get(servicePathTest);
        assertNotNull(service, "Service Not found on wsRegistry path " + servicePathTest);
        assertTrue(service.getPath().contains("branches/testing"), "Service not in branches/testing. " + servicePathTest);

        assertEquals(LifeCycleUtils.getLifeCycleState(lifeCycle), "Testing", "LifeCycle State Mismatched");

        Association[] dependency = wsRegistry.getAssociations(servicePathTest, ASS_TYPE_DEPENDS);
        assertNotNull(dependency, "Dependency Not Found.");
        assertTrue(dependency.length > 0, "Dependency list empty");
        assertEquals(dependency[0].getDestinationPath(), policyPathTest, "Dependency Name mismatched");

        assertTrue((lifeCycleAdminServiceClient.getAllDependencies(servicePathTest).length == 2),
                   "Dependency Count mismatched");

        assertEquals(wsRegistry.get(servicePathDev).getPath(), servicePathDev, "Preserve original failed for service");
        assertEquals(wsRegistry.get(policyPathDev).getPath(), policyPathDev, "Preserve original failed for dependency");

    }

    /**
     * @throws org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException
     *
     * @throws java.rmi.RemoteException
     * @throws InterruptedException
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
     *
     */
    @Test(groups = "wso2.greg", dependsOnMethods = {"promoteServiceToTesting"}, description = "Promote Service")
    public void promoteServiceToProduction()
            throws CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException,
                   InterruptedException, RegistryException {
        Thread.sleep(1000);
        ArrayOfString[] parameters = new ArrayOfString[2];
        parameters[0] = new ArrayOfString();
        parameters[0].setArray(new String[]{servicePathTest, "2.0.0"});

        parameters[1] = new ArrayOfString();
        parameters[1].setArray(new String[]{policyPathTest, "1.5.0"});
        lifeCycleAdminServiceClient.invokeAspectWithParams(servicePathTest, aspectName,
                                                           ACTION_PROMOTE, null, parameters);

        servicePathProd = "/_system/governance/branches/production/services/sns/2.0.0/" + serviceName;
        policyPathProd = "/_system/governance/branches/production/policies/1.5.0/" + serviceDependencyName;
        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(servicePathProd);

        Resource service = wsRegistry.get(servicePathProd);
        assertNotNull(service, "Service Not found on wsRegistry path " + servicePathProd);
        assertTrue(service.getPath().contains("branches/production"), "Service not in branches/production. " + servicePathProd);

        assertEquals(LifeCycleUtils.getLifeCycleState(lifeCycle), "Production", "LifeCycle State Mismatched");

        Association[] dependency = wsRegistry.getAssociations(servicePathProd, ASS_TYPE_DEPENDS);
        assertNotNull(dependency, "Dependency Not Found.");
        assertTrue(dependency.length > 0, "Dependency list empty");
        assertEquals(dependency[0].getDestinationPath(), policyPathProd, "Dependency Name mismatched");

        assertTrue((lifeCycleAdminServiceClient.getAllDependencies(servicePathProd).length == 2),
                   "Dependency Count mismatched");

        assertEquals(wsRegistry.get(servicePathTest).getPath(), servicePathTest, "Preserve original failed for service");
        assertEquals(wsRegistry.get(policyPathTest).getPath(), policyPathTest, "Preserve original failed for dependency");

    }

    /**
     * @throws org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException
     *
     * @throws java.rmi.RemoteException
     * @throws InterruptedException
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
     *
     */
    @Test(groups = "wso2.greg", dependsOnMethods = {"promoteServiceToProduction"}, description = "Promote Service")
    public void demoteServiceToTesting()
            throws CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException,
                   InterruptedException, RegistryException {
        Thread.sleep(1000);
        ArrayOfString[] parameters = new ArrayOfString[2];
        parameters[0] = new ArrayOfString();
        parameters[0].setArray(new String[]{servicePathProd, "1.0.0"});

        parameters[1] = new ArrayOfString();
        parameters[1].setArray(new String[]{policyPathProd, "1.0.0"});

        lifeCycleAdminServiceClient.invokeAspectWithParams(servicePathProd, aspectName,
                                                           ACTION_DEMOTE, null, parameters);
        servicePathTest = "/_system/governance/branches/testing/services/sns/1.0.0/" + serviceName;
        policyPathTest = "/_system/governance/branches/testing/policies/1.0.0/" + serviceDependencyName;
        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(servicePathTest);
        Resource service = wsRegistry.get(servicePathTest);
        assertNotNull(service, "Service Not found on wsRegistry path " + servicePathTest);
        assertTrue(service.getPath().contains("branches/testing"), "Service not in branches/testing. " + servicePathTest);

        assertEquals(LifeCycleUtils.getLifeCycleState(lifeCycle), "Testing", "LifeCycle State Mismatched");

        Association[] dependency = wsRegistry.getAssociations(servicePathTest, ASS_TYPE_DEPENDS);
        assertNotNull(dependency, "Dependency Not Found.");
        assertTrue(dependency.length > 0, "Dependency list empty");
        assertEquals(dependency[0].getDestinationPath(), policyPathTest, "Dependency Name mismatched");

        assertTrue((lifeCycleAdminServiceClient.getAllDependencies(servicePathTest).length == 2),
                   "Dependency Count mismatched");

        assertEquals(wsRegistry.get(servicePathDev).getPath(), servicePathDev, "Preserve original failed for service");
        assertEquals(wsRegistry.get(policyPathDev).getPath(), policyPathDev, "Preserve original failed for dependency");

    }

    /**
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
     *
     * @throws org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException
     *
     * @throws java.rmi.RemoteException
     */
    @AfterClass()
    public void cleanup()
            throws RegistryException, LifeCycleManagementServiceExceptionException,
                   RemoteException {
        String wsdlPathProd = "/_system/governance/branches/production/wsdls/org/wso2/carbon/core/services/echo/2.0.0/echoDependency.wsdl";
        String wsdlPathTest = "/_system/governance/branches/testing/wsdls/org/wso2/carbon/core/services/echo/2.0.0/echoDependency.wsdl";

        String schemaPathProd = "/_system/governance/branches/production/schemas/org/company/www/2.0.0/PersonDependency.xsd";
        String schemaPathTest = "/_system/governance/branches/testing/schemas/org/company/www/2.0.0/PersonDependency.xsd";

        if (servicePathDev != null) {
            wsRegistry.delete(servicePathDev);
        }
        if (servicePathTest != null) {
            wsRegistry.delete(servicePathTest);
        }
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
        if (wsRegistry.resourceExists(wsdlPathProd)) {
            wsRegistry.delete(wsdlPathProd);
        }
        if (wsRegistry.resourceExists(wsdlPathTest)) {
            wsRegistry.delete(wsdlPathTest);
        }
        if (wsRegistry.resourceExists(schemaPathProd)) {
            wsRegistry.delete(schemaPathProd);
        }
        if (wsRegistry.resourceExists(schemaPathTest)) {
            wsRegistry.delete(schemaPathTest);
        }
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
