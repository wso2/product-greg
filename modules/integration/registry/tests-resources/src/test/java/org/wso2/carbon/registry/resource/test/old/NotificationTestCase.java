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

package org.wso2.carbon.registry.resource.test.old;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.registry.InfoServiceAdminClient;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.registry.info.stub.beans.utils.xsd.SubscriptionInstance;
import org.wso2.carbon.registry.info.stub.beans.xsd.SubscriptionBean;

import java.rmi.RemoteException;

import static org.testng.Assert.assertNotNull;

/**
* A test case which tests registry notification operation
*/
public class NotificationTestCase {

    private static final Log log = LogFactory.getLog(NotificationTestCase.class);
    private String loggedInSessionCookie = "";

    private InfoServiceAdminClient infoServiceAdminClient;

    private static final String END_POINT = "https://localhost:9443/services/EventBrokerService";
    private static final String SYSTEM_FOLDER = "/_system";
    private static final String SYSTEM_FOLDER_CHILD = "config";
    private static final String EVENT_COLLECTION_UPDATED = "CollectionUpdated";
    private static final String EVENT_COLLECTION_DELETED = "CollectionDeleted";
    private static final String EVENT_CHILD_DELETED = "ChildDeleted";
    private static final String EVENT_CHILD_CREATED = "ChildCreated";

    @BeforeClass(groups = {"wso2.greg"})
    public void init() throws Exception {
        log.info("Initializing Notification Test");
        log.debug("Registry Notification Test Initialised");

        int userId = 0;
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        ManageEnvironment environment = builder.build();

        loggedInSessionCookie = environment.getGreg().getSessionCookie();
        UserInfo userInfo = UserListCsvReader.getUserInfo(userId);

        infoServiceAdminClient =
                new InfoServiceAdminClient(environment.getGreg().getBackEndUrl(),
                                           userInfo.getUserName(), userInfo.getPassword());


    }

    @Test(groups = {"wso2.greg"})
    public void testRunSuccessCase() throws RemoteException {
        log.debug("Running SuccessCase");

        SubscriptionBean collectionUpdatedBean = infoServiceAdminClient.subscribe(SYSTEM_FOLDER+"/"+SYSTEM_FOLDER_CHILD,
                    END_POINT, EVENT_COLLECTION_UPDATED, loggedInSessionCookie);

        assertNotNull(collectionUpdatedBean.getSubscriptionInstances(),"Failed to subscribe to "+SYSTEM_FOLDER+"/"+SYSTEM_FOLDER_CHILD +" on event "+EVENT_COLLECTION_UPDATED);
        collectionUpdatedBean.setSubscriptionInstances(new SubscriptionInstance[0]);
        SubscriptionBean collectionDeletedBean = infoServiceAdminClient.subscribe(SYSTEM_FOLDER+"/"+SYSTEM_FOLDER_CHILD,
                                                                 END_POINT, EVENT_COLLECTION_DELETED, loggedInSessionCookie);



        assertNotNull(collectionDeletedBean.getSubscriptionInstances(),"Failed to subscribe to "+SYSTEM_FOLDER +" on event "+EVENT_COLLECTION_DELETED);
        collectionDeletedBean.setSubscriptionInstances(new SubscriptionInstance[1]);
        SubscriptionBean childCreatedBean = infoServiceAdminClient.subscribe(SYSTEM_FOLDER,
                                                            END_POINT, EVENT_CHILD_CREATED, loggedInSessionCookie);


        assertNotNull(childCreatedBean.getSubscriptionInstances(),"Failed to subscribe to "+SYSTEM_FOLDER +" on event "+EVENT_CHILD_CREATED);
        childCreatedBean.setSubscriptionInstances(new SubscriptionInstance[2]);
        SubscriptionBean childDeletedBean = infoServiceAdminClient.subscribe(SYSTEM_FOLDER,
                                                            END_POINT, EVENT_CHILD_DELETED, loggedInSessionCookie);


        assertNotNull(childDeletedBean.getSubscriptionInstances(),"Failed to subscribe to "+SYSTEM_FOLDER +" on event "+EVENT_CHILD_CREATED);
        childDeletedBean.setSubscriptionInstances(new SubscriptionInstance[3]);

            // TODO: unsubscribe

    }

    //cleanup code
    @AfterClass
    public void cleanup()
            throws Exception {
        infoServiceAdminClient=null;
    }

}
