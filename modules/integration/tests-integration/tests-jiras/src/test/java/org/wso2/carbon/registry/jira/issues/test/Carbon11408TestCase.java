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

package org.wso2.carbon.registry.jira.issues.test;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.integration.common.utils.FileManager;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.lifecycle.test.utils.LifeCycleUtils;
import org.wso2.carbon.registry.relations.stub.AddAssociationRegistryExceptionException;
import org.wso2.carbon.registry.relations.stub.beans.xsd.AssociationTreeBean;
import org.wso2.carbon.registry.relations.stub.beans.xsd.DependenciesBean;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.clients.RelationAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import java.io.File;
import java.rmi.RemoteException;

import static junit.framework.Assert.assertTrue;

public class Carbon11408TestCase extends GREGIntegrationBaseTest {

    private Registry governance;
    private RelationAdminServiceClient relationAdminServiceClient;
    private WsdlManager wsdlManager;
    private String servicePath;
    private String wsdlPath;
    private String serviceName = "DependencyTestService";
    private String serviceName2 = "echoyuSer1";


    @BeforeClass(groups = {"wso2.greg"}, description = "Add a Service and Wsdl")
    public void init() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String session = getSessionCookie();

        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        WSRegistryServiceClient registry = registryProviderUtil.getWSRegistry(automationContext);
        governance = registryProviderUtil.getGovernanceRegistry(registry, automationContext);
        relationAdminServiceClient =
                new RelationAdminServiceClient(backendURL, session);

        wsdlManager = new WsdlManager(governance);
        Wsdl[] wsdls = wsdlManager.getAllWsdls();
        for (Wsdl wsdl : wsdls) {
            if (wsdl.getQName().getLocalPart().equals("echo.wsdl")) {
                wsdlManager.removeWsdl(wsdl.getId());
            }
        }

        ServiceManager serviceManager = new ServiceManager(governance);
        Service[] services = serviceManager.getAllServices();
        for (Service s : services) {
            if (s.getQName().getLocalPart().equals(serviceName)) {
                serviceManager.removeService(s.getId());
            }
        }

        servicePath = "/_system/governance" + LifeCycleUtils.addService("ns", serviceName, governance);
        wsdlManager = new WsdlManager(governance);

        String wsdlFilePath = getTestArtifactLocation() + "artifacts" + File.separator +
                              "GREG" + File.separator + "wsdl" + File.separator + "echo.wsdl";
        Wsdl wsdl1 = wsdlManager.newWsdl(FileManager.readFile(wsdlFilePath).getBytes(), "echo.wsdl");
        wsdlManager.addWsdl(wsdl1);
        wsdlPath = "/_system/governance" + wsdl1.getPath();
    }

    @Test(groups = {"wso2.greg"}, description = "Add a Wsdl dependency to the service and test")
    public void TestVersionDependency()
            throws ResourceAdminServiceExceptionException, RemoteException,
                   AddAssociationRegistryExceptionException {
        relationAdminServiceClient.addAssociation(servicePath, "depends", wsdlPath, "add");
        DependenciesBean dependenciesBean = relationAdminServiceClient.getDependencies(servicePath);
        AssociationTreeBean associationTreeBean = relationAdminServiceClient.getAssociationTree(servicePath, "depends");

        String resourcePath = associationTreeBean.getAssociationTree();
        assertTrue(resourcePath.contains(wsdlPath));
    }

    @AfterClass
    public void RemoveWSDL() throws RegistryException {
        wsdlManager = new WsdlManager(governance);
        Wsdl[] wsdls = wsdlManager.getAllWsdls();
        for (Wsdl wsdl : wsdls) {
            if (wsdl.getQName().getLocalPart().equals("echo.wsdl")) {
                wsdlManager.removeWsdl(wsdl.getId());
            }
        }

        ServiceManager serviceManager = new ServiceManager(governance);
        Service[] services = serviceManager.getAllServices();
        for (Service s : services) {
            if (s.getQName().getLocalPart().equals(serviceName) ||
                s.getQName().getLocalPart().equals(serviceName2)) {
                serviceManager.removeService(s.getId());
            }
        }
    }
}