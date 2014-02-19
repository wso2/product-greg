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
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.api.clients.server.admin.ServerAdminClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.ServerGroupManager;
import org.wso2.carbon.automation.core.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.core.annotations.SetEnvironment;
import org.wso2.carbon.automation.core.utils.ClientConnectionUtil;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.core.utils.fileutils.FileManager;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkFactory;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkProperties;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;

public class Carbon10907 {
    private ResourceAdminServiceClient resourceAdminClient;
    private ServerAdminClient serverAdminClient;
    private ManageEnvironment environment;
    private EnvironmentBuilder builder;

    private static final String NODE_CREATED_LOCATION = "/jcr_system/workspaces/default_workspace/node1";
    private static final String NODE_NAME = "node1";
    private static final String BUNDLE_NAME = "org.wso2.carbon.registry.jcr.test-1.0-SNAPSHOT.jar";
    private static final String JCR_LIB_NAME = "jcr-2.0.jar";
    private String dropinsPath;
    private int userId = ProductConstant.ADMIN_USER_ID;

    @BeforeClass(alwaysRun = true)
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_user})
    public void initialize()
            throws LoginAuthenticationExceptionException, IOException, RegistryException {

        init();

        //get the jcr lib location
        String jcrLibPath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator
                            + "GREG" + File.separator + "jcr" + File.separator + "jcr-2.0.jar";

        //get the artifact location
        String jarPath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator
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

    private void init() throws RemoteException, LoginAuthenticationExceptionException {
        builder = new EnvironmentBuilder().greg(userId);
        environment = builder.build();

        resourceAdminClient =
                new ResourceAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                               environment.getGreg().getSessionCookie());

        serverAdminClient =
                new ServerAdminClient(environment.getGreg().getBackEndUrl(),
                                      environment.getGreg().getSessionCookie());
    }


    @Test(groups = "wso2.greg")
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_user})
    public void testJcrComponent()
            throws Exception, RemoteException, InterruptedException,
                   ResourceAdminServiceExceptionException {

        //restart server
        FrameworkProperties frameworkProperties =
                FrameworkFactory.getFrameworkProperties(ProductConstant.GREG_SERVER_NAME);
        ServerGroupManager.getServerUtils().restartGracefully(serverAdminClient, frameworkProperties);

        init();//reinitialize after server restart.

        //check the added node
        String nodeName = resourceAdminClient.getResource(NODE_CREATED_LOCATION)[0].getName();

        assertTrue(NODE_NAME.equalsIgnoreCase(nodeName), "Node has not been created properly");

    }

    @AfterClass(alwaysRun = true)
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_user})
    public void cleanup() throws ResourceAdminServiceExceptionException, RemoteException {
        resourceAdminClient.deleteResource("/jcr_system");
        FileManager.deleteFile(dropinsPath + "/" + BUNDLE_NAME);
        FileManager.deleteFile(dropinsPath + "/" + JCR_LIB_NAME);
        resourceAdminClient = null;
        serverAdminClient = null;
        environment = null;
        builder = null;
    }

}
