package org.wso2.carbon.registry.utfsupport.test;

import org.apache.axis2.AxisFault;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.policies.PolicyManager;
import org.wso2.carbon.governance.api.policies.dataobjects.Policy;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.info.stub.RegistryExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.utfsupport.test.util.UTFSupport;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.clients.*;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;


public class UTFSupportForPolicyTestCase extends GREGIntegrationBaseTest {


    private Registry governance;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private String utfString;
    private String policyPath;
    private PolicyManager policyManager;
    private String wsdlPath;
    private RelationAdminServiceClient relationAdminServiceClient;
    private final String WSDL_URL = "https://svn.wso2.org/repos/wso2/carbon/platform/trunk/products/greg/modules" +
            "/integration/registry/tests-new/src/test/resources/artifacts/GREG/wsdl/AmazonWebServices.wsdl";

    private final String POLICY_URL = "https://svn.wso2.org/repos/wso2/carbon/platform/trunk/products/greg/modules" +
            "/integration/registry/tests-new/src/test/resources/artifacts/GREG/policy/EncrOnlyAnonymous.xml";

    private final String LC_NAME = "ÀÁÂÃÄÅÆÇÈÉ";
    private String pathPrefix = "/_system/governance";
    private LifeCycleManagementClient lifeCycleManagementClient;
    private WSRegistryServiceClient wsRegistryServiceClient;
    private RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
    private LifeCycleAdminServiceClient lifeCycleAdminServiceClient;
    private InfoServiceAdminClient infoServiceAdminClient;
    private UserManagementClient userManagementClient;
    private String policyID;

    private String sessionCookie;
    private String backEndUrl;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        backEndUrl = getBackendURL();
        sessionCookie = getSessionCookie();

        
        userManagementClient = new UserManagementClient(backEndUrl,
                                                        sessionCookie);
        infoServiceAdminClient = new InfoServiceAdminClient(backEndUrl,
                                                            sessionCookie);
        wsRegistryServiceClient = registryProviderUtil.getWSRegistry(automationContext);
        lifeCycleAdminServiceClient = new LifeCycleAdminServiceClient(backEndUrl,
                                                                      sessionCookie);
        lifeCycleManagementClient = new LifeCycleManagementClient(backEndUrl,
                                                                  sessionCookie);
        relationAdminServiceClient = new RelationAdminServiceClient(backEndUrl,
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

    @Test(groups = {"wso2.greg"}, description = "Test policy with UTF characters", dependsOnMethods = "testreadFile")
    public void testAddPolicy() throws Exception {
        policyPath = addPolicy("policy_" + utfString, "desc", POLICY_URL);
        wsdlPath = addWSDL("wsdl_", WSDL_URL);

        Assert.assertNotNull(policyPath);

    }

    @Test(groups = {"wso2.greg"}, description = "add dependency", dependsOnMethods = {"testAddPolicy"})
    public void testAddDependency() throws Exception {

        Assert.assertTrue(UTFSupport.addDependency(relationAdminServiceClient, pathPrefix + policyPath, pathPrefix + wsdlPath));
    }

    @Test(groups = {"wso2.greg"}, description = "add association", dependsOnMethods = {"testAddDependency"})
    public void testAddAssociation() throws Exception {
        Assert.assertTrue(UTFSupport.addAssociation(relationAdminServiceClient, pathPrefix + policyPath, pathPrefix + wsdlPath));
    }

    @Test(groups = {"wso2.greg"}, description = "create lifecycle", dependsOnMethods = {"testAddAssociation"})
    public void testCreateLifecycle() throws Exception {
        Assert.assertTrue(UTFSupport.createLifecycle(lifeCycleManagementClient, LC_NAME));
    }

    @Test(groups = {"wso2.greg"}, description = "create lifecycle", dependsOnMethods = {"testCreateLifecycle"})
    public void testAddLifecycle() throws Exception {
        Assert.assertTrue(UTFSupport.addLc(wsRegistryServiceClient, pathPrefix + policyPath, LC_NAME, lifeCycleAdminServiceClient));
    }

    @Test(groups = {"wso2.greg"}, description = "add comment", dependsOnMethods = {"testAddLifecycle"})
    public void testAddComment() throws Exception {
        Assert.assertTrue(UTFSupport.addComment(infoServiceAdminClient, utfString, pathPrefix + policyPath, automationContext));
    }

    @Test(groups = {"wso2.greg"}, description = "add subscription", dependsOnMethods = {"testAddRole"})
    public void testAddSubscription() throws Exception {
        Assert.assertTrue(UTFSupport.addSubscription(infoServiceAdminClient, pathPrefix + policyPath, utfString, automationContext));
    }


    @Test(groups = {"wso2.greg"}, description = "add role", dependsOnMethods = {"testAddComment"})
    public void testAddRole() throws Exception {
        Assert.assertTrue(UTFSupport.addRole(userManagementClient, utfString, automationContext));
    }

    @Test(groups = {"wso2.greg"}, description = "add tag", dependsOnMethods = {"testAddRole"})
    public void testAddTag() throws Exception {
        Assert.assertTrue(UTFSupport.addTag(infoServiceAdminClient, utfString, pathPrefix + policyPath, automationContext));
    }

    @Test(groups = "wso2.greg", description = "edit policy", dependsOnMethods = {"testAddTag"})
    public void testEditpolicy() throws RegistryException, AxisFault, RegistryExceptionException {

        Policy policy = policyManager.getPolicy(policyID);
        policy.addAttribute("overview_description", utfString);
        Assert.assertTrue((policy.getAttribute("overview_description")).equals(utfString));

    }

    public String addPolicy(String policyName, String desc, String url)
            throws MalformedURLException,
                   ResourceAdminServiceExceptionException, RemoteException, GovernanceException {

        resourceAdminServiceClient.addPolicy(policyName, desc, url);

        policyManager = new PolicyManager(governance);
        Policy[] policies = policyManager.getAllPolicies();
        String path = null;
        for (Policy policy : policies) {
            String name = policy.getQName().getLocalPart();
            if (name.equals(policyName + ".xml")) {
                path = policy.getPath();
                policyID = policy.getId();
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
        boolean wsdlAdded = false;
        for (Wsdl wsdl : wsdls) {
            String wsdlname = wsdl.getQName().getLocalPart();
            if (wsdlname.equals(wsdlName + utfString + ".wsdl")) {
                wsdlPath = wsdl.getPath();
                wsdlAdded = true;
            }
        }

        Assert.assertTrue(wsdlAdded);
        return wsdlPath;

    }

    @AfterClass
    public void clean() throws Exception {
        delete(pathPrefix + policyPath);
        policyManager = new PolicyManager(governance);
        Policy[] policies = policyManager.getAllPolicies();
        boolean policyDeleted = true;
        for (Policy policy : policies) {
            String path = policy.getPath();
            if (path.equals(policyPath)) {
                policyDeleted = false;
            }
        }
        Assert.assertTrue(policyDeleted);
        delete(pathPrefix + wsdlPath);
        delete("/_system/governance/trunk/services/com/amazon/soap/AmazonSearchService");
        userManagementClient.deleteRole(utfString);
        lifeCycleManagementClient.deleteLifeCycle(LC_NAME);
        delete("/_system/governance/trunk/endpoints/com");

        utfString = null;
        resourceAdminServiceClient=null;
        userManagementClient=null;
        policyManager=null;
        lifeCycleManagementClient=null;
        lifeCycleAdminServiceClient=null;
        governance=null;
        policyPath=null;
        wsdlPath=null;
        relationAdminServiceClient=null;
        infoServiceAdminClient=null;
        pathPrefix=null;
        wsRegistryServiceClient=null;
        registryProviderUtil=null;
    }

    public void delete(String destPath) throws ResourceAdminServiceExceptionException, RemoteException, RegistryException {
        if (wsRegistryServiceClient.resourceExists(destPath)) {
            resourceAdminServiceClient.deleteResource(destPath);
        }
    }

}