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

package org.wso2.carbon.registry.resource.test;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.registry.PropertiesAdminServiceClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.registry.properties.stub.PropertiesAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;

import javax.activation.DataHandler;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;

public class PropertyTestCase {
    private ManageEnvironment environment;
    private int userId = 2;
    private UserInfo userInfo;
    private ResourceAdminServiceClient resourceAdminServiceClient;

    @BeforeClass()
    public void initialize() throws LoginAuthenticationExceptionException, RemoteException {
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        environment = builder.build();
    }

    @Test(groups = "wso2.greg", description = "Add property to resource")
    public void testAddResource()
            throws RemoteException, MalformedURLException, ResourceAdminServiceExceptionException {
        userInfo = UserListCsvReader.getUserInfo(userId);
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                               environment.getGreg().getSessionCookie());
        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
                              File.separator + "GREG" + File.separator + "resource.txt";

        DataHandler dh = new DataHandler(new URL("file:///" + resourcePath));
        resourceAdminServiceClient.addResource("/_system/config/testResource", "test/plain", "testDesc", dh);

        assertTrue(resourceAdminServiceClient.getResource("/_system/config/testResource")[0].getAuthorUserName()
                           .contains(userInfo.getUserNameWithoutDomain()));


    }

    @Test(groups = "wso2.greg", description = "add property", dependsOnMethods = "testAddResource")
    public void testPropertyAddition()
            throws RemoteException, PropertiesAdminServiceRegistryExceptionException {
        PropertiesAdminServiceClient propertyPropertiesAdminServiceClient =
                new PropertiesAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                                 environment.getGreg().getSessionCookie());
        propertyPropertiesAdminServiceClient.setProperty("/_system/config/testResource", "Author", "TestValue");
        assertTrue(propertyPropertiesAdminServiceClient.getProperty("/_system/config/testResource",
                                                                    "true").getProperties()[0].getKey().equals("Author"));
        assertTrue(propertyPropertiesAdminServiceClient.getProperty("/_system/config/testResource",
                                                                    "true").getProperties()[0].getValue().equals("TestValue"));

    }

    @AfterClass
    public void testCleanup() throws ResourceAdminServiceExceptionException, RemoteException {
        resourceAdminServiceClient.deleteResource("/_system/config/testResource");
        resourceAdminServiceClient = null;
        environment = null;
        userInfo=null;
    }
}
