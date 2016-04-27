/*
*Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.registry.es.store.search;

import org.apache.wink.client.ClientResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.es.utils.GregESTestBaseTest;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GenericRestClient;

import javax.activation.DataHandler;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

@SetEnvironment(executionEnvironments = { ExecutionEnvironment.ALL})
public class GregConcatSearchTestCase extends GregESTestBaseTest {

    private TestUserMode userMode;
    private String publisherUrl;
    private String storeUrl;
    private String resourcePath;
    private ResourceAdminServiceClient resourceAdminServiceClient;

    private GenericRestClient genericRestClient;
    private Map<String, String> queryParamMap;
    private Map<String, String> headerMap;

    private String publisherCookieHeader;
    private String storeCookieHeader;

    @Factory(dataProvider = "userModeProvider")
    public GregConcatSearchTestCase(TestUserMode userMode) {
        this.userMode = userMode;
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(userMode);
        genericRestClient = new GenericRestClient();
        queryParamMap = new HashMap<>();
        headerMap = new HashMap<>();
        resourcePath =
                FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator + "GREG" + File.separator;
        publisherUrl = publisherContext.getContextUrls().getSecureServiceUrl().replace("services", "publisher/apis");
        storeUrl = storeContext.getContextUrls().getSecureServiceUrl().replace("services", "store/apis");
        resourceAdminServiceClient = new ResourceAdminServiceClient(automationContext.getContextUrls().getBackEndUrl(),
                sessionCookie);

        deteleExistingData();

    }

    private void deteleExistingData() {
        deleteResource("/_system/governance/trunk/soapservices");
        deleteResource("/_system/governance/trunk/wsdls");
        deleteResource("/_system/governance/trunk/endpoints");
    }

    @AfterClass(alwaysRun = true)
    private void cleanUp() throws RegistryException {
        deteleExistingData();
    }

    private void deleteResource(String path){
        try {
            resourceAdminServiceClient.deleteResource(path);
        }catch (RemoteException e) {
            log.error("Failed to Remove Resource :" + e);
        } catch (ResourceAdminServiceExceptionException e) {
            log.error("Failed to Remove Resource :" + e);
        }
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Authenticate Publisher test")
    public void authenticatePublisher() throws JSONException, XPathExpressionException {

        ClientResponse response = genericRestClient
                .geneticRestRequestPost(publisherUrl + "/authenticate/", MediaType.APPLICATION_FORM_URLENCODED,
                        MediaType.APPLICATION_JSON,
                        "username=" + automationContext.getContextTenant().getContextUser().getUserName() +
                                "&password=" + automationContext.getContextTenant().getContextUser().getPassword(),
                        queryParamMap, headerMap, null);
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        assertTrue((response.getStatusCode() == Response.Status.OK.getStatusCode()),
                "Wrong status code ,Expected 200 OK ,Received " + response.getStatusCode());
        String jSessionId = obj.getJSONObject("data").getString("sessionId");
        publisherCookieHeader = "JSESSIONID=" + jSessionId;
        assertNotNull(jSessionId, "Invalid JSessionID received");
    }

    /* add a schema Gar from file system */
    @Test(groups = "wso2.greg", description = "Add Gars from file system",
            dependsOnMethods = {"authenticatePublisher"})
    public void testAddZipFromFileSystem()
            throws IOException, RegistryException, ResourceAdminServiceExceptionException,
            LoginAuthenticationExceptionException, InterruptedException {
        String wsdlResourceName = "wsdls.zip";

        String wsdlZipPath =
                FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator + "GREG" + File.separator
                        + "gar" + File.separator + wsdlResourceName;

        resourceAdminServiceClient
                .addResource(wsdlZipPath, "application/vnd.wso2.governance-archive", "adding wsdl zip file",
                        new DataHandler(new URL("file:///" + wsdlZipPath)));
        Thread.sleep(20000);
    }

    @Test(groups = { "wso2.greg", "wso2.greg.es" }, description = "Search Added SOAP Service in publisher",
            dependsOnMethods = { "testAddZipFromFileSystem" })
    public void searchNameWithVersionInPublisher()
            throws JSONException, IOException, InterruptedException {

        queryParamMap.clear();

        // https://10.100.5.173:9443/publisher/pages/search-results?q="name":"info","version":"1.1.1"
        queryParamMap.put("q", "\"name\":\"info\",\"version\":\"1.0.0\"");

        Thread.sleep(10000);

        ClientResponse response = genericRestClient
                .geneticRestRequestGet(publisherUrl.split("/apis")[0] + "/pages/search-results", queryParamMap,
                        headerMap, publisherCookieHeader);

        log.info("Publisher search result for search by name: "+ response.getEntity(String.class));

        assertTrue((response.getStatusCode() == Response.Status.OK.getStatusCode()),
                "Wrong status code ,Expected 200 OK ,But Received " + response.getStatusCode());

        String str = response.getEntity(String.class).toLowerCase();
        int lastIndex = 0;
        int count = 0;

        while (lastIndex != -1) {

            lastIndex = str.indexOf("<h3 class=\"ast-name\">info", lastIndex);

            if (lastIndex != -1) {
                count++;
                lastIndex += "<h3 class=\"ast-name\">info".length();
            }
        }

        // If this test fails please increase the time(10000) first
        assertEquals(count, 2, "There should be 2 occurrences of <h3 class=\"ast-name\">info ");
    }

    @Test(groups = { "wso2.greg", "wso2.greg.es" }, description = "Search Added SOAP Service in publisher",
            dependsOnMethods = { "searchNameWithVersionInPublisher" })
    public void searchQuotedNameWithVersionInPublisher()
            throws JSONException, IOException, InterruptedException {

        queryParamMap.clear();

        // https://10.100.5.173:9443/publisher/pages/search-results?q="name":"\"info\"","version":"1.1.1"
        queryParamMap.put("q", "\"name\":\"\\\"info\\\"\",\"version\":\"1.0.0\"");

        Thread.sleep(5000);

        ClientResponse response = genericRestClient
                .geneticRestRequestGet(publisherUrl.split("/apis")[0] + "/pages/search-results", queryParamMap,
                        headerMap, publisherCookieHeader);

        log.info("Publisher search result for search by name: "+ response.getEntity(String.class));

        assertTrue((response.getStatusCode() == Response.Status.OK.getStatusCode()),
                "Wrong status code ,Expected 200 OK ,But Received " + response.getStatusCode());

        String str = response.getEntity(String.class);
        int lastIndex = 0;
        int count = 0;

        while (lastIndex != -1) {

            lastIndex = str.indexOf("<h3 class=\"ast-name\">Info</h3>", lastIndex);

            if (lastIndex != -1) {
                count++;
                lastIndex += "<h3 class=\"ast-name\">Info</h3>".length();
            }
        }

        // If this test fails please increase the time(10000) first
        assertEquals(count, 1, "There should be 1 occurrence of <h3 class=\"ast-name\">Info</h3> ");
    }

    @Test(groups = { "wso2.greg", "wso2.greg.es" }, description = "Search Added SOAP Service in publisher",
            dependsOnMethods = { "searchQuotedNameWithVersionInPublisher" })
    public void searchQuotedNameWithSpacesInPublisher()
            throws JSONException, IOException, InterruptedException {

        queryParamMap.clear();

        // https://10.100.5.173:9443/publisher/pages/search-results?q="name":"\"KK Web Service Eng.wsdl\""
        queryParamMap.put("q", "\"name\":\"\\\"KK Web Service Eng.wsdl\\\"\"");

        Thread.sleep(5000);

        ClientResponse response = genericRestClient
                .geneticRestRequestGet(publisherUrl.split("/apis")[0] + "/pages/search-results", queryParamMap,
                        headerMap, publisherCookieHeader);

        log.info("Publisher search result for search by name: "+ response.getEntity(String.class));

        assertTrue((response.getStatusCode() == Response.Status.OK.getStatusCode()),
                "Wrong status code ,Expected 200 OK ,But Received " + response.getStatusCode());

        String str = response.getEntity(String.class);
        int lastIndex = 0;
        int count = 0;

        while (lastIndex != -1) {

            lastIndex = str.indexOf("<h3 class=\"ast-name\">KK Web Service Eng.wsdl</h3>", lastIndex);

            if (lastIndex != -1) {
                count++;
                lastIndex += "<h3 class=\"ast-name\">KK Web Service Eng.wsdl</h3>".length();
            }
        }

        // If this test fails please increase the time(10000) first
        assertEquals(count, 1, "There should be 1 occurrence of <h3 class=\"ast-name\">KK Web Service Eng.wsdl</h3> ");
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Authenticate Store",
            dependsOnMethods = "searchQuotedNameWithSpacesInPublisher")
    public void authenticateStore() throws JSONException, XPathExpressionException {

        ClientResponse response = genericRestClient
                .geneticRestRequestPost(storeUrl + "/authenticate/", MediaType.APPLICATION_FORM_URLENCODED,
                        MediaType.APPLICATION_JSON,
                        "username=" + automationContext.getContextTenant().getContextUser().getUserName() +
                                "&password=" + automationContext.getContextTenant().getContextUser().getPassword(),
                        queryParamMap, headerMap, null);
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        assertTrue((response.getStatusCode() == Response.Status.OK.getStatusCode()),
                "Wrong status code ,Expected 200 OK ,Received " + response.getStatusCode());
        String jSessionId = obj.getJSONObject("data").getString("sessionId");
        storeCookieHeader = "JSESSIONID=" + jSessionId;
        assertNotNull(jSessionId, "Invalid JSessionID received");
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Search Added SOAP Service in store",
            dependsOnMethods = {"authenticateStore"})
    public void searchNameWithVersionInStore() throws JSONException, IOException, InterruptedException {

        String valueToBeFoundInHTML = "title=\"info";
        queryParamMap.clear();

        // https://localhost:9443/store/pages/top-assets?q="name":"\"info\"","version":"1.1.1"
        queryParamMap.put("q", "\"name\":\"info\",\"version\":\"1.0.0\"");

        Thread.sleep(5000);

        ClientResponse response = genericRestClient
                .geneticRestRequestGet(storeUrl.split("/apis")[0] + "/pages/top-assets", queryParamMap, headerMap,
                        storeCookieHeader);

        log.info("Store search result for search by name: "+ response.getEntity(String.class));

        assertTrue((response.getStatusCode() == Response.Status.OK.getStatusCode()),
                "Wrong status code ,Expected 200 OK ,But Received " + response.getStatusCode());

        String str = response.getEntity(String.class).toLowerCase();
        int lastIndex = 0;
        int count = 0;

        while (lastIndex != -1) {

            lastIndex = str.indexOf(valueToBeFoundInHTML, lastIndex);

            if (lastIndex != -1) {
                count++;
                lastIndex += valueToBeFoundInHTML.length();
            }
        }

        // If this test fails please increase the time(20000) first
        assertEquals(count, 2*2, "There should be 2x2 occurrences of " + valueToBeFoundInHTML);
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Search Added SOAP Service in store",
            dependsOnMethods = {"searchNameWithVersionInStore"})
    public void searchQuotedNameWithVersionInStore() throws JSONException, IOException, InterruptedException {

        String valueToBeFoundInHTML = "title=\"Info\">Info</a>";
        queryParamMap.clear();

        // https://localhost:9443/store/pages/top-assets?q="name":"\"info\"","version":"1.1.1"
        queryParamMap.put("q", "\"name\":\"\\\"info\\\"\",\"version\":\"1.0.0\"");

        Thread.sleep(5000);

        ClientResponse response = genericRestClient
                .geneticRestRequestGet(storeUrl.split("/apis")[0] + "/pages/top-assets", queryParamMap, headerMap,
                        storeCookieHeader);

        log.info("Store search result for search by name: "+ response.getEntity(String.class));

        assertTrue((response.getStatusCode() == Response.Status.OK.getStatusCode()),
                "Wrong status code ,Expected 200 OK ,But Received " + response.getStatusCode());

        String str = response.getEntity(String.class);
        int lastIndex = 0;
        int count = 0;

        while (lastIndex != -1) {

            lastIndex = str.indexOf(valueToBeFoundInHTML, lastIndex);

            if (lastIndex != -1) {
                count++;
                lastIndex += valueToBeFoundInHTML.length();
            }
        }

        // If this test fails please increase the time(20000) first
        assertEquals(count, 1, "There should be 1 occurrence of " + valueToBeFoundInHTML);
    }



    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Search Added SOAP Service in store",
            dependsOnMethods = {"searchQuotedNameWithVersionInStore"})
    public void searchQuotedNameWithSpacesInStore() throws JSONException, IOException, InterruptedException {

        String valueToBeFoundInHTML = "title=\"KK Web Service Eng.wsdl\">KK Web Service Eng.wsdl</a>";
        queryParamMap.clear();

        // https://10.100.5.173:9443/store/pages/top-assets?q="name":"\"KK Web Service Eng.wsdl\""
        queryParamMap.put("q", "\"name\":\"\\\"KK Web Service Eng.wsdl\\\"\"");

        Thread.sleep(5000);

        ClientResponse response = genericRestClient
                .geneticRestRequestGet(storeUrl.split("/apis")[0] + "/pages/top-assets", queryParamMap, headerMap,
                        storeCookieHeader);

        log.info("Store search result for search by name: "+ response.getEntity(String.class));

        assertTrue((response.getStatusCode() == Response.Status.OK.getStatusCode()),
                "Wrong status code ,Expected 200 OK ,But Received " + response.getStatusCode());

        String str = response.getEntity(String.class);
        int lastIndex = 0;
        int count = 0;

        while (lastIndex != -1) {

            lastIndex = str.indexOf(valueToBeFoundInHTML, lastIndex);

            if (lastIndex != -1) {
                count++;
                lastIndex += valueToBeFoundInHTML.length();
            }
        }

        // If this test fails please increase the time(20000) first
        assertEquals(count, 1, "There should be 1 occurrence of " + valueToBeFoundInHTML);
    }

    @DataProvider
    private static TestUserMode[][] userModeProvider() {
        return new TestUserMode[][] { new TestUserMode[] { TestUserMode.SUPER_TENANT_ADMIN }
                //                new TestUserMode[]{TestUserMode.TENANT_USER},
        };
    }

}