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
import org.wso2.carbon.registry.activities.stub.RegistryExceptionException;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.lifecycle.test.utils.LifeCycleUtils;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import java.io.IOException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class LifeCycleStateWhenUpdatingResourceLCTestCase {

    private int userId = 1;
    private UserInfo userInfo = UserListCsvReader.getUserInfo(userId);

    private WSRegistryServiceClient wsRegistry;
    private LifeCycleAdminServiceClient lifeCycleAdminServiceClient;
    Registry governance;

    private final String ASPECT_NAME = "ServiceLifeCycle";

    private String wsdlPathDev;
    private String servicePathDev;
    private String policyPathDev;
    private String schemaPathDev;


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
        governance = registryProviderUtil.getGovernanceRegistry(wsRegistry, userId);
    }

    @Test(groups = "wso2.greg", description = "Add lifecycle to a Schema and update Schema")
    public void SchemaAddLifecycleAndUpdateResource()
            throws RegistryException, CustomLifecyclesChecklistAdminServiceExceptionException,
                   IOException, InterruptedException, RegistryExceptionException {

        schemaPathDev = "/_system/governance" + LifeCycleUtils.addSchema("LifeCycleState.xsd", governance);
        lifeCycleAdminServiceClient.addAspect(schemaPathDev, ASPECT_NAME);
        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(schemaPathDev);
        Resource service = wsRegistry.get(schemaPathDev);
        assertNotNull(service, "Service Not found on registry path " + schemaPathDev);
        assertEquals(service.getPath(), schemaPathDev, "Service path changed after adding life cycle. " + schemaPathDev);

        assertEquals(LifeCycleUtils.getLifeCycleState(lifeCycle), "Development", "LifeCycle State Mismatched");

        schemaPathDev = "/_system/governance" + LifeCycleUtils.addSchema("LifeCycleState.xsd", governance);
        Thread.sleep(500);

        lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(schemaPathDev);
        service = wsRegistry.get(schemaPathDev);
        assertNotNull(service, "Service Not found on registry path " + schemaPathDev);
        assertEquals(service.getPath(), schemaPathDev, "Service path changed after adding life cycle. " + schemaPathDev);

        assertNotNull(lifeCycle, "Life Cycle Not Found after updating resource");
        assertNotNull(lifeCycle.getLifecycleProperties(), "Life Cycle properties Not Found after updating resource");
        assertEquals(LifeCycleUtils.getLifeCycleState(lifeCycle), "Development", "LifeCycle State Mismatched after updating resource");


    }

    @Test(groups = "wso2.greg", description = "Add lifecycle to a policy and update policy")
    public void policyAddLifecycleAndUpdateResource()
            throws RegistryException, CustomLifecyclesChecklistAdminServiceExceptionException,
                   IOException, InterruptedException, RegistryExceptionException {
        policyPathDev = "/_system/governance" + LifeCycleUtils.addPolicy("PolicyLifeCycleState.xml", governance);
        lifeCycleAdminServiceClient.addAspect(policyPathDev, ASPECT_NAME);
        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(policyPathDev);
        Resource service = wsRegistry.get(policyPathDev);
        assertNotNull(service, "Policy Not found on registry path " + policyPathDev);
        assertEquals(service.getPath(), policyPathDev, "Policy path changed after adding life cycle. " + policyPathDev);

        assertEquals(LifeCycleUtils.getLifeCycleState(lifeCycle), "Development", "LifeCycle State Mismatched");

        policyPathDev = "/_system/governance" + LifeCycleUtils.updatePolicy("PolicyLifeCycleState.xml", governance);
        Thread.sleep(500);

        lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(policyPathDev);
        service = wsRegistry.get(policyPathDev);
        assertNotNull(service, "Service Not found on registry path " + policyPathDev);
        assertEquals(service.getPath(), policyPathDev, "Service path changed after adding life cycle. " + policyPathDev);

        assertNotNull(lifeCycle, "Life Cycle Not Found after updating resource");
        assertNotNull(lifeCycle.getLifecycleProperties(), "Life Cycle properties Not Found after updating resource");
        assertEquals(LifeCycleUtils.getLifeCycleState(lifeCycle), "Development", "LifeCycle State Mismatched after updating resource");

    }

    @Test(groups = "wso2.greg", description = "Add lifecycle to a WSDl and update WSDL")
    public void WSDLAddLifecycleAndUpdateResource()
            throws RegistryException, CustomLifecyclesChecklistAdminServiceExceptionException,
                   IOException, InterruptedException, RegistryExceptionException {
        wsdlPathDev = "/_system/governance" + LifeCycleUtils.addWSDL("echoWsdlLifeCycleState.wsdl", governance);
        lifeCycleAdminServiceClient.addAspect(wsdlPathDev, ASPECT_NAME);
        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(wsdlPathDev);
        Resource service = wsRegistry.get(wsdlPathDev);
        assertNotNull(service, "Service Not found on registry path " + wsdlPathDev);
        assertEquals(service.getPath(), wsdlPathDev, "Service path changed after adding life cycle. " + wsdlPathDev);

        assertEquals(LifeCycleUtils.getLifeCycleState(lifeCycle), "Development", "LifeCycle State Mismatched");

        wsdlPathDev = "/_system/governance" + LifeCycleUtils.addWSDL("echoWsdlLifeCycleState.wsdl", governance);
        Thread.sleep(500);

        lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(wsdlPathDev);
        service = wsRegistry.get(wsdlPathDev);
        assertNotNull(service, "Service Not found on registry path " + wsdlPathDev);
        assertEquals(service.getPath(), wsdlPathDev, "Service path changed after adding life cycle. " + wsdlPathDev);

        assertNotNull(lifeCycle, "Life Cycle Not Found after updating resource");
        assertNotNull(lifeCycle.getLifecycleProperties(), "Life Cycle properties Not Found after updating resource");
        assertEquals(LifeCycleUtils.getLifeCycleState(lifeCycle), "Development", "LifeCycle State Mismatched after updating resource");

    }

    @Test(groups = "wso2.greg", description = "Add lifecycle to a Service and update Service")
    public void serviceAddLifecycleAndUpdateResource()
            throws Exception {
        servicePathDev = "/_system/governance" + LifeCycleUtils.addService("sns", "ServiceLifeCycleState", governance);
        lifeCycleAdminServiceClient.addAspect(servicePathDev, ASPECT_NAME);
        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(servicePathDev);
        Resource service = wsRegistry.get(servicePathDev);
        assertNotNull(service, "Service Not found on registry path " + servicePathDev);
        assertEquals(service.getPath(), servicePathDev, "Service path changed after adding life cycle. " + servicePathDev);

        assertEquals(LifeCycleUtils.getLifeCycleState(lifeCycle), "Development", "LifeCycle State Mismatched");

        servicePathDev = "/_system/governance" + LifeCycleUtils.addService("sns", "ServiceLifeCycleState", governance);
        Thread.sleep(500);

        lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(servicePathDev);
        service = wsRegistry.get(servicePathDev);
        assertNotNull(service, "Service Not found on registry path " + servicePathDev);
        assertEquals(service.getPath(), servicePathDev, "Service path changed after adding life cycle. " + servicePathDev);

        assertNotNull(lifeCycle, "Life Cycle Not Found after updating resource");
        assertNotNull(lifeCycle.getLifecycleProperties(), "Life Cycle properties Not Found after updating resource");
        assertEquals(LifeCycleUtils.getLifeCycleState(lifeCycle), "Development", "LifeCycle State Mismatched after updating resource");

    }


    @AfterClass
    public void cleanUp() throws RegistryException {

        String serviceFromWsdl = "/_system/governance/trunk/services/org/wso2/carbon/core/services/echo/echoyuSer1";

        if (schemaPathDev != null) {
            wsRegistry.delete(schemaPathDev);
        }
        if (policyPathDev != null) {
            wsRegistry.delete(policyPathDev);
        }
        if (wsdlPathDev != null) {
            wsRegistry.delete(wsdlPathDev);
        }
        if (servicePathDev != null) {
            wsRegistry.delete(servicePathDev);
        }
        if (wsRegistry.resourceExists(serviceFromWsdl)) {
            wsRegistry.delete(serviceFromWsdl);
        }
        wsRegistry = null;
        lifeCycleAdminServiceClient = null;
    }


}
