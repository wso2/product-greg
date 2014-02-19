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
package org.wso2.carbon.registry.metadata.test.wsdl;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.endpoints.dataobjects.Endpoint;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import java.rmi.RemoteException;

import static org.testng.Assert.*;

public class SameWsdlAgainAdditionTestCase {

    private Registry governance;
    private Wsdl wsdl, wsdlCopy;
    private WsdlManager wsdlManager;
    private WSRegistryServiceClient wsRegistry;

    @BeforeClass(groups = "wso2.greg", alwaysRun = true)
    public void initialize() throws RemoteException,
            LoginAuthenticationExceptionException,
            org.wso2.carbon.registry.api.RegistryException {
        int userId = 2;
        RegistryProviderUtil provider = new RegistryProviderUtil();
        wsRegistry = provider.getWSRegistry(userId,
                ProductConstant.GREG_SERVER_NAME);
        governance = provider.getGovernanceRegistry(wsRegistry, userId);
        wsdlManager = new WsdlManager(governance);
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);

    }

    /**
     * WSDL addition from URL
     */
    @Test(groups = "wso2.greg", description = "Add WSDL via URL: Automated.wsdl")
    public void testAddWSDL() throws RemoteException,
            ResourceAdminServiceExceptionException, GovernanceException {


        wsdl = wsdlManager
                .newWsdl("https://svn.wso2.org/repos/wso2/carbon/platform/trunk/platform-integration/"
                        + "platform-automated-test-suite/org.wso2.carbon.automation.test.repo/src/main/resources/artifacts/"
                        + "GREG/wsdl/Automated.wsdl");
        wsdl.addAttribute("version", "1.0.0");
        wsdl.addAttribute("author", "kana");
        wsdl.addAttribute("description", "added wsdl via URL");
        wsdlManager.addWsdl(wsdl);

        assertTrue(wsdl.getAttribute("description").contentEquals(
                "added wsdl via URL")); // WSDL addition from URL: verification
        assertTrue(wsdl.getAttribute("author").contentEquals("kana"));
        assertNotNull(wsdlManager.getWsdl(wsdl.getId()));
    }

    /**
     * adding the same wsdl again
     *
     * @throws java.rmi.RemoteException
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException
     *
     * @throws org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException
     *
     */
    @Test(groups = "wso2.greg", description = "Add same WSDL via URL", dependsOnMethods = "testAddWSDL")
    public void testAddAlreadyAddedWSDL() throws RemoteException,
            ResourceAdminServiceExceptionException,
            GovernanceException {

        wsdlCopy = wsdlManager
                .newWsdl("https://svn.wso2.org/repos/wso2/carbon/platform/trunk/platform-integration/"
                        + "platform-automated-test-suite/org.wso2.carbon.automation.test.repo/src/main/resources/artifacts/G"
                        + "REG/wsdl/Automated.wsdl");

        wsdlManager.addWsdl(wsdlCopy);
        assertTrue(wsdlCopy.getAttribute("description").contains(
                "added wsdl via URL"));
        // wsdl.removeAttribute("description");
        wsdlCopy.removeAttribute("description");
        wsdlManager.updateWsdl(wsdl);
        wsdlCopy.addAttribute("description", "added again via Url");
        wsdlManager.updateWsdl(wsdlCopy);
        assertFalse(wsdlCopy.getId().isEmpty());
        assertNotNull(wsdlCopy);
        assertTrue(wsdlCopy.getAttribute("description").contentEquals(
                "added again via Url")); // Second WSDL addition from URL:
        // verification

    }

    /**
     * compare both WSDLs additions from URL: verification
     *
     * @throws java.rmi.RemoteException
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException
     *
     */
    @Test(groups = "wso2.greg", description = "Add WSDL via URL", dependsOnMethods = "testAddAlreadyAddedWSDL")
    public void testCompareWSDLs() throws RemoteException,
            ResourceAdminServiceExceptionException,
            GovernanceException {
        assertFalse(wsdlCopy.getId().equals(wsdl.getId()),
                "Verifying both wsdl ids are different"); // the ids are
        // different
        assertEquals(wsdlCopy.getAttribute("author"),
                wsdl.getAttribute("author"), "Verifying both wsdls' author");

    }

    @AfterClass(groups = "wso2.greg", alwaysRun = true, description = "cleaning up the artifacts added")
    public void tearDown()
            throws RegistryException, LoginAuthenticationExceptionException, RemoteException,
            ResourceAdminServiceExceptionException {
        String pathPrefix = "/_system/governance";

        Endpoint[] endpoints;
        endpoints = wsdl.getAttachedEndpoints();

        GovernanceArtifact[] governanceArtifacts = wsdl.getDependents();
        for (GovernanceArtifact tmpGovernanceArtifact : governanceArtifacts) {
            wsRegistry.delete(pathPrefix + tmpGovernanceArtifact.getPath());
        }

        for (Endpoint tmpEndpoint : endpoints) {
        	GovernanceArtifact[] dependentArtifacts =  tmpEndpoint.getDependents();
        	for (GovernanceArtifact tmpGovernanceArtifact : dependentArtifacts) {
                wsRegistry.delete(pathPrefix + tmpGovernanceArtifact.getPath());
            }
            wsRegistry.delete(pathPrefix + tmpEndpoint.getPath());
        }
        wsRegistry = null;
        governance = null;
        wsdl = null;
        wsdlCopy = null;
        wsdlManager = null;
    }


}
