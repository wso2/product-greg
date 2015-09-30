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
*//*


package org.wso2.carbon.registry.mediatype.test;

import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.greg.integration.common.clients.ServerAdminClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;

import static org.testng.AssertJUnit.assertTrue;

public class ServerRestartTest extends GREGIntegrationBaseTest {

    private String sessionCookie;
    private String backEndUrl;
    private String userName;
    private String userNameWithoutDomain;

    public void testRestart() throws Exception {
        System.out.println("Tessssss....... ");

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        backEndUrl = getBackendURL();
        sessionCookie = getSessionCookie();
        userName = automationContext.getContextTenant().getContextUser().getUserName();
        userNameWithoutDomain = userName.substring(0, userName.indexOf('@'));

        ServerAdminClient serverAdminClient =
                new ServerAdminClient(backEndUrl,
                                      sessionCookie);

        FrameworkProperties frameworkProperties =
                FrameworkFactory.getFrameworkProperties(ProductConstant.GREG_SERVER_NAME);
        ServerGroupManager.getServerUtils().restartGracefully(serverAdminClient, frameworkProperties);
    }

    public void test1() {
        System.out.println("XXXXXXXX test1 ...................");
    }

    public void test2() {
        System.out.println("XXXXXXXX test2 ...................");
    }

    public void test3() {
        assertTrue(false);
    }
}
*/
