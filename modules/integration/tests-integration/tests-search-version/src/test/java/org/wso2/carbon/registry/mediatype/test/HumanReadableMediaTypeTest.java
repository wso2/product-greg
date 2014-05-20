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
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
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

import static org.testng.Assert.assertTrue;


public class HumanReadableMediaTypeTest extends GREGIntegrationBaseTest {

    private ResourceAdminServiceClient resourceAdminServiceClient;
    private CarbonAppUploaderClient cAppUploader;
    private SearchAdminServiceClient searchAdminServiceClient;
    private ApplicationAdminClient applicationAdminClient;
    private Registry governance;
    private long startTime;
    private WSRegistryServiceClient wsRegistry;
    private String sessionCookie;
    private String backEndUrl;
    private String userName;
    private String userNameWithoutDomain;

    @BeforeClass(alwaysRun = true)
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE,
                                             ExecutionEnvironment.STANDALONE})
    public void init() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        backEndUrl = getBackendURL();
        sessionCookie = getSessionCookie();
        userName = automationContext.getContextTenant().getContextUser().getUserName();

        if (userName.contains("@"))
            userNameWithoutDomain = userName.substring(0, userName.indexOf('@'));
        else
            userNameWithoutDomain = userName;
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(backEndUrl,
                                               sessionCookie);
        cAppUploader =
                new CarbonAppUploaderClient(backEndUrl,
                                            sessionCookie);
        searchAdminServiceClient =
                new SearchAdminServiceClient(backEndUrl,
                                             sessionCookie);
        applicationAdminClient =
                new ApplicationAdminClient(backEndUrl,
                                           sessionCookie);
        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        wsRegistry = registryProviderUtil.getWSRegistry(automationContext);
        governance = registryProviderUtil.getGovernanceRegistry(wsRegistry, automationContext);

        uploadCApplication();

    }

    @Test(groups = {"wso2.greg"}, description = "Human Readable Media type search")
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE,
                                             ExecutionEnvironment.STANDALONE})
    public void predefinedMediaTypeTest() throws Exception {
        String resourcePath = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator +
                              "GREG" + File.separator + "wsdl" + File.separator + "AmazonWebServices.wsdl";

        DataHandler dh = new DataHandler(new URL("file:///" + resourcePath));
        resourceAdminServiceClient.addResource("/_system/governance/trunk/wsdl/AmazonWebServices.wsdl",
                                               "application/wsdl+xml", "desc", dh);
        assertTrue(resourceAdminServiceClient.getHumanReadableMediaTypes().contains("wsdl"));
        assertTrue(resourceAdminServiceClient.getMimeTypeFromHuman("wsdl").contains("application/wsdl+xml"));
    }

    @Test(groups = {"wso2.greg"}, description = "Human Readable Mediatype search")
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE,
                                             ExecutionEnvironment.STANDALONE})
    public void notPredefinedMediaTypeTest() throws Exception {
        String resourcePath = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator +
                              "GREG" + File.separator + "mediatypes" + File.separator + "test.map";

        DataHandler dh = new DataHandler(new URL("file:///" + resourcePath));
        resourceAdminServiceClient.addResource("/_system/governance/trunk/test/test.map", "test/map", "desc", dh);
        assertTrue(resourceAdminServiceClient.getMimeTypeFromHuman("map").contains("test/map"));
        assertTrue(resourceAdminServiceClient.getHumanReadableMediaTypes().contains("map"));
    }


    @Test(groups = {"wso2.greg"}, description = "Human Readable Mediatype search")
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE,
                                             ExecutionEnvironment.STANDALONE})
    public void humanReadableMediaTypeForCApp() throws Exception {

        assertTrue((CAppTestUtils.isCAppDeployed(sessionCookie, /*"MyApp"*/ "MyApp_1.0.0", applicationAdminClient)));

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
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE,
                                             ExecutionEnvironment.STANDALONE})
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
        addResources("services", "defaultService.xml", "application/xml");

        deleteResources();
    }

    public void deleteResources() throws ResourceAdminServiceExceptionException, RemoteException, RegistryException {
        delete("/_system/governance/trunk/test");
        delete("/_system/governance/trunk/schemas/org/epo");
        //delete("/_system/governance/trunk/endpoints/org/epo/ops");
        delete("/_system/governance/trunk/wsdls/org/epo/ops");
        delete("/_system/governance/trunk/services/org/epo/ops");
        delete("/_system/governance/trunk/schemas/com/example/www/library/library.xsd");
        delete("/_system/governance/trunk/wsdls/com/amazon/soap/AmazonWebServices.wsdl");
        delete("/_system/governance/trunk/services/com/amazon/soap/AmazonSearchService");

    }

    public void deleteCarFile() throws ResourceAdminServiceExceptionException, RemoteException,
            ApplicationAdminExceptionException, RegistryException {
        applicationAdminClient.deleteApplication("MyApp_1.0.0");
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
        String resourcePath = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator +
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
        String filePath = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator +
                          "GREG" + File.separator + "car" + File.separator + "MyApp-1.0.0.car";

        cAppUploader.uploadCarbonAppArtifact("MyApp-1.0.0.car",
                                             new DataHandler(new URL("file:///" + filePath)));

    }


    @AfterClass(alwaysRun = true)
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE,
                                             ExecutionEnvironment.STANDALONE})
    public void clean()
            throws ResourceAdminServiceExceptionException, RemoteException, RegistryException {

        delete("/_system/governance/trunk/test");
        //delete("/_system/governance/trunk/endpoints/com");
        delete("/_system/governance/trunk/endpoints/org");
        delete("/_system/governance/trunk/schemas/org/w3/www/xml/_1998/namespace/xml.xsd");
        delete("/_system/governance/trunk/services/com/amazon/soap/AmazonSearchService");
        delete("/_system/governance/trunk/wsdls/com/amazon/soap/AmazonWebServices.wsdl");

        resourceAdminServiceClient = null;
        cAppUploader = null;
        searchAdminServiceClient = null;
        applicationAdminClient = null;
        governance = null;
        wsRegistry=null;
    }

    public void delete(String destPath) throws ResourceAdminServiceExceptionException, RemoteException, RegistryException {
        if (wsRegistry.resourceExists(destPath)) {
            resourceAdminServiceClient.deleteResource(destPath);
        }
    }
}
