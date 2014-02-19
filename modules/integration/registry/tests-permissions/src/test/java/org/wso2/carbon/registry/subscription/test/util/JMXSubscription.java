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

package org.wso2.carbon.registry.subscription.test.util;

import java.io.IOException;
import java.rmi.RemoteException;

import org.wso2.carbon.automation.api.clients.registry.InfoServiceAdminClient;
import org.wso2.carbon.automation.api.clients.registry.PropertiesAdminServiceClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.info.stub.beans.xsd.SubscriptionBean;
import org.wso2.carbon.registry.properties.stub.PropertiesAdminServiceRegistryExceptionException;

import javax.management.InstanceNotFoundException;
import javax.management.ListenerNotFoundException;

public class JMXSubscription {

    private static UserInfo userInfo;
    private static ManageEnvironment environment;
    private JMXClient jmxClient;

    /**
     * @param path      path of the collection or resource to be subscribed
     * @param eventType event to be subscribed
     * @param env       ManageEnvironment
     * @param userInf   UserInfo
     * @return true if the required jmx notification is generated for
     *         subscription, false otherwise
     * @throws Exception
     */
    public boolean init(String path, String eventType, ManageEnvironment env,
                        UserInfo userInf) throws Exception {
        environment = env;
        userInfo = userInf;
        boolean result = JMXSubscribe(path, eventType) && update(path) && getJMXNotification();
        clean(path);
        return result;
    }

    /**
     * @param path      path of the collection or resource to be subscribed
     * @param eventType event to be subscribed
     * @return true if the subscription is created for he collection or
     *         resource,otherwise false
     * @throws RemoteException
     * @throws RegistryException
     */
    private static boolean JMXSubscribe(String path, String eventType) throws RemoteException,
                                                                              RegistryException {
        InfoServiceAdminClient infoServiceAdminClient =
                new InfoServiceAdminClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                           userInfo.getUserName(), userInfo.getPassword());
        // subscribe for jmx notifications
        SubscriptionBean bean =
                infoServiceAdminClient.subscribe(path, "jmx://", eventType, environment.getGreg().getSessionCookie());
        return (bean.getSubscriptionInstances() != null);

    }

    /**
     * @param path path of the collection or resource to be subscribed
     * @return true if a new property is added to the collection or resource,
     *         false otherwise
     * @throws Exception
     */
    private boolean update(String path) throws Exception {
        jmxClient = new JMXClient();
        jmxClient.connect(userInfo.getUserName(), userInfo.getPassword());
        jmxClient.registerNotificationListener("at path " + path + " was updated.");
        PropertiesAdminServiceClient propertiesAdminServiceClient =
                new PropertiesAdminServiceClient(
                        environment.getGreg()
                                .getProductVariables()
                                .getBackendUrl(),
                        userInfo.getUserName(),
                        userInfo.getPassword());
        // update by adding a property
        propertiesAdminServiceClient.setProperty(path, "TestProperty", "TestValue");
        ResourceAdminServiceClient resourceAdminServiceClient =
                new ResourceAdminServiceClient(
                        environment.getGreg()
                                .getProductVariables()
                                .getBackendUrl(),
                        userInfo.getUserName(),
                        userInfo.getPassword());
        boolean status = jmxClient.getNotifications();
        return resourceAdminServiceClient.getProperty(path, "TestProperty").equals("TestValue") && status;
    }

    /**
     * @return true if the required jmx notification is captured
     */
    private boolean getJMXNotification() {
        return jmxClient.isSuccess();
    }

    /**
     * remove the properties added to collection or resource
     *
     * @param path path of the collection or resource to be subscribed
     * @throws RemoteException
     * @throws PropertiesAdminServiceRegistryExceptionException
     *
     */
    private static void clean(String path) throws IOException,
                                                  PropertiesAdminServiceRegistryExceptionException,
                                                  InstanceNotFoundException,
                                                  ListenerNotFoundException {
        PropertiesAdminServiceClient propertiesAdminServiceClient =
                new PropertiesAdminServiceClient(
                        environment.getGreg()
                                .getProductVariables()
                                .getBackendUrl(),
                        userInfo.getUserName(),
                        userInfo.getPassword());
        propertiesAdminServiceClient.removeProperty(path, "TestProperty");
    }

    public void disconnect()
            throws InstanceNotFoundException, ListenerNotFoundException, IOException {
        jmxClient.removeNotificationListener();
        jmxClient.disconnect();
        environment = null;
        jmxClient = null;
        userInfo = null;

    }
}