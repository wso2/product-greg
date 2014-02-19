package org.wso2.carbon.registry.utfsupport.test;

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
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.search.metadata.test.bean.SearchParameterBean;
import org.wso2.carbon.registry.search.stub.SearchAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.search.stub.beans.xsd.AdvancedSearchResultsBean;
import org.wso2.carbon.registry.search.stub.beans.xsd.ArrayOfString;
import org.wso2.carbon.registry.search.stub.beans.xsd.CustomSearchParameterBean;
import org.wso2.carbon.registry.utfsupport.test.util.UTFSupport;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import java.io.IOException;
import java.rmi.RemoteException;

public class UTFSupportForResourceTestCase {

    private ResourceAdminServiceClient resourceAdminServiceClient;
    private String utfString;
    private UserInfo userInfo;
    private SearchAdminServiceClient searchAdminServiceClient;
    private WSRegistryServiceClient wsRegistryServiceClient;
    private RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();

    @BeforeClass
    public void init() throws Exception {
        int userId = ProductConstant.ADMIN_USER_ID;
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        ManageEnvironment environment = builder.build();
        userInfo = UserListCsvReader.getUserInfo(userId);
        wsRegistryServiceClient =
                registryProviderUtil.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        searchAdminServiceClient =
                new SearchAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                             environment.getGreg().getSessionCookie());

        resourceAdminServiceClient =
                new ResourceAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                               environment.getGreg().getSessionCookie());

    }

    @Test(groups = {"wso2.greg"}, description = "read UTF characters from file")
    public void testreadFile() throws IOException {
        utfString = UTFSupport.readFile();

    }

    @Test(groups = {"wso2.greg"}, description = "add collection", dependsOnMethods = "testreadFile")
    public void testAddCollection() throws ResourceAdminServiceExceptionException, RemoteException {

        resourceAdminServiceClient.addCollection("/", "collection" + utfString, "other", "test collection");
        Assert.assertTrue(resourceAdminServiceClient.
                getResource("/collection" + utfString)[0].getAuthorUserName().equals(userInfo.getUserNameWithoutDomain()));
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
