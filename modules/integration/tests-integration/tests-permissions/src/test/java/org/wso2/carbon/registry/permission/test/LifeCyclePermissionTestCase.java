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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.FrameworkConstants;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.test.utils.common.FileManager;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.integration.common.admin.client.UserManagementClient;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.registry.permission.test.utils.PermissionTestConstants;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.greg.integration.common.clients.LifeCycleManagementClient;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.GREGTestConstants;

import javax.activation.DataHandler;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public  class LifeCyclePermissionTestCase extends GREGIntegrationBaseTest{

    private static final String[] PERMISSION_LIST_ENABLED = {
            "/permission/admin/login",
            "/permission/admin/configure/governance",
            "/permission/admin/configure/governance/api-ui",
            "/permission/admin/configure/governance/generic",
            "/permission/admin/configure/governance/lifecycles",
            "/permission/admin/configure/governance/service-ui",
            "/permission/admin/configure/governance/uri-ui",
            "/permission/admin/manage/resources",
            "/permission/admin/manage/resources/associations",
            "/permission/admin/manage/resources/browse",
            "/permission/admin/manage/resources/community-features",
            "/permission/admin/manage/resources/govern",
            "/permission/admin/manage/resources/govern/api",
            "/permission/admin/manage/resources/govern/api/add",
            "/permission/admin/manage/resources/govern/api/list",
            "/permission/admin/manage/resources/govern/generic",
            "/permission/admin/manage/resources/govern/generic/add",
            "/permission/admin/manage/resources/govern/generic/list",
            "/permission/admin/manage/resources/govern/impactanalysis",
            "/permission/admin/manage/resources/govern/lifecycles",
            "/permission/admin/manage/resources/govern/lifecyclestagemonitor",
            "/permission/admin/manage/resources/govern/metadata",
            "/permission/admin/manage/resources/govern/generic/add",
            "/permission/admin/manage/resources/govern/service/add",
            "/permission/admin/manage/resources/govern/wsdl/add",
            "/permission/admin/manage/resources/govern/schema/add",
            "/permission/admin/manage/resources/govern/policy/add",
            "/permission/admin/manage/resources/govern/generic/list",
            "/permission/admin/manage/resources/govern/service/list",
            "/permission/admin/manage/resources/govern/wsdl/list",
            "/permission/admin/manage/resources/govern/schema/list",
            "/permission/admin/manage/resources/govern/policy/list",
            "/permission/admin/manage/resources/govern/resourceimpact",
            "/permission/admin/manage/resources/govern/uri",
            "/permission/admin/manage/resources/govern/uri/add",
            "/permission/admin/manage/resources/govern/uri/list",
            "/permission/admin/manage/resources/notifications",
            "/permission/admin/manage/resources/ws-api",
    };
    private static final String[] PERMISSION_LIST_DISABLED = {
            "/permission/admin/login",
    };

    private static String[] ENABLED_USERS;
    private static String[] DISABLED_USERS;
    private static final String ENABLED_ROLE = "enabledRole";
    private static final String DISABLED_ROLE = "disabledRole";

    private static final String SERVICE_NAME = "IntergalacticServiceNoWSDL";
    private static final String SERVICE_NAMESPACE = "com.abb";
    private static final String SERVICES_PATH = "/_system/governance/trunk/services";
    public static final String PERMISSION_PATH = "/_system/governance/trunk";
    private static final String LIFECYCLE_NAME = "CheckListLC";

    private static UserManagementClient userManagementClient;
    private LifeCycleManagementClient permittedLifeCycleManagementClient;
    private LifeCycleManagementClient deniedLifeCycleManagementClient;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private ResourceAdminServiceClient permittedResourceAdminServiceClient;
    private ResourceAdminServiceClient denideResourceAdminServiceClient;


    @BeforeClass(alwaysRun = true)
    public void initialize() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String sessionCookie = getSessionCookie();

        ENABLED_USERS = new String [] {"enabledUser"};
        DISABLED_USERS = new String [] {"disabledUser"};

        resourceAdminServiceClient = new ResourceAdminServiceClient(backendURL, sessionCookie);


        //setup two roles, one with permission allowed and one with permission denied
        userManagementClient = new UserManagementClient(backendURL, sessionCookie);

        userManagementClient.updateUserListOfRole(FrameworkConstants.ADMIN_ROLE, null,
                ENABLED_USERS); //remove enabled users for admin role

        userManagementClient.updateUserListOfRole(FrameworkConstants.ADMIN_ROLE, null,
                DISABLED_USERS);// //remove disabled users for admin role

        userManagementClient.addRole(ENABLED_ROLE, ENABLED_USERS, PERMISSION_LIST_ENABLED);

        userManagementClient.addRole(DISABLED_ROLE, DISABLED_USERS, PERMISSION_LIST_DISABLED);

        userManagementClient.addRole(PermissionTestConstants.NON_ADMIN_TEST_ROLE, null, null);

        userManagementClient.updateUserListOfRole(PermissionTestConstants.NON_ADMIN_TEST_ROLE, new String[]{},
                DISABLED_USERS);

        userManagementClient.updateUserListOfRole(PermissionTestConstants.NON_ADMIN_TEST_ROLE, new String[]{},
                ENABLED_USERS);

        AutomationContext automationContextDisabledUser =
                new AutomationContext("GREG", "greg001", GREGTestConstants.SUPER_TENANT_DOMAIN_KEY,
                        DISABLED_USERS[0]);

        AutomationContext automationContextEnabledUser =
                new AutomationContext("GREG", "greg001", GREGTestConstants.SUPER_TENANT_DOMAIN_KEY,
                        ENABLED_USERS[0]);

        //initialize clients

        String userSessionCookieDisabledUser = new LoginLogoutClient(automationContextDisabledUser).login();
        String userSessionCookieEnabledUser = new LoginLogoutClient(automationContextEnabledUser).login();

        permittedLifeCycleManagementClient = new LifeCycleManagementClient(automationContextEnabledUser
                .getContextUrls().getBackEndUrl(),userSessionCookieEnabledUser);

        deniedLifeCycleManagementClient = new LifeCycleManagementClient(automationContextDisabledUser
                .getContextUrls().getBackEndUrl(), userSessionCookieDisabledUser);

        denideResourceAdminServiceClient = new ResourceAdminServiceClient(automationContextDisabledUser
                .getContextUrls().getBackEndUrl(), userSessionCookieDisabledUser);

        permittedResourceAdminServiceClient = new ResourceAdminServiceClient(automationContextDisabledUser
                .getContextUrls().getBackEndUrl(), userSessionCookieEnabledUser );

        String serviceConfigPath =
                getTestArtifactLocation() + "artifacts" +  File.separator + "GREG" + File.separator + "services" +
                File.separator + "defaultService.xml";

        DataHandler dataHandler = new DataHandler(new URL("file:///" + serviceConfigPath));
        String mediaType = "plain/text";
        String description = "This is a test service";
        resourceAdminServiceClient.addResource(
                "/_system/governance/repository/components/org.wso2.carbon.governance/configuration/services/service",
                mediaType, description, dataHandler);
    }

    @Test(groups = "wso2.greg", description = "Test permission to allow create a service",
          expectedExceptions = AxisFault.class, dependsOnMethods = "testCreateNewLifeCycle")
    public void testDenyCreateService() throws ResourceAdminServiceExceptionException, MalformedURLException, RemoteException {

        String servicePath = getTestArtifactLocation() + "artifacts" +
                             File.separator + "GREG" + File.separator + "services" + File.separator +
                             "intergalacticService_no_wsdl.metadata.xml";

        DataHandler dataHandler = new DataHandler(new URL("file:///" + servicePath));
        String mediaType = "application/vnd.wso2-service+xml";
        String description = "This is a test service";
        denideResourceAdminServiceClient.addResource("/_system/governance/service1", mediaType, description, dataHandler);
    }

    @Test(groups = "wso2.greg", description = "Test permission to create new life cycle",
          expectedExceptions = AxisFault.class, dependsOnMethods = "testDenyCreateService")
    public void testDenyCreateNewLifeCycle()
            throws LifeCycleManagementServiceExceptionException, IOException, InterruptedException {
        String resourcePath = getTestArtifactLocation() + "artifacts" +
                              File.separator + "GREG" + File.separator + "lifecycle" + File.separator +
                              "CheckItemTickedValidatorLifeCycle.xml";
        String lifeCycleContent = FileManager.readFile(resourcePath);
        deniedLifeCycleManagementClient.addLifeCycle(lifeCycleContent);
    }

    @Test(groups = "wso2.greg", description = "Test allowing a logged in user to view lifecycles",
           dependsOnMethods = "testDenyCreateNewLifeCycle")
    public void testAllowListLifeCycles()
            throws LifeCycleManagementServiceExceptionException, RemoteException {
        String[] lifeCycles = deniedLifeCycleManagementClient.getLifecycleList();
        assertEquals(lifeCycles.length,2,"Cannot view lifecycles for a logged in user with restricted permission");
    }

    @Test(groups = "wso2.greg", description = "Test permission to allow create a service")
    public void testAllowCreateServices() throws Exception{

        ResourceAdminServiceClient resourceAdminServiceClient =
                new ResourceAdminServiceClient(automationContext.getContextUrls().getBackEndUrl(),
                        new LoginLogoutClient(automationContext).login());

        resourceAdminServiceClient.getPermission(PERMISSION_PATH);

        resourceAdminServiceClient.addResourcePermission(PERMISSION_PATH, ENABLED_ROLE,
                PermissionTestConstants.READ_ACTION,
                PermissionTestConstants.PERMISSION_ENABLED);

        resourceAdminServiceClient.addResourcePermission(PERMISSION_PATH, ENABLED_ROLE,
                PermissionTestConstants.WRITE_ACTION,
                PermissionTestConstants.PERMISSION_ENABLED);

        resourceAdminServiceClient.addResourcePermission(PERMISSION_PATH, ENABLED_ROLE,
                PermissionTestConstants.DELETE_ACTION,
                PermissionTestConstants.PERMISSION_ENABLED);

        resourceAdminServiceClient.addResourcePermission(PERMISSION_PATH, ENABLED_ROLE,
                PermissionTestConstants.AUTHORIZE_ACTION,
                PermissionTestConstants.PERMISSION_ENABLED);

        String servicePath = getTestArtifactLocation() + "artifacts" +
                             File.separator + "GREG" + File.separator + "services" + File.separator +
                             "intergalacticService.metadata.xml";

        DataHandler dataHandler = new DataHandler(new URL("file:///" + servicePath));
        String mediaType = "application/vnd.wso2-service+xml";
        String description = "This is a test service";
        permittedResourceAdminServiceClient.addResource("/_system/governance/service1" , mediaType, description, dataHandler);
    }

    @Test(groups = "wso2.greg", description = "Test permission to create new life cycle",
          dependsOnMethods = "testAllowCreateServices")
    public void testCreateNewLifeCycle()
            throws LifeCycleManagementServiceExceptionException, IOException, InterruptedException {
        String resourcePath = getTestArtifactLocation() + "artifacts" +
                              File.separator + "GREG" + File.separator + "lifecycle" + File.separator +
                              "CheckItemTickedValidatorLifeCycle.xml";
        String lifeCycleContent = FileManager.readFile(resourcePath);
        permittedLifeCycleManagementClient.addLifeCycle(lifeCycleContent);
        String[] lifeCycles = permittedLifeCycleManagementClient.getLifecycleList();

        boolean lcStatus = false;
        for (String lc : lifeCycles) {

            if (lc.equalsIgnoreCase(LIFECYCLE_NAME)) {
                lcStatus = true;
            }
        }
        assertTrue(lcStatus);
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() throws Exception {
        try {
            String servicePathToDelete = SERVICES_PATH + "/" + SERVICE_NAMESPACE + "/" + SERVICE_NAME;
            resourceAdminServiceClient.deleteResource(servicePathToDelete);
            permittedLifeCycleManagementClient.deleteLifeCycle(LIFECYCLE_NAME);
        } catch (Exception e) {
        }
        userManagementClient.updateUserListOfRole(PermissionTestConstants.NON_ADMIN_TEST_ROLE, DISABLED_USERS, new String[]{});
        userManagementClient.updateUserListOfRole(PermissionTestConstants.NON_ADMIN_TEST_ROLE, ENABLED_USERS, new String[]{});
        userManagementClient.deleteRole(PermissionTestConstants.NON_ADMIN_TEST_ROLE);
        userManagementClient.deleteRole(ENABLED_ROLE);
        userManagementClient.deleteRole(DISABLED_ROLE);


        userManagementClient = null;
        permittedLifeCycleManagementClient = null;
        deniedLifeCycleManagementClient = null;
        resourceAdminServiceClient = null;
    }
}
