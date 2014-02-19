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

package org.wso2.carbon.registry.metadata.test.service.old;

import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.governance.GovernanceServiceClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.api.endpoints.dataobjects.Endpoint;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.governance.services.stub.AddServicesServiceRegistryExceptionException;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import javax.xml.stream.XMLStreamException;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.rmi.RemoteException;

import static org.testng.Assert.assertNotEquals;

/**
 * A test case which tests registry service add operation
 */
public class ServiceAddTestCase {

    private static final Log log = LogFactory.getLog(ServiceAddTestCase.class);
    public static final String TRUNK = "/_system/governance/trunk";
    private String servicePath = TRUNK + "/services/";
    private String wsdlPath = TRUNK + "/wsdls/";
    private String schemaPath = TRUNK + "/schemas/";
    private GovernanceServiceClient governanceServiceClient;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private ManageEnvironment environment;
    private UserInfo userInfo;
    private String resourceName = "info.xml";
    private String serviceName = "NewBizService";
    private String wsdlName = serviceName + ".wsdl";
    private String wsdlNamespacePath = "com/foo/";
    private String schemaNamespacePath = "org/bar/purchasing/";
    private String schemaName = "purchasing.xsd";
    private WsdlManager wsdlManager;

    @BeforeClass(groups = {"wso2.greg"})
    public void init() throws Exception {
        log.info("Initializing Add Service Resource Tests");
        log.debug("Add Service Resource Initialised");
        int userId = ProductConstant.ADMIN_USER_ID;
        userInfo = UserListCsvReader.getUserInfo(userId);
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        environment = builder.build();
        WSRegistryServiceClient wsRegistry =
                new RegistryProviderUtil().getWSRegistry(userId,
                                                         ProductConstant.GREG_SERVER_NAME);
        Registry governance = new RegistryProviderUtil().getGovernanceRegistry(wsRegistry, userId);

        wsdlManager = new WsdlManager(governance);
    }

    @Test(groups = {"wso2.greg"})
    public void addService()
            throws Exception, XMLStreamException, AddServicesServiceRegistryExceptionException,
                   ResourceAdminServiceExceptionException {
        log.debug("Running SuccessCase");
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                               environment.getGreg().getSessionCookie());
        governanceServiceClient =
                new GovernanceServiceClient(environment.getGreg().getBackEndUrl(),
                                            environment.getGreg().getSessionCookie());
        String resource = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator +
                          "GREG" + File.separator +
                          "xml" + File.separator + resourceName;
        governanceServiceClient.addService(AXIOMUtil.stringToOM(fileReader(resource)));

        String textContent =
                resourceAdminServiceClient.getTextContent(servicePath +
                                                          wsdlNamespacePath + serviceName);
        assertNotEquals(textContent.indexOf("http://foo.com"), -1);
        String textContentWsdl =
                resourceAdminServiceClient.getTextContent(wsdlPath +
                                                          wsdlNamespacePath + wsdlName);
        assertNotEquals(textContentWsdl.indexOf("../../../schemas/org/bar/purchasing/purchasing.xsd"), -1);
        String textContentSchema =
                resourceAdminServiceClient.getTextContent(schemaPath +
                                                          schemaNamespacePath + schemaName);
        assertNotEquals(textContentSchema.indexOf("http://bar.org/purchasing"), -1);


    }

    public static String fileReader(String fileName) {
        String fileContent = "";
        try {
            // Open the file that is the first
            // command line parameter
            FileInputStream fstream = new
                    FileInputStream(fileName);

            // Convert our input stream to a
            // DataInputStream
            DataInputStream in =
                    new DataInputStream(fstream);

            // Continue to read lines while
            // there are still some left to read

            while (in.available() != 0) {
                fileContent = fileContent + (in.readLine());
            }

            in.close();
        } catch (Exception e) {
            System.err.println("File input error");
        }
        return fileContent;

    }

    @AfterClass(groups = {"wso2.greg"})
    public void deleteResources()
            throws ResourceAdminServiceExceptionException, RemoteException, GovernanceException {

        Endpoint[] endpoints = null;
        Wsdl[] wsdls = wsdlManager.getAllWsdls();
        for (Wsdl wsdl : wsdls) {
            if (wsdl.getQName().getLocalPart().equals(wsdlName)) {
                endpoints = wsdlManager.getWsdl(wsdl.getId()).getAttachedEndpoints();
            }
        }
        resourceAdminServiceClient.deleteResource("/_system/governance/trunk/wsdls/com/foo/" + wsdlName);
        for (Endpoint path : endpoints) {
            resourceAdminServiceClient.deleteResource("_system/governance/" + path.getPath());
        }
        resourceAdminServiceClient.deleteResource(servicePath +
                                                  wsdlNamespacePath + serviceName);
        resourceAdminServiceClient.deleteResource("/_system/governance/trunk/schemas/org/bar/purchasing/purchasing.xsd");
        servicePath = null;
        wsdlPath = null;
        schemaPath = null;
        governanceServiceClient = null;
        resourceAdminServiceClient = null;
        environment = null;
        userInfo = null;
        resourceName = null;
        serviceName = null;
        wsdlName = null;
        wsdlNamespacePath = null;
        schemaNamespacePath = null;
        schemaName = null;


    }
}
