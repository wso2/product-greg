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
package org.wso2.carbon.registry.es.tags;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wink.client.ClientResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.es.utils.GregESTestBaseTest;
import org.wso2.greg.integration.common.utils.GenericRestClient;

import javax.ws.rs.core.MediaType;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
public class GregAssetsTagsESTestCase extends GregESTestBaseTest {
    private static final Log log = LogFactory.getLog(GregAssetsTagsESTestCase.class);
    private TestUserMode userMode;
    String jSessionId;
    String assetId;
    String assetType;
    String cookieHeaderPublisher;
    String cookieHeaderStore;
    GenericRestClient genericRestClient;
    Map<String, String> queryParamMap;
    Map<String, String> headerMap;
    String publisherUrl;
    String storeUrl;
    String resourcePath;

    @Factory(dataProvider = "userModeProvider")
    public GregAssetsTagsESTestCase(TestUserMode userMode) {
        this.userMode = userMode;
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(userMode);
        assetType = "restservice";
        genericRestClient = new GenericRestClient();
        queryParamMap = new HashMap<String, String>();
        headerMap = new HashMap<String, String>();
        resourcePath = FrameworkPathUtil.getSystemResourceLocation()
                       + "artifacts" + File.separator + "GREG" + File.separator + "json" + File.separator
                       + "publisherPublishRestResource.json";
        publisherUrl = publisherContext.getContextUrls()
                .getSecureServiceUrl().replace("services", "publisher/apis");
        storeUrl = storeContext.getContextUrls()
                .getSecureServiceUrl().replace("services", "store/apis");
        SetTestEnvironment();
    }


    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Add tag to asset test")
    public void addTagsToAsset() throws JSONException {
        queryParamMap.put("type", "restservice");
        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/asset/" + assetId + "/add-tags",
                                                         MediaType.APPLICATION_JSON,
                                                         MediaType.APPLICATION_JSON,
                                                         "{\"tags\": \"testTag\"}"
                        , queryParamMap, headerMap, cookieHeaderPublisher);
        Assert.assertTrue(response.getEntity(String.class).contains("data"));
        Assert.assertTrue((response.getStatusCode() == 200),
                          "Wrong status code ,Expected 200 OK ,Received " +
                          response.getStatusCode());
       // isTagAvailablePublisher("testTag");
    }
    //https://localhost:9443/publisher/apis/asset/d0733230-7d98-42c0-95c5-75eecebd0f46/remove-tags?type=restservice


    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Search for asset from tag in store",
            dependsOnMethods = {"addTagsToAsset"})
    public void searchAssetByTagInStore() throws JSONException {
        queryParamMap.put("type", "restservice");
        queryParamMap.put("q", "\"tags" + "\":" + "\"" + "testTag" + "\"");
        ClientResponse response =
                genericRestClient.geneticRestRequestGet(storeUrl + "/assets", queryParamMap
                        , headerMap, cookieHeaderStore);
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        Assert.assertTrue((response.getStatusCode() == 200),
                          "Wrong status code ,Expected 200 OK ,Received " +
                          response.getStatusCode());
    }

   /* @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Authenticate Publisher test"
    ,dependsOnMethods = {"addTagsToAsset","searchAssetByTagInStore"} )
    public void removeTagsFromAsset() throws JSONException {
        queryParamMap.put("type", "restservice");
        ClientResponse response =
                genericRestClient.geneticRestRequestDelete(publisherUrl + "/asset/" + assetId + "/remove-tags",
                                                         MediaType.APPLICATION_JSON,
                                                         MediaType.APPLICATION_JSON,
                                                         "{\"tags\": \"testTag\"}"
                        , queryParamMap, headerMap, cookieHeaderPublisher);
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        Assert.assertTrue((response.getStatusCode() == 200),
                          "Wrong status code ,Expected 200 OK ,Received " +
                          response.getStatusCode());
       // isTagAvailablePublisher("testTag");
    }
*/
    @AfterClass(alwaysRun = true)
    public void cleanUp() throws RegistryException {

        deleteAsset(assetId, publisherUrl, cookieHeaderPublisher, assetType, genericRestClient);
    }

    private void SetTestEnvironment() throws JSONException, XPathExpressionException, IOException {
        JSONObject objSessionPublisher =
                new JSONObject(authenticate(publisherUrl, genericRestClient,
                                            automationContext.getSuperTenant().getTenantAdmin().getUserName(),
                                            automationContext.getSuperTenant().getTenantAdmin().getPassword())
                                       .getEntity(String.class));
        jSessionId = objSessionPublisher.getJSONObject("data").getString("sessionId");
        cookieHeaderPublisher = "JSESSIONID=" + jSessionId;
        JSONObject objSessionStore =
                new JSONObject(authenticate(publisherUrl, genericRestClient,
                                            automationContext.getSuperTenant().getTenantAdmin().getUserName(),
                                            automationContext.getSuperTenant().getTenantAdmin().getPassword())
                                       .getEntity(String.class));
        jSessionId = objSessionStore.getJSONObject("data").getString("sessionId");
        cookieHeaderStore = "JSESSIONID=" + jSessionId;
        JSONObject objSessionId = new JSONObject(createAsset(resourcePath, publisherUrl,
                                                             cookieHeaderPublisher, assetType,
                                                             genericRestClient).getEntity(String.class));
        assetId = objSessionId.get("id").toString();
    }

    private boolean isTagAvailablePublisher(String tagName) throws JSONException {
       boolean isTagAvailable = false;

        queryParamMap.put("type", "restservice");
        queryParamMap.put("q",String.format("\"name\":\"%s\"",tagName));
        ClientResponse response =
                genericRestClient.geneticRestRequestGet(publisherUrl + "/tags", queryParamMap
                        , headerMap, cookieHeaderPublisher);
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        Assert.assertTrue((response.getStatusCode() == 200),
                          "Wrong status code ,Expected 200 OK ,Received " +
                          response.getStatusCode());

        return isTagAvailable;
    }

    @DataProvider
    private static TestUserMode[][] userModeProvider() {
        return new TestUserMode[][]{
                new TestUserMode[]{TestUserMode.SUPER_TENANT_ADMIN},
//                new TestUserMode[]{TestUserMode.TENANT_USER},
        };
    }

}
