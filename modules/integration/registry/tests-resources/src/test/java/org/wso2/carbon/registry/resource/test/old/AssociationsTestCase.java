/*
*  Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.

  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*
*/


package org.wso2.carbon.registry.resource.test.old;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.registry.RelationAdminServiceClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.registry.relations.stub.AddAssociationRegistryExceptionException;
import org.wso2.carbon.registry.relations.stub.beans.xsd.AssociationTreeBean;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.beans.xsd.CollectionContentBean;

import javax.activation.DataHandler;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;

/**
 * A test case which tests registry associations operation
 */
public class AssociationsTestCase {

    private ResourceAdminServiceClient resourceAdminServiceClient;
    private RelationAdminServiceClient relationAdminServiceClient;

    private static final Log log = LogFactory.getLog(AssociationsTestCase.class);
    private UserInfo userInfo;

    private static final String PARENT_PATH = "/TestAutomation";
    private static final String ASSOCIATION_PARENT_COLL_NAME = "AssociationTest";
    private static final String TEST_COLLECTION1 = "TestCollection1";
    private static final String TEST_COLLECTION2 = "TestCollection2";
    private static final String RESOURCE_NAME = "sampleText.txt";


    @BeforeClass(groups = {"wso2.greg"})
    public void init() throws Exception {

        int userId = 0;
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        ManageEnvironment environment = builder.build();

        userInfo = UserListCsvReader.getUserInfo(userId);

        log.debug("Running SuccessCase");


        resourceAdminServiceClient =
                new ResourceAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                               userInfo.getUserName(), userInfo.getPassword());
        relationAdminServiceClient = new RelationAdminServiceClient(
                environment.getGreg().getBackEndUrl(),
                userInfo.getUserName(), userInfo.getPassword());


    }

    @Test(groups = {"wso2.greg"})
    public void runSuccessCase() throws ResourceAdminServiceExceptionException, IOException {
        log.debug("Running SuccessCase");


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
        resourceAdminServiceClient.addCollection("/", "TestAutomation", "", "");
        String authorUserName =
                resourceAdminServiceClient.getResource(PARENT_PATH)[0].getAuthorUserName();
        assertTrue(userInfo.getUserName().equalsIgnoreCase(authorUserName),
                   PARENT_PATH + " creation failure");
        log.info("collection added to " + PARENT_PATH);


        resourceAdminServiceClient.addCollection(PARENT_PATH, ASSOCIATION_PARENT_COLL_NAME, "", "");
        authorUserName = resourceAdminServiceClient.getResource(
                PARENT_PATH + "/" + ASSOCIATION_PARENT_COLL_NAME)[0].getAuthorUserName();
        assertTrue(userInfo.getUserName().equalsIgnoreCase(authorUserName),
                   PARENT_PATH + "/" + ASSOCIATION_PARENT_COLL_NAME + " creation failure");
        log.info("collection added to " + PARENT_PATH + "/" + ASSOCIATION_PARENT_COLL_NAME);


        resourceAdminServiceClient.addCollection(PARENT_PATH + "/" + ASSOCIATION_PARENT_COLL_NAME,
                                                 TEST_COLLECTION1, "", "");
        authorUserName = resourceAdminServiceClient.getResource(
                PARENT_PATH + "/" + ASSOCIATION_PARENT_COLL_NAME + "/" +
                TEST_COLLECTION1)[0].getAuthorUserName();

        assertTrue(userInfo.getUserName().equalsIgnoreCase(authorUserName),
                   PARENT_PATH + "/" + ASSOCIATION_PARENT_COLL_NAME + "/" +
                   TEST_COLLECTION1 + " creation failure");

        resourceAdminServiceClient.addCollection(PARENT_PATH + "/" + ASSOCIATION_PARENT_COLL_NAME,
                                                 TEST_COLLECTION2, "", "");
        authorUserName = resourceAdminServiceClient.getResource(
                PARENT_PATH + "/" + ASSOCIATION_PARENT_COLL_NAME + "/" +
                TEST_COLLECTION2)[0].getAuthorUserName();
        assertTrue(userInfo.getUserName().equalsIgnoreCase(authorUserName),
                   PARENT_PATH + "/" + ASSOCIATION_PARENT_COLL_NAME + "/" +
                   TEST_COLLECTION2 + " creation failure");

        String resource = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION +
                          "artifacts" + File.separator
                          + "GREG" + File.separator + "txt" + File.separator + "sampleText.txt";

        DataHandler dh = new DataHandler(new URL("file:///" + resource));
        resourceAdminServiceClient.addResource(
                PARENT_PATH + "/" + ASSOCIATION_PARENT_COLL_NAME + "/" +
                TEST_COLLECTION1 + "/" + RESOURCE_NAME,
                "text/html",
                "txtDesc", dh);

        String text = resourceAdminServiceClient.getTextContent(
                PARENT_PATH + "/" + ASSOCIATION_PARENT_COLL_NAME + "/" +
                TEST_COLLECTION1 + "/" + RESOURCE_NAME);
        String fileText = dh.getContent().toString();

        assertTrue(fileText.equalsIgnoreCase(text),
                   PARENT_PATH + "/" + ASSOCIATION_PARENT_COLL_NAME + "/" +
                   TEST_COLLECTION1 + "/" + RESOURCE_NAME +
                   " creation failure");

    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = "runSuccessCase")
    private void testAddAssociation()
            throws AddAssociationRegistryExceptionException, RemoteException {

        relationAdminServiceClient.addAssociation(PARENT_PATH + "/" +
                                                  ASSOCIATION_PARENT_COLL_NAME + "/" +
                                                  TEST_COLLECTION2,
                                                  "association",
                                                  PARENT_PATH + "/" + ASSOCIATION_PARENT_COLL_NAME +
                                                  "/" + TEST_COLLECTION1 + "/" +
                                                  RESOURCE_NAME, "add");

        AssociationTreeBean associationTreeBean = relationAdminServiceClient.getAssociationTree(
                PARENT_PATH + "/" + ASSOCIATION_PARENT_COLL_NAME + "/" +
                TEST_COLLECTION2, "association");
        assertTrue(associationTreeBean.getAssociationTree().contains(
                PARENT_PATH + "/" + ASSOCIATION_PARENT_COLL_NAME + "/" +
                TEST_COLLECTION1 + "/" + RESOURCE_NAME),
                   "Added association not found in " + PARENT_PATH + "/" +
                   ASSOCIATION_PARENT_COLL_NAME + "/" + TEST_COLLECTION2);

        log.debug("associationTreeBean : " + associationTreeBean.getAssociationTree());

    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = "testAddAssociation")
    private void testDeleteAssociation()
            throws AddAssociationRegistryExceptionException, RemoteException {


        relationAdminServiceClient.addAssociation(PARENT_PATH + "/" +
                                                  ASSOCIATION_PARENT_COLL_NAME + "/" +
                                                  TEST_COLLECTION2,
                                                  "association",
                                                  PARENT_PATH + "/" + ASSOCIATION_PARENT_COLL_NAME +
                                                  "/" + TEST_COLLECTION1 + "/" + RESOURCE_NAME,
                                                  "remove");

        AssociationTreeBean associationTreeBean = relationAdminServiceClient.getAssociationTree(
                PARENT_PATH + "/" + ASSOCIATION_PARENT_COLL_NAME + "/" + TEST_COLLECTION2,
                "association");
        assertTrue(!associationTreeBean.getAssociationTree().contains(
                PARENT_PATH + "/" + ASSOCIATION_PARENT_COLL_NAME + "/" + TEST_COLLECTION1 +
                "/" + RESOURCE_NAME),
                   "Association still exists in " + PARENT_PATH + "/" +
                   ASSOCIATION_PARENT_COLL_NAME + "/" + TEST_COLLECTION2);

        log.debug("Association removed in : " + PARENT_PATH + "/" + ASSOCIATION_PARENT_COLL_NAME +
                  "/" + TEST_COLLECTION2);

    }

    //Disabled because REGISTRY-1257
    @Test(groups = {"wso2.greg"}, dependsOnMethods = "testDeleteAssociation", enabled = false)
    public void testAddInvalidTargetAssociation()
            throws AddAssociationRegistryExceptionException, RemoteException {
        //TODO - dd the expected exceptions to the test


        relationAdminServiceClient.addAssociation(
                PARENT_PATH + "/" + ASSOCIATION_PARENT_COLL_NAME + "/" + TEST_COLLECTION2,
                "association",
                PARENT_PATH + "/" + ASSOCIATION_PARENT_COLL_NAME + "/" +
                TEST_COLLECTION2 + "/invalid", "add");

    }

    //Disabled because REGISTRY-1257
    @Test(groups = {"wso2.greg"}, dependsOnMethods = "testDeleteAssociation", enabled = false)
    public void testAddInvalidSourceAssociation()
            throws AddAssociationRegistryExceptionException, RemoteException {
        //TODO - dd the expected exceptions to the test


        relationAdminServiceClient.addAssociation(
                "TestAutomation" + "/" + ASSOCIATION_PARENT_COLL_NAME + "/" + TEST_COLLECTION2,
                "association",
                PARENT_PATH + "/" + ASSOCIATION_PARENT_COLL_NAME + "/" + TEST_COLLECTION1 + "/" +
                RESOURCE_NAME, "add");


    }

    @AfterClass
    public void cleanup() throws ResourceAdminServiceExceptionException, RemoteException {
        resourceAdminServiceClient.deleteResource("/TestAutomation");

        resourceAdminServiceClient=null;
        relationAdminServiceClient=null;
    }
}
