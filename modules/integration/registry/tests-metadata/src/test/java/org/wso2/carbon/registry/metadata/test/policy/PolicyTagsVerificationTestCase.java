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
import org.wso2.carbon.registry.info.stub.beans.utils.xsd.Tag;
import org.wso2.carbon.registry.info.stub.beans.xsd.TagBean;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import java.net.MalformedURLException;
import java.rmi.RemoteException;

import static org.testng.Assert.*;

public class PolicyTagsVerificationTestCase {
    private Registry governanceRegistry;
    private Policy policy;
    private ManageEnvironment environment;
    private InfoServiceAdminClient infoServiceAdminclient;
    private PolicyManager policyManager;

    @BeforeClass(groups = "wso2.greg", alwaysRun = true)
    public void initialize() throws RemoteException,
            LoginAuthenticationExceptionException,
            org.wso2.carbon.registry.api.RegistryException {
        int userId = 2;
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        environment = builder.build();
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
     * tags verification
     */
    @Test(groups = "wso2.greg", description = "tags verification")
    public void testAddPolicy() throws RemoteException,
            ResourceAdminServiceExceptionException, GovernanceException,
            MalformedURLException {


        policy = policyManager
                .newPolicy("https://svn.wso2.org/repos/wso2/carbon/platform/trunk/"
                        + "platform-integration/platform-automated-test-suite/"
                        + "org.wso2.carbon.automation.test.repo/src/main/"
                        + "resources/artifacts/GREG/policy/policy.xml");

        policy.addAttribute("version", "1.0.0");
        policy.addAttribute("author", "Aparna");
        policy.addAttribute("description", "added policy using url");
        policyManager.addPolicy(policy);

        assertFalse(policy.getId().isEmpty());
        assertNotNull(policy);
        assertTrue(policy.getAttribute("author").contentEquals("Aparna"));

    }

    @Test(groups = "wso2.greg", description = "tags Verification", dependsOnMethods = "testAddPolicy")
    public void testTagsVerification() throws AxisFault, GovernanceException,
            RegistryException, RegistryExceptionException {

        final String policyPath = "/_system/governance" + policy.getPath();

        infoServiceAdminclient.addTag("my tag", policyPath, environment
                .getGreg().getSessionCookie());
        TagBean tagBean = infoServiceAdminclient.getTags(policyPath,
                environment.getGreg().getSessionCookie());

        Tag[] tags = tagBean.getTags();
        boolean status = false;
        for (Tag tmpTag : tags) {
            if (tmpTag.getTagName().contentEquals("my tag")) {
                status = true;
            }
        }
        assertTrue(status, "verifying the tag creation");

    }

    @AfterClass(groups = "wso2.greg", alwaysRun = true, description = "cleaning up the artifacts added")
    public void tearDown() throws GovernanceException {
        policyManager.removePolicy(policy.getId());
        environment = null;
        policy = null;
        policyManager = null;
        infoServiceAdminclient = null;
        governanceRegistry = null;

    }

}
