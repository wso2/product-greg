package org.wso2.carbon.registry.search.metadata.test;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.automation.test.utils.common.FileManager;
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

public class RegistrySearchPropertyValueByText extends GREGIntegrationBaseTest {

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

        ServerConfigurationManager serverConfigurationManager = new ServerConfigurationManager("GREG",TestUserMode.SUPER_TENANT_ADMIN);
        File targetFile = new File(FrameworkPathUtil.getCarbonServerConfLocation() + File.separator + "registry.xml");
        File sourceFile = new File(FrameworkPathUtil.getSystemResourceLocation() + "registry.xml");
        serverConfigurationManager.applyConfiguration(sourceFile,targetFile);

        backEndUrl = getBackendURL();
        sessionCookie = getSessionCookie();
        userName = automationContext.getContextTenant().getContextUser().getUserName();

        if (userName.contains("@"))
            userNameWithoutDomain = userName.substring(0, userName.indexOf('@'));
        else
            userNameWithoutDomain = userName;

        searchAdminServiceClient =
                new SearchAdminServiceClient(backEndUrl,
                                             sessionCookie);
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(backEndUrl,
                                               sessionCookie);
        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        wsRegistry = registryProviderUtil.getWSRegistry(automationContext);
        governance = registryProviderUtil.getGovernanceRegistry(wsRegistry, automationContext);

    }

    @Test(groups = {"wso2.greg"}, description = "add wsdl")

    public void addWSDL()
            throws IOException, ResourceAdminServiceExceptionException, RegistryException, InterruptedException {

        WsdlManager wsdlManager = new WsdlManager(governance);
        Wsdl wsdl;
        String wsdlFilePath = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" +
                              File.separator + "GREG" + File.separator + "wsdl" + File.separator + "AmazonWebServices.wsdl";
        wsdl = wsdlManager.newWsdl(FileManager.readFile(wsdlFilePath).getBytes(), "AmazonWebServices.wsdl");
        wsdlManager.addWsdl(wsdl);
        wsdl = wsdlManager.getWsdl(wsdl.getId());
        Resource resource = governance.get(wsdl.getPath());
        resource.addProperty("wsdlPropertyText", "ee");
        governance.put(wsdl.getPath(), resource);
        Thread.sleep(60000);
    }

    @Test(groups = {"wso2.greg"}, description = "Search by Property value a <= X < b", dependsOnMethods = "addWSDL")
    public void searchByPropertyValueText() throws SearchAdminServiceRegistryExceptionException,
                                                   RemoteException, RegistryException {


        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setPropertyName("wsdlPropertyText");

        paramBean.setRightPropertyValue("ee");

        paramBean.setRightOperator("eq");
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid property name");

        for (ResourceData resource : result.getResourceDataList()) {
            boolean propertyFound = false;
            Iterator<String> properties = (Iterator<String>) wsRegistry.get(resource.getResourcePath())
                    .getProperties().propertyNames();
            while (properties.hasNext()) {
                if (properties.next().contains("wsdlPropertyText")) {

                    Assert.assertTrue((wsRegistry.get(resource.getResourcePath()).getProperty("wsdlPropertyText").equals("ee")));
                    propertyFound = true;
                }
            }
            Assert.assertTrue(propertyFound, "Property name not found on Resource " + resource.getResourcePath());
        }
    }

    @AfterClass
    public void clean() throws ResourceAdminServiceExceptionException, RemoteException, RegistryException {
        delete("/_system/governance/trunk/wsdls/com/amazon/soap/1.0.0/AmazonWebServices.wsdl");
        delete("/_system/governance/trunk/services/com/amazon/soap/1.0.0/AmazonSearchService");
        delete("/_system/governance/trunk/endpoints/com");
        searchAdminServiceClient = null;
        governance = null;
        wsRegistry = null;
        resourceAdminServiceClient = null;

    }

     public void delete(String destPath) throws ResourceAdminServiceExceptionException, RemoteException, RegistryException {
        if(wsRegistry.resourceExists(destPath)){
            resourceAdminServiceClient.deleteResource(destPath);
        }
    }

}
