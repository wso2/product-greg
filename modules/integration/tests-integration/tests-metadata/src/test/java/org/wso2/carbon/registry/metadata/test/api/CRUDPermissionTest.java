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

package org.wso2.carbon.registry.metadata.test.api;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.jaxen.JaxenException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.authenticator.stub.LogoutAuthenticationExceptionException;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.carbon.integration.common.admin.client.UserManagementClient;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import javax.xml.stream.XMLStreamException;
import javax.xml.xpath.XPathExpressionException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;

import static org.testng.Assert.assertEquals;

public class CRUDPermissionTest extends GREGIntegrationBaseTest {

    private Registry governance;
    private AuthenticationAdminStub stub;
    private AuthenticatorClient authenticatorClient;
    private UserManagementClient userManagementClient;
    private String artifactId;
    private String eprAPI;
    private String userName = "CRUDPermissionTestUser";
    private String password = "CRUDPermissionTestUserPassword";

    @BeforeClass (alwaysRun = true)
    public void initialize () throws Exception {

        super.init(TestUserMode.SUPER_TENANT_USER);
        String sessionCookie = new LoginLogoutClient(automationContext).login();

        userManagementClient =
                new UserManagementClient(automationContext.getContextUrls().getBackEndUrl(),
                        sessionCookie);

        userManagementClient.addUser(userName, password, null, null);

        addTestRole("testRole2", new String[]{userName}, new String[]{"/permission/admin/login"});
        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();

        WSRegistryServiceClient WSRegistry = registryProviderUtil.getWSRegistry(automationContext);

        governance = registryProviderUtil.getGovernanceRegistry(WSRegistry, automationContext);

        authenticatorClient =
                new AuthenticatorClient(automationContext.getContextUrls().getBackEndUrl());

        authenticatorClient.login(userName, password,
                automationContext.getDefaultInstance().getHosts().get("default"));

        stub = (AuthenticationAdminStub) authenticatorClient.getAuthenticationAdminStub();
    }

    @Test (groups = "wso2.greg", description = "Add/get/delete API Artifact (CRUD)",
            expectedExceptions = AxisFault.class)
    public void testAPIArtifactDenyPermission ()
            throws XMLStreamException, LoginAuthenticationExceptionException, RemoteException,
            JaxenException, RegistryException, LogoutAuthenticationExceptionException,
            MalformedURLException, XPathExpressionException {

        ServiceClient client = stub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);

        eprAPI = getBackendURL() + "API";

        options.setTo(new EndpointReference(eprAPI));
        options.setAction("urn:addAPI");
        options.setManageSession(true);
        OMElement omElement =
                client.sendReceive(AXIOMUtil.stringToOM("<ser:addAPI "
                        + "xmlns:ser=\"http://services.add.api.governance.carbon.wso2.org\"><ser:info>&lt;metadata " +
                        "xmlns=\"http://www.wso2.org/governance/metadata\">&lt;overview>&lt;status>CREATED&lt;" +
                        "/status>&lt;context>API_Context&lt;/context>" +
                        "&lt;name>API_Name2&lt;/name>" + "&lt;version>1.2.3&lt;/version>" +
                        "&lt;tier>Gold&lt;/tier>" + "&lt;isLatest>false&lt;/isLatest>" +
                        "&lt;provider>API_Povider&lt;/provider>" +
                        "&lt;/overview>&lt;/metadata></ser:info></ser:addAPI>"));

    }

    @Test (groups = "wso2.greg", description = "Add/get/delete API Artifact (CRUD)",
            dependsOnMethods = "testAPIArtifactDenyPermission")
    public void testAPIArtifact () throws Exception {

        userManagementClient.updateUserListOfRole("testRole2", new String[]{},
                new String[]{userName});

        userManagementClient.addRole("testRole3",
                new String[]{userName},
                new String[]{"/permission/admin/login",
                        "/permission/admin/manage/resources"});

        authenticatorClient =
                new AuthenticatorClient(automationContext.getContextUrls().getBackEndUrl());

        authenticatorClient.login(userName,password,
                automationContext.getDefaultInstance().getHosts().get("default"));

        stub = (AuthenticationAdminStub) authenticatorClient.getAuthenticationAdminStub();
        ServiceClient client = stub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        options.setTo(new EndpointReference(eprAPI));
        options.setAction("urn:addAPI");
        options.setManageSession(true);

        OMElement omElement = client.sendReceive(AXIOMUtil.stringToOM("<ser:addAPI " +
                "xmlns:ser=\"http://services.add.api.governance.carbon.wso2.org\"><ser:info>&lt;metadata " +
                "xmlns=\"http://www.wso2.org/governance/metadata\">&lt;overview>&lt;status>CREATED&lt;" +
                "/status>&lt;context>API_Context&lt;/context>" +
                "&lt;name>API_Name&lt;/name>" + "&lt;version>1.2.3&lt;/version>" +
                "&lt;tier>Gold&lt;/tier>" + "&lt;isLatest>false&lt;/isLatest>" +
                "&lt;provider>API_Povider&lt;/provider>" +
                "&lt;/overview>&lt;/metadata></ser:info></ser:addAPI>"));

        AXIOMXPath expression = new AXIOMXPath("//ns:return");
        expression.addNamespace("ns", omElement.getNamespace().getNamespaceURI());
        artifactId = ((OMElement) expression.selectSingleNode(omElement)).getText();
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
        GenericArtifactManager artifactManager = new GenericArtifactManager(governance, "api");
        String[] allApiGenericArtifacts = artifactManager.getAllGenericArtifactIds();
        assertEquals(isGenericArtifactExists(allApiGenericArtifacts, artifactId), true);

    }

    @Test (groups = "wso2.greg", description = "Add/get/delete API Artifact (CRUD)",
            dependsOnMethods = "testAPIArtifact", expectedExceptions = AxisFault.class)
    public void testAPIArtifactDeleteDeniedPermission () throws Exception {

        userManagementClient.updateUserListOfRole("testRole3", new String[]{},
                new String[]{userName});

        authenticatorClient =
                new AuthenticatorClient(automationContext.getContextUrls().getBackEndUrl());

        authenticatorClient.login(automationContext.getContextTenant().getContextUser().getUserName(),
                automationContext.getContextTenant().getContextUser().getPassword(),
                automationContext.getDefaultInstance().getHosts().get("default"));

        stub = (AuthenticationAdminStub) authenticatorClient.getAuthenticationAdminStub();
        ServiceClient client = stub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        options.setAction("urn:getAPI");
        client.sendReceive(AXIOMUtil.stringToOM("<ser:getAPI " +
                "xmlns:ser=\"http://services.get.api.governance.carbon.wso2.org\"><ser:artifactId>" + artifactId
                + "</ser:artifactId></ser:getAPI>"));
        options.setAction("urn:deleteAPI");
        client.setOptions(options);
        OMElement omElementDeleteWsdl =
                client.sendReceive(AXIOMUtil.stringToOM("<ser:deleteAPI " +
                        "xmlns:ser=\"http://services" +
                        ".delete.api.governance.carbon.wso2.org\"><ser:artifactId>" + artifactId +
                        "</ser:artifactId></ser:deleteAPI>"));

    }

    private void addTestRole (String roleName, String[] userList, String[] permission) throws Exception {

        if (!userManagementClient.roleNameExists(roleName)) {
            userManagementClient.addRole(roleName, userList, permission);
        }
    }

    @AfterClass(alwaysRun = true)
    public void DeleteRolesAndRestoreTestUser1 () throws Exception {

        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
        GenericArtifactManager artifactManager = new GenericArtifactManager(governance, "api");
        String[] allGenericArtifacts = artifactManager.getAllGenericArtifactIds();
        for (String genericArtifacts : allGenericArtifacts) {
            artifactManager.removeGenericArtifact(genericArtifacts);

        }
        userManagementClient.deleteRole("testRole2");
        userManagementClient.deleteRole("testRole3");
        userManagementClient.deleteUser(userName);
    }

    public boolean isGenericArtifactExists (String[] allApiGenericArtifacts, String artifactId) {

        for (String apiArtifacts : allApiGenericArtifacts) {
            if (apiArtifacts.equals(artifactId)) {
                return true;
            }

        }
        return false;
    }

}
