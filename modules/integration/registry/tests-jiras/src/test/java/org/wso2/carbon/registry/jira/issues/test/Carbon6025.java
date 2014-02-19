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
import org.wso2.carbon.governance.api.policies.PolicyManager;
import org.wso2.carbon.governance.api.policies.dataobjects.Policy;
import org.wso2.carbon.governance.api.schema.SchemaManager;
import org.wso2.carbon.governance.api.schema.dataobjects.Schema;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import javax.activation.DataHandler;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;

public class Carbon6025 {

    private Registry governance;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
    private WSRegistryServiceClient wsRegistry;

    @BeforeClass
    public void init() throws Exception {

        int userId = ProductConstant.ADMIN_USER_ID;
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        ManageEnvironment environment = builder.build();
        UserInfo userInfo = UserListCsvReader.getUserInfo(userId);
        wsRegistry =
                registryProviderUtil.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                               environment.getGreg().getSessionCookie());
        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        governance = registryProviderUtil.getGovernanceRegistry(wsRegistry, userId);


    }

    @Test(groups = {"wso2.greg"}, description = "add a wsdl")
    public void testAddGar()
            throws RegistryException, IOException, ResourceAdminServiceExceptionException {

        String filePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator +
                          "GREG" + File.separator + "gar" + File.separator + "myGar.gar";
        DataHandler dh = new DataHandler(new URL("file:///" + filePath));
        resourceAdminServiceClient.addResource("/_system/governance/trunk/test/wsdls",
                                               "application/vnd.wso2.governance-archive", "desc", dh);


    }

    @Test(groups = {"wso2.greg"}, description = "verify imports inside wsdl", dependsOnMethods = "testAddGar")
    public void testVerifyImports() throws RegistryException {
        verifyService();
        verifySchema();
        verifyPolicy();
        verifyWSDL();
    }

    public void verifyService() throws RegistryException {
        ServiceManager serviceManager = new ServiceManager(governance);
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
        Service[] services = serviceManager.getAllServices();
        boolean resourceFound = false;
        for (Service service : services) {
            if (service.getQName().getLocalPart().equals("SimpleStockQuoteService1M")) {
                resourceFound = true;
            }
        }
        Assert.assertTrue(resourceFound);
    }

    public void verifyWSDL() throws GovernanceException {
        WsdlManager wsdlManager = new WsdlManager(governance);
        Wsdl[] wsdls = wsdlManager.getAllWsdls();
        boolean resourceFound = false;
        for (Wsdl wsdl : wsdls) {
            if (wsdl.getQName().getLocalPart().equals("wsdl_with_EncrOnlyAnonymous.wsdl")) {
                resourceFound = true;
            }
        }
        Assert.assertTrue(resourceFound);
    }

    public void verifySchema() throws GovernanceException {
        SchemaManager schemaManager = new SchemaManager(governance);
        Schema[] schemas = schemaManager.getAllSchemas();
        boolean resourceFound = false;
        for (Schema schema : schemas) {
            if (schema.getQName().getLocalPart().equals("SampleSchema.xsd")) {
                resourceFound = true;
            }
        }
        Assert.assertTrue(resourceFound);
    }

    public void verifyPolicy() throws GovernanceException {
        PolicyManager policyManager = new PolicyManager(governance);
        Policy[] policies = policyManager.getAllPolicies();
        boolean resourceFound = false;
        for (Policy policy : policies) {
            if (policy.getQName().getLocalPart().equals("EncrOnlyAnonymous.xml")) {
                resourceFound = true;
            }
        }
        Assert.assertTrue(resourceFound);
    }


    @AfterClass
    public void clean()
            throws RegistryException, ResourceAdminServiceExceptionException, RemoteException {

        delete("/_system/governance/trunk/services/samples/services/SimpleStockQuoteService1M");
        delete("/_system/governance/trunk/wsdls/samples/services/wsdl_with_EncrOnlyAnonymous.wsdl");
        delete("/_system/governance/trunk/schemas/samples/services/xsd/SampleSchema.xsd");
        delete("/_system/governance/trunk/policies/EncrOnlyAnonymous.xml");
        delete("/_system/governance/trunk/endpoints/_109");

        registryProviderUtil = null;
        resourceAdminServiceClient = null;
        governance = null;

    }

    public void delete(String destPath) throws ResourceAdminServiceExceptionException, RemoteException, RegistryException {
        if(wsRegistry.resourceExists(destPath)){
            resourceAdminServiceClient.deleteResource(destPath);
        }
    }

}

