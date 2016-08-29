/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.registry.es.notes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.es.utils.ESTestCommonUtils;
import org.wso2.carbon.registry.es.utils.GregESTestBaseTest;
import org.wso2.greg.integration.common.utils.GenericRestClient;

import javax.ws.rs.core.MediaType;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GregWsdlNotesESTestCase extends GregESTestBaseTest {
    private static final Log log = LogFactory.getLog(GregWsdlNotesESTestCase.class);
    private TestUserMode userMode;
    String jSessionId;
    String assetId;
    String assetName;
    String assetType;
    String cookieHeaderPublisher;
    GenericRestClient genericRestClient;
    Map<String, String> queryParamMap;
    Map<String, String> headerMap;
    String publisherUrl;
    String resourcePath;
    String noteName;
    String noteOverviewHash;
    String noteAssetId;
    String replyAssetId;
    ESTestCommonUtils esTestCommonUtils;

    @Factory(dataProvider = "userModeProvider")
    public GregWsdlNotesESTestCase(TestUserMode userMode) {
        this.userMode = userMode;
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(userMode);
        noteName = "testNote33";
        assetType = "wsdl";
        genericRestClient = new GenericRestClient();
        queryParamMap = new HashMap<String, String>();
        headerMap = new HashMap<String, String>();
        resourcePath =
                FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator + "GREG" + File.separator
                        + "json" + File.separator;
        publisherUrl = automationContext.getContextUrls().getSecureServiceUrl().replace("services", "publisher/apis");
        esTestCommonUtils = new ESTestCommonUtils(genericRestClient, publisherUrl, headerMap);
        setTestEnvironment();
    }


    @Test(groups = {"wso2.greg", "wso2.greg.es"}, alwaysRun = true,description = "Add note to wsdl asset test")
    public void addNoteToWsdlAsset() throws JSONException, IOException {
        queryParamMap.put("type", "note");
        //  https://localhost:9443/publisher/apis/assets?type=note
        String dataBody = String.format(readFile(resourcePath + "publisherAddNoteRestResource.json"), assetType,
                "testservice1234", noteName);
        ClientResponse response = genericRestClient.geneticRestRequestPost(publisherUrl + "/assets",
                MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, dataBody,
                queryParamMap, headerMap, cookieHeaderPublisher);
        Assert.assertTrue((response.getStatusCode() == 201), "Wrong status code ,Expected 201 OK ,Received "
                + response.getStatusCode());
        JSONObject responseObj = new JSONObject(response.getEntity(String.class));
        Assert.assertTrue(responseObj.getJSONObject("attributes").get("overview_note")
                .toString().contains(noteName), "Does not create a note");
        Assert.assertTrue(responseObj.getJSONObject("attributes").get("overview_resourcepath")
                .toString().contains(assetType), "Fault resource path for note");
        Assert.assertNotNull(responseObj.getJSONObject("attributes").get("overview_hash"));
        noteOverviewHash = responseObj.getJSONObject("attributes").get("overview_hash").toString();
        noteAssetId = responseObj.get("id").toString();
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, dependsOnMethods = "addNoteToWsdlAsset",
            description = "Add Reply to note added for a wsdl Asset test")
    public void addReplyToNoteWsdl() throws JSONException, IOException {
        queryParamMap.put("type", "note");
        String dataBody = String.format(readFile(resourcePath + "publisherNoteReplyRestResource.json"),
                noteOverviewHash, "replyNote123", noteOverviewHash);
        ClientResponse response = genericRestClient.geneticRestRequestPost(publisherUrl + "/assets",
                MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, dataBody,
                queryParamMap, headerMap, cookieHeaderPublisher);
        Assert.assertTrue((response.getStatusCode() == 201),
                "Wrong status code ,Expected 200 OK ,Received " + response.getStatusCode());

        JSONObject responseObj = new JSONObject(response.getEntity(String.class));
        Assert.assertTrue(responseObj.getJSONObject("attributes").get("overview_note")
                .toString().contains("replyNote123"), "Does not create a note");
        Assert.assertTrue(responseObj.getJSONObject("attributes").get("overview_resourcepath")
                .toString().contains(noteOverviewHash), "Fault resource path for note");
        Assert.assertNotNull(responseObj.getJSONObject("attributes").get("overview_hash"));
        replyAssetId = responseObj.get("id").toString();
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, dependsOnMethods = "addReplyToNoteWsdl",
            description = "Delete Reply to note added for a wsdl Asset test")
    public void deleteReplyToNoteWsdl() throws JSONException, IOException {
        queryParamMap.put("type", "note");
        ClientResponse response = genericRestClient.geneticRestRequestDelete(publisherUrl + "/assets/" + replyAssetId,
                MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, queryParamMap,
                headerMap, cookieHeaderPublisher);
        response.getEntity(String.class).contains("Asset Deleted Successfully");
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, dependsOnMethods = "deleteReplyToNoteWsdl",
            description = "Delete Note test")
    public void deleteNote() throws JSONException, IOException {
        queryParamMap.put("type", "note");
        //  https://localhost:9443/publisher/apis/assets?type=note
        ClientResponse response = genericRestClient.geneticRestRequestDelete(publisherUrl + "/assets/" + noteAssetId,
                MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, queryParamMap, headerMap,
                cookieHeaderPublisher);

        response.getEntity(String.class).contains("Asset Deleted Successfully");
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws RegistryException {
        deleteAsset(assetId, publisherUrl, cookieHeaderPublisher, assetType, genericRestClient);
    }

    private void setTestEnvironment() throws JSONException, XPathExpressionException,
            IOException {
        JSONObject objSessionPublisher = new JSONObject(authenticate(publisherUrl, genericRestClient,
                automationContext.getSuperTenant().getTenantAdmin().getUserName(),
                automationContext.getSuperTenant().getTenantAdmin().getPassword()).getEntity(String.class));
        jSessionId = objSessionPublisher.getJSONObject("data").getString("sessionId");
        cookieHeaderPublisher = "JSESSIONID=" + jSessionId;
        esTestCommonUtils.setCookieHeader(cookieHeaderPublisher);

        String dataBody = readFile(resourcePath + "wsdl-ops.json");
        assetName = (String)(new JSONObject(dataBody)).get("overview_name");
        JSONObject objSessionId = new JSONObject(createAsset(resourcePath + "wsdl-ops.json", publisherUrl,
                cookieHeaderPublisher, assetType, genericRestClient).getEntity(String.class));

        String resultName = objSessionId.get("overview_name").toString();
        Assert.assertEquals(resultName,assetName);
        searchWsdlAsset();
    }

    public void searchWsdlAsset() throws JSONException {
        boolean assetFound = false;
        Map<String, String> queryParamMap = new HashMap<String, String>();
        queryParamMap.put("type", "wsdl");
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

    @DataProvider
    private static TestUserMode[][] userModeProvider() {
        return new TestUserMode[][]{
                new TestUserMode[]{TestUserMode.SUPER_TENANT_ADMIN},
        };
    }
}
