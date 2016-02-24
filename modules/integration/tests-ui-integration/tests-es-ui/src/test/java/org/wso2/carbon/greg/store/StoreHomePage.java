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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.wso2.carbon.greg.store.exceptions.StoreTestException;
import org.wso2.carbon.greg.utils.StoreTestUtils;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.greg.integration.common.ui.page.util.UIElementMapper;

import java.io.File;

/**
 * UI page object class of store home page. This can be used to perform actions on the home page.
 */
public class StoreHomePage {

    private static final Log log = LogFactory.getLog(StoreHomePage.class);
    private WebDriver driver;
    private UIElementMapper uiElementMapper;
    private String gettingStartedPath =
            CarbonUtils.getCarbonHome() + File.separator + "samples" + File.separator + "getting_started"
                    + File.separator + "build.xml";

    /**
     * Constructor for store home page. This asserts if the driver is on the store home page before returns.
     *
     * @param driver WebDriver to perform actions on the page
     * @throws StoreTestException throws if the driver is not on the home page at the construction.
     */
    public StoreHomePage(WebDriver driver) throws StoreTestException {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();
        log.info("Page loaded : Store landing page");
    }

    /**
     * Performs logging out from Store
     */
    public void logOut() {
        driver.findElement(By.id(uiElementMapper.getElement("store.homepage.loggedinuser.id"))).click();
        driver.findElement(By.linkText(uiElementMapper.getElement("store.logoutText.link"))).click();
    }

    /**
     * Performs moving to login page.
     *
     * @return Login page object.
     */
    public StoreLoginPage moveToLoginPage() {
        driver.findElement(By.id(uiElementMapper.getElement("store.homepage.loginbutton.id"))).click();
        return new StoreLoginPage(driver);
    }

    /**
     * Performs running the ant script for getting started guide and populating the store.
     */
    public void populateStore() {
        try {
            StoreTestUtils.runAnt(gettingStartedPath, "run -Dport=10343");
        } catch (StoreTestException e) {
            throw new IllegalStateException("Ant script could not be executed", e);
        }
    }

    /**
     * Performs running the ant script to remove populated artifacts.
     */
    public void unpopulateStore() {
        try {
            StoreTestUtils.runAnt(gettingStartedPath, "remove -Dport=10343");
        } catch (StoreTestException e) {
            throw new IllegalStateException("Ant script could not be executed", e);
        }
    }

    /**
     * Verifies whether there are any assets visible on store by moving to asset listing page given the asset type.
     *
     * @param aType Asset type needs to be checked.
     * @return True, if any assets are visible.
     */
    public boolean verifyAssetsOnHomePage(String aType) {
        String assetClassName = uiElementMapper.getElement("store.homepage.asset.classname");
        String navMenuId = uiElementMapper.getElement("store.navmenu." + aType + ".id");
        driver.findElement(By.id(navMenuId)).click();
        return driver.findElements(By.className(assetClassName)).size() > 0;
    }

    /**
     * Performs clicking on see more button of a particular asset type.
     *
     * @param aType Asset type that needs to be expanded.
     * @return AssetList page object belong to the particular asset type.
     */
    public StoreAssetListPage clickOnSeeMore(String aType) {
        String seeMoreXpath = uiElementMapper.getElement("store.homepage.listing." + aType + ".xpath");
        driver.findElement(By.xpath(seeMoreXpath)).click();
        return new StoreAssetListPage(driver, aType);
    }

    /**
     * Performs clicking on a navBarbutton
     *
     * @param aType Asset type to be clicked on
     */
    public void clickOnNavButton(String aType) {
        String navMenuId = uiElementMapper.getElement("store.navmenu." + aType + ".id");
        driver.findElement(By.id(navMenuId)).click();
    }

}
