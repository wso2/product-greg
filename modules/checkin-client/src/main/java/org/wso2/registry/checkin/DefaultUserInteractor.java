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

import org.wso2.carbon.registry.synchronization.message.MessageCode;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;


public class DefaultUserInteractor implements UserInteractor {
    private Map<String, Boolean> yesToAllMap = new HashMap<String, Boolean>();
    private Map<String, Boolean> noToAllMap = new HashMap<String, Boolean>();
    
    public String showMessage(MessageCode msgCode, String[] parameters) {
        String codeMsg = DefaultMessages.getMessageFromCode(msgCode);
        int placeHolderCount = derivePlaceHolderCount(codeMsg);
        codeMsg = buildMessage(codeMsg, parameters);

        StringBuffer msgStringBuffer = new StringBuffer(codeMsg);
        if (parameters != null) {
            for (int i = placeHolderCount; i < parameters.length; i ++) {
                msgStringBuffer.append("\n").append(parameters[i]);
            }
        }
        String msg = msgStringBuffer.toString();
        System.out.println(msg);
        return msg;
    }

    public int derivePlaceHolderCount(String msg) {
        int count = 0;
        while (msg.indexOf(PARAMETER_PLACE_HOLDER) >= 0) {
            count ++;
            msg = msg.substring(msg.indexOf(PARAMETER_PLACE_HOLDER) + PARAMETER_PLACE_HOLDER.length());
        }
        return count;
    }

    public String buildMessage(String msg, String[] parameters) {
        int parameterIndex = 0;
        String remainingMsg;
        int placeHolderIndex;
        while (true) {
            placeHolderIndex = msg.indexOf(PARAMETER_PLACE_HOLDER);
            if (placeHolderIndex >= 0) {
                String parameter = parameters[parameterIndex ++];
                remainingMsg = msg.substring(placeHolderIndex + PARAMETER_PLACE_HOLDER.length());
                if (placeHolderIndex == 0) {
                    msg = parameter + remainingMsg;
                } else {
                    msg = msg.substring(0, placeHolderIndex) + parameter + remainingMsg;
                }
                placeHolderIndex += parameter.length();
            } else {
                break;
            }
        }
        return msg;
    }

    public UserInputCode getInput(MessageCode expectedInputExplanationMsgCode, String[] parameters, String context) {
        if (context != null) {
            if (yesToAllMap.get(context) != null && yesToAllMap.get(context)) {
                return UserInputCode.YES;
            } else if (noToAllMap.get(context) != null && noToAllMap.get(context)) {
                return UserInputCode.NO;
            }
        }
        showMessage(expectedInputExplanationMsgCode, parameters);
        // otherwise do the system.in
        Scanner scan = new Scanner(System.in);
        String answer = scan.nextLine();

        if (answer.toUpperCase().equals("Y") ||
                answer.toUpperCase().equals("YES")) {
            // just continue;
            return UserInputCode.YES;
        } else if (answer.toUpperCase().equals("N") ||
                answer.toUpperCase().equals("NO")) {
            // just continue;
            return UserInputCode.NO;
        } else if (answer.toUpperCase().equals("NA")) {
            // just continue;
            if (context != null) {
                noToAllMap.put(context, true);
            }
            return UserInputCode.NO;
        } else if (answer.toUpperCase().equals("A")) {
            // just continue;
            if (context != null) {
                yesToAllMap.put(context, true);
            }
            return UserInputCode.YES;
        }
        return UserInputCode.NO;
    }
}
