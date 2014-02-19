/*
 *  Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.registry.governance.api.test.old;

import org.apache.axis2.AxisFault;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import javax.xml.namespace.QName;
import java.util.Collections;

public class OrganizationTestCase extends RXTTestBase {

    public OrganizationTestCase() {
        fileName = "organization.metadata.xml";
        key = "organizations";
        path1 = "/organizations/RenameArtifact";
        path2 = "/organizations/NewRenameArtifact";
        values.put("overview_president", "/people/123-abc-4567-defghi");
        search = Collections.singletonMap("overview_president", "/people/123-abc-4567-defghi");
        nameReplacement = new QName("WSO2");
    }

    @BeforeClass(groups = {"wso2.greg"})
    public void initTest() throws RegistryException, AxisFault {
        super.initTest();
        loadRXTsForAssetModelSamples("PeopleModel");
    }

    @AfterClass()
    public void endGame() throws RegistryException, AxisFault {
        Registry governance;
        WSRegistryServiceClient wsRegistry;
        int userId = 1;
        wsRegistry =
                new RegistryProviderUtil().getWSRegistry(userId,
                        ProductConstant.GREG_SERVER_NAME);
        governance = new RegistryProviderUtil().getGovernanceRegistry(wsRegistry, userId);
        GenericArtifactManager artifactManager = new GenericArtifactManager(governance, "departments");

        GenericArtifact[] artifacts = artifactManager.getAllGenericArtifacts();
        for (GenericArtifact genericArtifact : artifacts) {
            artifactManager.removeGenericArtifact(genericArtifact.getId());
        }
        String rxtLocation = "/_system/governance/_system/governance/repository/components/org.wso2.carbon.governance/types/";
        if (wsRegistry.resourceExists(rxtLocation + "department.rxt")) {
            wsRegistry.delete(rxtLocation + "department.rxt");
        }
        if (wsRegistry.resourceExists(rxtLocation + "organization.rxt")) {
            wsRegistry.delete(rxtLocation + "organization.rxt");
        }
        if (wsRegistry.resourceExists(rxtLocation + "person.rxt")) {
            wsRegistry.delete(rxtLocation + "person.rxt");
        }
        if (wsRegistry.resourceExists(rxtLocation + "project-group.rxt")) {
            wsRegistry.delete(rxtLocation + "project-group.rxt");
        }
    }

}
