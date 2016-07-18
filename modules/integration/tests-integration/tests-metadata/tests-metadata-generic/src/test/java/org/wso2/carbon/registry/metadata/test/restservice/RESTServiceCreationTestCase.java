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

public class RESTServiceCreationTestCase extends GREGIntegrationBaseTest {
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
     * Creates a REST service using generic artifact.
     *
     * @throws GovernanceException If fails to create the rest service.
     */
    @Test(groups = { "wso2.greg" }, description = "create REST Service using GenericArtifact")
    public void createRESTService() throws GovernanceException {
        GenericArtifact artifact = artifactManager.newGovernanceArtifact(new QName("org.wso2.test", "RESTService1"));

        artifact.setAttribute("overview_name", "RESTService1");
        artifact.setAttribute("overview_context", "/rs_test");
        artifact.setAttribute("overview_version", "4.5.0");
        artifact.setAttribute("overview_description", "Description");

        artifactManager.addGenericArtifact(artifact);

        GenericArtifact receivedArtifact = artifactManager.getGenericArtifact(artifact.getId());
        assertEquals(artifact.getAttribute("overview_name"), receivedArtifact.getAttribute("overview_name"),
                " Service name must be equal");

        artifactManager.removeGenericArtifact(artifact.getId());
    }

    @Test(groups = { "wso2.greg" }, description = "try add REST service without version",
            dependsOnMethods = "createRESTService", expectedExceptions = GovernanceException.class)
    public void createRESTServiceFault() throws GovernanceException {
        GenericArtifact artifact = artifactManager.newGovernanceArtifact(new QName("RESTService2"));

        artifact.setAttribute("overview_name", "RESTService2");
        artifact.setAttribute("overview_context", "/rs_test");
        artifact.setAttribute("overview_description", "Description");

        artifactManager.addGenericArtifact(artifact);

        GenericArtifact receivedArtifact = artifactManager.getGenericArtifact(artifact.getId());
        assertEquals(artifact.getAttribute("overview_name"), receivedArtifact.getAttribute("overview_name"),
                " Service name must be equal");
    }

    /**
     * Create rest service artifact including swagger url. expected to be having 2 associations for the created
     * service which are swagger resource and the endpoint created.
     *
     * @throws GovernanceException If fails to create the rest service.
     */
    @Test(groups = { "wso2.greg" }, description = "add REST service including swagger url")
    public void createRESTServiceWithSwagger() throws GovernanceException {
        GenericArtifact artifact = artifactManager.newGovernanceArtifact(new QName("RESTService2"));

        artifact.setAttribute("overview_name", "RESTService2");
        artifact.setAttribute("overview_context", "/rs_test");
        artifact.setAttribute("overview_version", "4.5.0");
        artifact.setAttribute("interface_swagger", "http://petstore.swagger.io/v2/swagger.json");
        artifact.setAttribute("overview_description", "Description");

        artifactManager.addGenericArtifact(artifact);

        GenericArtifact receivedArtifact = artifactManager.getGenericArtifact(artifact.getId());
        assertNotNull(receivedArtifact.getDependencies());
        assertEquals(receivedArtifact.getDependencies().length, 2, "Expecting 2 dependencies : Swagger and Endpoint");

        artifactManager.removeGenericArtifact(artifact.getId());
    }

    /**
     * Create rest service artifact including wadl url. expected to be having 2 associations for the created
     * service which are wadl document and the endpoint created.
     *
     * @throws GovernanceException If fails to create the rest service.
     */
    @Test(groups = { "wso2.greg" }, description = "add REST service including wadl url")
    public void createRESTServiceWithWadl() throws GovernanceException {
        GenericArtifact artifact = artifactManager.newGovernanceArtifact(new QName("RESTService3"));

        artifact.setAttribute("overview_name", "RESTService3");
        artifact.setAttribute("overview_context", "/rs_test");
        artifact.setAttribute("overview_version", "1.1.1");
        artifact.setAttribute("interface_wadl",
                "https://raw.githubusercontent.com/wso2/wso2-qa-artifacts/master/automation-artifacts/greg/wadl/SearchSearvice.wadl");
        artifact.setAttribute("overview_description", "Description");

        artifactManager.addGenericArtifact(artifact);

        GenericArtifact receivedArtifact = artifactManager.getGenericArtifact(artifact.getId());
        assertNotNull(receivedArtifact.getDependencies());
        assertEquals(receivedArtifact.getDependencies().length, 2, "Expecting 2 dependencies : WADL and Endpoint");

        artifactManager.removeGenericArtifact(artifact.getId());
    }

    /**
     * Create rest service artifact including both swagger and wadl urls. expected to be having 4 associations
     * for the created service which are swagger and wadl resources and the 2 endpoint created.
     *
     * @throws GovernanceException If fails to create the rest service.
     */
    @Test(groups = { "wso2.greg" }, description = "add REST service including wadl and swagger urls")
    public void createRESTServiceWithWadlAndSwagger() throws GovernanceException {
        GenericArtifact artifact = artifactManager.newGovernanceArtifact(new QName("RESTService3"));

        artifact.setAttribute("overview_name", "RESTService3");
        artifact.setAttribute("overview_context", "/rs_test");
        artifact.setAttribute("overview_version", "1.1.1");
        artifact.setAttribute("interface_wadl",
                "https://raw.githubusercontent.com/wso2/wso2-qa-artifacts/master/automation-artifacts/greg/wadl/SearchSearvice.wadl");
        artifact.setAttribute("interface_swagger", "http://petstore.swagger.io/v2/swagger.json");
        artifact.setAttribute("overview_description", "Description");

        artifactManager.addGenericArtifact(artifact);

        GenericArtifact receivedArtifact = artifactManager.getGenericArtifact(artifact.getId());
        assertNotNull(receivedArtifact.getDependencies());
        assertEquals(receivedArtifact.getDependencies().length, 4,
                "Expecting 4 dependencies : WADL, swagger and and 2 endpoints");

        artifactManager.removeGenericArtifact(artifact.getId());
    }

    /**
     * Create rest service artifact including an endpoint.
     *
     * @throws GovernanceException If fails to create the rest service.
     */
    @Test(groups = { "wso2.greg" }, description = "add REST service with endpoints.")
    public void createRESTServiceWithEndpoints() throws GovernanceException {
        GenericArtifact artifact = artifactManager.newGovernanceArtifact(new QName("RESTService4"));

        artifact.setAttribute("overview_name", "RESTService4");
        artifact.setAttribute("overview_context", "/rs_test");
        artifact.setAttribute("overview_version", "1.0.1");
        artifact.setAttribute("endpoints_entry", "None:http://test.org");
        artifact.setAttribute("endpoints_entry", ":http://test.org");

        artifactManager.addGenericArtifact(artifact);

        GenericArtifact receivedArtifact = artifactManager.getGenericArtifact(artifact.getId());
        assertNotNull(receivedArtifact.getDependencies());
        assertEquals(receivedArtifact.getDependencies().length, 1, "Expecting 1 endpoint dependencies.");

        artifactManager.removeGenericArtifact(artifact.getId());
    }

    /**
     * Create rest service artifact including swagger url and an endpoint. expected to be having 3 associations
     * for the created service which are swagger resource and the endpoint created and also the endpoint added manually.
     *
     * @throws GovernanceException If fails to create the rest service.
     */
    @Test(groups = { "wso2.greg" }, description = "add REST service with endpoints and swagger.")
    public void createRESTServiceWithEndpointsAndSwagger() throws GovernanceException {
        GenericArtifact artifact = artifactManager.newGovernanceArtifact(new QName("RESTService5"));

        artifact.setAttribute("overview_name", "RESTService5");
        artifact.setAttribute("overview_context", "/rs_test");
        artifact.setAttribute("overview_version", "1.0.1");
        artifact.setAttribute("interface_swagger", "http://petstore.swagger.io/v2/swagger.json");
        artifact.setAttribute("endpoints_entry", "None:http://test.org");

        artifactManager.addGenericArtifact(artifact);

        GenericArtifact receivedArtifact = artifactManager.getGenericArtifact(artifact.getId());
        assertNotNull(receivedArtifact.getDependencies());
        assertEquals(receivedArtifact.getDependencies().length, 3, "Expecting 2 endpoint dependencies and swagger.");

        artifactManager.removeGenericArtifact(artifact.getId());
    }

    /**
     * Create rest service artifact including wadl url and an endpoint. expected to be having 3 associations
     * for the created service which are wadl resource and the endpoint created and also the endpoint added manually.
     *
     * @throws GovernanceException If fails to create the rest service.
     */

    @Test(groups = { "wso2.greg" }, description = "add REST service with endpoints and wadl")
    public void createRESTServiceWithEndpointsAndWadl() throws GovernanceException {
        GenericArtifact artifact = artifactManager.newGovernanceArtifact(new QName("RESTService6"));

        artifact.setAttribute("overview_name", "RESTService6");
        artifact.setAttribute("overview_context", "/rs_test");
        artifact.setAttribute("overview_version", "1.4.1");
        artifact.setAttribute("interface_wadl",
                "https://raw.githubusercontent.com/wso2/wso2-qa-artifacts/master/automation-artifacts/greg/wadl/SearchSearvice.wadl");
        artifact.setAttribute("endpoints_entry", "None:http://test.org");

        artifactManager.addGenericArtifact(artifact);

        GenericArtifact receivedArtifact = artifactManager.getGenericArtifact(artifact.getId());
        assertNotNull(receivedArtifact.getDependencies());
        assertEquals(receivedArtifact.getDependencies().length, 3, "Expecting 3 endpoint dependencies and swagger.");

        artifactManager.removeGenericArtifact(artifact.getId());
    }

    /**
     * Create rest service artifact including an invalid swagger url. Expected to throw an exception.
     *
     * @throws GovernanceException If fails to create the rest service.
     */
    @Test(groups = { "wso2.greg" }, description = "add REST service with invalid swagger url.",
            expectedExceptions = GovernanceException.class)
    public void createRESTServiceWithInvalidSwaggerUrl() throws GovernanceException {
        GenericArtifact artifact = artifactManager.newGovernanceArtifact(new QName("RESTService7"));

        artifact.setAttribute("overview_name", "RESTService7");
        artifact.setAttribute("overview_context", "/rs_test");
        artifact.setAttribute("overview_version", "1.4.1");
        artifact.setAttribute("interface_swagger", "http://wso2.com/");

        try {
            artifactManager.addGenericArtifact(artifact);
        } catch (RegistryException e) {
            throw new GovernanceException(e);
        }
    }

    /**
     * Create rest service artifact including an invalid wadl url. Expected to throw an exception.
     *
     * @throws GovernanceException If fails to create the rest service.
     */

    @Test(groups = { "wso2.greg" }, description = "add REST service with invalid wadl url.",
            expectedExceptions = GovernanceException.class)
    public void createRESTServiceWithInvalidWadlUrl() throws GovernanceException {
        GenericArtifact artifact = artifactManager.newGovernanceArtifact(new QName("RESTService7"));

        artifact.setAttribute("overview_name", "RESTService7");
        artifact.setAttribute("overview_context", "/rs_test");
        artifact.setAttribute("overview_version", "1.4.1");
        artifact.setAttribute("interface_wadl", "http://wso2.com/");

        try {
            artifactManager.addGenericArtifact(artifact);
        } catch (RegistryException e) {
            throw new GovernanceException(e);
        }
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
            genericArtifactManager = new GenericArtifactManager(registry, "swagger");
            deleteResources(genericArtifactManager);
            genericArtifactManager = new GenericArtifactManager(registry, "wadl");
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
