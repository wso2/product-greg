package org.wso2.carbon.registry.utfsupport.test;

import org.apache.axis2.AxisFault;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.governance.LifeCycleAdminServiceClient;
import org.wso2.carbon.automation.api.clients.governance.LifeCycleManagementClient;
import org.wso2.carbon.automation.api.clients.registry.InfoServiceAdminClient;
import org.wso2.carbon.automation.api.clients.registry.RelationAdminServiceClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.api.clients.user.mgt.UserManagementClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException;
import org.wso2.carbon.governance.list.stub.ListMetadataServiceRegistryExceptionException;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.info.stub.RegistryExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.utfsupport.test.util.UTFSupport;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.rmi.RemoteException;

public class UTFSupportForServiceTestCase {


    private Registry governance;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private String utfString;
    private String currentServiceID;
    private ServiceManager serviceManager;
    private RelationAdminServiceClient relationAdminServiceClient;
    private String servicepath1;
    private String servicePath2;
    private final String LC_NAME = "ÀÁÂÃÄÅÆÇÈÉ";
    private String pathPrefix = "/_system/governance";
    private LifeCycleManagementClient lifeCycleManagementClient;
    private WSRegistryServiceClient wsRegistryServiceClient;
    private RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
    private LifeCycleAdminServiceClient lifeCycleAdminServiceClient;
    private InfoServiceAdminClient infoServiceAdminClient;
    private ManageEnvironment environment;
    private UserManagementClient userManagementClient;
    private UserInfo userInfo;

    @BeforeClass
    public void init() throws Exception {
        int userId = 2;
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        environment = builder.build();
        userInfo = UserListCsvReader.getUserInfo(userId);
        userManagementClient = new UserManagementClient(environment.getGreg().getProductVariables().getBackendUrl(),
                environment.getGreg().getSessionCookie());
        infoServiceAdminClient = new InfoServiceAdminClient(environment.getGreg().getBackEndUrl(),
                environment.getGreg().getSessionCookie());
        wsRegistryServiceClient = registryProviderUtil.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        lifeCycleAdminServiceClient = new LifeCycleAdminServiceClient(environment.getGreg().getBackEndUrl(),
                environment.getGreg().getSessionCookie());
        lifeCycleManagementClient = new LifeCycleManagementClient(environment.getGreg().getBackEndUrl(),
                environment.getGreg().getSessionCookie());
        relationAdminServiceClient =
                new RelationAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                        environment.getGreg().getSessionCookie());

        resourceAdminServiceClient =
                new ResourceAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                        environment.getGreg().getSessionCookie());
        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        governance = registryProviderUtil.getGovernanceRegistry(wsRegistryServiceClient, userId);

    }

    @Test(groups = {"wso2.greg"}, description = "read UTF characters from file")
    public void testreadFile() throws IOException {
        utfString = UTFSupport.readFile();

    }


    @Test(groups = {"wso2.greg"}, description = "Test service with UTF characters", dependsOnMethods = "testreadFile")
    public void testAddService() throws Exception {
        servicepath1 = addService("https://contoso.accesscontrol.windows.net", "service" + utfString, governance);
        servicePath2 = addService("http://schemas.umbraworks.net/rebuildall", "dependency_" + utfString, governance);

        Assert.assertNotNull(servicepath1);

    }

    @Test(groups = {"wso2.greg"}, description = "add dependency to the service", dependsOnMethods = {"testAddService"})
    public void testAddServiceDependency() throws Exception {

        Assert.assertTrue(UTFSupport.addDependency(relationAdminServiceClient,
                pathPrefix + servicepath1, pathPrefix + servicePath2));

    }

    @Test(groups = {"wso2.greg"}, description = "add association to the service", dependsOnMethods = {"testAddServiceDependency"})
    public void testAddServiceAssociation() throws Exception {

        Assert.assertTrue(UTFSupport.addAssociation(relationAdminServiceClient,
                pathPrefix + servicepath1, pathPrefix + servicePath2));

    }

    @Test(groups = {"wso2.greg"}, description = "create lifecycle", dependsOnMethods = {"testAddServiceAssociation"})
    public void testcreateLifecycle() throws Exception {
        Assert.assertTrue(UTFSupport.createLifecycle(lifeCycleManagementClient, LC_NAME));

    }

    @Test(groups = "wso2.greg", description = "Add role", dependsOnMethods = "testcreateLifecycle")
    public void testAddRole() throws Exception {

        Assert.assertTrue(UTFSupport.addRole(userManagementClient, utfString, userInfo));

    }

    @Test(groups = "wso2.greg", description = "Add subscription", dependsOnMethods = {"testAddRole"})
    public void testAddSubscription() throws Exception {

        Assert.assertTrue(UTFSupport.addSubscription(infoServiceAdminClient, pathPrefix + servicepath1, utfString, environment));

    }


    @Test(groups = "wso2.greg", description = "Add lifecycle to a service", dependsOnMethods = {"testAddSubscription"})
    public void testAddLcToService() throws RegistryException, RemoteException,
            CustomLifecyclesChecklistAdminServiceExceptionException,
            ListMetadataServiceRegistryExceptionException,
            ResourceAdminServiceExceptionException {

        Assert.assertTrue(UTFSupport.addLc(wsRegistryServiceClient, pathPrefix + servicepath1, LC_NAME,
                lifeCycleAdminServiceClient));


    }

    @Test(groups = "wso2.greg", description = "edit service", dependsOnMethods = {"testAddLcToService"})
    public void testEditService() throws RegistryException, AxisFault, RegistryExceptionException {

        Service service = serviceManager.getService(currentServiceID);
        service.addAttribute("overview_description", utfString);
        Assert.assertTrue((service.getAttribute("overview_description")).equals(utfString));

    }


    @Test(groups = "wso2.greg", description = "Add comment to a service", dependsOnMethods = {"testEditService"})
    public void testAddCommentToService()
            throws RegistryException, AxisFault, RegistryExceptionException {

        Assert.assertTrue(UTFSupport.addComment(infoServiceAdminClient, utfString, pathPrefix + servicepath1, environment));
    }

    @Test(groups = "wso2.greg", description = "Add a utf char tag ", dependsOnMethods = {"testAddCommentToService"})
    public void tesAddTagToService()
            throws RegistryException, RemoteException, ResourceAdminServiceExceptionException,
            RegistryExceptionException {
        Assert.assertTrue(UTFSupport.addTag(infoServiceAdminClient, utfString, pathPrefix + servicepath1, environment));

    }

    public String addService(String nameSpace, String serviceName, Registry governance)
            throws Exception {
        serviceManager = new ServiceManager(governance);
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
        Service service;
        service = serviceManager.newService(new QName(nameSpace, serviceName));

        serviceManager.addService(service);

        for (String serviceId : serviceManager.getAllServiceIds()) {
            service = serviceManager.getService(serviceId);
            if (service.getPath().endsWith(serviceName) && service.getPath().contains("trunk")) {
                currentServiceID = serviceId;
                return service.getPath();

            }

        }
        throw new Exception("Getting Service path failed");

    }

    @AfterClass
    public void clean() throws Exception {
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
        delete(pathPrefix + servicepath1);
        Service[] services = serviceManager.getAllServices();
        boolean serviceDeleted = true;
        for (Service service : services) {
            if (service.getPath().endsWith("service" + utfString)) {
                serviceDeleted = false;
            }
        }
        Assert.assertTrue(serviceDeleted);
        delete(pathPrefix + servicePath2);
        userManagementClient.deleteRole(utfString);
        lifeCycleManagementClient.deleteLifeCycle(LC_NAME);

        utfString = null;
        governance = null;
        resourceAdminServiceClient = null;
        currentServiceID = null;
        serviceManager = null;
        relationAdminServiceClient = null;
        servicepath1 = null;
        servicePath2 = null;
        pathPrefix = null;
        lifeCycleManagementClient = null;
        wsRegistryServiceClient = null;
        wsRegistryServiceClient = null;
        lifeCycleAdminServiceClient = null;
        infoServiceAdminClient = null;
        userManagementClient = null;
    }

    public void delete(String destPath) throws ResourceAdminServiceExceptionException, RemoteException, RegistryException {
        if (wsRegistryServiceClient.resourceExists(destPath)) {
            resourceAdminServiceClient.deleteResource(destPath);
        }
    }

}
