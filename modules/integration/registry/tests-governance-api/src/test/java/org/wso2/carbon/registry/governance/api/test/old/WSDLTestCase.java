/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
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
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.schema.dataobjects.Schema;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.api.wsdls.WsdlFilter;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import javax.xml.namespace.QName;
import java.util.List;

import static org.testng.Assert.*;

public class WSDLTestCase {

    private Registry governanceRegistry;
    private static final Log log = LogFactory.getLog(WSDLTestCase.class);
    private Wsdl wsdlIdentifierUsedLast;
    private WSRegistryServiceClient wsRegistry;

    @BeforeClass(groups = {"wso2.greg"})
    public void initTest() throws AxisFault, RegistryException {
        int userId = 1;
        RegistryProviderUtil provider = new RegistryProviderUtil();
        wsRegistry = provider.getWSRegistry(userId,
                ProductConstant.GREG_SERVER_NAME);
        governanceRegistry = provider.getGovernanceRegistry(wsRegistry, userId);

    }

    @Test(groups = {"wso2.greg"})
    public void testAddWSDL() throws Exception {
        log.info("############## testAddWSDL started ...###################");
        WsdlManager wsdlManager = new WsdlManager(governanceRegistry);

        Wsdl wsdl = wsdlManager
                .newWsdl("http://svn.wso2.org/repos/wso2/carbon/platform/trunk/components/governance/org.wso2.carbon.governance.api/src/test/resources/test-resources/wsdl/BizService.wsdl");
        wsdl.addAttribute("creator", "it is me");
        wsdl.addAttribute("version", "0.01");
        wsdlManager.addWsdl(wsdl);

        Wsdl newWsdl = wsdlManager.getWsdl(wsdl.getId());
        assertEquals(newWsdl.getWsdlElement().toString(), wsdl.getWsdlElement()
                .toString());
        assertEquals(newWsdl.getAttribute("creator"), "it is me");
        assertEquals(newWsdl.getAttribute("version"), "0.01");

        // change the target namespace and check
        String oldWSDLPath = newWsdl.getPath();
        assertEquals(oldWSDLPath, "/trunk/wsdls/com/foo/BizService.wsdl");
        assertTrue(governanceRegistry
                .resourceExists("/trunk/wsdls/com/foo/BizService.wsdl"));

        OMElement wsdlElement = newWsdl.getWsdlElement();
        wsdlElement.addAttribute("targetNamespace", "http://ww2.wso2.org/test",
                null);
        wsdlElement.declareNamespace("http://ww2.wso2.org/test", "tns");
        wsdlManager.updateWsdl(newWsdl);


        assertEquals(newWsdl.getPath(),
                "/trunk/wsdls/org/wso2/ww2/test/BizService.wsdl");
        assertFalse(governanceRegistry.resourceExists("/wsdls/http/foo/com/BizService.wsdl"));
        wsRegistry.delete("/_system/governance/trunk/services/com/foo/BizService");       //deleting associated service of old wsdl

        // doing an update without changing anything.
        wsdlManager.updateWsdl(newWsdl);


        assertEquals(newWsdl.getPath(),
                "/trunk/wsdls/org/wso2/ww2/test/BizService.wsdl");
        assertEquals(newWsdl.getAttribute("version"), "0.01");

        newWsdl = wsdlManager.getWsdl(wsdl.getId());
        assertEquals(newWsdl.getAttribute("creator"), "it is me");
        assertEquals(newWsdl.getAttribute("version"), "0.01");

        // adding a new schema to the wsdl.
        wsdlElement = newWsdl.getWsdlElement();
        OMElement schemaElement = evaluateXPathToElement("//xsd:schema",
                wsdlElement);

        OMFactory factory = OMAbstractFactory.getOMFactory();

        OMElement importElement = factory.createOMElement(new QName(
                "http://www.w3.org/2001/XMLSchema", "import"));
        importElement
                .addAttribute(
                        "schemaLocation",
                        "http://svn.wso2.org/repos/wso2/carbon/platform/trunk/components/governance/org.wso2.carbon.governance.api/src/test/resources/test-resources/xsd/purchasing_dup.xsd",
                        null);
        schemaElement.addChild(importElement);
        importElement.addAttribute("namespace",
                "http://bar.org/purchasing_dup", null);

        wsdlManager.updateWsdl(newWsdl);


        Schema[] schemas = newWsdl.getAttachedSchemas();

        // test log
        log.info("####Schemas#####");
        for (Schema schema : schemas) {
            log.info("#####Schema:" + schema.getId() + " schemaName"
                    + schema.getQName().toString());
        }

        assertEquals(schemas[schemas.length - 1].getPath(),
                "/trunk/schemas/org/bar/purchasing_dup/purchasing_dup.xsd");

        Wsdl[] wsdls = wsdlManager.findWsdls(new WsdlFilter() {
            public boolean matches(Wsdl wsdl) throws GovernanceException {
                Schema[] schemas = wsdl.getAttachedSchemas();
                for (Schema schema : schemas) {
                    if (schema
                            .getPath()
                            .equals("/trunk/schemas/org/bar/purchasing_dup/purchasing_dup.xsd")) {
                        log.info("###### Matching Schemas name"
                                + schema.getQName().toString() + "  schemaID:"
                                + schema.getId());
                        return true;
                    }
                }
                return false;
            }
        });
        log.info("WSDL len:" + wsdls.length);
        assertEquals(wsdls.length, 1);
        assertEquals(newWsdl.getId(), wsdl.getId());

        // deleting the wsdl
        deleteWsdl(newWsdl);
        Wsdl deletedWsdl = wsdlManager.getWsdl(newWsdl.getId());

        assertNull(deletedWsdl);

        // add again
        Wsdl anotherWsdl = wsdlManager
                .newWsdl("http://svn.wso2.org/repos/wso2/carbon/platform/trunk/components/governance/org.wso2.carbon.governance.api/src/test/resources/test-resources/wsdl/BizService.wsdl");
        anotherWsdl.addAttribute("creator", "it is not me");
        anotherWsdl.addAttribute("version", "0.02");
        wsdlManager.addWsdl(anotherWsdl);

        // and delete the wsdl
        deleteWsdl(anotherWsdl);
        assertNull(wsdlManager.getWsdl(anotherWsdl.getId()));

    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"testAddWSDL"})
    public void testEditWSDL() throws Exception {
        initTest();

        WsdlManager wsdlManager = new WsdlManager(governanceRegistry);

        Wsdl wsdl = wsdlManager
                .newWsdl("http://svn.wso2.org/repos/wso2/carbon/platform/trunk/components/governance/org.wso2.carbon.governance.api/src/test/resources/test-resources/wsdl/BizService.wsdl");
        wsdl.addAttribute("creator2", "it is me");
        wsdl.addAttribute("version2", "0.01");
        wsdlManager.addWsdl(wsdl);

        // now edit the wsdl
        OMElement contentElement = wsdl.getWsdlElement();
        OMElement addressElement = evaluateXPathToElement("//soap:address",
                contentElement);
        addressElement.addAttribute("location",
                "http://my-custom-endpoint/hoooo", null);
        wsdl.setWsdlElement(contentElement);

        // now do an update.
        wsdlManager.updateWsdl(wsdl);

        // now get the wsdl and check the update is there.
        wsdlIdentifierUsedLast = wsdlManager.getWsdl(wsdl.getId());
        assertEquals(wsdlIdentifierUsedLast.getAttribute("creator2"), "it is me");
        assertEquals(wsdlIdentifierUsedLast.getAttribute("version2"), "0.01");
        OMElement contentElement2 = wsdl.getWsdlElement();
        OMElement addressElement2 = evaluateXPathToElement("//soap:address",
                contentElement2);

        assertEquals(addressElement2.getAttributeValue(new QName("location")),
                "http://my-custom-endpoint/hoooo");
        deleteWsdl(wsdl);
    }

    private static OMElement evaluateXPathToElement(String expression,
                                                    OMElement root) throws Exception {
        List<OMElement> nodes = GovernanceUtils.evaluateXPathToElements(
                expression, root);
        if (nodes == null || nodes.size() == 0) {
            return null;
        }
        return nodes.get(0);
    }

    public boolean deleteWsdl(Wsdl wsdl) throws RegistryException {

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


        return true;
    }

    @AfterClass(groups = "wso2.greg", alwaysRun = true, description = "cleaning up the artifacts added")
    public void tearDown() throws RegistryException {
        governanceRegistry = null;
        wsRegistry = null;

    }

}
