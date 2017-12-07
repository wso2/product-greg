/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.registry.jira2.issues.test2;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.integration.common.utils.exceptions.AutomationUtilException;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.xml.sax.SAXException;

import java.io.*;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.Scanner;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.*;
import javax.xml.xpath.XPathExpressionException;

import static org.testng.Assert.assertTrue;

/**
 * This test case is the fix for the patch WSO2-CARBON-PATCH-4.4.0-0102
 * Problem domain of the patch was Can't connect GREG to ESB and mediation error found in
 * carbon.log file.
 */
public class Carbon0102TestCase  extends GREGIntegrationBaseTest {

    private static final Log log = LogFactory.getLog(Carbon0102TestCase.class);
    private static final String CLASS_NOT_FOUND_EXCEPTION = "java.lang.ClassNotFoundException: org.wso2.carbon"
            + ".governance.platform.extensions.mediation.MediationArtifactPopulatorTask";
    private static final String LOG_FILE = "wso2carbon.log";
    private String carbonHome;
    String registryXmlPath;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        userInfo = automationContext.getContextTenant().getContextUser();
        // Get the carbon home
        carbonHome = CarbonUtils.getCarbonHome();

        registryXmlPath =
                CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator + "conf" + File.separator
                        + "registry.xml";
        log.info("Updating registry.xml file with new configurations");
        updateRegistryXMLConfigurations();
        restartServer();
    }

    @Test(groups = "wso2.greg", description = "check the carbon.log file content on server startup")
    public void checkServerLogsStartup()
            throws IOException, ParserConfigurationException, SAXException, InterruptedException {
        Thread.sleep(5000);
        // Read the logs
        String readCarbonLogs = readCarbonLogs();
        log.info("Read the " + LOG_FILE + " file successfully");
        assertTrue(!readCarbonLogs.contains(CLASS_NOT_FOUND_EXCEPTION),
                "Error in executing task Mediation Artifact Populator Task");
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws Exception {
        log.info("removing configuration from registry.xml");
        renewRegistryXML();
        restartServer();
    }

    /**
     * This method will return registry xml file nodes
     *
     * @return OMElement
     * @throws FileNotFoundException
     * @throws XMLStreamException
     */
    public static OMElement getRegistryXmlOmElement() throws FileNotFoundException, XMLStreamException {
        String registryXmlPath =
                CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator + "conf" + File.separator
                        + "registry.xml";
        File registryFile = new File(registryXmlPath);
        FileInputStream inputStream = new FileInputStream(registryFile);
        XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(inputStream);
        StAXOMBuilder builder = new StAXOMBuilder(parser);
        return builder.getDocumentElement();
    }

    /**
     * Method to read the carbon.log file content
     *
     * @return log content as a string
     * @throws FileNotFoundException Log file cannot be find
     */
    private String readCarbonLogs() throws FileNotFoundException {
        File carbonLogFile = new File(carbonHome + File.separator + "repository" + File.separator + "logs" +
                File.separator + LOG_FILE);
        return new Scanner(carbonLogFile).useDelimiter("\\A").next();
    }

    /**
     * This method will restart the server.
     *
     * @throws AutomationUtilException
     * @throws XPathExpressionException
     * @throws MalformedURLException
     */
    private void restartServer() throws AutomationUtilException, XPathExpressionException, MalformedURLException {
        ServerConfigurationManager serverConfigurationManager = new ServerConfigurationManager(automationContext);
        serverConfigurationManager.restartGracefully();
    }

    /**
     * Update the registry XML file with tasks configurations
     *
     * @throws IOException
     * @throws XMLStreamException
     */
    public void updateRegistryXMLConfigurations() throws IOException, XMLStreamException {

        FileOutputStream fileOutputStream = null;
        XMLStreamWriter writer = null;
        File srcFile = new File(registryXmlPath);
        try {
            OMElement handlerConfig = AXIOMUtil.stringToOM(
                    "\n<task name=\"MediationTask\" class=\"org.wso2.carbon.governance.platform.extensions.mediation."
                            + "MediationArtifactPopulatorTask\">\n" + "            <trigger cron=\"1/10 * * * * ?\"/>\n"
                            + "            <property key=\"userName\" value=\"admin\" />\n"
                            + "            <property key=\"password\" value=\"admin\" />\n"
                            + "            <property key=\"serverUrl\" value=\"https://localhost:9444/services/\"/>\n"
                            + "            <property key=\"proxyArtifactKey\" value=\"proxy\" />\n"
                            + "            <property key=\"sequenceArtifactKey\" value=\"sequence\" />\n"
                            + "            <property key=\"endpointArtifactKey\" value=\"endpoint\" />\n" +
                            "</task>");

            OMElement registryXML = getRegistryXmlOmElement();
            Iterator iterator = registryXML.getChildrenWithName(new QName("tasks"));
            OMElement om = (OMElement) iterator.next();
            om.addChild(handlerConfig);
            registryXML.addChild(om);
            registryXML.build();

            fileOutputStream = new FileOutputStream(srcFile);
            writer = XMLOutputFactory.newInstance().createXMLStreamWriter(fileOutputStream);
            registryXML.serialize(writer);

        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("Registry.xml file not found" + e);

        } catch (XMLStreamException e) {
            throw new XMLStreamException("XML stream exception" + e);

        } finally {
            if (writer != null) {
                writer.close();
            }
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
        }

    }

    /**
     * Remove the changes from the registry xml.
     * @throws IOException
     * @throws XMLStreamException
     */
    public void renewRegistryXML() throws IOException, XMLStreamException {

        FileOutputStream fileOutputStream = null;
        XMLStreamWriter writer = null;
        File srcFile = new File(registryXmlPath);
        try {

            OMElement registryXML = getRegistryXmlOmElement();
            Iterator iterator = registryXML.getChildrenWithName(new QName("tasks"));
            OMElement ele = (OMElement) iterator.next();
            Iterator childIterator = ele.getChildrenWithName(new QName("task"));
            childIterator.remove();
            registryXML.build();
            fileOutputStream = new FileOutputStream(srcFile);
            writer = XMLOutputFactory.newInstance().createXMLStreamWriter(fileOutputStream);
            registryXML.serialize(writer);

        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("Registry.xml file not found" + e);

        } catch (XMLStreamException e) {
            throw new XMLStreamException("XML stream exception" + e);

        } finally {
            if (writer != null) {
                writer.close();
            }
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
        }

    }

}
