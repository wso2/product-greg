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
import org.json.JSONArray;
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
 * This class tests giving, updating and removing permissions of roles on soap resources
 */
public class SoapServicePermissionTestCase extends GregESTestBaseTest {

    private static final Log log = LogFactory.getLog(SoapServicePermissionTestCase.class);
    private static final String STATUS_EXPECTED = "true";
    String assetId;
    String cookieHeaderPublisher;
    GenericRestClient genericRestClient;
    Map<String, String> queryParamMap;
    Map<String, String> headerMap;
    String publisherUrl;
    String resourcePath;
    String soapResourcePath;
    private TestUserMode userMode;

    @Factory(dataProvider = "userModeProvider")
    public SoapServicePermissionTestCase(TestUserMode userMode) {
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
        this.setTestEnvironment();
    }

    @Test(groups = { "wso2.greg", "wso2.greg.es" },
            description = "Retrieving permissions for asset")
    public void getPermissionsListSoapResource()
            throws JSONException, IOException {
        Assert.assertTrue(isPermissionsSoapResource("INTERNAL/everyone", "readAllow"),
                "Could not retrieve permission to resource");
    }

    @Test(groups = { "wso2.greg", "wso2.greg.es" },
            description = "Adding read permission to system/wso2.anonymous.role role",
            dependsOnMethods = { "getPermissionsListSoapResource" })
    public void addPublicPermissionToSoapResource()
            throws JSONException, IOException {

        String dataBody = String.
                format(readFile(resourcePath + "json" + File.separator + "publisherPermissionSoapResourceAdd.json"),
                        assetId, soapResourcePath);
        ClientResponse response = genericRestClient.
                geneticRestRequestPost(publisherUrl + "/permissions", MediaType.APPLICATION_JSON,
                        MediaType.APPLICATION_JSON, dataBody, queryParamMap, headerMap, cookieHeaderPublisher);
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        String responseStatus = obj.get("status").toString();
        Assert.assertTrue(((response.getStatusCode() == HttpStatus.OK.getCode()) ||
                        (response.getStatusCode() == HttpStatus.CREATED.getCode())),
                "Wrong status code ,Expected 200 OK or 201 OK ,Received " + response.getStatusCode());
        Assert.assertTrue(responseStatus.equals(STATUS_EXPECTED), "Could not add permission to resource");
        Assert.assertTrue(isPermissionsSoapResource("SYSTEM/wso2.anonymous.role", "readAllow"),
                "Could not add read permission to system/wso2.anonymous.role role");
    }

    @Test(groups = { "wso2.greg", "wso2.greg.es" },
            description = "Adding read permissions to internal/reviewer, system/wso2.anonymous.role, "
                    + "write permissions to internal/everyone and delete permission to internal/publisher",
            dependsOnMethods = { "addPublicPermissionToSoapResource" })
    public void updatePermissionToSoapResource()
            throws JSONException, IOException {
        String dataBody = String.
                format(readFile(resourcePath + "json" + File.separator
                                + "publisherPermissionSoapResourceUpdate.json"), assetId, soapResourcePath);
        ClientResponse response = genericRestClient.
                genericRestRequestPut(publisherUrl + "/permissions", MediaType.APPLICATION_JSON,
                        MediaType.APPLICATION_JSON, dataBody, queryParamMap, headerMap, cookieHeaderPublisher);
        JSONObject responseJson = new JSONObject(response.getEntity(String.class));
        String responseStatus = responseJson.get("status").toString();
        Assert.assertTrue(((response.getStatusCode() == HttpStatus.OK.getCode()) ||
                        (response.getStatusCode() == HttpStatus.CREATED.getCode())),
                "Wrong status code ,Expected 200 OK or 201 OK ,Received " + response.getStatusCode());
        Assert.assertTrue(responseStatus.equals(STATUS_EXPECTED), "Could not update permission to resource");
        Assert.assertTrue(isPermissionsSoapResource("INTERNAL/everyone", "writeAllow"),
                "Could not update permissions to role");
    }

    @Test(groups = { "wso2.greg", "wso2.greg.es" },
            description = "Removing permission of system/wso2.anonymous.role of the resource",
            dependsOnMethods = { "updatePermissionToSoapResource" })
    public void removePermissionToSoapResource()
            throws JSONException, IOException {

        queryParamMap.put("assetType", "soapservice");
        queryParamMap.put("assetId", assetId);
        queryParamMap.put("roleToRemove", "system/wso2.anonymous.role");
        queryParamMap.put("pathWithVersion", soapResourcePath);
        ClientResponse response = genericRestClient.
                geneticRestRequestDelete(publisherUrl + "/permissions", MediaType.APPLICATION_JSON,
                        MediaType.APPLICATION_JSON, queryParamMap, headerMap, cookieHeaderPublisher);
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        String resultName = obj.get("status").toString();
        Assert.assertTrue(((response.getStatusCode() == HttpStatus.OK.getCode()) ||
                        (response.getStatusCode() == HttpStatus.CREATED.getCode())),
                "Wrong status code ,Expected 200 OK or 201 OK ,Received " + response.getStatusCode());
        Assert.assertTrue(resultName.equals(STATUS_EXPECTED), "Could not remove permission to resource");
        Assert.assertTrue(isPermissionsSoapResource("SYSTEM/wso2.anonymous.role", "readDeny"),
                "Could not remove permission of resource");
    }

    /***
     *
     * This method is used to authenticate the publisher and create a soap service
     */
    private void setTestEnvironment() throws JSONException, IOException, XPathExpressionException {
        // Authenticate Publisher
        ClientResponse response = authenticate(publisherUrl, genericRestClient,
                automationContext.getSuperTenant().getTenantAdmin().getUserName(),
                automationContext.getSuperTenant().getTenantAdmin().getPassword());
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        String jSessionIdPublisher = obj.getJSONObject("data").getString("sessionId");
        cookieHeaderPublisher = "JSESSIONID=" + jSessionIdPublisher;
        String jsonResourcePath = resourcePath + "json" + File.separator + "publisherPublishSoapResource.json";

        //Create soap service
        ClientResponse createResponse = createAsset(jsonResourcePath, publisherUrl, cookieHeaderPublisher,
                "soapservice", genericRestClient);
        JSONObject createObj = new JSONObject(createResponse.getEntity(String.class));
        assetId = createObj.get("id").toString();
        soapResourcePath = createObj.get("path").toString();
        log.info("Artifact created");
    }

    /***
     *
     * This method is used to check whether the response received has valid data
     */
    private Boolean isPermissionsSoapResource(String role, String permissionType)
            throws JSONException, IOException {

        queryParamMap.put("assetType", "soapservice");
        queryParamMap.put("id", assetId);

        ClientResponse response = genericRestClient.
                geneticRestRequestGet(publisherUrl + "/permissions", queryParamMap, headerMap, cookieHeaderPublisher);
        JSONObject responseObject = new JSONObject(response.getEntity(String.class));
        Assert.assertTrue(((response.getStatusCode() == HttpStatus.OK.getCode()) || (response.getStatusCode()
                        == HttpStatus.CREATED.getCode())),
                "Wrong status code ,Expected 200 OK or 201 OK ,Received " + response.getStatusCode());

        JSONObject data = (JSONObject) responseObject.get("data");
        JSONArray permissionArray = data.getJSONArray("list");
        Boolean status = false;
        for(int i=0; i<permissionArray.length(); i++){
            JSONObject roleObject = permissionArray.getJSONObject(i);
            if(roleObject.get("userName").equals(role) && roleObject.getBoolean(permissionType)){
                status = true;
                break;
            }
        }

        return status;
    }

    @AfterClass(alwaysRun = true)
    public void clean() throws Exception {
        deleteAsset(assetId, publisherUrl, cookieHeaderPublisher, "soapservice", genericRestClient);
        log.info("Artifact deleted");
    }

    @DataProvider
    private static TestUserMode[][] userModeProvider() {
        return new TestUserMode[][] { new TestUserMode[] { TestUserMode.SUPER_TENANT_ADMIN }
                //                new TestUserMode[]{TestUserMode.TENANT_USER},
        };
    }
}
