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
package org.wso2.carbon.registry.lifecycle.test;

import org.apache.axis2.AxisFault;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.governance.LifeCycleManagementClient;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.registry.lifecycle.test.utils.Utils;
import org.wso2.carbon.registry.search.metadata.test.utils.GregTestUtils;

import java.io.File;
import java.rmi.RemoteException;

public class LifeCycleErrorHandlingTestCase {
    private String sessionCookie;

    private LifeCycleManagementClient lifeCycleManagerAdminService;

    private final String ASPECT_NAME = "LifeCycleSyntaxTest";
    private String lifeCycleConfiguration;

    @BeforeClass
    public void init() throws Exception {
        ClientConnectionUtil.waitForPort(Integer.parseInt(FrameworkSettings.HTTP_PORT));
        sessionCookie = new LoginLogoutUtil().login();
        final String SERVER_URL = GregTestUtils.getServerUrl();
        lifeCycleManagerAdminService = new LifeCycleManagementClient(SERVER_URL, sessionCookie);
        String filePath = GregTestUtils.getResourcePath()
                          + File.separator + "lifecycle" + File.separator + "customLifeCycle.xml";
        lifeCycleConfiguration = GregTestUtils.readFile(filePath);
        lifeCycleConfiguration = lifeCycleConfiguration.replaceFirst("IntergalacticServiceLC", ASPECT_NAME);

        Utils.deleteLifeCycleIfExist(sessionCookie, ASPECT_NAME, lifeCycleManagerAdminService);

    }

    @Test(priority = 1)
    public void createLifeCycleWithInvalidXmlSyntax()
            throws LifeCycleManagementServiceExceptionException, RemoteException,
                   InterruptedException {
        String invalidLifeCycleConfiguration = lifeCycleConfiguration.replaceFirst("id=\"Commencement\">", "id=\"Commencement\"");
        try {
            Assert.assertFalse(lifeCycleManagerAdminService.addLifeCycle(invalidLifeCycleConfiguration),
                               "Life Cycle Added with invalid Syntax");

        } catch (AxisFault e) {
            Assert.assertTrue(e.getMessage().contains("Unable to parse the XML configuration. Please validate the XML configuration")
                    , "Unable to parse the XML configuration. Please validate the XML configuration. Actual Message :" + e.getMessage());
        }

        Thread.sleep(2000);
    }

    @Test(priority = 2)
    public void createLifeCycleWithInvalidTagName()
            throws LifeCycleManagementServiceExceptionException, RemoteException,
                   InterruptedException {
        String invalidLifeCycleConfiguration = lifeCycleConfiguration.replaceFirst("<configuration", "<config");
        invalidLifeCycleConfiguration = invalidLifeCycleConfiguration.replaceFirst("</configuration>", "</config>");
        try {
            Assert.assertFalse(lifeCycleManagerAdminService.addLifeCycle(invalidLifeCycleConfiguration),
                               "Life Cycle Added with invalid tag");

        } catch (AxisFault e) {
            Assert.assertTrue(e.getMessage().contains("Unable to validate the lifecycle configuration")
                    , "Unable to validate the lifecycle configuration. Actual Message" + e.getMessage());
        }
        Thread.sleep(2000);
    }

    @Test(priority = 3)
    public void addLifeCycleHavingExistingName()
            throws LifeCycleManagementServiceExceptionException, RemoteException {
        Assert.assertTrue(lifeCycleManagerAdminService.addLifeCycle(lifeCycleConfiguration)
                , "Adding New LifeCycle Failed first time");
        Assert.assertFalse(lifeCycleManagerAdminService.addLifeCycle(lifeCycleConfiguration)
                , "Adding New LifeCycle again having same name success");
    }

    @AfterClass
    public void cleanUp() {
        lifeCycleManagerAdminService = null;
        lifeCycleConfiguration = null;
    }

}
