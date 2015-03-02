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
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.beans.xsd.LifecycleBean;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.governance.list.stub.ListMetadataServiceRegistryExceptionException;
import org.wso2.carbon.governance.services.stub.AddServicesServiceRegistryExceptionException;
import org.wso2.carbon.integration.common.admin.client.UserManagementClient;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.info.stub.RegistryExceptionException;
import org.wso2.carbon.registry.info.stub.beans.xsd.SubscriptionBean;
import org.wso2.carbon.registry.lifecycle.test.bean.SearchParameterBean;
import org.wso2.carbon.registry.lifecycle.test.utils.LifeCycleUtils;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.search.stub.SearchAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.search.stub.beans.xsd.AdvancedSearchResultsBean;
import org.wso2.carbon.registry.search.stub.beans.xsd.ArrayOfString;
import org.wso2.carbon.registry.search.stub.beans.xsd.CustomSearchParameterBean;
import org.wso2.carbon.registry.search.stub.common.xsd.ResourceData;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.clients.InfoServiceAdminClient;
import org.wso2.greg.integration.common.clients.LifeCycleAdminServiceClient;
import org.wso2.greg.integration.common.clients.LifeCycleManagementClient;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.clients.SearchAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.GREGTestConstants;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import javax.activation.DataHandler;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * This test class includes tests for a lifecycle which has checkpoints.
 */
public class LCCheckpointNotificationTestCase extends GREGIntegrationBaseTest {

    /**
     * Registry service client used to perform test operations.
     */
    private WSRegistryServiceClient wsRegistryServiceClient;

    /**
     * Lifecycle admin service.
     */
    private LifeCycleAdminServiceClient lifeCycleAdminServiceClient;

    /**
     * Lifecycle management service client.
     */
    private LifeCycleManagementClient lifeCycleManagementClient;

    /**
     * Search admin service.
     */
    private SearchAdminServiceClient searchAdminService;

    /**
     * Lifecycle name which is used in this test operations.
     */
    private final String aspectName = "CheckpointServiceLC";

    /**
     * Username without domain.
     */
    private String userNameWithoutDomain;

    /**
     * System governance registry path.
     */
    private final String govPath = "/_system/governance";

    /**
     * Resource admin service client.
     */
    private ResourceAdminServiceClient resourceAdminServiceClient;

    /**
     * Information admin service client.
     */
    private InfoServiceAdminClient infoServiceAdminClient;

    /**
     * User management client.
     */
    private UserManagementClient userManagementClient;

    /**
     * Registry path of the service which is created for the test cases.
     */
    private String serviceString = "/trunk/services/com/abb/1.0.0-SNAPSHOT/IntergalacticService9";

    /**
     * Absolute registry path of the service.
     */
    private final String absPath = govPath + serviceString;

    /**
     * Session cookie.
     */
    private String sessionCookie;

    /**
     * Lifecycle bean.
     */
    private LifecycleBean lifeCycle;

    /**
     * Init method to run before running the test cases.
     *
     * @throws Exception
     */
    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        sessionCookie = getSessionCookie();
        infoServiceAdminClient = new InfoServiceAdminClient(backendURL, sessionCookie);
        lifeCycleAdminServiceClient = new LifeCycleAdminServiceClient(backendURL, sessionCookie);
        lifeCycleManagementClient = new LifeCycleManagementClient(backendURL, sessionCookie);
        searchAdminService = new SearchAdminServiceClient(backendURL, sessionCookie);
        resourceAdminServiceClient = new ResourceAdminServiceClient(backendURL, sessionCookie);
        userManagementClient = new UserManagementClient(backendURL, sessionCookie);

        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        wsRegistryServiceClient = registryProviderUtil.getWSRegistry(automationContext);

        LifeCycleUtils.deleteLifeCycleIfExist(aspectName, lifeCycleManagementClient);

        String userName = automationContext.getContextTenant().getContextUser().getUserName();
        if (userName.contains("@")) {
            userNameWithoutDomain = userName.substring(0, userName.indexOf('@'));
        } else {
            userNameWithoutDomain = userName;
        }
    }

    /**
     * This test is used to test lifecycle addition with checkpoints in lifecycle state element.
     *
     * @throws IOException
     * @throws LifeCycleManagementServiceExceptionException
     * @throws InterruptedException
     * @throws SearchAdminServiceRegistryExceptionException
     */
    @Test(groups = "wso2.greg", description = "Add new Life Cycle with has checkpoints in states.")
    public void createNewLifeCycle() throws IOException, LifeCycleManagementServiceExceptionException,
            InterruptedException, SearchAdminServiceRegistryExceptionException {

        String filePath = getTestArtifactLocation() + GREGTestConstants.ARTIFACTS + File.separator
                + GREGTestConstants.GREG + File.separator + GREGTestConstants.LIFECYCLE + File.separator
                + GREGTestConstants.CHECKPOINT_LIFECYCLE;
        String lifeCycleConfiguration = FileManager.readFile(filePath);
        assertTrue(lifeCycleManagementClient.addLifeCycle(lifeCycleConfiguration), "Adding New LifeCycle Failed");
        Thread.sleep(2000);
        lifeCycleConfiguration = lifeCycleManagementClient.getLifecycleConfiguration(aspectName);
        assertTrue(lifeCycleConfiguration.contains("aspect name=\"" + aspectName + "\""),
                "LifeCycleName Not Found in lifecycle configuration");
        String[] lifeCycleList = lifeCycleManagementClient.getLifecycleList();
        assertNotNull(lifeCycleList);
        assertTrue(lifeCycleList.length > 0, "Life Cycle List length zero");
        boolean found = false;
        for (String lc : lifeCycleList) {
            if (aspectName.equalsIgnoreCase(lc)) {
                found = true;
            }
        }
        assertTrue(found, "Life Cycle list not contain newly added life cycle");
        //Metadata Search By Life Cycle Name
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setResourceName(aspectName);
        ArrayOfString[] paramList = paramBean.getParameterList();
        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminService.getAdvancedSearchResults(searchQuery);
        assertNotNull(result.getResourceDataList(), "No Record Found");
        assertTrue((result.getResourceDataList().length == 1), "No Record Found for Life Cycle Name or more record "
                + "found");
        for (ResourceData resource : result.getResourceDataList()) {
            assertEquals(resource.getName(), aspectName, "Life Cycle Name mismatched :" + resource.getResourcePath());
            assertTrue(resource.getResourcePath().contains("lifecycles"),
                    "Life Cycle Path does not contain lifecycles collection :" + resource.getResourcePath());
        }
    }

    /**
     * This method act as the test case to create a service.
     *
     * @throws XMLStreamException
     * @throws IOException
     * @throws AddServicesServiceRegistryExceptionException
     * @throws ListMetadataServiceRegistryExceptionException
     * @throws ResourceAdminServiceExceptionException
     */
    @Test(groups = "wso2.greg", description = "Create a service", dependsOnMethods = "createNewLifeCycle")
    public void testCreateService() throws XMLStreamException, IOException,
            AddServicesServiceRegistryExceptionException, ListMetadataServiceRegistryExceptionException,
            ResourceAdminServiceExceptionException {

        String servicePath = getTestArtifactLocation() + "artifacts" + File.separator + "GREG" + File.separator
                + "services" + File.separator + "intergalacticService9.metadata.xml";
        DataHandler dataHandler = new DataHandler(new URL("file:///" + servicePath));
        String mediaType = "application/vnd.wso2-service+xml";
        String description = "This is a test service";
        String destinationPath = govPath + "/service";
        resourceAdminServiceClient.addResource(destinationPath, mediaType, description, dataHandler);
        org.wso2.carbon.registry.resource.stub.common.xsd.ResourceData[] data = resourceAdminServiceClient
                .getResource(absPath);
        assertNotNull(data, "Service not found");
    }

    /**
     * This method act as the test case to subscribe for Checkpoint Notification.
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Subscribe LC Approval Needed notification while state change",
            dependsOnMethods = "testAddLcToService")
    public void testSubscribeLCCheckpointNotification() throws Exception {
        addRole();
        assertTrue(consoleNotificationSubscribe(absPath, "CheckpointNotification"));
    }

    /**
     * This method act as the test case to create a service.
     *
     * @throws RegistryException
     * @throws CustomLifecyclesChecklistAdminServiceExceptionException
     * @throws RemoteException
     */
    @Test(groups = "wso2.greg", description = "Add lifecycle to a service", dependsOnMethods = "testCreateService")
    public void testAddLcToService()
            throws RegistryException, CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException {

        wsRegistryServiceClient.associateAspect(absPath, aspectName);
        lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(absPath);
        Property[] properties = lifeCycle.getLifecycleProperties();
        boolean lifecycleStatus = false;
        for (Property prop : properties) {
            if (prop.getKey().contains(aspectName)) {
                lifecycleStatus = true;
            }
        }
        assertTrue(lifecycleStatus, "LifeCycle not added to service");
    }

    /**
     * This method is used to subscribe for a notification.
     *
     * @param path                  registry path of the resource.
     * @param eventType             event type to subscribe.
     * @return                      true if subscription is done.
     * @throws RemoteException
     * @throws RegistryException
     */
    private boolean consoleNotificationSubscribe(String path, String eventType)
            throws RemoteException, RegistryException {
        // subscribe for management console notifications
        SubscriptionBean bean = infoServiceAdminClient.subscribe(path, "work://RoleSubscriptionTest", eventType,
                sessionCookie);
        return bean.getSubscriptionInstances() != null;
    }

    /**
     * Method used to add a role
     *
     * @return              true if the role is successfully added.
     * @throws Exception
     */
    private boolean addRole() throws Exception {
        if (!userManagementClient.roleNameExists("RoleSubscriptionTest")) {
            userManagementClient.addRole("RoleSubscriptionTest", new String[] { userNameWithoutDomain },
                    new String[] { "/permission/admin/manage",
                            "/permission/admin/manage/add",
                            "/permission/admin/manage/add/module",
                            "/permission/admin/manage/add/service",
                            "/permission/admin/manage/add/webapp",
                            "/permission/admin/manage/api",
                            "/permission/admin/manage/api/create",
                            "/permission/admin/manage/api/publish",
                            "/permission/admin/manage/api/subscribe",
                            "/permission/admin/manage/attachment",
                            "/permission/admin/manage/extensions",
                            "/permission/admin/manage/extensions/add",
                            "/permission/admin/manage/extensions/list",
                            "/permission/admin/manage/humantask",
                            "/permission/admin/manage/humantask/add",
                            "/permission/admin/manage/humantask/packages",
                            "/permission/admin/manage/humantask/task",
                            "/permission/admin/manage/humantask/viewtasks",
                            "/permission/admin/manage/manage_tiers",
                            "/permission/admin/manage/modify",
                            "/permission/admin/manage/modify/module",
                            "/permission/admin/manage/modify/service",
                            "/permission/admin/manage/modify/user-profile",
                            "/permission/admin/manage/modify/webapp",
                            "/permission/admin/manage/resources",
                            "/permission/admin/manage/resources/associations",
                            "/permission/admin/manage/resources/browse",
                            "/permission/admin/manage/resources/community-features",
                            "/permission/admin/manage/resources/govern",
                            "/permission/admin/manage/resources/govern/api",
                            "/permission/admin/manage/resources/govern/api/add",
                            "/permission/admin/manage/resources/govern/api/list",
                            "/permission/admin/manage/resources/govern/document",
                            "/permission/admin/manage/resources/govern/document/add",
                            "/permission/admin/manage/resources/govern/document/list",
                            "/permission/admin/manage/resources/govern/ebook",
                            "/permission/admin/manage/resources/govern/ebook/add",
                            "/permission/admin/manage/resources/govern/ebook/list",
                            "/permission/admin/manage/resources/govern/endpoint",
                            "/permission/admin/manage/resources/govern/endpoint/add",
                            "/permission/admin/manage/resources/govern/endpoint/list",
                            "/permission/admin/manage/resources/govern/gadget",
                            "/permission/admin/manage/resources/govern/gadget/add",
                            "/permission/admin/manage/resources/govern/gadget/list",
                            "/permission/admin/manage/resources/govern/generic",
                            "/permission/admin/manage/resources/govern/generic/add",
                            "/permission/admin/manage/resources/govern/generic/list",
                            "/permission/admin/manage/resources/govern/lifecycles",
                            "/permission/admin/manage/resources/govern/metadata",
                            "/permission/admin/manage/resources/govern/policy",
                            "/permission/admin/manage/resources/govern/policy/add",
                            "/permission/admin/manage/resources/govern/policy/list",
                            "/permission/admin/manage/resources/govern/provider",
                            "/permission/admin/manage/resources/govern/provider/add",
                            "/permission/admin/manage/resources/govern/provider/list",
                            "/permission/admin/manage/resources/govern/proxy",
                            "/permission/admin/manage/resources/govern/proxy/add",
                            "/permission/admin/manage/resources/govern/proxy/list",
                            "/permission/admin/manage/resources/govern/schema",
                            "/permission/admin/manage/resources/govern/schema/add",
                            "/permission/admin/manage/resources/govern/schema/list",
                            "/permission/admin/manage/resources/govern/sequence",
                            "/permission/admin/manage/resources/govern/sequence/add",
                            "/permission/admin/manage/resources/govern/sequence/list",
                            "/permission/admin/manage/resources/govern/service",
                            "/permission/admin/manage/resources/govern/service/add",
                            "/permission/admin/manage/resources/govern/service/list",
                            "/permission/admin/manage/resources/govern/servicex",
                            "/permission/admin/manage/resources/govern/servicex/add",
                            "/permission/admin/manage/resources/govern/servicex/list",
                            "/permission/admin/manage/resources/govern/site",
                            "/permission/admin/manage/resources/govern/site/add",
                            "/permission/admin/manage/resources/govern/site/list",
                            "/permission/admin/manage/resources/govern/uri",
                            "/permission/admin/manage/resources/govern/uri/add",
                            "/permission/admin/manage/resources/govern/uri/list",
                            "/permission/admin/manage/resources/govern/wadl",
                            "/permission/admin/manage/resources/govern/wadl/add",
                            "/permission/admin/manage/resources/govern/wadl/list",
                            "/permission/admin/manage/resources/govern/wsdl",
                            "/permission/admin/manage/resources/govern/wsdl/add",
                            "/permission/admin/manage/resources/govern/wsdl/list",
                            "/permission/admin/manage/resources/notifications",
                            "/permission/admin/manage/resources/ws-api",
                            "/permission/admin/manage/search",
                            "/permission/admin/manage/search/activities",
                            "/permission/admin/manage/search/advanced-search",
                            "/permission/admin/manage/search/resources",
                            "/permission/admin/manage/uddipublish",
                            "/permission/admin/manage/workflowadmin" });
        }
        return userManagementClient.roleNameExists("RoleSubscriptionTest");
    }

    /**
     * THis method is used to unsubscribe from management console notifications.
     * @param path                          Registry path to the service.
     * @return                              true if successfully unsubscribed.
     * @throws RegistryException
     * @throws RegistryExceptionException
     * @throws RemoteException
     */
    public boolean consoleNotificationUnsubscribe(String path)
            throws RegistryException, RegistryExceptionException, RemoteException {

        SubscriptionBean sBean = infoServiceAdminClient.getSubscriptions(path, sessionCookie);
        infoServiceAdminClient.unsubscribe(path, sBean.getSubscriptionInstances()[0].getId(), sessionCookie);
        sBean = infoServiceAdminClient.getSubscriptions(path, sessionCookie);
        return (sBean.getSubscriptionInstances() == null);
    }

    /**
     * This method is used to clean up after running lifecycle checkpoint test cases.
     *
     * @throws Exception
     */
    @AfterClass()
    public void cleanup() throws Exception {
        // Remove Subscription from service.
        assertTrue(consoleNotificationUnsubscribe(absPath));
        // Delete created service.
        if (wsRegistryServiceClient.resourceExists(absPath)) {
            resourceAdminServiceClient.deleteResource(absPath);
        }
        // Delete created lifecycle.
        LifeCycleUtils.deleteLifeCycleIfExist(aspectName, lifeCycleManagementClient);
        infoServiceAdminClient = null;
        lifeCycleAdminServiceClient = null;
        lifeCycleManagementClient = null;
        resourceAdminServiceClient = null;
        userManagementClient = null;
        wsRegistryServiceClient = null;
    }
}
