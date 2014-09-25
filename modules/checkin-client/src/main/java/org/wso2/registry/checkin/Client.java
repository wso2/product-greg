/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.registry.checkin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.registry.synchronization.SynchronizationException;
import org.wso2.carbon.registry.synchronization.message.MessageCode;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * check-in client class
 *
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class Client
{
    private static final Log log = LogFactory.getLog(Client.class);
    
    public final static int CHECKOUT = 1;
    public final static int CHECK_IN = 2;
    public final static int UPDATE = 3;
    public final static int ADD = 4;
    public final static int DELETE = 5;
    public final static int PROPSET = 6;
    public final static int PROPDELETE = 7;
    public final static int STATUS = 8;

    ClientOptions clientOptions;

    public Client() {
        this(new ClientOptions());
    }

    public Client(ClientOptions clientOptions) {
        this.clientOptions = clientOptions;
    }

    public void start(String[] arguments) throws Exception {
        initializeLog4j();
        UserInteractor userInteractor = new DefaultUserInteractor();
        clientOptions.setUserInteractor(userInteractor);
        try {
            execute(arguments);
        } catch (SynchronizationException e) {
            MessageCode exceptionMsgCode  = e.getCode();
            String[] parameters = e.getParameters();
            // In the command line tool, we will just print the message code
            String msg = userInteractor.showMessage(exceptionMsgCode, parameters);
            log.error(msg, e);
        }
    }

    public void execute(String[] arguments) throws Exception {
        int operation = -1;
        if (arguments.length == 0) {
            throw new SynchronizationException(MessageCode.NO_OPTIONS_PROVIDED);
        }
        if (arguments.length == 1 && arguments[0].equals("-h")) {
            ClientUtils.printMessage(clientOptions, MessageCode.HELP);
            return;
        }

        // now loop through the arguments list to capture the options
        for (int i = 0; i < arguments.length; i ++) {
            if (operation == -1) {
                if (arguments[i].equals("checkout") || arguments[i].equals("co")) {
                    if ( arguments.length <= i) {
                        throw new SynchronizationException(MessageCode.CO_PATH_MISSING);
                    }
                    i ++;
                    if (arguments.length -1 == i) {
                        throw new SynchronizationException(MessageCode.CO_PATH_MISSING);
                    }
                    String url = arguments[i];
                    clientOptions.setUserUrl(url);
                    operation = CHECKOUT;
                }
                else if (arguments[i].equals("ci") || arguments[i].equals("checkin")) {
                    operation = CHECK_IN;
                    if (arguments.length > i + 1 ) {
                        String url = arguments[i +1];
                        if (url.startsWith("http") || url.startsWith("/")) {
                            clientOptions.setUserUrl(url);
                            i ++;
                        }

                        if (!arguments[i + 1].startsWith("-")) {
                            clientOptions.setWorkingLocation(arguments[i + 1]);
                            i ++;
                        }
                    }
                }
                else if(arguments[i].equals("up") || arguments[i].equals("update")) {
                    operation = UPDATE;
                    if (arguments.length > i + 1 ) {
                        String url = arguments[i +1];
                        if (url.startsWith("http") || url.startsWith("/")) {
                            clientOptions.setUserUrl(url);
                            i ++;
                        }
                    }
                }
                else if(arguments[i].equals("add")){
                    operation = ADD;
                    if(arguments.length > i + 1){
                        String targetResource = arguments[i+1];
                        clientOptions.setTargetResource(targetResource);
                        i++;
                        if(arguments.length > i + 1 && "--mediatype".equals(arguments[i+1])){
                            clientOptions.setMediatype(arguments[i+2]);
                            i = i+2;
                        }

                    }
                }
                else if(arguments[i].equals("delete") || arguments[i].equals("del")){
                    operation = DELETE;
                    if(arguments.length > i + 1){
                        String targetResource = arguments[i+1];
                        clientOptions.setTargetResource(targetResource);
                        break;  //No more arguments
                    }
                }
                else if(arguments[i].equals("propset") || arguments[i].equals("pset")){
                    operation = PROPSET;
                    if(arguments.length > i + 1){
                        String targetResource = arguments[++i];
                        clientOptions.setTargetResource(targetResource);
                    }
                    //Get the key value pairs
                    Map<String,String> propertyMap = new HashMap<String, String>();
                    for( ; i<arguments.length - 1; ){
                        String propertyKey = arguments[++i];
                        String propertyValue = arguments[++i];
                        propertyMap.put(propertyKey, propertyValue);
                    }
                    clientOptions.setProperties(propertyMap);
                    break; //No more arguments
                }
                else if(arguments[i].equals("propdelete") || arguments[i].equals("pdel")){
                    operation = PROPDELETE;
                    if(arguments.length > i + 1){
                        String targetResource = arguments[++i];
                        clientOptions.setTargetResource(targetResource);
                    }
                    Set<String> propertySet = new HashSet<String>();
                    for(; i<arguments.length-1; ){
                        propertySet.add(arguments[++i]);
                    }
                    clientOptions.setDeletedProperties(propertySet);
                    break; //No more arguments
                }
                else if(arguments[i].equals("status")) {
                    operation = STATUS;
                    if(arguments.length > i + 1){
                        String targetResource = arguments[i+1];
                        clientOptions.setTargetResource(targetResource);
                        break;  //No more arguments
                    }
                }

            }

            if (arguments[i].equals("-u") || arguments[i].equals("--user")) {
                if (arguments.length -1 == i) {
                    throw new SynchronizationException(MessageCode.USERNAME_MISSING);
                }
                i ++;
                String username = arguments[i];
                clientOptions.setUsername(username);
            }
            if (arguments[i].equals("-p") || arguments[i].equals("--password")) {
                if (arguments.length -1 == i) {
                    throw new SynchronizationException(MessageCode.PASSWORD_MISSING);
                }
                i ++;
                String password = arguments[i];
                clientOptions.setPassword(password);
            }
            if (arguments[i].equals("-l") || arguments[i].equals("--location")) {
                if (arguments.length -1 == i) {
                    throw new SynchronizationException(MessageCode.WORKING_DIR_MISSING);
                }
                i ++;
                String workingLocation = arguments[i];
                File workingFile = new File(workingLocation);
                if (workingFile.exists()) {
                    /*if (!workingFile.isDirectory()) {
                        throw new SynchronizationException(MessageCode.WRONG_WORKING_DIR);
                    }*/
                }
                else {
                    // ignores the return value
                    workingFile.mkdirs();
                }
                clientOptions.setWorkingLocation(workingLocation);
            }
            if (arguments[i].equals("-f") || arguments[i].equals("--filename")) {
                if (arguments.length -1 == i) {
                    throw new SynchronizationException(MessageCode.DUMP_FILE_MISSING);
                }
                i ++;
                String outputFile = arguments[i];
                clientOptions.setOutputFile(outputFile);
            }
            if (arguments[i].equals("-i") || arguments[i].equals("--interactive")) {
                clientOptions.setInteractive(true);
            }
            if (arguments[i].equals("-t") || arguments[i].equals("--type")) {
                if (arguments.length -1 == i) {
                    throw new SynchronizationException(MessageCode.REGISTRY_TYPE_MISSING);
                }
                i ++;
                String type = arguments[i].toUpperCase();
                clientOptions.setType(ClientOptions.RegistryType.valueOf(type));
            }
            if (arguments[i].equals("--tenant")) {
                if (arguments.length - 1 != i) {
                    i++;
                    clientOptions.setTenantId(Integer.parseInt(arguments[i]));
                }
            }
        }

        if (operation == -1) {
            throw new SynchronizationException(MessageCode.OPERATION_NOT_FOUND);
        }

        if ( (clientOptions.getUsername() == null || clientOptions.getUsername().equals("")) &&
             (operation==CHECK_IN || operation==CHECKOUT || operation==UPDATE)) {
            throw new SynchronizationException(MessageCode.USERNAME_NOT_PROVIDED);
        }

        try {
        		ClientUtils.setSystemProperties();
        } catch (Exception e) {
        		throw new Exception("Faild to set system properties",e);
        }

        if( null != clientOptions.getUserUrl() && clientOptions.getUserUrl().startsWith("/")) {
            // Enforce the initialization of the CarbonContextHolder if it's run from local registry. This
            // will make it possible to do required initializations for Multi-Tenant JNDI and caching.
            CarbonContext.getThreadLocalCarbonContext();

        }
        // now call the checkout operation.
        if (operation == CHECKOUT) {
            Checkout checkout = new Checkout(clientOptions);
            checkout.execute();
        }
        else if (operation == CHECK_IN) {
            new Checkin(clientOptions).execute();
        }
        else if (operation == UPDATE) {
            Update update = new Update(clientOptions);
            update.execute();
        }
        else if (operation == ADD) {
            Add add = new Add(clientOptions);
            add.execute();
        }
        else if (operation == DELETE) {
            Delete delete = new Delete(clientOptions);
            delete.execute();
        }
        else if (operation == PROPSET) {
            PropSet propSet = new PropSet(clientOptions);
            propSet.execute();
        }
        else if (operation == PROPDELETE) {
            PropDelete propDelete = new PropDelete(clientOptions);
            propDelete.execute();
        }
        else if (operation == STATUS) {
            Status status = new Status(clientOptions);
            status.execute();
        }


    }

    private static void initializeLog4j() {
        String log4jConfFile = "log4j.properties";
        String carbonHome = CarbonUtils.getCarbonHome();
        if (carbonHome != null) {
            log4jConfFile = carbonHome + "/lib/checkin-client/log4j.properties";
        }
        if (new File(log4jConfFile).exists()) {
            PropertyConfigurator.configure(log4jConfFile);
        }
    }
}
