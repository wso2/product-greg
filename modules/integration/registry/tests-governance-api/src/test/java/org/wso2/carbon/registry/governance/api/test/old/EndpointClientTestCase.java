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

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.endpoints.EndpointManager;
import org.wso2.carbon.governance.api.endpoints.dataobjects.Endpoint;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.services.ServiceFilter;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.extensions.utils.CommonConstants;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.*;

/**
 * contain test cases for governance API Endpoints
 */
public class EndpointClientTestCase {
    private Registry governance;
    int userId = 1;
    private WSRegistryServiceClient registry;
    @BeforeClass(groups = {"wso2.greg"})
    public void initTest() throws RegistryException, AxisFault {
        registry =
                new RegistryProviderUtil().getWSRegistry(userId,
                        ProductConstant.GREG_SERVER_NAME);
        governance = new RegistryProviderUtil().getGovernanceRegistry(registry, userId);
    }

    @Test(groups = {"wso2.greg"})
    public void testAddEndpoint() throws Exception {
        // first add an endpoint, get it delete it, simply stuff like that.
        EndpointManager endpointManager = new EndpointManager(governance);

        Endpoint endpoint1 = endpointManager.newEndpoint("http://localhost/papapa/booom");
        endpoint1.addAttribute("status", "QA");

        endpointManager.addEndpoint(endpoint1);
        Assert.assertEquals(endpoint1.getPath(), "/trunk/endpoints/localhost/papapa/ep-booom");

        // now get the endpoint back.
        Endpoint endpoint2 = endpointManager.getEndpoint(endpoint1.getId());
        Assert.assertEquals(endpoint2.getUrl(), "http://localhost/papapa/booom");
        Assert.assertEquals(endpoint1.getAttribute("status"), "QA");

        // so we will be deleting the endpoint
        endpointManager.removeEndpoint(endpoint2.getId());
        Assert.assertTrue(true);

        endpoint2 = endpointManager.getEndpoint(endpoint2.getId());

        Assert.assertNull(endpoint2);
    }

    @Test(groups = {"wso2.greg"})
    public void testAddWsdlWithEndpoints() throws Exception {
        WsdlManager wsdlManager = new WsdlManager(governance);

        Wsdl wsdl = wsdlManager.newWsdl("http://svn.wso2.org/repos/wso2/trunk/graphite/components/" +
                "governance/org.wso2.carbon.governance.api/src/test/resources" +
                "/test-resources/wsdl/BizService.wsdl");
        wsdlManager.addWsdl(wsdl);

        Endpoint[] endpoints = wsdl.getAttachedEndpoints();
        Assert.assertEquals(endpoints.length, 1);

        Assert.assertEquals(endpoints[0].getUrl(), "http://localhost:8080/axis2/services/BizService");
        Assert.assertEquals(endpoints[0].getAttributeKeys().length, 1);
        Assert.assertEquals(endpoints[0].getAttribute(CommonConstants.SOAP11_ENDPOINT_ATTRIBUTE), "true");

        // now we are trying to remove the endpoint
        EndpointManager endpointManager = new EndpointManager(governance);

        try {
            endpointManager.removeEndpoint(endpoints[0].getId());
            Assert.assertTrue(false);
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
        GovernanceArtifact[] artifacts = wsdl.getDependents();
        // delete the wsdl
        wsdlManager.removeWsdl(wsdl.getId());

        Wsdl[] wsdls = wsdlManager.getAllWsdls();
        for (Wsdl wsdl1 : wsdls) {
            if (wsdl1.getQName().toString().contains("abc")) {
                wsdlManager.removeWsdl(wsdl1.getId());
            }
        }

        ServiceManager serviceManager = new ServiceManager(governance);

        for (GovernanceArtifact artifact : artifacts) {
            if (artifact instanceof Service) {
                // getting the service.
                Service service2 = (Service) artifact;
                serviceManager.removeService(service2.getId());
            }
        }

        // now try to remove the endpoint
        ServiceManager service = new ServiceManager(governance);
        Service[] services = service.getAllServices();
        for (Service service1 : services) {
            if (service1.getQName().toString().contains("BizService") ||
                    service1.getQName().toString().contains("abc")) {
                service.removeService(service1.getId());
            }
        }

        endpointManager.removeEndpoint(endpoints[0].getId());
        Assert.assertTrue(true);
    }

    @Test(groups = {"wso2.greg"})
    public void testAddServiceWithEndpoints() throws Exception {
        ServiceManager serviceManager = new ServiceManager(governance);
        Service service = serviceManager.newService(new QName("http://wso2.com/test/xxx", "myService"));

        service.addAttribute("endpoints_entry", ":http://endpoint1");
        service.addAttribute("endpoints_entry", "QA:http://endpoint2");

        serviceManager.addService(service);

        Endpoint[] endpoints = service.getAttachedEndpoints();
        Assert.assertEquals(endpoints.length, 2);

        Assert.assertEquals(endpoints[0].getUrl(), "http://endpoint1");
        Assert.assertEquals(endpoints[0].getAttributeKeys().length, 0);

        Assert.assertEquals(endpoints[1].getUrl(), "http://endpoint2");
        Assert.assertEquals(endpoints[1].getAttributeKeys().length, 1);
        Assert.assertEquals(endpoints[1].getAttribute(CommonConstants.ENDPOINT_ENVIRONMENT_ATTR), "QA");

        // now update the endpoints in the service
        service.setAttributes("endpoints_entry", new String[]{
                "Dev:http://endpoint3",
                "Production:http://endpoint4",
                "QA:http://endpoint2",
        });
        serviceManager.updateService(service);

        endpoints = getAttachedEndpointsFromService(service);
//        endpoints = service.getAttachedEndpoints();
        Assert.assertEquals(endpoints.length, 3);


        Assert.assertEquals(endpoints[0].getUrl(), "http://endpoint3");
        Assert.assertEquals(endpoints[0].getAttributeKeys().length, 1);
        Assert.assertEquals(endpoints[0].getAttribute(CommonConstants.ENDPOINT_ENVIRONMENT_ATTR), "Dev");

        Assert.assertEquals(endpoints[1].getUrl(), "http://endpoint4");
        Assert.assertEquals(endpoints[1].getAttributeKeys().length, 1);
        Assert.assertEquals(endpoints[1].getAttribute(CommonConstants.ENDPOINT_ENVIRONMENT_ATTR), "Production");

        Assert.assertEquals(endpoints[2].getUrl(), "http://endpoint2");
        Assert.assertEquals(1, endpoints[2].getAttributeKeys().length, 1);
        Assert.assertEquals(endpoints[2].getAttribute(CommonConstants.ENDPOINT_ENVIRONMENT_ATTR), "QA");
    }

    // add endpoints as attachments
    @Test(groups = {"wso2.greg"})
    public void testAttachEndpointsToService() throws Exception {
        ServiceManager serviceManager = new ServiceManager(governance);
        Service service = serviceManager.newService(new QName("http://wso2.com/test234/xxxxx", "myServicxcde"));
        serviceManager.addService(service);

        EndpointManager endpointManager = new EndpointManager(governance);
        Endpoint ep1 = endpointManager.newEndpoint("http://endpoint1xx");
        endpointManager.addEndpoint(ep1);

        Endpoint ep2 = endpointManager.newEndpoint("http://endpoint2xx");
        ep2.setAttribute(CommonConstants.ENDPOINT_ENVIRONMENT_ATTR, "QA");
        endpointManager.addEndpoint(ep2);

        service.attachEndpoint(ep1);
        service.attachEndpoint(ep2);

        Endpoint[] endpoints = service.getAttachedEndpoints();
        Assert.assertEquals(endpoints.length, 2);

        Assert.assertEquals(endpoints[0].getUrl(), "http://endpoint1xx");
        Assert.assertEquals(endpoints[0].getAttributeKeys().length, 0);

        Assert.assertEquals(endpoints[1].getUrl(), "http://endpoint2xx");
        Assert.assertEquals(endpoints[1].getAttributeKeys().length, 1);
        Assert.assertEquals(endpoints[1].getAttribute(CommonConstants.ENDPOINT_ENVIRONMENT_ATTR), "QA");

        service.detachEndpoint(ep1.getId());
        endpoints = service.getAttachedEndpoints();
        Assert.assertEquals(endpoints.length, 1);

        Assert.assertEquals(endpoints[0].getUrl(), "http://endpoint2xx");
        Assert.assertEquals(endpoints[0].getAttributeKeys().length, 1);
        Assert.assertEquals(endpoints[0].getAttribute(CommonConstants.ENDPOINT_ENVIRONMENT_ATTR), "QA");


        // now update the endpoints in the service
        service.setAttributes("endpoints_entry", new String[]{
                "Dev:http://endpoint3",
                "Production:http://endpoint4",
                "QA:http://endpoint2xx",
        });
        serviceManager.updateService(service);

        endpoints = getAttachedEndpointsFromService(service);
//        endpoints = service.getAttachedEndpoints();
        Assert.assertEquals(endpoints.length, 3);


        Assert.assertEquals(endpoints[0].getUrl(), "http://endpoint3");
        Assert.assertEquals(endpoints[0].getAttributeKeys().length, 1);
        Assert.assertEquals(endpoints[0].getAttribute(CommonConstants.ENDPOINT_ENVIRONMENT_ATTR), "Dev");

        Assert.assertEquals(endpoints[1].getUrl(), "http://endpoint4");
        Assert.assertEquals(endpoints[1].getAttributeKeys().length, 1);
        Assert.assertEquals(endpoints[1].getAttribute(CommonConstants.ENDPOINT_ENVIRONMENT_ATTR), "Production");

        Assert.assertEquals(endpoints[2].getUrl(), "http://endpoint2xx");
        Assert.assertEquals(endpoints[2].getAttributeKeys().length, 1);
        Assert.assertEquals(endpoints[2].getAttribute(CommonConstants.ENDPOINT_ENVIRONMENT_ATTR), "QA");

        Endpoint ep5 = endpointManager.getEndpointByUrl("http://endpoint2");
        Assert.assertEquals(ep5.getAttribute(CommonConstants.ENDPOINT_ENVIRONMENT_ATTR), "QA");
    }

    @Test(groups = {"wso2.greg"})
    public void testAssociatingEndpoints() throws Exception {
        ServiceManager serviceManager = new ServiceManager(governance);
        Service service = serviceManager.newService(new QName("http://done.ding/dong/doodo", "bangService343"));

        serviceManager.addService(service);

        EndpointManager endpointManager = new EndpointManager(governance);
        Endpoint endpoint = endpointManager.newEndpoint("http://dos.dis/doos/safdsf/ppeekk");
        endpointManager.addEndpoint(endpoint);
        service.attachEndpoint(endpoint);

        // retrieve the service
        Service service2 = serviceManager.getService(service.getId());
        Endpoint[] endpoints = service2.getAttachedEndpoints();
        Assert.assertEquals(endpoints.length, 1);

        Assert.assertEquals(service2.getAttribute("endpoints_entry"), ":" + endpoints[0].getUrl());
    }

    @Test(groups = {"wso2.greg"})
    public void testServiceAddingEndpointsWithWsdl() throws Exception {
        String path = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
                File.separator + "GREG" + File.separator + "xml";

        String filePath = path + File.separator + "service.metadata.xml";
        File file = new File(filePath);
        FileInputStream fileInputStream = new FileInputStream(file);
        byte[] fileContents = new byte[(int) file.length()];
        fileInputStream.read(fileContents);

        OMElement contentElement = GovernanceUtils.buildOMElement(fileContents);

        ServiceManager serviceManager = new ServiceManager(governance);
        Service service = serviceManager.newService(contentElement);

        service.addAttribute("custom-attribute", "custom-value");
        serviceManager.addService(service);


        // so retrieve it back
        String serviceId = service.getId();
        Service newService = serviceManager.getService(serviceId);
        Assert.assertEquals(newService.getAttribute("custom-attribute"), "custom-value");
        Assert.assertEquals(newService.getAttribute("endpoints_entry"),
                ":http://localhost:8080/axis2/services/BizService");

        // now we just add an endpoints
        WsdlManager wsdlManager = new WsdlManager(governance);
        Wsdl wsdl = wsdlManager.newWsdl("http://svn.wso2.org/repos/wso2/trunk/graphite/components/" +
                "governance/org.wso2.carbon.governance.api/src/test/resources/" +
                "test-resources/wsdl/MyChangedBizService.wsdl");
        wsdl.addAttribute("boom", "hahahaha");

        wsdlManager.addWsdl(wsdl);

        GovernanceArtifact[] artifacts = wsdl.getDependents();

        for (GovernanceArtifact artifact : artifacts) {
            if (artifact instanceof Service) {
                // getting the service.
                Service service2 = (Service) artifact;
                Endpoint[] endpoints = service2.getAttachedEndpoints();
                Assert.assertEquals(endpoints.length, 1);
                Assert.assertEquals(endpoints[0].getUrl(), "http://localhost:8080/axis2/services/BizService-my-changes");
            }
        }
    }

    // detach endpoints
    @Test(groups = {"wso2.greg"})
    public void testDetachEndpointsToService() throws Exception {

        ServiceManager serviceManager = new ServiceManager(governance);

        Service[] serviceGet = serviceManager.getAllServices();
        for (Service service : serviceGet) {
            if (service.getQName().getLocalPart().contains("_myServicxcde")) {
                serviceManager.removeService(service.getId());
            }
        }

        Service service = serviceManager.newService(new QName("http://wso2.com/test234/xxxxxx", "_myServicxcde"));
        serviceManager.addService(service);

        EndpointManager endpointManager = new EndpointManager(governance);

        Endpoint endpoint1 = endpointManager.getEndpointByUrl("http://endpoint11");
        if (endpoint1 != null) {
            endpointManager.removeEndpoint(endpoint1.getId());
        }


        Endpoint endpoint2 = endpointManager.getEndpointByUrl("http://endpoint22");
        if (endpoint2 != null) {
            endpointManager.removeEndpoint(endpoint2.getId());
        }

        Endpoint ep1 = endpointManager.newEndpoint("http://endpoint11");
        endpointManager.addEndpoint(ep1);

        Endpoint ep2 = endpointManager.newEndpoint("http://endpoint22");
        ep2.setAttribute(CommonConstants.ENDPOINT_ENVIRONMENT_ATTR, "QA");
        endpointManager.addEndpoint(ep2);

        service.attachEndpoint(ep1);
        service.attachEndpoint(ep2);

        Endpoint[] endpoints = service.getAttachedEndpoints();
        Assert.assertEquals(endpoints.length, 2);

        // get the updated service endpoints
        service = serviceManager.getService(service.getId());

        String[] endpointValues = service.getAttributes("endpoints_entry");

        Assert.assertEquals(endpointValues.length, 2);

        serviceManager.removeService(service.getId());
    }

    // add non http endpoints as attachments
    @Test(groups = {"wso2.greg"})
    public void testNonHttpEndpointsToService() throws Exception {
        ServiceManager serviceManager = new ServiceManager(governance);


        Service service = serviceManager.newService(new QName("http://wso2.com/doadf/spidf", "myServisdfcxcde"));
        serviceManager.addService(service);

        EndpointManager endpointManager = new EndpointManager(governance);
        Endpoint ep1 = endpointManager.newEndpoint("jms:/Service1");
        endpointManager.addEndpoint(ep1);

        Endpoint ep2 = endpointManager.newEndpoint("jms:/Service2");
        ep2.setAttribute(CommonConstants.ENDPOINT_ENVIRONMENT_ATTR, "QA");
        endpointManager.addEndpoint(ep2);

        service.attachEndpoint(ep1);
        service.attachEndpoint(ep2);

        Endpoint[] endpoints = service.getAttachedEndpoints();
        Assert.assertEquals(endpoints.length, 2);

        // get the updated service endpoints
        service = serviceManager.getService(service.getId());

        String[] endpointValues = service.getAttributes("endpoints_entry");

        Assert.assertEquals(endpointValues.length, 2);
    }

    @Test(groups = {"wso2.greg"}, description = "Create a service and set ERP environment")
    public void testSetEndpointEnvironment() throws RegistryException {
        String service_namespace = "http://wso2.org/atomation/test";
        String service_name = "myServiceExample";


        ServiceManager serviceManager = new ServiceManager(governance);

        Service[] serviceGet = serviceManager.getAllServices();
        for (Service service : serviceGet) {
            if (service.getQName().getLocalPart().contains(service_name)) {
                serviceManager.removeService(service.getId());
            }
        }

        Service service;
        service = serviceManager.newService(new QName(service_namespace, service_name));
        serviceManager.addService(service);

        EndpointManager endpointManager = new EndpointManager(governance);

        Endpoint endpoint1 = endpointManager.getEndpointByUrl("http://endpoint11xx");
        if (endpoint1 != null) {
            endpointManager.removeEndpoint(endpoint1.getId());
        }


        Endpoint endpoint2 = endpointManager.getEndpointByUrl("http://endpoint22xx");
        if (endpoint2 != null) {
            endpointManager.removeEndpoint(endpoint2.getId());
        }


        Endpoint ep1 = endpointManager.newEndpoint("http://endpoint11xx");
        ep1.addAttribute("environment", "Dev");
        ep1.addAttribute("URL", "http://endpoint11xx");
        endpointManager.addEndpoint(ep1);

        Endpoint ep2 = endpointManager.newEndpoint("http://endpoint22xx");
        ep2.addAttribute("environment", "QA");
        ep2.addAttribute("URL", "http://endpoint22xx");
        endpointManager.addEndpoint(ep2);

        service.attachEndpoint(ep1);
        service.attachEndpoint(ep2);

        service.getAttributes("environment");
        service.getAttributes("URL");

        Endpoint[] endpoints = service.getAttachedEndpoints();
        assertEquals(2, endpoints.length);
        assertEquals(endpoints[0].getAttribute("URL"), "http://endpoint11xx");
        assertEquals(endpoints[1].getAttribute("URL"), "http://endpoint22xx");
        assertEquals(endpoints[0].getAttribute("environment"), "Dev");
        assertEquals(endpoints[1].getAttribute("environment"), "QA");
        assertEquals("http://endpoint11xx", endpoints[0].getUrl());
        assertEquals("http://endpoint22xx", endpoints[1].getUrl());

        //Detach Endpoint one
        service.detachEndpoint(ep1.getId());
        service.detachEndpoint(ep2.getId());
        endpoints = service.getAttachedEndpoints();
        assertEquals(0, endpoints.length);

        serviceManager.removeService(service.getId());
        assertNull(serviceManager.getService(service.getId()));
    }

    private Endpoint[] getAttachedEndpointsFromService(Service service) throws
            GovernanceException {
        List<Endpoint> endpoints = new ArrayList<Endpoint>();
        try {
            String[] endpointValues = service.getAttributes("endpoints_entry");
            EndpointManager endpointManager = new EndpointManager(governance);
            for (String ep : endpointValues) {
                endpoints.add(endpointManager.getEndpointByUrl(getFilteredEPURL(ep)));
            }
        } catch (GovernanceException e) {
            throw new GovernanceException("Exception occurred while getting endpoints ");
        }
        return endpoints.toArray(new Endpoint[endpoints.size()]);
    }

    private String getFilteredEPURL(String ep) {
//        Dev:http://endpoint3
        if (!ep.startsWith("http")) {
            return ep.substring(ep.indexOf(":") + 1, ep.length());
        } else {
            return ep;
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Validate getEndpointByUrl")
    public void testInvalidEndpointURL() throws GovernanceException {
        EndpointManager endpointManager = new EndpointManager(governance);
        String endpointURL = "http://localhost:8080/axis2/services/BizService";
        Endpoint endpoint = endpointManager.newEndpoint(endpointURL);
        endpointManager.addEndpoint(endpoint);
        try {
            endpoint = endpointManager.getEndpointByUrl(endpointURL);
            assertTrue(endpoint.getUrl().equalsIgnoreCase(endpointURL), "Get Endpoint URL not matched " +
                    "with the original");
        } catch (GovernanceException e) {
            throw new GovernanceException("Exception occurred while executing getEndpointByUrl API method"
                    + e.getMessage());
        }
        assertNotNull(endpointManager.getEndpointByUrl("https://localhost:8080/axis2/services/BizService"));
        assertNull(endpointManager.getEndpointByUrl("http://MyDomain:8080/axis2/services/BizService"));
        assertNull(endpointManager.getEndpointByUrl("http://MyDomain:9883/axis2/services/BizService"));
    }

    @AfterClass()
    public void endGame() throws RegistryException {
        ServiceManager serviceManager = new ServiceManager(governance);
        serviceManager.removeService(serviceManager.findServices(new ServiceFilter() {
            public boolean matches(Service service) throws GovernanceException {
                String attributeVal = service.getAttribute("overview_name");
                if (attributeVal != null && attributeVal.startsWith("myServicxcde")) {
                    return true;
                }
                return false;
            }
        })[0].getId());
        serviceManager.removeService(serviceManager.findServices(new ServiceFilter() {
            public boolean matches(Service service) throws GovernanceException {
                String attributeVal = service.getAttribute("overview_name");
                if (attributeVal != null && attributeVal.startsWith("myService")) {
                    return true;
                }
                return false;
            }
        })[0].getId());
        serviceManager.removeService(serviceManager.findServices(new ServiceFilter() {
            public boolean matches(Service service) throws GovernanceException {
                String attributeVal = service.getAttribute("overview_name");
                if (attributeVal != null && attributeVal.startsWith("myServisdfcxcde")) {
                    return true;
                }
                return false;
            }
        })[0].getId());
        serviceManager.removeService(serviceManager.findServices(new ServiceFilter() {
            public boolean matches(Service service) throws GovernanceException {
                String attributeVal = service.getAttribute("overview_name");
                if (attributeVal != null && attributeVal.startsWith("abc")) {
                    return true;
                }
                return false;
            }
        })[0].getId());
        serviceManager.removeService(serviceManager.findServices(new ServiceFilter() {
            public boolean matches(Service service) throws GovernanceException {
                String attributeVal = service.getAttribute("overview_name");
                if (attributeVal != null && attributeVal.startsWith("bangService343")) {
                    return true;
                }
                return false;
            }
        })[0].getId());
        serviceManager.removeService(serviceManager.findServices(new ServiceFilter() {
            public boolean matches(Service service) throws GovernanceException {
                String attributeVal = service.getAttribute("overview_name");
                if (attributeVal != null && attributeVal.startsWith("BizService")) {
                    return true;
                }
                return false;
            }
        })[0].getId());
        registry.delete("/_system/governance/trunk/endpoints/dis");
        registry.delete("/_system/governance/trunk/schemas/org/bar/purchasing");
        registry.delete("/_system/governance/trunk/wsdls/com/foo");
        String[] endPoints = {"ep-endpoint1", "ep-endpoint11", "ep-endpoint11xx", "ep-endpoint1xx", "ep-endpoint2", "ep-endpoint22", "ep-endpoint22xx", "ep-endpoint2xx", "ep-endpoint3", "ep-endpoint4", "ep-Service1", "ep-Service2"};
        String endPointLocation = "/_system/governance/trunk/endpoints/";
        for (String endPoint : endPoints) {
            registry.delete(endPointLocation+endPoint);
        }
        String endPointLocation2 = "/_system/governance/trunk/endpoints/localhost/axis2/services/";
        String[] endPoints2 = {"ep-BizService", "ep-BizService-my-changes"};
        for (String endPoint : endPoints2) {
            registry.delete(endPointLocation2+endPoint);
        }

        governance = null;
        registry = null;
    }
}
