/*
*Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.es.publisher.crud;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wink.client.ClientResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.*;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.es.utils.ESTestCommonUtils;
import org.wso2.carbon.registry.es.utils.GregESTestBaseTest;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GenericRestClient;

import javax.activation.DataHandler;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class CustomAssetCRUDTestCase extends GregESTestBaseTest {
    private static final Log log = LogFactory.getLog(CustomAssetCRUDTestCase.class);
    private TestUserMode userMode;
    String jSessionId;
    String assetId;
    String assetName;
    String cookieHeader;
    GenericRestClient genericRestClient;
    Map<String, String> headerMap;
    String publisherUrl;
    String resourcePath;
    ESTestCommonUtils esTestCommonUtils;

    @Factory(dataProvider = "userModeProvider")
    public CustomAssetCRUDTestCase(TestUserMode userMode) {
        this.userMode = userMode;
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(userMode);
        genericRestClient = new GenericRestClient();
        headerMap = new HashMap<>();
        resourcePath = FrameworkPathUtil.getSystemResourceLocation()
                + "artifacts" + File.separator + "GREG" + File.separator;
        publisherUrl = automationContext.getContextUrls()
                .getSecureServiceUrl().replace("services", "publisher/apis");
        setTestEnvironment();
    }

    private void doAdminConfigurations() throws Exception {
        String session = getSessionCookie();
        ResourceAdminServiceClient resourceAdminServiceClient = new ResourceAdminServiceClient(backendURL, session);
        String filePath = getTestArtifactLocation() + "artifacts" + File.separator +
                "GREG" + File.separator + "rxt" + File.separator + "application.rxt";
        DataHandler dh = new DataHandler(new URL("file:///" + filePath));
        resourceAdminServiceClient.addResource(
                "/_system/governance/repository/components/org.wso2.carbon.governance/types/application.rxt",
                "application/vnd.wso2.registry-ext-type+xml", "desc", dh);
    }

    private void setTestEnvironment() throws Exception {
        doAdminConfigurations();
        JSONObject objSessionPublisher =
                new JSONObject(authenticate(publisherUrl, genericRestClient,
                        automationContext.getSuperTenant().getTenantAdmin().getUserName(),
                        automationContext.getSuperTenant().getTenantAdmin().getPassword())
                        .getEntity(String.class));
        jSessionId = objSessionPublisher.getJSONObject("data").getString("sessionId");
        cookieHeader = "JSESSIONID=" + jSessionId;
        //refresh the publisher landing page to deploy new rxt type
        refreshPublisherLandingPage();
        Assert.assertNotNull(jSessionId, "Invalid JSessionID received");
        esTestCommonUtils = new ESTestCommonUtils(genericRestClient, publisherUrl, headerMap);
        esTestCommonUtils.setCookieHeader(cookieHeader);
    }

    /**
     * Need to refresh the landing page to deploy the new rxt in publisher
     */
    private void refreshPublisherLandingPage() {
        Map<String, String> queryParamMap = new HashMap<>();
        String landingUrl = publisherUrl.replace("apis", "pages/gc-landing");
        genericRestClient.geneticRestRequestGet(landingUrl, queryParamMap, headerMap, cookieHeader);
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Create Rest Service in Publisher")
    public void createCustomAsset() throws JSONException, IOException {
        Map<String, String> queryParamMap = new HashMap<>();
        queryParamMap.put("type", "applications");
        String dataBody = readFile(resourcePath + "json" + File.separator + "custom-applications-sample.json");
        assetName = (String) (new JSONObject(dataBody)).get("overview_name");
        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/assets",
                        MediaType.APPLICATION_JSON,
                        MediaType.APPLICATION_JSON, dataBody
                        , queryParamMap, headerMap, cookieHeader);
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        Assert.assertTrue((response.getStatusCode() == 201),
                "Wrong status code ,Expected 201 Created ,Received " +
                        response.getStatusCode());
        assetId = obj.get("id").toString();
        Assert.assertNotNull(assetId, "Empty asset resource id available" +
                response.getEntity(String.class));
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Get Rest Service in Publisher",
            dependsOnMethods = {"createCustomAsset"})
    public void getCustomAsset() throws JSONException {
        Map<String, String> queryParamMap = new HashMap<>();
        queryParamMap.put("type", "applications");
        ClientResponse clientResponse = esTestCommonUtils.getAssetById(assetId, queryParamMap);
        Assert.assertTrue((clientResponse.getStatusCode() == 200),
                "Wrong status code ,Expected 200 OK " +
                        clientResponse.getStatusCode());
        JSONObject obj = new JSONObject(clientResponse.getEntity(String.class));
        Assert.assertEquals(obj.get("id").toString(), assetId);
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Search Rest Service in Publisher",
            dependsOnMethods = {"createCustomAsset"})
    public void searchCustomAsset() throws JSONException {
        boolean assetFound = false;
        Map<String, String> queryParamMap = new HashMap<>();
        queryParamMap.put("type", "applications");
        queryParamMap.put("overview_name", assetName);
        ClientResponse clientResponse = esTestCommonUtils.searchAssetByQuery(queryParamMap);
        JSONObject obj = new JSONObject(clientResponse.getEntity(String.class));
        JSONArray jsonArray = obj.getJSONArray("list");
        for (int i = 0; i < jsonArray.length(); i++) {
            String id = (String) jsonArray.getJSONObject(i).get("id");
            if (assetId.equals(id)) {
                assetFound = true;
                break;
            }
        }
        Assert.assertTrue(assetFound, "Rest Service not found in assets listing");
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Search Rest Service in Publisher",
            dependsOnMethods = {"getCustomAsset"})
    public void updateCustomAsset() throws JSONException, IOException {
        Map<String, String> queryParamMap = new HashMap<>();
        queryParamMap.put("type", "applications");
        String dataBody = readFile(resourcePath + "json" + File.separator + "custom-applications-update-sample.json");
        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/assets/" + assetId,
                        MediaType.APPLICATION_JSON,
                        MediaType.APPLICATION_JSON, dataBody
                        , queryParamMap, headerMap, cookieHeader);
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        Assert.assertTrue((response.getStatusCode() == 202),
                "Wrong status code ,Expected 202 Created ,Received " +
                        response.getStatusCode());
        Assert.assertTrue(obj.getJSONObject("attributes").get("overview_description")
                .equals("Test update asset"));
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Delete Rest Service in Publisher",
            dependsOnMethods = {"getCustomAsset", "searchCustomAsset", "updateCustomAsset"})
    public void deleteCustomAsset() throws JSONException {
        Map<String, String> queryParamMap = new HashMap<>();
        queryParamMap.put("type", "applications");
        genericRestClient.geneticRestRequestDelete(publisherUrl + "/assets/" + assetId,
                MediaType.APPLICATION_JSON,
                MediaType.APPLICATION_JSON
                , queryParamMap, headerMap, cookieHeader);
        ClientResponse clientResponse = esTestCommonUtils.getAssetById(assetId, queryParamMap);
        Assert.assertTrue((clientResponse.getStatusCode() == 404),
                "Wrong status code ,Expected 404 Not Found " +
                        clientResponse.getStatusCode());
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws Exception {
        Map<String, String> queryParamMap = new HashMap<>();
        queryParamMap.put("type", "applications");
        esTestCommonUtils.deleteAllAssociationsById(assetId, queryParamMap);
        esTestCommonUtils.deleteAssetById(assetId, queryParamMap);
        this.deleteCustomRxt();
    }

    private void deleteCustomRxt() throws Exception {
        String session = getSessionCookie();
        ResourceAdminServiceClient resourceAdminServiceClient = new ResourceAdminServiceClient(backendURL, session);
        resourceAdminServiceClient.deleteResource(
                "/_system/governance/repository/components/org.wso2.carbon.governance/types/application.rxt");
    }

    @DataProvider
    private static TestUserMode[][] userModeProvider() {
        return new TestUserMode[][]{
                new TestUserMode[]{TestUserMode.SUPER_TENANT_ADMIN}
//                new TestUserMode[]{TestUserMode.TENANT_USER},
        };
    }
}