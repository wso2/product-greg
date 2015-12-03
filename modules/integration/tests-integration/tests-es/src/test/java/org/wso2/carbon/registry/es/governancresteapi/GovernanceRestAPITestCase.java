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
import org.wso2.carbon.registry.es.utils.GregESTestBaseTest;
import org.wso2.greg.integration.common.utils.GenericRestClient;

import javax.ws.rs.core.MediaType;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class GovernanceRestAPITestCase extends GregESTestBaseTest {

    private TestUserMode userMode;
    private GenericRestClient genericRestClient;
    private Map<String, String> queryParamMap;
    private Map<String, String> headerMap;
    private String governaceAPIUrl;
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String AUTHORIZATION_HEADER_VALUE = "Basic YWRtaW46YWRtaW4=";
    private String resourcePath;
    private String publisherUrl;
    private String cookieHeader;
    private ArrayList<String> assetId;

    @Factory(dataProvider = "userModeProvider")
    public GovernanceRestAPITestCase(TestUserMode userMode) {
        this.userMode = userMode;
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(userMode);
        genericRestClient = new GenericRestClient();
        queryParamMap = new HashMap<String, String>();
        headerMap = new HashMap<String, String>();
        governaceAPIUrl = automationContext.getContextUrls()
                .getSecureServiceUrl().replace("services", "governance");
        resourcePath = FrameworkPathUtil.getSystemResourceLocation()
                       + "artifacts" + File.separator + "GREG" + File.separator;
        publisherUrl = automationContext.getContextUrls()
                .getSecureServiceUrl().replace("services", "publisher/apis");
        assetId = new ArrayList<String>();
    }

    @Test(groups = {"wso2.greg", "wso2.greg.governance.rest.api"}, description = "Get list of available artifact types")
    public void getAssetTypes() throws JSONException {

        headerMap.put(AUTHORIZATION_HEADER,AUTHORIZATION_HEADER_VALUE);
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

    @Test(groups = {"wso2.greg", "wso2.greg.governance.rest.api"}, description = "Get list of available rest services")
    public void getListOfRestServices()
            throws JSONException, IOException, XPathExpressionException, InterruptedException {
        //create two rest services
        int numOfRestServices = 2;
        String restService1Name = "restService1";
        String restService2Name = "restService2";
        String context1 = "/rest1";
        String context2 = "/rest2";
        createRestService(restService1Name,"wso2",context1,"1.0.0");
        createRestService(restService2Name,"wso2",context2,"1.0.0");
        Thread.sleep(1000);//for resource indexing
        queryParamMap.clear();
        headerMap.put(AUTHORIZATION_HEADER,AUTHORIZATION_HEADER_VALUE);
        ClientResponse listOfRestServices =
                genericRestClient.geneticRestRequestGet
                        (governaceAPIUrl + "/restservices"
                                , queryParamMap, headerMap, null);
        JSONObject jsonObject = new JSONObject(listOfRestServices.getEntity(String.class));
        JSONArray jsonArray = jsonObject.getJSONArray("assets");
        Assert.assertEquals(numOfRestServices,jsonArray.length(),"Wrong number of rest services. Expected " +
                                                                 numOfRestServices+ " But received "+ jsonArray.length());
        for(int i=0;i<jsonArray.length();i++){
            String name = (String) jsonArray.getJSONObject(i).get("name");
            String context = (String) jsonArray.getJSONObject(i).get("context");
            String id = (String) jsonArray.getJSONObject(i).get("id");
            if(restService1Name.equals(name)){
                Assert.assertEquals(context1,context,"Incorrect context. Expected "+context1+" received "+context);
            }else if (restService2Name.equals(name)){
                Assert.assertEquals(context2,context,"Incorrect context. Expected "+context2+" received "+context);
            }
        }
    }

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

    /**
     * This method creates a restservice with the given parameters using REST client.
     * @param name
     * @param provider
     * @param context
     * @param version
     * @throws XPathExpressionException
     * @throws JSONException
     * @throws IOException
     */
    private void createRestService(String name, String provider, String context, String version) throws XPathExpressionException, JSONException, IOException {
        setTestEnvironment();
        headerMap.clear();
        queryParamMap.clear();
        queryParamMap.put("type", "restservice");
        String restTemplate = readFile(resourcePath + "json" + File.separator + "restservice-sample.json");
        String assetName = name;
        String dataBody = String.format(restTemplate, assetName, provider, context, version);
        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/assets",
                                                         MediaType.APPLICATION_JSON,
                                                         MediaType.APPLICATION_JSON, dataBody
                        , queryParamMap, headerMap, cookieHeader);
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        assetId.add((String)obj.get("id"));
    }


    @AfterClass(alwaysRun = true)
    public void cleanUp() throws RegistryException, JSONException {
        Map<String, String> queryParamMap = new HashMap<>();
        queryParamMap.put("type", "restservice");
        for(int i=0;i<assetId.size();i++) {
            deleteAssetById(publisherUrl, genericRestClient, cookieHeader, assetId.get(i), queryParamMap);
        }
    }

    @DataProvider
    private static TestUserMode[][] userModeProvider() {
        return new TestUserMode[][]{
                new TestUserMode[]{TestUserMode.SUPER_TENANT_ADMIN}
                //                new TestUserMode[]{TestUserMode.TENANT_USER},
        };
    }


}
