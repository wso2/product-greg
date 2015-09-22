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
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.logging.view.stub.LogViewerLogViewerException;
import org.wso2.carbon.logging.view.stub.types.carbon.LogEvent;
import org.wso2.carbon.registry.capp.deployment.test.utils.CAppTestUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.clients.ApplicationAdminClient;
import org.wso2.greg.integration.common.clients.CarbonAppUploaderClient;
import org.wso2.greg.integration.common.clients.LogViewerClient;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import javax.activation.DataHandler;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * This class hold test cases for deploying a carbon application with a wsdl.
 */
public class DeployCAppWithWsdl extends GREGIntegrationBaseTest {

    private CarbonAppUploaderClient cAppUploader;
    private ApplicationAdminClient adminServiceApplicationAdmin;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private String sessionCookie;
    private Registry governance;
    private LogViewerClient logViewerClient;
    private String wsdl_new_1Capp = "wsdl_new_1.0.0";
    private String wsdl_tCapp = "wsdl-t_1.0.0";

    /**
     * Method used to initialize deploying a carbon application with a wsdl.
     *
     * @throws Exception
     */
    @BeforeClass
    public void initialize() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String backEndUrl = getBackendURL();
        sessionCookie = getSessionCookie();
        adminServiceApplicationAdmin = new ApplicationAdminClient(backEndUrl, sessionCookie);
        cAppUploader = new CarbonAppUploaderClient(backEndUrl, sessionCookie);
        logViewerClient = new LogViewerClient(backEndUrl, sessionCookie);
        resourceAdminServiceClient = new ResourceAdminServiceClient(backEndUrl, sessionCookie);
        WSRegistryServiceClient wsRegistry = new RegistryProviderUtil().getWSRegistry(automationContext);
        governance = new RegistryProviderUtil().getGovernanceRegistry(wsRegistry, automationContext);
    }

    /**
     * Test case for testing a carbon application deployment with a wsdl.
     *
     * @throws MalformedURLException
     * @throws RemoteException
     * @throws ApplicationAdminExceptionException
     * @throws InterruptedException
     * @throws LogViewerLogViewerException
     */
    @Test(groups = "wso2.greg", description = "Upload CApp with wsdls")
    public void deployCAppWithWsdl() throws MalformedURLException, RemoteException, ApplicationAdminExceptionException,
            InterruptedException, LogViewerLogViewerException {
        String resourcePath = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator +
                "GREG" + File.separator + "car" + File.separator + "wsdl_1.0.0.car";
        cAppUploader.uploadCarbonAppArtifact("wsdl_1.0.0.car", new DataHandler(new URL("file:///" + resourcePath)));
        //artifact.xml  name is  "wsdl_new".
        assertTrue(CAppTestUtils.isCAppDeployed(sessionCookie, wsdl_new_1Capp, adminServiceApplicationAdmin),
                "Deployed wsdl_1.0.0.car not in CApp List");
        LogEvent[] logEvents = logViewerClient.getLogs("INFO", "Successfully Deployed Carbon Application : wsdl_new",
                "", "");

        boolean status = false;
        for (LogEvent event : logEvents) {
            if (event.getMessage().contains("Successfully Deployed Carbon Application : wsdl_new")) {
                status = true;
                break;
            }
        }
        assertTrue(status, "Log info message for capp deployment not found");
    }

    /**
     * Test case used to test a carbon application deployment failures with an incorrect server role.
     *
     * @throws MalformedURLException
     * @throws RemoteException
     * @throws ApplicationAdminExceptionException
     * @throws InterruptedException
     * @throws LogViewerLogViewerException
     */
    @Test(groups = "wso2.greg", description = "Upload CApp with incorrect ServerRole(ESB)",
            dependsOnMethods = "deployCAppWithWsdl")
    public void cAppWithIncorrectServerRole() throws MalformedURLException, RemoteException,
            ApplicationAdminExceptionException, InterruptedException, LogViewerLogViewerException {

        String resourcePath = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator +
                "GREG" + File.separator + "car" + File.separator + "serverRole-incorrect_1.0.0.car";
        cAppUploader.uploadCarbonAppArtifact("serverRole-incorrect_1.0.0.car",
                new DataHandler(new URL("file:///" + resourcePath)));
        String serverRole_incorrectCapp = "serverRole-incorrect_1.0.0";
        assertFalse(CAppTestUtils.isCAppDeployed(sessionCookie, serverRole_incorrectCapp,
                adminServiceApplicationAdmin), "Deployed serverRole-incorrect_1.0.0.car not in CApp List");
        LogEvent[] logEvents = logViewerClient.getLogs("WARN", "No artifacts found to be deployed in this server." +
                " Ignoring Carbon Application : serverRole-incorrect_1.0.0.car", "", "");

        boolean status = false;
        for (LogEvent event : logEvents) {
            if (event.getMessage().contains("No artifacts found to be deployed in this server. " +
                    "Ignoring Carbon Application : serverRole-incorrect_1.0.0.car")) {
                status = true;
                break;
            }
        }
        assertTrue(status, "Log info message for capp deployment not found");
    }

    /**
     * Test case used to test deploying a new carbon application which includes a wsdl.
     *
     * @throws MalformedURLException
     * @throws RemoteException
     * @throws InterruptedException
     * @throws ApplicationAdminExceptionException
     * @throws RegistryException
     * @throws ResourceAdminServiceExceptionException
     * @throws LogViewerLogViewerException
     */
    @Test(groups = "wso2.greg", description = "Upload CApp as Service",
            dependsOnMethods = "cAppWithIncorrectServerRole")
    public void deployNewCApplication() throws MalformedURLException, RemoteException, InterruptedException,
            ApplicationAdminExceptionException, RegistryException, ResourceAdminServiceExceptionException,
            LogViewerLogViewerException {
        String resourcePath = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator +
                "GREG" + File.separator + "car" + File.separator + "wsdl-t_1.0.0.car";
        cAppUploader.uploadCarbonAppArtifact("wsdl-t_1.0.0.car", new DataHandler(new URL("file:///" + resourcePath)));
        assertTrue(CAppTestUtils.isCAppDeployed(sessionCookie, wsdl_tCapp,
                adminServiceApplicationAdmin), "Deployed wsdl-t_1.0.0.car not in CApp List");
        LogEvent[] logEvents = logViewerClient.getLogs("INFO", "Successfully Deployed Carbon Application : wsdl-t", "",
                "");

        boolean status = false;
        for (LogEvent event : logEvents) {
            if (event.getMessage().contains("Successfully Deployed Carbon Application : wsdl-t")) {
                status = true;
                break;
            }
        }
        assertTrue(status, "Log info message for cApp deployment not found");

        boolean isService = false;
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
        GenericArtifactManager manager = new GenericArtifactManager(governance, "service");
        GenericArtifact[] serviceArtifacts = manager.getAllGenericArtifacts();

        for (GenericArtifact genericArtifact : serviceArtifacts) {
            String name = genericArtifact.getQName().getLocalPart();
            if (name.equalsIgnoreCase("GlobalWeather")) {
                isService = true;
                break;
            }
        }
        assertTrue(isService);
    }

    /**
     * Test case used to delete all deployed carbon applications.
     *
     * @throws ApplicationAdminExceptionException
     * @throws RemoteException
     * @throws InterruptedException
     * @throws RegistryException
     */
    @Test(groups = "wso2.greg", description = "Delete Carbon Applications ", dependsOnMethods = "deployNewCApplication")
    public void deleteAllCApplications() throws ApplicationAdminExceptionException, RemoteException,
            InterruptedException, RegistryException {
        adminServiceApplicationAdmin.deleteApplication(wsdl_new_1Capp);
        adminServiceApplicationAdmin.deleteApplication(wsdl_tCapp);
        Assert.assertTrue(CAppTestUtils.isCAppDeleted(sessionCookie, wsdl_new_1Capp, adminServiceApplicationAdmin)
                , "Deployed wsdl_new still in CApp List");
        Assert.assertTrue(CAppTestUtils.isCAppDeleted(sessionCookie, wsdl_tCapp, adminServiceApplicationAdmin)
                , "Deployed wsdl-t still in CApp List");
    }

    /**
     * Test case used to delete the artifacts which adds with the carbon application.
     * wsdl service.
     *
     * @throws ApplicationAdminExceptionException
     * @throws InterruptedException
     * @throws RemoteException
     * @throws ResourceAdminServiceExceptionException
     */
    @Test(groups = "wso2.greg", description = "Delete artifacts ", dependsOnMethods = "deleteAllCApplications")
    public void deleteArtifacts() throws ApplicationAdminExceptionException, InterruptedException, RemoteException,
            ResourceAdminServiceExceptionException {
        resourceAdminServiceClient.deleteResource
                ("/_system/governance/trunk/services/net/webservicex/www/1.0.0/GlobalWeather");
    }

    /**
     * Test case for the cleaning process after executing deploying a carbon application with a wsdl test cases.
     */
    @AfterClass
    public void cleanupResources() {
        resourceAdminServiceClient = null;
        adminServiceApplicationAdmin = null;
        logViewerClient = null;
    }
}
