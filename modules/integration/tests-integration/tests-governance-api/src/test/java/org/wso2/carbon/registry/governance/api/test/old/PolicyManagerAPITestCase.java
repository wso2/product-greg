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

package org.wso2.carbon.registry.governance.api.test.old;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.policies.PolicyFilter;
import org.wso2.carbon.governance.api.policies.PolicyManager;
import org.wso2.carbon.governance.api.policies.dataobjects.Policy;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import static org.testng.Assert.assertTrue;

/**
 * Class will test policy manager related API methods
 */
public class PolicyManagerAPITestCase {

    private Registry governance;
    private WSRegistryServiceClient wsRegistry;
    private RegistryProviderUtil registryProviderUtil;

    public static PolicyManager policyManager;
    public static Policy policyObj;
    public static Policy[] policies;
    private String policyName = "UTPolicy.xml";

    @BeforeClass(alwaysRun = true)
    public void initializeAPIObject() throws Exception {
        AutomationContext context = new AutomationContext("GREG", TestUserMode.SUPER_TENANT_ADMIN);
        registryProviderUtil = new RegistryProviderUtil();
        wsRegistry = registryProviderUtil.getWSRegistry(context);
        governance = registryProviderUtil.getGovernanceRegistry(wsRegistry, context);
        policyManager = new PolicyManager(governance);
    }

    @Test(groups = {"wso2.greg.api"}, description = "Testing newPolicy API method")
    public void testNewPolicy() throws GovernanceException {
        try {
            policyObj = policyManager.newPolicy("https://raw.githubusercontent.com/wso2/wso2-qa-artifacts/master/automation-artifacts/greg/policy/UTPolicy.xml");
        } catch (GovernanceException e) {
            throw new GovernanceException("Error occurred while executing PolicyManager:newPolicy method" + e);
        }
    }

    @Test(groups = {"wso2.greg.api"}, dependsOnMethods = {"testNewPolicy"}, description = "Testing " +
            "addPolicy API method")
    public void testAddPolicy() throws GovernanceException {
        try {
            cleanPolicies();
            policyManager.addPolicy(policyObj);
        } catch (GovernanceException e) {
            throw new GovernanceException("Error occurred while executing PolicyManager:addPolicy method" + e);
        }
    }


    @Test(groups = {"wso2.greg.api"}, dependsOnMethods = {"testAddPolicy"}, description = "Testing " +
            "getAllPolicies API method")
    public void testGetAllPolicies() throws GovernanceException {
        try {
            policies = policyManager.getAllPolicies();
            assertTrue(policies.length > 0, "Error occurred while executing PolicyManager:" +
                    "getAllPolicies method");
        } catch (GovernanceException e) {
            throw new GovernanceException("Error occurred while executing SchemaManager:PolicyManager method" + e);
        }
    }

    @Test(groups = {"wso2.greg.api"}, dependsOnMethods = {"testGetAllPolicies"}, description = "Testing " +
            "getPolicy API method")
    public void testGetPolicy() throws GovernanceException {
        try {
            policyObj = policyManager.getPolicy(policies[0].getId());
            assertTrue(policyObj.getQName().getLocalPart().equalsIgnoreCase(policyName), "PolicyManager:" +
                    "getPolicy API method not contain expected policy name");
        } catch (GovernanceException e) {
            throw new GovernanceException("Error occurred while executing PolicyManager:getPolicy method" + e);
        }
    }

    @Test(groups = {"wso2.greg.api"}, description = "Testing FindPolicy", dependsOnMethods = "testGetPolicy")
    public void testFindService() throws GovernanceException {
        System.out.println("test");
        try {
            Policy[] policyArray = policyManager.findPolicies(new PolicyFilter() {
                public boolean matches(Policy policy) throws GovernanceException {
                    String name = policy.getQName().getLocalPart();
                    assertTrue(name.contains(policyName), "Error occurred while executing findPolicy API method");
                    return name.contains(policyName);
                }
            });
            assertTrue(policyArray.length > 0, "Error occurred while executing findPolicies API method");
        } catch (GovernanceException e) {
            throw new GovernanceException("Error occurred while executing WsdlManager:findPolicies method" + e);
        }
    }

    @AfterClass
    public void cleanArtifacts() throws RegistryException {
        for (String string : new String[]{"/trunk", "/branches", "/departments", "/organizations",
                "/project-groups", "/people", "/applications", "/processes", "/projects",
                "/test_suites", "/test_cases", "/test_harnesses", "/test_methods"}) {

            if (governance.resourceExists(string)) {
                governance.delete(string);
            }
        }

        governance = null;
        wsRegistry = null;
        registryProviderUtil = null;
        policyManager = null;

    }

//    @Test(groups = {"wso2.greg.api"}, description = "Testing " +
//            "updatePolicy API method", dependsOnMethods = "testFindService")
//    public void testUpdatePolicy() throws GovernanceException {
//        try {
//            policyObj.setName("SamplePolicy");
//            policyManager.updatePolicy(policyObj);
//            Policy localPolicy = policyManager.getPolicy(policyObj.getId());
//            assertTrue(localPolicy.getQName().getLocalPart().equalsIgnoreCase("SamplePolicy.xml"), "Updated policy doesn't " +
//                    "have Updated Information.PolicyManager:updatePolicy didn't work");
//        } catch (GovernanceException e) {
//            throw new GovernanceException("Error occurred while executing PolicyManager:updatePolicy method" + e);
//        }
//    }

//    @Test(groups = {"wso2.greg.api"}, description = "Testing " +
//            "removePolicy API method", dependsOnMethods = "testUpdatePolicy")
//    public void testRemovePolicy() throws GovernanceException {
//        try {
//            policyManager.removePolicy(policyObj.getId());
//            policies = policyManager.getAllPolicies();
//            for (Policy s : policies) {
//                assertTrue(s.getId().equalsIgnoreCase(policyObj.getId()), "PolicyManager:removePolicy API method having error");
//            }
//
//        } catch (GovernanceException e) {
//            throw new GovernanceException("Error occurred while executing PolicyManager:removePolicy method" + e);
//        }
//    }

    private void cleanPolicies() throws GovernanceException {
        policies = policyManager.getAllPolicies();
        for (int i = 0; i <= policies.length - 1; i++) {
            if (policies[i].getQName().getLocalPart().contains("UTPolicy.xml")) {
                policyManager.removePolicy(policies[i].getId());
            }
        }
    }

}
