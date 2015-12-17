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
package org.wso2.carbon.registry.es.registry;

import org.apache.wink.client.ClientResponse;
import org.json.JSONException;
import org.testng.annotations.*;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.es.utils.GregESTestBaseTest;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GenericRestClient;

import javax.activation.DataHandler;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertTrue;

/**
 * This class can be used to test scenarios derived from [1]
 * <p/>
 * [1] https://docs.wso2.com/display/Governance510/Registry+REST+API+Sample+Screen+Shots
 */

@SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
public class GovernanceRestAPIAssociationsTestCase extends GregESTestBaseTest {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String AUTHORIZATION_HEADER_VALUE = "Basic YWRtaW46YWRtaW4=";
    private TestUserMode userMode;
    private GenericRestClient genericRestClient;
    private Map<String, String> queryParamMap;
    private Map<String, String> headerMap;
    private String registryAPIUrl;
    private String generalPath;
    private String path;
    private String registryPath;
    private ResourceAdminServiceClient resourceAdminServiceClient;

    @Factory(dataProvider = "userModeProvider")
    public GovernanceRestAPIAssociationsTestCase(TestUserMode userMode) {
        this.userMode = userMode;
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.init(userMode);
        genericRestClient = new GenericRestClient();
        queryParamMap = new HashMap<>();
        headerMap = new HashMap<>();

        headerMap.put(AUTHORIZATION_HEADER, AUTHORIZATION_HEADER_VALUE);

        registryAPIUrl = automationContext.getContextUrls().getWebAppURLHttps();
        generalPath = "/resource/1.0.0/";
        registryPath = "/_system/governance";
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(backendURL, sessionCookie);
        String filePath = getTestArtifactLocation() + "artifacts" + File.separator +
                "GREG" + File.separator + "txt" + File.separator + "MyText.txt";
        DataHandler dh = new DataHandler(new URL("file:///" + filePath));
        path = registryPath + "/" + "MyText.txt";
        resourceAdminServiceClient.addResource(path, "text/xml", "desc", dh);
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws RegistryException, RemoteException, ResourceAdminServiceExceptionException {
        resourceAdminServiceClient.deleteResource(path);
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Adding associations using rest api")
    public void addAssociations() throws JSONException, IOException, InterruptedException {

        queryParamMap.clear();

        queryParamMap.put("path", path);
        queryParamMap.put("targetPath", registryPath);

        int usedByValue = 0;
        int ownedByValue = 0;
        int otherValue = 0;

        String[] arr = {"usedBy", "ownedBy", "other"};
        for (int x = 0; x <= 2; x++) {
            if (x == 0) {
                queryParamMap.put("type", arr[0]);
                ClientResponse responseUsedBy =
                        genericRestClient.geneticRestRequestPost
                                (registryAPIUrl + generalPath + "/association", MediaType.APPLICATION_JSON,
                                        MediaType.APPLICATION_JSON, null, queryParamMap, headerMap, null);
                usedByValue = responseUsedBy.getStatusCode();
            }
            if (x == 1) {
                queryParamMap.put("type", arr[1]);
                ClientResponse responseOwnedBy =
                        genericRestClient.geneticRestRequestPost
                                (registryAPIUrl + generalPath + "/association", MediaType.APPLICATION_JSON,
                                        MediaType.APPLICATION_JSON, null, queryParamMap, headerMap, null);
                ownedByValue = responseOwnedBy.getStatusCode();
            }
            if (x == 2) {
                queryParamMap.put("type", arr[2]);
                ClientResponse responseOther =
                        genericRestClient.geneticRestRequestPost
                                (registryAPIUrl + generalPath + "/association", MediaType.APPLICATION_JSON,
                                        MediaType.APPLICATION_JSON, null, queryParamMap, headerMap, null);
                otherValue = responseOther.getStatusCode();
            }
        }


        assertTrue((usedByValue == Response.Status.NO_CONTENT.getStatusCode()),
                "Wrong status code for usedBy association ,Expected 204 No content ,Received " + usedByValue);
        assertTrue((ownedByValue == Response.Status.NO_CONTENT.getStatusCode()),
                "Wrong status code for ownedBy association ,Expected 204 No content ,Received " + ownedByValue);
        assertTrue((otherValue == Response.Status.NO_CONTENT.getStatusCode()),
                "Wrong status code for other association,Expected 204 No content ,Received " + otherValue);
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Deleting of added associations using rest api",
            dependsOnMethods = "addAssociations")
    public void deleteAssociations() throws JSONException, IOException, InterruptedException {

        queryParamMap.clear();

        queryParamMap.put("path", path);
        queryParamMap.put("targetPath", registryPath);

        int usedByValue = 0;
        int ownedByValue = 0;
        int otherValue = 0;

        String[] arr = {"usedBy", "ownedBy", "other"};
        for (int x = 0; x <= 2; x++) {
            if (x == 0) {
                queryParamMap.put("type", arr[0]);
                ClientResponse responseUsedBy =
                        genericRestClient.geneticRestRequestDelete
                                (registryAPIUrl + generalPath + "/association", MediaType.APPLICATION_JSON,
                                        MediaType.APPLICATION_JSON, null, queryParamMap, headerMap, null);
                usedByValue = responseUsedBy.getStatusCode();
            }
            if (x == 1) {
                queryParamMap.put("type", arr[1]);
                ClientResponse responseOwnedBy =
                        genericRestClient.geneticRestRequestDelete
                                (registryAPIUrl + generalPath + "/association", MediaType.APPLICATION_JSON,
                                        MediaType.APPLICATION_JSON, null, queryParamMap, headerMap, null);
                ownedByValue = responseOwnedBy.getStatusCode();
            }
            if (x == 2) {
                queryParamMap.put("type", arr[2]);
                ClientResponse responseOther =
                        genericRestClient.geneticRestRequestDelete
                                (registryAPIUrl + generalPath + "/association", MediaType.APPLICATION_JSON,
                                        MediaType.APPLICATION_JSON, null, queryParamMap, headerMap, null);
                otherValue = responseOther.getStatusCode();
            }
        }


        assertTrue((usedByValue == Response.Status.NO_CONTENT.getStatusCode()),
                "Wrong status code for usedBy association ,Expected 204 No content ,Received " + usedByValue);
        assertTrue((ownedByValue == Response.Status.NO_CONTENT.getStatusCode()),
                "Wrong status code for ownedBy association ,Expected 204 No content ,Received " + ownedByValue);
        assertTrue((otherValue == Response.Status.NO_CONTENT.getStatusCode()),
                "Wrong status code for other association,Expected 204 No content ,Received " + otherValue);

    }

    @DataProvider
    private static TestUserMode[][] userModeProvider() {
        return new TestUserMode[][]{
                new TestUserMode[]{TestUserMode.SUPER_TENANT_ADMIN},
//                new TestUserMode[]{TestUserMode.TENANT_USER},
        };
    }
}
