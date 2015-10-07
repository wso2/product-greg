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
package org.wso2.carbon.registry.search.metadata.test.old;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.registry.search.metadata.test.bean.SearchParameterBean;
import org.wso2.carbon.registry.search.stub.SearchAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.search.stub.beans.xsd.AdvancedSearchResultsBean;
import org.wso2.carbon.registry.search.stub.beans.xsd.ArrayOfString;
import org.wso2.carbon.registry.search.stub.beans.xsd.CustomSearchParameterBean;
import org.wso2.carbon.registry.search.stub.common.xsd.ResourceData;
import org.wso2.greg.integration.common.clients.SearchAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;

import java.rmi.RemoteException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/*
search registry metadata by resource created data
*/
public class RegistrySearchByCratedDataTestCase extends GREGIntegrationBaseTest {
    private static final Log log = LogFactory.getLog(RegistrySearchByCratedDataTestCase.class);

    private SearchAdminServiceClient searchAdminServiceClient;
    private String sessionCookie;
    private String backEndUrl;
    private String userName;
    private String userNameWithoutDomain;

    @BeforeClass
    public void init() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        backEndUrl = getBackendURL();
        sessionCookie = getSessionCookie();
        userName = automationContext.getContextTenant().getContextUser().getUserName();

        if (userName.contains("@"))
            userNameWithoutDomain = userName.substring(0, userName.indexOf('@'));
        else
            userNameWithoutDomain = userName;

        searchAdminServiceClient = new SearchAdminServiceClient(backEndUrl, sessionCookie);

    }

    @Test(groups = {"wso2.greg"}, description = "Metadata search by created date from")
    public void searchResourceByCreatedDateFrom()
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        Calendar calender = Calendar.getInstance();
        calender.add(Calendar.YEAR, -1);
        paramBean.setCreatedAfter(formatDate(calender.getTime()));
        ArrayOfString[] paramList = paramBean.getParameterList();
        log.info("From Date : " + formatDate(calender.getTime()));

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid from date");
        log.info(result.getResourceDataList().length + " Records found");
        for (ResourceData resource : result.getResourceDataList()) {
            Assert.assertTrue(calender.getTime().before(resource.getCreatedOn().getTime()),
                    "Resource created date is a previous date of the mentioned date on From date");

        }
    }

    @Test(groups = {"wso2.greg"}, description = "Metadata search by created date To")
    public void searchResourceByCreatedDateTo()
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        Calendar calender = Calendar.getInstance();
        log.info("To Date : " + formatDate(calender.getTime()));
        paramBean.setCreatedBefore(formatDate(calender.getTime()));
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid to date");
        log.info(result.getResourceDataList().length + " Records found");
        for (ResourceData resource : result.getResourceDataList()) {
            Assert.assertTrue(calender.getTime().after(resource.getCreatedOn().getTime()),
                    "Resource created date is a later date of the mentioned date on From date");

        }
    }

    @Test(groups = {"wso2.greg"}, description = "Metadata search from valid date range")
    public void searchResourceByValidDateRange()
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        Calendar fromCalender = Calendar.getInstance();
        fromCalender.add(Calendar.YEAR, -1);
        log.info("From Date : " + formatDate(fromCalender.getTime()));
        paramBean.setCreatedAfter(formatDate(fromCalender.getTime()));

        Calendar toCalender = Calendar.getInstance();
        log.info("To Date : " + formatDate(toCalender.getTime()));
        paramBean.setCreatedBefore(formatDate(toCalender.getTime()));

        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid data range");
        log.info(result.getResourceDataList().length + " Records found");
        for (ResourceData resource : result.getResourceDataList()) {
            Assert.assertTrue(toCalender.getTime().after(resource.getCreatedOn().getTime())
                    && fromCalender.getTime().before(resource.getCreatedOn().getTime()),
                    "Resource created date is a not within the mentioned date range");

        }
    }

    @Test(groups = {"wso2.greg"}, description = "Metadata search from valid date range")
    public void searchResourceWithInvalidDateFormat()
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        Format formatter = new SimpleDateFormat("yyyy/MM/dd");
        Calendar fromCalender = Calendar.getInstance();
        fromCalender.add(Calendar.YEAR, -1);
        log.info("From Date : " + formatter.format(fromCalender.getTime()));
        paramBean.setCreatedAfter(formatter.format(fromCalender.getTime()));

        Calendar toCalender = Calendar.getInstance();
        log.info("To Date : " + formatter.format(toCalender.getTime()));
        paramBean.setCreatedBefore(formatter.format(toCalender.getTime()));

        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = null;
        try {
            result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
        } finally {
            if (result != null) {
                Assert.assertTrue(result.getErrorMessage().contains("illegal characters"), "Wrong exception");
            }
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Metadata search records not in valid date range ")
    public void searchResourceByValidDateRangeNot()
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        Calendar fromCalender = Calendar.getInstance();
        fromCalender.add(Calendar.YEAR, -3);
        log.info("From Date : " + formatDate(fromCalender.getTime()));
        paramBean.setCreatedAfter(formatDate(fromCalender.getTime()));

        Calendar toCalender = Calendar.getInstance();
        toCalender.add(Calendar.YEAR, -1);
        log.info("To Date : " + formatDate(toCalender.getTime()));
        paramBean.setCreatedBefore(formatDate(toCalender.getTime()));

        ArrayOfString[] paramList = paramBean.getParameterList();
        searchQuery.setParameterValues(paramList);
        // to set not value
        ArrayOfString createdRangeNegate = new ArrayOfString();
        createdRangeNegate.setArray(new String[]{"createdRangeNegate", "on"});

        searchQuery.addParameterValues(createdRangeNegate);

        AdvancedSearchResultsBean result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid data range");
        log.info(result.getResourceDataList().length + " Records found");
        for (ResourceData resource : result.getResourceDataList()) {
            Assert.assertFalse((toCalender.getTime().after(resource.getCreatedOn().getTime())
                    && fromCalender.getTime().before(resource.getCreatedOn().getTime())),
                    "Resource created date is a not within the mentioned date range");

        }
    }

    @Test(groups = {"wso2.greg"}, description = "Metadata search from valid date range having no resource")
    public void searchResourceByValidDateRangeHavingNoRecords()
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        Calendar fromCalender = Calendar.getInstance();
        fromCalender.add(Calendar.YEAR, -5);
        log.info("From Date : " + formatDate(fromCalender.getTime()));
        paramBean.setCreatedAfter(formatDate(fromCalender.getTime()));

        Calendar toCalender = Calendar.getInstance();
        toCalender.set(Calendar.YEAR, -3);
        log.info("To Date : " + formatDate(toCalender.getTime()));
        paramBean.setCreatedBefore(formatDate(toCalender.getTime()));

        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = null;
        try {
            result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
        } finally {
            if (result != null) {
                if (result.getResourceDataList() != null) {
                    Assert.assertNull(result.getResourceDataList()[0], "Results found");
                } else {
                    Assert.assertNull(result.getResourceDataList(), "Result Object found.");
                }
            } else {
                Assert.assertNull(result, "No results returned");
            }
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Metadata search from invalid date range")
    public void searchResourceByInValidDateRange()
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        Calendar fromCalender = Calendar.getInstance();
        log.info("From Date : " + formatDate(fromCalender.getTime()));
        paramBean.setCreatedAfter(formatDate(fromCalender.getTime()));

        Calendar toCalender = Calendar.getInstance();
        toCalender.set(Calendar.YEAR, -1);
        log.info("To Date : " + formatDate(toCalender.getTime()));
        paramBean.setCreatedBefore(formatDate(toCalender.getTime()));

        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = null;
        try {
            result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
        } finally {
            if (result != null) {
                if (result.getResourceDataList() != null) {
                    Assert.assertNull(result.getResourceDataList()[0], "Results found");
                } else {
                    Assert.assertNull(result.getResourceDataList(), "Result Object found.");
                }
            } else {
                Assert.assertNull(result, "No results returned");
            }
        }
    }

    @Test(groups = {"wso2.greg"}, dataProvider = "invalidCharacter",
            description = "Metadata search by invalid String for date")
    public void searchResourceByInvalidValueForDate(String invalidInput)
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setCreatedAfter(invalidInput);
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = null;
        try {
            result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
        } finally {
            if (result != null) {
                Assert.assertTrue(result.getErrorMessage().contains("illegal characters"), "Wrong exception");
            }
        }

    }

    @DataProvider(name = "invalidCharacter")
    public Object[][] invalidCharacter() {
        return new Object[][]{
                {"invalid-date"},
                {"<a>"},
                {"#"},
                {"a|b"},
                {"@"},
                {"|"},
                {"^"},
                {"abc^"},
                {"/"},
                {"\\"}
        };
    }

    private String formatDate(Date date) {
        Format formatter = new SimpleDateFormat("MM/dd/yyyy");
        return formatter.format(date);
    }

    @AfterClass
    public void destroy(){
        searchAdminServiceClient = null;
    }
}
