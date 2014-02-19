package org.wso2.carbon.registry.integration.ui;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.selenium.login.LoginPage;
import org.wso2.carbon.automation.api.selenium.metadata.WsPolicyPage;
import org.wso2.carbon.automation.api.selenium.wsPolicyList.wsPolicyListPage;
import org.wso2.carbon.automation.core.BrowserManager;
import org.wso2.carbon.automation.core.ProductConstant;

import java.io.File;

public class WsPolicyTestCase extends GregUiIntegrationTest{

    private WebDriver driver;



    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = BrowserManager.getWebDriver();
        driver.get(getLoginURL(ProductConstant.GREG_SERVER_NAME));
    }

    @Test(groups = "wso2.greg", description = "verify adding a wspolicy is successful")
    public void testLogin() throws Exception {

        LoginPage test = new LoginPage(driver);
        test.loginAs(userInfo.getUserName(), userInfo.getPassword());
        WsPolicyPage addWsPolicy = new WsPolicyPage(driver);

        //Add the service name and the namespace here
        String wsPolicyUrl = "http://svn.wso2.org/repos/wso2/carbon/platform/branches/4.0.0/platform-integration/clarity-tests" +
                             "/1.0.5/org.wso2.carbon.automation.test.repo/src/main/resources/artifacts/GREG/policy/policy.xml";
        String wsPolicyUrlName = "My Policy";
        //adding .xml extension to the ws policy name
        String schemaUrlNameWithExtension = wsPolicyUrlName + ".xml";
        addWsPolicy.uploadWsPolicyFromUrl(wsPolicyUrl, wsPolicyUrlName);
        wsPolicyListPage wsPolicyListPage = new wsPolicyListPage(driver);
        wsPolicyListPage.checkOnUploadedPolicy(schemaUrlNameWithExtension);
        //uploading a wsPolicy from a file
        String wsPolicyFilePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + File.separator + "artifacts" + File.separator +
                                  "GREG" + File.separator + "policy" + File.separator + "UTPolicy.xml";

        String wsPolicyFileName = "TestWsPolciy";
        String wsPolicyNameWithExtension = wsPolicyFileName + ".xml";
        System.out.println(wsPolicyFilePath);
        System.out.println(wsPolicyNameWithExtension);
        addWsPolicy.uploadWsPolicyFromFile(wsPolicyFilePath, wsPolicyNameWithExtension);
        wsPolicyListPage.checkOnUploadedPolicy(wsPolicyNameWithExtension);
        driver.close();

    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }

}
