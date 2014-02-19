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
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.handler.stub.ExceptionException;
import org.wso2.carbon.registry.handler.stub.HandlerManagementServiceStub;

import javax.xml.stream.XMLStreamException;
import java.rmi.RemoteException;

/**
 * Handler management admin service client
 */
public class PopulatorHandlerManagerServiceClient {

    private HandlerManagementServiceStub stub;

    public PopulatorHandlerManagerServiceClient(
            String cookie, String backendServerURL, ConfigurationContext configContext) throws RegistryException {
        String epr = backendServerURL + "HandlerManagementService";
        try {
            stub = new HandlerManagementServiceStub(configContext, epr);

            PopulatorUtil.setCookie(stub, cookie);

        } catch (AxisFault axisFault) {
            String msg = "Failed to initiate handler management service client. " + axisFault.getMessage();
            throw new RegistryException(msg, axisFault);
        }
    }

    /**
     * Adding new handler to the registry
     *
     * @param payload
     * @throws ExceptionException
     * @throws RemoteException
     * @throws XMLStreamException
     */
    public void newHandler(String payload) throws ExceptionException, RemoteException, XMLStreamException {
        stub.createHandler(payload);
    }
}

