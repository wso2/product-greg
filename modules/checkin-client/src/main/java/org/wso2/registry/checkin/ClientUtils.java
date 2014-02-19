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

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.registry.app.RemoteRegistry;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.internal.RegistryCoreServiceComponent;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.synchronization.SynchronizationConstants;
import org.wso2.carbon.registry.synchronization.SynchronizationException;
import org.wso2.carbon.registry.synchronization.Utils;
import org.wso2.carbon.registry.synchronization.message.MessageCode;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.carbon.user.core.common.DefaultRealmService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ServerConstants;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class ClientUtils {

    public static void setSystemProperties() {
        if (System.getProperty("javax.net.ssl.trustStore") == null) {
            System.setProperty("javax.net.ssl.trustStore", System.getProperty("carbon.home") +
                    File.separator + "repository" + File.separator + "resources" + File.separator +
                    "security" + File.separator + "client-truststore.jks");
        }
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
        System.setProperty("javax.net.ssl.trustStoreType", "JKS");
        System.setProperty(ServerConstants.REPO_WRITE_MODE, Boolean.toString(true));
    }


    public static Registry newRegistry(String registryUrl,
                                       String username,
                                       String password,
                                       int tenantId,
                                       ClientOptions.RegistryType type)
            throws SynchronizationException {
        if (registryUrl != null) {
            try {
                switch (type) {
                    case WS:
                        ConfigurationContext context =
                                ConfigurationContextFactory
                                        .createConfigurationContextFromFileSystem(
                                                System.getProperty("carbon.home") +
                                                        File.separator + "repository" +
                                                        File.separator +
                                                        "deployment" + File.separator + "client",
                                                        ServerConfiguration.getInstance().getFirstProperty("Axis2Config.clientAxis2XmlLocation"));                                                                
                        if (registryUrl.endsWith("/")) {
                            registryUrl = registryUrl.substring(0, registryUrl.length() - 1);
                        }
                        String serverUrl = registryUrl.substring(0,
                                registryUrl.lastIndexOf("/")) + "/services/";
                        WSRegistryServiceClient wsRegistryServiceClient =
                                new WSRegistryServiceClient(serverUrl, username, password, context);
                        wsRegistryServiceClient.setTenantId(tenantId);
                        return wsRegistryServiceClient;
                    default:
                        return new RemoteRegistry(new URL(registryUrl), username, password);
                }
            } catch (MalformedURLException e) {
                throw new SynchronizationException(MessageCode.MALFORMED_URL, e,
                        new String[] {" registry url:" + registryUrl});
            } catch (RegistryException e) {
                throw new SynchronizationException(MessageCode.ERROR_IN_CONNECTING_REGISTRY, e,
                        new String[] {" registry url:" + registryUrl});
            } catch (AxisFault e) {
                throw new SynchronizationException(MessageCode.ERROR_IN_CONNECTING_REGISTRY, e,
                        new String[] {" registry url:" + registryUrl});
            }
        }

        // we need to get a registry service, but since we are not on top of OSGi instead we create 
        // a fake RegistryService and reuse it to get an instance of Registry

        // first we need to create a realm service, passing bundle context as null
        RealmService realmService;
        try {
            realmService = new DefaultRealmService(null);
        } catch (Exception e) {
            throw new SynchronizationException(MessageCode.REALM_SERVICE_FAILED, e);
        }
        
        // create a embedding registry core service component  inner class
        class EmbeddingRegistryCoreServiceComponent extends
                RegistryCoreServiceComponent {

            public void setRealmService(RealmService realmService) {
                super.setRealmService(realmService);
            }
        }

        // now we create an instance of RegistryCoreServiceComponent
        EmbeddingRegistryCoreServiceComponent registryComponent =
                new EmbeddingRegistryCoreServiceComponent();
        // setting the realm service, the only service that the registry component depends on
        registryComponent.setRealmService(realmService);

        // now we can build the registryService, it is like calling the start of the service 
        // component except setting the registry service registered in the bundle context
        RegistryService registryService;
        try {
            registryService = registryComponent.buildRegistryService();
        } catch (Exception e) {
            throw new SynchronizationException(MessageCode.REGISTRY_SERVICE_FAILED, e);
        }
        try {
            return registryService.getRegistry(username, password);
        } catch (RegistryException e) {
            throw new SynchronizationException(MessageCode.USER_REGISTRY_FAILED, e);
        }
    }

    public static void printMessage(ClientOptions clientOptions, MessageCode messageCode) {
        UserInteractor userInteractor = clientOptions.getUserInteractor();
        userInteractor.showMessage(messageCode, null);
    }

    public static void printMessage(ClientOptions clientOptions,
                                    MessageCode messageCode, String[] parameters) {
        UserInteractor userInteractor = clientOptions.getUserInteractor();
        userInteractor.showMessage(messageCode, parameters);
    }

    public static UserInputCode confirmMessage(ClientOptions clientOptions,
                                               MessageCode messageCode,
                                               String[] parameters,
                                               String context) {
        if (clientOptions.isTesting()) {
            return UserInputCode.YES;
        }
        UserInteractor userInteractor = clientOptions.getUserInteractor();
        return userInteractor.getInput(messageCode, parameters, context);
    }

    public static String getMetaFilePath(String path) throws SynchronizationException {
        File file = new File(path);
        if(file.isDirectory()){
            return path + File.separator + SynchronizationConstants.META_DIRECTORY +
                    File.separator +
                    SynchronizationConstants.META_FILE_PREFIX +
                    SynchronizationConstants.META_FILE_EXTENSION;
        } else {
            String parentDirName = file.getParent();
            return parentDirName + File.separator + SynchronizationConstants.META_DIRECTORY +
                    File.separator + SynchronizationConstants.META_FILE_PREFIX +
                    Utils.encodeResourceName(file.getName()) +
                    SynchronizationConstants.META_FILE_EXTENSION;
        }
    }

}

