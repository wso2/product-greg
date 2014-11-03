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
package org.wso2.greg.integration.common.ui.page.notififcations;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Select;
import org.wso2.greg.integration.common.ui.page.LoginPage;
import org.wso2.greg.integration.common.ui.page.util.UIElementMapper;

import java.io.IOException;

public class NotificationHome {

    private static final Log log = LogFactory.getLog(LoginPage.class);
    private WebDriver driver;
    private UIElementMapper uiElementMapper;

    public NotificationHome(WebDriver driver) throws IOException {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();
        // Check that we're on the right page.
        driver.findElement(By.id(uiElementMapper.getElement("configure.tab.id"))).click();
        driver.findElement(By.linkText(uiElementMapper.getElement("notification.adding.link"))).click();
        log.info("notification Add Page");
        if (!driver.findElement(By.id(uiElementMapper.getElement("notification.dashboard.middle.text"))).
                getText().contains("Manage Notifications")) {

            throw new IllegalStateException("This is not the correct Page");
        }
    }

    public void addNotification(String event, String notification, String emailId)
            throws InterruptedException {

        driver.findElement(By.linkText(uiElementMapper.getElement("notification.add.edit.link.text"))).click();
        new Select(driver.findElement(By.id("eventList"))).selectByVisibleText(event);
        new Select(driver.findElement(By.id("notificationMethodList"))).selectByVisibleText(notification);
        driver.findElement(By.id(uiElementMapper.getElement("notification.add.email.id"))).sendKeys(emailId);
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("subscribe()");

    }

    public LoginPage logout() throws IOException {

        driver.findElement(By.xpath(uiElementMapper.getElement("home.greg.sign.out.xpath"))).click();

        return new LoginPage(driver);
    }

}
