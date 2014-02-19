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

import static org.testng.Assert.assertTrue;

public class PublishLifeCycleAsAPI {

    private WSRegistryServiceClient registry;
    private LifeCycleAdminServiceClient lifeCycleAdminServiceClient;
    private Registry governance;

    private final String serviceName = "serviceName";
    private final String aspectName = "ServiceLifeCycle";
    private final String ACTION_PROMOTE = "Promote";
    private String trunk;
    private String testBranch;
    private String proBranch;
    private UserInfo userInfo;


    @BeforeClass(enabled = false, alwaysRun = true)
    public void init() throws Exception {

        int userId = 2;
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        ManageEnvironment environment = builder.build();
        userInfo = UserListCsvReader.getUserInfo(userId);
        lifeCycleAdminServiceClient =
                new LifeCycleAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                                environment.getGreg().getSessionCookie());

        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        registry =
                registryProviderUtil.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        governance =
                registryProviderUtil.getGovernanceRegistry(registry, userId);


        trunk = "/_system/governance" + LifeCycleUtils.addService("sns", serviceName, governance);


        registry.associateAspect(trunk, aspectName);

    }

    @Test(enabled = false, groups = "wso2.greg", description = " Promote LC from Development to Testing")
    public void testPromoteToTesting()
            throws CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException,
                   RegistryException {


        ArrayOfString[] parameters = new ArrayOfString[1];
        parameters[0] = new ArrayOfString();
        parameters[0].setArray(new String[]{trunk, "1.0.0"});

        lifeCycleAdminServiceClient.invokeAspectWithParams(trunk,
                                                           aspectName, ACTION_PROMOTE, null,
                                                           parameters);

        testBranch = "/_system/governance/branches/testing/services/sns/1.0.0/" + serviceName;

        LifecycleBean lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(testBranch);
        Resource service = registry.get(testBranch);
        Assert.assertNotNull(service, "Service Not found on registry path " + testBranch);
        Assert.assertEquals(service.getPath(), testBranch, "Service not in branches/testing. " + testBranch);

        Assert.assertEquals(LifeCycleUtils.getLifeCycleState(lifeCycle), "Testing", "LifeCycle State Mismatched");


    }

    @Test(enabled = false, groups = "wso2.greg", description = " Promote LC from Testing to Production",
          dependsOnMethods = "testPromoteToTesting")
    public void testPromoteToProduction()
            throws CustomLifecyclesChecklistAdminServiceExceptionException,
                   RemoteException, RegistryException {


        ArrayOfString[] parameters = new ArrayOfString[1];
        parameters[0] = new ArrayOfString();
        parameters[0].setArray(new String[]{testBranch, "1.0.0"});

        lifeCycleAdminServiceClient.invokeAspectWithParams(testBranch,
                                                           aspectName, ACTION_PROMOTE, null,
                                                           parameters);

        proBranch = "/_system/governance/branches/production/services/sns/1.0.0/" + serviceName;

        LifecycleBean lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(proBranch);
        Resource service = registry.get(proBranch);
        Assert.assertNotNull(service, "Service Not found on registry path " + proBranch);
        Assert.assertEquals(service.getPath(), proBranch, "Service not in branches/production. " + proBranch);

        Assert.assertEquals(LifeCycleUtils.getLifeCycleState(lifeCycle), "Production", "LifeCycle State Mismatched");


    }

    @Test(enabled = false, groups = "wso2.greg", description = " Promote LC from Production to Publish",
          dependsOnMethods = "testPromoteToProduction")
    public void testPromoteToPublish()
            throws CustomLifecyclesChecklistAdminServiceExceptionException,
                   RemoteException, RegistryException {


        ArrayOfString[] parameters = new ArrayOfString[1];
        parameters[0] = new ArrayOfString();
        parameters[0].setArray(new String[]{proBranch, "1.0.0"});

        String ACTION_PUBLISH = "Publish";
        lifeCycleAdminServiceClient.invokeAspectWithParams(proBranch,
                                                           aspectName, ACTION_PUBLISH, null,
                                                           parameters);
    }


    @Test(enabled = false, groups = "wso2.greg", description = " Testing if API Artifact created ",
          dependsOnMethods = "testPromoteToPublish")
    public void testAPICreation() throws RegistryException {

        assertTrue(governance.resourceExists
                ("apimgt/applicationdata/provider/" + userInfo.getUserNameWithoutDomain() + "/serviceName/1.0.0/api"),
                   "API Artifact doesn't exists");
    }

    @AfterClass (enabled = false)
    public void deleteLifeCycle()
            throws RegistryException, LifeCycleManagementServiceExceptionException,
                   RemoteException {
        if (trunk != null) {
            registry.delete(trunk);
        }

        if (testBranch != null) {
            registry.delete(testBranch);
        }

        if (proBranch != null) {
            registry.delete(proBranch);
        }
        if (governance.resourceExists("/apimgt/applicationdata/provider/" +
                                      userInfo.getUserNameWithoutDomain())) {
            registry.delete("/_system/governance/apimgt/applicationdata/provider/" +
                            userInfo.getUserNameWithoutDomain());
        }
        registry = null;
        governance = null;
        lifeCycleAdminServiceClient = null;
        lifeCycleAdminServiceClient = null;
    }

}
