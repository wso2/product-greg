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
package org.wso2.carbon.registry.resource.test.old;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.beans.xsd.CollectionContentBean;

import java.rmi.RemoteException;

import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

//TODO- cleanup comments

/**
 * A test case which tests registry collection
 */
public class RegistryCollectionTestCase {


    private static final Log log = LogFactory.getLog(RegistryCollectionTestCase.class);

    private static final String PARENT_PATH = "/TestAutomation";
    private static final String ESB_COLL_NAME = "wso2.esb";
    private static final String SYNAPSE_COLL_NAME = "apache.synapse";
    private static final String AXIS2_COLL_NAME = "apache.axis2";
    private static final String WSAS_COLL_NAME = "wso2.wsas";
    private static final String NEW_ESB_COLL_NAME = "new_wso2.esb";
    private static final String NEW_SYNAPSE_COLL_NAME = "new_apache.synapse";
    private static final String NEW_AXIS2_COLL_NAME = "new_apache.axis2";
    private static final String NEW_WSAS_COLL_NAME = "new_wso2.wsas";
    private static final String SYNAPSE_MEDIATYPE = "application/vnd.apache.synapse";
    private static final String AXIS2_MEDIATYPE = "application/vnd.apache.axis2";
    private static final String WSAS_MEDIATYPE = "application/vnd.wso2.wsas";
    private static final String ESB_MEDIATYPE = "application/vnd.wso2.esb";
    private static final String MOVED_COLLECTIONS_COLL_NAME = "movedCollections";

    private ResourceAdminServiceClient resourceAdminServiceClient;

    private UserInfo userInfo;


    @BeforeClass(groups = {"wso2.greg"})
    public void init() throws Exception {
        log.info("Registry collection test started");

        int userId = 0;
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        ManageEnvironment environment = builder.build();

        userInfo = UserListCsvReader.getUserInfo(userId);
        log.debug("Running SuccessCase");


        resourceAdminServiceClient =
                new ResourceAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                               userInfo.getUserName(), userInfo.getPassword());

    }

    @Test(groups = {"wso2.greg"})
    public void testCollectionManagement()
            throws ResourceAdminServiceExceptionException, RemoteException {

        CollectionContentBean collectionContentBean =
                resourceAdminServiceClient.getCollectionContent("/");

        if (collectionContentBean.getChildCount() > 0) {
            String[] childPath = collectionContentBean.getChildPaths();
            for (int i = 0; i <= childPath.length - 1; i++) {
                if (childPath[i].equalsIgnoreCase(PARENT_PATH)) {
                    resourceAdminServiceClient.deleteResource(PARENT_PATH);
                }
            }
        }
        // adding normal collection
        String collectionPath =
                resourceAdminServiceClient.addCollection("/", "TestAutomation", "", "");
        // Changing media type
        log.debug("collection added to " + collectionPath);
        collectionPath = resourceAdminServiceClient.addCollection(PARENT_PATH, ESB_COLL_NAME,
                                                                  ESB_MEDIATYPE, ESB_MEDIATYPE +
                                                                                 " media type collection");
        String authorUserName =
                resourceAdminServiceClient.getResource(PARENT_PATH + "/" +
                                                       ESB_COLL_NAME)[0].getAuthorUserName();
        assertTrue(userInfo.getUserName().equalsIgnoreCase(authorUserName),
                   PARENT_PATH + "/" + ESB_COLL_NAME + " creation failure");
        log.debug("Media type " + ESB_MEDIATYPE + " collection added to " + collectionPath);

        collectionPath =
                resourceAdminServiceClient.addCollection(PARENT_PATH, SYNAPSE_COLL_NAME,
                                                         SYNAPSE_MEDIATYPE, SYNAPSE_MEDIATYPE +
                                                                            " media type collection");
        authorUserName =
                resourceAdminServiceClient.getResource(PARENT_PATH + "/" +
                                                       SYNAPSE_COLL_NAME)[0].getAuthorUserName();
        assertTrue(userInfo.getUserName().equalsIgnoreCase(authorUserName),
                   PARENT_PATH + "/" + SYNAPSE_COLL_NAME + " creation failure");
        log.debug("Media type " + SYNAPSE_MEDIATYPE + " collection added to " + collectionPath);

        collectionPath = resourceAdminServiceClient.addCollection(PARENT_PATH, AXIS2_COLL_NAME,
                                                                  AXIS2_MEDIATYPE, AXIS2_MEDIATYPE +
                                                                                   " media type collection");
        authorUserName =
                resourceAdminServiceClient.getResource(PARENT_PATH +
                                                       "/" +
                                                       AXIS2_COLL_NAME)[0].getAuthorUserName();
        assertTrue(userInfo.getUserName().equalsIgnoreCase(authorUserName),
                   PARENT_PATH + "/" + AXIS2_COLL_NAME + " creation failure");
        log.debug("Media type " + AXIS2_MEDIATYPE + " collection added to " + collectionPath);

        collectionPath = resourceAdminServiceClient.addCollection(PARENT_PATH, WSAS_COLL_NAME,
                                                                  WSAS_MEDIATYPE, WSAS_MEDIATYPE +
                                                                                  " media type collection");
        authorUserName =
                resourceAdminServiceClient.getResource(PARENT_PATH +
                                                       "/" +
                                                       WSAS_COLL_NAME)[0].getAuthorUserName();
        assertTrue(userInfo.getUserName().equalsIgnoreCase(authorUserName),
                   PARENT_PATH + "/" + WSAS_COLL_NAME + " creation failure");
        log.debug("Media type " + WSAS_MEDIATYPE + " collection added to " + collectionPath);

        // Renaming collections
        resourceAdminServiceClient.renameResource(PARENT_PATH,
                                                  PARENT_PATH + "/" + ESB_COLL_NAME,
                                                  NEW_ESB_COLL_NAME);
        authorUserName =
                resourceAdminServiceClient.getResource(PARENT_PATH +
                                                       "/" +
                                                       NEW_ESB_COLL_NAME)[0].getAuthorUserName();
        assertTrue(userInfo.getUserName().equalsIgnoreCase(authorUserName),
                   PARENT_PATH + "/" + ESB_COLL_NAME + " renaming failure");

        resourceAdminServiceClient.renameResource(PARENT_PATH,
                                                  PARENT_PATH + "/" + SYNAPSE_COLL_NAME,
                                                  NEW_SYNAPSE_COLL_NAME);
        authorUserName =
                resourceAdminServiceClient.getResource(PARENT_PATH +
                                                       "/" +
                                                       NEW_SYNAPSE_COLL_NAME)[0].getAuthorUserName();
        assertTrue(userInfo.getUserName().equalsIgnoreCase(authorUserName),
                   PARENT_PATH + "/" + SYNAPSE_COLL_NAME + " renaming failure");

        resourceAdminServiceClient.renameResource(PARENT_PATH,
                                                  PARENT_PATH + "/" +
                                                  AXIS2_COLL_NAME, NEW_AXIS2_COLL_NAME);
        authorUserName =
                resourceAdminServiceClient.getResource(PARENT_PATH + "/" +
                                                       NEW_AXIS2_COLL_NAME)[0].getAuthorUserName();
        assertTrue(userInfo.getUserName().equalsIgnoreCase(authorUserName),
                   PARENT_PATH + "/" + AXIS2_COLL_NAME + " renaming failure");

        resourceAdminServiceClient.renameResource(PARENT_PATH,
                                                  PARENT_PATH + "/" + WSAS_COLL_NAME,
                                                  NEW_WSAS_COLL_NAME);
        authorUserName =
                resourceAdminServiceClient.getResource(PARENT_PATH + "/" +
                                                       NEW_WSAS_COLL_NAME)[0].getAuthorUserName();
        assertTrue(userInfo.getUserName().equalsIgnoreCase(authorUserName),
                   PARENT_PATH + "/" + WSAS_COLL_NAME + " renaming failure");


        // move collections
        resourceAdminServiceClient.addCollection(PARENT_PATH, MOVED_COLLECTIONS_COLL_NAME, "",
                                                 "Collections which contain moved sample collections");
        authorUserName =
                resourceAdminServiceClient.getResource(PARENT_PATH + "/" +
                                                       MOVED_COLLECTIONS_COLL_NAME)[0].getAuthorUserName();
        assertTrue(userInfo.getUserName().equalsIgnoreCase(authorUserName),
                   PARENT_PATH + "/" + MOVED_COLLECTIONS_COLL_NAME + " creation failure");


        resourceAdminServiceClient.moveResource(PARENT_PATH,
                                                PARENT_PATH + "/" + NEW_ESB_COLL_NAME,
                                                PARENT_PATH + "/" + MOVED_COLLECTIONS_COLL_NAME,
                                                NEW_ESB_COLL_NAME);
        authorUserName =
                resourceAdminServiceClient.getResource(PARENT_PATH + "/" +
                                                       MOVED_COLLECTIONS_COLL_NAME +
                                                       "/" +
                                                       NEW_ESB_COLL_NAME)[0].getAuthorUserName();
        assertTrue(userInfo.getUserName().equalsIgnoreCase(authorUserName),
                   PARENT_PATH + "/" + ESB_COLL_NAME + " moving failure - Resource didn't move");
//            authorUserName =
//                    resourceAdminServiceClient.getResource(PARENT_PATH+"/"+NEW_ESB_COLL_NAME)[0].getAuthorUserName();
//            assertTrue(userInfo.getUserName().equalsIgnoreCase(authorUserName),
//                       PARENT_PATH+"/"+ESB_COLL_NAME + " resource found in original path after move");


        resourceAdminServiceClient.moveResource(PARENT_PATH,
                                                PARENT_PATH + "/" + NEW_SYNAPSE_COLL_NAME,
                                                PARENT_PATH + "/" + MOVED_COLLECTIONS_COLL_NAME,
                                                NEW_SYNAPSE_COLL_NAME);
        authorUserName =
                resourceAdminServiceClient.getResource(PARENT_PATH + "/" +
                                                       MOVED_COLLECTIONS_COLL_NAME + "/" +
                                                       NEW_SYNAPSE_COLL_NAME)[0].getAuthorUserName();
        assertTrue(userInfo.getUserName().equalsIgnoreCase(authorUserName),
                   PARENT_PATH + "/" + SYNAPSE_COLL_NAME + " moving failure - Resource didn't move");
//            authorUserName =
//                    resourceAdminServiceClient.getResource(PARENT_PATH+"/"+NEW_SYNAPSE_COLL_NAME)[0].getAuthorUserName();
//            assertTrue(userInfo.getUserName().equalsIgnoreCase(authorUserName),
//                       PARENT_PATH+"/"+SYNAPSE_COLL_NAME + " resource found in original path after move");


        resourceAdminServiceClient.moveResource(PARENT_PATH, PARENT_PATH + "/" +
                                                             NEW_AXIS2_COLL_NAME,
                                                PARENT_PATH + "/" + MOVED_COLLECTIONS_COLL_NAME,
                                                NEW_AXIS2_COLL_NAME);
        authorUserName =
                resourceAdminServiceClient.getResource(PARENT_PATH +
                                                       "/" + MOVED_COLLECTIONS_COLL_NAME +
                                                       "/" +
                                                       NEW_AXIS2_COLL_NAME)[0].getAuthorUserName();
        assertTrue(userInfo.getUserName().equalsIgnoreCase(authorUserName),
                   PARENT_PATH + "/" + AXIS2_COLL_NAME + " moving failure - Resource didn't move");
//            authorUserName =
//                    resourceAdminServiceClient.getResource(PARENT_PATH+"/"+NEW_AXIS2_COLL_NAME)[0].getAuthorUserName();
//            assertTrue(userInfo.getUserName().equalsIgnoreCase(authorUserName),
//                       PARENT_PATH+"/"+AXIS2_COLL_NAME + " resource found in original path after move");

        resourceAdminServiceClient.moveResource(PARENT_PATH,
                                                PARENT_PATH + "/" + NEW_WSAS_COLL_NAME,
                                                PARENT_PATH + "/" + MOVED_COLLECTIONS_COLL_NAME,
                                                NEW_WSAS_COLL_NAME);
        authorUserName =
                resourceAdminServiceClient.getResource(PARENT_PATH + "/" +
                                                       MOVED_COLLECTIONS_COLL_NAME +
                                                       "/" + NEW_WSAS_COLL_NAME)[0].getAuthorUserName();
        assertTrue(userInfo.getUserName().equalsIgnoreCase(authorUserName),
                   PARENT_PATH + "/" + WSAS_COLL_NAME + " moving failure - Resource didn't move");
//            authorUserName =
//                    resourceAdminServiceClient.getResource(PARENT_PATH+"/"+NEW_WSAS_COLL_NAME)[0].getAuthorUserName();
//            assertTrue(userInfo.getUserName().equalsIgnoreCase(authorUserName),
//                       PARENT_PATH+"/"+WSAS_COLL_NAME + " resource found in original path after move");


        //copy collections
        resourceAdminServiceClient.copyResource(PARENT_PATH + "/" +
                                                MOVED_COLLECTIONS_COLL_NAME,
                                                PARENT_PATH + "/" +
                                                MOVED_COLLECTIONS_COLL_NAME + "/" +
                                                NEW_ESB_COLL_NAME,
                                                PARENT_PATH, NEW_ESB_COLL_NAME);
        authorUserName =
                resourceAdminServiceClient.getResource(PARENT_PATH +
                                                       "/" + NEW_ESB_COLL_NAME)[0].getAuthorUserName();
        assertTrue(userInfo.getUserName().equalsIgnoreCase(authorUserName),
                   PARENT_PATH + "/" + ESB_COLL_NAME + " copying failure");

        resourceAdminServiceClient.copyResource(PARENT_PATH + "/" +
                                                MOVED_COLLECTIONS_COLL_NAME,
                                                PARENT_PATH + "/" +
                                                MOVED_COLLECTIONS_COLL_NAME + "/" +
                                                NEW_SYNAPSE_COLL_NAME, PARENT_PATH,
                                                NEW_SYNAPSE_COLL_NAME);
        authorUserName =
                resourceAdminServiceClient.getResource(PARENT_PATH + "/" +
                                                       NEW_SYNAPSE_COLL_NAME)[0].getAuthorUserName();
        assertTrue(userInfo.getUserName().equalsIgnoreCase(authorUserName),
                   PARENT_PATH + "/" + SYNAPSE_COLL_NAME + " copying failure");
        resourceAdminServiceClient.copyResource(PARENT_PATH + "/" +
                                                MOVED_COLLECTIONS_COLL_NAME,
                                                PARENT_PATH + "/" + MOVED_COLLECTIONS_COLL_NAME +
                                                "/" + NEW_AXIS2_COLL_NAME,
                                                PARENT_PATH, NEW_AXIS2_COLL_NAME);
        authorUserName =
                resourceAdminServiceClient.getResource(PARENT_PATH + "/" +
                                                       NEW_AXIS2_COLL_NAME)[0].getAuthorUserName();
        assertTrue(userInfo.getUserName().equalsIgnoreCase(authorUserName),
                   PARENT_PATH + "/" + AXIS2_COLL_NAME + " copying failure");

        resourceAdminServiceClient.copyResource(PARENT_PATH + "/" +
                                                MOVED_COLLECTIONS_COLL_NAME,
                                                PARENT_PATH + "/" + MOVED_COLLECTIONS_COLL_NAME +
                                                "/" + NEW_WSAS_COLL_NAME,
                                                PARENT_PATH, NEW_WSAS_COLL_NAME);
        authorUserName =
                resourceAdminServiceClient.getResource(PARENT_PATH + "/" +
                                                       NEW_WSAS_COLL_NAME)[0].getAuthorUserName();
        assertTrue(userInfo.getUserName().equalsIgnoreCase(authorUserName),
                   PARENT_PATH + "/" + WSAS_COLL_NAME + " copying failure");

        resourceAdminServiceClient.deleteResource(PARENT_PATH);


        //assertNull(resourceAdminServiceClient.getResource(PARENT_PATH+"/"+MOVED_COLLECTIONS_COLL_NAME)[0],PARENT_PATH+"/"+MOVED_COLLECTIONS_COLL_NAME+" found even after deleting");


    }

    //

    @Test(groups = {"wso2.greg"})
    public void testCollectionBoundary() throws Exception {

        String collectionPath = null;
        // some characters may fail due to the CARBON-8331
        String[] charBuffer = {"~", "!", "@", "#", "%", "^",
                               "*", "+", "=", "{", "}", "|", "\\", "<", ">", "\"", "\'", ";"};
        for (String aCharBuffer : charBuffer) {
            try {
                System.out.println(aCharBuffer);
                collectionPath =
                        resourceAdminServiceClient.addCollection(PARENT_PATH,
                                                                 "wso2." + aCharBuffer,
                                                                 ESB_MEDIATYPE,
                                                                 ESB_MEDIATYPE +
                                                                 " media type collection");
                assertNull(collectionPath,
                           "Invalid collection added with illegal character " + aCharBuffer);

                resourceAdminServiceClient.deleteResource(PARENT_PATH);

            } catch (AxisFault e) {

                assertNull(collectionPath,
                           "Successfully rejected invalidly named collection add operation..!!");
            } catch (Exception e) {
                e.printStackTrace();

                assertTrue(e.getMessage().contains("con" +
                                                    "tains one or more illegal characters" +
                                                    " (~!@#$;%^*()+={}|\\<>\"',)"),
                           "Invalid collection added with illeagal character " + aCharBuffer);
                log.error("Invalid collection added with illigal character " + aCharBuffer);
                throw new Exception(e);

            }
        }
    }

    //cleanup code
    @AfterClass
    public void cleanup()
            throws Exception {
        resourceAdminServiceClient.deleteResource("/TestAutomation");
        resourceAdminServiceClient=null;
        userInfo=null;
    }

}
