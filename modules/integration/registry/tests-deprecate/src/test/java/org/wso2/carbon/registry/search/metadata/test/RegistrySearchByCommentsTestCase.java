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
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.governance.api.test.TestUtils;
import org.wso2.carbon.registry.search.metadata.test.bean.SearchParameterBean;
import org.wso2.carbon.registry.search.metadata.test.utils.CommonUtils;
import org.wso2.carbon.registry.search.metadata.test.utils.GregTestUtils;
import org.wso2.carbon.registry.search.metadata.test.utils.Parameters;
import org.wso2.carbon.registry.search.stub.SearchAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.search.stub.beans.xsd.AdvancedSearchResultsBean;
import org.wso2.carbon.registry.search.stub.beans.xsd.ArrayOfString;
import org.wso2.carbon.registry.search.stub.beans.xsd.CustomSearchParameterBean;
import org.wso2.carbon.registry.search.stub.common.xsd.ResourceData;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;

/*
Search Registry metadata by Comments
 */
public class RegistrySearchByCommentsTestCase {

    private SearchAdminServiceClient searchAdminService;
    private WSRegistryServiceClient registry;
    private Registry governance;

    @BeforeClass
    public void init() throws Exception {
        final String SERVER_URL = GregTestUtils.getServerUrl();
        ClientConnectionUtil.waitForPort(Integer.parseInt(FrameworkSettings.HTTP_PORT));
        String sessionCookie = new LoginLogoutUtil().login();
        searchAdminService = new SearchAdminServiceClient(SERVER_URL, sessionCookie);
        registry = GregTestUtils.getRegistry();
        governance = GregTestUtils.getGovernanceRegistry(registry);
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
        TestUtils.cleanupResources(governance);
        addResources();
    }

    @Test(groups = {"wso2.greg"}, description = "Metadata search by available Comment")
    public void searchResourceByComment()
            throws SearchAdminServiceRegistryExceptionException, RemoteException,
                   RegistryException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setCommentWords("TestAutomation");
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminService.getAdvancedSearchResults(searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid Comment");

        for (ResourceData resource : result.getResourceDataList()) {
            boolean commentFound = false;
            for (Comment comment : registry.getComments(resource.getResourcePath())) {
                if (comment.getText().contains("TestAutomation")) {
                    commentFound = true;
                    break;
                }
            }
            Assert.assertTrue(commentFound, "Comment not found on Resource. " + resource.getResourcePath());

        }


    }
    @Test(groups = {"wso2.greg"}, description = "Metadata search by Comment pattern matching")
    public void searchResourceByCommentPattern() throws SearchAdminServiceRegistryExceptionException, RemoteException,
                   RegistryException {

        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setCommentWords("%Automation%");
        ArrayOfString[] paramList
             = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminService.getAdvancedSearchResults(searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length > 0), "No Record Found. set valid Comment pattern");
        for (ResourceData resource : result.getResourceDataList()) {
            boolean commentFound = false;
            for (Comment comment : registry.getComments(resource.getResourcePath())) {
                if (comment.getText().contains("Automation")) {
                    commentFound = true;
                    break;
                }
            }
            Assert.assertTrue(commentFound, "Comment pattern not found on Resource");
        }
    }
    @Test(groups = {"wso2.greg"}, description = "Metadata search by unavailable Comment")
    public void searchResourceByUnAvailableComment() throws SearchAdminServiceRegistryExceptionException, RemoteException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setCommentWords("xyz1234ggf");
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminService.getAdvancedSearchResults(searchQuery);
        Assert.assertNull(result.getResourceDataList(), "Result Object found");


    }

    @Test(dataProvider = "invalidCharacter", groups = {"wso2.greg"},dataProviderClass = Parameters.class,
          description = "Metadata search by Comment with invalid characters")
    public void searchResourceByCommentWithInvalidCharacter(String invalidInput)
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();

        paramBean.setCommentWords(invalidInput);
        ArrayOfString[] paramList = paramBean.getParameterList();
        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = null;
        try {
            result = searchAdminService.getAdvancedSearchResults(searchQuery);
        } finally {
            if (result != null) {
                Assert.assertTrue(result.getErrorMessage().contains("illegal characters"),"Wrong exception");
            }
        }


    }

    private void addResources() throws RegistryException, IOException {
        addService("sns1", "autoService1", "TestAutomationComment");
        addService("sns2", "autoService2", "AutomationComment");
        addWSDL("TestAutomationComment");
        addPolicy("Policy");
        addSchema("AutomationComment");
    }

    private void addService(String nameSpace, String serviceName, String commentString)
            throws RegistryException {
        ServiceManager serviceManager = new ServiceManager(governance);
        Service service;
        service = serviceManager.newService(new QName(nameSpace, serviceName));
        serviceManager.addService(service);
        for (String serviceId : serviceManager.getAllServiceIds()) {
            service = serviceManager.getService(serviceId);
            if (service.getPath().endsWith(serviceName)) {
                Comment comment = new Comment();
                comment.setText(commentString);
                governance.addComment(service.getPath(), comment);
            }
        }
    }

    private void addWSDL(String commentString) throws IOException, RegistryException {
        WsdlManager wsdlManager = new WsdlManager(governance);
        Wsdl wsdl;
        String wsdlFilePath = GregTestUtils.getResourcePath()
                              + File.separator + "wsdl" + File.separator;
        wsdl = wsdlManager.newWsdl(GregTestUtils.readFile(wsdlFilePath + "echo.wsdl").getBytes(), "echo.wsdl");
        wsdlManager.addWsdl(wsdl);
        wsdl = wsdlManager.getWsdl(wsdl.getId());
        Comment comment = new Comment();
        comment.setText(commentString);
        governance.addComment(wsdl.getPath(), comment);
    }

    private void addSchema(String commentString) throws IOException, RegistryException {
        SchemaManager schemaManager = new SchemaManager(governance);
        String schemaFilePath = GregTestUtils.getResourcePath()
                                + File.separator + "schema" + File.separator;
        Schema schema = schemaManager.newSchema(GregTestUtils.readFile(schemaFilePath + "Person.xsd").getBytes(), "Person.xsd");
        schemaManager.addSchema(schema);
        schema = schemaManager.getSchema(schema.getId());
        Comment comment = new Comment();
        comment.setText(commentString);
        governance.addComment(schema.getPath(), comment);
    }

    private void addPolicy(String commentString) throws RegistryException, IOException {
        PolicyManager policyManager = new PolicyManager(governance);
        String policyFilePath = GregTestUtils.getResourcePath()
                                + File.separator + "policy" + File.separator;
        Policy policy = policyManager.newPolicy(GregTestUtils.readFile(policyFilePath + "UTPolicy.xml").getBytes(), "UTPolicy.xml");
        policyManager.addPolicy(policy);
        policy = policyManager.getPolicy(policy.getId());
        Comment comment = new Comment();
        comment.setText(commentString);
        governance.addComment(policy.getPath(), comment);

    }

    @AfterClass
    public void destroy() {
        try {
            CommonUtils.cleanUpResource(registry);
        } catch (RegistryException ignore) {

        }
    }
}
