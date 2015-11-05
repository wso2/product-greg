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
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.es.utils.ESTestCommonUtils;
import org.wso2.carbon.registry.es.utils.GregESTestBaseTest;
import org.wso2.greg.integration.common.clients.LifeCycleAdminServiceClient;
import org.wso2.greg.integration.common.utils.GenericRestClient;

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
 * This class tests subscriptions and notifications on publisher console for WSDLs
 */
public class WSDLNotificationAndSubscriptionTestCase extends GregESTestBaseTest {

    private static final Log log = LogFactory.getLog(WSDLNotificationAndSubscriptionTestCase.class);
    private TestUserMode userMode;
    String jSessionId;
    String assetId;
    String path;
    String cookieHeader;
    GenericRestClient genericRestClient;
    Map<String, String> queryParamMap;
    Map<String, String> headerMap;
    String publisherUrl;
    String resourcePath;
    String assetName;
    ESTestCommonUtils crudTestCommonUtils;
    LifeCycleAdminServiceClient lifeCycleAdminServiceClient;
    String lifeCycleName;
    String stateChangeMessage = " State changed successfully to Testing!";

    @Factory(dataProvider = "userModeProvider")
    public WSDLNotificationAndSubscriptionTestCase(TestUserMode userMode) {
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
        publisherUrl = automationContext.getContextUrls()
                .getSecureServiceUrl().replace("services", "publisher/apis");
        //need lifeCycleAdminServiceClient to attach a lifecycle to the WSDL, as WSDLs does not come with
        //a default lifecycle attached
        lifeCycleAdminServiceClient = new LifeCycleAdminServiceClient(backendURL, sessionCookie);
        crudTestCommonUtils = new ESTestCommonUtils(genericRestClient, publisherUrl, headerMap);
        lifeCycleName = "ServiceLifeCycle";
        setTestEnvironment();
    }



    @AfterClass(alwaysRun = true)
    public void cleanUp() throws RegistryException {

    }

    private void setTestEnvironment() throws XPathExpressionException, JSONException {
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
        crudTestCommonUtils.setCookieHeader(cookieHeader);
    }

    /*@Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Authenticate Publisher test")
    public void authenticatePublisher() throws JSONException {
        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/authenticate/",
                                                         MediaType.APPLICATION_FORM_URLENCODED,
                                                         MediaType.APPLICATION_JSON,
                                                         "username=admin&password=admin"
                        , queryParamMap, headerMap, null);
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        assertTrue((response.getStatusCode() == 200),
                   "Wrong status code ,Expected 200 OK ,Received " +
                   response.getStatusCode()
        );
        jSessionId = obj.getJSONObject("data").getString("sessionId");
        cookieHeader = "JSESSIONID=" + jSessionId;
        assertNotNull(jSessionId, "Invalid JSessionID received");
        crudTestCommonUtils.setCookieHeader(cookieHeader);
    }*/

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "create a wsdl with a LC attached.")
    public void createWSDLAssetWithLC()
            throws JSONException, InterruptedException, IOException,
                   CustomLifecyclesChecklistAdminServiceExceptionException {
        queryParamMap.put("type", "wsdl");
        String dataBody = readFile(resourcePath + "json" + File.separator
                                   + "publisherPublishWSDLResource.json");
        assetName = (String) (new JSONObject(dataBody)).get("overview_name");
        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/assets",
                                                         MediaType.APPLICATION_JSON,
                                                         MediaType.APPLICATION_JSON, dataBody
                        , queryParamMap, headerMap, cookieHeader);
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        Assert.assertTrue((response.getStatusCode() == 201),
                          "Wrong status code ,Expected 201 Created ,Received " +
                          response.getStatusCode());
        String resultName = obj.get("overview_name").toString();
        Assert.assertEquals(resultName, assetName);
        searchWsdlAsset();
        //attach a LC to the wsdl
        lifeCycleAdminServiceClient.addAspect(path, lifeCycleName);
        Assert.assertNotNull(assetId, "Empty asset resource id available" +
                                      response.getEntity(String.class));
        Assert.assertTrue(this.getAsset(assetId, "wsdl").get("lifecycle")
                                  .equals(lifeCycleName), "LifeCycle not assigned to given asset");
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Adding subscription to a wsdl LC check list item check")
    public void addSubscriptionForLCCheckListItemCheck() throws JSONException, IOException {

        JSONObject dataObject = new JSONObject();

        dataObject.put("notificationType", "PublisherCheckListItemChecked");
        dataObject.put("notificationMethod", "work");

        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/subscriptions/wsdl/" + assetId, MediaType.APPLICATION_JSON,
                                                         MediaType.APPLICATION_JSON, dataObject.toString()
                        , queryParamMap, headerMap, cookieHeader);

        assertTrue((response.getStatusCode() == Response.Status.OK.getStatusCode()),
                   "Wrong status code ,Expected" + Response.Status.OK.getStatusCode() + "Created ,Received " +
                   response.getStatusCode());
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Adding subscription to a wsdl LC check list item uncheck",
          dependsOnMethods = {"createWSDLAssetWithLC"})
    public void addSubscriptionForLCCheckListItemUnCheck() throws JSONException, IOException {

        JSONObject dataObject = new JSONObject();

        dataObject.put("notificationType", "PublisherCheckListItemUnchecked");
        dataObject.put("notificationMethod", "work");

        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/subscriptions/wsdl/" + assetId, MediaType.APPLICATION_JSON,
                                                         MediaType.APPLICATION_JSON, dataObject.toString()
                        , queryParamMap, headerMap, cookieHeader);

        assertTrue((response.getStatusCode() == Response.Status.OK.getStatusCode()),
                   "Wrong status code ,Expected" + Response.Status.OK.getStatusCode() + "Created ,Received " +
                   response.getStatusCode());
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Adding subscription to a wsdl LC state change",
          dependsOnMethods = {"createWSDLAssetWithLC"})
    public void addSubscriptionForLCStateChange() throws JSONException, IOException {

        JSONObject dataObject = new JSONObject();

        dataObject.put("notificationType", "PublisherLifeCycleStateChanged");
        dataObject.put("notificationMethod", "work");

        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/subscriptions/wsdl/" + assetId, MediaType.APPLICATION_JSON,
                                                         MediaType.APPLICATION_JSON, dataObject.toString()
                        , queryParamMap, headerMap, cookieHeader);

        assertTrue((response.getStatusCode() == Response.Status.OK.getStatusCode()),
                   "Wrong status code ,Expected" + Response.Status.OK.getStatusCode() + "Created ,Received " +
                   response.getStatusCode());
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "WSDL LC check list item check",
          dependsOnMethods = {"createWSDLAssetWithLC", "addSubscriptionForLCCheckListItemCheck"})
    public void checkLCCheckItemsOnWSDL() throws JSONException, IOException {
        queryParamMap.put("type", "wsdl");
        queryParamMap.put("lifecycle", lifeCycleName);
        JSONObject LCStateobj = getLifeCycleState(assetId, "wsdl");
        JSONObject dataObj = LCStateobj.getJSONObject("data");
        JSONArray checkItems = dataObj.getJSONArray("checkItems");
        Assert.assertEquals(((JSONObject) checkItems.get(0)).getString("isVisible"), "true");
        Assert.assertEquals(((JSONObject) checkItems.get(1)).getString("isVisible"), "true");
        Assert.assertEquals(((JSONObject) checkItems.get(2)).getString("isVisible"), "true");
        //check LC check items
        ClientResponse responseCheck0 =
                checkLifeCycleCheckItem(cookieHeader, 0);
        ClientResponse responseCheck1 =
                checkLifeCycleCheckItem(cookieHeader, 1);
        ClientResponse responseCheck2 =
                checkLifeCycleCheckItem(cookieHeader, 2);
        Assert.assertTrue(responseCheck0.getStatusCode() == 200);
        Assert.assertTrue(responseCheck1.getStatusCode() == 200);
        Assert.assertTrue(responseCheck2.getStatusCode() == 200);

    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Change LC state on WSDL",
          dependsOnMethods = {"createWSDLAssetWithLC", "addSubscriptionForLCStateChange", "checkLCCheckItemsOnWSDL"})
    public void changeLCStateWSDL() throws JSONException, IOException {
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

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "WSDL LC check list item check",
          dependsOnMethods = {"createWSDLAssetWithLC", "addSubscriptionForLCCheckListItemUnCheck", "checkLCCheckItemsOnWSDL", "changeLCStateWSDL"})
    public void uncheckLCCheckItemsOnWSDL() throws JSONException, IOException {
        queryParamMap.put("type", "wsdl");
        queryParamMap.put("lifecycle", lifeCycleName);
        JSONObject LCStateobj = getLifeCycleState(assetId, "wsdl");
        JSONObject dataObj = LCStateobj.getJSONObject("data");
        JSONArray checkItems = dataObj.getJSONArray("checkItems");
        Assert.assertEquals(((JSONObject) checkItems.get(0)).getString("isVisible"), "true");
        Assert.assertEquals(((JSONObject) checkItems.get(1)).getString("isVisible"), "true");
        Assert.assertEquals(((JSONObject) checkItems.get(2)).getString("isVisible"), "true");
        //check LC check items
        checkLifeCycleCheckItem(cookieHeader, 0);
        checkLifeCycleCheckItem(cookieHeader, 1);
        checkLifeCycleCheckItem(cookieHeader, 2);
        //uncheck LC check items
        ClientResponse responseUncheck0 =
                uncheckLifeCycleCheckItem(cookieHeader, 0);
        ClientResponse responseUncheck1 =
                uncheckLifeCycleCheckItem(cookieHeader, 1);
        ClientResponse responseUncheck2 =
                uncheckLifeCycleCheckItem(cookieHeader, 2);
        Assert.assertTrue(responseUncheck0.getStatusCode() == 200);
        Assert.assertTrue(responseUncheck1.getStatusCode() == 200);
        Assert.assertTrue(responseUncheck2.getStatusCode() == 200);

    }

    /**
     * This method get all the wsdls in publisher and select the one created by createWSDLAssetWithLC method.
     *
     * @throws JSONException
     */
    public void searchWsdlAsset() throws JSONException {
        Map<String, String> queryParamMap = new HashMap<>();
        queryParamMap.put("type", "wsdl");
        ClientResponse clientResponse = crudTestCommonUtils.searchAssetByQuery(queryParamMap);
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

    private JSONObject getAsset(String assetId, String assetType) throws JSONException {
        Map<String, String> assetTypeParamMap = new HashMap<String, String>();
        assetTypeParamMap.put("type", assetType);
        ClientResponse clientResponse = crudTestCommonUtils.getAssetById(assetId, queryParamMap);
        return new JSONObject(clientResponse.getEntity(String.class));
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

    private ClientResponse checkLifeCycleCheckItem(String managerCookieHeader, int itemId) {
        return genericRestClient.geneticRestRequestPost(publisherUrl + "/asset/" + assetId + "/update-checklist",
                                                        MediaType.APPLICATION_JSON,
                                                        MediaType.APPLICATION_JSON,
                                                        "{\"checklist\":[{\"index\":" + itemId + ",\"checked\":true}]}"
                , queryParamMap, headerMap, managerCookieHeader);
    }

    private ClientResponse uncheckLifeCycleCheckItem(String managerCookieHeader, int itemId) {
        return genericRestClient.geneticRestRequestPost(publisherUrl + "/asset/" + assetId + "/update-checklist",
                                                        MediaType.APPLICATION_JSON,
                                                        MediaType.APPLICATION_JSON,
                                                        "{\"checklist\":[{\"index\":" + itemId + ",\"checked\":false}]}"
                , queryParamMap, headerMap, managerCookieHeader);
    }

    @DataProvider
    private static TestUserMode[][] userModeProvider() {
        return new TestUserMode[][]{
                new TestUserMode[]{TestUserMode.SUPER_TENANT_ADMIN}
//                new TestUserMode[]{TestUserMode.TENANT_USER},
        };
    }
}
