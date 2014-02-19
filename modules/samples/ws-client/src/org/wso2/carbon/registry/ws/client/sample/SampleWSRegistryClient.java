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
package org.wso2.carbon.registry.ws.client.sample;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.registry.core.Comment;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import java.io.File;

public class SampleWSRegistryClient {

    private static ConfigurationContext configContext = null;
	
    private static final String CARBON_HOME = ".." + File.separator + ".." + File.separator;
    private static final String axis2Repo = CARBON_HOME + File.separator +"repository" +
            File.separator + "deployment" + File.separator + "client";
    private static final String axis2Conf = ServerConfiguration.getInstance().getFirstProperty("Axis2Config.clientAxis2XmlLocation");
    private static final String username = "admin";
    private static final String password = "admin";
    private static final String serverURL = "https://localhost:9443/services/";

    private static WSRegistryServiceClient initialize() throws Exception {

        System.setProperty("javax.net.ssl.trustStore", CARBON_HOME + File.separator + "repository" +
                File.separator + "resources" + File.separator + "security" + File.separator +
                "wso2carbon.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
        System.setProperty("javax.net.ssl.trustStoreType", "JKS");
        System.setProperty("carbon.repo.write.mode", "true");
        configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(
                axis2Repo, axis2Conf);
        return new WSRegistryServiceClient(serverURL, username, password, configContext);
    }
    
	public static void main(String[] args) throws Exception {
        Registry registry = initialize();
        try {
            Resource resource = registry.newResource();
            resource.setContent("Hello Out there!");

            String resourcePath = "/abc";
            registry.put(resourcePath, resource);

            System.out.println("A resource added to: " + resourcePath);

            registry.rateResource(resourcePath, 3);

            System.out.println("Resource rated with 3 stars!");
            Comment comment = new Comment();
            comment.setText("Wow! A comment out there");
            registry.addComment(resourcePath, comment);
            System.out.println("Comment added to resource");

            Resource getResource = registry.get("/abc");
            System.out.println("Resource retrived");
            System.out.println("Printing retrieved resource content: " +
                    new String((byte[])getResource.getContent()));

        } finally {
            //Close the session
            ((WSRegistryServiceClient)registry).logut();
        }
		System.exit(0);
		
	}
}
