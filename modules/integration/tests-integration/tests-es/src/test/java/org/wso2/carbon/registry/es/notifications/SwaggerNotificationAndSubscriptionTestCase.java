/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.registry.es.notifications;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wink.client.ClientResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.es.utils.GregESTestBaseTest;
import org.wso2.greg.integration.common.clients.LifeCycleAdminServiceClient;
import org.wso2.greg.integration.common.utils.GenericRestClient;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * This class tests subscriptions and notifications on publisher console for Swagger artifacts.
 */
@SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
public class SwaggerNotificationAndSubscriptionTestCase extends GregESTestBaseTest {

    private static final Log log = LogFactory.getLog(SwaggerNotificationAndSubscriptionTestCase.class);
    private TestUserMode userMode;
    String jSessionId;
    String assetId;
    String path;
    String cookieHeader;
    GenericRestClient genericRestClient;
    Map<String, String> queryParamMap;
    Map<String, String> headerMap;
    String publisherUrl;
    String resourcePath;
    String assetName;
    LifeCycleAdminServiceClient lifeCycleAdminServiceClient;
    String lifeCycleName;
    String stateChangeMessage = " State changed successfully to Testing!";
    Map<String, String> assocUUIDMap;

    @Factory(dataProvider = "userModeProvider")
    public SwaggerNotificationAndSubscriptionTestCase(TestUserMode userMode) {
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
        publisherUrl =publisherContext.getContextUrls()
                .getSecureServiceUrl().replace("services", "publisher/apis");
        //need lifeCycleAdminServiceClient to attach a lifecycle to the Swagger, as Swaggers does not come with
        //a default lifecycle attached
        lifeCycleAdminServiceClient = new LifeCycleAdminServiceClient(backendURL, sessionCookie);
        lifeCycleName = "ServiceLifeCycle";
        setTestEnvironment();
    }

    private void setTestEnvironment() throws XPathExpressionException, JSONException {
        ClientResponse response = authenticate(publisherUrl, genericRestClient,
                                               automationContext.getSuperTenant().getTenantAdmin().getUserName(),
                                               automationContext.getSuperTenant().getTenantAdmin().getPassword());
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        assertTrue((response.getStatusCode() == 200),
                   "Wrong status code ,Expected 200 OK ,Received " +
                   response.getStatusCode()
        );
        jSessionId = obj.getJSONObject("data").getString("sessionId");
        cookieHeader = "JSESSIONID=" + jSessionId;
        assertNotNull(jSessionId, "Invalid JSessionID received");
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "create a swagger with a LC attached.")
    public void createSwaggerAssetWithLC()
            throws JSONException, InterruptedException, IOException,
                   CustomLifecyclesChecklistAdminServiceExceptionException {
        queryParamMap.put("type", "swagger");
        String swaggerTemplate = readFile(resourcePath + "json" + File.separator + "swagger-sample.json");
        assetName = "swagger.json";
        String dataBody = String.format(swaggerTemplate, "http://petstore.swagger.io/v2/swagger.json",
                                        assetName, "1.0.0");
        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/assets",
                                                         MediaType.APPLICATION_JSON,
                                                         MediaType.APPLICATION_JSON, dataBody
                        , queryParamMap, headerMap, cookieHeader);
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        Assert.assertTrue((response.getStatusCode() == 201),
                          "Wrong status code ,Expected 201 Created ,Received " +
                          response.getStatusCode());
        String resultName = obj.get("overview_name").toString();
        Assert.assertEquals(resultName, assetName);
        searchSwaggerAsset();
        //attach a LC to the swagger
        lifeCycleAdminServiceClient.addAspect(path, lifeCycleName);
        Assert.assertNotNull(assetId, "Empty asset resource id available" +
                                      response.getEntity(String.class));
        Assert.assertTrue(this.getAsset(assetId, "swagger").get("lifecycle")
                                  .equals(lifeCycleName), "LifeCycle not assigned to given asset");
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Adding subscription to a swagger LC check list item check",
          dependsOnMethods = {"createSwaggerAssetWithLC"})
    public void addSubscriptionForLCCheckListItemCheck() throws JSONException, IOException {

        JSONObject dataObject = new JSONObject();

        dataObject.put("notificationType", "PublisherCheckListItemChecked");
        dataObject.put("notificationMethod", "work");

        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/subscriptions/swagger/" + assetId, MediaType.APPLICATION_JSON,
                                                         MediaType.APPLICATION_JSON, dataObject.toString()
                        , queryParamMap, headerMap, cookieHeader);

        assertTrue((response.getStatusCode() == Response.Status.OK.getStatusCode()),
                   "Wrong status code ,Expected" + Response.Status.OK.getStatusCode() + "Created ,Received " +
                   response.getStatusCode());
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Adding subscription to a swagger LC check list item uncheck",
          dependsOnMethods = {"addSubscriptionForLCCheckListItemCheck"})
    public void addSubscriptionForLCCheckListItemUnCheck() throws JSONException, IOException {

        JSONObject dataObject = new JSONObject();

        dataObject.put("notificationType", "PublisherCheckListItemUnchecked");
        dataObject.put("notificationMethod", "work");

        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/subscriptions/swagger/" + assetId, MediaType.APPLICATION_JSON,
                                                         MediaType.APPLICATION_JSON, dataObject.toString()
                        , queryParamMap, headerMap, cookieHeader);

        assertTrue((response.getStatusCode() == Response.Status.OK.getStatusCode()),
                   "Wrong status code ,Expected" + Response.Status.OK.getStatusCode() + "Created ,Received " +
                   response.getStatusCode());
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Adding subscription to a swagger LC state change",
          dependsOnMethods = {"addSubscriptionForLCCheckListItemUnCheck"})
    public void addSubscriptionForLCStateChange() throws JSONException, IOException {

        JSONObject dataObject = new JSONObject();

        dataObject.put("notificationType", "PublisherLifeCycleStateChanged");
        dataObject.put("notificationMethod", "work");

        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/subscriptions/swagger/" + assetId, MediaType.APPLICATION_JSON,
                                                         MediaType.APPLICATION_JSON, dataObject.toString()
                        , queryParamMap, headerMap, cookieHeader);

        assertTrue((response.getStatusCode() == Response.Status.OK.getStatusCode()),
                   "Wrong status code ,Expected" + Response.Status.OK.getStatusCode() + "Created ,Received " +
                   response.getStatusCode());
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Swagger LC check list item check",
          dependsOnMethods = {"addSubscriptionForLCCheckListItemCheck"})
    public void checkLCCheckItemsOnSwagger() throws JSONException, IOException {
        queryParamMap.put("type", "swagger");
        queryParamMap.put("lifecycle", lifeCycleName);
        JSONObject LCStateobj = getLifeCycleState(assetId, "swagger");
        JSONObject dataObj = LCStateobj.getJSONObject("data");
        JSONArray checkItems = dataObj.getJSONArray("checkItems");
        Assert.assertEquals(((JSONObject) checkItems.get(0)).getString("isVisible"), "true");
        ClientResponse responseCheck0 =
                checkLifeCycleCheckItem(cookieHeader, 0);
        Assert.assertTrue(responseCheck0.getStatusCode() == 200);
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Swagger LC check list item check",
          dependsOnMethods = {"addSubscriptionForLCCheckListItemUnCheck", "checkLCCheckItemsOnSwagger"})
    public void uncheckLCCheckItemsOnSwagger() throws JSONException, IOException {
        queryParamMap.put("type", "swagger");
        queryParamMap.put("lifecycle", lifeCycleName);
        JSONObject LCStateobj = getLifeCycleState(assetId, "swagger");
        JSONObject dataObj = LCStateobj.getJSONObject("data");
        JSONArray checkItems = dataObj.getJSONArray("checkItems");
        Assert.assertEquals(((JSONObject) checkItems.get(0)).getString("isVisible"), "true");
        ClientResponse responseUncheck0 =
                uncheckLifeCycleCheckItem(cookieHeader, 0);
        Assert.assertTrue(responseUncheck0.getStatusCode() == 200);
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Change LC state on Swagger",
          dependsOnMethods = {"addSubscriptionForLCStateChange", "uncheckLCCheckItemsOnSwagger"})
    public void changeLCStateSwagger() throws JSONException, IOException {
        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/assets/" + assetId + "/state",
                                                         MediaType.APPLICATION_FORM_URLENCODED,
                                                         MediaType.APPLICATION_JSON,
                                                         "nextState=Testing&comment=Completed"
                        , queryParamMap, headerMap, cookieHeader);
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        String status = obj.get("status").toString();
        Assert.assertEquals(status, stateChangeMessage);
    }

    /**
     * This method get all the swaggers in publisher and select the one created by createSwaggerAssetWithLC method.
     *
     * @throws JSONException
     */
    public void searchSwaggerAsset() throws JSONException {
        Map<String, String> queryParamMap = new HashMap<>();
        queryParamMap.put("type", "swagger");
        ClientResponse clientResponse = searchAssetByQuery(publisherUrl,genericRestClient,cookieHeader,queryParamMap);
        JSONObject obj = new JSONObject(clientResponse.getEntity(String.class));
        JSONArray jsonArray = obj.getJSONArray("list");
        for (int i = 0; i < jsonArray.length(); i++) {
            String name = (String) jsonArray.getJSONObject(i).get("name");
            if (assetName.equals(name)) {
                assetId = (String) jsonArray.getJSONObject(i).get("id");
                path = (String) jsonArray.getJSONObject(i).get("path");
                break;
            }
        }
    }

    private JSONObject getAsset(String assetId, String assetType) throws JSONException {
        Map<String, String> assetTypeParamMap = new HashMap<String, String>();
        assetTypeParamMap.put("type", assetType);
        ClientResponse clientResponse = getAssetById(publisherUrl,genericRestClient,cookieHeader,assetId, queryParamMap);
        return new JSONObject(clientResponse.getEntity(String.class));
    }

    private ClientResponse checkLifeCycleCheckItem(String managerCookieHeader, int itemId) {
        return genericRestClient.geneticRestRequestPost(publisherUrl + "/asset/" + assetId + "/update-checklist",
                                                        MediaType.APPLICATION_JSON,
                                                        MediaType.APPLICATION_JSON,
                                                        "{\"checklist\":[{\"index\":" + itemId + ",\"checked\":true}]}"
                , queryParamMap, headerMap, managerCookieHeader);
    }

    private JSONObject getLifeCycleState(String assetId, String assetType) throws JSONException {
        Map<String, String> assetTypeParamMap = new HashMap<String, String>();
        assetTypeParamMap.put("type", assetType);
        assetTypeParamMap.put("lifecycle", lifeCycleName);
        ClientResponse response =
                genericRestClient.geneticRestRequestGet
                        (publisherUrl + "/asset/" + assetId + "/state"
                                , queryParamMap, headerMap, cookieHeader);
        return new JSONObject(response.getEntity(String.class));
    }

    private ClientResponse uncheckLifeCycleCheckItem(String managerCookieHeader, int itemId) {
        return genericRestClient.geneticRestRequestPost(publisherUrl + "/asset/" + assetId + "/update-checklist",
                                                        MediaType.APPLICATION_JSON,
                                                        MediaType.APPLICATION_JSON,
                                                        "{\"checklist\":[{\"index\":" + itemId + ",\"checked\":false}]}"
                , queryParamMap, headerMap, managerCookieHeader);
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws RegistryException, JSONException {
        Map<String, String> queryParamMap = new HashMap<>();
        queryParamMap.put("type", "swagger");
        assocUUIDMap = getAssociationsFromPages(publisherUrl,genericRestClient,cookieHeader,assetId, queryParamMap);
        deleteAssetById(publisherUrl,genericRestClient,cookieHeader,assetId, queryParamMap);
        deleteAllAssociationsById(publisherUrl,genericRestClient,cookieHeader,assetId, queryParamMap);
        queryParamMap.clear();
        for (String uuid : assocUUIDMap.keySet()) {
            queryParamMap.put("type", getType(assocUUIDMap.get(uuid)));
            deleteAssetById(publisherUrl,genericRestClient,cookieHeader,uuid, queryParamMap);
        }
    }

    @DataProvider
    private static TestUserMode[][] userModeProvider() {
        return new TestUserMode[][]{
                new TestUserMode[]{TestUserMode.SUPER_TENANT_ADMIN}
//                new TestUserMode[]{TestUserMode.TENANT_USER},
        };
    }
}
