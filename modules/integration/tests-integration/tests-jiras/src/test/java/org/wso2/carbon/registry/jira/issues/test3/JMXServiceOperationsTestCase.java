/*
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.registry.jira.issues.test3;

import static org.testng.Assert.assertTrue;

import java.rmi.RemoteException;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.integration.common.admin.client.LogViewerClient;
import org.wso2.carbon.integration.common.admin.client.UserManagementClient;
import org.wso2.carbon.integration.common.utils.ClientConnectionUtil;
import org.wso2.carbon.logging.view.stub.types.carbon.LogEvent;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.subscription.JMXClient;

public class JMXServiceOperationsTestCase extends GREGIntegrationBaseTest{

    private UserManagementClient userManagementClient;
    private JMXClient jmxClient;

    private static final String ROLE_NAME = "testJMXRole";
    private static final String USER_NAME = "testJMXUser";

    @BeforeClass(alwaysRun = true)
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    public void initialize() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String sessionCookie = getSessionCookie();
        userManagementClient =
                new UserManagementClient(backendURL, sessionCookie);
    }


    @Test(groups = "wso2.greg", expectedExceptions = java.lang.Exception.class,  description = "Login to JMX console as a non admin")
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    public void testnonAdminJMXConnect() throws Exception {
    	String sessionCookie = getSessionCookie();
        userManagementClient =
                new UserManagementClient(backendURL, sessionCookie);
        userManagementClient.addUser(USER_NAME, "weqddgr", null, USER_NAME);

        String permission[] = {"/permission/admin/login"};
        userManagementClient.addRole(ROLE_NAME, new String[]{USER_NAME}, permission);

        jmxClient = new JMXClient();
        jmxClient.connect(USER_NAME, "weqddgr");
    }

    /**
     * Login to JMX console using JMX service URL as admin user and restart the
     * server
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Login to JMX console using as admin user", dependsOnMethods = "testnonAdminJMXConnect")
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    public void testAdminJMXConnect() throws Exception {
        JMXClient jmxClient = new JMXClient();
        jmxClient.connect(automationContext.getContextTenant().getContextUser().getUserName(),
                          automationContext.getContextTenant().getContextUser().getPassword(),
                          "org.wso2.carbon:type=ServerAdmin", "restartGracefully");

        Thread.sleep(6000);

        ClientConnectionUtil.waitForPort(
                Integer.parseInt(automationContext.getDefaultInstance().getPorts ().get("https"))
                , automationContext.getDefaultInstance().getHosts().get ("default"));

        ClientConnectionUtil.waitForLogin(automationContext);

    }

    @AfterClass(alwaysRun = true)
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    public void clean() throws Exception {
        String sessionCookie = getSessionCookie();
        userManagementClient =
                new UserManagementClient(backendURL, sessionCookie);
        userManagementClient.deleteUser(USER_NAME);
        userManagementClient.deleteRole(ROLE_NAME);

    }
}
