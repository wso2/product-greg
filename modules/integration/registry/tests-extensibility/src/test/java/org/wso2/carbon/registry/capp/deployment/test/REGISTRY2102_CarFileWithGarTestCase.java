/*
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.capp.deployment.test;

import static org.testng.Assert.assertTrue;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.application.mgt.stub.ApplicationAdminExceptionException;
import org.wso2.carbon.automation.api.clients.application.mgt.ApplicationAdminClient;
import org.wso2.carbon.automation.api.clients.application.mgt.CarbonAppUploaderClient;
import org.wso2.carbon.automation.api.clients.registry.HandlerManagementServiceClient;
mport org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.core.utils.fileutils.FileManager;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.registry.capp.deployment.test.CAppTestUtils;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.handler.stub.ExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import javax.activation.DataHandler;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

/**
 * + * This test case is  to verify the Capp deployment,
 * + * and undeployment with new ArticatCleaupHandler
 * +
 */
public class REGISTRY2102_CarFileWithGarTestCase {

    private WSRegistryServiceClient wsRegistry;
    private CarbonAppUploaderClient cAppUploader;
    private ApplicationAdminClient adminServiceApplicationAdmin;
    private ManageEnvironment environment;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private HandlerManagementServiceClient handlerManagementServiceClient;

    private String cAppName = "GarTestCApp";
    private final String wsdlPath = "/_system/governance/trunk/wsdls/org/wso2/carbon/service/Axis2Service.wsdl";
    private final String wsdlUploadedPath = "/_system/config/gar/Axis2Service.wsdl";
    private final String servicePath = "/_system/governance/trunk/services/org/wso2/carbon/service/Axis2Service";
    private String newHandlerPath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION
            + "artifacts" + File.separator + "GREG" + File.separator
            + "handler" + File.separator + "cleanup-handler.xml";
    private String handlerName = "org.wso2.carbon.registry.extensions.handlers.ArtifactCleanUpHandler";

    @BeforeClass
    public void init() throws Exception {
        int userId = 2;
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        environment = builder.build();
        UserInfo userInfo = UserListCsvReader.getUserInfo(userId);
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(environment.getGreg().getBackEndUrl(),
                        environment.getGreg().getSessionCookie());
        adminServiceApplicationAdmin =
                new ApplicationAdminClient(environment.getGreg().getBackEndUrl(),
                        environment.getGreg().getSessionCookie());

        cAppUploader =
                new CarbonAppUploaderClient(environment.getGreg().getBackEndUrl(),
                        environment.getGreg().getSessionCookie());

        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        wsRegistry = registryProviderUtil.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        handlerManagementServiceClient = new HandlerManagementServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                userInfo.getUserName(), userInfo.getPassword());

    }

    @Test(groups = {"wso2.greg"}, description = "Add new Handler")
    public void addNewHandler() throws IOException, ExceptionException {
        assertTrue(handlerManagementServiceClient.createHandler(FileManager.readFile(newHandlerPath)));
    }

    @Test(description = "Upload CApp having gar file", dependsOnMethods = {"addNewHandler"})
    public void uploadCApplicationWithGar()
            throws MalformedURLException, RemoteException, InterruptedException,
            ApplicationAdminExceptionException {
        String filePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator +
                "GREG" + File.separator + "car" + File.separator + "GarTestCApp_1.0.0.car";
        cAppUploader.uploadCarbonAppArtifact("GarTestCApp_1.0.0.car",
                new DataHandler(new URL("file:///" + filePath)));

        Assert.assertTrue(CAppTestUtils.isCAppDeployed(environment.getGreg().getSessionCookie(), cAppName, adminServiceApplicationAdmin)
                , "Deployed CApplication not in CApp List");
    }

    @Test(description = "Search whether CApp is in /_system/config/repository/applications",
            dependsOnMethods = {"uploadCApplicationWithGar"})
    public void isCApplicationInRegistry() throws RegistryException {
        wsRegistry.get("/_system/config/repository/applications/" + cAppName);
    }

    @Test(description = "Verify Uploaded Resources", dependsOnMethods = {"uploadCApplicationWithGar"})
    public void isResourcesExist() throws RegistryException {

        Assert.assertTrue(wsRegistry.resourceExists(wsdlPath), wsdlPath + " resource does not exist");
        Assert.assertTrue(wsRegistry.resourceExists(servicePath), servicePath + " resource does not exist");
        //        Assert.assertTrue(registry.resourceExists(wsdlUploadedPath), wsdlUploadedPath + " resource does not exist");

    }

    @Test(description = "Delete Carbon Application ", dependsOnMethods = {"isResourcesExist"})
    public void deleteCApplication()
            throws ApplicationAdminExceptionException, RemoteException, InterruptedException,
            RegistryException {
        adminServiceApplicationAdmin.deleteApplication(cAppName);

        Assert.assertTrue(CAppTestUtils.isCAppDeleted(environment.getGreg().getSessionCookie(), cAppName, adminServiceApplicationAdmin)
                , "Deployed CApplication still in CApp List");
    }

    @Test(description = "Verify Resource Deletion", dependsOnMethods = {"deleteCApplication"})
    public void isResourcesDeleted() throws RegistryException {

        Assert.assertFalse(wsRegistry.resourceExists(wsdlPath), "Resource not deleted");
        Assert.assertFalse(wsRegistry.resourceExists(wsdlUploadedPath), "Resource not deleted");
        Assert.assertFalse(wsRegistry.resourceExists(servicePath), "Resource not deleted");
        try {
            // Wait few second to delete the  GarTestCApp file
            Thread.sleep(50000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assert.assertFalse(wsRegistry.resourceExists("/_system/config/repository/applications/" + cAppName),
                "CApp Resource not deleted");

    }

    @Test(groups = {"wso2.greg"}, description = "delete handler", dependsOnMethods = "isResourcesDeleted")
    public void deleteHandler() throws ExceptionException, RemoteException {
        assertTrue(handlerManagementServiceClient.deleteHandler(handlerName));
    }

    @AfterClass
    public void destroy()
            throws ApplicationAdminExceptionException, RemoteException, InterruptedException,
            ResourceAdminServiceExceptionException, RegistryException {
        if (!(CAppTestUtils.isCAppDeleted(environment.getGreg().getSessionCookie(),
                cAppName, adminServiceApplicationAdmin))) {
            adminServiceApplicationAdmin.deleteApplication(cAppName);
        }

        delete("/_system/governance/trunk/services/org/wso2/carbon/service/Axis2Service");
        delete("/_system/governance/trunk/wsdls/org/wso2/carbon/service/Axis2Service.wsdl");
        delete("/_system/governance/trunk/schemas/org/wso2/carbon/service/axis2serviceschema.xsd");
        delete("/_system/governance/trunk/endpoints/_1");

        cAppUploader = null;
        adminServiceApplicationAdmin = null;
        wsRegistry = null;
        environment = null;
        resourceAdminServiceClient = null;
        handlerManagementServiceClient = null;

    }

    public void delete(String destPath)
            throws ResourceAdminServiceExceptionException, RemoteException, RegistryException {
        if (wsRegistry.resourceExists(destPath)) {
            resourceAdminServiceClient.deleteResource(destPath);
        }
    }
}