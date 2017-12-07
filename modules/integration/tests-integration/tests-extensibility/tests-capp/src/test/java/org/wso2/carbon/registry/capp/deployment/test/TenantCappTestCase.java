/*
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.capp.deployment.test;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.application.mgt.stub.ApplicationAdminExceptionException;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.automation.test.utils.common.FileManager;
import org.wso2.carbon.integration.common.admin.client.LogViewerClient;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.logging.view.stub.types.carbon.LogEvent;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.greg.integration.common.clients.ApplicationAdminClient;
import org.wso2.greg.integration.common.clients.CarbonAppUploaderClient;
import org.wso2.greg.integration.common.clients.HandlerManagementServiceClient;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import javax.activation.DataHandler;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;

/**
 * + * This test case is  to verify the Capp deployment, + * and undeployment with new ArticatCleaupHandler +
 */
public class TenantCappTestCase extends GREGIntegrationBaseTest {

    private WSRegistryServiceClient wsRegistry;
    private CarbonAppUploaderClient cAppUploader;
    private ApplicationAdminClient adminServiceApplicationAdmin;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private HandlerManagementServiceClient handlerManagementServiceClient;

    private String cAppName = "GarTestCApp_1.0.0";
    private final String wsdlPath = "/_system/governance/trunk/wsdls/org/wso2/carbon/service/1.0.0/Axis2Service.wsdl";
    private final String servicePath = "/_system/governance/trunk/services/org/wso2/carbon/service/1.0.0/Axis2Service";
    private String newHandlerPath = FrameworkPathUtil.getSystemResourceLocation()
            + "artifacts" + File.separator + "GREG" + File.separator
            + "handler" + File.separator + "cleanup-handler.xml";
    private String handlerName = "org.wso2.carbon.registry.extensions.handlers.ArtifactCleanUpHandler";

    private String sessionCookie;
    private String backEndUrl;
    private String userName;
    private String userNameWithoutDomain;

    private LogViewerClient logViewerClient;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.init(TestUserMode.TENANT_ADMIN);
        backEndUrl = getBackendURL();
        sessionCookie = getSessionCookie();
        userName = automationContext.getContextTenant().getContextUser().getUserName();

        if (userName.contains("@")) {
            userNameWithoutDomain = userName.substring(0, userName.indexOf('@'));
        } else {
            userNameWithoutDomain = userName;
        }

        resourceAdminServiceClient =
                new ResourceAdminServiceClient(backEndUrl,
                                               sessionCookie);
        adminServiceApplicationAdmin =
                new ApplicationAdminClient(backEndUrl,
                                           sessionCookie);

        cAppUploader =
                new CarbonAppUploaderClient(backEndUrl,
                                            sessionCookie);

        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        wsRegistry = registryProviderUtil.getWSRegistry(automationContext);
        handlerManagementServiceClient = new HandlerManagementServiceClient(backEndUrl,
                                                                            sessionCookie);

    }

    @Test(groups = {"wso2.greg"}, description = "Add new Handler")
    public void addNewHandler() throws Exception {
        assertTrue(handlerManagementServiceClient.createHandler(FileManager.readFile(newHandlerPath)));
    }

    @Test(description = "Upload CApp having gar file", dependsOnMethods = {"addNewHandler"})
    public void uploadCApplicationWithGar()
            throws Exception {
        String filePath = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator +
                "GREG" + File.separator + "car" + File.separator + "GarTestCApp_1.0.0.car";
        cAppUploader.uploadCarbonAppArtifact("GarTestCApp_1.0.0.car",
                                             new DataHandler(new URL("file:///" + filePath)));

        Assert.assertTrue(CAppTestUtils.isCAppDeployed(sessionCookie, cAppName, adminServiceApplicationAdmin)
                , "Deployed CApplication not in CApp List");

        AutomationContext automationContext2 = new AutomationContext("GREG", TestUserMode.SUPER_TENANT_ADMIN);
        ServerConfigurationManager serverConfigurationManager = new ServerConfigurationManager(automationContext2);
        String resourcePath =
                getTestArtifactLocation() + "artifacts" + File.separator + "GREG" + File.separator + "carbon" +
                        File.separator + "carbon.xml";
        serverConfigurationManager.applyConfiguration(new File(resourcePath));
        //serverConfigurationManager.restartGracefully();

        super.init(TestUserMode.TENANT_ADMIN);
        backEndUrl = getBackendURL();
        sessionCookie = getSessionCookie();

        resourceAdminServiceClient = new ResourceAdminServiceClient(backEndUrl, sessionCookie);
        adminServiceApplicationAdmin = new ApplicationAdminClient(backEndUrl, sessionCookie);

        cAppUploader = new CarbonAppUploaderClient(backEndUrl, sessionCookie);
        handlerManagementServiceClient = new HandlerManagementServiceClient(backEndUrl, sessionCookie);

        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        wsRegistry = registryProviderUtil.getWSRegistry(automationContext);
        logViewerClient = new LogViewerClient(backendURL, sessionCookie);
        Assert.assertTrue(testSearchLogs());
    }

    @Test(description = "Search whether CApp is in /_system/config/repository/applications",
          dependsOnMethods = {"uploadCApplicationWithGar"})
    public void isCApplicationInRegistry() throws RegistryException {
        wsRegistry.get("/_system/config/repository/carbonapps/gar_mapping/Axis2Service");
        wsRegistry.get("/_system/config/repository/carbonapps/path_mapping/" + cAppName);
    }

    @Test(description = "Verify Uploaded Resources", dependsOnMethods = {"uploadCApplicationWithGar"})
    public void isResourcesExist() throws RegistryException {

        Assert.assertTrue(wsRegistry.resourceExists(wsdlPath), wsdlPath + " resource does not exist");
        Assert.assertTrue(wsRegistry.resourceExists(servicePath), servicePath + " resource does not exist");
        //        Assert.assertTrue(registry.resourceExists(wsdlUploadedPath), wsdlUploadedPath + " resource does not
        // exist");

    }

    @Test(description = "Delete Carbon Application ", dependsOnMethods = {"isResourcesExist"})
    public void deleteCApplication()
            throws ApplicationAdminExceptionException, RemoteException, InterruptedException,
                   RegistryException {
        adminServiceApplicationAdmin.deleteApplication(cAppName);

        Assert.assertTrue(CAppTestUtils.isCAppDeleted(sessionCookie, cAppName, adminServiceApplicationAdmin)
                , "Deployed CApplication still in CApp List");
        try {
            // Wait few second to delete the  GarTestCApp file
            Thread.sleep(50000);
        } catch (InterruptedException e) {
            //ignore
        }
    }

    @Test(description = "Verify Resource Deletion", dependsOnMethods = {"deleteCApplication"})
    public void isResourcesDeleted() throws Exception {

        delete(wsdlPath);
        delete(servicePath);
        delete("/_system/governance/trunk/services/org/wso2/carbon/service/Axis2Service");
        delete("/_system/governance/trunk/wsdls/org/wso2/carbon/service/Axis2Service.wsdl");
        delete("/_system/governance/trunk/schemas/org/wso2/carbon/service/axis2serviceschema.xsd");
        delete("/_system/governance/trunk/endpoints/_1");
        delete("/_system/config/repository/carbonapps/gar_mapping/Axis2Service");

        Assert.assertFalse(wsRegistry.resourceExists(wsdlPath), "Resource not deleted");
        Assert.assertFalse(wsRegistry.resourceExists(servicePath), "Resource not deleted");

        Assert.assertFalse(wsRegistry.resourceExists("/_system/config/repository/carbonapps/path_mapping/" + cAppName)
                , "CApp Resource not deleted");
        Assert.assertFalse(wsRegistry.resourceExists("/_system/config/repository/carbonapps/gar_mapping/Axis2Service"),
                           "CApp Resource not deleted");

    }

    @Test(groups = {"wso2.greg"}, description = "delete handler", dependsOnMethods = "isResourcesDeleted")
    public void deleteHandler() throws Exception {
        assertTrue(handlerManagementServiceClient.deleteHandler(handlerName));
    }

    @AfterClass(alwaysRun = true)
    public void destroy()
            throws ApplicationAdminExceptionException, RemoteException, InterruptedException,
                   ResourceAdminServiceExceptionException, RegistryException {
        if (!(CAppTestUtils.isCAppDeleted(sessionCookie,
                                          cAppName, adminServiceApplicationAdmin))) {
            adminServiceApplicationAdmin.deleteApplication(cAppName);
        }

        delete(wsdlPath);
        delete(servicePath);
        delete("/_system/governance/trunk/services/org/wso2/carbon/service/Axis2Service");
        delete("/_system/governance/trunk/wsdls/org/wso2/carbon/service/Axis2Service.wsdl");
        delete("/_system/governance/trunk/schemas/org/wso2/carbon/service/axis2serviceschema.xsd");
        delete("/_system/governance/trunk/endpoints/_1");

        cAppUploader = null;
        adminServiceApplicationAdmin = null;
        wsRegistry = null;
        resourceAdminServiceClient = null;
        handlerManagementServiceClient = null;

    }

    public void delete(String destPath)
            throws ResourceAdminServiceExceptionException, RemoteException, RegistryException {
        if (wsRegistry.resourceExists(destPath)) {
            resourceAdminServiceClient.deleteResource(destPath);
        }
    }

    private void increaseSearchIndexStartTimeDelay() throws Exception {

        FileOutputStream fileOutputStream = null;
        XMLStreamWriter writer = null;
        OMElement documentElement = getRegistryXmlOmElement();
        try {
            AXIOMXPath xpathExpression = new AXIOMXPath("/Server/Tenant/LoadingPolicy/LazyLoading");

            OMElement element = (OMElement) xpathExpression.selectSingleNode(documentElement);
            element.detach();

            AXIOMXPath xpathExpression1 = new AXIOMXPath("/Server/Tenant/LoadingPolicy");
            OMElement indexConfigNode1 = (OMElement) xpathExpression1.selectSingleNode(documentElement);

            OMFactory factory = OMAbstractFactory.getOMFactory();
            OMElement method = factory.createOMElement("EagerLoading", null);
            OMElement value = factory.createOMElement("Include", null);
            value.addChild(factory.createOMText(value, "*"));
            method.addChild(value);

            fileOutputStream = new FileOutputStream(getRegistryXMLPath());
            writer = XMLOutputFactory.newInstance().createXMLStreamWriter(fileOutputStream);
            documentElement.serialize(writer);
            documentElement.build();
            Thread.sleep(2000);

        } catch (Exception e) {
            log.error("registry.xml edit fails" + e.getMessage());
            throw new Exception("registry.xml edit fails" + e.getMessage());
        } finally {
            assert fileOutputStream != null;
            fileOutputStream.close();
            assert writer != null;
            writer.flush();
        }
    }

    public boolean testSearchLogs() throws RemoteException {
        LogEvent[] logEvents = logViewerClient.getLogs("INFO", "GarTestCApp_1.0.0 {tenant-1}", "", "");
        return (logEvents[0].getMessage().contains("GarTestCApp_1.0.0 {tenant-1}"));
    }

    public static OMElement getRegistryXmlOmElement()
            throws FileNotFoundException, XMLStreamException {

        String registryXmlPath = CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator
                + "conf" + File.separator + "carbon.xml";
        File registryFile = new File(registryXmlPath);
        FileInputStream inputStream = new FileInputStream(registryFile);
        XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(inputStream);
        StAXOMBuilder builder = new StAXOMBuilder(parser);
        return builder.getDocumentElement();

    }

    private String getRegistryXMLPath() {
        return CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator
                + "conf" + File.separator + "carbon.xml";
    }
}