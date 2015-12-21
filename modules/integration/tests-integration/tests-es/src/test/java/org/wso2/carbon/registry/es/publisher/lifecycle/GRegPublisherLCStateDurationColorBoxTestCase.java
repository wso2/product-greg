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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wink.client.ClientResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.*;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.integration.common.utils.exceptions.AutomationUtilException;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.registry.es.utils.GregESTestBaseTest;
import org.wso2.carbon.registry.es.utils.LifeCycleBean;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.greg.integration.common.clients.LifeCycleAdminServiceClient;
import org.wso2.greg.integration.common.clients.LifeCycleManagementClient;
import org.wso2.greg.integration.common.clients.ManageGenericArtifactAdminServiceClient;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GenericRestClient;
import org.wso2.carbon.automation.test.utils.common.FileManager;
import org.wso2.carbon.registry.es.utils.LifeCycleConstants;
import org.wso2.carbon.registry.es.utils.LifeCycleUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.MediaType;

/***
 * This class will test Greg Publisher Lifecycle State Duration and Its Color bellow the asset icon
 */
public class GRegPublisherLCStateDurationColorBoxTestCase extends GregESTestBaseTest {
    public static final String CLASS_NAME = GRegPublisherLCStateDurationTestCase.class.getName();
    private static final Log log = LogFactory.getLog(GRegPublisherLCStateDurationTestCase.class);
    String assetId;
    String cookieHeader;
    GenericRestClient genericRestClient;
    Map<String, String> queryParamMap;
    Map<String, String> headerMap;
    String publisherUrl;
    ManageGenericArtifactAdminServiceClient manageGenericArtifactAdminServiceClient;
    LifeCycleManagementClient lifeCycleAdminServiceClient;
    ResourceAdminServiceClient resourceAdminServiceClient;
    private LifeCycleManagementClient lifeCycleManagementClient;
    private LifeCycleAdminServiceClient lifeCycleAdminService;

    private TestUserMode userMode;
    private ServerConfigurationManager serverConfigurationManager;

    @Factory(dataProvider = "userModeProvider")
    public GRegPublisherLCStateDurationColorBoxTestCase(
            TestUserMode userMode) {
        this.userMode = userMode;
    }

    @DataProvider
    private static TestUserMode[][] userModeProvider() {
        return new TestUserMode[][] { new TestUserMode[] { TestUserMode.SUPER_TENANT_ADMIN },
                //                new TestUserMode[]{TestUserMode.TENANT_USER},
        };
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(userMode);
        genericRestClient = new GenericRestClient();
        publisherUrl = automationContext.getContextUrls().getSecureServiceUrl().replace("services", "publisher/apis");
        manageGenericArtifactAdminServiceClient = new ManageGenericArtifactAdminServiceClient(backendURL,
                sessionCookie);
        lifeCycleAdminServiceClient = new LifeCycleManagementClient(backendURL, sessionCookie);
        resourceAdminServiceClient = new ResourceAdminServiceClient(backendURL, sessionCookie);
        serverConfigurationManager = new ServerConfigurationManager(automationContext);
        setTestEnvironment();
    }

    @BeforeMethod
    public void resetParameters() {
        queryParamMap = new HashMap<>();
        headerMap = new HashMap<>();
    }

    @Test(groups = { "wso2.greg", "wso2.greg.es" }, description = "Validate default Configurations, "
            + " before configuration enabled")
    public void checkLCStateDurationDetailsBeforeEnabled() throws Exception {
        LifeCycleBean restResponseBean;
        //Call REST API before server restart with new configurations
        restResponseBean = getRESTDetailsBean();
        Assert.assertFalse(restResponseBean.getLifecycleStateDurationState(),
                "Configuration is not correct in asset.js");
        Assert.assertFalse(restResponseBean.getLifecycleMetaDataState(),
                "Even configuration disabled data is visible");
        LifeCycleUtils.changeConfigurationAssetJS(LifeCycleConstants.STRING_FALSE, LifeCycleConstants.STRING_TRUE);
        serverConfigurationManager.restartGracefully();
    }

    @Test(groups = { "wso2.greg", "wso2.greg.es" }, description = "Validate Configurations, after Lifecycle State"
            + "Duration configuration enabled ", dependsOnMethods = { "checkLCStateDurationDetailsBeforeEnabled" })
    public void checkLCStateDurationDetailsAfterEnabled() throws Exception {
        LifeCycleBean restResponseBean;
        //Authentication user after server reset
        authenticate();
        restResponseBean = getRESTDetailsBean();
        Assert.assertTrue(restResponseBean.getLifecycleStateDurationState(),
                "Configuration is not correct in asset.js");
        Assert.assertTrue(restResponseBean.getLifecycleMetaDataState(),
                "Even configuration enabled data is not visible");
    }

    @Test(groups = { "wso2.greg", "wso2.greg.es" }, description = "Validate Lifecycle State Duration and Color",
            dependsOnMethods = { "checkLCStateDurationDetailsAfterEnabled" })
    public void checkLifecycleStateDurationDetails() throws Exception {
        LifeCycleBean restResponseBean;
        Thread.sleep(LifeCycleConstants.WAIT_TIME_MILLISECONDS);
        restResponseBean = getRESTDetailsBean();
        //yellow color defined in lifecycle XML.
        Assert.assertTrue(restResponseBean.getLifecycleStateDurationColor().equals("yellow"),
                "State Duration Color Not available from Lifecycle XML");
        Assert.assertTrue(LifeCycleUtils.filterInteger(restResponseBean.getLifecycleStateDuration()) >= (
                LifeCycleConstants.WAIT_TIME_MILLISECONDS / 1000), "State Duration Not available");
    }

    @Test(groups = { "wso2.greg", "wso2.greg.es" }, description = "Check results when lifecycle checkpoint not "
            + "null and color is null", dependsOnMethods = {
            "checkLifecycleStateDurationDetails" })
    public void checkLCStateDurationDetailsCheckPoint() throws JSONException, InterruptedException, IOException {
        LifeCycleBean restResponseBean;
        changeLifecycleState();
        log.info("Lifecycle State Changed to Testing " + CLASS_NAME);
        restResponseBean = getRESTDetailsBean();
        Assert.assertFalse(restResponseBean.getLifecycleStateDurationState(),
                "Configuration is not correct when checkpoint is not null and color is null ");
    }

    @Test(groups = { "wso2.greg", "wso2.greg.es" }, description = "Check StateDuration Details After Lifecycle " +
            "State Changed", dependsOnMethods = {
            "checkLCStateDurationDetailsCheckPoint" })
    public void checkLCStateDurationDetailsAfterStateChanged() throws JSONException, InterruptedException,
            IOException {
        LifeCycleBean restResponseBean;
        queryParamMap.put("type", LifeCycleConstants.SERVICE_TYPE);
        queryParamMap.put("lifecycle", LifeCycleConstants.LIFECYCLE_NAME);
        genericRestClient.geneticRestRequestPost(publisherUrl + "/asset/" + assetId + "/change-state",
                MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_JSON,
                "nextState=Production&comment=Completed", queryParamMap, headerMap, cookieHeader);
        log.info("Lifecycle State Changed to Production " + CLASS_NAME);
        restResponseBean = getRESTDetailsBean();
        int duration = LifeCycleUtils.filterInteger(restResponseBean.getLifecycleStateDuration());
        Assert.assertTrue(duration < (LifeCycleConstants.WAIT_TIME_MILLISECONDS / 1000) && duration >= 0,
                "State Duration Not Reset After Lifecycle State Change");
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws LifeCycleManagementServiceExceptionException, IOException,
            ResourceAdminServiceExceptionException, AutomationUtilException {
        LifeCycleUtils.changeConfigurationAssetJS(LifeCycleConstants.STRING_TRUE, LifeCycleConstants.STRING_FALSE);
        deleteAsset(assetId, publisherUrl, cookieHeader, LifeCycleConstants.SERVICE_TYPE, genericRestClient);
        log.info("Asset Deleted " + CLASS_NAME);
        lifeCycleManagementClient.deleteLifeCycle(LifeCycleConstants.LIFECYCLE_NAME);
        log.info("Lifecycle Deleted " + CLASS_NAME);
        serverConfigurationManager.restartGracefully();
    }

    /***
     * change lifecycle state to testing
     *
     * @throws JSONException
     * @throws InterruptedException
     * @throws IOException
     */
    private void changeLifecycleState() throws JSONException, InterruptedException, IOException {
        queryParamMap.put("type", LifeCycleConstants.SERVICE_TYPE);
        queryParamMap.put("lifecycle", LifeCycleConstants.LIFECYCLE_NAME);
        genericRestClient.geneticRestRequestPost(publisherUrl + "/asset/" + assetId + "/change-state",
                MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_JSON,
                "nextState=Testing&comment=Completed", queryParamMap, headerMap, cookieHeader);
    }

    /***
     * Call REST Service and return fetched data LifeCycleBean Object
     *
     * @return LifeCycleBean
     * @throws JSONException
     */
    private LifeCycleBean getRESTDetailsBean() throws JSONException {
        queryParamMap.put("type", LifeCycleConstants.SERVICE_TYPE);
        queryParamMap.put("lifecycle", LifeCycleConstants.LIFECYCLE_NAME);
        ClientResponse response = genericRestClient
                .geneticRestRequestGet(publisherUrl + "/assets/" + assetId, queryParamMap, headerMap, cookieHeader);
        JSONObject responseJSONObject = new JSONObject(response.getEntity(String.class));
        LifeCycleBean restResponseBean = new LifeCycleBean();
        if (responseJSONObject.has(LifeCycleConstants.IS_LC_STATE_DURATION_ENABLED)) {
            restResponseBean.setLifecycleStateDurationState(
                    responseJSONObject.getBoolean(LifeCycleConstants.IS_LC_STATE_DURATION_ENABLED));
        } else {
            restResponseBean.setLifecycleStateDurationState(false);
        }

        if (responseJSONObject.has(LifeCycleConstants.LC_STATE_DURATION_META_DATA)) {
            restResponseBean.setLifecycleMetaDataState(true);
            restResponseBean.setLifecycleStateDurationState(true);
            JSONObject durationDetailsObject = responseJSONObject.
                    getJSONObject(LifeCycleConstants.LC_STATE_DURATION_META_DATA);
            restResponseBean.setLifecycleStateDuration(durationDetailsObject.
                    getString(LifeCycleConstants.LC_STATE_DURATION));
            restResponseBean.setLifecycleStateDurationColor(durationDetailsObject.
                    getString(LifeCycleConstants.LC_STATE_DURATION_COLOR));
        } else {
            restResponseBean.setLifecycleStateDurationState(false);

        }
        return restResponseBean;
    }

    /***
     * Setting prerequisites ,authentication,create assert and addling lifecycle
     *
     * @throws Exception
     */
    private void setTestEnvironment() throws Exception {
        ClientResponse response;
        StringBuilder resourcePath = new StringBuilder();
        resourcePath.append(FrameworkPathUtil.getSystemResourceLocation()).append("artifacts").append(File.separator).
                append("GREG").append(File.separator);
        authenticate();
        String filePath = resourcePath.toString() + "lifecycle" + File.separator + "CheckpointsLifecycle.xml";
        String lifeCycleConfiguration = FileManager.readFile(filePath);
        lifeCycleManagementClient.addLifeCycle(lifeCycleConfiguration);

        response = createAsset(resourcePath.toString() + "json" + File.separator + "publisherPublishRestResource.json",
                publisherUrl, cookieHeader, LifeCycleConstants.SERVICE_TYPE, genericRestClient);
        log.info("Asset Created " + CLASS_NAME);
        JSONObject responseJSONObjectCreateAsset = new JSONObject(response.getEntity(String.class));
        assetId = responseJSONObjectCreateAsset.get("id").toString();
        lifeCycleAdminService
                .addAspect(responseJSONObjectCreateAsset.get("path").toString(), LifeCycleConstants.LIFECYCLE_NAME);
        log.info("Lifecycle Attached to Asset " + CLASS_NAME);
    }

    /***
     * Authenticating  Publisher and Admin Services
     *
     * @throws Exception
     */
    private void authenticate() throws Exception {
        ClientResponse response;
        //Authenticating Publisher
        String jSessionId;
        response = authenticate(publisherUrl, genericRestClient, "admin", "admin");
        log.info("Publisher Authenticated " + CLASS_NAME);
        JSONObject responseJSONObjectAuth = new JSONObject(response.getEntity(String.class));
        jSessionId = responseJSONObjectAuth.getJSONObject("data").getString("sessionId");
        cookieHeader = "JSESSIONID=" + jSessionId;

        //Authenticating to the admin services
        String sessionCookie = getSessionCookie();
        lifeCycleManagementClient = new LifeCycleManagementClient(backendURL, sessionCookie);
        lifeCycleAdminService = new LifeCycleAdminServiceClient(backendURL, sessionCookie);
    }

}
