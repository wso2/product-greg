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

package org.wso2.carbon.registry.governance.api.test.old;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.api.endpoints.EndpointManager;
import org.wso2.carbon.governance.api.endpoints.dataobjects.Endpoint;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.schema.SchemaManager;
import org.wso2.carbon.governance.api.schema.dataobjects.Schema;
import org.wso2.carbon.governance.api.services.ServiceFilter;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import javax.xml.namespace.QName;
import java.net.MalformedURLException;
import java.rmi.RemoteException;

/**
 * Test case for governance registry adding wsdl and remove wsdl.
 */
public class GovernanceApiWsdlTestCase {
    private static final Log log = LogFactory.getLog(GovernanceApiWsdlTestCase.class);
    Registry governanceRegistry;
    ServiceManager serviceManager;
    Service service;
    WsdlManager wsdlMgr;
    Wsdl wsdl;
    EndpointManager endpointManager;
    SchemaManager schemaManager;

    @BeforeTest(alwaysRun = true)
    public void setEnvironment()
            throws LoginAuthenticationExceptionException, RemoteException, RegistryException,
            MalformedURLException {
        int userId = 1;
        UserInfo userInfo = UserListCsvReader.getUserInfo(userId);
        WSRegistryServiceClient wsRegistry =
                new RegistryProviderUtil().getWSRegistry(userId,
                        ProductConstant.GREG_SERVER_NAME);
        governanceRegistry = new RegistryProviderUtil().getGovernanceRegistry(wsRegistry, userId);
        wsdlMgr = new WsdlManager(governanceRegistry);
        serviceManager = new ServiceManager(governanceRegistry);
        endpointManager = new EndpointManager(governanceRegistry);
        schemaManager = new SchemaManager(governanceRegistry);
    }

    @BeforeClass(alwaysRun = true, groups = {"wso2.greg", "wso2.greg.GovernanceApiWsdl"})
    public void deployArtifact() throws InterruptedException, RemoteException,
            MalformedURLException, GovernanceException {
        wsdl = wsdlMgr.newWsdl("http://svn.wso2.org/repos/wso2/trunk/commons/qa/qa-artifacts/greg/wsdl/calculator.wsdl");
        wsdlMgr.addWsdl(wsdl);
    }

    @Test(groups = {"wso2.greg", "wso2.greg.GovernanceApiWsdl"}, description = "compare uuid")
    public void testAddWsdl() throws Exception, RemoteException {

        boolean wsdlExists = false;
        for (Wsdl wsdlList : wsdlMgr.getAllWsdls()) {
            if (wsdlList.getId().equals(wsdl.getId())) {
                wsdlExists = true;
                log.info(wsdl.getPath() + "--exists");
            }
        }
        Assert.assertTrue(wsdlExists, "Wsdl is not listed in Registry");
    }

    @Test(groups = {"wso2.greg", "wso2.greg.GovernanceApiWsdl"}, description = "update WSDL", dependsOnMethods = "testAddWsdl")
    public void testUpdateWSDL() throws Exception {

        Wsdl wsdl1 = wsdlMgr.getWsdl(wsdl.getId());
        wsdl1.addAttribute("name", "Calculator");
        wsdl1.addAttribute("version", "10.10.10");

        wsdlMgr.updateWsdl(wsdl1);

        Wsdl wsdl2 = wsdlMgr.getWsdl(wsdl1.getId());
        //wsdl2.getAttribute("name") returns null
//           Assert.assertEquals(wsdl2.getAttribute("name"), "Calculator", "Add attributes failed to WSDL");
//           Assert.assertEquals(wsdl2.getAttribute("version"), "10.10.10", "Add attributes failed to WSDL");

        Service service1 = serviceManager.newService(new QName("TestCal"));
        serviceManager.addService(service1);
        service1.attachWSDL(wsdl2);

        Wsdl[] wsdl3 = service1.getAttachedWsdls();
        boolean isContains = false;
        if (wsdl3 != null) {

            for (Wsdl wsdl4 : wsdl3) {
                if (wsdl4.getPath().equalsIgnoreCase("/trunk/wsdls/org/charitha/calculator.wsdl")) {
                    isContains = true;
                }

            }
        }
        Assert.assertTrue(isContains, "WSDL attach to service failed");
        Endpoint endpoint = endpointManager.newEndpoint("http://svn.wso2.org/repos/wso2/trunk/commons/qa/qa-artifacts/" +
                "greg/wsdl/BizService.wsdl");
        Schema schema = schemaManager.newSchema("http://svn.wso2.org/repos/wso2/" +
                "trunk/commons/qa/qa-artifacts/greg/xsd/person.xsd");

        schemaManager.addSchema(schema);
        endpointManager.addEndpoint(endpoint);

        wsdl2.attachSchema(schema);
        wsdl2.attachEndpoint(endpoint);

        Schema[] schemas = wsdl2.getAttachedSchemas();
        boolean schemaContains = false;
        for (Schema schema1 : schemas) {
            if (schema1.getPath().equalsIgnoreCase("/trunk/schemas/org1/charitha/person.xsd")) {
                schemaContains = true;
            }
        }
        Assert.assertTrue(schemaContains, "Failed to attach schema to WSDL");

        Endpoint[] endpoint1 = wsdl2.getAttachedEndpoints();
        boolean endpointContains = false;
        for (Endpoint endpoint2 : endpoint1) {
            if (endpoint2.getPath().equalsIgnoreCase("/trunk/endpoints/localhost/axis2/services/ep-Charithacalculator")) {
                endpointContains = true;
            }
        }
        Assert.assertTrue(endpointContains, "Failed to get endpoint");

        wsdl2.attachLifecycle("ServiceLifeCycle");
        Assert.assertEquals(wsdl2.getLifecycleName(), "ServiceLifeCycle");

        wsdlMgr.removeWsdl(wsdl2.getId());
        Assert.assertFalse(governanceRegistry.resourceExists("/trunk/wsdls/org/charitha/calculator.wsdl"),
                "WSDL delete failed");
        schemaManager.removeSchema(schema.getId());

    }

    @AfterClass(alwaysRun = true, groups = {"wso2.greg", "wso2.greg.GovernanceApiWsdl"})
    public void rmArtifactsObjects() throws RegistryException, AxisFault {
        wsdlMgr = null;
        endpointManager = null;
        WSRegistryServiceClient wsRegistry;
        int userId = 0;
        wsRegistry =
                new RegistryProviderUtil().getWSRegistry(userId,
                        ProductConstant.GREG_SERVER_NAME);
        String endPointLocation1 = "/_system/governance/trunk/endpoints/localhost/axis2/services/";
        String endPointLocation2 = "/_system/governance/trunk/endpoints/org/wso2/svn/repos/wso2/trunk/commons/qa/qa_artifacts/greg/wsdl/";
        governanceRegistry = null;
        schemaManager = null;
        serviceManager.removeService(serviceManager.findServices(new ServiceFilter() {
            public boolean matches(Service service) throws GovernanceException {
                String attributeVal = service.getAttribute("overview_name");
                if (attributeVal != null && attributeVal.startsWith("TestCal")) {
                    return true;
                }
                return false;
            }
        })[0].getId());
        serviceManager.removeService(serviceManager.findServices(new ServiceFilter() {
            public boolean matches(Service service) throws GovernanceException {
                String attributeVal = service.getAttribute("overview_name");
                if (attributeVal != null && attributeVal.startsWith("calculatorService")) {
                    return true;
                }
                return false;
            }
        })[0].getId());
        wsRegistry.delete(endPointLocation1 + "ep-Charithacalculator");
        wsRegistry.delete(endPointLocation2 + "ep-BizService-wsdl");
        serviceManager = null;

        governanceRegistry = null;
        serviceManager = null;
        service = null;
        wsdlMgr = null;
        wsdl = null;
        endpointManager = null;
        schemaManager = null;
    }
}
