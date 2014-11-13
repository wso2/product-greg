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
import org.wso2.greg.integration.common.ui.page.wsdllist.WsdlListPage;

import java.io.IOException;

public class WSDLPage {

    private static final Log log = LogFactory.getLog(LoginPage.class);
    private WebDriver driver;
    private UIElementMapper uiElementMapper;

    public WSDLPage(WebDriver driver) throws IOException {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();

        driver.findElement(By.linkText(uiElementMapper.getElement("wsdl.add.link"))).click();
        if (!driver.findElement(By.id(uiElementMapper.getElement("wsdl.dashboard.middle.text"))).
                getText().contains("WSDL")) {
            throw new IllegalStateException("This is not the Wsdl Add Page");
        }
    }

    public WsdlListPage uploadWsdlFromUrl(String wsdlUrl, String WsdlName, String version)
            throws InterruptedException, IOException {
        // Check that we're on the right page.
        driver.findElement(By.linkText(uiElementMapper.getElement("wsdl.add.link"))).click();

        log.info("Wsdl Add Page");

        WebElement serviceUploadField = driver.findElement(By.id(uiElementMapper.getElement("wsdl.add.url")));
        serviceUploadField.sendKeys(wsdlUrl);
        WebElement serviceUploadNamespace = driver.findElement(By.id(uiElementMapper.getElement("wsdl.add.name")));
        serviceUploadNamespace.clear();
        serviceUploadNamespace.sendKeys(WsdlName);
        WebElement versionField = driver.findElement(By.xpath(uiElementMapper.getElement("wsdl.irversion")));
        versionField.clear();
        versionField.sendKeys(version);
        String wsdlName = serviceUploadField.getText();
        log.info("Printing the wsdl name " + wsdlName);
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("addFile()");
        log.info("successfully Saved");
        Thread.sleep(15000);
        driver.navigate().refresh();
        return new WsdlListPage(driver);

    }

    public WsdlListPage uploadWsdlFromFile(String wsdlPath, String WsdlName, String version)
            throws InterruptedException, IOException {

        driver.findElement(By.linkText(uiElementMapper.getElement("wsdl.add.link"))).click();
        new Select(driver.findElement(By.id("addMethodSelector"))).selectByVisibleText("Upload WSDL from a file");
        WebElement serviceUploadField = driver.findElement(By.id(uiElementMapper.getElement("wsdl.add.file.id")));
        serviceUploadField.sendKeys(wsdlPath);
        WebElement serviceUploadNamespace = driver.findElement(By.id(uiElementMapper.getElement("wsdl.add.file.name.id")));
        serviceUploadNamespace.clear();
        serviceUploadNamespace.sendKeys(WsdlName);
        WebElement versionField = driver.findElement(By.xpath(uiElementMapper.getElement("wsdl.uversion")));
        versionField.clear();
        versionField.sendKeys(version);
        JavascriptExecutor js2 = (JavascriptExecutor) driver;
        js2.executeScript("addFile()");
        log.info("successfully Saved");
        Thread.sleep(15000);
        driver.navigate().refresh();
        return new WsdlListPage(driver);

    }

    public LoginPage logout() throws IOException {
        driver.findElement(By.xpath(uiElementMapper.getElement("home.greg.sign.out.xpath"))).click();
        return new LoginPage(driver);
    }

}
