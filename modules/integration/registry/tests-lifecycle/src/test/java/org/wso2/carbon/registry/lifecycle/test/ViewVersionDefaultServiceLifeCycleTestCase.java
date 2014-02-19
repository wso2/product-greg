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
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.services.ArrayOfString;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.lifecycle.test.utils.LifeCycleUtils;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;

public class ViewVersionDefaultServiceLifeCycleTestCase {
    private int userId = 2;
    UserInfo userInfo = UserListCsvReader.getUserInfo(userId);

    private WSRegistryServiceClient registry;
    private LifeCycleAdminServiceClient lifeCycleAdminServiceClient;
    private Registry governance;

    private final String aspectName = "ServiceLifeCycle";
    private String trunkPreserve;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        ManageEnvironment environment = builder.build();
        lifeCycleAdminServiceClient =
                new LifeCycleAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                                environment.getGreg().getSessionCookie());
        lifeCycleAdminServiceClient =
                new LifeCycleAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                                environment.getGreg().getSessionCookie());

        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        registry = registryProviderUtil.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        governance = registryProviderUtil.getGovernanceRegistry(registry, userId);


        String serviceViewVersionTrue = "serviceViewVersionTrue";
        trunkPreserve = "/_system/governance" + LifeCycleUtils.addService("sns", serviceViewVersionTrue, governance);


        registry.associateAspect(trunkPreserve, aspectName);

    }

    @Test(groups = "wso2.greg", description = " Promote LC with new versions for Dependencies")
    public void testDependencyVersions()
            throws CustomLifecyclesChecklistAdminServiceExceptionException,
                   RemoteException, GovernanceException {

        ArrayOfString[] parameters = new ArrayOfString[1];
        String[] dependencyList = lifeCycleAdminServiceClient.getAllDependencies(trunkPreserve);

        parameters[0] = new ArrayOfString();
        parameters[0].setArray(new String[]{dependencyList[0], "2.0.0"});

        String ACTION_PROMOTE = "Promote";
        lifeCycleAdminServiceClient.invokeAspectWithParams(trunkPreserve,
                                                           aspectName, ACTION_PROMOTE, null,
                                                           parameters);

    }

    @Test(groups = "wso2.greg", description = "Test Service Version After Promoting",
          dependsOnMethods = "testDependencyVersions")
    public void testServiceVersion() throws RegistryException {
        ServiceManager serviceManager = new ServiceManager(governance);
        Service[] service;
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry)governance);
        service = serviceManager.getAllServices();

        Boolean versionIsSet = false;
        for (Service s : service) {
            if (s.getAttribute("overview_version").equalsIgnoreCase("2.0.0")) {
                versionIsSet = true;

            }

        }
        assertTrue(versionIsSet, "versionIsSet is not set");
    }


    @Test(groups = "wso2.greg", description = "Test Service Changes",
          dependsOnMethods = "testServiceVersion")
    public void testServiceChanged() throws RegistryException {
        ServiceManager serviceManager = new ServiceManager(governance);
        Service[] service;
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry)governance);
        service = serviceManager.getAllServices();
        for (Service s : service) {
            if (s.getAttribute("overview_version").equalsIgnoreCase("2.0.0")) {
                s.setAttribute("overview_version", "3.0.0");
                s.addAttribute("overview_description", "This is a Description");
                serviceManager.updateService(s);
            }

        }


    }


    @Test(groups = "wso2.greg", description = "Test if service changes saved",
          dependsOnMethods = "testServiceChanged")
    public void testServiceChangeTest() throws RegistryException {

        ServiceManager serviceManager = new ServiceManager(governance);
        Service[] service;
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry)governance);
        service = serviceManager.getAllServices();
        Boolean serviceChanged = false;
        for (Service s : service) {
            if (s.getAttribute("overview_version").equalsIgnoreCase("3.0.0")) {
                if (s.getAttribute("overview_description").equals("This is a Description")) {
                    serviceChanged = true;
                }


            }

        }
        assertTrue(serviceChanged, "Service is is not changed");
    }

    @AfterClass
    public void deleteServices()
            throws RegistryException, LifeCycleManagementServiceExceptionException,
                   RemoteException {
        if (trunkPreserve != null) {
            registry.delete(trunkPreserve);
        }
        registry.delete("/_system/governance/branches/testing/services/sns/2.0.0/serviceViewVersionTrue");
        registry = null;
        governance = null;
        lifeCycleAdminServiceClient = null;
    }
}
