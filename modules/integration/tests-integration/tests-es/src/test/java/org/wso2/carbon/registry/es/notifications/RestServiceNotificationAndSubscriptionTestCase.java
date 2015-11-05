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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.*;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.GenericRestClient;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertNotNull;

/**
 * This class testes subscription & notification for rest services on publisher notification
 */
public class RestServiceNotificationAndSubscriptionTestCase extends GREGIntegrationBaseTest {

    private static final Log log = LogFactory.getLog(RestServiceNotificationAndSubscriptionTestCase.class);

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
    public RestServiceNotificationAndSubscriptionTestCase(TestUserMode userMode) {
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
        setTestEnvironment();
    }

    @Test(groups = { "wso2.greg",
            "wso2.greg.es" }, description = "Adding subscription to rest service on LC state change",
            dependsOnMethods = { "addSubscriptionCheckListItem", "addSubscriptionUnCheckListItem" })
    public void addSubscriptionToLcStateChange() throws JSONException, IOException {

        JSONObject dataObject = new JSONObject();

        dataObject.put("notificationType", "PublisherLifeCycleStateChanged");
        dataObject.put("notificationMethod", "work");

        ClientResponse response = genericRestClient
                .geneticRestRequestPost(publisherUrl + "/subscriptions/restservice/" + assetId,
                        MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, dataObject.toString(), queryParamMap,
                        headerMap, cookieHeader);

        String payLoad = response.getEntity(String.class);
        payLoad = payLoad.substring(payLoad.indexOf('{'));
        JSONObject obj = new JSONObject(payLoad);
        assertNotNull(obj.get("id").toString(),
                "Response payload is not the in the correct format" + response.getEntity(String.class));

        response = genericRestClient.geneticRestRequestPost(publisherUrl + "/assets/" + assetId + "/state",
                MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_JSON,
                "nextState=Testing&comment=Completed", queryParamMap, headerMap, cookieHeader);

    }

    @Test(groups = { "wso2.greg",
            "wso2.greg.es" }, description = "Adding subscription to rest service on resource update")
    public void addSubscriptionToResourceUpdate() throws JSONException, IOException {

        JSONObject dataObject = new JSONObject();

        dataObject.put("notificationType", "PublisherResourceUpdated");
        dataObject.put("notificationMethod", "work");

        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/subscriptions/restservice/" + assetId,
                        MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, dataObject.toString(), queryParamMap,
                        headerMap, cookieHeader);

        String payLoad = response.getEntity(String.class);
        payLoad = payLoad.substring(payLoad.indexOf('{'));
        JSONObject obj = new JSONObject(payLoad);
        assertNotNull(obj.get("id").toString(),
                "Response payload is not the in the correct format" + response.getEntity(String.class));

        String dataBody = readFile(resourcePath + "json" + File.separator + "PublisherRestResourceUpdate.json");
        response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/assets/" + assetId,
                        MediaType.APPLICATION_JSON,
                        MediaType.APPLICATION_JSON, dataBody
                        , queryParamMap, headerMap, cookieHeader);
    }

    @Test(groups = { "wso2.greg",
            "wso2.greg.es" }, description = "Adding subscription to rest service on check list item checked")
    public void addSubscriptionCheckListItem() throws JSONException, IOException {

        JSONObject dataObject = new JSONObject();

        dataObject.put("notificationType", "PublisherCheckListItemChecked");
        dataObject.put("notificationMethod", "work");

        ClientResponse response = genericRestClient
                .geneticRestRequestPost(publisherUrl + "/subscriptions/restservice/" + assetId,
                        MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, dataObject.toString(), queryParamMap,
                        headerMap, cookieHeader);

        String payLoad = response.getEntity(String.class);
        payLoad = payLoad.substring(payLoad.indexOf('{'));
        JSONObject obj = new JSONObject(payLoad);
        assertNotNull(obj.get("id").toString(),
                "Response payload is not the in the correct format" + response.getEntity(String.class));

        queryParamMap.put("lifecycle", "ServiceLifeCycle");
        JSONObject checkListObject = new JSONObject();
        JSONObject checkedItems = new JSONObject();
        JSONArray checkedItemsArray = new JSONArray();
        checkedItems.put("index", 0);
        checkedItems.put("checked", true);
        checkedItemsArray.put(checkedItems);
        checkListObject.put("checklist", checkedItemsArray);

        response = genericRestClient.geneticRestRequestPost(publisherUrl + "/asset/" + assetId + "/update-checklist",
                MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, checkListObject.toString(), queryParamMap,
                headerMap, cookieHeader);
    }

    @Test(groups = { "wso2.greg",
            "wso2.greg.es" }, description = "Adding subscription to rest service on check list item unchecked",
            dependsOnMethods = { "addSubscriptionCheckListItem" })
    public void addSubscriptionUnCheckListItem() throws JSONException, IOException {

        JSONObject dataObject = new JSONObject();

        dataObject.put("notificationType", "PublisherCheckListItemUnchecked");
        dataObject.put("notificationMethod", "work");

        ClientResponse response = genericRestClient
                .geneticRestRequestPost(publisherUrl + "/subscriptions/restservice/" + assetId,
                        MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, dataObject.toString(), queryParamMap,
                        headerMap, cookieHeader);

        String payLoad = response.getEntity(String.class);
        payLoad = payLoad.substring(payLoad.indexOf('{'));
        JSONObject obj = new JSONObject(payLoad);
        assertNotNull(obj.get("id").toString(),
                "Response payload is not the in the correct format" + response.getEntity(String.class));

        JSONObject checkListObject = new JSONObject();
        JSONObject checkedItems = new JSONObject();
        JSONArray checkedItemsArray = new JSONArray();
        checkedItems.put("index", 0);
        checkedItems.put("checked", false);
        checkedItemsArray.put(checkedItems);
        checkListObject.put("checklist", checkedItemsArray);

        response = genericRestClient.geneticRestRequestPost(publisherUrl + "/asset/" + assetId + "/update-checklist",
                MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, checkListObject.toString(), queryParamMap,
                headerMap, cookieHeader);
    }

    @Test(groups = { "wso2.greg",
            "wso2.greg.es" }, description = "Adding wrong subscription method to check the error message")
    public void addWrongSubscriptionMethod() throws JSONException, IOException {

        JSONObject dataObject = new JSONObject();

        dataObject.put("notificationType", "PublisherCheckListItemUnchecked");
        dataObject.put("notificationMethod", "test");

        ClientResponse response = genericRestClient
                .geneticRestRequestPost(publisherUrl + "/subscriptions/restservice/" + assetId,
                        MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, dataObject.toString(), queryParamMap,
                        headerMap, cookieHeader);

        String payLoad = response.getEntity(String.class);
        payLoad = payLoad.substring(payLoad.indexOf('{'));
        JSONObject obj = new JSONObject(payLoad);
        assertNotNull(obj.get("error").toString(),
                "Error message is not contained in the response for notification method \"test\"" + response
                        .getEntity(String.class));
    }

    private void deleteRestServiceAsset() throws JSONException {
        genericRestClient.geneticRestRequestDelete(publisherUrl + "/assets/" + assetId, MediaType.APPLICATION_JSON,
                MediaType.APPLICATION_JSON, queryParamMap, headerMap, cookieHeader);
    }

    private void setTestEnvironment() throws JSONException, IOException {
        // Authenticate
        ClientResponse response = genericRestClient
                .geneticRestRequestPost(publisherUrl + "/authenticate/", MediaType.APPLICATION_FORM_URLENCODED,
                        MediaType.APPLICATION_JSON, "username=admin&password=admin", queryParamMap, headerMap, null);
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        jSessionId = obj.getJSONObject("data").getString("sessionId");
        cookieHeader = "JSESSIONID=" + jSessionId;

        //Create rest service
        queryParamMap.put("type", "restservice");
        String dataBody = readFile(resourcePath + "json" + File.separator + "publisherPublishRestResource.json");
        ClientResponse createResponse = genericRestClient
                .geneticRestRequestPost(publisherUrl + "/assets", MediaType.APPLICATION_JSON,
                        MediaType.APPLICATION_JSON, dataBody, queryParamMap, headerMap, cookieHeader);
        JSONObject createObj = new JSONObject(createResponse.getEntity(String.class));
        assetId = createObj.get("id").toString();
    }

    @AfterClass
    public void clean() throws Exception {
        deleteRestServiceAsset();
    }


    @DataProvider
    private static TestUserMode[][] userModeProvider() {
        return new TestUserMode[][]{
                new TestUserMode[]{TestUserMode.SUPER_TENANT_ADMIN}
                //                new TestUserMode[]{TestUserMode.TENANT_USER},
        };
    }
}
