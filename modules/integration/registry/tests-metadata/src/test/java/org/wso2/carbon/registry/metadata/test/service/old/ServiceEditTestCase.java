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

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;

import static org.testng.Assert.assertNotEquals;

/**
 * A test case which tests registry service edit operation
 */
public class ServiceEditTestCase {
    private static final Log log = LogFactory.getLog(ServiceEditTestCase.class);
    public static final String TRUNK = "/_system/governance/trunk";
    private String servicePath = TRUNK + "/services/";
    private String wsdlPath = TRUNK + "/wsdls/";
    private String schemaPath = TRUNK + "/schemas/";
    private GovernanceServiceClient addServicesServiceClient;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private ManageEnvironment environment;
    private UserInfo userInfo;
    private String resourceName = "SimpleStockQuote.xml";
    private String resourceNameUpdated = "SimpleStockQuote-updated.xml";
    private String serviceName = "SimpleStockQuoteService";
    private String wsdlName = serviceName + ".wsdl";
    private String wsdlNamespacePath = "samples/services/";
    private WsdlManager wsdlManager;

    @BeforeClass(groups = {"wso2.greg"})
    public void init() throws Exception {
        log.info("Initializing Edit Service Resource Tests");
        log.debug("Add Service Resource Initialised");
        int userId = ProductConstant.ADMIN_USER_ID;
        userInfo = UserListCsvReader.getUserInfo(userId);
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        environment = builder.build();
        addServicesServiceClient =
                new GovernanceServiceClient(environment.getGreg().getBackEndUrl(),
                                            environment.getGreg().getSessionCookie());
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                               environment.getGreg().getSessionCookie());
        WSRegistryServiceClient wsRegistry =
                new RegistryProviderUtil().getWSRegistry(userId,
                        ProductConstant.GREG_SERVER_NAME);
        Registry governance = new RegistryProviderUtil().getGovernanceRegistry(wsRegistry, userId);
        wsdlManager = new WsdlManager(governance);

    }

    @Test(groups = {"wso2.greg"})
    public void editService() throws Exception, IOException, AddServicesServiceRegistryExceptionException, ResourceAdminServiceExceptionException {
        log.debug("Running SuccessCase");
        String resource = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator +
                "GREG" + File.separator +"xml" + File.separator + resourceName;
        addServicesServiceClient.addService(AXIOMUtil.stringToOM(ServiceAddTestCase.fileReader(resource)));
        String textContent = resourceAdminServiceClient.getTextContent(servicePath +
                wsdlNamespacePath + serviceName);
        assertNotEquals(textContent.indexOf("http://services.samples"), -1);
        String resourceUpdated = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator +
                "GREG" + File.separator + "xml" + File.separator + resourceNameUpdated;
        addServicesServiceClient.addService(AXIOMUtil.stringToOM(ServiceAddTestCase.fileReader(resourceUpdated)));
        String textContentUpdated = resourceAdminServiceClient.getTextContent(servicePath +
                wsdlNamespacePath + serviceName);
        assertNotEquals(textContentUpdated.indexOf("SimpleStockQuoteService Description Updated"), -1);


    }

    @AfterClass(groups = {"wso2.greg"})
    public void deleteResources() throws ResourceAdminServiceExceptionException, RemoteException, GovernanceException {

        Endpoint[] endpoints = null;
        Wsdl[] wsdls = wsdlManager.getAllWsdls();
        for (Wsdl wsdl : wsdls) {
            if (wsdl.getQName().getLocalPart().equals(wsdlName)) {
                endpoints = wsdlManager.getWsdl(wsdl.getId()).getAttachedEndpoints();
            }
        }
        resourceAdminServiceClient.deleteResource("/_system/governance/trunk/wsdls/samples/services/SimpleStockQuoteService.wsdl");
        for (Endpoint path : endpoints) {
            resourceAdminServiceClient.deleteResource("_system/governance/" + path.getPath());
        }


        resourceAdminServiceClient.deleteResource(servicePath +
                wsdlNamespacePath + serviceName);
        servicePath = null;
        wsdlPath = null;
        schemaPath = null;
        addServicesServiceClient = null;
        resourceAdminServiceClient = null;
        environment = null;
        userInfo = null;
        resourceName = null;
        resourceNameUpdated = null;
        serviceName = null;
        wsdlName = null;
        wsdlNamespacePath = null;
    }


}



