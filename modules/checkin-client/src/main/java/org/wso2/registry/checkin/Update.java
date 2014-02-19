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
import org.wso2.carbon.registry.synchronization.SynchronizationException;
import org.wso2.carbon.registry.synchronization.Utils;
import org.wso2.carbon.registry.synchronization.message.MessageCode;
import org.wso2.carbon.registry.synchronization.operation.UpdateCommand;

import javax.xml.namespace.QName;

@SuppressWarnings("unused")
public class Update {
    private UpdateCommand updateCommand = null;
    private ClientOptions clientOptions = null;
    private String registryUrl = null;

    public int getAddedCount() {
        return updateCommand.getAddedCount();
    }

    public int getUpdatedCount() {
        return updateCommand.getUpdatedCount();
    }

    public int getConflictedCount() {
        return updateCommand.getConflictedCount();
    }

    public int getDeletedCount() {
        return updateCommand.getDeletedCount();
    }

    public int getNotDeletedCount() {
        return updateCommand.getNotDeletedCount();
    }

    public Update(ClientOptions clientOptions) throws SynchronizationException {
        this.updateCommand = new UpdateCommand(clientOptions.getOutputFile(),
                clientOptions.getWorkingLocation(), clientOptions.getUserUrl(),
                false, clientOptions.getUsername(), true);
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
        updateCommand.execute(registry, new DefaultUserInputCallback(clientOptions));

        if (updateCommand.getAddedCount() > 0) {
            ClientUtils.printMessage(clientOptions, MessageCode.ADDED_SUCCESS,
                    new String[] {updateCommand.getAddedCount() + ""});
        } else {
            ClientUtils.printMessage(clientOptions, MessageCode.NO_FILES_ADDED);
        }
        if (updateCommand.getUpdatedCount() > 0) {
            ClientUtils.printMessage(clientOptions, MessageCode.UPDATED_SUCCESS,
                    new String[] {updateCommand.getUpdatedCount() + ""});
        } else {
            ClientUtils.printMessage(clientOptions, MessageCode.NO_FILES_UPDATED);
        }
        if (updateCommand.getConflictedCount() > 0) {
            ClientUtils.printMessage(clientOptions, MessageCode.CONFLICTED_FAILURE,
                    new String[] {updateCommand.getConflictedCount() + ""});
        } else {
            ClientUtils.printMessage(clientOptions, MessageCode.NO_FILES_CONFLICTED);
        }
        if (updateCommand.getDeletedCount() > 0) {
            ClientUtils.printMessage(clientOptions, MessageCode.DELETED_SUCCESS,
                    new String[] {updateCommand.getDeletedCount() + ""});
        } else {
            ClientUtils.printMessage(clientOptions, MessageCode.NO_FILES_DELETED);
        }
        if (updateCommand.getNotDeletedCount() > 0) {
            ClientUtils.printMessage(clientOptions, MessageCode.NOT_DELETED_FINAL,
                    new String[] {updateCommand.getNotDeletedCount() + ""});
        }
        ClientUtils.printMessage(clientOptions, MessageCode.SUCCESS);        
    }
}
