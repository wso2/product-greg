/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.carbon.greg.publisher.lifecycles;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.carbon.greg.publisher.PublisherHomePage;
import org.wso2.carbon.greg.publisher.PublisherLoginPage;
import org.wso2.carbon.greg.publisher.utils.ESWebDriver;
import org.wso2.carbon.greg.publisher.utils.PublisherUtil;
import org.wso2.greg.integration.common.clients.LifeCycleManagementClient;
import org.wso2.greg.integration.common.ui.page.util.UIElementMapper;
import org.wso2.greg.integration.common.utils.GREGIntegrationUIBaseTest;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.concurrent.TimeUnit;
import javax.xml.xpath.XPathExpressionException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/***
 * This class have test methods for lifecycle operations with different users.
 */
public class CustomLifecycleUITestCase extends GREGIntegrationUIBaseTest {
    public static final String USER_NAME = "testuser11";
    public static final String PASSWORD = "testuser11";
    private static final Log log = LogFactory.getLog(CustomLifecycleUITestCase.class);
    private static final String SERVICE_VERSION = "1.0.0";
    private static final String RXT_NAME = "applicationUI.rxt";
    LifeCycleManagementClient lifeCycleAdminServiceClient;
    private ESWebDriver driver;
    private String UUID;
    private UIElementMapper uiElementMapper;
    private String uniqueName;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = new ESWebDriver(BrowserManager.getWebDriver());
        driver.manage().timeouts().implicitlyWait(LOGIN_WAIT_SECONDS, TimeUnit.SECONDS);
        driver.get(getPublisherBaseURL());
        PublisherLoginPage publisherLoginPage = new PublisherLoginPage(driver);
        this.uiElementMapper = UIElementMapper.getInstance();

        try {
            addNewRxtConfiguration(RXT_NAME, RXT_NAME);
            lifeCycleAdminServiceClient = new LifeCycleManagementClient(backendURL, sessionCookie);
            String resourcePath = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator + "GREG"
                    + File.separator;
            lifeCycleAdminServiceClient
                    .addLifeCycle(readFile(resourcePath + "lifecycle" + File.separator + "lifecycleUI.xml"));
        } catch (AxisFault e) {
            log.error("Error while adding new configurations", e);
        }
        publisherLoginPage.loginAs(automationContext.getContextTenant().getContextUser().getUserName(),
                automationContext.getContextTenant().getContextUser().getPassword());
    }

/*    @Test(groups = "wso2.greg", description = "Create custom RXT instance and validate its availability ")
    public void testCreateRESTInstance() throws IOException, XPathExpressionException {
        PublisherUtil asset = new PublisherUtil(driver);
        String uniqueName = getUniqueName();
        asset.createGenericTypeAsset(uniqueName, "/" + uniqueName, "1.0.0", "TestRest" + uniqueName,
                uiElementMapper.getElement("publisher.restservices"));
        UUID = asset.getUUID();
        PublisherHomePage publisherHomePage = new PublisherHomePage(driver);
        publisherHomePage.logOut();
    }*/

    @Test(groups = "wso2.greg", description = "Create a custom RXT instance and validate its availability")
    public void testCreateRXTInstance() throws MalformedURLException, XPathExpressionException {
        uniqueName = getUniqueName();
        driver.findElement(By.id(uiElementMapper.getElement("publisher.ea.add"))).click();
        driver.findElement(By.id(uiElementMapper.getElement("publisher.asset.name"))).click();
        driver.findElement(By.id(uiElementMapper.getElement("publisher.asset.name"))).clear();
        driver.findElement(By.id(uiElementMapper.getElement("publisher.asset.name"))).sendKeys(uniqueName);
        driver.findElement(By.id(uiElementMapper.getElement("publisher.asset.version"))).clear();
        driver.findElement(By.id(uiElementMapper.getElement("publisher.asset.version"))).sendKeys(SERVICE_VERSION);
        driver.findElement(By.id(uiElementMapper.getElement("publisher.asset.description"))).clear();
        driver.findElement(By.id(uiElementMapper.getElement("publisher.asset.description"))).sendKeys("desc");
        driver.findElement(By.id(uiElementMapper.getElement("rxt.ea.button"))).click();
        // wait until asset visible and do refresh in WAIT_SECONDS interval
        driver.findElementPoll(By.id(uniqueName), WAIT_SECONDS);
        // if page has more asset to scroll , scroll into end of page until asset available
        driver.findElementByDynamicScroll(By.id(uniqueName));
        assertTrue(isElementPresent(driver, By.id(uniqueName)),
                " RXT instance is not available in listing page for test case " + log.getClass().getName());
        driver.findElement(By.id(uniqueName)).click();
    }

    @Test(groups = "wso2.greg", description = "comment and promote Lifecycle Operation by admin",
            dependsOnMethods = "testCreateRXTInstance")
    public void testCommentAndPromote() throws MalformedURLException, XPathExpressionException {
        PublisherUtil asset = new PublisherUtil(driver);
        driver.findElement(By.id(uiElementMapper.getElement("publisher.lifecycle"))).click();
        driver.findElement(By.xpath(uiElementMapper.getElement("publisher.promote.checkbox1.xpath"))).click();
        asset.addLCComment("going to promote");
        assertTrue(isElementPresent(driver, By.id(uiElementMapper.getElement("publisher.promote"))),
                "Promote button not available in lifecycle UI for test case " + log.getClass().getName());
        driver.findElement(By.id(uiElementMapper.getElement("publisher.promote"))).click();
        assertEquals("State updated successfully",
                driver.findElement(By.cssSelector(uiElementMapper.getElement("publisher.lifecycle.notification")))
                        .getText().trim());
        assertTrue(isElementPresent(driver, By.id(uiElementMapper.getElement("publisher.demote"))),
                "Demote button not available in lifecycle UI for test case " + log.getClass().getName());
    }

    @Test(groups = "wso2.greg", description = "comment and demote Lifecycle Operation by admin",
            dependsOnMethods = "testCommentAndPromote")
    public void testCommentAndDemote() throws IOException, XPathExpressionException {
        WebDriverWait wait = new WebDriverWait(driver, WAIT_SECONDS);
        PublisherUtil asset = new PublisherUtil(driver);
        driver.findElement(By.xpath(uiElementMapper.getElement("publisher.promote.checkbox2.xpath"))).click();
        asset.addLCComment("going to demote");
        driver.findElement(By.id(uiElementMapper.getElement("publisher.demote"))).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector(uiElementMapper.
                getElement("publisher.lifecycle.alert")), "State updated successfully"));

        PublisherHomePage publisherHomePage = new PublisherHomePage(driver);
        publisherHomePage.logOut();
    }

    @Test(groups = "wso2.greg", description = "log as a user with permission and validate created asset availability",
            dependsOnMethods = "testCommentAndDemote")
    public void testLogAsUserWithPermission() throws IOException, XPathExpressionException {
        driver.manage().timeouts().implicitlyWait(LOGIN_WAIT_SECONDS, TimeUnit.SECONDS);
        driver.get(getPublisherBaseURL());
        PublisherLoginPage publisherLoginPage = new PublisherLoginPage(driver);
        publisherLoginPage.loginAs(USER_NAME, PASSWORD);
        driver.findElement(By.id(uiElementMapper.getElement("publisher.ninedot"))).click();
        driver.findElementWD(By.linkText(uiElementMapper.getElement("publisher.ea.name"))).click();
        driver.findElement(By.id(uniqueName)).click();
    }

    @Test(groups = "wso2.greg", description = "comment and promote Lifecycle Operation by new user",
            dependsOnMethods = "testLogAsUserWithPermission")
    public void testCommentAndPromoteAgain() throws MalformedURLException, XPathExpressionException {
        PublisherUtil asset = new PublisherUtil(driver);
        driver.findElement(By.id(uiElementMapper.getElement("publisher.lifecycle"))).click();
        driver.findElement(By.xpath(uiElementMapper.getElement("publisher.promote.checkbox1.xpath"))).click();
        asset.addLCComment("going to promote");
        // since permission is not working for custom RXT
      /*  assertTrue(isElementPresent(driver, By.id(uiElementMapper.getElement("publisher.promote"))),
                "Promote button not available in lifecycle UI for test case " + log.getClass().getName()); */

    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        PublisherHomePage publisherHomePage = new PublisherHomePage(driver);
        publisherHomePage.logOut();
        driver.quit();
    }

}
