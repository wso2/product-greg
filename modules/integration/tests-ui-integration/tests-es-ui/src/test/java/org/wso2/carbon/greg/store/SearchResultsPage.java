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

package org.wso2.carbon.greg.store;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.wso2.greg.integration.common.ui.page.util.UIElementMapper;

import static org.testng.AssertJUnit.assertTrue;

/**
 * UI model object class for search results page. This can be used to perform operations on the search results page.
 */
public class SearchResultsPage {

    private WebDriver driver;
    private UIElementMapper uiElementMapper;

    /**
     * Constructor for search results page. This performs search of any given query on store and constructs
     * the results page. This verifies if the driver is in results page before returns.
     *
     * @param driver   WebDriver object to perform operations.
     * @param query    The search query.
     * @param hitEnter True, if the search needs to be done by hitting the enter button on search field.
     */
    public SearchResultsPage(WebDriver driver, String query, boolean hitEnter) {

        this.driver = driver;
        uiElementMapper = UIElementMapper.getInstance();

        this.driver.findElement(By.id(uiElementMapper.getElement("store.searchbar.id"))).click();
        this.driver.findElement(By.id(uiElementMapper.getElement("store.searchbar.id"))).clear();
        if (hitEnter) {
            this.driver.findElement(By.id(uiElementMapper.getElement("store.searchbar.id")))
                    .sendKeys(query, Keys.ENTER);
        } else {
            this.driver.findElement(By.id(uiElementMapper.getElement("store.searchbar.id"))).sendKeys(query);
            this.driver.findElement(By.id(uiElementMapper.getElement("store.searchbtn.id"))).click();
        }
        assertTrue(driver.getCurrentUrl().contains("q="));
    }

    /**
     * Counts the number of assets on the results page.
     *
     * @return Integer, number of assets on the page.
     */
    public int countResults() {
        return this.driver.findElements(By.className(uiElementMapper.getElement("store.listpage.thumbnail"))).size();
    }

    /**
     * Verifies if the page contains any given element.
     *
     * @param assetId id of the asset to be checked
     * @return True, if the asset is listed on the page.
     */
    public boolean containsAsset(String assetId) {
        return this.driver.findElements(By.id(assetId)).size() > 0;
    }

    /**
     * Performs clicking on a tag given the tag name.
     *
     * @param tagName name of the tag that needs to be clicked on.
     */
    public void clickOnTag(String tagName) {
        this.driver.findElement(By.linkText(uiElementMapper.getElement("store.tag." + tagName + ".linktext"))).click();
    }
}
