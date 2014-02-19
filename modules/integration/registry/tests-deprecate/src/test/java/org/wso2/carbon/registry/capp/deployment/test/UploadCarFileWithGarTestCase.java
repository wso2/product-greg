/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.carbon.automation.api.clients.application.mgt.ApplicationAdminClient;
import org.wso2.carbon.automation.api.clients.application.mgt.CarbonAppUploaderClient;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.registry.capp.deployment.test.Utils.CAppTestUtils;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.search.metadata.test.utils.GregTestUtils;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import javax.activation.DataHandler;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

/**
 * This test case is  to verify the Capp deployment,
 * if that Car file contains a gar file.
 */
public class UploadCarFileWithGarTestCase {
    private String sessionCookie;
    private WSRegistryServiceClient registry;
    private CarbonAppUploaderClient cAppUploader;
    private ApplicationAdminClient adminServiceApplicationAdmin;

    private String cAppName = "GarTestCApp_1.0.0";
    private final String wsdlPath = "/_system/governance/trunk/wsdls/org/wso2/carbon/service/Axis2Service.wsdl";
    private final String wsdlUploadedPath = "/_system/config/gar/Axis2Service.wsdl";
    private final String servicePath = "/_system/governance/trunk/services/org/wso2/carbon/service/Axis2Service";

    @BeforeClass
    public void init() throws Exception {
        ClientConnectionUtil.waitForPort(Integer.parseInt(FrameworkSettings.HTTP_PORT));
        sessionCookie = new LoginLogoutUtil().login();
        final String SERVER_URL = GregTestUtils.getServerUrl();
        registry = GregTestUtils.getRegistry();
        cAppUploader = new CarbonAppUploaderClient(SERVER_URL, sessionCookie);
        adminServiceApplicationAdmin = new ApplicationAdminClient(SERVER_URL, sessionCookie);

    }

    @Test(priority = 1, description = "Upload CApp having gar file")
    public void uploadCApplicationWithGar()
            throws MalformedURLException, RemoteException, InterruptedException,
            ApplicationAdminExceptionException {
        String filePath = GregTestUtils.getResourcePath() + File.separator +
                "car" + File.separator + "GarTestCApp_1.0.0.car";
        cAppUploader.uploadCarbonAppArtifact("GarTestCApp_1.0.0.car",
                new DataHandler(new URL("file:///" + filePath)));

        Assert.assertTrue(CAppTestUtils.isCAppDeployed(sessionCookie, cAppName, adminServiceApplicationAdmin)
                , "Deployed CApplication not in CApp List");
    }

//    @Test(description = "Search whether CApp is in /_system/config/repository/applications",
//            dependsOnMethods = {"uploadCApplicationWithGar"})
//    public void isCApplicationInRegistry() throws RegistryException {
//        registry.get("/_system/config/repository/applications/" + cAppName);
//    }

    @Test(description = "Verify Uploaded Resources", dependsOnMethods = {"uploadCApplicationWithGar"})
    public void isResourcesExist() throws RegistryException {

        Assert.assertTrue(registry.resourceExists(wsdlPath), wsdlPath + " resource does not exist");
        Assert.assertTrue(registry.resourceExists(servicePath), servicePath + " resource does not exist");
//        Assert.assertTrue(registry.resourceExists(wsdlUploadedPath), wsdlUploadedPath + " resource does not exist");

    }

    @Test(description = "Delete Carbon Application ", dependsOnMethods = {"isResourcesExist"})
    public void deleteCApplication()
            throws ApplicationAdminExceptionException, RemoteException, InterruptedException,
            RegistryException {
        adminServiceApplicationAdmin.deleteApplication(cAppName);

        Assert.assertTrue(CAppTestUtils.isCAppDeleted(sessionCookie, cAppName, adminServiceApplicationAdmin)
                , "Deployed CApplication still in CApp List");
    }

    /*
     * This is an invalid use-case. We do not delete the files that was added from the gar.
    @Test(description = "Verify Resource Deletion", dependsOnMethods = {"deleteCApplication"})
    public void isResourcesDeleted() throws RegistryException {

        Assert.assertFalse(registry.resourceExists(wsdlPath), "Resource not deleted");
        Assert.assertFalse(registry.resourceExists(wsdlUploadedPath), "Resource not deleted");
        Assert.assertFalse(registry.resourceExists(servicePath), "Resource not deleted");
        try {
            // Wait few second to delete the  GarTestCApp file
            Thread.sleep(50000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assert.assertFalse(registry.resourceExists("/_system/config/repository/applications/" + cAppName),
                "CApp Resource not deleted");

    }*/

    @AfterClass
    public void destroy() {
        sessionCookie = null;
        cAppUploader = null;
        adminServiceApplicationAdmin = null;
        registry = null;
    }
}
