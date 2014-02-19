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
package org.wso2.carbon.registry.jira.issues.test;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.governance.ListMetaDataServiceClient;
import org.wso2.carbon.automation.api.clients.registry.ActivityAdminServiceClient;
import org.wso2.carbon.automation.api.clients.registry.RelationAdminServiceClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.api.clients.reporting.ReportResourceSupplierClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.registry.activities.services.ActivityService;
import org.wso2.carbon.registry.activity.search.utils.ActivitySearchUtil;
import org.wso2.carbon.registry.common.beans.ActivityBean;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.relations.stub.AddAssociationRegistryExceptionException;
import org.wso2.carbon.registry.relations.stub.beans.xsd.AssociationBean;
import org.wso2.carbon.registry.relations.stub.beans.xsd.DependenciesBean;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.carbon.reporting.api.ReportingException;
import org.wso2.carbon.reporting.stub.ReportingResourcesSupplierReportingExceptionException;
import org.wso2.carbon.reporting.ui.BeanCollectionReportData;
import org.wso2.carbon.reporting.util.JasperPrintProvider;
import org.wso2.carbon.reporting.util.ReportParamMap;
import org.wso2.carbon.reporting.util.ReportStream;

import javax.activation.DataHandler;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertNotNull;

public class Registry916 {

    private static final Log log = LogFactory.getLog(Registry916.class);
    private String wsdlPath = "/_system/governance/trunk/wsdls/eu/dataaccess/footballpool/";
    private String resourceName = "sample.wsdl";
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private ReportResourceSupplierClient reportResourceSupplierClient;
    private ActivityAdminServiceClient activityAdminServiceClient;
    private RelationAdminServiceClient relationAdminServiceClient;
    private ManageEnvironment environment;
    UserInfo userInfo;
    private WSRegistryServiceClient wsRegistry;

    @BeforeClass(groups = {"wso2.greg"})
    public void init() throws Exception {
        log.info("Initializing Tests for Activity Search");
        log.debug("Activity Search Tests Initialised");
        int userId = ProductConstant.ADMIN_USER_ID;
        userInfo = UserListCsvReader.getUserInfo(userId);
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        environment = builder.build();
        log.debug("Running SuccessCase");
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                               environment.getGreg().getSessionCookie());

        relationAdminServiceClient =
                new RelationAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                               environment.getGreg().getSessionCookie());
        activityAdminServiceClient =
                new ActivityAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                               environment.getGreg().getSessionCookie());
        reportResourceSupplierClient =
                new ReportResourceSupplierClient(environment.getGreg().getBackEndUrl(),
                                                 environment.getGreg().getSessionCookie());
        ListMetaDataServiceClient listMetaDataServiceClient =
                new ListMetaDataServiceClient(environment.getGreg().getBackEndUrl(),
                                              environment.getGreg().getSessionCookie());

        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();

        wsRegistry =
                registryProviderUtil.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);

    }

    @Test(groups = {"wso2.greg"})

    public void addResource() throws InterruptedException, MalformedURLException,
                                     ResourceAdminServiceExceptionException, RemoteException {
        String resource = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
                          File.separator + "GREG" + File.separator +
                          "wsdl" + File.separator + "sample.wsdl";

        resourceAdminServiceClient.addResource(wsdlPath + resourceName,
                                               "application/wsdl+xml", "test resource",
                                               new DataHandler(new URL("file:///" + resource)));


        // wait for sometime until the resource has been added. The activity logs are written
        // every 10 seconds, so you'll need to wait until that's done.
        // Thread.sleep(20000);
        // assertTrue(resourceAdminServiceClient.getResource("/_system/governance/trunk/wsdls/eu/dataaccess/footballpool/"+
        // resourceName )[0].getAuthorUserName().contains(userInfo.getUserName()));

    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = "addResource", description = "Test the Repeated Column Headers in Html Report")
    public void htmlReportTest()
            throws Exception, MalformedURLException, ResourceAdminServiceExceptionException,
                   RemoteException {
        assertNotNull(getReportOutputStream("html"));
        String htmlReport = new String(getReportOutputStream("html").toByteArray());
        if (htmlReport.split("User Name").length > 2) {
            Assert.assertTrue(false);
        }
        if (htmlReport.split("Resource Path").length > 2) {
            Assert.assertTrue(false);
        }
        if (htmlReport.split("Accsessed Time").length > 2) {
            Assert.assertTrue(false);
        }

    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = "htmlReportTest", description = "Test the Repeated Column Headers in the excelReport")
    public void excelReportTest()
            throws Exception, MalformedURLException, ResourceAdminServiceExceptionException,
                   RemoteException {
        assertNotNull(getReportOutputStream("excel"));
        String excelReport = new String(getReportOutputStream("excel").toByteArray());
        if (excelReport.split("User Name").length > 2) {
            Assert.assertTrue(false);
        }
        if (excelReport.split("Resource Path").length > 2) {
            Assert.assertTrue(false);
        }
        if (excelReport.split("Accsessed Time").length > 2) {
            Assert.assertTrue(false);
        }

    }


    @AfterClass(groups = {"wso2.greg"})
    public void deleteResource() throws ResourceAdminServiceExceptionException, RemoteException,
                                        AddAssociationRegistryExceptionException,
                                        RegistryException {

        String servicePath = "/_system/governance/trunk/services/eu/dataaccess/footballpool/Info";
        deleteDependencies(servicePath);
        deleteDependencies(wsdlPath + resourceName);
        if (wsRegistry.resourceExists(wsdlPath + resourceName)) {
            resourceAdminServiceClient.deleteResource(wsdlPath + resourceName);
        }

        if (wsRegistry.resourceExists(servicePath)) {
            resourceAdminServiceClient.deleteResource(servicePath);
        }
        resourceAdminServiceClient = null;
        reportResourceSupplierClient = null;
        activityAdminServiceClient = null;
        environment = null;
        userInfo = null;
    }

    private void deleteDependencies(String path)
            throws RemoteException, AddAssociationRegistryExceptionException,
                   ResourceAdminServiceExceptionException {
        DependenciesBean dependenciesBean =
                relationAdminServiceClient.getDependencies(path);
        if (dependenciesBean != null) {
            AssociationBean[] associationBean = dependenciesBean.getAssociationBeans();
            if (associationBean[0] != null) {
                for (AssociationBean anAssociationBean : associationBean) {
                    if (anAssociationBean.getAssociationType().equals("depends")) {
                        relationAdminServiceClient.addAssociation(path, "depends", anAssociationBean.getDestinationPath(), "remove");
                    }
                    if (anAssociationBean.getAssociationType().equals("usedBy")) {
                        relationAdminServiceClient.addAssociation(path, "usedBy", anAssociationBean.getDestinationPath(), "remove");
                    }
                }
            }
        }
    }

    private ByteArrayOutputStream getReportOutputStream(String type)
            throws Exception, RemoteException,
                   ReportingResourcesSupplierReportingExceptionException,
                   ReportingException,
                   JRException,
                   RegistryException {
        ActivityService service = new ActivityService();
        activityAdminServiceClient.getActivities(environment.getGreg().getSessionCookie(), "", "", "", "", "", 0);
        ActivityBean beanOne = service.getActivities(userInfo.getUserNameWithoutDomain(), "", "", "", "", "",
                                                     environment.getGreg().getSessionCookie());
        List<ActivityBean> beanList = new ArrayList<ActivityBean>();
        beanList.add(beanOne);
        String reportResource = reportResourceSupplierClient.getReportResource(ActivitySearchUtil.COMPONENT,
                                                                               ActivitySearchUtil.TEMPLATE);
        JRDataSource jrDataSource = new BeanCollectionReportData().getReportDataSource(beanList);
        JasperPrintProvider jasperPrintProvider = new JasperPrintProvider();
        JasperPrint jasperPrint = jasperPrintProvider.createJasperPrint(jrDataSource, reportResource, new ReportParamMap[0]);
        ReportStream reportStream = new ReportStream();
        return reportStream.getReportStream(jasperPrint, type);


    }
}
