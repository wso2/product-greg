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
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.subscription.JMXSubscription;
import org.wso2.greg.integration.common.utils.subscription.ManagementConsoleSubscription;

import javax.activation.DataHandler;
import java.io.File;
import java.net.URL;

import static org.testng.Assert.assertTrue;

public class LeafLevelResourceSubscriptionTestCase extends GREGIntegrationBaseTest {

    private static final String RESOURCE_PATH_NAME = "/_system/";
    private JMXSubscription jmxSubscription = new JMXSubscription();
    private String userNameWithoutDomain;

    @DataProvider(name = "ResourceDataProvider")
    public Object[][] dp() {
        return new Object[][]{new Object[]{"testresource.txt"}, new Object[]{"pom.xml"}, new Object[]{"Person.xsd"}};
    }

    @BeforeClass(alwaysRun = true)
    public void initialize() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String userName = automationContext.getContextTenant().getContextUser().getUserName();
        if(userName.contains("@")) {
            userNameWithoutDomain = userName.substring(0, userName.indexOf('@'));
        } else {
            userNameWithoutDomain = userName;
        }
    }

    @Test(groups = "wso2.greg", description = "Add resource", dataProvider = "ResourceDataProvider")
    public void testAddResource(String resourceName) throws Exception {
        ResourceAdminServiceClient resourceAdminServiceClient = new ResourceAdminServiceClient(backendURL, getSessionCookie());
        String resourcePath = getTestArtifactLocation() + "artifacts" +
                File.separator + "GREG" + File.separator + resourceName;
        DataHandler dh = new DataHandler(new URL("file:///" + resourcePath));
        resourceAdminServiceClient.addResource(RESOURCE_PATH_NAME + resourceName, "test/plain", "testDesc", dh);
        assertTrue(resourceAdminServiceClient.getResource(RESOURCE_PATH_NAME +
                resourceName)[0].getAuthorUserName().contains(userNameWithoutDomain));
    }

    @Test(groups = "wso2.greg", description = "Get Management Console Notification",
            dependsOnMethods = "testAddResource", dataProvider = "ResourceDataProvider")
    public void testConsoleSubscription(String resourceName) throws Exception {
        assertTrue(ManagementConsoleSubscription.init(RESOURCE_PATH_NAME + resourceName,
                "ResourceUpdated", automationContext));
    }

    @Test(groups = "wso2.greg", description = "Get JMX Notification",
            dependsOnMethods = "testConsoleSubscription", dataProvider = "ResourceDataProvider")
    public void testJMXSubscription(String resourceName) throws Exception {
        assertTrue(jmxSubscription.init(RESOURCE_PATH_NAME + resourceName,
                "ResourceUpdated", automationContext));
    }

    @AfterClass(alwaysRun = true)
    public void clean() throws Exception {
        ResourceAdminServiceClient resourceAdminServiceClient =
                new ResourceAdminServiceClient(backendURL, getSessionCookie());
        resourceAdminServiceClient.deleteResource(RESOURCE_PATH_NAME + "testresource.txt");
        resourceAdminServiceClient.deleteResource(RESOURCE_PATH_NAME + "pom.xml");
        resourceAdminServiceClient.deleteResource(RESOURCE_PATH_NAME + "Person.xsd");
        jmxSubscription.disconnect();
    }
}
