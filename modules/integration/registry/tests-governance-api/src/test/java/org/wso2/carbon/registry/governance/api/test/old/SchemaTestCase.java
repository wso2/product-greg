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
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.schema.SchemaFilter;
import org.wso2.carbon.governance.api.schema.SchemaManager;
import org.wso2.carbon.governance.api.schema.dataobjects.Schema;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class SchemaTestCase {

    private Registry registry;
    private static final Log log = LogFactory.getLog(SchemaTestCase.class);
    private WSRegistryServiceClient wsRegistry;
    private RegistryProviderUtil registryProviderUtil;
    private int userId = 1;


    @BeforeClass(groups = {"wso2.greg"})
    public void initTest() throws RegistryException, AxisFault {
        registryProviderUtil = new RegistryProviderUtil();
        wsRegistry = registryProviderUtil.getWSRegistry(userId,
                ProductConstant.GREG_SERVER_NAME);
        registry = registryProviderUtil.getGovernanceRegistry(wsRegistry, userId);
    }

    @Test(groups = {"wso2.greg"})
    public void testAddSchema() throws Exception {
        initTest();

        log.info("############## testAddSchema started ...###################");
        SchemaManager schemaManager = new SchemaManager(registry);

        Schema schema = schemaManager.newSchema("http://svn.wso2.org/repos/wso2/carbon/platform/trunk/components/governance/org.wso2.carbon.governance.api/src/test/resources/test-resources/xsd/purchasing.xsd");
        schema.addAttribute("creator", "it is me");
        schema.addAttribute("version", "0.01");
        schemaManager.addSchema(schema);

        Schema newSchema = schemaManager.getSchema(schema.getId());
        Assert.assertEquals(newSchema.getSchemaElement().toString(), schema.getSchemaElement().toString());
        Assert.assertEquals(newSchema.getAttribute("creator"), "it is me");
        Assert.assertEquals(newSchema.getAttribute("version"), "0.01");

        // change the target namespace and check
        String oldSchemaPath = newSchema.getPath();
        Assert.assertEquals(oldSchemaPath, "/trunk/schemas/org/bar/purchasing/purchasing.xsd");
        Assert.assertTrue(registry.resourceExists("/trunk/schemas/org/bar/purchasing/purchasing.xsd"));

        OMElement schemaElement = newSchema.getSchemaElement();
        schemaElement.addAttribute("targetNamespace", "http://ww2.wso2.org/schema-test", null);
        schemaElement.declareNamespace("http://ww2.wso2.org/schema-test", "tns");
        schemaManager.updateSchema(newSchema);
        doSleep();

        Assert.assertEquals(newSchema.getPath(), "/trunk/schemas/org/wso2/ww2/schema_test/purchasing.xsd");
        Assert.assertFalse(registry.resourceExists("/trunk/test_schemas/org/bar/purchasing.xsd"));

        // doing an update without changing anything.
        schemaManager.updateSchema(newSchema);
        doSleep();

        Assert.assertEquals(newSchema.getPath(), "/trunk/schemas/org/wso2/ww2/schema_test/purchasing.xsd");
        Assert.assertEquals(newSchema.getAttribute("version"), "0.01");

        newSchema = schemaManager.getSchema(schema.getId());
        Assert.assertEquals(newSchema.getAttribute("creator"), "it is me");
        Assert.assertEquals(newSchema.getAttribute("version"), "0.01");

        Schema[] schemas = schemaManager.findSchemas(new SchemaFilter() {
            public boolean matches(Schema schema) throws GovernanceException {
                if (schema.getAttribute("version") != null && schema.getAttribute("version").equals("0.01")) {
                    log.info("########Schema name" + schema.getQName().toString() + "  schemaID : " + schema.getId());
                    return true;
                }
                return false;
            }
        });
        doSleep();
        log.info("########Schema Len:" + schemas.length);
        Assert.assertEquals(schemas.length, 1);
        Assert.assertEquals(newSchema.getId(), schemas[0].getId());

        // deleting the schema
        schemaManager.removeSchema(newSchema.getId());
        Schema deletedSchema = schemaManager.getSchema(newSchema.getId());
        Assert.assertNull(deletedSchema);
    }

    @Test(groups = {"wso2.greg"})
    public void testAddSchemaFromContent() throws Exception {
        SchemaManager schemaManager = new SchemaManager(registry);
        byte[] bytes = null;
        try {
            InputStream inputStream = new URL("http://svn.wso2.org/repos/wso2/carbon/platform/trunk/components/governance/org.wso2.carbon.governance.api/src/test/resources/test-resources/xsd/purchasing.xsd").openStream();
            try {
                bytes = IOUtils.toByteArray(inputStream);
            } finally {
                inputStream.close();
            }
        } catch (IOException e) {
            Assert.fail("Unable to read WSDL content");
        }
        Schema schema = schemaManager.newSchema(bytes, "newPurchasing.xsd");
        schema.addAttribute("creator", "it is me");
        schema.addAttribute("version", "0.01");
        schemaManager.addSchema(schema);

        Schema newSchema = schemaManager.getSchema(schema.getId());
        Assert.assertEquals(newSchema.getSchemaElement().toString(),
                schema.getSchemaElement().toString());
        Assert.assertEquals(newSchema.getAttribute("creator"), "it is me");
        Assert.assertEquals(newSchema.getAttribute("version"), "0.01");

        // change the target namespace and check
        String oldSchemaPath = newSchema.getPath();
        Assert.assertEquals(oldSchemaPath, "/trunk/schemas/org/bar/purchasing/newPurchasing.xsd");
        Assert.assertTrue(registry.resourceExists("/trunk/schemas/org/bar/purchasing/newPurchasing.xsd"));
    }

    @Test(groups = {"wso2.greg"})
    public void testAddSchemaFromContentNoName() throws Exception {
        SchemaManager schemaManager = new SchemaManager(registry);
        byte[] bytes = null;
        try {
            InputStream inputStream = new URL("http://svn.wso2.org/repos/wso2/carbon/platform/trunk/components/governance/org.wso2.carbon.governance.api/src/test/resources/test-resources/xsd/purchasing.xsd").openStream();
            try {
                bytes = IOUtils.toByteArray(inputStream);
            } finally {
                inputStream.close();
            }
        } catch (IOException e) {
            Assert.fail("Unable to read WSDL content");
        }
        Schema schema = schemaManager.newSchema(bytes);
        schema.addAttribute("creator", "it is me");
        schema.addAttribute("version", "0.01");
        schemaManager.addSchema(schema);

        Schema newSchema = schemaManager.getSchema(schema.getId());
        Assert.assertEquals(newSchema.getSchemaElement().toString(),
                schema.getSchemaElement().toString());
        Assert.assertEquals(newSchema.getAttribute("creator"), "it is me");
        Assert.assertEquals(newSchema.getAttribute("version"), "0.01");

        // change the target namespace and check
        String oldSchemaPath = newSchema.getPath();
        Assert.assertEquals(oldSchemaPath, "/trunk/schemas/org/bar/purchasing/" + schema.getId() + ".xsd");
        Assert.assertTrue(
                registry.resourceExists("/trunk/schemas/org/bar/purchasing/" + schema.getId() + ".xsd"));
    }

    @AfterClass
    public void cleanArtifacts() throws RegistryException {
        for (String string : new String[]{"/trunk", "/branches", "/departments", "/organizations",
                "/project-groups", "/people", "/applications", "/processes", "/projects",
                "/test_suites", "/test_cases", "/test_harnesses", "/test_methods"}) {

            if (registry.resourceExists(string)) {
                registry.delete(string);
            }
        }
        registry = null;
        wsRegistry = null;
        registryProviderUtil = null;
    }

    private void doSleep() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
        }
    }
}
