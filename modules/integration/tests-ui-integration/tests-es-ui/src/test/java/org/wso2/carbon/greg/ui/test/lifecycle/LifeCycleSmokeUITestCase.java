/*
*Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.carbon.greg.ui.test.lifecycle;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.context.beans.User;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;

import org.wso2.greg.integration.common.ui.page.LoginPage;
import org.wso2.greg.integration.common.ui.page.lifecycle.LifeCycleHomePage;
import org.wso2.greg.integration.common.ui.page.lifecycle.LifeCyclesPage;
import org.wso2.greg.integration.common.ui.page.publisher.PublisherHomePage;
import org.wso2.greg.integration.common.ui.page.publisher.PublisherLoginPage;
import org.wso2.greg.integration.common.ui.page.store.StoreLoginPage;
import org.wso2.greg.integration.common.ui.page.util.UIElementMapper;
import org.wso2.greg.integration.common.utils.GREGIntegrationUIBaseTest;

import static org.testng.Assert.assertTrue;

/**
 * This UI test class covers a full testing scenario of,
 *
 *  1. uploading a LC to greg,
 *  2. implementation of rest service,
 *  3. adding the LC to rest service,
 *  4. attaching of LC for rest service
 *  5. Promoting of rest service
 *
 */
public class LifeCycleSmokeUITestCase extends GREGIntegrationUIBaseTest {

    private WebDriver driver;
    private User userInfo;

    private String restServiceName = "DimuthuD";
    private UIElementMapper uiElementMapper;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        userInfo = automationContext.getContextTenant().getContextUser();
        driver = BrowserManager.getWebDriver();
        this.uiElementMapper = UIElementMapper.getInstance();
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }

    @Test(groups = "wso2.greg", description = "verify login to governance registry")
    public void performingLoginToManagementConsole() throws Exception {

        driver.get(getLoginURL());
        LoginPage test = new LoginPage(driver);
        test.loginAs(automationContext.getContextTenant().getContextUser().getUserName(),
                automationContext.getContextTenant().getContextUser().getPassword());

        driver.get(getLoginURL() + "admin/index.jsp?loginStatus=true");
        LifeCycleHomePage lifeCycleHomePage = new LifeCycleHomePage(driver);


        String lifeCycle = "<aspect name=\"SampleLifeCycle\" class=\"org.wso2.carbon.governance.registry.extensions.aspects.DefaultLifeCycle\">\n" +
                "    <configuration type=\"literal\">\n" +
                "        <lifecycle>\n" +
                "            <scxml xmlns=\"http://www.w3.org/2005/07/scxml\"\n" +
                "                   version=\"1.0\"\n" +
                "                   initialstate=\"Development\">\n" +
                "                <state id=\"Development\">\n" +
                "                    <datamodel>\n" +
                "                        <data name=\"checkItems\">\n" +
                "                            <item name=\"Code Completed\" forEvent=\"\">\n" +
                "                            </item>\n" +
                "                            <item name=\"WSDL, Schema Created\" forEvent=\"\">\n" +
                "                            </item>\n" +
                "                            <item name=\"QoS Created\" forEvent=\"\">\n" +
                "                            </item>\n" +
                "                        </data>\n" +
                "                    </datamodel>\n" +
                "                    <transition event=\"Promote\" target=\"Tested\"/>\n" +
                "                    <checkpoints>\n" +
                "                        <checkpoint id=\"DevelopmentOne\" durationColour=\"green\">\n" +
                "                            <boundary min=\"0d:0h:0m:0s\" max=\"1d:0h:00m:20s\"/>\n" +
                "                        </checkpoint>\n" +
                "                        <checkpoint id=\"DevelopmentTwo\" durationColour=\"red\">\n" +
                "                            <boundary min=\"1d:0h:00m:20s\" max=\"23d:2h:5m:52s\"/>\n" +
                "                        </checkpoint>\n" +
                "                    </checkpoints>\n" +
                "                </state>\n" +
                "                <state id=\"Tested\">\n" +
                "                    <datamodel>\n" +
                "                        <data name=\"checkItems\">\n" +
                "                            <item name=\"Effective Inspection Completed\" forEvent=\"\">\n" +
                "                            </item>\n" +
                "                            <item name=\"Test Cases Passed\" forEvent=\"\">\n" +
                "                            </item>\n" +
                "                            <item name=\"Smoke Test Passed\" forEvent=\"\">\n" +
                "                            </item>\n" +
                "                        </data>\n" +
                "                    </datamodel>\n" +
                "                    <transition event=\"Promote\" target=\"Production\"/>\n" +
                "                    <transition event=\"Demote\" target=\"Development\"/>\n" +
                "                </state>\n" +
                "                <state id=\"Production\">\n" +
                "                    <transition event=\"Demote\" target=\"Tested\"/>\n" +
                "                </state>\n" +
                "            </scxml>\n" +
                "        </lifecycle>\n" +
                "    </configuration>\n" +
                "</aspect>\n";


        lifeCycleHomePage.addNewLifeCycle(lifeCycle);

        driver.get(getLoginURL() + "lcm/lcm.jsp?region=region3&item=governance_lcm_menu");
        LifeCyclesPage lifeCyclesPage = new LifeCyclesPage(driver);

        assertTrue(lifeCyclesPage.checkOnUploadedLifeCycle("SampleLifeCycle"), "Sample Life Cycle Could Not Be Found");

        Thread.sleep(6000);

        driver.findElement(By.linkText("Sign-out")).click();

        log.info("Login test case is completed ");
    }

    @Test(groups = "wso2.greg", description = "logging to publisher",
            dependsOnMethods = "performingLoginToManagementConsole")
    public void performingLoginToPublisher() throws Exception {

        // Setting publisher home page
        driver.get(getPublisherUrl().split("\\/apis")[0]);

        PublisherLoginPage test = new PublisherLoginPage(driver);

        // performing login to publisher
        test.loginAs(userInfo.getUserName(), userInfo.getPassword());

        driver.get(getPublisherUrl().split("/apis")[0] + "/pages/gc-landing");

        PublisherHomePage publisherHomePage = new PublisherHomePage(driver);

        //adding rest service
        publisherHomePage.createRestService(restServiceName, "/lana", "1.2.5");

        driver.findElement(By.cssSelector("div.auth-img")).click();
        driver.findElement(By.linkText("Sign out")).click();

        Thread.sleep(3000);
    }


    @Test(groups = "wso2.greg", description = "Adding of LC to the rest service",
            dependsOnMethods = "performingLoginToPublisher")
    public void addingLCToRestService() throws Exception {

        driver.get(getLoginURL());

        LoginPage loginPage = new LoginPage(driver);
        loginPage.loginAs(automationContext.getContextTenant().getContextUser().getUserName(),
                automationContext.getContextTenant().getContextUser().getPassword());

        loginPage.assignLCToRestService(restServiceName);

        driver.findElement(By.linkText("Sign-out")).click();

        Thread.sleep(3000);
    }

    @Test(groups = "wso2.greg", description = "Promoting of rest service with the LC",
            dependsOnMethods = "addingLCToRestService")
    public void lifeCycleEventsOfRestService() throws Exception {

        // Setting publisher home page
        driver.get(getPublisherUrl().split("/apis")[0]);

        PublisherLoginPage publisherLoginPage = new PublisherLoginPage(driver);

        // performing login to publisher
        publisherLoginPage.loginAs(userInfo.getUserName(), userInfo.getPassword());

        driver.get(getPublisherUrl().split("/apis")[0] + "/pages/gc-landing");

        driver.findElement(By.cssSelector("span.btn-asset")).click();
        driver.findElement(By.linkText("REST Services")).click();
        driver.findElement(By.linkText(restServiceName)).click();

        driver.findElement(By.id("LifeCycle")).click();

        driver.findElement(By.linkText("Other lifecycles")).click();
        driver.findElement(By.linkText("SampleLifeCycle")).click();

        driver.findElement(By.xpath(uiElementMapper.getElement("publisher.promote.checkbox1.xpath"))).click();
        Thread.sleep(2000);
        driver.findElement(By.xpath(uiElementMapper.getElement("publisher.promote.checkbox2.xpath"))).click();
        Thread.sleep(2000);
        driver.findElement(By.xpath(uiElementMapper.getElement("publisher.promote.checkbox3.xpath"))).click();
        Thread.sleep(2000);
        driver.findElement(By.id("lcActionPromote")).click();
        Thread.sleep(2000);
        driver.findElement(By.xpath(uiElementMapper.getElement("publisher.promote.checkbox1.xpath"))).click();
        Thread.sleep(2000);
        driver.findElement(By.xpath(uiElementMapper.getElement("publisher.promote.checkbox2.xpath"))).click();
        Thread.sleep(2000);
        driver.findElement(By.xpath(uiElementMapper.getElement("publisher.promote.checkbox3.xpath"))).click();
        Thread.sleep(2000);
        driver.findElement(By.id("lcActionPromote")).click();

        // removal of added rest service
        driver.findElement(By.id("Edit")).click();
        driver.findElement(By.id("Delete")).click();
        driver.findElement(By.id("btn-delete-con")).click();
    }


    @Test(groups = "wso2.greg", description = "verify creating a user is successful",
            dependsOnMethods = "lifeCycleEventsOfRestService", enabled = false)
    public void storeEventsOfRestService() throws Exception {

        // Setting publisher home page
        driver.get(getStoreUrl().split("/apis")[0]);

        StoreLoginPage storeLoginPage = new StoreLoginPage(driver);

        // performing login to publisher
        driver.findElement(By.className("ro-text")).click();

        //TODO - Pls refer the Jira : https://wso2.org/jira/browse/STORE-1156
    }
}
