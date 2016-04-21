/*
* Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.governance.api.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceArtifactConfiguration;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.integration.common.utils.FileManager;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import javax.xml.namespace.QName;
import java.io.File;

import static org.testng.Assert.assertTrue;

/**
 * Generic artifact test class - contain test cases for generic artifacts.
 */
public class GenericArtifactTestCase extends GREGIntegrationBaseTest {

    private static final String RXT_MEDIA_TYPE = "application/vnd.wso2.registry-ext-type+xml";
    private Registry governance;
    private static final Log log = LogFactory.getLog(GenericArtifactTestCase.class);
    private WSRegistryServiceClient wsRegistry;

    @BeforeClass(alwaysRun = true)
    public void initTest() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        wsRegistry =
                new RegistryProviderUtil().getWSRegistry(automationContext);
        governance = new RegistryProviderUtil().getGovernanceRegistry(wsRegistry, automationContext);
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
    }

    @Test(groups = {"wso2.greg"}, description = "add new rxt file")
    public void testAddNewRxtFile() throws Exception {
        Resource resource = governance.newResource();
        String rxtLocation = "/_system/governance/repository/components/org.wso2.carbon.governance/types/";

        String rxtFilePath = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" +
                File.separator + "GREG" + File.separator + "rxt" + File.separator;

        resource.setContent(FileManager.readFile(rxtFilePath + "event.rxt"));

        resource.setMediaType(RXT_MEDIA_TYPE);
        wsRegistry.put(rxtLocation + "event.rxt", resource);
        assertTrue(wsRegistry.resourceExists(rxtLocation + "event.rxt"),
                "rxt resource doesn't exists");

        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance, GovernanceUtils.findGovernanceArtifactConfigurations(governance));


        GovernanceArtifactConfiguration governanceArtifactConfiguration =
                GovernanceUtils.findGovernanceArtifactConfiguration("events", governance);

        assertTrue(governanceArtifactConfiguration.getMediaType().contains("application/vnd" +
                ".wso2-events+xml"));
        assertTrue(governanceArtifactConfiguration.getContentDefinition().toString().contains
                ("<table name=\"Rules\">\n" +
                        "                <field type=\"options\">\n" +
                        "                    <name>Gender</name>\n" +
                        "                    <values>\n" +
                        "                        <value>male</value>\n" +
                        "                        <value>female</value>\n" +
                        "                    </values>\n" +
                        "                </field>\n" +
                        "                <field type=\"text-area\" required=\"true\">\n" +
                        "                    <name>Description</name>\n" +
                        "                </field>\n" +
                        "                <field type=\"text\">\n" +
                        "                    <name>Auther</name>\n" +
                        "                </field>\n" +
                        "            </table>"));

        assertTrue(governanceArtifactConfiguration.getPathExpression().contains("/events/@{details_name}"));
        assertTrue(governanceArtifactConfiguration.getArtifactElementNamespace().
                contains("http://www.wso2.org/governance/metadata"));
        assertTrue(governanceArtifactConfiguration.getKey().equals("events"));
    }

    @Test(groups = {"wso2.greg"}, description = "add an artefact with multiple longer lines", enabled = true, dependsOnMethods = "testAddNewRxtFile")
    public void testAddNewGenericArtifactWithMultilineTextForTextField() throws Exception {
    	GovernanceUtils.loadGovernanceArtifacts((UserRegistry)governance,
    	                GovernanceUtils.findGovernanceArtifactConfigurations(governance));
        GenericArtifactManager artifactManager = new GenericArtifactManager(governance, "events");
        GenericArtifact artifact = artifactManager.newGovernanceArtifact(new QName
                ("multiLineEvent1"));
        artifact.setAttribute("details_venue", "Colombo");
        artifact.setAttribute("details_date", "12/12/2012");
        artifact.setAttribute("details_name", "code");
        artifact.setAttribute("details_author", "testAuthor");

        artifact.setAttribute("rules_description", 
        		"0123456789012345678901234567890123456789012345678901234567890120123456789012 More\r\n" + 
        		"012345678901234567890123456789012345678901234567890123456789012\r\n" + 
        	    "012345678901234567890123456789012345678901234567890123456789012 last\r\n");
        	            
        artifact.setAttribute("rules_gender", "male");
        artifact.setAttribute("serviceLifecycle_lifecycleName", "ServiceLifeCycle");
        artifactManager.addGenericArtifact(artifact);

        artifact = artifactManager.getGenericArtifact(artifact.getId());
        Thread.sleep(3000);
        assertTrue(artifact.getQName().toString().contains("multiLineEvent1"), "artifact name not found");

        assertTrue(artifact.getAttribute("details_venue").contains("Colombo"),
                "Artifact venue is not found");

        String description = artifact.getAttribute("rules_description");
        
        assertTrue(description.contains("0123456789012345678901234567890123456789012345678901234567890120123456789012 More") &&
        		description.contains("last"),
                "Artifact rule description not found");
    }

    @Test(groups = {"wso2.greg"}, description = "add an artefact with multiple longer lines",
            enabled = true, dependsOnMethods = "testAddNewGenericArtifactWithMultilineTextForTextField")
    public void testGetDependencies() throws RegistryException {
        GenericArtifactManager artifactManager = new GenericArtifactManager(governance, "events");
        GenericArtifact artifact1 = artifactManager.getAllGenericArtifacts()[0];

        GenericArtifact artifact2 = artifactManager.newGovernanceArtifact(new QName
                ("multiLineEvent2"));
        artifact2.setAttribute("details_venue", "Colombo");
        artifact2.setAttribute("details_date", "12/12/2012");
        artifact2.setAttribute("details_name", "code");
        artifact2.setAttribute("details_author", "testAuthor");
        artifact2.setAttribute("rules_description", "description is mandatory");
        artifactManager.addGenericArtifact(artifact2);

        governance.addAssociation(artifact1.getPath(), artifact2.getPath(), "depends");
        governance.addAssociation(artifact2.getPath(), artifact1.getPath(), "usedBy");

        GovernanceArtifact[] dependencies = artifact1.getDependencies();
        Assert.assertEquals(dependencies.length, 1, "Number of dependencies expected was incorrect");
        GovernanceArtifact dependency = dependencies[0];
        Assert.assertEquals(dependency.getQName().getLocalPart(), "multiLineEvent2", "Expected dependency artifact not found");

        GovernanceArtifact[] dependents = artifact2.getDependents();
        Assert.assertEquals(dependents.length, 1, "Number of dependencies expected was incorrect");
        GovernanceArtifact dependent = dependents[0];
        Assert.assertEquals(dependent.getQName().getLocalPart(), "multiLineEvent1", "Expected dependent artifact not found");
    }


    @AfterClass()
    public void endGame() throws RegistryException {

        GenericArtifactManager artifactManager = new GenericArtifactManager(governance, "events");
        GenericArtifact[] artifacts = artifactManager.getAllGenericArtifacts();
        for (GenericArtifact genericArtifact : artifacts) {
            artifactManager.removeGenericArtifact(genericArtifact.getId());
        }
        String rxtLocation = "/_system/governance/repository/components/org.wso2.carbon.governance/types/";
        wsRegistry.delete(rxtLocation + "event.rxt");

        wsRegistry = null;
        governance = null;
    }
}
