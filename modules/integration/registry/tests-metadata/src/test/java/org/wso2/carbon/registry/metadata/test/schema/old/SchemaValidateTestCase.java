package org.wso2.carbon.registry.metadata.test.schema.old;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.registry.PropertiesAdminServiceClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.registry.metadata.test.util.RegistryConsts;
import org.wso2.carbon.registry.properties.stub.beans.xsd.PropertiesBean;
import org.wso2.carbon.registry.properties.stub.utils.xsd.Property;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;

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

public class SchemaValidateTestCase {

    private static final Log log = LogFactory.getLog(SchemaValidateTestCase.class);
    private String schemaPath = "/_system/governance/trunk/schemas/";
    private PropertiesAdminServiceClient propertiesAdminServiceClient;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private ManageEnvironment environment;
    private UserInfo userInfo;


    @BeforeClass(groups = {"wso2.greg"})
    public void init() throws Exception {
        log.info("Initializing Add Schema Resource Tests");
        log.debug("Add Add Schema Resource Initialised");
        int userId = 0;
        userInfo = UserListCsvReader.getUserInfo(userId);
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        environment = builder.build();
        propertiesAdminServiceClient =
                new PropertiesAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                                 environment.getGreg().getSessionCookie());
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                               environment.getGreg().getSessionCookie());
    }


    /**
     * Check schema validation status for correctness.
     */
    @Test(groups = {"wso2.greg"})
    public void addValidSchemaTest()
            throws Exception, RemoteException, ResourceAdminServiceExceptionException {
        String resourceUrl =
                "http://svn.wso2.org/repos/wso2/trunk/commons/qa/qa-artifacts/greg/xsd/Patient.xsd";
        String resourceName = "Patient.xsd";
        resourceAdminServiceClient.importResource(schemaPath + resourceName, resourceName,
                                                  RegistryConsts.APPLICATION_X_XSD_XML, "schemaFile", resourceUrl, null);
        assertTrue(validateProperties(schemaPath + "org/ihc/xsd/" + resourceName, "Schema Validation",
                                      "Valid"), "Schema validation status incorrect");
        assertTrue(validateProperties(schemaPath + "org/ihc/xsd/" + resourceName, "targetNamespace",
                                      "http://ihc.org/xsd?patient"), "Target namespace not found");
        String textContent = resourceAdminServiceClient.getTextContent(schemaPath +
                                                                       "org/ihc/xsd/" + resourceName);
        assertTrue(!textContent.equals(null));
        //delete the added resource
        resourceAdminServiceClient.deleteResource(schemaPath +
                                                  "org/ihc/xsd/" + resourceName);
        //check if the deleted file exists in registry
        assertTrue(!isResourceExist(environment.getGreg().getSessionCookie(),
                                    schemaPath + "org/ihc/xsd/", resourceName, resourceAdminServiceClient));
    }

    /**
     * Check schema properties to find out whether the schema validation status is valid or invalid
     */
    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"addValidSchemaTest"})
    public void addInvalidSchemaTest()
            throws Exception, RemoteException, ResourceAdminServiceExceptionException {
        String resourceUrl =
//                "http://ww2.wso2.org/~charitha/xsds/calculator-no-element-name-invalid.xsd";
                "https://svn.wso2.org/repos/wso2/trunk/commons/qa/qa-artifacts/greg/xsd/calculator-no-element-name-invalid.xsd";
        String resourceName = "calculator-no-element-name-invalid.xsd";
        resourceAdminServiceClient.importResource(schemaPath + resourceName, resourceName,
                                                  RegistryConsts.APPLICATION_X_XSD_XML, "schemaFile", resourceUrl, null);
        assertTrue(validateProperties(schemaPath + "org/charitha/" +
                                      resourceName, "Schema Validation", "Invalid"), "Schema validation status incorrect");
        assertTrue(validateProperties(schemaPath + "org/charitha/" +
                                      resourceName, "targetNamespace", "http://charitha.org/"), "Target namespace not found");
        assertTrue(validateProperties(schemaPath + "org/charitha/" +
                                      resourceName, "Schema Validation Message 1", "Error: s4s-att-must-appear: Attribute 'name' must " +
                                                                                   "appear in element 'element'."), "Schema validation error not found");
        assertNotNull(resourceAdminServiceClient.getTextContent(schemaPath +
                                                                "org/charitha/" + resourceName));
        //delete the added resource
        resourceAdminServiceClient.deleteResource(schemaPath +
                                                  "org/charitha/" + resourceName);
        //check if the deleted file exists in registry
        assertTrue(!isResourceExist(environment.getGreg().getSessionCookie(), schemaPath +
                                                                              "org/charitha/", resourceName, resourceAdminServiceClient));

    }

    /**
     * Add Schemas to registry using a zip file, validation status of all uploaded files checked.
     */
    @Test(groups = {"wso2.greg.schema.b"}, dependsOnMethods = {"addInvalidSchemaTest"})
    public void addCompressSchemaFile()
            throws Exception, ResourceAdminServiceExceptionException, RemoteException {
        String resourceName = "registry-new.zip";
        String resource = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator +
                          "GREG" + File.separator + "zip" + File.separator + "registry-new.zip";
        resourceAdminServiceClient.addResource(schemaPath + resourceName,
                                               org.wso2.carbon.registry.metadata.test.util.RegistryConsts.APPLICATION_WSO2_GOVERNANCE_ARCHIVE, "schemaFile",
                                               new DataHandler(new URL("file:///" + resource)));
        assertTrue(validateProperties(schemaPath +
                                      "dk/dr/www/namespaces/schemas/application/mas/whatson/production/production.xsd",
                                      "Schema Validation", "Valid"), "Schema validation status incorrect");
        assertTrue(validateProperties(schemaPath +
                                      "dk/dr/www/namespaces/schemas/application/mas/whatson/production/production.xsd",
                                      "targetNamespace", "http://www.dr.dk/namespaces/schemas/application/mas/whatson/production"),
                   "Target namespace not found");
        assertTrue(validateProperties(schemaPath +
                                      "dk/dr/www/namespaces/schemas/common/types/types.xsd",
                                      "targetNamespace", "http://www.dr.dk/namespaces/schemas/common/types"),
                   "Target namespace not found");
        assertNotNull(resourceAdminServiceClient.getTextContent(schemaPath +
                                                                "dk/dr/www/namespaces/schemas/common/types/types.xsd"));
        assertTrue(isResourceExist(environment.getGreg().getSessionCookie(), schemaPath +
                                                                             "dk/dr/www/namespaces/schemas/common/types", "types.xsd", resourceAdminServiceClient));
        assertTrue(isResourceExist(environment.getGreg().getSessionCookie(), schemaPath +
                                                                             "dk/dr/www/namespaces/schemas/application/mas/whatson/production", "production.xsd",
                                   resourceAdminServiceClient));
        //check if the deleted file exists in registry
        assertTrue(!(isResourceExist(environment.getGreg().getSessionCookie(), schemaPath +
                                                                               "dk/dr/www/namespaces/schemas/common/types", "types.xsd", resourceAdminServiceClient) &&
                     !isResourceExist(environment.getGreg().getSessionCookie(), schemaPath +
                                                                                "dk/dr/www/namespaces/schemas/application/mas/whatson/production", "production.xsd",
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
                                                  "dk/dr/www/namespaces/schemas/common/types/types.xsd");
        resourceAdminServiceClient.deleteResource(schemaPath +
                                                  "dk/dr/www/namespaces/schemas/application/mas/whatson/production/production.xsd");
    }
}
