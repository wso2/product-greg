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

package org.wso2.carbon.registry.ws.client.test.general.old;

import org.apache.axis2.AxisFault;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.registry.app.RemoteRegistry;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import java.util.List;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;


/**
 * A test case which tests registry association operation
 */
public class TestAssociationWSTestCase {

    private WSRegistryServiceClient registry;

    @BeforeClass(groups = {"wso2.greg"})
    public void initTest() throws RegistryException, AxisFault {
        int userId = 0;
        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        registry = registryProviderUtil.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);
    }

    @Test(groups = {"wso2.greg"})
    public void addAssociationToResource() throws Exception {

        Resource r2 = registry.newResource();
        String path = "/testk12/testa/testbsp/test.txt";
        r2.setContent(new String("this is the content").getBytes());
        r2.setDescription("this is test desc");
        r2.setMediaType("plain/text");
        r2.setProperty("test2", "value2");

        registry.put(path, r2);
        registry.addAssociation(path, "/vtr2121/test", "testasstype1");
        registry.addAssociation(path, "/vtr2122/test", "testasstype2");
        registry.addAssociation(path, "/vtr2123/test", "testasstype3");


        assertTrue(associationPathExists(path, "/vtr2121/test"), "association Destination path not exist");
        assertTrue(associationPathExists(path, "/vtr2122/test"), "association Destination path not exist");
        assertTrue(associationPathExists(path, "/vtr2123/test"), "association Destination path not exist");


        assertTrue(associationTypeExists(path, "testasstype1"), "association Type not exist");
        assertTrue(associationTypeExists(path, "testasstype2"), "association Type not exist");
        assertTrue(associationTypeExists(path, "testasstype3"), "association Type not exist");

        assertTrue(associationSourcepathExists(path, path), "association Source path not exist");
        assertTrue(associationSourcepathExists(path, path), "association Source path not exist");
        assertTrue(associationSourcepathExists(path, path), "association Source path not exist");

    }

    @Test(groups = {"wso2.greg"})
    public void addAssociationToCollection() throws Exception {

        Resource r2 = registry.newCollection();
        String path = "/assocol1/assocol2/assoclo3";
        r2.setDescription("this is test desc");
        r2.setMediaType("plain/text");
        r2.setProperty("test2", "value2");

        registry.put(path, r2);
        registry.addAssociation(path, "/vtr2121/test", "testasstype1");
        registry.addAssociation(path, "/vtr2122/test", "testasstype2");
        registry.addAssociation(path, "/vtr2123/test", "testasstype3");


        assertTrue(associationPathExists(path, "/vtr2121/test"), "association Destination path not exist");
        assertTrue(associationPathExists(path, "/vtr2122/test"), "association Destination path not exist");
        assertTrue(associationPathExists(path, "/vtr2123/test"), "association Destination path not exist");


        assertTrue(associationTypeExists(path, "testasstype1"), "association Type not exist");
        assertTrue(associationTypeExists(path, "testasstype2"), "association Type not exist");
        assertTrue(associationTypeExists(path, "testasstype3"), "association Type not exist");

        assertTrue(associationSourcepathExists(path, path), "association Source path not exist");
        assertTrue(associationSourcepathExists(path, path), "association Source path not exist");
        assertTrue(associationSourcepathExists(path, path), "association Source path not exist");

    }

    @Test(groups = {"wso2.greg"})
    public void addAssociationToRoot() throws Exception {


        String path = "/";
        registry.addAssociation(path, "/vtr2121/test", "testasstype1");
        registry.addAssociation(path, "/vtr2122/test", "testasstype2");
        registry.addAssociation(path, "/vtr2123/test", "testasstype3");

        assertTrue(associationPathExists(path, "/vtr2121/test"), "association Destination path not exist");
        assertTrue(associationPathExists(path, "/vtr2122/test"), "association Destination path not exist");
        assertTrue(associationPathExists(path, "/vtr2123/test"), "association Destination path not exist");

        assertTrue(associationTypeExists(path, "testasstype1"), "association Type not exist");
        assertTrue(associationTypeExists(path, "testasstype2"), "association Type not exist");
        assertTrue(associationTypeExists(path, "testasstype3"), "association Type not exist");

        assertTrue(associationSourcepathExists(path, path), "association Source path not exist");
        assertTrue(associationSourcepathExists(path, path), "association Source path not exist");
        assertTrue(associationSourcepathExists(path, path), "association Source path not exist");

    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"addAssociationToResource"})
    public void getResourceAssociation() throws Exception {

        Resource r2 = registry.newResource();
        String path = "/testk1234/testa/testbsp/test.txt";
        r2.setContent(new String("this is the content").getBytes());
        r2.setDescription("this is test desc");
        r2.setMediaType("plain/text");
        r2.setProperty("test2", "value2");

        registry.addAssociation(path, "/vtr2121/test", "testasstype1");
        registry.addAssociation(path, "/vtr2122/test", "testasstype2");
        registry.addAssociation(path, "/vtr2123/test", "testasstype3");


        assertTrue(getAssocitionbyDestinationByType(path, "testasstype1", "/vtr2121/test"),
                   "association Destination path not exist");
        assertTrue(getAssocitionbyDestinationByType(path, "testasstype2", "/vtr2122/test"),
                   "association Destination path not exist");
        assertTrue(getAssocitionbyDestinationByType(path, "testasstype3", "/vtr2123/test"),
                   "association Destination path not exist");


        assertTrue(getAssocitionbyType(path, "testasstype1"), "association Type not exist");
        assertTrue(getAssocitionbyType(path, "testasstype2"), "association Type not exist");
        assertTrue(getAssocitionbyType(path, "testasstype3"), "association Type not exist");

        assertTrue(getAssocitionbySourceByType(path, "testasstype1"), "association Source path not exist");
        assertTrue(getAssocitionbySourceByType(path, "testasstype2"), "association Source path not exist");
        assertTrue(getAssocitionbySourceByType(path, "testasstype3"), "association Source path not exist");

    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"addAssociationToCollection"})
    public void getCollectionAssociation() throws Exception {

        Resource r2 = registry.newCollection();
        String path = "/getcol1/getcol2/getcol3";
        r2.setDescription("this is test desc");
        r2.setProperty("test2", "value2");

        registry.put(path, r2);
        registry.addAssociation(path, "/vtr2121/test", "testasstype1");
        registry.addAssociation(path, "/vtr2122/test", "testasstype2");
        registry.addAssociation(path, "/vtr2123/test", "testasstype3");

        assertTrue(getAssocitionbyDestinationByType(path, "testasstype1", "/vtr2121/test"),
                   "association Destination path not exist");
        assertTrue(getAssocitionbyDestinationByType(path, "testasstype2", "/vtr2122/test"),
                   "association Destination path not exist");
        assertTrue(getAssocitionbyDestinationByType(path, "testasstype3", "/vtr2123/test"),
                   "association Destination path not exist");


        assertTrue(getAssocitionbyType(path, "testasstype1"), "association Type not exist");
        assertTrue(getAssocitionbyType(path, "testasstype2"), "association Type not exist");
        assertTrue(getAssocitionbyType(path, "testasstype3"), "association Type not exist");

        assertTrue(getAssocitionbySourceByType(path, "testasstype1"), "association Source path not exist");
        assertTrue(getAssocitionbySourceByType(path, "testasstype2"), "association Source path not exist");
        assertTrue(getAssocitionbySourceByType(path, "testasstype3"), "association Source path not exist");

    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"addAssociationToResource"})
    public void removeResourceAssociation() throws Exception {

        Resource r2 = registry.newResource();
        String path = "/testk123456/testa/testbsp/test.txt";
        r2.setContent(new String("this is the content").getBytes());
        r2.setDescription("this is test desc");
        r2.setMediaType("plain/text");
        r2.setProperty("test2", "value2");

        registry.put(path, r2);
        registry.addAssociation(path, "/vtr2121/test", "testasstype1");
        registry.addAssociation(path, "/vtr2122/test", "testasstype2");
        registry.addAssociation(path, "/vtr2123/test", "testasstype3");


        assertTrue(getAssocitionbyDestinationByType(path, "testasstype1", "/vtr2121/test"),
                   "association Destination path not exist");
        assertTrue(getAssocitionbyDestinationByType(path, "testasstype2", "/vtr2122/test"),
                   "association Destination path not exist");
        assertTrue(getAssocitionbyDestinationByType(path, "testasstype3", "/vtr2123/test"),
                   "association Destination path not exist");


        assertTrue(getAssocitionbyType(path, "testasstype1"), "association Type not exist");
        assertTrue(getAssocitionbyType(path, "testasstype2"), "association Type not exist");
        assertTrue(getAssocitionbyType(path, "testasstype3"), "association Type not exist");

        assertTrue(getAssocitionbySourceByType(path, "testasstype1"), "association Source path not exist");
        assertTrue(getAssocitionbySourceByType(path, "testasstype2"), "association Source path not exist");
        assertTrue(getAssocitionbySourceByType(path, "testasstype3"), "association Source path not exist");

        registry.removeAssociation(path, "/vtr2121/test", "testasstype1");
        registry.removeAssociation(path, "/vtr2122/test", "testasstype2");
        registry.removeAssociation(path, "/vtr2123/test", "testasstype3");


        assertFalse(getAssocitionbyDestinationByType(path, "testasstype1", "/vtr2121/test"),
                    "association Destination path exists");
        assertFalse(getAssocitionbyDestinationByType(path, "testasstype2", "/vtr2122/test"),
                    "association Destination path exists");
        assertFalse(getAssocitionbyDestinationByType(path, "testasstype3", "/vtr2123/test"),
                    "association Destination path exists");

        assertFalse(getAssocitionbyType(path, "testasstype1"), "association Type not exist");
        assertFalse(getAssocitionbyType(path, "testasstype2"), "association Type not exist");
        assertFalse(getAssocitionbyType(path, "testasstype3"), "association Type not exist");

        assertFalse(getAssocitionbySourceByType(path, "testasstype1"), "association Source path not exist");
        assertFalse(getAssocitionbySourceByType(path, "testasstype2"), "association Source path not exist");
        assertFalse(getAssocitionbySourceByType(path, "testasstype3"), "association Source path not exist");
    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"addAssociationToCollection"})
    public void removeCollectionAssociation() throws Exception {

        Resource r2 = registry.newCollection();
        String path = "/assoColremove1/assoColremove2/assoColremove3";
        r2.setDescription("this is test desc");
        r2.setMediaType("plain/text");
        r2.setProperty("test2", "value2");

        registry.put(path, r2);
        registry.addAssociation(path, "/vtr2121/test", "testasstype1");
        registry.addAssociation(path, "/vtr2122/test", "testasstype2");
        registry.addAssociation(path, "/vtr2123/test", "testasstype3");


        assertTrue(getAssocitionbyDestinationByType(path, "testasstype1", "/vtr2121/test"),
                   "association Destination path not exist");
        assertTrue(getAssocitionbyDestinationByType(path, "testasstype2", "/vtr2122/test"),
                   "association Destination path not exist");
        assertTrue(getAssocitionbyDestinationByType(path, "testasstype3", "/vtr2123/test"),
                   "association Destination path not exist");


        assertTrue(getAssocitionbyType(path, "testasstype1"), "association Type not exist");
        assertTrue(getAssocitionbyType(path, "testasstype2"), "association Type not exist");
        assertTrue(getAssocitionbyType(path, "testasstype3"), "association Type not exist");

        assertTrue(getAssocitionbySourceByType(path, "testasstype1"), "association Source path not exist");
        assertTrue(getAssocitionbySourceByType(path, "testasstype2"), "association Source path not exist");
        assertTrue(getAssocitionbySourceByType(path, "testasstype3"), "association Source path not exist");

        registry.removeAssociation(path, "/vtr2121/test", "testasstype1");
        registry.removeAssociation(path, "/vtr2122/test", "testasstype2");
        registry.removeAssociation(path, "/vtr2123/test", "testasstype3");


        assertFalse(getAssocitionbyDestinationByType(path, "testasstype1", "/vtr2121/test"),
                    "association Destination path exists");
        assertFalse(getAssocitionbyDestinationByType(path, "testasstype2", "/vtr2122/test"),
                    "association Destination path exists");
        assertFalse(getAssocitionbyDestinationByType(path, "testasstype3", "/vtr2123/test"),
                    "association Destination path exists");

        assertFalse(getAssocitionbyType(path, "testasstype1"), "association Type not exist");
        assertFalse(getAssocitionbyType(path, "testasstype2"), "association Type not exist");
        assertFalse(getAssocitionbyType(path, "testasstype3"), "association Type not exist");

        assertFalse(getAssocitionbySourceByType(path, "testasstype1"), "association Source path not exist");
        assertFalse(getAssocitionbySourceByType(path, "testasstype2"), "association Source path not exist");
        assertFalse(getAssocitionbySourceByType(path, "testasstype3"), "association Source path not exist");

    }


    //All the following methods are used in the above methods. So no need to add them to the test suit.
    public static boolean resourceExists(RemoteRegistry registry, String fileName)
            throws Exception {
        boolean value;
        value = registry.resourceExists(fileName);
        return value;
    }

    public boolean associationPathExists(String path, String assoPath)
            throws Exception {
        Association association[] = registry.getAllAssociations(path);
        boolean value = false;

        for (Association anAssociation : association) {
            if (assoPath.equals(anAssociation.getDestinationPath())) {
                value = true;
            }
        }


        return value;
    }

    public boolean associationTypeExists(String path, String assoType) throws Exception {
        Association association[] = registry.getAllAssociations(path);
        boolean value = false;

        for (Association anAssociation : association) {
            anAssociation.getAssociationType();
            if (assoType.equals(anAssociation.getAssociationType())) {
                value = true;
            }
        }


        return value;
    }

    public boolean associationSourcepathExists(String path, String sourcePath) throws Exception {
        Association association[] = registry.getAllAssociations(path);
        boolean value = false;

        for (Association anAssociation : association) {
            anAssociation.getAssociationType();
            if (sourcePath.equals(anAssociation.getSourcePath())) {
                value = true;
            }
        }

        return value;
    }

    public boolean getAssocitionbyType(String path, String type) throws Exception {

        Association[] asso;
        asso = registry.getAssociations(path, type);

        boolean assoFound = false;
        if (asso == null) {
            return assoFound;
        }
        for (Association a2 : asso) {

            if (a2.getAssociationType().equals(type)) {
                assoFound = true;
                break;
            }
        }
        return assoFound;
    }

    public boolean getAssocitionbySourceByType(String path, String type) throws Exception {

        Association[] asso;
        asso = registry.getAssociations(path, type);

        boolean assoFound = false;
        if (asso == null) {
            return assoFound;
        }
        for (Association a2 : asso) {

            if (a2.getSourcePath().equals(path)) {
                assoFound = true;
                break;
            }
        }
        return assoFound;
    }

    public boolean getAssocitionbyDestinationByType(String path, String type,
                                                    String destinationPath) throws Exception {

        Association[] asso;
        asso = registry.getAssociations(path, type);


        boolean assoFound = false;

        if (asso == null) {
            return assoFound;
        }
        for (Association a2 : asso) {

            if (a2.getDestinationPath().equals(destinationPath)) {
                assoFound = true;
                break;
            }
        }
        return assoFound;
    }


    public boolean getProperty(String path, String key, String value) throws Exception {
        Resource r3 = registry.newResource();
        try {
            r3 = registry.get(path);
        } catch (Exception e) {
            fail((new StringBuilder()).append("Couldn't get file from the path :").append(path).toString());
        }
        List propertyValues = r3.getPropertyValues(key);
        Object valueName[] = propertyValues.toArray();
        boolean propertystatus;
        propertystatus = containsString(valueName, value);
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

    @AfterClass
    public void cleanup() throws RegistryException {
        registry.delete("/testk12");
        registry.delete("/assocol1");
        registry.delete("/assoColremove1");
        registry.delete("/getcol1");
        registry.delete("/testk123456");


    }
}
