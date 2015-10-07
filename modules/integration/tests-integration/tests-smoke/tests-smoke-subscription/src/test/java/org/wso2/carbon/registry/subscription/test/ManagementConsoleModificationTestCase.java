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

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.registry.info.stub.beans.xsd.SubscriptionBean;
import org.wso2.greg.integration.common.clients.InfoServiceAdminClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.subscription.JMXSubscription;
import org.wso2.greg.integration.common.utils.subscription.ManagementConsoleSubscription;

import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class ManagementConsoleModificationTestCase extends GREGIntegrationBaseTest {

    private JMXSubscription jmxSubscription = new JMXSubscription();
    private static final String ROOT = "/";
    private String sessionID;

    @BeforeClass(alwaysRun = true)
    public void initialize() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        sessionID = getSessionCookie();
    }

    /**
     * add subscription to root collection and send notification via Management
     * Console
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Get Management Console Notification")
    public void testConsoleSubscription() throws Exception {
        assertTrue(ManagementConsoleSubscription.init(ROOT, "CollectionUpdated", automationContext));
    }

    /**
     * delete Management Console subscription to root collection
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Unsubscribe Management Console Notification",
            dependsOnMethods = "testConsoleSubscription")
    public void testConsoleUnsubscription() throws Exception {
        InfoServiceAdminClient infoServiceAdminClient = new InfoServiceAdminClient(backendURL, sessionID);
        SubscriptionBean sBean = infoServiceAdminClient.getSubscriptions(ROOT, sessionID);
        infoServiceAdminClient.unsubscribe(ROOT, sBean.getSubscriptionInstances()[0].getId(), sessionID);
        sBean = infoServiceAdminClient.getSubscriptions(ROOT, sessionID);
        assertNull(sBean.getSubscriptionInstances(), "Error removing subscriptions");
    }

    /**
     * Add subscription to root collection and send notification via JMX
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Get JMX Notification",
            dependsOnMethods = "testConsoleUnsubscription")
    public void testJMXSubscription() throws Exception {
        assertTrue(jmxSubscription.init(ROOT, "CollectionUpdated", automationContext));
    }

    @AfterClass(alwaysRun = true)
    public void clean() throws Exception {
        InfoServiceAdminClient infoServiceAdminClient = new InfoServiceAdminClient(backendURL, sessionID);
        String sessionID = getSessionCookie();
        SubscriptionBean sBean = infoServiceAdminClient.getSubscriptions(ROOT, sessionID);
        infoServiceAdminClient.unsubscribe(ROOT, sBean.getSubscriptionInstances()[0].getId(), sessionID);
        jmxSubscription.disconnect();
    }
}
