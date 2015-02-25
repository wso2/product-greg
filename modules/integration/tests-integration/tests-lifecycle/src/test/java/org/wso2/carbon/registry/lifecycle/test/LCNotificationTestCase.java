/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.registry.lifecycle.test;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.test.utils.common.FileManager;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.lifecycle.test.bean.SearchParameterBean;
import org.wso2.carbon.registry.lifecycle.test.utils.LifeCycleUtils;
import org.wso2.carbon.registry.search.stub.beans.xsd.AdvancedSearchResultsBean;
import org.wso2.carbon.registry.search.stub.beans.xsd.ArrayOfString;
import org.wso2.carbon.registry.search.stub.beans.xsd.CustomSearchParameterBean;
import org.wso2.carbon.registry.search.stub.common.xsd.ResourceData;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.clients.ActivityAdminServiceClient;
import org.wso2.greg.integration.common.clients.LifeCycleAdminServiceClient;
import org.wso2.greg.integration.common.clients.LifeCycleManagementClient;
import org.wso2.greg.integration.common.clients.SearchAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.GREGTestConstants;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import java.io.File;
import java.rmi.RemoteException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * This test class includes tests for a lifecycle which has checkpoints.
 */
public class LCNotificationTestCase extends GREGIntegrationBaseTest {

    /**
     * Registry service client used to perform test operations.
     */
    private WSRegistryServiceClient wsRegistry;

    /**
     * Lifecycle admin service.
     */
    private LifeCycleAdminServiceClient lifeCycleAdminService;

    /**
     * Lifecycle management service client.
     */
    private LifeCycleManagementClient lifeCycleManagementClient;

    /**
     * Activity management service client.
     */
    private ActivityAdminServiceClient activityAdminServiceClient;

    /**
     * Search admin service.
     */
    private SearchAdminServiceClient searchAdminService;

    /**
     * Lifecycle name which is used in this test operations.
     */
    private final String ASPECT_NAME = "CheckpointServiceLC";

    /**
     * Lifecycle notification test service name.
     */
    private final String TEST_SERVICE_NAME = "LifeCycleNotificationTestService";

    /**
     * Service path.
     */
    private String servicePathDev;

    /**
     * Username without domain.
     */
    private String userNameWithoutDomain;

    /**
     * Init method to run before running the test cases.
     *
     */
    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String sessionCookie = getSessionCookie();
        lifeCycleAdminService = new LifeCycleAdminServiceClient(backendURL, sessionCookie);
        activityAdminServiceClient = new ActivityAdminServiceClient(backendURL, sessionCookie);
        lifeCycleManagementClient = new LifeCycleManagementClient(backendURL, sessionCookie);
        searchAdminService = new SearchAdminServiceClient(backendURL, sessionCookie);
        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        wsRegistry = registryProviderUtil.getWSRegistry(automationContext);
        Registry governance = registryProviderUtil.getGovernanceRegistry(wsRegistry, automationContext);
        LifeCycleUtils.deleteLifeCycleIfExist(ASPECT_NAME, lifeCycleManagementClient);
        servicePathDev = GREGTestConstants.GOVERNANCE_LOCATION + LifeCycleUtils
                .addService("sns", TEST_SERVICE_NAME, governance);
        Thread.sleep(1000);

        String userName = automationContext.getContextTenant().getContextUser().getUserName();
        if (userName.contains("@")) {
            userNameWithoutDomain = userName.substring(0, userName.indexOf('@'));
        } else {
            userNameWithoutDomain = userName;
        }
    }

    /**
     * THis test is used to test lifecycle addition with checkpoints in lifecycle state element.
     *
     * @throws java.io.IOException
     * @throws org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException
     * @throws InterruptedException
     * @throws org.wso2.carbon.registry.search.stub.SearchAdminServiceRegistryExceptionException
     */
    @Test(groups = "wso2.greg", description = "Add new Life Cycle with has checkpoints in states.")
    public void createNewLifeCycle() throws Exception {

        String filePath = getTestArtifactLocation() + GREGTestConstants.ARTIFACTS + File.separator +
                GREGTestConstants.GREG + File.separator + GREGTestConstants.LIFECYCLE + File.separator +
                GREGTestConstants.CHECKPOINT_LIFECYCLE;
        String lifeCycleConfiguration = FileManager.readFile(filePath);
        assertTrue(lifeCycleManagementClient.addLifeCycle(lifeCycleConfiguration), "Adding New LifeCycle Failed");
        Thread.sleep(2000);
        lifeCycleConfiguration = lifeCycleManagementClient.getLifecycleConfiguration(ASPECT_NAME);
        assertTrue(lifeCycleConfiguration.contains("aspect name=\"" + ASPECT_NAME + "\""),
                "LifeCycleName Not Found in lifecycle configuration");
        String[] lifeCycleList = lifeCycleManagementClient.getLifecycleList();
        assertNotNull(lifeCycleList);
        assertTrue(lifeCycleList.length > 0, "Life Cycle List length zero");
        boolean found = false;
        for (String lc : lifeCycleList) {
            if (ASPECT_NAME.equalsIgnoreCase(lc)) {
                found = true;
            }
        }
        assertTrue(found, "Life Cycle list not contain newly added life cycle");
        //Metadata Search By Life Cycle Name
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setResourceName(ASPECT_NAME);
        ArrayOfString[] paramList = paramBean.getParameterList();
        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminService.getAdvancedSearchResults(searchQuery);
        assertNotNull(result.getResourceDataList(), "No Record Found");
        assertTrue((result.getResourceDataList().length == 1), "No Record Found for Life Cycle Name or more record " +
                "found");
        for (ResourceData resource : result.getResourceDataList()) {
            assertEquals(resource.getName(), ASPECT_NAME,
                    "Life Cycle Name mismatched :" + resource.getResourcePath());
            assertTrue(resource.getResourcePath().contains("lifecycles"),
                    "Life Cycle Path does not contain lifecycles collection :" + resource.getResourcePath());
        }
    }

    /**
     * This method is used to clean up after running lifecycle checkpoint test cases.
     *
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
     * @throws org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException
     * @throws java.rmi.RemoteException
     */
    @AfterClass()
    public void cleanup() throws RegistryException, LifeCycleManagementServiceExceptionException,
            RemoteException {
        if (wsRegistry.resourceExists(servicePathDev)) {
            wsRegistry.delete(servicePathDev);
        }
        LifeCycleUtils.deleteLifeCycleIfExist(ASPECT_NAME, lifeCycleManagementClient);

    }
}
