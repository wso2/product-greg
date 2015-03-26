package org.wso2.carbon.registry.utfsupport.test;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.application.mgt.stub.ApplicationAdminExceptionException;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.registry.capp.deployment.test.utils.CAppTestUtils;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.search.metadata.test.bean.SearchParameterBean;
import org.wso2.carbon.registry.search.stub.SearchAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.search.stub.beans.xsd.AdvancedSearchResultsBean;
import org.wso2.carbon.registry.search.stub.beans.xsd.ArrayOfString;
import org.wso2.carbon.registry.search.stub.beans.xsd.CustomSearchParameterBean;
import org.wso2.carbon.registry.search.stub.common.xsd.ResourceData;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.clients.ApplicationAdminClient;
import org.wso2.greg.integration.common.clients.CarbonAppUploaderClient;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.clients.SearchAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import javax.activation.DataHandler;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Arrays;

public class UTFSupportForCAPPTestCase extends GREGIntegrationBaseTest {

    private ResourceAdminServiceClient resourceAdminServiceClient;
    private SearchAdminServiceClient searchAdminServiceClient;
    private CarbonAppUploaderClient cAppUploader;
    private ApplicationAdminClient applicationAdminClient;
    private RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
    private WSRegistryServiceClient wsRegistryServiceClient;

    private String sessionCookie;
    private String backEndUrl;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);

        backEndUrl = getBackendURL();
        sessionCookie = getSessionCookie();

        wsRegistryServiceClient = registryProviderUtil.getWSRegistry(automationContext);
        cAppUploader =
                new CarbonAppUploaderClient(backEndUrl,
                                            sessionCookie);
        applicationAdminClient =
                new ApplicationAdminClient(backEndUrl,
                                           sessionCookie);
        searchAdminServiceClient =
                new SearchAdminServiceClient(backEndUrl,
                                             sessionCookie);
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(backEndUrl,
                                               sessionCookie);


    }

    @Test(groups = {"wso2.greg"}, description = "upload CAPP")
    public void testuploadCapp() throws IOException, SearchAdminServiceRegistryExceptionException,
                                        InterruptedException, ApplicationAdminExceptionException {

        String filePath = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator +
                          "GREG" + File.separator + "car" + File.separator + "text_resources2_1.0.0.car";

        cAppUploader.uploadCarbonAppArtifact("text_resources2_1.0.0.car",
                                             new DataHandler(new URL("file:///" + filePath)));
        Thread.sleep(30000);

        Assert.assertTrue(CAppTestUtils.isCAppDeployed(sessionCookie, "text_resources_1.0.0",
                                                       applicationAdminClient));

        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setResourceName("buggggg.txt");
        //buggggg.txt contains utf 8 characters
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        boolean resourceExists = false;
        AdvancedSearchResultsBean result;
        long startTime = System.currentTimeMillis();
        do {
            result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
            if (result.getResourceDataList() != null) {
                Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
                Assert.assertTrue((result.getResourceDataList().length > 0));

                for (ResourceData resource : result.getResourceDataList()) {

                    if (resource.getResourcePath().equals("/_system/capps/buggggg.txt")) {
                        resourceExists = true;
                        break;
                    }
                }
            }

        }
        while ((result.getResourceDataList() == null) && ((System.currentTimeMillis() - startTime) <= 60 * 1000));
        Assert.assertTrue(resourceExists);
    }

    @Test(groups = {"wso2.greg"}, description = "upload CAPP")
    public void testuploadCappwithGar()
            throws IOException, SearchAdminServiceRegistryExceptionException,
                   InterruptedException, ApplicationAdminExceptionException {

        String filePath = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator +
                          "GREG" + File.separator + "car" + File.separator + "Capp_1.0.0.car";

        cAppUploader.uploadCarbonAppArtifact("Capp_1.0.0.car",
                                             new DataHandler(new URL("file:///" + filePath)));
        Thread.sleep(30000);

        Assert.assertTrue(CAppTestUtils.isCAppDeployed(sessionCookie, "Capp_1.0.0", applicationAdminClient));

        Assert.assertTrue(searchWsdl());
        Assert.assertTrue(searchSchema());


    }

    public boolean searchWsdl()
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setResourceName("listing5.wsdl");

        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0));
        boolean resourceExists = false;
        for (ResourceData resource : result.getResourceDataList()) {

            if (resource.getResourcePath().equals("/_system/governance/trunk/wsdls/listing5/1.0.0/listing5.wsdl")) {
                resourceExists = true;
                break;
            }
        }
        return resourceExists;
    }

    public boolean searchSchema()
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setResourceName("listing3.xsd");

        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0));
        boolean resourceExists = false;
        for (ResourceData resource : result.getResourceDataList()) {

            if (resource.getResourcePath().equals("/_system/governance/trunk/schemas/listing3/1.0.0/listing3.xsd")) {
                resourceExists = true;
                break;
            }
        }
        return resourceExists;
    }


    @AfterClass(alwaysRun = true)
    public void clean() throws ResourceAdminServiceExceptionException, RemoteException,
                               ApplicationAdminExceptionException, RegistryException {

        delete("/_system/capps");
        deleteApplication("text_resources");
        deleteApplication("Capp");
        delete("/_system/governance/trunk/services/com/strikeiron/www/1.0.0/DoNotCallRegistry");
        delete("/_system/governance/trunk/services/com/example/stockquote_wsdl/1.0.0/StockQuoteService");
        delete("/_system/governance/trunk/wsdls/com/strikeiron/www/1.0.0/Automated Name With Spaces.wsdl");
        delete("/_system/governance/trunk/wsdls/com/example/stockquote_wsdl/1.0.0/encodedURL_artifactGiven.wsdl");
        delete("/_system/governance/trunk/wsdls/listing5/1.0.0/listing5.wsdl");
        delete("/_system/governance/trunk/schemas/listing3/1.0.0/listing3.xsd");
        delete("/_system/governance/trunk/endpoints/com");

        resourceAdminServiceClient = null;
        applicationAdminClient = null;
        searchAdminServiceClient = null;
        cAppUploader = null;
        wsRegistryServiceClient = null;
        registryProviderUtil = null;


    }

    public void delete(String destPath)
            throws ResourceAdminServiceExceptionException, RemoteException, RegistryException {
        if (wsRegistryServiceClient.resourceExists(destPath)) {
            resourceAdminServiceClient.deleteResource(destPath);
        }
    }

    public void deleteApplication(String name)
            throws ApplicationAdminExceptionException, RemoteException, RegistryException {
        if (Arrays.asList(applicationAdminClient.listAllApplications()).contains(name)) {
            applicationAdminClient.deleteApplication(name);
        }
    }


}
