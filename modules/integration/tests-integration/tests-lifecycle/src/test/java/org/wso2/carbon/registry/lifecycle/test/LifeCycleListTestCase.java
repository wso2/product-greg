/*
* Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.registry.lifecycle.test;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.test.utils.common.FileManager;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.beans.xsd.LifecycleBean;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.governance.list.stub.ListMetadataServiceRegistryExceptionException;
import org.wso2.carbon.governance.list.stub.beans.xsd.ServiceBean;
import org.wso2.carbon.governance.services.stub.AddServicesServiceRegistryExceptionException;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.lifecycle.test.bean.SearchParameterBean;
import org.wso2.carbon.registry.search.stub.SearchAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.search.stub.beans.xsd.AdvancedSearchResultsBean;
import org.wso2.carbon.registry.search.stub.beans.xsd.ArrayOfString;
import org.wso2.carbon.registry.search.stub.beans.xsd.CustomSearchParameterBean;
import org.wso2.carbon.registry.search.stub.common.xsd.ResourceData;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.governance.api.exception.GovernanceException;

import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.greg.integration.common.clients.*;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import javax.activation.DataHandler;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * List all the added LCs
 * Tried editing the LC when it is used by a service
 * Tried editing the LC when it is not used by a service
 * Verified the functionality of 'Find Usage' feature (Verified whether services
 * without LCs are also included in the result)
 */
public class LifeCycleListTestCase extends GREGIntegrationBaseTest{

    private WSRegistryServiceClient wsRegistryServiceClient;

    private LifeCycleAdminServiceClient lifeCycleAdminServiceClient;
    private LifeCycleManagementClient lifeCycleManagementClient;
    private GovernanceServiceClient governanceServiceClient;
    private ListMetaDataServiceClient listMetadataServiceClient;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private SearchAdminServiceClient searchAdminServiceClient;
    private ServiceManager serviceManager;

    private static final String[] SERVICE_NAMES = {"IntergalacticService10", "abc", "def"};
    private static final String[] LC_NAMES = {"StateDemoteLC", "MultiplePromoteDemoteLC",
                                              "CheckListPermissionLC"};

    private static final String GOV_PATH = "/_system/governance";
    private String serviceString = "/trunk/services/com/abb/IntergalacticService10";
    private final String absPath = GOV_PATH + serviceString;

    private ServiceBean service;
    private RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
    private String servicePath1;
    private String servicePath2;

    /**
     * @throws RemoteException
     * @throws LoginAuthenticationExceptionException
     *
     * @throws RegistryException
     */
    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_USER);

        String sessionCookie = getSessionCookie();

        lifeCycleAdminServiceClient =
                new LifeCycleAdminServiceClient(backendURL,
                                                sessionCookie);
        governanceServiceClient =
                new GovernanceServiceClient(backendURL,
                                            sessionCookie);
        listMetadataServiceClient =
                new ListMetaDataServiceClient(backendURL,
                                              sessionCookie);
        lifeCycleManagementClient =
                new LifeCycleManagementClient(backendURL,
                                              sessionCookie);

        resourceAdminServiceClient =
                new ResourceAdminServiceClient(backendURL,
                                               sessionCookie);
        wsRegistryServiceClient =
                registryProviderUtil.getWSRegistry(automationContext);

        searchAdminServiceClient =
                new SearchAdminServiceClient(backendURL,
                                             sessionCookie);
	    Registry reg = registryProviderUtil.getGovernanceRegistry(wsRegistryServiceClient,
            automationContext);

        GovernanceUtils.loadGovernanceArtifacts((UserRegistry)reg);
        serviceManager = new ServiceManager(reg);

    }

    /**
     * @throws XMLStreamException
     * @throws IOException
     * @throws AddServicesServiceRegistryExceptionException
     *
     * @throws ListMetadataServiceRegistryExceptionException
     *
     * @throws ResourceAdminServiceExceptionException
     *
     */
    @Test(groups = "wso2.greg", description = "Create 3 services")
    public void testCreateServices() throws XMLStreamException, IOException,
            AddServicesServiceRegistryExceptionException,
            ListMetadataServiceRegistryExceptionException,
            ResourceAdminServiceExceptionException,
            GovernanceException, InterruptedException {

        String servicePath =
                getTestArtifactLocation() + "artifacts" +
                File.separator + "GREG" + File.separator + "services" +
                File.separator + "intergalacticService10.metadata.xml";
        DataHandler dataHandler = new DataHandler(new URL("file:///" + servicePath));
        String mediaType = "application/vnd.wso2-service+xml";
        String description = "This is a test service 1";
        resourceAdminServiceClient.addResource(
                "/_system/governance/service2", mediaType, description, dataHandler);
        

        servicePath =
                getTestArtifactLocation() + "artifacts" + File.separator +
                "GREG" + File.separator + "services" + File.separator +
                "service.metadata.xml";
        dataHandler = new DataHandler(new URL("file:///" + servicePath));
        mediaType = "application/vnd.wso2-service+xml";
        description = "This is a test service 2";
        resourceAdminServiceClient.addResource(
                "/_system/governance/service2", mediaType, description, dataHandler);

        servicePath =
                getTestArtifactLocation() + "artifacts" + File.separator +
                "GREG" + File.separator + "services" + File.separator +
                "xservice.xml";
        dataHandler = new DataHandler(new URL("file:///" + servicePath));
        mediaType = "application/vnd.wso2-service+xml";
        description = "This is a test service 3";
        resourceAdminServiceClient.addResource(
                "/_system/governance/service2", mediaType, description, dataHandler);

	Service[] services = serviceManager.getAllServices();
        int serviceCount = 0;

        if (services.length > 0) {
            for (String serviceName : SERVICE_NAMES) {
                for (Service service : services) {
                    if (service.getQName().getLocalPart().equals(serviceName)) {
                        serviceCount++;
                    }
                }
            }
        }
        Thread.sleep(30000);
        assertEquals(serviceCount, SERVICE_NAMES.length, "Mismatching number of services");
    }

    /**
     * @throws LifeCycleManagementServiceExceptionException
     *
     * @throws IOException
     * @throws InterruptedException
     */
    @Test(groups = "wso2.greg", description = "Create 3 lifecycles", dependsOnMethods = "testCreateServices")
    public void testCreateLifeCycles() throws LifeCycleManagementServiceExceptionException,
                                              IOException, InterruptedException {
        int lcCount = 0;

        String[] lcList = lifeCycleManagementClient.getLifecycleList();
        for (String lcName : lcList) {
            for (String lcNameExisting : LC_NAMES) {
                if (lcName.equals(lcNameExisting)) {
                    lcCount++;
                }
            }
        }

        int realLcCount = lcList.length - lcCount;

        String resourcePath =
                getTestArtifactLocation() + "artifacts" +
                File.separator + "GREG" + File.separator + "lifecycle" +
                File.separator + "StateDemoteLifeCycle.xml";
        String lifeCycleContent = FileManager.readFile(resourcePath);
        lifeCycleManagementClient.addLifeCycle(lifeCycleContent);

        resourcePath =
                getTestArtifactLocation() + "artifacts" +
                File.separator + "GREG" + File.separator + "lifecycle" +
                File.separator + "MultiplePromoteDemoteLC.xml";
        lifeCycleContent = FileManager.readFile(resourcePath);
        lifeCycleManagementClient.addLifeCycle(lifeCycleContent);

        resourcePath =
                getTestArtifactLocation() + "artifacts" +
                File.separator + "GREG" + File.separator + "lifecycle" +
                File.separator + "CheckListPermissionLC.xml";
        lifeCycleContent = FileManager.readFile(resourcePath);
        lifeCycleManagementClient.addLifeCycle(lifeCycleContent);
        Thread.sleep(30000);
        assertEquals(lifeCycleManagementClient.getLifecycleList().length, realLcCount + LC_NAMES.length,
                     "LifeCycle number mismatched");

    }

    /**
     * @throws RegistryException
     * @throws RemoteException
     * @throws CustomLifecyclesChecklistAdminServiceExceptionException
     *
     * @throws ListMetadataServiceRegistryExceptionException
     *
     * @throws ResourceAdminServiceExceptionException
     *
     */
    @Test(groups = "wso2.greg", description = "Add lifecycle to a service", dependsOnMethods = "testCreateLifeCycles")
    public void testAddLcToService() throws RegistryException, RemoteException,
            CustomLifecyclesChecklistAdminServiceExceptionException,
            ListMetadataServiceRegistryExceptionException,
            ResourceAdminServiceExceptionException, InterruptedException {

	Service[] services = serviceManager.getAllServices();
        String serviceString = "";
        for (Service service : services) {
	    String path = service.getPath();
            if (path.contains(SERVICE_NAMES[0])) {
                serviceString = path;
                servicePath1 = path;
            }
        }
        wsRegistryServiceClient.associateAspect(GOV_PATH + serviceString, LC_NAMES[0]);
        LifecycleBean lifeCycle =
                lifeCycleAdminServiceClient.getLifecycleBean(GOV_PATH +
                                                             serviceString);

        Property[] properties = lifeCycle.getLifecycleProperties();

        boolean lcStatus = false;
        for (Property prop : properties) {
            if (prop.getKey().contains(LC_NAMES[0])) {
                lcStatus = true;
            }
        }
        assertTrue(lcStatus, "LifeCycle not added to service");
        for (Service service2 : services) {
	    String path = service2.getPath();
            if (path.contains(SERVICE_NAMES[1])) {
                serviceString = path;
                servicePath2 = path;
            }
        }
        wsRegistryServiceClient.associateAspect(GOV_PATH + serviceString, LC_NAMES[0]);
        lifeCycle =
                lifeCycleAdminServiceClient.getLifecycleBean(GOV_PATH +
                                                             serviceString);
        properties = lifeCycle.getLifecycleProperties();

        lcStatus = false;
        for (Property prop : properties) {
            if (prop.getKey().contains(LC_NAMES[0])) {
                lcStatus = true;
            }
        }
        Thread.sleep(30000);
        assertTrue(lcStatus, "LifeCycle not added to service");
    }

    /**
     * @throws RegistryException
     * @throws RemoteException
     * @throws SearchAdminServiceRegistryExceptionException
     *
     */
    @Test(groups = "wso2.greg", description = "Find usage of StateDemoteLC lifecycle",
          dependsOnMethods = "testAddLcToService")
    public void testFindUsage() throws RegistryException, RemoteException,
                                       SearchAdminServiceRegistryExceptionException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setPropertyName("registry.LC.name");
        paramBean.setRightPropertyValue("StateDemoteLC");
        paramBean.setRightOperator("eq");
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        paramList = null;
        AdvancedSearchResultsBean result =
                searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
        assertNotNull(result.getResourceDataList(), "No Record Found");
        assertTrue((result.getResourceDataList().length > 0),
                   "No Record Found. set valid property name");

        assertEquals(result.getResourceDataList().length, 2, "Service count mismatched");
        boolean service1Found = false;
        boolean service2Found = false;

        for (ResourceData resource : result.getResourceDataList()) {
            if (resource.getResourcePath()
                    .contains(servicePath1)) {
                service1Found = true;
            }
            if (resource.getResourcePath()
                    .contains(servicePath2)) {
                service2Found = true;
            }
        }
        assertTrue((service1Found && service2Found), "Relevant services not found");
    }

    /**
     * @throws IOException
     * @throws LifeCycleManagementServiceExceptionException
     *
     */
    @Test(groups = "wso2.greg", description = "Edit lifecycle when it is not in usage",
          dependsOnMethods = "testFindUsage")
    public void testEditLCs() throws IOException, LifeCycleManagementServiceExceptionException {

        String resourcePath =
                getTestArtifactLocation() + "artifacts" +
                File.separator + "GREG" + File.separator + "lifecycle" +
                File.separator + "LongCheckListLC.xml";
        String lifeCycleConfiguration = FileManager.readFile(resourcePath);
        lifeCycleManagementClient.editLifeCycle("MultiplePromoteDemoteLC", lifeCycleConfiguration);
        String[] lifeCycles = lifeCycleManagementClient.getLifecycleList();

        boolean lcStatus = false;
        for (String lc : lifeCycles) {

            if (lc.equalsIgnoreCase("LongCheckListLC")) {
                lcStatus = true;
            }
        }
        assertTrue(lcStatus, "LifeCycle not edited");
    }

    /**
     * @throws IOException
     * @throws LifeCycleManagementServiceExceptionException
     *
     */
    @Test(groups = "wso2.greg", description = "Edit lifecycle while it is in usage",
          dependsOnMethods = "testEditLCs")
    public void testEditLCsWhileUsed() throws IOException,
                                              LifeCycleManagementServiceExceptionException {

        String resourcePath =
                getTestArtifactLocation() + "artifacts" +
                File.separator + "GREG" + File.separator + "lifecycle" +
                File.separator + "TransitionEventsLC.xml";

        String lifeCycleConfiguration = FileManager.readFile(resourcePath);
        lifeCycleManagementClient.editLifeCycle("StateDemoteLC", lifeCycleConfiguration);
        String[] lifeCycles = lifeCycleManagementClient.getLifecycleList();

        boolean lcStatus = false;
        for (String lc : lifeCycles) {

            if (lc.equalsIgnoreCase("TransitionEventsLC")) {
                lcStatus = true;
            }
        }
        assertTrue(lcStatus, "LifeCycle cannot be edited while in usage");
    }

    /**
     * @throws Exception
     */
    @AfterClass(alwaysRun = true)
    public void clear() throws Exception {
//        service = listMetadataServiceClient.listServices(null);
	Service[] services = serviceManager.getAllServices();
        for (Service service : services) {
	    String path = service.getPath();
            if (path.contains(SERVICE_NAMES[0]) || path.contains(SERVICE_NAMES[1]) || path.contains(SERVICE_NAMES[2])) {
                String servicePathToDelete = GOV_PATH + path;
                if (wsRegistryServiceClient.resourceExists(servicePathToDelete)) {
                    resourceAdminServiceClient.deleteResource(servicePathToDelete);
                }
            }
        }
 /*       SchemaBean schema = listMetadataServiceClient.listSchemas();
            String[] schemaPath = schema.getPath();
        for (String schemaDelete : schemaPath) {
            if (schemaDelete.contains("purchasing")) {
                String schemaPathToDelete = "/_system/governance/" + schemaDelete;
                if (wsRegistryServiceClient.resourceExists(schemaPathToDelete)) {
                    resourceAdminServiceClient.deleteResource(schemaPathToDelete);
                }
            }

        }*/
/*        WSDLBean wsdl = listMetadataServiceClient.listWSDLs();
        String[] wsdlPath = wsdl.getPath();
        if (wsdlPath != null) {
            for (String wsdlDelete : wsdlPath) {
                if (wsdlDelete.contains("abc") || wsdlDelete.contains("def") || wsdlDelete.contains("IntergalacticService")) {
                    String wsdlPathToDelete = "/_system/governance/" + wsdlDelete;
                    if (wsRegistryServiceClient.resourceExists(wsdlPathToDelete)) {
                        resourceAdminServiceClient.deleteResource(wsdlPathToDelete);
                    }
                }
            }
        }*/

        lifeCycleManagementClient.deleteLifeCycle(LC_NAMES[0]);
        lifeCycleManagementClient.deleteLifeCycle("LongCheckListLC");
        lifeCycleManagementClient.deleteLifeCycle(LC_NAMES[2]);

        governanceServiceClient = null;
        wsRegistryServiceClient = null;
        lifeCycleAdminServiceClient = null;
        lifeCycleManagementClient = null;
        listMetadataServiceClient = null;
        resourceAdminServiceClient = null;
    }
}
