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

package org.wso2.carbon.registry.jira2.issues.test2;

import org.apache.axiom.om.OMElement;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import java.io.IOException;

public class Registry633TestCase extends GREGIntegrationBaseTest {
    private WsdlManager wsdlManager;
    private WSRegistryServiceClient wsRegistry;
    private final static String WSDL_URL =
            "https://svn.wso2.org/repos/wso2/carbon/platform/trunk/platform-integration/" +
                    "platform-automated-test-suite/org.wso2.carbon.automation.test.repo/src/main/resources/artifacts" +
                    "/GREG/wsdl/Imports_with_imports.wsdl";

    private Wsdl wsdl, newWsdl;

    @BeforeClass(groups = {"wso2.greg"})
    public void initialize() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String session = getSessionCookie();
        wsRegistry =
                new RegistryProviderUtil().getWSRegistry(automationContext);

        Registry governance = new RegistryProviderUtil().getGovernanceRegistry(wsRegistry, automationContext);
        wsdlManager = new WsdlManager(governance);
    }

    /**
     * This adds a wsdl If a wsdl contains imported resources and those imported
     * resources depend on another resources
     *
     * @throws IOException
     * @throws RegistryException
     */
    @Test(groups = {"wso2.greg"}, description = "Add wsdl with imports")
    public void testAddWsdl() throws IOException, RegistryException {
        wsdl = wsdlManager.newWsdl(WSDL_URL);
        wsdl.addAttribute("test-att", "test-val");
        wsdlManager.addWsdl(wsdl);
        wsdlManager.updateWsdl(wsdl);
        newWsdl = wsdlManager.getWsdl(wsdl.getId());

        OMElement wsdlElement = newWsdl.getWsdlElement();
        wsdlElement.addAttribute("targetNamespace", "http://ww2.wso2.org/test", null);
        wsdlElement.declareNamespace("http://ww2.wso2.org/test", "tns");
        wsdlManager.updateWsdl(newWsdl);

        Assert.assertEquals(newWsdl.getPath(),
                            "/trunk/wsdls/org/wso2/ww2/test/Imports_with_imports.wsdl");
    }

    @AfterClass
    public void cleanup() throws RegistryException {
        wsdlManager.removeWsdl(newWsdl.getId());
        wsRegistry.delete("/_system/governance/trunk/wsdls/org/wso2/carbon/service/Axis2ImportedWsdl.wsdl");
        wsRegistry.delete("/_system/governance/trunk/wsdls/org/wso2/carbon/service/impl");
        wsRegistry.delete("/_system/governance/trunk/services/org/wso2/carbon/service/impl/Axis2Service");
        wsRegistry.delete("/_system/governance/trunk/services/org/wso2/ww2/test/Axis2Service");
        wsdlManager = null;
    }
}
