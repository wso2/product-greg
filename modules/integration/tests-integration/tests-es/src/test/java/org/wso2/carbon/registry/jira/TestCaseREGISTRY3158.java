/*
*Copyright (c) 2005-2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.jira;

import org.apache.wink.client.ClientResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.configurations.UrlGenerationUtil;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.es.utils.GregESTestBaseTest;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GenericRestClient;

import javax.activation.DataHandler;
import javax.ws.rs.core.MediaType;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertTrue;

/**
 * This test class is used to verify the JIRA REGISTRY-3158.
 */

public class TestCaseREGISTRY3158 extends GregESTestBaseTest {

    public static final String RXT_STORAGE_PATH =
            "/_system/governance/repository/components/org.wso2.carbon.governance/types/esbendpoints.rxt";
    public static final String ARTIFACT_RESOURCE_PATH =
            "/_system/governance/trunk/endpoints/esb-endpoints/endpoint-rxts/1.2.3/endpoint1234";
    private TestUserMode userMode;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private String publisherUrl;
    private String resourcePath;
    private String assetId;
    private String cookieHeader;
    private GenericRestClient genericRestClient;
    private Map<String, String> queryParamMap;
    private Map<String, String> headerMap;
    private String loginURL;

    @Factory(dataProvider = "userModeProvider")
    public TestCaseREGISTRY3158(TestUserMode userMode) {
        this.userMode = userMode;
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(userMode);
        loginURL = UrlGenerationUtil.getLoginURL(automationContext.getInstance());
        genericRestClient = new GenericRestClient();
        queryParamMap = new HashMap<>();
        headerMap = new HashMap<>();
        StringBuilder builder = new StringBuilder();
        builder.append(FrameworkPathUtil.getSystemResourceLocation()).append("artifacts").append(File.separator)
                .append("GREG").append(File.separator);
        resourcePath = builder.toString();
        publisherUrl = automationContext.getContextUrls().getSecureServiceUrl().replace("services", "publisher/apis");
        resourceAdminServiceClient = new ResourceAdminServiceClient(backendURL, sessionCookie);
        addCustomRxt();
        queryParamMap.put("type", "esbendpoints");
        setTestEnvironment();
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Update Custom Asset in Publisher")
    public void updateCustomAsset() throws JSONException, IOException, ResourceAdminServiceExceptionException {
        Map<String, String> queryParamMap = new HashMap<>();
        queryParamMap.put("type", "esbendpoints");
        String customTemplate = readFile(resourcePath + "json" + File.separator + "esbEndpoint-update.json");
        String dataBody = String.format(customTemplate, "Test update asset");
        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/assets/" + assetId,
                        MediaType.APPLICATION_JSON,
                        MediaType.APPLICATION_JSON, dataBody
                        , queryParamMap, headerMap, cookieHeader);
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        String resourceData = resourceAdminServiceClient.getTextContent(ARTIFACT_RESOURCE_PATH);
        assertTrue((response.getStatusCode() == 202),
                "Wrong status code ,Expected 202 Created ,Received " +
                        response.getStatusCode()
        );
        assertTrue(obj.getJSONObject("attributes").get("overview_timeoutInSeconds")
                .equals("31"));
        assertTrue(resourceData.contains("<endpoints/>"), "Expected empty endpoints tag, but found content");
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws Exception {
        Map<String, String> queryParamMap = new HashMap<>();
        queryParamMap.put("type", "esbendpoints");
        deleteAssetById(publisherUrl, genericRestClient, cookieHeader, assetId, queryParamMap);
        assertTrue(deleteCustomRxtConfiguration("esbendpoints.rxt"),"Deleting of added custom rxt encountered a failure");
    }

    /**
     * Method used to authenticate publisher and create asset of type esbendpoints. Created asset
     * is used to add subscriptions and to receive notification.
     */
    private void setTestEnvironment() throws JSONException, IOException, XPathExpressionException {
        // Authenticate Publisher
        ClientResponse response = authenticate(publisherUrl, genericRestClient,
                automationContext.getSuperTenant().getTenantAdmin().getUserName(),
                automationContext.getSuperTenant().getTenantAdmin().getPassword());
        JSONObject responseObject = new JSONObject(response.getEntity(String.class));
        String jSessionId = responseObject.getJSONObject("data").getString("sessionId");
        cookieHeader = "JSESSIONID=" + jSessionId;
        //refresh the publisher landing page to deploy new rxt type
        refreshPublisherLandingPage();

        //Create custom asset
        queryParamMap.put("type", "esbendpoints");
        String dataBody = readFile(resourcePath + "json" + File.separator + "esbEndpoint.json");
        ClientResponse createResponse = genericRestClient
                .geneticRestRequestPost(publisherUrl + "/assets", MediaType.APPLICATION_JSON,
                        MediaType.APPLICATION_JSON, dataBody, queryParamMap, headerMap, cookieHeader);
        JSONObject createObj = new JSONObject(createResponse.getEntity(String.class));
        assetId = (String) createObj.get("id");
    }

    /**
     * Need to refresh the landing page to deploy the new rxt in publisher
     */
    private void refreshPublisherLandingPage() {
        Map<String, String> queryParamMap = new HashMap<>();
        String landingUrl = publisherUrl.replace("apis", "pages/gc-landing");
        genericRestClient.geneticRestRequestGet(landingUrl, queryParamMap, headerMap, cookieHeader);
    }

    /**
     * Method used to add custom RXT (esbendpoints.rxt)
     */
    private void addCustomRxt()
            throws RegistryException, IOException, ResourceAdminServiceExceptionException, InterruptedException {
        StringBuilder builder = new StringBuilder();
        builder.append(getTestArtifactLocation()).append("artifacts").append(File.separator).append("GREG").
                append(File.separator).append("rxt").append(File.separator).append("esbendpoints.rxt");
        String filePath = builder.toString();
        DataHandler dh = new DataHandler(new URL("file:///" + filePath));
        resourceAdminServiceClient
                .addResource(RXT_STORAGE_PATH, "application/vnd.wso2.registry-ext-type+xml", "desc", dh);
    }


    @DataProvider
    private static TestUserMode[][] userModeProvider() {
        return new TestUserMode[][]{
                new TestUserMode[]{TestUserMode.SUPER_TENANT_ADMIN}
        };
    }
}
