/*
* Copyright 2004,2005 The Apache Software Foundation.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.test.utils.common.FileManager;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.beans.xsd.LifecycleBean;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.governance.list.stub.ListMetadataServiceRegistryExceptionException;
import org.wso2.carbon.governance.services.stub.AddServicesServiceRegistryExceptionException;
import org.wso2.carbon.registry.activities.stub.RegistryExceptionException;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.common.xsd.ResourceData;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.clients.*;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;
import org.xml.sax.InputSource;

import javax.activation.DataHandler;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.*;

public class LifeCycleDemoteTestCase extends GREGIntegrationBaseTest {

    private String serviceLocation;
    private WSRegistryServiceClient wsRegistryServiceClient;
    private LifeCycleAdminServiceClient lifeCycleAdminServiceClient;
    private LifeCycleManagementClient lifeCycleManagementClient;
    private GovernanceServiceClient governanceServiceClient;
    private ListMetaDataServiceClient listMetadataServiceClient;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private ServiceManager serviceManager;
    private static final String SERVICE_NAME = "IntergalacticService8";
    private static final String LC_NAME = "StateDemoteLC";
    private static final String ACTION_DEMOTE = "Demote";
    private static final String USER = "user";
    private static final String STATE = "state";
    private static final String TARGET_STATE = "targetState";
    private static final String ACTION_TYPE = "type";
    private static final String ACTION_NAME = "name";
    private static final String LC_STATE2 = "Development";
    private static final String LC_STATE1 = "Tested";
    private static final String TYPE = "transition";
    private static final String GOV_PATH = "/_system/governance";
    private String serviceString = "/trunk/services/com/abb/IntergalacticService8";
    private final String absPath = GOV_PATH + serviceString;
    private LifecycleBean lifeCycle;
    private RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
    private String userName1WithoutDomain;

    @BeforeClass (alwaysRun = true)
    public void init () throws Exception {

        super.init(TestUserMode.SUPER_TENANT_USER);
        String sessionCookie = getSessionCookie();
        lifeCycleAdminServiceClient =
                new LifeCycleAdminServiceClient(backendURL,
                        sessionCookie);
        governanceServiceClient =
                new GovernanceServiceClient(backendURL,
                        sessionCookie);
        listMetadataServiceClient =
                new ListMetaDataServiceClient(backendURL,
                        sessionCookie);
        lifeCycleManagementClient =
                new LifeCycleManagementClient(backendURL,
                        sessionCookie);
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(backendURL,
                        sessionCookie);
        wsRegistryServiceClient =
                registryProviderUtil.getWSRegistry(automationContext);
        Registry reg = registryProviderUtil.getGovernanceRegistry(wsRegistryServiceClient,
                automationContext);
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) reg);
        serviceManager = new ServiceManager(reg);
        String userName = automationContext.getContextTenant().getContextUser().getUserName();
        userName1WithoutDomain = userName.substring(0, userName.indexOf('@'));
    }

    @Test (groups = "wso2.greg", description = "Create a service")
    public void testCreateService ()
            throws XMLStreamException, IOException, AddServicesServiceRegistryExceptionException,
            ListMetadataServiceRegistryExceptionException,
            ResourceAdminServiceExceptionException {

        String servicePath = getTestArtifactLocation() + "artifacts" + File.separator + "GREG" +
                File.separator + "services" + File.separator + "intergalacticService8.metadata.xml";
        DataHandler dataHandler = new DataHandler(new URL("file:///" + servicePath));
        String mediaType = "application/vnd.wso2-service+xml";
        String description = "This is a test service";
        resourceAdminServiceClient.addResource(
                "/_system/governance/service2", mediaType, description, dataHandler);
        ResourceData[] data = resourceAdminServiceClient.getResource(absPath);
        assertNotNull(data, "Service not found");
    }

    @Test (groups = "wso2.greg", description = "Create new life cycle", dependsOnMethods = "testCreateService")
    public void testCreateNewLifeCycle ()
            throws LifeCycleManagementServiceExceptionException, IOException, InterruptedException {

        String resourcePath = getTestArtifactLocation() + "artifacts" + File.separator + "GREG" +
                File.separator + "lifecycle" + File.separator + "StateDemoteLifeCycle.xml";
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

    @Test (groups = "wso2.greg", description = "Add lifecycle to a service", dependsOnMethods = "testCreateNewLifeCycle")
    public void testAddLcToService () throws RegistryException, RemoteException,
            CustomLifecyclesChecklistAdminServiceExceptionException,
            ListMetadataServiceRegistryExceptionException,
            ResourceAdminServiceExceptionException {

        wsRegistryServiceClient.associateAspect(absPath, LC_NAME);
        lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(absPath);
        Property[] properties = lifeCycle.getLifecycleProperties();
        boolean lcStatus = false;
        for (Property prop : properties) {
            prop.getKey();
            if (prop.getKey().contains(LC_NAME)) {
                lcStatus = true;
            }
        }
        assertTrue(lcStatus, "LifeCycle not added to service");
    }

    @Test (groups = "wso2.greg", description = "Demote from Tested to Development", dependsOnMethods = "testAddLcToService")
    public void testDemoteLC ()
            throws RemoteException, CustomLifecyclesChecklistAdminServiceExceptionException,
            LifeCycleManagementServiceExceptionException, RegistryExceptionException {

        lifeCycleAdminServiceClient.invokeAspect(absPath, LC_NAME, ACTION_DEMOTE, null);
        lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(absPath);
        for (Property prop : lifeCycle.getLifecycleProperties()) {
            if (("registry.lifecycle." + LC_NAME + ".state").equalsIgnoreCase(prop.getKey())) {
                assertNotNull(prop.getValues(), "State Value Not Found");
                assertTrue(prop.getValues()[0].equalsIgnoreCase(LC_STATE2), "LifeCycle not demoted to Development");
            }
        }

    }

    @Test (groups = "wso2.greg", description = "Verify Audit records", dependsOnMethods = "testDemoteLC")
    public void testVerifyAuditAdmin () throws Exception {

        String auditRecord = absPath.replace("/", "_");
        String auditLocation = "/_system/governance/repository/components/org.wso2.carbon.governance" +
                "/lifecycles/history/";
        String auditPath = auditLocation.concat(auditRecord);
        assertEquals(getAuditRecords(auditPath, 0, USER, false), userName1WithoutDomain, "User is not testuser1");
        assertEquals(getAuditRecords(auditPath, 0, STATE, false), LC_STATE1, "State before transition is not Tested");
        assertEquals(getAuditRecords(auditPath, 0, TARGET_STATE, false), LC_STATE2, "State after transition is not Development");
        assertEquals(getAuditRecords(auditPath, 0, ACTION_TYPE, true), TYPE, "Action is not transition");
        assertEquals(getAuditRecords(auditPath, 0, ACTION_NAME, true), ACTION_DEMOTE, "Transition is not DEMOTE");
    }

    public String getAuditRecords (String auditPath, int node, String item, boolean isChild)
            throws Exception {

        byte[] string = wsRegistryServiceClient.getContent(auditPath);
        String xml = new String(string);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(xml)));
        Element rootElement = document.getDocumentElement();
        rootElement.getFirstChild().getNodeValue();
        NodeList nd = rootElement.getChildNodes();
        assertFalse((node >= nd.getLength()), "Record does not exist");
        if (!isChild) {
            return nd.item(node).getAttributes().getNamedItem(item).getNodeValue();
        } else {
            return nd.item(node).getFirstChild().getAttributes().getNamedItem(item).getNodeValue();
        }
    }

    @AfterClass ()
    public void clear () throws Exception {

        String servicePathToDelete = absPath;
        if (wsRegistryServiceClient.resourceExists(servicePathToDelete)) {
            resourceAdminServiceClient.deleteResource(servicePathToDelete);
        }
//        String schemaPathToDelete = "/_system/governance/trunk/schemas/org/bar/purchasing/purchasing.xsd";
        //      if (wsRegistryServiceClient.resourceExists(schemaPathToDelete)) {
        //        resourceAdminServiceClient.deleteResource(schemaPathToDelete);
        //  }
        //  String wsdlPathToDelete = "/_system/governance/trunk/wsdls/com/foo/IntergalacticService.wsdl";
        //if (wsRegistryServiceClient.resourceExists(wsdlPathToDelete)) {
        //  resourceAdminServiceClient.deleteResource(wsdlPathToDelete);
//        }
        lifeCycleManagementClient.deleteLifeCycle(LC_NAME);
        governanceServiceClient = null;
        wsRegistryServiceClient = null;
        lifeCycleAdminServiceClient = null;
        lifeCycleManagementClient = null;
        listMetadataServiceClient = null;
        resourceAdminServiceClient = null;
    }

}
