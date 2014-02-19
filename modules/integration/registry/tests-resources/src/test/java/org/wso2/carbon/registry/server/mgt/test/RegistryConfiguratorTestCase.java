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
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
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
import org.wso2.carbon.automation.core.utils.fileutils.FileManager;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkFactory;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkProperties;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkSettings;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.carbon.utils.CarbonUtils;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.stream.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

public class RegistryConfiguratorTestCase {

    private static final Log log = LogFactory.getLog(RegistryConfiguratorTestCase.class);
    private FrameworkProperties frameworkProperties;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private ServerAdminClient serverAdminClient;

    @BeforeClass(alwaysRun = true)
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_user})
    public void serverRestart() throws Exception {
        int userId = 0;
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        ManageEnvironment environment = builder.build();
        UserInfo userInfo = UserListCsvReader.getUserInfo(userId);

        serverAdminClient =
                new ServerAdminClient(environment.getGreg().getBackEndUrl(),
                                      userInfo.getUserName(), userInfo.getPassword());

        frameworkProperties =
                FrameworkFactory.getFrameworkProperties(ProductConstant.GREG_SERVER_NAME);

        resourceAdminServiceClient =
                new ResourceAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                               userInfo.getUserName(), userInfo.getPassword());


    }

    @Test(groups = "wso2.greg")
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_user})
    public void testSetupServerEnvironment() throws Exception {
        copyResources();
        copyJarFiles();
        editConfigurationFiles();
        updateRegistry();
        addResourceFileToRegistry();
        ServerGroupManager.getServerUtils().restartGracefully(serverAdminClient, frameworkProperties);
    }


    public void copyResources() throws Exception {
        copyMimeMappingFile();

    }

    public void copyJarFiles() {

    }

    public void updateRegistry() throws RegistryException, AxisFault {
        updateMimetypes();
    }

    public void editConfigurationFiles() throws Exception {
        increaseSearchIndexStartTimeDelay();
        enableJmxManagement();
        enableWorkList();
    }

    public void updateMimetypes() throws RegistryException, AxisFault {
        final String MIME_TYPE_PATH = "/_system/config/repository/components/org.wso2.carbon.governance/media-types/index";
        WSRegistryServiceClient wsRegistry;
        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();

        wsRegistry = registryProviderUtil.getWSRegistry(0, ProductConstant.GREG_SERVER_NAME);
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

        String sourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
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
        EnvironmentBuilder environmentBuilder = new EnvironmentBuilder();
        FrameworkSettings framework = environmentBuilder.getFrameworkSettings();
        String workList;
        UserInfo userInfo = UserListCsvReader.getUserInfo(ProductConstant.ADMIN_USER_ID);

        if (framework.getEnvironmentSettings().is_runningOnStratos()) {
            workList = "<workList serverURL=\"local://services/\" remote=\"false\">\n" +
                              "        <username>" + userInfo.getUserNameWithoutDomain() + "</username>\n" +
                              "        <password>" + userInfo.getPassword() + "</password>\n" +
                              "    </workList>";
        } else {
            workList = "<workList serverURL=\"local://services/\" remote=\"false\">\n" +
                              "        <username>" + userInfo.getUserName() + "</username>\n" +
                              "        <password>" + userInfo.getPassword() + "</password>\n" +
                              "    </workList>";
        }


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
        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
                              File.separator + "GREG" + File.separator + "utf8" + File.separator + "test.txt";

        DataHandler dh = new DataHandler(new URL("file:///" + resourcePath));
        resourceAdminServiceClient.addResource("/_system/config/test_utf8_Resource", "text/plain", "testDesc", dh);
    }

}

