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

package org.wso2.carbon.registry.ws.client.test.general.old;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.registry.api.Collection;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import java.io.*;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

//https://wso2.org/jira/browse/REGISTRY-2173
public class GarFileImportServiceWSTestCase extends GREGIntegrationBaseTest {

    private static final Log log = LogFactory.getLog(GarFileImportServiceWSTestCase.class);
    private static String resourcePath;
    private WSRegistryServiceClient registry;

    @BeforeClass(groups = {"wso2.greg"}, alwaysRun = true)
    public void initialize() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        registry = registryProviderUtil.getWSRegistry(automationContext);
        resourcePath = getTestArtifactLocation() + "artifacts" + File.separator + "GREG" + File.separator;
        Thread.sleep(8000);
    }

    @Test(groups = {"wso2.greg"}, description = "GAR file uploader - Axis2Service")
    public void testAddAxis2ServiceGarFile() throws org.wso2.carbon.registry.api.RegistryException, IOException {
        String filePath = resourcePath + "Axis2Service.gar";
        String service_path = "/_system/governance/trunk/services/org/wso2/carbon/service/1.0.0/Axis2Service";
        String wsdl_path = "/_system/governance/trunk/wsdls/org/wso2/carbon/service/1.0.0/Axis2Service.wsdl";
        String schema_path = "/_system/governance/trunk/schemas/org/wso2/carbon/service/1.0.0/axis2serviceschema.xsd";
        InputStream is = null;
        try {
            // Create Collection
            Collection collection = registry.newCollection();
            registry.put("/a1/a2/a3", collection);
            //Create Resource
            Resource r1 = registry.newResource();
            //create an Input Stream
            is = new BufferedInputStream(new FileInputStream(filePath));
            r1.setContentStream(is);
            r1.setMediaType("application/vnd.wso2.governance-archive");
            registry.put("/a1/a2/a3/r1", r1);

            //Assert Service exists
            assertTrue(registry.resourceExists(service_path), "Service Exists :");
            //Assert WSDL exists
            assertTrue(registry.resourceExists(wsdl_path), "WSDL Exists :");
            //Assert SCHEMA exists
            assertTrue(registry.resourceExists(schema_path), "Schema Exists :");
            //delete resources
            deleteResources(service_path);
            deleteResources(wsdl_path);
            deleteResources(schema_path);
            deleteResources("/a1");
            //Assert Resources have been properly deleted
            assertFalse(registry.resourceExists(service_path), "Service Deleted :");
            assertFalse(registry.resourceExists(wsdl_path), "WSDL Deleted :");
            assertFalse(registry.resourceExists(schema_path), "Schema Deleted :");
            log.info("GarFileImportServiceTestClient testAddAxis2ServiceGarFile()- Passed");
        } catch(RegistryException e) {
            log.error("Failed to add Axis2ServiceGarFile  RegistryException thrown:" + e);
            throw new RegistryException("Failed to add Axis2ServiceGarFile: " + e);
        } catch(FileNotFoundException e) {
            log.error("Failed to add Axis2ServiceGarFile FileNotFoundException  thrown: " + e);
            throw new RegistryException("Failed to add Axis2ServiceGarFile :" + e);
        } finally {
            assert is != null;
            is.close();
        }
    }

    @Test(groups = {"wso2.greg"}, description = "GAR file uploader - SimpleStockQuoteService",
            dependsOnMethods = "testAddAxis2ServiceGarFile")
    public void testAddSimpleStockQuoteServiceGarFile() throws Exception {
        String filePath = resourcePath + "SimpleStockQuoteService.gar";
        String service_path = "/_system/governance/trunk/services/samples/services/1.0.0/SimpleStockQuoteService";
        String wsdl_path = "/_system/governance/trunk/wsdls/samples/services/1.0.0/SimpleStockQuoteService.wsdl";
        try {
            //Create Resource
            Resource r2 = registry.newResource();
            //create an Input Stream
            InputStream is1 = new BufferedInputStream(new FileInputStream(filePath));
            r2.setContentStream(is1);
            r2.setMediaType("application/vnd.wso2.governance-archive");
            registry.put("/a1/a2/a3/r2", r2);
            //Assert Service exists
            assertTrue(registry.resourceExists(service_path), "Service Exists :");
            //Assert WSDL exists
            assertTrue(registry.resourceExists(wsdl_path), "WSDL Exists :");
            //Delete Resources
            deleteResources(service_path);
            deleteResources(wsdl_path);
            deleteResources("/a1");
            //Assert Resources have been properly deleted
            assertFalse(registry.resourceExists(service_path), "Service Deleted :");
            assertFalse(registry.resourceExists(wsdl_path), "WSDL Deleted :");
            log.info("GarFileImportServiceTestClient testAddSimpleStockQuoteServiceGarFile()- Passed");
        } catch(FileNotFoundException e) {
            log.error("Failed to add SimpleStockQuoteService GarFile:" + e);
            throw new FileNotFoundException("Failed to add SimpleStockQuoteService GarFile:" + e);
        } catch(org.wso2.carbon.registry.api.RegistryException e) {
            log.error("Failed to add SimpleStockQuoteService GarFile:" + e);
            throw new RegistryException("Failed to add SimpleStockQuoteService GarFile:" + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "GAR file uploader - XSDAllGarFile",
            dependsOnMethods = "testAddSimpleStockQuoteServiceGarFile")
    public void testAddXSDAllGarFile() throws FileNotFoundException, org.wso2.carbon.registry.api.RegistryException {
        String filePath = resourcePath + "xsdAll.gar";
        String schema_path1 = "/_system/governance/trunk/schemas/org/datacontract/schemas/_2004/_07/system/1.0.0/test1.xsd";
        String schema_path2 = "/_system/governance/trunk/schemas/com/microsoft/schemas/_2003/_10/serialization/1.0.0/test2.xsd";
        String schema_path3 = "/_system/governance/trunk/schemas/org/tempuri/1.0.0/test3.xsd";
        String schema_path4 = "/_system/governance/trunk/schemas/com/microsoft/schemas/_2003/_10/serialization/1.0.0/test4.xsd";
        try {
            //Create Resource
            Resource r6 = registry.newResource();
            //create an Input Stream
            InputStream is1 = new BufferedInputStream(new FileInputStream(filePath));
            r6.setContentStream(is1);
            r6.setMediaType("application/vnd.wso2.governance-archive");
            registry.put("/a1/a2/a3/r6", r6);
            //Assert Schema exists
            assertTrue(registry.resourceExists(schema_path1), "Schema Exists1 :");
            assertTrue(registry.resourceExists(schema_path2), "Schema Exists2 :");
            assertTrue(registry.resourceExists(schema_path3), "Schema Exists3 :");
            assertTrue(registry.resourceExists(schema_path4), "Schema Exists4 :");
            //Delete Resources
            deleteResources(schema_path1);
            deleteResources(schema_path2);
            deleteResources(schema_path3);
            deleteResources(schema_path4);
            deleteResources("/a1");
            //Assert Resources have been properly deleted
            assertFalse(registry.resourceExists(schema_path1), "schema_path1 Deleted :");
            assertFalse(registry.resourceExists(schema_path2), "schema_path2 Deleted :");
            assertFalse(registry.resourceExists(schema_path3), "schema_path3 Deleted :");
            assertFalse(registry.resourceExists(schema_path4), "schema_path4 Deleted :");
            log.info("GarFileImportServiceTestClient testAddXSDAllGarFile()- Passed");
        } catch(FileNotFoundException e) {
            log.error("Failed to add XSDAll Gar-File:" + e);
            throw new FileNotFoundException("Failed to add XSDAll Gar-File:" + e);
        } catch(org.wso2.carbon.registry.api.RegistryException e) {
            log.error("Failed to add XSDAll Gar-File:" + e);
            throw new org.wso2.carbon.registry.api.RegistryException("Failed to add XSDAll Gar-File:" + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "GAR file uploader - HeterogeneousNamespace",
            dependsOnMethods = "testAddXSDAllGarFile")
    public void testAddHeterogeneousNamespaceGarFile() throws FileNotFoundException, org.wso2.carbon.registry.api.RegistryException {
        String filePath = resourcePath + "Heterogeneous_Namespace.gar";
        String schema_path1 = "/_system/governance/trunk/schemas/org/company/www/1.0.0/Company.xsd";
        String schema_path2 = "/_system/governance/trunk/schemas/org/person/www/1.0.0/Person.xsd";
        String schema_path3 = "/_system/governance/trunk/schemas/org/product/www/1.0.0/Product.xsd";
        try {
            //Create Resource
            Resource r7 = registry.newResource();
            //create an Input Stream
            InputStream is1 = new BufferedInputStream(new FileInputStream(filePath));
            r7.setContentStream(is1);
            r7.setMediaType("application/vnd.wso2.governance-archive");
            registry.put("/a1/a2/a3/r7", r7);
            //Assert Schema exists
            assertTrue(registry.resourceExists(schema_path1), "Schema Exists1 :");
            assertTrue(registry.resourceExists(schema_path2), "Schema Exists2 :");
            assertTrue(registry.resourceExists(schema_path3), "Schema Exists3 :");
            //Delete Resources
            deleteResources(schema_path1);
            deleteResources(schema_path2);
            deleteResources(schema_path3);
            deleteResources("/a1");
            //Assert Resources have been properly deleted
            assertFalse(registry.resourceExists(schema_path1), "schema_path1 Deleted :");
            assertFalse(registry.resourceExists(schema_path2), "schema_path2 Deleted :");
            assertFalse(registry.resourceExists(schema_path3), "schema_path3 Deleted :");
            log.info("GarFileImportServiceTestClient testAddHeterogeneousNamespaceGarFile()- Passed");
        } catch(FileNotFoundException e) {
            log.error("Failed to add HeterogeneousNamespace Gar-File:" + e);
            throw new FileNotFoundException("Failed to add HeterogeneousNamespace Gar-File:" + e);
        } catch(org.wso2.carbon.registry.api.RegistryException e) {
            log.error("Failed to add HeterogeneousNamespace Gar-File:" + e);
            throw new org.wso2.carbon.registry.api.RegistryException("Failed to add HeterogeneousNamespace Gar-File:" + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "GAR file uploader - ArtistRegistry",
            dependsOnMethods = "testAddHeterogeneousNamespaceGarFile")
    public void testAddArtistRegistryGarFile() throws FileNotFoundException, org.wso2.carbon.registry.api.RegistryException {
        String filePath = resourcePath + "artistRegistry.gar";
        String service_path = "/_system/governance/trunk/services/eu/yesso/soamoa/samples/1.0.0/ArtistRegistryService";
        String wsdl_path = "/_system/governance/trunk/wsdls/eu/yesso/soamoa/samples/1.0.0/artistRegistry.wsdl";
        String schema_path = "/_system/governance/trunk/schemas/eu/yesso/soamoa/samples/1.0.0/artistRegistry.xsd";
        try {
            //Create Resource
            Resource r8 = registry.newResource();
            //create an Input Stream
            InputStream is1 = new BufferedInputStream(new FileInputStream(filePath));
            r8.setContentStream(is1);
            r8.setMediaType("application/vnd.wso2.governance-archive");
            registry.put("/a1/a2/a3/r8", r8);
            //Assert Schema exists
            assertTrue(registry.resourceExists(service_path), "service_path Exists1 :");
            assertTrue(registry.resourceExists(wsdl_path), "wsdl_path  Exists2 :");
            assertTrue(registry.resourceExists(schema_path), "Schema Exists3 :");
            //Delete Resources
            deleteResources(service_path);
            deleteResources(wsdl_path);
            deleteResources(schema_path);
            deleteResources("/a1");
            //Assert Resources have been properly deleted
            assertFalse(registry.resourceExists(service_path), "service_path Deleted :");
            assertFalse(registry.resourceExists(wsdl_path), "wsdl_path  Deleted :");
            assertFalse(registry.resourceExists(schema_path), "schema_path Deleted :");
            log.info("GarFileImportServiceTestClient testAddArtistRegistryGarFile()- Passed");
        } catch(FileNotFoundException e) {
            log.error("Failed to add ArtistRegistry Gar-File:" + e);
            throw new FileNotFoundException("Failed to add ArtistRegistry Gar-File:" + e);
        } catch(org.wso2.carbon.registry.api.RegistryException e) {
            log.error("Failed to add ArtistRegistry Gar-File:" + e);
            throw new org.wso2.carbon.registry.api.RegistryException("Failed to add ArtistRegistry Gar-File:" + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "GAR file uploader - OriginalWSDL",
            dependsOnMethods = "testAddArtistRegistryGarFile")
    public void testAddOriginalWSDLGarFile() throws FileNotFoundException, org.wso2.carbon.registry.api.RegistryException {
        String filePath = resourcePath + "original-wsdl.gar";
        String service_path1 = "/_system/governance/trunk/services/com/konakart/ws/1.0.0/KKWSEngIfService";
        String service_path2 = "/_system/governance/trunk/services/net/ecubicle/www/1.0.0/YouTubeDownloader";
        String wsdl_path1 = "/_system/governance/trunk/wsdls/com/konakart/ws/1.0.0/KKWebServiceEng.wsdl";
        String wsdl_path2 = "/_system/governance/trunk/wsdls/net/ecubicle/www/1.0.0/youtubedownloader.asmx.wsdl";
        try {
            //Create Resource
            Resource r9 = registry.newResource();
            //create an Input Stream
            InputStream is1 = new BufferedInputStream(new FileInputStream(filePath));
            r9.setContentStream(is1);
            r9.setMediaType("application/vnd.wso2.governance-archive");
            registry.put("/a1/a2/a3/r9", r9);
            //Assert Schema exists
            assertTrue(registry.resourceExists(service_path1), "Service1 Exists1 :");
            assertTrue(registry.resourceExists(service_path2), "Service2 Exists1 :");
            assertTrue(registry.resourceExists(wsdl_path1), "WSDL1 Exists2 :");
            assertTrue(registry.resourceExists(wsdl_path2), "WSDL2 Exists2 :");
            //Delete Resources
            deleteResources(service_path1);
            deleteResources(service_path2);
            deleteResources(wsdl_path1);
            deleteResources(wsdl_path2);
            deleteResources("/a1");
            //Assert Resources have been properly deleted
            assertFalse(registry.resourceExists(service_path1), "Service1 Deleted :");
            assertFalse(registry.resourceExists(service_path2), "Service2 Deleted :");
            assertFalse(registry.resourceExists(wsdl_path1), "WSDL1 Deleted :");
            assertFalse(registry.resourceExists(wsdl_path2), "WSDL2 Deleted :");
            log.info("GarFileImportServiceTestClient testAddOriginalWSDLGarFile()- Passed");
        } catch(FileNotFoundException e) {
            log.error("Failed to add OriginalWSDL Gar-File:" + e);
            throw new FileNotFoundException("Failed to add OriginalWSDL Gar-File:" + e);
        } catch(org.wso2.carbon.registry.api.RegistryException e) {
            log.error("Failed to add OriginalWSDL Gar-File:" + e);
            throw new org.wso2.carbon.registry.api.RegistryException("Failed to add OriginalWSDL Gar-File:" + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "GAR file uploader - HomogeneousNamespa",
            dependsOnMethods = "testAddOriginalWSDLGarFile")
    public void testAddHomogeneousNamespaceGarFile() throws FileNotFoundException, org.wso2.carbon.registry.api.RegistryException {
        String filePath = resourcePath + "Homogeneous-Namespace.gar";
        String schema_path1 = "/_system/governance/trunk/schemas/org/company/www/1.0.0/Company.xsd";
        String schema_path2 = "/_system/governance/trunk/schemas/org/company/www/1.0.0/Person.xsd";
        String schema_path3 = "/_system/governance/trunk/schemas/org/company/www/1.0.0/Product.xsd";
        try {
            //Create Resource
            Resource r10 = registry.newResource();
            //create an Input Stream
            InputStream is1 = new BufferedInputStream(new FileInputStream(filePath));
            r10.setContentStream(is1);
            r10.setMediaType("application/vnd.wso2.governance-archive");
            registry.put("/a1/a2/a3/r10", r10);
            //Assert Schema exists
            assertTrue(registry.resourceExists(schema_path1), "Schema1 Exists :");
            assertTrue(registry.resourceExists(schema_path2), "Schema1 Exists :");
            assertTrue(registry.resourceExists(schema_path3), "Schema1 Exists :");
            //Delete Resources
            deleteResources(schema_path1);
            deleteResources(schema_path2);
            deleteResources(schema_path3);
            deleteResources("/a1");
            //Assert Resources have been properly deleted
            assertFalse(registry.resourceExists(schema_path1), "Schema1 Deleted :");
            assertFalse(registry.resourceExists(schema_path2), "Schema2 Deleted :");
            assertFalse(registry.resourceExists(schema_path3), "Schema3 Deleted :");
            log.info("GarFileImportServiceTestClient testAddHomogeneousNamespaceGarFile()- Passed");
        } catch(FileNotFoundException e) {
            log.error("Failed to add HomogeneousNamespace Gar-File:" + e);
            throw new FileNotFoundException("Failed to add HomogeneousNamespace Gar-File:" + e);
        } catch(org.wso2.carbon.registry.api.RegistryException e) {
            log.error("Failed to add HomogeneousNamespace Gar-File:" + e);
            throw new org.wso2.carbon.registry.api.RegistryException("Failed to add HomogeneousNamespace Gar-File:" + e);
        }
    }

    @AfterClass(alwaysRun = true)
    private void removeResource() throws RegistryException {
        try {
            deleteResources("/_system/governance/trunk/services");
            deleteResources("/_system/governance/trunk/wsdls");
            deleteResources("/_system/governance/trunk/schemas");
            deleteResources("/_system/governance/trunk/endpoints");
            deleteResources("/a1");
        } catch(RegistryException e) {
            log.error("Failed to Remove Resource :" + e);
            throw new RegistryException("Failed to Remove Resource :" + e);
        }
    }

    public void deleteResources(String resourceName) throws RegistryException {
        try {
            if(registry.resourceExists(resourceName)) {
                registry.delete(resourceName);
            }
        } catch(RegistryException e) {
            log.error("Failed to Delete Resource :" + e);
            throw new RegistryException("Failed to Delete Resource :" + e);
        }
    }
}
