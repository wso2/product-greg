/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.greg.integration.common.ui.page.searchhome;

import java.io.IOException;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.wso2.greg.integration.common.ui.page.util.UIElementMapper;

public class SearchHomePage {

    private WebDriver driver;
    private UIElementMapper uiElementMapper;

    public SearchHomePage(WebDriver driver) throws IOException {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();
        driver.findElement(By.linkText(uiElementMapper.getElement("search.page.link"))).click();
        // Check that we're on the right page.
        if (!(driver.getCurrentUrl().contains("search"))) {
            // Alternatively, we could navigate to the login page, perhaps logging out first
            throw new IllegalStateException("This is not the Search Activity page");
        }
    }

    public void search(String searchItem) {
        driver.findElement(By.name(uiElementMapper.getElement("search.resource.name"))).sendKeys(searchItem);
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("submitAdvSearchForm()");
    }

    public void checkForTheUploadedElement(String searchItem) {

        if (!driver.findElement(By.id(uiElementMapper.getElement("search.results.id"))).
                getText().contains(searchItem)) {

            throw new IllegalStateException("Search Element Does not Exists");
        }

    }

}


