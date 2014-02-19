/*
* Copyright 2004,2005 The Apache Software Foundation.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.wso2.carbon.registry.resource.test;

import org.apache.axis2.AxisFault;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.governance.LifeCycleManagementClient;
import org.wso2.carbon.automation.api.clients.registry.InfoServiceAdminClient;
import org.wso2.carbon.automation.api.clients.registry.RelationAdminServiceClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.api.clients.user.mgt.UserManagementClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.core.utils.fileutils.FileManager;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.info.stub.RegistryExceptionException;
import org.wso2.carbon.registry.info.stub.beans.utils.xsd.Comment;
import org.wso2.carbon.registry.info.stub.beans.xsd.CommentBean;
import org.wso2.carbon.registry.info.stub.beans.xsd.SubscriptionBean;
import org.wso2.carbon.registry.relations.stub.AddAssociationRegistryExceptionException;
import org.wso2.carbon.registry.relations.stub.beans.xsd.AssociationTreeBean;
import org.wso2.carbon.registry.relations.stub.beans.xsd.DependenciesBean;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import javax.activation.DataHandler;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class MoveResourceCommunityFeaturesTestCase {

    private UserInfo userInfo;
    private ResourceAdminServiceClient resourceAdminClient;
    private RelationAdminServiceClient relationServiceClient;
    private LifeCycleManagementClient lifeCycleManagementClient;
    private WSRegistryServiceClient wsRegistryServiceClient;
    private InfoServiceAdminClient infoServiceAdminClient;
    UserManagementClient userManagementClient;

    private static final String PATH = "/c4/";
    private static final String RES_NAME = "rootTestResource";
    private static final String DEPENDENCY_PATH = "/_system/config/dependencyTest";
    private static final String TAG = "TestTag";
    private static final String ASSOCIATION_PATH = "/_system/config/associationTest";
    private static final String RES_NAME_AFTER_MOVING = "MovedTestResource";
    private static final String RES_MOVED_LOCATION = "/c1/c2/";
    private static final String RES_DESC = "A test resource";
    private static final String RES_RATING = "2";
    private static final String ROLE_NAME = "RoleSubscriptionTest";
    private final String ASPECT_NAME = "IntergalacticServiceLC";
    private String sessionId;

    @BeforeClass(alwaysRun = true)
    public void initialize()
            throws LoginAuthenticationExceptionException, RemoteException, RegistryException {

        int userId = ProductConstant.ADMIN_USER_ID;
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        ManageEnvironment environment = builder.build();

        sessionId = environment.getGreg().getSessionCookie();
        userInfo = UserListCsvReader.getUserInfo(userId);

        resourceAdminClient =
                new ResourceAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                               environment.getGreg().getSessionCookie());
        relationServiceClient =
                new RelationAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                               environment.getGreg().getSessionCookie());
        lifeCycleManagementClient =
                new LifeCycleManagementClient(environment.getGreg().getBackEndUrl(),
                                              environment.getGreg().getSessionCookie());
        infoServiceAdminClient =
                new InfoServiceAdminClient(environment.getGreg().getBackEndUrl(),
                                           environment.getGreg().getSessionCookie());
        userManagementClient =
                new UserManagementClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                         environment.getGreg().getSessionCookie());
        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        wsRegistryServiceClient = registryProviderUtil.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);

    }

    @Test
    public void testAddResource()
            throws ResourceAdminServiceExceptionException, RemoteException, MalformedURLException {

        String path = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator
                      + "GREG" + File.separator + "testresource.txt";
        DataHandler dataHandler = new DataHandler(new URL("file:///" + path));

        String fileType = "plain/text";
        resourceAdminClient.addResource(PATH + RES_NAME, fileType, RES_DESC, dataHandler);

        String authorUserName = resourceAdminClient.getResource(PATH + RES_NAME)[0].getAuthorUserName();
        assertTrue(userInfo.getUserNameWithoutDomain().equalsIgnoreCase(authorUserName), "Root resource creation failure");

    }

    @Test(groups = "wso2.greg", dependsOnMethods = "testAddResource")
    public void testAddDependencyToResource()
            throws MalformedURLException, ResourceAdminServiceExceptionException, RemoteException,
                   AddAssociationRegistryExceptionException {

        //create the dependency
        String path = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator
                      + "GREG" + File.separator + "testresource.txt";
        DataHandler dataHandler = new DataHandler(new URL("file:///" + path));

        resourceAdminClient.addResource(DEPENDENCY_PATH, "text/plain", "desc", dataHandler);


        String dependencyType = "depends";
        String todo = "add";

        relationServiceClient.addAssociation(PATH + RES_NAME, dependencyType, DEPENDENCY_PATH, todo);

        DependenciesBean bean = relationServiceClient.getDependencies(PATH + RES_NAME);
        assertTrue(dependencyType.equalsIgnoreCase(bean.getAssociationBeans()[0].getAssociationType()),
                   "Association type is not correct");

        assertTrue(DEPENDENCY_PATH.equalsIgnoreCase(bean.getAssociationBeans()[0].getDestinationPath()),
                   "Target association is not correct");

        assertTrue((PATH + RES_NAME).equalsIgnoreCase(bean.getAssociationBeans()[0].getSourcePath()),
                   "Source association is not correct");

    }

    @Test(groups = "wso2.greg", dependsOnMethods = "testAddDependencyToResource")
    public void testAddRating() throws RegistryException, RegistryExceptionException {
        infoServiceAdminClient.rateResource(RES_RATING, PATH + RES_NAME, sessionId);

        int userRating = infoServiceAdminClient.getRatings(PATH + RES_NAME, sessionId).getUserRating();

        assertTrue(RES_RATING.equalsIgnoreCase(Integer.toString(userRating)), "Resource rating error");
    }

    @Test(groups = "wso2.greg", dependsOnMethods = "testAddRating")
    public void testAddTag() throws RegistryException, AxisFault, RegistryExceptionException {

        infoServiceAdminClient.addTag(TAG, PATH + RES_NAME, sessionId);

        String tag = infoServiceAdminClient.getTags(PATH + RES_NAME, sessionId).getTags()[0].getTagName();

        assertTrue(TAG.equalsIgnoreCase(tag), "Tags does not match");
    }

    @Test(groups = "wso2.greg", dependsOnMethods = "testAddTag", enabled = true)
    public void testAddAssociation()
            throws AddAssociationRegistryExceptionException, RemoteException,
                   ResourceAdminServiceExceptionException,
                   MalformedURLException {

        //create the resource file for association
        String path = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator
                      + "GREG" + File.separator + "testresource.txt";
        DataHandler dataHandler = new DataHandler(new URL("file:///" + path));


        resourceAdminClient.addResource(ASSOCIATION_PATH, "text/plain", "desc", dataHandler);

        String associationType = "association";
        String todo = "add";

        relationServiceClient.addAssociation(PATH + RES_NAME, associationType, ASSOCIATION_PATH, todo);
        AssociationTreeBean aTreeBean = relationServiceClient.getAssociationTree(PATH + RES_NAME, "association");


        assertTrue(aTreeBean.getAssociationTree().contains(ASSOCIATION_PATH),
                   "Association is not correct");

    }

    @Test(groups = "wso2.greg", dependsOnMethods = "testAddAssociation")
    public void testAddCommentToResource()
            throws AddAssociationRegistryExceptionException, RemoteException, RegistryException,
                   RegistryExceptionException {

        infoServiceAdminClient.addComment("This is a comment", PATH + RES_NAME, sessionId);

        CommentBean cBean = infoServiceAdminClient.getComments(PATH + RES_NAME, sessionId);
        Comment[] comments = cBean.getComments();
        boolean found = false;
        for (Comment comment : comments) {
            if ("This is a comment".equalsIgnoreCase(comment.getContent())) {
                found = true;
            }
        }
        assertTrue(found, "Comment was not found");
    }

    @Test(groups = "wso2.greg", dependsOnMethods = "testAddCommentToResource")
    public void testAddLifeCycleToResource()
            throws IOException, LifeCycleManagementServiceExceptionException, InterruptedException,
                   RegistryException,
                   CustomLifecyclesChecklistAdminServiceExceptionException {

        //create the life cycle
        String filePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator
                          + "GREG" + File.separator + "lifecycle" + File.separator + "customLifeCycle.xml";
        String lifeCycleConfiguration = FileManager.readFile(filePath);
        assertTrue(lifeCycleManagementClient.addLifeCycle(lifeCycleConfiguration)
                , "Adding New LifeCycle Failed");
        Thread.sleep(2000);
        lifeCycleConfiguration = lifeCycleManagementClient.getLifecycleConfiguration(ASPECT_NAME);
        assertTrue(lifeCycleConfiguration.contains("aspect name=\"IntergalacticServiceLC\""),
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

        //associate the life cycle with the collection
        wsRegistryServiceClient.associateAspect(PATH + RES_NAME, ASPECT_NAME);

        String[] aspects = wsRegistryServiceClient.getAvailableAspects();
        found = false;
        for (String aspect : aspects) {
            if (ASPECT_NAME.equalsIgnoreCase(aspect)) {
                found = true;
            }
        }

        assertTrue(found, "Life cycle is not attached with the collection");
    }

    @Test(groups = "wso2.greg", dependsOnMethods = "testAddLifeCycleToResource")
    public void testAddRole() throws Exception {
        if(!userManagementClient.roleNameExists(ROLE_NAME)){
            userManagementClient.addRole(ROLE_NAME, new String[]{userInfo.getUserNameWithoutDomain()},
                    new String[]{""});
        }

        assertTrue(userManagementClient.roleNameExists(ROLE_NAME), "Adding role failed.");
    }

    @Test(groups = "wso2.greg", dependsOnMethods = "testAddRole")
    public void testAddSubscription() throws RemoteException {

        SubscriptionBean bean =
                infoServiceAdminClient.subscribe(PATH + RES_NAME, "work://RoleSubscriptionTest",
                                                 "ResourceUpdated",
                                                 sessionId);
        assertNotNull(bean.getSubscriptionInstances(), "Error adding subscriptions");
    }

    @Test(groups = "wso2.greg", dependsOnMethods = "testAddSubscription")
    public void testMoveResource() throws ResourceAdminServiceExceptionException, RemoteException {

        resourceAdminClient.moveResource(PATH, PATH + RES_NAME, RES_MOVED_LOCATION, RES_NAME_AFTER_MOVING);

        String authorUserName =
                resourceAdminClient.getResource(RES_MOVED_LOCATION + RES_NAME_AFTER_MOVING)[0].getAuthorUserName();
        assertTrue(userInfo.getUserNameWithoutDomain().equalsIgnoreCase(authorUserName), "Collection copying failure");
    }

    @Test(groups = "wso2.greg", dependsOnMethods = "testMoveResource", description = "Checks the dependencies after copying the collection")
    public void testDependenciesAfterMoving()
            throws AddAssociationRegistryExceptionException, RemoteException {

        String dependencyType = "depends";
        DependenciesBean bean = relationServiceClient.getDependencies(RES_MOVED_LOCATION + RES_NAME_AFTER_MOVING);
        assertTrue(dependencyType.equalsIgnoreCase(bean.getAssociationBeans()[0].getAssociationType()),
                   "Dependency type is not correct after copying.");

        assertTrue(DEPENDENCY_PATH.equalsIgnoreCase(bean.getAssociationBeans()[0].getDestinationPath()),
                   "Target dependency is not correct after copying.");

        assertTrue((RES_MOVED_LOCATION + RES_NAME_AFTER_MOVING).
                equalsIgnoreCase(bean.getAssociationBeans()[0].getSourcePath()),
                   "Source dependency is not correct after copying.");

    }

    @Test(groups = "wso2.greg", dependsOnMethods = "testDependenciesAfterMoving",
          description = "Checks the associations after copying the collection")
    public void testAssociationsAfterMoving()
            throws AddAssociationRegistryExceptionException, RemoteException {

        AssociationTreeBean aTreeBean =
                relationServiceClient.getAssociationTree(RES_MOVED_LOCATION + RES_NAME_AFTER_MOVING, "association");

        assertTrue(aTreeBean.getAssociationTree().contains(ASSOCIATION_PATH),
                   "Association is not correct after copying.");
    }

    @Test(groups = "wso2.greg", dependsOnMethods = "testAssociationsAfterMoving",
          description = "Checks the comments after copying the collection")
    public void testCommentsAfterMoving() throws RegistryException, RegistryExceptionException {

        CommentBean cBean = infoServiceAdminClient.getComments(RES_MOVED_LOCATION + RES_NAME_AFTER_MOVING, sessionId);
        Comment[] comments = cBean.getComments();
        boolean found = false;
        for (Comment comment : comments) {
            if ("This is a comment".equalsIgnoreCase(comment.getContent())) {
                found = true;
            }
        }
        assertTrue(found, "Comment was not found  after copying.");
    }


    @Test(groups = "wso2.greg", dependsOnMethods = "testCommentsAfterMoving",
          description = "Checks the Lifecycle after copying the collection")
    public void testLifecycleAfterMoving()
            throws LifeCycleManagementServiceExceptionException, RemoteException,
                   RegistryException {

        String lifeCycleConfiguration = lifeCycleManagementClient.getLifecycleConfiguration(ASPECT_NAME);
        assertTrue(lifeCycleConfiguration.contains("aspect name=\"IntergalacticServiceLC\""),
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
        assertTrue(found, "Life Cycle list not contain newly added life cycle  after copying.");

        //associate the life cycle with the collection
        wsRegistryServiceClient.associateAspect(RES_MOVED_LOCATION + RES_NAME_AFTER_MOVING, ASPECT_NAME);

        String[] aspects = wsRegistryServiceClient.getAvailableAspects();
        found = false;
        for (String aspect : aspects) {
            if (ASPECT_NAME.equalsIgnoreCase(aspect)) {
                found = true;
            }
        }

        assertTrue(found, "Life cycle is not attached with the collection  after copying.");
    }

    @Test(groups = "wso2.greg", dependsOnMethods = "testLifecycleAfterMoving", description = "Checks the rating after copying the collection")
    public void testRatingAfterMoving() throws RegistryException, RegistryExceptionException {

        int userRating = infoServiceAdminClient.getRatings(
                RES_MOVED_LOCATION + RES_NAME_AFTER_MOVING, sessionId).getUserRating();

        assertTrue(RES_RATING.equalsIgnoreCase(Integer.toString(userRating)), "Resource rating error  after copying.");
    }

    //    The following test is disabled because of REGISTRY-810
    @Test(groups = "wso2.greg", dependsOnMethods = "testRatingAfterMoving",
          description = "Checks the subscriptions after copying the collection", enabled = false)
    public void testSubscriptionsAfterCopying()
            throws RegistryException, RegistryExceptionException {

        SubscriptionBean sBean = infoServiceAdminClient.getSubscriptions(
                RES_MOVED_LOCATION + RES_NAME_AFTER_MOVING, sessionId);

        assertNotNull(sBean.getSubscriptionInstances(), "Error retrieving subscriptions  after copying.");
    }

    @Test(groups = "wso2.greg", dependsOnMethods = "testLifecycleAfterMoving", description = "Checks the tags after copying the collection")
    public void testTagsAfterCopying() throws RegistryException, RegistryExceptionException {

        String tag =
                infoServiceAdminClient.getTags(
                        RES_MOVED_LOCATION + RES_NAME_AFTER_MOVING, sessionId).getTags()[0].getTagName();

        assertTrue(TAG.equalsIgnoreCase(tag), "Tags does not match  after copying.");
    }

    @AfterClass
    public void cleanUp() throws Exception {
        resourceAdminClient.deleteResource(PATH);
        resourceAdminClient.deleteResource(DEPENDENCY_PATH);
        resourceAdminClient.deleteResource(ASSOCIATION_PATH);
        resourceAdminClient.deleteResource("/c1");
        userManagementClient.deleteRole(ROLE_NAME);
        wsRegistryServiceClient.removeAspect(ASPECT_NAME);
        lifeCycleManagementClient.deleteLifeCycle("IntergalacticServiceLC");

        resourceAdminClient = null;
        relationServiceClient=null;
        userManagementClient = null;
        lifeCycleManagementClient = null;
        wsRegistryServiceClient = null;
        infoServiceAdminClient = null;
        userInfo=null;
    }

}
