package org.wso2.carbon.registry.ws.client.test.general;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static org.testng.Assert.*;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.handler.stub.ExceptionException;
import org.wso2.carbon.registry.handler.stub.HandlerManagementServiceStub;
import org.wso2.carbon.registry.handler.test.TestUtils;
import org.wso2.carbon.registry.relations.stub.AddAssociationRegistryExceptionException;
import org.wso2.carbon.registry.relations.stub.GetAssociationTreeRegistryExceptionException;
import org.wso2.carbon.registry.relations.stub.RelationAdminServiceStub;
import org.wso2.carbon.registry.relations.stub.beans.xsd.AssociationTreeBean;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceStub;

import java.io.*;
import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;

/**
 * A test case which tests backwardAssociation handler add test
 */

public class BackwardAssociationHandlerTestCase extends TestSetup {

    private static final Log log = LogFactory.getLog(BackwardAssociationHandlerTestCase.class);
    public static final String HANDLER_NAME = "backwardAssociationHandler.xml";
    public static final String HANDLER_CLASS = "org.wso2.carbon.registry.backward.association.handler.BackwardAssociationHandler";

    public static final String SOURCE_PATH = "/_system/associationTest/source";
    public static final String TARGET_PATH = "/_system/associationTest/target";
    public static final String CALLS = "calls";
    public static final String CALLED_BY = "calledBy";

    private String loggedInSessionCookie = "";
    private LoginLogoutUtil util = new LoginLogoutUtil();

    @BeforeClass(groups = {"wso2.greg"})
    public void init() {
        log.info("Initializing backward association handler Test");
        try {
            loggedInSessionCookie = util.login();
        } catch (Exception e) {
            fail("Failed to login",e);
        }

//        Initializing the WS registry
        super.init();

//        Adding the handler configuration to the registry
        initializeHandler();

//        Initialize the resources that is used in the test
        initResources();

    }

    private void initializeHandler() {
        try {
            HandlerManagementServiceStub handlerManagementServiceStub =
                    TestUtils.getHandlerManagementServiceStub(loggedInSessionCookie);

            String handlerResource = TestUtils.getTestResourcesDir(frameworkPath)
                    + File.separator + "handler" + File.separator + HANDLER_NAME;

            handlerManagementServiceStub.createHandler(fileReader(handlerResource));

//        Checking whether the handler got added correctly.
            String handlerConfig = handlerManagementServiceStub.getHandlerConfiguration(HANDLER_CLASS);
            assertNotNull(handlerConfig, "Handler config cannot be null - " + HANDLER_CLASS);
        } catch (RemoteException e) {
            fail("Failed to initialize backward association handler",e);
        } catch (ExceptionException e) {
            fail("Failed to initialize backward association handler",e);
        }
    }

    private void initResources(){
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
            registry.addAssociation(SOURCE_PATH, TARGET_PATH, "calls");
        } catch (RegistryException e) {
            fail("Failed to initialize resources",e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Test backward association handler getAllAssociations method using source path")
    public void testGetAllAssociationSourcePath() throws RegistryException {

//        Retrieving all the associations of the source resource
        Association[] allAssociations = registry.getAllAssociations(SOURCE_PATH);

//        Testing whether we got the correct number of associations
        assertEquals(allAssociations.length, 2,"Invalid number of associations returned");

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

    @Test(groups = {"wso2.greg"}, description = "Test backward association handler getAllAssociations method using target path")
    public void testGetAllAssociationTargetPath() throws RegistryException {

//        Retrieving all the associations of the source resource
        Association[] allAssociations = registry.getAllAssociations(TARGET_PATH);

//        Testing whether we got the correct number of associations
        assertEquals(allAssociations.length, 2,"Invalid number of associations returned");

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

    @Test(groups = {"wso2.greg"}, description = "Test backward association handler getAssociations method using source path")
    public void testGetAssociationSourcePath() throws RegistryException{
        Association[] associations = registry.getAssociations(SOURCE_PATH,CALLS);

//        Testing whether we got the correct number of associations
        assertEquals(associations.length,1,"Invalid number of association returned");

//        Testing whether the association is correct
        if(!(associations[0].getSourcePath().equals(SOURCE_PATH)
                && associations[0].getDestinationPath().equals(TARGET_PATH)
                && associations[0].getAssociationType().equals(CALLS))){
            fail("Incorrect association returned");
        }

    }

    @Test(groups = {"wso2.greg"}, description = "Test backward association handler getAssociations method using target path")
    public void testGetAssociationTargetPath() throws RegistryException{
        Association[] associations = registry.getAssociations(TARGET_PATH,CALLED_BY);

//        Testing whether we got the correct number of associations
        assertEquals(associations.length,1,"Invalid number of association returned");

//        Testing whether the association is correct
        if(!(associations[0].getSourcePath().equals(TARGET_PATH)
                && associations[0].getDestinationPath().equals(SOURCE_PATH)
                && associations[0].getAssociationType().equals(CALLED_BY))){
            fail("Incorrect association returned");
        }

    }

    @AfterClass(groups = {"wso2.greg"})
    public void testCleanup() throws Exception {
        util.logout();

    }

    private void doSleep() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
        }
    }

    public static String fileReader(String fileName) {
        StringBuilder builder = new StringBuilder();
        String strLine;

        try {
            FileInputStream fileInputStream = new FileInputStream(fileName);

            // Get the object of DataInputStream
            DataInputStream dataInputStream = new DataInputStream(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(dataInputStream));

            //Read File Line By Line
            while ((strLine = bufferedReader.readLine()) != null) {
               builder.append(strLine);
            }

            //Close the input stream
            dataInputStream.close();
            bufferedReader.close();
            fileInputStream.close();

        } catch (Exception e) {
            fail("File input error",e);
        }
        return builder.toString();

    }

}
