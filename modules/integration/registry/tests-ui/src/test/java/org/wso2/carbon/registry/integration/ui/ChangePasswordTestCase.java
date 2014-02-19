package org.wso2.carbon.registry.integration.ui;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.selenium.configuretab.ChangePasswordPage;
import org.wso2.carbon.automation.api.selenium.login.LoginPage;
import org.wso2.carbon.automation.core.BrowserManager;
import org.wso2.carbon.automation.core.ProductConstant;

public class ChangePasswordTestCase extends  GregUiIntegrationTest{

    private WebDriver driver;



    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = BrowserManager.getWebDriver();
        driver.get(getLoginURL(ProductConstant.GREG_SERVER_NAME));
    }

    @Test(groups = "wso2.greg", description = "verify adding a new tenant is successful")
    public void testLogin() throws Exception {

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
        driver.close();

    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }



}
