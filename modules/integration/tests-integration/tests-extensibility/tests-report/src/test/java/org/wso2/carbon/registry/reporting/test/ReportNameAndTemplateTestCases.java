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

import org.apache.axis2.AxisFault;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.logging.view.stub.types.carbon.LogEvent;
import org.wso2.carbon.registry.reporting.stub.beans.xsd.ReportConfigurationBean;

import javax.activation.DataHandler;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class ReportNameAndTemplateTestCases extends ReportingTestCaseSuper {

    @BeforeClass(alwaysRun = true)
    public void initializeForTesting() throws Exception {
        applicationName = super.applicationName + "RNAndTTestCases";
        artifactName = super.artifactName + "RNAndTTestCases";
        init();
    }

    /**
     * Add resources and artifacts for test cases on report name and templates
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Add resources and artifacts to test report scheduling")
    public void testAddResourcesForTesting() throws Exception {
        testAddResourcesLCReport();
        testAddLCArtifact();
        testAddLCTemplateAnyLocation();
        createEditedLCtemplate();
        createMismatchLCtemplate();
        testAddInvalidLCTemplate();
        testAddResourcesApplicationReport();
        testAddApplicationArtifact1();
    }

    /**
     * Save report configuration and assert
     *
     * @throws Exception
     */

    @Test(groups = "wso2.greg", description = "Save Report configuration", dependsOnMethods = "testAddResourcesForTesting")
    public void testSaveReport() throws Exception {
        ReportConfigurationBean configurationBean = new ReportConfigurationBean();
        configurationBean.setName("TestGovernanceLCReport");
        configurationBean.setTemplate(testGovernanceLCtemplate);
        configurationBean.setType("HTML");
        configurationBean.setReportClass(testGovernanceLCClass);
        reportAdminServiceClient.saveReport(configurationBean);

        org.wso2.carbon.registry.common.beans.ReportConfigurationBean configurationCommonBean = super
                .createCommonBean(configurationBean);

        ReportConfigurationBean retrievedBean = reportAdminServiceClient
                .getSavedReport("TestGovernanceLCReport");
        org.wso2.carbon.registry.common.beans.ReportConfigurationBean retrievedCommonBean = super
                .createCommonBean(retrievedBean);

        assertEquals(configurationCommonBean, retrievedCommonBean);
    }

    /**
     * Add report configuration with a name with spaces and assert
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Add Report config with a name with spaces", dependsOnMethods = "testSaveReport")
    public void testSaveReportNameWithSpaces() throws Exception {
        ReportConfigurationBean configurationBean = new ReportConfigurationBean();
        configurationBean.setName("Test Governance Life Cycle Report");
        configurationBean.setTemplate(testGovernanceLCtemplate);
        configurationBean.setType("HTML");
        configurationBean.setReportClass(testGovernanceLCClass);
        reportAdminServiceClient.saveReport(configurationBean);

        org.wso2.carbon.registry.common.beans.ReportConfigurationBean configurationCommonBean = super
                .createCommonBean(configurationBean);

        ReportConfigurationBean retrievedBean = reportAdminServiceClient
                .getSavedReport("Test Governance Life Cycle Report");
        org.wso2.carbon.registry.common.beans.ReportConfigurationBean retrievedCommonBean = super
                .createCommonBean(retrievedBean);

        assertEquals(configurationCommonBean, retrievedCommonBean);
    }

    /**
     * verifies report generation with type set to HTML
     *
     * @throws AxisFault
     * @throws Exception
     */
    @Test(groups = "wso2.greg", dataProvider = "reportName", description = "Generate report bytes with type set to HTML", dependsOnMethods = "testSaveReportNameWithSpaces")
    public void testGetReportBytes(String reportName) throws AxisFault,
                                                             Exception {
        ReportConfigurationBean configurationBean = reportAdminServiceClient
                .getSavedReport(reportName);

        configurationBean.setAttributes(testLCattributes);

        DataHandler report = reportAdminServiceClient
                .getReportBytes(configurationBean);

        String result = readInputStreamAsString(report.getInputStream());
        log.info("testGetReportBytes result : " + result);
        assertTrue(result.contains("G-regTesting"));
        assertTrue(result.contains("4.5.0"));
        assertTrue(result.contains("Smoke test"));
    }

    /**
     * Add report with a name which already exists and assert
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Add Report with a name which already exists", dependsOnMethods = "testGetReportBytes")
    public void testSaveReportNameWhichExist() throws Exception {
        ReportConfigurationBean configurationBean = new ReportConfigurationBean();
        configurationBean.setName("TestGovernanceLCReport");
        configurationBean.setTemplate(applicationTemplate);
        configurationBean.setType("HTML");
        configurationBean.setReportClass(applicationClass);
        reportAdminServiceClient.saveReport(configurationBean);

        org.wso2.carbon.registry.common.beans.ReportConfigurationBean configurationCommonBean = createCommonBean(configurationBean);

        ReportConfigurationBean retrievedBean = reportAdminServiceClient
                .getSavedReport("TestGovernanceLCReport");
        org.wso2.carbon.registry.common.beans.ReportConfigurationBean retrievedCommonBean = createCommonBean(retrievedBean);

        assertEquals(configurationCommonBean, retrievedCommonBean);
    }

    /**
     * Add report without a name and assert
     *
     * @throws Exception
     */

    @Test(groups = "wso2.greg", description = "Add Report without a name", expectedExceptions = java.lang.Exception.class, dependsOnMethods = "testSaveReportNameWhichExist")
    public void testSaveReportWithoutName() throws Exception {
        ReportConfigurationBean configurationBean = new ReportConfigurationBean();
        configurationBean.setName("");
        configurationBean.setTemplate(testGovernanceLCtemplate);
        configurationBean.setType("HTML");
        configurationBean.setReportClass(testGovernanceLCClass);
        reportAdminServiceClient.saveReport(configurationBean);
    }

    /**
     * Add a report with a name which contains special characters
     * (~!@#;%^*+={}|<>,'"\)
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Add Report with a name which contains special characters", expectedExceptions = java.lang.Exception.class, dependsOnMethods = "testSaveReportWithoutName")
    public void testSaveReportNameSpecialCharacter() throws Exception {
        ReportConfigurationBean configurationBean = new ReportConfigurationBean();
        configurationBean.setName("Test~Governance@L#CReport");
        configurationBean.setTemplate(testGovernanceLCtemplate);
        configurationBean.setType("HTML");
        configurationBean.setReportClass(testGovernanceLCClass);
        reportAdminServiceClient.saveReport(configurationBean);

        org.wso2.carbon.registry.common.beans.ReportConfigurationBean configurationCommonBean = createCommonBean(configurationBean);

        ReportConfigurationBean retrievedBean = reportAdminServiceClient
                .getSavedReport("Test~Governance@L#CReport");
        org.wso2.carbon.registry.common.beans.ReportConfigurationBean retrievedCommonBean = createCommonBean(retrievedBean);

        assertEquals(configurationCommonBean, retrievedCommonBean);
    }

    /**
     * Add a report with a template that resides under any other location &
     * verify whether report is generated
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Add Report with a template that resides under any location", dependsOnMethods = {
            "testSaveReportWithoutName", "testSaveReportNameSpecialCharacter"})
    public void testSaveReportWithAnyTemplateLocation() throws Exception {
        ReportConfigurationBean configurationBean = new ReportConfigurationBean();
        configurationBean.setName("TestGovernanceLCReport");
        configurationBean.setTemplate(testGovernanceLCtemplateAnyLocation);
        configurationBean.setType("HTML");
        configurationBean.setReportClass(testGovernanceLCClass);
        reportAdminServiceClient.saveReport(configurationBean);

        org.wso2.carbon.registry.common.beans.ReportConfigurationBean configurationCommonBean = createCommonBean(configurationBean);

        ReportConfigurationBean retrievedBean = reportAdminServiceClient
                .getSavedReport("TestGovernanceLCReport");
        org.wso2.carbon.registry.common.beans.ReportConfigurationBean retrievedCommonBean = createCommonBean(retrievedBean);

        assertEquals(configurationCommonBean, retrievedCommonBean);

        configurationBean.setAttributes(testLCattributes);

        DataHandler report = reportAdminServiceClient
                .getReportBytes(configurationBean);

        String result = readInputStreamAsString(report.getInputStream());

        assertTrue(result.contains(artifactName + "1"));
        assertTrue(result.contains("G-regTesting"));
        assertTrue(result.contains("4.5.0"));
        assertTrue(result.contains("Smoke test"));
    }

    /**
     * update template programaticaly and verify report generation
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Add Report with a programaticaly updated template", dependsOnMethods = "testSaveReportWithAnyTemplateLocation")
    public void testGenerateReportWithUpdatedTemplate() throws Exception {
        ReportConfigurationBean configurationBean = new ReportConfigurationBean();
        configurationBean.setName("TestGovernanceLCReport");
        configurationBean
                .setTemplate("/_system/governance/repository/components/org.wso2.carbon.governance/templates/TestGovernanceLCEdited.jrxml");
        configurationBean.setType("HTML");
        configurationBean.setReportClass(testGovernanceLCClass);
        reportAdminServiceClient.saveReport(configurationBean);

        org.wso2.carbon.registry.common.beans.ReportConfigurationBean configurationCommonBean = createCommonBean(configurationBean);

        ReportConfigurationBean retrievedBean = reportAdminServiceClient
                .getSavedReport("TestGovernanceLCReport");
        org.wso2.carbon.registry.common.beans.ReportConfigurationBean retrievedCommonBean = createCommonBean(retrievedBean);

        assertEquals(configurationCommonBean, retrievedCommonBean);

        configurationBean.setAttributes(testLCattributes);
        DataHandler report = reportAdminServiceClient
                .getReportBytes(configurationBean);
        String result = readInputStreamAsString(report.getInputStream());

        assertTrue(result.contains("Edited_heading"));
        assertTrue(result.contains("Version_Edited"));
        assertTrue(result.contains(artifactName + "1"));
        assertTrue(result.contains("G-regTesting"));
        assertTrue(result.contains("4.5.0"));
        assertTrue(result.contains("Smoke test"));
    }

    /**
     * Add a report with a template which is invalid and verify error handling
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Add Report with a invalid template", dependsOnMethods = "testGenerateReportWithUpdatedTemplate")
    public void testSaveReportWithInvalidTemplate() throws Exception {
        ReportConfigurationBean configurationBean = new ReportConfigurationBean();
        configurationBean.setName("TestGovernanceLCReport");
        configurationBean.setTemplate(testGovernanceLCInvalidtemplate);
        configurationBean.setType("HTML");
        configurationBean.setReportClass(testGovernanceLCClass);
        reportAdminServiceClient.saveReport(configurationBean);

        org.wso2.carbon.registry.common.beans.ReportConfigurationBean configurationCommonBean = createCommonBean(configurationBean);

        ReportConfigurationBean retrievedBean = reportAdminServiceClient
                .getSavedReport("TestGovernanceLCReport");
        org.wso2.carbon.registry.common.beans.ReportConfigurationBean retrievedCommonBean = createCommonBean(retrievedBean);

        assertEquals(configurationCommonBean, retrievedCommonBean);

        configurationBean.setAttributes(testLCattributes);

        reportAdminServiceClient.getReportBytes(configurationBean);

        LogEvent[] logEvents = logViewerClient
                .getLogs("ERROR", "Parse Fatal Error at line 39 column 48: The element type \"text\" " +
                                  "must be terminated by the matching end-tag \"</text>\".", "", "");

        assertEquals(
                logEvents[0].getMessage(),
                "Parse Fatal Error at line 39 column 48: The element type \"text\" must be terminated by the matching end-tag \"</text>\".");

    }

    /**
     * feild mismatches in templates and RXTs
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "feild mismatches in templates and RXTs", dependsOnMethods = "testSaveReportWithInvalidTemplate")
    public void testFieldMismatch() throws Exception {
        ReportConfigurationBean configurationBean = new ReportConfigurationBean();
        configurationBean.setName("TestGovernanceLCReport");
        configurationBean.setTemplate(testGovernanceLCMismatchtemplate);
        configurationBean.setType("HTML");
        configurationBean.setReportClass(testGovernanceLCClass);
        reportAdminServiceClient.saveReport(configurationBean);

        org.wso2.carbon.registry.common.beans.ReportConfigurationBean configurationCommonBean = createCommonBean(configurationBean);

        ReportConfigurationBean retrievedBean = reportAdminServiceClient
                .getSavedReport("TestGovernanceLCReport");
        org.wso2.carbon.registry.common.beans.ReportConfigurationBean retrievedCommonBean = createCommonBean(retrievedBean);

        assertEquals(configurationCommonBean, retrievedCommonBean);

        configurationBean.setAttributes(testLCattributes);
        reportAdminServiceClient.getReportBytes(configurationBean);

        LogEvent[] logEvents =
                logViewerClient.getLogs("ERROR", "net.sf.jasperreports.engine.JRException: Error " +
                                                 "retrieving field value from bean : details_version1" ,"","");

        assertEquals(logEvents[0].getMessage(),
                     "net.sf.jasperreports.engine.JRException: Error retrieving field value from bean : details_version1");
    }

    /**
     * Delete an existing report & search for the report
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Delete Report", dependsOnMethods = "testFieldMismatch")
    public void testDeleteReport() throws Exception

    {
        reportAdminServiceClient.deleteSavedReport("TestGovernanceLCReport");

        ReportConfigurationBean retrievedBean[] = reportAdminServiceClient
                .getSavedReports();

        boolean assertVal = true; // assertVal remains "true" if deleted report
        // configuration does not exist

        if (retrievedBean != null) {
            for (int i = 0; i < retrievedBean.length; i++) {
                ReportConfigurationBean reportConfigurationBean = retrievedBean[i];
                if (reportConfigurationBean.getName().equals(
                        "TestGovernanceLCReport")) {
                    assertVal = false;
                    break;
                }
            }
        }

        assertTrue(assertVal);
    }

    /**
     * Delete an existing report, add a new report from the same name but with
     * different report templates & classes and verify whether correct report is
     * generated
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Delete and Add Report with different configuration", dependsOnMethods = "testDeleteReport")
    public void testDeleteANDAddReport() throws Exception

    {
        reportAdminServiceClient
                .deleteSavedReport("Test Governance Life Cycle Report");

        ReportConfigurationBean configurationBean = new ReportConfigurationBean();
        configurationBean.setName("Test Governance Life Cycle Report");
        configurationBean.setTemplate(applicationTemplate);
        configurationBean.setType("HTML");
        configurationBean.setReportClass(applicationClass);
        reportAdminServiceClient.saveReport(configurationBean);

        org.wso2.carbon.registry.common.beans.ReportConfigurationBean configurationCommonBean = createCommonBean(configurationBean);

        ReportConfigurationBean retrievedBean = reportAdminServiceClient
                .getSavedReport("Test Governance Life Cycle Report");
        org.wso2.carbon.registry.common.beans.ReportConfigurationBean retrievedCommonBean = createCommonBean(retrievedBean);

        assertEquals(configurationCommonBean, retrievedCommonBean);

        DataHandler report = reportAdminServiceClient
                .getReportBytes(retrievedBean);

        String result = readInputStreamAsString(report.getInputStream());

        assertTrue(result.contains(applicationName + "1"));
        assertTrue(result.contains("4.5.0"));
        assertTrue(result.contains("Description"));
    }

    @DataProvider(name = "reportName")
    public Object[][] reportName() {
        return new Object[][]{{"TestGovernanceLCReport"},
                              {"Test Governance Life Cycle Report"}};
    }

    @AfterClass(alwaysRun = true)
    public void ClearResourcesAddedForTesting() throws Exception {
        removeLCArtifact();
        removeResourcesLCReport();
        removeAppicationArtifact(1);
        removeResourcesAppicationReport();
        removeLCTemplateAnyLocation();
        removeEditedLCtemplate();
        removeMismatchLCtemplate();
        removeInvalidLCTemplate();
        removeTemplateCollection();
        removeAllReports();
        clear();
    }
}
