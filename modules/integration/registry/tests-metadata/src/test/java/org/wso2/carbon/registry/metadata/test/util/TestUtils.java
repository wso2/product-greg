package org.wso2.carbon.registry.metadata.test.util;

import junit.framework.Assert;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceStub;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceStub;
import org.wso2.carbon.governance.services.stub.AddServicesServiceStub;
import org.wso2.carbon.registry.info.stub.InfoAdminServiceStub;
import org.wso2.carbon.registry.properties.stub.PropertiesAdminServiceStub;
import org.wso2.carbon.registry.relations.stub.RelationAdminServiceStub;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceStub;
import org.wso2.carbon.registry.resource.stub.beans.xsd.CollectionContentBean;

import java.rmi.RemoteException;

public class TestUtils {

    private static final Log log = LogFactory.getLog(TestUtils.class);


//    public static ResourceAdminServiceStub getResourceAdminServiceStub(String sessionCookie) {
//        String serviceURL = null;
//        serviceURL = FrameworkSettings.SERVICE_URL + "ResourceAdminService";
//        ResourceAdminServiceStub resourceAdminServiceStub = null;
//        try {
//            resourceAdminServiceStub = new ResourceAdminServiceStub(serviceURL);
//
//            ServiceClient client = resourceAdminServiceStub._getServiceClient();
//            Options option = client.getOptions();
//            option.setManageSession(true);
//            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, sessionCookie);
//            resourceAdminServiceStub._getServiceClient().getOptions().setTimeOutInMilliSeconds(600000);
//        } catch (AxisFault axisFault) {
//            Assert.fail("Unexpected exception thrown");
//            axisFault.printStackTrace();
//        }
//        log.info("ResourceAdminServiceStub created");
//        return resourceAdminServiceStub;
//    }
//
//    public static RelationAdminServiceStub getRelationAdminServiceStub(String sessionCookie) {
//        String serviceURL;
//        serviceURL = FrameworkSettings.SERVICE_URL + "RelationAdminService";
//        RelationAdminServiceStub relationAdminServiceStub = null;
//        try {
//            relationAdminServiceStub = new RelationAdminServiceStub(serviceURL);
//
//            ServiceClient client = relationAdminServiceStub._getServiceClient();
//            Options option = client.getOptions();
//            option.setManageSession(true);
//            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, sessionCookie);
//            relationAdminServiceStub._getServiceClient().getOptions().setTimeOutInMilliSeconds(600000);
//        } catch (AxisFault axisFault) {
//            Assert.fail("Unexpected exception thrown");
//            axisFault.printStackTrace();
//        }
//        log.info("relationAdminServiceStub created");
//        return relationAdminServiceStub;
//
//    }
//
//    public static InfoAdminServiceStub getInfoAdminServiceStub(String sessionCookie) {
//        String serviceURL = null;
//        serviceURL = FrameworkSettings.SERVICE_URL + "InfoAdminService";
//        InfoAdminServiceStub infoAdminServiceStub = null;
//        try {
//            infoAdminServiceStub = new InfoAdminServiceStub(serviceURL);
//
//            ServiceClient client = infoAdminServiceStub._getServiceClient();
//            Options option = client.getOptions();
//            option.setManageSession(true);
//            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, sessionCookie);
//            infoAdminServiceStub._getServiceClient().getOptions().setTimeOutInMilliSeconds(600000);
//        } catch (AxisFault axisFault) {
//            Assert.fail("Unexpected exception thrown");
//            axisFault.printStackTrace();
//        }
//        log.info("infoAdminServiceStub created");
//        return infoAdminServiceStub;
//
//    }
//
//    public static LifeCycleManagementServiceStub getLifeCycleManagementServiceStub(String sessionCookie) {
//        String serviceURL = null;
//        serviceURL = FrameworkSettings.SERVICE_URL + "LifeCycleManagementService";
//        LifeCycleManagementServiceStub lifeCycleManagementServiceStub = null;
//        try {
//            lifeCycleManagementServiceStub = new LifeCycleManagementServiceStub(serviceURL);
//
//            ServiceClient client = lifeCycleManagementServiceStub._getServiceClient();
//            Options option = client.getOptions();
//            option.setManageSession(true);
//            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, sessionCookie);
//            lifeCycleManagementServiceStub._getServiceClient().getOptions().setTimeOutInMilliSeconds(600000);
//        } catch (AxisFault axisFault) {
//            Assert.fail("Unexpected exception thrown");
//            axisFault.printStackTrace();
//        }
//        log.info("lifecyclesAdminServiceStub created");
//        return lifeCycleManagementServiceStub;
//
//    }
//
//    public static CustomLifecyclesChecklistAdminServiceStub getCustomLifecyclesChecklistAdminServiceStub(String sessionCookie) {
//        String serviceURL = null;
//        serviceURL = FrameworkSettings.SERVICE_URL + "CustomLifecyclesChecklistAdminService";
//        CustomLifecyclesChecklistAdminServiceStub customLifecyclesChecklistAdminServiceStub = null;
//        try {
//            customLifecyclesChecklistAdminServiceStub = new CustomLifecyclesChecklistAdminServiceStub(serviceURL);
//
//            ServiceClient client = customLifecyclesChecklistAdminServiceStub._getServiceClient();
//            Options option = client.getOptions();
//            option.setManageSession(true);
//            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, sessionCookie);
//            customLifecyclesChecklistAdminServiceStub._getServiceClient().getOptions().setTimeOutInMilliSeconds(600000);
//        } catch (AxisFault axisFault) {
//            Assert.fail("Unexpected exception thrown");
//            axisFault.printStackTrace();
//        }
//        log.info("lifecyclesAdminServiceStub created");
//        return customLifecyclesChecklistAdminServiceStub;
//
//    }
//
//    public static PropertiesAdminServiceStub getPropertiesAdminServiceStub(String sessionCookie) {
//        String serviceURL;
//        serviceURL = FrameworkSettings.SERVICE_URL + "PropertiesAdminService";
//        PropertiesAdminServiceStub propertiesAdminServiceStub = null;
//        try {
//            propertiesAdminServiceStub = new PropertiesAdminServiceStub(serviceURL);
//
//            ServiceClient client = propertiesAdminServiceStub._getServiceClient();
//            Options option = client.getOptions();
//            option.setManageSession(true);
//            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, sessionCookie);
//            propertiesAdminServiceStub._getServiceClient().getOptions().setTimeOutInMilliSeconds(600000);
//        } catch (AxisFault axisFault) {
//            Assert.fail("Unexpected exception thrown");
//            axisFault.printStackTrace();
//        }
//        log.info("propertiesAdminServiceStub created");
//        return propertiesAdminServiceStub;
//
//    }
//
//    public static AddServicesServiceStub getAddServicesServiceStub(String sessionCookie) {
//        String serviceURL = null;
//        serviceURL = FrameworkSettings.SERVICE_URL + "AddServicesService";
//        AddServicesServiceStub addServicesServiceStub = null;
//        try {
//            addServicesServiceStub = new AddServicesServiceStub(serviceURL);
//
//            ServiceClient client = addServicesServiceStub._getServiceClient();
//            Options option = client.getOptions();
//            option.setManageSession(true);
//            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, sessionCookie);
//            addServicesServiceStub._getServiceClient().getOptions().setTimeOutInMilliSeconds(600000);
//        } catch (AxisFault axisFault) {
//            Assert.fail("Unexpected exception thrown");
//            axisFault.printStackTrace();
//        }
//        log.info("AddServicesServiceStub created");
//        return addServicesServiceStub;
//    }

    public static boolean isResourceExist(String sessionCookie, String resourcePath, String resourceName,
                                          ResourceAdminServiceClient resourceAdminServiceClient) throws Exception {
        boolean isResourceExist = false;
        CollectionContentBean collectionContentBean = new CollectionContentBean();
        collectionContentBean = resourceAdminServiceClient.getCollectionContent(resourcePath);
        if (collectionContentBean.getChildCount() > 0) {
            String[] childPath = collectionContentBean.getChildPaths();
            for (int i = 0; i <= childPath.length - 1; i++) {
                if (childPath[i].equalsIgnoreCase(resourcePath + "/" + resourceName)) {
                    isResourceExist = true;
                }
            }
        }
        return isResourceExist;
    }
}
