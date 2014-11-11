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
package org.wso2.carbon.registry.jira.issues.test;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.list.stub.beans.xsd.ServiceBean;
import org.wso2.carbon.governance.list.stub.beans.xsd.WSDLBean;
import org.wso2.carbon.integration.common.admin.client.LogViewerClient;
import org.wso2.carbon.logging.view.stub.LogViewerLogViewerException;
import org.wso2.carbon.logging.view.stub.types.carbon.LogEvent;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.clients.ListMetaDataServiceClient;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import javax.activation.DataHandler;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class Carbon11120TestCase extends GREGIntegrationBaseTest {

    private ResourceAdminServiceClient resourceAdminServiceClient;
    private Registry governance;
    private ListMetaDataServiceClient listMetaDataServiceClient;
    private WSRegistryServiceClient wsRegistryServiceClient;
    private LogViewerClient logViewerClient;
    private final static String SERVICE_NAME = "echoyuSer1";
    private final static String WSDL_NAME = "echo.wsdl";
    private WSDLBean wsdlBean;
    private ServiceBean serviceBean;
    private ServiceManager serviceManager;


    @BeforeClass(alwaysRun = true)
    public void initializeTests() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String session = getSessionCookie();

        resourceAdminServiceClient =
                new ResourceAdminServiceClient(backendURL, session);
        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        wsRegistryServiceClient =
                registryProviderUtil.getWSRegistry(automationContext);

        governance = registryProviderUtil.getGovernanceRegistry(wsRegistryServiceClient, automationContext);
        listMetaDataServiceClient =
                new ListMetaDataServiceClient(backendURL, session);
        logViewerClient =
                new LogViewerClient(backendURL, session);
        serviceManager = new ServiceManager(governance);

        GovernanceUtils.loadGovernanceArtifacts((UserRegistry)governance, GovernanceUtils.findGovernanceArtifactConfigurations(governance));
    }

    /**
     * Add a wsdl to create a service
     *
     * @throws ResourceAdminServiceExceptionException
     *
     * @throws RemoteException
     * @throws MalformedURLException
     */
    public void addWSDL() throws ResourceAdminServiceExceptionException, RemoteException,
            MalformedURLException, RegistryException {
        Boolean nameExists = false;
        String path1 =
                getTestArtifactLocation() + "artifacts" +
                File.separator + "GREG" + File.separator + "wsdl" + File.separator +
                "echo.wsdl";
        DataHandler dataHandler1 = new DataHandler(new URL("file:///" + path1));
        resourceAdminServiceClient.addWSDL("desc 1", dataHandler1);
        wsdlBean = listMetaDataServiceClient.listWSDLs();
        String[] names1 = wsdlBean.getName();

        for (String name : names1) {
            if (name.equalsIgnoreCase(WSDL_NAME)) {
                nameExists = true;
            }
        }
        assertTrue(nameExists, "WSDL not added");

        boolean serviceStatus = false;

        GovernanceUtils.loadGovernanceArtifacts((UserRegistry)governance, GovernanceUtils.findGovernanceArtifactConfigurations(governance));

        for (Service service : serviceManager.getAllServices()) {
            String name = service.getQName().getLocalPart();
            if (name.contains(SERVICE_NAME)) {
                serviceStatus = true;
            }
        }

        assertTrue(serviceStatus, "Service not found");

    }

    /**
     * @throws RemoteException
     * @throws MalformedURLException
     * @throws ResourceAdminServiceExceptionException
     *
     * @throws GovernanceException
     */
    @Test(groups = "wso2.greg", description = "Save the existing service without modifying and list services ")
    public void testSaveListService() throws RemoteException, MalformedURLException,
            ResourceAdminServiceExceptionException,
            RegistryException,
            LogViewerLogViewerException {

        String ADD_LOG =
                "Failed to get service details. Exception occurred while trying to invoke service method addService";
        String LIST_LOG = "Exception occurred while trying to invoke service method listservices";

        GovernanceUtils.loadGovernanceArtifacts((UserRegistry)governance, GovernanceUtils.findGovernanceArtifactConfigurations(governance));
        addWSDL();

        ServiceManager serviceManager = new ServiceManager(governance);
        Service[] services = serviceManager.getAllServices();
        for (Service s : services) {
            if (s.getQName().getLocalPart().equals(SERVICE_NAME)) {
                serviceManager.updateService(s);
                readLogs(ADD_LOG);
            }
        }
        readLogs(LIST_LOG);
    }

    /**
     * @param errorMessage Error message that needs to be checked
     * @throws RemoteException
     */
    public void readLogs(String errorMessage) throws RemoteException, LogViewerLogViewerException {
        LogEvent[] logEvents = logViewerClient.getRemoteLogs("ERROR", errorMessage, "", "");
        assertNull(logEvents, "Exception thrown");
    }

    /**
     * @throws Exception
     */
    @AfterClass()
    public void clear() throws Exception {
        String wsdlPathToDelete = "";
        for (String wsdlPath : wsdlBean.getPath()) {
            if (wsdlPath.contains(WSDL_NAME)) {
                wsdlPathToDelete = "/_system/governance" + wsdlPath;
            }
        }
        if (wsRegistryServiceClient.resourceExists(wsdlPathToDelete)) {
            resourceAdminServiceClient.deleteResource(wsdlPathToDelete);
        }

        String servicePathToDelete ="";
        for (String servicePath : serviceManager.getAllServicePaths()) {
            if (servicePath.contains(SERVICE_NAME)) {
                servicePathToDelete = "/_system/governance" + servicePath;
            }
        }
        if (wsRegistryServiceClient.resourceExists(servicePathToDelete)) {
            resourceAdminServiceClient.deleteResource(servicePathToDelete);
        }

        resourceAdminServiceClient = null;
        governance = null;
        serviceBean = null;
        listMetaDataServiceClient = null;
        logViewerClient = null;
        wsdlBean = null;
        wsRegistryServiceClient = null;
    }
}
