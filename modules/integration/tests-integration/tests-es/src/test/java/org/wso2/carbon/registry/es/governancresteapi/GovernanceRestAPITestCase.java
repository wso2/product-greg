/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.registry.es.governancresteapi;


import org.apache.wink.client.ClientResponse;
import org.apache.wink.common.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
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
import org.wso2.carbon.registry.es.utils.GovernanceRestApiUtil;
import org.wso2.carbon.registry.es.utils.GregESTestBaseTest;
import org.wso2.greg.integration.common.utils.GenericRestClient;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import javax.xml.xpath.XPathExpressionException;

/**
 * This class tests the wso2 governance REST api.
 */

@SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
public class GovernanceRestAPITestCase extends GregESTestBaseTest {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String AUTHORIZATION_HEADER_VALUE = "Basic YWRtaW46YWRtaW4=";
    private static final int ASSET_ID_ONE_INDEX = 0;
    private TestUserMode userMode;
    private GenericRestClient genericRestClient;
    private Map<String, String> queryParamMap;
    private Map<String, String> headerMap;
    private String governaceAPIUrl;
    private String resourcePath;
    private String publisherUrl;
    private String cookieHeader;
    private String restService1Name = "restService1";
    private String context1 = "/rest1";
    private String version = "1.0.0";
    private String assetId1, assetId2;
    private static final String LIFECYCLE_STATE = "Development";
    private static final String NEXT_LIFECYCLE_STATE = "Testing";
    private static final String LIFECYCLE = "ServiceLifeCycle";
    private static final String LIFECYCLE_STATE_CHANGE_ACTION = "Promote";

    @Factory(dataProvider = "userModeProvider")
    public GovernanceRestAPITestCase(TestUserMode userMode) {
        this.userMode = userMode;
    }

    @DataProvider
    private static TestUserMode[][] userModeProvider() {
        return new TestUserMode[][]{
                new TestUserMode[]{TestUserMode.SUPER_TENANT_ADMIN}
                //                new TestUserMode[]{TestUserMode.TENANT_USER},
        };
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(userMode);
        genericRestClient = new GenericRestClient();
        queryParamMap = new HashMap<>();
        headerMap = new HashMap<>();
        governaceAPIUrl = publisherContext.getContextUrls()
                .getSecureServiceUrl().replace("services", "governance");
        resourcePath = FrameworkPathUtil.getSystemResourceLocation()
                       + "artifacts" + File.separator + "GREG" + File.separator;
        publisherUrl = publisherContext.getContextUrls()
                .getSecureServiceUrl().replace("services", "publisher/apis");
        headerMap.put(AUTHORIZATION_HEADER, AUTHORIZATION_HEADER_VALUE);
    }

    @Test(groups = {"wso2.greg", "wso2.greg.governance.rest.api"}, description = "Get list of available artifact types")
    public void getAssetTypes() throws JSONException {

        ClientResponse response =
                genericRestClient.geneticRestRequestGet
                        (governaceAPIUrl + "/types"
                                , queryParamMap, headerMap, null);
        String allAssetTypes = response.getEntity(String.class);
        Assert.assertTrue(allAssetTypes.contains("SOAP Service"), "SOAP Service is not in the list of asset types");
        Assert.assertTrue(allAssetTypes.contains("REST Service"), "REST Service is not in the list of asset types");
        Assert.assertTrue(allAssetTypes.contains("WSDL"), "WSDL is not in the list of asset types");
        Assert.assertTrue(allAssetTypes.contains("WADL"), "WADL is not in the list of asset types");
        Assert.assertTrue(allAssetTypes.contains("Policy"), "Policy is not in the list of asset types");
        Assert.assertTrue(allAssetTypes.contains("Swagger"), "Swagger is not in the list of asset types");
    }

    @Test(groups = {"wso2.greg", "wso2.greg.governance.rest.api"}, description = "Create an asset of certain type." +
                                                                                 "This test case uses restservices for " +
                                                                                 "testing.")
    public void createAsset() throws IOException, JSONException, XPathExpressionException {

        String restTemplate = readFile(resourcePath + "json" + File.separator + "restservice-sample-gov-rest-api.json");
        String dataBody = String.format(restTemplate, restService1Name, "restservice", context1, version);
        String governanceRestApiUrlForRestServices = governaceAPIUrl + "/restservices";
        ClientResponse response = GovernanceRestApiUtil.createAsset(genericRestClient, dataBody, queryParamMap,
                                                                    headerMap, governanceRestApiUrlForRestServices);
        assetId1 = searchRestService(restService1Name);
        Assert.assertTrue(response.getStatusCode() == HttpStatus.CREATED.getCode(),
                          "Wrong status code ,Expected 201 Created ,Received " + response.getStatusCode());
        String locationHeader;
        if(governaceAPIUrl.contains(defaultHTTPPort)){
            locationHeader = governaceAPIUrl.replace(defaultHTTPPort,"") + "/restservices/" + assetId1;
        }else{
            locationHeader = governaceAPIUrl + "/restservices/" + assetId1;
        }


        Assert.assertEquals(response.getHeaders().get("Location").get(0), locationHeader,
                            "Incorrect header. Asset not added Successfully");
    }

    @Test(groups = {"wso2.greg", "wso2.greg.governance.rest.api"}, description = "Get an individual asset of certain type." +
                                                                                 "This test case uses restservices for " +
                                                                                 "testing.",
          dependsOnMethods = {"createAsset"})
    public void getAnIndividualAssetByUUID() throws JSONException, InterruptedException {

        String governanceRestApiUrl = governaceAPIUrl + "/restservices/" + assetId1;
        ClientResponse response = GovernanceRestApiUtil.getAssetById(genericRestClient, queryParamMap, headerMap,
                                                                     governanceRestApiUrl);
        JSONObject jsonObject = new JSONObject(response.getEntity(String.class));
        JSONArray jsonArray = jsonObject.getJSONArray("assets");
        String name = (String) jsonArray.getJSONObject(ASSET_ID_ONE_INDEX).get("name");
        String context = (String) jsonArray.getJSONObject(ASSET_ID_ONE_INDEX).get("context");
        String id = (String) jsonArray.getJSONObject(ASSET_ID_ONE_INDEX).get("id");
        Assert.assertEquals(name, restService1Name, "Incorrect asset name. Expected " + restService1Name + " received "
                                                    + name);
        Assert.assertEquals(context, context1, "Incorrect context. Expected " + context1 + " received " + context);
        Assert.assertEquals(id, assetId1, "Incorrect asset id. Expected " +
                                          assetId1 + " received " + id);
    }

    @Test(groups = {"wso2.greg", "wso2.greg.governance.rest.api"}, description = "Get list of available asset Type.This " +
                                                                                 "test case uses restservices for testing",
          dependsOnMethods = {"getAnIndividualAssetByUUID"})
    public void getListOfAssets()
            throws JSONException, IOException, XPathExpressionException, InterruptedException {
        //create two rest services
        int numOfRestServices = 2;
        String restService2Name = "restService2";
        String context2 = "/rest2";
        String version = "1.0.0";
        String restTemplate = readFile(resourcePath + "json" + File.separator + "restservice-sample-gov-rest-api.json");
        String dataBody = String.format(restTemplate, restService2Name, "restservice", context2, version);
        String governanceRestApiUrlForRestServices = governaceAPIUrl + "/restservices";
        GovernanceRestApiUtil.createAsset(genericRestClient, dataBody, queryParamMap,
                                          headerMap, governanceRestApiUrlForRestServices);
        assetId2 = searchRestService(restService2Name);
        queryParamMap.clear();
        ClientResponse listOfRestServices =
                genericRestClient.geneticRestRequestGet
                        (governaceAPIUrl + "/restservices"
                                , queryParamMap, headerMap, null);
        JSONObject jsonObject = new JSONObject(listOfRestServices.getEntity(String.class));
        JSONArray jsonArray = jsonObject.getJSONArray("assets");
        Assert.assertEquals(jsonArray.length(), numOfRestServices, "Wrong number of rest services. Expected " +
                                                                   numOfRestServices + " But received " +
                                                                   jsonArray.length());
        for (int i = 0; i < jsonArray.length(); i++) {
            String name = (String) jsonArray.getJSONObject(i).get("name");
            String context = (String) jsonArray.getJSONObject(i).get("context");
            String id = (String) jsonArray.getJSONObject(i).get("id");
            if (restService1Name.equals(name)) {
                Assert.assertEquals(context, context1, "Incorrect context. Expected " + context1 + " received " +
                                                       context);
                Assert.assertEquals(id, assetId1, "Incorrect asset ID. Expected " + assetId1
                                                  + " Received " + id);
            } else if (restService2Name.equals(name)) {
                Assert.assertEquals(context, context2, "Incorrect context. Expected " + context2 + " received " +
                                                       context);
                Assert.assertEquals(id, assetId2, "Incorrect asset ID. Expected " + assetId2 +
                                                  " Received " + id);
            }
        }
    }

    @Test(groups = {"wso2.greg", "wso2.greg.governance.rest.api"}, description = "Looks for artifacts with given name",
          dependsOnMethods = {"getListOfAssets"})
    public void getAssetWithName() throws JSONException {

        String governanceRestApiUrl = governaceAPIUrl + "/restservices";
        queryParamMap.put("name", restService1Name);
        ClientResponse clientResponse = genericRestClient.geneticRestRequestGet(governanceRestApiUrl, queryParamMap,
                                                                                headerMap, null);
        JSONObject jsonObject = new JSONObject(clientResponse.getEntity(String.class));
        JSONArray jsonArray = jsonObject.getJSONArray("assets");
        Assert.assertEquals(jsonArray.getJSONObject(ASSET_ID_ONE_INDEX).get("name"), restService1Name);
        Assert.assertEquals(jsonArray.getJSONObject(ASSET_ID_ONE_INDEX).get("context"), context1);
        Assert.assertEquals(jsonArray.getJSONObject(ASSET_ID_ONE_INDEX).get("id"), assetId1);
        queryParamMap.clear();
    }

    @Test(groups = {"wso2.greg", "wso2.greg.governance.rest.api"}, description = "Looks for artifacts with name and " +
                                                                                 "version",
          dependsOnMethods = {"getAssetWithName"})
    public void getAssetWithNameAndVersion() throws JSONException {

        String governanceRestApiUrl = governaceAPIUrl + "/restservices";
        queryParamMap.put("name",restService1Name);
        queryParamMap.put("version",version);
        ClientResponse clientResponse = genericRestClient.geneticRestRequestGet(governanceRestApiUrl,queryParamMap,
                                                                                headerMap,null);
        JSONObject jsonObject = new JSONObject(clientResponse.getEntity(String.class));
        JSONArray jsonArray = jsonObject.getJSONArray("assets");
        Assert.assertEquals(jsonArray.getJSONObject(ASSET_ID_ONE_INDEX).get("name"),restService1Name);
        Assert.assertEquals(jsonArray.getJSONObject(ASSET_ID_ONE_INDEX).get("context"),context1);
        Assert.assertEquals(jsonArray.getJSONObject(ASSET_ID_ONE_INDEX).get("id"),assetId1);
        queryParamMap.clear();
    }

    @Test(groups = {"wso2.greg", "wso2.greg.governance.rest.api"}, description = "Update the context of a REST service",
          dependsOnMethods = {"getAssetWithNameAndVersion"})
    public void updateAnAsset() throws IOException, JSONException {

        String updatedContext = "/rest1-new";
        String restTemplate = readFile(resourcePath + "json" + File.separator + "restservice-sample-gov-rest-api.json");
        String dataBody = String.format(restTemplate, restService1Name, "restservice", updatedContext, version);
        String governanceRestApiUrl = governaceAPIUrl + "/restservices/" + assetId1;
        ClientResponse response = GovernanceRestApiUtil.updateAsset(genericRestClient, dataBody, queryParamMap,
                                                                    headerMap, governanceRestApiUrl);
        Assert.assertTrue((response.getStatusCode() == HttpStatus.CREATED.getCode()),
                          "Wrong status code ,Expected 201 Created ,Received " +
                          response.getStatusCode());
        ClientResponse clientResponse = GovernanceRestApiUtil.getAssetById(genericRestClient, queryParamMap, headerMap,
                                                                     governanceRestApiUrl);
        JSONObject jsonObject = new JSONObject(clientResponse.getEntity(String.class));
        JSONArray jsonArray = jsonObject.getJSONArray("assets");
        String name = (String) jsonArray.getJSONObject(ASSET_ID_ONE_INDEX).get("name");
        String context = (String) jsonArray.getJSONObject(ASSET_ID_ONE_INDEX).get("context");
        String id = (String) jsonArray.getJSONObject(ASSET_ID_ONE_INDEX).get("id");
        Assert.assertEquals(name, restService1Name, "Incorrect asset name. Expected " + restService1Name + " received "
                                                    + name);
        //Check for updated context
        Assert.assertEquals(context, updatedContext, "Incorrect context. Expected " + updatedContext + " received " +
                                                     context);
        Assert.assertEquals(id, assetId1, "Incorrect asset id. Expected " +
                                          assetId1 + " received " + id);
    }

    @Test(groups = {"wso2.greg", "wso2.greg.governance.rest.api"}, description = "Get lifecycle state of an asset",
          dependsOnMethods = {"updateAnAsset"})
    public void getLifeCycleStateOfAsset() throws JSONException {

        String lCState = LIFECYCLE_STATE;
        String governanceRestApiUrl = governaceAPIUrl + "/restservices/" + assetId1 + "/states";
        ClientResponse responseOne = genericRestClient.geneticRestRequestGet(governanceRestApiUrl, queryParamMap,
                                                                             headerMap, null);
        JSONObject jsonObject = new JSONObject(responseOne.getEntity(String.class));
        Assert.assertEquals(jsonObject.get(LIFECYCLE), lCState, "Incorrect life cycle state. Expected " + lCState
                                                                + " ,received " + jsonObject.get(LIFECYCLE));
        queryParamMap.clear();
        queryParamMap.put("lc", "ServiceLifeCycle");
        ClientResponse responseTwo = genericRestClient.geneticRestRequestGet(governanceRestApiUrl, queryParamMap,
                                                                             headerMap, null);
        JSONObject lifeCycleState = new JSONObject(responseTwo.getEntity(String.class));
        Assert.assertEquals(lifeCycleState.get("state"), lCState, "Incorrect life cycle state. Expected " + lCState
                                                                  + " ,received " + lifeCycleState.get("state"));
        queryParamMap.clear();
    }

    @Test(groups = {"wso2.greg", "wso2.greg.governance.rest.api"}, description = "Update lifecycle change of an asset",
          dependsOnMethods = {"getLifeCycleStateOfAsset"})
    public void updateLifeCycle() throws IOException, JSONException {

        String governanceRestApiUrl = governaceAPIUrl + "/restservices/" + assetId1 + "/states";
        String lcStateChangeTemplate = readFile(resourcePath + "json" + File.separator +
                                                "lifecycle-info-gov-rest-api.json");
        String dataBody = String.format(lcStateChangeTemplate, LIFECYCLE, LIFECYCLE_STATE_CHANGE_ACTION);
        ClientResponse response = genericRestClient.genericRestRequestPut(governanceRestApiUrl,
                                                                          MediaType.APPLICATION_JSON,
                                                                          MediaType.APPLICATION_JSON, dataBody,
                                                                          queryParamMap, headerMap,
                                                                          null);
        JSONObject jsonObject = new JSONObject(response.getEntity(String.class));
        Assert.assertEquals(jsonObject.get(LIFECYCLE), NEXT_LIFECYCLE_STATE, "Incorrect life cycle state. Expected " +
                                                                             NEXT_LIFECYCLE_STATE
                                                                             + " ,received " +
                                                                             jsonObject.get(LIFECYCLE));
    }

    @Test(groups = {"wso2.greg", "wso2.greg.governance.rest.api"}, description = "Delete a rest service",
          dependsOnMethods = {"updateLifeCycle"})
    public void deleteAnAsset() throws JSONException {

        genericRestClient.geneticRestRequestDelete(governaceAPIUrl + "/restservices/" + assetId1,
                                                   MediaType.APPLICATION_JSON,
                                                   MediaType.APPLICATION_JSON, queryParamMap,
                                                   headerMap, null);
        String governanceRestApiUrl = governaceAPIUrl + "/restservices/" + assetId1;
        //Check whether asset got deleted.
        ClientResponse clientResponse = GovernanceRestApiUtil.getAssetById(genericRestClient, queryParamMap, headerMap,
                                                                           governanceRestApiUrl);
        Assert.assertNull(clientResponse.getEntity(String.class));
    }



    /**
     * This method search for a particular asset and returns the corresponding asset id.
     * @param assetName name of the artifact
     * @return asset id
     * @throws JSONException
     * @throws XPathExpressionException
     */
    private String searchRestService(String assetName) throws JSONException, XPathExpressionException {
        setTestEnvironment();
        Map<String, String> queryParamMap = new HashMap<>();
        queryParamMap.put("q", "\"name" + "\":" + "\"" + assetName + "\"");
        ClientResponse clientResponse = searchAssetByQuery(publisherUrl, genericRestClient, cookieHeader, queryParamMap);
        JSONObject obj = new JSONObject(clientResponse.getEntity(String.class));
        JSONArray jsonArray = obj.getJSONArray("list");
        for (int i = 0; i < jsonArray.length(); i++) {
            String id = (String) jsonArray.getJSONObject(i).get("id");
            String name = (String) jsonArray.getJSONObject(i).get("name");
            if (assetName.equals(name)) {
                return id;
            }
        }
        return null;
    }

    /**
     * This method creates the cookieHeader neede fot publisher REST api calls
     * @throws JSONException
     * @throws XPathExpressionException
     */
    private void setTestEnvironment() throws JSONException, XPathExpressionException {
        String jSessionId;
        ClientResponse response = authenticate(publisherUrl, genericRestClient,
                                               automationContext.getSuperTenant().getTenantAdmin().getUserName(),
                                               automationContext.getSuperTenant().getTenantAdmin().getPassword());
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        assertTrue((response.getStatusCode() == 200),
                   "Wrong status code ,Expected 200 OK ,Received " +
                   response.getStatusCode()
        );
        jSessionId = obj.getJSONObject("data").getString("sessionId");
        cookieHeader = "JSESSIONID=" + jSessionId;
        assertNotNull(jSessionId, "Invalid JSessionID received");
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws RegistryException, JSONException {
        Map<String, String> queryParamMap = new HashMap<>();
        queryParamMap.put("type", "restservice");
        deleteAssetById(publisherUrl, genericRestClient, cookieHeader, assetId1, queryParamMap);
        deleteAssetById(publisherUrl, genericRestClient, cookieHeader, assetId2, queryParamMap);
    }
}
