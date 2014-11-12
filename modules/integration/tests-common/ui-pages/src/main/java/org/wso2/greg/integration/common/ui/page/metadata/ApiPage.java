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
import org.wso2.greg.integration.common.ui.page.resourcebrowse.ResourceBrowsePage;
import org.wso2.greg.integration.common.ui.page.util.UIElementMapper;

import java.io.IOException;

public class ApiPage {

    private static final Log log = LogFactory.getLog(LoginPage.class);
    private WebDriver driver;
    private UIElementMapper uiElementMapper;

    public ApiPage(WebDriver driver) throws IOException {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();
        // Check that we're on the right page.
        driver.findElement(By.id(uiElementMapper.getElement("carbon.Main.tab"))).click();
        driver.findElement(By.linkText(uiElementMapper.getElement("api.add.link"))).click();

        log.info("API Add Page");
        if (!driver.findElement(By.id(uiElementMapper.getElement("api.dashboard.middle.text"))).
                getText().contains("API")) {

            throw new IllegalStateException("This is not the API  Add Page");
        }
    }

    public ResourceBrowsePage uploadApi(String provider, String name, String context,
                                        String version)
            throws InterruptedException, IOException {

        WebElement apiProvider = driver.findElement(By.id(uiElementMapper.getElement("api.provider.id")));
        apiProvider.sendKeys(provider);

        WebElement apiName = driver.findElement(By.id(uiElementMapper.getElement("api.name.id")));
        apiName.sendKeys(name);

        WebElement apiContext = driver.findElement(By.id(uiElementMapper.getElement("api.context.id")));
        apiContext.sendKeys(context);

        WebElement apiVersion = driver.findElement(By.id(uiElementMapper.getElement("api.version.id")));
        apiVersion.sendKeys(version);

        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("addEditArtifact()");
        log.info("successfully Saved");
        Thread.sleep(15000);
        driver.navigate().refresh();
        return new ResourceBrowsePage(driver);

    }

    public LoginPage logout() throws IOException {
        driver.findElement(By.linkText(uiElementMapper.getElement("home.greg.sign.out.xpath"))).click();
        return new LoginPage(driver);
    }

}
