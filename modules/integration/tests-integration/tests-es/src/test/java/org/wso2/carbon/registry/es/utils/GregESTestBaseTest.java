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
package org.wso2.carbon.registry.es.utils;

import org.apache.wink.client.ClientResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.GenericRestClient;

import javax.activation.DataHandler;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class GregESTestBaseTest extends GREGIntegrationBaseTest {
    Map<String, String> queryParamMap;
    Map<String, String> headerMap;

    public GregESTestBaseTest() {
        queryParamMap = new HashMap<String, String>();
        headerMap = new HashMap<String, String>();
    }

    /**
     * Authenticate and return jSessionId
     *
     * @param url
     * @param genericRestClient
     * @param username
     * @param password
     * @return ClientResponse
     * @throws JSONException
     */
    public ClientResponse authenticate(String url,
                                       GenericRestClient genericRestClient,
                                       String username,
                                       String password) throws JSONException {
        ClientResponse response =
                genericRestClient.geneticRestRequestPost(url + "/authenticate/",
                                                         MediaType.APPLICATION_FORM_URLENCODED,
                                                         MediaType.APPLICATION_JSON,
                                                         "username=" + username + "&password=" + password
                        , queryParamMap, headerMap, null);
        return response;
    }

    /**
     * Create Asset
     *
     * @param resourcePath
     * @param publisherUrl
     * @param cookieHeader
     * @param genericRestClient
     * @return
     * @throws JSONException
     * @throws IOException
     */
    public ClientResponse createAsset(String resourcePath,
                                      String publisherUrl,
                                      String cookieHeader,
                                      String assetType,
                                      GenericRestClient genericRestClient)
            throws JSONException, IOException {
        queryParamMap.put("type", assetType);
        String dataBody = readFile(resourcePath);
        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/assets",
                                                         MediaType.APPLICATION_JSON,
                                                         MediaType.APPLICATION_JSON, dataBody
                        , queryParamMap, headerMap, cookieHeader);

        return response;
    }

    public ClientResponse deleteAsset(String assetId,
                                      String url,
                                      String cookieHeader,
                                      String assetType,
                                      GenericRestClient genericRestClient) {
        queryParamMap.put("type", assetType);
        return genericRestClient.geneticRestRequestDelete(url + "/assets/" + assetId,
                                                          MediaType.APPLICATION_JSON,
                                                          MediaType.APPLICATION_JSON
                , queryParamMap, headerMap, cookieHeader);
    }

    private JSONObject getAllLifeCycles(String publisherUrl,
                                        String cookieHeader,
                                        GenericRestClient genericRestClient) throws JSONException {

        ClientResponse response =
                genericRestClient.geneticRestRequestGet
                        (publisherUrl + "/lifecycles"
                                , queryParamMap, headerMap, cookieHeader);
        return new JSONObject(response.getEntity(String.class));
    }

    private JSONObject getAllAssetsByType(String publisherUrl,
                                          String cookieHeader,
                                          GenericRestClient genericRestClient)
            throws JSONException {
        ClientResponse response =
                genericRestClient.geneticRestRequestGet
                        (publisherUrl + "/lifecycles"
                                , queryParamMap, headerMap, cookieHeader);
        return new JSONObject(response.getEntity(String.class));
    }

    public ClientResponse getAsset(String assetId,
                                String assetType,
                                String publisherUrl,
                                String cookieHeader,
                                GenericRestClient genericRestClient) throws JSONException {
        Map<String, String> assetTypeParamMap = new HashMap<String, String>();
        assetTypeParamMap.put("type", assetType);
        return genericRestClient.geneticRestRequestGet
                        (publisherUrl + "/assets/" + assetId
                                , queryParamMap, headerMap, cookieHeader);
    }

    private JSONObject getLifeCycleState(String assetId, String assetType,
                                         String publisherUrl,
                                         String cookieHeader,
                                         GenericRestClient genericRestClient) throws JSONException {
        Map<String, String> assetTypeParamMap = new HashMap<String, String>();
        assetTypeParamMap.put("type", assetType);
        ClientResponse response =
                genericRestClient.geneticRestRequestGet
                        (publisherUrl + "/assets/" + assetId + "/state"
                                , queryParamMap, headerMap, cookieHeader);
        return new JSONObject(response.getEntity(String.class));
    }

    private JSONObject getLifeCycleData(String lifeCycleName, String publisherUrl,
                                        String cookieHeader,
                                        GenericRestClient genericRestClient) throws JSONException {
        ClientResponse response =
                genericRestClient.geneticRestRequestGet
                        (publisherUrl + "/lifecycles/" + lifeCycleName
                                , queryParamMap, headerMap, cookieHeader);
        return new JSONObject(response.getEntity(String.class));

    }

    /**
     * Need to refresh the landing page to deploy the new rxt in publisher
     * @param publisherUrl publisher url
     * @param genericRestClient generic rest client object
     * @param cookieHeader session cookies header
     */
    public void refreshPublisherLandingPage(String publisherUrl, GenericRestClient genericRestClient, String cookieHeader) {
        Map<String, String> queryParamMap = new HashMap<>();
        String landingUrl = publisherUrl.replace("apis", "pages/gc-landing");
        genericRestClient.geneticRestRequestGet(landingUrl, queryParamMap, headerMap, cookieHeader);
    }

    /**
     * Add new rxt configuration via resource admin service
     * @param customRxt filename of the custom rxt which is in resources/artifacts/GREG/rxt/ directory
     * @throws Exception
     */
    public void addNewRxtConfigViaAdminService(String customRxt) throws Exception {
        String session = getSessionCookie();
        ResourceAdminServiceClient resourceAdminServiceClient = new ResourceAdminServiceClient(backendURL, session);
        String filePath = getTestArtifactLocation() + "artifacts" + File.separator +
                "GREG" + File.separator + "rxt" + File.separator + "application.rxt";
        DataHandler dh = new DataHandler(new URL("file:///" + filePath));
        resourceAdminServiceClient.addResource(
                "/_system/governance/repository/components/org.wso2.carbon.governance/types/application.rxt",
                "application/vnd.wso2.registry-ext-type+xml", "desc", dh);
    }

}
