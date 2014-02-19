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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
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

    private static final Log log = LogFactory.getLog(WSDLWithSpecialCharTestCase.class);
    private static WSRegistryServiceClient registry = null;
    private static Registry governance = null;
    private final static String WSDL_URL = "http://ws.strikeiron.com/donotcall2_5?WSDL";
    private static String resourcePath = "/_system/governance/trunk/wsdls/com/strikeiron/www/donotcall2_5.wsdl";


    @BeforeClass(alwaysRun = true)
    public void init() throws RegistryException, AxisFault {
        registry = TestUtils.getWSRegistry();
        governance = TestUtils.getRegistry();
        TestUtils.cleanupResources(governance);
        if (registry.resourceExists(resourcePath)) { //delete the wsdl if exists
            registry.delete(resourcePath);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "upload wsdl with special characters", priority = 1)
    public void testSpecialCharWSDL() throws RegistryException {
        WsdlManager manager = new WsdlManager(governance);
        Wsdl wsdl = manager.newWsdl(WSDL_URL);
        manager.addWsdl(wsdl);
        assertTrue(registry.resourceExists(resourcePath));
        log.info("Add WSDL was successful");
    }

    @Test(groups = {"wso2.greg"}, description = "Verify wsdl properties", priority = 2,
          dependsOnMethods = "testSpecialCharWSDL")
    public void testVerityWSDLProperties() throws RegistryException {
        WsdlManager manager = new WsdlManager(governance);
        Wsdl[] wsdl = manager.getAllWsdls();
        boolean status = false;
        for (Wsdl wsdlInfo : wsdl) {
            if (wsdlInfo.getQName().toString().contains("donotcall2_5.wsdl")) {
                status = true;
            }
        }
        assertTrue(status, "WSDL name not found in the registry");
        Resource wsdlResource = registry.get(resourcePath);
        assertTrue(new String((byte[]) wsdlResource.getContent()).contains("DoNotCallRegistry"),
                   "Expected wsdl content not found");
        log.info("WSDL verification was successful");
    }

    @AfterClass(groups = {"wso2.greg"}, description = "Remove the added wsdl")
    public void testCleanup() throws RegistryException {
        if (registry.resourceExists(resourcePath)) {
            registry.delete(resourcePath);
        }
    }
}
