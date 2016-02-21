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
import org.wso2.carbon.greg.store.AssetOverviewPage;
import org.wso2.carbon.greg.store.StoreAssetListPage;
import org.wso2.carbon.greg.store.StoreHomePage;
import org.wso2.carbon.greg.store.StoreLoginPage;
import org.wso2.carbon.greg.store.exceptions.StoreTestException;
import org.wso2.greg.integration.common.utils.GREGIntegrationUIBaseTest;

import javax.xml.xpath.XPathExpressionException;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertTrue;

public class AssetsListingTestCase extends GREGIntegrationUIBaseTest {

    private WebDriver driver;
    private String baseUrl;
    private StoreHomePage storeHomePage;

    @BeforeClass(alwaysRun = true, description = "Basic setup and populating the store")
    public void setUp() throws Exception {
        super.init();
        driver = BrowserManager.getWebDriver();
        driver.manage().timeouts().implicitlyWait(35, TimeUnit.SECONDS);
        baseUrl = getStoreBaseUrl();
        driver.get(baseUrl);
        storeHomePage = new StoreHomePage(driver);
        storeHomePage.populateStore();
    }

    @Test(description = "Performs verification on listing page and overview page with predefined assets")
    public void verifyAssetsByName() throws XPathExpressionException, StoreTestException {
        driver.get(baseUrl);
        StoreLoginPage storeLoginPage = storeHomePage.moveToLoginPage();
        storeHomePage = storeLoginPage.Login(automationContext.getContextTenant().getContextUser().getUserName(),
                automationContext.getContextTenant().getContextUser().getPassword());

        this.doVerifyAssetByName("policy");
        this.doVerifyAssetByName("restservice");
        this.doVerifyAssetByName("schema");
        this.doVerifyAssetByName("soapservice");
        this.doVerifyAssetByName("swagger");
        this.doVerifyAssetByName("wadl");
        this.doVerifyAssetByName("wsdl");

        storeHomePage.logOut();
    }

    /**
     * Performs asset verification by moving to asset listing page and overview page give the asset type.
     * Ids of different types of assets are defined in the mapper.properties file.
     *
     * @param aType Asset type needs to be tested.
     */
    private void doVerifyAssetByName(String aType) {
        StoreAssetListPage assetListPage = storeHomePage.clickOnSeeMore(aType);

        if (aType.equals("policy")) {
            assertTrue(assetListPage.verifyPolicyAssetListPageSubtitle(), "Listing page subtitle is not Policies");
        } else if (aType.equals("restservice")) {
            assertTrue(assetListPage.verifyRESTAssetListPageSubtitle(), "Listing page subtitle is not REST Services");
        } else if (aType.equals("schema")) {
            assertTrue(assetListPage.verifySchemaAssetListPageSubtitle(), "Listing page subtitle is not Schemas");
        } else if (aType.equals("soapservice")) {
            assertTrue(assetListPage.verifySOAPAssetListPageSubtitle(), "Listing page subtitle is not SOAP Services");
        } else if (aType.equals("swagger")) {
            assertTrue(assetListPage.verifySwaggerAssetListPageSubtitle(), "Listing page subtitle is not Swaggers");
        } else if (aType.equals("wadl")) {
            assertTrue(assetListPage.verifyWADLAssetListPageSubtitle(), "Listing page subtitle is not WADLs");
        } else if (aType.equals("wsdl")) {
            assertTrue(assetListPage.verifyWSDLAssetListPageSubtitle(), "WSDLs");
        }

        assertTrue(assetListPage.verifyAssetsListing());
        AssetOverviewPage assetOverviewPage = assetListPage.ClickAsset();
        assertTrue(assetOverviewPage.verifyOverviewPageName());
        assertTrue(assetOverviewPage.verifyDownloadFileButton());
        assertTrue(assetOverviewPage.verifyReviewButton());

        driver.get(baseUrl);
        log.info("Asset listing test successful for " + aType);
    }

    @AfterClass(alwaysRun = true, description = "Quiting the test and removing assets from the store.")
    public void tearDown() {
        storeHomePage.unpopulateStore();
        driver.quit();
    }
}
