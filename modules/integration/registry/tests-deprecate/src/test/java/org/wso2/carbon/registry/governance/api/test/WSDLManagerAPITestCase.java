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

package org.wso2.carbon.registry.governance.api.test;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.governance.api.endpoints.EndpointManager;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.schema.SchemaManager;
import org.wso2.carbon.governance.api.wsdls.WsdlFilter;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import static org.testng.Assert.assertTrue;

/**
 * Class will test all API methods of WSDL manager
 */
public class WSDLManagerAPITestCase {

    public static WsdlManager wsdlManager;
    public static EndpointManager endpointManager;
    public static SchemaManager schemaManager;
    private static Wsdl wsdlObj;
    private static Wsdl[] wsdlArray;
    public String sampleWsdlURL = "http://ws.strikeiron.com/donotcall2_5?WSDL";
    private String wsdlName = "donotcall2_5.wsdl";
    private  Registry governance;

    @BeforeClass(alwaysRun = true)
    public void initializeAPIObject() throws RegistryException, AxisFault {
        governance = TestUtils.getRegistry();
        TestUtils.cleanupResources(governance);
        wsdlManager = new WsdlManager(governance);
        endpointManager = new EndpointManager(governance);
        schemaManager = new SchemaManager(governance);

    }

    @Test(groups = {"wso2.greg.api"}, description = "Testing " +"addWsdl API method",priority = 1)
    public void testAddWsdl() throws GovernanceException {
        try {
            wsdlObj = wsdlManager.newWsdl(sampleWsdlURL);
            wsdlManager.addWsdl(wsdlObj);
        } catch (GovernanceException e) {
            throw new GovernanceException("Error occurred while executing WsdlManager:addWsdl method" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg.api"}, dependsOnMethods = "testAddWsdl", description = "Testing " +
                                                                                        "getAllWsdls API method")
    public void testGetAllWsdl() throws GovernanceException {
        boolean isWsdlFound = false;
        try {
            wsdlArray = wsdlManager.getAllWsdls();
        } catch (GovernanceException e) {
            throw new GovernanceException("Error occurred while executing WsdlManager:addWsdl method" + e.getMessage());
        }
        for (Wsdl w : wsdlArray) {
            if (w.getQName().getLocalPart().equalsIgnoreCase(wsdlName)) {
                isWsdlFound = true;
            }
        }
        assertTrue(isWsdlFound, "Return object of getAllWsdls" +
                                " method doesn't have all information ");
    }

    @Test(groups = {"wso2.greg.api"}, dependsOnMethods = "testAddWsdl", description = "Testing " +
                                                                                        "getWsdl API method")
    public void testGetWsdl() throws GovernanceException {
        Wsdl localWsdlObj = null;
        for (Wsdl w : wsdlArray) {
            if (w.getQName().getLocalPart().equalsIgnoreCase(wsdlName)) {
                try {
                    localWsdlObj = wsdlManager.getWsdl(w.getId());

                } catch (GovernanceException e) {
                    throw new GovernanceException("Error occurred while executing WsdlManager:getWsdl method" +
                            e.getMessage());
                }
            }
        }
        if (localWsdlObj != null) {
            assertTrue(localWsdlObj.getQName().getLocalPart().equalsIgnoreCase(wsdlName), "getWsdl method doesn't work");
        }
    }

    @Test(groups = {"wso2.greg.api"}, description = "Testing getQName method in WSDL object",
            dependsOnMethods = "testAddWsdl")
    public void testGetQName() throws RegistryException {

        assertTrue(wsdlObj.getQName().getLocalPart().equalsIgnoreCase(wsdlName),
                "WSDL:getQName API method thrown error");

    }

//    @Test(groups = {"wso2.greg.api"}, description = "Testing getUrl method in WSDL object",
//            dependsOnMethods = "testAddWsdl")
//    public void testGetURL() throws GovernanceException {
//
//        assertTrue(wsdlObj.getUrl().equalsIgnoreCase(sampleWsdlURL),
//                "WSDL:getUrl() API method thrown error");
//    }

    @Test(groups = {"wso2.greg.api"}, dependsOnMethods = "testAddWsdl", description = "Testing " +
                                                                                        "getWsdl API method")
    public void testUpdateWsdl() throws GovernanceException {
        String lcName = "ServiceLifeCycle";
        boolean isLCFound = false;
        try {
            Wsdl  wsdl =  wsdlManager.getWsdl(wsdlObj.getId());
            wsdl.attachLifecycle(lcName);
            wsdlManager.updateWsdl(wsdl);
            wsdlArray = wsdlManager.getAllWsdls();
            for (Wsdl w : wsdlArray) {
                if (w.getLifecycleName().equalsIgnoreCase(lcName)) {
                    isLCFound = true;
                }
            }
            assertTrue(isLCFound, "Error occurred while executing WsdlManager:updateWsdl method");
        } catch (GovernanceException e) {
            throw new GovernanceException("Error occurred while executing WsdlManager:updateWsdl method", e);
        }

    }

    @Test(groups = {"wso2.greg.api"}, description = "Testing FindWSDL", dependsOnMethods = "testUpdateWsdl")
    public void testFindService() throws GovernanceException {
        try {
            Wsdl[] wsdlArray = wsdlManager.findWsdls(new WsdlFilter() {
                public boolean matches(Wsdl wsdl) throws GovernanceException {
                    String name = wsdl.getQName().getLocalPart();
                    assertTrue(name.contains(wsdlName), "Error occurred while executing findWSDL API method");
                    return name.contains(wsdlName);
                }
            });
            assertTrue(wsdlArray.length > 0, "Error occurred while executing findWSDL API method");
        } catch (GovernanceException e) {
            throw new GovernanceException("Error occurred while executing WsdlManager:findWsdls method",e);
        }
    }
    @Test(groups = {"wso2.greg.api"}, description = "Testing getWsdlElement method in WSDL object",
            dependsOnMethods = "testFindService")
    public void testGetWsdlElement() throws GovernanceException {
        try {
            Wsdl[] allWSDLs = wsdlManager.getAllWsdls();
            for (Wsdl w : allWSDLs) {
                if (w.getQName().getLocalPart().equalsIgnoreCase(wsdlName)) {
                    OMElement omElement = w.getWsdlElement();
                    assertTrue(omElement.toString().contains("Do Not Call List Service"),
                            "Error occurred while executing getWsdlElement API method with WSDL object");
                }
            }
        } catch (GovernanceException e) {
            throw new GovernanceException("Error occurred while executing getWsdlElement API " +
                    "method with WSDL object" + e.getMessage());
        }
    }
    @Test(groups = {"wso2.greg.api"}, description = "Testing RemoveWSDL", dependsOnMethods = "testGetWsdlElement")
    public void testRemoveWSDL() throws GovernanceException {
        boolean check;
        try {
            cleanWSDL();
        } catch (GovernanceException e) {
            check =true;
            Assert.assertTrue(check ,"Failed to delete WSDL");
            throw new GovernanceException("Error occurred while executing WsdlManager:removeWsdl method " +
                                          ":" + e.getMessage());
        }
    }

    private void cleanWSDL() throws GovernanceException {
        wsdlArray = wsdlManager.getAllWsdls();
        for (Wsdl w : wsdlArray) {
            if (w.getQName().getNamespaceURI().contains("www.strikeiron.com")) {
                wsdlManager.removeWsdl(w.getId());
            }
        }
    }
    @AfterClass
    public void cleanTestArtifacts() throws RegistryException {
        TestUtils.cleanupResources(governance);

    }


}
