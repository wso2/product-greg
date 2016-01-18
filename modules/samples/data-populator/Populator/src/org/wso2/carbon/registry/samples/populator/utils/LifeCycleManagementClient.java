/*
 *  Copyright (c) 2015 WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.Stub;
import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceStub;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.services.ArrayOfString;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceStub;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * Lifecycle admin service client
 */
public class LifeCycleManagementClient {

    private LifeCycleManagementServiceStub lcmStub;
    private CustomLifecyclesChecklistAdminServiceStub stub;

    public LifeCycleManagementClient(String cookie, String backendServerURL, ConfigurationContext configContext)
            throws RegistryException {
        try {
            String epr = backendServerURL + "LifeCycleManagementService";
            lcmStub = new LifeCycleManagementServiceStub(configContext, epr);

            setCookie(lcmStub, cookie);
        } catch (AxisFault e) {
            String msg = "Failed to initiate lifecycle management service client.";
            throw new RegistryException(msg, e);
        }
        try {
            String epr = backendServerURL + "CustomLifecyclesChecklistAdminService";
            stub = new CustomLifecyclesChecklistAdminServiceStub(configContext, epr);

            setCookie(stub, cookie);
        } catch (AxisFault e) {
            String msg = "Failed to initiate checklist lifecycle admin service client.";
            throw new RegistryException(msg, e);
        }
    }

    /**
     * Adding a new lifecycle to the registry
     *
     * @param configuration
     * @throws Exception
     */
    public void createLifecycle(String configuration) throws Exception {
        lcmStub.createLifecycle(configuration);
    }

    /**
     * Invoke lifecycle action
     *
     * @param path
     * @param aspect
     * @param action
     * @param items
     * @param params
     * @throws Exception
     */
    public void invokeAspect(String path, String aspect, String action, String[] items)
            throws Exception {
        stub.invokeAspect(path, aspect, action, items);

    }

    /**
     * Add new aspect to the registry
     *
     * @param path
     * @param aspect
     * @throws Exception
     */
    public void addAspect(String path, String aspect) throws Exception {
        stub.addAspect(path, aspect);
    }

    /**
     * Remove aspect from the registry
     *
     * @param path
     * @param aspect
     * @throws Exception
     */
    public void removeAspect(String path, String aspect) throws Exception {
        stub.removeAspect(path, aspect);
    }

    private static void setCookie(Stub stub, String cookie) {
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        if (cookie != null) {
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        }
    }
}

