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
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.governance.ListMetaDataServiceClient;
import org.wso2.carbon.automation.api.clients.logging.LogViewerClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
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
import org.wso2.carbon.governance.list.stub.beans.xsd.ServiceBean;
import org.wso2.carbon.governance.list.stub.beans.xsd.WSDLBean;
import org.wso2.carbon.logging.view.stub.types.carbon.LogEvent;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import javax.activation.DataHandler;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class Carbon11120 {

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

    /**
     * @throws LoginAuthenticationExceptionException
     *
     * @throws RemoteException
     * @throws RegistryException
     * @throws ResourceAdminServiceExceptionException
     *
     * @throws MalformedURLException
     */
    @BeforeClass()
    public void initializeTests() throws LoginAuthenticationExceptionException, RemoteException,
                                         RegistryException, ResourceAdminServiceExceptionException,
                                         MalformedURLException {

        int userId = 2;
        UserInfo userInfo = UserListCsvReader.getUserInfo(userId);
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        ManageEnvironment environment = builder.build();

        resourceAdminServiceClient =
                new ResourceAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                               environment.getGreg().getSessionCookie());
        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        wsRegistryServiceClient =
                registryProviderUtil.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);

        governance = registryProviderUtil.getGovernanceRegistry(wsRegistryServiceClient, userId);
        listMetaDataServiceClient =
                new ListMetaDataServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                              environment.getGreg().getSessionCookie());
        logViewerClient =
                new LogViewerClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                    environment.getGreg().getSessionCookie());
        serviceManager = new ServiceManager(governance);
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
                ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
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

        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);

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
            RegistryException {

        String ADD_LOG =
                "Failed to get service details. Exception occurred while trying to invoke service method addService";
        String LIST_LOG = "Exception occurred while trying to invoke service method listservices";

        addWSDL();

        ServiceManager serviceManager = new ServiceManager(governance);
        Service[] services = serviceManager.getAllServices();
        for (Service s : services) {
            if (s.getQName().getLocalPart().equals(SERVICE_NAME)) {
                serviceManager.addService(s);
                readLogs(ADD_LOG);
            }
        }
        readLogs(LIST_LOG);
    }

    /**
     * @param errorMessage Error message that needs to be checked
     * @throws RemoteException
     */
    public void readLogs(String errorMessage) throws RemoteException {
        LogEvent[] logEvents = logViewerClient.getLogs("ERROR", errorMessage, "", "");
        assertNull(logEvents[0], "Exception thrown");
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
