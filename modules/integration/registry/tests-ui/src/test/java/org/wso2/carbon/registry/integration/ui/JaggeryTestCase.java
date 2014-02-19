package org.wso2.carbon.registry.integration.ui;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.selenium.Jaggery.JaggeryHome;
import org.wso2.carbon.automation.api.selenium.Jaggery.JaggeryListPage;
import org.wso2.carbon.automation.api.selenium.login.LoginPage;
import org.wso2.carbon.automation.core.BrowserManager;
import org.wso2.carbon.automation.core.ProductConstant;

import java.io.File;

public class JaggeryTestCase extends GregUiIntegrationTest{
    private WebDriver driver;



    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = BrowserManager.getWebDriver();
        driver.get(getLoginURL(ProductConstant.GREG_SERVER_NAME));
    }

    @Test(groups = "wso2.greg", description = "verify adding new jaggery Item is successful")
    public void testLogin() throws Exception {
        LoginPage test = new LoginPage(driver);
        test.loginAs(userInfo.getUserName(), userInfo.getPassword());
        JaggeryHome jaghome = new JaggeryHome(driver);
        String ItemPath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + File.separator + "artifacts" + File.separator +
                          "GREG" + File.separator + "jag" + File.separator + "seleniumjag.zip";
        String jaggeryName="/seleniumjag";
        

        jaghome.UploadJaggeryItem(ItemPath);
        JaggeryListPage jaggeryListPage = new JaggeryListPage(driver);
        jaggeryListPage.checkOnUploadJaggeryItem(jaggeryName);

        driver.close();
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }


}
