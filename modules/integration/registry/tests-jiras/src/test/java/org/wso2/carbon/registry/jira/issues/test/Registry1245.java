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
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

import org.wso2.carbon.automation.api.clients.registry.InfoServiceAdminClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.registry.info.stub.beans.xsd.SubscriptionBean;


public class Registry1245 {

    private static final Log log = LogFactory.getLog(Registry1245.class);
    private InfoServiceAdminClient infoServiceAdminClient;
    private ResourceAdminServiceClient resourceAdminServiceClient;

    private ManageEnvironment environment;

    @BeforeClass(groups = {"wso2.greg"})
    public void init() throws Exception {
        log.info("Initializing Notification Test");
        log.debug("Registry Notification Test Initialised");
        int userId = ProductConstant.ADMIN_USER_ID;
        UserInfo userInfo = UserListCsvReader.getUserInfo(userId);
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        environment = builder.build();

        infoServiceAdminClient =
                new InfoServiceAdminClient(environment.getGreg().getBackEndUrl(),
                                           environment.getGreg().getSessionCookie());

        resourceAdminServiceClient =
                new ResourceAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                               environment.getGreg().getSessionCookie());

    }

    @Test(groups = {"wso2.greg"}, expectedExceptions = AxisFault.class)
    public void runSuccessCase() throws Exception {

        log.debug("Running SuccessCase");
        SubscriptionBean collectionUpdatedBean;

        String loggedInSessionCookie = environment.getGreg().getSessionCookie();

        resourceAdminServiceClient.addCollection("/_system/local",
                                                 "test_subs", "test", "description");

        String TOPIC_INDEX_PATH = "/_system/governance/event/topicIndex";

        collectionUpdatedBean =
                infoServiceAdminClient.subscribe("/_system/local/test_subs",
                                                 environment.getGreg().getBackEndUrl() +
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
        environment = null;
    }

}
