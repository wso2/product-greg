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


package org.wso2.carbon.registry.jira.issues.test;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.AXIOMUtil;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.greg.integration.common.clients.ListMetaDataServiceClient;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.stream.*;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Iterator;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * 
 * Disabled this test case due to REGISTRY-2338
 *
 */
public class Carbon11572TestCase extends GREGIntegrationBaseTest {


    private ResourceAdminServiceClient resourceAdminClient;
    private ListMetaDataServiceClient listMetaDataServiceClient;
    private FileOutputStream fileOutputStream;
    private XMLStreamWriter writer;
    private Registry governance;
    private WsdlManager wsdlManager;
    private ServiceManager serviceManager;

    @BeforeClass(alwaysRun = true)
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    public void initialize() throws Exception {
        init();
    }

    public void init() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String session = getSessionCookie();

        resourceAdminClient =
                new ResourceAdminServiceClient(backendURL, session);
        listMetaDataServiceClient =
                new ListMetaDataServiceClient(backendURL, session);


        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();

        WSRegistryServiceClient wsRegistry =
                registryProviderUtil.getWSRegistry(automationContext);
        governance = registryProviderUtil.getGovernanceRegistry(wsRegistry, automationContext);

         wsdlManager = new WsdlManager(governance);

         serviceManager = new ServiceManager(governance);

    }

    @Test(groups = {"wso2.greg"}, description = "change registry.xml and restart server", enabled=false)
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    public void editRegistryXML() throws Exception {

        String registryXmlPath = CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator
                                 + "conf" + File.separator + "registry.xml";

        File srcFile = new File(registryXmlPath);
        try {
            OMElement handlerConfig = AXIOMUtil.stringToOM("<property name=\"createService\">false</property>");
            OMElement registryXML = getRegistryXmlOmElement();

            OMElement om1 = null;
            for (Iterator iterator = registryXML.getChildrenWithName(new QName("handler")); iterator.hasNext(); ) {
                OMElement om = (OMElement) iterator.next();

                if (om.getAttribute(new QName("class")).getAttributeValue().equals("org.wso2.carbon.registry.extensions.handlers" +
                                                                                   ".WSDLMediaTypeHandler")) {
                    om1 = om;
                    om1.addChild(handlerConfig);
                    registryXML.addChild(om1);
                    registryXML.build();
                    break;
                }

            }

            fileOutputStream = new FileOutputStream(srcFile);
            writer =
                    XMLOutputFactory.newInstance().createXMLStreamWriter(fileOutputStream);
            registryXML.serialize(writer);


        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("Registry.xml file not found" + e);

        } catch (XMLStreamException e) {
            throw new XMLStreamException("XML stream exception" + e);

        } catch (IOException e) {
            throw new IOException("IO exception" + e);

        } finally {
            writer.close();
            fileOutputStream.close();
        }

        restartServer();
        init(); //reinitialize environment after server restart.


    }

    @Test(groups = {
            "wso2.greg"}, description = "add a WSDL without creating a service", dependsOnMethods = "editRegistryXML", enabled=false)
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    public void addWSDL()
            throws ResourceAdminServiceExceptionException, RemoteException, MalformedURLException, RegistryException,
                   GovernanceException, InterruptedException {
    	GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
        boolean nameExists = false;
        //add WSDL file
        String path1 = getTestArtifactLocation() + "artifacts" + File.separator
                       + "GREG" + File.separator + "wsdl" + File.separator + "echo.wsdl";
        DataHandler dataHandler1 = new DataHandler(new URL("file:///" + path1));


       Wsdl[] wsdls = wsdlManager.getAllWsdls();

        //delete wsdl if exists
        if (wsdls != null) {
                for (Wsdl wsdl : wsdls) {
                    if ("echo.wsdl".equals(wsdl.getQName().getLocalPart())) {
                        resourceAdminClient.deleteResource(wsdl.getPath());
                        break;
                    }
                }
        }

       Service[] services = serviceManager.getAllServices();

        //delete service if exists
        if (services != null) {
                for (Service service : services) {
                    if ("echoyuSer1".equals(service.getPath())) {
                        resourceAdminClient.deleteResource(service.getPath());
                        break;
                    }
            }
        }
        resourceAdminClient.addWSDL("desc 1", dataHandler1);
        Thread.sleep(20000);
        wsdls = wsdlManager.getAllWsdls();

            for (Wsdl wsdl: wsdls) {
                if ("echo.wsdl".equals(wsdl.getQName().getLocalPart())) {
                    nameExists = true;
                }

        }
        assertTrue(nameExists);
        //check whether the service is created

        boolean serviceStatus = false;

        services = serviceManager.getAllServices();

        for (Service service : services) {
            if ("echoyuSer1".equals(service.getQName().getLocalPart())) {
                serviceStatus = true;
                break;
            }
        }

        assertFalse(serviceStatus);
    }

    @AfterClass(alwaysRun = true)
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    public void cleanUp()
            throws IOException, ResourceAdminServiceExceptionException, XMLStreamException {

        String registryXmlPath = CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator
                                 + "conf" + File.separator + "registry.xml";

        File srcFile = new File(registryXmlPath);
        OMElement element = getRegistryXmlOmElement();
        OMElement omElement = null;

        for (Iterator iterator = element.getChildrenWithName(new QName("handler")); iterator.hasNext(); ) {
            OMElement om = (OMElement) iterator.next();

            if (om.getAttribute(new QName("class")).getAttributeValue().equals("org.wso2.carbon.registry.extensions.handlers" +
                                                                               ".WSDLMediaTypeHandler")) {

                omElement = om;
                for (Iterator it = omElement.getChildrenWithName(new QName("property")); it.hasNext(); ) {
                    OMElement omRemove = (OMElement) it.next();
                    if (omRemove.getAttribute(new QName("name")).getAttributeValue().equals("createService")) {
                        it.remove();
                        element.build();
                        break;
                    }
                }
                break;
            }
        }

        fileOutputStream = new FileOutputStream(srcFile);
        writer = XMLOutputFactory.newInstance().createXMLStreamWriter(fileOutputStream);
        element.serialize(writer);
        element.build();
        writer.close();
        fileOutputStream.close();


        String pathToDelete = "/_system/governance/" + listMetaDataServiceClient.listWSDLs().getPath()[0];

        if (pathToDelete != null && listMetaDataServiceClient.listWSDLs().getPath()[0] != null) {
            resourceAdminClient.deleteResource(pathToDelete);
        }
    }

    public static OMElement getRegistryXmlOmElement()
            throws FileNotFoundException, XMLStreamException {
        String registryXmlPath = CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator
                                 + "conf" + File.separator + "registry.xml";


        File registryFile = new File(registryXmlPath);

        FileInputStream inputStream = new FileInputStream(registryFile);
        XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(inputStream);
        StAXOMBuilder builder = new StAXOMBuilder(parser);

        return builder.getDocumentElement();
    }


    private void restartServer() throws Exception {
        ServerConfigurationManager serverConfigurationManager = new ServerConfigurationManager
                (automationContext);
        serverConfigurationManager.restartGracefully();
    }
}
