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
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.schema.SchemaManager;
import org.wso2.carbon.governance.api.schema.dataobjects.Schema;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException;
import org.wso2.carbon.governance.list.stub.ListMetadataServiceRegistryExceptionException;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.info.stub.RegistryExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.utfsupport.test.util.UTFSupport;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import java.io.IOException;
import java.rmi.RemoteException;

public class UTFSupportForSchemaTestCase {

    private Registry governance;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private String utfString;
    private String schemaPath;
    private String wsdlPath;
    private RelationAdminServiceClient relationAdminServiceClient;
    private final String WSDL_URL = "https://svn.wso2.org/repos/wso2/carbon/platform/trunk/products/greg/modules/" +
            "integration/registry/tests-new/src/test/resources/artifacts/GREG/wsdl/AmazonWebServices.wsdl";

    private final String SCHEMA_URL = "https://svn.wso2.org/repos/wso2/carbon/platform/trunk/products/greg/modules/" +
            "integration/registry/tests-new/src/test/resources/artifacts/GREG/schema/LinkedSchema.xsd";
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
    private String schemaID;
    private SchemaManager schemaManager;

    @BeforeClass
    public void init() throws Exception {
        int userId = 2;
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        environment = builder.build();
        userInfo = UserListCsvReader.getUserInfo(userId);
        userManagementClient =
                new UserManagementClient(environment.getGreg().getBackEndUrl(),
                                         environment.getGreg().getSessionCookie());

        infoServiceAdminClient =
                new InfoServiceAdminClient(environment.getGreg().getBackEndUrl(),
                                           environment.getGreg().getSessionCookie());
        wsRegistryServiceClient =
                registryProviderUtil.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        lifeCycleAdminServiceClient =
                new LifeCycleAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                                environment.getGreg().getSessionCookie());
        lifeCycleManagementClient =
                new LifeCycleManagementClient(environment.getGreg().getBackEndUrl(),
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

    @Test(groups = {"wso2.greg"}, description = "Test schema with UTF characters", dependsOnMethods = "testreadFile")
    public void testAddSchema() throws Exception {
        schemaPath = addSchema("schema_" + utfString, "desc", SCHEMA_URL);
        wsdlPath = addWSDL("wsdl_", WSDL_URL);

        Assert.assertNotNull(schemaPath);

    }

    @Test(groups = {"wso2.greg"}, description = "add dependency", dependsOnMethods = {"testAddSchema"})
    public void testAddDependancies() throws Exception {
        Assert.assertTrue(UTFSupport.addDependency(relationAdminServiceClient, pathPrefix + schemaPath, pathPrefix + wsdlPath));
    }

    @Test(groups = {"wso2.greg"}, description = "add association", dependsOnMethods = {"testAddDependancies"})
    public void testAddAssociation() throws Exception {
        Assert.assertTrue(UTFSupport.addAssociation(relationAdminServiceClient, pathPrefix + schemaPath, pathPrefix + wsdlPath));
    }

    @Test(groups = {"wso2.greg"}, description = "create lifecycle", dependsOnMethods = {"testAddAssociation"})
    public void testCreateLifecycle() throws Exception {
        Assert.assertTrue(UTFSupport.createLifecycle(lifeCycleManagementClient, LC_NAME));
    }

    @Test(groups = {"wso2.greg"}, description = "add lifecycle", dependsOnMethods = {"testCreateLifecycle"})
    public void testAddLifecycle() throws ResourceAdminServiceExceptionException,
                                          RegistryException,
                                          ListMetadataServiceRegistryExceptionException,
                                          CustomLifecyclesChecklistAdminServiceExceptionException,
                                          RemoteException {
        Assert.assertTrue(UTFSupport.addLc(wsRegistryServiceClient, pathPrefix + schemaPath, LC_NAME, lifeCycleAdminServiceClient));
    }

    @Test(groups = {"wso2.greg"}, description = "add comment", dependsOnMethods = {"testAddLifecycle"})
    public void testAddComment() throws RegistryException, AxisFault, RegistryExceptionException {
        Assert.assertTrue(UTFSupport.addComment(infoServiceAdminClient, utfString, pathPrefix + schemaPath, environment));
    }

    @Test(groups = {"wso2.greg"}, description = "add role", dependsOnMethods = {"testAddComment"})
    public void testAddRole() throws Exception {
        Assert.assertTrue(UTFSupport.addRole(userManagementClient, utfString, userInfo));
    }

    @Test(groups = {"wso2.greg"}, description = "add subscription", dependsOnMethods = {"testAddRole"})
    public void testAddSubscription() throws Exception {
        Assert.assertTrue(UTFSupport.addSubscription(infoServiceAdminClient, pathPrefix + schemaPath, utfString, environment));
    }

    @Test(groups = {"wso2.greg"}, description = "add tag", dependsOnMethods = {"testAddSubscription"})
    public void testAddTag() throws ResourceAdminServiceExceptionException,
                                    RegistryException, RegistryExceptionException, RemoteException {
        Assert.assertTrue(UTFSupport.addTag(infoServiceAdminClient, utfString, pathPrefix + schemaPath, environment));
    }

    @Test(groups = {"wso2.greg"}, description = "edit schema", dependsOnMethods = {"testAddTag"})
    public void testEditSchema() throws GovernanceException {
        Schema schema = schemaManager.getSchema(schemaID);
        schema.addAttribute("overview_description", utfString);
        Assert.assertTrue((schema.getAttribute("overview_description")).equals(utfString));

    }

    public String addSchema(String name, String desc, String url)
            throws ResourceAdminServiceExceptionException,
                   RemoteException, GovernanceException {
        resourceAdminServiceClient.addSchema(name, desc, url);
        schemaManager = new SchemaManager(governance);
        Schema[] schemas = schemaManager.getAllSchemas();
        String path = null;
        for (Schema schema : schemas) {
            if (schema.getQName().getLocalPart().equals(name + ".xsd")) {
                path = schema.getPath();
                schemaID = schema.getId();
            }
        }
        return path;
    }

    public String addWSDL(String wsdlName, String url)
            throws ResourceAdminServiceExceptionException,
                   RemoteException, GovernanceException {

        resourceAdminServiceClient.addWSDL(wsdlName + utfString, "desc", url);

        WsdlManager wsdlManager = new WsdlManager(governance);
        Wsdl[] wsdls;
        wsdls = wsdlManager.getAllWsdls();
        String wsdlPath = null;

        for (Wsdl wsdl : wsdls) {
            String wsdlname = wsdl.getQName().getLocalPart();
            if (wsdlname.equals(wsdlName + utfString + ".wsdl")) {
                wsdlPath = wsdl.getPath();

            }
        }

        return wsdlPath;

    }

    @AfterClass
    public void clean() throws Exception {
        delete(pathPrefix + schemaPath);
        Schema[] schemas = schemaManager.getAllSchemas();
        boolean schemaDeleted = true;
        for (Schema schema : schemas) {
            if (schema.getPath().equals(pathPrefix + schemaPath)) {
                schemaDeleted = false;
            }
        }
        Assert.assertTrue(schemaDeleted);
        delete(pathPrefix + wsdlPath);
        delete("/_system/governance/trunk/services/com/amazon/soap/AmazonSearchService");
        userManagementClient.deleteRole(utfString);
        lifeCycleManagementClient.deleteLifeCycle(LC_NAME);
        delete("/_system/governance/trunk/endpoints/com");

        governance = null;
        resourceAdminServiceClient = null;
        utfString = null;
        schemaPath = null;
        wsdlPath = null;
        relationAdminServiceClient = null;
        pathPrefix = null;
        lifeCycleAdminServiceClient = null;
        wsRegistryServiceClient = null;
        registryProviderUtil = null;
        lifeCycleManagementClient = null;
        infoServiceAdminClient = null;
        userManagementClient = null;
    }

    public void delete(String destPath)
            throws ResourceAdminServiceExceptionException, RemoteException, RegistryException {
        if (wsRegistryServiceClient.resourceExists(destPath)) {
            resourceAdminServiceClient.deleteResource(destPath);
        }
    }

}
