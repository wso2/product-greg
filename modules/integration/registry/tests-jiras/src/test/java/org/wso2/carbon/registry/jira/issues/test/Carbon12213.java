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
package org.wso2.carbon.registry.jira.issues.test;


import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
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
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkFactory;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkProperties;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.utils.CarbonUtils;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.stream.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Iterator;

public class Carbon12213 {

    private ResourceAdminServiceClient resourceAdminServiceClient;
    private String schemaPath;
    private String transcriptFile;
    private ServerAdminClient serverAdminClient;
    private int userId;


    @BeforeClass(groups = {"wso2.greg"})
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_user})
    public void init() throws Exception {
        userId = ProductConstant.ADMIN_USER_ID;
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        ManageEnvironment environment = builder.build();
        
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                               environment.getGreg().getSessionCookie());
        serverAdminClient =
                new ServerAdminClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                      environment.getGreg().getSessionCookie());
    }

    @Test(groups = {"wso2.greg"}, description = "change registry.xml and restart server")
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_user})
    public void editRegistryXML() throws Exception {

        String registryXmlPath = CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator
                                 + "conf" + File.separator + "registry.xml";

        FileOutputStream fileOutputStream = null;
        XMLStreamWriter writer = null;
        File srcFile = new File(registryXmlPath);
        try {
            OMElement handlerConfig = AXIOMUtil.stringToOM("<property name=\"useOriginalSchema\">true</property>");
            OMElement registryXML = getRegistryXmlOmElement();

            OMElement om1;
            for (Iterator iterator = registryXML.getChildrenWithName(new QName("handler")); iterator.hasNext(); ) {
                OMElement om = (OMElement) iterator.next();

                if (om.getAttribute(new QName("class")).getAttributeValue().equals("org.wso2.carbon.registry.extensions.handlers" +
                                                                                   ".ZipWSDLMediaTypeHandler")) {
                    om1 = om;
                    om1.addChild(handlerConfig);
                    registryXML.addChild(om1);
                    registryXML.build();
                    break;
                }

            }

            fileOutputStream = new FileOutputStream(srcFile);
            writer =
                    XMLOutputFactory.newInstance().createXMLStreamWriter(fileOutputStream);
            registryXML.serialize(writer);


        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("Registry.xml file not found" + e);

        } catch (XMLStreamException e) {
            throw new XMLStreamException("XML stream exception" + e);

        } catch (IOException e) {
            throw new IOException("IO exception" + e);

        } finally {
            if (writer != null) {
                writer.close();
            }
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
        }

        restartServer();
        
        
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        ManageEnvironment environment = builder.build();

        //reinitialize environment after server restart
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                               environment.getGreg().getSessionCookie());
        serverAdminClient =
                new ServerAdminClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                      environment.getGreg().getSessionCookie());
    }


    @Test(groups = {"wso2.greg"}, dependsOnMethods = "editRegistryXML")
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_user})
    public void addSchema() throws InterruptedException, MalformedURLException,
                                   ResourceAdminServiceExceptionException, RemoteException {
        String resource = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION
                          + "artifacts" + File.separator + "GREG" + File.separator + "gar" + File.separator
                          + "transcript.gar";

        schemaPath = "/_system/governance/trunk/schemas/wifi/digitalcircuit/ops/schema/transcript/_2005/transcript-schema.xsd";

        transcriptFile = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator +
                         "GREG" + File.separator +
                         "schema" + File.separator + "transcript-schema.xsd";

        resourceAdminServiceClient.addResource(resource, "application/vnd.wso2.governance-archive",
                                               "Adding Schema From Zip file", new DataHandler(new URL("file:///" + resource)));
        // wait for sometime until the schema has been added. The activity logs are written
        // every 10 seconds, so you'll need to wait until that's done.
        Thread.sleep(20000);
    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = "addSchema",
          description = "Checking whether Content is equal before adding and after adding")
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_user})
    public void compareSchema() throws IOException, ResourceAdminServiceExceptionException {
        File file = new File(transcriptFile);
        String contentOriginal = FileUtils.readFileToString(file);
        String contentUploadedSchema = resourceAdminServiceClient.getTextContent(schemaPath);
        Assert.assertEquals(contentOriginal, contentUploadedSchema);
    }

    @AfterClass(groups = {"wso2.greg"})
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_user})
    public void deleteSchema() throws Exception {
        resourceAdminServiceClient.deleteResource(schemaPath);
        String registryXmlPath = CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator
                                 + "conf" + File.separator + "registry.xml";

        FileOutputStream fileOutputStream = null;
        XMLStreamWriter writer = null;
        try {
            File srcFile = new File(registryXmlPath);
            OMElement element = getRegistryXmlOmElement();
            OMElement omElement = null;

            for (Iterator iterator = element.getChildrenWithName(new QName("handler")); iterator.hasNext(); ) {
                OMElement om = (OMElement) iterator.next();

                if (om.getAttribute(new QName("class")).getAttributeValue().equals("org.wso2.carbon.registry.extensions.handlers" +
                                                                                   ".ZipWSDLMediaTypeHandler")) {

                    omElement = om;
                    for (Iterator it = omElement.getChildrenWithName(new QName("property")); it.hasNext(); ) {
                        OMElement omRemove = (OMElement) it.next();
                        if (omRemove.getAttribute(new QName("name")).getAttributeValue().equals("useOriginalSchema")) {
                            it.remove();
                            element.build();
                            break;
                        }
                    }
                    break;
                }
            }

            fileOutputStream = new FileOutputStream(srcFile);
            writer = XMLOutputFactory.newInstance().createXMLStreamWriter(fileOutputStream);
            element.serialize(writer);
            restartServer();

        } finally {
            if (writer != null) {
                writer.close();
            }
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }

        }
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


    private void restartServer() throws Exception {
        FrameworkProperties frameworkProperties = FrameworkFactory.getFrameworkProperties(ProductConstant.GREG_SERVER_NAME);
        ServerGroupManager.getServerUtils().restartGracefully(serverAdminClient, frameworkProperties);
    }


}
