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
import org.wso2.carbon.automation.api.clients.governance.LifeCycleAdminServiceClient;
import org.wso2.carbon.automation.api.clients.governance.LifeCycleManagementClient;
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
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.governance.list.stub.ListMetadataServiceRegistryExceptionException;
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
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import javax.activation.DataHandler;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;


public class PropertyResourceTestCase {
    private UserInfo userInfo;

    private ManageEnvironment environment;
    private ResourceAdminServiceClient resourceAdminClient;
    private static final String PATHROOT = "/PropertyResourceTestCase1";
    private static final String PATHLEAF = "/_system/config/PropertyResourceTestCase2";
    private static final String PATH = "/_system/config/PropertyResourceTestCase3";
    private PropertiesAdminServiceClient propertiesAdminServiceClient;
    private RelationAdminServiceClient relationAdminServiceClient;
    private InfoServiceAdminClient infoServiceAdminClient;
    private LifeCycleAdminServiceClient lifeCycleAdminServiceClient;
    private LifeCycleManagementClient lifeCycleManagementClient;
    private WSRegistryServiceClient wsRegistryServiceClient;
    private static String LC_NAME = "MultiplePromoteDemoteLC";
    private static String LC_NAME_2 = "StateDemoteLC";


    private org.wso2.carbon.governance.custom.lifecycles.checklist.stub.beans.xsd.LifecycleBean lifeCycle;


    @BeforeClass(alwaysRun = true)
    public void initializeTests()
            throws LoginAuthenticationExceptionException, RemoteException, RegistryException,
                   ResourceAdminServiceExceptionException, MalformedURLException {

        int userId = 2;
        userInfo = UserListCsvReader.getUserInfo(userId);
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        environment = builder.build();

        resourceAdminClient =
                new ResourceAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                               environment.getGreg().getSessionCookie());
        propertiesAdminServiceClient = new PropertiesAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                                                        environment.getGreg().getSessionCookie());
        relationAdminServiceClient = new RelationAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                                                    environment.getGreg().getSessionCookie());

        infoServiceAdminClient = new InfoServiceAdminClient(environment.getGreg().getBackEndUrl(),
                                                            environment.getGreg().getSessionCookie());
        lifeCycleAdminServiceClient = new LifeCycleAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                                                      environment.getGreg().getSessionCookie());

        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        wsRegistryServiceClient = registryProviderUtil.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        lifeCycleManagementClient = new LifeCycleManagementClient(environment.getGreg().getBackEndUrl(),
                                                                  environment.getGreg().getSessionCookie());

        addResource();
    }


    private void addResource()
            throws ResourceAdminServiceExceptionException, RemoteException, MalformedURLException {
        String path1 = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator
                       + "GREG" + File.separator + "testresource.txt";
        DataHandler dataHandler1 = new DataHandler(new URL("file:///" + path1));
        resourceAdminClient.addResource(PATHROOT, "text/plain", "desc", dataHandler1);
        assertTrue(resourceAdminClient.getResource(PATHROOT)[0].getAuthorUserName().contains(userInfo.getUserNameWithoutDomain()));
        String path2 = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator
                       + "GREG" + File.separator + "testresource.txt";
        DataHandler dataHandler2 = new DataHandler(new URL("file:///" + path2));
        resourceAdminClient.addResource(PATHLEAF, "text/plain", "desc", dataHandler2);
        assertTrue(resourceAdminClient.getResource(PATHLEAF)[0].getAuthorUserName().contains(userInfo.getUserNameWithoutDomain()));

        String path3 = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator
                       + "GREG" + File.separator + "testresource.txt";
        DataHandler dataHandler3 = new DataHandler(new URL("file:///" + path3));
        resourceAdminClient.addResource(PATH, "text/plain", "desc", dataHandler3);
        assertTrue(resourceAdminClient.getResource(PATH)[0].getAuthorUserName().contains(userInfo.getUserNameWithoutDomain()));

    }

    @Test(groups = {"wso2.greg"}, description = "Add a property to a resource,version it and check that property at root level")
    public void testVersionPropertyRoot()
            throws ResourceAdminServiceExceptionException, RegistryException,
                   PropertiesAdminServiceRegistryExceptionException, RemoteException,
                   InterruptedException {


        boolean status = false;
        int count = 0;
        propertiesAdminServiceClient.setProperty(PATHROOT, "name1", "value1");
        Property[] properties1 = propertiesAdminServiceClient.getProperty(PATHROOT, "true").getProperties();

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

        resourceAdminClient.createVersion(PATHROOT);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATHROOT);
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
        assertNull(deleteVersion(PATHROOT));


    }


    @Test(groups = {"wso2.greg"}, description = "Add a property to a resource,version it and check that property at leaf level",
          dependsOnMethods = "testVersionPropertyRoot")
    public void testVersionPropertyLeaf()
            throws ResourceAdminServiceExceptionException, RemoteException, RegistryException,
                   PropertiesAdminServiceRegistryExceptionException {


        boolean status = false;
        int count = 0;
        propertiesAdminServiceClient.setProperty(PATHLEAF, "name1", "value1");
        Property[] properties1 = propertiesAdminServiceClient.getProperty(PATHLEAF, "true").getProperties();

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

        resourceAdminClient.createVersion(PATHLEAF);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATHLEAF);
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
        assertNull(deleteVersion(PATHLEAF));


    }


    @Test(groups = {"wso2.greg"},
          description = "Add a association to a resource  at root level,version it and check that association",
          dependsOnMethods = "testVersionPropertyLeaf")
    public void testVersionAssociationRoot()
            throws ResourceAdminServiceExceptionException, RemoteException,
                   AddAssociationRegistryExceptionException {

        relationAdminServiceClient.addAssociation(PATHROOT, "usedBy", PATH, "add");
        AssociationTreeBean associationTreeBean = relationAdminServiceClient.getAssociationTree(PATHROOT, "usedBy");
        String resourcePath = associationTreeBean.getResourcePath();
        assertEquals(PATHROOT, resourcePath);
        resourceAdminClient.createVersion(PATHROOT);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATHROOT);
        String verPath = vp1[0].getCompleteVersionPath();
        assertEquals(verPath, relationAdminServiceClient.getAssociationTree(verPath, "usedBy").getResourcePath());


        assertNull(deleteVersion(PATHROOT));


    }

    @Test(groups = {"wso2.greg"},
          description = "Add a association to a resource at leaf level,version it and check that association",
          dependsOnMethods = "testVersionAssociationRoot")
    public void testVersionAssociationLeaf()
            throws ResourceAdminServiceExceptionException, RemoteException,
                   AddAssociationRegistryExceptionException {

        relationAdminServiceClient.addAssociation(PATHLEAF, "usedBy", PATH, "add");
        AssociationTreeBean associationTreeBean = relationAdminServiceClient.getAssociationTree(PATHLEAF, "usedBy");
        String resourcePath = associationTreeBean.getResourcePath();
        assertEquals(PATHLEAF, resourcePath);
        resourceAdminClient.createVersion(PATHLEAF);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATHLEAF);
        String verPath = vp1[0].getCompleteVersionPath();
        assertEquals(verPath, relationAdminServiceClient.getAssociationTree(verPath, "usedBy").getResourcePath());
        assertNull(deleteVersion(PATHLEAF));


    }

    @Test(groups = {"wso2.greg"}, description = "Add a dependency to a resource at root level,version it and check for that dependency",
          dependsOnMethods = "testVersionAssociationLeaf")
    public void testVersionDependencyRoot()
            throws ResourceAdminServiceExceptionException, RemoteException,
                   AddAssociationRegistryExceptionException {
        relationAdminServiceClient.addAssociation(PATHROOT, "depends", PATH, "add");
        AssociationTreeBean associationTreeBean = relationAdminServiceClient.getAssociationTree(PATHROOT, "depends");
        String resourcePath = associationTreeBean.getResourcePath();
        assertEquals(PATHROOT, resourcePath);
        resourceAdminClient.createVersion(PATHROOT);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATHROOT);
        String verPath = vp1[0].getCompleteVersionPath();
        assertEquals(verPath, relationAdminServiceClient.getAssociationTree(verPath, "depends").getResourcePath());
        assertNull(deleteVersion(PATHROOT));

    }


    @Test(groups = {"wso2.greg"}, description = "Add a dependency to a resource at leaf level,version it and check for that dependency ",
          dependsOnMethods = "testVersionDependencyRoot")
    public void testVersionDependencyLeaf()
            throws ResourceAdminServiceExceptionException, RemoteException,
                   AddAssociationRegistryExceptionException {
        relationAdminServiceClient.addAssociation(PATHLEAF, "depends", PATH, "add");
        AssociationTreeBean associationTreeBean = relationAdminServiceClient.getAssociationTree(PATHLEAF, "depends");
        String resourcePath = associationTreeBean.getResourcePath();
        assertEquals(PATHLEAF, resourcePath);
        resourceAdminClient.createVersion(PATHLEAF);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATHLEAF);
        String verPath = vp1[0].getCompleteVersionPath();
        assertEquals(verPath, relationAdminServiceClient.getAssociationTree(verPath, "depends").getResourcePath());
        assertNull(deleteVersion(PATHLEAF));

    }


    @Test(groups = {"wso2.greg"}, description = "Add a tag to a resource at root level,version it and check that tag ",
          dependsOnMethods = "testVersionDependencyLeaf")
    public void testVersionTagRoot()
            throws RegistryException, RemoteException, ResourceAdminServiceExceptionException,
                   RegistryExceptionException {
        infoServiceAdminClient.addTag("testTag", PATHROOT, environment.getGreg().getSessionCookie());
        TagBean tagBean = infoServiceAdminClient.getTags(PATHROOT, environment.getGreg().getSessionCookie());
        boolean status = false;
        Tag[] tags1 = tagBean.getTags();

        for (Tag aTags1 : tags1) {
            if (aTags1.getTagName().equals("testTag")) {
                status = true;
                break;
            }
        }
        assertTrue(status);

        resourceAdminClient.createVersion(PATHROOT);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATHROOT);
        String verPath = vp1[0].getCompleteVersionPath();
        boolean tagFound = false;
        Tag[] tags2 = infoServiceAdminClient.getTags(verPath, environment.getGreg().getSessionCookie()).getTags();

        for (Tag tag : tags2) {
            if (tag.getTagName().equals("testTag")) {
                tagFound = true;
                break;
            }
        }
        assertTrue("test tags was not found in the version path", tagFound);
        assertNull(deleteVersion(PATHROOT));


    }

    @Test(groups = {"wso2.greg"}, description = "Add a tag to a resource at leaf level,version it and check that tag ",
          dependsOnMethods = "testVersionTagRoot")
    public void testVersionTagLeaf()
            throws ResourceAdminServiceExceptionException, RemoteException, RegistryException,
                   RegistryExceptionException {
        infoServiceAdminClient.addTag("testTag", PATHLEAF, environment.getGreg().getSessionCookie());
        TagBean tagBean = infoServiceAdminClient.getTags(PATHLEAF, environment.getGreg().getSessionCookie());
        boolean status = false;
        Tag[] tags1 = tagBean.getTags();

        for (Tag aTags1 : tags1) {
            if (aTags1.getTagName().equals("testTag")) {
                status = true;
                break;
            }
        }
        assertTrue(status);
        resourceAdminClient.createVersion(PATHLEAF);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATHLEAF);
        String verPath = vp1[0].getCompleteVersionPath();
        boolean tagFound = false;
        Tag[] tags2 = infoServiceAdminClient.getTags(verPath, environment.getGreg().getSessionCookie()).getTags();

        for (Tag tag : tags2) {
            if (tag.getTagName().equals("testTag")) {
                tagFound = true;
                break;
            }
        }
        assertTrue("test tags was not found in the version path", tagFound);
        assertNull(deleteVersion(PATHLEAF));
    }


    @Test(groups = {"wso2.greg"}, description = "Add a comment to a resource at root level,version it and check that comment ",
          dependsOnMethods = "testVersionTagLeaf")
    public void testVersionCommentRoot()
            throws RegistryException, RemoteException, RegistryExceptionException,
                   ResourceAdminServiceExceptionException {
        infoServiceAdminClient.addComment("This is a comment", PATHROOT, environment.getGreg().getSessionCookie());
        CommentBean commentBean = infoServiceAdminClient.getComments(PATHROOT, environment.getGreg().getSessionCookie());
        assertEquals("This is a comment", commentBean.getComments()[0].getContent());
        resourceAdminClient.createVersion(PATHROOT);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATHROOT);
        String verPath = vp1[0].getCompleteVersionPath();
        assertEquals("This is a comment", infoServiceAdminClient.getComments(verPath, environment.getGreg().getSessionCookie()).getComments()[0].getContent());
        assertNull(deleteVersion(PATHROOT));
    }


    @Test(groups = {"wso2.greg"}, description = "Add a comment to a resource at leaf level,version it and check that comment ",
          dependsOnMethods = "testVersionCommentRoot")
    public void testVersionCommentLeaf()
            throws RegistryException, RemoteException, RegistryExceptionException,
                   ResourceAdminServiceExceptionException {

        infoServiceAdminClient.addComment("This is a comment", PATHLEAF, environment.getGreg().getSessionCookie());
        CommentBean commentBean = infoServiceAdminClient.getComments(PATHLEAF, environment.getGreg().getSessionCookie());
        assertEquals("This is a comment", commentBean.getComments()[0].getContent());
        resourceAdminClient.createVersion(PATHLEAF);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATHLEAF);
        String verPath = vp1[0].getCompleteVersionPath();
        assertEquals("This is a comment", infoServiceAdminClient.getComments(verPath, environment.getGreg().getSessionCookie()).getComments()[0].getContent());
        assertNull(deleteVersion(PATHLEAF));
    }

    @Test(groups = {"wso2.greg"}, description = "Add ratings to a resource at root level,version it and check ratings",
          dependsOnMethods = "testVersionCommentLeaf")
    public void testVersionRatingRoot() throws RegistryException, RegistryExceptionException,
                                               ResourceAdminServiceExceptionException,
                                               RemoteException {
        String sessionId = environment.getGreg().getSessionCookie();
        infoServiceAdminClient.rateResource("2", PATHROOT, sessionId);
        RatingBean ratingBean = infoServiceAdminClient.getRatings(PATHROOT, sessionId);
        assertEquals(2, ratingBean.getUserRating());
        resourceAdminClient.createVersion(PATHROOT);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATHROOT);
        String verPath = vp1[0].getCompleteVersionPath();
        assertEquals(2, infoServiceAdminClient.getRatings(verPath, sessionId).getUserRating());
        assertNull(deleteVersion(PATHROOT));

    }


    @Test(groups = {"wso2.greg"}, description = "Add ratings to a resource at leaf level,version it and check ratings ",
          dependsOnMethods = "testVersionRatingRoot")
    public void testVersionRatingLeaf() throws RegistryException, RegistryExceptionException,
                                               ResourceAdminServiceExceptionException,
                                               RemoteException {
        String sessionId = environment.getGreg().getSessionCookie();
        infoServiceAdminClient.rateResource("2", PATHLEAF, sessionId);
        RatingBean ratingBean = infoServiceAdminClient.getRatings(PATHLEAF, sessionId);
        assertEquals(2, ratingBean.getUserRating());
        resourceAdminClient.createVersion(PATHLEAF);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATHLEAF);
        String verPath = vp1[0].getCompleteVersionPath();
        assertEquals(2, infoServiceAdminClient.getRatings(verPath, sessionId).getUserRating());
        assertNull(deleteVersion(PATHLEAF));

    }

    @Test(groups = {"wso2.greg"}, description = "Add description to a resource at root level,version it and check description ",
          dependsOnMethods = "testVersionRatingLeaf")
    public void testVersionDescriptionRoot()
            throws IOException, LifeCycleManagementServiceExceptionException,
                   ResourceAdminServiceExceptionException {

        assertEquals("desc", resourceAdminClient.getResource(PATHROOT)[0].getDescription());
        resourceAdminClient.createVersion(PATHROOT);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATHROOT);
        String verPath = vp1[0].getCompleteVersionPath();
        assertEquals("desc", resourceAdminClient.getResource(verPath)[0].getDescription());
        assertNull(deleteVersion(PATHROOT));


    }

    @Test(groups = {"wso2.greg"}, description = "Add description to a resource at leaf level,version it and check description ",
          dependsOnMethods = "testVersionDescriptionRoot")
    public void testVersionDescriptionLeaf()
            throws IOException, LifeCycleManagementServiceExceptionException,
                   ResourceAdminServiceExceptionException {

        assertEquals("desc", resourceAdminClient.getResource(PATHLEAF)[0].getDescription());
        resourceAdminClient.createVersion(PATHLEAF);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATHLEAF);
        String verPath = vp1[0].getCompleteVersionPath();
        assertEquals("desc", resourceAdminClient.getResource(verPath)[0].getDescription());
        assertNull(deleteVersion(PATHLEAF));
    }

    @Test(groups = {"wso2.greg"}, description = "Add retention to a resource at root level,version it and check retention ",
          dependsOnMethods = "testVersionDescriptionLeaf")
    public void testVersionRetentionRoot()
            throws PropertiesAdminServiceRegistryExceptionException, RemoteException,
                   ResourceAdminServiceExceptionException {
        propertiesAdminServiceClient.setRetentionProperties(PATHROOT, "write", "07/02/2012", "08/22/2012");
        assertEquals("07/02/2012", propertiesAdminServiceClient.getRetentionProperties(PATHROOT).getFromDate());
        assertEquals("08/22/2012", propertiesAdminServiceClient.getRetentionProperties(PATHROOT).getToDate());
        resourceAdminClient.createVersion(PATHROOT);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATHROOT);
        String verPath = vp1[0].getCompleteVersionPath();
        assertEquals("07/02/2012", propertiesAdminServiceClient.getRetentionProperties(verPath).getFromDate());
        assertEquals("08/22/2012", propertiesAdminServiceClient.getRetentionProperties(verPath).getToDate());
        assertNull(deleteVersion(PATHROOT));

    }


    @Test(groups = {"wso2.greg"}, description = "Add retention to a resource at leaf level,version it and check retention ",
          dependsOnMethods = "testVersionRetentionRoot")
    public void testVersionRetentionLeaf()
            throws PropertiesAdminServiceRegistryExceptionException, RemoteException,
                   ResourceAdminServiceExceptionException {
        propertiesAdminServiceClient.setRetentionProperties(PATHLEAF, "write", "07/02/2012", "08/22/2012");
        assertEquals("07/02/2012", propertiesAdminServiceClient.getRetentionProperties(PATHLEAF).getFromDate());
        assertEquals("08/22/2012", propertiesAdminServiceClient.getRetentionProperties(PATHLEAF).getToDate());
        resourceAdminClient.createVersion(PATHLEAF);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATHLEAF);
        String verPath = vp1[0].getCompleteVersionPath();
        assertEquals("07/02/2012", propertiesAdminServiceClient.getRetentionProperties(verPath).getFromDate());
        assertEquals("08/22/2012", propertiesAdminServiceClient.getRetentionProperties(verPath).getToDate());
        assertNull(deleteVersion(PATHLEAF));

    }


    public void testAddNewLifeCycle(String LCName, String fileName)
            throws LifeCycleManagementServiceExceptionException, IOException {
        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator + "GREG" + File.separator + "lifecycle" + File.separator + fileName;
        String lifeCycleContent = FileManager.readFile(resourcePath);
        lifeCycleManagementClient.addLifeCycle(lifeCycleContent);


        String[] lifeClycles = lifeCycleManagementClient.getLifecycleList();
        boolean lcStatus = false;
        for (String lc : lifeClycles) {
            if (lc.equalsIgnoreCase(LCName)) {
                lcStatus = true;
            }
        }
        assertTrue(lcStatus);
    }

    @Test(groups = "wso2.greg", description = "Add lifecycle to a resource at root level",dependsOnMethods = "testVersionRetentionLeaf")
    public void testAddLcRoot() throws RegistryException, IOException,
                                       CustomLifecyclesChecklistAdminServiceExceptionException,
                                       ListMetadataServiceRegistryExceptionException,
                                       ResourceAdminServiceExceptionException,
                                       LifeCycleManagementServiceExceptionException {
        testAddNewLifeCycle(LC_NAME, "MultiplePromoteDemoteLC.xml");
        wsRegistryServiceClient.associateAspect(PATHROOT, LC_NAME);
        lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(PATHROOT);

        org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property[] properties = lifeCycle.getLifecycleProperties();
        boolean lcStatus = false;
        for (org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property prop : properties) {
            prop.getKey();
            if (prop.getKey().contains(LC_NAME)) {
                lcStatus = true;
            }
        }
        assertTrue(lcStatus);
        lcStatus = false;
        resourceAdminClient.createVersion(PATHROOT);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATHROOT);
        String verPath = vp1[0].getCompleteVersionPath();
        lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(verPath);

        org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property[] properties2 = lifeCycle.getLifecycleProperties();

        for (org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property prop : properties2) {
            prop.getKey();
            if (prop.getKey().contains(LC_NAME)) {
                lcStatus = true;
            }
        }
        assertTrue(lcStatus);
        assertNull(deleteVersion(PATHROOT));
    }


    @Test(groups = "wso2.greg", description = "Add lifecycle to a resource at leaf level", dependsOnMethods = "testAddLcRoot")
    public void testAddLcLeaf() throws RegistryException, IOException,
                                       CustomLifecyclesChecklistAdminServiceExceptionException,
                                       ListMetadataServiceRegistryExceptionException,
                                       ResourceAdminServiceExceptionException,
                                       LifeCycleManagementServiceExceptionException {
        testAddNewLifeCycle(LC_NAME_2, "StateDemoteLifeCycle.xml");

        wsRegistryServiceClient.associateAspect(PATHLEAF, LC_NAME_2);
        lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(PATHLEAF);

        org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property[] properties = lifeCycle.getLifecycleProperties();
        boolean lcStatus = false;
        for (org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property prop : properties) {
            prop.getKey();
            if (prop.getKey().contains(LC_NAME_2)) {
                lcStatus = true;
            }
        }
        assertTrue(lcStatus);
        lcStatus = false;
        resourceAdminClient.createVersion(PATHLEAF);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATHLEAF);
        String verPath = vp1[0].getCompleteVersionPath();
        lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(verPath);

        org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property[] properties2 = lifeCycle.getLifecycleProperties();

        for (org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property prop : properties2) {
            prop.getKey();
            if (prop.getKey().contains(LC_NAME_2)) {
                lcStatus = true;
            }
        }
        assertTrue(lcStatus);
        assertNull(deleteVersion(PATHLEAF));
    }

    @AfterClass(groups = "wso2.greg")
    public void deleteResources() throws ResourceAdminServiceExceptionException, RemoteException,
                                         LifeCycleManagementServiceExceptionException,
                                         RegistryException {
        deleteResource(PATHLEAF);
        deleteResource(PATHROOT);
        deleteResource(PATH);
        lifeCycleManagementClient.deleteLifeCycle(LC_NAME);
        lifeCycleManagementClient.deleteLifeCycle(LC_NAME_2);
        resourceAdminClient = null;
        propertiesAdminServiceClient = null;
        relationAdminServiceClient = null;
        infoServiceAdminClient = null;
        lifeCycleManagementClient = null;
        lifeCycleAdminServiceClient = null;
        wsRegistryServiceClient = null;
        environment = null;

    }

    public void deleteResource(String path) throws RegistryException {
        if (wsRegistryServiceClient.resourceExists(path)) {
            wsRegistryServiceClient.delete(path);
        }
    }

    public VersionPath[] deleteVersion(String path)
            throws ResourceAdminServiceExceptionException, RemoteException {
        long versionNo = resourceAdminClient.getVersionPaths(path)[0].getVersionNumber();
        String snapshotId = String.valueOf(versionNo);
        resourceAdminClient.deleteVersionHistory(path, snapshotId);
        VersionPath[] vp2;
        vp2 = resourceAdminClient.getVersionPaths(path);
        return vp2;
    }
}


