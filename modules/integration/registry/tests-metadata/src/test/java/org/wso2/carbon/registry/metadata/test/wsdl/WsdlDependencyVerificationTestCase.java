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
import org.wso2.carbon.automation.api.clients.registry.RelationAdminServiceClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.registry.relations.stub.AddAssociationRegistryExceptionException;
import org.wso2.carbon.registry.relations.stub.beans.xsd.AssociationBean;
import org.wso2.carbon.registry.relations.stub.beans.xsd.DependenciesBean;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;

import java.net.MalformedURLException;
import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;

/**
 * uses admin service clients to do so
 */
public class WsdlDependencyVerificationTestCase {

    private ManageEnvironment environment;
    private UserInfo userInfo;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private final String amazonWsdlPath = "/_system/governance/trunk/wsdls/com/amazon/soap/AmazonWebServices.wsdl";
    private final String automatedWsdlPath = "/_system/governance/trunk/wsdls/com/strikeiron/www/Automated.wsdl";
    private final String bizWsdlPath = "/_system/governance/trunk/wsdls/com/foo/BizService.wsdl";
    private final String associatedServicePathBiz = "/_system/governance/trunk/services/com/foo/BizService";
    private final String associatedEndPointPathBiz = "/_system/governance/trunk/endpoints/localhost/axis2/services/ep-BizService";
    private final String associatedSchemaPathBiz = "/_system/governance/trunk/schemas/org/bar/purchasing/purchasing.xsd";
    private final String associatedServicePathAutomated = "/_system/governance/trunk/services/com/strikeiron/www/DoNotCallRegistry";
    private final String associatedEndpointPathAutomated = "/_system/governance/trunk/endpoints/com/strikeiron/ws/strikeiron/donotcall2_5/ep-DoNotCallRegistry";
    private final String associatedServicePathAmazon = "/_system/governance/trunk/services/com/amazon/soap/AmazonSearchService";
    private final String associatedEndpointPathAmazon = "/_system/governance/trunk/endpoints/com/amazon/soap/onca/ep-soap2";


    @BeforeClass(groups = "wso2.greg", alwaysRun = true)
    public void initialize() throws LoginAuthenticationExceptionException,
                                    RemoteException {
        int userId = 2;
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        environment = builder.build();
        userInfo = UserListCsvReader.getUserInfo(userId);
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                               environment.getGreg().getSessionCookie());
    }

    /**
     * for wsdl dependency verification
     */
    @Test(groups = "wso2.greg", description = "wsdl addition for dependency Verification")
    public void testAddResourcesToVerifyDependency() throws RemoteException,
                                                            MalformedURLException,
                                                            ResourceAdminServiceExceptionException {

        resourceAdminServiceClient
                .addWSDL(
                        "AmazonWebServices",
                        "for the dependency verification",
                        "https://svn.wso2.org/repos/wso2/carbon/platform/trunk/"
                        + "platform-integration/platform-automated-test-suite/org.wso2.carbon.automation.test.repo/"
                        + "src/main/resources/artifacts/GREG/wsdl/AmazonWebServices.wsdl");

        resourceAdminServiceClient
                .addWSDL(
                        "Automated",
                        "a dependency of AmazonWebServices",
                        "https://svn.wso2.org/repos/wso2/carbon/platform/trunk/platform-integration/"
                        + "platform-automated-test-suite/org.wso2.carbon.automation.test.repo/src/main/resources/artifacts"
                        + "/GREG/wsdl/Automated.wsdl");

        resourceAdminServiceClient
                .addWSDL(
                        "BizService",
                        "a dependency of AmazonWebServices",
                        "https://svn.wso2.org/repos/wso2/carbon/platform/trunk/"
                        + "platform-integration/platform-automated-test-suite/org.wso2.carbon.automation.test.repo/"                        
                        + "src/main/resources/artifacts/GREG/wsdl/BizService.wsdl");


    }

    @Test(groups = "wso2.greg", description = "Dependency Verification", dependsOnMethods = "testAddResourcesToVerifyDependency")
    public void testVerifyDependency() throws RemoteException,
                                              MalformedURLException,
                                              ResourceAdminServiceExceptionException,
                                              AddAssociationRegistryExceptionException {

        RelationAdminServiceClient relationAdminServiceClient = new RelationAdminServiceClient(
                environment.getGreg().getProductVariables().getBackendUrl(),
                environment.getGreg().getSessionCookie());

        relationAdminServiceClient
                .addAssociation(
                        "/_system/governance/trunk/wsdls/com/amazon/soap/AmazonWebServices.wsdl",
                        "depends",
                        "/_system/governance/trunk/wsdls/com/strikeiron/www/Automated.wsdl",
                        "add");


        relationAdminServiceClient
                .addAssociation(
                        "/_system/governance/trunk/wsdls/com/amazon/soap/AmazonWebServices.wsdl",
                        "depends",
                        "/_system/governance/trunk/wsdls/com/foo/BizService.wsdl",
                        "add");
        DependenciesBean dependenciesBean = relationAdminServiceClient
                .getDependencies("/_system/governance/trunk/wsdls/com/amazon/soap/AmazonWebServices.wsdl");

        AssociationBean[] associationBean = dependenciesBean
                .getAssociationBeans();

        boolean dependency1 = false;
        boolean dependency2 = false;
        boolean dependency3 = false;

        for (AssociationBean tmpAssociationBean : associationBean) {
            if (tmpAssociationBean
                    .getDestinationPath()
                    .contentEquals(
                            "/_system/governance/trunk/wsdls/com/strikeiron/www/Automated.wsdl")) {
                dependency1 = true;

            }
            if (tmpAssociationBean.getDestinationPath().contentEquals(
                    "/_system/governance/trunk/wsdls/com/foo/BizService.wsdl")) {
                dependency2 = true;

            }

        }

        assertTrue(dependency1, "verifies the dependency Automation wsdl");
        assertTrue(dependency2, "verifies the dependency BizService wsdl ");


    }

    @AfterClass(groups = "wso2.greg", alwaysRun = true, description = "cleaning up the artifacts added")
    public void tearDown() throws GovernanceException, RemoteException,
                                  ResourceAdminServiceExceptionException {
        resourceAdminServiceClient.deleteResource(associatedServicePathAmazon);
        resourceAdminServiceClient
                .deleteResource(amazonWsdlPath);
        resourceAdminServiceClient.deleteResource(associatedEndpointPathAmazon);

        resourceAdminServiceClient.deleteResource(associatedServicePathAutomated);
        resourceAdminServiceClient
                .deleteResource(automatedWsdlPath);
        resourceAdminServiceClient.deleteResource(associatedEndpointPathAutomated);

        resourceAdminServiceClient.deleteResource(associatedServicePathBiz);
        resourceAdminServiceClient.deleteResource(associatedSchemaPathBiz);
        resourceAdminServiceClient.deleteResource(bizWsdlPath);
        resourceAdminServiceClient.deleteResource(associatedEndPointPathBiz);


        environment = null;
        resourceAdminServiceClient = null;

    }
}
