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
import org.wso2.carbon.application.mgt.stub.ApplicationAdminExceptionException;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.registry.capp.deployment.test.utils.CAppTestUtils;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.search.metadata.test.bean.SearchParameterBean;
import org.wso2.carbon.registry.search.stub.SearchAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.search.stub.beans.xsd.AdvancedSearchResultsBean;
import org.wso2.carbon.registry.search.stub.beans.xsd.ArrayOfString;
import org.wso2.carbon.registry.search.stub.beans.xsd.CustomSearchParameterBean;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.clients.ApplicationAdminClient;
import org.wso2.greg.integration.common.clients.CarbonAppUploaderClient;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.clients.SearchAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import javax.activation.DataHandler;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

public class RegistrySpecialSearchTestCase extends GREGIntegrationBaseTest {


    private CarbonAppUploaderClient cAppUploader;
    private SearchAdminServiceClient searchAdminServiceClient;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private ApplicationAdminClient applicationAdminClient;
    private WSRegistryServiceClient wsRegistryServiceClient;
    private String CAppName = "text_resources_1.0.0";
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

        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();

        wsRegistryServiceClient =
                registryProviderUtil.getWSRegistry(automationContext);

        cAppUploader =
                new CarbonAppUploaderClient(backEndUrl,
                                            sessionCookie);

        searchAdminServiceClient =
                new SearchAdminServiceClient(backEndUrl,
                                             sessionCookie);

        resourceAdminServiceClient =
                new ResourceAdminServiceClient(backEndUrl,
                                               sessionCookie);
        applicationAdminClient =
                new ApplicationAdminClient(backEndUrl,
                                           sessionCookie);

        uploadCApplication();

    }

    public void uploadCApplication()
            throws MalformedURLException, RemoteException, InterruptedException,
                   ApplicationAdminExceptionException {
        String filePath = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator +
                          "GREG" + File.separator + "car" + File.separator + "text_resources_1.0.0.car";

        cAppUploader.uploadCarbonAppArtifact("text_resources_1.0.0.car",
                                             new DataHandler(new URL("file:///" + filePath)));
        Thread.sleep(60000);

    }

    @Test(groups = {"wso2.greg"}, description = "verify CApp search")
    public void verifyCAppSearchContent()
            throws SearchAdminServiceRegistryExceptionException, RemoteException,
                   ResourceAdminServiceExceptionException, InterruptedException,
                   ApplicationAdminExceptionException {
        Assert.assertTrue(CAppTestUtils.isCAppDeployed(sessionCookie,CAppName
                                                       , applicationAdminClient));
        Assert.assertTrue(searchResource("text_files.xml"));
        Assert.assertTrue(searchResource("buggggg.txt"));
    }


    public boolean searchResource(String resourceName)
            throws SearchAdminServiceRegistryExceptionException, RemoteException,
                   ResourceAdminServiceExceptionException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setResourceName(resourceName);
        boolean resourceExists = false;
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        paramList = null;
        AdvancedSearchResultsBean result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0));

        for (org.wso2.carbon.registry.search.stub.common.xsd.ResourceData resource : result.getResourceDataList()) {
            if (resource.getResourcePath().equals("/_system/capps/" + resourceName)) {
                resourceExists = true;
                break;
            }

        }

        return resourceExists;
    }

    @AfterClass
    public void clean() throws ApplicationAdminExceptionException, RemoteException,
                               ResourceAdminServiceExceptionException, RegistryException {
        applicationAdminClient.deleteApplication(CAppName);
        delete("/_system/capps");
        searchAdminServiceClient = null;
        resourceAdminServiceClient = null;
        cAppUploader = null;
        applicationAdminClient = null;
    }

    public void delete(String destPath)
            throws ResourceAdminServiceExceptionException, RemoteException, RegistryException {
        if (wsRegistryServiceClient.resourceExists(destPath)) {
            resourceAdminServiceClient.deleteResource(destPath);
        }
    }

}
