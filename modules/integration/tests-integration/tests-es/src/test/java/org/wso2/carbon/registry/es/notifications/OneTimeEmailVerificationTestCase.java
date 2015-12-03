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
import org.wso2.greg.integration.common.utils.GenericRestClient;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.mail.MessagingException;
import javax.ws.rs.core.MediaType;
import javax.xml.xpath.XPathExpressionException;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * This test class can be used to check the email notification functionality for
 * multiple updates after a single verification at the publisher.
 */
public class OneTimeEmailVerificationTestCase extends GregESTestBaseTest {

    private TestUserMode userMode;
    private String publisherUrl;
    private String resourcePath;
    private String assetId;
    private String cookieHeader;
    private GenericRestClient genericRestClient;
    private Map<String, String> queryParamMap;
    private Map<String, String> headerMap;
    private String loginURL;


    @Factory(dataProvider = "userModeProvider")
    public OneTimeEmailVerificationTestCase(TestUserMode userMode) {
        this.userMode = userMode;
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.init(userMode);
        loginURL = UrlGenerationUtil.getLoginURL(automationContext.getInstance());
        genericRestClient = new GenericRestClient();
        queryParamMap = new HashMap<>();
        headerMap = new HashMap<>();
        resourcePath =
                FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator + "GREG" + File.separator;
        publisherUrl = automationContext.getContextUrls().getSecureServiceUrl().replace("services", "publisher/apis");

        EmailUtil.updateProfileAndEnableEmailConfiguration(automationContext, backendURL, sessionCookie);
        setTestEnvironment();
    }

    /**
     * This test case add subscription to lifecycle state change and verifies the reception of multiple
     * email notifications by changing the life cycle state several times.
     */
    @Test(groups = { "wso2.greg",
            "wso2.greg.es" }, description = "Check multiple LC state change notifications after a single verification",
            dependsOnMethods = { "addSubscriptionCheckListItem", "addSubscriptionUnCheckListItem" })
    public void addSubscriptionToLcStateChange() throws Exception {

        JSONObject dataObject = new JSONObject();
        dataObject.put("notificationType", "PublisherLifeCycleStateChanged");
        dataObject.put("notificationMethod", "email");

        ClientResponse response = genericRestClient
                .geneticRestRequestPost(publisherUrl + "/subscriptions/soapservice/" + assetId,
                        MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, dataObject.toString(), queryParamMap,
                        headerMap, cookieHeader);

        String payLoad = response.getEntity(String.class);
        payLoad = payLoad.substring(payLoad.indexOf('{'));
        JSONObject payLoadObject = new JSONObject(payLoad);
        assertNotNull(payLoadObject.get("id"),
                "Response payload is not the in the correct format" + response.getEntity(String.class));

        // verify e-mail
        String pointBrowserURL = EmailUtil.readGmailInboxForVerification();
        assertTrue(pointBrowserURL.contains("https"), "Verification mail has failed to reach Gmail inbox");
        EmailUtil.browserRedirectionOnVerification(pointBrowserURL, loginURL,
                automationContext.getContextTenant().getContextUser().getUserName(),
                automationContext.getContextTenant().getContextUser().getPassword());

        // Change the life cycle state to testing

        genericRestClient.geneticRestRequestPost(publisherUrl + "/assets/" + assetId + "/state",
                MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_JSON,
                "nextState=Testing&comment=Completed", queryParamMap, headerMap, cookieHeader);
        assertTrue(EmailUtil.readGmailInboxForNotification("PublisherLifeCycleStateChanged"),
                "Publisher LC state changed mail has failed to reach Gmail inbox");

        // change life cycle state to production
        genericRestClient.geneticRestRequestPost(publisherUrl + "/assets/" + assetId + "/state",
                MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_JSON,
                "nextState=Production&comment=Completed", queryParamMap, headerMap, cookieHeader);
        assertTrue(EmailUtil.readGmailInboxForNotification("PublisherLifeCycleStateChanged"),
                "Publisher LC state changed mail has failed to reach Gmail inbox");

        // Change life cycle state back to testing
        genericRestClient.geneticRestRequestPost(publisherUrl + "/assets/" + assetId + "/state",
                MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_JSON, "nextState=Testing&comment=Demoting",
                queryParamMap, headerMap, cookieHeader);
        assertTrue(EmailUtil.readGmailInboxForNotification("PublisherLifeCycleStateChanged"),
                "Publisher LC state changed mail has failed to reach Gmail inbox");

    }

    /**
     * This test case add subscription to resource update and verifies the reception of multiple email notifications
     * by updating the resource several times.
     */
    @Test(groups = { "wso2.greg",
            "wso2.greg.es" }, description = "Check multiple resource update notifications after a single verification")
    public void addSubscriptionOnResourceUpdate() throws Exception {

        JSONObject dataObject = new JSONObject();
        dataObject.put("notificationType", "PublisherResourceUpdated");
        dataObject.put("notificationMethod", "email");

        ClientResponse response = genericRestClient
                .geneticRestRequestPost(publisherUrl + "/subscriptions/soapservice/" + assetId,
                        MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, dataObject.toString(), queryParamMap,
                        headerMap, cookieHeader);

        String payLoad = response.getEntity(String.class);
        payLoad = payLoad.substring(payLoad.indexOf('{'));
        JSONObject payLoadObject = new JSONObject(payLoad);
        assertNotNull(payLoadObject.get("id"),
                "Response payload is not the in the correct format" + response.getEntity(String.class));

        // verify e-mail
        String pointBrowserURL = EmailUtil.readGmailInboxForVerification();
        assertTrue(pointBrowserURL.contains("https"), "Verification mail has failed to reach Gmail inbox");
        EmailUtil.browserRedirectionOnVerification(pointBrowserURL, loginURL,
                automationContext.getContextTenant().getContextUser().getUserName(),
                automationContext.getContextTenant().getContextUser().getPassword());

        // update the resource in order to retrieve e-mail
        String dataBody = readFile(resourcePath + "json" + File.separator + "PublisherSoapResourceUpdateFile.json");
        genericRestClient.geneticRestRequestPost(publisherUrl + "/assets/" + assetId, MediaType.APPLICATION_JSON,
                MediaType.APPLICATION_JSON, dataBody, queryParamMap, headerMap, cookieHeader);
        assertTrue(EmailUtil.readGmailInboxForNotification("PublisherResourceUpdated"),
                "Publisher resource updated mail has failed to reach Gmail inbox");

        // update again in order to retrieve the e-mail
        dataBody = readFile(resourcePath + "json" + File.separator + "PublisherSoapResourceUpdateFile.json");
        genericRestClient.geneticRestRequestPost(publisherUrl + "/assets/" + assetId, MediaType.APPLICATION_JSON,
                MediaType.APPLICATION_JSON, dataBody, queryParamMap, headerMap, cookieHeader);
        assertTrue(EmailUtil.readGmailInboxForNotification("PublisherResourceUpdated"),
                "Publisher resource updated mail has failed to reach Gmail inbox");
    }

    /**
     * This test case add subscription to selecting check list item of life cycle and verifies
     * the reception of multiple email notifications by selecting multiple check list items.
     */
    @Test(groups = { "wso2.greg", "wso2.greg.es" }, description =
            "Test notifications of multiple items selecting on LC check list after a single verification ")
    public void addSubscriptionCheckListItem() throws Exception {

        JSONObject dataObject = new JSONObject();
        dataObject.put("notificationType", "PublisherCheckListItemChecked");
        dataObject.put("notificationMethod", "email");

        ClientResponse response = genericRestClient
                .geneticRestRequestPost(publisherUrl + "/subscriptions/soapservice/" + assetId,
                        MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, dataObject.toString(), queryParamMap,
                        headerMap, cookieHeader);

        String payLoad = response.getEntity(String.class);
        payLoad = payLoad.substring(payLoad.indexOf('{'));
        JSONObject payLoadObject = new JSONObject(payLoad);
        assertNotNull(payLoadObject.get("id"),
                "Response payload is not the in the correct format" + response.getEntity(String.class));

        // verify e-mail
        String pointBrowserURL = EmailUtil.readGmailInboxForVerification();
        assertTrue(pointBrowserURL.contains("https"), "Verification mail has failed to reach Gmail inbox");
        EmailUtil.browserRedirectionOnVerification(pointBrowserURL, loginURL,
                automationContext.getContextTenant().getContextUser().getUserName(),
                automationContext.getContextTenant().getContextUser().getPassword());

        // check  first item  on LC
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
        assertTrue(EmailUtil.readGmailInboxForNotification("PublisherCheckListItemChecked"),
                "Publisher check list item ticked on life cycle, notification mail has failed to reach Gmail inbox");

        // check  second item  on LC
        queryParamMap.put("lifecycle", "ServiceLifeCycle");
        checkListObject = new JSONObject();
        checkedItems = new JSONObject();
        checkedItemsArray = new JSONArray();
        checkedItems.put("index", 1);
        checkedItems.put("checked", true);
        checkedItemsArray.put(checkedItems);
        checkListObject.put("checklist", checkedItemsArray);

        genericRestClient.geneticRestRequestPost(publisherUrl + "/asset/" + assetId + "/update-checklist",
                MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, checkListObject.toString(), queryParamMap,
                headerMap, cookieHeader);
        assertTrue(EmailUtil.readGmailInboxForNotification("PublisherCheckListItemChecked"),
                "Publisher check list item ticked on life cycle, notification mail has failed to reach Gmail inbox");

        // check  third item  on LC
        queryParamMap.put("lifecycle", "ServiceLifeCycle");
        checkListObject = new JSONObject();
        checkedItems = new JSONObject();
        checkedItemsArray = new JSONArray();
        checkedItems.put("index", 2);
        checkedItems.put("checked", true);
        checkedItemsArray.put(checkedItems);
        checkListObject.put("checklist", checkedItemsArray);

        genericRestClient.geneticRestRequestPost(publisherUrl + "/asset/" + assetId + "/update-checklist",
                MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, checkListObject.toString(), queryParamMap,
                headerMap, cookieHeader);
        assertTrue(EmailUtil.readGmailInboxForNotification("PublisherCheckListItemChecked"),
                "Publisher check list item ticked on life cycle, notification mail has failed to reach Gmail inbox");

    }

    /**
     * This test case add subscription to un ticking check list item of life cycle and verifies
     * the reception of multiple email notifications by un ticking multiple check list items.
     */
    @Test(groups = { "wso2.greg", "wso2.greg.es" }, description =
            "Test notifications of multiple items un ticking on LC check list after "
                    + "a single verification ", dependsOnMethods = { "addSubscriptionCheckListItem" })
    public void addSubscriptionUnCheckListItem() throws Exception {

        JSONObject dataObject = new JSONObject();
        dataObject.put("notificationType", "PublisherCheckListItemUnchecked");
        dataObject.put("notificationMethod", "email");

        ClientResponse response = genericRestClient
                .geneticRestRequestPost(publisherUrl + "/subscriptions/soapservice/" + assetId,
                        MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, dataObject.toString(), queryParamMap,
                        headerMap, cookieHeader);

        String payLoad = response.getEntity(String.class);
        payLoad = payLoad.substring(payLoad.indexOf('{'));
        JSONObject payLoadObject = new JSONObject(payLoad);
        assertNotNull(payLoadObject.get("id"),
                "Response payload is not the in the correct format" + response.getEntity(String.class));

        // verify e-mail
        String pointBrowserURL = EmailUtil.readGmailInboxForVerification();
        assertTrue(pointBrowserURL.contains("https"), "Verification mail has failed to reach Gmail inbox");
        EmailUtil.browserRedirectionOnVerification(pointBrowserURL, loginURL,
                automationContext.getContextTenant().getContextUser().getUserName(),
                automationContext.getContextTenant().getContextUser().getPassword());

        // un check  first item on LC
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

        assertTrue(EmailUtil.readGmailInboxForNotification("PublisherCheckListItemUnchecked"),
                "Publisher un check list item on life cycle notification mail has failed to reach Gmail inbox");

        // un check  second item on LC
        checkListObject = new JSONObject();
        checkedItems = new JSONObject();
        checkedItemsArray = new JSONArray();
        checkedItems.put("index", 1);
        checkedItems.put("checked", false);
        checkedItemsArray.put(checkedItems);
        checkListObject.put("checklist", checkedItemsArray);

        genericRestClient.geneticRestRequestPost(publisherUrl + "/asset/" + assetId + "/update-checklist",
                MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, checkListObject.toString(), queryParamMap,
                headerMap, cookieHeader);

        assertTrue(EmailUtil.readGmailInboxForNotification("PublisherCheckListItemUnchecked"),
                "Publisher un check list item on life cycle notification mail has failed to reach Gmail inbox");

        // un check  third item on LC
        checkListObject = new JSONObject();
        checkedItems = new JSONObject();
        checkedItemsArray = new JSONArray();
        checkedItems.put("index", 2);
        checkedItems.put("checked", false);
        checkedItemsArray.put(checkedItems);
        checkListObject.put("checklist", checkedItemsArray);

        genericRestClient.geneticRestRequestPost(publisherUrl + "/asset/" + assetId + "/update-checklist",
                MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, checkListObject.toString(), queryParamMap,
                headerMap, cookieHeader);

        assertTrue(EmailUtil.readGmailInboxForNotification("PublisherCheckListItemUnchecked"),
                "Publisher un check list item on life cycle notification mail has failed to reach Gmail inbox");

    }

    /**
     * Method used to authenticate publisher and create a soap service asset. Created asset
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

        //Create soap service
        queryParamMap.put("type", "soapservice");
        String dataBody = readFile(resourcePath + "json" + File.separator + "publisherPublishSoapResource.json");
        ClientResponse createResponse = genericRestClient
                .geneticRestRequestPost(publisherUrl + "/assets", MediaType.APPLICATION_JSON,
                        MediaType.APPLICATION_JSON, dataBody, queryParamMap, headerMap, cookieHeader);
        JSONObject createObj = new JSONObject(createResponse.getEntity(String.class));
        assetId = (String) createObj.get("id");
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws RegistryException, JSONException, IOException, MessagingException {
        deleteAssetById(publisherUrl, genericRestClient, cookieHeader, assetId, queryParamMap);
        EmailUtil.deleteSentMails();
    }

    @DataProvider
    private static TestUserMode[][] userModeProvider() {
        return new TestUserMode[][]{
                new TestUserMode[]{TestUserMode.SUPER_TENANT_ADMIN}
                //                new TestUserMode[]{TestUserMode.TENANT_USER},
        };
    }
}

