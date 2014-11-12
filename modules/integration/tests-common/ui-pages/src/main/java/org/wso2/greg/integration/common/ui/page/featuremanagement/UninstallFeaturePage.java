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
package org.wso2.greg.integration.common.ui.page.featuremanagement;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.wso2.greg.integration.common.ui.page.LoginPage;
import org.wso2.greg.integration.common.ui.page.util.UIElementMapper;

// this could be executed when needed to uninstall a feature

public class UninstallFeaturePage {

    private static final Log log = LogFactory.getLog(LoginPage.class);
    private WebDriver driver;
    private UIElementMapper uiElementMapper;

    public UninstallFeaturePage(WebDriver driver) throws IOException {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();
        // Check that we're on the right page.
        driver.findElement(By.id(uiElementMapper.getElement("configure.tab.id"))).click();
        driver.findElement(By.linkText(uiElementMapper.getElement("features.add.link"))).click();
        driver.findElement(By.linkText(uiElementMapper.getElement("installed.features.tab.linkText"))).click();
        log.info("API Add Page");
        if (!driver.findElement(By.id(uiElementMapper.getElement("repositories.dashboard.text"))).
                getText().contains("Feature")) {
            throw new IllegalStateException("This is not the correct Page");
        }
    }

    public void checkOnUnInstalledSuccess(String repositoryName) throws InterruptedException {
        driver.findElement(By.id(uiElementMapper.getElement("carbon.Main.tab    "))).click();
        driver.findElement(By.linkText(uiElementMapper.getElement("Is.server.shutdown.link.text"))).click();
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("restartServer();return false");
        //cannot close the popup window coding is hold due to this server restart pop up
    }

    public void revertTheUninstall() {
        // Check that we're on the right page.
        driver.findElement(By.id(uiElementMapper.getElement("configure.tab.id"))).click();
        driver.findElement(By.linkText(uiElementMapper.getElement("features.add.link"))).click();
        driver.findElement(By.linkText(uiElementMapper.getElement("feature.revert.tab"))).click();

    }

    public void uninstallFeature(String uninstallApplicationName) {

        driver.findElement(By.id(uiElementMapper.getElement("features.filter.id"))).sendKeys(uninstallApplicationName);
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("searchInstalledFeatures(); return false;");
        driver.findElement(By.name(uiElementMapper.getElement("feature.checkbox.click.name"))).click();
        js.executeScript("doUninstall();return false;");
        driver.findElement(By.id(uiElementMapper.getElement("feature.uninstall.next.button.id"))).click();
        driver.findElement(By.id(uiElementMapper.getElement("feature.uninstall.finish.button.id"))).click();

    }

    public LoginPage logout() throws IOException {
        driver.findElement(By.xpath(uiElementMapper.getElement("home.greg.sign.out.xpath"))).click();
        return new LoginPage(driver);
    }

}
