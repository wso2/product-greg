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
import org.json.JSONArray;
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
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GenericRestClient;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.activation.DataHandler;
import javax.ws.rs.core.MediaType;
import javax.xml.xpath.XPathExpressionException;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * This test class can be used to check the email notification functionality for custom asset type at the publisher.
 */

public class CustomRXTEmailNotificationTestCase extends GregESTestBaseTest {

    public static final String RXT_STORAGE_PATH =
            "/_system/governance/repository/components/org.wso2.carbon.governance/types/applications.rxt";
    private TestUserMode userMode;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private String publisherUrl;
    private String resourcePath;
    private String assetId;
    private String cookieHeader;
    private GenericRestClient genericRestClient;
    private Map<String, String> queryParamMap;
    private Map<String, String> headerMap;
    private String loginURL;
    private boolean isNotificationMailAvailable;

    @Factory(dataProvider = "userModeProvider")
    public CustomRXTEmailNotificationTestCase(TestUserMode userMode) {
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
        resourceAdminServiceClient = new ResourceAdminServiceClient(backendURL, sessionCookie);
        addCustomRxt();
        queryParamMap.put("type", "applications");
        EmailUtil.updateProfileAndEnableEmailConfiguration(automationContext, backendURL, sessionCookie);
        setTestEnvironment();
    }

    /**
     * This test case add subscription to lifecycle state change and verifies the reception of email notification
     * by changing the life cycle state.
     */
    @Test(groups = { "wso2.greg",
            "wso2.greg.es" }, description = "Adding subscription to custom asset on LC state change",
            dependsOnMethods = { "addSubscriptionCheckListItem" , "addSubscriptionUnCheckListItem"})
    public void addSubscriptionToLcStateChange() throws Exception {
        JSONObject dataObject = new JSONObject();
        dataObject.put("notificationType", "PublisherLifeCycleStateChanged");
        dataObject.put("notificationMethod", "email");
        ClientResponse response = genericRestClient
                .geneticRestRequestPost(publisherUrl + "/subscriptions/applications/" + assetId,
                        MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, dataObject.toString(), queryParamMap,
                        headerMap, cookieHeader);

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
                "nextState=Testing&comment=Completed", queryParamMap, headerMap, cookieHeader);

        isNotificationMailAvailable = EmailUtil.readGmailInboxForNotification("PublisherLifeCycleStateChanged");
        assertTrue(isNotificationMailAvailable,
                "Publisher LC state changed notification mail has failed to reach Gmail inbox");
        isNotificationMailAvailable = false;
    }

    /**
     * This test case add subscription to resource update and verifies the reception of email notification
     * by updating the resource.
     */
    @Test(groups = { "wso2.greg",
            "wso2.greg.es" }, description = "Adding subscription to custom asset on resource update")
    public void addSubscriptionOnResourceUpdate() throws Exception {
        JSONObject dataObject = new JSONObject();
        dataObject.put("notificationType", "PublisherResourceUpdated");
        dataObject.put("notificationMethod", "email");

        ClientResponse response = genericRestClient
                .geneticRestRequestPost(publisherUrl + "/subscriptions/applications/" + assetId,
                        MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, dataObject.toString(), queryParamMap,
                        headerMap, cookieHeader);

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

        // update the resource in order to retrieve e-mail
        String dataBody = readFile(resourcePath + "json" + File.separator + "PublisherCustomResourceUpdate.json");
        genericRestClient.geneticRestRequestPost(publisherUrl + "/assets/" + assetId, MediaType.APPLICATION_JSON,
                MediaType.APPLICATION_JSON, dataBody, queryParamMap, headerMap, cookieHeader);
        isNotificationMailAvailable = EmailUtil.readGmailInboxForNotification("PublisherResourceUpdated");
        assertTrue(isNotificationMailAvailable, "Publisher resource updated mail has failed to reach Gmail inbox");
        isNotificationMailAvailable = false;
    }

    /**
     * This test case add subscription to selecting check list item of life cycle and verifies
     * the reception of email notification by selecting the check list item.
     */
    @Test(groups = { "wso2.greg",
            "wso2.greg.es" }, description = "Adding subscription to custom asset on check list item checked")
    public void addSubscriptionCheckListItem() throws Exception {
        JSONObject dataObject = new JSONObject();
        dataObject.put("notificationType", "PublisherCheckListItemChecked");
        dataObject.put("notificationMethod", "email");

        ClientResponse response = genericRestClient
                .geneticRestRequestPost(publisherUrl + "/subscriptions/applications/" + assetId,
                        MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, dataObject.toString(), queryParamMap,
                        headerMap, cookieHeader);

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

        // check items on LC
        queryParamMap.put("lifecycle", "ServiceLifeCycle");
        JSONObject checkListObject = new JSONObject();
        JSONObject checkedItems = new JSONObject();
        JSONArray checkedItemsArray = new JSONArray();
        checkedItems.put("index", 0);
        checkedItems.put("checked", true);
        checkedItemsArray.put(checkedItems);
        checkListObject.put("checklist", checkedItemsArray);

        genericRestClient.geneticRestRequestPost(publisherUrl + "/asset/" + assetId + "/update-checklist",
                MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, checkListObject.toString(), queryParamMap,
                headerMap, cookieHeader);
        isNotificationMailAvailable = EmailUtil.readGmailInboxForNotification("PublisherCheckListItemChecked");
        assertTrue(isNotificationMailAvailable,
                "Publisher check list item on life cycle, notification mail has failed to reach Gmail inbox");
        isNotificationMailAvailable = false;
    }

    /**
     * This test case add subscription to un ticking check list item of life cycle and verifies
     * the reception of email notification by un ticking the check list item.
     */
    @Test(groups = { "wso2.greg",
            "wso2.greg.es" }, description = "Adding subscription to custom asset on check list item unchecked",
            dependsOnMethods = { "addSubscriptionCheckListItem"})
    public void addSubscriptionUnCheckListItem() throws Exception {
        JSONObject dataObject = new JSONObject();
        dataObject.put("notificationType", "PublisherCheckListItemUnchecked");
        dataObject.put("notificationMethod", "email");
        ClientResponse response = genericRestClient
                .geneticRestRequestPost(publisherUrl + "/subscriptions/applications/" + assetId,
                        MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, dataObject.toString(), queryParamMap,
                        headerMap, cookieHeader);

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

        // un check items on LC
        JSONObject checkListObject = new JSONObject();
        JSONObject checkedItems = new JSONObject();
        JSONArray checkedItemsArray = new JSONArray();
        checkedItems.put("index", 0);
        checkedItems.put("checked", false);
        checkedItemsArray.put(checkedItems);
        checkListObject.put("checklist", checkedItemsArray);

        genericRestClient.geneticRestRequestPost(publisherUrl + "/asset/" + assetId + "/update-checklist",
                MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, checkListObject.toString(), queryParamMap,
                headerMap, cookieHeader);
        isNotificationMailAvailable = EmailUtil.readGmailInboxForNotification("PublisherCheckListItemUnchecked");
        assertTrue(isNotificationMailAvailable,
                "Publisher un check list item on life cycle, notification mail has failed to reached Gmail inbox");
        isNotificationMailAvailable = false;
    }

    /**
     * Method used to add custom RXT (application.rxt)
     */
    private void addCustomRxt()
            throws RegistryException, IOException, ResourceAdminServiceExceptionException, InterruptedException {
        StringBuilder builder = new StringBuilder();
        builder.append(getTestArtifactLocation()).append("artifacts").append(File.separator).append("GREG").
                append(File.separator).append("rxt").append(File.separator).append("application.rxt");
        String filePath = builder.toString();
        DataHandler dh = new DataHandler(new URL("file:///" + filePath));
        resourceAdminServiceClient
                .addResource(RXT_STORAGE_PATH, "application/vnd.wso2.registry-ext-type+xml", "desc", dh);
    }

    /**
     * Method used to delete custom RXT (application.rxt)
     */
    private void deleteCustomRxt() throws Exception {
        String session = getSessionCookie();
        resourceAdminServiceClient = new ResourceAdminServiceClient(backendURL, session);
        resourceAdminServiceClient.deleteResource(RXT_STORAGE_PATH);
    }

    /**
     * Need to refresh the landing page to deploy the new rxt in publisher
     */
    private void refreshPublisherLandingPage() {
        Map<String, String> queryParamMap = new HashMap<>();
        String landingUrl = publisherUrl.replace("apis", "pages/gc-landing");
        genericRestClient.geneticRestRequestGet(landingUrl, queryParamMap, headerMap, cookieHeader);
    }

    /**
     * Method used to authenticate publisher and create asset of type applications. Created asset
     * is used to add subscriptions and to receive notification.
     */
    private void setTestEnvironment() throws JSONException, IOException, XPathExpressionException {
        // Authenticate Publisher
        ClientResponse response = authenticate(publisherUrl, genericRestClient,
                automationContext.getSuperTenant().getTenantAdmin().getUserName(),
                automationContext.getSuperTenant().getTenantAdmin().getPassword());
        JSONObject responseObject = new JSONObject(response.getEntity(String.class));
        String jSessionId = responseObject.getJSONObject("data").getString("sessionId");
        cookieHeader = "JSESSIONID=" + jSessionId;
        //refresh the publisher landing page to deploy new rxt type
        refreshPublisherLandingPage();

        //Create custom asset
        queryParamMap.put("type", "applications");
        String dataBody = readFile(resourcePath + "json" + File.separator + "publisherPublishCustomResource.json");
        ClientResponse createResponse = genericRestClient
                .geneticRestRequestPost(publisherUrl + "/assets", MediaType.APPLICATION_JSON,
                        MediaType.APPLICATION_JSON, dataBody, queryParamMap, headerMap, cookieHeader);
        JSONObject createObj = new JSONObject(createResponse.getEntity(String.class));
        assetId = (String) createObj.get("id");
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws Exception {
        deleteAssetById(publisherUrl, genericRestClient, cookieHeader, assetId, queryParamMap);
        deleteCustomRxt();
        EmailUtil.deleteSentMails();
    }

    @DataProvider
    private static TestUserMode[][] userModeProvider() {
        return new TestUserMode[][]{
                new TestUserMode[]{TestUserMode.SUPER_TENANT_ADMIN}
        };
    }
}
