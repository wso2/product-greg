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
import org.wso2.carbon.automation.api.clients.logging.LogViewerClient;
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
import org.wso2.carbon.logging.view.stub.types.carbon.LogEvent;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import java.io.File;
import java.io.IOException;

import static org.testng.Assert.assertTrue;

public class Carbon9160 {
    private int userId = 2;
    UserInfo userInfo = UserListCsvReader.getUserInfo(userId);
    private Registry governance;
    private LogViewerClient logViewerClient;


    @BeforeClass
    public void init() throws Exception {

        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        ManageEnvironment environment = builder.build();

        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        WSRegistryServiceClient registry = registryProviderUtil.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        governance = registryProviderUtil.getGovernanceRegistry(registry, userId);
        logViewerClient = new LogViewerClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                              environment.getGreg().getSessionCookie());
    }

    @Test(groups = "wso2.greg", description = "Add schema which has a invalid schema import",
          expectedExceptions = GovernanceException.class)
    public void addInvalidSchema() throws RegistryException, IOException {

        addSchema();
        LogEvent[] logEvents = logViewerClient.getLogs("ERROR", "Failed to add resource /SchemaImportInvalidSample.xsd","","");
        boolean errorMessageGiven = false;
        for (LogEvent logEvent : logEvents) {
            if (logEvent.getMessage().equals("Failed to add resource /SchemaImportInvalidSample.xsd. " +
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
        String schemaFilePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
                                File.separator + "GREG" + File.separator + "schema" + File.separator + "SchemaImportInvalidSample.xsd";
        Schema schema = schemaManager.newSchema(FileManager.readFile(schemaFilePath).getBytes(), "SchemaImportInvalidSample.xsd");
        schemaManager.addSchema(schema);

    }

    @AfterClass
    public void cleanup() throws GovernanceException {
        governance = null;
        userInfo = null;
        logViewerClient = null;
    }
}
