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
import org.wso2.carbon.automation.engine.FrameworkConstants;
import org.wso2.carbon.automation.engine.configurations.UrlGenerationUtil;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.integration.common.admin.client.UserManagementClient;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.registry.permission.test.utils.PermissionTestConstants;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceResourceServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.beans.xsd.VersionPath;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;

import javax.activation.DataHandler;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

public class RolePermissionAsAdminTestCase extends GREGIntegrationBaseTest{

    private static final String NEW_RESOURCE_PATH = "/_system/config/testRolePermissionDummy.txt";
    private static final String EXISTING_RESOURCE_PATH = "/_system/config/repository/components/" +
                                                         "org.wso2.carbon.governance/configuration/uri";
    private ResourceAdminServiceClient adminResourceAdminClient;
    private ResourceAdminServiceClient nonAdminResourceAdminClient;
    private UserManagementClient userManagementClient;
    private String [] userNames = { "permissionUN4"};

    @BeforeClass(alwaysRun = true)
    public void initialize() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String sessionCookie = getSessionCookie();

        AutomationContext automationContextUser1 = new AutomationContext("GREG", "greg001",
                FrameworkConstants.SUPER_TENANT_KEY, "permissionUN4");

        userManagementClient = new UserManagementClient(backendURL, sessionCookie);

        userManagementClient.updateUserListOfRole(FrameworkConstants.ADMIN_ROLE, null,  userNames);


        userManagementClient.addRole(PermissionTestConstants.NON_ADMIN_TEST_ROLE,
                userNames, PermissionTestConstants.NON_ADMIN_PERMISSION);


        adminResourceAdminClient =
                new ResourceAdminServiceClient(backendURL, sessionCookie);

        nonAdminResourceAdminClient =
                new ResourceAdminServiceClient(automationContextUser1.getContextUrls()
                        .getBackEndUrl(), new LoginLogoutClient(automationContextUser1).login());

        //Add a new resource
        String resourcePath = getTestArtifactLocation() + "artifacts" + File.separator
                              + "GREG" + File.separator + "resource.txt";

        DataHandler dataHandler = new DataHandler(new URL("file:///" + resourcePath));

        adminResourceAdminClient.addResource(NEW_RESOURCE_PATH, "text/plain", "", dataHandler);
    }

    @Test(groups = "wso2.greg", expectedExceptions = IOException.class, description = "Test deny access to new " +
                                                                                      "resources")
    public void testUserDenyAccessToNewResource()
            throws ResourceAdminServiceResourceServiceExceptionException, IOException,
            ResourceAdminServiceExceptionException, XPathExpressionException {
        adminResourceAdminClient.addResourcePermission(NEW_RESOURCE_PATH,
                                                       PermissionTestConstants.NON_ADMIN_TEST_ROLE,
                                                       PermissionTestConstants.READ_ACTION,
                                                       PermissionTestConstants.PERMISSION_DISABLED);

        adminResourceAdminClient.addResourcePermission(NEW_RESOURCE_PATH,
                                                       PermissionTestConstants.EVERYONE_ROLE,
                                                       PermissionTestConstants.READ_ACTION,
                                                       PermissionTestConstants.PERMISSION_DISABLED);
        assertNull(nonAdminResourceAdminClient.getResource(NEW_RESOURCE_PATH));

        //test anonymous access. should give an exception
        URL resourceURL = new URL(UrlGenerationUtil.getRemoteRegistryURL(automationContext
                .getDefaultInstance()) +  NEW_RESOURCE_PATH);
        InputStream resourceStream = resourceURL.openStream();
    }

    @Test(groups = "wso2.greg", expectedExceptions = IOException.class, description = "Test allow access to new " +
                                                                                      "resources")
    public void testUserAllowAccessToNewResource() throws ResourceAdminServiceResourceServiceExceptionException, IOException,
            ResourceAdminServiceExceptionException, XPathExpressionException {

        adminResourceAdminClient.addResourcePermission(NEW_RESOURCE_PATH,
                                                       PermissionTestConstants.NON_ADMIN_TEST_ROLE,
                                                       PermissionTestConstants.READ_ACTION,
                                                       PermissionTestConstants.PERMISSION_ENABLED);

        assertNotNull(nonAdminResourceAdminClient.getResource(NEW_RESOURCE_PATH));

        //test anonymous access. should give an exception
        URL resourceURL = new URL(UrlGenerationUtil.getRemoteRegistryURL(automationContext.getDefaultInstance()) +
                PermissionTestConstants.WEB_APP_RESOURCE_URL + NEW_RESOURCE_PATH);
        InputStream resourceStream = resourceURL.openStream();
    }

    @Test(groups = "wso2.greg", expectedExceptions = IOException.class, description = "Test deny access to existing " +
                                                                                      "resources")
    public void testUserDenyAccessToExistingResource()
            throws ResourceAdminServiceResourceServiceExceptionException, IOException,
            ResourceAdminServiceExceptionException, XPathExpressionException {
        adminResourceAdminClient.addResourcePermission(EXISTING_RESOURCE_PATH, PermissionTestConstants.NON_ADMIN_TEST_ROLE,
                                                       PermissionTestConstants.READ_ACTION,
                                                       PermissionTestConstants.PERMISSION_DISABLED);

        adminResourceAdminClient.addResourcePermission(EXISTING_RESOURCE_PATH, PermissionTestConstants.EVERYONE_ROLE,
                                                       PermissionTestConstants.READ_ACTION,
                                                       PermissionTestConstants.PERMISSION_DISABLED);
        assertNull(nonAdminResourceAdminClient.getResource(EXISTING_RESOURCE_PATH));

        //test anonymous access. should give an exception
        URL resourceURL = new URL(UrlGenerationUtil.getRemoteRegistryURL(automationContext.getDefaultInstance())
                + PermissionTestConstants.WEB_APP_RESOURCE_URL + EXISTING_RESOURCE_PATH);
        InputStream resourceStream = resourceURL.openStream();
    }

    @Test(groups = "wso2.greg", expectedExceptions = IOException.class, description = "Test allow access to existing " +
                                                                                      "resources")
    public void testUserAllowAccessToExistingResource()
            throws ResourceAdminServiceResourceServiceExceptionException, IOException,
            ResourceAdminServiceExceptionException, XPathExpressionException {
        adminResourceAdminClient.addResourcePermission(EXISTING_RESOURCE_PATH, PermissionTestConstants.NON_ADMIN_TEST_ROLE,
                                                       PermissionTestConstants.READ_ACTION,
                                                       PermissionTestConstants.PERMISSION_ENABLED);
        assertNotNull(nonAdminResourceAdminClient.getResource(EXISTING_RESOURCE_PATH));

        //test anonymous access. should give an exception
        URL resourceURL = new URL(UrlGenerationUtil.getRemoteRegistryURL(automationContext.getDefaultInstance()) +
                PermissionTestConstants.WEB_APP_RESOURCE_URL + EXISTING_RESOURCE_PATH);
        InputStream resourceStream = resourceURL.openStream();
    }

    @Test(groups = "wso2.greg", expectedExceptions = IOException.class,
          description = "Test anonymous deny access to resources when internal/everyone is allowed for resources")
    public void testDenyAccessToAnonymousWhenEveryoneAllowed()
            throws ResourceAdminServiceResourceServiceExceptionException, IOException, XPathExpressionException {
        adminResourceAdminClient.addResourcePermission(NEW_RESOURCE_PATH, PermissionTestConstants.EVERYONE_ROLE,
                                                       PermissionTestConstants.READ_ACTION,
                                                       PermissionTestConstants.PERMISSION_ENABLED);

        //test anonymous access. should give an exception
        URL resourceURL = new URL(UrlGenerationUtil.getRemoteRegistryURL(automationContext.getDefaultInstance())
                + PermissionTestConstants.WEB_APP_RESOURCE_URL + NEW_RESOURCE_PATH);
        InputStream resourceStream = resourceURL.openStream();
    }

    @Test(groups = "wso2.greg", expectedExceptions = IOException.class,
          description = "Test anonymous deny access to resources when internal/everyone is denied for resources")
    public void testDenyAccessToAnonymousWhenEveryoneDenied()
            throws ResourceAdminServiceResourceServiceExceptionException, IOException, XPathExpressionException {
        adminResourceAdminClient.addResourcePermission(NEW_RESOURCE_PATH, PermissionTestConstants.EVERYONE_ROLE,
                                                       PermissionTestConstants.READ_ACTION,
                                                       PermissionTestConstants.PERMISSION_DISABLED);

        //test anonymous access. should give an exception
        URL resourceURL = new URL(UrlGenerationUtil.getRemoteRegistryURL(automationContext.getDefaultInstance())
                + PermissionTestConstants.WEB_APP_RESOURCE_URL + NEW_RESOURCE_PATH);
        InputStream resourceStream = resourceURL.openStream();
    }

    @Test(groups = "wso2.greg", description = "Test access to versions of resources using non-admin role")
    public void testVersionAccess()
            throws ResourceAdminServiceExceptionException, RemoteException,
                   ResourceAdminServiceResourceServiceExceptionException {
        adminResourceAdminClient.addResourcePermission(NEW_RESOURCE_PATH, PermissionTestConstants.NON_ADMIN_TEST_ROLE,
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
        adminResourceAdminClient.addResourcePermission(NEW_RESOURCE_PATH, PermissionTestConstants.NON_ADMIN_TEST_ROLE,
                                                       PermissionTestConstants.READ_ACTION,
                                                       PermissionTestConstants.PERMISSION_DISABLED);
        adminResourceAdminClient.addResourcePermission(NEW_RESOURCE_PATH, PermissionTestConstants.EVERYONE_ROLE,
                                                       PermissionTestConstants.READ_ACTION,
                                                       PermissionTestConstants.PERMISSION_DISABLED);
        adminResourceAdminClient.deleteResource(NEW_RESOURCE_PATH);

        //add the same resource with same name
        String resourcePath = getTestArtifactLocation() + "artifacts" + File.separator
                              + "GREG" + File.separator + "resource.txt";
        DataHandler dataHandler = new DataHandler(new URL("file:///" + resourcePath));
        adminResourceAdminClient.addResource(NEW_RESOURCE_PATH, "text/plain", "", dataHandler);

        //old permission shouldn't be reset.
        assertNotNull(nonAdminResourceAdminClient.getResource(NEW_RESOURCE_PATH));
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws Exception {
        adminResourceAdminClient.deleteResource(NEW_RESOURCE_PATH);

        adminResourceAdminClient.addResourcePermission(EXISTING_RESOURCE_PATH,
                                                       PermissionTestConstants.EVERYONE_ROLE,
                                                       PermissionTestConstants.READ_ACTION,
                                                       PermissionTestConstants.PERMISSION_ENABLED);

        adminResourceAdminClient.addResourcePermission(EXISTING_RESOURCE_PATH,
                                                       PermissionTestConstants.NON_ADMIN_TEST_ROLE,
                                                       PermissionTestConstants.READ_ACTION,
                                                       PermissionTestConstants.PERMISSION_ENABLED);

        if( userManagementClient.roleNameExists(PermissionTestConstants.NON_ADMIN_TEST_ROLE)){
            userManagementClient.deleteRole(PermissionTestConstants.NON_ADMIN_TEST_ROLE);
        }
    }
}
