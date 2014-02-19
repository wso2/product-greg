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
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.registry.InfoServiceAdminClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.policies.PolicyManager;
import org.wso2.carbon.governance.api.policies.dataobjects.Policy;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.info.stub.RegistryExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import java.net.MalformedURLException;
import java.rmi.RemoteException;

import static org.testng.Assert.*;

public class PolicyRatingVerificationTestCase {
    private Registry governanceRegistry;
    private Policy policy;
    private ManageEnvironment environment;
    private int userId = 2;
    private UserInfo userInfo;

    private InfoServiceAdminClient infoServiceAdminclient;
    private PolicyManager policyManager;
    private float avgRating;
    private int myRating;

    @BeforeClass(groups = "wso2.greg", alwaysRun = true)
    public void initialize() throws RemoteException,
            LoginAuthenticationExceptionException,
            org.wso2.carbon.registry.api.RegistryException {

        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        environment = builder.build();
        userInfo = UserListCsvReader.getUserInfo(userId);
        infoServiceAdminclient = new InfoServiceAdminClient(environment
                .getGreg().getProductVariables().getBackendUrl(),
                environment.getGreg().getSessionCookie());

        RegistryProviderUtil provider = new RegistryProviderUtil();
        WSRegistryServiceClient wsRegistry = provider.getWSRegistry(userId,
                ProductConstant.GREG_SERVER_NAME);
        governanceRegistry = provider.getGovernanceRegistry(wsRegistry, userId);
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
                .newPolicy("https://svn.wso2.org/repos/wso2/carbon/platform/trunk/"
                        + "platform-integration/platform-automated-test-suite/"
                        + "org.wso2.carbon.automation.test.repo/src/main/"
                        + "resources/artifacts/GREG/policy/policy.xml");

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
    public void testRatingVerification() throws AxisFault, GovernanceException,
            RegistryException, RegistryExceptionException {

        final String policyPath = "/_system/governance/trunk/policies/policy.xml";

        myRating = 3;

        infoServiceAdminclient.rateResource(String.valueOf(myRating),
                policyPath, environment.getGreg().getSessionCookie());

        int checkRating = infoServiceAdminclient.getRatings(policyPath,
                environment.getGreg().getSessionCookie()).getUserRating();

        //	policyManager.updatePolicy(policy); // updating

        checkRating = infoServiceAdminclient.getRatings(policyPath,
                environment.getGreg().getSessionCookie()).getUserRating();

        assertEquals(checkRating, myRating);

        avgRating = infoServiceAdminclient.getRatings(policyPath,
                environment.getGreg().getSessionCookie()).getAverageRating();
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
                policyPath, environment.getGreg().getSessionCookie());
        //policyManager.updatePolicy(policy);
        avgRating = infoServiceAdminclient.getRatings(policyPath,
                environment.getGreg().getSessionCookie()).getAverageRating();

        assertTrue(avgRating < 3.0f);

    }

    @AfterClass(groups = "wso2.greg", alwaysRun = true, description = "cleaning up the artifacts added")
    public void tearDown() throws GovernanceException {
        policyManager.removePolicy(policy.getId());
        policy = null;
        policyManager = null;
        governanceRegistry = null;
        infoServiceAdminclient = null;
        environment = null;


    }

}
