package org.wso2.carbon.registry.subscription.test;

import static org.testng.Assert.assertTrue;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import javax.activation.DataHandler;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.registry.InfoServiceAdminClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.info.stub.RegistryExceptionException;
import org.wso2.carbon.registry.info.stub.beans.xsd.SubscriptionBean;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.subscription.test.util.ManagementConsoleSubscription;

/**
 * test cases fails due to - https://wso2.org/jira/browse/REGISTRY-1190
 */
public final class UpdateSubscriptionNonAdminTestCase {

    /*private ManageEnvironment environment;
    private UserInfo userInfo;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private static final String ROOT = "/";
    private static final String LEAF_LEVEL = "/_system/";
    private static final String COLLECTION_NAME = "testNonAdminCollection";
    private static final String RESOURCE_NAME = "UTPolicy.xml";

    @BeforeClass(alwaysRun = true)
    public void initialize() throws RemoteException, LoginAuthenticationExceptionException,
                                    RegistryException {

        int userID = 2;
        userInfo = UserListCsvReader.getUserInfo(userID);
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userID);
        environment = builder.build();
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(environment.getGreg().getProductVariables()
                                                       .getBackendUrl(), userInfo.getUserName(),
                                               userInfo.getPassword());
    }

    *//**
     * add subscription for root as a non admin user and get notifications for
     * updates
     *
     * @throws Exception
     *//*
    @Test(groups = "wso2.greg", description = "Get Management Console Notification")
    public void testNonadminConsoleSubscriptionRoot() throws Exception {
        assertTrue(ManagementConsoleSubscription.init(ROOT, "CollectionUpdated", environment,
                                                      userInfo));
    }

    *//**
     * add collection to leaf level
     *
     * @throws MalformedURLException
     * @throws RemoteException
     * @throws ResourceAdminServiceExceptionException
     *
     *//*
    @Test(groups = "wso2.greg", description = "Add collection to root level",
          dependsOnMethods = "testNonadminConsoleSubscriptionRoot")
    public void testAddLeafCollection() throws MalformedURLException, RemoteException,
                                               ResourceAdminServiceExceptionException {
        resourceAdminServiceClient.addCollection(ROOT, COLLECTION_NAME, "other", "test collection");
        String authorUserName =
                resourceAdminServiceClient.getResource(ROOT + COLLECTION_NAME)[0].getAuthorUserName();
        assertTrue(userInfo.getUserNameWithoutDomain().equalsIgnoreCase(authorUserName),
                   "Root collection creation failure");
    }

    *//**
     * add subscription for leaf level collection as a non admin user and get
     * notifications for
     * updates
     *
     * @throws Exception
     *//*
    @Test(groups = "wso2.greg", description = "Get Management Console Notification",
          dependsOnMethods = "testAddLeafCollection")
    public void testNonadminConsoleSubscriptionLeafLevel() throws Exception {
        assertTrue(ManagementConsoleSubscription.init(ROOT + COLLECTION_NAME, "CollectionUpdated",
                                                      environment, userInfo));
    }

    *//**
     * add subscription for root level collection as a non admin user and get
     * notifications for
     * updates
     *
     * @throws Exception
     *//*
    @Test(groups = "wso2.greg", description = "Get Management Console Notification",
          dependsOnMethods = "testNonadminConsoleSubscriptionLeafLevel")
    public void testNonadminConsoleSubscriptionRootLevel() throws Exception {
        assertTrue(ManagementConsoleSubscription.init("/_system", "CollectionUpdated", environment,
                                                      userInfo));
    }

    *//**
     * add resource to root level
     *
     * @throws MalformedURLException
     * @throws RemoteException
     * @throws ResourceAdminServiceExceptionException
     *
     *//*
    @Test(groups = "wso2.greg", description = "Add resource", dependsOnMethods = "testNonadminConsoleSubscriptionRootLevel")
    public void testAddResourceRoot() throws MalformedURLException, RemoteException,
                                             ResourceAdminServiceExceptionException {
        String resourcePath =
                ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
                File.separator + "GREG" + File.separator + "policy" +
                File.separator + RESOURCE_NAME;
        DataHandler dh = new DataHandler(new URL("file:///" + resourcePath));
        resourceAdminServiceClient.addResource(ROOT + RESOURCE_NAME, "test/plain", "testDesc", dh);
        resourceAdminServiceClient.addResource(LEAF_LEVEL + RESOURCE_NAME, "test/plain",
                                               "testDesc", dh);
        assertTrue(resourceAdminServiceClient.getResource(ROOT + RESOURCE_NAME)[0].getAuthorUserName()
                           .contains(userInfo.getUserNameWithoutDomain()));
        assertTrue(resourceAdminServiceClient.getResource(LEAF_LEVEL + RESOURCE_NAME)[0].getAuthorUserName()
                           .contains(userInfo.getUserNameWithoutDomain()));

    }

    *//**
     * add subscription for leaf level resource as a non admin user and get
     * notifications for
     * updates
     *
     * @throws Exception
     *//*
    @Test(groups = "wso2.greg", description = "Get Management Console Notification",
          dependsOnMethods = "testAddResourceRoot")
    public void testNonAdminConsoleSubscriptionLeafLevelResource() throws Exception {
        assertTrue(ManagementConsoleSubscription.init(LEAF_LEVEL + RESOURCE_NAME,
                                                      "ResourceUpdated", environment, userInfo));
    }

    *//**
     * add subscription for root level resource as a non admin user and get
     * notifications for
     * updates
     *
     * @throws Exception
     *//*
    @Test(groups = "wso2.greg", description = "Get Management Console Notification",
          dependsOnMethods = "testNonAdminConsoleSubscriptionLeafLevelResource")
    public void testNonAdminConsoleSubscriptionRootLevelResource() throws Exception {
        assertTrue(ManagementConsoleSubscription.init(ROOT + RESOURCE_NAME, "ResourceUpdated",
                                                      environment, userInfo));
    }

    @AfterClass(alwaysRun = true)
    public void clean() throws RemoteException, ResourceAdminServiceExceptionException,
                               RegistryException, RegistryExceptionException {
        resourceAdminServiceClient.deleteResource(ROOT + COLLECTION_NAME);
        resourceAdminServiceClient.deleteResource(ROOT + RESOURCE_NAME);
        resourceAdminServiceClient.deleteResource(LEAF_LEVEL + RESOURCE_NAME);
        InfoServiceAdminClient infoServiceAdminClient =
                new InfoServiceAdminClient(environment.getGreg().getProductVariables()
                                                   .getBackendUrl(), userInfo.getUserName(),
                                           userInfo.getPassword());

        String sessionID = environment.getGreg().getSessionCookie();

        SubscriptionBean sBean = infoServiceAdminClient.getSubscriptions(ROOT, sessionID);
        infoServiceAdminClient.unsubscribe(ROOT, sBean.getSubscriptionInstances()[0].getId(),
                                           sessionID);
        sBean = infoServiceAdminClient.getSubscriptions("/_system", sessionID);
        infoServiceAdminClient.unsubscribe("/_system", sBean.getSubscriptionInstances()[0].getId(),
                                           sessionID);
        resourceAdminServiceClient = null;
        infoServiceAdminClient = null;
        environment = null;
    }*/
}
