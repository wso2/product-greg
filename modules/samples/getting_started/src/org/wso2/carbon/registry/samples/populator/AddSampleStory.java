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
    private static String fileSeperator = File.separator + File.separator + File.separator;
    private static String serverURL;
    private static final String MEDIA_TYPE_SWAGGER = "application/swagger+json";
    private static final String MEDIA_TYPE_WSDL = "application/wsdl+xml";
    private static final String MEDIA_TYPE_POLICY = "application/policy+xml";
    private static ResourceServiceClient resourceServiceClient;
    private static LifeCycleManagementClient lifeCycleManagementClient;
    private static String governancePath = "/_system/governance";
    private static final String[] DEV_ROLE_PERMISSION = {"/permission/admin/login",
            "/permission/admin/enterprisestore",
            "/permission/admin/manage"};
    private static final String[] DEVOPS_ROLE_PERMISSION = {"/permission/admin/login",
            "/permission/admin/enterprisestore",
            "/permission/admin/manage"};
    private static final String[] QAMGR_ROLE_PERMISSION = {"/permission/admin/login",
            "/permission/admin/enterprisestore",
            "/permission/admin/manage"};
    private static final String[] STRGMGR_ROLE_PERMISSION = {"/permission/admin/login",
            "/permission/admin/enterprisestore",
            "/permission/admin/manage"};

    private static void setSystemProperties() {
        String trustStore = System.getProperty("carbon.home") + File.separator + "repository" + File.separator +
                "resources" + File.separator + "security" + File.separator + "wso2carbon.jks";
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
            lifeCycleManagementClient.createLifecycle(readFile(projectPath + File.separator + "resources" +
                    File.separator + "BuyMoreLC.xml"));

            Thread.sleep(5 * 1000);

            //String[][] developmentProperties = {{"version", "3.0.0"}};
            String[][] testingProperties = {{"version", "2.0.0"}};
            String[][] productionProperties = {{"version", "1.0.0"}};
            /*addSwagger("Adding a swagger 2.0 definition from file. ",
                    new DataHandler(new URL("file:" + fileSeperator + projectPath + File.separator +
                            "resources" + File.separator + "calc-swagger-v3.json")), developmentProperties);*/
            addSwagger("Adding a swagger 2.0 definition from file. ",
                    new DataHandler(new URL("file:" + fileSeperator + projectPath + File.separator +
                            "resources" + File.separator + "calc-swagger-v2.json")),testingProperties);
            addSwagger("Adding a swagger 2.0 definition from file. ",
                    new DataHandler(new URL("file:" + fileSeperator + projectPath + File.separator +
                            "resources" + File.separator + "calc-swagger.json")), productionProperties);
            addSwagger("Adding a swagger 2.0 definition from file. ",
                    new DataHandler(new URL("file:" + fileSeperator + projectPath + File.separator +
                            "resources" + File.separator + "loyalty-swagger.json")), productionProperties);
            /*addSwagger("Adding a swagger 2.0 definition from file. ", new DataHandler( new URL("file:" +
                    fileSeperator + projectPath + File.separator + "resources" + File.separator
                            + "loyalty-swagger-v2.json")), testingProperties);*/

            //adding legacy service with a WSDL and attaching a policy to it
            addWsdl("Adding the WSDL file file. ", new DataHandler(new URL("file:" + fileSeperator + projectPath +
                    File.separator + "resources" + File.separator + "BuyMore.wsdl")), null);
            addPolicy("Adding the WS-Policy file. ", new DataHandler(new URL("file:" + fileSeperator + projectPath +
                    File.separator + "resources" + File.separator + "BuyMoreUTPolicy.xml")), null);

            System.out.println("Added Swagger files, WSDL and WS-Policy");
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
                genericArtifact.attachLifecycle("BuyMoreLifeCycle");
                genericArtifact.addAttribute("endpoints_entry", "Dev:http://dev.buymore.com/services/bill_calculator");
                genericArtifact.addAttribute("endpoints_entry", "Test:http://qa.buymore" +
                        ".com/services/bill_calculator");
                artifactManager.updateGenericArtifact(genericArtifact);
                if (genericArtifact.getAttribute("overview_version").equals("2.0.0")) {
                    changeLcState("Promote", genericArtifact.getPath());
                    if (loyaltyServices != null && loyaltyService.length() > 0) {
                        for (GenericArtifact loyaltyServiceArtifact : loyaltyServices) {
                            loyaltyServiceArtifact.attachLifecycle("BuyMoreLifeCycle");
                            loyaltyServiceArtifact.addAttribute("endpoints_entry", "Dev:http://dev.buymore.com/services/loyal_customer");
                            loyaltyServiceArtifact.addAttribute("endpoints_entry", "Test:http://qa.buymore" +
                                    ".com/services/loyal_customer");
                            artifactManager.updateGenericArtifact(loyaltyServiceArtifact);
                            if (loyaltyServiceArtifact.getAttribute("overview_version").equals("1.0.0")) {
                                genericArtifact.addAssociation("DependsOn", loyaltyServiceArtifact);
                                loyaltyServiceArtifact.addAssociation("UsedBy", genericArtifact);
                                changeLcState("Promote", loyaltyServiceArtifact.getPath());
                            }
                        }
                    }
                } else if (genericArtifact.getAttribute("overview_version").equals("1.0.0")) {
                    changeLcState("Promote", genericArtifact.getPath());
                    changeLcState("Promote", genericArtifact.getPath());
                    changeLcState("Promote", genericArtifact.getPath());
                } else if (genericArtifact.getAttribute("overview_version").equals("3.0.0")) {
                    if (loyaltyServices != null && loyaltyService.length() > 0) {
                        for (GenericArtifact loyaltyServiceArtifact : loyaltyServices) {
                            if (loyaltyServiceArtifact.getAttribute("overview_version").equals("2.0.0")) {
                                genericArtifact.addAssociation("DependsOn", loyaltyServiceArtifact);
                                loyaltyServiceArtifact.addAssociation("UsedBy", genericArtifact);
                            }
                        }
                    }
                }
            }

            GenericArtifactManager soapServiceManager = new GenericArtifactManager(gov, "soapservice");
            final String buyMoreSoapService = "BuyMore";
            GenericArtifact[] soapService = soapServiceManager.findGenericArtifacts(new GenericArtifactFilter() {

                @Override
                public boolean matches(GenericArtifact genericArtifact) throws GovernanceException {
                    return buyMoreSoapService.equals(genericArtifact.getQName().getLocalPart());
                }
            });

            if (soapService != null && soapService.length > 0) {
                soapService[0].attachLifecycle("BuyMoreLifeCycle");
                changeLcState("Promote", soapService[0].getPath());
                Thread.sleep(1 * 500);
                changeLcState("Promote", soapService[0].getPath());
                Thread.sleep(1 * 500);
                changeLcState("Promote", soapService[0].getPath());
                Thread.sleep(1 * 500);
                changeLcState("Deprecate", soapService[0].getPath());
                Thread.sleep(1 * 500);
                GenericArtifactManager policyManager = new GenericArtifactManager(gov, "policy");
                final String buyMorePolicy = "BuyMoreUTPolicy.xml";
                GenericArtifact[] policy = policyManager.findGenericArtifacts(new GenericArtifactFilter() {

                    @Override
                    public boolean matches(GenericArtifact genericArtifact) throws GovernanceException {
                        return buyMorePolicy.equals(genericArtifact.getQName().getLocalPart());
                    }
                });

                if (policy != null && policy.length > 0) {
                    soapService[0].addAssociation("DependsOn", policy[0]);
                    policy[0].addAssociation("EnforcedsOn", soapService[0]);
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
        fileName = dh.getName().substring(dh.getName().lastIndexOf(File.separator) + 1);
        if (fileName.contains("-v")) {
            fileName = fileName.substring(0,fileName.lastIndexOf('-'));
        } else {
            fileName = fileName.substring(0,fileName.lastIndexOf('.'));
        }
        resourceServiceClient.addResource(File.separator + fileName, MEDIA_TYPE_SWAGGER, description, dh, null, props);
    }

    private static void addWsdl(String description, DataHandler dh, String[][] props)
            throws Exception {
        String fileName;
        fileName = dh.getName().substring(dh.getName().lastIndexOf(File.separator) + 1);
        resourceServiceClient.addResource(File.separator + fileName, MEDIA_TYPE_WSDL, description, dh, null, props);
    }

    private static void addPolicy(String description, DataHandler dh, String[][] props)
            throws Exception {
        String fileName;
        fileName = dh.getName().substring(dh.getName().lastIndexOf(File.separator) + 1);
        resourceServiceClient.addResource(File.separator + fileName, MEDIA_TYPE_POLICY, description, dh, null, props);
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
                new FileReader(rootpath + "resources" + File.separator + "users_list.txt"));
        String userNamePwd;
        String publisherUser = "";
        String publisherUserPassword = "";
        String demoUserRole = "";

        //userManager.addRole("dev", null, DEV_ROLE_PERMISSION);
        userManager.addRole("devops", null, DEVOPS_ROLE_PERMISSION);
        //userManager.addRole("qamanager", null, QAMGR_ROLE_PERMISSION);
        //userManager.addRole("strategymanager", null, STRGMGR_ROLE_PERMISSION);
        userManager.addRole("consumer", null, STRGMGR_ROLE_PERMISSION);

        while ((userNamePwd = bufferedReader.readLine()) != null) {
            if (userNamePwd != null && !userNamePwd.equals("")) {
                String [] credentials = userNamePwd.split(":");
                publisherUser = credentials[0];
                publisherUserPassword = credentials[1];
                demoUserRole = credentials[2];
                System.out.println("New user added. Please use following credentials to login.\n");
                System.out.println("Username : "+publisherUser+"\n");
                System.out.println("Password : "+publisherUserPassword+"\n");
                System.out.println("Role : "+demoUserRole+"\n");
                String[] role = {demoUserRole};
                userManager.addUser(publisherUser, publisherUserPassword, role, new ClaimValue[0], null);
            }
        }

        System.out.println("********************************************************************************\n\n");
    }

}
