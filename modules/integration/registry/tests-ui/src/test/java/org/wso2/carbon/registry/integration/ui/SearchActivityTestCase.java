package org.wso2.carbon.registry.integration.ui;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.selenium.login.LoginPage;
import org.wso2.carbon.automation.api.selenium.searchactivity.SearchActivityPage;
import org.wso2.carbon.automation.core.BrowserManager;
import org.wso2.carbon.automation.core.ProductConstant;

public class SearchActivityTestCase extends GregUiIntegrationTest{

    private WebDriver driver;



    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = BrowserManager.getWebDriver();
        driver.get(getLoginURL(ProductConstant.GREG_SERVER_NAME));
    }

    @Test(groups = "wso2.greg", description = "verify search activity is successful")
    public void testLogin() throws Exception {
        LoginPage test = new LoginPage(driver);
        test.loginAs(userInfo.getUserName(), userInfo.getPassword());
        SearchActivityPage searchActivityPage = new SearchActivityPage(driver);
        //searching an element
        searchActivityPage.searchElement();
        //Search element verify
        searchActivityPage.verifySearchElement("/Capp_1.0.0.carTestFile");
        driver.close();

    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }


}
