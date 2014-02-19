package org.wso2.carbon.registry.integration.ui;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.selenium.login.LoginPage;
import org.wso2.carbon.automation.api.selenium.metadata.UriPage;
import org.wso2.carbon.automation.api.selenium.uriList.UriListPage;
import org.wso2.carbon.automation.core.BrowserManager;
import org.wso2.carbon.automation.core.ProductConstant;

public class UriTestCase extends GregUiIntegrationTest{

    private WebDriver driver;



    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = BrowserManager.getWebDriver();
        driver.get(getLoginURL(ProductConstant.GREG_SERVER_NAME));
    }

    @Test(groups = "wso2.greg", description = "verify adding a URI is successful")
    public void testLogin() throws Exception {
        LoginPage test = new LoginPage(driver);
        //AddService  reshome =("admin","admin");
        test.loginAs(userInfo.getUserName(), userInfo.getPassword());
        UriPage addUri = new UriPage(driver);

        //Add the Uri as Generic
        String uriLink = "http://svn.wso2.org/repos/wso2/carbon/platform/branches/4.0.0/platform-integration" +
                         "/clarity-tests/1.0.5/org.wso2.carbon.automation.test.repo/src/main/resources/artifacts/GREG/wsdl" +
                         "/sample.wsdl";
        String uriName = "Sample";

        //-------------------------adding uri as Gerneric--------------

        addUri.uploadGenericUri(uriLink, uriName);
        Thread.sleep(8000);
        UriListPage uriListPage = new UriListPage(driver);
        uriListPage.checkOnUploadUri(uriName);

        //------------------------------------------------------------

        //add the Uri as a Wsdl

        String uriWsdlLink = "http://svn.wso2.org/repos/wso2/carbon/platform/branches/4.0.0/platform-integration" +
                             "/clarity-tests/1.0.5/org.wso2.carbon.automation.test.repo/src/main/resources/artifacts/GREG/wsdl" +
                             "/echo.wsdl";
        String uriWsdlName = "Echo";

        //-------------------------adding uri as Wsdl------------------------
        addUri.uploadWsdlUri(uriWsdlLink, uriWsdlName);

        //-------------------------------------------------------------------

        //add Uri as xsd

        String uriXsdPath = "http://svn.wso2.org/repos/wso2/carbon/platform/branches/4.0.0/platform-integration" +
                            "/clarity-tests/1.0.5/org.wso2.carbon.automation.test.repo/src/main/resources/artifacts/GREG/policy" +
                            "/UTPolicy.xml";
        String uriXsdName = "CalculatorXsd";

        //---------------adding uri as  xsd------------------------------
        addUri.uploadXsdUri(uriXsdPath, uriXsdName);
        //----------------------------------------------------------------

        //add Uri as a Policy

        String uriPolicyPath = "http://svn.wso2.org/repos/wso2/carbon/platform/branches/4.0.0/platform-integration" +
                               "/clarity-tests/1.0.5/org.wso2.carbon.automation.test.repo/src/main/resources/artifacts/GREG/policy" +
                               "/UTPolicy.xml";
        String uriPolicyPathName = "policyURI";

        //-----------------adding uri as policy-------------------------
        addUri.uploadPolicyUri(uriPolicyPath, uriPolicyPathName);
        //-------------------------------------------------------------

        driver.close();

    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }



}
