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

package org.wso2.carbon.registry.metadata.test.service.old;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.governance.GovernanceServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;

import static org.testng.Assert.assertNotEquals;

/**
 * A test case which tests registry service get configuration operation
 */
public class ServiceGetConfigurationTestCase {
    private static final Log log = LogFactory.getLog(ServiceEditTestCase.class);
    private GovernanceServiceClient addServicesServiceClient;
    private ManageEnvironment environment;
    private UserInfo userInfo;


    @BeforeClass(groups = {"wso2.greg"})
    public void init() throws Exception {
        log.info("Initializing Get Service Configuration Tests");
        log.debug("Add Service Resource Initialised");
        int userId = ProductConstant.ADMIN_USER_ID;
        userInfo = UserListCsvReader.getUserInfo(userId);
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        environment = builder.build();
        addServicesServiceClient =
                new GovernanceServiceClient(environment.getGreg().getBackEndUrl(),
                                            environment.getGreg().getSessionCookie());

    }

    @Test(groups = {"wso2.greg"})
    public void getServiceConfigurationTest() throws Exception {
        log.debug("Running SuccessCase");
        String serviceConfiguration = addServicesServiceClient.getServiceConfiguration();
        assertNotEquals(serviceConfiguration.indexOf("<table name=\"Overview\">"), -1);
    }

    @AfterClass(groups = {"wso2.greg"})
    public void deleteResources() throws AxisFault {
        addServicesServiceClient = null;
        environment = null;
        userInfo = null;
    }


}
