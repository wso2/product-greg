package org.wso2.carbon.registry.ws.client.test.general.old;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.integration.common.utils.FileManager;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.FileManipulator;
import org.wso2.greg.integration.common.clients.HandlerManagementServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import java.io.File;
import java.io.IOException;

import static org.testng.Assert.*;

/**
 * A test case which tests backwardAssociation handler add test
 */

public class BackwardAssociationHandlerTestCase extends GREGIntegrationBaseTest {

    private static final Log log = LogFactory.getLog(BackwardAssociationHandlerTestCase.class);
    public static final String HANDLER_CLASS = "org.wso2.carbon.registry.backward.association.handler.BackwardAssociationHandler";

    public static final String SOURCE_PATH = "/_system/associationTest/source";
    public static final String TARGET_PATH = "/_system/associationTest/target";
    public static final String CALLS = "calls";
    public static final String CALLED_BY = "calledBy";

    private HandlerManagementServiceClient handlerManagementServiceClient;
    private String newHandlerPath;
    private WSRegistryServiceClient registry;
    private RegistryProviderUtil registryProviderUtil;
    private String sessionID;

    @BeforeClass(groups = {"wso2.greg"})
    public void init() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        newHandlerPath = getTestArtifactLocation() + "artifacts" + File.separator + "GREG" + File.separator + "handler" +
                File.separator + "backwardAssociationHandler.xml";
        copyBackwardAssociationHandler();
        restartServer();
        registryProviderUtil = new RegistryProviderUtil();
        registry = registryProviderUtil.getWSRegistry(automationContext);
        sessionID = getSessionCookie();
        handlerManagementServiceClient = new HandlerManagementServiceClient(backendURL, sessionID);
    }

    @Test(groups = {"wso2.greg"}, description = "Add new Handler")
    public void addNewHandler() throws Exception {
        assertTrue(handlerManagementServiceClient.createHandler(FileManager.readFile(newHandlerPath)));
        String handlerConfig = handlerManagementServiceClient.getHandlerConfiguration(HANDLER_CLASS);
        assertNotNull(handlerConfig, "Handler config cannot be null - " + HANDLER_CLASS);
        initResources();
    }

    @Test(groups = {"wso2.greg"}, description = "Test backward association handler getAllAssociations method using "
            + "source path", dependsOnMethods = "addNewHandler")
    public void testGetAllAssociationSourcePath() throws RegistryException {
        //        Retrieving all the associations of the source resource
        Association[] allAssociations = registry.getAllAssociations(SOURCE_PATH);
        //        Testing whether we got the correct number of associations
        assertEquals(allAssociations.length, 2, "Invalid number of associations returned");
        for(Association association : allAssociations) {
            if(association.getAssociationType().equals(CALLS)) {
                if(!association.getSourcePath().equals(SOURCE_PATH) && !association.getDestinationPath().equals(TARGET_PATH)) {
                    fail("Added association is missing");
                }
            } else if(association.getAssociationType().equals(CALLED_BY)) {
                if(!association.getSourcePath().equals(TARGET_PATH) && !association.getDestinationPath().equals(SOURCE_PATH)) {
                    fail("Backward association is missing");
                }
            } else {
                fail("Invalid association found");
            }
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Test backward association handler getAllAssociations method using "
            + "target path", dependsOnMethods = "testGetAllAssociationSourcePath")
    public void testGetAllAssociationTargetPath() throws RegistryException {
        //        Retrieving all the associations of the source resource
        Association[] allAssociations = registry.getAllAssociations(TARGET_PATH);
        //        Testing whether we got the correct number of associations
        assertEquals(allAssociations.length, 2, "Invalid number of associations returned");
        for(Association association : allAssociations) {
            if(association.getAssociationType().equals(CALLS)) {
                if(!association.getSourcePath().equals(SOURCE_PATH) && !association.getDestinationPath().equals(TARGET_PATH)) {
                    fail("Added association is missing");
                }
            } else if(association.getAssociationType().equals(CALLED_BY)) {
                if(!association.getSourcePath().equals(TARGET_PATH) && !association.getDestinationPath().equals(SOURCE_PATH)) {
                    fail("Backward association is missing");
                }
            } else {
                fail("Invalid association found");
            }
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Test backward association handler getAssociations method using "
            + "source path", dependsOnMethods = "testGetAllAssociationTargetPath")
    public void testGetAssociationSourcePath() throws RegistryException {
        Association[] associations = registry.getAssociations(SOURCE_PATH, CALLS);
        //        Testing whether we got the correct number of associations
        assertEquals(associations.length, 1, "Invalid number of association returned");
        //        Testing whether the association is correct
        if(!(associations[0].getSourcePath().equals(SOURCE_PATH) && associations[0].
                getDestinationPath().equals(TARGET_PATH) && associations[0].getAssociationType().equals(CALLS))) {
            fail("Incorrect association returned");
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Test backward association handler getAssociations method using "
            + "target path", dependsOnMethods = "testGetAssociationSourcePath")
    public void testGetAssociationTargetPath() throws RegistryException {
        Association[] associations = registry.getAssociations(TARGET_PATH, CALLED_BY);
        //        Testing whether we got the correct number of associations
        assertEquals(associations.length, 1, "Invalid number of association returned");
        //        Testing whether the association is correct
        if(!(associations[0].getSourcePath().equals(TARGET_PATH) && associations[0].
                getDestinationPath().equals(SOURCE_PATH) && associations[0].getAssociationType().equals(CALLED_BY))) {
            fail("Incorrect association returned");
        }
    }

    @AfterClass
    public void removeArtifacts() throws Exception {
        registry.delete(SOURCE_PATH);
        registry.delete(TARGET_PATH);
        registry.delete("/_system/associationTest");
        handlerManagementServiceClient.deleteHandler(HANDLER_CLASS);
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
        } catch(RegistryException e) {
            fail("Failed to initialize resources", e);
        }
    }

    private void copyBackwardAssociationHandler() throws IOException {
        String handlerSampleBundle = getTestArtifactLocation() + "artifacts" +
                File.separator + "GREG" + File.separator +
                "handler" + File.separator + "backwardAssociationHandler.jar";

        File srcFile = new File(handlerSampleBundle);
        assert srcFile.exists() : srcFile.getAbsolutePath() + " does not exist";
        String deploymentPath = CarbonUtils.getCarbonHome() + File.separator + "repository" +
                File.separator + "components" + File.separator + "dropins";
        File depFile = new File(deploymentPath);
        if(!depFile.exists() && !depFile.mkdir()) {
            throw new IOException("Error while creating the deployment folder : " + deploymentPath);
        }
        File dstFile = new File(depFile.getAbsolutePath() + File.separator +
                "backwardAssociationHandler.jar");
        log.info("Copying " + srcFile.getAbsolutePath() + " => " + dstFile.getAbsolutePath());
        FileManipulator.copyFile(srcFile, dstFile);
    }

    private void restartServer() throws Exception {
        ServerConfigurationManager serverConfigurationManager = new ServerConfigurationManager
                (automationContext);
        serverConfigurationManager.restartGracefully();
    }
}
