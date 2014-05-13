package org.wso2.carbon.registry.utfsupport.test;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.search.metadata.test.bean.SearchParameterBean;
import org.wso2.carbon.registry.search.stub.SearchAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.search.stub.beans.xsd.AdvancedSearchResultsBean;
import org.wso2.carbon.registry.search.stub.beans.xsd.ArrayOfString;
import org.wso2.carbon.registry.search.stub.beans.xsd.CustomSearchParameterBean;
import org.wso2.carbon.registry.utfsupport.test.util.UTFSupport;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.clients.SearchAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import java.io.IOException;
import java.rmi.RemoteException;

public class UTFSupportForResourceTestCase extends GREGIntegrationBaseTest {

    private ResourceAdminServiceClient resourceAdminServiceClient;
    private String utfString;
    private SearchAdminServiceClient searchAdminServiceClient;
    private WSRegistryServiceClient wsRegistryServiceClient;
    private RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();

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

        wsRegistryServiceClient =
                registryProviderUtil.getWSRegistry(automationContext);
        searchAdminServiceClient =
                new SearchAdminServiceClient(backEndUrl,
                                             sessionCookie);

        resourceAdminServiceClient =
                new ResourceAdminServiceClient(backEndUrl,
                                               sessionCookie);

    }

    @Test(groups = {"wso2.greg"}, description = "read UTF characters from file")
    public void testreadFile() throws IOException {
        utfString = UTFSupport.readFile();

    }

    @Test(groups = {"wso2.greg"}, description = "add collection", dependsOnMethods = "testreadFile")
    public void testAddCollection() throws ResourceAdminServiceExceptionException, RemoteException {

        resourceAdminServiceClient.addCollection("/", "collection" + utfString, "other", "test collection");
        Assert.assertTrue(resourceAdminServiceClient.
                getResource("/collection" + utfString)[0].getAuthorUserName().equals(userNameWithoutDomain));
    }


    @AfterClass
    public void clean() throws ResourceAdminServiceExceptionException, RemoteException,
                               SearchAdminServiceRegistryExceptionException, RegistryException {
        delete("/collection" + utfString);
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setResourceName("collection" + utfString);


        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        paramList = null;
        AdvancedSearchResultsBean result =
                searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
        boolean collectionDeleted = true;
        if ((result.getResourceDataList() != null)) {
            collectionDeleted = false;
        }
        Assert.assertTrue(collectionDeleted);
        utfString = null;
        searchAdminServiceClient = null;
        resourceAdminServiceClient = null;
        wsRegistryServiceClient = null;
        registryProviderUtil = null;
    }

    public void delete(String destPath)
            throws ResourceAdminServiceExceptionException, RemoteException, RegistryException {
        if (wsRegistryServiceClient.resourceExists(destPath)) {
            resourceAdminServiceClient.deleteResource(destPath);
        }
    }
}
