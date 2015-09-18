/*
 * Copyright 2015 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.registry.jira.issues.test;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.integration.common.admin.client.LogViewerClient;
import org.wso2.carbon.logging.view.stub.LogViewerLogViewerException;
import org.wso2.carbon.logging.view.stub.types.carbon.LogEvent;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;

import java.rmi.RemoteException;

import static org.testng.Assert.assertNull;

public class Registry2977TestCase extends GREGIntegrationBaseTest {

    private LogViewerClient logViewerClient;

    @BeforeClass(alwaysRun = true)
    public void initializeTests() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String session = getSessionCookie();
        logViewerClient = new LogViewerClient(backendURL, session);

    }

    /**
     * Check the Solr date format exception is logged when server startup.
     *
     * @throws RemoteException
     * @throws LogViewerLogViewerException
     *
     */
    @Test(groups = "wso2.greg", description = "Checks whether solar date format error exists ")
    public void testIndexingLocaleError()
            throws LogViewerLogViewerException, RemoteException {

        String ADD_LOG = "Error when passing date to create solr date format.";
        LogEvent[] logEvents = readLogs(ADD_LOG);
        assertNull(logEvents, "Exception thrown");
    }

    /**
     * @param   errorMessage Error message that needs to be checked
     * @throws  RemoteException
     * @throws  LogViewerLogViewerException
     */
    public LogEvent[] readLogs(String errorMessage) throws RemoteException, LogViewerLogViewerException {
        LogEvent[] logEvents = logViewerClient.getRemoteLogs("ERROR", errorMessage, "", "");
        return logEvents;

    }

}
