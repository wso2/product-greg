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

import org.apache.axiom.om.OMException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.logging.LogViewerClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.core.utils.fileutils.FileManager;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import java.io.File;
import java.io.IOException;

public class Carbon8868 {

    private int userId = 2;
    UserInfo userInfo = UserListCsvReader.getUserInfo(userId);
    private Registry governance;
    private LogViewerClient logViewerClient;

    @BeforeClass
    public void init() throws Exception {

        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        ManageEnvironment environment = builder.build();

        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        WSRegistryServiceClient registry = registryProviderUtil.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        governance = registryProviderUtil.getGovernanceRegistry(registry, userId);
        logViewerClient = new LogViewerClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                              environment.getGreg().getSessionCookie());
    }

    @Test(groups = "wso2.greg", description = "Add Invalid WSDL", expectedExceptions = OMException.class)
    public void addInvalidWSDL() throws IOException, RegistryException {

        Wsdl wsdl;
        WsdlManager wsdlManager = new WsdlManager(governance);

        String wsdlFilePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator +
                              "GREG" + File.separator + "wsdl" + File.separator + "AutomatedInvalidWSDL.wsdl";

        try {
            wsdl = wsdlManager.newWsdl(FileManager.readFile(wsdlFilePath).getBytes(), "AutomatedInvalidWSDL.wsdl");
            wsdlManager.addWsdl(wsdl);

        } catch (Exception e) {
            if (e.toString().contains("com.ctc.wstx.exc.WstxParsingException: Undeclared namespace " + "prefix \"s\"")) {
                throw new OMException();

            }

        }
    }

    @AfterClass
    public void cleanup() {
        userInfo = null;
        governance = null;
        logViewerClient = null;
    }
}
