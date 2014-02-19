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
import org.testng.annotations.BeforeTest;
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

/**
 * Covers the public jira https://wso2.org/jira/browse/CARBON-12975 Missing the service paths
 * while promoting Life Cycle
 */

public class GregLifeCycleTestWithServicePathTestCase {
    private String sessionCookie;

    private WSRegistryServiceClient registry;
    private LifeCycleAdminServiceClient lifeCycleAdminService;

    private final String ASPECT_NAME = "ServiceLifeCycle";
    private final String ACTION_PROMOTE = "Promote";
    private String servicePathTrunk = null;
    private String servicePathTest;
    Registry governance;
    String wsdlPath = null;

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {

        ClientConnectionUtil.waitForPort(Integer.parseInt(FrameworkSettings.HTTP_PORT));
        sessionCookie = new LoginLogoutUtil().login();
        final String SERVER_URL = GregTestUtils.getServerUrl();
        lifeCycleAdminService = new LifeCycleAdminServiceClient(SERVER_URL, sessionCookie);
        registry = GregTestUtils.getRegistry();
        governance = GregTestUtils.getGovernanceRegistry(registry);

    }

    @Test( description = "deployArtifact")
    public void deployArtifact() throws Exception {

        wsdlPath = "/_system/governance" + Utils.addWSDL("echoDependency.wsdl", governance);
        Association[] usedBy = registry.getAssociations(wsdlPath, "usedBy");
        for (Association association : usedBy) {
            if (association.getSourcePath().equalsIgnoreCase(wsdlPath)) {
                servicePathTrunk = association.getDestinationPath();
            }
        }

        lifeCycleAdminService.addAspect(servicePathTrunk, ASPECT_NAME);
        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathTrunk);
        Resource service = registry.get(servicePathTrunk);
        Assert.assertNotNull(service, "Service Not found on registry path " + servicePathTrunk);
        Assert.assertEquals(service.getPath(), servicePathTrunk, "Service path changed after adding life cycle. " + servicePathTrunk);

        Assert.assertEquals(Utils.getLifeCycleState(lifeCycle), "Development", "LifeCycle State Mismatched");


    }

    @Test( description = "Promote service to Test",dependsOnMethods ="deployArtifact" )
    public void promoteToTesting()
            throws CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException,
                   RegistryException, InterruptedException {
        servicePathTest = "/_system/governance/branches/testing/services/org/wso2/carbon/core/services/echo/2.0.0/echoyuSer1";

        lifeCycleAdminService.invokeAspect(servicePathTrunk, ASPECT_NAME,
                                           ACTION_PROMOTE, null);
        Thread.sleep(2000);

        Thread.sleep(500);
        Resource service = registry.get(servicePathTrunk);
        Assert.assertNotNull(service, "Service Not found on registry path " + servicePathTest);
    }

    @Test( description = "Promote service to Production",dependsOnMethods ="promoteToTesting")
    public void promoteToProduction()
            throws CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException,
                   RegistryException, InterruptedException {
        ArrayOfString[] parameters = new ArrayOfString[1];
        parameters[0] = new ArrayOfString();
        parameters[0].setArray(new String[]{"/_system/governance/branches/testing/services/org/wso2/carbon/core/services/echo/1.0.0-SNAPSHOT/echoyuSer1", "1.0.0"});

        String servicePathProd = "/_system/governance/branches/production/services/org/wso2/carbon/core/services/echo/1.0.0/echoyuSer1";
        lifeCycleAdminService.invokeAspectWithParams("/_system/governance/branches/testing/services/org/wso2/carbon/core/services/echo/1.0.0-SNAPSHOT/echoyuSer1", ASPECT_NAME,
                                                     ACTION_PROMOTE, null, parameters);
        Thread.sleep(2000);

        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathProd);
        Resource service = registry.get(servicePathProd);
        Assert.assertEquals(Utils.getLifeCycleState(lifeCycle), "Production", "LifeCycle State Mismatched");
        Assert.assertNotNull(service, "Service Not found on registry path " + servicePathProd);


    }
}
