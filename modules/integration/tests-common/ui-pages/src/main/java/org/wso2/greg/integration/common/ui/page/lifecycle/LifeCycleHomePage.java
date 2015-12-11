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
package org.wso2.greg.integration.common.ui.page.lifecycle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.wso2.greg.integration.common.ui.page.LoginPage;
import org.wso2.greg.integration.common.ui.page.util.UIElementMapper;

import java.io.IOException;

public class LifeCycleHomePage {

    private static final Log log = LogFactory.getLog(LoginPage.class);
    private WebDriver driver;
    private UIElementMapper uiElementMapper;

    public LifeCycleHomePage(WebDriver driver) throws IOException {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();
        // Check that we're on the right page.
        driver.findElement(By.className(uiElementMapper.getElement("life.cycle.tab.class"))).click();
        driver.findElement(By.xpath(uiElementMapper.getElement("life.cycle.tab.xpath"))).click();
        driver.findElement(By.linkText("Lifecycles")).click();

        log.info("Lifecycle adding page");

    }

    public void addNewLifeCycle(String lifeCycleName) throws InterruptedException, IOException {

        driver.findElement(By.linkText(uiElementMapper.getElement("add.new.lifecycle.link.text"))).click();
        Thread.sleep(3000);
        driver.switchTo().frame("frame_payload");
        driver.findElement(By.xpath(uiElementMapper.getElement("add.new.lifecycle.text.area"))).clear();
        driver.findElement(By.xpath(uiElementMapper.getElement("add.new.lifecycle.text.area"))).sendKeys(lifeCycleName);
        Thread.sleep(4000);
        driver.switchTo().defaultContent();
        driver.findElement(By.cssSelector(uiElementMapper.getElement("add.new.lifecycle.save.css"))).click();

    }

    public LoginPage logout() throws IOException {
        driver.findElement(By.xpath(uiElementMapper.getElement("home.greg.sign.out.xpath"))).click();
        return new LoginPage(driver);
    }

}


