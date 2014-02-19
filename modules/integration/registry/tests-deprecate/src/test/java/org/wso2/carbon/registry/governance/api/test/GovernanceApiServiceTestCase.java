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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import javax.xml.namespace.QName;
import java.net.MalformedURLException;
import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;

/**
 * covers https://wso2.org/jira/browse/CARBON-11573
 * https://wso2.org/jira/browse/CARBON-11256
 */

public class GovernanceApiServiceTestCase {
    private static final Log log = LogFactory.getLog(GovernanceApiServiceTestCase.class);
    private WSRegistryServiceClient registryWS;
    private ServiceManager serviceManager;
    private Service service;
    private WsdlManager wsdlMgr;
    private Wsdl wsdl;


    @BeforeClass(alwaysRun = true, groups = {"wso2.greg", "wso2.greg.GovernanceServiceCreation"})
    public void deployArtifact() throws InterruptedException, RemoteException,
                                        MalformedURLException, RegistryException {
        Registry governanceRegistry = TestUtils.getRegistry();
        registryWS = TestUtils.getWSRegistry();
        TestUtils.cleanupResources(governanceRegistry);
        serviceManager = new ServiceManager(governanceRegistry);
        wsdlMgr = new WsdlManager(governanceRegistry);
        wsdl = wsdlMgr.newWsdl("https://svn.wso2.org/repos/wso2/carbon/platform/trunk/products/greg/modules/" +
                "integration/registry/tests/src/test/java/resources/wsdl/echo.wsdl");
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governanceRegistry);
    }

    @Test(groups = {"wso2.greg", "wso2.greg.GovernanceServiceCreate"})
    public void testAddService() throws Exception {

        String service_namespace = "http://example.com/demo/services";
        String service_name = "ExampleService";
        String service_path = "/_system/governance/trunk/services/com/example/demo/services/ExampleService";

         service = serviceManager.newService(new QName(service_namespace, service_name));
         serviceManager.addService(service);

        assertTrue(registryWS.resourceExists(service_path), "Service doesn't Exists");
    }

    @Test(groups = {"wso2.greg", "wso2.greg.GovernanceServiceCreate"}, dependsOnMethods = "testAddService")
    public void testUpdateService() throws Exception {

        Service[] services = serviceManager.getAllServices();
        Service updateService = null;
        for (Service service : services) {
            String name = service.getQName().getLocalPart();
            if (name.equals("ExampleService")) {
                updateService = service;
            }
        }
        if (updateService != null) {
            updateService.setAttribute("overview_version", "3.3.3");
        }
        serviceManager.updateService(updateService);

        Service[] services2 = serviceManager.getAllServices();
        Service updateService2 = null;
        for (Service service : services2) {
            String name = service.getQName().getLocalPart();
            if (name.equals("ExampleService")) {
                updateService2 = service;
            }
        }
        String updatedVersion = null;
        if (updateService2 != null) {
            updatedVersion = updateService2.getAttribute("overview_version");
        }
        assertTrue(updatedVersion != null && updatedVersion.equals("3.3.3"), "Service Exists");
    }

    @Test(groups = {"wso2.greg", "wso2.greg.GovernanceServiceCreation"}, dependsOnMethods = "testUpdateService")
    public void testAddWsdlToService() throws Exception {
        String wsdlPath = "/trunk/wsdls/org/wso2/carbon/core/services/echo/echo.wsdl";

        wsdlMgr.addWsdl(wsdl);
        service.attachWSDL(wsdlMgr.getWsdl(wsdl.getId()));
        serviceManager.updateService(service);
        Wsdl[] wsdls = service.getAttachedWsdls();
        boolean resourceExist = false;
        for (Wsdl wsdl1 : wsdls) {
            if (wsdl1.getPath() != null && wsdl1.getPath().equals(wsdlPath)) {
                resourceExist = true;
            }
        }
        Assert.assertTrue(resourceExist, "Wsdl is not listed in Registry");
    }

    @Test(groups = {"wso2.greg", "wso2.greg.GovernanceServiceCreation"}, dependsOnMethods = "testAddWsdlToService")
    public void testRemoveWsdlFromService() throws Exception {
        service.detachWSDL(wsdl.getId());
        serviceManager.updateService(service);
        boolean wsdlfound = true;
        for (Service gregService : serviceManager.getAllServices()) {
            if (gregService.getId().equals(service.getId())) {
                for (Wsdl serviceWsdl : gregService.getAttachedWsdls()) {
                    if (serviceWsdl.getId().equals(wsdl.getId())) {
                        log.info("Wsdl is attached to the service");
                        wsdlfound = false;
                    }
                }
            }
        }
        Assert.assertFalse(wsdlfound, "Wsdl is not listed in Registry");
    }

    @AfterClass(alwaysRun = true, groups = {"wso2.greg"})
    public void removeArtifacts() throws GovernanceException {
        serviceManager.removeService(service.getId());
    }


}