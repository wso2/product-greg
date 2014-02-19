package org.wso2.carbon.registry.ws.client.test.general.old;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.registry.HandlerManagementServiceClient;
import org.wso2.carbon.automation.api.clients.server.admin.ServerAdminClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.ServerGroupManager;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.core.utils.fileutils.FileManager;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkFactory;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkProperties;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.extensions.stub.ExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.FileManipulator;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;

import static org.testng.Assert.*;

/**
 * A test case which tests backwardAssociation handler add test
 */

public class BackwardAssociationHandlerTestCase {

    private static final Log log = LogFactory.getLog(BackwardAssociationHandlerTestCase.class);
    public static final String HANDLER_CLASS = "org.wso2.carbon.registry.backward.association.handler.BackwardAssociationHandler";

    public static final String SOURCE_PATH = "/_system/associationTest/source";
    public static final String TARGET_PATH = "/_system/associationTest/target";
    public static final String CALLS = "calls";
    public static final String CALLED_BY = "calledBy";


    private int userId = 1;
    UserInfo userInfo = UserListCsvReader.getUserInfo(userId);
    private HandlerManagementServiceClient handlerManagementServiceClient;
    private String newHandlerPath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION
            + "artifacts" + File.separator + "GREG" + File.separator
            + "handler" + File.separator + "backwardAssociationHandler.xml";
    private WSRegistryServiceClient registry;
    private EnvironmentBuilder builder;
    private ManageEnvironment environment;
    private ServerAdminClient serverAdminClient;
    private RegistryProviderUtil registryProviderUtil;
    private FrameworkProperties frameworkProperties;

    @BeforeClass(groups = {"wso2.greg"})
    public void init() throws Exception {
        log.info("Initializing Add Handler Test");
        log.debug("Add Handler Test Initialised");
        builder = new EnvironmentBuilder().greg(userId);
        environment = builder.build();

        handlerManagementServiceClient = new HandlerManagementServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                userInfo.getUserName(), userInfo.getPassword());
        serverAdminClient = new ServerAdminClient(environment.getGreg().getProductVariables().getBackendUrl(),
                userInfo.getUserName(), userInfo.getPassword());
        copyBackwardAssociationHandler();
        restartServer();
        builder = new EnvironmentBuilder().greg(userId);
        environment = builder.build();
        registryProviderUtil = new RegistryProviderUtil();
        registry = registryProviderUtil.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);
    }


    @Test(groups = {"wso2.greg"}, description = "Add new Handler")
    public void addNewHandler() throws IOException, ExceptionException,
                                       org.wso2.carbon.registry.handler.stub.ExceptionException {


        assertTrue(handlerManagementServiceClient.createHandler(FileManager.readFile(newHandlerPath)));
        String handlerConfig = handlerManagementServiceClient.getHandlerConfiguration(HANDLER_CLASS);
        assertNotNull(handlerConfig, "Handler config cannot be null - " + HANDLER_CLASS);
        initResources();

    }


    @Test(groups = {"wso2.greg"}, description = "Test backward association handler getAllAssociations method using " +
            "source path", dependsOnMethods = "addNewHandler")
    public void testGetAllAssociationSourcePath() throws RegistryException {

//        Retrieving all the associations of the source resource
        Association[] allAssociations = registry.getAllAssociations(SOURCE_PATH);


//        Testing whether we got the correct number of associations
        assertEquals(allAssociations.length, 2, "Invalid number of associations returned");


        for (Association association : allAssociations) {
            if (association.getAssociationType().equals(CALLS)) {
                if (!association.getSourcePath().equals(SOURCE_PATH)
                        && !association.getDestinationPath().equals(TARGET_PATH)) {
                    fail("Added association is missing");
                }
            } else if (association.getAssociationType().equals(CALLED_BY)) {
                if (!association.getSourcePath().equals(TARGET_PATH)
                        && !association.getDestinationPath().equals(SOURCE_PATH)) {
                    fail("Backward association is missing");
                }
            } else {
                fail("Invalid association found");
            }
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Test backward association handler getAllAssociations method using " +
            "target path", dependsOnMethods = "testGetAllAssociationSourcePath")
    public void testGetAllAssociationTargetPath() throws RegistryException {

//        Retrieving all the associations of the source resource
        Association[] allAssociations = registry.getAllAssociations(TARGET_PATH);

//        Testing whether we got the correct number of associations
        assertEquals(allAssociations.length, 2, "Invalid number of associations returned");


        for (Association association : allAssociations) {
            if (association.getAssociationType().equals(CALLS)) {
                if (!association.getSourcePath().equals(SOURCE_PATH)
                        && !association.getDestinationPath().equals(TARGET_PATH)) {
                    fail("Added association is missing");
                }
            } else if (association.getAssociationType().equals(CALLED_BY)) {
                if (!association.getSourcePath().equals(TARGET_PATH)
                        && !association.getDestinationPath().equals(SOURCE_PATH)) {
                    fail("Backward association is missing");
                }
            } else {
                fail("Invalid association found");
            }
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Test backward association handler getAssociations method using " +
            "source path", dependsOnMethods = "testGetAllAssociationTargetPath")
    public void testGetAssociationSourcePath() throws RegistryException {
        Association[] associations = registry.getAssociations(SOURCE_PATH, CALLS);

//        Testing whether we got the correct number of associations
        assertEquals(associations.length, 1, "Invalid number of association returned");

//        Testing whether the association is correct
        if (!(associations[0].getSourcePath().equals(SOURCE_PATH)
                && associations[0].getDestinationPath().equals(TARGET_PATH)
                && associations[0].getAssociationType().equals(CALLS))) {
            fail("Incorrect association returned");
        }

    }

    @Test(groups = {"wso2.greg"}, description = "Test backward association handler getAssociations method using " +
            "target path", dependsOnMethods = "testGetAssociationSourcePath")
    public void testGetAssociationTargetPath() throws RegistryException {
        Association[] associations = registry.getAssociations(TARGET_PATH, CALLED_BY);

//        Testing whether we got the correct number of associations
        assertEquals(associations.length, 1, "Invalid number of association returned");

//        Testing whether the association is correct
        if (!(associations[0].getSourcePath().equals(TARGET_PATH)
                && associations[0].getDestinationPath().equals(SOURCE_PATH)
                && associations[0].getAssociationType().equals(CALLED_BY))) {
            fail("Incorrect association returned");
        }

    }

    @AfterClass
    public void removeArtifacts() throws RegistryException, ExceptionException, RemoteException,
                                         org.wso2.carbon.registry.handler.stub.ExceptionException {

        registry.delete(SOURCE_PATH);
        registry.delete(TARGET_PATH);
        registry.delete("/_system/associationTest");
        handlerManagementServiceClient.deleteHandler(HANDLER_CLASS);
        registry = null;
        serverAdminClient = null;
        environment = null;
        builder = null;
        handlerManagementServiceClient = null;
    }


    private void initResources() {
        try {
//        Adding the source resource
            Resource sourceResource = registry.newResource();
            sourceResource.setContent("source".getBytes());
            registry.put(SOURCE_PATH, sourceResource);

//        Adding the target resource
            Resource targetResource = registry.newResource();
            targetResource.setContent("target".getBytes());
            registry.put(TARGET_PATH, targetResource);

//        Adding the association
            registry.addAssociation(SOURCE_PATH, TARGET_PATH, CALLS);

        } catch (RegistryException e) {
            fail("Failed to initialize resources", e);
        }
    }

    private void copyBackwardAssociationHandler() throws IOException {
        String handlerSampleBundle = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION
                + "artifacts" + File.separator + "GREG" + File.separator
                + "handler" + File.separator + "backwardAssociationHandler.jar";


        File srcFile = new File(handlerSampleBundle);
        assert srcFile.exists() : srcFile.getAbsolutePath() + " does not exist";

        String deploymentPath = CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator
                + "components" + File.separator + "dropins";

        File depFile = new File(deploymentPath);
        if (!depFile.exists() && !depFile.mkdir()) {
            throw new IOException("Error while creating the deployment folder : " + deploymentPath);
        }
        File dstFile = new File(depFile.getAbsolutePath() + File.separator +
                "backwardAssociationHandler.jar");
        log.info("Copying " + srcFile.getAbsolutePath() + " => " + dstFile.getAbsolutePath());
        FileManipulator.copyFile(srcFile, dstFile);
    }

    private void restartServer() throws Exception, RemoteException, InterruptedException {
        frameworkProperties =
                FrameworkFactory.getFrameworkProperties(ProductConstant.GREG_SERVER_NAME);
        ServerGroupManager.getServerUtils().restartGracefully(serverAdminClient, frameworkProperties);
    }


}
