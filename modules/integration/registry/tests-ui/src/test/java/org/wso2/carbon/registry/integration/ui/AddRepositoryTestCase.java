package org.wso2.carbon.registry.integration.ui;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.selenium.featuremanagement.RepositoryPage;
import org.wso2.carbon.automation.api.selenium.login.LoginPage;
import org.wso2.carbon.automation.core.BrowserManager;
import org.wso2.carbon.automation.core.ProductConstant;

public class AddRepositoryTestCase extends GregUiIntegrationTest{

    private WebDriver driver;



    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = BrowserManager.getWebDriver();
        driver.get(getLoginURL(ProductConstant.GREG_SERVER_NAME));
    }

    @Test(groups = "wso2.greg", description = "verify adding new repositories are successful")
    public void testLogin() throws Exception {

        LoginPage test = new LoginPage(driver);
        test.loginAs(userInfo.getUserName(), userInfo.getPassword());
        RepositoryPage addRepositoryPage= new RepositoryPage(driver);
        String repositoryUrl = "http://dist.wso2.org/p2/carbon/releases/3.0.0";
        String repositoryName = "TestingRepo";
        addRepositoryPage.addRepository(repositoryUrl, repositoryName);
        Thread.sleep(5000);
        addRepositoryPage.checkOnUploadRepository(repositoryName);
        driver.close();

    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }



}
