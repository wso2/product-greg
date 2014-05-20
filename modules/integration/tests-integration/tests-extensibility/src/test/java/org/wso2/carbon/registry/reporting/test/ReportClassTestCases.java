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

package org.wso2.carbon.registry.reporting.test;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.logging.view.stub.types.carbon.LogEvent;
import org.wso2.carbon.registry.reporting.stub.beans.xsd.ReportConfigurationBean;

import static org.testng.Assert.assertEquals;

public class ReportClassTestCases extends ReportingTestCaseSuper {

    @BeforeClass
    public void initializeForReportClassTesting() throws Exception {
        applicationName = super.applicationName + "ReportClassTestCases";
        artifactName = super.artifactName + "ReportClassTestCases";
        init();
    }

    /**
     * Add resources and artifacts to test report classes
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Add resorces and artifacts to test report scheduling")
    public void testAddResourcesForReportClassTesting() throws Exception {
        testAddResourcesLCReport();
        testAddResourcesApplicationReport();
        testAddApplicationArtifact1();
    }

    /**
     * testing attribute fetching
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "testing attribute fetching ", dependsOnMethods = "testAddResourcesForReportClassTesting")
    public void testgetAttributeNames() throws Exception {
        String[] attributenames = reportAdminServiceClient
                .getAttributeNames(testGovernanceLCClass);

        assertEquals(attributenames.length, 2,
                     "Fetched number of attributes is wrong");
        assertEquals(attributenames[0], "responsibleQA", "Attribute mismatch");
        assertEquals(attributenames[1], "responsibleQAA", "Attribute mismatch");
    }

    /**
     * testing mandatory attribute fetching
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "testing mandatory attribute fetching ", dependsOnMethods = "testAddResourcesForReportClassTesting")
    public void testgetMandatoryAttributeNames() throws Exception {
        String[] mandatoryAttributenames = reportAdminServiceClient
                .getMandatoryAttributeNames(testGovernanceLCClass);

        assertEquals(mandatoryAttributenames.length, 1,
                     "Fetched number of mandatory attributes is wrong");
        assertEquals(mandatoryAttributenames[0], "responsibleQAA",
                     "Attribute mismatch");
    }

    /**
     * testing attribute fetching of an invalid class name
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "testing attribute fetching of an invalid class name ", expectedExceptions = java.lang.Exception.class, dependsOnMethods = "testAddResourcesForReportClassTesting")
    public void testgetAttributeNamesWIthInvalidClass() throws Exception {
        reportAdminServiceClient
                .getAttributeNames("org.wso2.carbon.registry.samples.reporting.TestingLCReportGenerator1");
    }

    /**
     * feild mismatches in template and Class
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "filed mismatches in template and Class", dependsOnMethods = "testAddResourcesForReportClassTesting")
    public void testTemplateAndClassMismatch() throws Exception {
        ReportConfigurationBean configurationBean = new ReportConfigurationBean();
        configurationBean.setName("TestGovernanceLCReport1");
        configurationBean.setTemplate(testGovernanceLCtemplate);
        configurationBean.setType("HTML");
        configurationBean.setReportClass(applicationClass);

        reportAdminServiceClient.getReportBytes(configurationBean);

        LogEvent[] logEvents = logViewerClient.getLogs("ERROR", "net.sf.jasperreports.engine.JRException: " +
                                                                "Error retrieving field value from bean : " +
                                                                "details_govCycleName", "", "");

        assertEquals(
                logEvents[0].getMessage(),
                "net.sf.jasperreports.engine.JRException: Error retrieving field value from bean : details_govCycleName");
    }

    @AfterClass
    public void ClearResourcesAddedForReportClassTesting() throws Exception {
        removeResourcesLCReport();
        removeAppicationArtifact(1);
        removeResourcesAppicationReport();
        removeTemplateCollection();
        removeAllReports();
        clear();
    }
}
