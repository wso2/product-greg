/*
*Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.es.publisher.search;

import org.apache.wink.client.ClientResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.*;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.es.utils.GregESTestBaseTest;
import org.wso2.greg.integration.common.utils.GenericRestClient;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.Response;
import javax.xml.xpath.XPathExpressionException;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * This class can be used to test search functionality & advance search functionality with an ampersand
 */
public class GregRestResourceSearchAndAdvancedSearchWithAmpersandTestCase extends GregESTestBaseTest {
    private TestUserMode userMode;
    private String publisherUrl;
    private String resourcePath;
    private String assetId;
    private String type = "restservice";
    private String restServiceName;
    private String version;
    private String lcState;
    private GenericRestClient genericRestClient;
    private Map<String, String> queryParamMap;

    String cookieHeader;

    @Factory(dataProvider = "userModeProvider")
    public GregRestResourceSearchAndAdvancedSearchWithAmpersandTestCase(TestUserMode userMode) {
        this.userMode = userMode;
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(userMode);
        genericRestClient = new GenericRestClient();
        queryParamMap = new HashMap<>();
        resourcePath =
                FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator + "GREG" + File.separator;
        publisherUrl = automationContext.getContextUrls().getSecureServiceUrl().replace("services", "publisher/apis");
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws RegistryException {
        deleteAsset(assetId, publisherUrl, cookieHeader, type, genericRestClient);
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Authenticate Publisher test")
    public void authenticatePublisher() throws JSONException, XPathExpressionException {

        ClientResponse response = authenticate(publisherUrl, genericRestClient,
                automationContext.getContextTenant().getContextUser().getUserName(),
                automationContext.getContextTenant().getContextUser().getPassword());
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        assertTrue((response.getStatusCode() == Response.Status.OK.getStatusCode()),
                "Wrong status code ,Expected 200 OK ,Received " +
                        response.getStatusCode()
        );
        String jSessionId = obj.getJSONObject("data").getString("sessionId");
        cookieHeader = "JSESSIONID=" + jSessionId;
        assertNotNull(jSessionId, "Invalid JSessionID received");
    }


    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Create Test Rest Service with ampersand",
            dependsOnMethods = {"authenticatePublisher"})
    public void createTestRestServicesWithAmpersand() throws JSONException, IOException {

        ClientResponse response = createAsset(
                resourcePath + "json" + File.separator + "publisherPublishRestResourceWithAmpersand.json",
                publisherUrl, cookieHeader, type, genericRestClient);

        JSONObject obj = new JSONObject(response.getEntity(String.class));
        assertTrue((response.getStatusCode() == Response.Status.CREATED.getStatusCode()),
                "Wrong status code ,Expected 201 Created ,Received " +
                        response.getStatusCode()
        );
        assetId = obj.get("id").toString();
        restServiceName = obj.get("name").toString();
        version = obj.get("version").toString();
        lcState = obj.get("lifecycleState").toString();

        assertNotNull(assetId, "Empty asset resource id available" +
                response.getEntity(String.class));
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Search Added Rest Service with ampersand",
            dependsOnMethods = {"createTestRestServicesWithAmpersand"})
    public void searchAddedRestServiceWithAmpersand() throws JSONException, IOException, InterruptedException {
        queryParamMap.clear();

        queryParamMap.put("q", "\"name" + "\":" + "\"" + restServiceName + "\"");

        ClientResponse response = searchAssetByQuery(publisherUrl, genericRestClient, cookieHeader, queryParamMap);

        assertTrue(response.getEntity(String.class).contains(restServiceName),
                "Response does not contain Rest service name " + restServiceName);

    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Advance search with ampersand",
            dependsOnMethods = {"searchAddedRestServiceWithAmpersand"})
    public void advancedSearchAddedRestServiceWithAmpersand()
            throws JSONException, IOException, XPathExpressionException {
        queryParamMap.clear();

        queryParamMap.put("q", "\"name" + "\":" + "\"" + restServiceName + "\"" + "," +
                "\"provider" + "\":" + "\"" + automationContext.getContextTenant().getContextUser().getUserName()
                + "\"" + "," +
                "\"version" + "\":" + "\"" + version + "\"" + "," +
                "\"lcState" + "\":" + "\"" + lcState + "\"");

        ClientResponse response = searchAssetByQuery(publisherUrl, genericRestClient, cookieHeader, queryParamMap);

        assertTrue(response.getEntity(String.class).contains(restServiceName),
                "Response does not contain Rest service name " + restServiceName);
        assertTrue(response.getEntity(String.class).contains(version),
                "Response does not contain correct version " + version);
        assertTrue(response.getEntity(String.class).contains(lcState),
                "Response does not contain correct LC State " + lcState);

    }

    @DataProvider
    private static TestUserMode[][] userModeProvider() {
        return new TestUserMode[][]{
                new TestUserMode[]{TestUserMode.SUPER_TENANT_ADMIN}
                //                new TestUserMode[]{TestUserMode.TENANT_USER},
        };
    }
}
