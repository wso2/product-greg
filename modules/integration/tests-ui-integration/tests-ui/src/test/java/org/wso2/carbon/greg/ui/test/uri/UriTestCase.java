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
package org.wso2.carbon.greg.ui.test.uri;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.context.beans.User;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.greg.integration.common.ui.page.LoginPage;
import org.wso2.greg.integration.common.ui.page.metadata.UriPage;
import org.wso2.greg.integration.common.ui.page.uriList.UriListPage;
import org.wso2.greg.integration.common.utils.GREGIntegrationUIBaseTest;

public class UriTestCase extends GREGIntegrationUIBaseTest{

    private WebDriver driver;
    private User userInfo;



    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
    	super.init(TestUserMode.SUPER_TENANT_ADMIN);
    	userInfo = automationContext.getContextTenant().getContextUser();
        driver = BrowserManager.getWebDriver();
        driver.get(getLoginURL());
    }

    @Test(groups = "wso2.greg", description = "verify adding a URI is successful")
    public void testAddUri() throws Exception {
        LoginPage test = new LoginPage(driver);
        test.loginAs(userInfo.getUserName(), userInfo.getPassword());
        UriPage addUri = new UriPage(driver);

        //Add the URI as Generic
        String uriLink = "http://svn.wso2.org/repos/wso2/carbon/platform/branches/4.0.0/platform-integration" +
                         "/clarity-tests/1.0.5/org.wso2.carbon.automation.test.repo/src/main/resources/artifacts/GREG/wsdl" +
                         "/sample.wsdl";
        String uriName = "Sample";

        //-------------------------adding uri as Gerneric--------------

        addUri.uploadGenericUri(uriLink, uriName);
        Thread.sleep(8000);
        UriListPage uriListPage = new UriListPage(driver);
        uriListPage.checkOnUploadUri(uriName);

        //------------------------------------------------------------

        //add the Uri as a Wsdl

        String uriWsdlLink = "http://svn.wso2.org/repos/wso2/carbon/platform/branches/4.0.0/platform-integration" +
                             "/clarity-tests/1.0.5/org.wso2.carbon.automation.test.repo/src/main/resources/artifacts/GREG/wsdl" +
                             "/echo.wsdl";
        String uriWsdlName = "Echo";

        //-------------------------adding uri as Wsdl------------------------
        addUri.uploadWsdlUri(uriWsdlLink, uriWsdlName);

        //-------------------------------------------------------------------

        //add Uri as xsd

        String uriXsdPath = "http://svn.wso2.org/repos/wso2/carbon/platform/branches/4.0.0/platform-integration" +
                            "/clarity-tests/1.0.5/org.wso2.carbon.automation.test.repo/src/main/resources/artifacts/GREG/policy" +
                            "/UTPolicy.xml";
        String uriXsdName = "CalculatorXsd";

        //---------------adding uri as  xsd------------------------------
        addUri.uploadXsdUri(uriXsdPath, uriXsdName);
        //----------------------------------------------------------------

        //add Uri as a Policy

        String uriPolicyPath = "http://svn.wso2.org/repos/wso2/carbon/platform/branches/4.0.0/platform-integration" +
                               "/clarity-tests/1.0.5/org.wso2.carbon.automation.test.repo/src/main/resources/artifacts/GREG/policy" +
                               "/UTPolicy.xml";
        String uriPolicyPathName = "policyURI";

        //-----------------adding uri as policy-------------------------
        addUri.uploadPolicyUri(uriPolicyPath, uriPolicyPathName);
        //-------------------------------------------------------------

        driver.close();

    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }



}
