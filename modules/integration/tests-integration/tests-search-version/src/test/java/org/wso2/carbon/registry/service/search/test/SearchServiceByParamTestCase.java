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

import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.resource.stub.ResourceAdminService;
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
import java.util.Iterator;

public class SearchServiceByParamTestCase extends GREGIntegrationBaseTest {

    private static final Log log = LogFactory.getLog(SearchServiceByParamTestCase.class);
    private SearchAdminServiceClient searchAdminServiceClient;
    private ResourceAdminServiceClient resourceAdminServiceClient;

    private ServiceManager serviceManager;
    private Registry governance;
    private WSRegistryServiceClient wsRegistry;
    private final String service1 = "aaa";
    private final String service2 = "bbb";

    @BeforeClass(groups = {"wso2.greg"})
    public void init() throws Exception {
        log.info("Initializing Tests for Service Search");
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String sessionCookie = getSessionCookie();
        String backEndUrl = getBackendURL();

        log.debug("Running SuccessCase");
        searchAdminServiceClient = new SearchAdminServiceClient(backEndUrl, sessionCookie);
        resourceAdminServiceClient = new ResourceAdminServiceClient(backEndUrl,sessionCookie);
        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();

        wsRegistry = registryProviderUtil.getWSRegistry(automationContext);

        governance = registryProviderUtil.getGovernanceRegistry(wsRegistry, automationContext);

        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
        serviceManager = new ServiceManager(governance);
        addResources();
        Thread.sleep(60000);
    }

    @Test(groups = {"wso2.greg"}, description = "search via filter")
    public void searchForDefaultServiceTest()
            throws SearchAdminServiceRegistryExceptionException, RemoteException,
            RegistryException {

        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setPropertyName("default");
        paramBean.setRightPropertyValue("true");
        paramBean.setRightOperator("eq");
        paramBean.setLeftOperator("na");
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        paramList = null;
        AdvancedSearchResultsBean result =
                searchAdminServiceClient.getAdvancedSearchResults(searchQuery);

        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertEquals(result.getResourceDataList().length, 2);

        for (ResourceData resource : result.getResourceDataList()) {
            boolean propertyFound = false;
            Iterator<String> properties = (Iterator<String>) wsRegistry.get(resource.getResourcePath())
                    .getProperties().propertyNames();
            while (properties.hasNext()) {
                if (properties.next().contains("default")) {
                    Assert.assertTrue((wsRegistry.get(resource.getResourcePath()).getProperty("default").equals("true")));
                    propertyFound = true;
                }
            }
            Assert.assertTrue(propertyFound, "Property name not found on Resource " + resource.getResourcePath());
            log.info("Default Resource Path: " +resource.getResourcePath());
        }

    }

    @Test(groups = {"wso2.greg"}, description = "search via filter", dependsOnMethods = "searchForDefaultServiceTest")
    public void searchForServicesTest()
            throws SearchAdminServiceRegistryExceptionException, RemoteException,
            RegistryException {

        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setMediaType("application/vnd.wso2-service+xml");
        paramBean.setResourceName(service1);
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        paramList = null;
        AdvancedSearchResultsBean result =
                searchAdminServiceClient.getAdvancedSearchResults(searchQuery);

        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid property name");

        for (ResourceData resource : result.getResourceDataList()) {
            Assert.assertNotNull(resource, "No resource found");
            log.info("Resource Path: " +resource.getResourcePath());
        }

    }

    private void addResources() throws Exception {
        /*
        * add services with different versions
        * nameSpace: aaa
        * serviceName: aaa
        * version: 1.0.0, 1.0.1, 1.0.2
        * */
        String resourcePath = addService(service1, service1, "1.0.0");
        if (resourcePath != null) {
            Resource resource = governance.get(resourcePath);
            resource.addProperty("default", "true");
            governance.put(resourcePath,resource);
        }
        addService(service1, service1, "1.0.1");
        addService(service1, service1, "1.0.2");

        /*
        * add services with different versions
        * nameSpace: bbb
        * serviceName: bbb
        * version: 1.0.0, 1.0.1
        * */
        addService(service2, service2, "1.0.0");
        String resourcePath2 = addService(service2, service2, "1.0.1");
        if (resourcePath2 != null) {
            Resource resource = governance.get(resourcePath2);
            resource.addProperty("default", "true");
            governance.put(resourcePath2, resource);
        }
    }


    private String addService(String nameSpace, String serviceName, String version)
            throws Exception {
        Service service;
        StringBuilder builder = new StringBuilder();
        builder.append("<serviceMetaData xmlns=\"http://www.wso2.org/governance/metadata\">");
        builder.append("<overview><name>" + serviceName + "</name>");
        builder.append("<namespace>" + nameSpace + "</namespace>");
        builder.append("<version>" +version+"</version></overview>");
        builder.append("</serviceMetaData>");
        org.apache.axiom.om.OMElement XMLContent = AXIOMUtil.stringToOM(builder.toString());

        service = serviceManager.newService(XMLContent);
        serviceManager.addService(service);
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
        for (String serviceId : serviceManager.getAllServiceIds()) {
            service = serviceManager.getService(serviceId);
            if (service.getPath().endsWith(serviceName) && service.getPath().contains("trunk")) {
                return service.getPath();
            }

        }
        throw new Exception("Getting Service path failed");

    }

    @AfterClass(alwaysRun = true)
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE,
            ExecutionEnvironment.STANDALONE})
    public void clean()
            throws ResourceAdminServiceExceptionException, RemoteException, RegistryException {

        delete("/_system/governance/trunk/services/bbb/1.0.1/bbb");
        delete("/_system/governance/trunk/services/bbb/1.0.0/bbb");
        delete("/_system/governance/trunk/services/aaa/1.0.0/aaa");
        delete("/_system/governance/trunk/services/aaa/1.0.1/aaa");
        delete("/_system/governance/trunk/services/aaa/1.0.2/aaa");

        resourceAdminServiceClient = null;
        searchAdminServiceClient = null;
        governance = null;
        wsRegistry=null;
    }

    private void delete(String destPath) throws ResourceAdminServiceExceptionException, RemoteException, RegistryException {
        if (wsRegistry.resourceExists(destPath)) {
            resourceAdminServiceClient.deleteResource(destPath);
        }
    }
}
