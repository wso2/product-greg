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
package org.wso2.carbon.registry.es.notifications;

import org.apache.commons.httpclient.methods.PostMethod;
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
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.es.utils.FileUploadWithAttachmentUtil;
import org.wso2.carbon.registry.es.utils.GregESTestBaseTest;
import org.wso2.greg.integration.common.utils.GenericRestClient;
import org.wso2.greg.integration.common.clients.LifeCycleAdminServiceClient;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * This class tests the subscription functionality of swaggers by uploading them to G-Reg.
 */
@SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
public class SwaggerPublisherFileUploadTestCase extends GregESTestBaseTest {

    private TestUserMode userMode;
    private String assetId;
    private String path;
    private String cookieHeader;
    private GenericRestClient genericRestClient;
    private Map<String, String> queryParamMap;
    private Map<String, String> headerMap;
    private String publisherUrl;
    private String resourcePath;
    private String assetName;
    private LifeCycleAdminServiceClient lifeCycleAdminServiceClient;
    private String lifeCycleName;
    private static final String SWAGGER_UPLOAD_API_URL = "assets/swagger/apis/swaggers";

    @Factory(dataProvider = "userModeProvider")
    public SwaggerPublisherFileUploadTestCase(TestUserMode userMode) {
        this.userMode = userMode;
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(userMode);
        genericRestClient = new GenericRestClient();
        queryParamMap = new HashMap<String, String>();
        headerMap = new HashMap<String, String>();
        resourcePath = FrameworkPathUtil.getSystemResourceLocation()
                       + "artifacts" + File.separator + "GREG" + File.separator;
        publisherUrl = publisherContext.getContextUrls()
                .getSecureServiceUrl().replace("services", "publisher/apis");
        //need lifeCycleAdminServiceClient to attach a lifecycle to the WSDL, as WSDLs does not come with
        //a default lifecycle attached
        lifeCycleAdminServiceClient = new LifeCycleAdminServiceClient(backendURL, sessionCookie);
        lifeCycleName = "ServiceLifeCycle";
        setTestEnvironment();
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "upload a swagger and attach a lifecycle")
    public void createSwaggerAssetWithLC()
            throws JSONException, InterruptedException, IOException,
                   CustomLifecyclesChecklistAdminServiceExceptionException {
        queryParamMap.put("type", "swagger");
        String wsdlFilePath = resourcePath + "swagger" + File.separator + "swagger2.json";
        assetName = "swagger2.json";
        //The api-for policy upload path is  in format of https://localhost:9443/publisher/assets/swagger/apis/swaggers
        String url = publisherUrl.replace("apis", SWAGGER_UPLOAD_API_URL);
        PostMethod httpMethod = FileUploadWithAttachmentUtil.uploadContentTypeAssets(wsdlFilePath, "1.0.0", assetName,
                                                                                     "swagger", cookieHeader, url);
        Assert.assertTrue((httpMethod.getStatusCode() == 200),
                          "Wrong status code ,Expected 200 ,Received " +
                          httpMethod.getStatusCode());
        searchSwaggerAsset();
        //attach a LC to the swagger
        lifeCycleAdminServiceClient.addAspect(path, lifeCycleName);
        Assert.assertNotNull(assetId, "Empty asset resource id available");
        Assert.assertTrue(this.getAsset(assetId).get("lifecycle")
                                  .equals(lifeCycleName), "LifeCycle not assigned to given asset");

    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Adding subscription to a swagger LC check list item check",
          dependsOnMethods = {"createSwaggerAssetWithLC"})
    public void addSubscriptionForLCCheckListItemCheck() throws JSONException, IOException {

        JSONObject dataObject = new JSONObject();

        dataObject.put("notificationType", "PublisherCheckListItemChecked");
        dataObject.put("notificationMethod", "work");

        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/subscriptions/swagger/" + assetId, MediaType.APPLICATION_JSON,
                                                         MediaType.APPLICATION_JSON, dataObject.toString()
                        , queryParamMap, headerMap, cookieHeader);

        assertTrue((response.getStatusCode() == Response.Status.OK.getStatusCode()),
                   "Wrong status code ,Expected" + Response.Status.OK.getStatusCode() + "Created ,Received " +
                   response.getStatusCode());
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Adding subscription to a swagger LC check list item uncheck",
          dependsOnMethods = {"addSubscriptionForLCCheckListItemCheck"})
    public void addSubscriptionForLCCheckListItemUnCheck() throws JSONException, IOException {

        JSONObject dataObject = new JSONObject();

        dataObject.put("notificationType", "PublisherCheckListItemUnchecked");
        dataObject.put("notificationMethod", "work");

        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/subscriptions/swagger/" + assetId, MediaType.APPLICATION_JSON,
                                                         MediaType.APPLICATION_JSON, dataObject.toString()
                        , queryParamMap, headerMap, cookieHeader);

        assertTrue((response.getStatusCode() == Response.Status.OK.getStatusCode()),
                   "Wrong status code ,Expected" + Response.Status.OK.getStatusCode() + "Created ,Received " +
                   response.getStatusCode());
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Adding subscription to a swagger LC state change",
          dependsOnMethods = {"addSubscriptionForLCCheckListItemUnCheck"})
    public void addSubscriptionForLCStateChange() throws JSONException, IOException {

        JSONObject dataObject = new JSONObject();

        dataObject.put("notificationType", "PublisherLifeCycleStateChanged");
        dataObject.put("notificationMethod", "work");

        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/subscriptions/swagger/" + assetId, MediaType.APPLICATION_JSON,
                                                         MediaType.APPLICATION_JSON, dataObject.toString()
                        , queryParamMap, headerMap, cookieHeader);

        assertTrue((response.getStatusCode() == Response.Status.OK.getStatusCode()),
                   "Wrong status code ,Expected" + Response.Status.OK.getStatusCode() + "Created ,Received " +
                   response.getStatusCode());
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Swagger LC check list item check",
          dependsOnMethods = {"addSubscriptionForLCCheckListItemCheck", "addSubscriptionForLCStateChange"})
    public void checkLCCheckItemsOnSwagger() throws JSONException, IOException {
        queryParamMap.put("type", "swagger");
        queryParamMap.put("lifecycle", lifeCycleName);
        JSONObject LCStateobj = getLifeCycleState(assetId, "swagger");
        JSONObject dataObj = LCStateobj.getJSONObject("data");
        JSONArray checkItems = dataObj.getJSONArray("checkItems");
        Assert.assertEquals(((JSONObject) checkItems.get(0)).getString("isVisible"), "true");
        ClientResponse responseCheck0 =
                checkLifeCycleCheckItem(cookieHeader, 0);
        Assert.assertTrue(responseCheck0.getStatusCode() == 200);
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Swagger LC check list item check",
          dependsOnMethods = {"addSubscriptionForLCCheckListItemUnCheck", "checkLCCheckItemsOnSwagger"})
    public void uncheckLCCheckItemsOnSwagger() throws JSONException, IOException {
        queryParamMap.put("type", "swagger");
        queryParamMap.put("lifecycle", lifeCycleName);
        JSONObject LCStateobj = getLifeCycleState(assetId, "swagger");
        JSONObject dataObj = LCStateobj.getJSONObject("data");
        JSONArray checkItems = dataObj.getJSONArray("checkItems");
        Assert.assertEquals(((JSONObject) checkItems.get(0)).getString("isVisible"), "true");
        ClientResponse responseUncheck0 =
                uncheckLifeCycleCheckItem(cookieHeader, 0);
        Assert.assertTrue(responseUncheck0.getStatusCode() == 200);
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Change LC state on Swagger",
          dependsOnMethods = {"addSubscriptionForLCStateChange", "uncheckLCCheckItemsOnSwagger"})
    public void changeLCStateSwagger() throws JSONException, IOException {
        String stateChangeMessage = " State changed successfully to Testing!";
        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/assets/" + assetId + "/state",
                                                         MediaType.APPLICATION_FORM_URLENCODED,
                                                         MediaType.APPLICATION_JSON,
                                                         "nextState=Testing&comment=Completed"
                        , queryParamMap, headerMap, cookieHeader);
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        String status = obj.get("status").toString();
        Assert.assertEquals(status, stateChangeMessage);
    }

    private void setTestEnvironment() throws XPathExpressionException, JSONException {
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
     * This method get all the swaggers in publisher and select the one created by createSwaggerAssetWithLC method.
     *
     * @throws JSONException
     */
    public void searchSwaggerAsset() throws JSONException {
        Map<String, String> queryParamMap = new HashMap<>();
        queryParamMap.put("type", "swagger");
        ClientResponse clientResponse = searchAssetByQuery(publisherUrl, genericRestClient, cookieHeader, queryParamMap);
        JSONObject obj = new JSONObject(clientResponse.getEntity(String.class));
        JSONArray jsonArray = obj.getJSONArray("list");
        for (int i = 0; i < jsonArray.length(); i++) {
            String name = (String) jsonArray.getJSONObject(i).get("name");
            if (assetName.equals(name)) {
                assetId = (String) jsonArray.getJSONObject(i).get("id");
                path = (String) jsonArray.getJSONObject(i).get("path");
                break;
            }
        }
    }

    private JSONObject getAsset(String assetId) throws JSONException {
        ClientResponse clientResponse = getAssetById(publisherUrl, genericRestClient, cookieHeader, assetId, queryParamMap);
        return new JSONObject(clientResponse.getEntity(String.class));
    }

    private ClientResponse checkLifeCycleCheckItem(String managerCookieHeader, int itemId) {
        return genericRestClient.geneticRestRequestPost(publisherUrl + "/asset/" + assetId + "/update-checklist",
                                                        MediaType.APPLICATION_JSON,
                                                        MediaType.APPLICATION_JSON,
                                                        "{\"checklist\":[{\"index\":" + itemId + ",\"checked\":true}]}"
                , queryParamMap, headerMap, managerCookieHeader);
    }

    private JSONObject getLifeCycleState(String assetId, String assetType) throws JSONException {
        Map<String, String> assetTypeParamMap = new HashMap<String, String>();
        assetTypeParamMap.put("type", assetType);
        assetTypeParamMap.put("lifecycle", lifeCycleName);
        ClientResponse response =
                genericRestClient.geneticRestRequestGet
                        (publisherUrl + "/asset/" + assetId + "/state"
                                , queryParamMap, headerMap, cookieHeader);
        return new JSONObject(response.getEntity(String.class));
    }

    private ClientResponse uncheckLifeCycleCheckItem(String managerCookieHeader, int itemId) {
        return genericRestClient.geneticRestRequestPost(publisherUrl + "/asset/" + assetId + "/update-checklist",
                                                        MediaType.APPLICATION_JSON,
                                                        MediaType.APPLICATION_JSON,
                                                        "{\"checklist\":[{\"index\":" + itemId + ",\"checked\":false}]}"
                , queryParamMap, headerMap, managerCookieHeader);
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws RegistryException, JSONException {
        Map<String, String> assocUUIDMap;
        Map<String, String> queryParamMap = new HashMap<>();
        queryParamMap.put("type", "swagger");
        assocUUIDMap = getAssociationsFromPages(publisherUrl, genericRestClient, cookieHeader, assetId, queryParamMap);
        deleteAssetById(publisherUrl, genericRestClient, cookieHeader, assetId, queryParamMap);
        deleteAllAssociationsById(publisherUrl, genericRestClient, cookieHeader, assetId, queryParamMap);
        queryParamMap.clear();
        for (String uuid : assocUUIDMap.keySet()) {
            queryParamMap.put("type", getType(assocUUIDMap.get(uuid)));
            deleteAssetById(publisherUrl, genericRestClient, cookieHeader, uuid, queryParamMap);
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
