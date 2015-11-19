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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wink.client.ClientResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.ParseException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.registry.es.utils.GregESTestBaseTest;
import org.wso2.greg.integration.common.utils.GenericRestClient;

import javax.activation.DataHandler;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
<<<<<<< HEAD
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
=======
>>>>>>> 4bdd82c253befedf3a4238e239d0fb7420f08239

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class CustomRXTAssociationTestCase extends GregESTestBaseTest {

    private static final Log log = LogFactory.getLog(CustomRXTAssociationTestCase.class);

    private TestUserMode userMode;
    String jSessionId;
    String assetId;
    String testAssetId;
    String cookieHeader;
    GenericRestClient genericRestClient;
    Map<String, String> queryParamMap;
    Map<String, String> headerMap;
    String publisherUrl;
    String resourcePath;

    @Factory(dataProvider = "userModeProvider")
    public CustomRXTAssociationTestCase(TestUserMode userMode) {
        this.userMode = userMode;
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(userMode);
        String session = getSessionCookie();
        genericRestClient = new GenericRestClient();
        queryParamMap = new HashMap<String, String>();
        headerMap = new HashMap<String, String>();
        resourcePath =
                FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator + "GREG" + File.separator;
        publisherUrl = automationContext.getContextUrls().getSecureServiceUrl().replace("services", "publisher/apis");

        assertTrue(addNewRxtConfiguration("application.rxt", "application.rxt"),
                "Addition of new custom service rxt failed");
       // addCustomRxt();
        new ServerConfigurationManager(automationContext).restartGracefully();
        Thread.sleep(120000);
        setTestEnvironment();
    }

    private void setTestEnvironment() throws JSONException, IOException {
        // Authenticate
        ClientResponse response = genericRestClient
                .geneticRestRequestPost(publisherUrl + "/authenticate/", MediaType.APPLICATION_FORM_URLENCODED,
                        MediaType.APPLICATION_JSON, "username=admin&password=admin", queryParamMap, headerMap, null);
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        jSessionId = obj.getJSONObject("data").getString("sessionId");
        cookieHeader = "JSESSIONID=" + jSessionId;

        //Create rest service
        queryParamMap.put("type", "applications");
        String dataBody = readFile(resourcePath + "json" + File.separator + "publisherPublishCustomResource.json");
        ClientResponse createResponse = genericRestClient
                .geneticRestRequestPost(publisherUrl + "/assets", MediaType.APPLICATION_JSON,
                        MediaType.APPLICATION_JSON, dataBody, queryParamMap, headerMap, cookieHeader);
        JSONObject createObj = new JSONObject(createResponse.getEntity(String.class));
        assetId = createObj.get("id").toString();
        createTestRestServices();
    }

    private void createTestRestServices() throws JSONException, IOException {
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

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Create Association between Custom RXT and Rest Service")
    public void createAssociation() throws JSONException, IOException, ParseException, InterruptedException {

        JSONObject dataObject = new JSONObject();

        dataObject.put("associationType", "dependancies");
        dataObject.put("destType", "restservice");
        dataObject.put("sourceType", "applications");
        dataObject.put("destUUID", testAssetId);
        dataObject.put("sourceUUID", assetId);

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
                "/association/restservice/dependancies/" + assetId, queryParamMap, headerMap, cookieHeader);


        JsonArray jsonObject = new JsonParser().parse(associationList.getEntity(String.class)).
                getAsJsonObject().get("results").getAsJsonArray();


        assertTrue(jsonObject.toString().contains("uuid"));
        assertTrue(jsonObject.toString().contains(testAssetId));

    }

<<<<<<< HEAD
    @AfterClass
    public void clean() throws Exception {
        deleteAsset(testAssetId, publisherUrl, cookieHeader, "restservice", genericRestClient);
        assertTrue(deleteCustomRxtConfiguration("application.rxt"), "Deleting of added custom rxt encountered a failure");
=======
    private void deleteCustomAsset() throws JSONException {
        deleteAsset(assetId, publisherUrl, cookieHeader, "applications", genericRestClient);
    }

    private void deleteCustomRxt() throws Exception {
        String session = getSessionCookie();
        resourceAdminServiceClient = new ResourceAdminServiceClient(backendURL, session);
        resourceAdminServiceClient.deleteResource(
                "/_system/governance/repository/components/org.wso2.carbon.governance/types/application.rxt");
    }

    @AfterClass(alwaysRun = true)
    public void clean() throws Exception {
        this.cleanupAsset(genericRestClient, publisherUrl, testAssetId, cookieHeader, "restservice");
        deleteCustomAsset();
        deleteCustomRxt();
>>>>>>> 4bdd82c253befedf3a4238e239d0fb7420f08239
    }

    @DataProvider
    private static TestUserMode[][] userModeProvider() {
        return new TestUserMode[][]{
                new TestUserMode[]{TestUserMode.SUPER_TENANT_ADMIN}
                //                new TestUserMode[]{TestUserMode.TENANT_USER},
        };
    }
}
