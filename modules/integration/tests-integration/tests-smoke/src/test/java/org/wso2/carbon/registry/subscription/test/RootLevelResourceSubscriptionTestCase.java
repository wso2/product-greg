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
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.subscription.JMXSubscription;
import org.wso2.greg.integration.common.utils.subscription.ManagementConsoleSubscription;

import javax.activation.DataHandler;
import javax.management.InstanceNotFoundException;
import javax.management.ListenerNotFoundException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;

public class RootLevelResourceSubscriptionTestCase extends GREGIntegrationBaseTest {

    private ResourceAdminServiceClient resourceAdminServiceClient;
    private static final String RESOURCE_PATH_NAME = "/";
    private JMXSubscription jmxSubscription = new JMXSubscription();
    private String userNameWithoutDomain;

    @DataProvider(name = "ResourceDataProvider")
    public Object[][] dp() {
        return new Object[][]{new Object[]{"testresource.txt"}, new Object[]{"pom.xml"}, new Object[]{"resource.txt"}};
    }

    @BeforeClass(alwaysRun = true)
    public void initialize() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String sessionCookie = getSessionCookie();
        resourceAdminServiceClient = new ResourceAdminServiceClient(backendURL, sessionCookie);
        String userName = automationContext.getContextTenant().getContextUser().getUserName();
        if (userName.contains("@")){
            userNameWithoutDomain = userName.substring(0, userName.indexOf('@'));
        }
        else {
            userNameWithoutDomain = userName;
        }
    }

    /**
     * add resources to root
     *
     * @param resourceName
     * @throws MalformedURLException
     * @throws RemoteException
     * @throws ResourceAdminServiceExceptionException
     *
     */
    @Test(groups = "wso2.greg", description = "Add resource", dataProvider = "ResourceDataProvider")
    public void testAddResource(String resourceName) throws MalformedURLException, RemoteException,
            ResourceAdminServiceExceptionException {
        String resourcePath = getTestArtifactLocation()  + "artifacts" +
                File.separator + "GREG" + File.separator + resourceName;

        DataHandler dh = new DataHandler(new URL("file:///" + resourcePath));

        resourceAdminServiceClient.addResource(RESOURCE_PATH_NAME + resourceName, "test/plain", "testDesc", dh);

        assertTrue(resourceAdminServiceClient.getResource(RESOURCE_PATH_NAME + resourceName)[0]
                .getAuthorUserName().contains(userNameWithoutDomain));
    }

    /**
     * Add subscription to a root level resource and send notification via
     * Management Console
     *
     * @param resourceName
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Get Management Console Notification",
            dataProvider = "ResourceDataProvider", dependsOnMethods = "testAddResource")
    public void testConsoleSubscription(String resourceName) throws Exception {
        assertTrue(ManagementConsoleSubscription.init(RESOURCE_PATH_NAME + resourceName,
                "ResourceUpdated", automationContext));
    }

    /**
     * Add subscription to a root level resource and send notification via JMX
     *
     * @param resourceName
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Get JMX Notification",
            dataProvider = "ResourceDataProvider", dependsOnMethods = "testConsoleSubscription")
    public void testJMXSubscription(String resourceName) throws Exception {
        assertTrue(jmxSubscription.init(RESOURCE_PATH_NAME + resourceName, "ResourceUpdated",
                automationContext));
    }

    @AfterClass(alwaysRun = true)
    public void clean() throws IOException, ResourceAdminServiceExceptionException,
            ListenerNotFoundException, InstanceNotFoundException {
        resourceAdminServiceClient.deleteResource(RESOURCE_PATH_NAME + "testresource.txt");
        resourceAdminServiceClient.deleteResource(RESOURCE_PATH_NAME + "pom.xml");
        resourceAdminServiceClient.deleteResource(RESOURCE_PATH_NAME + "resource.txt");
        jmxSubscription.disconnect();
    }
}
