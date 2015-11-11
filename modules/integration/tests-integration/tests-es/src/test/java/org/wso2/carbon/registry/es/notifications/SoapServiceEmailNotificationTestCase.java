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
import org.testng.annotations.*;
import org.wso2.carbon.automation.engine.configurations.UrlGenerationUtil;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.automation.test.utils.common.TestConfigurationProvider;
import org.wso2.carbon.identity.user.profile.stub.UserProfileMgtServiceUserProfileExceptionException;
import org.wso2.carbon.identity.user.profile.stub.types.UserFieldDTO;
import org.wso2.carbon.identity.user.profile.stub.types.UserProfileDTO;
import org.wso2.carbon.integration.common.utils.exceptions.AutomationUtilException;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.es.utils.EmailUtil;
import org.wso2.greg.integration.common.clients.UserProfileMgtServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.GenericRestClient;

import javax.ws.rs.core.MediaType;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * This test class can be used to check the email notification functionality
 */
public class SoapServiceEmailNotificationTestCase extends GREGIntegrationBaseTest {

    private TestUserMode userMode;
    String jSessionId;
    private UserProfileMgtServiceClient userProfileMgtClient;
    private File axis2File;
    private String publisherUrl;
    private String resourcePath;
    private String assetId;
    private String cookieHeader;
    private GenericRestClient genericRestClient;
    private Map<String, String> queryParamMap;
    private Map<String, String> headerMap;
    private String loginURL;
    private String emailAddress;
    private EmailUtil emailUtil;
    boolean isNotificationMailAvailable;


    @Factory(dataProvider = "userModeProvider")
    public SoapServiceEmailNotificationTestCase(TestUserMode userMode) {
        this.userMode = userMode;
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.init(userMode);
        loginURL = UrlGenerationUtil.getLoginURL(automationContext.getInstance());
        emailAddress = "gregtestes@gmail.com";
        genericRestClient = new GenericRestClient();
        queryParamMap = new HashMap<>();
        headerMap = new HashMap<>();
        resourcePath =
                FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator + "GREG" + File.separator;
        publisherUrl = automationContext.getContextUrls().getSecureServiceUrl().replace("services", "publisher/apis");
        userProfileMgtClient = new UserProfileMgtServiceClient(backendURL, sessionCookie);
        axis2File = new File(
                TestConfigurationProvider.getResourceLocation("GREG") + File.separator + "axis2" + File.separator
                        + "axis2.xml");

        updateProfileAndEnableEmailConfiguration();
        setTestEnvironment();

    }

    private void updateProfileAndEnableEmailConfiguration()
            throws UserProfileMgtServiceUserProfileExceptionException, IOException, XPathExpressionException,
            AutomationUtilException {

        UserProfileDTO profile = new UserProfileDTO();
        profile.setProfileName("default");

        UserFieldDTO lastName = new UserFieldDTO();
        lastName.setClaimUri("http://wso2.org/claims/lastname");
        lastName.setFieldValue("GregUserFirstName");

        UserFieldDTO givenName = new UserFieldDTO();
        givenName.setClaimUri("http://wso2.org/claims/givenname");
        givenName.setFieldValue("GregUserLastName");

        UserFieldDTO email = new UserFieldDTO();
        email.setClaimUri("http://wso2.org/claims/emailaddress");
        email.setFieldValue(emailAddress);

        UserFieldDTO[] fields = new UserFieldDTO[3];
        fields[0] = lastName;
        fields[1] = givenName;
        fields[2] = email;

        profile.setFieldValues(fields);

        userProfileMgtClient
                .setUserProfile(automationContext.getContextTenant().getContextUser().getUserName(), profile);

        // apply new axis2.xml configuration
        ServerConfigurationManager serverConfigurationManager = new ServerConfigurationManager(automationContext);
        serverConfigurationManager.applyConfiguration(axis2File);
    }

    @Test(groups = { "wso2.greg",
            "wso2.greg.es" }, description = "Adding subscription to soap service on LC state change",
            dependsOnMethods = { "addSubscriptionCheckListItem" ,"addSubscriptionUnCheckListItem"  })
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
        JSONObject obj = new JSONObject(payLoad);
        assertNotNull(obj.get("id").toString(),
                "Response payload is not the in the correct format" + response.getEntity(String.class));
        Thread.sleep(10000);

        // verify e-mail
        emailUtil = new EmailUtil(loginURL, automationContext.getContextTenant().getContextUser().getUserName(),
                automationContext.getContextTenant().getContextUser().getPassword());
        String pointBrowserURL = emailUtil.readGmailInboxForVerification();
        assertTrue(pointBrowserURL.contains("https"), "Verification mail has failed to reach Gmail inbox");
        emailUtil.browserRedirectionOnVerification(pointBrowserURL);

        // Change the life cycle state in order to retrieve e-mail

        genericRestClient.geneticRestRequestPost(publisherUrl + "/assets/" + assetId + "/state",
                MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_JSON,
                "nextState=Testing&comment=Completed", queryParamMap, headerMap, cookieHeader);

        Thread.sleep(25000);
        isNotificationMailAvailable = emailUtil.readGmailInboxForNotification("PublisherLifeCycleStateChanged");
        assertTrue(isNotificationMailAvailable, "Publisher LC state changed mail has failed to reach Gmail inbox");
        isNotificationMailAvailable = false;
    }

    @Test(groups = { "wso2.greg",
            "wso2.greg.es" }, description = "Adding subscription to Soap service on resource update")
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
        JSONObject obj = new JSONObject(payLoad);
        assertNotNull(obj.get("id").toString(),
                "Response payload is not the in the correct format" + response.getEntity(String.class));
        Thread.sleep(10000);

        // verify e-mail
        emailUtil = new EmailUtil(loginURL, automationContext.getContextTenant().getContextUser().getUserName(),
                automationContext.getContextTenant().getContextUser().getPassword());
        String pointBrowserURL = emailUtil.readGmailInboxForVerification();
        assertTrue(pointBrowserURL.contains("https"), "Verification mail has failed to reach Gmail inbox");
        emailUtil.browserRedirectionOnVerification(pointBrowserURL);

        // update the resource in order to retrieve e-mail
        String dataBody = readFile(resourcePath + "json" + File.separator + "PublisherSoapResourceUpdateFile.json");
        genericRestClient.geneticRestRequestPost(publisherUrl + "/assets/" + assetId, MediaType.APPLICATION_JSON,
                MediaType.APPLICATION_JSON, dataBody, queryParamMap, headerMap, cookieHeader);

        Thread.sleep(15000);
        isNotificationMailAvailable = emailUtil.readGmailInboxForNotification("PublisherResourceUpdated");
        assertTrue(isNotificationMailAvailable, "Publisher resource updated mail has failed to reach Gmail inbox");
        isNotificationMailAvailable = false;
    }

    @Test(groups = { "wso2.greg",
            "wso2.greg.es" }, description = "Adding subscription to soap service on check list item checked")
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
        JSONObject obj = new JSONObject(payLoad);
        assertNotNull(obj.get("id").toString(),
                "Response payload is not the in the correct format" + response.getEntity(String.class));
        Thread.sleep(10000);

        // verify e-mail
        emailUtil = new EmailUtil(loginURL, automationContext.getContextTenant().getContextUser().getUserName(),
                automationContext.getContextTenant().getContextUser().getPassword());
        String pointBrowserURL = emailUtil.readGmailInboxForVerification();
        assertTrue(pointBrowserURL.contains("https"), "Verification mail has failed to reach Gmail inbox");
        emailUtil.browserRedirectionOnVerification(pointBrowserURL);

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
        Thread.sleep(5000);
        isNotificationMailAvailable = emailUtil.readGmailInboxForNotification("PublisherCheckListItemChecked");
        assertTrue(isNotificationMailAvailable,
                "Publisher check list item ticked on life cycle, notification mail has failed to reach Gmail inbox");
        isNotificationMailAvailable = false;

    }

    @Test(groups = { "wso2.greg",
            "wso2.greg.es" }, description = "Adding subscription to soap service on check list item unchecked",
            dependsOnMethods = { "addSubscriptionCheckListItem" })
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
        JSONObject obj = new JSONObject(payLoad);
        assertNotNull(obj.get("id").toString(),
                "Response payload is not the in the correct format" + response.getEntity(String.class));
        Thread.sleep(10000);

        // verify e-mail
        emailUtil = new EmailUtil(loginURL, automationContext.getContextTenant().getContextUser().getUserName(),
                automationContext.getContextTenant().getContextUser().getPassword());
        String pointBrowserURL = emailUtil.readGmailInboxForVerification();
        assertTrue(pointBrowserURL.contains("https"), "Verification mail has failed to reach Gmail inbox");

        emailUtil.browserRedirectionOnVerification(pointBrowserURL);

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

        Thread.sleep(5000);
        isNotificationMailAvailable = emailUtil.readGmailInboxForNotification("PublisherCheckListItemUnchecked");
        assertTrue(isNotificationMailAvailable,
                "Publisher un check list item on life cycle notification mail has failed to reach Gmail inbox");
        isNotificationMailAvailable = false;

    }

    private void setTestEnvironment() throws JSONException, IOException {
        // Authenticate
        ClientResponse response = genericRestClient
                .geneticRestRequestPost(publisherUrl + "/authenticate/", MediaType.APPLICATION_FORM_URLENCODED,
                        MediaType.APPLICATION_JSON, "username=admin&password=admin", queryParamMap, headerMap, null);
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        jSessionId = obj.getJSONObject("data").getString("sessionId");
        cookieHeader = "JSESSIONID=" + jSessionId;

        //Create soap service
        queryParamMap.put("type", "soapservice");
        String dataBody = readFile(resourcePath + "json" + File.separator + "publisherPublishSoapResource.json");
        ClientResponse createResponse = genericRestClient
                .geneticRestRequestPost(publisherUrl + "/assets", MediaType.APPLICATION_JSON,
                        MediaType.APPLICATION_JSON, dataBody, queryParamMap, headerMap, cookieHeader);
        JSONObject createObj = new JSONObject(createResponse.getEntity(String.class));
        assetId = createObj.get("id").toString();
    }

    private void deleteSoapServiceAsset() throws JSONException {
        genericRestClient.geneticRestRequestDelete(publisherUrl + "/assets/" + assetId, MediaType.APPLICATION_JSON,
                MediaType.APPLICATION_JSON, queryParamMap, headerMap, cookieHeader);
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws RegistryException, JSONException {
        deleteSoapServiceAsset();
    }

    @DataProvider
    private static TestUserMode[][] userModeProvider() {
        return new TestUserMode[][]{
                new TestUserMode[]{TestUserMode.SUPER_TENANT_ADMIN}
                //                new TestUserMode[]{TestUserMode.TENANT_USER},
        };
    }
}

