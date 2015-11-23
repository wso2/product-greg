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
import org.wso2.carbon.automation.engine.configurations.UrlGenerationUtil;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.automation.test.utils.common.TestConfigurationProvider;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException;
import org.wso2.carbon.identity.user.profile.stub.UserProfileMgtServiceUserProfileExceptionException;
import org.wso2.carbon.identity.user.profile.stub.types.UserFieldDTO;
import org.wso2.carbon.identity.user.profile.stub.types.UserProfileDTO;
import org.wso2.carbon.integration.common.utils.exceptions.AutomationUtilException;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.es.utils.EmailUtil;
import org.wso2.carbon.registry.es.utils.GregESTestBaseTest;
import org.wso2.greg.integration.common.clients.CustomLifecyclesChecklistAdminClient;
import org.wso2.greg.integration.common.clients.LifeCycleAdminServiceClient;
import org.wso2.greg.integration.common.clients.UserProfileMgtServiceClient;
import org.wso2.greg.integration.common.utils.GenericRestClient;

import javax.mail.MessagingException;
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
 * This test class can be used to check the email notification functionality on WSDL resources.
 */
public class WSDLEmailNotificationTestCase extends GregESTestBaseTest {

    private TestUserMode userMode;
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
    private String assetName;
    private LifeCycleAdminServiceClient lifeCycleAdminServiceClient;
    private CustomLifecyclesChecklistAdminClient customLifecyclesChecklistAdminClient;
    private String path;
    private String lifeCycleName;
    private String jSessionId;
    private boolean isNotificationMailAvailable;
    private final static String STATE_CHANGE_MESSAGE = " State changed successfully to Testing!";
    private final static String LIFECYCLE = "ServiceLifeCycle";

    @Factory(dataProvider = "userModeProvider")
    public WSDLEmailNotificationTestCase(TestUserMode userMode) {
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
        //need lifeCycleAdminServiceClient to attach a lifecycle to the WSDL, as WSDLs does not come with
        //a default lifecycle attached
        lifeCycleAdminServiceClient = new LifeCycleAdminServiceClient(backendURL, sessionCookie);
        customLifecyclesChecklistAdminClient = new CustomLifecyclesChecklistAdminClient(backendURL, sessionCookie);
        lifeCycleName = LIFECYCLE;
        setTestEnvironment();
    }

    @Test(groups = "wso2.greg", description = "Updating the default user profile and configure axis2.xml file",
          dependsOnMethods = {"createWSDLAssetWithLC"})
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

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws RegistryException, JSONException, IOException, MessagingException {
        deleteWSDLAsset();
        EmailUtil.deleteSentMails();
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

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "create a wsdl with a LC attached.")
    public void createWSDLAssetWithLC()
            throws JSONException, InterruptedException, IOException,
                   CustomLifecyclesChecklistAdminServiceExceptionException {
        queryParamMap.put("type", "wsdl");
        String wsdlTemplate = readFile(resourcePath + "json" + File.separator + "wsdl-sample.json");
        assetName = "echo.wsdl";
        String dataBody = String.format(wsdlTemplate,
                                        "https://raw.githubusercontent.com/wso2/wso2-qa-artifacts/master/automation-artifacts/greg/wsdl/StockQuote.wsdl",
                                        assetName,
                                        "1.0.0");
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
        searchWsdlAsset();
        //attach a LC to the wsdl
        lifeCycleAdminServiceClient.addAspect(path, lifeCycleName);
        Assert.assertNotNull(assetId, "Empty asset resource id available" +
                                      response.getEntity(String.class));
        Assert.assertTrue(this.getAsset(assetId, "wsdl").get("lifecycle")
                                  .equals(lifeCycleName), "LifeCycle not assigned to given asset");
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Adding subscription to a wsdl LC check list item check",
          dependsOnMethods = {"updateProfileAndEnableEmailConfiguration"})
    public void addSubscriptionForLCCheckListItemCheck() throws Exception {
        setTestEnvironment();
        JSONObject dataObject = new JSONObject();
        Map<String, String> newQueryParamMap = new HashMap<String, String>();
        dataObject.put("notificationType", "PublisherCheckListItemChecked");
        dataObject.put("notificationMethod", "email");

        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/subscriptions/wsdl/" + assetId, MediaType.APPLICATION_JSON,
                                                         MediaType.APPLICATION_JSON, dataObject.toString()
                        , newQueryParamMap, headerMap, cookieHeader);

        assertTrue((response.getStatusCode() == Response.Status.OK.getStatusCode()),
                   "Wrong status code ,Expected" + Response.Status.OK.getStatusCode() + "Created ,Received " +
                   response.getStatusCode());

        // verify e-mail
        verifyEmail();
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Adding subscription to a wsdl LC check list item uncheck",
          dependsOnMethods = {"checkLCCheckItemsOnWSDL"})
    public void addSubscriptionForLCCheckListItemUnCheck() throws Exception {

        JSONObject dataObject = new JSONObject();

        dataObject.put("notificationType", "PublisherCheckListItemUnchecked");
        dataObject.put("notificationMethod", "email");

        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/subscriptions/wsdl/" + assetId, MediaType.APPLICATION_JSON,
                                                         MediaType.APPLICATION_JSON, dataObject.toString()
                        , queryParamMap, headerMap, cookieHeader);

        assertTrue((response.getStatusCode() == Response.Status.OK.getStatusCode()),
                   "Wrong status code ,Expected" + Response.Status.OK.getStatusCode() + "Created ,Received " +
                   response.getStatusCode());
        // verify e-mail
        verifyEmail();
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Adding subscription to a wsdl LC state change",
          dependsOnMethods = {"uncheckLCCheckItemsOnWSDL"})
    public void addSubscriptionForLCStateChange() throws Exception {

        JSONObject dataObject = new JSONObject();

        dataObject.put("notificationType", "PublisherLifeCycleStateChanged");
        dataObject.put("notificationMethod", "email");

        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/subscriptions/wsdl/" + assetId, MediaType.APPLICATION_JSON,
                                                         MediaType.APPLICATION_JSON, dataObject.toString()
                        , queryParamMap, headerMap, cookieHeader);

        assertTrue((response.getStatusCode() == Response.Status.OK.getStatusCode()),
                   "Wrong status code ,Expected" + Response.Status.OK.getStatusCode() + "Created ,Received " +
                   response.getStatusCode());
        // verify e-mail
        verifyEmail();
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "WSDL LC check list item check",
          dependsOnMethods = {"addSubscriptionForLCCheckListItemCheck"})
    public void checkLCCheckItemsOnWSDL() throws Exception {
        queryParamMap.put("type", "wsdl");
        queryParamMap.put("lifecycle", lifeCycleName);
        JSONObject LCStateobj = getLifeCycleState(assetId, "wsdl");
        JSONObject dataObj = LCStateobj.getJSONObject("data");
        JSONArray checkItems = dataObj.getJSONArray("checkItems");
        Assert.assertEquals(((JSONObject) checkItems.get(0)).getString("isVisible"), "true");
        ClientResponse responseCheck0 =
                checkLifeCycleCheckItem(cookieHeader, 0);
        Assert.assertTrue(responseCheck0.getStatusCode() == 200);
        isNotificationMailAvailable = EmailUtil.readGmailInboxForNotification("PublisherCheckListItemChecked");
        assertTrue(isNotificationMailAvailable,
                   "Publisher check list item ticked on life cycle, notification mail has failed to reach Gmail inbox");
        isNotificationMailAvailable = false;
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "WSDL LC check list item check",
          dependsOnMethods = {"addSubscriptionForLCCheckListItemUnCheck"})
    public void uncheckLCCheckItemsOnWSDL() throws Exception {
        queryParamMap.put("type", "wsdl");
        queryParamMap.put("lifecycle", lifeCycleName);
        JSONObject LCStateobj = getLifeCycleState(assetId, "wsdl");
        JSONObject dataObj = LCStateobj.getJSONObject("data");
        JSONArray checkItems = dataObj.getJSONArray("checkItems");
        Assert.assertEquals(((JSONObject) checkItems.get(0)).getString("isVisible"), "true");
        ClientResponse responseUncheck0 =
                uncheckLifeCycleCheckItem(cookieHeader, 0);
        Assert.assertTrue(responseUncheck0.getStatusCode() == 200);
        isNotificationMailAvailable = EmailUtil.readGmailInboxForNotification("PublisherCheckListItemUnchecked");
        assertTrue(isNotificationMailAvailable,
                   "Publisher uncheck list item ticked on life cycle, notification mail has failed to reach Gmail inbox");
        isNotificationMailAvailable = false;
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Change LC state on WSDL",
          dependsOnMethods = {"addSubscriptionForLCStateChange"})
    public void changeLCStateWSDL() throws Exception {
        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/assets/" + assetId + "/state",
                                                         MediaType.APPLICATION_FORM_URLENCODED,
                                                         MediaType.APPLICATION_JSON,
                                                         "nextState=Testing&comment=Completed"
                        , queryParamMap, headerMap, cookieHeader);
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        String status = obj.get("status").toString();
        Assert.assertEquals(status, STATE_CHANGE_MESSAGE);
        isNotificationMailAvailable = EmailUtil.readGmailInboxForNotification("PublisherLifeCycleStateChanged");
        assertTrue(isNotificationMailAvailable,
                   "Publisher lifecycle state changed notification mail has failed to reach Gmail inbox");
        isNotificationMailAvailable = false;

    }

    /**
     * This method get all the wsdls in publisher and select the one created by createWSDLAssetWithLC method.
     *
     * @throws JSONException
     */
    public void searchWsdlAsset() throws JSONException {
        Map<String, String> queryParamMap = new HashMap<>();
        queryParamMap.put("type", "wsdl");
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

    private ClientResponse checkLifeCycleCheckItem(String managerCookieHeader, int itemId) {
        return genericRestClient.geneticRestRequestPost(publisherUrl + "/asset/" + assetId + "/update-checklist",
                                                        MediaType.APPLICATION_JSON,
                                                        MediaType.APPLICATION_JSON,
                                                        "{\"checklist\":[{\"index\":" + itemId + ",\"checked\":true}]}"
                , queryParamMap, headerMap, managerCookieHeader);
    }

    private ClientResponse uncheckLifeCycleCheckItem(String managerCookieHeader, int itemId) {
        return genericRestClient.geneticRestRequestPost(publisherUrl + "/asset/" + assetId + "/update-checklist",
                                                        MediaType.APPLICATION_JSON,
                                                        MediaType.APPLICATION_JSON,
                                                        "{\"checklist\":[{\"index\":" + itemId + ",\"checked\":false}]}"
                , queryParamMap, headerMap, managerCookieHeader);
    }

    private void deleteWSDLAsset() throws JSONException {
        queryParamMap.clear();
        queryParamMap.put("type", "wsdl");
        genericRestClient.geneticRestRequestDelete(publisherUrl + "/assets/" + assetId, MediaType.APPLICATION_JSON,
                                                   MediaType.APPLICATION_JSON, queryParamMap, headerMap, cookieHeader);
    }

    private void verifyEmail() throws Exception {
        String pointBrowserURL = EmailUtil.readGmailInboxForVerification();
        assertTrue(pointBrowserURL.contains("https"), "Verification mail has failed to reach Gmail inbox");
        EmailUtil.browserRedirectionOnVerification(pointBrowserURL, loginURL,
                automationContext.getContextTenant().getContextUser().getUserName(),
                automationContext.getContextTenant().getContextUser().getPassword());
    }

    @DataProvider
    private static TestUserMode[][] userModeProvider() {
        return new TestUserMode[][]{
                new TestUserMode[]{TestUserMode.SUPER_TENANT_ADMIN}
//                new TestUserMode[]{TestUserMode.TENANT_USER},
        };
    }
}

