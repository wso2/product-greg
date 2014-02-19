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
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.api.clients.server.admin.ServerAdminClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.ServerGroupManager;
import org.wso2.carbon.automation.core.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.core.annotations.SetEnvironment;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkFactory;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkProperties;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.stub.common.xsd.ResourceData;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.carbon.utils.CarbonUtils;

import javax.activation.DataHandler;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.testng.Assert.assertTrue;

public class Carbon6014 {

    int userId = ProductConstant.ADMIN_USER_ID;
    private UserInfo userInfo = UserListCsvReader.getUserInfo(userId);
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private WSRegistryServiceClient wsRegistryServiceClient;
    private static final String OLD_SERVICE_PATH = "/trunk/services/";
    private static final String NEW_SERVICE_PATH = "/trunk/new/services/";
    private ManageEnvironment environment;
    private String servicePath;

    /**
     * @throws LoginAuthenticationExceptionException
     *
     * @throws XMLStreamException
     * @throws FactoryConfigurationError
     * @throws IOException
     * @throws RegistryException
     */
    @BeforeClass(alwaysRun = true)
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_user})
    public void init() throws LoginAuthenticationExceptionException, XMLStreamException,
                              FactoryConfigurationError, IOException, RegistryException {

        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        environment = builder.build();
    }

    /**
     * @param pathToChange new service location
     * @throws java.lang.Exception
     */
    public void editRegistry(String pathToChange) throws java.lang.Exception {

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

    /**
     * @throws java.lang.Exception
     */
    @Test(groups = "wso2.greg", description = "")
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_user})
    public void testAddService() throws java.lang.Exception {

        editRegistry(NEW_SERVICE_PATH);
        restartServer();


        String serviceXmlPath =
                ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
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
        ServerAdminClient serverAdminClient =
                new ServerAdminClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                      environment.getGreg().getSessionCookie());
        FrameworkProperties frameworkProperties =
                FrameworkFactory.getFrameworkProperties(ProductConstant.GREG_SERVER_NAME);
        ServerGroupManager.getServerUtils().restartGracefully(serverAdminClient, frameworkProperties);
        //build the environment again after restart
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        environment = builder.build();
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                               environment.getGreg().getSessionCookie());

        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        wsRegistryServiceClient =
                registryProviderUtil.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);

    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass()
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_user})
    public void clear() throws java.lang.Exception {
        resourceAdminServiceClient.deleteResource("/_system/governance/trunk/services");
        resourceAdminServiceClient.deleteResource("/_system/governance/trunk/wsdls");
        resourceAdminServiceClient.deleteResource("/_system/governance/trunk/new");
        resourceAdminServiceClient.deleteResource("/_system/governance/trunk");
        editRegistry(OLD_SERVICE_PATH);
        restartServer();

        userInfo = null;
        resourceAdminServiceClient = null;
        environment = null;
        wsRegistryServiceClient = null;

    }

}
