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
package org.wso2.carbon.registry.integration.ui;

import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.selenium.login.LoginPage;
import org.wso2.carbon.automation.api.selenium.notififcations.ManageNotificationPage;
import org.wso2.carbon.automation.api.selenium.notififcations.RegistrySubscriptionPage;
import org.wso2.carbon.automation.core.BrowserManager;
import org.wso2.carbon.automation.core.ProductConstant;

/**
 * This test class verifies that Hierarchical Subscription Method state is successfully persisted
 */
public class AddNewRegistrySubscriptionTestCase extends GregUiIntegrationTest {

    private WebDriver driver;
    private String baseUrl;
    private StringBuffer verificationErrors = new StringBuffer();

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = BrowserManager.getWebDriver();
        driver.get(getLoginURL(ProductConstant.GREG_SERVER_NAME));
    }

    @Test(groups = "wso2.greg", description = "verify Hierarchical Subscription Method state is successfully persisted")
    public void testLogin() throws Exception {

        LoginPage test = new LoginPage(driver);
        test.loginAs(userInfo.getUserName(), userInfo.getPassword());

        //Add  Subscription details here
        String event = "Update";
        String email = "test@test.com";
        String digestFrequency = "None";
        String hierarchicalSubscriptionMethod = "Collection and Children only";
        RegistrySubscriptionPage registrySubscriptionPage = new RegistrySubscriptionPage(driver);
        ManageNotificationPage manageNotificationPage =registrySubscriptionPage.addEmailSubscription(event, email,
                digestFrequency,
                hierarchicalSubscriptionMethod);
        manageNotificationPage.checkOnUploadedNotification(email);
        Assert.assertTrue(manageNotificationPage.testHierarchicalSubscriptionMethodStatePersistance
                (hierarchicalSubscriptionMethod));
        driver.close();

    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }

}
