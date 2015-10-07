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

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.FrameworkConstants;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.registry.permission.test.utils.PermissionTestConstants;
import org.wso2.carbon.registry.permission.test.utils.PermissionTestUtil;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.GREGTestConstants;

import javax.activation.DataHandler;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

public class ExtensionPermissionTestCase extends GREGIntegrationBaseTest{

    private ResourceAdminServiceClient adminResourceAdminClient;
    private ResourceAdminServiceClient nonAdminResourceAdminClient;

    @BeforeClass(alwaysRun = true)
    public void initialize() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);

      //  PermissionTestUtil.resetTestRoles(automationContext);
        PermissionTestUtil.setUpTestRoles(automationContext);

        AutomationContext automationContextUser = new AutomationContext("GREG", "greg001",
                GREGTestConstants.SUPER_TENANT_DOMAIN_KEY, PermissionTestConstants.NON_ADMIN_ROLE_USER_1);

        String userSessionCookie = new LoginLogoutClient(automationContextUser).login();
        String adminSessionCookie = new LoginLogoutClient(automationContext).login();

        adminResourceAdminClient =
                new ResourceAdminServiceClient(backendURL, adminSessionCookie);

        nonAdminResourceAdminClient =
                new ResourceAdminServiceClient(backendURL, userSessionCookie);


    }

    @Test(groups = "wso2.greg", description = "Test whether admin can add an extension")
    public void testAdminExtensionAddPermissions()
            throws MalformedURLException, ResourceAdminServiceExceptionException, RemoteException {
        String resourcePath = getTestArtifactLocation() + "artifacts" + File.separator
                              + "GREG" + File.separator + "reports" + File.separator +
                              "TestingLCReportGenerator.jar";

        DataHandler dataHandler = new DataHandler(new URL("file:///" + resourcePath));
        adminResourceAdminClient.addExtension("TestingLCReportGenerator.jar", dataHandler);
        String[] extensionListToAdmin = adminResourceAdminClient.listExtensions();
        if (extensionListToAdmin != null) {
            boolean pass = false;
            for (String extension : extensionListToAdmin) {
                if (extension.equals("TestingLCReportGenerator.jar")) {
                    pass = true;
                }
            }
            assertTrue(pass);
        } else {
            fail("Test Extension adding has failed");
        }
    }

    @Test(groups = "wso2.greg", description = "Test whether admin can delete an extension",
          dependsOnMethods = "testAdminExtensionAddPermissions")
    public void testAdminExtensionDeletePermissions()
            throws MalformedURLException, ResourceAdminServiceExceptionException, RemoteException {
        adminResourceAdminClient.removeExtension("TestingLCReportGenerator.jar");
        String[] extensionListToAdmin = adminResourceAdminClient.listExtensions();
        if (extensionListToAdmin != null) {
            boolean pass = true;
            for (String extension : extensionListToAdmin) {
                if (extension.equals("TestingLCReportGenerator.jar")) {
                    pass = false;
                }
            }
            assertTrue(pass);
        }
    }

    @Test(groups = "wso2.greg", description = "Test whether a non admin can add an extension",
            dependsOnMethods = "testAdminExtensionDeletePermissions")
    public void testNonAdminExtensionAddPermissions()
            throws MalformedURLException, ResourceAdminServiceExceptionException, RemoteException {
        String resourcePath =  getTestArtifactLocation() + "artifacts" + File.separator
                              + "GREG" + File.separator + "reports" + File.separator + "TestingLCReportGenerator.jar";
        DataHandler dataHandler = new DataHandler(new URL("file:///" + resourcePath));

        //nonAdminResourceAdminClient created from testuser3 , that user in testRole and testRole has "Extension" permission
        //in the permission tree.
        boolean statues = nonAdminResourceAdminClient.addExtension("TestingLCReportGenerator.jar", dataHandler);

       assertTrue(statues ,"Failed to add extension");

    }

    @Test(dependsOnMethods = "testNonAdminExtensionAddPermissions")
    public void testNonAdminListExtensionPermissions()
            throws MalformedURLException, ResourceAdminServiceExceptionException, RemoteException {
        nonAdminResourceAdminClient.listExtensions(); //Not allowed
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws Exception {
        PermissionTestUtil.resetTestRoles(automationContext);
        String[] extensionListToAdmin = adminResourceAdminClient.listExtensions();
        if (extensionListToAdmin != null) {
            for (String extension : extensionListToAdmin) {
                if (extension.equals("TestingLCReportGenerator.jar")) {
                    adminResourceAdminClient.removeExtension("TestingLCReportGenerator.jar");
                }
            }
        }
        adminResourceAdminClient = null;
        nonAdminResourceAdminClient = null;
    }
}
