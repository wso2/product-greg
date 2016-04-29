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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wink.client.ClientResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.*;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.registry.es.utils.GregESTestBaseTest;
import org.wso2.greg.integration.common.utils.GenericRestClient;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.xpath.XPathExpressionException;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * This class can be used to test search functionality & advance search functionality
 */

@SetEnvironment(executionEnvironments = { ExecutionEnvironment.ALL})
public class GregRestResourceStoreCategorizationSearchTestCase  extends GregESTestBaseTest {

    protected Log log = LogFactory.getLog(GregRestResourceStoreCategorizationSearchTestCase.class);

    private TestUserMode userMode;
    private String publisherUrl;
    private String storeUrl;
    private String resourcePath;
    private String assetIdOne;
    private String assetIdTwo;
    private String type = "restservice";
    private String restServiceNameOne;
    private String restServiceNameTwo;
    private String productOne;
    private String productTwo;

    private GenericRestClient genericRestClient;
    private Map<String, String> queryParamMap;
    private Map<String, String> headerMap;


    private String publisherCookieHeader;
    private String storeCookieHeader;

    @Factory(dataProvider = "userModeProvider")
    public GregRestResourceStoreCategorizationSearchTestCase(TestUserMode userMode) {
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
        assertTrue(addNewRxtConfiguration("restserviceCategorization.rxt", "restservice.rxt"),
                "Addition of new rest service rxt failed");

    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws Exception {
        deleteAsset(assetIdOne, publisherUrl, publisherCookieHeader, type, genericRestClient);
        deleteAsset(assetIdTwo, publisherUrl, publisherCookieHeader, type, genericRestClient);
        assertTrue(defaultCustomRxtConfiguration("restservicedefault.rxt", "restservice.rxt"),
                "Reverting back to default rest service rxt failed");
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Authenticate Publisher test")
    public void authenticatePublisher() throws JSONException, XPathExpressionException {

        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/authenticate/",
                        MediaType.APPLICATION_FORM_URLENCODED,
                        MediaType.APPLICATION_JSON,
                        "username=" + automationContext.getContextTenant().getContextUser().getUserName() +
                                "&password=" + automationContext.getContextTenant().getContextUser().getPassword()
                        , queryParamMap, headerMap, null
                );
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        assertTrue((response.getStatusCode() == Response.Status.OK.getStatusCode()),
                "Wrong status code ,Expected 200 OK ,Received " +
                        response.getStatusCode()
        );
        String jSessionId = obj.getJSONObject("data").getString("sessionId");
        publisherCookieHeader = "JSESSIONID=" + jSessionId;
        assertNotNull(jSessionId, "Invalid JSessionID received");
    }


    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Create Test Rest Service",
            dependsOnMethods = {"authenticatePublisher"})
    public void createTestRestServicesWithCategorization() throws JSONException, IOException {

        queryParamMap.put("type", type);
        String dataBodyOne = readFile(resourcePath + "json" + File.separator
                + "publisherPublishRestResourceWithCategorization.json");
        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/assets",
                        MediaType.APPLICATION_JSON,
                        MediaType.APPLICATION_JSON, dataBodyOne
                        , queryParamMap, headerMap, publisherCookieHeader);
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        assertTrue((response.getStatusCode() == Response.Status.CREATED.getStatusCode()),
                "Wrong status code ,Expected 201 Created ,Received " +
                        response.getStatusCode()
        );
        assetIdOne = obj.get("id").toString();
        restServiceNameOne = obj.get("name").toString();
        productOne = obj.getJSONObject("attributes").get("categorization_product").toString();

        String dataBodyTwo = readFile(resourcePath + "json" + File.separator +
                "publisherPublishNewRestResourceWithCategorization.json");

        response = genericRestClient.geneticRestRequestPost(publisherUrl + "/assets",
                        MediaType.APPLICATION_JSON,
                        MediaType.APPLICATION_JSON, dataBodyTwo
                        , queryParamMap, headerMap, publisherCookieHeader);

        JSONObject objTwo = new JSONObject(response.getEntity(String.class));
        assertTrue((response.getStatusCode() == Response.Status.CREATED.getStatusCode()),
                "Wrong status code ,Expected 201 Created ,Received " +
                        response.getStatusCode()
        );

        assetIdTwo = objTwo.get("id").toString();
        restServiceNameTwo = objTwo.get("name").toString();
        productTwo = objTwo.getJSONObject("attributes").get("categorization_product").toString();

        assertNotNull(assetIdOne, "Empty asset one resource id available" +
                response.getEntity(String.class));
        assertNotNull(assetIdTwo, "Empty asset two resource id available" +
                response.getEntity(String.class));
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Authenticate Store",
            dependsOnMethods = "createTestRestServicesWithCategorization")
    public void authenticateStore() throws JSONException, XPathExpressionException {

        ClientResponse response =
                genericRestClient.geneticRestRequestPost(storeUrl + "/authenticate/",
                        MediaType.APPLICATION_FORM_URLENCODED,
                        MediaType.APPLICATION_JSON,
                        "username=" + automationContext.getContextTenant().getContextUser().getUserName() +
                                "&password=" + automationContext.getContextTenant().getContextUser().getPassword()
                        , queryParamMap, headerMap, null
                );
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        assertTrue((response.getStatusCode() == Response.Status.OK.getStatusCode()),
                "Wrong status code ,Expected 200 OK ,Received " +
                        response.getStatusCode()
        );
        String jSessionId = obj.getJSONObject("data").getString("sessionId");
        storeCookieHeader = "JSESSIONID=" + jSessionId;
        assertNotNull(jSessionId, "Invalid JSessionID received");
    }


    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Search Added Rest Service",
            dependsOnMethods = {"authenticateStore"})
    public void searchByFilters() throws JSONException, IOException, InterruptedException {

        queryParamMap.clear();
        queryParamMap.put("paginationLimit", "20");
        queryParamMap.put("start", "0");
        queryParamMap.put("count", "20");
        // https://localhost:9443/store/apis/assets?q="categorization_product"%3A"(g-reg OR esb)"
        queryParamMap.put("q", "\"categorization_product" + "\":" + "\"(" + productOne + " OR "+productTwo+")\"");
        Thread.sleep(10000);
        ClientResponse response = genericRestClient.geneticRestRequestGet
                (storeUrl + "/assets", queryParamMap, headerMap, storeCookieHeader);

        assertTrue((response.getStatusCode() == Response.Status.OK.getStatusCode()),
                "Wrong status code ,Expected 200 OK ,But Received " + response.getStatusCode());
        assertTrue(response.getEntity(String.class).contains(restServiceNameOne),
                "Response does not contain Rest service name " + restServiceNameOne);
        assertTrue(response.getEntity(String.class).contains(restServiceNameOne),
                "Response does not contain Rest service name " + restServiceNameTwo);
    }

    @DataProvider
    private static TestUserMode[][] userModeProvider() {
        return new TestUserMode[][]{
                new TestUserMode[]{TestUserMode.SUPER_TENANT_ADMIN}
                //                new TestUserMode[]{TestUserMode.TENANT_USER},
        };
    }
}
