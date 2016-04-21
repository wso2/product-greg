/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.wso2.carbon.greg.publisher.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;
import java.util.Set;

/**
 * This class wraps the WebDriver given by the framework to achieve following concerns
 * - Slow down the web driver by overriding findElementBy method
 * - Introduce findElementByPoll to refresh page until an element if present for a given number
 * of times
 * This class uses decorator pattern.
 */

public class ESWebDriver implements WebDriver {
    private static final int MAX_WAIT_SECONDS = 30;
    private static final long SLEEP_INTERVAL_MILLISECONDS = 2000;
    private static final Log log = LogFactory.getLog(ESWebDriver.class);
    private WebDriver driver = null;

    /**
     * Throwing an exception in the constructor since BrowserManager.getWebDriver() throws exception
     */
    public ESWebDriver(WebDriver webDriver) {
        this.driver = webDriver;
    }

    /**
     * This method will keep refreshing/reloading the current url for a given number of poll-count
     * until a given element is available
     *
     * @param by        Element that is expected to be present
     * @param pollCount Number of time page need to be reloaded into webDriver
     */
    public void findElementPoll(By by, int pollCount) {
        int count = 0;
        try {
            while (!isElementPresent(by) && count < pollCount) {
                Thread.sleep(SLEEP_INTERVAL_MILLISECONDS);
                driver.get(driver.getCurrentUrl());
                count++;
            }
        } catch (InterruptedException ex) {
            log.info("An InterruptedException was caught : " + ex.getMessage(), ex);
        }
    }

    /**
     * find element in listing page. scroll down to bottom until element available
     *
     * @param by selector
     */
    public void findElementByDynamicScroll(By by) {
        while (!isElementPresent(by) && isScrollable()) {
            scrollWebPage();
        }

    }

    /**
     * This method checks whether a given element is present in the page
     *
     * @param by Element to be present in the page
     * @return true if element is present false otherwise
     */
    private boolean isElementPresent(By by) {
        return (driver.findElements(by).size() > 0);
    }

    /**
     * This method will wait until a given element is present in the page for a given amount of time
     *
     * @param by          Element to be present in the current page
     * @param waitTimeSec Time to wait in seconds
     */
    private void waitTillElementPresent(By by, int waitTimeSec) {
        WebDriverWait wait = new WebDriverWait(driver, waitTimeSec);
        wait.until(ExpectedConditions.presenceOfElementLocated(by));
    }

    /**
     * This method has override the findElement method in a way it will wait for maximum of 30 seconds
     *
     * @param by By element for findElement method
     * @return return the result of default WebDriver.findElement(By by) subjected to 30sec of max wait time
     */
    @Override public WebElement findElement(By by) {
        waitTillElementPresent(by, MAX_WAIT_SECONDS);
        return driver.findElement(by);
    }

    /**
     * This method is same as WebDriver default method. This returns an exception if there is no element
     *
     * @param by By element for findElement method
     * @return return the result of default WebDriver.findElement(By by)
     */
    public WebElement findElementWD(By by) {
        // waitTillElementPresent(by, MAX_WAIT_SECONDS);
        return driver.findElement(by);
    }

    /**
     * This method wait for a given timeout and returns the WebElement
     *
     * @param by By element for findElement method
     * @return return the WebElement or null
     */
    public WebElement findDynamicElement(By by, int timeOut) {
        WebDriverWait wait = new WebDriverWait(driver, timeOut);
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(by));
        } catch (TimeoutException ex) {
            log.error("Element not found : " + by.toString() + " in given timeout", ex);
        }

        return null;
    }

    /**
     * This method is use to scroll the web page into bottom of the page
     */
    private void scrollWebPage() {
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight)");
        log.info("Scrolled");
    }

    /**
     * This method will check page can be scrollable
     *
     * @return boolean
     */
    private boolean isScrollable() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        Object state = js.executeScript("return window.innerHeight + window.scrollY == document.body.scrollHeight; ");
        return !Boolean.valueOf(state.toString());
    }

    // proxying to the WebDriver
    @Override public void get(String s) {
        driver.get(s);
    }

    @Override public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    @Override public String getTitle() {
        return driver.getTitle();
    }

    @Override public List<WebElement> findElements(By by) {
        return driver.findElements(by);
    }

    @Override public String getPageSource() {
        return driver.getPageSource();
    }

    @Override public void close() {
        driver.close();
    }

    @Override public void quit() {
        driver.quit();
    }

    @Override public Set<String> getWindowHandles() {
        return driver.getWindowHandles();
    }

    @Override public String getWindowHandle() {
        return driver.getWindowHandle();
    }

    @Override public TargetLocator switchTo() {
        return driver.switchTo();
    }

    @Override public Navigation navigate() {
        return driver.navigate();
    }

    @Override public Options manage() {
        return driver.manage();
    }

}
