/*
 * Copyright 2004,2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.registry.jira.issues.test;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.registry.resource.stub.common.xsd.ResourceData;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import javax.activation.DataHandler;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.net.URL;

import static org.testng.Assert.assertTrue;

public class Carbon6014TestCase extends GREGIntegrationBaseTest {

    private ResourceAdminServiceClient resourceAdminServiceClient;
    private WSRegistryServiceClient wsRegistryServiceClient;
    private static final String OLD_SERVICE_PATH = "/trunk/services/";
    private static final String NEW_SERVICE_PATH = "/trunk/new/services/";
    private String servicePath;


    @BeforeClass(alwaysRun = true)
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    public void init() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String session = getBackendURL();
        editRegistry(NEW_SERVICE_PATH);
        restartServer();
    }


    private void editRegistry(String pathToChange) throws java.lang.Exception {

        String configPath =
                CarbonUtils.getCarbonHome() + File.separator + "repository" +
                File.separator + "conf" + File.separator + "registry.xml";

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(configPath);
        Element rootElement = document.getDocumentElement();
        Node nd = rootElement.getElementsByTagName("staticConfiguration").item(0);
        NodeList list = nd.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {

            Node node = list.item(i);

            if ("servicePath".equals(node.getNodeName())) {
                node.setTextContent(pathToChange);
            }
        }

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(new File(configPath));
        transformer.transform(source, result);

    }


    @Test(groups = "wso2.greg", description = "")
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    public void testAddService() throws java.lang.Exception {


        String serviceXmlPath =
                getTestArtifactLocation() + "artifacts" +
                File.separator + "GREG" + File.separator + "services" +
                File.separator + "intergalacticService.metadata.xml";

        DataHandler dataHandler = new DataHandler(new URL("file:///" + serviceXmlPath));
        String mediaType = "application/vnd.wso2-service+xml";
        String description = "This is a test service";
        resourceAdminServiceClient.addResource("/_system/governance/service1", mediaType, description, dataHandler);

        ResourceData[] resourceDataArray =  resourceAdminServiceClient.getResource(
                "/_system/governance" + NEW_SERVICE_PATH + "com/abb/IntergalacticService");

        assertTrue(resourceDataArray.length == 1, "Service path not changed");

    }

    /**
     * @throws java.lang.Exception
     * @throws java.lang.Exception
     */
    private void restartServer() throws java.lang.Exception {
        ServerConfigurationManager serverConfigurationManager = new ServerConfigurationManager
                                                                                (automationContext);
        serverConfigurationManager.restartGracefully();

        resourceAdminServiceClient =
                new ResourceAdminServiceClient(backendURL, getSessionCookie());

        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        wsRegistryServiceClient =
                registryProviderUtil.getWSRegistry(automationContext);

    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass()
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    public void clear() throws java.lang.Exception {
        resourceAdminServiceClient.deleteResource("/_system/governance/trunk/services");
        resourceAdminServiceClient.deleteResource("/_system/governance/trunk/wsdls");
        resourceAdminServiceClient.deleteResource("/_system/governance/trunk/new");
        resourceAdminServiceClient.deleteResource("/_system/governance/trunk");
        editRegistry(OLD_SERVICE_PATH);
        restartServer();
    }

}
