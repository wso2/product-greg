package org.wso2.carbon.registry.jira.issues.test;


import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.osgi.framework.FrameworkUtil;
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
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public abstract class Registry1166TestCase extends GREGIntegrationBaseTest {

    private Registry governance;
    private String scopes;
    private String types;
    private String metadataVersion;

    @BeforeClass(alwaysRun = true)
    public void initialize() throws Exception{
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        WSRegistryServiceClient registry = registryProviderUtil.getWSRegistry(automationContext);
        governance = registryProviderUtil.getGovernanceRegistry(registry, automationContext);
        AuthenticatorClient authenticatorClient = new AuthenticatorClient(backendURL);

        String session = getSessionCookie();
    }

    @Test(groups = "wso2.greg", description = "DiscoveryProxy hello/probe")
    public void testDiscoveryProxy() throws Exception {

        String discoveryProxyEPR = backendURL + "DiscoveryProxy";
        //Hello
        ServiceClient serviceClient = initServiceClient(discoveryProxyEPR, DiscoveryConstants.NOTIFICATION_TYPE_HELLO);

        String uniqueID = UUID.randomUUID().toString();
        EndpointReference endpointReference = new EndpointReference(uniqueID);
        TargetService targetService = new TargetService(endpointReference);
        populateTargetService(targetService, DiscoveryConstants.NOTIFICATION_TYPE_HELLO);

        Notification notification = new Notification(DiscoveryConstants.NOTIFICATION_TYPE_HELLO, targetService);

        serviceClient.addStringHeader(
                new QName(DiscoveryConstants.DISCOVERY_HEADER_ELEMENT_NAMESPACE,
                          DiscoveryConstants.DISCOVERY_HEADER_SERVICE_NAME,
                          DiscoveryConstants.DISCOVERY_HEADER_ELEMENT_NAMESPACE_PREFIX),
                "DiscoveryProxyService");
        serviceClient.addStringHeader(new QName(DiscoveryConstants.DISCOVERY_HEADER_ELEMENT_NAMESPACE,
                                                DiscoveryConstants.DISCOVERY_HEADER_WSDL_URI,
                                                DiscoveryConstants.DISCOVERY_HEADER_ELEMENT_NAMESPACE_PREFIX), "wsdl_url");

        serviceClient.fireAndForget(DiscoveryOMUtils.toOM(notification,
                                                          OMAbstractFactory.getOMFactory()));
        Calendar startTime = Calendar.getInstance();
        boolean serviceDiscovered = false;
        while (Calendar.getInstance().getTimeInMillis() - startTime.getTimeInMillis() < 30000 | !serviceDiscovered) {
            ServiceManager serviceManager = new ServiceManager(governance);
            for (Service s : serviceManager.getAllServices()) {
                if (s.getAttribute("overview_namespace").equals("http://docs.oasis-open.org/ws-dd/ns/discovery/2009/01")) {
                    serviceDiscovered = true;
                    break;
                }
            }
            Thread.sleep(3000);
        }
        assertTrue(serviceDiscovered, "Service is not available in the Registry");
        serviceClient.cleanup();


        serviceClient = initServiceClient(discoveryProxyEPR, 2);
        OMElement element = serviceClient.sendReceive(AXIOMUtil.stringToOM("<wsd:Probe xmlns:wsd=\"http:" +
                                                                           "//docs.oasis-open.org/ws-dd/ns/discovery/2009/01\" />"));

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

    @AfterClass
    public void DeleteServices()
            throws LifeCycleManagementServiceExceptionException, RemoteException,
                   RegistryException {

        ServiceManager serviceManager = new ServiceManager(governance);
        Service[] services = serviceManager.getAllServices();
        for (Service s : services) {
            if (s.getAttribute("overview_namespace").equals("http://docs.oasis-open.org/ws-dd/ns/discovery/2009/01")) {
                serviceManager.removeService(s.getId());
            }
        }
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

        String axis2Repo = getTestArtifactLocation() + "client" + File.separator + "modules";

        String axis2Conf = getTestArtifactLocation() + File.separator + "axis2config" + File.separator
                           + "axis2_client.xml";

        TestFrameworkUtils.setKeyStoreProperties(automationContext);

        ConfigurationContext configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(axis2Repo, axis2Conf);


        ServiceClient serviceClient = new ServiceClient(configContext, null);
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
