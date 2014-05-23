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
import org.wso2.carbon.governance.api.schema.SchemaManager;
import org.wso2.carbon.governance.api.schema.dataobjects.Schema;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.io.*;
import java.net.MalformedURLException;

import static org.testng.Assert.*;


public class SchemaImportServiceTestCase {
    private static final Log log = LogFactory.getLog(SchemaImportServiceTestCase.class);
    private static Registry registry = null;
    private static Registry governance = null;
    private static String resourcePath;
    private String repoLocation = "http://svn.wso2.org/repos/wso2/carbon/platform/branches/4.2.0/platform-integration/" +
            "platform-automated-test-suite/1.2.0/org.wso2.carbon.automation.test.repo/src/main/resources/artifacts/GREG";

    @BeforeClass(alwaysRun = true)
    public void init() throws RegistryException, AxisFault, MalformedURLException {
        registry = TestUtils.getWSRegistry();
        governance = TestUtils.getRegistry();
        resourcePath = FrameworkPathUtil.getSystemResourceLocation()() + File.separator + ".." + File.separator + ".." + File.separator + ".."
                       + File.separator + "src" + File.separator + "test" + File.separator + "java" + File.separator
                       + "resources" + File.separator;
        deleteSchema();   //  Delete Schemas already existing
    }

    @Test(groups = {"wso2.greg"}, enabled = true, description = "upload Patient Schema sample")
    public void testAddGeoIPServiceSchema() throws RegistryException {
        String schema_url = "http://www.restfulwebservices.net/wcf/GeoIPService.svc?xsd=xsd0";
        String schema_path1 = "/_system/governance/trunk/schemas/net/restfulwebservices/www/" +
                              "servicecontracts/_2008/_01/GeoIPService.svc.xsd";
        String schema_path2 = "/_system/governance/trunk/schemas/net/restfulwebservices/www/" +
                              "datacontracts/_2008/_01/GeoIPService1.xsd";
        String schema1_property1 = "http://www.restfulwebservices.net/ServiceContracts/2008/01";
        String property3 = "Aaaa";
        String schema2_property1 = "Valid";
        String schema2_property2 = "http://www.restfulwebservices.net/DataContracts/2008/01";
        String keyword1 = "Registry";
        String keyword2 = "CountryCode";

        try {
            createSchema(governance, schema_url);                                                 //Add Schema
            assertTrue(registry.resourceExists(schema_path1), "GeoIPService.svc.xsd Not Present "); //Assert Schema exist
            assertTrue(registry.resourceExists(schema_path2), "GeoIPService1c.xsd Not Present ");
            propertyAssertion(schema_path1, schema2_property1, schema1_property1, property3);                    //Assert Properties
            propertyAssertion(schema_path2, null, schema2_property2, property3);
            schemaContentAssertion(schema_path2, keyword1, keyword2);                              //Assert Schema content
            registry.delete(schema_path1);                                                         //Remove Schema
            registry.delete(schema_path2);
            assertFalse(registry.resourceExists(schema_path1), "Schema exists at " + schema_path1);  //Assert Resource was deleted successfully
            assertFalse(registry.resourceExists(schema_path2), "Schema exists at " + schema_path2);
            log.info("SchemaImportServiceTestClient testAddPatientSchema()- Passed");
        } catch (RegistryException e) {
            log.error("Failed to add Patient Schema  :" + e);
            throw new RegistryException("Failed to add Patient Schema  :" + e);
        }
    }


    @Test(groups = {"wso2.greg"}, description = "upload Book Schema sample")
    public void testAddBookSchema() throws RegistryException {
        String schema_url = repoLocation + "/schema/books.xsd";
        String schema_path = "/_system/governance/trunk/schemas/books/books.xsd";
        String property1 = "Valid";
        String property2 = "urn:books";
        String property3 = "Aaaa";
        String keyword1 = "bks:BookForm";
        String keyword2 = "author";

        try {
            createSchema(governance, schema_url);                              //Add Schema
            assertTrue(registry.resourceExists(schema_path), "Book Schema exist ");  //Assert Schema exist
            propertyAssertion(schema_path, property1, property2, property3);   //Assert Properties
            schemaContentAssertion(schema_path, keyword1, keyword2);           //Assert Schema content
            registry.delete(schema_path);                                      //Remove Schema
            assertFalse(registry.resourceExists(schema_path), "Schema exists at " + schema_path);
            log.info("SchemaImportServiceTestClient testAddBookSchema()- Passed");
        } catch (RegistryException e) {
            log.error("Failed to add Book Schema  :" + e);
            throw new RegistryException("Failed to add Book Schema  :" + e);
        }
    }


    @Test(groups = {"wso2.greg"}, description = "upload Listing3 Schema sample")
    public void testAddListing3Schema() throws RegistryException {
        String schema_url = repoLocation + "/schema/SchemaImportSample.xsd";
        String schema_path = "/_system/governance/trunk/schemas/listing3/SchemaImportSample.xsd";
        String schema_path2 = "/_system/governance/trunk/schemas/listing4/LinkedSchema.xsd";
        String property1 = "Valid";
        String property2 = "urn:listing3";
        String property3 = "Aaaa";
        String keyword1 = "areaCode";
        String keyword2 = "exchange";

        try {

            createSchema(governance, schema_url);                         //Add Schema
            assertTrue(registry.resourceExists(schema_path), "Listing3 Schema exist ");//Assert Schema exist
            assertTrue(registry.resourceExists(schema_path2), "Listing4 Schema exist ");
            propertyAssertion(schema_path, property1, property2, property3);   //Assert Properties
            Association[] associations = registry.getAllAssociations(schema_path2); //Assert Association
            assertTrue(associations[1].getDestinationPath().equalsIgnoreCase(schema_path), "Association Exsists");
            schemaContentAssertion(schema_path, keyword1, keyword2);              //Assert Schema content
            registry.delete(schema_path);                                         //Remove Registry
            registry.delete(schema_path2);
            assertFalse(registry.resourceExists(schema_path), "Schema exists at " + schema_path);
            log.info("SchemaImportServiceTestClient testAddListing3Schema()- Passed");
        } catch (GovernanceException e) {
            log.error("Failed to add Listing3 Schema:" + e);
            throw new GovernanceException("Failed to add Listing3 Schema:" + e);
        } catch (RegistryException e) {
            log.error("Failed to add Listing3 Schema:" + e);
            throw new RegistryException("Failed to add Listing3 Schema:" + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "upload Listing4 Schema sample")
    public void testAddListing4Schema() throws RegistryException {
        String schema_url = repoLocation + "/schema/LinkedSchema.xsd";
        String schema_path = "/_system/governance/trunk/schemas/listing4/LinkedSchema.xsd";
        String property1 = "Valid";
        String property2 = "urn:listing4";
        String property3 = "Aaaa";
        String keyword1 = "areaCode2";
        String keyword2 = "exchange2";

        try {
            createSchema(governance, schema_url);                            //Add Schema
            assertTrue(registry.resourceExists(schema_path), "Listing4 Schema exist "); //Assert Schema exist
            propertyAssertion(schema_path, property1, property2, property3);   //Assert Properties
            schemaContentAssertion(schema_path, keyword1, keyword2);           //Assert Schema content
            registry.delete(schema_path);                                      //Remove Registry
            assertFalse(registry.resourceExists(schema_path), "Schema exists at " + schema_path);
            log.info("SchemaImportServiceTestClient testAddListing4Schema()- Passed");
        } catch (GovernanceException e) {
            log.error("Failed to add Listing4 Schema:" + e);
            throw new GovernanceException("Failed to add Listing4 Schema:" + e);
        } catch (RegistryException e) {
            log.error("Failed to add Listing4 Schema:" + e);
            throw new RegistryException("Failed to add Listing4 Schema:" + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "upload Listing4 Schema sample")
    public void testAddPurchasingSchema() throws RegistryException {
        String schema_url = repoLocation + "/xsd/purchasing.xsd";
        String schema_path = "/_system/governance/trunk/schemas/org/bar/purchasing/purchasing.xsd";
        String property1 = "Valid";
        String property2 = "http://bar.org/purchasing";
        String property3 = "Aaaa";
        String keyword1 = "productQueryResult";
        String keyword2 = "invalidProductId";

        try {
            createSchema(governance, schema_url);                                 //Add Schema
            assertTrue(registry.resourceExists(schema_path), "Purchasing Schema exist ");//Assert Schema exist
            propertyAssertion(schema_path, property1, property2, property3);      //Assert Properties
            schemaContentAssertion(schema_path, keyword1, keyword2);              //Assert Schema content
            registry.delete(schema_path);                                          //Remove Registry
            assertFalse(registry.resourceExists(schema_path), "Schema exists at " + schema_path);
            log.info("SchemaImportServiceTestClient testAddPurchasingSchema()- Passed");
        } catch (GovernanceException e) {
            log.error("Failed to add Purchasing Schema:" + e);
            throw new GovernanceException("Failed to add Purchasing Schema:" + e);
        } catch (RegistryException e) {
            log.error("Failed to add Purchasing Schema:" + e);
            throw new RegistryException("Failed to add Purchasing Schema:" + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "upload Schema from File")
    public void testAddSchemaFromFile()
            throws org.wso2.carbon.registry.api.RegistryException, FileNotFoundException {
        String filePath = resourcePath + "Person.xsd";
        String schema_path = "/_system/governance/trunk/schemas/Person.xsd";
        String keyword1 = "Name";
        String keyword2 = "SSN";
        Resource resource = registry.newResource();
        InputStream is = new BufferedInputStream(new FileInputStream(filePath));
        resource.setContentStream(is);

        try {
            registry.put(schema_path, resource);
//            RegistryClientUtils.importToRegistry(file, toPath, registry);//Upload Schema from file

            assertTrue(registry.resourceExists(schema_path), "Person Schema exist ");//Assert Resource Exists
            schemaContentAssertion(schema_path, keyword1, keyword2);           //Assert Schema content
            registry.delete(schema_path);                               //Delete Schema
            assertFalse(registry.resourceExists(schema_path), "Schema exists at " + schema_path);
            log.info("SchemaImportServiceTestClient testAddSchemaFromFile()- Passed");
        } catch (RegistryException e) {
            log.error("Failed to add Purchasing Schema from File:" + e);
            throw new RegistryException("Failed to add Purchasing Schema from File:" + e);
        }
    }

    public void createSchema(Registry governance, String schema_url) throws GovernanceException {
        SchemaManager schemaManager = new SchemaManager(governance);
        Schema schema;
        try {
            schema = schemaManager.newSchema(schema_url);
            schema.addAttribute("creator", "Aaaa");
            schema.addAttribute("version", "1.0.0");
            schemaManager.addSchema(schema);
            log.info("Schema was added successfully");
        } catch (GovernanceException e) {
            log.error("Failed to create Schema:" + e);
            throw new GovernanceException("Failed to create Schema:" + e);
        }
    }

    public void deleteSchema() throws RegistryException {
        try {
            if (registry.resourceExists("/_system/governance/trunk/schemas")) {
                registry.delete("/_system/governance/trunk/schemas");
            }
        } catch (RegistryException e) {
            log.error("Failed to Delete Schemas :" + e);
            throw new RegistryException("Failed to Delete Schemas :" + e);
        }
    }


    public void propertyAssertion(String schema_path, String property1, String property2,
                                  String property3) throws RegistryException {
        Resource resource;
        try {
            resource = registry.get(schema_path);
            assertEquals(resource.getProperty("Schema Validation"), property1, "Schema Property - Schema Validation");
            assertEquals(resource.getProperty("targetNamespace"), property2, "Schema Property - targetNamespace");
            assertEquals(resource.getProperty("creator"), property3, "Schema Property - Creator");
        } catch (RegistryException e) {
            log.error("Failed to assert Schema Properties:" + e);
            throw new RegistryException("Failed to assert Schema Properties:" + e);
        }
    }

    public void schemaContentAssertion(String schema_path, String keyword1, String keyword2)
            throws RegistryException {
        String content;
        try {
            Resource  r1 = registry.get(schema_path);
            content = new String((byte[]) r1.getContent());
            assertTrue(content.contains(keyword1), "Assert Content Schema file - key word 1");
            assertTrue(content.contains(keyword2) , "Assert Content Schema file - key word 2");
        } catch (org.wso2.carbon.registry.api.RegistryException e) {
            log.error("Failed to assert Schema content :" + e);
            throw new RegistryException("Failed to assert Schema content :" + e);
        }
    }

}
