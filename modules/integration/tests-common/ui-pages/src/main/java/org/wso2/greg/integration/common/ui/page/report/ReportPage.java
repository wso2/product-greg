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

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Select;
import org.wso2.greg.integration.common.ui.page.util.UIElementMapper;

public class ReportPage {

    private WebDriver driver;
    private UIElementMapper uiElementMapper;

    public ReportPage(WebDriver driver) throws IOException {
        this.driver = driver;

        this.uiElementMapper = UIElementMapper.getInstance();

        driver.findElement(By.linkText(uiElementMapper.getElement("manage.report.page.link"))).click();

        if (!driver.findElement(By.id(uiElementMapper.getElement("add.report.list.dashboard.middle.text"))).
                getText().contains("Add Report")) {
           

            throw new IllegalStateException("This is not the add Report Page");
        }

    }

    public void addNewReport(String reportName, String templatePath, String reportType,
                             String reportClass)
            throws InterruptedException, IOException {

        driver.findElement(By.linkText(uiElementMapper.getElement("report.add.link"))).click();
        driver.findElement(By.id(uiElementMapper.getElement("add.report.name"))).sendKeys(reportName);
        driver.findElement(By.id(uiElementMapper.getElement("add.report.template"))).sendKeys(templatePath);
        new Select(driver.findElement(By.id(uiElementMapper.getElement("add.report.type")))).selectByVisibleText(reportType);
        driver.findElement(By.id(uiElementMapper.getElement("add.report.class"))).sendKeys(reportClass);
        JavascriptExecutor js2 = (JavascriptExecutor) driver;
        js2.executeScript("addReport()");

    }

}
