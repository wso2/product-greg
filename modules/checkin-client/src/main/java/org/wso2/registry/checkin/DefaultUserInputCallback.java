/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.registry.checkin;

import org.wso2.carbon.registry.synchronization.UserInputCallback;
import org.wso2.carbon.registry.synchronization.message.Message;

public class DefaultUserInputCallback implements UserInputCallback {

    private ClientOptions clientOptions = null;

    public DefaultUserInputCallback(ClientOptions clientOptions) {
        this.clientOptions = clientOptions;
    }

    public void displayMessage(Message message) {
        ClientUtils.printMessage(clientOptions, message.getMessageCode(), message.getParameters());
    }

    public boolean getConfirmation(Message message, String context) {
        UserInputCode inputCode = ClientUtils.confirmMessage(clientOptions,
                message.getMessageCode(), message.getParameters(),
                context);
        return (inputCode == UserInputCode.YES);
    }
}
