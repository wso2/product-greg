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
import org.wso2.carbon.automation.api.clients.governance.ListMetaDataServiceClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.api.clients.user.mgt.UserManagementClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkSettings;
import org.wso2.carbon.governance.list.stub.beans.xsd.PolicyBean;
import org.wso2.carbon.governance.list.stub.beans.xsd.SchemaBean;
import org.wso2.carbon.governance.list.stub.beans.xsd.WSDLBean;
import org.wso2.carbon.registry.permission.test.utils.PermissionTestConstants;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;

import javax.activation.DataHandler;
import java.io.File;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class PermissionOnArtifactsTestCase {

    private static final String WSDL_FILE_PATH =
            "/_system/governance/trunk/wsdls/org/wso2/carbon/core/services/echo/echo.wsdl";
    private static final String SCHEMA_FILE_PATH = "/_system/governance/trunk/schemas/org/charitha/" +
                                                   "calculator.xsd";

    private static final String SERVICE_FILE_PATH = "/_system/governance/trunk/services/org/wso2/" +
                                                    "carbon/core/services/echo/echoyuSer1";

    private static final String[] PERMISSION_LIST_ENABLED = {
            "/permission/admin/login",
            "/permission/admin/manage/resources/browse",
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
            "/permission/admin/manage/resources/govern/policy/list"
    };
    private static final String[] PERMISSION_LIST_DISABLED = {
            "/permission/admin/login",
    };
    private static final String ENABLED_ROLE = "enabledRole";
    private static final String DISABLED_ROLE = "disabledRole";
    private static final String[] ENABLED_USERS = {"testuser2"};
    private static final String[] DISABLED_USERS = {"testuser3"};

    private String policyPath;

    private ResourceAdminServiceClient adminResourceAdminServiceClient;
    private ResourceAdminServiceClient permittedResourceAdminServiceClient;
    private ResourceAdminServiceClient deniedResourceAdminServiceClient;
    private ListMetaDataServiceClient permittedListMetaDataServiceClient;
    private DataHandler wsdlDataHandler;
    private DataHandler policyDataHandler;
    private DataHandler schemaDataHandler;
    private UserManagementClient userManagementClient;

    @BeforeClass
    public void initialize() throws Exception {
        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator
                              + "GREG" + File.separator + "wsdl" + File.separator + "echo.wsdl";
        wsdlDataHandler = new DataHandler(new URL("file:///" + resourcePath));

        String path = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator
                      + "GREG" + File.separator + "policy" + File.separator + "policy.xml";
        policyDataHandler = new DataHandler(new URL("file:///" + path));

        String path1 = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator
                       + "GREG" + File.separator + "schema" + File.separator + "calculator.xsd";
        schemaDataHandler = new DataHandler(new URL("file:///" + path1));

        EnvironmentBuilder builderAdmin;
        ManageEnvironment adminEnvironment;
        EnvironmentBuilder builderNonAdmin1;
        ManageEnvironment nonAdminEnvironment1;
        EnvironmentBuilder builderNonAdmin2;
        ManageEnvironment nonAdminEnvironment2;

        //build environments
        builderAdmin = new EnvironmentBuilder().greg(ProductConstant.ADMIN_USER_ID);
        adminEnvironment = builderAdmin.build();

        builderNonAdmin1 = new EnvironmentBuilder().greg(2);
        nonAdminEnvironment1 = builderNonAdmin1.build();
        builderNonAdmin2 = new EnvironmentBuilder().greg(3);
        nonAdminEnvironment2 = builderNonAdmin2.build();

        userManagementClient = new UserManagementClient(adminEnvironment.getGreg().getBackEndUrl(),
                                                        adminEnvironment.getGreg().getSessionCookie());
        userManagementClient.addRole(ENABLED_ROLE, ENABLED_USERS, PERMISSION_LIST_ENABLED);
        userManagementClient.addRole(DISABLED_ROLE, DISABLED_USERS, PERMISSION_LIST_DISABLED);
        userManagementClient.updateUserListOfRole(PermissionTestConstants.NON_ADMIN_ROLE,
                                                  new String[]{}, DISABLED_USERS);
        userManagementClient.updateUserListOfRole(PermissionTestConstants.NON_ADMIN_ROLE,
                                                  new String[]{}, ENABLED_USERS);

        adminResourceAdminServiceClient =
                new ResourceAdminServiceClient(adminEnvironment.getGreg().getBackEndUrl(),
                                               adminEnvironment.getGreg().getSessionCookie());

        permittedResourceAdminServiceClient =
                new ResourceAdminServiceClient(nonAdminEnvironment1.getGreg().getBackEndUrl(),
                                               nonAdminEnvironment1.getGreg().getSessionCookie());
        permittedListMetaDataServiceClient =
                new ListMetaDataServiceClient(nonAdminEnvironment1.getGreg().getBackEndUrl(),
                                              nonAdminEnvironment1.getGreg().getSessionCookie());
        deniedResourceAdminServiceClient =
                new ResourceAdminServiceClient(nonAdminEnvironment2.getGreg().getBackEndUrl(),
                                               nonAdminEnvironment2.getGreg().getSessionCookie());

        adminResourceAdminServiceClient.addResourcePermission("/_system/governance", ENABLED_ROLE,
                                                              PermissionTestConstants.WRITE_ACTION,
                                                              PermissionTestConstants.PERMISSION_ENABLED);

        adminResourceAdminServiceClient.addResourcePermission("/_system/config", ENABLED_ROLE,
                                                              PermissionTestConstants.WRITE_ACTION,
                                                              PermissionTestConstants.PERMISSION_ENABLED);
    }

    @Test(groups = "wso2.greg", description = "Test deny permission to add a WSDL",
          expectedExceptions = AxisFault.class)
    public void testDenyAddWSDL() throws ResourceAdminServiceExceptionException, RemoteException {
        deniedResourceAdminServiceClient.addWSDL("", wsdlDataHandler);
    }

    @Test(groups = "wso2.greg", description = "Test allow permission to add a WSDL",
          dependsOnMethods = "testDenyAddWSDL")
    public void testAllowAddWSDL() throws ResourceAdminServiceExceptionException, RemoteException {
        permittedResourceAdminServiceClient.addWSDL("", wsdlDataHandler);
        WSDLBean wsdlBean = permittedListMetaDataServiceClient.listWSDLs();
        boolean wsdlExists = false;
        String[] names1 = wsdlBean.getName();
        for (String name : names1) {
            if ("echo.wsdl".equalsIgnoreCase(name)) {
                wsdlExists = true;
            }
        }
        assertTrue(wsdlExists);
    }


    @Test(groups = "wso2.greg", description = "Test deny permission to add a Policy",
          expectedExceptions = AxisFault.class, dependsOnMethods = "testAllowAddWSDL")
    public void testDenyAddPolicy() throws ResourceAdminServiceExceptionException, RemoteException {
        deniedResourceAdminServiceClient.addPolicy("", policyDataHandler);
    }

    @Test(groups = "wso2.greg", description = "Test allow permission to add a Policy",
          dependsOnMethods = "testDenyAddPolicy")
    public void testAllowAddPolicy()
            throws ResourceAdminServiceExceptionException, RemoteException {
        permittedResourceAdminServiceClient.addPolicy("", policyDataHandler);
        PolicyBean policyBean = permittedListMetaDataServiceClient.listPolicies();
        String[] names = policyBean.getName();
        boolean policyExists = false;
        for (String name : names) {
            if ("policy.xml".equalsIgnoreCase(name)) {
                policyExists = true;
            }
        }
        assertTrue(policyExists);

        String[] policyNames = permittedListMetaDataServiceClient.listPolicies().getPath();
        for (String policyName : policyNames) {
            if (policyName.contains("policy.xml")) {
                policyPath = "/_system/governance" + policyName;
            }
        }
    }

    @Test(groups = "wso2.greg", description = "Test deny permission to add a schema",
          expectedExceptions = AxisFault.class, dependsOnMethods = "testAllowAddPolicy")
    public void testDenyAddSchema() throws ResourceAdminServiceExceptionException, RemoteException {
        deniedResourceAdminServiceClient.addSchema("", schemaDataHandler);
    }

    @Test(groups = "wso2.greg", description = "Test allow permission to add a schema",
          dependsOnMethods = "testDenyAddSchema")
    public void testAllowAddSchema()
            throws ResourceAdminServiceExceptionException, RemoteException {
        permittedResourceAdminServiceClient.addSchema("", schemaDataHandler);
        SchemaBean schemaBean = permittedListMetaDataServiceClient.listSchemas();
        String[] names = schemaBean.getName();
        boolean schemaExists = false;
        for (String name : names) {
            if ("calculator.xsd".equalsIgnoreCase(name)) {
                schemaExists = true;
            }
        }
        assertTrue(schemaExists);
    }

    @Test(groups = "wso2.greg", description = "Test deny permission to add a symlink",
          expectedExceptions = AxisFault.class)
    public void testDenyAddSymlink()
            throws ResourceAdminServiceExceptionException, RemoteException {
        deniedResourceAdminServiceClient.addSymbolicLink("/_system", "sym1", "/_system/config");
    }

    @Test(groups = "wso2.greg", description = "Test allow permission to add a symlink", dependsOnMethods = "testDenyAddSymlink")
    public void testAllowAddSymlink()
            throws ResourceAdminServiceExceptionException, RemoteException {
        permittedResourceAdminServiceClient.addSymbolicLink("/_system/governance", "sym1", "/_system/config");
        assertNotNull(permittedResourceAdminServiceClient.getResource("/_system/governance/sym1"));
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() throws Exception {
        if (adminResourceAdminServiceClient.getResource(SERVICE_FILE_PATH) != null) {
            adminResourceAdminServiceClient.deleteResource(SERVICE_FILE_PATH);
        }
        if (adminResourceAdminServiceClient.getResource(WSDL_FILE_PATH) != null) {
            adminResourceAdminServiceClient.deleteResource(WSDL_FILE_PATH);
        }
        if (adminResourceAdminServiceClient.getResource(SCHEMA_FILE_PATH) != null) {
            adminResourceAdminServiceClient.deleteResource(SCHEMA_FILE_PATH);
        }
        if (policyPath != null && adminResourceAdminServiceClient.getResource(policyPath) != null) {
            adminResourceAdminServiceClient.deleteResource(policyPath);
        }

        adminResourceAdminServiceClient.deleteResource("/_system/governance/sym1");
        userManagementClient.updateUserListOfRole(PermissionTestConstants.NON_ADMIN_ROLE, DISABLED_USERS, new String[]{});
        userManagementClient.updateUserListOfRole(PermissionTestConstants.NON_ADMIN_ROLE, ENABLED_USERS, new String[]{});
        userManagementClient.deleteRole(ENABLED_ROLE);
        userManagementClient.deleteRole(DISABLED_ROLE);

        adminResourceAdminServiceClient = null;
        permittedResourceAdminServiceClient = null;
        deniedResourceAdminServiceClient = null;
        permittedListMetaDataServiceClient = null;
        wsdlDataHandler = null;
        policyDataHandler = null;
        schemaDataHandler = null;
        userManagementClient = null;
    }
}
