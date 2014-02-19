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
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.endpoints.dataobjects.Endpoint;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.wsdls.WsdlFilter;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import static org.testng.Assert.assertTrue;

/**
 * Class will test all API methods of WSDL manager
 */
public class WSDLManagerAPITestCase {

    public static WsdlManager wsdlManager;
    private static Wsdl wsdlObj;
    private static Wsdl[] wsdlArray;
    public String sampleWsdlURL = "http://ws.strikeiron.com/donotcall2_5?WSDL";
    private String wsdlName = "donotcall2_5.wsdl";
    private Registry governanceRegistry;
    private WSRegistryServiceClient wsRegistry;


    @BeforeClass(alwaysRun = true)
    public void initializeAPIObject() throws RegistryException, AxisFault {
        int userId = 1;
        RegistryProviderUtil provider = new RegistryProviderUtil();
        wsRegistry = provider.getWSRegistry(userId,
                ProductConstant.GREG_SERVER_NAME);
        governanceRegistry = provider.getGovernanceRegistry(wsRegistry, userId);

        wsdlManager = new WsdlManager(governanceRegistry);


    }

    @Test(groups = {"wso2.greg"}, description = "Testing "
            + "addWsdl API method", priority = 1)
    public void testAddWsdl() throws GovernanceException {

        wsdlObj = wsdlManager.newWsdl(sampleWsdlURL);
        wsdlManager.addWsdl(wsdlObj);

    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = "testAddWsdl", description = "Testing "
            + "getAllWsdls API method")
    public void testGetAllWsdl() throws GovernanceException {
        boolean isWsdlFound = false;

        wsdlArray = wsdlManager.getAllWsdls();

        for (Wsdl w : wsdlArray) {
            if (w.getQName().getLocalPart().equalsIgnoreCase(wsdlName)) {
                isWsdlFound = true;
            }
        }
        assertTrue(isWsdlFound, "Return object of getAllWsdls"
                + " method doesn't have all information ");
    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = "testAddWsdl", description = "Testing "
            + "getWsdl API method")
    public void testGetWsdl() throws GovernanceException {
        Wsdl localWsdlObj = null;
        boolean check = false;
        for (Wsdl w : wsdlArray) {
            if (w.getQName().getLocalPart().equalsIgnoreCase(wsdlName)) {

                localWsdlObj = wsdlManager.getWsdl(w.getId());

            }
        }
        if (localWsdlObj != null) {
            if (localWsdlObj.getQName().getLocalPart()
                    .equalsIgnoreCase(wsdlName)) {
                check = true;
            }

        }
        assertTrue(check, "getWsdl method doesn't work");
    }

    @Test(groups = {"wso2.greg"}, description = "Testing getQName method in WSDL object", dependsOnMethods = "testAddWsdl")
    public void testGetQName() throws RegistryException {

        assertTrue(
                wsdlObj.getQName().getLocalPart().equalsIgnoreCase(wsdlName),
                "WSDL:getQName API method thrown error");

    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = "testAddWsdl", description = "Testing "
            + "getWsdl API method")
    public void testUpdateWsdl() throws GovernanceException {
        String lcName = "ServiceLifeCycle";
        boolean isLCFound = false;

        Wsdl wsdl = wsdlManager.getWsdl(wsdlObj.getId());
        wsdl.attachLifecycle(lcName);
        wsdlManager.updateWsdl(wsdl);
        wsdlArray = wsdlManager.getAllWsdls();
        for (Wsdl w : wsdlArray) {
            if (w.getLifecycleName().equalsIgnoreCase(lcName)) {
                isLCFound = true;
            }
        }
        assertTrue(isLCFound,
                "Error occurred while executing WsdlManager:updateWsdl method");

    }

    @Test(groups = {"wso2.greg"}, description = "Testing FindWSDL", dependsOnMethods = "testUpdateWsdl")
    public void testFindService() throws GovernanceException {

        Wsdl[] wsdlArray = wsdlManager.findWsdls(new WsdlFilter() {
            public boolean matches(Wsdl wsdl) throws GovernanceException {
                String name = wsdl.getQName().getLocalPart();
                assertTrue(name.contains(wsdlName),

                        "Error occurred while executing findWSDL API method");
                return name.contains(wsdlName);
            }
        });
        assertTrue(wsdlArray.length > 0,
                "Error occurred while executing findWSDL API method");

    }

    @Test(groups = {"wso2.greg"}, description = "Testing getWsdlElement method in WSDL object", dependsOnMethods = "testFindService")
    public void testGetWsdlElement() throws GovernanceException {
        boolean check = false;
        Wsdl[] allWSDLs = wsdlManager.getAllWsdls();
        for (Wsdl w : allWSDLs) {
            if (w.getQName().getLocalPart().equalsIgnoreCase(wsdlName)) {
                OMElement omElement = w.getWsdlElement();
                if (omElement.toString().contains("Do Not Call List Service")) {
                    check = true;
                }
            }
        }
        assertTrue(check,
                "Error occurred while executing getWsdlElement API method with WSDL object");
    }

    @AfterClass(groups = "wso2.greg", alwaysRun = true, description = "cleaning up the artifacts added")
    public void tearDown() throws RegistryException {

        GovernanceArtifact[] governanceArtifacts = wsdlObj.getDependents();
        final String pathPrefix = "/_system/governance";

        Endpoint[] endpoints;
        endpoints = wsdlObj.getAttachedEndpoints();

        for (GovernanceArtifact tmpGovernanceArtifact : governanceArtifacts) {

            if (wsRegistry.resourceExists(pathPrefix + tmpGovernanceArtifact.getPath())) {
                wsRegistry.delete(pathPrefix + tmpGovernanceArtifact.getPath());
            }

        }

        for (Endpoint tmpEndpoint : endpoints) {
            if (wsRegistry.resourceExists(pathPrefix + tmpEndpoint.getPath())) {
                wsRegistry.delete(pathPrefix + tmpEndpoint.getPath());
            }
        }

        wsRegistry = null;
        wsdlManager = null;
        wsdlObj = null;
        governanceRegistry = null;
    }

}
