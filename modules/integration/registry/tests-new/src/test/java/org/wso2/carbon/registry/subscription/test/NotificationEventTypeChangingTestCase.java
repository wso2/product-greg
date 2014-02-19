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
package org.wso2.carbon.registry.subscription.test;

import static org.testng.Assert.assertTrue;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import javax.activation.DataHandler;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.governance.LifeCycleManagementClient;
import org.wso2.carbon.automation.api.clients.registry.PropertiesAdminServiceClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.properties.stub.PropertiesAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.subscription.test.util.LifecycleUtil;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

public class NotificationEventTypeChangingTestCase {

    private ManageEnvironment environment;
    private UserInfo userInfo;
    private ResourceAdminServiceClient resourceAdminServiceClient;

    private static final String ROOT = "/";
    private static final String RESOURCE_LEAF_PATH_NAME = "/_system/";
    private static final String COLLECTION_PATH_NAME = "/_system/";
    private static final String COLLECTION_NAME = "testCollection";


    @BeforeClass(alwaysRun = true)
    public void initialize() throws RemoteException, LoginAuthenticationExceptionException {
        int userID = ProductConstant.ADMIN_USER_ID;
        userInfo = UserListCsvReader.getUserInfo(userID);
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userID);
        environment = builder.build();
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                               userInfo.getUserName(), userInfo.getPassword());

    }

    @DataProvider(name = "ResourceDataProvider")
    public Object[][] dp() {
        return new Object[][]{
                new Object[]{"service.metadata.xml", "application/xml", "services"},
                new Object[]{"policy.xml", "application/xml", "policy"},
                new Object[]{"test.map", "Unknown", "mediatypes"},};
    }

    /**
     * add resources to root and to a leaf level collection
     *
     * @param name   resource name
     * @param type   resource type
     * @param folder folder where the resource exist
     * @throws MalformedURLException
     * @throws RemoteException
     * @throws ResourceAdminServiceExceptionException
     *
     */
    @Test(groups = "wso2.greg", description = "Add resource", dataProvider = "ResourceDataProvider")
    public void testAddResource(String name, String type, String folder)
            throws MalformedURLException, RemoteException, ResourceAdminServiceExceptionException {
        String resourcePath =
                ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
                File.separator + "GREG" + File.separator + folder + File.separator + name;
        DataHandler dh = new DataHandler(new URL("file:///" + resourcePath));
        resourceAdminServiceClient.addResource(ROOT + name, type, "testDesc", dh);
        resourceAdminServiceClient.addResource(RESOURCE_LEAF_PATH_NAME + name, type, "testDesc", dh);
        assertTrue(resourceAdminServiceClient.getResource(ROOT + name)[0].getAuthorUserName()
                           .contains(userInfo.getUserNameWithoutDomain()));
        assertTrue(resourceAdminServiceClient.getResource(RESOURCE_LEAF_PATH_NAME + name)[0].getAuthorUserName()
                           .contains(userInfo.getUserNameWithoutDomain()));

    }

    /**
     * Change event type of notification for resources at root and get
     * notifications to each event type
     *
     * @param name
     * @param type   resource type
     * @param folder folder where the resource exist
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Get console Notification when event type changes",
          dataProvider = "ResourceDataProvider", dependsOnMethods = "testAddResource")
    public void testSubscriptionEventTypeChangeRootLevelResource(String name, String type,
                                                                 String folder) throws Exception {
        assertTrue(new LifecycleUtil().init(ROOT + name, environment, userInfo, "Resource"));
    }

    /**
     * Change event type of notification for resources at leaf level and get
     * notifications to each event type
     *
     * @param name
     * @param type   resource type
     * @param folder folder where the resource exist
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Get console Notification when event type changes",
          dataProvider = "ResourceDataProvider", dependsOnMethods = "testSubscriptionEventTypeChangeRootLevelResource")
    public void testSubscriptionEventTypeChangeLeafLevelResource(String name, String type,
                                                                 String folder) throws Exception {
        assertTrue(new LifecycleUtil().init(RESOURCE_LEAF_PATH_NAME + name, environment, userInfo,
                                            "Resource"));
    }

    /**
     * add wsdl
     *
     * @throws MalformedURLException
     * @throws RemoteException
     * @throws ResourceAdminServiceExceptionException
     *
     */
    @Test(groups = "wso2.greg", description = "Add wsdl",
          dependsOnMethods = "testSubscriptionEventTypeChangeLeafLevelResource")
    public void testAddWsdl()
            throws MalformedURLException, RemoteException, ResourceAdminServiceExceptionException {
        String resourcePath =
                ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
                File.separator + "GREG" + File.separator + "wsdl" +
                File.separator + "AmazonWebServices.wsdl";

        DataHandler dh = new DataHandler(new URL("file:///" + resourcePath));
        resourceAdminServiceClient.addResource(ROOT + "AmazonWebServices.wsdl", "application/wsdl+xml", "testDesc", dh);
        assertTrue(resourceAdminServiceClient.getResource("/_system/governance/trunk/wsdls/com/amazon/" +
                                                          "soap/AmazonWebServices.wsdl")
                           [0].getAuthorUserName().contains(userInfo.getUserNameWithoutDomain()));
    }

    /**
     * Change event type of notification for a wsdl and get
     * notifications to each event type
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Get console Notification when event type changes",
          dependsOnMethods = "testAddWsdl")
    public void testSubscriptionEventTypeChangeWsdl() throws Exception {
        assertTrue(new LifecycleUtil().init("/_system/governance/trunk/wsdls/com/amazon/soap/AmazonWebServices.wsdl",
                                            environment, userInfo, "Resource"));
    }

    /**
     * Change event type of notification for root and get
     * notifications to each event type
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Get console Notification when event type change",
          dependsOnMethods = "testSubscriptionEventTypeChangeWsdl")
    public void testSubscriptionEventTypeChangeRoot() throws Exception {
        assertTrue(new LifecycleUtil().init(ROOT, environment, userInfo, "Collection"));
    }

    /**
     * add collection to leaf level
     *
     * @throws RemoteException
     * @throws ResourceAdminServiceExceptionException
     *
     */
    @Test(groups = "wso2.greg", description = "Add collection", dependsOnMethods = "testSubscriptionEventTypeChangeRoot")
    public void testAddCollection() throws RemoteException, ResourceAdminServiceExceptionException {
        resourceAdminServiceClient.addCollection(COLLECTION_PATH_NAME, COLLECTION_NAME, "other",
                                                 "test collection");
        String authorUserName =
                resourceAdminServiceClient.getResource(COLLECTION_PATH_NAME +
                                                       COLLECTION_NAME)[0].getAuthorUserName();
        assertTrue(userInfo.getUserNameWithoutDomain().equalsIgnoreCase(authorUserName),
                   "Leaf collection creation failure");

    }

    /**
     * Change event type of notification for leaf level collection and get
     * notifications to each event type
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Get console Notification when event type change",
          dependsOnMethods = "testAddCollection")
    public void testSubscriptionEventTypeChangeLeafLevelCollection() throws Exception {
        assertTrue(new LifecycleUtil().init(COLLECTION_PATH_NAME + COLLECTION_NAME, environment,
                                            userInfo, "Collection"));
    }

    /**
     * add collection to root level
     *
     * @throws RemoteException
     * @throws ResourceAdminServiceExceptionException
     *
     */
    @Test(groups = "wso2.greg", description = "Add root collection",
          dependsOnMethods = "testSubscriptionEventTypeChangeLeafLevelCollection")
    public void testAddRootCollection() throws RemoteException,
                                               ResourceAdminServiceExceptionException {
        resourceAdminServiceClient.addCollection(ROOT, COLLECTION_NAME, "other", "test collection");
        String authorUserName =
                resourceAdminServiceClient.getResource(ROOT + COLLECTION_NAME)[0].getAuthorUserName();
        assertTrue(userInfo.getUserNameWithoutDomain().equalsIgnoreCase(authorUserName),
                   "Root collection creation failure");

    }

    /**
     * Change event type of notification for root level collection and get
     * notifications to each event type
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Get console Notification when event type change",
          dependsOnMethods = "testAddRootCollection")
    public void testSubscriptionEventTypeChangeRootLevelCollection() throws Exception {
        assertTrue(new LifecycleUtil().init(ROOT + COLLECTION_NAME, environment, userInfo, "Collection"));
    }

    @AfterClass(alwaysRun = true)
    public void clean() throws RegistryException, NumberFormatException, RemoteException,
                               PropertiesAdminServiceRegistryExceptionException,
                               ResourceAdminServiceExceptionException,
                               LifeCycleManagementServiceExceptionException {
        WSRegistryServiceClient wsRegistryServiceClient =
                new RegistryProviderUtil().getWSRegistry(Integer.parseInt(userInfo.getUserId()),
                                                         ProductConstant.GREG_SERVER_NAME);
        wsRegistryServiceClient.removeAspect("StateDemoteLC");
        LifeCycleManagementClient lifeCycleManagementClient =
                new LifeCycleManagementClient(environment.getGreg().getProductVariables()
                                                      .getBackendUrl(), userInfo.getUserName(),
                                              userInfo.getPassword());

        if (wsRegistryServiceClient.resourceExists("/_system/governance/trunk/services/com/amazon/soap" +
                                                   "/AmazonSearchService")) {
            resourceAdminServiceClient.deleteResource("/_system/governance/trunk/services/com/amazon/soap/" +
                                                      "AmazonSearchService");
        }

        PropertiesAdminServiceClient propertiesAdminServiceClient =
                new PropertiesAdminServiceClient(environment.getGreg().getProductVariables()
                                                         .getBackendUrl(), userInfo.getUserName(), userInfo.getPassword());
        propertiesAdminServiceClient.removeProperty("/", "TestProperty");
        lifeCycleManagementClient.deleteLifeCycle("StateDemoteLC");
        resourceAdminServiceClient = null;
        environment = null;
        userInfo = null;
    }

}
