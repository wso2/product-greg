/*
 * Copyright 2004,2005 The Apache Software Foundation.
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

package org.wso2.carbon.registry.app2.test;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.registry.app.RemoteRegistry;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import java.util.List;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * A test case which tests registry WSDL import
 */

public class WsdlImportTestCase extends GREGIntegrationBaseTest{
    public RemoteRegistry registry;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception{

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        registry = new RegistryProviderUtil().getRemoteRegistry(automationContext);
    }


    @Test(groups = {"wso2.greg"})
    public void WsdlimportTest() throws RegistryException {

        String url =
                "https://svn.wso2.org/repos/wso2/trunk/commons/qa/qa-artifacts/greg/xsd/complextypedoclit.wsdl";
        Resource r1 = registry.newResource();
        r1.setDescription("WSDL imported from url");
        r1.setMediaType("application/wsdl+xml");
        String path = "/_system/governance/trunk/wsdls/complextypedoclit.wsdl";

        registry.importResource(path, url, r1);

        String wsdlPath = "/_system/governance/trunk/wsdls/com/foo/1.0.0/complextypedoclit.wsdl";

        assertTrue(resourceExists(registry, wsdlPath), "ComplexDataTypesRpcLit.svc.wsdl");
        String servicePath = "/_system/governance/trunk/services/com/foo/1.0.0/BizService";
        assertTrue(resourceExists(registry, servicePath), "ComplexDataTypesRpcLitService is not available");


        String schemaLocation0 = "/_system/governance/trunk/schemas/org/datacontract/schemas/_2004/_07/system/1.0.0/test_a.xsd";
        String schemaLocation1 = "/_system/governance/trunk/schemas/com/microsoft/schemas/_2003/_10/serialization/1.0.0/test_b.xsd";
        String schemaLocation2 = "/_system/governance/trunk/schemas/org/tempuri/1.0.0/test_c.xsd";
        String schemaLocation3 = "/_system/governance/trunk/schemas/com/microsoft/schemas/_2003/_12/serialization/1.0.0/test_d.xsd";

        assertTrue(resourceExists(registry, schemaLocation0), "ComplexDataTypesRpcLit.xsd not found");
        assertTrue(resourceExists(registry, schemaLocation1), "ComplexDataTypesRpcLit1.xsd not found");
        assertTrue(resourceExists(registry, schemaLocation2), "ComplexDataTypesRpcLit2.xsd not found");
        assertTrue(resourceExists(registry, schemaLocation3), "ComplexDataTypesRpcLit3.xsd not found");

        String endPointPath = "/_system/governance/trunk/endpoints/ep-localhost.services-BizService";
        assertTrue(resourceExists(registry, endPointPath), "ep-ComplexDataTypesRpcLit-svc endpoint not found");

        assertTrue(associationPathExists(wsdlPath, servicePath), "association Destination path not exist");

        assertTrue(associationPathExists(wsdlPath, endPointPath), "association Destination path not exist");

        assertTrue(associationPathExists(wsdlPath, schemaLocation0), "association Destination path not exist");

        assertTrue(associationPathExists(wsdlPath, schemaLocation1), "association Destination path not exist");

        assertTrue(associationPathExists(wsdlPath, schemaLocation2), "association Destination path not exist");

        assertTrue(associationPathExists(wsdlPath, schemaLocation3), "association Destination path not exist");

        /*check wsdl properties*/

        Resource r2b = registry.get(wsdlPath);


        /*check for enpoint dependencies*/
        assertTrue(associationPathExists(endPointPath, wsdlPath),
                   "association Destination path not exist");
        assertTrue(associationPathExists(endPointPath, servicePath),
                   "association Destination path not exist");

        /*check for xsd dependencies*/

        assertTrue(associationPathExists(schemaLocation0, wsdlPath),
                   "association Destination path not exist");

        assertTrue(associationPathExists(schemaLocation3, wsdlPath), "association Destination path not exist");
    }

    public static boolean resourceExists(RemoteRegistry registry, String fileName)
            throws RegistryException {
        boolean value = registry.resourceExists(fileName);
        return value;
    }

    public boolean associationPathExists(String path, String assoPath)
            throws RegistryException {
        Association association[] = registry.getAllAssociations(path);
        boolean value = false;

        for (int i = 0; i < association.length; i++) {
            //System.out.println(association[i].getDestinationPath());
            if (assoPath.equals(association[i].getDestinationPath())) {
                value = true;
            }
        }


        return value;
    }

    public boolean associationExists(String path, String pathValue)
            throws RegistryException {
        Association association[] = registry.getAllAssociations(path);
        boolean value = false;
        for (int i = 0; i < association.length; i++) {
            if (pathValue.equals(association[i].getDestinationPath())) {
                value = true;
            }
        }

        return value;
    }

    public boolean associationNotExists(String path) throws RegistryException {
        Association association[] = registry.getAllAssociations(path);
        boolean value = true;
        if (association.length > 0) {
            value = false;
        }
        return value;
    }

    public boolean getProperty(String path, String key, String value) throws RegistryException {
        Resource r3 = registry.newResource();
        try {
            r3 = registry.get(path);
        } catch (RegistryException e) {
            fail((new StringBuilder()).append("Couldn't get file from the path :").append(path).toString());
        }
        List propertyValues = r3.getPropertyValues(key);
        Object valueName[] = propertyValues.toArray();
        boolean propertystatus = containsString(valueName, value);
        return propertystatus;
    }

    private boolean containsString(Object[] array, String value) {
        boolean found = false;
        for (Object anArray : array) {
            String s = anArray.toString();
            if (s.startsWith(value)) {
                found = true;
                break;
            }
        }
        return found;
    }

    @AfterClass(alwaysRun = true)
    public void cleanArtifact() throws RegistryException {
        registry = null;
    }
}
