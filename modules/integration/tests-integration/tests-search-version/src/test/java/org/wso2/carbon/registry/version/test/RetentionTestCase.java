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

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.registry.info.stub.RegistryExceptionException;
import org.wso2.carbon.registry.properties.stub.PropertiesAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.beans.xsd.VersionPath;
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

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class RetentionTestCase extends GREGIntegrationBaseTest {


    private String RESOURCE_NAME = "/retentionResource";
    private String COLLECTION_NAME = "/RetentionCol1/RetetionCol2/";
    private ResourceAdminServiceClient resourceAdminClient1;
    private ResourceAdminServiceClient resourceAdminClient2;
    private PropertiesAdminServiceClient propertiesAdminServiceClient;
    private String sessionCookie;
    private String backEndUrl;
    private String userName;
    private String userNameWithoutDomain;


    @BeforeClass(alwaysRun = true)
    public void initializeTests() throws LoginAuthenticationExceptionException, IOException,
            ResourceAdminServiceExceptionException, XPathExpressionException, URISyntaxException, SAXException, XMLStreamException {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        backEndUrl = getBackendURL();
        sessionCookie = getSessionCookie();
        userName = automationContext.getContextTenant().getContextUser().getUserName();

        if (userName.contains("@"))
            userNameWithoutDomain = userName.substring(0, userName.indexOf('@'));
        else
            userNameWithoutDomain = userName;

        resourceAdminClient1 = new ResourceAdminServiceClient(backEndUrl, sessionCookie);
        resourceAdminClient2 = new ResourceAdminServiceClient(backEndUrl, sessionCookie);
        propertiesAdminServiceClient = new PropertiesAdminServiceClient(backEndUrl, sessionCookie);

        String path1 = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator
                       + "GREG" + File.separator + "testresource.txt";
        DataHandler dataHandler1 = new DataHandler(new URL("file:///" + path1));
        resourceAdminClient1.addResource(RESOURCE_NAME, "text/plain", "desc", dataHandler1);
        assertTrue(resourceAdminClient1.getResource(RESOURCE_NAME)[0].getAuthorUserName().contains(userNameWithoutDomain));
        resourceAdminClient1.addCollection(COLLECTION_NAME, "dir1", "text/plain", "Description 1 for collection");
    }

    @Test(groups = {"wso2.greg"}, description = "Check whether a resource under retention can be restored from a previous version")
    public void testSymlinkRootResource()
            throws ResourceAdminServiceExceptionException,
                   RegistryExceptionException,
                   PropertiesAdminServiceRegistryExceptionException {

        try {
            resourceAdminClient1.createVersion(RESOURCE_NAME);
        } catch (RemoteException e) {
            assertFalse("Failed to create version for resource " + RESOURCE_NAME, true);
            e.printStackTrace();
        }
        try {
            resourceAdminClient1.setDescription(RESOURCE_NAME, "Edited Description");
        } catch (RemoteException e) {
            assertFalse("Failed to add description " + RESOURCE_NAME, true);
            e.printStackTrace();
        }
        VersionPath[] vp1 = new VersionPath[0];
        try {
            vp1 = resourceAdminClient1.getVersionPaths(RESOURCE_NAME);
        } catch (RemoteException e) {
            assertFalse("Failed to get version path " + RESOURCE_NAME, true);
            e.printStackTrace();
        }
        String verPath = vp1[0].getCompleteVersionPath();
        try {
            propertiesAdminServiceClient.setRetentionProperties(RESOURCE_NAME, "write", "07/02/2012", "09/22/2040");
        } catch (RemoteException e) {
            assertFalse("Failed to get retention properties " + RESOURCE_NAME, true);
            e.printStackTrace();
        }
        try {
            Assert.assertEquals("07/02/2012", propertiesAdminServiceClient.getRetentionProperties(RESOURCE_NAME).getFromDate());
        } catch (RemoteException e) {
            assertFalse("Failed to get retention properties - getFromDate  " + RESOURCE_NAME, true);
            e.printStackTrace();
        }
        try {
            Assert.assertEquals("09/22/2040", propertiesAdminServiceClient.getRetentionProperties(RESOURCE_NAME).getToDate());

        } catch (RemoteException e) {
            assertFalse("Failed to get retention properties -getToDate  " + RESOURCE_NAME, true);
            e.printStackTrace();
        }
        try {
            resourceAdminClient2.restoreVersion(verPath);
            assertFalse("Restored resource under retention", true);
        } catch (RemoteException e) {
            assertTrue("Failed to restore resource under retention (it is expected behaviour)" + verPath, true);
            e.printStackTrace();
        }
    }

    @AfterClass
    public void clear() throws ResourceAdminServiceExceptionException, RemoteException {
        resourceAdminClient1.deleteResource("/RetentionCol1");
        resourceAdminClient1.deleteResource(RESOURCE_NAME);

        resourceAdminClient1 = null;
        resourceAdminClient2 = null;
        propertiesAdminServiceClient = null;
    }

}
