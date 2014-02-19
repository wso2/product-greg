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
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import static org.testng.Assert.*;

import java.util.List;

/**
 * A test case which tests registry associations
 */
public class TestAssociationTestCase {
    public RemoteRegistry registry;

    @BeforeClass(groups = {"wso2.greg"})
    public void init() {
        InitializeAPI initializeAPI = new InitializeAPI();
        registry = initializeAPI.getRegistry(FrameworkSettings.CARBON_HOME, FrameworkSettings.HTTPS_PORT, FrameworkSettings.HTTP_PORT);
    }

    @Test(groups = {"wso2.greg"})
    public void AddAssociationToResourceTest() throws RegistryException {

        Resource r2 = registry.newResource();
        String path = "/testk12/testa/testbsp/test.txt";
        r2.setContent(new String("this is the content").getBytes());
        r2.setDescription("this is test desc");
        r2.setMediaType("plain/text");
        r2.setProperty("test2", "value2");

        registry.put(path, r2);

        Resource dummy = registry.newResource();
        dummy.setContent("dummy".getBytes());
        registry.put("/vtr2121/test", dummy);
        registry.put("/vtr2122/test", dummy);
        registry.put("/vtr2123/test", dummy);

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
    public void AddAssociationToCollectionTest() throws RegistryException {

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
    public void AddAssociationToRootTest() throws RegistryException {

        Resource r2 = registry.newCollection();

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

    @Test(groups = {"wso2.greg"})
    public void GetResourceAssociationTest() throws RegistryException {

        Resource r2 = registry.newResource();
        String path = "/testk1234/testa/testbsp/test.txt";
        r2.setContent(new String("this is the content").getBytes());
        r2.setDescription("this is test desc");
        r2.setMediaType("plain/text");
        r2.setProperty("test2", "value2");
        registry.put(path, r2);

        registry.addAssociation(path, "/vtr2121/test", "testasstype1");
        registry.addAssociation(path, "/vtr2122/test", "testasstype2");
        registry.addAssociation(path, "/vtr2123/test", "testasstype3");


        assertTrue(getAssocitionbyDestinationByType(path, "testasstype1", "/vtr2121/test"), "association Destination path not exist");
        assertTrue(getAssocitionbyDestinationByType(path, "testasstype2", "/vtr2122/test"), "association Destination path not exist");
        assertTrue(getAssocitionbyDestinationByType(path, "testasstype3", "/vtr2123/test"), "association Destination path not exist");


        assertTrue(getAssocitionbyType(path, "testasstype1"), "association Type not exist");
        assertTrue(getAssocitionbyType(path, "testasstype2"), "association Type not exist");
        assertTrue(getAssocitionbyType(path, "testasstype3"), "association Type not exist");

        assertTrue(getAssocitionbySourceByType(path, "testasstype1"), "association Source path not exist");
        assertTrue(getAssocitionbySourceByType(path, "testasstype2"), "association Source path not exist");
        assertTrue(getAssocitionbySourceByType(path, "testasstype3"), "association Source path not exist");

    }

    @Test(groups = {"wso2.greg"})
    public void GetCollectionAssociationTest() throws RegistryException {

        Resource r2 = registry.newCollection();
        String path = "/getcol1/getcol2/getcol3";
        r2.setDescription("this is test desc");
        r2.setProperty("test2", "value2");

        registry.put(path, r2);
        registry.addAssociation(path, "/vtr2121/test", "testasstype1");
        registry.addAssociation(path, "/vtr2122/test", "testasstype2");
        registry.addAssociation(path, "/vtr2123/test", "testasstype3");

        assertTrue(getAssocitionbyDestinationByType(path, "testasstype1", "/vtr2121/test"), "association Destination path not exist");
        assertTrue(getAssocitionbyDestinationByType(path, "testasstype2", "/vtr2122/test"), "association Destination path not exist");
        assertTrue(getAssocitionbyDestinationByType(path, "testasstype3", "/vtr2123/test"), "association Destination path not exist");


        assertTrue(getAssocitionbyType(path, "testasstype1"), "association Type not exist");
        assertTrue(getAssocitionbyType(path, "testasstype2"), "association Type not exist");
        assertTrue(getAssocitionbyType(path, "testasstype3"), "association Type not exist");

        assertTrue(getAssocitionbySourceByType(path, "testasstype1"), "association Source path not exist");
        assertTrue(getAssocitionbySourceByType(path, "testasstype2"), "association Source path not exist");
        assertTrue(getAssocitionbySourceByType(path, "testasstype3"), "association Source path not exist");

    }

    @Test(groups = {"wso2.greg"})
    public void GetRootAssociationTest() throws RegistryException {

        Resource r2 = registry.newCollection();
        String path = "/";
        r2.setDescription("this is test desc");
        r2.setProperty("test2", "value2");

        registry.put(path, r2);
        registry.addAssociation(path, "/vtr21211/test", "testasstype1");
        registry.addAssociation(path, "/vtr21221/test", "testasstype2");
        registry.addAssociation(path, "/vtr21231/test", "testasstype3");

        assertTrue(getAssocitionbyDestinationByType(path, "testasstype1", "/vtr21211/test"), "association Destination path not exist");
        assertTrue(getAssocitionbyDestinationByType(path, "testasstype2", "/vtr21221/test"), "association Destination path not exist");
        assertTrue(getAssocitionbyDestinationByType(path, "testasstype3", "/vtr21231/test"), "association Destination path not exist");

        assertTrue(getAssocitionbyType(path, "testasstype1"), "association Type not exist");
        assertTrue(getAssocitionbyType(path, "testasstype2"), "association Type not exist");
        assertTrue(getAssocitionbyType(path, "testasstype3"), "association Type not exist");

        assertTrue(getAssocitionbySourceByType(path, "testasstype1"), "association Source path not exist");
        assertTrue(getAssocitionbySourceByType(path, "testasstype2"), "association Source path not exist");
        assertTrue(getAssocitionbySourceByType(path, "testasstype3"), "association Source path not exist");

    }

    @Test(groups = {"wso2.greg"})
    public void RemoveResourceAssociationTest() throws RegistryException {

        Resource r2 = registry.newResource();
        String path = "/testk1234/testa/testbsp/test.txt";
        r2.setContent(new String("this is the content").getBytes());
        r2.setDescription("this is test desc");
        r2.setMediaType("plain/text");
        r2.setProperty("test2", "value2");

        registry.put(path, r2);
        registry.addAssociation(path, "/vtr2121/test", "testasstype1");
        registry.addAssociation(path, "/vtr2122/test", "testasstype2");
        registry.addAssociation(path, "/vtr2123/test", "testasstype3");


        assertTrue(getAssocitionbyDestinationByType(path, "testasstype1", "/vtr2121/test"), "association Destination path not exist");
        assertTrue(getAssocitionbyDestinationByType(path, "testasstype2", "/vtr2122/test"), "association Destination path not exist");
        assertTrue(getAssocitionbyDestinationByType(path, "testasstype3", "/vtr2123/test"), "association Destination path not exist");


        assertTrue(getAssocitionbyType(path, "testasstype1"), "association Type not exist");
        assertTrue(getAssocitionbyType(path, "testasstype2"), "association Type not exist");
        assertTrue(getAssocitionbyType(path, "testasstype3"), "association Type not exist");

        assertTrue(getAssocitionbySourceByType(path, "testasstype1"), "association Source path not exist");
        assertTrue(getAssocitionbySourceByType(path, "testasstype2"), "association Source path not exist");
        assertTrue(getAssocitionbySourceByType(path, "testasstype3"), "association Source path not exist");

        registry.removeAssociation(path, "/vtr2121/test", "testasstype1");
        registry.removeAssociation(path, "/vtr2122/test", "testasstype2");
        registry.removeAssociation(path, "/vtr2123/test", "testasstype3");


        assertFalse(getAssocitionbyDestinationByType(path, "testasstype1", "/vtr2121/test"), "association Destination path exists");
        assertFalse(getAssocitionbyDestinationByType(path, "testasstype2", "/vtr2122/test"), "association Destination path exists");
        assertFalse(getAssocitionbyDestinationByType(path, "testasstype3", "/vtr2123/test"), "association Destination path exists");

        assertFalse(getAssocitionbyType(path, "testasstype1"), "association Type not exist");
        assertFalse(getAssocitionbyType(path, "testasstype2"), "association Type not exist");
        assertFalse(getAssocitionbyType(path, "testasstype3"), "association Type not exist");

        assertFalse(getAssocitionbySourceByType(path, "testasstype1"), "association Source path not exist");
        assertFalse(getAssocitionbySourceByType(path, "testasstype2"), "association Source path not exist");
        assertFalse(getAssocitionbySourceByType(path, "testasstype3"), "association Source path not exist");
    }

    @Test(groups = {"wso2.greg"})
    public void RemoveCollectionAssociationTest() throws RegistryException {

        Resource r2 = registry.newCollection();
        String path = "/assoColremove1/assoColremove2/assoColremove3";
        r2.setDescription("this is test desc");
        r2.setMediaType("plain/text");
        r2.setProperty("test2", "value2");

        registry.put(path, r2);
        registry.addAssociation(path, "/vtr2121/test", "testasstype1");
        registry.addAssociation(path, "/vtr2122/test", "testasstype2");
        registry.addAssociation(path, "/vtr2123/test", "testasstype3");


        assertTrue(getAssocitionbyDestinationByType(path, "testasstype1", "/vtr2121/test"), "association Destination path not exist");
        assertTrue(getAssocitionbyDestinationByType(path, "testasstype2", "/vtr2122/test"), "association Destination path not exist");
        assertTrue(getAssocitionbyDestinationByType(path, "testasstype3", "/vtr2123/test"), "association Destination path not exist");


        assertTrue(getAssocitionbyType(path, "testasstype1"), "association Type not exist");
        assertTrue(getAssocitionbyType(path, "testasstype2"), "association Type not exist");
        assertTrue(getAssocitionbyType(path, "testasstype3"), "association Type not exist");

        assertTrue(getAssocitionbySourceByType(path, "testasstype1"), "association Source path not exist");
        assertTrue(getAssocitionbySourceByType(path, "testasstype2"), "association Source path not exist");
        assertTrue(getAssocitionbySourceByType(path, "testasstype3"), "association Source path not exist");

        registry.removeAssociation(path, "/vtr2121/test", "testasstype1");
        registry.removeAssociation(path, "/vtr2122/test", "testasstype2");
        registry.removeAssociation(path, "/vtr2123/test", "testasstype3");


        assertFalse(getAssocitionbyDestinationByType(path, "testasstype1", "/vtr2121/test"), "association Destination path exists");
        assertFalse(getAssocitionbyDestinationByType(path, "testasstype2", "/vtr2122/test"), "association Destination path exists");
        assertFalse(getAssocitionbyDestinationByType(path, "testasstype3", "/vtr2123/test"), "association Destination path exists");

        assertFalse(getAssocitionbyType(path, "testasstype1"), "association Type not exist");
        assertFalse(getAssocitionbyType(path, "testasstype2"), "association Type not exist");
        assertFalse(getAssocitionbyType(path, "testasstype3"), "association Type not exist");

        assertFalse(getAssocitionbySourceByType(path, "testasstype1"), "association Source path not exist");
        assertFalse(getAssocitionbySourceByType(path, "testasstype2"), "association Source path not exist");
        assertFalse(getAssocitionbySourceByType(path, "testasstype3"), "association Source path not exist");

    }

    @Test(groups = {"wso2.greg"})
    public void RemoveRootAssociationTest() throws RegistryException {

        Resource r2 = registry.newCollection();
        String path = "/";
        r2.setDescription("this is test desc");
        r2.setMediaType("plain/text");
        r2.setProperty("test2", "value2");

        registry.put(path, r2);
        registry.addAssociation(path, "/vtr21212/test", "testasstype11");
        registry.addAssociation(path, "/vtr21222/test", "testasstype21");
        registry.addAssociation(path, "/vtr21232/test", "testasstype31");


        assertTrue(getAssocitionbyDestinationByType(path, "testasstype11", "/vtr21212/test"), "association Destination path not exist");
        assertTrue(getAssocitionbyDestinationByType(path, "testasstype21", "/vtr21222/test"), "association Destination path not exist");
        assertTrue(getAssocitionbyDestinationByType(path, "testasstype31", "/vtr21232/test"), "association Destination path not exist");


        assertTrue(getAssocitionbyType(path, "testasstype11"), "association Type not exist");
        assertTrue(getAssocitionbyType(path, "testasstype21"), "association Type not exist");
        assertTrue(getAssocitionbyType(path, "testasstype31"), "association Type not exist");

        assertTrue(getAssocitionbySourceByType(path, "testasstype11"), "association Source path not exist");
        assertTrue(getAssocitionbySourceByType(path, "testasstype21"), "association Source path not exist");
        assertTrue(getAssocitionbySourceByType(path, "testasstype31"), "association Source path not exist");

        registry.removeAssociation(path, "/vtr21212/test", "testasstype11");
        registry.removeAssociation(path, "/vtr21222/test", "testasstype21");
        registry.removeAssociation(path, "/vtr21232/test", "testasstype31");


        assertFalse(getAssocitionbyDestinationByType(path, "testasstype11", "/vtr21212/test"), "association Destination path exists");
        assertFalse(getAssocitionbyDestinationByType(path, "testasstype21", "/vtr21222/test"), "association Destination path exists");
        assertFalse(getAssocitionbyDestinationByType(path, "testasstype31", "/vtr21232/test"), "association Destination path exists");

        assertFalse(getAssocitionbyType(path, "testasstype11"), "association Type not exist");
        assertFalse(getAssocitionbyType(path, "testasstype21"), "association Type not exist");
        assertFalse(getAssocitionbyType(path, "testasstype31"), "association Type not exist");

        assertFalse(getAssocitionbySourceByType(path, "testasstype11"), "association Source path not exist");
        assertFalse(getAssocitionbySourceByType(path, "testasstype21"), "association Source path not exist");
        assertFalse(getAssocitionbySourceByType(path, "testasstype31"), "association Source path not exist");

    }

    /*  public void testRemoveCollectionAssociationwithSpaces () throws RegistryException {

        Resource r2 = registry.newCollection();
        String path = "/assoColremove11123/assoColremove21/assoColremove31";
        r2.setDescription("this is test desc");
        r2.setMediaType("plain/text");
        r2.setProperty("test2","value2");

        registry.put(path, r2);
        registry.addAssociation(path,"/vtr2121 test/test test","testasstype1 space");
        registry.addAssociation(path,"/vtr2122 test/test test","testasstype2 space");
        registry.addAssociation(path,"/vtr2123 test/test test","testasstype3 space");

        assertFalse("association Type not exist" ,getAssocitionbyType(path , "testasstype1 space"));
        assertTrue("association Destination path not exist" ,getAssocitionbyDestinationByType(path , "testasstype1 space", "/vtr2121 test/test test" ));
        assertTrue("association Destination path not exist" ,getAssocitionbyDestinationByType(path , "testasstype2 space", "/vtr2122 test/test test" ));
        assertTrue("association Destination path not exist" ,getAssocitionbyDestinationByType(path , "testasstype3 space", "/vtr2123 test/test test" ));

    assertTrue("association Destination path not exist" ,associationPathExists(path,"/vtr2121 test/test test"));
    assertTrue("association Destination path not exist" ,associationPathExists(path,"/vtr2122 test/test test"));
    assertTrue("association Destination path not exist" ,associationPathExists(path,"/vtr2123 test/test test"));


    assertTrue("association Type not exist" ,associationTypeExists(path,"testasstype1 space1"));
    assertTrue("association Type not exist" ,associationTypeExists(path,"testasstype1 space2"));
    assertTrue("association Type not exist" ,associationTypeExists(path,"testasstype1 space3"));

    assertTrue("association Source path not exist" ,associationSourcepathExists(path, path));
    assertTrue("association Source path not exist" ,associationSourcepathExists(path, path));
    assertTrue("association Source path not exist" ,associationSourcepathExists(path, path));


        assertTrue("association Type not exist" ,getAssocitionbyType(path , "testasstype1 space"));
        assertTrue("association Type not exist" ,getAssocitionbyType(path , "testasstype2 space"));
        assertTrue("association Type not exist" ,getAssocitionbyType(path , "testasstype3 space"));

        assertTrue("association Source path not exist" ,getAssocitionbySourceByType(path , "testasstype1 space"));
        assertTrue("association Source path not exist" ,getAssocitionbySourceByType(path , "testasstype2 space"));
        assertTrue("association Source path not exist" ,getAssocitionbySourceByType(path , "testasstype3 space"));

        registry.removeAssociation(path, "/vtr2121/test", "testasstype1 space");
        registry.removeAssociation(path, "/vtr2122/test", "testasstype2 space");
        registry.removeAssociation(path, "/vtr2123/test", "testasstype3 space");


        assertFalse("association Destination path exists" ,getAssocitionbyDestinationByType(path , "testasstype1 space", "/vtr2121 test/test test" ));
        assertFalse("association Destination path exists" ,getAssocitionbyDestinationByType(path , "testasstype2 space", "/vtr2122 test/test test" ));
        assertFalse("association Destination path exists" ,getAssocitionbyDestinationByType(path , "testasstype3 space", "/vtr2123 test/test test" ));

        assertFalse("association Type not exist" ,getAssocitionbyType(path , "testasstype1 space"));
        assertFalse("association Type not exist" ,getAssocitionbyType(path , "testasstype2 space"));
        assertFalse("association Type not exist" ,getAssocitionbyType(path , "testasstype3 space"));

        assertFalse("association Source path not exist" ,getAssocitionbySourceByType(path , "testasstype1 space"));
        assertFalse("association Source path not exist" ,getAssocitionbySourceByType(path , "testasstype2 space"));
        assertFalse("association Source path not exist" ,getAssocitionbySourceByType(path , "testasstype3 space"));

    }*/

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

    public boolean associationTypeExists(String path, String assoType) throws RegistryException {
        Association association[] = registry.getAllAssociations(path);
        boolean value = false;

        for (int i = 0; i < association.length; i++) {
            association[i].getAssociationType();
            if (assoType.equals(association[i].getAssociationType())) {
                value = true;
            }
        }


        return value;
    }

    public boolean associationSourcepathExists(String path, String sourcePath)
            throws RegistryException {
        Association association[] = registry.getAllAssociations(path);
        boolean value = false;

        for (int i = 0; i < association.length; i++) {
            association[i].getAssociationType();
            if (sourcePath.equals(association[i].getSourcePath())) {
                value = true;
            }
        }

        return value;
    }

    public boolean getAssocitionbyType(String path, String type) throws RegistryException {

        Association[] asso;
        asso = registry.getAssociations(path, type);

        boolean assoFound = false;

        for (Association a2 : asso) {

            if (a2.getAssociationType().equals(type)) {
                assoFound = true;
                break;
            }
        }
        return assoFound;
    }

    public boolean getAssocitionbySourceByType(String path, String type) throws RegistryException {

        Association[] asso;
        asso = registry.getAssociations(path, type);

        boolean assoFound = false;

        for (Association a2 : asso) {

            if (a2.getSourcePath().equals(path)) {
                assoFound = true;
                break;
            }
        }
        return assoFound;
    }

    public boolean getAssocitionbyDestinationByType(String path, String type,
                                                    String destinationPath)
            throws RegistryException {

        Association[] asso;
        asso = registry.getAssociations(path, type);


        boolean assoFound = false;

        for (Association a2 : asso) {

            if (a2.getDestinationPath().equals(destinationPath)) {
                assoFound = true;
                break;
            }
        }
        return assoFound;
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

}
