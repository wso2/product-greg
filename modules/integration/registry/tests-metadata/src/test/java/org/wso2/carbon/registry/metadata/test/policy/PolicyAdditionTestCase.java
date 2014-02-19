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
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.fileutils.FileManager;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.policies.PolicyManager;
import org.wso2.carbon.governance.api.policies.dataobjects.Policy;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;

import static org.testng.Assert.*;

public class PolicyAdditionTestCase {
    private Registry governanceRegistry;
    private PolicyManager policyManager;
    private Policy policy;
    private Policy policyViaUrl;

    @BeforeClass(groups = "wso2.greg", alwaysRun = true)
    public void initialize() throws RemoteException,
                                    LoginAuthenticationExceptionException,
                                    org.wso2.carbon.registry.api.RegistryException {
        int userId = 1;
        RegistryProviderUtil provider = new RegistryProviderUtil();
        WSRegistryServiceClient wsRegistry = provider.getWSRegistry(userId,
                                                                    ProductConstant.GREG_SERVER_NAME);
        governanceRegistry = provider.getGovernanceRegistry(wsRegistry, userId);
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governanceRegistry);
        policyManager = new PolicyManager(governanceRegistry);


    }

    /* add a Policy from file system */
    @Test(groups = "wso2.greg", description = "Add Policy from file system")
    public void testAddPolicyFromFileSystem() throws IOException,

                                                     ResourceAdminServiceExceptionException,
                                                     RegistryException {
        String policyPath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION
                            + "artifacts" + File.separator + "GREG" + File.separator
                            + "policy" + File.separator + "policy.xml";


        policy = policyManager.newPolicy(FileManager.readFile(policyPath)
                                                 .getBytes());
        policyManager.addPolicy(policy);
        policy.addAttribute("author", "Kana");
        policy.addAttribute("version", "1.0.0");
        policy.addAttribute("description",
                            "Policy addtion via file system verification");

        policyManager.updatePolicy(policy);

        assertNotNull(policy);
        assertTrue(policy.getAttribute("author").contentEquals("Kana"));
        assertEquals(policy.getAttribute("version"), "1.0.0");
        assertEquals(policy.getAttribute("description"),
                     "Policy addtion via file system verification");

        assertTrue(policy.getPath().contains("/trunk/policies"));
    }

    /* add a Policy via URL */
    @Test(groups = "wso2.greg", description = "Add Policy via URL")
    public void testAddPolicyViaURL() throws IOException,
                                             ResourceAdminServiceExceptionException,
                                             RegistryException {

        policyViaUrl = policyManager
                .newPolicy("https://svn.wso2.org/repos/wso2/carbon/platform/"
                           + "trunk/platform-integration/platform-automated-test-suite/"
                           + "org.wso2.carbon.automation.test.repo/"
                           + "src/main/resources/artifacts/GREG/policy/UTPolicy.xml");
        policyManager.addPolicy(policyViaUrl);
        policyViaUrl.addAttribute("author", "KanaURL");
        policyViaUrl.addAttribute("version", "1.0.0");
        policyViaUrl.addAttribute("description", "Policy addition via url");

        policyManager.updatePolicy(policyViaUrl);

        assertNotNull(policyViaUrl);
        assertTrue(policyViaUrl.getAttribute("author").contentEquals("KanaURL"));
        assertEquals(policyViaUrl.getAttribute("version"), "1.0.0");
        assertEquals(policyViaUrl.getAttribute("description"),
                     "Policy addition via url");

        assertTrue(policyViaUrl.getPath().contains(
                "/trunk/policies/UTPolicy.xml"));
        assertTrue(policyViaUrl.getQName().toString().contains("UTPolicy.xml"));

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
