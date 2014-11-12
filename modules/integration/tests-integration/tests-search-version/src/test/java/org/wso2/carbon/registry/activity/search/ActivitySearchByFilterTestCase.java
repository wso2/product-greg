/*
Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.

WSO2 Inc. licenses this file to you under the Apache License,
Version 2.0 (the "License"); you may not use this file except
in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
*/


package org.wso2.carbon.registry.activity.search;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.governance.api.endpoints.dataobjects.Endpoint;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.registry.activities.stub.RegistryExceptionException;
import org.wso2.carbon.registry.activity.search.utils.ActivitySearchUtil;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.relations.stub.AddAssociationRegistryExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.beans.xsd.VersionPath;
import org.wso2.carbon.registry.resource.stub.beans.xsd.VersionsBean;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.clients.*;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import javax.activation.DataHandler;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class ActivitySearchByFilterTestCase extends GREGIntegrationBaseTest{

    private static final Log log = LogFactory.getLog(ActivitySearchByFilterTestCase.class);

    private String wsdlPath = "/_system/governance/trunk/wsdls/eu/dataaccess/footballpool/1.0.0/";
    private String resourceName = "sample.wsdl";
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private ActivityAdminServiceClient activityAdminServiceClient;
    private InfoServiceAdminClient infoServiceAdminClient;
    private RelationAdminServiceClient relationalServiceClient;

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

        infoServiceAdminClient =
                new InfoServiceAdminClient(backEndUrl,
                                           sessionCookie);

        relationalServiceClient =
                new RelationAdminServiceClient(backEndUrl,
                        sessionCookie);

        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();

        WSRegistryServiceClient wsRegistry =
                registryProviderUtil.getWSRegistry(automationContext);

        Registry governance = registryProviderUtil.getGovernanceRegistry(wsRegistry, automationContext);

        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
        serviceManager = new ServiceManager(governance);
        wsdlManager = new WsdlManager(governance);

    }

//

    @Test(groups = {"wso2.greg"})
    public void addResource() throws InterruptedException, MalformedURLException,
            ResourceAdminServiceExceptionException, RemoteException {
        String resource = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" +
                File.separator + "GREG" + File.separator +
                "wsdl" + File.separator + "sample.wsdl";

        resourceAdminServiceClient.addResource(wsdlPath + resourceName,
                "application/wsdl+xml", "test resource",
                new DataHandler(new URL("file:///" + resource)));


        // wait for sometime until the resource has been added. The activity logs are written
        // every 10 seconds, so you'll need to wait until that's done.
        Thread.sleep(10000);
        assertTrue(resourceAdminServiceClient.getResource(wsdlPath + resourceName)[0].
                getAuthorUserName().contains(userNameWithoutDomain));


    }


    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"addResource"})
    public void activitySearchFilterByAdd() throws InterruptedException, MalformedURLException,
            ResourceAdminServiceExceptionException,
            RemoteException, RegistryExceptionException {
        assertNotNull(activityAdminServiceClient.getActivities(sessionCookie, "", "", "", "",
                ActivitySearchUtil.ADD_RESOURCE, 0).getActivity());
    }

    @Test(groups = "wso2.greg", description = "add property", dependsOnMethods = {"activitySearchFilterByAdd"})
    public void testPropertyAddition() throws Exception {
        PropertiesAdminServiceClient propertiesAdminServiceClient =
                new PropertiesAdminServiceClient(backendURL,
                                                 sessionCookie);
        propertiesAdminServiceClient.setProperty(wsdlPath + resourceName, "Author", "TestValuse");

    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"testPropertyAddition"})
    public void activitySearchFilterByUpdate() throws InterruptedException, MalformedURLException,
            ResourceAdminServiceExceptionException,
            RemoteException, RegistryExceptionException {
        assertNotNull(activityAdminServiceClient.getActivities(sessionCookie, "", "", "", "",
                ActivitySearchUtil.UPDATE_RESOURCE, 0).getActivity());


    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"activitySearchFilterByUpdate"})
    public void activitySearchFilterByDelete() throws InterruptedException, MalformedURLException,
            ResourceAdminServiceExceptionException,
            RemoteException, RegistryExceptionException {
        assertNotNull(activityAdminServiceClient.getActivities(sessionCookie, "", "", "", "",
                ActivitySearchUtil.DELETE_RESOURCE, 0).getActivity());
    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = "activitySearchFilterByDelete")
    public void addResourceAgain() throws InterruptedException, MalformedURLException,
            ResourceAdminServiceExceptionException, RemoteException {
        String resource = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" +
                File.separator + "GREG" + File.separator +
                "wsdl" + File.separator + "sample.wsdl";

        resourceAdminServiceClient.addResource(wsdlPath + resourceName,
                "application/wsdl+xml", "test resource",
                new DataHandler(new URL("file:///" + resource)));


    }


    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"addResourceAgain"})
    public void restoreResource() throws InterruptedException, MalformedURLException,
            ResourceAdminServiceExceptionException, RemoteException {
        resourceAdminServiceClient.createVersion(wsdlPath + resourceName);
        VersionsBean bean = resourceAdminServiceClient.getVersionsBean(wsdlPath + resourceName);
        for (VersionPath path : bean.getVersionPaths()) {
            if (path.isActiveResourcePathSpecified()) {
                resourceAdminServiceClient.restoreVersion(path.getCompleteVersionPath());
            }
        }

    }


    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"restoreResource"})
    public void activitySearchFilterByRestores() throws InterruptedException, MalformedURLException,
            ResourceAdminServiceExceptionException,
            RemoteException,
            RegistryExceptionException {
        assertNotNull(activityAdminServiceClient.getActivities(sessionCookie, "", "", "", "",
                ActivitySearchUtil.RESTORE_RESOURCES, 0).getActivity());
    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"activitySearchFilterByRestores"})
    public void addComment() throws RegistryException, AxisFault {
        infoServiceAdminClient.addComment("this is comment", "", sessionCookie);

    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"addComment"})
    public void activitySearchFilterByComment() throws InterruptedException, MalformedURLException,
            ResourceAdminServiceExceptionException,
            RemoteException, RegistryExceptionException {
        assertNotNull(activityAdminServiceClient.getActivities(sessionCookie, "", "", "", "",
                ActivitySearchUtil.COMMENTS, 0).getActivity());
    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = "activitySearchFilterByComment")
    public void addTag() throws RegistryException, AxisFault {
        infoServiceAdminClient.addTag("this is tag", wsdlPath, sessionCookie);
    }


    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"addTag"})
    public void activitySearchFilterByTagging() throws InterruptedException, MalformedURLException,
            ResourceAdminServiceExceptionException,
            RemoteException, RegistryExceptionException {
        assertNotNull(activityAdminServiceClient.getActivities(sessionCookie, "", "", "", "",
                ActivitySearchUtil.TAGGING, 0).getActivity());

    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = "activitySearchFilterByTagging")
    public void rateResource() throws RegistryException, AxisFault,
            org.wso2.carbon.registry.info.stub.RegistryExceptionException {
        infoServiceAdminClient.rateResource("1", wsdlPath + resourceName, sessionCookie);
    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"rateResource"})
    public void activitySearchFilterByRating() throws InterruptedException, MalformedURLException,
            ResourceAdminServiceExceptionException,
            RemoteException, RegistryExceptionException {
        assertNotNull(activityAdminServiceClient.getActivities(sessionCookie, "", "", "", "",
                ActivitySearchUtil.RATINGS, 0).getActivity());
    }


    @Test(groups = {"wso2.greg"}, dependsOnMethods = "activitySearchFilterByRating")
    public void createSymbolicLink() throws RegistryException, RemoteException,
            org.wso2.carbon.registry.info.stub.RegistryExceptionException,
            ResourceAdminServiceExceptionException {

        resourceAdminServiceClient.addSymbolicLink("/_system/governance/trunk/wsdls/eu/dataaccess/",
                "SymbolicName", "/_system/governance/trunk/wsdls/eu/dataaccess/footballpool/1.0.0/"+
                resourceName);
    }




    @Test(groups = {"wso2.greg"}, dependsOnMethods = "createSymbolicLink")
    public void activitySearchFilterBySymbolLink()
            throws InterruptedException, MalformedURLException,
            ResourceAdminServiceExceptionException, RemoteException,
            RegistryExceptionException {
        assertNotNull(activityAdminServiceClient.getActivities(sessionCookie, "", "", "", "",
                ActivitySearchUtil.CREATE_SYMBOLIC_LINK, 0).getActivity());

    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"activitySearchFilterBySymbolLink"})
    public void deleteLink() throws RegistryException, RemoteException,
            org.wso2.carbon.registry.info.stub.RegistryExceptionException,
            ResourceAdminServiceExceptionException {
        resourceAdminServiceClient.deleteResource(wsdlPath + resourceName);
    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"deleteLink"})
    public void activitySearchFilterByRemoveLink()
            throws InterruptedException, MalformedURLException,
            ResourceAdminServiceExceptionException, RemoteException,
            RegistryExceptionException {
        assertNotNull(activityAdminServiceClient.getActivities(sessionCookie, "", "", "", "",
                ActivitySearchUtil.REMOVE_LINK, 0).getActivity());
    }

    @Test(groups = {"wso2.greg"},dependsOnMethods = "activitySearchFilterByRemoveLink")
    public void addResourceForAssociation() throws InterruptedException, MalformedURLException,
            ResourceAdminServiceExceptionException,
            RemoteException {
        String resource = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" +
                File.separator + "GREG" + File.separator +
                "wsdl" + File.separator + "info.wsdl";

        resourceAdminServiceClient.addResource(wsdlPath + "info.wsdl",
                "application/wsdl+xml", "test resource",
                new DataHandler(new URL("file:///" + resource)));
    }


    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"addResourceForAssociation"})
    public void addAssociation() throws InterruptedException, MalformedURLException,
            ResourceAdminServiceExceptionException, RemoteException,
            AddAssociationRegistryExceptionException {
        relationalServiceClient.addAssociation(wsdlPath + resourceName, "depends", "/_system/governance/", "add");
    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"addAssociation"})
    public void activitySearchFilterByAddAssociation()
            throws InterruptedException, MalformedURLException,
            ResourceAdminServiceExceptionException, RemoteException,
            RegistryExceptionException {
        assertNotNull(activityAdminServiceClient.getActivities(sessionCookie, "", "", "", "",
                ActivitySearchUtil.ADD_ASSOCIATION, 0).getActivity());
    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = "activitySearchFilterByAddAssociation")
    public void removeAssociation() throws InterruptedException, MalformedURLException,
            ResourceAdminServiceExceptionException, RemoteException,
            AddAssociationRegistryExceptionException {
        relationalServiceClient.addAssociation(wsdlPath + resourceName, "depends", "/_system/governance/", "remove");


    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"removeAssociation"})
    public void activitySearchFilterByRemoveAssociation()
            throws InterruptedException, MalformedURLException,
            ResourceAdminServiceExceptionException, RemoteException,
            RegistryExceptionException {
        assertNotNull(activityAdminServiceClient.getActivities(sessionCookie, "", "", "", "",
                ActivitySearchUtil.REMOVE_ASSOCIATION, 0).getActivity());
    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"activitySearchFilterByRemoveAssociation"})
    public void activitySearchFilterByAssociationAspect()
            throws InterruptedException, MalformedURLException,
            ResourceAdminServiceExceptionException, RemoteException,
            RegistryExceptionException {
        assertNotNull(activityAdminServiceClient.getActivities(sessionCookie, "", "", "", "",
                ActivitySearchUtil.ASSOCIATE_ASPECT, 0).getActivity());
    }


    @AfterClass(groups = "wso2.greg")
    public void deleteResources() throws ResourceAdminServiceExceptionException, RemoteException, RegistryException, org.wso2.carbon.registry.info.stub.RegistryExceptionException, AddAssociationRegistryExceptionException {
        Endpoint[] endpoints = null;
        Wsdl[] wsdls = wsdlManager.getAllWsdls();
        for (Wsdl wsdl : wsdls) {
            if (wsdl.getQName().getLocalPart().equals("info.wsdl")) {
                endpoints = wsdlManager.getWsdl(wsdl.getId()).getAttachedEndpoints();
            }
        }
        resourceAdminServiceClient.deleteResource(wsdlPath + "info.wsdl");
        for (Endpoint path : endpoints) {
            resourceAdminServiceClient.deleteResource("_system/governance/" + path.getPath());
        }
        Service[] services = serviceManager.getAllServices();
        for (Service service : services) {
            if (service.getQName().getLocalPart().equals("Info")) {
                serviceManager.removeService(service.getId());
            }
        }
        infoServiceAdminClient.removeTag("this is tag", wsdlPath, sessionCookie);
        resourceAdminServiceClient = null;
        relationalServiceClient = null;
        infoServiceAdminClient = null;
        serviceManager = null;
        wsdlManager = null;




    }
}




