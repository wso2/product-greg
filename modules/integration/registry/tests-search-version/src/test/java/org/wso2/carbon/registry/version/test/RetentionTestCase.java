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
import org.wso2.carbon.automation.api.clients.registry.PropertiesAdminServiceClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.registry.info.stub.RegistryExceptionException;
import org.wso2.carbon.registry.properties.stub.PropertiesAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.beans.xsd.VersionPath;

import javax.activation.DataHandler;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class RetentionTestCase {


    private String RESOURCE_NAME = "/retentionResource";
    private String COLLECTION_NAME = "/RetentionCol1/RetetionCol2/";
    private ResourceAdminServiceClient resourceAdminClient1;
    private ResourceAdminServiceClient resourceAdminClient2;
    private PropertiesAdminServiceClient propertiesAdminServiceClient;


    @BeforeClass(alwaysRun = true)
    public void initializeTests() throws LoginAuthenticationExceptionException, RemoteException,
                                         MalformedURLException,
                                         ResourceAdminServiceExceptionException {
        int userId = 2;
        UserInfo userInfo = UserListCsvReader.getUserInfo(userId);
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        ManageEnvironment environment = builder.build();
        int userId2 = ProductConstant.ADMIN_USER_ID;
        UserInfo userInfo2 = UserListCsvReader.getUserInfo(userId2);
        EnvironmentBuilder builder2 = new EnvironmentBuilder().greg(userId2);
        ManageEnvironment environment2 = builder2.build();

        resourceAdminClient1 =
                new ResourceAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                               environment.getGreg().getSessionCookie());
        resourceAdminClient2 =
                new ResourceAdminServiceClient(environment2.getGreg().getBackEndUrl(),
                                               environment2.getGreg().getSessionCookie());
        propertiesAdminServiceClient = new PropertiesAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                                                        environment.getGreg().getSessionCookie());
        String path1 = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator
                       + "GREG" + File.separator + "testresource.txt";
        DataHandler dataHandler1 = new DataHandler(new URL("file:///" + path1));
        resourceAdminClient1.addResource(RESOURCE_NAME, "text/plain", "desc", dataHandler1);
        assertTrue(resourceAdminClient1.getResource(RESOURCE_NAME)[0].getAuthorUserName().contains(userInfo.getUserNameWithoutDomain()));
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
            assertEquals("07/02/2012", propertiesAdminServiceClient.getRetentionProperties(RESOURCE_NAME).getFromDate());
        } catch (RemoteException e) {
            assertFalse("Failed to get retention properties - getFromDate  " + RESOURCE_NAME, true);
            e.printStackTrace();
        }
        try {
            assertEquals("09/22/2040", propertiesAdminServiceClient.getRetentionProperties(RESOURCE_NAME).getToDate());

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
