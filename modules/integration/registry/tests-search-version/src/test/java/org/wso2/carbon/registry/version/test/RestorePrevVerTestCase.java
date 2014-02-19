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
import org.wso2.carbon.registry.resource.stub.common.xsd.ResourceData;

import javax.activation.DataHandler;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

public class RestorePrevVerTestCase {

    private ResourceAdminServiceClient resourceAdminClient;
    private static final String RESOURCE_PATH_ROOT = "/testVersion";
    private static final String RESOURCE_PATH_LEAF = "/RestoreDir1/RestoreDir2/testVersion";
    private static final String COLLECTION_PATH_LEAF = "/RestoreCol1/RestoreCol2/";
    private static final String COLLECTION_PATH_ROOT = "/";


    @BeforeClass(alwaysRun = true)
    public void initializeTests() throws LoginAuthenticationExceptionException, RemoteException,
                                         ResourceAdminServiceExceptionException,
                                         MalformedURLException {

        int userId = 2;
        UserInfo userInfo = UserListCsvReader.getUserInfo(userId);
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        ManageEnvironment environment = builder.build();

        resourceAdminClient =
                new ResourceAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                               environment.getGreg().getSessionCookie());


        resourceAdminClient.addCollection(COLLECTION_PATH_ROOT, "RestoreCollection", "text/plain", "Description 1 for collection1");
        resourceAdminClient.addCollection(COLLECTION_PATH_LEAF, "RestoreCollection3", "text/plain", "Description 1 for collection2");

        String path1 = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator
                       + "GREG" + File.separator + "testresource.txt";
        DataHandler dataHandler1 = new DataHandler(new URL("file:///" + path1));
        resourceAdminClient.addResource(RESOURCE_PATH_ROOT, "text/plain", "desc", dataHandler1);
        assertTrue(resourceAdminClient.getResource(RESOURCE_PATH_ROOT)[0].getAuthorUserName().contains(userInfo.getUserNameWithoutDomain()));

        String path2 = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator
                       + "GREG" + File.separator + "testresource.txt";
        DataHandler dataHandler2 = new DataHandler(new URL("file:///" + path2));
        resourceAdminClient.addResource(RESOURCE_PATH_LEAF, "text/plain", "desc", dataHandler2);
        assertTrue(resourceAdminClient.getResource(RESOURCE_PATH_LEAF)[0].getAuthorUserName().contains(userInfo.getUserNameWithoutDomain()));
    }

    @Test(groups = {"wso2.greg"}, description = "Restore a previous version of a resource at root level")
    public void testRestoreVersioningResourceRoot()
            throws ResourceAdminServiceExceptionException, RemoteException, MalformedURLException {
        Boolean status;

        assertEquals("This is Test Data", resourceAdminClient.getTextContent(RESOURCE_PATH_ROOT));
        resourceAdminClient.createVersion(RESOURCE_PATH_ROOT);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(RESOURCE_PATH_ROOT);
        String verPath = vp1[0].getCompleteVersionPath();
        assertEquals(1, vp1.length);
        String editedContent = "This is edited content";
        resourceAdminClient.updateTextContent(RESOURCE_PATH_ROOT, editedContent);
        assertEquals("This is edited content", resourceAdminClient.getTextContent(RESOURCE_PATH_ROOT));
        resourceAdminClient.createVersion(RESOURCE_PATH_ROOT);
        VersionPath[] vp2 = resourceAdminClient.getVersionPaths(RESOURCE_PATH_ROOT);
        assertEquals(2, vp2.length);
        status = resourceAdminClient.restoreVersion(verPath);
        assertTrue(status);
        assertEquals("This is Test Data", resourceAdminClient.getTextContent(RESOURCE_PATH_ROOT));
        assertNull(deleteVersion(RESOURCE_PATH_ROOT));
    }


    @Test(groups = {"wso2.greg"}, description = "Restore a previous version of a resource at root level")
    public void testRestoreVersioningResourceLeaf()
            throws ResourceAdminServiceExceptionException, RemoteException, MalformedURLException {
        Boolean status;

        assertEquals("This is Test Data", resourceAdminClient.getTextContent(RESOURCE_PATH_LEAF));
        resourceAdminClient.createVersion(RESOURCE_PATH_LEAF);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(RESOURCE_PATH_LEAF);
        String verPath = vp1[0].getCompleteVersionPath();
        assertEquals(1, vp1.length);
        String editedContent = "This is edited content";
        resourceAdminClient.updateTextContent(RESOURCE_PATH_LEAF, editedContent);
        assertEquals("This is edited content", resourceAdminClient.getTextContent(RESOURCE_PATH_LEAF));
        resourceAdminClient.createVersion(RESOURCE_PATH_LEAF);
        VersionPath[] vp2 = resourceAdminClient.getVersionPaths(RESOURCE_PATH_LEAF);
        assertEquals(2, vp2.length);
        status = resourceAdminClient.restoreVersion(verPath);
        assertTrue(status);
        assertEquals("This is Test Data", resourceAdminClient.getTextContent(RESOURCE_PATH_LEAF));
        assertNull(deleteVersion(RESOURCE_PATH_LEAF));
    }


    @Test(groups = {"wso2.greg"}, description = "Restore a previous version of a collection at root level")
    public void testRestoreVersioningColRoot()
            throws PropertiesAdminServiceRegistryExceptionException, RemoteException,
                   ResourceAdminServiceExceptionException, RegistryExceptionException {
        boolean status = false;
        String PATH = COLLECTION_PATH_ROOT + "RestoreCollection";
        resourceAdminClient.createVersion(PATH);
        resourceAdminClient.setDescription(PATH, "Edited description");
        ResourceData[] resource1 = resourceAdminClient.getResource(PATH);
        for (ResourceData aResource1 : resource1) {
            if (aResource1.getDescription().equals("Edited description")) {
                status = true;
            }
        }
        assertTrue(status);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATH);
        String verPath = vp1[0].getCompleteVersionPath();
        resourceAdminClient.restoreVersion(verPath);
        status = false;
        ResourceData[] resource2 = resourceAdminClient.getResource(PATH);
        for (ResourceData aResource2 : resource2) {
            if (aResource2.getDescription().equals("Description 1 for collection1")) {
                status = true;
            }
        }
        assertTrue(status);
        assertNull(deleteVersion(PATH));
    }


    @Test(groups = {"wso2.greg"}, description = "Restore a previous version of a collection at root level")
    public void testRestoreVersioningColLeaf()
            throws PropertiesAdminServiceRegistryExceptionException, RemoteException,
                   ResourceAdminServiceExceptionException, RegistryExceptionException {
        boolean status = false;
        String PATH = COLLECTION_PATH_LEAF + "RestoreCollection3";
        resourceAdminClient.createVersion(PATH);
        resourceAdminClient.setDescription(PATH, "Edited description");
        ResourceData[] resource1 = resourceAdminClient.getResource(PATH);
        for (ResourceData aResource1 : resource1) {
            if (aResource1.getDescription().equals("Edited description")) {
                status = true;
            }
        }
        assertTrue(status);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATH);
        String verPath = vp1[0].getCompleteVersionPath();
        resourceAdminClient.restoreVersion(verPath);
        status = false;
        ResourceData[] resource2 = resourceAdminClient.getResource(PATH);
        for (ResourceData aResource2 : resource2) {
            if (aResource2.getDescription().equals("Description 1 for collection2")) {
                status = true;
            }
        }
        assertTrue(status);
        assertNull(deleteVersion(PATH));
    }


    public VersionPath[] deleteVersion(String path)
            throws ResourceAdminServiceExceptionException, RemoteException {
        int length = resourceAdminClient.getVersionPaths(path).length;
        for (int i = 0; i < length; i++) {
            long versionNo = resourceAdminClient.getVersionPaths(path)[0].getVersionNumber();
            String snapshotId = String.valueOf(versionNo);
            resourceAdminClient.deleteVersionHistory(path, snapshotId);
        }
        VersionPath[] vp2;
        vp2 = resourceAdminClient.getVersionPaths(path);
        return vp2;
    }

    @AfterClass
    public void clear() throws ResourceAdminServiceExceptionException, RemoteException {
        resourceAdminClient.deleteResource(RESOURCE_PATH_ROOT);
        resourceAdminClient.deleteResource(COLLECTION_PATH_ROOT + "RestoreCollection");
        resourceAdminClient.deleteResource(COLLECTION_PATH_LEAF + "RestoreCollection3");
        resourceAdminClient.deleteResource("/RestoreCol1");
        resourceAdminClient.deleteResource("/RestoreDir1");
        resourceAdminClient = null;
    }
}
