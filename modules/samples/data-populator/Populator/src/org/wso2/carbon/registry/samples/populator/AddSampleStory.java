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
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.resource.ui.clients.ResourceServiceClient;
import org.wso2.carbon.registry.samples.populator.utils.LifeCycleManagementClient;
import org.wso2.carbon.registry.samples.populator.utils.UserManagementClient;
import org.wso2.carbon.registry.samples.populator.utils.Utils;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.carbon.user.mgt.stub.types.carbon.ClaimValue;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

public class AddSampleStory {

    private static String cookie;
    private static final String username = "admin";
    private static final String password = "admin";
    private static String port;
    private static String host;
    private static String rootpath = "";
    private static String serverURL;
    private static final String MEDIA_TYPE_SWAGGER = "application/swagger+json";
    private static ResourceServiceClient resourceServiceClient;
    private static LifeCycleManagementClient lifeCycleManagementClient;
    private static final String serviceRxtPath = "/_system/governance/repository/components/org.wso2.carbon.governance/types/";
    private static String governancePath = "/_system/governance";
    private static final String[] DEMO_USER_PERMISSION = {"/permission/admin/login",
            "/permission/admin/enterprisestore",
            "/permission/admin/manage"};

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
            port = args[0];
            if (port == null || port.length() == 0) {
                port = "9443";
            }
            host = args[1];
            if (host == null || host.length() == 0) {
                host = "localhost";
            }
            serverURL = "https://" + host + ":" + port + "/services/";
            setSystemProperties();

            String axis2Configuration = System.getProperty("carbon.home") + File.separator + "repository" +
                    File.separator + "conf" + File.separator + "axis2" + File.separator + "axis2_client.xml";
            ConfigurationContext configContext = ConfigurationContextFactory
                    .createConfigurationContextFromFileSystem(axis2Configuration);

            Registry registry = new WSRegistryServiceClient(serverURL, username, password, configContext) {

                public void setCookie(String cookie) {
                    AddSampleStory.cookie = cookie;
                    super.setCookie(cookie);
                }
            };

            resourceServiceClient = new ResourceServiceClient(cookie, serverURL, configContext);
            lifeCycleManagementClient = new LifeCycleManagementClient(cookie, serverURL, configContext);

            String projectPath = System.getProperty("user.dir");
            startUpMessage();
            addUsers(configContext);
            lifeCycleManagementClient.createLifecycle(readFile(projectPath + "/resources/BuyMoreLC.xml"));

            String restServiceRxtPath = serviceRxtPath + "restservice.rxt";
            Utils.backUpRXTs(registry, restServiceRxtPath, "restserviceExisting.rxt");
            resourceServiceClient.delete(restServiceRxtPath);
            DataHandler dh1 = new DataHandler(new URL("file://" + projectPath + "/resources/buymore-restservice.rxt"));
            resourceServiceClient.addResource(restServiceRxtPath,
                    "application/vnd.wso2.registry-ext-type+xml", null, dh1, null, null);
            Thread.sleep(5 * 1000);

            String[][] developmentProperties = {{"version", "3.0.0"}};
            String[][] testingProperties = {{"version", "2.0.0"}};
            String[][] productionProperties = {{"version", "1.0.0"}};
            addSwagger("Adding a swagger 2.0 definition from file. ", new DataHandler(new URL("file:///" + projectPath + "/resources/calc-swagger-v3.json")), developmentProperties);
            addSwagger("Adding a swagger 2.0 definition from file. ", new DataHandler(new URL("file:///" + projectPath + "/resources/calc-swagger-v2.json")), testingProperties);
            addSwagger("Adding a swagger 2.0 definition from file. ", new DataHandler(new URL("file:///" + projectPath + "/resources/calc-swagger.json")), productionProperties);
            addSwagger("Adding a swagger 2.0 definition from file. ", new DataHandler(new URL("file:///" + projectPath + "/resources/loyalty-swagger.json")), productionProperties);
            addSwagger("Adding a swagger 2.0 definition from file. ", new DataHandler(new URL("file:///" + projectPath + "/resources/loyalty-swagger-v2.json")), testingProperties);

            System.out.println("Added Swagger");
            Thread.sleep(3 * 1000);

            Registry gov = GovernanceUtils.getGovernanceUserRegistry(registry, "admin");
            // Should be load the governance artifact.
            GovernanceUtils.loadGovernanceArtifacts((UserRegistry) gov);

            GenericArtifactManager artifactManager = new GenericArtifactManager(gov, "restservice");
            final String buyMoreService = "BuyMoreBillCalculateRESTAPI";
            GenericArtifact[] calcServices = artifactManager.findGenericArtifacts(new GenericArtifactFilter() {

                @Override
                public boolean matches(GenericArtifact genericArtifact) throws GovernanceException {
                    return buyMoreService.equals(genericArtifact.getQName().getLocalPart());
                }
            });

            final String loyaltyService = "BuyMoreLoyaltyRESTAPI";
            GenericArtifact[] loyaltyServices = artifactManager.findGenericArtifacts(new GenericArtifactFilter() {

                @Override
                public boolean matches(GenericArtifact genericArtifact) throws GovernanceException {
                    return loyaltyService.equals(genericArtifact.getQName().getLocalPart());
                }
            });

            System.out.println("Found " + calcServices.length + " calc services");
            System.out.println("Found " + loyaltyServices.length + " loyalty services");

            for (GenericArtifact genericArtifact : calcServices) {
                if (genericArtifact.getAttribute("overview_version").equals("2.0.0")) {
                    changeLcState("Promote", genericArtifact.getPath());
                    if (loyaltyServices != null && loyaltyService.length() > 0) {
                        for (GenericArtifact loyaltyServiceArtifact : loyaltyServices) {
                            if (loyaltyServiceArtifact.getAttribute("overview_version").equals("1.0.0")) {
                                genericArtifact.addAssociation("DependsOn", loyaltyServiceArtifact);
                                loyaltyServiceArtifact.addAssociation("UsedBy", genericArtifact);
                            }
                        }
                    }
                } else if (genericArtifact.getAttribute("overview_version").equals("1.0.0")) {
                    changeLcState("Promote", genericArtifact.getPath());
                    changeLcState("Promote", genericArtifact.getPath());
                } else if (genericArtifact.getAttribute("overview_version").equals("3.0.0")) {
                    if (loyaltyServices != null && loyaltyService.length() > 0) {
                        for (GenericArtifact loyaltyServiceArtifact : loyaltyServices) {
                            if (loyaltyServiceArtifact.getAttribute("overview_version").equals("2.0.0")) {
                                genericArtifact.addAssociation("DependsOn", loyaltyServiceArtifact);
                                loyaltyServiceArtifact.addAssociation("UsedBy", genericArtifact);
                                changeLcState("Promote", loyaltyServiceArtifact.getPath());
                            }
                        }
                    }
                }
            }
            completedMessage();
        } catch (Exception e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        System.exit(0);
    }


    /**
     * This method is used to print the starting message for the sample populator. .
     */
    private static void startUpMessage() {
        System.out.println("********************************************************************************\n");
        System.out.println("Sample Data population initiated. This may take some time depending on your system.\n");
        System.out.println("********************************************************************************\n\n");
    }

    /**
     * This method is used to print the completed message for the sample populator. .
     */
    private static void completedMessage() {
        System.out.println("********************************************************************************\n");
        System.out.println("Sample Data population completed. \n");
        System.out.println("********************************************************************************\n\n");
    }

    private static void addSwagger(String description, DataHandler dh, String[][] props)
            throws Exception {
        String fileName;
        fileName = dh.getName().substring(dh.getName().lastIndexOf('/') + 1);
        resourceServiceClient.addResource("/" + fileName, MEDIA_TYPE_SWAGGER, description, dh, null, props);
    }

    private static String readFile(String filePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }

    /**
     * This method assign life cycle state to asset based on state.
     *
     * @param state LC transition action
     * @param path  path of the resource.
     * @throws Exception
     */
    private static void changeLcState(String state, String path) throws Exception {
        path = governancePath + path;
        String items[] = {"false", "false", "false"};
        lifeCycleManagementClient.invokeAspect(path, "BuyMoreLifeCycle", state, items);
    }

    /**
     * This method create publisher and store user.
     *
     * @param configContext
     * @throws Exception
     */
    private static void addUsers(ConfigurationContext configContext) throws Exception {
        UserManagementClient userManager = new UserManagementClient(cookie, serverURL, configContext);
        BufferedReader bufferedReader = new BufferedReader(
                new FileReader(rootpath + "resources/" + "users_list.txt"));
        String userNamePwd;
        String publisherUser = "";
        String publisherUserPassword = "";
        while ((userNamePwd = bufferedReader.readLine()) != null) {
            if (userNamePwd != null && !userNamePwd.equals("")) {
                String [] credentials = userNamePwd.split(":");
                publisherUser = credentials[0];
                publisherUserPassword = credentials[1];
                System.out.println("New user added. Please use following credentials to login.\n");
                System.out.println("Username : "+publisherUser+"\n");
                System.out.println("Password : "+publisherUserPassword+"\n");
            }
        }
        userManager.addRole("demorole", null, DEMO_USER_PERMISSION);
        String[] demoRole = {"demorole"};
        userManager.addUser(publisherUser, publisherUserPassword, demoRole, new ClaimValue[0], null);
        System.out.println("********************************************************************************\n\n");
    }

}
