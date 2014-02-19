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

public class PreserveOriginalDefaultServiceLCTestCase {

    private int userId = 1;
    private UserInfo userInfo = UserListCsvReader.getUserInfo(userId);

    private WSRegistryServiceClient wsRegistry;
    private LifeCycleAdminServiceClient lifeCycleAdminServiceClient;

    private final String serviceNamePreserveFalse = "servicePreserveOriginalFalse";
    private final String serviceNamePreserveTrue = "servicePreserveOriginal";
    private final String aspectName = "ServiceLifeCycle";
    private final String ACTION_PROMOTE = "Promote";
    private String trunk;
    private String trunkPreserve;
    private String testBranch;
    private String testBranchPreserve;
    private String proBranch;
    private String proBranchPreserve;


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


        trunk = "/_system/governance" + LifeCycleUtils.addService("sns", serviceNamePreserveFalse, governance);
        trunkPreserve = "/_system/governance" + LifeCycleUtils.addService("sns", serviceNamePreserveTrue, governance);
        Thread.sleep(500);
        wsRegistry.associateAspect(trunk, aspectName);
        wsRegistry.associateAspect(trunkPreserve, aspectName);

    }

    @Test(groups = "wso2.greg", description = "Promote Service and delete original")
    public void preserveOriginalFalseAndPromoteToTesting() throws Exception {
        Thread.sleep(1000);

        ArrayOfString[] parameters = new ArrayOfString[2];
        parameters[0] = new ArrayOfString();
        parameters[0].setArray(new String[]{trunk, "1.0.0"});

        parameters[1] = new ArrayOfString();
        parameters[1].setArray(new String[]{"preserveOriginal", "false"});

        lifeCycleAdminServiceClient.invokeAspectWithParams(trunk, aspectName,
                                                           ACTION_PROMOTE, null, parameters);
        testBranch = "/_system/governance/branches/testing/services/sns/1.0.0/" + serviceNamePreserveFalse;
        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(testBranch);
        Resource service = wsRegistry.get(testBranch);
        assertNotNull(service, "Service Not found on registry path " + testBranch);
        assertEquals(service.getPath(), testBranch, "Service not in branches/testing. " + testBranch);

        assertEquals(LifeCycleUtils.getLifeCycleState(lifeCycle), "Testing", "LifeCycle State Mismatched");


        try {
            wsRegistry.get(trunk);
            fail(trunk + " Resource exist");
        } catch (RegistryException e) {
            assertTrue(e.getCause().getMessage().contains("Resource does not exist at path /_system/governance/trunk/services/"));
        }

    }

    @Test(groups = "wso2.greg", dependsOnMethods = {"preserveOriginalFalseAndPromoteToTesting"}, description = "Promote Service and delete original")
    public void preserveOriginalFalseAndPromoteToProduction()
            throws CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException,
                   InterruptedException, RegistryException {
        Thread.sleep(1000);
        ArrayOfString[] parameters = new ArrayOfString[2];
        parameters[0] = new ArrayOfString();
        parameters[0].setArray(new String[]{testBranch, "1.0.0"});

        parameters[1] = new ArrayOfString();
        parameters[1].setArray(new String[]{"preserveOriginal", "false"});

        lifeCycleAdminServiceClient.invokeAspectWithParams(testBranch, aspectName,
                                                           ACTION_PROMOTE, null, parameters);
        proBranch = "/_system/governance/branches/production/services/sns/1.0.0/" + serviceNamePreserveFalse;
        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(proBranch);

        Resource service = wsRegistry.get(proBranch);
        assertNotNull(service, "Service Not found on registry path " + proBranch);
        assertEquals(service.getPath(), proBranch, "Service not in branches/production. " + proBranch);

        assertEquals(LifeCycleUtils.getLifeCycleState(lifeCycle), "Production", "LifeCycle State Mismatched");

        try {
            wsRegistry.get(testBranch);
            fail(trunk + " Resource exist");
        } catch (RegistryException e) {
            assertTrue(e.getCause().getMessage().contains("Resource does not exist at path /_system/governance/branches/testing/services/"));
        }

    }

    @Test(groups = "wso2.greg", description = "Promote Service preserve original", dependsOnMethods = "preserveOriginalFalseAndPromoteToProduction")
    public void preserveOriginalAndPromoteToTesting() throws Exception {
        Thread.sleep(1000);

        ArrayOfString[] parameters = new ArrayOfString[2];
        parameters[0] = new ArrayOfString();
        parameters[0].setArray(new String[]{trunkPreserve, "1.0.0"});

        parameters[1] = new ArrayOfString();
        parameters[1].setArray(new String[]{"preserveOriginal", "true"});

        lifeCycleAdminServiceClient.invokeAspectWithParams(trunkPreserve, aspectName,
                                                           ACTION_PROMOTE, null, parameters);
        testBranchPreserve = "/_system/governance/branches/testing/services/sns/1.0.0/" + serviceNamePreserveTrue;
        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(testBranchPreserve);
        Resource service = wsRegistry.get(testBranchPreserve);
        assertNotNull(service, "Service Not found on registry path " + testBranchPreserve);
        assertEquals(service.getPath(), testBranchPreserve, "Service not in branches/testing. " + testBranchPreserve);

        assertEquals(LifeCycleUtils.getLifeCycleState(lifeCycle), "Testing", "LifeCycle State Mismatched");


        assertEquals(wsRegistry.get(trunkPreserve).getPath(), trunkPreserve, "Resource not exist on trunk");

    }

    @Test(groups = "wso2.greg", dependsOnMethods = {"preserveOriginalAndPromoteToTesting"},
          description = "Promote Service and preserve original")
    public void preserveOriginalAndPromoteToProduction()
            throws CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException,
                   InterruptedException, RegistryException {
        Thread.sleep(1000);
        ArrayOfString[] parameters = new ArrayOfString[2];
        parameters[0] = new ArrayOfString();
        parameters[0].setArray(new String[]{testBranchPreserve, "1.0.0"});

        parameters[1] = new ArrayOfString();
        parameters[1].setArray(new String[]{"preserveOriginal", "true"});

        lifeCycleAdminServiceClient.invokeAspectWithParams(testBranchPreserve, aspectName,
                                                           ACTION_PROMOTE, null, parameters);
        proBranchPreserve = "/_system/governance/branches/production/services/sns/1.0.0/" + serviceNamePreserveTrue;
        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(proBranchPreserve);

        Resource service = wsRegistry.get(proBranchPreserve);
        assertNotNull(service, "Service Not found on registry path " + proBranchPreserve);
        assertEquals(service.getPath(), proBranchPreserve, "Service not in branches/production. " + proBranchPreserve);

        assertEquals(LifeCycleUtils.getLifeCycleState(lifeCycle), "Production", "LifeCycle State Mismatched");

        assertEquals(wsRegistry.get(proBranchPreserve).getPath(), proBranchPreserve, "Resource not exist on branch");
    }

    /**
     * @throws RegistryException
     * @throws LifeCycleManagementServiceExceptionException
     *
     * @throws RemoteException
     */
    @AfterClass()
    public void deleteResources()
            throws RegistryException, LifeCycleManagementServiceExceptionException,
                   RemoteException {

        if (trunkPreserve != null) {
            wsRegistry.delete(trunkPreserve);
        }
        if (proBranch != null) {
            wsRegistry.delete(proBranch);
        }
        if (testBranchPreserve != null) {
            wsRegistry.delete(testBranchPreserve);
        }
        if (proBranchPreserve != null) {
            wsRegistry.delete(proBranchPreserve);
        }
    }
}
