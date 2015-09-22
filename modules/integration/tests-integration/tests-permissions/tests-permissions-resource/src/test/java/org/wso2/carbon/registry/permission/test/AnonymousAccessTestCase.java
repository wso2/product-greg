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

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.authenticator.stub.LogoutAuthenticationExceptionException;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.registry.permission.test.utils.PermissionTestConstants;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.xml.sax.SAXException;

import javax.activation.DataHandler;
import javax.xml.stream.XMLStreamException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.RemoteException;

public class AnonymousAccessTestCase extends GREGIntegrationBaseTest {
    private static final String NEW_RESOURCE_PATH = "/_system/config/testNonRoot.txt";
    private static final String EXISTING_RESOURCE_PATH = "/_system/config/repository/components/" +
                                                         "org.wso2.carbon.governance/configuration/uri";
    private String serverUrl;
    private ResourceAdminServiceClient resourceAdminClient;

    @BeforeClass(alwaysRun = true)
    public void initialize() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String sessionCookie = getSessionCookie();

        resourceAdminClient = new ResourceAdminServiceClient(backendURL, sessionCookie);
        String backEndUrl = backendURL;
        backEndUrl = backEndUrl.substring(0, backEndUrl.lastIndexOf("/"));
        serverUrl = backEndUrl.substring(0, backEndUrl.lastIndexOf("/"));
        String resourcePath = getTestArtifactLocation() + "artifacts" + File.separator
                              + "GREG" + File.separator + "resource.txt";
        DataHandler dataHandler = new DataHandler(new URL("file:///" + resourcePath));
        resourceAdminClient.addResource(NEW_RESOURCE_PATH, "text/plain", "Dummy non root file", dataHandler);
    }

    @Test(groups = "wso2.greg", expectedExceptions = IOException.class,
          description = "Test Anonymous access to an existing resource file")
    public void testExistingResourceAccess() throws IOException {
        URL existingResourceURL = new URL(serverUrl + PermissionTestConstants.WEB_APP_RESOURCE_URL +
                                          EXISTING_RESOURCE_PATH);
        InputStream existingResourceStream = existingResourceURL.openStream();
    }

    @Test(groups = "wso2.greg", expectedExceptions = IOException.class,
          description = "Test Anonymous access to new root level resources", dependsOnMethods = "testExistingResourceAccess")
    public void testNewResourceAnonAccess() throws IOException {
        URL resourceURL = new URL(serverUrl + PermissionTestConstants.WEB_APP_RESOURCE_URL + NEW_RESOURCE_PATH);
        InputStream resourceStream = resourceURL.openStream();
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() throws ResourceAdminServiceExceptionException, RemoteException {
        resourceAdminClient.deleteResource(NEW_RESOURCE_PATH);
        resourceAdminClient = null;

    }
}
