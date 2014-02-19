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
 **/

package org.wso2.carbon.registry.metadata.test.schema;

import org.apache.axiom.om.OMException;
import org.apache.axis2.AxisFault;
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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;

public class NegativeSchemaAdditionTestCase {

    private Registry governanceRegistry;
    private SchemaManager schemaManager;

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

    }

    /**
     * adding an invalid schema no assertions and tear down because schema
     * addition wouldn't happen
     */
    @Test(groups = "wso2.greg", description = "Add Invalid Schema", expectedExceptions = GovernanceException.class)
    public void testInvalidAdditionSchemaViaUrl() throws RemoteException,
            ResourceAdminServiceExceptionException,
            GovernanceException,
            MalformedURLException {


        Schema schema = schemaManager
                .newSchema("https://svn.wso2.org/repos/wso2/carbon/platform/trunk/platform-integration/"
                        + "platform-automated-test-suite/org.wso2.carbon.automation.test.repo/src/main/resources/artifacts/GREG/"
                        + "schema/XmlInvalidSchema.xsd");

        schema.addAttribute("version", "1.0.0");
        schema.addAttribute("author", "Kana");
        schema.addAttribute("description", "added invalid schema using url");
        schemaManager.addSchema(schema);

    }

    /**
     * adding schema without name via url
     */
    @Test(groups = "wso2.greg", description = "Add Schema without name",  dependsOnMethods = "testInvalidAdditionSchemaViaUrl")
    public void testAdditionSchemaWithoutNameViaUrl() throws RemoteException,
            ResourceAdminServiceExceptionException,
            GovernanceException,
            MalformedURLException {


        Schema schema = schemaManager
                .newSchema("https://svn.wso2.org/repos/wso2/carbon/platform/trunk/"
                        + "platform-integration/platform-automated-test-suite/"
                        + "org.wso2.carbon.automation.test.repo/src/main/resources/"
                        + "artifacts/GREG/schema/books_withoutName.xsd");

        schema.addAttribute("version", "1.0.0");
        schema.addAttribute("author", "Kana");
        schema.addAttribute("description", "added schema without name using url");
        schemaManager.addSchema(schema);

    }

    /**
     * invalid form file system
     */
    @Test(groups = "wso2.greg", description = "invalid schema form file system using admin services", dependsOnMethods = "testAdditionSchemaWithoutNameViaUrl", expectedExceptions = OMException.class)
    public void testAddInvalidSchemaFromFileSystem() throws IOException, RegistryException {

        // clarity automation api for registry
        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION
                + "artifacts" + File.separator + "GREG" + File.separator
                + "schema" + File.separator + "XmlInvalidSchema.xsd"; // the
        // path
        Schema schema = schemaManager.newSchema(FileManager.readFile(resourcePath)
                .getBytes());
        schemaManager.addSchema(schema);// OMException will be generated

    }

    /**
     * Schema Without Name from file system
     */
    @Test(groups = "wso2.greg", description = "schema without name form file system using admin services", dependsOnMethods = "testAddInvalidSchemaFromFileSystem", expectedExceptions = AxisFault.class)
    public void testAddSchemaWithoutNameViaUrl()
            throws RegistryException, IOException, LoginAuthenticationExceptionException, ResourceAdminServiceExceptionException {
        int userId1 = 1;

        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId1);
        ManageEnvironment environment = builder.build();
        UserInfo userInfo = UserListCsvReader.getUserInfo(userId1);
        ResourceAdminServiceClient resourceAdminServiceClient = new ResourceAdminServiceClient(
                environment.getGreg().getProductVariables().getBackendUrl(),
                environment.getGreg().getSessionCookie());


        resourceAdminServiceClient.addSchema("", "adding schema without a name", "https://svn.wso2.org/repos/wso2/" +
                "carbon/platform/trunk/platform-integration/platform-automated-test-suite/org.wso2.carbon.automation.test.repo/" +
                "src/main/resources/artifacts/GREG/schema/books_withoutName.xsd");                                           //AxisFault Error

    }


    @AfterClass(groups = "wso2.greg", alwaysRun = true, description = "cleaning up the artifacts added")
    public void tearDown() throws GovernanceException, RemoteException,
            ResourceAdminServiceExceptionException {

        governanceRegistry = null;
        schemaManager = null;


    }
}


