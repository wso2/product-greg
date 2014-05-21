package org.wso2.carbon.registry.governance.api.ui.test;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.list.stub.ListMetadataServiceRegistryExceptionException;
import org.wso2.carbon.governance.services.stub.AddServicesServiceRegistryExceptionException;
import org.wso2.carbon.integration.common.utils.FileManager;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.clients.GenericServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;

public class ConfigureServiceUITestCase extends GREGIntegrationBaseTest {

    private Registry governance;
    private ServiceManager serviceManager;
    private GenericServiceClient governanceServiceClient;
    private final static String WSDL_URL =
            "https://svn.wso2.org/repos/wso2/carbon/platform/trunk/products/greg/modules/integration/registry/tests-new" +
            "/src/test/resources/artifacts/GREG/wsdl/info.wsdl";
    private Service serviceForUITesting1, serviceForUITesting2, serviceForUITesting3;

    @BeforeClass(alwaysRun = true)
    public void initialize() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        WSRegistryServiceClient wsRegistry =
                new RegistryProviderUtil().getWSRegistry(automationContext);
        governance = new RegistryProviderUtil().getGovernanceRegistry(wsRegistry, automationContext);
        new ServiceManager(governance);
        governanceServiceClient =
                new GenericServiceClient(getBackendURL(), getSessionCookie());

    }

    /**
     * "Add new element types to the existing service UI (and save so that they
     * get added to new services). Try the following elements.
     * - table (with 2 or more columns)
     * - subheading (with two or more columns)
     * - field elements
     * a) text
     * b) options
     * c) text-area
     * d) option-text
     * e) check-box
     * - url (with text or option-text)
     * - required=""true""
     * - read-only=""true""
     * - maxoccurs=unbounded (with option-text)
     *
     * @throws XMLStreamException
     * @throws IOException
     * @throws AddServicesServiceRegistryExceptionException
     *
     * @throws ListMetadataServiceRegistryExceptionException
     *
     * @throws ResourceAdminServiceExceptionException
     *
     */
    @Test(groups = "wso2.greg", description = "Create a service")
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    public void testCreateService() throws java.lang.Exception {

        String servicePath =
                FrameworkPathUtil.getSystemResourceLocation() + "artifacts" +
                File.separator + "GREG" + File.separator + "services" +
                File.separator + "rxtConfForUITesting1.xml";
        String serviceContent = FileManager.readFile(servicePath);
        governanceServiceClient.saveConfiguration(serviceContent, "/_system/governance/repository/components/org.wso2.carbon.governance/types/service.rxt");

        Assert.assertEquals(governanceServiceClient.getConfiguration("service"), serviceContent, "Service Configuration not saved");
    }

    /**
     * "Inside a single table element use the same name more than one field of the same type"
     *
     * @throws IOException
     */
    @Test(groups = "wso2.greg", description = "repeating the same field inside a table")
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    public void testElementRepeat() throws java.lang.Exception {
        String servicePath =
                FrameworkPathUtil.getSystemResourceLocation() + "artifacts" +
                File.separator + "GREG" + File.separator + "services" +
                File.separator + "rxtConfForUITesting2.xml";
        String serviceContent = FileManager.readFile(servicePath);
        boolean success = governanceServiceClient.saveConfiguration(serviceContent, "/_system/governance/repository/components/org.wso2.carbon.governance/types/service.rxt");
        Assert.assertTrue(success, "Service Configuration not saved");
    }

    /**
     * When two sub headers are specified set maxoccurs attribute to unbounded.
     *
     * @throws IOException
     */
    @Test(groups = "wso2.greg", description = "repeting the same field inside a table")
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    public void testSubHeaders() throws java.lang.Exception {
        String servicePath =
                FrameworkPathUtil.getSystemResourceLocation() + "artifacts" +
                File.separator + "GREG" + File.separator + "services" +
                File.separator + "rxtConfForUITesting3.xml";
        String serviceContent = FileManager.readFile(servicePath);
        boolean success = governanceServiceClient.saveConfiguration(serviceContent, "/_system/governance/repository/components/org.wso2.carbon.governance/types/service.rxt");
        Assert.assertFalse(success, "Service Configuration with wrong config should not be saved");
    }

    /**
     * Try to save the Service UI with syntax errors/invalid configurations
     *
     * @throws IOException
     */
    @Test(groups = "wso2.greg", description = "repeting the same field inside a table")
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    public void testSyntaxErrors() throws java.lang.Exception {
        String servicePath =
                FrameworkPathUtil.getSystemResourceLocation() + "artifacts" +
                File.separator + "GREG" + File.separator + "services" +
                File.separator + "rxtConfForUITesting4.xml";
        String serviceContent = FileManager.readFile(servicePath);
        boolean success = governanceServiceClient.saveConfiguration(serviceContent, "/_system/governance/repository/components/org.wso2.carbon.governance/types/service.rxt");
        Assert.assertFalse(success, "Service Configuration with wrong config should not be saved");

    }

    @Test(groups = "wso2.greg", description = "repeting the same field inside a table")
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    public void testListServicesAfterRestart() throws java.lang.Exception {
        int servicesBefore = serviceManager.getAllServices().length;
        String servicePath =
                FrameworkPathUtil.getSystemResourceLocation() + "artifacts" +
                File.separator + "GREG" + File.separator + "services" +
                File.separator + "rxtConfForUITesting1.xml";
        String serviceContent = FileManager.readFile(servicePath);
        boolean success = governanceServiceClient.saveConfiguration(serviceContent, "/_system/governance/repository/components/org.wso2.carbon.governance/types/service.rxt");
        Assert.assertTrue(success, "Service Configuration not saved");

        serviceForUITesting1 =
                serviceManager.newService(new QName(
                        "http://service.for.uitesting1/mnm/",
                        "serviceForUITesting1"));
        serviceForUITesting1.addAttribute("overview_version", "3.0.0");
        serviceForUITesting1.addAttribute("overview_description", "Test");
        serviceForUITesting1.addAttribute("interface_wsdlUrl", WSDL_URL);
        serviceForUITesting1.addAttribute("docLinks_documentType", "test");
        serviceForUITesting1.addAttribute("interface_messageFormats", "SOAP 1.2");
        serviceForUITesting1.addAttribute("interface_messageExchangePatterns", "Request Response");
        serviceForUITesting1.addAttribute("security_authenticationPlatform ", "XTS-WS TRUST");
        serviceForUITesting1.addAttribute("security_authenticationMechanism", "InfoCard");
        serviceForUITesting1.addAttribute("security_messageIntegrity", "WS-Security");
        serviceForUITesting1.addAttribute("security_messageEncryption", "WS-Security");
        serviceManager.addService(serviceForUITesting1);

        serviceForUITesting2 =
                serviceManager.newService(new QName(
                        "http://service.for.uitesting2/mnm/",
                        "serviceForUITesting2"));
        serviceForUITesting2.addAttribute("overview_version", "4.0.0");
        serviceForUITesting2.addAttribute("overview_description", "Test");
        serviceForUITesting2.addAttribute("interface_wsdlUrl", WSDL_URL);
        serviceForUITesting2.addAttribute("docLinks_documentType", "test");
        serviceForUITesting2.addAttribute("interface_messageFormats", "SOAP 1.2");
        serviceForUITesting2.addAttribute("interface_messageExchangePatterns", "Request Response");
        serviceForUITesting2.addAttribute("security_authenticationPlatform ", "XTS-WS TRUST");
        serviceForUITesting2.addAttribute("security_authenticationMechanism", "InfoCard");
        serviceForUITesting2.addAttribute("security_messageIntegrity", "WS-Security");
        serviceForUITesting2.addAttribute("security_messageEncryption", "WS-Security");
        serviceManager.addService(serviceForUITesting2);

        serviceForUITesting3 =
                serviceManager.newService(new QName(
                        "http://service.for.uitesting3/mnm/",
                        "serviceForUITesting3"));
        serviceForUITesting3.addAttribute("overview_version", "5.0.0");
        serviceForUITesting3.addAttribute("overview_description", "Test");
        serviceForUITesting3.addAttribute("interface_wsdlUrl", WSDL_URL);
        serviceForUITesting3.addAttribute("docLinks_documentType", "test");
        serviceForUITesting3.addAttribute("interface_messageFormats", "SOAP 1.2");
        serviceForUITesting3.addAttribute("interface_messageExchangePatterns", "Request Response");
        serviceForUITesting3.addAttribute("security_authenticationPlatform ", "XTS-WS TRUST");
        serviceForUITesting3.addAttribute("security_authenticationMechanism", "InfoCard");
        serviceForUITesting3.addAttribute("security_messageIntegrity", "WS-Security");
        serviceForUITesting3.addAttribute("security_messageEncryption", "WS-Security");
        serviceManager.addService(serviceForUITesting3);

        new ServerConfigurationManager(automationContext).restartGracefully();

        WSRegistryServiceClient wsRegistry =
                new RegistryProviderUtil().getWSRegistry(automationContext);
        governance = new RegistryProviderUtil().getGovernanceRegistry(wsRegistry, automationContext);
        serviceManager = new ServiceManager(governance);

        //initialize the environment again after server restart
        governanceServiceClient =
                new GenericServiceClient(getBackendURL(),
                                         getSessionCookie());

        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
        int servicesAfter = serviceManager.getAllServices().length;
        Assert.assertEquals(servicesAfter - servicesBefore, 3, "Number of services have changed after restart");

    }

    @AfterClass(alwaysRun = true)
    public void endGame() throws GovernanceException {
        serviceManager.removeService(serviceForUITesting3.getId());
        serviceManager.removeService(serviceForUITesting2.getId());
        serviceManager.removeService(serviceForUITesting1.getId());

        governanceServiceClient = null;
        serviceManager = null;
        serviceForUITesting1 = null;
        serviceForUITesting2 = null;
        serviceForUITesting3 = null;
    }

}
