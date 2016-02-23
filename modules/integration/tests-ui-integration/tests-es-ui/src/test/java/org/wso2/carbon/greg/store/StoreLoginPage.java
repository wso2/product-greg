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
import org.wso2.carbon.greg.store.exceptions.StoreTestException;
import org.wso2.greg.integration.common.ui.page.util.UIElementMapper;

/**
 * UI object model for Login page.
 */
public class StoreLoginPage {

    private WebDriver driver;
    private UIElementMapper uiElementMapper;

    public StoreLoginPage(WebDriver driver) {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();
        driver.findElement(By.id(uiElementMapper.getElement("store.login.form.id")));
    }

    /**
     * Performs login action given the user name and password.
     *
     * @param username username of the tenant
     * @param password password of the tenant
     * @return StoreHomePage object model
     * @throws StoreTestException Throws if couldn't return to store home page.
     */
    public StoreHomePage Login(String username, String password) throws StoreTestException {
        driver.findElement(By.id(uiElementMapper.getElement("publisher.login.username.id"))).clear();
        driver.findElement(By.id(uiElementMapper.getElement("publisher.login.password.id"))).clear();
        driver.findElement(By.id(uiElementMapper.getElement("publisher.login.username.id"))).sendKeys(username);
        driver.findElement(By.id(uiElementMapper.getElement("publisher.login.password.id"))).sendKeys(password);
        driver.findElement(By.xpath(uiElementMapper.getElement("store.login.submit.xpath"))).click();
        return new StoreHomePage(driver);
    }
}
