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
import org.wso2.carbon.automation.engine.configurations.UrlGenerationUtil;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.es.utils.EmailUtil;
import org.wso2.carbon.registry.es.utils.GregESTestBaseTest;
import org.wso2.greg.integration.common.utils.GenericRestClient;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.mail.MessagingException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.xpath.XPathExpressionException;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * This test class can be used to check the email notification functionality for rest services at Store side.
 */
public class RestServiceStoreEmailNotificationTestCase extends GregESTestBaseTest {

    private TestUserMode userMode;
    private String publisherUrl;
    private String storeUrl;
    private String resourcePath;
    private String assetId;
    private String cookieHeaderPublisher;
    private String cookieHeaderStore;
    private GenericRestClient genericRestClient;
    private Map<String, String> queryParamMap;
    private Map<String, String> headerMap;
    private String loginURL;
    private boolean isNotificationMailAvailable;

    @Factory(dataProvider = "userModeProvider")
    public RestServiceStoreEmailNotificationTestCase(TestUserMode userMode) {
        this.userMode = userMode;
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(userMode);
        loginURL = UrlGenerationUtil.getLoginURL(automationContext.getInstance());
        genericRestClient = new GenericRestClient();
        queryParamMap = new HashMap<>();
        headerMap = new HashMap<>();
        StringBuilder builder = new StringBuilder();
        builder.append(FrameworkPathUtil.getSystemResourceLocation()).append("artifacts").append(File.separator)
                .append("GREG").append(File.separator);
        resourcePath = builder.toString();
        publisherUrl = automationContext.getContextUrls().getSecureServiceUrl().replace("services", "publisher/apis");
        storeUrl = automationContext.getContextUrls().getSecureServiceUrl().replace("services", "store/apis");

        EmailUtil.updateProfileAndEnableEmailConfiguration(automationContext, backendURL, sessionCookie);
        setTestEnvironment();
    }

    /**
     * This test case add subscription to lifecycle state change and verifies the reception of email notification
     * by changing the life cycle state.
     */
    @Test(groups = { "wso2.greg",
            "wso2.greg.es" }, description = "Adding subscription to rest service on LC state change")
    public void addSubscriptionToLcStateChange() throws Exception {
        JSONObject dataObject = new JSONObject();
        dataObject.put("notificationType", "StoreLifeCycleStateChanged");
        dataObject.put("notificationMethod", "email");

        ClientResponse response = genericRestClient
                .geneticRestRequestPost(storeUrl + "/subscription/restservice/" + assetId, MediaType.APPLICATION_JSON,
                        MediaType.APPLICATION_JSON, dataObject.toString(), queryParamMap, headerMap, cookieHeaderStore);

        String payload = response.getEntity(String.class);
        payload = payload.substring(payload.indexOf('{'));
        JSONObject payloadObject = new JSONObject(payload);
        assertNotNull(payloadObject.get("id"),
                "Response payload is not the in the correct format" + response.getEntity(String.class));

        // verify e-mail
        String pointBrowserURL = EmailUtil.readGmailInboxForVerification();
        assertTrue(pointBrowserURL.contains("https"), "Verification mail has failed to reach Gmail inbox");
        EmailUtil.browserRedirectionOnVerification(pointBrowserURL, loginURL,
                automationContext.getContextTenant().getContextUser().getUserName(),
                automationContext.getContextTenant().getContextUser().getPassword());

        // Change the life cycle state in order to retrieve e-mail

        genericRestClient.geneticRestRequestPost(publisherUrl + "/assets/" + assetId + "/state",
                MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_JSON,
                "nextState=Testing&comment=Completed", queryParamMap, headerMap, cookieHeaderPublisher);
        isNotificationMailAvailable = EmailUtil.readGmailInboxForNotification("StoreLifeCycleStateChanged");
        assertTrue(isNotificationMailAvailable,
                "Store lifecycle state changed notification mail has failed to reach Gmail inbox");
        isNotificationMailAvailable = false;
    }

    /**
     * This test case add subscription to resource update and verifies the reception of email notification
     * by updating the resource.
     */
    @Test(groups = { "wso2.greg",
            "wso2.greg.es" }, description = "Adding subscription to rest service on resource update")
    public void addSubscriptionOnResourceUpdate() throws Exception {
        JSONObject dataObject = new JSONObject();
        dataObject.put("notificationType", "StoreResourceUpdated");
        dataObject.put("notificationMethod", "email");

        ClientResponse response = genericRestClient
                .geneticRestRequestPost(storeUrl + "/subscription/restservice/" + assetId, MediaType.APPLICATION_JSON,
                        MediaType.APPLICATION_JSON, dataObject.toString(), queryParamMap, headerMap, cookieHeaderStore);

        String payload = response.getEntity(String.class);
        payload = payload.substring(payload.indexOf('{'));
        JSONObject payloadObject = new JSONObject(payload);
        assertNotNull(payloadObject.get("id"),
                "Response payload is not the in the correct format" + response.getEntity(String.class));

        //verify e-mail
        String pointBrowserURL = EmailUtil.readGmailInboxForVerification();
        assertTrue(pointBrowserURL.contains("https"), "Verification mail has failed to reach Gmail inbox");
        EmailUtil.browserRedirectionOnVerification(pointBrowserURL, loginURL,
                automationContext.getContextTenant().getContextUser().getUserName(),
                automationContext.getContextTenant().getContextUser().getPassword());

        // update the resource in order to retrieve e-mail
        String dataBody = readFile(resourcePath + "json" + File.separator + "PublisherRestResourceUpdate.json");
        response = genericRestClient
                .geneticRestRequestPost(publisherUrl + "/assets/" + assetId, MediaType.APPLICATION_JSON,
                        MediaType.APPLICATION_JSON, dataBody, queryParamMap, headerMap, cookieHeaderPublisher);
        payloadObject = new JSONObject(response.getEntity(String.class));
        assertTrue((response.getStatusCode() == Response.Status.ACCEPTED.getStatusCode()),
                "Wrong status code ,Expected 202 Created ,Received " + response.getStatusCode());
        assertTrue(payloadObject.getJSONObject("attributes").get("overview_context").equals("/changed/Context"));
        isNotificationMailAvailable = EmailUtil.readGmailInboxForNotification("StoreResourceUpdated");
        assertTrue(isNotificationMailAvailable, "Publisher resource updated mail has failed to reach Gmail nbox");
        isNotificationMailAvailable = false;
    }

    /**
     * Method used to authenticate publisher,store and create a rest service asset. Created asset
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

        //Create rest service
        queryParamMap.put("type", "restservice");
        String dataBody = readFile(resourcePath + "json" + File.separator + "publisherPublishRestResource.json");
        ClientResponse createResponse = genericRestClient
                .geneticRestRequestPost(publisherUrl + "/assets", MediaType.APPLICATION_JSON,
                        MediaType.APPLICATION_JSON, dataBody, queryParamMap, headerMap, cookieHeaderPublisher);
        JSONObject createObj = new JSONObject(createResponse.getEntity(String.class));
        assetId = (String) createObj.get("id");
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws RegistryException, JSONException, IOException, MessagingException {
        deleteAssetById(publisherUrl, genericRestClient, cookieHeaderPublisher, assetId, queryParamMap);
        EmailUtil.deleteSentMails();
    }

    @DataProvider
    private static TestUserMode[][] userModeProvider() {
        return new TestUserMode[][] { new TestUserMode[] { TestUserMode.SUPER_TENANT_ADMIN }
        };
    }
}
