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

import org.apache.axis2.AxisFault;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault;
import org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault;
import org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault;
import org.wso2.carbon.integration.common.admin.client.UserManagementClient;
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
import org.wso2.greg.integration.common.clients.*;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.subscription.WorkItemClient;

import javax.activation.DataHandler;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;

public class UpdateNotificationEventTypeCollectionTestCase extends GREGIntegrationBaseTest{

    private ResourceAdminServiceClient resourceAdminServiceClient;
    private InfoServiceAdminClient infoServiceAdminClient;
    private RelationAdminServiceClient relationAdminServiceClient;
    private UserManagementClient userManagementClient;
    private String sessionID;

    private static final String ROOT = "/";
    private static final String RESOURCE_NAME = "test.css";
    private static final String LEAF_COLLECTION_PAH = "/_system/";
    private static final String COLLECTION_NAME = "testCollectionUpdate";
    private static final String TAG = "TestUpdateTag";
    private String userNameWithoutDomain;

    @DataProvider(name = "SubscriptionPathDataProvider")
    public Object[][] sdp() {
        return new Object[][]{new Object[]{ROOT}, new Object[]{LEAF_COLLECTION_PAH + COLLECTION_NAME},
                new Object[]{ROOT + COLLECTION_NAME}};
    }

    @BeforeClass(alwaysRun = true)
    public void initialize() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        sessionID = getSessionCookie();
        resourceAdminServiceClient = new ResourceAdminServiceClient(backendURL, sessionID);
        infoServiceAdminClient = new InfoServiceAdminClient(backendURL, sessionID);
        relationAdminServiceClient = new RelationAdminServiceClient(backendURL, sessionID);
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
     * Add a resource to collection
     *
     * @throws MalformedURLException
     * @throws RemoteException
     * @throws ResourceAdminServiceExceptionException
     *
     */
    @Test(groups = "wso2.greg", description = "Add resource")
    public void testAddResource() throws MalformedURLException, RemoteException, ResourceAdminServiceExceptionException {
        String resourcePath = getTestArtifactLocation() + "artifacts" +
                File.separator + "GREG" + File.separator + RESOURCE_NAME;
        DataHandler dh = new DataHandler(new URL("file:///" + resourcePath));
        resourceAdminServiceClient.addResource(ROOT + RESOURCE_NAME, "test/plain", "testDesc", dh);
        assertTrue(resourceAdminServiceClient.getResource(ROOT + RESOURCE_NAME)[0].
                getAuthorUserName().contains(userNameWithoutDomain));
    }

    /**
     * Add root level collection
     *
     * @throws MalformedURLException
     * @throws RemoteException
     * @throws ResourceAdminServiceExceptionException
     *
     */
    @Test(groups = "wso2.greg", description = "Add collection to root level", dependsOnMethods = "testAddResource")
    public void testAddRootCollection() throws MalformedURLException, RemoteException, ResourceAdminServiceExceptionException {
        resourceAdminServiceClient.addCollection(ROOT, COLLECTION_NAME, "other", "test collection");
        String authorUserName = resourceAdminServiceClient.getResource(ROOT + COLLECTION_NAME)[0].getAuthorUserName();
        assertTrue(userNameWithoutDomain.equalsIgnoreCase(authorUserName), "Root collection creation failure");
    }

    /**
     * Add leaf level collection
     *
     * @throws MalformedURLException
     * @throws RemoteException
     * @throws ResourceAdminServiceExceptionException
     *
     */
    @Test(groups = "wso2.greg", description = "Add collection to leaf level", dependsOnMethods = "testAddRootCollection")
    public void testAddLeafCollection() throws MalformedURLException, RemoteException, ResourceAdminServiceExceptionException {
        resourceAdminServiceClient.addCollection(LEAF_COLLECTION_PAH, COLLECTION_NAME, "other", "test collection");
        String authorUserName = resourceAdminServiceClient.getResource(LEAF_COLLECTION_PAH + COLLECTION_NAME)[0].getAuthorUserName();
        assertTrue(userNameWithoutDomain.equalsIgnoreCase(authorUserName), "Leaf level collection creation failure");
    }

    /**
     * add role
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Add role", dependsOnMethods = "testAddLeafCollection")
    public void testAddRole() throws Exception {
        if(userManagementClient.roleNameExists("RoleSubscriptionTest")) {
            return;
        }
        userManagementClient.addRole("RoleSubscriptionTest", new String[]{userNameWithoutDomain}, new String[]{""});
        assertTrue(userManagementClient.roleNameExists("RoleSubscriptionTest"));
    }

    /**
     * Create a subscription for collections and send notifications via
     * Management
     * Console
     *
     * @param path path of the collection
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Get Management Console Notification",
            dependsOnMethods = {"testAddRole"}, dataProvider = "SubscriptionPathDataProvider")
    public void testConsoleSubscription(String path) throws Exception {
        infoServiceAdminClient = new InfoServiceAdminClient(backendURL, sessionID);
        SubscriptionBean bean = infoServiceAdminClient.subscribe(path,
                "work://RoleSubscriptionTest", "CollectionUpdated", sessionID);
        assertTrue(bean.getSubscriptionInstances() != null);
    }

    /**
     * Add a comment to a collection
     *
     * @param path collection path
     * @throws AddAssociationRegistryExceptionException
     *
     * @throws RemoteException
     * @throws RegistryException
     * @throws RegistryExceptionException
     */
    @Test(groups = "wso2.greg", description = "Add comment",
            dependsOnMethods = "testConsoleSubscription", dataProvider = "SubscriptionPathDataProvider")
    public void testAddComment(String path) throws Exception {
        infoServiceAdminClient.addComment("This is a comment", path, sessionID);
        CommentBean cBean = infoServiceAdminClient.getComments(path, sessionID);
        Comment[] comments = cBean.getComments();
        boolean found = false;
        for(Comment comment : comments) {
            if("This is a comment".equalsIgnoreCase(comment.getContent())) {
                found = true;
            }
        }
        assertTrue(found, "Comment was not found");
    }

    /**
     * Add a tag to a collection
     *
     * @param path collection path
     * @throws RegistryException
     * @throws AxisFault
     * @throws RegistryExceptionException
     */
    @Test(groups = "wso2.greg", description = "Add tag",
            dependsOnMethods = "testAddComment", dataProvider = "SubscriptionPathDataProvider")
    public void testAddTag(String path) throws RegistryException, AxisFault, RegistryExceptionException {
        infoServiceAdminClient.addTag(TAG, path, sessionID);
        String tag = infoServiceAdminClient.getTags(path, sessionID).getTags()[0].getTagName();
        assertTrue(TAG.equalsIgnoreCase(tag), "Tags does not match");
    }

    /**
     * Add ratings to a collection
     *
     * @param path collection path
     * @throws RegistryException
     * @throws RegistryExceptionException
     */
    @Test(groups = "wso2.greg", description = "Add rating", dependsOnMethods = "testAddTag",
            dataProvider = "SubscriptionPathDataProvider")
    public void testAddRating(String path) throws RegistryException, RegistryExceptionException {
        infoServiceAdminClient.rateResource("1", path, sessionID);
        int userRating = infoServiceAdminClient.getRatings(path, sessionID).getUserRating();
        assertTrue(userRating == 1, "Resource rating error");
    }

    /**
     * Add dependency to a collection
     *
     * @param path collection path
     * @throws RemoteException
     * @throws AddAssociationRegistryExceptionException
     *
     */
    @Test(groups = "wso2.greg", description = "Add dependency", dataProvider = "SubscriptionPathDataProvider",
            dependsOnMethods = "testAddRating")
    public void testAddDependency(String path) throws RemoteException, AddAssociationRegistryExceptionException {
        String dependencyType = "depends";
        String todo = "add";
        relationAdminServiceClient.addAssociation(path, dependencyType, ROOT + RESOURCE_NAME, todo);
        DependenciesBean bean = relationAdminServiceClient.getDependencies(path);
        AssociationBean[] assBeans = bean.getAssociationBeans();
        boolean success = false;
        for(AssociationBean associationBean : assBeans) {
            if(dependencyType.equals(associationBean.getAssociationType())) {
                success = true;
                success = success && (ROOT + RESOURCE_NAME).equalsIgnoreCase(associationBean.getDestinationPath());
                success = success && (path).equalsIgnoreCase(associationBean.getSourcePath());
                break;
            }
        }
        assertTrue(success, "dependency is not correct");
    }

    /**
     * Add association to a collection
     *
     * @param path collection path
     * @throws RemoteException
     * @throws AddAssociationRegistryExceptionException
     *
     */
    @Test(groups = "wso2.greg", description = "Add association",
            dataProvider = "SubscriptionPathDataProvider", dependsOnMethods = "testAddDependency")
    public void testAddAssociation(String path) throws RemoteException, AddAssociationRegistryExceptionException {
        String associationType = "association";
        String todo = "add";
        relationAdminServiceClient.addAssociation(path, associationType, ROOT + RESOURCE_NAME, todo);
        AssociationTreeBean aTreeBean = relationAdminServiceClient.getAssociationTree(path, "association");
        assertTrue(aTreeBean.getAssociationTree().contains(ROOT + RESOURCE_NAME), "Association is not correct");
    }

    /**
     * Get notification for all collection updates including adding
     * comments,tags,associations,ratings and dependencies
     *
     * @throws RemoteException
     * @throws AddAssociationRegistryExceptionException
     *
     * @throws InterruptedException
     * @throws IllegalStateFault
     * @throws IllegalAccessFault
     * @throws IllegalArgumentFault
     */
    @Test(groups = "wso2.greg", description = "Get notifications",
            dataProvider = "SubscriptionPathDataProvider", dependsOnMethods = "testAddAssociation")
    public void testGetNotifications(String path) throws RemoteException, AddAssociationRegistryExceptionException,
            InterruptedException, IllegalStateFault, IllegalAccessFault, IllegalArgumentFault {
        HumanTaskAdminClient humanTaskAdminClient = new HumanTaskAdminClient(backendURL, sessionID);
        boolean notiTag = false, notiRate = false, notiComment = false, notiDepend = false, notiAssociation = false;
        Thread.sleep(1000);
        WorkItem[] workItems = WorkItemClient.getWorkItems(humanTaskAdminClient);
        for(WorkItem workItem : workItems) {
            if(workItem.getPresentationSubject().toString().contains("The tag " + TAG + " was applied on resource " + path)) {
                notiTag = true;
            } else if(workItem.getPresentationSubject().toString().contains("A rating of 1 was given to the collection at " + path)) {
                notiRate = true;
            } else if(workItem.getPresentationSubject().toString().contains("A comment was added to the collection at " + path +
                    ". Comment: This is a comment")) {
                notiComment = true;
            } else if(workItem.getPresentationSubject().toString().contains("An association of type depends to the resource at /" +
                    RESOURCE_NAME + " was added to the resource at " +
                    path)) {
                notiDepend = true;
            } else if(workItem.getPresentationSubject().toString().contains("An association of type association to the resource at /" +
                    RESOURCE_NAME + " was added to the resource at " +
                    path)) {
                notiAssociation = true;
            }
        }
        workItems = null;
        assertTrue(notiTag && notiRate && notiComment && notiDepend && notiAssociation);
    }

    @AfterClass(alwaysRun = true)
    public void clean() throws Exception {
        resourceAdminServiceClient.deleteResource(ROOT + COLLECTION_NAME);
        resourceAdminServiceClient.deleteResource(LEAF_COLLECTION_PAH + COLLECTION_NAME);
        resourceAdminServiceClient.deleteResource(ROOT + RESOURCE_NAME);
        SubscriptionBean sBean = infoServiceAdminClient.getSubscriptions(ROOT, sessionID);
        infoServiceAdminClient.unsubscribe(ROOT, sBean.getSubscriptionInstances()[0].getId(), sessionID);
        infoServiceAdminClient.rateResource("0", ROOT, sessionID);
        infoServiceAdminClient.removeComment("/;comments:1", sessionID);
        infoServiceAdminClient.removeTag(TAG, ROOT, sessionID);
    }
}
