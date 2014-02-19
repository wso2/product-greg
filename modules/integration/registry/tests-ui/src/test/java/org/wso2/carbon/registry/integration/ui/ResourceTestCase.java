package org.wso2.carbon.registry.integration.ui;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.selenium.login.LoginPage;
import org.wso2.carbon.automation.api.selenium.resource.ResourceHome;
import org.wso2.carbon.automation.core.BrowserManager;
import org.wso2.carbon.automation.core.ProductConstant;

import java.io.File;

public class ResourceTestCase extends GregUiIntegrationTest{

    private WebDriver driver;


    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = BrowserManager.getWebDriver();
        driver.get(getLoginURL(ProductConstant.GREG_SERVER_NAME));
    }

    @Test(groups = "wso2.greg", description = "verify adding Resources are successful")
    public void testLogin() throws Exception {
        //Login to the ResourceHome Page
        LoginPage test = new LoginPage(driver);
        test.loginAs(userInfo.getUserName(), userInfo.getPassword());
        ResourceHome resourceHome = new ResourceHome(driver);
       //adding the file from the resources  file from the
        String carPath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + File.separator + "artifacts" + File.separator +
                         "GREG" + File.separator + "car" + File.separator + "Capp_1.0.0.car";
        resourceHome.uploadResourceFromFile(carPath);
       //checking whether the uploaded resource exists in the table
        resourceHome.checkOnUploadSuccess("Capp_1.0.0.carTestFile");
       //enter URL to add resource form a URL
        String UrlPath = "http://svn.wso2.org/repos/wso2/carbon/platform/branches/4.0.0/platform-integration/clarity-tests/" +
                         "1.0.8/org.wso2.carbon.automation.test.repo/src/main/resources/artifacts/GREG/txt/sampleText.txt";
        resourceHome.uploadResourceFromUrl(UrlPath);
        System.out.println("Im here");
       //checking whether the uploaded resource exists in the table
        resourceHome.checkOnUploadSuccess("sampleText.txt");
       //uploading a collection Item to the Greg
        String folderName = "My Document";
        resourceHome.uploadCollection(folderName);
       //Closing the web driver
        driver.close();

    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }

}
