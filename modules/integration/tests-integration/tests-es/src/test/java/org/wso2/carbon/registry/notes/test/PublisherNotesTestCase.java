/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.registry.notes.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;

public class PublisherNotesTestCase extends GREGIntegrationBaseTest {

    private static final Log log = LogFactory.getLog(PublisherNotesTestCase.class);
    private GenericArtifactManager artifactManager;
    private Registry governance;

    /**
     * This method used to init the wsdl addition test cases.
     *
     * @throws Exception
     */
    @BeforeClass(groups = { "wso2.greg" })
    public void init() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        WSRegistryServiceClient wsRegistryServiceClient = new RegistryProviderUtil().getWSRegistry(automationContext);

        governance = new RegistryProviderUtil().getGovernanceRegistry(wsRegistryServiceClient, automationContext);
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance,
                GovernanceUtils.findGovernanceArtifactConfigurations(governance));
        artifactManager = new GenericArtifactManager(governance, "soapservice");

    }

    @Test(groups = { "wso2.greg" }, description = "create SOAP Service using GenericArtifact")
    public void createSOAPService() throws GovernanceException {
        GenericArtifact artifact = artifactManager.newGovernanceArtifact(new QName("SOAPService1"));

        artifact.setAttribute("overview_name", "SOAPService1");
        artifact.setAttribute("overview_version", "4.5.0");
        artifact.setAttribute("overview_description", "Description");

        artifactManager.addGenericArtifact(artifact);

        GenericArtifact receivedArtifact = artifactManager.getGenericArtifact(artifact.getId());
        assertEquals(artifact.getAttribute("overview_name"), receivedArtifact.getAttribute("overview_name"),
                " Service name must be equal");

        artifactManager.removeGenericArtifact(artifact.getId());
    }

    @Test(groups = "wso2.greg", description = "Add Schema without name", dependsOnMethods = "createSOAPService")
    public void addNoteTestCase() throws Exception {
        String session_id = authenticateJaggeryAPI();
        String endPoint = "https://localhost:10343/publisher/apis/assets?type=note";

        CookieStore cookieStore = new BasicCookieStore();
        HttpContext httpContext = new BasicHttpContext();
        httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(endPoint);
        post.setHeader("Cookie", "JSESSIONID=" + session_id);
        List nameValuePairs = new ArrayList(5);
        nameValuePairs.add(new BasicNameValuePair("overview_note", "note_one"));
        nameValuePairs.add(new BasicNameValuePair("overview_resourcepath",
                "/_system/governance/trunk/soapservices/4.5.0/SOAPService1"));
        nameValuePairs.add(new BasicNameValuePair("overview_visibility", "public"));
        nameValuePairs.add(new BasicNameValuePair("overview_note", "admin"));
        nameValuePairs.add(new BasicNameValuePair("overview_status", "open"));

        post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
        HttpResponse response = client.execute(post);
        assertEquals("Created",response.getStatusLine().getReasonPhrase());
    }

    @Test(groups = "wso2.greg", description = "Add Schema without name", dependsOnMethods = "addNoteTestCase")
    public void getNoteTestCase() throws Exception {
        Thread.sleep(60000);
        String session_id = authenticateJaggeryAPI();
        CookieStore cookieStore = new BasicCookieStore();
        HttpContext httpContext = new BasicHttpContext();
        httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

        HttpClient client = new DefaultHttpClient();

        URIBuilder builder = new URIBuilder();
        builder.setScheme("https").setHost("localhost:10343").setPath("/publisher/apis/assets")
                .setParameter("type", "note")
                .setParameter("q", "overview_resourcepath\":\"/_system/governance/trunk/soapservices/4.5.0/SOAPService1\"");
        URI uri = builder.build();
        HttpGet httpget = new HttpGet(uri);
        HttpResponse response = client.execute(httpget);
        assertEquals("OK",response.getStatusLine().getReasonPhrase());
    }

    private String authenticateJaggeryAPI() throws Exception {
        String endPoint = "https://localhost:10343/publisher/apis/authenticate";

        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(endPoint);
        List nameValuePairs = new ArrayList(2);
        nameValuePairs.add(new BasicNameValuePair("username", "admin"));
        nameValuePairs.add(new BasicNameValuePair("password", "admin"));
        post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
        HttpResponse response = client.execute(post);
        HttpEntity entity = response.getEntity();
        String responseString = EntityUtils.toString(entity, "UTF-8");
        if (response.getStatusLine().getStatusCode() != 200) {
            throw new Exception(" Authentication with Jaggery API failed: HTTP error code : " +
                    response.getStatusLine().getStatusCode());
        }
        JSONObject obj = new JSONObject(responseString);
        return obj.getJSONObject("data").getString("sessionId");
    }
}
