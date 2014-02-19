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

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.integration.core.TestTemplate;
import org.wso2.carbon.registry.app.RemoteRegistry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.io.*;

import static org.testng.Assert.*;

/**
 * A test case which tests registry content streaming
 */
public class TestContentStreamTestCase {
    public RemoteRegistry registry;

    @BeforeClass(groups = {"wso2.greg"})
    public void init() {
        InitializeAPI initializeAPI = new InitializeAPI();
        registry = initializeAPI.getRegistry(FrameworkSettings.CARBON_HOME, FrameworkSettings.HTTPS_PORT, FrameworkSettings.HTTP_PORT);
    }

    @Test(groups = {"wso2.greg"})
    public void putResourceasStreamXML() throws RegistryException, FileNotFoundException {

        final String description = "testPutXMLResourceAsBytes";
        final String mediaType = "application/xml";

        // Establish where we are putting the resource in registry
        final String registryPath = "/wso2registry/conf/registry.xml";

        InputStream is = new BufferedInputStream(new FileInputStream(FrameworkSettings.CARBON_HOME + File.separator + "repository" + File.separator + "conf" + File.separator + "registry.xml"));
        String st = null;
        try {
            st = slurp(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Resource resource = registry.newResource();

        resource.setContent(st.getBytes());
        resource.setDescription(description);
        resource.setMediaType(mediaType);
        registry.put(registryPath, resource);


        Resource r2 = registry.get(registryPath);

        assertEquals(new String((byte[]) r2.getContent()),
                new String((byte[]) resource.getContent()), "File content is not matching");

    }

    @Test(groups = {"wso2.greg"})
    public void ContentStreaming() throws RegistryException, IOException {


        Resource r3 = registry.newResource();
        String path = "/content/stream/content.txt";
        r3.setContent(new String("this is the content").getBytes());
        r3.setDescription("this is test desc");
        r3.setMediaType("plain/text");
        r3.setProperty("test2", "value2");
        r3.setProperty("test1", "value1");
        registry.put(path, r3);

        Resource r4 = registry.get("/content/stream/content.txt");

        assertEquals(new String((byte[]) r4.getContent()),
                new String((byte[]) r3.getContent()), "Content is not equal.");

        InputStream isTest = r4.getContentStream();

        assertEquals(convertStreamToString(isTest),
                new String((byte[]) r3.getContent()), "Content stream is not equal.");

        r3.discard();
    }


    @Test(groups = {"wso2.greg"})
    public void setContainStreamXML() throws RegistryException, FileNotFoundException {

        final String description = "testPutXMLResourceAsBytes";
        final String mediaType = "application/xml";

        // Establish where we are putting the resource in registry
        final String registryPath = "/wso2registry/contentstream/conf/registry.xml";

        InputStream is = new BufferedInputStream(new FileInputStream(FrameworkSettings.CARBON_HOME + File.separator + "repository" + File.separator + "conf" + File.separator + "registry.xml"));

        Resource resource = registry.newResource();

        resource.setContentStream(is);
        resource.setDescription(description);
        resource.setMediaType(mediaType);
        registry.put(registryPath, resource);


        Resource r2 = registry.get(registryPath);

        assertEquals(new String((byte[]) r2.getContent()),
                new String((byte[]) resource.getContent()), "File content is not matching");

    }

    public String convertStreamToString(InputStream is) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return sb.toString();
    }

    public static String slurp(InputStream in) throws IOException {
        StringBuffer out = new StringBuffer();
        byte[] b = new byte[4096];
        for (int n; (n = in.read(b)) != -1; ) {
            out.append(new String(b, 0, n));
        }
        return out.toString();
    }
}
