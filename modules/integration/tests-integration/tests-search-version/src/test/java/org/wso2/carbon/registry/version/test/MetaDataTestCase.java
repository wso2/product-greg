/*
 * Copyright 2004,2005 The Apache Software Foundation.
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


package org.wso2.carbon.registry.version.test;


import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.governance.list.stub.beans.xsd.PolicyBean;
import org.wso2.carbon.governance.list.stub.beans.xsd.SchemaBean;
import org.wso2.carbon.governance.list.stub.beans.xsd.WSDLBean;
import org.wso2.carbon.governance.services.stub.AddServicesServiceRegistryExceptionException;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.beans.xsd.VersionPath;
import org.wso2.carbon.registry.resource.stub.common.xsd.ResourceData;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.clients.GovernanceServiceClient;
import org.wso2.greg.integration.common.clients.ListMetaDataServiceClient;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import javax.activation.DataHandler;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import static junit.framework.Assert.*;
import static org.testng.Assert.assertNotNull;


public class MetaDataTestCase extends GREGIntegrationBaseTest {

    private ResourceAdminServiceClient resourceAdminClient;
    private GovernanceServiceClient governanceServiceClient;
    private ListMetaDataServiceClient listMetaDataServiceClient;
    private WSRegistryServiceClient wsRegistryServiceClient;
    private String sessionCookie;
    private String backEndUrl;
    private String userName;
    private String userNameWithoutDomain;

    @BeforeClass(alwaysRun = true)
    public void initializeTests()
            throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        backEndUrl = getBackendURL();
        sessionCookie = getSessionCookie();
        userName = automationContext.getContextTenant().getContextUser().getUserName();

        if (userName.contains("@"))
            userNameWithoutDomain = userName.substring(0, userName.indexOf('@'));
        else
            userNameWithoutDomain = userName;

        resourceAdminClient =
                new ResourceAdminServiceClient(backEndUrl,
                                               sessionCookie);
        governanceServiceClient =
                new GovernanceServiceClient(backEndUrl,
                                            sessionCookie);
        listMetaDataServiceClient =
                new ListMetaDataServiceClient(backEndUrl,
                                              sessionCookie);

        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        wsRegistryServiceClient =
                registryProviderUtil.getWSRegistry(automationContext);
    }


    @Test(groups = {"wso2.greg"}, description = "add a policy at leaf level and version it")
    public void testVersionPolicy()
            throws MalformedURLException, ResourceAdminServiceExceptionException, RemoteException {
        Boolean nameExists = false;
        String path = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator
                      + "GREG" + File.separator + "policy" + File.separator + "policy.xml";
        DataHandler dataHandler = new DataHandler(new URL("file:///" + path));
        resourceAdminClient.addPolicy("desc 1", dataHandler);
        PolicyBean policyBean = listMetaDataServiceClient.listPolicies();
        String[] names = policyBean.getName();

        for (String name : names) {
            if (name.equalsIgnoreCase("policy.xml")) {
                nameExists = true;
            }
        }

        assertTrue(nameExists);
        deleteVersion("/_system/governance/trunk/policies/1.0.0/policy.xml");
        resourceAdminClient.createVersion("/_system/governance/trunk/policies/1.0.0/policy.xml");
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths("/_system/governance/trunk/policies/1.0.0/policy.xml");
        assertEquals(1, vp1.length);
        assertNull(deleteVersion("/_system/governance/trunk/policies/1.0.0/policy.xml"));


    }


    @Test(groups = {"wso2.greg"}, description = "add a wsdl at leaf level and version it")
    public void testVersionWSDL()
            throws ResourceAdminServiceExceptionException, RemoteException, MalformedURLException {
        Boolean nameExists = false;
        String path = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator
                      + "GREG" + File.separator + "wsdl" + File.separator + "echo.wsdl";
        DataHandler dataHandler = new DataHandler(new URL("file:///" + path));
        resourceAdminClient.addWSDL("desc 1", dataHandler);

        WSDLBean wsdlBean = listMetaDataServiceClient.listWSDLs();
        String[] names = wsdlBean.getName();

        for (String name : names) {
            if (name.equalsIgnoreCase("echo.wsdl")) {
                nameExists = true;
            }
        }
        assertTrue(nameExists);
        deleteVersion("/_system/governance/trunk/wsdls/org/wso2/carbon/core/services/echo/1.0.0/echo.wsdl");
        resourceAdminClient.createVersion("/_system/governance/trunk/wsdls/org/wso2/carbon/core/services" +
                                          "/echo/1.0.0/echo.wsdl");
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths("/_system/governance/trunk/wsdls/org/wso2/carbon" +
                                                                "/core/services/echo/1.0.0/echo.wsdl");
        assertEquals(1, vp1.length);
        assertNull(deleteVersion("/_system/governance/trunk/wsdls/org/wso2/carbon/core/services/echo/1.0.0/echo.wsdl"));

    }


    @Test(groups = {"wso2.greg"}, description = "add a schema at leaf level and version it")
    public void testVerSchema()
            throws MalformedURLException, ResourceAdminServiceExceptionException, RemoteException {
        Boolean nameExists = false;
        String path = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator
                      + "GREG" + File.separator + "schema" + File.separator + "calculator.xsd";
        DataHandler dataHandler = new DataHandler(new URL("file:///" + path));
        resourceAdminClient.addSchema("desc 1", dataHandler);
        SchemaBean schemaBean = listMetaDataServiceClient.listSchemas();

        String[] names = schemaBean.getName();

        for (String name : names) {
            if (name.equalsIgnoreCase("calculator.xsd")) {
                nameExists = true;
            }
        }
        assertTrue(nameExists);
        deleteVersion("/_system/governance/trunk/schemas/org/charitha/1.0.0/calculator.xsd");
        resourceAdminClient.createVersion("/_system/governance/trunk/schemas/org/charitha/1.0.0/calculator.xsd");
        VersionPath[] vp1 =
                resourceAdminClient.getVersionPaths("/_system/governance/trunk/schemas/org/charitha/1.0.0/calculator.xsd");
        assertEquals(1, vp1.length);
        assertNull(deleteVersion("/_system/governance/trunk/schemas/org/charitha/1.0.0/calculator.xsd"));
    }


    @Test(groups = {"wso2.greg"}, description = "add a service and create a version of it")
    public void testAddService()
            throws XMLStreamException, IOException, AddServicesServiceRegistryExceptionException,
                   ResourceAdminServiceExceptionException {

        Boolean nameExists = false;
        String serviceContent;
        String path = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator
                      + "GREG" + File.separator + "services" + File.separator + "service.metadata.xml";
        DataHandler dataHandler = new DataHandler(new URL("file:///" + path));
        String mediaType = "application/vnd.wso2-service+xml";
        String description = "This is a test service";
        resourceAdminClient.addResource(
                "/_system/governance/service", mediaType, description, dataHandler);

        ResourceData[] data =  resourceAdminClient.getResource("/_system/governance/trunk/services/com/abb/1.0.0-SNAPSHOT/abc");
        
        assertNotNull(data, "Service not found");
        deleteVersion("/_system/governance/trunk/wsdls/com/foo/1.0.0-SNAPSHOT/abc.wsdl");
        resourceAdminClient.createVersion("/_system/governance/trunk/wsdls/com/foo/1.0.0-SNAPSHOT/abc.wsdl");
        VersionPath[] vp1 = resourceAdminClient.getVersionPaths("/_system/governance/trunk/wsdls/com/foo/1.0.0-SNAPSHOT/abc.wsdl");

        assertEquals(1, vp1.length);
        assertNull(deleteVersion("/_system/governance/trunk/wsdls/com/foo/1.0.0-SNAPSHOT/abc.wsdl"));
    }


    public VersionPath[] deleteVersion(String path)
            throws ResourceAdminServiceExceptionException, RemoteException {
        VersionPath[] vp2 = null;
        if (resourceAdminClient.getVersionPaths(path) == null) {
            return vp2;
        } else {
            int length = resourceAdminClient.getVersionPaths(path).length;
            for (int i = 0; i < length; i++) {
                long versionNo = resourceAdminClient.getVersionPaths(path)[0].getVersionNumber();
                String snapshotId = String.valueOf(versionNo);
                resourceAdminClient.deleteVersionHistory(path, snapshotId);
            }

            vp2 = resourceAdminClient.getVersionPaths(path);
            return vp2;
        }
    }


    @AfterClass
    public void testCleanResources()
            throws ResourceAdminServiceExceptionException, RemoteException, RegistryException {
        deleteResource("/_system/governance/trunk/services/com/abb/1.0.0-SNAPSHOT/abc");
        deleteResource("/_system/governance/trunk/services/org/wso2/carbon/core/services/echo/1.0.0-SNAPSHOT/echoyuSer1");
        deleteResource("/_system/governance/trunk/wsdls/com/foo/1.0.0-SNAPSHOT/abc.wsdl");
        deleteResource("/_system/governance/trunk/schemas/org/charitha/1.0.0/calculator.xsd");
        deleteResource("/_system/governance/trunk/policies/1.0.0/policy.xml");
        deleteResource("/_system/governance/trunk/wsdls/org/wso2/carbon/core/services/echo/echo.wsdl");
        deleteResource("/_system/governance/trunk/schemas/org/bar/purchasing/1.0.0-SNAPSHOT/purchasing.xsd");
        resourceAdminClient = null;
        governanceServiceClient = null;
        listMetaDataServiceClient = null;
    }

    public void deleteResource(String path) throws RegistryException {
        if (wsRegistryServiceClient.resourceExists(path)) {
            wsRegistryServiceClient.delete(path);
        }
    }
}
