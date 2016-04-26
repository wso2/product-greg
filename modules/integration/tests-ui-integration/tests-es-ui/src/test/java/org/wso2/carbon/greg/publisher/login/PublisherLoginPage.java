/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.wso2.carbon.greg.publisher.login;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.wso2.carbon.greg.publisher.utils.ESWebDriver;
import org.wso2.greg.integration.common.ui.page.util.UIElementMapper;

import java.io.IOException;

/**
 * UI Page class of Publisher Login page for Greg
 */
public class PublisherLoginPage {

    private static final Log log = LogFactory.getLog(PublisherLoginPage.class);
    private ESWebDriver driver;
    private UIElementMapper uiElementMapper;

    public PublisherLoginPage(ESWebDriver driver) {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();
        // Check that we're on the right page.
        if (!(driver.getCurrentUrl().contains(""))) {
            throw new IllegalStateException("This is not the publisher login page");
        }
    }

    /**
     * Login to Publisher. after login this page will return to Publisher home page.
     *
     * @param userName Login username
     * @param password Login password
     * @return instance of a PublisherHomePage
     * @throws IOException
     */
    public PublisherHomePage loginAs(String userName, CharSequence password) throws IOException {

        driver.findElement(By.id(uiElementMapper.getElement("publisher.login.username.id"))).sendKeys(userName);
        driver.findElement(By.id(uiElementMapper.getElement("publisher.login.password.id"))).sendKeys(password);

        driver.findElement(By.tagName(uiElementMapper.getElement("publisher.button"))).click();
        log.info("login as " + userName + " to Publisher Page");
        return new PublisherHomePage(driver);

    }
}
