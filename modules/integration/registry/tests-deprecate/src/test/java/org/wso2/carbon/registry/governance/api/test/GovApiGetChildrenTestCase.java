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
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import javax.xml.namespace.QName;
import java.net.MalformedURLException;
import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;

/**
 * test case for issue  String[] children = collection.getChildren();      via Governance Registry API
 * https://wso2.org/jira/browse/CARBON-12200
 */
public class GovApiGetChildrenTestCase {
    private static final Log log = LogFactory.getLog(GovApiGetChildrenTestCase.class);

    Registry governanceRegistry;
    WSRegistryServiceClient registryWS;
    ServiceManager serviceManager;
    Service service;
    WsdlManager wsdlMgr;
    Wsdl wsdl;

    @BeforeTest(alwaysRun = true)
    public void setEnvironment()
            throws LoginAuthenticationExceptionException, RemoteException, RegistryException,
                   MalformedURLException {
        governanceRegistry = TestUtils.getRegistry();
        TestUtils.cleanupResources(governanceRegistry);
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governanceRegistry);
        registryWS = TestUtils.getWSRegistry();
        serviceManager = new ServiceManager(governanceRegistry);


        wsdlMgr = new WsdlManager(governanceRegistry);
    }

    @Test(groups = {"wso2.greg", "wso2.greg.GovernanceServiceCreation"})
    public void deployArtifact() throws InterruptedException, RemoteException,
                                        MalformedURLException, GovernanceException {
        wsdl = wsdlMgr.newWsdl("http://svn.wso2.org/repos/wso2/carbon/platform/trunk/platform-integration/" +
                "platform-automated-test-suite/org.wso2.carbon.automation.test.repo/src/main/resources/" +
                "artifacts/GREG/wsdl/donotcall2_5.wsdl");
        wsdlMgr.addWsdl(wsdl);
        service = serviceManager.newService(new QName("http://my.service.ns1", "MyService"));

    }

    @Test(groups = {"wso2.greg", "wso2.greg.GovernanceServiceCreation"}, dependsOnMethods = "deployArtifact")
    public void testCheckChildList() throws Exception {
        serviceManager.addService(service);
        service.addAttribute("Application_Owner", "Financial Department");
        serviceManager.updateService(service);


        String service_namespace = "http://example.com/demo/services";
        String service_name = "ExampleService";
        String service_path = "/_system/governance/trunk/services/com/example/demo/services/ExampleService";

        Service service2 = serviceManager.newService(new QName(service_namespace, service_name));
        serviceManager.addService(service2);

        assertTrue(registryWS.resourceExists(service_path), "Service doesn't Exists");

        Collection collection = registryWS.get("/", 0,
                                               Integer.MAX_VALUE);
        String[] children = collection.getChildren();
        if (children.length == 0) {
            Assert.assertFalse(true, "child list is null");
        }
    }


    @AfterClass(alwaysRun = true, groups = {"wso2.greg"})
    public void removeArtifacts() throws GovernanceException {
        serviceManager.removeService(service.getId());
    }


}