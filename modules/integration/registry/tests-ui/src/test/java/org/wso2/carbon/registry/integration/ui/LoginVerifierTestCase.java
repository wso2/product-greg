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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.selenium.home.HomePage;
import org.wso2.carbon.automation.api.selenium.login.LoginPage;
import org.wso2.carbon.automation.core.BrowserManager;
import org.wso2.carbon.automation.core.ProductConstant;

import java.io.IOException;

public class LoginVerifierTestCase extends GregUiIntegrationTest {

    private LoginPage loginPage;
    private WebDriver webDriver;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init();
        webDriver = BrowserManager.getWebDriver();
        webDriver.get(getLoginURL(ProductConstant.GREG_SERVER_NAME));
        loginPage = new LoginPage(webDriver);
    }

    @Test(groups = "wso2.greg", description = "login to greg")
    public void loginToGreg() throws IOException {
        HomePage homePage = loginPage.loginAs(userInfo.getUserName(), userInfo.getPassword());
        homePage.logout();
    }

    @AfterClass(alwaysRun = true)
    public void cleanEnvironment() {
        webDriver.quit();
    }

}
