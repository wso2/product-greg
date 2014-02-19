package org.wso2.carbon.registry.resource.test;

import org.omg.IOP.ComponentIdHelper;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;

import static org.testng.Assert.*;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceStub;
import org.wso2.carbon.registry.activities.stub.ActivityAdminServiceStub;
import org.wso2.carbon.registry.indexing.stub.generated.ContentSearchAdminServiceStub;
import org.wso2.carbon.registry.info.stub.InfoAdminServiceStub;
import org.wso2.carbon.registry.relations.stub.RelationAdminServiceStub;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceStub;
import org.wso2.carbon.registry.search.stub.SearchAdminServiceStub;

import javax.activation.DataHandler;
import java.io.File;
import java.net.URL;

public class TestUtils {

    private static final Log log = LogFactory.getLog(TestUtils.class);
    private static ResourceAdminServiceStub resourceAdminServiceStub;

    public static ResourceAdminServiceStub getResourceAdminServiceStub(String sessionCookie) {
        String serviceURL = null;
        serviceURL = FrameworkSettings.SERVICE_URL + "ResourceAdminService";
        ResourceAdminServiceStub resourceAdminServiceStub = null;
        try {
            resourceAdminServiceStub = new ResourceAdminServiceStub(serviceURL);

            ServiceClient client = resourceAdminServiceStub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, sessionCookie);
            resourceAdminServiceStub._getServiceClient().getOptions().setTimeOutInMilliSeconds(600000);
        } catch (AxisFault axisFault) {
            fail("Unexpected exception thrown");
            axisFault.printStackTrace();
        }
        log.info("ResourceAdminServiceStub created");
        return resourceAdminServiceStub;
    }

    public static ActivityAdminServiceStub getActivityAdminServiceStub(String sessionCookie) {
        String serviceURL;
        serviceURL = FrameworkSettings.SERVICE_URL + "ActivityAdminService";
        ActivityAdminServiceStub activityAdminServiceStub = null;
        try {
            activityAdminServiceStub = new ActivityAdminServiceStub(serviceURL);

            ServiceClient client = activityAdminServiceStub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, sessionCookie);
            activityAdminServiceStub._getServiceClient().getOptions().setTimeOutInMilliSeconds(600000);
        } catch (AxisFault axisFault) {
            fail("Unexpected exception thrown");
            axisFault.printStackTrace();
        }
        log.info("activityAdminServiceStub created");
        return activityAdminServiceStub;
    }

    public static RelationAdminServiceStub getRelationAdminServiceStub(String sessionCookie) {
        String serviceURL;
        serviceURL = FrameworkSettings.SERVICE_URL + "RelationAdminService";
        RelationAdminServiceStub relationAdminServiceStub = null;
        try {
            relationAdminServiceStub = new RelationAdminServiceStub(serviceURL);

            ServiceClient client = relationAdminServiceStub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, sessionCookie);
            relationAdminServiceStub._getServiceClient().getOptions().setTimeOutInMilliSeconds(600000);
        } catch (AxisFault axisFault) {
            fail("Unexpected exception thrown");
            axisFault.printStackTrace();
        }
        log.info("relationAdminServiceStub created");
        return relationAdminServiceStub;

    }

    public static InfoAdminServiceStub getInfoAdminServiceStub(String sessionCookie) {
        String serviceURL = null;
        serviceURL = FrameworkSettings.SERVICE_URL + "InfoAdminService";
        InfoAdminServiceStub infoAdminServiceStub = null;
        try {
            infoAdminServiceStub = new InfoAdminServiceStub(serviceURL);

            ServiceClient client = infoAdminServiceStub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, sessionCookie);
            infoAdminServiceStub._getServiceClient().getOptions().setTimeOutInMilliSeconds(600000);
        } catch (AxisFault axisFault) {
            fail("Unexpected exception thrown");
            axisFault.printStackTrace();
        }
        log.info("infoAdminServiceStub created");
        return infoAdminServiceStub;

    }

    public static ContentSearchAdminServiceStub getContentSearchAdminServiceStub(String sessionCookie) {
        log.debug("sessionCookie:" + sessionCookie);
        String serviceURL = null;
//        FrameworkSettings.getProperty();
        serviceURL = FrameworkSettings.SERVICE_URL + "ContentSearchAdminService";
        ContentSearchAdminServiceStub contentSearchAdminServiceStub = null;
        try {
            contentSearchAdminServiceStub = new ContentSearchAdminServiceStub(serviceURL);

            ServiceClient client = contentSearchAdminServiceStub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, sessionCookie);
            contentSearchAdminServiceStub._getServiceClient().getOptions().setTimeOutInMilliSeconds(600000);
        } catch (AxisFault axisFault) {
            fail("Unexpected exception thrown");
            axisFault.printStackTrace();
        }
        log.info("contentSearchAdminServiceStub created");
        return contentSearchAdminServiceStub;

    }

    public static LifeCycleManagementServiceStub getLifeCycleManagementServiceStub(String sessionCookie) {
        String serviceURL = null;
        serviceURL = FrameworkSettings.SERVICE_URL + "LifeCycleManagementService";
        LifeCycleManagementServiceStub lifeCycleManagementServiceStub = null;
        try {
            lifeCycleManagementServiceStub = new LifeCycleManagementServiceStub(serviceURL);

            ServiceClient client = lifeCycleManagementServiceStub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, sessionCookie);
            lifeCycleManagementServiceStub._getServiceClient().getOptions().setTimeOutInMilliSeconds(600000);
        } catch (AxisFault axisFault) {
            fail("Unexpected exception thrown");
            axisFault.printStackTrace();
        }
        log.info("lifecyclesAdminServiceStub created");
        return lifeCycleManagementServiceStub;

    }

    public static SearchAdminServiceStub getSearchAdminServiceStub(String sessionCookie) {
        String serviceURL;
        serviceURL = FrameworkSettings.SERVICE_URL + "SearchAdminService";
        SearchAdminServiceStub searchAdminServiceStub = null;
        try {
            searchAdminServiceStub = new SearchAdminServiceStub(serviceURL);

            ServiceClient client = searchAdminServiceStub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, sessionCookie);
            searchAdminServiceStub._getServiceClient().getOptions().setTimeOutInMilliSeconds(600000);
        } catch (AxisFault axisFault) {
            fail("Unexpected exception thrown");
            axisFault.printStackTrace();
        }
        log.info("searchAdminServiceStub created");
        return searchAdminServiceStub;

    }


    public static void addSearchSupplResource() {
        String wsdlPath = "/_system/governance/trunk/wsdls/eu/dataaccess/footballpool/";
        String resourceName = "sample.wsdl";
        LoginLogoutUtil util = new LoginLogoutUtil();
        try {
            String loggedInSessionCookie = util.login();
            String frameworkPath = FrameworkSettings.getFrameworkPath();

            resourceAdminServiceStub = getResourceAdminServiceStub(loggedInSessionCookie);

            String resource = frameworkPath + File.separator + ".." + File.separator + ".." + File.separator + ".." +
                    File.separator + "src" + File.separator + "test" + File.separator + "java" + File.separator +
                    "resources" + File.separator + resourceName;

            resourceAdminServiceStub.addResource(wsdlPath + resourceName,
                    "application/wsdl+xml", "test resource", new DataHandler(new URL("file:///" + resource)), null, null);

        } catch (Exception e) {
            fail("Unable to get file content: " + e);
            log.error("Unable to get file content: " + e.getMessage());
        }
    }


}
