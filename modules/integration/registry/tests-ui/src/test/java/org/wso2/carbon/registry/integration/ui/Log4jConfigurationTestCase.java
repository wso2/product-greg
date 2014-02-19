package org.wso2.carbon.registry.integration.ui;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.selenium.logging.LoggingHomePage;
import org.wso2.carbon.automation.api.selenium.login.LoginPage;
import org.wso2.carbon.automation.core.BrowserManager;
import org.wso2.carbon.automation.core.ProductConstant;

public class Log4jConfigurationTestCase extends GregUiIntegrationTest{

    private WebDriver driver;



    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = BrowserManager.getWebDriver();
        driver.get(getLoginURL(ProductConstant.GREG_SERVER_NAME));
    }

    @Test(groups = "wso2.greg", description = "verify log4j configuration")
    public void testLogin() throws Exception {

        LoginPage test = new LoginPage(driver);
        //username and the password is hard coded
        //logging feature is disabled when log as testuser2
        test.loginAs("admin", "admin123");
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
