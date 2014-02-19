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
package org.wso2.carbon.registry.governance.api.test.old;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.fileutils.FileManager;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.schema.SchemaFilter;
import org.wso2.carbon.governance.api.schema.SchemaManager;
import org.wso2.carbon.governance.api.schema.dataobjects.Schema;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import java.io.File;
import java.io.IOException;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * This class will test Schema Manager related Governance API methods
 */

public class SchemaManagerAPITestCase {

    private WSRegistryServiceClient wsRegistry;
    private RegistryProviderUtil registryProviderUtil;
    private Registry governance;
    private int userId = 1;
    UserInfo userInfo = UserListCsvReader.getUserInfo(userId);
    public static SchemaManager schemaManager;
    public static Schema schemaObj;
    public static Schema[] schemaArray;
    public String schemaName = "calculator.xsd";
    private static String resourcePath;

    @BeforeClass(alwaysRun = true)
    public void initializeAPIObject() throws RegistryException, AxisFault, InterruptedException {
        registryProviderUtil = new RegistryProviderUtil();
        wsRegistry = registryProviderUtil.getWSRegistry(userId,
                ProductConstant.GREG_SERVER_NAME);
        governance = registryProviderUtil.getGovernanceRegistry(wsRegistry, userId);
        schemaManager = new SchemaManager(governance);
        resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
                File.separator + "GREG";
        System.out.println(resourcePath);
    }

    @Test(groups = {"wso2.greg.api"}, description = "Testing newSchema API method")
    public void testNewSchemaUrl() throws GovernanceException {
        try {
            cleanSchema();
            schemaObj = schemaManager.newSchema("https://svn.wso2.org/repos/wso2/trunk/commons/qa/"
                    + "qa-artifacts/greg/xsd/calculator.xsd");
        } catch (GovernanceException e) {
            throw new GovernanceException("Error occurred while executing SchemaManager:newSchema with " +
                    "URL method" + e);
        }
    }

    @Test(groups = {"wso2.greg.api"}, dependsOnMethods = {"testNewSchemaUrl"}, description = "Testing " +
            "addSchema API method")
    public void testAddSchema() throws GovernanceException {
        try {
            schemaManager.addSchema(schemaObj);
        } catch (GovernanceException e) {
            throw new GovernanceException("Error occurred while executing SchemaManager:addSchema method" + e);
        }
    }

    @Test(groups = {"wso2.greg.api"}, dependsOnMethods = {"testAddSchema"}, description = "Testing " +
            "getAllSchemas API method")
    public void testGetAllSchema() throws GovernanceException {
        try {
            schemaArray = schemaManager.getAllSchemas();
            assertTrue(schemaArray.length > 0, "Error occurred while executing SchemaManager:" +
                    "getAllSchemas method");
        } catch (GovernanceException e) {
            throw new GovernanceException("Error occurred while executing SchemaManager:getAllSchemas method" + e);
        }
    }

    @Test(groups = {"wso2.greg.api"}, dependsOnMethods = {"testGetAllSchema"}, description = "Testing " +
            "getSchema API method")
    public void testGetSchema() throws GovernanceException {
        try {
            schemaObj = schemaManager.getSchema(schemaArray[0].getId());
            assertTrue(schemaObj.getQName().getLocalPart().equalsIgnoreCase(schemaName), "SchemaManager:" +
                    "getSchema API method not contain expected schema name");
        } catch (GovernanceException e) {
            throw new GovernanceException("Error occurred while executing SchemaManager:getSchema method" + e);
        }
    }

    @Test(groups = {"wso2.greg.api"}, dependsOnMethods = {"testGetSchema"}, description = "Testing " +
            "updateSchema API method")
    public void testUpdateSchema() throws GovernanceException {
        String lcName = "ServiceLifeCycle";
        try {
            schemaObj.attachLifecycle(lcName);
            schemaManager.updateSchema(schemaObj);
            Schema localSchema = schemaManager.getSchema(schemaObj.getId());
            assertTrue(localSchema.getLifecycleName().equalsIgnoreCase(lcName), "Updated schema doesn't " +
                    "have lifecycle Information.SchemaManager:updateSchema didn't work");
        } catch (GovernanceException e) {
            throw new GovernanceException("Error occurred while executing SchemaManager:updateSchema method" + e);
        }
    }

    @Test(groups = {"wso2.greg.api"}, description = "Testing FindSchema", dependsOnMethods = "testUpdateSchema")
    public void testFindService() throws GovernanceException {
        try {
            Schema[] schemaArray = schemaManager.findSchemas(new SchemaFilter() {
                public boolean matches(Schema schema) throws GovernanceException {
                    String name = schema.getQName().getLocalPart();
                    assertTrue(name.contains(schemaName), "Error occurred while executing " +
                            "findSchema API method");
                    return name.contains(schemaName);
                }
            }
            );
            assertTrue(schemaArray.length > 0, "Error occurred while executing findSchema API " +
                    "method");
        } catch (GovernanceException e) {
            throw new GovernanceException("Error occurred while executing WsdlManager:findSchemas method" + e);
        }
    }


//    Schema data object specific test cases

    @Test(groups = {"wso2.greg.api"}, description = "Testing getQName API method with schema object",
            dependsOnMethods = "testFindService")
    public void testGetQName() throws Exception {
        boolean isSchemaFound = false;
        Schema[] schema = schemaManager.getAllSchemas();
        try {
            for (Schema s : schema) {
                if (s.getQName().getLocalPart().equalsIgnoreCase(schemaName)) {
                    isSchemaFound = true;
                }
            }
            assertTrue(isSchemaFound, "getQName method prompt error while executing with schema object");
        } catch (Exception e) {
            throw new Exception("Error occurred while executing WsdlManager:getQName method" + e);
        }
    }

//    //https://wso2.org/jira/browse/REGISTRY-762
//    @Test(groups = {"wso2.greg.api"}, description = "Testing getUrl API method with schema object",
//          dependsOnMethods = "testGetQName", enabled = false)
//    public void testGetUrl() throws Exception {
//        boolean isSchemaFound = false;
//        Schema[] schema = schemaManager.getAllSchemas();
//        try {
//            for (Schema s : schema) {
//                if ((s.getUrl() != null)) {
//                    isSchemaFound = true;
//                }
//            }
//            assertTrue(isSchemaFound, "getUrl method prompt error while executing with schema object");
//        } catch (Exception e) {
//            throw new Exception("Error occurred while executing WsdlManager:getUrl method" + e);
//        }
//    }

    @Test(groups = {"wso2.greg.api"}, description = "Testing getSchemaElement API method with schema object",
            dependsOnMethods = "testGetQName")
    public void testGetSchemaElement() throws Exception {
        boolean isSchemaFound = false;
        Schema[] schema = schemaManager.getAllSchemas();
        try {
            for (Schema s : schema) {
                if (s.getQName().getLocalPart().equalsIgnoreCase(schemaName)) {
                    OMElement omElement = s.getSchemaElement();
                    if (omElement.toString().contains("http://charitha.org/")) {
                        isSchemaFound = true;
                    }
                }
            }
            assertTrue(isSchemaFound, "getSchemaElement method prompt error while executing with schema object");
        } catch (Exception e) {
            throw new Exception("Error occurred while executing getSchemaElement method" + e);
        }
    }


    @Test(groups = {"wso2.greg.api"}, description = "Testing newSchema API method with inline schema content",
            dependsOnMethods = "testGetSchemaElement")
    public void testNewSchemaInlineContent() throws GovernanceException, IOException {
        String schemaFileLocation = resourcePath + File.separator + "schema" + File.separator
                + "calculator.xsd";
        try {
            schemaObj = schemaManager.newSchema(FileManager.readFile(schemaFileLocation).getBytes());
            schemaManager.addSchema(schemaObj);
        } catch (GovernanceException e) {
            throw new GovernanceException("Exception thrown while executing newSchema API method with " +
                    "inline wsdl content" + e.getMessage());
        } catch (RegistryException e) {
            e.printStackTrace();
        }
        schemaObj = schemaManager.getSchema(schemaObj.getId());
        assertTrue(schemaObj.getQName().getNamespaceURI().contains("http://charitha.org/"), "Error" +
                " found in newSchema with inline schema content");
    }


    @Test(groups = {"wso2.greg.api"}, description = "Testing newSchema API method with inline schema " +
            "content with name value", dependsOnMethods = "testNewSchemaInlineContent")
    public void testNewSchemaInlineContentWithName() throws GovernanceException, IOException {
        String schemaFileLocation = resourcePath + File.separator + "schema" + File.separator
                + "calculator.xsd";
        try {
            schemaObj = schemaManager.newSchema(FileManager.readFile(schemaFileLocation).getBytes(),
                    "SampleSchemaContentWithName.xsd");
            schemaManager.addSchema(schemaObj);
        } catch (GovernanceException e) {
            throw new GovernanceException("Exception thrown while executing newSchema API method with " +
                    "inline wsdl content and name" + e.getMessage());
        } catch (RegistryException e) {
            e.printStackTrace();
        }
        schemaObj = schemaManager.getSchema(schemaObj.getId());
        assertTrue(schemaObj.getQName().getLocalPart().equalsIgnoreCase("SampleSchemaContentWithName.xsd"),
                "Error found in newSchema with inline schema content and name");
    }

    @Test(groups = {"wso2.greg.api"}, description = "Testing removeSchema API method",
            dependsOnMethods = "testNewSchemaInlineContentWithName")
    public void testRemoveSchema() throws GovernanceException {
        try {
            schemaManager.removeSchema(schemaObj.getId());
            schemaArray = schemaManager.getAllSchemas();
            for (Schema s : schemaArray) {
                assertFalse(s.getId().equalsIgnoreCase(schemaObj.getId()), "SchemaManager:removeSchema" +
                        " API method having error");
            }

        } catch (GovernanceException e) {
            throw new GovernanceException("Error occurred while executing SchemaManager:removeSchema method" + e);
        }
    }

    @AfterClass
    public void removeTestArtifacts() throws RegistryException {
        cleanSchema();

        for (String string : new String[]{"/trunk", "/branches", "/departments", "/organizations",
                "/project-groups", "/people", "/applications", "/processes", "/projects",
                "/test_suites", "/test_cases", "/test_harnesses", "/test_methods"}) {

            if (governance.resourceExists(string)) {
                governance.delete(string);
            }
        }

        governance = null;
        wsRegistry = null;
        registryProviderUtil = null;
        schemaManager = null;

    }

    private void cleanSchema() throws GovernanceException {
        Schema[] schemas = schemaManager.getAllSchemas();
        for (Schema s : schemas) {
            schemaManager.removeSchema(s.getId());
        }
    }


}
