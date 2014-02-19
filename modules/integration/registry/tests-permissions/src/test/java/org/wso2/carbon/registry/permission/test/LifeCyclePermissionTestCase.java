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
import org.wso2.carbon.automation.api.clients.governance.GovernanceServiceClient;
import org.wso2.carbon.automation.api.clients.governance.LifeCycleManagementClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.api.clients.user.mgt.UserManagementClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.core.utils.fileutils.FileManager;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.governance.list.stub.ListMetadataServiceRegistryExceptionException;
import org.wso2.carbon.governance.services.stub.AddServicesServiceRegistryExceptionException;
import org.wso2.carbon.registry.permission.test.utils.PermissionTestConstants;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.beans.xsd.PermissionBean;

import javax.activation.DataHandler;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public  class LifeCyclePermissionTestCase {

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
    private ManageEnvironment nonAdminEnvironment1;


    @BeforeClass(alwaysRun = true)
    public void initialize() throws Exception {
        EnvironmentBuilder builderAdmin = new EnvironmentBuilder().greg(ProductConstant.ADMIN_USER_ID);
        ManageEnvironment adminEnvironment = builderAdmin.build();
        UserInfo userInfo1 = UserListCsvReader.getUserInfo(2);
        UserInfo userInfo2 = UserListCsvReader.getUserInfo(3);


        ENABLED_USERS = new String [] {userInfo1.getUserNameWithoutDomain()};
        DISABLED_USERS = new String [] {userInfo2.getUserNameWithoutDomain()};

        resourceAdminServiceClient = new ResourceAdminServiceClient(adminEnvironment.getGreg().getBackEndUrl(),
                                                                    adminEnvironment.getGreg().getSessionCookie());


        //setup two roles, one with permission allowed and one with permission denied
        userManagementClient = new UserManagementClient(adminEnvironment.getGreg().getBackEndUrl(),
                                                        adminEnvironment.getGreg().getSessionCookie());

        GovernanceServiceClient governanceServiceClient =
                new GovernanceServiceClient(adminEnvironment.getGreg().getBackEndUrl(),
                                            adminEnvironment.getGreg().getSessionCookie());

        userManagementClient.addRole(ENABLED_ROLE, ENABLED_USERS, PERMISSION_LIST_ENABLED);
        userManagementClient.addRole(DISABLED_ROLE, DISABLED_USERS, PERMISSION_LIST_DISABLED);
        userManagementClient.updateUserListOfRole(PermissionTestConstants.NON_ADMIN_ROLE, new String[]{}, DISABLED_USERS);
        userManagementClient.updateUserListOfRole(PermissionTestConstants.NON_ADMIN_ROLE, new String[]{}, ENABLED_USERS);

        EnvironmentBuilder builderNonAdmin1 = new EnvironmentBuilder().greg(2);
        nonAdminEnvironment1 = builderNonAdmin1.build();
        EnvironmentBuilder builderNonAdmin2 = new EnvironmentBuilder().greg(3);
        ManageEnvironment nonAdminEnvironment2 = builderNonAdmin2.build();

        //initialize clients
        permittedLifeCycleManagementClient = new LifeCycleManagementClient(nonAdminEnvironment1.getGreg().getBackEndUrl()
                , userInfo1.getUserName(), userInfo1.getPassword());
        deniedLifeCycleManagementClient = new LifeCycleManagementClient(nonAdminEnvironment2.getGreg().getBackEndUrl()
                , userInfo2.getUserName(), userInfo2.getPassword());
        denideResourceAdminServiceClient = new ResourceAdminServiceClient(nonAdminEnvironment2.getGreg().getBackEndUrl(),
                userInfo2.getUserName(), userInfo2.getPassword());
        permittedResourceAdminServiceClient = new ResourceAdminServiceClient(nonAdminEnvironment1.getGreg().getBackEndUrl(),
                userInfo1.getUserName(), userInfo1.getPassword());

        String serviceConfigPath =
                ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
                File.separator + "GREG" + File.separator + "services" +
                File.separator + "defaultService.xml";

        DataHandler dataHandler = new DataHandler(new URL("file:///" + serviceConfigPath));
        String mediaType = "plain/text";
        String description = "This is a test service";
        resourceAdminServiceClient.addResource(
                "/_system/governance/repository/components/org.wso2.carbon.governance/configuration/services/service", mediaType, description, dataHandler);
    }

    @Test(groups = "wso2.greg", description = "Test permission to allow create a service",
          expectedExceptions = AxisFault.class)
    public void testDenyCreateService() throws ResourceAdminServiceExceptionException, MalformedURLException, RemoteException {

        String servicePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
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
        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
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
        assertEquals(lifeCycles.length,2 ,"Cannot view lifecycles for a logged in user with restricted permission");
    }

    @Test(groups = "wso2.greg", description = "Test permission to allow create a service")
    public void testAllowCreateServices()
            throws Exception, IOException, AddServicesServiceRegistryExceptionException,
                   ListMetadataServiceRegistryExceptionException,
                   ResourceAdminServiceExceptionException {

        UserInfo userInfo1 = UserListCsvReader.getUserInfo(1);

        ResourceAdminServiceClient resourceAdminServiceClient =
                new ResourceAdminServiceClient(nonAdminEnvironment1.getGreg().getBackEndUrl(),
                                               userInfo1.getUserName(), userInfo1.getPassword());

        PermissionBean permissionBean = resourceAdminServiceClient.getPermission(PERMISSION_PATH);

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

        String servicePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
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
        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
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
        userManagementClient.updateUserListOfRole(PermissionTestConstants.NON_ADMIN_ROLE, DISABLED_USERS, new String[]{});
        userManagementClient.updateUserListOfRole(PermissionTestConstants.NON_ADMIN_ROLE, ENABLED_USERS, new String[]{});
        userManagementClient.deleteRole(ENABLED_ROLE);
        userManagementClient.deleteRole(DISABLED_ROLE);

        userManagementClient = null;
        permittedLifeCycleManagementClient = null;
        deniedLifeCycleManagementClient = null;
        resourceAdminServiceClient = null;
    }
}
