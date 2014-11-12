/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.context.beans.User;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.greg.integration.common.ui.page.LoginPage;
import org.wso2.greg.integration.common.ui.page.lifecycle.LifeCycleHomePage;
import org.wso2.greg.integration.common.ui.page.lifecycle.LifeCyclesPage;
import org.wso2.greg.integration.common.utils.GREGIntegrationUIBaseTest;

public class AddNewLifeCycleTestCase extends GREGIntegrationUIBaseTest{

    private WebDriver driver;
    private User userInfo;



    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        userInfo = automationContext.getContextTenant().getContextUser();
        driver = BrowserManager.getWebDriver();
        driver.get(getLoginURL());
    }

    @Test(groups = "wso2.greg", description ="verify adding a new life cycle is successful")
    public void testAddNewLifeCycle() throws Exception {
        LoginPage test = new LoginPage(driver);
        test.loginAs(userInfo.getUserName(), userInfo.getPassword());
        LifeCycleHomePage lifeCycleHomePage= new LifeCycleHomePage(driver);
        LifeCyclesPage lifeCyclesPage = new LifeCyclesPage(driver);


        String lifeCycle = "<aspect name=\"AutomatedLifeCycle\" class=\"org.wso2.carbon.governance.registry.extensions.aspects.DefaultLifeCycle\">\n" +
                           "    <configuration type=\"literal\">\n" +
                           "        <lifecycle>\n" +
                           "            <scxml xmlns=\"http://www.w3.org/2005/07/scxml\"\n" +
                           "                   version=\"1.0\"\n" +
                           "                   initialstate=\"Development\">\n" +
                           "                <state id=\"Development\">\n" +
                           "                    <datamodel>\n" +
                           "                        <data name=\"checkItems\">\n" +
                           "                            <item name=\"Code Completed\" forEvent=\"\">                               \n" +
                           "                            </item>\n" +
                           "                            <item name=\"WSDL, Schema Created\" forEvent=\"\">\n" +
                           "                            </item>\n" +
                           "                            <item name=\"QoS Created\" forEvent=\"\">\n" +
                           "                            </item>\n" +
                           "                        </data>\n" +
                           "\t\t\t<data name=\"transitionExecution\">\n" +
                           "                            <execution forEvent=\"Promote\" class=\"org.wso2.carbon.governance.registry.extensions.executors.ServiceVersionExecutor\">\n" +
                           "                                <parameter name=\"currentEnvironment\" value=\"/_system/governance/trunk/{@resourcePath}/{@resourceName}\"/>\n" +
                           "                                <parameter name=\"targetEnvironment\" value=\"/_system/governance/branches/testing/{@resourcePath}/{@version}/{@resourceName}\"/>\n" +
                           "                                <parameter name=\"service.mediatype\" value=\"application/vnd.wso2-service+xml\"/>\n" +
                           "                                <parameter name=\"wsdl.mediatype\" value=\"application/wsdl+xml\"/>\n" +
                           "                                <parameter name=\"endpoint.mediatype\" value=\"application/vnd.wso2.endpoint\"/>\n" +
                           "                            </execution>\t\t\t\n" +
                           "                        </data>\n" +
                           "\t\t\t<data name=\"transitionUI\">\n" +
                           "                            <ui forEvent=\"Promote\" href=\"../lifecycles/pre_invoke_aspect_ajaxprocessor.jsp?currentEnvironment=/_system/governance/trunk/\"/>\n" +
                           "                        </data>\n" +
                           "                        \n" +
                           "                        <data name=\"transitionScripts\">\n" +
                           "                            <js forEvent=\"Promote\">\n" +
                           "                                <console function=\"showServiceList\">\n" +
                           "<script type=\"text/javascript\">\n" +
                           "                                        showServiceList = function() { var element = document.getElementById('hidden_media_type'); var mediaType = \"\"; if (element) { mediaType = element.value;} if (mediaType == \"application/vnd.wso2-service+xml\") { location.href = unescape(\"../generic/list.jsp?region=region3%26item=governance_list_service_menu%26key=service%26breadcrumb=Services%26singularLabel=Service%26pluralLabel=Services\"); } }\n" +
                           "</script>\n" +
                           "                                </console>\n" +
                           "                            </js>\n" +
                           "                        </data>\n" +
                           "                    </datamodel>\n" +
                           "                    <transition event=\"Promote\" target=\"Testing\"/>                  \n" +
                           "                </state>\n" +
                           "                <state id=\"Testing\">\n" +
                           "                    <datamodel>\n" +
                           "                        <data name=\"checkItems\">\n" +
                           "                            <item name=\"Effective Inspection Completed\" forEvent=\"\">\n" +
                           "                            </item>\n" +
                           "                            <item name=\"Test Cases Passed\" forEvent=\"\">\n" +
                           "                            </item>\n" +
                           "                            <item name=\"Smoke Test Passed\" forEvent=\"\">\n" +
                           "                            </item>\n" +
                           "                        </data>\n" +
                           "                        <data name=\"transitionExecution\">\n" +
                           "                            <execution forEvent=\"Promote\" class=\"org.wso2.carbon.governance.registry.extensions.executors.ServiceVersionExecutor\">\n" +
                           "                                <parameter name=\"currentEnvironment\" value=\"/_system/governance/branches/testing/{@resourcePath}/{@version}/{@resourceName}\"/>\n" +
                           "                                <parameter name=\"targetEnvironment\" value=\"/_system/governance/branches/production/{@resourcePath}/{@version}/{@resourceName}\"/>\n" +
                           "                                <parameter name=\"service.mediatype\" value=\"application/vnd.wso2-service+xml\"/>\n" +
                           "                                <parameter name=\"wsdl.mediatype\" value=\"application/wsdl+xml\"/>\n" +
                           "                                <parameter name=\"endpoint.mediatype\" value=\"application/vnd.wso2.endpoint\"/>\n" +
                           "                            </execution>\n" +
                           "\t\t\t    <execution forEvent=\"Demote\" class=\"org.wso2.carbon.governance.registry.extensions.executors.DemoteActionExecutor\">\n" +
                           "                            </execution>\n" +
                           "                        </data>\n" +
                           "\t\t\t<data name=\"transitionUI\">\n" +
                           "                            <ui forEvent=\"Promote\" href=\"../lifecycles/pre_invoke_aspect_ajaxprocessor.jsp?currentEnvironment=/_system/governance/branches/testing/\"/>\n" +
                           "                        </data>\n" +
                           "                        <data name=\"transitionScripts\">\n" +
                           "                            <js forEvent=\"Promote\">\n" +
                           "                                <console function=\"showServiceList\">\n" +
                           "<script type=\"text/javascript\">\n" +
                           "                                        showServiceList = function() { var element = document.getElementById('hidden_media_type'); var mediaType = \"\"; if (element) { mediaType = element.value;} if (mediaType == \"application/vnd.wso2-service+xml\") { location.href = unescape(\"../generic/list.jsp?region=region3%26item=governance_list_service_menu%26key=service%26breadcrumb=Services%26singularLabel=Service%26pluralLabel=Services\"); } }\n" +
                           "</script>\n" +
                           "                                </console>\n" +
                           "                            </js>\n" +
                           "                        </data>\n" +
                           "                    </datamodel>\n" +
                           "                    <transition event=\"Promote\" target=\"Production\"/>\n" +
                           "                    <transition event=\"Demote\" target=\"Development\"/>\n" +
                           "                </state>\n" +
                           "                <state id=\"Production\">\n" +
                           "                    <datamodel>\n" +
                           "                        <data name=\"transitionExecution\">\n" +
                           "                            <execution forEvent=\"Demote\" class=\"org.wso2.carbon.governance.registry.extensions.executors.DemoteActionExecutor\">\n" +
                           "                            </execution>\n" +
                           "                            <execution forEvent=\"Publish\" class=\"org.wso2.carbon.governance.registry.extensions.executors.apistore.ApiStoreExecutor\">\n" +
                           "                            </execution>\n" +
                           "                        </data>\n" +
                           "                    </datamodel>\n" +
                           "                    <transition event=\"Publish\" target=\"Published.to.APIStore\"/>\n" +
                           "                    <transition event=\"Demote\" target=\"Testing\"/>\n" +
                           "                </state>\n" +
                           "                <state id=\"Published.to.APIStore\">\n" +
                           "                </state>                \n" +
                           "            </scxml>\n" +
                           "        </lifecycle>\n" +
                           "    </configuration>\n" +
                           "</aspect>\n" +
                           "\n" +
                           "\n" +
                           "\n" +
                           "\n" +
                           "\n" +
                           "\n" +
                           "";

        String lifeCycleName = "AutomatedLifeCycle";
        lifeCycleHomePage.addNewLifeCycle(lifeCycle);
        lifeCyclesPage.checkOnUploadedLifeCycle(lifeCycleName);
        driver.close();

    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }



}
