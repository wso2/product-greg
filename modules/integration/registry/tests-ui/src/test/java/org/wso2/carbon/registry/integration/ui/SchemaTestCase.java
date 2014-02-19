package org.wso2.carbon.registry.integration.ui;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.selenium.login.LoginPage;
import org.wso2.carbon.automation.api.selenium.metadata.SchemaPage;
import org.wso2.carbon.automation.api.selenium.schemalist.SchemaListPage;
import org.wso2.carbon.automation.core.BrowserManager;
import org.wso2.carbon.automation.core.ProductConstant;

import java.io.File;

public class SchemaTestCase extends GregUiIntegrationTest{

    private WebDriver driver;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = BrowserManager.getWebDriver();
        driver.get(getLoginURL(ProductConstant.GREG_SERVER_NAME));
    }

    @Test(groups = "wso2.greg", description = "verify adding a schema is successful")
    public void testLogin() throws Exception {

        LoginPage test = new LoginPage(driver);
        test.loginAs(userInfo.getUserName(), userInfo.getPassword());
        SchemaPage schemaPage = new SchemaPage(driver);


        //Add the service name and the namespace here
        String schemaUrl = "http://svn.wso2.org/repos/wso2/carbon/platform/branches/4.0.0/platform-integration" +
                           "/clarity-tests/1.0.5/org.wso2.carbon.automation.test.repo/src/main/resources/artifacts/GREG" +
                           "/schema/library.xsd";
        String schemaUrlName = "My Test Schema";
        //adding .wsdl extension to the wsdl name
        String schemaUrlNameWithExtension = schemaUrlName + ".xsd";
        schemaPage.uploadSchemaFromUrl(schemaUrl, schemaUrlName);
        Thread.sleep(6000);
        SchemaListPage schemaListPage = new SchemaListPage(driver);
        driver.navigate().refresh();
        schemaListPage.checkOnUploadSchema(schemaUrlNameWithExtension);
        //uploading a wsdl from a file
        String SchemaFilePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + File.separator + "artifacts" + File.separator +
                                "GREG" + File.separator + "schema" + File.separator + "Person.xsd";
        String SchemaFileName = "Personone";
        String SchemaNameWithExtension = SchemaFileName + ".xsd";
        System.out.println(SchemaFilePath);
        System.out.println(SchemaNameWithExtension);
        schemaPage.uploadSchemaFromFile(SchemaFilePath, SchemaNameWithExtension);
        Thread.sleep(6000);
        driver.navigate().refresh();
        schemaListPage.checkOnUploadSchema(SchemaNameWithExtension);
        driver.close();

    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }



}
