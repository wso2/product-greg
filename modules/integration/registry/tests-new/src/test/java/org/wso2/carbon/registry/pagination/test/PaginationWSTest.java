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

package org.wso2.carbon.registry.pagination.test;


import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.client.WSRegistrySearchClient;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.pagination.PaginationContext;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertTrue;

public class PaginationWSTest {
    private WSRegistryServiceClient registry;
    private ManageEnvironment environment;

    @BeforeClass(alwaysRun = true)
    public void initTest() throws Exception {
        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        int userId = ProductConstant.ADMIN_USER_ID;
        registry = registryProviderUtil.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        environment = builder.build();
    }
    @Test(groups = {"wso2.greg"})
    public void testPaginate() throws Exception {

        try {

            Registry gov = GovernanceUtils.getGovernanceUserRegistry(registry, "admin");
            // Should be load the governance artifact.
            GovernanceUtils.loadGovernanceArtifacts((UserRegistry) gov);
            addServices(gov);
            //Initialize the pagination context.
            //Top five services, sortBy name , and sort order descending.
            PaginationContext.init(0, 5, "DES", "overview_name", 100);
            WSRegistrySearchClient wsRegistrySearchClient = new WSRegistrySearchClient(
                    environment.getGreg().getServiceUrl(),environment.getGreg().getSessionCookie());
            //This should be execute to initialize the AttributeSearchService.
            wsRegistrySearchClient.init();
            //Initialize the GenericArtifactManager
            GenericArtifactManager artifactManager =
                    new GenericArtifactManager(gov, "service");
            Map<String, List<String>> listMap = new HashMap<String, List<String>>();
            //Create the search attribute map
            listMap.put("lcName", new ArrayList<String>() {{
                add("ServiceLifeCycle");
            }});
            listMap.put("lcState", new ArrayList<String>() {{
                add("Development");
            }});
            //Find the results.
            GenericArtifact[] genericArtifacts = artifactManager.findGenericArtifacts(listMap);

            assertTrue(genericArtifacts.length >0 ,"No any service found");
            assertTrue(genericArtifacts.length == 5,"Filtered service count should be 5");

            assertTrue(genericArtifacts[0].getQName().getLocalPart().equals("FlightService9"),
                    "filter results are not sorted");
            assertTrue(genericArtifacts[4].getQName().getLocalPart().equals("FlightService5"),
                    "filter results are not sorted");
        } finally {
            PaginationContext.destroy();
        }
    }

    private static void addServices(Registry govRegistry) throws RegistryException {
        GenericArtifactManager artifactManager = new GenericArtifactManager(govRegistry, "service");
        for (int i = 1; i < 10; i++) {
            GenericArtifact artifact =
                    artifactManager.newGovernanceArtifact(new QName("ns", "FlightService" + i));
            artifactManager.addGenericArtifact(artifact);
        }
        //Services need to be index before search.
        try {
            Thread.sleep(2 * 60 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
