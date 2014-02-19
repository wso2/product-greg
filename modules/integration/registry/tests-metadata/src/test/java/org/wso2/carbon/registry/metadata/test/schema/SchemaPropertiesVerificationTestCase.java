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
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.schema.SchemaManager;
import org.wso2.carbon.governance.api.schema.dataobjects.Schema;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import java.net.MalformedURLException;
import java.rmi.RemoteException;

import static org.testng.Assert.*;

public class SchemaPropertiesVerificationTestCase {

    private Registry governanceRegistry;
    private Schema schema;
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
     * verifying property
     */
    @Test(groups = "wso2.greg", description = "verify properties of Schema")
    public void testPropertiesSchema() throws RemoteException,
                                              ResourceAdminServiceExceptionException,
                                              GovernanceException,
                                              MalformedURLException {


        schema = schemaManager
                .newSchema("https://svn.wso2.org/repos/wso2/carbon/platform/trunk/"
                           + "platform-integration/"
                           + "platform-automated-test-suite/org.wso2.carbon.automation.test.repo/src/main/"
                           + "resources/artifacts/GREG/schema/books.xsd");
        schema.addAttribute("version", "1.0.0");
        schema.addAttribute("author", "Kanarupan");
        schema.addAttribute("description", "Schema added for property checking");
        schemaManager.addSchema(schema);

        // Properties Verification
        assertFalse(schema.getId().isEmpty());
        assertNotNull(schema);

        assertTrue(schema.getAttribute("author").contentEquals("Kanarupan"));
        assertTrue(schema.getAttribute("version").contentEquals("1.0.0"));
        assertTrue(schema.getAttribute("description").contentEquals(
                "Schema added for property checking"));

        schema.setAttribute("author", "Kanarupan");
        schema.setAttribute("description", "this is to verify property edition");

        schemaManager.updateSchema(schema);

        assertTrue(schema.getAttribute("author").contentEquals("Kanarupan"));
        assertTrue(schema.getAttribute("version").contentEquals("1.0.0"));
        assertTrue(schema.getAttribute("description").contentEquals(
                "this is to verify property edition"));

    }

    @AfterClass(groups = "wso2.greg", alwaysRun = true, description = "cleaning up the artifacts added")
    public void tearDown() throws GovernanceException {
        schemaManager.removeSchema(schema.getId());
        schema = null;
        schemaManager = null;
        governanceRegistry = null;


    }

}
