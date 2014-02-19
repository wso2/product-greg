/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.carbon.registry.capp.deployment.test.utils;

import org.wso2.carbon.application.mgt.stub.ApplicationAdminExceptionException;
import org.wso2.carbon.automation.api.clients.application.mgt.ApplicationAdminClient;

import java.rmi.RemoteException;
import java.util.Calendar;

public class CAppTestUtils {

    public static boolean isCAppDeployed(String sessionCookie, String cAppName,
                                         ApplicationAdminClient adminServiceApplicationAdmin)
            throws ApplicationAdminExceptionException, RemoteException, InterruptedException {
        String[] appList;
        boolean isFound = false;
        Calendar startTime = Calendar.getInstance();
        long time;
        while ((time = (Calendar.getInstance().getTimeInMillis() - startTime.getTimeInMillis())) < 90000) {
            appList = adminServiceApplicationAdmin.listAllApplications();
            if (appList != null) {

                for (String cApp : appList) {
                    if (cAppName.equalsIgnoreCase(cApp)) {
                        isFound = true;
                        break;
                    }
                }
            }
            if (isFound) {
                break;
            }
            Thread.sleep(5000);
        }

        return isFound;
    }

    public static boolean isCAppDeleted(String sessionCookie, String cAppName,
                                        ApplicationAdminClient adminServiceApplicationAdmin)
            throws ApplicationAdminExceptionException, RemoteException, InterruptedException {
        String[] appList;
        boolean isDeleted = true;
        Calendar startTime = Calendar.getInstance();
        long time;
        while ((time = (Calendar.getInstance().getTimeInMillis() - startTime.getTimeInMillis())) < 60000) {
            isDeleted = true;
            appList = adminServiceApplicationAdmin.listAllApplications();
            if (appList != null) {
                for (String cApp : appList) {
                    if (cAppName.equalsIgnoreCase(cApp)) {
                        isDeleted = false;
                        break;
                    }
                }
            }
            if (isDeleted) {
                break;
            }
            Thread.sleep(5000);
        }

        return isDeleted;
    }
}
