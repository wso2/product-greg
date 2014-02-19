package org.wso2.carbon.registry.volume.test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;

import javax.xml.namespace.QName;

import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.authenticator.stub.LogoutAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.registry.ActivityAdminServiceClient;
import org.wso2.carbon.automation.api.clients.registry.SearchAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.core.utils.fileutils.FileManager;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.api.endpoints.EndpointManager;
import org.wso2.carbon.governance.api.endpoints.dataobjects.Endpoint;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.policies.PolicyManager;
import org.wso2.carbon.governance.api.policies.dataobjects.Policy;
import org.wso2.carbon.governance.api.schema.SchemaManager;
import org.wso2.carbon.governance.api.schema.dataobjects.Schema;
import org.wso2.carbon.governance.api.services.ServiceFilter;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.registry.activities.stub.RegistryExceptionException;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.search.metadata.test.bean.SearchParameterBean;
import org.wso2.carbon.registry.search.stub.SearchAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.search.stub.beans.xsd.AdvancedSearchResultsBean;
import org.wso2.carbon.registry.search.stub.beans.xsd.ArrayOfString;
import org.wso2.carbon.registry.search.stub.beans.xsd.CustomSearchParameterBean;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

/**
 * covers following jiras
 * https://wso2.org/jira/browse/REGISTRY-838 OR
 * https://wso2.org/jira/browse/REGISTRY-724 OR
 * https://wso2.org/jira/browse/CARBON-12871
 */
public class MetaDataVolumeTest {

    private Registry governance;
    private ServiceManager serviceManager;
    private SchemaManager schemaManager;
    private GenericArtifactManager artifactManager;
    private SearchAdminServiceClient searchAdminServiceClient;
    private WsdlManager wsdlManager;
    private PolicyManager policyManager;
    private final static String WSDL_URL =
            "https://svn.wso2.org/repos/wso2/carbon/platform/trunk/"
            + "platform-integration/clarity-tests/org.wso2.carbon.automation.test.repo/"
            + "src/main/resources/artifacts/GREG/wsdl/info.wsdl";
    private final String serviceName = "WSO2AutomatedService";
    private UserInfo userInfo;
    private ManageEnvironment environment;
    private ActivityAdminServiceClient activityAdminServiceClient;

    private static final Log log = LogFactory.getLog(MetaDataVolumeTest.class);
    private final int NUMBER_OF_ENDPOINTS = 10;
    private final int NUMBER_OF_ARTIFACTS = 10;
    private final int NUMBER_OF_POLICIES = 10;
    private final int NO_OF_SERVICES = 10;

    @BeforeClass(groups = {"wso2.greg"})
    public void initTest() throws RegistryException, RemoteException,
                                  LoginAuthenticationExceptionException {
        int userId = 1;
        userInfo = UserListCsvReader.getUserInfo(userId);
        WSRegistryServiceClient wsRegistry = new RegistryProviderUtil().getWSRegistry(userId,
                                                                                      ProductConstant.GREG_SERVER_NAME);

        governance = new RegistryProviderUtil().getGovernanceRegistry(wsRegistry, userId);
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        environment = builder.build();
        searchAdminServiceClient =
                new SearchAdminServiceClient(environment.getGreg()
                                                     .getBackEndUrl(),
                                             userInfo.getUserName(),
                                             userInfo.getPassword());
        activityAdminServiceClient =
                new ActivityAdminServiceClient(environment.getGreg()
                                                       .getBackEndUrl(),
                                               userInfo.getUserName(),
                                               userInfo.getPassword());
    }

    @Test(groups = {"wso2.greg"}, description = "Add 1000 endpoints", priority = 1)
    public void testAddLargeNumberOfEndpoints() throws RegistryException {

        EndpointManager endpointManager = new EndpointManager(governance);
        log.info("adding " + NUMBER_OF_ENDPOINTS + "of Endpoints...");
        for (int i = 1; i < NUMBER_OF_ENDPOINTS; i++) {
            Endpoint ep1 = endpointManager.newEndpoint("http://wso2.automation.endpoint" + i);
            endpointManager.addEndpoint(ep1);
            assertTrue(endpointManager.getEndpoint(ep1.getId()).getQName().toString()
                               .contains("http://wso2.automation.endpoint" + i),
                       "Endpoint not found");
        }

        for (int i = 1; i < NUMBER_OF_ENDPOINTS; i++) {
            governance.delete("trunk/endpoints" + "/ep-wso2-automation-endpoint" + i);
        }
    }

    @Test(groups = "wso2.greg", description = "Add resource", priority = 2)
    public void testAddResource() throws RemoteException, MalformedURLException,
                                         ResourceAdminServiceExceptionException,

                                         RegistryException, FileNotFoundException,
                                         LoginAuthenticationExceptionException,
                                         LogoutAuthenticationExceptionException {

        String resourcePath =
                ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
                File.separator + "GREG/rxt" + File.separator + "event.rxt";

        Resource rxt = governance.newResource();
        rxt.setContentStream(new FileInputStream(resourcePath));
        rxt.setMediaType("application/vnd.wso2.registry-ext-type+xml");
        governance.put("repository/components/org.wso2.carbon.governance/types/event.rxt", rxt);
        assertTrue(governance.resourceExists("repository/components/org.wso2.carbon.governance/types/event.rxt"),
                   "rxt resource doesn't exists");
    }

    @Test(groups = {"wso2.greg"}, description = "add new rxt file", dependsOnMethods = "testAddResource")
    public void testAddLargeNumberOfGenericArtifacts() throws Exception {

        artifactManager = new GenericArtifactManager(governance, "events");

        GenericArtifact artifact;
        log.info("adding " + NUMBER_OF_ARTIFACTS + "of Generic Artifacts...");
        for (int i = 1; i <= NUMBER_OF_ARTIFACTS; i++) {
            String governanceArtifactContent =
                    "<metadata xmlns=\"http://www.wso2" +
                    ".org/governance/metadata\"><details><author>testAuthor" +
                    "</author><venue>Colombo</venue><date>12/12/2012</date>" +
                    "<name>testEvent" +
                    i +
                    "</name>" +
                    "</details><overview>" +
                    "<namespace></namespace></overview><serviceLifecycle>" +
                    "<lifecycleName>ServiceLifeCycle</lifecycleName>" +
                    "</serviceLifecycle><rules>" +
                    "<gender>male</gender>" +
                    "<description>Coding event</description></rules></metadata>";

            artifact =
                    artifactManager.newGovernanceArtifact(AXIOMUtil.stringToOM(governanceArtifactContent));
            artifactManager.addGenericArtifact(artifact);
        }

    }

    @Test(groups = {"wso2.greg"}, description = "Add 1000 policies", priority = 3)
    public void testAddLargeNumberOfPolicies() throws GovernanceException {

        policyManager = new PolicyManager(governance);
        int policyCountBeforeTest = policyManager.getAllPolicies().length;
        Policy policy;
        log.info("adding " + NUMBER_OF_POLICIES + "of Policies...");

        for (int i = 1; i <= NUMBER_OF_POLICIES; i++) {
            policy =
                    policyManager.newPolicy("http://svn.wso2.org/repos/wso2/carbon/platform/"
                                            + "trunk/platform-integration/system-test-framework"
                                            + "/core/org.wso2.automation.platform.core/src/main/"
                                            + "resources/artifacts/GREG/policy/UTPolicy.xml");
//            policy.setName("WSO2AutomationUTPolicy" + i + ".xml");
            policyManager.addPolicy(policy);
        }

        // delete policies

        Policy[] policies = policyManager.getAllPolicies();
        int policyCountAfterTest = policies.length;
        assertTrue(((policyCountAfterTest - policyCountBeforeTest) == NUMBER_OF_POLICIES),
                   "All " + NUMBER_OF_POLICIES + "policies were not added");
        for (Policy policyEntry : policies) {
            if (policyEntry.getQName().toString().contains("WSO2AutomationUTPolicy")) {
                policyManager.removePolicy(policyEntry.getId());
                Assert.assertNull(policyManager.getPolicy(policyEntry.getId()));
            }
        }
    }

    @Test(groups = {"wso2.greg"}, description = "add 10000 resources to registry", priority = 4)
    public void testAddLargeNumberOfServices() throws Exception {
        serviceManager = new ServiceManager(governance);
        Service newService;
        final String serviceName = "WSO2AutomatedService";
        log.info("adding " + NO_OF_SERVICES + "of Services...");
        for (int i = 1; i <= NO_OF_SERVICES; i++) {
            Service service =
                    serviceManager.newService(new QName("http://wso2.test" +
                                                        ".automation/boom/test" + i,
                                                        serviceName + i));
            service.addAttribute("testAttribute", "service" + i);
            serviceManager.addService(service);
            String serviceId = service.getId();
            newService = serviceManager.getService(serviceId);
            assertTrue(newService.getQName().toString().contains(serviceName + i));
            assertEquals(newService.getAttribute("testAttribute"), "service" + i);

        }
        assertTrue(serviceManager.getAllServices().length >= NO_OF_SERVICES, "Less than " +
                                                                             NO_OF_SERVICES +
                                                                             " services exists");

        assertTrue(serviceManager.getAllServiceIds().length >= NO_OF_SERVICES, "Less than " +
                                                                               NO_OF_SERVICES +
                                                                               "  ids exists");

    }

    @Test(groups = {"wso2.greg"}, description = "Attache 1000 endpoints to a service", priority = 5)
    public void testAttachLargeNumberOfEndpoints() throws RegistryException {
        String service_namespace = "http://wso2.org/atomation/test";
        String service_name = "ServiceForLargeNumberOfEndpoints";

        ServiceManager serviceManager = new ServiceManager(governance);
        Service service;
        service = serviceManager.newService(new QName(service_namespace, service_name));
        serviceManager.addService(service);
        EndpointManager endpointManager = new EndpointManager(governance);
        log.info("Attaching " + NUMBER_OF_ENDPOINTS + "of Endpoints to a service...");
        for (int i = 1; i <= NUMBER_OF_ENDPOINTS; i++) {
            Endpoint ep1 = endpointManager.newEndpoint("http://wso2.automation" + ".endpoint" + i);
            endpointManager.addEndpoint(ep1);
            service.attachEndpoint(ep1);
        }

        Endpoint[] endpoints = service.getAttachedEndpoints();
        assertEquals(NUMBER_OF_ENDPOINTS, endpoints.length);

        // Detach Endpoint one
        int noOfEndPoints = NUMBER_OF_ENDPOINTS;
        for (Endpoint endpoint : endpoints) {
            service.detachEndpoint(endpoint.getId());
            noOfEndPoints--;
            Assert.assertTrue(noOfEndPoints == service.getAttachedEndpoints().length);
        }

        // remove the service
        serviceManager.removeService(service.getId());
        Assert.assertNull(serviceManager.getService(service.getId()));
    }

    @Test(groups = {"wso2.greg"}, description = "Attache 100 policies to a service", priority = 6)
    public void testAttachLargeNumberOfPolicies() throws RegistryException {
        String service_namespace = "http://wso2.org/atomation/test";
        String service_name = "ServiceForLargeNumberOfPolicies1";

        ServiceManager serviceManager = new ServiceManager(governance);
        Service service;
        service = serviceManager.newService(new QName(service_namespace, service_name));
        serviceManager.addService(service);
        PolicyManager policyManager = new PolicyManager(governance);
        log.info("Attaching " + NUMBER_OF_POLICIES + "of Policies to a service...");
        for (int i = 1; i <= NUMBER_OF_POLICIES; i++) {
            Policy policy =
                    policyManager.newPolicy("https://svn.wso2.org/repos/wso2/carbon/platform"
                                            + "/trunk/platform-integration/system-test-framework"
                                            + "/core/org.wso2.automation.platform.core/src/main"
                                            + "/resources/artifacts/GREG/policy/UTPolicy.xml");
//            policy.setName("testPolicy" + i);
            policyManager.addPolicy(policy);
            service.attachPolicy(policy);
        }

        Policy[] policies = service.getAttachedPolicies();
        assertEquals(NUMBER_OF_POLICIES, policies.length);

        // Detach Endpoint one
        int numberOfPolicies = NUMBER_OF_POLICIES;
        for (Policy policy : policies) {
            service.detachPolicy(policy.getId());
            numberOfPolicies--;
            Assert.assertTrue(numberOfPolicies == service.getAttachedPolicies().length);
        }

        // remove the service
        serviceManager.removeService(service.getId());
        Assert.assertNull(serviceManager.getService(service.getId()));
    }

    @Test(groups = {"wso2.greg"}, description = "Adding large number of schemas", priority = 7)
    public void testAddLargeNoOfSchemas() throws GovernanceException {
        Schema schema;
        String schemaContent =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<xsd:schema xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" "
                + "targetNamespace=\"http://www.company.org\" xmlns=\"http://www.company.org\" "
                + "elementFormDefault=\"qualified\">\n"
                + "    <xsd:complexType name=\"PersonType\">\n"
                + "        <xsd:sequence>\n"
                + "           <xsd:element name=\"Name\" type=\"xsd:string\"/>\n"
                + "           <xsd:element name=\"SSN\" type=\"xsd:string\"/>\n"
                + "        </xsd:sequence>\n" + "    </xsd:complexType>\n"
                + "</xsd:schema>";

        schemaManager = new SchemaManager(governance);

        int NUMBER_OF_SCHEMAS = 10;
        log.info("Adding " + NUMBER_OF_SCHEMAS + " of Schemas..");
        try {
            for (int i = 0; i <= NUMBER_OF_SCHEMAS; i++) {
                schema =
                        schemaManager.newSchema(schemaContent.getBytes(), "AutomatedSchema" + i +
                                                                          ".xsd");
                schemaManager.addSchema(schema);
                if (!schemaManager.getSchema(schema.getId()).getQName().getLocalPart()
                        .equalsIgnoreCase("AutomatedSchema" + i + ".xsd")) {
                    assertTrue(false, "Schema not added..");
                }
            }
        } catch (GovernanceException e) {
            throw new GovernanceException("Error found while adding multiple schemas : " +
                                          e.getMessage());
        } catch (RegistryException e) {
            e.printStackTrace();
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Test adding multiple wsdl", priority = 8)
    public void testMultipleWsdl() throws GovernanceException, IOException {
        Wsdl wsdl;

        wsdlManager = new WsdlManager(governance);
        String wsdlFileLocation =
                ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
                File.separator + "GREG" + File.separator + "wsdl" +
                File.separator + "Automated.wsdl";

        int NUMBER_OF_WSDLS = 10;
        log.info("Adding " + NUMBER_OF_WSDLS + " of WSDLs..");
        try {
            for (int i = 0; i <= NUMBER_OF_WSDLS; i++) {
                wsdl =
                        wsdlManager.newWsdl(FileManager.readFile(wsdlFileLocation).getBytes(),
                                            "AutomatedWsdl" + i + ".wsdl");
                wsdlManager.addWsdl(wsdl);
                if (!wsdlManager.getWsdl(wsdl.getId()).getQName().getLocalPart()
                        .equalsIgnoreCase("AutomatedWsdl" + i + ".wsdl")) {
                    assertTrue(false, "Wsdl not added..");
                }
            }
        } catch (GovernanceException e) {
            throw new GovernanceException("Error found while adding multiple Wsdl : " +
                                          e.getMessage());
        } catch (RegistryException e) {
            e.printStackTrace();
        }
    }

    /**
     * Related to jira issue: https://wso2.org/jira/browse/REGISTRY-724
     *
     * @throws GovernanceException
     */
    @Test(groups = {"wso2.greg"}, description = "add a service to the loaded registry", dependsOnMethods = "testAddLargeNumberOfServices")
    public void testAddService() throws GovernanceException {
        Service service = serviceManager.newService(new QName("http://bang.boom.com/mnm/beep", "MyService"));

        service.addAttribute("overview_version", "5.1.0");
        service.addAttribute("overview_description", "Test");
        service.addAttribute("interface_wsdlUrl", WSDL_URL);
        service.addAttribute("docLinks_documentType", "test");
        service.addAttribute("interface_messageFormats", "SOAP 1.2");
        service.addAttribute("interface_messageExchangePatterns", "Request Response");
        service.addAttribute("security_authenticationPlatform ", "XTS-WS TRUST");
        service.addAttribute("security_authenticationMechanism", "InfoCard");
        service.addAttribute("security_messageIntegrity", "WS-Security");
        service.addAttribute("security_messageEncryption", "WS-Security");

        long startTime = System.currentTimeMillis();
        serviceManager.addService(service);
        long endTime = System.currentTimeMillis();
        log.info("adding a service to the loaded registry took " + (endTime - startTime) + " ms");
        Assert.assertTrue((endTime - startTime) < 30000);
        serviceManager.removeService(service.getId());

    }

    /**
     * related to jira issue: https://wso2.org/jira/browse/REGISTRY-838
     *
     * @throws SearchAdminServiceRegistryExceptionException
     *
     * @throws RemoteException
     * @throws RegistryException
     */
    @Test(groups = {"wso2.greg"}, description = "Search by content", enabled = false, priority = 9)
    public void searchByContent() throws SearchAdminServiceRegistryExceptionException,
                                         RemoteException, RegistryException {

        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setContent("automation");
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        paramList = null;
        AdvancedSearchResultsBean result =
                searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0),
                          "No Record Found. set valid property name");
    }

    /**
     * related to jira issue: https://wso2.org/jira/browse/REGISTRY-838
     *
     * @throws GovernanceException
     */
    @Test(groups = {"wso2.greg"}, description = "Search by content", priority = 9)
    public void listGenaricArtifacts() throws GovernanceException {

        Assert.assertNotNull(artifactManager.getAllGenericArtifacts());
    }

    /**
     * related to jira issue: https://wso2.org/jira/browse/REGISTRY-838
     *
     * @throws RegistryExceptionException
     * @throws RemoteException
     */
    @Test(groups = {"wso2.greg"}, description = "Activity Search", priority = 9)
    public void testActivitySearch() throws RemoteException, RegistryExceptionException {
        long startTime = System.currentTimeMillis();
        String[] activites =
                activityAdminServiceClient.getActivities(environment.getGreg()
                                                                 .getSessionCookie(),
                                                         userInfo.getUserName(), "",
                                                         "", "", "", 0).getActivity();
        long endTime = System.currentTimeMillis();
        log.info("browsing the activitys took " + (endTime - startTime) + " ms");
        Assert.assertTrue((endTime - startTime) < 30000);
        assertNotNull(activites);
    }

    @AfterClass()
    public void cleanup() throws GovernanceException, RegistryException {

        // delete all artifacts
        int counter = 0;
        GenericArtifact[] genericArtifacts = artifactManager.getAllGenericArtifacts();
        for (GenericArtifact genericArtifact : genericArtifacts) {

            if (genericArtifact.getPath().contains("testEvent")) {
                counter++;
                artifactManager.removeGenericArtifact(genericArtifact.getId());
                assertNull(artifactManager.getGenericArtifact(genericArtifact.getId()));
            }
        }
        assertTrue(counter == NUMBER_OF_ARTIFACTS, "All artifacts were not added.");

        // delete services
        String[] servicePaths = serviceManager.getAllServicePaths();
        int numberOfServices = NO_OF_SERVICES;
        for (String servicePath : servicePaths) {
            if (servicePath.contains(serviceName)) {
                governance.delete(servicePath);
                numberOfServices--;
                ServiceFilter filter = new ServiceFilter() {
                    public boolean matches(Service service) throws GovernanceException {
                        return service.getQName().toString().contains(serviceName);
                    }
                };
                assertTrue(serviceManager.findServices(filter).length == numberOfServices);
            }
        }

        //delete wsdls
        Wsdl[] wsdlList = wsdlManager.getAllWsdls();
        for (Wsdl w : wsdlList) {
            if (w.getQName().getLocalPart().contains("Automated")) {
                wsdlManager.removeWsdl(w.getId());
            }
        }

        //delete schemas
        Schema[] schemaList = schemaManager.getAllSchemas();
        for (Schema s : schemaList) {
            if (s.getQName().getLocalPart().contains("Automated")) {
                schemaManager.removeSchema(s.getId());
            }
        }

        //delete policies
        Policy[] policies = policyManager.getAllPolicies();
        for (Policy policyEntry : policies) {
            if (policyEntry.getQName().toString().contains("testPolicy")) {
                policyManager.removePolicy(policyEntry.getId());
                Assert.assertNull(policyManager.getPolicy(policyEntry.getId()));
            }
        }
    }
}