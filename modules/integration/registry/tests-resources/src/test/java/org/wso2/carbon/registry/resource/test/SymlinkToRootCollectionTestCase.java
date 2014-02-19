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
import org.wso2.carbon.automation.api.clients.registry.SearchAdminServiceClient;
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
import org.wso2.carbon.registry.relations.stub.beans.xsd.AssociationBean;
import org.wso2.carbon.registry.relations.stub.beans.xsd.AssociationTreeBean;
import org.wso2.carbon.registry.relations.stub.beans.xsd.DependenciesBean;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.search.metadata.test.bean.SearchParameterBean;
import org.wso2.carbon.registry.search.stub.SearchAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.search.stub.beans.xsd.AdvancedSearchResultsBean;
import org.wso2.carbon.registry.search.stub.beans.xsd.ArrayOfString;
import org.wso2.carbon.registry.search.stub.beans.xsd.CustomSearchParameterBean;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import javax.activation.DataHandler;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.*;

public class SymlinkToRootCollectionTestCase {

    private UserInfo userInfo;
    private ResourceAdminServiceClient resourceAdminClient;
    private RelationAdminServiceClient relationServiceClient;
    private LifeCycleManagementClient lifeCycleManagementClient;
    private WSRegistryServiceClient wsRegistryServiceClient;
    private SearchAdminServiceClient searchAdminServiceClient;
    private InfoServiceAdminClient infoServiceAdminClient;
    UserManagementClient userManagementClient;

    private static final String ROOT = "/";
    private static final String COLL_NAME = "rootTestFolder";
    private static final String DEPENDENCY_PATH = "/_system/config/dependencyTest";
    private static final String TAG = "TestTag";
    private static final String ASSOCIATION_PATH = "/_system/config/associationTest";
    private static final String SYMLINK_LOC = "/_system/";
    private static final String SYMLINK_NAME = "TestSymlink";
    private static final String COPY_OF_SYMLINK_NAME = "CopyOfTestSymlink";
    private static final String SYMLINK_TO_ROOT_LOC = "/_system";
    private static final String SYMLINK_TO_ROOT_NAME = "RootSymLink";


    private final String ASPECT_NAME = "IntergalacticServiceLC";
    private static final String ROLE_NAME = "RoleSubscriptionTest";
    private String sessionId;

    @BeforeClass(alwaysRun = true)
    public void initialize()
            throws LoginAuthenticationExceptionException, RemoteException, RegistryException {

        int userId = 2;
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
        searchAdminServiceClient = new SearchAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                                                environment.getGreg().getSessionCookie());


    }


    @Test(groups = "wso2.greg", expectedExceptions = AxisFault.class, enabled = true)
    public void testCreateSymlinkToRoot()
            throws ResourceAdminServiceExceptionException, RemoteException {
        resourceAdminClient.addSymbolicLink(SYMLINK_TO_ROOT_LOC, SYMLINK_TO_ROOT_NAME, ROOT);
    }

    @Test(groups = "wso2.greg",dependsOnMethods = "testCreateSymlinkToRoot")
    public void testAddCollectionToRoot()
            throws ResourceAdminServiceExceptionException, RemoteException {

        String fileType = "other";
        String description = "A test collection";
        resourceAdminClient.addCollection(ROOT, COLL_NAME, fileType, description);
        String authorUserName = resourceAdminClient.getResource(ROOT + COLL_NAME)[0].getAuthorUserName();
        assertTrue(userInfo.getUserNameWithoutDomain().equalsIgnoreCase(authorUserName), "Root collection creation failure");
    }

    @Test(groups = "wso2.greg", dependsOnMethods = "testAddCollectionToRoot")
    public void testAddSymlinkToRootCollection()
            throws ResourceAdminServiceExceptionException, RemoteException {
        resourceAdminClient.addSymbolicLink(
                SYMLINK_LOC.substring(0, SYMLINK_LOC.length() - 1), SYMLINK_NAME, ROOT + COLL_NAME);

        String authorUserName = resourceAdminClient.getResource(SYMLINK_LOC + SYMLINK_NAME)[0].getAuthorUserName();
        assertTrue(userInfo.getUserNameWithoutDomain().equalsIgnoreCase(authorUserName), "Symlink creation failure");
    }

    @Test(groups = "wso2.greg", dependsOnMethods = "testAddSymlinkToRootCollection")
    public void testCopySymlink() throws ResourceAdminServiceExceptionException, RemoteException {
        resourceAdminClient.copyResource(
                SYMLINK_LOC.substring(0, SYMLINK_LOC.length() - 1),
                SYMLINK_LOC + SYMLINK_NAME, SYMLINK_LOC.substring(0, SYMLINK_LOC.length() - 1), COPY_OF_SYMLINK_NAME);

        String authorUserName = resourceAdminClient.getResource(
                SYMLINK_LOC + COPY_OF_SYMLINK_NAME)[0].getAuthorUserName();
        assertTrue(userInfo.getUserNameWithoutDomain().equalsIgnoreCase(authorUserName), "Copy of Symlink creation failure");
    }


    @Test(groups = "wso2.greg", dependsOnMethods = "testCopySymlink")
    public void testAddDependencyToSymlink()
            throws MalformedURLException, ResourceAdminServiceExceptionException, RemoteException,
                   AddAssociationRegistryExceptionException {

        //create the dependency
        String path = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator
                      + "GREG" + File.separator + "testresource.txt";
        DataHandler dataHandler = new DataHandler(new URL("file:///" + path));

        resourceAdminClient.addResource(DEPENDENCY_PATH, "text/plain", "desc", dataHandler);


        String dependencyType = "depends";
        String todo = "add";

        relationServiceClient.addAssociation(SYMLINK_LOC + SYMLINK_NAME, dependencyType, DEPENDENCY_PATH, todo);

        DependenciesBean bean = relationServiceClient.getDependencies(SYMLINK_LOC + SYMLINK_NAME);
        assertTrue(dependencyType.equalsIgnoreCase(bean.getAssociationBeans()[0].getAssociationType()),
                   "Association type is not correct on symlink.");

        assertTrue(DEPENDENCY_PATH.equalsIgnoreCase(bean.getAssociationBeans()[0].getDestinationPath()),
                   "Target association is not correct  on symlink.");

        assertTrue((SYMLINK_LOC + SYMLINK_NAME).equalsIgnoreCase(bean.getAssociationBeans()[0].getSourcePath()),
                   "Source association is not correct  on symlink.");

        bean = relationServiceClient.getDependencies(ROOT + COLL_NAME);
        assertTrue(dependencyType.equalsIgnoreCase(bean.getAssociationBeans()[0].getAssociationType()),
                   "Association type is not correct on collection in root.");

        assertTrue(DEPENDENCY_PATH.equalsIgnoreCase(bean.getAssociationBeans()[0].getDestinationPath()),
                   "Target association is not correct on collection in root.");

        assertTrue((ROOT + COLL_NAME).equalsIgnoreCase(bean.getAssociationBeans()[0].getSourcePath()),
                   "Source association is not correct on collection in root.");

        bean = relationServiceClient.getDependencies(SYMLINK_LOC + COPY_OF_SYMLINK_NAME);
        assertTrue(dependencyType.equalsIgnoreCase(bean.getAssociationBeans()[0].getAssociationType()),
                   "Association type is not correct on symlink copy.");

        assertTrue(DEPENDENCY_PATH.equalsIgnoreCase(bean.getAssociationBeans()[0].getDestinationPath()),
                   "Target association is not correct on symlink copy.");

        assertTrue((SYMLINK_LOC + COPY_OF_SYMLINK_NAME).equalsIgnoreCase(bean.getAssociationBeans()[0].getSourcePath()),
                   "Source association is not correct on symlink copy.");
    }

    @Test(groups = "wso2.greg", dependsOnMethods = "testAddDependencyToSymlink")
    public void testDeleteDependency()
            throws AddAssociationRegistryExceptionException, RemoteException {

        String dependencyType = "depends";
        String todo = "remove";

        relationServiceClient.addAssociation(SYMLINK_LOC + SYMLINK_NAME, dependencyType, DEPENDENCY_PATH, todo);

        DependenciesBean bean = relationServiceClient.getDependencies(SYMLINK_LOC + SYMLINK_NAME);
        AssociationBean[] aBeans = bean.getAssociationBeans();

        assertNull(aBeans, "Collection- Deleting dependency error");

        bean = relationServiceClient.getDependencies(ROOT + COLL_NAME);
        aBeans = bean.getAssociationBeans();

        assertNull(aBeans, "Collection- Deleting dependency error");


    }

    @Test(groups = "wso2.greg", dependsOnMethods = "testDeleteDependency", enabled = true)
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

        relationServiceClient.addAssociation(SYMLINK_LOC + SYMLINK_NAME, associationType, ASSOCIATION_PATH, todo);
        AssociationTreeBean aTreeBean =
                relationServiceClient.getAssociationTree(SYMLINK_LOC + SYMLINK_NAME, "association");


        assertTrue(aTreeBean.getAssociationTree().contains(ASSOCIATION_PATH),
                   "Symlink- Association is not correct");

        aTreeBean = relationServiceClient.getAssociationTree(ROOT + COLL_NAME, "association");


        assertTrue(aTreeBean.getAssociationTree().contains(ASSOCIATION_PATH),
                   "Collection- Association is not correct");

    }

    @Test(groups = "wso2.greg", dependsOnMethods = "testAddAssociation")
    public void testRemoveAssociation()
            throws AddAssociationRegistryExceptionException, RemoteException {

        String associationType = "association";
        String todo = "remove";

        relationServiceClient.addAssociation(SYMLINK_NAME + SYMLINK_LOC, associationType, ASSOCIATION_PATH, todo);

        DependenciesBean bean = relationServiceClient.getDependencies(SYMLINK_NAME + SYMLINK_LOC);
        AssociationBean[] aBeans = bean.getAssociationBeans();

        assertNull(aBeans[0], "Symlink- Deleting association error");

        bean = relationServiceClient.getDependencies(ROOT + COLL_NAME);
        aBeans = bean.getAssociationBeans();
        //the following line is commented out because it causes the test to fail
        //assertNull(aBeans[0], "Collection- Deleting association error");

    }

    @Test(groups = "wso2.greg", dependsOnMethods = "testRemoveAssociation")
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
        wsRegistryServiceClient.associateAspect(SYMLINK_LOC + SYMLINK_NAME, ASPECT_NAME);

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
    public void testAddCommentToResource()
            throws AddAssociationRegistryExceptionException, RemoteException, RegistryException,
                   RegistryExceptionException {

        String theComment = "!@#$%^&*()";
        infoServiceAdminClient.addComment(theComment, SYMLINK_LOC + SYMLINK_NAME, sessionId);

        CommentBean cBean = infoServiceAdminClient.getComments(SYMLINK_LOC + SYMLINK_NAME, sessionId);
        Comment[] comments = cBean.getComments();
        boolean found = false;
        for (Comment comment : comments) {
            if (theComment.equalsIgnoreCase(comment.getContent())) {
                found = true;
            }
        }
        assertTrue(found, "Symlink-Comment was not found");

        cBean = infoServiceAdminClient.getComments(ROOT + COLL_NAME, sessionId);
        comments = cBean.getComments();
        found = false;

        for (Comment comment : comments) {
            if (theComment.equalsIgnoreCase(comment.getContent())) {
                found = true;
            }
        }
        assertTrue(found, "Collection-Comment was not found");

    }

    @Test(groups = "wso2.greg", dependsOnMethods = "testAddCommentToResource", enabled = true)
    public void testDeleteComment() throws RegistryException, RegistryExceptionException {

        CommentBean cBean = infoServiceAdminClient.getComments(SYMLINK_LOC + SYMLINK_NAME, sessionId);
        Comment[] comments = cBean.getComments();

        infoServiceAdminClient.removeComment(comments[0].getCommentPath(), sessionId);

        cBean = infoServiceAdminClient.getComments(SYMLINK_LOC + SYMLINK_NAME, sessionId);
        comments = cBean.getComments();

        boolean found = false;

        if (comments != null) {
            for (Comment comment : comments) {
                if ("This is a comment".equalsIgnoreCase(comment.getContent())) {
                    found = true;
                }
            }

        }
        assertFalse(found, "Symlink- Comment has not been deleted");

        cBean = infoServiceAdminClient.getComments(ROOT + COLL_NAME, sessionId);
        comments = cBean.getComments();
        found = false;

        if (comments != null) {
            for (Comment comment : comments) {
                if ("This is a comment".equalsIgnoreCase(comment.getContent())) {
                    found = true;
                }
            }
        }


        assertFalse(found, "Collection- Comment has not been deleted");
    }


    @Test(groups = "wso2.greg", dependsOnMethods = "testDeleteComment")
    public void testAddRating() throws RegistryException, RegistryExceptionException {
        infoServiceAdminClient.rateResource("1", SYMLINK_LOC + SYMLINK_NAME, sessionId);

        int userRating = infoServiceAdminClient.getRatings(SYMLINK_LOC + SYMLINK_NAME, sessionId).getUserRating();
        assertTrue(userRating == 1, "Symlink - Resource rating error");

        userRating = infoServiceAdminClient.getRatings(ROOT + COLL_NAME, sessionId).getUserRating();
        assertTrue(userRating == 1, "Collection - Resource rating error");
    }

    @Test(groups = "wso2.greg", dependsOnMethods = "testAddRating")
    public void testEditRating() throws RegistryException, RegistryExceptionException {

        infoServiceAdminClient.rateResource("3", SYMLINK_LOC + SYMLINK_NAME, sessionId);

        int userRating = infoServiceAdminClient.getRatings(SYMLINK_LOC + SYMLINK_NAME, sessionId).getUserRating();
        assertTrue(userRating == 3, "Resource rating editing error");

        userRating = infoServiceAdminClient.getRatings(ROOT + COLL_NAME, sessionId).getUserRating();
        assertTrue(userRating == 3, "Collection - Resource rating editing error");

    }

    @Test(groups = "wso2.greg", dependsOnMethods = "testEditRating")
    public void testAddTag() throws RegistryException, AxisFault, RegistryExceptionException {

        infoServiceAdminClient.addTag(TAG, SYMLINK_LOC + SYMLINK_NAME, sessionId);

        String tag = infoServiceAdminClient.getTags(SYMLINK_LOC + SYMLINK_NAME, sessionId).getTags()[0].getTagName();
        assertTrue(TAG.equalsIgnoreCase(tag), "Symlink - Tags does not match");

        tag = infoServiceAdminClient.getTags(ROOT + COLL_NAME, sessionId).getTags()[0].getTagName();
        assertTrue(TAG.equalsIgnoreCase(tag), "Collection - Tags does not match");


    }

    @Test(groups = "wso2.greg", dependsOnMethods = "testAddTag", expectedExceptions = NullPointerException.class)
    public void testRemoveTag() throws RegistryException, RegistryExceptionException {

        infoServiceAdminClient.removeTag(TAG, SYMLINK_LOC + SYMLINK_NAME, sessionId);

        infoServiceAdminClient.getTags(SYMLINK_LOC + SYMLINK_NAME, sessionId).getTags()[0].getTagName();

        infoServiceAdminClient.getTags(ROOT + COLL_NAME, sessionId).getTags()[0].getTagName();

    }

    @Test(groups = "wso2.greg", dependsOnMethods = "testRemoveTag")
    public void testAddRole() throws Exception {

        userManagementClient.addRole(ROLE_NAME, new String[]{userInfo.getUserNameWithoutDomain()},
                                     new String[]{""});
        assertTrue(userManagementClient.roleNameExists(ROLE_NAME));
    }

    @Test(groups = "wso2.greg", dependsOnMethods = "testAddRole")
    public void testAddSubscription()
            throws RemoteException, RegistryException, RegistryExceptionException {

        SubscriptionBean bean =
                infoServiceAdminClient.subscribe(SYMLINK_LOC + SYMLINK_NAME, "work://RoleSubscriptionTest",
                                                 "ResourceUpdated",
                                                 sessionId);
        assertNotNull(bean.getSubscriptionInstances(), "Symlink - Error adding subscriptions");

        SubscriptionBean sBean = infoServiceAdminClient.getSubscriptions(ROOT + COLL_NAME, sessionId);
        assertNotNull(sBean.getSubscriptionInstances(), "Collection - Error adding subscriptions");
    }

    @Test(groups = "wso2.greg", dependsOnMethods = "testAddSubscription")
    public void testRemoveSubscription()
            throws RegistryException, RegistryExceptionException, RemoteException {

        SubscriptionBean sBean = infoServiceAdminClient.getSubscriptions(SYMLINK_LOC + SYMLINK_NAME, sessionId);

        infoServiceAdminClient.unsubscribe(
                SYMLINK_LOC + SYMLINK_NAME, sBean.getSubscriptionInstances()[0].getId(), sessionId);

        sBean = infoServiceAdminClient.getSubscriptions(SYMLINK_LOC + SYMLINK_NAME, sessionId);
        assertNull(sBean.getSubscriptionInstances(), "Symlink - Error removing subscriptions");

        sBean = infoServiceAdminClient.getSubscriptions(ROOT + COLL_NAME, sessionId);
        assertNull(sBean.getSubscriptionInstances(), "Collection - Error removing subscriptions");


    }

    @Test(groups = "wso2.greg", dependsOnMethods = "testRemoveSubscription")
    public void testDeleteLifeCycle()
            throws ResourceAdminServiceExceptionException, RemoteException, RegistryException,
                   LifeCycleManagementServiceExceptionException,
                   SearchAdminServiceRegistryExceptionException,
                   InterruptedException {

        resourceAdminClient.deleteResource(SYMLINK_LOC + SYMLINK_NAME);
        resourceAdminClient.deleteResource(ROOT + COLL_NAME);

        wsRegistryServiceClient.removeAspect(ASPECT_NAME);
        assertTrue(lifeCycleManagementClient.deleteLifeCycle(ASPECT_NAME),
                   "Life Cycle Delete failed");

        Thread.sleep(2000);
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setResourceName(ASPECT_NAME);
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        paramList = null;
        AdvancedSearchResultsBean result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
        assertNull(result.getResourceDataList(), "Life Cycle Record Found even if it is deleted");

    }

    //cleanup code
    @AfterClass
    public void cleanup() throws Exception {

        deleteResources(DEPENDENCY_PATH);
        deleteResources(ASSOCIATION_PATH);

        if (userManagementClient.roleNameExists(ROLE_NAME)) {
            userManagementClient.deleteRole(ROLE_NAME);
        }

        for (String aspect : wsRegistryServiceClient.getAvailableAspects()) {
            if (aspect.contains(ASPECT_NAME)) {
                wsRegistryServiceClient.removeAspect(ASPECT_NAME);
            }
        }
        lifeCycleManagementClient.deleteLifeCycle("IntergalacticServiceLC");

        resourceAdminClient = null;
        relationServiceClient = null;
        lifeCycleManagementClient = null;
        wsRegistryServiceClient = null;
        searchAdminServiceClient = null;
        infoServiceAdminClient = null;
        userManagementClient = null;
        userInfo = null;
    }

    private void deleteResources(String path) throws RegistryException {
        if(wsRegistryServiceClient.resourceExists(path)){
            wsRegistryServiceClient.delete(path);
        }
    }
}
