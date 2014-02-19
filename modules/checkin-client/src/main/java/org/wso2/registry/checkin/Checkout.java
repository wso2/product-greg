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

import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.synchronization.SynchronizationException;
import org.wso2.carbon.registry.synchronization.Utils;
import org.wso2.carbon.registry.synchronization.message.MessageCode;
import org.wso2.carbon.registry.synchronization.operation.CheckOutCommand;

@SuppressWarnings("unused")
public class Checkout {
    private CheckOutCommand checkOutCommand = null;
    private ClientOptions clientOptions = null;
    private String registryUrl = null;

    public int getAddedCount() {
        return checkOutCommand.getAddedCount();
    }

    public int getOverwrittenCount() {
        return checkOutCommand.getOverwrittenCount();
    }

    public int getNonOverwrittenCount() {
        return checkOutCommand.getNonOverwrittenCount();
    }

    public Checkout(ClientOptions clientOptions) throws SynchronizationException {
        this.checkOutCommand = new CheckOutCommand(clientOptions.getOutputFile(),
                clientOptions.getWorkingLocation(), clientOptions.getUserUrl(),
                clientOptions.getUsername(), true);
        this.clientOptions = clientOptions;
        String url = clientOptions.getUserUrl();

        if (url == null) {
            throw new SynchronizationException(MessageCode.CO_PATH_MISSING);
        }
        
        // derive the registry url and the path
        registryUrl = Utils.getRegistryUrl(url);
    }

    public void execute() throws SynchronizationException {
        execute(ClientUtils.newRegistry(registryUrl,
                clientOptions.getUsername(), clientOptions.getPassword(),
                clientOptions.getTenantId(), clientOptions.getType()));
    }

    public void execute(Registry registry) throws SynchronizationException {
        checkOutCommand.execute(registry, new DefaultUserInputCallback(clientOptions));
        if (clientOptions.getOutputFile() == null) {
            if (checkOutCommand.getAddedCount() > 0) {
                ClientUtils.printMessage(clientOptions, MessageCode.ADDED_SUCCESS,
                        new String[] {checkOutCommand.getAddedCount() + ""});
            }
            else if (checkOutCommand.getAddedCount() == 0) {
                ClientUtils.printMessage(clientOptions, MessageCode.NO_FILES_ADDED);
            }
            if (checkOutCommand.getOverwrittenCount() > 0) {
                ClientUtils.printMessage(clientOptions, MessageCode.OVERWRITTEN_FINAL,
                        new String[] {checkOutCommand.getOverwrittenCount() + ""});
            }
            if (checkOutCommand.getNonOverwrittenCount() > 0) {
                ClientUtils.printMessage(clientOptions, MessageCode.NON_OVERWRITTEN_FINAL,
                        new String[] {checkOutCommand.getNonOverwrittenCount() + ""});
            }
        }
        ClientUtils.printMessage(clientOptions, MessageCode.SUCCESS);
    }
}
