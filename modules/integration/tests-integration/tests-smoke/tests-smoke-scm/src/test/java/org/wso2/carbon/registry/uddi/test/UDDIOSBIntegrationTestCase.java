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

package org.wso2.carbon.registry.uddi.test;

import org.apache.juddi.api_v3.AccessPointType;
import org.apache.juddi.api_v3.Publisher;
import org.apache.juddi.api_v3.SavePublisher;
import org.apache.juddi.v3.client.config.UDDIClient;
import org.apache.juddi.v3.client.transport.Transport;
import org.apache.juddi.v3_service.JUDDIApiPortType;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.uddi.api_v3.*;
import org.uddi.v3_service.UDDIInquiryPortType;
import org.uddi.v3_service.UDDIPublicationPortType;
import org.uddi.v3_service.UDDISecurityPortType;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.governance.api.endpoints.EndpointManager;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import java.io.File;
import java.rmi.RemoteException;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Class will test UDDI and OSB integration aspect
 */
public class UDDIOSBIntegrationTestCase extends GREGIntegrationBaseTest{

    String service_namespace = "http://example.com/demo/services";
    String service_name = "GovernanceAPIAutomatedTestService";
    private WSRegistryServiceClient wsRegistry;
    private RegistryProviderUtil registryProviderUtil;
    private Registry governance;
    private int userId = 1;
    public static ServiceManager serviceManager;
    public static EndpointManager endpointManager;
    private static WsdlManager wsdlManager;

    private static UDDISecurityPortType security = null;
    private static JUDDIApiPortType juddiApi = null;
    private static UDDIPublicationPortType publish = null;
    private static UDDIInquiryPortType inquiry = null;

    @BeforeClass(alwaysRun = true)
    public void initializeAPIObject() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);

        registryProviderUtil = new RegistryProviderUtil();
        wsRegistry = registryProviderUtil.getWSRegistry(automationContext);
        governance = registryProviderUtil.getGovernanceRegistry(wsRegistry, automationContext);
        serviceManager = new ServiceManager(governance);
        endpointManager = new EndpointManager(governance);
        initJuddi();
    }

    private void initJuddi() {
        try {
            String uddi_path = getTestArtifactLocation() + "artifacts" + File.separator +
                    "GREG" + File.separator + "xml" + File.separator + "simple-publish-uddi.xml";
            UDDIClient client = new UDDIClient(uddi_path);
            client.start();
            Transport transport = client.getTransport("default");
            security = transport.getUDDISecurityService();
            juddiApi = transport.getJUDDIApiService();
            publish = transport.getUDDIPublishService();
            inquiry = transport.getUDDIInquiryService();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Test(groups = {"wso2.greg.api"}, description = "Testing addService API method", enabled = false)
    public void testUDDIToGRegFlow() throws GovernanceException {
        try {
            GetAuthToken getAuthTokenRoot = new GetAuthToken();
            getAuthTokenRoot.setUserID("root");
            getAuthTokenRoot.setCred("root");
            AuthToken rootAuthToken = security.getAuthToken(getAuthTokenRoot);
            Publisher p = new Publisher();
            p.setAuthorizedName("wso2");
            p.setPublisherName("wso2");
            //
            SavePublisher sp = new SavePublisher();
            sp.getPublisher().add(p);
            sp.setAuthInfo(rootAuthToken.getAuthInfo());
            juddiApi.savePublisher(sp);
            GetAuthToken getAuthTokenMyPub = new GetAuthToken();
            getAuthTokenMyPub.setUserID("wso2");
            getAuthTokenMyPub.setCred("wso2carbon");
            AuthToken myPubAuthToken = security.getAuthToken(getAuthTokenMyPub);
            BusinessEntity myBusEntity = new BusinessEntity();
            Name myBusName = new Name();
            myBusName.setValue("My_Business");
            myBusEntity.getName().add(myBusName);
            SaveBusiness sb = new SaveBusiness();
            sb.getBusinessEntity().add(myBusEntity);
            sb.setAuthInfo(myPubAuthToken.getAuthInfo());
            BusinessDetail bd = publish.saveBusiness(sb);
            String myBusKey = bd.getBusinessEntity().get(0).getBusinessKey();
            BusinessService myService = new BusinessService();
            myService.setBusinessKey(myBusKey);
            Name myServName = new Name();
            myServName.setValue("My_Service");
            myService.getName().add(myServName);
            BindingTemplate myBindingTemplate = new BindingTemplate();
            AccessPoint accessPoint = new AccessPoint();
            accessPoint.setUseType(AccessPointType.WSDL_DEPLOYMENT.toString());
            accessPoint.setValue("https://raw.githubusercontent.com/wso2/wso2-qa-artifacts/master/automation-artifacts/greg/wsdl/" +
                    "BizService.wsdl");
            myBindingTemplate.setAccessPoint(accessPoint);
            BindingTemplates myBindingTemplates = new BindingTemplates();
            myBindingTemplates.getBindingTemplate().add(myBindingTemplate);
            myService.setBindingTemplates(myBindingTemplates);
            SaveService ss = new SaveService();
            ss.getBusinessService().add(myService);
            ss.setAuthInfo(myPubAuthToken.getAuthInfo());
            ServiceDetail sd = publish.saveService(ss);
            String myServKey = sd.getBusinessService().get(0).getServiceKey();
            assertNotNull(myServKey, "Service key cannot be null");
            assertTrue(governance.resourceExists("/trunk/wsdls/com/foo/BizService.wsdl"));
            //Clear WSDLs/Services created
            wsRegistry.delete("/_system/governance/trunk/wsdls/com/foo/BizService.wsdl");
            wsRegistry.delete("/_system/governance/trunk/services/com/foo/BizService");
        } catch(Exception e) {
            throw new GovernanceException("Failed to add the business service to UDDI." + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg.api"}, description = "Testing getAllServicePath API method",
            dependsOnMethods = "testUDDIToGRegFlow", enabled = false)
    public void testGRegToUDDIFlow() throws GovernanceException {
        wsdlManager = new WsdlManager(governance);
        Wsdl wsdl = wsdlManager.newWsdl("https://raw.githubusercontent.com/wso2/wso2-qa-artifacts/master/automation-artifacts/greg/wsdl/BizService.wsdl");

        wsdl.addAttribute("creator", "it is me");
        wsdl.addAttribute("version", "0.01");
        wsdlManager.addWsdl(wsdl);
        try {
            GetAuthToken getAuthTokenRoot = new GetAuthToken();
            getAuthTokenRoot.setUserID("root");
            getAuthTokenRoot.setCred("root");
            AuthToken rootAuthToken = null;
            rootAuthToken = security.getAuthToken(getAuthTokenRoot);
            FindService fs = new FindService();
            Name fname = new Name();
            fname.setValue("%");
            fs.setAuthInfo(rootAuthToken.getAuthInfo());
            fs.getName().add(fname);
            org.uddi.api_v3.FindQualifiers qualifiers = new org.uddi.api_v3.FindQualifiers();
            qualifiers.getFindQualifier().add("approximateMatch");
            fs.setFindQualifiers(qualifiers);
            ServiceList list = inquiry.findService(fs);
            boolean serviceFound = false;
            for(ServiceInfo serviceInfo : list.getServiceInfos().getServiceInfo()) {
                if("BizService".equalsIgnoreCase(serviceInfo.getName().get(0).getValue())) {
                    serviceFound = true;
                    break;
                }
            }
            assertTrue(serviceFound, "Business service not found");
            wsRegistry.delete("/_system/governance/trunk/wsdls/com/foo/BizService.wsdl");
            wsRegistry.delete("/_system/governance/trunk/services/com/foo/BizService");
        } catch(RemoteException e) {
            throw new GovernanceException("Error occurred while executing inquiry service in JUDDI." + e.getMessage());
        } catch(RegistryException e) {
            throw new GovernanceException("Error occurred while cleaning up the WSDL" + e.getMessage());
        }
    }

    @AfterClass
    public void cleanArtifacts() throws RegistryException {
        for(String string : new String[]{"/trunk", "/branches", "/departments", "/organizations",
                "/project-groups", "/people", "/applications", "/processes", "/projects",
                "/test_suites", "/test_cases", "/test_harnesses", "/test_methods"}) {
            if(governance.resourceExists(string)) {
                governance.delete(string);
            }
        }
        governance = null;
        wsRegistry = null;
        registryProviderUtil = null;
        serviceManager = null;
        endpointManager = null;
    }

    private void cleanService() throws GovernanceException {
        Service[] service = serviceManager.getAllServices();
        for(int i = 0; i <= service.length - 1; i++) {
            if(service[i].getQName().getLocalPart().contains(service_name)) {
                serviceManager.removeService(service[i].getId());
            }
        }
    }
}
