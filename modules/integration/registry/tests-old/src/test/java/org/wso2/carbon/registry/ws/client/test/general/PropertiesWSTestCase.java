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

package org.wso2.carbon.registry.ws.client.test.general;


import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.registry.core.Resource;

import java.util.List;

import static org.testng.Assert.*;

/**
 * A test case which tests registry properties
 */
public class PropertiesWSTestCase extends TestSetup {


    @BeforeClass(groups = {"wso2.greg"})
    public void initTest() {
        super.init();
    }

    @Test(groups = {"wso2.greg"})
    public void rootLevelProperties() throws Exception {

        Resource root = registry.get("/");
        root.addProperty("p1", "v1");
        registry.put("/", root);

        Resource rootb = registry.get("/");
        assertEquals(rootb.getProperty("p1"), "v1", "Root should have a property named p1 with value v1");
    }

    @Test(groups = {"wso2.greg"})
    public void singleValuedProperties() throws Exception {

        Resource r2 = registry.newResource();
        r2.setContent("Some content for r2");
        r2.addProperty("p1", "p1v1");
        registry.put("/propTest/r2", r2);

        Resource r2b = registry.get("/propTest/r2");
        String p1Value = r2b.getProperty("p1");

        assertEquals(p1Value, "p1v1", "Property p1 of /propTest/r2 should contain the value p1v1");
    }

    @Test(groups = {"wso2.greg"})
    public void multiValuedProperties() throws Exception {

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
    public void removingProperties() throws Exception {

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
    public void removingMultivaluedProperties() throws Exception {

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
    public void editingMultivaluedProperties() throws Exception {

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

    // Hashtables do not allow null values. Since the property object is child class of hashtable the null tests on properties are invalid.
//    public void testNullValuedProperties() throws Exception {
//
//        Resource r2 = registry.newResource();
//        r2.setContent("Some content for r2");
//        r2.addProperty("p1", null);
//        registry.put("/propTest3/r2", r2);
//
//        Resource r2b = registry.get("/propTest3/r2");
//        String p1Value = r2b.getProperty("p1");
//
//        assertEquals("Property p1 of /propTest3/r2 should contain the value null",
//                p1Value, null);
//    }
//
//// Hashtables do not allow null values. Since the property object is child class of hashtable the null tests on properties are invalid.
//    public void testNullMultiValuedProperties() throws Exception {
//
//        Resource r1 = registry.newResource();
//        r1.setContent("Some content for r1");
//        r1.addProperty("p1", null);
//        r1.addProperty("p1", null);
//        registry.put("/propTest4/r1", r1);
//
//        Resource r1b = registry.get("/propTest4/r1");
//        List propValues = r1b.getPropertyValues("p1");
//
//        assertEquals("Property p1 of /propTest4/r1 should contain the value null",
//                propValues.get(0), null);
//
//        assertEquals("Property p1 of /propTest4/r1 should contain the value null",
//                propValues.get(1), null);
//    }
}
