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
package org.wso2.carbon.registry.governance.api.test;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.schema.dataobjects.Schema;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.api.wsdls.WsdlFilter;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import javax.xml.namespace.QName;
import java.util.List;

public class WSDLTestCase {

    private Registry registry;
    private static final Log log = LogFactory.getLog(WSDLTestCase.class);

    @BeforeClass(groups = {"wso2.greg"})
    public void initTest() {
        registry = TestUtils.getRegistry();
        try {
            TestUtils.cleanupResources(registry);
        } catch (RegistryException e) {
            e.printStackTrace();
            Assert.fail("Unable to run Governance API tests: " + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"})
    public void testAddWSDL() throws Exception {
        log.info("############## testAddWSDL started ...###################");
        WsdlManager wsdlManager = new WsdlManager(registry);

        Wsdl wsdl = wsdlManager.newWsdl("http://svn.wso2.org/repos/wso2/carbon/platform/trunk/components/governance/org.wso2.carbon.governance.api/src/test/resources/test-resources/wsdl/BizService.wsdl");
        wsdl.addAttribute("creator", "it is me");
        wsdl.addAttribute("version", "0.01");
        wsdlManager.addWsdl(wsdl);
        doSleep();
        Wsdl newWsdl = wsdlManager.getWsdl(wsdl.getId());
        Assert.assertEquals(newWsdl.getWsdlElement().toString(), wsdl.getWsdlElement().toString());
        Assert.assertEquals(newWsdl.getAttribute("creator"), "it is me");
        Assert.assertEquals(newWsdl.getAttribute("version"), "0.01");

        // change the target namespace and check
        String oldWSDLPath = newWsdl.getPath();
        Assert.assertEquals(oldWSDLPath, "/trunk/wsdls/com/foo/BizService.wsdl");
        Assert.assertTrue(registry.resourceExists("/trunk/wsdls/com/foo/BizService.wsdl"));

        OMElement wsdlElement = newWsdl.getWsdlElement();
        wsdlElement.addAttribute("targetNamespace", "http://ww2.wso2.org/test", null);
        wsdlElement.declareNamespace("http://ww2.wso2.org/test", "tns");
        wsdlManager.updateWsdl(newWsdl);
        doSleep();

        Assert.assertEquals(newWsdl.getPath(), "/trunk/wsdls/org/wso2/ww2/test/BizService.wsdl");
        //assertFalse(registry.resourceExists("/wsdls/http/foo/com/BizService.wsdl"));

        // doing an update without changing anything.
        wsdlManager.updateWsdl(newWsdl);
        doSleep();

        Assert.assertEquals(newWsdl.getPath(), "/trunk/wsdls/org/wso2/ww2/test/BizService.wsdl");
        Assert.assertEquals(newWsdl.getAttribute("version"), "0.01");

        newWsdl = wsdlManager.getWsdl(wsdl.getId());
        Assert.assertEquals(newWsdl.getAttribute("creator"), "it is me");
        Assert.assertEquals(newWsdl.getAttribute("version"), "0.01");

        // adding a new schema to the wsdl.
        wsdlElement = newWsdl.getWsdlElement();
        OMElement schemaElement = evaluateXPathToElement("//xsd:schema", wsdlElement);

        OMFactory factory = OMAbstractFactory.getOMFactory();

        OMElement importElement = factory.createOMElement(
                new QName("http://www.w3.org/2001/XMLSchema", "import"));
        importElement.addAttribute("schemaLocation",
                "http://svn.wso2.org/repos/wso2/carbon/platform/trunk/components/governance/org.wso2.carbon.governance.api/src/test/resources/test-resources/xsd/purchasing_dup.xsd", null);
        schemaElement.addChild(importElement);
        importElement.addAttribute("namespace", "http://bar.org/purchasing_dup", null);

        wsdlManager.updateWsdl(newWsdl);
        doSleep();

        Schema[] schemas = newWsdl.getAttachedSchemas();

       //test log
        log.info("####Schemas#####");
        for(Schema schema:schemas){
            log.info("#####Schema:"+schemas[0].getId() + " schemaName" + schema.getQName().toString());
        }

        Assert.assertEquals(schemas[schemas.length - 1].getPath(), "/trunk/schemas/org/bar/purchasing_dup/purchasing_dup.xsd");


        Wsdl[] wsdls = wsdlManager.findWsdls(new WsdlFilter() {
            public boolean matches(Wsdl wsdl) throws GovernanceException {
                Schema[] schemas = wsdl.getAttachedSchemas();
                for (Schema schema : schemas) {
                    if (schema.getPath().equals("/trunk/schemas/org/bar/purchasing_dup/purchasing_dup.xsd")) {
                        log.info("###### Matching Schemas name"+ schema.getQName().toString() + "  schemaID:"+schema.getId());
                        return true;
                    }
                }
                return false;
            }
        });
        log.info("WSDL len:"+wsdls.length);
        Assert.assertEquals(wsdls.length, 1);
        Assert.assertEquals(newWsdl.getId(), wsdls[0].getId());

        // deleting the wsdl
        wsdlManager.removeWsdl(newWsdl.getId());
        Wsdl deletedWsdl = wsdlManager.getWsdl(newWsdl.getId());
        doSleep();
        Assert.assertNull(deletedWsdl);

        // add again
        Wsdl anotherWsdl = wsdlManager.newWsdl("http://svn.wso2.org/repos/wso2/carbon/platform/trunk/components/governance/org.wso2.carbon.governance.api/src/test/resources/test-resources/wsdl/BizService.wsdl");
        anotherWsdl.addAttribute("creator", "it is not me");
        anotherWsdl.addAttribute("version", "0.02");
        wsdlManager.addWsdl(anotherWsdl);

        // and delete the wsdl
        wsdlManager.removeWsdl(anotherWsdl.getId());
        Assert.assertNull(wsdlManager.getWsdl(anotherWsdl.getId()));

    }

    @Test(groups = {"wso2.greg"} , dependsOnMethods = {"testAddWSDL"})
    public void testEditWSDL() throws Exception {
        initTest();

        WsdlManager wsdlManager = new WsdlManager(registry);

        Wsdl wsdl = wsdlManager.newWsdl("http://svn.wso2.org/repos/wso2/carbon/platform/trunk/components/governance/org.wso2.carbon.governance.api/src/test/resources/test-resources/wsdl/BizService.wsdl");
        wsdl.addAttribute("creator2", "it is me");
        wsdl.addAttribute("version2", "0.01");
        wsdlManager.addWsdl(wsdl);

        // now edit the wsdl
        OMElement contentElement = wsdl.getWsdlElement();
        OMElement addressElement = evaluateXPathToElement("//soap:address", contentElement);
        addressElement.addAttribute("location", "http://my-custom-endpoint/hoooo", null);
        wsdl.setWsdlElement(contentElement);

        // now do an update.
        wsdlManager.updateWsdl(wsdl);
        doSleep();
        // now get the wsdl and check the update is there.
        Wsdl wsdl2 = wsdlManager.getWsdl(wsdl.getId());
        Assert.assertEquals(wsdl2.getAttribute("creator2"), "it is me");
        Assert.assertEquals(wsdl2.getAttribute("version2"), "0.01");
        OMElement contentElement2 = wsdl.getWsdlElement();
        OMElement addressElement2 = evaluateXPathToElement("//soap:address", contentElement2);

        Assert.assertEquals(addressElement2.getAttributeValue(new QName("location")), "http://my-custom-endpoint/hoooo");
    }

    private static OMElement evaluateXPathToElement(String expression,
                                                    OMElement root) throws Exception {
        List<OMElement> nodes = GovernanceUtils.evaluateXPathToElements(expression, root);
        if (nodes == null || nodes.size() == 0) {
            return null;
        }
        return nodes.get(0);
    }

    private void doSleep(){
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {

        }
    }

}
