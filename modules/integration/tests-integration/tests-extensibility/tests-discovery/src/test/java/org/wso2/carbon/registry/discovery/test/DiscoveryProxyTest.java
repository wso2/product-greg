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

package org.wso2.carbon.registry.discovery.test;


import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.automation.engine.frameworkutils.TestFrameworkUtils;
import org.wso2.carbon.discovery.DiscoveryConstants;
import org.wso2.carbon.discovery.DiscoveryOMUtils;
import org.wso2.carbon.discovery.messages.Notification;
import org.wso2.carbon.discovery.messages.TargetService;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.clients.AuthenticatorClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import javax.xml.namespace.QName;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.UUID;

import static org.apache.axiom.om.OMAbstractFactory.getOMFactory;
import static org.testng.Assert.*;

public class DiscoveryProxyTest extends GREGIntegrationBaseTest {

    int userId = 2;
    private Registry governance;
    private String scopes;
    private String types;
    private String metadataVersion;
    private ServiceClient serviceClient;
    private String discoveryProxyEPR;
    private EndpointReference endpointReference;
    private String uniqueID;
    private TargetService targetService;
    private Notification notification;
    private WSRegistryServiceClient registry;
    private AuthenticatorClient authenticatorClient;
    private boolean serviceDiscovered;

    private String sessionCookie;
    private String backEndUrl;
    private String userName;
    private String userNameWithoutDomain;

    @BeforeClass(alwaysRun = true)
    public void initialize()
            throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        backEndUrl = getBackendURL();
        sessionCookie = getSessionCookie();
        userName = automationContext.getContextTenant().getContextUser().getUserName();

        if (userName.contains("@"))
            userNameWithoutDomain = userName.substring(0, userName.indexOf('@'));
        else
            userNameWithoutDomain = userName;

        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        registry = registryProviderUtil.getWSRegistry(automationContext);
        governance = registryProviderUtil.getGovernanceRegistry(registry, automationContext);
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);

        authenticatorClient = new AuthenticatorClient( backendURL);

        authenticatorClient.login(userName,
                automationContext.getContextTenant().getContextUser().getPassword(),
                automationContext.getInstance().getHosts().get("default"));
        if (governance.resourceExists("trunk/endpoints/eu")) {
            governance.delete("trunk/endpoints/eu");

        }
    }

    @Test(groups = "wso2.greg", description = "DiscoveryProxy hello/probe")
    public void testDiscoveryProxy() throws Exception {

         discoveryProxyEPR = (backEndUrl + "DiscoveryProxy");


        //Hello
        serviceClient = initServiceClient(discoveryProxyEPR, DiscoveryConstants.NOTIFICATION_TYPE_HELLO);

        uniqueID = UUID.randomUUID().toString();
        endpointReference = new EndpointReference(uniqueID);
        targetService = new TargetService(endpointReference);
        populateTargetService(targetService, DiscoveryConstants.NOTIFICATION_TYPE_HELLO);

        notification =
                new Notification(DiscoveryConstants.NOTIFICATION_TYPE_HELLO, targetService);

        serviceClient.addStringHeader(
                new QName(DiscoveryConstants.DISCOVERY_HEADER_ELEMENT_NAMESPACE,
                          DiscoveryConstants.DISCOVERY_HEADER_SERVICE_NAME,
                          DiscoveryConstants.DISCOVERY_HEADER_ELEMENT_NAMESPACE_PREFIX),
                "DiscoveryProxyService");

        serviceClient.addStringHeader(
                new QName(DiscoveryConstants.DISCOVERY_HEADER_ELEMENT_NAMESPACE,
                          DiscoveryConstants.DISCOVERY_HEADER_WSDL_URI,
                          DiscoveryConstants.DISCOVERY_HEADER_ELEMENT_NAMESPACE_PREFIX),
                "https://raw.githubusercontent.com/wso2/wso2-qa-artifacts/master/automation-artifacts/greg/wsdl/calculator.wsdl");

        serviceClient.fireAndForget(DiscoveryOMUtils.toOM(notification, getOMFactory()));

        Calendar startTime = Calendar.getInstance();
        serviceDiscovered = false;
        while ((Calendar.getInstance().getTimeInMillis() - startTime.getTimeInMillis()) < 30000) {
            ServiceManager serviceManager = new ServiceManager(governance);
            for (Service s : serviceManager.getAllServices()) {
                if (s.getAttribute("overview_namespace").equals("http://docs.oasis-open.org/ws-dd/ns/discovery/2009/01")) {
                    serviceDiscovered = true;
                    break;
                }
            }
            Thread.sleep(3000);
            if(serviceDiscovered){
                break;
            }
        }

        WsdlManager wsdlManager = new WsdlManager(governance);
        Wsdl[] wsdls = wsdlManager.getAllWsdls();
        assertEquals(wsdls[0].getQName().getLocalPart(), "DiscoveryProxyService.wsdl", "Wsdl not imported with the service");

        assertTrue(serviceDiscovered, "Service is not available in the Registry");
        serviceClient.cleanup();


        serviceClient = initServiceClient(discoveryProxyEPR, 3);
        OMElement element =
                serviceClient.sendReceive(AXIOMUtil.stringToOM("<wsd:Probe xmlns:wsd=\"http://docs.oasis-open.org/" +
                                                               "ws-dd/ns/discovery/2009/01\" />"));
        element.build();
        for (Iterator iterator = element.getChildrenWithName(new QName("ProbeMatch")); iterator.hasNext(); ) {
            OMElement omElement = (OMElement) iterator.next();
            if (omElement.getChildrenWithName(new QName("Scopes")).hasNext()) {
                OMElement omElement1 = (OMElement) omElement.getChildrenWithName(new QName("Scopes")).next();
                scopes = omElement1.getText();
            }

            if (omElement.getChildrenWithName(new QName("Types")).hasNext()) {
                OMElement omElement2 = (OMElement) omElement.getChildrenWithName(new QName("Types")).next();
                types = omElement2.getText();
            }

            if (omElement.getChildrenWithName(new QName("MetadataVersion")).hasNext()) {
                OMElement omElement3 = (OMElement) omElement.getChildrenWithName(new QName("MetadataVersion")).next();
                metadataVersion = omElement3.getText();
            }

        }
        assertEquals(scopes, "http://docs.oasis-open.org/ws-dd/ns/discovery/2009/01/DefaultScope",
                     "Qname 'Scopes' not set");
        assertNotNull(types, "Qname 'Types' not set");
        assertNotNull(metadataVersion, "Qname 'MetadataVersion' not set");


    }

    @Test(groups = "wso2.greg", description = "DiscoveryProxy/Bye", dependsOnMethods = "testDiscoveryProxy")
    public void testDiscoveryProxyBye() throws Exception {

        // Bye
        serviceClient = initServiceClient(discoveryProxyEPR, DiscoveryConstants.NOTIFICATION_TYPE_BYE);

        endpointReference = new EndpointReference(uniqueID);
        populateTargetService(targetService, DiscoveryConstants.NOTIFICATION_TYPE_BYE);

        notification = new Notification(DiscoveryConstants.NOTIFICATION_TYPE_BYE, targetService);
        serviceClient.fireAndForget(DiscoveryOMUtils.toOM(notification,
                                                          getOMFactory()));

        Calendar startTime = Calendar.getInstance();
        serviceDiscovered = false;
        while ((Calendar.getInstance().getTimeInMillis() - startTime.getTimeInMillis()) < 30000) {
            ServiceManager serviceManager = new ServiceManager(governance);
            for (Service s : serviceManager.getAllServices()) {
                if (s.getAttribute("overview_namespace").equals("http://docs.oasis-open.org/ws-dd/ns/discovery/2009/01")) {
                    serviceDiscovered = true;
                }


            }
            Thread.sleep(3000);

        }
        assertTrue(serviceDiscovered, "Service is still available in the Registry");

        serviceClient.cleanup();

        serviceClient = initServiceClient(discoveryProxyEPR, 3);
        Calendar byeStartTime = Calendar.getInstance();
        boolean byeSuccess = false;
        while ((Calendar.getInstance().getTimeInMillis() - byeStartTime.getTimeInMillis()) < 20000) {
            OMElement element2 = serviceClient.sendReceive(AXIOMUtil.stringToOM("<wsd:Probe xmlns:wsd=\"http://docs" +
                                                                                ".oasis-open.org/ws-dd/ns/discovery/2009/01\" />"));
            element2.build();
            Iterator iterator2 = element2.getChildrenWithName(new QName("ProbeMatch"));

            if (!iterator2.hasNext()) {
                byeSuccess = true;
                break;
            }

            Thread.sleep(2000);
        }
        assertTrue(byeSuccess, "Service is not Removed from the Registry");
    }

    @AfterClass
    public void DeleteLCResources()
            throws LifeCycleManagementServiceExceptionException, RemoteException,
                   RegistryException {

        ServiceManager serviceManager = new ServiceManager(governance);
        Service[] services = serviceManager.getAllServices();
        for (Service s : services) {
            if (s.getAttribute("overview_namespace").equals("http://docs.oasis-open.org/ws-dd/ns/discovery/2009/01")) {
                serviceManager.removeService(s.getId());
            }
        }
        governance = null;
        registry = null;
        authenticatorClient = null;
        serviceClient.cleanup();
        serviceClient = null;

    }

    private void populateTargetService(TargetService targetService, int notificationType)
            throws URISyntaxException {

        targetService.setTypes(getTypes());
        targetService.setScopes(getScopes());

        URI[] xAddress = new URI[2];
        xAddress[0] = new URI("https://localhost:9446/services/HelloService/");
        xAddress[1] = new URI("http://localhost:9446/services/HelloService/");

        targetService.setXAddresses(xAddress);
        targetService.setMetadataVersion(notificationType);
    }


    private QName[] getTypes() {
        QName[] types = new QName[2];
        types[0] = new QName("http://www.example.com/service1", "type1");
        types[1] = new QName("http://www.example.com/service2", "type1");
        return types;
    }

    private URI[] getScopes() throws URISyntaxException {
        URI[] scopes = new URI[1];
        scopes[0] = new URI("http://docs.oasis-open.org/ws-dd/ns/discovery/2009/01/DefaultScope");
        return scopes;
    }


    private ServiceClient initServiceClient(String epr, int notificationType) throws Exception {

        String axis2Repo = FrameworkPathUtil.getSystemResourceLocation() + File.separator + "client";
        String axis2Conf = FrameworkPathUtil.getSystemResourceLocation() + File.separator + "axis2config" +
                           File.separator + "axis2_client.xml";
        TestFrameworkUtils.setKeyStoreProperties(automationContext);

        ConfigurationContext configContext =
                ConfigurationContextFactory.createConfigurationContextFromFileSystem(axis2Repo, axis2Conf);


        serviceClient = new ServiceClient(configContext, null);
        serviceClient.setTargetEPR(new EndpointReference(epr));
        if (notificationType == DiscoveryConstants.NOTIFICATION_TYPE_HELLO) {
            serviceClient.getOptions().setAction(DiscoveryConstants.WS_DISCOVERY_HELLO_ACTION);
        } else if (notificationType == DiscoveryConstants.NOTIFICATION_TYPE_BYE) {
            serviceClient.getOptions().setAction(DiscoveryConstants.WS_DISCOVERY_BYE_ACTION);
        } else {
            serviceClient.getOptions().setAction(DiscoveryConstants.WS_DISCOVERY_PROBE_ACTION);
        }

        serviceClient.engageModule("addressing");

        return serviceClient;
    }
}
