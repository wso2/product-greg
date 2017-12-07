
/*
*Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.es.taxonomy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wink.client.ClientResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.*;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.es.utils.GregESTestBaseTest;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GenericRestClient;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.activation.DataHandler;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.xpath.XPathExpressionException;
import static org.testng.Assert.assertTrue;

@SetEnvironment(executionEnvironments = { ExecutionEnvironment.ALL})
public class RestServiceCRUDTaxonomyTestCase extends GregESTestBaseTest {
    private static final Log log = LogFactory.getLog(RestServiceCRUDTaxonomyTestCase.class);
    private TestUserMode userMode;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    String jSessionId;
    String assetId;
    String assetName;
    String cookieHeader;
    String cookieHeaderStore;
    GenericRestClient genericRestClient;
    Map<String, String> headerMap;
    String publisherUrl;
    String StoreUrl;
    String resourcePath;

    @Factory(dataProvider = "userModeProvider")
    public RestServiceCRUDTaxonomyTestCase(TestUserMode userMode) {
        this.userMode = userMode;
    }
    public static final String RXT_STORAGE_PATH =
            "/_system/governance/repository/components/org.wso2.carbon.governance/types/employee.rxt";
    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(userMode);
        genericRestClient = new GenericRestClient();
        headerMap = new HashMap<>();
        resourcePath = FrameworkPathUtil.getSystemResourceLocation()
                + "artifacts" + File.separator + "GREG" + File.separator;
        publisherUrl = publisherContext.getContextUrls()
                .getSecureServiceUrl().replace("services", "publisher/apis");

        StoreUrl = publisherContext.getContextUrls()
                .getSecureServiceUrl().replace("services", "store/apis");
        resourceAdminServiceClient = new ResourceAdminServiceClient(backendURL, sessionCookie);
        addCustomRxt();
        addNewTaxonomyConfiguration("Location","Location");
        setTestEnvironment();
    }

    /**
     * Method used to add custom RXT (employee.rxt)
     */
    private void addCustomRxt()
            throws RegistryException, IOException, ResourceAdminServiceExceptionException, InterruptedException {
        StringBuilder builder = new StringBuilder();
        builder.append(getTestArtifactLocation()).append("artifacts").append(File.separator).append("GREG").
                append(File.separator).append("rxt").append(File.separator).append("employee.rxt");
        String filePath = builder.toString();
        DataHandler dh = new DataHandler(new URL("file:///" + filePath));
        resourceAdminServiceClient
                .addResource(RXT_STORAGE_PATH, "application/vnd.wso2.registry-ext-type+xml", "desc", dh);
    }
    private void setTestEnvironment() throws Exception {
        Map<String, String> queryParamMap = new HashMap<>();
        queryParamMap.put("type", "employee");
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
        queryParamMap.put("type", "employee");
        String dataBody = readFile(resourcePath + "json" + File.separator + "publisherPublishCustomResource.json");
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

    private void authenticateStore() throws XPathExpressionException, JSONException {
        Map<String, String> queryParamMap = new HashMap<>();
        ClientResponse response2 =
                genericRestClient.geneticRestRequestPost(StoreUrl + "/authenticate/",
                        MediaType.APPLICATION_FORM_URLENCODED,
                        MediaType.APPLICATION_JSON,
                        "username=" + automationContext.getContextTenant().getContextUser().getUserName() +
                                "&password=" + automationContext.getContextTenant().getContextUser().getPassword()
                        , queryParamMap, headerMap, null
                );
        JSONObject obj2 = new JSONObject(response2.getEntity(String.class));
        assertTrue((response2.getStatusCode() == Response.Status.OK.getStatusCode()),
                "Wrong status code ,Expected 200 OK ,Received " +
                        response2.getStatusCode()
        );
        String jSessionId = obj2.getJSONObject("data").getString("sessionId");
        cookieHeaderStore = "JSESSIONID=" + jSessionId;
    }


    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Get Rest Service in Publisher")
    public void getRestServiceAsset() throws JSONException {
        Map<String, String> queryParamMap = new HashMap<>();
        queryParamMap.put("type", "employee");
        ClientResponse clientResponse = getAssetById(publisherUrl, genericRestClient, cookieHeader, assetId,
                queryParamMap);
        Assert.assertTrue((clientResponse.getStatusCode() == 200),
                "Wrong status code ,Expected 200 OK " + clientResponse.getStatusCode());
        JSONObject obj = new JSONObject(clientResponse.getEntity(String.class));
        Assert.assertEquals(obj.get("id"), assetId);
    }

    /**

     * Can be used to add new taxonomy configuration
     * @param fileName name of the new taxonomy file
     * @param resourceFileName saving name for the taxonomy file
     * @return true on successful addition of taxonomy
     * @throws Exception
     */
    public boolean addNewTaxonomyConfiguration(String fileName, String resourceFileName) throws Exception {

        ResourceAdminServiceClient resourceAdminServiceClient = new ResourceAdminServiceClient(backendURL,
                getSessionCookie());

        String filePath = getTestArtifactLocation() + "artifacts" + File.separator +
                "GREG" + File.separator + "xml" + File.separator + fileName;
        DataHandler dh = new DataHandler(new URL("file:///" + filePath));
        return resourceAdminServiceClient.addResource(
                "/_system/governance/repository/components/org.wso2.carbon.governance/taxonomy/"+ resourceFileName,
                "application/taxo+xml", "desc", dh);
    }


    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Get admin defined taxonomies",
            dependsOnMethods = {"getRestServiceAsset"})
    public void getAdminDefinedTaxonomies() throws Exception {
        Thread.sleep(5000);
        Map<String, String> queryParamMap = new HashMap<>();
        assetName = "Employee1";
        ClientResponse response = genericRestClient
                .geneticRestRequestGet(publisherUrl + "/taxonomies?assetType=employee", queryParamMap,
                        headerMap, cookieHeader);
        Assert.assertTrue((response.getStatusCode() == 200),
                "Wrong status code ,Expected 200 Created ,Received " + response.getStatusCode());
    }


    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Add taxonomy to Rest Service in Publisher",
            dependsOnMethods = {"getAdminDefinedTaxonomies"})
    public void addTaxonomiesToRestService() throws Exception {
        Map<String, String> queryParamMap = new HashMap<>();
        queryParamMap.put("type", "employee");
        String dataBody = "{\"taxa\" : \"locations/asia/sriLanka\"}";
        ClientResponse response = genericRestClient
                .geneticRestRequestPost(publisherUrl + "/asset/" + assetId + "/taxonomies", MediaType.APPLICATION_JSON,
                        MediaType.APPLICATION_JSON, dataBody, queryParamMap, headerMap, cookieHeader);
        Assert.assertTrue((response.getStatusCode() == 201),
                "Wrong status code ,Expected 200 Created ,Received " + response.getStatusCode());

    }


    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Add taxonomy to Rest Service in Publisher",
            dependsOnMethods = {"addTaxonomiesToRestService"})
    public void getTaxonomiesFromRestService() throws Exception {
        Map<String, String> queryParamMap = new HashMap<>();
        queryParamMap.put("type", "employee");
        assetName = "Employee1";
        ClientResponse response = genericRestClient
                .geneticRestRequestGet(publisherUrl + "/asset/" + assetId + "/taxonomies", queryParamMap, headerMap,
                        cookieHeader);
        Assert.assertTrue((response.getStatusCode() == 200),
                "Wrong status code ,Expected 200 Created ,Received " + response.getStatusCode());
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        Assert.assertTrue((obj.length() > 0), "Asset not added successfully" + response.getStatusCode());
    }



    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Search valid taxonomy in publisher",
            dependsOnMethods = {"getTaxonomiesFromRestService"})
    public void searchRestServiceInStore() throws JSONException, XPathExpressionException, InterruptedException {
        Map<String, String> queryParamMap = new HashMap<>();
        boolean assetFound = false;
        authenticateStore();
        queryParamMap.put("q", "\"taxonomy" + "\":" + "\"" + "locations/asia/sriLanka" + "\"");
        Thread.sleep(20000);
        ClientResponse response = genericRestClient
                .geneticRestRequestGet(StoreUrl + "/assets", queryParamMap, headerMap, cookieHeaderStore);

        JSONObject obj = new JSONObject(response.getEntity(String.class));
        JSONArray jsonArray = obj.getJSONArray("data");
        for (int i = 0; i < jsonArray.length(); i++) {
            String id = (String) jsonArray.getJSONObject(i).get("id");
            if (assetId.equals(id)) {
                assetFound = true;
                break;
            }
        }
        Assert.assertTrue(assetFound, "Rest Service not found in assets listing");
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Search Invalid taxonomy",
            dependsOnMethods = {"searchRestServiceInStore"})
    public void searchInvalidRestServiceInStore() throws JSONException, InterruptedException, XPathExpressionException {
        boolean assetFound = false;
        Map<String, String> queryParamMap = new HashMap<>();
        queryParamMap.put("q", "\"taxonomy" + "\":" + "\"" + "WSO3/LineOfBusiness1/Department1" + "\"");
        Thread.sleep(20000);
        ClientResponse response = genericRestClient
                .geneticRestRequestGet(StoreUrl + "/assets", queryParamMap, headerMap, cookieHeaderStore);

        JSONObject obj = new JSONObject(response.getEntity(String.class));
        JSONArray jsonArray = obj.getJSONArray("data");
        for (int i = 0; i < jsonArray.length(); i++) {
            String id = (String) jsonArray.getJSONObject(i).get("id");
            if (assetId.equals(id)) {
                assetFound = true;
                break;
            }
        }
        Assert.assertFalse(assetFound, "Rest Service  found in assets listing");

    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Update Rest Service in Publisher",
            dependsOnMethods = { "searchInvalidRestServiceInStore" })
    public void deleteTaxonomyFromAsset()
            throws JSONException, IOException, InterruptedException, XPathExpressionException {
        Map<String, String> queryParamMap = new HashMap<>();
        queryParamMap.put("type", "employee");
        queryParamMap.put("taxa", "locations/asia/sriLanka");
        ClientResponse response = genericRestClient
                .geneticRestRequestDelete(publisherUrl + "/asset/" + assetId + "/taxonomies",
                        MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, queryParamMap, headerMap, cookieHeader);
        Assert.assertTrue((response.getStatusCode() == 200),
                "Wrong status code ,Expected 200 Created ,Received " + response.getStatusCode());
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws Exception {
        Map<String, String> queryParamMap = new HashMap<>();
        queryParamMap.put("type", "employee");
        deleteAssetById(publisherUrl, genericRestClient, cookieHeader, assetId, queryParamMap);
        deleteCustomRxt();
    }

    /**
     * Method used to delete custom RXT (employee.rxt)
     */
    private void deleteCustomRxt() throws Exception {
        String session = getSessionCookie();
        resourceAdminServiceClient = new ResourceAdminServiceClient(backendURL, session);
        resourceAdminServiceClient.deleteResource(RXT_STORAGE_PATH);
    }


    @DataProvider
    private static TestUserMode[][] userModeProvider() {
        return new TestUserMode[][]{
                new TestUserMode[]{TestUserMode.SUPER_TENANT_ADMIN}
        };
    }

}