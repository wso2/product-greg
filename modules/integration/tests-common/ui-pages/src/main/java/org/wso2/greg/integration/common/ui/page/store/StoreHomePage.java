/*
*Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.greg.integration.common.ui.page.store;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.io.IOException;

/**
 * UI Page object class of Store Home page for Greg
 */

public class StoreHomePage {

    private static final Log log = LogFactory.getLog(StoreHomePage.class);
    private WebDriver driver;

    public StoreHomePage(WebDriver driver) throws IOException {
        this.driver = driver;
        // Check that we're on the right page.
        if (!driver.getCurrentUrl().contains("")) {
            throw new IllegalStateException(driver.getCurrentUrl() + ":    This is not the store home page");
        }
        log.info("Page load : store Home Page");
    }

    /**
     * Performing Logout from publisher
     * @throws java.io.IOException
     */
    public void logOut() throws IOException {
        driver.findElement(By.id("store.usermenu.id")).click();
        driver.findElement(By.cssSelector("store.logout.button.css")).click();
    }

}
