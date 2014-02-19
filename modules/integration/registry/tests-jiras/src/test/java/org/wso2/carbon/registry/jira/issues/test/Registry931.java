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

package org.wso2.carbon.registry.jira.issues.test;

import org.testng.Assert;
import org.wso2.carbon.automation.api.clients.governance.LifeCycleAdminServiceClient;
import org.wso2.carbon.automation.api.clients.governance.LifeCycleManagementClient;
import org.wso2.carbon.automation.api.clients.logging.LogViewerClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.core.utils.fileutils.FileManager;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.beans.xsd.LifecycleBean;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.services.ArrayOfString;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.logging.view.stub.types.carbon.LogEvent;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import javax.activation.DataHandler;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;

/*
 *     https://wso2.org/jira/browse/REGISTRY-931 reopened
*/
public class Registry931 {

    private ResourceAdminServiceClient resourceAdminServiceClient;
    private RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
    private LifeCycleManagementClient lifeCycleManagementClient;
    private WSRegistryServiceClient wsRegistryServiceClient;
    private LifeCycleAdminServiceClient lifeCycleAdminServiceClient;
    private String servicePath =  "/_system/governance/trunk/services/com/abb/IntergalacticService";
    private final String SERVICE_NAME = "IntergalacticService";
    private final String NAMESPACE = "com/abb";
    private LogViewerClient logViewerClient;

//    @BeforeClass
    public void init() throws Exception {
        int userId = ProductConstant.ADMIN_USER_ID;
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        ManageEnvironment environment = builder.build();
        UserInfo userInfo = UserListCsvReader.getUserInfo(userId);
        logViewerClient = new LogViewerClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                              environment.getGreg().getSessionCookie());
        lifeCycleManagementClient = new LifeCycleManagementClient(environment.getGreg().getBackEndUrl(),
                                                                  environment.getGreg().getSessionCookie());
        wsRegistryServiceClient =
                registryProviderUtil.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);

        resourceAdminServiceClient =
                new ResourceAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                               environment.getGreg().getSessionCookie());
        lifeCycleAdminServiceClient =
                new LifeCycleAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                                environment.getGreg().getSessionCookie());

    }

//    @Test(groups = {"wso2.greg"}, description = "add service")
    public void testAddservice() throws Exception {
        addService();
    }

//    @Test(groups = {"wso2.greg"}, description = "promote service", dependsOnMethods = {"testAddLC"})
    public void testPromote() throws Exception {
        servicePath = promoteService("1.0.0", servicePath, "testing");
        addService();
        Assert.assertTrue(testSearchLogs());
    }

//    @Test(groups = {"wso2.greg"}, description = "add duplicate service", dependsOnMethods = {"testPromote"})
    public void testAddDuplicate() throws Exception {
        addService();
        Assert.assertTrue(testSearchLogs());
    }

//    @Test(groups = {"wso2.greg"}, description = "create lifecycle", dependsOnMethods = "testAddservice")
    public void testCreateLc()
            throws GovernanceException, IOException, LifeCycleManagementServiceExceptionException {

        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
                              File.separator + "GREG" + File.separator + "lifecycle" + File.separator +
                              "MultiplePromoteDemoteLCViewVersionsTrue.xml";
        String lifeCycleContent = FileManager.readFile(resourcePath);
        lifeCycleManagementClient.addLifeCycle(lifeCycleContent);

        String[] lifeClycles = lifeCycleManagementClient.getLifecycleList();
        boolean lccreated = false;
        for (String lc : lifeClycles) {
            if (lc.equals("DiffEnvironmentLC")) {
                lccreated = true;
            }
        }

        Assert.assertTrue(lccreated);


    }


//    @Test(groups = {"wso2.greg"}, description = "add lifecycle to service",
//          dependsOnMethods = {"testCreateLc"})
    public void testAddLC()
            throws RegistryException, CustomLifecyclesChecklistAdminServiceExceptionException,
                   RemoteException {
        wsRegistryServiceClient.associateAspect(servicePath, "DiffEnvironmentLC");

        LifecycleBean lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(servicePath);

        Property[] properties = lifeCycle.getLifecycleProperties();

        boolean lcAdded = false;
        for (Property prop : properties) {
            if (prop.getKey().contains("DiffEnvironmentLC")) {
                lcAdded = true;
            }
        }
        Assert.assertTrue(lcAdded);
    }

    public boolean testSearchLogs() throws RemoteException {
        LogEvent[] logEvents = logViewerClient.getLogs("WARN", "A resource with the given name and namespace exists", "", "");
        return (logEvents[0].getMessage().contains("A resource with the given name and namespace exists"));
    }

    public String promoteService(String version, String currentPath, String promoteStatus)
            throws CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException,
                   RegistryException {
        ArrayOfString[] parameters = new ArrayOfString[2];
        String[] dependencyList = lifeCycleAdminServiceClient.getAllDependencies(currentPath);

        parameters[0] = new ArrayOfString();
        parameters[0].setArray(new String[]{dependencyList[0], version});
        parameters[1] = new ArrayOfString();
        parameters[1].setArray(new String[]{"preserveOriginal", "false"});

        String ACTION_PROMOTE = "Promote";
        String ASPECT_NAME = "DiffEnvironmentLC";
        lifeCycleAdminServiceClient.invokeAspectWithParams(currentPath,
                                                           ASPECT_NAME, ACTION_PROMOTE, null, parameters);

        String newPath = "/_system/governance/branches/" + promoteStatus + "/services/" +
                         NAMESPACE + "/" + version + "/" + SERVICE_NAME;

        Resource service = wsRegistryServiceClient.get(newPath);
        Assert.assertNotNull(service, "Service Not found on registry path " + newPath);
        Assert.assertEquals(service.getPath(), newPath, "Service not in branches/testing. " + newPath);


        return newPath;

    }

    public void addService() throws Exception {
        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
                              File.separator + "GREG" + File.separator + "services" + File.separator +
                              "intergalacticService.xml";

        DataHandler dataHandler = new DataHandler(new URL("file:///" + resourcePath));
        String mediaType = "application/vnd.wso2-service+xml";
        String description = "This is a test service";
        resourceAdminServiceClient.addResource("/_system/governance/service1", mediaType, description, dataHandler);
    }

//    @AfterClass
    public void clean() throws ResourceAdminServiceExceptionException,
                               RemoteException, LifeCycleManagementServiceExceptionException,
                               RegistryException {
        delete(servicePath);
        delete("/_system/governance/trunk");
        lifeCycleManagementClient.deleteLifeCycle("DiffEnvironmentLC");

        logViewerClient = null;
        lifeCycleManagementClient = null;
        lifeCycleAdminServiceClient = null;
        resourceAdminServiceClient = null;
        wsRegistryServiceClient = null;

    }

    public void delete(String destPath)
            throws ResourceAdminServiceExceptionException, RemoteException, RegistryException {
        if (wsRegistryServiceClient.resourceExists(destPath)) {
            resourceAdminServiceClient.deleteResource(destPath);
        }
    }


}
