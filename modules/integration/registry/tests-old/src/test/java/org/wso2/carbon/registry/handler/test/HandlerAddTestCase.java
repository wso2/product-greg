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

package org.wso2.carbon.registry.handler.test;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.core.TestTemplate;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.registry.handler.stub.HandlerManagementServiceStub;

import java.io.DataInputStream;
import java.io.FileInputStream;

/**
 * A test case which tests registry handler add test
 */

public class HandlerAddTestCase {


    private static final Log log = LogFactory.getLog(HandlerAddTestCase.class);
    private String loggedInSessionCookie = "";
    private String frameworkPath = "";
    private LoginLogoutUtil util = new LoginLogoutUtil();

    @BeforeClass(groups = {"wso2.greg"})
    public void init() throws Exception {
        log.info("Initializing Add Handler Test");
        log.debug("Add Handler Test Initialised");
        loggedInSessionCookie = util.login();
        frameworkPath = FrameworkSettings.getFrameworkPath();
    }

    @Test(groups = {"wso2.greg"})
    public void runSuccessCase() {
        log.debug("Running SuccessCase");
        String sampleHandlerName = "sample-handler.xml";
        String handlerName = "org.wso2.carbon.registry.extensions.handlers.ServiceMediaTypeHandler";
        HandlerManagementServiceStub handlerManagementServiceStub =
                TestUtils.getHandlerManagementServiceStub(loggedInSessionCookie);

        String handlerResource = TestUtils.getHandlerResourcePath(frameworkPath);
        try {
            handlerManagementServiceStub.createHandler(fileReader(handlerResource));

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Failed to add handler configuration " + e);
        }

        //Trying to add the same handler again.
        try {
            handlerManagementServiceStub.createHandler(fileReader(handlerResource));
        } catch (AxisFault e) {
            log.info("Handler already exists" + e.getMessage());

        } catch (Exception e) {
            e.printStackTrace();
        }

        //Delete the handler
        try {
            handlerManagementServiceStub.deleteHandler(handlerName);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Failed to delete the handler" + e.getMessage());
        }
    }

    @AfterClass(groups = {"wso2.greg"})
    public void logout() throws Exception {
        util.logout();
    }

//    @Override
//    public void runFailureCase() {
//
//    }
//
//    @Override
//    public void cleanup
//            () {
//
//    }

    public static String fileReader(String fileName) {
        String fileContent = "";
        try {
            // Open the file that is the first
            // command line parameter
            FileInputStream fstream = new
                    FileInputStream(fileName);

            // Convert our input stream to a
            // DataInputStream
            DataInputStream in =
                    new DataInputStream(fstream);

            // Continue to read lines while
            // there are still some left to read

            while (in.available() != 0) {
                fileContent = fileContent + (in.readLine());
            }

            in.close();
        } catch (Exception e) {
            System.err.println("File input error");
        }
        return fileContent;

    }
}
