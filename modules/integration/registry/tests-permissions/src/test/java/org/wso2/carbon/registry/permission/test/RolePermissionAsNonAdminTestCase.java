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

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.registry.permission.test.utils.PermissionTestConstants;
import org.wso2.carbon.registry.permission.test.utils.PermissionTestUtil;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceResourceServiceExceptionException;

import javax.activation.DataHandler;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

public class RolePermissionAsNonAdminTestCase {

    private static final String NEW_RESOURCE_PATH = "/_system/config/testPermissionNonAdminDummy.txt";
    private ResourceAdminServiceClient adminResourceAdminClient;
    private ResourceAdminServiceClient nonAdminResourceAdminClient;
    private ResourceAdminServiceClient nonAdminResourceAdminClient2;
    private String serverUrl;

    @BeforeClass(alwaysRun = true)
    public void initialize()
            throws Exception, RemoteException, MalformedURLException,
                   ResourceAdminServiceExceptionException,
                   ResourceAdminServiceResourceServiceExceptionException {

        PermissionTestUtil.setUpTestRoles();
        //Setup environments
        EnvironmentBuilder builderAdmin = new EnvironmentBuilder().greg(ProductConstant.ADMIN_USER_ID);
        ManageEnvironment adminEnvironment = builderAdmin.build();

        EnvironmentBuilder builderNonAdmin = new EnvironmentBuilder().greg(2);
        ManageEnvironment nonAdminEnvironment = builderNonAdmin.build();

        EnvironmentBuilder builderNonAdmin2 = new EnvironmentBuilder().greg(3);
        ManageEnvironment nonAdmin2Environment = builderNonAdmin2.build();

        adminResourceAdminClient =
                new ResourceAdminServiceClient(adminEnvironment.getGreg().getBackEndUrl(),
                                               adminEnvironment.getGreg().getSessionCookie());
        nonAdminResourceAdminClient =
                new ResourceAdminServiceClient(nonAdminEnvironment.getGreg().getBackEndUrl(),
                                               nonAdminEnvironment.getGreg().getSessionCookie());
        nonAdminResourceAdminClient2 =
                new ResourceAdminServiceClient(nonAdmin2Environment.getGreg().getBackEndUrl(),
                                               nonAdmin2Environment.getGreg().getSessionCookie());

        //setup server url
        String backEndUrl = adminEnvironment.getGreg().getBackEndUrl();
        backEndUrl = backEndUrl.substring(0, backEndUrl.lastIndexOf("/"));
        serverUrl = backEndUrl.substring(0, backEndUrl.lastIndexOf("/"));

        //Add a new resource
        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator
                              + "GREG" + File.separator + "resource.txt";
        DataHandler dataHandler = new DataHandler(new URL("file:///" + resourcePath));
        adminResourceAdminClient.addResource(NEW_RESOURCE_PATH, "text/plain", "", dataHandler);
        adminResourceAdminClient.addResourcePermission(NEW_RESOURCE_PATH, PermissionTestConstants.EVERYONE_ROLE,
                                                       PermissionTestConstants.READ_ACTION, PermissionTestConstants.PERMISSION_DISABLED);
    }

    @Test(groups = "wso2.greg", expectedExceptions = IOException.class, description = "Test deny access to new " +
                                                                                      "resources")
    public void testUserDenyAccessToNewResource()
            throws ResourceAdminServiceResourceServiceExceptionException, IOException,
                   ResourceAdminServiceExceptionException {
        adminResourceAdminClient.addResourcePermission(NEW_RESOURCE_PATH, PermissionTestConstants.NON_ADMIN_ROLE_2,
                                                       PermissionTestConstants.READ_ACTION, PermissionTestConstants.PERMISSION_ENABLED);
        assertNotNull(nonAdminResourceAdminClient2.getResource(NEW_RESOURCE_PATH));
        nonAdminResourceAdminClient.addResourcePermission(NEW_RESOURCE_PATH, PermissionTestConstants.NON_ADMIN_ROLE_2,
                                                          PermissionTestConstants.READ_ACTION, PermissionTestConstants.PERMISSION_DISABLED);
        nonAdminResourceAdminClient2.getResource(NEW_RESOURCE_PATH);
        assertNull(nonAdminResourceAdminClient2.getResource(NEW_RESOURCE_PATH));

        //test anonymous access. should give an exception
        URL resourceURL = new URL(serverUrl + PermissionTestConstants.WEB_APP_RESOURCE_URL + NEW_RESOURCE_PATH);
        InputStream resourceStream = resourceURL.openStream();
    }

    @Test(groups = "wso2.greg", expectedExceptions = IOException.class, description = "Test allow access to new " +
                                                                                      "resources")
    public void testUserAllowAccessToNewResource()
            throws ResourceAdminServiceResourceServiceExceptionException, IOException,
                   ResourceAdminServiceExceptionException {
        adminResourceAdminClient.addResourcePermission(NEW_RESOURCE_PATH, PermissionTestConstants.NON_ADMIN_ROLE_2,
                                                       PermissionTestConstants.READ_ACTION, PermissionTestConstants.PERMISSION_DISABLED);
        assertNull(nonAdminResourceAdminClient2.getResource(NEW_RESOURCE_PATH));

        nonAdminResourceAdminClient.addResourcePermission(NEW_RESOURCE_PATH, PermissionTestConstants.NON_ADMIN_ROLE_2,
                                                          PermissionTestConstants.READ_ACTION, PermissionTestConstants.PERMISSION_ENABLED);
        assertNotNull(nonAdminResourceAdminClient2.getResource(NEW_RESOURCE_PATH));

        //test anonymous access. should give an exception
        URL resourceURL = new URL(serverUrl + PermissionTestConstants.WEB_APP_RESOURCE_URL + NEW_RESOURCE_PATH);
        InputStream resourceStream = resourceURL.openStream();
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws Exception {
        PermissionTestUtil.resetTestRoles();
        adminResourceAdminClient.deleteResource(NEW_RESOURCE_PATH);

        adminResourceAdminClient = null;
        nonAdminResourceAdminClient = null;
        nonAdminResourceAdminClient2 = null;
    }
}
