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


import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
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
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.es.utils.GovernanceRestApiUtil;
import org.wso2.carbon.registry.es.utils.GregESTestBaseTest;
import org.wso2.greg.integration.common.utils.GenericRestClient;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import javax.xml.xpath.XPathExpressionException;

/**
 * This class tests the wso2 governance REST api on endpoint assets
 */
public class GovernanceRestApiEndpointTestCase extends GregESTestBaseTest {

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
    private String endpoint1Name = "endpoint-1";
    private String enviornment_qa = "QA";
    private String endpointId1, endpointId2;
    private String restTemplate;
    private String associationEndpointName = "endpoint-association";
    private String restServiceName = "TestRESTService";
    private String restServiceVersion = "1.0.0";
    private String assetIdOfRestService;
    private String governanceRestApiUrlForEndpoints;
    private String lifeCycleName;
    private String lifeCycleState;
    private String endpointLifecycle = "EndpointLifeCycle";
    private String activeState = "Active";


    @Factory(dataProvider = "userModeProvider")
    public GovernanceRestApiEndpointTestCase(TestUserMode userMode) {
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
        governaceAPIUrl = automationContext.getContextUrls()
                .getSecureServiceUrl().replace("services", "governance");
        resourcePath = FrameworkPathUtil.getSystemResourceLocation()
                       + "artifacts" + File.separator + "GREG" + File.separator;
        publisherUrl = automationContext.getContextUrls()
                .getSecureServiceUrl().replace("services", "publisher/apis");
        headerMap.put(AUTHORIZATION_HEADER, AUTHORIZATION_HEADER_VALUE);
        restTemplate = readFile(resourcePath + "json" + File.separator + "endpoint-sample-gov-rest-api.json");
        governanceRestApiUrlForEndpoints = governaceAPIUrl + "/endpoints";
    }

    @Test(groups = {"wso2.greg", "wso2.greg.governance.rest.api"}, description = "Create new endpoint")
    public void createAsset() throws IOException, JSONException, XPathExpressionException {

        String dataBody = GovernanceRestApiUtil.createEndpointDataBody(restTemplate, endpoint1Name, enviornment_qa);
        ClientResponse response = GovernanceRestApiUtil.createAsset(genericRestClient, dataBody, queryParamMap,
                                                                    headerMap, governanceRestApiUrlForEndpoints);

        Assert.assertTrue(response.getStatusCode() == HttpStatus.CREATED.getCode(),
                          "Wrong status code ,Expected 201 Created ,Received " + response.getStatusCode());
        String locationHeader = response.getHeaders().get("Location").get(0);
        endpointId1 = locationHeader.substring(locationHeader.lastIndexOf("/") + 1);
    }

    @Test(groups = {"wso2.greg", "wso2.greg.governance.rest.api"}, description = "Get an individual endpoint",
          dependsOnMethods = {"createAsset"})
    public void getAnIndividualAssetByUUID() throws JSONException {

        String governanceRestApiUrl = governanceRestApiUrlForEndpoints + "/" + endpointId1;
        ClientResponse response = GovernanceRestApiUtil.getEndpointByID(genericRestClient, queryParamMap, headerMap,
                                                                        governanceRestApiUrl);
        JSONObject jsonObject = new JSONObject(response.getEntity(String.class));
        JSONArray jsonArray = jsonObject.getJSONArray("assets");
        String name = (String) jsonArray.getJSONObject(ASSET_ID_ONE_INDEX).get("name");
        String id = (String) jsonArray.getJSONObject(ASSET_ID_ONE_INDEX).get("id");
        String enviornment = (String) jsonArray.getJSONObject(ASSET_ID_ONE_INDEX).get("environment");
        String type = (String) jsonArray.getJSONObject(ASSET_ID_ONE_INDEX).get("type");
        Assert.assertEquals(name, endpoint1Name, "Incorrect asset name. Expected " + endpoint1Name + " received "
                                                 + name);
        Assert.assertEquals(id, endpointId1, "Incorrect asset id. Expected " + endpointId1 + " received "
                                             + id);
        Assert.assertEquals(enviornment, enviornment_qa, "Incorrect asset enviornment. Expected " + enviornment_qa +
                                                         " received "
                                                         + enviornment);
        Assert.assertEquals(type, "endpoint", "Incorrect asset type. Expected " + "endpoint" + " received "
                                              + type);
    }

    @Test(groups = {"wso2.greg", "wso2.greg.governance.rest.api"}, description = "Get all endpoints", dependsOnMethods =
            {"getAnIndividualAssetByUUID"})
    public void getAllEndpoints() throws IOException, InterruptedException, JSONException {

        int numOfEndpoints = 2;
        //create another endpoint
        String endpoint2Name = "endpoint-2";
        String environment2 = "DEV";
        String dataBody = GovernanceRestApiUtil.createEndpointDataBody(restTemplate, endpoint2Name, environment2);
        ClientResponse response = GovernanceRestApiUtil.createAsset(genericRestClient, dataBody, queryParamMap,
                                                                    headerMap, governanceRestApiUrlForEndpoints);
        Assert.assertTrue(response.getStatusCode() == HttpStatus.CREATED.getCode(),
                          "Wrong status code ,Expected 201 Created ,Received " + response.getStatusCode());
        String locationHeader = response.getHeaders().get("Location").get(0);
        endpointId2 = locationHeader.substring(locationHeader.lastIndexOf("/") + 1);
        Thread.sleep(1000);//for asset indexing
        ClientResponse listOfEndpoints = genericRestClient.geneticRestRequestGet(governanceRestApiUrlForEndpoints,
                                                                                 queryParamMap, headerMap, null);

        JSONObject jsonObject = new JSONObject(listOfEndpoints.getEntity(String.class));
        JSONArray jsonArray = jsonObject.getJSONArray("assets");
        Assert.assertEquals(jsonArray.length(), numOfEndpoints, "Wrong number of endpoints. Expected " + numOfEndpoints +
                                                                " But received " + jsonArray.length());
        for (int i = 0; i < jsonArray.length(); i++) {
            String name = (String) jsonArray.getJSONObject(i).get("name");
            String environment = (String) jsonArray.getJSONObject(i).get("environment");
            String id = (String) jsonArray.getJSONObject(i).get("id");
            if (endpoint1Name.equals(name)) {
                Assert.assertEquals(environment, enviornment_qa, "Incorrect context. Expected " + enviornment_qa +
                                                                 " received " +
                                                                 environment);
                Assert.assertEquals(id, endpointId1, "Incorrect asset ID. Expected " + endpointId1
                                                     + " Received " + id);
            } else if (endpoint2Name.equals(name)) {
                Assert.assertEquals(environment, environment2, "Incorrect context. Expected " + environment2 +
                                                               " received " +
                                                               environment);
                Assert.assertEquals(id, endpointId2, "Incorrect asset ID. Expected " + endpointId2 +
                                                     " Received " + id);
            }
        }

    }

    @Test(groups = {"wso2.greg", "wso2.greg.governance.rest.api"}, description = "Create an endpoint and associate it " +
                                                                                 "with a REST service",
          dependsOnMethods = {"getAllEndpoints"})
    public void createEndpointAsAnAssociationWithUUID()
            throws IOException, JSONException, XPathExpressionException, InterruptedException {

        setTestEnvironment();
        assetIdOfRestService = createRestService();
        String governanceRestApiUrlForEndpointsAssociation = governanceRestApiUrlForEndpoints + "/restservices/" +
                                                             assetIdOfRestService;
        String dataBody = GovernanceRestApiUtil.createEndpointDataBody(restTemplate, associationEndpointName, enviornment_qa);
        ClientResponse response = GovernanceRestApiUtil.createAsset(genericRestClient, dataBody, queryParamMap,
                                                                    headerMap, governanceRestApiUrlForEndpointsAssociation);

        Assert.assertTrue(response.getStatusCode() == HttpStatus.CREATED.getCode(),
                          "Wrong status code ,Expected 201 Created ,Received " + response.getStatusCode());
        String locationHeader = response.getHeaders().get("Location").get(0);
        String assetIdOfEndpoint = locationHeader.substring(locationHeader.lastIndexOf("/") + 1);
        Thread.sleep(1000);

        Map<String, String> headerMap = new HashMap<>();
        ClientResponse associationList = genericRestClient.geneticRestRequestGet(publisherUrl +
                                                                                 "/association/restservice/dependancies/"
                                                                                 + assetIdOfRestService, queryParamMap,
                                                                                 headerMap, cookieHeader);
        JsonArray jsonObject = new JsonParser().parse(associationList.getEntity(String.class)).
                getAsJsonObject().get("results").getAsJsonArray();
        assertTrue(jsonObject.toString().contains(assetIdOfEndpoint));
        assertTrue(jsonObject.toString().contains(associationEndpointName));
        setTestEnvironment();
        //delete the endpoint in the end
        queryParamMap.put("type", "endpoint");
        deleteAssetById(publisherUrl, genericRestClient, cookieHeader, assetIdOfEndpoint, queryParamMap);
        queryParamMap.clear();
    }

    @Test(groups = {"wso2.greg", "wso2.greg.governance.rest.api"}, description = "Create an endpoint and associate it " +
                                                                                 "with a REST service using name and " +
                                                                                 "version of REST service",
          dependsOnMethods = {"createEndpointAsAnAssociationWithUUID"})
    public void createEndpointAsAnAssociationWithNameAndVersion()
            throws IOException, InterruptedException, XPathExpressionException, JSONException {

        String governanceRestApiUrlForEndpointsAssociation = governanceRestApiUrlForEndpoints + "/restservices";
        queryParamMap.put("name", restServiceName);
        queryParamMap.put("version", restServiceVersion);
        String dataBody = GovernanceRestApiUtil.createEndpointDataBody(restTemplate, associationEndpointName, enviornment_qa);
        ClientResponse response = GovernanceRestApiUtil.createAsset(genericRestClient, dataBody, queryParamMap,
                                                                    headerMap, governanceRestApiUrlForEndpointsAssociation);
        Assert.assertTrue(response.getStatusCode() == HttpStatus.CREATED.getCode(),
                          "Wrong status code ,Expected 201 Created ,Received " + response.getStatusCode());
        String locationHeader = response.getHeaders().get("Location").get(0);
        String assetIdOfEndpoint = locationHeader.substring(locationHeader.lastIndexOf("/") + 1);
        Thread.sleep(1000);
        Map<String, String> headerMap = new HashMap<>();
        ClientResponse associationList = genericRestClient.geneticRestRequestGet(publisherUrl +
                                                                                 "/association/restservice/dependancies/"
                                                                                 + assetIdOfRestService, queryParamMap,
                                                                                 headerMap, cookieHeader);
        JsonArray jsonObject = new JsonParser().parse(associationList.getEntity(String.class)).
                getAsJsonObject().get("results").getAsJsonArray();
        assertTrue(jsonObject.toString().contains(assetIdOfEndpoint));
        assertTrue(jsonObject.toString().contains(associationEndpointName));
        setTestEnvironment();
        //delete the endpoint in the end
        queryParamMap.put("type", "endpoint");
        deleteAssetById(publisherUrl, genericRestClient, cookieHeader, assetIdOfEndpoint, queryParamMap);
        queryParamMap.clear();
    }

    //Blocked due to https://wso2.org/jira/browse/REGISTRY-3142
    @Test(groups = {"wso2.greg", "wso2.greg.governance.rest.api"}, description = "Get an endpoint lifecycle status",
          dependsOnMethods = {"createEndpointAsAnAssociationWithNameAndVersion"})
    public void getEndpointState() throws JSONException {

        String governanceRestApiUrlForEndpointState = governaceAPIUrl + "/endpoints/" + endpointId1 + "/states";
        ClientResponse response = genericRestClient.geneticRestRequestGet(governanceRestApiUrlForEndpointState,
                                                                          queryParamMap, headerMap, null);
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        Assert.assertEquals(obj.get(endpointLifecycle), activeState);
    }

    @Test(groups = {"wso2.greg", "wso2.greg.governance.rest.api"}, description = "Deactivate an endpoint",
          dependsOnMethods = {"getEndpointState"})
    public void deactivateEndpoint()
            throws JSONException, RemoteException {

        String governanceRestApiUrlForDeactivate = governanceRestApiUrlForEndpoints + "/deactivate/" + endpointId1;
        ClientResponse clientResponse = genericRestClient.geneticRestRequestPost(governanceRestApiUrlForDeactivate,
                                                                                 MediaType.APPLICATION_JSON,
                                                                                 MediaType.APPLICATION_JSON, null,
                                                                                 queryParamMap, headerMap, null);
        searchEndpoint();
        Assert.assertEquals(lifeCycleName, endpointLifecycle);
        Assert.assertEquals(lifeCycleState, "Off");
    }

    @Test(groups = {"wso2.greg", "wso2.greg.governance.rest.api"}, description = "Deactivate an endpoint",
          dependsOnMethods = {"deactivateEndpoint"})
    public void activateEndpoint()
            throws JSONException, RemoteException {

        String governanceRestApiUrlForDeactivate = governanceRestApiUrlForEndpoints + "/activate/" + endpointId1;
        ClientResponse clientResponse = genericRestClient.geneticRestRequestPost(governanceRestApiUrlForDeactivate,
                                                                                 MediaType.APPLICATION_JSON,
                                                                                 MediaType.APPLICATION_JSON, null,
                                                                                 queryParamMap, headerMap, null);
        searchEndpoint();
        Assert.assertEquals(lifeCycleName, endpointLifecycle);
        Assert.assertEquals(lifeCycleState, "Active");
    }

    @Test(groups = {"wso2.greg", "wso2.greg.governance.rest.api"}, description = "Delete an endpoint",
          dependsOnMethods = {"activateEndpoint"})
    public void deleteAnEndpoint() {

        String governanceRestApiUrlForEndpointDelete = governanceRestApiUrlForEndpoints + "/" + endpointId1;
        genericRestClient.geneticRestRequestDelete(governanceRestApiUrlForEndpointDelete,
                                                   MediaType.APPLICATION_JSON,
                                                   MediaType.APPLICATION_JSON, queryParamMap,
                                                   headerMap, null);
        String governanceRestApiUrl = governanceRestApiUrlForEndpoints + "/" + endpointId1;
        ClientResponse clientResponse = GovernanceRestApiUtil.getEndpointByID(genericRestClient, queryParamMap, headerMap,
                                                                              governanceRestApiUrl);
        //Check whether asset got deleted by checking the response for get method returns null
        Assert.assertNull(clientResponse.getEntity(String.class));
    }

    /**
     * This method get the list of endpoints and filters the lifecycle detail of endpoint1.
     *
     * @throws JSONException
     */
    private void searchEndpoint() throws JSONException {

        Map<String, String> queryParamMap = new HashMap<>();
        queryParamMap.put("type", "endpoint");
        ClientResponse clientResponse = searchAssetByQuery(publisherUrl, genericRestClient, cookieHeader, queryParamMap);
        JSONObject obj = new JSONObject(clientResponse.getEntity(String.class));
        JSONArray jsonArray = obj.getJSONArray("list");
        for (int i = 0; i < jsonArray.length(); i++) {
            String name = (String) jsonArray.getJSONObject(i).get("name");
            if (endpoint1Name.equals(name)) {
                lifeCycleName = (String) jsonArray.getJSONObject(i).get("lifecycle");
                lifeCycleState = (String) jsonArray.getJSONObject(i).get("lifecycleState");
                break;
            }
        }
    }



    /**
     * This method creates a rest service.This is later used to associate an endpoint.
     *
     * @return asset id of the created rest service
     * @throws IOException
     * @throws JSONException
     */
    private String createRestService() throws IOException, JSONException {
        Map<String, String> queryParamMap = new HashMap<>();
        queryParamMap.put("type", "restservice");
        String restTemplate = readFile(resourcePath + "json" + File.separator + "restservice-sample.json");
        String dataBody = String.format(restTemplate, restServiceName, "wso2", "/rest", restServiceVersion);
        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/assets",
                                                         MediaType.APPLICATION_JSON,
                                                         MediaType.APPLICATION_JSON, dataBody
                        , queryParamMap, headerMap, cookieHeader);
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        Assert.assertTrue((response.getStatusCode() == 201),
                          "Wrong status code ,Expected 201 Created ,Received " +
                          response.getStatusCode());
        String assetId = (String) obj.get("id");
        Assert.assertNotNull(assetId, "Empty asset resource id available" +
                                      response.getEntity(String.class));
        return assetId;
    }

    /**
     * This method creates the cookieHeader needed fot publisher REST api calls
     *
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
    public void cleanUp() throws RegistryException, JSONException, XPathExpressionException {
        Map<String, String> queryParamMap = new HashMap<>();
        setTestEnvironment();
        queryParamMap.put("type", "endpoint");
        deleteAssetById(publisherUrl, genericRestClient, cookieHeader, endpointId2, queryParamMap);
        //delete the rest service created
        queryParamMap.clear();
        queryParamMap.put("type", "restservice");
        deleteAssetById(publisherUrl, genericRestClient, cookieHeader, assetIdOfRestService, queryParamMap);
    }

}
