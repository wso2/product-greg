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
import org.wso2.carbon.automation.engine.configurations.UrlGenerationUtil;
import org.wso2.carbon.logging.view.stub.types.carbon.LogEvent;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.reporting.stub.beans.xsd.ReportConfigurationBean;

import java.util.Calendar;
import java.util.Date;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class ReportSchedulingTestCases extends ReportingTestCaseSuper {

    Date benchDate;
    int scheduleDelay = 5;
    int checkInterval = 3;
    int waitTime = 30000;

    @BeforeClass(alwaysRun = true)
    public void initializeToTestScheduling() throws Exception {
        applicationName = super.applicationName + "ReportSchedulingTestCases";
        artifactName = super.artifactName + "ReportSchedulingTestCases";
        init();
        testAddResourcesLCReport();
        testAddLCArtifact();
    }

    /**
     * Specify valid cron expression and let a report generate and verify
     * whether the report is generated at the specified time
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Schedule a report and verify", enabled = true)
    public void testScheduleReport() throws Exception {

        ReportConfigurationBean configurationBean = new ReportConfigurationBean();

        configurationBean.setName("schedule");
        configurationBean.setTemplate(testGovernanceLCtemplate);
        configurationBean.setType("HTML");
        configurationBean.setReportClass(testGovernanceLCClass);

        reportAdminServiceClient.saveReport(configurationBean);

        configurationBean.setAttributes(testLCattributes);

        String url = UrlGenerationUtil.getRemoteRegistryURL(automationContext.getInstance());

        configurationBean.setRegistryURL(url);
        configurationBean.setUsername(automationContext.getContextTenant().getContextUser().getUserName());
        configurationBean.setPassword(automationContext.getContextTenant().getContextUser().getPassword());
        configurationBean.setResourcePath(scheduleReportLocation + ".html");

        Resource benchMark = registry.get(testGovernanceLCtemplate);

        registry.put("/_system/governance/benchMark", benchMark);

        benchMark = registry.get(scheduleBenchmarkLocation);

        benchDate = benchMark.getCreatedTime();

        Calendar calLower = Calendar.getInstance();
        calLower.setTime(benchDate);
        calLower.add(Calendar.SECOND, scheduleDelay);

        Date dateLower = calLower.getTime();

        String cronExpression = "0/3 * * * * ?";
        configurationBean.setCronExpression(cronExpression);

        reportAdminServiceClient.scheduleReport(configurationBean);

        Thread.sleep(waitTime);

        calLower.add(Calendar.SECOND, checkInterval);
        Date dateUpper = calLower.getTime();

        boolean assertVal = false;
        if (registry.resourceExists("/_system/governance/report.html")) {
            assertTrue(true);
        } else {
            assertTrue(false, "Report doesn't generated");

        }
        Resource report = registry.get("/_system/governance/report.html");

        if (dateLower.compareTo(report.getCreatedTime()) < 0
            && dateUpper.compareTo(report.getCreatedTime()) > 0) {
            assertVal = true;
        }

        String reportData = convertResourceToString(report);
        assertTrue(reportData.contains(artifactName + "1"));
        assertTrue(reportData.contains("G-regTesting"));
        assertTrue(reportData.contains("4.5.0"));
        assertTrue(reportData.contains("Smoke test"));
        reportAdminServiceClient.stopScheduledReport("schedule");

        calLower.setTime(benchDate);
        calLower.add(Calendar.SECOND, waitTime / 1000);

        benchDate = calLower.getTime();
    }

    /**
     * Specify credentials of a non-existing user and verify error handling
     * during report generation
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Schedule a report with non-existing user details",
            dependsOnMethods = "testScheduleReport", enabled = true)
    public void testScheduleReportNonExistingUser() throws Exception {
        ReportConfigurationBean configurationBean = reportAdminServiceClient
                .getSavedReport("schedule");

        configurationBean.setAttributes(testLCattributes);

        String url = UrlGenerationUtil.getRemoteRegistryURL(automationContext.getInstance());

        configurationBean.setRegistryURL(url);
        configurationBean.setUsername("NonExistingUser");
        configurationBean.setPassword("NonExistingUserPassword");
        configurationBean
                .setResourcePath("/_system/governance/reportInvalidRegistryURL");

        Calendar calLower = Calendar.getInstance();
        calLower.setTime(benchDate);
        calLower.add(Calendar.SECOND, scheduleDelay);

        int seconds = calLower.get(Calendar.SECOND);
        int minutes = calLower.get(Calendar.MINUTE);
        int hours = calLower.get(Calendar.HOUR_OF_DAY);
        int dayOfMonth = calLower.get(Calendar.DAY_OF_MONTH);
        int month = calLower.get(Calendar.MONTH) + 1;
        int year = calLower.get(Calendar.YEAR);

        String cronExpression = seconds + " " + minutes + " " + hours + " "
                                + dayOfMonth + " " + month + " ? " + year;
        configurationBean.setCronExpression(cronExpression);

        reportAdminServiceClient.scheduleReport(configurationBean);
        Thread.sleep(waitTime);

        assertTrue(!registry
                .resourceExists("/_system/governance/reportInvalidRegistryURL"));
        LogEvent[] logEvents = logViewerClient.getLogs("ERROR", "Unable to obtain reporting content stream", "", "");
        assertEquals(logEvents[0].getMessage(),
                     "Unable to obtain reporting content stream");
        logEvents = logViewerClient.getLogs("WARN", "Attempted to authenticate invalid user", "", "");
        assertEquals(logEvents[0].getMessage(),
                     "Attempted to authenticate invalid user.");
        reportAdminServiceClient.stopScheduledReport("schedule");

        calLower.setTime(benchDate);
        calLower.add(Calendar.SECOND, waitTime / 1000);

        benchDate = calLower.getTime();
    }

    /**
     * Specify an invalid registry URL and verify error handling during report
     * generation
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "schedule a report with an invalid registry URL",
            dependsOnMethods = "testScheduleReport", enabled = true)
    public void testScheduleReportInvalidRegistryURL() throws Exception {
        ReportConfigurationBean configurationBean = reportAdminServiceClient
                .getSavedReport("schedule");

        configurationBean.setAttributes(testLCattributes);
        configurationBean.setRegistryURL("https://registry");
        configurationBean.setUsername(automationContext.getContextTenant().getTenantUser("user2").getUserName());
        configurationBean.setPassword(automationContext.getContextTenant().getTenantUser("user2").getPassword());
        configurationBean
                .setResourcePath("/_system/governance/reportInvalidRegistryURL");

        Calendar calLower = Calendar.getInstance();
        calLower.setTime(benchDate);
        calLower.add(Calendar.SECOND, scheduleDelay);

        int seconds = calLower.get(Calendar.SECOND);
        int minutes = calLower.get(Calendar.MINUTE);
        int hours = calLower.get(Calendar.HOUR_OF_DAY);
        int dayOfMonth = calLower.get(Calendar.DAY_OF_MONTH);
        int month = calLower.get(Calendar.MONTH) + 1;
        int year = calLower.get(Calendar.YEAR);

        StringBuilder stringBuilder =  new StringBuilder().append(seconds).append(" ").append(minutes).append(" ")
                     .append(hours).append(" ").append(dayOfMonth).append(" ").append(month).append(" ? ").append(year);

        configurationBean.setCronExpression(stringBuilder.toString());

        reportAdminServiceClient.scheduleReport(configurationBean);
        Thread.sleep(waitTime);

        assertTrue(!registry
                .resourceExists("/_system/governance/reportInvalidRegistryURL"));
        LogEvent[] logEvents = logViewerClient.getLogs("ERROR", "Unable to obtain reporting content stream", "", "");
        assertEquals(logEvents[0].getMessage(),
                     "Unable to obtain reporting content stream");
        reportAdminServiceClient.stopScheduledReport("schedule");

        calLower.setTime(benchDate);
        calLower.add(Calendar.SECOND, waitTime / 1000);

        benchDate = calLower.getTime();
    }

    /**
     * Specify an invalid registry path and verify error handling during report
     * generation
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Schedule a reort with an invalid registry path",
            dependsOnMethods = "testScheduleReport", enabled = true)
    public void testScheduleReportInvalidPath() throws Exception {
        ReportConfigurationBean configurationBean = reportAdminServiceClient
                .getSavedReport("schedule");

        configurationBean.setAttributes(testLCattributes);
        String url = UrlGenerationUtil.getRemoteRegistryURL(automationContext.getInstance());
        configurationBean.setRegistryURL(url);
        configurationBean.setUsername(automationContext.getContextTenant().getTenantUser("user2").getUserName());
        configurationBean.setPassword(automationContext.getContextTenant().getTenantUser("user2").getPassword());
        configurationBean.setResourcePath("/_sy3s*te%m");

        Calendar calLower = Calendar.getInstance();
        calLower.setTime(benchDate);
        calLower.add(Calendar.SECOND, scheduleDelay);

        int seconds = calLower.get(Calendar.SECOND);
        int minutes = calLower.get(Calendar.MINUTE);
        int hours = calLower.get(Calendar.HOUR_OF_DAY);
        int dayOfMonth = calLower.get(Calendar.DAY_OF_MONTH);
        int month = calLower.get(Calendar.MONTH) + 1;
        int year = calLower.get(Calendar.YEAR);

        StringBuilder cronExpression = new StringBuilder().append(seconds).append(" ").append(minutes).append(" ")
                     .append(hours).append(" ").append(dayOfMonth).append(" ").append(month).append(" ? ").append(year);
        configurationBean.setCronExpression(cronExpression.toString());

        reportAdminServiceClient.scheduleReport(configurationBean);
        Thread.sleep(waitTime);

        assertTrue(!registry.resourceExists("/_sy3s*te%m"));
        LogEvent[] logEvents = logViewerClient
                .getLogs("ERROR", "Add resource fail. Suggested Path: /_sy3s*te%m, Response Status: " +
                                  "500, Response Type: SERVER_ERROR", "", "");
        assertEquals(
                logEvents[0].getMessage(),
                "Add resource fail. Suggested Path: /_sy3s*te%m, Response Status: 500, Response Type: SERVER_ERROR");
        reportAdminServiceClient.stopScheduledReport("schedule");

        calLower.setTime(benchDate);
        calLower.add(Calendar.SECOND, waitTime / 1000);

        benchDate = calLower.getTime();
    }

    @AfterClass(alwaysRun = true)
    public void ClearResourcesAddedToTestScheduling() throws Exception {
        removeLCArtifact();
        removeResourcesLCReport();
        removeTemplateCollection();
        removeAllReports();
        if (registry.resourceExists(scheduleBenchmarkLocation)) {
            resourceAdminServiceClient.deleteResource(scheduleBenchmarkLocation);
        }
        if (registry.resourceExists("/_system/governance/report.html")) {
            resourceAdminServiceClient.deleteResource("/_system/governance/report.html");
        }
        clear();
    }
}
