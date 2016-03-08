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

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.carbon.greg.store.SearchResultsPage;
import org.wso2.carbon.greg.store.StoreHomePage;
import org.wso2.carbon.greg.store.StoreLoginPage;
import org.wso2.carbon.greg.store.exceptions.StoreTestException;
import org.wso2.greg.integration.common.ui.page.util.UIElementMapper;
import org.wso2.greg.integration.common.utils.GREGIntegrationUIBaseTest;

import javax.xml.xpath.XPathExpressionException;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class SearchTestCase extends GREGIntegrationUIBaseTest {

    private WebDriver driver;
    private String baseUrl;
    private StoreHomePage storeHomePage;
    private UIElementMapper uiElementMapper;

    @BeforeClass(alwaysRun = true, description = "Basic setup and populating the store")
    public void setUp() throws Exception {
        super.init();
        driver = BrowserManager.getWebDriver();
        driver.manage().timeouts().implicitlyWait(90, TimeUnit.SECONDS);
        baseUrl = getStoreBaseUrl();
        uiElementMapper = UIElementMapper.getInstance();
        driver.get(baseUrl);
        storeHomePage = new StoreHomePage(driver);
        storeHomePage.populateStore();
        StoreLoginPage storeLoginPage = storeHomePage.moveToLoginPage();
        storeHomePage = storeLoginPage.Login(automationContext.getContextTenant().getContextUser().getUserName(),
                automationContext.getContextTenant().getContextUser().getPassword());
    }

    @Test(description = "This test is intended to check hit enter and click on search button")
    public void testSearchBar() throws XPathExpressionException, StoreTestException {
        SearchResultsPage searchResultsPageClick = new SearchResultsPage(driver, "uber", false);
        int countHitEnter = searchResultsPageClick.countResults();
        SearchResultsPage searchResultsPageHitEnter = new SearchResultsPage(driver, "uber", true);
        int countClick = searchResultsPageHitEnter.countResults();
        assertEquals(countHitEnter, countClick,
                "Search results returned from clicking search button are different from pressing Enter key");
    }

    @Test(description = "This test is intended to test search query with spaces", enabled = false)
    public void testSearchBarTrailingSpaces() {
        SearchResultsPage searchResultspage = new SearchResultsPage(driver, "            uber              ", false);
        int count = searchResultspage.countResults();
        assertTrue(count > 0, "No results returned from the search query");
        assertTrue(searchResultspage.containsAsset(uiElementMapper.getElement("store.search.namesearch1.asset1.id")),
                "Returned results are different from expected results");
    }

    @Test(description = "Test search ability with and without name tag")
    public void testSearchByName() {
        SearchResultsPage searchResultspageByName = new SearchResultsPage(driver, "uber", false);
        int countName = searchResultspageByName.countResults();
        assertTrue(
                searchResultspageByName.containsAsset(uiElementMapper.getElement("store.search.namesearch1.asset1.id")),
                "Expected asset is not returned from the search query");
        assertTrue(
                searchResultspageByName.containsAsset(uiElementMapper.getElement("store.search.namesearch2.asset1.id")),
                "Expected asset is not returned from the search query");

        SearchResultsPage searchResultspageByNameTag = new SearchResultsPage(driver, "name:uber", false);
        assertTrue(searchResultspageByNameTag
                        .containsAsset(uiElementMapper.getElement("store.search.namesearch1.asset1.id")),
                "Expected asset is not returned from the search query");
        assertTrue(searchResultspageByNameTag
                        .containsAsset(uiElementMapper.getElement("store.search.namesearch2.asset1.id")),
                "Expected asset is not returned from the search query");
        int countNameTag = searchResultspageByNameTag.countResults();

        assertEquals(countName, countNameTag, "Results returned from name search queries are different");
    }

    @Test(description = "Assert results returned by clicking on a tag and searching for the same tag")
    public void testSearchByTag() {
        SearchResultsPage searchResultspage = new SearchResultsPage(driver, "tags:people", false);
        int countSearch = searchResultspage.countResults();
        assertTrue(searchResultspage.containsAsset(uiElementMapper.getElement("store.search.tag1.resultasset.id")),
                "Expected asset is not returned from the search query");
        searchResultspage.clickOnTag("people");
        int countTag = searchResultspage.countResults();
        assertTrue(searchResultspage.containsAsset(uiElementMapper.getElement("store.search.tag1.resultasset.id")),
                "Expected asset is not returned from the search query");
        assertEquals(countSearch, countTag);
    }

    @Test(description = "Test the ability to search by version")
    public void testSearchByVersion() {
        SearchResultsPage searchResultspage = new SearchResultsPage(driver, "version:1.0.0", false);
        assertTrue(searchResultspage.containsAsset(uiElementMapper.getElement("store.search.version1.asset1.id")),
                "Expected asset is not returned from the search query");
        assertTrue(searchResultspage.containsAsset(uiElementMapper.getElement("store.search.version1.asset2.id")),
                "Expected asset is not returned from the search query");
    }

    @Test(description = "Test the ability to search by content")
    public void testSearchByContent() {
        SearchResultsPage searchResultspage = new SearchResultsPage(driver, "content:pet", false);
        assertTrue(searchResultspage.containsAsset(uiElementMapper.getElement("store.search.content.asset1.id")),
                "Expected asset is not returned from the search query");
        assertTrue(searchResultspage.containsAsset(uiElementMapper.getElement("store.search.content.asset2.id")),
                "Expected asset is not returned from the search query");
        assertTrue(searchResultspage.containsAsset(uiElementMapper.getElement("store.search.content.asset3.id")),
                "Expected asset is not returned from the search query");
    }

    @Test(description = "Test the ability to search by lifecycle name")
    public void testByLifecycleName() {
        storeHomePage.clickOnNavButton("restservice");
        SearchResultsPage searchResultspage = new SearchResultsPage(driver, "lcName:ServiceLifeCycle", false);
        assertTrue(searchResultspage
                        .containsAsset(uiElementMapper.getElement("store.search.lc.ServiceLifeCycle.asset1.id")),
                "Expected asset is not returned from the search query");
        assertTrue(searchResultspage
                        .containsAsset(uiElementMapper.getElement("store.search.lc.ServiceLifeCycle.asset2.id")),
                "Expected asset is not returned from the search query");
        assertTrue(searchResultspage
                        .containsAsset(uiElementMapper.getElement("store.search.lc.ServiceLifeCycle.asset3.id")),
                "Expected asset is not returned from the search query");
        driver.findElement(By.id(uiElementMapper.getElement("store.navmenu.all"))).click();
    }

    @Test(description = "Test search by version and asset name")
    public void testSearchByNameAndVersion() {
        SearchResultsPage searchResultspage = new SearchResultsPage(driver, "name:uber version:1.0", false);
        log.info("searched by name and version");
        assertTrue(searchResultspage.containsAsset(uiElementMapper.getElement("store.search.name.version.asset1.id")),
                "Expected asset is not returned from the search query");
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        storeHomePage.unpopulateStore();
        driver.quit();
    }
}
