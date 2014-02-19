/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
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
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.extensions.utils.CommonConstants;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class EndpointTestCase {

    int userId = 1;
    private static Registry governance = null;
    private WSRegistryServiceClient wsRegistry;

    @BeforeClass(groups = {"wso2.greg"})
    public void initTest() throws RegistryException, AxisFault {
        UserInfo userInfo = UserListCsvReader.getUserInfo(userId);
        wsRegistry =
                new RegistryProviderUtil().getWSRegistry(userId,
                        ProductConstant.GREG_SERVER_NAME);
        governance = new RegistryProviderUtil().getGovernanceRegistry(wsRegistry, userId);
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
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

        Wsdl wsdl = wsdlManager.newWsdl("http://svn.wso2.org/repos/wso2/carbon/platform/trunk/components/governance/org.wso2.carbon.governance.api/src/test/resources/test-resources/wsdl/BizService.wsdl");
        wsdlManager.addWsdl(wsdl);

        Endpoint[] endpoints = wsdl.getAttachedEndpoints();
        Assert.assertEquals(endpoints.length, 1);

        Assert.assertEquals(endpoints[0].getUrl(), "http://localhost:8080/axis2/services/BizService");
        Assert.assertEquals(endpoints[0].getAttributeKeys().length, 1);
        Assert.assertEquals(endpoints[0].getAttribute(CommonConstants.SOAP11_ENDPOINT_ATTRIBUTE), "true");

        // now we are trying to remove the endpoint
        EndpointManager endpointManager = new EndpointManager(governance);

        Endpoint endpoint =
                endpointManager.newEndpoint("https://localhost:9443/axis2/services/BizServiceX");
        endpointManager.addEndpoint(endpoint);
        wsdl.attachEndpoint(endpoint);
        wsdlManager.updateWsdl(wsdl);
        wsdl = wsdlManager.getWsdl(wsdl.getId());
        endpoints = wsdl.getAttachedEndpoints();
        Assert.assertEquals(endpoints.length, 2);
        wsdl.detachEndpoint(endpoint.getId());
        wsdlManager.updateWsdl(wsdl);
        wsdl = wsdlManager.getWsdl(wsdl.getId());
        endpoints = wsdl.getAttachedEndpoints();
        Assert.assertEquals(endpoints.length, 1);

        try {
            endpointManager.removeEndpoint(endpoints[0].getId());
            Assert.assertTrue(false);
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
        GovernanceArtifact[] artifacts = wsdl.getDependents();
        // delete the wsdl
        wsdlManager.removeWsdl(wsdl.getId());

        ServiceManager serviceManager = new ServiceManager(governance);

        for (GovernanceArtifact artifact : artifacts) {
            if (artifact instanceof Service) {
                // getting the service.
                Service service2 = (Service) artifact;
                serviceManager.removeService(service2.getId());
            }
        }

        // now try to remove the endpoint
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

//        endpoints = getAttachedEndpointsFromService(service);

        endpoints = service.getAttachedEndpoints();
        Assert.assertEquals(endpoints.length, 4);

        Assert.assertEquals(endpoints[1].getUrl(), "http://endpoint2");
        Assert.assertEquals(1, endpoints[1].getAttributeKeys().length, 1);
        Assert.assertEquals(endpoints[1].getAttribute(CommonConstants.ENDPOINT_ENVIRONMENT_ATTR), "QA");

        Assert.assertEquals(endpoints[2].getUrl(), "http://endpoint3");
        Assert.assertEquals(endpoints[2].getAttributeKeys().length, 1);
        Assert.assertEquals(endpoints[2].getAttribute(CommonConstants.ENDPOINT_ENVIRONMENT_ATTR), "Dev");

        Assert.assertEquals(endpoints[3].getUrl(), "http://endpoint4");
        Assert.assertEquals(endpoints[3].getAttributeKeys().length, 1);
        Assert.assertEquals(endpoints[3].getAttribute(CommonConstants.ENDPOINT_ENVIRONMENT_ATTR), "Production");


        serviceManager.removeService(service.getId());
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

        Endpoint ep2 = endpointManager.newEndpoint("http://endpoint5xx");
        ep2.setAttribute(CommonConstants.ENDPOINT_ENVIRONMENT_ATTR, "QA");
        endpointManager.addEndpoint(ep2);

        service.attachEndpoint(ep1);
        service.attachEndpoint(ep2);

        Endpoint[] endpoints = serviceManager.getService(service.getId()).getAttachedEndpoints();
        Assert.assertEquals(endpoints.length, 2);

        Assert.assertEquals(endpoints[0].getUrl(), "http://endpoint1xx");
        Assert.assertEquals(endpoints[0].getAttributeKeys().length, 0);

        Assert.assertEquals(endpoints[1].getUrl(), "http://endpoint5xx");
        Assert.assertEquals(endpoints[1].getAttributeKeys().length, 1);
        Assert.assertEquals(endpoints[1].getAttribute(CommonConstants.ENDPOINT_ENVIRONMENT_ATTR), "QA");

        service.detachEndpoint(ep1.getId());
        endpoints = service.getAttachedEndpoints();
        Assert.assertEquals(endpoints.length, 1);

        Assert.assertEquals(endpoints[0].getUrl(), "http://endpoint5xx");
        Assert.assertEquals(endpoints[0].getAttributeKeys().length, 1);
        Assert.assertEquals(endpoints[0].getAttribute(CommonConstants.ENDPOINT_ENVIRONMENT_ATTR), "QA");


        // now update the endpoints in the service
        service.setAttributes("endpoints_entry", new String[]{
                "Dev:http://endpoint3",
                "Production:http://endpoint4",
                "QA:http://endpoint5xx",
        });
        serviceManager.updateService(service);

//        endpoints = getAttachedEndpointsFromService(service);
        endpoints = service.getAttachedEndpoints();
        Assert.assertEquals(endpoints.length, 3);


        Assert.assertEquals(endpoints[1].getUrl(), "http://endpoint3");
        Assert.assertEquals(endpoints[1].getAttributeKeys().length, 1);
        Assert.assertEquals(endpoints[1].getAttribute(CommonConstants.ENDPOINT_ENVIRONMENT_ATTR), "Dev");

        Assert.assertEquals(endpoints[2].getUrl(), "http://endpoint4");
        Assert.assertEquals(endpoints[2].getAttributeKeys().length, 1);
        Assert.assertEquals(endpoints[2].getAttribute(CommonConstants.ENDPOINT_ENVIRONMENT_ATTR), "Production");

        Assert.assertEquals(endpoints[0].getUrl(), "http://endpoint5xx");
        Assert.assertEquals(endpoints[0].getAttributeKeys().length, 1);
        Assert.assertEquals(endpoints[0].getAttribute(CommonConstants.ENDPOINT_ENVIRONMENT_ATTR), "QA");

        Endpoint ep5 = endpointManager.getEndpointByUrl("http://endpoint5xx");
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
        File file = new File(path + File.separator + "service.metadata.xml");
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
        Wsdl wsdl = wsdlManager.newWsdl("http://svn.wso2.org/repos/wso2/carbon/platform/trunk/components/governance/org.wso2.carbon.governance.api/src/test/resources/test-resources/wsdl/MyChangedBizService.wsdl");
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
        Service service = serviceManager.newService(new QName("http://wso2.com/test234/xxxxxx", "_myServicxcde"));
        serviceManager.addService(service);

        EndpointManager endpointManager = new EndpointManager(governance);
        Endpoint ep1 = endpointManager.newEndpoint("http://endpoint-dt");
        endpointManager.addEndpoint(ep1);

        Endpoint ep2 = endpointManager.newEndpoint("http://endpoint2-dt");
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

        service.detachEndpoint(ep1.getId());
        service.detachEndpoint(ep2.getId());

        service = serviceManager.getService(service.getId());
        String[] detachEndpoints = service.getAttributes("endpoints_entry");

        Assert.assertNull(detachEndpoints, "endpoint detach failed of service");


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
            throw new GovernanceException("Exception occurred while geting endpoints ");
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
                if (attributeVal != null && attributeVal.startsWith("myServisdfcxcde")) {
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
        serviceManager.removeService(serviceManager.findServices(new ServiceFilter() {
            public boolean matches(Service service) throws GovernanceException {
                String attributeVal = service.getAttribute("overview_name");
                if (attributeVal != null && attributeVal.startsWith("_myServicxcde")) {
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
                if (attributeVal != null && attributeVal.startsWith("abc")) {
                    return true;
                }
                return false;
            }
        })[0].getId());
        wsRegistry.delete("/_system/governance/trunk/endpoints/dis");
        wsRegistry.delete("/_system/governance/trunk/schemas/org/bar/purchasing");
        wsRegistry.delete("/_system/governance/trunk/wsdls/com/foo");
        String[] endPoints = {"ep-endpoint-dt", "ep-endpoint1", "ep-endpoint1xx", "ep-endpoint2", "ep-endpoint2-dt", "ep-endpoint5xx", "ep-endpoint3", "ep-endpoint4", "ep-Service1", "ep-Service2"};
        String endPointLocation = "/_system/governance/trunk/endpoints/";
        for (String endPoint : endPoints) {
            wsRegistry.delete(endPointLocation+endPoint);
        }
        String endPointLocation2 = "/_system/governance/trunk/endpoints/localhost/axis2/services/";
        String[] endPoints2 = {"ep-BizService", "ep-BizService-my-changes", "ep-BizServiceX"};
        for (String endPoint : endPoints2) {
           wsRegistry.delete(endPointLocation2+endPoint);
        }
        wsRegistry = null;
        governance = null;
    }

}
