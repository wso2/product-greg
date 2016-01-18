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
package org.wso2.carbon.registry.es.publisher.associations;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.apache.wink.client.ClientResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.*;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.es.utils.GregESTestBaseTest;
import org.wso2.greg.integration.common.utils.GenericRestClient;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.lang.Exception;
import java.lang.InterruptedException;
import java.lang.String;
import java.lang.Thread;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * This class can be used to test Owned By association between two rest services
 */

@SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
public class GregRestResourceOwnedByAssociationTestCase extends GregESTestBaseTest {

    private TestUserMode userMode;
    private String spaceAssetId;
    private String testAssetId;
    private String cookieHeader;
    private GenericRestClient genericRestClient;
    private Map<String, String> queryParamMap;
    private Map<String, String> headerMap;
    private String publisherUrl;
    private String resourcePath;
    private String type = "restservice";

    @Factory(dataProvider = "userModeProvider")
    public GregRestResourceOwnedByAssociationTestCase(TestUserMode userMode) {
        this.userMode = userMode;
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(userMode);
        genericRestClient = new GenericRestClient();
        queryParamMap = new HashMap<String, String>();
        headerMap = new HashMap<String, String>();
        resourcePath =
                FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator + "GREG" + File.separator;
        publisherUrl = publisherContext.getContextUrls().getSecureServiceUrl().replace("services", "publisher/apis");
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws RegistryException {

        deleteAsset(spaceAssetId, publisherUrl, cookieHeader, type, genericRestClient);
        deleteAsset(testAssetId, publisherUrl, cookieHeader, type, genericRestClient);
    }


    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Authenticate Publisher test")
    public void authenticatePublisher() throws JSONException, XPathExpressionException {

        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/authenticate/",
                        MediaType.APPLICATION_FORM_URLENCODED,
                        MediaType.APPLICATION_JSON,
                        "username=" + automationContext.getContextTenant().getContextUser().getUserName() +
                                "&password=" + automationContext.getContextTenant().getContextUser().getPassword()
                        , queryParamMap, headerMap, null
                );
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        assertTrue((response.getStatusCode() == Response.Status.OK.getStatusCode()),
                "Wrong status code ,Expected 200 OK ,Received " +
                        response.getStatusCode()
        );
        String jSessionId = obj.getJSONObject("data").getString("sessionId");
        cookieHeader = "JSESSIONID=" + jSessionId;
        assertNotNull(jSessionId, "Invalid JSessionID received");
    }


    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Create Test Rest Service"
            , dependsOnMethods = "authenticatePublisher")
    public void createTestRestServices() throws JSONException, IOException {
        queryParamMap.put("type", "restservice");
        String dataBody = readFile(resourcePath + "json" + File.separator + "publisherPublishRestResource.json");
        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/assets",
                        MediaType.APPLICATION_JSON,
                        MediaType.APPLICATION_JSON, dataBody
                        , queryParamMap, headerMap, cookieHeader);
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        assertTrue((response.getStatusCode() == Response.Status.CREATED.getStatusCode()),
                "Wrong status code ,Expected 201 Created ,Received " +
                        response.getStatusCode()
        );
        testAssetId = obj.get("id").toString();
        assertNotNull(testAssetId, "Empty asset resource id available" +
                response.getEntity(String.class));
    }


    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Create Space Rest Service",
            dependsOnMethods = {"createTestRestServices"})
    public void createSpaceRestServices() throws JSONException, IOException {
        queryParamMap.put("type", "restservice");
        String dataBody = readFile(resourcePath + "json" + File.separator + "publisherPublishRestSpaceResource.json");
        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/assets",
                        MediaType.APPLICATION_JSON,
                        MediaType.APPLICATION_JSON, dataBody
                        , queryParamMap, headerMap, cookieHeader);
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        assertTrue((response.getStatusCode() == Response.Status.CREATED.getStatusCode()),
                "Wrong status code ,Expected 201 Created ,Received " +
                        response.getStatusCode()
        );
        spaceAssetId = obj.get("id").toString();
        assertNotNull(spaceAssetId, "Empty asset resource id available" +
                response.getEntity(String.class));
    }

   @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Create Space Rest Service",
            dependsOnMethods = {"createSpaceRestServices"})
    public void createAssociationOwnedBy() throws JSONException, IOException, InterruptedException {

        JSONObject dataObject = new JSONObject();

        dataObject.put("associationType", "ownedBy");
        dataObject.put("destType", type);
        dataObject.put("sourceType", type);
        dataObject.put("destUUID", testAssetId);
        dataObject.put("sourceUUID", spaceAssetId);

        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/association", MediaType.APPLICATION_JSON,
                        MediaType.APPLICATION_JSON, dataObject.toString()
                        , queryParamMap, headerMap, cookieHeader);

        assertTrue((response.getStatusCode() == Response.Status.OK.getStatusCode()),
                "Wrong status code ,Expected" + Response.Status.OK.getStatusCode() + "Created ,Received " +
                        response.getStatusCode()
        );

        Thread.sleep(3000);

        ClientResponse associationList = genericRestClient.geneticRestRequestGet(publisherUrl +
                "/association/restservice/ownedBy/" + spaceAssetId, queryParamMap, headerMap, cookieHeader);


        JsonArray jsonObject = new JsonParser().parse(associationList.getEntity(String.class)).
                getAsJsonObject().get("results").getAsJsonArray();


        assertTrue(jsonObject.toString().contains("uuid"), "Response does not contain uuid property");
        assertTrue(jsonObject.toString().contains(testAssetId),"Response does not contain testAssetId "+ testAssetId);

    }

    @DataProvider
    private static TestUserMode[][] userModeProvider() {
        return new TestUserMode[][]{
                new TestUserMode[]{TestUserMode.SUPER_TENANT_ADMIN}
//                new TestUserMode[]{TestUserMode.TENANT_USER},
        };
    }


}
