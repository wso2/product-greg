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
package org.wso2.carbon.registry.samples.populator.utils;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.governance.notifications.stub.InfoAdminServiceStub;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

public class SubscriberClient {

    private InfoAdminServiceStub stub;

    public SubscriberClient(String cookie, String backendServerURL, ConfigurationContext configContext)
            throws RegistryException {
        try {
            String epr = backendServerURL + "InfoAdminService";
            stub = new InfoAdminServiceStub(configContext, epr);

            PopulatorUtil.setCookie(stub, cookie);
        } catch (AxisFault e) {
            String msg = "Failed to initiate info admin service client.";
            throw new RegistryException(msg, e);
        }
    }

    public void subscribe(String path, String endpoint, String eventName) throws Exception {
        stub.subscribe(path, endpoint, eventName, null);
    }
}

