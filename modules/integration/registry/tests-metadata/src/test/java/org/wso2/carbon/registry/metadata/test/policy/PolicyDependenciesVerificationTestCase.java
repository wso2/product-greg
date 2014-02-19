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
import org.wso2.carbon.automation.api.clients.registry.RelationAdminServiceClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.registry.relations.stub.AddAssociationRegistryExceptionException;
import org.wso2.carbon.registry.relations.stub.beans.xsd.AssociationBean;
import org.wso2.carbon.registry.relations.stub.beans.xsd.DependenciesBean;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;

import javax.activation.DataHandler;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;

/**
 * this classes uses Admin services to do so
 */
public class PolicyDependenciesVerificationTestCase {
    private ManageEnvironment environment;
    private UserInfo userInfo;
    private ResourceAdminServiceClient resourceAdminServiceClient;

    @BeforeClass(groups = "wso2.greg", alwaysRun = true)
    public void initialize() throws LoginAuthenticationExceptionException,
            RemoteException {
        int userId = 2;
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        environment = builder.build();
        userInfo = UserListCsvReader.getUserInfo(userId);
        resourceAdminServiceClient = new ResourceAdminServiceClient(environment
                .getGreg().getProductVariables().getBackendUrl(),
                environment.getGreg().getSessionCookie());
    }

    @Test(groups = "wso2.greg", description = "Policy addition for dependency Verification")
    public void testAddResourcesToVerifyDependencies() throws RemoteException,
            MalformedURLException,
            ResourceAdminServiceExceptionException {

        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION
                + "artifacts" + File.separator + "GREG" + File.separator
                + "policy" + File.separator + "policy.xml"; // the path

        String associationResourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION
                + "artifacts"
                + File.separator
                + "GREG"
                + File.separator
                + "policy" + File.separator + "UTPolicy.xml"; // the path

        DataHandler dhReso = new DataHandler(new URL("file:///" + resourcePath));
        DataHandler dhAsso = new DataHandler(new URL("file:///"
                + associationResourcePath));

        resourceAdminServiceClient.addPolicy(
                "Policy add from the the file system", dhReso);
        resourceAdminServiceClient.addPolicy(
                "Policy add from the the file system", dhAsso);

    }

    @Test(groups = "wso2.greg", description = "Dependency Verification", dependsOnMethods = "testAddResourcesToVerifyDependencies")
    public void testVerifyAssociation() throws RemoteException,
            MalformedURLException,
            ResourceAdminServiceExceptionException,
            AddAssociationRegistryExceptionException {

        RelationAdminServiceClient relationAdminServiceClient = new RelationAdminServiceClient(
                environment.getGreg().getProductVariables().getBackendUrl(),
                environment.getGreg().getSessionCookie());

        relationAdminServiceClient.addAssociation(
                "/_system/governance/trunk/policies/policy.xml", "depends",
                "/_system/governance/trunk/policies/UTPolicy.xml", "add");

        DependenciesBean dependenciesBean = relationAdminServiceClient
                .getDependencies("/_system/governance/trunk/policies/policy.xml");

        AssociationBean[] associationBean = dependenciesBean
                .getAssociationBeans();

        boolean status = false;

        for (AssociationBean tmpAssociationBean : associationBean) {
            if (tmpAssociationBean.getDestinationPath().contentEquals(
                    "/_system/governance/trunk/policies/UTPolicy.xml")) {
                status = true;

            }
        }

        assertTrue(status, "verifies the dependency");

    }

    @AfterClass(groups = "wso2.greg", alwaysRun = true, description = "cleaning up the artifacts added")
    public void tearDown() throws GovernanceException, RemoteException,
            ResourceAdminServiceExceptionException {

        resourceAdminServiceClient
                .deleteResource("/_system/governance/trunk/policies/UTPolicy.xml");

        resourceAdminServiceClient
                .deleteResource("/_system/governance/trunk/policies/policy.xml");
        resourceAdminServiceClient = null;
        environment = null;
    }
}
