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
 * This class have test methods for landing page search, policy,rest services,advanced search
 */
public class CrossAssetSearchTestCase extends GREGIntegrationUIBaseTest {
    private static final Log log = LogFactory.getLog(CrossAssetSearchTestCase.class);
    private static final String SERVICE_VERSION1 = "1.2.2";
    private static final String SERVICE_VERSION2 = "1.0.0";
    private static final String POLICY_NAME = "UTPolicy.xml";
    private static final String WSDL_NAME = "StockQuote.wsdl";
    private static final String POLICY_URL = "https://raw.githubusercontent.com/wso2/wso2-qa-artifacts/master/"
            + "automation-artifacts/greg/policy/UTPolicy.xml";
    String uniqueName;
    String tempName, tempName2;
    private ESWebDriver driver;
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
        asset.createGenericTypeAsset(uniqueName, "/" + uniqueName, "1.2.1", "TestDescription" + uniqueName,
                uiElementMapper.getElement("publisher.restservices"));
        uniqueName = getUniqueName();
        tempName = uniqueName;
        asset.createGenericTypeAsset(uniqueName, "/" + uniqueName, SERVICE_VERSION1, "TestDescription" + uniqueName,
                uiElementMapper.getElement("publisher.restservices"));
        uniqueName = getUniqueName();
        asset.createGenericTypeAsset(uniqueName, "/" + uniqueName, "1.2.3", "TestDescription" + uniqueName,
                uiElementMapper.getElement("publisher.restservices"));
        uniqueName = getUniqueName();
        asset.createGenericTypeAsset(uniqueName, "/" + uniqueName, "1.2.1", "TestDescription" + uniqueName,
                uiElementMapper.getElement("publisher.restservices"));
        uniqueName = getUniqueName();
        asset.createGenericTypeAsset(uniqueName, "/" + uniqueName, "1.2.2", "TestDescription" + uniqueName,
                uiElementMapper.getElement("publisher.restservices"));
        uniqueName = getUniqueName();
        asset.createGenericTypeAsset(uniqueName, "/" + uniqueName, "1.2.3", "TestDescription" + uniqueName,
                uiElementMapper.getElement("publisher.restservices"));

        uniqueName = getUniqueName();
        tempName2 = uniqueName;
        asset.createGenericAsset(POLICY_URL, uniqueName + POLICY_NAME, SERVICE_VERSION2,
                uiElementMapper.getElement("publisher.policies"));

        asset.createGenericAsset(POLICY_URL, getUniqueName() + POLICY_NAME, "2.0.0", uiElementMapper.
                getElement("publisher.policies"));

        asset.createGenericAsset("https://raw.githubusercontent.com/wso2/wso2-qa-artifacts/master/automation-artifacts/"
                        + "greg/wsdl/StockQuote.wsdl", getUniqueName() + WSDL_NAME, "1.0.0",
                uiElementMapper.getElement("publisher.wsdls"));
    }

    @Test(groups = "wso2.greg", description = "search for a REST service in landing page")
    public void testRestServiceSearch() throws MalformedURLException, XPathExpressionException {
        PublisherUtil util = new PublisherUtil(driver);
        util.landingPageSearch(tempName);
    }

    @Test(groups = "wso2.greg", description = "Click and validate the asset", dependsOnMethods = "testRestServiceSearch")
    public void testClickAndValidateRestSearch() {
        driver.findElementByDynamicScroll(By.id(tempName));
        assertTrue(isElementPresent(driver, By.id(tempName)), "asset " + tempName +
                " not available in listing page for test case " + log.getClass().getName());
        driver.findElement(By.id(tempName)).click();
        asset.validateDetailsContentType(tempName, uiElementMapper.getElement("publisher.general.name"));
        asset.validateDetailsContentType(SERVICE_VERSION1, uiElementMapper.getElement("publisher.general.version"));
    }

    @Test(groups = "wso2.greg", description = "search for a policy service in landing page",
            dependsOnMethods = "testClickAndValidateRestSearch")
    public void testColicySearch() throws MalformedURLException, XPathExpressionException {
        PublisherUtil util = new PublisherUtil(driver);
        util.landingPageSearch(tempName2);
    }

    @Test(groups = "wso2.greg", description = "Click and validate the asset", dependsOnMethods = "testColicySearch")
    public void testClickAndValidatePolicySearch() {
        driver.findElementByDynamicScroll(By.id(tempName2 + POLICY_NAME));
        assertTrue(isElementPresent(driver, By.id(tempName2 + POLICY_NAME)), "asset " + tempName2 + POLICY_NAME +
                " not available in listing page for test case " + log.getClass().getName());
        driver.findElement(By.id(tempName2 + POLICY_NAME)).click();
        asset.validateDetailsGenericType(tempName2 + POLICY_NAME, uiElementMapper.getElement("publisher.general.name"));
        asset.validateDetailsGenericType(SERVICE_VERSION2, uiElementMapper.getElement("publisher.general.version"));
    }

    @Test(groups = "wso2.greg", description = "Do landing page advanced search for a policy by name and version",
            dependsOnMethods = "testClickAndValidatePolicySearch")
    public void testAdvancedSearch() throws MalformedURLException, XPathExpressionException {
        driver.get(getPublisherBaseURL());
        PublisherUtil util = new PublisherUtil(driver);
        util.advancedSearch(tempName2, SERVICE_VERSION2, "", "");
        assertTrue(isElementPresent(driver, By.id(tempName2 + POLICY_NAME)),
                "Policy " + tempName2 + POLICY_NAME + " is not listed after search for test case" +
                        log.getClass().toString());
    }

    @Test(groups = "wso2.greg", description = "Click and validate the asset", dependsOnMethods = "testAdvancedSearch")
    public void testClickAndValidateAdvancedSearch() {
        driver.findElementByDynamicScroll(By.id(tempName2 + POLICY_NAME));
        assertTrue(isElementPresent(driver, By.id(tempName2 + POLICY_NAME)), "asset " + tempName2 + POLICY_NAME +
                " not available in listing page for test case " + log.getClass().getName());
        driver.findElement(By.id(tempName2 + POLICY_NAME)).click();
        asset.validateDetailsGenericType(tempName2 + POLICY_NAME,
                uiElementMapper.getElement("publisher.general.name"));
        asset.validateDetailsGenericType(SERVICE_VERSION2, uiElementMapper.getElement("publisher.general.version"));
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        PublisherHomePage publisherHomePage = new PublisherHomePage(driver);
        publisherHomePage.logOut();
        driver.quit();
    }

}