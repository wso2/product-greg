/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.registry.governance.api.test;

import org.apache.axis2.AxisFault;
import org.apache.axis2.databinding.ADBException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.governance.api.endpoints.EndpointManager;
import org.wso2.carbon.governance.api.endpoints.dataobjects.Endpoint;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.policies.PolicyManager;
import org.wso2.carbon.governance.api.policies.dataobjects.Policy;
import org.wso2.carbon.governance.api.schema.SchemaManager;
import org.wso2.carbon.governance.api.schema.dataobjects.Schema;
import org.wso2.carbon.governance.api.services.ServiceFilter;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault;
import org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault;
import org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.info.stub.RegistryExceptionException;
import org.wso2.carbon.registry.info.stub.beans.xsd.SubscriptionBean;
import org.wso2.carbon.registry.properties.stub.PropertiesAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.properties.stub.beans.xsd.RetentionBean;
import org.wso2.carbon.registry.relations.stub.AddAssociationRegistryExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.beans.xsd.VersionPath;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.clients.HumanTaskAdminClient;
import org.wso2.greg.integration.common.clients.InfoServiceAdminClient;
import org.wso2.greg.integration.common.clients.LifeCycleAdminServiceClient;
import org.wso2.greg.integration.common.clients.PropertiesAdminServiceClient;
import org.wso2.greg.integration.common.clients.RelationAdminServiceClient;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.clients.WorkItem;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;
import org.wso2.greg.integration.common.utils.subscription.WorkItemClient;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class ServicesResourceInformationManagementTestCase extends GREGIntegrationBaseTest {

    private Registry governance, governance2;
    private String startDate, endDate;
    private String sessionCookie;

    private Service newService, serviceForInformationVerification, serviceForCommentVerification,
            serviceForCheckpointVerification, serviceForSavingServiceTestCase,
            serviceForEndPointDeleting, serviceForDependencyVerification2,
            serviceForDependencyVerification, serviceForRetentionVerification,
            serviceForNotificationVerification, serviceForNotificationVerification2;
    private final static String WSDL_URL =
            "https://svn.wso2.org/repos/wso2/carbon/platform/trunk/products/greg/modules/integration/registry/tests-new/" +
            "src/test/resources/artifacts/GREG/wsdl/info.wsdl";
    private final static String POLICY_URL =
            "http://svn.wso2.org/repos/wso2/carbon/platform/trunk/"
            + "platform-integration/platform-automated-test-suite/org.wso2.carbon.automation.test.repo/"
            + "src/main/resources/artifacts/GREG/policy/UTPolicy.xml";
    private final static String SCHEMA_URL =
            "https://svn.wso2.org/repos/wso2/trunk/commons/qa/"
            + "qa-artifacts/greg/xsd/calculator.xsd";
    private final static String DEPENDENCY_PATH = "/_system/governance/trunk/";
    private final static String ROOT = "/_system/governance";
    private ServiceManager serviceManager, serviceManager2;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private InfoServiceAdminClient infoServiceAdminClient;
    private RelationAdminServiceClient relationServiceClient;
    private HumanTaskAdminClient humanTaskAdminClient;
    private LifeCycleAdminServiceClient lifeCycleAdminService;
    private PropertiesAdminServiceClient propertiesAdminServiceClient;

    private EndpointManager endpointManager;
    private PolicyManager policyManager;
    private WsdlManager wsdlManager;
    private SchemaManager schemaManager;
    private Wsdl wsdl;
    private Policy policy;
    private Schema schema;
    private Endpoint endpoint;
    private static final String SERVICE_LIFE_CYCLE = "ServiceLifeCycle";

    @BeforeClass(alwaysRun = true)
    public void initialize() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        sessionCookie = getSessionCookie();
        WSRegistryServiceClient wsRegistry =
                new RegistryProviderUtil().getWSRegistry(automationContext);
        WSRegistryServiceClient wsRegistry2 =
                new RegistryProviderUtil().getWSRegistry(new AutomationContext("GREG", TestUserMode.SUPER_TENANT_USER));

        governance = new RegistryProviderUtil().getGovernanceRegistry(wsRegistry, automationContext);
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
        governance2 = new RegistryProviderUtil().getGovernanceRegistry(wsRegistry2, new AutomationContext("GREG", TestUserMode.SUPER_TENANT_USER));
        serviceManager = new ServiceManager(governance);
        serviceManager2 = new ServiceManager(governance2);

        resourceAdminServiceClient =
                new ResourceAdminServiceClient(getBackendURL(), sessionCookie);
        humanTaskAdminClient =
                new HumanTaskAdminClient(getBackendURL(), sessionCookie);
        infoServiceAdminClient =
                new InfoServiceAdminClient(getBackendURL(), sessionCookie);
        relationServiceClient =
                new RelationAdminServiceClient(getBackendURL(), sessionCookie);
        lifeCycleAdminService =
                new LifeCycleAdminServiceClient(getBackendURL(), sessionCookie);
        propertiesAdminServiceClient =
                new PropertiesAdminServiceClient(getBackendURL(), sessionCookie);

        endpointManager = new EndpointManager(governance);
        policyManager = new PolicyManager(governance);
        wsdlManager = new WsdlManager(governance);
        schemaManager = new SchemaManager(governance);

        wsdl = wsdlManager.newWsdl(WSDL_URL);
        policy = policyManager.newPolicy(POLICY_URL);
        schema = schemaManager.newSchema(SCHEMA_URL);
        wsdlManager.addWsdl(wsdl);
        policyManager.addPolicy(policy);
        schemaManager.addSchema(schema);
    }

    /*
     * All information added at service creation should be available (service
     * content)
     */
    @Test(groups = {"wso2.greg"}, description = "All information added at service creation should be available")
    public void testAddServiceInformation() throws Exception {

        serviceForInformationVerification =
                serviceManager.newService(new QName(
                        "http://service.for.informationverification/mnm/",
                        "serviceForInformationVerification"));
        serviceForInformationVerification.addAttribute("overview_version", "2.0.0");
        serviceForInformationVerification.addAttribute("overview_description", "Test");
        serviceForInformationVerification.addAttribute("interface_wsdlUrl", WSDL_URL);
        serviceForInformationVerification.addAttribute("docLinks_documentType", "test");
        serviceForInformationVerification.addAttribute("interface_messageFormats", "SOAP 1.2");
        serviceForInformationVerification.addAttribute("interface_messageExchangePatterns",
                                                       "Request Response");
        serviceForInformationVerification.addAttribute("security_authenticationPlatform",
                                                       "XTS-WS TRUST");
        serviceForInformationVerification.addAttribute("security_authenticationMechanism",
                                                       "InfoCard");
        serviceForInformationVerification.addAttribute("security_messageIntegrity", "WS-Security");
        serviceForInformationVerification.addAttribute("security_messageEncryption", "WS-Security");

        serviceManager.addService(serviceForInformationVerification);
        String serviceId = serviceForInformationVerification.getId();
        newService = serviceManager.getService(serviceId);

        Assert.assertEquals(serviceForInformationVerification.getAttribute("overview_version"),
                            "2.0.0");
        Assert.assertEquals(serviceForInformationVerification.getAttribute("overview_description"),
                            "Test");
        Assert.assertEquals(serviceForInformationVerification.getAttribute("interface_wsdlUrl"),
                            WSDL_URL);
        Assert.assertEquals(serviceForInformationVerification.getAttribute("docLinks_documentType"),
                            "test");
        Assert.assertEquals(serviceForInformationVerification.getAttribute("interface_messageFormats"),
                            "SOAP 1.2");
        Assert.assertEquals(serviceForInformationVerification.getAttribute("interface_messageExchangePatterns"),
                            "Request Response");
        Assert.assertEquals(serviceForInformationVerification.getAttribute("security_authenticationMechanism"),
                            "InfoCard");
        Assert.assertEquals(serviceForInformationVerification.getAttribute("security_authenticationPlatform"),
                            "XTS-WS TRUST");
        Assert.assertEquals(serviceForInformationVerification.getAttribute("security_messageIntegrity"),
                            "WS-Security");
        Assert.assertEquals(serviceForInformationVerification.getAttribute("security_messageEncryption"),
                            "WS-Security");
    }

    /**
     * Verify whether feeds of comments and service resource contains correct
     * information
     *
     * @throws RegistryException
     * @throws AxisFault
     * @throws RegistryExceptionException
     */
    @Test(groups = "wso2.greg", description = "Comments Verification", dependsOnMethods = {"testAddServiceInformation"})
    public void testCommentVerification() throws AxisFault, RegistryException,
                                                 RegistryExceptionException {
        serviceForCommentVerification =
                serviceManager.newService(new QName(
                        "http://service.for.commentverification2/mnm/",
                        "serviceForCommentVerification"));
        serviceForCommentVerification.setAttribute("overview_version", "1.0.0");
        serviceManager.addService(serviceForCommentVerification);
        String comment = "Test Comment";
        String path = ROOT + serviceForCommentVerification.getPath();
        String sessionId = sessionCookie;
        infoServiceAdminClient.addComment(comment, path, sessionId);
        Assert.assertEquals(infoServiceAdminClient.getComments(path, sessionId).getComments()[0].getContent(),
                            comment, "comment don't have the correct information");

    }

    /**
     * Create checkpoints and verify whether the created versions contain
     * correct information
     *
     * @throws GovernanceException
     * @throws ResourceAdminServiceExceptionException
     *
     * @throws RemoteException
     * @throws ADBException
     */
    @Test(groups = {"wso2.greg"}, description = "Checkpoint Service Verification", dependsOnMethods = "testCommentVerification")
    public void testCheckpointServiceVerification() throws GovernanceException, RemoteException,
                                                           ResourceAdminServiceExceptionException,
                                                           ADBException {
        serviceForCheckpointVerification =
                serviceManager.newService(new QName(
                        "http://service.for.checkpointverification/mnm/",
                        "serviceForCheckpointVerification"));
        serviceForCheckpointVerification.addAttribute("test-att", "test-val");
        serviceForCheckpointVerification.addAttribute("overview_version", "1.0.0");
        serviceManager.addService(serviceForCheckpointVerification);
        String destinationPath = ROOT + serviceForCheckpointVerification.getPath();
        resourceAdminServiceClient.createVersion(destinationPath);
        serviceForCheckpointVerification.addAttribute("test-att2", "test-val2");
        serviceManager.updateService(serviceForCheckpointVerification);
        resourceAdminServiceClient.createVersion(destinationPath);

        VersionPath[] versionPaths = resourceAdminServiceClient.getVersionPaths(destinationPath);

        Assert.assertTrue(resourceAdminServiceClient.getTextContent(versionPaths[1].getCompleteVersionPath())
                                  .contains("test-att"), "versions doesn't contain correct information");
        Assert.assertFalse(resourceAdminServiceClient.getTextContent(versionPaths[1].getCompleteVersionPath())
                                   .contains("test-att2"), "versions doesn't contain correct information");
        Assert.assertTrue(resourceAdminServiceClient.getTextContent(versionPaths[0].getCompleteVersionPath())
                                  .contains("test-att"), "versions doesn't contain correct information");
        Assert.assertTrue(resourceAdminServiceClient.getTextContent(versionPaths[0].getCompleteVersionPath())
                                  .contains("test-att2"), "versions doesn't contain correct information");

    }

    /**
     * Verify whether deleted endpoints appear back after saving the service
     *
     * @throws GovernanceException
     */
    @Test(groups = {"wso2.greg"}, description = "deleted endpoints", dependsOnMethods = "testCheckpointServiceVerification")
    public void testDeletingEndPoints() throws GovernanceException {
        serviceForEndPointDeleting =
                serviceManager.newService(new QName(
                        "http://service.for.EndPointDeleting/mnm/",
                        "serviceForEndPointDeleting"));
        Endpoint endpoint;
        endpoint = endpointManager.newEndpoint("http://service.for.EndPointDeleting");
        endpointManager.addEndpoint(endpoint);
        serviceForEndPointDeleting.addAttribute("overview_version", "1.0.0");
        serviceManager.addService(serviceForEndPointDeleting);
        serviceForEndPointDeleting.attachEndpoint(endpoint);
        Endpoint endpoints[] = serviceForEndPointDeleting.getAttachedEndpoints();
        int beforeDeleting = endpoints.length;
        serviceForEndPointDeleting.detachEndpoint(endpoint.getId());
        endpoints = serviceForEndPointDeleting.getAttachedEndpoints();
        int afterDeleting = endpoints.length;
        Assert.assertEquals(beforeDeleting - afterDeleting, 1, "endpoint deletion failed");
    }

    /*
     * Attach/detach resources other than metadata to the service as
     * dependencies and verify whether they appear with check boxes to be
     * promoted
     */
    @Test(groups = "wso2.greg", description = "Dependency Verification",
          dependsOnMethods = {"testDeletingEndPoints"})
    public void testDependencyVerification() throws GovernanceException, RemoteException,
                                                    ResourceAdminServiceExceptionException,
                                                    MalformedURLException,
                                                    AddAssociationRegistryExceptionException {
        serviceForDependencyVerification2 =
                serviceManager.newService(new QName(
                        "http://service.for.dependencyverification2/mnm/",
                        "serviceForDependencyVerification2"));
        serviceForDependencyVerification2.addAttribute("overview_version", "1.0.0");
        serviceManager.addService(serviceForDependencyVerification2);

        String path =
                FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator +
                "GREG" + File.separator + "testresource.txt";
        DataHandler dataHandler = new DataHandler(new URL("file:///" + path));

        resourceAdminServiceClient.addResource(DEPENDENCY_PATH + "testresource.txt", "text/plain",
                                               "desc", dataHandler);

        String dependencyType = "depends";
        String todo = "add";
        relationServiceClient.addAssociation(ROOT + serviceForDependencyVerification2.getPath(),
                                             dependencyType, DEPENDENCY_PATH + "testresource.txt",
                                             todo);

        Assert.assertEquals(relationServiceClient.getDependencies(ROOT +
                                                                  serviceForDependencyVerification2.getPath())
                                    .getAssociationBeans()[0].getDestinationPath(),
                            "/_system/governance/trunk/testresource.txt");
    }

    /*
     * Verify whether metadata description/properties/dependancies,community
     * features gets removed from the service when saving the service after
     * doing changes
     *
     */
    @Test(groups = "wso2.greg", description = "saving the service", dependsOnMethods = {"testDependencyVerification"})
    public void testSavingService()
            throws AxisFault, RegistryException, RegistryExceptionException {
        serviceForSavingServiceTestCase =
                serviceManager.newService(new QName(
                        "http://service.for.saving/mnm/",
                        "serviceForSavingServiceTestCase"));
        serviceForSavingServiceTestCase.addAttribute("overview_version", "1.0.0");
        serviceManager.addService(serviceForSavingServiceTestCase);
        serviceForSavingServiceTestCase.getDependencies();
        serviceForSavingServiceTestCase.attachPolicy(policy);
        serviceForSavingServiceTestCase.attachSchema(schema);
        serviceForSavingServiceTestCase.attachWSDL(wsdl);
        String comment = "Test Comment";
        String path = ROOT + serviceForSavingServiceTestCase.getPath();
        String sessionId = sessionCookie;
        infoServiceAdminClient.addComment(comment, path, sessionId);
        serviceManager.updateService(serviceForSavingServiceTestCase);

        int noOfDependencies = serviceForSavingServiceTestCase.getDependencies().length;
        serviceForSavingServiceTestCase.addAttribute("overview_description", "description");
        serviceForSavingServiceTestCase.addAttribute("test-att", "test-val");
        serviceManager.updateService(serviceForSavingServiceTestCase);
        Assert.assertEquals(serviceForSavingServiceTestCase.getDependencies().length,
                            noOfDependencies, "number of dependencies don't match with the expected number after saving");
        Assert.assertEquals(infoServiceAdminClient.getComments(path, sessionId).getComments()[0].getContent(),
                            comment, "comment don't match with the expected comment after saving");
        Assert.assertEquals(serviceForSavingServiceTestCase.getAttribute("overview_description"),
                            "description", "overview_description don't match with the original description number after saving");

    }

    /*
     * Lock a service using retention, login from a different user and try to
     * update/delete the service and it's properties
     */
    @Test(groups = "wso2.greg", description = "Retention Verification", dependsOnMethods = {"testSavingService"})
    public void testRetentionVerification() throws GovernanceException, RemoteException,
                                                   PropertiesAdminServiceRegistryExceptionException {

        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        Calendar now = Calendar.getInstance();
        startDate = dateFormat.format(now.getTime());
        now.add(Calendar.DAY_OF_MONTH, 2);
        endDate = dateFormat.format(now.getTime());

        serviceForRetentionVerification =
                serviceManager.newService(new QName(
                        "http://service.for.retentionverification/mnm/",
                        "serviceForRetentionVerification"));
        serviceForRetentionVerification.addAttribute("overview_version", "1.0.0");
        serviceManager.addService(serviceForRetentionVerification);
        propertiesAdminServiceClient.setRetentionProperties(ROOT +
                                                            serviceForRetentionVerification.getPath(),
                                                            "delete", startDate, endDate);

        RetentionBean retentionBean =
                propertiesAdminServiceClient.getRetentionProperties(ROOT +
                                                                    serviceForRetentionVerification.getPath());
        assertTrue(retentionBean.getDeleteLocked(), "delete not locked");
        assertFalse(retentionBean.getWriteLocked(), "write should not be blocked");
        assertTrue(retentionBean.getFromDate().equals(startDate), "wrong start date");
        assertTrue(retentionBean.getToDate().equals(endDate), "wrong end date");

    }

    /*
     * Lock a service using retention, login from a different user and try to
     * update/delete the service and it's properties
     */
    @Test(groups = "wso2.greg", description = "Retention Verification", dependsOnMethods = {"testRetentionVerification"},
          expectedExceptions = GovernanceException.class)
    public void testRetentionVerification2() throws GovernanceException,
                                                    PropertiesAdminServiceRegistryExceptionException,
                                                    RemoteException {
        serviceManager2.removeService(serviceForRetentionVerification.getId());
    }

    /*
     * Verify whether notifications work for services (LC transition (within
     * same/different environments), LC item click)
     *
     */
    @Test(groups = "wso2.greg", description = "Get Notification", dependsOnMethods = {"testRetentionVerification2"})
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    public void testGetNotification() throws RemoteException, IllegalStateFault,
                                             IllegalAccessFault, IllegalArgumentFault,
                                             GovernanceException,
                                             LifeCycleManagementServiceExceptionException,
                                             CustomLifecyclesChecklistAdminServiceExceptionException,
                                             InterruptedException {
        String checkListItem1 = "[ServiceLifeCycle] The CheckList item 'QoS Created' of LifeCycle State " +
                                "'Development' was Checked for resource at /_system/governance/trunk/services" +
                                "/notificationverification/for/service/mnm/1.0.0/serviceForNotificationVerification.";

        String checkListItem2 = "[ServiceLifeCycle] The CheckList item 'WSDL, Schema Created' of LifeCycle State " +
                                "'Development' was Checked for resource at /_system/governance/trunk/services/" +
                                "notificationverification/for/service/mnm/1.0.0/serviceForNotificationVerification.";

        serviceForNotificationVerification =
                serviceManager.newService(new QName(
                        "http://service.for.notificationverification/mnm/",
                        "serviceForNotificationVerification"));
        serviceForNotificationVerification.addAttribute("overview_version", "1.0.0");
        serviceManager.addService(serviceForNotificationVerification);
        serviceForNotificationVerification.attachLifecycle(SERVICE_LIFE_CYCLE);
        String path = ROOT + serviceForNotificationVerification.getPath();
        String endpoint = "work://admin";
        String eventName = "CheckListItemChecked";
        String sessionId = sessionCookie;
        SubscriptionBean bean =
                infoServiceAdminClient.subscribe(path, endpoint, eventName,
                                                 sessionId);
        Assert.assertTrue(bean.getSubscriptionInstances() != null, "SubscriptionBean is null");

        String ACTION_ITEM_CLICK = "itemClick";
        lifeCycleAdminService.invokeAspect(path, "ServiceLifeCycle", ACTION_ITEM_CLICK,
                                           new String[]{"false", "true", "true"});

        Thread.sleep(5000);
        int count = 0;
        Property[] lcBean = lifeCycleAdminService.getLifecycleBean(path).getLifecycleProperties();
        if (lcBean != null) {
            for (Property lc : lcBean) {
                String[] values = lc.getValues();
                for (String value : values) {
                    if (value.contains("name:WSDL, Schema Created") || value.contains("name:QoS Created")) {
                        count++;
                    }
                }
            }
        }

        assertEquals(count, 2, "couldn't find checked items tow");

//                [0].getValues();
        WorkItem[] workItems = WorkItemClient.getWorkItems(humanTaskAdminClient);
//        WorkItem[] workItems = humanTaskAdminClient.getWorkItems();
        boolean status = false;
        if (workItems.length > 0) {
            for (WorkItem workItem : workItems) {
                if (workItem.getPresentationSubject().toString()
                        .startsWith("[ServiceLifeCycle] The CheckList item")) {
                    if (workItem.getPresentationSubject().toString().equals(checkListItem1)) {
                        status = true;
                    }
                    if (workItem.getPresentationSubject().toString().equals(checkListItem2)) {
                        status = true;
                    }
                }
            }
        }
        assertTrue(status);
    }

    /*
     * Verify whether notifications work for services (LC transition (within
     * same/different environments), service update)
     *
     */
    @Test(groups = "wso2.greg", description = "Get Notification", dependsOnMethods = {"testGetNotification"})
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    public void testGetNotification2() throws RemoteException, IllegalStateFault,
                                              IllegalAccessFault, IllegalArgumentFault,
                                              GovernanceException,
                                              LifeCycleManagementServiceExceptionException,
                                              CustomLifecyclesChecklistAdminServiceExceptionException,
                                              InterruptedException {
        serviceForNotificationVerification2 =
                serviceManager.newService(new QName(
                        "http://service.for.notificationverification2/mnm/",
                        "serviceForNotificationVerification2"));
        serviceForNotificationVerification2.addAttribute("overview_version", "1.0.0");
        serviceManager.addService(serviceForNotificationVerification2);
        serviceForNotificationVerification2.attachLifecycle(SERVICE_LIFE_CYCLE);
        String path = ROOT + serviceForNotificationVerification2.getPath();
        String endpoint = "work://admin";
        String eventName = "ResourceUpdated";
        String sessionId = sessionCookie;
        SubscriptionBean bean =
                infoServiceAdminClient.subscribe(path, endpoint, eventName,
                                                 sessionId);
        Assert.assertTrue(bean.getSubscriptionInstances() != null, "SubscriptionBean is null");

        serviceForNotificationVerification2.addAttribute("test-att", "test-val");
        serviceManager.updateService(serviceForNotificationVerification2);

        Thread.sleep(2000);
        WorkItem[] workItems = WorkItemClient.getWorkItems(humanTaskAdminClient);

        Assert.assertTrue(workItems[0].getPresentationSubject().toString()
                                  .contains("serviceForNotificationVerification2 was updated"));

    }


    /**
     * Attach/detach a policy/schema/wsdl/endpoint to the service as
     * dependencies and verify whether they appear with check boxes to be
     * promoted
     * <p/>
     * https://wso2.org/jira/browse/REGISTRY-1179
     *
     * @throws GovernanceException
     */
    @Test(groups = "wso2.greg", description = "Dependency Verification",
          dependsOnMethods = {"testRetentionVerification2"})
    public void testDependencyVerification2() throws GovernanceException {
        serviceForDependencyVerification =
                serviceManager.newService(new QName(
                        "http://service.for.dependencyverification/mnm/",
                        "serviceForDependencyVerification"));
        serviceForDependencyVerification.addAttribute("overview_version", "1.0.0");
        endpoint = endpointManager.newEndpoint("http://endpoint.for.EndPointDeleting");
        endpointManager.addEndpoint(endpoint);
        serviceManager.addService(serviceForDependencyVerification);
        serviceForDependencyVerification.attachEndpoint(endpoint);
        serviceForDependencyVerification.attachPolicy(policy);
        serviceForDependencyVerification.attachSchema(schema);
        serviceForDependencyVerification.attachWSDL(wsdl);

        Assert.assertEquals(serviceForDependencyVerification.getDependencies().length, 4, "number of dependencies do not match");

    }

    @AfterClass(alwaysRun = true)
    public void endGame()
            throws RegistryException, PropertiesAdminServiceRegistryExceptionException,
                   RemoteException {
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
        removeServiceRetention(ROOT + serviceForRetentionVerification.getPath());

        deleteService(serviceForCheckpointVerification);
        deleteService(serviceForCommentVerification);
        deleteService(serviceForDependencyVerification);
        deleteService(serviceForDependencyVerification2);
        deleteService(serviceForEndPointDeleting);
        deleteService(serviceForInformationVerification);
        deleteService(serviceForRetentionVerification);
        deleteService(serviceForNotificationVerification);
        deleteService(serviceForNotificationVerification2);
        deleteService(serviceForSavingServiceTestCase);

        Service[] services = serviceManager.findServices(new ServiceFilter() {
            public boolean matches(Service service) throws GovernanceException {
                String attributeVal = service.getAttribute("overview_name");
                return attributeVal != null && attributeVal.startsWith("Info");
            }
        });

        if (services != null) {
            for (Service service : services) {
                serviceManager.removeService(service.getId());
            }
        }

        //TODO Uncomment followings when tests are not running parallel
        //wsdlManager.removeWsdl(wsdl.getId());
        //endpointManager.removeEndpoint(endpoint.getId());

        policyManager.removePolicy(policy.getId());
        schemaManager.removeSchema(schema.getId());

        governance = null;
        governance2 = null;
        newService = null;
        serviceForInformationVerification = null;
        serviceForCommentVerification = null;
        serviceForCheckpointVerification = null;
        serviceForSavingServiceTestCase = null;
        serviceForEndPointDeleting = null;
        serviceForDependencyVerification2 = null;
        serviceForDependencyVerification = null;
        serviceForRetentionVerification = null;
        serviceForNotificationVerification = null;
        serviceForNotificationVerification2 = null;

        serviceManager = null;
        serviceManager2 = null;
        resourceAdminServiceClient = null;
        infoServiceAdminClient = null;
        relationServiceClient = null;
        humanTaskAdminClient = null;
        lifeCycleAdminService = null;
        propertiesAdminServiceClient = null;

        endpointManager = null;
        policyManager = null;
        wsdlManager = null;
        schemaManager = null;
        wsdl = null;
        policy = null;
        schema = null;
        endpoint = null;
    }

    private void removeServiceRetention(String path) throws GovernanceException,
                                                            PropertiesAdminServiceRegistryExceptionException,
                                                            RemoteException {
        propertiesAdminServiceClient.setRetentionProperties(path, "can_remove",
                                                            startDate, endDate);
    }

    private void deleteService(Service service) throws RegistryException {
        if (service != null) {
            if (governance.resourceExists(service.getPath())) {
                serviceManager.removeService(service.getId());
            }
        }
    }

}
