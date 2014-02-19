/*
* Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.governance.api.test.old;


import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.endpoints.EndpointManager;
import org.wso2.carbon.governance.api.endpoints.dataobjects.Endpoint;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import javax.xml.namespace.QName;

import static org.testng.Assert.*;


public class EndpointServiceTestCase {
    private static final Log log = LogFactory.getLog(EndpointServiceTestCase.class);
    private static WSRegistryServiceClient registry = null;
    private static Registry governance = null;
    private static WsdlManager wsdlManager;
    private static EndpointManager endpointManager;
    int userId = 1;

    @BeforeClass(alwaysRun = true)
    public void init() throws RegistryException, AxisFault {
        UserInfo userInfo = UserListCsvReader.getUserInfo(userId);
        registry =
                new RegistryProviderUtil().getWSRegistry(userId,
                        ProductConstant.GREG_SERVER_NAME);
        governance = new RegistryProviderUtil().getGovernanceRegistry(registry, userId);
        wsdlManager = new WsdlManager(governance);
        endpointManager = new EndpointManager(governance);

    }

    @Test(groups = {"wso2.greg"}, description = "test adding an Endpoint to G-Reg")
    public void testAddEndpoint() throws RegistryException {
        String endpoint_url = "http://ws.strikeiron.com/StrikeIron/donotcall2_5/DoNotCallRegistryUnique";
        String endpoint_path = "/_system/governance/trunk/endpoints/com/strikeiron/ws/strikeiron/donotcall2_5/ep-DoNotCallRegistryUnique";
        String property1 = "QA";
        String property2 = "Dev";

        //Create Endpoint
        createEndpoint(endpoint_url);
        assertTrue(registry.resourceExists(endpoint_path), "Endpoint Resource Does not exists :");

        propertyAssertion(endpoint_path, property1, property2);
        deleteResources(endpoint_path);
        log.info("EndpointServiceTestClient -testAddEndpoint() Passed");

    }

    @Test(groups = {"wso2.greg"}, description = "test adding an WSDL with Endpoints to G-Reg")
    public void testAddWsdlWithEndpoints() throws Exception {
        String wsdl_url = "http://people.wso2.com/~evanthika/wsdls/BizService.wsdl";
        String endpoint_path = "http://people.wso2.com:9763/services/BizService";

        Wsdl wsdl;
        try {
            wsdl = wsdlManager.newWsdl(wsdl_url);
            wsdlManager.addWsdl(wsdl);
            log.info("EndpointServiceTestClient - WSDL was successfully added");
            Endpoint[] endpoints = testVerifyEndpoints(endpoint_path, wsdl);
            endpointManager = removeEndpoint(endpoints[0]);
            GovernanceArtifact[] artifacts = wsdl.getDependents();
            wsdlManager.removeWsdl(wsdl.getId());// delete the WSDL
            removeServices(artifacts);
            endpointManager.removeEndpoint(endpoints[0].getId());// now try to remove the endpoint
            deleteResources(endpoint_path);
            log.info("EndpointServiceTestClient testAddWsdlWithEndpoints()- Passed");
        } catch (GovernanceException e) {
            log.error("Failed to add WSDL:" + e);
            throw new RegistryException("Failed to add WSDL:" + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "adding a service with Endpoints to G-Reg")
    public void testAddServiceWithEndpoints() throws GovernanceException, RegistryException {
        String service_namespace = "http://wso2.com/test/examples";
        String service_name = "myService";
        String service_path = "/_system/governance/trunk/services/com/wso2/test/examples/myService";
        String endpoint_path1 = "/_system/governance/trunk/endpoints/ep-endpoint19";
        String endpoint_path2 = "/_system/governance/trunk/endpoints/ep-endpoint29";

        ServiceManager serviceManager = new ServiceManager(governance);
        try {
            Service service = serviceManager.newService(new QName(service_namespace, service_name));
            service.addAttribute("endpoints_entry", ":http://endpoint19");
            service.addAttribute("endpoints_entry", "QA:http://endpoint29");
            serviceManager.addService(service);

            Endpoint[] endpoints = service.getAttachedEndpoints();
            assertEquals(2, endpoints.length, "Endpoint length does not match:");
            assertEquals(endpoints[0].getUrl(), "http://endpoint19", "Endpoint URL element 0 does not match :");
            assertEquals(endpoints[0].getAttributeKeys().length, 0, "Endpoint element 0 service does not exists:");
            assertEquals(endpoints[1].getUrl(), "http://endpoint29", "Endpoint URL element 1 does not exists:");
            assertEquals(endpoints[1].getAttributeKeys().length, 1, "Endpoint element 1 service does not exists:");

            deleteResources(service_path);
            deleteResources(endpoint_path1);
            deleteResources(endpoint_path2);
            log.info("EndpointServiceTestClient testAddServiceWithEndpoints()- Passed");
        } catch (GovernanceException e) {
            log.error("testAddServiceWithEndpoints GovernanceException Exception thrown:" + e);
            throw new GovernanceException("testAddServiceWithEndpoints-Governance Registry Exception thrown:" + e);
        } catch (RegistryException e) {
            log.error("testAddServiceWithEndpoints RegistryException Exception thrown:" + e);
            throw new RegistryException("testAddServiceWithEndpoints-Registry Exception thrown:" + e);
        }

    }

    @Test(groups = {"wso2.greg"}, description = "Attach an Endpoint to a service ")
    public void testAttachEndpointsToService() throws RegistryException {
        String service_namespace = "http://wso2.com/test234/xxxxx";
        String service_name = "myServicxcde";
        String service_path = "/_system/governance/trunk/services/com/wso2/test234/xxxxx/myServicxcde";
        String endpoint_path1 = "/_system/governance/trunk/endpoints/ep-endpoint4xx";
        String endpoint_path2 = "/_system/governance/trunk/endpoints/ep-endpoint3xx";



        ServiceManager serviceManager = new ServiceManager(governance);
        Service service;
        try {
            service = serviceManager.newService(new QName(service_namespace, service_name));
            serviceManager.addService(service);

            EndpointManager endpointManager = new EndpointManager(governance);

            String url = "http://endpoint4xx";

            String url1 = "http://endpoint3xx";

            Endpoint ep1 = endpointManager.newEndpoint(url);
            endpointManager.addEndpoint(ep1);

            Endpoint ep2 = endpointManager.newEndpoint(url1);
            endpointManager.addEndpoint(ep2);

            service.attachEndpoint(ep1);
            service.attachEndpoint(ep2);

            Endpoint[] endpoints = service.getAttachedEndpoints();
            assertEquals(2, endpoints.length);
            assertEquals(url, endpoints[0].getUrl());
            assertEquals(url1, endpoints[1].getUrl());

            //Detach Endpoint one
            service.detachEndpoint(ep1.getId());
            endpoints = service.getAttachedEndpoints();
            assertEquals(1, endpoints.length);
            deleteResources(service_path);
            deleteResources(endpoint_path1);
            deleteResources(endpoint_path2);

            log.info("EndpointServiceTestClient testAttachEndpointsToService()- Passed");
        } catch (GovernanceException e) {
            log.error("testAttachEndpointsToService GovernanceException Exception thrown:" + e);
            throw new RegistryException("testAttachEndpointsToService-Registry Exception thrown:" + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Add duplicate endpoints")
    public void testAddDuplicateEndpoints() throws GovernanceException {

        EndpointManager endpointManager = new EndpointManager(governance);
        Endpoint ep1 = endpointManager.newEndpoint("http://wso2.automation.endpoint");
        endpointManager.addEndpoint(ep1);
        assertTrue(endpointManager.getEndpoint(ep1.getId()).getQName().toString().
                contains("http://wso2.automation.endpoint"), "Endpoint not found");

        //add same endpoint again
        EndpointManager endpointManagerDuplicate = new EndpointManager(governance);
        Endpoint ep2 = endpointManagerDuplicate.newEndpoint("http://wso2.automation.endpoint");
        endpointManagerDuplicate.addEndpoint(ep2);

        assertTrue(endpointManagerDuplicate.getEndpoint(ep1.getId()).getQName().toString().
                contains("http://wso2.automation.endpoint"), "Endpoint not found");

        //delete endpoint
        endpointManagerDuplicate.removeEndpoint(ep1.getId());
        assertNull(endpointManagerDuplicate.getEndpoint(ep1.getId()), "Endpoint not removed");
    }

    @Test(groups = {"wso2.greg"}, description = "Add jmx endpoints")
    public void testAddJmxEndpoint() throws GovernanceException {

        EndpointManager endpointManager = new EndpointManager(governance);
        Endpoint ep1 = endpointManager.newEndpoint("jms:/myqueue?transport.jms." +
                "ConnectionFactoryJNDIName=QueueConnectionFactory&amp;" +
                "java.naming.factory.initial=" +
                "org.apache.qpid.jndi.PropertiesFileInitialContextFactory&amp;" +
                "java.naming.provider.url=resources/jndi.properties");

        endpointManager.addEndpoint(ep1);
        assertTrue(endpointManager.getEndpoint(ep1.getId()).getQName().toString().contains("jms:/myqueue?transport.jms"), "Endpoint not found");

        //delete endpoint
        endpointManager.removeEndpoint(ep1.getId());
        assertNull(endpointManager.getEndpoint(ep1.getId()), "Endpoint not removed");
    }


    @Test(groups = {"wso2.greg"}, description = "Get endpoint by URL")
    public void testGetEndpointByURL() throws GovernanceException {

        EndpointManager endpointManager = new EndpointManager(governance);
        Endpoint ep1 = endpointManager.newEndpoint("http://wso2.automation.endpoint/urltest");
        endpointManager.addEndpoint(ep1);
        assertTrue(endpointManager.getEndpoint(ep1.getId()).getQName().toString().contains("http://wso2.automation.endpoint/urltest"), "Endpoint not found");
        assertTrue(endpointManager.getEndpointByUrl(ep1.getUrl()).getQName().toString().contains("http://wso2.automation.endpoint/urltest"), "Endpoint not found");
        endpointManager.removeEndpoint(ep1.getId());
        assertNull(endpointManager.getEndpoint(ep1.getId()), "Endpoint not removed");
    }

    private void removeServices(GovernanceArtifact[] artifacts) throws GovernanceException {
        ServiceManager serviceManager = new ServiceManager(governance);

        for (GovernanceArtifact artifact : artifacts) {
            if (artifact instanceof Service) {
                // getting the service.
                Service service2 = (Service) artifact;
                serviceManager.removeService(service2.getId());
            }
        }
    }

    private EndpointManager removeEndpoint(Endpoint endpoint) throws Exception {
        try {
            endpointManager.removeEndpoint(endpoint.getId());
            assertTrue(registry.resourceExists(endpoint.getPath()), "EPR exists in the registry");
        } catch (Exception ignored) {
            log.info("Can't remove Endpoint yet because of service & wsdl Exists");
        }
        return endpointManager;
    }

    private Endpoint[] testVerifyEndpoints(String endpoint_path, Wsdl wsdl)
            throws GovernanceException {
        Endpoint[] endpoints = wsdl.getAttachedEndpoints();
        assertEquals(1, endpoints.length, "Endpoint length does not match :");
        assertEquals(endpoints[0].getUrl(), endpoint_path, "Endpoint Path does not exsits");
        assertEquals(endpoints[0].getAttributeKeys().length, 1, "Endpoint Element 0 does not exists:");
        return endpoints;
    }


    private void deleteResources(String resourceName) throws RegistryException {
        if (registry.resourceExists(resourceName)) {
            registry.delete(resourceName);
        }
    }

    private void createEndpoint(String endpoint_url) throws GovernanceException {
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
    }

    private void propertyAssertion(String endpoint_path, String property1, String property2)
            throws RegistryException {
        Resource resource;
        try {
            resource = registry.get(endpoint_path);
            assertEquals(resource.getProperty("status1"), property1, "Endpoint Property - Status1 does not Exists :");
            assertEquals(resource.getProperty("status2"), property2, "Endpoint Property - Status2 does not Exists :");
        } catch (RegistryException e) {
            log.error("propertyAssertion Exception thrown:" + e);
            throw new RegistryException("propertyAssertion-Registry Exception thrown:" + e);
        }
    }

    @AfterClass(alwaysRun = true, groups = {"wso2.greg"})
    public void removeArtifacts() throws RegistryException, AxisFault {
        deleteResources("/_system/governance/trunk/schemas/org/bar/purchasing/purchasing.xsd");
        deleteResources("/_system/governance/trunk/endpoints/ep-wso2-automation-endpoint");

        registry = null;
        governance = null;
        wsdlManager = null;
        endpointManager = null;
    }

}
