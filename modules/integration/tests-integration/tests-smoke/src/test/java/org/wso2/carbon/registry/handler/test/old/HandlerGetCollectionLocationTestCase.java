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
import org.wso2.greg.integration.common.clients.HandlerManagementServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;

import java.io.File;

import static org.testng.Assert.assertTrue;

/**
 * A test case which tests registry handler getCollectionLocation operation
 */
public class HandlerGetCollectionLocationTestCase extends GREGIntegrationBaseTest {

    private static final Log log = LogFactory.getLog(HandlerAddTestCase.class);
    private HandlerManagementServiceClient handlerManagementServiceClient;
    private String newHandlerPath;
    private String handlerName = "org.wso2.carbon.registry.extensions.handlers.ServiceMediaTypeHandler";

    @BeforeClass(groups = {"wso2.greg"})
    public void init() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String sessionCookie = getSessionCookie();
        handlerManagementServiceClient = new HandlerManagementServiceClient(backendURL, sessionCookie);
        newHandlerPath = getTestArtifactLocation() + "artifacts" + File.separator + "GREG" +
                File.separator + "handler" + File.separator + "sample-handler.xml";
    }

    @Test(groups = {"wso2.greg"}, description = "Add new Handler")
    public void addNewHandler() throws Exception {
        assertTrue(handlerManagementServiceClient.createHandler(FileManager.readFile(newHandlerPath)));

    }

    @Test(groups = {"wso2.greg"}, description = "Get/set Handler collection Location",
            dependsOnMethods = "addNewHandler")
    public void handlerCollectionLocation() throws Exception {
        String newHandlerPath = "/_system/handler/test/path/";
        String defaultHandlerPath = "/repository/components/org.wso2.carbon.governance/handlers/";
        String path = handlerManagementServiceClient.getHandlerCollectionLocation();
        assertTrue(path.equalsIgnoreCase(defaultHandlerPath), "Handler collection path not returned");
        //set handler path to new value        .
        handlerManagementServiceClient.setHandlerCollectionLocation(newHandlerPath);
        String newPath = handlerManagementServiceClient.getHandlerCollectionLocation();
        assertTrue(newPath.equalsIgnoreCase(newHandlerPath), "Updated handler collection path not returned");
        //set the path back to default
        handlerManagementServiceClient.setHandlerCollectionLocation(defaultHandlerPath);
        String defaultPath = handlerManagementServiceClient.getHandlerCollectionLocation();
        assertTrue(defaultPath.equalsIgnoreCase(defaultHandlerPath), "Updated handler collection path not returned");

    }

    @Test(groups = {"wso2.greg"}, description = "delete handler", dependsOnMethods = "handlerCollectionLocation")
    public void deleteHandler() throws Exception {
        assertTrue(handlerManagementServiceClient.deleteHandler(handlerName));

    }
    @AfterClass(alwaysRun = true)
    public void destroy(){
        handlerManagementServiceClient = null;
    }


}
