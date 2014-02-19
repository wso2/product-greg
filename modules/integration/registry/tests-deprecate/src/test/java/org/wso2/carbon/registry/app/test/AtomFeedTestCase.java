/*
 * Copyright 2004,2005 The Apache Software Foundation.
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

package org.wso2.carbon.registry.app.test;

import org.apache.abdera.model.AtomDate;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.util.base64.Base64Utils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.registry.app.RemoteRegistry;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

/**
 * A test case which tests registry version handling
 */

public class AtomFeedTestCase {
    public static final String USER_NAME = "admin";
    public static final String PASSWORD = "admin";
    public static final String REGISTRY_NAMESPACE = "http://wso2.org/registry";

    public static final String resourcePath = "/c1/c2/r1";
    public RemoteRegistry registry;


    @BeforeClass(groups = {"wso2.greg"})
    public void init() {
        InitializeAPI initializeAPI = new InitializeAPI();
        registry = initializeAPI.getRegistry(FrameworkSettings.CARBON_HOME, FrameworkSettings.HTTPS_PORT,
                FrameworkSettings.HTTP_PORT);

        populateData();
    }

    private void populateData(){
        try {
            Resource resource = registry.newResource();
            resource.setContent("This is a test resource".getBytes());
            resource.setDescription("This is a test description");

            registry.put(resourcePath,resource);
        } catch (RegistryException e) {
            fail("Could not populate data. Failed to add resource to the registry");
        }
    }

    @Test(groups = {"wso2.greg"})
    public void atomFeedTest() throws RegistryException {

        Resource resource = registry.get(resourcePath);
        OMElement atomFeedOMElement = getAtomFeedContent(constructAtomUrl(resourcePath));

        if(atomFeedOMElement == null){
            fail("No feed data available");

        }

//        checking whether the updated times are correct
        OMElement updatedElement = atomFeedOMElement.getFirstChildWithName(
                new QName(atomFeedOMElement.getNamespace().getNamespaceURI(),"updated"));
        if(!updatedElement.getText().equals(getAtomDateString(resource.getLastModified()))){
            fail("Last updated times are incorrect");
        }

//        Checking whether the created times are correct
        OMElement createdElement = atomFeedOMElement.getFirstChildWithName(
                new QName(REGISTRY_NAMESPACE,"createdTime"));
        if(!createdElement.getText().equals(getAtomDateString(resource.getCreatedTime()))){
            fail("Created times are incorrect");
        }

//        Checking whether description is correct
        OMElement descriptionElement = atomFeedOMElement.getFirstChildWithName(
                new QName(atomFeedOMElement.getNamespace().getNamespaceURI(),"summary"));
        if(!descriptionElement.getText().equals(resource.getDescription())){
            fail("description is invalid");
        }
    }

    private String getAtomDateString(Date date){
        AtomDate atomDate = new AtomDate(date);
        return atomDate.getValue();
    }

    private String constructAtomUrl(String path){
        return "https://" + FrameworkSettings.HOST_NAME + ":" + FrameworkSettings.HTTPS_PORT + "/registry/atom" + path;
    }

    private OMElement getAtomFeedContent(String registryUrl){
        try {
            URL url = new URL(registryUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            String userPassword = USER_NAME + ":" + PASSWORD;
            String encodedAuthorization = Base64Utils.encode(userPassword.getBytes());
            connection.setRequestProperty("Authorization", "Basic "+
                    encodedAuthorization);
            connection.connect();

            InputStream inputStream = connection.getInputStream();
            StringBuilder sb = new StringBuilder();
            String line;

            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            } finally {
                inputStream.close();
            }

            return AXIOMUtil.stringToOM(sb.toString());
        } catch (MalformedURLException e) {
            fail("Malformed URL provided");
        } catch (IOException e) {
            fail("Unable to get the content from the URL");
        } catch (XMLStreamException e) {
            fail("Unable to convert the content to OMElement");
        }
        return null;
    }

}
