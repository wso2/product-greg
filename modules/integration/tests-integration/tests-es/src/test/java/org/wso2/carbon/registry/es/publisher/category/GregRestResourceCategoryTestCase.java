/*
*Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.es.publisher.category;

import org.apache.wink.client.ClientResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.ParseException;
import org.testng.annotations.*;
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
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * This class can be used to test category related functionality
 */
@SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
public class GregRestResourceCategoryTestCase extends GregESTestBaseTest {

    private TestUserMode userMode;
    private String publisherUrl;
    private String resourcePath;
    private String restServiceOneAssetId;
    private String restServiceTwoAssetId;
    private String type = "restservice";
    private String restServiceName;
    private GenericRestClient genericRestClient;
    private Map<String, String> queryParamMap;
    private Map<String, String> headerMap;
    private String cookieHeader;

    @Factory(dataProvider = "userModeProvider")
    public GregRestResourceCategoryTestCase(TestUserMode userMode) {
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
        assertTrue(addNewRxtConfiguration("restserviceCategory.rxt", "restservice.rxt"),
                "Addition of new rest service rxt failed");

    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws Exception {
        deleteAsset(restServiceOneAssetId, publisherUrl, cookieHeader, type, genericRestClient);
        deleteAsset(restServiceTwoAssetId, publisherUrl, cookieHeader, type, genericRestClient);
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
        cookieHeader = "JSESSIONID=" + jSessionId;
        assertNotNull(jSessionId, "Invalid JSessionID received");
    }


    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Create Test Rest Service",
            dependsOnMethods = {"authenticatePublisher"})
    public void createTestRestServices() throws JSONException, IOException {
        queryParamMap.put("type", type);
        String dataBody = readFile(resourcePath + "json" + File.separator + "publisherPublishRestResource.json");
        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/assets",
                        MediaType.APPLICATION_JSON,
                        MediaType.APPLICATION_JSON, dataBody
                        , queryParamMap, headerMap, cookieHeader);
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        assertTrue((response.getStatusCode() == Response.Status.CREATED.getStatusCode()),
                "Wrong status code ,Expected 201 Created ,Received " + response.getStatusCode());
        restServiceOneAssetId = obj.get("id").toString();
        restServiceName = obj.get("name").toString();

        assertNotNull(restServiceOneAssetId, "Empty asset resource id available" +
                response.getEntity(String.class));
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Adding Category To Existing Rest Service",
            dependsOnMethods = {"createTestRestServices"})
    public void addCategoryToExistingRestService() throws JSONException, IOException, InterruptedException, ParseException {

        queryParamMap.clear();

        queryParamMap.put("type", type);

        String dataBody = readFile(resourcePath + "json" + File.separator +
                "publisherPublishRestResourceWithCategory.json");

        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/assets/" + restServiceOneAssetId, MediaType.APPLICATION_JSON,
                        MediaType.APPLICATION_JSON, dataBody, queryParamMap, headerMap, cookieHeader);

        JSONObject obj = new JSONObject(response.getEntity(String.class));

        assertTrue((response.getStatusCode() == Response.Status.ACCEPTED.getStatusCode()),
                "Wrong status code ,Expected 201 Created ,Received " + response.getStatusCode());

        assertTrue(obj.get("attributes").toString().contains("overview_name" + "\":" + "\"" + restServiceName + "\""),
                "Rest service name mismatched");

        assertTrue(obj.get("attributes").toString().contains("overview_category" + "\":" + "\"" + "Category5" + "\""),
                "Rest service updating to category 5 was unsuccessful");

        queryParamMap.put("paginationLimit", "20");
        queryParamMap.put("start", "0");
        queryParamMap.put("count", "20");
        queryParamMap.put("q", "\"category" + "\":" + "\"" + "Category5" + "\"");

        // Verify whether rest service lists incorrectly for irrelevant category
        ClientResponse searchedIrrelevantCategoryResponse = genericRestClient.geneticRestRequestGet
                (publisherUrl.split("/apis")[0] + "/apis/assets", queryParamMap, headerMap, cookieHeader);

        JSONObject searchJsonObj = new JSONObject(searchedIrrelevantCategoryResponse.getEntity(String.class));
        assertTrue(searchJsonObj.get("count").equals(0.0), "Search for category 2 unsuccessful due to count should " +
                "equal to zero");

        queryParamMap.put("q", "\"category" + "\":" + "\"" + "Category5" + "\"");

        refreshPublisherLandingPage(publisherUrl, genericRestClient, sessionCookie);

        // Verify whether rest service lists correctly for correct category
        ClientResponse searchedRelevantCategoryResponse = genericRestClient.geneticRestRequestGet
                (publisherUrl.split("/apis")[0] + "/apis/assets", queryParamMap, headerMap, cookieHeader);

        JSONObject newSearchJsonObj = new JSONObject(searchedRelevantCategoryResponse.getEntity(String.class));
        assertTrue(newSearchJsonObj.get("count").equals(1.0), "count should equal to 1");
        assertTrue(newSearchJsonObj.get("list").toString().contains("testservice1234"),
                "Search for category 5 Rest service unsuccessful since unable to find testservice1234");

    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Adding Category To New Rest Service",
            dependsOnMethods = {"addCategoryToExistingRestService"})
    public void addCategoryToNewRestService() throws JSONException, IOException, InterruptedException {

        queryParamMap.clear();
        queryParamMap.put("type", type);

        String dataBody = readFile(resourcePath + "json" +
                File.separator + "publisherPublishNewRestResourceWithCategory.json");

        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/assets",
                        MediaType.APPLICATION_JSON,
                        MediaType.APPLICATION_JSON, dataBody
                        , queryParamMap, headerMap, cookieHeader);

        JSONObject obj = new JSONObject(response.getEntity(String.class));
        assertTrue((response.getStatusCode() == Response.Status.CREATED.getStatusCode()),
                "Wrong status code ,Expected 201 Created ,Received " + response.getStatusCode());
        restServiceTwoAssetId = obj.get("id").toString();
        restServiceName = obj.get("name").toString();

        assertNotNull(restServiceTwoAssetId, "Empty asset resource id available" +
                response.getEntity(String.class));

        queryParamMap.put("paginationLimit", "20");
        queryParamMap.put("start", "0");
        queryParamMap.put("count", "20");
        queryParamMap.put("q", "\"category" + "\":" + "\"" + "Category2" + "\"");

        refreshPublisherLandingPage(publisherUrl, genericRestClient, sessionCookie);

        ClientResponse searchedNewlyAddedRestServiceResponse = genericRestClient.geneticRestRequestGet
                (publisherUrl.split("/apis")[0] + "/apis/assets", queryParamMap, headerMap, cookieHeader);

        JSONObject searchJsonObj = new JSONObject(searchedNewlyAddedRestServiceResponse.getEntity(String.class));
        assertTrue(searchJsonObj.get("count").equals(1.0), "count should equal to 1");
        assertTrue(searchJsonObj.get("list").toString().contains("MyTestRestService"),
                "Search for category 2 Rest service was unsuccessful since unable to find MyTestRestService");

    }


    @DataProvider
    private static TestUserMode[][] userModeProvider() {
        return new TestUserMode[][]{
                new TestUserMode[]{TestUserMode.SUPER_TENANT_ADMIN}
//                new TestUserMode[]{TestUserMode.TENANT_USER},
        };
    }

}
