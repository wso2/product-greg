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
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import java.net.MalformedURLException;
import java.rmi.RemoteException;

public class ValidWsdlWithCorruptedPoliciesAdditionTestCase extends GREGIntegrationBaseTest{

    private Registry governanceRegistry;
    private String sessionCookie;

    @BeforeClass(groups = "wso2.greg", alwaysRun = true)
    public void initialize() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        sessionCookie = new LoginLogoutClient(automationContext).login();

        RegistryProviderUtil provider = new RegistryProviderUtil();
        WSRegistryServiceClient wsRegistry = provider.getWSRegistry(automationContext);
        governanceRegistry = provider.getGovernanceRegistry(wsRegistry, automationContext);

    }

    /**
     * adding an valid wsdl with corrupted policies: exception verified via
     * expectedExceptions
     */
    @Test(groups = "wso2.greg", description = "Add valid wsdl with corrupted policies", expectedExceptions = GovernanceException.class)
    public void testAddValidWSDLWithCorruptedPolicies() throws RemoteException,
            GovernanceException,
            MalformedURLException {

        WsdlManager wsdlManager = new WsdlManager(governanceRegistry);

        Wsdl wsdl = wsdlManager
                .newWsdl("https://raw.githubusercontent.com/wso2/wso2-qa-artifacts/master/automation-artifacts/greg/" +
                        "wsdl/wsdlWithCorruptedPolicy.wsdl");

        wsdl.addAttribute("version", "1.0.0");
        wsdl.addAttribute("author", "Aparna");
        wsdl.addAttribute("description",
                "added valid wsdl with corrupted Policy via url");
        wsdlManager.addWsdl(wsdl);

    }

    @AfterClass(groups = "wso2.greg", alwaysRun = true, description = "cleaning up the artifacts added")
    public void tearDown() throws GovernanceException {

        governanceRegistry = null;

    }
}
