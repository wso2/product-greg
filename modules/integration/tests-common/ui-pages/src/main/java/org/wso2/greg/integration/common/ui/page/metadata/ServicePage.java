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
package org.wso2.greg.integration.common.ui.page.metadata;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.wso2.greg.integration.common.ui.page.LoginPage;
import org.wso2.greg.integration.common.ui.page.servlistlist.ServiceListPage;
import org.wso2.greg.integration.common.ui.page.util.UIElementMapper;

import java.io.IOException;

public class ServicePage {

    private static final Log log = LogFactory.getLog(LoginPage.class);
    private WebDriver driver;
    private UIElementMapper uiElementMapper;

    public ServicePage(WebDriver driver) throws IOException {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();
        // Check that we're on the right page.
        driver.findElement(By.id(uiElementMapper.getElement("carbon.Main.tab"))).click();
        driver.findElement(By.linkText(uiElementMapper.getElement("service.add.link"))).click();
        log.info("we are in the correct  Page");
        if (!driver.findElement(By.id(uiElementMapper.getElement("service.dashboard.middle.text"))).
                getText().contains("Service")) {
            throw new IllegalStateException("not in the correct Page");
        }
    }

    public ServiceListPage uploadService(String name, String nameSpace, String version) throws InterruptedException
            , IOException {
        WebElement serviceUploadField = driver.findElement(By.id(uiElementMapper.getElement("service.add.name.id")));
        serviceUploadField.sendKeys(name);
        WebElement serviceUploadNamespace = driver.findElement(By.id(uiElementMapper.getElement("service.add.namespace.id")));
        serviceUploadNamespace.sendKeys(nameSpace);
        WebElement versionField = driver.findElement(By.xpath("//input[@id='id_Overview_Version']"));
        versionField.clear();
        versionField.sendKeys(version);
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("addEditArtifact()");
        log.info("successfully Saved");
        driver.findElement(By.linkText(uiElementMapper.getElement("service.check.save.service"))).click();
        Thread.sleep(15000);
        driver.navigate().refresh();
        return new ServiceListPage(driver);

    }

    public LoginPage logout() throws IOException {
        driver.findElement(By.linkText(uiElementMapper.getElement("home.greg.sign.out.xpath"))).click();
        return new LoginPage(driver);
    }

}
