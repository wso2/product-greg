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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.governance.LifeCycleManagementClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.core.utils.fileutils.FileManager;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.beans.xsd.CollectionContentBean;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
* A test case which tests registry life cycle operation
*/
public class LifeCycleTestCase {


    private static final Log log = LogFactory.getLog(LifeCycleTestCase.class);

    private ResourceAdminServiceClient resourceAdminServiceClient;
    private LifeCycleManagementClient lifeCycleManagementClient;

    private static final String PARENT_PATH = "/TestAutomation";
    private static final String LIFECYCLE_PARENT_COLL_NAME = "LifeCycleTestCase";

    private UserInfo userInfo;


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
        lifeCycleManagementClient =
                new LifeCycleManagementClient(environment.getGreg().getBackEndUrl(),
                                              userInfo.getUserName(), userInfo.getPassword());
    }

    @Test(groups = {"wso2.greg"})
    public void runSuccessCase() throws ResourceAdminServiceExceptionException, RemoteException {
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
        String collectionPath =
                resourceAdminServiceClient.addCollection("/", "TestAutomation", "", "");
        String authorUserName =
                resourceAdminServiceClient.getResource(PARENT_PATH)[0].getAuthorUserName();
        assertTrue(userInfo.getUserName().equalsIgnoreCase(authorUserName),
                   PARENT_PATH + " creation failure");
        log.info("collection added to " + collectionPath);

        collectionPath =
                resourceAdminServiceClient.addCollection(PARENT_PATH,
                                                         LIFECYCLE_PARENT_COLL_NAME, "", "");
        authorUserName = resourceAdminServiceClient.getResource(
                PARENT_PATH + "/" + LIFECYCLE_PARENT_COLL_NAME)[0].getAuthorUserName();
        assertTrue(userInfo.getUserName().equalsIgnoreCase(authorUserName),
                   PARENT_PATH + "/" + LIFECYCLE_PARENT_COLL_NAME + " creation failure");
        log.info("collection added to " + collectionPath);
    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = "runSuccessCase")
    public void testAddDefaultLC()
            throws LifeCycleManagementServiceExceptionException, IOException {

        String resource = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION +
                          "artifacts" + File.separator
                          + "GREG" + File.separator + "lifecycle" +
                          File.separator + "customLifeCycle.xml";

        String content = FileManager.readFile(resource);
        lifeCycleManagementClient.addLifeCycle(content);

        String[] lcList = lifeCycleManagementClient.getLifecycleList();
        boolean found = false;

        for (String lcName : lcList) {
            if (lcName.equalsIgnoreCase("IntergalacticServiceLC")) {
                found = true;
                break;
            }
        }

        assertTrue(found, "Lifecycle not added.");


    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = "testAddDefaultLC")
    public void testUpdateLifecycle()
            throws LifeCycleManagementServiceExceptionException, IOException {

        String resource = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION +
                          "artifacts" + File.separator
                          + "GREG" + File.separator + "lifecycle" + File.separator +
                          "LongCheckListLC.xml";

        String content = FileManager.readFile(resource);

        lifeCycleManagementClient.editLifeCycle("IntergalacticServiceLC", content);

        String[] lifeCycles = lifeCycleManagementClient.getLifecycleList();

        boolean lcStatus = false;
        for (String lc : lifeCycles) {

            if (lc.equalsIgnoreCase("LongCheckListLC")) {
                lcStatus = true;
            }
        }
        assertTrue(lcStatus, "LifeCycle not edited");
    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = "testUpdateLifecycle")
    public void testDeleteLifecycle()
            throws LifeCycleManagementServiceExceptionException, RemoteException {
        lifeCycleManagementClient.deleteLifeCycle("LongCheckListLC");

        String[] lcList = lifeCycleManagementClient.getLifecycleList();
        boolean found = false;

        for (String lcName : lcList) {
            if (lcName.equalsIgnoreCase("LongCheckListLC")) {
                found = true;
                break;
            }
        }

        assertFalse(found, "Lifecycle not deleted.");

        log.info("Lifecycle \"CustomLifeCycleChanged\" deleted.");

    }

    @AfterClass
    public void cleanup() throws ResourceAdminServiceExceptionException, RemoteException {
        resourceAdminServiceClient.deleteResource("/TestAutomation");

        resourceAdminServiceClient=null;
        lifeCycleManagementClient=null;
        userInfo=null;
    }
}
