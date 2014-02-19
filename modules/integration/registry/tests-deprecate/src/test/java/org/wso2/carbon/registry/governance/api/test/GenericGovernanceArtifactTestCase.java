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

import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceArtifactConfiguration;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.governance.api.test.util.FileManagerUtil;

import javax.xml.namespace.QName;
import java.io.File;

import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertNull;

/**
 * Generic artifact test class - contain test cases for generic artifacts.
 */
public class GenericGovernanceArtifactTestCase {

    private static final String RXT_MEDIA_TYPE = "application/vnd.wso2.registry-ext-type+xml";
    private Registry governance;
    private static final Log log = LogFactory.getLog(GenericGovernanceArtifactTestCase.class);
    private Registry wsRegistry;

    @BeforeClass(groups = {"wso2.greg"})
    public void initTest() throws RegistryException, AxisFault {
        governance = TestUtils.getRegistry();
        TestUtils.cleanupResources(governance);
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
        wsRegistry = TestUtils.getWSRegistry();
    }

    @Test(groups = {"wso2.greg"}, description = "add new rxt file")
    public void testAddNewRxtFile() throws Exception {
        Resource resource = governance.newResource();
        String rxtLocation = "/_system/governance/repository/components/org.wso2.carbon.governance/types/";

        String rxtFilePath = FrameworkSettings.getFrameworkPath() + File.separator + ".." + File.separator + ".." + File.separator + ".."
                             + File.separator + "src" + File.separator + "test" + File.separator + "java" + File.separator
                             + "resources" + File.separator + "rxt" + File.separator;

        resource.setContent(FileManagerUtil.readFile(rxtFilePath + "event.rxt"));

        resource.setMediaType(RXT_MEDIA_TYPE);
        wsRegistry.put(rxtLocation +"event.rxt", resource);
        assertTrue(wsRegistry.resourceExists(rxtLocation +"event.rxt"),
                   "rxt resource doesn't exists");

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

    @Test(groups = {"wso2.greg"}, description = "add new rxt file", enabled = true,dependsOnMethods ="testAddNewRxtFile")
    public void testAddNewGenericArtifact() throws Exception {
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
        GenericArtifactManager artifactManager = new GenericArtifactManager(governance, "events");
        GenericArtifact artifact = artifactManager.newGovernanceArtifact(new QName
                                                                         ("testEvent1"));
        artifact.setAttribute("details_venue", "Colombo");
        artifact.setAttribute("details_date", "12/12/2012");
        artifact.setAttribute("details_name", "code");
        artifact.setAttribute("details_author", "testAuthor");

        artifact.setAttribute("rules_description", "Coding event");
        artifact.setAttribute("rules_gender", "male");
        artifact.setAttribute("serviceLifecycle_lifecycleName", "ServiceLifeCycle");
        artifactManager.addGenericArtifact(artifact);

        artifact = artifactManager.getGenericArtifact(artifact.getId());
        Thread.sleep(3000);
        assertTrue(artifact.getQName().toString().contains("testEvent1"), "artifact name not found");
        assertTrue(artifact.getPath().contains("/events/testEvent1"), "Artifact path not found");
        assertTrue(artifact.getAttribute("details_venue").equals("Colombo"),
                   "artifact venue not found");
        assertTrue(artifact.getAttribute("details_date").contains("12/12/2012"),
                   "Date not found");
        assertTrue(artifact.getAttribute("details_name").contains("testEvent1"),
                   "Artifact detail name not found");
        assertTrue(artifact.getAttribute("details_author").contains("testAuthor"),
                   "Artifact Author not found");
        assertTrue(artifact.getAttribute("rules_description").contains("Coding event"),
                   "Artifact rule description not found");
        assertTrue(artifact.getAttribute("rules_gender").equals("male"),
                   "Artifact field gender not matching");
        assertTrue(artifact.getAttribute("serviceLifecycle_lifecycleName").contains("ServiceLifeCycle"),
                   "Artifact field LC not found");

        GenericArtifact[] artifacts = artifactManager.getAllGenericArtifacts();

        boolean status = false;
        for (GenericArtifact genericArtifact : artifacts) {
            if (genericArtifact.getQName().toString().contains("testEvent1")) {
                status = true;
                log.info("Generic artifact found - " + genericArtifact.getQName().toString());
                assertTrue(artifact.getQName().toString().contains("testEvent1"), "artifact name not found");
                assertTrue(artifact.getPath().contains("/events/testEvent1"),
                           "Artifact path not found");
                assertTrue(artifact.getAttribute("details_venue").equals("Colombo"),
                           "artifact venue not found");
                assertTrue(artifact.getAttribute("details_date").contains("12/12/2012"),
                           "Date not found");
                assertTrue(artifact.getAttribute("details_name").contains("testEvent1"),
                           "Artifact detail name not found");
                assertTrue(artifact.getAttribute("details_author").contains("testAuthor"),
                           "Artifact Author not found");
                assertTrue(artifact.getAttribute("rules_description").contains("Coding event"),
                           "Artifact rule description not found");
                assertTrue(artifact.getAttribute("rules_gender").equals("male"),
                           "Artifact field gender not matching");
                assertTrue(artifact.getAttribute("serviceLifecycle_lifecycleName").contains("ServiceLifeCycle"),
                           "Artifact field LC not found");
            }
        }
        assertTrue(status, "Artifact not found");
    }

    @Test(groups = {"wso2.greg"}, description = "add new rxt file", enabled = true,dependsOnMethods ="testAddNewRxtFile")
    public void testAddAnotherGenericArtifact() throws Exception {
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
        GenericArtifactManager artifactManager = new GenericArtifactManager(governance, "events");
        String governanceArtifactContent = "<metadata xmlns=\"http://www.wso2" +
                                           ".org/governance/metadata\"><details><author>testAuthor" +
                                           "</author>" + "<venue>Colombo</venue><date>12/12/2012</date>" +
                                           "<name>testEvent2</name>" + "</details><overview><namespace>" +
                                           "</namespace></overview><serviceLifecycle>" +
                                           "<lifecycleName>ServiceLifeCycle</lifecycleName>" +
                                           "</serviceLifecycle><rules>" + "<gender>male</gender>" +
                                           "<description>Coding event</description></rules></metadata>";

        GenericArtifact artifact = artifactManager.newGovernanceArtifact(AXIOMUtil.stringToOM(governanceArtifactContent));


        artifactManager.addGenericArtifact(artifact);
        artifact = artifactManager.getGenericArtifact(artifact.getId());

        assertTrue(artifact.getQName().toString().contains("testEvent2"),
                   "artifact name not found");
        assertTrue(artifact.getPath().contains("/events/testEvent2"), "Artifact path not found");
        assertTrue(artifact.getAttribute("details_venue").equals("Colombo"),
                   "artifact venue not found");
        assertTrue(artifact.getAttribute("details_date").contains("12/12/2012"),
                   "Date not found");
        assertTrue(artifact.getAttribute("details_name").contains("testEvent2"),
                   "Artifact detail name not found");
        assertTrue(artifact.getAttribute("details_author").contains("testAuthor"),
                   "Artifact Author not found");
        assertTrue(artifact.getAttribute("rules_description").contains("Coding event"),
                   "Artifact rule description not found");
        assertTrue(artifact.getAttribute("rules_gender").equals("male"),
                   "Artifact field gender not matching");
        assertTrue(artifact.getAttribute("serviceLifecycle_lifecycleName").contains("ServiceLifeCycle"),
                   "Artifact field artifact.getDependencies()LC not found");

        GenericArtifact[] artifacts = artifactManager.getAllGenericArtifacts();

        boolean status = false;
        for (GenericArtifact genericArtifact : artifacts) {
            if (genericArtifact.getQName().toString().contains("testEvent2")) {
                status = true;
                log.info("Generic artifact found - " + genericArtifact.getQName().toString());
                assertTrue(artifact.getQName().toString().contains("testEvent2"),
                           "artifact name not found");
                assertTrue(artifact.getPath().contains("/events/testEvent2"), "Artifact path not found");
                assertTrue(artifact.getAttribute("details_venue").equals("Colombo"),
                           "artifact venue not found");
                assertTrue(artifact.getAttribute("details_date").contains("12/12/2012"),
                           "Date not found");
                assertTrue(artifact.getAttribute("details_name").contains("testEvent2"),
                           "Artifact detail name not found");
                assertTrue(artifact.getAttribute("details_author").contains("testAuthor"),
                           "Artifact Author not found");
                assertTrue(artifact.getAttribute("rules_description").contains("Coding event"),
                           "Artifact rule description not found");
                assertTrue(artifact.getAttribute("rules_gender").equals("male"),
                           "Artifact field gender not matching");
                assertTrue(artifact.getAttribute("serviceLifecycle_lifecycleName").contains("ServiceLifeCycle"),
                           "Artifact field LC not found");
            }
        }
        assertTrue(status, "Artifact not found");
        artifactManager.removeGenericArtifact(artifact.getId());
        assertNull("Generic artifact exists even after deletion",
                   artifactManager.getGenericArtifact(artifact.getId()));
    }

    @Test(groups = {"wso2.greg"}, description = "add new rxt file", enabled = true,dependsOnMethods ="testAddNewRxtFile")
    public void testAddInvalidContent() throws Exception {
        boolean status = false;
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
        GenericArtifactManager artifactManager = new GenericArtifactManager(governance, "events");
        String governanceArtifactContent = "<metadata xmlns=\"http://www.wso2.org/governance/metadata\">" +
                                           "<overview><name>UserA</name></overview></metadata>";

        try {
            GenericArtifact artifact = artifactManager.
                    newGovernanceArtifact(AXIOMUtil.stringToOM(governanceArtifactContent));
            artifactManager.addGenericArtifact(artifact);
        } catch (GovernanceException ignored) {
            log.info("Governance Exception thrown for invalid content");
            status = true;
        }
        assertTrue(status, "Add Artifact - Invalid content added ");
    }

    @Test(groups = {"wso2.greg"}, description = "Test artifact dependencies and associations",
          enabled = true,dependsOnMethods ="testAddNewRxtFile")
    public void testArtifactDependencies() throws Exception {
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
        GenericArtifactManager artifactManager = new GenericArtifactManager(governance, "events");
        GenericArtifact artifact = artifactManager.newGovernanceArtifact(new QName
                                                                         ("testEvent5"));
        artifact.setAttribute("details_venue", "Colombo");
        artifact.setAttribute("details_date", "12/12/2012");
        artifact.setAttribute("details_name", "code");
        artifact.setAttribute("details_author", "testAuthor");

        artifact.setAttribute("rules_description", "Coding event");
        artifact.setAttribute("rules_gender", "male");
        artifact.setAttribute("serviceLifecycle_lifecycleName", "ServiceLifeCycle");
        artifactManager.addGenericArtifact(artifact);

        //add another artifact
        GenericArtifact artifactSecond = artifactManager.newGovernanceArtifact(new QName
                                                                               ("testEvent6"));
        artifactSecond.setAttribute("details_venue", "Colombo");
        artifactSecond.setAttribute("details_date", "12/12/2012");
        artifactSecond.setAttribute("details_name", "code");
        artifactSecond.setAttribute("details_author", "testAuthor");

        artifactSecond.setAttribute("rules_description", "Coding event");
        artifactSecond.setAttribute("rules_gender", "male");
        artifactSecond.setAttribute("serviceLifecycle_lifecycleName", "ServiceLifeCycle");

        artifactManager.addGenericArtifact(artifactSecond);

        governance.addAssociation(artifact.getPath(), artifactSecond.getPath(), "depends");
        governance.addAssociation(artifact.getPath(), artifactSecond.getPath(), "usedBy");

        assertTrue(governance.getAssociations(artifact.getPath(), "depends").length > 0);
        assertTrue(governance.getAssociations(artifact.getPath(), "usedBy").length > 0);

        assertTrue(artifact.getDependents().length == 1);
        assertTrue(artifact.getDependencies().length == 1);

        assertTrue(artifact.getDependencies()[0].getQName().toString().contains("testEvent6"));
        assertTrue(artifact.getDependents()[0].getQName().toString().contains("testEvent6"));

        artifactManager.removeGenericArtifact(artifact.getId());
        assertNull("artifact exists", artifactManager.getGenericArtifact(artifact.getId()));
    }

    @Test(groups = {"wso2.greg"}, description = "Get LC state and LC name",
          enabled = true,dependsOnMethods ="testAddNewRxtFile")
    public void testArtifactLifeCycle() throws Exception {
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
        GenericArtifactManager artifactManager = new GenericArtifactManager(governance, "events");
        GenericArtifact artifact = artifactManager.newGovernanceArtifact(new QName
                                                                         ("testEvent7"));
        artifact.setAttribute("details_venue", "Colombo");
        artifact.setAttribute("details_date", "12/12/2012");
        artifact.setAttribute("details_name", "code");
        artifact.setAttribute("details_author", "testAuthor");

        artifact.setAttribute("rules_description", "Coding event");
        artifact.setAttribute("rules_gender", "male");
        artifact.setAttribute("serviceLifecycle_lifecycleName", "ServiceLifeCycle");
        artifactManager.addGenericArtifact(artifact);
        Thread.sleep(5000);

        artifact = artifactManager.getGenericArtifact(artifact.getId());
        assertTrue(artifact.getQName().toString().contains("testEvent7"), "artifact name not found");
        assertTrue(artifact.getPath().contains("/events/testEvent7"), "Artifact path not found");
        assertTrue(artifact.getAttribute("details_venue").equals("Colombo"),
                   "artifact venue not found");
        assertTrue(artifact.getAttribute("details_date").contains("12/12/2012"),
                   "Date not found");
        assertTrue(artifact.getAttribute("details_name").contains("testEvent7"),
                   "Artifact detail name not found");
        assertTrue(artifact.getAttribute("details_author").contains("testAuthor"),
                   "Artifact Author not found");
        assertTrue(artifact.getAttribute("rules_description").contains("Coding event"),
                   "Artifact rule description not found");
        assertTrue(artifact.getAttribute("rules_gender").equals("male"),
                   "Artifact field gender not matching");
        artifact.attachLifecycle("ServiceLifeCycle");
        artifactManager.updateGenericArtifact(artifact);
        assertTrue(artifact.getLifecycleName().contains("ServiceLifeCycle"), "LC name not found");
        assertTrue(artifact.getLifecycleState().contains("Development"), "LC state not found");

        artifactManager.removeGenericArtifact(artifact.getId());
        assertNull("artifact exists", artifactManager.getGenericArtifact(artifact.getId()));
    }

    @Test(groups = {"wso2.greg"}, description = "Get LC state and LC name", enabled = true,dependsOnMethods ="testAddNewRxtFile")
    public void testUpdateArtifact() throws Exception {
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
        GenericArtifactManager artifactManager = new GenericArtifactManager(governance, "events");
        GenericArtifact artifact = artifactManager.newGovernanceArtifact(new QName
                                                                         ("testEvent8"));
        artifact.setAttribute("details_venue", "Colombo");
        artifact.setAttribute("details_date", "12/12/2012");
        artifact.setAttribute("details_name", "code");
        artifact.setAttribute("details_author", "testAuthor");

        artifact.setAttribute("rules_description", "Coding event");
        artifact.setAttribute("rules_gender", "male");
        artifact.setAttribute("serviceLifecycle_lifecycleName", "ServiceLifeCycle");
        artifactManager.addGenericArtifact(artifact);

        artifact = artifactManager.getGenericArtifact(artifact.getId());
        Thread.sleep(5000);
        assertTrue(artifact.getQName().toString().contains("testEvent8"),
                   "artifact name not found");
        assertTrue(artifact.getPath().contains("/events/testEvent8"),
                   "Artifact path not found");
        assertTrue(artifact.getAttribute("details_venue").equals("Colombo"),
                   "artifact venue not found");
        assertTrue(artifact.getAttribute("details_date").contains("12/12/2012"),
                   "Date not found");
        assertTrue(artifact.getAttribute("details_name").contains("testEvent8"),
                   "Artifact detail name not found");
        assertTrue(artifact.getAttribute("details_author").contains("testAuthor"),
                   "Artifact Author not found");
        assertTrue(artifact.getAttribute("rules_description").contains("Coding event"),
                   "Artifact rule description not found");
        assertTrue(artifact.getAttribute("rules_gender").equals("male"),
                   "Artifact field gender not matching");
    }
}
