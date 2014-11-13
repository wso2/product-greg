package org.wso2.carbon.registry.subscription.test;

import org.apache.axis2.AxisFault;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.integration.common.admin.client.UserManagementClient;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.info.stub.RegistryExceptionException;
import org.wso2.carbon.registry.info.stub.beans.xsd.SubscriptionBean;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.greg.integration.common.clients.HumanTaskAdminClient;
import org.wso2.greg.integration.common.clients.InfoServiceAdminClient;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.clients.WorkItem;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.subscription.WorkItemClient;

import java.net.MalformedURLException;
import java.rmi.RemoteException;

import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class SymlinkSubscriptionTestCase extends GREGIntegrationBaseTest {

    private String sessionID;
    private InfoServiceAdminClient infoServiceAdminClient;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private UserManagementClient userManagementClient;

    private static final String ROOT = "/";
    private static final String TAG = "symlinkTag";
    private static final String COLLECTION_NAME = "symlinkCollection";
    private static final String SYMLINK_NAME = "testSymlink";
    private String userNameWithoutDomain;

    @BeforeClass(alwaysRun = true)
    public void initialize() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        sessionID = getSessionCookie();
        resourceAdminServiceClient = new ResourceAdminServiceClient(backendURL, sessionID);
        infoServiceAdminClient = new InfoServiceAdminClient(backendURL, sessionID);
        userManagementClient = new UserManagementClient(backendURL, sessionID);

        String userName = automationContext.getContextTenant().getContextUser().getUserName();
        if (userName.contains("@")){
            userNameWithoutDomain = userName.substring(0, userName.indexOf('@'));
        }
        else {
            userNameWithoutDomain = userName;
        }
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
    public void testSymlinkAddCollection() throws MalformedURLException, RemoteException, ResourceAdminServiceExceptionException {
        resourceAdminServiceClient.addCollection(ROOT, COLLECTION_NAME, "other", "test collection");
        String authorUserName = resourceAdminServiceClient.getResource(ROOT + COLLECTION_NAME)[0].getAuthorUserName();
        assertTrue(userNameWithoutDomain.equalsIgnoreCase(authorUserName), "collection creation failure");
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
    public void testAddSymlink() throws MalformedURLException, RemoteException, ResourceAdminServiceExceptionException {
        resourceAdminServiceClient.addSymbolicLink(ROOT, SYMLINK_NAME, ROOT + COLLECTION_NAME);
        String authorUserName = resourceAdminServiceClient.getResource(ROOT + SYMLINK_NAME)[0].getAuthorUserName();
        assertTrue(userNameWithoutDomain.equalsIgnoreCase(authorUserName), "Symlink creation failure");
    }

    /**
     * add a role
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Add role", dependsOnMethods = "testAddSymlink")
    public void testAddRole() throws Exception {
        if(userManagementClient.roleNameExists("RoleSymlinkSubscriptionTest")) {
            return;
        }
        userManagementClient.addRole("RoleSymlinkSubscriptionTest", new String[]{userNameWithoutDomain}, new String[]{""});
        assertTrue(userManagementClient.roleNameExists("RoleSymlinkSubscriptionTest"));
    }

    /**
     * create a subscription for a symlink
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Get Management Console Notification", dependsOnMethods = "testAddRole")
    public void testSymlinkConsoleSubscription() throws Exception {
        infoServiceAdminClient = new InfoServiceAdminClient(backendURL, sessionID);
        SubscriptionBean bean = infoServiceAdminClient.subscribe(ROOT + SYMLINK_NAME,
                "work://RoleSymlinkSubscriptionTest", "CollectionUpdated", sessionID);
        assertTrue(bean.getSubscriptionInstances() != null);
    }

    /**
     * get notifications of symlink updates
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Get notifications",
            dependsOnMethods = "testSymlinkConsoleSubscription", enabled = false)
    public void testGetNotifications() throws Exception {
        this.restartServer();
        this.addTag();
        HumanTaskAdminClient humanTaskAdminClient = new HumanTaskAdminClient(backendURL, sessionID);
        boolean notiTag = false;
        Thread.sleep(2000);
        //get all the notifications
        WorkItem[] workItems = WorkItemClient.getWorkItems(humanTaskAdminClient);
        for(WorkItem workItem : workItems) {
            if(workItem.getPresentationSubject().toString().contains("The tag " + TAG + " was applied on resource " + ROOT +
                    COLLECTION_NAME)) {
                notiTag = true;
                break;
            }
        }
        assertTrue(notiTag);
    }

    /**
     * delete subscription of the symlink
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Unsubscribe Management Console Notification",
            dependsOnMethods = "testGetNotifications", enabled = false)
    public void testConsoleUnsubscription() throws Exception {
        infoServiceAdminClient = new InfoServiceAdminClient(backendURL,sessionID);
        SubscriptionBean sBean = infoServiceAdminClient.getSubscriptions(ROOT + SYMLINK_NAME, sessionID);
        infoServiceAdminClient.unsubscribe(ROOT + SYMLINK_NAME, sBean.getSubscriptionInstances()[0].getId(), sessionID);
        sBean = infoServiceAdminClient.getSubscriptions(ROOT + SYMLINK_NAME, sessionID);
        assertNull(sBean.getSubscriptionInstances(), "Error removing subscriptions");
    }

    @AfterClass(alwaysRun = true)
    public void clean() throws Exception {
        resourceAdminServiceClient = new ResourceAdminServiceClient(backendURL, sessionID);
        resourceAdminServiceClient.deleteResource(ROOT + SYMLINK_NAME);
        resourceAdminServiceClient.deleteResource(ROOT + COLLECTION_NAME);
    }

    /**
     * restart server
     *
     * @throws Exception
     */
    private void restartServer() throws Exception {
        ServerConfigurationManager serverConfigurationManager = new ServerConfigurationManager
                (automationContext);
        serverConfigurationManager.restartGracefully();
    }

    /**
     * add a tag to symlink
     *
     * @throws RegistryException
     * @throws AxisFault
     * @throws RegistryExceptionException
     */
    private void addTag() throws Exception {
        sessionID = getSessionCookie();
        infoServiceAdminClient = new InfoServiceAdminClient(backendURL, sessionID);
        infoServiceAdminClient.addTag(TAG, ROOT + SYMLINK_NAME, sessionID);
    }
}
