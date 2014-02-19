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

package org.wso2.carbon.registry.ws.client.test.general;

import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import java.io.File;

import static org.testng.Assert.fail;

/**
 * A test case which is a util parent test of its peers
 */
public class TestSetup {

    protected static Registry registry = null;
    static boolean isInitialized = false;
    protected String userName ="admin";
    protected String password ="admin";

    private static final Log log = LogFactory.getLog(TestSetup.class);
    public static final String POLICY_FILE_PATH = File.separator + "repository" + File.separator +
            "conf" + File.separator + "ws-api-sec-policy.xml";
    String frameworkPath = "";

    public void init() {
        frameworkPath = FrameworkSettings.getFrameworkPath();

        log.info("Initializing WS-API Tests");
        try {
            String url = "https://" + FrameworkSettings.HOST_NAME + ":" + FrameworkSettings.HTTPS_PORT + "/services/";
            registry = new WSRegistryServiceClient(url, FrameworkSettings.USER_NAME,
                    FrameworkSettings.PASSWORD,
                    ConfigurationContextFactory.createConfigurationContextFromFileSystem(
                            FrameworkSettings.CARBON_HOME +
                                    File.separator + "repository" + File.separator + "deployment" +
                                    File.separator + "client", ServerConfiguration.getInstance().getFirstProperty("Axis2Config.clientAxis2XmlLocation")));
            if (Boolean.parseBoolean(System.getProperty("run.with.security"))) {
                ((WSRegistryServiceClient) registry).addSecurityOptions(
                        FrameworkSettings.CARBON_HOME + TestSetup.POLICY_FILE_PATH,
                        FrameworkSettings.CARBON_HOME + File.separator +
                                "repository" + File.separator + "resources"
                                + File.separator + "security" + File.separator + "wso2carbon.jks",userName,password);
            }

        } catch (Exception e) {
            e.printStackTrace();
            fail("Failed to authenticate the client. Caused by: " + e.getMessage());
        }
        log.info("WS-API Tests Initialized");
    }

}
