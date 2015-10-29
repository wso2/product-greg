/*
*Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.registry.reporting.stub.beans.xsd.ReportConfigurationBean;

import javax.activation.DataHandler;

import static org.testng.Assert.assertTrue;

public class ReportGenerationImageConversionTest extends ReportingTestCaseSuper {

    @BeforeClass(alwaysRun = true)
    public void initializeForReportGenerationTesting() throws Exception {
        applicationName = super.applicationName + "ReportGenerationImageConversionTest";
        artifactName = super.artifactName + "ReportGenerationImageConversionTest";
        init();
    }

    @Test(groups = "wso2.greg", description = "Add resources and artifacts to test image conversion of HTML reports")
    public void testAddResourcesForReportImageConversionTesting()
            throws Exception {
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
     * verifies whether the images have been converted into base64 in HTML report type
     *
     * @throws AxisFault
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "verifies HTML report images have been converted into base64",
            dependsOnMethods = "testAddResourcesForReportImageConversionTesting")
    public void testHtmlImageConversion()
            throws AxisFault, Exception {

        ReportConfigurationBean configurationBean = reportAdminServiceClient.getSavedReport("TestGovernanceLCReport");
        configurationBean.setAttributes(testLCattributes);
        DataHandler report = reportAdminServiceClient.getReportBytes(configurationBean);

        String result = readInputStreamAsString(report.getInputStream());
        assertTrue(result.contains("data:image/gif;base64"));

    }

}
