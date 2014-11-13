/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.subscription.test;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.registry.info.stub.beans.xsd.SubscriptionBean;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.greg.integration.common.clients.HumanTaskAdminClient;
import org.wso2.greg.integration.common.clients.InfoServiceAdminClient;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.clients.WorkItem;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.subscription.ManagementConsoleSubscription;
import org.wso2.greg.integration.common.utils.subscription.WorkItemClient;

import javax.activation.DataHandler;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;

public class DeleteSubscriptionTestCase extends GREGIntegrationBaseTest {

    private ResourceAdminServiceClient resourceAdminServiceClient;
    private InfoServiceAdminClient infoServiceAdminClient;
    private String sessionID;

    private static final String RESOURCE_PATH_NAME = "/";
    private static final String WSDL_PATH = "/_system/governance/trunk/wsdls/com/amazon/soap/1.0.0/AmazonWebServices.wsdl";
    private static final String SCHEMA_PATH = "/_system/governance/trunk/schemas/org/company/www/1.0.0/Person.xsd";
    private static final String TAG = "testDeleteTag";
    private String userNameWithoutDomain;

    @BeforeClass(alwaysRun = true)
    public void initialize() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        sessionID = getSessionCookie();
        resourceAdminServiceClient = new ResourceAdminServiceClient(backendURL, sessionID);
        infoServiceAdminClient = new InfoServiceAdminClient(backendURL, sessionID);

        String userName = automationContext.getContextTenant().getContextUser().getUserName();
        if (userName.contains("@")){
            userNameWithoutDomain = userName.substring(0, userName.indexOf('@'));
        }
        else {
            userNameWithoutDomain = userName;
        }
    }

    @DataProvider(name = "ResourceDataProvider")
    public Object[][] dp() {
        return new Object[][]{new Object[]{"service.metadata.xml", "application/xml", "services"},
                new Object[]{"policy.xml", "application/xml", "policy"}, new Object[]{"test.map",
                "Unknown", "mediatypes"}, new Object[]{"AmazonWebServices.wsdl", "application/wsdl+xml", "wsdl"},
                new Object[]{"Person.xsd", "application/x-xsd+xml", "schema"},};
    }

    /**
     * @param name   resource name
     * @param type   resource type
     * @param folder folder where the resource exists
     * @throws MalformedURLException
     * @throws RemoteException
     * @throws ResourceAdminServiceExceptionException
     *
     */
    @Test(groups = "wso2.greg", description = "Add resource", dataProvider = "ResourceDataProvider")
    public void testAddResource(String name, String type, String folder) throws MalformedURLException,
            RemoteException, ResourceAdminServiceExceptionException {
        String resourcePath = getTestArtifactLocation() + "artifacts" +
                File.separator + "GREG" + File.separator + folder +
                File.separator + name;
        DataHandler dh = new DataHandler(new URL("file:///" + resourcePath));
        resourceAdminServiceClient.addResource(RESOURCE_PATH_NAME + name, type, "testDesc", dh);

        if(!(folder.equals("wsdl") || folder.equals("schema"))) {
            assertTrue(resourceAdminServiceClient.getResource(RESOURCE_PATH_NAME +
                    name)[0].getAuthorUserName().contains(userNameWithoutDomain));
        } else if(folder.equals("wsdl")) {
            assertTrue(resourceAdminServiceClient.getResource(WSDL_PATH)[0]
                    .getAuthorUserName().contains(userNameWithoutDomain));
        } else {
            assertTrue(resourceAdminServiceClient.getResource(SCHEMA_PATH)[0]
                    .getAuthorUserName().contains(userNameWithoutDomain));
        }
    }

    /**
     * Add subscription to a resource and send notification via Management
     * Console
     *
     * @param name   resource name
     * @param type   resource type
     * @param folder folder where the resource exists
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Get Management Console Notification for resources",
            dataProvider = "ResourceDataProvider", dependsOnMethods = "testAddResource")
    public void testConsoleSubscription(String name, String type, String folder) throws Exception {
        if(!(folder.equals("wsdl") || folder.equals("schema"))) {
            assertTrue(ManagementConsoleSubscription.init(RESOURCE_PATH_NAME + name,
                    "ResourceUpdated", automationContext));
        } else if(folder.equals("wsdl")) {
            assertTrue(ManagementConsoleSubscription.init(WSDL_PATH, "ResourceUpdated", automationContext));
        } else {
            assertTrue(ManagementConsoleSubscription.init(SCHEMA_PATH, "ResourceUpdated", automationContext));
        }
    }

    /**
     * delete subscription of a resource
     *
     * @param name   resource name
     * @param type   resource type
     * @param folder folder where the resource exists
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "unsubscribe", dataProvider = "ResourceDataProvider",
            dependsOnMethods = {"testConsoleSubscription"})
    public void testDeleteSubscription(String name, String type, String folder) throws Exception {
        InfoServiceAdminClient infoServiceAdminClient = new InfoServiceAdminClient(backendURL, sessionID);
        SubscriptionBean sBean = null;
        if(!(folder.equals("wsdl") || folder.equals("schema"))) {
            sBean = infoServiceAdminClient.getSubscriptions(RESOURCE_PATH_NAME + name, sessionID);
            infoServiceAdminClient.unsubscribe(RESOURCE_PATH_NAME + name, sBean.getSubscriptionInstances()[0].getId(), sessionID);
            sBean = infoServiceAdminClient.getSubscriptions(RESOURCE_PATH_NAME + name, sessionID);
        } else if(folder.equals("wsdl")) {
            sBean = infoServiceAdminClient.getSubscriptions(WSDL_PATH, sessionID);
            infoServiceAdminClient.unsubscribe(WSDL_PATH, sBean.getSubscriptionInstances()[0].getId(), sessionID);
            sBean = infoServiceAdminClient.getSubscriptions(WSDL_PATH, sessionID);
        } else {
            sBean = infoServiceAdminClient.getSubscriptions(SCHEMA_PATH, sessionID);
            infoServiceAdminClient.unsubscribe(SCHEMA_PATH, sBean.getSubscriptionInstances()[0].getId(), sessionID);
            sBean = infoServiceAdminClient.getSubscriptions(SCHEMA_PATH, sessionID);
        }
        assertTrue(sBean.getSubscriptionInstances() == null);
    }

    /**
     * add tag to resources
     *
     * @param name
     * @param type   resource type
     * @param folder folder where the resource exists
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "add tag", dataProvider = "ResourceDataProvider",
            dependsOnMethods = {"testDeleteSubscription"})
    public void testAddTag(String name, String type, String folder) throws Exception {
        String tag = null;
        if(!(folder.equals("wsdl") || folder.equals("schema"))) {
            infoServiceAdminClient.addTag(TAG, RESOURCE_PATH_NAME + name, sessionID);
            tag = infoServiceAdminClient.getTags(RESOURCE_PATH_NAME + name, sessionID).getTags()[0].getTagName();
        } else if(folder.equals("wsdl")) {
            infoServiceAdminClient.addTag(TAG, WSDL_PATH, sessionID);
            tag = infoServiceAdminClient.getTags(WSDL_PATH, sessionID).getTags()[0].getTagName();
        } else {
            infoServiceAdminClient.addTag(TAG, SCHEMA_PATH, sessionID);
            tag = infoServiceAdminClient.getTags(SCHEMA_PATH, sessionID).getTags()[0].getTagName();
        }
        assertTrue(TAG.equalsIgnoreCase(tag), "Tags does not match");
    }

    /**
     * check whether notifications are generated even after subscription is
     * deleted
     * <p/>
     * name
     *
     * @param name
     * @param type   resource type
     * @param folder folder where the resource exists
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "check for notification tag", dataProvider = "ResourceDataProvider",
            dependsOnMethods = {"testAddTag"})
    public void testCheckNotifications(String name, String type, String folder) throws Exception {
        boolean success = false;
        HumanTaskAdminClient humanTaskAdminClient = new HumanTaskAdminClient(backendURL, sessionID);
        Thread.sleep(1000);
        WorkItem[] workItems = WorkItemClient.getWorkItems(humanTaskAdminClient);
        for(WorkItem workItem : workItems) {
            if(workItem.getPresentationSubject().toString().contains("The tag " + TAG + " was applied on resource " +
                    RESOURCE_PATH_NAME + name) ||
                    workItem.getPresentationSubject().toString().contains("The tag " + TAG +
                            " was applied on resource " + WSDL_PATH) ||
                    workItem.getPresentationSubject().toString().contains("The tag " + TAG +
                            " was applied on resource " + SCHEMA_PATH)) {
                success = true;
                break;
            }
        }
        workItems = null;
        assertTrue(!success);
    }

    /**
     * Add subscription to a collection and send notification via Management
     * Console
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Get Management Console Notification for Collection",
            dependsOnMethods = "testCheckNotifications")
    public void testCollectionConsoleSubscription() throws Exception {
        assertTrue(ManagementConsoleSubscription.init(RESOURCE_PATH_NAME, "CollectionUpdated",
                automationContext));
    }

    /**
     * delete subscription of a collection
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "unsubscribe", dependsOnMethods = "testCollectionConsoleSubscription")
    public void testDeleteCollectionSubscription() throws Exception {
        InfoServiceAdminClient infoServiceAdminClient = new InfoServiceAdminClient(backendURL, sessionID);
        String sessionID = getSessionCookie();
        SubscriptionBean sBean = infoServiceAdminClient.getSubscriptions(RESOURCE_PATH_NAME, sessionID);
        infoServiceAdminClient.unsubscribe(RESOURCE_PATH_NAME, sBean.getSubscriptionInstances()[0].getId(), sessionID);
        sBean = infoServiceAdminClient.getSubscriptions(RESOURCE_PATH_NAME, sessionID);
        assertTrue(sBean.getSubscriptionInstances() == null);
    }

    @AfterClass(alwaysRun = true)
    public void clean() throws RemoteException, ResourceAdminServiceExceptionException {
        resourceAdminServiceClient.deleteResource(RESOURCE_PATH_NAME + "service.metadata.xml");
        resourceAdminServiceClient.deleteResource(RESOURCE_PATH_NAME + "policy.xml");
        resourceAdminServiceClient.deleteResource(SCHEMA_PATH);
        resourceAdminServiceClient.deleteResource(RESOURCE_PATH_NAME + "test.map");
        resourceAdminServiceClient.deleteResource(WSDL_PATH);
        resourceAdminServiceClient.deleteResource("/_system/governance/trunk/services/com/amazon/soap/1.0.0/AmazonSearchService");
    }
}
