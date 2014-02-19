/*
 *Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *WSO2 Inc. licenses this file to you under the Apache License,
 *Version 2.0 (the "License"); you may not use this file except
 *in compliance with the License.
 *You may obtain a copy of the License at
 *
 *http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing,
 *software distributed under the License is distributed on an
 *"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *KIND, either express or implied.  See the License for the
 *specific language governing permissions and limitations
 *under the License.
 */
package org.wso2.carbon.registry.jira.issues.test;

import java.io.File;
import java.rmi.RemoteException;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.registry.info.stub.RegistryExceptionException;

public class FileSystemImportationSecurityHotFixTestCase {

    private ResourceAdminServiceClient resourceAdminServiceClient;

    @BeforeClass(groups = "wso2.greg", alwaysRun = true, description = "initiating admin service, logging in as user2 ")
    public void initialize() throws RemoteException,
                                    LoginAuthenticationExceptionException {
        int userId = 2;
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        ManageEnvironment environment = builder.build();

        resourceAdminServiceClient =
                new ResourceAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                               environment.getGreg().getSessionCookie());
    }

    /*
     * "The source URL must not be file in the server's local file system" error
     * message generated when try to add a local file system path as a URL
     *
     */
    @Test(groups = "wso2.greg", description = "file importation security verification",
          expectedExceptions = RemoteException.class)
    public void testVerifyMetadata() throws RemoteException,
                                            RegistryExceptionException {
        final String MEDIA_TYPE = "application/xml";
        String path = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION
                      + "artifacts" + File.separator + "GREG" + File.separator
                      + "wsdl" + File.separator + "echo.wsdl";

        try {
            resourceAdminServiceClient.importResource("", "registry.xml",
                                                      MEDIA_TYPE, "file importation security fix verification",
                                                      "file:///" + path, null);
        } catch (Exception e) {
            if (e.toString().contains("The source URL must not be file in the server's local file system")) {
                throw new RemoteException();
            }
        }

    }

    @AfterClass(alwaysRun = true)
    public void testCleanup() {
        resourceAdminServiceClient = null;
    }
}
