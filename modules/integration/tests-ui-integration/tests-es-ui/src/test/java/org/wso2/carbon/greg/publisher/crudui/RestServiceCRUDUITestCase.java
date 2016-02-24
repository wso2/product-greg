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
package org.wso2.carbon.greg.publisher.crudui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.carbon.greg.publisher.PublisherHomePage;
import org.wso2.carbon.greg.publisher.PublisherLoginPage;
import org.wso2.carbon.greg.publisher.utils.ESWebDriver;
import org.wso2.carbon.greg.publisher.utils.PublisherUtil;
import org.wso2.greg.integration.common.ui.page.util.UIElementMapper;
import org.wso2.greg.integration.common.utils.GREGIntegrationUIBaseTest;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.concurrent.TimeUnit;
import javax.xml.xpath.XPathExpressionException;

/***
 * This class have test methods for Create,Retrieve,Remove,Update Operations for Rest Service
 */
public class RestServiceCRUDUITestCase extends GREGIntegrationUIBaseTest {
    private static final Log log = LogFactory.getLog(RestServiceCRUDUITestCase.class);
    private static final String SERVICE_VERSION = "1.0.0";
    private ESWebDriver driver;
    private UIElementMapper uiElementMapper;
    private String elementID;
    private String uniqueName;
    private PublisherUtil asset;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = new ESWebDriver(BrowserManager.getWebDriver());
        driver.manage().timeouts().implicitlyWait(LOGIN_WAIT_SECONDS, TimeUnit.SECONDS);
        driver.get(getPublisherURL());
        PublisherLoginPage publisherLoginPage = new PublisherLoginPage(driver);
        this.uiElementMapper = UIElementMapper.getInstance();
        this.asset = new PublisherUtil(driver);
        // performing login to publisher
        publisherLoginPage.loginAs(automationContext.getContextTenant().getContextUser().getUserName(),
                automationContext.getContextTenant().getContextUser().getPassword());
    }

    @Test(groups = "wso2.greg", description = "Create a REST service and validate it availability")
    public void createAsset() throws MalformedURLException, XPathExpressionException {
        uniqueName = getUniqueName();
        asset.createGenericTypeAsset(uniqueName, "/" + uniqueName, SERVICE_VERSION, "TestRest" + uniqueName,
                uiElementMapper.getElement("publisher.restservices"));
        this.elementID = asset.getUUID();
        driver.findElementWD(By.linkText(uiElementMapper.getElement("publisher.restservices"))).click();

    }

    @Test(groups = "wso2.greg", description = "Click and validate the asset", dependsOnMethods = "createAsset")
    public void clickAndValidate() {
        asset.clickOnGenericTypeAsset(uiElementMapper.getElement("publisher.restservices"), elementID);
        asset.validateDetailsContentType(uniqueName, uiElementMapper.getElement("publisher.general.name"));
        asset.validateDetailsContentType(SERVICE_VERSION, uiElementMapper.getElement("publisher.general.version"));
    }

    @Test(groups = "wso2.greg", description = "Update a REST service and validate it",
            dependsOnMethods = "clickAndValidate")
    public void updateAsset() throws MalformedURLException, XPathExpressionException {
        uniqueName = getUniqueName();
        asset.updateAsset(uiElementMapper.getElement("publisher.restservices"), log.getClass().getName(), elementID,
                uniqueName);
    }

    @Test(groups = "wso2.greg", description = "Click and validate the asset", dependsOnMethods = "updateAsset")
    public void clickAndValidateAfterUpdate() {
        asset.clickOnGenericTypeAsset(uiElementMapper.getElement("publisher.restservices"), elementID);
        asset.validateDetailsContentType("/" + uniqueName, uiElementMapper.getElement("publisher.general.context"));
        asset.validateDetailsContentType("desc" + uniqueName,
                uiElementMapper.getElement("publisher.general.description"));
    }

    @Test(groups = "wso2.greg", description = "Delete a REST service and validate its availability",
            dependsOnMethods = "clickAndValidateAfterUpdate")
    public void testDeleteAsset() throws MalformedURLException, XPathExpressionException {
        PublisherUtil asset = new PublisherUtil(driver);
        asset.clickOnGenericTypeAsset(uiElementMapper.getElement("publisher.restservices"), elementID);
        asset.deleteAndValidateAsset(uiElementMapper.getElement("publisher.restservices"), elementID);
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws IOException {
        PublisherHomePage publisherHomePage = new PublisherHomePage(driver);
        publisherHomePage.logOut();
        driver.quit();
    }

}