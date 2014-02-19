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

import static org.testng.Assert.*;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.registry.permission.test.utils.PermissionTestConstants;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceResourceServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.beans.xsd.VersionPath;

import javax.activation.DataHandler;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

public class RolePermissionAsAdminTestCase {

    private static final String NEW_RESOURCE_PATH = "/_system/config/testRolePermissionDummy.txt";
    private static final String EXISTING_RESOURCE_PATH = "/_system/config/repository/components/" +
                                                         "org.wso2.carbon.governance/configuration/uri";
    private ResourceAdminServiceClient adminResourceAdminClient;
    private ResourceAdminServiceClient nonAdminResourceAdminClient;
    private String serverUrl;

    @BeforeClass(alwaysRun = true)
    public void initialize()
            throws LoginAuthenticationExceptionException, RemoteException, MalformedURLException,
                   ResourceAdminServiceExceptionException,
                   ResourceAdminServiceResourceServiceExceptionException {

        //Setup environments
        EnvironmentBuilder builderAdmin = new EnvironmentBuilder().greg(ProductConstant.ADMIN_USER_ID);
        ManageEnvironment adminEnvironment = builderAdmin.build();

        EnvironmentBuilder builderNonAdmin = new EnvironmentBuilder().greg(2);
        ManageEnvironment nonAdminEnvironment = builderNonAdmin.build();

        adminResourceAdminClient =
                new ResourceAdminServiceClient(adminEnvironment.getGreg().getBackEndUrl(),
                                               adminEnvironment.getGreg().getSessionCookie());
        nonAdminResourceAdminClient =
                new ResourceAdminServiceClient(nonAdminEnvironment.getGreg().getBackEndUrl(),
                                               nonAdminEnvironment.getGreg().getSessionCookie());

        //setup server url
        String backEndUrl = adminEnvironment.getGreg().getBackEndUrl();
        backEndUrl = backEndUrl.substring(0, backEndUrl.lastIndexOf("/"));
        serverUrl = backEndUrl.substring(0, backEndUrl.lastIndexOf("/"));

        //Add a new resource
        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator
                              + "GREG" + File.separator + "resource.txt";
        DataHandler dataHandler = new DataHandler(new URL("file:///" + resourcePath));
        adminResourceAdminClient.addResource(NEW_RESOURCE_PATH, "text/plain", "", dataHandler);
    }

    @Test(groups = "wso2.greg", expectedExceptions = IOException.class, description = "Test deny access to new " +
                                                                                      "resources")
    public void testUserDenyAccessToNewResource()
            throws ResourceAdminServiceResourceServiceExceptionException, IOException,
                   ResourceAdminServiceExceptionException {
        adminResourceAdminClient.addResourcePermission(NEW_RESOURCE_PATH, PermissionTestConstants.NON_ADMIN_ROLE,
                                                       PermissionTestConstants.READ_ACTION,
                                                       PermissionTestConstants.PERMISSION_DISABLED);
        adminResourceAdminClient.addResourcePermission(NEW_RESOURCE_PATH, PermissionTestConstants.EVERYONE_ROLE,
                                                       PermissionTestConstants.READ_ACTION,
                                                       PermissionTestConstants.PERMISSION_DISABLED);
        assertNull(nonAdminResourceAdminClient.getResource(NEW_RESOURCE_PATH));

        //test anonymous access. should give an exception
        URL resourceURL = new URL(serverUrl + PermissionTestConstants.WEB_APP_RESOURCE_URL + NEW_RESOURCE_PATH);
        InputStream resourceStream = resourceURL.openStream();
    }

    @Test(groups = "wso2.greg", expectedExceptions = IOException.class, description = "Test allow access to new " +
                                                                                      "resources")
    public void testUserAllowAccessToNewResource()
            throws ResourceAdminServiceResourceServiceExceptionException, IOException,
                   ResourceAdminServiceExceptionException {
        adminResourceAdminClient.addResourcePermission(NEW_RESOURCE_PATH, PermissionTestConstants.NON_ADMIN_ROLE,
                                                       PermissionTestConstants.READ_ACTION,
                                                       PermissionTestConstants.PERMISSION_ENABLED);
        assertNotNull(nonAdminResourceAdminClient.getResource(NEW_RESOURCE_PATH));

        //test anonymous access. should give an exception
        URL resourceURL = new URL(serverUrl + PermissionTestConstants.WEB_APP_RESOURCE_URL + NEW_RESOURCE_PATH);
        InputStream resourceStream = resourceURL.openStream();
    }

    @Test(groups = "wso2.greg", expectedExceptions = IOException.class, description = "Test deny access to existing " +
                                                                                      "resources")
    public void testUserDenyAccessToExistingResource()
            throws ResourceAdminServiceResourceServiceExceptionException, IOException,
                   ResourceAdminServiceExceptionException {
        adminResourceAdminClient.addResourcePermission(EXISTING_RESOURCE_PATH, PermissionTestConstants.NON_ADMIN_ROLE,
                                                       PermissionTestConstants.READ_ACTION,
                                                       PermissionTestConstants.PERMISSION_DISABLED);

        adminResourceAdminClient.addResourcePermission(EXISTING_RESOURCE_PATH, PermissionTestConstants.EVERYONE_ROLE,
                                                       PermissionTestConstants.READ_ACTION,
                                                       PermissionTestConstants.PERMISSION_DISABLED);
        assertNull(nonAdminResourceAdminClient.getResource(EXISTING_RESOURCE_PATH));

        //test anonymous access. should give an exception
        URL resourceURL = new URL(serverUrl + PermissionTestConstants.WEB_APP_RESOURCE_URL + EXISTING_RESOURCE_PATH);
        InputStream resourceStream = resourceURL.openStream();
    }

    @Test(groups = "wso2.greg", expectedExceptions = IOException.class, description = "Test allow access to existing " +
                                                                                      "resources")
    public void testUserAllowAccessToExistingResource()
            throws ResourceAdminServiceResourceServiceExceptionException, IOException,
                   ResourceAdminServiceExceptionException {
        adminResourceAdminClient.addResourcePermission(EXISTING_RESOURCE_PATH, PermissionTestConstants.NON_ADMIN_ROLE,
                                                       PermissionTestConstants.READ_ACTION,
                                                       PermissionTestConstants.PERMISSION_ENABLED);
        assertNotNull(nonAdminResourceAdminClient.getResource(EXISTING_RESOURCE_PATH));

        //test anonymous access. should give an exception
        URL resourceURL = new URL(serverUrl + PermissionTestConstants.WEB_APP_RESOURCE_URL + EXISTING_RESOURCE_PATH);
        InputStream resourceStream = resourceURL.openStream();
    }

    @Test(groups = "wso2.greg", expectedExceptions = IOException.class,
          description = "Test anonymous deny access to resources when internal/everyone is allowed for resources")
    public void testDenyAccessToAnonymousWhenEveryoneAllowed()
            throws ResourceAdminServiceResourceServiceExceptionException, IOException {
        adminResourceAdminClient.addResourcePermission(NEW_RESOURCE_PATH, PermissionTestConstants.EVERYONE_ROLE,
                                                       PermissionTestConstants.READ_ACTION,
                                                       PermissionTestConstants.PERMISSION_ENABLED);

        //test anonymous access. should give an exception
        URL resourceURL = new URL(serverUrl + PermissionTestConstants.WEB_APP_RESOURCE_URL + NEW_RESOURCE_PATH);
        InputStream resourceStream = resourceURL.openStream();
    }

    @Test(groups = "wso2.greg", expectedExceptions = IOException.class,
          description = "Test anonymous deny access to resources when internal/everyone is denied for resources")
    public void testDenyAccessToAnonymousWhenEveryoneDenied()
            throws ResourceAdminServiceResourceServiceExceptionException, IOException {
        adminResourceAdminClient.addResourcePermission(NEW_RESOURCE_PATH, PermissionTestConstants.EVERYONE_ROLE,
                                                       PermissionTestConstants.READ_ACTION,
                                                       PermissionTestConstants.PERMISSION_DISABLED);

        //test anonymous access. should give an exception
        URL resourceURL = new URL(serverUrl + PermissionTestConstants.WEB_APP_RESOURCE_URL + NEW_RESOURCE_PATH);
        InputStream resourceStream = resourceURL.openStream();
    }

    @Test(groups = "wso2.greg", description = "Test access to versions of resources using non-admin role")
    public void testVersionAccess()
            throws ResourceAdminServiceExceptionException, RemoteException,
                   ResourceAdminServiceResourceServiceExceptionException {
        adminResourceAdminClient.addResourcePermission(NEW_RESOURCE_PATH, PermissionTestConstants.NON_ADMIN_ROLE,
                                                       PermissionTestConstants.READ_ACTION,
                                                       PermissionTestConstants.PERMISSION_ENABLED);
        adminResourceAdminClient.createVersion(NEW_RESOURCE_PATH);
        VersionPath[] versionPaths = nonAdminResourceAdminClient.getVersionPaths(NEW_RESOURCE_PATH);
        for (VersionPath versionPath : nonAdminResourceAdminClient.getVersionPaths(NEW_RESOURCE_PATH)) {
            String resourceVersionPath = versionPath.getCompleteVersionPath();
            assertNotNull(nonAdminResourceAdminClient.getResource(resourceVersionPath));
        }
    }

    @Test(groups = "wso2.greg", description = "Test whether permissions are reassigned " +
                                              "when resource deleted and recreated with same name")
    public void testPermissionReassignWhenDeletedAndReCreated()
            throws ResourceAdminServiceExceptionException, RemoteException, MalformedURLException,
                   ResourceAdminServiceResourceServiceExceptionException, InterruptedException {
        //deny non-admin read permission and delete resource
        adminResourceAdminClient.addResourcePermission(NEW_RESOURCE_PATH, PermissionTestConstants.NON_ADMIN_ROLE,
                                                       PermissionTestConstants.READ_ACTION,
                                                       PermissionTestConstants.PERMISSION_DISABLED);
        adminResourceAdminClient.addResourcePermission(NEW_RESOURCE_PATH, PermissionTestConstants.EVERYONE_ROLE,
                                                       PermissionTestConstants.READ_ACTION,
                                                       PermissionTestConstants.PERMISSION_DISABLED);
        adminResourceAdminClient.deleteResource(NEW_RESOURCE_PATH);

        //add the same resource with same name
        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator
                              + "GREG" + File.separator + "resource.txt";
        DataHandler dataHandler = new DataHandler(new URL("file:///" + resourcePath));
        adminResourceAdminClient.addResource(NEW_RESOURCE_PATH, "text/plain", "", dataHandler);

        //old permission shouldn't be reset.
        assertNotNull(nonAdminResourceAdminClient.getResource(NEW_RESOURCE_PATH));
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp()
            throws ResourceAdminServiceExceptionException, RemoteException,
                   ResourceAdminServiceResourceServiceExceptionException {
        adminResourceAdminClient.deleteResource(NEW_RESOURCE_PATH);
        adminResourceAdminClient.addResourcePermission(EXISTING_RESOURCE_PATH, PermissionTestConstants.EVERYONE_ROLE,
                                                       PermissionTestConstants.READ_ACTION,
                                                       PermissionTestConstants.PERMISSION_ENABLED);
        adminResourceAdminClient.addResourcePermission(EXISTING_RESOURCE_PATH, PermissionTestConstants.NON_ADMIN_ROLE,
                                                       PermissionTestConstants.READ_ACTION,
                                                       PermissionTestConstants.PERMISSION_ENABLED);

        adminResourceAdminClient = null;
        nonAdminResourceAdminClient = null;
    }
}
