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
import org.openqa.selenium.WebDriver;
import org.wso2.greg.integration.common.ui.page.util.UIElementMapper;

/**
 * UI object model class for asset listing page of the store for a given asset type.
 * This can be used to perform operations on asset listing page.
 */
public class StoreAssetListPage {

    private WebDriver driver;
    private UIElementMapper uiElementMapper;
    private String assetType;
    private String pageSubtitle;

    public StoreAssetListPage(WebDriver driver, String assetType) {
        this.driver = driver;
        this.assetType = assetType;
        this.uiElementMapper = UIElementMapper.getInstance();
        this.pageSubtitle = this.driver.findElement(
                By.className(uiElementMapper.getElement("store.listpage.subtitle.class.prefix") + assetType)).getText()
                .trim();
        this.uiElementMapper = UIElementMapper.getInstance();
    }

    /**
     * Following methods can be used to verify the subtitle of the asset listing page for a given asset type.
     *
     * @return True or false based on the asset type and the subtitle displayed.
     */
    public boolean verifyPolicyAssetListPageSubtitle() {
        return this.pageSubtitle.equals("Policies");
    }

    public boolean verifySchemaAssetListPageSubtitle() {
        return this.pageSubtitle.equals("Schemas");
    }

    public boolean verifyRESTAssetListPageSubtitle() {
        return this.pageSubtitle.equals("REST Services");
    }

    public boolean verifySOAPAssetListPageSubtitle() {
        return this.pageSubtitle.equals("SOAP Services");
    }

    public boolean verifySwaggerAssetListPageSubtitle() {
        return this.pageSubtitle.equals("Swaggers");
    }

    public boolean verifyWADLAssetListPageSubtitle() {
        return this.pageSubtitle.equals("WADLs");
    }

    public boolean verifyWSDLAssetListPageSubtitle() {
        return this.pageSubtitle.equals("WSDLs");
    }

    /**
     * Make sure the particular asset that is used to verify the asset type is displayed on the asset listing page.
     * Particular asset has to be defined in the mapper.properties file with the asset type.
     *
     * @return True ar false based on any asset is listed or not on asset container div.
     */
    public boolean verifyAssetsListing() {
        String assetId = uiElementMapper.getElement("store.listpage." + this.assetType + ".testasset.id");
        return driver.findElements(By.id(assetId)).size() > 0;
    }

    /**
     * Performs the action of clicking on a particular asset that is defined inside the mapper.properties file.
     *
     * @return This returns an AssetOverviewPage object.
     */
    public AssetOverviewPage ClickAsset() {
        String assetId = uiElementMapper.getElement("store.listpage." + this.assetType + ".testasset.id");
        driver.findElement(By.id(assetId)).click();
        return new AssetOverviewPage(driver, assetId);
    }
}
