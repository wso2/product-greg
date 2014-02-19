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
package org.wso2.carbon.registry.lifecycle.test.old;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.governance.LifeCycleManagementClient;
import org.wso2.carbon.automation.api.clients.registry.SearchAdminServiceClient;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.lifecycle.test.utils.LifeCycleUtils;
import org.wso2.carbon.registry.search.metadata.test.bean.SearchParameterBean;
import org.wso2.carbon.registry.search.stub.SearchAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.search.stub.beans.xsd.AdvancedSearchResultsBean;
import org.wso2.carbon.registry.search.stub.beans.xsd.ArrayOfString;
import org.wso2.carbon.registry.search.stub.beans.xsd.CustomSearchParameterBean;
import org.wso2.carbon.registry.search.stub.common.xsd.ResourceData;

import java.rmi.RemoteException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class EditLifeCycleNameLCTestCase {

    private int userId = 1;
    private UserInfo userInfo = UserListCsvReader.getUserInfo(userId);

    private LifeCycleManagementClient lifeCycleManagementClient;
    private SearchAdminServiceClient searchAdminServiceClient;

    private final String ASPECT_NAME = "NewServiceLifeCycle";
    private final String NEW_ASPECT_NAME = "EditedServiceLC";

    /**
     * @throws Exception
     */
    @BeforeClass
    public void init() throws Exception {
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        ManageEnvironment environment = builder.build();

        lifeCycleManagementClient = new LifeCycleManagementClient(
                environment.getGreg()
                        .getProductVariables()
                        .getBackendUrl(),
                userInfo.getUserName(),
                userInfo.getPassword());

        searchAdminServiceClient = new SearchAdminServiceClient(environment.getGreg()
                                                                        .getProductVariables()
                                                                        .getBackendUrl(),
                                                                userInfo.getUserName(),
                                                                userInfo.getPassword());


        LifeCycleUtils.deleteLifeCycleIfExist(ASPECT_NAME, lifeCycleManagementClient);
        LifeCycleUtils.deleteLifeCycleIfExist(NEW_ASPECT_NAME, lifeCycleManagementClient);
        Thread.sleep(1000);
        LifeCycleUtils.createNewLifeCycle(ASPECT_NAME, lifeCycleManagementClient);
        Thread.sleep(1000);

    }

    /**
     * @throws org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException
     *
     * @throws java.rmi.RemoteException
     * @throws InterruptedException
     */
    @Test(groups = "wso2.greg", description = "Edit Life Cycle Name")
    public void editLifeCycleName()
            throws LifeCycleManagementServiceExceptionException, RemoteException,
                   InterruptedException {
        String config = lifeCycleManagementClient.getLifecycleConfiguration(ASPECT_NAME);
        assertTrue(config.contains("aspect name=\"" + ASPECT_NAME + "\""),
                   "LifeCycleName Not Found in lifecycle configuration");
        String newLifeCycleConfiguration = config.replace(ASPECT_NAME, NEW_ASPECT_NAME);
        assertTrue(lifeCycleManagementClient.editLifeCycle(ASPECT_NAME, newLifeCycleConfiguration)
                , "Editing LifeCycle Name Failed");
        Thread.sleep(1000);

        newLifeCycleConfiguration = lifeCycleManagementClient.getLifecycleConfiguration(NEW_ASPECT_NAME);
        assertTrue(newLifeCycleConfiguration.contains("aspect name=\"" + NEW_ASPECT_NAME + "\""),
                   "New LifeCycleName Not Found in lifecycle configuration");

        String[] lifeCycleList = lifeCycleManagementClient.getLifecycleList();
        assertNotNull(lifeCycleList);
        assertTrue(lifeCycleList.length > 0, "Life Cycle List length zero");
        boolean found = false;
        for (String lc : lifeCycleList) {
            if (NEW_ASPECT_NAME.equalsIgnoreCase(lc)) {
                found = true;
            }
        }
        assertTrue(found, "Life Cycle list not contain edited life cycle");

        //Old Name should not be here
        found = false;
        for (String lc : lifeCycleList) {
            if (ASPECT_NAME.equalsIgnoreCase(lc)) {
                found = true;
            }
        }
        assertFalse(found, "Life Cycle list not contain Old life cycle");
    }

    /**
     * @throws org.wso2.carbon.registry.search.stub.SearchAdminServiceRegistryExceptionException
     *
     * @throws java.rmi.RemoteException
     */
    @Test(groups = "wso2.greg", description = "Metadata Search for Edited Life Cycle", dependsOnMethods = {"editLifeCycleName"})
    public void searchNewLifeCycleByName()
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setResourceName(NEW_ASPECT_NAME);
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
        assertNotNull(result.getResourceDataList(), "No Record Found");
        assertTrue((result.getResourceDataList().length == 1), "No Record Found for Life Cycle " +
                                                               "Name or more record found");
        for (ResourceData resource : result.getResourceDataList()) {
            assertEquals(resource.getName(), NEW_ASPECT_NAME,
                         "Life Cycle Name mismatched :" + resource.getResourcePath());
            assertTrue(resource.getResourcePath().contains("lifecycles"),
                       "Life Cycle Path does not contain lifecycles collection :" + resource.getResourcePath());
        }
    }

    /**
     * @throws org.wso2.carbon.registry.search.stub.SearchAdminServiceRegistryExceptionException
     *
     * @throws java.rmi.RemoteException
     */
    @Test(groups = "wso2.greg", description = "Metadata search by old Life Cycle Name", dependsOnMethods = {"editLifeCycleName"})
    public void searchOldLifeCycleByName()
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setResourceName(ASPECT_NAME);
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
        assertNull(result.getResourceDataList(), "Life Cycle Record Found even if it is deleted");

    }

    /**
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
     *
     * @throws org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException
     *
     * @throws java.rmi.RemoteException
     */
    @Test(groups = "wso2.greg", description = "Delete added resources", dependsOnMethods = "searchOldLifeCycleByName")
    public void deleteResources()
            throws RegistryException, LifeCycleManagementServiceExceptionException,
                   RemoteException {

        assertTrue(lifeCycleManagementClient.deleteLifeCycle(NEW_ASPECT_NAME),
                   "Life Cycle Deleted failed");

    }

    /**
     * @throws org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException
     *
     * @throws java.rmi.RemoteException
     */
    @AfterClass()
    public void cleanup() throws LifeCycleManagementServiceExceptionException, RemoteException {

        LifeCycleUtils.deleteLifeCycleIfExist(NEW_ASPECT_NAME, lifeCycleManagementClient);

    }

}
