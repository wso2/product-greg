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

import org.apache.axis2.AxisFault;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.registry.InfoServiceAdminClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.endpoints.dataobjects.Endpoint;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.info.stub.RegistryExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import java.net.MalformedURLException;
import java.rmi.RemoteException;

import static org.testng.Assert.*;

public class WsdlRatingVerificationTestCase {
    private Registry governanceRegistry;
    private Wsdl wsdl;
    private ManageEnvironment environment;
    private InfoServiceAdminClient infoServiceAdminclient;
    private WsdlManager wsdlManager;
    private float avgRating;
    private int myRating;
    private WSRegistryServiceClient wsRegistry;

    @BeforeClass(groups = "wso2.greg", alwaysRun = true)
    public void initialize() throws RemoteException,
                                    LoginAuthenticationExceptionException,
                                    org.wso2.carbon.registry.api.RegistryException {

        int userId = 2;
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        environment = builder.build();
        infoServiceAdminclient =
                new InfoServiceAdminClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                           environment.getGreg().getSessionCookie());

        RegistryProviderUtil provider = new RegistryProviderUtil();
        wsRegistry = provider.getWSRegistry(userId,
                                            ProductConstant.GREG_SERVER_NAME);
        governanceRegistry = provider.getGovernanceRegistry(wsRegistry, userId);
        wsdlManager = new WsdlManager(governanceRegistry);

    }


    @Test(groups = "wso2.greg", description = "ratings verification")
    public void testAddWSDL() throws RemoteException,
                                     ResourceAdminServiceExceptionException, GovernanceException,
                                     MalformedURLException {


        wsdl = wsdlManager
                .newWsdl("https://svn.wso2.org/repos/wso2/carbon/platform/trunk/platform-integration/"
                         + "platform-automated-test-suite/org.wso2.carbon.automation.test.repo/src/main/resources/artifacts/"
                         + "GREG/wsdl/echo.wsdl");

        wsdl.addAttribute("version", "1.0.0");
        wsdl.addAttribute("author", "Aparna");
        wsdl.addAttribute("description", "added wsdl using url");
        wsdlManager.addWsdl(wsdl);
        wsdlManager.updateWsdl(wsdl);
        assertFalse(wsdl.getId().isEmpty());
        assertNotNull(wsdl);
        assertTrue(wsdl.getAttribute("author").contentEquals("Aparna"));

    }

    @Test(groups = "wso2.greg", description = "ratings Verification", dependsOnMethods = "testAddWSDL")
    public void testRatingVerification()
            throws AxisFault, RegistryException, RegistryExceptionException {

        final String wsdlPath = "/_system/governance" + wsdl.getPath();

        myRating = 3;
        infoServiceAdminclient.rateResource(String.valueOf(myRating), wsdlPath,
                                            environment.getGreg().getSessionCookie());
        wsdlManager.updateWsdl(wsdl);
        assertEquals(
                infoServiceAdminclient.getRatings(wsdlPath,
                                                  environment.getGreg().getSessionCookie())
                        .getUserRating(), myRating);

        avgRating = infoServiceAdminclient.getRatings(wsdlPath,
                                                      environment.getGreg().getSessionCookie()).getAverageRating();
        assertEquals(avgRating, 3.0f);

    }

    @Test(groups = "wso2.greg", description = "myrating, rating relationship verified", dependsOnMethods = "testRatingVerification")
    public void testRatingChanges()
            throws AxisFault, RegistryException, RegistryExceptionException {

        final String wsdlPath = "/_system/governance" + wsdl.getPath();
        myRating = 2;
        infoServiceAdminclient.rateResource(String.valueOf(myRating), wsdlPath,
                                            environment.getGreg().getSessionCookie());
        wsdlManager.updateWsdl(wsdl);
        avgRating = infoServiceAdminclient.getRatings(wsdlPath,
                                                      environment.getGreg().getSessionCookie()).getAverageRating();

        assertTrue(avgRating < 3.0f);

    }

    @AfterClass(groups = "wso2.greg", alwaysRun = true, description = "cleaning up the artifacts added")
    public void tearDown() throws RegistryException {
        String pathPrefix = "/_system/governance";
        Endpoint[] endpoints;
        endpoints = wsdl.getAttachedEndpoints();
        String previousGovernanceArtifactPath = "to prevent re-deleting errors";
        
        GovernanceArtifact[] governanceArtifacts = wsdl.getDependents();
        for (GovernanceArtifact tmpGovernanceArtifact : governanceArtifacts) {
            if (!tmpGovernanceArtifact.getPath().contentEquals(previousGovernanceArtifactPath)) {
                wsRegistry.delete(pathPrefix + tmpGovernanceArtifact.getPath());
            }
            previousGovernanceArtifactPath = tmpGovernanceArtifact.getPath();
        }

        for (Endpoint tmpEndpoint : endpoints) {
        	GovernanceArtifact[] dependentArtifacts =  tmpEndpoint.getDependents();
        	for (GovernanceArtifact tmpGovernanceArtifact : dependentArtifacts) {
                wsRegistry.delete(pathPrefix + tmpGovernanceArtifact.getPath());
            }
            wsRegistry.delete(pathPrefix + tmpEndpoint.getPath());
        }
        
        wsRegistry = null;
        wsdl = null;
        wsdlManager = null;
        infoServiceAdminclient
                = null;
        environment = null;
        governanceRegistry = null;
    }

}
