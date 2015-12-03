/*
* Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.es.notifications;

import org.apache.wink.client.ClientResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.*;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.registry.es.utils.GregESTestBaseTest;
import org.wso2.greg.integration.common.utils.GenericRestClient;

import javax.ws.rs.core.MediaType;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertTrue;

/**
 * This class tests permission for subscriptions by trying to add and get subscriptions from a user
 * without permission.
 */
public class PublisherSubscriptionPermissionTestCase extends GregESTestBaseTest {

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
    public PublisherSubscriptionPermissionTestCase(TestUserMode userMode) {
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

    /**
     * Method check the permission denial for a user without permission to get all
     * subscriptions associated with a resource.
     */
    @Test(groups = { "wso2.greg", "wso2.greg.es" }, description = "Get all subscriptions associated with a "
            + "rest service from a user without permission")
    public void getAllSubscriptionsWithoutPermission()
            throws JSONException, IOException {

        ClientResponse response = genericRestClient
                .geneticRestRequestGet(publisherUrl + "/subscriptions/restservice/" + assetId, queryParamMap, headerMap,
                        cookieHeader);

        assertTrue((response.getStatusCode() == 401),
                "Wrong status code ,Expected 401 Permission Denied , Received " + response.getStatusCode());
    }

    /**
     * Method to check the permission denial for a user without permission trying to subscribe for publisher
     * life cycle state change.
     */
    @Test(groups = { "wso2.greg", "wso2.greg.es" }, description = "Adding subscription to rest service on"
            + " LC state change from a user without permission")
    public void addSubscriptionToLcStateChangeWithoutPermission()
            throws JSONException, IOException {

        JSONObject dataObject = new JSONObject();
        dataObject.put("notificationType", "PublisherLifeCycleStateChanged");
        dataObject.put("notificationMethod", "work");

        ClientResponse response = genericRestClient
                .geneticRestRequestPost(publisherUrl + "/subscriptions/restservice/" + assetId,
                        MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, dataObject.toString(), queryParamMap,
                        headerMap, cookieHeader);

        assertTrue((response.getStatusCode() == 401),
                "Wrong status code ,Expected 401 Permission Denied , Received " + response.getStatusCode());
    }

    /**
     * Method to check the permission denial for a user without permission trying to subscribe for publisher
     * resource update.
     */
    @Test(groups = { "wso2.greg", "wso2.greg.es" },
            description = "Adding subscription to rest service on resource update from a user without permission")
    public void addSubscriptionToResourceUpdateWithoutPermission() throws JSONException, IOException {

        JSONObject dataObject = new JSONObject();
        dataObject.put("notificationType", "PublisherResourceUpdated");
        dataObject.put("notificationMethod", "work");

        ClientResponse response = genericRestClient
                .geneticRestRequestPost(publisherUrl + "/subscriptions/restservice/" + assetId,
                        MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, dataObject.toString(), queryParamMap,
                        headerMap, cookieHeader);
        assertTrue((response.getStatusCode() == 401),
                "Wrong status code ,Expected 401 Permission Denied , Received " + response.getStatusCode());
    }

    /**
     * Method to check the permission denial for a user without permission trying to subscribe for publisher
     * check list item select.
     */
    @Test(groups = { "wso2.greg", "wso2.greg.es" },
            description = "Adding subscription to rest service on check list item checked from a "
                    + "user without permission")
    public void addSubscriptionCheckListItemWithoutPermission() throws JSONException, IOException {

        JSONObject dataObject = new JSONObject();
        dataObject.put("notificationType", "PublisherCheckListItemChecked");
        dataObject.put("notificationMethod", "work");

        ClientResponse response = genericRestClient
                .geneticRestRequestPost(publisherUrl + "/subscriptions/restservice/" + assetId,
                        MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, dataObject.toString(), queryParamMap,
                        headerMap, cookieHeader);
        assertTrue((response.getStatusCode() == 401),
                "Wrong status code ,Expected 401 Permission Denied , Received " + response.getStatusCode());
    }

    /**
     * Method to check the permission denial for a user without permission trying to subscribe for publisher
     * check list item deselect.
     */
    @Test(groups = { "wso2.greg", "wso2.greg.es" },
            description = "Adding subscription to rest service on check list item unchecked from a "
                    + "user without permission")
    public void addSubscriptionUnCheckListItemWithoutPermission() throws JSONException, IOException {

        JSONObject dataObject = new JSONObject();
        dataObject.put("notificationType", "PublisherCheckListItemUnchecked");
        dataObject.put("notificationMethod", "work");

        ClientResponse response = genericRestClient
                .geneticRestRequestPost(publisherUrl + "/subscriptions/restservice/" + assetId,
                        MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, dataObject.toString(), queryParamMap,
                        headerMap, cookieHeader);
        assertTrue((response.getStatusCode() == 401),
                "Wrong status code ,Expected 401 Permission Denied , Received " + response.getStatusCode());
    }

    private void deleteRestServiceAsset() throws JSONException {
        genericRestClient.geneticRestRequestDelete(publisherUrl + "/assets/" + assetId, MediaType.APPLICATION_JSON,
                MediaType.APPLICATION_JSON, queryParamMap, headerMap, cookieHeader);
    }

    /**
     * Method used to authenticate publisher and create a rest service asset. Created asset
     * is used by a user without permission to try add subscriptions .
     */
    private void setTestEnvironment() throws JSONException, IOException, XPathExpressionException {
        // Authenticate Publisher
        ClientResponse response = authenticate(publisherUrl, genericRestClient,
                automationContext.getSuperTenant().getTenantUser("subscribeUser").getUserName(),
                automationContext.getSuperTenant().getTenantUser("subscribeUser").getPassword());
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        jSessionId = obj.getJSONObject("data").getString("sessionId");
        cookieHeader = "JSESSIONID=" + jSessionId;

        //Create custom asset
        queryParamMap.put("type", "restservice");
        String dataBody = readFile(resourcePath + "json" + File.separator + "publisherPublishRestResource.json");
        ClientResponse createResponse = genericRestClient
                .geneticRestRequestPost(publisherUrl + "/assets", MediaType.APPLICATION_JSON,
                        MediaType.APPLICATION_JSON, dataBody, queryParamMap, headerMap, cookieHeader);
        JSONObject createObj = new JSONObject(createResponse.getEntity(String.class));
        assetId = (String) createObj.get("id");
    }

    @AfterClass
    public void clean() throws Exception {
        ClientResponse response = authenticate(publisherUrl, genericRestClient,
                automationContext.getSuperTenant().getTenantAdmin().getUserName(),
                automationContext.getSuperTenant().getTenantAdmin().getPassword());
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        jSessionId = obj.getJSONObject("data").getString("sessionId");
        cookieHeader = "JSESSIONID=" + jSessionId;
        deleteAssetById(publisherUrl, genericRestClient, cookieHeader, assetId, queryParamMap);
    }

    @DataProvider
    private static TestUserMode[][] userModeProvider() {
        return new TestUserMode[][]{
                new TestUserMode[]{TestUserMode.SUPER_TENANT_ADMIN}
                //                new TestUserMode[]{TestUserMode.TENANT_USER},
        };
    }

}
