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
package org.wso2.carbon.registry.es.permission;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wink.client.ClientResponse;
import org.apache.wink.common.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.*;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.registry.es.utils.GregESTestBaseTest;
import org.wso2.greg.integration.common.utils.GenericRestClient;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import javax.xml.xpath.XPathExpressionException;

/**
 * This class tests permission check when assigning permissions to soap resources
 */
public class SoapServicePermissionCheckTestCase extends GregESTestBaseTest {

    private static final Log log = LogFactory.getLog(SoapServicePermissionCheckTestCase.class);
    private static final String STATUS_EXPECTED = "false";

    private TestUserMode userMode;
    String assetId;
    String cookieHeaderPublisher;
    GenericRestClient genericRestClient;
    Map<String, String> queryParamMap;
    Map<String, String> headerMap;
    String publisherUrl;
    String resourcePath;
    String soapResourcePath;

    @Factory(dataProvider = "userModeProvider")
    public SoapServicePermissionCheckTestCase(TestUserMode userMode) {
        this.userMode = userMode;
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(userMode);
        genericRestClient = new GenericRestClient();
        queryParamMap = new HashMap<>();
        headerMap = new HashMap<>();
        resourcePath =
                FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator + "GREG" + File.separator;
        publisherUrl = automationContext.getContextUrls().getSecureServiceUrl().replace("services", "publisher/apis");

        setTestEnvironment();
    }

    @Test(groups = { "wso2.greg", "wso2.greg.es" },
            description = "Retrieving permissions for asset")
    public void getPermissionsSoapResourceWithoutPermission() throws JSONException, IOException {

        queryParamMap.put("assetType","soapservice");
        queryParamMap.put("id",assetId);

        ClientResponse response = genericRestClient.
                geneticRestRequestGet(publisherUrl + "/permissions", queryParamMap, headerMap, cookieHeaderPublisher);

        JSONObject responseObject = new JSONObject(response.getEntity(String.class));
        String responseStatus = responseObject.getJSONObject("data").getString("status");
        Assert.assertTrue(((response.getStatusCode() == HttpStatus.UNAUTHORIZED.getCode())),
                "Wrong status code ,Expected 401 Permission Denied ,Received " + response.getStatusCode());

        Assert.assertTrue(responseStatus.equals(STATUS_EXPECTED), "Could not retrieve permission to resource");
    }

    @Test(groups = { "wso2.greg", "wso2.greg.es" },
            description = "Adding read permission to system/wso2.anonymous.role role from a user without permission",
            dependsOnMethods = { "getPermissionsSoapResourceWithoutPermission" })
    public void addPublicPermissionToSoapResourceWithoutPermission() throws JSONException, IOException {

        String dataBody = String.format(
                readFile(resourcePath + "json" + File.separator + "publisherPermissionSoapResourceAdd.json"), assetId,
                soapResourcePath);

        ClientResponse response = genericRestClient.
                geneticRestRequestPost(publisherUrl + "/permissions", MediaType.APPLICATION_JSON,
                        MediaType.APPLICATION_JSON, dataBody, queryParamMap, headerMap, cookieHeaderPublisher);

        JSONObject responseObject = new JSONObject(response.getEntity(String.class));
        String responseStatus = responseObject.get("status").toString();
        Assert.assertTrue(((response.getStatusCode() == HttpStatus.UNAUTHORIZED.getCode())),
                "Wrong status code ,Expected 401 Permission Denied ,Received " + response.getStatusCode());

        Assert.assertTrue(responseStatus.equals(STATUS_EXPECTED), "Allows to add permission to resource");
        log.info("Add permission successfully denied");

    }

    @Test(groups = { "wso2.greg", "wso2.greg.es" },
            description = "Adding read permissions to internal/reviewer, system/wso2.anonymous.role, write"
                    + " permissions to internal/everyone and delete permission to internal/publisher from"
                    + " a user without permission",
            dependsOnMethods = { "addPublicPermissionToSoapResourceWithoutPermission" })
    public void updatePermissionToSoapResourceWithoutPermission() throws JSONException, IOException {

        String dataBody = String.
                format(readFile(resourcePath + "json" + File.separator
                                + "publisherPermissionSoapResourceUpdate.json"), assetId, soapResourcePath);

        ClientResponse response = genericRestClient.
                genericRestRequestPut(publisherUrl + "/permissions", MediaType.APPLICATION_JSON,
                        MediaType.APPLICATION_JSON, dataBody, queryParamMap, headerMap, cookieHeaderPublisher);

        JSONObject responseObject = new JSONObject(response.getEntity(String.class));
        String responseStatus = responseObject.get("status").toString();

        Assert.assertTrue(((response.getStatusCode() == HttpStatus.UNAUTHORIZED.getCode())),
                "Wrong status code ,Expected 401 Permission Denied ,Received " + response.getStatusCode());

        Assert.assertTrue(responseStatus.equals(STATUS_EXPECTED), "Allows to update permission to resource");
        log.info("Update permission successfully denied");

    }

    @Test(groups = { "wso2.greg", "wso2.greg.es" },
            description = "Removing permission of "
                    + "system/wso2.anonymous.role of the resource from a user without permission",
            dependsOnMethods = { "updatePermissionToSoapResourceWithoutPermission" })
    public void removePermissionToSoapResourceWithoutPermission() throws JSONException, IOException {

        queryParamMap.put("assetType","soapservice");
        queryParamMap.put("assetId",assetId);
        queryParamMap.put("roleToRemove","system/wso2.anonymous.role");
        queryParamMap.put("pathWithVersion",soapResourcePath);

        ClientResponse response = genericRestClient.
                geneticRestRequestDelete(publisherUrl + "/permissions", MediaType.APPLICATION_JSON,
                        MediaType.APPLICATION_JSON, queryParamMap, headerMap, cookieHeaderPublisher);

        JSONObject responseObject = new JSONObject(response.getEntity(String.class));
        String responseStatus = responseObject.get("status").toString();

        Assert.assertTrue(((response.getStatusCode() == HttpStatus.UNAUTHORIZED.getCode())),
                "Wrong status code ,Expected 401 Permission Denied ,Received " + response.getStatusCode());

        Assert.assertTrue(responseStatus.equals(STATUS_EXPECTED), "Allows to remove permission to resource");
        log.info("Remove permission successfully denied");
    }

    /***
     *
     * This method is used to authenticate the publisher and create a soap service
     */
    private void setTestEnvironment() throws JSONException, IOException, XPathExpressionException {
        // Authenticate Publisher
        ClientResponse response = authenticate(publisherUrl, genericRestClient,
                automationContext.getSuperTenant().getTenantUser("subscribeUser").getUserName(),
                automationContext.getSuperTenant().getTenantUser("subscribeUser").getPassword());
        JSONObject responseObject = new JSONObject(response.getEntity(String.class));
        String jSessionIdPublisher = responseObject.getJSONObject("data").getString("sessionId");
        cookieHeaderPublisher = "JSESSIONID=" + jSessionIdPublisher;
        String jsonResourcePath = resourcePath + "json" + File.separator + "publisherPublishSoapResource.json";

        //Create soap service
        ClientResponse createResponse = createAsset(jsonResourcePath, publisherUrl, cookieHeaderPublisher,
                "soapservice", genericRestClient);
        JSONObject createObj = new JSONObject(createResponse.getEntity(String.class));
        assetId = createObj.get("id").toString();
        soapResourcePath = createObj.get("path").toString();
    }

    @AfterClass(alwaysRun = true)
    public void clean() throws Exception {
        ClientResponse response = authenticate(publisherUrl, genericRestClient,
                automationContext.getSuperTenant().getTenantAdmin().getUserName(),
                automationContext.getSuperTenant().getTenantAdmin().getPassword());
        JSONObject responseObject = new JSONObject(response.getEntity(String.class));
        String jSessionIdPublisher = responseObject.getJSONObject("data").getString("sessionId");
        cookieHeaderPublisher = "JSESSIONID=" + jSessionIdPublisher;
        deleteAsset(assetId, publisherUrl, cookieHeaderPublisher, "soapservice", genericRestClient);
    }

    @DataProvider private static TestUserMode[][] userModeProvider() {
        return new TestUserMode[][] { new TestUserMode[] { TestUserMode.SUPER_TENANT_ADMIN }
                //                new TestUserMode[]{TestUserMode.TENANT_USER},
        };
    }

}
