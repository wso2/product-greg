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
import org.wso2.carbon.automation.api.clients.server.admin.ServerAdminClient;
import org.wso2.carbon.automation.api.clients.user.mgt.UserManagementClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.ServerGroupManager;
import org.wso2.carbon.automation.core.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.core.annotations.SetEnvironment;
import org.wso2.carbon.automation.core.utils.ClientConnectionUtil;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkFactory;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkProperties;
import org.wso2.carbon.utils.CarbonUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;

public class Carbon9161 {

    private static final String ROLE_NAME = "carbon9161role";

    private UserManagementClient userManagementClient;
    private ServerAdminClient serverAdminClient;
    private ManageEnvironment environment;


    private void setUserStoreReadOnly(boolean state) throws IOException, XMLStreamException,
                                                            ParserConfigurationException,
                                                            SAXException,
                                                            TransformerException {
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

    private void restartServer() throws Exception,
                                        RemoteException, InterruptedException {
        FrameworkProperties frameworkProperties =
                FrameworkFactory.getFrameworkProperties(ProductConstant.GREG_SERVER_NAME);
        ServerGroupManager.getServerUtils().restartGracefully(serverAdminClient, frameworkProperties);
    }

    @BeforeClass(alwaysRun = true)
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_user})
    public void copyMappingFileAndRestart()
            throws Exception {

        int userId = ProductConstant.ADMIN_USER_ID;
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        environment = builder.build();
        userManagementClient = new UserManagementClient(environment.getGreg().getBackEndUrl(),
                                                        environment.getGreg().getSessionCookie());
        userManagementClient.addRole(ROLE_NAME, new String[]{}, new String[]{});
        serverAdminClient = new ServerAdminClient(environment.getGreg().getBackEndUrl(),
                                                  environment.getGreg().getSessionCookie());

        setUserStoreReadOnly(true);
        restartServer();
        environment = new EnvironmentBuilder().greg(0).build();
        userManagementClient = new UserManagementClient(environment.getGreg().getBackEndUrl(),
                                                        environment.getGreg().getSessionCookie());
        serverAdminClient = new ServerAdminClient(environment.getGreg().getBackEndUrl(),
                                                  environment.getGreg().getSessionCookie());
    }

    @Test(expectedExceptions = AxisFault.class)
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_user})
    public void testReadWriteRoles() throws Exception {
        //as the datastore is made read only this was added to internal userstore. So the operation was allowed.
        //with the fix this operation should fail
        userManagementClient.addRole(ROLE_NAME, new String[]{}, new String[]{});
    }

    @AfterClass(alwaysRun = true)
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_user,
                                             ExecutionEnvironment.integration_tenant})
    public void cleanup() throws Exception {
        userManagementClient.deleteRole(ROLE_NAME);
        String userMgtPath = CarbonUtils.getCarbonHome() + File.separator + "repository" +
                             File.separator + "conf" + File.separator + "user-mgt.xml";

        //restore backed up user-mgt file
        File oldFile = new File(userMgtPath + ".bak");
        oldFile.renameTo(new File(userMgtPath));
        restartServer();
        userManagementClient = null;
        environment = null;
        serverAdminClient = null;
    }
}
