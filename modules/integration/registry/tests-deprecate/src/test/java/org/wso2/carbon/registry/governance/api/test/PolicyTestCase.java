/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.governance.api.policies.PolicyManager;
import org.wso2.carbon.governance.api.policies.dataobjects.Policy;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class PolicyTestCase {

    private Registry registry;

    @BeforeClass(groups = {"wso2.greg"})
    public void initTest() {
        registry = TestUtils.getRegistry();
        try {
            TestUtils.cleanupResources(registry);
        } catch (RegistryException e) {
            e.printStackTrace();
            Assert.fail("Unable to run Governance API tests: " + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"})
    public void testAddPolicy() throws Exception {
        PolicyManager policyManager = new PolicyManager(registry);

        Policy policy = policyManager.newPolicy("http://svn.wso2.org/repos/wso2/carbon/platform/trunk/components/governance/org.wso2.carbon.governance.api/src/test/resources/test-resources/policy/policy.xml");
        policy.addAttribute("creator", "it is me");
        policy.addAttribute("version", "0.01");
        policyManager.addPolicy(policy);

        Policy newPolicy = policyManager.getPolicy(policy.getId());
        Assert.assertEquals(newPolicy.getPolicyContent(), policy.getPolicyContent());
        Assert.assertEquals(newPolicy.getAttribute("creator"), "it is me");
        Assert.assertEquals(newPolicy.getAttribute("version"), "0.01");

        // change the target namespace and check
        String oldPolicyPath = newPolicy.getPath();
        Assert.assertEquals(oldPolicyPath, "/trunk/policies/policy.xml");
        Assert.assertTrue(registry.resourceExists("/trunk/policies/policy.xml"));

//        newPolicy.setName("my-policy.xml");
//        policyManager.updatePolicy(newPolicy);
//
//        Assert.assertEquals(newPolicy.getPath(), "/trunk/policies/my-policy.xml");
//        Assert.assertFalse(registry.resourceExists("/trunk/policies/policy.xml"));
//
//        // doing an update without changing anything.
//        policyManager.updatePolicy(newPolicy);
//
//        Assert.assertEquals(newPolicy.getPath(), "/trunk/policies/my-policy.xml");
//        Assert.assertEquals(newPolicy.getAttribute("version"), "0.01");
//
//        newPolicy = policyManager.getPolicy(policy.getId());
//        Assert.assertEquals(newPolicy.getAttribute("creator"), "it is me");
//        Assert.assertEquals(newPolicy.getAttribute("version"), "0.01");
//
//        Policy[] policies = policyManager.findPolicies(new PolicyFilter() {
//            public boolean matches(Policy policy) throws GovernanceException {
//                return policy.getAttribute("version") != null && policy.getAttribute("version").equals("0.01");
//            }
//        });
//        Assert.assertEquals(policies.length, 1);
//        Assert.assertEquals(newPolicy.getId(), policies[0].getId());
//
//        // deleting the policy
//        policyManager.removePolicy(newPolicy.getId());
//        Policy deletedPolicy = policyManager.getPolicy(newPolicy.getId());
//        Assert.assertNull(deletedPolicy);
    }

    @Test(groups = {"wso2.greg"})
    public void testAddPolicyFromContent() throws Exception {
        PolicyManager policyManager = new PolicyManager(registry);
        byte[] bytes = null;
        try {
            InputStream inputStream = new URL("http://svn.wso2.org/repos/wso2/carbon/platform/trunk/components/governance/org.wso2.carbon.governance.api/src/test/resources/test-resources/policy/policy.xml").openStream();
            try {
                bytes = IOUtils.toByteArray(inputStream);
            } finally {
                inputStream.close();
            }
        } catch (IOException e) {
            Assert.fail("Unable to read WSDL content");
        }
        Policy policy = policyManager.newPolicy(bytes, "newPolicy.xml");
        policy.addAttribute("creator", "it is me");
        policy.addAttribute("version", "0.01");
        policyManager.addPolicy(policy);

        Policy newPolicy = policyManager.getPolicy(policy.getId());
        Assert.assertEquals(policy.getPolicyContent(), newPolicy.getPolicyContent());
        Assert.assertEquals("it is me", newPolicy.getAttribute("creator"));
        Assert.assertEquals("0.01", newPolicy.getAttribute("version"));

        // change the target namespace and check
        String oldPolicyPath = newPolicy.getPath();
        Assert.assertEquals(oldPolicyPath, "/trunk/policies/newPolicy.xml");
        Assert.assertTrue(registry.resourceExists("/trunk/policies/newPolicy.xml"));
        policyManager.removePolicy(newPolicy.getId());
        Assert.assertFalse(registry.resourceExists(newPolicy.getPath()),"Policy doesn't delete");
    }

    @Test(groups = {"wso2.greg"})
    public void testAddPolicyFromContentNoName() throws Exception {
        PolicyManager policyManager = new PolicyManager(registry);
        byte[] bytes = null;
        try {
            InputStream inputStream = new URL("http://svn.wso2.org/repos/wso2/carbon/platform/trunk/components/governance/org.wso2.carbon.governance.api/src/test/resources/test-resources/policy/policy.xml").openStream();
            try {
                bytes = IOUtils.toByteArray(inputStream);
            } finally {
                inputStream.close();
            }
        } catch (IOException e) {
            Assert.fail("Unable to read WSDL content");
        }
        Policy policy = policyManager.newPolicy(bytes);
        policy.addAttribute("creator", "it is me");
        policy.addAttribute("version", "0.01");
        policyManager.addPolicy(policy);

        Policy newPolicy = policyManager.getPolicy(policy.getId());
        Assert.assertEquals(policy.getPolicyContent(), newPolicy.getPolicyContent());
        Assert.assertEquals("it is me", newPolicy.getAttribute("creator"));
        Assert.assertEquals("0.01", newPolicy.getAttribute("version"));

        // change the target namespace and check
        String oldPolicyPath = newPolicy.getPath();
        Assert.assertEquals(oldPolicyPath, "/trunk/policies/"+ policy.getId() + ".xml");
        Assert.assertTrue(registry.resourceExists("/trunk/policies/"+ policy.getId() + ".xml"));
        policyManager.removePolicy(policy.getId());
        Assert.assertFalse(registry.resourceExists(policy.getPath()) ,"Policy doesn't delete");
    }
}
