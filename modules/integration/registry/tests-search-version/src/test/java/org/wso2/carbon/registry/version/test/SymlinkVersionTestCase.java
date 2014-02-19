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
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.info.stub.RegistryExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.beans.xsd.VersionPath;
import org.wso2.carbon.registry.resource.stub.common.xsd.ResourceData;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import javax.activation.DataHandler;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class SymlinkVersionTestCase {

    private String RESOURCE_NAME_ROOT = "/symlinkResource1";
    private String RESOURCE_NAME_LEAF = "/SymBranch1/SymBranch2/symlinkResource2";
    private String COLLECTION_NAME_LEAF = "/SymCol1/SymCol2/";
    private String COLLECTION_NAME_ROOT = "/";
    private String TARGET = "/_system";
    private ResourceAdminServiceClient resourceAdminClient;
    private WSRegistryServiceClient wsRegistryServiceClient;

    @BeforeClass(alwaysRun = true)
    public void initializeTests()
            throws LoginAuthenticationExceptionException, RemoteException, MalformedURLException,
                   ResourceAdminServiceExceptionException, RegistryException {
        int userId = 2;
        UserInfo userInfo = UserListCsvReader.getUserInfo(userId);
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        ManageEnvironment environment = builder.build();


        resourceAdminClient =
                new ResourceAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                               environment.getGreg().getSessionCookie());


        String path1 = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator
                       + "GREG" + File.separator + "testresource.txt";
        DataHandler dataHandler1 = new DataHandler(new URL("file:///" + path1));
        resourceAdminClient.addResource(RESOURCE_NAME_ROOT, "text/plain", "desc", dataHandler1);
        assertTrue(resourceAdminClient.getResource(RESOURCE_NAME_ROOT)[0].getAuthorUserName().contains(userInfo.getUserNameWithoutDomain()));

        String path2 = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator
                       + "GREG" + File.separator + "testresource.txt";
        DataHandler dataHandler2 = new DataHandler(new URL("file:///" + path2));
        resourceAdminClient.addResource(RESOURCE_NAME_LEAF, "text/plain", "desc", dataHandler2);
        assertTrue(resourceAdminClient.getResource(RESOURCE_NAME_LEAF)[0].getAuthorUserName().contains(userInfo.getUserNameWithoutDomain()));

        resourceAdminClient.addCollection(COLLECTION_NAME_ROOT, "dir1", "text/plain", "Description 1 for collection1");
        resourceAdminClient.addCollection(COLLECTION_NAME_LEAF, "dir2", "text/plain", "Description 1 for collection2");

        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        wsRegistryServiceClient =
                registryProviderUtil.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);


    }

    @Test(groups = {"wso2.greg"}, description = "Create new resource at root level and add a symlink " +
                                                "to the resource.Check versions of both symlink adn actual resource")
    public void testSymlinkRootResource()
            throws ResourceAdminServiceExceptionException, RemoteException,
                   RegistryExceptionException {
        // resourceAdminClient.addSymbolicLink(TARGET,"symlink_to_dir1",COLLECTION_NAME_ROOT+"dir1");
        boolean status = false;

        String symlinkPath = TARGET + "/symlink_to_symlinkResource1";
        //add a symlink to a resource at root leevel
        resourceAdminClient.addSymbolicLink(TARGET, "symlink_to_symlinkResource1", RESOURCE_NAME_ROOT);
        // Check whether the description is there in sumlink
        ResourceData[] resource1 = resourceAdminClient.getResource(symlinkPath);
        for (ResourceData aResource1 : resource1) {
            if (aResource1.getDescription().equals("desc")) {
                status = true;
            }
        }
        assertTrue(status);
        //create a check point of the resource
        resourceAdminClient.createVersion(RESOURCE_NAME_ROOT);
        //edit resource description
        resourceAdminClient.setDescription(RESOURCE_NAME_ROOT, "Edited discription");
        // Check whether the edited description is there in sumlink
        ResourceData[] resource2 = resourceAdminClient.getResource(symlinkPath);
        status = false;
        for (ResourceData aResource2 : resource2) {
            if (aResource2.getDescription().equals("Edited discription")) {
                status = true;
            }
        }
        assertTrue(status);


        // Check whether the edited description is there in resource
        ResourceData[] resource3 = resourceAdminClient.getResource(RESOURCE_NAME_ROOT);
        status = false;
        for (ResourceData aResource3 : resource3) {
            if (aResource3.getDescription().equals("Edited discription")) {
                status = true;
            }
        }
        assertTrue(status);
        //restore to previous version
        resourceAdminClient.restoreVersion(resourceAdminClient.getVersionPaths(RESOURCE_NAME_ROOT)[0].getCompleteVersionPath());
        //check whether the previous description is stored in simlink
        ResourceData[] resource4 = resourceAdminClient.getResource(symlinkPath);
        status = false;
        for (ResourceData aResource4 : resource4) {
            if (aResource4.getDescription().equals("desc")) {
                status = true;
            }
        }
        assertTrue(status);
        //check whether the previous description is stored in resource
        ResourceData[] resource5 = resourceAdminClient.getResource(RESOURCE_NAME_ROOT);
        status = false;
        for (ResourceData aResource5 : resource5) {
            if (aResource5.getDescription().equals("desc")) {
                status = true;
            }
        }
        assertTrue(status);
        assertEquals(null, deleteVersion(RESOURCE_NAME_ROOT));


    }


    @Test(groups = {"wso2.greg"}, description = "Create new resource at leaf level and add a symlink " +
                                                "to the resource.Check versions of both symlink adn actual resource")
    public void testSymlinkLeafResource()
            throws ResourceAdminServiceExceptionException, RemoteException,
                   RegistryExceptionException {
        // resourceAdminClient.addSymbolicLink(TARGET,"symlink_to_dir1",COLLECTION_NAME_ROOT+"dir1");
        boolean status = false;

        String symlinkPath = TARGET + "/symlink_to_symlinkResource2";
        //add a symlink to a resource at root leevel
        resourceAdminClient.addSymbolicLink(TARGET, "symlink_to_symlinkResource2", RESOURCE_NAME_LEAF);
        // Check whether the description is there in sumlink
        ResourceData[] resource1 = resourceAdminClient.getResource(symlinkPath);
        for (ResourceData aResource1 : resource1) {
            if (aResource1.getDescription().equals("desc")) {
                status = true;
            }
        }
        assertTrue(status);
        //create a check point of the resource
        resourceAdminClient.createVersion(RESOURCE_NAME_LEAF);
        //edit resource description
        resourceAdminClient.setDescription(RESOURCE_NAME_LEAF, "Edited discription");
        // Check whether the edited description is there in sumlink
        ResourceData[] resource2 = resourceAdminClient.getResource(symlinkPath);
        status = false;
        for (ResourceData aResource2 : resource2) {
            if (aResource2.getDescription().equals("Edited discription")) {
                status = true;
            }
        }
        assertTrue(status);


        // Check whether the edited description is there in resource
        ResourceData[] resource3 = resourceAdminClient.getResource(RESOURCE_NAME_LEAF);
        status = false;
        for (ResourceData aResource3 : resource3) {
            if (aResource3.getDescription().equals("Edited discription")) {
                status = true;
            }
        }
        assertTrue(status);
        //restore to previous version
        resourceAdminClient.restoreVersion(resourceAdminClient.getVersionPaths(RESOURCE_NAME_LEAF)[0].getCompleteVersionPath());
        //check whether the previous description is stored in simlink
        ResourceData[] resource4 = resourceAdminClient.getResource(symlinkPath);
        status = false;
        for (ResourceData aResource4 : resource4) {
            if (aResource4.getDescription().equals("desc")) {
                status = true;
            }
        }
        assertTrue(status);
        //check whether the previous description is stored in resource
        ResourceData[] resource5 = resourceAdminClient.getResource(RESOURCE_NAME_LEAF);
        status = false;
        for (ResourceData aResource5 : resource5) {
            if (aResource5.getDescription().equals("desc")) {
                status = true;
            }
        }
        assertTrue(status);
        assertEquals(null, deleteVersion(RESOURCE_NAME_LEAF));


    }


    @Test(groups = {"wso2.greg"}, description = "Create new collection at root level and add a symlink " +
                                                "to the resource.Check versions of both symlink adn actual resource")
    public void testSymlinkRootCollection()
            throws ResourceAdminServiceExceptionException, RemoteException,
                   RegistryExceptionException {
        // resourceAdminClient.addSymbolicLink(TARGET,"symlink_to_dir1",COLLECTION_NAME_ROOT+"dir1");
        boolean status = false;

        String symlinkPath = TARGET + "/symlink_to_dir1";
        //add a symlink to a resource at root leevel
        resourceAdminClient.addSymbolicLink(TARGET, "symlink_to_dir1", COLLECTION_NAME_ROOT + "dir1");
        // Check whether the description is there in sumlink
        ResourceData[] resource1 = resourceAdminClient.getResource(symlinkPath);
        for (ResourceData aResource1 : resource1) {
            if (aResource1.getDescription().equals("Description 1 for collection1")) {
                status = true;
            }
        }
        assertTrue(status);
        //create a check point of the resource
        resourceAdminClient.createVersion(COLLECTION_NAME_ROOT + "dir1");
        //edit resource description
        resourceAdminClient.setDescription(COLLECTION_NAME_ROOT + "dir1", "Edited discription");
        // Check whether the edited description is there in sumlink
        ResourceData[] resource2 = resourceAdminClient.getResource(symlinkPath);
        status = false;
        for (ResourceData aResource2 : resource2) {
            if (aResource2.getDescription().equals("Edited discription")) {
                status = true;
            }
        }
        assertTrue(status);

        // Check whether the edited description is there in resource
        ResourceData[] resource3 = resourceAdminClient.getResource(COLLECTION_NAME_ROOT + "dir1");
        status = false;
        for (ResourceData aResource3 : resource3) {
            if (aResource3.getDescription().equals("Edited discription")) {
                status = true;
            }
        }
        assertTrue(status);
        //restore to previous version
        resourceAdminClient.restoreVersion(resourceAdminClient.getVersionPaths(COLLECTION_NAME_ROOT + "dir1")[0].getCompleteVersionPath());
        //check whether the previous description is stored in simlink
        ResourceData[] resource4 = resourceAdminClient.getResource(symlinkPath);
        status = false;
        for (ResourceData aResource4 : resource4) {
            if (aResource4.getDescription().equals("Description 1 for collection1")) {
                status = true;
            }
        }
        assertTrue(status);
        //check whether the previous description is stored in resource
        ResourceData[] resource5 = resourceAdminClient.getResource(COLLECTION_NAME_ROOT + "dir1");
        status = false;
        for (ResourceData aResource5 : resource5) {
            if (aResource5.getDescription().equals("Description 1 for collection1")) {
                status = true;
            }
        }
        assertTrue(status);
        assertEquals(null, deleteVersion(COLLECTION_NAME_ROOT + "dir1"));


    }


    @Test(groups = {"wso2.greg"}, description = "Create new collection at leaf level and add a symlink to the resource.Check versions of both symlink adn actual resource")
    public void testSymlinkLeafCollection()
            throws ResourceAdminServiceExceptionException, RemoteException,
                   RegistryExceptionException {
        // resourceAdminClient.addSymbolicLink(TARGET,"symlink_to_dir1",COLLECTION_NAME_ROOT+"dir1");
        boolean status = false;

        String symlinkPath = TARGET + "/symlink_to_dir2";
        //add a symlink to a resource at root leevel
        resourceAdminClient.addSymbolicLink(TARGET, "symlink_to_dir2", COLLECTION_NAME_LEAF + "dir2");
        // Check whether the description is there in sumlink
        ResourceData[] resource1 = resourceAdminClient.getResource(symlinkPath);
        for (ResourceData aResource1 : resource1) {
            if (aResource1.getDescription().equals("Description 1 for collection2")) {
                status = true;
            }
        }
        assertTrue(status);
        //create a check point of the resource
        resourceAdminClient.createVersion(COLLECTION_NAME_LEAF + "dir2");
        //edit resource description
        resourceAdminClient.setDescription(COLLECTION_NAME_LEAF + "dir2", "Edited discription");
        // Check whether the edited description is there in sumlink
        ResourceData[] resource2 = resourceAdminClient.getResource(symlinkPath);
        status = false;
        for (ResourceData aResource2 : resource2) {
            if (aResource2.getDescription().equals("Edited discription")) {
                status = true;
            }
        }
        assertTrue(status);


        // Check whether the edited description is there in resource
        ResourceData[] resource3 = resourceAdminClient.getResource(COLLECTION_NAME_LEAF + "dir2");
        status = false;
        for (ResourceData aResource3 : resource3) {
            if (aResource3.getDescription().equals("Edited discription")) {
                status = true;
            }
        }
        assertTrue(status);
        //restore to previous version
        resourceAdminClient.restoreVersion(resourceAdminClient.getVersionPaths(COLLECTION_NAME_LEAF + "dir2")[0].getCompleteVersionPath());
        //check whether the previous description is stored in simlink
        ResourceData[] resource4 = resourceAdminClient.getResource(symlinkPath);
        status = false;
        for (ResourceData aResource4 : resource4) {
            if (aResource4.getDescription().equals("Description 1 for collection2")) {
                status = true;
            }
        }
        assertTrue(status);
        //check whether the previous description is stored in resource
        ResourceData[] resource5 = resourceAdminClient.getResource(COLLECTION_NAME_LEAF + "dir2");
        status = false;
        for (ResourceData aResource5 : resource5) {
            if (aResource5.getDescription().equals("Description 1 for collection2")) {
                status = true;
            }
        }
        assertTrue(status);
        assertEquals(null, deleteVersion(COLLECTION_NAME_LEAF + "dir2"));
    }


    public VersionPath[] deleteVersion(String path)
            throws ResourceAdminServiceExceptionException, RemoteException {
        int length = resourceAdminClient.getVersionPaths(path).length;
        int i = 0;
        while (i < length) {
            long versionNo = resourceAdminClient.getVersionPaths(path)[0].getVersionNumber();
            String snapshotId = String.valueOf(versionNo);
            resourceAdminClient.deleteVersionHistory(path, snapshotId);
            i++;
        }
        VersionPath[] vp2;
        vp2 = resourceAdminClient.getVersionPaths(path);
        return vp2;
    }


    @AfterClass(alwaysRun = true)
    public void clear()
            throws ResourceAdminServiceExceptionException, RemoteException, RegistryException {
        deleteResource(RESOURCE_NAME_ROOT);
        deleteResource(RESOURCE_NAME_LEAF);
        deleteResource("/SymBranch1");
        deleteResource(COLLECTION_NAME_ROOT + "dir1");
        deleteResource(COLLECTION_NAME_LEAF + "dir2");
        deleteResource("/SymCol1");
    }

    public void deleteResource(String path) throws RegistryException {
        if (wsRegistryServiceClient.resourceExists(path)) {
            wsRegistryServiceClient.delete(path);
        }
    }
}
