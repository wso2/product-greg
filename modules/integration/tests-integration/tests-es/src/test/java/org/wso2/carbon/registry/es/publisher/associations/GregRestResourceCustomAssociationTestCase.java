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
package org.wso2.carbon.registry.es.publisher.associations;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.wink.client.ClientResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.ParseException;
import org.testng.annotations.*;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.es.utils.GregESTestBaseTest;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.greg.integration.common.utils.GenericRestClient;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.stream.*;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class GregRestResourceCustomAssociationTestCase extends GregESTestBaseTest {
    private TestUserMode userMode;
    String jSessionId;
    String testAssetId;
    String spaceAssetId;
    String cookieHeader;
    private GenericRestClient genericRestClient;
    private String publisherUrl;
    private Map<String, String> queryParamMap;
    private Map<String, String> headerMap;
    String resourcePath;
    private ServerConfigurationManager serverConfigurationManager;

    @Factory(dataProvider = "userModeProvider")
    public GregRestResourceCustomAssociationTestCase(TestUserMode userMode) {
        this.userMode = userMode;
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(userMode);
        genericRestClient = new GenericRestClient();
        queryParamMap = new HashMap<String, String>();
        headerMap = new HashMap<String, String>();
        resourcePath =
                FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator + "GREG" + File.separator;
        publisherUrl = automationContext.getContextUrls().getSecureServiceUrl().replace("services", "publisher/apis");
        serverConfigurationManager = new ServerConfigurationManager(automationContext);
        setTestEnvironment();
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws RegistryException {
        queryParamMap.put("type", "restservice");
        genericRestClient.geneticRestRequestDelete(publisherUrl + "/assets/" + testAssetId,
                MediaType.APPLICATION_JSON,
                MediaType.APPLICATION_JSON
                , queryParamMap, headerMap, cookieHeader);
        genericRestClient.geneticRestRequestDelete(publisherUrl + "/assets/" + spaceAssetId,
                MediaType.APPLICATION_JSON,
                MediaType.APPLICATION_JSON
                , queryParamMap, headerMap, cookieHeader);
    }

    private void setTestEnvironment() throws Exception {
        addCustomAssociation();
        JSONObject objSessionPublisher =
                new JSONObject(authenticate(publisherUrl, genericRestClient,
                        automationContext.getSuperTenant().getTenantAdmin().getUserName(),
                        automationContext.getSuperTenant().getTenantAdmin().getPassword())
                        .getEntity(String.class));
        jSessionId = objSessionPublisher.getJSONObject("data").getString("sessionId");
        cookieHeader = "JSESSIONID=" + jSessionId;
    }

    private void addCustomAssociation() throws Exception {
        changeAssociationElement();
        serverConfigurationManager.restartGracefully();
    }

    public OMElement getGovernanceXmlOmElement() throws IOException, XMLStreamException {

        FileInputStream inputStream = null;
        try {
            String govenanceXmlPath = CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator
                    + "conf" + File.separator + "governance.xml";
            File governanceFile = new File(govenanceXmlPath);
            inputStream = new FileInputStream(governanceFile);
            XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(inputStream);
            StAXOMBuilder builder = new StAXOMBuilder(parser);
            return builder.getDocumentElement();
        } catch (FileNotFoundException e) {
            log.error("Error when opening governance.xml");
            throw new FileNotFoundException(e.getMessage());
        } catch (XMLStreamException e) {
            log.error("Error when building governance.xml");
            throw new XMLStreamException(e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Create Test Rest Service")
    public void createTestRestServices() throws JSONException, IOException {
        queryParamMap.put("type", "restservice");
        String dataBody = readFile(resourcePath + "json" + File.separator + "publisherPublishRestResource.json");
        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/assets",
                        MediaType.APPLICATION_JSON,
                        MediaType.APPLICATION_JSON, dataBody
                        , queryParamMap, headerMap, cookieHeader);
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        assertTrue((response.getStatusCode() == Response.Status.CREATED.getStatusCode()),
                "Wrong status code ,Expected 201 Created ,Received " +
                        response.getStatusCode()
        );
        testAssetId = obj.get("id").toString();
        assertNotNull(testAssetId, "Empty asset resource id available" +
                response.getEntity(String.class));
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Create Space Rest Service",
            dependsOnMethods = {"createTestRestServices"})
    public void createSpaceRestServices() throws JSONException, IOException {
        queryParamMap.put("type", "restservice");
        String dataBody = readFile(resourcePath + "json" + File.separator + "publisherPublishRestSpaceResource.json");
        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/assets",
                        MediaType.APPLICATION_JSON,
                        MediaType.APPLICATION_JSON, dataBody
                        , queryParamMap, headerMap, cookieHeader);
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        assertTrue((response.getStatusCode() == Response.Status.CREATED.getStatusCode()),
                "Wrong status code ,Expected 201 Created ,Received " +
                        response.getStatusCode()
        );
        spaceAssetId = obj.get("id").toString();
        assertNotNull(spaceAssetId, "Empty asset resource id available" +
                response.getEntity(String.class));
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Create Association between Rest Services",
            dependsOnMethods = {"createSpaceRestServices"})
    public void createAssociation() throws JSONException, IOException, ParseException, InterruptedException {

        JSONObject dataObject = new JSONObject();

        dataObject.put("associationType", "governedBy");
        dataObject.put("destType", "restservice");
        dataObject.put("sourceType", "restservice");
        dataObject.put("destUUID", testAssetId);
        dataObject.put("sourceUUID", spaceAssetId);

        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/association", MediaType.APPLICATION_JSON,
                        MediaType.APPLICATION_JSON, dataObject.toString()
                        , queryParamMap, headerMap, cookieHeader);

        assertTrue((response.getStatusCode() == Response.Status.OK.getStatusCode()),
                "Wrong status code ,Expected" + Response.Status.OK.getStatusCode() + "Created ,Received " +
                        response.getStatusCode()
        );

        Thread.sleep(3000);

        ClientResponse associationList = genericRestClient.geneticRestRequestGet(publisherUrl +
                "/association/restservice/dependancies/" + spaceAssetId, queryParamMap, headerMap, cookieHeader);


        JsonArray jsonObject = new JsonParser().parse(associationList.getEntity(String.class)).
                getAsJsonObject().get("results").getAsJsonArray();


        assertTrue(jsonObject.toString().contains("uuid"));
        assertTrue(jsonObject.toString().contains(testAssetId));

    }

    private void changeAssociationElement() throws Exception {
        FileOutputStream fileOutputStream = null;
        XMLStreamWriter writer = null;
        OMElement documentElement = getGovernanceXmlOmElement();
        try {
            AXIOMXPath xpathExpression = new AXIOMXPath("/ns:GovernanceConfiguration/ns:AssociationConfig/ns:Association[@type='restservice']");
            xpathExpression.addNamespace("ns", "http://wso2.org/projects/carbon/governance.xml");
            String newAssociation = "<governedBy>restservice,soapservice,wsdl</governedBy>";
            List<OMElement> nodes = xpathExpression.selectNodes(documentElement);
            for (OMElement node : nodes) {
                node.addChild(AXIOMUtil.stringToOM(newAssociation));
                node.build();
                break;
            }

            fileOutputStream = new FileOutputStream(getGovernanceXMLPath());
            writer = XMLOutputFactory.newInstance().createXMLStreamWriter(fileOutputStream);
            documentElement.serialize(writer);
            documentElement.build();
            Thread.sleep(1000);

        } catch (Exception e) {
            log.error("Failed to modify governance.xml " + e.getMessage());
            throw new Exception("Failed to modify governance.xml " + e.getMessage());
        } finally {
            assert fileOutputStream != null;
            fileOutputStream.close();
            assert writer != null;
            writer.flush();
        }
    }

    private String getGovernanceXMLPath() {
        return CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator
                + "conf" + File.separator + "governance.xml";
    }

    @BeforeMethod(alwaysRun = true)
    public void resetParameters() {
        queryParamMap = new HashMap<String, String>();
        headerMap = new HashMap<String, String>();
    }

    @DataProvider
    private static TestUserMode[][] userModeProvider() {
        return new TestUserMode[][]{
                new TestUserMode[]{TestUserMode.SUPER_TENANT_ADMIN},
//                new TestUserMode[]{TestUserMode.TENANT_USER},
        };
    }
}
