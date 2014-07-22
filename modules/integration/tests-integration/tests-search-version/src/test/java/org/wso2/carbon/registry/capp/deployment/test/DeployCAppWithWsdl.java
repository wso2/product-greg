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


import org.apache.poi.util.SystemOutLogger;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.application.mgt.stub.ApplicationAdminExceptionException;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils; //
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
import sun.rmi.runtime.Log;

import javax.activation.DataHandler;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class DeployCAppWithWsdl extends GREGIntegrationBaseTest {

    private CarbonAppUploaderClient cAppUploader;
    private ApplicationAdminClient adminServiceApplicationAdmin;
//    private ListMetaDataServiceClient listMetaDataServiceClient;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private String sessionCookie;
    private String backEndUrl;
    private String userName;
    private String userNameWithoutDomain;
    private LogViewerClient logViewerClient;
    private String wsdl_new_1Capp ="wsdl_new_1.0.0";
    private String wsdl_tCapp = "wsdl-t_1.0.0";
    private String serverRole_incorrectCapp ="serverRole-incorrect_1.0.0";
    private Registry governance;

    @BeforeClass
    public void initialize()
            throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        backEndUrl = getBackendURL();
        sessionCookie = getSessionCookie();
        userName = automationContext.getContextTenant().getContextUser().getUserName();
        if (userName.contains("@"))
            userNameWithoutDomain = userName.substring(0, userName.indexOf('@'));
        else
            userNameWithoutDomain = userName;
        
        adminServiceApplicationAdmin =
                new ApplicationAdminClient(backEndUrl,
                                           sessionCookie);
        cAppUploader =
                new CarbonAppUploaderClient(backEndUrl,
                                            sessionCookie);
//        listMetaDataServiceClient =
//                new ListMetaDataServiceClient(backEndUrl,
//                                              sessionCookie);
        logViewerClient =
                new LogViewerClient(backEndUrl,
                                    sessionCookie);
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(backEndUrl,
                                               sessionCookie);
        WSRegistryServiceClient wsRegistry =
                new RegistryProviderUtil().getWSRegistry(automationContext);
        governance = new RegistryProviderUtil().getGovernanceRegistry(wsRegistry, automationContext);
    }

    @Test(groups = "wso2.greg", description = "Upload CApp with wsdls")
    public void deployCAppWithWsdl()
            throws MalformedURLException, RemoteException, ApplicationAdminExceptionException,
                   InterruptedException {

        String resourcePath = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator +
                              "GREG" + File.separator + "car" + File.separator + "wsdl_1.0.0.car";

        cAppUploader.uploadCarbonAppArtifact("wsdl_1.0.0.car", new DataHandler(new URL("file:///" + resourcePath)));

        //artifact.xml  name is  "wsdl_new".
        assertTrue(CAppTestUtils.isCAppDeployed(sessionCookie, wsdl_new_1Capp, adminServiceApplicationAdmin), "Deployed wsdl_1.0.0.car not in CApp List");

        LogEvent[] logEvents = logViewerClient.getLogs("INFO", "Successfully Deployed Carbon Application : wsdl_new", "", "");

        boolean status = false;
        for (LogEvent event : logEvents) {
            if (event.getMessage().contains("Successfully Deployed Carbon Application : wsdl_new")) {
                status = true;
                break;
            }
        }
        assertTrue(status, "Log info message for capp deployment not found");
    }


    @Test(groups = "wso2.greg", description = "Upload CApp with incorrect ServerRole(ESB)",
          dependsOnMethods = "deployCAppWithWsdl")
    public void cAppWithIncorrectServerRole() throws MalformedURLException, RemoteException,
                                                     ApplicationAdminExceptionException,
                                                     InterruptedException {

        String resourcePath = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator +
                              "GREG" + File.separator + "car" + File.separator + "serverRole-incorrect_1.0.0.car";

        cAppUploader.uploadCarbonAppArtifact("serverRole-incorrect_1.0.0.car", new DataHandler(new URL("file:///" + resourcePath)));

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


    @Test(groups = "wso2.greg", description = "Upload CApp as Service",
          dependsOnMethods = "cAppWithIncorrectServerRole")
    public void deployNewCApplication() throws MalformedURLException, RemoteException,
            ApplicationAdminExceptionException,
            InterruptedException, RegistryException,
            ResourceAdminServiceExceptionException {

        String resourcePath = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator +
                              "GREG" + File.separator + "car" + File.separator + "wsdl-t_1.0.0.car";

        cAppUploader.uploadCarbonAppArtifact("wsdl-t_1.0.0.car", new DataHandler(new URL("file:///" + resourcePath)));

        assertTrue(CAppTestUtils.isCAppDeployed(sessionCookie, wsdl_tCapp,
                                                adminServiceApplicationAdmin), "Deployed wsdl-t_1.0.0.car not in CApp List");

        LogEvent[] logEvents = logViewerClient.getLogs("INFO", "Successfully Deployed Carbon Application : wsdl-t", "", "");

        boolean status = false;
        for (LogEvent event : logEvents) {
            if (event.getMessage().contains("Successfully Deployed Carbon Application : wsdl-t")) {
                status = true;
                break;
            }
        }
        assertTrue(status, "Log info message for cApp deployment not found");

        boolean isService = false;

        //
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry)governance);
        GenericArtifactManager manager = new GenericArtifactManager(governance, "service");

        GenericArtifact[] serviceArtifacts = manager.getAllGenericArtifacts();

        for (GenericArtifact genericArtifact : serviceArtifacts) {
            String name = genericArtifact.getQName().getLocalPart();
            if (name.equalsIgnoreCase("WeatherForecastService")) {
                isService = true;
                break;
            }
        }
        assertTrue(isService);

    }

    @Test(groups = "wso2.greg", description = "Delete Carbon Applications ", dependsOnMethods = "deployNewCApplication")
    public void deleteAllCApplications()
            throws ApplicationAdminExceptionException, RemoteException, InterruptedException,
                   RegistryException {

        adminServiceApplicationAdmin.deleteApplication(wsdl_new_1Capp);
        adminServiceApplicationAdmin.deleteApplication(wsdl_tCapp);

        Assert.assertTrue(CAppTestUtils.isCAppDeleted(sessionCookie, wsdl_new_1Capp, adminServiceApplicationAdmin)
                , "Deployed wsdl_new still in CApp List");
        Assert.assertTrue(CAppTestUtils.isCAppDeleted(sessionCookie, wsdl_tCapp, adminServiceApplicationAdmin)
                , "Deployed wsdl-t still in CApp List");
    }

    @Test(groups = "wso2.greg", description = "Delete artifacts ", dependsOnMethods = "deleteAllCApplications")
    public void deleteArtifacts()
            throws ApplicationAdminExceptionException, InterruptedException, RemoteException,
                   ResourceAdminServiceExceptionException {

        resourceAdminServiceClient.deleteResource
                ("/_system/governance/trunk/services/net/restfulwebservices/www/servicecontracts/_2008/_01" +
                 "/1.0.0-SNAPSHOT/WeatherForecastService");
        resourceAdminServiceClient.deleteResource
                ("/_system/governance/trunk/endpoints/net/restfulwebservices/www/wcf/ep-WeatherForecastService-svc");
        resourceAdminServiceClient.deleteResource
                ("/_system/governance/trunk/schemas/com/microsoft/schemas/_2003");
        resourceAdminServiceClient.deleteResource
                ("/_system/governance/trunk/schemas/net/restfulwebservices/www");
        resourceAdminServiceClient.deleteResource
                ("/_system/governance/trunk/schemas/faultcontracts/gotlservices/_2008");

    }

    @AfterClass
    public void cleanupResources() {
        resourceAdminServiceClient = null;
        adminServiceApplicationAdmin = null;
//        listMetaDataServiceClient = null;
        logViewerClient = null;
    }
}
