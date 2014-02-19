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

package org.wso2.carbon.registry.lifecycle.test;


import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.governance.LifeCycleAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.services.ArrayOfString;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.lifecycle.test.utils.LifeCycleUtils;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import java.rmi.RemoteException;

public class InvalidVersionNoTest {
    private int userId = 2;
    UserInfo userInfo = UserListCsvReader.getUserInfo(userId);

    private WSRegistryServiceClient registry;
    private LifeCycleAdminServiceClient lifeCycleAdminServiceClient;
    private Registry governance;
    private String serviceName = "serviceName";
    private final String aspectName = "ServiceLifeCycle";
    private String trunk;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        ManageEnvironment environment = builder.build();
        lifeCycleAdminServiceClient =
                new LifeCycleAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                                                      environment.getGreg().getSessionCookie());
        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        registry = registryProviderUtil.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        governance = registryProviderUtil.getGovernanceRegistry(registry, userId);


        trunk = "/_system/governance" + LifeCycleUtils.addService("ns", serviceName, governance);


        registry.associateAspect(trunk, aspectName);

    }

    @Test(groups = "wso2.greg", description = " Promote LC with invalid versions for Dependencies", enabled = false)
    public void testInvalidDependencyVersions()
            throws CustomLifecyclesChecklistAdminServiceExceptionException,
                   RemoteException, GovernanceException {

        ArrayOfString[] parameters = new ArrayOfString[1];
        String[] dependencyList = lifeCycleAdminServiceClient.getAllDependencies(trunk);

        parameters[0] = new ArrayOfString();
        parameters[0].setArray(new String[]{dependencyList[0], "2.0"});

        String ACTION_PROMOTE = "Promote";
        lifeCycleAdminServiceClient.invokeAspectWithParams(trunk,
                                                           aspectName, ACTION_PROMOTE, null,
                                                           parameters);

    }

    @AfterClass
    public void deleteServices()
            throws RegistryException, LifeCycleManagementServiceExceptionException,
                   RemoteException {
        if (trunk != null) {
            registry.delete(trunk);
        }

        ServiceManager serviceManager = new ServiceManager(governance);
        Service[] services = serviceManager.getAllServices();
        for (Service s : services) {
            if (s.getQName().getLocalPart().equals(serviceName)) {
                serviceManager.removeService(s.getId());
            }
        }

        governance = null;
        registry = null;
        lifeCycleAdminServiceClient = null;
    }

}
