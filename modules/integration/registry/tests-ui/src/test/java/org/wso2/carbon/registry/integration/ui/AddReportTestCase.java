package org.wso2.carbon.registry.integration.ui;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.selenium.login.LoginPage;
import org.wso2.carbon.automation.api.selenium.report.ManageReportPage;
import org.wso2.carbon.automation.api.selenium.report.ReportPage;
import org.wso2.carbon.automation.core.BrowserManager;
import org.wso2.carbon.automation.core.ProductConstant;

public class AddReportTestCase extends GregUiIntegrationTest{

    private WebDriver driver;
    private String baseUrl;
    private StringBuffer verificationErrors = new StringBuffer();

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = BrowserManager.getWebDriver();
        driver.get(getLoginURL(ProductConstant.GREG_SERVER_NAME));
    }

    @Test(groups = "wso2.greg", description = "verify adding a new report successful")
    public void testLogin() throws Exception {
        //Login to the ResourceHome Page
        LoginPage test = new LoginPage(driver);
        test.loginAs(userInfo.getUserName(), userInfo.getPassword());
        ReportPage addReportPage = new ReportPage(driver);
        ManageReportPage mangeReportPage = new ManageReportPage(driver);


        //adding the file from the resources  file from the
        String reportName = "AutomatedReport";
        String reportPath = "/sampleText.txt";
        String reporttype = "Excel";
        String reportClass = "testClass";
        
        addReportPage.addNewReport(reportName, reportPath, reporttype, reportClass);

        //Thread.sleep(5000);
        System.out.println("working after the sleep");
        mangeReportPage.checkOnUploadedReport(reportName);
        //Closing the web driver
        driver.close();

        }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }

    private boolean isElementPresent(By by) {
        try {
            driver.findElement(by);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }
}
