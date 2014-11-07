/*
*Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.greg.ui.test.log;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.greg.integration.common.ui.page.LoginPage;
import org.wso2.greg.integration.common.ui.page.logging.LoggingHomePage;
import org.wso2.greg.integration.common.utils.GREGIntegrationUIBaseTest;

public class Log4jConfigurationTestCase extends GREGIntegrationUIBaseTest{

    private WebDriver driver;
    //private User userInfo;



    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        //userInfo = automationContext.getContextTenant().getContextUser();
        driver = BrowserManager.getWebDriver();
        driver.get(getLoginURL());
    }

    @Test(groups = "wso2.greg", description = "verify log4j configuration")
    public void testLog4jConfiguration() throws Exception {

        LoginPage test = new LoginPage(driver);
        //username and the password is hard coded
        //logging feature is disabled when log as testuser2
        test.loginAs("admin", "admin");
        LoggingHomePage loggingHomePage =new LoggingHomePage(driver);
        String loggerName = "org.wso2.carbon.core.services.util.CarbonAuthenticationUtil";
        String logLevel = "ERROR";
        loggingHomePage.configureLog4jLoggers(loggerName, logLevel);
        driver.close();

    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }



}
