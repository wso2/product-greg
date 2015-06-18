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

package org.wso2.carbon.registry.search.tags.test;

import org.apache.axis2.AxisFault;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.info.stub.RegistryExceptionException;
import org.wso2.carbon.registry.info.stub.beans.utils.xsd.Tag;
import org.wso2.carbon.registry.info.stub.beans.xsd.TagBean;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.clients.InfoServiceAdminClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import javax.xml.namespace.QName;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * This test case is to test tag addition and tag search added tags.
 */
public class TagsSearchTestCase extends GREGIntegrationBaseTest {

    private GenericArtifactManager artifactManager;
    private Registry governanceRegistry;
    private InfoServiceAdminClient infoAdminServiceClient;
    private String sessionCookie;

    /**
     * This method used to initialize TagsSearchTestCase.
     *
     * @throws Exception
     */
    @BeforeClass(groups = { "wso2.greg" })
    public void init() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        WSRegistryServiceClient wsRegistryServiceClient = new RegistryProviderUtil().getWSRegistry(automationContext);
        governanceRegistry = new RegistryProviderUtil()
                .getGovernanceRegistry(wsRegistryServiceClient, automationContext);
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governanceRegistry,
                GovernanceUtils.findGovernanceArtifactConfigurations(governanceRegistry));
        artifactManager = new GenericArtifactManager(governanceRegistry, "soapservice");
        sessionCookie = new LoginLogoutClient(automationContext).login();
        infoAdminServiceClient = new InfoServiceAdminClient(backendURL, sessionCookie);
    }

    /**
     * THis test case is to add a SOAP service to test tag addition.
     *
     * @throws GovernanceException
     * @throws InterruptedException
     */
    @Test(groups = { "wso2.greg" }, description = "create SOAP Service using GenericArtifact")
    public void createSOAPServiceTest() throws GovernanceException, InterruptedException {
        GenericArtifact artifact = artifactManager.newGovernanceArtifact(new QName("SOAPService1"));

        artifact.setAttribute("overview_name", "SOAPService1");
        artifact.setAttribute("overview_version", "4.5.0");
        artifact.setAttribute("overview_description", "Description");

        artifactManager.addGenericArtifact(artifact);

        GenericArtifact receivedArtifact = artifactManager.getGenericArtifact(artifact.getId());
        assertEquals(artifact.getAttribute("overview_name"), receivedArtifact.getAttribute("overview_name"),
                " Service name must be equal");
    }

    /**
     * THis test is to test tag addition.
     *
     * @throws RegistryException
     * @throws AxisFault
     * @throws RegistryExceptionException
     */
    @Test(groups = { "wso2.greg" }, dependsOnMethods = { "createSOAPServiceTest" })
    public void addTagsTest() throws RegistryException, AxisFault, RegistryExceptionException, InterruptedException {
        String soapService = "/_system/governance/trunk/soapservices/4.5.0/SOAPService1";
        infoAdminServiceClient.addTag("SampleTag1", soapService, sessionCookie);
        infoAdminServiceClient.addTag("SampleTag2", soapService, sessionCookie);

        TagBean tagBean = infoAdminServiceClient.getTags(soapService, sessionCookie);
        Tag[] tag = tagBean.getTags();
        assertTrue(tag[0].getTagName().equalsIgnoreCase("SampleTag1"));
        assertTrue(tag[1].getTagName().equalsIgnoreCase("SampleTag2"));
    }
}
