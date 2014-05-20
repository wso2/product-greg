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

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
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
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.registry.activities.stub.RegistryExceptionException;
import org.wso2.carbon.registry.activity.search.bean.ActivityReportBean;
import org.wso2.carbon.registry.activity.search.utils.ActivitySearchUtil;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.reporting.api.ReportingException;
import org.wso2.carbon.reporting.stub.ReportingResourcesSupplierReportingExceptionException;
import org.wso2.carbon.reporting.ui.BeanCollectionReportData;
import org.wso2.carbon.reporting.util.JasperPrintProvider;
import org.wso2.carbon.reporting.util.ReportParamMap;
import org.wso2.carbon.reporting.util.ReportStream;
import org.wso2.greg.integration.common.clients.*;

import javax.activation.DataHandler;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class Carbon11686 extends ReportingTestCaseSuper {

    private ReportResourceSupplierClient reportResourceSupplierClient;
    private ActivityAdminServiceClient activityAdminServiceClient;
    private UserManagementClient userManagementClient;
    private String[] activities;
    private String userNameRandom = "random1";
    private String passwordRandom = "random1";
    private final static int waitTime = 120; //time to wait till activities are fetched

    private String sessionCookie;
    private String backEndUrl;
    private String userName;
    private String userNameWithoutDomain;

    @BeforeClass(groups = {"wso2.greg"})
    public void initializeForActivityReportTesting() throws Exception {

        applicationName = super.applicationName + "Carbon11686";
        artifactName = super.artifactName + "Carbon11686";

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        backEndUrl = getBackendURL();
        sessionCookie = getSessionCookie();
        userName = automationContext.getContextTenant().getContextUser().getUserName();
        
        if (userName.contains("@"))
            userNameWithoutDomain = userName.substring(0, userName.indexOf('@'));
        else
            userNameWithoutDomain = userName;

        userManagementClient = new UserManagementClient(backEndUrl,
                                                        sessionCookie);

        String[] roles = {"testRoleCarbon11686"};

        if (!userManagementClient.userNameExists(roles[0], userNameRandom)) {
            userManagementClient.addRole(roles[0],null, null);
            userManagementClient.addUser(userNameRandom, passwordRandom, roles, null);
        }

        String randomUserSession = new AuthenticatorClient(backEndUrl).
                login(userNameRandom, passwordRandom,
                        automationContext.getInstance().getHosts().get("default"));

        activityAdminServiceClient =
                new ActivityAdminServiceClient(backEndUrl,
                                               randomUserSession);
        reportResourceSupplierClient =
                new ReportResourceSupplierClient(backEndUrl,
                                                 randomUserSession);
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(backEndUrl,
                                               randomUserSession);
    }

    /**
     * Add resources and artifacts to test Activity report generation
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Add resource and artifacts to test report scheduling")
    public void testAddResourcesForActivityReportTesting() throws Exception {
        String resourcePath = FrameworkPathUtil.getSystemResourceLocation()
                              + "artifacts" + File.separator + "GREG" + File.separator
                              + "reports" + File.separator + "TestGovernanceLC.jrxml";

        DataHandler dh = new DataHandler(new URL("file:///" + resourcePath));
        resourceAdminServiceClient.addResource(testGovernanceLCtemplate,
                                               "application/xml", "TstDec", dh);

        assertTrue(resourceAdminServiceClient
                           .getResource(testGovernanceLCtemplate)[0].getAuthorUserName()
                           .contains(userNameWithoutDomain));

        resourcePath = FrameworkPathUtil.getSystemResourceLocation()
                       + "artifacts" + File.separator + "GREG" + File.separator
                       + "rxt" + File.separator + "TestGovernanceCycle.rxt";

        dh = new DataHandler(new URL("file:///" + resourcePath));
        resourceAdminServiceClient.addResource(testGovernanceLCRXT,
                                               "application/vnd.wso2.registry-ext-type+xml", "TstDec", dh);

        String addUser = resourceAdminServiceClient.getResource(testGovernanceLCRXT)[0]
                .getAuthorUserName();
        assertTrue(userNameWithoutDomain.equals(addUser), "user name was " + addUser);
    }

    /**
     * search and retrieve activities recorded in registry
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "get activities from the registry",
          dependsOnMethods = "testAddResourcesForActivityReportTesting")
    public void testGetActivities() throws Exception {
        activities = activityAdminServiceClient.getActivities
                (sessionCookie, userNameWithoutDomain, "", "", "", "", 0).getActivity();
        long startTime = new Date().getTime();
        long endTime = startTime + waitTime * 1000;
        while (activities == null) {
            activities = activityAdminServiceClient.getActivities
                    (sessionCookie, userNameWithoutDomain, "", "", "", "", 0).getActivity();
            if ((new Date().getTime()) >= endTime) {
                break;
            }
        }

        assertNotNull(activities);
    }

    /**
     * verifies Activity report generation with PDF type
     *
     * @throws Exception
     * @throws MalformedURLException
     * @throws ResourceAdminServiceExceptionException
     *
     * @throws RemoteException
     * @throws ReportingResourcesSupplierReportingExceptionException
     *
     * @throws ReportingException
     * @throws JRException
     * @throws RegistryExceptionException
     */
    @Test(groups = "wso2.greg", description = "verifies Activity report generation with PDF type",
          dependsOnMethods = "testGetActivities")
    public void testActivityReportPDF() throws Exception, MalformedURLException,
                                               ResourceAdminServiceExceptionException,
                                               RemoteException,
                                               ReportingResourcesSupplierReportingExceptionException,
                                               ReportingException, JRException,
                                               RegistryExceptionException {
        ByteArrayOutputStream report = getReportOutputStream("pdf");

        assertNotNull(report);

        saveByteArrayOutputStreamtoFile(report);
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

        parsedText = parsedText.replace("\n", "");

        assertTrue(parsedText.contains(userNameWithoutDomain));
        assertTrue(parsedText.contains("hasadded the resource"));
        assertTrue(parsedText.contains(testGovernanceLCtemplate));
        assertTrue(parsedText.contains(testGovernanceLCRXT));
    }

    /**
     * verifies Activity report generation with HTML type
     *
     * @throws Exception
     * @throws MalformedURLException
     * @throws ResourceAdminServiceExceptionException
     *
     * @throws RemoteException
     */
    @Test(groups = "wso2.greg", description = "verifies Activity report generation with HTML type",
          dependsOnMethods = "testActivityReportPDF")
    public void testActivityReportHTML() throws Exception, MalformedURLException,
                                                ResourceAdminServiceExceptionException,
                                                RemoteException {
        ByteArrayOutputStream report = getReportOutputStream("HTML");

        assertNotNull(report);

        String reportString = report.toString();

        assertTrue(reportString.contains(userNameWithoutDomain));
        assertTrue(reportString.contains("has added the resource"));
        assertTrue(reportString.contains(testGovernanceLCtemplate));
        assertTrue(reportString.contains(testGovernanceLCRXT));
    }

    /**
     * verifies Activity report generation with type set to Excel
     *
     * @throws org.apache.axis2.AxisFault
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "verifies report generation with type set to Excel",
          dependsOnMethods = "testActivityReportHTML")
    public void testActivityReportExcelType() throws AxisFault, Exception {
        ByteArrayOutputStream report = getReportOutputStream("Excel");

        assertNotNull(report);

        saveByteArrayOutputStreamtoFile(report);

        try {
            FileInputStream myInput = new FileInputStream(Dest_file);

            POIFSFileSystem myFileSystem = new POIFSFileSystem(myInput);

            HSSFWorkbook myWorkBook = new HSSFWorkbook(myFileSystem);

            HSSFSheet mySheet = myWorkBook.getSheetAt(0);

            HSSFRow customRow = mySheet.getRow(4);
            HSSFCell customCell = customRow.getCell(2);
            assertTrue(customCell.getStringCellValue().contains(userNameWithoutDomain));

            customCell = customRow.getCell(2);
            assertTrue(customCell.getStringCellValue().contains("has added the resource"));

            customCell = customRow.getCell(2);
            assertTrue(customCell.getStringCellValue().contains(testGovernanceLCRXT));

            customRow = mySheet.getRow(6);
            customCell = customRow.getCell(2);
            assertTrue(customCell.getStringCellValue().contains(userNameWithoutDomain));

            customCell = customRow.getCell(2);
            assertTrue(customCell.getStringCellValue().contains("has added the resource"));
            //This is only valid for the fresh instance (with fresh database)
//            customCell = customRow.getCell(2);
//            System.out.println("String cell value #####################################" + customCell.getStringCellValue());
//            assertTrue(customCell.getStringCellValue().contains(testGovernanceLCtemplate));
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * get ByteArrayOutputStream of a selected report type
     *
     * @param type
     * @return
     * @throws Exception
     * @throws RemoteException
     * @throws ReportingResourcesSupplierReportingExceptionException
     *
     * @throws ReportingException
     * @throws JRException
     * @throws RegistryException
     */
    private ByteArrayOutputStream getReportOutputStream(String type)
            throws Exception, RemoteException,
                   ReportingResourcesSupplierReportingExceptionException,
                   ReportingException,
                   JRException,
                   RegistryException {

        List<ActivityReportBean> beanList = new ArrayList<ActivityReportBean>();

        for (String stringBean : activities) {
            ActivityReportBean beanOne = new ActivityReportBean();
            beanOne.setUserName(userNameWithoutDomain);
            beanOne.setActivity(stringBean);
            beanOne.setAccessedTime("");
            beanOne.setResourcePath("");
            beanList.add(beanOne);
        }
        String reportResource = reportResourceSupplierClient.getReportResource(ActivitySearchUtil.COMPONENT,
                                                                               ActivitySearchUtil.TEMPLATE);
        JRDataSource jrDataSource = new BeanCollectionReportData().getReportDataSource(beanList);
        JasperPrintProvider jasperPrintProvider = new JasperPrintProvider();
        JasperPrint jasperPrint = jasperPrintProvider.createJasperPrint(jrDataSource, reportResource, new ReportParamMap[0]);
        ReportStream reportStream = new ReportStream();
        return reportStream.getReportStream(jasperPrint, type);
    }

    /**
     * save byte array to a file on the disk for testing purposes
     *
     * @param report
     * @throws IOException
     */
    private void saveByteArrayOutputStreamtoFile(ByteArrayOutputStream report)
            throws IOException {
        FileOutputStream out = new FileOutputStream(Dest_file);
        report.writeTo(out);
        out.close();
    }

    @AfterClass
    public void ClearResourcesAddedForActivityReportTesting()
            throws Exception, ResourceAdminServiceExceptionException {
        resourceAdminServiceClient.deleteResource(testGovernanceLCtemplate);
        resourceAdminServiceClient.deleteResource(testGovernanceLCRXT);
        resourceAdminServiceClient.deleteResource(testTemplateCollection);
        deleteDestiationFile();
        userManagementClient = new UserManagementClient(backEndUrl,
                                                        sessionCookie);
        userManagementClient.deleteUser(userNameWithoutDomain);

        userManagementClient = null;
        reportResourceSupplierClient = null;
        activityAdminServiceClient = null;
        activities = null;
        clear();
    }
}

