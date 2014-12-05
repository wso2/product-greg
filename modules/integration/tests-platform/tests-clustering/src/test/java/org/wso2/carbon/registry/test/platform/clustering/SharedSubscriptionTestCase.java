/*
*Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.test.platform.clustering;

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
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault;
import org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault;
import org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.info.stub.RegistryExceptionException;
import org.wso2.carbon.registry.info.stub.beans.utils.xsd.Comment;
import org.wso2.carbon.registry.info.stub.beans.xsd.CommentBean;
import org.wso2.carbon.registry.info.stub.beans.xsd.SubscriptionBean;
import org.wso2.carbon.registry.relations.stub.AddAssociationRegistryExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.greg.integration.common.clients.HumanTaskAdminClient;
import org.wso2.greg.integration.common.clients.InfoServiceAdminClient;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.clients.UserManagementClient;
import org.wso2.greg.integration.common.clients.WorkItem;
import org.wso2.greg.integration.common.utils.subscription.WorkItemClient;
import org.wso2.greg.platform.common.utils.GREGPlatformBaseTest;

public class SharedSubscriptionTestCase extends GREGPlatformBaseTest {

	//Base path to add resource and collection for testing "/_system/governance/"
	private static final String GOV_COLLECTION_PATH = File.separator +"_system"+ File.separator +"governance" + File.separator;
	//resource name
	private static final String RESOURCE_NAME = "test.css";
	//tag name
	private static final String TAG = "TestUpdateTag";

	private AutomationContext automationContext1, automationContext2;
	private String sessionID1, sessionID2;
	private String backendURL1, backendURL2;

	private ResourceAdminServiceClient resourceAdminServiceClient1;
	private UserManagementClient userManagementClient1;
	private InfoServiceAdminClient infoServiceAdminClient1,infoServiceAdminClient2;

	private String userNameWithoutDomain;

	@DataProvider(name = "SubscriptionPathDataProvider")
	public Object[][] sdp() {
		return new Object[][] { new Object[] { GOV_COLLECTION_PATH + RESOURCE_NAME }};
	}

	/**
	 * Prepare environment for tests.
	 * 
	 * @throws Exception
	 */
	@BeforeClass(alwaysRun = true)
	public void init() throws Exception {
		super.initCluster(TestUserMode.SUPER_TENANT_ADMIN);

		//get the automation context for cluster nodes
		automationContext1 = getAutomationContextWithKey("greg001");
		automationContext2 = getAutomationContextWithKey("greg002");

		//get session and backendURL
		sessionID1 = getSessionCookie(automationContext1);
		sessionID2 = getSessionCookie(automationContext2);
		backendURL1 = getBackEndUrl(automationContext1);
		backendURL2 = getBackEndUrl(automationContext2);

		resourceAdminServiceClient1 = new ResourceAdminServiceClient(
				backendURL1, sessionID1);
		infoServiceAdminClient1 = new InfoServiceAdminClient(backendURL1,
				sessionID1);
		userManagementClient1 = new UserManagementClient(backendURL1,
				sessionID1);

		String userName = automationContext1.getContextTenant()
				.getContextUser().getUserName();
		if (userName.contains("@")) {
			userNameWithoutDomain = userName
					.substring(0, userName.indexOf('@'));
		} else {
			userNameWithoutDomain = userName;
		}

		infoServiceAdminClient2 = new InfoServiceAdminClient(backendURL2,
				sessionID2);
	}

	/**
	 * Add a resource to governance collection path
	 * 
	 * @throws MalformedURLException
	 * @throws RemoteException
	 * @throws ResourceAdminServiceExceptionException
	 * 
	 */
	@Test(groups = "wso2.greg", description = "Add resource")
	public void testAddResource() throws MalformedURLException,
			RemoteException, ResourceAdminServiceExceptionException {
        String resourcePath = getTestArtifactLocation() + "artifacts" +
                File.separator + "GREG" + File.separator + RESOURCE_NAME;
		DataHandler dh = new DataHandler(new URL("file:///" + resourcePath));
		resourceAdminServiceClient1.addResource(GOV_COLLECTION_PATH
				+ RESOURCE_NAME, "test/plain", "testDesc", dh);
		assertTrue(resourceAdminServiceClient1.getResource(GOV_COLLECTION_PATH
				+ RESOURCE_NAME)[0] != null);
	}

	/**
	 * add role
	 * 
	 * @throws Exception
	 */
	@Test(groups = "wso2.greg", description = "Add role", dependsOnMethods = "testAddResource")
	public void testAddRole() throws Exception {
        if(userManagementClient1.roleNameExists("RoleSubscriptionTest1")) {
            return;
        }
        userManagementClient1.addRole("RoleSubscriptionTest1", new String[]{userNameWithoutDomain}, new String[]{""});
        assertTrue(userManagementClient1.roleNameExists("RoleSubscriptionTest1"));
	}

	/**
	 * Create a subscription for resource to send notifications via
	 * Management Console
	 * 
	 * @param path: path of the resource
	 * @throws Exception
	 */
	@Test(groups = "wso2.greg", description = "Get Management Console Notification", 
			dependsOnMethods = { "testAddRole" }, dataProvider = "SubscriptionPathDataProvider")
	public void testConsoleSubscription(String path) throws Exception {
		infoServiceAdminClient1 = new InfoServiceAdminClient(backendURL1,
				sessionID1);
		SubscriptionBean bean = infoServiceAdminClient1
				.subscribe(path, "work://RoleSubscriptionTest1",
						"ResourceUpdated", sessionID1);
		assertTrue(bean.getSubscriptionInstances() != null);
	}

	/**
	 * Add a comment to the resource
	 * 
	 * @param path resource path
	 * @throws AddAssociationRegistryExceptionException
	 * 
	 * @throws RemoteException
	 * @throws RegistryException
	 * @throws RegistryExceptionException
	 */
	@Test(groups = "wso2.greg", description = "Add comment", dependsOnMethods = "testConsoleSubscription", dataProvider = "SubscriptionPathDataProvider")
	public void testAddComment(String path) throws Exception {
		infoServiceAdminClient2.addComment("This is a comment", path,
				sessionID2);
		CommentBean cBean = infoServiceAdminClient2.getComments(path,
				sessionID2);
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
	 * Add a tag to the resource
	 * 
	 * @param path resource path
	 * 
	 * @throws RegistryException
	 * @throws AxisFault
	 * @throws RegistryExceptionException
	 */
	@Test(groups = "wso2.greg", description = "Add tag", dependsOnMethods = "testAddComment", dataProvider = "SubscriptionPathDataProvider")
	public void testAddTag(String path) throws RegistryException, AxisFault,
			RegistryExceptionException {
		infoServiceAdminClient2.addTag(TAG, path, sessionID2);
		String tag = infoServiceAdminClient2.getTags(path, sessionID2)
				.getTags()[0].getTagName();
		assertTrue(TAG.equalsIgnoreCase(tag), "Tags does not match");
	}

	/**
	 * Add ratings to the resource
	 * 
	 * @param path resource path
	 * 
	 * @throws RegistryException
	 * @throws RegistryExceptionException
	 */
	@Test(groups = "wso2.greg", description = "Add rating", dependsOnMethods = "testAddTag", dataProvider = "SubscriptionPathDataProvider")
	public void testAddRating(String path) throws RegistryException,
			RegistryExceptionException {
		infoServiceAdminClient2.rateResource("1", path, sessionID2);
		int userRating = infoServiceAdminClient2.getRatings(path, sessionID2)
				.getUserRating();
		assertTrue(userRating == 1, "Resource rating error");
	}

	/**
	 * Get notification for all resource updates including adding
	 * comments,tags and ratings
	 * @throws Exception 
	 */
	@Test(groups = "wso2.greg", description = "Get notifications", dataProvider = "SubscriptionPathDataProvider", dependsOnMethods = "testAddRating")
	public void testGetNotifications(String path) throws Exception {
		HumanTaskAdminClient humanTaskAdminClient = new HumanTaskAdminClient(
				backendURL2, sessionID2);

		boolean notiTag = false, notiRate = false, notiComment = false;
		Thread.sleep(1000);
		WorkItem[] workItems = WorkItemClient
				.getWorkItems(humanTaskAdminClient);
		for (WorkItem workItem : workItems) {
			if ("COMPLETED".equals(workItem.getStatus().toString())) {
				continue;
			}

			if (workItem
					.getPresentationSubject()
					.toString()
					.contains("The tag " + TAG + " was applied on resource "
									+ path)) {
				notiTag = true;
			} else if (workItem
					.getPresentationSubject()
					.toString()
					.contains("A rating of 1 was given to the resource at "
									+ path)) {
				notiRate = true;
			} else if (workItem
					.getPresentationSubject()
					.toString()
					.contains("A comment was added to the resource at " + path
									+ ". Comment: This is a comment")) {
				notiComment = true;
			}

			if ("RESERVED".equals(workItem.getStatus().toString())) {
				humanTaskAdminClient.completeTask(workItem.getId());
			}
		}
		workItems = null;
		assertTrue(notiTag && notiRate && notiComment);
	}

	@Test(groups = "wso2.greg", description = "Delete Subscription", dependsOnMethods = "testGetNotifications")
	public void testDeleteSubscription() throws RegistryException,
			RegistryExceptionException, RemoteException {
		SubscriptionBean sBean = infoServiceAdminClient2.getSubscriptions(
				GOV_COLLECTION_PATH + RESOURCE_NAME, sessionID1);
		if (sBean.getSubscriptionInstances() != null) {
			infoServiceAdminClient2.unsubscribe(GOV_COLLECTION_PATH
					+ RESOURCE_NAME,
					sBean.getSubscriptionInstances()[0].getId(), sessionID1);
		}
		sBean = infoServiceAdminClient2.getSubscriptions(GOV_COLLECTION_PATH
				+ RESOURCE_NAME, sessionID1);
		assertTrue(sBean.getSubscriptionInstances() == null);

	}

	/**
	 * Add a tag to a collection
	 * 
	 * @param path
	 *            collection path
	 * @throws RegistryException
	 * @throws AxisFault
	 * @throws RegistryExceptionException
	 */
	@Test(groups = "wso2.greg", description = "Add tag", dependsOnMethods = "testDeleteSubscription", dataProvider = "SubscriptionPathDataProvider")
	public void testAddTagWithNoSubscription(String path)
			throws RegistryException, AxisFault, RegistryExceptionException {
		infoServiceAdminClient1.addTag(TAG, path, sessionID1);
		String tag = infoServiceAdminClient1.getTags(path, sessionID1)
				.getTags()[0].getTagName();
		assertTrue(TAG.equalsIgnoreCase(tag), "Tags does not match");
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
	@Test(groups = "wso2.greg", description = "Get notifications", dataProvider = "SubscriptionPathDataProvider", dependsOnMethods = "testAddTagWithNoSubscription")
	public void testGetNotificationsWithNoSubscription(String path)
			throws RemoteException, AddAssociationRegistryExceptionException,
			InterruptedException, IllegalStateFault, IllegalAccessFault,
			IllegalArgumentFault {
		HumanTaskAdminClient humanTaskAdminClient = new HumanTaskAdminClient(
				backendURL1, sessionID1);
		boolean notiTag = false;
		Thread.sleep(1000);
		WorkItem[] workItems = WorkItemClient
				.getWorkItems(humanTaskAdminClient);
		for (WorkItem workItem : workItems) {
			if ("COMPLETED".equals(workItem.getStatus().toString())) {
				continue;
			}

			if (workItem
					.getPresentationSubject()
					.toString()
					.contains("The tag " + TAG + " was applied on resource "
									+ path)) {
				notiTag = true;
			}
		}
		workItems = null;
		assertTrue(!notiTag);
	}

	@AfterClass(alwaysRun = true)
	public void clean() throws Exception {
		resourceAdminServiceClient1.deleteResource(GOV_COLLECTION_PATH
				+ RESOURCE_NAME);
		infoServiceAdminClient1.rateResource("0", GOV_COLLECTION_PATH,
				sessionID1);
		infoServiceAdminClient1.removeComment("/;comments:1", sessionID1);
		infoServiceAdminClient1.removeTag(TAG, GOV_COLLECTION_PATH, sessionID1);
	}

	protected String getTestArtifactLocation() {
		return FrameworkPathUtil.getSystemResourceLocation();
	}
}
