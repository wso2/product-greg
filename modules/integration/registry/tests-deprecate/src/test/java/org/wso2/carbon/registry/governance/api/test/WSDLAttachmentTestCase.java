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
package org.wso2.carbon.registry.governance.api.test;


import org.apache.axis2.AxisFault;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.governance.api.endpoints.EndpointManager;
import org.wso2.carbon.governance.api.endpoints.dataobjects.Endpoint;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.schema.SchemaManager;
import org.wso2.carbon.governance.api.schema.dataobjects.Schema;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import static org.testng.Assert.assertTrue;

public class WSDLAttachmentTestCase {
    public static WsdlManager wsdlManager;
    public static EndpointManager endpointManager;
    public static SchemaManager schemaManager;
    private static Wsdl wsdlObj;
    public String sampleWsdlURL = "http://ws.strikeiron.com/donotcall2_5?WSDL";

    @BeforeClass(alwaysRun = true)
    public void initializeAPIObject() throws RegistryException, AxisFault {
        Registry governance = TestUtils.getRegistry();
        TestUtils.cleanupResources(governance);
        wsdlManager = new WsdlManager(governance);
        endpointManager = new EndpointManager(governance);
        schemaManager = new SchemaManager(governance);

    }

    @Test(groups = {"wso2.greg.api"}, description = "Testing " + "addWsdl API method", priority = 1)
    public void testAddWsdl() throws GovernanceException {
        try {
            wsdlObj = wsdlManager.newWsdl(sampleWsdlURL);
            wsdlManager.addWsdl(wsdlObj);
        } catch (GovernanceException e) {
            throw new GovernanceException("Error occurred while executing WsdlManager:addWsdl method" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg.api"}, description = "Testing attachEndpoint method in WSDL object",
            enabled = true, dependsOnMethods = "testAddWsdl")
    public void testAttachEndpoint() throws GovernanceException {
        Endpoint endpoint = endpointManager.newEndpoint("http://localhost:9763/services/TestEndPointManager");
        endpointManager.addEndpoint(endpoint);
        try {
            wsdlObj.attachEndpoint(endpoint);
            boolean endpointContains = false;
            Endpoint[] endpoints = wsdlManager.getWsdl(wsdlObj.getId()).getAttachedEndpoints();
            for (Endpoint endpoint1 : endpoints) {
                if ("http://localhost:9763/services/TestEndPointManager".equalsIgnoreCase(endpoint1.getUrl())) {
                    endpointContains = true;
                }
            }
            Assert.assertTrue(endpointContains, "Endpoint add failed");
        } catch (GovernanceException e) {
            throw new GovernanceException("WSDL:attachEndpoint method throwing an error : " + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg.api"}, description = "Testing getAttachEndpoint method in WSDL object", enabled = true,
            dependsOnMethods = "testAttachEndpoint")
    public void testGetAttachEndpoint() throws GovernanceException {

        boolean isEndpointFound = false;
        try {
            Endpoint[] endpoints = wsdlObj.getAttachedEndpoints();
            for (Endpoint e : endpoints) {
                if (e.getUrl().contains("http://localhost:9763/services/TestEndPointManager")) {
                    isEndpointFound = true;
                }
            }
            assertTrue(isEndpointFound, "WSDL:getAttachEndpoint throwing an error");
        } catch (GovernanceException e) {
            throw new GovernanceException("WSDL:getAttachEndpoint method throwing an error : " + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg.api"}, description = "Testing attachSchema method in WSDL object", enabled = true,
            dependsOnMethods = "testGetAttachEndpoint")
    public void testAttachSchema() throws GovernanceException {

        Schema schema = schemaManager.newSchema("https://svn.wso2.org/repos/wso2/trunk/commons/qa/" +
                "qa-artifacts/greg/xsd/calculator.xsd");
        schemaManager.addSchema(schema);
        try {
            wsdlManager.getWsdl(wsdlObj.getId()).attachSchema(schema);
        } catch (GovernanceException e) {
            throw new GovernanceException("WSDL:attachSchema method throwing an error : ", e);
        }
    }

    @Test(groups = {"wso2.greg.api"}, description = "Testing GetAttachSchema method in WSDL object", enabled = true,
            dependsOnMethods = "testAttachSchema")
    public void testGetAttachSchema() throws GovernanceException {
        boolean isSchemaFound = false;
        try {
            Schema[] schema = wsdlManager.getWsdl(wsdlObj.getId()).getAttachedSchemas();
            for (Schema s : schema) {
                if (s.getQName().getLocalPart().equalsIgnoreCase("calculator.xsd")) {
                    isSchemaFound = true;
                }
            }
            assertTrue(isSchemaFound, "Error occurred while executing getAttachedSchemas API method with WSDL object.");
        } catch (GovernanceException e) {
            throw new GovernanceException("Error occurred while executing getAttachedSchemas API " +
                    "method with WSDL object" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg.api"}, description = "Testing GetAttachSchema method in WSDL object", enabled = true,
            dependsOnMethods = "testGetAttachSchema")
    public void testDetachSchema() throws GovernanceException {
        try {
            Schema[] schema = wsdlManager.getWsdl(wsdlObj.getId()).getAttachedSchemas();
            for (Schema s : schema) {
                if (s.getQName().getLocalPart().equalsIgnoreCase("calculator.xsd")) {
                    wsdlManager.getWsdl(wsdlObj.getId()).detachSchema(s.getId());
                }
            }
            schema = wsdlManager.getWsdl(wsdlObj.getId()).getAttachedSchemas();
            for (Schema s : schema) {
                if (s.getQName().getLocalPart().equalsIgnoreCase("calculator.xsd")) {
                    assertTrue(false, "detachSchema method didn't work with WSDL object");
                }
            }
        } catch (GovernanceException e) {
            throw new GovernanceException("Error occurred while executing detachSchema API " +
                    "method with WSDL object" + e.getMessage());
        }
    }
}
