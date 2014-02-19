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
package org.wso2.carbon.registry.jira.issues.test;

import java.rmi.RemoteException;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.logging.LogViewerClient;
import org.wso2.carbon.automation.api.clients.user.mgt.UserManagementClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.core.annotations.SetEnvironment;
import org.wso2.carbon.automation.core.utils.ClientConnectionUtil;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.logging.view.stub.types.carbon.LogEvent;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.subscription.test.util.JMXClient;

public class JMXServiceOperationsTestCase {

    private ManageEnvironment environment;
    private UserInfo userInfo;
    private UserManagementClient userManagementClient;
    private EnvironmentBuilder builder;
    private JMXClient jmxClient;
    private int userID = ProductConstant.ADMIN_USER_ID;

    private static final String ROLE_NAME = "testJMXRole";
    private static final String USER_NAME = "testJMXUser";

    @BeforeClass(alwaysRun = true)
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_user})
    public void initialize() throws RemoteException, LoginAuthenticationExceptionException,
                                    RegistryException {
        userInfo = UserListCsvReader.getUserInfo(userID);
        builder = new EnvironmentBuilder().greg(userID);
        environment = builder.build();
        userManagementClient =
                new UserManagementClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                         environment.getGreg().getSessionCookie());
    }

    /**
     * Login to JMX console using JMX service URL as a non admin user
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", expectedExceptions = java.lang.Exception.class,
          expectedExceptionsMessageRegExp = "infoAdminServiceStub Initialization fail Unauthorized access attempt to JMX operation. ",
          description = "Login to JMX console as a non admin")
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_user})
    public void testnonAdminJMXConnect() throws Exception {
        addUser();
        addRole();
        jmxClient = new JMXClient();
        jmxClient.connect(USER_NAME, "weqddgr");

    }

    /**
     * Login to JMX console using JMX service URL as admin user and restart the
     * server
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Login to JMX console using as admin user")
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_user})
    public void testAdminJMXConnect() throws Exception {
        JMXClient jmxClient = new JMXClient();
        jmxClient.connect(userInfo.getUserName(), userInfo.getPassword(),
                          "org.wso2.carbon:type=ServerAdmin", "restartGracefully");

        Thread.sleep(6000);
        environment = builder.build();
        ClientConnectionUtil.waitForPort(Integer.parseInt(environment.getGreg().getProductVariables().getHttpPort()),
                                         environment.getGreg().getProductVariables().getHostName());

        ClientConnectionUtil.waitForLogin(Integer.parseInt(environment.getGreg().getProductVariables().getHttpPort()),
                                          environment.getGreg().getProductVariables().getHostName(),
                                          environment.getGreg().getBackEndUrl());

        builder = new EnvironmentBuilder().greg(userID);
        environment = builder.build();
        LogViewerClient logViewerClient =
                new LogViewerClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                    environment.getGreg().getSessionCookie());
        userManagementClient =
                new UserManagementClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                         environment.getGreg().getSessionCookie());


        LogEvent[] logEvents = logViewerClient.getLogs("INFO", "WSO2 Carbon started in ", "", "");
        Assert.assertTrue(logEvents[0].getMessage().contains("WSO2 Carbon started in "));

    }

    /**
     * add a user
     *
     * @throws Exception
     */
    private void addUser() throws Exception {
        userManagementClient.addUser(USER_NAME, "weqddgr", null, USER_NAME);

    }

    /**
     * add a role
     *
     * @throws Exception
     */
    private void addRole() throws Exception {
        String permission[] = {"/permission/admin/login"};
        userManagementClient.addRole(ROLE_NAME, new String[]{USER_NAME}, permission);

    }

    @AfterClass(alwaysRun = true)
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_user})
    public void clean() throws Exception {
        userManagementClient.deleteUser(USER_NAME);
        userManagementClient.deleteRole(ROLE_NAME);
        environment = null;
        userInfo = null;
        userManagementClient = null;
        builder = null;
        jmxClient = null;

    }
}
