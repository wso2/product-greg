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
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactFilter;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.samples.populator.utils.LifeCycleManagementClient;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.carbon.registry.samples.populator.utils.UserManagementClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
* This class is used to delete all the added sample data.
 */
public class CleanUp {

    private static String cookie;
    private static String rootpath = "";
    private static final String username = "admin";
    private static final String password = "admin";
    private static String port ;
    private static String host ;
    private static String serverURL;

    private static void setSystemProperties() {
        StringBuilder builder = new StringBuilder();
        builder.append(System.getProperty("carbon.home")).append(File.separator).append("repository")
                .append(File.separator).append("resources").append(File.separator).append("security")
                .append(File.separator).append("wso2carbon.jks");
        String trustStore = builder.toString();
        System.setProperty("javax.net.ssl.trustStore", trustStore);
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
        System.setProperty("javax.net.ssl.trustStoreType", "JKS");
        System.setProperty("carbon.repo.write.mode", "true");
        if (System.getProperty("carbon.home").equals(".." + File.separator + ".." + File.separator + ".." + File
                .separator)) {
            rootpath = ".." + File.separator;
        }
    }

    public static void main(String[] args) {
        try {
            port = args[0];
            if(port == null || port.length() ==0){
                port = "9443";
            }
            host =args [1];
            if(host == null || host.length() ==0){
                host = "localhost";
            }
            serverURL = "https://"+host+":"+port+"/services/";
            setSystemProperties();

            StringBuilder builder = new StringBuilder();
            builder.append(System.getProperty("carbon.home")).append(File.separator).append("repository")
                    .append(File.separator).append("conf").append(File.separator).append("axis2")
                    .append(File.separator).append("axis2_client.xml");
            String axis2Configuration = builder.toString();
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

            try {
                System.out.println("Deleting sample wsdls .........");
                deleteArtifacts(gov, "wsdl", "wsdl");
                System.out.println("########## Successfully deleted sample wsdls ###########\n\n");
            } catch (Exception e){
                System.out.println("######## Unable to delete sample wsdls ########\n\n");
            }

            try {
                System.out.println("Deleting sample wadls .........");
                deleteArtifacts(gov, "wadl", "wadl");
                System.out.println("########## Successfully deleted sample wadls ###########\n\n");
            } catch (Exception e){
                System.out.println("######## Unable to delete sample wadls ########\n\n");
            }

            try {
                System.out.println("Deleting sample schemas .........");
                deleteArtifacts(gov, "schema", "xsd");
                System.out.println("########## Successfully deleted sample schemas ###########\n\n");
            } catch (Exception e){
                System.out.println("######## Unable to delete sample schemas ########\n\n");

            }

            try {
                System.out.println("Deleting sample swagger docs .........");
                deleteArtifacts(gov, "swagger", "json");
                deleteImportedSwaggers(gov, "swagger");
                System.out.println("########## Successfully deleted sample swagger docs ###########\n\n");
            } catch (Exception e){
                System.out.println("######## Unable to delete sample swagger docs ########\n\n");
            }

            try {
                System.out.println("Deleting sample policies .........");
                deleteArtifacts(gov, "policy", "xml");
                System.out.println("########## Successfully deleted sample policies ###########\n\n");
            } catch (Exception e){
                System.out.println("######## Unable to delete sample policies ########\n\n");

            }

            try {
                System.out.println("Deleting sample Soap Services .........");
                deleteServices(gov, "soapservice");
                System.out.println("########## Successfully deleted sample Soap Services ###########\n\n");
            } catch (Exception e){
                System.out.println("######## Unable to delete sample soap services ########\n\n");
            }

            try {
                System.out.println("Deleting sample Rest Services .........");
                deleteServices(gov, "restservice");
                System.out.println("########## Successfully deleted sample Rest Services ###########\n\n");
            } catch (Exception e){
                System.out.println("######## Unable to delete sample rest services ########\n\n");
            }

            try {
                System.out.println("Deleting sample users .........");
                BufferedReader bufferedReader = new BufferedReader(
                        new FileReader(rootpath + "resources" + File.separator + "users_list.txt"));
                String userNamePwd;
                List userList = new ArrayList<String>();
                List roleList = new ArrayList<String>();
                while ((userNamePwd = bufferedReader.readLine()) != null) {
                    if (userNamePwd != null && !userNamePwd.equals("")) {
                        String[] credentials = userNamePwd.split(":");
                        userList.add(credentials[0]);
                        if (credentials[2].contains(",")) {
                            String [] tempRoles = credentials[2].split(",");
                            for (int i = 0; i < tempRoles.length; i++) {
                                roleList.add(tempRoles[i]);
                            }
                        } else {
                            roleList.add(credentials[2]);
                        }
                    }
                }
                String[] users = new String[userList.size()];
                users = (String[])userList.toArray(users);
                deleteUsers(users, configContext);
                System.out.println("########## Successfully deleted sample users ###########\n\n");

                System.out.println("Deleting sample roles .........");
                String [] roles = new String[roleList.size()];
                roles = (String[])roleList.toArray(roles);
                deleteRoles(roles, configContext);
                System.out.println("########## Successfully deleted sample roles ###########\n\n");
            } catch (Exception e){
                System.out.println("######## Unable to delete sample users and roles ########\n\n");
            }

            try {
                System.out.println("Deleting sample life-cycles .........");
                String [] lifeCycles = {"BuyMoreLifeCycle"};
                deleteLifeCycles(lifeCycles, configContext);
                System.out.println("########## Successfully deleted sample life-cycles ###########\n\n");
            } catch (Exception e){
                System.out.println("######## Unable to delete sample life-cycles ########\n\n");
            }

            cleanUpMessage();

        } catch (Exception e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        System.exit(0);
    }

    /**
     *This method delete artifacts added by running sample
     *
     * @param govRegistry     registry instance.
     * @param shortName       rxt short name of the asset type.
     * @param extension       extension type of the asset.
     * @throws FileNotFoundException
     * @throws IOException
     * @throws RegistryException
     * @throws GovernanceException
     */
    private static void deleteArtifacts(Registry govRegistry, String shortName, String extension)
            throws FileNotFoundException, IOException, RegistryException, GovernanceException {
        BufferedReader bufferedReader = new BufferedReader(
                new FileReader(rootpath + "resources" + File.separator + shortName + "_list.txt"));
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

    /**
     *This method delete swagger docs imported using url.
     *
     * @param govRegistry     registry instance.
     * @param shortName       rxt short name of the asset type.
     * @throws FileNotFoundException
     * @throws IOException
     * @throws RegistryException
     * @throws GovernanceException
     */
    private static void deleteImportedSwaggers(Registry govRegistry, String shortName)
            throws FileNotFoundException, IOException, RegistryException, GovernanceException {
        BufferedReader bufferedReader = new BufferedReader(
                new FileReader(rootpath + "resources" + File.separator + "swagger_imported.txt"));
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

    /**
     *This method delete soap and rest services.
     *
     * @param govRegistry     registry instance.
     * @param shortName       rxt short name of the asset type.
     * @throws FileNotFoundException
     * @throws IOException
     * @throws RegistryException
     * @throws GovernanceException
     */
    private static void deleteServices(Registry govRegistry, String shortName)
            throws FileNotFoundException, IOException, RegistryException, GovernanceException {
        BufferedReader bufferedReader = new BufferedReader(
                new FileReader(rootpath + "resources" + File.separator + shortName + "_list.txt"));
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

    /**
     * This method is used to delete users
     *
     * @param userNames
     * @param configContext
     * @throws Exception
     */
    private static void deleteUsers(String [] userNames, ConfigurationContext configContext) throws Exception{
        if (userNames != null && userNames.length > 0) {
            UserManagementClient userManager = new UserManagementClient(cookie, serverURL, configContext);
            for (int i =0; i < userNames.length; i++) {
                userManager.deleteUser(userNames[i]);
            }
        }
    }

    /***
     * This method is used to delete roles
     *
     * @param rolesNames
     * @param configContext
     * @throws Exception
     */
    private static void deleteRoles(String [] rolesNames, ConfigurationContext configContext) throws Exception{
        if (rolesNames != null && rolesNames.length > 0) {
            UserManagementClient userManager = new UserManagementClient(cookie, serverURL, configContext);
            for (int i =0; i < rolesNames.length; i++) {
                userManager.deleteRole(rolesNames[i]);
            }
        }
    }

    /***
     * This method is used to delete roles
     *
     * @param lcNames
     * @param configContext
     * @throws Exception
     */
    private static void deleteLifeCycles(String [] lcNames, ConfigurationContext configContext) throws Exception{
        if (lcNames != null && lcNames.length > 0) {
            LifeCycleManagementClient lifeCycleManagementClient = new LifeCycleManagementClient(cookie, serverURL,
                    configContext);
            for (int i =0; i < lcNames.length; i++) {
                lifeCycleManagementClient.deleteLifecycle(lcNames[i]);
            }
        }
    }

    /**
     * This method is used to print the clean up message for the sample populator. .
     */
    private static void cleanUpMessage() {
        System.out.println("********************************************************************************\n");
        System.out.println("All sample data populated by this sample are deleted successfully.");
        System.out.println("********************************************************************************\n\n");
    }

}
