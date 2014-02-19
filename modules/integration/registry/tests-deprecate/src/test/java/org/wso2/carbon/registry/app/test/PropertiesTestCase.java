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

package org.wso2.carbon.registry.app.test;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.integration.core.TestTemplate;
import org.wso2.carbon.registry.app.RemoteRegistry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import static org.testng.Assert.*;

import java.util.List;

/**
 * A test case which tests registry properties
 */
public class PropertiesTestCase {
    public RemoteRegistry registry;

    @BeforeClass(groups = {"wso2.greg"})
    public void init() {
        InitializeAPI initializeAPI = new InitializeAPI();
        registry = initializeAPI.getRegistry(FrameworkSettings.CARBON_HOME, FrameworkSettings.HTTPS_PORT, FrameworkSettings.HTTP_PORT);
    }

    @Test(groups = {"wso2.greg"})
    public void RootLevelPropertiesTest() throws RegistryException {

        Resource root = registry.get("/");
        root.addProperty("p1", "v1");
        registry.put("/", root);

        Resource rootb = registry.get("/");
        assertEquals(rootb.getProperty("p1"), "v1", "Root should have a property named p1 with value v1");
    }

    @Test(groups = {"wso2.greg"})
    public void SingleValuedPropertiesTest() throws RegistryException {

        Resource r2 = registry.newResource();
        r2.setContent("Some content for r2");
        r2.addProperty("p1", "p1v1");
        registry.put("/propTest/r2", r2);

        Resource r2b = registry.get("/propTest/r2");
        String p1Value = r2b.getProperty("p1");

        assertEquals(p1Value, "p1v1", "Property p1 of /propTest/r2 should contain the value p1v1");
    }

    @Test(groups = {"wso2.greg"})
    public void MultiValuedPropertiesTest() throws RegistryException {

        Resource r1 = registry.newResource();
        r1.setContent("Some content for r1");
        r1.addProperty("p1", "p1v1");
        r1.addProperty("p1", "p1v2");
        registry.put("/propTest/r1", r1);

        Resource r1b = registry.get("/propTest/r1");
        List propValues = r1b.getPropertyValues("p1");

        assertTrue(propValues.contains("p1v1"), "Property p1 of /propTest/r1 should contain the value p1v1");
        //System.out.println(propValues.contains("p1v1"));

        assertTrue(propValues.contains("p1v2"), "Property p1 of /propTest/r1 should contain the value p1v2");
        //System.out.println(propValues.contains("p1v2"));
    }

    @Test(groups = {"wso2.greg"})
    public void NullValuedPropertiesTest() throws RegistryException {

        Resource r2 = registry.newResource();
        r2.setContent("Some content for r2");
        r2.addProperty("p1", null);
        registry.put("/propTest3/r2", r2);

        Resource r2b = registry.get("/propTest3/r2");
        String p1Value = r2b.getProperty("p1");

        assertNull(p1Value, "Property p1 of /propTest3/r2 should contain the value null");
    }

    @Test(groups = {"wso2.greg"})
    public void NullMultiValuedPropertiesTest() throws RegistryException {

        Resource r1 = registry.newResource();
        r1.setContent("Some content for r1");
        r1.addProperty("p1", null);
        r1.addProperty("p1", null);
        registry.put("/propTest4/r1", r1);

        Resource r1b = registry.get("/propTest4/r1");
        List propValues = r1b.getPropertyValues("p1");
        String value = "";
        try {
            value = (String) propValues.get(0);
        } catch (NullPointerException e) {
            assertTrue(true, "Property p1 of /propTest4/r1 should contain the value null");
        }
    }

    @Test(groups = {"wso2.greg"})
    public void RemovingPropertiesTest() throws RegistryException {

        Resource r1 = registry.newResource();
        r1.setContent("r1 content");
        r1.setProperty("p1", "v1");
        r1.setProperty("p2", "v2");
        registry.put("/props/t1/r1", r1);

        Resource r1e1 = registry.get("/props/t1/r1");
        r1e1.setContent("r1 content");
        r1e1.removeProperty("p1");
        registry.put("/props/t1/r1", r1e1);

        Resource r1e2 = registry.get("/props/t1/r1");

        assertNull(r1e2.getProperty("p1"), "Property is not removed.");
        assertNotNull(r1e2.getProperty("p2"), "Wrong property is removed.");
    }


    @Test(groups = {"wso2.greg"})
    public void RemovingMultivaluedPropertiesTest() throws RegistryException {

        Resource r1 = registry.newResource();
        r1.setContent("r1 content");
        r1.addProperty("p1", "v1");
        r1.addProperty("p1", "v2");
        registry.put("/props/t2/r1", r1);

        Resource r1e1 = registry.get("/props/t2/r1");
        r1e1.setContent("r1 content updated");
        r1e1.removePropertyValue("p1", "v1");
        registry.put("/props/t2/r1", r1e1);

        Resource r1e2 = registry.get("/props/t2/r1");

        assertFalse(r1e2.getPropertyValues("p1").contains("v1"), "Property is not removed.");
        assertTrue(r1e2.getPropertyValues("p1").contains("v2"), "Wrong property is removed.");
    }

    @Test(groups = {"wso2.greg"})
    public void EditingMultivaluedPropertiesTest() throws RegistryException {

        Resource r1 = registry.newResource();
        r1.setContent("r1 content");
        r1.addProperty("p1", "v1");
        r1.addProperty("p1", "v2");
        r1.setProperty("test", "value2");
        r1.setProperty("test2", "value2");
        registry.put("/props/t3/r1", r1);

        Resource r1e1 = registry.get("/props/t3/r1");
        r1e1.setContent("r1 content");
        r1e1.editPropertyValue("p1", "v1", "v3");
        List list = r1e1.getPropertyValues("/props/t3/r");

        registry.put("/props/t3/r1", r1e1);

        Resource r1e2 = registry.get("/props/t3/r1");


        assertFalse(r1e2.getPropertyValues("p1").contains("v1"), "Property is not edited.");
        assertTrue(r1e2.getPropertyValues("p1").contains("v3"), "Property is not edited.");
        assertTrue(r1e2.getPropertyValues("p1").contains("v2"), "Wrong property is removed.");


    }
}
