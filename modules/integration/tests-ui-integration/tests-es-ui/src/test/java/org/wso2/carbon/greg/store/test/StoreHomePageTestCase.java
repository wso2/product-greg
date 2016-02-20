/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.greg.store.test;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.carbon.greg.store.StoreHomePage;
import org.wso2.carbon.greg.store.StoreLoginPage;
import org.wso2.carbon.greg.store.exceptions.StoreTestException;
import org.wso2.greg.integration.common.utils.GREGIntegrationUIBaseTest;

import javax.xml.xpath.XPathExpressionException;

import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertTrue;

public class StoreHomePageTestCase extends GREGIntegrationUIBaseTest {

    private WebDriver driver;
    private String baseUrl;
    private StoreHomePage storeHomePage;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = BrowserManager.getWebDriver();
        baseUrl = getStoreBaseUrl();
        driver.get(baseUrl);
        storeHomePage = new StoreHomePage(driver);
        storeHomePage.populateStore();
        driver.manage().timeouts().implicitlyWait(35, TimeUnit.SECONDS);
    }

    @Test(description = "This tests the anonymous view of Store home page. Assets other than schemas and policies should be visible here")
    public void testAnonymousView() throws StoreTestException, XPathExpressionException {
        driver.get(baseUrl);
        assertTrue(!storeHomePage.verifyAssetsOnHomePage("policy"), "Policies are visible on anonymous listing");
        assertTrue(storeHomePage.verifyAssetsOnHomePage("restservice"), "No REST services on anonymous listing");
        assertTrue(!storeHomePage.verifyAssetsOnHomePage("schema"), "Schemas are visible on anonymous listing");
        assertTrue(storeHomePage.verifyAssetsOnHomePage("soapservice"), "No SOAP services on anonymous listing");
        assertTrue(storeHomePage.verifyAssetsOnHomePage("swagger"), "No swaggers on anonymous listing");
        assertTrue(storeHomePage.verifyAssetsOnHomePage("wadl"), "No WADLs on anonymous listing");
        assertTrue(storeHomePage.verifyAssetsOnHomePage("wsdl"), "No WSDLs on anonymous listing");
    }

    @Test(description = "This tests visibility of assets on a signed in home page")
    public void testSignedInView() throws StoreTestException, XPathExpressionException {
        driver.get(baseUrl);
        StoreLoginPage storeLoginPage = storeHomePage.moveToLoginPage();
        storeHomePage = storeLoginPage.Login(automationContext.getContextTenant().getContextUser().getUserName(),
                automationContext.getContextTenant().getContextUser().getPassword());

        assertTrue(storeHomePage.verifyAssetsOnHomePage("policy"), "No Policies on list view");
        assertTrue(storeHomePage.verifyAssetsOnHomePage("restservice"), "No REST services on list view");
        assertTrue(storeHomePage.verifyAssetsOnHomePage("schema"), "No schemas on list view");
        assertTrue(storeHomePage.verifyAssetsOnHomePage("soapservice"), "No SOAP services on list view");
        assertTrue(storeHomePage.verifyAssetsOnHomePage("swagger"), "No swaggers on list view");
        assertTrue(storeHomePage.verifyAssetsOnHomePage("wadl"), "No WADLs on list view");
        assertTrue(storeHomePage.verifyAssetsOnHomePage("wsdl"), "No WSDLs on list view");

        storeHomePage.logOut();
    }

    @AfterClass(alwaysRun = true, description = "Quiting the test and removing assets from the store.")
    public void tearDown() throws Exception {
        storeHomePage.unpopulateStore();
        driver.quit();
        log.info("Finished running anonymous view test");
    }
}
