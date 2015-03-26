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
package org.wso2.greg.integration.resources.resource.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.automation.test.utils.common.FileManager;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.beans.xsd.CollectionContentBean;
import org.wso2.greg.integration.common.clients.LifeCycleManagementClient;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;

import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * A test case which tests registry life cycle operation
 */
public class LifeCycleTestCase extends GREGIntegrationBaseTest {

    private static final Log log = LogFactory.getLog(LifeCycleTestCase.class);

    private static final String PARENT_PATH = "/TestAutomation";
    private static final String LIFECYCLE_PARENT_COLL_NAME = "LifeCycleTestCase";

    private ResourceAdminServiceClient resourceAdminServiceClient;
    private LifeCycleManagementClient lifeCycleManagementClient;

    @BeforeClass(groups = {"wso2.greg"}, alwaysRun = true)
    public void init() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);

        log.debug("Running SuccessCase");
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(getBackendURL(),
                        automationContext.getContextTenant().getContextUser().getUserName(), automationContext.getContextTenant().getContextUser().getPassword());
        lifeCycleManagementClient =
                new LifeCycleManagementClient(getBackendURL(),
                        automationContext.getContextTenant().getContextUser().getUserName(), automationContext.getContextTenant().getContextUser().getPassword());
    }

    @Test(groups = {"wso2.greg"})
    public void runSuccessCase() throws ResourceAdminServiceExceptionException, RemoteException, XPathExpressionException {

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
        assertTrue(automationContext.getContextTenant().getContextUser().getUserName().equalsIgnoreCase(authorUserName),
                PARENT_PATH + " creation failure");
        log.info("collection added to " + collectionPath);

        collectionPath =
                resourceAdminServiceClient.addCollection(PARENT_PATH,
                        LIFECYCLE_PARENT_COLL_NAME, "", "");
        authorUserName = resourceAdminServiceClient.getResource(
                PARENT_PATH + "/" + LIFECYCLE_PARENT_COLL_NAME)[0].getAuthorUserName();
        assertTrue(automationContext.getContextTenant().getContextUser().getUserName().equalsIgnoreCase(authorUserName),
                PARENT_PATH + "/" + LIFECYCLE_PARENT_COLL_NAME + " creation failure");
        log.info("collection added to " + collectionPath);
    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = "runSuccessCase")
    public void testAddDefaultLC()
            throws LifeCycleManagementServiceExceptionException, IOException {

        String resource = FrameworkPathUtil.getSystemResourceLocation() +
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

        String resource = FrameworkPathUtil.getSystemResourceLocation() +
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

    @AfterClass(groups = {"wso2.greg"}, alwaysRun = true)
    public void cleanup() throws ResourceAdminServiceExceptionException, RemoteException {
        resourceAdminServiceClient.deleteResource("/TestAutomation");
        resourceAdminServiceClient = null;
        lifeCycleManagementClient = null;
    }
}
