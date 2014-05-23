/*
 *  Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.subscription.test;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.info.stub.RegistryExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.clients.InfoServiceAdminClient;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import java.util.Collection;

public class RESTSubscriptionTestCase extends GREGIntegrationBaseTest {

    private InfoServiceAdminClient infoServiceAdminClient;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    String sessionID = "";
    private WSRegistryServiceClient registry;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        sessionID = getSessionCookie();
        infoServiceAdminClient = new InfoServiceAdminClient(backendURL, sessionID);
        resourceAdminServiceClient = new ResourceAdminServiceClient(backendURL, sessionID);
        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        registry = registryProviderUtil.getWSRegistry(automationContext);
        Registry governance = registryProviderUtil.getGovernanceRegistry(registry, automationContext);
    }

    @Test(groups = "wso2.org")
    public void validateSubscribeREST() throws RegistryExceptionException, RegistryException {
        String path1 = "/_system/governance/repository/#";
        String path2 = "/_system/governance/repository/*";
        String endpoint = "http://localhost:9764";
        String eventName = "CollectionUpdated";
        //Hierarchical Subscription Method:  Collection , Children and Grand Children
        infoServiceAdminClient.subscribeREST(path1, endpoint, eventName, sessionID);
        //Hierarchical Subscription Method:  Collection , Children.
        infoServiceAdminClient.subscribeREST(path2, endpoint, eventName, sessionID);
        Resource resource = registry.get("/_system/governance/event/topicIndex");
        Collection<Object> values = resource.getProperties().values();
        boolean foundTopicsForPath1 = false;
        boolean foundTopicsForPath2 = false;
        for(Object value1 : values) {
            if(value1.toString().contains("[/registry/notifications/CollectionUpdated/_system/governance/repository" + "/#]")) {
                foundTopicsForPath1 = true;
            }
            if(value1.toString().contains("/registry/notifications/CollectionUpdated/_system/governance/repository/*")) {
                foundTopicsForPath2 = true;
            }
        }
        Assert.assertTrue(foundTopicsForPath1, "topic url is wrong for Hierarchical Subscription Method:  " + "Collection , Children and Grand Children");
        Assert.assertTrue(foundTopicsForPath2, "topic url is wrong for Hierarchical Subscription Method:  " + "Collection , Children.");
    }
}