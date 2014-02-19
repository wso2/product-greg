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
package org.wso2.carbon.registry.search.metadata.test;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.registry.SearchAdminServiceClient;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.registry.search.metadata.test.bean.SearchParameterBean;
import org.wso2.carbon.registry.search.metadata.test.utils.GregTestUtils;
import org.wso2.carbon.registry.search.stub.SearchAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.search.stub.beans.xsd.ArrayOfString;
import org.wso2.carbon.registry.search.stub.beans.xsd.CustomSearchParameterBean;

import java.rmi.RemoteException;

/*
Save and Delete metadata search Filter test
*/
public class SaveDeleteFilterTestCase {

    private SearchAdminServiceClient searchAdminService;
    private final String filterName = "testFilter";

    @BeforeClass
    public void init() throws Exception {
        final String SERVER_URL = GregTestUtils.getServerUrl();
        ClientConnectionUtil.waitForPort(Integer.parseInt(FrameworkSettings.HTTP_PORT));
        String sessionCookie = new LoginLogoutUtil().login();

        searchAdminService = new SearchAdminServiceClient(SERVER_URL, sessionCookie);

    }

    @Test(priority = 1, groups = {"wso2.greg"}, description = "Save Metadata search filter")
    public void saveFilter() throws SearchAdminServiceRegistryExceptionException, RemoteException {
        CustomSearchParameterBean queryBean = new CustomSearchParameterBean();
        SearchParameterBean bean = new SearchParameterBean();
        bean.setResourceName("a");
        ArrayOfString[] paramList = bean.getParameterList();

        queryBean.setParameterValues(paramList);
        searchAdminService.saveAdvancedSearchFilter(queryBean, filterName);
        String[] filters = searchAdminService.getSavedFilters();
        boolean isFilterFound = false;
        for (String filter : filters) {
            if (filterName.equalsIgnoreCase(filter)) {
                isFilterFound = true;
                break;
            }
        }
        Assert.assertTrue(isFilterFound, "Filter saved failed");
        CustomSearchParameterBean filterBean = searchAdminService.getAdvancedSearchFilter(filterName);
        ArrayOfString[] criteria = filterBean.getParameterValues();
        String resourcePath = "";
        for (ArrayOfString array : criteria) {
            if ("resourcePath".equalsIgnoreCase(array.getArray()[0])) {
                resourcePath = array.getArray()[1];
                break;
            }
        }
        Assert.assertEquals(resourcePath, "a", "resource path value 'a' not found");

    }

    @Test(priority = 2, dependsOnMethods = {"saveFilter"}, groups = {"wso2.greg"}, description = "Edit Metadata search Filter")
    public void editFilter() throws SearchAdminServiceRegistryExceptionException, RemoteException {
        CustomSearchParameterBean queryBean = new CustomSearchParameterBean();
        SearchParameterBean bean = new SearchParameterBean();
        bean.setResourceName("x");
        ArrayOfString[] paramList = bean.getParameterList();

        queryBean.setParameterValues(paramList);
        searchAdminService.saveAdvancedSearchFilter(queryBean, filterName);
        String[] filters = searchAdminService.getSavedFilters();
        boolean isFilterFound = false;
        for (String filter : filters) {
            if (filterName.equalsIgnoreCase(filter)) {
                isFilterFound = true;
                break;
            }
        }
        Assert.assertTrue(isFilterFound, "Filter saved failed");
        CustomSearchParameterBean filterBean = searchAdminService.getAdvancedSearchFilter(filterName);
        ArrayOfString[] criteria = filterBean.getParameterValues();
        String resourcePath = "";
        for (ArrayOfString array : criteria) {
            if ("resourcePath".equalsIgnoreCase(array.getArray()[0])) {
                resourcePath = array.getArray()[1];
                break;
            }
        }
        Assert.assertEquals(resourcePath, "x", "Editing failed. resource path value 'x' not found");

    }

    @Test(priority = 3, dependsOnMethods = {"editFilter"}, groups = {"wso2.greg"}, description = "Delete Metadata search Filter")
    public void deleteFilter()
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        boolean isFilterFound = false;
        searchAdminService.deleteFilter(filterName);
        String[] filters = searchAdminService.getSavedFilters();
        if (filters != null && filters.length > 0) {
            for (String filter : filters) {
                if (filterName.equalsIgnoreCase(filter)) {
                    isFilterFound = true;
                    break;
                }
            }

            Assert.assertFalse(isFilterFound, "Filter Deletion Failed");
        }

    }


}
