/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.greg.store.test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.carbon.greg.store.StoreHomePage;
import org.wso2.carbon.greg.store.StoreLoginPage;
import org.wso2.carbon.greg.store.exceptions.StoreTestException;
import org.wso2.greg.integration.common.ui.page.util.UIElementMapper;
import org.wso2.greg.integration.common.utils.GREGIntegrationUIBaseTest;

import javax.xml.xpath.XPathExpressionException;
import java.util.concurrent.TimeUnit;

import static org.testng.AssertJUnit.assertEquals;

public class LoginStoreTestCase extends GREGIntegrationUIBaseTest {

    private WebDriver driver;
    private String baseUrl;
    private UIElementMapper uiElementMapper;
    private StoreHomePage storeHomePage;

    @BeforeClass(alwaysRun = true, description = "Basic setup and moving to store")
    public void setUp() throws Exception {
        super.init();
        driver = BrowserManager.getWebDriver();
        driver.manage().timeouts().implicitlyWait(35, TimeUnit.SECONDS);
        baseUrl = getStoreBaseUrl();
        uiElementMapper = UIElementMapper.getInstance();
    }

    @Test(description = "This method tests login functionality of the Store home page")
    public void testLoginStore() throws StoreTestException, XPathExpressionException {
        driver.get(baseUrl);
        storeHomePage = new StoreHomePage(driver);
        StoreLoginPage storeLoginPage = storeHomePage.moveToLoginPage();
        storeLoginPage.Login(automationContext.getContextTenant().getContextUser().getUserName(),
                automationContext.getContextTenant().getContextUser().getPassword());

        //assert signed in
        WebElement signedInUser = driver
                .findElement(By.id(uiElementMapper.getElement("store.homepage.loggedinuser.id")));
        assertEquals(signedInUser.getText().trim(), automationContext.getContextTenant().getContextUser().getUserName(),
                "Signed is user is different from context username");
        log.info("Successfully logged in");
    }

    @Test(description = "This tests the logout functionality of Store home page", dependsOnMethods = "testLoginStore")
    public void testLogoutStore() throws XPathExpressionException {
        storeHomePage.logOut();

        //assert logout
        WebElement signInButton = driver
                .findElement(By.id(uiElementMapper.getElement("store.homepage.loginbutton.id")));
        assertEquals(signInButton.getText(), "Sign in", "Not properly signed out from the store");
        log.info("Successfully logged out");
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        driver.quit();
    }

}
