package org.wso2.carbon.registry.utfsupport.test;

import org.apache.axis2.AxisFault;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.governance.api.exception.GovernanceException;
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
import org.wso2.greg.integration.common.clients.*;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import java.io.IOException;
import java.rmi.RemoteException;

public class UTFSupportForWSDLTestCase extends GREGIntegrationBaseTest {

    private Registry governance;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private String utfString;
    private String wsdl1;
    private String wsdl2;
    private RelationAdminServiceClient relationAdminServiceClient;
    private final String WSDL_URL1 = "https://raw.githubusercontent.com/wso2/wso2-qa-artifacts/master/" +
            "automation-artifacts/greg/wsdl/AmazonWebServices.wsdl";

    private final String WSDL_URL2 = "https://raw.githubusercontent.com/wso2/wso2-qa-artifacts/master/" +
            "automation-artifacts/greg/wsdl/BizService.wsdl";

    private final String LC_NAME = "ÀÁÂÃÄÅÆÇÈÉ";
    private String pathPrefix = "/_system/governance";
    private LifeCycleManagementClient lifeCycleManagementClient;
    private WSRegistryServiceClient wsRegistryServiceClient;
    private RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
    private LifeCycleAdminServiceClient lifeCycleAdminServiceClient;
    private InfoServiceAdminClient infoServiceAdminClient;
    private String currentWSDLid;
    private WsdlManager wsdlManager;
    private UserManagementClient userManagementClient;

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
    public void testReadFile() throws IOException {
        utfString = UTFSupport.readFile();

    }

    @Test(groups = {"wso2.greg"}, description = "add wsdl", dependsOnMethods = "testReadFile")
    public void testAddWSDL() throws ResourceAdminServiceExceptionException,
                                     RemoteException, GovernanceException {
        wsdl1 = addWSDL("wsdl1_", WSDL_URL1);
        wsdl2 = addWSDL("wsdl2_", WSDL_URL2);

    }

    @Test(groups = {"wso2.greg"}, description = "add dependency to the wsdl", dependsOnMethods = {"testAddWSDL"})
    public void testAddWSDLDependency() throws Exception {

        Assert.assertTrue(UTFSupport.addDependency(relationAdminServiceClient, pathPrefix + wsdl1, pathPrefix + wsdl2));
    }

    @Test(groups = {"wso2.greg"}, description = "add association to the wsdl", dependsOnMethods = {"testAddWSDLDependency"})
    public void testAddWSDLAssociation() throws Exception {
        Assert.assertTrue(UTFSupport.addAssociation(relationAdminServiceClient, pathPrefix + wsdl1, pathPrefix + wsdl2));
    }

    @Test(groups = {"wso2.greg"}, description = "create lifecycle", dependsOnMethods = {"testAddWSDLAssociation"})
    public void testCreateLifecycle() throws Exception {

        Assert.assertTrue(UTFSupport.createLifecycle(lifeCycleManagementClient, LC_NAME));

    }

    @Test(groups = "wso2.greg", description = "Add lifecycle to a wsdl", dependsOnMethods = {"testCreateLifecycle"})
    public void testAddLcToWSDL() throws ResourceAdminServiceExceptionException,
                                         RegistryException,
                                         ListMetadataServiceRegistryExceptionException,
                                         CustomLifecyclesChecklistAdminServiceExceptionException,
                                         RemoteException {
        Assert.assertTrue(UTFSupport.addLc(wsRegistryServiceClient, pathPrefix + wsdl1,
                                           LC_NAME, lifeCycleAdminServiceClient));

    }

    @Test(groups = "wso2.greg", description = "Add comment to a wsdl", dependsOnMethods = {"testAddLcToWSDL"})
    public void testAddCommentToWSDL()
            throws Exception {

        Assert.assertTrue(UTFSupport.addComment(infoServiceAdminClient, utfString, pathPrefix + wsdl1, automationContext));
    }

    @Test(groups = "wso2.greg", description = "Add subscription to a wsdl", dependsOnMethods = {"testAddCommentToWSDL"})
    public void testAddRole() throws Exception {
        Assert.assertTrue(UTFSupport.addRole(userManagementClient, utfString, automationContext));
    }

    @Test(groups = "wso2.greg", description = "Add subscription to a wsdl", dependsOnMethods = {"testAddRole"})
    public void testAddSubscriptionToWSDL() throws Exception {

        Assert.assertTrue(UTFSupport.addSubscription(infoServiceAdminClient, pathPrefix + wsdl1, utfString, automationContext));
    }

    @Test(groups = "wso2.greg", description = "Add tage to a wsdl", dependsOnMethods = {"testAddSubscriptionToWSDL"})
    public void testAddTagToWSDL() throws Exception {

        Assert.assertTrue(UTFSupport.addTag(infoServiceAdminClient, utfString, pathPrefix + wsdl1, automationContext));
    }

    @Test(groups = "wso2.greg", description = "edit wsdl", dependsOnMethods = {"testAddTagToWSDL"})
    public void testEditWSDL() throws RegistryException, AxisFault, RegistryExceptionException {

        Wsdl wsdl = wsdlManager.getWsdl(currentWSDLid);
        wsdl.addAttribute("overview_description", utfString);
        Assert.assertTrue((wsdl.getAttribute("overview_description")).equals(utfString));

    }


    public String addWSDL(String wsdlPrefix, String url)
            throws ResourceAdminServiceExceptionException,
                   RemoteException, GovernanceException {

        resourceAdminServiceClient.addWSDL(wsdlPrefix + utfString, "desc", url);

        wsdlManager = new WsdlManager(governance);
        Wsdl[] wsdls;
        wsdls = wsdlManager.getAllWsdls();
        String wsdlPath = null;
        boolean wsdlAdded = false;
        for (Wsdl wsdl : wsdls) {
            String wsdlname = wsdl.getQName().getLocalPart();
            if (wsdlname.equals(wsdlPrefix + utfString + ".wsdl")) {
                wsdlPath = wsdl.getPath();
                currentWSDLid = wsdl.getId();
                wsdlAdded = true;
            }
        }

        Assert.assertTrue(wsdlAdded);
        return wsdlPath;

    }

    @AfterClass(alwaysRun = true)
    public void testDeleteWSDL() throws Exception {

        delete(pathPrefix + wsdl1);

        wsdlManager = new WsdlManager(governance);
        Wsdl[] wsdls;
        wsdls = wsdlManager.getAllWsdls();
        boolean wsdldeleted = true;
        for (Wsdl wsdl : wsdls) {
            String wsdlname = wsdl.getQName().getLocalPart();
            if (wsdlname.equals("wsdl1_" + utfString + ".wsdl")) {
                wsdldeleted = false;
            }
        }
        Assert.assertTrue(wsdldeleted);
        delete(pathPrefix + wsdl2);
        delete("/_system/governance/trunk/services/com/amazon/soap/1.0.0/AmazonSearchService");
        delete("/_system/governance/trunk/services/com/foo/1.0.0/BizService");
        delete("/_system/governance/trunk/schemas/org/bar/purchasing/1.0.0/purchasing.xsd");
        userManagementClient.deleteRole(utfString);
        lifeCycleManagementClient.deleteLifeCycle(LC_NAME);
        delete("/_system/governance/trunk/endpoints/com/amazon/soap/onca/ep-soap2");
        delete("/_system/governance/trunk/endpoints/com");
        delete("/_system/governance/trunk/endpoints/localhost");

        utfString = null;
        wsdl1 = null;
        wsdl2 = null;
        resourceAdminServiceClient = null;
        relationAdminServiceClient = null;
        registryProviderUtil = null;
        wsRegistryServiceClient = null;
        lifeCycleManagementClient = null;
        lifeCycleAdminServiceClient = null;
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