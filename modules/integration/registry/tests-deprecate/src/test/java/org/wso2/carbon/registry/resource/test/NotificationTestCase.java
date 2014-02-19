/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.resource.test;

import static org.testng.Assert.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.core.TestTemplate;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.registry.info.stub.InfoAdminServiceStub;
import org.wso2.carbon.registry.info.stub.beans.utils.xsd.SubscriptionInstance;
import org.wso2.carbon.registry.info.stub.beans.xsd.SubscriptionBean;

/**
 * A test case which tests registry notification operation
 */
public class NotificationTestCase {

    private static final Log log = LogFactory.getLog(NotificationTestCase.class);
    private String loggedInSessionCookie = "";
    private LoginLogoutUtil util = new LoginLogoutUtil();

    @BeforeClass(groups = {"wso2.greg"})
    public void init() throws Exception {
        log.info("Initializing Notification Test");
        log.debug("Registry Notification Test Initialised");
        loggedInSessionCookie = util.login();
    }

    @Test(groups = {"wso2.greg"})
    public void runSuccessCase() {
        log.debug("Running SuccessCase");
        SubscriptionBean collectionUpdatedBean = null, collectionDeletedBean = null, childCreatedBean = null, childDeletedBean = null;
        try {
            InfoAdminServiceStub infoAdminServiceStub = TestUtils.getInfoAdminServiceStub(loggedInSessionCookie);

            collectionUpdatedBean = infoAdminServiceStub.subscribe("/_system/config",
                    "https://localhost:9443/services/EventBrokerService", "CollectionUpdated", loggedInSessionCookie);

            if (collectionUpdatedBean.getSubscriptionInstances() != null) {
                collectionUpdatedBean.setSubscriptionInstances(new SubscriptionInstance[0]);
                collectionDeletedBean = infoAdminServiceStub.subscribe("/_system/config",
                        "https://localhost:9443/services/EventBrokerService", "CollectionDeleted", loggedInSessionCookie);

            } else {
                log.error("Failed to subscribe. ");
                fail("Failed to subscribe");

            }
            if (collectionDeletedBean.getSubscriptionInstances() != null) {
                collectionDeletedBean.setSubscriptionInstances(new SubscriptionInstance[1]);
                childCreatedBean = infoAdminServiceStub.subscribe("/_system",
                        "https://localhost:9443/services/EventBrokerService", "ChildCreated", loggedInSessionCookie);

            } else {
                log.error("Failed to subscribe. ");
                fail("Failed to subscribe");

            }
            if (childCreatedBean.getSubscriptionInstances() != null) {
                childCreatedBean.setSubscriptionInstances(new SubscriptionInstance[2]);
                childDeletedBean = infoAdminServiceStub.subscribe("/_system",
                        "https://localhost:9443/services/EventBrokerService", "ChildDeleted", loggedInSessionCookie);

            } else {
                log.error("Failed to subscribe. ");
                fail("Failed to subscribe");

            }
            if (childDeletedBean.getSubscriptionInstances() != null) {
                childDeletedBean.setSubscriptionInstances(new SubscriptionInstance[3]);

            } else {
                log.error("Failed to subscribe. ");
                fail("Failed to subscribe");

            }
            // TODO: unsubscribe


        } catch (Exception e) {
            String msg = "Failed to subscribe. " +
                    e.getMessage();
            log.error(msg, e);
            fail("Failed to subscribe");
        }
    }

}
