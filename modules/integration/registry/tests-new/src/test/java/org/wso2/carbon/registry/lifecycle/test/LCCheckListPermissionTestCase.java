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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.governance.GovernanceServiceClient;
import org.wso2.carbon.automation.api.clients.governance.LifeCycleAdminServiceClient;
import org.wso2.carbon.automation.api.clients.governance.LifeCycleManagementClient;
import org.wso2.carbon.automation.api.clients.governance.ListMetaDataServiceClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.api.clients.user.mgt.UserManagementClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.core.utils.fileutils.FileManager;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.beans.xsd.LifecycleBean;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.governance.list.stub.ListMetadataServiceRegistryExceptionException;
import org.wso2.carbon.governance.list.stub.beans.xsd.ServiceBean;
import org.wso2.carbon.governance.services.stub.AddServicesServiceRegistryExceptionException;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.common.xsd.ResourceData;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.governance.api.exception.GovernanceException;

import org.wso2.carbon.governance.api.util.GovernanceUtils;

import javax.activation.DataHandler;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Specify different permission roles for different check list items of the same
 * LC state and verify whether only the correct user is allowed to tick the
 * checklist item
 */
public class LCCheckListPermissionTestCase {

    private int userId = 2;
    private UserInfo userInfo = UserListCsvReader.getUserInfo(userId);
    private WSRegistryServiceClient wsRegistryServiceClient;
    private LifeCycleAdminServiceClient lifeCycleAdminServiceClient;
    private LifeCycleManagementClient lifeCycleManagementClient;
    private GovernanceServiceClient governanceServiceClient;
    private ListMetaDataServiceClient listMetadataServiceClient;
    private UserManagementClient userManagementClient;
    private ResourceAdminServiceClient resourceAdminServiceClient;

    private ServiceManager serviceManager;

    private static final String SERVICE_NAME = "IntergalacticService5";
    private static final String LC_NAME = "CheckListPermissionLC3";
    private static final String ACTION_ITEM_CLICK = "itemClick";
    private static final String ITEM1 = "Requirements Gathered";
    private static final String ITEM2 = "Document Requirements";
    private static final String ITEM3 = "Architecture Diagram Finalized";
    private static final String GOV_PATH = "/_system/governance";
    private String serviceString = "/trunk/services/com/abb/IntergalacticService5";
    private final String absPath = GOV_PATH + serviceString;

    private LifecycleBean lifeCycle;
    private RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();

    /**
     * @throws RemoteException
     * @throws LoginAuthenticationExceptionException
     *
     * @throws RegistryException
     */
    @BeforeClass(alwaysRun = true)
    public void init() throws RemoteException, LoginAuthenticationExceptionException,
                              RegistryException {
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        ManageEnvironment environment = builder.build();

        lifeCycleAdminServiceClient =
                new LifeCycleAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                                environment.getGreg().getSessionCookie());
        governanceServiceClient =
                new GovernanceServiceClient(environment.getGreg().getProductVariables()
                                                    .getBackendUrl(), environment.getGreg().getSessionCookie());
        listMetadataServiceClient =
                new ListMetaDataServiceClient(environment.getGreg().getProductVariables()
                                                      .getBackendUrl(), environment.getGreg().getSessionCookie());
        lifeCycleManagementClient =
                new LifeCycleManagementClient(environment.getGreg().getProductVariables()
                                                      .getBackendUrl(), environment.getGreg().getSessionCookie());
        wsRegistryServiceClient =
                registryProviderUtil.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);

        userManagementClient =
                new UserManagementClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                         environment.getGreg().getSessionCookie());
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(environment.getGreg().getProductVariables()
                                                       .getBackendUrl(), environment.getGreg().getSessionCookie());

	Registry reg = registryProviderUtil.getGovernanceRegistry(new RegistryProviderUtil().getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME), userId);
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry)reg);
        serviceManager = new ServiceManager(reg);
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
    @Test(enabled = true, groups = "wso2.greg", description = "Create a service")
    public void testCreateService() throws XMLStreamException, IOException,
                                           AddServicesServiceRegistryExceptionException,
                                           ListMetadataServiceRegistryExceptionException,
                                           ResourceAdminServiceExceptionException {

        String servicePath =
                ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
                File.separator + "GREG" + File.separator + "services" +
                File.separator + "intergalacticService5.metadata.xml";
        DataHandler dataHandler = new DataHandler(new URL("file:///" + servicePath));
        String mediaType = "application/vnd.wso2-service+xml";
        String description = "This is a test service";
        resourceAdminServiceClient.addResource(
                "/_system/governance/service2", mediaType, description, dataHandler);

        ResourceData[] data =  resourceAdminServiceClient.getResource(absPath);
        
        assertNotNull(data, "Service not found");
    }

    /**
     * @throws LifeCycleManagementServiceExceptionException
     *
     * @throws IOException
     * @throws InterruptedException
     */
    @Test(enabled = true, groups = "wso2.greg", description = "Create new life cycle",
          dependsOnMethods = "testCreateService")
    public void testCreateNewLifeCycle() throws LifeCycleManagementServiceExceptionException,
                                                IOException, InterruptedException {
        String resourcePath =
                ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
                File.separator + "GREG" + File.separator + "lifecycle" +
                File.separator + "CheckListPermissionLC3.xml";
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
    @Test(enabled = true, groups = "wso2.greg", description = "Add lifecycle to a service",
          dependsOnMethods = "testCreateNewLifeCycle")
    public void testAddLcToService() throws RegistryException, RemoteException,
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
    @Test(enabled = true, groups = "wso2.greg", description = "Check Commencement checklist without permission",
          dependsOnMethods = "testAddLcToService", expectedExceptions = org.apache.axis2.AxisFault.class)
    public void testClickCheckListItem() throws Exception {


        lifeCycleAdminServiceClient.invokeAspect(absPath, LC_NAME,
                                                 ACTION_ITEM_CLICK, new String[]{"true", "false",
                                                                                 "false"});

    }

    /**
     * @throws Exception
     */
    @Test(enabled = true, groups = "wso2.greg", description = "Check Commencement checklist with devrole",
          dependsOnMethods = "testClickCheckListItem", expectedExceptions = org.apache.axis2.AxisFault.class)
    public void testClickCheckListItemDevRole() throws Exception {

        addRole("devrole");
        lifeCycleAdminServiceClient.invokeAspect(absPath, LC_NAME,
                                                 ACTION_ITEM_CLICK, new String[]{"true", "false",
                                                                                 "false"});
        lifeCycle =
                lifeCycleAdminServiceClient.getLifecycleBean(absPath);
        boolean item1Status = false;
        for (Property prop : lifeCycle.getLifecycleProperties()) {
            if (prop.getKey().contains("registry.custom_lifecycle.checklist.option.") && !prop.getKey().contains("permission")) {
                for (String name : prop.getValues()) {
                    if (name.contains(ITEM1)) {
                        for (String value : prop.getValues()) {
                            if (value.equals("value:true")) {
                                item1Status = true;
                            }
                        }
                    }
                }
            }
        }
        assertTrue(item1Status, "Item:" + ITEM1 + " not clicked");
        try {
            lifeCycleAdminServiceClient.invokeAspect(absPath, LC_NAME,
                                                     ACTION_ITEM_CLICK, new String[]{"true", "true",
                                                                                     "false"});
        } catch (AxisFault e) {

            lifeCycleAdminServiceClient.invokeAspect(absPath, LC_NAME,
                                                     ACTION_ITEM_CLICK,
                                                     new String[]{"false", "false", "false"});
            throw new AxisFault(e.getMessage());
        }
    }

    /**
     * @throws Exception
     */
    @Test(enabled = true, groups = "wso2.greg", description = "Check Commencement checklist with archrole",
          dependsOnMethods = "testClickCheckListItemDevRole", expectedExceptions = org.apache.axis2.AxisFault.class)
    public void testClickCheckListItemArchRole() throws Exception {

        addRole("archrole");
        lifeCycleAdminServiceClient.invokeAspect(absPath, LC_NAME,
                                                 ACTION_ITEM_CLICK, new String[]{"true", "false",
                                                                                 "false"});
        lifeCycleAdminServiceClient.invokeAspect(absPath, LC_NAME,
                                                 ACTION_ITEM_CLICK, new String[]{"true", "true",
                                                                                 "false"});
        lifeCycle =
                lifeCycleAdminServiceClient.getLifecycleBean(absPath);

        boolean item1Status = false;
        boolean item2Status = false;

        for (Property prop : lifeCycle.getLifecycleProperties()) {
            if (prop.getKey().contains("registry.custom_lifecycle.checklist.option.") && !prop.getKey().contains("permission")) {
                for (String name : prop.getValues()) {
                    if (name.contains(ITEM1)) {
                        for (String value : prop.getValues()) {
                            if (value.equals("value:true")) {
                                item1Status = true;
                            }
                        }
                    }
                    if (name.contains(ITEM2)) {
                        for (String value : prop.getValues()) {
                            if (value.equals("value:true")) {
                                item2Status = true;
                            }
                        }
                    }
                }
            }
        }
        assertTrue(item1Status, "Item:" + ITEM1 + " not clicked");
        assertTrue(item2Status, "Item:" + ITEM2 + " not clicked");

        try {
            lifeCycleAdminServiceClient.invokeAspect(absPath, LC_NAME,
                                                     ACTION_ITEM_CLICK, new String[]{"true", "true",
                                                                                     "true"});
        } catch (AxisFault e) {
            lifeCycleAdminServiceClient.invokeAspect(absPath, LC_NAME,
                                                     ACTION_ITEM_CLICK,
                                                     new String[]{"false", "false", "false"});
            throw new AxisFault(e.getMessage());
        }
    }


    /**
     * @throws Exception
     */
    @Test(enabled = true, groups = "wso2.greg", description = "Check Commencement checklist with techoprole",
          dependsOnMethods = "testClickCheckListItemArchRole")
    public void testClickCheckListItemTechopRole() throws Exception {
        addRole("techoprole");
        lifeCycleAdminServiceClient.invokeAspect(absPath, LC_NAME,
                                                 ACTION_ITEM_CLICK, new String[]{"true", "false",
                                                                                 "true"});
        lifeCycleAdminServiceClient.invokeAspect(absPath, LC_NAME,
                                                 ACTION_ITEM_CLICK, new String[]{"true", "true",
                                                                                 "false"});
        lifeCycleAdminServiceClient.invokeAspect(absPath, LC_NAME,
                                                 ACTION_ITEM_CLICK, new String[]{"true", "true",
                                                                                 "true"});
        lifeCycle =
                lifeCycleAdminServiceClient.getLifecycleBean(absPath);
        boolean item1Status = false;
        boolean item2Status = false;
        boolean item3Status = false;
        for (Property prop : lifeCycle.getLifecycleProperties()) {
            if (prop.getKey().contains("registry.custom_lifecycle.checklist.option.") && !prop.getKey().contains("permission")) {
                for (String name : prop.getValues()) {
                    if (name.contains(ITEM1)) {
                        for (String value : prop.getValues()) {
                            if (value.equals("value:true")) {
                                item1Status = true;
                            }
                        }
                    }
                    if (name.contains(ITEM2)) {
                        for (String value : prop.getValues()) {
                            if (value.equals("value:true")) {
                                item2Status = true;
                            }
                        }
                    }
                    if (name.contains(ITEM3)) {
                        for (String value : prop.getValues()) {
                            if (value.equals("value:true")) {
                                item3Status = true;
                            }
                        }
                    }
                }
            }
        }
        assertTrue(item1Status, "Item:" + ITEM1 + " not clicked");
        assertTrue(item2Status, "Item:" + ITEM2 + " not clicked");
        assertTrue(item3Status, "Item:" + ITEM3 + " not clicked");

        lifeCycleAdminServiceClient.invokeAspect(absPath, LC_NAME,
                                                 ACTION_ITEM_CLICK,
                                                 new String[]{"false", "false", "false"});
    }

    /**
     * @param roleName name of the role needs to be added to the current user
     * @throws Exception
     */
    private void addRole(String roleName) throws Exception {
        String[] permissions = {"/permission/admin/manage/"};
        userManagementClient.addRole(roleName, new String[]{userInfo.getUserNameWithoutDomain()}, permissions);
    }

    /**
     * @throws Exception
     */
    @AfterClass()
    public void clear() throws Exception {
        String servicePathToDelete = absPath;
        if (wsRegistryServiceClient.resourceExists(servicePathToDelete)) {
            resourceAdminServiceClient.deleteResource(servicePathToDelete);
        }
        /*
        String schemaPathToDelete = "/_system/governance/trunk/schemas/org/bar/purchasing/purchasing.xsd";
        if (wsRegistryServiceClient.resourceExists(schemaPathToDelete)) {
            resourceAdminServiceClient.deleteResource(schemaPathToDelete);
        }        */
//        String wsdlPathToDelete = "/_system/governance/trunk/wsdls/com/foo/IntergalacticService.wsdl";
  //      if (wsRegistryServiceClient.resourceExists(wsdlPathToDelete)) {
    //        resourceAdminServiceClient.deleteResource(wsdlPathToDelete);
      //  }
        lifeCycleManagementClient.deleteLifeCycle(LC_NAME);

        userManagementClient.deleteRole("devrole");
        userManagementClient.deleteRole("archrole");
        userManagementClient.deleteRole("techoprole");

        governanceServiceClient = null;
        wsRegistryServiceClient = null;
        lifeCycleAdminServiceClient = null;
        lifeCycleManagementClient = null;
        listMetadataServiceClient = null;
        resourceAdminServiceClient = null;
        userManagementClient = null;

    }

}
