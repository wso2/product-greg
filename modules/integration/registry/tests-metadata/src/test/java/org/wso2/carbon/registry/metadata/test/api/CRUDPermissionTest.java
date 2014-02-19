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
import org.wso2.carbon.automation.api.clients.authenticators.AuthenticatorClient;
import org.wso2.carbon.automation.api.clients.user.mgt.UserManagementClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkSettings;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import javax.xml.stream.XMLStreamException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.assertEquals;

public class CRUDPermissionTest {


    int userId = 2;
    private Registry governance;
    private ManageEnvironment environment;
    private UserInfo userinfo;
    private AuthenticationAdminStub stub;
    private AuthenticatorClient authenticatorClient;
    private UserManagementClient userManagementClient;
    private String artifactId;
    private UserInfo userInfo;
    private String eprAPI;

    @BeforeClass(alwaysRun = true)
    public void initialize()
            throws Exception {
        userInfo = UserListCsvReader.getUserInfo(userId);
        EnvironmentBuilder builderAdmin = new EnvironmentBuilder().greg(ProductConstant.ADMIN_USER_ID);
        ManageEnvironment adminEnvironment = builderAdmin.build();

        userManagementClient = new UserManagementClient(adminEnvironment.getGreg().getBackEndUrl(),
                                                        adminEnvironment.getGreg().getSessionCookie());

        userManagementClient.updateUserListOfRole(ProductConstant.DEFAULT_PRODUCT_ROLE, new String[]{},
                                                  new String[]{userInfo.getUserNameWithoutDomain()});

        if (!userManagementClient.roleNameExists("testRole2")) {
            userManagementClient.addRole("testRole2",
                                         new String[]{userInfo.getUserNameWithoutDomain()},
                                         new String[]{"/permission/admin/login"});
        }


        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        WSRegistryServiceClient registry = registryProviderUtil.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        governance = registryProviderUtil.getGovernanceRegistry(registry, userId);
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        environment = builder.build();
        userinfo = UserListCsvReader.getUserInfo(userId);
        authenticatorClient =
                new AuthenticatorClient(environment.getGreg().getBackEndUrl());

        authenticatorClient.login(userinfo.getUserName(), userinfo.getPassword(),
                                  environment.getGreg().getProductVariables().getHostName());

        stub = (AuthenticationAdminStub) authenticatorClient.getAuthenticationAdminStub();

    }

    @Test(groups = "wso2.greg", description = "Add/get/delete API Artifact (CRUD)",
          expectedExceptions = AxisFault.class)
    public void testAPIArtifactDenyPermission()
            throws XMLStreamException, LoginAuthenticationExceptionException, RemoteException,
                   JaxenException, RegistryException, LogoutAuthenticationExceptionException,
                   MalformedURLException {

        ServiceClient client = stub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        EnvironmentBuilder env = new EnvironmentBuilder();
        FrameworkSettings framework = env.getFrameworkSettings();

        if (framework.getEnvironmentSettings().is_runningOnStratos()) {
            eprAPI = (environment.getGreg().getServiceUrl() + "/API").replaceAll("^http?://", "https://");
            eprAPI = eprAPI.replace(Integer.toString(new URL(eprAPI).getPort()),
                                    environment.getGreg().getProductVariables().getHttpsPort());

        } else {
            eprAPI = (environment.getGreg().getBackEndUrl() + "API");
        }
        options.setTo(new EndpointReference(eprAPI));
        options.setAction("urn:addAPI");
        options.setManageSession(true);
        OMElement omElement =
                client.sendReceive(AXIOMUtil.stringToOM("<ser:addAPI " + "xmlns:ser=\"http://services.add.api.governance.carbon.wso2.org\"><ser:info>&lt;metadata " +
                                                        "xmlns=\"http://www.wso2.org/governance/metadata\">&lt;overview>&lt;status>CREATED&lt;" +
                                                        "/status>&lt;context>API_Context&lt;/context>" +
                                                        "&lt;name>API_Name2&lt;/name>" + "&lt;version>1.2.3&lt;/version>" +
                                                        "&lt;tier>Gold&lt;/tier>" + "&lt;isLatest>false&lt;/isLatest>" +
                                                        "&lt;provider>API_Povider&lt;/provider>" +
                                                        "&lt;/overview>&lt;/metadata></ser:info></ser:addAPI>"));


    }


    @Test(groups = "wso2.greg", description = "Add/get/delete API Artifact (CRUD)",
          dependsOnMethods = "testAPIArtifactDenyPermission")
    public void testAPIArtifact()
            throws Exception {

        userManagementClient.updateUserListOfRole("testRole2", new String[]{},
                                                  new String[]{userInfo.getUserNameWithoutDomain()});
        userManagementClient.addRole("testRole3",
                                     new String[]{userInfo.getUserNameWithoutDomain()},
                                     new String[]{"/permission/admin/login",
                                                  "/permission/admin/manage/resources"});

        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        environment = builder.build();
        userinfo = UserListCsvReader.getUserInfo(userId);
        authenticatorClient =
                new AuthenticatorClient(environment.getGreg().getBackEndUrl());

        authenticatorClient.login(userinfo.getUserName(), userinfo.getPassword(),
                                  environment.getGreg().getProductVariables().getHostName());

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

    @Test(groups = "wso2.greg", description = "Add/get/delete API Artifact (CRUD)",
          dependsOnMethods = "testAPIArtifact", expectedExceptions = AxisFault.class)
    public void testAPIArtifactDeleteDeniedPermission()
            throws Exception,
                   JaxenException, RegistryException {

        userManagementClient.updateUserListOfRole("testRole3", new String[]{},
                                                  new String[]{userinfo.getUserNameWithoutDomain()});
        userManagementClient.addRole("testRole4",
                                     new String[]{userinfo.getUserNameWithoutDomain()},
                                     new String[]{"/permission/admin/login"});

        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        environment = builder.build();
        userinfo = UserListCsvReader.getUserInfo(userId);
        authenticatorClient =
                new AuthenticatorClient(environment.getGreg().getBackEndUrl());

        authenticatorClient.login(userinfo.getUserName(), userinfo.getPassword(),
                                  environment.getGreg().getProductVariables().getHostName());

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

//    @Test(groups = "wso2.greg", description = "get/delete API from CRUD ",  //not possible at the moment
//            dependsOnMethods = "testAPIArtifactDeleteDeniedPermission")
//    public void testAPIArtifactDelete()
//            throws Exception,
//            JaxenException, RegistryException {
//
//        userManagementClient.updateUserListOfRole("testRole4", new String[]{},
//                new String[]{"testuser1"});
//        userManagementClient.addRole("testRole5",
//                new String[]{"testuser1"}, new String[]{"/permission/admin/login",
//                "/permission/admin/manage/resources"});
//
//        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
//        environment = builder.build();
//        userinfo = UserListCsvReader.getUserInfo(userId);
//        authenticatorClient =
//                new AuthenticatorClient(environment.getGreg().getBackEndUrl());
//
//        authenticatorClient.login(userinfo.getUserName(), userinfo.getPassword(),
//                environment.getGreg().getProductVariables().getHostName());
//
//        stub = (AuthenticationAdminStub) authenticatorClient.getAuthenticationAdminStub();
//
//        ServiceClient client = stub._getServiceClient();
//        Options options = client.getOptions();
//        options.setManageSession(true);
//
//
//        options.setAction("urn:getAPI");
//        client.sendReceive(AXIOMUtil.stringToOM("<ser:getAPI " +
//                "xmlns:ser=\"http://services.get.api.governance.carbon.wso2.org\"><ser:artifactId>" + artifactId
//                + "</ser:artifactId></ser:getAPI>"));
//
//
//        options.setAction("urn:deleteAPI");
//        client.setOptions(options);
//        OMElement omElementDeleteWsdl = client.sendReceive(AXIOMUtil.stringToOM("<ser:deleteAPI " +
//                "xmlns:ser=\"http://services" +
//                ".delete.api.governance.carbon.wso2.org\"><ser:artifactId>" + artifactId +
//                "</ser:artifactId></ser:deleteAPI>"));
//
//        assertEquals(omElementDeleteWsdl.toString(), "<ns:deleteAPIResponse xmlns:ns=\"http://services.delete.api" +
//                ".governance.carbon.wso2.org\"><ns:return>true</ns:return></ns:deleteAPIResponse>");
//
//
//        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
//        GenericArtifactManager artifactManager = new GenericArtifactManager(governance, "api");
//        String[] allGenericArtifacts = artifactManager.getAllGenericArtifactIds();
//        for (String genericArtifacts : allGenericArtifacts) {
//            artifactManager.removeGenericArtifact(genericArtifacts);
//
//        }
//
//    }

    @AfterClass
    public void DeleteRolesAndRestoreTestUser1() throws Exception {

        userManagementClient.updateUserListOfRole("testRole", new String[]{userinfo.getUserNameWithoutDomain()},
                                                  new String[]{});

        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
        GenericArtifactManager artifactManager = new GenericArtifactManager(governance, "api");
        String[] allGenericArtifacts = artifactManager.getAllGenericArtifactIds();
        for (String genericArtifacts : allGenericArtifacts) {
            artifactManager.removeGenericArtifact(genericArtifacts);

        }

        userManagementClient.deleteRole("testRole2");
        userManagementClient.deleteRole("testRole3");
        userManagementClient.deleteRole("testRole4");

        governance = null;
        environment = null;
        authenticatorClient = null;
        userManagementClient = null;
    }

    public boolean isGenericArtifactExists(String[] allApiGenericArtifacts, String artifactId) {

        for (String apiArtifacts : allApiGenericArtifacts) {
            if (apiArtifacts.equals(artifactId)) {
                return true;
            }

        }
        return false;
    }

}
