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

import org.apache.axis2.AxisFault;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.policies.PolicyManager;
import org.wso2.carbon.governance.api.policies.dataobjects.Policy;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.info.stub.RegistryExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.clients.InfoServiceAdminClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;
import java.net.MalformedURLException;
import java.rmi.RemoteException;

import static org.testng.Assert.*;

public class PolicyRatingVerificationTestCase extends GREGIntegrationBaseTest{
    private Registry governanceRegistry;
    private Policy policy;

    private InfoServiceAdminClient infoServiceAdminclient;
    private PolicyManager policyManager;
    private float avgRating;
    private int myRating;
    private String sessionCookie;

    @BeforeClass(groups = "wso2.greg", alwaysRun = true)
    public void initialize() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_USER);
        sessionCookie = new LoginLogoutClient(automationContext).login();

        infoServiceAdminclient = new InfoServiceAdminClient(automationContext.getContextUrls()
                .getBackEndUrl(),sessionCookie);

        RegistryProviderUtil provider = new RegistryProviderUtil();
        WSRegistryServiceClient wsRegistry = provider.getWSRegistry(automationContext);
        governanceRegistry = provider.getGovernanceRegistry(wsRegistry, automationContext);
        policyManager = new PolicyManager(governanceRegistry);

    }

    /**
     * ratings verification
     */
    @Test(groups = "wso2.greg", description = "ratings verification")
    public void testAddPolicy() throws RemoteException,
            ResourceAdminServiceExceptionException, GovernanceException,
            MalformedURLException {


        policy = policyManager
                .newPolicy("https://raw.githubusercontent.com/wso2/wso2-qa-artifacts/master/automation-artifacts" +
                        "/greg/policy/policy1.xml");

        policy.addAttribute("version", "1.0.0");
        policy.addAttribute("author", "Kanarupan");
        policy.addAttribute("description", "added Policy using url");
        policyManager.addPolicy(policy);
        policyManager.updatePolicy(policy);

        assertFalse(policy.getId().isEmpty());
        assertNotNull(policy);
        assertTrue(policy.getAttribute("author").contentEquals("Kanarupan"));
        assertTrue(policy.getAttribute("description").contentEquals(
                "added Policy using url"));

    }

    /**
     * Jira created: Key: REGISTRY-1204:
     * https://wso2.org/jira/browse/REGISTRY-1204
     */
    @Test(groups = "wso2.greg", description = "ratings Verification", dependsOnMethods = "testAddPolicy")
    public void testRatingVerification() throws AxisFault, RegistryException, RegistryExceptionException {

        final String policyPath = "/_system/governance/trunk/policies/policy.xml";

        myRating = 3;

        infoServiceAdminclient.rateResource(String.valueOf(myRating),
                policyPath, sessionCookie);

        int checkRating = infoServiceAdminclient.getRatings(policyPath,
                sessionCookie).getUserRating();

        //	policyManager.updatePolicy(policy); // updating

        checkRating = infoServiceAdminclient.getRatings(policyPath,
                sessionCookie).getUserRating();

        assertEquals(checkRating, myRating);

        avgRating = infoServiceAdminclient.getRatings(policyPath,
                sessionCookie).getAverageRating();
        assertEquals(avgRating, 3.0f);

    }

    /**
     * the rating is returned 0 instead of 3
     */
    @Test(groups = "wso2.greg", description = "myrating, rating relationship verified", dependsOnMethods = "testRatingVerification")
    public void testRatingChanges() throws AxisFault, GovernanceException,
            RegistryException, RegistryExceptionException {

        final String policyPath = "/_system/governance/trunk/policies/policy.xml";

        myRating = 2;
        infoServiceAdminclient.rateResource(String.valueOf(myRating),
                policyPath, sessionCookie);
        //policyManager.updatePolicy(policy);
        avgRating = infoServiceAdminclient.getRatings(policyPath,
                sessionCookie).getAverageRating();

        assertTrue(avgRating < 3.0f);

    }

    @AfterClass(groups = "wso2.greg", alwaysRun = true, description = "cleaning up the artifacts added")
    public void tearDown() throws GovernanceException {
        policyManager.removePolicy(policy.getId());
        policy = null;
        policyManager = null;
        governanceRegistry = null;
        infoServiceAdminclient = null;


    }

}
