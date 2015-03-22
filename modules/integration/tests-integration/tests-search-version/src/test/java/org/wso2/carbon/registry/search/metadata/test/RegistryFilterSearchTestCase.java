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

package org.wso2.carbon.registry.search.metadata.test;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.automation.test.utils.common.FileManager;
import org.wso2.carbon.governance.api.schema.SchemaManager;
import org.wso2.carbon.governance.api.schema.dataobjects.Schema;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
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

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Iterator;

public class RegistryFilterSearchTestCase extends GREGIntegrationBaseTest {

    private SearchAdminServiceClient searchAdminServiceClient;
    private Registry governance;
    private WSRegistryServiceClient wsRegistry;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private String sessionCookie;
    private String backEndUrl;
    private String userName;
    private String userNameWithoutDomain;

    @BeforeClass
    public void init() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);

        backEndUrl = getBackendURL();
        sessionCookie = getSessionCookie();
        userName = automationContext.getContextTenant().getContextUser().getUserName();

        if (userName.contains("@"))
            userNameWithoutDomain = userName.substring(0, userName.indexOf('@'));
        else
            userNameWithoutDomain = userName;

        searchAdminServiceClient =
                new SearchAdminServiceClient(backEndUrl, sessionCookie);
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(backEndUrl, sessionCookie);
        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        wsRegistry = registryProviderUtil.getWSRegistry(automationContext);
        governance = registryProviderUtil.getGovernanceRegistry(wsRegistry, automationContext);

        addResources();

    }

    @Test(groups = {"wso2.greg"}, description = "Add filter")
    public void addFilterTest() throws SearchAdminServiceRegistryExceptionException,
            RemoteException, RegistryException {

        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setPropertyName("wsdlProperty");
        paramBean.setLeftPropertyValue("0");
        paramBean.setRightPropertyValue("15");
        paramBean.setLeftOperator("gt");
        paramBean.setRightOperator("lt");
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        paramList = null;

        searchAdminServiceClient.saveAdvancedSearchFilter(searchQuery, "wsdlSearch");
        String[] filters = searchAdminServiceClient.getSavedFilters();

        boolean filterFound = false;
        for (String filter : filters) {

            if (filter.equals("wsdlSearch")) {
                filterFound = true;
                break;
            }
        }
        Assert.assertTrue(filterFound);
        searchAdminServiceClient.deleteFilter("wsdlSearch");
    }


    @Test(groups = {"wso2.greg"}, description = "delete filter")
    public void deleteFilterTest() throws SearchAdminServiceRegistryExceptionException,
            RemoteException, RegistryException {

        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setPropertyName("wsdlProperty");
        paramBean.setLeftPropertyValue("0");
        paramBean.setRightPropertyValue("15");
        paramBean.setLeftOperator("gt");
        paramBean.setRightOperator("lt");
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        paramList = null;
        searchAdminServiceClient.saveAdvancedSearchFilter(searchQuery, "wsdlSearch");
        String[] filters = searchAdminServiceClient.getSavedFilters();

        boolean filterFound = false;
        for (String filter : filters) {

            if (filter.equals("wsdlSearch")) {
                filterFound = true;
                break;
            }
        }
        Assert.assertTrue(filterFound);

        searchAdminServiceClient.deleteFilter("wsdlSearch");
        boolean filterdeleted = true;

        if (!(searchAdminServiceClient.getSavedFilters() == null)) {
            String[] filter = searchAdminServiceClient.getSavedFilters();

            for (String filt : filter) {

                if (filt.equals("wsdlSearch")) {
                    filterdeleted = false;
                    break;
                }
            }

        }
        Assert.assertTrue(filterdeleted);

    }


    @Test(groups = {"wso2.greg"}, description = "search via filter")
    public void searchViaFilterTest()
            throws SearchAdminServiceRegistryExceptionException, RemoteException,
            RegistryException {

        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setPropertyName("wsdlProperty");
        paramBean.setRightPropertyValue("10");
        paramBean.setRightOperator("eq");
        paramBean.setLeftOperator("na");
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        paramList = null;
        searchAdminServiceClient.saveAdvancedSearchFilter(searchQuery, "wsdlSearch");
        AdvancedSearchResultsBean result =
                searchAdminServiceClient.getAdvancedSearchResults(searchAdminServiceClient.getAdvancedSearchFilter("wsdlSearch"));

        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid property name");

        for (ResourceData resource : result.getResourceDataList()) {
            boolean propertyFound = false;
            Iterator<String> properties = (Iterator<String>) wsRegistry.get(resource.getResourcePath())
                    .getProperties().propertyNames();
            while (properties.hasNext()) {
                if (properties.next().contains("wsdlProperty")) {
                    Assert.assertTrue((wsRegistry.get(resource.getResourcePath()).getProperty("wsdlProperty").equals("10")));
                    propertyFound = true;
                }
            }
            Assert.assertTrue(propertyFound, "Property name not found on Resource " + resource.getResourcePath());
        }
        searchAdminServiceClient.deleteFilter("wsdlSearch");

    }


    public void addResources()
            throws ResourceAdminServiceExceptionException, IOException, RegistryException, InterruptedException {
        addWSDL();
        addSchema();
    }

    public void addWSDL()
            throws IOException, ResourceAdminServiceExceptionException, RegistryException, InterruptedException {

        WsdlManager wsdlManager = new WsdlManager(governance);
        Wsdl wsdl;
        String wsdlFilePath = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator +
                "GREG" + File.separator + "wsdl" + File.separator + "AmazonWebServices.wsdl";
        wsdl = wsdlManager.newWsdl(FileManager.readFile(wsdlFilePath).getBytes(), "AmazonWebServices.wsdl");
        wsdlManager.addWsdl(wsdl);
        wsdl = wsdlManager.getWsdl(wsdl.getId());
        Resource resource = governance.get(wsdl.getPath());
        resource.addProperty("wsdlProperty", "10");
        governance.put(wsdl.getPath(), resource);
        Thread.sleep(60000);
    }

    public void addSchema() throws IOException, RegistryException, InterruptedException {
        SchemaManager schemaManager = new SchemaManager(governance);
        String schemaFilePath = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" +
                File.separator + "GREG" + File.separator + "schema" + File.separator + "library.xsd";
        Schema schema = schemaManager.newSchema(FileManager.readFile(schemaFilePath).getBytes(), "library.xsd");
        schemaManager.addSchema(schema);
        schema = schemaManager.getSchema(schema.getId());
        Resource resource = governance.get(schema.getPath());
        resource.addProperty("wsdlProperty", "20");
        governance.put(schema.getPath(), resource);
        Thread.sleep(60000);
    }

    @AfterClass
    public void clean() throws ResourceAdminServiceExceptionException, RemoteException, RegistryException {
        delete("/_system/governance/trunk/wsdls/com/amazon/soap/1.0.0/AmazonWebServices.wsdl");
        delete("/_system/governance/trunk/services/com/amazon/soap/1.0.0/AmazonSearchService");
        delete("/_system/governance/trunk/schemas/com/example/www/library/1.0.0/library.xsd");
        delete("/_system/governance/trunk/endpoints/com");

        searchAdminServiceClient = null;
        resourceAdminServiceClient = null;
        wsRegistry = null;
        governance = null;
    }

    public void delete(String destPath) throws ResourceAdminServiceExceptionException, RemoteException, RegistryException {
        if (wsRegistry.resourceExists(destPath)) {
            resourceAdminServiceClient.deleteResource(destPath);
        }
    }

}