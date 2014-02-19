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

/**
 * Covers the public jira https://wso2.org/jira/browse/CARBON-12975 Missing the service paths
 * while promoting Life Cycle
 */

public class GregLifeCycleTestWithServicePathLCTestCase {

    private int userId = 1;
    private UserInfo userInfo = UserListCsvReader.getUserInfo(userId);

    private WSRegistryServiceClient wsRegistry;
    private LifeCycleAdminServiceClient lifeCycleAdminServiceClient;

    private final String ASPECT_NAME = "ServiceLifeCycle";
    private final String ACTION_PROMOTE = "Promote";
    private String servicePathTrunk;
    private String servicePathTest;
    private Registry governance;
    private String wsdlPath;
    private String servicePathProd;

    /**
     * @throws Exception
     */
    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {

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
        governance = registryProviderUtil.getGovernanceRegistry(wsRegistry, userId);


    }

    /**
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "deployArtifact")
    public void deployArtifact() throws Exception {

        wsdlPath = "/_system/governance" + LifeCycleUtils.addWSDL("echoDependency.wsdl", governance);
        Association[] usedBy = wsRegistry.getAssociations(wsdlPath, "usedBy");
        for (Association association : usedBy) {
            if (association.getSourcePath().equalsIgnoreCase(wsdlPath)) {
                servicePathTrunk = association.getDestinationPath();
            }
        }

        lifeCycleAdminServiceClient.addAspect(servicePathTrunk, ASPECT_NAME);
        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(servicePathTrunk);
        Resource service = wsRegistry.get(servicePathTrunk);
        assertNotNull(service, "Service Not found on registry path " + servicePathTrunk);
        assertEquals(service.getPath(), servicePathTrunk, "Service path changed after adding life cycle. " + servicePathTrunk);

        assertEquals(LifeCycleUtils.getLifeCycleState(lifeCycle), "Development", "LifeCycle State Mismatched");


    }

    /**
     * @throws org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException
     *
     * @throws java.rmi.RemoteException
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
     *
     * @throws InterruptedException
     */
    @Test(groups = "wso2.greg", description = "Promote service to Test", dependsOnMethods = "deployArtifact")
    public void promoteToTesting()
            throws CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException,
                   RegistryException, InterruptedException {
        servicePathTest = "/_system/governance/branches/testing/services/org/wso2/carbon/core/services/echo/1.0.0-SNAPSHOT/echoyuSer1";

        lifeCycleAdminServiceClient.invokeAspect(servicePathTrunk, ASPECT_NAME,
                                                 ACTION_PROMOTE, null);
        Thread.sleep(2000);

        Thread.sleep(500);
        Resource service = wsRegistry.get(servicePathTest);
        assertNotNull(service, "Service Not found on registry path " + servicePathTest);
    }

    /**
     * @throws org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException
     *
     * @throws java.rmi.RemoteException
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
     *
     * @throws InterruptedException
     */
    @Test(groups = "wso2.greg", description = "Promote service to Production", dependsOnMethods = "promoteToTesting")
    public void promoteToProduction()
            throws CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException,
                   RegistryException, InterruptedException {
        ArrayOfString[] parameters = new ArrayOfString[1];
        parameters[0] = new ArrayOfString();
        parameters[0].setArray(new String[]{"/_system/governance/branches/testing/services/org/wso2/carbon/core/services/echo/1.0.0-SNAPSHOT/echoyuSer1", "1.0.0"});

        servicePathProd = "/_system/governance/branches/production/services/org/wso2/carbon/core/services/echo/1.0.0/echoyuSer1";
        lifeCycleAdminServiceClient.invokeAspectWithParams("/_system/governance/branches/testing/services/org/wso2/carbon/core/services/echo/1.0.0-SNAPSHOT/echoyuSer1", ASPECT_NAME,
                                                           ACTION_PROMOTE, null, parameters);
        Thread.sleep(2000);

        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(servicePathProd);
        Resource service = wsRegistry.get(servicePathProd);
        assertEquals(LifeCycleUtils.getLifeCycleState(lifeCycle), "Production", "LifeCycle State Mismatched");
        assertNotNull(service, "Service Not found on registry path " + servicePathProd);


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

        if (servicePathTrunk != null) {
            wsRegistry.delete(servicePathTrunk);
        }
        if (servicePathTest != null) {
            wsRegistry.delete(servicePathTest);
        }
        if (servicePathProd != null) {
            wsRegistry.delete(servicePathProd);
        }
        if (wsdlPath != null) {
            wsRegistry.delete(wsdlPath);
        }
    }

}
