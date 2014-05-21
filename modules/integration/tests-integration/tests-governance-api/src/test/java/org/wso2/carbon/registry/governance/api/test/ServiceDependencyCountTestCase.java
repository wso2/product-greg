/**
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.registry.governance.api.test;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import javax.xml.namespace.QName;


public class ServiceDependencyCountTestCase extends GREGIntegrationBaseTest {
    GenericArtifactManager artifactManager;
    GenericArtifact service1, service2;

    @Test(groups = {"wso2.greg"}, description = "Dependency Verification")
    public void testServiceDependencyCount() throws Exception {

        WSRegistryServiceClient wsRegistry =
                new RegistryProviderUtil().getWSRegistry(automationContext);

        Registry registry = GovernanceUtils.getGovernanceUserRegistry(wsRegistry, "admin");
        ServiceManager serviceManager = new ServiceManager(registry);
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) registry);

        //"service" is the short name of the RXT
        artifactManager = new GenericArtifactManager(registry, "service");

        service1 =
                artifactManager.newGovernanceArtifact(new QName("http://dp.com", "S1-REGISTRY-1595"));
        service1.addAttribute("overview_version", "1.1.1");
        artifactManager.addGenericArtifact(service1);

        service2 =
                artifactManager.newGovernanceArtifact(new QName("http://dp.com", "S2-REGISTRY-1595"));
        service1.addAttribute("overview_version", "1.1.2");
        artifactManager.addGenericArtifact(service2);

        wsRegistry.addAssociation("/_system/governance/trunk/services/com/dp/S1-REGISTRY-1595",
                "/_system/governance/trunk/services/com/dp/S2-REGISTRY-1595", "depends");

        wsRegistry.addAssociation("/_system/governance/trunk/services/com/dp/S2-REGISTRY-1595",
                "/_system/governance/trunk/services/com/dp/S1-REGISTRY-1595", "depends");

        boolean isCorrectCount = false;
        GenericArtifact[] artifacts = artifactManager.getAllGenericArtifacts();
        for (GenericArtifact artifact : artifacts) {
            if (artifact.getAttribute("overview_name").equals("S1-REGISTRY-1595")) {

                GovernanceArtifact artifact1[] = artifact.getDependencies();
                if ((artifact1.length == 1)) {
                     isCorrectCount =true;
                }
            }
        }
        Assert.assertTrue(isCorrectCount, "dependency count is not correct");

    }

    @AfterClass(alwaysRun = true)
    public void endTest() throws RegistryException {
        artifactManager.removeGenericArtifact(service1.getId());
        artifactManager.removeGenericArtifact(service2.getId());
    }
}

