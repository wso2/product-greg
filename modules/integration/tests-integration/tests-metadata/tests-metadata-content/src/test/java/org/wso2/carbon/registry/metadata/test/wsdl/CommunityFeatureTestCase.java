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
package org.wso2.carbon.registry.metadata.test.wsdl;

import org.apache.axis2.AxisFault;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.governance.api.endpoints.dataobjects.Endpoint;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.beans.xsd.LifecycleBean;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.info.stub.RegistryExceptionException;
import org.wso2.carbon.registry.info.stub.beans.utils.xsd.Comment;
import org.wso2.carbon.registry.info.stub.beans.utils.xsd.Tag;
import org.wso2.carbon.registry.info.stub.beans.xsd.CommentBean;
import org.wso2.carbon.registry.info.stub.beans.xsd.RatingBean;
import org.wso2.carbon.registry.info.stub.beans.xsd.TagBean;
import org.wso2.carbon.registry.metadata.test.util.RegistryConstants;
import org.wso2.carbon.registry.relations.stub.AddAssociationRegistryExceptionException;
import org.wso2.carbon.registry.relations.stub.beans.xsd.AssociationTreeBean;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.beans.xsd.ResourceTreeEntryBean;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.clients.InfoServiceAdminClient;
import org.wso2.greg.integration.common.clients.LifeCycleAdminServiceClient;
import org.wso2.greg.integration.common.clients.RelationAdminServiceClient;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import javax.activation.DataHandler;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * This class used to add WSDL files in to the governance registry using resource-admin command in the purpose for
 * wsdl addition test cases.
 */
public class CommunityFeatureTestCase extends GREGIntegrationBaseTest {

    private RelationAdminServiceClient relationAdminServiceClient;
    private InfoServiceAdminClient infoAdminServiceClient;
    private LifeCycleAdminServiceClient lifeCycleAdminServiceClient;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private ServiceManager serviceManager;
    private WsdlManager wsdlManager;
    private String sessionCookie;
    private Registry governance;
    private final String wsdlPath = "/_system/governance/trunk/wsdls/net/restfulwebservices/www/servicecontracts"
            + "/_2008/_01/1.0.0/GeoIPService.svc.wsdl";

    /**
     * This method used to init the wsdl addition test cases.
     *
     * @throws Exception
     */
    @BeforeClass(groups = { "wso2.greg" })
    public void init() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        sessionCookie = new LoginLogoutClient(automationContext).login();

        infoAdminServiceClient = new InfoServiceAdminClient(backendURL, sessionCookie);
        lifeCycleAdminServiceClient = new LifeCycleAdminServiceClient(backendURL, sessionCookie);
        relationAdminServiceClient = new RelationAdminServiceClient(backendURL, sessionCookie);
        resourceAdminServiceClient = new ResourceAdminServiceClient(backendURL, sessionCookie);
        WSRegistryServiceClient wsRegistryServiceClient = new RegistryProviderUtil().getWSRegistry(automationContext);

        governance = new RegistryProviderUtil().getGovernanceRegistry(wsRegistryServiceClient, automationContext);
        serviceManager = new ServiceManager(governance);
        wsdlManager = new WsdlManager(governance);
    }

    /**
     * This method act as the test case for wsdl addition.
     *
     * @throws RegistryExceptionException
     * @throws RemoteException
     * @throws InterruptedException
     * @throws MalformedURLException
     * @throws ResourceAdminServiceExceptionException
     */
    @Test(groups = { "wso2.greg" }, description = "addWSDL")
    public void addWSDL() throws RegistryExceptionException, RemoteException, InterruptedException,
            MalformedURLException, ResourceAdminServiceExceptionException {
        boolean isFound = false;
        String resource = getTestArtifactLocation() + "artifacts" + File.separator + "GREG" + File.separator + "wsdl"
                + File.separator + "sample.wsdl";
        resourceAdminServiceClient.addResource("/_system/governance/trunk/wsdls/sample.wsdl",
                RegistryConstants.APPLICATION_WSDL_XML, "txtDesc", new DataHandler(new URL("file:///" + resource)));
        resourceAdminServiceClient.importResource("/_system/governance/trunk/wsdls", "GeoIPService.svc.wsdl",
                RegistryConstants.APPLICATION_WSDL_XML, "txtDesc", "https://github.com/wso2/wso2-qa-artifacts/" +
                        "tree/master/automation-artifacts/greg/wsdl/GeoIPService/GeoIPService.svc.wsdl", null);
        ResourceTreeEntryBean searchFileOne = resourceAdminServiceClient.getResourceTreeEntryBean
                ("/_system/governance/trunk/wsdls/eu/dataaccess/footballpool/1.0.0");
        ResourceTreeEntryBean searchFileTwo = resourceAdminServiceClient.getResourceTreeEntryBean
                ("/_system/governance/trunk/wsdls/net/restfulwebservices/www/servicecontracts/_2008/_01/1.0.0");

        String[] resourceChildOne = searchFileOne.getChildren();
        for (int childCount = 0; childCount <= resourceChildOne.length; childCount++) {
            if (resourceChildOne[childCount]
                    .equalsIgnoreCase("/_system/governance/trunk/wsdls/eu/dataaccess/footballpool/1.0.0/sample.wsdl")) {
                isFound = true;
                break;
            }
        }
        assertTrue(isFound);

        isFound = false;
        String[] resourceChildTwo = searchFileTwo.getChildren();
        for (int childCount = 0; childCount <= resourceChildTwo.length; childCount++) {
            if (resourceChildTwo[childCount].equalsIgnoreCase(wsdlPath)) {
                isFound = true;
                break;
            }
        }
        assertTrue(isFound);
    }

    /**
     * This method act as the test case for adding associations for created wsdl.
     *
     * @throws AddAssociationRegistryExceptionException
     * @throws RemoteException
     */
    @Test(groups = { "wso2.greg" }, dependsOnMethods = { "addWSDL" })
    public void associationTest() throws AddAssociationRegistryExceptionException, RemoteException {
        //check association is in position
        AssociationTreeBean associationTreeBean = relationAdminServiceClient.getAssociationTree(wsdlPath,
                "association");
        assertTrue(associationTreeBean.getAssociationTree().contains("usedBy"));
    }

    /**
     * This method act as the test case for adding dependency for added wsdl.
     *
     * @throws AddAssociationRegistryExceptionException
     * @throws RemoteException
     */
    @Test(groups = { "wso2.greg" }, dependsOnMethods = { "addWSDL" })
    public void dependencyTest() throws AddAssociationRegistryExceptionException, RemoteException {
        //check dependency information is in position
        AssociationTreeBean associationTreeBean = relationAdminServiceClient.getAssociationTree(wsdlPath, "depends");
        assertTrue(associationTreeBean.getAssociationTree().contains
                ("/_system/governance/trunk/endpoints/ep-net.restfulwebservices.www.wcf-GeoIPService-svc"));
    }

    /**
     * This method act as the test case for testing commenting on add wsdl.
     *
     * @throws RegistryException
     * @throws AxisFault
     * @throws RegistryExceptionException
     */
    @Test(groups = { "wso2.greg" }, dependsOnMethods = { "addWSDL" })
    public void commentTest() throws RegistryException, AxisFault, RegistryExceptionException {
        infoAdminServiceClient.addComment("this is sample comment", wsdlPath, sessionCookie);
        infoAdminServiceClient.addComment("this is sample comment2", wsdlPath, sessionCookie);
        CommentBean commentBean = infoAdminServiceClient.getComments(wsdlPath, sessionCookie);
        Comment[] comment = commentBean.getComments();
        assertTrue(comment[0].getDescription().equalsIgnoreCase("this is sample comment"));
        assertTrue(comment[1].getDescription().equalsIgnoreCase("this is sample comment2"));
        infoAdminServiceClient.removeComment(comment[0].getCommentPath(), sessionCookie);
        commentBean = infoAdminServiceClient.getComments(wsdlPath, sessionCookie);
        comment = commentBean.getComments();
        assertFalse(comment[0].getDescription().equalsIgnoreCase("this is sample comment"));
    }

    /**
     * This method act as the test case for adding a tag to the added wsdl.
     *
     * @throws RegistryException
     * @throws AxisFault
     * @throws RegistryExceptionException
     */
    @Test(groups = { "wso2.greg" }, dependsOnMethods = { "dependencyTest" })
    public void tagTest() throws RegistryException, AxisFault, RegistryExceptionException {
        infoAdminServiceClient.addTag("SampleTag", wsdlPath, sessionCookie);
        TagBean tagBean = infoAdminServiceClient.getTags(wsdlPath, sessionCookie);
        Tag[] tag = tagBean.getTags();
        for (int i = 0; i <= tag.length - 1; i++) {
            assertTrue(tag[i].getTagName().equalsIgnoreCase("SampleTag"));
        }
    }

    /**
     * This method act as the test case for rating the added wsdl.
     *
     * @throws RegistryException
     * @throws RegistryExceptionException
     */
    @Test(groups = { "wso2.greg" }, dependsOnMethods = { "commentTest" })
    public void rateTest() throws RegistryException, RegistryExceptionException {
        infoAdminServiceClient.rateResource("2", wsdlPath, sessionCookie);
        RatingBean ratingBean = infoAdminServiceClient.getRatings(wsdlPath, sessionCookie);
        assertEquals(ratingBean.getUserRating(), 2);
    }

    /**
     * This method act as the test case for adding a lifecycle for the added wsdl.
     *
     * @throws Exception
     */
    @Test(groups = { "wso2.greg" }, dependsOnMethods = { "rateTest" })
    public void lifeCycleTest() throws Exception {
        String[] lifeCycleItem = { "Requirements Gathered", "Architecture Finalized", "High Level Design Completed" };
        lifeCycleAdminServiceClient.addAspect(wsdlPath, "ServiceLifeCycle");
        lifeCycleAdminServiceClient.invokeAspect(wsdlPath, "ServiceLifeCycle", "Promote", lifeCycleItem);
        LifecycleBean lifecycleBean = lifeCycleAdminServiceClient.getLifecycleBean(wsdlPath);
        Property[] lifecycleProperties = lifecycleBean.getLifecycleProperties();
        for (Property property : lifecycleProperties) {
            if (property.getKey().equals("registry.lifecycle.ServiceLifeCycle.state")) {
                assertTrue("Testing".equalsIgnoreCase(property.getValues()[0]));
            }
        }
        lifeCycleAdminServiceClient.removeAspect(wsdlPath, "ServiceLifeCycle");
    }

    /**
     * THis method act as the test case cleaning process after the wsdl test case.
     *
     * @throws ResourceAdminServiceExceptionException
     * @throws RemoteException
     * @throws RegistryException
     */
    @AfterClass(groups = { "wso2.greg" })
    public void deleteResources() throws ResourceAdminServiceExceptionException, RemoteException, RegistryException {

        Endpoint[] endpoints = null;
        Endpoint[] endPointsOther = null;

        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);

        Wsdl[] wsdls = wsdlManager.getAllWsdls();
        for (Wsdl wsdl : wsdls) {
            if (wsdl.getQName().getLocalPart().equals("sample.wsdl")) {
                endpoints = wsdlManager.getWsdl(wsdl.getId()).getAttachedEndpoints();
            } else if (wsdl.getQName().getLocalPart().equals("GeoIPService.svc.wsdl")) {
                endPointsOther = wsdlManager.getWsdl(wsdl.getId()).getAttachedEndpoints();
            }
        }
        resourceAdminServiceClient.deleteResource(
                "/_system/governance/trunk/wsdls/eu/dataaccess/footballpool/1.0.0/sample.wsdl");
        resourceAdminServiceClient.deleteResource(wsdlPath);
        resourceAdminServiceClient.deleteResource
                ("/_system/governance/trunk/schemas/net/restfulwebservices/www/datacontracts/_2008/_01/1.0"
                        + ".0/GeoIPService.svc.xsd");
        resourceAdminServiceClient.deleteResource
                ("/_system/governance/trunk/schemas/faultcontracts/gotlservices/_2008/_01/1.0.0/GeoIPService3.xsd");
        resourceAdminServiceClient.deleteResource
                ("/_system/governance/trunk/schemas/net/restfulwebservices/www/servicecontracts/_2008/_01/1.0"
                        + ".0/GeoIPService1.xsd");
        resourceAdminServiceClient.deleteResource
                ("/_system/governance/trunk/schemas/com/microsoft/schemas/_2003/_10/serialization/1.0.0/GeoIPService2"
                        + ".xsd");
        for (Endpoint path : endpoints) {
            resourceAdminServiceClient.deleteResource("_system/governance/" + path.getPath());
        }
        for (Endpoint path : endPointsOther) {
            resourceAdminServiceClient.deleteResource("_system/governance/" + path.getPath());
        }

        for (Service service : serviceManager.getAllServices()) {
            if (service.getQName().getLocalPart().equals("Info")) {
                serviceManager.removeService(service.getId());
            } else if (service.getQName().getLocalPart().equals("GeoIPService")) {
                serviceManager.removeService(service.getId());
            }
        }
        infoAdminServiceClient = null;
        relationAdminServiceClient = null;
        resourceAdminServiceClient = null;
        lifeCycleAdminServiceClient = null;
        serviceManager = null;
        wsdlManager = null;
    }
}
