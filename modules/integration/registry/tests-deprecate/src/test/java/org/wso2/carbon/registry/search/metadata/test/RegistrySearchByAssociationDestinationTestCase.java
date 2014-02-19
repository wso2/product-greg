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
import org.wso2.carbon.automation.api.clients.registry.SearchAdminServiceClient;
import org.wso2.carbon.governance.api.policies.PolicyManager;
import org.wso2.carbon.governance.api.policies.dataobjects.Policy;
import org.wso2.carbon.governance.api.schema.SchemaManager;
import org.wso2.carbon.governance.api.schema.dataobjects.Schema;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.governance.api.test.TestUtils;
import org.wso2.carbon.registry.search.metadata.test.bean.SearchParameterBean;
import org.wso2.carbon.registry.search.metadata.test.utils.CommonUtils;
import org.wso2.carbon.registry.search.metadata.test.utils.GregTestUtils;
import org.wso2.carbon.registry.search.metadata.test.utils.Parameters;
import org.wso2.carbon.registry.search.stub.SearchAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.search.stub.beans.xsd.AdvancedSearchResultsBean;
import org.wso2.carbon.registry.search.stub.beans.xsd.ArrayOfString;
import org.wso2.carbon.registry.search.stub.beans.xsd.CustomSearchParameterBean;
import org.wso2.carbon.registry.search.stub.common.xsd.ResourceData;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;

/*
Search Registry metadata by AssociationDestination
 */
public class RegistrySearchByAssociationDestinationTestCase {

    private SearchAdminServiceClient searchAdminService;
    private WSRegistryServiceClient registry;
    private Registry governance;

    private String destinationPath1 = null;

    @BeforeClass
    public void init() throws Exception {

        final String  SERVER_URL = GregTestUtils.getServerUrl();
        ClientConnectionUtil.waitForPort(Integer.parseInt(FrameworkSettings.HTTP_PORT));
        String sessionCookie = new LoginLogoutUtil().login();

        searchAdminService = new SearchAdminServiceClient(SERVER_URL, sessionCookie);
        registry = GregTestUtils.getRegistry();
        governance = GregTestUtils.getGovernanceRegistry(registry);
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
        TestUtils.cleanupResources(governance);
        addResources();
    }

    @Test(groups = {"wso2.greg"}, description = "Metadata search by available AssociationDestination")
    public void searchResourceByAssociationDestination()
            throws SearchAdminServiceRegistryExceptionException, RemoteException,
            RegistryException, InterruptedException {
        Assert.assertNotNull(destinationPath1, "Destination path not found for search Query. Clarity Error");
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setAssociationDest(RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + destinationPath1);
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = CommonUtils.getSearchResult(searchAdminService,searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid Association Destination");

        for (ResourceData resource : result.getResourceDataList()) {
            Association[] array = registry.getAllAssociations(resource.getResourcePath());
            Assert.assertNotNull(array, "Association list null");
            Assert.assertTrue(array.length > 0);
            boolean associationDestination = false;
            for (Association association : array) {
                if (association.getDestinationPath().contains(RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH
                        + destinationPath1)) {
                    associationDestination = true;
                    break;
                }
            }
            Assert.assertTrue(associationDestination, "Association Destination not found on Resource"
                                                      + resource.getResourcePath());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Metadata search by available " +
                                                              "Association Destination relative path")
    public void searchResourceByAssociationDestinationRelativePath()
            throws SearchAdminServiceRegistryExceptionException, RemoteException,
            RegistryException, InterruptedException {
        final String relativePath = "sns";
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setAssociationDest(relativePath);
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = CommonUtils.getSearchResult(searchAdminService,searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid Association Destination");

        for (ResourceData resource : result.getResourceDataList()) {
            Association[] array = registry.getAllAssociations(resource.getResourcePath());
            Assert.assertNotNull(array, "Association list null");
            Assert.assertTrue(array.length > 0);
            boolean associationDestination = false;
            for (Association association : array) {
                if (association.getDestinationPath().contains(relativePath)) {
                    associationDestination = true;
                    break;
                }
            }
            Assert.assertTrue(associationDestination, "Association Destination relative path not " +
                                                      "found on Resource" + resource.getResourcePath());
        }
    }

   /* @Test(priority = 2, groups = {"wso2.greg"}, description = "Metadata search by available Association Destinations")
    public void searchResourceByAssociationDestinations()
            throws SearchAdminServiceRegistryExceptionException, RemoteException,
                   RegistryException {

        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setAssociationDest("autoService1 autoService2");
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminService.getAdvancedSearchResults(sessionCookie, searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid Association Types");
        for (ResourceData resource : result.getResourceDataList()) {
            boolean associationDestinationFound = false;
            for (Association association : registry.getAllAssociations(resource.getResourcePath())) {
                if (association.getAssociationType().contains("autoService1")
                    || association.getAssociationType().contains("autoService2")) {
                    associationDestinationFound = true;
                    break;
                }
            }
            Assert.assertTrue(associationDestinationFound, "Association Destinations not found on Resource"
                                                           + resource.getResourcePath());

        }


    }*/

    /*@Test(priority = 3, groups = {"wso2.greg"}, description = "Metadata search by Association Destination pattern matching")
    public void searchResourceByAssociationDestinationPattern()
            throws SearchAdminServiceRegistryExceptionException, RemoteException,
                   RegistryException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setAssociationDest("%sns%autoService%");
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminService.getAdvancedSearchResults(sessionCookie, searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid Association Destination pattern");
        for (ResourceData resource : result.getResourceDataList()) {
            boolean associationDestinationFound = false;
            for (Association association : registry.getAllAssociations(resource.getResourcePath())) {
                if (association.getDestinationPath().contains("sns")
                    && association.getDestinationPath().contains("autoService")) {
                    associationDestinationFound = true;
                    break;
                }
            }
            Assert.assertTrue(associationDestinationFound, "Association Destination pattern not found on Resource"
                                                           + resource.getResourcePath());

        }

    }*/


    @Test(groups = {"wso2.greg"}, description = "Metadata search by unavailable Association Destination")
    public void searchResourceByUnAvailableAssociationDestination()
            throws SearchAdminServiceRegistryExceptionException, RemoteException, InterruptedException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setAssociationDest("xyz1234ggf76tgf");
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = CommonUtils.getSearchResult(searchAdminService,searchQuery);
        Assert.assertNull(result.getResourceDataList(), "Result Object found");
    }

    @Test(dataProvider = "invalidCharacter", groups = {"wso2.greg"}, dataProviderClass = Parameters.class,
          description = "Metadata search by Association Destination with invalid characters")
    public void searchResourceByAssociationDestinationWithInvalidCharacter(String invalidInput)
            throws SearchAdminServiceRegistryExceptionException, RemoteException, InterruptedException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();

        paramBean.setAssociationDest(invalidInput);
        ArrayOfString[] paramList = paramBean.getParameterList();
        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = null;
        try {
            result = CommonUtils.getSearchResult(searchAdminService,searchQuery);
        }finally {
            Assert.assertTrue(result.getErrorMessage().contains("illegal characters"),"Wrong exception");
        }
    }

    private void addResources() throws Exception {
        destinationPath1 = addService("sns1", "autoService1");
        String destinationPath2 = addService("sns2", "autoService2");
        addPolicy(destinationPath1, "associationType1");
        addSchema(destinationPath2, "associationType2");
    }

    private String addService(String nameSpace, String serviceName) throws Exception {
        ServiceManager serviceManager = new ServiceManager(governance);
        Service service;
        service = serviceManager.newService(new QName(nameSpace, serviceName));
        serviceManager.addService(service);
        for (String serviceId : serviceManager.getAllServiceIds()) {
            service = serviceManager.getService(serviceId);
            if (service.getPath().endsWith(serviceName)) {
                return service.getPath();
            }
        }
        throw new Exception("Getting Service path failed");
    }

    private void addSchema(String destinationPath, String typ)  throws IOException, RegistryException {
        SchemaManager schemaManager = new SchemaManager(governance);
        String schemaFilePath = GregTestUtils.getResourcePath()
                                + File.separator + "schema" + File.separator;
        Schema schema = schemaManager.newSchema(GregTestUtils.readFile(schemaFilePath + "Person.xsd").getBytes(), "Person1.xsd");
        schemaManager.addSchema(schema);
        schema = schemaManager.getSchema(schema.getId());
        governance.addAssociation(schema.getPath(), destinationPath, typ);
    }

    private void addPolicy(String destinationPath, String type) throws RegistryException, IOException {
        PolicyManager policyManager = new PolicyManager(governance);
        String policyFilePath = GregTestUtils.getResourcePath()
                                + File.separator + "policy" + File.separator;
        Policy policy = policyManager.newPolicy(GregTestUtils.readFile(policyFilePath + "UTPolicy.xml").getBytes(), "UTPolicy1.xml");
        policyManager.addPolicy(policy);
        policy = policyManager.getPolicy(policy.getId());
        governance.addAssociation(policy.getPath(), destinationPath, type);
    }

    @AfterClass
    public void destroy() {
        try {
            CommonUtils.cleanUpResource(registry);
        } catch (RegistryException ignore) {

        }
    }
}
