/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.registry.metadata.test.restservice;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import javax.xml.namespace.QName;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class RESTServiceUpdateTestCase extends GREGIntegrationBaseTest {
    private GenericArtifactManager artifactManager;
    private Registry registry;

    /**
     * This method used to init the REST Service addition test cases.
     *
     * @throws Exception
     */
    @BeforeClass(groups = { "wso2.greg" }, alwaysRun = true)
    public void init() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        WSRegistryServiceClient wsRegistryServiceClient = new RegistryProviderUtil().getWSRegistry(automationContext);

        registry = new RegistryProviderUtil().getGovernanceRegistry(wsRegistryServiceClient, automationContext);
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) registry,
                GovernanceUtils.findGovernanceArtifactConfigurations(registry));
        artifactManager = new GenericArtifactManager(registry, "restservice");
    }

    /**
     * Creates an rest service without any interface urls and then update it by adding a swagger url.
     * Expected to have two dependencies after update.
     *
     * @throws GovernanceException
     */
    @Test(groups = { "wso2.greg" }, description = "Update REST Service add swagger url")
    public void updateRESTServiceWithSwagger() throws GovernanceException {
        String artifactId = createArtifact("RestService1");
        GenericArtifact artifact = artifactManager.getGenericArtifact(artifactId);
        artifact.setAttribute("interface_swagger", "http://petstore.swagger.io/v2/swagger.json");

        artifactManager.updateGenericArtifact(artifact);

        GenericArtifact receivedArtifact = artifactManager.getGenericArtifact(artifact.getId());
        assertNotNull(receivedArtifact.getDependencies());
        assertEquals(receivedArtifact.getDependencies().length, 2, "Expecting 2 dependencies : Swagger and Endpoint");

        artifactManager.removeGenericArtifact(artifactId);
    }

    /**
     * Creates an rest service without any interface urls and then update it by adding a wadl url.
     * Expected to have two dependencies after update.
     *
     * @throws GovernanceException
     */
    @Test(groups = { "wso2.greg" }, description = "Update REST Service add wadl url")
    public void updateRESTServiceWithWadl() throws GovernanceException {
        String artifactId = createArtifact("RestService2");
        GenericArtifact artifact = artifactManager.getGenericArtifact(artifactId);
        artifact.setAttribute("interface_swagger", "");
        artifact.setAttribute("interface_wadl",
                "https://raw.githubusercontent.com/wso2/wso2-qa-artifacts/master/automation-artifacts/greg/wadl/SearchSearvice.wadl");

        artifactManager.updateGenericArtifact(artifact);

        GenericArtifact receivedArtifact = artifactManager.getGenericArtifact(artifact.getId());
        assertNotNull(receivedArtifact.getDependencies());
        assertEquals(receivedArtifact.getDependencies().length, 2, "Expecting 2 dependencies : Wadl and Endpoint");

        artifactManager.removeGenericArtifact(artifactId);
    }

    /**
     * Creates an rest service without any interface urls and then update it by adding a swagger and a wadl url.
     * Expected to have four dependencies after update.
     *
     * @throws GovernanceException
     */
    @Test(groups = { "wso2.greg" }, description = "Update REST Service add both swagger and wadl url.")
    public void updateRESTServiceWithWadlAndSwagger() throws GovernanceException {
        String artifactId = createArtifact("RestService3");
        GenericArtifact artifact = artifactManager.getGenericArtifact(artifactId);
        artifact.setAttribute("interface_swagger", "http://petstore.swagger.io/v2/swagger.json");
        artifact.setAttribute("interface_wadl",
                "https://raw.githubusercontent.com/wso2/wso2-qa-artifacts/master/automation-artifacts/greg/wadl/SearchSearvice.wadl");

        artifactManager.updateGenericArtifact(artifact);

        GenericArtifact receivedArtifact = artifactManager.getGenericArtifact(artifact.getId());
        assertNotNull(receivedArtifact.getDependencies());
        assertEquals(receivedArtifact.getDependencies().length, 4, "Expecting 2 dependencies : Wadl and Endpoint");

        artifactManager.removeGenericArtifact(artifactId);
    }

    /**
     * Creates an rest service without any interface urls and then update it by adding a swagger url.
     * After that revert the change. Expected to have the endpoint artifact but not the swagger document.
     *
     * @throws GovernanceException
     */
    @Test(groups = { "wso2.greg" }, description = "Update REST Service add swagger and then remove.")
    public void updateRESTServiceAddSwaggerAndRemove() throws GovernanceException {
        String artifactId = createArtifact("RestService4");
        GenericArtifact artifact = artifactManager.getGenericArtifact(artifactId);
        artifact.setAttribute("interface_swagger", "http://petstore.swagger.io/v2/swagger.json");

        artifactManager.updateGenericArtifact(artifact);

        GenericArtifact receivedArtifact = artifactManager.getGenericArtifact(artifact.getId());
        assertNotNull(receivedArtifact.getDependencies());
        assertEquals(receivedArtifact.getDependencies().length, 2, "Expecting 2 dependencies : Swagger and Endpoint");

        receivedArtifact.removeAttribute("interface_swagger");

        artifactManager.updateGenericArtifact(receivedArtifact);

        receivedArtifact = artifactManager.getGenericArtifact(receivedArtifact.getId());
        assertEquals(receivedArtifact.getDependencies().length, 1, "Only endpoint dependency is expected.");

        artifactManager.removeGenericArtifact(receivedArtifact.getId());
    }

    /**
     * Creates an rest service without any interface urls and then update it by adding a wadl url.
     * After that revert the change. Expected to have the endpoint artifact but not the wadl document.
     *
     * @throws GovernanceException
     */

    @Test(groups = { "wso2.greg" }, description = "Update REST Service add wadl and then remove.")
    public void updateRESTServiceAddWadlAndRemove() throws GovernanceException {
        String artifactId = createArtifact("RestService5");
        GenericArtifact artifact = artifactManager.getGenericArtifact(artifactId);
        artifact.setAttribute("interface_wadl",
                "https://raw.githubusercontent.com/wso2/wso2-qa-artifacts/master/automation-artifacts/greg/wadl/SearchSearvice.wadl");


        artifactManager.updateGenericArtifact(artifact);

        GenericArtifact receivedArtifact = artifactManager.getGenericArtifact(artifact.getId());
        assertNotNull(receivedArtifact.getDependencies());
        assertEquals(receivedArtifact.getDependencies().length, 2, "Expecting 2 dependencies : Wadl and Endpoint");

        receivedArtifact.removeAttribute("interface_wadl");

        artifactManager.updateGenericArtifact(receivedArtifact);

        receivedArtifact = artifactManager.getGenericArtifact(receivedArtifact.getId());
        assertEquals(receivedArtifact.getDependencies().length, 1, "Only endpoint dependency is expected.");

        artifactManager.removeGenericArtifact(receivedArtifact.getId());
    }

    /**
     * Creates an rest service without any interface urls and then update it by adding a wadl url.
     * Then again update it by adding a swagger url. Expected to be have 4 dependencies after the second update.
     *
     * @throws GovernanceException
     */

    @Test(groups = { "wso2.greg" }, description = "Update REST Service add wadl and then add swagger.")
    public void updateRESTServiceAddWadlAndSwagger() throws GovernanceException {
        String artifactId = createArtifact("RestService6");
        GenericArtifact artifact = artifactManager.getGenericArtifact(artifactId);
        artifact.setAttribute("interface_wadl",
                "https://raw.githubusercontent.com/wso2/wso2-qa-artifacts/master/automation-artifacts/greg/wadl/SearchSearvice.wadl");


        artifactManager.updateGenericArtifact(artifact);

        GenericArtifact receivedArtifact = artifactManager.getGenericArtifact(artifact.getId());
        assertNotNull(receivedArtifact.getDependencies());
        assertEquals(receivedArtifact.getDependencies().length, 2, "Expecting 2 dependencies : Wadl and Endpoint");

        receivedArtifact.setAttribute("interface_swagger", "http://petstore.swagger.io/v2/swagger.json");

        artifactManager.updateGenericArtifact(receivedArtifact);

        receivedArtifact = artifactManager.getGenericArtifact(receivedArtifact.getId());
        assertEquals(receivedArtifact.getDependencies().length, 4,
                "Expecting 4 dependencies : Wadl Swagger and 2 Endpoints");

        artifactManager.removeGenericArtifact(receivedArtifact.getId());
    }

    /**
     * Cleans up resources after the tests finishes.
     *
     * @throws GovernanceException
     */
    @AfterClass(groups = { "wso2.greg" })
    public void cleanup() throws GovernanceException {
        try {
            GenericArtifactManager genericArtifactManager = new GenericArtifactManager(registry, "restservice");
            deleteResources(genericArtifactManager);
            genericArtifactManager = new GenericArtifactManager(registry, "wadl");
            deleteResources(genericArtifactManager);
            genericArtifactManager = new GenericArtifactManager(registry, "swagger");
            deleteResources(genericArtifactManager);
            genericArtifactManager = new GenericArtifactManager(registry, "endpoint");
            deleteResources(genericArtifactManager);
            genericArtifactManager = new GenericArtifactManager(registry, "schema");
            deleteResources(genericArtifactManager);
        } catch (RegistryException e) {
            throw new GovernanceException("Error in cleaning up resources. GenericArtifactManager cannot be created. ", e);
        } finally {
            artifactManager = null;
        }
    }

    /**
     * Creates an rest service artifact and adds to the registry.
     *
     * @param serviceName           service name.
     * @return                      artifact id.
     * @throws GovernanceException  If fails to create a rest service artifact.
     */
    private String createArtifact(String serviceName) throws GovernanceException {
        GenericArtifact artifact = artifactManager.newGovernanceArtifact(new QName("org.wso2.test", serviceName));

        artifact.setAttribute("overview_name", serviceName);
        artifact.setAttribute("overview_context", "/rs_test");
        artifact.setAttribute("overview_version", "4.5.0");
        artifact.setAttribute("overview_description", "Description");

        artifactManager.addGenericArtifact(artifact);
        return artifact.getId();
    }

    /**
     * Removes created resources from the registry
     *
     * @param genericArtifactManager    generic artifact manager
     * @throws GovernanceException      If fails to remove resources
     */
    private void deleteResources(GenericArtifactManager genericArtifactManager) throws GovernanceException {
        GenericArtifact[] genericArtifacts = genericArtifactManager.getAllGenericArtifacts();
        for(GenericArtifact genericArtifact : genericArtifacts) {
            genericArtifactManager.removeGenericArtifact(genericArtifact);
        }
    }
}
