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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.governance.api.endpoints.dataobjects.Endpoint;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.beans.xsd.LifecycleBean;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
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
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.*;

//import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.ExceptionException;

/**
 * This class used to add WSDL files in to the governance registry using resource-admin command.
 */
public class CommunityFeatureTestCase extends GREGIntegrationBaseTest{
    private static final Log log = LogFactory.getLog(CommunityFeatureTestCase.class);
    private RelationAdminServiceClient relationAdminServiceClient;
    private InfoServiceAdminClient infoAdminServiceClient;
    private LifeCycleAdminServiceClient lifeCycleAdminServiceClient;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private ServiceManager serviceManager;
    private WsdlManager wsdlManager;
    private String sessionCookie;

    @BeforeClass(groups = {"wso2.greg"})
    public void init() throws Exception {
        int userId = 0;
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        sessionCookie = new LoginLogoutClient(automationContext).login();


        relationAdminServiceClient =
                new RelationAdminServiceClient(backendURL,
                                               sessionCookie);
        infoAdminServiceClient =
                new InfoServiceAdminClient(backendURL,
                                           sessionCookie);
        lifeCycleAdminServiceClient =
                new LifeCycleAdminServiceClient(backendURL,
                                                sessionCookie);
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(backendURL,
                                               sessionCookie);
        WSRegistryServiceClient wsRegistry =
                new RegistryProviderUtil().getWSRegistry(automationContext);

        Registry governance = new RegistryProviderUtil().getGovernanceRegistry(wsRegistry, automationContext);
        serviceManager = new ServiceManager(governance);
        wsdlManager = new WsdlManager(governance);
    }

    @Test(groups = {"wso2.greg"}, description = "addWSDL")
    public void addWSDL() throws Exception {
        boolean isFound = false;
        String resource = getTestArtifactLocation() + "artifacts" + File.separator +
                          "GREG" + File.separator +
                          "wsdl" + File.separator + "sample.wsdl";
        resourceAdminServiceClient.addResource("/_system/governance/trunk/wsdls/sample.wsdl",
                                               RegistryConstants.APPLICATION_WSDL_XML, "txtDesc",
                new DataHandler(new URL("file:///" + resource)));
        resourceAdminServiceClient.importResource("/_system/governance/trunk/wsdls", "WeatherForecastService.wsdl",
                RegistryConstants.APPLICATION_WSDL_XML, "txtDesc",
                                                  "https://svn.wso2.org/repos/wso2/trunk/commons/qa/qa-artifacts/greg/wsdl/WeatherForecastService.wsdl", null);
        ResourceTreeEntryBean searchFileOne = resourceAdminServiceClient.getResourceTreeEntryBean
                ("/_system/governance/trunk/wsdls/eu/dataaccess/footballpool");
        ResourceTreeEntryBean searchFileTwo = resourceAdminServiceClient.getResourceTreeEntryBean
                ("/_system/governance/trunk/wsdls/net/restfulwebservices/www/servicecontracts/_2008/_01");
        String[] resourceChildOne = searchFileOne.getChildren();
        String[] resourceChildTwo = searchFileTwo.getChildren();
        for (int childCount = 0; childCount <= resourceChildOne.length; childCount++) {
            if (resourceChildOne[childCount].equalsIgnoreCase("/_system/governance/trunk/wsdls/eu/dataaccess/footballpool/sample.wsdl")) {
                isFound = true;
                break;
            }
        }
        assertTrue(isFound);
        for (int childCount = 0; childCount <= resourceChildTwo.length; childCount++) {
            if (resourceChildTwo[childCount].equalsIgnoreCase("/_system/governance/trunk/wsdls/net/restfulwebservices/www/servicecontracts/_2008/_01/WeatherForecastService.wsdl")) {
                isFound = true;
                break;
            }
        }
        assertTrue(isFound);


    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"addWSDL"})
    public void associationTest() throws AddAssociationRegistryExceptionException, RemoteException {
        AssociationTreeBean associationTreeBean = null;
        //check association is in position
        associationTreeBean = relationAdminServiceClient.getAssociationTree("/_system/governance/trunk/wsdls/net/restfulwebservices/www/servicecontracts/_2008/_01/WeatherForecastService.wsdl", "association");
        assertTrue(associationTreeBean.getAssociationTree().contains("usedBy"));
    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"addWSDL"})
    public void dependencyTest() throws AddAssociationRegistryExceptionException, RemoteException {
        AssociationTreeBean associationTreeBean = null;
        //check dependency information is in position
        associationTreeBean = relationAdminServiceClient.getAssociationTree("/_system/governance/trunk/wsdls/net/restfulwebservices/www/servicecontracts/_2008/_01/WeatherForecastService.wsdl", "depends");
        assertTrue(associationTreeBean.getAssociationTree().contains("/_system/governance/trunk/endpoints/net/restfulwebservices/www/wcf/ep-WeatherForecastService-svc"));
    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"addWSDL"})
    public void commentTest() throws RegistryException, AxisFault, RegistryExceptionException {

        infoAdminServiceClient.addComment("this is sample comment", "/_system/governance/trunk/wsdls/net/restfulwebservices/www/servicecontracts/_2008/_01/WeatherForecastService.wsdl", sessionCookie);
        infoAdminServiceClient.addComment("this is sample comment2", "/_system/governance/trunk/wsdls/net/restfulwebservices/www/servicecontracts/_2008/_01/WeatherForecastService.wsdl", sessionCookie);
        CommentBean commentBean = infoAdminServiceClient.getComments("/_system/governance/trunk/wsdls/net/restfulwebservices/www/servicecontracts/_2008/_01/WeatherForecastService.wsdl", sessionCookie);
        Comment[] comment = commentBean.getComments();
        assertTrue(comment[0].getDescription().equalsIgnoreCase("this is sample comment"));
        assertTrue(comment[1].getDescription().equalsIgnoreCase("this is sample comment2"));
        infoAdminServiceClient.removeComment(comment[0].getCommentPath(), sessionCookie);
        commentBean = infoAdminServiceClient.getComments("/_system/governance/trunk/wsdls/net/restfulwebservices/www/servicecontracts/_2008/_01/WeatherForecastService.wsdl", sessionCookie);
        comment = commentBean.getComments();
        assertFalse(comment[0].getDescription().equalsIgnoreCase("this is sample comment"));
    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"dependencyTest"})
    public void tagTest() throws RegistryException, AxisFault, RegistryExceptionException {

        TagBean tagBean;

        infoAdminServiceClient.addTag("SampleTag", "/_system/governance/trunk/wsdls/net/restfulwebservices/www/servicecontracts/_2008/_01/WeatherForecastService.wsdl", sessionCookie);
        tagBean = infoAdminServiceClient.getTags("/_system/governance/trunk/wsdls/net/restfulwebservices/www/servicecontracts/_2008/_01/WeatherForecastService.wsdl", sessionCookie);
        Tag[] tag = tagBean.getTags();
        for (int i = 0; i <= tag.length - 1; i++) {
            assertTrue(tag[i].getTagName().equalsIgnoreCase("SampleTag"));
        }

    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"commentTest"})
    public void rateTest() throws RegistryException, RegistryExceptionException {
        RatingBean ratingBean;

        infoAdminServiceClient.rateResource("2", "/_system/governance/trunk/wsdls/net/restfulwebservices/www/servicecontracts/_2008/_01/WeatherForecastService.wsdl", sessionCookie);
        ratingBean = infoAdminServiceClient.getRatings("/_system/governance/trunk/wsdls/net/restfulwebservices/www/servicecontracts/_2008/_01/WeatherForecastService.wsdl", sessionCookie);
        assertEquals(ratingBean.getUserRating(), 2);
    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"rateTest"})
    public void lifeCycleTest() throws Exception {
        String[] lifeCycleItem = {"Requirements Gathered", "Architecture Finalized", "High Level Design Completed"};
        lifeCycleAdminServiceClient.addAspect("/_system/governance/trunk/wsdls/net/restfulwebservices/www/servicecontracts/_2008/_01/WeatherForecastService.wsdl", "ServiceLifeCycle");
        lifeCycleAdminServiceClient.invokeAspect("/_system/governance/trunk/wsdls/net/restfulwebservices/www/servicecontracts/_2008/_01/WeatherForecastService.wsdl", "ServiceLifeCycle", "Promote", lifeCycleItem);
        LifecycleBean lifecycleBean = lifeCycleAdminServiceClient.getLifecycleBean("/_system/governance/trunk/wsdls/net/restfulwebservices/www/servicecontracts/_2008/_01/WeatherForecastService.wsdl");
        Property[] lifecycleProperties = lifecycleBean.getLifecycleProperties();
        for (Property property : lifecycleProperties) {
            if (property.getKey().equals("registry.lifecycle.ServiceLifeCycle.state")) {
                assertTrue("Testing".equalsIgnoreCase(property.getValues()[0]));
            }
        }
        lifeCycleAdminServiceClient.removeAspect("/_system/governance/trunk/wsdls/net/restfulwebservices/www/servicecontracts/_2008/_01/WeatherForecastService.wsdl", "ServiceLifeCycle");
    }


    @AfterClass(groups = {"wso2.greg"})
    public void deleteResources()
            throws ResourceAdminServiceExceptionException, RemoteException, GovernanceException {

        Endpoint[] endpoints = null;
        Endpoint[] endPointsOther = null;
        Wsdl[] wsdls = wsdlManager.getAllWsdls();
        for (Wsdl wsdl : wsdls) {
            if (wsdl.getQName().getLocalPart().equals("sample.wsdl")) {
                endpoints = wsdlManager.getWsdl(wsdl.getId()).getAttachedEndpoints();
            } else if (wsdl.getQName().getLocalPart().equals("WeatherForecastService.wsdl")) {
                endPointsOther = wsdlManager.getWsdl(wsdl.getId()).getAttachedEndpoints();
            }
        }
        resourceAdminServiceClient.deleteResource("/_system/governance/trunk/wsdls/eu/dataaccess/footballpool/sample.wsdl");
        resourceAdminServiceClient.deleteResource("/_system/governance/trunk/wsdls/net/restfulwebservices/www/servicecontracts/_2008/_01/WeatherForecastService.wsdl");
        resourceAdminServiceClient.deleteResource("/_system/governance/trunk/schemas/com/microsoft/schemas/_2003/_10/serialization/arrays/WeatherForecastService.svc.xsd");
        resourceAdminServiceClient.deleteResource("/_system/governance/trunk/schemas/net/restfulwebservices/www/datacontracts/_2008/_01/WeatherForecastService1.xsd");
        resourceAdminServiceClient.deleteResource("/_system/governance/trunk/schemas/net/restfulwebservices/www/servicecontracts/_2008/_01/WeatherForecastService2.xsd");
        resourceAdminServiceClient.deleteResource("/_system/governance/trunk/schemas/com/microsoft/schemas/_2003/_10/serialization/WeatherForecastService3.xsd");
        resourceAdminServiceClient.deleteResource("/_system/governance/trunk/schemas/faultcontracts/gotlservices/_2008/_01/WeatherForecastService4.xsd");
        for (Endpoint path : endpoints) {
            resourceAdminServiceClient.deleteResource("_system/governance/" + path.getPath());
        }
        for (Endpoint path : endPointsOther) {
            resourceAdminServiceClient.deleteResource("_system/governance/" + path.getPath());
        }
        for (Service service : serviceManager.getAllServices()) {
            if (service.getQName().getLocalPart().equals("Info")) {
                serviceManager.removeService(service.getId());
            } else if (service.getQName().getLocalPart().equals("WeatherForecastService")) {
                serviceManager.removeService(service.getId());
            }
        }

        relationAdminServiceClient = null;
        resourceAdminServiceClient = null;
        infoAdminServiceClient = null;
        lifeCycleAdminServiceClient = null;
        wsdlManager = null;
        serviceManager = null;
    }
}
