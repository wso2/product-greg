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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.llom.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.endpoints.EndpointManager;
import org.wso2.carbon.governance.api.endpoints.dataobjects.Endpoint;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import javax.xml.namespace.QName;
import java.util.List;

import static org.testng.Assert.*;

public class WSDLUpdateContentServiceTestCase {
    private static final Log log = LogFactory
            .getLog(WSDLUpdateContentServiceTestCase.class);
    private static WSRegistryServiceClient wsRegistry = null;
    private static Registry governanceRegistry = null;
    String wsdl_path = "/_system/governance/trunk/wsdls/com/foo/BizService.wsdl";
    String service_path = "/_system/governance/trunk/services/com/foo/BizService";
    String endPoint_path = "/_system/governance/trunk/endpoints/com/wso2/people/services/ep-BizService";
    private Wsdl wsdl;

    @BeforeClass(alwaysRun = true)
    public void init() throws RegistryException, AxisFault {
        int userId = 1;
        RegistryProviderUtil provider = new RegistryProviderUtil();
        wsRegistry = provider.getWSRegistry(userId,
                ProductConstant.GREG_SERVER_NAME);
        governanceRegistry = provider.getGovernanceRegistry(wsRegistry, userId);
    }

    @Test(groups = {"wso2.greg"}, description = "Update WSDL content test", priority = 1)
    public void testUpdateWsdlContent() throws Exception {
        String wsdl_url = "http://people.wso2.com/~evanthika/wsdls/BizService.wsdl";
        // String wsdl_url =
        // "http://svn.wso2.org/repos/wso2/trunk/carbon/components/governance/org.wso2.carbon.governance.api/src/test/resources/test-resources/wsdl/BizService.wsdl";
        WsdlManager wsdlManager = new WsdlManager(governanceRegistry);
        ServiceManager serviceManager = new ServiceManager(governanceRegistry);
        EndpointManager endpointManager = new EndpointManager(governanceRegistry);

        wsdl = wsdlManager.newWsdl(wsdl_url);
        wsdl.addAttribute("creator", "Aaaa");
        wsdl.addAttribute("version", "0.01");
        wsdlManager.addWsdl(wsdl);
        log.info("wsdl was added");

        Resource r1 = wsRegistry.newResource();
        r1 = wsRegistry.get(wsdl_path);
        String content = new String((byte[]) r1.getContent());
        // Assert Content location
        assertTrue(
                content.indexOf("http://people.wso2.com:9763/services/BizService") > 0,
                "Assert Content -location :");

          // add test here to change the wsdl port and check
        OMElement contentElement3 = wsdl.getWsdlElement();
        OMElement addressElement3 = evaluateXPathToElement("//soap:address",
                contentElement3);

        String endpoint = addressElement3.getAttributeValue(new QName("location"));

        String contentString = contentElement3.toString();
        contentString = contentString.replace("9763" , "9764");
        OMElement updatedElement = AXIOMUtil.stringToOM(contentString);
        wsdl.setWsdlElement(updatedElement);
        wsdlManager.updateWsdl(wsdl);

        String updatedEndpoint = endpoint.replace("9763" , "9764");

        Endpoint[] wsdlEps = wsdlManager.getWsdl(wsRegistry.get(wsdl_path).getUUID()).getAttachedEndpoints();
        for(Endpoint ep : wsdlEps) {
            //assert endpoint change
            assertEquals(ep.getUrl(), updatedEndpoint, "attached endpoints of wsdl should be updated with new url");
        }

        Endpoint[] serviceEPs = serviceManager.getService(wsRegistry.get(service_path).getUUID()).getAttachedEndpoints();
        for(Endpoint ep : serviceEPs) {
            //assert endpoint change
            assertEquals(ep.getUrl(), updatedEndpoint, "attached endpoints of service related to the wsdl should be updated with new url");
        }

        //assert endpoint change
        assertEquals(endpointManager.getEndpoint(wsRegistry.get(endPoint_path).getUUID()).getUrl(), updatedEndpoint, "endpoint should be updated with new url");

        // Edit wsdl content
        OMElement contentElement = wsdl.getWsdlElement();
        OMElement addressElement = evaluateXPathToElement("//soap:address",
                contentElement);
        addressElement.addAttribute("location",
                "http://my-custom-endpoint/hoooo", null);
        wsdl.setWsdlElement(contentElement);

        // now do an update.
        wsdlManager.updateWsdl(wsdl);

        OMElement contentElement2 = wsdl.getWsdlElement();
        OMElement addressElement2 = evaluateXPathToElement("//soap:address",
                contentElement2);

        // create new resource
        Resource r2 = wsRegistry.newResource();
        r2 = wsRegistry.get(wsdl_path);
        String content2 = new String((byte[]) r2.getContent());

        // Assert initial value has been updated properly
        assertFalse(
                content2.indexOf("http://localhost:8080/axis2/services/BizService") > 0,
                "Assert Content wsdl file - key word 1");
        assertEquals("http://my-custom-endpoint/hoooo",
                addressElement2.getAttributeValue(new QName("location")));


        log.info("WsdlUpadateContentServiceTestClient testUpdateWsdlContent() - Passed");

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

    @AfterClass(groups = "wso2.greg", alwaysRun = true, description = "cleaning up the artifacts added")
    public void tearDown() throws RegistryException {

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

