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

package org.wso2.carbon.registry.metadata.test.schema;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.core.TestTemplate;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.registry.metadata.test.util.RegistryConsts;
import org.wso2.carbon.registry.metadata.test.util.TestUtils;
import org.wso2.carbon.registry.properties.stub.PropertiesAdminServiceStub;
import org.wso2.carbon.registry.properties.stub.beans.xsd.PropertiesBean;
import org.wso2.carbon.registry.properties.stub.utils.xsd.Property;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceStub;

import static org.testng.Assert.*;

import javax.activation.DataHandler;
import java.io.File;
import java.net.URL;

import static org.wso2.carbon.registry.metadata.test.util.TestUtils.isResourceExist;


/**
 * A test case which tests registry Schema validate operation
 */

public class SchemaValidateTestCase {

    private static final Log log = LogFactory.getLog(SchemaValidateTestCase.class);
    private String schemaPath = "/_system/governance/trunk/schemas/";
    private ResourceAdminServiceStub resourceAdminServiceStub;
    private PropertiesAdminServiceStub propertiesAdminServiceStub;
    private String loggedInSessionCookie = "";
    private LoginLogoutUtil util = new LoginLogoutUtil();
    private String frameworkPath = "";


    @BeforeClass(groups = {"wso2.greg.schema.b"})
    public void init() throws Exception {
        log.info("Initializing Add Schema Resource Tests");
        log.debug("Add Add Schema Resource Initialised");
        loggedInSessionCookie = util.login();
        frameworkPath = FrameworkSettings.getFrameworkPath();
        log.debug("Running SuccessCase");
        resourceAdminServiceStub = TestUtils.getResourceAdminServiceStub(loggedInSessionCookie);
        propertiesAdminServiceStub = TestUtils.getPropertiesAdminServiceStub(loggedInSessionCookie);

    }

//    @Test(groups = {"wso2.greg"})
//    public void runSuccessCase() {
//        log.debug("Running SuccessCase");
//        resourceAdminServiceStub = TestUtils.getResourceAdminServiceStub(loggedInSessionCookie);
//        propertiesAdminServiceStub = TestUtils.getPropertiesAdminServiceStub(loggedInSessionCookie);
//        addValidSchemaTest();
//        addInvalidSchemaTest();
//        addCompressSchemaFile();
//    }

    /**
     * Check schema validation status for correctness.
     */
    @Test(groups = {"wso2.greg.schema.b"}, dependsOnGroups = {"wso2.greg.schema.a"})
    public void addValidSchemaTest() {
        String resourceUrl =
                "http://svn.wso2.org/repos/wso2/trunk/commons/qa/qa-artifacts/greg/xsd/Patient.xsd";

        String resourceName = "Patient.xsd";

        try {
            resourceAdminServiceStub.importResource(schemaPath + resourceName, resourceName,
                    RegistryConsts.APPLICATION_X_XSD_XML, "schemaFile", resourceUrl, null, null);

            assertTrue(validateProperties(schemaPath + "org/ihc/xsd/" + resourceName, "Schema Validation",
                    "Valid"), "Schema validation status incorrect");
            assertTrue(validateProperties(schemaPath + "org/ihc/xsd/" + resourceName, "targetNamespace",
                    "http://ihc.org/xsd?patient"), "Target namespace not found");

            String textContent = resourceAdminServiceStub.getTextContent(schemaPath +
                    "org/ihc/xsd/" + resourceName);

            if (!textContent.equals(null)) {
                log.info("Resource successfully added to the registry and retrieved contents successfully");
            } else {
                log.error("Unable to get text content");
                fail("Unable to get text content");
            }

            //delete the added resource
            resourceAdminServiceStub.delete(schemaPath +
                    "org/ihc/xsd/" + resourceName);

            //check if the deleted file exists in registry
            if (!isResourceExist(loggedInSessionCookie, schemaPath +
                    "org/ihc/xsd/", resourceName, resourceAdminServiceStub)) {
                log.info("Resource successfully deleted from the registry");

            } else {
                log.error("Resource not deleted from the registry");
                fail("Resource not deleted from the registry");
            }
        } catch (Exception e) {
            fail("Unable to get text content " + e);
            log.error(" : " + e.getMessage());
        }
    }

    /**
     * Check schema properties to find out whether the schema validation status is valid or invalid
     */
    @Test(groups = {"wso2.greg.schema.b"}, dependsOnGroups = {"wso2.greg.schema.a"}, dependsOnMethods = {"addValidSchemaTest"})
    public void addInvalidSchemaTest() {
        String resourceUrl =
//                "http://ww2.wso2.org/~charitha/xsds/calculator-no-element-name-invalid.xsd";
                "https://svn.wso2.org/repos/wso2/trunk/commons/qa/qa-artifacts/greg/xsd/calculator-no-element-name-invalid.xsd";
        String resourceName = "calculator-no-element-name-invalid.xsd";

        try {
            resourceAdminServiceStub.importResource(schemaPath + resourceName, resourceName,
                    RegistryConsts.APPLICATION_X_XSD_XML, "schemaFile", resourceUrl, null, null);

            assertTrue(validateProperties(schemaPath + "org/charitha/" +
                    resourceName, "Schema Validation", "Invalid"), "Schema validation status incorrect");
            assertTrue(validateProperties(schemaPath + "org/charitha/" +
                    resourceName, "targetNamespace", "http://charitha.org/"), "Target namespace not found");
            assertTrue(validateProperties(schemaPath + "org/charitha/" +
                    resourceName, "Schema Validation Message 1", "Error: s4s-att-must-appear: Attribute 'name' must " +
                    "appear in element 'element'."), "Schema validation error not found");

            String textContent = resourceAdminServiceStub.getTextContent(schemaPath +
                    "org/charitha/" + resourceName);

            if (!textContent.equals(null)) {
                log.info("Resource successfully added to the registry and retrieved contents successfully");
            } else {
                log.error("Unable to get text content");
                fail("Unable to get text content");
            }

            //delete the added resource
            resourceAdminServiceStub.delete(schemaPath +
                    "org/charitha/" + resourceName);

            //check if the deleted file exists in registry
            if (!isResourceExist(loggedInSessionCookie, schemaPath +
                    "org/charitha/", resourceName, resourceAdminServiceStub)) {
                log.info("Resource successfully deleted from the registry");

            } else {
                log.error("Resource not deleted from the registry");
                fail("Resource not deleted from the registry");
            }
        } catch (Exception e) {
            fail("Unable to get text content " + e);
            log.error(" : " + e.getMessage());
        }
    }

    /**
     * Add Schemas to registry using a zip file, validation status of all uploaded files checked.
     */
    @Test(groups = {"wso2.greg.schema.b"}, dependsOnGroups = {"wso2.greg.schema.a"}, dependsOnMethods = {"addInvalidSchemaTest"})
    public void addCompressSchemaFile() {
        String resourceName = "registry-new.zip";

        try {
            String resource = frameworkPath + File.separator + ".." + File.separator + ".." + File.separator + ".."
                    + File.separator + "src" + File.separator + "test" + File.separator + "java" + File.separator
                    + "resources" + File.separator + resourceName;

            resourceAdminServiceStub.addResource(schemaPath + resourceName,
                    RegistryConsts.APPLICATION_WSO2_GOVERNANCE_ARCHIVE, "schemaFile",
                    new DataHandler(new URL("file:///" + resource)), null, null);

            assertTrue(validateProperties(schemaPath +
                    "dk/dr/www/namespaces/schemas/application/mas/whatson/production/production.xsd",
                    "Schema Validation", "Valid"), "Schema validation status incorrect");
            assertTrue(validateProperties(schemaPath +
                    "dk/dr/www/namespaces/schemas/application/mas/whatson/production/production.xsd",
                    "targetNamespace", "http://www.dr.dk/namespaces/schemas/application/mas/whatson/production"),
                    "Target namespace not found");
//            assertTrue(validateProperties(schemaPath +
//                    "dk/dr/www/namespaces/schemas/common/types/types.xsd",
//                    "Schema Validation", "Valid"), "Schema validation status incorrect");
            assertTrue(validateProperties(schemaPath +
                    "dk/dr/www/namespaces/schemas/common/types/types.xsd",
                    "targetNamespace", "http://www.dr.dk/namespaces/schemas/common/types"),
                    "Target namespace not found");

            String textContent = resourceAdminServiceStub.getTextContent(schemaPath +
                    "dk/dr/www/namespaces/schemas/common/types/types.xsd");


            if (!textContent.equals(null)) {
                log.info("Resource successfully added to the registry and retrieved contents successfully");
            } else {
                log.error("Unable to get text content");
                fail("Unable to get text content");
            }

            if (isResourceExist(loggedInSessionCookie, schemaPath +
                    "dk/dr/www/namespaces/schemas/common/types", "types.xsd", resourceAdminServiceStub) &&
                    isResourceExist(loggedInSessionCookie, schemaPath +
                            "dk/dr/www/namespaces/schemas/application/mas/whatson/production", "production.xsd",
                            resourceAdminServiceStub)) {

                log.info("Resources have been uploaded to registry successfully");

            } else {
                log.error("Resources not exist in registry");
                fail("Resources not exist in registry");
            }

            //delete the added resource
            resourceAdminServiceStub.delete(schemaPath +
                    "dk/dr/www/namespaces/schemas/common/types/types.xsd");
            resourceAdminServiceStub.delete(schemaPath +
                    "dk/dr/www/namespaces/schemas/application/mas/whatson/production/production.xsd");

            //check if the deleted file exists in registry
            if (!(isResourceExist(loggedInSessionCookie, schemaPath +
                    "dk/dr/www/namespaces/schemas/common/types", "types.xsd", resourceAdminServiceStub) &&
                    isResourceExist(loggedInSessionCookie, schemaPath +
                            "dk/dr/www/namespaces/schemas/application/mas/whatson/production", "production.xsd",
                            resourceAdminServiceStub))) {

                log.info("Resource successfully deleted from the registry");

            } else {
                log.error("Resource not deleted from the registry");
                fail("Resource not deleted from the registry");
            }
        } catch (Exception e) {
            fail("Unable to get text content " + e);
            log.error(" : " + e.getMessage());
        }
    }


    public boolean validateProperties(String resourcePath, String key, String value) {
        boolean validationState = false;
        try {
            PropertiesBean propertiesBean = propertiesAdminServiceStub.getProperties(resourcePath, "yes");
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
}

