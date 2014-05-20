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

import org.apache.abdera.model.AtomDate;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.util.base64.Base64Utils;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.FrameworkConstants;
import org.wso2.carbon.automation.engine.configurations.UrlGenerationUtil;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.integration.common.admin.client.UserManagementClient;
import org.wso2.carbon.registry.app.RemoteRegistry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.permission.test.utils.PermissionTestConstants;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.GREGTestConstants;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathExpressionException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

public class AtomFeedPermissionTestCase extends GREGIntegrationBaseTest{
    //Same scenario as in AtomFeedTestCase class with non-admin limited permissions

    public static String USER_NAME;
    public static String PASSWORD;
    public static final String REGISTRY_NAMESPACE = "http://wso2.org/registry";

    private static final String[] PERMISSION_LIST = {
            "/permission/admin/login",
            "/permission/admin/manage/resources",
            "/permission/admin/manage/resources/browse",
    };

    private static final String ROLE_NAME = "atomtestrole";
    private static String[] ROLE_USERS = {"atomtestuser"};

    public static final String resourcePath = "/rrr1";
    public RemoteRegistry registry;
    public RemoteRegistry registryAdmin;
    private UserManagementClient userManagementClient;
    String sessionCookie;

    @BeforeClass(groups = {"wso2.greg"})
    public void init() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        sessionCookie = getSessionCookie();

        userManagementClient = new UserManagementClient(backendURL, sessionCookie);

        userManagementClient.updateUserListOfRole(FrameworkConstants.ADMIN_ROLE, null,
                ROLE_USERS); //remote user from admin role

        if(!userManagementClient.userNameExists(ROLE_NAME, ROLE_USERS[0])){
            userManagementClient.addUser(ROLE_USERS[0],ROLE_USERS[0], null,
                    null);//add the server to new role
        }

        if (!userManagementClient.roleNameExists(ROLE_NAME)) {
            userManagementClient.addRole(ROLE_NAME, ROLE_USERS, PERMISSION_LIST);
        }

        AutomationContext automationContextUser = new AutomationContext("GREG", "greg001",
                GREGTestConstants.SUPER_TENANT_DOMAIN_KEY, ROLE_USERS[0]);

        registryAdmin =
                new RegistryProviderUtil().getRemoteRegistry(automationContext);

        registry =
                new RegistryProviderUtil().getRemoteRegistry(automationContextUser);

        populateData();

        String userName = automationContext.getContextTenant().getContextUser().getUserName();

        if(!automationContext.getContextTenant().getContextUser().getUserName().contains
                (FrameworkConstants.SUPER_TENANT_DOMAIN_NAME)){
            USER_NAME = userName;
        }else{
            USER_NAME = userName.substring(0, userName.indexOf('@'));
        }

        PASSWORD = automationContext.getContextTenant().getContextUser().getPassword();
    }

    private void populateData() {
        try {
            Resource resource = registryAdmin.newResource();
            resource.setContent("This is a test resource".getBytes());
            resource.setDescription("This is a test description");

            registryAdmin.put(resourcePath, resource);
        } catch (RegistryException e) {
            fail("Could not populate data. Failed to add resource to the registry");
        }
    }

    @Test(groups = {"wso2.greg"})
    public void atomFeedTest() throws Exception {

        Resource resource = registry.get(resourcePath);
        OMElement atomFeedOMElement = getAtomFeedContent(constructAtomURL(automationContext,
                resourcePath));

        if (atomFeedOMElement == null) {
            fail("No feed data available");

        }

//        checking whether the updated times are correct
        OMElement updatedElement = atomFeedOMElement.getFirstChildWithName(
                new QName(atomFeedOMElement.getNamespace().getNamespaceURI(), "updated"));
        if (!updatedElement.getText().equals(getAtomDateString(resource.getLastModified()))) {
            fail("Last updated times are incorrect");
        }

//        Checking whether the created times are correct
        OMElement createdElement = atomFeedOMElement.getFirstChildWithName(
                new QName(REGISTRY_NAMESPACE, "createdTime"));
        if (!createdElement.getText().equals(getAtomDateString(resource.getCreatedTime()))) {
            fail("Created times are incorrect");
        }

//        Checking whether description is correct
        OMElement descriptionElement = atomFeedOMElement.getFirstChildWithName(
                new QName(atomFeedOMElement.getNamespace().getNamespaceURI(), "summary"));
        if (!descriptionElement.getText().equals(resource.getDescription())) {
            fail("description is invalid");
        }
    }

    private String getAtomDateString(Date date) {
        AtomDate atomDate = new AtomDate(date);
        return atomDate.getValue();
    }

    private String constructAtomURL(AutomationContext autoCtx, String path)
            throws XPathExpressionException {
        String registryURL = UrlGenerationUtil.getRemoteRegistryURL(autoCtx.getDefaultInstance());
        return registryURL + "atom" + path;
    }

    private OMElement getAtomFeedContent(String registryUrl) throws Exception {
        try {
            URL url = new URL(registryUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            String userPassword = USER_NAME + ":" + PASSWORD;
            String encodedAuthorization = Base64Utils.encode(userPassword.getBytes());
            connection.setRequestProperty("Authorization", "Basic " +
                                                           encodedAuthorization);
            connection.connect();

            InputStream inputStream = connection.getInputStream();
            StringBuilder sb = new StringBuilder();
            String line;

            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            } finally {
                inputStream.close();
            }

            return AXIOMUtil.stringToOM(sb.toString());
        } catch (Exception e) {
            throw new Exception("Unable to process the content ", e);
        }
    }

    @AfterClass
    public void cleanup() throws Exception {
        userManagementClient.deleteRole(ROLE_NAME);
        userManagementClient.updateUserListOfRole(PermissionTestConstants.NON_ADMIN_TEST_ROLE, ROLE_USERS, new String[]{});
        registryAdmin.delete(resourcePath);

        registry = null;
        registryAdmin = null;
        userManagementClient = null;
    }
}
