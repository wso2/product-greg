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
package org.wso2.carbon.registry.lifecycle.test.old;

import org.apache.axis2.AxisFault;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.governance.LifeCycleManagementClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.core.utils.fileutils.FileManager;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.registry.lifecycle.test.utils.LifeCycleUtils;

import java.io.File;
import java.rmi.RemoteException;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class LifeCycleErrorHandlingLCTestCase {

    private int userId = 1;
    private UserInfo userInfo = UserListCsvReader.getUserInfo(userId);

    private LifeCycleManagementClient lifeCycleManagementClient;

    private final String ASPECT_NAME = "LifeCycleSyntaxTest";
    private String lifeCycleConfiguration;

    /**
     * @throws Exception
     */
    @BeforeClass
    public void init() throws Exception {
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        ManageEnvironment environment = builder.build();

        lifeCycleManagementClient = new LifeCycleManagementClient(environment.getGreg()
                                                                          .getProductVariables()
                                                                          .getBackendUrl(),
                                                                  userInfo.getUserName(),
                                                                  userInfo.getPassword());

        String filePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
                          File.separator + "GREG" + File.separator + "lifecycle" + File.separator + "customLifeCycle.xml";
        lifeCycleConfiguration = FileManager.readFile(filePath);
        lifeCycleConfiguration = lifeCycleConfiguration.replaceFirst("IntergalacticServiceLC", ASPECT_NAME);

        LifeCycleUtils.deleteLifeCycleIfExist(ASPECT_NAME, lifeCycleManagementClient);

    }

    /**
     * @throws org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException
     *
     * @throws java.rmi.RemoteException
     * @throws InterruptedException
     */
    @Test(groups = "wso2.greg", description = "Create a lifeCycle with invalid xml syntax")
    public void createLifeCycleWithInvalidXmlSyntax()
            throws LifeCycleManagementServiceExceptionException, RemoteException,
                   InterruptedException {
        String invalidLifeCycleConfiguration = lifeCycleConfiguration.replaceFirst("id=\"Commencement\">", "id=\"Commencement\"");
        try {
            assertFalse(lifeCycleManagementClient.addLifeCycle(invalidLifeCycleConfiguration),
                        "Life Cycle Added with invalid Syntax");

        } catch (AxisFault e) {
            assertTrue(e.getMessage().contains("Unable to parse the XML configuration. Please validate the XML configuration")
                    , "Unable to parse the XML configuration. Please validate the XML configuration. Actual Message :" + e.getMessage());
        }

        Thread.sleep(2000);
    }

    /**
     * @throws org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException
     *
     * @throws java.rmi.RemoteException
     * @throws InterruptedException
     */
    @Test(groups = "wso2.greg", description = "Create a lifeCycle with invalid tag name", dependsOnMethods = "createLifeCycleWithInvalidXmlSyntax")
    public void createLifeCycleWithInvalidTagName()
            throws LifeCycleManagementServiceExceptionException, RemoteException,
                   InterruptedException {
        String invalidLifeCycleConfiguration = lifeCycleConfiguration.replaceFirst("<configuration", "<config");
        invalidLifeCycleConfiguration = invalidLifeCycleConfiguration.replaceFirst("</configuration>", "</config>");
        try {
            assertFalse(lifeCycleManagementClient.addLifeCycle(invalidLifeCycleConfiguration),
                        "Life Cycle Added with invalid tag");

        } catch (AxisFault e) {
            assertTrue(e.getMessage().contains("Unable to validate the lifecycle configuration")
                    , "Unable to validate the lifecycle configuration. Actual Message" + e.getMessage());
        }
        Thread.sleep(2000);
    }

    /**
     * @throws org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException
     *
     * @throws java.rmi.RemoteException
     */
    @Test(groups = "wso2.greg", description = "Create a lifeCycle with existing name", dependsOnMethods = "createLifeCycleWithInvalidTagName")
    public void addLifeCycleHavingExistingName()
            throws LifeCycleManagementServiceExceptionException, RemoteException {
        assertTrue(lifeCycleManagementClient.addLifeCycle(lifeCycleConfiguration)
                , "Adding New LifeCycle Failed first time");
        assertFalse(lifeCycleManagementClient.addLifeCycle(lifeCycleConfiguration)
                , "Adding New LifeCycle again having same name success");
    }

    @Test(groups = "wso2.greg", description = "Delete added resources", dependsOnMethods = "addLifeCycleHavingExistingName")
    public void deleteResources()
            throws LifeCycleManagementServiceExceptionException, RemoteException {

        assertTrue(lifeCycleManagementClient.deleteLifeCycle(ASPECT_NAME),
                   "Life Cycle Deleted failed");
    }

    /**
     * @throws Exception
     */
    @AfterClass()
    public void cleanup() throws Exception {
        LifeCycleUtils.deleteLifeCycleIfExist(ASPECT_NAME, lifeCycleManagementClient);

    }

}
