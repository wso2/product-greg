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

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.application.mgt.stub.ApplicationAdminExceptionException;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.automation.test.utils.common.FileManager;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.handler.stub.ExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.clients.ApplicationAdminClient;
import org.wso2.greg.integration.common.clients.CarbonAppUploaderClient;
import org.wso2.greg.integration.common.clients.HandlerManagementServiceClient;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import javax.activation.DataHandler;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;

/**
 * + * This test case is  to verify the Capp deployment,
 * + * and undeployment with new ArticatCleaupHandler
 * +
 */
public class REGISTRY2102_CarFileWithGarTestCase extends GREGIntegrationBaseTest {

    private WSRegistryServiceClient wsRegistry;
    private CarbonAppUploaderClient cAppUploader;
    private ApplicationAdminClient adminServiceApplicationAdmin;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private HandlerManagementServiceClient handlerManagementServiceClient;

    private String cAppName = "GarTestCApp_1.0.0";
    private final String wsdlPath = "/_system/governance/trunk/wsdls/org/wso2/carbon/service/1.0.0/Axis2Service.wsdl";
    private final String servicePath = "/_system/governance/trunk/services/org/wso2/carbon/service/1.0.0/Axis2Service";
    private String newHandlerPath = FrameworkPathUtil.getSystemResourceLocation()
            + "artifacts" + File.separator + "GREG" + File.separator
            + "handler" + File.separator + "cleanup-handler.xml";
    private String handlerName = "org.wso2.carbon.registry.extensions.handlers.ArtifactCleanUpHandler";

    private String sessionCookie;
    private String backEndUrl;
    private String userName;
    private String userNameWithoutDomain;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        backEndUrl = getBackendURL();
        sessionCookie = getSessionCookie();
        userName = automationContext.getContextTenant().getContextUser().getUserName();

        if (userName.contains("@"))
            userNameWithoutDomain = userName.substring(0, userName.indexOf('@'));
        else
            userNameWithoutDomain = userName;

        resourceAdminServiceClient =
                new ResourceAdminServiceClient(backEndUrl,
                        sessionCookie);
        adminServiceApplicationAdmin =
                new ApplicationAdminClient(backEndUrl,
                        sessionCookie);

        cAppUploader =
                new CarbonAppUploaderClient(backEndUrl,
                        sessionCookie);

        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        wsRegistry = registryProviderUtil.getWSRegistry(automationContext);
        handlerManagementServiceClient = new HandlerManagementServiceClient(backEndUrl,
                sessionCookie);

    }

    @Test(groups = {"wso2.greg"}, description = "Add new Handler")
    public void addNewHandler() throws IOException, ExceptionException, InterruptedException {
        Thread.sleep(60000L);
        assertTrue(handlerManagementServiceClient.createHandler(FileManager.readFile(newHandlerPath)));
    }

    @Test(description = "Upload CApp having gar file", dependsOnMethods = {"addNewHandler"})
    public void uploadCApplicationWithGar()
            throws MalformedURLException, RemoteException, InterruptedException,
            ApplicationAdminExceptionException {
        String filePath = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator +
                "GREG" + File.separator + "car" + File.separator + "GarTestCApp_1.0.0.car";
        cAppUploader.uploadCarbonAppArtifact("GarTestCApp_1.0.0.car",
                new DataHandler(new URL("file:///" + filePath)));

        Assert.assertTrue(CAppTestUtils.isCAppDeployed(sessionCookie, cAppName, adminServiceApplicationAdmin)
                , "Deployed CApplication not in CApp List");
    }

    @Test(description = "Search whether CApp is in /_system/config/repository/applications",
            dependsOnMethods = {"uploadCApplicationWithGar"})
    public void isCApplicationInRegistry() throws RegistryException {
        wsRegistry.get("/_system/config/repository/carbonapps/gar_mapping/Axis2Service");
        wsRegistry.get("/_system/config/repository/carbonapps/path_mapping/" + cAppName);
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

        Assert.assertTrue(CAppTestUtils.isCAppDeleted(sessionCookie, cAppName, adminServiceApplicationAdmin)
                , "Deployed CApplication still in CApp List");
        try {
            // Wait few second to delete the  GarTestCApp file
            Thread.sleep(50000);
        } catch (InterruptedException e) {
            //ignore
        }
    }

    @Test(description = "Verify Resource Deletion", dependsOnMethods = {"deleteCApplication"})
    public void isResourcesDeleted() throws RegistryException {

        Assert.assertFalse(wsRegistry.resourceExists(wsdlPath), "Resource not deleted");
        Assert.assertFalse(wsRegistry.resourceExists(servicePath), "Resource not deleted");

        Assert.assertFalse(wsRegistry.resourceExists("/_system/config/repository/carbonapps/path_mapping/" + cAppName)
                , "CApp Resource not deleted");
        Assert.assertFalse(wsRegistry.resourceExists("/_system/config/repository/carbonapps/gar_mapping/Axis2Service"),
                "CApp Resource not deleted");

    }

    @Test(groups = {"wso2.greg"}, description = "delete handler", dependsOnMethods = "isResourcesDeleted")
    public void deleteHandler() throws ExceptionException, RemoteException {
        assertTrue(handlerManagementServiceClient.deleteHandler(handlerName));
    }

    @AfterClass(alwaysRun = true)
    public void destroy()
            throws ApplicationAdminExceptionException, RemoteException, InterruptedException,
            ResourceAdminServiceExceptionException, RegistryException {
        if (!(CAppTestUtils.isCAppDeleted(sessionCookie,
                cAppName, adminServiceApplicationAdmin))) {
            adminServiceApplicationAdmin.deleteApplication(cAppName);
        }

        delete(wsdlPath);
        delete(servicePath);
        delete("/_system/governance/trunk/services/org/wso2/carbon/service/Axis2Service");
        delete("/_system/governance/trunk/wsdls/org/wso2/carbon/service/Axis2Service.wsdl");
        delete("/_system/governance/trunk/schemas/org/wso2/carbon/service/axis2serviceschema.xsd");
        delete("/_system/governance/trunk/endpoints/_1");

        cAppUploader = null;
        adminServiceApplicationAdmin = null;
        wsRegistry = null;
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