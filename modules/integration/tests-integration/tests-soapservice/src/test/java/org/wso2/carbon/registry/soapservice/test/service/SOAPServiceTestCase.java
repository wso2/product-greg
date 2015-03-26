/*                                                                             
 * Copyright 2004,2005 The Apache Software Foundation.                         
 *                                                                             
 * Licensed under the Apache License, Version 2.0 (the "License");             
 * you may not use this file except in compliance with the License.            
 * You may obtain a copy of the License at                                     
 *                                                                             
 *      http://www.apache.org/licenses/LICENSE-2.0                             
 *                                                                             
 * Unless required by applicable law or agreed to in writing, software         
 * distributed under the License is distributed on an "AS IS" BASIS,           
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.    
 * See the License for the specific language governing permissions and         
 * limitations under the License.                                              
 */
package org.wso2.carbon.registry.soapservice.test.service;

import org.apache.axis2.AxisFault;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.governance.api.endpoints.dataobjects.Endpoint;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.beans.xsd.LifecycleBean;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.info.stub.RegistryExceptionException;
import org.wso2.carbon.registry.info.stub.beans.utils.xsd.Comment;
import org.wso2.carbon.registry.info.stub.beans.utils.xsd.Tag;
import org.wso2.carbon.registry.info.stub.beans.xsd.CommentBean;
import org.wso2.carbon.registry.info.stub.beans.xsd.RatingBean;
import org.wso2.carbon.registry.info.stub.beans.xsd.TagBean;
import org.wso2.carbon.registry.metadata.test.util.RegistryConstants;
import org.wso2.carbon.registry.relations.stub.AddAssociationRegistryExceptionException;
import org.wso2.carbon.registry.relations.stub.beans.xsd.AssociationTreeBean;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.beans.xsd.ResourceTreeEntryBean;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.clients.InfoServiceAdminClient;
import org.wso2.greg.integration.common.clients.LifeCycleAdminServiceClient;
import org.wso2.greg.integration.common.clients.RelationAdminServiceClient;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.*;

/**
 * This class used to add WSDL files in to the governance registry using resource-admin command in the purpose for
 * wsdl addition test cases.
 */
public class SOAPServiceTestCase extends GREGIntegrationBaseTest {

    private GenericArtifactManager artifactManager;
    private Registry governance;

    /**
     * This method used to init the wsdl addition test cases.
     *
     * @throws Exception
     */
    @BeforeClass(groups = { "wso2.greg" })
    public void init() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        WSRegistryServiceClient wsRegistryServiceClient = new RegistryProviderUtil().getWSRegistry(automationContext);

        governance = new RegistryProviderUtil().getGovernanceRegistry(wsRegistryServiceClient, automationContext);
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance,
                                                GovernanceUtils.findGovernanceArtifactConfigurations(governance));
        artifactManager = new GenericArtifactManager(governance, "soapservice");

    }

    @Test(groups = { "wso2.greg" }, description = "create SOAP Service using GenericArtifact")
    public void createSOAPService() throws GovernanceException {
        GenericArtifact artifact = artifactManager.newGovernanceArtifact(new QName("SOAPService1"));

        artifact.setAttribute("overview_name", "SOAPService1");
        artifact.setAttribute("overview_version", "4.5.0");
        artifact.setAttribute("overview_description", "Description");

        artifactManager.addGenericArtifact(artifact);

        GenericArtifact receivedArtifact = artifactManager.getGenericArtifact(artifact.getId());
        assertEquals(artifact.getAttribute("overview_name"), receivedArtifact.getAttribute("overview_name"),
                     " Service name must be equal");

        artifactManager.removeGenericArtifact(artifact.getId());
    }

    @Test(groups = { "wso2.greg" }, description = "try add SOAP service without version",
          dependsOnMethods = "createSOAPService", expectedExceptions = GovernanceException.class)
    public void createSOAPServiceFault() throws GovernanceException {
        GenericArtifact artifact = artifactManager.newGovernanceArtifact(new QName("SOAPService2"));

        artifact.setAttribute("overview_name", "SOAPService2");
        artifact.setAttribute("overview_description", "Description");

        artifactManager.addGenericArtifact(artifact);

        GenericArtifact receivedArtifact = artifactManager.getGenericArtifact(artifact.getId());
        assertEquals(artifact.getAttribute("overview_name"), receivedArtifact.getAttribute("overview_name"),
                     " Service name must be equal");
    }

    @Test(groups = { "wso2.greg" }, description = "try add SOAP service without version",
          dependsOnMethods = "createSOAPServiceFault")
    public void createSOAPServiceWithWSDL() throws GovernanceException {
        GenericArtifact artifact =
                artifactManager.newGovernanceArtifact(new QName("http://com.wso2.sample", "SOAPService2"));

        artifact.setAttribute("overview_name", "SOAPService2");
        artifact.setAttribute("overview_version", "4.5.0");
        artifact.setAttribute("overview_namespace", "com.wso2.sample");
        artifact.setAttribute("interface_wsdlURL",
                              "http://graphical.weather.gov/xml/SOAP_server/ndfdXMLserver.php?wsdl");
        artifact.setAttribute("overview_description", "Description");

        artifactManager.addGenericArtifact(artifact);

        GenericArtifact receivedArtifact = artifactManager.getGenericArtifact(artifact.getId());
        assertNotNull(receivedArtifact.getDependencies());
        assertEquals(receivedArtifact.getDependencies().length, 2, "Expecting 2 dependencies : WSDL and Endpoint");

        artifactManager.removeGenericArtifact(artifact.getId());
    }

    /**
     * This method act as the test case cleaning process after the wsdl test case.
     *
     * @throws org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException
     * @throws java.rmi.RemoteException
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
     */
    @AfterClass(groups = { "wso2.greg" })
    public void deleteResources() throws ResourceAdminServiceExceptionException, RemoteException, RegistryException {
        artifactManager = null;

    }
}
