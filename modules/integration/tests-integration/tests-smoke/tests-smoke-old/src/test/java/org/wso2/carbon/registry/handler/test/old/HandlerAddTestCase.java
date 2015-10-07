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

package org.wso2.carbon.registry.handler.test.old;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.integration.common.utils.FileManager;
import org.wso2.carbon.registry.handler.stub.ExceptionException;
import org.wso2.greg.integration.common.clients.HandlerManagementServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;

/**
 * A test case which tests registry handler add test
 */
public class HandlerAddTestCase extends GREGIntegrationBaseTest {

    private static final Log log = LogFactory.getLog(HandlerAddTestCase.class);
    private HandlerManagementServiceClient handlerManagementServiceClient;
    private String newHandlerPath;

    @BeforeClass(groups = {"wso2.greg"})
    public void init() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String sessionCookie = getSessionCookie();
        handlerManagementServiceClient = new HandlerManagementServiceClient(backendURL, sessionCookie);
        newHandlerPath = getTestArtifactLocation() + "artifacts" + File.separator + "GREG" +
                File.separator + "handler" + File.separator + "sample-handler.xml";
    }

    @Test(groups = {"wso2.greg"}, description = "Add new Handler")
    public void addNewHandler() throws IOException, ExceptionException {
        assertTrue(handlerManagementServiceClient.createHandler(FileManager.readFile(newHandlerPath)));

    }

    @Test(groups = {"wso2.greg"}, description = "Add same handler again", dependsOnMethods = "addNewHandler",
            expectedExceptions = RemoteException.class)
    public void addSameHandlerAgain() throws IOException, ExceptionException {
        handlerManagementServiceClient.createHandler(FileManager.readFile(newHandlerPath));

    }

    @Test(groups = {"wso2.greg"}, description = "delete handler", dependsOnMethods = "addSameHandlerAgain")
    public void deleteHandler() throws ExceptionException, RemoteException {
        String handlerName = "org.wso2.carbon.registry.extensions.handlers.ServiceMediaTypeHandler";
        assertTrue(handlerManagementServiceClient.deleteHandler(handlerName));

    }
    @AfterClass(alwaysRun = true)
    public void destroy(){
        handlerManagementServiceClient = null;
    }

}
