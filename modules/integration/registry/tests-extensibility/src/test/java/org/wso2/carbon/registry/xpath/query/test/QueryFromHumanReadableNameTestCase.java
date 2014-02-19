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


package org.wso2.carbon.registry.xpath.query.test;

import org.apache.axis2.AxisFault;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.authenticator.stub.LogoutAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.registry.*;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.api.endpoints.dataobjects.Endpoint;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.info.stub.RegistryExceptionException;
import org.wso2.carbon.registry.properties.stub.PropertiesAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.relations.stub.AddAssociationRegistryExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.carbon.registry.xpath.query.test.utils.XpathQueryUtil;

import javax.activation.DataHandler;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;


import static org.testng.Assert.assertTrue;

public class QueryFromHumanReadableNameTestCase {
    public WSRegistryServiceClient registryAdmin;

    private String wsdlPath = "/_system/governance/trunk/wsdls/eu/dataaccess/footballpool/sample.wsdl";
    private String txtPath = "/_system/config/testResource";
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private PropertiesAdminServiceClient propertiesAdminServiceClient;
    private RelationAdminServiceClient relationalServiceClient;
    private ManageEnvironment environmentAdmin;
    private ManageEnvironment environmentNormal;
    private InfoServiceAdminClient infoServiceAdminClient;
    private ServiceManager serviceManager;
    private WsdlManager wsdlManager;
    private UserInfo userInfo;
    private UserInfo normalUser;
    private String resource = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
                              File.separator + "GREG" + File.separator +
                              "wsdl" + File.separator + "sample.wsdl";
    private String resourceTXT = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
                                 File.separator + "GREG" + File.separator + "resource.txt";
    private String policy = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
                            File.separator + "GREG" + File.separator +
                            "policy" + File.separator + "policy.xml";
    private Registry governance;

    @BeforeClass(groups = {"wso2.greg"})
    public void init()
            throws MalformedURLException, RegistryException, LoginAuthenticationExceptionException,
                   RemoteException {
        normalUser = UserListCsvReader.getUserInfo(2);
        int userId = ProductConstant.ADMIN_USER_ID;
        userInfo = UserListCsvReader.getUserInfo(userId);
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        EnvironmentBuilder builderNormal = new EnvironmentBuilder().greg(2);


        environmentAdmin = builder.build();
        environmentNormal = builderNormal.build();
        RegistryProviderUtil providerUtil = new RegistryProviderUtil();
        registryAdmin = providerUtil.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);

        resourceAdminServiceClient =
                new ResourceAdminServiceClient(environmentAdmin.getGreg().getBackEndUrl(),
                                               environmentAdmin.getGreg().getSessionCookie());
        propertiesAdminServiceClient =
                new PropertiesAdminServiceClient(environmentAdmin.getGreg().getBackEndUrl(),
                                                 environmentAdmin.getGreg().getSessionCookie());
        infoServiceAdminClient =
                new InfoServiceAdminClient(environmentAdmin.getGreg().getBackEndUrl(),
                                           environmentAdmin.getGreg().getSessionCookie());
        relationalServiceClient =
                new RelationAdminServiceClient(environmentAdmin.getGreg().getBackEndUrl(),
                                               environmentAdmin.getGreg().getSessionCookie());
        WSRegistryServiceClient wsRegistry =
                new RegistryProviderUtil().getWSRegistry(userId,
                                                         ProductConstant.GREG_SERVER_NAME);
        governance = new RegistryProviderUtil().getGovernanceRegistry(wsRegistry, userId);
        serviceManager = new ServiceManager(governance);
        wsdlManager = new WsdlManager(governance);

    }

    @Test(groups = {"wso2.greg"}, description = "Adding wsdl file to Registry")
    public void addResource() throws InterruptedException, MalformedURLException,
                                     ResourceAdminServiceExceptionException, RemoteException {

        resourceAdminServiceClient.addResource(wsdlPath,
                                               "application/wsdl+xml", "wsdl resource",
                                               new DataHandler(new URL("file:///" + resource)));

        // wait for sometime until the resource has been added. The activity logs are written
        // every 10 seconds, so you'll need to wait until that's done.
        Thread.sleep(20000);
        assertTrue(resourceAdminServiceClient.getResource(wsdlPath)[0].getAuthorUserName().
                contains(userInfo.getUserNameWithoutDomain()));
    }

    /**
     * Query Resource Belonging to Given Media Type
     */
    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"addResource"}, description = "Query Resource Belonging To Media Type wsdl")
    public void queryResourceBelongingToMediaType()
            throws org.wso2.carbon.registry.api.RegistryException {

        String[] paths = XpathQueryUtil.searchFromXpathQuery(registryAdmin, null, "//WSDL");
        Assert.assertNotNull(paths);
        boolean pass = false;
        for (String path : paths) {
            if (path.equals(wsdlPath)) {
                pass = true;
                break;
            }

        }
        Assert.assertTrue(pass);

    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"queryResourceBelongingToMediaType"}, description = "Query Resource Created By Admin ")
    public void queryResourceByUser() throws RegistryException {
        String[] paths = XpathQueryUtil.searchFromXpathQuery(registryAdmin, null, "//WSDL[@author='admin']");
        Assert.assertNotNull(paths);
        boolean pass = false;
        for (String path : paths) {
            if (path.equals(wsdlPath)) {
                pass = true;
                break;
            }

        }
        Assert.assertTrue(pass);
    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"queryResourceByUser"}, description = "Adding Property")
    public void addProperty()
            throws PropertiesAdminServiceRegistryExceptionException, RemoteException {
        propertiesAdminServiceClient.setProperty(wsdlPath, "property", "value");
    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"addProperty"}, description = "Query Resource By Property Name")
    public void queryResourceByPropertyName() throws RegistryException {
        String[] paths = XpathQueryUtil.searchFromXpathQuery(registryAdmin, null,
                                                             "//WSDL[@propertyName='property']");
        Assert.assertNotNull(paths);
        boolean pass = false;
        for (String path : paths) {
            if (path.equals(wsdlPath)) {
                pass = true;
                break;
            }

        }
        Assert.assertTrue(pass);
    }


    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"queryResourceByPropertyName"}, description = " Updating Resource")
    public void updateResource()
            throws MalformedURLException, ResourceAdminServiceExceptionException,
                   RemoteException {
        resourceAdminServiceClient.addSchema("schema", new DataHandler(new URL("file:///" + resource)));
    }


    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"updateResource"}, description = "Query Resource By Updater")
    public void queryResourceByUpdatedUser() throws RegistryException {
        String[] paths = XpathQueryUtil.searchFromXpathQuery(registryAdmin, null, "//WSDL[@updater='admin']");
        Assert.assertNotNull(paths);
        boolean pass = false;
        for (String path : paths) {
            if (path.equals(wsdlPath)) {
                pass = true;
                break;
            }

        }
        Assert.assertTrue(pass);
    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"queryResourceByUpdatedUser"}, description = "Query Resource By Property Value")
    public void queryResourceByPropertyValue() throws RegistryException {
        String[] paths = XpathQueryUtil.searchFromXpathQuery(registryAdmin, null, "//WSDL[@propertyValue='value']");
        Assert.assertNotNull(paths);
        boolean pass = false;
        for (String path : paths) {
            if (path.equals(wsdlPath)) {
                pass = true;
                break;
            }

        }
        Assert.assertTrue(pass);
    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"queryResourceByPropertyValue"}, description = "Query Resource By Property value")
    public void queryResourceByPropertyNameAndPropertyValue() throws RegistryException {
        String[] paths = XpathQueryUtil.searchFromXpathQuery
                (registryAdmin, null, "//WSDL[@propertyName='property' and @propertyValue='value']");
        Assert.assertNotNull(paths);
        boolean pass = false;
        for (String path : paths) {
            if (path.equals(wsdlPath)) {
                pass = true;
                break;
            }

        }
        Assert.assertTrue(pass);
    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = "queryResourceByPropertyNameAndPropertyValue", description = "Update Resource By Normal User")
    public void updatingResourceByNormalUser() throws RemoteException,
                                                      MalformedURLException,
                                                      ResourceAdminServiceExceptionException,
                                                      PropertiesAdminServiceRegistryExceptionException,
                                                      LogoutAuthenticationExceptionException,
                                                      LoginAuthenticationExceptionException {

        PropertiesAdminServiceClient propertiesAdminServiceClientNormal =
                new PropertiesAdminServiceClient(environmentNormal.getGreg().getBackEndUrl(),
                                                 environmentNormal.getGreg().getSessionCookie());
        propertiesAdminServiceClientNormal.setProperty(wsdlPath, normalUser.getUserNameWithoutDomain(), normalUser.getPassword());

    }


    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"updatingResourceByNormalUser"}, description = "Query Resource Created By Admin and Updated By Normal User")
    public void queryResourceByCreatedByAdminAndUpdatedByUser() throws RegistryException {
        String[] paths = XpathQueryUtil.searchFromXpathQuery
                (registryAdmin, null, "//WSDL[@author='admin' and @updater='" + normalUser.getUserNameWithoutDomain() + "']");
        Assert.assertNotNull(paths);
        boolean pass = false;
        for (String path : paths) {

            if (path.equals(wsdlPath)) {
                pass = true;
                break;
            }

        }
        Assert.assertTrue(pass);

    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"queryResourceByCreatedByAdminAndUpdatedByUser"}, description = "Query Resource Created By Admin or Updating BY Normal User")
    public void queryResourceByCreatedByAdminOrUpdatedByUser() throws RegistryException {
        String[] paths = XpathQueryUtil.searchFromXpathQuery
                (registryAdmin, null, "//WSDL[@author='admin' or @updater='" + normalUser.getUserNameWithoutDomain() + "']");
        Assert.assertNotNull(paths);
        boolean pass = false;
        for (String path : paths) {
            if (path.equals(wsdlPath)) {
                pass = true;
                break;
            }

        }
        Assert.assertTrue(pass);

    }

    @Test(groups = {"wso2.greg"}, description = "Add Policy", dependsOnMethods = "queryResourceByCreatedByAdminOrUpdatedByUser")
    public void addPolicy()
            throws MalformedURLException, ResourceAdminServiceExceptionException, RemoteException {
        resourceAdminServiceClient.addPolicy("policy", new DataHandler(new URL("file:///" + policy)));

    }

    /**
     * Checking Whether Policy Is returned.Negative Test Case
     */

    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"addPolicy"}, description = "Checking Policy Has Returned ")
    public void checkNonMediaTypeReturn() throws RegistryException {
        String[] paths = XpathQueryUtil.searchFromXpathQuery(registryAdmin, null, "//WSDL");
        Assert.assertNotNull(paths);
        boolean pass = true;
        for (String path : paths) {

            if (path.contains("policy.xml")) {
                pass = false;
                break;
            }

        }
        Assert.assertTrue(pass);


    }


    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"checkNonMediaTypeReturn"}, description = "Adding Comment")
    public void addComment() throws RegistryException, AxisFault {
        infoServiceAdminClient.addComment
                ("comment", wsdlPath, environmentAdmin.getGreg().getSessionCookie());

    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"addComment"}, description = "Query Resource By Comment")
    public void queryResourceByGivenComment() throws RegistryException, AxisFault {
        String[] paths = XpathQueryUtil.searchFromXpathQuery(registryAdmin, null, "//WSDL[@commentWords='comment']");
        Assert.assertNotNull(paths);
        boolean pass = false;
        for (String path : paths) {

            if (path.equals(wsdlPath)) {
                pass = true;
                break;
            }

        }
        Assert.assertTrue(pass);

    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"queryResourceByGivenComment"}, description = "Adding Tag")
    public void addTag() throws RegistryException, AxisFault {
        infoServiceAdminClient.addTag("this is a tag", wsdlPath, environmentAdmin.getGreg().getSessionCookie());
    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"addTag"}, description = "Query Resource By Tag")
    public void queryResourceByGivenTag() throws RegistryException, AxisFault {
        String[] paths = XpathQueryUtil.searchFromXpathQuery(registryAdmin, null, "//WSDL[@tags='this is a tag']");
        Assert.assertNotNull(paths);
        boolean pass = false;
        for (String path : paths) {

            if (path.equals(wsdlPath)) {
                pass = true;
                break;
            }

        }
        Assert.assertTrue(pass);

    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"queryResourceByGivenTag"}, description = "Add Content")
    public void addContent() throws org.wso2.carbon.registry.api.RegistryException, RemoteException,
                                    ResourceAdminServiceExceptionException, MalformedURLException {
        resourceAdminServiceClient.addResource(txtPath,
                                               "text/plain", "txt resource",
                                               new DataHandler(new URL("file:///" + resourceTXT)));
        resourceAdminServiceClient.updateTextContent(txtPath, "newContent");

    }


    /**
     * JiraIssue -https://wso2.org/jira/browse/REGISTRY-1152
     */
    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"addContent"})
    public void queryResourceByGivenContentAndHumanReadableType() throws RegistryException {
        String[] paths = XpathQueryUtil.searchFromXpathQuery(registryAdmin, null, "//WSDL[@content='http']");
        Assert.assertNotNull(paths);
        boolean pass = false;
        for (String path : paths) {

            if (path.equals(wsdlPath)) {
                pass = true;
                break;
            }

        }
        Assert.assertTrue(pass);


    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"queryResourceByGivenContentAndHumanReadableType"}, description = "Add Association")
    public void addAssociation() throws AddAssociationRegistryExceptionException, RemoteException {
        relationalServiceClient.addAssociation(wsdlPath, "depends", "/_system/governance/", "add");
    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"addAssociation"}, description = "Query Property With Association")
    public void queryResourceBySpecifiedProperty() throws RegistryException {
        String[] paths = XpathQueryUtil.searchFromXpathQuery
                (registryAdmin, null, "//WSDL[@propertyName='property']/depends");
        Assert.assertNotNull(paths);
        boolean pass = false;
        for (String path : paths) {

            if (path.equals("/_system/governance")) {
                pass = true;
                break;
            }

        }
        Assert.assertTrue(pass);


    }

    @AfterClass(groups = {"wso2.greg"}, description = "Deleting Added Resources")
    public void deleteResources() throws ResourceAdminServiceExceptionException, RemoteException,
                                         PropertiesAdminServiceRegistryExceptionException,
                                         RegistryException, RegistryExceptionException,
                                         AddAssociationRegistryExceptionException {

        relationalServiceClient.addAssociation(wsdlPath, "depends", "/_system/governance/", "remove");
        Endpoint[] endpoints = null;
        Wsdl[] wsdls = wsdlManager.getAllWsdls();
        for (Wsdl wsdl : wsdls) {
            if (wsdl.getQName().getLocalPart().equals("sample.wsdl")) {
                endpoints = wsdlManager.getWsdl(wsdl.getId()).getAttachedEndpoints();
            }
        }
        resourceAdminServiceClient.deleteResource(wsdlPath);
        resourceAdminServiceClient.deleteResource("/_system/governance/trunk/schemas/eu/dataaccess/footballpool/sample.xsd");
        assert endpoints != null;
        for (Endpoint path : endpoints) {
            resourceAdminServiceClient.deleteResource("_system/governance/" + path.getPath());
        }
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
        Service[] services = serviceManager.getAllServices();
        for (Service service : services) {
            if (service.getQName().getLocalPart().equals("Info")) {
                serviceManager.removeService(service.getId());
            }
        }
        resourceAdminServiceClient.deleteResource(txtPath);
        resourceAdminServiceClient.deleteResource("/_system/governance/trunk/policies/policy.xml");
        registryAdmin = null;
        resourceAdminServiceClient = null;
        propertiesAdminServiceClient = null;
        relationalServiceClient = null;
        environmentAdmin = null;
        environmentNormal = null;
        infoServiceAdminClient = null;

    }


}
