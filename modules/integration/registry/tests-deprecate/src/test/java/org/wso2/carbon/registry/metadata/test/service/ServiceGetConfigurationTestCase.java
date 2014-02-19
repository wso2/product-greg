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

package org.wso2.carbon.registry.metadata.test.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.governance.services.stub.AddServicesServiceStub;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.registry.metadata.test.util.TestUtils;

import static org.testng.Assert.fail;

/**
 * A test case which tests registry service get configuration operation
 */
public class ServiceGetConfigurationTestCase {
    private static final Log log = LogFactory.getLog(ServiceEditTestCase.class);
    private AddServicesServiceStub addServicesServiceStub;
    private String loggedInSessionCookie = "";
    private LoginLogoutUtil util = new LoginLogoutUtil();


    @BeforeClass(groups = {"wso2.greg.service.c"})
    public void init() throws Exception {
        log.info("Initializing Get Service Configuration Tests");
        log.debug("Add Service Resource Initialised");
        loggedInSessionCookie = util.login();

    }

    @Test(groups = {"wso2.greg.service.c"}, dependsOnGroups = {"wso2.greg.service.b"})
    public void runSuccessCase() {
        log.debug("Running SuccessCase");
        addServicesServiceStub = TestUtils.getAddServicesServiceStub(loggedInSessionCookie);

        try {
            String serviceConfiguration = addServicesServiceStub.getServiceConfiguration();

            if (serviceConfiguration.indexOf("<table name=\"Overview\">") != -1) {
                log.info("service configuration content found");

            } else {
                log.error("service configuration content not found");
                fail("service configuration content not found");
            }
        } catch (Exception e) {
            fail("Unable to get text content " + e);
            log.error(" : " + e.getMessage());
        }
    }


}
