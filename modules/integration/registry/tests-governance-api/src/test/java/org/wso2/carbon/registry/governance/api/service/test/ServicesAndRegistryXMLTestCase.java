package org.wso2.carbon.registry.governance.api.service.test;

import org.apache.xerces.dom.DeferredElementImpl;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.api.clients.server.admin.ServerAdminClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.ServerGroupManager;
import org.wso2.carbon.automation.core.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.core.annotations.SetEnvironment;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkFactory;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkProperties;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.beans.xsd.VersionPath;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.rmi.RemoteException;

public class ServicesAndRegistryXMLTestCase {
    int userId = ProductConstant.SUPER_ADMIN_USER_ID;
    ServiceManager serviceManager;
    private Service service, serviceWithVersion;
    private ServerAdminClient serverAdminClient;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private ManageEnvironment environment;
    private EnvironmentBuilder builder;
    private final static String ROOT = "/_system/governance";
    private final static String NEW_PATH = "/trunk/services/test/";
    private final static boolean NEW_VALUE = true;
    private final static String NEW_VERSION = "5.0.0-SNAPSHOT";
    private String OLD_VERSION = "1.0.0-SNAPSHOT";
    private String OLD_PATH = "";
    private boolean OLD_VALUE = false;

    @BeforeClass(alwaysRun = true)
    public void initialize() throws Exception {

        builder = new EnvironmentBuilder().greg(userId);
        environment = builder.build();

        serverAdminClient =
                new ServerAdminClient(environment.getGreg().getBackEndUrl(),
                                      environment.getGreg().getSessionCookie());

        editRegistryServicePath(NEW_PATH);
        editRegistryAutoVersioning(NEW_VALUE);
        editRegistryDefaultVersion(NEW_VERSION);
        FrameworkProperties frameworkProperties =
                FrameworkFactory.getFrameworkProperties(ProductConstant.GREG_SERVER_NAME);

        ServerGroupManager.getServerUtils().restartGracefully(serverAdminClient, frameworkProperties);

        builder = new EnvironmentBuilder().greg(userId);
        environment = builder.build();

        resourceAdminServiceClient =
                new ResourceAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                               environment.getGreg().getSessionCookie());

        serverAdminClient = new ServerAdminClient(environment.getGreg().getBackEndUrl(),
                                                  environment.getGreg().getSessionCookie());
    }

    /*
     * Add a service without the Version property so that the
     * service is saved with defaultServiceVersion
     * 1.0.0-SNAPSHOT
     */
    @Test(groups = {"wso2.greg"}, description = "service without the defaultServiceVersion property")
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_all})
    public void testAddServiceWithoutVersion() throws Exception {
        WSRegistryServiceClient wsRegistry =
                new RegistryProviderUtil().getWSRegistry(userId,
                                                         ProductConstant.GREG_SERVER_NAME);
        Registry governance = new RegistryProviderUtil().getGovernanceRegistry(wsRegistry, userId);
        serviceManager = new ServiceManager(governance);
        service =
                serviceManager.newService(new QName("http://bang.boom.com/mnm/beep/test", "RegistryXMLService"));
        serviceManager.addService(service);
        String serviceId = service.getId();
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
        service = serviceManager.getService(serviceId);

        Assert.assertEquals(service.getAttribute("overview_version"), "5.0.0-SNAPSHOT", "overview_version should be 5.0.0-SNAPSHOT");
    }

    /*
     * Change the default location where you want to add services and verify
     * whether the service gets created at the correct location
     */
    @Test(groups = {"wso2.greg"}, description = "service location change",
          dependsOnMethods = "testAddServiceWithoutVersion")
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_all})
    public void testDefaultLocationChange() throws GovernanceException {
        Assert.assertEquals(service.getPath(),
                            "/trunk/services/test/com/boom/bang/mnm/beep/test/RegistryXMLService");
    }

    /*
     * If default versioning is set to true in registry.xml, verify whether new
     * versions are created whenever a change is done to the service
     * <p/>
     * Verify whether versioning (automatic metadata versioning) works for
     * services (whether correct version contains correct information)
     *
     *
     */
    @Test(groups = {"wso2.greg"}, description = "versioning", dependsOnMethods = "testDefaultLocationChange")
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_all})
    public void testAutomaticVersioning() throws GovernanceException, RemoteException,
                                                 ResourceAdminServiceExceptionException {
        String destinationPath = ROOT + service.getPath();
        VersionPath[] versionPaths;

        service.addAttribute("test-att", "test-val");
        serviceManager.updateService(service);
        versionPaths = resourceAdminServiceClient.getVersionPaths(destinationPath);
        Assert.assertFalse(resourceAdminServiceClient.getTextContent(versionPaths[0].getCompleteVersionPath())
                                   .contains("test-att"), "versions contain wrong information");
        service.addAttribute("test-att2", "test-val2");
        serviceManager.updateService(service);
        versionPaths = resourceAdminServiceClient.getVersionPaths(destinationPath);
        Assert.assertTrue(resourceAdminServiceClient.getTextContent(versionPaths[0].getCompleteVersionPath())
                                  .contains("test-att"), "versions contain wrong information");
        Assert.assertFalse(resourceAdminServiceClient.getTextContent(versionPaths[0].getCompleteVersionPath())
                                   .contains("test-att2"), "versions contain wrong information");
    }

    /*
     * Add a service with the Version property so that the
     * service is saved with new version not the defaultServiceVersion
     * 1.0.0-SNAPSHOT
     */
    @Test(groups = {"wso2.greg"}, description = "service without the defaultServiceVersion property",
          dependsOnMethods = "testAddServiceWithoutVersion")
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_all})
    public void testAddServiceWithVersion() throws Exception {

        serviceWithVersion =
                serviceManager.newService(new QName(
                        "http://service.with.version/mnm/",
                        "serviceWithVersion"));
        serviceWithVersion.addAttribute("overview_version", "3.0.0");
        serviceManager.addService(serviceWithVersion);
        serviceWithVersion = serviceManager.getService(serviceWithVersion.getId());
        Assert.assertEquals(serviceWithVersion.getAttribute("overview_version"), "3.0.0", "overview_version should be 3.0.0");
    }

    public void editRegistryServicePath(String pathToChange) throws java.lang.Exception {

        String configPath =
                CarbonUtils.getCarbonHome() + File.separator + "repository" +
                File.separator + "conf" + File.separator + "registry.xml";

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(configPath);
        Element rootElement = document.getDocumentElement();
        Node nd = rootElement.getElementsByTagName("staticConfiguration").item(0);
        NodeList list = nd.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {

            Node node = list.item(i);

            if ("servicePath".equals(node.getNodeName())) {
                OLD_PATH = node.getTextContent();
                node.setTextContent(pathToChange);
            }
        }

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(new File(configPath));
        transformer.transform(source, result);

    }

    public void editRegistryDefaultVersion(String versionValue) throws java.lang.Exception {

        String configPath =
                CarbonUtils.getCarbonHome() + File.separator + "repository" +
                File.separator + "conf" + File.separator + "registry.xml";

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(configPath);
        Element rootElement = document.getDocumentElement();
        int length = rootElement.getElementsByTagName("handler").getLength();
        Node nd = null;
        for (int i = 0; i < length; i++) {
            nd = rootElement.getElementsByTagName("handler").item(i);
            if (((DeferredElementImpl) nd).getAttribute("class").contains("org.wso2.carbon.registry.extensions.handlers.ServiceMediaTypeHandler")) {
                break;
            }
        }
        assert nd != null;
        NodeList list = nd.getChildNodes();
        for (int j = 0; j < list.getLength(); j++) {

            Node node = list.item(j);

            if ("property".equals(node.getNodeName()) && ((DeferredElementImpl) node).getAttribute("name").contains("defaultServiceVersion")) {

                OLD_VERSION = node.getTextContent();
                node.setTextContent(versionValue);
            }
        }

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(new File(configPath));
        transformer.transform(source, result);

    }

    public void editRegistryAutoVersioning(boolean value) throws java.lang.Exception {

        String configPath =
                CarbonUtils.getCarbonHome() + File.separator + "repository" +
                File.separator + "conf" + File.separator + "registry.xml";

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(configPath);
        Element rootElement = document.getDocumentElement();
        Node nd = rootElement.getElementsByTagName("versionResourcesOnChange").item(0);
        OLD_VALUE = Boolean.valueOf(nd.getTextContent());
        nd.setTextContent(Boolean.toString(value).toLowerCase());


        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(new File(configPath));
        transformer.transform(source, result);

    }

    @AfterClass(alwaysRun = true)
    public void endGame() throws Exception {

        editRegistryServicePath(OLD_PATH);
        editRegistryAutoVersioning(OLD_VALUE);
        editRegistryDefaultVersion(OLD_VERSION);

        serviceManager.removeService(service.getId());
        serviceManager.removeService(serviceWithVersion.getId());

        FrameworkProperties frameworkProperties =
                FrameworkFactory.getFrameworkProperties(ProductConstant.GREG_SERVER_NAME);

        ServerGroupManager.getServerUtils().restartGracefully(serverAdminClient, frameworkProperties);

        environment = builder.build();

        serviceManager = null;
        service = null;
        serviceWithVersion = null;
        serverAdminClient = null;
        resourceAdminServiceClient = null;
        environment = null;
        builder = null;
    }
}
