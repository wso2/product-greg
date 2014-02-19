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
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.policies.PolicyManager;
import org.wso2.carbon.governance.api.policies.dataobjects.Policy;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import java.net.MalformedURLException;
import java.rmi.RemoteException;

import static org.testng.Assert.*;

public class PolicyPropertiesVerificationTestCase {

    private Registry governanceRegistry;
    private Policy policy;
    private PolicyManager policyManager;

    @BeforeClass(groups = "wso2.greg", alwaysRun = true)
    public void initialize() throws RemoteException,
            LoginAuthenticationExceptionException,
            org.wso2.carbon.registry.api.RegistryException {
        int userId = 2;

        RegistryProviderUtil provider = new RegistryProviderUtil();
        WSRegistryServiceClient wsRegistry = provider.getWSRegistry(userId,
                ProductConstant.GREG_SERVER_NAME);

        governanceRegistry = provider.getGovernanceRegistry(wsRegistry, userId);
        policyManager = new PolicyManager(governanceRegistry);

    }

    /**
     * verifying property
     */
    @Test(groups = "wso2.greg", description = "verify properties of Policy")
    public void testPropertiesPolicy() throws RemoteException,
            ResourceAdminServiceExceptionException,
            GovernanceException,
            MalformedURLException {


        policy = policyManager
                .newPolicy("https://svn.wso2.org/repos/wso2/carbon/platform/trunk/"
                        + "platform-integration/platform-automated-test-suite/"
                        + "org.wso2.carbon.automation.test.repo/src/main/"
                        + "resources/artifacts/GREG/policy/policy.xml");

        policy.addAttribute("version", "1.0.0");
        policy.addAttribute("author", "Kanarupan");
        policy.addAttribute("description", "Policy added for property checking");
        policyManager.addPolicy(policy);

        // Properties Verification
        assertFalse(policy.getId().isEmpty());
        assertNotNull(policy);

        assertTrue(policy.getAttribute("author").contentEquals("Kanarupan"));
        assertTrue(policy.getAttribute("version").contentEquals("1.0.0"));
        assertTrue(policy.getAttribute("description").contentEquals(
                "Policy added for property checking"));

        policy.setAttribute("author", "Kanarupan");
        policy.setAttribute("description", "this is to verify property edition");

        policyManager.updatePolicy(policy);

        assertTrue(policy.getAttribute("author").contentEquals("Kanarupan"));
        assertTrue(policy.getAttribute("version").contentEquals("1.0.0"));
        assertTrue(policy.getAttribute("description").contentEquals(
                "this is to verify property edition"));

    }

    @AfterClass(groups = "wso2.greg", alwaysRun = true, description = "cleaning up the artifacts added")
    public void tearDown() throws GovernanceException {
        policyManager.removePolicy(policy.getId());
        policy = null;
        policyManager = null;
        governanceRegistry = null;


    }

}
