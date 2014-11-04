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
package org.wso2.greg.integration.common.ui.page.extensionlist;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.wso2.greg.integration.common.ui.page.util.UIElementMapper;

import java.io.IOException;
import java.util.NoSuchElementException;

public class ExtensionListPage {

    private static final Log log = LogFactory.getLog(ExtensionListPage.class);
    private WebDriver driver;

    public ExtensionListPage(WebDriver driver) throws IOException {
        this.driver = driver;
        UIElementMapper uiElementMapper = UIElementMapper.getInstance();
        // Check that we're on the right page.
        if (!driver.findElement(By.id(uiElementMapper.getElement("extension.list.page.dashboard.middle.text"))).
                getText().contains("Extension List")) {

            throw new IllegalStateException("This is not the Extension List Page");
        }
    }

    public boolean checkOnUploadedExtension(String extensionName) throws InterruptedException {

        String extensionNameOnServer = driver.findElement(By.xpath("/html/body/table/tbody/tr[2]/td[3]" +
                                                                   "/table/tbody/tr[2]/td/div/div/form/" +
                                                                   "table/tbody/tr/td")).getText();

        log.info(extensionName);
        if (extensionName.equals(extensionNameOnServer)) {
            log.info("newly Created extension exists");
            return true;
        } else {
            String resourceXpath = "/html/body/table/tbody/tr[2]/td[3]/table/tbody/tr[2]/td/div/div/form/table/tbody/tr[";
            String resourceXpath2 = "]/td";

            for (int i = 2; i < 10; i++) {
                String extensionNameOnAppServer = resourceXpath + i + resourceXpath2;

                String actualUsername = driver.findElement(By.xpath(extensionNameOnAppServer)).getText();
                log.info("val on app is -------> " + actualUsername);
                log.info("Correct is    -------> " + extensionName);

                try {

                    if (extensionName.contains(actualUsername)) {
                        log.info("newly Created extension   exists");
                        return true;
                    }  else {
                        return false;
                    }

                } catch (NoSuchElementException ex) {
                    log.info("Cannot Find the newly Created organization");

                }
            }
        }
        return false;
    }
}
