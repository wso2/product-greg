/*
-*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
-*
-*WSO2 Inc. licenses this file to you under the Apache License,
-*Version 2.0 (the "License"); you may not use this file except
-*in compliance with the License.
-*You may obtain a copy of the License at
-*
-*http://www.apache.org/licenses/LICENSE-2.0
-*
-*Unless required by applicable law or agreed to in writing,
-*software distributed under the License is distributed on an
-*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
-*KIND, either express or implied.  See the License for the
-*specific language governing permissions and limitations
-*under the License.
-*/
package org.wso2.carbon.registry.activity.search;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.governance.api.endpoints.dataobjects.Endpoint;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.registry.activities.stub.RegistryExceptionException;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.clients.ActivityAdminServiceClient;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import javax.activation.DataHandler;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.*;

public class ActivitySearchByPathTestCase extends GREGIntegrationBaseTest{
    private static final Log log = LogFactory.getLog(ActivitySearchByPathTestCase.class);
    private String wsdlPath = "/_system/governance/trunk/wsdls/eu/dataaccess/footballpool/1.0.0/";
    private String resourceName = "sample.wsdl";

    private ResourceAdminServiceClient resourceAdminServiceClient;
    private ActivityAdminServiceClient activityAdminServiceClient;
    private ServiceManager serviceManager;
    private WsdlManager wsdlManager;
    private String sessionCookie;
    private String backEndUrl;
    private String userName;
    private String userNameWithoutDomain;

    @BeforeClass(groups = {"wso2.greg"})
    public void init() throws Exception {
        log.info("Initializing Tests for Activity Search");
        log.debug("Activity Search Tests Initialised");
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        sessionCookie = getSessionCookie();
        backEndUrl = getBackendURL();
        userName = automationContext.getContextTenant().getContextUser().getUserName();
        if (userName.contains("@"))
            userNameWithoutDomain = userName.substring(0, userName.indexOf('@'));
        else
            userNameWithoutDomain = userName;

        log.debug("Running SuccessCase");
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(backEndUrl,
                        sessionCookie);

        activityAdminServiceClient =
                new ActivityAdminServiceClient(backEndUrl,
                        sessionCookie);

        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();

        WSRegistryServiceClient wsRegistry =
                registryProviderUtil.getWSRegistry(automationContext);

        Registry governance = registryProviderUtil.getGovernanceRegistry(wsRegistry, automationContext);

        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
        serviceManager = new ServiceManager(governance);
        wsdlManager = new WsdlManager(governance);
    }


    @Test(groups = {"wso2.greg"})

    public void addResource() throws InterruptedException, MalformedURLException,
            ResourceAdminServiceExceptionException, RemoteException {
        String resource = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" +
                File.separator + "GREG" + File.separator +
                "wsdl" + File.separator + "sample.wsdl";

        resourceAdminServiceClient.addResource(wsdlPath + resourceName,
                "application/wsdl+xml", "test resource",
                new DataHandler(new URL("file:///" + resource)));

        Thread.sleep(20000);
        assertTrue(resourceAdminServiceClient.getResource(wsdlPath + resourceName)[0].getAuthorUserName().
                contains(userNameWithoutDomain));

    }


    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"addResource"})
    public void searchActivityByValidPath() throws RegistryExceptionException, RemoteException {

        assertNotNull(activityAdminServiceClient.getActivities(sessionCookie,
                "", wsdlPath + resourceName, "", "",
                "", 0).getActivity());
    }


    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"searchActivityByValidPath"})
    public void searchActivityByDummyPath() throws RegistryExceptionException, RemoteException {

        assertNull(activityAdminServiceClient.getActivities(sessionCookie,
                "", "/dummy/path", "", "", "", 0).getActivity());
    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"searchActivityByDummyPath"})
    public void searchActivityWithInvalidCharacterPath()
            throws RegistryExceptionException, RemoteException {
        String[] invalidCharacters = {"<a>", "|", "#", "@", "+", " "};
        for (String path : invalidCharacters) {
            assertNull(activityAdminServiceClient.getActivities(sessionCookie,
                    "", path, "", "", "", 0).getActivity());
        }
    }

    @AfterClass(groups = "wso2.greg")
    public void deleteResources() throws ResourceAdminServiceExceptionException, RemoteException, GovernanceException {
        Endpoint[] endpoints = null;
        Wsdl[] wsdls = wsdlManager.getAllWsdls();
        for (Wsdl wsdl : wsdls) {
            if (wsdl.getQName().getLocalPart().equals("sample.wsdl")) {
                endpoints = wsdlManager.getWsdl(wsdl.getId()).getAttachedEndpoints();
                break;
            }
        }
        resourceAdminServiceClient.deleteResource(wsdlPath + "sample.wsdl");
        for (Endpoint path : endpoints) {
            resourceAdminServiceClient.deleteResource("_system/governance/" + path.getPath());
        }
        Service[] services = serviceManager.getAllServices();
        for (Service service : services) {
            if (service.getQName().getLocalPart().equals("Info")) {
                serviceManager.removeService(service.getId());
            }
        }

        resourceAdminServiceClient = null;
        serviceManager = null;
        wsdlManager = null;
        resourceAdminServiceClient = null;
        activityAdminServiceClient = null;
    }
}
