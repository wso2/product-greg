/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.wso2.carbon.registry.version.test;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.beans.xsd.VersionPath;
import org.wso2.carbon.registry.version.test.utils.VersionUtils;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.xml.sax.SAXException;

import javax.activation.DataHandler;
import javax.xml.stream.XMLStreamException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.RemoteException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class RestoreResourcesTestCase extends GREGIntegrationBaseTest {

    private ResourceAdminServiceClient resourceAdminClient;
    private static final String PATH = "/testVersionTest";
    private String sessionCookie;
    private String backEndUrl;
    private String userName;
    private String userNameWithoutDomain;


    @BeforeClass(alwaysRun = true)
    public void initializeTests() throws LoginAuthenticationExceptionException, IOException,
            XPathExpressionException, URISyntaxException, SAXException, XMLStreamException {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        backEndUrl = getBackendURL();
        sessionCookie = getSessionCookie();
        userName = automationContext.getContextTenant().getContextUser().getUserName();

        if (userName.contains("@"))
            userNameWithoutDomain = userName.substring(0, userName.indexOf('@'));
        else
            userNameWithoutDomain = userName;

        resourceAdminClient =
                new ResourceAdminServiceClient(backEndUrl,
                                               sessionCookie);
    }

    @Test(groups = {"wso2.greg"}, description = "Restore a previous version")
    public void testRestoreVersioning()
            throws ResourceAdminServiceExceptionException, RemoteException, MalformedURLException {
        Boolean status;
        String path = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator
                      + "GREG" + File.separator + "testresource.txt";
        DataHandler dataHandler = new DataHandler(new URL("file:///" + path));
        resourceAdminClient.addResource(PATH, "text/plain", "desc", dataHandler);
        VersionUtils.deleteAllVersions(resourceAdminClient, PATH);
        assertTrue(resourceAdminClient.getResource(PATH)[0].getAuthorUserName().contains(userNameWithoutDomain));
        assertEquals("This is Test Data", resourceAdminClient.getTextContent(PATH));
        resourceAdminClient.createVersion(PATH);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATH);
        String verPath = vp1[0].getCompleteVersionPath();
        assertEquals(1, vp1.length);
        String editedContent = "This is edited content";
        resourceAdminClient.updateTextContent(PATH, editedContent);
        assertEquals("This is edited content", resourceAdminClient.getTextContent(PATH));
        resourceAdminClient.createVersion(PATH);
        VersionPath[] vp2 = resourceAdminClient.getVersionPaths(PATH);
        assertEquals(2, vp2.length);
        status = resourceAdminClient.restoreVersion(verPath);
        assertTrue(status);
        assertEquals("This is Test Data", resourceAdminClient.getTextContent(PATH));
    }


    @AfterClass
    public void clear() throws ResourceAdminServiceExceptionException, RemoteException {
        resourceAdminClient.deleteResource(PATH);
        resourceAdminClient = null;
    }

}
