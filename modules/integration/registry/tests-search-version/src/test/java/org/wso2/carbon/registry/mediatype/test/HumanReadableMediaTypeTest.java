/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.registry.mediatype.test;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.application.mgt.stub.ApplicationAdminExceptionException;
import org.wso2.carbon.automation.api.clients.application.mgt.ApplicationAdminClient;
import org.wso2.carbon.automation.api.clients.application.mgt.CarbonAppUploaderClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.api.clients.registry.SearchAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.core.annotations.SetEnvironment;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.registry.capp.deployment.test.utils.CAppTestUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.search.metadata.test.bean.SearchParameterBean;
import org.wso2.carbon.registry.search.stub.beans.xsd.AdvancedSearchResultsBean;
import org.wso2.carbon.registry.search.stub.beans.xsd.ArrayOfString;
import org.wso2.carbon.registry.search.stub.beans.xsd.CustomSearchParameterBean;
import org.wso2.carbon.registry.search.stub.common.xsd.ResourceData;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import javax.activation.DataHandler;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;


public class HumanReadableMediaTypeTest {

    private ResourceAdminServiceClient resourceAdminServiceClient;
    private CarbonAppUploaderClient cAppUploader;
    private SearchAdminServiceClient searchAdminServiceClient;
    private ApplicationAdminClient applicationAdminClient;
    private ManageEnvironment environment;
    private Registry governance;
    private long startTime;
    private WSRegistryServiceClient wsRegistry;

    @BeforeClass(alwaysRun = true)
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_user,
                                             ExecutionEnvironment.integration_tenant})
    public void init() throws Exception {

        int userId = 2;
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        environment = builder.build();
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                               environment.getGreg().getSessionCookie());
        cAppUploader =
                new CarbonAppUploaderClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                            environment.getGreg().getSessionCookie());
        searchAdminServiceClient =
                new SearchAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                             environment.getGreg().getSessionCookie());
        applicationAdminClient =
                new ApplicationAdminClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                           environment.getGreg().getSessionCookie());
        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        wsRegistry = registryProviderUtil.getWSRegistry(userId,ProductConstant.GREG_SERVER_NAME);
        governance = registryProviderUtil.getGovernanceRegistry(wsRegistry, userId);

        uploadCApplication();

    }

    @Test(groups = {"wso2.greg"}, description = "Human Readable Media type search")
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_user,
                                             ExecutionEnvironment.integration_tenant})
    public void predefinedMediaTypeTest() throws Exception {
        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator +
                              "GREG" + File.separator + "wsdl" + File.separator + "AmazonWebServices.wsdl";

        DataHandler dh = new DataHandler(new URL("file:///" + resourcePath));
        resourceAdminServiceClient.addResource("/_system/governance/trunk/wsdl/AmazonWebServices.wsdl",
                                               "application/wsdl+xml", "desc", dh);
        assertTrue(resourceAdminServiceClient.getHumanReadableMediaTypes().contains("wsdl"));
        assertTrue(resourceAdminServiceClient.getMimeTypeFromHuman("wsdl").contains("application/wsdl+xml"));
    }

    @Test(groups = {"wso2.greg"}, description = "Human Readable Mediatype search")
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_user,
                                             ExecutionEnvironment.integration_tenant})
    public void notPredefinedMediaTypeTest() throws Exception {
        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator +
                              "GREG" + File.separator + "mediatypes" + File.separator + "test.map";

        DataHandler dh = new DataHandler(new URL("file:///" + resourcePath));
        resourceAdminServiceClient.addResource("/_system/governance/trunk/test/test.map", "test/map", "desc", dh);
        assertTrue(resourceAdminServiceClient.getMimeTypeFromHuman("map").contains("test/map"));
        assertTrue(resourceAdminServiceClient.getHumanReadableMediaTypes().contains("map"));
    }


    @Test(groups = {"wso2.greg"}, description = "Human Readable Mediatype search")
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_user,
                                             ExecutionEnvironment.integration_tenant})
    public void humanReadableMediaTypeForCApp() throws Exception {

        assertTrue((CAppTestUtils.isCAppDeployed(environment.getGreg().getSessionCookie(), "MyApp", applicationAdminClient)));

        startTime = System.currentTimeMillis();

        assertTrue(searchMediaTypeForCApp("direct.properties", "text/properties"));
        assertTrue(searchMediaTypeForCApp("ESBAddAddressEndpointTest.java", "text/x-java"));
        assertTrue(searchMediaTypeForCApp("fix-synapse.cfg", "text/config"));
        assertTrue(searchMediaTypeForCApp("mywaves.txt", "plain/text"));
        assertTrue(searchMediaTypeForCApp("release-notes.html", "text/html"));
        assertTrue(searchMediaTypeForCApp("stockquoteTransform.js", "application/x-javascript"));
        assertTrue(searchMediaTypeForCApp("stockquoteTransform.rb", "text/ruby"));
        assertTrue(searchMediaTypeForCApp("synapse_all.xml", "application/xml"));
        assertTrue(searchMediaTypeForCApp("tranform_back_rule.drl", "xml/drool"));
        assertTrue(searchMediaTypeForCApp("transform.xslt", "application/xml"));
        assertTrue(searchMediaTypeForCApp("UTPolicy.xml", "application/xml"));
        assertTrue(searchMediaTypeForCApp("validate.xsd", "application/x-xsd+xml"));
        assertTrue(searchMediaTypeForCApp("xquery_req.xq", "xml/xquery"));

        deleteCarFile();

    }

    @Test(groups = {"wso2.greg"}, description = "Human Readable Mediatype search")
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_user,
                                             ExecutionEnvironment.integration_tenant})
    public void humanReadableMediatypeTest() throws Exception {
        addResources("gar", "opps.gar", "application/vnd.wso2.governance-archive");
        assertTrue(verifyMediatype("my-gar", "application/vnd.wso2.governance-archive"));
        addResources("schema", "library.xsd", "application/x-xsd+xml");
        assertTrue(verifyMediatype("eva-xsd", "application/x-xsd+xml"));
        addResources("javascript", "app.js", "application/x-javascript");
        assertTrue(verifyMediatype("eva-js", "application/x-javascript"));
        addResources("zip", "app.zip", "application/zip");
        assertTrue(verifyMediatype("eva-zip", "application/zip"));
        addResources("wsdl", "AmazonWebServices.wsdl", "application/wsdl+xml");
        assertTrue(verifyMediatype("wsdl", "application/wsdl+xml"));
        addResources("pdf", "test.pdf", "application/pdf");
        assertTrue(verifyMediatype("eva-pdf", "application/pdf"));
        addResources("xml", "student.xml", "application/xml");
        assertTrue(verifyMediatype("eva-xml eva-xsl eva-xslt eva-jrxml", "application/xml"));
        addResources("html", "test.html", "text/html");
        assertTrue(verifyMediatype("html", "text/html"));
        addResources("properties", "test.properties", "config/properties");
        assertTrue(verifyMediatype("my-properties", "config/properties"));
        addResources("csv", "test.csv", "text/comma-separated-values");
        assertTrue(verifyMediatype("eva-csv", "text/comma-separated-values"));
        addResources("config", "test.cfg", "config/cfg");
        assertTrue(verifyMediatype("my-cfg", "config/cfg"));
        addResources("drool", "test.drl", "config/drool");
        assertTrue(verifyMediatype("myconfig/drool-drl", "config/drool"));
        addResources("odp", "test.odp", "application/vnd.oasis.opendocument.presentation");
        assertTrue(verifyMediatype("eva-odp", "application/vnd.oasis.opendocument.presentation"));

        deleteResources();
    }

    public void deleteResources() throws ResourceAdminServiceExceptionException, RemoteException, RegistryException {
        delete("/_system/governance/trunk/test");
        delete("/_system/governance/trunk/schemas/org/epo");
        delete("/_system/governance/trunk/endpoints/org/epo/ops");
        delete("/_system/governance/trunk/wsdls/org/epo/ops");
        delete("/_system/governance/trunk/services/org/epo/ops");
        delete("/_system/governance/trunk/schemas/com/example/www/library/library.xsd");
        delete("/_system/governance/trunk/wsdls/com/amazon/soap/AmazonWebServices.wsdl");
        delete("/_system/governance/trunk/services/com/amazon/soap/AmazonSearchService");

    }

    public void deleteCarFile() throws ResourceAdminServiceExceptionException, RemoteException,
            ApplicationAdminExceptionException, RegistryException {
        applicationAdminClient.deleteApplication("MyApp");
        delete("/_system/config/evanthika");

    }

    public boolean verifyMediatype(String mimeType, String mediatype) throws Exception {
        boolean typeVerified = false;
        boolean mediaTypeFound = resourceAdminServiceClient.getMimeTypeFromHuman(mimeType).contains(mediatype);
        boolean mimeTypeFound = resourceAdminServiceClient.getHumanReadableMediaTypes().contains(mimeType);
        if (mediaTypeFound && mimeTypeFound) {
            typeVerified = true;
        }
        return typeVerified;
    }

    public void addResources(String fileFolder, String filename, String mediatype)
            throws RemoteException,
                   ResourceAdminServiceExceptionException, MalformedURLException {
        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator +
                              "GREG" + File.separator + fileFolder + File.separator + filename;

        DataHandler dh = new DataHandler(new URL("file:///" + resourcePath));
        resourceAdminServiceClient.addResource("/_system/governance/trunk/test/" + filename, mediatype, "desc", dh);

    }

    public boolean searchMediaTypeForCApp(String filename, String mediatype) throws Exception {


        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setResourceName(filename);

        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        paramList = null;
        AdvancedSearchResultsBean result;
        boolean resourceExists = false;
        do {
            result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);

            Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
            Assert.assertTrue((result.getResourceDataList().length > 0));

            for (ResourceData resource : result.getResourceDataList()) {

                if (resource.getResourcePath().equals("/_system/config/evanthika/uploaded_resources/" + filename)) {
                    if ((resourceAdminServiceClient.getMetadata("/_system/config/evanthika/uploaded_resources/" +
                                                                filename).getMediaType()).equals(mediatype)) {
                        resourceExists = true;
                        break;
                    }

                }
            }
        }
        while ((result.getResourceDataList() == null) && ((System.currentTimeMillis() - startTime) <= 60 * 1000));
        return resourceExists;

    }

    public void uploadCApplication()
            throws MalformedURLException, RemoteException, InterruptedException,
                   ApplicationAdminExceptionException {
        String filePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator +
                          "GREG" + File.separator + "car" + File.separator + "MyApp-1.0.0.car";

        cAppUploader.uploadCarbonAppArtifact("MyApp-1.0.0.car",
                                             new DataHandler(new URL("file:///" + filePath)));

    }


    @AfterClass(alwaysRun = true)
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_user,
                                             ExecutionEnvironment.integration_tenant})
    public void clean()
            throws ResourceAdminServiceExceptionException, RemoteException, RegistryException {

        delete("/_system/governance/trunk/test");
        delete("/_system/governance/trunk/endpoints/com");
        delete("/_system/governance/trunk/endpoints/org");
        delete("/_system/governance/trunk/schemas/org/w3/www/xml/_1998/namespace/xml.xsd");
        delete("/_system/governance/trunk/services/com/amazon/soap/AmazonSearchService");
        delete("/_system/governance/trunk/wsdls/com/amazon/soap/AmazonWebServices.wsdl");

        resourceAdminServiceClient = null;
        cAppUploader = null;
        searchAdminServiceClient = null;
        applicationAdminClient = null;
        environment = null;
        governance = null;
        wsRegistry=null;
    }

    public void delete(String destPath) throws ResourceAdminServiceExceptionException, RemoteException, RegistryException {
        if (wsRegistry.resourceExists(destPath)) {
            resourceAdminServiceClient.deleteResource(destPath);
        }
    }
}
