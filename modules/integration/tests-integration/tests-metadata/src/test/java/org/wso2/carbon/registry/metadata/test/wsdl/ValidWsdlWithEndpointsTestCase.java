/*
 *Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.metadata.test.wsdl;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.governance.api.endpoints.dataobjects.Endpoint;
import org.wso2.carbon.governance.api.endpoints.dataobjects.EndpointImpl;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import java.net.MalformedURLException;
import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;

public class ValidWsdlWithEndpointsTestCase extends GREGIntegrationBaseTest {

    private Registry governanceRegistry;
    private Wsdl wsdl;
    private WsdlManager wsdlManager;
    private WSRegistryServiceClient wsRegistry;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private String sessionCookie;
    private String[] endpointArtifactPaths = {
            "/trunk/endpoints/localhost/services/ep-echo-yu-echoHttpsSoap11Endpoint",
            "/trunk/endpoints/localhost/services/ep-echo-yu-echoHttpSoap11Endpoint",
            "/trunk/endpoints/localhost/services/ep-echo-yu-echoHttpsSoap12Endpoint",
            "/trunk/endpoints/localhost/services/ep-echo-yu-echoHttpSoap12Endpoint",
            "/trunk/endpoints/localhost/services/ep-echo-yu-echoHttpEndpoint",
            "/trunk/endpoints/localhost/services/ep-echo-yu-echoHttpsEndpoint" };

    @BeforeClass(groups = "wso2.greg", alwaysRun = true)
    public void initialize() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        sessionCookie = new LoginLogoutClient(automationContext).login();
        String backEndUrl = getBackendURL();

        resourceAdminServiceClient = new ResourceAdminServiceClient(backEndUrl, sessionCookie);
        RegistryProviderUtil provider = new RegistryProviderUtil();
        wsRegistry = provider.getWSRegistry(automationContext);
        governanceRegistry = provider.getGovernanceRegistry(wsRegistry, automationContext);
        wsdlManager = new WsdlManager(governanceRegistry);
    }

    /**
     * adding a secured wsdl
     *
     * @throws MalformedURLException
     */
    @Test(groups = "wso2.greg", description = "Add secured URL WSDL Axis2Import.wsdl")
    public void testAddSecuredURLWSDLWithEndpoints()
            throws RemoteException, ResourceAdminServiceExceptionException, GovernanceException, MalformedURLException {

        wsdl = wsdlManager.newWsdl("https://svn.wso2.org" +
                "/repos/wso2/carbon/platform/trunk/platform-integration/" +
                "platform-automated-test-suite/org.wso2.carbon.automation.test.repo/" +
                "src/main/resources/artifacts/GREG/wsdl/echo.wsdl");
        wsdl.addAttribute("version", "1.0.0");
        wsdl.addAttribute("author", "NewUser");
        wsdl.addAttribute("description", "added wsdl with endpoints");
        wsdlManager.addWsdl(wsdl);

        Endpoint[] endpoints = wsdl.getAttachedEndpoints();
        Assert.assertNotNull(endpoints, "No Records Found");
        Assert.assertTrue((endpoints.length == 6), "There should be only 6 records");

        for (int i = 0; i < endpoints.length; i++) {
            log.info("Resource Path: " + ((EndpointImpl) endpoints[i]).getArtifactPath());
            assertTrue(endpointArtifactPaths[i].equals(((EndpointImpl) endpoints[i]).getArtifactPath()));
        }
    }

    @AfterClass(groups = "wso2.greg", alwaysRun = true, description = "cleaning up the artifacts added")
    @SetEnvironment(executionEnvironments = {
            ExecutionEnvironment.STANDALONE, ExecutionEnvironment.STANDALONE })
    public void tearDown()
            throws RegistryException, RemoteException, ResourceAdminServiceExceptionException {
        wsdlManager.removeWsdl(wsdl.getId());
        delete("/_system/governance/trunk/services/org/wso2/carbon/core/services/echo/1.0.0/echoyuSer1");
        for (int i = 0; i < endpointArtifactPaths.length; i++) {
            delete("/_system/governance" + endpointArtifactPaths[i]);
        }
        governanceRegistry = null;
        wsdl = null;
        wsdlManager = null;
    }

    private void delete(String destPath)
            throws ResourceAdminServiceExceptionException, RemoteException, RegistryException {
        if (wsRegistry.resourceExists(destPath)) {
            resourceAdminServiceClient.deleteResource(destPath);
        }
    }
}
