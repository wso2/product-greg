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
package org.wso2.carbon.registry.es.publisher.lifecycle;

import org.apache.wink.client.ClientResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.logging.view.stub.LogViewerLogViewerException;
import org.wso2.carbon.registry.es.utils.GregESTestBaseTest;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GenericRestClient;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

@SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
public class GregPublisherPermissionCheckForLCOperations extends GregESTestBaseTest {

    public static final String MANAGER_TEST_ROLE = "manager";

    public static final String READ_ACTION = "2";
    public static final String WRITE_ACTION = "3";
    public static final String DELETE_ACTION = "4";
    public static final String AUTHORIZE_ACTION = "5";

    public static final String PERMISSION_ENABLED = "1";
    public static final String PERMISSION_DISABLED = "0";

    private TestUserMode userMode;
    private GenericRestClient genericRestClient;
    private String publisherUrl;
    private Map<String, String> queryParamMap;
    private Map<String, String> headerMap;
    private String cookieHeader;
    private String resourcePath = FrameworkPathUtil.getSystemResourceLocation()
                                  + "artifacts" + File.separator + "GREG" + File.separator;
    private String jSessionId;
    private String NEW_RESOURCE_PATH = "/_system/governance/trunk/restservices/1.0.0/testservice1234";
    private String lifeCycleName = "ServiceLifeCycle";
    private String assetId;
    private ResourceAdminServiceClient resourceAdminServiceClient;

    @Factory(dataProvider = "userModeProvider")
    public GregPublisherPermissionCheckForLCOperations(TestUserMode userMode) {
        this.userMode = userMode;
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(userMode);
        publisherUrl = publisherContext.getContextUrls()
                .getSecureServiceUrl().replace("services", "publisher/apis");
    }

    @BeforeMethod(alwaysRun = true)
    public void resetParameters() {
        queryParamMap = new HashMap<String, String>();
        headerMap = new HashMap<String, String>();
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Authenticate Publisher test")
    protected void testAddResources()
            throws Exception {
        genericRestClient = new GenericRestClient();

        super.init(userMode);
        resourceAdminServiceClient = new ResourceAdminServiceClient(backendURL, sessionCookie);

        authenticatePublisher("admin", "admin");

        ClientResponse response = createAsset(resourcePath + "json" + File.separator +
                                              "publisherPublishRestResource.json", publisherUrl,
                                              cookieHeader, "restservice", genericRestClient);

        JSONObject obj = new JSONObject(response.getEntity(String.class));
        Assert.assertTrue((response.getStatusCode() == 201),
                          "Wrong status code ,Expected 201 Created ,Received " +
                          response.getStatusCode());
        assetId = obj.get("id").toString();
        Assert.assertNotNull(assetId, "Empty asset resource id available" +
                                      response.getEntity(String.class));
        response = getAsset(assetId, "restservice", publisherUrl, cookieHeader, genericRestClient);
        obj = new JSONObject(response.getEntity(String.class));
        Assert.assertTrue(obj.get("lifecycle").equals(lifeCycleName), "LifeCycle not assigned to given assert");

        resourceAdminServiceClient.addResourcePermission(NEW_RESOURCE_PATH, MANAGER_TEST_ROLE,
                                                         WRITE_ACTION, PERMISSION_DISABLED);
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "PromoteLifeCycle with fault user",
          dependsOnMethods = {"testAddResources"})
    public void CheckLCPermissionForAdmin()
            throws JSONException, InterruptedException, IOException, LogViewerLogViewerException {
        ClientResponse response = getLifeCycleState(assetId, "restservice", lifeCycleName);
        Assert.assertTrue(response.getStatusCode() == 200, "Fault user accepted");

        JSONObject LCStateobj = new JSONObject(response.getEntity(String.class));
        JSONObject dataObj = LCStateobj.getJSONObject("data");

        boolean isLCActionsPermitted = dataObj.getBoolean("isLCActionsPermitted");
        Assert.assertEquals(isLCActionsPermitted, true);

        JSONArray checkItems = dataObj.getJSONArray("checkItems");
        //all 3 check list items should be available for admin role user
        Assert.assertEquals(((JSONObject) checkItems.get(0)).getString("isVisible"), "true");
        Assert.assertEquals(((JSONObject) checkItems.get(1)).getString("isVisible"), "true");
        Assert.assertEquals(((JSONObject) checkItems.get(2)).getString("isVisible"), "true");
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "PromoteLifeCycle with fault user",
          dependsOnMethods = {"CheckLCPermissionForAdmin"})
    public void CheckLCPermissionForManager() throws LogViewerLogViewerException, RemoteException, JSONException {
        authenticatePublisher("manager", "manager");
        ClientResponse response = getLifeCycleState(assetId, "restservice", lifeCycleName);
        Assert.assertTrue(response.getStatusCode() == 200, "Fault user accepted");

        JSONObject LCStateobj = new JSONObject(response.getEntity(String.class));
        JSONObject dataObj = LCStateobj.getJSONObject("data");

        boolean isLCActionsPermitted = dataObj.getBoolean("isLCActionsPermitted");
        Assert.assertEquals(isLCActionsPermitted, false);

        JSONArray checkItems = dataObj.getJSONArray("checkItems");
        //all 3 check list items should NOT be available for manager role user
        Assert.assertEquals(((JSONObject) checkItems.get(0)).getString("isVisible"), "null");
        Assert.assertEquals(((JSONObject) checkItems.get(1)).getString("isVisible"), "null");
        Assert.assertEquals(((JSONObject) checkItems.get(2)).getString("isVisible"), "null");
    }


    @DataProvider
    private static TestUserMode[][] userModeProvider() {
        return new TestUserMode[][]{
                new TestUserMode[]{TestUserMode.SUPER_TENANT_ADMIN},
//                new TestUserMode[]{TestUserMode.TENANT_USER},
        };
    }

    private void authenticatePublisher(String username, String password) throws JSONException {
        ClientResponse response = authenticate(publisherUrl, genericRestClient, username, password);

        JSONObject obj = new JSONObject(response.getEntity(String.class));
        Assert.assertTrue((response.getStatusCode() == 200),
                          "Wrong status code ,Expected 200 OK ,Received " +
                          response.getStatusCode());
        jSessionId = obj.getJSONObject("data").getString("sessionId");
        cookieHeader = "JSESSIONID=" + jSessionId;
        Assert.assertNotNull(jSessionId, "Invalid JSessionID received");
    }

    private ClientResponse getLifeCycleState(String assetId, String assetType, String requestLCname) throws JSONException {
        Map<String, String> assetTypeParamMap = new HashMap<String, String>();
        assetTypeParamMap.put("type", assetType);
        assetTypeParamMap.put("lifecycle", requestLCname);
        ClientResponse response =
                genericRestClient.geneticRestRequestGet
                        (publisherUrl + "/asset/" + assetId + "/state"
                                , assetTypeParamMap, headerMap, cookieHeader);
        return response;
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws Exception {
        deleteAsset(assetId, publisherUrl, cookieHeader, "restservice", genericRestClient);
    }
}
