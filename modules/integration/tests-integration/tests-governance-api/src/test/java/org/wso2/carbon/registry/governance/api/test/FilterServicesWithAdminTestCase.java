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

package org.wso2.carbon.registry.governance.api.test;

import org.apache.axiom.om.util.AXIOMUtil;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.services.ServiceFilter;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.services.ArrayOfString;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.clients.LifeCycleAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.rmi.RemoteException;

public class FilterServicesWithAdminTestCase extends GREGIntegrationBaseTest {
    private static final String SERVICE_LIFE_CYCLE = "ServiceLifeCycle";
    private final static String WSDL_URL =
            "https://raw.githubusercontent.com/wso2/wso2-qa-artifacts/master/automation-artifacts/greg/wsdl/info.wsdl";
    private ServiceManager serviceManager;

    private Service serviceForSearching1;
    private Service serviceForSearching2;
    private Service serviceForSearching3;
    private Service serviceForSearching4;
    private Service serviceForSearching5;
    private Service serviceForPromoting;
    private Service searchResultPromoted;
    private Service serviceForSearching6;
    private LifeCycleAdminServiceClient lifeCycleAdminService;
    private Registry governance;

    @BeforeClass(alwaysRun = true)
    public void initialize() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        WSRegistryServiceClient wsRegistry =
                new RegistryProviderUtil().getWSRegistry(automationContext);
        governance = new RegistryProviderUtil().getGovernanceRegistry(wsRegistry, automationContext);
        serviceManager = new ServiceManager(governance);
        serviceForSearching1 =
                serviceManager.newService(new QName(
                        "http://service.for.searching1/mnm/",
                        "serviceForSearching1"));
        serviceForSearching1.addAttribute("overview_version", "3.0.0");
        serviceForSearching1.addAttribute("overview_description", "Test");
        serviceForSearching1.addAttribute("interface_wsdlUrl", WSDL_URL);
        serviceForSearching1.addAttribute("docLinks_documentType", "test");
        serviceForSearching1.addAttribute("interface_messageFormats", "SOAP 1.2");
        serviceForSearching1.addAttribute("interface_messageExchangePatterns", "Request Response");
        serviceForSearching1.addAttribute("security_authenticationPlatform ", "XTS-WS TRUST");
        serviceForSearching1.addAttribute("security_authenticationMechanism", "InfoCard");
        serviceForSearching1.addAttribute("security_messageIntegrity", "WS-Security");
        serviceForSearching1.addAttribute("security_messageEncryption", "WS-Security");
        serviceManager.addService(serviceForSearching1);

        serviceForSearching2 =
                serviceManager.newService(new QName(
                        "http://service.for.searching2/mnm/",
                        "serviceForSearching2"));
        serviceForSearching2.addAttribute("overview_version", "4.0.0");
        serviceForSearching2.addAttribute("overview_description", "Test");
        serviceForSearching2.addAttribute("interface_wsdlUrl", WSDL_URL);
        serviceForSearching2.addAttribute("docLinks_documentType", "test");
        serviceForSearching2.addAttribute("interface_messageFormats", "SOAP 1.2");
        serviceForSearching2.addAttribute("interface_messageExchangePatterns", "Request Response");
        serviceForSearching2.addAttribute("security_authenticationPlatform ", "XTS-WS TRUST");
        serviceForSearching2.addAttribute("security_authenticationMechanism", "InfoCard");
        serviceForSearching2.addAttribute("security_messageIntegrity", "WS-Security");
        serviceForSearching2.addAttribute("security_messageEncryption", "WS-Security");
        serviceManager.addService(serviceForSearching2);

        serviceForSearching3 =
                serviceManager.newService(new QName(
                        "http://service.for.searching3/mnm/",
                        "serviceForSearching3"));
        serviceForSearching3.addAttribute("overview_version", "5.0.0");
        serviceForSearching3.addAttribute("overview_description", "Test");
        serviceForSearching3.addAttribute("interface_wsdlUrl", WSDL_URL);
        serviceForSearching3.addAttribute("docLinks_documentType", "test");
        serviceForSearching3.addAttribute("interface_messageFormats", "SOAP 1.2");
        serviceForSearching3.addAttribute("interface_messageExchangePatterns", "Request Response");
        serviceForSearching3.addAttribute("security_authenticationPlatform ", "XTS-WS TRUST");
        serviceForSearching3.addAttribute("security_authenticationMechanism", "InfoCard");
        serviceForSearching3.addAttribute("security_messageIntegrity", "WS-Security");
        serviceForSearching3.addAttribute("security_messageEncryption", "WS-Security");
        serviceManager.addService(serviceForSearching3);

        serviceForSearching4 =
                serviceManager.newService(new QName(
                        "http://service.for.searching4/mnm/",
                        "serviceForSearching4"));
        serviceForSearching4.addAttribute("overview_version", "5.1.0");
        serviceForSearching4.addAttribute("overview_description", "Test");
        serviceForSearching4.addAttribute("interface_wsdlUrl", WSDL_URL);
        serviceForSearching4.addAttribute("docLinks_documentType", "test");
        serviceForSearching4.addAttribute("interface_messageFormats", "SOAP 1.2");
        serviceForSearching4.addAttribute("interface_messageExchangePatterns", "Request Response");
        serviceForSearching4.addAttribute("security_authenticationPlatform ", "XTS-WS TRUST");
        serviceForSearching4.addAttribute("security_authenticationMechanism", "InfoCard");
        serviceForSearching4.addAttribute("security_messageIntegrity", "WS-Security");
        serviceForSearching4.addAttribute("security_messageEncryption", "WS-Security");
        serviceManager.addService(serviceForSearching4);

        serviceForSearching5 =
                serviceManager.newService(new QName(
                        "http://service.for.searching5/mnm/",
                        "serviceForSearching5"));
        serviceForSearching5.addAttribute("overview_version", "5.1.1");
        serviceForSearching5.addAttribute("overview_description", "Test");
        serviceForSearching5.addAttribute("interface_wsdlUrl", WSDL_URL);
        serviceForSearching5.addAttribute("docLinks_documentType", "test");
        serviceForSearching5.addAttribute("interface_messageFormats", "SOAP 1.2");
        serviceForSearching5.addAttribute("interface_messageExchangePatterns", "Request Response");
        serviceForSearching5.addAttribute("security_authenticationPlatform ", "XTS-WS TRUST");
        serviceForSearching5.addAttribute("security_authenticationMechanism", "InfoCard");
        serviceForSearching5.addAttribute("security_messageIntegrity", "WS-Security");
        serviceForSearching5.addAttribute("security_messageEncryption", "WS-Security");
        serviceManager.addService(serviceForSearching5);
        lifeCycleAdminService = new LifeCycleAdminServiceClient(automationContext.getContextUrls().getBackEndUrl(), getSessionCookie());
    }

    /**
     * Do filtering from the basic filter available on the 'Service List' page.
     * Here I will use the service filter to mimic the process
     * <p/>
     * Search by giving single search criteria
     * <p/>
     * Add a new service, then search for the services through basic and
     * advanced search
     *
     * @throws GovernanceException
     */
    @Test(groups = {"wso2.greg"}, description = "Filter Services")
    public void testFilterServices() throws RegistryException {
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
        Service searchResult = serviceManager.findServices(new ServiceFilter() {

            public boolean matches(Service service) throws GovernanceException {
                String attributeVal = service.getAttribute("overview_name");
                return attributeVal != null && attributeVal.startsWith("serviceForSearching1");
            }
        })[0];

        Assert.assertEquals(searchResult.getAttribute("overview_name"), "serviceForSearching1",
                "overview_name should be serviceForSearching1");
    }

    /**
     * Filter from all the available fields in the 'Filter Services' advance
     * search page. Here I will use the service filter to mimic the process
     * <p/>
     * Search by giving multiple search criteria
     *
     * @throws GovernanceException
     */
    @Test(groups = {"wso2.greg"}, description = "Filter Services", dependsOnMethods = "testFilterServices")
    public void testAdvFilterServices() throws RegistryException {
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
        Service searchResult = serviceManager.findServices(new ServiceFilter() {

            public boolean matches(Service service) throws GovernanceException {
                String attributeVal1 = service.getAttribute("overview_name");
                String attributeVal2 = service.getAttribute("overview_version");
                String attributeVal3 = service.getAttribute("overview_description");
                String attributeVal4 = service.getAttribute("interface_wsdlUrl");
                String attributeVal5 = service.getAttribute("docLinks_documentType");
                String attributeVal6 = service.getAttribute("interface_messageFormats");
                String attributeVal7 = service.getAttribute("interface_messageExchangePatterns");
                String attributeVal8 = service.getAttribute("security_authenticationPlatform");
                String attributeVal9 = service.getAttribute("security_authenticationMechanism");
                String attributeVal10 = service.getAttribute("security_messageIntegrity");
                String attributeVal11 = service.getAttribute("security_messageEncryption");
                return attributeVal1 != null && attributeVal1.startsWith("serviceForSearching4") &&
                        attributeVal2 != null && attributeVal2.startsWith("5.1.0") &&
                        attributeVal3 != null && attributeVal3.startsWith("Test") &&
                        attributeVal4 != null && attributeVal4.startsWith(WSDL_URL) &&
                        attributeVal5 != null && attributeVal5.startsWith("test") &&
                        attributeVal6 != null && attributeVal6.startsWith("SOAP 1.2") &&
                        attributeVal7 != null && attributeVal7.startsWith("Request Response") &&
                        attributeVal8 != null && attributeVal8.startsWith("XTS-WS TRUST") &&
                        attributeVal9 != null && attributeVal9.startsWith("InfoCard") &&
                        attributeVal10 != null && attributeVal10.startsWith("WS-Security") &&
                        attributeVal11 != null && attributeVal11.startsWith("WS-Security");
            }
        })[0];

        Assert.assertEquals(searchResult.getAttribute("overview_name"), "serviceForSearching4");
        Assert.assertEquals(searchResult.getAttribute("overview_version"), "5.1.0");
        Assert.assertEquals(searchResult.getAttribute("overview_description"), "Test");
        Assert.assertEquals(searchResult.getAttribute("interface_wsdlUrl"), WSDL_URL);
        Assert.assertEquals(searchResult.getAttribute("docLinks_documentType"), "test");
        Assert.assertEquals(searchResult.getAttribute("interface_messageFormats"), "SOAP 1.2");
        Assert.assertEquals(searchResult.getAttribute("interface_messageExchangePatterns"),
                "Request Response");
        Assert.assertEquals(searchResult.getAttribute("security_authenticationPlatform"),
                "XTS-WS TRUST");
        Assert.assertEquals(searchResult.getAttribute("security_authenticationMechanism"),
                "InfoCard");
        Assert.assertEquals(searchResult.getAttribute("security_messageIntegrity"), "WS-Security");
        Assert.assertEquals(searchResult.getAttribute("security_messageEncryption"), "WS-Security");
    }

    /*
     * Edit the content of the service and then search for the services through
     * basic and advanced search
     */
    @Test(groups = {"wso2.greg"}, description = "Filter Edited Services", dependsOnMethods = "testAdvFilterServices")
    public void testFilterEditedServices() throws RegistryException {
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
        serviceForSearching2.addAttribute("test-att", "test-val");
        serviceManager.updateService(serviceForSearching2);
        Service[] searchResult = serviceManager.findServices(new ServiceFilter() {

            public boolean matches(Service service) throws GovernanceException {
                String attributeVal = service.getAttribute("test-att");
                return attributeVal != null && attributeVal.startsWith("test-val");
            }
        });
        boolean resultFound = false;
        for (Service service : searchResult) {
            if (service.getAttribute("overview_name").equals("serviceForSearching2")) {
                Assert.assertTrue(true);
                resultFound = true;
            }
        }
        Assert.assertTrue(resultFound, "serviceForSearching2 is not found");
    }

    /**
     * Search for a particular service. Then promote that service to the next
     * LC state and search to see whether both the original service as well as
     * the promoted service is captured through the search
     *
     * @throws GovernanceException
     * @throws LifeCycleManagementServiceExceptionException
     *
     * @throws RemoteException
     * @throws CustomLifecyclesChecklistAdminServiceExceptionException
     *
     */
    @Test(groups = {"wso2.greg"}, description = "Filter Promoted Services", dependsOnMethods = "testFilterEditedServices")
    public void testFilterPromotedServices() throws GovernanceException, RemoteException,
            LifeCycleManagementServiceExceptionException,
            CustomLifecyclesChecklistAdminServiceExceptionException, XMLStreamException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<serviceMetaData xmlns=\"http://www.wso2.org/governance/metadata\">");
        stringBuilder.append("<overview><name>");
        stringBuilder.append("serviceForPromoting");
        stringBuilder.append("</name><namespace>");
        stringBuilder.append("http://service.delete.branch/mnm/beep");
        stringBuilder.append("</namespace><version>1.0.0-SNAPSHOT</version></overview>");
        stringBuilder.append("</serviceMetaData>");
        String content = stringBuilder.toString();
        org.apache.axiom.om.OMElement XMLContent = AXIOMUtil.stringToOM(content);
        serviceForPromoting =
                serviceManager.newService(XMLContent);
        serviceManager.addService(serviceForPromoting);
        String servicePathDev = "/_system/governance" + serviceForPromoting.getPath();
        ArrayOfString[] parameters = new ArrayOfString[2];
        parameters[0] = new ArrayOfString();
        parameters[0].setArray(new String[]{servicePathDev, "2.0.0"});
        serviceForPromoting.attachLifecycle(SERVICE_LIFE_CYCLE);
        String ACTION_PROMOTE = "Promote";
        lifeCycleAdminService.invokeAspectWithParams(servicePathDev, SERVICE_LIFE_CYCLE,
                ACTION_PROMOTE, null, parameters);
        Service searchResult = serviceManager.findServices(new ServiceFilter() {
            public boolean matches(Service service) throws GovernanceException {
                String attributeVal = service.getAttribute("overview_name");
                String attributeVal2 = service.getAttribute("overview_version");
                return attributeVal != null && attributeVal.startsWith("serviceForPromoting") &&
                        attributeVal2.startsWith("1.0.0-SNAPSHOT");
            }
        })[0];

        searchResultPromoted = serviceManager.findServices(new ServiceFilter() {
            public boolean matches(Service service) throws GovernanceException {
                String attributeVal = service.getAttribute("overview_name");
                String attributeVal2 = service.getAttribute("overview_version");
                return attributeVal != null && attributeVal.startsWith("serviceForPromoting") &&
                        attributeVal2.startsWith("2.0.0");
            }
        })[0];

        Assert.assertEquals(searchResult.getAttribute("overview_version"), "1.0.0-SNAPSHOT", "overview_version should be 1.0.0-SNAPSHOT");
        Assert.assertEquals(searchResultPromoted.getAttribute("overview_version"), "2.0.0", "overview_version should be 2.0.0");
    }

    /**
     * Search for a service with particular information. Then remove that
     * information from the service and save. Search again using the same search
     * criteria and the service should not be captured now
     *
     * @throws GovernanceException
     */
    @Test(groups = {"wso2.greg"}, description = "Filter Changed Services", dependsOnMethods = "testFilterPromotedServices")
    public void testFilterChangedServices() throws GovernanceException {
        Service searchResult1 = serviceManager.findServices(new ServiceFilter() {
            public boolean matches(Service service) throws GovernanceException {
                String attributeVal = service.getAttribute("overview_description");
                String attributeVal2 = service.getAttribute("overview_version");
                String attributeVal3 = service.getAttribute("overview_name");
                return attributeVal != null && attributeVal.startsWith("Test") &&
                        attributeVal2.startsWith("5.0.0") &&
                        attributeVal3 != null && attributeVal3.startsWith("serviceForSearching");
            }
        })[0];
        Assert.assertEquals(searchResult1.getAttribute("overview_name"), "serviceForSearching3", "overview name should be serviceForSearching3");

        serviceForSearching3.removeAttribute("overview_description");
        serviceManager.updateService(serviceForSearching3);
        Service searchResult2[] = serviceManager.findServices(new ServiceFilter() {
            public boolean matches(Service service) throws GovernanceException {
                String attributeVal = service.getAttribute("overview_description");
                String attributeVal2 = service.getAttribute("overview_version");
                String attributeVal3 = service.getAttribute("overview_name");
                return attributeVal != null && attributeVal.startsWith("Test") &&
                        attributeVal2.startsWith("5.0.0") &&
                        attributeVal3 != null && attributeVal3.startsWith("serviceForSearching");
            }
        });
        Assert.assertEquals(searchResult2.length, 0, "Expecting 0 Search results");
    }

    /**
     * Delete a service and search for it
     *
     * @throws GovernanceException
     */
    @Test(groups = {"wso2.greg"}, description = "Searching for a deleted service", dependsOnMethods = "testFilterChangedServices")
    public void testSearchForDeletedService() throws GovernanceException {
        serviceManager.removeService(serviceForSearching5.getId());
        Service searchResult[] = serviceManager.findServices(new ServiceFilter() {
            public boolean matches(Service service) throws GovernanceException {
                String attributeVal = service.getAttribute("overview_name");
                return attributeVal != null && attributeVal.startsWith("serviceForSearching5");
            }
        });
        Assert.assertEquals(searchResult.length, 0, "Expecting 0 Search results");
    }

    /**
     * Try out wild card search from the basic filter
     *
     * @throws ResourceAdminServiceExceptionException
     *
     * @throws RemoteException
     */
    @Test(groups = {"wso2.greg"}, description = "Try out wild card search from the basic filter", dependsOnMethods = "testSearchForDeletedService")
    public void testWildCardSearch()
            throws RemoteException, ResourceAdminServiceExceptionException, RegistryException {
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
        Service searchResult[] = serviceManager.findServices(new ServiceFilter() {
            public boolean matches(Service service) throws GovernanceException {
                String attributeVal = service.getAttribute("overview_name");
                return attributeVal != null && attributeVal.startsWith("serviceForSearching");
            }
        });
        Assert.assertEquals(searchResult.length, 4, "Expect only 4 Search result");
        Assert.assertEquals(searchResult[0].getAttribute("overview_name"), "serviceForSearching1");
        Assert.assertEquals(searchResult[1].getAttribute("overview_name"), "serviceForSearching2");
        Assert.assertEquals(searchResult[2].getAttribute("overview_name"), "serviceForSearching3");
        Assert.assertEquals(searchResult[3].getAttribute("overview_name"), "serviceForSearching4");

    }

    /*
     * Do wild card search for almost all the fields available
     *
     */
    @Test(groups = {"wso2.greg"}, description = "Try out wild card search from the basic filter", dependsOnMethods = "testWildCardSearch")
    public void testAdvWildCardSearch() throws RemoteException,
            ResourceAdminServiceExceptionException, RegistryException {

        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
        Service searchResult[] = serviceManager.findServices(new ServiceFilter() {
            public boolean matches(Service service) throws GovernanceException {
                String attributeVal_name = service.getAttribute("overview_name");
                String attributeVal_description = service.getAttribute("overview_description");
                String attributeVal_messageFormats = service.getAttribute("interface_messageFormats");
                String attributeVal_messageExchangePatterns = service.getAttribute("interface_messageExchangePatterns");
                return attributeVal_name != null && attributeVal_name.startsWith("serviceForSearching") &&
                        attributeVal_description != null && attributeVal_description.startsWith("Tes") &&
                        attributeVal_messageFormats != null && attributeVal_messageFormats.equals("SOAP 1.2") &&
                        attributeVal_messageExchangePatterns != null && attributeVal_messageExchangePatterns.startsWith("Request Res");
            }
        });

        Assert.assertEquals(searchResult.length, 3, "Expect only 3 Search result");
        Assert.assertEquals(searchResult[0].getAttribute("overview_name"), "serviceForSearching1");
        Assert.assertEquals(searchResult[1].getAttribute("overview_name"), "serviceForSearching2");
        Assert.assertEquals(searchResult[2].getAttribute("overview_name"), "serviceForSearching4");

    }

//    TODO: FIXME: The test below has 3 bugs.
//    1. This is trying to update the content of a non-existing Asset Type (which cannot be the
//       case) so someone is removing the service.rxt.
//    2. The RXT payload does not have the Version2 field in it, which is what is used in the test.
//    3. The search result should return just one hit, since everything added up to now does not
//       have Version2.
//
//    /*
//     * Change the service UI (add new elements), fill in data and use them in
//     * 'Filter Services'
//     */
//    @Test(groups = "wso2.greg", description = "Create a service", dependsOnMethods = "testAdvWildCardSearch")
//    public void testCreateService() throws Exception {
//
//        String servicePath =
//                ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
//                File.separator + "GREG" + File.separator + "services" +
//                File.separator + "rxtConfForUITesting1.xml";
//        String serviceContent = FileManager.readFile(servicePath);
//        governanceServiceClient.saveConfiguration(serviceContent,
//                "/_system/governance/repository/components/org.wso2.carbon.governance/types/service.rxt");
//
//        Assert.assertEquals(governanceServiceClient.getConfiguration("service"), serviceContent, "Service configuration not saved");
//
//        serviceForSearching6 =
//                serviceManager.newService(new QName(
//                        "http://service.for.searching6/mnm/",
//                        "serviceForSearching6"));
//        serviceForSearching6.addAttribute("overview_version", "5.1.1");
//        serviceForSearching6.addAttribute("overview_version2", "6.1.1");
//        serviceForSearching6.addAttribute("overview_description", "Test");
//        serviceForSearching6.addAttribute("interface_wsdlUrl", WSDL_URL);
//        serviceForSearching6.addAttribute("docLinks_documentType", "test");
//        serviceForSearching6.addAttribute("interface_messageFormats", "SOAP 1.2");
//        serviceForSearching6.addAttribute("interface_messageExchangePatterns", "Request Response");
//        serviceForSearching6.addAttribute("security_authenticationPlatform ", "XTS-WS TRUST");
//        serviceForSearching6.addAttribute("security_authenticationMechanism", "InfoCard");
//        serviceForSearching6.addAttribute("security_messageIntegrity", "WS-Security");
//        serviceForSearching6.addAttribute("security_messageEncryption", "WS-Security");
//        serviceManager.addService(serviceForSearching6);
//
//        String criteria =
//                "<serviceMetaData xmlns=\"http://www.wso2.org/governance/metadata\"><overview>" +
//                "<name>serviceForSear[A-Za-z1-9]+</name><version>5.1.1</version></overview><serviceLifecycle />" +
//                "<contacts /><interface /><security /><endpoints /><docLinks /><operation xmlns=\"\">Add</operation>" +
//                "<currentName xmlns=\"\"></currentName><currentNamespace xmlns=\"\"></currentNamespace></serviceMetaData>";
//
//        Assert.assertEquals(listMetaDataServiceClient.listServices(criteria).getNames().length, 1,
//                            "Expect only 1 Search result");
//        Assert.assertEquals(listMetaDataServiceClient.listServices(criteria).getNames()[0],
//                            "serviceForSearching6", "Wrong Search result");
//    }

    @AfterClass(alwaysRun = true)
    public void endGame() throws RegistryException {

        deleteService(serviceForPromoting);
        deleteService(serviceForSearching1);
        deleteService(serviceForSearching2);
        deleteService(serviceForSearching3);
        deleteService(serviceForSearching4);
        deleteService(serviceForSearching6);
        //deleteService(searchResultPromoted);

        serviceManager = null;
        serviceForSearching1 = null;
        serviceForSearching2 = null;
        serviceForSearching3 = null;
        serviceForSearching4 = null;
        serviceForSearching5 = null;
        serviceForPromoting = null;
        searchResultPromoted = null;
        serviceForSearching6 = null;
    }

    private void deleteService(Service service) throws RegistryException {
        if (service != null) {
            if (governance.resourceExists(service.getPath())) {
                serviceManager.removeService(service.getId());
            }
        }
    }
}
