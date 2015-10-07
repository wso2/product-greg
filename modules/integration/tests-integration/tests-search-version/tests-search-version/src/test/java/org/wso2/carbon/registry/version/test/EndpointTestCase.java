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


package org.wso2.carbon.registry.version.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.governance.api.endpoints.EndpointManager;
import org.wso2.carbon.governance.api.endpoints.dataobjects.Endpoint;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.beans.xsd.VersionPath;
import org.wso2.carbon.registry.version.test.utils.VersionUtils;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.clients.ListMetaDataServiceClient;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import java.rmi.RemoteException;

import static junit.framework.Assert.assertEquals;
import static org.testng.Assert.assertTrue;


public class EndpointTestCase extends GREGIntegrationBaseTest {

    private static Registry governance = null;
    private ResourceAdminServiceClient resourceAdminClient;
    private static final Log log = LogFactory.getLog(ListMetaDataServiceClient.class);
    private static WSRegistryServiceClient registry = null;
    private String PATH_CONSTANT = "/_system/governance";
    private String sessionCookie;
    private String backEndUrl;
    private String userName;
    private String userNameWithoutDomain;
    private String endpointPath;


    @BeforeClass
    public void initializeRegistry()
            throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        backEndUrl = getBackendURL();
        sessionCookie = getSessionCookie();
        userName = automationContext.getContextTenant().getContextUser().getUserName();

        if (userName.contains("@"))
            userNameWithoutDomain = userName.substring(0, userName.indexOf('@'));
        else
            userNameWithoutDomain = userName;

        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        resourceAdminClient =
                new ResourceAdminServiceClient(backEndUrl,
                                               sessionCookie);
        registry = registryProviderUtil.getWSRegistry(automationContext);
        governance = registryProviderUtil.getGovernanceRegistry(registry, automationContext);
    }

    /*
   create a new End point and
    version it
    */


    @Test(groups = {"wso2.greg"}, description = "Create a new endpoint")
    public void testAddEndpoint()
            throws RegistryException, ResourceAdminServiceExceptionException, RemoteException {
        String endpoint_url = "http://ws.strikeiron.com/StrikeIron/donotcall2_5/DoNotCallRegistryUnique";

        Endpoint endpoint = createEndpoint(endpoint_url);
        assertTrue(registry.resourceExists(PATH_CONSTANT + endpoint.getPath()), "Endpoint Resource Does not exists :");
        endpointPath = PATH_CONSTANT + endpoint.getPath();
        VersionUtils.deleteAllVersions(resourceAdminClient, PATH_CONSTANT + endpoint.getPath());
        registry.createVersion(PATH_CONSTANT + endpoint.getPath());
        VersionPath[] vp = resourceAdminClient.getVersionPaths(PATH_CONSTANT + endpoint.getPath());
        assertEquals(1, vp.length);
    }


    private Endpoint createEndpoint(String endpoint_url) throws GovernanceException {
        EndpointManager endpointManager = new EndpointManager(governance);

        Endpoint endpoint1;
        try {
            endpoint1 = endpointManager.newEndpoint(endpoint_url);
            endpoint1.addAttribute("status1", "QA");
            endpoint1.addAttribute("status2", "Dev");
            endpointManager.addEndpoint(endpoint1);
            log.info("Endpoint was successfully added");
        } catch (GovernanceException e) {
            log.error("Unable add Endpoint:" + e);
            throw new GovernanceException("Unable to add Endpoint:" + e);
        }
        return endpoint1;
    }


    @AfterClass
    public void clear() throws RegistryException {
        EndpointManager endpointManager = new EndpointManager(governance);
        Endpoint endpoint = endpointManager.getEndpointByUrl("http://ws.strikeiron.com/StrikeIron/donotcall2_5/DoNotCallRegistryUnique");
        System.out.println(endpointPath);
        registry.delete(endpointPath);
        //System.out.println(endpoint.getId());
        //System.out.println(endpoint.getPath());
        //endpointManager.removeEndpoint(endpoint.getId());
        governance = null;
        resourceAdminClient = null;
        registry = null;
    }

}
