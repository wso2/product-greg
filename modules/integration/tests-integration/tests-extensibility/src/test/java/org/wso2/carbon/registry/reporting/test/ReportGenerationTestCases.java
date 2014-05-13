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
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.pdfbox.cos.COSDocument;
import org.pdfbox.pdfparser.PDFParser;
import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.util.PDFTextStripper;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.registry.reporting.stub.beans.xsd.ReportConfigurationBean;

import javax.activation.DataHandler;
import java.io.File;
import java.io.FileInputStream;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class ReportGenerationTestCases extends ReportingTestCaseSuper {

    @BeforeClass
    public void initializeForReportGenerationTesting() throws Exception {
        applicationName = super.applicationName + "ReportGenerationTestCases";
        artifactName = super.artifactName + "ReportGenerationTestCases";
        init();
    }

	/**
	 * Add resources and artifacts to test report generation
	 * 
	 * @throws Exception
	 */
	@Test(groups = "wso2.greg", description = "Add resorces and artifacts to test report generation")
	public void testAddResourcesForReportGenerationTesting() throws Exception {
		testAddResourcesLCReport();
		testAddLCArtifact();
		testAddResourcesApplicationReport();
		testAddApplicationArtifact1();
		createEditedLCtemplate();

        ReportConfigurationBean configurationBean = new ReportConfigurationBean();
        configurationBean.setName("TestGovernanceLCReport");
        configurationBean.setTemplate(testGovernanceLCtemplate);
        configurationBean.setType("HTML");
        configurationBean.setReportClass(testGovernanceLCClass);
        reportAdminServiceClient.saveReport(configurationBean);
    }

    /**
     * verifies report generation with type set to HTML
     *
     * @throws AxisFault
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Generate report bytes with type set to HTML",
          dependsOnMethods = "testAddResourcesForReportGenerationTesting")
    public void testGetReportBytesHTMLtype() throws AxisFault, Exception {
        ReportConfigurationBean configurationBean = reportAdminServiceClient
                .getSavedReport("TestGovernanceLCReport");

        configurationBean.setAttributes(testLCattributes);
        DataHandler report = reportAdminServiceClient
                .getReportBytes(configurationBean);

        String result = readInputStreamAsString(report.getInputStream());

        assertTrue(result.contains("G-regTesting"));
        assertTrue(result.contains("4.5.0"));
        assertTrue(result.contains("Smoke test"));
        assertTrue(result.contains(artifactName + "1"));
    }

    /**
     * verifies report generation with type set to Excel
     *
     * @throws AxisFault
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "verifies report generation with type set to Excel", dependsOnMethods = "testAddResourcesForReportGenerationTesting")
    public void testGetReportExcelType() throws AxisFault, Exception {
        ReportConfigurationBean configurationBean = reportAdminServiceClient
                .getSavedReport("TestGovernanceLCReport");
        configurationBean.setType("Excel");
        configurationBean.setAttributes(testLCattributes);

        DataHandler report = reportAdminServiceClient
                .getReportBytes(configurationBean);

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

    }

    /**
     * verifies report generation with type set to PDF
     *
     * @throws AxisFault
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "verifies report generation with type set to PDF", dependsOnMethods = "testAddResourcesForReportGenerationTesting")
    public void testGetReportPDFType() throws AxisFault, Exception {
        ReportConfigurationBean configurationBean = reportAdminServiceClient
                .getSavedReport("TestGovernanceLCReport");
        configurationBean.setType("PDF");
        configurationBean.setAttributes(testLCattributes);

        DataHandler report = reportAdminServiceClient
                .getReportBytes(configurationBean);

        saveDataHandlerToFile(report);

        File file = new File(Dest_file);
        PDFParser parser = null;
        if (!file.isFile()) {
            String msg = "File " + Dest_file + " does not exist.";
            throw new Exception(msg);
        }

        FileInputStream pdfInputStream = null;

        try {
            pdfInputStream = new FileInputStream(file);
            parser = new PDFParser(pdfInputStream);
        } catch (Exception e) {
            String msg = "Unable to open PDF Parser.";
            throw new Exception(msg, e);
        }

        COSDocument cosDoc = null;
        PDFTextStripper pdfStripper;
        PDDocument pdDoc = null;
        String parsedText = null;
        try {
            parser.parse();
            cosDoc = parser.getDocument();
            pdfStripper = new PDFTextStripper();
            pdDoc = new PDDocument(cosDoc);
            parsedText = pdfStripper.getText(pdDoc);
        } catch (Exception e) {
            String msg = "An exception occured in parsing the PDF Document.";
            e.printStackTrace();
            throw new Exception(msg, e);
        } finally {
            if (cosDoc != null) {
                cosDoc.close();
            }
            if (pdDoc != null) {
                pdDoc.close();
            }
            if (pdfInputStream != null) {
                pdfInputStream.close();
            }
        }

        assertTrue(parsedText.contains(artifactName + "1"));
        assertTrue(parsedText.contains("G-regTesting"));
        assertTrue(parsedText.contains("4.5.0"));
        assertTrue(parsedText.contains("Smoke test"));
    }

    /**
     * Select an existing report resource, select a new jasper report from the
     * existing location and generate the new report to see whether new data is
     * shown
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Genarate report with a existing report configuration and a new template", dependsOnMethods = {
            "testGetReportPDFType", "testGetReportBytesHTMLtype",
            "testGetReportExcelType"})
    public void testGenerateExistingReportWithChangedTemplate() throws Exception {
        ReportConfigurationBean configurationBean =
                reportAdminServiceClient.getSavedReport("TestGovernanceLCReport");
        configurationBean.setTemplate(testGovernanceLCEditedTemplate);
        reportAdminServiceClient.saveReport(configurationBean);

        configurationBean.setAttributes(testLCattributes);
        DataHandler report = reportAdminServiceClient.getReportBytes(configurationBean);
        String result = readInputStreamAsString(report.getInputStream());

        assertTrue(result.contains("Edited_heading"));
        assertTrue(result.contains("Version_Edited"));
        assertTrue(result.contains(artifactName + "1"));
        assertTrue(result.contains("G-regTesting"));
        assertTrue(result.contains("4.5.0"));
        assertTrue(result.contains("Smoke test"));
    }

    /**
     * Edit an existing report, and generate a report & verify whether the
     * correct reports are generated
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Edit an existing report, and generate", dependsOnMethods = "testGenerateExistingReportWithChangedTemplate")
    public void testEditConfigAndGenerate() throws Exception {
        ReportConfigurationBean configurationBean = reportAdminServiceClient
                .getSavedReport("TestGovernanceLCReport");

        configurationBean.setTemplate(applicationTemplate);
        configurationBean.setType("HTML");
        configurationBean.setReportClass(applicationClass);

        reportAdminServiceClient.saveReport(configurationBean);

        DataHandler report = reportAdminServiceClient
                .getReportBytes(configurationBean);

        String result = readInputStreamAsString(report.getInputStream());

        assertTrue(result.contains(applicationName + "1"));
        assertTrue(result.contains("4.5.0"));
        assertTrue(result.contains("Description"));
    }

    /**
     * Change the existing resource content, generate the report and verify
     * whether updated data can be seen in the report
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Edit report content, and generate", dependsOnMethods = "testEditConfigAndGenerate")
    public void testGenerateReportWith2Contents() throws Exception {
        testAddApplicationArtifact2();
        ReportConfigurationBean configurationBean = reportAdminServiceClient
                .getSavedReport("TestGovernanceLCReport");

        DataHandler report = reportAdminServiceClient
                .getReportBytes(configurationBean);

        String result = readInputStreamAsString(report.getInputStream());

        assertTrue(result.contains(applicationName + "1"));
        assertTrue(result.contains("4.5.0"));
        assertTrue(result.contains("Description"));
        assertTrue(result.contains(applicationName + "2"));
        assertTrue(result.contains("4.6.0"));
        assertTrue(result.contains("Description2"));
    }

    /**
     * Generate the report and verify whether All artifacts are listed in the
     * report
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Add report content, and generate", dependsOnMethods = "testGenerateReportWith2Contents")
    public void testGenerateReportWithMoreContents() throws Exception {
        testAddApplicationArtifacts();
        ReportConfigurationBean configurationBean = reportAdminServiceClient
                .getSavedReport("TestGovernanceLCReport");

        DataHandler report = reportAdminServiceClient
                .getReportBytes(configurationBean);

        String result = readInputStreamAsString(report.getInputStream());

        assertTrue(result.contains(applicationName + "1"));
        assertTrue(result.contains("4.5.0"));
        assertTrue(result.contains("Description"));
        assertTrue(result.contains(applicationName + "2"));
        assertTrue(result.contains("4.6.0"));
        assertTrue(result.contains("Description2"));
        assertTrue(result.contains(applicationName + "3"));
        assertTrue(result.contains("4.7.0"));
        assertTrue(result.contains("Description3"));
        assertTrue(result.contains(applicationName + "4"));
        assertTrue(result.contains("4.8.0"));
        assertTrue(result.contains("Description4"));
    }

    /**
     * Generate the report and verify whether report data has changed according
     * to the previous operations in the report
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Delete report content, and generate", dependsOnMethods = "testGenerateReportWithMoreContents")
    public void testGenerateReportWithDeletedContents() throws Exception {
        removeAppicationArtifact(2);
        removeAppicationArtifact(4);
        ReportConfigurationBean configurationBean = reportAdminServiceClient
                .getSavedReport("TestGovernanceLCReport");

        DataHandler report = reportAdminServiceClient
                .getReportBytes(configurationBean);

        String result = readInputStreamAsString(report.getInputStream());

        assertTrue(result.contains(applicationName + "1"));
        assertTrue(result.contains("4.5.0"));
        assertTrue(result.contains("Description"));
        assertTrue(!result.contains(applicationName + "2"));
        assertTrue(!result.contains("4.6.0"));
        assertTrue(!result.contains("Description2"));
        assertTrue(result.contains(applicationName + "3"));
        assertTrue(result.contains("4.7.0"));
        assertTrue(result.contains("Description3"));
        assertTrue(!result.contains(applicationName + "4"));
        assertTrue(!result.contains("4.8.0"));
        assertTrue(!result.contains("Description4"));
    }

    /**
     * Select an existing report and try to generate a report while the Jasper
     * report has been removed
     * <p/>
     * For this case exception is thrown and logged in the back end only and
     * empty report is generated and sent to the front end
     * This  issue is reported in public jira at https://wso2.org/jira/browse/REGISTRY-1083
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Delete report content, and generate", dependsOnMethods = "testGenerateReportWithDeletedContents")
    public void testGenerateReportUnavailableTemplate() throws Exception {

        resourceAdminServiceClient.deleteResource(applicationTemplate);

        ReportConfigurationBean[] configurationBeans = reportAdminServiceClient
                .getSavedReports();

        boolean assertVal = true;

        if (configurationBeans != null) {
            for (int i = 0; i < configurationBeans.length; i++) {
                if (configurationBeans[i].getName().equals("TestGovernanceLCReport")) {
                    assertVal = false;
                }
            }
        }

        assertFalse(assertVal);
    }

    @AfterClass
    public void ClearResourcesAddedForReportGenerationTesting() throws Exception {
        removeLCArtifact();
        removeResourcesLCReport();
        removeAppicationArtifact(1);
        removeAppicationArtifact(3);
        removeEditedLCtemplate();
        removeTemplateCollection();

        resourceAdminServiceClient.deleteResource(applicationRXT);
        resourceAdminServiceClient.removeExtension(applicationJAR);

        removeAllReports();
        deleteDestiationFile();

        clear();
    }
}
