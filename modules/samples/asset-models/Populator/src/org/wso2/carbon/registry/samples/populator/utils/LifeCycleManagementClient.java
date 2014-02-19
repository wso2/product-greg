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

            PopulatorUtil.setCookie(lcmStub, cookie);
        } catch (AxisFault e) {
            String msg = "Failed to initiate lifecycle management service client.";
            throw new RegistryException(msg, e);
        }
        try {
            String epr = backendServerURL + "CustomLifecyclesChecklistAdminService";
            stub = new CustomLifecyclesChecklistAdminServiceStub(configContext, epr);

            PopulatorUtil.setCookie(stub, cookie);
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
    public void invokeAspect(String path, String aspect, String action, String[] items, Map<String, String> params)
            throws Exception {
        if (params.size() == 0) {
            stub.invokeAspect(path, aspect, action, items);
        } else {
            List<ArrayOfString> paramsList = new LinkedList<ArrayOfString>();
            for (Map.Entry<String, String> e : params.entrySet()) {
                ArrayOfString arrayOfString = new ArrayOfString();
                arrayOfString.addArray(e.getKey());
                arrayOfString.addArray(e.getValue());
                paramsList.add(arrayOfString);
            }
            stub.invokeAspectWithParams(path, aspect, action, items, paramsList.toArray(
                    new ArrayOfString[paramsList.size()]));
        }
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
}

