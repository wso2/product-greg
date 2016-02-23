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
package org.wso2.carbon.greg.publisher.searchui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.carbon.greg.publisher.PublisherHomePage;
import org.wso2.carbon.greg.publisher.PublisherLoginPage;
import org.wso2.carbon.greg.publisher.utils.ESWebDriver;
import org.wso2.carbon.greg.publisher.utils.PublisherUtil;
import org.wso2.greg.integration.common.ui.page.util.UIElementMapper;
import org.wso2.greg.integration.common.utils.GREGIntegrationUIBaseTest;

import java.net.MalformedURLException;
import java.util.concurrent.TimeUnit;
import javax.xml.xpath.XPathExpressionException;

import static org.testng.Assert.assertTrue;

/***
 * This class have test methods for asset type search. (SOAP services)
 */
public class SoapServiceSearchTestCase extends GREGIntegrationUIBaseTest {
    private static final Log log = LogFactory.getLog(SoapServiceSearchTestCase.class);
    private static final String SERVICE_VERSION1 = "1.2.3";
    private static final String SERVICE_VERSION2 = "3.2.1";
    String uniqueName;
    private ESWebDriver driver;
    private String UUID;
    private UIElementMapper uiElementMapper;
    private PublisherUtil asset;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = new ESWebDriver(BrowserManager.getWebDriver());
        driver.manage().timeouts().implicitlyWait(LOGIN_WAIT_SECONDS, TimeUnit.SECONDS);
        driver.get(getPublisherBaseURL());
        PublisherLoginPage publisherLoginPage = new PublisherLoginPage(driver);
        this.uiElementMapper = UIElementMapper.getInstance();

        // performing login to publisher
        publisherLoginPage.loginAs(automationContext.getContextTenant().getContextUser().getUserName(),
                automationContext.getContextTenant().getContextUser().getPassword());
        // create new asset
        asset = new PublisherUtil(driver);
        uniqueName = getUniqueName();
        asset.createGenericTypeAsset(uniqueName, "/" + uniqueName, "1.2.1", "TestDescription1" + uniqueName,
                uiElementMapper.getElement("publisher.soapservices"));
        uniqueName = getUniqueName();
        asset.createGenericTypeAsset(uniqueName, "/" + uniqueName, "1.2.2", "TestDescription2" + uniqueName,
                uiElementMapper.getElement("publisher.soapservices"));
        uniqueName = getUniqueName();
        //TODO use file seperator
        asset.createGenericTypeAsset(uniqueName, "/" + uniqueName, "1.2.3", "TestDescription3" + uniqueName,
                uiElementMapper.getElement("publisher.soapservices"));
        UUID = asset.getUUID();
    }

    @Test(groups = "wso2.greg", description = "search for a SOAP service inside rest services")
    public void searchByName() throws MalformedURLException, XPathExpressionException {
        driver.findDynamicElement(By.linkText(uiElementMapper.getElement("publisher.soapservices")), WAIT_SECONDS)
                .click();
        asset.generalSearch(uniqueName);
    }

    @Test(groups = "wso2.greg", description = "Click and validate the asset", dependsOnMethods = "searchByName")
    public void clickAndValidateNameSearch() {
        driver.findElementByDynamicScroll(By.id(UUID));
        assertTrue(isElementPresent(driver, By.id(UUID)), "asset " + uniqueName +
                " not available in listing page for test case " + log.getClass().getName());
        driver.findElement(By.id(UUID)).click();
        asset.validateDetailsContentType(uniqueName, uiElementMapper.getElement("publisher.general.name"));
        asset.validateDetailsContentType(SERVICE_VERSION1, uiElementMapper.getElement("publisher.general.version"));
        asset.createVersion(SERVICE_VERSION2);
    }

    @Test(groups = "wso2.greg", description = "search for a SOAP service inside rest services",
            dependsOnMethods = "clickAndValidateNameSearch")
    public void searchByVersion() throws MalformedURLException, XPathExpressionException {
        driver.findDynamicElement(By.linkText(uiElementMapper.getElement("publisher.soapservices")), WAIT_SECONDS)
                .click();
        asset.generalSearch("version:3.2.1");
    }

    @Test(groups = "wso2.greg", description = "Click and validate the asset", dependsOnMethods = "searchByVersion")
    public void clickAndValidateVersionSearch() {
        driver.findElementByDynamicScroll(By.id(asset.getUUID()));
        assertTrue(isElementPresent(driver, By.id(asset.getUUID())), "asset " + uniqueName +
                " not available in listing page for test case " + log.getClass().getName());
        driver.findElement(By.id(asset.getUUID())).click();
        asset.validateDetailsContentType(uniqueName, uiElementMapper.getElement("publisher.general.name"));
        asset.validateDetailsContentType(SERVICE_VERSION2, uiElementMapper.getElement("publisher.general.version"));
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        PublisherHomePage publisherHomePage = new PublisherHomePage(driver);
        publisherHomePage.logOut();
        driver.quit();
    }

}