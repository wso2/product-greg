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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.endpoints.EndpointManager;
import org.wso2.carbon.governance.api.endpoints.dataobjects.Endpoint;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.schema.SchemaManager;
import org.wso2.carbon.governance.api.schema.dataobjects.Schema;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;

public class WSDLAttachmentTestCase {
    public WsdlManager wsdlManager;
    public EndpointManager endpointManager;
    public SchemaManager schemaManager;
    private static Wsdl wsdlObj;
    public String sampleWsdlURL = "http://ws.strikeiron.com/donotcall2_5?WSDL";
    private Endpoint endpoint;
    private Schema schema;
        private WSRegistryServiceClient wsRegistry;


    @BeforeClass(alwaysRun = true)
    public void initializeAPIObject() throws RegistryException, AxisFault {

        int userId = 1;
        RegistryProviderUtil provider = new RegistryProviderUtil();
        wsRegistry = provider.getWSRegistry(userId,
                ProductConstant.GREG_SERVER_NAME);
        Registry governanceRegistry = provider.getGovernanceRegistry(wsRegistry, userId);

        wsdlManager = new WsdlManager(governanceRegistry);
        endpointManager = new EndpointManager(governanceRegistry);
        schemaManager = new SchemaManager(governanceRegistry);

    }

    @Test(groups = {"wso2.greg"}, description = "Testing "
            + "addWsdl API method", priority = 1)
    public void testAddWsdl() throws GovernanceException {

        wsdlObj = wsdlManager.newWsdl(sampleWsdlURL);
        wsdlManager.addWsdl(wsdlObj);

    }

    @Test(groups = {"wso2.greg"}, description = "Testing attachEndpoint method in WSDL object", enabled = true, dependsOnMethods = "testAddWsdl")
    public void testAttachEndpoint() throws GovernanceException {
        endpoint = endpointManager
                .newEndpoint("http://localhost:9763/services/TestEndPointManager");
        endpointManager.addEndpoint(endpoint);

        wsdlObj.attachEndpoint(endpoint);
        boolean endpointContains = false;
        Endpoint[] endpoints = wsdlManager.getWsdl(wsdlObj.getId())
                .getAttachedEndpoints();
        for (Endpoint endpoint1 : endpoints) {
            if ("http://localhost:9763/services/TestEndPointManager"
                    .equalsIgnoreCase(endpoint1.getUrl())) {
                endpointContains = true;
            }
        }
        assertTrue(endpointContains, "Endpoint addition failed");

    }

    @Test(groups = {"wso2.greg"}, description = "Testing getAttachEndpoint method in WSDL object", enabled = true, dependsOnMethods = "testAttachEndpoint")
    public void testGetAttachEndpoint() throws GovernanceException {

        boolean isEndpointFound = false;

        Endpoint[] endpoints = wsdlObj.getAttachedEndpoints();
        for (Endpoint e : endpoints) {
            if (e.getUrl().contains(
                    "http://localhost:9763/services/TestEndPointManager")) {
                isEndpointFound = true;
            }
        }
        assertTrue(isEndpointFound, "WSDL:getAttachEndpoint throwing an error");

    }

    @Test(groups = {"wso2.greg"}, description = "Testing attachSchema method in WSDL object", enabled = true, dependsOnMethods = "testGetAttachEndpoint")
    public void testAttachSchema() throws GovernanceException {

        schema = schemaManager
                .newSchema("https://svn.wso2.org/repos/wso2/trunk/commons/qa/"
                        + "qa-artifacts/greg/xsd/calculator.xsd");
        schemaManager.addSchema(schema);

        wsdlManager.getWsdl(wsdlObj.getId()).attachSchema(schema);

    }

    @Test(groups = {"wso2.greg"}, description = "Testing GetAttachSchema method in WSDL object", enabled = true, dependsOnMethods = "testAttachSchema")
    public void testGetAttachSchema() throws GovernanceException {
        boolean isSchemaFound = false;

        Schema[] schema = wsdlManager.getWsdl(wsdlObj.getId())
                .getAttachedSchemas();
        for (Schema s : schema) {
            if (s.getQName().getLocalPart().equalsIgnoreCase("calculator.xsd")) {
                isSchemaFound = true;
            }
        }
        assertTrue(
                isSchemaFound,
                "Error occurred while executing getAttachedSchemas API method with WSDL object.");

    }

    @Test(groups = {"wso2.greg"}, description = "Testing GetAttachSchema method in WSDL object", enabled = true, dependsOnMethods = "testGetAttachSchema")
    public void testDetachSchema() throws GovernanceException {

        Schema[] schema = wsdlManager.getWsdl(wsdlObj.getId())
                .getAttachedSchemas();
        for (Schema s : schema) {
            if (s.getQName().getLocalPart().equalsIgnoreCase("calculator.xsd")) {
                wsdlManager.getWsdl(wsdlObj.getId()).detachSchema(s.getId());
            }
        }
        schema = wsdlManager.getWsdl(wsdlObj.getId()).getAttachedSchemas();
        boolean check = true;
        for (Schema s : schema) {
            if (s.getQName().getLocalPart().equalsIgnoreCase("calculator.xsd")) {
                check = false;
            }
        }
        assertTrue(check, "detachSchema method didn't work with WSDL object");

    }

    @AfterClass(groups = "wso2.greg", alwaysRun = true, description = "cleaning up the artifacts added")
    public void tearDown() throws RegistryException, RemoteException,
            LoginAuthenticationExceptionException,
            ResourceAdminServiceExceptionException {

        schemaManager.removeSchema(schema.getId());
        GovernanceArtifact[] governanceArtifacts = wsdlObj.getDependents();
        final String pathPrefix = "/_system/governance";
        Endpoint[] endpoints;
        endpoints = wsdlObj.getAttachedEndpoints();

        for (GovernanceArtifact tmpGovernanceArtifact : governanceArtifacts) {

            if (wsRegistry.resourceExists(pathPrefix + tmpGovernanceArtifact.getPath())) {
                wsRegistry.delete(pathPrefix + tmpGovernanceArtifact.getPath());
            }

        }

        for (Endpoint tmpEndpoint : endpoints) {
            if (wsRegistry.resourceExists(pathPrefix + tmpEndpoint.getPath())){
                wsRegistry.delete(pathPrefix + tmpEndpoint.getPath());
            }
        }
        wsdlManager = null;
        endpoint = null;
        wsdlObj = null;
        endpointManager = null;
        schema = null;
        schemaManager = null;
        wsRegistry = null;

    }


}
