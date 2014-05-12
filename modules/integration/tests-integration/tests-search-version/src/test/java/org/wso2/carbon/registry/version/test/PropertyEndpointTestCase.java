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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.automation.test.utils.common.FileManager;
import org.wso2.carbon.governance.api.endpoints.EndpointManager;
import org.wso2.carbon.governance.api.endpoints.dataobjects.Endpoint;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.beans.xsd.LifecycleBean;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.governance.list.stub.ListMetadataServiceRegistryExceptionException;
import org.wso2.carbon.registry.core.Registry;
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
import org.wso2.greg.integration.common.clients.*;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import javax.activation.DataHandler;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class PropertyEndpointTestCase extends GREGIntegrationBaseTest {


    private static Registry governance = null;
    private ResourceAdminServiceClient resourceAdminClient;
    private static final Log log = LogFactory.getLog(ListMetaDataServiceClient.class);
    private Endpoint endpoint;
    private static final String PATH_ROOT = "/PropertyEndpointTestCase1";

    private PropertiesAdminServiceClient propertiesAdminServiceClient;
    private RelationAdminServiceClient relationAdminServiceClient;
    private InfoServiceAdminClient infoServiceAdminClient;
    private LifeCycleManagementClient lifeCycleManagementClient;
    private WSRegistryServiceClient wsRegistryServiceClient;
    private LifeCycleAdminServiceClient lifeCycleAdminServiceClient;
    private String PATH_CONSTANT = "/_system/governance";
    private String sessionCookie;
    private String backEndUrl;
    private String userName;
    private String userNameWithoutDomain;

    private static String LC_NAME = "MultiplePromoteDemoteLC";

    @BeforeClass
    public void initializeRegistry()
            throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        backEndUrl = getBackendURL();
        sessionCookie = getSessionCookie();
        userName = automationContext.getContextTenant().getContextUser().getUserName();

        if (userName.contains("@"))
            userNameWithoutDomain = userName.substring(0, userName.indexOf('@'));
        else
            userNameWithoutDomain = userName;

        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        resourceAdminClient =
                new ResourceAdminServiceClient(backEndUrl,
                                               sessionCookie);
        WSRegistryServiceClient registry =
                registryProviderUtil.getWSRegistry(automationContext);
        governance =
                registryProviderUtil.getGovernanceRegistry(registry, automationContext);
        propertiesAdminServiceClient =
                new PropertiesAdminServiceClient(backEndUrl,
                                                 sessionCookie);
        lifeCycleManagementClient =
                new LifeCycleManagementClient(backEndUrl,
                                              sessionCookie);
        resourceAdminClient =
                new ResourceAdminServiceClient(backEndUrl,
                                               sessionCookie);
        lifeCycleAdminServiceClient = new LifeCycleAdminServiceClient(backEndUrl,
                                                                      sessionCookie);
        wsRegistryServiceClient = registryProviderUtil.getWSRegistry(automationContext);

        relationAdminServiceClient = new RelationAdminServiceClient(backEndUrl,
                                                                    sessionCookie);

        infoServiceAdminClient = new InfoServiceAdminClient(backEndUrl,
                                                            sessionCookie);

        String endpoint_url = "http://DoNotCallRegistryUnique";
        //create an endpoint
        endpoint = createEndpoint(endpoint_url);
        assertTrue(registry.resourceExists(PATH_CONSTANT + endpoint.getPath()), "Endpoint Resource Does not exists :");


        //Create a resource at root level

        String path1 = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator
                       + "GREG" + File.separator + "testresource.txt";
        DataHandler dataHandler1 = new DataHandler(new URL("file:///" + path1));
        resourceAdminClient.addResource(PATH_ROOT, "text/plain", "desc", dataHandler1);
        Assert.assertTrue(resourceAdminClient.getResource(PATH_ROOT)[0].getAuthorUserName().contains(userNameWithoutDomain));
    }


    private Endpoint createEndpoint(String endpoint_url) throws GovernanceException {
        EndpointManager endpointManager = new EndpointManager(governance);
        Endpoint endpoint1;
        try {
            endpoint1 = endpointManager.newEndpoint(endpoint_url);
            endpoint1.addAttribute("status1", "QA");
            endpoint1.addAttribute("status2", "Dev");
            endpointManager.addEndpoint(endpoint1);
            log.info("Endpoint was successfully added");
        } catch (GovernanceException e) {
            log.error("Unable add Endpoint:" + e);
            throw new GovernanceException("Unable to add Endpoint:" + e);
        }
        return endpoint1;
    }

    @Test(groups = {"wso2.greg"}, description = "Add a property to an Endpoint,version it and check it")
    public void testVersionPropertyRoot()
            throws ResourceAdminServiceExceptionException, RegistryException,
                   PropertiesAdminServiceRegistryExceptionException, RemoteException {
        boolean status = false;
        int count = 0;
        propertiesAdminServiceClient.setProperty(PATH_CONSTANT + endpoint.getPath(),
                                                 "name1", "value1");
        Property[] properties1 =
                propertiesAdminServiceClient.getProperty(PATH_CONSTANT + endpoint.getPath(),
                                                         "true").getProperties();

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
        Assert.assertTrue(status);
        status = false;
        count = 0;
        resourceAdminClient.createVersion(PATH_CONSTANT + endpoint.getPath());
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATH_CONSTANT + endpoint.getPath());
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
        Assert.assertTrue(status);
        assertNull(deleteVersion(PATH_CONSTANT + endpoint.getPath()));


    }


    @Test(groups = {"wso2.greg"}, description = "Add a association to an endpoint,version it and check that association")
    public void testVersionAssociation()
            throws ResourceAdminServiceExceptionException, RemoteException, GovernanceException,
                   AddAssociationRegistryExceptionException {

        relationAdminServiceClient.addAssociation(PATH_CONSTANT + endpoint.getPath(), "usedBy", PATH_ROOT, "add");
        AssociationTreeBean associationTreeBean = relationAdminServiceClient.getAssociationTree(PATH_CONSTANT + endpoint.getPath(), "usedBy");

        String resourcePath = associationTreeBean.getAssociationTree();
        Assert.assertTrue(resourcePath.contains(PATH_ROOT));
        resourceAdminClient.createVersion(PATH_CONSTANT + endpoint.getPath());
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATH_CONSTANT + endpoint.getPath());
        String verPath = vp1[0].getCompleteVersionPath();
        Assert.assertTrue(relationAdminServiceClient.getAssociationTree(verPath, "usedBy").getAssociationTree().contains(PATH_ROOT));


        assertNull(deleteVersion(PATH_CONSTANT + endpoint.getPath()));


    }


    @Test(groups = {"wso2.greg"}, description = "Add a dependency to an endpoint ,version it and check for that dependency")
    public void testVersionDependency()
            throws ResourceAdminServiceExceptionException, RemoteException, GovernanceException,
                   AddAssociationRegistryExceptionException {
        relationAdminServiceClient.addAssociation(PATH_CONSTANT + endpoint.getPath(), "depends", PATH_ROOT, "add");
        AssociationTreeBean associationTreeBean = relationAdminServiceClient.getAssociationTree(PATH_CONSTANT + endpoint.getPath(), "depends");
        String resourcePath = associationTreeBean.getAssociationTree();
        Assert.assertTrue(resourcePath.contains(PATH_ROOT));
        resourceAdminClient.createVersion(PATH_CONSTANT + endpoint.getPath());
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATH_CONSTANT + endpoint.getPath());
        String verPath = vp1[0].getCompleteVersionPath();
        Assert.assertTrue(relationAdminServiceClient.getAssociationTree(verPath, "depends").getAssociationTree().contains(PATH_ROOT));
        assertNull(deleteVersion(PATH_CONSTANT + endpoint.getPath()));

    }


    @Test(groups = {"wso2.greg"}, description = "Add a tag to an endpoint ,version it and check that tag ")
    public void testVersionTag()
            throws RegistryException, RemoteException, ResourceAdminServiceExceptionException,
                   RegistryExceptionException {
        boolean status = false;
        infoServiceAdminClient.addTag("testTag", PATH_CONSTANT + endpoint.getPath(), sessionCookie);
        TagBean tagBean = infoServiceAdminClient.getTags(PATH_CONSTANT + endpoint.getPath(), sessionCookie);
        Tag[] tags1 = tagBean.getTags();

        for (Tag aTags1 : tags1) {
            if (aTags1.getTagName().equals("testTag")) {
                status = true;
            }
        }


        Assert.assertTrue(status);
        resourceAdminClient.createVersion(PATH_CONSTANT + endpoint.getPath());
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATH_CONSTANT + endpoint.getPath());
        String verPath = vp1[0].getCompleteVersionPath();

        TagBean tagBean1 = infoServiceAdminClient.getTags(verPath, sessionCookie);
        Tag[] tags2 = tagBean1.getTags();
        for (Tag aTags2 : tags2) {
            if (aTags2.getTagName().equals("testTag")) {
                status = true;
                break;
            }
        }
        Assert.assertTrue("test tags was not found in the version path", status);
        assertNull(deleteVersion(PATH_CONSTANT + endpoint.getPath()));


    }


    @Test(groups = {"wso2.greg"}, description = "Add a comment to an endpoint,version it and check that comment ")
    public void testVersionCommentR()
            throws RegistryException, RemoteException, RegistryExceptionException,
                   ResourceAdminServiceExceptionException {
        infoServiceAdminClient.addComment("This is a comment", PATH_CONSTANT + endpoint.getPath(),
                                          sessionCookie);
        CommentBean commentBean =
                infoServiceAdminClient.getComments(PATH_CONSTANT + endpoint.getPath(),
                                                   sessionCookie);
        assertEquals("This is a comment", commentBean.getComments()[0].getContent());
        resourceAdminClient.createVersion(PATH_CONSTANT + endpoint.getPath());
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATH_CONSTANT + endpoint.getPath());
        String verPath = vp1[0].getCompleteVersionPath();
        assertEquals("This is a comment", infoServiceAdminClient.getComments(verPath, sessionCookie).getComments()[0].getContent());
        assertNull(deleteVersion(PATH_CONSTANT + endpoint.getPath()));
    }

    @Test(groups = {"wso2.greg"}, description = "Add ratings to an endpoint ,version it and check ratings ")
    public void testVersionRatingR() throws RegistryException, RegistryExceptionException,
                                            ResourceAdminServiceExceptionException,
                                            RemoteException {
        String sessionId = sessionCookie;
        //add ratings for the end point
        infoServiceAdminClient.rateResource("2", PATH_CONSTANT + endpoint.getPath(), sessionId);
        RatingBean ratingBean = infoServiceAdminClient.getRatings(PATH_CONSTANT + endpoint.getPath(), sessionId);
        assertEquals(2, ratingBean.getUserRating());
        resourceAdminClient.createVersion(PATH_CONSTANT + endpoint.getPath());
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATH_CONSTANT + endpoint.getPath());
        String verPath = vp1[0].getCompleteVersionPath();
        assertEquals(2, infoServiceAdminClient.getRatings(verPath, sessionId).getUserRating());
        assertNull(deleteVersion(PATH_CONSTANT + endpoint.getPath()));

    }


    @Test(groups = {"wso2.greg"}, description = "Add retention to an endpoint,version it and check retention ")
    public void testVersionRetentionRoot()
            throws PropertiesAdminServiceRegistryExceptionException, RemoteException,
                   ResourceAdminServiceExceptionException, GovernanceException {
        propertiesAdminServiceClient.setRetentionProperties(PATH_CONSTANT + endpoint.getPath(), "write", "07/02/2012", "08/22/2012");
        assertEquals("07/02/2012", propertiesAdminServiceClient.getRetentionProperties(PATH_CONSTANT + endpoint.getPath()).getFromDate());
        assertEquals("08/22/2012", propertiesAdminServiceClient.getRetentionProperties(PATH_CONSTANT + endpoint.getPath()).getToDate());
        resourceAdminClient.createVersion(PATH_CONSTANT + endpoint.getPath());
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATH_CONSTANT + endpoint.getPath());
        String verPath = vp1[0].getCompleteVersionPath();
        assertEquals("07/02/2012", propertiesAdminServiceClient.getRetentionProperties(verPath).getFromDate());
        assertEquals("08/22/2012", propertiesAdminServiceClient.getRetentionProperties(verPath).getToDate());
        assertNull(deleteVersion(PATH_CONSTANT + endpoint.getPath()));

    }

    private void testCreateNewLifeCycle()
            throws LifeCycleManagementServiceExceptionException, IOException {
        String resourcePath = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator + "GREG" + File.separator + "lifecycle" + File.separator + "MultiplePromoteDemoteLC.xml";
        String lifeCycleContent = FileManager.readFile(resourcePath);
        lifeCycleManagementClient.addLifeCycle(lifeCycleContent);

        String[] lifeClycles = lifeCycleManagementClient.getLifecycleList();
        boolean lcStatus = false;
        for (String lc : lifeClycles) {
            if (lc.equalsIgnoreCase(LC_NAME)) {
                lcStatus = true;
            }
        }
        Assert.assertTrue(lcStatus);
    }

    @Test(groups = "wso2.greg", description = "Add lifecycle to an endpoint")
    public void testAddLc() throws RegistryException, IOException,
                                   CustomLifecyclesChecklistAdminServiceExceptionException,
                                   ListMetadataServiceRegistryExceptionException,
                                   ResourceAdminServiceExceptionException,
                                   LifeCycleManagementServiceExceptionException {

        testCreateNewLifeCycle();
        wsRegistryServiceClient.associateAspect(PATH_CONSTANT + endpoint.getPath(), LC_NAME);
        LifecycleBean lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(PATH_CONSTANT + endpoint.getPath());

        org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property[] properties = lifeCycle.getLifecycleProperties();
        boolean lcStatus = false;
        for (org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property prop : properties) {
            prop.getKey();
            if (prop.getKey().contains(LC_NAME)) {
                lcStatus = true;
            }
        }
        Assert.assertTrue(lcStatus);
        lcStatus = false;
        resourceAdminClient.createVersion(PATH_CONSTANT + endpoint.getPath());
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths(PATH_CONSTANT + endpoint.getPath());
        String verPath = vp1[0].getCompleteVersionPath();
        lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(verPath);

        org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property[] properties2 = lifeCycle.getLifecycleProperties();

        for (org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property prop : properties2) {
            prop.getKey();
            if (prop.getKey().contains(LC_NAME)) {
                lcStatus = true;
            }
        }
        Assert.assertTrue(lcStatus);
        assertNull(deleteVersion(PATH_CONSTANT + endpoint.getPath()));
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

    @AfterClass
    public void clear()
            throws RegistryException, ResourceAdminServiceExceptionException, RemoteException,
                   LifeCycleManagementServiceExceptionException {
        deleteResource(PATH_ROOT);
        EndpointManager endpointManager = new EndpointManager(governance);
        endpointManager.removeEndpoint(endpoint.getId());
        lifeCycleManagementClient.deleteLifeCycle(LC_NAME);
        governance = null;
        resourceAdminClient = null;
        endpoint = null;
        propertiesAdminServiceClient = null;
        relationAdminServiceClient = null;
        infoServiceAdminClient = null;
        lifeCycleManagementClient = null;
        lifeCycleAdminServiceClient = null;
        wsRegistryServiceClient = null;
    }

    public void deleteResource(String path) throws RegistryException {
        if (wsRegistryServiceClient.resourceExists(path)) {
            wsRegistryServiceClient.delete(path);
        }
    }
}
