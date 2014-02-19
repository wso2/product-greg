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
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
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
import org.wso2.carbon.registry.info.stub.RegistryExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.beans.xsd.VersionPath;
import org.wso2.carbon.registry.version.test.utils.VersionUtils;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import java.rmi.RemoteException;

import static org.testng.Assert.*;

public class WsdlMetadataVerificationTestCase {

    private ManageEnvironment environment;
    private Wsdl wsdl;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private WsdlManager wsdlManager;
    private String wsdlPath;
    private VersionPath[] versionPaths;
    private WSRegistryServiceClient wsRegistry;

    @BeforeClass(groups = "wso2.greg", alwaysRun = true)
    public void initialize() throws RemoteException,
                                    LoginAuthenticationExceptionException,
                                    org.wso2.carbon.registry.api.RegistryException {
        int userId = 2;
        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        wsRegistry = registryProviderUtil
                .getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        Registry governanceRegistry = registryProviderUtil
                .getGovernanceRegistry(wsRegistry, userId);
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);

        environment = builder.build();

        UserInfo userInfo = UserListCsvReader.getUserInfo(1);

        resourceAdminServiceClient =
                new ResourceAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                               environment.getGreg().getSessionCookie());
        wsdlManager = new WsdlManager(governanceRegistry);
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governanceRegistry);
    }

    /**
     * Metadata verification
     *
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException
     *
     */
    @Test(groups = "wso2.greg", description = "add wsdl")
    public void testAddWSDL()
            throws GovernanceException, ResourceAdminServiceExceptionException, RemoteException {

        wsdl = wsdlManager
                .newWsdl("https://svn.wso2.org/repos/wso2/carbon/platform/trunk/"
                         + "platform-integration/platform-automated-test-suite/"
                         + "org.wso2.carbon.automation.test.repo/"
                         + "src/main/resources/artifacts/GREG/wsdl/echo.wsdl");

        wsdl.addAttribute("version", "1.0.0");
        wsdl.addAttribute("author", "Aparna");
        wsdl.addAttribute("description",
                          "added valid wsdl to verify the check point");
        wsdlManager.addWsdl(wsdl);
        wsdlManager.updateWsdl(wsdl);
        assertNotNull(wsdl);
        assertTrue(wsdl.getAttribute("author").contentEquals("Aparna"));
        wsdlPath = "/_system/governance" + wsdl.getPath();
        VersionUtils.deleteAllVersions(resourceAdminServiceClient, wsdlPath);

    }

    @Test(groups = "wso2.greg", description = "metadata verification", dependsOnMethods = "testAddWSDL")
    public void testVerifyMetadata() throws RemoteException,
                                            LoginAuthenticationExceptionException,
                                            GovernanceException,
                                            ResourceAdminServiceExceptionException {

        assertTrue(resourceAdminServiceClient
                           .getMetadata("/_system/governance" + wsdl.getPath())
                           .getMediaType().contentEquals("application/wsdl+xml")); // mediatype
        // verified

        assertNull(resourceAdminServiceClient.getMetadata(wsdlPath)
                           .getPermalink());
        // / verified
        // permalink:is null

    }

    /**
     * the description is able to set when the resource is restored
     */
    @Test(groups = "wso2.greg", description = "metadata verisons verification", dependsOnMethods = "testVerifyMetadata")
    public void testFirstVersionVerification() throws RegistryException,
                                                      RemoteException,
                                                      ResourceAdminServiceExceptionException,
                                                      RegistryExceptionException {

        /* first check point creation */
        resourceAdminServiceClient.setDescription(wsdlPath,
                                                  "verifying metadata:first check point");
        resourceAdminServiceClient.createVersion(wsdlPath);
        wsdlManager.updateWsdl(wsdl);

        /* restoring the last check point */
        boolean isRestored = false;

        VersionPath[] versionPaths = resourceAdminServiceClient
                .getVersionPaths(wsdlPath);

        for (VersionPath tmpPath : versionPaths) {

            if (resourceAdminServiceClient
                    .getMetadata(tmpPath.getCompleteVersionPath())
                    .getDescription()
                    .contains("verifying metadata:first check point")) {

                resourceAdminServiceClient.restoreVersion(tmpPath
                                                                  .getCompleteVersionPath());

                isRestored = true;
                resourceAdminServiceClient.setDescription(wsdlPath,
                                                          "verifying metadata:second check point");
            }
        }

        assertTrue(isRestored, "verification of first version restore");
    }

    /* second check point creation */
    @Test(groups = "wso2.greg", description = "metadata verisons verification", dependsOnMethods = "testFirstVersionVerification")
    public void testSecondVersionVerification() throws RemoteException,
                                                       ResourceAdminServiceExceptionException,
                                                       RegistryExceptionException,
                                                       GovernanceException {

        resourceAdminServiceClient.createVersion(wsdlPath);
        wsdlManager.updateWsdl(wsdl);

        /* restoring the second check point */
        boolean isRestored = false;

        VersionPath[] versionPaths2 = resourceAdminServiceClient
                .getVersionPaths(wsdlPath);

        for (VersionPath tmpPath : versionPaths2) {

            if (resourceAdminServiceClient
                    .getMetadata(tmpPath.getCompleteVersionPath())
                    .getDescription()
                    .contains("verifying metadata:second check point")) {

                resourceAdminServiceClient.restoreVersion(tmpPath
                                                                  .getCompleteVersionPath());

                isRestored = true;
            }
        }

        assertTrue(isRestored, "verification of the second version restore");

    }

    /* for verifying both of the versions */
    @Test(groups = "wso2.greg", description = "metadata verisons verification", dependsOnMethods = "testSecondVersionVerification")
    public void testVersionsVerification() throws RemoteException,
                                                  ResourceAdminServiceExceptionException {

        versionPaths = resourceAdminServiceClient.getVersionPaths(wsdlPath);
        assertEquals(versionPaths.length, 2,
                     "the number of versions created should be 2");
        boolean getSecondVersion = false;
        boolean getFirstVersion = false;

        for (VersionPath pathOfResource : versionPaths) {

            if (resourceAdminServiceClient
                    .getMetadata(pathOfResource.getCompleteVersionPath())
                    .getDescription()
                    .contentEquals("verifying metadata:second check point")) {
                getSecondVersion = true;
            }

            if (resourceAdminServiceClient
                    .getMetadata(pathOfResource.getCompleteVersionPath())
                    .getDescription()
                    .contentEquals("verifying metadata:first check point")) {
                getFirstVersion = true;
            }

        }

        assertTrue(getSecondVersion, "verified the new version is created");
        assertTrue(getFirstVersion, "verified the previous version is created");

    }

    /* restoring an older version */
    /* for verifying both of the versions */
    @Test(groups = "wso2.greg", description = "metadata verisons verification", dependsOnMethods = "testVersionsVerification")
    public void testRestoreOlderVersionsVerification() throws RemoteException,
                                                              ResourceAdminServiceExceptionException,
                                                              GovernanceException {

        boolean isRestoredOlderVersion = false;
        boolean isDeletedOlderVersion = false;
        for (VersionPath pathOfResource : versionPaths) {

            if (resourceAdminServiceClient
                    .getMetadata(pathOfResource.getCompleteVersionPath())
                    .getDescription()
                    .contentEquals("verifying metadata:first check point")) {
                resourceAdminServiceClient.restoreVersion(pathOfResource
                                                                  .getCompleteVersionPath());
                if (resourceAdminServiceClient.getMetadata(wsdlPath)
                        .getDescription()
                        .contains("verifying metadata:first check point")) {
                    isRestoredOlderVersion = true;
                }

                resourceAdminServiceClient.deleteVersionHistory(
                        pathOfResource.getActiveResourcePath(),
                        String.valueOf(pathOfResource.getVersionNumber()));
                //wsdlManager.updateWsdl(wsdl);
                int len;

                versionPaths = resourceAdminServiceClient
                        .getVersionPaths(wsdlPath);

                len = versionPaths.length;

                assertEquals(len, 1, "the number of versions should be 1");

                //the for-loop iterates once
                boolean onlySecondVersionExist = false;
                if (len == 1) {
                    for (VersionPath tmpVersionPath : versionPaths) {
                        onlySecondVersionExist = resourceAdminServiceClient
                                .getMetadata(tmpVersionPath.getCompleteVersionPath())
                                .getDescription()
                                .contentEquals("verifying metadata:second check point");
                    }
                }

                if (resourceAdminServiceClient.getMetadata(wsdlPath)
                        .getDescription().contentEquals("verifying metadata:first check point")) {
                    if (onlySecondVersionExist) {
                        isDeletedOlderVersion = true;            //if both conditions are met
                    }

                }

            }

        }

        assertTrue(isRestoredOlderVersion,
                   "older version is not restored");
        assertTrue(isDeletedOlderVersion, "older version is not deleted");

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
            wsRegistry.delete(pathPrefix + tmpEndpoint.getPath());
        }
        wsRegistry = null;
        wsdl = null;
        wsdlManager = null;
        resourceAdminServiceClient = null;
        environment = null;


    }
}
