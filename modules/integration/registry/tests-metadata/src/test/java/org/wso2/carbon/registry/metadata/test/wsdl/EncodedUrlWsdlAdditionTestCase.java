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
package org.wso2.carbon.registry.metadata.test.wsdl;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.endpoints.dataobjects.Endpoint;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import java.net.MalformedURLException;
import java.rmi.RemoteException;

import static org.testng.Assert.*;

public class EncodedUrlWsdlAdditionTestCase {

    private Registry governanceRegistry;
    private Wsdl wsdl;
    private WsdlManager wsdlManager;
    private WSRegistryServiceClient wsRegistry;

    @BeforeClass(groups = "wso2.greg", alwaysRun = true)
    public void initialize() throws RemoteException,
            LoginAuthenticationExceptionException,
            org.wso2.carbon.registry.api.RegistryException {
        int userId = 2;

        RegistryProviderUtil provider = new RegistryProviderUtil();
        wsRegistry = provider.getWSRegistry(userId,
                ProductConstant.GREG_SERVER_NAME);
        governanceRegistry = provider.getGovernanceRegistry(wsRegistry, userId);
        wsdlManager = new WsdlManager(governanceRegistry);

    }

    /**
     * adding a encoded URL wsdl
     */
    @Test(groups = "wso2.greg", description = "Add Encoded WSDL")
    public void testAddEncodedURLWSDL() throws RemoteException,
            ResourceAdminServiceExceptionException,
            GovernanceException,
            MalformedURLException {


        wsdl = wsdlManager
                .newWsdl("https://svn.wso2.org/repos/wso2/carbon/platform/branches/4.2.0/products/greg/4.6.0/modules/" +
                        "integration/registry/tests-metadata/src/test/resources/artifacts/GREG/wsdl/StockQuote.wsdl");

        wsdl.addAttribute("version", "1.0.0");
        wsdl.addAttribute("author", "Aparna");
        wsdl.addAttribute("description", "added encoded url wsdl");
        wsdlManager.addWsdl(wsdl);

        assertFalse(wsdl.getId().isEmpty());
        assertNotNull(wsdl);
        assertTrue(wsdl.getAttribute("author").contentEquals("Aparna")); // encoded
        // URL
        // WSDL
        // addition:
        // verification
    }

    @AfterClass(groups = "wso2.greg", alwaysRun = true, description = "cleaning up the artifacts added")
    public void tearDown()
            throws RegistryException, LoginAuthenticationExceptionException, RemoteException,
            ResourceAdminServiceExceptionException {

        String pathPrefix = "/_system/governance";

        Endpoint[] endpoints;
        endpoints = wsdl.getAttachedEndpoints();

        GovernanceArtifact[] governanceArtifacts = wsdl.getDependents();
        for (GovernanceArtifact tmpGovernanceArtifact : governanceArtifacts) {
            wsRegistry.delete(pathPrefix + tmpGovernanceArtifact.getPath());
        }
        
        for (Endpoint tmpEndpoint : endpoints) {
        	GovernanceArtifact[] dependentArtifacts =  tmpEndpoint.getDependents();
        	for (GovernanceArtifact tmpGovernanceArtifact : dependentArtifacts) {
                wsRegistry.delete(pathPrefix + tmpGovernanceArtifact.getPath());
            }
            wsRegistry.delete(pathPrefix + tmpEndpoint.getPath());
        }
        wsRegistry = null;
        governanceRegistry = null;
        wsdl = null;
        wsdl = null;


    }
}
