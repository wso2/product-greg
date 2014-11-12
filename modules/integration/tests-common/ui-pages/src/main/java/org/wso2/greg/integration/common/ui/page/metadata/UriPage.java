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
import org.wso2.greg.integration.common.ui.page.resourcebrowse.ResourceBrowsePage;
import org.wso2.greg.integration.common.ui.page.util.UIElementMapper;

import java.io.IOException;

public class UriPage {

    private static final Log log = LogFactory.getLog(LoginPage.class);
    private WebDriver driver;
    private UIElementMapper uiElementMapper;

    public UriPage(WebDriver driver) throws IOException {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();
        // Check that we're on the right page.
        driver.findElement(By.id(uiElementMapper.getElement("carbon.Main.tab"))).click();
        driver.findElement(By.linkText(uiElementMapper.getElement("uri.add.link"))).click();

        log.info("we are in the correct  Page");
        if (!driver.findElement(By.id(uiElementMapper.getElement("uri.dashboard.middle.text"))).
                getText().contains("URI")) {

            throw new IllegalStateException("not in the correct Page");
        }
    }

    public ResourceBrowsePage uploadGenericUri(String uriAddress, String name)
            throws InterruptedException, IOException {

        WebElement serviceUploadField = driver.findElement(By.id(uiElementMapper.getElement("uri.add.uri")));
        serviceUploadField.sendKeys(uriAddress);
        WebElement serviceUploadNamespace = driver.findElement(By.id(uiElementMapper.getElement("uri.add.uri.name")));
        serviceUploadNamespace.sendKeys(name);
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("addEditArtifact()");
        log.info("successfully Saved");
        Thread.sleep(15000);
        driver.navigate().refresh();
        return new ResourceBrowsePage(driver);

    }

    public ResourceBrowsePage uploadWsdlUri(String uriAddress, String name)
            throws InterruptedException, IOException {

        driver.findElement(By.linkText(uiElementMapper.getElement("uri.add.link"))).click();
        new Select(driver.findElement(By.id("id_Overview_Type"))).selectByVisibleText("WSDL");
        WebElement serviceUploadField = driver.findElement(By.id(uiElementMapper.getElement("uri.add.uri")));
        serviceUploadField.sendKeys(uriAddress);
        WebElement serviceUploadNamespace = driver.findElement(By.id(uiElementMapper.getElement("uri.add.uri.name")));
        serviceUploadNamespace.sendKeys(name);
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("addEditArtifact()");
        log.info("successfully Saved");
        Thread.sleep(10000);
        driver.navigate().refresh();
        return new ResourceBrowsePage(driver);

    }

    public ResourceBrowsePage uploadXsdUri(String uriAddress, String name)
            throws InterruptedException, IOException {

        log.info("this is the failing Uri name " + name);
        driver.findElement(By.linkText(uiElementMapper.getElement("uri.add.link"))).click();
        new Select(driver.findElement(By.id("id_Overview_Type"))).selectByVisibleText("XSD");
        WebElement serviceUploadField = driver.findElement(By.id(uiElementMapper.getElement("uri.add.uri")));
        serviceUploadField.sendKeys(uriAddress);
        WebElement serviceUploadNamespace = driver.findElement(By.id(uiElementMapper.getElement("uri.add.uri.name")));
        serviceUploadNamespace.sendKeys(name);
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("addEditArtifact()");
        log.info("successfully Saved");
        Thread.sleep(15000);
        driver.navigate().refresh();
        return new ResourceBrowsePage(driver);

    }

    public ResourceBrowsePage uploadPolicyUri(String uriAddress, String name)
            throws InterruptedException, IOException {

        driver.findElement(By.linkText(uiElementMapper.getElement("uri.add.link"))).click();
        new Select(driver.findElement(By.id("id_Overview_Type"))).selectByVisibleText("Policy");
        WebElement serviceUploadField = driver.findElement(By.id(uiElementMapper.getElement("uri.add.uri")));
        serviceUploadField.sendKeys(uriAddress);
        WebElement serviceUploadNamespace = driver.findElement(By.id(uiElementMapper.getElement("uri.add.uri.name")));
        serviceUploadNamespace.sendKeys(name);
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("addEditArtifact()");
        log.info("successfully Saved");
        Thread.sleep(10000);
        driver.navigate().refresh();
        return new ResourceBrowsePage(driver);

    }

    public LoginPage logout() throws IOException {
        driver.findElement(By.xpath(uiElementMapper.getElement("home.greg.sign.out.xpath"))).click();
        return new LoginPage(driver);
    }

}
