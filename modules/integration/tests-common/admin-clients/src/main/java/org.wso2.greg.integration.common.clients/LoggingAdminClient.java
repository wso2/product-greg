/*
*Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.greg.integration.common.clients;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.logging.admin.stub.LoggingAdminException;
import org.wso2.carbon.logging.admin.stub.LoggingAdminStub;

import java.rmi.RemoteException;

/**
 * This class is used to enable logging for server. This is a client for the logging admin service.
 */
public class LoggingAdminClient {

    private final String serviceName = "LoggingAdmin";
    private String endpoint = null;
    private LoggingAdminStub loggingAdminStub;

    /**
     * Constructor used to initiate logging admin client.
     * @param  backEndUrl       server url.
     * @param  sessionCookie    session cookie of the server.
     */
    public LoggingAdminClient(String backEndUrl, String sessionCookie) throws AxisFault {
        this.endpoint = backEndUrl + serviceName;
        loggingAdminStub = new LoggingAdminStub(this.endpoint);
        AuthenticateStub.authenticateStub(sessionCookie, loggingAdminStub);
    }

    /**
     * Constructor used to initiate logging admin client.
     * @param  backEndUrl       server url.
     * @param  userName         Logged in user.
     * @param  password         password for the user.
     */
    public LoggingAdminClient(String backEndUrl, String userName, String password) throws AxisFault {
        this.endpoint = backEndUrl + serviceName;
        loggingAdminStub = new LoggingAdminStub(this.endpoint);
        AuthenticateStub.authenticateStub(userName, password, loggingAdminStub);
    }

    /**
     * Update the System Logging configuration. The global logging level & the package which logging to be enabled
     * will be updated by this method.
     *
     * @param loggerName The package which logging to be enabled.
     * @param logLevel   The global log level to be set.
     * @param additivity  if true all the levels below specified logLevel will be logged.
     * @param persist    true - indicates persist these changes to the DB; false - indicates make
     *                   changes only in memory and do not persist the changes to DB.
     *
     * @return Status of the function.
     *
     * @throws RemoteException
     * @throws LoggingAdminException
     */
    public boolean updateLoggerData(String loggerName, String logLevel, boolean additivity, boolean persist)
            throws RemoteException, LoggingAdminException {
        loggingAdminStub.updateLoggerData(loggerName, logLevel, additivity, persist);
        return true;
    }
}
