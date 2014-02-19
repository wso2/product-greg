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
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceStub;

import static org.testng.Assert.*;

import javax.activation.DataHandler;
import java.io.File;
import java.net.URL;

import static org.wso2.carbon.registry.metadata.test.util.TestUtils.isResourceExist;

/**
 * A test case which tests registry Schema add operation
 */

public class SchemaAddTestCase {

    private static final Log log = LogFactory.getLog(SchemaAddTestCase.class);
    private String schemaPath = "/_system/governance/trunk/schemas/";
    private ResourceAdminServiceStub resourceAdminServiceStub;
    private String loggedInSessionCookie = "";
    private LoginLogoutUtil util = new LoginLogoutUtil();
    private String frameworkPath = "";


    @BeforeClass(groups = {"wso2.greg.schema.a"})
    public void init() throws Exception {
        log.info("Initializing Add Schema Resource Tests");
        log.debug("Add Add Schema Resource Initialised");
        loggedInSessionCookie = util.login();
        frameworkPath = FrameworkSettings.getFrameworkPath();
        log.debug("Running SuccessCase");
        resourceAdminServiceStub = TestUtils.getResourceAdminServiceStub(loggedInSessionCookie);

    }

//    @Test(groups = {"wso2.greg.schema.a"})
//    public void runSuccessCase() {
//          addSchema();
//        addSchemafromURL();
//        addSchemaFromGar();
//        updateSchemaTest();
//        updateSchemaFromURL();
//        addSchemaMultipleImports();
//    }


    /**
     * Add schema files from fie system to registry
     */

    @Test(groups = {"wso2.greg.schema.a"})
    public void addSchema() {
        String resourceName = "calculator.xsd";

        try {
            String resource = frameworkPath + File.separator + ".." + File.separator + ".." + File.separator + ".."
                    + File.separator + "src" + File.separator + "test" + File.separator + "java" + File.separator
                    + "resources" + File.separator + resourceName;

            resourceAdminServiceStub.addResource(schemaPath + resourceName,
                    RegistryConsts.APPLICATION_X_XSD_XML, "schemaFile", new DataHandler(new URL("file:///" + resource)),
                    null, null);

            String textContent = resourceAdminServiceStub.getTextContent(schemaPath +
                    "org1899988/charitha/" + resourceName);

            if (!textContent.equals(null)) {
                log.info("Resource successfully added to the registry and retrieved contents successfully");
            } else {
                log.error("Unable to get text content");
                fail("Unable to get text content");
            }

            //delete the added resource
            resourceAdminServiceStub.delete(schemaPath +
                    "org1899988/charitha/" + resourceName);

            //check if the deleted file exists in registry
            if (!isResourceExist(loggedInSessionCookie, schemaPath +
                    "org1899988/charitha/", resourceName, resourceAdminServiceStub)) {
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
     * Add schema file from URL
     */
    @Test(groups = {"wso2.greg.schema.a"}, dependsOnMethods = {"addSchema"})
    public void addSchemafromURL() {
        String resourceUrl =
//                "http://ww2.wso2.org/~qa/greg/simpleXsd1.xsd";
                "https://svn.wso2.org/repos/wso2/trunk/commons/qa/qa-artifacts/greg/xsd/simpleXsd1.xsd";

        String resourceName = "simpleXsd1.xsd";

        try {
            resourceAdminServiceStub.importResource(schemaPath + resourceName, resourceName,
                    RegistryConsts.APPLICATION_X_XSD_XML, "schemaFile", resourceUrl, null, null);

            String textContent = resourceAdminServiceStub.getTextContent(schemaPath +
                    "services/samples/xsd/" + resourceName);

            if (!textContent.equals(null)) {
                log.info("Resource successfully added to the registry and retrieved contents successfully");
            } else {
                log.error("Unable to get text content");
                fail("Unable to get text content");
            }

            //delete the added resource
            resourceAdminServiceStub.delete(schemaPath +
                    "services/samples/xsd/" + resourceName);

            //check if the deleted file exists in registry
            if (!isResourceExist(loggedInSessionCookie, schemaPath +
                    "services/samples/xsd/", resourceName, resourceAdminServiceStub)) {
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
     * upload a governance archive file containing four xsds
     */
    @Test(groups = {"wso2.greg.schema.a"}, dependsOnMethods = {"addSchemafromURL"})
    public void addSchemaFromGar() {
        String resourceName = "xsdAll.gar";

        try {
            String resource = frameworkPath + File.separator + ".." + File.separator + ".." + File.separator + ".."
                    + File.separator + "src" + File.separator + "test" + File.separator + "java" + File.separator
                    + "resources" + File.separator + resourceName;

            resourceAdminServiceStub.addResource(schemaPath + resourceName,
                    RegistryConsts.APPLICATION_WSO2_GOVERNANCE_ARCHIVE, "schemaFile",
                    new DataHandler(new URL("file:///" + resource)), null, null);

            String textContent = resourceAdminServiceStub.getTextContent(schemaPath +
                    "com/microsoft/schemas/_2003/_10/serialization/test2.xsd");

            if (!textContent.equals(null)) {
                log.info("Resource successfully added to the registry and retrieved contents successfully");
            } else {
                log.error("Unable to get text content");
                fail("Unable to get text content");
            }

            if (isResourceExist(loggedInSessionCookie, schemaPath +
                    "com/microsoft/schemas/_2003/_10/serialization", "test2.xsd", resourceAdminServiceStub) &&
                    isResourceExist(loggedInSessionCookie, schemaPath +
                            "org/datacontract/schemas/_2004/_07/system", "test1.xsd", resourceAdminServiceStub) &&
                    isResourceExist(loggedInSessionCookie, schemaPath +
                            "org/tempuri", "test3.xsd", resourceAdminServiceStub) &&
                    isResourceExist(loggedInSessionCookie, schemaPath +
                            "com/microsoft/schemas/_2003/_10/serialization", "test4.xsd", resourceAdminServiceStub)) {

                log.info("Resources have been uploaded to registry successfully");

            } else {
                log.error("Resources not exist in registry");
                fail("Resources not exist in registry");
            }

            //delete the added resource
            resourceAdminServiceStub.delete(schemaPath +
                    "com/microsoft/schemas/_2003/_10/serialization/test2.xsd");
            resourceAdminServiceStub.delete(schemaPath +
                    "org/datacontract/schemas/_2004/_07/system/test1.xsd");
            resourceAdminServiceStub.delete(schemaPath +
                    "org/tempuri/test3.xsd");
            resourceAdminServiceStub.delete(schemaPath +
                    "com/microsoft/schemas/_2003/_10/serialization/test4.xsd");

            //check if the deleted file exists in registry
            if (!(isResourceExist(loggedInSessionCookie, schemaPath +
                    "com/microsoft/schemas/_2003/_10/serialization", "test2.xsd", resourceAdminServiceStub) &&
                    isResourceExist(loggedInSessionCookie, schemaPath +
                            "org/datacontract/schemas/_2004/_07/system", "test1.xsd", resourceAdminServiceStub) &&
                    isResourceExist(loggedInSessionCookie, schemaPath +
                            "org/tempuri", "test3.xsd", resourceAdminServiceStub) &&
                    isResourceExist(loggedInSessionCookie, schemaPath +
                            "com/microsoft/schemas/_2003/_10/serialization", "test4.xsd", resourceAdminServiceStub))) {

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

    @Test(groups = {"wso2.greg.schema.a"}, dependsOnMethods = {"addSchemaFromGar"})
    public void updateSchemaTest() {
        String resourceName = "calculator.xsd";
        String updatedResourceName = "calculator-updated.xsd";

        try {
            String resource = frameworkPath + File.separator + ".." + File.separator + ".." + File.separator + ".."
                    + File.separator + "src" + File.separator + "test" + File.separator + "java" + File.separator
                    + "resources" + File.separator + resourceName;

            resourceAdminServiceStub.addResource(schemaPath + resourceName,
                    RegistryConsts.APPLICATION_X_XSD_XML, "schemaFile",
                    new DataHandler(new URL("file:///" + resource)), null, null);

            String textContent = resourceAdminServiceStub.getTextContent(schemaPath +
                    "org1899988/charitha/" + resourceName);

            if (textContent.indexOf("xmlns:tns=\"http://charitha.org/\"") != -1) {
                log.info("Schema added successfully");

            } else {
                log.error("Schema content not found");
                fail("Schema content not found");
            }

            String resourceUpdated = frameworkPath + File.separator + ".." + File.separator + ".." + File.separator + ".."
                    + File.separator + "src" + File.separator + "test" + File.separator + "java" + File.separator
                    + "resources" + File.separator + updatedResourceName;

            resourceAdminServiceStub.addResource(schemaPath + resourceName,
                    RegistryConsts.APPLICATION_X_XSD_XML, "schemaFile",
                    new DataHandler(new URL("file:///" + resourceUpdated)), null, null);

            String textContentUpdated = resourceAdminServiceStub.getTextContent(schemaPath +
                    "org1899988/charitha/" + resourceName);

            if (textContentUpdated.indexOf("xmlns:tns=\"http://charitha.org.updated/\"") != -1) {
                log.info("Schema Updated successfully");

            } else {
                log.error("Schema has not been updated");
                fail("Schema has not been updated");
            }

            //delete the added resource
            resourceAdminServiceStub.delete(schemaPath +
                    "org1899988/charitha/" + resourceName);

            //check if the deleted file exists in registry
            if (!isResourceExist(loggedInSessionCookie, schemaPath +
                    "org1899988/charitha/", resourceName, resourceAdminServiceStub)) {
                log.info("Resource successfully deleted from the registry");

            } else {
                log.error("Resource not deleted from registry");
                fail("Resource not deleted from registry");
            }
        } catch (Exception e) {
            fail("Unable to get text content " + e);
            log.error(" : " + e.getMessage());
        }
    }

    /**
     * Update schema file from URL
     */
    @Test(groups = {"wso2.greg.schema.a"}, dependsOnMethods = {"updateSchemaTest"})
    public void updateSchemaFromURL() {
        String resourceUrl =
//                "http://ww2.wso2.org/~qa/greg/calculator-new.xsd";
                "https://svn.wso2.org/repos/wso2/trunk/commons/qa/qa-artifacts/greg/xsd/calculator-new.xsd";
        String resourceName = "calculator-new.xsd";
        String updatedResourceUrl =
//                "http://ww2.wso2.org/~qa/greg/calculator-new-updated.xsd";
                "https://svn.wso2.org/repos/wso2/trunk/commons/qa/qa-artifacts/greg/xsd/calculator-new-updated.xsd";
        String updatedResourceName = "calculator-new-updated.xsd\"";

        try {
            resourceAdminServiceStub.importResource(schemaPath, resourceName, RegistryConsts.APPLICATION_X_XSD_XML,
                    "Import Schema from URL", resourceUrl, null, null);

            String textContent = resourceAdminServiceStub.getTextContent(schemaPath +
                    "org1/charitha/" + resourceName);

            if (textContent.indexOf("xmlns:tns=\"http://charitha.org/\"") != -1) {
                log.info("Schema added successfully");

            } else {
                log.error("Schema content not found");
                fail("Schema content not found");
            }


            resourceAdminServiceStub.importResource(schemaPath, resourceName, RegistryConsts.APPLICATION_X_XSD_XML,
                    "Update Schema from URL", updatedResourceUrl, null, null);

            String textContentUpdated = resourceAdminServiceStub.getTextContent(schemaPath +
                    "org1/charitha/" + resourceName);

            if (textContentUpdated.indexOf("xmlns:tns=\"http://charitha.org.updated/\"") != -1) {
                log.info("Schema Updated successfully");

            } else {
                log.error("Schema has not been updated");
                fail("Schema has not been updated");
            }

            //delete the added resource
            resourceAdminServiceStub.delete(schemaPath +
                    "org1/charitha/" + resourceName);

            //check if the deleted file exists in registry
            if (!isResourceExist(loggedInSessionCookie, schemaPath +
                    "org1/charitha/", resourceName, resourceAdminServiceStub)) {
                log.info("Resource successfully deleted from the registry");

            } else {
                log.error("Resource not deleted from registry");
                fail("Resource not deleted from registry");
            }
        } catch (Exception e) {
            fail("Unable to get text content " + e);
            log.error(" : " + e.getMessage());
        }
    }

    /**
     * Add a schema which imports another schema
     */

    @Test(groups = {"wso2.greg.schema.a"}, dependsOnMethods = {"updateSchemaFromURL"})
    public void addSchemaMultipleImports() {
        String resourceUrl =
//                "http://ww2.wso2.org/~qa/greg/calculator.xsd";
                "https://svn.wso2.org/repos/wso2/trunk/commons/qa/qa-artifacts/greg/xsd/company.xsd";
        String resourceName = "company.xsd";
        String referenceSchemaFile = "person.xsd";

        try {
            resourceAdminServiceStub.importResource(schemaPath, resourceName,
                    RegistryConsts.APPLICATION_X_XSD_XML, "schemaFile", resourceUrl, null, null);

            String textContent = resourceAdminServiceStub.getTextContent(schemaPath +
                    "org/charitha/" + resourceName);

            if (textContent.indexOf("xmlns:tns=\"http://charitha.org/\"") != -1) {
                log.info("Schema content found");

            } else {
                log.error("Schema content not found");
                fail("Schema content not found");
            }


            String textContentImportedSchema = resourceAdminServiceStub.getTextContent(schemaPath +
                    "org1/charitha/" + referenceSchemaFile);

            if (textContentImportedSchema.indexOf("xmlns:tns=\"http://charitha.org1/\"") != -1) {
                log.info("Schema content found");

            } else {
                log.error("Schema content not found");
                fail("Schema content not found");
            }

            //delete the added resource
            resourceAdminServiceStub.delete(schemaPath +
                    "org/charitha/" + resourceName);

            resourceAdminServiceStub.delete(schemaPath +
                    "org1/charitha/" + referenceSchemaFile);

            //check if the deleted file exists in registry
            if (!isResourceExist(loggedInSessionCookie, schemaPath +
                    "org/charitha/", resourceName, resourceAdminServiceStub)) {
                log.info("Resource successfully deleted from the registry");

            } else {
                log.error("Resource not deleted from the registry");
                fail("Resource not deleted from the registry");
            }

            if (!isResourceExist(loggedInSessionCookie, schemaPath +
                    "org1/charitha/", referenceSchemaFile, resourceAdminServiceStub)) {
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

}
