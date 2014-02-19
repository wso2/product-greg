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
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.api.clients.registry.SearchAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.core.utils.fileutils.FileManager;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.api.schema.SchemaManager;
import org.wso2.carbon.governance.api.schema.dataobjects.Schema;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
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

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Iterator;

public class RegistrySearchByPropertyValueWithEmptyValueTest {

    private SearchAdminServiceClient searchAdminServiceClient;
    private Registry governance;
    private WSRegistryServiceClient wsRegistry;
    private ResourceAdminServiceClient resourceAdminServiceClient;

    @BeforeClass
    public void init() throws Exception {
        int userId = 2;
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        ManageEnvironment environment = builder.build();
        UserInfo userInfo = UserListCsvReader.getUserInfo(userId);
        searchAdminServiceClient =
                new SearchAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                             environment.getGreg().getSessionCookie());
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                               environment.getGreg().getSessionCookie());
        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        wsRegistry = registryProviderUtil.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        governance = registryProviderUtil.getGovernanceRegistry(wsRegistry, userId);

        addResources();

    }

    @Test(groups = {"wso2.greg"}, description = "Search by Property value 0 < X <")
    public void searchByEmptyValue_lt_novalue0()
            throws SearchAdminServiceRegistryExceptionException, RemoteException,
            RegistryException {


        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setPropertyName("wsdlProperty");
        paramBean.setLeftPropertyValue("0");

        paramBean.setLeftOperator("gt");
        paramBean.setRightOperator("lt");

        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        paramList = null;
        AdvancedSearchResultsBean result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid property name");

        for (ResourceData resource : result.getResourceDataList()) {
            boolean propertyFound = false;
            Iterator<String> properties = (Iterator<String>) wsRegistry.get(resource.getResourcePath())
                    .getProperties().propertyNames();
            while (properties.hasNext()) {
                if (properties.next().contains("wsdlProperty")) {
                    Assert.assertTrue((wsRegistry.get(resource.getResourcePath()).getProperty("wsdlProperty").equals("20")) ||
                            (wsRegistry.get(resource.getResourcePath()).getProperty("wsdlProperty").equals("10")));
                    propertyFound = true;
                }
            }
            Assert.assertTrue(propertyFound, "Property name not found on Resource " + resource.getResourcePath());
        }


    }


    @Test(groups = {"wso2.greg"}, description = "Search by Property value a < X  <")
    public void searchByEmptyValue_lt_novalue()
            throws SearchAdminServiceRegistryExceptionException, RemoteException,
            RegistryException {


        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setPropertyName("wsdlProperty");
        paramBean.setLeftPropertyValue("10");

        paramBean.setLeftOperator("gt");
        paramBean.setRightOperator("lt");


        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        paramList = null;
        AdvancedSearchResultsBean result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid property name");

        for (ResourceData resource : result.getResourceDataList()) {
            boolean propertyFound = false;
            Iterator<String> properties = (Iterator<String>) wsRegistry.get(resource.getResourcePath())
                    .getProperties().propertyNames();
            while (properties.hasNext()) {
                if (properties.next().contains("wsdlProperty")) {
                    Assert.assertTrue((wsRegistry.get(resource.getResourcePath()).getProperty("wsdlProperty").equals("20")));
                    propertyFound = true;
                }
            }
            Assert.assertTrue(propertyFound, "Property name not found on Resource " + resource.getResourcePath());
        }


    }


    @Test(groups = {"wso2.greg"}, description = "Search by Property value 0 <= X < ")
    public void searchByPropertyValue_lt_novalue0()
            throws SearchAdminServiceRegistryExceptionException, RemoteException,
            RegistryException {


        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setPropertyName("wsdlProperty");
        paramBean.setLeftPropertyValue("0");

        paramBean.setLeftOperator("ge");
        paramBean.setRightOperator("lt");


        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        paramList = null;
        AdvancedSearchResultsBean result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid property name");

        for (ResourceData resource : result.getResourceDataList()) {
            boolean propertyFound = false;
            Iterator<String> properties = (Iterator<String>) wsRegistry.get(resource.getResourcePath())
                    .getProperties().propertyNames();
            while (properties.hasNext()) {
                if (properties.next().contains("wsdlProperty")) {
                    Assert.assertTrue((wsRegistry.get(resource.getResourcePath()).getProperty("wsdlProperty").equals("20")) ||
                            (wsRegistry.get(resource.getResourcePath()).getProperty("wsdlProperty").equals("10")));
                    propertyFound = true;
                }
            }
            Assert.assertTrue(propertyFound, "Property name not found on Resource " + resource.getResourcePath());
        }


    }


    @Test(groups = {"wso2.greg"}, description = "Search by Property value a <= X < ")
    public void searchByPropertyValue_lt_novalue()
            throws SearchAdminServiceRegistryExceptionException, RemoteException,
            RegistryException {


        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setPropertyName("wsdlProperty");
        paramBean.setLeftPropertyValue("10");

        paramBean.setLeftOperator("ge");
        paramBean.setRightOperator("lt");


        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        paramList = null;
        AdvancedSearchResultsBean result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid property name");

        for (ResourceData resource : result.getResourceDataList()) {
            boolean propertyFound = false;
            Iterator<String> properties = (Iterator<String>) wsRegistry.get(resource.getResourcePath())
                    .getProperties().propertyNames();
            while (properties.hasNext()) {
                if (properties.next().contains("wsdlProperty")) {
                    Assert.assertTrue((wsRegistry.get(resource.getResourcePath()).getProperty("wsdlProperty").equals("20")) ||
                            (wsRegistry.get(resource.getResourcePath()).getProperty("wsdlProperty").equals("10")));
                    propertyFound = true;
                }
            }
            Assert.assertTrue(propertyFound, "Property name not found on Resource " + resource.getResourcePath());
        }


    }


    @Test(groups = {"wso2.greg"}, description = "Search by Property value 0 <= X <= ")
    public void searchByPropertyValue_le_novalue0()
            throws SearchAdminServiceRegistryExceptionException, RemoteException,
            RegistryException {


        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setPropertyName("wsdlProperty");
        paramBean.setLeftPropertyValue("0");

        paramBean.setLeftOperator("ge");
        paramBean.setRightOperator("le");


        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        paramList = null;
        AdvancedSearchResultsBean result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid property name");

        for (ResourceData resource : result.getResourceDataList()) {
            boolean propertyFound = false;
            Iterator<String> properties = (Iterator<String>) wsRegistry.get(resource.getResourcePath())
                    .getProperties().propertyNames();
            while (properties.hasNext()) {
                if (properties.next().contains("wsdlProperty")) {
                    Assert.assertTrue((wsRegistry.get(resource.getResourcePath()).getProperty("wsdlProperty").equals("20")) ||
                            (wsRegistry.get(resource.getResourcePath()).getProperty("wsdlProperty").equals("10")));
                    propertyFound = true;
                }
            }
            Assert.assertTrue(propertyFound, "Property name not found on Resource " + resource.getResourcePath());
        }


    }


    @Test(groups = {"wso2.greg"}, description = "Search by Property value a <= X  <=")
    public void searchByPropertyValue_le_novalue()
            throws SearchAdminServiceRegistryExceptionException, RemoteException,
            RegistryException {


        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setPropertyName("wsdlProperty");
        paramBean.setLeftPropertyValue("10");

        paramBean.setLeftOperator("ge");
        paramBean.setRightOperator("le");


        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        paramList = null;
        AdvancedSearchResultsBean result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid property name");

        for (ResourceData resource : result.getResourceDataList()) {
            boolean propertyFound = false;
            Iterator<String> properties = (Iterator<String>) wsRegistry.get(resource.getResourcePath())
                    .getProperties().propertyNames();
            while (properties.hasNext()) {
                if (properties.next().contains("wsdlProperty")) {
                    Assert.assertTrue((wsRegistry.get(resource.getResourcePath()).getProperty("wsdlProperty").equals("20")) ||
                            (wsRegistry.get(resource.getResourcePath()).getProperty("wsdlProperty").equals("10")));
                    propertyFound = true;
                }
            }
            Assert.assertTrue(propertyFound, "Property name not found on Resource " + resource.getResourcePath());
        }


    }


    @Test(groups = {"wso2.greg"}, description = "Search by Property value 0 < X <= ")
    public void searchByPropertyValue_lte_novalue0()
            throws SearchAdminServiceRegistryExceptionException, RemoteException,
            RegistryException {


        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setPropertyName("wsdlProperty");
        paramBean.setLeftPropertyValue("0");

        paramBean.setLeftOperator("gt");
        paramBean.setRightOperator("le");


        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        paramList = null;
        AdvancedSearchResultsBean result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid property name");

        for (ResourceData resource : result.getResourceDataList()) {
            boolean propertyFound = false;
            Iterator<String> properties = (Iterator<String>) wsRegistry.get(resource.getResourcePath())
                    .getProperties().propertyNames();
            while (properties.hasNext()) {
                if (properties.next().contains("wsdlProperty")) {
                    Assert.assertTrue((wsRegistry.get(resource.getResourcePath()).getProperty("wsdlProperty").equals("20")) ||
                            (wsRegistry.get(resource.getResourcePath()).getProperty("wsdlProperty").equals("10")));
                    propertyFound = true;
                }
            }
            Assert.assertTrue(propertyFound, "Property name not found on Resource " + resource.getResourcePath());
        }


    }


    @Test(groups = {"wso2.greg"}, description = "Search by Property value a < X  <=")
    public void searchByPropertyValue_lte_novalue()
            throws SearchAdminServiceRegistryExceptionException, RemoteException,
            RegistryException {


        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setPropertyName("wsdlProperty");
        paramBean.setLeftPropertyValue("13");

        paramBean.setLeftOperator("gt");
        paramBean.setRightOperator("le");


        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        paramList = null;
        AdvancedSearchResultsBean result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid property name");

        for (ResourceData resource : result.getResourceDataList()) {
            boolean propertyFound = false;
            Iterator<String> properties = (Iterator<String>) wsRegistry.get(resource.getResourcePath())
                    .getProperties().propertyNames();
            while (properties.hasNext()) {
                if (properties.next().contains("wsdlProperty")) {
                    Assert.assertTrue((wsRegistry.get(resource.getResourcePath()).getProperty("wsdlProperty").equals("20")));
                    propertyFound = true;
                }
            }
            Assert.assertTrue(propertyFound, "Property name not found on Resource " + resource.getResourcePath());
        }


    }

    @Test(groups = {"wso2.greg"}, description = "Search by Property value  < X < 0 ")
    public void searchByPropertyValue_gt_novalue0()
            throws SearchAdminServiceRegistryExceptionException, RemoteException,
            RegistryException {


        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setPropertyName("wsdlProperty");
        paramBean.setRightPropertyValue("0");

        paramBean.setLeftOperator("gt");
        paramBean.setRightOperator("lt");


        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        paramList = null;
        AdvancedSearchResultsBean result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);


        Assert.assertTrue((result.getResourceDataList()) == null);
    }


    @Test(groups = {"wso2.greg"}, description = "Search by Property value  < X  < a")
    public void searchByPropertyValue_gt_novalue()
            throws SearchAdminServiceRegistryExceptionException, RemoteException,
            RegistryException {


        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setPropertyName("wsdlProperty");
        paramBean.setRightPropertyValue("16");

        paramBean.setLeftOperator("gt");
        paramBean.setRightOperator("lt");


        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        paramList = null;
        AdvancedSearchResultsBean result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
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


    }


    @Test(groups = {"wso2.greg"}, description = "Search by Property value  <= X < 0 ")
    public void searchByEmptyValue_gte_value0()
            throws SearchAdminServiceRegistryExceptionException, RemoteException,
            RegistryException {


        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setPropertyName("wsdlProperty");
        paramBean.setRightPropertyValue("0");

        paramBean.setLeftOperator("ge");
        paramBean.setRightOperator("lt");


        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        paramList = null;
        AdvancedSearchResultsBean result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);


        Assert.assertTrue((result.getResourceDataList()) == null);
    }


    @Test(groups = {"wso2.greg"}, description = "Search by Property value  <= X  < a")
    public void searchByEmptyValue_gte_value()
            throws SearchAdminServiceRegistryExceptionException, RemoteException,
            RegistryException {


        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setPropertyName("wsdlProperty");
        paramBean.setRightPropertyValue("16");

        paramBean.setLeftOperator("ge");
        paramBean.setRightOperator("lt");


        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        paramList = null;
        AdvancedSearchResultsBean result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
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


    }


    @Test(groups = {"wso2.greg"}, description = "Search by Property value  < X <= 0 ")
    public void searchByEmptyValue_gt_value0()
            throws SearchAdminServiceRegistryExceptionException, RemoteException,
            RegistryException {


        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setPropertyName("wsdlProperty");
        paramBean.setRightPropertyValue("0");

        paramBean.setLeftOperator("gt");
        paramBean.setRightOperator("le");


        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        paramList = null;
        AdvancedSearchResultsBean result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);


        Assert.assertTrue((result.getResourceDataList()) == null);
    }


    @Test(groups = {"wso2.greg"}, description = "Search by Property value  < X  <= a")
    public void searchByEmptyValue_gt_value()
            throws SearchAdminServiceRegistryExceptionException, RemoteException,
            RegistryException {


        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setPropertyName("wsdlProperty");
        paramBean.setRightPropertyValue("16");

        paramBean.setLeftOperator("gt");
        paramBean.setRightOperator("le");


        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        paramList = null;
        AdvancedSearchResultsBean result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
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


    }


    @Test(groups = {"wso2.greg"}, description = "Search by Property value  <= X <= 0 ")
    public void searchByEmptyValue_gte_evalue0()
            throws SearchAdminServiceRegistryExceptionException, RemoteException,
            RegistryException {


        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setPropertyName("wsdlProperty");
        paramBean.setRightPropertyValue("0");

        paramBean.setLeftOperator("ge");
        paramBean.setRightOperator("le");


        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        if (paramList.length > 0) {
            paramList = null;
        }
        AdvancedSearchResultsBean result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);


        Assert.assertTrue((result.getResourceDataList()) == null);
    }


    @Test(groups = {"wso2.greg"}, description = "Search by Property value  <= X  <= a")
    public void searchByEmptyValue_gte_evalue()
            throws SearchAdminServiceRegistryExceptionException, RemoteException,
            RegistryException {


        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setPropertyName("wsdlProperty");
        paramBean.setRightPropertyValue("16");

        paramBean.setLeftOperator("ge");
        paramBean.setRightOperator("le");


        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        paramList = null;
        AdvancedSearchResultsBean result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
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


    }


    public void addResources()
            throws ResourceAdminServiceExceptionException, IOException, RegistryException {
        addWSDL();
        addSchema();
    }

    public void addWSDL()
            throws IOException, ResourceAdminServiceExceptionException, RegistryException {

        WsdlManager wsdlManager = new WsdlManager(governance);
        Wsdl wsdl;
        String wsdlFilePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator +
                "GREG" + File.separator + "wsdl" + File.separator + "AmazonWebServices.wsdl";
        wsdl = wsdlManager.newWsdl(FileManager.readFile(wsdlFilePath).getBytes(), "AmazonWebServices.wsdl");
        wsdlManager.addWsdl(wsdl);
        wsdl = wsdlManager.getWsdl(wsdl.getId());
        Resource resource = governance.get(wsdl.getPath());
        resource.addProperty("wsdlProperty", "10");
        governance.put(wsdl.getPath(), resource);

    }

    public void addSchema() throws IOException, RegistryException {

        SchemaManager schemaManager = new SchemaManager(governance);
        String schemaFilePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
                File.separator + "GREG" + File.separator + "schema" + File.separator + "library.xsd";
        Schema schema = schemaManager.newSchema(FileManager.readFile(schemaFilePath).getBytes(), "library.xsd");
        schemaManager.addSchema(schema);
        schema = schemaManager.getSchema(schema.getId());
        Resource resource = governance.get(schema.getPath());
        resource.addProperty("wsdlProperty", "20");
        governance.put(schema.getPath(), resource);
    }

    @AfterClass
    public void clean() throws ResourceAdminServiceExceptionException, RemoteException, RegistryException {
        delete("/_system/governance/trunk/wsdls/com/amazon/soap/AmazonWebServices.wsdl");
        delete("/_system/governance/trunk/services/com/amazon/soap/AmazonSearchService");
        delete("/_system/governance/trunk/schemas/com/example/www/library/library.xsd");
        delete("/_system/governance/trunk/endpoints/com");

        searchAdminServiceClient = null;
        resourceAdminServiceClient = null;
        wsRegistry = null;
        governance = null;
    }

     public void delete(String destPath) throws ResourceAdminServiceExceptionException, RemoteException, RegistryException {
        if(wsRegistry.resourceExists(destPath)){
            resourceAdminServiceClient.deleteResource(destPath);
        }
    }

}