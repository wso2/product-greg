package org.wso2.carbon.registry.es.publisher.lifecycle;

import org.apache.wink.client.ClientResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.integration.common.utils.FileManager;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.logging.view.stub.LogViewerLogViewerException;
import org.wso2.carbon.logging.view.stub.types.carbon.LogEvent;
import org.wso2.carbon.registry.es.utils.GregESTestBaseTest;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.greg.integration.common.clients.LifeCycleManagementClient;
import org.wso2.greg.integration.common.clients.LogViewerClient;
import org.wso2.greg.integration.common.utils.GenericRestClient;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GregRestResourceLCWithTwoActionsPointingToSameStateWithExecutor extends GregESTestBaseTest {

    protected final String executorJAR = "test-artifacts-1.0-SNAPSHOT.jar";
    private TestUserMode userMode;
    private GenericRestClient genericRestClient;
    private String publisherUrl;
    private LifeCycleManagementClient lifeCycleAdminServiceClient;
    private Map<String, String> queryParamMap;
    private Map<String, String> headerMap;
    private String cookieHeader;
    private String resourcePath = FrameworkPathUtil.getSystemResourceLocation()
            + "artifacts" + File.separator + "GREG" + File.separator;
    private String jSessionId;
    private LogViewerClient logViewerClient;
    private String lifeCycleName = "ServiceLifeCycle";
    private String assetId;
    private String libPath = CarbonUtils.getCarbonHome() + File.separator + "repository" +
                             File.separator + "components" + File.separator + "lib";
    private String dropinsPath = CarbonUtils.getCarbonHome() + File.separator + "repository" +
                             File.separator + "components" + File.separator + "dropins";
    private ServerConfigurationManager serverConfigurationManager;

    @Factory(dataProvider = "userModeProvider")
    public GregRestResourceLCWithTwoActionsPointingToSameStateWithExecutor(TestUserMode userMode) {
        this.userMode = userMode;
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(userMode);
        publisherUrl = automationContext.getContextUrls()
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
        String jarPath = resourcePath + "lifecycle" + File.separator + executorJAR;

        FileManager.copyResourceToFileSystem(jarPath, libPath, executorJAR);

        serverConfigurationManager = new ServerConfigurationManager(automationContext);
        super.init(userMode);
        lifeCycleAdminServiceClient = new LifeCycleManagementClient(backendURL, sessionCookie);
        lifeCycleAdminServiceClient.
                editLifeCycle(lifeCycleName,
                              readFile(resourcePath + "lifecycle" + File.separator +
                                       "ServiceLifeCycleWirh2actionsPointingToSameStateWithExecutors.xml"));

        serverConfigurationManager.restartGracefully();

        genericRestClient = new GenericRestClient();

        super.init(userMode);
        lifeCycleAdminServiceClient = new LifeCycleManagementClient(backendURL, sessionCookie);
        logViewerClient = new LogViewerClient(backendURL, sessionCookie);

        authenticatePublisher();

        queryParamMap.put("type", "restservice");
        String dataBody = readFile(resourcePath + "json" + File.separator
                                   + "publisherPublishRestResource.json");
        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/assets",
                                                         MediaType.APPLICATION_JSON,
                                                         MediaType.APPLICATION_JSON, dataBody
                        , queryParamMap, headerMap, cookieHeader);
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        Assert.assertTrue((response.getStatusCode() == 201),
                          "Wrong status code ,Expected 201 Created ,Received " +
                          response.getStatusCode());
        assetId = obj.get("id").toString();
        Assert.assertNotNull(assetId, "Empty asset resource id available" +
                                      response.getEntity(String.class));
        Assert.assertTrue(this.getAsset(assetId, "restservice").get("lifecycle")
                                  .equals(lifeCycleName), "LifeCycle not assigned to given assert");
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "PromoteLifeCycle with fault user",
          dependsOnMethods = {"testAddResources"})
    public void promoteLifeCycleWithFCorrectUserWithRole()
            throws JSONException, InterruptedException, IOException, LogViewerLogViewerException {
        queryParamMap.put("type", "restservice");
        queryParamMap.put("lifecycle", lifeCycleName);
        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/asset/" + assetId + "/change-state",
                                                         MediaType.APPLICATION_FORM_URLENCODED,
                                                         MediaType.APPLICATION_JSON,
                                                         "nextState=development&comment=Completed"
                        , queryParamMap, headerMap, cookieHeader);
        JSONObject obj2 = new JSONObject(response.getEntity(String.class));
        Assert.assertTrue(response.getStatusCode() == 200, "Fault user accepted");
        LogEvent[] logEvents = logViewerClient.getLogs("INFO", "@@@@@@@@@@@@@@@@@@@@@@ " +
                                                               "PromoteNotificationExecutor ACTION executed! " +
                                                               "---------------------------", "", "");
        Assert.assertEquals(logEvents.length, 1);
    }


    @DataProvider
    private static TestUserMode[][] userModeProvider() {
        return new TestUserMode[][]{
                new TestUserMode[]{TestUserMode.SUPER_TENANT_ADMIN},
//                new TestUserMode[]{TestUserMode.TENANT_USER},
        };
    }

    private JSONObject getAsset(String assetId, String assetType) throws JSONException {
        Map<String, String> assetTypeParamMap = new HashMap<String, String>();
        assetTypeParamMap.put("type", assetType);
        ClientResponse response =
                genericRestClient.geneticRestRequestGet
                        (publisherUrl + "/assets/" + assetId
                                , queryParamMap, headerMap, cookieHeader);
        return new JSONObject(response.getEntity(String.class));
    }

    public void authenticatePublisher() throws JSONException {
        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/authenticate/",
                                                         MediaType.APPLICATION_FORM_URLENCODED,
                                                         MediaType.APPLICATION_JSON,
                                                         "username=admin&password=admin"
                        , queryParamMap, headerMap, null);
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        Assert.assertTrue((response.getStatusCode() == 200),
                          "Wrong status code ,Expected 200 OK ,Received " +
                          response.getStatusCode());
        jSessionId = obj.getJSONObject("data").getString("sessionId");
        cookieHeader = "JSESSIONID=" + jSessionId;
        Assert.assertNotNull(jSessionId, "Invalid JSessionID received");
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws Exception {
        queryParamMap.put("type", "restservice");
        genericRestClient.geneticRestRequestDelete(publisherUrl + "/assets/" + assetId,
                                                   MediaType.APPLICATION_JSON,
                                                   MediaType.APPLICATION_JSON
                , queryParamMap, headerMap, cookieHeader);
        lifeCycleAdminServiceClient.editLifeCycle(lifeCycleName,
                                                  readFile(resourcePath + "lifecycle" +
                                                           File.separator + "ServiceLifeCycle.xml"));
        FileManager.deleteFile(libPath + File.separator + executorJAR);
        FileManager.deleteFile(dropinsPath + File.separator + "test_artifacts_1.0_SNAPSHOT_1.0.0.jar");
        serverConfigurationManager.restartGracefully();
    }
}
