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

import org.eclipse.wst.xml.core.internal.commentelement.util.TagScanner;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.governance.LifeCycleAdminServiceClient;
import org.wso2.carbon.automation.api.clients.governance.LifeCycleManagementClient;
import org.wso2.carbon.automation.api.clients.governance.ListMetaDataServiceClient;
import org.wso2.carbon.automation.api.clients.registry.InfoServiceAdminClient;
import org.wso2.carbon.automation.api.clients.registry.PropertiesAdminServiceClient;
import org.wso2.carbon.automation.api.clients.registry.RelationAdminServiceClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.core.utils.fileutils.FileManager;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.beans.xsd.LifecycleBean;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.governance.list.stub.ListMetadataServiceRegistryExceptionException;
import org.wso2.carbon.governance.list.stub.beans.xsd.WSDLBean;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.info.stub.RegistryExceptionException;
import org.wso2.carbon.registry.info.stub.beans.utils.xsd.Tag;
import org.wso2.carbon.registry.info.stub.beans.xsd.CommentBean;
import org.wso2.carbon.registry.info.stub.beans.xsd.RatingBean;
import org.wso2.carbon.registry.info.stub.beans.xsd.TagBean;
import org.wso2.carbon.registry.properties.stub.PropertiesAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.properties.stub.utils.xsd.Property;
import org.wso2.carbon.registry.relations.stub.AddAssociationRegistryExceptionException;
import org.wso2.carbon.registry.relations.stub.beans.xsd.AssociationTreeBean;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.beans.xsd.VersionPath;
import org.wso2.carbon.registry.resource.stub.common.xsd.ResourceData;
import org.wso2.carbon.registry.version.test.utils.VersionUtils;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import javax.activation.DataHandler;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Date;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

public class PropertyWSDLTestCase {


    private ManageEnvironment environment;
    private ResourceAdminServiceClient resourceAdminClient;
    private static final String PATH1 = "/_system/governance/trunk/wsdls/org/wso2/carbon/core/services/echo/echo.wsdl";
    private static final String PATH2 = "/_system/governance/trunk/wsdls/org/wso2/carbon/core/services/echo/echo_empty.wsdl";
    private Date createdDate;
    private PropertiesAdminServiceClient propertiesAdminServiceClient;
    private RelationAdminServiceClient relationAdminServiceClient;
    private InfoServiceAdminClient infoServiceAdminClient;
    private LifeCycleManagementClient lifeCycleManagementClient;
    private LifeCycleAdminServiceClient lifeCycleAdminServiceClient;
    private WSRegistryServiceClient wsRegistryServiceClient;
    private UserInfo userInfo;
    private ListMetaDataServiceClient listMetaDataServiceClient;
    private static String LC_NAME = "MultiplePromoteDemoteLC";


    @BeforeClass(alwaysRun = true)
    public void initializeTests()
            throws LoginAuthenticationExceptionException, RemoteException, RegistryException,
                   ResourceAdminServiceExceptionException, MalformedURLException,
                   InterruptedException {

        int userId = 2;
        userInfo = UserListCsvReader.getUserInfo(userId);
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        environment = builder.build();

        resourceAdminClient =
                new ResourceAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                               environment.getGreg().getSessionCookie());
        propertiesAdminServiceClient =
                new PropertiesAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                                 environment.getGreg().getSessionCookie());
        relationAdminServiceClient =
                new RelationAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                               environment.getGreg().getSessionCookie());

        infoServiceAdminClient =
                new InfoServiceAdminClient(environment.getGreg().getBackEndUrl(),
                                           environment.getGreg().getSessionCookie());

        lifeCycleManagementClient =
                new LifeCycleManagementClient(environment.getGreg().getBackEndUrl(),
                                              environment.getGreg().getSessionCookie());
        listMetaDataServiceClient =
                new ListMetaDataServiceClient(environment.getGreg().getBackEndUrl(),
                                              environment.getGreg().getSessionCookie());
        lifeCycleAdminServiceClient =
                new LifeCycleAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                                environment.getGreg().getSessionCookie());

        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        wsRegistryServiceClient =
                registryProviderUtil.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        addWSDL();
    }


    public void addWSDL()
            throws ResourceAdminServiceExceptionException, RemoteException, MalformedURLException,
                   InterruptedException {
        Boolean nameExists = false;
        String path1 = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator
                       + "GREG" + File.separator + "wsdl" + File.separator + "echo.wsdl";
        DataHandler dataHandler1 = new DataHandler(new URL("file:///" + path1));
        resourceAdminClient.addWSDL("desc 1", dataHandler1);
        Thread.sleep(5000);
        createdDate = resourceAdminClient.getResource(PATH1)[0].getCreatedOn().getTime();
        WSDLBean wsdlBean = listMetaDataServiceClient.listWSDLs();
        String[] names1 = wsdlBean.getName();

        for (String name : names1) {
            if (name.equalsIgnoreCase("echo.wsdl")) {
                nameExists = true;
                break;
            }
        }
        assertTrue(nameExists);
        VersionUtils.deleteAllVersions(resourceAdminClient, PATH1);

        nameExists = false;
        String path2 = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator
                       + "GREG" + File.separator + "wsdl" + File.separator + "echo_empty.wsdl";
        DataHandler dataHandler2 = new DataHandler(new URL("file:///" + path2));
        resourceAdminClient.addWSDL("desc 2", dataHandler2);
        wsdlBean = listMetaDataServiceClient.listWSDLs();
        String[] names2 = wsdlBean.getName();

        for (String name : names2) {
            if (name.equalsIgnoreCase("echo_empty.wsdl")) {
                nameExists = true;
                break;
            }
        }
        assertTrue(nameExists);
        VersionUtils.deleteAllVersions(resourceAdminClient, PATH1);

    }

    @Test(groups = {"wso2.greg"}, description = "Add a property to a WSDL,version it and check it")
    public void testVersionPropertyRoot()
            throws ResourceAdminServiceExceptionException, RegistryException,
                   PropertiesAdminServiceRegistryExceptionException, RemoteException {
        boolean status = false;
        int count = 0;
        propertiesAdminServiceClient.setProperty(PATH1, "name1", "value1");
        Property[] properties1 = propertiesAdminServiceClient.getProperty(PATH1, "true").getProperties();

        for (Property aProperties1 : properties1) {

            if (aProperties1.getKey().equals("name1")) {
                count++;
            }
            if (aProperties1.getValue().equals("value1")) {
                count++;
            }
        }
        if (count == 2) {
            status = true;
        }
        assertTrue(status);
        status = false;
        count = 0;
        resourceAdminClient.createVersion(PATH1);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATH1);
        String verPath = vp1[0].getCompleteVersionPath();
        Property[] properties2 = propertiesAdminServiceClient.getProperty(verPath, "true").getProperties();

        for (Property aProperties2 : properties2) {

            if (aProperties2.getKey().equals("name1")) {
                count++;
            }
            if (aProperties2.getValue().equals("value1")) {
                count++;
            }
        }
        if (count == 2) {
            status = true;
        }
        assertTrue(status);
        assertNull(VersionUtils.deleteAllVersions(resourceAdminClient, PATH1));
    }

    @Test(groups = {"wso2.greg"}, description = "Add a association to a WSDL file,version it and check that WSDL")
    public void testVersionAssociation()
            throws ResourceAdminServiceExceptionException, RemoteException,
                   AddAssociationRegistryExceptionException {

        relationAdminServiceClient.addAssociation(PATH1, "usedBy", PATH2, "add");
        AssociationTreeBean associationTreeBean = relationAdminServiceClient.getAssociationTree(PATH1, "usedBy");

        String resourcePath = associationTreeBean.getAssociationTree();
        assertTrue(resourcePath.contains(PATH2));
        resourceAdminClient.createVersion(PATH1);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATH1);
        String verPath = vp1[0].getCompleteVersionPath();
        assertTrue(relationAdminServiceClient.getAssociationTree(verPath, "usedBy").getAssociationTree().contains(PATH2));
        assertNull(VersionUtils.deleteAllVersions(resourceAdminClient, PATH1));
    }


    @Test(groups = {"wso2.greg"}, description = "Add a dependency to a WSDL ,version it and check for that dependency")
    public void testVersionDependency()
            throws ResourceAdminServiceExceptionException, RemoteException,
                   AddAssociationRegistryExceptionException {
        relationAdminServiceClient.addAssociation(PATH1, "depends", PATH2, "add");
        AssociationTreeBean associationTreeBean = relationAdminServiceClient.getAssociationTree(PATH1, "depends");
        String resourcePath = associationTreeBean.getAssociationTree();
        assertTrue(resourcePath.contains(PATH2));
        resourceAdminClient.createVersion(PATH1);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATH1);
        String verPath = vp1[0].getCompleteVersionPath();
        assertTrue(relationAdminServiceClient.getAssociationTree(verPath, "depends").getAssociationTree().contains(PATH2));
        assertNull(VersionUtils.deleteAllVersions(resourceAdminClient, PATH1));
    }


    @Test(groups = {"wso2.greg"}, description = "Add a tag to a WSDL ,version it and check that tag ")
    public void testVersionTag()
            throws RegistryException, RemoteException, ResourceAdminServiceExceptionException,
                   RegistryExceptionException {
        boolean status = false;
        infoServiceAdminClient.addTag("testTag", PATH1, environment.getGreg().getSessionCookie());
        TagBean tagBean = infoServiceAdminClient.getTags(PATH1, environment.getGreg().getSessionCookie());
        Tag[] tags1 = tagBean.getTags();
        for (Tag aTags1 : tags1) {
            if (aTags1.getTagName().equals("testTag")) {
                status = true;
                break;
            }
        }
        assertTrue(status);
        resourceAdminClient.createVersion(PATH1);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATH1);
        String verPath = vp1[0].getCompleteVersionPath();

        boolean tagFound = false;
        Tag[] tags = infoServiceAdminClient.getTags(verPath, environment.getGreg().getSessionCookie()).getTags();

        for (Tag tag : tags) {
            if (tag.getTagName().equals("testTag")) {
                tagFound = true;
                break;
            }
        }
        assertTrue("test tags was not found in the version path", tagFound);
        assertNull(VersionUtils.deleteAllVersions(resourceAdminClient, PATH1));
    }


    @Test(groups = {"wso2.greg"}, description = "Add a comment to a WSDL file,version it and check that comment ")
    public void testVersionCommentR()
            throws RegistryException, RemoteException, RegistryExceptionException,
                   ResourceAdminServiceExceptionException {
        infoServiceAdminClient.addComment("This is a comment", PATH1, environment.getGreg().getSessionCookie());
        CommentBean commentBean = infoServiceAdminClient.getComments(PATH1, environment.getGreg().getSessionCookie());
        assertEquals("This is a comment", commentBean.getComments()[0].getContent());
        resourceAdminClient.createVersion(PATH1);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATH1);
        String verPath = vp1[0].getCompleteVersionPath();
        assertEquals("This is a comment", infoServiceAdminClient.getComments(verPath, environment.getGreg().
                getSessionCookie()).getComments()[0].getContent());
        assertNull(VersionUtils.deleteAllVersions(resourceAdminClient, PATH1));
    }

    @Test(groups = {"wso2.greg"}, description = "Add ratings to a WSDL ,version it and check ratings ")
    public void testVersionRatingR() throws RegistryException, RegistryExceptionException,
                                            ResourceAdminServiceExceptionException,
                                            RemoteException {
        String sessionId = environment.getGreg().getSessionCookie();
        infoServiceAdminClient.rateResource("2", PATH1, sessionId);
        RatingBean ratingBean = infoServiceAdminClient.getRatings(PATH1, sessionId);
        assertEquals(2, ratingBean.getUserRating());
        resourceAdminClient.createVersion(PATH1);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATH1);
        String verPath = vp1[0].getCompleteVersionPath();
        assertEquals(2, infoServiceAdminClient.getRatings(verPath, sessionId).getUserRating());
        assertNull(VersionUtils.deleteAllVersions(resourceAdminClient, PATH1));
    }


    @Test(groups = {"wso2.greg"}, description = "Add description to a WSDL file,version it and check description ")
    public void testVersionDescription()
            throws IOException, LifeCycleManagementServiceExceptionException,
                   ResourceAdminServiceExceptionException {
        boolean status = false;
        ResourceData[] resource1 = resourceAdminClient.getResource(PATH1);
        for (ResourceData aResource1 : resource1) {
            if (aResource1.getDescription().equals("desc 1")) {
                status = true;
                break;
            }
        }
        assertTrue(status);
        status = false;
        resourceAdminClient.createVersion(PATH1);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATH1);
        String verPath = vp1[0].getCompleteVersionPath();

        ResourceData[] resource2 = resourceAdminClient.getResource(verPath);
        for (ResourceData aResource2 : resource2) {
            if (aResource2.getDescription().equals("desc 1")) {
                status = true;
                break;
            }
        }
        assertTrue(status);
        assertNull(VersionUtils.deleteAllVersions(resourceAdminClient, PATH1));
    }


    @Test(groups = {"wso2.greg"}, description = "Add retention to a WSDL file,version it and check retention ")
    public void testVersionRetentionRoot()
            throws PropertiesAdminServiceRegistryExceptionException, RemoteException,
                   ResourceAdminServiceExceptionException {
        propertiesAdminServiceClient.setRetentionProperties(PATH1, "write", "07/02/2012", "08/22/2012");
        assertEquals("07/02/2012", propertiesAdminServiceClient.getRetentionProperties(PATH1).getFromDate());
        assertEquals("08/22/2012", propertiesAdminServiceClient.getRetentionProperties(PATH1).getToDate());
        resourceAdminClient.createVersion(PATH1);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATH1);
        String verPath = vp1[0].getCompleteVersionPath();
        assertEquals("07/02/2012", propertiesAdminServiceClient.getRetentionProperties(verPath).getFromDate());
        assertEquals("08/22/2012", propertiesAdminServiceClient.getRetentionProperties(verPath).getToDate());
        assertNull(VersionUtils.deleteAllVersions(resourceAdminClient, PATH1));
    }

    //Create a new lifecycle
    public void testCreateNewLifeCycle()
            throws LifeCycleManagementServiceExceptionException, IOException {

        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator + "GREG" + File.separator + "lifecycle" + File.separator + "MultiplePromoteDemoteLC.xml";
        String lifeCycleContent = FileManager.readFile(resourcePath);
        lifeCycleManagementClient.addLifeCycle(lifeCycleContent);

        String[] lifeCycles = lifeCycleManagementClient.getLifecycleList();
        boolean lcStatus = false;
        for (String lc : lifeCycles) {
            if (lc.equalsIgnoreCase(LC_NAME)) {
                lcStatus = true;
            }
        }
        assertTrue(lcStatus);
    }

    @Test(groups = "wso2.greg", description = "Add lifecycle to a WSDL")
    public void testAddLc() throws RegistryException, IOException,
                                   CustomLifecyclesChecklistAdminServiceExceptionException,
                                   ListMetadataServiceRegistryExceptionException,
                                   ResourceAdminServiceExceptionException,
                                   LifeCycleManagementServiceExceptionException {

        testCreateNewLifeCycle();
        wsRegistryServiceClient.associateAspect(PATH1, LC_NAME);
        LifecycleBean lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(PATH1);

        org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property[] properties =
                lifeCycle.getLifecycleProperties();
        boolean lcStatus = false;
        for (org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property prop : properties) {
            prop.getKey();
            if (prop.getKey().contains(LC_NAME)) {
                lcStatus = true;
                break;
            }
        }
        assertTrue(lcStatus);
        lcStatus = false;
        resourceAdminClient.createVersion(PATH1);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATH1);
        String verPath = vp1[0].getCompleteVersionPath();
        lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(verPath);

        org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property[] properties2 =
                lifeCycle.getLifecycleProperties();

        for (org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property prop : properties2) {
            prop.getKey();
            if (prop.getKey().contains(LC_NAME)) {
                lcStatus = true;
                break;
            }
        }
        assertTrue(lcStatus);
        assertNull(VersionUtils.deleteAllVersions(resourceAdminClient, PATH1));
    }


    @Test(groups = "wso2.greg", description = "Create a check point and check the accuracy of version " +
                                              "details, version no, last modified date and last modified by values")
    public void testWSDLVersionAccuracy()
            throws ResourceAdminServiceExceptionException, RemoteException {
        boolean status = false;
        resourceAdminClient.createVersion(PATH1);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATH1);
        String verPath = vp1[0].getCompleteVersionPath();

        //check the accuracy of last modified date
        Date modifiedDate = resourceAdminClient.getResource(verPath)[0].getCreatedOn().getTime();
        Date newDate;

        Calendar cal = Calendar.getInstance();
        cal.setTime(createdDate);
        cal.add(Calendar.SECOND, 60);
        newDate = cal.getTime();

        if ((modifiedDate.after(createdDate) || modifiedDate.equals(createdDate)) && modifiedDate.before(newDate)) {
            status = true;
        }

        assertTrue(status);
        //check the accuracy of last modify user
        assertEquals(userInfo.getUserNameWithoutDomain(), resourceAdminClient.getVersionPaths(PATH1)[0].getUpdater());
        //check the accuracy of version WSDL description
        assertEquals("desc 1", resourceAdminClient.getResource(verPath)[0].getDescription());
        //check the accuracy of version number
        long prevVerNo = resourceAdminClient.getVersionPaths(PATH1)[0].getVersionNumber();
        resourceAdminClient.createVersion(PATH1);
        VersionPath[] versionPaths = resourceAdminClient.getVersionPaths(PATH1);
        status = false;
        for (VersionPath versionPath : versionPaths) {
            if (versionPath.getVersionNumber() == prevVerNo + 1) {
                status = true;
                break;
            }
        }
        assertTrue(status);
        assertNull(VersionUtils.deleteAllVersions(resourceAdminClient, PATH1));
    }


    @AfterClass
    public void deleteResources() throws ResourceAdminServiceExceptionException, RemoteException,
                                         LifeCycleManagementServiceExceptionException,
                                         RegistryException {

        deleteResource("/_system/governance/trunk/services/org/wso2/carbon/core/services/echo/echoyuSer1");
        deleteResource(PATH1);
        deleteResource(PATH2);
        lifeCycleManagementClient.deleteLifeCycle(LC_NAME);
        resourceAdminClient = null;
        propertiesAdminServiceClient = null;
        relationAdminServiceClient = null;
        infoServiceAdminClient = null;
        lifeCycleManagementClient = null;
        lifeCycleAdminServiceClient = null;
        wsRegistryServiceClient = null;
        listMetaDataServiceClient = null;
        environment = null;
    }

    public void deleteResource(String path) throws RegistryException {
        if (wsRegistryServiceClient.resourceExists(path)) {
            wsRegistryServiceClient.delete(path);
        }
    }

    public VersionPath[] deleteVersion(String path)
            throws ResourceAdminServiceExceptionException, RemoteException {
        VersionPath[] vp2 = null;
        if (resourceAdminClient.getVersionPaths(path) == null) {
            return vp2;
        } else {
            int length = resourceAdminClient.getVersionPaths(path).length;
            for (int i = 0; i < length; i++) {
                long versionNo = resourceAdminClient.getVersionPaths(path)[0].getVersionNumber();
                String snapshotId = String.valueOf(versionNo);
                resourceAdminClient.deleteVersionHistory(path, snapshotId);
            }

            vp2 = resourceAdminClient.getVersionPaths(path);
            return vp2;
        }
    }

}
