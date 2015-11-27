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


import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.user.mgt.stub.types.carbon.ClaimValue;
import org.wso2.carbon.registry.samples.populator.utils.UserManagementClient;
import org.wso2.carbon.registry.samples.populator.utils.SwaggerImportClient;
import org.wso2.carbon.registry.resource.ui.clients.ResourceServiceClient;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import javax.activation.DataHandler;
import java.lang.String;
import java.lang.Thread;
import java.net.URL;
import java.io.File;
import java.rmi.RemoteException;

/*
* This class is used to add sample wsdls ,wadls,swagger doces, policies and scehmas to G-Reg server.
 */
public class Main {
    private static String cookie;
    private static final String username = "admin";
    private static final String password = "admin";
    private static String port ;
    private static String host ;
    private static String serverURL;

    private static void setSystemProperties() {
        String trustStore = System.getProperty("carbon.home") + File.separator + "repository" + File.separator +
                "resources" + File.separator + "security" + File.separator + "wso2carbon.jks";
        System.setProperty("javax.net.ssl.trustStore", trustStore);
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
        System.setProperty("javax.net.ssl.trustStoreType", "JKS");
        System.setProperty("carbon.repo.write.mode", "true");
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

            String axis2Configuration = System.getProperty("carbon.home") + File.separator + "repository" +
                    File.separator + "conf" + File.separator + "axis2" + File.separator + "axis2_client.xml";
            ConfigurationContext configContext = ConfigurationContextFactory
                    .createConfigurationContextFromFileSystem(axis2Configuration);

            Registry registry = new WSRegistryServiceClient(serverURL, username, password, configContext) {

                public void setCookie(String cookie) {
                    Main.cookie = cookie;
                    super.setCookie(cookie);
                }
            };

            ResourceServiceClient resourceServiceClient = new ResourceServiceClient(cookie, serverURL, configContext);
            SwaggerImportClient swaggerImportClient = new SwaggerImportClient(serverURL, cookie);

            int currentTask = 0;
            int tasks = 10;
            String projectPath = System.getProperty("user.dir");
            startUpMessage();

            try {
                System.out.println("Uploading sample wsdls .........");
                addWsdlGar(resourceServiceClient, projectPath);
                Thread.sleep(10*1000);
                System.out.println("######## Successfully uploaded sample wsdls ########\n\n");
            } catch (Exception e) {
                System.out.println("######## Unable to upload sample wsdls ########\n\n");
            }

            try {
                System.out.println("Uploading sample wadls .........");
                addWadlGar(resourceServiceClient, projectPath);
                System.out.println("######## Successfully uploaded sample wadls ########\n\n");
            } catch (Exception e) {
                System.out.println("######## Unable to upload sample wadls ########\n\n");
            }

            try {
                System.out.println("Uploading sample schemas .........");
                addSchemaGar(resourceServiceClient, projectPath);
                System.out.println("######## Successfully uploaded sample schemas ########\n\n");
            } catch (Exception e) {
                System.out.println("######## Unable to upload sample schemas ########\n\n");
            }


            try {
                System.out.println("Uploading sample swagger docs .........");
                addSwaggerGar(resourceServiceClient, projectPath);
                addSwaggerFromURL(swaggerImportClient);
                System.out.println("######## Successfully uploaded sample swagger docs ########\n\n");
            } catch (Exception e) {
                System.out.println("######## Unable to upload sample swagger docs ########\n\n");
            }

            try {
                System.out.println("Uploading sample policies .........");
                addPolicyGar(resourceServiceClient, projectPath);
                System.out.println("######## Successfully uploaded sample policies ########\n\n");
            } catch (Exception e) {
                System.out.println("######## Unable to upload sample policies ########\n\n");
            }

        } catch (Exception e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        System.exit(0);
    }

    /**
     *This method is used to upload sample WADLs using gar file.
     *
     * @param resourceServiceClient
     * @param projectPath  absolute path of the gar file.
     * @throws Exception
     */
    private static void addWadlGar(ResourceServiceClient resourceServiceClient, String projectPath) throws Exception {
	DataHandler dh = new DataHandler(new URL("file://"+projectPath+"/resources/wadls.gar"));
        resourceServiceClient.addResource("/_system/governance/trunk/test/wadls/",
                "application/vnd.wso2.governance-archive", null, dh, null, null);
    }

    /**
     *This method is used to upload sample WSDLs using gar file.
     *
     * @param resourceServiceClient
     * @param projectPath  absolute path of the gar file.
     * @throws Exception
     */
	private static void addWsdlGar(ResourceServiceClient resourceServiceClient, String projectPath) throws Exception {       
        DataHandler dh = new DataHandler(new URL("file://"+projectPath+"/resources/wsdl_new.gar"));
        resourceServiceClient.addResource("/_system/governance/trunk/test/wsdls/",
                "application/vnd.wso2.governance-archive", null, dh, null, null);
    }

    /**
     *This method is used to upload sample schemas using gar file.
     *
     * @param resourceServiceClient
     * @param projectPath  absolute path of the gar file.
     * @throws Exception
     */
	private static void addSchemaGar(ResourceServiceClient resourceServiceClient, String projectPath) throws Exception {
	DataHandler dh = new DataHandler(new URL("file://"+projectPath+"/resources/schemas.gar"));
        resourceServiceClient.addResource("/_system/governance/trunk/test/schemas/",
                                               "application/vnd.wso2.governance-archive", null, dh, null,null);
    }

    /**
     *This method is used to upload sample swagger docs using gar file.
     *
     * @param resourceServiceClient
     * @param projectPath  absolute path of the gar file.
     * @throws Exception
     */
    private static void addSwaggerGar(ResourceServiceClient resourceServiceClient, String projectPath) throws Exception {
        DataHandler dh = new DataHandler(new URL("file://"+projectPath+"/resources/swagger.gar"));
        resourceServiceClient.addResource("/_system/governance/trunk/test/schemas/",
                "application/vnd.wso2.governance-archive", null, dh, null,null);
    }

    /**
     *This method is used to upload sample policies using gar file.
     *
     * @param resourceServiceClient
     * @param projectPath  absolute path of the gar file.
     * @throws Exception
     */
    private static void addPolicyGar(ResourceServiceClient resourceServiceClient, String projectPath) throws Exception {
        String[][] properties = { { "registry.mediaType", "application/policy+xml" }, { "version", "1.0.0" } };
        DataHandler dh = new DataHandler(new URL("file://" + projectPath + "/resources/policies.gar"));
        resourceServiceClient
                .addResource("/_system/governance/trunk/test/1.0.0/policies/",
                        "application/vnd.wso2.governance-archive", null, dh, null, properties);
    }

    /**
     *This method is used to upload sample swagger docs using URL import.
     *
     * @param SwaggerImportClient
     * @throws Exception
     */
    private static void addSwaggerFromURL(SwaggerImportClient swaggerImportClient)
            throws ResourceAdminServiceExceptionException, RemoteException {
        String resourceUrl = "https://localhost:9443/resource/1.0.0/swagger/api-docs";
        String resourceName = "api-docs.json";
        swaggerImportClient.addSwagger(resourceName, "adding From URL", resourceUrl);

    }

    /**
     *This method is used to print the starting message for the sample populator. .
     */
    private static void startUpMessage(){
        System.out.println("********************************************************************************\n");
        System.out.println("Sample Data Populating Started. This may take 2-3 minutes depend on your system\n");
        System.out.println("********************************************************************************\n\n");
    }
}

