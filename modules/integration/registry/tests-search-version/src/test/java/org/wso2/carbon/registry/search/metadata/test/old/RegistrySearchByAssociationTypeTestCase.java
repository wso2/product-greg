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
package org.wso2.carbon.registry.search.metadata.test.old;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.registry.SearchAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.core.utils.fileutils.FileManager;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.policies.PolicyManager;
import org.wso2.carbon.governance.api.policies.dataobjects.Policy;
import org.wso2.carbon.governance.api.schema.SchemaManager;
import org.wso2.carbon.governance.api.schema.dataobjects.Schema;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.search.metadata.test.bean.SearchParameterBean;
import org.wso2.carbon.registry.search.metadata.test.utils.CommonUtils;
import org.wso2.carbon.registry.search.metadata.test.utils.Parameters;
import org.wso2.carbon.registry.search.stub.SearchAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.search.stub.beans.xsd.AdvancedSearchResultsBean;
import org.wso2.carbon.registry.search.stub.beans.xsd.ArrayOfString;
import org.wso2.carbon.registry.search.stub.beans.xsd.CustomSearchParameterBean;
import org.wso2.carbon.registry.search.stub.common.xsd.ResourceData;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;

/*
Search Registry metadata by AssociationType
 */
public class RegistrySearchByAssociationTypeTestCase {
    private WSRegistryServiceClient registry;
    private final int userId = 1;
    private final UserInfo userInfo = UserListCsvReader.getUserInfo(userId);
    private SearchAdminServiceClient searchAdminServiceClient;
    ;
    private Registry governance;

    @BeforeClass
    public void init() throws Exception {

        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        ManageEnvironment environment = builder.build();
        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        registry = registryProviderUtil.getWSRegistry(userId,
                ProductConstant.GREG_SERVER_NAME);
        governance = registryProviderUtil.getGovernanceRegistry(registry,
                userId);
        searchAdminServiceClient = new SearchAdminServiceClient(environment.getGreg().getBackEndUrl(),
                userInfo.getUserName(), userInfo.getPassword());
        addResources();
    }

    @Test(groups = {"wso2.greg"}, description = "Metadata search by available AssociationType")
    public void searchResourceByAssociationType()
            throws SearchAdminServiceRegistryExceptionException, RemoteException,
            RegistryException, InterruptedException {
        final String searchAssociationType = "associationType1";
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setAssociationType(searchAssociationType);
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = CommonUtils.getSearchResult(searchAdminServiceClient, searchQuery);
        Assert.assertNotNull(result, "Result object null");
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid Association Type");

        for (ResourceData resource : result.getResourceDataList()) {
            Assert.assertNotNull(resource, "Resource object null");
            Association[] array = registry.getAssociations(resource.getResourcePath(), searchAssociationType);
            Assert.assertNotNull(array, "Association list null");
            Assert.assertTrue(array.length > 0);
            for (Association association : array) {
                Assert.assertEquals(association.getAssociationType(), searchAssociationType,
                        "AssociationT type not found on Resource");
            }
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Metadata search by unavailable AssociationType", dependsOnMethods = "searchResourceByAssociationType")
    public void searchResourceByUnAvailableAssociationType()
            throws SearchAdminServiceRegistryExceptionException, RemoteException, InterruptedException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setAssociationType("xyz1234ggf");
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = CommonUtils.getSearchResult(searchAdminServiceClient, searchQuery);
        Assert.assertNull(result.getResourceDataList(), "Result Object found");


    }

    @Test(dataProvider = "invalidCharacter", groups = {"wso2.greg"}, dataProviderClass = Parameters.class,
            description = "Metadata search by AssociationType with invalid characters", dependsOnMethods = "searchResourceByUnAvailableAssociationType")
    public void searchResourceByAssociationTypeWithInvalidCharacter(String invalidInput)
            throws SearchAdminServiceRegistryExceptionException, RemoteException, InterruptedException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();

        paramBean.setAssociationType(invalidInput);
        ArrayOfString[] paramList = paramBean.getParameterList();
        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = null;
        try {
            result = CommonUtils.getSearchResult(searchAdminServiceClient, searchQuery);
        } finally {
            Assert.assertTrue(result.getErrorMessage().contains("illegal characters"), "Wrong exception");
        }
    }

    private void addResources() throws Exception {

        String path = addWSDL("echoSearch.wsdl", governance, "echoAssociationService");
        addPolicy(path, "associationType1");
        addSchema(path, "associationType2");
    }

    private void addSchema(String destinationPath, String typ)
            throws IOException, RegistryException {
        SchemaManager schemaManager = new SchemaManager(governance);
        String schemaFilePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION
                + "artifacts" + File.separator + "GREG" + File.separator + "schema" + File.separator;
        Schema schema = schemaManager.newSchema(FileManager.readFile(schemaFilePath + "Person.xsd").getBytes(),
                "Person2.xsd");
        schemaManager.addSchema(schema);
        schema = schemaManager.getSchema(schema.getId());
        registry.addAssociation(RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + schema.getPath(),
                RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + destinationPath, typ);
    }

    private void addPolicy(String destinationPath, String typ)
            throws RegistryException, IOException {
        PolicyManager policyManager = new PolicyManager(governance);
        String policyFilePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION
                + "artifacts" + File.separator + "GREG" + File.separator + "policy" + File.separator;
        Policy policy = policyManager.newPolicy(FileManager.readFile(policyFilePath + "UTPolicy.xml").getBytes(),
                "UTPolicy2.xml");
        policyManager.addPolicy(policy);
        policy = policyManager.getPolicy(policy.getId());
        registry.addAssociation(RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + policy.getPath(),
                RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + destinationPath, typ);
    }

    private static String addWSDL(String name, Registry governance, String serviceName)
            throws IOException, RegistryException {
        WsdlManager wsdlManager = new WsdlManager(governance);
        Wsdl wsdl;
        String wsdlFilePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION
                + "artifacts" + File.separator + "GREG" + File.separator + "wsdl" + File.separator;
        wsdl = wsdlManager.newWsdl(FileManager.readFile(wsdlFilePath + "echo.wsdl")
                .replaceFirst("wsdl:service name=\"echoyuSer1\"",
                        "wsdl:service name=\"" + serviceName + "\"").getBytes(), name);
        wsdlManager.addWsdl(wsdl);
        wsdl = wsdlManager.getWsdl(wsdl.getId());

        return wsdl.getPath();
    }

    @AfterClass
    public void destroy() throws GovernanceException {
        WsdlManager wsdlManager = new WsdlManager(governance);
        Wsdl[] wsdls = wsdlManager.getAllWsdls();
        for (int i = 0; i < wsdls.length; i++) {
            wsdlManager
                    .removeWsdl(wsdls[i].getId());
        }

        SchemaManager schemaManager = new SchemaManager(governance);
        Schema[] schemas = schemaManager.getAllSchemas();
        for (int i = 0; i < schemas.length; i++) {
            schemaManager.removeSchema(schemas[i].getId());
        }

        PolicyManager policyManager = new PolicyManager(governance);
        Policy[] policies = policyManager.getAllPolicies();
        for (int i = 0; i < policies.length; i++) {
            policyManager.removePolicy(policies[i].getId());
        }
        searchAdminServiceClient = null;
        governance = null;
        registry = null;
    }
}
