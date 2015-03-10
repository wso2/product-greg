/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.greg.ui.test.jira;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.context.beans.User;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.greg.integration.common.ui.page.LoginPage;
import org.wso2.greg.integration.common.ui.page.main.HomePage;
import org.wso2.greg.integration.common.utils.GREGIntegrationUIBaseTest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import static org.testng.Assert.assertTrue;

/**
 * This test case is the fix for the patch WSO2-CARBON-PATCH-4.2.0-1103
 * https://wso2.org/jira/browse/CARBON-14768
 *
 * Problem domain of the patch was when we login the Registry and add an endpoint SOF was found in the
 * carbon.log file.
 */
public class Registry1103EndpointAddTestCase extends GREGIntegrationUIBaseTest {

    private static final Log log = LogFactory.getLog(Registry1103EndpointAddTestCase.class);
    private WebDriver driver;
    private static final String STACK_OVERFLOW_ERROR_MESSAGE = "StackOverflowError";
    private static final String LOG_FILE = "wso2carbon.log";
    private String carbonHome;
    private User userInfo;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        userInfo = automationContext.getContextTenant().getContextUser();
        driver = BrowserManager.getWebDriver();
        driver.get(getLoginURL());
        // Get the carbon home
        carbonHome = CarbonUtils.getCarbonHome();
    }

    @Test(groups = "wso2.greg", description = "Verify adding new endpoint")
    public void test() throws IOException {
        try {
            // Login to server
            LoginPage loginPage = new LoginPage(driver);
            HomePage homePage = loginPage.loginAs(userInfo.getUserName(), userInfo.getPassword());

            // Add endpoint
            driver.findElement(By.linkText("ESB Endpoint")).click();
            driver.findElement(By.id("id_Overview_Name")).sendKeys("myendpoint");
            driver.findElement(By.id("id_Overview_Version")).sendKeys("1.0.0");
            driver.findElement(By.id("id_Overview_Address"))
                    .sendKeys("http://localhost:9000/services/SimpleStockQutoteService");
            driver.findElement(By.xpath("//input[contains(@class,'button registryWriteOperation')]")).click();
            log.info("Endpoint added successfully");

            // Read the logs
            String readCarbonLogs = readCarbonLogs();
            log.info("Read the " + LOG_FILE + " file successfully");
            assertTrue(!readCarbonLogs.contains(STACK_OVERFLOW_ERROR_MESSAGE), "Error StackOverflowError encountered");
        } finally {
            driver.close();
        }

    }

    /**
     * Method to read the carbon.log file content
     * @return log content as a string
     * @throws FileNotFoundException Log file cannot be find
     */
    private String readCarbonLogs() throws FileNotFoundException {
        File carbonLogFile = new File(carbonHome + File.separator + "repository" + File.separator +
                "logs" + File.separator + LOG_FILE);
        return new Scanner(carbonLogFile).useDelimiter("\\A").next();
    }

    @AfterClass(alwaysRun = true, groups = { "wso2.greg" })
    public void tearDown() throws RegistryException, AxisFault {
        driver.quit();
    }
}
