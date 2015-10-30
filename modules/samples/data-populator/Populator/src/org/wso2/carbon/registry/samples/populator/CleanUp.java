/*
 *  Copyright (c) 2015 WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.wso2.carbon.registry.samples.populator;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.resource.ui.clients.ResourceServiceClient;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.GenericArtifactFilter;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.registry.core.pagination.PaginationContext;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import javax.activation.DataHandler;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.String;
import java.net.URL;
import java.io.File;
import java.io.FileNotFoundException;

public class CleanUp {

    private static String cookie;
    private static String rootpath = "";
    private static final String username = "admin";
    private static final String password = "admin";
    private static final String serverURL = "https://localhost:9443/services/";

    private static void setSystemProperties() {
        String trustStore = System.getProperty("carbon.home") + File.separator + "repository" + File.separator +
                "resources" + File.separator + "security" + File.separator + "wso2carbon.jks";
        System.setProperty("javax.net.ssl.trustStore", trustStore);
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
        System.setProperty("javax.net.ssl.trustStoreType", "JKS");
        System.setProperty("carbon.repo.write.mode", "true");
        if (System.getProperty("carbon.home").equals("../../../../")) {
            rootpath = "../";
        }
    }

    public static void main(String[] args) {
        try {

            setSystemProperties();

            String axis2Configuration = System.getProperty("carbon.home") + File.separator + "repository" +
                    File.separator + "conf" + File.separator + "axis2" + File.separator + "axis2_client.xml";
            ConfigurationContext configContext = ConfigurationContextFactory
                    .createConfigurationContextFromFileSystem(axis2Configuration);

            Registry registry = new WSRegistryServiceClient(serverURL, username, password, configContext) {

                public void setCookie(String cookie) {
                    CleanUp.cookie = cookie;
                    super.setCookie(cookie);
                }
            };
            Registry gov = GovernanceUtils.getGovernanceUserRegistry(registry, "admin");
            GovernanceUtils.loadGovernanceArtifacts((UserRegistry) gov);
            deleteArtifacts(gov, "wsdl", "wsdl");
            System.out.println("########## Successfully deleted sample wsdls ###########");
            deleteArtifacts(gov, "wadl", "wadl");
            System.out.println("########## Successfully deleted sample wadls ###########");
            deleteArtifacts(gov, "schema", "xsd");
            System.out.println("########## Successfully deleted sample schemas ###########");
            deleteArtifacts(gov, "swagger", "json");
            System.out.println("########## Successfully deleted sample swagger docs ###########");
            deleteArtifacts(gov, "policy", "xml");
            System.out.println("########## Successfully deleted sample policies ###########");
            deleteServices(gov, "soapservice");
            System.out.println("########## Successfully deleted sample Soap Services ###########");
            deleteServices(gov, "restservice");
            System.out.println("########## Successfully deleted sample Rest Services ###########");

        } catch (Exception e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        System.exit(0);
    }

    private static void deleteArtifacts(Registry govRegistry, String shortName, String extension)
            throws FileNotFoundException, IOException, RegistryException, GovernanceException {
        BufferedReader bufferedReader = new BufferedReader(
                new FileReader(rootpath + "resources/" + shortName + "_list.txt"));
        String artifactName;
        GenericArtifactManager manager = new GenericArtifactManager(govRegistry, shortName);
        while ((artifactName = bufferedReader.readLine()) != null) {

            final String name = artifactName + "." + extension;
            GenericArtifact[] artifacts = manager.findGenericArtifacts(new GenericArtifactFilter() {

                @Override public boolean matches(GenericArtifact genericArtifact) throws GovernanceException {
                    return name.equals(genericArtifact.getQName().getLocalPart());
                }
            });
            for (GenericArtifact genericArtifact : artifacts) {
                for (GovernanceArtifact dependency : genericArtifact.getDependencies()) {
                    GovernanceUtils.removeArtifact(govRegistry, dependency.getId());
                }
                GovernanceUtils.removeArtifact(govRegistry, genericArtifact.getId());
            }
        }
    }

    private static void deleteServices(Registry govRegistry, String shortName)
            throws FileNotFoundException, IOException, RegistryException, GovernanceException {
        BufferedReader bufferedReader = new BufferedReader(
                new FileReader(rootpath + "resources/" + shortName + "_list.txt"));
        String artifactName;
        GenericArtifactManager manager = new GenericArtifactManager(govRegistry, shortName);

        while ((artifactName = bufferedReader.readLine()) != null) {

            final String name = artifactName;
            GenericArtifact[] artifacts = manager.findGenericArtifacts(new GenericArtifactFilter() {

                @Override public boolean matches(GenericArtifact genericArtifact) throws GovernanceException {
                    return name.equals(genericArtifact.getQName().getLocalPart());
                }
            });
            for (GenericArtifact genericArtifact : artifacts) {
                for (GovernanceArtifact dependency : genericArtifact.getDependencies()) {
                    GovernanceUtils.removeArtifact(govRegistry, dependency.getId());
                }
                GovernanceUtils.removeArtifact(govRegistry, genericArtifact.getId());
            }
        }
    }
}
