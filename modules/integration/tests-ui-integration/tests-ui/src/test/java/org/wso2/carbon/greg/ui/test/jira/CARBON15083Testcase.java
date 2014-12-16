/*
*Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.greg.ui.test.jira;

import java.io.File;

import junit.framework.Assert;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.context.beans.User;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.carbon.greg.ui.test.util.ProductConstant;
import org.wso2.greg.integration.common.ui.page.LoginPage;
import org.wso2.greg.integration.common.ui.page.extensionlist.ExtensionListPage;
import org.wso2.greg.integration.common.ui.page.extensions.ExtensionPage;
import org.wso2.greg.integration.common.ui.page.handler.HandlerHome;
import org.wso2.greg.integration.common.ui.page.metadata.ServicePage;
import org.wso2.greg.integration.common.ui.page.servlistlist.ServiceListPage;
import org.wso2.greg.integration.common.ui.page.util.UIElementMapper;
import org.wso2.greg.integration.common.utils.GREGIntegrationUIBaseTest;

public class CARBON15083Testcase extends GREGIntegrationUIBaseTest {

    private WebDriver driver;
    private User userInfo;
    private UIElementMapper uiElementMapper;

    private static final String ERROR_MESSAGE = "Example exception message";


    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        ProductConstant.init();
        userInfo = automationContext.getContextTenant().getContextUser();
        driver = BrowserManager.getWebDriver();
        driver.get(getLoginURL());
    }

    @Test(groups = "wso2.greg", description = "verify adding new extension is successful")
    public void testCarbon15083() throws Exception {
        LoginPage test = new LoginPage(driver);
        this.uiElementMapper = UIElementMapper.getInstance();
        test.loginAs(userInfo.getUserName(), userInfo.getPassword());

        //Add Extension JAR
        ExtensionPage addNewExtension = new ExtensionPage(driver);
        String extensionFilePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + File.separator + "artifacts" +
                File.separator +
                "GREG" + File.separator + "extensions" + File.separator + "org.wso2.governance.handler.test-1.0.0.jar";
        String extensionName = "org.wso2.governance.handler.test-1.0.0.jar";
        addNewExtension.addNewExtension(extensionFilePath);
        ExtensionListPage extensionListpage = new ExtensionListPage(driver);
        extensionListpage.checkOnUploadedExtension(extensionName);
        driver.navigate().to(getLoginURL());

        //Add Handler
        HandlerHome addNewHandlerHome = new HandlerHome(driver);

        String handler = "<handler class=\"org.wso2.governance.handler.test.SampleHandler\" profiles=\"default,uddi-registry\">\n" +
                "\t<filter class=\"org.wso2.carbon.registry.core.jdbc.handlers.filters.MediaTypeMatcher\">\n" +
                "\t\t<property name=\"mediaType\">application/vnd.wso2-service+xml</property>\n" +
                "\t</filter>\n" +
                "</handler>";

        String handlerName = "org.wso2.governance.handler.test.SampleHandler";
        addNewHandlerHome.addNewHandler(handler);
        addNewHandlerHome.checkOnUploadedHandler(handlerName);

        driver.navigate().to(getLoginURL());
        //Add Service
        ServicePage addService = new ServicePage(driver);
        //Add the service name and the namespace here
        String serviceName = "testServiceName";
        String serviceNameSpace = "testNameSpace";
        String version = "1.0.0";
        addService.uploadService(serviceName, serviceNameSpace, version);
        ServiceListPage serviceListPage = new ServiceListPage(driver);

        //Waiting maximum 18secs to show updated service list.
        for (int i = 0; i <= 6; i++) {
            Thread.sleep(3000);
            driver.navigate().refresh();

            if (!driver.findElement(By.xpath(uiElementMapper.getElement("wsdl.list.workarea"))).
                    getText().contains("There are no Services added")) {
                serviceListPage.checkOnUploadService(serviceName);
                break;
            }
        }

        driver.findElement(By.xpath("//div[@id='workArea']//table//td[contains(.,'" +
                serviceName +
                "')]/..//a[@title='Delete']")).click();
        driver.findElement(By.xpath(uiElementMapper.getElement("error.button.yes.xpath"))).click();
        Thread.sleep(3000);
        Assert.assertTrue(driver.getPageSource().contains(ERROR_MESSAGE));


        driver.close();

    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }
}

