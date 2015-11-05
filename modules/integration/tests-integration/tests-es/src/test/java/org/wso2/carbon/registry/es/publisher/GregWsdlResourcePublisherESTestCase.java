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
package org.wso2.carbon.registry.es.publisher;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wink.client.ClientResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.*;
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

public class GregWsdlResourcePublisherESTestCase extends GregESTestBaseTest {
    private static final Log log = LogFactory.getLog(GregWsdlResourcePublisherESTestCase.class);
    private TestUserMode userMode;
    String jSessionId;
    String assetId;
    String assetType;
    String cookieHeader;
    GenericRestClient genericRestClient;
    Map<String, String> queryParamMap;
    Map<String, String> headerMap;
    String publisherUrl;
    String resourcePath;

    @Factory(dataProvider = "userModeProvider")
    public GregWsdlResourcePublisherESTestCase(TestUserMode userMode) {
        this.userMode = userMode;
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(userMode);
        assetType = "wsdl";
        genericRestClient = new GenericRestClient();
        queryParamMap = new HashMap<String, String>();
        headerMap = new HashMap<String, String>();
        resourcePath = FrameworkPathUtil.getSystemResourceLocation()
                + "artifacts" + File.separator + "GREG" + File.separator;
        publisherUrl=automationContext.getContextUrls()
                .getSecureServiceUrl().replace("services","publisher/apis");

        SetTestEnvironment();

    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Create WSDL Resource Publisher test")
    public void createWsdlServiceAsset() throws JSONException, IOException {
        queryParamMap.put("type", "wsdl");
        String dataBody = readFile(resourcePath + "json" + File.separator + "publisherPublishWsdlResource.json");
        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl+"/assets",
                        MediaType.APPLICATION_JSON,
                        MediaType.APPLICATION_JSON, dataBody
                        , queryParamMap, headerMap, cookieHeader);
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        Assert.assertTrue((response.getStatusCode() == 201),
                "Wrong status code ,Expected 201 Created ,Received " +
                        response.getStatusCode());
        assetId = obj.get("id").toString();
        log.info("=================AssetId============= :"+ assetId);
        Assert.assertNotNull(assetId, "Empty asset resource id available" +
                response.getEntity(String.class));
        getAllAvailableWsdlServiceAssets().getEntity(String.class);
        getWsdlServiceAssetById(assetId).getEntity(String.class);
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Update WSDL Resource Publisher test",
            dependsOnMethods = {"createWsdlServiceAsset"})
    public void updateWsdlServiceAsset() throws JSONException, IOException {
        queryParamMap.put("type", "wsdl");
        String dataBody = readFile(resourcePath + "json" + File.separator + "PublisherRestResourceUpdate.json");
        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl+"/assets/"+assetId,
                        MediaType.APPLICATION_JSON,
                        MediaType.APPLICATION_JSON, dataBody
                        , queryParamMap, headerMap, cookieHeader);
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        Assert.assertTrue((response.getStatusCode() == 202),
                "Wrong status code ,Expected 202 Created ,Received " +
                        response.getStatusCode());
        Assert.assertTrue(obj.getJSONObject("attributes").get("overview_context")
                .equals("/changed/Context"));
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Delete WSDL Resource Publisher test",
            dependsOnMethods = {"createWsdlServiceAsset",
                    "updateWsdlServiceAsset"})
    public void deleteWsdlServiceAsset() throws JSONException {
        queryParamMap.put("type", "wsdl");
        genericRestClient.geneticRestRequestDelete(publisherUrl+"/assets/" + assetId,
                MediaType.APPLICATION_JSON,
                MediaType.APPLICATION_JSON
                , queryParamMap, headerMap, cookieHeader);
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Delete WSDL Resource Publisher test",
            dependsOnMethods = {"createWsdlServiceAsset",
                    "updateWsdlServiceAsset", "deleteWsdlServiceAsset"})
    public void uploadWsdlServiceAsset() throws JSONException {
            //TO DO
    }



    private void SetTestEnvironment() throws JSONException, XPathExpressionException, IOException {
        JSONObject objSessionPublisher =
                new JSONObject(authenticate(publisherUrl, genericRestClient,
                        automationContext.getSuperTenant().getTenantAdmin().getUserName(),
                        automationContext.getSuperTenant().getTenantAdmin().getPassword())
                        .getEntity(String.class));
        jSessionId = objSessionPublisher.getJSONObject("data").getString("sessionId");
        cookieHeader = "JSESSIONID=" + jSessionId;
    }

    private ClientResponse getAllAvailableWsdlServiceAssets()
    {
        queryParamMap.put("type", "wsdl");
        ClientResponse response =
                genericRestClient.geneticRestRequestGet
                        (publisherUrl+"/assets/"
                                + assetId, queryParamMap, headerMap, cookieHeader);
        return response;
    }

    private ClientResponse getWsdlServiceAssetById(String id)
    {
        queryParamMap.put("type", "wsdl");
        ClientResponse response =
                genericRestClient.geneticRestRequestGet
                        (publisherUrl+"/assets/"+id
                                , queryParamMap, headerMap, cookieHeader);
        return response;
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws RegistryException {
        deleteAsset(assetId, publisherUrl, cookieHeader, assetType, genericRestClient);
    }

    @DataProvider
    private static TestUserMode[][] userModeProvider() {
        return new TestUserMode[][]{
                new TestUserMode[]{TestUserMode.SUPER_TENANT_ADMIN}
//                new TestUserMode[]{TestUserMode.TENANT_USER},
        };
    }
}
