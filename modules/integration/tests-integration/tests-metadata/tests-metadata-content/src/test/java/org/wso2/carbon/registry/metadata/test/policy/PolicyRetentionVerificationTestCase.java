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
import org.wso2.carbon.authenticator.stub.LogoutAuthenticationExceptionException;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.policies.PolicyManager;
import org.wso2.carbon.governance.api.policies.dataobjects.Policy;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.properties.stub.PropertiesAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.properties.stub.beans.xsd.RetentionBean;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.clients.PropertiesAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import javax.xml.xpath.XPathExpressionException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.testng.Assert.*;

public class PolicyRetentionVerificationTestCase extends GREGIntegrationBaseTest {

    private Policy policy;
    private Registry governanceRegistry;
    private PropertiesAdminServiceClient propertiesAdminServiceClient;
    private PolicyManager policyManager;
    private RegistryProviderUtil registryProviderUtil;
    private PolicyManager policyManager2;
    private String policyPath;
    private SimpleDateFormat dateFormat;
    private Date date;
    private Calendar calendar;
    private Policy policyAddedByFirstUser;
    private String sessionCookieUser1;
    private String sessionCookieUser2;

    @BeforeClass (groups = "wso2.greg", alwaysRun = true)
    public void initialize () throws Exception {

        super.init(TestUserMode.SUPER_TENANT_USER);
        sessionCookieUser1 = new LoginLogoutClient(automationContext).login();

        registryProviderUtil = new RegistryProviderUtil();

        WSRegistryServiceClient wsRegistryServiceClient =
                registryProviderUtil.getWSRegistry(automationContext);

        governanceRegistry = registryProviderUtil.getGovernanceRegistry(
                wsRegistryServiceClient, automationContext);

    }

    @Test (groups = "wso2.greg", description = "policy addition for retention Verification")
    public void testAddResourcesToVerifyRetention () throws RemoteException,
            MalformedURLException,
            ResourceAdminServiceExceptionException,
            GovernanceException {

        policyManager = new PolicyManager(governanceRegistry);
        policy = policyManager
                .newPolicy("https://raw.githubusercontent.com/wso2/wso2-qa-artifacts/master/automation-artifacts/" +
                        "greg/policy/policy1.xml");
        policy.addAttribute("version", "1.0.0");
        policy.addAttribute("author", "Kanarupan");
        policy.addAttribute("description", "for retention verification");
        policyManager.addPolicy(policy);
        policyManager.updatePolicy(policy);
        policyPath = "/_system/governance" + policy.getPath();
    }

    /*testFirstUserSetRetention
     * Jira created: Key: REGISTRY-1204:
     * https://wso2.org/jira/browse/REGISTRY-1204
     */
    @Test (groups = "wso2.greg", description = "Retention Verification", dependsOnMethods = "testAddResourcesToVerifyRetention")
    public void testFirstUserSetRetention () throws GovernanceException,
            RemoteException,
            PropertiesAdminServiceRegistryExceptionException,
            LogoutAuthenticationExceptionException, XPathExpressionException {

        propertiesAdminServiceClient = new PropertiesAdminServiceClient(automationContext
                .getContextUrls().getBackEndUrl(), sessionCookieUser1);

        /* getting current date and date exactly after a month */
        dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        date = new Date();
        calendar = GregorianCalendar.getInstance();
        calendar.add(Calendar.MONTH, 1);
        propertiesAdminServiceClient.setRetentionProperties(policyPath,
                "delete", dateFormat.format(date),
                dateFormat.format(calendar.getTime())); // delete
        // access
        // locked
        //	policyManager.updatePolicy(policy); // updating policy
        RetentionBean retentionBean = propertiesAdminServiceClient
                .getRetentionProperties(policyPath);
        assertTrue(retentionBean.getDeleteLocked());
        assertTrue(retentionBean.getFromDate().contentEquals(
                dateFormat.format(date)));
        assertFalse(retentionBean.getWriteLocked());

        /* logout */
        new AuthenticatorClient(automationContext.getContextUrls().getBackEndUrl()).logOut();
    }

    /*
     * With SecondUser, couldn't access the artifact using
     * policyManager.getPolicy(path), used getAllPolicies()
     */

    @Test (groups = "wso2.greg", description = "Retention verification: second user",
            dependsOnMethods = "testFirstUserSetRetention")
    public void testSecondUserRetention () throws Exception {

        AutomationContext automationContext1 = new AutomationContext("GREG", "greg001",
                "superTenant", "user2");
        sessionCookieUser2 = new LoginLogoutClient(automationContext1).login();

        propertiesAdminServiceClient = new PropertiesAdminServiceClient(automationContext1
                .getContextUrls().getBackEndUrl(), sessionCookieUser2);

        WSRegistryServiceClient wsRegistryServiceClient = registryProviderUtil
                .getWSRegistry(automationContext1);

        Registry governanceRegistry1 = registryProviderUtil
                .getGovernanceRegistry(wsRegistryServiceClient, automationContext1);

        policyManager2 = new PolicyManager(governanceRegistry1);
        Policy[] policies = policyManager2.getAllPolicies();
        for (Policy tmpPolicy : policies) {
            if (tmpPolicy.getQName().toString().contains("policy1.xml")) {
                policyAddedByFirstUser = tmpPolicy;
            }
        }
        assertTrue(policyAddedByFirstUser.getQName().toString()
                .contains("policy1.xml"));
        assertTrue(policyAddedByFirstUser.getAttribute("author").contains(
                "Kanarupan"));
        policyAddedByFirstUser.addAttribute("WriteAccess", "enabled");
        //policyManager2.updatePolicy(policyAddedByFirstUser);
        assertTrue(policyAddedByFirstUser.getAttribute("WriteAccess").contains(
                "enabled"));
        RetentionBean retentionBean = propertiesAdminServiceClient
                .getRetentionProperties(policyPath);
        assertEquals(retentionBean.getFromDate(), dateFormat.format(date));

        String userName = automationContext.getContextTenant().getContextUser().getUserName();
        String userNameWithoutDomain = userName.substring(0, userName.indexOf('@'));
        assertEquals(retentionBean.getUserName(), userNameWithoutDomain);

        assertFalse(retentionBean.getWriteLocked());
        assertTrue(retentionBean.getDeleteLocked());

    }

    @Test (groups = "wso2.greg", description = "second user deletion check: blocked by first user",
            expectedExceptions = GovernanceException.class, dependsOnMethods = "testSecondUserRetention")
    public void testSecondUserRetentionDeleteCheck () throws GovernanceException {

        policyManager2.removePolicy(policyAddedByFirstUser.getPath());
    }

    @AfterClass (groups = "wso2.greg", alwaysRun = true, description = "cleaning up the artifacts added")
    public void tearDown () throws Exception {

        registryProviderUtil = new RegistryProviderUtil();
        WSRegistryServiceClient wsRegistryServiceClient = registryProviderUtil
                .getWSRegistry(automationContext);
        governanceRegistry = registryProviderUtil.getGovernanceRegistry(
                wsRegistryServiceClient, automationContext);
        policyManager = new PolicyManager(governanceRegistry);
        policyManager.removePolicy(policy.getId());
        policy = null;
        policyAddedByFirstUser = null;
        policyManager = null;
        policyManager2 = null;
        governanceRegistry = null;
        date = null;
        calendar = null;
        dateFormat = null;
        propertiesAdminServiceClient = null;
        registryProviderUtil = null;

    }
}
