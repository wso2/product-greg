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

package org.wso2.greg.integration.common.ui.page.carbon;

import java.io.IOException;
import java.util.NoSuchElementException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.wso2.greg.integration.common.ui.page.LoginPage;
import org.wso2.greg.integration.common.ui.page.util.UIElementMapper;

public class CappHome {

    private static final Log log = LogFactory.getLog(LoginPage.class);
    private WebDriver driver;
    private UIElementMapper uiElementMapper;

    public CappHome(WebDriver driver) throws IOException {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();
        // Check that we're on the right page.
        driver.findElement(By.id(uiElementMapper.getElement("carbon.Main.tab"))).click();
        driver.findElement(By.id(uiElementMapper.getElement("carbon.Region1.tab"))).click();
        driver.findElement(By.linkText(uiElementMapper.getElement("carbon.add.href"))).click();

        log.info("in the carbon element Add Page");
        if (!driver.findElement(By.id(uiElementMapper.getElement("carbon.dashboard.middle.text"))).
                getText().contains("Add Carbon Applications")) {

            throw new IllegalStateException("This is not the carbon Element adding page######################");
        }
    }

    public CappListPage UploadCarbonItem(String carbonElement) throws InterruptedException, IOException {
        WebElement carbonUploadField = driver.findElement(By.name(uiElementMapper.getElement("carbon.file.upload.field")));
        carbonUploadField.sendKeys(carbonElement);
        Thread.sleep(5000);

        driver.findElement(By.name(uiElementMapper.getElement("carbon.upload.button"))).click();
        Thread.sleep(5000);
        driver.navigate().refresh();
        if (!driver.findElement(By.id(uiElementMapper.getElement("carbon.upload.successful.message"))).
                getText().contains("successfully")) {

            throw new NoSuchElementException();
        }

        log.info("Successfully Uploaded");

        driver.findElement(By.className(uiElementMapper.getElement("carbon.upload.successful.button"))).click();
        log.info("Ready to sign out");

        return new CappListPage(driver);
    }

    public LoginPage logout() throws IOException {
        driver.findElement(By.linkText(uiElementMapper.getElement("home.greg.sign.out.xpath"))).click();
        return new LoginPage(driver);
    }

}


