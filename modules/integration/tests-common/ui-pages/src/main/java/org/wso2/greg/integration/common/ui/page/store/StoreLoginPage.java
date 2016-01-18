/*
 *
 * Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * /
 */
package org.wso2.greg.integration.common.ui.page.store;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.wso2.greg.integration.common.ui.page.util.UIElementMapper;

import java.io.IOException;

/**
 * UI Page class of Store Login page for Greg
 */
public class StoreLoginPage {

    private static final Log log = LogFactory.getLog(StoreLoginPage.class);
    private WebDriver driver;
    private UIElementMapper uiElementMapper;

    public StoreLoginPage(WebDriver driver) {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();
        // Check that we're on the right page.
        if (!(driver.getCurrentUrl().contains(""))) {
            throw new IllegalStateException("This is not the store login page");
        }
    }

    /**
     * Login to Store. after login this page will return to Store home page.
     *
     * @param userName Login username
     * @param password Login password
     * @return instance of a StoreHomePage
     * @throws Exception
     */
    public StoreHomePage loginAs(String userName, CharSequence password)
            throws IOException {

        driver.findElement(By.id(uiElementMapper.getElement("store.login.username.id"))).sendKeys(userName);
        driver.findElement(By.id(uiElementMapper.getElement("store.login.password.id"))).sendKeys(password);

        driver.findElement(By.tagName("button")).click();
        log.info("login as " + userName + " to Store Page");
        return new StoreHomePage(driver);

    }
}
