package org.wso2.carbon.registry.integration.ui;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.selenium.login.LoginPage;
import org.wso2.carbon.automation.api.selenium.metadata.ServicePage;
import org.wso2.carbon.automation.api.selenium.servlistlist.ServiceListPage;
import org.wso2.carbon.automation.core.BrowserManager;
import org.wso2.carbon.automation.core.ProductConstant;

public class ServiceTestCase extends GregUiIntegrationTest{

    private WebDriver driver;



    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = BrowserManager.getWebDriver();
        driver.get(getLoginURL(ProductConstant.GREG_SERVER_NAME));
    }

    @Test(groups = "wso2.greg", description = "verify adding a service is successful")
    public void testLogin() throws Exception {
        LoginPage test = new LoginPage(driver);
        //AddService  reshome =("admin","admin");
         test.loginAs(userInfo.getUserName(), userInfo.getPassword());
        ServicePage addService = new ServicePage(driver);
        //Add the service name and the namespace here
        String serviceName = "testServiceName";
        String serviceNameSpace = "testNameSpace";
        addService.uploadService(serviceName, serviceNameSpace);
        ServiceListPage serviceListPage = new ServiceListPage(driver);
        serviceListPage.checkOnUploadService(serviceName);
        driver.close();

    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }

}
