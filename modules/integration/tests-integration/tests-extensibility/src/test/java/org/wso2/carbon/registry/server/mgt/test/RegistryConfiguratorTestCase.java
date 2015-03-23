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

package org.wso2.carbon.registry.server.mgt.test;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.automation.test.utils.common.FileManager;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.stream.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

public class RegistryConfiguratorTestCase extends GREGIntegrationBaseTest {

    private static final Log log = LogFactory.getLog(RegistryConfiguratorTestCase.class);
    private ResourceAdminServiceClient resourceAdminServiceClient;

    private String sessionCookie;
    private String backEndUrl;
    private String userName;
    private String userNameWithoutDomain;

    @BeforeSuite(alwaysRun = true)
    public void serverRestart() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        backEndUrl = getBackendURL();
        sessionCookie = getSessionCookie();
        userName = automationContext.getContextTenant().getContextUser().getUserName();

        if (userName.contains("@"))
            userNameWithoutDomain = userName.substring(0, userName.indexOf('@'));
        else
            userNameWithoutDomain = userName;
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(backEndUrl,
                                               sessionCookie);
       // testSetupServerEnvironment();


    }

   @Test(groups = "wso2.greg")
   @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    public void testSetupServerEnvironment() throws Exception {
        copyResources();
        copyJarFiles();
        editConfigurationFiles();
        updateRegistry();
       /*adding the resource from here fails the corresponding test case (UTFSupportForMetadataTestCase.testSearchByKeywords()),
       so moved the resource to the class level.*/
       // addResourceFileToRegistry();
        ServerConfigurationManager serverConfigurationManager =
                new ServerConfigurationManager(automationContext);
        serverConfigurationManager.restartGracefully();
    }


    public void copyResources() throws Exception {
        copyMimeMappingFile();

    }

    public void copyJarFiles() {

    }

    public void updateRegistry() throws Exception {
        updateMimetypes();
    }

    public void editConfigurationFiles() throws Exception {
        increaseSearchIndexStartTimeDelay();
        enableJmxManagement();
        enableWorkList();
    }

    public void updateMimetypes() throws Exception {
        final String MIME_TYPE_PATH = "/_system/config/repository/components/org.wso2.carbon.governance/media-types/index";
        WSRegistryServiceClient wsRegistry;
        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();

        wsRegistry = registryProviderUtil.getWSRegistry(automationContext);
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
            indexConfigNode.setText("30");
            AXIOMXPath xpathExpression1 = new AXIOMXPath("/wso2registry/indexingConfiguration/indexingFrequencyInSeconds");
            OMElement indexConfigNode1 = (OMElement) xpathExpression1.selectSingleNode(documentElement);
            indexConfigNode1.setText("3");
            AXIOMXPath xpathExpression2 = new AXIOMXPath("/wso2registry/indexingConfiguration/batchSize");
            OMElement indexConfigNode2 = (OMElement) xpathExpression2.selectSingleNode(documentElement);
            indexConfigNode2.setText("120");
            AXIOMXPath xpathExpression3 = new AXIOMXPath("/wso2registry/indexingConfiguration/indexerPoolSize");
            OMElement indexConfigNode3 = (OMElement) xpathExpression3.selectSingleNode(documentElement);
            indexConfigNode3.setText("50");

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

        String workList;



            workList = "<workList serverURL=\"local://services/\" remote=\"false\">\n" +
                              "        <username>" + userNameWithoutDomain + "</username>\n" +
                              "        <password>" + automationContext.getContextTenant().getTenantAdmin().getPassword() + "</password>\n" +
                              "    </workList>";


        try {
            OMElement registryXML = getRegistryXmlOmElement();

            registryXML.addChild(AXIOMUtil.stringToOM(workList));
            registryXML.build();
            fileOutputStream = new FileOutputStream(getRegistryXMLPath());
            writer = XMLOutputFactory.newInstance().createXMLStreamWriter(fileOutputStream);
            registryXML.serialize(writer);


        } catch (Exception e) {
            throw new Exception("registry.xml update fails");
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

