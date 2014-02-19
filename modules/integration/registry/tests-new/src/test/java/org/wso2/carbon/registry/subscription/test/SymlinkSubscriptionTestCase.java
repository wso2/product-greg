package org.wso2.carbon.registry.subscription.test;

import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.net.MalformedURLException;
import java.rmi.RemoteException;

import org.apache.axis2.AxisFault;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.governance.HumanTaskAdminClient;
import org.wso2.carbon.automation.api.clients.governance.WorkItem;
import org.wso2.carbon.automation.api.clients.registry.InfoServiceAdminClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.api.clients.server.admin.ServerAdminClient;
import org.wso2.carbon.automation.api.clients.user.mgt.UserManagementClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.ServerGroupManager;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkFactory;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkProperties;
import org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault;
import org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault;
import org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.info.stub.RegistryExceptionException;
import org.wso2.carbon.registry.info.stub.beans.xsd.SubscriptionBean;
import org.wso2.carbon.registry.relations.stub.AddAssociationRegistryExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.subscription.test.util.WorkItemClient;

public class SymlinkSubscriptionTestCase {
    private ManageEnvironment environment;
    private UserInfo userInfo;
    private String sessionID;
    private InfoServiceAdminClient infoServiceAdminClient;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private UserManagementClient userManagementClient;

    private static final String ROOT = "/";
    private static final String TAG = "symlinkTag";
    private static final String COLLECTION_NAME = "symlinkCollection";
    private static final String SYMLINK_NAME = "testSymlink";

    @BeforeClass(alwaysRun = true)
    public void initialize() throws RemoteException, LoginAuthenticationExceptionException,
                                    RegistryException {
        int userID = ProductConstant.ADMIN_USER_ID;
        userInfo = UserListCsvReader.getUserInfo(userID);
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userID);
        environment = builder.build();
        sessionID = environment.getGreg().getSessionCookie();
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                               userInfo.getUserName(),
                                               userInfo.getPassword());
        infoServiceAdminClient =
                new InfoServiceAdminClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                           userInfo.getUserName(),
                                           userInfo.getPassword());
        userManagementClient =
                new UserManagementClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                         userInfo.getUserName(),
                                         userInfo.getPassword());

    }

    /**
     * add a collection to create a symlink to the collection
     *
     * @throws MalformedURLException
     * @throws RemoteException
     * @throws ResourceAdminServiceExceptionException
     *
     */
    @Test(groups = "wso2.greg", description = "Add collection")
    public void testSymlinkAddCollection() throws MalformedURLException, RemoteException,
                                                  ResourceAdminServiceExceptionException {
        resourceAdminServiceClient.addCollection(ROOT, COLLECTION_NAME, "other", "test collection");
        String authorUserName =
                resourceAdminServiceClient.getResource(ROOT + COLLECTION_NAME)[0].getAuthorUserName();
        assertTrue(userInfo.getUserNameWithoutDomain().equalsIgnoreCase(authorUserName),
                   "collection creation failure");
    }

    /**
     * add a symlink in root for the added collection
     *
     * @throws MalformedURLException
     * @throws RemoteException
     * @throws ResourceAdminServiceExceptionException
     *
     */
    @Test(groups = "wso2.greg", description = "Add symlink at root for a collection",
          dependsOnMethods = "testSymlinkAddCollection")
    public void testAddSymlink() throws MalformedURLException, RemoteException,
                                        ResourceAdminServiceExceptionException {
        resourceAdminServiceClient.addSymbolicLink(ROOT, SYMLINK_NAME, ROOT + COLLECTION_NAME);

        String authorUserName =
                resourceAdminServiceClient.getResource(ROOT + SYMLINK_NAME)[0].getAuthorUserName();
        assertTrue(userInfo.getUserNameWithoutDomain().equalsIgnoreCase(authorUserName),
                   "Symlink creation failure");
    }

    /**
     * add a role
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Add role", dependsOnMethods = "testAddSymlink")
    public void testAddRole() throws Exception {
        if(userManagementClient.roleNameExists("RoleSymlinkSubscriptionTest")){
            return;
        }
        userManagementClient.addRole("RoleSymlinkSubscriptionTest",
                                     new String[]{userInfo.getUserNameWithoutDomain()}, new String[]{""});
        assertTrue(userManagementClient.roleNameExists("RoleSymlinkSubscriptionTest"));
    }

    /**
     * create a subscription for a symlink
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Get Management Console Notification", dependsOnMethods =
            "testAddRole")
    public void testSymlinkConsoleSubscription() throws Exception {
        infoServiceAdminClient =
                new InfoServiceAdminClient(environment.getGreg()
                                                   .getProductVariables()
                                                   .getBackendUrl(),
                                           userInfo.getUserName(),
                                           userInfo.getPassword());
        SubscriptionBean bean =
                infoServiceAdminClient.subscribe(ROOT + SYMLINK_NAME,
                                                 "work://RoleSymlinkSubscriptionTest",
                                                 "CollectionUpdated", sessionID);
        assertTrue(bean.getSubscriptionInstances() != null);
    }

    /**
     * get notifications of symlink updates
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Get notifications", dependsOnMethods = "testSymlinkConsoleSubscription")
    public void testGetNotifications() throws Exception {

        this.restartServer();
        this.addTag();
        HumanTaskAdminClient humanTaskAdminClient =
                new HumanTaskAdminClient(
                        environment.getGreg()
                                .getBackEndUrl(),
                        userInfo.getUserName(),
                        userInfo.getPassword());
        boolean notiTag = false;
        Thread.sleep(2000);
        //get all the notifications
        WorkItem[] workItems = WorkItemClient.getWorkItems(humanTaskAdminClient);
        for (WorkItem workItem : workItems) {
            if (workItem.getPresentationSubject()
                    .toString()
                    .contains("The tag " + TAG + " was applied on resource " + ROOT +
                              COLLECTION_NAME)) {
                notiTag = true;
                break;
            }

        }
        workItems = null;
        assertTrue(notiTag);
    }

    /**
     * delete subscription of the symlink
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Unsubscribe Management Console Notification",
          dependsOnMethods = "testGetNotifications")
    public void testConsoleUnsubscription() throws Exception {
        infoServiceAdminClient =
                new InfoServiceAdminClient(environment.getGreg()
                                                   .getProductVariables()
                                                   .getBackendUrl(),
                                           userInfo.getUserName(),
                                           userInfo.getPassword());
        SubscriptionBean sBean =
                infoServiceAdminClient.getSubscriptions(ROOT + SYMLINK_NAME,
                                                        sessionID);
        infoServiceAdminClient.unsubscribe(ROOT + SYMLINK_NAME,
                                           sBean.getSubscriptionInstances()[0].getId(), sessionID);
        sBean = infoServiceAdminClient.getSubscriptions(ROOT + SYMLINK_NAME, sessionID);
        assertNull(sBean.getSubscriptionInstances(), "Error removing subscriptions");
    }

    @AfterClass(alwaysRun = true)
    public void clean() throws Exception {
        resourceAdminServiceClient.deleteResource(ROOT + SYMLINK_NAME);
        resourceAdminServiceClient.deleteResource(ROOT + COLLECTION_NAME);
//        userManagementClient.deleteRole("RoleSymlinkSubscriptionTest");
        resourceAdminServiceClient = null;
        userManagementClient = null;
        infoServiceAdminClient = null;
        environment = null;
    }

    /**
     * restart server
     *
     * @throws Exception
     */
    private void restartServer() throws Exception {
        ServerAdminClient serverAdminClient =
                new ServerAdminClient(environment.getGreg().getProductVariables()
                                              .getBackendUrl(), userInfo.getUserName(), userInfo.getPassword());
        FrameworkProperties frameworkProperties =
                FrameworkFactory.getFrameworkProperties(ProductConstant.GREG_SERVER_NAME);
        ServerGroupManager.getServerUtils().restartGracefully(serverAdminClient, frameworkProperties);

    }

    /**
     * add a tag to symlink
     *
     * @throws RegistryException
     * @throws AxisFault
     * @throws RegistryExceptionException
     */
    private void addTag() throws RegistryException, AxisFault, RegistryExceptionException {
        infoServiceAdminClient.addTag(TAG, ROOT + SYMLINK_NAME, sessionID);


    }
}
