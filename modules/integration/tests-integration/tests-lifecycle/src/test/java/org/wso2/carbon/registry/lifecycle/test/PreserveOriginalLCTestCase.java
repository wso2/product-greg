package org.wso2.carbon.registry.lifecycle.test;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.test.utils.common.FileManager;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.beans.xsd.LifecycleBean;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.services.ArrayOfString;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.registry.activities.stub.RegistryExceptionException;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.common.xsd.ResourceData;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.clients.LifeCycleAdminServiceClient;
import org.wso2.greg.integration.common.clients.LifeCycleManagementClient;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import javax.activation.DataHandler;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Promote services with "Preserve Original" true/false and see whether the
 * service gets preserved or not once promoted
 * <p/>
 * Enter values with typos: prserveOrginial="fal" and check whether Original is
 * preserved
 */
public class PreserveOriginalLCTestCase extends GREGIntegrationBaseTest {

    private int userId = 2;
    private String serviceStringTrunk = "/trunk/services/com/abb/1.0.0-SNAPSHOT/IntergalacticService6";
    private String serviceStringTest = "/branches/testing/services/com/abb/1.0.0/IntergalacticService6";
    private WSRegistryServiceClient wsRegistryServiceClient;
    private LifeCycleAdminServiceClient lifeCycleAdminServiceClient;
    private LifeCycleManagementClient lifeCycleManagementClient;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private static final String LC_NAME = "DiffEnvironmentLC3";
    private static final String ACTION_PROMOTE = "Promote";
    private LifecycleBean lifeCycle;
    private RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
    private String[] dependencyList;
    private static final String GOV_PATH = "/_system/governance";
    private final String absPath = GOV_PATH + serviceStringTrunk;

    /**
     * @throws RemoteException
     * @throws LoginAuthenticationExceptionException
     *
     * @throws RegistryException
     */
    @BeforeClass (alwaysRun = true)
    public void init () throws Exception {

        super.init(TestUserMode.SUPER_TENANT_USER);
        String sessionCookie = getSessionCookie();
        lifeCycleAdminServiceClient =
                new LifeCycleAdminServiceClient(backendURL, sessionCookie);
        lifeCycleManagementClient =
                new LifeCycleManagementClient(backendURL, sessionCookie);
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(backendURL, sessionCookie);
        wsRegistryServiceClient =
                registryProviderUtil.getWSRegistry(automationContext);

    }

    /**
     * @throws XMLStreamException
     * @throws IOException
     * @throws ResourceAdminServiceExceptionException
     *
     */
    @Test (groups = "wso2.greg", description = "Create a service")
    public void testCreateService () throws XMLStreamException, IOException,
            ResourceAdminServiceExceptionException {

        String servicePath =
                getTestArtifactLocation() + "artifacts" +
                        File.separator + "GREG" + File.separator + "services" +
                        File.separator + "intergalacticService6.metadata.xml";
        DataHandler dataHandler = new DataHandler(new URL("file:///" + servicePath));
        String mediaType = "application/vnd.wso2-service+xml";
        String description = "This is a test service";
        resourceAdminServiceClient.addResource("/_system/governance/service1", mediaType, description, dataHandler);
        ResourceData[] resourceDataArray = resourceAdminServiceClient.getResource(absPath);
        assertNotNull(resourceDataArray, "Service not found");
    }

    /**
     * @throws LifeCycleManagementServiceExceptionException
     *
     * @throws IOException
     * @throws InterruptedException
     */
    @Test (groups = "wso2.greg", description = "Create new life cycle", dependsOnMethods = "testCreateService")
    public void testCreateNewLifeCycle () throws LifeCycleManagementServiceExceptionException,
            IOException, InterruptedException {

        String resourcePath =
                getTestArtifactLocation() + "artifacts" +
                        File.separator + "GREG" + File.separator + "lifecycle" +
                        File.separator + "EnvironmentChangeLC3.xml";
        String lifeCycleContent = FileManager.readFile(resourcePath);
        lifeCycleManagementClient.addLifeCycle(lifeCycleContent);
        String[] lifeCycles = lifeCycleManagementClient.getLifecycleList();
        boolean lcStatus = false;
        for (String lc : lifeCycles) {
            if (lc.equalsIgnoreCase(LC_NAME)) {
                lcStatus = true;
            }
        }
        assertTrue(lcStatus, "LifeCycle not found");

    }

    /**
     * @throws RegistryException
     * @throws RemoteException
     * @throws CustomLifecyclesChecklistAdminServiceExceptionException
     *
     * @throws ResourceAdminServiceExceptionException
     *
     */
    @Test (groups = "wso2.greg", description = "Add lifecycle to a service", dependsOnMethods = "testCreateNewLifeCycle")
    public void testAddLcToService () throws RegistryException, RemoteException,
            CustomLifecyclesChecklistAdminServiceExceptionException,
            ResourceAdminServiceExceptionException {

        wsRegistryServiceClient.associateAspect("/_system/governance" + serviceStringTrunk, LC_NAME);
        lifeCycle =
                lifeCycleAdminServiceClient.getLifecycleBean("/_system/governance" +
                        serviceStringTrunk);
        Property[] properties = lifeCycle.getLifecycleProperties();
        boolean lcStatus = false;
        for (Property prop : properties) {
            if (prop.getKey().contains(LC_NAME)) {
                lcStatus = true;
            }
        }
        assertTrue(lcStatus, "LifeCycle not added to service");
    }

    /**
     * @throws RemoteException
     * @throws CustomLifecyclesChecklistAdminServiceExceptionException
     *
     * @throws LifeCycleManagementServiceExceptionException
     *
     * @throws RegistryExceptionException
     * @throws ResourceAdminServiceExceptionException
     *
     */
    @Test (groups = "wso2.greg", description = "Promote from Development to Testing, without preserving the original", dependsOnMethods = "testAddLcToService")
    public void testPromoteToTesting () throws RemoteException,
            CustomLifecyclesChecklistAdminServiceExceptionException,
            LifeCycleManagementServiceExceptionException,
            RegistryExceptionException,
            ResourceAdminServiceExceptionException {

        assertNotNull(resourceAdminServiceClient.getResource(
                "/_system/governance" + serviceStringTrunk), "New version not created");
        ArrayOfString[] parameters = new ArrayOfString[5];
        dependencyList =
                lifeCycleAdminServiceClient.getAllDependencies("/_system/governance" +
                        serviceStringTrunk);
        parameters[0] = new ArrayOfString();
        parameters[0].setArray(new String[]{dependencyList[0], "1.0.0"});
        parameters[1] = new ArrayOfString();
        parameters[1].setArray(new String[]{dependencyList[1], "1.0.0"});
        parameters[2] = new ArrayOfString();
        parameters[2].setArray(new String[]{dependencyList[2], "1.0.0"});
        //  parameters[3] = new ArrayOfString();
        //  parameters[3].setArray(new String[]{dependencyList[3], "1.0.0"});
        parameters[3] = new ArrayOfString();
        parameters[3].setArray(new String[]{"preserveOriginal", "false"});
        lifeCycleAdminServiceClient.invokeAspectWithParams("/_system/governance" + serviceStringTrunk,
                LC_NAME, ACTION_PROMOTE, null,
                parameters);
        try {
            resourceAdminServiceClient.getResource("/_system/governance" + serviceStringTrunk);
        } catch (Exception e) {
            assertTrue(e.getMessage().equals
                    ("Resource does not exist at path /_system/governance/trunk/services/com/abb/IntergalacticService6"), "Resource preserved");
        }
        assertNotNull(resourceAdminServiceClient.getResource("/_system/governance/branches"), "New version not created");
    }

    /**
     * @throws RemoteException
     * @throws ResourceAdminServiceExceptionException
     *
     * @throws RegistryException
     */
    @Test (groups = "wso2.greg", description = "Delete the added service", dependsOnMethods = "testPromoteToTesting")
    public void testDeleteService ()
            throws RemoteException, ResourceAdminServiceExceptionException, RegistryException {

        String servicePathToDelete = "/_system/governance/" + serviceStringTest;
        if (wsRegistryServiceClient.resourceExists(servicePathToDelete)) {
            resourceAdminServiceClient.deleteResource(servicePathToDelete);
        }
/*
        String schemaPathToDelete = "/_system/governance/branches/testing/schemas/org/bar/purchasing/1.0.0/purchasing.xsd";
        if (wsRegistryServiceClient.resourceExists(schemaPathToDelete)) {
            resourceAdminServiceClient.deleteResource(schemaPathToDelete);
        }
        schemaPathToDelete = "/_system/governance/trunk/schemas/org/bar/purchasing/purchasing.xsd";
        if (wsRegistryServiceClient.resourceExists(schemaPathToDelete)) {
            resourceAdminServiceClient.deleteResource(schemaPathToDelete);
        }
        String wsdlPathToDelete = "/_system/governance/branches/testing/wsdls/com/foo/1.0.0/IntergalacticService.wsdl";
        if (wsRegistryServiceClient.resourceExists(wsdlPathToDelete)) {
            resourceAdminServiceClient.deleteResource(wsdlPathToDelete);
        }*/
//        wsdlPathToDelete = "/_system/governance/trunk/wsdls/com/foo/IntergalacticService.wsdl";
        //      if (wsRegistryServiceClient.resourceExists(wsdlPathToDelete)) {
        //        resourceAdminServiceClient.deleteResource(wsdlPathToDelete);
        //  }
    }

    /**
     * @throws XMLStreamException
     * @throws IOException
     * @throws ResourceAdminServiceExceptionException
     *
     */
    @Test (groups = "wso2.greg", description = "Create a service again", dependsOnMethods = "testDeleteService")
    public void testCreateServiceAgain () throws XMLStreamException, IOException,
            ResourceAdminServiceExceptionException {

        String servicePath =
                getTestArtifactLocation() + "artifacts" +
                        File.separator + "GREG" + File.separator + "services" +
                        File.separator + "intergalacticService6.metadata.xml";
        DataHandler dataHandler = new DataHandler(new URL("file:///" + servicePath));
        String mediaType = "application/vnd.wso2-service+xml";
        String description = "This is a test service";
        resourceAdminServiceClient.addResource("/_system/governance/service1", mediaType, description, dataHandler);
        ResourceData[] resourceDataArray = resourceAdminServiceClient.getResource(absPath);
        assertNotNull(resourceDataArray, "Service not found");

    }

    /**
     * @throws RegistryException
     * @throws RemoteException
     * @throws CustomLifecyclesChecklistAdminServiceExceptionException
     *
     * @throws ResourceAdminServiceExceptionException
     *
     */
    @Test (groups = "wso2.greg", description = "Add lifecycle to a service", dependsOnMethods = "testCreateServiceAgain")
    public void testAddLcToServiceAgain () throws RegistryException, RemoteException,
            CustomLifecyclesChecklistAdminServiceExceptionException,
            ResourceAdminServiceExceptionException {

        wsRegistryServiceClient.associateAspect("/_system/governance" + serviceStringTrunk, LC_NAME);
        lifeCycle =
                lifeCycleAdminServiceClient.getLifecycleBean("/_system/governance" +
                        serviceStringTrunk);
        Property[] properties = lifeCycle.getLifecycleProperties();
        boolean lcStatus = false;
        for (Property prop : properties) {
            if (prop.getKey().contains(LC_NAME)) {
                lcStatus = true;
            }
        }
        assertTrue(lcStatus, "LifeCycle not added to service");
    }

    /**
     * @throws RemoteException
     * @throws CustomLifecyclesChecklistAdminServiceExceptionException
     *
     * @throws LifeCycleManagementServiceExceptionException
     *
     * @throws RegistryExceptionException
     * @throws ResourceAdminServiceExceptionException
     *
     */
    @Test (groups = "wso2.greg", description = "Promote from Development to Testing, preserving the original", dependsOnMethods = "testAddLcToServiceAgain")
    public void testPromoteToTestingAgain () throws RemoteException,
            CustomLifecyclesChecklistAdminServiceExceptionException,
            LifeCycleManagementServiceExceptionException,
            RegistryExceptionException,
            ResourceAdminServiceExceptionException {

        ArrayOfString[] parameters = new ArrayOfString[5];
        dependencyList =
                lifeCycleAdminServiceClient.getAllDependencies("/_system/governance" +
                        serviceStringTrunk);
        parameters[0] = new ArrayOfString();
        parameters[0].setArray(new String[]{dependencyList[0], "1.0.0"});
        parameters[1] = new ArrayOfString();
        parameters[1].setArray(new String[]{dependencyList[1], "1.0.0"});
        parameters[2] = new ArrayOfString();
        parameters[2].setArray(new String[]{dependencyList[2], "1.0.0"});
        //parameters[3] = new ArrayOfString();
        //parameters[3].setArray(new String[]{dependencyList[3], "1.0.0"});
        parameters[3] = new ArrayOfString();
        parameters[3].setArray(new String[]{"preserveOriginal", "true"});
        lifeCycleAdminServiceClient.invokeAspectWithParams("/_system/governance" + serviceStringTrunk,
                LC_NAME, ACTION_PROMOTE, null,
                parameters);
        assertNotNull(resourceAdminServiceClient.getResource(
                "/_system/governance/trunk/services/com/abb/IntergalacticService6"), "Original not preserved");
        assertNotNull(resourceAdminServiceClient.getResource(
                "/_system/governance/branches/testing/services/com/abb/1.0.0/IntergalacticService6"), "New version not created");
    }

    /**
     * @throws RemoteException
     * @throws ResourceAdminServiceExceptionException
     *
     * @throws RegistryException
     */
    @Test (groups = "wso2.greg", description = "Delete the added service", dependsOnMethods = "testPromoteToTestingAgain")
    public void testDeleteServiceAgain () throws RemoteException,
            ResourceAdminServiceExceptionException,
            RegistryException {

        String servicePathToDelete = "/_system/governance/" + serviceStringTrunk;
        if (wsRegistryServiceClient.resourceExists(servicePathToDelete)) {
            resourceAdminServiceClient.deleteResource(servicePathToDelete);
        }
        servicePathToDelete = "/_system/governance/" + serviceStringTest;
        if (wsRegistryServiceClient.resourceExists(servicePathToDelete)) {
            resourceAdminServiceClient.deleteResource(servicePathToDelete);
        }
     /*   String schemaPathToDelete = "/_system/governance/trunk/schemas/org/bar/purchasing/purchasing.xsd";
        if (wsRegistryServiceClient.resourceExists(schemaPathToDelete)) {
            resourceAdminServiceClient.deleteResource(schemaPathToDelete);
        }
        schemaPathToDelete = "/_system/governance/branches/testing/schemas/org/bar/purchasing/1.0.0/purchasing.xsd";
        if (wsRegistryServiceClient.resourceExists(schemaPathToDelete)) {
            resourceAdminServiceClient.deleteResource(schemaPathToDelete);
        }
        String wsdlPathToDelete = "/_system/governance/trunk/wsdls/com/foo/IntergalacticService.wsdl";
        if (wsRegistryServiceClient.resourceExists(wsdlPathToDelete)) {
            resourceAdminServiceClient.deleteResource(wsdlPathToDelete);
        }
        wsdlPathToDelete = "/_system/governance/branches/testing/wsdls/com/foo/1.0.0/IntergalacticService.wsdl";
        if (wsRegistryServiceClient.resourceExists(wsdlPathToDelete)) {
            resourceAdminServiceClient.deleteResource(wsdlPathToDelete);
        }*/
    }

    /**
     * @throws XMLStreamException
     * @throws IOException
     * @throws ResourceAdminServiceExceptionException
     *
     */
    @Test (groups = "wso2.greg", description = "Create a service again", dependsOnMethods = "testDeleteServiceAgain")
    public void testCreateNewService () throws XMLStreamException, IOException,
            ResourceAdminServiceExceptionException {

        String servicePath =
                getTestArtifactLocation() + "artifacts" +
                        File.separator + "GREG" + File.separator + "services" +
                        File.separator + "intergalacticService6.metadata.xml";
        DataHandler dataHandler = new DataHandler(new URL("file:///" + servicePath));
        String mediaType = "application/vnd.wso2-service+xml";
        String description = "This is a test service";
        resourceAdminServiceClient.addResource("/_system/governance/service1", mediaType, description, dataHandler);
        ResourceData[] resourceDataArray = resourceAdminServiceClient.getResource(absPath);
        assertNotNull(resourceDataArray, "Service not found");

    }

    /**
     * @throws RegistryException
     * @throws RemoteException
     * @throws CustomLifecyclesChecklistAdminServiceExceptionException
     *
     * @throws ResourceAdminServiceExceptionException
     *
     */
    @Test (groups = "wso2.greg", description = "Add lifecycle to a service again", dependsOnMethods = "testCreateNewService")
    public void testAddLcToNewService () throws RegistryException, RemoteException,
            CustomLifecyclesChecklistAdminServiceExceptionException,
            ResourceAdminServiceExceptionException {

        wsRegistryServiceClient.associateAspect("/_system/governance" + serviceStringTrunk, LC_NAME);
        lifeCycle =
                lifeCycleAdminServiceClient.getLifecycleBean("/_system/governance" +
                        serviceStringTrunk);
        Property[] properties = lifeCycle.getLifecycleProperties();
        boolean lcStatus = false;
        for (Property prop : properties) {
            if (prop.getKey().contains(LC_NAME)) {
                lcStatus = true;
            }
        }
        assertTrue(lcStatus, "LifeCycle not added to service");
    }

    /**
     * @throws RemoteException
     * @throws CustomLifecyclesChecklistAdminServiceExceptionException
     *
     * @throws LifeCycleManagementServiceExceptionException
     *
     * @throws RegistryExceptionException
     * @throws ResourceAdminServiceExceptionException
     *
     */
    @Test (groups = "wso2.greg", description = "Promote from Development to Testing to Production, with preserveOriginal value having typos in Testing Promote", dependsOnMethods = "testAddLcToNewService")
    public void testPromoteToProduction () throws RemoteException,
            CustomLifecyclesChecklistAdminServiceExceptionException,
            LifeCycleManagementServiceExceptionException,
            RegistryExceptionException,
            ResourceAdminServiceExceptionException {

        ArrayOfString[] parameters = new ArrayOfString[5];
        dependencyList =
                lifeCycleAdminServiceClient.getAllDependencies("/_system/governance" +
                        serviceStringTrunk);
        parameters[0] = new ArrayOfString();
        parameters[0].setArray(new String[]{dependencyList[0], "1.0.0"});
        parameters[1] = new ArrayOfString();
        parameters[1].setArray(new String[]{dependencyList[1], "1.0.0"});
        parameters[2] = new ArrayOfString();
        parameters[2].setArray(new String[]{dependencyList[2], "1.0.0"});
        //parameters[3] = new ArrayOfString();
        //parameters[3].setArray(new String[]{dependencyList[3], "1.0.0"});
        parameters[3] = new ArrayOfString();
        parameters[3].setArray(new String[]{"preserveOriginal", "false"});
        lifeCycleAdminServiceClient.invokeAspectWithParams("/_system/governance" + serviceStringTrunk,
                LC_NAME, ACTION_PROMOTE, null,
                parameters);
        ArrayOfString[] parameters2 = new ArrayOfString[1];
        parameters[0].setArray(new String[]{"preserveOriginal", "fal"});
        lifeCycleAdminServiceClient.invokeAspectWithParams("/_system/governance" + serviceStringTest,
                LC_NAME, ACTION_PROMOTE, null,
                parameters2);
        assertNotNull(resourceAdminServiceClient.getResource
                ("/_system/governance" + serviceStringTest), "Original does not exist");
        assertNotNull(resourceAdminServiceClient.getResource
                ("/_system/governance/branches/production/services/com/abb/1.0.0/IntergalacticService6"), "New version not created");

    }

    /**
     * @throws Exception
     */
    @AfterClass ()
    public void clear () throws Exception {

        String servicePathToDelete = "/_system/governance/" + serviceStringTest;
        if (wsRegistryServiceClient.resourceExists(servicePathToDelete)) {
            resourceAdminServiceClient.deleteResource(servicePathToDelete);
        }
        servicePathToDelete = "/_system/governance/branches/production/services/com/abb/1.0.0/IntergalacticService6";
        if (wsRegistryServiceClient.resourceExists(servicePathToDelete)) {
            resourceAdminServiceClient.deleteResource(servicePathToDelete);
        }
        if (wsRegistryServiceClient.resourceExists(absPath)) {
            resourceAdminServiceClient.deleteResource(absPath);
        }
/*        String schemaPathToDelete = "/_system/governance/branches/testing/schemas/org/bar/purchasing/1.0.0/purchasing.xsd";
        if (wsRegistryServiceClient.resourceExists(schemaPathToDelete)) {
            resourceAdminServiceClient.deleteResource(schemaPathToDelete);
        }
        schemaPathToDelete = "/_system/governance/trunk/schemas/org/bar/purchasing/purchasing.xsd";
        if (wsRegistryServiceClient.resourceExists(schemaPathToDelete)) {
            resourceAdminServiceClient.deleteResource(schemaPathToDelete);
        }
        String wsdlPathToDelete = "/_system/governance/branches/testing/wsdls/com/foo/1.0.0/IntergalacticService.wsdl";
        if (wsRegistryServiceClient.resourceExists(wsdlPathToDelete)) {
            resourceAdminServiceClient.deleteResource(wsdlPathToDelete);
        }
        wsdlPathToDelete = "/_system/governance/trunk/wsdls/com/foo/IntergalacticService.wsdl";
        if (wsRegistryServiceClient.resourceExists(wsdlPathToDelete)) {
            resourceAdminServiceClient.deleteResource(wsdlPathToDelete);
        }*/
        lifeCycleManagementClient.deleteLifeCycle(LC_NAME);
        wsRegistryServiceClient = null;
        lifeCycleAdminServiceClient = null;
        lifeCycleManagementClient = null;
        resourceAdminServiceClient = null;
    }
}
