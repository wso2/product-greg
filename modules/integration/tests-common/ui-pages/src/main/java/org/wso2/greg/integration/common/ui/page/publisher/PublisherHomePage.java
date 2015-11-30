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

package org.wso2.greg.integration.common.ui.page.publisher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import java.io.IOException;

/**
 * UI Page class of Publisher Home page for Greg
 */
public class PublisherHomePage {

    private static final Log log = LogFactory.getLog(PublisherHomePage.class);
    private WebDriver driver;

    public PublisherHomePage(WebDriver driver) throws IOException {
        this.driver = driver;
        // Check that we're on the right page.
        if (!driver.getCurrentUrl().contains("")) {
            throw new IllegalStateException(driver.getCurrentUrl() + ":    This is not the Publisher home page");
        }
        log.info("Page load : Publisher Home Page");
    }

    /**
     * Create a API. Resource URL Pattern is"*", resource name is "default" and resource http method is "GET".
     *
     * @param restServiceName    name of the Rest Service
     *
     */
    public void createRestService(String restServiceName, String context,String version)
            throws IOException, InterruptedException {

        Thread.sleep(6000);

        driver.findElement(By.xpath("//div[3]/div/div/div")).click();
        driver.findElement(By.xpath("//div[2]/div/div/div[2]/span/a[2]/span/i[2]")).click();
        driver.findElement(By.id("rest-manual-radio")).click();
        driver.findElement(By.linkText("Continue")).click();

        //TODO - WHICH IS WRONG !!!!!!
        //assertTrue(driver.getTitle().equals("Asset | WSO2 Enterprise Store Publisher"));

        driver.findElement(By.id("overview_name")).click();
        driver.findElement(By.id("overview_name")).clear();
        driver.findElement(By.id("overview_name")).sendKeys(restServiceName);
        driver.findElement(By.id("overview_context")).clear();
        driver.findElement(By.id("overview_context")).sendKeys(context);
        driver.findElement(By.id("overview_version")).clear();
        driver.findElement(By.id("overview_version")).sendKeys(version);
        driver.findElement(By.id("btn-create-asset")).click();

        Thread.sleep(6000);
    }

    /**
     * Logout from publisher
     */
    public void logOut() throws IOException {
        driver.findElement(By.id("publisher.usermenu.id")).click();
        driver.findElement(By.cssSelector("publisher.logout.button.css")).click();
    }


}
