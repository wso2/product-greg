/*
* Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.governance.api.test.old;


import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.policies.PolicyManager;
import org.wso2.carbon.governance.api.policies.dataobjects.Policy;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import static org.testng.Assert.*;


public class PolicyImportServiceTestCase {
    private static final Log log = LogFactory.getLog(PolicyImportServiceTestCase.class);
    private WSRegistryServiceClient registry;
    private RegistryProviderUtil registryProviderUtil;
    private Registry governance;
    private int userId = 1;
    UserInfo userInfo = UserListCsvReader.getUserInfo(userId);

    @BeforeClass(alwaysRun = true)
    public void init() throws RegistryException, AxisFault {
        registryProviderUtil = new RegistryProviderUtil();
        registry = registryProviderUtil.getWSRegistry(userId,
                ProductConstant.GREG_SERVER_NAME);
        governance = registryProviderUtil.getGovernanceRegistry(registry, userId);
    }

    @Test(groups = {"wso2.greg"}, description = "Add EncrOnlyAnonymousPolicy sample", priority = 1)
    public void testAddEncrOnlyAnonymousPolicy() throws RegistryException {
        String policy_url = "http://people.wso2.com/~evanthika/policies/EncrOnlyAnonymous.xml";
        String policy_path = "/_system/governance/trunk/policies/EncrOnlyAnonymous.xml";
        String keyword1 = "EncrOnlyAnonymous";
        String keyword2 = "WssX509V3Token10";

        createPolicy(policy_url);  //Add Policy

        try {
            assertTrue(registry.resourceExists(policy_path), "EncrOnlyAnonymous Policy Exists:");    //Assert Policy exists
            propertyAssertion(policy_path);                                                        //Assert Property
            policyContentAssertion(keyword1, keyword2, policy_path);                               //Assert content of Policy
            registry.delete(policy_path);                                                           //Remove Policy
            assertFalse(registry.resourceExists(policy_path), "Policy exists at " + policy_path);     //Assert Resource was deleted successfully
            log.info("PolicyImportServiceTestClient testAddEncrOnlyAnonymousPolicy()-Passed");
        } catch (RegistryException e) {
            log.error("Failed to add EncrOnlyAnonymous Policy " + e);
            throw new RegistryException("Failed to add EncrOnlyAnonymous Policy:" + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Add SecurePartnerService01 sample", priority = 2)
    public void testAddPolicySecurePartnerService01() throws RegistryException {
        String policy_url = "http://people.wso2.com/~evanthika/policies/PolicySecurePartnerService01.xml";
        String policy_path = "/_system/governance/trunk/policies/PolicySecurePartnerService01.xml";
        String keyword1 = "UTOverTransport";
        String keyword2 = "service";

        createPolicy(policy_url);  //Add Policy

        try {
            assertTrue(registry.resourceExists(policy_path), "PolicySecurePartnerService01 Policy Exists:");       //Assert Policy exists
            propertyAssertion(policy_path);                                                                      //Assert Property
            policyContentAssertion(keyword1, keyword2, policy_path);                                             //Assert content of Policy
            registry.delete(policy_path);                                                                        //Remove Policy
            assertFalse(registry.resourceExists(policy_path), "Policy exists at " + policy_path);                  //Assert Resource was deleted successfully
            log.info("PolicyImportServiceTestClient testAddPolicySecurePartnerService01()-Passed");
        } catch (RegistryException e) {
            log.error("Failed to add SecurePartnerService01 Policy:" + e);
            throw new RegistryException("Failed to add SecurePartnerService01 Policy:" + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Add SecurePartnerService02 sample", priority = 3)
    public void testAddPolicySecurePartnerService02() throws RegistryException {
        String policy_url = "http://people.wso2.com/~evanthika/policies/PolicySecurePartnerService02.xml";
        String policy_path = "/_system/governance/trunk/policies/PolicySecurePartnerService02.xml";
        String keyword1 = "SecurePartnerService02";
        String keyword2 = "SigOnly";

        createPolicy(policy_url);   //Add Policy

        try {
            assertTrue(registry.resourceExists(policy_path), "PolicySecurePartnerService02 Policy Exists:");        //Assert Policy exists
            propertyAssertion(policy_path);                                                                       //Assert Property
            policyContentAssertion(keyword1, keyword2, policy_path);                                              //Assert content of Policy
            registry.delete(policy_path);                                                                          //Remove Policy
            assertFalse(registry.resourceExists(policy_path), "Policy exists at " + policy_path);                    //Assert Resource was deleted successfully
            log.info("PolicyImportServiceTestClient testAddPolicySecurePartnerService02()-Passed");
        } catch (RegistryException e) {
            log.error("Failed to add SecurePartnerService02 Policy:" + e);
            throw new RegistryException("Failed to add SecurePartnerService02 Policy:" + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Add SecurePartnerService03 sample", priority = 4)
    public void testAddPolicySecurePartnerService03() throws RegistryException {
        String policy_url = "http://people.wso2.com/~evanthika/policies/PolicySecurePartnerService03.xml";
        String policy_path = "/_system/governance/trunk/policies/PolicySecurePartnerService03.xml";
        String keyword1 = "SgnOnlyAnonymous";
        String keyword2 = "service";

        createPolicy(policy_url);      //Add Policy

        try {
            assertTrue(registry.resourceExists(policy_path), "PolicySecurePartnerService03 Policy Exists:");         //Assert Policy exists
            propertyAssertion(policy_path);                                                                         //Assert Property
            policyContentAssertion(keyword1, keyword2, policy_path);                                                //Assert content of Policy
            registry.delete(policy_path);                                                                           //Remove Policy
            assertFalse(registry.resourceExists(policy_path), "Policy exists at " + policy_path);                     //Assert Resource was deleted successfully
            log.info("PolicyImportServiceTestClient testAddPolicySecurePartnerService03()-Passed");
        } catch (RegistryException e) {
            log.error("Failed to add SecurePartnerService03 Policy:" + e);
            throw new RegistryException("Failed to add SecurePartnerService03 Policy:" + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Add SecurePartnerService04 sample", priority = 5)
    public void testAddPolicySecurePartnerService04() throws RegistryException {
        String policy_url = "http://people.wso2.com/~evanthika/policies/PolicySecurePartnerService04.xml";
        String policy_path = "/_system/governance/trunk/policies/PolicySecurePartnerService04.xml";
        String keyword1 = "EncrOnlyAnonymous";
        String keyword2 = "WssX509V3Token10";

        createPolicy(policy_url);           //Add Policy

        try {
            assertTrue(registry.resourceExists(policy_path), "PolicySecurePartnerService04 Policy Exists:");            //Assert Policy exists
            propertyAssertion(policy_path);                                                                           //Assert Property
            policyContentAssertion(keyword1, keyword2, policy_path);                                                  //Assert content of Policy
            registry.delete(policy_path);                                                                             //Remove Policy
            log.info("PolicyImportServiceTestClient testAddPolicySecurePartnerService04()-Passed");                  //Assert Resource was deleted successfully
        } catch (RegistryException e) {
            log.error("Failed to add SecurePartnerService04 Policy:" + e);
            throw new RegistryException("Failed to add SecurePartnerService04 Policy:" + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Add SecurePartnerService05 sample", priority = 6)
    public void testAddPolicySecurePartnerService05() throws RegistryException {
        String policy_url = "http://people.wso2.com/~evanthika/policies/PolicySecurePartnerService05.xml";
        String policy_path = "/_system/governance/trunk/policies/PolicySecurePartnerService05.xml";
        String keyword1 = "SigEncr";
        String keyword2 = "service";

        createPolicy(policy_url);     //Add Policy

        try {
            assertTrue(registry.resourceExists(policy_path), "PolicySecurePartnerService05Policy Exists:");             //Assert Policy exists
            propertyAssertion(policy_path);                                                                           //Assert Property
            policyContentAssertion(keyword1, keyword2, policy_path);                                                  //Assert content of Policy
            registry.delete(policy_path);                                                                             //Remove Policy
            assertFalse(registry.resourceExists(policy_path), "Policy exists at " + policy_path);                       //Assert Resource was deleted successfully
            log.info("PolicyImportServiceTestClient testAddPolicySecurePartnerService05()-Passed");                  //Assert Resource was deleted successfully
        } catch (RegistryException e) {
            log.error("Failed to add SecurePartnerService05 Policy:" + e);
            throw new RegistryException("Failed to add SecurePartnerService05 Policy:" + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Add SecurePartnerService06 sample", priority = 7)
    public void testAddPolicySecurePartnerService06() throws RegistryException {
        String policy_url = "http://people.wso2.com/~evanthika/policies/PolicySecurePartnerService06.xml";
        String policy_path = "/_system/governance/trunk/policies/PolicySecurePartnerService06.xml";
        String keyword1 = "SgnEncrAnonymous";
        String keyword2 = "service";

        createPolicy(policy_url);          //Add Policy

        try {
            assertTrue(registry.resourceExists(policy_path), "PolicySecurePartnerService06Policy Exists:");             //Assert Policy exists
            propertyAssertion(policy_path);                                                                           //Assert Property
            policyContentAssertion(keyword1, keyword2, policy_path);                                                  //Assert content of Policy
            registry.delete(policy_path);                                                                             //Remove Policy
            assertFalse(registry.resourceExists(policy_path), "Policy exists at " + policy_path);                       //Assert Resource was deleted successfully
            log.info("PolicyImportServiceTestClient testAddPolicySecurePartnerService06()-Passed");                  //Assert Resource was deleted successfully
        } catch (RegistryException e) {
            log.error("Failed to add SecurePartnerService06 Policy:" + e);
            throw new RegistryException("Failed to add SecurePartnerService06 Policy:" + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Add SecurePartnerService07 sample", priority = 8)
    public void testAddPolicySecurePartnerService07() throws RegistryException {
        String policy_url = "http://people.wso2.com/~evanthika/policies/PolicySecurePartnerService07.xml";
        String policy_path = "/_system/governance/trunk/policies/PolicySecurePartnerService07.xml";
        String keyword1 = "EncrOnlyUsername";
        String keyword2 = "service";

        createPolicy(policy_url);     //Add Policy

        try {
            assertTrue(registry.resourceExists(policy_path), "PolicySecurePartnerService07Policy Exists:");             //Assert Policy exists
            propertyAssertion(policy_path);                                                                           //Assert Property
            policyContentAssertion(keyword1, keyword2, policy_path);                                                  //Assert content of Policy
            registry.delete(policy_path);                                                                             //Remove Policy
            assertFalse(registry.resourceExists(policy_path), "Policy exists at " + policy_path);                       //Assert Resource was deleted successfully
            log.info("PolicyImportServiceTestClient testAddPolicySecurePartnerService07()-Passed");                  //Assert Resource was deleted successfully
        } catch (RegistryException e) {
            log.error("Failed to add SecurePartnerService07 Policy:" + e);
            throw new RegistryException("Failed to add SecurePartnerService07 Policy:" + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Add SecurePartnerService08 sample", priority = 9)
    public void testAddPolicySecurePartnerService08() throws RegistryException {
        String policy_url = "http://people.wso2.com/~evanthika/policies/PolicySecurePartnerService08.xml";
        String policy_path = "/_system/governance/trunk/policies/PolicySecurePartnerService08.xml";
        String keyword1 = "SgnEncrUsername";
        String keyword2 = "service";

        createPolicy(policy_url);     //Add Policy

        try {
            assertTrue(registry.resourceExists(policy_path), "PolicySecurePartnerService08Policy Exists:");             //Assert Policy exists
            propertyAssertion(policy_path);                                                                           //Assert Property
            policyContentAssertion(keyword1, keyword2, policy_path);                                                  //Assert content of Policy
            registry.delete(policy_path);                                                                             //Remove Policy
            assertFalse(registry.resourceExists(policy_path), "Policy exists at " + policy_path);                       //Assert Resource was deleted successfully
            log.info("PolicyImportServiceTestClient testAddPolicySecurePartnerService08()-Passed");                  //Assert Resource was deleted successfully
        } catch (RegistryException e) {
            log.error("Failed to add SecurePartnerService08 Policy:" + e);
            throw new RegistryException("Failed to add SecurePartnerService08 Policy:" + e);
        }

    }

    @Test(groups = {"wso2.greg"}, description = "Add SecurePartnerService09 sample", priority = 10)
    public void testAddPolicySecurePartnerService09() throws RegistryException {
        String policy_url = "http://people.wso2.com/~evanthika/policies/PolicySecurePartnerService09.xml";
        String policy_path = "/_system/governance/trunk/policies/PolicySecurePartnerService09.xml";
        String keyword1 = "SecConSignOnly";
        String keyword2 = "service";

        createPolicy(policy_url);   //Add Policy

        try {
            assertTrue(registry.resourceExists(policy_path), "PolicySecurePartnerService09Policy Exists:");             //Assert Policy exists
            propertyAssertion(policy_path);                                                                           //Assert Property
            policyContentAssertion(keyword1, keyword2, policy_path);                                                  //Assert content of Policy
            registry.delete(policy_path);                                                                             //Remove Policy
            assertFalse(registry.resourceExists(policy_path), "Policy exists at " + policy_path);                       //Assert Resource was deleted successfully
            log.info("PolicyImportServiceTestClient testAddPolicySecurePartnerService09()-Passed");                  //Assert Resource was deleted successfully
        } catch (RegistryException e) {
            log.error("Failed to add SecurePartnerService09 Policy:" + e);
            throw new RegistryException("Failed to add SecurePartnerService09 Policy:" + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Add SecurePartnerService10 sample", priority = 11)
    public void testAddPolicySecurePartnerService10() throws RegistryException {
        String policy_url = "http://people.wso2.com/~evanthika/policies/PolicySecurePartnerService10.xml";
        String policy_path = "/_system/governance/trunk/policies/PolicySecurePartnerService10.xml";
        String keyword1 = "SecConEncrOnly";
        String keyword2 = "service";

        createPolicy(policy_url);   //Add Policy

        try {
            assertTrue(registry.resourceExists(policy_path), "PolicySecurePartnerService10Policy Exists:");             //Assert Policy exists
            propertyAssertion(policy_path);                                                                           //Assert Property
            policyContentAssertion(keyword1, keyword2, policy_path);                                                  //Assert content of Policy
            registry.delete(policy_path);                                                                             //Remove Policy
            assertFalse(registry.resourceExists(policy_path), "Policy exists at " + policy_path);                       //Assert Resource was deleted successfully
            log.info("PolicyImportServiceTestClient testAddPolicySecurePartnerService10()-Passed");                  //Assert Resource was deleted successfully
        } catch (RegistryException e) {
            log.error("Failed to add SecurePartnerService10 Policy:" + e);
            throw new RegistryException("Failed to add SecurePartnerService10 Policy:" + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Add SecurePartnerService11 sample", priority = 12)
    public void testAddPolicySecurePartnerService11() throws RegistryException {
        String policy_url = "http://people.wso2.com/~evanthika/policies/PolicySecurePartnerService11.xml";
        String policy_path = "/_system/governance/trunk/policies/PolicySecurePartnerService11.xml";
        String keyword1 = "SecConSgnEncr";
        String keyword2 = "service";

        createPolicy(policy_url);   //Add Policy

        try {
            assertTrue(registry.resourceExists(policy_path), "PolicySecurePartnerService11Policy Exists:");             //Assert Policy exists
            propertyAssertion(policy_path);                                                                           //Assert Property
            policyContentAssertion(keyword1, keyword2, policy_path);                                                  //Assert content of Policy
            registry.delete(policy_path);                                                                             //Remove Policy
            assertFalse(registry.resourceExists(policy_path), "Policy exists at " + policy_path);                       //Assert Resource was deleted successfully
            log.info("PolicyImportServiceTestClient testAddPolicySecurePartnerService11()-Passed");                  //Assert Resource was deleted successfully
        } catch (RegistryException e) {
            log.error("Failed to add SecurePartnerService11 Policy:" + e);
            throw new RegistryException("Failed to add SecurePartnerService11 Policy:" + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Add SecurePartnerService12 sample", priority = 13)
    public void testAddPolicySecurePartnerService12() throws RegistryException {
        String policy_url = "http://people.wso2.com/~evanthika/policies/PolicySecurePartnerService12.xml";
        String policy_path = "/_system/governance/trunk/policies/PolicySecurePartnerService12.xml";
        String keyword1 = "SecConSignOnlyAnonymous";
        String keyword2 = "service";

        createPolicy(policy_url);   //Add Policy

        try {
            assertTrue(registry.resourceExists(policy_path), "PolicySecurePartnerService12Policy Exists:");             //Assert Policy exists
            propertyAssertion(policy_path);                                                                           //Assert Property
            policyContentAssertion(keyword1, keyword2, policy_path);                                                  //Assert content of Policy
            registry.delete(policy_path);                                                                             //Remove Policy
            assertFalse(registry.resourceExists(policy_path), "Policy exists at " + policy_path);                       //Assert Resource was deleted successfully
            log.info("PolicyImportServiceTestClient testAddPolicySecurePartnerService12()-Passed");                  //Assert Resource was deleted successfully
        } catch (RegistryException e) {
            log.error("Failed to add SecurePartnerService12 Policy:" + e);
            throw new RegistryException("Failed to add SecurePartnerService12 Policy:" + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Add SecurePartnerService13 sample", priority = 14)
    public void testAddPolicySecurePartnerService13() throws RegistryException {
        String policy_url = "http://people.wso2.com/~evanthika/policies/PolicySecurePartnerService13.xml";
        String policy_path = "/_system/governance/trunk/policies/PolicySecurePartnerService13.xml";
        String keyword1 = "SecConEncrOnlyAnonymous";
        String keyword2 = "service";

        createPolicy(policy_url);   //Add Policy

        try {
            assertTrue(registry.resourceExists(policy_path), "PolicySecurePartnerService13Policy Exists:");             //Assert Policy exists
            propertyAssertion(policy_path);                                                                           //Assert Property
            policyContentAssertion(keyword1, keyword2, policy_path);                                                  //Assert content of Policy
            registry.delete(policy_path);                                                                             //Remove Policy
            assertFalse(registry.resourceExists(policy_path), "Policy exists at " + policy_path);                       //Assert Resource was deleted successfully
            log.info("PolicyImportServiceTestClient testAddPolicySecurePartnerService13()-Passed");                  //Assert Resource was deleted successfully
        } catch (RegistryException e) {
            log.error("Failed to add SecurePartnerService13 Policy:" + e);
            throw new RegistryException("Failed to add SecurePartnerService13 Policy:" + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Add SecurePartnerService14 sample", priority = 15)
    public void testAddPolicySecurePartnerService14() throws RegistryException {
        String policy_url = "http://people.wso2.com/~evanthika/policies/PolicySecurePartnerService14.xml";
        String policy_path = "/_system/governance/trunk/policies/PolicySecurePartnerService14.xml";
        String keyword1 = "SecConEncrUsername";
        String keyword2 = "service";

        createPolicy(policy_url);   //Add Policy

        try {
            assertTrue(registry.resourceExists(policy_path), "PolicySecurePartnerService14Policy Exists:");             //Assert Policy exists
            propertyAssertion(policy_path);                                                                           //Assert Property
            policyContentAssertion(keyword1, keyword2, policy_path);                                                  //Assert content of Policy
            registry.delete(policy_path);                                                                             //Remove Policy
            assertFalse(registry.resourceExists(policy_path), "Policy exists at " + policy_path);                       //Assert Resource was deleted successfully
            log.info("PolicyImportServiceTestClient testAddPolicySecurePartnerService14()-Passed");                  //Assert Resource was deleted successfully
        } catch (RegistryException e) {
            log.error("Failed to add SecurePartnerService14 Policy:" + e);
            throw new RegistryException("Failed to add SecurePartnerService14 Policy:" + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Add SecurePartnerService15 sample", priority = 16)
    public void testAddPolicySecurePartnerService15() throws RegistryException {
        String policy_url = "http://people.wso2.com/~evanthika/policies/PolicySecurePartnerService15.xml";
        String policy_path = "/_system/governance/trunk/policies/PolicySecurePartnerService15.xml";
        String keyword1 = "SecConSgnEncrUsername";
        String keyword2 = "service";

        createPolicy(policy_url);   //Add Policy

        try {
            assertTrue(registry.resourceExists(policy_path), "PolicySecurePartnerService15Policy Exists:");             //Assert Policy exists
            propertyAssertion(policy_path);                                                                           //Assert Property
            policyContentAssertion(keyword1, keyword2, policy_path);                                                  //Assert content of Policy
            registry.delete(policy_path);                                                                             //Remove Policy
            assertFalse(registry.resourceExists(policy_path), "Policy exists at " + policy_path);                       //Assert Resource was deleted successfully
            log.info("PolicyImportServiceTestClient testAddPolicySecurePartnerService15()-Passed");
        } catch (RegistryException e) {
            log.error("Failed to add SecurePartnerService15 Policy:" + e);
            throw new RegistryException("Failed to add SecurePartnerService15 Policy:" + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Add SecConSignOnly Policy sample", priority = 17)
    public void testAddSecConSignOnly() throws RegistryException {
        String policy_url = "http://people.wso2.com/~evanthika/policies/SecConSignOnly.xml";
        String policy_path = "/_system/governance/trunk/policies/SecConSignOnly.xml";
        String keyword1 = "SecConSignOnly";
        String keyword2 = "sp:SymmetricBinding";

        createPolicy(policy_url);    //Add Policy

        try {
            assertTrue(registry.resourceExists(policy_path), "SecConSignOnly Exists:");          //Assert Policy exists
            propertyAssertion(policy_path);                                                    //Assert Property
            policyContentAssertion(keyword1, keyword2, policy_path);                           //Assert content of Policy
            registry.delete(policy_path);                                                        //Remove Policy
            assertFalse(registry.resourceExists(policy_path), "Policy exists at " + policy_path);   //Assert Resource was deleted successfully
            log.info("PolicyImportServiceTestClient testAddSecConSignOnly()-Passed");
        } catch (RegistryException e) {
            log.error("Failed to add SecConSignOnly Policy:" + e);
            throw new RegistryException("Failed to add SecConSignOnly Policy:" + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Add SgnEncrAnonymous Policy sample", priority = 18)
    public void testAddSgnEncrAnonymous() throws RegistryException {
        String policy_url = "http://people.wso2.com/~evanthika/policies/SgnEncrAnonymous.xml";
        String policy_path = "/_system/governance/trunk/policies/SgnEncrAnonymous.xml";
        String keyword1 = "SgnEncrAnonymous";
        String keyword2 = "sp:X509Token";

        createPolicy(policy_url);         //Add Policy

        try {

            assertTrue(registry.resourceExists(policy_path), "SgnEncrAnonymous Exists:");               //Assert Policy exists
            propertyAssertion(policy_path);                                                           //Assert Property
            policyContentAssertion(keyword1, keyword2, policy_path);                                  //Assert content of Policy
            registry.delete(policy_path);                                                             //Remove Policy
            assertFalse(registry.resourceExists(policy_path), "Policy exists at " + policy_path);       //Assert Resource was deleted successfully
            log.info("PolicyImportServiceTestClient testAddSgnEncrAnonymous()-Passed");
        } catch (RegistryException e) {
            log.error("Failed to add SgnEncrAnonymous Policy:" + e);
            throw new RegistryException("Failed to add SgnEncrAnonymous Policy:" + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Add SigEncr Policy sample", priority = 19)
    public void testAddSigEncr() throws RegistryException {
        String policy_url = "http://people.wso2.com/~evanthika/policies/SigEncr.xml";
        String policy_path = "/_system/governance/trunk/policies/SigEncr.xml";
        String keyword1 = "SigEncr";
        String keyword2 = "WssX509V3Token10";

        createPolicy(policy_url);  //Add Policy

        try {
            assertTrue(registry.resourceExists(policy_path), "SigEncr:");                      //Assert Policy exists
            propertyAssertion(policy_path);                                                  //Assert Property
            policyContentAssertion(keyword1, keyword2, policy_path);                         //Assert content of Policy
            registry.delete(policy_path);                                                    //Remove Policy
            assertFalse(registry.resourceExists(policy_path), "Policy exists at " + policy_path);  //Assert Resource was deleted successfully
            log.info("PolicyImportServiceTestClient testAddSigEncr()-Passed");
        } catch (RegistryException e) {
            log.error("Failed to add SigEncr Policy:" + e);
            throw new RegistryException("Failed to add SigEncr Policy:" + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Add SigOnly Policy sample", priority = 20)
    public void testAddSigOnly() throws RegistryException {
        String policy_url = "http://people.wso2.com/~evanthika/policies/SignOnly.xml";
        String policy_path = "/_system/governance/trunk/policies/SignOnly.xml";
        String keyword1 = "SigOnly";
        String keyword2 = "WssX509V3Token10";

        createPolicy(policy_url);     //Add Policy

        try {
            assertTrue(registry.resourceExists(policy_path), "SigOnly:");              //Assert Policy exists
            propertyAssertion(policy_path);                                            //Assert Property
            policyContentAssertion(keyword1, keyword2, policy_path);                   //Assert content of Policy
            registry.delete(policy_path);                                              //Remove Policy
            assertFalse(registry.resourceExists(policy_path), "Policy exists at " + policy_path);   //Assert Resource was deleted successfully
            log.info("PolicyImportServiceTestClient testAddSigOnly()-Passed");
        } catch (RegistryException e) {
            log.error("Failed to add SigOnly Policy:" + e);
            throw new RegistryException("Failed to add SigOnly Policy:" + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Add SignOnlyAnonymous Policy sample", priority = 21)
    public void testAddSignOnlyAnonymous() throws RegistryException {
        String policy_url = "http://people.wso2.com/~evanthika/policies/SignOnlyAnonymous.xml";
        String policy_path = "/_system/governance/trunk/policies/SignOnlyAnonymous.xml";
        String keyword1 = "SgnOnlyAnonymous";
        String keyword2 = "WssX509V3Token10";

        createPolicy(policy_url);            //Add Policy

        try {
            assertTrue(registry.resourceExists(policy_path), "SignOnlyAnonymous:");                          //Assert Policy exists
            propertyAssertion(policy_path);                                                               //Assert Property
            policyContentAssertion(keyword1, keyword2, policy_path);                                      //Assert content of Policy
            registry.delete(policy_path);                                                                  //Remove Policy
            assertFalse(registry.resourceExists(policy_path), "Policy exists at " + policy_path);           //Assert Resource was deleted successfully
            log.info("PolicyImportServiceTestClient testAddSignOnlyAnonymous()-Passed");
        } catch (RegistryException e) {
            log.error("Failed to add SignOnlyAnonymous Policy:" + e);
            throw new RegistryException("Failed to add SignOnlyAnonymous Policy:" + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Add SignOnlyAnonymous Policy sample", priority = 22)
    public void testAddUTOverTransport() throws RegistryException {
        String policy_url = "http://people.wso2.com/~evanthika/policies/UTPolicy.xml";
        String policy_path = "/_system/governance/trunk/policies/UTPolicy.xml";
        String keyword1 = "UTOverTransport";
        String keyword2 = "Basic256";

        createPolicy(policy_url);       //Add Policy

        try {
            assertTrue(registry.resourceExists(policy_path), "UTOverTransport:");                       //Assert Policy exists
            propertyAssertion(policy_path);                                                           //Assert Property
            policyContentAssertion(keyword1, keyword2, policy_path);                                 //Assert content of Policy
            registry.delete(policy_path);                                                             //Remove Policy
            assertFalse(registry.resourceExists(policy_path), "Policy exists at " + policy_path);       //Assert Resource was deleted successfully
            log.info("PolicyImportServiceTestClient testAddUTOverTransport()-Passed");
        } catch (RegistryException e) {
            log.error("Failed to add UTOverTransport Policy:" + e);
            throw new RegistryException("Failed to add UTOverTransport Policy:" + e);
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
        registry = null;
        registryProviderUtil = null;
    }

    public void createPolicy
            (String
                     policy_url) throws GovernanceException {
        PolicyManager policyManager = new PolicyManager(governance);
        Policy policy = null;
        try {
            policy = policyManager.newPolicy(policy_url);
            policy.addAttribute("creator", "Aaaa");
            policy.addAttribute("version", "1.0.0");
            policyManager.addPolicy(policy);
        } catch (GovernanceException e) {
            log.error("Failed to add Policy:" + e);
            throw new GovernanceException("Failed to add Policy :" + e);
        }
    }

    public void deletePolicy() throws RegistryException {
        try {
            if (registry.resourceExists("/_system/governance/trunk/policies")) {
                registry.delete("/_system/governance/trunk/policies");
            }
        } catch (RegistryException e) {
            log.error("Failed to delete Policy:" + e);
            throw new RegistryException("Failed to delete Policy :" + e);
        }
    }


    public void propertyAssertion(String policy_path) throws RegistryException {
        Resource resource;
        try {
            resource = registry.get(policy_path);
            assertEquals(resource.getProperty("creator"), "Aaaa", "WSDL Property - WSI creator");
            assertEquals(resource.getProperty("version"), "1.0.0", "WSDL Property - WSI version");
        } catch (RegistryException e) {
            log.error("Failed to Assert Properties :" + e);
            throw new RegistryException("Failed to Assert Properties :" + e);
        }
    }


    public void policyContentAssertion(String keyword1, String keyword2, String policy_path) throws RegistryException {
        Resource r1 = registry.newResource();
        String content = null;
        try {
            r1 = registry.get(policy_path);
            content = new String((byte[]) r1.getContent());
            assertTrue(content.indexOf(keyword1) > 0, "Assert Content Policy file - keyword1");
            assertTrue(content.indexOf(keyword2) > 0, "Assert Content Policy file - keyword2");
        } catch (RegistryException e) {
            log.error("Registry Exception thrown:" + e);
            throw new RegistryException("Failed to Assert Properties :" + e);
        }
    }

}
