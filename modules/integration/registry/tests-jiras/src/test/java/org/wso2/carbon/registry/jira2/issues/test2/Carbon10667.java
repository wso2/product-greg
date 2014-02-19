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


import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.governance.GenericServiceClient;
import org.wso2.carbon.automation.api.clients.governance.ListMetaDataServiceClient;
import org.wso2.carbon.automation.api.clients.user.mgt.UserManagementClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.core.utils.fileutils.FileManager;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.list.stub.beans.xsd.ServiceBean;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.lifecycle.test.utils.LifeCycleUtils;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import java.io.File;

import static org.testng.Assert.assertTrue;

public class Carbon10667 {

    private int adminId = ProductConstant.ADMIN_USER_ID;
    private int userId = 2;
    UserInfo userInfo = UserListCsvReader.getUserInfo(userId);
    private WSRegistryServiceClient registry;
    private UserManagementClient userManagementClient;
    ManageEnvironment environment;
    private Registry governance;
    RegistryProviderUtil registryProviderUtil;
    private final String serviceName = "PermissionTestService";



    @BeforeClass
    public void init() throws Exception {

        EnvironmentBuilder builder = new EnvironmentBuilder().greg(adminId);
        environment = builder.build();
        registryProviderUtil = new RegistryProviderUtil();
        registry = registryProviderUtil.getWSRegistry(adminId, ProductConstant.GREG_SERVER_NAME);
        governance = registryProviderUtil.getGovernanceRegistry(registry, adminId);

    }

    @Test(groups = "wso2.greg", description = "Add Service 'PermissionTestService' as admin ")
    public void testAddService() throws Exception {

        LifeCycleUtils.addService("sns", serviceName, governance);

    }

    @Test(groups = "wso2.greg", description = "Login as only browse permission allowed user ",
          dependsOnMethods = "testAddService")
    public void testViewService() throws Exception {

        userManagementClient = new UserManagementClient(environment.getGreg().getBackEndUrl(),
                                                        environment.getGreg().getSessionCookie());
        userManagementClient.updateUserListOfRole("testRole", new String[]{},
                                                  new String[]{userInfo.getUserNameWithoutDomain()});
        userManagementClient.addRole("testRoleBrowse",
                                     new String[]{userInfo.getUserNameWithoutDomain()},
                                     new String[]{"/permission/admin/login",
                                                  "/permission/admin/manage/resources/browse",
                                                  "/permission/admin/manage/resources/govern/generic/list",
                                                  "/permission/admin/manage/resources/govern/service/list",
                                                  "/permission/admin/manage/resources/govern/wsdl/list",
                                                  "/permission/admin/manage/resources/govern/schema/list",
                                                  "/permission/admin/manage/resources/govern/policy/list"});

        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        environment = builder.build();

        registry = registryProviderUtil.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        governance = registryProviderUtil.getGovernanceRegistry(registry, userId);
        ListMetaDataServiceClient listMetaDataServiceClient =
                new ListMetaDataServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                              environment.getGreg().getSessionCookie());
        ServiceBean serviceBean = listMetaDataServiceClient.listServices(null);
        String[] serviceNames = serviceBean.getNames();

        boolean serviceAccessible = false;

        for (String s : serviceNames) {
            if (s.equals(serviceName)) {
                serviceAccessible = true;
                break;
            }
        }
        assertTrue(serviceAccessible, "Service is not accessible from the user");
    }


    @Test(groups = "wso2.greg", description = "Add service by permission denied user ",
          dependsOnMethods = "testViewService", expectedExceptions = AxisFault.class)
    public void addService() throws Exception {

        GenericServiceClient genericServiceClient =
                new GenericServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                            environment.getGreg().getSessionCookie());

        String servicePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
                             File.separator + "GREG" + File.separator + "services" + File.separator +
                             "intergalacticService.metadata.xml";
        String serviceContent = FileManager.readFile(servicePath);
        OMElement service = AXIOMUtil.stringToOM(serviceContent);
        genericServiceClient.addArtifact(service, "service", null);

    }

    @AfterClass(groups = "wso2.greg", description = "Remove created service/role by admin", alwaysRun = true)
    public void removeService() throws Exception {
        try{

        EnvironmentBuilder builder = new EnvironmentBuilder().greg(adminId);
        environment = builder.build();
        registry = registryProviderUtil.getWSRegistry(adminId, ProductConstant.GREG_SERVER_NAME);
        governance = registryProviderUtil.getGovernanceRegistry(registry, adminId);

        ServiceManager serviceManager = new ServiceManager(governance);
        Service[] services = serviceManager.getAllServices();

        for (Service s : services) {
            if (s.getQName().getLocalPart().equals(serviceName)) {
                serviceManager.removeService(s.getId());
            }
        }
        }finally {

            userManagementClient.updateUserListOfRole("testRole", new String[]{userInfo.getUserNameWithoutDomain()},
                    new String[]{});
            userManagementClient.deleteRole("testRoleBrowse");
            userManagementClient = null;
            registry = null;
            environment = null;
            registryProviderUtil = null;
            governance = null;
        }
    }
}
