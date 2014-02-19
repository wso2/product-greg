/*
 *  Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.registry.governance.api.test;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.testng.Assert;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.extensions.handlers.RecursiveDeleteHandler;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import java.io.File;

public class TestUtils {

    private TestUtils() {
    }

    public static void cleanupResources(Registry registry) throws RegistryException {
        for (String string : new String[]{"/trunk/services","/trunk/wsdls","/trunk","/branches","/departments",
                "/organizations",
                "/project-groups", "/people", "/applications", "/processes", "/projects",
                "/test_suites", "/test_cases", "/test_harnesses", "/test_methods"}) {
            if (registry.resourceExists(string)) {
                try{
                RecursiveDeleteHandler.acquireDeleteLock();
                registry.delete(string);
                RecursiveDeleteHandler.releaseDeleteLock();
                }catch (Exception skip){

                }
            }
        }
    }

    public static Registry getRegistry() {
        try {
            String url = "https://" + FrameworkSettings.HOST_NAME + ":" + FrameworkSettings.HTTPS_PORT
                    + "/services/";
            WSRegistryServiceClient registry =
                    new WSRegistryServiceClient(url, FrameworkSettings.USER_NAME,
                            FrameworkSettings.PASSWORD,
                            ConfigurationContextFactory.createConfigurationContextFromFileSystem(
                                    FrameworkSettings.CARBON_HOME +
                                            File.separator + "repository" + File.separator +
                                            "deployment" +
                                            File.separator + "client",
                                    ServerConfiguration.getInstance()
                                            .getFirstProperty(
                                                    "Axis2Config.clientAxis2XmlLocation")));
            return GovernanceUtils.getGovernanceUserRegistry(registry, FrameworkSettings.USER_NAME);
        } catch (RegistryException e) {
            Assert.fail("Unable to create registry instance: " + e.getMessage());
        } catch (AxisFault e) {
            Assert.fail("Unable to create registry instance: " + e.getMessage());
        }
        return null;
    }

    public static WSRegistryServiceClient getWSRegistry() {
        try {
            String url = "https://" + FrameworkSettings.HOST_NAME + ":" + FrameworkSettings.HTTPS_PORT
                         + "/services/";
            return new WSRegistryServiceClient(url, FrameworkSettings.USER_NAME,
                                        FrameworkSettings.PASSWORD,
                                        ConfigurationContextFactory.createConfigurationContextFromFileSystem(
                                                FrameworkSettings.CARBON_HOME +
                                                File.separator + "repository" + File.separator +
                                                "deployment" +
                                                File.separator + "client",
                                                ServerConfiguration.getInstance()
                                                        .getFirstProperty(
                                                                "Axis2Config.clientAxis2XmlLocation")));
        } catch (RegistryException e) {
            Assert.fail("Unable to create registry instance: " + e.getMessage());
        } catch (AxisFault e) {
            Assert.fail("Unable to create registry instance: " + e.getMessage());
        }
        return null;
    }

}
