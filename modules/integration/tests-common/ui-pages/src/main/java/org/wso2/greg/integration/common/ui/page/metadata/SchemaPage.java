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
import org.wso2.greg.integration.common.ui.page.schemalist.SchemaListPage;
import org.wso2.greg.integration.common.ui.page.util.UIElementMapper;

import java.io.IOException;

public class SchemaPage {

    private static final Log log = LogFactory.getLog(LoginPage.class);
    private WebDriver driver;
    private UIElementMapper uiElementMapper;

    public SchemaPage(WebDriver driver) throws IOException {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();
        // Check that we're on the right page.
        driver.findElement(By.linkText(uiElementMapper.getElement("schema.add.link"))).click();

        log.info("Schema Add Page");
        if (!driver.findElement(By.id(uiElementMapper.getElement("schema.dashboard.middle.text"))).
                getText().contains("Schema")) {

            throw new IllegalStateException("This is not the schema Add Page");
        }
    }

    public SchemaListPage uploadSchemaFromUrl(String SchemaUrl, String SchemaName, String version)
            throws InterruptedException, IOException {

        WebElement serviceUploadField = driver.findElement(By.id(uiElementMapper.getElement("schema.add.url")));
        serviceUploadField.sendKeys(SchemaUrl);
        WebElement serviceUploadNamespace = driver.findElement(By.id(uiElementMapper.getElement("schema.add.name")));
        serviceUploadNamespace.clear();
        serviceUploadNamespace.sendKeys(SchemaName);
        WebElement versionField = driver.findElement(By.xpath("//input[@id='irversion']"));
        versionField.clear();
        versionField.sendKeys(version);
        String schemaName = serviceUploadField.getText();
        log.info("Printing the Schema name" + schemaName);
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("addFile()");
        log.info("successfully Saved");
        Thread.sleep(15000);
        driver.navigate().refresh();
        return new SchemaListPage(driver);

    }

    public SchemaListPage uploadSchemaFromFile(String schemaPath, String schemaName, String version)
            throws InterruptedException, IOException {

        driver.findElement(By.linkText(uiElementMapper.getElement("schema.add.link"))).click();
        new Select(driver.findElement(By.id("addMethodSelector"))).selectByVisibleText("Upload Schema from a file");
        WebElement serviceUploadField = driver.findElement(By.id(uiElementMapper.getElement("wsdl.add.file.id")));
        serviceUploadField.sendKeys(schemaPath);
        WebElement serviceUploadNamespace = driver.findElement(By.id(uiElementMapper.getElement("schema.add.schema.name.id")));
        serviceUploadNamespace.clear();
        serviceUploadNamespace.sendKeys(schemaName);
        WebElement versionField = driver.findElement(By.xpath("//input[@id='uversion']"));
        versionField.clear();
        versionField.sendKeys(version);
        JavascriptExecutor js2 = (JavascriptExecutor) driver;
        js2.executeScript("addFile()");
        log.info("successfully Saved");
        Thread.sleep(15000);
        driver.navigate().refresh();
        return new SchemaListPage(driver);

    }

    public LoginPage logout() throws IOException {
        driver.findElement(By.xpath(uiElementMapper.getElement("home.greg.sign.out.xpath"))).click();
        return new LoginPage(driver);
    }

}
