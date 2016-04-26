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

package org.wso2.carbon.greg.publisher.general;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.carbon.greg.publisher.login.PublisherHomePage;
import org.wso2.carbon.greg.publisher.login.PublisherLoginPage;
import org.wso2.carbon.greg.publisher.utils.ESWebDriver;
import org.wso2.carbon.greg.publisher.utils.PublisherUtil;
import org.wso2.greg.integration.common.ui.page.util.UIElementMapper;
import org.wso2.greg.integration.common.utils.GREGIntegrationUIBaseTest;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.concurrent.TimeUnit;
import javax.xml.xpath.XPathExpressionException;

/***
 * This class have test methods for a creating a new REST service version
 */
public class VersionTestCase extends GREGIntegrationUIBaseTest {
    private static final Log log = LogFactory.getLog(VersionTestCase.class);
    private static final String SERVICE_VERSION = "1.2.4";
    PublisherUtil asset;
    private ESWebDriver driver;
    private UIElementMapper uiElementMapper;
    private String uniqueName;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = new ESWebDriver(BrowserManager.getWebDriver());
        driver.manage().timeouts().implicitlyWait(LOGIN_WAIT_SECONDS, TimeUnit.SECONDS);
        driver.get(getPublisherBaseUrl());
        PublisherLoginPage publisherLoginPage = new PublisherLoginPage(driver);
        this.uiElementMapper = UIElementMapper.getInstance();
        // performing login to publisher
        publisherLoginPage.loginAs(automationContext.getContextTenant().getContextUser().getUserName(),
                automationContext.getContextTenant().getContextUser().getPassword());
        // create new asset
        asset = new PublisherUtil(driver);
        uniqueName = getUniqueName();
        asset.createGenericTypeAsset(uniqueName, "/" + uniqueName, "1.2.2", "TestDescription" + uniqueName,
                uiElementMapper.getElement("publisher.restservices"));
        asset.clickOnGenericTypeAsset(uiElementMapper.getElement("publisher.restservices"), asset.getUUID());
    }

    @Test(groups = "wso2.greg", description = "Create new version for a created rest service")
    public void testCreateVersion() throws MalformedURLException, XPathExpressionException {
        asset.createVersion(SERVICE_VERSION);
        driver.findElement(By.linkText(uiElementMapper.getElement("publisher.defaults.view"))).click();
        Assert.assertTrue(isElementPresent(driver, By.id(uiElementMapper.getElement("publisher.defaults.collapse"))),
                "Rest service : " + uniqueName + " Version " + SERVICE_VERSION + " is not Created for test case " + log
                        .getClass().toString());
    }

    @Test(groups = "wso2.greg", description = "Click and validate the asset", dependsOnMethods = "testCreateVersion")
    public void testClickAndValidate() throws MalformedURLException, XPathExpressionException {
        asset.clickOnGenericTypeAsset(uiElementMapper.getElement("publisher.restservices"), asset.getUUID());
        asset.validateDetailsContentType(uniqueName, uiElementMapper.getElement("publisher.general.name"));
        asset.validateDetailsContentType(SERVICE_VERSION, uiElementMapper.getElement("publisher.general.version"));
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws IOException {
        PublisherHomePage publisherHomePage = new PublisherHomePage(driver);
        publisherHomePage.logOut();
        driver.quit();
    }

}