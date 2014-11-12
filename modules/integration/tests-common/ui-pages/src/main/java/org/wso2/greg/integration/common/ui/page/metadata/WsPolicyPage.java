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
import org.openqa.selenium.support.ui.Select;
import org.wso2.greg.integration.common.ui.page.LoginPage;
import org.wso2.greg.integration.common.ui.page.util.UIElementMapper;
import org.wso2.greg.integration.common.ui.page.wsPolicyList.wsPolicyListPage;

import java.io.IOException;

public class WsPolicyPage {

    private static final Log log = LogFactory.getLog(LoginPage.class);
    private WebDriver driver;
    private UIElementMapper uiElementMapper;

    public WsPolicyPage(WebDriver driver) throws IOException {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();

        driver.findElement(By.id(uiElementMapper.getElement("carbon.Main.tab"))).click();
        driver.findElement(By.linkText(uiElementMapper.getElement("wsPolicy.add.link"))).click();
        log.info("ws policy add page");

        if (!driver.findElement(By.id(uiElementMapper.getElement("wsPolicy.dashboard.middle.text"))).
                getText().contains("Policy")) {

            throw new IllegalStateException("This is not the Ws Policy  Add Page");
        }
    }

    public wsPolicyListPage uploadWsPolicyFromUrl(String wsPolicyUrl, String wsPolicyName, String version)
            throws InterruptedException, IOException {

        WebElement policyUploadField = driver.findElement(By.id(uiElementMapper.getElement("wsPolicy.add.url")));
        policyUploadField.sendKeys(wsPolicyUrl);
        WebElement wsPolicyNameField = driver.findElement(By.id(uiElementMapper.getElement("wsPolicy.add.name")));
        wsPolicyNameField.clear();
        wsPolicyNameField.sendKeys(wsPolicyName);
        WebElement versionField = driver.findElement(By.xpath("//input[@id='irversion']"));
        versionField.clear();
        versionField.sendKeys(version);
        String schemaName = wsPolicyNameField.getText();
        log.info("Printing the Schema name" + schemaName);
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("addFile();");
        log.info("successfully Saved");
        Thread.sleep(15000);
        driver.navigate().refresh();
        return new wsPolicyListPage(driver);

    }

    public wsPolicyListPage uploadWsPolicyFromFile(String wsFilePath, String wsPolicyName, String version)
            throws InterruptedException, IOException {
        driver.findElement(By.linkText(uiElementMapper.getElement("wsPolicy.add.link"))).click();

        new Select(driver.findElement(By.id("addMethodSelector"))).selectByVisibleText("Upload Policy from a file");
        WebElement serviceUploadField = driver.findElement(By.id(uiElementMapper.getElement("wsPolicy.add.file.id")));
        serviceUploadField.sendKeys(wsFilePath);
        WebElement serviceUploadNamespace = driver.findElement(By.id(uiElementMapper.getElement("wsPolicy.add.schema.name.id")));
        serviceUploadNamespace.clear();
        serviceUploadNamespace.sendKeys(wsPolicyName);
        WebElement versionField = driver.findElement(By.xpath("//input[@id='uversion']"));
        versionField.clear();
        versionField.sendKeys(version);
        JavascriptExecutor js2 = (JavascriptExecutor) driver;
        js2.executeScript("addFile();");
        log.info("successfully Saved");
        Thread.sleep(15000);
        driver.navigate().refresh();
        return new wsPolicyListPage(driver);

    }

    public LoginPage logout() throws IOException {
        driver.findElement(By.xpath(uiElementMapper.getElement("home.greg.sign.out.xpath"))).click();
        return new LoginPage(driver);
    }

}
