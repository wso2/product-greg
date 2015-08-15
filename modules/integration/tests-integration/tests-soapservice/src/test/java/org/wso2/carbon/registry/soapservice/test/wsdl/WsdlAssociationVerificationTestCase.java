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

package org.wso2.carbon.registry.soapservice.test.wsdl;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.registry.relations.stub.AddAssociationRegistryExceptionException;
import org.wso2.carbon.registry.relations.stub.beans.xsd.AssociationTreeBean;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.greg.integration.common.clients.RelationAdminServiceClient;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.xml.sax.SAXException;

import javax.activation.DataHandler;
import javax.xml.stream.XMLStreamException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;

/**
 * this classes uses Admin services to do so
 */
public class WsdlAssociationVerificationTestCase extends GREGIntegrationBaseTest {

    private ResourceAdminServiceClient resourceAdminServiceClient;
    private final String amazonWsdlPath =
            "/_system/governance/trunk/wsdls/com/amazon/soap/1.0.0/AmazonWebServices.wsdl";
    private final String automatedWsdlPath =
            "/_system/governance/trunk/wsdls/com/strikeiron/www/1.0.0/Automated.wsdl";
    private final String associatedServicePathAutomated =
            "/_system/governance/trunk/soapservices/com/strikeiron/www/1.0.0/DoNotCallRegistry";
    private final String associatedEndpointPathAutomated =
            "/_system/governance/trunk/endpoints/ep-com.strikeiron.ws.strikeiron.donotcall2_5-DoNotCallRegistry";
    private final String associatedServicePathAmazon =
            "/_system/governance/trunk/soapservices/com/amazon/soap/1.0.0/AmazonSearchService";
    private final String associatedEndpointPathAmazon =
            "/_system/governance/trunk/endpoints/ep-com.amazon.soap.onca-soap2";
    private String sessionCookie;

    @BeforeClass (groups = "wso2.greg", alwaysRun = true)
    public void initialize () throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        sessionCookie = new LoginLogoutClient(automationContext).login();
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(backendURL, sessionCookie);
    }

    @Test (groups = "wso2.greg", description = "wsdl addition for association Verification")
    public void testAddResourcesToVerifyAssociation () throws RemoteException,
            MalformedURLException,
            ResourceAdminServiceExceptionException {

        String resourcePath = getTestArtifactLocation()
                + "artifacts" + File.separator + "GREG" + File.separator
                + "wsdl" + File.separator + "AmazonWebServices.wsdl"; // the
        // path
        String associationResourcePath = getTestArtifactLocation()
                + "artifacts" + File.separator + "GREG"
                + File.separator + "wsdl" + File.separator + "Automated.wsdl"; // the path
        DataHandler dhReso = new DataHandler(new URL("file:///" + resourcePath));
        DataHandler dhAsso = new DataHandler(new URL("file:///"
                + associationResourcePath));
        resourceAdminServiceClient.addWSDL("wsdl add from the the file system",
                dhReso);
        resourceAdminServiceClient.addWSDL("wsdl add from the the file system",
                dhAsso);

    }

    @Test (groups = "wso2.greg", description = "Association Verification",
            dependsOnMethods = "testAddResourcesToVerifyAssociation")
    public void testVerifyAssociation () throws RemoteException,
            MalformedURLException,
            ResourceAdminServiceExceptionException,
            AddAssociationRegistryExceptionException {

        RelationAdminServiceClient relationAdminServiceClient = new RelationAdminServiceClient(
                backendURL, sessionCookie);
        relationAdminServiceClient
                .addAssociation(
                        "/_system/governance/trunk/wsdls/com/amazon/soap/1.0.0/AmazonWebServices.wsdl",
                        "usedBy",
                        "/_system/governance/trunk/wsdls/com/strikeiron/www/1.0.0/Automated.wsdl",
                        "add");
        AssociationTreeBean associationTreeBean = relationAdminServiceClient
                .getAssociationTree(
                        "/_system/governance/trunk/wsdls/com/amazon/soap/1.0.0/AmazonWebServices.wsdl",
                        "association");
        assertTrue(associationTreeBean
                .getAssociationTree()
                .contains(
                        "/_system/governance/trunk/soapservices/com/amazon/soap/1.0.0/AmazonSearchService"));
        assertTrue(associationTreeBean
                .getAssociationTree()
                .contains(
                        "/_system/governance/trunk/wsdls/com/strikeiron/www/1.0.0/Automated.wsdl"));

    }

    @AfterClass (groups = "wso2.greg", alwaysRun = true, description = "cleaning up the artifacts added")
    public void tearDown () throws GovernanceException, RemoteException,
            ResourceAdminServiceExceptionException {

        resourceAdminServiceClient.deleteResource(associatedServicePathAmazon);
        resourceAdminServiceClient
                .deleteResource(amazonWsdlPath);
        resourceAdminServiceClient.deleteResource(associatedEndpointPathAmazon);
        resourceAdminServiceClient.deleteResource(associatedServicePathAutomated);
        resourceAdminServiceClient
                .deleteResource(automatedWsdlPath);
        resourceAdminServiceClient.deleteResource(associatedEndpointPathAutomated);
        resourceAdminServiceClient = null;
    }
}
