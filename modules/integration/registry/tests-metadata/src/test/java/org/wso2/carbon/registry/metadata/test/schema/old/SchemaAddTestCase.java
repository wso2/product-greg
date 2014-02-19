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

package org.wso2.carbon.registry.metadata.test.schema.old;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.registry.info.stub.RegistryExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;

import javax.activation.DataHandler;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

/**
 * A test case which tests registry Schema add operation
 */

public class SchemaAddTestCase {
    private static final Log log = LogFactory.getLog(SchemaAddTestCase.class);
    private String schemaPath = "/_system/governance/trunk/schemas/";
    private ManageEnvironment environment;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private UserInfo userInfo;

    @BeforeClass(groups = {"wso2.greg"})
    public void init() throws Exception {
        log.info("Initializing Add Schema Resource Tests");
        log.debug("Add Add Schema Resource Initialised");
        int userId = 0;
        userInfo = UserListCsvReader.getUserInfo(userId);
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        environment = builder.build();
        log.debug("Running SuccessCase");
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                               environment.getGreg().getSessionCookie());
    }


    /**
     * Add schema files from fie system to registry
     */

    @Test(groups = {"wso2.greg"}, description = "Add schema files from fie system to registry")
    public void addSchema()
            throws MalformedURLException, ResourceAdminServiceExceptionException, RemoteException {
        String resourceName = "calculator.xsd";
        String resource = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator +
                          "GREG" + File.separator + "schema" + File.separator + resourceName;
        resourceAdminServiceClient.addSchema("adding Schema", new DataHandler(new URL("file:///" + resource)));
        Assert.assertNotNull(resourceAdminServiceClient.getTextContent(schemaPath + "org/charitha/calculator.xsd"));

    }


    /**
     * Add schema file from URL
     */
    @Test(groups = {"wso2.greg"}, description = "Add schema file from URL")
    public void addSchemafromURL() throws ResourceAdminServiceExceptionException, RemoteException {
        String resourceUrl =
//                "http://ww2.wso2.org/~qa/greg/simpleXsd1.xsd";
                "https://svn.wso2.org/repos/wso2/trunk/commons/qa/qa-artifacts/greg/xsd/simpleXsd1.xsd";
        String resourceName = "simpleXsd1.xsd";
        resourceAdminServiceClient.addSchema(resourceName, "adding Schema Via URL", resourceUrl);
        Assert.assertNotNull(resourceAdminServiceClient.getTextContent(schemaPath + "/services/samples/xsd/simpleXsd1.xsd"));

    }

    /**
     * upload a governance archive file containing four xsds
     */
    @Test(groups = {"wso2.greg"}, description = " upload a governance archive file containing four xsds")
    public void addSchemaFromGar()
            throws ResourceAdminServiceExceptionException, RemoteException, MalformedURLException {
        String resourceName = "xsdAll.gar";
        String resource = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator +
                          "GREG" + File.separator + resourceName;
        resourceAdminServiceClient.addResource(schemaPath + resourceName, "application/vnd.wso2.governance-archive", "adding gar File", new DataHandler(new URL("file:///" + resource)));
        Assert.assertNotNull(resourceAdminServiceClient.getTextContent(schemaPath +
                                                                       "com/microsoft/schemas/_2003/_10/serialization/test2.xsd"));
        Assert.assertNotNull(resourceAdminServiceClient.getTextContent(schemaPath +
                                                                       "com/microsoft/schemas/_2003/_10/serialization/test4.xsd"));


    }

    @Test(groups = {"wso2.greg"}, description = "update the schema")
    public void updateSchemaTest()
            throws ResourceAdminServiceExceptionException, RemoteException, MalformedURLException {
        String resourceName = "calculator.xsd";
        String updatedResourceName = "calculator-updated.xsd";
        String resource = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator +
                          "GREG" + File.separator +
                          "schema" + File.separator + resourceName;
        resourceAdminServiceClient.addResource(schemaPath + resourceName,
                                               "application/x-xsd+xml", "schemaFile",
                                               new DataHandler(new URL("file:///" + resource)));
        Assert.assertNotNull(resourceAdminServiceClient.getTextContent(schemaPath +
                                                                       "org/charitha/calculator.xsd"));
        String resourceUpdated = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator +
                                 "GREG" + File.separator +
                                 "schema" + File.separator + updatedResourceName;
        resourceAdminServiceClient.addResource(schemaPath + resourceName,
                                               "application/x-xsd+xml", "schemaFile",
                                               new DataHandler(new URL("file:///" + resourceUpdated)));
        String textContentUpdated = resourceAdminServiceClient.getTextContent(schemaPath +
                                                                              "/org/charitha/calculator.xsd");
        Assert.assertEquals(textContentUpdated.indexOf("xmlns:tns=\"http://charitha.org.updated/\""), -1);

    }

    /**
     * Update schema file from URL
     */
    @Test(groups = {"wso2.greg"}, dependsOnMethods = "addSchemafromURL", description = "Update schema file from URL")
    public void updateSchemaFromURL()
            throws ResourceAdminServiceExceptionException, RemoteException,
                   RegistryExceptionException {
        String resourceUrl =
//                "http://ww2.wso2.org/~qa/greg/calculator-new.xsd";
                "https://svn.wso2.org/repos/wso2/trunk/commons/qa/qa-artifacts/greg/xsd/calculator-new.xsd";
        String resourceName = "calculator-new.xsd";
        String updatedResourceUrl =
//                "http://ww2.wso2.org/~qa/greg/calculator-new-updated.xsd";
                "https://svn.wso2.org/repos/wso2/trunk/commons/qa/qa-artifacts/greg/xsd/calculator-new-updated.xsd";
        String updatedResourceName = "calculator-new-updated.xsd\"";
        resourceAdminServiceClient.addSchema(resourceName, "adding from URL", resourceUrl);
        Assert.assertNotNull(resourceAdminServiceClient.getTextContent(schemaPath + "org1/charitha/calculator-new.xsd"));
        resourceAdminServiceClient.importResource(schemaPath, resourceName, "application/x-xsd+xml",
                                                  "Update Schema from URL", updatedResourceUrl, null);
        String textContentUpdated = resourceAdminServiceClient.getTextContent(schemaPath +
                                                                              "org1/charitha/" + resourceName);
        Assert.assertNotEquals(textContentUpdated.indexOf("xmlns:tns=\"http://charitha.org.updated/\""), -1);

    }

    /**
     * Add a schema which imports another schema
     */

    @Test(groups = {"wso2.greg"}, description = "Add a schema which imports another schema")
    public void addSchemaMultipleImports() throws RegistryExceptionException, RemoteException,
                                                  ResourceAdminServiceExceptionException {
        String resourceUrl =
                "https://svn.wso2.org/repos/wso2/trunk/commons/qa/qa-artifacts/greg/xsd/company.xsd";
        String resourceName = "company.xsd";
        String referenceSchemaFile = "person.xsd";
        resourceAdminServiceClient.importResource(schemaPath, resourceName,
                                                  "application/x-xsd+xml", "schemaFile", resourceUrl, null);
        String textContent = resourceAdminServiceClient.getTextContent(schemaPath +
                                                                       "org/charitha/" + resourceName);
        Assert.assertNotEquals(textContent.indexOf("xmlns:tns=\"http://charitha.org/\""), -1);
        String textContentImportedSchema = resourceAdminServiceClient.getTextContent(schemaPath +
                                                                                     "org1/charitha/" + referenceSchemaFile);
        Assert.assertNotEquals(textContentImportedSchema.indexOf("xmlns:tns=\"http://charitha.org1/\""), -1);
        //delete the added resource

    }

    @AfterClass(groups = {"wso2.greg"})
    public void deleteResources() throws ResourceAdminServiceExceptionException, RemoteException {

        resourceAdminServiceClient.deleteResource(schemaPath +
                                                  "org/charitha/company.xsd");
        resourceAdminServiceClient.deleteResource("/_system/governance/trunk/schemas/org1899988/charitha/calculator.xsd");
        resourceAdminServiceClient.deleteResource(schemaPath +
                                                  "org1/charitha/person.xsd");
        resourceAdminServiceClient.deleteResource(schemaPath +
                                                  "/org/charitha/calculator.xsd");
        resourceAdminServiceClient.deleteResource(schemaPath +
                                                  "com/microsoft/schemas/_2003/_10/serialization/test2.xsd");
        resourceAdminServiceClient.deleteResource(schemaPath +
                                                  "com/microsoft/schemas/_2003/_10/serialization/test4.xsd");
        resourceAdminServiceClient.deleteResource("/_system/governance/trunk/schemas/org/datacontract/schemas/_2004/_07/system/test1.xsd");
        resourceAdminServiceClient.deleteResource("/_system/governance/trunk/schemas/org/tempuri/test3.xsd");
        resourceAdminServiceClient.deleteResource("/_system/governance/trunk/schemas/org1/charitha/calculator-new.xsd");
        resourceAdminServiceClient.deleteResource("/_system/governance/trunk/schemas/services/samples/xsd/simpleXsd1.xsd");
    }
}
