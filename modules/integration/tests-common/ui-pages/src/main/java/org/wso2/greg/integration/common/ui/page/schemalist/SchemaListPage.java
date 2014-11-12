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
package org.wso2.greg.integration.common.ui.page.schemalist;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Select;
import org.wso2.greg.integration.common.ui.page.util.UIElementMapper;

import java.io.IOException;
import java.util.NoSuchElementException;

public class SchemaListPage {

    private static final Log log = LogFactory.getLog(SchemaListPage.class);
    private WebDriver driver;
    private UIElementMapper uiElementMapper;


    public SchemaListPage(WebDriver driver) throws IOException {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();
        // Check that we're on the right page.
        if (!driver.findElement(By.id(uiElementMapper.getElement("schema.list.dashboard.middle.text"))).
                getText().contains("List")) {

            throw new IllegalStateException("This is not Schema List Page");
        }
    }

    public boolean checkOnUploadSchema(String wsdlName) throws InterruptedException {
        log.info(wsdlName);
        Thread.sleep(5000);
        driver.navigate().refresh();
        // driver.findElement(By.xpath(uiElementMapper.getElement("service.check.save.service"))).click();
        String ServiceNameOnServer = driver.findElement(By.xpath(" /html/body/table/tbody/tr[2]/td[3]" +
                                 "/table/tbody/tr[2]/td/div/div/form[4]/table/tbody/tr/td/a")).getText();

        log.info(ServiceNameOnServer);

        if (wsdlName.equals(ServiceNameOnServer)) {
            log.info("Uploaded Schema exists");
            return true;

        } else {
            String resourceXpath = "/html/body/table/tbody/tr[2]/td[3]/table/tbody/tr[2]/td/div/" +
                                                                      "div/form[4]/table/tbody/tr[";

            String resourceXpath2 = "]/td/a";
            for (int i = 2; i < 10; i++) {
                String serviceNameOnAppServer = resourceXpath + i + resourceXpath2;
                String actualResourceName = driver.findElement(By.xpath(serviceNameOnAppServer)).getText();
                log.info("val on app is -------> " + actualResourceName);
                log.info("Correct is    -------> " + wsdlName);
                try {
                    if (wsdlName.contains(actualResourceName)) {
                        log.info("Uploaded Schema    exists");
                        return true;

                    }  else {
                        return false ;

                    }


                } catch (NoSuchElementException ex) {
                    log.info("Cannot Find the Uploaded Wsdl");

                }

            }

        }

        return false;
    }


    public void lifeCyclePromotion(String lifeCycleName) throws InterruptedException {
        driver.findElement(By.id(uiElementMapper.getElement("life.cycle.expand.id"))).click();
        driver.findElement(By.linkText(uiElementMapper.getElement("life.cycle.add"))).click();
        new Select(driver.findElement(By.id("aspect"))).selectByVisibleText(lifeCycleName);
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("addAspect()");

        //checking the checkList
        String lifeCycleStage= driver.findElement(By.xpath(uiElementMapper.getElement("life.cycle.stage"))).getText();

        if(lifeCycleStage.contains("Development")){
            log.info("lifecycle is at the Testing stage");

            driver.findElement(By.id(uiElementMapper.getElement("life.cycle.add.option"))).click();
            Thread.sleep(1000);
            driver.findElement(By.id(uiElementMapper.getElement("life.cycle.add.option1"))).click();
            Thread.sleep(2000);
            driver.findElement(By.id(uiElementMapper.getElement("life.cycle.add.option2"))).click();
            Thread.sleep(1000);

            //promoting the lifecycle
            driver.findElement(By.id(uiElementMapper.getElement("life.cycle.promote"))).click();


            driver.findElement(By.cssSelector(uiElementMapper.getElement("life.cycle.promote.ok.button"))).click();

            String nextLifeCycleStage= driver.findElement(By.xpath(uiElementMapper.getElement("life.cycle.stage"))).getText();

            if(nextLifeCycleStage.contains("Testing")){
                log.info("lifecycle is at the Testing stage");


            }  else {
                log.info("lifecycle is not  at the Testing stage");
                throw new NoSuchElementException();
            }

        } else {
            log.info("lifecycle is not  at the Development stage");
            throw new NoSuchElementException();
        }


        String lifeCycleStage2= driver.findElement(By.xpath(uiElementMapper.getElement("life.cycle.stage"))).getText();


        if(lifeCycleStage2.contains("Testing")){
            log.info("lifecycle is promoting from  Testing stage");

            driver.findElement(By.id(uiElementMapper.getElement("life.cycle.add.option"))).click();
            Thread.sleep(1000);
            driver.findElement(By.id(uiElementMapper.getElement("life.cycle.add.option1"))).click();
            Thread.sleep(1000);
            driver.findElement(By.id(uiElementMapper.getElement("life.cycle.add.option2"))).click();

            Thread.sleep(1000);
            //promoting the lifecycle
            driver.findElement(By.id(uiElementMapper.getElement("life.cycle.promote"))).click();
            driver.findElement(By.cssSelector(uiElementMapper.getElement("life.cycle.promote.ok.button"))).click();
            Thread.sleep(1000);

            String FinalLifeCycleStage= driver.findElement(By.xpath(uiElementMapper.getElement("life.cycle.stage"))).getText();

            if(FinalLifeCycleStage.contains("production")){
                log.info("lifecycle is at the production stage");

                driver.findElement(By.id(uiElementMapper.getElement("life.cycle.publish"))).click();
                driver.findElement(By.cssSelector(uiElementMapper.getElement("life.cycle.promote.ok.button"))).click();


            }
            else {
                log.info("lifecycle is not at the production stage");
                throw new NoSuchElementException();

            }

        }

        else {
            log.info("cannot promote the lifecycle its not at the Testing stage");
            throw new NoSuchElementException();
        }

    }


    public boolean promoteSchemaLifecycle(String SchemaName,String lifeCycleName) throws InterruptedException {


        log.info(SchemaName);
        Thread.sleep(5000);

        String firstElementXpath ="/html/body/table/tbody/tr[2]/td[3]/table/tbody/tr[2]/td/div/div/" +
                "form[4]/table/tbody/tr/td/a";
        String schemaNameOnServer = driver.findElement(By.xpath(firstElementXpath)).getText();
        log.info(schemaNameOnServer);
        if (SchemaName.equals(schemaNameOnServer)) {
            log.info("Uploaded schema exists");
            driver.findElement(By.xpath(firstElementXpath)).click();
            lifeCyclePromotion(lifeCycleName);
            return true;
        } else {
            String resourceXpath = "/html/body/table/tbody/tr[2]/td[3]/table/tbody/tr[2]/td/div/div/" +
                    "form[4]/table/tbody/tr[";
            String resourceXpath2 = "]/td/a";
            for (int i = 2; i < 10; i++) {
                String schemaNameOnAppServer = resourceXpath + i + resourceXpath2;

                String actualSchemaName = driver.findElement(By.xpath(schemaNameOnAppServer)).getText();
                log.info("val on app is -------> " + actualSchemaName);
                log.info("Correct is    -------> " + SchemaName);

                try {
                    if (SchemaName.contains(actualSchemaName)) {
                        log.info("Uploaded Schema exists");
                        driver.findElement(By.xpath(schemaNameOnAppServer)).click();
                        lifeCyclePromotion(lifeCycleName);
                        return true;
                    } else {
                        return false;
                    }
                } catch (NoSuchElementException ex) {
                    log.info("Cannot Find the Uploaded Schema");

                }
            }
        }
        return false;
    }








}
