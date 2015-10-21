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

package org.wso2.carbon.registry.soapservice.test.wsdl;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import java.net.MalformedURLException;
import java.rmi.RemoteException;

import static org.testng.Assert.*;

public class SecuredUrlWsdlAdditionTestCase extends GREGIntegrationBaseTest {

    private Registry governanceRegistry;
    private Wsdl wsdl;
    private WsdlManager wsdlManager;
    private WSRegistryServiceClient wsRegistry;
    private String sessionCookie;

    @BeforeClass(groups = "wso2.greg", alwaysRun = true)
    public void initialize() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        sessionCookie = new LoginLogoutClient(automationContext).login();

        RegistryProviderUtil provider = new RegistryProviderUtil();
        wsRegistry = provider.getWSRegistry(automationContext);
        governanceRegistry = provider.getGovernanceRegistry(wsRegistry, automationContext);
        wsdlManager = new WsdlManager(governanceRegistry);


    }

    /**
     * adding a secured wsdl
     *
     * @throws MalformedURLException
     */
    @Test(groups = "wso2.greg", description = "Add secured URL WSDL Axis2Import.wsdl")
    public void testAddSecuredURLWSDL() throws RemoteException,
            ResourceAdminServiceExceptionException,
            GovernanceException,
            MalformedURLException {


        wsdl = wsdlManager
                .newWsdl("https://raw.githubusercontent.com/wso2/wso2-qa-artifacts/master/automation-artifacts/greg" +
                        "/wsdl/IClinicalNotes.wsdl");
        wsdl.addAttribute("version", "1.0.0");
        wsdl.addAttribute("author", "Aparna");
        wsdl.addAttribute("description", "added secured URL wsdl");
        wsdlManager.addWsdl(wsdl);

        assertFalse(wsdl.getId().isEmpty());
        assertNotNull(wsdl);
        assertTrue(wsdl.getAttribute("description").contentEquals(
                "added secured URL wsdl")); // Secured URL WSDL addition from
        // URL: verification
    }

    @AfterClass(groups = "wso2.greg", alwaysRun = true, description = "cleaning up the artifacts added")
    public void tearDown() throws RegistryException {
//
        wsdlManager.removeWsdl(wsdl.getId());     //no associations nor dependencies
        governanceRegistry = null;
        wsdl = null;
        wsdlManager = null;

    }

}
