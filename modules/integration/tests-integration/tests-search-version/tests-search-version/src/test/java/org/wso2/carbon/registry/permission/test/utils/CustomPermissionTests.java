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

package org.wso2.carbon.registry.permission.test.utils;

import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.automation.test.utils.common.FileManager;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.integration.common.utils.exceptions.AutomationUtilException;
import org.wso2.carbon.registry.activities.stub.RegistryExceptionException;
import org.wso2.carbon.registry.activities.stub.beans.xsd.ActivityBean;
import org.wso2.carbon.registry.properties.stub.PropertiesAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.relations.stub.AddAssociationRegistryExceptionException;
import org.wso2.carbon.registry.relations.stub.beans.xsd.AssociationTreeBean;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceResourceServiceExceptionException;
import org.wso2.greg.integration.common.clients.*;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.xml.sax.SAXException;

import javax.activation.DataHandler;
import javax.xml.stream.XMLStreamException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Calendar;

public class CustomPermissionTests extends GREGIntegrationBaseTest {

    private static final String LIFECYCLE_NAME = "CheckListLC";



    public static boolean canReadResource(AutomationContext automationContext, String resourcePath)
            throws IOException, ResourceAdminServiceExceptionException, XPathExpressionException,
            URISyntaxException, SAXException, XMLStreamException, LoginAuthenticationExceptionException,
            AutomationUtilException {

        LoginLogoutClient loginLogoutClient = new LoginLogoutClient(automationContext);
        ResourceAdminServiceClient resourceClient =
                new ResourceAdminServiceClient(automationContext.getContextUrls().getBackEndUrl(),
                        loginLogoutClient.login());

        return resourceClient.getResource(resourcePath) != null;
    }

    public static boolean canAddAssociation(AutomationContext automationContext)
            throws IOException, LoginAuthenticationExceptionException,
            ResourceAdminServiceExceptionException,
            AddAssociationRegistryExceptionException, XPathExpressionException,
            URISyntaxException, SAXException, XMLStreamException, AutomationUtilException {

        LoginLogoutClient loginLogoutClient = new LoginLogoutClient(automationContext);
        String sessionCookie = loginLogoutClient.login();
        ResourceAdminServiceClient resourceAdminServiceClient =
                new ResourceAdminServiceClient( automationContext.getContextUrls().getBackEndUrl(),
                                               sessionCookie);

        String resourcePath = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator
                              + "GREG" + File.separator + "resource.txt";

        DataHandler dataHandler = new DataHandler(new URL("file:///" + resourcePath));
        String path1 = "/_system/config/resA";
        String path2 = "/_system/config/resB";
        resourceAdminServiceClient.addResource(path1, "text/plain", "", dataHandler);
        resourceAdminServiceClient.addResource(path2, "text/plain", "", dataHandler);
        RelationAdminServiceClient relationAdminServiceClient =
                new RelationAdminServiceClient(automationContext.getContextUrls().getBackEndUrl(),
                                               sessionCookie);
        try {
            relationAdminServiceClient.addAssociation(path1, "usedBy", path2, "add");
            AssociationTreeBean associationTreeBean =
                    relationAdminServiceClient.getAssociationTree(path1, "usedBy");
            String resource = associationTreeBean.getAssociationTree();
            boolean result = resource.contains(path2);
            resourceAdminServiceClient.deleteResource(path1);
            resourceAdminServiceClient.deleteResource(path2);
            return result;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean canAddLifecycles(AutomationContext automationContext) throws IOException,
            URISyntaxException, SAXException, XPathExpressionException,
            LoginAuthenticationExceptionException, XMLStreamException, AutomationUtilException {

        String resourcePath = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" +
                              File.separator + "GREG" + File.separator + "lifecycle" + File.separator +
                              "CheckItemTickedValidatorLifeCycle.xml";

        String lifeCycleContent = FileManager.readFile(resourcePath);

        LoginLogoutClient loginLogoutClient = new LoginLogoutClient(automationContext);
        String sessionCookie = loginLogoutClient.login();

        LifeCycleManagementClient lifeCycleManagementClient = new LifeCycleManagementClient(
                automationContext.getContextUrls().getBackEndUrl(),
                sessionCookie);
        boolean lcStatus = false;

        try {
            lifeCycleManagementClient.addLifeCycle(lifeCycleContent);
            String[] lifeCycles = lifeCycleManagementClient.getLifecycleList();


            for (String lc : lifeCycles) {

                if (lc.equalsIgnoreCase(LIFECYCLE_NAME)) {
                    lcStatus = true;
                }
            }
            if (lcStatus) {
                lifeCycleManagementClient.deleteLifeCycle(LIFECYCLE_NAME);
            }
            return lcStatus;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean canSearchActivities(AutomationContext automationContext, String username)
            throws IOException, LoginAuthenticationExceptionException,
            ResourceAdminServiceExceptionException, RegistryExceptionException,
            PropertiesAdminServiceRegistryExceptionException,
            ResourceAdminServiceResourceServiceExceptionException, InterruptedException,
            URISyntaxException, SAXException, XMLStreamException, XPathExpressionException, AutomationUtilException{

        LoginLogoutClient loginLogoutClient = new LoginLogoutClient(automationContext);
        String sessionCookie = loginLogoutClient.login();
        String backEndUrl = automationContext.getContextUrls().getBackEndUrl();

        ActivityAdminServiceClient activityAdminServiceClient =
                new ActivityAdminServiceClient(backEndUrl, sessionCookie);

        ResourceAdminServiceClient resourceAdminServiceClient =
                new ResourceAdminServiceClient( backEndUrl, sessionCookie);

        String resourcePath = FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator
                              + "GREG" + File.separator + "resource.txt";

        DataHandler dataHandler = new DataHandler(new URL("file:///" + resourcePath));
        String resPath = "/_system/config/resAct";
        resourceAdminServiceClient.addResource(resPath, "text/plain", "", dataHandler);
        PropertiesAdminServiceClient propertiesAdminServiceClient = new PropertiesAdminServiceClient
                (backEndUrl, sessionCookie);
        resourceAdminServiceClient.addResourcePermission(resPath, PermissionTestConstants.EVERYONE_ROLE,
                PermissionTestConstants.WRITE_ACTION, PermissionTestConstants.PERMISSION_ENABLED);
        propertiesAdminServiceClient.setProperty(resPath, "Test", "Test");
        Thread.sleep(5000);
        boolean result = false;
        try {
            Calendar startTime = Calendar.getInstance();
            ActivityBean bean = null;
            while (((Calendar.getInstance().getTimeInMillis() - startTime.getTimeInMillis())) < 120000) {
                try {
                    bean = activityAdminServiceClient.getActivities(sessionCookie, username, resPath,
                            "1/1/1980", "1/1/2100", "", 0);

                    if (bean != null) {
                        if (bean.getActivity() != null) {
                            result = true;
                            break;
                        }
                    }
                } catch (Exception e) {
                    return false;
                }
            }


        } finally {
            resourceAdminServiceClient.deleteResource(resPath);
        }
        return result;
    }
}
