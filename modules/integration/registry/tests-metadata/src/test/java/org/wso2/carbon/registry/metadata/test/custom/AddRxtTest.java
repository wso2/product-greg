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

package org.wso2.carbon.registry.metadata.test.custom;

import org.apache.axis2.AxisFault;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.authenticator.stub.LogoutAuthenticationExceptionException;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;


public class AddRxtTest {

    int userId = 2;
    private Registry governance;


    @BeforeClass
    public void initialize()
            throws LoginAuthenticationExceptionException, RemoteException, RegistryException {

        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        WSRegistryServiceClient registry = registryProviderUtil.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        governance = registryProviderUtil.getGovernanceRegistry(registry, userId);

    }

    @Test(groups = "wso2.greg", description = "Add resource")
    public void testAddResource()
            throws RemoteException, MalformedURLException, ResourceAdminServiceExceptionException,

                   RegistryException, FileNotFoundException, LoginAuthenticationExceptionException,
                   LogoutAuthenticationExceptionException {

        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator + "GREG/rxt" +
                              File.separator + "person_with_age.rxt";

        Resource rxt = governance.newResource();
        rxt.setContentStream(new FileInputStream(resourcePath));
        rxt.setMediaType("application/vnd.wso2.registry-ext-type+xml");
        governance.put("repository/components/org.wso2.carbon.governance/types/person.rxt", rxt);
        assertTrue(governance.resourceExists("repository/components/org.wso2.carbon.governance/types/person.rxt"),
                   "rxt resource doesn't exists");

    }


    @Test(groups = "wso2.greg", description = "Delete and Re upload", dependsOnMethods = "testAddResource")
    public void testDeleteAndReUpload()
            throws RegistryException, IOException, ResourceAdminServiceExceptionException {

        governance.delete("repository/components/org.wso2.carbon.governance/types/person.rxt");

        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
                              File.separator + "GREG" + File.separator + "rxt" + File.separator + "person.rxt";

        Resource rxt = governance.newResource();
        rxt.setContentStream(new FileInputStream(new File(resourcePath)));
        rxt.setMediaType("application/vnd.wso2.registry-ext-type+xml");
        governance.put("repository/components/org.wso2.carbon.governance/types/person.rxt", rxt);
        assertTrue(governance.resourceExists("repository/components/org.wso2.carbon.governance/types/person.rxt"),
                   "rxt resource doesn't exists");


    }

    @Test(groups = "wso2.greg", description = "Add Artifact", dependsOnMethods = "testDeleteAndReUpload")
    public void testAddArtifact() throws RegistryException, InterruptedException, AxisFault {

        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
        GenericArtifactManager artifactManager = new GenericArtifactManager(governance, "person");
        removeGenericArtifactByQName(artifactManager, "newPerson");
        GenericArtifact artifact = artifactManager.newGovernanceArtifact(new QName
                                                                         ("newPerson"));

        artifact.setAttribute("overview_name", "Lasindu");
        artifactManager.addGenericArtifact(artifact);
        assertTrue(artifact.getAttribute("overview_id").equals("newPerson"), "artifact Id not found");
        assertTrue(artifact.getAttribute("overview_name").equals("Lasindu"), "artifact Name not found");

    }

    private void removeGenericArtifactByQName(GenericArtifactManager artifactManager, String qName)
            throws GovernanceException {
        GenericArtifact[] allGenericArtifacts = artifactManager.getAllGenericArtifacts();
        for (GenericArtifact genericArtifact : allGenericArtifacts) {
            if (genericArtifact.getQName().toString().contains(qName)) {
                artifactManager.removeGenericArtifact(genericArtifact.getId());
                break;
            }
        }
    }


    @Test(groups = "wso2.greg", description = "Delete and Re upload while use", dependsOnMethods = "testAddArtifact")
    public void testDeleteAndReUploadWhileUse()
            throws RegistryException, IOException, ResourceAdminServiceExceptionException {

        governance.delete("repository/components/org.wso2.carbon.governance/types/person.rxt");

        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator + "GREG" + File.separator + "rxt" +
                              File.separator + "person_with_age.rxt";

        Resource rxt = governance.newResource();
        rxt.setContentStream(new FileInputStream(new File(resourcePath)));
        rxt.setMediaType("application/vnd.wso2.registry-ext-type+xml");
        governance.put("repository/components/org.wso2.carbon.governance/types/person.rxt", rxt);
        assertTrue(governance.resourceExists("repository/components/org.wso2.carbon.governance/types/person.rxt"),
                   "rxt resource doesn't exists");


    }

    @Test(groups = "wso2.greg", description = "Add new Artifact to new Rxt file", dependsOnMethods = "testDeleteAndReUploadWhileUse")
    public void testAddArtifactToNewRxt()
            throws RegistryException, InterruptedException, AxisFault {

        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
        GenericArtifactManager artifactManager = new GenericArtifactManager(governance, "person");
        removeGenericArtifactByQName(artifactManager, "newPerson2");
        GenericArtifact artifact = artifactManager.newGovernanceArtifact(new QName
                                                                         ("newPerson2"));

        artifact.setAttribute("overview_name", "Charith");
        artifact.setAttribute("overview_age", "22");
        artifactManager.addGenericArtifact(artifact);
        assertTrue(artifact.getAttribute("overview_id").equals("newPerson2"), "artifact Id not found");
        assertTrue(artifact.getAttribute("overview_name").equals("Charith"), "artifact Name not found");
        assertTrue(artifact.getAttribute("overview_age").equals("22"), "artifact Age not found");

    }


    @Test(groups = "wso2.greg", description = "Add Rxt with incomplete Tags", dependsOnMethods = "testAddArtifactToNewRxt",
          expectedExceptions = RegistryException.class)
    public void testAddRxtWitIncompleteTags() throws FileNotFoundException, RegistryException {

        if (governance.resourceExists("repository/components/org.wso2.carbon.governance/types/person.rxt")) {
            governance.delete("repository/components/org.wso2.carbon.governance/types/person.rxt");
        }

        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator + "GREG" + File.separator + "rxt" +
                              File.separator + "person_tags_removed.rxt";


        Resource rxt = governance.newResource();
        rxt.setContentStream(new FileInputStream(new File(resourcePath)));
        rxt.setMediaType("application/vnd.wso2.registry-ext-type+xml");

        governance.put("repository/components/org.wso2.carbon.governance/types/person.rxt", rxt);

    }


    @Test(groups = "wso2.greg", description = "Add Rxt with columns/names duplicated", dependsOnMethods = "testAddRxtWitIncompleteTags")
    public void testColumnsNamesDuplicated() throws RegistryException, FileNotFoundException {

        if (governance.resourceExists("repository/components/org.wso2.carbon.governance/types/person.rxt")) {
            governance.delete("repository/components/org.wso2.carbon.governance/types/person.rxt");
        }
        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator + "GREG" + File.separator + "rxt" +
                              File.separator + "person_columns_names_duplicated.rxt";

        Resource rxt = governance.newResource();
        rxt.setContentStream(new FileInputStream(new File(resourcePath)));
        rxt.setMediaType("application/vnd.wso2.registry-ext-type+xml");
        governance.put("repository/components/org.wso2.carbon.governance/types/person.rxt", rxt);
        assertTrue(governance.resourceExists("repository/components/org.wso2.carbon.governance/types/person.rxt"),
                   "rxt resource doesn't exists");

    }

    @Test(groups = "wso2.greg", description = "Add new Artifact to new Rxt file which has duplicated columns/names",
          dependsOnMethods = "testColumnsNamesDuplicated")
    public void testArtifactWithDuplicatedColumnsNames()
            throws RegistryException, InterruptedException, AxisFault {

        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
        GenericArtifactManager artifactManager = new GenericArtifactManager(governance, "person");
        removeGenericArtifactByQName(artifactManager, "newPerson3");
        GenericArtifact artifact = artifactManager.newGovernanceArtifact(new QName
                                                                         ("newPerson3"));

        artifact.setAttribute("overview_name", "abc");
        artifact.setAttribute("overview_age", "10");
        artifactManager.addGenericArtifact(artifact);
        assertTrue(artifact.getAttribute("overview_id").equals("newPerson3"), "artifact Id not found");
        assertTrue(artifact.getAttribute("overview_name").equals("abc"), "artifact Name not found");
        assertTrue(artifact.getAttribute("overview_age").equals("10"), "artifact Age not found");
    }

    @Test(groups = "wso2.greg", description = "Delete and Re upload the artifact name changed file",
          dependsOnMethods = "testArtifactWithDuplicatedColumnsNames")
    public void testArtifactName()
            throws RegistryException, IOException, ResourceAdminServiceExceptionException {


        if (governance.resourceExists("repository/components/org.wso2.carbon.governance/types/person.rxt")) {
            governance.delete("repository/components/org.wso2.carbon.governance/types/person.rxt");
        }

        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator + "GREG" + File.separator + "rxt" +
                              File.separator + "person_somename.rxt";

        Resource rxt = governance.newResource();
        rxt.setContentStream(new FileInputStream(new File(resourcePath)));
        rxt.setMediaType("application/vnd.wso2.registry-ext-type+xml");
        governance.put("repository/components/org.wso2.carbon.governance/types/person.rxt", rxt);
        assertTrue(governance.resourceExists("repository/components/org.wso2.carbon.governance/types/person.rxt"), "rxt resource doesn't exists");


        governance.delete("repository/components/org.wso2.carbon.governance/types/person.rxt");

        String resourcePath2 = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator + "GREG" + File.separator + "rxt" +
                               File.separator + "person_name-name.rxt";

        Resource rxt2 = governance.newResource();
        rxt2.setContentStream(new FileInputStream(new File(resourcePath2)));
        rxt2.setMediaType("application/vnd.wso2.registry-ext-type+xml");
        governance.put("repository/components/org.wso2.carbon.governance/types/person.rxt", rxt2);
        assertTrue(governance.resourceExists("repository/components/org.wso2.carbon.governance/types/person.rxt"), "rxt resource doesn't exists");


        governance.delete("repository/components/org.wso2.carbon.governance/types/person.rxt");

        String resourcePath3 = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator + "GREG" + File.separator + "rxt" +
                               File.separator + "person_name.name.rxt";

        Resource rxt3 = governance.newResource();
        rxt3.setContentStream(new FileInputStream(new File(resourcePath3)));
        rxt3.setMediaType("application/vnd.wso2.registry-ext-type+xml");
        governance.put("repository/components/org.wso2.carbon.governance/types/person.rxt", rxt3);
        assertTrue(governance.resourceExists("repository/components/org.wso2.carbon.governance/types/person.rxt"), "rxt resource doesn't exists");


        governance.delete("repository/components/org.wso2.carbon.governance/types/person.rxt");

        String resourcePath4 = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator + "GREG" + File.separator + "rxt" +
                               File.separator + "person_123.name.rxt";

        Resource rxt4 = governance.newResource();
        rxt4.setContentStream(new FileInputStream(new File(resourcePath4)));
        rxt4.setMediaType("application/vnd.wso2.registry-ext-type+xml");
        governance.put("repository/components/org.wso2.carbon.governance/types/person.rxt", rxt4);
        assertTrue(governance.resourceExists("repository/components/org.wso2.carbon.governance/types/person.rxt"), "rxt resource doesn't exists");


        governance.delete("repository/components/org.wso2.carbon.governance/types/person.rxt");

        String resourcePath5 = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator + "GREG" + File.separator + "rxt" +
                               File.separator + "person_qa123.test345.rxt";

        Resource rxt5 = governance.newResource();
        rxt5.setContentStream(new FileInputStream(new File(resourcePath5)));
        rxt5.setMediaType("application/vnd.wso2.registry-ext-type+xml");
        governance.put("repository/components/org.wso2.carbon.governance/types/person.rxt", rxt5);
        assertTrue(governance.resourceExists("repository/components/org.wso2.carbon.governance/types/person.rxt"), "rxt resource doesn't exists");


        governance.delete("repository/components/org.wso2.carbon.governance/types/person.rxt");

        String resourcePath6 = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator + "GREG" + File.separator + "rxt" +
                               File.separator + "person_qa123-ght6.rxt";

        Resource rxt6 = governance.newResource();
        rxt6.setContentStream(new FileInputStream(new File(resourcePath6)));
        rxt6.setMediaType("application/vnd.wso2.registry-ext-type+xml");
        governance.put("repository/components/org.wso2.carbon.governance/types/person.rxt", rxt6);
        assertTrue(governance.resourceExists("repository/components/org.wso2.carbon.governance/types/person.rxt"), "rxt resource doesn't exists");
    }

    @Test(groups = "wso2.greg", description = "when @{ATTRIBUTE_NAME} is NOT specified ", dependsOnMethods = "testArtifactName")
    public void testAttributeNameRemoved()
            throws RegistryException, IOException, ResourceAdminServiceExceptionException,
                   InterruptedException {


        if (governance.resourceExists("repository/components/org.wso2.carbon.governance/types/person.rxt")) {
            governance.delete("repository/components/org.wso2.carbon.governance/types/person.rxt");
        }

        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator + "GREG" + File.separator + "rxt" +
                              File.separator + "person_attribute_name_removed.rxt";

        Resource rxt = governance.newResource();
        rxt.setContentStream(new FileInputStream(new File(resourcePath)));
        rxt.setMediaType("application/vnd.wso2.registry-ext-type+xml");
        governance.put("repository/components/org.wso2.carbon.governance/types/person.rxt", rxt);
        assertTrue(governance.resourceExists("repository/components/org.wso2.carbon.governance/types/person.rxt"), "rxt resource doesn't exists");


        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
        GenericArtifactManager artifactManager = new GenericArtifactManager(governance, "person");
        removeGenericArtifactByQName(artifactManager, "newPerson4");
        GenericArtifact artifact = artifactManager.newGovernanceArtifact(new QName("newPerson4"));

        artifact.setAttribute("overview_name", "def");
        artifactManager.addGenericArtifact(artifact);
        assertTrue(artifact.getAttribute("overview_id").equals("newPerson4"), "artifact Id not found");
        assertTrue(artifact.getAttribute("overview_name").equals("def"), "artifact Name not found");
        assertTrue(governance.resourceExists("people/newPerson4"), "newPerson4 doesn't exists");

    }


    @Test(groups = "wso2.greg", description = "Test Unbounded option-text ", dependsOnMethods = "testAttributeNameRemoved")
    public void testUnboundedOptionText()
            throws RegistryException, IOException, ResourceAdminServiceExceptionException,
                   InterruptedException {


        if (governance.resourceExists("repository/components/org.wso2.carbon.governance/types/person.rxt")) {
            governance.delete("repository/components/org.wso2.carbon.governance/types/person.rxt");
        }

        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
                              File.separator + "GREG" + File.separator + "rxt" +
                              File.separator + "project-group.rxt";

        Resource rxt = governance.newResource();
        rxt.setContentStream(new FileInputStream(new File(resourcePath)));
        rxt.setMediaType("application/vnd.wso2.registry-ext-type+xml");
        governance.put("repository/components/org.wso2.carbon.governance/types/groups.rxt", rxt);
        assertTrue(governance.resourceExists("repository/components/org.wso2.carbon.governance/types/groups.rxt"),
                   "rxt resource doesn't exists");


        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
        GenericArtifactManager artifactManager = new GenericArtifactManager(governance, "groups");
        removeGenericArtifactByQName(artifactManager, "projectGroupName");
        GenericArtifact artifact = artifactManager.newGovernanceArtifact(new QName("projectGroupName"));


        artifact.setAttribute("overview_group Owner", "groupOwner");
        artifact.setAttributes("groupMembers_member", new String[]{"Junior:path1", "Senior:path2"});


        artifactManager.addGenericArtifact(artifact);
        assertTrue(artifact.getAttribute("overview_group Owner").equals("groupOwner"), "artifact Group Owner not found");
        assertTrue(artifact.getAttributes("groupMembers_member")[0].equals("Junior:path1"), "artifact Group Member1 not found");
        assertTrue(artifact.getAttributes("groupMembers_member")[1].equals("Senior:path2"), "artifact Group Member2 not found");

        if (governance.resourceExists("repository/components/org.wso2.carbon.governance/types/groups.rxt")) {
            governance.delete("repository/components/org.wso2.carbon.governance/types/groups.rxt");
        }

    }

    @AfterClass
    public void RemoveRxts() throws RegistryException {


        if (governance.resourceExists("repository/components/org.wso2.carbon.governance/types/person.rxt")) {
            governance.delete("repository/components/org.wso2.carbon.governance/types/person.rxt");

            if (governance.resourceExists("repository/components/org.wso2.carbon.governance/types/groups.rxt")) {
                governance.delete("repository/components/org.wso2.carbon.governance/types/groups.rxt");
            }
        }
        governance = null;
    }

}
