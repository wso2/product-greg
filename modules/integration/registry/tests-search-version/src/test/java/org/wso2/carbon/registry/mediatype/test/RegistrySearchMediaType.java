/*
 * Copyright 2004,2005 The Apache Software Foundation.
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

package org.wso2.carbon.registry.mediatype.test;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.api.endpoints.EndpointManager;
import org.wso2.carbon.governance.api.endpoints.dataobjects.Endpoint;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import javax.activation.DataHandler;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;


public class RegistrySearchMediaType {

    private ResourceAdminServiceClient resourceAdminServiceClient;
    private Registry governance;
    private WSRegistryServiceClient wsRegistry;

    @BeforeClass
    public void init() throws Exception {
        int userId = 2;
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        ManageEnvironment environment = builder.build();
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                               environment.getGreg().getSessionCookie());
        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        wsRegistry = registryProviderUtil.getWSRegistry(userId,
                                                        ProductConstant.GREG_SERVER_NAME);
        governance = registryProviderUtil.getGovernanceRegistry(wsRegistry, userId);

    }

    @Test(groups = {"wso2.greg"}, description = "Mediatype search", dependsOnMethods = {"searchWSDLMediaType"})
    public void SearchForMediaType()
            throws RemoteException, MalformedURLException, ResourceAdminServiceExceptionException {


        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
                              File.separator + "GREG" + File.separator + "resource.txt";

        DataHandler dh = new DataHandler(new URL("file:///" + resourcePath));
        resourceAdminServiceClient.addResource("/_system/config/testResource", "text/plain", "testDesc", dh);
        assertTrue(resourceAdminServiceClient.getMetadata("/_system/config/testResource").getMediaType().equals("text/plain"));
    }


    @Test(groups = {"wso2.greg"}, description = "Mediatype search")
    public void searchWSDLMediaType()
            throws IOException, ResourceAdminServiceExceptionException, RegistryException {
        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator +
                              "GREG" + File.separator + "wsdl" + File.separator + "AmazonWebServices.wsdl";

        DataHandler dh = new DataHandler(new URL("file:///" + resourcePath));

        resourceAdminServiceClient.addResource("/_system/governance/trunk/wsdl/AmazonWebServices.wsdl",
                                               "application/wsdl+xml", "desc", dh);


        assertTrue(resourceAdminServiceClient.getMetadata("/_system/governance/trunk/wsdls/com/amazon" +
                                                          "/soap/AmazonWebServices.wsdl").getMediaType().equals("application/wsdl+xml"));


    }

    @Test(groups = {"wso2.greg"}, description = "Mediatype search", dependsOnMethods = {"SearchForMediaType"})
    public void searchWSDLSymLinkMediaType()
            throws IOException, ResourceAdminServiceExceptionException, RegistryException {

        resourceAdminServiceClient.addSymbolicLink("/", "AmazonWebServices.wsdl",
                                                   "/_system/governance/trunk/wsdls/com/amazon/soap/AmazonWebServices.wsdl");

        assertTrue(resourceAdminServiceClient.
                getMetadata("/AmazonWebServices.wsdl").getMediaType().equals("application/wsdl+xml"));


    }


    @Test(groups = {"wso2.greg"}, description = "Mediatype search", dependsOnMethods = {"searchWSDLSymLinkMediaType"})
    public void searchSchemaMediaType()
            throws RemoteException, MalformedURLException, ResourceAdminServiceExceptionException {

        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
                              File.separator + "GREG" + File.separator + "schema" + File.separator + "library.xsd";

        DataHandler dh = new DataHandler(new URL("file:///" + resourcePath));
        resourceAdminServiceClient.addSchema("adding schema", dh);
        assertTrue(resourceAdminServiceClient.getMetadata("/_system/governance/trunk/schemas/com/exam" +
                                                          "ple/www/library/library.xsd").getMediaType().equals("application/x-xsd+xml"));

    }

    @Test(groups = {"wso2.greg"}, description = "Mediatype search", dependsOnMethods = "searchSchemaMediaType")
    public void searchSchemaSymLinkMediaType()
            throws RemoteException, MalformedURLException, ResourceAdminServiceExceptionException {
        resourceAdminServiceClient.addSymbolicLink("/", "library.xsd",
                                                   "/_system/governance/trunk/schemas/com/example/www/library/library.xsd");
        assertTrue(resourceAdminServiceClient.getMetadata("/library.xsd").getMediaType().equals("application/x-xsd+xml"));

    }

    @Test(groups = {"wso2.greg"}, description = "Mediatype search", dependsOnMethods = "searchSchemaSymLinkMediaType")
    public void searchServiceMediaType()
            throws RemoteException, MalformedURLException, ResourceAdminServiceExceptionException {

        assertTrue(resourceAdminServiceClient.getMetadata("/_system/governance/trunk/services/com/amazon" +
                                                          "/soap/AmazonSearchService").getMediaType().
                equals("application/vnd.wso2-service+xml"));
    }

    @Test(groups = {"wso2.greg"}, description = "Mediatype search", dependsOnMethods = {"searchServiceMediaType"})
    public void searchPolicyMediaType()
            throws ResourceAdminServiceExceptionException, RemoteException, MalformedURLException {
        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
                              File.separator + "GREG" + File.separator + "policy" + File.separator + "policy.xml";
        DataHandler dh = new DataHandler(new URL("file:///" + resourcePath));
        resourceAdminServiceClient.addPolicy("desc", dh);
        assertTrue(resourceAdminServiceClient.getMetadata("/_system/governance/trunk/policies/policy.xml").
                getMediaType().equals("application/policy+xml"));
    }

    @Test(groups = {"wso2.greg"}, description = "Mediatype search", dependsOnMethods = {"searchPolicyMediaType"})
    public void searchEndPointMediaType()
            throws GovernanceException, ResourceAdminServiceExceptionException, RemoteException {
        EndpointManager endpointManager = new EndpointManager(governance);
        Endpoint endpoint = endpointManager.newEndpoint("http://wso2.carbon.automation/test");
        endpointManager.addEndpoint(endpoint);


        String endPointPath = endpoint.getPath();

        assertTrue(resourceAdminServiceClient.getMetadata("/_system/governance" + endPointPath).
                getMediaType().equals("application/vnd.wso2.endpoint"));

    }

    @AfterClass
    public void clean()
            throws ResourceAdminServiceExceptionException, RemoteException, RegistryException {

        delete("/_system/governance/trunk/policies/policy.xml");
        delete("/_system/config/testResource");
        delete("/_system/governance/trunk/endpoints/automation");
        delete("/_system/governance/trunk/services/com/amazon/soap/AmazonSearchService");
        delete("/_system/governance/trunk/wsdls/com/amazon/soap/AmazonWebServices.wsdl");
        delete("/_system/governance/trunk/schemas/com/example/www/library/library.xsd");

        resourceAdminServiceClient = null;
        governance = null;
        wsRegistry = null;
    }

    public void delete(String destPath)
            throws ResourceAdminServiceExceptionException, RemoteException, RegistryException {
        if (wsRegistry.resourceExists(destPath)) {
            resourceAdminServiceClient.deleteResource(destPath);
        }
    }


}

