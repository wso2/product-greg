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


import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.governance.api.exception.GovernanceException;
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
    private static final Log log = LogFactory.getLog(WSDLUpdateContentServiceTestCase.class);
    private static WSRegistryServiceClient registry = null;
    private static Registry governance = null;
    String wsdl_path = "/_system/governance/trunk/wsdls/com/foo/BizService.wsdl";

    @BeforeClass(alwaysRun = true)
    public void init() throws RegistryException, AxisFault {
        registry = TestUtils.getWSRegistry();
        governance = TestUtils.getRegistry();
        TestUtils.cleanupResources(governance);
        deleteWSDL();    //Delete WSDL which exists
    }

    @Test(groups = {"wso2.greg"}, description = "Update WSDL content test", priority = 1)
    public void testUpdateWsdlContent() throws Exception {
        String wsdl_url = "http://svn.wso2.org/repos/wso2/carbon/platform/branches/4.2.0/platform-integration/" +
                "platform-automated-test-suite/1.2.0/org.wso2.carbon.automation.test.repo/src/main/resources/artifacts/GREG/" +
                "wsdl/BizService.wsdl";
//
        WsdlManager wsdlManager = new WsdlManager(governance);
        Wsdl wsdl = null;
        try {
            wsdl = wsdlManager.newWsdl(wsdl_url);
            wsdl.addAttribute("creator", "Aaaa");
            wsdl.addAttribute("version", "0.01");
            wsdlManager.addWsdl(wsdl);
            log.info("wsdl was added");

            Resource r1 = registry.newResource();
            r1 = registry.get(wsdl_path);
            String content = new String((byte[]) r1.getContent());
            //Assert Content location
            assertTrue(content.indexOf("http://localhost:8080/axis2/services/BizService") > 0, "Assert Content -location :");

            //Edit wsdl content
            OMElement contentElement = wsdl.getWsdlElement();
            OMElement addressElement = evaluateXPathToElement("//soap:address", contentElement);
            addressElement.addAttribute("location", "http://my-custom-endpoint/hoooo", null);
            wsdl.setWsdlElement(contentElement);

            // now do an update.
            wsdlManager.updateWsdl(wsdl);

            OMElement contentElement2 = wsdl.getWsdlElement();
            OMElement addressElement2 = evaluateXPathToElement("//soap:address", contentElement2);

            //create new resource
            Resource r2 = registry.newResource();
            r2 = registry.get(wsdl_path);
            String content2 = new String((byte[]) r2.getContent());

            //Assert initial value has been updated properly
            assertFalse(content2.indexOf("http://localhost:8080/axis2/services/BizService") > 0, "Assert Content wsdl file - key word 1");
            assertEquals("http://my-custom-endpoint/hoooo", addressElement2.getAttributeValue(new QName("location")));

            registry.delete(wsdl_path);                                            //delete resource
            assertFalse(registry.resourceExists(wsdl_path), "WSDL Deleted");         //Assert resource does not exists any more
            log.info("WsdlUpadateContentServiceTestClient testUpdateWsdlContent() - Passed");
        } catch (GovernanceException e) {
            log.error("Failed to update WSDL :" + e);
            throw new GovernanceException("Failed to update WSDL :" + e);
        } catch (org.wso2.carbon.registry.api.RegistryException e) {
            log.error("Failed to update WSDL :" + e);
            throw new org.wso2.carbon.registry.api.RegistryException("Failed to update WSDL :" + e);
        } catch (Exception e) {
            log.error("Failed to update WSDL :" + e);
            throw new Exception("Failed to update WSDL :" + e);
        }
    }


    public void deleteWSDL() throws RegistryException {
        try {
            if (registry.resourceExists("/_system/governance/trunk/wsdls")) {
                registry.delete("/_system/governance/trunk/wsdls");
            }
        } catch (RegistryException e) {
            log.error("Failed to Delete WSDL:" + e);
            throw new RegistryException("Failed to Delete WSDL:" + e);
        }
    }


    private static OMElement evaluateXPathToElement(String expression,
                                                    OMElement root) throws Exception {
        List<OMElement> nodes = GovernanceUtils.evaluateXPathToElements(expression, root);
        if (nodes == null || nodes.size() == 0) {
            return null;
        }
        return nodes.get(0);
    }
}
