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

package org.wso2.carbon.registry.soapservice.test.wsdl;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.endpoints.dataobjects.Endpoint;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import java.net.MalformedURLException;
import java.rmi.RemoteException;

import static org.testng.Assert.*;

public class EncodedUrlWsdlAdditionTestCase extends GREGIntegrationBaseTest {

    private Registry governanceRegistry;
    private Wsdl wsdl;
    private WsdlManager wsdlManager;
    private WSRegistryServiceClient wsRegistry;

    @BeforeClass (groups = "wso2.greg", alwaysRun = true)
    public void initialize () throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String sessionCookie = new LoginLogoutClient(automationContext).login();
        RegistryProviderUtil provider = new RegistryProviderUtil();
        wsRegistry = provider.getWSRegistry(automationContext);
        governanceRegistry = provider.getGovernanceRegistry(wsRegistry, automationContext);
        wsdlManager = new WsdlManager(governanceRegistry);

    }

    /**
     * adding a encoded URL wsdl
     */
    @Test (groups = "wso2.greg", description = "Add Encoded WSDL")
    public void testAddEncodedURLWSDL () throws RemoteException, ResourceAdminServiceExceptionException,
            GovernanceException, MalformedURLException {

        wsdl = wsdlManager.newWsdl(
                "https://raw.githubusercontent.com/wso2/wso2-qa-artifacts/master/automation-artifacts/greg" +
                        "/wsdl/StockQuote.wsdl");

        wsdl.addAttribute("version", "1.0.0");
        wsdl.addAttribute("author", "Aparna");
        wsdl.addAttribute("description", "added encoded url wsdl");
        wsdlManager.addWsdl(wsdl);
        assertFalse(wsdl.getId().isEmpty());
        assertNotNull(wsdl);
        assertTrue(wsdl.getAttribute("author").contentEquals("Aparna"));    // encoded url wsdl
                                                                            // addition verification
    }

    @AfterClass (groups = "wso2.greg", alwaysRun = true, description = "cleaning up the artifacts added")
    public void tearDown () throws RegistryException, LoginAuthenticationExceptionException, RemoteException,
            ResourceAdminServiceExceptionException {

        String pathPrefix = "/_system/governance";
        Endpoint[] endpoints;

        endpoints = wsdl.getAttachedEndpoints();
        assertNotNull(endpoints, "there should be associated endpoints with the wsdl");

        GovernanceArtifact[] governanceArtifacts = wsdl.getDependents();
        assertNotNull(governanceArtifacts, "there should be dependent of the wsdl");

        for (GovernanceArtifact tmpGovernanceArtifact : governanceArtifacts) {
            wsRegistry.delete(pathPrefix + tmpGovernanceArtifact.getPath());
        }

        for (Endpoint tmpEndpoint : endpoints) {
            GovernanceArtifact[] dependentArtifacts = tmpEndpoint.getDependents();
            for (GovernanceArtifact tmpGovernanceArtifact : dependentArtifacts) {
                wsRegistry.delete(pathPrefix + tmpGovernanceArtifact.getPath());
            }
            wsRegistry.delete(pathPrefix + tmpEndpoint.getPath());
        }
    }
}
