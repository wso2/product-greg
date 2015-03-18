/*
 *Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.metadata.test.wadl;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.governance.api.schema.SchemaManager;
import org.wso2.carbon.governance.api.schema.dataobjects.Schema;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.beans.xsd.ContentBean;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import java.rmi.RemoteException;

public class AddWADLTestCase extends GREGIntegrationBaseTest{
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private Registry governanceRegistry;
    private String sessionCookie;

    @BeforeClass(groups = "wso2.greg", alwaysRun = true)
    public void initialize() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        sessionCookie = new LoginLogoutClient(automationContext).login();

        resourceAdminServiceClient =
                new ResourceAdminServiceClient(backendURL, sessionCookie);

        RegistryProviderUtil provider = new RegistryProviderUtil();
        WSRegistryServiceClient wsRegistry = provider.getWSRegistry(automationContext);
        governanceRegistry = provider.getGovernanceRegistry(wsRegistry, automationContext);
    }

    @Test(groups = "wso2.greg", description = "wadl addition")
    public void addWADLFromURL() throws Exception{
        String wadlPath = "/_system/governance/trunk/wadls/com/sun/research/wadl/_2006/_10/1.0.0/StorageService";

        resourceAdminServiceClient.addWADL("StorageService", "Adding simple WADL",
                "https://svn.wso2.org/repos/wso2/trunk/commons/qa/qa-artifacts/greg/wadl/StorageService.wadl");

        ContentBean wadlContentBean = resourceAdminServiceClient.getResourceContent(wadlPath);
        Assert.assertNotNull(wadlContentBean);

        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governanceRegistry);

        ServiceManager serviceManager = new ServiceManager(governanceRegistry);
        Service[] services = serviceManager.getAllServices();

        Assert.assertEquals(services.length, 1, "More than one services created after adding WADL");

        boolean serviceAvailable = false;
        for(Service service : services){
            if(service.getQName().getLocalPart().equals("StorageService")){
                serviceAvailable = true;
                serviceManager.removeService(service.getId());
            }
        }
        Assert.assertTrue(serviceAvailable, "Service not created for the WADL");
    }

    @Test(groups = "wso2.greg", description = "adding wadl with schema import", dependsOnMethods = "addWADLFromURL")
    public void addWADLWithSchemaImport() throws Exception {
        String wadlPath = "/_system/governance/trunk/wadls/net/java/dev/wadl/_2009/_02/1.0.0/SearchSearvice";

        resourceAdminServiceClient.addWADL("SearchSearvice", "Adding simple WADL with schema import",
                        "https://svn.wso2.org/repos/wso2/trunk/commons/qa/qa-artifacts/greg/wadl/SearchSearvice.wadl");

        ContentBean wadlContentBean = resourceAdminServiceClient.getResourceContent(wadlPath);
        Assert.assertNotNull(wadlContentBean);

        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governanceRegistry);

        ServiceManager serviceManager = new ServiceManager(governanceRegistry);
        Service[] services = serviceManager.getAllServices();

        Assert.assertEquals(services.length, 1, "More than one services created after adding WADL");

        boolean serviceAvailable = false;
        for(Service service : services){
            if(service.getQName().getLocalPart().equals("SearchSearvice")){
                serviceAvailable = true;
                serviceManager.removeService(service.getId());
            }
        }
        Assert.assertTrue(serviceAvailable, "Service not created for the WADL");

        SchemaManager schemaManager = new SchemaManager(governanceRegistry);
        Schema[] schemas = schemaManager.getAllSchemas();
        boolean schemaAvailable = false;
        for (Schema schema : schemas){
            if(schema.getQName().getLocalPart().equals("purchasing.xsd")){
                schemaAvailable = true;
                schemaManager.removeSchema(schema.getId());
            }
        }
        Assert.assertTrue(schemaAvailable, "Schema not imported for the WADL");
    }

    @Test(groups = "wso2.greg", description = "adding invalid schema importing wadl",
            dependsOnMethods = "addWADLWithSchemaImport")
    public void addInvalidSchemaImportWADL() throws Exception {
        try {
            resourceAdminServiceClient.addWADL("InvalidSchemaImportWADL", "Adding WADL with invalid schema import",
                            "http://svn.wso2.org/repos/wso2/trunk/commons/qa/qa-artifacts/greg/wadl/InvalidSchemaImport.wadl");
        } catch (Exception e){
            if (!e.getMessage().contains("WADL not found")) {
                Assert.assertTrue(false, "");
            }
        }

    }

    @AfterClass(groups = { "wso2.greg" })
    public void deleteResources()
            throws ResourceAdminServiceExceptionException, RemoteException {
        resourceAdminServiceClient.deleteResource(
                "/_system/governance/trunk/wadls/com/sun/research/wadl/_2006/_10/1.0.0/StorageService");
        resourceAdminServiceClient.deleteResource(
                "/_system/governance/trunk/wadls/net/java/dev/wadl/_2009/_02/1.0.0/SearchSearvice");
        resourceAdminServiceClient = null;
    }
}
