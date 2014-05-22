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

package org.wso2.carbon.registry.jira2.issues.test2;

import org.apache.axis2.AxisFault;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.integration.common.admin.client.ServerAdminClient;
import org.wso2.carbon.integration.common.admin.client.UserManagementClient;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;

public class Carbon9161TestCase extends GREGIntegrationBaseTest {

    private static final String ROLE_NAME = "carbon9161role";

    private UserManagementClient userManagementClient;
    private ServerAdminClient serverAdminClient;

    private void setUserStoreReadOnly(boolean state) throws Exception {
        String userMgtPath = CarbonUtils.getCarbonHome() + File.separator + "repository" +
                             File.separator + "conf" + File.separator + "user-mgt.xml";

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(new File(userMgtPath));
        Element rootElement = doc.getDocumentElement();
        Node nd = rootElement.getElementsByTagName("UserStoreManager").item(0);
        NodeList list = nd.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            if ("Property".equals(node.getNodeName())) {
                NamedNodeMap attribMap = node.getAttributes();
                Node attrib = attribMap.getNamedItem("name");
                if ("ReadOnly".equals(attrib.getTextContent())) {
                    node.setTextContent(String.valueOf(state));
                }
            }
        }

        //backup old config file
        File oldFile = new File(userMgtPath);
        oldFile.renameTo(new File(userMgtPath + ".bak"));

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(userMgtPath);
        transformer.transform(source, result);
    }

    private void restartServer() throws Exception {
        ServerConfigurationManager serverConfigurationManager = new ServerConfigurationManager
                (automationContext);
        serverConfigurationManager.restartGracefully();
    }

    @BeforeClass(alwaysRun = true)
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    public void copyMappingFileAndRestart() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String session = getSessionCookie();
        userManagementClient = new UserManagementClient(backendURL, session);
        userManagementClient.addRole(ROLE_NAME, new String[]{}, new String[]{});
        serverAdminClient = new ServerAdminClient(backendURL, session);

        setUserStoreReadOnly(true);
        restartServer();

        session = getSessionCookie();
        userManagementClient = new UserManagementClient(backendURL, session);
        serverAdminClient = new ServerAdminClient(backendURL, session);
    }

    @Test(expectedExceptions = AxisFault.class)
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    public void testReadWriteRoles() throws Exception {
        //as the datastore is made read only this was added to internal userstore. So the operation was allowed.
        //with the fix this operation should fail
        userManagementClient.addRole(ROLE_NAME, new String[]{}, new String[]{});
    }

    @AfterClass(alwaysRun = true)
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    public void cleanup() throws Exception {
        userManagementClient.deleteRole(ROLE_NAME);
        String userMgtPath = CarbonUtils.getCarbonHome() + File.separator + "repository" +
                             File.separator + "conf" + File.separator + "user-mgt.xml";

        //restore backed up user-mgt file
        File oldFile = new File(userMgtPath + ".bak");
        oldFile.renameTo(new File(userMgtPath));
        restartServer();
    }
}
