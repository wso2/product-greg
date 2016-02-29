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
 * UI object model class for asset overview page of Store.
 * This can be used to perform actions on the overview page.
 */
public class AssetOverviewPage {

    private String shortName;
    private UIElementMapper uiElementMapper;
    private WebDriver driver;
    private String storeOverviewPageName;

    public AssetOverviewPage(WebDriver driver, String shortName) {
        this.shortName = shortName;
        this.driver = driver;
        uiElementMapper = UIElementMapper.getInstance();
        String storeOverviewPageNameId = uiElementMapper.getElement("store.overview.assetname.id");
        this.storeOverviewPageName = driver.findElement(By.id(storeOverviewPageNameId)).getText().trim();
    }

    /**
     * Verifies the whether the overview page contains the short name of the asset as its subtitle.
     *
     * @return True, if overview page contains the asset short name as the subtitle.
     */
    public boolean verifyOverviewPageName() {
        return storeOverviewPageName.equals(this.shortName);
    }

    /**
     * Verifies if the download asset file button exists on the overview page.
     *
     * @return True, if the download button exists.
     */
    public boolean verifyDownloadFileButton() {
        return driver.findElements(By.id(uiElementMapper.getElement("store.overview.download.a.id"))).size() > 0;
    }

    /**
     * Verifies if the user reviews tab exists on the overview page.
     *
     * @return True, if the user reviews tab exists.
     */
    public boolean verifyReviewButton() {
        return driver.findElements(By.linkText(uiElementMapper.getElement("store.overview.userreview.a.id"))).size()
                > 0;
    }
}
