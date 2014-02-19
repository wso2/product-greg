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

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import javax.xml.namespace.QName;
import java.rmi.RemoteException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

public class ServiceVersioningTestCase {

    int userId = 2;
    private final static String WSDL_URL_1 =
            "https://svn.wso2.org/repos/wso2/carbon/platform/trunk/products/greg/modules/integration/registry/tests-new/" +
                    "src/test/resources/artifacts/GREG/wsdl/info.wsdl";
    private final static String WSDL_URL_2 =
            "https://svn.wso2.org/repos/wso2/carbon/platform/trunk/products/greg/modules/integration/registry/tests-new" +
                    "/src/test/resources/artifacts/GREG/wsdl/ops.wsdl";
    ServiceManager serviceManager;
    Service versionService;


    @BeforeClass(alwaysRun = true)
    public void initialize() throws RemoteException, LoginAuthenticationExceptionException,
                                    RegistryException {
        WSRegistryServiceClient wsRegistry =
                new RegistryProviderUtil().getWSRegistry(userId,
                                                         ProductConstant.GREG_SERVER_NAME);
        Registry governance = new RegistryProviderUtil().getGovernanceRegistry(wsRegistry, userId);
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
        serviceManager = new ServiceManager(governance);


    }

    /**
     * add a service with a version number,
     * edit service content and save it with different versioon number
     * edit again and save with the previous version number
     *
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException
     *
     */
    @Test(groups = {"wso2.greg"}, description = "test Versioning")
    public void testVersioning() throws GovernanceException {

        Service addedService=null;

        //add a service with version number 1.0.0
        versionService =
                serviceManager.newService(new QName(
                        "http://service.for.searching1/mnm/",
                        "versionService"));
        versionService.addAttribute("overview_version", "1.0.0");
        versionService.addAttribute("overview_description", "Test");
        versionService.addAttribute("interface_wsdlUrl", WSDL_URL_1);
        versionService.addAttribute("interface_messageFormats", "SOAP 1.2");
        versionService.addAttribute("interface_messageExchangePatterns", "Request Response");
        versionService.addAttribute("security_authenticationPlatform", "XTS-WS TRUST");
        versionService.addAttribute("security_authenticationMechanism", "InfoCard");
        versionService.addAttribute("security_messageIntegrity", "WS-Security");
        versionService.addAttribute("security_messageEncryption", "WS-Security");
        serviceManager.addService(versionService);
         Service[] service=serviceManager.getAllServices();
        for (Service aService1 : service) {
            if (aService1.getAttribute("overview_name").equals("versionService")) {
                addedService = aService1;
            }
        }
        assertEquals("Test", addedService.getAttribute("overview_description"));
        assertEquals("1.0.0", addedService.getAttribute("overview_version"));
        assertEquals("SOAP 1.2", addedService.getAttribute("interface_messageFormats"));
        assertEquals(WSDL_URL_1, addedService.getAttribute("interface_wsdlUrl"));
        assertEquals("XTS-WS TRUST", addedService.getAttribute("security_authenticationPlatform"));
        assertEquals("InfoCard", addedService.getAttribute("security_authenticationMechanism"));
        assertEquals("WS-Security", addedService.getAttribute("security_messageIntegrity"));
        assertEquals("WS-Security", addedService.getAttribute("security_messageEncryption"));
        assertEquals("Request Response", addedService.getAttribute("interface_messageExchangePatterns"));


        //Edit the service content and save it with different version
        versionService.setAttribute("overview_description", "Edited Test");
        versionService.setAttribute("interface_wsdlUrl", WSDL_URL_2);
        versionService.setAttribute("interface_messageFormats", "JSON");
        versionService.setAttribute("overview_version", "1.0.1");
        versionService.setAttribute("security_authenticationPlatform", "TAM_WEBSEAL");

        versionService.setAttribute("security_authenticationMechanism", "OpenID");

        serviceManager.updateService(versionService);

        service=serviceManager.getAllServices();
        for (Service aService : service) {
            if (aService.getAttribute("overview_name").equals("versionService")) {
                addedService = aService;
            }
        }
        assertEquals("1.0.1", addedService.getAttribute("overview_version"));
        assertEquals("Edited Test", addedService.getAttribute("overview_description"));
        assertEquals("JSON", addedService.getAttribute("interface_messageFormats"));
        assertEquals(WSDL_URL_2, addedService.getAttribute("interface_wsdlUrl"));
        assertEquals("Request Response", addedService.getAttribute("interface_messageExchangePatterns"));
        assertEquals("OpenID", addedService.getAttribute("security_authenticationMechanism"));
        assertEquals("WS-Security", addedService.getAttribute("security_messageIntegrity"));
        assertEquals("WS-Security", addedService.getAttribute("security_messageEncryption"));
        assertEquals("TAM_WEBSEAL", addedService.getAttribute("security_authenticationPlatform"));

        versionService.removeAttribute("overview_description");
        versionService.setAttribute("overview_version", "1.0.0");
        versionService.removeAttribute("interface_wsdlUrl");
        versionService.removeAttribute("interface_messageFormats");
        versionService.setAttribute("interface_messageExchangePatterns", "One Way");
        versionService.setAttribute("security_authenticationPlatform", "WSO2 Identity Server");
        versionService.removeAttribute("security_authenticationMechanism");
        versionService.removeAttribute("security_messageIntegrity");
        versionService.removeAttribute("security_messageEncryption");

        serviceManager.updateService(versionService);
         service=serviceManager.getAllServices();
        for (Service aService : service) {
            if (aService.getAttribute("overview_name").equals("versionService")) {
                addedService = aService;
            }
        }
        assertEquals("1.0.0", addedService.getAttribute("overview_version"));
        assertNull(addedService.getAttribute("overview_description"));
        assertNull(addedService.getAttribute("interface_messageFormats"));
        assertNull(addedService.getAttribute("interface_wsdlUrl"));
        assertEquals("One Way", addedService.getAttribute("interface_messageExchangePatterns"));
        assertNull(addedService.getAttribute("security_authenticationMechanism"));
        assertNull(addedService.getAttribute("security_messageIntegrity"));
        assertNull(addedService.getAttribute("security_messageEncryption"));
        assertEquals("WSO2 Identity Server", addedService.getAttribute("security_authenticationPlatform"));

    }

    @AfterClass(alwaysRun = true)
    public void cleanup() throws GovernanceException {
        serviceManager.removeService(versionService.getId());
        serviceManager = null;
        versionService = null;
    }
}
