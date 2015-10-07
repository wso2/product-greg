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

package org.wso2.carbon.greg.ui.test.lifecycle;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.context.beans.User;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.greg.integration.common.ui.page.LoginPage;
import org.wso2.greg.integration.common.ui.page.api.ApiListPage;
import org.wso2.greg.integration.common.ui.page.metadata.ApiPage;
import org.wso2.greg.integration.common.ui.page.resourcebrowse.ResourceBrowsePage;
import org.wso2.greg.integration.common.utils.GREGIntegrationUIBaseTest;

/**
 * This test class tests the functionality of the lifecycle filter
 */
public class LifeCycleFilterStatePersistenceTest extends GREGIntegrationUIBaseTest {

    private WebDriver driver;
    private User userInfo;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        userInfo = automationContext.getContextTenant().getContextUser();
        driver = BrowserManager.getWebDriver();
        driver.get(getLoginURL());
    }

    @Test(groups = "wso2.greg", description = "verify adding new api is successful")
    public void testLifeCycleFilter() throws Exception {

        LoginPage test = new LoginPage(driver);
        test.loginAs(userInfo.getUserName(), userInfo.getPassword());

        //Add  Api Details Here
        String provider = "Test Provider";
        String name = "Test Api";
        String context = "Test Context";
        String versionApi = "1.2.3";
        String lifeCycle = "ServiceLifeCycle";
        String lifeCycleState = "Development";
        ApiPage addApi;
        for (int i = 0; i < 17; i++) {
            addApi = new ApiPage(driver);
            addApi.uploadApi(provider + i, name + i, context, versionApi);
            ResourceBrowsePage resourceBrowsePage = new ResourceBrowsePage(driver);
            resourceBrowsePage.addLifeCycle(lifeCycle);
        }
        Thread.sleep(10000);
        ApiListPage apiListPage = new ApiListPage(driver);
        apiListPage.checkOnUploadApi(provider + 1);
        apiListPage.checkFilterStatePersistence(lifeCycle, lifeCycleState);
        driver.close();

    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }


}
