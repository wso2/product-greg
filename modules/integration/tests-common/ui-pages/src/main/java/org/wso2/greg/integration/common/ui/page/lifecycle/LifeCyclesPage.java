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
package org.wso2.greg.integration.common.ui.page.lifecycle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.wso2.greg.integration.common.ui.page.LoginPage;
import org.wso2.greg.integration.common.ui.page.util.UIElementMapper;

import java.io.IOException;
import java.util.NoSuchElementException;

public class LifeCyclesPage {

    private static final Log log = LogFactory.getLog(LoginPage.class);
    private WebDriver driver;

    public LifeCyclesPage(WebDriver driver) throws IOException {
        this.driver = driver;
        UIElementMapper uiElementMapper = UIElementMapper.getInstance();
        // Check that we're on the right page.
        driver.findElement(By.id(uiElementMapper.getElement("life.cycle.tab.id"))).click();

        driver.findElement(By.linkText(uiElementMapper.getElement("life.cycle.add.link"))).click();

        log.info("Lifecycle adding page");
        if (!driver.findElement(By.id(uiElementMapper.getElement("add.new.lifecycle.dashboard.middle.text"))).
                getText().contains("Lifecycles")) {

            throw new IllegalStateException("This is not the correct Page");
        }
    }

    public boolean checkOnUploadedLifeCycle(String lifeCycleName) throws InterruptedException {

        Thread.sleep(10000);
        String lifeCycleNameOnServer = driver.findElement(By.xpath("/html/body/table/tbody/tr[2]/" +
                                                                   "td[3]/table/tbody/tr[2]/td/div/div/table/tbody/tr/td")).getText();

        log.info(lifeCycleName);

        if (lifeCycleName.equals(lifeCycleNameOnServer)) {
            log.info("newly Created lifecycle exists");
            return true;

        } else {
            String resourceXpath = "/html/body/table/tbody/tr[2]/td[3]/table/tbody/tr[2]/td/div/div/table/tbody/tr[";
            String resourceXpath2 = "]/td";

            for (int i = 2; i < 10; i++) {
                String lifeCycleNameOnAppServer = resourceXpath + i + resourceXpath2;
                String actualUsername = driver.findElement(By.xpath(lifeCycleNameOnAppServer)).getText();
                log.info("val on app is -------> " + actualUsername);
                log.info("Correct is    -------> " + lifeCycleName);
                try {

                    if (lifeCycleName.contains(actualUsername)) {

                        log.info("newly Created lifecycle   exists");
                        return true;

                    }   else {
                        return false;
                    }

                } catch (NoSuchElementException ex) {
                  log.info("Cannot Find the newly Created lifecycle");


                }

            }

        }

        return false;
    }

}
