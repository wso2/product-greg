/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/


package org.wso2.carbon.registry.governance.api.test;


import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.governance.api.endpoints.EndpointManager;
import org.wso2.carbon.governance.api.endpoints.dataobjects.Endpoint;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.schema.SchemaManager;
import org.wso2.carbon.governance.api.schema.dataobjects.Schema;
import org.wso2.carbon.governance.api.services.ServiceFilter;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.governance.api.test.util.FileManagerUtil;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;

import static org.testng.Assert.*;


/**
 * Contain tests for volume tests carried out using governance API
 */
public class MetaDataVolumeTestClient {

    private static final Log log = LogFactory.getLog(MetaDataVolumeTestClient.class);
    private static Registry governance;

    @BeforeClass(groups = {"wso2.greg"})
    public void initTest() throws RegistryException, AxisFault {
        governance = TestUtils.getRegistry();
        TestUtils.cleanupResources(governance);
    }

    @Test(groups = {"wso2.greg"}, description = "Add 1000 endpoints", priority = 1)
    public void testAddLargeNumberOfEndpoints() throws RegistryException {
        int numberOfEndpoints = 1000;
        EndpointManager endpointManager = new EndpointManager(governance);
        log.info("adding " + numberOfEndpoints + "of Endpoints...");
        for (int i = 1; i < numberOfEndpoints; i++) {
            Endpoint ep1 = endpointManager.newEndpoint("http://wso2.automation.endpoint" + i);
            endpointManager.addEndpoint(ep1);
            assertTrue(endpointManager.getEndpoint(ep1.getId()).getQName().toString().contains
                    ("http://wso2.automation.endpoint" + i), "Endpoint not found");
        }

        for (int i = 1; i < numberOfEndpoints; i++) {
            governance.delete("trunk/endpoints" + "/ep-wso2-automation-endpoint"
                              + i);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "add new rxt file", priority = 2)
    public void testAddLargeNumberOfGenericArtifacts() throws Exception {
        int numberOfArtifacts = 10000;
        GenericArtifactManager artifactManager = new GenericArtifactManager(governance, "events");

        GenericArtifact artifact;
        log.info("adding " + numberOfArtifacts + "of Generic Artifacts...");
        for (int i = 1; i <= numberOfArtifacts; i++) {
            String governanceArtifactContent = "<metadata xmlns=\"http://www.wso2" +
                                               ".org/governance/metadata\"><details><author>testAuthor" +
                                               "</author><venue>Colombo</venue><date>12/12/2012</date>" +
                                               "<name>testEvent" + i + "</name>" + "</details><overview>" +
                                               "<namespace></namespace></overview><serviceLifecycle>" +
                                               "<lifecycleName>ServiceLifeCycle</lifecycleName>" +
                                               "</serviceLifecycle><rules>" + "<gender>male</gender>" +
                                               "<description>Coding event</description></rules></metadata>";

            artifact = artifactManager.newGovernanceArtifact(AXIOMUtil.stringToOM(governanceArtifactContent));
            artifactManager.addGenericArtifact(artifact);
        }

        //delete all artifacts
        int counter = 0;
        GenericArtifact[] genericArtifacts = artifactManager.getAllGenericArtifacts();
        for (GenericArtifact genericArtifact : genericArtifacts) {
            if (genericArtifact.getQName().toString().contains("testEvent")) {
                counter++;
                artifactManager.removeGenericArtifact(genericArtifact.getId());
                assertNull(artifactManager.getGenericArtifact(genericArtifact.getId()));
            }
        }
        assertTrue(counter == numberOfArtifacts, "All artifacts were not added.");
    }

//    @Test(groups = {"wso2.greg"}, description = "Add 1000 policies", priority = 3)
//    public void testAddLargeNumberOfPolicies() throws GovernanceException {
//        int policyCount = 1000;
//        PolicyManager policyManager = new PolicyManager(governance);
//
//        Policy policy;
//        log.info("adding " + policyCount + "of Policies...");
//        int policyCountBeforeTest = policyManager.getAllPolicies().length;
//
//        for (int i = 1; i <= policyCount; i++) {
//            policy = policyManager.newPolicy("http://svn.wso2.org/repos/wso2/carbon/platform/" +
//                                             "trunk/platform-integration/system-test-framework" +
//                                             "/core/org.wso2.automation.platform.core/src/main/" +
//                                             "resources/artifacts/GREG/policy/UTPolicy.xml");
//            policy.setName("WSO2AutomationUTPolicy" + i + ".xml");
//            policyManager.addPolicy(policy);
//        }
//        Policy[] policies = policyManager.getAllPolicies();
//        int policyCountAfterTest = policies.length;
//        assertTrue(((policyCountAfterTest - policyCountBeforeTest) == policyCount),
//                   "All " + policyCount + "policies were not added");
//
//        //delete policies
//        for (Policy policyEntry : policies) {
//            if (policyEntry.getQName().toString().contains("WSO2AutomationUTPolicy")) {
//                policyManager.removePolicy(policyEntry.getId());
//                Assert.assertNull(policyManager.getPolicy(policyEntry.getId()));
//            }
//        }
//    }

    @Test(groups = {"wso2.greg"}, description = "add 10000 resources to registry", priority = 4)
    public void testAddLargeNumberOfServices() throws Exception {
        ServiceManager serviceManager = new ServiceManager(governance);
        Service newService;
        final String serviceName = "WSO2AutomatedService";
        int numberOfServices = 10 * 1000; //10000 services
        log.info("adding " + numberOfServices + "of Services...");
        for (int i = 1; i <= numberOfServices; i++) {
            Service service = serviceManager.newService(new QName("http://wso2.test" +
                                                                  ".automation/boom/test" + i,
                                                                  serviceName + i));
            service.addAttribute("testAttribute", "service" + i);
            serviceManager.addService(service);
            String serviceId = service.getId();
            newService = serviceManager.getService(serviceId);
            assertTrue(newService.getQName().toString().contains(serviceName + i));
            assertEquals(newService.getAttribute("testAttribute"), "service" + i);

        }
        assertTrue(serviceManager.getAllServices().length >= numberOfServices,
                   "Less than " + numberOfServices + " services exists");

        assertTrue(serviceManager.getAllServiceIds().length >= numberOfServices,
                   "Less than " + numberOfServices + "  ids exists");

        String[] servicePaths = serviceManager.getAllServicePaths();

        assertTrue(serviceManager.getAllServicePaths().length >= numberOfServices,
                   "Less than " + numberOfServices + "  paths exists");

        //delete services
        for (String servicePath : servicePaths) {
            if (servicePath.contains(serviceName)) {
                governance.delete(servicePath);
                numberOfServices--;
                ServiceFilter filter = new ServiceFilter() {
                    public boolean matches(Service service) throws GovernanceException {
                        return service.getQName().toString().contains(serviceName);
                    }
                };
                assertTrue(serviceManager.findServices(filter).length == numberOfServices);
            }
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Attache 1000 endpoints to a service", priority = 5)
    public void testAttachLargeNumberOfEndpoints() throws RegistryException {
        String service_namespace = "http://wso2.org/atomation/test";
        String service_name = "ServiceForLargeNumberOfEndpoints";
        int numberOfEndPoints = 1000;

        ServiceManager serviceManager = new ServiceManager(governance);
        Service service;
        service = serviceManager.newService(new QName(service_namespace, service_name));
        serviceManager.addService(service);
        EndpointManager endpointManager = new EndpointManager(governance);
        log.info("Attaching " + numberOfEndPoints + "of Endpoints to a service...");
        for (int i = 1; i <= numberOfEndPoints; i++) {
            Endpoint ep1 = endpointManager.newEndpoint("http://wso2.automation" +
                                                       ".endpoint" + i);
            endpointManager.addEndpoint(ep1);
            service.attachEndpoint(ep1);
        }

        Endpoint[] endpoints = service.getAttachedEndpoints();
        assertEquals(numberOfEndPoints, endpoints.length);

        //Detach Endpoint one
        for (Endpoint endpoint : endpoints) {
            service.detachEndpoint(endpoint.getId());
            numberOfEndPoints--;
            Assert.assertTrue(numberOfEndPoints == service.getAttachedEndpoints().length);
        }

        //remove the service
        serviceManager.removeService(service.getId());
        Assert.assertNull(serviceManager.getService(service.getId()));
    }

//    @Test(groups = {"wso2.greg"}, description = "Attache 100 policies to a service", priority = 6)
//    public void testAttachLargeNumberOfPolicies() throws RegistryException {
//        String service_namespace = "http://wso2.org/atomation/test";
//        String service_name = "ServiceForLargeNumberOfPolicies1";
//        int numberOfPolicies = 1000;
//
//        ServiceManager serviceManager = new ServiceManager(governance);
//        Service service;
//        service = serviceManager.newService(new QName(service_namespace, service_name));
//        serviceManager.addService(service);
//        PolicyManager policyManager = new PolicyManager(governance);
//        log.info("Attaching " + numberOfPolicies + "of Policies to a service...");
//        for (int i = 1; i <= numberOfPolicies; i++) {
//            Policy policy = policyManager.newPolicy("https://svn.wso2.org/repos/wso2/carbon/platform" +
//                                                    "/trunk/platform-integration/system-test-framework" +
//                                                    "/core/org.wso2.automation.platform.core/src/main" +
//                                                    "/resources/artifacts/GREG/policy/UTPolicy.xml");
//            policy.setName("testPolicy" + i);
//            policyManager.addPolicy(policy);
//            service.attachPolicy(policy);
//        }
//
//        Policy[] policies = service.getAttachedPolicies();
//        assertEquals(numberOfPolicies, policies.length);
//
//        //Detach Endpoint one
//        for (Policy policy : policies) {
//            service.detachPolicy(policy.getId());
//            numberOfPolicies--;
//            Assert.assertTrue(numberOfPolicies == service.getAttachedPolicies().length);
//        }
//
//        //remove the service
//        serviceManager.removeService(service.getId());
//        Assert.assertNull(serviceManager.getService(service.getId()));
//    }


    @Test(groups = {"wso2.greg"}, description = "Adding large number of schemas", priority = 7)
    public void testAddLargeNoOfSchemas() throws GovernanceException {
        Schema schema;
        int schemaCount = 10000;
        String schemaContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                               "<xsd:schema xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " +
                               "targetNamespace=\"http://www.company.org\" xmlns=\"http://www.company.org\" " +
                               "elementFormDefault=\"qualified\">\n" +
                               "    <xsd:complexType name=\"PersonType\">\n" +
                               "        <xsd:sequence>\n" +
                               "           <xsd:element name=\"Name\" type=\"xsd:string\"/>\n" +
                               "           <xsd:element name=\"SSN\" type=\"xsd:string\"/>\n" +
                               "        </xsd:sequence>\n" +
                               "    </xsd:complexType>\n" +
                               "</xsd:schema>";

        SchemaManager schemaManager = new SchemaManager(governance);
        Schema[] schemaList = schemaManager.getAllSchemas();
        for (Schema s : schemaList) {
            if (s.getQName().getLocalPart().contains("Automated")) {
                schemaManager.removeSchema(s.getId());
            }
        }
        log.info("Adding " + schemaCount + " of Schemas..");
        try {
            for (int i = 0; i <= schemaCount; i++) {
                schema = schemaManager.newSchema(schemaContent.getBytes(), "AutomatedSchema" + i + ".xsd");
                schemaManager.addSchema(schema);
                if (!schemaManager.getSchema(schema.getId()).getQName().getLocalPart().
                        equalsIgnoreCase("AutomatedSchema" + i + ".xsd")) {
                    assertTrue(false, "Schema not added..");
                }
            }
        } catch (GovernanceException e) {
            throw new GovernanceException("Error found while adding multiple schemas : " + e.getMessage());
        } catch (RegistryException e) {
            e.printStackTrace();
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Test adding multiple wsdl", priority = 8)
    public void testMultipleWsdl() throws GovernanceException, IOException {
        Wsdl wsdl;
        int wsdlCount = 10000;
        String resourcePath = FrameworkSettings.getFrameworkPath() + File.separator + ".." + File.separator + ".." + File.separator + ".."
                              + File.separator + "src" + File.separator + "test" + File.separator + "java" + File.separator
                              + "resources";

        WsdlManager wsdlManager = new WsdlManager(governance);
        String wsdlFileLocation = resourcePath + File.separator + "wsdl" + File.separator + "Automated.wsdl";


        Wsdl[] wsdlList = wsdlManager.getAllWsdls();
        for (Wsdl w : wsdlList) {
            if (w.getQName().getLocalPart().contains("Automated")) {
                wsdlManager.removeWsdl(w.getId());
            }
        }
        log.info("Adding " + wsdlCount + " of WSDLs..");
        try {
            for (int i = 0; i <= wsdlCount; i++) {
                wsdl = wsdlManager.newWsdl(FileManagerUtil.readFile(wsdlFileLocation).getBytes(),
                                           "AutomatedWsdl" + i + ".wsdl");
                wsdlManager.addWsdl(wsdl);
                if (!wsdlManager.getWsdl(wsdl.getId()).getQName().getLocalPart().equalsIgnoreCase
                        ("AutomatedWsdl" + i + ".wsdl")) {
                    assertTrue(false, "Wsdl not added..");
                }
            }
        } catch (GovernanceException e) {
            throw new GovernanceException("Error found while adding multiple Wsdl : " + e.getMessage());
        } catch (RegistryException e) {
            e.printStackTrace();
        }
    }
}
