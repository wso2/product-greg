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
import org.wso2.carbon.registry.es.utils.GregESTestBaseTest;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.clients.UserProfileMgtServiceClient;
import org.wso2.greg.integration.common.utils.GenericRestClient;

import javax.activation.DataHandler;
import javax.ws.rs.core.MediaType;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * This test class can be used to check the email notification functionality on store side for custom asset type.
 */
public class CustomRXTStoreEmailNotificationTestCase extends GregESTestBaseTest {

    private TestUserMode userMode;
    private String jSessionIdPublisher;
    private String jSessionIdStore;
    private UserProfileMgtServiceClient userProfileMgtClient;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private File axis2File;
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
    private String emailAddress;
    private boolean isNotificationMailAvailable;


    @Factory(dataProvider = "userModeProvider")
    public CustomRXTStoreEmailNotificationTestCase(TestUserMode userMode) {
        this.userMode = userMode;
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.init(userMode);
        String session = getSessionCookie();
        loginURL = UrlGenerationUtil.getLoginURL(automationContext.getInstance());
        emailAddress = "gregtestes@gmail.com";
        genericRestClient = new GenericRestClient();
        queryParamMap = new HashMap<>();
        headerMap = new HashMap<>();
        resourcePath =
                FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator + "GREG" + File.separator;
        publisherUrl = automationContext.getContextUrls().getSecureServiceUrl().replace("services", "publisher/apis");
        storeUrl = automationContext.getContextUrls().getSecureServiceUrl().replace("services", "store/apis");
        resourceAdminServiceClient = new ResourceAdminServiceClient(backendURL, session);
        addCustomRxt();
        userProfileMgtClient = new UserProfileMgtServiceClient(backendURL, session);

        queryParamMap.put("type", "applications");
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
            "wso2.greg.es" }, description = "Adding subscription to custom asset on LC state change")
    public void addSubscriptionToLcStateChange() throws Exception {

        JSONObject dataObject = new JSONObject();
        dataObject.put("notificationType", "StoreLifeCycleStateChanged");
        dataObject.put("notificationMethod", "email");
        ClientResponse response = genericRestClient
                .geneticRestRequestPost(storeUrl + "/subscription/applications/" + assetId, MediaType.APPLICATION_JSON,
                        MediaType.APPLICATION_JSON, dataObject.toString(), queryParamMap, headerMap, cookieHeaderStore);

        String payLoad = response.getEntity(String.class);
        payLoad = payLoad.substring(payLoad.indexOf('{'));
        JSONObject obj = new JSONObject(payLoad);
        assertNotNull(obj.get("id").toString(),
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
                "Publisher LC state changed notification mail has failed to reach Gmail inbox");
        isNotificationMailAvailable = false;
    }

    @Test(groups = { "wso2.greg",
            "wso2.greg.es" }, description = "Adding subscription to custom asset on resource update")
    public void addSubscriptionOnResourceUpdate() throws Exception {

        JSONObject dataObject = new JSONObject();
        dataObject.put("notificationType", "StoreResourceUpdated");
        dataObject.put("notificationMethod", "email");

        ClientResponse response = genericRestClient
                .geneticRestRequestPost(storeUrl + "/subscription/applications/" + assetId, MediaType.APPLICATION_JSON,
                        MediaType.APPLICATION_JSON, dataObject.toString(), queryParamMap, headerMap, cookieHeaderStore);

        String payLoad = response.getEntity(String.class);
        payLoad = payLoad.substring(payLoad.indexOf('{'));
        JSONObject obj = new JSONObject(payLoad);
        assertNotNull(obj.get("id").toString(),
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
                MediaType.APPLICATION_JSON, dataBody, queryParamMap, headerMap, cookieHeaderPublisher);

        isNotificationMailAvailable = EmailUtil.readGmailInboxForNotification("StoreResourceUpdated");
        assertTrue(isNotificationMailAvailable, "Publisher resource updated mail has failed to reach Gmail inbox");
        isNotificationMailAvailable = false;
    }

    private void addCustomRxt()
            throws RegistryException, IOException, ResourceAdminServiceExceptionException, InterruptedException {
        String filePath = getTestArtifactLocation() + "artifacts" + File.separator +
                "GREG" + File.separator + "rxt" + File.separator + "application.rxt";
        DataHandler dh = new DataHandler(new URL("file:///" + filePath));
        resourceAdminServiceClient.addResource(
                "/_system/governance/repository/components/org.wso2.carbon.governance/types/application.rxt",
                "application/vnd.wso2.registry-ext-type+xml", "desc", dh);
    }

    private void deleteCustomRxt() throws Exception {
        String session = getSessionCookie();
        resourceAdminServiceClient = new ResourceAdminServiceClient(backendURL, session);
        resourceAdminServiceClient.deleteResource(
                "/_system/governance/repository/components/org.wso2.carbon.governance/types/application.rxt");
    }

    /**
     * Need to refresh the landing page to deploy the new rxt in publisher
     */
    private void refreshPublisherLandingPage() {
        Map<String, String> queryParamMap = new HashMap<>();
        String landingUrl = publisherUrl.replace("apis", "pages/gc-landing");
        genericRestClient.geneticRestRequestGet(landingUrl, queryParamMap, headerMap, cookieHeaderPublisher);
    }

    private void setTestEnvironment() throws JSONException, IOException, XPathExpressionException {
        // Authenticate Publisher
        ClientResponse response = authenticate(publisherUrl, genericRestClient,
                automationContext.getSuperTenant().getTenantAdmin().getUserName(),
                automationContext.getSuperTenant().getTenantAdmin().getPassword());
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        jSessionIdPublisher = obj.getJSONObject("data").getString("sessionId");
        cookieHeaderPublisher = "JSESSIONID=" + jSessionIdPublisher;
        //refresh the publisher landing page to deploy new rxt type
        refreshPublisherLandingPage();

        // Authenticate Store
        ClientResponse responseStore = authenticate(storeUrl, genericRestClient,
                automationContext.getSuperTenant().getTenantAdmin().getUserName(),
                automationContext.getSuperTenant().getTenantAdmin().getPassword());
        obj = new JSONObject(responseStore.getEntity(String.class));
        jSessionIdStore = obj.getJSONObject("data").getString("sessionId");
        cookieHeaderStore = "JSESSIONID=" + jSessionIdStore;

        //Create custom asset
        queryParamMap.put("type", "applications");
        String dataBody = readFile(resourcePath + "json" + File.separator + "publisherPublishCustomResource.json");
        ClientResponse createResponse = genericRestClient
                .geneticRestRequestPost(publisherUrl + "/assets", MediaType.APPLICATION_JSON,
                        MediaType.APPLICATION_JSON, dataBody, queryParamMap, headerMap, cookieHeaderPublisher);
        JSONObject createObj = new JSONObject(createResponse.getEntity(String.class));
        assetId = createObj.get("id").toString();
    }

    private void deleteCustomAsset() throws JSONException {
        genericRestClient.geneticRestRequestDelete(publisherUrl + "/assets/" + assetId, MediaType.APPLICATION_JSON,
                MediaType.APPLICATION_JSON, queryParamMap, headerMap, cookieHeaderPublisher);
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws Exception {
        deleteCustomAsset();
        deleteCustomRxt();
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
