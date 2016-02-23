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
package org.wso2.carbon.greg.publisher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.wso2.carbon.greg.publisher.utils.ESWebDriver;
import org.wso2.greg.integration.common.ui.page.util.UIElementMapper;

import java.io.IOException;

/**
 * UI Page object class of Publisher Home page for Greg
 */
public class PublisherHomePage {

    private static final Log log = LogFactory.getLog(PublisherHomePage.class);
    private ESWebDriver driver;
    private UIElementMapper uiElementMapper;

    public PublisherHomePage(ESWebDriver driver) throws IOException {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();
        // Check that we're on the right page.
        if (!driver.getCurrentUrl().contains("")) {
            throw new IllegalStateException(driver.getCurrentUrl() + ":    This is not the Publisher home page");
        }
        log.info("Page load : Publisher Home Page");
    }

    /**
     * Performing Logout from publisher
     *
     * @throws IOException
     */
    public void logOut() throws IOException {
        driver.findElement(By.id(uiElementMapper.getElement("publisher.auth.button"))).click();
        driver.findElement(By.linkText(uiElementMapper.getElement("publisher.signout"))).click();
    }
}
