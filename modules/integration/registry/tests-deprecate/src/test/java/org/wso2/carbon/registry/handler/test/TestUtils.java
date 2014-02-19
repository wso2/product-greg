package org.wso2.carbon.registry.handler.test;

import junit.framework.Assert;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.registry.handler.stub.HandlerManagementServiceStub;
import org.wso2.carbon.registry.relations.stub.RelationAdminServiceStub;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceStub;

import java.io.File;

import static org.testng.Assert.fail;

public class TestUtils {

    private static final Log log = LogFactory.getLog(TestUtils.class);

    public static String getHandlerResourcePath(String frameworkPath) {
        return frameworkPath + File.separator + ".." + File.separator + ".." + File.separator + ".." +
                File.separator + "src" + File.separator + "test" + File.separator + "java" + File.separator +
                "resources" + File.separator + "sample-handler.xml";
    }

    public static String getTestResourcesDir(String frameworkPath) {
        return frameworkPath + File.separator + ".." + File.separator + ".." + File.separator + ".." + File.separator + ".." +
                File.separator + "tests" + File.separator + "src" + File.separator + "test" + File.separator +
                "java" + File.separator + "resources";
    }

    public static String getTestSamplesDir(String frameworkPath) {
        return frameworkPath + File.separator + ".." + File.separator + ".." + File.separator + ".." + File.separator + ".." +
                File.separator + "test.samples";
    }

    public static HandlerManagementServiceStub getHandlerManagementServiceStub(String sessionCookie) {
        String serviceURL = null;
        serviceURL = FrameworkSettings.SERVICE_URL + "HandlerManagementService";
        HandlerManagementServiceStub handlerManagementServiceStub = null;
        try {
            handlerManagementServiceStub = new HandlerManagementServiceStub(serviceURL);
            ServiceClient client = handlerManagementServiceStub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, sessionCookie);
            handlerManagementServiceStub._getServiceClient().getOptions().setTimeOutInMilliSeconds(600000);
        } catch (AxisFault axisFault) {
            Assert.fail("Unexpected exception thrown");
            axisFault.printStackTrace();
        }
        log.info("handlerManagementServiceStub created");
        return handlerManagementServiceStub;
    }

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
}
