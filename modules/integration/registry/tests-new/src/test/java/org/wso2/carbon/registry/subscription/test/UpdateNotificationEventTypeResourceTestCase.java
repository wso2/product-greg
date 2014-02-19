/*
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
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
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.governance.HumanTaskAdminClient;
import org.wso2.carbon.automation.api.clients.governance.WorkItem;
import org.wso2.carbon.automation.api.clients.registry.InfoServiceAdminClient;
import org.wso2.carbon.automation.api.clients.registry.RelationAdminServiceClient;
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
import org.wso2.carbon.registry.info.stub.beans.utils.xsd.Comment;
import org.wso2.carbon.registry.info.stub.beans.xsd.CommentBean;
import org.wso2.carbon.registry.info.stub.beans.xsd.SubscriptionBean;
import org.wso2.carbon.registry.relations.stub.AddAssociationRegistryExceptionException;
import org.wso2.carbon.registry.relations.stub.beans.xsd.AssociationBean;
import org.wso2.carbon.registry.relations.stub.beans.xsd.AssociationTreeBean;
import org.wso2.carbon.registry.relations.stub.beans.xsd.DependenciesBean;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.subscription.test.util.WorkItemClient;

public class UpdateNotificationEventTypeResourceTestCase {

    private ManageEnvironment environment;
    private final int userID = ProductConstant.ADMIN_USER_ID;
    private UserInfo userInfo;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private InfoServiceAdminClient infoServiceAdminClient;
    private RelationAdminServiceClient relationAdminServiceClient;
    private UserManagementClient userManagementClient;
    private String sessionID;

    private static final String ROOT = "/";
    private static final String RESOURCE_NAME = "application_template.jrxml";
    private static final String ASSOCIATION_RESOURCE_NAME = "TestGovernanceLC.jrxml";
    private static final String LEAF_RESOURCE_PAH = "/_system/";
    private static final String TAG = "TestUpdateTag";

    @DataProvider(name = "SubscriptionPathDataProvider")
    public Object[][] sdp() {
        return new Object[][]{new Object[]{ROOT}, new Object[]{LEAF_RESOURCE_PAH},};
    }

    @BeforeClass(alwaysRun = true)
    public void initialize() throws RemoteException, LoginAuthenticationExceptionException,
                                    RegistryException {
        userInfo = UserListCsvReader.getUserInfo(userID);
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userID);
        environment = builder.build();
        sessionID = environment.getGreg().getSessionCookie();
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(environment.getGreg().getProductVariables()
                                                       .getBackendUrl(), userInfo.getUserName(),
                                               userInfo.getPassword());
        infoServiceAdminClient =
                new InfoServiceAdminClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                           userInfo.getUserName(), userInfo.getPassword());
        relationAdminServiceClient =
                new RelationAdminServiceClient(environment.getGreg().getProductVariables()
                                                       .getBackendUrl(), userInfo.getUserName(),
                                               userInfo.getPassword());
        userManagementClient =
                new UserManagementClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                         userInfo.getUserName(), userInfo.getPassword());
    }

    /**
     * Add resources to leaf level and root level
     *
     * @param path resource path
     * @throws MalformedURLException
     * @throws RemoteException
     * @throws ResourceAdminServiceExceptionException
     *
     */
    @Test(groups = "wso2.greg", description = "Add resource", dataProvider = "SubscriptionPathDataProvider")
    public void testAddResource(String path) throws MalformedURLException, RemoteException,
                                                    ResourceAdminServiceExceptionException {
        String resourcePath =
                ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
                File.separator + "GREG" + File.separator + "reports" +
                File.separator + RESOURCE_NAME;
        DataHandler dh = new DataHandler(new URL("file:///" + resourcePath));
        resourceAdminServiceClient.addResource(path + RESOURCE_NAME, "test/plain", "testDesc", dh);
        assertTrue(resourceAdminServiceClient.getResource(path + RESOURCE_NAME)[0].getAuthorUserName()
                           .contains(userInfo.getUserNameWithoutDomain()));
    }

    /**
     * Add resource to add as dependencies and associations
     *
     * @throws MalformedURLException
     * @throws RemoteException
     * @throws ResourceAdminServiceExceptionException
     *
     */
    @Test(groups = "wso2.greg", description = "Add resource for associations and dependencies",
          dependsOnMethods = "testAddResource")
    public void testAddResourceForAssociation() throws MalformedURLException, RemoteException,
                                                       ResourceAdminServiceExceptionException {
        String resourcePath =
                ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
                File.separator + "GREG" + File.separator + "reports" +
                File.separator + ASSOCIATION_RESOURCE_NAME;
        DataHandler dh = new DataHandler(new URL("file:///" + resourcePath));
        resourceAdminServiceClient.addResource(ROOT + ASSOCIATION_RESOURCE_NAME, "test/plain",
                                               "testDesc", dh);
        assertTrue(resourceAdminServiceClient.getResource(ROOT + ASSOCIATION_RESOURCE_NAME)[0].getAuthorUserName()
                           .contains(userInfo.getUserNameWithoutDomain()));
    }

    /**
     * add role
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Add role", dependsOnMethods = "testAddResourceForAssociation")
    public void testAddRole() throws Exception {
        if(userManagementClient.roleNameExists("RoleSubscriptionTest")){
            return;
        }
        userManagementClient.addRole("RoleSubscriptionTest",
                                     new String[]{userInfo.getUserNameWithoutDomain()}, new String[]{""});
        assertTrue(userManagementClient.roleNameExists("RoleSubscriptionTest"));
    }

    /**
     * Create a subscription for resource updates and send notifications via
     * Management Console
     *
     * @param path resource path
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Get Management Console Notification",
          dependsOnMethods = "testAddRole", dataProvider = "SubscriptionPathDataProvider")
    public void testConsoleSubscription(String path) throws Exception {
        infoServiceAdminClient =
                new InfoServiceAdminClient(environment.getGreg()
                                                   .getProductVariables()
                                                   .getBackendUrl(),
                                           userInfo.getUserName(),
                                           userInfo.getPassword());
        SubscriptionBean bean =
                infoServiceAdminClient.subscribe(path + RESOURCE_NAME,
                                                 "work://RoleSubscriptionTest",
                                                 "ResourceUpdated", sessionID);
        assertTrue(bean.getSubscriptionInstances() != null);
    }

    /**
     * add comments to resources
     *
     * @param path resource path
     * @throws AddAssociationRegistryExceptionException
     *
     * @throws RemoteException
     * @throws RegistryException
     * @throws RegistryExceptionException
     */
    @Test(groups = "wso2.greg", description = "Add comment",
          dependsOnMethods = "testConsoleSubscription", dataProvider = "SubscriptionPathDataProvider")

    public void testAddCommenttoResource(String path)
            throws AddAssociationRegistryExceptionException,
                   RemoteException, RegistryException,
                   RegistryExceptionException {

        infoServiceAdminClient.addComment("This is a comment", path + RESOURCE_NAME, sessionID);
        CommentBean cBean = infoServiceAdminClient.getComments(path + RESOURCE_NAME, sessionID);
        Comment[] comments = cBean.getComments();
        boolean found = false;
        for (Comment comment : comments) {
            if ("This is a comment".equalsIgnoreCase(comment.getContent())) {
                found = true;
            }
        }
        assertTrue(found, "Comment was not found");
    }

    /**
     * add tags to resources
     *
     * @param path resource path
     * @throws RegistryException
     * @throws AxisFault
     * @throws RegistryExceptionException
     */
    @Test(groups = "wso2.greg", description = "Add tag", dependsOnMethods = "testAddCommenttoResource", dataProvider = "SubscriptionPathDataProvider")
    public void testAddTagtoResource(String path) throws RegistryException, AxisFault,
                                                         RegistryExceptionException {

        infoServiceAdminClient.addTag(TAG, path + RESOURCE_NAME, sessionID);

        String tag =
                infoServiceAdminClient.getTags(path + RESOURCE_NAME, sessionID).getTags()[0].getTagName();

        assertTrue(TAG.equalsIgnoreCase(tag), "Tags does not match");
    }

    /**
     * add ratings to resources
     *
     * @param path resource path
     * @throws RegistryException
     * @throws RegistryExceptionException
     */
    @Test(groups = "wso2.greg", description = "Add rating", dependsOnMethods = "testAddTagtoResource", dataProvider = "SubscriptionPathDataProvider")
    public void testAddRatingtoResource(String path) throws RegistryException,
                                                            RegistryExceptionException {
        infoServiceAdminClient.rateResource("1", path + RESOURCE_NAME, sessionID);

        int userRating =
                infoServiceAdminClient.getRatings(path + RESOURCE_NAME, sessionID)
                        .getUserRating();

        assertTrue(userRating == 1, "Resource rating error");
    }

    /**
     * add dependencies to resources
     *
     * @param path resource path
     * @throws RemoteException
     * @throws AddAssociationRegistryExceptionException
     *
     */
    @Test(groups = "wso2.greg", description = "Add dependency", dataProvider = "SubscriptionPathDataProvider",
          dependsOnMethods = "testAddRatingtoResource")
    public void testAddDependencytoResource(String path) throws RemoteException,
                                                                AddAssociationRegistryExceptionException {
        String dependencyType = "depends";
        String todo = "add";
        relationAdminServiceClient.addAssociation(path + RESOURCE_NAME, dependencyType,
                                                  ROOT + ASSOCIATION_RESOURCE_NAME, todo);
        DependenciesBean bean = relationAdminServiceClient.getDependencies(path + RESOURCE_NAME);
        AssociationBean[] assBeans = bean.getAssociationBeans();
        boolean success = false;
        for (AssociationBean associationBean : assBeans) {
            if (dependencyType.equals(associationBean.getAssociationType())) {
                success = true;
                success = (ROOT + ASSOCIATION_RESOURCE_NAME).equalsIgnoreCase(associationBean.getDestinationPath());
                success = success && (path + RESOURCE_NAME).equalsIgnoreCase(associationBean.getSourcePath());
                break;
            }
        }
        assertTrue(success, "dependency is not correct");
    }

    /**
     * add associations to resources
     *
     * @param path resource path
     * @throws RemoteException
     * @throws AddAssociationRegistryExceptionException
     *
     */
    @Test(groups = "wso2.greg", description = "Add association",
          dataProvider = "SubscriptionPathDataProvider", dependsOnMethods = "testAddDependencytoResource")
    public void testAddAssociationtoResource(String path) throws RemoteException,
                                                                 AddAssociationRegistryExceptionException {
        String associationType = "association";
        String todo = "add";
        relationAdminServiceClient.addAssociation(path + RESOURCE_NAME, associationType,
                                                  ROOT + ASSOCIATION_RESOURCE_NAME, todo);
        AssociationTreeBean aTreeBean =
                relationAdminServiceClient.getAssociationTree(path +
                                                              RESOURCE_NAME,
                                                              "association");
        assertTrue(aTreeBean.getAssociationTree().contains(ROOT + ASSOCIATION_RESOURCE_NAME),
                   "Association is not correct");
    }

    /**
     * Get notification for all collection updates including adding
     * comments,tags,associations,ratings and dependencies
     *
     * @param path resource path
     * @throws RemoteException
     * @throws AddAssociationRegistryExceptionException
     *
     * @throws InterruptedException
     * @throws IllegalStateFault
     * @throws IllegalAccessFault
     * @throws IllegalArgumentFault
     */
    @Test(groups = "wso2.greg", description = "Get notifications",
          dataProvider = "SubscriptionPathDataProvider", dependsOnMethods = "testAddAssociationtoResource")
    public void testGetNotifications(String path) throws RemoteException,
                                                         AddAssociationRegistryExceptionException,
                                                         InterruptedException, IllegalStateFault,
                                                         IllegalAccessFault, IllegalArgumentFault {
        HumanTaskAdminClient humanTaskAdminClient =
                new HumanTaskAdminClient(
                        environment.getGreg()
                                .getBackEndUrl(),
                        userInfo.getUserName(),
                        userInfo.getPassword());
        boolean notiTag = false, notiRate = false, notiComment = false, notiDepend = false, notiAssociation =
                false;
        Thread.sleep(2000);
        WorkItem[] workItems = WorkItemClient.getWorkItems(humanTaskAdminClient);
        for (WorkItem workItem : workItems) {
            if (workItem.getPresentationSubject()
                    .toString()
                    .contains("The tag " + TAG + " was applied on resource " + path +
                              RESOURCE_NAME)) {
                notiTag = true;
            } else if (workItem.getPresentationSubject()
                    .toString()
                    .contains("A rating of 1 was given to the resource at " + path +
                              RESOURCE_NAME)) {
                notiRate = true;
            } else if (workItem.getPresentationSubject()
                    .toString()
                    .contains("A comment was added to the resource at " + path +
                              RESOURCE_NAME + ". Comment: This is a comment")) {
                notiComment = true;
            } else if (workItem.getPresentationSubject()
                    .toString()
                    .contains("An association of type depends to the resource at /" +
                              ASSOCIATION_RESOURCE_NAME +
                              " was added to the resource at " + path +
                              RESOURCE_NAME)) {
                notiDepend = true;
            } else if (workItem.getPresentationSubject()
                    .toString()
                    .contains("An association of type association to the resource at /" +
                              ASSOCIATION_RESOURCE_NAME +
                              " was added to the resource at " + path +
                              RESOURCE_NAME)) {
                notiAssociation = true;
            }

        }
        workItems = null;
        assertTrue(notiTag && notiRate && notiComment && notiDepend && notiAssociation);
    }

    @AfterClass(alwaysRun = true)
    public void clean() throws Exception {
        resourceAdminServiceClient.deleteResource(ROOT + RESOURCE_NAME);
        resourceAdminServiceClient.deleteResource(LEAF_RESOURCE_PAH + RESOURCE_NAME);
        resourceAdminServiceClient.deleteResource(ROOT + ASSOCIATION_RESOURCE_NAME);
//        userManagementClient.deleteRole("RoleSubscriptionTest");
        resourceAdminServiceClient = null;
        userManagementClient = null;
        infoServiceAdminClient = null;
        environment = null;
    }

}
