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
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.schema.SchemaManager;
import org.wso2.carbon.governance.api.schema.dataobjects.Schema;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.info.stub.RegistryExceptionException;
import org.wso2.carbon.registry.info.stub.beans.xsd.CommentBean;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.clients.InfoServiceAdminClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;
import java.net.MalformedURLException;
import java.rmi.RemoteException;

import static org.testng.Assert.*;

public class SchemaCommentsVerificationTestCase extends GREGIntegrationBaseTest{

    private Registry governanceRegistry;
    private Schema schema;
    private InfoServiceAdminClient infoServiceAdminclient;
    private SchemaManager schemaManager;
    private String sessionCookie;

    @BeforeClass(groups = "wso2.greg", alwaysRun = true)
    public void initialize() throws Exception {

        sessionCookie = new LoginLogoutClient(automationContext).login();
        infoServiceAdminclient =
                new InfoServiceAdminClient(automationContext.getContextUrls().getBackEndUrl(),
                                           sessionCookie);

        RegistryProviderUtil provider = new RegistryProviderUtil();
        WSRegistryServiceClient wsRegistry = provider.getWSRegistry(automationContext);
        governanceRegistry = provider.getGovernanceRegistry(wsRegistry, automationContext);
        schemaManager = new SchemaManager(governanceRegistry);

    }

    /**
     * comments verification
     */
    @Test(groups = "wso2.greg", description = "comments verification")
    public void testAddSchema() throws RemoteException,
                                       ResourceAdminServiceExceptionException, GovernanceException,
                                       MalformedURLException {


        schema = schemaManager
                .newSchema("https://svn.wso2.org/repos/wso2/carbon/platform/trunk/"
                           + "platform-integration/platform-automated-test-suite/org.wso2.carbon.automation.test.repo/"
                           + "src/main/resources/artifacts/GREG/schema/Person.xsd");

        schema.addAttribute("version", "1.0.0");
        schema.addAttribute("author", "Aparna");
        schema.addAttribute("description", "added large schema using url");
        schemaManager.addSchema(schema);

        assertFalse(schema.getId().isEmpty());
        assertNotNull(schema);
        assertTrue(schema.getAttribute("author").contentEquals("Aparna"));

    }

    @Test(groups = "wso2.greg", description = "Comments Verification", dependsOnMethods = "testAddSchema")
    public void testCommentVerification() throws AxisFault, RegistryException,
                                                 RegistryExceptionException {

        infoServiceAdminclient.addComment(
                "This schema is added to verify the comments",
                "/_system/governance" + schema.getPath(), sessionCookie);

        CommentBean commentBean = infoServiceAdminclient.getComments(
                "/_system/governance" + schema.getPath(), sessionCookie);

        assertTrue(commentBean.getComments()[0].getContent().contentEquals(
                "This schema is added to verify the comments"));

        infoServiceAdminclient.removeComment(commentBean.getComments()[0]
                                                     .getCommentPath(), sessionCookie);

    }

    @AfterClass(groups = "wso2.greg", alwaysRun = true, description = "cleaning up the artifacts added")
    public void tearDown() throws GovernanceException {
        schemaManager.removeSchema(schema.getId());
        schema = null;
        schemaManager = null;
        governanceRegistry = null;
        infoServiceAdminclient = null;

    }
}
