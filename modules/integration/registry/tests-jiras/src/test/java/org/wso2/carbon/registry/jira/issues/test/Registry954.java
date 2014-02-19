package org.wso2.carbon.registry.jira.issues.test;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import javax.xml.namespace.QName;
import java.rmi.RemoteException;

/**
 * [Governance-API]service.getPath() is return NPE
 * https://wso2.org/jira/browse/REGISTRY-954
 */
public class Registry954 {

    private Registry governance;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
    private ServiceManager serviceManager;
    private WSRegistryServiceClient wsRegistryServiceClient;

    @BeforeClass
    public void init() throws Exception {
        int userId = ProductConstant.ADMIN_USER_ID;
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        ManageEnvironment environment = builder.build();
        UserInfo userInfo = UserListCsvReader.getUserInfo(userId);
        wsRegistryServiceClient =
                registryProviderUtil.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                               environment.getGreg().getSessionCookie());
        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        governance = registryProviderUtil.getGovernanceRegistry(wsRegistryServiceClient, userId);

    }

    @Test(groups = {"wso2.greg"}, description = "test getPath() method")
    public void testGetServicePath() throws RegistryException {
        addService("test_namespace1", "service1", "1.0.0");
        addService("test_namespace2", "service2", "1.0.0");
        addService("test_namespace3", "service3", "1.0.0");
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
        Service[] services = serviceManager.getAllServices();
        boolean foundNull = false;
        for (Service service : services) {
            if (service.getPath() == null) {
                foundNull = true;
            }
        }
        Assert.assertFalse(foundNull);
    }

    public void addService(String namespace, String serviceName, String version)
            throws GovernanceException {
        serviceManager = new ServiceManager(governance);
        Service service;
        service = serviceManager.newService(new QName(namespace, serviceName));
        service.addAttribute("overview_version", version);
        serviceManager.addService(service);
    }

    @AfterClass
    public void clean()
            throws RegistryException, ResourceAdminServiceExceptionException, RemoteException {

        delete("/_system/governance/trunk/services/test_namespace1/service1");
        delete("/_system/governance/trunk/services/test_namespace2/service2");
        delete("/_system/governance/trunk/services/test_namespace3/service3");
        governance = null;
        resourceAdminServiceClient = null;
        registryProviderUtil = null;
        serviceManager = null;
    }

    public void delete(String destPath) throws ResourceAdminServiceExceptionException, RemoteException, RegistryException {
        if(wsRegistryServiceClient.resourceExists(destPath)){
            resourceAdminServiceClient.deleteResource(destPath);
        }
    }
}
