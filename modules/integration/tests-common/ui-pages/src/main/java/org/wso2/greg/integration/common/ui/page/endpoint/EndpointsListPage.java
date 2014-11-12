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

package org.wso2.greg.integration.common.ui.page.endpoint;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.wso2.greg.integration.common.ui.page.util.UIElementMapper;

/**
 * Endpoints list page class - contains methods to test endpoints page
 */
public class EndpointsListPage {
    private static final Log log = LogFactory.getLog(EndpointsListPage.class);
    private UIElementMapper uiElementMapper;
    private WebDriver driver;

    public EndpointsListPage(WebDriver driver) throws IOException {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();
        // Check whether we are on the correct page.
        log.info("Endpoints list page");
        if (!driver
                .findElement(
                        By.xpath(uiElementMapper.getElement("endpoints.tab.id")))
                .getText().contains("Endpoints")) {
            throw new IllegalStateException("This is not the correct Page");
        }
    }

    public void testPageLoadFail() throws IOException {
        driver.findElement(
                By.xpath(uiElementMapper.getElement("endpoints.tab.id")))
                .click();
        // test should fail if page load fails
        try {
            if (!driver
                    .findElement(
                            By.id(uiElementMapper
                                    .getElement("endpoints.page.middle")))
                    .getText().contains("Manage Endpoints")) {
                throw new IOException("Page loading failed");
            }
        } catch (NoSuchElementException e) {
            throw new IOException("Page loading failed");
        }
    }
}
