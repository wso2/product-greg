package org.wso2.carbon.greg.ui.test.wsdl;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.context.beans.User;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.carbon.greg.ui.test.util.ProductConstant;
import org.wso2.greg.integration.common.ui.page.LoginPage;
import org.wso2.greg.integration.common.ui.page.metadata.WSDLPage;
import org.wso2.greg.integration.common.ui.page.wsdllist.WsdlListPage;
import org.wso2.greg.integration.common.utils.GREGIntegrationUIBaseTest;

import java.io.File;

public class WsdlVisualizeTestCase extends GREGIntegrationUIBaseTest {

    private WebDriver driver;
    private User userInfo;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        ProductConstant.init();
        userInfo = automationContext.getContextTenant().getContextUser();
        driver = BrowserManager.getWebDriver();
        driver.get(getLoginURL());
    }

    @Test(groups = "wso2.greg", description = "verify visualizing a wsdl is successful")
    public void testAddWSDL() throws Exception {

        LoginPage test = new LoginPage(driver);
        test.loginAs(userInfo.getUserName(), userInfo.getPassword());
        WSDLPage addWSDL = new WSDLPage(driver);

        //uploading a wsdl from a file
        String WsdlFilePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + File.separator + "artifacts" + File.separator +
                "GREG" + File.separator + "zip" + File.separator + "BizService.zip";
        String WsdlFileName = "BizService";
        String WsdlFileNameWithExtension = WsdlFileName + ".wsdl";
        String wsdlVersion = "1.0.0";
        addWSDL.uploadWsdlFromFile(WsdlFilePath, WsdlFileNameWithExtension, wsdlVersion);
        WsdlListPage wsdlListPage = new WsdlListPage(driver);
        wsdlListPage.checkOnUploadWsdl(WsdlFileNameWithExtension);
        wsdlListPage.visualize(WsdlFileNameWithExtension, "/_system/governance/trunk/wsdls/com/foo/1.0.0/BizService.wsdl");
        driver.close();
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }

}
