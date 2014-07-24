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

package org.wso2.carbon.registry.jira.issues.test;

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
import org.wso2.greg.integration.common.clients.LifeCycleManagementClient;
import org.wso2.greg.integration.common.clients.ListMetaDataServiceClient;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.rmi.RemoteException;

public class Carbon10714TestCase extends GREGIntegrationBaseTest {

    private static final String SERVICE_LIFE_CYCLE = "ServiceLifeCycle";
    private final static String WSDL_URL =
            "https://svn.wso2.org/repos/wso2/carbon/platform/trunk/"
            + "platform-integration/clarity-tests/org.wso2.carbon.automation.test.repo/"
            + "src/main/resources/artifacts/GREG/wsdl/info.wsdl";
    ServiceManager serviceManager;
    ResourceAdminServiceClient resourceAdminServiceClient;
    private LifeCycleManagementClient lifeCycleManagerAdminService;
    private ListMetaDataServiceClient listMetaDataServiceClient;
    Service serviceForSearching1, serviceAForSearching2, serviceForSearching3,
            serviceForSearching4, serviceForSearching5i, serviceForSearching6, serviceForSearching7,
            serviceForSearching8, serviceForSearching9, serviceForSearching10, serviceForPromoting,
            searchResultPromoted;
    private LifeCycleAdminServiceClient lifeCycleAdminService;
    private Registry governance;

    @BeforeClass()
    public void initialize() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String sessionCookie = getSessionCookie();

        WSRegistryServiceClient wsRegistry =
                new RegistryProviderUtil().getWSRegistry(automationContext);
        governance = new RegistryProviderUtil().getGovernanceRegistry(wsRegistry, automationContext);
        serviceManager = new ServiceManager(governance);


        lifeCycleManagerAdminService =
                new LifeCycleManagementClient(backendURL, sessionCookie);
        lifeCycleAdminService =
                new LifeCycleAdminServiceClient(backendURL, sessionCookie);
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(backendURL, sessionCookie);
        listMetaDataServiceClient =
                new ListMetaDataServiceClient(backendURL, sessionCookie);

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

        serviceAForSearching2 =
                serviceManager.newService(new QName(
                        "http://serviceA.for.searching2/mnm/",
                        "serviceAForSearching2"));
        serviceAForSearching2.addAttribute("overview_version", "4.0.0");
        serviceAForSearching2.addAttribute("overview_description", "Test");
        serviceAForSearching2.addAttribute("interface_wsdlUrl", WSDL_URL);
        serviceAForSearching2.addAttribute("docLinks_documentType", "test");
        serviceAForSearching2.addAttribute("interface_messageFormats", "SOAP 1.2");
        serviceAForSearching2.addAttribute("interface_messageExchangePatterns", "Request Response");
        serviceAForSearching2.addAttribute("security_authenticationPlatform ", "XTS-WS TRUST");
        serviceAForSearching2.addAttribute("security_authenticationMechanism", "InfoCard");
        serviceAForSearching2.addAttribute("security_messageIntegrity", "WS-Security");
        serviceAForSearching2.addAttribute("security_messageEncryption", "WS-Security");
        serviceManager.addService(serviceAForSearching2);

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

        serviceForSearching5i =
                serviceManager.newService(new QName(
                        "http://service.for.searching5/mnm/",
                        "serviceForSearching5i"));
        serviceForSearching5i.addAttribute("overview_version", "5.1.1");
        serviceForSearching5i.addAttribute("overview_description", "Test");
        serviceForSearching5i.addAttribute("interface_wsdlUrl", WSDL_URL);
        serviceForSearching5i.addAttribute("docLinks_documentType", "test");
        serviceForSearching5i.addAttribute("interface_messageFormats", "SOAP 1.2");
        serviceForSearching5i.addAttribute("interface_messageExchangePatterns", "Request Response");
        serviceForSearching5i.addAttribute("security_authenticationPlatform ", "XTS-WS TRUST");
        serviceForSearching5i.addAttribute("security_authenticationMechanism", "InfoCard");
        serviceForSearching5i.addAttribute("security_messageIntegrity", "WS-Security");
        serviceForSearching5i.addAttribute("security_messageEncryption", "WS-Security");
        serviceManager.addService(serviceForSearching5i);

        serviceForSearching6 =
                serviceManager.newService(new QName(
                        "http://service.for.searching6/mnm/",
                        "serviceForSearching6"));
        serviceForSearching6.addAttribute("overview_version", "6.1.1");
        serviceForSearching6.addAttribute("overview_description", "Test");
        serviceForSearching6.addAttribute("interface_wsdlUrl", WSDL_URL);
        serviceForSearching6.addAttribute("docLinks_documentType", "test");
        serviceForSearching6.addAttribute("interface_messageFormats", "SOAP 1.2");
        serviceForSearching6.addAttribute("interface_messageExchangePatterns", "Request Response");
        serviceForSearching6.addAttribute("security_authenticationPlatform ", "XTS-WS TRUST");
        serviceForSearching6.addAttribute("security_authenticationMechanism", "InfoCard");
        serviceForSearching6.addAttribute("security_messageIntegrity", "WS-Security");
        serviceForSearching6.addAttribute("security_messageEncryption", "WS-Security");
        serviceManager.addService(serviceForSearching6);

        serviceForSearching7 =
                serviceManager.newService(new QName(
                        "http://service.for.searching7/mnm/",
                        "serviceForSearching7"));
        serviceForSearching7.addAttribute("overview_version", "7.1.1");
        serviceForSearching7.addAttribute("overview_description", "Test");
        serviceForSearching7.addAttribute("interface_wsdlUrl", WSDL_URL);
        serviceForSearching7.addAttribute("docLinks_documentType", "test");
        serviceForSearching7.addAttribute("interface_messageFormats", "SOAP 1.2");
        serviceForSearching7.addAttribute("interface_messageExchangePatterns", "Request Response");
        serviceForSearching7.addAttribute("security_authenticationPlatform ", "XTS-WS TRUST");
        serviceForSearching7.addAttribute("security_authenticationMechanism", "InfoCard");
        serviceForSearching7.addAttribute("security_messageIntegrity", "WS-Security");
        serviceForSearching7.addAttribute("security_messageEncryption", "WS-Security");
        serviceManager.addService(serviceForSearching7);

        serviceForSearching8 =
                serviceManager.newService(new QName(
                        "http://service.for.searching8/mnm/",
                        "serviceForSearching8"));
        serviceForSearching8.addAttribute("overview_version", "8.1.1");
        serviceForSearching8.addAttribute("overview_description", "Test");
        serviceForSearching8.addAttribute("interface_wsdlUrl", WSDL_URL);
        serviceForSearching8.addAttribute("docLinks_documentType", "test");
        serviceForSearching8.addAttribute("interface_messageFormats", "SOAP 1.2");
        serviceForSearching8.addAttribute("interface_messageExchangePatterns", "Request Response");
        serviceForSearching8.addAttribute("security_authenticationPlatform ", "XTS-WS TRUST");
        serviceForSearching8.addAttribute("security_authenticationMechanism", "InfoCard");
        serviceForSearching8.addAttribute("security_messageIntegrity", "WS-Security");
        serviceForSearching8.addAttribute("security_messageEncryption", "WS-Security");
        serviceManager.addService(serviceForSearching8);

        serviceForSearching9 =
                serviceManager.newService(new QName(
                        "http://service.for.searching9/mnm/",
                        "serviceForSearching9"));
        serviceForSearching9.addAttribute("overview_version", "9.1.1");
        serviceForSearching9.addAttribute("overview_description", "Test");
        serviceForSearching9.addAttribute("interface_wsdlUrl", WSDL_URL);
        serviceForSearching9.addAttribute("docLinks_documentType", "test");
        serviceForSearching9.addAttribute("interface_messageFormats", "SOAP 1.2");
        serviceForSearching9.addAttribute("interface_messageExchangePatterns", "Request Response");
        serviceForSearching9.addAttribute("security_authenticationPlatform ", "XTS-WS TRUST");
        serviceForSearching9.addAttribute("security_authenticationMechanism", "InfoCard");
        serviceForSearching9.addAttribute("security_messageIntegrity", "WS-Security");
        serviceForSearching9.addAttribute("security_messageEncryption", "WS-Security");
        serviceManager.addService(serviceForSearching9);

        serviceForSearching10 =
                serviceManager.newService(new QName(
                        "http://service.for.searching10/mnm/",
                        "serviceForSearching10"));
        serviceForSearching10.addAttribute("overview_version", "10.1.1");
        serviceForSearching10.addAttribute("overview_description", "Test");
        serviceForSearching10.addAttribute("interface_wsdlUrl", WSDL_URL);
        serviceForSearching10.addAttribute("docLinks_documentType", "test");
        serviceForSearching10.addAttribute("interface_messageFormats", "SOAP 1.2");
        serviceForSearching10.addAttribute("interface_messageExchangePatterns", "Request Response");
        serviceForSearching10.addAttribute("security_authenticationPlatform ", "XTS-WS TRUST");
        serviceForSearching10.addAttribute("security_authenticationMechanism", "InfoCard");
        serviceForSearching10.addAttribute("security_messageIntegrity", "WS-Security");
        serviceForSearching10.addAttribute("security_messageEncryption", "WS-Security");
        serviceManager.addService(serviceForSearching10);

    }

    /**
     * Promoted services, that should order according to version as they have
     * the same name
     *
     * @throws GovernanceException
     * @throws LifeCycleManagementServiceExceptionException
     *
     * @throws RemoteException
     * @throws CustomLifecyclesChecklistAdminServiceExceptionException
     *
     */
    @Test(groups = {"wso2.greg"}, description = "Promoted Services")
    public void testPromotedServices() throws RegistryException, RemoteException,
            LifeCycleManagementServiceExceptionException,
            CustomLifecyclesChecklistAdminServiceExceptionException, XMLStreamException {

        String content = "<serviceMetaData xmlns=\"http://www.wso2.org/governance/metadata\">" +
                "<overview><name>" + "serviceForPromotingNew1" + "</name><namespace>" +
                "http://service.delete.branch/mnm/beep" + "</namespace><version>1.0.0-SNAPSHOT</version></overview>" +
                "</serviceMetaData>";
        org.apache.axiom.om.OMElement XMLContent = AXIOMUtil.stringToOM(content);
        serviceForPromoting = serviceManager.newService(XMLContent);
        serviceManager.addService(serviceForPromoting);
        String servicePathDev = "/_system/governance" + serviceForPromoting.getPath();
        ArrayOfString[] parameters = new ArrayOfString[2];
        parameters[0] = new ArrayOfString();
        parameters[0].setArray(new String[]{servicePathDev, "2.0.0"});
        serviceForPromoting.attachLifecycle(SERVICE_LIFE_CYCLE);
        String ACTION_PROMOTE = "Promote";
        lifeCycleAdminService.invokeAspectWithParams(servicePathDev, SERVICE_LIFE_CYCLE,
                                                     ACTION_PROMOTE, null, parameters);
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
        Service searchResult = serviceManager.findServices(new ServiceFilter() {
            public boolean matches(Service service) throws GovernanceException {
                String attributeVal = service.getAttribute("overview_name");
                String attributeVal2 = service.getAttribute("overview_version");
                return attributeVal != null && attributeVal.startsWith("serviceForPromotingNew1") &&
                       attributeVal2.startsWith("1.0.0-SNAPSHOT");
            }
        })[0];
        searchResultPromoted = serviceManager.findServices(new ServiceFilter() {
            public boolean matches(Service service) throws GovernanceException {
                String attributeVal = service.getAttribute("overview_name");
                String attributeVal2 = service.getAttribute("overview_version");
                return attributeVal != null && attributeVal.startsWith("serviceForPromotingNew1") &&
                       attributeVal2.startsWith("2.0.0");
            }
        })[0];

        Assert.assertEquals(searchResult.getAttribute("overview_version"), "1.0.0-SNAPSHOT");
        Assert.assertEquals(searchResultPromoted.getAttribute("overview_version"), "2.0.0");
    }

    /**
     * Listing the services and checking on the sorted list
     *
     * @throws ResourceAdminServiceExceptionException
     *
     * @throws RemoteException
     */
    @Test(groups = {"wso2.greg"}, description = "Try out wild card search from the basic filter",
          dependsOnMethods = "testPromotedServices")
    public void testWildCardSearch()
            throws RemoteException, ResourceAdminServiceExceptionException, RegistryException {
        String criteria = null;
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
        Service [] services = serviceManager.getAllServices();
        int count = 0;

        for (Service service : services) {
            if (service.getQName().getLocalPart().contains("serviceForSearching") ||
                    service.getQName().getLocalPart().contains("serviceForPromotingNew1")) {
                count++;
            }
        }
        Assert.assertEquals(count, 11);
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() throws RegistryException {

        deleteService(serviceForPromoting);
        deleteService(serviceForSearching1);
        deleteService(serviceAForSearching2);
        deleteService(serviceForSearching3);
        deleteService(serviceForSearching4);
        deleteService(serviceForSearching5i);
        deleteService(serviceForSearching6);
        deleteService(serviceForSearching7);
        deleteService(serviceForSearching8);
        deleteService(serviceForSearching9);
        deleteService(serviceForSearching10);

        serviceManager.removeService(serviceManager.findServices(new ServiceFilter() {
            public boolean matches(Service service) throws GovernanceException {
                String attributeVal = service.getAttribute("overview_name");
                String attributeVal2 = service.getAttribute("overview_version");
                return attributeVal != null && attributeVal.startsWith("serviceForPromoting") &&
                       attributeVal2.startsWith("2.0.0");
            }
        })[0].getId());

        serviceManager = null;
        resourceAdminServiceClient = null;
        lifeCycleManagerAdminService = null;
        listMetaDataServiceClient = null;
        serviceForSearching1 = null;
        serviceAForSearching2 = null;
        serviceForSearching3 = null;
        serviceForSearching4 = null;
        serviceForSearching5i = null;
        serviceForSearching6 = null;
        serviceForSearching7 = null;
        serviceForSearching8 = null;
        serviceForSearching9 = null;
        serviceForSearching10 = null;
        serviceForPromoting = null;
        searchResultPromoted = null;
        lifeCycleAdminService = null;
    }

    private void deleteService(Service service) throws RegistryException {
        if (service != null) {
            if (governance.resourceExists(service.getPath())) {
                serviceManager.removeService(service.getId());
            }
        }
    }


}