package org.wso2.carbon.registry;
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

import org.testng.annotations.*;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A test case which tests logging in & logging out of a Carbon core server
 */
public class LoginLogoutTestCase {
    private static final Log log = LogFactory.getLog(LoginLogoutTestCase.class);

    private LoginLogoutUtil util = new LoginLogoutUtil();

    //    @BeforeGroups(groups = {"wso2.greg", "wso2.greg.schema.a", "wso2.greg.schema.b", "wso2.greg.schema.c", "wso2.greg.policy.a",
//            "wso2.greg.policy.b", "wso2.greg.policy.c", "wso2.greg.wsdl.a", "wso2.greg.wsdl.b", "wso2.greg.wsdl.c"})
    public void login() throws Exception {
        log.info("--------- waiting for Port - Logging In -------- ");
        ClientConnectionUtil.waitForPort(9763);
        util.login();
    }

    //    @Test(groups = {"wso2.greg", "wso2.greg.schema.a", "wso2.greg.schema.b", "wso2.greg.schema.c", "wso2.greg.policy.a",
//            "wso2.greg.policy.b", "wso2.greg.policy.c", "wso2.greg.wsdl.a", "wso2.greg.wsdl.b", "wso2.greg.wsdl.c"})
    public void loginLogoutGreetingService() {
        log.info("------- Inside Login Logout Greeting Service --------");
    }

    //    @AfterGroups(groups = {"wso2.greg", "wso2.greg.schema.a", "wso2.greg.schema.b", "wso2.greg.schema.c", "wso2.greg.policy.a",
//            "wso2.greg.policy.b", "wso2.greg.policy.c", "wso2.greg.wsdl.a", "wso2.greg.wsdl.b", "wso2.greg.wsdl.c"})
    public void logout() throws Exception {
        log.info(" ------ waiting for Port - Logging Out --------");
        ClientConnectionUtil.waitForPort(9763);
        util.logout();
    }
}
