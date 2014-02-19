/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.registry.ws.client.test.general;

import org.apache.axis2.context.ConfigurationContextFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import java.io.File;

import static org.testng.Assert.*;

/**
 * A test case which tests registry security
 */
public class UserSecurityWSTestCase extends TestSetup {

    private Registry everyOneRegistry = null;

    @BeforeClass(groups = {"wso2.greg"})
    public void initTest() {
        super.init();
    }

    @BeforeClass(groups = {"wso2.greg"})
    public void init() {
        // TODO Auto-generated method stub
        super.init();
        try {
            everyOneRegistry = new WSRegistryServiceClient("https://" + FrameworkSettings.HOST_NAME + ":" +
                    FrameworkSettings.HTTPS_PORT + "/services/",
                    "testuser1", "testuser1",
                    ConfigurationContextFactory.createConfigurationContextFromFileSystem(FrameworkSettings.CARBON_HOME +
                            File.separator + "repository" + File.separator + "deployment" + File.separator + "client",
                            ServerConfiguration.getInstance().getFirstProperty("Axis2Config.clientAxis2XmlLocation")));
            if (Boolean.parseBoolean(System.getProperty("run.with.security"))) {
                ((WSRegistryServiceClient) everyOneRegistry).addSecurityOptions(
                        FrameworkSettings.CARBON_HOME + TestSetup.POLICY_FILE_PATH,
                        FrameworkSettings.CARBON_HOME + File.separator +
                                "repository" + File.separator + "resources"
                                + File.separator + "security" + File.separator + "wso2carbon.jks",userName,password);
            }
        } catch (Exception e) {
            fail("everyone role was not able to authenticate - check permissions");
        }
    }
    // Since user manager does not have a WS API at the time of this test
    // users and permissions were manually removed/added from resources before this test was conducted
    // /testuser1/testuser1 is created - with /testuser1 having no permissions for the everyone role

    @Test(groups = {"wso2.greg"})
    public void checkEveryoneRoleDeniedPermissions() {

        try {

            Resource onlyAdminAcessResource = registry.newResource();
            registry.put("/testuser1/adminresource", onlyAdminAcessResource);

        } catch (Exception e) {
            // TODO Auto-generated catch block

        }

        try {
            Resource everyoneResource = everyOneRegistry.newResource();
            everyoneResource.setContent("this is a test resource");
            everyOneRegistry.put("/testuser1/everyoneresource", everyoneResource);
            fail("Everyone should not be able to add a resource");
        } catch (Exception e) {

        }

        try {
            everyOneRegistry.move("/testuser1/adminresource", "/newtestuser1");
            fail("Everyone should not be able to move resource");
        } catch (Exception e) {

        }
    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"checkEveryoneRoleDeniedPermissions"})
    public void checkEveryoneRoleAllowedPermissions() {
        try {

            Resource everoneAccessAccessResource = registry.newResource();
            registry.put("/testuser1/testuser1/everyoneAccessResource", everoneAccessAccessResource);

        } catch (Exception e) {

        }

        try {
            Resource everyoneResource = everyOneRegistry.newResource();
            everyoneResource.setContent("this is a test resource");
            everyOneRegistry.put("/testuser1/testuser1/everyoneresource", everyoneResource);
            fail("Everyone should not be able to add a resource");
        } catch (Exception e) {

        }

        try {
            everyOneRegistry.move("/testuser1/testuser1/adminresource", "/newtestuser2");
            fail("Everyone should not be able to move resource");
        } catch (Exception e) {

        }
    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"checkEveryoneRoleAllowedPermissions"})
    public void checkAdminRolePermissions() {
        try {

            Resource everyoneResource = registry.newResource();
            registry.put("/testuser1/testuser1/everyoneresource", everyoneResource);

        } catch (Exception e) {
            fail("everyone role was not able to add resource - please modify permissions");
        }

        try {
            Resource adminResource = registry.newResource();
            adminResource.setContent("this is a test resource");
            registry.put("/testuser1/everyoneresource", adminResource);
        } catch (Exception e) {
            fail("Admin should be able to add any resource");
        }

        try {
            registry.move("/testuser1/everyoneresource", "/newtestuser3");
        } catch (Exception e) {
            fail("Admin should be able to move resource");
        }
    }

}
