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
import org.wso2.carbon.registry.resource.stub.beans.xsd.VersionsBean;
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

import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;


public class VersionNoTestCase extends GREGIntegrationBaseTest {

    private String RESOURCE_NAME_ROOT = "/versionNo1";
    private String RESOURCE_NAME_LEAF = "/verBranch1/verBranch2/versionNo2";
    private String COLLECTION_NAME_LEAF = "/barnch1/branch2/";
    private String COLLECTION_NAME_ROOT = "/";
    private ResourceAdminServiceClient resourceAdminClient;
    private String sessionCookie;
    private String backEndUrl;
    private String userName;
    private String userNameWithoutDomain;

    @BeforeClass(alwaysRun = true)
    public void initializeTests()
            throws LoginAuthenticationExceptionException, IOException,
            ResourceAdminServiceExceptionException, XPathExpressionException, URISyntaxException, SAXException, XMLStreamException {

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

        String path1 = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator
                       + "GREG" + File.separator + "testresource.txt";
        DataHandler dataHandler1 = new DataHandler(new URL("file:///" + path1));
        resourceAdminClient.addResource(RESOURCE_NAME_ROOT, "text/plain", "desc", dataHandler1);
        assertTrue(resourceAdminClient.getResource(RESOURCE_NAME_ROOT)[0].getAuthorUserName().contains(userNameWithoutDomain));

        String path2 = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator
                       + "GREG" + File.separator + "testresource.txt";
        DataHandler dataHandler2 = new DataHandler(new URL("file:///" + path2));
        resourceAdminClient.addResource(RESOURCE_NAME_LEAF, "text/plain", "desc", dataHandler2);
        assertTrue(resourceAdminClient.getResource(RESOURCE_NAME_LEAF)[0].getAuthorUserName().contains(userNameWithoutDomain));

        resourceAdminClient.addCollection(COLLECTION_NAME_ROOT, "dir1", "text/plain", "Description 1 for collection1");
        resourceAdminClient.addCollection(COLLECTION_NAME_LEAF, "dir2", "text/plain", "Description 1 for collection2");
    }


    @Test(groups = {"wso2.greg"}, description = "Create new resource at root level and check accuracy of the version number")
    public void testCheckVersionNoRootResource()
            throws ResourceAdminServiceExceptionException, RemoteException {
        //create a checkpoint of a resource and get the version number
        resourceAdminClient.createVersion(RESOURCE_NAME_ROOT);
        VersionsBean versionsBean1 = resourceAdminClient.getVersionsBean(RESOURCE_NAME_ROOT);
        long versionNo1 = versionsBean1.getVersionPaths()[0].getVersionNumber();
        //create another checkpoint and compare the version number with the previous version number
        resourceAdminClient.createVersion(RESOURCE_NAME_ROOT);
        boolean accurate = false;
        for (int i = 0; i < resourceAdminClient.getVersionsBean(RESOURCE_NAME_ROOT).getVersionPaths().length; i++) {
            if (resourceAdminClient.getVersionsBean(RESOURCE_NAME_ROOT).getVersionPaths()[i].getVersionNumber() == (versionNo1 + 1)) {
                accurate = true;
            }
        }
        assertTrue(accurate);
        assertNull(deleteVersion(RESOURCE_NAME_ROOT));
    }


    @Test(groups = {"wso2.greg"}, description = "Create new resource at leaf level and check accuracy of the version number")
    public void testCheckVersionNoLeafResource()
            throws ResourceAdminServiceExceptionException, RemoteException {
        //create a checkpoint of a resource and get the version number

        resourceAdminClient.createVersion(RESOURCE_NAME_LEAF);
        VersionsBean versionsBean1 = resourceAdminClient.getVersionsBean(RESOURCE_NAME_LEAF);
        long versionNo1 = versionsBean1.getVersionPaths()[0].getVersionNumber();
        //create another checkpoint and compare the version number with the previous version number

        resourceAdminClient.createVersion(RESOURCE_NAME_LEAF);
        boolean accurate = false;
        for (int i = 0; i < resourceAdminClient.getVersionsBean(RESOURCE_NAME_LEAF).getVersionPaths().length; i++) {
            if (resourceAdminClient.getVersionsBean(RESOURCE_NAME_LEAF).getVersionPaths()[i].getVersionNumber() == (versionNo1 + 1)) {
                accurate = true;
            }
        }
        assertTrue(accurate);
        assertNull(deleteVersion(RESOURCE_NAME_LEAF));
    }


    @Test(groups = {"wso2.greg"}, description = "Create new collection at root level and check accuracy of the version number")
    public void testCheckVersionNoRootCollection()
            throws ResourceAdminServiceExceptionException, RemoteException {
        //create a checkpoint of a resource and get the version number

        resourceAdminClient.createVersion(COLLECTION_NAME_ROOT + "dir1");
        VersionsBean versionsBean1 = resourceAdminClient.getVersionsBean(COLLECTION_NAME_ROOT + "dir1");
        long versionNo1 = versionsBean1.getVersionPaths()[0].getVersionNumber();
        //create another checkpoint and compare the version number with the previous version number

        resourceAdminClient.createVersion(COLLECTION_NAME_ROOT + "dir1");

        boolean accurate = false;
        for (int i = 0; i < resourceAdminClient.getVersionsBean(COLLECTION_NAME_ROOT + "dir1").getVersionPaths().length; i++) {
            if (resourceAdminClient.getVersionsBean(COLLECTION_NAME_ROOT + "dir1").getVersionPaths()[i].getVersionNumber() == (versionNo1 + 1)) {
                accurate = true;
            }
        }
        assertTrue(accurate);
        assertNull(deleteVersion(COLLECTION_NAME_ROOT + "dir1"));
    }


    @Test(groups = {"wso2.greg"}, description = "Create new collection at leaf level and check accuracy of the version number")
    public void testCheckVersionNoLeafCollection()
            throws ResourceAdminServiceExceptionException, RemoteException {

        //create a checkpoint of a resource and get the version number
        resourceAdminClient.createVersion(COLLECTION_NAME_LEAF + "dir2");
        VersionsBean versionsBean1 = resourceAdminClient.getVersionsBean(COLLECTION_NAME_LEAF + "dir2");
        long versionNo1 = versionsBean1.getVersionPaths()[0].getVersionNumber();
        //create another checkpoint and compare the version number with the previous version number
        resourceAdminClient.createVersion(COLLECTION_NAME_LEAF + "dir2");
        boolean accurate = false;
        for (int i = 0; i < resourceAdminClient.getVersionsBean(COLLECTION_NAME_LEAF + "dir2").getVersionPaths().length; i++) {
            if (resourceAdminClient.getVersionsBean(COLLECTION_NAME_LEAF + "dir2").getVersionPaths()[i].getVersionNumber() == (versionNo1 + 1)) {
                accurate = true;
            }
        }
        assertTrue(accurate);
        assertNull(deleteVersion(COLLECTION_NAME_LEAF + "dir2"));
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
        resourceAdminClient.deleteResource(RESOURCE_NAME_ROOT);
        resourceAdminClient.deleteResource(RESOURCE_NAME_LEAF);
        resourceAdminClient.deleteResource("/verBranch1");
        resourceAdminClient.deleteResource(COLLECTION_NAME_ROOT + "dir1");
        resourceAdminClient.deleteResource(COLLECTION_NAME_LEAF + "dir2");
        resourceAdminClient.deleteResource("/barnch1");
        resourceAdminClient = null;
    }


}
