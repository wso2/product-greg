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

package org.wso2.carbon.registry.ws.client.test.general.old;

import org.apache.axis2.AxisFault;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.testng.Assert.assertEquals;

/**
 * A test case which tests registry content stream operation
 */
public class TestContentStreamWSTestCase {

    private WSRegistryServiceClient registry;
    private String frameworkPath;

    @BeforeClass(groups = {"wso2.greg"})
    public void initTest() throws RegistryException, AxisFault {
        frameworkPath = CarbonUtils.getCarbonHome();
        int userId = 0;
        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        registry = registryProviderUtil.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);
    }

    @Test(groups = {"wso2.greg"})
    public void putResourceasStreamXML() throws Exception {

        final String description = "testPutXMLResourceAsBytes";
        final String mediaType = "application/xml";

        // Establish where we are putting the resource in registry
        final String registryPath = "/wso2registry/conf/pom.xml";

        InputStream is = new BufferedInputStream(new FileInputStream(getTestResourcePath() +
                                                                     "pom.xml"));
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

    private String getTestResourcePath() {
        return frameworkPath + File.separator + ".." + File.separator + ".." + File.separator +
               ".." + File.separator;
    }

    @Test(groups = {"wso2.greg"})
    public void contentStreaming() throws Exception {


        Resource r3 = registry.newResource();
        String path = "/content/stream/content.txt";
        r3.setContent("this is the content".getBytes());
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
    public void setContainStreamXML() throws Exception {

        final String description = "testPutXMLResourceAsBytes";
        final String mediaType = "application/xml";

        // Establish where we are putting the resource in registry
        final String registryPath = "/wso2registry/contentstream/conf/pom.xml";

        InputStream is = new BufferedInputStream(new FileInputStream(getTestResourcePath() +
                                                                     "pom.xml"));

        Resource resource = registry.newResource();

        resource.setContentStream(is);
        resource.setDescription(description);
        resource.setMediaType(mediaType);
        registry.put(registryPath, resource);


        Resource r2 = registry.get(registryPath);

        assertEquals(new String((byte[]) r2.getContent()),
                     new String((byte[]) resource.getContent()), "File content is not matching");

    }

//    The below methods are used in the above tests. No need to add them to the test suit.


    private String convertStreamToString(InputStream is) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
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

    private static String slurp(InputStream in) throws IOException {
        StringBuffer out = new StringBuffer();
        byte[] b = new byte[4096];
        for (int n; (n = in.read(b)) != -1;) {
            out.append(new String(b, 0, n));
        }
        return out.toString();
    }

    @AfterClass
    public void cleanup() throws RegistryException {
        registry.delete("/wso2registry");
        registry.delete("content");

    }
}
