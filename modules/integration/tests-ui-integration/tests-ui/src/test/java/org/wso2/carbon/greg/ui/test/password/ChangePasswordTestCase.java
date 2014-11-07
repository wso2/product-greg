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
package org.wso2.carbon.greg.ui.test.password;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.greg.integration.common.ui.page.LoginPage;
import org.wso2.greg.integration.common.ui.page.configuretab.ChangePasswordPage;
import org.wso2.greg.integration.common.utils.GREGIntegrationUIBaseTest;

public class ChangePasswordTestCase extends  GREGIntegrationUIBaseTest{

    private WebDriver driver;
    //private User userInfo;



    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        //userInfo = automationContext.getContextTenant().getContextUser();
        driver = BrowserManager.getWebDriver();
        driver.get(getLoginURL());
    }

    @Test(groups = "wso2.greg", description = "verify change password is successful", enabled=false)
    public void testChangePassword() throws Exception {

        LoginPage test = new LoginPage(driver);
        //AddService  reshome =("admin","admin");
        test.loginAs("admin", "admin");
        ChangePasswordPage changePasswordPage = new ChangePasswordPage(driver);
        //Already Created Password
        String userName = "admin";
        String passWord = "admin";
        String newPassWord = "admin123";
        changePasswordPage.changePassword(passWord, newPassWord);
        changePasswordPage.changePasswordCheck(userName, newPassWord);
        driver.findElement(By.xpath("//button[contains(.,'OK')]")).click();
        driver.findElement(By.xpath("//a[contains(.,'                                    Change My Password')]")).click();
        changePasswordPage.changePassword(newPassWord, passWord);
        driver.close();

    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }



}
