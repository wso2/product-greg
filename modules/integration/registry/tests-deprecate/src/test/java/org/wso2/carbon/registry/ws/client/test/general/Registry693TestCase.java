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
package org.wso2.carbon.registry.ws.client.test.general;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.governance.api.schema.SchemaManager;
import org.wso2.carbon.governance.api.schema.dataobjects.Schema;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;

import static org.testng.Assert.assertEquals;

public class Registry693TestCase extends TestSetup {

    @BeforeClass(groups = {"wso2.greg"})
    public void initTest() throws RegistryException {
        super.init();
    }

    @Test(groups = {"wso2.greg"})
    public void addSchema() throws RegistryException {
        Registry governanceRegistry = GovernanceUtils.getGovernanceUserRegistry(registry, FrameworkSettings.USER_NAME);
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governanceRegistry);
        SchemaManager manager = new SchemaManager(governanceRegistry);
        Schema schema = manager.newSchema(
                "https://svn.wso2.org/repos/wso2/trunk/commons/qa/qa-artifacts/greg/xsd/servicecontracts/_2008/_01/GeoIPService.svc.xsd");
        int currentSchemaLength = manager.getAllSchemas().length;
        manager.addSchema(schema);
        // The insertion will add 2 schemas, so the length should increase by 2.
        assertEquals(manager.getAllSchemas().length, currentSchemaLength + 2, "Invalid number of schemas on registry");
    }

}
