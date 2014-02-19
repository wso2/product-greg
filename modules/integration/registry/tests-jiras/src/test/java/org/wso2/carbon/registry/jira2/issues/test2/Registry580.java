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
package org.wso2.carbon.registry.jira2.issues.test2;


import org.apache.axis2.AxisFault;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.api.clients.user.mgt.UserManagementClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.lifecycle.test.utils.LifeCycleUtils;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;

public class Registry580 {
    private int adminId = ProductConstant.ADMIN_USER_ID;
    private WSRegistryServiceClient registry;
    private UserManagementClient userManagementClient;
    private EnvironmentBuilder builder;
    private ServiceManager serviceManager;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private String ROLE_NAME = "testRoleDeleteDenied";
    private ManageEnvironment environment;
    private Registry governance;
    private RegistryProviderUtil registryProviderUtil;
    private final String serviceName = "DeletePermissionDeniedService";
    int userId = 2;


    @BeforeClass(groups = "wso2.greg", description = "Create a role with Delete permission denied")
    public void init() throws Exception {

        builder = new EnvironmentBuilder().greg(adminId);
        environment = builder.build();
        userManagementClient = new UserManagementClient(environment.getGreg().getBackEndUrl(),
                                                        environment.getGreg().getSessionCookie());
        resourceAdminServiceClient = new ResourceAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                                                    environment.getGreg().getSessionCookie());
        userManagementClient.updateUserListOfRole("testRole", new String[]{},
                                                  new String[]{"testuser2"});
        userManagementClient.addRole(ROLE_NAME,
                                     new String[]{"testuser2"},
                                     new String[]{"/permission/admin/login",
                                                  "/permission/admin/manage/resources/browse",
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
                                                  "/permission/admin/manage/resources/ws-api"
                                     });

        String DENIED_DIR_PATH = "/_system/governance";
        String READ_ACTION = "2";
        String PERMISSION_ENABLED = "1";
        resourceAdminServiceClient.addResourcePermission(DENIED_DIR_PATH,
                                                         ROLE_NAME, READ_ACTION, PERMISSION_ENABLED);
        String WRITE_ACTION = "3";
        resourceAdminServiceClient.addResourcePermission(DENIED_DIR_PATH,
                                                         ROLE_NAME, WRITE_ACTION, PERMISSION_ENABLED);
        String DELETE_ACTION = "4";
        String PERMISSION_DISABLED = "0";
        resourceAdminServiceClient.addResourcePermission(DENIED_DIR_PATH,
                                                         ROLE_NAME, DELETE_ACTION, PERMISSION_DISABLED);

        String SERVICES_DENIED_DIR_PATH = "/_system/governance/trunk/services";
        resourceAdminServiceClient.addResourcePermission(SERVICES_DENIED_DIR_PATH,
                                                         ROLE_NAME, READ_ACTION, PERMISSION_ENABLED);
        resourceAdminServiceClient.addResourcePermission(SERVICES_DENIED_DIR_PATH,
                                                         ROLE_NAME, WRITE_ACTION, PERMISSION_ENABLED);
        resourceAdminServiceClient.addResourcePermission(SERVICES_DENIED_DIR_PATH,
                                                         ROLE_NAME, DELETE_ACTION, PERMISSION_DISABLED);
        governance = registryProviderUtil.getGovernanceRegistry(registry, userId);
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);


    }

    @Test(groups = "wso2.greg", description = "Add Service from new Role")
    public void testAddService() throws Exception {

        builder = new EnvironmentBuilder().greg(userId);
        environment = builder.build();
        registryProviderUtil = new RegistryProviderUtil();
        registry = registryProviderUtil.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);

        resourceAdminServiceClient = new ResourceAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                                                    environment.getGreg().getSessionCookie());

        serviceManager = new ServiceManager(governance);
        Service[] services = serviceManager.getAllServices();
        for (Service s : services) {
            if (s.getQName().getLocalPart().equals(serviceName)) {
                serviceManager.removeService(s.getId());
            }
        }

        LifeCycleUtils.addService("ns", serviceName, governance);


    }


    @Test(groups = "wso2.greg", description = "Edit Service from new Role",
          dependsOnMethods = "testAddService")
    public void testEditService() throws GovernanceException {

        serviceManager = new ServiceManager(governance);
        Service[] services;
        services = serviceManager.getAllServices();
        for (Service s : services) {
            if (s.getQName().getLocalPart().equals(serviceName)) {
                s.setAttribute("overview_version", "2.0.0");
                s.setAttribute("overview_description", "This is a Description");
                serviceManager.updateService(s);
            }
        }

        services = serviceManager.getAllServices();
        Boolean serviceChanged = false;
        for (Service s : services) {
            if (s.getAttribute("overview_version").equalsIgnoreCase("2.0.0")) {
                if (s.getAttribute("overview_description")!=null &&
                        s.getAttribute("overview_description").equals("This is a Description")) {
                    serviceChanged = true;
                }
            }
        }
        assertTrue(serviceChanged, "Service is not changed");
    }

    @Test(groups = "wso2.greg", description = "Try to delete Service as a Resource by a delete denied user ",
          dependsOnMethods = "testEditService", expectedExceptions = AxisFault.class)
    public void testDeleteResource()
            throws ResourceAdminServiceExceptionException, RemoteException {

        resourceAdminServiceClient.deleteResource("/_system/governance/trunk/services/ns");
    }

    @Test(groups = "wso2.greg", description = "Try to delete Service byService Manager by a delete denied user ",
          dependsOnMethods = "testDeleteResource", expectedExceptions = GovernanceException.class)
    public void testDeleteService() throws GovernanceException {


        serviceManager = new ServiceManager(governance);
        Service[] services = serviceManager.getAllServices();
        for (Service s : services) {
            if (s.getQName().getLocalPart().equals(serviceName)) {
                serviceManager.removeService(s.getId());
            }
        }


    }

    @AfterClass(groups = "wso2.greg", description = "Remove created service & role")
    public void removeServiceArtifacts() throws Exception {

        EnvironmentBuilder builder = new EnvironmentBuilder().greg(adminId);
        environment = builder.build();
        registry = registryProviderUtil.getWSRegistry(adminId, ProductConstant.GREG_SERVER_NAME);
        governance = registryProviderUtil.getGovernanceRegistry(registry, adminId);

        serviceManager = new ServiceManager(governance);
        Service[] services = serviceManager.getAllServices();
        for (Service s : services) {
            if (s.getQName().getLocalPart().equals(serviceName)) {
                serviceManager.removeService(s.getId());
            }
        }

        userManagementClient.updateUserListOfRole("testRole", new String[]{"testuser2"},
                                                  new String[]{});
        userManagementClient.deleteRole(ROLE_NAME);

        registry = null;
        userManagementClient = null;
        serviceManager = null;
        resourceAdminServiceClient = null;
        environment = null;
        governance = null;
        registryProviderUtil = null;
    }
}
