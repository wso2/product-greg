/*
 * Copyright 2015 The Apache Software Foundation.
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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.pagination.PaginationContext;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.search.stub.beans.xsd.AdvancedSearchResultsBean;
import org.wso2.carbon.registry.search.stub.beans.xsd.ArrayOfString;
import org.wso2.carbon.registry.search.stub.beans.xsd.CustomSearchParameterBean;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.clients.SearchAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import javax.xml.stream.XMLStreamException;

import static org.testng.Assert.assertTrue;

/**
 * This class is used to test the sorting based on rating and created time of the search results at the
 * management console.
 */
public class SearchResultSortTestCase extends GREGIntegrationBaseTest {

    private WSRegistryServiceClient registry;
    private Registry gov;
    private SearchAdminServiceClient searchAdminServiceClient;
    private String sessionCookie;
    private String backEndUrl;

    @BeforeClass (alwaysRun = true)
    public void initTest() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);

        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        registry = registryProviderUtil.getWSRegistry(automationContext);
        backEndUrl = getBackendURL();
        sessionCookie = getSessionCookie();
        searchAdminServiceClient = new SearchAdminServiceClient(backEndUrl, sessionCookie);
        gov = GovernanceUtils.getGovernanceUserRegistry(registry, "admin");
    }

    /**
     * This test method is used to test sorting based on created time of the search results.
     *
     * @throws Exception
     */
    @Test (groups = { "wso2.greg" })
    public void sortByCreatedDate() throws Exception {
        try {
            // Should be load the governance artifact.
            GovernanceUtils.loadGovernanceArtifacts((UserRegistry) gov);
            addServices(gov);
            rateServices();

            //Initialize the pagination context.
            //Gives search results in ascending order of created time
            PaginationContext.init(0, 15, "ASC", "created", 100);
            ArrayOfString[] paramList = new ArrayOfString[1];
            paramList[0] = new ArrayOfString();
            paramList[0].setArray(new String[] { "resourcePath", "FlightService%" });
            CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
            searchQuery.setParameterValues(paramList);
            AdvancedSearchResultsBean advancedSearchResultsBean = searchAdminServiceClient
                    .getAdvancedSearchResults(searchQuery);
            org.wso2.carbon.registry.search.stub.common.xsd.ResourceData[] resourceData = advancedSearchResultsBean
                    .getResourceDataList();

            String createdDateAscendingSortMessage =
                    "filter results are not sorted based on the created date in ascending order";
            assertTrue(resourceData[0].getName().equals("FlightService1"), createdDateAscendingSortMessage);
            assertTrue(resourceData[8].getName().equals("FlightService9"), createdDateAscendingSortMessage);

            //Initialize the pagination context.
            //Gives search results in descending order of created time.
            PaginationContext.init(0, 15, "DES", "created", 100);
            paramList = new ArrayOfString[1];
            paramList[0] = new ArrayOfString();
            paramList[0].setArray(new String[] { "resourcePath", "FlightService%" });
            searchQuery = new CustomSearchParameterBean();
            searchQuery.setParameterValues(paramList);
            advancedSearchResultsBean = searchAdminServiceClient
                    .getAdvancedSearchResults(searchQuery);
            resourceData = advancedSearchResultsBean
                    .getResourceDataList();

            String createdDateDescendingSortMessage =
                    "filter results are not sorted based on the created date in descending order.";
            assertTrue(resourceData[8].getName().equals("FlightService1"), createdDateDescendingSortMessage);
            assertTrue(resourceData[0].getName().equals("FlightService9"), createdDateDescendingSortMessage);
        } finally {
            PaginationContext.destroy();
        }
    }

    /**
     * This test method is used to test sorting based on rating of the search results.
     *
     * @throws Exception
     */
    @Test (groups = { "wso2.greg" }, dependsOnMethods = "sortByCreatedDate")
    public void sortByRating()
            throws Exception {
        try {
            PaginationContext.init(0, 15, "DES", "rating", 100);
            ArrayOfString[] paramList = new ArrayOfString[1];
            paramList[0] = new ArrayOfString();
            paramList[0].setArray(new String[] { "resourcePath", "FlightService%" });
            CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
            searchQuery.setParameterValues(paramList);
            AdvancedSearchResultsBean advancedSearchResultsBean = searchAdminServiceClient
                    .getAdvancedSearchResults(searchQuery);
            org.wso2.carbon.registry.search.stub.common.xsd.ResourceData[] resourceData = advancedSearchResultsBean
                    .getResourceDataList();

            String ratingSortErrorMessage = "filter results are not sorted based on the rating";
            assertTrue(resourceData[0].getName().equals("FlightService5"), ratingSortErrorMessage);
            assertTrue(resourceData[1].getName().equals("FlightService4"), ratingSortErrorMessage);
        } finally {
            PaginationContext.destroy();
        }
    }

    @AfterClass (alwaysRun = true)
    public void cleanUp() throws RegistryException {
        GenericArtifactManager genericArtifactManager = new GenericArtifactManager(gov, "soapservice");
        GenericArtifact[] artifacts = genericArtifactManager.getAllGenericArtifacts();
        for (GenericArtifact artifact : artifacts) {
            genericArtifactManager.removeGenericArtifact(artifact.getId());
        }
    }

    /**
     * This private method is used to create few soap services in order to run the test methods.
     *
     * @param govRegistry   instance of the user governance registry.
     * @throws RegistryException
     * @throws XMLStreamException
     * @throws InterruptedException
     */
    private void addServices(Registry govRegistry) throws RegistryException, XMLStreamException, InterruptedException {
        GenericArtifactManager artifactManager = new GenericArtifactManager(govRegistry, "soapservice");
        for (int i = 1; i < 10; i++) {
            StringBuilder builder = new StringBuilder();
            builder.append("<serviceMetaData xmlns=\"http://www.wso2.org/governance/metadata\">");
            builder.append("<overview><name>FlightService");
            builder.append(i);
            builder.append("</name><namespace>ns</namespace>");
            builder.append("<version>1.0.0</version></overview>");
            builder.append("</serviceMetaData>");
            org.apache.axiom.om.OMElement XMLContent = AXIOMUtil.stringToOM(builder.toString());
            GenericArtifact artifact = artifactManager.newGovernanceArtifact(XMLContent);
            artifactManager.addGenericArtifact(artifact);
            //Services need to be index before search.
        }
        Thread.sleep(10 * 1000);
    }

    /**
     * This private method is used to add ratings to the soap services.
     *
     * @throws RegistryException
     */
    private void rateServices() throws RegistryException {
        for (int i = 1; i < 6; i++) {
            registry.rateResource("/_system/governance/trunk/soapservices/ns/1.0.0/FlightService" + i, i);
        }
    }
}