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
package org.wso2.carbon.registry.search.metadata.test.utils;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class GregTestUtils {
    public static WSRegistryServiceClient getRegistry() throws RegistryException, AxisFault {
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

    }

    public static Registry getGovernanceRegistry(WSRegistryServiceClient registry)
            throws RegistryException {
        return GovernanceUtils.getGovernanceUserRegistry(registry, FrameworkSettings.USER_NAME);
    }

    public static String getResourcePath() {
        return FrameworkSettings.getFrameworkPath() + File.separator + ".." + File.separator + ".." + File.separator + ".." +
               File.separator + "src" + File.separator + "test" + File.separator + "java" + File.separator + "resources";
    }

    public static String readFile(String filePath) throws IOException {
        BufferedReader reader = null;
        FileReader fileReader = null;
        StringBuilder stringBuilder;
        String line;
        String ls;
        try {
            stringBuilder = new StringBuilder();
            fileReader = new FileReader(filePath);
            reader = new BufferedReader(fileReader);

            ls = System.getProperty("line.separator");

            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(ls);
            }

        } finally {
            if (fileReader != null) {
                fileReader.close();
            }
            if (reader != null) {
                reader.close();
            }
        }
        return stringBuilder.toString();
    }

    public static String getServerUrl() {
        return "https://" + FrameworkSettings.HOST_NAME +
               ":" + FrameworkSettings.HTTPS_PORT + "/services/";

    }
}
