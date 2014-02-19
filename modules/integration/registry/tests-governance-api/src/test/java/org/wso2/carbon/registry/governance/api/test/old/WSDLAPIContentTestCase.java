/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.core.utils.fileutils.FileManager;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.endpoints.dataobjects.Endpoint;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.relations.stub.AddAssociationRegistryExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class WSDLAPIContentTestCase {
    public static WsdlManager wsdlManager;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private static final String pathPrefix = "/_system/governance";

    @BeforeClass(alwaysRun = true)
    public void initializeAPIObject() throws RegistryException,
                                             RemoteException,
                                             LoginAuthenticationExceptionException {
        int userId = 1;
        RegistryProviderUtil provider = new RegistryProviderUtil();
        WSRegistryServiceClient wsRegistry = provider.getWSRegistry(userId,
                                                                    ProductConstant.GREG_SERVER_NAME);
        Registry governanceRegistry;
        governanceRegistry = provider.getGovernanceRegistry(wsRegistry, userId);
        wsdlManager = new WsdlManager(governanceRegistry);

        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        ManageEnvironment environment = builder.build();
        UserInfo userInfo = UserListCsvReader.getUserInfo(1);
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                               environment.getGreg().getSessionCookie());

    }

    @Test(groups = {"wso2.greg"}, description = "Testing AddWSDL with Inline content")
    public void testAddWSDLContentWithName() throws IOException,
                                                    RegistryException,
                                                    ResourceAdminServiceExceptionException,
                                                    AddAssociationRegistryExceptionException {

        String wsdlFileLocation = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION
                                  + "artifacts" + File.separator + "GREG" + File.separator
                                  + "wsdl" + File.separator + "Automated.wsdl";
        Wsdl wsdl = wsdlManager.newWsdl(FileManager.readFile(wsdlFileLocation)
                                                .getBytes(), "AutomatedSample.wsdl");
        wsdlManager.addWsdl(wsdl);

        // wsdl addition verification
        assertNotNull(wsdlManager.getWsdl(wsdl.getId()));
        Wsdl[] wsdlArray = wsdlManager.getAllWsdls();
        boolean isWSDLFound = false;
        for (Wsdl w : wsdlArray) {
            if (w.getQName().getNamespaceURI()
                    .equalsIgnoreCase("http://www.strikeiron.com")) {
                isWSDLFound = true;
            }
        }
        assertTrue(isWSDLFound,
                   "WsdlManager:newWsdl method does not execute with inline wsdl content");

        Endpoint[] endpoints;
        endpoints = wsdl.getAttachedEndpoints();

        GovernanceArtifact[] governanceArtifacts = wsdl.getDependents();
        for (GovernanceArtifact tmpGovernanceArtifact : governanceArtifacts) {
            resourceAdminServiceClient.deleteResource(pathPrefix + tmpGovernanceArtifact.getPath());
        }

        for (Endpoint tmpEndpoint : endpoints) {
            resourceAdminServiceClient.deleteResource(pathPrefix + tmpEndpoint.getPath());
        }

    }

    @Test(groups = {"wso2.greg"}, description = "Testing AddWSDL with Inline content", dependsOnMethods = "testAddWSDLContentWithName")
    public void testAddWSDLContentWithoutName() throws RegistryException,
                                                       IOException,
                                                       ResourceAdminServiceExceptionException,
                                                       AddAssociationRegistryExceptionException {

        String wsdlFileLocation = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION
                                  + "artifacts" + File.separator + "GREG" + File.separator
                                  + "wsdl" + File.separator + "Automated.wsdl";

        boolean isWSDLFound = false;
        Wsdl wsdl = wsdlManager.newWsdl(FileManager.readFile(wsdlFileLocation)
                                                .getBytes());
        wsdlManager.addWsdl(wsdl);
        Wsdl[] wsdlArray = wsdlManager.getAllWsdls();
        for (Wsdl w : wsdlArray) {
            if (w.getQName().getNamespaceURI()
                    .equalsIgnoreCase("http://www.strikeiron.com")) {
                isWSDLFound = true;
            }
        }
        assertTrue(isWSDLFound,
                   "WsdlManager:newWsdl method does not execute with inline wsdl content");

        GovernanceArtifact[] governanceArtifacts = wsdl.getDependents();

        Endpoint[] endpoints;
        endpoints = wsdl.getAttachedEndpoints();

        for (GovernanceArtifact tmpGovernanceArtifact : governanceArtifacts) {
            resourceAdminServiceClient.deleteResource(pathPrefix + tmpGovernanceArtifact.getPath());
        }

        for (Endpoint tmpEndpoint : endpoints) {
            resourceAdminServiceClient.deleteResource(pathPrefix + tmpEndpoint.getPath());
        }


    }

    @AfterClass(groups = "wso2.greg", alwaysRun = true, description = "cleaning up the artifacts added")
    public void tearDown() throws GovernanceException {
        // added resources are locally removed in side the test methods for
        // convenience
        resourceAdminServiceClient = null;
        wsdlManager = null;

    }
}
