///*
//* Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//*
//* Licensed under the Apache License, Version 2.0 (the "License");
//* you may not use this file except in compliance with the License.
//* You may obtain a copy of the License at
//*
//*      http://www.apache.org/licenses/LICENSE-2.0
//*
//* Unless required by applicable law or agreed to in writing, software
//* distributed under the License is distributed on an "AS IS" BASIS,
//* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//* See the License for the specific language governing permissions and
//* limitations under the License.
//*/
//
//package org.wso2.carbon.registry.governance.api.test;
//
//import org.apache.axis2.AxisFault;
//import org.testng.Assert;
//import org.testng.annotations.BeforeClass;
//import org.testng.annotations.Test;
//import org.wso2.carbon.governance.api.exception.GovernanceException;
//import org.wso2.carbon.governance.api.policies.PolicyFilter;
//import org.wso2.carbon.governance.api.policies.PolicyManager;
//import org.wso2.carbon.governance.api.policies.dataobjects.Policy;
//import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
//import org.wso2.carbon.registry.core.Registry;
//import org.wso2.carbon.registry.core.exceptions.RegistryException;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//
//import static org.testng.Assert.assertNull;
//import static org.testng.Assert.assertTrue;
//
//public class PolicyClientTestCase {
//
//    private Registry governance;
//
//    @BeforeClass(groups = {"wso2.greg"})
//    public void initTest() throws RegistryException, AxisFault {
//        governance = TestUtils.getRegistry();
//        TestUtils.cleanupResources(governance);
//    }
//
//    @Test(groups = {"wso2.greg"})
//    public void testAddPolicy() throws Exception {
//        PolicyManager policyManager = new PolicyManager(governance);
//
//        Policy policy = policyManager.newPolicy("http://svn.wso2.org/repos/wso2/trunk/graphite/" +
//                                                "components/governance/org.wso2.carbon.governance.api" +
//                                                "/src/test/resources/test-resources/policy/policy.xml");
//        policy.addAttribute("creator", "it is me");
//        policy.addAttribute("version", "0.01");
//        policyManager.addPolicy(policy);
//
//        Policy newPolicy = policyManager.getPolicy(policy.getId());
//        Assert.assertEquals(newPolicy.getPolicyContent(), policy.getPolicyContent());
//        Assert.assertEquals(newPolicy.getAttribute("creator"), "it is me");
//        Assert.assertEquals(newPolicy.getAttribute("version"), "0.01");
//
//        // change the target namespace and check
//        String oldPolicyPath = newPolicy.getPath();
//        Assert.assertEquals(oldPolicyPath, "/trunk/policies/policy.xml");
//        Assert.assertTrue(governance.resourceExists("/trunk/policies/policy.xml"));
//
//        newPolicy.setName("my-policy.xml");
//        policyManager.updatePolicy(newPolicy);
//
//        Assert.assertEquals(newPolicy.getPath(), "/trunk/policies/my-policy.xml");
//        Assert.assertFalse(governance.resourceExists("/trunk/policies/policy.xml"));
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
//    }
//
//    @Test(groups = {"wso2.greg"}, description = "Adding duplicate policy")
//    public void testAddDuplicatePolicy() throws GovernanceException {
//        PolicyManager policyManager = new PolicyManager(governance);
//
//        Policy policy = policyManager.newPolicy("http://svn.wso2.org/repos/wso2/carbon/platform/trunk" +
//                                                "/products/greg/modules/integration/registry/tests/src" +
//                                                "/test/java/resources/policy/UTPolicy.xml");
//
//        policy.setName("WSO2AutomationUTPolicyDuplicate.xml");
//        policyManager.addPolicy(policy);
//        assertTrue(policy.getQName().toString().contains("WSO2AutomationUTPolicyDuplicate.xml"));
//
//        Policy policyDuplicate = policyManager.newPolicy("http://svn.wso2.org/repos/wso2/carbon/" +
//                                                         "platform/trunk/products/greg/modules/integration" +
//                                                         "/registry/tests/src/test/java/resources/" +
//                                                         "policy/UTPolicy.xml");
//
//        policyDuplicate.setName("WSO2AutomationUTPolicyDuplicate.xml");
//        policyManager.addPolicy(policyDuplicate);
//
//        assertTrue(policyDuplicate.getQName().toString().contains("WSO2AutomationUTPolicyDuplicate.xml"));
//        policyManager.removePolicy(policyDuplicate.getId());
//        assertNull(policyManager.getPolicy(policyDuplicate.getId()));
//    }
//
//    @Test(groups = {"wso2.greg"}, description = "Adding invalid policy")
//    public void testAddInvalidPolicy() throws GovernanceException {
//        PolicyManager policyManager = new PolicyManager(governance);
//
//        Policy policy = policyManager.newPolicy("http://svn.wso2.org/repos/wso2/carbon/platform/trunk/" +
//                                                "products/greg/modules/integration/registry/tests/src/" +
//                                                "test/java/resources/policy/invlidPolicySyntax.xml");
//
//        policy.setName("invalidPolicy.xml");
//        policyManager.addPolicy(policy);
//        assertTrue(policy.getQName().toString().contains("invalidPolicy.xml"));
//        policyManager.removePolicy(policy.getId());
//        assertNull(policyManager.getPolicy(policy.getId()));
//    }
//
//    @Test(groups = {"wso2.greg"}, description = "Update policy content")
//    public void testUpdatePolicy() throws GovernanceException, IOException {
//        PolicyManager policyManager = new PolicyManager(governance);
//        String updatedPolicyPath = FrameworkSettings.getFrameworkPath() + File.separator + ".." + File.separator + ".." + File.separator + ".."
//                                   + File.separator + "src" + File.separator + "test" + File.separator + "java" + File.separator
//                                   + "resources" + File.separator + "policy" + File.separator + "UTPolicy-updated.xml";
//
//        Policy policy = policyManager.newPolicy("http://svn.wso2.org/repos/wso2/carbon/platform/trunk/" +
//                                                "products/greg/modules/integration/registry/tests/src" +
//                                                "/test/java/resources/policy/UTPolicy.xml");
//
//        policy.setName("NewPolicyAutomation.xml");
//        policyManager.addPolicy(policy);
//        assertTrue(policy.getQName().toString().contains("NewPolicyAutomation.xml"));
//
//
//        File newPolicyFile = new File(updatedPolicyPath);
//        StringBuilder strContent = new StringBuilder("");
//
//        if (newPolicyFile.exists()) {
//            FileInputStream inputStream = new FileInputStream(newPolicyFile);
//
//            int character;
//            while ((character = inputStream.read()) != -1) {
//                strContent.append((char) character);
//            }
//            inputStream.close();
//        }
//        policy.setPolicyContent(strContent.toString());
//        policyManager.updatePolicy(policy);
//
//        assertTrue(policy.getQName().toString().contains("NewPolicyAutomation.xml"));
//        assertTrue(policyManager.getPolicy(policy.getId()).getPolicyContent().contains("PolicyUpdatedForAutomationTest"),
//                   "Updated content not found");
//
//        policyManager.removePolicy(policy.getId());
//        assertNull(policyManager.getPolicy(policy.getId()));
//    }
//}
