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
import org.wso2.carbon.governance.api.policies.PolicyManager;
import org.wso2.carbon.governance.api.policies.dataobjects.Policy;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.beans.xsd.LifecycleBean;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.registry.activities.stub.RegistryExceptionException;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.lifecycle.test.utils.Utils;
import org.wso2.carbon.registry.search.metadata.test.utils.GregTestUtils;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;

public class LifeCycleStateWhenUpdatingResourceTestCase {

    private WSRegistryServiceClient registry;
    private LifeCycleAdminServiceClient lifeCycleAdminService;
    Registry governance;

    private final String ASPECT_NAME = "ServiceLifeCycle";

    private String wsdlPathDev;
    private String servicePathDev;
    private String policyPathDev;
    private String schemaPathDev;


    @BeforeClass
    public void init() throws Exception {
        ClientConnectionUtil.waitForPort(Integer.parseInt(FrameworkSettings.HTTP_PORT));
        String sessionCookie = new LoginLogoutUtil().login();
        final String SERVER_URL = GregTestUtils.getServerUrl();
        lifeCycleAdminService = new LifeCycleAdminServiceClient(SERVER_URL, sessionCookie);
        registry = GregTestUtils.getRegistry();
        governance = GregTestUtils.getGovernanceRegistry(registry);
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);

    }

    @Test(description = "Add lifecycle to a Schema and update Schema")
    public void SchemaAddLifecycleAndUpdateResource()
            throws RegistryException, CustomLifecyclesChecklistAdminServiceExceptionException,
                   IOException, InterruptedException, RegistryExceptionException {
        schemaPathDev = "/_system/governance" + Utils.addSchema("LifeCycleState.xsd", governance);
        lifeCycleAdminService.addAspect(schemaPathDev, ASPECT_NAME);
        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(schemaPathDev);
        Resource service = registry.get(schemaPathDev);
        Assert.assertNotNull(service, "Service Not found on registry path " + schemaPathDev);
        Assert.assertEquals(service.getPath(), schemaPathDev, "Service path changed after adding life cycle. " + schemaPathDev);

        Assert.assertEquals(Utils.getLifeCycleState(lifeCycle), "Development", "LifeCycle State Mismatched");

        schemaPathDev = "/_system/governance" + Utils.addSchema("LifeCycleState.xsd", governance);
        Thread.sleep(500);

        lifeCycle = lifeCycleAdminService.getLifecycleBean(schemaPathDev);
        service = registry.get(schemaPathDev);
        Assert.assertNotNull(service, "Service Not found on registry path " + schemaPathDev);
        Assert.assertEquals(service.getPath(), schemaPathDev, "Service path changed after adding life cycle. " + schemaPathDev);

        Assert.assertNotNull(lifeCycle, "Life Cycle Not Found after updating resource");
        Assert.assertNotNull(lifeCycle.getLifecycleProperties(), "Life Cycle properties Not Found after updating resource");
        Assert.assertEquals(Utils.getLifeCycleState(lifeCycle), "Development", "LifeCycle State Mismatched after updating resource");


    }

    @Test( description = "Add lifecycle to a policy and update policy")
    public void policyAddLifecycleAndUpdateResource()
            throws RegistryException, CustomLifecyclesChecklistAdminServiceExceptionException,
                   IOException, InterruptedException, RegistryExceptionException {

        PolicyManager policyManager = new PolicyManager(governance);
        String policyFilePath = GregTestUtils.getResourcePath() + File.separator + "policy" + File.separator;
        Policy policy = policyManager.newPolicy(GregTestUtils.readFile(policyFilePath + "UTPolicy.xml").getBytes(),
                "PolicyLifeCycleState.xml");
        policyManager.addPolicy(policy);
        policyPathDev = "/_system/governance" + policyManager.getPolicy(policy.getId()).getPath();

        lifeCycleAdminService.addAspect(policyPathDev, ASPECT_NAME);
        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(policyPathDev);
        Resource service = registry.get(policyPathDev);
        Assert.assertNotNull(service, "Policy Not found on registry path " + policyPathDev);
        Assert.assertEquals(service.getPath(), policyPathDev, "Policy path changed after adding life cycle. " + policyPathDev);

        Assert.assertEquals(Utils.getLifeCycleState(lifeCycle), "Development", "LifeCycle State Mismatched");


        Policy policy1 = policyManager.getPolicy(policy.getId());
        policy1.addAttribute("rrrr", "tttt");
        policyManager.updatePolicy(policy1);

        policyPathDev = "/_system/governance" + policyManager.getPolicy(policy1.getId()).getPath();

        Thread.sleep(500);

        lifeCycle = lifeCycleAdminService.getLifecycleBean(policyPathDev);
        service = registry.get(policyPathDev);
        Assert.assertNotNull(service, "Service Not found on registry path " + policyPathDev);
        Assert.assertEquals(service.getPath(), policyPathDev, "Service path changed after adding life cycle. " + policyPathDev);

        Assert.assertNotNull(lifeCycle, "Life Cycle Not Found after updating resource");
        Assert.assertNotNull(lifeCycle.getLifecycleProperties(), "Life Cycle properties Not Found after updating resource");
        Assert.assertEquals(Utils.getLifeCycleState(lifeCycle), "Development", "LifeCycle State Mismatched after updating resource");

    }

    @Test( description = "Add lifecycle to a WSDl and update WSDL")
    public void WSDLAddLifecycleAndUpdateResource()
            throws RegistryException, CustomLifecyclesChecklistAdminServiceExceptionException,
                   IOException, InterruptedException, RegistryExceptionException {
        wsdlPathDev = "/_system/governance" + Utils.addWSDL("echoWsdlLifeCycleState.wsdl", governance);
        lifeCycleAdminService.addAspect(wsdlPathDev, ASPECT_NAME);
        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(wsdlPathDev);
        Resource service = registry.get(wsdlPathDev);
        Assert.assertNotNull(service, "Service Not found on registry path " + wsdlPathDev);
        Assert.assertEquals(service.getPath(), wsdlPathDev, "Service path changed after adding life cycle. " + wsdlPathDev);

        Assert.assertEquals(Utils.getLifeCycleState(lifeCycle), "Development", "LifeCycle State Mismatched");

        wsdlPathDev = "/_system/governance" + Utils.addWSDL("echoWsdlLifeCycleState.wsdl", governance);
        Thread.sleep(500);

        lifeCycle = lifeCycleAdminService.getLifecycleBean(wsdlPathDev);
        service = registry.get(wsdlPathDev);
        Assert.assertNotNull(service, "Service Not found on registry path " + wsdlPathDev);
        Assert.assertEquals(service.getPath(), wsdlPathDev, "Service path changed after adding life cycle. " + wsdlPathDev);

        Assert.assertNotNull(lifeCycle, "Life Cycle Not Found after updating resource");
        Assert.assertNotNull(lifeCycle.getLifecycleProperties(), "Life Cycle properties Not Found after updating resource");
        Assert.assertEquals(Utils.getLifeCycleState(lifeCycle), "Development", "LifeCycle State Mismatched after updating resource");

    }

    @Test( description = "Add lifecycle to a Service and update Service")
    public void serviceAddLifecycleAndUpdateResource()
            throws Exception {

        ServiceManager serviceManager = new ServiceManager(governance);
        Service  serviceNew =serviceManager.newService(new QName("sns", "ServiceLifeCycleState"));
        serviceManager.addService(serviceNew);
        servicePathDev = "/_system/governance" + serviceManager.getService(serviceNew.getId()).getPath();

        lifeCycleAdminService.addAspect(servicePathDev, ASPECT_NAME);
        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathDev);
        Resource service = registry.get(servicePathDev);
        Assert.assertNotNull(service, "Service Not found on registry path " + servicePathDev);
        Assert.assertEquals(service.getPath(), servicePathDev, "Service path changed after adding life cycle. " + servicePathDev);

        Assert.assertEquals(Utils.getLifeCycleState(lifeCycle), "Development", "LifeCycle State Mismatched");

        Service service1 = serviceManager.getService(serviceNew.getId());
        service1.addAttribute("overview_scopes","http://docs.oasis-open.org/ws-dd/ns/discovery/2009/01/DefaultScope");
        serviceManager.updateService(service1);

        servicePathDev = "/_system/governance" + service1.getPath();
        Thread.sleep(500);

        lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathDev);
        service = registry.get(servicePathDev);
        Assert.assertNotNull(service, "Service Not found on registry path " + servicePathDev);
        Assert.assertEquals(service.getPath(), servicePathDev, "Service path changed after adding life cycle. " + servicePathDev);

        Assert.assertNotNull(lifeCycle, "Life Cycle Not Found after updating resource");
        Assert.assertNotNull(lifeCycle.getLifecycleProperties(), "Life Cycle properties Not Found after updating resource");
        Assert.assertEquals(Utils.getLifeCycleState(lifeCycle), "Development", "LifeCycle State Mismatched after updating resource");

    }


    @AfterClass
    public void cleanUp() throws RegistryException {
        if (schemaPathDev != null) {
            registry.delete(schemaPathDev);
        }
        if (policyPathDev != null) {
            registry.delete(policyPathDev);
        }
        if (wsdlPathDev != null) {
            registry.delete(wsdlPathDev);
        }
        if (servicePathDev != null) {
            registry.delete(servicePathDev);
        }
        registry = null;
        lifeCycleAdminService = null;
    }


}
