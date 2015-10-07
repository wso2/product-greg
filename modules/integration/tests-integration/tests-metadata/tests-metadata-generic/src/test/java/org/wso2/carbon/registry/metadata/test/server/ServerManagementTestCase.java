/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.registry.metadata.test.server;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import javax.xml.namespace.QName;

import static org.testng.Assert.assertEquals;

public class ServerManagementTestCase extends GREGIntegrationBaseTest {

    private GenericArtifactManager artifactManager;

    @BeforeClass(groups = "wso2.greg", alwaysRun = true)
    public void initialize() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_USER);
        WSRegistryServiceClient wsRegistryServiceClient = new RegistryProviderUtil().getWSRegistry(automationContext);

        Registry governance = new RegistryProviderUtil().getGovernanceRegistry(wsRegistryServiceClient,
                automationContext);
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance,
                GovernanceUtils.findGovernanceArtifactConfigurations(governance));
        artifactManager = new GenericArtifactManager(governance, "server");
    }

    @Test(groups = { "wso2.greg" }, description = "Create a server GenericArtifact")
    public void createServer() throws GovernanceException {
        GenericArtifact artifact = artifactManager.newGovernanceArtifact(new QName("ServerOne"));

        artifact.setAttribute("overview_name", "ServerOne");
        artifact.setAttribute("overview_version", "4.5.0");
        artifact.setAttribute("overview_description", "Description");

        artifactManager.addGenericArtifact(artifact);
        assertEquals(artifactManager.isExists(artifact), true);

        GenericArtifact receivedArtifact = artifactManager.getGenericArtifact(artifact.getId());
        assertEquals(artifact.getAttribute("overview_name"), receivedArtifact.getAttribute("overview_name"),
                " Service name must be equal");

        artifactManager.removeGenericArtifact(artifact.getId());
    }

    @Test(groups = "wso2.greg", alwaysRun = true, description = "Updated Create a server GenericArtifact.")
    public void updateServer() throws GovernanceException {
        GenericArtifact artifact = artifactManager.newGovernanceArtifact(new QName("ServerOne"));

        artifact.setAttribute("overview_name", "ServerOne");
        artifact.setAttribute("overview_version", "4.5.0");
        artifact.setAttribute("overview_description", "Description");

        artifactManager.addGenericArtifact(artifact);

        GenericArtifact receivedArtifact = artifactManager.getGenericArtifact(artifact.getId());
        receivedArtifact.setAttribute("overview_version", "4.6.0");
        receivedArtifact.setAttribute("overview_description", "Description New");

        artifactManager.updateGenericArtifact(receivedArtifact);

        GenericArtifact receivedArtifact2 = artifactManager.getGenericArtifact(receivedArtifact.getId());

        assertEquals(receivedArtifact2.getAttribute("overview_version"), "4.6.0");
        assertEquals(receivedArtifact2.getAttribute("overview_description"), "Description New");

        artifactManager.removeGenericArtifact(artifact.getId());
    }

    @Test(groups = "wso2.greg", alwaysRun = true, description = "Delete server GenericArtifact.")
    public void deleteServer() throws GovernanceException {

        GenericArtifact artifact = artifactManager.newGovernanceArtifact(new QName("ServerOne"));

        artifact.setAttribute("overview_name", "ServerOne");
        artifact.setAttribute("overview_version", "4.5.0");
        artifact.setAttribute("overview_description", "Description");

        artifactManager.addGenericArtifact(artifact);

        GenericArtifact receivedArtifact = artifactManager.getGenericArtifact(artifact.getId());
        assertEquals(artifact.getAttribute("overview_name"), receivedArtifact.getAttribute("overview_name"),
                " Service name must be equal");

        assertEquals(artifactManager.isExists(artifact), true);
        artifactManager.removeGenericArtifact(artifact.getId());
        assertEquals(artifactManager.isExists(artifact), false);
    }
}
