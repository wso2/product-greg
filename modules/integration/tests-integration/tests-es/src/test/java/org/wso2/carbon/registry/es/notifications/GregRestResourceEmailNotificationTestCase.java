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

/*package org.wso2.carbon.registry.es.notifications;

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
import org.wso2.greg.integration.common.clients.UserProfileMgtServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.GenericRestClient;

import javax.mail.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.testng.Assert.*;

/**
 * This test class can be used to check the email notification functionality
 */
/*public class GregRestResourceEmailNotificationTestCase extends GREGIntegrationBaseTest {

    private TestUserMode userMode;
    private UserProfileMgtServiceClient userProfileMgtClient;
    private File axis2File;
    private String publisherUrl;
    private String resourcePath;
    private String assetId;
    private String cookieHeader;
    private String pointBrowserURL;
    private GenericRestClient genericRestClient;
    private Map<String, String> queryParamMap;
    private Map<String, String> headerMap;
    private String loginURL;
    private String emailAddress;
    private static char[] emailPassword;

    boolean isVerificationMailAvailable;
    boolean isResourceUpdatedMailAvailable;


    @Factory(dataProvider = "userModeProvider")
    public GregRestResourceEmailNotificationTestCase(TestUserMode userMode) {
        this.userMode = userMode;
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.init(userMode);
        loginURL = UrlGenerationUtil.getLoginURL(automationContext.getInstance());
        emailAddress = "testingwso2@gmail.com";
        emailPassword = new char[]{'T','e','s','t','i','n','g','w','s','o','2','1','2','3'};
        genericRestClient = new GenericRestClient();
        queryParamMap = new HashMap<>();
        headerMap = new HashMap<>();
        resourcePath = FrameworkPathUtil.getSystemResourceLocation()
                + "artifacts" + File.separator + "GREG" + File.separator;
        publisherUrl = automationContext.getContextUrls()
                .getSecureServiceUrl().replace("services", "publisher/apis");
        userProfileMgtClient = new UserProfileMgtServiceClient(backendURL, sessionCookie);
        axis2File = new File(TestConfigurationProvider.getResourceLocation("GREG")
                + File.separator + "axis2" + File.separator
                + "axis2.xml");
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws RegistryException {

    }

    @Test(groups = "wso2.greg", description = "Updating the default user profile and configure axis2.xml file")
    public void updateProfileAndEnableEmailConfiguration() throws UserProfileMgtServiceUserProfileExceptionException,
            IOException, XPathExpressionException, AutomationUtilException {

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

        userProfileMgtClient.setUserProfile(automationContext.getContextTenant().getContextUser().getUserName(), profile);

        // apply new axis2.xml configuration
        ServerConfigurationManager serverConfigurationManager =
                new ServerConfigurationManager(automationContext);
        serverConfigurationManager.applyConfiguration(axis2File);
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Authenticate Publisher test",
            dependsOnMethods = "updateProfileAndEnableEmailConfiguration")
    public void authenticatePublisher() throws JSONException, XPathExpressionException {
        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/authenticate/",
                        MediaType.APPLICATION_FORM_URLENCODED,
                        MediaType.APPLICATION_JSON,
                        "username=" + automationContext.getContextTenant().getContextUser().getUserName() +
                                "&password=" + automationContext.getContextTenant().getContextUser().getPassword()
                        , queryParamMap, headerMap, null
                );
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        assertTrue((response.getStatusCode() == Response.Status.OK.getStatusCode()),
                "Wrong status code ,Expected 200 OK ,Received " +
                        response.getStatusCode()
        );
        String jSessionId = obj.getJSONObject("data").getString("sessionId");
        cookieHeader = "JSESSIONID=" + jSessionId;
        assertNotNull(jSessionId, "Invalid JSessionID received");
    }


    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Create Test Rest Service",
            dependsOnMethods = {"authenticatePublisher"})
    public void createTestRestServices() throws JSONException, IOException {
        queryParamMap.put("type", "restservice");
        String dataBody = readFile(resourcePath + "json" + File.separator + "publisherPublishRestResource.json");
        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/assets",
                        MediaType.APPLICATION_JSON,
                        MediaType.APPLICATION_JSON, dataBody
                        , queryParamMap, headerMap, cookieHeader);
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        assertTrue((response.getStatusCode() == Response.Status.CREATED.getStatusCode()),
                "Wrong status code ,Expected 201 Created ,Received " +
                        response.getStatusCode()
        );
        assetId = obj.get("id").toString();
        assertNotNull(assetId, "Empty asset resource id available" +
                response.getEntity(String.class));
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Adding subscription to rest service",
            dependsOnMethods = {"createTestRestServices"})
    public void addSubscription() throws Exception {

        JSONObject dataObject = new JSONObject();

        dataObject.put("notificationType", "PublisherResourceUpdated");
        dataObject.put("notificationMethod", "email");

        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/subscriptions/restservice/"
                                + assetId, MediaType.APPLICATION_JSON,
                        MediaType.APPLICATION_JSON, dataObject.toString()
                        , queryParamMap, headerMap, cookieHeader
                );

        assertTrue((response.getStatusCode() == Response.Status.OK.getStatusCode()),
                "Wrong status code ,Expected" + Response.Status.OK.getStatusCode() + "Created ,Received " +
                        response.getStatusCode()
        );
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Update Test Rest Service",
            dependsOnMethods = {"addSubscription"})
    public void emailConfirmationVerification() throws Exception {

        readGmailInbox();

        Map<String, String> newQueryParamMap = new HashMap<>();

        newQueryParamMap.put(pointBrowserURL.split("\\?")[1].split("=")[0],
                pointBrowserURL.split("\\?")[1].split("=")[1]);

        ClientResponse verificationUrlResponse = genericRestClient.geneticRestRequestGet
                (String.format(pointBrowserURL.split("\\?")[0]), newQueryParamMap, headerMap, cookieHeader);

        assertEquals(verificationUrlResponse.getStatusCode(), 200,
                "Response mismatch should be 302 but received " + verificationUrlResponse.getStatusCode());

        assertTrue(isVerificationMailAvailable, "Verification Mail Has Failed To Reached Gmail Inbox");

        newQueryParamMap.clear();

        ClientResponse loginResponse =
                genericRestClient.geneticRestRequestPost(loginURL + "admin/login.jsp",
                        MediaType.APPLICATION_FORM_URLENCODED,
                        MediaType.TEXT_HTML, "username=" +
                                automationContext.getContextTenant().getContextUser().getUserName() +
                                "&password=" + automationContext.getContextTenant().getContextUser().getPassword()
                        , queryParamMap, headerMap, cookieHeader
                );

        assertEquals(loginResponse.getStatusCode(), Response.Status.OK.getStatusCode(),
                "Response mismatch should be 200 but received " + loginResponse.getStatusCode());

        GenericRestClient redirectClient = new GenericRestClient(false);

        ClientResponse reDirectionResponse =
                redirectClient.geneticRestRequestPost(loginURL + "admin/login_action.jsp",
                        MediaType.APPLICATION_FORM_URLENCODED,
                        MediaType.TEXT_HTML, "username=" +
                                automationContext.getContextTenant().getContextUser().getUserName() +
                                "&password=" + automationContext.getContextTenant().getContextUser().getPassword()
                        , queryParamMap, headerMap, cookieHeader
                );

        assertEquals(reDirectionResponse.getStatusCode(), 302,
                "Response mismatch should be 302 but received " + reDirectionResponse.getStatusCode());

        String newRedirectionUrl = locationHeader(reDirectionResponse);

        assertTrue(newRedirectionUrl.contains("loginStatus=true"), "Response does not contain expected state " +
                "loginStatus=true");

        newQueryParamMap.clear();

        ClientResponse newResponse = genericRestClient.geneticRestRequestGet
                (newRedirectionUrl, newQueryParamMap, headerMap, cookieHeader);

        assertEquals(newResponse.getStatusCode(), Response.Status.OK.getStatusCode(),
                "Response mismatch should be 200 but received " + newResponse.getStatusCode());


        newQueryParamMap.clear();


        //TODO - Since genericRestClient does not yield the desired response
        newQueryParamMap.put("confirmation", pointBrowserURL.split("confirmation=")[1].split("&")[0]);

        ClientResponse verificationConfirmationResponse = genericRestClient.geneticRestRequestGet
                (loginURL + "email-verification/validator_ajaxprocessor.jsp"
                        , newQueryParamMap, headerMap, cookieHeader);

        assertEquals(verificationConfirmationResponse.getStatusCode(), Response.Status.OK.getStatusCode(),
                "Response mismatch should be 200 but received " + verificationUrlResponse.getStatusCode());

        ClientResponse confirmationSuccessResponse = genericRestClient.geneticRestRequestGet
                (newRedirectionUrl, newQueryParamMap, headerMap, cookieHeader);


        assertEquals(confirmationSuccessResponse.getStatusCode(), Response.Status.OK.getStatusCode(),
                "Response mismatch should be 200 but received " + verificationUrlResponse.getStatusCode());

    }


    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Update Test Rest Service",
            dependsOnMethods = {"emailConfirmationVerification"})
    public void updateRestService() throws Exception {

        queryParamMap.put("type", "restservice");
        String dataBody = readFile(resourcePath + "json" + File.separator + "PublisherRestResourceUpdate.json");
        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/assets/" + assetId,
                        MediaType.APPLICATION_JSON,
                        MediaType.APPLICATION_JSON, dataBody
                        , queryParamMap, headerMap, cookieHeader);
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        assertTrue((response.getStatusCode() == Response.Status.ACCEPTED.getStatusCode()),
                "Wrong status code ,Expected 202 Created ,Received " +
                        response.getStatusCode()
        );
        assertTrue(obj.getJSONObject("attributes").get("overview_context")
                .equals("/changed/Context"));

        Thread.sleep(5000);

        readGmailInbox();

        assertTrue(isResourceUpdatedMailAvailable, "Publisher Resource Updated Mail Has Failed To Reached Gmail Inbox");
    }

    private void readGmailInbox() throws Exception {

        Properties props = new Properties();

        props.load(new FileInputStream(new File(TestConfigurationProvider.getResourceLocation("GREG")
                + File.separator + "axis2" + File.separator
                + "smtp.properties")));
        Session session = Session.getDefaultInstance(props, null);

        Store store = session.getStore("imaps");

        store.connect("smtp.gmail.com", emailAddress, java.nio.CharBuffer.wrap(emailPassword).toString());

        Folder inbox = store.getFolder("inbox");
        inbox.open(Folder.READ_WRITE);

        Thread.sleep(5000);

        int messageCount = inbox.getMessageCount();

        log.info("Total Messages:- " + messageCount);

        Message[] messages = inbox.getMessages();

        for (Message message : messages) {
            log.info("Mail Subject:- " + message.getSubject());
            if (message.getSubject().contains("EmailVerification")) {
                isVerificationMailAvailable = true;
                pointBrowserURL = getBodyFromMessage(message);

                // Optional : deleting the inbox verification mail
                message.setFlag(Flags.Flag.DELETED, true);
            }
            if (message.getSubject().contains("PublisherResourceUpdated")) {
                isResourceUpdatedMailAvailable = true;

                // Optional : deleting the inbox resource updated mail
                message.setFlag(Flags.Flag.DELETED, true);
            }

        }
        inbox.close(true);
        store.close();
    }

    private String getBodyFromMessage(Message message) throws IOException, MessagingException {
        if (message.isMimeType("text/plain")) {
            String[] arr = message.getContent().toString().split("\\r?\\n");
            for (int x = 0; x <= arr.length; x++) {
                if (arr[x].contains("https://")) {
                    return arr[x];
                }
            }

        }
        return "";
    }

    private String locationHeader(ClientResponse response) {

        MultivaluedMap<String, String> headers = response.getHeaders();


        for (int x = 0; x <= headers.size(); x++) {
            if (headers.containsKey("Location")) {
                return headers.get("Location").get(0);
            }
        }
        return "";
    }

    @DataProvider
    private static TestUserMode[][] userModeProvider() {
        return new TestUserMode[][]{
                new TestUserMode[]{TestUserMode.SUPER_TENANT_ADMIN}
//                new TestUserMode[]{TestUserMode.TENANT_USER},
        };
    }
}
*/