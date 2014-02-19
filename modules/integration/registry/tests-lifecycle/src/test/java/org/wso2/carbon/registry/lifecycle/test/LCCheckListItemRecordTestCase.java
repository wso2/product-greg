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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.governance.GovernanceServiceClient;
import org.wso2.carbon.automation.api.clients.governance.LifeCycleAdminServiceClient;
import org.wso2.carbon.automation.api.clients.governance.LifeCycleManagementClient;
import org.wso2.carbon.automation.api.clients.governance.ListMetaDataServiceClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
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
import org.wso2.carbon.registry.activities.stub.RegistryExceptionException;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.common.xsd.ResourceData;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.xml.sax.InputSource;

import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.governance.api.exception.GovernanceException;

import org.wso2.carbon.governance.api.util.GovernanceUtils;

import javax.activation.DataHandler;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class LCCheckListItemRecordTestCase {
    private int userId = 2;
    UserInfo userInfo = UserListCsvReader.getUserInfo(userId);    
    private WSRegistryServiceClient wsRegistryServiceClient;
    private String auditPath = "/_system/governance/repository/components/org.wso2.carbon.governance/" +
                               "lifecycles/history/__system_governance_trunk_services_com_abb_IntergalacticService7";

    private LifeCycleAdminServiceClient lifeCycleAdminServiceClient;
    private LifeCycleManagementClient lifeCycleManagementClient;
    private GovernanceServiceClient governanceServiceClient;
    private ListMetaDataServiceClient listMetadataServiceClient;
    private ResourceAdminServiceClient resourceAdminServiceClient;

    private ServiceManager serviceManager;

    private static final String SERVICE_NAME = "IntergalacticService7";
    private static final String LC_NAME = "CheckListLC";
    private static final String LC_STATE0 = "Development";
    private static final String LC_STATE1 = "Testing";
    private static final String ACTION_PROMOTE = "Promote";
    private static final String ACTION_TYPE = "type";
    private static final String ACTION_ITEM_CLICK = "itemClick";
    private static final String USER = "user";
    private static final String STATE = "state";
    private static final String VALUE_TRUE = "value:true";
    private static final String VALUE_FALSE = "value:false";
    private static final String TYPE = "itemClick";
    private static final String ACTION_NAME = "name";
    private static final String DEV_ITEM1 = "Code Completed";
    private static final String DEV_ITEM2 = "WSDL, Schema Created";
    private static final String DEV_ITEM3 = "QoS Created";
    private static final String Q_ITEM1 = "Effective Inspection Completed";
    private static final String Q_ITEM2 = "Test Cases Passed";
    private static final String Q_ITEM3 = "Smoke Test Passed";

    private static final String GOV_PATH = "/_system/governance";
    private String serviceString = "/trunk/services/com/abb/IntergalacticService7";
    private final String absPath = GOV_PATH + serviceString;


    private LifecycleBean lifeCycle;
    private RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();

    @BeforeClass(alwaysRun = true)
    public void init()
            throws RemoteException, LoginAuthenticationExceptionException, RegistryException {
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        ManageEnvironment environment = builder.build();

        lifeCycleAdminServiceClient =
                new LifeCycleAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                                environment.getGreg().getSessionCookie());
        governanceServiceClient =
                new GovernanceServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                            environment.getGreg().getSessionCookie());
        listMetadataServiceClient =
                new ListMetaDataServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                              environment.getGreg().getSessionCookie());
        lifeCycleManagementClient =
                new LifeCycleManagementClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                              environment.getGreg().getSessionCookie());
        wsRegistryServiceClient =
                registryProviderUtil.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                               environment.getGreg().getSessionCookie());

	Registry reg = registryProviderUtil.getGovernanceRegistry(new RegistryProviderUtil().getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME), userId);
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry)reg);
        serviceManager = new ServiceManager(reg);
    }


    @Test(groups = "wso2.greg", description = "Create a service")
    public void testCreateService()
            throws XMLStreamException, IOException, AddServicesServiceRegistryExceptionException,
                   ListMetadataServiceRegistryExceptionException,
                   ResourceAdminServiceExceptionException {

        String servicePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
                             File.separator + "GREG" + File.separator + "services" + File.separator +
                             "intergalacticService7.metadata.xml";
        DataHandler dataHandler = new DataHandler(new URL("file:///" + servicePath));
        String mediaType = "application/vnd.wso2-service+xml";
        String description = "This is a test service";
        resourceAdminServiceClient.addResource(
                "/_system/governance/service2", mediaType, description, dataHandler);

        ResourceData[] data =  resourceAdminServiceClient.getResource(absPath);
        
        assertNotNull(data, "Service not found");

    }

    @Test(groups = "wso2.greg", description = "Create new life cycle", dependsOnMethods = "testCreateService")
    public void testCreateNewLifeCycle()
            throws LifeCycleManagementServiceExceptionException, IOException, InterruptedException {
        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
                              File.separator + "GREG" + File.separator + "lifecycle" + File.separator +
                              "CheckItemTickedValidatorLifeCycle.xml";
        String lifeCycleContent = FileManager.readFile(resourcePath);
        lifeCycleManagementClient.addLifeCycle(lifeCycleContent);
        String[] lifeClycles = lifeCycleManagementClient.getLifecycleList();

        boolean lcStatus = false;
        for (String lc : lifeClycles) {

            if (lc.equalsIgnoreCase(LC_NAME)) {
                lcStatus = true;
            }
        }
        assertTrue(lcStatus, "LifeCycle not found");

    }

    @Test(groups = "wso2.greg", description = "Add lifecycle to a service", dependsOnMethods = "testCreateNewLifeCycle")
    public void testAddLcToService() throws RegistryException, RemoteException,
                                            CustomLifecyclesChecklistAdminServiceExceptionException,
                                            ListMetadataServiceRegistryExceptionException,
                                            ResourceAdminServiceExceptionException {

        wsRegistryServiceClient.associateAspect(absPath, LC_NAME);
        lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(absPath);

        Property[] properties = lifeCycle.getLifecycleProperties();

        boolean lcStatus = false;
        for (Property prop : properties) {
            if (prop.getKey().contains(LC_NAME)) {
                lcStatus = true;
            }
        }
        assertTrue(lcStatus, "LifeCycle not added to service");
    }

    @Test(groups = "wso2.greg", description = "Check Development checklist", dependsOnMethods = "testAddLcToService")
    public void testClickDevelopmentCheckListItem()
            throws RemoteException, CustomLifecyclesChecklistAdminServiceExceptionException {
        lifeCycleAdminServiceClient.invokeAspect(absPath, LC_NAME,
                                                 ACTION_ITEM_CLICK, new String[]{"true", "false", "false"});
        lifeCycleAdminServiceClient.invokeAspect(absPath, LC_NAME,
                                                 ACTION_ITEM_CLICK, new String[]{"true", "true", "false"});
        lifeCycleAdminServiceClient.invokeAspect(absPath, LC_NAME,
                                                 ACTION_ITEM_CLICK, new String[]{"true", "true", "true"});
        lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(absPath);

        for (Property prop : lifeCycle.getLifecycleProperties()) {
            if (("registry.custom_lifecycle.checklist.option.1.item").equalsIgnoreCase(prop.getKey())) {
                assertNotNull(prop.getValues(), "Item Not Found");
                assertEquals(prop.getValues()[0], "status:" + LC_STATE0, "State is not development");
                assertEquals(prop.getValues()[3], "value:true", "Item not clicked");
            }
            if (("registry.custom_lifecycle.checklist.option.2.item").equalsIgnoreCase(prop.getKey())) {
                assertNotNull(prop.getValues(), "Item Not Found");
                assertEquals(prop.getValues()[0], "status:" + LC_STATE0, "State is not development");
                assertEquals(prop.getValues()[3], "value:true", "Item not clicked");
            }
            if (("registry.custom_lifecycle.checklist.option.3.item").equalsIgnoreCase(prop.getKey())) {
                assertNotNull(prop.getValues(), "Item Not Found");
                assertEquals(prop.getValues()[0], "status:" + LC_STATE0, "State is not development");
                assertEquals(prop.getValues()[3], "value:true", "Item not clicked");
            }
        }
    }

    @Test(groups = "wso2.greg", description = "Verify Audit records", dependsOnMethods = "testClickDevelopmentCheckListItem")
    public void testVerifyAuditDevCheck() throws Exception {

        assertEquals(getAuditRecords(auditPath, 2, USER, false, false), userInfo.getUserNameWithoutDomain(), "User is not testuser1");
        assertEquals(getAuditRecords(auditPath, 2, STATE, false, false), LC_STATE0, "State is not" + LC_STATE0);
        assertEquals(getAuditRecords(auditPath, 2, ACTION_TYPE, true, false), TYPE, "Action is not itemClick");
        assertEquals(getAuditRecords(auditPath, 2, ACTION_NAME, true, false), DEV_ITEM1, "No" + DEV_ITEM1 + "recorded");
        assertEquals(getAuditRecords(auditPath, 2, null, true, true), VALUE_TRUE, "Item not clicked");

        assertEquals(getAuditRecords(auditPath, 1, USER, false, false), userInfo.getUserNameWithoutDomain(), "User is not testuser1");
        assertEquals(getAuditRecords(auditPath, 1, STATE, false, false), LC_STATE0, "State is not" + LC_STATE0);
        assertEquals(getAuditRecords(auditPath, 1, ACTION_TYPE, true, false), TYPE, "Action is not itemClick");
        assertEquals(getAuditRecords(auditPath, 1, ACTION_NAME, true, false), DEV_ITEM2, "No" + DEV_ITEM2 + "recorded");
        assertEquals(getAuditRecords(auditPath, 1, null, true, true), VALUE_TRUE, "Item not clicked");

        assertEquals(getAuditRecords(auditPath, 0, USER, false, false), userInfo.getUserNameWithoutDomain(), "User is not testuser1");
        assertEquals(getAuditRecords(auditPath, 0, STATE, false, false), LC_STATE0, "State is not" + LC_STATE0);
        assertEquals(getAuditRecords(auditPath, 0, ACTION_TYPE, true, false), TYPE, "Action is not itemClick");
        assertEquals(getAuditRecords(auditPath, 0, ACTION_NAME, true, false), DEV_ITEM3, "No" + DEV_ITEM3 + "recorded");
        assertEquals(getAuditRecords(auditPath, 0, null, true, true), VALUE_TRUE, "Item not clicked");

    }

    @Test(groups = "wso2.greg", description = "Untick Development checklist", dependsOnMethods = "testVerifyAuditDevCheck")
    public void testUnclickDevelopmentCheckListItem()
            throws RemoteException, CustomLifecyclesChecklistAdminServiceExceptionException {
        lifeCycleAdminServiceClient.invokeAspect(absPath, LC_NAME,
                                                 ACTION_ITEM_CLICK, new String[]{"false", "true", "true"});
        lifeCycleAdminServiceClient.invokeAspect(absPath, LC_NAME,
                                                 ACTION_ITEM_CLICK, new String[]{"false", "false", "true"});
        lifeCycleAdminServiceClient.invokeAspect(absPath, LC_NAME,
                                                 ACTION_ITEM_CLICK, new String[]{"false", "false", "false"});
        lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(absPath);

        for (Property prop : lifeCycle.getLifecycleProperties()) {
            if (("registry.custom_lifecycle.checklist.option.1.item").equalsIgnoreCase(prop.getKey())) {
                assertNotNull(prop.getValues(), "Item Not Found");
                assertEquals(prop.getValues()[0], "status:" + LC_STATE0, "State is not development");
                assertEquals(prop.getValues()[3], VALUE_FALSE, "Item not unclicked");
            }
            if (("registry.custom_lifecycle.checklist.option.2.item").equalsIgnoreCase(prop.getKey())) {
                assertNotNull(prop.getValues(), "Item Not Found");
                assertEquals(prop.getValues()[0], "status:" + LC_STATE0, "State is not development");
                assertEquals(prop.getValues()[3], VALUE_FALSE, "Item not unclicked");
            }
            if (("registry.custom_lifecycle.checklist.option.3.item").equalsIgnoreCase(prop.getKey())) {
                assertNotNull(prop.getValues(), "Item Not Found");
                assertEquals(prop.getValues()[0], "status:" + LC_STATE0, "State is not development");
                assertEquals(prop.getValues()[3], VALUE_FALSE, "Item not unclicked");
            }
        }
    }

    @Test(groups = "wso2.greg", description = "Verify Audit records", dependsOnMethods = "testUnclickDevelopmentCheckListItem")
    public void testVerifyAuditDevUncheck() throws Exception {

        assertEquals(getAuditRecords(auditPath, 2, USER, false, false), userInfo.getUserNameWithoutDomain(), "User is not testuser1");
        assertEquals(getAuditRecords(auditPath, 2, STATE, false, false), LC_STATE0, "State is not" + LC_STATE0);
        assertEquals(getAuditRecords(auditPath, 2, ACTION_TYPE, true, false), TYPE, "Action is not itemClick");
        assertEquals(getAuditRecords(auditPath, 2, ACTION_NAME, true, false), DEV_ITEM1, "No" + DEV_ITEM1 + "recorded");
        assertEquals(getAuditRecords(auditPath, 2, null, true, true), VALUE_FALSE, "Item not clicked");

        assertEquals(getAuditRecords(auditPath, 1, USER, false, false), userInfo.getUserNameWithoutDomain(), "User is not testuser1");
        assertEquals(getAuditRecords(auditPath, 1, STATE, false, false), LC_STATE0, "State is not" + LC_STATE0);
        assertEquals(getAuditRecords(auditPath, 1, ACTION_TYPE, true, false), TYPE, "Action is not itemClick");
        assertEquals(getAuditRecords(auditPath, 1, ACTION_NAME, true, false), DEV_ITEM2, "No" + DEV_ITEM2 + "recorded");
        assertEquals(getAuditRecords(auditPath, 1, null, true, true), VALUE_FALSE, "Item not clicked");

        assertEquals(getAuditRecords(auditPath, 0, USER, false, false), userInfo.getUserNameWithoutDomain(), "User is not testuser1");
        assertEquals(getAuditRecords(auditPath, 0, STATE, false, false), LC_STATE0, "State is not" + LC_STATE0);
        assertEquals(getAuditRecords(auditPath, 0, ACTION_TYPE, true, false), TYPE, "Action is not itemClick");
        assertEquals(getAuditRecords(auditPath, 0, ACTION_NAME, true, false), DEV_ITEM3, "No" + DEV_ITEM3 + "recorded");
        assertEquals(getAuditRecords(auditPath, 0, null, true, true), VALUE_FALSE, "Item not clicked");

    }

    @Test(groups = "wso2.greg", description = "Promote from Development to Testing", dependsOnMethods = "testVerifyAuditDevUncheck")
    public void testPromoteLCToTesting()
            throws RemoteException, CustomLifecyclesChecklistAdminServiceExceptionException,
                   LifeCycleManagementServiceExceptionException, RegistryExceptionException {

        lifeCycleAdminServiceClient.invokeAspect(absPath, LC_NAME, ACTION_PROMOTE, null);
        lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(absPath);

        for (Property prop : lifeCycle.getLifecycleProperties()) {
            if (("registry.lifecycle." + LC_NAME + ".state").equalsIgnoreCase(prop.getKey())) {
                assertNotNull(prop.getValues(), "State Value Not Found");
                assertTrue(prop.getValues()[0].equalsIgnoreCase(LC_STATE1), "LifeCycle not promoted to Testing");
            }
        }

    }

    @Test(groups = "wso2.greg", description = "Check Testing checklist", dependsOnMethods = "testPromoteLCToTesting")
    public void testClickTestingCheckListItem()
            throws RemoteException, CustomLifecyclesChecklistAdminServiceExceptionException {
        lifeCycleAdminServiceClient.invokeAspect(absPath, LC_NAME,
                                                 ACTION_ITEM_CLICK, new String[]{"true", "false", "false"});
        lifeCycleAdminServiceClient.invokeAspect(absPath, LC_NAME,
                                                 ACTION_ITEM_CLICK, new String[]{"true", "true", "false"});
        lifeCycleAdminServiceClient.invokeAspect(absPath, LC_NAME,
                                                 ACTION_ITEM_CLICK, new String[]{"true", "true", "true"});
        lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(absPath);

        for (Property prop : lifeCycle.getLifecycleProperties()) {
            if (("registry.custom_lifecycle.checklist.option.1.item").equalsIgnoreCase(prop.getKey())) {
                assertNotNull(prop.getValues(), "Item Not Found");
                assertEquals(prop.getValues()[0], "status:" + LC_STATE1, "State is not " + LC_STATE1);
                assertEquals(prop.getValues()[3], VALUE_TRUE, "Item not clicked");
            }
            if (("registry.custom_lifecycle.checklist.option.2.item").equalsIgnoreCase(prop.getKey())) {
                assertNotNull(prop.getValues(), "Item Not Found");
                assertEquals(prop.getValues()[0], "status:" + LC_STATE1, "State is not " + LC_STATE1);
                assertEquals(prop.getValues()[3], VALUE_TRUE, "Item not clicked");
            }
            if (("registry.custom_lifecycle.checklist.option.3.item").equalsIgnoreCase(prop.getKey())) {
                assertNotNull(prop.getValues(), "Item Not Found");
                assertEquals(prop.getValues()[0], "status:" + LC_STATE1, "State is not " + LC_STATE1);
                assertEquals(prop.getValues()[3], VALUE_TRUE, "Item not clicked");
            }
        }
    }

    @Test(groups = "wso2.greg", description = "Verify Audit records", dependsOnMethods = "testClickTestingCheckListItem")
    public void testVerifyAuditTestCheck() throws Exception {

        assertEquals(getAuditRecords(auditPath, 2, USER, false, false), userInfo.getUserNameWithoutDomain(), "User is not testuser1");
        assertEquals(getAuditRecords(auditPath, 2, STATE, false, false), LC_STATE1, "State is not " + LC_STATE1);
        assertEquals(getAuditRecords(auditPath, 2, ACTION_TYPE, true, false), TYPE, "Action is not itemClick");
        assertEquals(getAuditRecords(auditPath, 2, ACTION_NAME, true, false), Q_ITEM1, "No" + Q_ITEM1 + "recorded");
        assertEquals(getAuditRecords(auditPath, 2, null, true, true), VALUE_TRUE, "Item not clicked");

        assertEquals(getAuditRecords(auditPath, 1, USER, false, false), userInfo.getUserNameWithoutDomain(), "User is not testuser1");
        assertEquals(getAuditRecords(auditPath, 1, STATE, false, false), LC_STATE1, "State is not " + LC_STATE1);
        assertEquals(getAuditRecords(auditPath, 1, ACTION_TYPE, true, false), TYPE, "Action is not itemClick");
        assertEquals(getAuditRecords(auditPath, 1, ACTION_NAME, true, false), Q_ITEM2, "No " + Q_ITEM2 + " recorded");
        assertEquals(getAuditRecords(auditPath, 1, null, true, true), VALUE_TRUE, "Item not clicked");

        assertEquals(getAuditRecords(auditPath, 0, USER, false, false), userInfo.getUserNameWithoutDomain(), "User is not testuser1");
        assertEquals(getAuditRecords(auditPath, 0, STATE, false, false), LC_STATE1, "State is not " + LC_STATE1);
        assertEquals(getAuditRecords(auditPath, 0, ACTION_TYPE, true, false), TYPE, "Action is not itemClick");
        assertEquals(getAuditRecords(auditPath, 0, ACTION_NAME, true, false), Q_ITEM3, "No " + Q_ITEM3 + " recorded");
        assertEquals(getAuditRecords(auditPath, 0, null, true, true), VALUE_TRUE, "Item not clicked");

    }


    @Test(groups = "wso2.greg", description = "Check Testing checklist", dependsOnMethods = "testVerifyAuditTestCheck")
    public void testUnclickTestingCheckListItem()
            throws RemoteException, CustomLifecyclesChecklistAdminServiceExceptionException {
        lifeCycleAdminServiceClient.invokeAspect(absPath, LC_NAME,
                                                 ACTION_ITEM_CLICK, new String[]{"false", "true", "true"});
        lifeCycleAdminServiceClient.invokeAspect(absPath, LC_NAME,
                                                 ACTION_ITEM_CLICK, new String[]{"false", "false", "true"});
        lifeCycleAdminServiceClient.invokeAspect(absPath, LC_NAME,
                                                 ACTION_ITEM_CLICK, new String[]{"false", "false", "false"});
        lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(absPath);

        for (Property prop : lifeCycle.getLifecycleProperties()) {
            if (("registry.custom_lifecycle.checklist.option.1.item").equalsIgnoreCase(prop.getKey())) {
                assertNotNull(prop.getValues(), "Item Not Found");
                assertEquals(prop.getValues()[0], "status:" + LC_STATE1, "State is not " + LC_STATE1);
                assertEquals(prop.getValues()[3], VALUE_FALSE, "Item not unclicked");
            }
            if (("registry.custom_lifecycle.checklist.option.2.item").equalsIgnoreCase(prop.getKey())) {
                assertNotNull(prop.getValues(), "Item Not Found");
                assertEquals(prop.getValues()[0], "status:" + LC_STATE1, "State is not " + LC_STATE1);
                assertEquals(prop.getValues()[3], VALUE_FALSE, "Item not unclicked");
            }
            if (("registry.custom_lifecycle.checklist.option.3.item").equalsIgnoreCase(prop.getKey())) {
                assertNotNull(prop.getValues(), "Item Not Found");
                assertEquals(prop.getValues()[0], "status:" + LC_STATE1, "State is not " + LC_STATE1);
                assertEquals(prop.getValues()[3], VALUE_FALSE, "Item not unclicked");
            }
        }
    }

    @Test(groups = "wso2.greg", description = "Verify Audit records", dependsOnMethods = "testUnclickTestingCheckListItem")
    public void testVerifyAuditTestUncheck() throws Exception {

        assertEquals(getAuditRecords(auditPath, 2, USER, false, false), userInfo.getUserNameWithoutDomain(), "User is not testuser1");
        assertEquals(getAuditRecords(auditPath, 2, STATE, false, false), LC_STATE1, "State is not " + LC_STATE1);
        assertEquals(getAuditRecords(auditPath, 2, ACTION_TYPE, true, false), TYPE, "Action is not itemClick");
        assertEquals(getAuditRecords(auditPath, 2, ACTION_NAME, true, false), Q_ITEM1, "No " + Q_ITEM1 + " recorded");
        assertEquals(getAuditRecords(auditPath, 2, null, true, true), VALUE_FALSE, "Item not clicked");

        assertEquals(getAuditRecords(auditPath, 1, USER, false, false), userInfo.getUserNameWithoutDomain(), "User is not testuser1");
        assertEquals(getAuditRecords(auditPath, 1, STATE, false, false), LC_STATE1, "State is not " + LC_STATE1);
        assertEquals(getAuditRecords(auditPath, 1, ACTION_TYPE, true, false), TYPE, "Action is not itemClick");
        assertEquals(getAuditRecords(auditPath, 1, ACTION_NAME, true, false), Q_ITEM2, "No " + Q_ITEM2 + " recorded");
        assertEquals(getAuditRecords(auditPath, 1, null, true, true), VALUE_FALSE, "Item not clicked");

        assertEquals(getAuditRecords(auditPath, 0, USER, false, false), userInfo.getUserNameWithoutDomain(), "User is not testuser1");
        assertEquals(getAuditRecords(auditPath, 0, STATE, false, false), LC_STATE1, "State is not " + LC_STATE1);
        assertEquals(getAuditRecords(auditPath, 0, ACTION_TYPE, true, false), TYPE, "Action is not itemClick");
        assertEquals(getAuditRecords(auditPath, 0, ACTION_NAME, true, false), Q_ITEM3, "No " + Q_ITEM3 + " recorded");
        assertEquals(getAuditRecords(auditPath, 0, null, true, true), VALUE_FALSE, "Item not clicked");

    }

    private String getAuditRecords(String auditPath, int node, String item, boolean isChild,
                                   boolean isNodeValue) throws Exception {

        byte[] string = wsRegistryServiceClient.getContent(auditPath);
        String xml = new String(string);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(xml)));
        Element rootElement = document.getDocumentElement();
        NodeList nd = rootElement.getChildNodes();

        assertFalse((node >= nd.getLength()), "Record does not exist");

        if (!isChild) {
            return nd.item(node).getAttributes().getNamedItem(item).getNodeValue();
        } else if (!isNodeValue) {
            return nd.item(node).getFirstChild().getAttributes().getNamedItem(item).getNodeValue();
        } else {
            return nd.item(node).getFirstChild().getFirstChild().getTextContent();
        }
    }

    @AfterClass()
    public void clear() throws Exception {
        String servicePathToDelete = absPath;
        if (wsRegistryServiceClient.resourceExists(servicePathToDelete)) {
            resourceAdminServiceClient.deleteResource(servicePathToDelete);
        }
//        String schemaPathToDelete = "/_system/governance/trunk/schemas/org/bar/purchasing/purchasing.xsd";
  //      if (wsRegistryServiceClient.resourceExists(schemaPathToDelete)) {
    //        resourceAdminServiceClient.deleteResource(schemaPathToDelete);
      //  }
       // String wsdlPathToDelete = "/_system/governance/trunk/wsdls/com/foo/IntergalacticService.wsdl";
        //if (wsRegistryServiceClient.resourceExists(wsdlPathToDelete)) {
          //  resourceAdminServiceClient.deleteResource(wsdlPathToDelete);
       // }
        lifeCycleManagementClient.deleteLifeCycle(LC_NAME);

        governanceServiceClient = null;
        wsRegistryServiceClient = null;
        lifeCycleAdminServiceClient = null;
        lifeCycleManagementClient = null;
        governanceServiceClient = null;
        listMetadataServiceClient = null;
        resourceAdminServiceClient = null;

    }
}
