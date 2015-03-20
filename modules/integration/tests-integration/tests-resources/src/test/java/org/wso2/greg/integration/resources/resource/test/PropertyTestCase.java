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

package org.wso2.greg.integration.resources.resource.test;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.registry.properties.stub.PropertiesAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.greg.integration.common.clients.PropertiesAdminServiceClient;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.xml.sax.SAXException;

import javax.activation.DataHandler;
import javax.xml.stream.XMLStreamException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;

public class PropertyTestCase extends GREGIntegrationBaseTest {

    private ResourceAdminServiceClient resourceAdminServiceClient;

    @BeforeClass(groups = {"wso2.greg"}, alwaysRun = true)
    public void initialize() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        //org.wso2.carbon.automation.extensions.servers..carbonserver.CarbonServerExtension
    }

    @Test(groups = "wso2.greg", description = "Add property to resource")
    public void testAddResource() throws Exception {
        resourceAdminServiceClient = new ResourceAdminServiceClient(getBackendURL(), getSessionCookie());
        String resourcePath = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator + File.separator + "GREG" + File.separator + "txt" + File.separator + "resource.txt";
        DataHandler dh = new DataHandler(new URL("file:///" + resourcePath));
        resourceAdminServiceClient.addResource("/_system/config/testResource", "test/plain", "testDesc", dh);
        assertTrue(resourceAdminServiceClient.getResource("/_system/config/testResource")[0].getAuthorUserName().contains(automationContext.getContextTenant().getContextUser().getUserName()));
    }

    @Test(groups = "wso2.greg", description = "add property", dependsOnMethods = "testAddResource")
    public void testPropertyAddition() throws Exception {
        PropertiesAdminServiceClient propertyPropertiesAdminServiceClient = new PropertiesAdminServiceClient(getBackendURL(), getSessionCookie());
        propertyPropertiesAdminServiceClient.setProperty("/_system/config/testResource", "Author", "TestValue");
        assertTrue(propertyPropertiesAdminServiceClient.getProperty("/_system/config/testResource", "true").getProperties()[0].getKey().equals("Author"));
        assertTrue(propertyPropertiesAdminServiceClient.getProperty("/_system/config/testResource", "true").getProperties()[0].getValue().equals("TestValue"));
    }

    @AfterClass(groups = {"wso2.greg"}, alwaysRun = true)
    public void testCleanup() throws ResourceAdminServiceExceptionException, RemoteException {
        resourceAdminServiceClient.deleteResource("/_system/config/testResource");
        resourceAdminServiceClient = null;
    }
}
