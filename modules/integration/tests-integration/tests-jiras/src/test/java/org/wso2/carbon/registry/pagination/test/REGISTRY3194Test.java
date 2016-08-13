/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.registry.pagination.test;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.transport.http.HTTPConstants;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.automation.engine.frameworkutils.TestFrameworkUtils;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.governance.client.WSRegistrySearchClient;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.pagination.PaginationContext;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.indexing.IndexingConstants;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import javax.xml.stream.XMLStreamException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.util.Collections;
import java.util.List;

import static org.testng.Assert.assertEquals;

public class REGISTRY3194Test extends GREGIntegrationBaseTest {
    private static Registry governanceRegistry;
    WsdlManager wsdlManager;
    private static final String WSDL_URL = "https://raw.githubusercontent.com/wso2/wso2-qa-artifacts/master/automation-artifacts/greg/wsdl/StockQuote.wsdl";
    private static final String WSDL_NAME = "StockQuote.wsdl";
    private GenericArtifactManager genericArtifactManager;
    private static final int TIME_OUT_VALUE = 1000 * 60;
    private static String cookie;

    @BeforeClass(alwaysRun = true)
    public void initTest() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        cookie = getSessionCookie();
        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        WSRegistryServiceClient registry = registryProviderUtil.getWSRegistry(automationContext);
        governanceRegistry = registryProviderUtil.getGovernanceRegistry(registry, automationContext);
        wsdlManager = new WsdlManager(governanceRegistry);
        //Initialize the GenericArtifactManager
        genericArtifactManager = new GenericArtifactManager(governanceRegistry, "wsdl");
        removeWsdlArtifacts();
        addWSDLFile();
    }

    private void addWSDLFile() throws RegistryException, XMLStreamException {
        Wsdl wsdl = wsdlManager.newWsdl(WSDL_URL);
        wsdl.addAttribute("version", "1.0.0");
        wsdl.addAttribute("creator", "admin");
        wsdlManager.addWsdl(wsdl);
    }

    @Test(groups = { "wso2.greg" })
    public void testWsdlAssetSearch() throws RegistryException, XPathExpressionException, AxisFault {
        try {
            GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governanceRegistry);
            initializeRegistrySearchClient();
            //Initialize the pagination context.
            PaginationContext.init(0, 1, "DESC", IndexingConstants.META_CREATED_DATE, 100);
            //Find the results.
            GenericArtifact[] genericArtifacts = getGovernanceArtifacts();
            assertEquals(genericArtifacts.length, 1);
            assertEquals(genericArtifacts[0].getQName().getLocalPart(),WSDL_NAME);
        } finally {
            PaginationContext.destroy();
        }
    }

    private GenericArtifact[] getGovernanceArtifacts() throws GovernanceException {
        GenericArtifact[] genericArtifacts;
        double time1 = System.currentTimeMillis();
        while (true) {
            //Find list of artifacts.
            genericArtifacts = genericArtifactManager.findGenericArtifacts(Collections.<String, List<String>>emptyMap());
            double time2 = System.currentTimeMillis();
            if (genericArtifacts.length > 0) {
                break;
            } else if ((time2 - time1) > 120000) {
                log.error("Timeout while searching for assets | time waited: " + (time2 - time1));
                break;
            }
        }
        return genericArtifacts;
    }

    private void initializeRegistrySearchClient() throws XPathExpressionException, AxisFault, RegistryException {
        WSRegistrySearchClient wsRegistrySearchClient = new WSRegistrySearchClient();
        //This should be execute to initialize the AttributeSearchService.
        ConfigurationContext configContext;
        String axis2Repo = FrameworkPathUtil.getSystemResourceLocation() + "client";
        String axis2Conf = FrameworkPathUtil.getSystemResourceLocation() + "axis2config" +
                File.separator + "axis2_client.xml";
        TestFrameworkUtils.setKeyStoreProperties(automationContext);
        configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(axis2Repo,
                axis2Conf);
        configContext.setProperty(HTTPConstants.CONNECTION_TIMEOUT, TIME_OUT_VALUE);

        wsRegistrySearchClient.init(cookie, backendURL, configContext);
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws RegistryException {
        removeWsdlArtifacts();
    }

    private void removeWsdlArtifacts() throws GovernanceException {
        GenericArtifact[] artifacts = genericArtifactManager.getAllGenericArtifacts();
        for (GenericArtifact artifact : artifacts) {
            genericArtifactManager.removeGenericArtifact(artifact.getId());
        }
    }
}
