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
package org.wso2.carbon.registry.es.publisher.associations;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wink.client.ClientResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.ParseException;
import org.testng.annotations.*;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.es.utils.ESTestCommonUtils;
import org.wso2.carbon.registry.es.utils.GregESTestBaseTest;
import org.wso2.greg.integration.common.utils.GenericRestClient;

import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertTrue;

@SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
public class GregRestResourceWSDLAssociationTestCase extends GregESTestBaseTest {
    private static final Log log =
            LogFactory.getLog(GregRestResourceWSDLAssociationTestCase.class);
    private TestUserMode userMode;
    String jSessionId;
    String assetType;
    String assetId;
    String cookieHeaderPublisher;
    GenericRestClient genericRestClient;
    Map<String, String> queryParamMap;
    Map<String, String> headerMap;
    String publisherUrl;
    String resourcePath;
    ESTestCommonUtils testCommonUtils;

    @Factory(dataProvider = "userModeProvider")
    public GregRestResourceWSDLAssociationTestCase(TestUserMode userMode) {
        this.userMode = userMode;
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(userMode);
        assetType = "wsdl";
        genericRestClient = new GenericRestClient();
        headerMap = new HashMap<String, String>();
        publisherUrl = publisherContext.getContextUrls()
                .getSecureServiceUrl().replace("services", "publisher/apis");
        resourcePath = FrameworkPathUtil.getSystemResourceLocation()
                + "artifacts" + File.separator + "GREG" + File.separator + "json" + File.separator + "publisherPublishWSDLResource.json";
        setTestEnvironment();
    }

    private void setTestEnvironment() throws JSONException, XPathExpressionException, IOException {
        JSONObject objSessionPublisher =
                new JSONObject(authenticate(publisherUrl, genericRestClient,
                        automationContext.getSuperTenant().getTenantAdmin().getUserName(),
                        automationContext.getSuperTenant().getTenantAdmin().getPassword())
                        .getEntity(String.class));
        jSessionId = objSessionPublisher.getJSONObject("data").getString("sessionId");
        cookieHeaderPublisher = "JSESSIONID=" + jSessionId;

        JSONObject obj = new JSONObject(createAsset(resourcePath, publisherUrl,
                cookieHeaderPublisher, assetType,
                genericRestClient).getEntity(String.class));
        String resultName = obj.get("overview_name").toString();
        Map<String, String> queryParamMap = new HashMap<>();
        queryParamMap.put("type", "wsdl");
        queryParamMap.put("overview_name", resultName);
        testCommonUtils = new ESTestCommonUtils(genericRestClient, publisherUrl, headerMap);
        testCommonUtils.setCookieHeader(cookieHeaderPublisher);
        ClientResponse clientResponse = testCommonUtils.searchAssetByQuery(queryParamMap);
        JSONObject wsdlObj = new JSONObject(clientResponse.getEntity(String.class));
        JSONArray jsonArray = wsdlObj.getJSONArray("list");
        for (int i = 0; i < jsonArray.length(); i++) {
            String name = (String) jsonArray.getJSONObject(i).get("name");
            if (resultName.equals(name)) {
                assetId = (String) jsonArray.getJSONObject(i).get("id");
                break;
            }
        }
    }

    @BeforeMethod
    public void resetParameters() {
        queryParamMap = new HashMap<String, String>();
        headerMap = new HashMap<String, String>();
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "check the wsdl upload created associations")
    public void serviceAssociationExists() throws JSONException, IOException, ParseException, InterruptedException {
        ClientResponse associationList = genericRestClient.geneticRestRequestGet(publisherUrl +
                "/association/restservice/dependancies/" + assetId, queryParamMap, headerMap, cookieHeaderPublisher);
        JsonArray jsonObject = new JsonParser().parse(associationList.getEntity(String.class)).
                getAsJsonObject().get("results").getAsJsonArray();
        assertTrue(jsonObject.toString().contains("uuid"));
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws RegistryException, JSONException {
        Map<String, String> queryParamMap = new HashMap<>();
        queryParamMap.put("type", "wsdl");
        Map<String, String> assocUUIDMap = testCommonUtils.getAssociationsFromPages(assetId, queryParamMap);
        testCommonUtils.deleteAssetById(assetId, queryParamMap);
        testCommonUtils.deleteAllAssociationsById(assetId, queryParamMap);
        queryParamMap.clear();
        for (String uuid : assocUUIDMap.keySet()) {
            queryParamMap.put("type", testCommonUtils.getType(assocUUIDMap.get(uuid)));
            testCommonUtils.deleteAssetById(uuid, queryParamMap);
        }
    }

    @DataProvider
    private static TestUserMode[][] userModeProvider() {
        return new TestUserMode[][]{
                new TestUserMode[]{TestUserMode.SUPER_TENANT_ADMIN},
        };
    }
}
