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
package org.wso2.greg.integration.common.ui.page.tenant;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.wso2.greg.integration.common.ui.page.LoginPage;
import org.wso2.greg.integration.common.ui.page.util.UIElementMapper;

public class TenantHomePage {

    private static final Log log = LogFactory.getLog(LoginPage.class);
    private WebDriver driver;
    private UIElementMapper uiElementMapper;

    public TenantHomePage(WebDriver driver) throws IOException {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();
        // Check that we're on the right page.
        driver.findElement(By.id(uiElementMapper.getElement("configure.tab.id"))).click();
        driver.findElement(By.linkText(uiElementMapper.getElement("add.new.tenant.link.text"))).click();
        log.info("New Tenant add page");
        if (!driver.findElement(By.id(uiElementMapper.getElement("tenant.role.dashboard.middle.text"))).
                getText().contains("Register A New Organization")) {
            throw new IllegalStateException("This is not the correct Page");
        }
    }

    public void addNewTenant(String tenantDomain, String tenantFirstName, String tenantLastName,
                             String adminUsername, String adminPassWord, String email)
            throws InterruptedException, IOException {

        driver.findElement(By.id(uiElementMapper.getElement("tenant.domain"))).sendKeys(tenantDomain);
        driver.findElement(By.id(uiElementMapper.getElement("tenant.first.name"))).sendKeys(tenantFirstName);
        driver.findElement(By.id(uiElementMapper.getElement("tenant.last.name"))).sendKeys(tenantLastName);
        driver.findElement(By.id(uiElementMapper.getElement("tenant.admin.user.name"))).sendKeys(adminUsername);
        driver.findElement(By.id(uiElementMapper.getElement("tenant.admin.password"))).sendKeys(adminPassWord);
        driver.findElement(By.id(uiElementMapper.getElement("tenant.admin.password.repeat"))).sendKeys(adminPassWord);
        driver.findElement(By.id(uiElementMapper.getElement("tenant.admin.email.id"))).sendKeys(email);
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("addTenant(false, true)");
        Thread.sleep(7000);
        driver.findElement(By.xpath(uiElementMapper.getElement("add.new.tenant.success.button"))).click();

    }

    public LoginPage logout() throws IOException {
        driver.findElement(By.xpath(uiElementMapper.getElement("home.greg.sign.out.xpath"))).click();
        return new LoginPage(driver);
    }

}
