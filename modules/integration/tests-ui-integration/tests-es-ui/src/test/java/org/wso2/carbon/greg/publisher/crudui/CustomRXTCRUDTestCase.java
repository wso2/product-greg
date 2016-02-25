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
package org.wso2.carbon.greg.publisher.crudui;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.carbon.greg.publisher.login.PublisherHomePage;
import org.wso2.carbon.greg.publisher.login.PublisherLoginPage;
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

import static org.testng.Assert.assertTrue;

/***
 * This class have test methods for Create,Retrieve,Remove,Update Operations for Custom RXT
 */
public class CustomRXTCRUDTestCase extends GREGIntegrationUIBaseTest {
    private static final Log log = LogFactory.getLog(CustomRXTCRUDTestCase.class);
    private static final String SERVICE_VERSION = "1.0.0";
    public static final String LIFECYCLE_NAME = "lifecycleUI.xml";
    private ESWebDriver driver;
    private UIElementMapper uiElementMapper;
    private String uniqueName;
    private PublisherUtil asset;
    private static final String RXT_NAME = "applicationUI.rxt";

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = new ESWebDriver(BrowserManager.getWebDriver());
        driver.manage().timeouts().implicitlyWait(LOGIN_WAIT_SECONDS, TimeUnit.SECONDS);
        try {
            addNewRxtConfiguration(RXT_NAME, RXT_NAME);
            LifeCycleManagementClient lifeCycleAdminServiceClient = new LifeCycleManagementClient(backendURL,
                    sessionCookie);
            String resourcePath = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator + "GREG"
                    + File.separator;
            lifeCycleAdminServiceClient
                    .addLifeCycle(readFile(resourcePath + "lifecycle" + File.separator + LIFECYCLE_NAME));
        } catch (AxisFault e) {
            log.error("Error while adding new configurations", e);
        }
        driver.get(getPublisherBaseUrl());
        PublisherLoginPage publisherLoginPage = new PublisherLoginPage(driver);
        this.uiElementMapper = UIElementMapper.getInstance();
        asset = new PublisherUtil(driver);
        publisherLoginPage.loginAs(automationContext.getContextTenant().getContextUser().getUserName(),
                automationContext.getContextTenant().getContextUser().getPassword());
    }

    @Test(groups = "wso2.greg", description = "Create a custom RXT instance and validate its availability")
    public void testCreateRXTInstance() {
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

    @Test(groups = "wso2.greg", description = "Click and validate the asset",
            dependsOnMethods = "testCreateRXTInstance")
    public void testClickAndValidate() {
        asset.clickOnGenericTypeAsset(uiElementMapper.getElement("publisher.ea.name"), uniqueName);
        asset.validateDetailsContentType(uniqueName, uiElementMapper.getElement("publisher.general.name"));
        asset.validateDetailsContentType(SERVICE_VERSION, uiElementMapper.getElement("publisher.general.version"));
    }

    @Test(groups = "wso2.greg", description = "Update custom RXT instance's description and validate it",
            dependsOnMethods = "testClickAndValidate")
    public void testUpdateAsset() {
        driver.findElement(By.id(uiElementMapper.getElement("publisher.defaults.edit.link"))).click();
        driver.findElement(By.id(uiElementMapper.getElement("publisher.asset.description"))).click();
        driver.findElement(By.id(uiElementMapper.getElement("publisher.asset.description"))).clear();
        driver.findElement(By.id(uiElementMapper.getElement("publisher.asset.description"))).sendKeys(getUniqueName());
        driver.findElement(By.id(uiElementMapper.getElement("publisher.defaults.edit.button"))).click();
        assertTrue(isElementPresent(driver, By.id(uiElementMapper.getElement("publisher.defaults.collapse"))),
                "Custom RXT instance not Updated for test case " + log.getClass().getName());
        driver.findElement(By.linkText(uiElementMapper.getElement("publisher.ea.name"))).click();
        driver.findElementByDynamicScroll(By.id(uniqueName));
        assertTrue(isElementPresent(driver, By.id(uniqueName)), "Enterprise Applications : " + uniqueName +
                " not available in listing page for test case " + log.getClass().getName());
    }

    @Test(groups = "wso2.greg", description = "Delete custom RXT instance and validate it",
            dependsOnMethods = "testUpdateAsset")
    public void testDeleteAsset() throws MalformedURLException, XPathExpressionException {
        PublisherUtil asset = new PublisherUtil(driver);
        asset.clickOnGenericTypeAsset(uiElementMapper.getElement("publisher.ea.name"), uniqueName);
        asset.deleteAndValidateAsset(uiElementMapper.getElement("publisher.ea.name"), uniqueName);
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws IOException {
        PublisherHomePage publisherHomePage = new PublisherHomePage(driver);
        publisherHomePage.logOut();
        driver.quit();
    }

}

