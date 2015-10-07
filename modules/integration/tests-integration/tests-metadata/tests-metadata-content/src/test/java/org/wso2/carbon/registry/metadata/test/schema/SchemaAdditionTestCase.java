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
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.automation.test.utils.common.FileManager;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.schema.SchemaManager;
import org.wso2.carbon.governance.api.schema.dataobjects.Schema;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import javax.activation.DataHandler;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.*;

public class SchemaAdditionTestCase extends GREGIntegrationBaseTest {
    private Registry governanceRegistry;
    private SchemaManager schemaManager;
    private Schema schema;
    private Schema schemaViaUrl;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private final String prefixPath = "/_system/governance";
    private String sessionCookie;

    @BeforeClass(groups = "wso2.greg", alwaysRun = true)
    public void initialize() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_USER);
        RegistryProviderUtil provider = new RegistryProviderUtil();
        WSRegistryServiceClient wsRegistry = provider.getWSRegistry(automationContext);
        governanceRegistry = provider.getGovernanceRegistry(wsRegistry, automationContext);
        sessionCookie = new LoginLogoutClient(automationContext).login();
        schemaManager = new SchemaManager(governanceRegistry);

        resourceAdminServiceClient =
                new ResourceAdminServiceClient(automationContext.getContextUrls().getBackEndUrl(),
                sessionCookie);

    }


    /* add a schema Gar from file system */
    @Test(groups = "wso2.greg", description = "Add schema Gar from file system")
    public void testAddSchemaGarFromFileSystem() throws IOException,
            RegistryException,
            ResourceAdminServiceExceptionException,
            LoginAuthenticationExceptionException {
        String resourceName = "xsdAll.gar";
        String schemaGarPath = FrameworkPathUtil.getSystemResourceLocation()
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
        String schemaPath = getTestArtifactLocation()
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

        assertTrue(schema.getPath().contains("/trunk/schemas/books/1.0.0"));
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

        assertTrue(schemaViaUrl.getPath().contains("/trunk/schemas/org/charitha/1.0.0/calculator.xsd"));
        assertTrue(schemaViaUrl.getQName().toString().contains("http://charitha.org/"));

        resourceAdminServiceClient.deleteResource(prefixPath + schemaViaUrl.getPath()); //deleted here for convenience

    }

    @AfterClass(groups = "wso2.greg", alwaysRun = true, description = "cleaning up the artifacts added")
    public void tearDown() throws GovernanceException, RemoteException,
            ResourceAdminServiceExceptionException {

        resourceAdminServiceClient
                .deleteResource("/_system/governance/trunk/schemas/com/microsoft/schemas/_2003/_10/serialization/1.0.0/test4.xsd");
        resourceAdminServiceClient
                .deleteResource("/_system/governance/trunk/schemas/org/tempuri/1.0.0/test3.xsd");
        resourceAdminServiceClient
                .deleteResource("/_system/governance/trunk/schemas/com/microsoft/schemas/_2003/_10/serialization/1.0.0/test2.xsd");
        resourceAdminServiceClient
                .deleteResource("/_system/governance/trunk/schemas/org/datacontract/schemas/_2004/_07/system/1.0.0/test1.xsd");
        governanceRegistry = null;
        schema = null;
        schemaManager = null;
        resourceAdminServiceClient = null;
        schemaViaUrl = null;


    }

}
