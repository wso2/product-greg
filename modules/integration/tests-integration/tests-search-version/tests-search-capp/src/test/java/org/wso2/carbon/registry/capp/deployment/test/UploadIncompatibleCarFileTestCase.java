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
package org.wso2.carbon.registry.capp.deployment.test;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.application.mgt.stub.ApplicationAdminExceptionException;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.clients.ApplicationAdminClient;
import org.wso2.greg.integration.common.clients.CarbonAppUploaderClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import javax.activation.DataHandler;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

public class UploadIncompatibleCarFileTestCase extends GREGIntegrationBaseTest {

    private WSRegistryServiceClient wsRegistry;
    private CarbonAppUploaderClient cAppUploader;
    private ApplicationAdminClient adminServiceApplicationAdmin;
    private String sessionCookie;
    private String backEndUrl;
    private String userName;
    private String userNameWithoutDomain;

    @BeforeClass
    public void init() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        backEndUrl = getBackendURL();
        sessionCookie = getSessionCookie();
        userName = automationContext.getContextTenant().getContextUser().getUserName();
        if (userName.contains("@"))
            userNameWithoutDomain = userName.substring(0, userName.indexOf('@'));
        else
            userNameWithoutDomain = userName;

        adminServiceApplicationAdmin = new ApplicationAdminClient(backEndUrl,
                                                                  sessionCookie);
        cAppUploader = new CarbonAppUploaderClient(backEndUrl,
                                                   sessionCookie);
        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        wsRegistry = registryProviderUtil.getWSRegistry(automationContext);

    }

    @Test(description = "Upload CApp having Text Resources")
    public void uploadIncompatibleCApplication()
            throws MalformedURLException, RemoteException, InterruptedException, RegistryException,
                   ApplicationAdminExceptionException {
        final String cAppName = "erro_content-1.0.0.car";
        String filePath = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator +
                          "GREG" + File.separator + "car" + File.separator + cAppName;
        cAppUploader.uploadCarbonAppArtifact(cAppName,
                                             new DataHandler(new URL("file:///" + filePath)));
        Thread.sleep(20000);
        Assert.assertFalse(wsRegistry.resourceExists("/_system/config/repository/applications/erro_content"),
                           "Cabon Application Found on /_system/config/repository/applications/erro_content");
        Assert.assertFalse(wsRegistry.resourceExists("/_system/erro_content_temp/temp1.txt"),
                           "Resources deployed");

        String[] appList = adminServiceApplicationAdmin.listAllApplications();
        boolean isFound = false;
        if (appList != null) {
            for (String cApp : appList) {
                if ("erro_content".equalsIgnoreCase(cApp)) {
                    isFound = true;
                    break;
                }
            }
        }
        Assert.assertFalse(isFound, "Invalid Deployed CApplication is in CApp List");

    }

    @Test(description = "Upload CApp having Text Resources", dependsOnMethods = {"uploadIncompatibleCApplication"})
    public void uploadIncompatibleServerRoleCApplication()
            throws MalformedURLException, RemoteException, InterruptedException, RegistryException,
                   ApplicationAdminExceptionException {
        final String cAppName = "serverRole-incorrect_1.0.0.car";
        String filePath = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator +
                          "GREG" + File.separator + "car" + File.separator + cAppName;
        cAppUploader.uploadCarbonAppArtifact(cAppName,
                                             new DataHandler(new URL("file:///" + filePath)));
        Thread.sleep(20000);
        Assert.assertFalse(wsRegistry.resourceExists("/_system/config/repository/applications/incorrectServerRole"),
                           "Cabon Application Found on /_system/config/repository/applications/incorrectServerRole");

        String[] appList = adminServiceApplicationAdmin.listAllApplications();
        boolean isFound = false;
        if (appList != null) {
            for (String cApp : appList) {
                if ("serverRole-incorrect".equalsIgnoreCase(cApp)) {
                    isFound = true;
                    break;
                }
            }
        }
        Assert.assertFalse(isFound, "Invalid Deployed CApplication is in CApp List");
    }

    @Test(description = "Verify Uploaded Resources", dependsOnMethods = {"uploadIncompatibleServerRoleCApplication"})
    public void isResourcesNotExist() throws RegistryException {

        Assert.assertFalse(wsRegistry.resourceExists("/_system/server_role_temp/temp1.txt"));

    }

    @AfterClass
    public void destroy() {
        cAppUploader = null;
        adminServiceApplicationAdmin = null;
        wsRegistry = null;
    }
}
