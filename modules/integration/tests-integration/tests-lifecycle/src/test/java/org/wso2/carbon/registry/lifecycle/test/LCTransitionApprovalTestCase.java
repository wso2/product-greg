package org.wso2.carbon.registry.lifecycle.test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.RemoteException;

import javax.activation.DataHandler;
import javax.xml.stream.XMLStreamException;
import javax.xml.xpath.XPathExpressionException;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.test.utils.common.FileManager;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.beans.xsd.LifecycleBean;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.governance.list.stub.ListMetadataServiceRegistryExceptionException;
import org.wso2.carbon.governance.services.stub.AddServicesServiceRegistryExceptionException;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.common.xsd.ResourceData;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.registry.core.session.UserRegistry;

import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.greg.integration.common.clients.*;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

public class LCTransitionApprovalTestCase extends GREGIntegrationBaseTest{
	
	
    private WSRegistryServiceClient wsRegistryServiceClient;
    private LifeCycleAdminServiceClient lifeCycleAdminServiceClient;
    private LifeCycleManagementClient lifeCycleManagementClient;
    private GovernanceServiceClient governanceServiceClient;
    private ListMetaDataServiceClient listMetadataServiceClient;
    private ResourceAdminServiceClient resourceAdminServiceClient;
	
    private ServiceManager serviceManager;

    private static final String SERVICE_NAME = "IntergalacticService";
    private static final String LC_NAME = "TransitionApprovalLC";
    private static final String ACTION_PROMOTE = "Promote";
    private static final String GOV_PATH = "/_system/governance";
    private String serviceString = "/trunk/services/com/abb/1.0.0-SNAPSHOT/IntergalacticService";
    private final String absPath = GOV_PATH + serviceString;

	
    private static final String ACTION_VOTE_CLICK = "voteClick";
    private LifecycleBean lifeCycle;
    private RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();

    /**
     * @throws RemoteException
     * @throws LoginAuthenticationExceptionException
     *
     * @throws RegistryException
     */
    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_USER);
        String sessionCookie = new LoginLogoutClient(automationContext).login();

        lifeCycleAdminServiceClient =
                new LifeCycleAdminServiceClient(backendURL,
                                                sessionCookie);
        governanceServiceClient =
                new GovernanceServiceClient(backendURL,
                                            sessionCookie);
        listMetadataServiceClient =
                new ListMetaDataServiceClient(backendURL,
                                              sessionCookie);
        lifeCycleManagementClient =
                new LifeCycleManagementClient(backendURL,
                                              sessionCookie);

        resourceAdminServiceClient =
                new ResourceAdminServiceClient(backendURL,
                                               sessionCookie);
        wsRegistryServiceClient =
                registryProviderUtil.getWSRegistry(automationContext);
	
	    Registry reg = registryProviderUtil.getGovernanceRegistry(new RegistryProviderUtil()
            .getWSRegistry(automationContext), automationContext);

        GovernanceUtils.loadGovernanceArtifacts((UserRegistry)reg);
        serviceManager = new ServiceManager(reg);

    }

    /**
     * @throws XMLStreamException
     * @throws IOException
     * @throws AddServicesServiceRegistryExceptionException
     *
     * @throws ListMetadataServiceRegistryExceptionException
     *
     * @throws ResourceAdminServiceExceptionException
     *
     */
    @Test(groups = "wso2.greg", description = "Create a service")
    public void testCreateService() throws XMLStreamException, IOException,
                                           AddServicesServiceRegistryExceptionException,
                                           ListMetadataServiceRegistryExceptionException,
                                           ResourceAdminServiceExceptionException {

        String servicePath =
                getTestArtifactLocation() + "artifacts" +
                File.separator + "GREG" + File.separator + "services" +
                File.separator + "intergalacticService.metadata.xml";
        DataHandler dataHandler = new DataHandler(new URL("file:///" + servicePath));
        String mediaType = "application/vnd.wso2-service+xml";
        String description = "This is a test service";
        resourceAdminServiceClient.addResource(
                "/_system/governance/service", mediaType, description, dataHandler);

        ResourceData[] data =  resourceAdminServiceClient.getResource(GOV_PATH + serviceString);
        
        assertNotNull(data, "Service not found");

    }

    /**
     * @throws LifeCycleManagementServiceExceptionException
     *
     * @throws IOException
     * @throws InterruptedException
     */
    @Test(groups = "wso2.greg", description = "Create new life cycle", dependsOnMethods = "testCreateService")
    public void testCreateNewLifeCycle() throws LifeCycleManagementServiceExceptionException,
                                                IOException, InterruptedException {
        String resourcePath =
                getTestArtifactLocation() + "artifacts" +
                File.separator + "GREG" + File.separator + "lifecycle" +
                File.separator + "TransitionTestLC.xml";
        String lifeCycleContent = FileManager.readFile(resourcePath);
        lifeCycleManagementClient.addLifeCycle(lifeCycleContent);
        String[] lifeCycles = lifeCycleManagementClient.getLifecycleList();

        boolean lcStatus = false;
        for (String lc : lifeCycles) {

            if (lc.equalsIgnoreCase(LC_NAME)) {
                lcStatus = true;
            }
        }
        assertTrue(lcStatus, "LifeCycle not found");

    }

    /**
     * @throws RegistryException
     * @throws RemoteException
     * @throws CustomLifecyclesChecklistAdminServiceExceptionException
     *
     * @throws ListMetadataServiceRegistryExceptionException
     *
     * @throws ResourceAdminServiceExceptionException
     *
     */
    @Test(groups = "wso2.greg", description = "Add lifecycle to a service", dependsOnMethods = "testCreateNewLifeCycle")
    public void testAddLcToService() throws RegistryException, RemoteException,
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
            }
        }
        assertTrue(lcStatus, "LifeCycle not added to service");
    }
    
    
    /**
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "LC Transition Event Approval(Tick)", dependsOnMethods = "testAddLcToService")
    public void testLCTransitionApproval() throws Exception {
        lifeCycleAdminServiceClient.invokeAspect(absPath, LC_NAME,
        		ACTION_VOTE_CLICK, new String[]{"true", "true"});

        lifeCycle =
                lifeCycleAdminServiceClient.getLifecycleBean(absPath);
        for (Property prop : lifeCycle.getLifecycleApproval()) {        	
            if (prop.getKey().contains("registry.custom_lifecycle.votes.option") &&
                !prop.getKey().contains("permission")) {            	
                assertEquals(prop.getValues()[4], "current:1", "Not Approved");
            }
        }
    }

    /**
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Remove LC Transition Event Approval(Untick)", dependsOnMethods = "testLCTransitionApproval")
    public void testLCTransitionApprovalWithDrown() throws Exception {
        lifeCycleAdminServiceClient.invokeAspect(absPath, LC_NAME,
        		ACTION_VOTE_CLICK, new String[]{"false", "false"});

        lifeCycle =
                lifeCycleAdminServiceClient.getLifecycleBean(absPath);
        for (Property prop : lifeCycle.getLifecycleApproval()) {        	
            if (prop.getKey().contains("registry.custom_lifecycle.votes.option") &&
                !prop.getKey().contains("permission")) {            	
                assertEquals(prop.getValues()[4], "current:0", "Not Approved");
            }
        }
    }
    

    /**
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Try to Promote without Votes",dependsOnMethods = "testLCTransitionApprovalWithDrown",           expectedExceptions = org.apache.axis2.AxisFault.class)
    public void testLCInvalidePromote() throws Exception {
        lifeCycleAdminServiceClient.invokeAspect(absPath, LC_NAME,
                                                 ACTION_PROMOTE, null);

        lifeCycle =
                lifeCycleAdminServiceClient.getLifecycleBean(absPath);
        for (Property prop : lifeCycle.getLifecycleApproval()) {        	
            if (prop.getKey().contains("registry.custom_lifecycle.votes.option") &&
                !prop.getKey().contains("permission")) {            	
                assertNotNull(prop, "Promoted without Approval");
            }
        }
    }

    /**
     * @throws Exception
     */
    @AfterClass()
    public void clear() throws Exception {
        String servicePathToDelete = absPath;
        if (wsRegistryServiceClient.resourceExists(servicePathToDelete)) {
            resourceAdminServiceClient.deleteResource(servicePathToDelete);
        }
   /*     String schemaPathToDelete = "/_system/governance/trunk/schemas/org/bar/purchasing/purchasing.xsd";
        if (wsRegistryServiceClient.resourceExists(schemaPathToDelete)) {
            resourceAdminServiceClient.deleteResource(schemaPathToDelete);
        }
        String wsdlPathToDelete = "/_system/governance/trunk/wsdls/com/foo/IntergalacticService.wsdl";
        if (wsRegistryServiceClient.resourceExists(wsdlPathToDelete)) {
            resourceAdminServiceClient.deleteResource(wsdlPathToDelete);
        }*/
        lifeCycleManagementClient.deleteLifeCycle(LC_NAME);

        governanceServiceClient = null;
        wsRegistryServiceClient = null;
        lifeCycleAdminServiceClient = null;
        lifeCycleManagementClient = null;
        governanceServiceClient = null;
        listMetadataServiceClient = null;
        resourceAdminServiceClient = null;
        resourceAdminServiceClient = null;
    }
}
