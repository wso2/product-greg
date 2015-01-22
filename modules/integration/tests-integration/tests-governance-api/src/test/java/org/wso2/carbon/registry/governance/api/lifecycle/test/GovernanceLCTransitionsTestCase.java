package org.wso2.carbon.registry.governance.api.lifecycle.test;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.services.ServiceFilter;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.integration.common.utils.FileManager;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.clients.LifeCycleManagementClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import javax.xml.namespace.QName;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class GovernanceLCTransitionsTestCase extends GREGIntegrationBaseTest {
    private String LIFE_CYCLE_NAME = "StoreServiceLifeCycle";
    private Registry governance;
    private ServiceManager serviceManager;
    private Service service;

    @BeforeClass(alwaysRun = true)
    public void initialize() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        WSRegistryServiceClient wsRegistry =
                new RegistryProviderUtil().getWSRegistry(automationContext);
        governance = new RegistryProviderUtil().getGovernanceRegistry(wsRegistry, automationContext);
        serviceManager = new ServiceManager(governance);
        service = serviceManager.newService(new QName("https://www.wso2.com/greg/store", "StoreService")) ;
        service.addAttribute("overview_version", "1.0.0");
        serviceManager.addService(service);

        LifeCycleManagementClient lifeCycleManagementClient =
                new LifeCycleManagementClient(getBackendURL(), getSessionCookie());

        String resourcePath =
                FrameworkPathUtil.getSystemResourceLocation() + "artifacts" +
                        File.separator + "GREG" + File.separator + "lifecycle" +
                        File.separator + "StoreServiceLifeCycle.xml";
        String lifeCycleContent = FileManager.readFile(resourcePath);
        lifeCycleManagementClient.addLifeCycle(lifeCycleContent);
    }

    @Test(groups = {"wso2.greg"}, description = "LC Transition")
    public void testAttachLifecycle() throws RegistryException {
        Service addedService = getAddedService();
        addedService.attachLifecycle(LIFE_CYCLE_NAME);
        String lifecycleName = addedService.getLifecycleName();
        String lifecycleState = addedService.getLifecycleState();

        Assert.assertEquals(lifecycleName, LIFE_CYCLE_NAME, "Different lifecycle found");
        Assert.assertEquals(lifecycleState, "Development", "Different lifecycle state found");
    }

    @Test(groups = {"wso2.greg"}, description = "LC Transition", dependsOnMethods = "testAttachLifecycle")
    public void testGetCheckListItems() throws RegistryException {
        Service service = getAddedService();
        String[] checklistItems = service.getAllCheckListItemNames(LIFE_CYCLE_NAME);

        Assert.assertEquals(checklistItems[0], "Code Completed", "Expected checklist item not found");
        Assert.assertEquals(checklistItems[1], "WSDL, Schema Created", "Expected checklist item not found");
        Assert.assertEquals(checklistItems[2], "QoS Created", "Expected checklist item not found");
    }

    @Test(groups = {"wso2.greg"}, description = "LC Transition", dependsOnMethods = "testGetCheckListItems")
    public void testCheckLCItem() throws RegistryException {
        Service service = getAddedService();

        service.checkLCItem(0,LIFE_CYCLE_NAME);
        service.checkLCItem(1,LIFE_CYCLE_NAME);
        service.checkLCItem(2,LIFE_CYCLE_NAME);

        Assert.assertTrue(service.isLCItemChecked(0,LIFE_CYCLE_NAME), "Lifecycle item not checked");
        Assert.assertTrue(service.isLCItemChecked(1,LIFE_CYCLE_NAME), "Lifecycle item not checked");
        Assert.assertTrue(service.isLCItemChecked(2,LIFE_CYCLE_NAME), "Lifecycle item not checked");

        service.uncheckLCItem(0,LIFE_CYCLE_NAME);

        Assert.assertFalse(service.isLCItemChecked(0,LIFE_CYCLE_NAME), "Lifecycle item unchecked");
    }

    @Test(groups = {"wso2.greg"}, description = "LC Transition", dependsOnMethods = "testCheckLCItem")
    public void testGetVotingEvents() throws RegistryException {
        Service service = getAddedService();

        String[] votingItems = service.getAllVotingItems();
        Assert.assertEquals(votingItems[0], "Promote", "Unexpected voting event found");
    }

    @Test(groups = {"wso2.greg"}, description = "LC Transition", dependsOnMethods = "testGetVotingEvents")
    public void testVoting() throws RegistryException {
        Service service = getAddedService();

        service.vote(0);
        Assert.assertTrue(service.isVoted(0), "Not voted");

        service.unvote(0);
        Assert.assertFalse(service.isVoted(0), "Vote not reverted");

        service.vote(0);
    }

    @Test(groups = {"wso2.greg"}, description = "LC Transition", dependsOnMethods = "testVoting")
    public void testGetAllActions() throws RegistryException {
        Service service = getAddedService();

        String[] actions = service.getAllLifecycleActions(LIFE_CYCLE_NAME);
        Assert.assertEquals(actions[0], "Promote", "Unexpected action found");
    }

    @Test(groups = {"wso2.greg"}, description = "LC Transition", dependsOnMethods = "testGetAllActions")
    public void testPromote() throws RegistryException {
        Service[] services = serviceManager.getAllServices();
        Service service = getAddedService();

        Map<String, String> map = new HashMap<String, String>();
        map.put("/_system/governance/trunk/services/com/wso2/www/greg/store/1.0.0/StoreService", "2.3.5");
        service.invokeAction("Promote", map, LIFE_CYCLE_NAME);


        services = serviceManager.findServices(new ServiceFilter() {
            public boolean matches(Service service) throws GovernanceException {
                String version = service.getAttribute("overview_version");
                if(version.equals("2.3.5")){
                    return true;
                }
                return false;
            }
        });

        Assert.assertEquals(services[0].getQName().getLocalPart(), "StoreService", "New storage service version not created");
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws RegistryException {
        if (service != null){
            deleteService(service);
        }
    }

    private Service getAddedService() throws RegistryException {
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry)governance);
        return serviceManager.getService(service.getId());
    }

    private void deleteService(Service service) throws RegistryException {
        if (service != null) {
            if (governance.resourceExists(service.getPath())) {
                serviceManager.removeService(service.getId());
            }
        }
    }

}
