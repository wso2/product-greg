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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.registry.HandlerManagementServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.core.utils.fileutils.FileManager;
import org.wso2.carbon.registry.handler.stub.ExceptionException;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;

/**
 * A test case which tests registry handler update operation
 */

public class HandlerUpdateTestCase {
    private static final Log log = LogFactory.getLog(HandlerAddTestCase.class);
    private int userId = 1;
    UserInfo userInfo = UserListCsvReader.getUserInfo(userId);
    private HandlerManagementServiceClient handlerManagementServiceClient;
    private String newHandlerPath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION
            + "artifacts" + File.separator + "GREG" + File.separator
            + "handler" + File.separator + "sample-handler.xml";
    private String handlerName = "org.wso2.carbon.registry.extensions.handlers.ServiceMediaTypeHandler";


    @BeforeClass(groups = {"wso2.greg"})
    public void init() throws Exception {
        log.info("Initializing Add Handler Test");
        log.debug("Add Handler Test Initialised");
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        ManageEnvironment environment = builder.build();
        handlerManagementServiceClient = new HandlerManagementServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                userInfo.getUserName(), userInfo.getPassword());
    }


    @Test(groups = {"wso2.greg"}, description = "Add new Handler")
    public void addNewHandler() throws IOException, ExceptionException,
                                       org.wso2.carbon.registry.handler.stub.ExceptionException {


        assertTrue(handlerManagementServiceClient.createHandler(FileManager.readFile(newHandlerPath)));


    }

    @Test(groups = {"wso2.greg"}, description = "Update Handler", dependsOnMethods = "addNewHandler")
    public void updateHandler() throws IOException, ExceptionException {


        assertTrue(handlerManagementServiceClient.updateHandler(handlerName, FileManager.readFile(newHandlerPath)));


    }

    @Test(groups = {"wso2.greg"}, description = "delete handler", dependsOnMethods = "updateHandler")
    public void deleteHandler() throws ExceptionException, RemoteException {


        assertTrue(handlerManagementServiceClient.deleteHandler(handlerName));


    }


}
