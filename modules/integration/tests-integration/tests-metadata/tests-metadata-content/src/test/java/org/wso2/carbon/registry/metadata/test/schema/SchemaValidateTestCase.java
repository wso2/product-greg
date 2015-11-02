package org.wso2.carbon.registry.metadata.test.schema;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.registry.metadata.test.util.RegistryConstants;
import org.wso2.carbon.registry.properties.stub.beans.xsd.PropertiesBean;
import org.wso2.carbon.registry.properties.stub.utils.xsd.Property;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.greg.integration.common.clients.PropertiesAdminServiceClient;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;

import javax.activation.DataHandler;
import java.io.File;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.wso2.carbon.registry.metadata.test.util.TestUtils.isResourceExist;


/**
 * A test case which tests registry Schema validate operation
 */

public class SchemaValidateTestCase extends GREGIntegrationBaseTest{

    private static final Log log = LogFactory.getLog(SchemaValidateTestCase.class);
    private String schemaPath = "/_system/governance/trunk/schemas/";
    private PropertiesAdminServiceClient propertiesAdminServiceClient;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private String sessionCookie;


    @BeforeClass(groups = {"wso2.greg"})
    public void init() throws Exception {
        log.info("Initializing Add Schema Resource Tests");
        log.debug("Add Add Schema Resource Initialised");
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        sessionCookie = new LoginLogoutClient(automationContext).login();

        propertiesAdminServiceClient =
                new PropertiesAdminServiceClient(backendURL, sessionCookie);
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(backendURL, sessionCookie);
    }


    /**
     * Check schema validation status for correctness.
     */
    @Test(groups = {"wso2.greg"})
    public void addValidSchemaTest()
            throws Exception, RemoteException, ResourceAdminServiceExceptionException {
        String resourceUrl =
                "https://raw.githubusercontent.com/wso2/wso2-qa-artifacts/master/automation-artifacts/greg/xsd/Patient.xsd";
        String resourceName = "Patient.xsd";
        resourceAdminServiceClient.importResource(schemaPath + resourceName, resourceName,
                                                  RegistryConstants.APPLICATION_X_XSD_XML,
                "schemaFile", resourceUrl, null);
        assertTrue(validateProperties(schemaPath + "org/ihc/xsd/1.0.0/" + resourceName, "Schema Validation",
                                      "Valid"), "Schema validation status incorrect");
        assertTrue(validateProperties(schemaPath + "org/ihc/xsd/1.0.0/" + resourceName, "targetNamespace",
                                      "http://ihc.org/xsd?patient"), "Target namespace not found");
        String textContent = resourceAdminServiceClient.getTextContent(schemaPath +
                                                                       "org/ihc/xsd/1.0.0/" + resourceName);
        assertTrue(!textContent.equals(null));
        //delete the added resource
        resourceAdminServiceClient.deleteResource(schemaPath +
                                                  "org/ihc/xsd/1.0.0/" + resourceName);
        //check if the deleted file exists in registry
        assertTrue(!isResourceExist(sessionCookie,
                                    schemaPath + "org/ihc/xsd/1.0.0/", resourceName, resourceAdminServiceClient));
    }

    /**
     * Check schema properties to find out whether the schema validation status is valid or invalid
     */
    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"addValidSchemaTest"})
    public void addInvalidSchemaTest()
            throws Exception, RemoteException, ResourceAdminServiceExceptionException {
        String resourceUrl =
//                "http://ww2.wso2.org/~charitha/xsds/calculator-no-element-name-invalid.xsd";
                "https://raw.githubusercontent.com/wso2/wso2-qa-artifacts/master/automation-artifacts/greg/" +
                        "xsd/calculator-no-element-name-invalid.xsd";
        String resourceName = "calculator-no-element-name-invalid.xsd";
        resourceAdminServiceClient.importResource(schemaPath + resourceName, resourceName,
                                                  RegistryConstants.APPLICATION_X_XSD_XML,
                "schemaFile", resourceUrl, null);
        assertTrue(validateProperties(schemaPath + "org/charitha/1.0.0/" +
                                      resourceName, "Schema Validation", "Invalid"), "Schema validation status incorrect");
        assertTrue(validateProperties(schemaPath + "org/charitha/1.0.0/" +
                                      resourceName, "targetNamespace", "http://charitha.org/"), "Target namespace not found");
        assertTrue(validateProperties(schemaPath + "org/charitha/1.0.0/" +
                                      resourceName, "Schema Validation Message 1", "Error: s4s-att-must-appear: Attribute 'name' must " +
                                                                                   "appear in element 'element'."), "Schema validation error not found");
        assertNotNull(resourceAdminServiceClient.getTextContent(schemaPath +
                                                                "org/charitha/1.0.0/" + resourceName));
        //delete the added resource
        resourceAdminServiceClient.deleteResource(schemaPath +
                                                  "org/charitha/1.0.0/" + resourceName);
        //check if the deleted file exists in registry
        assertTrue(!isResourceExist(sessionCookie, schemaPath +
                                                                              "org/charitha/1.0.0/", resourceName, resourceAdminServiceClient));

    }

    /**
     * Add Schemas to registry using a zip file, validation status of all uploaded files checked.
     */
    @Test(groups = {"wso2.greg.schema.b"}, dependsOnMethods = {"addInvalidSchemaTest"})
    public void addCompressSchemaFile()
            throws Exception, ResourceAdminServiceExceptionException, RemoteException {
        String resourceName = "registry-new.zip";
        String resource = getTestArtifactLocation() + "artifacts" + File.separator +
                          "GREG" + File.separator + "zip" + File.separator + "registry-new.zip";
        resourceAdminServiceClient.addResource(schemaPath + resourceName,
                                               org.wso2.carbon.registry.metadata.test.util.RegistryConstants.APPLICATION_WSO2_GOVERNANCE_ARCHIVE, "schemaFile",
                                               new DataHandler(new URL("file:///" + resource)));
        assertTrue(validateProperties(schemaPath +
                                      "dk/dr/www/namespaces/schemas/application/mas/whatson/production/1.0.0/production.xsd",
                                      "Schema Validation", "Valid"), "Schema validation status incorrect");
        assertTrue(validateProperties(schemaPath +
                                      "dk/dr/www/namespaces/schemas/application/mas/whatson/production/1.0.0/production.xsd",
                                      "targetNamespace", "http://www.dr.dk/namespaces/schemas/application/mas/whatson/production"),
                   "Target namespace not found");
        assertTrue(validateProperties(schemaPath +
                                      "dk/dr/www/namespaces/schemas/common/types/1.0.0/types.xsd",
                                      "targetNamespace", "http://www.dr.dk/namespaces/schemas/common/types"),
                   "Target namespace not found");
        assertNotNull(resourceAdminServiceClient.getTextContent(schemaPath +
                                                                "dk/dr/www/namespaces/schemas/common/types/1.0.0/types.xsd"));
        assertTrue(isResourceExist(sessionCookie, schemaPath +
                                                                             "dk/dr/www/namespaces/schemas/common/types/1.0.0", "types.xsd", resourceAdminServiceClient));
        assertTrue(isResourceExist(sessionCookie, schemaPath +
                                                                             "dk/dr/www/namespaces/schemas/application/mas/whatson/production/1.0.0", "production.xsd",
                                   resourceAdminServiceClient));
        //check if the deleted file exists in registry
        assertTrue(!(isResourceExist(sessionCookie, schemaPath +
                                                                               "dk/dr/www/namespaces/schemas/common/types/1.0.0", "types.xsd", resourceAdminServiceClient) &&
                     !isResourceExist(sessionCookie, schemaPath +
                                                                                "dk/dr/www/namespaces/schemas/application/mas/whatson/production/1.0.0", "production.xsd",
                                      resourceAdminServiceClient)));
    }


    public boolean validateProperties(String resourcePath, String key, String value) {
        boolean validationState = false;
        try {
            PropertiesBean propertiesBean = propertiesAdminServiceClient.getProperty(resourcePath, "yes");
            Property[] property = propertiesBean.getProperties();
            for (int i = 0; i <= property.length - 1; i++) {
                if (property[i].getKey().equalsIgnoreCase(key) && property[i].getValue().equalsIgnoreCase(value)) {
                    validationState = true;
                    log.info("Property key and value found");
                }
            }
        } catch (Exception e) {
            log.error("Error on finding resource properties : " + e);
            throw new RuntimeException("Error on finding resource properties : " + e);
        }
        return validationState;
    }


    @AfterClass(groups = "wso2.greg")
    public void deleteResources() throws ResourceAdminServiceExceptionException, RemoteException {

        resourceAdminServiceClient.deleteResource(schemaPath +
                                                  "dk/dr/www/namespaces/schemas/common/types/1.0.0/types.xsd");
        resourceAdminServiceClient.deleteResource(schemaPath +
                                                  "dk/dr/www/namespaces/schemas/application/mas/whatson/production/1.0.0/production.xsd");
    }
}
