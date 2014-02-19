package org.wso2.carbon.registry.integration.ui;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.selenium.login.LoginPage;
import org.wso2.carbon.automation.api.selenium.tenant.TenantHomePage;
import org.wso2.carbon.automation.api.selenium.tenant.TenantListpage;
import org.wso2.carbon.automation.core.BrowserManager;
import org.wso2.carbon.automation.core.ProductConstant;

public class CreateNewTenantTestCase extends GregUiIntegrationTest{

    private WebDriver driver;


    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = BrowserManager.getWebDriver();
        driver.get(getLoginURL(ProductConstant.GREG_SERVER_NAME));
    }

    @Test()
    public void testLogin() throws Exception {

        LoginPage test = new LoginPage(driver);
        test.loginAs(userInfo.getUserName(), userInfo.getPassword());
        TenantHomePage addNewTenantHome = new TenantHomePage(driver);

        String tenantDomain = "test2.org";
        String tenantFirstName = "testname";
        String tenantLastName = "testLastName";
        String adminUsername = "testAdmin";
        String password = "tenantpassword";
        String email = "test@wso2.com";
        addNewTenantHome.addNewTenant(tenantDomain, tenantFirstName, tenantLastName, adminUsername, password, email);
        TenantListpage tenantListpage = new TenantListpage(driver);
        tenantListpage.checkOnUplodedTenant(tenantDomain);
        driver.close();

    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }



}
