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
import org.wso2.carbon.automation.api.clients.registry.SearchAdminServiceClient;
import org.wso2.carbon.governance.api.policies.PolicyManager;
import org.wso2.carbon.governance.api.policies.dataobjects.Policy;
import org.wso2.carbon.governance.api.schema.SchemaManager;
import org.wso2.carbon.governance.api.schema.dataobjects.Schema;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.registry.core.Comment;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Tag;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.governance.api.test.TestUtils;
import org.wso2.carbon.registry.search.metadata.test.bean.SearchParameterBean;
import org.wso2.carbon.registry.search.metadata.test.utils.CommonUtils;
import org.wso2.carbon.registry.search.metadata.test.utils.GregTestUtils;
import org.wso2.carbon.registry.search.stub.SearchAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.search.stub.beans.xsd.AdvancedSearchResultsBean;
import org.wso2.carbon.registry.search.stub.beans.xsd.ArrayOfString;
import org.wso2.carbon.registry.search.stub.beans.xsd.CustomSearchParameterBean;
import org.wso2.carbon.registry.search.stub.common.xsd.ResourceData;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/*
test matching resource with all fields
 */
public class RegistrySearchByAllTestCase {
    private String userName;
    private String destinationPath;

    private SearchAdminServiceClient searchAdminService;
    private Registry governance;
    
    private final String wsdlName = "echo1.wsdl";
    private final String policyName = "UTPolicy3.xml";
    private final String schemaName = "Person3.xsd";

    @BeforeClass
    public void init() throws Exception {

        ClientConnectionUtil.waitForPort(Integer.parseInt(FrameworkSettings.HTTP_PORT));
        String sessionCookie = new LoginLogoutUtil().login();
        String SERVER_URL = GregTestUtils.getServerUrl();
        userName = FrameworkSettings.USER_NAME;
        searchAdminService = new SearchAdminServiceClient(SERVER_URL, sessionCookie);
        governance = GregTestUtils.getGovernanceRegistry(GregTestUtils.getRegistry());
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
        TestUtils.cleanupResources(governance);
        addResources();
    }

    @Test(description = "Metadata search by All fields for wsdl")
    public void searchWsdlByAllCriteria()
            throws SearchAdminServiceRegistryExceptionException, RemoteException, InterruptedException {
        searchWsdl();
    }

    @Test(description = "Metadata search by All fields for wsdl when having two wsdl name starting same prefix")
    public void searchWsdlByAllCriteriaHavingTwoResources()
            throws SearchAdminServiceRegistryExceptionException, IOException, RegistryException,
            InterruptedException {
        WsdlManager wsdlManager = new WsdlManager(governance);
        Wsdl wsdl;
        String wsdlFilePath = GregTestUtils.getResourcePath()
                              + File.separator + "wsdl" + File.separator;
        wsdl = wsdlManager.newWsdl(GregTestUtils.readFile(wsdlFilePath + "echo.wsdl").getBytes(), "echo234.wsdl");
        wsdlManager.addWsdl(wsdl);
        searchWsdl();
    }

    @Test(description = "Metadata search by All fields for schema")
    public void searchSchemaByAllCriteria()
            throws SearchAdminServiceRegistryExceptionException, RemoteException, InterruptedException {
        searchSchemaFile();
    }

    @Test(description = "Metadata search by All fields for schema when having two schema name starting same prefix")
    public void searchSchemaByAllCriteriaHavingTwoResources()
            throws SearchAdminServiceRegistryExceptionException, IOException, RegistryException,
            InterruptedException {
        SchemaManager schemaManager = new SchemaManager(governance);
        String schemaFilePath = GregTestUtils.getResourcePath()
                                + File.separator + "schema" + File.separator;
        Schema schema = schemaManager.newSchema(GregTestUtils.readFile(schemaFilePath + "Person.xsd").getBytes(), "Person234.xsd");
        schemaManager.addSchema(schema);
        searchSchemaFile();
    }

    @Test(description = "Metadata search by All fields for policy")
    public void searchPolicyByAllCriteria()
            throws SearchAdminServiceRegistryExceptionException, RemoteException, InterruptedException {
        searchPolicyFile();
    }

    @Test(description = "Metadata search by All fields for policy when having two policy name starting same prefix")
    public void searchPolicyByAllCriteriaHavingTwoResources()
            throws SearchAdminServiceRegistryExceptionException, IOException, RegistryException,
            InterruptedException {
        PolicyManager policyManager = new PolicyManager(governance);
        String policyFilePath = GregTestUtils.getResourcePath()
                                + File.separator + "policy" + File.separator;
        Policy policy = policyManager.newPolicy(GregTestUtils.readFile(policyFilePath + "UTPolicy.xml").getBytes(), "UTPolicy234d.xml");
        policyManager.addPolicy(policy);
        searchPolicyFile();
    }

    @Test(description = "Search schema with all fields with wrong tag")
    public void searchSchemaNotExist()
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();

        Calendar fromCalender = Calendar.getInstance();
        fromCalender.add(Calendar.DAY_OF_MONTH, -2);
        paramBean.setCreatedAfter(formatDate(fromCalender.getTime()));

        Calendar toCalender = Calendar.getInstance();
        paramBean.setCreatedBefore(formatDate(toCalender.getTime()));

        paramBean.setResourceName("Person");
        paramBean.setContent("PersonType");
        paramBean.setCreatedBefore(formatDate(toCalender.getTime()));
        paramBean.setCreatedAfter(formatDate(fromCalender.getTime()));

        paramBean.setUpdatedAfter(formatDate(fromCalender.getTime()));
        paramBean.setUpdatedBefore(formatDate(toCalender.getTime()));

        paramBean.setAuthor(userName);
        paramBean.setUpdater(userName);
        paramBean.setTags("autoTag1234");
        paramBean.setCommentWords("TestAutomation");
        paramBean.setAssociationType("associationType1");
        paramBean.setAssociationDest(RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + destinationPath);
        paramBean.setMediaType("application/x-xsd+xml");
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = null;
        try {
            result = searchAdminService.getAdvancedSearchResults(searchQuery);
        } finally {
            if (result != null) {
                if (result.getResourceDataList() != null) {
                    Assert.assertNull(result.getResourceDataList()[0],"Results found");
                } else {
                    Assert.assertNull(result.getResourceDataList(), "Result Object found.");
                }
            }else{
                Assert.assertNull(result, "No results returned");
            }
        }
    }

    private void addResources() throws Exception {
        destinationPath = addService("sns1", "autoService1");
        addWSDL(destinationPath, "associationType1");
        addSchema(destinationPath, "associationType1");
        addPolicy(destinationPath, "associationType1");
    }

    private String addService(String nameSpace, String serviceName)
            throws Exception {
        ServiceManager serviceManager = new ServiceManager(governance);
        Service service;
        service = serviceManager.newService(new QName(nameSpace, serviceName));
        serviceManager.addService(service);
        for (String serviceId : serviceManager.getAllServiceIds()) {
            service = serviceManager.getService(serviceId);
            if (service.getPath().endsWith(serviceName)) {
                return service.getPath();
            }
        }
        throw new Exception("Getting Service path failed");
    }

    private void addWSDL(String destinationPath, String type)
            throws IOException, RegistryException {
        WsdlManager wsdlManager = new WsdlManager(governance);
        Wsdl wsdl;
        String wsdlFilePath = GregTestUtils.getResourcePath()
                              + File.separator + "wsdl" + File.separator;
        wsdl = wsdlManager.newWsdl(GregTestUtils.readFile(wsdlFilePath + "echo.wsdl").getBytes(), wsdlName);
        wsdlManager.addWsdl(wsdl);
        wsdl = wsdlManager.getWsdl(wsdl.getId());

        governance.addAssociation(wsdl.getPath(), destinationPath, type);
        Comment comment = new Comment();
        comment.setText("TestAutomation Comment");
        governance.addComment(wsdl.getPath(), comment);
        governance.applyTag(wsdl.getPath(), "autoTag");
    }

    private void addSchema(String destinationPath, String type)
            throws IOException, RegistryException {
        SchemaManager schemaManager = new SchemaManager(governance);
        String schemaFilePath = GregTestUtils.getResourcePath()
                                + File.separator + "schema" + File.separator;
        Schema schema = schemaManager.newSchema(GregTestUtils.readFile(schemaFilePath + "Person.xsd").getBytes(), schemaName);
        schemaManager.addSchema(schema);
        schema = schemaManager.getSchema(schema.getId());
        governance.addAssociation(schema.getPath(), destinationPath, type);
        Comment comment = new Comment();
        comment.setText("TestAutomation Comment");
        governance.addComment(schema.getPath(), comment);

        Tag[] allTags = governance.getTags(schema.getPath());
        StringBuilder tagString = new StringBuilder();

        if(allTags != null){
            for (Tag allTag : allTags) {
                tagString.append(allTag.getTagName());
                tagString.append(",");
            }
        }
        tagString.append("autoTag");
        governance.applyTag(schema.getPath(),tagString.toString());
    }

    private void addPolicy(String destinationPath, String type)
            throws RegistryException, IOException {
        PolicyManager policyManager = new PolicyManager(governance);
        String policyFilePath = GregTestUtils.getResourcePath()
                                + File.separator + "policy" + File.separator;
        Policy policy = policyManager.newPolicy(GregTestUtils.readFile(policyFilePath + "UTPolicy.xml").getBytes(), policyName);
        policyManager.addPolicy(policy);
        policy = policyManager.getPolicy(policy.getId());
        governance.addAssociation(policy.getPath(), destinationPath, type);
        Comment comment = new Comment();
        comment.setText("TestAutomation");
        governance.addComment(policy.getPath(), comment);

        Tag[] allTags = governance.getTags(policy.getPath());
        StringBuilder tagString = new StringBuilder();

        if(allTags != null){
            for (Tag allTag : allTags) {
                tagString.append(allTag.getTagName());
                tagString.append(",");
            }
        }
        tagString.append("autoTag");
        governance.applyTag(policy.getPath(),tagString.toString());
    }

    private String formatDate(Date date) {
        Format formatter = new SimpleDateFormat("MM/dd/yyyy");
        return formatter.format(date);
    }

    private void searchPolicyFile()
            throws SearchAdminServiceRegistryExceptionException, RemoteException, InterruptedException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();

        Calendar fromCalender = Calendar.getInstance();
        fromCalender.add(Calendar.DAY_OF_MONTH, -2);
        paramBean.setCreatedAfter(formatDate(fromCalender.getTime()));

        Calendar toCalender = Calendar.getInstance();
        paramBean.setCreatedBefore(formatDate(toCalender.getTime()));

        paramBean.setResourceName(policyName);
        paramBean.setCreatedBefore(formatDate(toCalender.getTime()));
        paramBean.setCreatedAfter(formatDate(fromCalender.getTime()));

        paramBean.setUpdatedAfter(formatDate(fromCalender.getTime()));
        paramBean.setUpdatedBefore(formatDate(toCalender.getTime()));

        paramBean.setAuthor(userName);
        paramBean.setTags("autoTag");
        paramBean.setCommentWords("TestAutomation");
        paramBean.setAssociationType("associationType1");
        paramBean.setAssociationDest(RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + destinationPath);
        paramBean.setMediaType("application/policy+xml");
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result =CommonUtils.getSearchResult(searchAdminService,searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length == 1), "No Record Found.");
        for (ResourceData resource : result.getResourceDataList()) {
            Assert.assertEquals(resource.getName(), "UTPolicy3.xml", "Schema not found");
        }
    }

    private void searchSchemaFile()
            throws SearchAdminServiceRegistryExceptionException, RemoteException, InterruptedException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();

        Calendar fromCalender = Calendar.getInstance();
        fromCalender.add(Calendar.DAY_OF_MONTH, -2);
        paramBean.setCreatedAfter(formatDate(fromCalender.getTime()));

        Calendar toCalender = Calendar.getInstance();
        paramBean.setCreatedBefore(formatDate(toCalender.getTime()));

        paramBean.setResourceName("Person%");
//        paramBean.setContent("PersonType");
        paramBean.setCreatedBefore(formatDate(toCalender.getTime()));
        paramBean.setCreatedAfter(formatDate(fromCalender.getTime()));

        paramBean.setUpdatedAfter(formatDate(fromCalender.getTime()));
        paramBean.setUpdatedBefore(formatDate(toCalender.getTime()));

        paramBean.setAuthor(userName);
//        paramBean.setUpdater(userName);
        paramBean.setTags("autoTag");
        paramBean.setCommentWords("TestAutomation");
        paramBean.setAssociationType("associationType1");
        paramBean.setAssociationDest(RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + destinationPath);
        paramBean.setMediaType("application/x-xsd+xml");
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = CommonUtils.getSearchResult(searchAdminService,searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length == 1), "No Record Found.");
        for (ResourceData resource : result.getResourceDataList()) {
            Assert.assertEquals(resource.getName(), schemaName, "Schema not found");
        }
    }

    private void searchWsdl() throws SearchAdminServiceRegistryExceptionException, RemoteException, InterruptedException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();

        Calendar fromCalender = Calendar.getInstance();
        fromCalender.add(Calendar.DAY_OF_MONTH, -2);
        paramBean.setCreatedAfter(formatDate(fromCalender.getTime()));

        Calendar toCalender = Calendar.getInstance();
        paramBean.setCreatedBefore(formatDate(toCalender.getTime()));

        paramBean.setResourceName("echo%");
//        paramBean.setContent("echoString");
        paramBean.setCreatedBefore(formatDate(toCalender.getTime()));
        paramBean.setCreatedAfter(formatDate(fromCalender.getTime()));

        paramBean.setUpdatedAfter(formatDate(fromCalender.getTime()));
        paramBean.setUpdatedBefore(formatDate(toCalender.getTime()));

        paramBean.setAuthor(userName);
//        paramBean.setUpdater(userName);
        paramBean.setTags("autoTag");
        paramBean.setCommentWords("TestAutomation");
        paramBean.setAssociationType("associationType1");
        paramBean.setAssociationDest(RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + destinationPath);
        paramBean.setMediaType("application/wsdl+xml");
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = CommonUtils.getSearchResult(searchAdminService,searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length == 1), "No Record Found.");
        for (ResourceData resource : result.getResourceDataList()) {
            Assert.assertEquals(resource.getName(), wsdlName, "wsdl not found");
        }
    }

    @AfterClass
    public void destroy() {
        try {
            CommonUtils.cleanUpResource(governance);
        } catch (RegistryException ignore) {

        }
    }
}
