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
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.es.utils.ESTestCommonUtils;
import org.wso2.carbon.registry.es.utils.GregESTestBaseTest;
import org.wso2.greg.integration.common.utils.GenericRestClient;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class WadlCRUDTestCase extends GregESTestBaseTest {
    private static final Log log = LogFactory.getLog(WadlCRUDTestCase.class);
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
    public WadlCRUDTestCase(TestUserMode userMode) {
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
        esTestCommonUtils = new ESTestCommonUtils(genericRestClient, publisherUrl, headerMap);
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Authenticate Publisher test")
    public void authenticatePublisher() throws JSONException {
        Map<String, String> queryParamMap = new HashMap<>();
        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl+"/authenticate/",
                        MediaType.APPLICATION_FORM_URLENCODED,
                        MediaType.APPLICATION_JSON,
                        "username=admin&password=admin"
                        , queryParamMap, headerMap, null);
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        Assert.assertTrue((response.getStatusCode() == 200),
                "Wrong status code ,Expected 200 OK ,Received " +
                        response.getStatusCode());
        jSessionId = obj.getJSONObject("data").getString("sessionId");
        cookieHeader="JSESSIONID=" + jSessionId;
        Assert.assertNotNull(jSessionId, "Invalid JSessionID received");
        esTestCommonUtils.setCookieHeader(cookieHeader);
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Create Rest Service in Publisher",
            dependsOnMethods = {"authenticatePublisher"})
    public void createWadlServiceAsset() throws JSONException, IOException {
        Map<String, String> queryParamMap = new HashMap<>();
        queryParamMap.put("type", "wadl");
        String dataBody = readFile(resourcePath+"json"+ File.separator+"wadl-sample.json");
        assetName = (String)(new JSONObject(dataBody)).get("overview_name");
        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl+"/assets",
                        MediaType.APPLICATION_JSON,
                        MediaType.APPLICATION_JSON, dataBody
                        , queryParamMap, headerMap, cookieHeader);
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        Assert.assertTrue((response.getStatusCode() == 201),
                "Wrong status code ,Expected 201 Created ,Received " +
                        response.getStatusCode());
        String resultName = obj.get("overview_name").toString();
        Assert.assertEquals(resultName,assetName);
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Create Rest Service in Publisher",
            dependsOnMethods = {"authenticatePublisher", "createWadlServiceAsset"})
    public void searchWadlAsset() throws JSONException {
        boolean assetFound = false;
        Map<String, String> queryParamMap = new HashMap<>();
        queryParamMap.put("type", "wadl");
        queryParamMap.put("overview_name", assetName);
        ClientResponse clientResponse = esTestCommonUtils.searchAssetByQuery(queryParamMap);
        JSONObject obj = new JSONObject(clientResponse.getEntity(String.class));
        JSONArray jsonArray = obj.getJSONArray("list");
        for (int i = 0; i < jsonArray.length(); i++) {
            String name = (String)jsonArray.getJSONObject(i).get("name");
            if (assetName.equals(name)) {
                assetFound = true;
                assetId = (String)jsonArray.getJSONObject(i).get("id");
                break;
            }
        }
        Assert.assertEquals(assetFound,true);
        Assert.assertNotNull(assetId, "Empty asset resource id available");
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Create Rest Service in Publisher",
            dependsOnMethods = {"authenticatePublisher", "createWadlServiceAsset", "searchWadlAsset"})
    public void getWadlAsset() throws JSONException {
        Map<String, String> queryParamMap = new HashMap<>();
        queryParamMap.put("type", "wadl");
        ClientResponse clientResponse = esTestCommonUtils.getAssetById(assetId, queryParamMap);
        Assert.assertTrue((clientResponse.getStatusCode() == 200),
                "Wrong status code ,Expected 200 OK " +
                        clientResponse.getStatusCode());
        JSONObject obj = new JSONObject(clientResponse.getEntity(String.class));
        Assert.assertEquals(obj.get("id").toString(), assetId);
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Delete Publisher test",
            dependsOnMethods = {"authenticatePublisher", "createWadlServiceAsset", "searchWadlAsset", "getWadlAsset"})
    public void deleteWadlAsset() throws JSONException {
        Map<String, String> queryParamMap = new HashMap<>();
        queryParamMap.put("type", "wadl");
        genericRestClient.geneticRestRequestDelete(publisherUrl + "/assets/" + assetId,
                MediaType.APPLICATION_JSON,
                MediaType.APPLICATION_JSON
                , queryParamMap, headerMap, cookieHeader);
        ClientResponse clientResponse = esTestCommonUtils.getAssetById(assetId, queryParamMap);
        JSONObject obj = new JSONObject(clientResponse.getEntity(String.class));
        Assert.assertTrue((clientResponse.getStatusCode() == 404),
                "Wrong status code ,Expected 404 Not Found " +
                        clientResponse.getStatusCode());
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws RegistryException {

    }

    @DataProvider
    private static TestUserMode[][] userModeProvider() {
        return new TestUserMode[][]{
                new TestUserMode[]{TestUserMode.SUPER_TENANT_ADMIN}
//                new TestUserMode[]{TestUserMode.TENANT_USER},
        };
    }
}
