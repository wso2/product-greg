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

package org.wso2.registry.jaxr.sample;

import javax.xml.registry.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.PasswordAuthentication;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class RegistryJaxrClient {

    protected static Connection connection;
    private String SCOUT_UDDI_CLIENT_CONFIG ="scout.juddi.client.config.file";

    //Set some default values
    protected String uddiversion = null;
    protected String userid = null;
    protected String passwd = null;


    public static void main(String[] args) {

        // Set ConnectionFactoryImpl class
        System.setProperty("javax.xml.registry.ConnectionFactoryClass",
                "org.apache.ws.scout.registry.ConnectionFactoryImpl");
        RegistryJaxrClient jaxrClient = new RegistryJaxrClient();
        jaxrClient.setUp();
        JaxrServiceTest jaxrServiceTest = new JaxrServiceTest();
        jaxrServiceTest.publishFindAndDeleteService(connection);
    }

    public void setUp() {

        uddiversion = "3.0";
        Properties scoutProperties = new Properties();
        String scoutv3File = System.getProperty("sample.resources") + "scoutv3.properties";
        try {
            scoutProperties.load(new FileReader(new File(scoutv3File)));
            scoutProperties.setProperty(SCOUT_UDDI_CLIENT_CONFIG ,
                    System.getProperty("sample.resources") +"META-INF" +File.separator
                            + "jaxr-uddi.xml");

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (scoutProperties.getProperty("userid") != null) {
            userid = scoutProperties.getProperty("userid");
        }
        if (scoutProperties.getProperty("password") != null) {
            passwd = scoutProperties.getProperty("password");
        }
        ConnectionFactory factory;
        try {
            factory = ConnectionFactory.newInstance();
            factory.setProperties(scoutProperties);
            connection = factory.createConnection();

            PasswordAuthentication passwdAuth = new PasswordAuthentication(userid,
                    passwd.toCharArray());
            Set<PasswordAuthentication> creds = new HashSet<PasswordAuthentication>();
            creds.add(passwdAuth);

            connection.setCredentials(creds);
            System.out.println("\n*********Connection established with UDDI Registry************");
        } catch (JAXRException e) {
            e.printStackTrace();
        }
    }
}
