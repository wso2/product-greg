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

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.registry.reporting.stub.beans.xsd.ReportConfigurationBean;

import javax.activation.DataHandler;
import java.io.FileInputStream;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class ReportCopyingTestCases extends ReportingTestCaseSuper {

    @BeforeClass
    public void initializeToTestCopying() throws Exception {
        applicationName = super.applicationName + "ReportCopyingTestCases";
        artifactName = super.artifactName + "ReportCopyingTestCases";
        init();
    }

    /**
     * Add resources and artifacts to test report copying
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Add resorces and artifacts to test report scheduling")
    public void testAddResourcesToTestCopying() throws Exception {
        testAddResourcesLCReport();
        testAddLCArtifact();
        testAddResourcesApplicationReport();
        testAddApplicationArtifact1();

        ReportConfigurationBean configurationBean = new ReportConfigurationBean();
        configurationBean.setName("TestGovernanceLCReport");
        configurationBean.setTemplate(testGovernanceLCtemplate);
        configurationBean.setType("HTML");
        configurationBean.setReportClass(testGovernanceLCClass);
        reportAdminServiceClient.saveReport(configurationBean);
    }

    /**
     * Copy an existing report and verify
     * - whether the new resource generates the report successfully
     * - whether the existing resource generates the report successfully as
     * before
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Copy an existing report", dependsOnMethods = "testAddResourcesToTestCopying")
    public void testCopyReportConfig() throws Exception {
        reportAdminServiceClient.copySavedReport("TestGovernanceLCReport",
                                                 "TestGovernanceLCReportCopy");
        ReportConfigurationBean configurationBeanOriginal =
                reportAdminServiceClient.getSavedReport("TestGovernanceLCReport");
        ReportConfigurationBean configurationBeanCopy =
                reportAdminServiceClient.getSavedReport("TestGovernanceLCReportCopy");

        assertEquals(configurationBeanOriginal.getTemplate(), configurationBeanCopy.getTemplate());
        assertEquals(configurationBeanOriginal.getType(), configurationBeanCopy.getType());
        assertEquals(configurationBeanOriginal.getClass(), configurationBeanCopy.getClass());

        configurationBeanOriginal.setAttributes(testLCattributes);

        DataHandler report = reportAdminServiceClient.getReportBytes(configurationBeanOriginal);

        String result = readInputStreamAsString(report.getInputStream());

        assertTrue(result.contains("G-regTesting"));
        assertTrue(result.contains("4.5.0"));
        assertTrue(result.contains("Smoke test"));

        configurationBeanCopy.setAttributes(testLCattributes);

        report = reportAdminServiceClient.getReportBytes(configurationBeanCopy);

        result = readInputStreamAsString(report.getInputStream());

        assertTrue(result.contains("G-regTesting"));
        assertTrue(result.contains("4.5.0"));
        assertTrue(result.contains("Smoke test"));
    }

    /**
     * Copy an existing report then change its report type and verify
     * - whether the new resource generates the report successfully
     * - whether the existing resource generates the report successfully as
     * before
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Copy an existing report then change its report type", dependsOnMethods = "testCopyReportConfig")
    public void testCopyReportConfigChangeType() throws Exception {
        ReportConfigurationBean configurationBeanOriginal =
                reportAdminServiceClient.getSavedReport("TestGovernanceLCReport");
        ReportConfigurationBean configurationBeanCopy =
                reportAdminServiceClient.getSavedReport("TestGovernanceLCReportCopy");

        configurationBeanCopy.setType("Excel");

        reportAdminServiceClient.saveReport(configurationBeanCopy);

        configurationBeanCopy.setAttributes(testLCattributes);

        DataHandler report = reportAdminServiceClient.getReportBytes(configurationBeanCopy);

        saveDataHandlerToFile(report);

        try {
            FileInputStream myInput = new FileInputStream(Dest_file);

            POIFSFileSystem myFileSystem = new POIFSFileSystem(myInput);

            HSSFWorkbook myWorkBook = new HSSFWorkbook(myFileSystem);

            HSSFSheet mySheet = myWorkBook.getSheetAt(0);

            HSSFRow customRow = mySheet.getRow(9);
            HSSFCell customCell = customRow.getCell(2);
            assertTrue(customCell.getStringCellValue().equals("G-regTesting"));

            customCell = customRow.getCell(5);
            assertTrue(customCell.getStringCellValue().equals(artifactName + "1"));

            customCell = customRow.getCell(8);
            assertTrue(customCell.getStringCellValue().equals("4.5.0"));

            customCell = customRow.getCell(12);
            assertTrue(customCell.getStringCellValue().equals("Smoke test"));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        configurationBeanOriginal.setAttributes(testLCattributes);

        report = reportAdminServiceClient.getReportBytes(configurationBeanOriginal);

        String result = readInputStreamAsString(report.getInputStream());

        assertTrue(result.contains("G-regTesting"));
        assertTrue(result.contains("4.5.0"));
        assertTrue(result.contains("Smoke test"));
    }

    /**
     * Copy a report with a name which already exists and verify error handling
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Add Report config with a name with spaces", expectedExceptions = java.lang.Exception.class, dependsOnMethods = "testCopyReportConfigChangeType")
    public void testCopyReportConfigToExistingName() throws Exception {
        reportAdminServiceClient.copySavedReport("TestGovernanceLCReport",
                                                 "TestGovernanceLCReportCopy");
    }

    /**
     * Copy an existing report then change its template,Class and verify
     * - whether the new resource generates the report successfully
     * - whether the existing resource generates the report successfully as
     * before
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Copy an existing report then change its template,Class", dependsOnMethods = "testCopyReportConfigChangeType")
    public void testCopyReportConfigChangeTemplateAndClass() throws Exception {
        reportAdminServiceClient.copySavedReport("TestGovernanceLCReport",
                                                 "TestGovernanceLCReportCopy1");
        ReportConfigurationBean configurationBeanOriginal =
                reportAdminServiceClient.getSavedReport("TestGovernanceLCReport");
        ReportConfigurationBean configurationBeanCopy =
                reportAdminServiceClient.getSavedReport("TestGovernanceLCReportCopy1");

        configurationBeanCopy.setTemplate(applicationTemplate);
        configurationBeanCopy.setReportClass(applicationClass);

        reportAdminServiceClient.saveReport(configurationBeanCopy);

        configurationBeanOriginal.setAttributes(testLCattributes);

        DataHandler report = reportAdminServiceClient.getReportBytes(configurationBeanOriginal);

        String result = readInputStreamAsString(report.getInputStream());

        assertTrue(result.contains("G-regTesting"));
        assertTrue(result.contains("4.5.0"));
        assertTrue(result.contains("Smoke test"));

        report = reportAdminServiceClient.getReportBytes(configurationBeanCopy);

        result = readInputStreamAsString(report.getInputStream());

        assertTrue(result.contains(applicationName + "1"));
        assertTrue(result.contains("4.5.0"));
        assertTrue(result.contains("Description"));
    }

    @AfterClass
    public void ClearResourcesAddedToTestCopying() throws Exception {
        removeLCArtifact();
        removeResourcesLCReport();
        removeAppicationArtifact(1);
        removeResourcesAppicationReport();
        removeTemplateCollection();
        removeAllReports();
        deleteDestiationFile();

        clear();
    }
}
