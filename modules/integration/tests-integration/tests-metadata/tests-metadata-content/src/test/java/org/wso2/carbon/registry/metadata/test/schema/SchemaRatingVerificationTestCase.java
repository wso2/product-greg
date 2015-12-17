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

import org.apache.axis2.AxisFault;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.schema.SchemaManager;
import org.wso2.carbon.governance.api.schema.dataobjects.Schema;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.info.stub.RegistryExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.clients.InfoServiceAdminClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import java.net.MalformedURLException;
import java.rmi.RemoteException;

import static org.testng.Assert.*;

public class SchemaRatingVerificationTestCase extends GREGIntegrationBaseTest {

    private Registry governanceRegistry;
    private Schema schema;
    private InfoServiceAdminClient infoServiceAdminclient;
    private SchemaManager schemaManager;
    private float avgRating;
    private int myRating;
    private String sessionCookie;

    @BeforeClass (groups = "wso2.greg", alwaysRun = true)
    public void initialize () throws Exception {
        super.init(TestUserMode.SUPER_TENANT_USER);
        sessionCookie = new LoginLogoutClient(automationContext).login();
        infoServiceAdminclient =
                new InfoServiceAdminClient(backendURL, sessionCookie);
        RegistryProviderUtil provider = new RegistryProviderUtil();
        WSRegistryServiceClient wsRegistry = provider.getWSRegistry(automationContext);
        governanceRegistry = provider.getGovernanceRegistry(wsRegistry, automationContext);
        schemaManager = new SchemaManager(governanceRegistry);

    }

    /**
     * ratings verification
     */
    @Test (groups = "wso2.greg", description = "ratings verification")
    public void testAddSchema () throws RemoteException,
            ResourceAdminServiceExceptionException, GovernanceException,
            MalformedURLException {

        schema = schemaManager
                .newSchema("https://raw.githubusercontent.com/wso2/wso2-qa-artifacts/master/" +
                        "automation-artifacts/greg/schema/books.xsd");
        schema.addAttribute("version", "1.0.0");
        schema.addAttribute("author", "Kanarupan");
        schema.addAttribute("description", "added Schema using url");
        schemaManager.addSchema(schema);
        schemaManager.updateSchema(schema);
        assertFalse(schema.getId().isEmpty());
        assertNotNull(schema);
        assertTrue(schema.getAttribute("author").contentEquals("Kanarupan"));

    }

    @Test (groups = "wso2.greg", description = "ratings Verification", dependsOnMethods = "testAddSchema")
    public void testRatingVerification () throws AxisFault, RegistryException, RegistryExceptionException {

        final String SchemaPath = "/_system/governance" + schema.getPath();
        myRating = 3;
        infoServiceAdminclient.rateResource(String.valueOf(myRating), SchemaPath, sessionCookie);
        schemaManager.updateSchema(schema);
        infoServiceAdminclient.getRatings(SchemaPath, sessionCookie).getUserRating();
        assertEquals(infoServiceAdminclient.getRatings(SchemaPath, sessionCookie).getUserRating(), myRating);
        avgRating = infoServiceAdminclient.getRatings(SchemaPath, sessionCookie).getAverageRating();
        assertEquals(avgRating, 3.0f);

    }

    @Test (groups = "wso2.greg", description = "myrating, rating relationship verified",
            dependsOnMethods = "testRatingVerification")
    public void testRatingChanges () throws AxisFault, RegistryException, RegistryExceptionException {

        final String schemaPath = "/_system/governance" + schema.getPath();
        myRating = 2;
        infoServiceAdminclient.rateResource(String.valueOf(myRating), schemaPath, sessionCookie);
        schemaManager.updateSchema(schema);
        avgRating = infoServiceAdminclient.getRatings(schemaPath, sessionCookie).getAverageRating();
        assertTrue(avgRating < 3.0f);

    }

    @AfterClass (groups = "wso2.greg", alwaysRun = true, description = "cleaning up the artifacts added")
    public void tearDown () throws GovernanceException {

        schemaManager.removeSchema(schema.getId());
        governanceRegistry = null;
        schema = null;
        schemaManager = null;
        infoServiceAdminclient = null;
    }

}
