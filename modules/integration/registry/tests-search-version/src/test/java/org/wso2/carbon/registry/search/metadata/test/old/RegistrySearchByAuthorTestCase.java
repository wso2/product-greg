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

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.api.clients.registry.SearchAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.search.metadata.test.bean.SearchParameterBean;
import org.wso2.carbon.registry.search.metadata.test.utils.Parameters;
import org.wso2.carbon.registry.search.stub.SearchAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.search.stub.beans.xsd.AdvancedSearchResultsBean;
import org.wso2.carbon.registry.search.stub.beans.xsd.ArrayOfString;
import org.wso2.carbon.registry.search.stub.beans.xsd.CustomSearchParameterBean;
import org.wso2.carbon.registry.search.stub.common.xsd.ResourceData;

import javax.activation.DataHandler;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

/*
Search Registry metadata by Author Name
 */
public class RegistrySearchByAuthorTestCase {

    private final int userId = 0;
    private String userName;
    private final UserInfo userInfo = UserListCsvReader.getUserInfo(userId);
    private SearchAdminServiceClient searchAdminServiceClient;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private final String benchmarkLocation = "/_system/governance/benchMark";

    @BeforeClass
    public void init() throws Exception {
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        ManageEnvironment environment = builder.build();
        userName = userInfo.getUserName();
        searchAdminServiceClient = new SearchAdminServiceClient(environment.getGreg().getBackEndUrl(),
                userInfo.getUserName(), userInfo.getPassword());
        resourceAdminServiceClient = new ResourceAdminServiceClient(environment
                .getGreg().getProductVariables().getBackendUrl(),
                userInfo.getUserName(), userInfo.getPassword());
        addBenchMark();
    }

    private void addBenchMark() throws MalformedURLException, ResourceAdminServiceExceptionException, RemoteException {
        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION
                + "artifacts" + File.separator + "GREG" + File.separator
                + "wsdl" + File.separator + "echo.wsdl";
        DataHandler dh = new DataHandler(new URL("file:///" + resourcePath));
        resourceAdminServiceClient.addResource(benchmarkLocation,
                "WSDL", "TstDec", dh);
    }

    @Test(groups = {"wso2.greg"}, description = "Metadata search by available Author Name")
    public void searchResourceByAuthor()
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setAuthor(userName);
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid Author name");
        for (ResourceData resource : result.getResourceDataList()) {
            Assert.assertTrue(resource.getAuthorUserName().contains(userName),
                    "search keyword not contain on Author Name :" + resource.getAuthorUserName());
        }


    }

    @Test(groups = {"wso2.greg"}, description = "Metadata search by available Author Name not", dependsOnMethods = "searchResourceByAuthor")
    public void searchResourceByAuthorNot()
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setAuthor(userName);
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);

        // to set updatedRangeNegate
        ArrayOfString authorNameNegate = new ArrayOfString();
        authorNameNegate.setArray(new String[]{"authorNameNegate", "on"});

        searchQuery.addParameterValues(authorNameNegate);

        AdvancedSearchResultsBean result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid Author name");
        for (ResourceData resource : result.getResourceDataList()) {
            Assert.assertFalse(resource.getAuthorUserName().contains(userName),
                    "search keyword contain on Author Name :" + resource.getResourcePath());
        }


    }

    @Test(groups = {"wso2.greg"}, description = "Metadata search by Author Name pattern matching", dependsOnMethods = "searchResourceByAuthorNot")
    public void searchResourceByAuthorNamePattern()
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setAuthor("wso2%user");
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid Author name pattern");
        for (ResourceData resource : result.getResourceDataList()) {
            Assert.assertTrue((resource.getAuthorUserName().contains("wso2") && resource.getAuthorUserName().contains("user")),
                    "search word pattern not contain on Author Name :" + resource.getResourcePath());
        }


    }


    @Test(groups = {"wso2.greg"}, description = "Metadata search by unavailable Author Name", dependsOnMethods = "searchResourceByAuthorNamePattern")
    public void searchResourceByUnAvailableAuthorName()
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setAuthor("xyz1234");
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
        Assert.assertNull(result.getResourceDataList(), "Result Object found");


    }

    @Test(dataProvider = "invalidCharacter", groups = {"wso2.greg"}, dataProviderClass = Parameters.class,
            description = "Metadata search by Author Name with invalid characters", dependsOnMethods = "searchResourceByUnAvailableAuthorName")
    public void searchResourceByAuthorNameWithInvalidCharacter(String invalidInput)
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();

        paramBean.setAuthor(invalidInput);
        ArrayOfString[] paramList = paramBean.getParameterList();
        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = null;
        try {
            result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
        } finally {
            Assert.assertTrue(result.getErrorMessage().contains("illegal characters"), "Wrong exception");
        }


    }

    @AfterClass
    public void destroy() throws ResourceAdminServiceExceptionException, RemoteException {
        resourceAdminServiceClient.deleteResource(benchmarkLocation);
        searchAdminServiceClient = null;
        resourceAdminServiceClient = null;
    }
}
