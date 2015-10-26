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
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import java.net.MalformedURLException;
import java.rmi.RemoteException;

public class DummyWsdlAdditionTestCase extends GREGIntegrationBaseTest{

    private Registry governanceRegistry;

    @BeforeClass(groups = "wso2.greg", alwaysRun = true)
    public void initialize() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_USER);

        RegistryProviderUtil provider = new RegistryProviderUtil();
        WSRegistryServiceClient wsRegistry = provider.getWSRegistry(automationContext);
        governanceRegistry = provider.getGovernanceRegistry(wsRegistry, automationContext);

    }

    /**
     * added an .rxt file as the dummy wsdl:verified the exception using the
     * expectedException
     */
    @Test(groups = "wso2.greg", description = "a .rxt file is given as the dummy wsdl", expectedExceptions = GovernanceException.class)
    public void testAddDummyWsdl() throws RemoteException,
            ResourceAdminServiceExceptionException,
            GovernanceException,
            MalformedURLException {

        WsdlManager wsdlManager = new WsdlManager(governanceRegistry);

        Wsdl wsdl = wsdlManager
                .newWsdl("https://svn.wso2.org/repos/wso2/carbon/platform/trunk/"
                        + "platform-integration/platform-automated-test-suite/org.wso2.carbon.automation.test.repo"
                        + "/src/main/resources/artifacts/GREG/rxt/event.rxt");

        wsdl.addAttribute("version", "1.0.0");
        wsdl.addAttribute("author", "Aparna");
        wsdl.addAttribute("description", "added invalid wsdl using url");
        wsdlManager.addWsdl(wsdl);

    }

    @AfterClass(groups = "wso2.greg", alwaysRun = true, description = "cleaning up memory")
    public void tearDown() throws GovernanceException {
        governanceRegistry = null;

    }

}
