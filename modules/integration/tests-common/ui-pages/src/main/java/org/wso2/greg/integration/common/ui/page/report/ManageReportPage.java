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
package org.wso2.greg.integration.common.ui.page.report;

import java.io.IOException;
import java.util.NoSuchElementException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.wso2.greg.integration.common.ui.page.LoginPage;
import org.wso2.greg.integration.common.ui.page.util.UIElementMapper;

public class ManageReportPage {
    private static final Log log = LogFactory.getLog(LoginPage.class);
    private WebDriver driver;
    private UIElementMapper uiElementMapper;

    public ManageReportPage(WebDriver driver) throws IOException {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();
        driver.findElement(By.linkText(uiElementMapper.getElement("manage.report.page.link"))).click();

        if (!driver.findElement(By.id(uiElementMapper.getElement("manage.report.list.dashboard.middle.text"))).
                getText().contains("Manage Reports")) {

            throw new IllegalStateException("This is not the manage Report Page");

        }


    }

    public boolean checkOnUploadedReport(String reportName) throws InterruptedException {
        log.info(reportName);
        Thread.sleep(5000);
        // driver.findElement(By.xpath(uiElementMapper.getElement("service.check.save.service"))).click();
        String reportNameOnServer = driver.findElement(By.xpath("/html/body/table/tbody/tr[2]/td[3]" +
                                                                "/table/tbody/tr[2]/td/div/div/table/tbody/tr/td/a")).getText();

        log.info(reportNameOnServer);

        if (reportName.equals(reportNameOnServer)) {
            log.info("Uploaded report exists");
            return true;

        } else {
            String resourceXpath = "/html/body/table/tbody/tr[2]/td[3]/table/tbody/tr[2]/td/div/div/table/tbody/tr[";

            String resourceXpath2 = "]/td/a";

            for (int i = 2; i < 10; i++) {
                String reportNameOnAppServer = resourceXpath + i + resourceXpath2;
                String actualResourceName = driver.findElement(By.xpath(reportNameOnAppServer)).getText();
                log.info("val on app is -------> " + actualResourceName);
                log.info("Correct is    -------> " + reportName);

                try {

                    if (reportName.contains(actualResourceName)) {

                        log.info("Uploaded report exists");

                        return true;

                    } else {
                        return false;
                    }

                } catch (NoSuchElementException ex) {
                    log.info("Cannot Find the Uploaded report");

                }

            }

        }

        return false;
    }

    public LoginPage logout() throws IOException {
        driver.findElement(By.xpath(uiElementMapper.getElement("home.greg.sign.out.xpath"))).click();
        return new LoginPage(driver);
    }

}
