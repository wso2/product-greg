/*
Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.

WSO2 Inc. licenses this file to you under the Apache License,
Version 2.0 (the "License"); you may not use this file except
in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
*/

package org.wso2.carbon.registry.version.test;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.registry.RelationAdminServiceClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.relations.stub.AddAssociationRegistryExceptionException;
import org.wso2.carbon.registry.relations.stub.beans.xsd.AssociationTreeBean;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.beans.xsd.VersionPath;

import javax.activation.DataHandler;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

public class ResourceTestCase {


    private UserInfo userInfo;
    private ResourceAdminServiceClient resourceAdminClient;
    private static final String PATH = "/testVersion";
    private static final String PATH2 = "/testVersion2";
    private static final String PATH3 = "/testVersion3";
    private static final String CSS_PATH = "/testCSS";
    private static final String LEAF_PATH = "/_system/branch1/branch2/leafTest";
    private RelationAdminServiceClient relationAdminServiceClient;

    @BeforeClass(alwaysRun = true)
    public void initializeTests() throws LoginAuthenticationExceptionException, RemoteException,
                                         MalformedURLException,
                                         ResourceAdminServiceExceptionException {

        int userId = 2;
        userInfo = UserListCsvReader.getUserInfo(userId);
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        ManageEnvironment environment = builder.build();
        resourceAdminClient =
                new ResourceAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                               environment.getGreg().getSessionCookie());
        relationAdminServiceClient =
                new RelationAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                               environment.getGreg().getSessionCookie());
        relationAdminServiceClient =
                new RelationAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                               environment.getGreg().getSessionCookie());


        String path = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator
                      + "GREG" + File.separator + "testresource.txt";
        DataHandler dataHandler = new DataHandler(new URL("file:///" + path));
        resourceAdminClient.addResource(LEAF_PATH, "text/plain", "desc", dataHandler);
        assertTrue(resourceAdminClient.getResource(LEAF_PATH)[0].getAuthorUserName().contains(userInfo.getUserNameWithoutDomain()));

        String path2 = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator
                       + "GREG" + File.separator + "testresource.txt";
        DataHandler dataHandler2 = new DataHandler(new URL("file:///" + path2));
        resourceAdminClient.addResource(PATH, "text/plain", "desc", dataHandler2);
        assertTrue(resourceAdminClient.getResource(PATH)[0].getAuthorUserName().contains(userInfo.getUserNameWithoutDomain()));

        String path3 = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator
                       + "GREG" + File.separator + "testresource.txt";
        DataHandler dataHandler3 = new DataHandler(new URL("file:///" + path3));
        resourceAdminClient.addResource(PATH2, "text/plain", "desc", dataHandler3);
        assertTrue(resourceAdminClient.getResource(PATH2)[0].getAuthorUserName().contains(userInfo.getUserNameWithoutDomain()));
    }


    @Test(groups = {"wso2.greg"}, description = "Create new resource at root level   and create a version for it")
    public void testAddResourceRoot()
            throws RegistryException, MalformedURLException, ResourceAdminServiceExceptionException,
                   RemoteException {

        resourceAdminClient.createVersion(PATH);

        VersionPath[] vp = resourceAdminClient.getVersionPaths(PATH);
        assertEquals(1, vp.length);
        assertNull(deleteVersion(PATH));
    }

    @Test(groups = {"wso2.greg"}, description = "Create new resource at leaf level   and create a version for it")
    public void testAddResourceLeaf()
            throws ResourceAdminServiceExceptionException, RemoteException, MalformedURLException {

        resourceAdminClient.createVersion(LEAF_PATH);
        VersionPath[] vp = resourceAdminClient.getVersionPaths(LEAF_PATH);
        assertEquals(1, vp.length);
        assertNull(deleteVersion(LEAF_PATH));
    }


    @Test(groups = {"wso2.greg"}, description = "Edit a resource and version it")
    public void testEditResourceVersioning()
            throws ResourceAdminServiceExceptionException, RemoteException, MalformedURLException {

        resourceAdminClient.createVersion(PATH2);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATH2);
        assertEquals(1, vp1.length);

        String editedContent = "This is edited content";
        resourceAdminClient.updateTextContent(PATH2, editedContent);
        resourceAdminClient.createVersion(PATH2);
        VersionPath[] vp2 = resourceAdminClient.getVersionPaths(PATH2);
        assertEquals(2, vp2.length);
        assertNull(deleteVersion(PATH2));
    }

    @Test(groups = {"wso2.greg"}, description = "Delete version at root level resource")
    public void testDeleteVerRoot()
            throws ResourceAdminServiceExceptionException, RemoteException, MalformedURLException {
        String path = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator
                      + "GREG" + File.separator + "testresource.txt";
        DataHandler dataHandler = new DataHandler(new URL("file:///" + path));
        resourceAdminClient.addResource(PATH3, "text/plain", "desc", dataHandler);
        assertTrue(resourceAdminClient.getResource(PATH3)[0].getAuthorUserName().contains(userInfo.getUserNameWithoutDomain()));
        resourceAdminClient.createVersion(PATH3);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATH3);
        assertEquals(1, vp1.length);
        long versionNo = resourceAdminClient.getVersionPaths(PATH3)[0].getVersionNumber();
        String snapshotId = String.valueOf(versionNo);
        resourceAdminClient.deleteVersionHistory(PATH3, snapshotId);
        VersionPath[] vp2;
        vp2 = resourceAdminClient.getVersionPaths(PATH3);
        assertNull(vp2);
    }

    @Test(groups = {"wso2.greg"}, description = "add resources with different media types")
    public void testAddResourceDiff()
            throws ResourceAdminServiceExceptionException, RemoteException, MalformedURLException {
        String path = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator
                      + "GREG" + File.separator + "test.css";
        DataHandler dataHandler = new DataHandler(new URL("file:///" + path));
        resourceAdminClient.addResource(CSS_PATH, "text/css", "desc", dataHandler);
        assertTrue(resourceAdminClient.getResource(CSS_PATH)[0].getAuthorUserName().contains(userInfo.getUserNameWithoutDomain()));
        resourceAdminClient.createVersion(CSS_PATH);
        VersionPath[] vp = resourceAdminClient.getVersionPaths(CSS_PATH);
        assertEquals(1, vp.length);
        assertNull(deleteVersion(CSS_PATH));
    }


    @Test(groups = {"wso2.greg"}, description = "create a resource at leaf level and check association with versioning ")
    public void testAssociations() throws ResourceAdminServiceExceptionException, RemoteException,
                                          AddAssociationRegistryExceptionException {
        String PATH_1 = LEAF_PATH;
        String PATH_2 = PATH;
        String PATH_3 = PATH2;
        relationAdminServiceClient.addAssociation(PATH_1, "usedBy", PATH_2, "add");
        AssociationTreeBean associationTreeBean = relationAdminServiceClient.getAssociationTree(PATH_1, "usedBy");

        String resourcePath = associationTreeBean.getAssociationTree();
        assertTrue(resourcePath.contains(PATH_2));
        resourceAdminClient.createVersion(PATH_1);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATH_1);
        String verPath = vp1[0].getCompleteVersionPath();
        assertTrue(relationAdminServiceClient.getAssociationTree(verPath, "usedBy").getAssociationTree().contains(PATH_2));
        relationAdminServiceClient.addAssociation(PATH_1, "usedBy", PATH_3, "add");
        AssociationTreeBean associationTreeBean2 = relationAdminServiceClient.getAssociationTree(PATH_1, "usedBy");
        String resourcePath2 = associationTreeBean2.getAssociationTree();
        assertTrue(resourcePath2.contains(PATH_3));
        resourceAdminClient.createVersion(PATH_1);
        VersionPath[] vp2 = resourceAdminClient.getVersionPaths(PATH_1);
        String verPath2 = vp2[0].getCompleteVersionPath();
        assertTrue(relationAdminServiceClient.getAssociationTree(verPath2, "usedBy").getAssociationTree().contains(PATH_2));
        assertTrue(relationAdminServiceClient.getAssociationTree(verPath2, "usedBy").getAssociationTree().contains(PATH_3));
        resourceAdminClient.restoreVersion(verPath);
        boolean contains;
        contains = !(relationAdminServiceClient.getAssociationTree(PATH_1, "usedBy").getAssociationTree().contains(PATH_3));
        assertTrue(contains);
        assertNull(deleteVersion(PATH_1));
    }


    //Delete created versions
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
        resourceAdminClient.deleteResource(PATH);
        resourceAdminClient.deleteResource(PATH2);
        resourceAdminClient.deleteResource(PATH3);
        resourceAdminClient.deleteResource(LEAF_PATH);
        resourceAdminClient.deleteResource(CSS_PATH);
        resourceAdminClient.deleteResource("/_system/branch1");
        resourceAdminClient = null;
        relationAdminServiceClient = null;
    }
}
