/*
 * Copyright 2004,2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.registry.jira.issues.test;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.governance.ListMetaDataServiceClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.policies.PolicyManager;
import org.wso2.carbon.governance.api.policies.dataobjects.Policy;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.governance.list.stub.beans.xsd.PolicyBean;
import org.wso2.carbon.governance.list.stub.beans.xsd.WSDLBean;
import org.wso2.carbon.registry.app.RemoteRegistry;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import java.net.MalformedURLException;
import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;

/**
 * https://wso2.org/jira/browse/CARBON-8971 reopened
 * test cases are disabled
 */
public class Carbon8971 {

    private int userId = 2;
    private UserInfo userInfo = UserListCsvReader.getUserInfo(userId);
    private Registry registry;
    private ListMetaDataServiceClient listMetadataServiceClient;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private WSRegistryServiceClient wsRegistryServiceClient;

    private final static String WSDL_URL =
            "https://svn.wso2.org/repos/wso2/carbon/platform/trunk/platform-integration/" +
                    "platform-automated-test-suite/org.wso2.carbon.automation.test.repo/src/main/resources/" +
                    "artifacts/GREG/wsdl/info.wsdl";
    private final static String POLICY_URL =
            "https://svn.wso2.org/repos/wso2/carbon/platform/trunk/platform-integration/" +
                    "platform-automated-test-suite/org.wso2.carbon.automation.test.repo/src/main/resources/" +
                    "artifacts/GREG/policy/UTPolicy.xml";

    private final static String WSDL_NAME = "info.wsdl";
    private final static String POLICY_NAME = "UTPolicy.xml";

    /**
     * @throws RemoteException
     * @throws LoginAuthenticationExceptionException
     *
     * @throws MalformedURLException
     * @throws RegistryException
     */
    @BeforeClass(alwaysRun = true)
    public void init() throws RemoteException, LoginAuthenticationExceptionException,
                              MalformedURLException, RegistryException {

        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        ManageEnvironment environment = builder.build();
        RegistryProviderUtil registryProvider = new RegistryProviderUtil();
        RemoteRegistry remoteRegistry =
                registryProvider.getRemoteRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        registry = registryProvider.getGovernanceRegistry(remoteRegistry, userId);

        listMetadataServiceClient =
                new ListMetaDataServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                              environment.getGreg().getSessionCookie());

        resourceAdminServiceClient =
                new ResourceAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                               environment.getGreg().getSessionCookie());

        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();

        wsRegistryServiceClient =
                registryProviderUtil.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);
    }

    /**
     * @throws RegistryException
     * @throws MalformedURLException
     * @throws RemoteException
     * @throws ResourceAdminServiceExceptionException
     *
     */
    @Test(groups = "wso2.greg", description = "Use addWsdl() method via remote registry")
    public void testAddWsdl() throws RegistryException, MalformedURLException, RemoteException,
                                     ResourceAdminServiceExceptionException {

        WsdlManager wsdlMgr = new WsdlManager(registry);
        Wsdl wsdl = wsdlMgr.newWsdl(WSDL_URL);
        wsdlMgr.addWsdl(wsdl);

        WSDLBean bean = listMetadataServiceClient.listWSDLs();

        String[] names = bean.getName();
        boolean wsdlStatus = false;
        for (String name : names) {
            if (name.contains(WSDL_NAME)) {
                wsdlStatus = true;
            }
        }
        assertTrue(wsdlStatus, "Wsdl not added");
    }

    /**
     * @throws GovernanceException
     * @throws RemoteException
     * @throws ResourceAdminServiceExceptionException
     *
     */
    @Test(groups = "wso2.greg", description = "Use addPolicy() method via remote registry")
    public void testAddPolicy() throws GovernanceException, RemoteException,
                                       ResourceAdminServiceExceptionException {

        PolicyManager policyMgr = new PolicyManager(registry);
        Policy policy = policyMgr.newPolicy(POLICY_URL);
        policyMgr.addPolicy(policy);

        PolicyBean policyBean = listMetadataServiceClient.listPolicies();

        String[] names = policyBean.getName();
        boolean policyStatus = false;
        for (String name : names) {
            if (name.contains(POLICY_NAME)) {
                policyStatus = true;
            }
        }
        assertTrue(policyStatus, "Policy not added");
    }

    /**
     * @throws Exception
     */
    @AfterClass()
    public void clear() throws Exception {
        PolicyBean policy = listMetadataServiceClient.listPolicies();
        String policyPathToDelete = "";
        for (String policyPath : policy.getPath()) {
            if (policyPath.contains(POLICY_NAME)) {
                policyPathToDelete = "/_system/governance/" + policyPath;
            }
        }
        if (resourceAdminServiceClient.getResource(policyPathToDelete) != null) {
            resourceAdminServiceClient.deleteResource(policyPathToDelete);
        }

        WSDLBean wsdl = listMetadataServiceClient.listWSDLs();
        String wsdlPathToDelete = "";
        for (String wsdlPath : wsdl.getPath()) {
            if (wsdlPath.contains(WSDL_NAME)) {
                wsdlPathToDelete = "/_system/governance/" + wsdlPath;
            }
        }
        if (wsRegistryServiceClient.resourceExists(wsdlPathToDelete)) {
            resourceAdminServiceClient.deleteResource(wsdlPathToDelete);
        }

        userInfo = null;
        registry = null;
        listMetadataServiceClient = null;
        resourceAdminServiceClient = null;
        wsRegistryServiceClient = null;
    }
}
