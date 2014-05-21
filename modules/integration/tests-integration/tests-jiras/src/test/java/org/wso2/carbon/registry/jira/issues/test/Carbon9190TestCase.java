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
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.integration.common.utils.FileManager;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.info.stub.RegistryExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.beans.xsd.ContentDownloadBean;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import javax.activation.DataHandler;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class Carbon9190TestCase extends GREGIntegrationBaseTest {

    private Registry governance;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private WsdlManager wsdlManager;
    private boolean wsdlEchoAvailable;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String session = getSessionCookie();

        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        WSRegistryServiceClient registry = registryProviderUtil.getWSRegistry(automationContext);
        governance = registryProviderUtil.getGovernanceRegistry(registry, automationContext);

        resourceAdminServiceClient =
                new ResourceAdminServiceClient(backendURL, session);
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);


    }

    @Test(groups = {"wso2.greg"}, description = "Add 2 WSDLs with different names but same NS and Validate Existence")
    public void addWSDLWithDifferentNames()
            throws ResourceAdminServiceExceptionException, RegistryException,
                   IOException {
        addWSDLFromManager("echo.wsdl", "echo.wsdl");
        addWSDLAsResource("echo2.wsdl");
        assertTrue(governance.resourceExists("trunk/wsdls/org/wso2/carbon/core/services/echo/echo.wsdl"),
                   "echo.wsdl does not exist");
        assertTrue(governance.resourceExists("trunk/wsdls/org/wso2/carbon/core/services/echo/echo2.wsdl"),
                   "echo2.wsdl does not exist");

        wsdlEchoAvailable = false;
        boolean wsdlEcho2Available = false;
        wsdlManager = new WsdlManager(governance);
        Wsdl[] wsdls = wsdlManager.getAllWsdls();
        for (Wsdl wsdl : wsdls) {
            if (wsdl.getQName().getLocalPart().equals("echo.wsdl")) {
                wsdlEchoAvailable = true;

            }
            if (wsdl.getQName().getLocalPart().equals("echo2.wsdl")) {
                wsdlEcho2Available = true;
            }
        }

        assertTrue(wsdlEchoAvailable, "Wsdl echo.wsdl is not available");
        assertTrue(wsdlEcho2Available, "Wsdl echo2.wsdl is not available");

        for (Wsdl wsdl : wsdls) {
            if (wsdl.getQName().getLocalPart().equals("echo.wsdl") | wsdl.getQName().getLocalPart().equals("echo2.wsdl")) {
                wsdlManager.removeWsdl(wsdl.getId());
            }
        }
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
        ServiceManager serviceManager = new ServiceManager(governance);
        Service[] services = serviceManager.getAllServices();
        for (Service s : services) {
            if (s.getQName().getLocalPart().equals("echoyuSer1")) {
                serviceManager.removeService(s.getId());
            }

        }

    }

    @Test(groups = {"wso2.greg"}, description = "Add WSDLs with Different NS but same name and Validate Existence",
          dependsOnMethods = "addWSDLWithDifferentNames")
    public void addWSDLWithDifferentNs()
            throws ResourceAdminServiceExceptionException, IOException, RegistryException {
        addWSDLFromManager("echoNsChanged.wsdl", "echo.wsdl");
        addWSDLAsResource("echo.wsdl");

        assertTrue(governance.resourceExists("trunk/wsdls/org/wso2/carbon/core/services/echo/echo.wsdl"),
                   "echo.wsdl does not exist");
        assertTrue(governance.resourceExists("trunk/wsdls/org/wso2/carbon/core/services/echo/new/echo.wsdl"),
                   "echo.wsdl with Different NS does not exist");

        int noOfWsdls = 0;
        wsdlManager = new WsdlManager(governance);
        Wsdl[] wsdls = wsdlManager.getAllWsdls();
        for (Wsdl wsdl : wsdls) {
            if (wsdl.getQName().getLocalPart().equals("echo.wsdl")) {
                noOfWsdls += 1;

            }

        }

        assertEquals(noOfWsdls, 2, "All 2 echo wsdls are not available");

        for (Wsdl wsdl : wsdls) {
            if (wsdl.getQName().getLocalPart().equals("echo.wsdl")) {
                wsdlManager.removeWsdl(wsdl.getId());
            }
        }

        ServiceManager serviceManager = new ServiceManager(governance);
        Service[] services = serviceManager.getAllServices();
        for (Service s : services) {
            if (s.getQName().getLocalPart().equals("echoyuSer1")) {
                serviceManager.removeService(s.getId());
            }

        }

    }


    @Test(groups = {"wso2.greg"}, description = "Add WSDLs with Different Content but same Name,NS",
          dependsOnMethods = "addWSDLWithDifferentNs")
    public void addWSDLWithDifferentContent()
            throws ResourceAdminServiceExceptionException, IOException,
                   RegistryException, RegistryExceptionException {
        addWSDLAsResource("echo.wsdl");
        addWSDLFromManager("echoWithNewTag.wsdl", "echo.wsdl");

        assertTrue(governance.resourceExists("trunk/wsdls/org/wso2/carbon/core/services/echo/echo.wsdl"),
                   "echo.wsdl does not exist");
        wsdlEchoAvailable = false;
        wsdlManager = new WsdlManager(governance);
        Wsdl[] wsdls = wsdlManager.getAllWsdls();
        for (Wsdl wsdl : wsdls) {
            if (wsdl.getQName().getLocalPart().equals("echo.wsdl")) {
                wsdlEchoAvailable = true;
            }

        }

        assertTrue(wsdlEchoAvailable, "echo.wsdl is not available.");


        ContentDownloadBean contentDownloadBean = resourceAdminServiceClient.getContentDownloadBean
                ("/_system/governance/trunk/wsdls/org/wso2/carbon/core/services/echo/echo.wsdl");
        DataHandler dh = contentDownloadBean.getContent();
        BufferedReader br = new BufferedReader(new InputStreamReader(dh.getInputStream()));
        StringBuffer sb = new StringBuffer();

        String sCurrentLine;

        while ((sCurrentLine = br.readLine()) != null) {

            sb.append(sCurrentLine);
        }


        boolean wsdlContentUpdated = false;
        for (Wsdl wsdl : wsdls) {
            if (wsdl.getQName().getLocalPart().equals("echo.wsdl")) {
                if (sb.toString().contains("<xs:element maxOccurs=\"unbounded\" minOccurs=\"0\"" +
                                           " name=\"c\" nillable=\"true\" type=\"xs:string\"/>")) {
                    wsdlContentUpdated = true;
                }

            }

        }

        assertTrue(wsdlContentUpdated, "Wsdl Content is not updated");


    }

    @AfterClass
    public void removeArtifacts() throws RegistryException, ResourceAdminServiceExceptionException, RemoteException {

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
            if (s.getQName().getLocalPart().equals("echoyuSer1")) {
                serviceManager.removeService(s.getId());
            }

        }

        resourceAdminServiceClient.deleteResource("/_system/governance/trunk/wsdls/org");
        governance = null;
        resourceAdminServiceClient = null;
        wsdlManager = null;

    }

    public void addWSDLFromManager(String wsdlFileName, String wsdlName)
            throws IOException, ResourceAdminServiceExceptionException, RegistryException {
        Wsdl wsdl;
        wsdlManager = new WsdlManager(governance);

        String wsdlFilePath = getTestArtifactLocation() + "artifacts" + File.separator +
                              "GREG" + File.separator + "wsdl" + File.separator + wsdlFileName;
        wsdl = wsdlManager.newWsdl(FileManager.readFile(wsdlFilePath).getBytes(), wsdlName);
        wsdlManager.addWsdl(wsdl);

    }

    public void addWSDLAsResource(String wsdlName)
            throws MalformedURLException, ResourceAdminServiceExceptionException, RemoteException,
                   RegistryException, FileNotFoundException {

        String resourcePath = getTestArtifactLocation() + "artifacts" + File.separator +
                              "GREG" + File.separator + "wsdl" + File.separator + wsdlName;
        DataHandler dh = new DataHandler(new URL("file:///" + resourcePath));
        resourceAdminServiceClient.addWSDL("WSDL Description", dh);


    }
}
