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
import org.wso2.carbon.registry.es.utils.GregESTestBaseTest;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GenericRestClient;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
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
public class GovernanceRestAPIResourcesImplementationTestCase extends GregESTestBaseTest {

    private TestUserMode userMode;
    private GenericRestClient genericRestClient;
    private Map<String, String> queryParamMap;
    private Map<String, String> headerMap;
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String AUTHORIZATION_HEADER_VALUE = "Basic YWRtaW46YWRtaW4=";
    private String registryAPIUrl;
    private String generalPath;
    private String registryPath;
    private String textFileName;
    private String path;
    private ResourceAdminServiceClient resourceAdminServiceClient;

    @Factory(dataProvider = "userModeProvider")
    public GovernanceRestAPIResourcesImplementationTestCase(TestUserMode userMode) {
        this.userMode = userMode;
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.init(userMode);
        genericRestClient = new GenericRestClient();
        queryParamMap = new HashMap<>();
        headerMap = new HashMap<>();
        headerMap.put(AUTHORIZATION_HEADER, AUTHORIZATION_HEADER_VALUE);
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(backendURL, sessionCookie);
        registryAPIUrl = automationContext.getContextUrls().getWebAppURLHttps();
        generalPath = "/resource/1.0.0/";
        registryPath = "/_system/governance";
        path = "/_system/governance/MyTextFile.txt";
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() { // in-case if the deleteResource test case unsuccessful , deleting the resource
        try {
            resourceAdminServiceClient.deleteResource(path);
        } catch (ResourceAdminServiceExceptionException | RemoteException e) {
            log.error("Error occurred " + e);
        }
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Adding a resource using rest api")
    public void addResource() throws JSONException, IOException, InterruptedException {

        headerMap.put("Content-Type", "text/xml");

        queryParamMap.clear();

        textFileName = "MyTextFile.txt";

        ClientResponse response =
                genericRestClient.genericRestRequestPut
                        (registryAPIUrl + generalPath + "artifact" + registryPath + "/" + textFileName,
                                MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON,
                                "This is the body of the text file", queryParamMap, headerMap, sessionCookie);

        assertTrue((response.getStatusCode() == Response.Status.CREATED.getStatusCode()),
                "Wrong status code ,Expected 201 , However Received " + response.getStatusCode());

        ClientResponse newResponse = genericRestClient.genericRestRequestPut(registryAPIUrl + generalPath + "artifact"
                        + registryPath + "/" + textFileName, MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON,
                "This is the updated body of the text file", queryParamMap, headerMap, sessionCookie
        );

        assertTrue((newResponse.getStatusCode() == Response.Status.NO_CONTENT.getStatusCode()),
                "Wrong status code while updating contents ,Expected 204 , However Received "
                        + newResponse.getStatusCode()
        );

    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Getting resource contents using rest api"
            , dependsOnMethods = "addResource" , enabled = false)
    public void getResource() throws JSONException, IOException, InterruptedException {

        headerMap.put("Content-Type", "text/xml");

        queryParamMap.clear();

        ClientResponse response =
                genericRestClient.geneticRestRequestGet
                        (registryAPIUrl + generalPath + "artifact" + registryPath + "/" + textFileName,
                                queryParamMap, headerMap, sessionCookie);

        assertTrue((response.getStatusCode() == Response.Status.OK.getStatusCode()),
                "Wrong status code ,Expected 200 , However Received " + response.getStatusCode());

        assertTrue((response.getEntity(String.class).equals("This is the updated body of the text file")),
                "Contents of the added resource mismatch");
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Deleting added resource using rest api"
           /* , dependsOnMethods = "getResource"*/)
    public void deleteResource() throws JSONException, IOException, InterruptedException {

        headerMap.put("Content-Type", "text/xml");

        queryParamMap.clear();

        textFileName = "MyTextFile.txt";

        ClientResponse response =
                genericRestClient.geneticRestRequestDelete
                        (registryAPIUrl + generalPath + "artifact" + registryPath + "/" + textFileName,
                                MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON,
                                "This is the body of the text file", queryParamMap, headerMap, sessionCookie);

        assertTrue((response.getStatusCode() == Response.Status.NO_CONTENT.getStatusCode()),
                "Wrong status code ,Expected 204 , However Received " + response.getStatusCode());
    }


    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Getting Metadata using rest api",
            dependsOnMethods = "deleteResource")
    public void getMetadata() throws JSONException, IOException, InterruptedException {

        queryParamMap.clear();

        queryParamMap.put("path", registryPath);

        ClientResponse response =
                genericRestClient.geneticRestRequestGet
                        (registryAPIUrl + generalPath + "/metadata", queryParamMap, headerMap, null);

        assertTrue((response.getStatusCode() == Response.Status.OK.getStatusCode()),
                "Wrong status code ,Expected 200 OK ,Received " + response.getStatusCode());

        assertTrue(response.getEntity(String.class).contains("{\"authorUsername\":\"wso2.system.user\""));
        assertTrue(response.getEntity(String.class).contains("\"description\":\"Governance registry of the carbon" +
                " server. This collection is used to store the resources common to the whole platform."));

    }

    @DataProvider
    private static TestUserMode[][] userModeProvider() {
        return new TestUserMode[][]{
                new TestUserMode[]{TestUserMode.SUPER_TENANT_ADMIN},
//                new TestUserMode[]{TestUserMode.TENANT_USER},
        };
    }
}
