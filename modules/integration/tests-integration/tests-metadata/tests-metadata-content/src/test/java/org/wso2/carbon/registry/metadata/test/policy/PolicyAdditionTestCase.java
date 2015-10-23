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
package org.wso2.carbon.registry.metadata.test.policy;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.automation.test.utils.common.FileManager;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.policies.PolicyManager;
import org.wso2.carbon.governance.api.policies.dataobjects.Policy;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;

import static org.testng.Assert.*;

public class PolicyAdditionTestCase extends GREGIntegrationBaseTest {
    private Registry governanceRegistry;
    private PolicyManager policyManager;
    private Policy policy;
    private Policy policyViaUrl;

    @BeforeClass(groups = "wso2.greg", alwaysRun = true)
    public void initialize() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_USER);
        RegistryProviderUtil provider = new RegistryProviderUtil();
        WSRegistryServiceClient wsRegistry = provider.getWSRegistry(automationContext);
        governanceRegistry = provider.getGovernanceRegistry(wsRegistry, automationContext);
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governanceRegistry);
        policyManager = new PolicyManager(governanceRegistry);


    }

    /* add a Policy from file system */
    @Test(groups = "wso2.greg", description = "Add Policy from file system")
    public void testAddPolicyFromFileSystem() throws IOException,

                                                     ResourceAdminServiceExceptionException,
                                                     RegistryException {
        String policyPath = FrameworkPathUtil.getSystemResourceLocation()
                            + "artifacts" + File.separator + "GREG" + File.separator
                            + "policy" + File.separator + "policy.xml";


        policy = policyManager.newPolicy(FileManager.readFile(policyPath)
                                                 .getBytes());
        policy.addAttribute("version", "2.0.0");
        policyManager.addPolicy(policy);
        policy.addAttribute("author", "Kana");
        policy.addAttribute("description",
                            "Policy addtion via file system verification");

        policyManager.updatePolicy(policy);

        assertNotNull(policy);
        assertTrue(policy.getAttribute("author").contentEquals("Kana"));
        assertEquals(policy.getAttribute("version"), "2.0.0");
        assertEquals(policy.getAttribute("description"),
                     "Policy addtion via file system verification");

        assertTrue(policy.getPath().contains("/trunk/policies"));
    }

    /* add a Policy via URL */
    @Test(groups = "wso2.greg", description = "Add Policy via URL",
            dependsOnMethods = "testAddPolicyFromFileSystem")
    public void testAddPolicyViaURL() throws IOException,
                                             ResourceAdminServiceExceptionException,
                                             RegistryException {

        policyViaUrl = policyManager
                .newPolicy("https://raw.githubusercontent.com/wso2/wso2-qa-artifacts/master/automation-artifacts/greg" +
                        "/policy/UTPolicy1.xml");
        policyViaUrl.addAttribute("version", "2.0.0");
        policyManager.addPolicy(policyViaUrl);
        policyViaUrl.addAttribute("author", "KanaURL");
        
        policyViaUrl.addAttribute("description", "Policy addition via url");

        policyManager.updatePolicy(policyViaUrl);

        assertNotNull(policyViaUrl);
        assertTrue(policyViaUrl.getAttribute("author").contentEquals("KanaURL"));
        assertEquals(policyViaUrl.getAttribute("version"), "2.0.0");
        assertEquals(policyViaUrl.getAttribute("description"),
                     "Policy addition via url");

        assertTrue(policyViaUrl.getPath().contains(
                "/trunk/policies/2.0.0/UTPolicy1.xml"));
        assertTrue(policyViaUrl.getQName().toString().contains("UTPolicy1.xml"));

    }

    @AfterClass(groups = "wso2.greg", alwaysRun = true, description = "cleaning up the artifacts added")
    public void tearDown() throws GovernanceException {
        policyManager.removePolicy(policy.getId());
        policyManager.removePolicy(policyViaUrl.getId());

        governanceRegistry = null;
        policyManager = null;
        policy = null;
        policyViaUrl = null;
    }

}
