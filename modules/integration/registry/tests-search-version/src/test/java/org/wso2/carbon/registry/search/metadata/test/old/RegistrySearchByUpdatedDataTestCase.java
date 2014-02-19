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
import org.wso2.carbon.automation.api.clients.registry.SearchAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.search.metadata.test.bean.SearchParameterBean;
import org.wso2.carbon.registry.search.stub.SearchAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.search.stub.beans.xsd.AdvancedSearchResultsBean;
import org.wso2.carbon.registry.search.stub.beans.xsd.ArrayOfString;
import org.wso2.carbon.registry.search.stub.beans.xsd.CustomSearchParameterBean;
import org.wso2.carbon.registry.search.stub.common.xsd.ResourceData;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import javax.xml.namespace.QName;
import java.rmi.RemoteException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/*
search registry metadata by resource updated data
*/
public class RegistrySearchByUpdatedDataTestCase {
    private static final Log log = LogFactory.getLog(RegistrySearchByUpdatedDataTestCase.class);

    private WSRegistryServiceClient registry;
    private final int userId = 1;
    private final UserInfo userInfo = UserListCsvReader.getUserInfo(userId);
    private SearchAdminServiceClient searchAdminServiceClient;
    private String userName;
    private Registry governance;

    @BeforeClass
    public void init() throws Exception {
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        ManageEnvironment environment = builder.build();
        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        registry = registryProviderUtil.getWSRegistry(userId,
                ProductConstant.GREG_SERVER_NAME);
        userName = userInfo.getUserName();
        searchAdminServiceClient = new SearchAdminServiceClient(environment.getGreg().getBackEndUrl(),
                userInfo.getUserName(), userInfo.getPassword());
        governance = registryProviderUtil.getGovernanceRegistry(registry,
                userId);

        ServiceManager serviceManager =
                new ServiceManager(registryProviderUtil.getGovernanceRegistry(registry, userId));
        Service service =
                serviceManager.newService(new QName("RegistrySearchByUpdatedDataTestCase"));
        serviceManager.addService(service);


    }

    @Test(groups = {"wso2.greg"}, description = "Metadata search by updated date from")
    public void searchResourceByUpdatedDateFrom()
            throws SearchAdminServiceRegistryExceptionException, RemoteException,
            RegistryException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        Calendar calender = Calendar.getInstance();
        calender.add(Calendar.YEAR, -1);
        paramBean.setUpdatedAfter(formatDate(calender.getTime()));

        ArrayOfString[] paramList = paramBean.getParameterList();
        log.info("From Date : " + formatDate(calender.getTime()));


        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid from date");
        log.info(result.getResourceDataList().length + " Records found");
        for (ResourceData resource : result.getResourceDataList()) {
            Resource rs = registry.get(resource.getResourcePath());
            Assert.assertTrue(calender.getTime().before(rs.getLastModified()),
                    "Resource updated date is a previous date of the mentioned date on From date:" +
                            " Actual Date: " + formatDate(rs.getLastModified())
                            + " Mentioned From Date : " + formatDate(calender.getTime()));

        }

    }

    @Test(groups = {"wso2.greg"}, description = "Metadata search by updated date To")
    public void searchResourceByUpdatedDateTo()
            throws SearchAdminServiceRegistryExceptionException, RemoteException,
            RegistryException, InterruptedException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        Calendar calender = Calendar.getInstance();

        long currentTime = calender.getTimeInMillis();

        log.info("To Date : " + formatDate(calender.getTime()));
        paramBean.setUpdatedBefore(formatDate(calender.getTime()));
        paramBean.setUpdater(userName);
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid to date");
        log.info(result.getResourceDataList().length + " Records found");

        for (ResourceData resource : result.getResourceDataList()) {
            Resource rs = registry.get(resource.getResourcePath());

            long resourceTime = rs.getLastModified().getTime();

            Assert.assertTrue(currentTime > resourceTime,
                    resource.getResourcePath() + " Resource updated date is a later date of the mentioned date on From date. " +
                            " Actual Date: " + formatDate(rs.getLastModified())
                            + " Mentioned To Date : " + formatDate(calender.getTime()));

        }
    }

    @Test(groups = {"wso2.greg"}, description = "Metadata search from valid date range")
    public void searchResourceByValidUpdatedDateRange()
            throws SearchAdminServiceRegistryExceptionException, RemoteException,
            RegistryException, InterruptedException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        Calendar fromCalender = Calendar.getInstance();
        fromCalender.add(Calendar.MONTH, -1);
        log.info("From Date : " + formatDate(fromCalender.getTime()));
        paramBean.setUpdatedAfter(formatDate(fromCalender.getTime()));

        Calendar toCalender = Calendar.getInstance();
        log.info("To Date : " + formatDate(toCalender.getTime()));
        paramBean.setUpdatedBefore(formatDate(toCalender.getTime()));


        long fromTime = fromCalender.getTimeInMillis();
        long toTime = toCalender.getTimeInMillis();

        paramBean.setUpdater(userName);

        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid data range");
        log.info(result.getResourceDataList().length + " Records found");
        for (ResourceData resource : result.getResourceDataList()) {
            Resource rs = registry.get(resource.getResourcePath());

            long resourceTime = rs.getLastModified().getTime();

            Assert.assertTrue(fromTime < resourceTime
                    && toTime > resourceTime,
                    "Resource updated date is a not within the mentioned date range");

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
        paramBean.setUpdatedAfter(formatter.format(fromCalender.getTime()));

        Calendar toCalender = Calendar.getInstance();
        log.info("To Date : " + formatter.format(toCalender.getTime()));
        paramBean.setUpdatedBefore(formatter.format(toCalender.getTime()));

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
    public void searchResourceByValidUpdatedDateRangeNot()
            throws SearchAdminServiceRegistryExceptionException, RemoteException,
            RegistryException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        Calendar fromCalender = Calendar.getInstance();
        fromCalender.add(Calendar.YEAR, -3);
        log.info("From Date : " + formatDate(fromCalender.getTime()));
        paramBean.setUpdatedAfter(formatDate(fromCalender.getTime()));

        Calendar toCalender = Calendar.getInstance();
        toCalender.add(Calendar.YEAR, -1);
        log.info("To Date : " + formatDate(toCalender.getTime()));
        paramBean.setUpdatedBefore(formatDate(toCalender.getTime()));

        ArrayOfString[] paramList = paramBean.getParameterList();
        searchQuery.setParameterValues(paramList);

        // to set updatedRangeNegate
        ArrayOfString updatedRangeNegate = new ArrayOfString();
        updatedRangeNegate.setArray(new String[]{"updatedRangeNegate", "on"});

        searchQuery.addParameterValues(updatedRangeNegate);

        AdvancedSearchResultsBean result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid data range");
        log.info(result.getResourceDataList().length + " Records found");
        for (ResourceData resource : result.getResourceDataList()) {
            Resource rs = registry.get(resource.getResourcePath());
            Assert.assertFalse((toCalender.getTime().after(rs.getLastModified())
                    && fromCalender.getTime().before(rs.getLastModified())),
                    "Resource updated date is a not within the mentioned date range");

        }
    }

    @Test(groups = {"wso2.greg"},
            description = "Metadata search from valid updated date range having no resource")
    public void searchResourceByValidUpdatedDateRangeHavingNoRecords()
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        Calendar fromCalender = Calendar.getInstance();
        fromCalender.add(Calendar.YEAR, -5);
        log.info("From Date : " + formatDate(fromCalender.getTime()));
        paramBean.setUpdatedAfter(formatDate(fromCalender.getTime()));

        Calendar toCalender = Calendar.getInstance();
        toCalender.set(Calendar.YEAR, -3);
        log.info("To Date : " + formatDate(toCalender.getTime()));
        paramBean.setUpdatedBefore(formatDate(toCalender.getTime()));

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

    @Test(groups = {"wso2.greg"}, description = "Metadata search from invalid updated date range")
    public void searchResourceByInValidDateRange()
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        Calendar fromCalender = Calendar.getInstance();
        log.info("From Date : " + formatDate(fromCalender.getTime()));
        paramBean.setUpdatedAfter(formatDate(fromCalender.getTime()));

        Calendar toCalender = Calendar.getInstance();
        toCalender.set(Calendar.YEAR, -1);
        log.info("To Date : " + formatDate(toCalender.getTime()));
        paramBean.setUpdatedBefore(formatDate(toCalender.getTime()));

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
            description = "Metadata search by invalid String for updated date")
    public void searchResourceByInvalidValueForUpdatedDate(String invalidInput)
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setUpdatedAfter(invalidInput);
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
    public void destroy() throws GovernanceException {
        ServiceManager serviceManager = new ServiceManager(governance);
        String[] services = serviceManager.getAllServiceIds();
        for (int i = 0; i < services.length; i++) {
            serviceManager.removeService(services[i]);
        }
        searchAdminServiceClient = null;
        governance = null;
        registry = null;
    }
}
