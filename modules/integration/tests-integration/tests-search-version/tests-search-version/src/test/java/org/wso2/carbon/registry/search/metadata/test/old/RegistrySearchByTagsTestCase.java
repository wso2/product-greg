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
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.automation.test.utils.common.FileManager;
import org.wso2.carbon.governance.api.policies.PolicyManager;
import org.wso2.carbon.governance.api.policies.dataobjects.Policy;
import org.wso2.carbon.governance.api.schema.SchemaManager;
import org.wso2.carbon.governance.api.schema.dataobjects.Schema;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Tag;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.search.metadata.test.bean.SearchParameterBean;
import org.wso2.carbon.registry.search.metadata.test.utils.Parameters;
import org.wso2.carbon.registry.search.stub.SearchAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.search.stub.beans.xsd.AdvancedSearchResultsBean;
import org.wso2.carbon.registry.search.stub.beans.xsd.ArrayOfString;
import org.wso2.carbon.registry.search.stub.beans.xsd.CustomSearchParameterBean;
import org.wso2.carbon.registry.search.stub.common.xsd.ResourceData;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.clients.SearchAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;

/*
Search Registry metadata by Tags
 */
public class RegistrySearchByTagsTestCase extends GREGIntegrationBaseTest {

    private WSRegistryServiceClient registry;
    private SearchAdminServiceClient searchAdminServiceClient;
    private Registry governance;
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

        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        registry = registryProviderUtil.getWSRegistry(automationContext);
        governance = registryProviderUtil.getGovernanceRegistry(registry,
                automationContext);
        searchAdminServiceClient = new SearchAdminServiceClient(backEndUrl, sessionCookie);

        addResources();

    }

    @Test(groups = {"wso2.greg"}, description = "Metadata search by available Tag")
    public void searchResourceByTag()
            throws SearchAdminServiceRegistryExceptionException, RemoteException,
            RegistryException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setTags("AB");
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid Tag");

        for (ResourceData resource : result.getResourceDataList()) {
            boolean tagFound = false;
            for (Tag tag : registry.getTags(resource.getResourcePath())) {
                if ("AB".equalsIgnoreCase(tag.getTagName())) {
                    tagFound = true;
                    break;
                }
            }
            Assert.assertTrue(tagFound, "Tag not found on Resource " + resource.getResourcePath());

        }


    }

    @Test(groups = {"wso2.greg"}, description = "Metadata search by available Tags")
    public void searchResourceByTags()
            throws SearchAdminServiceRegistryExceptionException, RemoteException,
            RegistryException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setTags("AB,BC");
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid Tags");
        for (ResourceData resource : result.getResourceDataList()) {
            boolean tagFound = false;
            for (Tag tag : registry.getTags(resource.getResourcePath())) {
                if ("AB".equalsIgnoreCase(tag.getTagName()) || "BC".equalsIgnoreCase(tag.getTagName())) {
                    tagFound = true;
                    break;
                }
            }
            Assert.assertTrue(tagFound, "Tags not found on Resource " + resource.getResourcePath());

        }


    }


    @Test(groups = {"wso2.greg"}, description = "Metadata search by unavailable tag")
    public void searchResourceByUnAvailableTag()
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setTags("xyz1234");
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = null;
        try {
            result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
        } finally {
            if (result != null) {
                if (result.getResourceDataList() != null) {
                    Assert.assertNull(result.getResourceDataList()[0], "Results found");
                } else {
                    Assert.assertNull(result.getResourceDataList(), "Result Object found.");
                }
            } else {
                Assert.assertNull(result, "No results returned");
            }
        }


    }

    @Test(dataProvider = "invalidCharacterForTags", groups = {"wso2.greg"}, dataProviderClass = Parameters.class,
            description = "Metadata search by tag with invalid characters")
    public void searchResourceByTagNameWithInvalidCharacter(String invalidInput)
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();

        paramBean.setTags(invalidInput);
        ArrayOfString[] paramList = paramBean.getParameterList();
        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = null;
        try {
            result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
        } finally {
            Assert.assertTrue(result.getErrorMessage().contains("illegal characters"), "Wrong exception for input "
                    + invalidInput);
        }


    }

    @Test(dataProvider = "invalidCharacterForTags2", groups = {"wso2.greg"}, dataProviderClass = Parameters.class,
            description = "Metadata search by tag with invalid characters")
    public void searchResourceByTagNameWithInvalidCharacter2(String invalidInput)
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();

        paramBean.setTags(invalidInput);
        ArrayOfString[] paramList = paramBean.getParameterList();
        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = null;
        try {
            result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
        } finally {
            Assert.assertTrue(result.getErrorMessage().contains("At least one field must be filled"), "Wrong exception for input "
                    + invalidInput);
        }


    }

    private void addResources() throws RegistryException, IOException {
        addService("sns1", "autoService1", "AB");
        addService("sns2", "autoService2", "BC");
        addWSDL("AB");
        addPolicy("BC");
        addSchema("ABB");
    }

    private void addService(String nameSpace, String serviceName, String tag)
            throws RegistryException {
        ServiceManager serviceManager = new ServiceManager(governance);
        Service service;
        service = serviceManager.newService(new QName(nameSpace, serviceName));
        serviceManager.addService(service);
        for (String serviceId : serviceManager.getAllServiceIds()) {
            service = serviceManager.getService(serviceId);
            if (service.getPath().endsWith(serviceName)) {
                governance.applyTag(service.getPath(), tag);

            }

        }

    }

    private void addWSDL(String tag) throws IOException, RegistryException {
        WsdlManager wsdlManager = new WsdlManager(governance);
        Wsdl wsdl;
        String wsdlFilePath = FrameworkPathUtil.getSystemResourceLocation()
                + "artifacts" + File.separator + "GREG" + File.separator + "wsdl" + File.separator;
        wsdl = wsdlManager.newWsdl(FileManager.readFile(wsdlFilePath + "echo.wsdl").getBytes(), "echo.wsdl");
        wsdlManager.addWsdl(wsdl);
        wsdl = wsdlManager.getWsdl(wsdl.getId());
        governance.applyTag(wsdl.getPath(), tag);
    }

    private void addSchema(String tag) throws IOException, RegistryException {
        SchemaManager schemaManager = new SchemaManager(governance);
        String schemaFilePath = FrameworkPathUtil.getSystemResourceLocation()
                + "artifacts" + File.separator + "GREG" + File.separator + "schema" + File.separator;
        Schema schema = schemaManager.newSchema(FileManager.readFile(schemaFilePath + "Person.xsd").getBytes(), "Person.xsd");
        schemaManager.addSchema(schema);
        schema = schemaManager.getSchema(schema.getId());
        governance.applyTag(schema.getPath(), tag);
    }

    private void addPolicy(String tag) throws RegistryException, IOException {
        PolicyManager policyManager = new PolicyManager(governance);
        String policyFilePath = FrameworkPathUtil.getSystemResourceLocation()
                + "artifacts" + File.separator + "GREG" + File.separator + "policy" + File.separator;
        Policy policy = policyManager.newPolicy(FileManager.readFile(policyFilePath + "UTPolicy.xml").getBytes(), "UTPolicy.xml");
        policyManager.addPolicy(policy);
        policy = policyManager.getPolicy(policy.getId());
        governance.applyTag(policy.getPath(), tag);

    }

    @AfterClass
    public void destroy() throws RegistryException {
        WsdlManager wsdlManager = new WsdlManager(governance);
        Wsdl[] wsdls = wsdlManager.getAllWsdls();
        for (int i = 0; i < wsdls.length; i++) {
            wsdlManager.removeWsdl(wsdls[i].getId());
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

        ServiceManager serviceManager = new ServiceManager(governance);
        String[] services = serviceManager.getAllServiceIds();
        for (int i = 0; i < services.length; i++) {
            serviceManager.removeService(services[i]);
        }

        searchAdminServiceClient = null;
        governance = null;
        registry = null;
    }
}
