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

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.carbon.greg.publisher.login.PublisherHomePage;
import org.wso2.carbon.greg.publisher.login.PublisherLoginPage;
import org.wso2.carbon.greg.publisher.utils.ESWebDriver;
import org.wso2.carbon.greg.publisher.utils.PublisherUtil;
import org.wso2.greg.integration.common.ui.page.util.UIElementMapper;
import org.wso2.greg.integration.common.utils.GREGIntegrationUIBaseTest;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.concurrent.TimeUnit;
import javax.xml.xpath.XPathExpressionException;

/***
 * This class have test methods for Create,Retrieve,Remove,Update Operations for Swagger
 */
public class SwaggerCRUDUITestCase extends GREGIntegrationUIBaseTest {
    private static final String SWAGGER_NAME = "swagger.json";
    private static final String SWAGGER_VERSION = "1.0.0";
    private ESWebDriver driver;
    private UIElementMapper uiElementMapper;
    private String uniqueName;
    private PublisherUtil asset;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = new ESWebDriver(BrowserManager.getWebDriver());
        driver.manage().timeouts().implicitlyWait(LOGIN_WAIT_SECONDS, TimeUnit.SECONDS);
        driver.get(getPublisherBaseUrl());
        PublisherLoginPage publisherLoginPage = new PublisherLoginPage(driver);
        this.uiElementMapper = UIElementMapper.getInstance();
        asset = new PublisherUtil(driver);
        // performing login to publisher
        publisherLoginPage.loginAs(automationContext.getContextTenant().getContextUser().getUserName(),
                automationContext.getContextTenant().getContextUser().getPassword());
    }

    @Test(groups = "wso2.greg", description = "Create a swagger by url and validate its availability")
    public void testCreateAsset() throws MalformedURLException, XPathExpressionException {
        uniqueName = getUniqueName();
        asset.createGenericAsset("http://petstore.swagger.io/v2/swagger.json", uniqueName + SWAGGER_NAME,
                SWAGGER_VERSION, uiElementMapper.getElement("publisher.swaggers"));
    }

    @Test(groups = "wso2.greg", description = "Click and validate the asset", dependsOnMethods = "testCreateAsset")
    public void testClickAndValidate() {
        asset.clickOnContentTypeAsset(uniqueName + SWAGGER_NAME, uiElementMapper.getElement("publisher.swaggers"));
        asset.validateDetailsGenericType(uniqueName + SWAGGER_NAME,
                uiElementMapper.getElement("publisher.general.name"));
        asset.validateDetailsGenericType(SWAGGER_VERSION, uiElementMapper.getElement("publisher.general.version"));
    }

    @Test(groups = "wso2.greg", description = "Delete a swagger and validate its availability",
            dependsOnMethods = "testClickAndValidate")
    public void testDeleteAsset() throws MalformedURLException, XPathExpressionException {
        PublisherUtil asset = new PublisherUtil(driver);
        asset.deleteAndValidateAsset(uiElementMapper.getElement("publisher.swaggers"), uniqueName + SWAGGER_NAME);
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws IOException {
        PublisherHomePage publisherHomePage = new PublisherHomePage(driver);
        publisherHomePage.logOut();
        driver.quit();
    }

}

