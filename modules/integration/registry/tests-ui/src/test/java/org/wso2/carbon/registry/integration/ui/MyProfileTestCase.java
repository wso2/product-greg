package org.wso2.carbon.registry.integration.ui;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.selenium.login.LoginPage;
import org.wso2.carbon.automation.api.selenium.myprofile.MyProfilePage;
import org.wso2.carbon.automation.core.BrowserManager;
import org.wso2.carbon.automation.core.ProductConstant;

public class MyProfileTestCase extends GregUiIntegrationTest{

    private WebDriver driver;


    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = BrowserManager.getWebDriver();
        driver.get(getLoginURL(ProductConstant.GREG_SERVER_NAME));
    }

    @Test(groups = "wso2.greg", description = "verify adding a profile is successful")
    public void testLogin() throws Exception {

        LoginPage test = new LoginPage(driver);

        test.loginAs(userInfo.getUserName(), userInfo.getPassword());
        MyProfilePage myProfilePage = new MyProfilePage(driver);
        //Add my Profile Details here 
        String profileName = "Test Profile";
        String profileFirstName = "Test Profile FirstName";
        String profileLastName = "Test Profile Last Name";
        String profileEmail = "Test@wso.com";
        myProfilePage.uploadProfile(profileName, profileFirstName, profileLastName, profileEmail);
        myProfilePage.checkOnUploadProfile(profileName);
        driver.close();

    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }



}
