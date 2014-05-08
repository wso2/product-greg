/*
 * Copyright 2004,2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.registry.lifecycle.test;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.test.utils.common.FileManager;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
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
 * Added a lifecycle with 'forEvent="Pause"'
 */
public abstract class InvalidForEventLCTestCase extends GREGIntegrationBaseTest {

    private LifeCycleManagementClient lifeCycleManagementClient;
    private static final String LC_NAME = "InvalidForEventLC";

    /**
     * @throws RemoteException
     * @throws LoginAuthenticationExceptionException
     *
     */
    @BeforeClass(enabled = false)
    public void init() throws IOException, LoginAuthenticationExceptionException, XPathExpressionException,
            URISyntaxException, SAXException, XMLStreamException {

        super.init(TestUserMode.SUPER_TENANT_USER);
        String sessionCookie = new LoginLogoutClient(automationContext).login();
        lifeCycleManagementClient =
                new LifeCycleManagementClient(backendURL, sessionCookie);

    }

    /**
     * This test case is disabled due to
     * https://wso2.org/jira/browse/REGISTRY-1188
     *
     * @throws LifeCycleManagementServiceExceptionException
     *
     * @throws IOException
     * @throws InterruptedException
     */
    @Test(enabled = false, groups = "wso2.greg", description = "Create new life cycle")
    public void testCreateNewLifeCycle() throws LifeCycleManagementServiceExceptionException,
                                                IOException, InterruptedException {
        String resourcePath =
                getTestArtifactLocation() + "artifacts" +
                File.separator + "GREG" + File.separator + "lifecycle" +
                File.separator + "InvalidForEventLC.xml";
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

    @AfterClass(enabled = false)
    public void cleanResources() {
        lifeCycleManagementClient = null;
    }

}
