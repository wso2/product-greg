/*
*Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.service.search.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.search.metadata.test.bean.SearchParameterBean;
import org.wso2.carbon.registry.search.stub.SearchAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.search.stub.beans.xsd.AdvancedSearchResultsBean;
import org.wso2.carbon.registry.search.stub.beans.xsd.ArrayOfString;
import org.wso2.carbon.registry.search.stub.beans.xsd.CustomSearchParameterBean;
import org.wso2.carbon.registry.search.stub.common.xsd.ResourceData;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.clients.SearchAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;

public class SearchDependentEndpointInWsdlTestCase extends GREGIntegrationBaseTest {

    private static final Log log = LogFactory.getLog(SearchServiceByParamTestCase.class);
    private SearchAdminServiceClient searchAdminServiceClient;
    private ResourceAdminServiceClient resourceAdminServiceClient;

    private Wsdl wsdl;
    private WsdlManager wsdlManager;
    private Registry governance;
    private WSRegistryServiceClient wsRegistry;

    private String[] endpointArtifactPaths = {
            "/_system/governance/trunk/endpoints/localhost/services/ep-echo-yu-echoHttpEndpoint",
            "/_system/governance/trunk/endpoints/localhost/services/ep-echo-yu-echoHttpsEndpoint",
            "/_system/governance/trunk/endpoints/localhost/services/ep-echo-yu-echoHttpSoap11Endpoint",
            "/_system/governance/trunk/endpoints/localhost/services/ep-echo-yu-echoHttpSoap12Endpoint",
            "/_system/governance/trunk/endpoints/localhost/services/ep-echo-yu-echoHttpsSoap11Endpoint",
            "/_system/governance/trunk/endpoints/localhost/services/ep-echo-yu-echoHttpsSoap12Endpoint" };

    @BeforeClass(groups = { "wso2.greg" })
    public void init() throws Exception {
        log.info("Initializing Tests for Service Search");
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String sessionCookie = getSessionCookie();
        String backEndUrl = getBackendURL();

        log.info("Running SuccessCase");
        searchAdminServiceClient = new SearchAdminServiceClient(backEndUrl, sessionCookie);
        resourceAdminServiceClient = new ResourceAdminServiceClient(backEndUrl, sessionCookie);
        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();

        wsRegistry = registryProviderUtil.getWSRegistry(automationContext);

        governance = registryProviderUtil.getGovernanceRegistry(wsRegistry, automationContext);

        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
        wsdlManager = new WsdlManager(governance);
        addWsdl();
        Thread.sleep(30000);
    }

    private void addWsdl() throws GovernanceException {
        wsdl = wsdlManager.newWsdl("https://svn.wso2.org" +
                "/repos/wso2/carbon/platform/trunk/platform-integration/" +
                "platform-automated-test-suite/org.wso2.carbon.automation.test.repo/" +
                "src/main/resources/artifacts/GREG/wsdl/echo.wsdl");
        wsdl.addAttribute("version", "1.0.0");
        wsdl.addAttribute("author", "NewUser");
        wsdl.addAttribute("description", "added wsdl with endpoints");
        wsdlManager.addWsdl(wsdl);
    }

    @Test(groups = { "wso2.greg" }, description = "search via filter")
    public void searchForEndpointsTest()
            throws SearchAdminServiceRegistryExceptionException, RemoteException, RegistryException {

        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setMediaType("application/vnd.wso2-endpoint+xml");
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);

        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        log.info("Number of resources: " + result.getResourceDataList().length);
        Assert.assertTrue((result.getResourceDataList().length >= 6), "There should be more than 6 records");

        int i = 0;
        for (ResourceData resource : result.getResourceDataList()) {
            assertTrue(endpointArtifactPaths[i].equals(resource.getResourcePath()));
            log.info("Resource Path: " + resource.getResourcePath());
            i++;
        }
    }

    @AfterClass(groups = "wso2.greg", alwaysRun = true, description = "cleaning up the artifacts added")
    public void tearDown()
            throws RegistryException, RemoteException, ResourceAdminServiceExceptionException {
        wsdlManager.removeWsdl(wsdl.getId());
        delete("/_system/governance/trunk/services/org/wso2/carbon/core/services/echo/1.0.0/echoyuSer1");
        for (int i = 0; i < endpointArtifactPaths.length; i++) {
            delete(endpointArtifactPaths[i]);
        }
        governance = null;
        wsdl = null;
        wsdlManager = null;
    }

    private void delete(String destPath)
            throws ResourceAdminServiceExceptionException, RemoteException, RegistryException {
        if (wsRegistry.resourceExists(destPath)) {
            resourceAdminServiceClient.deleteResource(destPath);
        }
    }
}
