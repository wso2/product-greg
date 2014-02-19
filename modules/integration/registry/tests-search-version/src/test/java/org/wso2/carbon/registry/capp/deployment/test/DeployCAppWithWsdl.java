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
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.application.mgt.ApplicationAdminClient;
import org.wso2.carbon.automation.api.clients.application.mgt.CarbonAppUploaderClient;
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
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.list.stub.beans.xsd.ServiceBean;
import org.wso2.carbon.logging.view.stub.types.carbon.LogEvent;
import org.wso2.carbon.registry.capp.deployment.test.utils.CAppTestUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import javax.activation.DataHandler;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.*;

public class DeployCAppWithWsdl {

    int userId = 2;
    private CarbonAppUploaderClient cAppUploader;
    private UserInfo userInfo = UserListCsvReader.getUserInfo(userId);
    private ApplicationAdminClient adminServiceApplicationAdmin;
//    private ListMetaDataServiceClient listMetaDataServiceClient;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private String sessionCookie;
    private LogViewerClient logViewerClient;
    private String wsdl_new_1Capp ="wsdl_new_1.0.0";
    private String wsdl_tCapp = "wsdl-t_1.0.0";
    private String serverRole_incorrectCapp ="serverRole-incorrect_1.0.0";
    private Registry governance;

    @BeforeClass
    public void initialize()
            throws LoginAuthenticationExceptionException, RemoteException, RegistryException {

        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        ManageEnvironment environment = builder.build();
        sessionCookie = environment.getGreg().getSessionCookie();
        adminServiceApplicationAdmin =
                new ApplicationAdminClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                           environment.getGreg().getSessionCookie());
        cAppUploader =
                new CarbonAppUploaderClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                            environment.getGreg().getSessionCookie());
//        listMetaDataServiceClient =
//                new ListMetaDataServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
//                                              environment.getGreg().getSessionCookie());
        logViewerClient =
                new LogViewerClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                    environment.getGreg().getSessionCookie());
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                               environment.getGreg().getSessionCookie());
        WSRegistryServiceClient wsRegistry =
                new RegistryProviderUtil().getWSRegistry(userId,
                        ProductConstant.GREG_SERVER_NAME);
        governance = new RegistryProviderUtil().getGovernanceRegistry(wsRegistry, userId);
    }

    @Test(groups = "wso2.greg", description = "Upload CApp with wsdls")
    public void deployCAppWithWsdl()
            throws MalformedURLException, RemoteException, ApplicationAdminExceptionException,
                   InterruptedException {

        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator +
                              "GREG" + File.separator + "car" + File.separator + "wsdl_1.0.0.car";

        cAppUploader.uploadCarbonAppArtifact("wsdl_1.0.0.car", new DataHandler(new URL("file:///" + resourcePath)));

        //artifact.xml  name is  "wsdl_new".
        assertTrue(CAppTestUtils.isCAppDeployed(sessionCookie, wsdl_new_1Capp,
                                                adminServiceApplicationAdmin), "Deployed wsdl_1.0.0.car not in CApp List");

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

        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator +
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

        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator +
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
                 "/WeatherForecastService");
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
