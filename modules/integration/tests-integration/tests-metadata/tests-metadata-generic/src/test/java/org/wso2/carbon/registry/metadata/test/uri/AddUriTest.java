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

package org.wso2.carbon.registry.metadata.test.uri;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import javax.xml.namespace.QName;
import java.io.FileNotFoundException;

import static org.testng.Assert.assertTrue;

public class AddUriTest extends GREGIntegrationBaseTest {

    private Registry governance;

    @BeforeClass(alwaysRun = true)
    public void initialize() throws Exception {
        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        super.init(TestUserMode.SUPER_TENANT_USER);
        WSRegistryServiceClient registry = registryProviderUtil.getWSRegistry(automationContext);
        governance = registryProviderUtil.getGovernanceRegistry(registry, automationContext);

    }

    @Test(groups = "wso2.greg", description = "Test URI of type wsdl/schema/service/policy ")
    public void testUri() throws RegistryException, FileNotFoundException, InterruptedException {

        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
        GenericArtifactManager artifactManager = new GenericArtifactManager(governance, "uri");
        GenericArtifact artifactWsdl = artifactManager.newGovernanceArtifact(new QName
                                                                             ("AmazonWebServices"));

        artifactWsdl.setAttribute("overview_uri", "https://svn.wso2.org/repos/wso2/carbon/platform/trunk/" +
                                                  "platform-integration/platform-automated-test-suite/org.wso2.carbon.automation.test.repo" +
                                                  "/src/main/resources/artifacts/GREG/wsdl/AmazonWebServices.wsdl");

        artifactWsdl.setAttribute("overview_type", "WSDL");
        artifactManager.addGenericArtifact(artifactWsdl);

        assertTrue(artifactWsdl.getAttribute("overview_uri").equals("https://svn.wso2.org/repos/wso2/carbon/platform/trunk/" +
                                                                    "platform-integration/platform-automated-test-suite/org.wso2.carbon.automation.test.repo" +
                                                                    "/src/main/resources/artifacts/GREG/" +
                                                                    "wsdl/AmazonWebServices.wsdl"), "artifact URI not found");
        assertTrue(artifactWsdl.getAttribute("overview_name").equals("AmazonWebServices"), "artifact name not found");
        assertTrue(artifactWsdl.getAttribute("overview_type").equals("WSDL"), "artifact WSDL not found");
        assertTrue(governance.resourceExists("uris/WSDL/AmazonWebServices.wsdl"), "AmazonWebServices.wsdl URI doesn't exists");


        GenericArtifact artifactSchema = artifactManager.newGovernanceArtifact(new QName
                                                                               ("purchasing"));

        artifactSchema.setAttribute("overview_uri", "https://svn.wso2.org/repos/wso2/carbon/platform/trunk/" +
                                                    "platform-integration/platform-automated-test-suite/org.wso2.carbon.automation.test.repo" +
                                                    "/src/main/resources/artifacts/GREG/xsd/purchasing.xsd");

        artifactSchema.setAttribute("overview_type", "XSD");
        artifactManager.addGenericArtifact(artifactSchema);

        assertTrue(artifactSchema.getAttribute("overview_uri").equals("https://svn.wso2.org/repos/wso2/carbon/platform/" +
                                                                      "trunk/platform-integration/platform-automated-test-suite/" +
                                                                      "org.wso2.carbon.automation.test.repo/src/main/resources/artifacts/" +
                                                                      "GREG/xsd/purchasing.xsd"), "artifact URI not found");
        assertTrue(artifactSchema.getAttribute("overview_name").equals("purchasing"), "artifact name not found");
        assertTrue(artifactSchema.getAttribute("overview_type").equals("XSD"), "artifact XSD not found");
        assertTrue(governance.resourceExists("uris/XSD/purchasing.xsd"), "purchasing.xsd URI doesn't exists");


        GenericArtifact artifactPolicy = artifactManager.newGovernanceArtifact(new QName
                                                                               ("policy"));

        artifactPolicy.setAttribute("overview_uri", "https://svn.wso2.org/repos/wso2/carbon/platform/trunk/" +
                                                    "platform-integration/platform-automated-test-suite/org.wso2.carbon.automation.test.repo" +
                                                    "/src/main/resources/artifacts/GREG/policy/policy.xml");
        artifactPolicy.setAttribute("overview_type", "Policy");
        artifactManager.addGenericArtifact(artifactPolicy);

        assertTrue(artifactPolicy.getAttribute("overview_uri").equals("https://svn.wso2.org/repos/wso2/carbon/platform/" +
                                                                      "trunk/platform-integration/platform-automated-test-suite/org.wso2.carbon.automation.test.repo/" +
                                                                      "src/main/resources/artifacts/" +
                                                                      "GREG/policy/policy.xml"), "artifact URI not found");
        assertTrue(artifactPolicy.getAttribute("overview_name").equals("policy"), "artifact name not found");
        assertTrue(artifactPolicy.getAttribute("overview_type").equals("Policy"), "artifact Policy not found");
        assertTrue(governance.resourceExists("uris/Policy/policy.xml"), "policy.xml URI doesn't exists");


        GenericArtifact artifactGeneric = artifactManager.newGovernanceArtifact(new QName
                                                                                ("resource.txt"));

        artifactGeneric.setAttribute("overview_uri", "https://svn.wso2.org/repos/wso2/carbon/platform/trunk/" +
                                                     "platform-integration/platform-automated-test-suite/org.wso2.carbon.automation.test.repo/src/main/resources/" +
                                                     "artifacts/GREG/resource.txt");
        artifactGeneric.setAttribute("overview_type", "Generic");
        artifactManager.addGenericArtifact(artifactGeneric);

        assertTrue(artifactGeneric.getAttribute("overview_uri").equals("https://svn.wso2.org/repos/wso2/carbon/platform/" +
                                                                       "trunk/platform-integration/platform-automated-test-suite/org.wso2.carbon.automation.test.repo/src/main/resources/artifacts/" +
                                                                       "GREG/resource.txt"), "artifact URI not found");
        assertTrue(artifactGeneric.getAttribute("overview_name").equals("resource.txt"), "artifact name not found");
        assertTrue(artifactGeneric.getAttribute("overview_type").equals("Generic"), "artifact Generic not found");
        assertTrue(governance.resourceExists("uris/Generic/resource.txt"), "resource.txt URI doesn't exists");


    }

    @Test(groups = "wso2.greg", description = "Test URI of type wsdl with ..", dependsOnMethods = "testUri")
    public void testUriWsdlWith() throws RegistryException, FileNotFoundException {

        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
        GenericArtifactManager artifactManager = new GenericArtifactManager(governance, "uri");
        GenericArtifact artifactWsdlWithWsdl = artifactManager.newGovernanceArtifact(new QName
                                                                                     ("Axis2Service_Wsdl_With_Wsdl_Imports"));

        artifactWsdlWithWsdl.setAttribute("overview_uri", "https://svn.wso2.org/repos/wso2/carbon/platform/trunk/" +
                                                          "platform-integration/platform-automated-test-suite/org.wso2.carbon.automation.test.repo/src/main/resources/artifacts/GREG/wsdl/" +
                                                          "Axis2Service_Wsdl_With_Wsdl_Imports.wsdl");
        artifactWsdlWithWsdl.setAttribute("overview_type", "WSDL");
        artifactManager.addGenericArtifact(artifactWsdlWithWsdl);

        assertTrue(artifactWsdlWithWsdl.getAttribute("overview_uri").equals("https://svn.wso2.org/repos/wso2/carbon/platform/" +
                                                                            "trunk/platform-integration/platform-automated-test-suite/org.wso2.carbon.automation.test.repo/src/main/resources/artifacts/" +
                                                                            "GREG/wsdl/Axis2Service_Wsdl_With_Wsdl_Imports.wsdl"), "artifact URI not found");
        assertTrue(artifactWsdlWithWsdl.getAttribute("overview_name").equals("Axis2Service_Wsdl_With_Wsdl_Imports"), "artifact name not found");
        assertTrue(artifactWsdlWithWsdl.getAttribute("overview_type").equals("WSDL"), "artifact type not found");
        assertTrue(governance.resourceExists("uris/WSDL/Axis2Service_Wsdl_With_Wsdl_Imports.wsdl"),
                   "Axis2Service_Wsdl_With_Wsdl_Imports.wsdl URI doesn't exists");
        assertTrue(governance.resourceExists("uris/WSDL/Axis2ImportedWsdl.wsdl"), "Dependency Axis2ImportedWsdl.wsdl doesn't exists");

        GenericArtifact artifactWsdlWithSchema = artifactManager.newGovernanceArtifact(new QName
                                                                                       ("wsdl_with_EncrOnlyAnonymous"));

        artifactWsdlWithSchema.setAttribute("overview_uri", "https://svn.wso2.org/repos/wso2/carbon/platform/trunk/" +
                                                            "platform-integration/platform-automated-test-suite/org.wso2.carbon.automation.test.repo/src/main/resources/artifacts/GREG/wsdl/wsdl_with_EncrOnlyAnonymous.wsdl");
        artifactWsdlWithSchema.setAttribute("overview_type", "WSDL");
        artifactManager.addGenericArtifact(artifactWsdlWithSchema);

        assertTrue(artifactWsdlWithSchema.getAttribute("overview_uri").equals("https://svn.wso2.org/repos/wso2/carbon/platform/" +
                                                                              "trunk/platform-integration/platform-automated-test-suite/org.wso2.carbon.automation.test.repo/src/main/resources/artifacts/GREG/wsdl/" +
                                                                              "wsdl_with_EncrOnlyAnonymous.wsdl"), "artifact URI not found");
        assertTrue(artifactWsdlWithSchema.getAttribute("overview_name").equals("wsdl_with_EncrOnlyAnonymous"), "artifact name not found");
        assertTrue(artifactWsdlWithSchema.getAttribute("overview_type").equals("WSDL"), "artifact type not found");
        assertTrue(governance.resourceExists("uris/WSDL/wsdl_with_EncrOnlyAnonymous.wsdl"), "wsdl_with_EncrOnlyAnonymous.wsdl URI doesn't exists");
        assertTrue(governance.resourceExists("uris/XSD/SampleSchema.xsd"), "Dependency SampleSchema.xsd doesn't exists");


        GenericArtifact artifactWsdlWitPolicy = artifactManager.newGovernanceArtifact(new QName
                                                                                      ("wsdl_with_SigEncr"));

        artifactWsdlWitPolicy.setAttribute("overview_uri", "https://svn.wso2.org/repos/wso2/carbon/platform/trunk/" +
                                                           "platform-integration/platform-automated-test-suite/org.wso2.carbon.automation.test.repo/src/main/resources/artifacts/GREG/wsdl/wsdl_with_SigEncr.wsdl");
        artifactWsdlWitPolicy.setAttribute("overview_type", "WSDL");
        artifactManager.addGenericArtifact(artifactWsdlWitPolicy);

        assertTrue(artifactWsdlWitPolicy.getAttribute("overview_uri").equals("https://svn.wso2.org/repos/wso2/carbon/platform/trunk/" +
                                                                             "platform-integration/platform-automated-test-suite/org.wso2.carbon.automation.test.repo/src/main/resources/artifacts/GREG/wsdl/" +
                                                                             "wsdl_with_SigEncr.wsdl"), "artifact URI not found");
        assertTrue(artifactWsdlWitPolicy.getAttribute("overview_name").equals("wsdl_with_SigEncr"), "artifact name not found");
        assertTrue(artifactWsdlWitPolicy.getAttribute("overview_type").equals("WSDL"), "artifact type not found");
        assertTrue(governance.resourceExists("uris/WSDL/wsdl_with_SigEncr.wsdl"), "wsdl_with_EncrOnlyAnonymous.wsdl URI doesn't exists");
        assertTrue(governance.resourceExists("uris/Policy/EncrOnlyAnonymous.xml"), "Dependency EncrOnlyAnonymous.xml doesn't exists");
        assertTrue(governance.resourceExists("uris/Policy/SgnEncrAnonymous.xml"), "Dependency SgnEncrAnonymous.xml doesn't exists");

        String[] allGenericArtifacts = artifactManager.getAllGenericArtifactIds();
        for (String genericArtifacts : allGenericArtifacts) {
            artifactManager.removeGenericArtifact(genericArtifacts);

        }

    }

    @Test(groups = "wso2.greg", description = "Test URI of type schema with schema", dependsOnMethods = "testUriWsdlWith")
    public void testUriSchemaWithSchema() throws RegistryException, FileNotFoundException {

        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
        GenericArtifactManager artifactManager = new GenericArtifactManager(governance, "uri");
        GenericArtifact artifactSchemaWithSchema = artifactManager.newGovernanceArtifact(new QName
                                                                                         ("SchemaImportSample"));

        artifactSchemaWithSchema.setAttribute("overview_uri", "https://svn.wso2.org/repos/wso2/carbon/platform/trunk/" +
                                                              "platform-integration/platform-automated-test-suite/org.wso2.carbon.automation.test.repo/" +
                                                              "src/main/resources/artifacts/GREG/schema/SchemaImportSample.xsd");
        artifactSchemaWithSchema.setAttribute("overview_type", "XSD");
        artifactManager.addGenericArtifact(artifactSchemaWithSchema);

        assertTrue(artifactSchemaWithSchema.getAttribute("overview_uri").equals("https://svn.wso2.org/repos/wso2/carbon/" +
                                                                                "platform/trunk/platform-integration/platform-automated-test-suite/" +
                                                                                "org.wso2.carbon.automation.test.repo/src/main/resources/" +
                                                                                "artifacts/" + "GREG/schema/SchemaImportSample.xsd"),
                   "artifact URI not found");
        assertTrue(artifactSchemaWithSchema.getAttribute("overview_name").equals("SchemaImportSample"), "artifact name not found");
        assertTrue(artifactSchemaWithSchema.getAttribute("overview_type").equals("XSD"), "artifact type not found");
        assertTrue(governance.resourceExists("uris/XSD/SchemaImportSample.xsd"), "SchemaImportSample.xsd URI doesn't exists");
        assertTrue(governance.resourceExists("uris/XSD/LinkedSchema.xsd"), "Dependency LinkedSchema.xsd doesn't exists");
        artifactManager.removeGenericArtifact(artifactSchemaWithSchema.getId());

        String[] allGenericArtifacts = artifactManager.getAllGenericArtifactIds();
        for (String genericArtifacts : allGenericArtifacts) {
            artifactManager.removeGenericArtifact(genericArtifacts);

        }
    }

    @Test(groups = "wso2.greg", description = "Test invalid URIs",
          dependsOnMethods = "testUriSchemaWithSchema", expectedExceptions = RegistryException.class)
    public void testInvalidUriWsdl() throws RegistryException, FileNotFoundException {

        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
        GenericArtifactManager artifactManager = new GenericArtifactManager(governance, "uri");
        GenericArtifact artifact = artifactManager.newGovernanceArtifact(new QName
                                                                         ("AutomatedInvalidWSDL"));

        artifact.setAttribute("overview_uri", "https://svn.wso2.org/repos/wso2/carbon/platform/trunk/" +
                                              "platform-integration/platform-automated-test-suite/org.wso2.carbon.automation.test.repo/" +
                                              "src/main/resources/artifacts/GREG/wsdl/AutomatedInvalidWSDL.wsdl");
        artifact.setAttribute("overview_type", "WSDL");
        artifactManager.addGenericArtifact(artifact);


    }

    @Test(groups = "wso2.greg", description = "Test invalid URIs", dependsOnMethods =
            "testInvalidUriWsdl", expectedExceptions = RegistryException.class)
    public void testInvalidUriSchema() throws RegistryException, FileNotFoundException {

        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
        GenericArtifactManager artifactManager = new GenericArtifactManager(governance, "uri");
        GenericArtifact artifact = artifactManager.newGovernanceArtifact(new QName
                                                                         ("XmlInvalidSchema"));

        artifact.setAttribute("overview_uri", "https://svn.wso2.org/repos/wso2/carbon/platform/trunk/" +
                                              "platform-integration/platform-automated-test-suite/org.wso2.carbon.automation.test.repo/" +
                                              "src/main/resources/artifacts/GREG/schema/XmlInvalidSchema.xsd");
        artifact.setAttribute("overview_type", "XSD");
        artifactManager.addGenericArtifact(artifact);


    }

    @Test(groups = "wso2.greg", description = "Test invalid URIs",
          dependsOnMethods = "testInvalidUriSchema", expectedExceptions = RegistryException.class)
    public void testInvalidUriPolicy() throws RegistryException, FileNotFoundException {

        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
        GenericArtifactManager artifactManager = new GenericArtifactManager(governance, "uri");
        GenericArtifact artifact = artifactManager.newGovernanceArtifact(new QName
                                                                         ("invlidPolicy"));

        artifact.setAttribute("overview_uri", "https://svn.wso2.org/repos/wso2/carbon/platform/trunk/" +
                                              "platform-integration/platform-automated-test-suite/org.wso2.carbon.automation.test.repo/" +
                                              "src/main/resources/artifacts/GREG/policy/invlidPolicy.xml");
        artifact.setAttribute("overview_type", "Policy");
        artifactManager.addGenericArtifact(artifact);


    }

    @Test(groups = "wso2.greg", description = "Test invalid URIs", dependsOnMethods = "testInvalidUriPolicy", expectedExceptions = RegistryException.class)
    public void testNonExistingUrl() throws RegistryException, FileNotFoundException {

        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
        GenericArtifactManager artifactManager = new GenericArtifactManager(governance, "uri");
        GenericArtifact artifact = artifactManager.newGovernanceArtifact(new QName
                                                                         ("nonExistingUrl"));

        artifact.setAttribute("overview_uri", "https://svn.wso2.org/repos/wso2/carbon/platform/trunk/" +
                                              "platform-integration/platform-automated-test-suite/org.wso2.carbon.automation.test.repo/" +
                                              "src/main/resources/artifacts/wsdl/AmazonWebServices.wsdl");
        artifact.setAttribute("overview_type", "WSDL");
        artifactManager.addGenericArtifact(artifact);

        String[] allGenericArtifacts = artifactManager.getAllGenericArtifactIds();
        for (String genericArtifacts : allGenericArtifacts) {
            artifactManager.removeGenericArtifact(genericArtifacts);

        }
    }


    @AfterClass
    private void RemoveArtifacts() throws RegistryException {

        ServiceManager serviceManager = new ServiceManager(governance);
        Service[] services = serviceManager.getAllServices();
        for (Service s : services) {
            if (s.getQName().getLocalPart().equals("AmazonSearchService") | s.getQName().getLocalPart().equals
                    ("Axis2Service") | s.getQName().getLocalPart().equals("SimpleStockQuoteService1M")) {
                serviceManager.removeService(s.getId());
            }
        }
	
        governance.delete("trunk/endpoints");
        governance.delete("uris");
        governance = null;

    }
}
