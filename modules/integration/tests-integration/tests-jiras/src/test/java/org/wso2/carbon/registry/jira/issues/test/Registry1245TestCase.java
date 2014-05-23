package org.wso2.carbon.registry.jira.issues.test;

/*
* Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.registry.info.stub.beans.xsd.SubscriptionBean;
import org.wso2.greg.integration.common.clients.InfoServiceAdminClient;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;

public class Registry1245TestCase extends GREGIntegrationBaseTest {

    private static final Log log = LogFactory.getLog(Registry1245TestCase.class);
    private InfoServiceAdminClient infoServiceAdminClient;
    private ResourceAdminServiceClient resourceAdminServiceClient;

    @BeforeClass(groups = {"wso2.greg"})
    public void init() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String session = getSessionCookie();

        infoServiceAdminClient =
                new InfoServiceAdminClient(backendURL, session);

        resourceAdminServiceClient =
                new ResourceAdminServiceClient(backendURL, session);

    }

    @Test(groups = {"wso2.greg"}, expectedExceptions = AxisFault.class)
    public void runSuccessCase() throws Exception {

        log.debug("Running SuccessCase");
        SubscriptionBean collectionUpdatedBean;

        String loggedInSessionCookie = getSessionCookie();

        resourceAdminServiceClient.addCollection("/_system/local",
                                                 "test_subs", "test", "description");

        String TOPIC_INDEX_PATH = "/_system/governance/event/topicIndex";

        collectionUpdatedBean =
                infoServiceAdminClient.subscribe("/_system/local/test_subs",
                                                 automationContext.getContextUrls().getBackEndUrl() +
                                                 "EventBrokerService", "CollectionUpdated",
                                                 loggedInSessionCookie);

        String key = collectionUpdatedBean.getSubscriptionInstances()[0].getId();

        assertNotNull(resourceAdminServiceClient.getProperty(TOPIC_INDEX_PATH, key));

        resourceAdminServiceClient.deleteResource("/_system/local/test_subs");

        assertNull(resourceAdminServiceClient.getProperty(TOPIC_INDEX_PATH, key));

        resourceAdminServiceClient.getResource("/_system/governance/event/topics/registry/notifications/" +
                                               "CollectionUpdated/_system/local/test_subs");
    }

    @AfterClass
    public void cleanupResource() {
        resourceAdminServiceClient = null;
        infoServiceAdminClient = null;
    }

}
