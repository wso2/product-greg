/*
 * Copyright 2004,2005 The Apache Software Foundation.
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

package org.wso2.carbon.registry.pagination.test;

import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.transport.http.HTTPConstants;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.automation.engine.frameworkutils.TestFrameworkUtils;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.client.WSRegistrySearchClient;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.pagination.PaginationContext;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertTrue;

public class PaginationWSTest extends GREGIntegrationBaseTest {

    private WSRegistryServiceClient registry;

    private Registry governance;
    private GenericArtifactManager genericArtifactManager;
    private GenericArtifact genericArtifact1;
    private GenericArtifact genericArtifact2;
    private String mediaType = "application/vnd.wso2-soap-service+xml";
    private static final int TIME_OUT_VALUE = 1000 * 60;
    private static String cookie;

    @BeforeClass(alwaysRun = true)
    public void initTest() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);

        cookie = getSessionCookie();
        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        registry = registryProviderUtil.getWSRegistry(automationContext);

        initSearchQueryResources();
    }

    @Test(groups = {"wso2.greg"})
    public void testPaginate() throws Exception {
        try {
            Registry gov = GovernanceUtils.getGovernanceUserRegistry(registry, "admin");
            // Should be load the governance artifact.
            GovernanceUtils.loadGovernanceArtifacts((UserRegistry) gov);
            addServices(gov);
            //Initialize the pagination context.
            //Top five services, sortBy name , and sort order descending.
            PaginationContext.init(0, 5, "DES", "overview_name", 100);
            WSRegistrySearchClient wsRegistrySearchClient = new WSRegistrySearchClient();
            //This should be execute to initialize the AttributeSearchService.
            ConfigurationContext configContext;
            String axis2Repo = FrameworkPathUtil.getSystemResourceLocation() + "client";
            String axis2Conf = FrameworkPathUtil.getSystemResourceLocation() + "axis2config" +
                    File.separator + "axis2_client.xml";
            TestFrameworkUtils.setKeyStoreProperties(automationContext);
            configContext = ConfigurationContextFactory.
                    createConfigurationContextFromFileSystem(axis2Repo, axis2Conf);
            configContext.setProperty(HTTPConstants.CONNECTION_TIMEOUT, TIME_OUT_VALUE);

            wsRegistrySearchClient.init(cookie, backendURL, configContext);

//            wsRegistrySearchClient.authenticate(configContext, getServiceURL(),
//                    automationContext.getContextTenant().getContextUser().getUserName(),
//                    automationContext.getContextTenant().getContextUser().getPassword());

            //Initialize the GenericArtifactManager
            GenericArtifactManager artifactManager = new GenericArtifactManager(gov, "service");
            Map<String, List<String>> listMap = new HashMap<String, List<String>>();
            //Create the search attribute map
            listMap.put("lcName", new ArrayList<String>() {{
                add("ServiceLifeCycle");
            }});
            listMap.put("lcState", new ArrayList<String>() {{
                add("Development");
            }});
            //Find the results.
            GenericArtifact[] genericArtifacts = artifactManager.findGenericArtifacts(listMap);
            assertTrue(genericArtifacts.length > 0, "No any service found");
            assertTrue(genericArtifacts.length == 5, "Filtered service count should be 5");
            assertTrue(genericArtifacts[0].getQName().getLocalPart().equals("FlightService9"), "filter results are not sorted");
            assertTrue(genericArtifacts[4].getQName().getLocalPart().equals("FlightService5"), "filter results are not sorted");
        } finally {
            PaginationContext.destroy();
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Search by name", dependsOnMethods = {"testPaginate"})
    public void testSearchByName() throws RegistryException {
        try {
            PaginationContext.init(0, 5, "DES", "overview_name", 100);
            List<GovernanceArtifact> searchResults = GovernanceUtils.findGovernanceArtifacts("name=GenericArtifactSearchService", governance, mediaType);
            Assert.assertEquals(searchResults.size(), 2, "Wrong number of artifacts returned by search");

            for (GovernanceArtifact artifact : searchResults) {
                Assert.assertEquals(artifact.getQName().getLocalPart(), "GenericArtifactSearchService", "Wrong number of artifacts returned by search");
            }
        } finally {
            PaginationContext.destroy();
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Search by name and version", dependsOnMethods = {"testSearchByName"})
    public void testSearchByVersion() throws RegistryException {
        try {
            PaginationContext.init(0, 5, "DES", "overview_name", 100);
            List<GovernanceArtifact> searchResults = GovernanceUtils.findGovernanceArtifacts("version=4.5.6", governance, mediaType);
            Assert.assertEquals(searchResults.size(), 1, "Wrong number of artifacts returned by search");

            for (GovernanceArtifact artifact : searchResults) {
                Assert.assertEquals(artifact.getQName().getLocalPart(), "GenericArtifactSearchService", "Wrong number of artifacts returned by search");
            }
        } finally {
            PaginationContext.destroy();
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Search by name and property", dependsOnMethods = {"testSearchByVersion"})
    public void testSearchByProperty() throws RegistryException {
        try {
            PaginationContext.init(0, 5, "DES", "overview_name", 100);
            List<GovernanceArtifact> searchResults = GovernanceUtils.findGovernanceArtifacts("country=usa", governance, mediaType);
            Assert.assertEquals(searchResults.size(), 1, "Wrong number of artifacts returned by search");

            for (GovernanceArtifact artifact : searchResults) {
                Assert.assertEquals(artifact.getQName().getLocalPart(), "GenericArtifactSearchService", "Wrong number of artifacts returned by search");
            }
        } finally {
            PaginationContext.destroy();
        }
    }

    private void initSearchQueryResources() throws Exception {
        governance = GovernanceUtils.getGovernanceUserRegistry(registry, "admin");
        // Should be load the governance artifact.
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);

        genericArtifactManager = new GenericArtifactManager(governance, "soapservice");
        String content = "<serviceMetaData xmlns=\"http://www.wso2.org/governance/metadata\">" + "<overview><name>" + "GenericArtifactSearchService"
                + "</name><namespace>" + "https://www.wso2.com/greg/store" + "</namespace><version>4.5.6</version></overview>" + "</serviceMetaData>";
        org.apache.axiom.om.OMElement XMLContent = AXIOMUtil.stringToOM(content);
        genericArtifact1 = genericArtifactManager.newGovernanceArtifact(XMLContent);
        genericArtifactManager.addGenericArtifact(genericArtifact1);

        String path = genericArtifact1.getPath();
        Resource resource = governance.get(path);
        resource.addProperty("country", "usa");
        governance.put(path, resource);

        content = "<serviceMetaData xmlns=\"http://www.wso2.org/governance/metadata\">" + "<overview><name>" + "GenericArtifactSearchService"
                + "</name><namespace>" + "https://www.wso2.com/greg/store" + "</namespace><version>4.5.7</version></overview>" + "</serviceMetaData>";
        XMLContent = AXIOMUtil.stringToOM(content);
        genericArtifact2 = genericArtifactManager.newGovernanceArtifact(XMLContent);
        genericArtifactManager.addGenericArtifact(genericArtifact2);

        cookie = getSessionCookie();

        ConfigurationContext configContext;
        String axis2Repo = FrameworkPathUtil.getSystemResourceLocation() + "client";
        String axis2Conf = FrameworkPathUtil.getSystemResourceLocation() + "axis2config" +
                File.separator + "axis2_client.xml";
        TestFrameworkUtils.setKeyStoreProperties(automationContext);
        configContext = ConfigurationContextFactory.
                createConfigurationContextFromFileSystem(axis2Repo, axis2Conf);
        configContext.setProperty(HTTPConstants.CONNECTION_TIMEOUT, TIME_OUT_VALUE);

        WSRegistrySearchClient wsRegistrySearchClient = new WSRegistrySearchClient();
        //This should be execute to initialize the AttributeSearchService.
        wsRegistrySearchClient.init(cookie, backendURL, configContext);
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws RegistryException {
        genericArtifactManager.removeGenericArtifact(genericArtifact1.getId());
        genericArtifactManager.removeGenericArtifact(genericArtifact2.getId());
    }

    private static void addServices(Registry govRegistry) throws RegistryException, XMLStreamException {
        GenericArtifactManager artifactManager = new GenericArtifactManager(govRegistry, "service");
        for(int i = 1; i < 10; i++) {
            StringBuilder builder = new StringBuilder();
            builder.append("<serviceMetaData xmlns=\"http://www.wso2.org/governance/metadata\">");
            builder.append("<overview><name>FlightService" + i + "</name><namespace>ns</namespace>");
            builder.append("<version>1.0.0-SNAPSHOT</version></overview>");
            builder.append("</serviceMetaData>");
            org.apache.axiom.om.OMElement XMLContent = AXIOMUtil.stringToOM(builder.toString());
            GenericArtifact artifact = artifactManager.newGovernanceArtifact(XMLContent);
            artifactManager.addGenericArtifact(artifact);
        }
        //Services need to be index before search.
        try {
            Thread.sleep(60 * 1000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

    }
}
