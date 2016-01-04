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

package org.wso2.carbon.registry.es.notifications;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wink.client.ClientResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.*;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.GenericRestClient;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * This class testes subscription & notification
 */
public class GregRestResourceSubscriptionAndNotificationTestCase extends GREGIntegrationBaseTest {

    private static final Log log = LogFactory.getLog(GregRestResourceSubscriptionAndNotificationTestCase.class);

    private TestUserMode userMode;
    String jSessionId;
    String assetId;
    String cookieHeader;
    GenericRestClient genericRestClient;
    Map<String, String> queryParamMap;
    Map<String, String> headerMap;
    String publisherUrl;
    String resourcePath;

    @Factory(dataProvider = "userModeProvider")
    public GregRestResourceSubscriptionAndNotificationTestCase(TestUserMode userMode) {
        this.userMode = userMode;
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(userMode);
        genericRestClient = new GenericRestClient();
        queryParamMap = new HashMap<String, String>();
        headerMap = new HashMap<String, String>();
        resourcePath = FrameworkPathUtil.getSystemResourceLocation()
                + "artifacts" + File.separator + "GREG" + File.separator;
        publisherUrl = automationContext.getContextUrls()
                .getSecureServiceUrl().replace("services", "publisher/apis");
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws RegistryException {
        queryParamMap.put("type", "restservice");
        genericRestClient.geneticRestRequestDelete(publisherUrl+"/assets/" + assetId,
                                                   MediaType.APPLICATION_JSON,
                                                   MediaType.APPLICATION_JSON
                , queryParamMap, headerMap, cookieHeader);
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Authenticate Publisher test")
    public void authenticatePublisher() throws JSONException {
        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/authenticate/",
                        MediaType.APPLICATION_FORM_URLENCODED,
                        MediaType.APPLICATION_JSON,
                        "username=admin&password=admin"
                        , queryParamMap, headerMap, null);
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        assertTrue((response.getStatusCode() == 200),
                "Wrong status code ,Expected 200 OK ,Received " +
                        response.getStatusCode()
        );
        jSessionId = obj.getJSONObject("data").getString("sessionId");
        cookieHeader = "JSESSIONID=" + jSessionId;
        assertNotNull(jSessionId, "Invalid JSessionID received");
    }


    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Create Test Rest Service",
            dependsOnMethods = {"authenticatePublisher"})
    public void createRestServiceAsset() throws JSONException, IOException {
        queryParamMap.put("type", "restservice");
        String dataBody = readFile(resourcePath + "json" + File.separator + "publisherPublishRestResource.json");
        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/assets",
                        MediaType.APPLICATION_JSON,
                        MediaType.APPLICATION_JSON, dataBody
                        , queryParamMap, headerMap, cookieHeader);
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        assertTrue((response.getStatusCode() == Response.Status.CREATED.getStatusCode()),
                "Wrong status code ,Expected 201 Created ,Received " +
                        response.getStatusCode()
        );
        assetId = obj.get("id").toString();
        assertNotNull(assetId, "Empty asset resource id available" +
                response.getEntity(String.class));
        getAllAvailableRestServiceAssets().getEntity(String.class);
        getRestServiceAssetById(assetId).getEntity(String.class);
    }


    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Adding subscription to rest service",
            dependsOnMethods = {"createRestServiceAsset"})
    public void addSubscription() throws JSONException, IOException {

        JSONObject dataObject = new JSONObject();

        dataObject.put("notificationType","PublisherResourceUpdated");
        dataObject.put("notificationMethod","work");

        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/subscriptions/restservice/" + assetId, MediaType.APPLICATION_JSON,
                        MediaType.APPLICATION_JSON, dataObject.toString()
                        , queryParamMap, headerMap, cookieHeader);

        assertTrue((response.getStatusCode() == Response.Status.OK.getStatusCode()),
                "Wrong status code ,Expected" + Response.Status.OK.getStatusCode() + "Created ,Received " +
                        response.getStatusCode());
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Update Test Rest Service",
            dependsOnMethods = {"addSubscription"})
    public void updateRestServiceAsset() throws JSONException, IOException, InterruptedException {
        queryParamMap.put("type", "restservice");
        String dataBody = readFile(resourcePath + "json" + File.separator + "PublisherRestResourceUpdate.json");
        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/assets/" + assetId,
                        MediaType.APPLICATION_JSON,
                        MediaType.APPLICATION_JSON, dataBody
                        , queryParamMap, headerMap, cookieHeader);
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        assertTrue((response.getStatusCode() == Response.Status.ACCEPTED.getStatusCode()),
                "Wrong status code ,Expected 202 Created ,Received " +
                        response.getStatusCode()
        );
        assertTrue(obj.getJSONObject("attributes").get("overview_context")
                .equals("/changed/Context"));

        Thread.sleep(5000);

        ClientResponse responseUpdateRestService =
                genericRestClient.geneticRestRequestGet(publisherUrl +
                        "/notification/", queryParamMap, headerMap, cookieHeader);

        //TODO - since the expected notification appearing issue on publisher side

    }

    private ClientResponse getAllAvailableRestServiceAssets() {
        queryParamMap.put("type", "restservice");
        return genericRestClient.geneticRestRequestGet
                (publisherUrl + "/assets/"
                        + assetId, queryParamMap, headerMap, cookieHeader);
    }

    private ClientResponse getRestServiceAssetById(String id) {
        queryParamMap.put("type", "restservice");
        return genericRestClient.geneticRestRequestGet
                (publisherUrl + "/assets/" + id
                        + assetId, queryParamMap, headerMap, cookieHeader);
    }


    @DataProvider
    private static TestUserMode[][] userModeProvider() {
        return new TestUserMode[][]{
                new TestUserMode[]{TestUserMode.SUPER_TENANT_ADMIN}
//                new TestUserMode[]{TestUserMode.TENANT_USER},
        };
    }
}
