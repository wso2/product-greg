package org.wso2.carbon.registry.integration.ui;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.selenium.configuretab.UsersHomePage;
import org.wso2.carbon.automation.api.selenium.login.LoginPage;
import org.wso2.carbon.automation.core.BrowserManager;
import org.wso2.carbon.automation.core.ProductConstant;

public class UserCreationTestCase extends GregUiIntegrationTest{

    private WebDriver driver;



    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = BrowserManager.getWebDriver();
        driver.get(getLoginURL(ProductConstant.GREG_SERVER_NAME));
    }

    @Test(groups = "wso2.greg", description = "verify creating a user is successful")
    public void testLogin() throws Exception {

        LoginPage test = new LoginPage(driver);
        //AddService  reshome =("admin","admin");
        test.loginAs(userInfo.getUserName(), userInfo.getPassword());
        UsersHomePage usersAndRolesHomePage = new UsersHomePage(driver);
                //Add the service name and the namespace here
        String userName = "Seleniumtest";
        String passWord = "Selenium123";
        usersAndRolesHomePage.addUser(userName, passWord);
        usersAndRolesHomePage.checkOnUploadUser(userName);
        driver.close();

    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }



}
