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

package org.wso2.carbon.registry.jira.issues.test;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.schema.SchemaManager;
import org.wso2.carbon.governance.api.schema.dataobjects.Schema;
import org.wso2.carbon.integration.common.admin.client.LogViewerClient;
import org.wso2.carbon.integration.common.utils.FileManager;
import org.wso2.carbon.logging.view.stub.types.carbon.LogEvent;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import java.io.File;
import java.io.IOException;

import static org.testng.Assert.assertTrue;

public class Carbon9160TestCase extends GREGIntegrationBaseTest {

    private Registry governance;
    private LogViewerClient logViewerClient;
    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String session = getSessionCookie();
        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        WSRegistryServiceClient registry = registryProviderUtil.getWSRegistry(automationContext);
        governance = registryProviderUtil.getGovernanceRegistry(registry, automationContext);
        logViewerClient = new LogViewerClient(backendURL, session);
    }
    @Test(groups = "wso2.greg", description = "Add schema which has a invalid schema import",
            expectedExceptions = GovernanceException.class)
    public void addInvalidSchema() throws RegistryException, IOException {
        addSchema();
        LogEvent[] logEvents = logViewerClient.getLogs("ERROR", "Failed to add resource /SchemaImportInvalidSample.xsd", "", "");
        boolean errorMessageGiven = false;
        for(LogEvent logEvent : logEvents) {
            if(logEvent.getMessage().equals("Failed to add resource /SchemaImportInvalidSample.xsd. " +
                    "Could not read the XML Schema Definition file. https://svn.wso2.org/repos/wso2/carbon/platform/trunk/" +
                    "platform-integration/system-test-framework/core/org.wso2.automation.platform.core/src/main/resources/" +
                    "artifacts/GREG/schema/abc.xsd")) {
                errorMessageGiven = true;
            }

        }
        assertTrue(errorMessageGiven, "Error is not Granted");
    }
    public void addSchema() throws IOException, RegistryException {
        SchemaManager schemaManager = new SchemaManager(governance);
        String schemaFilePath = getTestArtifactLocation() + "artifacts" +
                File.separator + "GREG" + File.separator + "schema" + File.separator + "SchemaImportInvalidSample.xsd";
        Schema schema = schemaManager.newSchema(FileManager.readFile(schemaFilePath).getBytes(), "SchemaImportInvalidSample.xsd");
        schemaManager.addSchema(schema);

    }
    @AfterClass
    public void cleanup() throws GovernanceException {
        governance = null;
        logViewerClient = null;
    }
}
