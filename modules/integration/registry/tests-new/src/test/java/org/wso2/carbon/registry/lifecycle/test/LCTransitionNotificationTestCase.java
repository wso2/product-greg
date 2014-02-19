package org.wso2.carbon.registry.lifecycle.test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;

import javax.activation.DataHandler;
import javax.xml.stream.XMLStreamException;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.governance.GovernanceServiceClient;
import org.wso2.carbon.automation.api.clients.governance.HumanTaskAdminClient;
import org.wso2.carbon.automation.api.clients.governance.LifeCycleAdminServiceClient;
import org.wso2.carbon.automation.api.clients.governance.LifeCycleManagementClient;
import org.wso2.carbon.automation.api.clients.governance.ListMetaDataServiceClient;
import org.wso2.carbon.automation.api.clients.governance.WorkItem;
import org.wso2.carbon.automation.api.clients.registry.InfoServiceAdminClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.api.clients.user.mgt.UserManagementClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.core.utils.fileutils.FileManager;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.beans.xsd.LifecycleBean;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.governance.list.stub.ListMetadataServiceRegistryExceptionException;
import org.wso2.carbon.governance.list.stub.beans.xsd.ServiceBean;
import org.wso2.carbon.governance.services.stub.AddServicesServiceRegistryExceptionException;
import org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault;
import org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault;
import org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.info.stub.RegistryExceptionException;
import org.wso2.carbon.registry.info.stub.beans.xsd.SubscriptionBean;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.common.xsd.ResourceData;
import org.wso2.carbon.registry.subscription.test.util.WorkItemClient;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.governance.api.exception.GovernanceException;

import org.wso2.carbon.governance.api.util.GovernanceUtils;


public class LCTransitionNotificationTestCase {

	private int userId = 2;
	UserInfo userInfo = UserListCsvReader.getUserInfo(userId);
	private WSRegistryServiceClient wsRegistryServiceClient;
	private LifeCycleAdminServiceClient lifeCycleAdminServiceClient;
	private LifeCycleManagementClient lifeCycleManagementClient;
	private GovernanceServiceClient governanceServiceClient;
	private ListMetaDataServiceClient listMetadataServiceClient;
	private ResourceAdminServiceClient resourceAdminServiceClient;
	private UserManagementClient userManagementClient;
	private InfoServiceAdminClient infoServiceAdminClient;
	private HumanTaskAdminClient humanTaskAdminClient;
	private ManageEnvironment environment;
	private ServiceManager serviceManager;

	private static final String SERVICE_NAME = "IntergalacticService9";
	private static final String LC_NAME = "TransitionApprovalLC";
	private static final String ACTION_PROMOTE = "Promote";

	private static final String ACTION_VOTE_CLICK = "voteClick";

	private static final String GOV_PATH = "/_system/governance";
	private String serviceString = "/trunk/services/com/abb/IntergalacticService9";
        private final String absPath = GOV_PATH + serviceString;

	private LifecycleBean lifeCycle;
	private RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();

	/**
	 * @throws RemoteException
	 * @throws LoginAuthenticationExceptionException
	 * 
	 * @throws RegistryException
	 */
	@BeforeClass(alwaysRun = true)
	public void init() throws RemoteException,
			LoginAuthenticationExceptionException, RegistryException {
		EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
		environment = builder.build();

		lifeCycleAdminServiceClient = new LifeCycleAdminServiceClient(
				environment.getGreg().getProductVariables().getBackendUrl(),
				environment.getGreg().getSessionCookie());
		userManagementClient = new UserManagementClient(environment.getGreg()
				.getProductVariables().getBackendUrl(), environment.getGreg()
				.getSessionCookie());
		governanceServiceClient = new GovernanceServiceClient(environment
				.getGreg().getProductVariables().getBackendUrl(), environment
				.getGreg().getSessionCookie());
		listMetadataServiceClient = new ListMetaDataServiceClient(environment
				.getGreg().getProductVariables().getBackendUrl(), environment
				.getGreg().getSessionCookie());
		lifeCycleManagementClient = new LifeCycleManagementClient(environment
				.getGreg().getProductVariables().getBackendUrl(), environment
				.getGreg().getSessionCookie());

		infoServiceAdminClient = new InfoServiceAdminClient(environment
				.getGreg().getProductVariables().getBackendUrl(),
				userInfo.getUserName(), userInfo.getPassword());

		humanTaskAdminClient = new HumanTaskAdminClient(environment.getGreg()
				.getBackEndUrl(), userInfo.getUserName(),
				userInfo.getPassword());

		resourceAdminServiceClient = new ResourceAdminServiceClient(environment
				.getGreg().getProductVariables().getBackendUrl(), environment
				.getGreg().getSessionCookie());
		wsRegistryServiceClient = registryProviderUtil.getWSRegistry(userId,
				ProductConstant.GREG_SERVER_NAME);
		Registry reg = registryProviderUtil.getGovernanceRegistry(new RegistryProviderUtil().getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME), userId);
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

		String servicePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION
				+ "artifacts" + File.separator + "GREG" + File.separator
				+ "services" + File.separator
				+ "intergalacticService9.metadata.xml";
		DataHandler dataHandler = new DataHandler(new URL("file:///"
				+ servicePath));
		String mediaType = "application/vnd.wso2-service+xml";
		String description = "This is a test service";
		resourceAdminServiceClient.addResource("/_system/governance/service",mediaType, description, dataHandler);

		ResourceData[] data = resourceAdminServiceClient.getResource(absPath);

		assertNotNull(data, "Service not found");

	}

	/**
	 * @throws LifeCycleManagementServiceExceptionException
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test(groups = "wso2.greg", description = "Create new life cycle", dependsOnMethods = "testCreateService")
	public void testCreateNewLifeCycle()
			throws LifeCycleManagementServiceExceptionException, IOException,
			InterruptedException {
		String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION
				+ "artifacts" + File.separator + "GREG" + File.separator
				+ "lifecycle" + File.separator + "TransitionTestLC.xml";
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
	 * @throws Exception 
	 * 
	 */
	@Test(groups = "wso2.greg", description = "Subscribe LC Approval Needed notification while state change", dependsOnMethods = "testCreateNewLifeCycle")
	public void testSubscribeLCApprovalNeededNotification() throws Exception, GovernanceException {
		
		addRole();
		assertTrue(consoleSubscribe(absPath,	"LifeCycleApprovalNeeded"));
	}

	/**
	 * @throws Exception
	 * 
	 */
	@Test(groups = "wso2.greg", description = "Add lifecycle to a service", dependsOnMethods = "testSubscribeLCApprovalNeededNotification")
	public void testAddLcToService() throws Exception {

		wsRegistryServiceClient.associateAspect(absPath, LC_NAME);
		lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(absPath);

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
	 * 
	 * @throws RemoteException
	 * @throws IllegalStateFault
	 * @throws IllegalAccessFault
	 * @throws IllegalArgumentFault
	 * @throws InterruptedException
	 * @throws RegistryException
	 * @throws RegistryExceptionException
	 */
	@Test(groups = "wso2.greg", description = "Check LC Approval Needed notification is recived", dependsOnMethods = "testAddLcToService")
	public void testLCApprovalNeededNotification() throws RemoteException, IllegalStateFault, IllegalAccessFault, IllegalArgumentFault, InterruptedException, RegistryException, RegistryExceptionException{
		assertTrue(getNotification("The LifeCycle was created and some transitions are awating for approval, resource locate at " + absPath));
		assertTrue(managementUnsubscribe(absPath));
	}
	
	/**
	 * 
	 * @throws RemoteException
	 * @throws IllegalStateFault
	 * @throws IllegalAccessFault
	 * @throws IllegalArgumentFault
	 * @throws InterruptedException
	 * @throws RegistryException
	 * @throws RegistryExceptionException
	 */
	@Test(groups = "wso2.greg", description = "Subscribe LC Approved notification ", dependsOnMethods = "testLCApprovalNeededNotification")
	public void testSubscribeLCApprovedNotification() throws RemoteException, IllegalStateFault, IllegalAccessFault, IllegalArgumentFault, InterruptedException, RegistryException, RegistryExceptionException{
		assertTrue(consoleSubscribe(absPath,	"LifeCycleApproved"));
	}


	/**
	 * @throws Exception
	 */
	@Test(groups = "wso2.greg", description = "LifeCycle Transition Event Approval(Tick)", dependsOnMethods = "testSubscribeLCApprovedNotification")
	public void testLCTransitionApproval() throws Exception {
		lifeCycleAdminServiceClient.invokeAspect(absPath, LC_NAME, ACTION_VOTE_CLICK, new String[] {
				"true", "true" });

		lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(absPath);
		for (Property prop : lifeCycle.getLifecycleApproval()) {
			if (prop.getKey().contains("registry.custom_lifecycle.votes.option") && !prop.getKey().contains("permission")) {
				System.out.println(prop.getValues());
				for (String value : prop.getValues()) {
					if(value.startsWith("current")){
						assertEquals(value, "current:1", "Not Approved");
					}
				}
			}
		}		
	}
	
	/**
	 * 
	 * @throws RemoteException
	 * @throws IllegalStateFault
	 * @throws IllegalAccessFault
	 * @throws IllegalArgumentFault
	 * @throws InterruptedException
	 * @throws RegistryException
	 * @throws RegistryExceptionException
	 */
	@Test(groups = "wso2.greg", description = "Check LifeCycle Approved notification is recived", dependsOnMethods = "testLCTransitionApproval")
	public void testLCApprovedNotification() throws RemoteException, IllegalStateFault, IllegalAccessFault, IllegalArgumentFault, InterruptedException, RegistryException, RegistryExceptionException{
		assertTrue(getNotification("LifeCycle State 'Commencement', transitions event 'Abort' was approved for resource at " +  absPath));
		assertTrue(managementUnsubscribe(absPath));
	}
	
	/**
	 * 
	 * @throws RemoteException
	 * @throws IllegalStateFault
	 * @throws IllegalAccessFault
	 * @throws IllegalArgumentFault
	 * @throws InterruptedException
	 * @throws RegistryException
	 * @throws RegistryExceptionException
	 */
	@Test(groups = "wso2.greg", description = "Subscribe LifeCycle Approval Withdrawn notification ", dependsOnMethods = "testLCApprovedNotification")
	public void testSubscribeLCApprovalWithdrawnNotification() throws RemoteException, IllegalStateFault, IllegalAccessFault, IllegalArgumentFault, InterruptedException, RegistryException, RegistryExceptionException{
		assertTrue(consoleSubscribe(absPath,	"LifeCycleApprovalWithdrawn"));
	}

	/**
	 * @throws Exception
	 */
	@Test(groups = "wso2.greg", description = "Remove LC Transition Event Approval(Untick)", dependsOnMethods = "testSubscribeLCApprovalWithdrawnNotification")
	public void testLCTransitionApprovalWithDrawn() throws Exception {

		lifeCycleAdminServiceClient.invokeAspect(absPath, LC_NAME, ACTION_VOTE_CLICK, new String[] {
				"false", "false" });

		lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(absPath);
		for (Property prop : lifeCycle.getLifecycleApproval()) {
			if (prop.getKey() .contains("registry.custom_lifecycle.votes.option") && !prop.getKey().contains("permission")) {
				for (String value : prop.getValues()) {
					if(value.startsWith("current")){
						assertEquals(value, "current:0", "Not Approved");
					}
				}
			}
		}
	}
	
	/**
	 * 
	 * @throws RemoteException
	 * @throws IllegalStateFault
	 * @throws IllegalAccessFault
	 * @throws IllegalArgumentFault
	 * @throws InterruptedException
	 * @throws RegistryException
	 * @throws RegistryExceptionException
	 */
	@Test(groups = "wso2.greg", description = "Check LC Approval WithDrown notification is recived", dependsOnMethods = "testLCTransitionApprovalWithDrawn")
	public void testLCApprovalWithdrawnNotification() throws RemoteException, IllegalStateFault, IllegalAccessFault, IllegalArgumentFault, InterruptedException, RegistryException, RegistryExceptionException{
		assertTrue(getNotification("LifeCycle State 'Commencement' transitions event 'Abort' approvel was removed for resource at " + absPath));
		assertTrue(managementUnsubscribe(absPath));
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
//		String schemaPathToDelete = "/_system/governance/trunk/schemas/org/bar/purchasing/purchasing.xsd";
//		if (wsRegistryServiceClient.resourceExists(schemaPathToDelete)) {
//			resourceAdminServiceClient.deleteResource(schemaPathToDelete);
//		}
//		String wsdlPathToDelete = "/_system/governance/trunk/wsdls/com/foo/IntergalacticService.wsdl";
//		if (wsRegistryServiceClient.resourceExists(wsdlPathToDelete)) {
//			resourceAdminServiceClient.deleteResource(wsdlPathToDelete);
//		}
		lifeCycleManagementClient.deleteLifeCycle(LC_NAME);

		wsRegistryServiceClient = null;
		lifeCycleAdminServiceClient = null;
		lifeCycleManagementClient = null;
		governanceServiceClient = null;
		listMetadataServiceClient = null;
		resourceAdminServiceClient = null;
		userManagementClient = null;
		infoServiceAdminClient = null;
		humanTaskAdminClient = null;
	}
	
	
	private boolean addRole() throws Exception {

		if (!userManagementClient.roleNameExists("RoleSubscriptionTest")) {
			userManagementClient.addRole("RoleSubscriptionTest",
					new String[] { userInfo.getUserNameWithoutDomain() },
					new String[] { "" });
		}
		return userManagementClient.roleNameExists("RoleSubscriptionTest");
	}

	private boolean consoleSubscribe(String path, String eventType)
			throws RemoteException, RegistryException {

		// subscribe for management console notifications
		SubscriptionBean bean = infoServiceAdminClient.subscribe(path,
				"work://RoleSubscriptionTest", eventType, environment.getGreg().getSessionCookie());
		return bean.getSubscriptionInstances() != null;

	}
	
	private boolean getNotification(String type) throws RemoteException,
			IllegalStateFault, IllegalAccessFault, IllegalArgumentFault,
			InterruptedException {
		boolean success = false;
		Thread.sleep(3000);// force delay otherwise getWorkItems return null
		// get all the management console notifications
		WorkItem[] workItems = WorkItemClient.getWorkItems(humanTaskAdminClient);
		for (WorkItem workItem : workItems) {
			// search for the correct notification
			if ((workItem.getPresentationSubject().toString()).contains(type)) {
				success = true;
				break;
			}
		}
		workItems = null;
		return success;
	}
	
	public boolean managementUnsubscribe(String path) throws RegistryException, RegistryExceptionException, RemoteException {

        String sessionID = environment.getGreg().getSessionCookie();
        SubscriptionBean sBean = infoServiceAdminClient.getSubscriptions(path, sessionID);
        infoServiceAdminClient.unsubscribe(path, sBean.getSubscriptionInstances()[0].getId(),
                                           sessionID);
        sBean = infoServiceAdminClient.getSubscriptions(path, sessionID);
        return (sBean.getSubscriptionInstances() == null);
    }


}
