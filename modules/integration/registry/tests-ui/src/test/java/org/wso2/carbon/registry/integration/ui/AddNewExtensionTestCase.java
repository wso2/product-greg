package org.wso2.carbon.registry.integration.ui;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.selenium.extensionlist.ExtensionListPage;
import org.wso2.carbon.automation.api.selenium.extensions.ExtensionPage;
import org.wso2.carbon.automation.api.selenium.login.LoginPage;
import org.wso2.carbon.automation.core.BrowserManager;
import org.wso2.carbon.automation.core.ProductConstant;

import java.io.File;

public class AddNewExtensionTestCase extends GregUiIntegrationTest{

    private WebDriver driver;


    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {

        super.init();
        driver = BrowserManager.getWebDriver();
        driver.get(getLoginURL(ProductConstant.GREG_SERVER_NAME));
    }

    @Test(groups = "wso2.greg", description = "verify adding new extension is successful")
    public void testLogin() throws Exception {
        LoginPage test = new LoginPage(driver);
        test.loginAs(userInfo.getUserName(), userInfo.getPassword());
        ExtensionPage addNewExtension = new ExtensionPage(driver);
        String extensionFilePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + File.separator + "artifacts" + File.separator +
                                   "GREG" + File.separator + "extensions" + File.separator + "HelloWorld.jar";
        String extensionName = "HelloWorld.jar";
        addNewExtension.addNewExtension(extensionFilePath);
        ExtensionListPage extensionListpage = new ExtensionListPage(driver);
        extensionListpage.checkOnUploadedExtension(extensionName);
        driver.close();

    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }



}
