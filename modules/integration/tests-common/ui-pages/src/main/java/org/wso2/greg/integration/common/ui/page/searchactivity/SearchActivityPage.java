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
package org.wso2.greg.integration.common.ui.page.searchactivity;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.wso2.greg.integration.common.ui.page.resourcebrowse.ResourceBrowsePage;
import org.wso2.greg.integration.common.ui.page.util.UIElementMapper;

import java.io.IOException;

public class SearchActivityPage {

    private static final org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog(ResourceBrowsePage.class);
    private WebDriver driver;
    private UIElementMapper uiElementMapper;

    public SearchActivityPage(WebDriver driver) throws IOException {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();
        driver.findElement(By.linkText(uiElementMapper.getElement("search.activity.link"))).click();

        // Check that we're on the right page.
        if (!(driver.getCurrentUrl().contains("activities"))) {
            // Alternatively, we could navigate to the login page, perhaps logging out first
            throw new IllegalStateException("This is not the Search Activity page");
        }
    }

    public void searchElement() throws InterruptedException {

        driver.findElement(By.id(uiElementMapper.getElement("search.activity.id"))).clear();
        driver.findElement(By.id(uiElementMapper.getElement("search.activity.id"))).sendKeys("testuser2");
        driver.findElement(By.id(uiElementMapper.getElement("search.activity.name.id"))).sendKeys("/Capp_1.0.0.carTestFile");
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("submitActivityForm(1) ");

    }

    public void verifySearchElement(String searchElement) throws InterruptedException {
        log.info("---------------------->>>>> " + searchElement);
        Thread.sleep(5000);

        if (!driver.findElement(By.id(uiElementMapper.getElement("search.activity.exists.id"))).
                getText().contains(searchElement)) {

            throw new IllegalStateException("Search Element Does not Exists");
        }

    }

}
