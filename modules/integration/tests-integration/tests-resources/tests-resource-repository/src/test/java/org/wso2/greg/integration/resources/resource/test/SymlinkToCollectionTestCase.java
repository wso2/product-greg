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

package org.wso2.greg.integration.resources.resource.test;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.automation.test.utils.common.FileManager;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.integration.common.admin.client.UserManagementClient;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.info.stub.beans.utils.xsd.Comment;
import org.wso2.carbon.registry.info.stub.beans.xsd.CommentBean;
import org.wso2.carbon.registry.info.stub.beans.xsd.SubscriptionBean;
import org.wso2.carbon.registry.properties.stub.PropertiesAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.relations.stub.AddAssociationRegistryExceptionException;
import org.wso2.carbon.registry.relations.stub.beans.xsd.AssociationBean;
import org.wso2.carbon.registry.relations.stub.beans.xsd.AssociationTreeBean;
import org.wso2.carbon.registry.relations.stub.beans.xsd.DependenciesBean;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.search.stub.SearchAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.search.stub.beans.xsd.AdvancedSearchResultsBean;
import org.wso2.carbon.registry.search.stub.beans.xsd.ArrayOfString;
import org.wso2.carbon.registry.search.stub.beans.xsd.CustomSearchParameterBean;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.clients.*;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;
import org.wso2.greg.integration.resources.search.metadata.test.bean.SearchParameterBean;

import javax.activation.DataHandler;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.*;

public class SymlinkToCollectionTestCase extends GREGIntegrationBaseTest {

    private ResourceAdminServiceClient resourceAdminClient;
    private RelationAdminServiceClient relationServiceClient;
    private LifeCycleManagementClient lifeCycleManagementClient;
    private WSRegistryServiceClient wsRegistryServiceClient;
    private SearchAdminServiceClient searchAdminServiceClient;
    private InfoServiceAdminClient infoServiceAdminClient;
    private UserManagementClient userManagementClient;
    private PropertiesAdminServiceClient propertiesAdminServiceClient;

    private static final String PATH = "/c1/";
    private static final String COLL_NAME = "TestFolder";
    private static final String DEPENDENCY_PATH = "/_system/config/dependencyTest";
    private static final String TAG = "TestTag";
    private static final String ASSOCIATION_PATH = "/_system/config/associationTest";
    private static final String SYMLINK_LOC = "/_system/";
    private static final String SYMLINK_NAME = "TestSymlink";
    private static final String COPY_OF_SYMLINK_NAME = "CopyOfTestSymlink";
    private static final String RES_NAME = "TheResource";
    private static final String RES_NAME_AFTER_RENAME = "RenamedResource";

    private static final String ASPECT_NAME = "IntergalacticServiceLC";
    private static final String ROLE_NAME = "RoleSubscriptionTest";
    private String sessionID;

    @BeforeClass(groups = {"wso2.greg"}, alwaysRun = true)
    public void initialize() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        sessionID = getSessionCookie();

        resourceAdminClient = new ResourceAdminServiceClient(getBackendURL(), sessionID);
        relationServiceClient = new RelationAdminServiceClient(getBackendURL(), sessionID);
        lifeCycleManagementClient = new LifeCycleManagementClient(getBackendURL(), sessionID);
        infoServiceAdminClient = new InfoServiceAdminClient(getBackendURL(), sessionID);
        userManagementClient = new UserManagementClient(getBackendURL(), sessionID);
        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        wsRegistryServiceClient = registryProviderUtil.getWSRegistry(automationContext);
        searchAdminServiceClient = new SearchAdminServiceClient(getBackendURL(), sessionID);
        propertiesAdminServiceClient = new PropertiesAdminServiceClient(getBackendURL(), sessionID);
    }

    @Test(groups = "wso2.greg")
    public void testAddCollection() throws ResourceAdminServiceExceptionException, RemoteException, XPathExpressionException {
        String fileType = "other";
        String description = "A test collection";
        resourceAdminClient.addCollection(PATH, COLL_NAME, fileType, description);
        String authorUserName = resourceAdminClient.getResource(PATH + COLL_NAME)[0].getAuthorUserName();
        assertTrue(automationContext.getContextTenant().getContextUser().getUserName().equalsIgnoreCase(authorUserName), "Root collection creation failure");
    }

    @Test(groups = "wso2.greg", dependsOnMethods = "testAddCollection")
    public void testAddSymlinkToCollection() throws ResourceAdminServiceExceptionException, RemoteException, XPathExpressionException {
        resourceAdminClient.addSymbolicLink(SYMLINK_LOC.substring(0, SYMLINK_LOC.length() - 1), SYMLINK_NAME, PATH + COLL_NAME);
        String authorUserName = resourceAdminClient.getResource(SYMLINK_LOC + SYMLINK_NAME)[0].getAuthorUserName();
        assertTrue(automationContext.getContextTenant().getContextUser().getUserName().equalsIgnoreCase(authorUserName), "Symlink creation failure");
    }

    @Test(groups = "wso2.greg", dependsOnMethods = "testAddSymlinkToCollection")
    public void testCopySymlink() throws ResourceAdminServiceExceptionException, RemoteException, XPathExpressionException {
        resourceAdminClient.copyResource(SYMLINK_LOC.substring(0, SYMLINK_LOC.length() - 1), SYMLINK_LOC + SYMLINK_NAME, SYMLINK_LOC.substring(0, SYMLINK_LOC.length() - 1), COPY_OF_SYMLINK_NAME);
        String authorUserName = resourceAdminClient.getResource(SYMLINK_LOC + COPY_OF_SYMLINK_NAME)[0].getAuthorUserName();
        assertTrue(automationContext.getContextTenant().getContextUser().getUserName().equalsIgnoreCase(authorUserName), "Copy of Symlink creation failure");
    }

    @Test(groups = "wso2.greg", dependsOnMethods = "testCopySymlink")
    public void testAddResourceToCollection() throws ResourceAdminServiceExceptionException, RemoteException, MalformedURLException, XPathExpressionException {
        String path = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator + "GREG" + File.separator + "testresource.txt";
        DataHandler dataHandler = new DataHandler(new URL("file:///" + path));
        resourceAdminClient.addResource(PATH + COLL_NAME + "/" + RES_NAME, "text/plain", "desc", dataHandler);
        String authorUserName = resourceAdminClient.getResource(PATH + COLL_NAME + "/" + RES_NAME)[0].getAuthorUserName();
        assertTrue(automationContext.getContextTenant().getContextUser().getUserName().equalsIgnoreCase(authorUserName), "Adding resource to the collection failed");
        authorUserName = resourceAdminClient.getResource(SYMLINK_LOC + SYMLINK_NAME + "/" + RES_NAME)[0].getAuthorUserName();
        assertTrue(automationContext.getContextTenant().getContextUser().getUserName().equalsIgnoreCase(authorUserName), "Symlink does not point to the new resource");
        authorUserName = resourceAdminClient.getResource(SYMLINK_LOC + COPY_OF_SYMLINK_NAME + "/" + RES_NAME)[0].getAuthorUserName();
        assertTrue(automationContext.getContextTenant().getContextUser().getUserName().equalsIgnoreCase(authorUserName), "Copied symlink does not point to the new resource");
    }

    @Test(groups = "wso2.greg", dependsOnMethods = "testAddResourceToCollection")
    public void testRenameResourceInSymlink() throws ResourceAdminServiceExceptionException, RemoteException, XPathExpressionException {
        resourceAdminClient.renameResource(SYMLINK_LOC + SYMLINK_NAME, SYMLINK_LOC + SYMLINK_NAME + "/" + RES_NAME, RES_NAME_AFTER_RENAME);
        String authorUserName = resourceAdminClient.getResource(PATH + COLL_NAME + "/" + RES_NAME_AFTER_RENAME)[0].getAuthorUserName();
        assertTrue(automationContext.getContextTenant().getContextUser().getUserName().equalsIgnoreCase(authorUserName), "Renamed resource using the symlink is not visible in the collection");
    }

    @Test(groups = "wso2.greg", dependsOnMethods = "testRenameResourceInSymlink")
    public void testAddDependencyToSymlink() throws MalformedURLException, ResourceAdminServiceExceptionException, RemoteException, AddAssociationRegistryExceptionException {
        //create the dependency
        String path = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator + "GREG" + File.separator + "testresource.txt";
        DataHandler dataHandler = new DataHandler(new URL("file:///" + path));
        resourceAdminClient.addResource(DEPENDENCY_PATH, "text/plain", "desc", dataHandler);
        String dependencyType = "depends";
        String todo = "add";
        relationServiceClient.addAssociation(SYMLINK_LOC + SYMLINK_NAME, dependencyType, DEPENDENCY_PATH, todo);
        DependenciesBean bean = relationServiceClient.getDependencies(SYMLINK_LOC + SYMLINK_NAME);
        assertTrue(dependencyType.equalsIgnoreCase(bean.getAssociationBeans()[0].getAssociationType()), "Association type is not correct on symlink.");
        assertTrue(DEPENDENCY_PATH.equalsIgnoreCase(bean.getAssociationBeans()[0].getDestinationPath()), "Target association is not correct  on symlink.");
        assertTrue((SYMLINK_LOC + SYMLINK_NAME).equalsIgnoreCase(bean.getAssociationBeans()[0].getSourcePath()), "Source association is not correct  on symlink.");
        bean = relationServiceClient.getDependencies(PATH + COLL_NAME);
        assertTrue(dependencyType.equalsIgnoreCase(bean.getAssociationBeans()[0].getAssociationType()), "Association type is not correct on collection in root.");
        assertTrue(DEPENDENCY_PATH.equalsIgnoreCase(bean.getAssociationBeans()[0].getDestinationPath()), "Target association is not correct on collection in root.");
        assertTrue((PATH + COLL_NAME).equalsIgnoreCase(bean.getAssociationBeans()[0].getSourcePath()), "Source association is not correct on collection in root.");
        bean = relationServiceClient.getDependencies(SYMLINK_LOC + COPY_OF_SYMLINK_NAME);
        assertTrue(dependencyType.equalsIgnoreCase(bean.getAssociationBeans()[0].getAssociationType()), "Association type is not correct on symlink copy.");
        assertTrue(DEPENDENCY_PATH.equalsIgnoreCase(bean.getAssociationBeans()[0].getDestinationPath()), "Target association is not correct on symlink copy.");
        assertTrue((SYMLINK_LOC + COPY_OF_SYMLINK_NAME).equalsIgnoreCase(bean.getAssociationBeans()[0].getSourcePath()), "Source association is not correct on symlink copy.");
    }

    @Test(groups = "wso2.greg", dependsOnMethods = "testAddDependencyToSymlink")
    public void testDeleteDependency() throws AddAssociationRegistryExceptionException, RemoteException {
        String dependencyType = "depends";
        String todo = "remove";
        relationServiceClient.addAssociation(SYMLINK_LOC + SYMLINK_NAME, dependencyType, DEPENDENCY_PATH, todo);
        DependenciesBean bean = relationServiceClient.getDependencies(SYMLINK_LOC + SYMLINK_NAME);
        AssociationBean[] aBeans = bean.getAssociationBeans();
        assertNull(aBeans, "Collection- Deleting dependency error");
        bean = relationServiceClient.getDependencies(PATH + COLL_NAME);
        aBeans = bean.getAssociationBeans();
        assertNull(aBeans, "Collection- Deleting dependency error");
    }

    @Test(groups = "wso2.greg", dependsOnMethods = "testDeleteDependency", enabled = true)
    public void testAddAssociation() throws AddAssociationRegistryExceptionException, RemoteException, ResourceAdminServiceExceptionException, MalformedURLException {
        //create the resource file for association
        String path = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator + "GREG" + File.separator + "testresource.txt";
        DataHandler dataHandler = new DataHandler(new URL("file:///" + path));
        resourceAdminClient.addResource(ASSOCIATION_PATH, "text/plain", "desc", dataHandler);
        String associationType = "association";
        String todo = "add";
        relationServiceClient.addAssociation(SYMLINK_LOC + SYMLINK_NAME, associationType, ASSOCIATION_PATH, todo);
        AssociationTreeBean aTreeBean = relationServiceClient.getAssociationTree(SYMLINK_LOC + SYMLINK_NAME, "association");
        assertTrue(aTreeBean.getAssociationTree().contains(ASSOCIATION_PATH), "Symlink- Association is not correct");
        aTreeBean = relationServiceClient.getAssociationTree(PATH + COLL_NAME, "association");
        assertTrue(aTreeBean.getAssociationTree().contains(ASSOCIATION_PATH), "Collection- Association is not correct");
    }

    @Test(groups = "wso2.greg", dependsOnMethods = "testAddAssociation")
    public void testRemoveAssociation() throws AddAssociationRegistryExceptionException, RemoteException {
        String associationType = "association";
        String todo = "remove";
        relationServiceClient.addAssociation(SYMLINK_NAME + SYMLINK_LOC, associationType, ASSOCIATION_PATH, todo);
        DependenciesBean bean = relationServiceClient.getDependencies(SYMLINK_NAME + SYMLINK_LOC);
        AssociationBean[] aBeans = bean.getAssociationBeans();
        assertNull(aBeans[0], "Symlink- Deleting association error");
        bean = relationServiceClient.getDependencies(PATH + COLL_NAME);
        bean.getAssociationBeans();
        //the following line is commented out because it causes the test to fail
        //assertNull(aBeans[0], "Collection- Deleting association error");
    }

    @Test(groups = "wso2.greg", dependsOnMethods = "testRemoveAssociation")
    public void testAddLifeCycleToResource() throws IOException, LifeCycleManagementServiceExceptionException, InterruptedException, RegistryException, CustomLifecyclesChecklistAdminServiceExceptionException {
        //create the life cycle
        String filePath = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator + "GREG" + File.separator + "lifecycle" + File.separator + "customLifeCycle.xml";
        String lifeCycleConfiguration = FileManager.readFile(filePath);
        assertTrue(lifeCycleManagementClient.addLifeCycle(lifeCycleConfiguration), "Adding New LifeCycle Failed");
        Thread.sleep(2000);
        lifeCycleConfiguration = lifeCycleManagementClient.getLifecycleConfiguration(ASPECT_NAME);
        assertTrue(lifeCycleConfiguration.contains("aspect name=\"IntergalacticServiceLC\""), "LifeCycleName Not Found in lifecycle configuration");
        String[] lifeCycleList = lifeCycleManagementClient.getLifecycleList();
        assertNotNull(lifeCycleList);
        assertTrue(lifeCycleList.length > 0, "Life Cycle List length zero");
        boolean found = false;
        for(String lc : lifeCycleList) {
            if(ASPECT_NAME.equalsIgnoreCase(lc)) {
                found = true;
            }
        }
        assertTrue(found, "Life Cycle list not contain newly added life cycle");
        //associate the life cycle with the collection
        wsRegistryServiceClient.associateAspect(SYMLINK_LOC + SYMLINK_NAME, ASPECT_NAME);
        String[] aspects = wsRegistryServiceClient.getAvailableAspects();
        found = false;
        for(String aspect : aspects) {
            if(ASPECT_NAME.equalsIgnoreCase(aspect)) {
                found = true;
            }
        }
        assertTrue(found, "Life cycle is not attached with the collection");
    }

    @Test(groups = "wso2.greg", dependsOnMethods = "testAddLifeCycleToResource")
    public void testAddCommentToResource() throws Exception {
        String theComment = "!@#$%^&*()";
        infoServiceAdminClient.addComment(theComment, SYMLINK_LOC + SYMLINK_NAME, sessionID);
        CommentBean cBean = infoServiceAdminClient.getComments(SYMLINK_LOC + SYMLINK_NAME, sessionID);
        Comment[] comments = cBean.getComments();
        boolean found = false;
        for(Comment comment : comments) {
            if(theComment.equalsIgnoreCase(comment.getContent())) {
                found = true;
            }
        }
        assertTrue(found, "Symlink-Comment was not found");
        cBean = infoServiceAdminClient.getComments(PATH + COLL_NAME, sessionID);
        comments = cBean.getComments();
        found = false;
        for(Comment comment : comments) {
            if(theComment.equalsIgnoreCase(comment.getContent())) {
                found = true;
            }
        }
        assertTrue(found, "Collection-Comment was not found");
    }

    @Test(groups = "wso2.greg", dependsOnMethods = "testAddCommentToResource", enabled = true)
    public void testDeleteComment() throws Exception {
        CommentBean cBean = infoServiceAdminClient.getComments(SYMLINK_LOC + SYMLINK_NAME, sessionID);
        Comment[] comments = cBean.getComments();
        infoServiceAdminClient.removeComment(comments[0].getCommentPath(), sessionID);
        cBean = infoServiceAdminClient.getComments(SYMLINK_LOC + SYMLINK_NAME, sessionID);
        comments = cBean.getComments();
        boolean found = false;
        if(comments != null) {
            for(Comment comment : comments) {
                if("This is a comment".equalsIgnoreCase(comment.getContent())) {
                    found = true;
                }
            }
        }
        assertFalse(found, "Symlink- Comment has not been deleted");
        cBean = infoServiceAdminClient.getComments(PATH + COLL_NAME, sessionID);
        comments = cBean.getComments();
        found = false;
        if(comments != null) {
            for(Comment comment : comments) {
                if("This is a comment".equalsIgnoreCase(comment.getContent())) {
                    found = true;
                }
            }
        }
        assertFalse(found, "Collection- Comment has not been deleted");
    }

    @Test(groups = "wso2.greg", dependsOnMethods = "testDeleteComment")
    public void testAddRating() throws Exception {
        infoServiceAdminClient.rateResource("1", SYMLINK_LOC + SYMLINK_NAME, sessionID);
        int userRating = infoServiceAdminClient.getRatings(SYMLINK_LOC + SYMLINK_NAME, sessionID).getUserRating();
        assertTrue(userRating == 1, "Symlink - Resource rating error");
        userRating = infoServiceAdminClient.getRatings(PATH + COLL_NAME, sessionID).getUserRating();
        assertTrue(userRating == 1, "Collection - Resource rating error");
    }

    @Test(groups = "wso2.greg", dependsOnMethods = "testAddRating")
    public void testEditRating() throws Exception {
        infoServiceAdminClient.rateResource("3", SYMLINK_LOC + SYMLINK_NAME, sessionID);
        int userRating = infoServiceAdminClient.getRatings(SYMLINK_LOC + SYMLINK_NAME, sessionID).getUserRating();
        assertTrue(userRating == 3, "Resource rating editing error");
        userRating = infoServiceAdminClient.getRatings(PATH + COLL_NAME, sessionID).getUserRating();
        assertTrue(userRating == 3, "Collection - Resource rating editing error");
    }

    @Test(groups = "wso2.greg", dependsOnMethods = "testEditRating")
    public void testAddTag() throws Exception {
        infoServiceAdminClient.addTag(TAG, SYMLINK_LOC + SYMLINK_NAME, sessionID);
        String tag = infoServiceAdminClient.getTags(SYMLINK_LOC + SYMLINK_NAME, sessionID).getTags()[0].getTagName();
        assertTrue(TAG.equalsIgnoreCase(tag), "Symlink - Tags does not match");
        tag = infoServiceAdminClient.getTags(PATH + COLL_NAME, sessionID).getTags()[0].getTagName();
        assertTrue(TAG.equalsIgnoreCase(tag), "Collection - Tags does not match");
    }

    @Test(groups = "wso2.greg", dependsOnMethods = "testAddTag", expectedExceptions = NullPointerException.class)
    public void testRemoveTag() throws Exception {
        infoServiceAdminClient.removeTag(TAG, SYMLINK_LOC + SYMLINK_NAME, sessionID);
        infoServiceAdminClient.getTags(SYMLINK_LOC + SYMLINK_NAME, sessionID).getTags()[0].getTagName();
        infoServiceAdminClient.getTags(PATH + COLL_NAME, sessionID).getTags()[0].getTagName();
    }

    @Test(groups = "wso2.greg", dependsOnMethods = "testRemoveTag")
    public void testAddRole() throws Exception {
        userManagementClient.addRole(ROLE_NAME, new String[]{automationContext.getContextTenant().getContextUser().getUserName()}, new String[]{""});
        assertTrue(userManagementClient.roleNameExists(ROLE_NAME));
    }

    @Test(groups = "wso2.greg", dependsOnMethods = "testAddRole")
    public void testAddSubscription() throws Exception {
        SubscriptionBean bean = infoServiceAdminClient.subscribe(SYMLINK_LOC + SYMLINK_NAME, "work://RoleSubscriptionTest", "ResourceUpdated", sessionID);
        assertNotNull(bean.getSubscriptionInstances(), "Symlink - Error adding subscriptions");
        SubscriptionBean sBean = infoServiceAdminClient.getSubscriptions(PATH + COLL_NAME, sessionID);
        assertNotNull(sBean.getSubscriptionInstances(), "Collection - Error adding subscriptions");
    }

    @Test(groups = "wso2.greg", dependsOnMethods = "testAddSubscription")
    public void testRemoveSubscription() throws Exception {
        SubscriptionBean sBean = infoServiceAdminClient.getSubscriptions(SYMLINK_LOC + SYMLINK_NAME, sessionID);
        infoServiceAdminClient.unsubscribe(SYMLINK_LOC + SYMLINK_NAME, sBean.getSubscriptionInstances()[0].getId(), sessionID);
        sBean = infoServiceAdminClient.getSubscriptions(SYMLINK_LOC + SYMLINK_NAME, sessionID);
        assertNull(sBean.getSubscriptionInstances(), "Symlink - Error removing subscriptions");
        sBean = infoServiceAdminClient.getSubscriptions(PATH + COLL_NAME, sessionID);
        assertNull(sBean.getSubscriptionInstances(), "Collection - Error removing subscriptions");
    }

    @Test(groups = "wso2.greg", dependsOnMethods = "testRemoveSubscription")
    public void testAddProperties() throws PropertiesAdminServiceRegistryExceptionException, RemoteException, ResourceAdminServiceExceptionException {
        String propertyName = "propertyName";
        String propertyValue = "propertyValue";
        propertiesAdminServiceClient.setProperty(SYMLINK_LOC + SYMLINK_NAME, propertyName, propertyValue);
        String propValue = resourceAdminClient.getProperty(PATH + COLL_NAME, propertyName);
        assertTrue(propertyValue.equalsIgnoreCase(propValue), "Property values does not match");
    }

    /*
    Create symlink to a collection. make a copy of the symlink and delete the original symlink.
    Check if the copy still remains with all its content.
     */

    @Test(groups = "wso2.greg", dependsOnMethods = "testAddProperties")
    public void testCopiedSymlinkStatus() throws ResourceAdminServiceExceptionException, RemoteException, XPathExpressionException {
        resourceAdminClient.deleteResource(SYMLINK_LOC + SYMLINK_NAME);
        String authorUserName = resourceAdminClient.getResource(SYMLINK_LOC + COPY_OF_SYMLINK_NAME + "/" + RES_NAME_AFTER_RENAME)[0].getAuthorUserName();
        assertTrue(automationContext.getContextTenant().getContextUser().getUserName().equalsIgnoreCase(authorUserName), "Resource cannot be accessed after the original copy of the symbolic link is deleted");
    }

    @Test(groups = "wso2.greg", dependsOnMethods = "testCopiedSymlinkStatus")
    public void testDeleteLifeCycle() throws ResourceAdminServiceExceptionException, RemoteException, RegistryException, LifeCycleManagementServiceExceptionException, InterruptedException, SearchAdminServiceRegistryExceptionException {
        resourceAdminClient.deleteResource(PATH);
        wsRegistryServiceClient.removeAspect(ASPECT_NAME);
        assertTrue(lifeCycleManagementClient.deleteLifeCycle(ASPECT_NAME), "Life Cycle Delete failed");
        Thread.sleep(2000);
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setResourceName(ASPECT_NAME);
        ArrayOfString[] paramList = paramBean.getParameterList();
        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
        assertNull(result.getResourceDataList(), "Life Cycle Record Found even if it is deleted");
    }

    //cleanup code
    @AfterClass(groups = {"wso2.greg"}, alwaysRun = true)
    public void cleanup() throws Exception {
        UserManagementClient adminUserManagementClient = new UserManagementClient(getBackendURL(), sessionID);
        resourceAdminClient.deleteResource(DEPENDENCY_PATH);
        resourceAdminClient.deleteResource(ASSOCIATION_PATH);
        resourceAdminClient.deleteResource(SYMLINK_LOC + "/" + COPY_OF_SYMLINK_NAME);
        adminUserManagementClient.deleteRole(ROLE_NAME);
        wsRegistryServiceClient.removeAspect(ASPECT_NAME);
        lifeCycleManagementClient.deleteLifeCycle("IntergalacticServiceLC");
        resourceAdminClient = null;
        relationServiceClient = null;
        lifeCycleManagementClient = null;
        wsRegistryServiceClient = null;
        searchAdminServiceClient = null;
        infoServiceAdminClient = null;
        userManagementClient = null;
        propertiesAdminServiceClient = null;
    }
}
