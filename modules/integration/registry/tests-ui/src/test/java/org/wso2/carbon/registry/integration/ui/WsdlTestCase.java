package org.wso2.carbon.registry.integration.ui;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.selenium.login.LoginPage;
import org.wso2.carbon.automation.api.selenium.metadata.WSDLPage;
import org.wso2.carbon.automation.api.selenium.wsdllist.WsdlListPage;
import org.wso2.carbon.automation.core.BrowserManager;
import org.wso2.carbon.automation.core.ProductConstant;

import java.io.File;

public class WsdlTestCase extends GregUiIntegrationTest{

    private WebDriver driver;



    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = BrowserManager.getWebDriver();
        driver.get(getLoginURL(ProductConstant.GREG_SERVER_NAME));
    }

    @Test(groups = "wso2.greg", description = "verify adding a wsdl is successful")
    public void testLogin() throws Exception {

        LoginPage test = new LoginPage(driver);
        test.loginAs(userInfo.getUserName(), userInfo.getPassword());
        WSDLPage addWSDL = new WSDLPage(driver);

                //Add the service name and the namespace here
        String wsdlUrl = "http://www.webservicex.net/geoipservice.asmx?WSDL";
        String wsdlUrlName = "My Test";
        //adding .wsdl extension to the wsdl name
        String wsdlUrlNameWithExtension = wsdlUrlName + ".wsdl";
        addWSDL.uploadWsdlFromUrl(wsdlUrl, wsdlUrlName);
        WsdlListPage wsdlListPage = new WsdlListPage(driver);
        wsdlListPage.checkOnUploadWsdl(wsdlUrlNameWithExtension);
        //uploading a wsdl from a file
        String WsdlFilePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + File.separator + "artifacts" + File.separator +
                              "GREG" + File.separator + "wsdl" + File.separator + "AmazonWebServices.wsdl";
        String WsdlFileName = "Amazon";
        String WsdlFileNameWithExtension = WsdlFileName + ".wsdl";
        System.out.println(WsdlFilePath);
        System.out.println(WsdlFileNameWithExtension);
        addWSDL.uploadWsdlFromFile(WsdlFilePath, WsdlFileNameWithExtension);
        wsdlListPage.checkOnUploadWsdl(WsdlFileNameWithExtension);
        driver.close();

    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }



}
