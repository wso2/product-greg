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
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.es.utils.GregESTestBaseTest;
import org.wso2.greg.integration.common.utils.GenericRestClient;

import javax.ws.rs.core.MediaType;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;

@SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
public class SoapServiceCRUDTestCase extends GregESTestBaseTest {
    private static final Log log = LogFactory.getLog(SoapServiceCRUDTestCase.class);
    private TestUserMode userMode;
    String jSessionId;
    String assetId;
    String assocAssetId;
    String assetName;
    String cookieHeader;
    GenericRestClient genericRestClient;
    Map<String, String> headerMap;
    String publisherUrl;
    String resourcePath;

    @Factory(dataProvider = "userModeProvider")
    public SoapServiceCRUDTestCase(TestUserMode userMode) {
        this.userMode = userMode;
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(userMode);
        genericRestClient = new GenericRestClient();
        headerMap = new HashMap<>();
        resourcePath = FrameworkPathUtil.getSystemResourceLocation()
                + "artifacts" + File.separator + "GREG" + File.separator;
        publisherUrl = publisherContext.getContextUrls()
                .getSecureServiceUrl().replace("services", "publisher/apis");
        setTestEnvironment();
    }

    @BeforeMethod(alwaysRun = true)
    public void reInitEnvironment() throws XPathExpressionException, JSONException {
        setTestEnvironment();
    }

    private void setTestEnvironment() throws JSONException, XPathExpressionException {
        JSONObject objSessionPublisher =
                new JSONObject(authenticate(publisherUrl, genericRestClient,
                        automationContext.getSuperTenant().getTenantAdmin().getUserName(),
                        automationContext.getSuperTenant().getTenantAdmin().getPassword())
                        .getEntity(String.class));
        jSessionId = objSessionPublisher.getJSONObject("data").getString("sessionId");
        cookieHeader = "JSESSIONID=" + jSessionId;
        Assert.assertNotNull(jSessionId, "Invalid JSessionID received");
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Create Soap Service in Publisher")
    public void createSoapServiceAsset() throws JSONException, IOException {
        Map<String, String> queryParamMap = new HashMap<>();
        queryParamMap.put("type", "soapservice");
/*        "overview_name":"bbb",
          "overview_namespace": "bbb",
          "overview_version":"1.0.0"*/
        String soapTemplate = readFile(resourcePath + "json" + File.separator + "soapservice-sample.json");
        assetName = "bbb";
        String dataBody = String.format(soapTemplate, assetName, "bbb", "1.0.0",null);
        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/assets",
                        MediaType.APPLICATION_JSON,
                        MediaType.APPLICATION_JSON, dataBody
                        , queryParamMap, headerMap, cookieHeader);
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        Assert.assertTrue((response.getStatusCode() == 201),
                "Wrong status code ,Expected 201 Created ,Received " +
                        response.getStatusCode());
        assetId = (String)obj.get("id");
        Assert.assertNotNull(assetId, "Empty asset resource id available" +
                response.getEntity(String.class));
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Get Soap Service in Publisher",
            dependsOnMethods = {"createSoapServiceAsset"})
    public void getSoapServiceAsset() throws JSONException {
        Map<String, String> queryParamMap = new HashMap<>();
        queryParamMap.put("type", "soapservice");
        ClientResponse clientResponse = getAssetById(publisherUrl, genericRestClient, cookieHeader, assetId, queryParamMap);
        Assert.assertTrue((clientResponse.getStatusCode() == 200),
                "Wrong status code ,Expected 200 OK " +
                        clientResponse.getStatusCode());
        JSONObject obj = new JSONObject(clientResponse.getEntity(String.class));
        Assert.assertEquals(obj.get("id"), assetId);
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Search Soap Service in Publisher",
            dependsOnMethods = {"getSoapServiceAsset"})
    public void searchSoapService() throws JSONException {
        boolean assetFound = false;
        Map<String, String> queryParamMap = new HashMap<>();
        queryParamMap.put("q", "\"name" + "\":" + "\"" + assetName + "\"");
        ClientResponse clientResponse = searchAssetByQuery(publisherUrl, genericRestClient, cookieHeader, queryParamMap);
        JSONObject obj = new JSONObject(clientResponse.getEntity(String.class));
        JSONArray jsonArray = obj.getJSONArray("list");
        for (int i = 0; i < jsonArray.length(); i++) {
            String id = (String) jsonArray.getJSONObject(i).get("id");
            if (assetId.equals(id)) {
                assetFound = true;
                break;
            }
        }
        Assert.assertTrue(assetFound, "Soap Service not found in assets listing");
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Update Soap Service in Publisher",
            dependsOnMethods = {"searchSoapService"})
    public void updateSoapServiceAsset() throws JSONException, IOException {
        Map<String, String> queryParamMap = new HashMap<>();
        queryParamMap.put("type", "soapservice");



        String soapTemplate = readFile(resourcePath + "json" + File.separator + "soapservice-sample-update.json");
        String dataBody = String.format(soapTemplate, "bbb", "updating soap service ...");
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
                .equals("updating soap service ..."));
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Delete Soap Service in Publisher",
            dependsOnMethods = {"updateSoapServiceAsset"})
    public void deleteSoapServiceAsset() throws JSONException {
        Map<String, String> queryParamMap = new HashMap<>();
        queryParamMap.put("type", "soapservice");
        genericRestClient.geneticRestRequestDelete(publisherUrl + "/assets/" + assetId,
                MediaType.APPLICATION_JSON,
                MediaType.APPLICATION_JSON
                , queryParamMap, headerMap, cookieHeader);
        ClientResponse clientResponse = getAssetById(publisherUrl, genericRestClient, cookieHeader, assetId, queryParamMap);
        Assert.assertTrue((clientResponse.getStatusCode() == 404),
                "Wrong status code ,Expected 404 Not Found " +
                        clientResponse.getStatusCode());
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Add SOAP service with wsdl url", dependsOnMethods = "deleteSoapServiceAsset")
    public void createSOAPServiceWithWSDL() throws GovernanceException, IOException, JSONException, InterruptedException {
        Map<String, String> queryParamMap = new HashMap<>();
        queryParamMap.put("type", "soapservice");
        String soapTemplate = readFile(resourcePath + "json" + File.separator + "soapservice-sample.json");
        assetName = "SOAPService1";
        String dataBody = String.format(soapTemplate, assetName, "com.wso2.sample", "1.0.0", "Description");

        JSONObject jsonObject = new JSONObject(dataBody);
        jsonObject.put("interface_wsdlURL", "https://raw.githubusercontent.com/wso2/wso2-qa-artifacts/master/automation-artifacts/greg/wsdl/calculator.wsdl");
        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/assets",
                        MediaType.APPLICATION_JSON,
                        MediaType.APPLICATION_JSON, jsonObject.toString()
                        , queryParamMap, headerMap, cookieHeader);
        Assert.assertTrue((response.getStatusCode() == 201),
                "Wrong status code ,Expected 201 Created ,Received " +
                        response.getStatusCode());
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        assocAssetId = obj.get("id").toString();
        Assert.assertNotNull(assocAssetId, "Empty asset resource id available" +
                response.getEntity(String.class));
        Map<String,String> assocMap = getAssociationsFromPages(publisherUrl, genericRestClient, cookieHeader, assocAssetId, queryParamMap);
        assertEquals(assocMap.size(), 2, "Expecting 2 dependencies : WSDL and Endpoint");
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws RegistryException, JSONException {
        Map<String, String> queryParamMap = new HashMap<>();
        queryParamMap.put("type", "soapservice");
        deleteAssetById(publisherUrl, genericRestClient, cookieHeader, assetId, queryParamMap);
        deleteAssetById(publisherUrl, genericRestClient, cookieHeader, assocAssetId, queryParamMap);
        deleteAllAssociationsById(publisherUrl, genericRestClient, cookieHeader, assocAssetId, queryParamMap);
    }

    @DataProvider
    private static TestUserMode[][] userModeProvider() {
        return new TestUserMode[][]{
                new TestUserMode[]{TestUserMode.SUPER_TENANT_ADMIN}
//                new TestUserMode[]{TestUserMode.TENANT_USER},
        };
    }
}
