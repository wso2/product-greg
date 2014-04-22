/*
 * Copyright 2004,2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.greg.integration.governance.api.test;

import org.apache.axis2.AxisFault;
import org.apache.axis2.databinding.ADBException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
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
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
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
import org.wso2.greg.integration.common.clients.*;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;
import org.wso2.greg.integration.governance.subscription.test.util.WorkItemClient;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;

public class ServicesResourceInformationManagementTestCase extends GREGIntegrationBaseTest {

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
    private static final String SERVICE_LIFE_CYCLE = "ServiceLifeCycle";

    private Registry governance, governanceTwo;
    private String startDate, endDate;
    private Service serviceForInformationVerification;
    private Service serviceForCommentVerification;
    private Service serviceForCheckpointVerification;
    private Service serviceForSavingServiceTestCase;
    private Service serviceForEndPointDeleting;
    private Service serviceForDependencyVerificationTwo;
    private Service serviceForDependencyVerification;
    private Service serviceForRetentionVerification;
    private Service serviceForNotificationVerification;
    private Service serviceForNotificationVerification2;
    private ServiceManager serviceManager, serviceManagerTwo;
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

    @BeforeClass(alwaysRun = true)
    public void initialize() throws RemoteException, LoginAuthenticationExceptionException,
            RegistryException, XPathExpressionException {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        WSRegistryServiceClient wsRegistry =
                new RegistryProviderUtil().getWSRegistry(automationContext.getProductGroup().getGroupName(), automationContext.getDefaultInstance().getName(),
                        automationContext.getConfigurationNode("//superTenant/tenant/@key").getNodeValue(),
                        automationContext.getSuperTenant().getTenantAdmin().getKey());
        WSRegistryServiceClient wsRegistryTwo =
                new RegistryProviderUtil().getWSRegistry(automationContext.getProductGroup().getGroupName(), "greg002",
                        automationContext.getConfigurationNode("//superTenant/tenant/@key").getNodeValue(),
                        automationContext.getTenant().getTenantUserList().get(1).getKey());

        governance = new RegistryProviderUtil().getGovernanceRegistry(wsRegistry, automationContext.getUser().getUserName());
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
        governanceTwo = new RegistryProviderUtil().getGovernanceRegistry(wsRegistryTwo,
                automationContext.getConfigurationNode("//userManagement/superTenant/tenant/users").getFirstChild().getChildNodes().item(0).getFirstChild().getNodeValue());
        serviceManager = new ServiceManager(governance);
        serviceManagerTwo = new ServiceManager(governanceTwo);

        resourceAdminServiceClient =
                new ResourceAdminServiceClient(getBackendURL(),
                        getSessionCookie());
        humanTaskAdminClient =
                new HumanTaskAdminClient(getBackendURL(),
                        getSessionCookie());
        infoServiceAdminClient =
                new InfoServiceAdminClient(getBackendURL(),
                        getSessionCookie());
        relationServiceClient =
                new RelationAdminServiceClient(getBackendURL(),
                        getSessionCookie());
        lifeCycleAdminService =
                new LifeCycleAdminServiceClient(getBackendURL(),
                        getSessionCookie());
        propertiesAdminServiceClient =
                new PropertiesAdminServiceClient(getBackendURL(),
                        getSessionCookie());

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
        serviceForInformationVerification.getId();

        assertEquals("2.0.0", serviceForInformationVerification.getAttribute("overview_version"));
        assertEquals("Test", serviceForInformationVerification.getAttribute("overview_description"));
        assertEquals(WSDL_URL, serviceForInformationVerification.getAttribute("interface_wsdlUrl"));
        assertEquals("test", serviceForInformationVerification.getAttribute("docLinks_documentType"));
        assertEquals("SOAP 1.2", serviceForInformationVerification.getAttribute("interface_messageFormats"));
        assertEquals("Request Response", serviceForInformationVerification.getAttribute("interface_messageExchangePatterns"));
        assertEquals("InfoCard", serviceForInformationVerification.getAttribute("security_authenticationMechanism"));
        assertEquals("XTS-WS TRUST", serviceForInformationVerification.getAttribute("security_authenticationPlatform"));
        assertEquals("WS-Security", serviceForInformationVerification.getAttribute("security_messageIntegrity"));
        assertEquals("WS-Security", serviceForInformationVerification.getAttribute("security_messageEncryption"));
    }

    /**
     * Verify whether feeds of comments and service resource contains correct
     * information
     *
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
     *
     * @throws org.apache.axis2.AxisFault
     * @throws RegistryExceptionException
     */
    @Test(groups = "wso2.greg", description = "Comments Verification", dependsOnMethods = {"testAddServiceInformation"})
    public void testCommentVerification() throws RemoteException, RegistryException,
            RegistryExceptionException, LoginAuthenticationExceptionException, XPathExpressionException {
        serviceForCommentVerification =
                serviceManager.newService(new QName(
                        "http://service.for.commentverification2/mnm/",
                        "serviceForCommentVerification"));
        serviceManager.addService(serviceForCommentVerification);
        String comment = "Test Comment";
        String path = ROOT + serviceForCommentVerification.getPath();
        String sessionId = getSessionCookie();
        infoServiceAdminClient.addComment(comment, path, sessionId);
        assertEquals("comment don't have the correct information", infoServiceAdminClient.getComments(path, sessionId).getComments()[0].getContent(),
                comment);

    }

    /**
     * Create checkpoints and verify whether the created versions contain
     * correct information
     *
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException
     *
     * @throws ResourceAdminServiceExceptionException
     *
     * @throws java.rmi.RemoteException
     * @throws org.apache.axis2.databinding.ADBException
     *
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
        serviceManager.addService(serviceForCheckpointVerification);
        String destinationPath = ROOT + serviceForCheckpointVerification.getPath();
        resourceAdminServiceClient.createVersion(destinationPath);
        serviceForCheckpointVerification.addAttribute("test-att2", "test-val2");
        serviceManager.updateService(serviceForCheckpointVerification);
        resourceAdminServiceClient.createVersion(destinationPath);

        VersionPath[] versionPaths = resourceAdminServiceClient.getVersionPaths(destinationPath);

        assertTrue(resourceAdminServiceClient.getTextContent(versionPaths[1].getCompleteVersionPath())
                .contains("test-att"), "versions doesn't contain correct information");
        assertFalse(resourceAdminServiceClient.getTextContent(versionPaths[1].getCompleteVersionPath())
                .contains("test-att2"), "versions doesn't contain correct information");
        assertTrue(resourceAdminServiceClient.getTextContent(versionPaths[0].getCompleteVersionPath())
                .contains("test-att"), "versions doesn't contain correct information");
        assertTrue(resourceAdminServiceClient.getTextContent(versionPaths[0].getCompleteVersionPath())
                .contains("test-att2"), "versions doesn't contain correct information");

    }

    /**
     * Verify whether deleted endpoints appear back after saving the service
     *
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException
     *
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
        serviceManager.addService(serviceForEndPointDeleting);
        serviceForEndPointDeleting.attachEndpoint(endpoint);
        Endpoint endpoints[] = serviceForEndPointDeleting.getAttachedEndpoints();
        int beforeDeleting = endpoints.length;
        serviceForEndPointDeleting.detachEndpoint(endpoint.getId());
        endpoints = serviceForEndPointDeleting.getAttachedEndpoints();
        int afterDeleting = endpoints.length;
        assertEquals("endpoint deletion failed", beforeDeleting - afterDeleting, 1);
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
        serviceForDependencyVerificationTwo =
                serviceManager.newService(new QName(
                        "http://service.for.dependencyverification2/mnm/",
                        "serviceForDependencyVerificationTwo"));
        serviceManager.addService(serviceForDependencyVerificationTwo);

        String path =
                FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator +
                        "GREG" + File.separator + "testresource.txt";
        DataHandler dataHandler = new DataHandler(new URL("file:///" + path));

        resourceAdminServiceClient.addResource(DEPENDENCY_PATH + "testresource.txt", "text/plain",
                "desc", dataHandler);

        String dependencyType = "depends";
        String todo = "add";
        relationServiceClient.addAssociation(ROOT + serviceForDependencyVerificationTwo.getPath(),
                dependencyType, DEPENDENCY_PATH + "testresource.txt",
                todo);

        assertEquals(relationServiceClient.getDependencies(ROOT +
                serviceForDependencyVerificationTwo.getPath())
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
            throws RemoteException, RegistryException, RegistryExceptionException, LoginAuthenticationExceptionException, XPathExpressionException {
        serviceForSavingServiceTestCase =
                serviceManager.newService(new QName(
                        "http://service.for.saving/mnm/",
                        "serviceForSavingServiceTestCase"));
        serviceManager.addService(serviceForSavingServiceTestCase);
        serviceForSavingServiceTestCase.getDependencies();
        serviceForSavingServiceTestCase.attachPolicy(policy);
        serviceForSavingServiceTestCase.attachSchema(schema);
        serviceForSavingServiceTestCase.attachWSDL(wsdl);
        String comment = "Test Comment";
        String path = ROOT + serviceForSavingServiceTestCase.getPath();
        String sessionId = getSessionCookie();
        infoServiceAdminClient.addComment(comment, path, sessionId);
        serviceManager.updateService(serviceForSavingServiceTestCase);

        int noOfDependencies = serviceForSavingServiceTestCase.getDependencies().length;
        serviceForSavingServiceTestCase.addAttribute("overview_description", "description");
        serviceForSavingServiceTestCase.addAttribute("test-att", "test-val");
        serviceManager.updateService(serviceForSavingServiceTestCase);
        assertEquals("number of dependencies don't match with the expected number after saving",
                serviceForSavingServiceTestCase.getDependencies().length,
                noOfDependencies);
        assertEquals("comment don't match with the expected comment after saving",
                infoServiceAdminClient.getComments(path, sessionId).getComments()[0].getContent(),
                comment);
        assertEquals("overview_description don't match with the original description number after saving",
                serviceForSavingServiceTestCase.getAttribute("overview_description"),
                "description");

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
    public void testRetentionVerification2() throws RegistryException,
            PropertiesAdminServiceRegistryExceptionException, RemoteException {
        //   GovernanceUtils.loadGovernanceArtifacts((UserRegistry)governance);
        serviceManagerTwo.removeService(serviceForRetentionVerification.getId());
    }

    /*
     * Verify whether notifications work for services (LC transition (within
     * same/different environments), LC item click)
     *
     */
    @Test(groups = "wso2.greg", description = "Get Notification", dependsOnMethods = {"testRetentionVerification2"})
    /*@SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_user,
                                             ExecutionEnvironment.integration_tenant})
    */ public void testGetNotification() throws Exception {

        ServerConfigurationManager serverConfigurationManager = new ServerConfigurationManager(automationContext.getProductGroup().getGroupName()
                , TestUserMode.SUPER_TENANT_ADMIN);
        serverConfigurationManager.applyConfiguration(new File(FrameworkPathUtil.getSystemResourceLocation() +
                "artifact" + File.separator + "GREG" + File.separator
                + "xml" + File.separator + "registry.xml"));

        String checkListItem1 = "[ServiceLifeCycle] The CheckList item 'QoS Created' of LifeCycle State " +
                "'Development' was Checked for resource at /_system/governance/trunk/services" +
                "/notificationverification/for/service/mnm/serviceForNotificationVerification.";

        String checkListItem2 = "[ServiceLifeCycle] The CheckList item 'WSDL, Schema Created' of LifeCycle State " +
                "'Development' was Checked for resource at /_system/governance/trunk/services/" +
                "notificationverification/for/service/mnm/serviceForNotificationVerification.";

        Thread.sleep(30000);
        WSRegistryServiceClient wsRegistry =
                new RegistryProviderUtil().getWSRegistry(automationContext.getProductGroup().getGroupName(), automationContext.getDefaultInstance().getName(),
                        automationContext.getConfigurationNode("//superTenant/tenant/@key").getNodeValue(),
                        automationContext.getSuperTenant().getTenantAdmin().getKey());
        governance = new RegistryProviderUtil().getGovernanceRegistry(wsRegistry, automationContext.getUser().getUserName());


        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
        serviceManager = new ServiceManager(governance);

        serviceForNotificationVerification =
                serviceManager.newService(new QName(
                        "http://service.for.notificationverification/mnm/",
                        "serviceForNotificationVerification"));
        serviceManager.addService(serviceForNotificationVerification);
        serviceForNotificationVerification.attachLifecycle(SERVICE_LIFE_CYCLE);
        String path = ROOT + serviceForNotificationVerification.getPath();
        String endpoint = "work://admin";
        String eventName = "CheckListItemChecked";
        String sessionId = getSessionCookie();

        infoServiceAdminClient =
                new InfoServiceAdminClient(getBackendURL(),
                        getSessionCookie());

        SubscriptionBean bean =
                infoServiceAdminClient.subscribe(path, endpoint, eventName,
                        sessionId);
        assertTrue(bean.getSubscriptionInstances() != null, "SubscriptionBean is null");

        String ACTION_ITEM_CLICK = "itemClick";

        lifeCycleAdminService =
                new LifeCycleAdminServiceClient(getBackendURL(),
                        getSessionCookie());

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

        assertEquals("couldn't find checked items tow", count, 2);

        humanTaskAdminClient =
                new HumanTaskAdminClient(getBackendURL(),
                        getSessionCookie());

        WorkItem[] workItems = WorkItemClient.getWorkItems(humanTaskAdminClient);

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
    /*@SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_user,
                                             ExecutionEnvironment.integration_tenant})*/
    public void testGetNotificationTwo() throws RemoteException, IllegalStateFault,
            IllegalAccessFault, IllegalArgumentFault,
            GovernanceException,
            LifeCycleManagementServiceExceptionException,
            CustomLifecyclesChecklistAdminServiceExceptionException,
            InterruptedException, LoginAuthenticationExceptionException, XPathExpressionException {

        serviceForNotificationVerification2 =
                serviceManager.newService(new QName(
                        "http://service.for.notificationverification2/mnm/",
                        "serviceForNotificationVerification2"));
        serviceManager.addService(serviceForNotificationVerification2);
        serviceForNotificationVerification2.attachLifecycle(SERVICE_LIFE_CYCLE);
        String path = ROOT + serviceForNotificationVerification2.getPath();
        String endpoint = "work://admin";
        String eventName = "ResourceUpdated";
        String sessionId = getSessionCookie();
        SubscriptionBean bean =
                infoServiceAdminClient.subscribe(path, endpoint, eventName,
                        sessionId);
        assertTrue(bean.getSubscriptionInstances() != null, "SubscriptionBean is null");

        serviceForNotificationVerification2.addAttribute("test-att", "test-val");
        serviceManager.updateService(serviceForNotificationVerification2);

        Thread.sleep(2000);
        WorkItem[] workItems = WorkItemClient.getWorkItems(humanTaskAdminClient);

        assertTrue(workItems[0].getPresentationSubject().toString()
                .contains("serviceForNotificationVerification2 was updated"));

    }


    /**
     * Attach/detach a policy/schema/wsdl/endpoint to the service as
     * dependencies and verify whether they appear with check boxes to be
     * promoted
     * <p/>
     * https://wso2.org/jira/browse/REGISTRY-1179
     *
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException
     *
     */
    @Test(groups = "wso2.greg", description = "Dependency Verification",
            dependsOnMethods = {"testGetNotificationTwo"})
    public void testDependencyVerificationTwo() throws RegistryException, XPathExpressionException, AxisFault {
        serviceForDependencyVerification =
                serviceManager.newService(new QName(
                        "http://service.for.dependencyverification/mnm/",
                        "serviceForDependencyVerification"));
        WSRegistryServiceClient wsRegistry =
                new RegistryProviderUtil().getWSRegistry(automationContext.getProductGroup().getGroupName(), automationContext.getDefaultInstance().getName(),
                        automationContext.getConfigurationNode("//superTenant/tenant/@key").getNodeValue(),
                        automationContext.getSuperTenant().getTenantAdmin().getKey());
        governance = new RegistryProviderUtil().getGovernanceRegistry(wsRegistry, automationContext.getUser().getUserName());
        endpointManager = new EndpointManager(governance);
        endpoint = endpointManager.newEndpoint("http://endpoint.for.EndPointDeleting");
        endpointManager.addEndpoint(endpoint);
        serviceManager.addService(serviceForDependencyVerification);
        serviceForDependencyVerification.attachEndpoint(endpoint);
        serviceForDependencyVerification.attachPolicy(policy);
        serviceForDependencyVerification.attachSchema(schema);
        serviceForDependencyVerification.attachWSDL(wsdl);

        assertEquals("number of dependencies do not match", serviceForDependencyVerification.getDependencies().length, 4);

    }

    @AfterClass(alwaysRun = true)
    public void endGame() throws RegistryException, PropertiesAdminServiceRegistryExceptionException,
            RemoteException, LoginAuthenticationExceptionException, XPathExpressionException {
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);

        removeServiceRetention(ROOT + serviceForRetentionVerification.getPath());

        deleteService(serviceForCheckpointVerification);
        deleteService(serviceForCommentVerification);
        deleteService(serviceForDependencyVerification);
        deleteService(serviceForDependencyVerificationTwo);
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

        policyManager = new PolicyManager(governance);
        schemaManager = new SchemaManager(governance);
        policyManager.removePolicy(policy.getId());
        schemaManager.removeSchema(schema.getId());

        governance = null;
        governanceTwo = null;
        serviceForInformationVerification = null;
        serviceForCommentVerification = null;
        serviceForCheckpointVerification = null;
        serviceForSavingServiceTestCase = null;
        serviceForEndPointDeleting = null;
        serviceForDependencyVerificationTwo = null;
        serviceForDependencyVerification = null;
        serviceForRetentionVerification = null;
        serviceForNotificationVerification = null;
        serviceForNotificationVerification2 = null;

        serviceManager = null;
        serviceManagerTwo = null;
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
            PropertiesAdminServiceRegistryExceptionException, RemoteException, XPathExpressionException,
            LoginAuthenticationExceptionException {
        propertiesAdminServiceClient =
                new PropertiesAdminServiceClient(getBackendURL(),
                        getSessionCookie());
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
