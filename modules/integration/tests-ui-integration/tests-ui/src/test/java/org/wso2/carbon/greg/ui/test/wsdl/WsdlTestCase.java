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
package org.wso2.carbon.greg.ui.test.wsdl;

import java.io.File;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.context.beans.User;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.carbon.greg.ui.test.util.ProductConstant;
import org.wso2.greg.integration.common.ui.page.LoginPage;
import org.wso2.greg.integration.common.ui.page.metadata.WSDLPage;
import org.wso2.greg.integration.common.ui.page.wsdllist.WsdlListPage;
import org.wso2.greg.integration.common.utils.GREGIntegrationUIBaseTest;

public class WsdlTestCase extends GREGIntegrationUIBaseTest{

    private WebDriver driver;
    private User userInfo;



    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
    	super.init(TestUserMode.SUPER_TENANT_ADMIN);
    	ProductConstant.init();
    	userInfo = automationContext.getContextTenant().getContextUser();
        driver = BrowserManager.getWebDriver();
        driver.get(getLoginURL());
    }

    @Test(groups = "wso2.greg", description = "verify adding a wsdl is successful")
    public void testAddWSDL() throws Exception {

        LoginPage test = new LoginPage(driver);
        test.loginAs(userInfo.getUserName(), userInfo.getPassword());
        WSDLPage addWSDL = new WSDLPage(driver);

                //Add the service name and the namespace here
        String wsdlUrl = "http://www.webservicex.net/geoipservice.asmx?WSDL";
        String wsdlUrlName = "My Test";
        String wsdlVersion = "1.0.0";
        //adding .wsdl extension to the wsdl name
        String wsdlUrlNameWithExtension = wsdlUrlName + ".wsdl";
        addWSDL.uploadWsdlFromUrl(wsdlUrl, wsdlUrlName, wsdlVersion);
        WsdlListPage wsdlListPage = new WsdlListPage(driver);
        wsdlListPage.checkOnUploadWsdl(wsdlUrlNameWithExtension);
        //uploading a wsdl from a file
        System.out.println(ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION);
        System.out.println(ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION);
        String WsdlFilePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + File.separator + "artifacts" + File.separator +
                              "GREG" + File.separator + "wsdl" + File.separator + "AmazonWebServices.wsdl";
        String WsdlFileName = "Amazon";
        String WsdlFileNameWithExtension = WsdlFileName + ".wsdl";
        System.out.println(WsdlFilePath);
        System.out.println(WsdlFileNameWithExtension);
        addWSDL.uploadWsdlFromFile(WsdlFilePath, WsdlFileNameWithExtension, wsdlVersion);
        wsdlListPage.checkOnUploadWsdl(WsdlFileNameWithExtension);
        driver.close();

    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }



}
