package org.wso2.carbon.registry.governance.api.rest.test;

import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;

import java.util.HashMap;
import java.util.Map;

public class ResponseValidatorTestCase extends GREGIntegrationBaseTest {

    private String type = "restservices";
    private String serverUrl;
    private String createdAssetUUID;
    Map<String, String> requestHeaders = new HashMap<>();


    private static final String PAYLOAD =
            "{\n" + "   \"name\": \"rest1\",\n" + "   \"type\": \"restservice\",\n" + "   \"uritemplate\":[\n"
            + "      {\n" + "      \"urlPattern\":\"/api\",\n" + "      \"authType\": \"Basic AUth\",\n"
            + "      \"httpVerb\" : \"http\"\n" + "      },\n" + "      {\n"
            + "      \"urlPattern\":\"/auth/api\",\n" + "      \"authType\": \"oauth\",\n"
            + "      \"httpVerb\" : \"https\"\n" + "      }\n" + "   ],\n" + "   \"context\": \"contest\",\n"
            + "   \"endpoints\": null,\n" + "   \"description\": \"description\",\n"
            + "   \"version\": \"1.2.3\",\n" + "   \"security_authenticationType\": \"None\",\n"
            + "   \"contacts\": null\n" + "}";

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        serverUrl = automationContext.getContextUrls().getWebAppURLHttps() + "/governance/" + type;
        requestHeaders.put("Authorization", "Basic YWRtaW46YWRtaW4=");
        requestHeaders.put("Content-Type", "application/json");

        // create the policy using the rest api
        HttpResponse response = HTTPSClientUtils.doPost(serverUrl, requestHeaders, PAYLOAD);
        String createdAssetUrl = ((HashMap) response.getHeaders()).get("Location").toString();
        String[] splitParams = createdAssetUrl.split("/");
        createdAssetUUID = splitParams[splitParams.length - 1];

    }


    @Test(groups = {
            "wso2.greg" }, description = "validate rest api response json")
    public void testResponseJson()
            throws Exception {
        Thread.sleep(30000);
        HttpResponse response = HTTPSClientUtils.doGet(serverUrl + "/" + createdAssetUUID, requestHeaders);
        Assert.assertEquals(response.getResponseCode(), 200);
        Assert.assertTrue(isJSONValid(response.getData()),"Invalid json response");
        Assert.assertTrue(response.getData().contains("type"),"Json missing the type parameter");


    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws Exception {
        HTTPSClientUtils.doDelete(serverUrl + "/" + createdAssetUUID, requestHeaders);
    }


    public boolean isJSONValid(String responseData) {
        try {
            new JSONObject(responseData);
        } catch (JSONException ex) {
            return false;
        }
        return true;
    }

}