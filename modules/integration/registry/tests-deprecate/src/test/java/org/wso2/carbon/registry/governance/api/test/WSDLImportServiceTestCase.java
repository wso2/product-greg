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

package org.wso2.carbon.registry.governance.api.test;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import javax.xml.namespace.QName;

import static org.testng.Assert.*;


public class WSDLImportServiceTestCase {
    private static final Log log = LogFactory.getLog(WSDLImportServiceTestCase.class);
    private static WSRegistryServiceClient registry = null;
    private static Registry governance = null;


    @BeforeClass(alwaysRun = true)
    public void init() throws RegistryException, AxisFault {
        governance = TestUtils.getRegistry();
        TestUtils.cleanupResources(governance);
        registry = TestUtils.getWSRegistry();
    }

    @Test(groups = {"wso2.greg"}, description = "upload GeoCoder WSDL sample", priority = 1)
    public void testAddSimpleWsdl_Import() throws org.wso2.carbon.registry.api.RegistryException {
        String wsdl_url = "http://geocoder.us/dist/eg/clients/GeoCoder.wsdl";
        String wsdl_path = "/_system/governance/trunk/wsdls/us/geocoder/rpc/geo/coder/us/GeoCoder.wsdl";
        String association_path = "/_system/governance/trunk/services/us/geocoder/rpc/geo/coder/us/GeoCode_Service";
        String service_namespace = "http://rpc.geocoder.us/Geo/Coder/US/";
        String service_name = "GeoCode_Service";
        String service_path = "/_system/governance/trunk/services/us/geocoder/rpc/geo/coder/us/GeoCode_Service";
        String property1 = "Invalid";
        String property2 = "Invalid";
        String property3 = "Aaaa";
        String keyword1 = "?xml version=";
        String keyword2 = "ArrayOfGeocoderResult";

        createWsdl(wsdl_url);        // Import wsdl Geocoder
        try {
            assertTrue(registry.resourceExists(wsdl_path), "Simple WSDL file exists");                        // Assert Resource exsists
            propertyAssertion(wsdl_path, property1, property2, property3);                                  //Assert Properties
            getAssociationPath(wsdl_path, association_path);                                                //Assert Association path exsist
            wsdlContentAssertion(wsdl_path, keyword1, keyword2);                                            //Assert wsdl content
            checkServiceExsist(service_namespace, service_name, service_path);                              //Assert Service Exsist
            registry.delete(wsdl_path);                                                                      //Remove wsdl
            registry.delete(service_path);                                                                   //Remove service
            assertFalse(registry.resourceExists(wsdl_path), "Simple WSDL file exists");                        //Assert wsdl was Removed successfully
            log.info("WSDLImportServiceTestClient testAddSimpleWsdl_Import()-Passed");
        } catch (RegistryException e) {
            log.error("Failed to assert WSDL Service Exsits:" + e);
            throw new RegistryException("Failed to assert WSDL Service Exsits:" + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "upload Upper Case WSDL sample", priority = 2)
    public void testAddUppercase_WSDL_extension()
            throws org.wso2.carbon.registry.api.RegistryException {
        String wsdl_url = "http://ws.strikeiron.com/donotcall2_5?WSDL";
        String wsdl_path = "/_system/governance/trunk/wsdls/com/strikeiron/www/donotcall2_5.wsdl";
        String association_path = "/_system/governance/trunk/services/com/strikeiron/www/DoNotCallRegistry";
        String service_namespace = "http://www.strikeiron.com";
        String service_name = "DoNotCallRegistry";
        String service_path = "/_system/governance/trunk/services/com/strikeiron/www/DoNotCallRegistry";
        String property1 = "Valid";
        String property2 = "Invalid";
        String property3 = "Aaaa";
        String keyword1 = "?xml version=";
        String keyword2 = "wsdl:definitions targetNamespace";

        //import wsdl file
        createWsdl(wsdl_url);

        try {
            assertTrue(registry.resourceExists(wsdl_path), "WSDL which ends with uppercase WSDL extension does not exists");      // Assert Resource exsists
            propertyAssertion(wsdl_path, property1, property2, property3);                                                       //Assert Properties
            getAssociationPath(wsdl_path, association_path);                                                                    //Assert Association path exsist
            wsdlContentAssertion(wsdl_path, keyword1, keyword2);                                                                 //Assert wsdl content
            checkServiceExsist(service_namespace, service_name, service_path);                                                   //Assert Service Exsist
            registry.delete(wsdl_path);                                                                                           //Remove wsdl
            registry.delete(service_path);                                                                                         //Remove service
            assertFalse(registry.resourceExists(wsdl_path), "WSDL which ends with uppercase WSDL extension exists");                 //Assert wsdl was Removed successfully
            log.info("WSDLImportServiceTestClient testAddUppercase_WSDL_extension()-Passed");
        } catch (RegistryException e) {
            log.error("Failed to add Upper Case WSDL:" + e);
            throw new RegistryException("Failed to add Upper Case WSDL:" + e);
        }
    }

    public void createWsdl(String wsdl_url) throws GovernanceException {
        WsdlManager wsdlManager = new WsdlManager(governance);
        Wsdl wsdl;
        try {
            wsdl = wsdlManager.newWsdl(wsdl_url);
            wsdl.addAttribute("version", "1.0.0");
            wsdl.addAttribute("creator", "Aaaa");
            wsdlManager.addWsdl(wsdl);
            log.info("wsdl was successfully added");
        } catch (GovernanceException e) {
            log.error("Failed to create WSDL:" + e);
            throw new GovernanceException("Failed to create WSDL:" + e);
        }
    }

    public void deleteWSDL() throws RegistryException {
        try {
            if (registry.resourceExists("/_system/governance/trunk/wsdls")) {
                registry.delete("/_system/governance/trunk/wsdls");
            }
        } catch (RegistryException e) {
            log.error("Failed to delete WSDL:" + e);
            throw new RegistryException("Failed to delete WSDL:" + e);
        }
    }

    public void propertyAssertion(String wsdl_path, String property1, String property2,
                                  String property3) throws RegistryException {
        Resource resource;
        try {
            resource = registry.get(wsdl_path);
            assertEquals(resource.getProperty("WSDL Validation"), property1, "WSDL Property - WSDL Validation");
            assertEquals(resource.getProperty("WSI Validation"), property2, "WSDL Property - WSI Validation");
            assertEquals(resource.getProperty("creator"), property3, "WSDL Property - WSI creator");
        } catch (RegistryException e) {
            log.error("Failed to assert WSDL property:" + e);
            throw new RegistryException("Failed to assert WSDL property:" + e);
        }
    }

    public void wsdlContentAssertion(String wsdl_path, String keyword1, String keyword2)
            throws org.wso2.carbon.registry.api.RegistryException {
        String content = null;
        try {
            Resource r1 = registry.newResource();
            r1 = registry.get(wsdl_path);
            content = new String((byte[]) r1.getContent());

            assertTrue(content.indexOf(keyword1) > 0, "Assert Content wsdl file - key word 1");
            assertTrue(content.indexOf(keyword2) > 0, "Assert Content wsdl file - key word 2");
        } catch (org.wso2.carbon.registry.api.RegistryException e) {
            log.error("Failed to assert WSDL content:" + e);
            throw new org.wso2.carbon.registry.api.RegistryException("Failed to assert WSDL content:" + e);
        }
    }

    public void getAssociationPath(String wsdl_path, String association_path)
            throws RegistryException {
        Association[] associations;
        try {

            associations = registry.getAssociations(wsdl_path, "usedBy");
            boolean status = false;
            for (Association association : associations) {
                if (association.getDestinationPath().equalsIgnoreCase(association_path)) {
                    status = true;
                }
            }

            assertTrue(status, "Association Path doesn't exists :");
        } catch (RegistryException e) {
            log.error("Failed to assert WSDL Association Path:" + e);
            throw new RegistryException("Failed to assert WSDL Association Path:" + e);
        }
    }

    public void checkServiceExsist(String service_namespace, String service_name,
                                   String service_path) throws RegistryException {
        ServiceManager serviceManager = new ServiceManager(governance);
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);

        Service[] services = new Service[0];
        try {
            services = serviceManager.getAllServices();
            for (Service service : services) {
                if (service.getQName().equals(new QName(service_namespace, service_name))) {
                    assertTrue(registry.resourceExists(service_path), "Service does not Exist :");
                }
            }
        } catch (GovernanceException e) {
            log.error("Failed to assert WSDL Service Exsits:" + e);
            throw new GovernanceException("Failed to assert WSDL Service Exsits:" + e);
        } catch (RegistryException e) {
            log.error("Failed to assert WSDL Service Exsits:" + e);
            throw new RegistryException("Failed to assert WSDL Service Exsits:" + e);
        }
    }
}



