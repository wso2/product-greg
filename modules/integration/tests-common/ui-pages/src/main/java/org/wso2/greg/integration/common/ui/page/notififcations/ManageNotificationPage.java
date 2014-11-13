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
package org.wso2.greg.integration.common.ui.page.notififcations;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Select;
import org.wso2.greg.integration.common.ui.page.LoginPage;
import org.wso2.greg.integration.common.ui.page.util.UIElementMapper;

import java.io.IOException;
import java.util.NoSuchElementException;

public class ManageNotificationPage {

    private static final Log log = LogFactory.getLog(LoginPage.class);
    private WebDriver driver;
    private UIElementMapper uiElementMapper;

    public ManageNotificationPage(WebDriver driver) throws IOException {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();
        // Check that we're on the right page.
        driver.findElement(By.id(uiElementMapper.getElement("configure.tab.id"))).click();
        driver.findElement(By.linkText(uiElementMapper.getElement("notification.adding.link"))).click();
        log.info("notification Add Page");
        if (!driver.findElement(By.id(uiElementMapper.getElement("notification.dashboard.middle.text"))).
                getText().contains("Manage Notifications")) {
            throw new IllegalStateException("This is not the correct Page");
        }
    }

    public boolean checkOnUploadedNotification(String notificationSubscribe)
            throws InterruptedException {

        log.info("---------------------------->>>> " + notificationSubscribe);
        Thread.sleep(5000);
        driver.navigate().refresh();
        String notificationNameOnServer = driver.findElement(By.xpath("/html/body/table/tbody/tr[2]/" +
                                                                      "td[3]/table/tbody/tr[2]/td/div/div/table/tbody/tr/td[5]")).getText();
        log.info(notificationNameOnServer);
        if (notificationSubscribe.equals(notificationNameOnServer)) {
            log.info("newly Created notification exists");
            return true;

        } else {
            String resourceXpath = "/html/body/table/tbody/tr[2]/td[3]/table/tbody/tr[2]/td/div/div/table/tbody/tr[";
            String resourceXpath2 = "]/td[5]";

            for (int i = 2; i < 10; i++) {
                String notificationNameOnAppServer = resourceXpath + i + resourceXpath2;
                String actualUsername = driver.findElement(By.xpath(notificationNameOnAppServer)).getText();
                log.info("val on app is -------> " + actualUsername);
                log.info("Correct is    -------> " + notificationSubscribe);
                try {

                    if (notificationSubscribe.contains(actualUsername)) {
                        log.info("newly Created notification   exists");
                        return true;

                    }  else  {
                        return false;
                    }


                } catch (NoSuchElementException ex) {
                    log.info("Cannot Find the newly Created notification");


                }

            }

        }

        return false;
    }

    public boolean testHierarchicalSubscriptionMethodStatePersistance(String expectedValue){

        driver.findElement(By.xpath("//*[@id=\"subscriptionsTable\"]/tbody/tr[1]/td[6]/a[1]")).click();
        String value = new Select(driver.findElement(By.id(uiElementMapper.getElement("registry.subscription.hsmethod" +
                ".id")))).getFirstSelectedOption().getText();
        return  expectedValue.equals(value);

    };

}
