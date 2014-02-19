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
Search Registry metadata by last updater Name
*/
public class RegistrySearchByUpdaterTestCase {

    private WSRegistryServiceClient registry;
    private final int userId = 0;
    private final UserInfo userInfo = UserListCsvReader.getUserInfo(userId);
    private SearchAdminServiceClient searchAdminServiceClient;
    private String userName;

    private String resourcePath;

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

        resourcePath = "/_system/governance/test";

        populateResource();

    }

    private void populateResource() throws RegistryException {
        Resource initialResource = registry.newResource();

//        Adding the resource for the first time
//        user admin will be the creator and updater of this resource.
        initialResource.setContent("Test content");
        registry.put(resourcePath, initialResource);
    }

    @Test(groups = {"wso2.greg"}, description = "Metadata search by available Updater Name")
    public void searchResourceByUpdater()
            throws SearchAdminServiceRegistryExceptionException, RemoteException,
            RegistryException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setUpdater(userName);
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid updater name");
        for (ResourceData resource : result.getResourceDataList()) {
            Assert.assertTrue(registry.get(resource.getResourcePath()).getLastUpdaterUserName().contains(userName),
                    "search word not contain on Updater Name :" + resource.getResourcePath());
        }


    }

    @Test(groups = {"wso2.greg"}, description = "Metadata search by available Updater Name not", dependsOnMethods = "searchResourceByUpdater")
    public void searchResourceByUpdaterNot()
            throws SearchAdminServiceRegistryExceptionException, RemoteException,
            RegistryException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setUpdater(userName);
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);

        // to set updatedRangeNegate
        ArrayOfString updaterNameNegate = new ArrayOfString();
        updaterNameNegate.setArray(new String[]{"updaterNameNegate", "on"});

        searchQuery.addParameterValues(updaterNameNegate);

        AdvancedSearchResultsBean result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid Updater name");
        for (ResourceData resource : result.getResourceDataList()) {
            Resource regResource = registry.get(resource.getResourcePath());
            if (regResource.getProperty("registry.link") == null) {
                Assert.assertFalse(regResource.getLastUpdaterUserName().contains(userName),
                        "searched updater name not contain on actual Updater Name :" + resource.getResourcePath());
            }
        }


    }

    @Test(groups = {"wso2.greg"}, description = "Metadata search by Updater Name pattern matching", dependsOnMethods = "searchResourceByUpdaterNot")
    public void searchResourceByUpdaterNamePattern()
            throws SearchAdminServiceRegistryExceptionException, RemoteException,
            RegistryException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setUpdater("adm%");
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid Updater name pattern");
        for (ResourceData resourceData : result.getResourceDataList()) {
            Resource resource = registry.get(resourceData.getResourcePath());
            Assert.assertTrue((resource.getLastUpdaterUserName().startsWith("adm")),
                    "search word pattern not contain on Updater Name :" + resourceData.getResourcePath());
        }

    }


    @Test(groups = {"wso2.greg"}, description = "Metadata search by unavailable Updater Name", dependsOnMethods = "searchResourceByUpdaterNamePattern")
    public void searchResourceByUnAvailableUpdaterName()
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setUpdater("xyz1234");
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
        Assert.assertNull(result.getResourceDataList(), "Result Object found");


    }

    @Test(dataProvider = "invalidCharacter", groups = {"wso2.greg"}, dataProviderClass = Parameters.class,
            description = "Metadata search by Updater Name with invalid characters", dependsOnMethods = "searchResourceByUnAvailableUpdaterName")
    public void searchResourceByUpdaterNameWithInvalidCharacter(String invalidInput)
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();

        paramBean.setUpdater(invalidInput);
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
    public void destroy() throws RegistryException {
        registry.delete(resourcePath);
        searchAdminServiceClient = null;
        registry = null;
    }
}
