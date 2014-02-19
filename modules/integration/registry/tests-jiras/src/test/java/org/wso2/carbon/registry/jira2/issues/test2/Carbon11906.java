/*
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

package org.wso2.carbon.registry.jira2.issues.test2;

import org.apache.axis2.AxisFault;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.governance.LifeCycleManagementClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.core.utils.fileutils.FileManager;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.RemoteException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 *
 *
 */
public class Carbon11906 {
    int userId = 2;
    private UserInfo userInfo = UserListCsvReader.getUserInfo(userId);
    private Registry governance;
    private static final String LC_NAME = "StateDemoteLC";
    private LifeCycleManagementClient lifeCycleManagementClient;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private WSRegistryServiceClient registry;

    private String personPath;
    private String mediaType = "application/vnd.wso2.registry-ext-type+xml";
    private String rxtPath = "repository/components/org.wso2.carbon.governance/types/person.rxt";
    private GenericArtifact artifact;
    private GenericArtifactManager artifactManager;

    /**
     * @throws LoginAuthenticationExceptionException
     *
     * @throws RemoteException
     * @throws RegistryException
     */
    @BeforeClass
    public void initialize() throws LoginAuthenticationExceptionException, RemoteException,
                                    RegistryException {
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        ManageEnvironment environment = builder.build();

        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        registry =
                registryProviderUtil.getWSRegistry(userId,
                                                   ProductConstant.GREG_SERVER_NAME);
        governance = registryProviderUtil.getGovernanceRegistry(registry, userId);

        lifeCycleManagementClient =
                new LifeCycleManagementClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                              environment.getGreg().getSessionCookie());
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                               environment.getGreg().getSessionCookie());
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);

    }

    /**
     * @throws RegistryException
     * @throws IOException
     * @throws ResourceAdminServiceExceptionException
     *
     */
    @Test(groups = "wso2.greg", description = "Add RXT")
    public void testAddRxt() throws RegistryException, IOException,
                                    ResourceAdminServiceExceptionException {
        String resourcePath =
                ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
                File.separator + "GREG/rxt" + File.separator + "person.rxt";

        Resource rxt = governance.newResource();
        rxt.setContentStream(new FileInputStream(new File(resourcePath)));
        rxt.setMediaType(mediaType);
        governance.put(rxtPath, rxt);
        assertTrue(governance.resourceExists(rxtPath),
                   "rxt resource doesn't exists");

    }

    /**
     * @throws RegistryException
     * @throws InterruptedException
     * @throws AxisFault
     */
    @Test(groups = "wso2.greg", description = "Add Artifact", dependsOnMethods = "testAddRxt")
    public void testAddArtifact() throws RegistryException, InterruptedException, AxisFault {

        artifactManager = new GenericArtifactManager(governance, "person");
        artifact = artifactManager.newGovernanceArtifact(new QName("newPerson_Carbon11906"));

        artifact.setAttribute("overview_name", "ABC");
        artifactManager.addGenericArtifact(artifact);
        assertTrue(artifact.getAttribute("overview_id").equals("newPerson_Carbon11906"),
                   "artifact Id not found");
        assertTrue(artifact.getAttribute("overview_name").equals("ABC"), "artifact Name not found");

    }

    /**
     * @throws LifeCycleManagementServiceExceptionException
     *
     * @throws IOException
     * @throws GovernanceException
     */
    @Test(groups = "wso2.greg", description = "Add lifecycle to a person", dependsOnMethods = "testAddArtifact")
    public void testAddLcToPerson() throws LifeCycleManagementServiceExceptionException,
                                           IOException, GovernanceException {

        String resourcePath =
                ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
                File.separator + "GREG" + File.separator + "lifecycle" +
                File.separator + "StateDemoteLifeCycle.xml";
        String lifeCycleContent = FileManager.readFile(resourcePath);
        lifeCycleManagementClient.addLifeCycle(lifeCycleContent);
        personPath = artifact.getPath();
        artifact.attachLifecycle(LC_NAME);
        artifact.getAttributeKeys();
    }

    /**
     * @throws GovernanceException
     */
    @Test(groups = "wso2.greg", description = "Modify the person and check whether the lifecycle is still attached",
          dependsOnMethods = "testAddLcToPerson")
    public void testModifyPerson() throws GovernanceException {

        artifact.setAttribute("overview_name", "ABCDEFG");
        artifactManager.updateGenericArtifact(artifact);

        assertNotNull(artifact.getLifecycleName(), "No attached lifecyle");
        assertEquals(artifact.getLifecycleName(), LC_NAME, "Lifecycle mismatched");
    }

    /**
     * @throws Exception
     */
    @AfterClass()
    public void clear() throws Exception {
        if (registry.resourceExists("/_system/governance/" + rxtPath)) {
            resourceAdminServiceClient.deleteResource("/_system/governance/" + rxtPath);
        }
        if (registry.resourceExists("/_system/governance" + personPath)) {
            resourceAdminServiceClient.deleteResource("/_system/governance" + personPath);
        }
        lifeCycleManagementClient.deleteLifeCycle(LC_NAME);
        resourceAdminServiceClient = null;
        lifeCycleManagementClient = null;
        artifact = null;
        artifactManager = null;
        governance = null;
        registry = null;
    }

}
