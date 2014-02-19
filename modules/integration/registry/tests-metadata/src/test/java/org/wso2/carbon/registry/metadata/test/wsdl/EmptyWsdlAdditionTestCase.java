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

import org.apache.axiom.om.OMException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.fileutils.FileManager;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;

import static org.testng.Assert.*;

public class EmptyWsdlAdditionTestCase {
    private Wsdl wsdl;
    private Registry governanceRegistry;
    private WsdlManager wsdlManager;

    @BeforeClass(groups = "wso2.greg", alwaysRun = true)
    public void initialize() throws RemoteException,
            LoginAuthenticationExceptionException,
            org.wso2.carbon.registry.api.RegistryException {
        int userId = 2;

        RegistryProviderUtil provider = new RegistryProviderUtil();
        WSRegistryServiceClient wsRegistry = provider.getWSRegistry(userId,
                ProductConstant.GREG_SERVER_NAME);
        governanceRegistry = provider.getGovernanceRegistry(wsRegistry, userId);
        wsdlManager = new WsdlManager(governanceRegistry);

    }

    /**
     * adding empty wsdl:added a wsdl only with the prime tags: accepted this is
     * meant to be a negative wsdl test case: need a complety emptied artifact
     */
    @Test(groups = "wso2.greg", description = "Add empty wsdl")
    public void testAddEmptyWSDL() throws RemoteException,
            ResourceAdminServiceExceptionException,
            GovernanceException,
            MalformedURLException {

        wsdl = wsdlManager
                .newWsdl("https://svn.wso2.org/repos/wso2/carbon/platform/trunk/"
                        + "platform-integration/platform-automated-test-suite/org.wso2.carbon.automation.test.repo/"
                        + "src/main/resources/artifacts/GREG/wsdl/echo_empty.wsdl");

        wsdl.addAttribute("version", "1.0.0");
        wsdl.addAttribute("author", "Aparna");
        wsdl.addAttribute("description", "added empty wsdl using url");
        wsdlManager.addWsdl(wsdl);
        assertFalse(wsdl.getId().isEmpty());
        assertNotNull(wsdl);
        assertTrue(wsdl.getAttribute("description").contentEquals(
                "added empty wsdl using url")); // name with spaces
        // WSDL addition
        // from URL:
        // verification

    }

    /**
     * empty wsdl form file system
     */
    @Test(groups = "wso2.greg", description = "empty wsdl form file system using admin services", expectedExceptions = OMException.class, dependsOnMethods = "testAddEmptyWSDL")
    public void testAddEmptyWsdlFromFileSystem() throws RegistryException,
            IOException {

        // clarity automation api for registry
        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION
                + "artifacts" + File.separator + "GREG" + File.separator
                + "wsdl" + File.separator + "emptyWsdl.wsdl"; // the
        // path
        wsdlManager.newWsdl(FileManager.readFile(resourcePath).getBytes()); // OMException
        // will
        // be
        // generated

    }

    @AfterClass(groups = "wso2.greg", alwaysRun = true, description = "cleaning up the artifacts added")
    public void tearDown() throws GovernanceException {
        wsdlManager.removeWsdl(wsdl.getId());    //has no associations nor dependencies
        governanceRegistry = null;
        wsdlManager = null;
        wsdl = null;


    }
}
