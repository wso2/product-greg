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

package org.wso2.carbon.registry.jira.issues.test;

import org.apache.axiom.om.util.AXIOMUtil;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

public class Carbon8636TestCase extends GREGIntegrationBaseTest {

    private Registry governance;
    private ServiceManager serviceManager;
    private Service serviceDescriptionsWithEnterKeys;
    private Service serviceDescriptionWithOutEnterKeys;

    @BeforeClass(groups = {"wso2.greg"}, alwaysRun = true)
    public void init() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        WSRegistryServiceClient wsRegistry = new RegistryProviderUtil().getWSRegistry(automationContext);
        governance = new RegistryProviderUtil().getGovernanceRegistry(wsRegistry, automationContext);
        serviceManager = new ServiceManager(governance);
    }
    @Test(groups = {"wso2.greg"}, description = "description with having enter keys")
    public void descriptionWithEnterKeysTestCase() throws GovernanceException, XMLStreamException {
        String content = "<serviceMetaData xmlns=\"http://www.wso2.org/governance/metadata\">" +
                "<overview><name>" + "serviceDescriptionsWithEnterKeys" + "</name><namespace>" +
                "http://service.with.EnterKeys/mnm/" + "</namespace><version>1.0.0-SNAPSHOT</version></overview>" +
                "</serviceMetaData>";
        org.apache.axiom.om.OMElement XMLContent = AXIOMUtil.stringToOM(content);

        serviceDescriptionsWithEnterKeys =
                serviceManager.newService(XMLContent);
        serviceDescriptionsWithEnterKeys.addAttribute("description", "This is Description&#xd;");
        serviceManager.addService(serviceDescriptionsWithEnterKeys);
        Assert.assertNotNull(serviceManager.getService(serviceDescriptionsWithEnterKeys.getId()));

    }
    @Test(groups = {"wso2.greg"}, description = "description with out having enter keys")
    public void descriptionWithoutEnterKeysTestCase() throws GovernanceException, XMLStreamException {
        String content = "<serviceMetaData xmlns=\"http://www.wso2.org/governance/metadata\">" +
                "<overview><name>" + "serviceDescriptionsWithOutEnterKeys" + "</name><namespace>" +
                "http://service.with.EnteroutKeys/mnm/" + "</namespace><version>1.0.0-SNAPSHOT</version></overview>" +
                "</serviceMetaData>";
        org.apache.axiom.om.OMElement XMLContent = AXIOMUtil.stringToOM(content);

        serviceDescriptionWithOutEnterKeys =
                serviceManager.newService(XMLContent);
        serviceDescriptionWithOutEnterKeys.addAttribute("description", "This is Description");
        serviceManager.addService(serviceDescriptionWithOutEnterKeys);
        Assert.assertNotNull(serviceManager.getService(serviceDescriptionWithOutEnterKeys.getId()));

    }
    @AfterClass(groups = {"wso2.greg"}, alwaysRun = true)
    public void deleteServices() throws GovernanceException {
        serviceManager.removeService(serviceDescriptionsWithEnterKeys.getId());
        serviceManager.removeService(serviceDescriptionWithOutEnterKeys.getId());
    }
}
