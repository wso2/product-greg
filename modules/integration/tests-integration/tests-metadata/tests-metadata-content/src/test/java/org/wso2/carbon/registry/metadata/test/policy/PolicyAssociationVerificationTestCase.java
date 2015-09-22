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
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.registry.relations.stub.AddAssociationRegistryExceptionException;
import org.wso2.carbon.registry.relations.stub.beans.xsd.AssociationTreeBean;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.greg.integration.common.clients.RelationAdminServiceClient;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;

import javax.activation.DataHandler;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;

/**
 * this classes uses Admin services to do so
 */
public class PolicyAssociationVerificationTestCase extends GREGIntegrationBaseTest {

    private ResourceAdminServiceClient resourceAdminServiceClient;
    private String session;

    @BeforeClass(groups = "wso2.greg", alwaysRun = true)
    public void initialize() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_USER);
        session = new LoginLogoutClient(automationContext).login();
        resourceAdminServiceClient = new ResourceAdminServiceClient(automationContext.getContextUrls().getBackEndUrl(), session);
    }

    @Test(groups = "wso2.greg", description = "Policy addition for association Verification")
    public void testAddResourcesToVerifyAssociation() throws RemoteException, MalformedURLException, ResourceAdminServiceExceptionException {
        String resourcePath = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator + "GREG" + File.separator + "policy" + File.separator + "policy.xml"; // the path
        String associationResourcePath = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator + "GREG" + File.separator + "policy" + File.separator + "UTPolicy.xml"; // the path
        DataHandler dhReso = new DataHandler(new URL("file:///" + resourcePath));
        DataHandler dhAsso = new DataHandler(new URL("file:///" + associationResourcePath));
        resourceAdminServiceClient.addPolicy("Policy add from the the file system", dhReso);
        resourceAdminServiceClient.addPolicy("Policy add from the the file system", dhAsso);
    }

    @Test(groups = "wso2.greg", description = "Association Verification",
            dependsOnMethods = "testAddResourcesToVerifyAssociation")
    public void testVerifyAssociation() throws RemoteException, MalformedURLException, ResourceAdminServiceExceptionException, AddAssociationRegistryExceptionException, XPathExpressionException {
        RelationAdminServiceClient relationAdminServiceClient = new RelationAdminServiceClient(automationContext.getContextUrls().getBackEndUrl(), session);
        relationAdminServiceClient.addAssociation("/_system/governance/trunk/policies/1.0.0/policy.xml", "usedBy", "/_system/governance/trunk/policies/1.0.0/UTPolicy.xml", "add");
        AssociationTreeBean associationTreeBean = relationAdminServiceClient.getAssociationTree("/_system/governance/trunk/policies/1.0.0/policy.xml", "association");
        assertTrue(associationTreeBean.getAssociationTree().contains("/_system/governance/trunk/policies/1.0.0/UTPolicy.xml"));
    }

    @AfterClass(groups = "wso2.greg", alwaysRun = true, description = "cleaning up the artifacts added")
    public void tearDown() throws GovernanceException, RemoteException, ResourceAdminServiceExceptionException {
        resourceAdminServiceClient.deleteResource("/_system/governance/trunk/policies/1.0.0/UTPolicy.xml");
        resourceAdminServiceClient.deleteResource("/_system/governance/trunk/policies/1.0.0/policy.xml");
        resourceAdminServiceClient = null;
    }
}
