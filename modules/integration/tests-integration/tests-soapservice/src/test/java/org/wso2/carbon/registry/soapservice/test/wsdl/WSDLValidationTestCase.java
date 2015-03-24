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
package org.wso2.carbon.registry.soapservice.test.wsdl;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.governance.api.endpoints.dataobjects.Endpoint;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.info.stub.RegistryExceptionException;
import org.wso2.carbon.registry.metadata.test.util.RegistryConstants;
import org.wso2.carbon.registry.properties.stub.PropertiesAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.properties.stub.beans.xsd.PropertiesBean;
import org.wso2.carbon.registry.properties.stub.utils.xsd.Property;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.beans.xsd.ResourceTreeEntryBean;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.clients.PropertiesAdminServiceClient;
import org.wso2.greg.integration.common.clients.RelationAdminServiceClient;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import javax.activation.DataHandler;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;

/**
 * This class used to add WSDL files in to the governance registry using resource-admin command.
 */
public class WSDLValidationTestCase extends GREGIntegrationBaseTest {

    private PropertiesAdminServiceClient propertiesAdminServiceClient;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private GenericArtifactManager artifactManager;
    private WsdlManager wsdlManager;
    private Registry governance;
    private final String wsdlPath = "/_system/governance/trunk/wsdls/net/restfulwebservices/www/servicecontracts"
            + "/_2008/_01/1.0.0/GeoIPService.svc.wsdl";

    /**
     * THis method used to init the wsdl validation test cases.
     *
     * @throws Exception
     */
    @BeforeClass(groups = { "wso2.greg" })
    public void init() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String sessionCookie = new LoginLogoutClient(automationContext).login();

        propertiesAdminServiceClient = new PropertiesAdminServiceClient(backendURL, sessionCookie);
        resourceAdminServiceClient = new ResourceAdminServiceClient(backendURL, sessionCookie);
        WSRegistryServiceClient wsRegistryServiceClient = new RegistryProviderUtil().getWSRegistry(automationContext);

        governance = new RegistryProviderUtil().getGovernanceRegistry(wsRegistryServiceClient, automationContext);
        artifactManager = new GenericArtifactManager(governance, "soapservice");
        wsdlManager = new WsdlManager(governance);
    }

    /**
     * This method act as the wsdl addition test case.
     *
     * @throws MalformedURLException
     * @throws RemoteException
     * @throws ResourceAdminServiceExceptionException
     * @throws RegistryExceptionException
     */
    @Test(groups = { "wso2.greg" })
    public void addWSDL() throws MalformedURLException, RemoteException, ResourceAdminServiceExceptionException,
            RegistryExceptionException {
        boolean isFound = false;

        String resource = getTestArtifactLocation() + "artifacts" + File.separator + "GREG" + File.separator
                + "wsdl" + File.separator + "sample.wsdl";

        resourceAdminServiceClient.addResource("/_system/governance/trunk/wsdls/sample.wsdl",
                RegistryConstants.APPLICATION_WSDL_XML, "txtDesc",
                new DataHandler(new URL("file:///" + resource)));
        resourceAdminServiceClient.importResource("/_system/governance/trunk/wsdls", "GeoIPService.svc.wsdl",
                RegistryConstants.APPLICATION_WSDL_XML, "txtDesc", "https://svn.wso2"
                        + ".org/repos/wso2/carbon/platform/trunk/products/greg/modules/integration/registry/tests"
                        + "-metadata/src/test/resources/artifacts/GREG/wsdl/GeoIPService/GeoIPService.svc.wsdl", null);
        ResourceTreeEntryBean searchFileOne = resourceAdminServiceClient.getResourceTreeEntryBean
                ("/_system/governance/trunk/wsdls/eu/dataaccess/footballpool/1.0.0");
        ResourceTreeEntryBean searchFileTwo = resourceAdminServiceClient.getResourceTreeEntryBean
                ("/_system/governance/trunk/wsdls/net/restfulwebservices/www/servicecontracts/_2008/_01/1.0.0");

        String[] resourceChildOne = searchFileOne.getChildren();
        for (int childCount = 0; childCount <= resourceChildOne.length; childCount++) {
            if (resourceChildOne[childCount]
                    .equalsIgnoreCase("/_system/governance/trunk/wsdls/eu/dataaccess/footballpool/1.0.0/sample.wsdl")) {
                isFound = true;
                break;
            }
        }
        assertTrue(isFound);

        isFound = false;
        String[] resourceChildTwo = searchFileTwo.getChildren();
        for (int childCount = 0; childCount <= resourceChildTwo.length; childCount++) {
            if (resourceChildTwo[childCount].equalsIgnoreCase(wsdlPath)) {
                isFound = true;
                break;
            }
        }
        assertTrue(isFound);
    }

    /**
     * This method act as the wsdl validation test case.
     * wsdlValidationTestCase have two different of test-cases.
     * <ul>
     * <li>Adding wsdl from local file system.</li>
     * <li>Adding wsdl from global url.</li>
     * </ul>
     *
     * @throws PropertiesAdminServiceRegistryExceptionException
     * @throws RemoteException
     */
    @Test(groups = { "wso2.greg" }, dependsOnMethods = { "addWSDL" })
    public void wsdlValidationTestCase() throws PropertiesAdminServiceRegistryExceptionException, RemoteException {
        PropertiesBean propertiesBean = propertiesAdminServiceClient.getProperty(wsdlPath, "yes");
        Property[] property = propertiesBean.getProperties();
        for (int i = 0; i <= property.length - 1; i++) {
            if (property[i].getKey().equalsIgnoreCase("WSDL Validation")) {
                assertTrue(property[i].getValue().equalsIgnoreCase("valid"),
                        "WSDL validation not matched with expected result");
            }
        }
    }

    /**
     * This method act as the test case for adding a property for the validated test case.
     *
     * @throws PropertiesAdminServiceRegistryExceptionException
     * @throws RemoteException
     */
    @Test(groups = { "wso2.greg" }, dependsOnMethods = "wsdlValidationTestCase")
    public void addPropertyTest() throws PropertiesAdminServiceRegistryExceptionException, RemoteException {
        propertiesAdminServiceClient.setProperty(wsdlPath, "TestProperty", "sample-value");
        PropertiesBean propertiesBean = propertiesAdminServiceClient.getProperty(wsdlPath, "yes");
        Property[] property = propertiesBean.getProperties();
        for (int i = 0; i <= property.length - 1; i++) {
            if (property[i].getKey().equalsIgnoreCase("TestProperty")) {
                assertTrue(property[i].getValue().equalsIgnoreCase("sample-value"), "Newly added property not found");
            }
        }

    }

    /**
     * This method act as the cleaning process after validation test cases.
     *
     * @throws ResourceAdminServiceExceptionException
     * @throws RemoteException
     * @throws RegistryException
     */
    @AfterClass(groups = { "wso2.greg" })
    public void deleteResources() throws ResourceAdminServiceExceptionException, RemoteException, RegistryException {
        Endpoint[] endpoints = null;
        Endpoint[] endPointsOther = null;

        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);

        Wsdl[] wsdls = wsdlManager.getAllWsdls();
        for (Wsdl wsdl : wsdls) {
            if (wsdl.getQName().getLocalPart().equals("sample.wsdl")) {
                endpoints = wsdlManager.getWsdl(wsdl.getId()).getAttachedEndpoints();
            } else if (wsdl.getQName().getLocalPart().equals("GeoIPService.svc.wsdl")) {
                endPointsOther = wsdlManager.getWsdl(wsdl.getId()).getAttachedEndpoints();
            }
        }

        resourceAdminServiceClient
                .deleteResource("/_system/governance/trunk/wsdls/eu/dataaccess/footballpool/1.0.0/sample.wsdl");
        resourceAdminServiceClient.deleteResource(wsdlPath);

        for (Endpoint path : endpoints) {
            resourceAdminServiceClient.deleteResource("_system/governance/" + path.getPath());
        }
        for (Endpoint path : endPointsOther) {
            resourceAdminServiceClient.deleteResource("_system/governance/" + path.getPath());
        }

        for (GenericArtifact artifact : artifactManager.getAllGenericArtifacts()) {
            if (artifact.getAttribute("overview_name").equals("GeoIPService")){
                artifactManager.removeGenericArtifact(artifact.getId());
            } else if (artifact.getAttribute("overview_name").equals("Info")) {
                artifactManager.removeGenericArtifact(artifact.getId());
            }
        }
    }
}
