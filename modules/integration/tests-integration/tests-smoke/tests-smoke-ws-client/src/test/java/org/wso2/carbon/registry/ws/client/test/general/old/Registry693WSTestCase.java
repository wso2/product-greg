/*
 *  Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.registry.ws.client.test.general.old;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.governance.api.schema.SchemaManager;
import org.wso2.carbon.governance.api.schema.dataobjects.Schema;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import static org.testng.Assert.assertEquals;

public class
        Registry693WSTestCase extends GREGIntegrationBaseTest {

    private WSRegistryServiceClient registry;
    private String USER_NAME;

    @BeforeClass(groups = {"wso2.greg"})
    public void initTest() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        registry = registryProviderUtil.getWSRegistry(automationContext);
        String userName = automationContext.getContextTenant().getContextUser().getUserName();
        if(userName.contains("@")) {
            USER_NAME = userName.substring(0, userName.indexOf('@'));
        } else {
            USER_NAME = userName;
        }
    }

    @Test(groups = {"wso2.greg"})
    public void addSchema() throws RegistryException {
        SchemaManager manager = new SchemaManager(GovernanceUtils.getGovernanceUserRegistry(registry, USER_NAME));
        Schema schema = manager.newSchema("https://raw.githubusercontent.com/wso2/wso2-qa-artifacts/master/automation-artifacts/greg/xsd/2008_01GeoIPService.svc.xsd");
        int currentSchemaLength = manager.getAllSchemas().length;
        manager.addSchema(schema);
        // The insertion will add 2 schemas, so the length should increase by 2.
        assertEquals(manager.getAllSchemas().length, currentSchemaLength + 2, "Invalid number of schemas on registry");
    }

    @AfterClass
    public void cleanup() throws RegistryException {
        registry.delete("/_system/governance/trunk/schemas");
    }
}
