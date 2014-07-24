/*
* Copyright 2004,2005 The Apache Software Foundation.
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

package org.wso2.carbon.registry.lifecycle.test;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.test.utils.common.FileManager;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.beans.xsd.LifecycleBean;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.governance.list.stub.ListMetadataServiceRegistryExceptionException;
import org.wso2.carbon.governance.services.stub.AddServicesServiceRegistryExceptionException;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.lifecycle.test.utils.LifeCycleUtils;
import org.wso2.carbon.registry.relations.stub.AddAssociationRegistryExceptionException;
import org.wso2.carbon.registry.relations.stub.beans.xsd.AssociationBean;
import org.wso2.carbon.registry.relations.stub.beans.xsd.DependenciesBean;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.common.xsd.ResourceData;
import org.wso2.carbon.registry.search.stub.SearchAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.clients.*;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import javax.activation.DataHandler;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.*;

public class Registry732 extends GREGIntegrationBaseTest {

    private LifeCycleAdminServiceClient lifeCycleAdminServiceClient;
    private LifeCycleManagementClient lifeCycleManagementClient;
    private GovernanceServiceClient governanceServiceClient;
    private ListMetaDataServiceClient listMetadataServiceClient;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private RelationAdminServiceClient relationServiceClient;
    private SearchAdminServiceClient searchAdminServiceClient;
    private WSRegistryServiceClient wsRegistryServiceClient;
    private ServiceManager serviceManager;
    private String verServiceString;
    private static final String SERVICE_NAME = "IntergalacticService3";
    private static final String ROOT = "/";
    private static final String DEPENDENCY_RES_NAME = "AResource";
    private static final String ASSOCIATION_COLL = "/_system";
    private static final String SERVICE_LOCATION = "/_system/governance";
    private static final String LC_NAME = "CARepService";
    private static final String RES_DESC = "Description of the resource";
    private static final String LC_STATE1 = "Planned";
    private static final String ACTION_PROMOTE = "Promote";
    private static final String ACTION_ITEM_CLICK = "itemClick";
    private String serviceString = "/trunk/services/com/abb/1.0.0-SNAPSHOT/IntergalacticService3";
    private final String absPath = SERVICE_LOCATION + serviceString;
    private LifecycleBean lifeCycle;
    private String[] dependencyList;
    private String userName1WithoutDomain;

    @BeforeClass (alwaysRun = true)
    public void initialize () throws Exception {

        super.init(TestUserMode.SUPER_TENANT_USER);
        String sessionCookie = getSessionCookie();
        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        lifeCycleAdminServiceClient =
                new LifeCycleAdminServiceClient(backendURL, sessionCookie);
        governanceServiceClient =
                new GovernanceServiceClient(backendURL, sessionCookie);
        listMetadataServiceClient =
                new ListMetaDataServiceClient(backendURL, sessionCookie);
        lifeCycleManagementClient =
                new LifeCycleManagementClient(backendURL, sessionCookie);
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(backendURL, sessionCookie);
        wsRegistryServiceClient =
                registryProviderUtil.getWSRegistry(automationContext);
        relationServiceClient =
                new RelationAdminServiceClient(backendURL, sessionCookie);
        searchAdminServiceClient =
                new SearchAdminServiceClient(backendURL, sessionCookie);
        wsRegistryServiceClient =
                new RegistryProviderUtil().getWSRegistry(automationContext);
        Registry reg = registryProviderUtil.getGovernanceRegistry(new RegistryProviderUtil()
                .getWSRegistry(automationContext), automationContext);
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) reg);
        serviceManager = new ServiceManager(reg);
        String userName = automationContext.getContextTenant().getContextUser().getUserName();
        userName1WithoutDomain = userName.substring(0, userName.indexOf('@'));

    }

    @Test (groups = "wso2.greg", description = "Create a service")
    public void testCreateService () throws XMLStreamException, IOException,
            AddServicesServiceRegistryExceptionException,
            ListMetadataServiceRegistryExceptionException,
            ResourceAdminServiceExceptionException,
            CustomLifecyclesChecklistAdminServiceExceptionException,
            GovernanceException {

        String servicePath =
                getTestArtifactLocation() + "artifacts" +
                        File.separator + "GREG" + File.separator + "services" +
                        File.separator + "intergalacticService3.metadata.xml";
        DataHandler dataHandler = new DataHandler(new URL("file:///" + servicePath));
        String mediaType = "application/vnd.wso2-service+xml";
        String description = "This is a test service";
        resourceAdminServiceClient.addResource(
                "/_system/governance/service2", mediaType, description, dataHandler);
        ResourceData[] data = resourceAdminServiceClient.getResource(absPath);
        assertNotNull(data, "Service not found");
        //set the dependencies
        dependencyList = lifeCycleAdminServiceClient.getAllDependencies(absPath);

    }

    @Test (dependsOnMethods = "testCreateService")
    public void testAddResource ()
            throws MalformedURLException, ResourceAdminServiceExceptionException, RemoteException {

        String path = getTestArtifactLocation() + "artifacts" + File.separator
                + "GREG" + File.separator + "testresource.txt";
        DataHandler dataHandler = new DataHandler(new URL("file:///" + path));
        String fileType = "plain/text";
        resourceAdminServiceClient.addResource(ROOT + DEPENDENCY_RES_NAME, fileType, RES_DESC, dataHandler);
        String authorUserName =
                resourceAdminServiceClient.getResource(ROOT + DEPENDENCY_RES_NAME)[0].getAuthorUserName();
        assertTrue(userName1WithoutDomain.equalsIgnoreCase(authorUserName), "Dependency resource creation failure");

    }

    @Test (description = "Add dependency", dependsOnMethods = "testAddResource")
    public void testAddDependencyToService ()
            throws AddAssociationRegistryExceptionException, RemoteException {

        String dependencyType = "depends";
        String todo = "add";
        relationServiceClient.addAssociation(absPath,
                dependencyType, ROOT + DEPENDENCY_RES_NAME, todo);
        DependenciesBean bean = relationServiceClient.getDependencies(absPath);
        boolean dependencyTypeMatches = false;
        boolean targetDependencyMatches = false;
        boolean sourceDependencyMatches = false;
        for (AssociationBean dBean : bean.getAssociationBeans()) {
            if (dBean.getDestinationPath().equalsIgnoreCase(ROOT + DEPENDENCY_RES_NAME)) {
                targetDependencyMatches = true;
                if (dBean.getAssociationType().equalsIgnoreCase(dependencyType)) {
                    dependencyTypeMatches = true;
                }
                if (dBean.getSourcePath().equalsIgnoreCase(absPath)) {
                    sourceDependencyMatches = true;
                }
            }

        }
        assertTrue(targetDependencyMatches,
                "Target dependency is not correct");
        assertTrue(dependencyTypeMatches,
                "Dependency type is not correct");
        assertTrue(sourceDependencyMatches,
                "Source dependency is not correct");
    }

    @Test (description = "Add dependency", dependsOnMethods = "testAddDependencyToService")
    public void testAddAssociationToService ()
            throws AddAssociationRegistryExceptionException, RemoteException {

        String dependencyType = "association";
        String todo = "add";
        relationServiceClient.addAssociation(absPath, dependencyType, ASSOCIATION_COLL, todo);
        DependenciesBean bean = relationServiceClient.getDependencies(absPath);
        AssociationBean[] aBeans = bean.getAssociationBeans();
        boolean found = false;
        for (AssociationBean aBean : aBeans) {
            if (ASSOCIATION_COLL.equalsIgnoreCase(aBean.getDestinationPath())) {
                found = true;
            }
        }
        assertTrue(found, "Association cannot be found");

    }

    @Test (groups = "wso2.greg", description = "Create new life cycle", dependsOnMethods = "testAddDependencyToService")
    public void testCreateNewLifeCycle () throws LifeCycleManagementServiceExceptionException,
            IOException, InterruptedException,
            SearchAdminServiceRegistryExceptionException,
            RegistryException {

        String resourcePath =
                getTestArtifactLocation() + "artifacts" +
                        File.separator + "GREG" + File.separator + "lifecycle" +
                        File.separator + "CARepService.xml";
        String lifeCycleContent = FileManager.readFile(resourcePath);
        LifeCycleUtils.deleteLcUsageResources(searchAdminServiceClient, wsRegistryServiceClient, LC_NAME);
        LifeCycleUtils.deleteLifeCycleIfExist(LC_NAME, lifeCycleManagementClient);
        lifeCycleManagementClient.addLifeCycle(lifeCycleContent);
        String[] lifeClycles = lifeCycleManagementClient.getLifecycleList();
        boolean lcStatus = false;
        for (String lc : lifeClycles) {
            if (lc.equalsIgnoreCase(LC_NAME)) {
                lcStatus = true;
                break;
            }
        }
        assertTrue(lcStatus, "LifeCycle not found");

    }

    @Test (groups = "wso2.greg", description = "Add lifecycle to a service",
            dependsOnMethods = "testCreateNewLifeCycle")
    public void testAddLcToService () throws RegistryException, RemoteException,
            CustomLifecyclesChecklistAdminServiceExceptionException,
            ListMetadataServiceRegistryExceptionException,
            ResourceAdminServiceExceptionException {

        wsRegistryServiceClient.associateAspect(absPath, LC_NAME);
        lifeCycle =
                lifeCycleAdminServiceClient.getLifecycleBean(absPath);
        Property[] properties = lifeCycle.getLifecycleProperties();
        boolean lcStatus = false;
        for (Property prop : properties) {
            if (prop.getKey().contains(LC_NAME)) {
                lcStatus = true;
                break;
            }
        }
        assertTrue(lcStatus, "LifeCycle not added to service");
    }

    @Test (groups = "wso2.greg", description = "test the promoting", dependsOnMethods = "testAddLcToService")
    public void testPromoting ()
            throws CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException,
            ResourceAdminServiceExceptionException {

        lifeCycleAdminServiceClient.invokeAspect(absPath, LC_NAME,
                ACTION_ITEM_CLICK, new String[]{"true"});
        lifeCycleAdminServiceClient.invokeAspect(absPath,
                LC_NAME, ACTION_PROMOTE, null);
        verServiceString = "/_system/governance/branches/testing/services/com/abb/IntergalacticService3/1.0.0-SNAPSHOT/service";
        lifeCycle =
                lifeCycleAdminServiceClient.getLifecycleBean(verServiceString);
        Property[] properties = lifeCycle.getLifecycleProperties();
        if (properties != null) {
            for (Property prop : properties) {
                if (("registry.lifecycle." + LC_NAME + ".state").equalsIgnoreCase(prop.getKey())) {
                    assertNotNull(prop.getValues(), "State Value Not Found");
                    assertTrue(prop.getValues()[0].equalsIgnoreCase(LC_STATE1),
                            "Not promoted to Testing");
                }
            }
        }

    }

    @Test (groups = "wso2.greg", description = "test The dependencies after promoting",
            dependsOnMethods = "testPromoting")
    public void testDependencies () throws CustomLifecyclesChecklistAdminServiceExceptionException,
            RemoteException, AddAssociationRegistryExceptionException {

        DependenciesBean bean = relationServiceClient.getDependencies(verServiceString);
        //search for the association
        boolean associationFound = false;
        boolean dependencyFound = false;
        AssociationBean[] aBeans = bean.getAssociationBeans();
        if (aBeans != null) {
            associationFound = true;
            dependencyFound = true;
        }
        assertFalse(associationFound, "Association exists even after promoting the service");
        assertFalse(dependencyFound, "Dependency exists even after promoting the service");

    }

    @AfterClass (alwaysRun = true)
    public void cleanup () throws ResourceAdminServiceExceptionException, RemoteException,
            LifeCycleManagementServiceExceptionException {

        resourceAdminServiceClient.deleteResource(ROOT + DEPENDENCY_RES_NAME);
        resourceAdminServiceClient.deleteResource(absPath);
        resourceAdminServiceClient.deleteResource(verServiceString);
        lifeCycleManagementClient.deleteLifeCycle(LC_NAME);
    }
}
