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
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.search.metadata.test.bean.SearchParameterBean;
import org.wso2.carbon.registry.search.metadata.test.utils.Parameters;
import org.wso2.carbon.registry.search.stub.SearchAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.search.stub.beans.xsd.AdvancedSearchResultsBean;
import org.wso2.carbon.registry.search.stub.beans.xsd.ArrayOfString;
import org.wso2.carbon.registry.search.stub.beans.xsd.CustomSearchParameterBean;
import org.wso2.carbon.registry.search.stub.common.xsd.ResourceData;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import java.rmi.RemoteException;

/*
Search Registry metadata by keyword(content)
 */
public class RegistrySearchByKeywordTestCase {

    private WSRegistryServiceClient registry;
    private final int userId = 1;
    private final UserInfo userInfo = UserListCsvReader.getUserInfo(userId);
    private SearchAdminServiceClient searchAdminServiceClient;

    @BeforeClass
    public void init() throws Exception {
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        ManageEnvironment environment = builder.build();
        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        registry = registryProviderUtil.getWSRegistry(userId,
                ProductConstant.GREG_SERVER_NAME);
        searchAdminServiceClient = new SearchAdminServiceClient(environment.getGreg().getBackEndUrl(),
                userInfo.getUserName(), userInfo.getPassword());
    }

    @Test(groups = {"wso2.greg"}, description = "Metadata search by available keyword")
    public void searchResourceByAvailableKeyword() throws SearchAdminServiceRegistryExceptionException, RemoteException,
            RegistryException, InterruptedException {

        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setContent("org");
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);

        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid keyword");
        boolean isResource = false;
        for (ResourceData resource : result.getResourceDataList()) {
            Resource data = registry.get(resource.getResourcePath());
            if (data.getMediaType() != null && data instanceof ResourceImpl && data.getMediaType().contains("xml")) {
                String content = new String((byte[]) data.getContent());
                Assert.assertTrue(content.contains("org"),
                        "search keyword not contain on Resource Name :" + resource.getName());
                isResource = true;
                break;
            }
        }
        Assert.assertTrue(isResource, "Resource didn't found having org");

    }


    @Test(groups = {"wso2.greg"}, description = "Metadata search by available keywords")
    //, dependsOnMethods = "searchResourceByAvailableKeyword"
    public void searchResourceByAvailableContents() throws SearchAdminServiceRegistryExceptionException, RemoteException,
            RegistryException {

        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setContent("com");
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid keywords");
        boolean isResource = false;
        for (ResourceData resource : result.getResourceDataList()) {
            Resource data = registry.get(resource.getResourcePath());
            if (data.getMediaType() != null && data instanceof ResourceImpl && data.getMediaType().contains("xml")) {
                String content = new String((byte[]) data.getContent());
                Assert.assertTrue((content.contains("org") || content.contains("com")),
                        "search keyword not contain on Resource Name :" + resource.getResourcePath());
                isResource = true;
                break;
            }
        }
        Assert.assertTrue(isResource, "Any content not found having org");

    }

    @Test(groups = {"wso2.greg"}, description = "Metadata search by unavailable keyword")
    public void searchResourceByUnAvailableContent() throws SearchAdminServiceRegistryExceptionException,
            RemoteException {

        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setContent("com,org");
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

    @Test(dataProvider = "invalidCharacterForContent", dataProviderClass = Parameters.class, groups = {"wso2.greg"},
            description = "Metadata search by keywords with invalid characters")
    public void searchResourceByContentWithInvalidCharacter(String invalidInput)
            throws SearchAdminServiceRegistryExceptionException, RemoteException {

        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setContent(invalidInput);
        ArrayOfString[] paramList = paramBean.getParameterList();
        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = null;
        try {
            result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
        } finally {
            Assert.assertNotNull(result, "No result found " + invalidInput);
            Assert.assertTrue(result.getErrorMessage().contains("illegal characters"), "Wrong exception");
        }
    }

    @AfterClass
    public void destroy(){
        searchAdminServiceClient = null;
    }
}
