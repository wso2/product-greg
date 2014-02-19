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

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.core.utils.fileutils.FileManager;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.schema.SchemaManager;
import org.wso2.carbon.governance.api.schema.dataobjects.Schema;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import javax.activation.DataHandler;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.*;

public class SchemaAdditionTestCase {
    private Registry governanceRegistry;
    private SchemaManager schemaManager;
    private Schema schema;
    private Schema schemaViaUrl;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private final String prefixPath = "/_system/governance";

    @BeforeClass(groups = "wso2.greg", alwaysRun = true)
    public void initialize() throws RemoteException,
            LoginAuthenticationExceptionException,
            org.wso2.carbon.registry.api.RegistryException {
        int userId = 2;
        RegistryProviderUtil provider = new RegistryProviderUtil();
        WSRegistryServiceClient wsRegistry = provider.getWSRegistry(userId,
                ProductConstant.GREG_SERVER_NAME);
        governanceRegistry = provider.getGovernanceRegistry(wsRegistry, userId);
        schemaManager = new SchemaManager(governanceRegistry);


        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        ManageEnvironment environment = builder.build();

        resourceAdminServiceClient = new ResourceAdminServiceClient(environment
                .getGreg().getProductVariables().getBackendUrl(),
                environment.getGreg().getSessionCookie());

    }


    /* add a schema Gar from file system */
    @Test(groups = "wso2.greg", description = "Add schema Gar from file system")
    public void testAddSchemaGarFromFileSystem() throws IOException,
            RegistryException,
            ResourceAdminServiceExceptionException,
            LoginAuthenticationExceptionException {
        String resourceName = "xsdAll.gar";
        String schemaGarPath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION
                + "artifacts" + File.separator + "GREG" + File.separator
                + resourceName;


        resourceAdminServiceClient.addResource(schemaGarPath,
                "application/vnd.wso2.governance-archive",
                "adding schema gar file", new DataHandler(new URL("file:///"
                + schemaGarPath)));
        int count = 0;
        Schema[] schemas = schemaManager.getAllSchemas();
        for (Schema tmpSchema : schemas) {
            if (tmpSchema.getQName().toString().contains("test1")
                    || tmpSchema.getQName().toString().contains("test2")
                    || tmpSchema.getQName().toString().contains("test3")
                    || tmpSchema.getQName().toString().contains("test4")) {
                count++;

            }

        }
        assertEquals(count, 4, "schema gar is not properly added");
    }

    /* add a schema from file system */
    @Test(groups = "wso2.greg", description = "Add schema from file system", dependsOnMethods = "testAddSchemaGarFromFileSystem")
    public void testAddSchemaFromFileSystem() throws IOException,
            RegistryException,
            ResourceAdminServiceExceptionException {
        String schemaPath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION
                + "artifacts" + File.separator + "GREG" + File.separator
                + "schema" + File.separator + "books.xsd";


        schema = schemaManager.newSchema(FileManager.readFile(schemaPath)
                .getBytes());
        schemaManager.addSchema(schema);
        schema.addAttribute("author", "Kana");
        schema.addAttribute("version", "1.0.0");
        schema.addAttribute("description",
                "schema addtion via file system verification");
        schemaManager.updateSchema(schema);
        assertNotNull(schema);
        assertTrue(schema.getAttribute("author").contentEquals("Kana"));
        assertEquals(schema.getAttribute("version"), "1.0.0");
        assertEquals(schema.getAttribute("description"),
                "schema addtion via file system verification");

        assertTrue(schema.getPath().contains("/trunk/schemas/books"));
        assertTrue(schema.getQName().toString().contains("urn:books"));
        resourceAdminServiceClient.deleteResource(prefixPath + schema.getPath()); //deleted here for convenience

    }

    /* add a schema via URL */
    @Test(groups = "wso2.greg", description = "Add Schema via URL", dependsOnMethods = "testAddSchemaFromFileSystem")
    public void testAddSchemaViaURL()
            throws IOException, RegistryException, ResourceAdminServiceExceptionException {

        schemaViaUrl = schemaManager
                .newSchema("https://svn.wso2.org/repos/wso2/carbon/platform/trunk/"
                        + "platform-integration/platform-automated-test-suite/"
                        + "org.wso2.carbon.automation.test.repo/src/main/"
                        + "resources/artifacts/GREG/schema/calculator.xsd");
        schemaManager.addSchema(schemaViaUrl);
        schemaViaUrl.addAttribute("author", "KanaURL");
        schemaViaUrl.addAttribute("version", "1.0.0");
        schemaViaUrl.addAttribute("description", "schema addtion via url");

        schemaManager.updateSchema(schemaViaUrl);

        assertNotNull(schemaViaUrl);
        assertTrue(schemaViaUrl.getAttribute("author").contentEquals("KanaURL"));
        assertEquals(schemaViaUrl.getAttribute("version"), "1.0.0");
        assertEquals(schemaViaUrl.getAttribute("description"),
                "schema addtion via url");

        assertTrue(schemaViaUrl.getPath().contains("/trunk/schemas/org/charitha/calculator.xsd"));
        assertTrue(schemaViaUrl.getQName().toString().contains("http://charitha.org/"));

        resourceAdminServiceClient.deleteResource(prefixPath + schemaViaUrl.getPath()); //deleted here for convenience

    }

    @AfterClass(groups = "wso2.greg", alwaysRun = true, description = "cleaning up the artifacts added")
    public void tearDown() throws GovernanceException, RemoteException,
            ResourceAdminServiceExceptionException {

        resourceAdminServiceClient
                .deleteResource("/_system/governance/trunk/schemas/com/microsoft/schemas/_2003/_10/serialization/test4.xsd");
        resourceAdminServiceClient
                .deleteResource("/_system/governance/trunk/schemas/org/tempuri/test3.xsd");
        resourceAdminServiceClient
                .deleteResource("/_system/governance/trunk/schemas/com/microsoft/schemas/_2003/_10/serialization/test2.xsd");
        resourceAdminServiceClient
                .deleteResource("/_system/governance/trunk/schemas/org/datacontract/schemas/_2004/_07/system/test1.xsd");
        governanceRegistry = null;
        schema = null;
        schemaManager = null;
        resourceAdminServiceClient = null;
        schemaViaUrl = null;


    }

}
