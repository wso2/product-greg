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

import org.apache.wink.client.ClientResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
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
import javax.xml.xpath.XPathExpressionException;

import static org.testng.Assert.assertNotNull;

/**
 * This class testes subscription & notification for soap services at store.
 */
@SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
public class SoapServiceStoreNotificationTestCase extends GregESTestBaseTest {

    private TestUserMode userMode;
    private String assetId;
    private String cookieHeaderPublisher;
    private String cookieHeaderStore;
    private GenericRestClient genericRestClient;
    private Map<String, String> queryParamMap;
    private Map<String, String> headerMap;
    private String publisherUrl;
    private String storeUrl;
    private String resourcePath;

    @Factory(dataProvider = "userModeProvider")
    public SoapServiceStoreNotificationTestCase(TestUserMode userMode) {
        this.userMode = userMode;
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(userMode);
        genericRestClient = new GenericRestClient();
        queryParamMap = new HashMap<>();
        headerMap = new HashMap<>();
        StringBuilder builder = new StringBuilder();
        builder.append(FrameworkPathUtil.getSystemResourceLocation()).append("artifacts").append(File.separator)
                .append("GREG").append(File.separator);
        resourcePath = builder.toString();
        publisherUrl = publisherContext.getContextUrls()
                .getSecureServiceUrl().replace("services", "publisher/apis");
        storeUrl = storeContext.getContextUrls()
                .getSecureServiceUrl().replace("services", "store/apis");
        setTestEnvironment();
    }

    /**
     * This test case add subscription to lifecycle state change and verifies the reception of store notification
     * by changing the life cycle state.
     */
    @Test(groups = { "wso2.greg",
            "wso2.greg.es" }, description = "Adding subscription to soap service on LC state change")
    public void addSubscriptionToLcStateChange() throws JSONException, IOException {
        JSONObject dataObject = new JSONObject();
        dataObject.put("notificationType", "StoreLifeCycleStateChanged");
        dataObject.put("notificationMethod", "work");

        ClientResponse response = genericRestClient
                .geneticRestRequestPost(storeUrl + "/subscription/soapservice/" + assetId, MediaType.APPLICATION_JSON,
                        MediaType.APPLICATION_JSON, dataObject.toString(), queryParamMap, headerMap, cookieHeaderStore);

        String payload = response.getEntity(String.class);
        payload = payload.substring(payload.indexOf('{'));
        JSONObject payloadObject = new JSONObject(payload);
        assertNotNull(payloadObject.get("id"),
                "Response payload is not the in the correct format" + response.getEntity(String.class));

        genericRestClient.geneticRestRequestPost(publisherUrl + "/assets/" + assetId + "/state",
                MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_JSON,
                "nextState=Testing&comment=Completed", queryParamMap, headerMap, cookieHeaderPublisher);
        // TODO - Since notification not appearing in the store
    }

    /**
     * This test case add subscription to resource update and verifies the reception of store notification
     * by updating the resource.
     */
    @Test(groups = { "wso2.greg",
            "wso2.greg.es" }, description = "Adding subscription to soap service on resource update")
    public void addSubscriptionToResourceUpdate() throws JSONException, IOException {
        JSONObject dataObject = new JSONObject();
        dataObject.put("notificationType", "StoreResourceUpdated");
        dataObject.put("notificationMethod", "work");

        ClientResponse response = genericRestClient
                .geneticRestRequestPost(storeUrl + "/subscription/soapservice/" + assetId, MediaType.APPLICATION_JSON,
                        MediaType.APPLICATION_JSON, dataObject.toString(), queryParamMap, headerMap, cookieHeaderStore);

        String payload = response.getEntity(String.class);
        payload = payload.substring(payload.indexOf('{'));
        JSONObject payloadObject = new JSONObject(payload);
        assertNotNull(payloadObject.get("id"),
                "Response payload is not the in the correct format" + response.getEntity(String.class));

        String dataBody = readFile(resourcePath + "json" + File.separator + "PublisherSoapResourceUpdateFile.json");
        genericRestClient.geneticRestRequestPost(publisherUrl + "/assets/" + assetId, MediaType.APPLICATION_JSON,
                MediaType.APPLICATION_JSON, dataBody, queryParamMap, headerMap, cookieHeaderPublisher);
        // TODO - Since notification not appearing in the store
    }

    /**
     * This test case tries to add a wrong notification method and verifies the reception of error message.
     */
    @Test(groups = { "wso2.greg",
            "wso2.greg.es" }, description = "Adding wrong subscription method to check the error message")
    public void addWrongSubscriptionMethod() throws JSONException, IOException {
        JSONObject dataObject = new JSONObject();
        dataObject.put("notificationType", "StoreResourceUpdated");
        dataObject.put("notificationMethod", "test");

        ClientResponse response = genericRestClient
                .geneticRestRequestPost(publisherUrl + "/subscriptions/soapservice/" + assetId,
                        MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, dataObject.toString(), queryParamMap,
                        headerMap, cookieHeaderPublisher);

        String payload = response.getEntity(String.class);
        payload = payload.substring(payload.indexOf('{'));
        JSONObject payloadObject = new JSONObject(payload);
        assertNotNull(payloadObject.get("error"),
                "Error message is not contained in the response for wrong notification method \"test\"" + response
                        .getEntity(String.class)
        );
    }

    /**
     * Method used to authenticate publisher,store and create a soap service asset. Created asset
     * is used to add subscriptions and to receive notification.
     */
    private void setTestEnvironment() throws JSONException, IOException, XPathExpressionException {
        // Authenticate Publisher
        ClientResponse response = authenticate(publisherUrl, genericRestClient,
                automationContext.getSuperTenant().getTenantAdmin().getUserName(),
                automationContext.getSuperTenant().getTenantAdmin().getPassword());
        JSONObject responseObject = new JSONObject(response.getEntity(String.class));
        String jSessionIdPublisher = responseObject.getJSONObject("data").getString("sessionId");
        cookieHeaderPublisher = "JSESSIONID=" + jSessionIdPublisher;

        // Authenticate Store
        ClientResponse responseStore = authenticate(storeUrl, genericRestClient,
                automationContext.getSuperTenant().getTenantAdmin().getUserName(),
                automationContext.getSuperTenant().getTenantAdmin().getPassword());
        responseObject = new JSONObject(responseStore.getEntity(String.class));
        String jSessionIdStore = responseObject.getJSONObject("data").getString("sessionId");
        cookieHeaderStore = "JSESSIONID=" + jSessionIdStore;

        //Create soap service
        queryParamMap.put("type", "soapservice");
        String dataBody = readFile(resourcePath + "json" + File.separator + "publisherPublishSoapResource.json");
        ClientResponse createResponse = genericRestClient
                .geneticRestRequestPost(publisherUrl + "/assets", MediaType.APPLICATION_JSON,
                        MediaType.APPLICATION_JSON, dataBody, queryParamMap, headerMap, cookieHeaderPublisher);
        JSONObject createObj = new JSONObject(createResponse.getEntity(String.class));
        assetId = (String) createObj.get("id");
    }

    @AfterClass(alwaysRun = true)
    public void clean() throws Exception {
        deleteAssetById(publisherUrl, genericRestClient, cookieHeaderPublisher, assetId, queryParamMap);
    }

    @DataProvider
    private static TestUserMode[][] userModeProvider() {
        return new TestUserMode[][]{
                new TestUserMode[]{TestUserMode.SUPER_TENANT_ADMIN}
        };
    }
}
