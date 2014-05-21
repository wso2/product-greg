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

package org.wso2.carbon.registry.jira.issues.test;


import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.integration.common.admin.client.ServerAdminClient;
import org.wso2.carbon.integration.common.utils.FileManager;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;

import java.io.File;
import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;

public class Carbon10907TestCase extends GREGIntegrationBaseTest {
    private ResourceAdminServiceClient resourceAdminClient;

    private static final String NODE_CREATED_LOCATION = "/jcr_system/workspaces/default_workspace/node1";
    private static final String NODE_NAME = "node1";
    private static final String BUNDLE_NAME = "org.wso2.carbon.registry.jcr.test-1.0-SNAPSHOT.jar";
    private static final String JCR_LIB_NAME = "jcr-2.0.jar";
    private String dropinsPath;

    @BeforeClass(alwaysRun = true)
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    public void initialize() throws Exception{

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String session = getSessionCookie();
        resourceAdminClient =
                new ResourceAdminServiceClient(backendURL, session);


        //get the jcr lib location
        String jcrLibPath = getTestArtifactLocation() + "artifacts" + File.separator
                            + "GREG" + File.separator + "jcr" + File.separator + "jcr-2.0.jar";

        //get the artifact location
        String jarPath = getTestArtifactLocation() + "artifacts" + File.separator
                         + "GREG" + File.separator + "jcr" + File.separator +
                         "org.wso2.carbon.registry.jcr.test-1.0-SNAPSHOT.jar";

        //get the dropins location
        dropinsPath = CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator + "components" +
                      File.separator + "dropins";

        //copy the jcr lib
        FileManager.copyResourceToFileSystem(jcrLibPath, dropinsPath, JCR_LIB_NAME);

        //copy the file
        FileManager.copyResourceToFileSystem(jarPath, dropinsPath, BUNDLE_NAME);

    }




    @Test(groups = "wso2.greg")
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    public void testJcrComponent() throws Exception{
        ServerConfigurationManager serverConfigurationManager = new ServerConfigurationManager
                (automationContext);
        serverConfigurationManager.restartGracefully();

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String session = getSessionCookie();
        resourceAdminClient =
                new ResourceAdminServiceClient(backendURL, session);

        //check the added node
        String nodeName = resourceAdminClient.getResource(NODE_CREATED_LOCATION)[0].getName();

        assertTrue(NODE_NAME.equalsIgnoreCase(nodeName), "Node has not been created properly");

    }

    @AfterClass(alwaysRun = true)
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    public void cleanup() throws ResourceAdminServiceExceptionException, RemoteException {
        resourceAdminClient.deleteResource("/jcr_system");
        FileManager.deleteFile(dropinsPath + "/" + BUNDLE_NAME);
        FileManager.deleteFile(dropinsPath + "/" + JCR_LIB_NAME);
        resourceAdminClient = null;
    }

}
