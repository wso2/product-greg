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
import org.wso2.carbon.automation.engine.FrameworkConstants;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.test.utils.common.FileManager;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.list.stub.beans.xsd.ServiceBean;
import org.wso2.carbon.integration.common.admin.client.UserManagementClient;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.lifecycle.test.utils.LifeCycleUtils;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.clients.GenericServiceClient;
import org.wso2.greg.integration.common.clients.ListMetaDataServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import java.io.File;

import static org.testng.Assert.assertTrue;

public class Carbon10667TestCase extends GREGIntegrationBaseTest {

    private WSRegistryServiceClient registry;
    private UserManagementClient userManagementClient;
    private Registry governance;
    RegistryProviderUtil registryProviderUtil;
    private final String serviceName = "PermissionTestService";
    private String session;
    private String userNameWithoutDomain;
    private String userName = "deniedUser";


    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        session = getSessionCookie();
        registryProviderUtil = new RegistryProviderUtil();
        registry = registryProviderUtil.getWSRegistry(automationContext);
        governance = registryProviderUtil.getGovernanceRegistry(registry, automationContext);

    }

    @Test(groups = "wso2.greg", description = "Add Service 'PermissionTestService' as admin ")
    public void testAddService() throws Exception {
        LifeCycleUtils.addService("sns", serviceName, governance);

    }

    @Test(groups = "wso2.greg", description = "Login as only browse permission allowed user ",
            dependsOnMethods = "testAddService")
    public void testViewService() throws Exception {
        userManagementClient = new UserManagementClient(backendURL, session);

        userManagementClient.updateUserListOfRole(FrameworkConstants.ADMIN_ROLE, null,
                new String [] {userName});

        userManagementClient.addRole("testRoleBrowse", new String[]{userName},
                new String[]{"/permission/admin/login",
                        "/permission/admin/manage/resources/browse",
                        "/permission/admin/manage/resources/govern/generic/list",
                        "/permission/admin/manage/resources/govern/service/list",
                        "/permission/admin/manage/resources/govern/wsdl/list",
                        "/permission/admin/manage/resources/govern/schema/list",
                        "/permission/admin/manage/resources/govern/policy/list"});

        registry = registryProviderUtil.getWSRegistry(automationContext);
        governance = registryProviderUtil.getGovernanceRegistry(registry, automationContext);


        ServiceManager serviceManager = new ServiceManager(governance);
        Service[] services = serviceManager.getAllServices();

        boolean serviceAccessible = false;
        for(Service service: services){
            if(service.getQName().getLocalPart().contains(serviceName)){
                serviceAccessible = true;
                break;
            }
        }

        assertTrue(serviceAccessible, "Service is not accessible from the user");

    }


    @Test(groups = "wso2.greg", description = "Add service by permission denied user ",
            dependsOnMethods = "testViewService", expectedExceptions = AxisFault.class)
    public void testAddServiceByDeniedUser() throws Exception {
        AutomationContext automationContextUser = new AutomationContext("GREG", "greg001",
                FrameworkConstants.SUPER_TENANT_KEY, userName);

        GenericServiceClient genericServiceClient = new GenericServiceClient(backendURL,
                new LoginLogoutClient(automationContextUser).login());

        String servicePath = getTestArtifactLocation() + "artifacts" +
                File.separator + "GREG" + File.separator + "services" + File.separator +
                "intergalacticService.metadata.xml";

        String serviceContent = FileManager.readFile(servicePath);
        OMElement service = AXIOMUtil.stringToOM(serviceContent);
        genericServiceClient.addArtifact(service, "service", null);

    }


    @AfterClass(groups = "wso2.greg", description = "Remove created service/role by admin", alwaysRun = true)
    public void removeService() throws Exception {
        try {
            registry = registryProviderUtil.getWSRegistry(automationContext);
            governance = registryProviderUtil.getGovernanceRegistry(registry, automationContext);
            ServiceManager serviceManager = new ServiceManager(governance);
            Service[] services = serviceManager.getAllServices();
            for(Service s : services) {
                if(s.getQName().getLocalPart().equals(serviceName)) {
                    serviceManager.removeService(s.getId());
                }
            }
        } finally {
            userManagementClient.deleteRole("testRoleBrowse");
            userManagementClient = null;
            registry = null;
            registryProviderUtil = null;
            governance = null;
        }
    }
}
