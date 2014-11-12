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

package org.wso2.greg.integration.common.ui.page.configuretab;

import java.io.IOException;
import java.util.NoSuchElementException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.wso2.greg.integration.common.ui.page.LoginPage;
import org.wso2.greg.integration.common.ui.page.util.UIElementMapper;

public class RoleHomePage {

    private static final Log log = LogFactory.getLog(LoginPage.class);
    private WebDriver driver;
    private UIElementMapper uiElementMapper;

    public RoleHomePage(WebDriver driver) throws IOException {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();
        // Check that we're on the right page.
        driver.findElement(By.id(uiElementMapper.getElement("configure.tab.id"))).click();
        driver.findElement(By.linkText(uiElementMapper.getElement("users.and.roles.link.text"))).click();
        driver.findElement(By.linkText(uiElementMapper.getElement("roles.add.link.id"))).click();
        log.info("Roles add page");
        if (!driver.findElement(By.id(uiElementMapper.getElement("users.adn.roles.add.page.middle.text"))).
                getText().contains("Roles")) {

            throw new IllegalStateException("This is not the correct Page");
        }
    }

    public boolean checkOnUploadRole(String roleName) throws InterruptedException {

        if (!driver.findElement(By.id(uiElementMapper.getElement("users.adn.roles.add.page.middle.text"))).
                getText().contains("Roles")) {

            throw new IllegalStateException("This is not the user add Page");
        }

        log.info(roleName);
        Thread.sleep(5000);

        String roleNameOnServer = driver.findElement(By.xpath("/html/body/table/tbody/tr[2]/td[3]/table/tbody/tr[2]/td/div/div/table/tbody/tr/td")).getText();
        log.info(roleNameOnServer);
        if (roleName.equals(roleNameOnServer)) {
            log.info("Uploaded Api exists");
            return true;
        } else {
            String resourceXpath = "/html/body/table/tbody/tr[2]/td[3]/table/tbody/tr[2]/td/div/div/table/tbody/tr[";
            String resourceXpath2 = "]/td";

            for (int i = 2; i < 10; i++) {
                String roleNameOnAppServer = resourceXpath + i + resourceXpath2;

                String actualRoleName = driver.findElement(By.xpath(roleNameOnAppServer)).getText();
                log.info("val on app is -------> " + actualRoleName);
                log.info("Correct is    -------> " + roleName);

                try {

                    if (roleName.contains(actualRoleName)) {
                        log.info("newly Created Role   exists");
                        return true;
                    } else {
                        return false;
                    }
                } catch (NoSuchElementException ex) {
                    log.info("Cannot Find the newly Created ROle");
                }
            }
        }

        return false;
    }

    public void addRole(String roleName) throws InterruptedException {

        driver.findElement(By.linkText(uiElementMapper.getElement("role.add.new.user.link.id"))).click();
        driver.findElement(By.name(uiElementMapper.getElement("role.add.new.user.name.id"))).sendKeys(roleName);
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("doNext()");
        driver.findElement(By.className(uiElementMapper.getElement("role.permission.id"))).click();
        Thread.sleep(3000);
        js.executeScript("doNext()");
        driver.findElement(By.name(uiElementMapper.getElement("role.add.user.to.role.name"))).clear();
        driver.findElement(By.name(uiElementMapper.getElement("role.add.user.to.role.name"))).sendKeys("Seleniumtest");
        //This thread waits until roles list appears
        Thread.sleep(2000);
        driver.findElement(By.cssSelector(uiElementMapper.getElement("role.search.button"))).click();
        driver.findElement(By.cssSelector(uiElementMapper.getElement("role.add.ok.button.css"))).click();
    }

    public LoginPage logout() throws IOException {
        driver.findElement(By.xpath(uiElementMapper.getElement("home.greg.sign.out.xpath"))).click();
        return new LoginPage(driver);
    }
}
