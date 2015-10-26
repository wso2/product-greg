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
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
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
import java.rmi.RemoteException;

/*
Search Registry metadata by mediaType
 */
public class RegistrySearchByMediaTypeTestCase extends GREGIntegrationBaseTest {

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
        searchAdminServiceClient = new SearchAdminServiceClient(backEndUrl, sessionCookie);
        governance = registryProviderUtil.getGovernanceRegistry(registry, automationContext);
        ServiceManager serviceManager = new ServiceManager(governance);
        Service service = serviceManager.newService(new QName("http://abc.mediatype.com",
                "RegistrySearchByMediaTypeTestCase"));
        serviceManager.addService(service);
    }

    @Test(groups = {"wso2.greg"}, description = "Metadata search by available MediaType")
    public void searchResourceByMediaType()
            throws SearchAdminServiceRegistryExceptionException, RemoteException,
            RegistryException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setMediaType("application/vnd.wso2-service+xml");
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid MediaType");
        for (ResourceData resource : result.getResourceDataList()) {
            Assert.assertEquals(registry.get(resource.getResourcePath()).getMediaType(), "application/vnd.wso2-service+xml",
                    "search keyword not contain on MediaType :" + resource.getResourcePath());
        }


    }

    @Test(groups = {"wso2.greg"}, description = "Metadata search by MediaType pattern matching")
    public void searchResourceByMediaTypePattern()
            throws SearchAdminServiceRegistryExceptionException, RemoteException,
            RegistryException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setMediaType("%vnd%service%");
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid MediaType pattern");
        for (ResourceData resource : result.getResourceDataList()) {
            Resource rs = registry.get(resource.getResourcePath());
            Assert.assertTrue((rs.getMediaType().contains("vnd") && rs.getMediaType().contains("service")),
                    "search word pattern not contain on MediaType :" + resource.getResourcePath());
        }


    }


    @Test(groups = {"wso2.greg"}, description = "Metadata search by unavailable MediaType")
    public void searchResourceByUnAvailableMediaType()
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setMediaType("xyz1234");
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
        Assert.assertNull(result.getResourceDataList(), "Result Object found");


    }

    @Test(dataProvider = "invalidCharacterForMediaType", groups = {"wso2.greg"}, dataProviderClass = Parameters.class,
            description = "Metadata search by MediaType with invalid characters")
    public void searchResourceByMediaTypeWithInvalidCharacter(String invalidInput)
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();

        paramBean.setMediaType(invalidInput);
        ArrayOfString[] paramList = paramBean.getParameterList();
        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = null;
        try {
            result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
        } finally {
            Assert.assertTrue(result.getErrorMessage().contains("illegal characters"), "Wrong exception");
        }
    }

    @AfterClass
    public void destroy() throws RegistryException {
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
