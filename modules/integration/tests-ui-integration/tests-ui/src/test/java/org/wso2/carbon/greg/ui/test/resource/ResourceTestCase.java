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
package org.wso2.carbon.greg.ui.test.resource;

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
import org.wso2.greg.integration.common.ui.page.resource.ResourceHome;
import org.wso2.greg.integration.common.utils.GREGIntegrationUIBaseTest;

public class ResourceTestCase extends GREGIntegrationUIBaseTest{

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

    @Test(groups = "wso2.greg", description = "verify adding Resources are successful")
    public void testResourceAdd() throws Exception {
        //Login to the ResourceHome Page
        LoginPage test = new LoginPage(driver);
        test.loginAs(userInfo.getUserName(), userInfo.getPassword());
        ResourceHome resourceHome = new ResourceHome(driver);
       //adding the file from the resources  file from the
        String carPath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + File.separator + "artifacts" + File.separator +
                         "GREG" + File.separator + "car" + File.separator + "Capp_1.0.0.car";
        resourceHome.uploadResourceFromFile(carPath);
       //checking whether the uploaded resource exists in the table
        resourceHome.checkOnUploadSuccess("Capp_1.0.0.carTestFile");
       //enter URL to add resource form a URL
        String UrlPath = "http://svn.wso2.org/repos/wso2/carbon/platform/branches/4.0.0/platform-integration/clarity-tests/" +
                         "1.0.8/org.wso2.carbon.automation.test.repo/src/main/resources/artifacts/GREG/txt/sampleText.txt";
        resourceHome.uploadResourceFromUrl(UrlPath);
        System.out.println("Im here");
       //checking whether the uploaded resource exists in the table
        resourceHome.checkOnUploadSuccess("sampleText.txt");
       //uploading a collection Item to the Greg
        String folderName = "My Document";
        resourceHome.uploadCollection(folderName);
       //Closing the web driver
        driver.close();

    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }

}
