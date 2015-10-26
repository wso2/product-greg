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

package org.wso2.carbon.registry.utfsupport.test;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.automation.test.utils.common.FileManager;
import org.wso2.carbon.governance.api.policies.PolicyManager;
import org.wso2.carbon.governance.api.policies.dataobjects.Policy;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.info.stub.RegistryExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.utfsupport.test.bean.SearchParameterBean;
import org.wso2.carbon.registry.utfsupport.test.util.CommonUtils;
import org.wso2.carbon.registry.search.stub.SearchAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.search.stub.beans.xsd.AdvancedSearchResultsBean;
import org.wso2.carbon.registry.search.stub.beans.xsd.ArrayOfString;
import org.wso2.carbon.registry.search.stub.beans.xsd.CustomSearchParameterBean;
import org.wso2.carbon.registry.search.stub.common.xsd.ResourceData;
import org.wso2.carbon.registry.utfsupport.test.util.UTFSupport;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.clients.*;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import javax.activation.DataHandler;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

public class UTFSupportForMetadataTestCase extends GREGIntegrationBaseTest {

    private Registry governance;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private String utfString;
    private String policyPath;
    private String wsdlPath;
    private String associatePath;
    private RelationAdminServiceClient relationAdminServiceClient;
    private String pathPrefix = "/_system/governance";
    private RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
    private InfoServiceAdminClient infoServiceAdminClient;
    private UserManagementClient userManagementClient;
    private SearchAdminServiceClient searchAdminServiceClient;
    private WSRegistryServiceClient wsRegistryServiceClient;
    private String sessionCookie;
    private String backEndUrl;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.init(TestUserMode.SUPER_TENANT_ADMIN);

        backEndUrl = getBackendURL();
        sessionCookie = getSessionCookie();


        searchAdminServiceClient = new SearchAdminServiceClient(backEndUrl,
                                                                sessionCookie);

        userManagementClient =
                new UserManagementClient(backEndUrl,
                                         sessionCookie);

        infoServiceAdminClient =
                new InfoServiceAdminClient(backEndUrl,
                                           sessionCookie);
        wsRegistryServiceClient =
                registryProviderUtil.getWSRegistry(automationContext);

        relationAdminServiceClient =
                new RelationAdminServiceClient(backEndUrl,
                                               sessionCookie);
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(backEndUrl,
                                               sessionCookie);

        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        governance = registryProviderUtil.getGovernanceRegistry(wsRegistryServiceClient, automationContext);

        //adding the resource which was earlier added from 'RegistryConfiguratorTestCase'
        String resourcePath = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" +
                File.separator + "GREG" + File.separator + "utf8" + File.separator + "test.txt";

        DataHandler dh = new DataHandler(new URL("file:///" + resourcePath));
        resourceAdminServiceClient.addResource("/_system/config/test_utf8_Resource", "text/plain", "testDesc", dh);
        wsRegistryServiceClient.resourceExists("/_system/config/test_utf8_Resource");

    }

    @Test(groups = {"wso2.greg"}, description = "read UTF characters from file")
    public void testreadFile() throws IOException {
        utfString = UTFSupport.readFile();

    }

    @Test(groups = {"wso2.greg"}, description = "add resource", dependsOnMethods = "testreadFile")
    public void testAddResource() throws ResourceAdminServiceExceptionException,
            IOException, RegistryException, InterruptedException {
        String POLICY_URL = "https://svn.wso2.org/repos/wso2/carbon/platform/trunk/products/greg/modules/integration/" +
                            "registry/tests-new/src/test/resources/artifacts/GREG/policy/EncrOnlyAnonymous.xml";
        policyPath = addPolicy("policy" + utfString, "desc", POLICY_URL);
        wsdlPath = addWSDL();
        String WSDL_URL = "https://svn.wso2.org/repos/wso2/carbon/platform/trunk/products/greg/modules/integration/registry" +
                          "/tests-new/src/test/resources/artifacts/GREG/wsdl/Axis2ImportedWsdl.wsdl";
        associatePath = addWSDL("wsdl_" + utfString, "desc", WSDL_URL);
        Thread.sleep(30000);

    }

    @Test(groups = {"wso2.greg"}, description = "search by resource name",
          dependsOnMethods = {"testAddResource"})
    public void testByResourceName() throws SearchAdminServiceRegistryExceptionException,
                                            RemoteException, RegistryException,
                                            InterruptedException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setResourceName("policy" + utfString + ".xml");

        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = CommonUtils.getSearchResult(searchAdminServiceClient, searchQuery);
//        AdvancedSearchResultsBean result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid property name");

        for (ResourceData resource : result.getResourceDataList()) {
            boolean resourceFound = false;
            if (resource.getName().equals("policy" + utfString + ".xml")) {
                resourceFound = true;
            }
            Assert.assertTrue(resourceFound);
        }


    }

    @Test(groups = {"wso2.greg"}, description = "search by resource tag",
          dependsOnMethods = {"testByResourceName"})
    public void testSearchByTag() throws Exception {
        UTFSupport.addTag(infoServiceAdminClient, utfString, pathPrefix + policyPath, automationContext);
        Thread.sleep(20000);
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setTags(utfString);

        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid property name");

        for (ResourceData resource : result.getResourceDataList()) {
            boolean resourceFound = false;
            if (resource.getName().equals("policy" + utfString + ".xml")) {
                resourceFound = true;
            }
            Assert.assertTrue(resourceFound);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "search by resource comment",
          dependsOnMethods = {"testSearchByTag"})
    public void testSearchByComment()
            throws Exception {
        UTFSupport.addComment(infoServiceAdminClient, utfString, pathPrefix + policyPath, automationContext);
        Thread.sleep(20000);
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setCommentWords(utfString);

        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid property name");

        for (ResourceData resource : result.getResourceDataList()) {
            boolean resourceFound = false;
            if (resource.getName().equals("policy" + utfString + ".xml")) {
                resourceFound = true;
            }
            Assert.assertTrue(resourceFound);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "search by property name",
          dependsOnMethods = {"testSearchByComment"})
    public void testByPropertyName()
            throws SearchAdminServiceRegistryExceptionException, RemoteException,
                   RegistryException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setPropertyName(utfString);

        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid property name");

        for (ResourceData resource : result.getResourceDataList()) {
            boolean resourceFound = false;
            if (resource.getName().equals("AmazonWebServices.wsdl")) {
                resourceFound = true;
            }
            Assert.assertTrue(resourceFound);
        }

    }

    @Test(groups = {"wso2.greg"}, description = "search by created user",
          dependsOnMethods = {"testByPropertyName"})
    public void testBycreatedUser() throws Exception {

        String[] roles = {"testBycreatedUser"};
        String[] permissions = {"/permission/admin/configure/",
                                "/permission/admin/login",
                                "/permission/admin/manage/",
                                "/permission/admin/monitor",
                                "/permission/protected"};

        if (!userManagementClient.userNameExists(roles[0], utfString)) {
            userManagementClient.addRole(roles[0], null, permissions);
            resourceAdminServiceClient.addResourcePermission("/", roles[0], "3", "1");
            resourceAdminServiceClient.addResourcePermission("/", roles[0], "2", "1");
            resourceAdminServiceClient.addResourcePermission("/", roles[0], "4", "1");
            resourceAdminServiceClient.addResourcePermission("/", roles[0], "5", "1");
            userManagementClient.addUser(utfString, "abcdef2", roles, utfString);
        }

        boolean userAdded = userManagementClient.userNameExists(roles[0], utfString);

        Assert.assertTrue(userAdded);

        AuthenticatorClient authenticatorClient = new AuthenticatorClient(automationContext.getContextUrls().getBackEndUrl());

        String sessionCookieLocal = authenticatorClient.login(utfString, "abcdef2",
                                                              automationContext.getInstance().getHosts().get("default"));

        //new URL(backEndUrl).getHost();

        //create collection with new user
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(backEndUrl,
                                               sessionCookieLocal);

        resourceAdminServiceClient.addCollection("/", "test_collection", "other", "desc");
        Thread.sleep(20000);
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setAuthor(utfString);

        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        searchAdminServiceClient = new SearchAdminServiceClient(backEndUrl, sessionCookieLocal);
        AdvancedSearchResultsBean result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid property name");

        for (ResourceData resource : result.getResourceDataList()) {
            boolean resourceFound = false;
            if (resource.getName().equals("test_collection")) {
                resourceFound = true;
            }
            Assert.assertTrue(resourceFound);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "search by updater",
          dependsOnMethods = {"testBycreatedUser"})
    public void testByUpdatedUser() throws Exception {

        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setUpdater(utfString);

        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid property name");

        for (ResourceData resource : result.getResourceDataList()) {
            boolean resourceFound = false;
            if (resource.getName().equals("test_collection")) {
                resourceFound = true;
            }
            Assert.assertTrue(resourceFound);
        }

    }


    @Test(groups = {"wso2.greg"}, description = "search by property value",
          dependsOnMethods = {"testByUpdatedUser"})
    public void testByPropertyValue()
            throws SearchAdminServiceRegistryExceptionException, RemoteException,
                   RegistryException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setPropertyName(utfString);
        paramBean.setRightOperator("eq");
        paramBean.setRightPropertyValue(utfString);

        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid property name");

        for (ResourceData resource : result.getResourceDataList()) {
            boolean resourceFound = false;
            if (resource.getName().equals("AmazonWebServices.wsdl")) {
                resourceFound = true;
            }
            Assert.assertTrue(resourceFound);
        }

    }


    @Test(groups = {"wso2.greg"}, description = "search by media type",
          dependsOnMethods = {"testByPropertyValue"})
    public void testByMediaType()
            throws SearchAdminServiceRegistryExceptionException, RemoteException,
            MalformedURLException, ResourceAdminServiceExceptionException, InterruptedException {

        String resourcePath = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator +
                              "GREG" + File.separator + "testresource.txt";

        DataHandler dh = new DataHandler(new URL("file:///" + resourcePath));
        resourceAdminServiceClient.addResource("/_system/governance/trunk/test1/" + "testresource.txt", utfString, "desc", dh);
        Thread.sleep(20000);
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setMediaType(utfString);


        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result =
                searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
        for (ResourceData resource : result.getResourceDataList()) {
            boolean propertyFound = false;
            if (resource.getName().equals("testresource.txt")) {
                propertyFound = true;
            }
            Assert.assertTrue(propertyFound);
        }


    }

    @Test(groups = {"wso2.greg"}, description = "Add filter",
          dependsOnMethods = {"testByMediaType"})
    public void testAddFilter() throws SearchAdminServiceRegistryExceptionException,
                                       RemoteException, RegistryException {

        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setResourceName("AmazonWebServices.wsdl");


        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        searchAdminServiceClient.saveAdvancedSearchFilter(searchQuery, utfString);

        String[] filters = searchAdminServiceClient.getSavedFilters();

        boolean filterFound = false;
        for (String filter : filters) {

            if (filter.equals(utfString)) {
                filterFound = true;
                break;
            }
        }
        Assert.assertTrue(filterFound);

    }

    @Test(groups = {"wso2.greg"}, description = "search via filter",
          dependsOnMethods = {"testAddFilter"})
    public void testSearchViaFilter()
            throws SearchAdminServiceRegistryExceptionException, RemoteException,
                   RegistryException {


        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setResourceName("AmazonWebServices.wsdl");


        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        searchAdminServiceClient.saveAdvancedSearchFilter(searchQuery, utfString);

        AdvancedSearchResultsBean result =
                searchAdminServiceClient.getAdvancedSearchResults(searchAdminServiceClient.getAdvancedSearchFilter(utfString));
        for (ResourceData resource : result.getResourceDataList()) {
            boolean propertyFound = false;
            if (resource.getName().equals("AmazonWebServices.wsdl")) {
                propertyFound = true;
            }
            Assert.assertTrue(propertyFound);
        }


    }

    @Test(groups = {"wso2.greg"}, description = "delete filter",
          dependsOnMethods = {"testSearchViaFilter"})
    public void testDeleteFilter()
            throws SearchAdminServiceRegistryExceptionException, RemoteException {

        searchAdminServiceClient.deleteFilter(utfString);
        boolean filterdeleted = true;

        if (!(searchAdminServiceClient.getSavedFilters() == null)) {
            String[] filter = searchAdminServiceClient.getSavedFilters();

            for (String filt : filter) {

                if (filt.equals(utfString)) {
                    filterdeleted = false;
                    break;
                }
            }

        }
        Assert.assertTrue(filterdeleted);
    }


    @Test(groups = {"wso2.greg"}, description = "search by property name",
          dependsOnMethods = {"testDeleteFilter"})
    public void testSearchByPropertyName()
            throws ResourceAdminServiceExceptionException, RegistryException,
                   RegistryExceptionException, RemoteException,
                   SearchAdminServiceRegistryExceptionException {

        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setPropertyName(utfString);

        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid property name");

        for (ResourceData resource : result.getResourceDataList()) {
            boolean resourceFound = false;
            if (resource.getName().equals("AmazonWebServices.wsdl")) {
                resourceFound = true;
            }
            Assert.assertTrue(resourceFound);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "search by keywords", dependsOnMethods = {"testSearchByPropertyName"})
    public void testSearchByKeywords()
            throws MalformedURLException, ResourceAdminServiceExceptionException, RemoteException,
                   SearchAdminServiceRegistryExceptionException, InterruptedException {

        //resource added in @BeforeSuite to minimize indexing delay
        // update:since @BeforeSuite was not found, add the resource in @BeforeClass method.
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setContent(utfString);

        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result;
        long startTime = System.currentTimeMillis();
        do {
          result = CommonUtils.getSearchResult(searchAdminServiceClient, searchQuery);
        }
        while ((result.getResourceDataList() == null) && ((System.currentTimeMillis() - startTime) <= 60 * 10 * 1000));
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid content");

//        boolean resourceFound = false;
//        for (ResourceData resource : result.getResourceDataList()) {
//
//            if (resource.getName().equals("AmazonSe
// archService") || resource.getName().equals("test_utf8_Resource")) {
//                resourceFound = true;
//            }
//        }
//        Assert.assertTrue(resourceFound);

    }


    @Test(groups = {"wso2.greg"}, description = "search by association destination",
          dependsOnMethods = {"testSearchByKeywords"})
    public void testByAssociationDest() throws Exception {

        relationAdminServiceClient.addAssociation(pathPrefix + policyPath, utfString, pathPrefix + associatePath, "add");
        Thread.sleep(20000);
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setAssociationDest(pathPrefix + associatePath);
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = CommonUtils.getSearchResult(searchAdminServiceClient, searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid property name");
        boolean resourceFound = false;
        for (ResourceData resource : result.getResourceDataList()) {
            if (resource.getName().equals("policy" + utfString + ".xml")) {
                resourceFound = true;
                break;
            }
        }
        Assert.assertTrue(resourceFound);

    }

    @Test(groups = {"wso2.greg"}, description = "search by association type",
          dependsOnMethods = {"testByAssociationDest"})
    public void testByAssociationType()
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setAssociationType(utfString);
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid property name");

        for (ResourceData resource : result.getResourceDataList()) {
            boolean resourceFound = false;
            if (resource.getName().equals("policy" + utfString + ".xml")) {
                resourceFound = true;
            }
            Assert.assertTrue(resourceFound);
        }

    }

    public String addWSDL()
            throws IOException, ResourceAdminServiceExceptionException, RegistryException, InterruptedException {

        WsdlManager wsdlManager = new WsdlManager(governance);
        Wsdl wsdl;
        String wsdlFilePath = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator +
                              "GREG" + File.separator + "wsdl" + File.separator + "AmazonWebServices.wsdl";
        wsdl = wsdlManager.newWsdl(FileManager.readFile(wsdlFilePath).getBytes(), "AmazonWebServices.wsdl");
        wsdlManager.addWsdl(wsdl);
        wsdl = wsdlManager.getWsdl(wsdl.getId());
        Resource resource = governance.get(wsdl.getPath());
        resource.addProperty(utfString, utfString);
        governance.put(wsdl.getPath(), resource);
        return wsdl.getPath();

    }

    public String addPolicy(String policyName, String desc, String url)
            throws MalformedURLException,
            ResourceAdminServiceExceptionException, RemoteException, RegistryException, InterruptedException {

        resourceAdminServiceClient.addPolicy(policyName, desc, url);
        PolicyManager policyManager = new PolicyManager(governance);
        Policy[] policies = policyManager.getAllPolicies();
        String path = null;
        for (Policy policy : policies) {
            String name = policy.getQName().getLocalPart();
            if (name.equals(policyName + ".xml")) {
                path = policy.getPath();
            }
        }


        return path;
    }

    public String addWSDL(String wsdlName, String desc, String url) throws MalformedURLException,
            ResourceAdminServiceExceptionException,
            RemoteException,
            RegistryException, InterruptedException {

        resourceAdminServiceClient.addWSDL(wsdlName, desc, url);
        WsdlManager wsdlManager = new WsdlManager(governance);
        Wsdl[] wsdls = wsdlManager.getAllWsdls();
        String path = null;
        for (Wsdl wsdl : wsdls) {
            String name = wsdl.getQName().getLocalPart();
            if (name.equals(wsdlName + ".wsdl")) {
                path = wsdl.getPath();

            }
        }
        return path;
    }

    @AfterClass(alwaysRun = true)
    public void clean() throws Exception {
        delete(pathPrefix + policyPath);
        delete(pathPrefix + wsdlPath);
        delete(pathPrefix + associatePath);
        delete("/_system/config/test_utf8_Resource");
        delete("/_system/governance/trunk/services/com/amazon/soap/1.0.0/AmazonSearchService");
        delete("/test_collection");
        delete("/_system/governance/trunk/test1/testresource.txt");
        delete("/_system/governance/trunk/endpoints/com/amazon/soap/onca/ep-soap2");
        delete("/_system/governance/trunk/endpoints/com");
        /*delete("/_system/config/testResource");*/

        userManagementClient = new UserManagementClient(backEndUrl,
                                                        sessionCookie);

        if (userManagementClient.userNameExists("testBycreatedUser", utfString)) {
            userManagementClient.deleteUser(utfString);
        }

        utfString = null;
        resourceAdminServiceClient = null;
        userManagementClient = null;
        governance = null;
        relationAdminServiceClient = null;
        registryProviderUtil = null;
        infoServiceAdminClient = null;
        searchAdminServiceClient = null;
        policyPath = null;
        wsdlPath = null;
        associatePath = null;
        pathPrefix = null;
        wsRegistryServiceClient = null;
    }

    public void delete(String destPath)
            throws ResourceAdminServiceExceptionException, RemoteException, RegistryException {
        if (wsRegistryServiceClient.resourceExists(destPath)) {
            resourceAdminServiceClient.deleteResource(destPath);
        }
    }

}
