package org.wso2.carbon.registry.integration.ui;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.selenium.apimanager.apilist.ApiListPage;
import org.wso2.carbon.automation.api.selenium.login.LoginPage;
import org.wso2.carbon.automation.api.selenium.metadata.ApiPage;
import org.wso2.carbon.automation.core.BrowserManager;
import org.wso2.carbon.automation.core.ProductConstant;

public class ApiTestCase extends GregUiIntegrationTest{

    private WebDriver driver;
    private String baseUrl;
    private StringBuffer verificationErrors = new StringBuffer();


    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = BrowserManager.getWebDriver();
        driver.get(getLoginURL(ProductConstant.GREG_SERVER_NAME));
    }

    @Test(groups = "wso2.greg", description = "verify adding new api is successful")
    public void testLogin() throws Exception {

        LoginPage test = new LoginPage(driver);
        test.loginAs(userInfo.getUserName(), userInfo.getPassword());
        ApiPage addApi = new ApiPage(driver);

        //Add  Api Details Here
        String provider = "Test Provider";
        String name = "Test Api";
        String context = "Test Context";
        String versionApi = "1.2.3";
        String lifeCycle="ServiceLifeCycle";
        addApi.uploadApi(provider, name, context, versionApi);
        Thread.sleep(8000);
        ApiListPage apiListPage = new ApiListPage(driver);
        apiListPage.checkOnUploadApi(provider);
        apiListPage.promoteApiLifecycle(provider,lifeCycle);
        driver.close();

    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }

    private boolean isElementPresent(By by) {
        try {
            driver.findElement(by);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

}
