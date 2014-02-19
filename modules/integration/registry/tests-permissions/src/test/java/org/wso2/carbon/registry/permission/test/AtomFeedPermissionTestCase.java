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

import static org.testng.Assert.fail;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.user.mgt.UserManagementClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.core.utils.environmentutils.ProductUrlGeneratorUtil;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkFactory;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkProperties;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.registry.app.RemoteRegistry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.permission.test.utils.PermissionTestConstants;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

public class AtomFeedPermissionTestCase {
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
    private static String[] ROLE_USERS = {"testuser2"};

    public static final String resourcePath = "/rrr1";
    public RemoteRegistry registry;
    public RemoteRegistry registryAdmin;
    private ManageEnvironment environment;
    private EnvironmentBuilder builder;
    private UserInfo userInfo;
    private UserManagementClient userManagementClient;

    @BeforeClass(groups = {"wso2.greg"})
    public void init() throws Exception {
        int userId = 2;
        userInfo = UserListCsvReader.getUserInfo(userId);
        ROLE_USERS = new String[]{userInfo.getUserNameWithoutDomain()};
        builder = new EnvironmentBuilder().greg(userId);

        environment = builder.build();
        EnvironmentBuilder builderAdmin = new EnvironmentBuilder().greg(ProductConstant.ADMIN_USER_ID);
        ManageEnvironment adminEnvironment = builderAdmin.build();
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);

        environment = builder.build();

        userManagementClient = new UserManagementClient(adminEnvironment.getGreg().getBackEndUrl(),
                                                        adminEnvironment.getGreg().getSessionCookie());
        if (!userManagementClient.roleNameExists(ROLE_NAME)) {
            userManagementClient.addRole(ROLE_NAME, ROLE_USERS, PERMISSION_LIST);
        }
        userManagementClient.updateUserListOfRole(PermissionTestConstants.NON_ADMIN_ROLE, new String[]{}, ROLE_USERS);

        registryAdmin =
                new RegistryProviderUtil().getRemoteRegistry(ProductConstant.ADMIN_USER_ID,
                                                             ProductConstant.GREG_SERVER_NAME);

        registry =
                new RegistryProviderUtil().getRemoteRegistry(userId,
                                                             ProductConstant.GREG_SERVER_NAME);
        populateData();
        USER_NAME = UserListCsvReader.getUserInfo(ProductConstant.ADMIN_USER_ID).getUserName();
        PASSWORD = UserListCsvReader.getUserInfo(ProductConstant.ADMIN_USER_ID).getPassword();
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
    public void atomFeedTest() throws RegistryException {

        Resource resource = registry.get(resourcePath);
        OMElement atomFeedOMElement = getAtomFeedContent(constructAtomUrl(resourcePath));

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

    private String constructAtomUrl(String path) {

        FrameworkProperties gregProperties =
                FrameworkFactory.getFrameworkProperties(ProductConstant.GREG_SERVER_NAME);

        String registryURL;
        if (builder.getFrameworkSettings().getEnvironmentSettings().is_runningOnStratos()) {
            registryURL = ProductUrlGeneratorUtil.getRemoteRegistryURLOfStratos(environment.getGreg().getProductVariables().
                    getHttpsPort(), environment.getGreg().getProductVariables().getHostName(), gregProperties, userInfo);
        } else {
            registryURL = ProductUrlGeneratorUtil.getRemoteRegistryURLOfProducts(environment.getGreg().getProductVariables().
                    getHttpsPort(), environment.getGreg().getProductVariables().getHostName(), environment.getGreg().getProductVariables().getWebContextRoot());
        }
        return registryURL + "atom" + path;
//        return "https://" + environment.getGreg().getProductVariables().getHostName() + ":" + environment.getGreg().getProductVariables().getHttpsPort() + "/registry/atom" + path;
    }

    private OMElement getAtomFeedContent(String registryUrl) {
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
        } catch (MalformedURLException e) {
            fail("Malformed URL provided");
        } catch (IOException e) {
            fail("Unable to get the content from the URL");
        } catch (XMLStreamException e) {
            fail("Unable to convert the content to OMElement");
        }
        return null;
    }

    @AfterClass
    public void cleanup() throws Exception {
        userManagementClient.deleteRole(ROLE_NAME);
        userManagementClient.updateUserListOfRole(PermissionTestConstants.NON_ADMIN_ROLE, ROLE_USERS, new String[]{});
        registryAdmin.delete(resourcePath);

        registry = null;
        registryAdmin = null;
        environment = null;
        builder = null;
        userManagementClient = null;
    }
}
