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

package org.wso2.carbon.registry.metadata.test.policy;

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
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.policies.PolicyManager;
import org.wso2.carbon.governance.api.policies.dataobjects.Policy;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.info.stub.RegistryExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.beans.xsd.VersionPath;
import org.wso2.carbon.registry.version.test.utils.VersionUtils;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import java.net.MalformedURLException;
import java.rmi.RemoteException;

import static org.testng.Assert.*;

public class PolicyMetadataVerificationTestCase {

    private ManageEnvironment environment;
    private Policy policy;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private PolicyManager policyManager;
    private String policyPath;
    private VersionPath[] versionPaths;

    @BeforeClass(groups = "wso2.greg", alwaysRun = true)
    public void initialize() throws RemoteException,
            LoginAuthenticationExceptionException,
            org.wso2.carbon.registry.api.RegistryException {
        int userId = 3;
        UserInfo userInfo;
        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        WSRegistryServiceClient wsRegistryServiceClient =
                registryProviderUtil.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);

        Registry governanceRegistry =
                registryProviderUtil.getGovernanceRegistry(wsRegistryServiceClient, userId);

        EnvironmentBuilder builder =
                new EnvironmentBuilder().greg(userId);

        environment = builder.build();

        userInfo = UserListCsvReader.getUserInfo(userId);

        EnvironmentBuilder adminBuilder =
                new EnvironmentBuilder().greg(userId);

        ManageEnvironment environmentAdmin = adminBuilder.build();

        resourceAdminServiceClient =
                new ResourceAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                               environmentAdmin.getGreg().getSessionCookie());
        policyManager = new PolicyManager(governanceRegistry);
    }

    /**
     * Metadata verification
     */
    @Test(groups = "wso2.greg", description = "add policy")
    public void testAddPolicy() throws RemoteException,
            ResourceAdminServiceExceptionException, GovernanceException,
            MalformedURLException {

        policy = policyManager
                .newPolicy("https://svn.wso2.org/repos/wso2/carbon/platform/"
                        + "trunk/platform-integration/platform-automated-test-suite/"
                        + "org.wso2.carbon.automation.test.repo/src/main/resources/"
                        + "artifacts/GREG/policy/UTPolicy.xml");

        policy.addAttribute("version", "1.0.0");
        policy.addAttribute("author", "Aparna");
        policy.addAttribute("description",
                "added valid policy to verify the check point");
        policyManager.addPolicy(policy);
        policyManager.updatePolicy(policy);
        assertNotNull(policy);
        assertTrue(policy.getAttribute("author").contentEquals("Aparna"));
        policyPath = "/_system/governance" + policy.getPath();

        VersionUtils.deleteAllVersions(resourceAdminServiceClient, policyPath);

    }

    @Test(groups = "wso2.greg", description = "metadata verification", dependsOnMethods = "testAddPolicy")
    public void testVerifyMetadata() throws RemoteException,
            LoginAuthenticationExceptionException,
            GovernanceException,
            ResourceAdminServiceExceptionException {

        assertTrue(resourceAdminServiceClient
                .getMetadata("/_system/governance" + policy.getPath())
                .getMediaType().contains("application/policy+xml")); // mediatype
        // verified

        assertNull(resourceAdminServiceClient.getMetadata(policyPath)
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
        resourceAdminServiceClient.setDescription(policyPath,
                "verifying metadata:first check point");
        resourceAdminServiceClient.createVersion(policyPath);
        policyManager.updatePolicy(policy);

        /* restoring the last check point */
        boolean isRestored = false;

        VersionPath[] versionPaths = resourceAdminServiceClient
                .getVersionPaths(policyPath);

        for (VersionPath tmpPath : versionPaths) {

            if (resourceAdminServiceClient
                    .getMetadata(tmpPath.getCompleteVersionPath())
                    .getDescription()
                    .contains("verifying metadata:first check point")) {

                resourceAdminServiceClient.restoreVersion(tmpPath
                        .getCompleteVersionPath());

                isRestored = true;
                resourceAdminServiceClient.setDescription(policyPath,
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

        resourceAdminServiceClient.createVersion(policyPath);
        policyManager.updatePolicy(policy);

        /* restoring the second check point */
        boolean isRestored = false;

        VersionPath[] versionPaths2 = resourceAdminServiceClient
                .getVersionPaths(policyPath);

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

        versionPaths = resourceAdminServiceClient.getVersionPaths(policyPath);
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

            if (resourceAdminServiceClient.getMetadata(pathOfResource.getCompleteVersionPath())
                    .getDescription().contentEquals("verifying metadata:first check point")) {
                resourceAdminServiceClient.restoreVersion(pathOfResource.getCompleteVersionPath());

                if (resourceAdminServiceClient.getMetadata(policyPath)
                        .getDescription()
                        .contains("verifying metadata:first check point")) {
                    isRestoredOlderVersion = true;
                }

                resourceAdminServiceClient.deleteVersionHistory(
                        pathOfResource.getActiveResourcePath(),
                        String.valueOf(pathOfResource.getVersionNumber()));
                // policyManager.updatePolicy(policy);
                int len;

                versionPaths = resourceAdminServiceClient
                        .getVersionPaths(policyPath);
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

                if (resourceAdminServiceClient.getMetadata(policyPath)
                        .getDescription().contentEquals("verifying metadata:first check point")) {
                    if (onlySecondVersionExist) {
                        isDeletedOlderVersion = true;            //if both conditions are met
                    }

                }

            }

        }

        assertTrue(isRestoredOlderVersion,
                "the older version is not restored");
        assertTrue(isDeletedOlderVersion, "older version is not deleted");

    }

    @AfterClass(groups = "wso2.greg", alwaysRun = true, description = "cleaning up the artifacts added")
    public void tearDown() throws GovernanceException {
        policyManager.removePolicy(policy.getId());
        policy = null;
        policyManager = null;
        resourceAdminServiceClient = null;
        environment = null;
    }
}
