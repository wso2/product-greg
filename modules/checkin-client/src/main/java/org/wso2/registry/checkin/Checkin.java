/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.registry.checkin;

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.synchronization.SynchronizationConstants;
import org.wso2.carbon.registry.synchronization.SynchronizationException;
import org.wso2.carbon.registry.synchronization.Utils;
import org.wso2.carbon.registry.synchronization.message.MessageCode;
import org.wso2.carbon.registry.synchronization.operation.CheckInCommand;

import javax.xml.namespace.QName;

public class Checkin {
    private CheckInCommand checkInCommand = null;
    private ClientOptions clientOptions = null;
    private String registryUrl = null;

    public Checkin(ClientOptions clientOptions) throws SynchronizationException {
        this.checkInCommand = new CheckInCommand(clientOptions.getOutputFile(),
                clientOptions.getWorkingLocation(), clientOptions.getUserUrl(),
                clientOptions.getUsername(), true, true, false);
        this.clientOptions = clientOptions;
        String url = clientOptions.getUserUrl();
        String startingDir =  clientOptions.getWorkingLocation();

        // now if the user url is different to the registry url we are going to consider that as well.
        if (url != null) {
            registryUrl = Utils.getRegistryUrl(url);
        } else {
            // get the update details form the meta element of the current checkout
            OMElement metaOMElement = Utils.getMetaOMElement(startingDir);
            if (metaOMElement == null) {
                throw new SynchronizationException(MessageCode.CHECKOUT_BEFORE_UPDATE);
            }
            registryUrl = metaOMElement.getAttributeValue(new QName("registryUrl"));
        }
    }

    public void execute() throws SynchronizationException {
        execute(ClientUtils.newRegistry(registryUrl,
                clientOptions.getUsername(), clientOptions.getPassword(),
                clientOptions.getTenantId(), clientOptions.getType()));
    }

    public void execute(Registry registry) throws SynchronizationException {
        UserInputCode inputCode = UserInputCode.YES;
        // Remove confirmation if  interaction option given .
        if (clientOptions.isInteractive()) {
            inputCode = ClientUtils.confirmMessage(clientOptions,
                    MessageCode.CHECK_IN_RESOURCES_CONFIRMATION, null,
                    SynchronizationConstants.CHECK_IN_CONFIRMATION_CONTEXT);
        }
        if (inputCode == UserInputCode.YES) {
            checkInCommand.execute(registry, new DefaultUserInputCallback(clientOptions));
            if (clientOptions.getOutputFile() == null) {
                ClientUtils.printMessage(clientOptions, MessageCode.TRANSMIT_SUCCESS,
                        new String[]{checkInCommand.getSentCount() + ""});

            }
            ClientUtils.printMessage(clientOptions, MessageCode.SUCCESS);            
        }
        else {
            ClientUtils.printMessage(clientOptions, MessageCode.CHECK_IN_OPERATION_ABORTED);
        }
    }
}
