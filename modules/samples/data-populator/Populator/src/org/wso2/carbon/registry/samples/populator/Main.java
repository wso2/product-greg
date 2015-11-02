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

import javax.activation.DataHandler;
import java.net.URL;
import java.io.File;


public class Main {
    private static String cookie;
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
                    Main.cookie = cookie;
                    super.setCookie(cookie);
                }
            };

            ResourceServiceClient resourceServiceClient = new ResourceServiceClient(cookie, serverURL, configContext);

            int currentTask = 0;
            int tasks = 10;
            String projectPath = System.getProperty("user.dir");

            try {
                addWsdlGar(resourceServiceClient, projectPath);
                Thread.sleep(1 * 60 * 1000);
                System.out.println("######## Successfully uploaded sample wsdls ########");
            } catch (Exception e) {
                System.out.println("######## Unable to upload sample wsdls ########");
            }

            try {
                addWadlGar(resourceServiceClient, projectPath);
                Thread.sleep(30 * 1000);
                System.out.println("######## Successfully uploaded sample wadls ########");
            } catch (Exception e) {
                System.out.println("######## Unable to upload sample wsdls ########");
            }

            try {
                addSchemaGar(resourceServiceClient, projectPath);
                Thread.sleep(30 * 1000);
                System.out.println("######## Successfully uploaded sample schemas ########");
            } catch (Exception e) {
                System.out.println("######## Unable to upload sample schemas ########");
            }


            try {
                addSwaggerGar(resourceServiceClient, projectPath);
                Thread.sleep(30 * 1000);
                System.out.println("######## Successfully uploaded sample swagger docs ########");
            } catch (Exception e) {
                System.out.println("######## Unable to upload sample swagger docs ########");
            }

            try {
                addPolicyGar(resourceServiceClient, projectPath);
                Thread.sleep(30 * 1000);
                System.out.println("######## Successfully uploaded sample policies ########");
            } catch (Exception e) {
                System.out.println("######## Unable to upload sample policies ########");
            }

        } catch (Exception e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        System.exit(0);
    }

    private static void addWadlGar(ResourceServiceClient resourceServiceClient, String projectPath) throws Exception {
	DataHandler dh = new DataHandler(new URL("file://"+projectPath+"/resources/wadls.gar"));
        resourceServiceClient.addResource("/_system/governance/trunk/test/wadls/",
                "application/vnd.wso2.governance-archive", null, dh, null, null);
    }

	private static void addWsdlGar(ResourceServiceClient resourceServiceClient, String projectPath) throws Exception {       
        DataHandler dh = new DataHandler(new URL("file://"+projectPath+"/resources/wsdl_new.gar"));
        resourceServiceClient.addResource("/_system/governance/trunk/test/wsdls/",
                                               "application/vnd.wso2.governance-archive", null, dh, null,null);
    }

	private static void addSchemaGar(ResourceServiceClient resourceServiceClient, String projectPath) throws Exception {
	DataHandler dh = new DataHandler(new URL("file://"+projectPath+"/resources/schemas.gar"));
        resourceServiceClient.addResource("/_system/governance/trunk/test/schemas/",
                                               "application/vnd.wso2.governance-archive", null, dh, null,null);
    }

    private static void addSwaggerGar(ResourceServiceClient resourceServiceClient, String projectPath) throws Exception {
        DataHandler dh = new DataHandler(new URL("file://"+projectPath+"/resources/swagger.gar"));
        resourceServiceClient.addResource("/_system/governance/trunk/test/schemas/",
                "application/vnd.wso2.governance-archive", null, dh, null,null);
    }

    private static void addPolicyGar(ResourceServiceClient resourceServiceClient, String projectPath) throws Exception {
        String[][] properties = { { "registry.mediaType", "application/policy+xml" }, { "version", "1.0.0" } };
        DataHandler dh = new DataHandler(new URL("file://" + projectPath + "/resources/policies.gar"));
        resourceServiceClient
                .addResource("/_system/governance/trunk/test/1.0.0/policies/", "application/vnd.wso2.governance-archive",
                        null, dh, null, properties);
    }
}

