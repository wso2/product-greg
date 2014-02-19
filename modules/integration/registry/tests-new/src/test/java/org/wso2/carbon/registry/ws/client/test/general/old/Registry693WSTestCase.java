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

import org.apache.axis2.AxisFault;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.api.schema.SchemaManager;
import org.wso2.carbon.governance.api.schema.dataobjects.Schema;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import static org.testng.Assert.assertEquals;

public class Registry693WSTestCase {

    private WSRegistryServiceClient registry;
    private String USER_NAME;

    @BeforeClass(groups = {"wso2.greg"})
    public void initTest() throws RegistryException, AxisFault {
        int userId = 0;
        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        registry = registryProviderUtil.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        UserInfo userInfo;
        userInfo = UserListCsvReader.getUserInfo(userId);
        USER_NAME = userInfo.getUserName();
    }

    @Test(groups = {"wso2.greg"})
    public void addSchema() throws RegistryException {
        SchemaManager manager = new SchemaManager(
                GovernanceUtils.getGovernanceUserRegistry(registry, USER_NAME));
        Schema schema = manager.newSchema(
                "https://svn.wso2.org/repos/wso2/trunk/commons/qa/qa-artifacts/greg/xsd/servicecontracts/_2008/_01/GeoIPService.svc.xsd");
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
