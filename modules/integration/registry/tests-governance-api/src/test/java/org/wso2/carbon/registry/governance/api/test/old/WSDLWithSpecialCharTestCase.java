/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.registry.governance.api.test.old;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.endpoints.dataobjects.Endpoint;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import static org.testng.Assert.assertTrue;

/*
 Test case for WSO2 Governance Registry - Allowing '?' in the wsdl url via Governance Registry API
 https://wso2.org/jira/browse/CARBON-11012
 */
public class WSDLWithSpecialCharTestCase {

    private static final Log log = LogFactory
            .getLog(WSDLWithSpecialCharTestCase.class);
    private static WSRegistryServiceClient wsRegistry = null;
    private static Registry governanceRegistry = null;
    private final static String WSDL_URL = "http://ws.strikeiron.com/donotcall2_5?WSDL";
    private static String resourcePath = "/_system/governance/trunk/wsdls/com/strikeiron/www/donotcall2_5.wsdl";
    private Wsdl wsdl;

    @BeforeClass(alwaysRun = true)
    public void init() throws RegistryException, AxisFault {
        int userId = 1;
        RegistryProviderUtil provider = new RegistryProviderUtil();
        wsRegistry = provider.getWSRegistry(userId,
                ProductConstant.GREG_SERVER_NAME);
        governanceRegistry = provider.getGovernanceRegistry(wsRegistry, userId);
    }

    @Test(groups = {"wso2.greg"}, description = "upload wsdl with special characters", priority = 1)
    public void testSpecialCharWSDL() throws RegistryException {
        WsdlManager manager = new WsdlManager(governanceRegistry);
        wsdl = manager.newWsdl(WSDL_URL);
        manager.addWsdl(wsdl);
        assertTrue(wsRegistry.resourceExists(resourcePath));
        log.info("Add WSDL was successful");
    }

    @Test(groups = {"wso2.greg"}, description = "Verify wsdl properties", priority = 2, dependsOnMethods = "testSpecialCharWSDL")
    public void testVerityWSDLProperties() throws RegistryException {
        WsdlManager manager = new WsdlManager(governanceRegistry);
        Wsdl[] wsdl = manager.getAllWsdls();
        boolean status = false;
        for (Wsdl wsdlInfo : wsdl) {
            if (wsdlInfo.getQName().toString().contains("donotcall2_5.wsdl")) {
                status = true;
            }
        }
        assertTrue(status, "WSDL name not found in the registry");
        Resource wsdlResource = wsRegistry.get(resourcePath);
        assertTrue(
                new String((byte[]) wsdlResource.getContent())
                        .contains("DoNotCallRegistry"),
                "Expected wsdl content not found");
        log.info("WSDL verification was successful");
    }

    @AfterClass(groups = {"wso2.greg"}, description = "Remove the added wsdl")
    public void testCleanup() throws RegistryException {
        GovernanceArtifact[] governanceArtifacts = wsdl.getDependents();
        GovernanceArtifact[] dependencies = wsdl.getDependencies();
        final String pathPrefix = "/_system/governance";

        Endpoint[] endpoints;
        endpoints = wsdl.getAttachedEndpoints();


        for (GovernanceArtifact tmpGovernanceArtifact : governanceArtifacts) {

            if (wsRegistry.resourceExists(pathPrefix + tmpGovernanceArtifact.getPath())) {
                wsRegistry.delete(pathPrefix + tmpGovernanceArtifact.getPath());
            }

        }

        for (GovernanceArtifact tmpGovernanceArtifact : dependencies) {
            if (!tmpGovernanceArtifact.getPath().contains("/trunk/endpoints/")) {
                if (wsRegistry.resourceExists(pathPrefix + tmpGovernanceArtifact.getPath())) {
                    wsRegistry.delete(pathPrefix + tmpGovernanceArtifact.getPath());
                }
            }
        }
        for (Endpoint tmpEndpoint : endpoints) {
            if (wsRegistry.resourceExists(pathPrefix + tmpEndpoint.getPath())) {
                wsRegistry.delete(pathPrefix + tmpEndpoint.getPath());
            }
        }

        wsdl = null;
        wsRegistry = null;
        governanceRegistry = null;
    }

}
