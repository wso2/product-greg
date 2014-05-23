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


import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.FrameworkConstants;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.beans.xsd.LifecycleBean;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.integration.common.admin.client.ServerAdminClient;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.registry.activities.stub.RegistryExceptionException;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.lifecycle.test.utils.LifeCycleUtils;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.greg.integration.common.clients.LifeCycleAdminServiceClient;
import org.wso2.greg.integration.common.clients.LifeCycleManagementClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import javax.xml.namespace.QName;
import javax.xml.stream.*;
import java.io.*;
import java.rmi.RemoteException;

import static org.testng.Assert.assertEquals;


public class Carbon12924TestCase extends GREGIntegrationBaseTest {
    
    private static final Log log = LogFactory.getLog(Carbon12924TestCase.class);
    private LifeCycleManagementClient lifeCycleManagementClient;
    private LifeCycleAdminServiceClient lifeCycleAdminServiceClient;
    private ServerAdminClient serverAdminClient;
    private WSRegistryServiceClient registry;
    private Registry governance;
    private final String ASPECT_NAME = "RegistryXMLLifeCycle";
    private final String ACTION_PROMOTE = "Promote";
    private String trunk;
    private final String serviceName = "RegistryXMLLCTestService";
    private static FileOutputStream fileOutputStream;
    private static XMLStreamWriter writer;
    private String userNameWithoutDomain;


    @BeforeClass(groups = {"wso2.greg"}, description = "Add the LC configuration in registry.xml")
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    public void init() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String sessionCookie = getSessionCookie();

        serverAdminClient = new ServerAdminClient(backendURL, sessionCookie);
        String userName = automationContext.getContextTenant().getContextUser().getUserName();

        if(userName.contains(FrameworkConstants.SUPER_TENANT_DOMAIN_NAME)) {
            userNameWithoutDomain = userName.substring(0, userName.indexOf('@'));
        }else{
            userNameWithoutDomain = userName;
        }
    }

    @Test(groups = "wso2.greg", description = "Add Service 'RegistryXMLLCTestService' ")
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    public void changeRegXML() throws Exception {

        editRegistryXML();
        restartServer();
        initEnvironment();

    }

    private void initEnvironment() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String sessionCookie = getSessionCookie();

        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();

        registry = registryProviderUtil.getWSRegistry(automationContext);

        governance = registryProviderUtil.getGovernanceRegistry(registry, automationContext);

        lifeCycleManagementClient =
                new LifeCycleManagementClient(backendURL, sessionCookie);

        serverAdminClient =
                new ServerAdminClient(backendURL, sessionCookie);

        lifeCycleAdminServiceClient =
                new LifeCycleAdminServiceClient(backendURL, sessionCookie);
    }

    @Test(groups = "wso2.greg", description = "Add Service 'RegistryXMLLCTestService' ",
          dependsOnMethods = "changeRegXML")
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    public void testAddService() throws Exception {

        LifeCycleUtils.deleteLifeCycleIfExist(ASPECT_NAME, lifeCycleManagementClient);
        trunk = "/_system/governance" + LifeCycleUtils.addService("reg", serviceName, governance);

    }


    @Test(groups = "wso2.greg", description = "Add LifeCycle to a service", dependsOnMethods = "testAddService")
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    public void addLifeCycleToService()
            throws RegistryException, InterruptedException,
                   CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException,
                   RegistryExceptionException {

        registry.associateAspect(trunk, ASPECT_NAME);

        LifecycleBean lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(trunk);
        Resource service = registry.get(trunk);
        Assert.assertNotNull(service, "Service Not found on registry path " + trunk);
        assertEquals(service.getPath(), trunk, "Service path changed after adding life cycle. " + trunk);
        assertEquals(getLifeCycleState(lifeCycle), "Development",
                     "LifeCycle State Mismatched");

    }


    @Test(groups = "wso2.greg", description = " Promote LC from Development to Testing",
          dependsOnMethods = "addLifeCycleToService")
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    public void testPromoteToTesting()
            throws CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException,
                   RegistryException {


        lifeCycleAdminServiceClient.invokeAspect(trunk,
                                                 ASPECT_NAME, ACTION_PROMOTE, null);


        LifecycleBean lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(trunk);
        Resource service = registry.get(trunk);
        Assert.assertNotNull(service, "Service Not found on registry path " + trunk);
        Assert.assertEquals(service.getPath(), trunk, "Service not in trunk. " + trunk);

        Assert.assertEquals(getLifeCycleState(lifeCycle), "Tested", "LifeCycle State Mismatched");


    }

    @Test(groups = "wso2.greg", description = " Promote LC from Development to Testing",
          dependsOnMethods = "testPromoteToTesting")
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    public void deleteService()
            throws RegistryException, LifeCycleManagementServiceExceptionException,
                   RemoteException {


        ServiceManager serviceManager = new ServiceManager(governance);
        Service[] services = serviceManager.getAllServices();
        for (Service s : services) {
            if (s.getQName().getLocalPart().equals(serviceName)) {
                serviceManager.removeService(s.getId());
            }

        }

    }


    @Test(groups = {"wso2.greg"}, description = "Restore registry.xml to the previous state",
          dependsOnMethods = "deleteService")
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    public void restoreRegistryXML()
            throws XMLStreamException, IOException, InterruptedException, RegistryException {

        String registryXmlPath = CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator
                                 + "conf" + File.separator + "registry.xml";

        File srcFile = new File(registryXmlPath);
        try {
            OMElement element = getRegistryXmlOmElement();
            element.getChildrenWithName(new QName("aspect")).remove();
            element.build();
            fileOutputStream = new FileOutputStream(srcFile);
            writer = XMLOutputFactory.newInstance().createXMLStreamWriter(fileOutputStream);
            element.serialize(writer);
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


        String resourcePath = getTestArtifactLocation() + "artifacts" +
                              File.separator + "GREG" + File.separator + "xml" +
                              File.separator + "lifeCycleConfig.xml";

        Resource lifecycleConfig = governance.newResource();
        lifecycleConfig.setMediaType("application/xml");
        lifecycleConfig.setContentStream(new FileInputStream(resourcePath));
        governance.put("trunk/lifeCycleConfig.xml", lifecycleConfig);


    }


    @Test(groups = "wso2.greg", description = "Add Service 'RegistryXMLLCTestService' ",
          dependsOnMethods = "restoreRegistryXML")
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    public void changeRegXMLWithLCConfigLocation() throws Exception {

//        restartServer();
//        builder = new EnvironmentBuilder().greg(userId);
//        environment = builder.build();

        editRegistryXMLWithNewConfig();
        restartServer();
        initEnvironment();

    }

    @Test(groups = "wso2.greg", description = "Add Service 'RegistryXMLLCTestService' ",
          dependsOnMethods = "changeRegXMLWithLCConfigLocation")
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    public void testAddSameService() throws Exception {

        LifeCycleUtils.deleteLifeCycleIfExist(ASPECT_NAME, lifeCycleManagementClient);
        trunk = "/_system/governance" + LifeCycleUtils.addService("reg", serviceName, governance);

    }


    @Test(groups = "wso2.greg", description = "Add LifeCycle to a service", dependsOnMethods = "testAddSameService")
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    public void addLifeCycleToServiceAgain()
            throws RegistryException, InterruptedException,
                   CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException,
                   RegistryExceptionException {

        registry.associateAspect(trunk, ASPECT_NAME);

        LifecycleBean lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(trunk);
        Resource service = registry.get(trunk);
        Assert.assertNotNull(service, "Service Not found on registry path " + trunk);
        assertEquals(service.getPath(), trunk, "Service path changed after adding life cycle. " + trunk);
        assertEquals(getLifeCycleState(lifeCycle), "Development",
                     "LifeCycle State Mismatched");

    }


    @Test(groups = "wso2.greg", description = " Promote LC from Development to Testing",
          dependsOnMethods = "addLifeCycleToServiceAgain")
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    public void testPromoteToTestingAgain()
            throws CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException,
                   RegistryException {


        lifeCycleAdminServiceClient.invokeAspect(trunk,
                                                 ASPECT_NAME, ACTION_PROMOTE, null);


        LifecycleBean lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(trunk);
        Resource service = registry.get(trunk);
        Assert.assertNotNull(service, "Service Not found on registry path " + trunk);
        Assert.assertEquals(service.getPath(), trunk, "Service not in trunk. " + trunk);

        Assert.assertEquals(getLifeCycleState(lifeCycle), "Tested", "LifeCycle State Mismatched");


    }

    @Test(groups = "wso2.greg", description = " Promote LC from Development to Testing",
          dependsOnMethods = "testPromoteToTestingAgain")
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    public void deleteServiceAgain()
            throws RegistryException, LifeCycleManagementServiceExceptionException,
                   RemoteException {


        ServiceManager serviceManager = new ServiceManager(governance);
        Service[] services = serviceManager.getAllServices();
        for (Service s : services) {
            if (s.getQName().getLocalPart().equals(serviceName)) {
                serviceManager.removeService(s.getId());
            }

        }


    }

    @AfterClass(alwaysRun = true)
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    public void deleteAndRestore()
            throws Exception {

        String workList = "<workList serverURL=\"" + automationContext.getContextUrls().getBackEndUrl() +
                          "\" remote=\"false\">\n" +
                          "        <username>" + userNameWithoutDomain + "</username>\n" +
                          "        <password>" + automationContext.getContextTenant()
                .getContextUser().getPassword() +
                "</password>\n" +
                          "        </workList>";

        ServiceManager serviceManager = new ServiceManager(governance);
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
        Service[] services = serviceManager.getAllServices();
        for (Service s : services) {
            if (s.getQName().getLocalPart().equals(serviceName)) {
                serviceManager.removeService(s.getId());
            }

        }

        if (governance.resourceExists("trunk/lifeCycleConfig.xml")) {
            governance.delete("trunk/lifeCycleConfig.xml");
        }
        String registryXmlPath = CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator
                                 + "conf" + File.separator + "registry.xml";

        File srcFile = new File(registryXmlPath);
        try {
            OMElement element = getRegistryXmlOmElement();
            element.getChildrenWithName(new QName("aspect")).remove();

            if (!element.getChildrenWithName(new QName("workList")).hasNext()) {
                element.addChild(AXIOMUtil.stringToOM(workList));

            }

            element.build();
            fileOutputStream = new FileOutputStream(srcFile);
            writer = XMLOutputFactory.newInstance().createXMLStreamWriter(fileOutputStream);
            element.serialize(writer);

        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("Registry.xml file not found" + e.getMessage());

        } catch (XMLStreamException e) {
            throw new XMLStreamException("XML stream exception" + e.getMessage());

        } catch (IOException e) {
            throw new IOException("XML stream exception" + e.getMessage());

        } finally {
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            if (writer != null) {
                writer.flush();
            }
        }
        restartServer();
    }


    public void editRegistryXML()
            throws XMLStreamException, IOException, InterruptedException {
        String registryXmlPath = CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator
                                 + "conf" + File.separator + "registry.xml";

        File srcFile = new File(registryXmlPath);
        try {
            OMElement handlerConfig = getHandlerOmElement();
            OMElement registryXML = getRegistryXmlOmElement();
            if (registryXML.getChildrenWithName(new QName("workList")).hasNext()) {
                registryXML.getChildrenWithName(new QName("workList")).remove();

            }

            registryXML.addChild(handlerConfig);
            registryXML.build();
            fileOutputStream = new FileOutputStream(srcFile);
            writer = XMLOutputFactory.newInstance().createXMLStreamWriter(fileOutputStream);
            registryXML.serialize(writer);

        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("Registry.xml file not found" + e.getMessage());

        } catch (XMLStreamException e) {
            throw new XMLStreamException("XML stream exception" + e.getMessage());

        } catch (IOException e) {
            throw new IOException("XML stream exception" + e.getMessage());

        } finally {
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            if (writer != null) {
                writer.flush();
            }
        }


    }

    public void editRegistryXMLWithNewConfig() throws XMLStreamException, IOException,
                                                             InterruptedException {
        String registryXmlPath = CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator
                                 + "conf" + File.separator + "registry.xml";

        File srcFile = new File(registryXmlPath);
        try {
            OMElement handlerConfig = getHandlerOmElementNew();
            OMElement registryXML = getRegistryXmlOmElement();

            registryXML.addChild(handlerConfig);
            registryXML.build();
            fileOutputStream = new FileOutputStream(srcFile);
            writer =
                    XMLOutputFactory.newInstance().createXMLStreamWriter(fileOutputStream);
            registryXML.serialize(writer);


        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("Registry.xml file not found" + e.getMessage());

        } catch (XMLStreamException e) {
            throw new XMLStreamException("XML stream exception" + e.getMessage());

        } catch (IOException e) {
            throw new IOException("XML stream exception" + e.getMessage());

        } finally {
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            if (writer != null) {
                writer.flush();
            }
        }
    }

    public OMElement getHandlerOmElement()
            throws IOException, XMLStreamException {

        String handlerFilePath = getTestArtifactLocation() + "artifacts" +
                                 File.separator + "GREG" + File.separator + "xml" +
                                 File.separator + "lifecycleConfigForRegistryXML.xml";


        File handlerFile = new File(handlerFilePath);

        FileInputStream inputStream = new FileInputStream(handlerFile);
        XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(inputStream);
        StAXOMBuilder builder = new StAXOMBuilder(parser);

        return builder.getDocumentElement();

    }

    public OMElement getHandlerOmElementNew()
            throws FileNotFoundException, XMLStreamException {

        String handlerFilePath = getTestArtifactLocation() + "artifacts" +
                                 File.separator + "GREG" + File.separator + "xml" +
                                 File.separator + "lifecycleConfigForRegistryXML2.xml";


        File handlerFile = new File(handlerFilePath);

        FileInputStream inputStream = new FileInputStream(handlerFile);
        XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(inputStream);
        StAXOMBuilder builder = new StAXOMBuilder(parser);

        return builder.getDocumentElement();

    }

    public static OMElement getRegistryXmlOmElement()
            throws IOException, XMLStreamException, InterruptedException {
        String registryXmlPath = CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator
                                 + "conf" + File.separator + "registry.xml";


        File registryFile = new File(registryXmlPath);

        FileInputStream inputStream = new FileInputStream(registryFile);
        XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(inputStream);
        StAXOMBuilder builder = new StAXOMBuilder(parser);
        Thread.sleep(2000);

        return builder.getDocumentElement();

    }

    private void restartServer() throws Exception {
        ServerConfigurationManager serverConfigurationManager = new ServerConfigurationManager
                (automationContext);
        serverConfigurationManager.restartGracefully();
    }

    public static String getLifeCycleState(LifecycleBean lifeCycle) {
        Assert.assertTrue((lifeCycle.getLifecycleProperties().length > 0), "LifeCycle properties missing some properties");
        String state = null;
        boolean stateFound = false;
        for (Property prop : lifeCycle.getLifecycleProperties()) {
            if ("registry.lifecycle.RegistryXMLLifeCycle.state".equalsIgnoreCase(prop.getKey())) {
                stateFound = true;
                Assert.assertNotNull(prop.getValues(), "State Value Not Found");
                state = prop.getValues()[0];

            }
        }
        Assert.assertTrue(stateFound, "LifeCycle State property not found");
        return state;
    }
}
