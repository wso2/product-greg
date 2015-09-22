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

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.beans.xsd.LifecycleBean;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.services.ArrayOfString;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.registry.activities.stub.RegistryExceptionException;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.lifecycle.test.utils.LifeCycleUtils;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.search.stub.SearchAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.clients.LifeCycleAdminServiceClient;
import org.wso2.greg.integration.common.clients.LifeCycleManagementClient;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import java.io.IOException;
import java.rmi.RemoteException;

import static org.testng.Assert.assertEquals;

public class DemoteLCTestTransition extends GREGIntegrationBaseTest{

    private int userId = 2;
    private WSRegistryServiceClient registry;
    private LifeCycleManagementClient lifeCycleManagementClient;
    private LifeCycleAdminServiceClient lifeCycleAdminServiceClient;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private Registry governance;
    private final String serviceName = "DiffEnvironmentService";
    private final String ASPECT_NAME = "DiffEnvironmentLC";
    private final String ACTION_PROMOTE = "Promote";
    private final String ACTION_DEMOTE = "Demote";
    private static final String GOV_PATH = "/_system/governance";
    private String trunk;
    private String trunkTesting;
    private String trunkProduction;
    private String trunkProductionDemote;
    private String[] dependencyList;

    @BeforeClass (alwaysRun = true)
    public void init () throws Exception {
        super.init(TestUserMode.SUPER_TENANT_USER);
        String sessionCookie = new LoginLogoutClient(automationContext).login();

        lifeCycleAdminServiceClient =
                new LifeCycleAdminServiceClient(backendURL,sessionCookie);
        lifeCycleManagementClient =
                new LifeCycleManagementClient(backendURL,sessionCookie);
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(backendURL,sessionCookie);
        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();

        registry = registryProviderUtil.getWSRegistry(automationContext);
        governance = registryProviderUtil.getGovernanceRegistry(registry, automationContext);

    }

    @Test (groups = "wso2.greg", description = "Add Service 'MultiplePromoteDemoteService' ")
    public void testAddService () throws Exception {

        LifeCycleUtils.deleteLifeCycleIfExist(ASPECT_NAME, lifeCycleManagementClient);
        trunk = "/_system/governance" + LifeCycleUtils.addService("sns", serviceName, governance);

    }

    @Test (groups = "wso2.greg", description = "Add new Life Cycle 'DiffEnvironmentLC' ",
            dependsOnMethods = "testAddService")
    public void createNewLifeCycle ()
            throws IOException, LifeCycleManagementServiceExceptionException, InterruptedException,
            SearchAdminServiceRegistryExceptionException {

        LifeCycleUtils.createLifeCycleMultiplePromoteDemote(ASPECT_NAME, lifeCycleManagementClient);

    }

    @Test (groups = "wso2.greg", description = "Add LifeCycle to a service", dependsOnMethods = "createNewLifeCycle")
    public void addLifeCycleToService ()
            throws RegistryException, InterruptedException,
            CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException,
            RegistryExceptionException {

        registry.associateAspect(trunk, ASPECT_NAME);
        LifecycleBean lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(trunk);
        Resource service = registry.get(trunk);
        Assert.assertNotNull(service, "Service Not found on registry path " + trunk);
        assertEquals(service.getPath(), trunk, "Service path changed after adding life cycle. " + trunk);
        assertEquals(getLifeCycleState(lifeCycle), "Development",
                "LifeCycle State Mismatched");

    }

    @Test (groups = "wso2.greg", description = "Promote Service to Testing",
            dependsOnMethods = "addLifeCycleToService")
    public void promoteServiceToTesting ()
            throws CustomLifecyclesChecklistAdminServiceExceptionException,
            RemoteException, RegistryException {

        ArrayOfString[] parameters = new ArrayOfString[2];
        dependencyList =
                lifeCycleAdminServiceClient.getAllDependencies(trunk);
        parameters[0] = new ArrayOfString();
        parameters[0].setArray(new String[]{dependencyList[0], "1.1.1"});
        parameters[1] = new ArrayOfString();
        parameters[1].setArray(new String[]{"preserveOriginal", "false"});
        lifeCycleAdminServiceClient.invokeAspectWithParams(trunk,
                ASPECT_NAME, ACTION_PROMOTE, null,
                parameters);
        trunkTesting = "/_system/governance/branches/testing/services/sns/1.1.1/" + serviceName;
        LifecycleBean lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(trunkTesting);
        Resource service = registry.get(trunkTesting);
        Assert.assertNotNull(service, "Service Not found on registry path " + trunkTesting);
        Assert.assertEquals(service.getPath(), trunkTesting, "Service not in branches/testing. " + trunkTesting);
        Assert.assertEquals(getLifeCycleState(lifeCycle), "Testing", "LifeCycle State Mismatched");
        try {
            registry.get(trunk);
            Assert.fail(trunk + " Resource exist");
        } catch (RegistryException e) {
            Assert.assertTrue(e.getCause().getMessage().contains("Resource does not exist at path /_system/governance/trunk/services/"));
        }

    }

    @Test (groups = "wso2.greg", description = "Promote Service to Production",
            dependsOnMethods = "promoteServiceToTesting")
    public void promoteServiceToProduction ()
            throws CustomLifecyclesChecklistAdminServiceExceptionException,
            RemoteException, RegistryException {

        ArrayOfString[] parameters = new ArrayOfString[2];
        dependencyList =
                lifeCycleAdminServiceClient.getAllDependencies(trunkTesting);
        parameters[0] = new ArrayOfString();
        parameters[0].setArray(new String[]{dependencyList[0], "2.2.2"});
        parameters[1] = new ArrayOfString();
        parameters[1].setArray(new String[]{"preserveOriginal", "true"});
        lifeCycleAdminServiceClient.invokeAspectWithParams(trunkTesting,
                ASPECT_NAME, ACTION_PROMOTE, null,
                parameters);
        trunkProduction = "/_system/governance/branches/production/services/sns/2.2.2/" + serviceName;
        LifecycleBean lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(trunkProduction);
        Resource service = registry.get(trunkProduction);
        Assert.assertNotNull(service, "Service Not found on registry path " + trunkProduction);
        Assert.assertEquals(service.getPath(), trunkProduction, "Service not in branches/testing. " + trunkProduction);
        Assert.assertEquals(getLifeCycleState(lifeCycle), "Production", "LifeCycle State Mismatched");
        Assert.assertEquals(registry.get(trunkTesting).getPath(), trunkTesting, "Resource not exist on trunk");
        if (trunkTesting != null) {
            registry.delete(trunkTesting);

        }
    }

    @Test (groups = "wso2.greg", description = "Demote Service to Testing",
            dependsOnMethods = "promoteServiceToProduction")
    public void demoteServiceToTesting ()
            throws CustomLifecyclesChecklistAdminServiceExceptionException,
            RemoteException, RegistryException {

        ArrayOfString[] parameters = new ArrayOfString[2];
        dependencyList =
                lifeCycleAdminServiceClient.getAllDependencies(trunkProduction);
        parameters[0] = new ArrayOfString();
        parameters[0].setArray(new String[]{dependencyList[0], "3.3.3"});
        parameters[1] = new ArrayOfString();
        parameters[1].setArray(new String[]{"preserveOriginal", "true"});
        lifeCycleAdminServiceClient.invokeAspectWithParams(trunkProduction,
                ASPECT_NAME, ACTION_DEMOTE, null,
                parameters);
        trunkProductionDemote = "/_system/governance/branches/testing/services/sns/3.3.3/" + serviceName;
        LifecycleBean lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(trunkProductionDemote);
        Resource service = registry.get(trunkProductionDemote);
        Assert.assertNotNull(service, "Service Not found on registry path " + trunkProductionDemote);
        Assert.assertEquals(service.getPath(), trunkProductionDemote, "Service not in branches/testing. " + trunkProductionDemote);
        Assert.assertEquals(getLifeCycleState(lifeCycle), "Testing", "LifeCycle State Mismatched");
        Assert.assertEquals(registry.get(trunkProduction).getPath(), trunkProduction, "Resource not exist on trunk");
        if (trunkProduction != null) {
            registry.delete(trunkProduction);

        }

    }

    @Test (groups = "wso2.greg", description = "Demote Service To Development",
            dependsOnMethods = "demoteServiceToTesting")
    public void demoteServiceToDevelopment ()
            throws CustomLifecyclesChecklistAdminServiceExceptionException,
            RemoteException, RegistryException,
            LifeCycleManagementServiceExceptionException {

        ArrayOfString[] parameters = new ArrayOfString[2];
        dependencyList =
                lifeCycleAdminServiceClient.getAllDependencies(trunkProductionDemote);
        parameters[0] = new ArrayOfString();
        parameters[0].setArray(new String[]{dependencyList[0], "4.4.4"});
        parameters[1] = new ArrayOfString();
        parameters[1].setArray(new String[]{"preserveOriginal", "false"});
        lifeCycleAdminServiceClient.invokeAspectWithParams(trunkProductionDemote,
                ASPECT_NAME, ACTION_DEMOTE, null,
                parameters);
        String trunkTestingDemote = "/_system/governance/trunk/services/sns/4.4.4/" + serviceName;
        LifecycleBean lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(trunkTestingDemote);
        Resource service = registry.get(trunkTestingDemote);
        Assert.assertNotNull(service, "Service Not found on registry path " + trunkTestingDemote);
        Assert.assertEquals(service.getPath(), trunkTestingDemote, "Service not in trunk/services. " +
                trunkTestingDemote);
        Assert.assertEquals(getLifeCycleState(lifeCycle), "Development", "LifeCycle State Mismatched");
        try {
            registry.get(trunkProductionDemote);
            Assert.fail(trunkProductionDemote + " Resource exist");
        } catch (RegistryException e) {
            Assert.assertTrue(e.getCause().getMessage().contains("Resource does not exist at path /_system/governance/branches/testing/services/"));
        }

    }

    @AfterClass
    public void DeleteLCResources ()
            throws LifeCycleManagementServiceExceptionException, RemoteException,
            RegistryException, ResourceAdminServiceExceptionException {
        //GovernanceUtils.loadGovernanceArtifacts((UserRegistry)governance);
        ServiceManager serviceManager = new ServiceManager(governance);
        Service[] services = serviceManager.getAllServices();
        for (Service s : services) {
            if (s.getQName().getLocalPart().equals(serviceName)) {
                String path = s.getPath();
                resourceAdminServiceClient.deleteResource(GOV_PATH + path);
            }
        }
        LifeCycleUtils.deleteLifeCycleIfExist(ASPECT_NAME, lifeCycleManagementClient);
        registry = null;
        lifeCycleAdminServiceClient = null;
        lifeCycleManagementClient = null;
    }

    public static String getLifeCycleState (LifecycleBean lifeCycle) {

        Assert.assertTrue((lifeCycle.getLifecycleProperties().length > 0), "LifeCycle properties missing some properties");
        String state = null;
        boolean stateFound = false;
        for (Property prop : lifeCycle.getLifecycleProperties()) {
            if ("registry.lifecycle.DiffEnvironmentLC.state".equalsIgnoreCase(prop.getKey())) {
                stateFound = true;
                Assert.assertNotNull(prop.getValues(), "State Value Not Found");
                state = prop.getValues()[0];

            }
        }
        Assert.assertTrue(stateFound, "LifeCycle State property not found");
        return state;
    }

}
