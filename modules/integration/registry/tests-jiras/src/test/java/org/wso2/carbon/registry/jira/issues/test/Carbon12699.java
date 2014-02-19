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
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.governance.list.stub.beans.xsd.WSDLBean;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import javax.activation.DataHandler;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class Carbon12699 {

    private int userId = 2;
    private UserInfo userInfo = UserListCsvReader.getUserInfo(userId);
    private ListMetaDataServiceClient listMetadataServiceClient;
    private LifeCycleManagementClient lifeCycleManagementClient;
    private ResourceAdminServiceClient resourceAdminClient;
    private WSRegistryServiceClient wsRegistryServiceClient;
    private LifeCycleAdminServiceClient lifeCycleAdminServiceClient;
    private WSDLBean wsdl;
    private RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
    private final static String WSDL_NAME = "echo.wsdl";
    private static final String LC_NAME = "ESBLifeCycle2";
    private static final String ACTION_PROMOTE = "Promote";
    private String wsdlPathLeaf;
    private int wsdlCount;
    private ServiceManager serviceManager;

    /**
     * @throws RemoteException
     * @throws LoginAuthenticationExceptionException
     *
     * @throws MalformedURLException
     * @throws RegistryException
     */
    @BeforeClass
    public void init() throws RemoteException, LoginAuthenticationExceptionException,
                              MalformedURLException, RegistryException {
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        ManageEnvironment environment = builder.build();
        listMetadataServiceClient =
                new ListMetaDataServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                              environment.getGreg().getSessionCookie());
        lifeCycleManagementClient =
                new LifeCycleManagementClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                              environment.getGreg().getSessionCookie());
        resourceAdminClient =
                new ResourceAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                               environment.getGreg().getSessionCookie());

        wsRegistryServiceClient =
                registryProviderUtil.getWSRegistry(userId,
                                                   ProductConstant.GREG_SERVER_NAME);

        lifeCycleAdminServiceClient =
                new LifeCycleAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                                environment.getGreg().getSessionCookie());
        serviceManager = new ServiceManager(wsRegistryServiceClient);

    }

    /**
     * @throws RegistryException
     * @throws MalformedURLException
     * @throws RemoteException
     * @throws ResourceAdminServiceExceptionException
     *
     */
    @Test(groups = "wso2.greg", description = "Use addWsdl() method via remote registry")
    public void testAddWsdl() throws RegistryException, MalformedURLException, RemoteException,
                                     ResourceAdminServiceExceptionException {
        wsdl = listMetadataServiceClient.listWSDLs();
        wsdlCount = wsdl.getSize();
        String path1 =
                ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
                File.separator + "GREG" + File.separator + "wsdl" + File.separator +
                "echo.wsdl";
        DataHandler dataHandler1 = new DataHandler(new URL("file:///" + path1));
        resourceAdminClient.addWSDL("desc 1", dataHandler1);

        WSDLBean bean = listMetadataServiceClient.listWSDLs();

        String[] names = bean.getName();
        boolean wsdlStatus = false;
        for (String name : names) {
            if (name.contains(WSDL_NAME)) {
                wsdlStatus = true;
            }
        }

        assertTrue(wsdlStatus, "Wsdl not added");

    }

    /**
     * @throws IOException
     * @throws LifeCycleManagementServiceExceptionException
     *
     * @throws ResourceAdminServiceExceptionException
     *
     * @throws CustomLifecyclesChecklistAdminServiceExceptionException
     *
     * @throws RegistryException
     */
    @Test(groups = "wso2.greg", description = "Use copyExecutor via a Lifecycle", dependsOnMethods = "testAddWsdl")
    public void useCopyExecutor() throws IOException, LifeCycleManagementServiceExceptionException,
                                         ResourceAdminServiceExceptionException,
                                         CustomLifecyclesChecklistAdminServiceExceptionException,
                                         RegistryException {
        String resourcePath =
                ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
                File.separator + "GREG" + File.separator + "lifecycle" +
                File.separator + "copyExecutorLifeCycle2.xml";
        String lifeCycleContent = FileManager.readFile(resourcePath);
        lifeCycleManagementClient.addLifeCycle(lifeCycleContent);
        wsdl = listMetadataServiceClient.listWSDLs();
        wsdlPathLeaf = wsdl.getPath()[0];
        wsRegistryServiceClient.associateAspect("/_system/governance" + wsdlPathLeaf, LC_NAME);

        lifeCycleAdminServiceClient.invokeAspect("/_system/governance" + wsdlPathLeaf, LC_NAME,
                                                 ACTION_PROMOTE, null);
        wsdl = listMetadataServiceClient.listWSDLs();
        assertEquals(wsdl.getSize(), wsdlCount + 2, "WSDL count mismatched");
        boolean isOriginal = false;
        boolean isNew = false;

        for (String wsdlPath : wsdl.getPath()) {
            if (wsdlPath.contains("/trunk")) {
                isOriginal = true;
            }
            if (wsdlPath.contains("/wso2/branches/testing")) {
                isNew = true;
            }
        }
        assertTrue(isOriginal, "original WSDL does not exist");
        assertTrue(isNew, "WSDL not copied");

    }

    /**
     * @throws RemoteException
     * @throws ResourceAdminServiceExceptionException
     *
     * @throws LifeCycleManagementServiceExceptionException
     *
     */
    @AfterClass
    public void clear() throws RemoteException, ResourceAdminServiceExceptionException,
                               LifeCycleManagementServiceExceptionException, RegistryException {

        for (String wsdlPath : wsdl.getPath()) {
            if (wsdlPath.contains("echo.wsdl")) {
                wsdlPathLeaf = wsdlPath;
            }
            if (wsRegistryServiceClient.resourceExists("/_system/governance" + wsdlPathLeaf)) {
                resourceAdminClient.deleteResource("/_system/governance" + wsdlPathLeaf);
            }
        }
        String servicePathToDelete = "";
        for (String servicePath : serviceManager.getAllServicePaths()) {
            if (servicePath.contains("echoyuSer1")) {
                servicePathToDelete = "/_system/governance" + servicePath;
            }
        }
        if (wsRegistryServiceClient.resourceExists(servicePathToDelete)) {
            resourceAdminClient.deleteResource(servicePathToDelete);
        }

        lifeCycleManagementClient.deleteLifeCycle(LC_NAME);

        userInfo = null;
        listMetadataServiceClient = null;
        lifeCycleManagementClient = null;
        resourceAdminClient = null;
        wsRegistryServiceClient = null;
        lifeCycleAdminServiceClient = null;
        wsdl = null;
        registryProviderUtil = null;
    }

}
