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
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.automation.test.utils.common.FileManager;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.services.ServiceFilter;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.beans.xsd.LifecycleBean;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.governance.list.stub.ListMetadataServiceRegistryExceptionException;
import org.wso2.carbon.governance.services.stub.AddServicesServiceRegistryExceptionException;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.info.stub.RegistryExceptionException;
import org.wso2.carbon.registry.info.stub.beans.utils.xsd.Tag;
import org.wso2.carbon.registry.info.stub.beans.xsd.CommentBean;
import org.wso2.carbon.registry.info.stub.beans.xsd.RatingBean;
import org.wso2.carbon.registry.info.stub.beans.xsd.TagBean;
import org.wso2.carbon.registry.lifecycle.test.utils.LifeCycleUtils;
import org.wso2.carbon.registry.properties.stub.PropertiesAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.properties.stub.utils.xsd.Property;
import org.wso2.carbon.registry.relations.stub.AddAssociationRegistryExceptionException;
import org.wso2.carbon.registry.relations.stub.beans.xsd.AssociationTreeBean;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.beans.xsd.VersionPath;
import org.wso2.carbon.registry.resource.stub.common.xsd.ResourceData;
import org.wso2.carbon.registry.search.stub.SearchAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.version.test.utils.VersionUtils;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.clients.*;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import javax.activation.DataHandler;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Date;

import static junit.framework.Assert.*;
import static org.testng.Assert.assertNotNull;


public class PropertyServiceTestCase extends GREGIntegrationBaseTest {

    private ResourceAdminServiceClient resourceAdminClient;
    private static final String PATH1 = "/_system/governance/trunk/services/com/abb/1.0.0-SNAPSHOT/abc";
    private static final String PATH2 = "/_system/governance/trunk/services/com/def/1.0.0-SNAPSHOT/def";
    private PropertiesAdminServiceClient propertiesAdminServiceClient;
    private RelationAdminServiceClient relationAdminServiceClient;
    private InfoServiceAdminClient infoServiceAdminClient;
    private LifeCycleManagementClient lifeCycleManagementClient;
    private LifeCycleAdminServiceClient lifeCycleAdminServiceClient;
    private ServiceManager serviceManager;
//    private ListMetaDataServiceClient listMetaDataServiceClient;
    private GovernanceServiceClient governanceServiceClient;
    private WSRegistryServiceClient wsRegistryServiceClient;
    private static String LC_NAME = "MultiplePromoteDemoteLC";
    private Date createdDate;
    private SearchAdminServiceClient searchAdminServiceClient;
    private Registry governance;
    private String sessionCookie;
    private String backEndUrl;
    private String userName;
    private String userNameWithoutDomain;


    @BeforeClass(alwaysRun = true)
    public void initializeTests()
            throws Exception {

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
        propertiesAdminServiceClient = new PropertiesAdminServiceClient(backEndUrl,
                                                                        sessionCookie);
        relationAdminServiceClient = new RelationAdminServiceClient(backEndUrl,
                                                                    sessionCookie);

        infoServiceAdminClient = new InfoServiceAdminClient(backEndUrl,
                                                            sessionCookie);
        lifeCycleManagementClient = new LifeCycleManagementClient(backEndUrl,
                                                                  sessionCookie);
//        listMetaDataServiceClient =
//                new ListMetaDataServiceClient(backEndUrl,
//                                              sessionCookie);
        governanceServiceClient =
                new GovernanceServiceClient(backEndUrl,
                                            sessionCookie);

        WSRegistryServiceClient wsRegistry =
                new RegistryProviderUtil().getWSRegistry(automationContext);

         governance = new RegistryProviderUtil().getGovernanceRegistry(wsRegistry, automationContext);
        serviceManager = new ServiceManager(governance);


        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        wsRegistryServiceClient = registryProviderUtil.getWSRegistry(automationContext);

        lifeCycleAdminServiceClient = new LifeCycleAdminServiceClient(backEndUrl,
                                                                      sessionCookie);
        searchAdminServiceClient = new SearchAdminServiceClient(backEndUrl,
                                                                      sessionCookie);
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
        addService();
    }


    public void addService()
            throws ResourceAdminServiceExceptionException, IOException, XMLStreamException,
            AddServicesServiceRegistryExceptionException, InterruptedException, RegistryException {
        Boolean nameExists = false;
        String serviceContent;
        String path = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator
                      + "GREG" + File.separator + "services" + File.separator + "service.metadata.xml";
        DataHandler dataHandler = new DataHandler(new URL("file:///" + path));
        String mediaType = "application/vnd.wso2-service+xml";
        String description = "This is a test service";
        resourceAdminClient.addResource(
                "/_system/governance/service2", mediaType, description, dataHandler);
        Thread.sleep(4000);
        VersionUtils.deleteAllVersions(resourceAdminClient, PATH2);
        createdDate = resourceAdminClient.getResource(PATH1)[0].getCreatedOn().getTime();


        //ResourceData[] data =  resourceAdminClient.getResource("/_system/governance/trunk/services/com/abb/abc");
        ResourceData[] data =  resourceAdminClient.getResource("/_system/governance/trunk/services/com/abb/1.0.0-SNAPSHOT/abc");
        
        assertNotNull(data, "Service not found");
        
        VersionUtils.deleteAllVersions(resourceAdminClient, PATH1);
        nameExists = false;

        String path2 = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator
                       + "GREG" + File.separator + "services" + File.separator + "xservice.xml";
        dataHandler = new DataHandler(new URL("file:///" + path2));
        mediaType = "application/vnd.wso2-service+xml";
        description = "This is a test service";
        resourceAdminClient.addResource(
                "/_system/governance/service2", mediaType, description, dataHandler);
        GenericArtifactManager manager = new GenericArtifactManager(governance, "service");

        GenericArtifact[] serviceArtifacts = manager.getAllGenericArtifacts();

        for (GenericArtifact genericArtifact : serviceArtifacts) {
            String name = genericArtifact.getQName().getLocalPart();
            if (name.equalsIgnoreCase("def")) {
                nameExists = true;
            }
        }
        assertTrue(nameExists);
        VersionUtils.deleteAllVersions(resourceAdminClient, PATH2);
    }


    @Test(groups = {"wso2.greg"}, description = "Add a property to a service,version it and check it")
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


    @Test(groups = {"wso2.greg"}, description = "Add a association to a service ,version it and check that association")
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


    @Test(groups = {"wso2.greg"}, description = "Add a dependency to a service ,version it and check for that dependency")
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


    @Test(groups = {"wso2.greg"}, description = "Add a tag to a service,version it and check that tag ")
    public void TestVersionTag()
            throws RegistryException, RemoteException, ResourceAdminServiceExceptionException,
                   RegistryExceptionException {
        boolean status = false;
        infoServiceAdminClient.addTag("testTag", PATH1, sessionCookie);
        TagBean tagBean = infoServiceAdminClient.getTags(PATH1, sessionCookie);
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
        Tag[] tags2 = infoServiceAdminClient.getTags(verPath, sessionCookie).getTags();

        for (Tag tag : tags2) {
            if (tag.getTagName().equals("testTag")) {
                tagFound = true;
                break;
            }
        }
        assertTrue("test tags was not found in the version path", tagFound);
        assertNull(VersionUtils.deleteAllVersions(resourceAdminClient, PATH1));


    }

    @Test(groups = {"wso2.greg"}, description = "Add a comment to a service file,version it and check that comment ")
    public void testVersionCommentR()
            throws RegistryException, RemoteException, RegistryExceptionException,
                   ResourceAdminServiceExceptionException {
        infoServiceAdminClient.addComment("This is a comment", PATH1, sessionCookie);
        CommentBean commentBean = infoServiceAdminClient.getComments(PATH1, sessionCookie);
        assertEquals("This is a comment", commentBean.getComments()[0].getContent());
        resourceAdminClient.createVersion(PATH1);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATH1);
        String verPath = vp1[0].getCompleteVersionPath();
        assertEquals("This is a comment", infoServiceAdminClient.getComments(verPath, sessionCookie).getComments()[0].getContent());
        assertNull(VersionUtils.deleteAllVersions(resourceAdminClient, PATH1));
    }

    @Test(groups = {"wso2.greg"}, description = "Add ratings to a service ,version it and check ratings ")
    public void testVersionRatingR() throws RegistryException, RegistryExceptionException,
                                            ResourceAdminServiceExceptionException,
                                            RemoteException {
        String sessionId = sessionCookie;
        infoServiceAdminClient.rateResource("2", PATH1, sessionId);
        RatingBean ratingBean = infoServiceAdminClient.getRatings(PATH1, sessionId);
        assertEquals(2, ratingBean.getUserRating());
        resourceAdminClient.createVersion(PATH1);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATH1);
        String verPath = vp1[0].getCompleteVersionPath();
        assertEquals(2, infoServiceAdminClient.getRatings(verPath, sessionId).getUserRating());
        assertNull(VersionUtils.deleteAllVersions(resourceAdminClient, PATH1));

    }


    @Test(groups = {"wso2.greg"}, description = "Add description to a service ," +
                                                "version it and check description ")
    public void testVersionDescriptionLeaf()
            throws IOException, LifeCycleManagementServiceExceptionException,
                   ResourceAdminServiceExceptionException, GovernanceException {
        Service testService = serviceManager.findServices(new ServiceFilter() {

            public boolean matches(Service service) throws GovernanceException {
                String attributeVal = service.getAttribute("overview_name");
                return attributeVal != null && attributeVal.startsWith("abc");
            }
        })[0];
        assertTrue(testService.getAttribute("overview_description").equals("this is description 1"));

        resourceAdminClient.createVersion(PATH1);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATH1);
        String verPath = vp1[0].getCompleteVersionPath();
        Assert.assertTrue(resourceAdminClient.getTextContent(verPath).contains("this is description 1"));

        assertNull(VersionUtils.deleteAllVersions(resourceAdminClient, PATH1));


    }


    @Test(groups = {"wso2.greg"}, description = "Add retention to a service,version it and check retention ")
    public void testVersionRetentionRoot()
            throws PropertiesAdminServiceRegistryExceptionException, RemoteException,
                   ResourceAdminServiceExceptionException {
        propertiesAdminServiceClient.setRetentionProperties(PATH1, "write", "07/02/2012", "08/22/2040");
        assertEquals("07/02/2012", propertiesAdminServiceClient.getRetentionProperties(PATH1).getFromDate());
        assertEquals("08/22/2040", propertiesAdminServiceClient.getRetentionProperties(PATH1).getToDate());
        resourceAdminClient.createVersion(PATH1);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATH1);
        String verPath = vp1[0].getCompleteVersionPath();
        assertEquals("07/02/2012", propertiesAdminServiceClient.getRetentionProperties(verPath).getFromDate());
        assertEquals("08/22/2040", propertiesAdminServiceClient.getRetentionProperties(verPath).getToDate());
        assertNull(VersionUtils.deleteAllVersions(resourceAdminClient, PATH1));

    }


    //Create a new lifecycle
    public void testCreateNewLifeCycle()
            throws LifeCycleManagementServiceExceptionException, IOException,
                   SearchAdminServiceRegistryExceptionException, RegistryException {
        String resourcePath = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator +
                              "GREG" + File.separator + "lifecycle" + File.separator + "MultiplePromoteDemoteLC.xml";
        String lifeCycleContent = FileManager.readFile(resourcePath);
        LifeCycleUtils.deleteLcUsageResources(searchAdminServiceClient, wsRegistryServiceClient, LC_NAME);
        LifeCycleUtils.deleteLifeCycleIfExist(LC_NAME, lifeCycleManagementClient);
        if (lifeCycleExists(LC_NAME)) {
            lifeCycleManagementClient.deleteLifeCycle(LC_NAME);
        }
        lifeCycleManagementClient.addLifeCycle(lifeCycleContent);

        String[] lifeClycles = lifeCycleManagementClient.getLifecycleList();
        boolean lcStatus = false;
        for (String lc : lifeClycles) {
            if (lc.equalsIgnoreCase(LC_NAME)) {
                lcStatus = true;
            }
        }
        assertTrue(lcStatus);
    }

    @Test(groups = "wso2.greg", description = "Add lifecycle to a service, version it and check again")
    public void testAddLcToService() throws RegistryException, IOException,
                                            CustomLifecyclesChecklistAdminServiceExceptionException,
                                            ListMetadataServiceRegistryExceptionException,
                                            ResourceAdminServiceExceptionException,
                                            LifeCycleManagementServiceExceptionException,
                                            SearchAdminServiceRegistryExceptionException {

        testCreateNewLifeCycle();
        wsRegistryServiceClient.associateAspect(PATH1, LC_NAME);
        LifecycleBean lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(PATH1);

        org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property[] properties = lifeCycle.getLifecycleProperties();
        boolean lcStatus = false;
        for (org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property prop : properties) {
            prop.getKey();
            if (prop.getKey().contains(LC_NAME)) {
                lcStatus = true;
                break;
            }
        }
        assertTrue(lcStatus);

        resourceAdminClient.createVersion(PATH1);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATH1);
        String verPath = vp1[0].getCompleteVersionPath();
        lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(verPath);

        org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property[] properties2 = lifeCycle.getLifecycleProperties();
        lcStatus = false;
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


    @Test(groups = "wso2.greg", description = "Create a check point and check the accuracy of version details, version no, " +
                                              "last modified date and last modified by values")
    public void testServiceVersionAccuracy() throws Exception {
        boolean status = false;
        resourceAdminClient.createVersion(PATH1);
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATH1);
        String verPath = vp1[0].getCompleteVersionPath();

        Date modifiedDate = resourceAdminClient.getResource(verPath)[0].getCreatedOn().getTime();
        Date newDate;

        Calendar cal = Calendar.getInstance();
        cal.setTime(createdDate);
        cal.add(Calendar.SECOND, 60);
        newDate = cal.getTime();

        if ((modifiedDate.after(createdDate) || modifiedDate.equals(createdDate))  && modifiedDate.before(newDate)) {
            status = true;
        }

        assertTrue(status);
        //check the accuracy of last modify user
        assertEquals(userNameWithoutDomain, resourceAdminClient.getVersionPaths(PATH1)[0].getUpdater());
        //check the accuracy of version WSDL description
        Assert.assertTrue(resourceAdminClient.getTextContent(verPath).contains("this is description 1"));
        //check the accuracy of version number
        long prevVerNo = resourceAdminClient.getVersionPaths(PATH1)[0].getVersionNumber();
        resourceAdminClient.createVersion(PATH1);
        VersionPath[] versionPaths = resourceAdminClient.getVersionPaths(PATH1);
        status = false;
        for (VersionPath versionPath : versionPaths) {
            if (versionPath.getVersionNumber() == prevVerNo + 1) {
                status = true;
            }

        }
        assertTrue(status);
        assertNull(VersionUtils.deleteAllVersions(resourceAdminClient, PATH1));
    }


    @AfterClass
    public void deleteResources() throws ResourceAdminServiceExceptionException, RemoteException,
                                         LifeCycleManagementServiceExceptionException,
                                         RegistryException {
        deleteResource(PATH1);
        deleteResource(PATH2);
        deleteResource("/_system/governance/trunk/wsdls/com/foo/1.0.0-SNAPSHOT/def.wsdl");

        deleteResource("/_system/governance/trunk/wsdls/com/foo/1.0.0-SNAPSHOT/abc.wsdl");
        deleteResource("/_system/governance/trunk/schemas/org/bar/purchasing/1.0.0-SNAPSHOT/purchasing.xsd");

        lifeCycleManagementClient.deleteLifeCycle(LC_NAME);
        resourceAdminClient = null;
        propertiesAdminServiceClient = null;
        relationAdminServiceClient = null;
        infoServiceAdminClient = null;
        lifeCycleManagementClient = null;
        lifeCycleAdminServiceClient = null;
        wsRegistryServiceClient = null;
//        listMetaDataServiceClient = null;
        serviceManager = null;
        governanceServiceClient = null;
    }

    public void deleteResource(String path) throws RegistryException {
        if (wsRegistryServiceClient.resourceExists(path)) {
            wsRegistryServiceClient.delete(path);
        }
    }

    public boolean lifeCycleExists(String LcName)
            throws LifeCycleManagementServiceExceptionException, RemoteException {
        String[] lifeCycles = lifeCycleManagementClient.getLifecycleList();
        for (String lifeCycle : lifeCycles) {
            if (lifeCycle.equals(LcName)) {
                return true;
            }
        }
        return false;
    }
}
