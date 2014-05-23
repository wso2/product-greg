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
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.integration.common.admin.client.LogViewerClient;
import org.wso2.carbon.integration.common.utils.FileManager;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import java.io.File;
import java.io.IOException;

public class Carbon8868TestCase extends GREGIntegrationBaseTest {

    private Registry governance;
    private LogViewerClient logViewerClient;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_USER);
        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        WSRegistryServiceClient registry = registryProviderUtil.getWSRegistry(automationContext);
        governance = registryProviderUtil.getGovernanceRegistry(registry, automationContext);
        logViewerClient = new LogViewerClient(backendURL, getSessionCookie());
    }

    @Test(groups = "wso2.greg", description = "Add Invalid WSDL", expectedExceptions = OMException.class)
    public void addInvalidWSDL() throws IOException, RegistryException {
        Wsdl wsdl;
        WsdlManager wsdlManager = new WsdlManager(governance);
        String wsdlFilePath = getTestArtifactLocation() + "artifacts" + File.separator +
                "GREG" + File.separator + "wsdl" + File.separator + "AutomatedInvalidWSDL.wsdl";
        try {
            wsdl = wsdlManager.newWsdl(FileManager.readFile(wsdlFilePath).getBytes(), "AutomatedInvalidWSDL.wsdl");
            wsdlManager.addWsdl(wsdl);

        } catch(Exception e) {
            if(e.toString().contains("com.ctc.wstx.exc.WstxParsingException: Undeclared namespace " + "prefix \"s\"")) {
                throw new OMException();

            }
        }
    }
    @AfterClass
    public void cleanup() {
        governance = null;
        logViewerClient = null;
    }
}
