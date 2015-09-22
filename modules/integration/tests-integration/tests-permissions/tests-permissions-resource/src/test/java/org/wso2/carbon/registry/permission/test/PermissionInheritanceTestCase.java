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

package org.wso2.carbon.registry.permission.test;

import org.apache.axis2.AxisFault;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.FrameworkConstants;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.integration.common.admin.client.UserManagementClient;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.permission.test.utils.PermissionTestConstants;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceResourceServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.beans.xsd.PermissionBean;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;

import javax.activation.DataHandler;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

public class PermissionInheritanceTestCase extends GREGIntegrationBaseTest{

    private ResourceAdminServiceClient adminResourceAdminClient;
    private ResourceAdminServiceClient nonAdminResourceAdminClient;

    private static final String TEST_DIR_PATH = "/_system/config/";
    private static final String DENIED_DIR = "dirDenied";
    private static final String ALLOWED_DIR = "dirAllowed";

    private static final String TEST_RESOURCE_MEDIA_TYPE = "text/plain";

    private DataHandler dataHandler;
    private UserManagementClient userManagementClient;
    private String userName = "permissionUN1";

    private  String[] permissions = {"/permission/admin/configure/",
            "/permission/admin/login",
            "/permission/admin/manage/",
            "/permission/admin/monitor",
            "/permission/protected"};
    
    private String testCaseRole = "permissionInRole";


    @BeforeClass(alwaysRun = true)
    public void initialize() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String adminSession = getSessionCookie();


        adminResourceAdminClient =
                new ResourceAdminServiceClient(backendURL, adminSession);

        AutomationContext automationContextUser1 = new AutomationContext("GREG", "greg001",
                FrameworkConstants.SUPER_TENANT_KEY, userName);

        String sessionUser = new LoginLogoutClient(automationContextUser1).login();

        nonAdminResourceAdminClient =
                new ResourceAdminServiceClient(automationContextUser1.getContextUrls()
                        .getBackEndUrl(), sessionUser);

        //set up resources
        adminResourceAdminClient.addCollection(TEST_DIR_PATH, DENIED_DIR, "plain/text",
                                               "Test dir for deny permission inheritance");

        adminResourceAdminClient.addCollection(TEST_DIR_PATH, ALLOWED_DIR, "plain/text",
                                               "Test dir for allow permission inheritance");

        String resourcePath = getTestArtifactLocation() + "artifacts" + File.separator
                              + "GREG" + File.separator + "resource.txt";

        dataHandler = new DataHandler(new URL("file:///" + resourcePath));


        //Deny all permission to allow test collection
        adminResourceAdminClient.addResourcePermission(TEST_DIR_PATH + ALLOWED_DIR,
                                                       testCaseRole,
                                                       PermissionTestConstants.READ_ACTION,
                                                       PermissionTestConstants.PERMISSION_DISABLED);

        adminResourceAdminClient.addResourcePermission(TEST_DIR_PATH + ALLOWED_DIR,
                                                       PermissionTestConstants.EVERYONE_ROLE,
                                                       PermissionTestConstants.READ_ACTION,
                                                       PermissionTestConstants.PERMISSION_DISABLED);

        adminResourceAdminClient.addResourcePermission(TEST_DIR_PATH + ALLOWED_DIR,
                                                       testCaseRole,
                                                       PermissionTestConstants.WRITE_ACTION,
                                                       PermissionTestConstants.PERMISSION_DISABLED);

        adminResourceAdminClient.addResourcePermission(TEST_DIR_PATH + ALLOWED_DIR,
                                                       testCaseRole,
                                                       PermissionTestConstants.DELETE_ACTION,
                                                       PermissionTestConstants.PERMISSION_DISABLED);

        adminResourceAdminClient.addResourcePermission(TEST_DIR_PATH + ALLOWED_DIR,
                                                       testCaseRole,
                                                       PermissionTestConstants.AUTHORIZE_ACTION,
                                                       PermissionTestConstants.PERMISSION_DISABLED);

        userManagementClient = new UserManagementClient(backendURL, adminSession);

        userManagementClient.updateUserListOfRole(FrameworkConstants.ADMIN_ROLE, null,
                new String[] {userName} ); //remove user from admin role

        userManagementClient.addRole(testCaseRole, new String[] {userName}, permissions);

        //Add  testuser2 to testRole.
//        userManagementClient.updateUserListOfRole(testCaseRole,
//                new String [] {userName}, null);
    }

    @Test(groups = "wso2.greg", description = "Test read access inheritance")
    public void testDenyReadPermission() throws ResourceAdminServiceExceptionException, RemoteException,
                   ResourceAdminServiceResourceServiceExceptionException, MalformedURLException,
                   RegistryException {
        //deny read permission
        adminResourceAdminClient.addResourcePermission(TEST_DIR_PATH + DENIED_DIR,
                                                       testCaseRole,
                                                       PermissionTestConstants.READ_ACTION,
                                                       PermissionTestConstants.PERMISSION_DISABLED);

        adminResourceAdminClient.addResourcePermission(TEST_DIR_PATH + DENIED_DIR,
                                                       PermissionTestConstants.EVERYONE_ROLE,
                                                       PermissionTestConstants.READ_ACTION,
                                                       PermissionTestConstants.PERMISSION_DISABLED);

        //add resource
        adminResourceAdminClient.addResource(TEST_DIR_PATH + DENIED_DIR + "/denyRead.txt",
                                             TEST_RESOURCE_MEDIA_TYPE,
                                             "", dataHandler);

        //test access
        Assert.assertNull(nonAdminResourceAdminClient.getResource(TEST_DIR_PATH + DENIED_DIR + "/denyRead.txt"));
    }

    @Test(groups = "wso2.greg", description = "Test write access inheritance",
            expectedExceptions = AxisFault.class, dependsOnMethods = "testDenyReadPermission")
    public void testDenyWritePermission() throws ResourceAdminServiceExceptionException,
            RemoteException, MalformedURLException, ResourceAdminServiceResourceServiceExceptionException {
        //create a new collection in the test directory and disable permission at root
        adminResourceAdminClient.addCollection(TEST_DIR_PATH + DENIED_DIR, "testdir", "text/plain", "");
        adminResourceAdminClient.addResourcePermission(TEST_DIR_PATH + DENIED_DIR,
                                                       testCaseRole,
                                                       PermissionTestConstants.WRITE_ACTION,
                                                       PermissionTestConstants.PERMISSION_DISABLED);

        nonAdminResourceAdminClient.addResource(TEST_DIR_PATH + DENIED_DIR + "/testdir/denywrite.txt",
                                                "text/plain", "", dataHandler);
    }

    @Test(groups = "wso2.greg", description = "Test delete access inheritance",
          expectedExceptions = AxisFault.class, dependsOnMethods = "testDenyWritePermission")
    public void testDenyDeletePermission()
            throws ResourceAdminServiceExceptionException, RemoteException,
                   ResourceAdminServiceResourceServiceExceptionException {
        //deny permission
        adminResourceAdminClient.addResourcePermission(TEST_DIR_PATH + DENIED_DIR,
                                                       testCaseRole, 
                                                       PermissionTestConstants.DELETE_ACTION,
                                                       PermissionTestConstants.PERMISSION_DISABLED);
        
        adminResourceAdminClient.addResourcePermission(TEST_DIR_PATH + DENIED_DIR,
                                                       testCaseRole, 
                                                       PermissionTestConstants.READ_ACTION,
                                                       PermissionTestConstants.PERMISSION_ENABLED);

        adminResourceAdminClient.addResource(TEST_DIR_PATH + DENIED_DIR + "/denyDelete.txt",
                                             TEST_RESOURCE_MEDIA_TYPE, "", dataHandler);

        //try to delete the resource
        nonAdminResourceAdminClient.deleteResource(TEST_DIR_PATH + DENIED_DIR + "/denyDelete.txt");

    }

    //fails due to REGISTRY-1185
    @Test(groups = "wso2.greg", description = "Test authorization access inheritance",
          expectedExceptions = ResourceAdminServiceResourceServiceExceptionException.class, dependsOnMethods = "testDenyDeletePermission")
    public void testDenyAuthPermission() throws Exception {

        //Deny permission
        adminResourceAdminClient.addResourcePermission(TEST_DIR_PATH + DENIED_DIR,
                                                       testCaseRole, 
                                                       PermissionTestConstants.AUTHORIZE_ACTION,
                                                       PermissionTestConstants.PERMISSION_DISABLED);

        //add a test resource
        adminResourceAdminClient.addResource(TEST_DIR_PATH + DENIED_DIR + "/denyAuth.txt",
                                             TEST_RESOURCE_MEDIA_TYPE, "", dataHandler);


        //try to change permission as non admin
        nonAdminResourceAdminClient.addResourcePermission(TEST_DIR_PATH + DENIED_DIR + "/denyAuth.txt",
                                                          testCaseRole, 
                                                          PermissionTestConstants.AUTHORIZE_ACTION,
                                                          PermissionTestConstants.PERMISSION_ENABLED);
    }

    @Test(groups = "wso2.greg", description = "Test read access inheritance", dependsOnMethods = "testDenyAuthPermission")
    public void testAllowReadPermission()
            throws ResourceAdminServiceExceptionException, RemoteException,
                   ResourceAdminServiceResourceServiceExceptionException, MalformedURLException,
                   RegistryException {
        //allow denied read permission
        adminResourceAdminClient.addResourcePermission(TEST_DIR_PATH + ALLOWED_DIR,
                                                       testCaseRole,
                                                       PermissionTestConstants.READ_ACTION,
                                                       PermissionTestConstants.PERMISSION_ENABLED);

        adminResourceAdminClient.addResource(TEST_DIR_PATH + ALLOWED_DIR + "/allowRead.txt",
                                             TEST_RESOURCE_MEDIA_TYPE, "", dataHandler);

        //test access
        Assert.assertNotNull(nonAdminResourceAdminClient.getResource(TEST_DIR_PATH + ALLOWED_DIR + "/allowRead.txt"));
    }

    @Test(groups = "wso2.greg", description = "Test write access inheritance", dependsOnMethods = "testAllowReadPermission")
    public void testAllowWritePermission()
            throws ResourceAdminServiceExceptionException, RemoteException, MalformedURLException,
                   ResourceAdminServiceResourceServiceExceptionException {
        //create a new collection in the test directory and disable permission at root
        adminResourceAdminClient.addCollection(TEST_DIR_PATH + ALLOWED_DIR, "testdir", "text/plain", "");

        adminResourceAdminClient.addResourcePermission(TEST_DIR_PATH + ALLOWED_DIR,
                                                       testCaseRole,
                                                       PermissionTestConstants.WRITE_ACTION,
                                                       PermissionTestConstants.PERMISSION_ENABLED);

        adminResourceAdminClient.addResourcePermission(TEST_DIR_PATH + ALLOWED_DIR,
                                                       testCaseRole,
                                                       PermissionTestConstants.READ_ACTION,
                                                       PermissionTestConstants.PERMISSION_ENABLED);

        //try to add a new resource to the new sub collection
        nonAdminResourceAdminClient.addResource(TEST_DIR_PATH + ALLOWED_DIR + "/testdir/allowWrite.txt",
                                                TEST_RESOURCE_MEDIA_TYPE, "", dataHandler);
        Assert.assertNotNull(adminResourceAdminClient.getResource(TEST_DIR_PATH + ALLOWED_DIR + "/testdir/allowWrite" +
                                                                  ".txt"));
    }

    @Test(groups = "wso2.greg", expectedExceptions = AxisFault.class, description = "Test delete access inheritance", dependsOnMethods = "testAllowWritePermission")
    public void testAllowDeletePermission()
            throws ResourceAdminServiceExceptionException, RemoteException,
                   ResourceAdminServiceResourceServiceExceptionException, MalformedURLException {

        //allow denied permission
        adminResourceAdminClient.addResourcePermission(TEST_DIR_PATH + ALLOWED_DIR,
                                                       testCaseRole,
                                                       PermissionTestConstants.DELETE_ACTION,
                                                       PermissionTestConstants.PERMISSION_ENABLED);

        adminResourceAdminClient.addResourcePermission(TEST_DIR_PATH + ALLOWED_DIR,
                                                       testCaseRole,
                                                       PermissionTestConstants.READ_ACTION,
                                                       PermissionTestConstants.PERMISSION_ENABLED);

        adminResourceAdminClient.addResource(TEST_DIR_PATH + ALLOWED_DIR + "/allowDelete.txt",
                                             TEST_RESOURCE_MEDIA_TYPE, "", dataHandler);

        //try to delete
        nonAdminResourceAdminClient.deleteResource(TEST_DIR_PATH + ALLOWED_DIR + "/allowDelete.txt");

        //try to get it, gives an exception
        adminResourceAdminClient.getResource(TEST_DIR_PATH + ALLOWED_DIR + "/allowDelete.txt");
    }

    @Test(groups = "wso2.greg", description = "Test authorization access inheritance", dependsOnMethods = "testAllowDeletePermission")
    public void testAllowAuthPermission() throws Exception {
        //Allow denied permission
        adminResourceAdminClient.addResourcePermission(TEST_DIR_PATH + ALLOWED_DIR,
                                                       testCaseRole,
                                                       PermissionTestConstants.AUTHORIZE_ACTION,
                                                       PermissionTestConstants.PERMISSION_ENABLED);

        adminResourceAdminClient.addResource(TEST_DIR_PATH + ALLOWED_DIR + "/allowAuth.txt",
                TEST_RESOURCE_MEDIA_TYPE, "", dataHandler);

        PermissionBean permissionBean = nonAdminResourceAdminClient.getPermission(TEST_DIR_PATH + ALLOWED_DIR +
                                                                                  "/allowAuth.txt");
        Assert.assertTrue(permissionBean.getAuthorizeAllowed());
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() throws Exception {
        if (adminResourceAdminClient.getResource(TEST_DIR_PATH + ALLOWED_DIR) != null) {
            adminResourceAdminClient.deleteResource(TEST_DIR_PATH + ALLOWED_DIR);
        }
        if (adminResourceAdminClient.getResource(TEST_DIR_PATH + DENIED_DIR) != null) {
            adminResourceAdminClient.deleteResource(TEST_DIR_PATH + DENIED_DIR);
        }

        if(userManagementClient.roleNameExists(testCaseRole)){
            userManagementClient.deleteRole(testCaseRole);
        }

        adminResourceAdminClient = null;
        nonAdminResourceAdminClient = null;
        dataHandler = null;
    }
}
