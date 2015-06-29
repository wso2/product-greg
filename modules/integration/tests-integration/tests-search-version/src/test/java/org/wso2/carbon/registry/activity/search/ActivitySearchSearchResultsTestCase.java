/*
* Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.activity.search;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperPrint;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.governance.api.endpoints.dataobjects.Endpoint;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.registry.activities.services.ActivityService;
import org.wso2.carbon.registry.activities.stub.RegistryExceptionException;
import org.wso2.carbon.registry.activities.stub.beans.xsd.ActivityBean;
import org.wso2.carbon.registry.activity.search.bean.ActivityReportBean;
import org.wso2.carbon.registry.activity.search.utils.ActivitySearchUtil;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.carbon.reporting.ui.BeanCollectionReportData;
import org.wso2.carbon.reporting.util.JasperPrintProvider;
import org.wso2.carbon.reporting.util.ReportParamMap;
import org.wso2.carbon.reporting.util.ReportStream;
import org.wso2.greg.integration.common.clients.ActivityAdminServiceClient;
import org.wso2.greg.integration.common.clients.ReportResourceSupplierClient;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.clients.UserManagementClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import javax.activation.DataHandler;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertNotNull;


/**
 * A test case which tests registry activity search operation
 */
public class ActivitySearchSearchResultsTestCase extends GREGIntegrationBaseTest {
    private static final Log log = LogFactory.getLog(ActivitySearchSearchResultsTestCase.class);
    private String wsdlPath = "/_system/governance/trunk/wsdls/eu/dataaccess/footballpool/1.0.0/";
    private String resourceName = "sample.wsdl";

    private ResourceAdminServiceClient resourceAdminServiceClient;
    private ReportResourceSupplierClient reportResourceSupplierClient;
    private ActivityAdminServiceClient activityAdminServiceClient;

    private ServiceManager serviceManager;
    private WsdlManager wsdlManager;
    private String sessionCookie;
    private String backEndUrl;
    private String userName;
    private String userNameWithoutDomain;

    @BeforeClass(groups = {"wso2.greg"})
    public void init() throws Exception {
        log.info("Initializing Tests for Activity Search");
        log.debug("Activity Search Tests Initialised");

        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        backEndUrl = getBackendURL();
        sessionCookie = getSessionCookie();
        userName = automationContext.getContextTenant().getContextUser().getUserName();
        if (userName.contains("@"))
            userNameWithoutDomain = userName.substring(0, userName.indexOf('@'));
        else
            userNameWithoutDomain = userName;
        log.debug("Running SuccessCase");

        resourceAdminServiceClient =
                new ResourceAdminServiceClient(backEndUrl,
                                               sessionCookie);
        activityAdminServiceClient =
                new ActivityAdminServiceClient(backEndUrl,
                                               sessionCookie);
        UserManagementClient userManagementClient = new UserManagementClient(backEndUrl,
                                                                             sessionCookie);
        reportResourceSupplierClient =
                new ReportResourceSupplierClient(backEndUrl,
                                                 sessionCookie);
        WSRegistryServiceClient wsRegistry =
                new RegistryProviderUtil().getWSRegistry(automationContext);
        Registry governance = new RegistryProviderUtil().getGovernanceRegistry(wsRegistry, automationContext);
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
        serviceManager = new ServiceManager(governance);
        wsdlManager = new WsdlManager(governance);
    }


    @Test(groups = {"wso2.greg"})

    public void addResource() throws InterruptedException, MalformedURLException,
                                     ResourceAdminServiceExceptionException, RemoteException {
        String resource = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" +
                          File.separator + "GREG" + File.separator +
                          "wsdl" + File.separator + "sample.wsdl";

        resourceAdminServiceClient.addResource(wsdlPath + resourceName,
                                               "application/wsdl+xml", "test resource",
                                               new DataHandler(new URL("file:///" + resource)));


        // wait for sometime until the resource has been added. The activity logs are written
        // every 10 seconds, so you'll need to wait until that's done.
        Thread.sleep(20000);
        // Assert.assertTrue(resourceAdminServiceClient.getResource("/_system/governance/trunk/wsdls/eu/dataaccess/footballpool/"+
        // resourceName )[0].getAuthorUserName().contains(userInfo.getUserName()));

    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = "addResource", description = "check in result page")
    public void verifySearchResultsPage() throws InterruptedException, MalformedURLException,
                                                 ResourceAdminServiceExceptionException,
                                                 RemoteException, RegistryExceptionException {

        assertNotNull(activityAdminServiceClient.getActivities(sessionCookie, "", "", "", "",
                                                               "", 0).getActivity());

    }


    @Test(groups = {"wso2.greg"}, description = "pagination testing", dependsOnMethods = "verifySearchResultsPage")
    public void verifySearchResultsPagination() throws InterruptedException, MalformedURLException,
                                                       ResourceAdminServiceExceptionException,
                                                       RemoteException, RegistryExceptionException {
        for (int k = 0; k < 100; k++) {
            addResource();
        }
        assertNotNull(activityAdminServiceClient.getActivities(sessionCookie, "", "", "", "",
                                                               "", 0).getActivity());
        assertNotNull(activityAdminServiceClient.getActivities(sessionCookie, "", "", "", "",
                                                               "", 1).getActivity());
    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = "verifySearchResultsPagination", description = "checking pdf Report Generation")
    public void pdfReportTest()
            throws Exception{
        assertNotNull(getReportOutputStream("pdf"));
    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = "pdfReportTest", description = "checking html Report Generation")
    public void htmlReportTest()
            throws Exception, MalformedURLException, ResourceAdminServiceExceptionException,
                   RemoteException {
        assertNotNull(getReportOutputStream("html"));
    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = "htmlReportTest", description = "checking excel Report Generation")
    public void excelReportTest()
            throws Exception, MalformedURLException, ResourceAdminServiceExceptionException,
                   RemoteException {
        assertNotNull(getReportOutputStream("excel"));
    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = "excelReportTest", description = "consecutive searching")
    public void searchAgainLink() throws InterruptedException, MalformedURLException,
                                         ResourceAdminServiceExceptionException, RemoteException,
                                         RegistryExceptionException {
        assertNotNull(activityAdminServiceClient.getActivities(sessionCookie, "", "", "", "",
                                                               "", 0).getActivity());

        assertNotNull(activityAdminServiceClient.getActivities(sessionCookie, "", "", "", "",
                                                               "", 0).getActivity());
    }

    private ByteArrayOutputStream getReportOutputStream(String type) throws Exception{
        ActivityService service = new ActivityService();

        ActivityBean bean = activityAdminServiceClient.getActivities
                (sessionCookie, userNameWithoutDomain, "", "", "", "", 0);
        Assert.assertNotNull(bean);
        bean.getActivity();
        Thread.sleep(20000);
        List<ActivityReportBean> beanList = new ArrayList<ActivityReportBean>();


        for (String stringBean : activityAdminServiceClient.getActivities
                (sessionCookie, userNameWithoutDomain, "", "", "", "", 0).getActivity()) {

            ActivityReportBean beanOne = new ActivityReportBean();
            beanOne.setUserName(userNameWithoutDomain);
            beanOne.setActivity(stringBean);
            beanOne.setAccessedTime("");
            beanOne.setResourcePath("");
            beanList.add(beanOne);
        }
        String reportResource = reportResourceSupplierClient.getReportResource(ActivitySearchUtil.COMPONENT,
                                                                               ActivitySearchUtil.TEMPLATE);
        JRDataSource jrDataSource = new BeanCollectionReportData().getReportDataSource(beanList);
        JasperPrintProvider jasperPrintProvider = new JasperPrintProvider();
        JasperPrint jasperPrint = jasperPrintProvider.createJasperPrint(jrDataSource, reportResource, new ReportParamMap[0]);
        ReportStream reportStream = new ReportStream();
        return reportStream.getReportStream(jasperPrint, type);
    }

    @AfterClass(groups = {"wso2.greg"})
    public void deleteResources()
            throws ResourceAdminServiceExceptionException, RemoteException, GovernanceException {
        Endpoint[] endpoints = null;
        Wsdl[] wsdls = wsdlManager.getAllWsdls();
        for (Wsdl wsdl : wsdls) {
            if (wsdl.getQName().getLocalPart().equals("sample.wsdl")) {
                endpoints = wsdlManager.getWsdl(wsdl.getId()).getAttachedEndpoints();
            }
        }
        resourceAdminServiceClient.deleteResource(wsdlPath + "sample.wsdl");
        for (Endpoint path : endpoints) {
            resourceAdminServiceClient.deleteResource("_system/governance/" + path.getPath());
        }

        Service[] services = serviceManager.getAllServices();
        for (Service service : services) {
            if (service.getQName().getLocalPart().equals("Info")) {
                serviceManager.removeService(service.getId());
            }
        }

        resourceAdminServiceClient = null;
        serviceManager = null;
        wsdlManager = null;

    }

}
