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
import org.wso2.carbon.automation.api.clients.registry.SearchAdminServiceClient;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.registry.search.metadata.test.bean.SearchParameterBean;
import org.wso2.carbon.registry.search.metadata.test.utils.Parameters;
import org.wso2.carbon.registry.search.stub.SearchAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.search.stub.beans.xsd.AdvancedSearchResultsBean;
import org.wso2.carbon.registry.search.stub.beans.xsd.ArrayOfString;
import org.wso2.carbon.registry.search.stub.beans.xsd.CustomSearchParameterBean;
import org.wso2.carbon.registry.search.stub.common.xsd.ResourceData;

import java.rmi.RemoteException;

/*
Search Registry metadata by Resource Name
 */
public class RegistrySearchByResourceNameTestCase {

    private final int userId = 1;
    private final UserInfo userInfo = UserListCsvReader.getUserInfo(userId);
    private SearchAdminServiceClient searchAdminServiceClient;

    @BeforeClass
    public void init() throws Exception {
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        ManageEnvironment environment = builder.build();
        searchAdminServiceClient = new SearchAdminServiceClient(environment.getGreg().getBackEndUrl(),
                userInfo.getUserName(), userInfo.getPassword());

    }

    @Test(groups = {"wso2.greg"}, description = "Metadata search by available Resource Name")
    public void searchResourceByAvailableName()
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setResourceName("org");
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid resource name");
        for (ResourceData resource : result.getResourceDataList()) {
            Assert.assertTrue(resource.getName().contains("org"),
                    "search keyword not contain on Resource Name :" + resource.getResourcePath());
        }


    }

    @Test(groups = {"wso2.greg"}, description = "Metadata search by Resource Name pattern matching", dependsOnMethods = "searchResourceByAvailableName")
    public void searchResourceByNamePattern()
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setResourceName("org%mgt");
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid resource name pattern");
        for (ResourceData resource : result.getResourceDataList()) {
            Assert.assertTrue((resource.getName().contains("org") && resource.getName().contains("mgt")),
                    "search keyword not contain on Resource Name :" + resource.getName());
        }


    }

    /*@Test(groups = {"wso2.greg"}, description = "Metadata search by available Resource Names")
    public void searchResourceByAvailableNames()
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setResourceName("org,com");
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminService.getAdvancedSearchResults(sessionCookie, searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid resource names");
        for (ResourceData resource : result.getResourceDataList()) {
            Assert.assertTrue(resource.getName().contains("org") || resource.getName().contains("com"),
                              "search keyword not contain on Resource Name :" + resource.getName());
        }


    }*/

    @Test(groups = {"wso2.greg"}, description = "Metadata search by unavailable Resource Name", dependsOnMethods = "searchResourceByNamePattern")
    public void searchResourceByUnAvailableName()
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setResourceName("xyz123");
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
        Assert.assertNull(result.getResourceDataList(), "Result Object found");


    }

    @Test(dataProvider = "invalidCharacter", groups = {"wso2.greg"}, dataProviderClass = Parameters.class,
            description = "Metadata search by Resource Name with invalid characters", dependsOnMethods = "searchResourceByUnAvailableName")
    public void searchResourceByNameWithInvalidCharacter(String invalidInput)
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();

        paramBean.setResourceName(invalidInput);
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
    public void destroy(){
        searchAdminServiceClient = null;
    }
}
