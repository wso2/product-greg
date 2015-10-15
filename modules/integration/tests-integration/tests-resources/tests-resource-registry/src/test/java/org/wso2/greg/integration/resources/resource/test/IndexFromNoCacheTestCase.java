/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.greg.integration.resources.resource.test;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.integration.common.admin.client.LogViewerClient;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.logging.view.stub.LogViewerLogViewerException;
import org.wso2.carbon.logging.view.stub.types.carbon.LogEvent;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.indexing.IndexingManager;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.clients.LoggingAdminClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import java.rmi.RemoteException;

import static org.testng.Assert.assertNotNull;

public class IndexFromNoCacheTestCase extends GREGIntegrationBaseTest {

    private WSRegistryServiceClient wsRegistry;
    private LogViewerClient logViewerClient;
    private static volatile IndexingManager instance;
    String path = "/_system/governance/test";

    @BeforeClass(groups = "wso2.greg", alwaysRun = true) public void initialize() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_USER);
        String sessionCookie = new LoginLogoutClient(automationContext).login();

        RegistryProviderUtil provider = new RegistryProviderUtil();
        wsRegistry = provider.getWSRegistry(automationContext);

        LoggingAdminClient loggingAdminClient = new LoggingAdminClient(backendURL, sessionCookie);
        loggingAdminClient.updateLoggerData("org.wso2.carbon.registry.indexing", "DEBUG", true, true);
        logViewerClient = new LogViewerClient(backendURL, sessionCookie);
    }

    @Test(groups = "wso2.greg", description = "Add resource for indexing and check it is fetched from database")
    public void testResoucesFetchFromDatabase()
            throws RegistryException, InterruptedException, LogViewerLogViewerException, RemoteException {

        Resource resource = wsRegistry.newResource();
        resource.setContent("Adding resource for indexing");
        wsRegistry.put(path, resource);
        Thread.sleep(1000 * 15);
        LogEvent[] logEvents = readLogs("Resource at path \"" + path + "\" is added as a no cache path");
        assertNotNull(logEvents, "Resource not added as no cache path");
    }

    @Test(groups = "wso2.greg", description = "Remove resource from no cache path")
    public void testResoucesremoveFromNoCachePath()
            throws RegistryException, InterruptedException, LogViewerLogViewerException, RemoteException {

        Thread.sleep(1000 * 5);
        LogEvent[] logEvents = readLogs("Resource at path \"" + path + "\" is removed from no cache path");
        assertNotNull(logEvents, "Resource not removed from no cache path");
    }

    /**
     * @param   errorMessage Error message that needs to be checked
     * @throws  RemoteException
     * @throws  LogViewerLogViewerException
     */
    public LogEvent[] readLogs(String errorMessage) throws RemoteException, LogViewerLogViewerException {
        LogEvent[] logEvents = logViewerClient.getRemoteLogs("DEBUG", errorMessage, "", "");
        return logEvents;
    }

    @AfterClass(groups = "wso2.greg", alwaysRun = true, description = "cleaning up the artifacts added") public void cleanUp()
            throws RegistryException {
        if (wsRegistry.resourceExists(path)) {
            wsRegistry.delete(path);
        }
        logViewerClient = null;
        wsRegistry = null;
    }
}
