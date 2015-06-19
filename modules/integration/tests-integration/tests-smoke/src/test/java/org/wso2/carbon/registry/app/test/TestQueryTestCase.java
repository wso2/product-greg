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

package org.wso2.carbon.registry.app.test;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.registry.app.RemoteRegistry;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * A test case which tests registry queries
 */
public class TestQueryTestCase extends GREGIntegrationBaseTest{
    public RemoteRegistry registry;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception{

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        registry = new RegistryProviderUtil().getRemoteRegistry(automationContext);
    }

    @Test(groups = {"wso2.greg"})
    public void putRegistryQueriesTest() throws RegistryException {

        String QUERY_EPR_BY_PATH = "/Queries1/EPRByPath-new";
        Resource resource1 = null;

        try {

            storeSQLQuery(QUERY_EPR_BY_PATH);

            assertTrue(registry.resourceExists(QUERY_EPR_BY_PATH), "Resource doesn't exists");

            Map<String, String> parameters = new HashMap<String, String>();
            parameters.put("1", RegistryConstants.SQL_QUERY_MEDIA_TYPE); // media type
            Collection collection = registry.executeQuery(QUERY_EPR_BY_PATH, parameters);
            String[] children = collection.getChildren();


            boolean successful = false;
            for (String path : children) {
                if (path.contains(QUERY_EPR_BY_PATH)) {
                    successful = true;
                }
            }
            assertTrue(successful);
        } catch (Exception e) {
            fail(e.getMessage());
            e.printStackTrace();
        }
    }

    @Test(groups = {"wso2.greg"})
    public void executeQueriesTest() throws RegistryException {
        String QUERY_EPR_BY_PATH = "/Queries1/EPRByPath-new";
        Resource resource1 = null;

        try {

            storeSQLQuery(QUERY_EPR_BY_PATH);

            assertTrue(registry.resourceExists(QUERY_EPR_BY_PATH), "Resource doesn't exists");

            Map<String, String> parameters = new HashMap<String, String>();
            parameters.put("1", RegistryConstants.SQL_QUERY_MEDIA_TYPE); // media type
            Collection collection = registry.executeQuery(QUERY_EPR_BY_PATH, parameters);
            String[] children = collection.getChildren();


            boolean successful = false;
            for (String path : children) {
                if (path.contains(QUERY_EPR_BY_PATH)) {
                    successful = true;
                }
            }
            assertTrue(successful);
        } catch (Exception e) {
            fail(e.getMessage());
            e.printStackTrace();
        }
    }


    private void storeSQLQuery(String path) throws RegistryException, Exception {
        String sql1 = "SELECT REG_PATH_ID, REG_NAME FROM REG_RESOURCE R WHERE R.REG_MEDIA_TYPE LIKE ?";
        Resource q1 = registry.newResource();
        q1.setContent(sql1);
        q1.setMediaType(RegistryConstants.SQL_QUERY_MEDIA_TYPE);
        q1.addProperty(RegistryConstants.RESULT_TYPE_PROPERTY_NAME,
                       RegistryConstants.RESOURCES_RESULT_TYPE);
        registry.put(path, q1);
    }

    @AfterClass(alwaysRun = true)
    public void cleanArtifact() throws RegistryException {
        registry = null;
    }
}
