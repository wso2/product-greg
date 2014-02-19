package org.wso2.carbon.registry.integration.ui;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.selenium.keystore.KeyStoreHome;
import org.wso2.carbon.automation.api.selenium.keystore.KeyStoreManagementPage;
import org.wso2.carbon.automation.api.selenium.login.LoginPage;
import org.wso2.carbon.automation.core.BrowserManager;
import org.wso2.carbon.automation.core.ProductConstant;

import java.io.File;

public class AddKeyStoreTestCase extends GregUiIntegrationTest {

    private WebDriver driver;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = BrowserManager.getWebDriver();
        driver.get(getLoginURL(ProductConstant.GREG_SERVER_NAME));
    }

    @Test(groups = "wso2.greg", description = "verify login as user")
    public void testLogin() throws Exception {
        LoginPage test = new LoginPage(driver);
        test.loginAs(userInfo.getUserName(), userInfo.getPassword());
        KeyStoreHome keyStoreHome = new KeyStoreHome(driver);
        //adding the key store
        String keyStoreFilePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + File.separator + "artifacts" + File.separator +
                                  "GREG" + File.separator + "keystore" + File.separator + "examplestore";
        String keyStorePassWord = "randika123";
        String keyStoreProvider = "testprovider";
        String keyStoreName = "wso2carbon.jks";

        keyStoreHome.addKeyStore(keyStoreFilePath, keyStorePassWord, keyStoreProvider);
        KeyStoreManagementPage keyStoreManagementPage = new KeyStoreManagementPage(driver);
        keyStoreManagementPage.checkOnUploadedKeyStore(keyStoreName);

        driver.close();

    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }



}
