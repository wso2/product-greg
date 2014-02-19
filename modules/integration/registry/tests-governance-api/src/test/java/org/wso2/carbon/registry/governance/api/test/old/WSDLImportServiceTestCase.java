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
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.api.endpoints.dataobjects.Endpoint;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import javax.xml.namespace.QName;

import static org.testng.Assert.*;

public class WSDLImportServiceTestCase {
    private static final Log log = LogFactory
            .getLog(WSDLImportServiceTestCase.class);
    private static WSRegistryServiceClient wsRegistry = null;
    private static Registry governanceRegistry = null;
    private Wsdl wsdl;
    private static final String pathPrefix = "/_system/governance";

    @BeforeClass(alwaysRun = true)
    public void init() throws RegistryException, AxisFault {

        int userId = 1;
        RegistryProviderUtil provider = new RegistryProviderUtil();
        wsRegistry = provider.getWSRegistry(userId,
                ProductConstant.GREG_SERVER_NAME);
        governanceRegistry = provider.getGovernanceRegistry(wsRegistry, userId);
    }

    @Test(groups = {"wso2.greg"}, description = "upload GeoCoder WSDL sample", priority = 1)
    public void testAddSimpleWsdl_Import()
            throws org.wso2.carbon.registry.api.RegistryException {
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

        createWsdl(wsdl_url); // Import wsdl Geocoder

        assertTrue(wsRegistry.resourceExists(wsdl_path),
                "Simple WSDL file exists"); // Assert Resource exsists
        propertyAssertion(wsdl_path, property1, property2, property3); // Assert
        // Properties
        getAssociationPath(wsdl_path, association_path); // Assert
        // Association
        // path exsist
        wsdlContentAssertion(wsdl_path, keyword1, keyword2); // Assert wsdl
        // content                                                                              http://geocoder.us/dist/eg/clients/GeoCoder.wsdl
        checkServiceExist(service_namespace, service_name, service_path); // Assert
        // Service
        // Exsist

        Endpoint[] endpoints = wsdl.getAttachedEndpoints();
        wsRegistry.delete(wsdl_path); // Remove wsdl
        wsRegistry.delete(service_path); // Remove service
        for (Endpoint tmpEndpoint : endpoints) {
            if (wsRegistry.resourceExists(pathPrefix + tmpEndpoint.getPath())) {
                wsRegistry.delete(pathPrefix + tmpEndpoint.getPath());
            }
        }
        assertFalse(wsRegistry.resourceExists(wsdl_path), "Simple WSDL file exists"); // Assert wsdl was Removed
        // successfully
        log.info("WSDLImportServiceTestClient testAddSimpleWsdl_Import()-Passed");

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

        // import wsdl file
        createWsdl(wsdl_url);

        assertTrue(wsRegistry.resourceExists(wsdl_path),
                "WSDL which ends with uppercase WSDL extension does not exists"); // Assert
        // Resource
        // exsists
        propertyAssertion(wsdl_path, property1, property2, property3); // Assert
        // Properties
        getAssociationPath(wsdl_path, association_path); // Assert
        // Association
        // path exsist
        wsdlContentAssertion(wsdl_path, keyword1, keyword2); // Assert wsdl
        // content
        checkServiceExist(service_namespace, service_name, service_path); // Assert
        // Service
        // Exsist

        Endpoint[] endpoints = wsdl.getAttachedEndpoints();
        wsRegistry.delete(wsdl_path); // Remove wsdl
        wsRegistry.delete(service_path); // Remove service
        for (Endpoint tmpEndpoint : endpoints) {
            if (wsRegistry.resourceExists(pathPrefix + tmpEndpoint.getPath())) {
                wsRegistry.delete(pathPrefix + tmpEndpoint.getPath());
            }
        }
        assertFalse(wsRegistry.resourceExists(wsdl_path),
                "WSDL which ends with uppercase WSDL extension exists"); // Assert
        // wsdl
        // was
        // Removed
        // successfully
        log.info("WSDLImportServiceTestClient testAddUppercase_WSDL_extension()-Passed");

    }

    public void createWsdl(String wsdl_url) throws GovernanceException {
        WsdlManager wsdlManager = new WsdlManager(governanceRegistry);

        wsdl = wsdlManager.newWsdl(wsdl_url);
        wsdl.addAttribute("version", "1.0.0");
        wsdl.addAttribute("creator", "Aaaa");
        wsdlManager.addWsdl(wsdl);
        log.info("wsdl was successfully added");

    }


    public void propertyAssertion(String wsdl_path, String property1,
                                  String property2, String property3) throws RegistryException {
        Resource resource;

        resource = wsRegistry.get(wsdl_path);
        assertEquals(resource.getProperty("WSDL Validation"), property1,
                "WSDL Property - WSDL Validation");
        assertEquals(resource.getProperty("WSI Validation"), property2,
                "WSDL Property - WSI Validation");
        assertEquals(resource.getProperty("creator"), property3,
                "WSDL Property - WSI creator");

    }

    public void wsdlContentAssertion(String wsdl_path, String keyword1,
                                     String keyword2)
            throws org.wso2.carbon.registry.api.RegistryException {
        String content = null;

        Resource r1 = wsRegistry.newResource();
        r1 = wsRegistry.get(wsdl_path);
        content = new String((byte[]) r1.getContent());

        assertTrue(content.indexOf(keyword1) > 0,
                "Assert Content wsdl file - key word 1");
        assertTrue(content.indexOf(keyword2) > 0,
                "Assert Content wsdl file - key word 2");

    }

    public void getAssociationPath(String wsdl_path, String association_path)
            throws RegistryException {
        Association[] associations;

        associations = wsRegistry.getAssociations(wsdl_path, "usedBy");
        boolean status = false;
        for (Association association : associations) {
            if (association.getDestinationPath().equalsIgnoreCase(
                    association_path)) {
                status = true;
            }
        }

        assertTrue(status, "Association Path doesn't exists :");

    }

    public void checkServiceExist(String service_namespace,
                                  String service_name, String service_path) throws RegistryException {
        ServiceManager serviceManager = new ServiceManager(governanceRegistry);

        Service[] services = serviceManager.getAllServices();
        boolean check = false;
        for (Service service : services) {
            if (service.getQName().equals(
                    new QName(service_namespace, service_name))) {
                if (wsRegistry.resourceExists(service_path)) {
                    check = true;
                }

            }

        }
        assertTrue(check, "Service does not Exist :");
    }

    @AfterClass(groups = "wso2.greg", alwaysRun = true, description = "cleaning up the memory, artifacts are deleted locally for convenience")
    public void tearDown() throws GovernanceException {

        wsRegistry = null;
        governanceRegistry = null;
        wsdl = null;

    }

}
