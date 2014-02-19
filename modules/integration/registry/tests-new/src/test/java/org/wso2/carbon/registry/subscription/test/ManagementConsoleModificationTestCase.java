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
package org.wso2.carbon.registry.subscription.test;

import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.rmi.RemoteException;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.registry.InfoServiceAdminClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.info.stub.RegistryExceptionException;
import org.wso2.carbon.registry.info.stub.beans.xsd.SubscriptionBean;
import org.wso2.carbon.registry.subscription.test.util.JMXSubscription;
import org.wso2.carbon.registry.subscription.test.util.ManagementConsoleSubscription;

import javax.management.InstanceNotFoundException;
import javax.management.ListenerNotFoundException;

public class ManagementConsoleModificationTestCase {
    private ManageEnvironment environment;
    private final int userID = ProductConstant.ADMIN_USER_ID;
    private UserInfo userInfo;
    private String sessionID = "";
    private JMXSubscription jmxSubscription = new JMXSubscription();

    private static final String ROOT = "/";

    @BeforeClass(alwaysRun = true)
    public void initialize() throws RemoteException, LoginAuthenticationExceptionException {
        userInfo = UserListCsvReader.getUserInfo(userID);
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userID);
        environment = builder.build();
        sessionID = environment.getGreg().getSessionCookie();
    }

    /**
     * add subscription to root collection and send notification via Management
     * Console
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Get Management Console Notification")
    public void testConsoleSubscription() throws Exception {
        assertTrue(ManagementConsoleSubscription.init(ROOT, "CollectionUpdated", environment,
                                                      userInfo));
    }

    /**
     * delete Management Console subscription to root collection
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Unsubscribe Management Console Notification", dependsOnMethods = "testConsoleSubscription")
    public void testConsoleUnsubscription() throws Exception {
        InfoServiceAdminClient infoServiceAdminClient =
                new InfoServiceAdminClient(
                        environment.getGreg()
                                .getProductVariables()
                                .getBackendUrl(),
                        userInfo.getUserName(),
                        userInfo.getPassword());
        SubscriptionBean sBean = infoServiceAdminClient.getSubscriptions(ROOT, sessionID);
        infoServiceAdminClient.unsubscribe(ROOT, sBean.getSubscriptionInstances()[0].getId(),
                                           sessionID);
        sBean = infoServiceAdminClient.getSubscriptions(ROOT, sessionID);
        assertNull(sBean.getSubscriptionInstances(), "Error removing subscriptions");
    }

    /**
     * Add subscription to root collection and send notification via JMX
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Get JMX Notification", dependsOnMethods = "testConsoleUnsubscription")
    public void testJMXSubscription() throws Exception {
        assertTrue(jmxSubscription.init(ROOT, "CollectionUpdated", environment, userInfo));
    }

    @AfterClass(alwaysRun = true)
    public void clean() throws RegistryException, RegistryExceptionException, IOException,
                               ListenerNotFoundException, InstanceNotFoundException {
        InfoServiceAdminClient infoServiceAdminClient =
                new InfoServiceAdminClient(
                        environment.getGreg()
                                .getProductVariables()
                                .getBackendUrl(),
                        userInfo.getUserName(),
                        userInfo.getPassword());
        String sessionID = environment.getGreg().getSessionCookie();
        SubscriptionBean sBean = infoServiceAdminClient.getSubscriptions(ROOT, sessionID);
        infoServiceAdminClient.unsubscribe(ROOT, sBean.getSubscriptionInstances()[0].getId(),
                                           sessionID);
        jmxSubscription.disconnect();
        infoServiceAdminClient = null;
        environment = null;
        jmxSubscription = null;
    }
}
