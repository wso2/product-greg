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

import junit.framework.Assert;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.core.TestTemplate;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.registry.handler.stub.HandlerManagementServiceStub;

/**
 * A test case which tests registry handler update operation
 */

public class HandlerUpdateTestCase {
    private static final Log log = LogFactory.getLog(HandlerUpdateTestCase.class);
    private String loggedInSessionCookie = "";
    private LoginLogoutUtil util = new LoginLogoutUtil();
    private String frameworkPath = "";


    @BeforeClass(groups = {"wso2.greg"})
    public void init() throws Exception {
        log.info("Initializing Update Handler Test");
        log.debug("Update Handler Test Initialised");
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


            System.out.println(handlerResource);
            handlerManagementServiceStub.createHandler(HandlerAddTestCase.fileReader(handlerResource));

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Failed to add handler configuration " + e);
        }

        try {
            handlerManagementServiceStub.updateHandler(handlerName, HandlerAddTestCase.fileReader(handlerResource));

            String handlerContent = handlerManagementServiceStub.getHandlerConfiguration(handlerName);
            if (handlerContent.indexOf("org.wso2.carbon.registry.extensions.handlers.ServiceMediaTypeHandler") != -1) {
                log.info("Handler Configuration not updated");

            } else {
                log.error("Handler configuration not updated");
                Assert.fail("Handler configuration not updated");
            }

            try {
                handlerManagementServiceStub.deleteHandler(handlerName);
            } catch (Exception e) {
                e.printStackTrace();
                Assert.fail("Failed to delete the handler" + e.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Failed to add handler configuration " + e);
        }
    }


}
