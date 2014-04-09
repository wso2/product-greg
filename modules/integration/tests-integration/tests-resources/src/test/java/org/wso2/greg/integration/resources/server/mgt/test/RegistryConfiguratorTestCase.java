/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.greg.integration.resources.server.mgt.test;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.automation.test.utils.common.FileManager;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.stream.*;
import javax.xml.xpath.XPathExpressionException;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

public class RegistryConfiguratorTestCase extends GREGIntegrationBaseTest {

    private static final Log log = LogFactory.getLog(RegistryConfiguratorTestCase.class);
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private ServerConfigurationManager serverConfigurationManager;

    @BeforeClass(alwaysRun = true)
    // @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_user})
    public void serverRestart() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(getBackendURL(),
                        automationContext.getUser().getUserName(), automationContext.getUser().getPassword());

        serverConfigurationManager = new ServerConfigurationManager("GREG", TestUserMode.SUPER_TENANT_ADMIN);
    }

    @Test(groups = "wso2.greg")
    // @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_user})
    public void testSetupServerEnvironment() throws Exception {
        copyResources();
        editConfigurationFiles();
        updateRegistry();
        addResourceFileToRegistry();
        // ServerGroupManager.getServerUtils().restartGracefully(serverAdminClient, frameworkProperties);
        serverConfigurationManager.restartGracefully();
    }


    private void copyResources() throws Exception {
        copyMimeMappingFile();

    }

    private void updateRegistry() throws RegistryException, AxisFault, XPathExpressionException {
        updateMimetypes();
    }

    private void editConfigurationFiles() throws Exception {
        increaseSearchIndexStartTimeDelay();
        enableJmxManagement();
        enableWorkList();
    }

    private void updateMimetypes() throws RegistryException, AxisFault, XPathExpressionException {

        final String MIME_TYPE_PATH = "/_system/config/repository/components/org.wso2.carbon.governance/media-types/index";
        WSRegistryServiceClient wsRegistry;
        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();

        wsRegistry = registryProviderUtil.getWSRegistry("GREG", "greg001",
                automationContext.getConfigurationNode("//superTenant/tenant/@key").getNodeValue(),
                automationContext.getSuperTenant().getTenantAdmin().getKey());
        Resource resource = wsRegistry.get(MIME_TYPE_PATH);
        resource.addProperty("properties", "text/properties");
        resource.addProperty("cfg", "text/config");
        resource.addProperty("rb", "text/ruby");
        resource.addProperty("drl", "xml/drool");
        resource.addProperty("xq", "xml/xquery");
        resource.addProperty("eva", "xml/evan");
        wsRegistry.put(MIME_TYPE_PATH, resource);

    }

    private void copyMimeMappingFile() throws IOException {
        String fileName = "mime.mappings";

        String targetPath = CarbonUtils.getCarbonHome() + File.separator + "repository" +
                File.separator + "conf" + File.separator + "etc";

        String sourcePath = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" +
                File.separator + "GREG" + File.separator + "config" + File.separator +
                "mime.mappings";

        FileManager.copyResourceToFileSystem(sourcePath, targetPath, fileName);
    }

    public static OMElement getRegistryXmlOmElement()
            throws FileNotFoundException, XMLStreamException {
        String registryXmlPath = CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator
                + "conf" + File.separator + "registry.xml";

        File registryFile = new File(registryXmlPath);

        FileInputStream inputStream = new FileInputStream(registryFile);
        XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(inputStream);
        StAXOMBuilder builder = new StAXOMBuilder(parser);

        return builder.getDocumentElement();

    }

    private void increaseSearchIndexStartTimeDelay() throws Exception {
        FileOutputStream fileOutputStream = null;
        XMLStreamWriter writer = null;
        OMElement documentElement = getRegistryXmlOmElement();
        try {
            AXIOMXPath xpathExpression = new AXIOMXPath("/wso2registry/indexingConfiguration/startingDelayInSeconds");
            OMElement indexConfigNode = (OMElement) xpathExpression.selectSingleNode(documentElement);
            indexConfigNode.setText("60");

            AXIOMXPath xpathExpression1 = new AXIOMXPath("/wso2registry/indexingConfiguration/indexingFrequencyInSeconds");
            OMElement indexConfigNode1 = (OMElement) xpathExpression1.selectSingleNode(documentElement);
            indexConfigNode1.setText("30");

            fileOutputStream = new FileOutputStream(getRegistryXMLPath());
            writer = XMLOutputFactory.newInstance().createXMLStreamWriter(fileOutputStream);
            documentElement.serialize(writer);
            documentElement.build();
            Thread.sleep(2000);

        } catch (Exception e) {
            log.error("registry.xml edit fails" + e.getMessage());
            throw new Exception("registry.xml edit fails" + e.getMessage());
        } finally {
            assert fileOutputStream != null;
            fileOutputStream.close();
            assert writer != null;
            writer.flush();
        }
    }

    private void enableJmxManagement() throws Exception {
        FileOutputStream fileOutputStream = null;
        XMLStreamWriter writer = null;
        OMElement documentElement = getRegistryXmlOmElement();
        try {
            AXIOMXPath xpathExpression = new AXIOMXPath("/wso2registry/jmx");
            OMElement indexConfigNode = (OMElement) xpathExpression.selectSingleNode(documentElement);
            OMAttribute omAttribute = indexConfigNode.getAttribute(new QName("enabled"));
            omAttribute.setAttributeValue("true");

            fileOutputStream = new FileOutputStream(getRegistryXMLPath());
            writer = XMLOutputFactory.newInstance().createXMLStreamWriter(fileOutputStream);
            documentElement.serialize(writer);
            documentElement.build();
            Thread.sleep(2000);

        } catch (Exception e) {
            log.error("registry.xml edit fails" + e.getMessage());
            throw new Exception("registry.xml edit fails" + e.getMessage());
        } finally {
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            if (writer != null) {
                writer.flush();
            }
        }
    }

    private void enableWorkList() throws Exception {
        FileOutputStream fileOutputStream = null;
        XMLStreamWriter writer = null;

        String workList = "<workList serverURL=\"local://services/\" remote=\"false\">\n" +
                "        <username>" + automationContext.getUser().getUserName() + "</username>\n" +
                "        <password>" + automationContext.getUser().getPassword() + "</password>\n" +
                "    </workList>";

        try {
            OMElement registryXML = getRegistryXmlOmElement();

            registryXML.addChild(AXIOMUtil.stringToOM(workList));
            registryXML.build();
            fileOutputStream = new FileOutputStream(getRegistryXMLPath());
            writer = XMLOutputFactory.newInstance().createXMLStreamWriter(fileOutputStream);
            registryXML.serialize(writer);


        } catch (Exception e) {
            throw e;
        } finally {
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            if (writer != null) {
                writer.flush();
            }
        }

    }

    private String getRegistryXMLPath() {
        return CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator
                + "conf" + File.separator + "registry.xml";
    }

    private void addResourceFileToRegistry()
            throws MalformedURLException, ResourceAdminServiceExceptionException, RemoteException {
        String resourcePath = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" +
                File.separator + "GREG" + File.separator + "utf8" + File.separator + "test.txt";

        DataHandler dh = new DataHandler(new URL("file:///" + resourcePath));
        resourceAdminServiceClient.addResource("/_system/config/test_utf8_Resource", "text/plain", "testDesc", dh);
    }
}

