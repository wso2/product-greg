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

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.user.mgt.UserManagementClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.registry.activities.stub.RegistryExceptionException;
import org.wso2.carbon.registry.permission.test.utils.CustomPermissionTests;
import org.wso2.carbon.registry.permission.test.utils.PermissionTestConstants;
import org.wso2.carbon.registry.properties.stub.PropertiesAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.relations.stub.AddAssociationRegistryExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceResourceServiceExceptionException;

import java.io.IOException;
import java.rmi.RemoteException;

public abstract class CustomPermissionTestCase {
    private static final String[][] PERMISSION_LIST = {
            {
                    "/permission/admin/login",
                    "/permission/admin/manage/resources/notifications",
                    "/permission/admin/manage/resources/browse",
                    "/permission/admin/manage/resources/community-features",
                    "/permission/admin/configure/security"
            },
            {
                    "/permission/admin/login",
                    "/permission/admin/manage/resources/browse",
                    "/permission/admin/manage/search/activities",
            },
            {
                    "/permission/admin/login",
                    "/permission/admin/manage/resources/browse"
            },
            {
                    "/permission/admin/login",
                    "/permission/admin/manage/resources/browse",
                    "/permission/admin/manage/resources/associations"
            },
            {
                    "/permission/admin/login",
                    "/permission/admin/manage/resources/browse",
                    "/permission/admin/manage/resources/community-features",
                    "/permission/admin/configure/governance/lifecycles"
            },
    };


    private static final String[] TEST_ROLES = {
            "testRole3",
            "testRole4",
            "testRole5",
            "testRole6",
            "testRole7",
    };

    private static final String[] TEST_USERS = {
            "testuser3",
            "testuser4",
            "testuser5",
            "testuser6",
            "testuser7",
    };

    private UserManagementClient userManagementClient;

    private void initRoles() throws Exception {
        int roleCount = TEST_ROLES.length;
        userManagementClient.updateUserListOfRole(PermissionTestConstants.NON_ADMIN_ROLE, new String[]{}, TEST_USERS);
        for (int i = 0; i < roleCount; i++) {
            userManagementClient.addRole(TEST_ROLES[i], new String[]{TEST_USERS[i]}, PERMISSION_LIST[i]);
        }
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        EnvironmentBuilder builderAdmin = new EnvironmentBuilder().greg(ProductConstant.ADMIN_USER_ID);
        ManageEnvironment adminEnvironment = builderAdmin.build();

        userManagementClient = new UserManagementClient(adminEnvironment.getGreg().getBackEndUrl(),
                                                        adminEnvironment.getGreg().getSessionCookie());
        initRoles();
    }

    @Test(groups = "wso2.greg", description = "test permission when only activity search permission given")
    public void testActivitySearchOnly() throws ResourceAdminServiceExceptionException, IOException,
                                                LoginAuthenticationExceptionException,
                                                RegistryExceptionException, RemoteException,
                                                LifeCycleManagementServiceExceptionException,
                                                AddAssociationRegistryExceptionException,
                                                ResourceAdminServiceResourceServiceExceptionException,
                                                PropertiesAdminServiceRegistryExceptionException,
                                                InterruptedException {
        ManageEnvironment manageEnvironment = new EnvironmentBuilder().greg(2).build();
        Assert.assertTrue(CustomPermissionTests.canSearchActivities(manageEnvironment, "testuser3"));
        Assert.assertFalse(CustomPermissionTests.canAddLifecycles(manageEnvironment));
        Assert.assertFalse(CustomPermissionTests.canAddAssociation(manageEnvironment));
    }

    @Test(groups = "wso2.greg", description = "test permission when only browse permission given")
    public void testBrowseOnly() throws ResourceAdminServiceExceptionException, IOException,
                                        LoginAuthenticationExceptionException,
                                        RegistryExceptionException,
                                        LifeCycleManagementServiceExceptionException,
                                        AddAssociationRegistryExceptionException,
                                        ResourceAdminServiceResourceServiceExceptionException,
                                        PropertiesAdminServiceRegistryExceptionException,
                                        InterruptedException {
        ManageEnvironment manageEnvironment = new EnvironmentBuilder().greg(3).build();
        Assert.assertTrue(CustomPermissionTests.canReadResource(manageEnvironment, "/_system/config"));
        Assert.assertFalse(CustomPermissionTests.canSearchActivities(manageEnvironment, "testuser4"));
        Assert.assertFalse(CustomPermissionTests.canAddLifecycles(manageEnvironment));
        Assert.assertFalse(CustomPermissionTests.canAddAssociation(manageEnvironment));
    }

    @Test(groups = "wso2.greg", description = "test permission when only browse and Association permission given")
    public void testBrowseAndAssociations()
            throws ResourceAdminServiceExceptionException, IOException,
                   LoginAuthenticationExceptionException, RegistryExceptionException,
                   LifeCycleManagementServiceExceptionException,
                   AddAssociationRegistryExceptionException,
                   ResourceAdminServiceResourceServiceExceptionException,
                   PropertiesAdminServiceRegistryExceptionException, InterruptedException {
        ManageEnvironment manageEnvironment = new EnvironmentBuilder().greg(4).build();
        Assert.assertTrue(CustomPermissionTests.canReadResource(manageEnvironment, "/_system/config"));
        Assert.assertTrue(CustomPermissionTests.canAddAssociation(manageEnvironment));
        Assert.assertFalse(CustomPermissionTests.canSearchActivities(manageEnvironment, "testuser5"));
        Assert.assertFalse(CustomPermissionTests.canAddLifecycles(manageEnvironment));
    }

    @Test(groups = "wso2.greg", description = "test permission when only browse and lifecycle permission given")
    public void testBrowseAndLifecycles()
            throws ResourceAdminServiceExceptionException, IOException,
                   LoginAuthenticationExceptionException, RegistryExceptionException,
                   LifeCycleManagementServiceExceptionException,
                   AddAssociationRegistryExceptionException,
                   ResourceAdminServiceResourceServiceExceptionException,
                   PropertiesAdminServiceRegistryExceptionException, InterruptedException {
        ManageEnvironment manageEnvironment = new EnvironmentBuilder().greg(5).build();
        Assert.assertTrue(CustomPermissionTests.canReadResource(manageEnvironment, "/_system/config"));
        Assert.assertTrue(CustomPermissionTests.canAddLifecycles(manageEnvironment));
        Assert.assertFalse(CustomPermissionTests.canAddAssociation(manageEnvironment));
        Assert.assertFalse(CustomPermissionTests.canSearchActivities(manageEnvironment, "testuser6"));
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() throws Exception {
        int roleCount = TEST_ROLES.length;
        userManagementClient.updateUserListOfRole(PermissionTestConstants.NON_ADMIN_ROLE, TEST_USERS, new String[]{});
        for (int i = 0; i < roleCount; i++) {
            userManagementClient.deleteRole(TEST_ROLES[i]);
        }
        userManagementClient = null;
    }
}
