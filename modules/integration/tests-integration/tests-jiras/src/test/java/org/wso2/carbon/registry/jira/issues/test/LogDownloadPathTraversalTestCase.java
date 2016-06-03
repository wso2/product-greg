/*
*Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.greg.integration.common.clients.LogViewerClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.apache.axis2.AxisFault;
import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.logging.view.stub.LogViewerException;

import javax.activation.DataHandler;
import java.io.IOException;
import java.io.InputStream;

/**
 * This test case addresses the path traversal security vulnerability in archived log download requests,
 */
public class LogDownloadPathTraversalTestCase extends GREGIntegrationBaseTest {

    private LogViewerClient logViewerClient;
    private String sessionCookie;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        sessionCookie = getSessionCookie();
        logViewerClient = new LogViewerClient(backendURL, sessionCookie);
    }

    @Test(groups = "wso2.as", description = "Download archived logfile")
    public void testDownloadArchivedLogFiles() throws Exception {
        String logFileContent = downloadLogFile("wso2carbon.log");
        Assert.assertTrue(logFileContent.contains("@carbon.super [-1234]' logged in at"),
                "Downloaded log file does not contain required logged event");
    }

    @Test(groups = "wso2.as", description = "Download non existing file")
    public void testDownloadArchivedLogFilesErrorCase1() throws Exception {
        try {
            downloadLogFile("anyfile");
        } catch (Exception e) {
            Assert.assertTrue(e instanceof AxisFault && e.getMessage().contains("Error getting the file input stream"));
        }
    }

    @Test(groups = "wso2.as", description = "Download path traversed file")
    public void testDownloadArchivedLogFilesErrorCase2() throws Exception {
        try {
            downloadLogFile("../../repository/conf/registry.xml");
        } catch (Exception e) {
            Assert.assertTrue(e instanceof AxisFault && e.getMessage().contains("Error getting the file input stream"));
        }
    }

    private String downloadLogFile(String logFileName) throws LogViewerException, IOException {
        LogViewerClient logViewerClient = new LogViewerClient(backendURL, sessionCookie);
        DataHandler logFileDataHandler = logViewerClient.downloadArchivedLogFiles(logFileName, "", "");
        InputStream logFileInputStream = logFileDataHandler.getInputStream();
        return IOUtils.toString(logFileInputStream);
    }
}
