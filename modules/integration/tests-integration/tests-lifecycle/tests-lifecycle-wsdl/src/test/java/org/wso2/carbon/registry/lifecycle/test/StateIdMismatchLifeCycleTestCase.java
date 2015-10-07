/*
* Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.registry.lifecycle.test;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.test.utils.common.FileManager;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.greg.integration.common.clients.LifeCycleManagementClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.xml.sax.SAXException;

import javax.xml.stream.XMLStreamException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.rmi.RemoteException;

import static org.testng.Assert.assertFalse;

/**
 * Create lifecycles where the "initialstate" and the "state id" does not match.
 * Create a lifecycle where the "Promote/Demote" states contain values that does
 * not match with any of the "state ids" that are in the LC config
 * https://wso2.org/jira/browse/REGISTRY-1188
 */
public class StateIdMismatchLifeCycleTestCase extends GREGIntegrationBaseTest {

    private LifeCycleManagementClient lifeCycleManagementClient;
    private final String LC_NAME = "InvalidInitialStateLC";
    private final String LC_NAME2 = "InvalidTargetStateLC";

    /**
     * @throws RemoteException
     * @throws LoginAuthenticationExceptionException
     *
     * @throws RegistryException
     */
    @BeforeClass (enabled = false)
    public void init () throws Exception {

        super.init(TestUserMode.SUPER_TENANT_USER);
        String sessionCookie = getSessionCookie();
        lifeCycleManagementClient =
                new LifeCycleManagementClient(backendURL, sessionCookie);
    }

    /**
     * This test case is disabled because of,
     * https://wso2.org/jira/browse/REGISTRY-1188
     *
     * @throws LifeCycleManagementServiceExceptionException
     *
     * @throws IOException
     * @throws InterruptedException
     */
    @Test (enabled = false, groups = "wso2.greg", description = "Create new life cycle with undefined initial state")
    public void testCreateNewLifeCycle () throws LifeCycleManagementServiceExceptionException,
            IOException, InterruptedException {

        String resourcePath =
                getTestArtifactLocation() + "artifacts" +
                        File.separator + "GREG" + File.separator + "lifecycle" +
                        File.separator + "InvalidInitialStateLC.xml";
        String lifeCycleContent = FileManager.readFile(resourcePath);
        lifeCycleManagementClient.addLifeCycle(lifeCycleContent);
        String[] lifeCycles = lifeCycleManagementClient.getLifecycleList();
        boolean lcStatus = false;
        for (String lc : lifeCycles) {
            if (lc.equalsIgnoreCase(LC_NAME)) {
                lcStatus = true;

            }
        }
        assertFalse(lcStatus, "LifeCycle added with invalid configuration");

    }

    /**
     * This test case is disabled because of,
     * https://wso2.org/jira/browse/REGISTRY-1188
     *
     * @throws LifeCycleManagementServiceExceptionException
     *
     * @throws IOException
     * @throws InterruptedException
     */
    @Test (enabled = false, groups = "wso2.greg", description = "Create new life cycle with undefined promoting state",
            dependsOnMethods = "testCreateNewLifeCycle")
    public void testCreateAnotherLifeCycle () throws LifeCycleManagementServiceExceptionException,
            IOException, InterruptedException {

        String resourcePath =
                getTestArtifactLocation() + "artifacts" +
                        File.separator + "GREG" + File.separator + "lifecycle" +
                        File.separator + "InvalidTargetStateLC.xml";
        String lifeCycleContent = FileManager.readFile(resourcePath);
        lifeCycleManagementClient.addLifeCycle(lifeCycleContent);
        String[] lifeCycles = lifeCycleManagementClient.getLifecycleList();
        boolean lcStatus = false;
        for (String lc : lifeCycles) {
            if (lc.equalsIgnoreCase(LC_NAME2)) {
                lcStatus = true;

            }
        }
        assertFalse(lcStatus, "LifeCycle added with invalid configuration");
    }

    @AfterClass (enabled = false)
    public void clearResources () {

        lifeCycleManagementClient = null;
    }

}
