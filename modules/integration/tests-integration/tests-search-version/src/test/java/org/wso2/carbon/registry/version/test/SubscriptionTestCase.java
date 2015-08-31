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

package org.wso2.carbon.registry.version.test;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.info.stub.beans.utils.xsd.SubscriptionInstance;
import org.wso2.carbon.registry.info.stub.beans.xsd.SubscriptionBean;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.beans.xsd.VersionPath;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.clients.InfoServiceAdminClient;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.clients.UserManagementClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import javax.activation.DataHandler;
import java.io.File;
import java.rmi.RemoteException;

import static junit.framework.Assert.*;


public class SubscriptionTestCase extends GREGIntegrationBaseTest {

    private ResourceAdminServiceClient resourceAdminClient;
    private static final String PATH1 = "/testResource";
    private static final String PATH2 = "/branch1/branch2/testResource2";
    private String COLLECTION_PATH_ROOT = "/";

    private InfoServiceAdminClient infoServiceAdminClient;
    private UserManagementClient userManagementClient;
    private WSRegistryServiceClient wsRegistryServiceClient;
    private String sessionCookie;
    private String backEndUrl;
    private String userName;
    private String userNameWithoutDomain;

    @BeforeClass(alwaysRun = true)
    public void initializeTests() throws Exception {


        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        backEndUrl = getBackendURL();
        sessionCookie = getSessionCookie();
        userName = automationContext.getContextTenant().getContextUser().getUserName();

        if (userName.contains("@"))
            userNameWithoutDomain = userName.substring(0, userName.indexOf('@'));
        else
            userNameWithoutDomain = userName;

        resourceAdminClient =
                new ResourceAdminServiceClient(backEndUrl,
                                               sessionCookie);
        infoServiceAdminClient = new InfoServiceAdminClient(backEndUrl,
                                                            sessionCookie);
        userManagementClient =
                new UserManagementClient(backEndUrl,
                                         sessionCookie);
        resourceAdminClient.addCollection(COLLECTION_PATH_ROOT, "dir11", "text/plain", "Desc1");

        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        wsRegistryServiceClient =
                registryProviderUtil.getWSRegistry(automationContext);


        testAddRole();

    }

    @Test(groups = {"wso2.greg"}, description = "Subscribe for a resource and version it")
    public void testSubscriptionVersioning() throws Exception {

        SubscriptionInstance[] sb1;
        SubscriptionInstance[] sb2;

        String path = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator
                      + "GREG" + File.separator + "testresource.txt";
        DataHandler dataHandler = new DataHandler(new java.net.URL("file:///" + path));
        resourceAdminClient.addResource(PATH1, "text/plain", "desc", dataHandler);
        assertTrue(resourceAdminClient.getResource(PATH1)[0].getAuthorUserName().contains(userNameWithoutDomain));
        //subscribe for the resource
        SubscriptionBean bean = testMgtConsoleResourceSubscription(PATH1, "ResourceUpdated");
        assertTrue(bean.getSubscriptionInstances() != null);
        sb1 = infoServiceAdminClient.getSubscriptions(PATH1, sessionCookie).getSubscriptionInstances();
        assertEquals(userNameWithoutDomain, sb1[0].getOwner());
        //create a checkpoint
        resourceAdminClient.createVersion(PATH1);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATH1);
        String verPath = vp1[0].getCompleteVersionPath();
        sb2 = infoServiceAdminClient.getSubscriptions(verPath, sessionCookie).getSubscriptionInstances();
        assertNull(sb2);
        assertNull(deleteVersion(PATH1));


    }

    @Test(groups = {"wso2.greg"}, description = "Subscribe for a resource and version it at leaf level")
    public void testSubsResourceLeaf() throws Exception {
        SubscriptionInstance[] sb1;
        SubscriptionInstance[] sb2;
        //create a resource at root level
        String path = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator
                      + "GREG" + File.separator + "testresource.txt";
        DataHandler dataHandler = new DataHandler(new java.net.URL("file:///" + path));
        resourceAdminClient.addResource(PATH2, "text/plain", "desc", dataHandler);
        assertTrue(resourceAdminClient.getResource(PATH2)[0].getAuthorUserName().contains(userNameWithoutDomain));
        //subscribe for the resource
        SubscriptionBean bean = testMgtConsoleResourceSubscription(PATH2, "ResourceUpdated");
        assertTrue(bean.getSubscriptionInstances() != null);
        sb1 = infoServiceAdminClient.getSubscriptions(PATH2, sessionCookie).getSubscriptionInstances();
        assertEquals(userNameWithoutDomain, sb1[0].getOwner());
        //create a checkpoint
        resourceAdminClient.createVersion(PATH2);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATH2);
        String verPath = vp1[0].getCompleteVersionPath();
        //see whethere the subscription is in version
        sb2 = infoServiceAdminClient.getSubscriptions(verPath, sessionCookie).getSubscriptionInstances();
        assertNull(sb2);
        assertNull(deleteVersion(PATH2));


    }

    @Test(groups = {"wso2.greg"}, description = "Create a collection with subscriptions, version and restore to the previous version ")
    public void testSubscriptionRestore() throws Exception {

        String PATH = COLLECTION_PATH_ROOT + "dir11";
        SubscriptionInstance[] sb1;
        SubscriptionInstance[] sb2;
        SubscriptionInstance[] sb3;


        //SUbscribe for a collection and creat a checkpoint
        SubscriptionBean bean = testMgtConsoleResourceSubscription(PATH, "CollectionUpdated");
        assertTrue(bean.getSubscriptionInstances() != null);
        sb1 = infoServiceAdminClient.getSubscriptions(PATH, sessionCookie).getSubscriptionInstances();
        assertEquals(userNameWithoutDomain, sb1[0].getOwner());
        resourceAdminClient.createVersion(PATH);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATH);
        String verPath = vp1[0].getCompleteVersionPath();
        sb3 = infoServiceAdminClient.getSubscriptions(verPath, sessionCookie).getSubscriptionInstances();

        assertNull(sb3);
        //unsubscribe from the subscription after versioning
        infoServiceAdminClient.unsubscribe(PATH, sb1[0].getId(), sessionCookie);
        assertNull(infoServiceAdminClient.getSubscriptions(PATH, sessionCookie).getSubscriptionInstances());

        //restore to previous version

        resourceAdminClient.restoreVersion(verPath);
        //Check the previous subscription is there
        sb2 = infoServiceAdminClient.getSubscriptions(PATH, sessionCookie).getSubscriptionInstances();

        assertNull(sb2);
        //delete created version
        assertNull(deleteVersion(PATH));


    }


    public SubscriptionBean testMgtConsoleResourceSubscription(String path, String updateType)
            throws RegistryException, RemoteException {
        return infoServiceAdminClient.subscribe(path, "work://SubscriptionTestRole", updateType,
                                                sessionCookie);
    }

    public void testAddRole() throws Exception {

        if (!userManagementClient.roleNameExists("SubscriptionTestRole1")) {
            userManagementClient.addRole("SubscriptionTestRole1", new String[]
                                         {userNameWithoutDomain}, new String[]{""});
            assertTrue(userManagementClient.roleNameExists("SubscriptionTestRole1"));
        }
    }


    public VersionPath[] deleteVersion(String path)
            throws ResourceAdminServiceExceptionException, RemoteException {
        int length = resourceAdminClient.getVersionPaths(path).length;
        for (int i = 0; i < length; i++) {
            long versionNo = resourceAdminClient.getVersionPaths(path)[0].getVersionNumber();
            String snapshotId = String.valueOf(versionNo);
            resourceAdminClient.deleteVersionHistory(path, snapshotId);
        }
        VersionPath[] vp2;
        vp2 = resourceAdminClient.getVersionPaths(path);

        return vp2;
    }

    @AfterClass
    public void clear() throws Exception {
        deleteResource(COLLECTION_PATH_ROOT + "dir11");
        deleteResource(PATH1);
        deleteResource("/branch1");
        userManagementClient.deleteRole("SubscriptionTestRole1");
        resourceAdminClient = null;
        infoServiceAdminClient = null;
        userManagementClient = null;

    }

    public void deleteResource(String path) throws RegistryException {
        if (wsRegistryServiceClient.resourceExists(path)) {
            wsRegistryServiceClient.delete(path);
        }
    }

}
