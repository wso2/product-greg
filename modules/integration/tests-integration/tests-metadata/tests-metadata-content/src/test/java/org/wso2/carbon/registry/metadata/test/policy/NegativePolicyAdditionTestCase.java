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
 **/

package org.wso2.carbon.registry.metadata.test.policy;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.automation.test.utils.common.FileManager;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.policies.PolicyManager;
import org.wso2.carbon.governance.api.policies.dataobjects.Policy;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;

public class NegativePolicyAdditionTestCase extends GREGIntegrationBaseTest{

    private Registry governanceRegistry;
    private Policy policy;
    private PolicyManager policyManager;

    @BeforeClass(groups = "wso2.greg", alwaysRun = true)
    public void initialize() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_USER);
        RegistryProviderUtil provider = new RegistryProviderUtil();
        WSRegistryServiceClient wsRegistry = provider.getWSRegistry(automationContext);
        governanceRegistry = provider.getGovernanceRegistry(wsRegistry, automationContext);
        policyManager = new PolicyManager(governanceRegistry);

    }

    /**
     * adding an invalid policy no assertions and tear down because policy
     * addition wouldn't happen
     * here the relevant invalid policy is required
     *
     * @throws java.net.MalformedURLException
     * @throws java.rmi.RemoteException
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException
     *
     * @throws org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException
     *
     */
    @Test(groups = "wso2.greg", description = "Add Invalid policy",
            expectedExceptions = org.wso2.carbon.registry.core.exceptions.RegistryException.class)
    public void testInvalidAdditionPolicyViaUrl() throws RemoteException,
                                                         ResourceAdminServiceExceptionException,
                                                         GovernanceException,
                                                         MalformedURLException {


        policy = policyManager
                .newPolicy("https://raw.githubusercontent.com/wso2/wso2-qa-artifacts/master/automation-artifacts/" +
                        "greg/policy/invlidPolicySyntax.xml");

        policy.addAttribute("version", "1.0.0");
        policy.addAttribute("author", "Kana");
        policy.addAttribute("description", "added invalid policy using url");
        policyManager.addPolicy(policy);

    }

    // invalid wsdl form file system
    //, expectedExceptions = NullPointerException.class
    @Test (groups = "wso2.greg", description = "invalid policy from file system using admin services",
            dependsOnMethods = "testInvalidAdditionPolicyViaUrl")
    public void testAddInvalidpolicyFromFileSystem() throws RegistryException,
                                                            IOException {

        // clarity automation api for registry
        String resourcePath = FrameworkPathUtil.getSystemResourceLocation()
                              + "artifacts" + File.separator + "GREG" + File.separator
                              + "policy" + File.separator + "invlidPolicy.xml"; // the
        // path
        policy = policyManager.newPolicy(FileManager.readFile(resourcePath)// NullPointerException
                                                 .getBytes());

    }


    @AfterClass
    public void resourceCleanup() {
        governanceRegistry = null;
        policy = null;
        policyManager = null;
    }

}
