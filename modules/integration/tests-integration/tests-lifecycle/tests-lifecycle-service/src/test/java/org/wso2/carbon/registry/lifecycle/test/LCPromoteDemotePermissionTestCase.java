/*
 * Copyright 2004,2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.registry.lifecycle.test;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.engine.FrameworkConstants;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.test.utils.common.FileManager;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.beans.xsd.LifecycleBean;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.governance.list.stub.ListMetadataServiceRegistryExceptionException;
import org.wso2.carbon.governance.list.stub.beans.xsd.ServiceBean;
import org.wso2.carbon.governance.services.stub.AddServicesServiceRegistryExceptionException;
import org.wso2.carbon.integration.common.admin.client.UserManagementClient;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.common.xsd.ResourceData;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.clients.*;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import javax.activation.DataHandler;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.*;

/**
 * Use LCs with different transitionPermission roles defined for
 * "Promote/Demote" options and make sure that users under only those roles are
 * allowed to do the transition. Login from other roles and verify whether they
 * are not allowed to perform LC transition
 */
public class LCPromoteDemotePermissionTestCase extends GREGIntegrationBaseTest {

    private String serviceString;
    private WSRegistryServiceClient wsRegistryServiceClient;
    private LifeCycleAdminServiceClient lifeCycleAdminServiceClient;
    private LifeCycleManagementClient lifeCycleManagementClient;
    private GovernanceServiceClient governanceServiceClient;
    private ListMetaDataServiceClient listMetadataServiceClient;
    private UserManagementClient userManagementClient;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private ServiceManager serviceManager;
    private final String SERVICE_NAME = "IntergalacticService13";
    private final String LC_NAME = "CheckListPermissionLC2";
    private static final String ACTION_PROMOTE = "Promote";
    private static final String ACTION_DEMOTE = "Demote";
    private static final String LC_STATE0 = "Commencement";
    private static final String LC_STATE1 = "Creation";
    private static final String ACTION_ITEM_CLICK = "itemClick";
    private static final String GOV_PATH = "/_system/governance";
    private String servicePath = "/trunk/services/com/abb/1.0.0-SNAPSHOT/IntergalacticService13";
    private final String absPath = GOV_PATH + servicePath;
    private LifecycleBean lifeCycle;
    private RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
    private ServiceBean service;
    private String userName1WithoutDomain;
    private String userName2WithoutDomain;

    /**
     * @throws RemoteException
     * @throws LoginAuthenticationExceptionException
     *
     * @throws RegistryException
     */
    @BeforeClass (alwaysRun = true)
    public void init () throws Exception {

        AutomationContext automationContextUser1 = new AutomationContext("GREG", "greg001",
                FrameworkConstants.SUPER_TENANT_KEY, "user1");
        String backendURL = automationContextUser1.getContextUrls().getBackEndUrl();
        String sessionCookie = new LoginLogoutClient(automationContextUser1).login();
        lifeCycleAdminServiceClient =
                new LifeCycleAdminServiceClient(backendURL, sessionCookie);
        governanceServiceClient =
                new GovernanceServiceClient(backendURL, sessionCookie);
        listMetadataServiceClient =
                new ListMetaDataServiceClient(backendURL, sessionCookie);
        lifeCycleManagementClient =
                new LifeCycleManagementClient(backendURL, sessionCookie);
        userManagementClient =
                new UserManagementClient(backendURL, sessionCookie);
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(backendURL, sessionCookie);
        wsRegistryServiceClient =
                registryProviderUtil.getWSRegistry(automationContextUser1);
        Registry reg =
                registryProviderUtil.getGovernanceRegistry
                        (new RegistryProviderUtil().getWSRegistry(automationContextUser1), automationContextUser1);
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) reg);
        serviceManager = new ServiceManager(reg);

        String userName = automationContextUser1.getContextTenant().getContextUser().getUserName();
        userName1WithoutDomain = userName.substring(0, userName.indexOf('@'));
    }

    /**
     * @throws XMLStreamException
     * @throws IOException
     * @throws AddServicesServiceRegistryExceptionException
     *
     * @throws ListMetadataServiceRegistryExceptionException
     *
     * @throws ResourceAdminServiceExceptionException
     *
     */
    @Test (groups = "wso2.greg", description = "Create a service")
    public void testCreateService () throws XMLStreamException, IOException,
            AddServicesServiceRegistryExceptionException,
            ListMetadataServiceRegistryExceptionException,
            ResourceAdminServiceExceptionException {

        String servicePath =
                getTestArtifactLocation() + "artifacts" +
                        File.separator + "GREG" + File.separator + "services" +
                        File.separator + "intergalacticService13.metadata.xml";
        DataHandler dataHandler = new DataHandler(new URL("file:///" + servicePath));
        String mediaType = "application/vnd.wso2-service+xml";
        String description = "This is a test service";
        resourceAdminServiceClient.addResource(
                "/_system/governance/service13", mediaType, description, dataHandler);
        ResourceData[] data = resourceAdminServiceClient.getResource(absPath);
        assertNotNull(data, "Service not found");

    }

    /**
     * @throws LifeCycleManagementServiceExceptionException
     *
     * @throws IOException
     * @throws InterruptedException
     */
    @Test (groups = "wso2.greg", description = "Create new life cycle", dependsOnMethods = "testCreateService")
    public void testCreateNewLifeCycle () throws LifeCycleManagementServiceExceptionException,
            IOException, InterruptedException {

        String resourcePath =
                getTestArtifactLocation() + "artifacts" +
                        File.separator + "GREG" + File.separator + "lifecycle" +
                        File.separator + "CheckListPermissionLC2.xml";
        String lifeCycleContent = FileManager.readFile(resourcePath);
        lifeCycleManagementClient.addLifeCycle(lifeCycleContent);
        String[] lifeCycles = lifeCycleManagementClient.getLifecycleList();
        boolean lcStatus = false;
        for (String lc : lifeCycles) {
            if (lc.equalsIgnoreCase(LC_NAME)) {
                lcStatus = true;
            }
        }
        assertTrue(lcStatus, "LifeCycle not found");

    }

    /**
     * @throws RegistryException
     * @throws RemoteException
     * @throws CustomLifecyclesChecklistAdminServiceExceptionException
     *
     * @throws ListMetadataServiceRegistryExceptionException
     *
     * @throws ResourceAdminServiceExceptionException
     *
     */
    @Test (groups = "wso2.greg", description = "Add lifecycle to a service", dependsOnMethods = "testCreateNewLifeCycle")
    public void testAddLcToService () throws RegistryException, RemoteException,
            CustomLifecyclesChecklistAdminServiceExceptionException,
            ListMetadataServiceRegistryExceptionException,
            ResourceAdminServiceExceptionException {

        wsRegistryServiceClient.associateAspect(absPath, LC_NAME);
        lifeCycle =
                lifeCycleAdminServiceClient.getLifecycleBean(absPath);
        Property[] properties = lifeCycle.getLifecycleProperties();
        boolean lcStatus = false;
        for (Property prop : properties) {
            if (prop.getKey().contains(LC_NAME)) {
                lcStatus = true;
            }
        }
        assertTrue(lcStatus, "LifeCycle not added to service");
    }

    /**
     * @throws Exception
     */
    @Test (groups = "wso2.greg", description = "Promote from Commencement to Creation",
            dependsOnMethods = "testAddLcToService")
    public void testPromoteToCreation () throws Exception {

        addRole("managerrole", userName1WithoutDomain);
        lifeCycle =
                lifeCycleAdminServiceClient.getLifecycleBean(absPath);
        boolean isPermitted = false;
        for (String action : lifeCycle.getAvailableActions()[0].getActions()) {
            if (action.equalsIgnoreCase(ACTION_PROMOTE)) {
                isPermitted = true;
            }
        }
        assertTrue(isPermitted, "Not allowed to promote with permission");
        lifeCycleAdminServiceClient.invokeAspect(absPath, LC_NAME,
                ACTION_PROMOTE, null);
        Service[] services = serviceManager.getAllServices();
        for (Service service : services) {
            String path = service.getPath();
            if (path.contains("IntergalacticService13")) {
                serviceString = path;
            }
        }
        lifeCycle =
                lifeCycleAdminServiceClient.getLifecycleBean(absPath);
        for (Property prop : lifeCycle.getLifecycleProperties()) {
            if (("registry.lifecycle." + LC_NAME + ".state").equalsIgnoreCase(prop.getKey())) {
                assertNotNull(prop.getValues(), "State Value Not Found");
                assertTrue(prop.getValues()[0].equalsIgnoreCase(LC_STATE1),
                        "Not promoted to Creation with permission");
            }
        }

    }

    /**
     * @throws Exception
     */
    @Test (groups = "wso2.greg", description = "Demote from Commencement to Creation",
            dependsOnMethods = "testPromoteToCreation", expectedExceptions = org.apache.axis2.AxisFault.class)
    public void testDemoteFromCreation () throws Exception {

        lifeCycle =
                lifeCycleAdminServiceClient.getLifecycleBean(GOV_PATH + serviceString);
        boolean isPermitted = false;
        for (String action : lifeCycle.getAvailableActions()[0].getActions()) {
            if (action.equalsIgnoreCase(ACTION_DEMOTE)) {
                isPermitted = true;
            }
        }
        assertFalse(isPermitted, "Allowed to demote without permission");
        lifeCycleAdminServiceClient.invokeAspect(GOV_PATH + serviceString, LC_NAME,
                ACTION_DEMOTE, null);
        lifeCycle =
                lifeCycleAdminServiceClient.getLifecycleBean(GOV_PATH + serviceString);
        for (Property prop : lifeCycle.getLifecycleProperties()) {
            if (("registry.lifecycle." + LC_NAME + ".state").equalsIgnoreCase(prop.getKey())) {
                assertNotNull(prop.getValues(), "State Value Not Found");
                assertTrue(prop.getValues()[0].equalsIgnoreCase(LC_STATE1),
                        "Demoted without permission");
            }
        }
    }

    /**
     * @throws RemoteException
     * @throws LoginAuthenticationExceptionException
     *
     * @throws RegistryException
     */
    @Test (groups = "wso2.greg", description = "Log in as a different user without devrole",
            dependsOnMethods = "testDemoteFromCreation")
    public void testSetNewUser () throws Exception {

        AutomationContext automationContextUser2 = new AutomationContext("GREG", "greg001",
                FrameworkConstants.SUPER_TENANT_KEY, "user2");
        String backendURL = automationContextUser2.getContextUrls().getBackEndUrl();
        String sessionCookie = new LoginLogoutClient(automationContextUser2).login();
        lifeCycleAdminServiceClient =
                new LifeCycleAdminServiceClient(backendURL, sessionCookie);
        governanceServiceClient =
                new GovernanceServiceClient(backendURL, sessionCookie);
        listMetadataServiceClient =
                new ListMetaDataServiceClient(backendURL, sessionCookie);
        lifeCycleManagementClient =
                new LifeCycleManagementClient(backendURL, sessionCookie);
        wsRegistryServiceClient =
                registryProviderUtil.getWSRegistry(automationContextUser2);
        userManagementClient =
                new UserManagementClient(backendURL, sessionCookie);
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(backendURL, sessionCookie);

        String userName = automationContextUser2.getContextTenant().getContextUser().getUserName();
        userName2WithoutDomain = userName.substring(0, userName.indexOf('@'));

    }

    /**
     * @throws Exception
     */
    @Test (groups = "wso2.greg", description = "Demote  from Creation with permission",
            dependsOnMethods = "testSetNewUser")
    public void testDemoteFromCreationAgain () throws Exception {

        addRole("archrole", userName2WithoutDomain);
        lifeCycle =
                lifeCycleAdminServiceClient.getLifecycleBean(absPath);
        boolean isPermitted = false;
        for (String action : lifeCycle.getAvailableActions()[0].getActions()) {
            if (action.equalsIgnoreCase(ACTION_DEMOTE)) {
                isPermitted = true;
            }
        }
        assertTrue(isPermitted, "Not allowed to demote with permission");
        lifeCycleAdminServiceClient.invokeAspect(absPath, LC_NAME,
                ACTION_DEMOTE, null);
        lifeCycle =
                lifeCycleAdminServiceClient.getLifecycleBean(absPath);
        for (Property prop : lifeCycle.getLifecycleProperties()) {
            if (("registry.lifecycle." + LC_NAME + ".state").equalsIgnoreCase(prop.getKey())) {
                assertNotNull(prop.getValues(), "State Value Not Found");
                assertTrue(prop.getValues()[0].equalsIgnoreCase(LC_STATE0),
                        "Not promoted to Creation with permission");
            }
        }

    }

    /**
     * @throws Exception Expected exception because user is not allowed to promote LC without permission(ex: using WS)
     */
    @Test (groups = "wso2.greg", description = "Promote from Commencement to Creation without permission",
            dependsOnMethods = "testDemoteFromCreationAgain", expectedExceptions = org.apache.axis2.AxisFault.class)
    public void testPromoteToCreationAgain () throws Exception,
            GovernanceException {

        lifeCycle =
                lifeCycleAdminServiceClient.getLifecycleBean(absPath);
        boolean isPermitted = false;
        for (String action : lifeCycle.getAvailableActions()[0].getActions()) {
            if (action.equalsIgnoreCase(ACTION_PROMOTE)) {
                isPermitted = true;
            }
        }
        assertFalse(isPermitted, "Allowed to promote without permission");
        lifeCycleAdminServiceClient.invokeAspect(absPath, LC_NAME,
                ACTION_ITEM_CLICK, new String[]{"false", "false", "false",
                "false", "false", "false"});
        lifeCycleAdminServiceClient.invokeAspect(absPath, LC_NAME,
                ACTION_PROMOTE, null);
        lifeCycle =
                lifeCycleAdminServiceClient.getLifecycleBean(absPath);
        for (Property prop : lifeCycle.getLifecycleProperties()) {
            if (("registry.lifecycle." + LC_NAME + ".state").equalsIgnoreCase(prop.getKey())) {
                assertNotNull(prop.getValues(), "State Value Not Found");
                assertTrue(prop.getValues()[0].equalsIgnoreCase(LC_STATE0),
                        "Promoted without permission");
            }
        }

    }

    /**
     * @param roleName name of the role needs to be added to the current user
     * @throws Exception
     */
    private void addRole (String roleName, String userName) throws Exception {

        String[] permissions = {"/permission/admin/manage/"};
        if (!userManagementClient.roleNameExists(roleName)) {
            userManagementClient.addRole(roleName, new String[]{userName}, permissions);
        }
    }

    /**
     * @throws Exception
     */
    @AfterClass ()
    public void clear () throws Exception {

        String servicePathToDelete = absPath;
        if (wsRegistryServiceClient.resourceExists(servicePathToDelete)) {
            resourceAdminServiceClient.deleteResource(servicePathToDelete);
        }
/*        String schemaPathToDelete = "/_system/governance/trunk/schemas/org/bar/purchasing/purchasing.xsd";
        if (wsRegistryServiceClient.resourceExists(schemaPathToDelete)) {
            resourceAdminServiceClient.deleteResource(schemaPathToDelete);
        }
        String wsdlPathToDelete = "/_system/governance/trunk/wsdls/com/foo/IntergalacticService.wsdl";
        if (wsRegistryServiceClient.resourceExists(wsdlPathToDelete)) {
            resourceAdminServiceClient.deleteResource(wsdlPathToDelete);
        }*/
        lifeCycleManagementClient.deleteLifeCycle(LC_NAME);
        if (userManagementClient.roleNameExists("archrole")) {
            userManagementClient.deleteRole("archrole");
        }
        if (userManagementClient.roleNameExists("managerrole")) {
            userManagementClient.deleteRole("managerrole");
        }
        governanceServiceClient = null;
        wsRegistryServiceClient = null;
        lifeCycleAdminServiceClient = null;
        lifeCycleManagementClient = null;
        governanceServiceClient = null;
        listMetadataServiceClient = null;
        resourceAdminServiceClient = null;
        resourceAdminServiceClient = null;
        userManagementClient = null;

    }
}
