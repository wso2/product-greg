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

package org.wso2.carbon.registry.metadata.test.custom;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.om.xpath.AXIOMXPath;
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
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;


public class CRUDOperationsRxt {

    int userId = 2;
    private Registry governance;
    private ManageEnvironment environment;
    private UserInfo userinfo;

    @BeforeClass
    public void initialize()
            throws LoginAuthenticationExceptionException, RemoteException, RegistryException {

        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        WSRegistryServiceClient registry = registryProviderUtil.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        governance = registryProviderUtil.getGovernanceRegistry(registry, userId);
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        environment = builder.build();
        userinfo = UserListCsvReader.getUserInfo(userId);
    }

    @Test(groups = "wso2.greg", description = "Upload Person.rxt")
    public void testRxtUpload()
            throws RegistryException, IOException, ResourceAdminServiceExceptionException,
                   LogoutAuthenticationExceptionException {


        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator +
                              "GREG" + File.separator + "rxt" + File.separator + "person.rxt";

        Resource rxt = governance.newResource();
        rxt.setContentStream(new FileInputStream(new File(resourcePath)));
        rxt.setMediaType("application/vnd.wso2.registry-ext-type+xml");

        if (governance.resourceExists("repository/components/org.wso2.carbon.governance/types/person.rxt")) {
            governance.delete("repository/components/org.wso2.carbon.governance/types/person.rxt");
        }
        governance.put("repository/components/org.wso2.carbon.governance/types/person.rxt", rxt);
        assertTrue(governance.resourceExists("repository/components/org.wso2.carbon.governance/types/person.rxt"),
                   "rxt resource doesn't exists");
        AuthenticatorClient authenticatorClient = new AuthenticatorClient(environment.getGreg().getBackEndUrl());
        authenticatorClient.logOut();

    }

    @Test(groups = "wso2.greg", description = "Add custom Artifact/test CRUD", dependsOnMethods = "testRxtUpload")
    public void testRxtArtifact()
            throws XMLStreamException, LoginAuthenticationExceptionException, RemoteException,
                   JaxenException, RegistryException, MalformedURLException {

        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        environment = builder.build();
        AuthenticatorClient authenticatorClient =
                new AuthenticatorClient(environment.getGreg().getBackEndUrl());

        authenticatorClient.login(userinfo.getUserName(), userinfo.getPassword(),
                                  environment.getGreg().getProductVariables().getHostName());

        AuthenticationAdminStub stub = (AuthenticationAdminStub) authenticatorClient.getAuthenticationAdminStub();

        ServiceClient client = stub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);

        boolean result = stub.login(userinfo.getUserName(), userinfo.getPassword(),
                                    environment.getGreg().getProductVariables().getHostName());
        EnvironmentBuilder env = new EnvironmentBuilder();
        FrameworkSettings framework = env.getFrameworkSettings();
        if (result) {
            String eprPerson;
            if (framework.getEnvironmentSettings().is_runningOnStratos()) {
                eprPerson = (environment.getGreg().getServiceUrl() + "/Person").replaceAll("^http?://", "https://");
                eprPerson = eprPerson.replace(Integer.toString(new URL(eprPerson).getPort()),
                                        environment.getGreg().getProductVariables().getHttpsPort());

            } else {
                eprPerson = (environment.getGreg().getBackEndUrl() + "Person");
            }

            options.setTo(new EndpointReference(eprPerson));
            options.setAction("urn:addPerson");
            options.setManageSession(true);
            OMElement omElement = client.sendReceive(AXIOMUtil.stringToOM("<ser:addPerson " +
                                                                          "xmlns:ser=\"http://services.add.person.governance.carbon.wso2.org\"><ser:info>&lt;metadata " +
                                                                          "xmlns=\"http://www.wso2.org/governance/metadata\">&lt;overview>" +
                                                                          "&lt;id>person_id&lt;/id>&lt;title>Mr.&lt;/title>&lt;name>person_name&lt;/name>" +
                                                                          "&lt;/overview>&lt;/metadata></ser:info></ser:addPerson>"));


            AXIOMXPath expression = new AXIOMXPath("//ns:return");
            expression.addNamespace("ns", omElement.getNamespace().getNamespaceURI());
            String artifactId = ((OMElement) expression.selectSingleNode(omElement)).getText();


            GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
            GenericArtifactManager artifactManager = new GenericArtifactManager(governance, "person");
            String[] allPersonGenericArtifacts = artifactManager.getAllGenericArtifactIds();

            assertEquals(isGenericArtifactExists(allPersonGenericArtifacts, artifactId), true);

            options.setAction("urn:getPerson");
            client.sendReceive(AXIOMUtil.stringToOM("<ser:getPerson " +
                                                    "xmlns:ser=\"http://services.get.person.governance.carbon.wso2.org\"><ser:artifactId>" + artifactId
                                                    + "</ser:artifactId></ser:getPerson>"));


            options.setAction("urn:getPersonArtifactIDs");
            client.sendReceive(AXIOMUtil.stringToOM("<ser:getPersonArtifactIDs " +
                                                    "" +
                                                    "xmlns:ser=\"http://services.get.person.artifactids.governance.carbon.wso2.org\"/>"));


            options.setAction("urn:getPersonDependencies");
            client.sendReceive(AXIOMUtil.stringToOM
                    ("<ser:getPersonDependencies" +
                     " " +
                     "xmlns:ser=\"http://services.get.person.dependencies.governance.carbon.wso2" +
                     ".org\"><ser:artifactId>" + artifactId + "</ser:artifactId></ser:getPersonDependencies>"));


            options.setAction("urn:deletePerson");
            client.setOptions(options);
            OMElement omElementDeleteWsdl = client.sendReceive(AXIOMUtil.stringToOM("<ser:deletePerson " +
                                                                                    "xmlns:ser=\"http://services" +
                                                                                    ".delete.person.governance.carbon.wso2.org\"><ser:artifactId>" + artifactId +
                                                                                    "</ser:artifactId></ser:deletePerson>"));
            assertEquals(omElementDeleteWsdl.toString(), "<ns:deletePersonResponse xmlns:ns=\"http://services.delete" +
                                                         ".person" +
                                                         ".governance.carbon.wso2.org\"><ns:return>true</ns:return></ns:deletePersonResponse>");

            String[] allGenericArtifacts = artifactManager.getAllGenericArtifactIds();
            for (String genericArtifacts : allGenericArtifacts) {
                artifactManager.removeGenericArtifact(genericArtifacts);

            }
        }
    }

    public boolean isGenericArtifactExists(String[] allPersonGenericArtifacts, String artifactId) {

        for (String personArtifacts : allPersonGenericArtifacts) {
            if (personArtifacts.equals(artifactId)) {
                return true;
            }

        }
        return false;
    }

    @AfterClass
    public void cleanResources() {
        governance = null;
        environment = null;
    }

}
