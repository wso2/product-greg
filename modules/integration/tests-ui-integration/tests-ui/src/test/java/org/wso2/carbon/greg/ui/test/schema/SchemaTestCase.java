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
package org.wso2.carbon.greg.ui.test.schema;

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
import org.wso2.greg.integration.common.ui.page.metadata.SchemaPage;
import org.wso2.greg.integration.common.ui.page.schemalist.SchemaListPage;
import org.wso2.greg.integration.common.utils.GREGIntegrationUIBaseTest;

public class SchemaTestCase extends GREGIntegrationUIBaseTest{

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

    @Test(groups = "wso2.greg", description = "verify adding a schema is successful")
    public void testAddSchema() throws Exception {

        LoginPage test = new LoginPage(driver);
        test.loginAs(userInfo.getUserName(), userInfo.getPassword());
        SchemaPage schemaPage = new SchemaPage(driver);
        String version = "1.0.0";


        //Add the service name and the namespace here
        String schemaUrl = "http://svn.wso2.org/repos/wso2/carbon/platform/branches/4.0.0/platform-integration" +
                           "/clarity-tests/1.0.5/org.wso2.carbon.automation.test.repo/src/main/resources/artifacts/GREG" +
                           "/schema/library.xsd";
        String schemaUrlName = "My Test Schema";
        //adding .wsdl extension to the wsdl name
        String schemaUrlNameWithExtension = schemaUrlName + ".xsd";
        schemaPage.uploadSchemaFromUrl(schemaUrl, schemaUrlName, version);
        Thread.sleep(6000);
        SchemaListPage schemaListPage = new SchemaListPage(driver);
        driver.navigate().refresh();
        schemaListPage.checkOnUploadSchema(schemaUrlNameWithExtension);
        //uploading a wsdl from a file
        String SchemaFilePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + File.separator + "artifacts" + File.separator +
                                "GREG" + File.separator + "schema" + File.separator + "Person.xsd";
        String SchemaFileName = "Personone";
        String SchemaNameWithExtension = SchemaFileName + ".xsd";
        System.out.println(SchemaFilePath);
        System.out.println(SchemaNameWithExtension);
        schemaPage.uploadSchemaFromFile(SchemaFilePath, SchemaNameWithExtension, version);
        Thread.sleep(6000);
        driver.navigate().refresh();
        schemaListPage.checkOnUploadSchema(SchemaNameWithExtension);
        driver.close();

    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }



}
