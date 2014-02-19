/*
* Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.wso2.carbon.registry.governance.api.test.old;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.registry.PropertiesAdminServiceClient;
import org.wso2.carbon.automation.api.clients.server.admin.ServerAdminClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.ServerGroupManager;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkFactory;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkProperties;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.api.endpoints.dataobjects.Endpoint;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.schema.dataobjects.Schema;
import org.wso2.carbon.governance.api.services.ServiceFilter;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.registry.api.Association;
import org.wso2.carbon.registry.core.Comment;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.properties.stub.beans.xsd.RetentionBean;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileInputStream;
import java.rmi.RemoteException;
import java.util.List;

import static org.testng.Assert.*;

public class ServiceTestClientCase {
    private static final Log log = LogFactory.getLog(ServiceTestClientCase.class);
    private Registry governance;
    private int userId = 1;
    UserInfo userInfo = UserListCsvReader.getUserInfo(userId);
    private String configPath;
    private ServerAdminClient serverAdminClient;
    private ManageEnvironment environment;
    private EnvironmentBuilder builder;
    private WSRegistryServiceClient wsRegistry;
    private RegistryProviderUtil registryProviderUtil;
    private FrameworkProperties frameworkProperties;

    @BeforeClass(groups = {"wso2.greg"})
    public void initTest() throws RegistryException, RemoteException, LoginAuthenticationExceptionException {

        builder = new EnvironmentBuilder().greg(userId);
        environment = builder.build();
        serverAdminClient = new ServerAdminClient(environment.getGreg().getProductVariables().getBackendUrl(),
                environment.getGreg().getSessionCookie());
        registryProviderUtil = new RegistryProviderUtil();
        wsRegistry = registryProviderUtil.getWSRegistry(userId,
                ProductConstant.GREG_SERVER_NAME);
        governance = registryProviderUtil.getGovernanceRegistry(wsRegistry, userId);
        configPath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
                File.separator + "GREG" + File.separator + "xml" + File.separator + "service.metadata.xml";

    }

    @Test(groups = {"wso2.greg"}, description = "add a simple service ", priority = 1)
    public void testAddService() throws Exception {
        ServiceManager serviceManager = new ServiceManager(governance);

        Service service = serviceManager.newService(new QName("http://bang.boom.com/mnm/beep", "MyService"));
        service.addAttribute("testAttribute", "somevalue");
        serviceManager.addService(service);

        String serviceId = service.getId();
        Service newService = serviceManager.getService(serviceId);

        Assert.assertEquals(newService.getAttribute("testAttribute"), "somevalue");

        service.addAttribute("testAttribute", "somevalue2");
        serviceManager.updateService(service);

        newService = serviceManager.getService(serviceId);

        String[] values = newService.getAttributes("testAttribute");

        Assert.assertEquals(values.length, 2);
    }

    @Test(groups = {"wso2.greg"}, description = "Search services ", priority = 2)
    public void testServiceSearch() throws Exception {
        File file = new File(configPath);
        FileInputStream fileInputStream = new FileInputStream(file);
        byte[] fileContents = new byte[(int) file.length()];
        fileInputStream.read(fileContents);

        OMElement contentElement = GovernanceUtils.buildOMElement(fileContents);

        ServiceManager serviceManager = new ServiceManager(governance);
        Service service = serviceManager.newService(contentElement);

        service.addAttribute("custom-attribute", "custom-value");
        serviceManager.addService(service);


        // so retrieve it back
        String serviceId = service.getId();
        Service newService = serviceManager.getService(serviceId);

        Assert.assertEquals(newService.getAttribute("custom-attribute"), "custom-value");
        Assert.assertEquals(newService.getAttribute("interface_wsdlURL"),
                "/_system/governance/trunk/wsdls/com/foo/abc.wsdl");
        Assert.assertEquals(newService.getQName(), service.getQName());

        Service service2 = serviceManager.newService(new QName("http://baps.paps.mug/done", "meep"));
        service2.addAttribute("custom-attribute", "custom-value2");
        serviceManager.addService(service2);

        Service service3 = serviceManager.newService(new QName("http://baps.paps.mug/jug", "peem"));
        service3.addAttribute("custom-attribute", "not-custom-value");
        serviceManager.addService(service3);

        Service service4 = serviceManager.newService(new QName("http://baps.dadan.mug/jug", "doon"));
        service4.addAttribute("not-custom-attribute", "custom-value3");
        serviceManager.addService(service4);

        Service[] services = serviceManager.findServices(new ServiceFilter() {
            public boolean matches(Service service) throws GovernanceException {
                String attributeVal = service.getAttribute("custom-attribute");
                return attributeVal != null && attributeVal.startsWith("custom-value");
            }
        });

//        Assert.assertEquals(services.length, 2);
        Assert.assertTrue(services[0].getQName().equals(service.getQName()) ||
                services[0].getQName().equals(service2.getQName()));
        Assert.assertTrue(services[1].getQName().equals(service.getQName()) ||
                services[1].getQName().equals(service2.getQName()));

        // update the service2
//        service2.setQName(new QName("http://bom.bom.com/baaaang", "bingo"));
//        serviceManager.updateService(service2);

//        newService = serviceManager.getService(service2.getId());
        Assert.assertEquals(service2.getAttribute("custom-attribute"), "custom-value2");


        // do the test again
        services = serviceManager.findServices(new ServiceFilter() {
            public boolean matches(Service service) throws GovernanceException {
                String attributeVal = service.getAttribute("custom-attribute");
                return attributeVal != null && attributeVal.startsWith("custom-value");
            }
        });

        Assert.assertEquals(services.length, 2);
        Assert.assertTrue(services[0].getQName().equals(service.getQName()) ||
                services[0].getQName().equals(service2.getQName()));
        Assert.assertTrue(services[1].getQName().equals(service.getQName()) ||
                services[1].getQName().equals(service2.getQName()));
    }

    @Test(groups = {"wso2.greg"}, description = "Attach meta data into service", priority = 3)
    public void testServiceAttachments() throws Exception {
        // first put a WSDL
        WsdlManager wsdlManager = new WsdlManager(governance);

        Wsdl wsdl = wsdlManager.newWsdl("https://svn.wso2.org/repos/wso2/carbon/platform/trunk/" +
                "platform-integration/system-test-framework/core/" +
                "org.wso2.automation.platform.core/src/main/resources/" +
                "artifacts/GREG/wsdl/BizService.wsdl");
        wsdlManager.addWsdl(wsdl);

        ServiceManager serviceManager = new ServiceManager(governance);
        Service service = serviceManager.newService(new QName("http://test/org/bang", "myservice"));
        serviceManager.addService(service);

        service.attachWSDL(wsdl);

        Wsdl[] wsdls = service.getAttachedWsdls();

        Assert.assertEquals(wsdls.length, 1);
        Assert.assertEquals(wsdls[0].getQName(), new QName("http://foo.com", "BizService.wsdl"));

        Schema[] schemas = wsdls[0].getAttachedSchemas();

        Assert.assertEquals(schemas.length, 1);
        Assert.assertEquals(schemas[0].getQName(),
                new QName("http://bar.org/purchasing", "purchasing.xsd"));
        Assert.assertNotNull(schemas[0].getId());

//        // now add a policy.
//        PolicyManager policyManager = new PolicyManager(governance);
//
//        Policy policy = policyManager.newPolicy("http://svn.wso2.org/repos/wso2/tags/wsf/php/2.1" +
//                                                ".0/samples/security/complete/policy.xml");
//        policy.setName("mypolicy.xml");
//        policyManager.addPolicy(policy);
//
//        service.attachPolicy(policy);
//
//        Policy[] policies = service.getAttachedPolicies();
//        Assert.assertEquals(policies.length, 1);
//        Assert.assertEquals(policies[0].getQName(), new QName("mypolicy.xml"));
//
//        File file = new File(configPath);
////        FileInputStream fileInputStream = new FileInputStream(file);
////        byte[] fileContents = new byte[(int) file.length()];
////        fileInputStream.read(fileContents);
//
////        OMElement contentElement = GovernanceUtils.buildOMElement(fileContents);
////
//        // service = serviceManager.newService(contentElement);
//        //serviceManager.addService(service);
//
//        String[] serviceIds = serviceManager.getAllServiceIds();
//        for (String serviceId : serviceIds) {
//            Service servicex = serviceManager.getService(serviceId);
//            Policy[] policiesx = servicex.getAttachedPolicies();
//            if (policiesx != null && policiesx.length != 0) {
//                Assert.assertEquals(policiesx[0].getQName(), new QName("mypolicy.xml"));
//            }
//        }

    }

//    @Test(groups = {"wso2.greg"}, description = "Rename service", priority = 4)
//    public void testServiceRename() throws Exception {
//        ServiceManager serviceManager = new ServiceManager(governance);
//
//        deleteServiceByName(serviceManager, "almdo");
//
//        Service service = serviceManager.newService(new QName("http://banga.queek.queek/blaa", "sfosf"));
//        serviceManager.addService(service);
//
//        service.setQName(new QName("http://doc.x.ge/yong", "almdo"));
//        serviceManager.updateService(service);
//
//        Service exactServiceCopy = serviceManager.getService(service.getId());
//        QName qname = exactServiceCopy.getQName();
//
//        Assert.assertEquals(new QName("http://doc.x.ge/yong", "almdo"), qname);
//        Assert.assertEquals(exactServiceCopy.getPath(), "/trunk/services/ge/x/doc/yong/almdo");
//
//
//        // doing the same for a meta data service
//        File file = new File(configPath);
//        FileInputStream fileInputStream = new FileInputStream(file);
//        byte[] fileContents = new byte[(int) file.length()];
//        fileInputStream.read(fileContents);
//
//        OMElement contentElement = GovernanceUtils.buildOMElement(fileContents);
//
//        Service service1 = serviceManager.newService(contentElement);
//        serviceManager.addService(service1);
//
//        service1.setQName(new QName("http://doc.x.ge/yong", "almdo"));
//        serviceManager.updateService(service1);
//
//        exactServiceCopy = serviceManager.getService(service.getId());
//        qname = exactServiceCopy.getQName();
//
//        Assert.assertEquals(qname, new QName("http://doc.x.ge/yong", "almdo"));
//        Assert.assertEquals(exactServiceCopy.getPath(), "/trunk/services/ge/x/doc/yong/almdo");
//
//        serviceManager.removeService(service.getId());
//
//    }

    private void deleteServiceByName(ServiceManager serviceManager, String serviceName)
            throws GovernanceException {
        Service[] serviceGet = serviceManager.getAllServices();
        for (Service service : serviceGet) {
            if (service.getQName().getLocalPart().contains(serviceName)) {
                serviceManager.removeService(service.getId());
            }
        }
    }

    @Test(groups = {"wso2.greg"}, description = "delete service ", priority = 5)
    public void testServiceDelete() throws Exception {
        ServiceManager serviceManager = new ServiceManager(governance);

        Service service = serviceManager.newService(new QName("http://banga.doom.queek/blaa", "lmnop"));
        serviceManager.addService(service);

        Service newService = serviceManager.getService(service.getId());
        Assert.assertNotNull(newService);

        serviceManager.removeService(newService.getId());
        newService = serviceManager.getService(service.getId());
        Assert.assertNull(newService);


        service = serviceManager.newService(new QName("http://banga.bang.queek/blaa", "basss"));
        serviceManager.addService(service);

        newService = serviceManager.getService(service.getId());
        Assert.assertNotNull(newService);

        governance.delete(newService.getPath());
        newService = serviceManager.getService(service.getId());
        Assert.assertNull(newService);
    }

    @Test(groups = {"wso2.greg"}, description = "add a duplicate service again with different " +
            "namespace", priority = 6)
    public void testDuplicateService() throws Exception {
        ServiceManager serviceManager = new ServiceManager(governance);

        Service service = serviceManager.newService(new QName("http://bang.boom.com/mnm/beep",
                "DuplicateService2"));
        service.addAttribute("testAttribute", "duplicate1");
        serviceManager.addService(service);
        String serviceId = service.getId();


        Service newService = serviceManager.getService(serviceId);
        assertTrue(newService.getQName().toString().contains("DuplicateService2"));
        assertEquals(newService.getAttribute("testAttribute"), "duplicate1");

        //try to add same service again
        ServiceManager serviceManagerDuplicate = new ServiceManager(governance);
        Service duplicateService = serviceManagerDuplicate.newService(new QName("http://bang.boom" +
                ".com/mnm/beep2",
                "DuplicateService2"));
        duplicateService.addAttribute("testAttributeDuplicate", "duplicate2");
        serviceManagerDuplicate.addService(duplicateService);

        Service newServiceDuplicate = serviceManagerDuplicate.getService(duplicateService.getId());
        assertTrue(newServiceDuplicate.getQName().toString().contains("DuplicateService2"));
        assertEquals(newServiceDuplicate.getAttribute("testAttributeDuplicate"), "duplicate2");

        governance.delete(newService.getPath());
        governance.delete(duplicateService.getPath());
        newService = serviceManager.getService(service.getId());
        newServiceDuplicate = serviceManagerDuplicate.getService(duplicateService.getId());
        Assert.assertNull(newService);
        Assert.assertNull(newServiceDuplicate);
    }

    @Test(groups = {"wso2.greg"}, description = "add a duplicate service again with same " +
            "namespace", priority = 7)
    public void testDuplicateServiceWithNameSpaces() throws Exception {
        ServiceManager serviceManager = new ServiceManager(governance);

        Service service = serviceManager.newService(new QName("http://bang.boom.com/mnm/beep",
                "Service1"));
        deleteServiceByName(serviceManager, "Service1");
        service.addAttribute("testAttribute", "service1");
        serviceManager.addService(service);
        String serviceId = service.getId();

        Service newService = serviceManager.getService(serviceId);
        assertTrue(newService.getQName().toString().contains("Service1"));
        assertEquals(newService.getAttribute("testAttribute"), "service1");

        //try to add same service again
        ServiceManager serviceManagerDuplicate = new ServiceManager(governance);
        Service duplicateService = serviceManagerDuplicate.newService(new QName("http://bang.boom.com/mnm/beep",
                "Service1"));
        duplicateService.addAttribute("testAttributeDuplicate", "duplicate2");
        serviceManagerDuplicate.addService(duplicateService);

        assertTrue(duplicateService.getQName().toString().contains("Service1"));
        assertEquals(duplicateService.getAttribute("testAttributeDuplicate"), "duplicate2");

        //add duplicate service though same service manger.
        service = serviceManager.newService(new QName("http://bang.boom.com/mnm/beep",
                "Service1"));
        service.addAttribute("testAttribute", "service1");
        serviceManager.addService(service);

        boolean status = false;
        String[] serviceArray = serviceManager.getAllServicePaths();
        for (String service1 : serviceArray) {
            if (service1.contains("Service1")) {
                status = true;
                break;
            }
        }

        assertTrue(status, "service not found");
        //governance.delete(newService.getPath());
    }

    @Test(groups = {"wso2.greg"}, description = "add service with special characters",
            dataProvider = "invalidCharacter", priority = 8)
    public void testServiceWithSpecialCharacters(String invalidCharacters) throws Exception {
        ServiceManager serviceManager = new ServiceManager(governance);
        boolean status = false;
        Service service = serviceManager.newService(new QName("http://bang.boom.com/mnm/beep",
                "Service1" + invalidCharacters));
        service.addAttribute("testAttribute", "service1");
        try {
            serviceManager.addService(service);
        } catch (GovernanceException ignored) {
            status = true;
            log.info("Adding service with invalid Character " + invalidCharacters);
        }
        assertTrue(status, "Invalid service added with special character - " + invalidCharacters);
    }

    @Test(groups = {"wso2.greg"}, description = "Add a wsdl to service with schema imports", priority = 9)
    public void testAttacheWsdlWithSchemaImports() throws Exception {
        // first put a WSDL
        WsdlManager wsdlManager = new WsdlManager(governance);

        Wsdl wsdl = wsdlManager.newWsdl("https://svn.wso2.org/repos/wso2/carbon/platform/branches/4.0.0/platform-integration" +
                "/clarity-tests/1.0.1/org.wso2.carbon.automation.test.repo/src/main/resources/artifacts/GREG/wsdl/BizService.wsdl");
        wsdlManager.addWsdl(wsdl);

        ServiceManager serviceManager = new ServiceManager(governance);
        Service service = serviceManager.newService(new QName("http://test/org/bang", "BizService"));
        serviceManager.addService(service);

        service.attachWSDL(wsdl);
        Wsdl[] wsdls = service.getAttachedWsdls();

        Assert.assertEquals(wsdls.length, 1);
        Assert.assertEquals(wsdls[0].getQName(), new QName("http://foo.com", "BizService.wsdl"));
        Schema[] schemas = wsdls[0].getAttachedSchemas();

        Assert.assertEquals(schemas.length, 1);
        Assert.assertEquals(schemas[0].getQName(),
                new QName("http://bar.org/purchasing", "purchasing.xsd"));
        Assert.assertNotNull(schemas[0].getId());

        Association[] associations = governance.getAllAssociations(service.getPath());

        verifyServiceAssociations(associations);

        service.detachWSDL(wsdls[0].getId());
        assertTrue(service.getAttachedWsdls().length == 0, "WSDL still exits ");

        service.detachSchema(schemas[0].getId());
        assertTrue(service.getAttachedSchemas().length == 0, "Schema still exits ");

        //remove the service
        serviceManager.removeService(service.getId());
        assertNull(serviceManager.getService(service.getId()));
    }


    @Test(groups = {"wso2.greg"}, description = "Attach LC to a service", priority = 10)
    public void testAttachLifeCycleToService() throws Exception {
        ServiceManager serviceManager = new ServiceManager(governance);

        Service service = serviceManager.newService(new QName("http://bang.boom.com/mnm/beep",
                "LCService"));
        service.addAttribute("testAttribute", "serviceAttr");
        service.setAttribute("serviceLifecycle_lifecycleName", "ServiceLifeCycle");

        serviceManager.addService(service);
        String serviceId = service.getId();

        service.attachLifecycle("ServiceLifeCycle");

        assertEquals(service.getLifecycleName(), "ServiceLifeCycle");
        assertEquals(service.getLifecycleState(), "Development");

        Service newService = serviceManager.getService(serviceId);
        assertTrue(newService.getQName().toString().contains("LCService"));
        assertEquals(newService.getAttribute("testAttribute"), "serviceAttr");

        serviceManager.removeService(service.getId());
        assertNull(serviceManager.getService(service.getId()));
    }

    @Test(groups = {"wso2.greg"}, description = "Attach Not existing LC to a service", priority = 11)
    public void testAttachNonExistingLCToService() throws Exception {
        ServiceManager serviceManager = new ServiceManager(governance);
        boolean status = false;
        Service service = serviceManager.newService(new QName("http://wso2.org/automation/test",
                "NonLCService"));
        service.addAttribute("testAttribute", "serviceAttr");
        serviceManager.addService(service);
        String serviceId = service.getId();
        try {
            service.attachLifecycle("ServiceLifeCycleNonExisting");
        } catch (GovernanceException ignored) {
            status = true;
            log.info("Cannot add invalid LC to service");
        }
        assertTrue(status, "LC not get added");
        assertNull(service.getLifecycleName());
        assertNull(service.getLifecycleState());

        Service newService = serviceManager.getService(serviceId);
        assertTrue(newService.getQName().toString().contains("NonLCService"));
        assertEquals(newService.getAttribute("testAttribute"), "serviceAttr");

        serviceManager.removeService(service.getId());
        assertNull(serviceManager.getService(service.getId()));
    }


//    @Test(groups = {"wso2.greg"}, description = "Attach Not existing LC to a service", priority = 12)
//    public void testCreateServiceVersions() throws Exception {
//        ServiceManager serviceManager = new ServiceManager(governance);
//        Service service = serviceManager.newService(new QName("http://wso2.org/automation/test",
//                                                              "ServiceVersions1"));
//        service.addAttribute("testAttribute", "serviceAttr");
//        serviceManager.addService(service);
//        String serviceId = service.getId();
//
//        int versionCountBefore;
//        if (governance.getVersions(service.getPath()) == null) {
//            versionCountBefore = 0;
//        } else {
//            versionCountBefore = governance.getVersions(service.getPath()).length;
//        }
//
//        //create service versions
//        service.createVersion();
//        service.createVersion();
//
//        int versionCountAfter = governance.getVersions(service.getPath()).length;
//        assertTrue((versionCountAfter - versionCountBefore) == 2);
//
//        //get last version and asset for service name
//        assertTrue(governance.getVersions(service.getPath())[versionCountAfter - 1].contains("ServiceVersions1;version"));
//
//        Service newService = serviceManager.getService(serviceId);
//        assertTrue(newService.getQName().toString().contains("ServiceVersions1"));
//        assertEquals(newService.getAttribute("testAttribute"), "serviceAttr");
//
//        serviceManager.removeService(service.getId());
//        assertNull(serviceManager.getService(service.getId()));
//    }


    @DataProvider(name = "invalidCharacter")
    public Object[][] invalidCharacter() {
        return new Object[][]{
                {"<a>"},
                {"#"},
                {"a|b"},
                {"@"},
                {"|"},
                {"^"},
                {"abc^"},
                {"\\"},
                {"{"},
                {"}"},
                {"%"},
                {"+"},
                {"="},
                {"}"},
                {"*"},
                {";"},
        };
    }


    @Test(groups = {"wso2.greg"}, description = "Add a wsdl to service with policy imports", priority = 13)
    public void testAttacheWsdlWithPolicyImports() throws Exception {

        ServiceManager serviceManager = new ServiceManager(governance);
        Service service = serviceManager.newService(new QName("http://wso2.org/automation/test",
                "WSDLWithPolicyTest"));
        service.addAttribute("interface_wsdlURL",
                "https://svn.wso2.org/repos/wso2/carbon/platform/trunk/" +
                        "platform-integration/system-test-framework/core/org.wso2.automation." +
                        "platform.core/src/main/resources/artifacts/GREG/wsdl/wsdl_with_SigEncr.wsdl");
        serviceManager.addService(service);

        service = serviceManager.getService(service.getId());
        assertTrue(service.getQName().toString().contains("WSDLWithPolicyTest"));

        Endpoint[] endpoints = service.getAttachedEndpoints();
        assertTrue(endpoints.length >= 4);

        for (Endpoint endpoint : endpoints) {
            assertTrue(endpoint.getUrl().contains
                    ("SimpleStockQuoteService1M"), "Endpoint not found");
        }

        Wsdl[] wsdls = service.getAttachedWsdls();
        assertTrue(wsdls[0].getAttribute("registry.WSDLImport").contains("true"));
        assertTrue(wsdls[0].getAttribute("WSDL Validation").contains("Valid"));
        assertTrue(wsdls[0].getAttribute("WSI Validation").contains("Invalid"));
        assertTrue(wsdls[0].getAttribute("WSI Validation Message 1").contains
                ("NullPointerException"));

        assertEquals(wsdls.length, 1);
        assertEquals(wsdls[0].getQName(), new QName("http://services.samples", "WSDLWithPolicyTest.wsdl"));

        Association[] associations = governance.getAssociations(wsdls[0].getPath(), "depends");
        boolean policyStatus = false;
        for (Association association : associations) {
            if (association.getDestinationPath().contains("policies/SgnEncrAnonymous.xml")) {
                policyStatus = true;
                break;
            }
        }

        assertTrue(policyStatus, "policy dependency not found");
        service.detachWSDL(wsdls[0].getId());
        assertTrue(service.getAttachedWsdls().length == 0, "WSDL still exits ");

        //remove the service
        serviceManager.removeService(service.getId());
        assertNull(serviceManager.getService(service.getId()));
    }

    @Test(groups = {"wso2.greg"}, description = "Add a wsdl to service with wsdl imports", priority = 14)
    public void testAttacheWsdlWithWsdlImports() throws Exception {

        ServiceManager serviceManager = new ServiceManager(governance);
        Service service = serviceManager.newService(new QName("http://wso2.org/automation/test",
                "WSDLImportWSDLTest"));
        service.addAttribute("interface_wsdlURL",
                "https://svn.wso2.org/repos/wso2/carbon/platform/trunk/platform-integration/" +
                        "system-test-framework/core/org.wso2.automation.platform.core/src/" +
                        "main/resources/artifacts/GREG/wsdl/" +
                        "Axis2Service_Wsdl_With_Wsdl_Imports.wsdl");

        serviceManager.addService(service);

        service = serviceManager.getService(service.getId());
        assertTrue(service.getQName().toString().contains("WSDLImportWSDLTest"));

        Wsdl[] wsdls = service.getAttachedWsdls();
        assertTrue(wsdls[0].getAttribute("WSDL Validation").contains("Validation is not supported for " +
                "WSDLs containing WSDL imports."));
        assertTrue(wsdls[0].getAttribute("WSI Validation").contains("Validation is not supported for " +
                "WSDLs containing WSDL imports."));
        assertEquals(wsdls.length, 1);
        assertTrue(wsdls[0].getQName().toString().contains("WSDLImportWSDLTest.wsdl"));

        Association[] associations = governance.getAssociations(wsdls[0].getPath(), "depends");
        boolean wsdlStatus = false;
        for (Association association : associations) {
            if (association.getDestinationPath().contains("carbon/service/Axis2ImportedWsdl.wsdl")) {
                wsdlStatus = true;
                break;
            }
        }
        assertTrue(wsdlStatus, "wsdl dependency not found");


        service.detachWSDL(wsdls[0].getId());
        assertTrue(service.getAttachedWsdls().length == 0, "WSDL still exits ");

        //remove the service
        serviceManager.removeService(service.getId());
        assertNull(serviceManager.getService(service.getId()));
    }

    @Test(groups = {"wso2.greg"}, description = "delete service ", priority = 15)
    public void testAddServiceWithAllCommunityFeatures() throws Exception {
        ServiceManager serviceManager = new ServiceManager(governance);
        String[] serviceIds = serviceManager.getAllServiceIds();

        String serviceName = "ServiceWithAllCommunityFeatures";
        if (serviceIds.length > 0) {
            for (String serviceId : serviceIds) {
                if (serviceManager.getService(serviceId).getQName().toString().contains(serviceName)) {
                    serviceManager.removeService(serviceId);
                }
            }
        }

        Service service = serviceManager.newService(new QName("http://wso2.org/automation/test",
                serviceName));
        serviceManager.addService(service);
        String servicePath = service.getPath();
        Service newService = serviceManager.getService(service.getId());

//        restartServer();
//
//        builder = new EnvironmentBuilder().greg(userId);
//        environment = builder.build();
//        wsRegistry = registryProviderUtil.getWSRegistry(userId,
//                ProductConstant.GREG_SERVER_NAME);
//        governance = registryProviderUtil.getGovernanceRegistry(wsRegistry, userId);
//

        Assert.assertNotNull(newService);

        governance.applyTag(servicePath, "TestTag");
        assertTrue(governance.getTags(servicePath)[0].getTagName().equals("TestTag"));

        Comment comment = new Comment();
        comment.setText("TestComment");
        governance.addComment(servicePath, comment);
        assertTrue(governance.getComments(servicePath)[0].getText().contains("TestComment"));

        governance.addAssociation(servicePath, servicePath, "depends");
        governance.addAssociation(servicePath, servicePath, "usedBy");

        assertTrue(governance.getAssociations(servicePath,
                "depends")[0].getDestinationPath().contains(servicePath));
        assertTrue(governance.getAssociations(servicePath,
                "usedBy")[0].getDestinationPath().contains(servicePath));

        governance.associateAspect(servicePath, "ServiceLifeCycle");


        governance.invokeAspect(servicePath, "ServiceLifeCycle", "Promote");

        List<String> aspects = governance.get(servicePath).getAspects();
        assertTrue(aspects.get(0).contains("ServiceLifeCycle"));

        ServiceManager serviceManagerGet = new ServiceManager(governance);
        assertTrue(serviceManagerGet.getService(service.getId()).getLifecycleState().equals("Development"));

        Resource resource = governance.get(servicePath);
        resource.addProperty("Key1", "Value1");
        governance.put(servicePath, resource);
        assertTrue(governance.get(servicePath).getPropertyValues("Key1").get(0).contains("Value1"));

        governance.rateResource(servicePath, 3);
        assertTrue(governance.getAverageRating(servicePath) == 3);

        PropertiesAdminServiceClient propertiesAdminServiceClient =
                new PropertiesAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                                 environment.getGreg().getSessionCookie());

        //REGISTRY-921
        propertiesAdminServiceClient.setRetentionProperties("/_system/governance" + servicePath,
                "write", "05/12/2012", "06/18/2012");

        RetentionBean retentionBean =
                propertiesAdminServiceClient.getRetentionProperties("/_system/governance" + servicePath);
        assertFalse(retentionBean.getDeleteLocked());
        assertTrue(retentionBean.getWriteLocked());
        assertTrue(retentionBean.getFromDate().equals("05/12/2012"));
        assertTrue(retentionBean.getToDate().equals("06/18/2012"));
        assertTrue(retentionBean.getUserName().contains(userInfo.getUserNameWithoutDomain()));
    }

    @AfterClass
    public void cleanArtifacts() throws RegistryException {
        for (String string : new String[]{"/trunk", "/branches", "/departments", "/organizations",
                "/project-groups", "/people", "/applications", "/processes", "/projects",
                "/test_suites", "/test_cases", "/test_harnesses", "/test_methods"}) {

            if (governance.resourceExists(string)) {
                governance.delete(string);
            }
        }

        governance = null;
        serverAdminClient = null;
        environment = null;
        builder = null;
        wsRegistry = null;
        registryProviderUtil = null;


    }

    private boolean verifyServiceAssociations(Association[] associations) {
        boolean status = false;
        for (Association association : associations) {
            if (association.getAssociationType().equals("depends") &&
                    association.getDestinationPath().contains("ep-")) {
                assertTrue(association.getAssociationType().equals("depends"));
                assertTrue(association.getSourcePath().contains("/test/org/bang/BizService"));
                assertTrue(association.getDestinationPath().contains("endpoints/localhost/axis2/services/ep-BizService"));
                status = true;

            } else if (association.getAssociationType().equals("depends") &&
                    association.getDestinationPath().contains(".wsdl")) {
                assertTrue(association.getAssociationType().equals("depends"));
                assertTrue(association.getSourcePath().contains("/test/org/bang/BizService"));
                assertTrue(association.getDestinationPath().contains("/wsdls/com/foo/BizService" +
                        ".wsdl"));
                status = true;


            } else if (association.getAssociationType().equals("usedBy")
                    && association.getSourcePath().contains("ep-")) {
                assertTrue(association.getAssociationType().equals("usedBy"));
                assertTrue(association.getSourcePath().contains("endpoints/localhost/axis2/services/ep-BizService"));
                assertTrue(association.getDestinationPath().contains("services/test/org/bang/BizService"));
                status = true;

            } else if (association.getAssociationType().equals("usedBy")
                    && association.getSourcePath().contains(".wsdl")) {
                assertTrue(association.getAssociationType().equals("usedBy"));
                assertTrue(association.getSourcePath().contains("/wsdls/com/foo/BizService" +
                        ".wsdl"));
                assertTrue(association.getDestinationPath().contains("services/test/org/bang/BizService"));
                status = true;

            } else if (!association.getAssociationType().equals("usedBy") ||
                    association.getAssociationType().equals("depends")) {
                fail("Required association types usedBy or depends not found");
                status = false;

            }
        }
        return status;
    }

    private void restartServer() throws Exception, RemoteException, InterruptedException {
        frameworkProperties =
                FrameworkFactory.getFrameworkProperties(ProductConstant.GREG_SERVER_NAME);
        ServerGroupManager.getServerUtils().restartGracefully(serverAdminClient, frameworkProperties);
    }
}
