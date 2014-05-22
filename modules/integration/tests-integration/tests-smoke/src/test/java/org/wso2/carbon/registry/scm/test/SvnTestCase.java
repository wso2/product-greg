/*
*Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.scm.test;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.util.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.server.mgt.test.RegistryConfiguratorTestCase;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;

import javax.xml.namespace.QName;
import javax.xml.stream.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.rmi.RemoteException;

import static org.testng.AssertJUnit.assertTrue;

/**
 * Registry SCM mount test for SVN
 */
public class SvnTestCase extends GREGIntegrationBaseTest {

    private static final Log log = LogFactory.getLog(SvnTestCase.class);
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private boolean isSCMConfigAdded = false;
    private ServerConfigurationManager serverConfigurationManager;

    @BeforeClass(alwaysRun = true)
    public void addSCMConfiguration() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String sessionCookie = getSessionCookie();
        resourceAdminServiceClient = new ResourceAdminServiceClient(backendURL, sessionCookie);
        addScmConfiguration();
        serverConfigurationManager = new ServerConfigurationManager(automationContext);
        serverConfigurationManager.restartGracefully();
        sessionCookie = getSessionCookie();
        resourceAdminServiceClient = new ResourceAdminServiceClient(backendURL, sessionCookie);
    }

    @AfterClass(alwaysRun = true)
    public void removeSCMConfiguration() throws Exception {
        if(isSCMConfigAdded) {
            removeScmConfiguration();
            serverConfigurationManager.restartGracefully();
        }
    }

    /**
     * Get SCM mounted resource test
     *
     * @throws RemoteException
     * @throws ResourceAdminServiceExceptionException
     *
     */
    @Test(groups = "wso2.greg")
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    public void testSCMResourceExists() throws InterruptedException {
        boolean status = waitForPolicyFile();
        assertTrue("policy file not found ", status);
    }

    private boolean waitForPolicyFile() throws InterruptedException {
        long timeStart = System.currentTimeMillis();
        long lastTime =  timeStart + (60 * 1000);
        while(lastTime >= System.currentTimeMillis()) {
            try {
                String content = resourceAdminServiceClient.getTextContent
                        ("/_system/governance/policy/policy" + ".xml");

                if(content != null){
                    return true;
                }
            } catch(Exception ignored) {
                Thread.sleep(500);
            }

        }
        return false;
    }

    private void addScmConfiguration() throws Exception {
        FileOutputStream fileOutputStream = null;
        XMLStreamWriter writer = null;
        try {
            OMElement regConfig = getRegistryXmlOmElement();
            File checkOutDir = new File(getTempLocation());

            String scmConfig = "<scm>" +
                    "        <connection checkOutURL=\"scm:svn:https://svn.wso2.org/repos/wso2/carbon/" +
                    "platform/trunk/platform-integration/platform-automated-test-suite/" +
                    "org.wso2.carbon.automation.test.repo/src/main/resources/artifacts/GREG/" +
                    "policy\" workingDir=\"" + getTempLocation() + "\" mountPoint=\"/_system/" +
                    "governance/policy\" checkInURL=\"\" readOnly=\"\" updateFrequency=\"1\">" +
                    "                <username>anonymoususer</username>" +
                    "                <password>anonymoususer123</password>" +
                    "        </connection>" +
                    "    </scm>";

            OMElement scmConfigOMElement = AXIOMUtil.stringToOM(scmConfig);
            scmConfigOMElement.build();
            regConfig.addChild(scmConfigOMElement);
            fileOutputStream = new FileOutputStream(getRegistryXMLPath());
            writer = XMLOutputFactory.newInstance().createXMLStreamWriter(fileOutputStream);
            regConfig.serialize(writer);
            regConfig.build();
            isSCMConfigAdded = true;
            if(!checkOutDir.exists()) {
                checkOutDir.mkdir();
            }
            Thread.sleep(2000);
        } catch(Exception e) {
            log.error("registry.xml edit fails" + e.getMessage());
            throw new Exception("registry.xml edit fails" + e.getMessage());
        } finally {
            assert fileOutputStream != null;
            assert writer != null;
            writer.flush();
            writer.close();
            fileOutputStream.close();
        }
    }

    private void removeScmConfiguration() throws Exception {
        FileOutputStream fileOutputStream = null;
        XMLStreamWriter writer = null;
        try {
            OMElement regConfig = RegistryConfiguratorTestCase.getRegistryXmlOmElement();
            regConfig.getFirstChildWithName(new QName("scm")).discard();
            fileOutputStream = new FileOutputStream(getRegistryXMLPath());
            writer = XMLOutputFactory.newInstance().createXMLStreamWriter(fileOutputStream);
            regConfig.serialize(writer);
            regConfig.build();
            Thread.sleep(2000);
        } catch(Exception e) {
            log.error("registry.xml edit fails" + e.getMessage());
            throw new Exception("registry.xml edit fails" + e.getMessage());
        } finally {
            assert fileOutputStream != null;
            assert writer != null;
            writer.flush();
            writer.close();
            fileOutputStream.close();
        }
    }

    private String getRegistryXMLPath() {
        return CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator + "conf" +
                File.separator + "registry.xml";
    }

    private String getTempLocation() {
        return CarbonUtils.getCarbonHome() + File.separator + "checkout";
    }

    public static OMElement getRegistryXmlOmElement() throws FileNotFoundException, XMLStreamException {
        String registryXmlPath = CarbonUtils.getCarbonHome() + File.separator + "repository" +
                File.separator + "conf" + File.separator + "registry.xml";
        File registryFile = new File(registryXmlPath);
        FileInputStream inputStream = new FileInputStream(registryFile);
        XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(inputStream);
        StAXOMBuilder builder = new StAXOMBuilder(parser);
        OMElement registryXml = builder.getDocumentElement();
        registryXml.build();
        return registryXml;
    }
}
