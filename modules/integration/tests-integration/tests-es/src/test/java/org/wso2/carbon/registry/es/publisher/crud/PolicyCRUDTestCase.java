package org.wso2.carbon.registry.es.publisher.crud;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wink.client.ClientResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.*;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.es.utils.GregESTestBaseTest;
import org.wso2.greg.integration.common.utils.GenericRestClient;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PolicyCRUDTestCase extends GregESTestBaseTest {
    private static final Log log = LogFactory.getLog(PolicyCRUDTestCase.class);
    private TestUserMode userMode;
    String jSessionId;
    String assetId;
    String assetName;
    String cookieHeader;
    GenericRestClient genericRestClient;
    Map<String, String> headerMap;
    String publisherUrl;
    String resourcePath;
    CRUDTestCommonUtils crudTestCommonUtils;

    @Factory(dataProvider = "userModeProvider")
    public PolicyCRUDTestCase(TestUserMode userMode) {
        this.userMode = userMode;
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(userMode);
        genericRestClient = new GenericRestClient();
        headerMap = new HashMap<>();
        resourcePath = FrameworkPathUtil.getSystemResourceLocation()
                + "artifacts" + File.separator + "GREG" + File.separator;
        publisherUrl = automationContext.getContextUrls()
                .getSecureServiceUrl().replace("services", "publisher/apis");
        crudTestCommonUtils = new CRUDTestCommonUtils(genericRestClient, publisherUrl, headerMap);
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Authenticate Publisher test")
    public void authenticatePublisher() throws JSONException {
        Map<String, String> queryParamMap = new HashMap<>();
        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl+"/authenticate/",
                        MediaType.APPLICATION_FORM_URLENCODED,
                        MediaType.APPLICATION_JSON,
                        "username=admin&password=admin"
                        , queryParamMap, headerMap, null);
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        Assert.assertTrue((response.getStatusCode() == 200),
                "Wrong status code ,Expected 200 OK ,Received " +
                        response.getStatusCode());
        jSessionId = obj.getJSONObject("data").getString("sessionId");
        cookieHeader="JSESSIONID=" + jSessionId;
        Assert.assertNotNull(jSessionId, "Invalid JSessionID received");
        crudTestCommonUtils.setCookieHeader(cookieHeader);
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Create Rest Service in Publisher",
            dependsOnMethods = {"authenticatePublisher"})
    public void createPolicyServiceAsset() throws JSONException, IOException {
        Map<String, String> queryParamMap = new HashMap<>();
        queryParamMap.put("type", "policy");
        String dataBody = readFile(resourcePath+"json"+ File.separator+"policy-utpolicy.json");
        assetName = (String)(new JSONObject(dataBody)).get("overview_name");
        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl+"/assets",
                        MediaType.APPLICATION_JSON,
                        MediaType.APPLICATION_JSON, dataBody
                        , queryParamMap, headerMap, cookieHeader);
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        Assert.assertTrue((response.getStatusCode() == 201),
                "Wrong status code ,Expected 201 Created ,Received " +
                        response.getStatusCode());
        String resultName = obj.get("overview_name").toString();
        Assert.assertEquals(resultName,assetName);
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Create Rest Service in Publisher",
            dependsOnMethods = {"authenticatePublisher","createPolicyServiceAsset"})
    public void searchPolicyAsset() throws JSONException {
        boolean assetFound = false;
        Map<String, String> queryParamMap = new HashMap<>();
        queryParamMap.put("type", "policy");
        queryParamMap.put("overview_name", assetName);
        ClientResponse clientResponse = crudTestCommonUtils.searchAssetByQuery(queryParamMap);
        JSONObject obj = new JSONObject(clientResponse.getEntity(String.class));
        JSONArray jsonArray = obj.getJSONArray("list");
        for (int i = 0; i < jsonArray.length(); i++) {
            String name = (String)jsonArray.getJSONObject(i).get("overview_name");
            if (assetName.equals(name)) {
                assetFound = true;
                assetId = (String)jsonArray.getJSONObject(i).get("id");
                break;
            }
        }
        Assert.assertEquals(assetFound,true);
        Assert.assertNotNull(assetId, "Empty asset resource id available");
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Create Rest Service in Publisher",
            dependsOnMethods = {"authenticatePublisher","createPolicyServiceAsset","searchPolicyAsset"})
    public void getPolicyAsset() throws JSONException {
        Map<String, String> queryParamMap = new HashMap<>();
        queryParamMap.put("type", "policy");
        ClientResponse clientResponse = crudTestCommonUtils.getAssetById(assetId, queryParamMap);
        Assert.assertTrue((clientResponse.getStatusCode() == 200),
                "Wrong status code ,Expected 200 OK " +
                        clientResponse.getStatusCode());
        JSONObject obj = new JSONObject(clientResponse.getEntity(String.class));
        Assert.assertEquals(obj.get("id").toString(), assetId);
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Delete Publisher test",
            dependsOnMethods = {"authenticatePublisher","createPolicyServiceAsset","searchPolicyAsset","getPolicyAsset"})
    public void deletePolicyAsset() throws JSONException {
        Map<String, String> queryParamMap = new HashMap<>();
        queryParamMap.put("type", "policy");
        genericRestClient.geneticRestRequestDelete(publisherUrl + "/assets/" + assetId,
                MediaType.APPLICATION_JSON,
                MediaType.APPLICATION_JSON
                , queryParamMap, headerMap, cookieHeader);
        ClientResponse clientResponse = crudTestCommonUtils.getAssetById(assetId, queryParamMap);
        JSONObject obj = new JSONObject(clientResponse.getEntity(String.class));
        Assert.assertTrue((clientResponse.getStatusCode() == 404),
                "Wrong status code ,Expected 404 Not Found " +
                        clientResponse.getStatusCode());
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws RegistryException {

    }

    @DataProvider
    private static TestUserMode[][] userModeProvider() {
        return new TestUserMode[][]{
                new TestUserMode[]{TestUserMode.SUPER_TENANT_ADMIN}
//                new TestUserMode[]{TestUserMode.TENANT_USER},
        };
    }
}
