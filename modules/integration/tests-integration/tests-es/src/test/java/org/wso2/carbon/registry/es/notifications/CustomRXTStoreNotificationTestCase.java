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
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.es.utils.GregESTestBaseTest;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
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

/**
 * This class test subscription & notification for custom rxt type at store.
 */
public class CustomRXTStoreNotificationTestCase extends GregESTestBaseTest {

    private static final Log log = LogFactory.getLog(CustomRXTStoreNotificationTestCase.class);

    private TestUserMode userMode;
    String jSessionIdPublisher;
    String jSessionIdStore;
    String assetId;
    String cookieHeaderPublisher;
    String cookieHeaderStore;
    GenericRestClient genericRestClient;
    Map<String, String> queryParamMap;
    Map<String, String> headerMap;
    String publisherUrl;
    String storeUrl;
    String resourcePath;
    private ResourceAdminServiceClient resourceAdminServiceClient;

    @Factory(dataProvider = "userModeProvider")
    public CustomRXTStoreNotificationTestCase(TestUserMode userMode) {
        this.userMode = userMode;
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(userMode);
        String session = getSessionCookie();
        genericRestClient = new GenericRestClient();
        queryParamMap = new HashMap<String, String>();
        headerMap = new HashMap<String, String>();
        resourcePath =
                FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator + "GREG" + File.separator;
        publisherUrl = automationContext.getContextUrls().getSecureServiceUrl().replace("services", "publisher/apis");
        storeUrl = automationContext.getContextUrls().getSecureServiceUrl().replace("services", "store/apis");
        resourceAdminServiceClient = new ResourceAdminServiceClient(backendURL, session);
        addCustomRxt();
        setTestEnvironment();
    }

    @Test(groups = { "wso2.greg",
            "wso2.greg.es" }, description = "Adding subscription to custom asset on LC state change")
    public void addSubscriptionToLcStateChange() throws JSONException, IOException {
        JSONObject dataObject = new JSONObject();
        dataObject.put("notificationType", "StoreLifeCycleStateChanged");
        dataObject.put("notificationMethod", "work");

        ClientResponse response = genericRestClient
                .geneticRestRequestPost(storeUrl + "/subscription/applications/" + assetId, MediaType.APPLICATION_JSON,
                        MediaType.APPLICATION_JSON, dataObject.toString(), queryParamMap, headerMap, cookieHeaderStore);

        String payLoad = response.getEntity(String.class);
        payLoad = payLoad.substring(payLoad.indexOf('{'));
        JSONObject obj = new JSONObject(payLoad);
        assertNotNull(obj.get("id").toString(),
                "Response payload is not the in the correct format" + response.getEntity(String.class));

        genericRestClient.geneticRestRequestPost(publisherUrl + "/assets/" + assetId + "/state",
                MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_JSON,
                "nextState=Testing&comment=Completed", queryParamMap, headerMap, cookieHeaderPublisher);
        // TODO - Since notification not appearing in the store
    }

    @Test(groups = { "wso2.greg",
            "wso2.greg.es" }, description = "Adding subscription to custom asset on resource update")
    public void addSubscriptionToResourceUpdate() throws JSONException, IOException {
        JSONObject dataObject = new JSONObject();
        dataObject.put("notificationType", "StoreResourceUpdated");
        dataObject.put("notificationMethod", "work");

        ClientResponse response = genericRestClient
                .geneticRestRequestPost(storeUrl + "/subscription/applications/" + assetId, MediaType.APPLICATION_JSON,
                        MediaType.APPLICATION_JSON, dataObject.toString(), queryParamMap, headerMap, cookieHeaderStore);

        String payLoad = response.getEntity(String.class);
        payLoad = payLoad.substring(payLoad.indexOf('{'));
        JSONObject obj = new JSONObject(payLoad);
        assertNotNull(obj.get("id").toString(),
                "Response payload is not the in the correct format" + response.getEntity(String.class));

        queryParamMap.put("type", "applications");
        String dataBody = readFile(resourcePath + "json" + File.separator + "PublisherCustomResourceUpdate.json");
        genericRestClient.geneticRestRequestPost(publisherUrl + "/assets/" + assetId, MediaType.APPLICATION_JSON,
                MediaType.APPLICATION_JSON, dataBody, queryParamMap, headerMap, cookieHeaderPublisher);
        // TODO - Since notification not appearing in the store
    }

    @Test(groups = { "wso2.greg",
            "wso2.greg.es" }, description = "Adding wrong subscription method to check the error message")
    public void addWrongSubscriptionMethod() throws JSONException, IOException {

        JSONObject dataObject = new JSONObject();
        dataObject.put("notificationType", "StoreResourceUpdated");
        dataObject.put("notificationMethod", "test");

        ClientResponse response = genericRestClient
                .geneticRestRequestPost(storeUrl + "/subscription/applications/" + assetId,
                        MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, dataObject.toString(), queryParamMap,
                        headerMap, cookieHeaderStore);

        String payLoad = response.getEntity(String.class);
        payLoad = payLoad.substring(payLoad.indexOf('{'));
        JSONObject obj = new JSONObject(payLoad);
        assertNotNull(obj.get("error").toString(),
                "Error message is not contained in the response for notification method \"test\"" + response
                        .getEntity(String.class));
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

    private void deleteCustomAsset() throws JSONException {
        genericRestClient.geneticRestRequestDelete(publisherUrl + "/assets/" + assetId, MediaType.APPLICATION_JSON,
                MediaType.APPLICATION_JSON, queryParamMap, headerMap, cookieHeaderPublisher);

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

        //Create rest service
        queryParamMap.put("type", "applications");
        String dataBody = readFile(resourcePath + "json" + File.separator + "publisherPublishCustomResource.json");
        ClientResponse createResponse = genericRestClient
                .geneticRestRequestPost(publisherUrl + "/assets", MediaType.APPLICATION_JSON,
                        MediaType.APPLICATION_JSON, dataBody, queryParamMap, headerMap, cookieHeaderPublisher);
        JSONObject createObj = new JSONObject(createResponse.getEntity(String.class));
        assetId = createObj.get("id").toString();
    }

    @AfterClass(alwaysRun = true)
    public void clean() throws Exception {
        deleteCustomAsset();
        deleteCustomRxt();
    }

    @DataProvider
    private static TestUserMode[][] userModeProvider() {
        return new TestUserMode[][]{
                new TestUserMode[]{TestUserMode.SUPER_TENANT_ADMIN}
                //                new TestUserMode[]{TestUserMode.TENANT_USER},
        };
    }
}
