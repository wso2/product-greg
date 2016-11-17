/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.greg.publisher.crudui;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.carbon.greg.publisher.login.PublisherHomePage;
import org.wso2.carbon.greg.publisher.login.PublisherLoginPage;
import org.wso2.carbon.greg.publisher.utils.ESWebDriver;
import org.wso2.greg.integration.common.utils.GREGIntegrationUIBaseTest;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertEquals;

/**
 * This class have test method for tooltip functionality of custom rest service rxt.
 */
public class CustomRestRXTTestCase extends GREGIntegrationUIBaseTest {
    private static final Log log = LogFactory.getLog(CustomRestRXTTestCase.class);
    private static final String REST_SERVICE_RXT = "restservice.rxt";
    private static final String CUSTOM_REST_RXT_FILE = "customRest.rxt";
    private static final String DEFAULT_REST_RXT_FILE = "defaultRest.rxt";
    private static final String EXPECTED_URL_PATTERN_TOOLTIP = "/*";
    private static final String EXPECTED_HTTP_VERB_TOOLTIP = "GET, POST";
    private static final String EXPECTED_AUTH_TYPE_TOOLTIP = "None, Application";
    private ESWebDriver driver;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = new ESWebDriver(BrowserManager.getWebDriver());
        driver.manage().timeouts().implicitlyWait(LOGIN_WAIT_SECONDS, TimeUnit.SECONDS);
        try {
            addNewRxtConfiguration(CUSTOM_REST_RXT_FILE, REST_SERVICE_RXT);
        } catch (AxisFault e) {
            log.error("Error while adding new asset type", e);
        }

        driver.get(getPublisherBaseUrl());
        PublisherLoginPage publisherLoginPage = new PublisherLoginPage(driver);
        publisherLoginPage.loginAs(automationContext.getContextTenant().getContextUser().getUserName(),
                automationContext.getContextTenant().getContextUser().getPassword());
    }

    @Test(groups = "wso2.greg", description = "Test for tooltip in custom unbounded table")
    public void testTooltipForUnboundTable() {
        String actualUrlPatternTooltip;
        String actualHttpVerbTooltip;
        String actualAuthTypeTooltip;
        driver.findElement(By.id("rest-services-btn-add")).click();
        driver.findElement(By.id("rest-services-btn-continue")).click();
        driver.findElement(By.cssSelector("[href=\"#collapseuritemplate\"]")).click();
        driver.findElement(By.cssSelector("#collapseuritemplate .js-add-unbounded-row")).click();
        WebElement uriTemplate = driver.findElement(By.id("table_uritemplate"));
        actualUrlPatternTooltip = uriTemplate.findElement(By.id("uritemplate_urlPattern")).getAttribute("title");
        actualHttpVerbTooltip = uriTemplate.findElement(By.id("uritemplate_httpVerb")).getAttribute("title");
        actualAuthTypeTooltip = uriTemplate.findElement(By.id("uritemplate_authType")).getAttribute("title");
        assertEquals(actualUrlPatternTooltip, EXPECTED_URL_PATTERN_TOOLTIP, "URL pattern tooltip does not match");
        assertEquals(actualHttpVerbTooltip, EXPECTED_HTTP_VERB_TOOLTIP, "HTTP verb tooltip does not match");
        assertEquals(actualAuthTypeTooltip, EXPECTED_AUTH_TYPE_TOOLTIP, "Auth type tooltip does not match");
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws IOException {
        try {
            addNewRxtConfiguration(DEFAULT_REST_RXT_FILE, REST_SERVICE_RXT);
        } catch (Exception e) {
            log.error("Error while adding new asset type", e);
        }
        PublisherHomePage publisherHomePage = new PublisherHomePage(driver);
        publisherHomePage.logOut();
        driver.quit();
    }
}
