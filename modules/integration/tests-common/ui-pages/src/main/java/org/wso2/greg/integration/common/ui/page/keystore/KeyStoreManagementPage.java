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
package org.wso2.greg.integration.common.ui.page.keystore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.wso2.greg.integration.common.ui.page.LoginPage;
import org.wso2.greg.integration.common.ui.page.util.UIElementMapper;

import java.io.IOException;
import java.util.NoSuchElementException;

public class KeyStoreManagementPage {
    private static final Log log = LogFactory.getLog(LoginPage.class);
    private WebDriver driver;

    public KeyStoreManagementPage(WebDriver driver) throws IOException {
        this.driver = driver;
        UIElementMapper uiElementMapper = UIElementMapper.getInstance();
        // Check that we're on the right page.
        driver.findElement(By.id(uiElementMapper.getElement("configure.tab.id"))).click();
        driver.findElement(By.linkText(uiElementMapper.getElement("key.store.add.link"))).click();
        log.info("key store add page");
        if (!driver.findElement(By.id(uiElementMapper.getElement("key.store.dashboard.middle.text"))).
                getText().contains("KeyStore Management")) {
            throw new IllegalStateException("This is not the correct Page");
        }
    }

    public boolean checkOnUploadedKeyStore(String keyStoreName) throws InterruptedException {
        log.info("---------------------------->>>> " + keyStoreName);
        Thread.sleep(25000);

        String kerStoreNameOnServer = driver.findElement(By.xpath("/html/body/table/tbody/tr[2]/td[3]" +
                                                                  "/table/tbody/tr[2]/td/div/div/table/tbody/tr/td")).getText();
        log.info(kerStoreNameOnServer);
        if (keyStoreName.equals(kerStoreNameOnServer)) {
            log.info("Uploaded KeyStore exists");
            return true;

        } else {
            String resourceXpath = "/html/body/table/tbody/tr[2]/td[3]/table/tbody/tr[2]/td/div/div/table/tbody/tr[";
            String resourceXpath2 = "]/td";

            for (int i = 2; i < 10; i++) {
                String keyStoreNameNameOnAppServer = resourceXpath + i + resourceXpath2;

                String actualUsername = driver.findElement(By.xpath(keyStoreNameNameOnAppServer)).getText();

                log.info("val on app is -------> " + actualUsername);

                log.info("Correct is    -------> " + keyStoreName);

                try {

                    if (keyStoreName.contains(actualUsername)) {
                        log.info("newly Created keyStore   exists");
                        return true;

                    } else {
                        return false ;
                    }

                } catch (NoSuchElementException ex) {
                    log.info("Cannot Find the newly Created keryStore");

                }
            }

        }
        return false;
    }

}
