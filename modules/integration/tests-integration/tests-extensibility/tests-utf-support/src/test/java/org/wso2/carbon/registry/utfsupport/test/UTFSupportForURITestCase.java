package org.wso2.carbon.registry.utfsupport.test;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException;
import org.wso2.carbon.governance.list.stub.ListMetadataServiceRegistryExceptionException;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.utfsupport.test.util.UTFSupport;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.clients.*;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;

public class UTFSupportForURITestCase extends GREGIntegrationBaseTest {

    private Registry governance;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private String utfString;
    private String uriWsdlPath;
    private String uriSchemaPath;
    private String uriPolicyPath;
    private String uriGenericPath;
    private RelationAdminServiceClient relationAdminServiceClient;
    private final String WSDL_URL = "https://svn.wso2.org/repos/wso2/carbon/platform/trunk/products/greg/modules/" +
            "integration/registry/tests-new/src/test/resources/artifacts/GREG/wsdl/AmazonWebServices.wsdl";

    private final String SCHEMA_URL = "https://svn.wso2.org/repos/wso2/carbon/platform/trunk/products/greg/modules/" +
            "integration/registry/tests-new/src/test/resources/artifacts/GREG/schema/LinkedSchema.xsd";

    private final String POLICY_URL = "https://svn.wso2.org/repos/wso2/carbon/platform/trunk/products/greg/modules/" +
            "integration/registry/tests-new/src/test/resources/artifacts/GREG/policy/EncrOnlyAnonymous.xml";

    private final String TEXT_URL = "https://svn.wso2.org/repos/wso2/carbon/platform/trunk/products/greg/modules/" +
            "integration/registry/tests-new/src/test/resources/artifacts/GREG/resource.txt";

    private final String LC_NAME = "ÀÁÂÃÄÅÆÇÈÉ";
    private String pathPrefix = "/_system/governance";
    private LifeCycleManagementClient lifeCycleManagementClient;
    private WSRegistryServiceClient wsRegistryServiceClient;
    private RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
    private LifeCycleAdminServiceClient lifeCycleAdminServiceClient;
    private InfoServiceAdminClient infoServiceAdminClient;
    private UserManagementClient userManagementClient;
    private String uriID;

    private String sessionCookie;
    private String backEndUrl;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        backEndUrl = getBackendURL();
        sessionCookie = getSessionCookie();

        userManagementClient =
                new UserManagementClient(backEndUrl,
                                         sessionCookie);
        infoServiceAdminClient =
                new InfoServiceAdminClient(backEndUrl,
                                           sessionCookie);
        wsRegistryServiceClient =
                registryProviderUtil.getWSRegistry(automationContext);
        lifeCycleAdminServiceClient =
                new LifeCycleAdminServiceClient(backEndUrl,
                                                sessionCookie);
        lifeCycleManagementClient =
                new LifeCycleManagementClient(backEndUrl,
                                              sessionCookie);
        relationAdminServiceClient =
                new RelationAdminServiceClient(backEndUrl,
                                               sessionCookie);
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(backEndUrl,
                                               sessionCookie);

        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        governance = registryProviderUtil.getGovernanceRegistry(wsRegistryServiceClient, automationContext);

    }

    @Test(groups = {"wso2.greg"}, description = "read UTF characters from file")
    public void testreadFile() throws IOException {
        utfString = UTFSupport.readFile();

    }

    @Test(groups = {"wso2.greg"}, description = "Test URI with UTF character names", dependsOnMethods = "testreadFile")
    public void testAddURI() throws Exception {
        uriGenericPath = addURI("text_" + utfString, TEXT_URL, "Generic");
        uriWsdlPath = addURI("wsdl_" + utfString, WSDL_URL, "WSDL");
        uriSchemaPath = addURI("schema_" + utfString, SCHEMA_URL, "XSD");
        uriPolicyPath = addURI("policy" + utfString, POLICY_URL, "Policy");

        Assert.assertNotNull(uriSchemaPath);
        Assert.assertNotNull(uriPolicyPath);
        Assert.assertNotNull(uriGenericPath);
        Assert.assertNotNull(uriWsdlPath);
    }

    @Test(groups = {"wso2.greg"}, description = "add dependency", dependsOnMethods = {"testAddURI"})
    public void testAddDependency() throws Exception {
        Assert.assertTrue(UTFSupport.addDependency(relationAdminServiceClient, pathPrefix + uriPolicyPath, pathPrefix + uriSchemaPath));
    }

    @Test(groups = {"wso2.greg"}, description = "add association", dependsOnMethods = {"testAddDependency"})
    public void testaddAssociation() throws Exception {
        Assert.assertTrue(UTFSupport.addAssociation(relationAdminServiceClient, pathPrefix + uriPolicyPath, pathPrefix + uriWsdlPath));
    }

    @Test(groups = {"wso2.greg"}, description = "create lifecycle", dependsOnMethods = {"testaddAssociation"})
    public void testCreateLifecycle() throws Exception {
        Assert.assertTrue(UTFSupport.createLifecycle(lifeCycleManagementClient, LC_NAME));
    }

    @Test(groups = {"wso2.greg"}, description = "add lifecycle", dependsOnMethods = {"testCreateLifecycle"})
    public void testAddLifecycle() throws ResourceAdminServiceExceptionException,
                                          RegistryException,
                                          ListMetadataServiceRegistryExceptionException,
                                          CustomLifecyclesChecklistAdminServiceExceptionException,
                                          RemoteException {
        Assert.assertTrue(UTFSupport.addLc(wsRegistryServiceClient, pathPrefix + uriPolicyPath, LC_NAME, lifeCycleAdminServiceClient));
    }

    @Test(groups = {"wso2.greg"}, description = "add comment", dependsOnMethods = {"testAddLifecycle"})
    public void testAddComment() throws Exception {
        Assert.assertTrue(UTFSupport.addComment(infoServiceAdminClient, utfString, pathPrefix + uriPolicyPath, automationContext));
    }

    @Test(groups = {"wso2.greg"}, description = "create role", dependsOnMethods = {"testAddComment"})
    public void testCreateRole() throws Exception {
        Assert.assertTrue(UTFSupport.addRole(userManagementClient, utfString, automationContext));
    }

    @Test(groups = {"wso2.greg"}, description = "add subscription", dependsOnMethods = {"testCreateRole"})
    public void testAddSubscription() throws Exception {
        Assert.assertTrue(UTFSupport.addSubscription(infoServiceAdminClient, pathPrefix + uriPolicyPath, utfString, automationContext));
    }

    @Test(groups = {"wso2.greg"}, description = "add tag", dependsOnMethods = {"testAddSubscription"})
    public void testAddTag() throws Exception {
        Assert.assertTrue(UTFSupport.addTag(infoServiceAdminClient, utfString, pathPrefix + uriPolicyPath, automationContext));
    }

    @Test(groups = {"wso2.greg"}, description = "edit uri", dependsOnMethods = {"testAddTag"})
    public void testEditURI() throws RegistryException {
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
        GenericArtifactManager artifactManager = new GenericArtifactManager(governance, "uri");
        GenericArtifact artifact = artifactManager.getGenericArtifact(uriID);
        artifact.addAttribute("overview_description", utfString);
        Assert.assertTrue(artifact.getAttribute("overview_description").equals(utfString));

    }

    public String addURI(String name, String uri, String type) throws RegistryException {
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
        GenericArtifactManager artifactManager = new GenericArtifactManager(governance, "uri");
        GenericArtifact artifact = artifactManager.newGovernanceArtifact(new QName
                                                                         (name));

        artifact.setAttribute("overview_uri", uri);

        artifact.setAttribute("overview_type", type);
        artifactManager.addGenericArtifact(artifact);
        uriID = artifact.getId();
        assertTrue(artifact.getAttribute("overview_uri").equals(uri), "artifact URI not found");
        assertTrue(artifact.getAttribute("overview_name").equals(name), "artifact name not found");
        assertTrue(artifact.getAttribute("overview_type").equals(type), "artifact WSDL not found");

        return artifact.getPath();
    }

    @AfterClass(alwaysRun = true)
    public void clean() throws Exception {
        delete(pathPrefix + uriSchemaPath);

        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
        GenericArtifactManager artifactManager = new GenericArtifactManager(governance, "uri");
        GenericArtifact[] artifacts = artifactManager.getAllGenericArtifacts();
        boolean uriDeleted = true;
        for (GenericArtifact genericArtifact : artifacts) {
            if (genericArtifact.getPath().equals(pathPrefix + uriSchemaPath)) {
                uriDeleted = false;
            }
        }
        Assert.assertTrue(uriDeleted);
        delete(pathPrefix + uriWsdlPath);
        delete(pathPrefix + uriGenericPath);
        delete(pathPrefix + uriPolicyPath);
        delete("/_system/governance/trunk/services/com/amazon/soap/1.0.0/AmazonSearchService");
        userManagementClient.deleteRole(utfString);
        lifeCycleManagementClient.deleteLifeCycle(LC_NAME);
        delete("/_system/governance/trunk/endpoints/com/amazon/soap/onca/ep-soap2");
        delete("/_system/governance/trunk/endpoints/com");

        utfString = null;
        governance = null;
        resourceAdminServiceClient = null;
        uriGenericPath = null;
        uriPolicyPath = null;
        uriSchemaPath = null;
        uriWsdlPath = null;
        relationAdminServiceClient = null;
        lifeCycleManagementClient = null;
        lifeCycleAdminServiceClient = null;
        wsRegistryServiceClient = null;
        registryProviderUtil = null;
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
