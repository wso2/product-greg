package org.wso2.carbon.registry.subscription.test;

import static org.testng.Assert.assertTrue;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import javax.activation.DataHandler;

import org.apache.axis2.AxisFault;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.governance.HumanTaskAdminClient;
import org.wso2.carbon.automation.api.clients.governance.WorkItem;
import org.wso2.carbon.automation.api.clients.registry.InfoServiceAdminClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.api.clients.user.mgt.UserManagementClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault;
import org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault;
import org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.info.stub.RegistryExceptionException;
import org.wso2.carbon.registry.info.stub.beans.xsd.SubscriptionBean;
import org.wso2.carbon.registry.relations.stub.AddAssociationRegistryExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.subscription.test.util.WorkItemClient;

/**
 * Add subscription to a child and check whether notifications are received when
 * parent collection is changed
 */
public class ChildParentNotificationTestCase {

    private ManageEnvironment environment;
    private final int userID = ProductConstant.ADMIN_USER_ID;
    private UserInfo userInfo;
    private String sessionID;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private UserManagementClient userManagementClient;
    private InfoServiceAdminClient infoServiceAdminClient;

    private static final String RESOURCE_NAME = "rules_new.rxt";
    private static final String ROOT = "/";
    private static final String TAG = "tagForParent";
    private static final String ROLE_NAME = "ChildParentSubscriptionTest";

    @BeforeClass(alwaysRun = true)
    public void initialize() throws RemoteException, LoginAuthenticationExceptionException,
                                    RegistryException {
        userInfo = UserListCsvReader.getUserInfo(userID);
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userID);
        environment = builder.build();
        sessionID = environment.getGreg().getSessionCookie();
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(
                        environment.getGreg()
                                .getProductVariables()
                                .getBackendUrl(),
                        userInfo.getUserName(),
                        userInfo.getPassword());
        userManagementClient =
                new UserManagementClient(environment.getGreg().getProductVariables()
                                                 .getBackendUrl(),
                                         userInfo.getUserName(),
                                         userInfo.getPassword());
        infoServiceAdminClient =
                new InfoServiceAdminClient(environment.getGreg()
                                                   .getProductVariables()
                                                   .getBackendUrl(),
                                           userInfo.getUserName(),
                                           userInfo.getPassword());
    }

    /**
     * add a child to a collection
     *
     * @throws MalformedURLException
     * @throws RemoteException
     * @throws ResourceAdminServiceExceptionException
     *
     */
    @Test(groups = "wso2.greg", description = "Add resource")
    public void testAddResource() throws MalformedURLException, RemoteException,
                                         ResourceAdminServiceExceptionException {
        String resourcePath =
                ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
                File.separator + "GREG" + File.separator + "rxt" +
                File.separator + RESOURCE_NAME;
        DataHandler dh = new DataHandler(new URL("file:///" + resourcePath));
        resourceAdminServiceClient.addResource(ROOT + RESOURCE_NAME, "test/plain", "testDesc", dh);
        assertTrue(resourceAdminServiceClient.getResource(ROOT + RESOURCE_NAME)[0].getAuthorUserName()
                           .contains(userInfo.getUserNameWithoutDomain()));
    }

    /**
     * add a role
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Add role", dependsOnMethods = "testAddResource")
    public void testAddRole() throws Exception {
        userManagementClient.addRole(ROLE_NAME, new String[]{userInfo.getUserNameWithoutDomain()},
                                     new String[]{""});
        assertTrue(userManagementClient.roleNameExists(ROLE_NAME));
    }

    /**
     * create a subscription for a child
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Get Management Console Notification", dependsOnMethods = {
            "testAddRole"})
    public void testChildConsoleSubscription() throws Exception {
        infoServiceAdminClient =
                new InfoServiceAdminClient(environment.getGreg()
                                                   .getProductVariables()
                                                   .getBackendUrl(),
                                           userInfo.getUserName(),
                                           userInfo.getPassword());
        SubscriptionBean bean =
                infoServiceAdminClient.subscribe(ROOT + RESOURCE_NAME, "work://" +
                                                                       ROLE_NAME,
                                                 "ResourceUpdated", sessionID);
        assertTrue(bean.getSubscriptionInstances() != null);
    }

    /**
     * check for notifications when parent is updated
     *
     * @throws RemoteException
     * @throws AddAssociationRegistryExceptionException
     *
     * @throws InterruptedException
     * @throws IllegalStateFault
     * @throws IllegalAccessFault
     * @throws IllegalArgumentFault

     * @throws RegistryException
     * @throws RegistryExceptionException
     */
    @Test(groups = "wso2.greg", description = "Get notifications", dependsOnMethods = "testChildConsoleSubscription")
    public void testGetNotifications() throws RemoteException,
                                              AddAssociationRegistryExceptionException,
                                              InterruptedException, IllegalStateFault,
                                              IllegalAccessFault,
                                              IllegalArgumentFault,
                                              RegistryException, RegistryExceptionException {
        addTag();
        HumanTaskAdminClient humanTaskAdminClient =
                new HumanTaskAdminClient(
                        environment.getGreg()
                                .getBackEndUrl(),
                        userInfo.getUserName(),
                        userInfo.getPassword());
        boolean notiTag = false;
        WorkItem[] workItems = WorkItemClient.getWorkItems(humanTaskAdminClient);
        for (WorkItem workItem : workItems) {
            if (workItem.getPresentationSubject().toString()
                    .contains("The tag " + TAG + " was applied on resource " + ROOT)) {
                notiTag = true;
                break;
            }

        }
        workItems = null;
        assertTrue(!notiTag);
    }

    @AfterClass(alwaysRun = true)
    public void clean() throws Exception {
        resourceAdminServiceClient.deleteResource(ROOT + RESOURCE_NAME);
//        userManagementClient.deleteRole(ROLE_NAME);
        infoServiceAdminClient.removeTag(TAG, ROOT, sessionID);

        resourceAdminServiceClient = null;
        userManagementClient = null;
        infoServiceAdminClient = null;
        environment = null;
    }

    /**
     * add a tag to collection
     *
     * @throws AxisFault
     * @throws RegistryException
     */
    private void addTag() throws AxisFault, RegistryException {
        infoServiceAdminClient.addTag(TAG, ROOT, sessionID);

    }
}
