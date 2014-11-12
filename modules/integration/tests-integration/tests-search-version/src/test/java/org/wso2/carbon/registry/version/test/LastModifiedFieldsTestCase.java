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

import junit.framework.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.registry.properties.stub.PropertiesAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.beans.xsd.VersionPath;
import org.wso2.carbon.registry.resource.stub.beans.xsd.VersionsBean;
import org.wso2.carbon.registry.version.test.utils.VersionUtils;
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
import java.util.Calendar;
import java.util.Date;

import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

public class LastModifiedFieldsTestCase extends GREGIntegrationBaseTest {
    private String RESOURCE_NAME_ROOT = "/modifyField1";
    private String RESOURCE_NAME_LEAF = "/verBranch1/verBranch2/modifyField2";
    private String COLLECTION_NAME_LEAF = "/barnch1/branch2/";
    private String COLLECTION_NAME_ROOT = "/";

    private ResourceAdminServiceClient resourceAdminClient;
    private ResourceAdminServiceClient resourceAdminClientAdmin;
    private PropertiesAdminServiceClient propertiesAdminServiceClient;
    private PropertiesAdminServiceClient propertiesAdminServiceClientAdmin;

    private String sessionCookie;
    private String backEndUrl;
    private String userName;
    private String userNameWithoutDomain;


    @BeforeClass(alwaysRun = true)
    public void initializeTests()
            throws LoginAuthenticationExceptionException, IOException,
            ResourceAdminServiceExceptionException, XPathExpressionException,
            URISyntaxException, SAXException, XMLStreamException , Exception{

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        sessionCookie = getSessionCookie();
        backEndUrl = getBackendURL();

        userName = automationContext.getContextTenant().getContextUser().getUserName();

        if (userName.contains("@"))
            userNameWithoutDomain = userName.substring(0, userName.indexOf('@'));
        else
            userNameWithoutDomain = userName;

        resourceAdminClient =
                new ResourceAdminServiceClient(backEndUrl, sessionCookie);
        resourceAdminClientAdmin =
                new ResourceAdminServiceClient(backEndUrl, sessionCookie);
        propertiesAdminServiceClientAdmin =
                new PropertiesAdminServiceClient(backEndUrl, sessionCookie);
        propertiesAdminServiceClient =
                new PropertiesAdminServiceClient(backEndUrl, sessionCookie);

        String path1 = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator
                       + "GREG" + File.separator + "testresource.txt";
        DataHandler dataHandler1 = new DataHandler(new URL("file:///" + path1));

        resourceAdminClient.addResource(RESOURCE_NAME_ROOT, "text/plain", "desc", dataHandler1);
        assertTrue(resourceAdminClient.getResource(RESOURCE_NAME_ROOT)[0].getAuthorUserName().contains(userNameWithoutDomain));


        String path2 = FrameworkPathUtil.getSystemResourceLocation() + "arti" +
                "facts" + File.separator
                       + "GREG" + File.separator + "testresource.txt";
        DataHandler dataHandler2 = new DataHandler(new URL("file:///" + path2));
        resourceAdminClient.addResource(RESOURCE_NAME_LEAF, "text/plain", "desc", dataHandler2);
        assertTrue(resourceAdminClient.getResource(RESOURCE_NAME_LEAF)[0].getAuthorUserName().contains(userNameWithoutDomain));


        resourceAdminClient.addCollection(COLLECTION_NAME_ROOT, "dir1", "text/plain", "Description 1 for collection1");
        resourceAdminClient.addCollection(COLLECTION_NAME_LEAF, "dir2", "text/plain", "Description 1 for collection2");

        VersionUtils.deleteAllVersions(resourceAdminClient, COLLECTION_NAME_ROOT + "dir1");
        VersionUtils.deleteAllVersions(resourceAdminClient, COLLECTION_NAME_LEAF + "dir2");
    }

    @Test(groups = {"wso2.greg"}, description = "Create new resource at root level and check accuracy of the last modified date")
    public void testCheckModifiedDateRootResource()
            throws ResourceAdminServiceExceptionException, RemoteException,
                   PropertiesAdminServiceRegistryExceptionException {
        boolean status = false;
        //Created time
        Date createdTime = resourceAdminClient.getResource(RESOURCE_NAME_ROOT)[0].getCreatedOn().getTime();
        //get upper threshold by adding one minute
        long threshold = createdTime.getTime() + 60 * 1000;
        Date thresholdDate = new Date(threshold);
        //add a property to the resource
        propertiesAdminServiceClient.setProperty(RESOURCE_NAME_ROOT, "name1", "value1");
        //create a checkpoint
        resourceAdminClient.createVersion(RESOURCE_NAME_ROOT);
        VersionsBean versionBean = resourceAdminClient.getVersionsBean(RESOURCE_NAME_ROOT);
        Calendar calender = versionBean.getVersionPaths()[0].getUpdatedOn();
        //get the updated time
        Date modifiedDate = calender.getTime();
        Date newDate;

        Calendar cal = Calendar.getInstance();
        cal.setTime(createdTime);
        cal.add(Calendar.SECOND, 60);
        newDate = cal.getTime();

        if (modifiedDate.after(createdTime) && modifiedDate.before(newDate)) {
            status = true;
        }

        assertTrue(status);
        //delete the created version
        assertNull(deleteVersion(RESOURCE_NAME_ROOT));


    }


    @Test(groups = {"wso2.greg"}, description = "Create new resource at leaf level and check accuracy of the last modified date")
    public void testCheckModifiedDateLeafResource()
            throws ResourceAdminServiceExceptionException, RemoteException,
                   PropertiesAdminServiceRegistryExceptionException {
        boolean status = false;
        //Created time
        Date createdTime = resourceAdminClient.getResource(RESOURCE_NAME_LEAF)[0].getCreatedOn().getTime();
        //get upper threshold by adding one minute
        long threshold = createdTime.getTime() + 60 * 1000;
        Date thresholdDate = new Date(threshold);
        //add a property to the resource
        propertiesAdminServiceClient.setProperty(RESOURCE_NAME_LEAF, "name1", "value1");
        //create a checkpoint
        resourceAdminClient.createVersion(RESOURCE_NAME_LEAF);
        VersionsBean versionBean = resourceAdminClient.getVersionsBean(RESOURCE_NAME_LEAF);
        Calendar calender = versionBean.getVersionPaths()[0].getUpdatedOn();
        //get the updated time    createdTime

        Date modifiedDate = calender.getTime();
        Date newDate;

        Calendar cal = Calendar.getInstance();
        cal.setTime(createdTime);
        cal.add(Calendar.SECOND, 60);
        newDate = cal.getTime();

        if (modifiedDate.after(createdTime) && modifiedDate.before(newDate)) {
            status = true;
        }

        assertTrue(status);
        //delete the created version
        assertNull(deleteVersion(RESOURCE_NAME_LEAF));


    }

    @Test(groups = {"wso2.greg"}, description = "Create new collection at  root level and check accuracy of the last modified date")
    public void testCheckModifiedDateRootCollection()
            throws ResourceAdminServiceExceptionException, RemoteException,
                   PropertiesAdminServiceRegistryExceptionException {
        boolean status = false;
        //Created time
        Date createdTime = resourceAdminClient.getResource(COLLECTION_NAME_ROOT + "dir1")[0].getCreatedOn().getTime();
        //get upper threshold by adding one minute
        long threshold = createdTime.getTime() + 60 * 1000;
        Date thresholdDate = new Date(threshold);
        //add a property to the resource
        propertiesAdminServiceClient.setProperty(COLLECTION_NAME_ROOT + "dir1", "name1", "value1");
        //create a checkpoint
        resourceAdminClient.createVersion(COLLECTION_NAME_ROOT + "dir1");
        VersionsBean versionBean = resourceAdminClient.getVersionsBean(COLLECTION_NAME_ROOT + "dir1");
        Calendar calender = versionBean.getVersionPaths()[0].getUpdatedOn();
        //get the updated time

        Date modifiedDate = calender.getTime();
        Date newDate;

        Calendar cal = Calendar.getInstance();
        cal.setTime(createdTime);
        cal.add(Calendar.SECOND, 60);
        newDate = cal.getTime();

        if (modifiedDate.after(createdTime) && modifiedDate.before(newDate)) {
            status = true;
        }

        assertTrue(status);
        //delete the created version
        assertNull(deleteVersion(COLLECTION_NAME_ROOT + "dir1"));


    }


    @Test(groups = {"wso2.greg"}, description = "Create new collection at  leaf level and check accuracy of the last modified date")
    public void testCheckModifiedDateLeafCollection()
            throws ResourceAdminServiceExceptionException, RemoteException,
                   PropertiesAdminServiceRegistryExceptionException {
        boolean status = false;
        //Created time
        Date createdTime = resourceAdminClient.getResource(COLLECTION_NAME_LEAF + "dir2")[0].getCreatedOn().getTime();

        //get upper threshold by adding one minute
        long threshold = createdTime.getTime() + 60 * 1000;
        Date thresholdDate = new Date(threshold);
        //add a property to the resource
        propertiesAdminServiceClient.setProperty(COLLECTION_NAME_LEAF + "dir2", "name1", "value1");
        //create a checkpoint
        resourceAdminClient.createVersion(COLLECTION_NAME_LEAF + "dir2");
        VersionsBean versionBean = resourceAdminClient.getVersionsBean(COLLECTION_NAME_LEAF + "dir2");
        Calendar calender = versionBean.getVersionPaths()[0].getUpdatedOn();
        //get the updated time
        Date modifiedDate = calender.getTime();
        Date newDate;

        Calendar cal = Calendar.getInstance();
        cal.setTime(createdTime);
        cal.add(Calendar.SECOND, 60);
        newDate = cal.getTime();

        if (modifiedDate.after(createdTime) && modifiedDate.before(newDate)) {
            status = true;
        }

        assertTrue(status);
        //delete the created version
        assertNull(deleteVersion(COLLECTION_NAME_LEAF + "dir2"));


    }


    @Test(groups = {"wso2.greg"}, description = "Create new resource at root level and check accuracy of the last modified by field")
    public void testCheckModifiedByRootResource()
            throws ResourceAdminServiceExceptionException, RemoteException {
        String updatedBy;
        //update the text content by testuser1
        resourceAdminClient.updateTextContent(RESOURCE_NAME_ROOT, "Edited content by " +
                                                                  userNameWithoutDomain);
        //create a checkpoint
        resourceAdminClient.createVersion(RESOURCE_NAME_ROOT);
        updatedBy = resourceAdminClient.getVersionPaths(RESOURCE_NAME_ROOT)[0].getUpdater();
        Assert.assertEquals(userNameWithoutDomain, updatedBy);
        //get the version no of last checkpoint
        long verNo1 = resourceAdminClient.getVersionPaths(RESOURCE_NAME_ROOT)[0].getVersionNumber();
        //update the text content by testuser1
        resourceAdminClient.updateTextContent(RESOURCE_NAME_ROOT, "second edition  by " + userNameWithoutDomain);
        //update the text content by admin
        resourceAdminClientAdmin.updateTextContent(RESOURCE_NAME_ROOT, "Edited content by admin");
        //create a checkpoint
        resourceAdminClient.createVersion(RESOURCE_NAME_ROOT);
        VersionPath[] versionsPaths = resourceAdminClient.getVersionPaths(RESOURCE_NAME_ROOT);
        //Check the last modified by field in last checkpoint
        for (VersionPath versionsPath : versionsPaths) {
            if (versionsPath.getVersionNumber() == verNo1 + 1) {
                updatedBy = versionsPath.getUpdater();
            }
        }
        Assert.assertEquals(userNameWithoutDomain, updatedBy);
        assertNull(deleteVersion(RESOURCE_NAME_ROOT));


    }


    @Test(groups = {"wso2.greg"}, description = "Create new resource at leaf level and check accuracy of the last modified by field")
    public void testCheckModifiedByLeafResource()
            throws ResourceAdminServiceExceptionException, RemoteException {
        String updatedBy;
        //update the text content by testuser1
        resourceAdminClient.updateTextContent(RESOURCE_NAME_LEAF, "Edited content by " +
                                                                  userNameWithoutDomain);
        //create a checkpoint
        resourceAdminClient.createVersion(RESOURCE_NAME_LEAF);
        updatedBy = resourceAdminClient.getVersionPaths(RESOURCE_NAME_LEAF)[0].getUpdater();
        Assert.assertEquals(userNameWithoutDomain, updatedBy);
        //get the version no of last checkpoint
        long verNo1 = resourceAdminClient.getVersionPaths(RESOURCE_NAME_LEAF)[0].getVersionNumber();
        //update the text content by testuser1
        resourceAdminClient.updateTextContent(RESOURCE_NAME_LEAF, "second edition  by " +
                                                                  userNameWithoutDomain);
        //update the text content by admin
        resourceAdminClientAdmin.updateTextContent(RESOURCE_NAME_LEAF, "Edited content by admin");
        //create a checkpoint
        resourceAdminClient.createVersion(RESOURCE_NAME_LEAF);
        //Check the last modified by field in last checkpoint
        VersionPath[] versionsPaths = resourceAdminClient.getVersionPaths(RESOURCE_NAME_LEAF);
        for (VersionPath versionsPath : versionsPaths) {
            if (versionsPath.getVersionNumber() == verNo1 + 1) {
                updatedBy = versionsPath.getUpdater();
            }
        }
        Assert.assertEquals(userNameWithoutDomain, updatedBy);
        assertNull(deleteVersion(RESOURCE_NAME_LEAF));


    }


    @Test(groups = {"wso2.greg"}, description = "Create new collection at root level and check accuracy of the last modified by field")
    public void testCheckModifiedByRootCollection()
            throws ResourceAdminServiceExceptionException, RemoteException,
                   PropertiesAdminServiceRegistryExceptionException {
        String updatedBy;
        //add a new property to the collection by testuser1
        propertiesAdminServiceClient.setProperty(COLLECTION_NAME_ROOT + "dir1", "collectionRootTestUser_first", "value1");
        //create a checkpoint
        resourceAdminClient.createVersion(COLLECTION_NAME_ROOT + "dir1");
        updatedBy = resourceAdminClient.getVersionPaths(COLLECTION_NAME_ROOT + "dir1")[0].getUpdater();
        Assert.assertEquals(userNameWithoutDomain, updatedBy);
        long verNo1 = resourceAdminClient.getVersionPaths(COLLECTION_NAME_ROOT + "dir1")[0].getVersionNumber();
        //add another new property to the collection by testuser1
        propertiesAdminServiceClient.setProperty(COLLECTION_NAME_ROOT + "dir1", "collectionRootTestUser_second", "value2");
        //add another new property to the collection by admin
        propertiesAdminServiceClientAdmin.setProperty(COLLECTION_NAME_ROOT + "dir1", "collectionRootAdmin_first", "value1");
        //create a checkpoint
        resourceAdminClient.createVersion(COLLECTION_NAME_ROOT + "dir1");
        VersionPath[] versionsPaths = resourceAdminClient.getVersionPaths(COLLECTION_NAME_ROOT + "dir1");
        //Check the last modified by field in last checkpoint
        for (VersionPath versionsPath : versionsPaths) {
            if (versionsPath.getVersionNumber() == verNo1 + 1) {
                updatedBy = versionsPath.getUpdater();
            }
        }
        Assert.assertEquals(userNameWithoutDomain, updatedBy);
        assertNull(deleteVersion(COLLECTION_NAME_ROOT + "dir1"));


    }


    @Test(groups = {"wso2.greg"}, description = "Create new collection at leaf level and check accuracy of the last modified by field")
    public void testCheckModifiedByLeafCollection()
            throws ResourceAdminServiceExceptionException, RemoteException,
                   PropertiesAdminServiceRegistryExceptionException {
        String updatedBy;
        //add a new property to the collection by testuser1
        propertiesAdminServiceClient.setProperty(COLLECTION_NAME_LEAF + "dir2", "collectionRootTestUser_first", "value1");
        //create a check point
        resourceAdminClient.createVersion(COLLECTION_NAME_LEAF + "dir2");
        updatedBy = resourceAdminClient.getVersionPaths(COLLECTION_NAME_LEAF + "dir2")[0].getUpdater();
        Assert.assertEquals(userNameWithoutDomain, updatedBy);
        //get the last checkpoint's version number
        long verNo1 = resourceAdminClient.getVersionPaths(COLLECTION_NAME_LEAF + "dir2")[0].getVersionNumber();
        //add another new property to the collection by testuser1
        propertiesAdminServiceClient.setProperty(COLLECTION_NAME_LEAF + "dir2", "collectionRootTestUser_second", "value2");
        //add another new property to the collection by admin
        propertiesAdminServiceClientAdmin.setProperty(COLLECTION_NAME_LEAF + "dir2", "collectionRootAdmin_first", "value1");
        //create a checkpoint
        resourceAdminClient.createVersion(COLLECTION_NAME_LEAF + "dir2");
        VersionPath[] versionsPaths = resourceAdminClient.getVersionPaths(COLLECTION_NAME_LEAF + "dir2");
        //Check the last modified by field in last checkpoint
        for (VersionPath versionsPath : versionsPaths) {
            if (versionsPath.getVersionNumber() == verNo1 + 1) {
                updatedBy = versionsPath.getUpdater();
            }
        }
        Assert.assertEquals(userNameWithoutDomain, updatedBy);
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
        resourceAdminClient.deleteResource(COLLECTION_NAME_LEAF);
        resourceAdminClient = null;
        resourceAdminClientAdmin = null;
        propertiesAdminServiceClient = null;
        propertiesAdminServiceClientAdmin = null;

    }


}
