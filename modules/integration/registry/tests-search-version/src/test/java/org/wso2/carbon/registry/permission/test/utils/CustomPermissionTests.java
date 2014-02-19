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
import org.wso2.carbon.automation.api.clients.governance.LifeCycleManagementClient;
import org.wso2.carbon.automation.api.clients.registry.ActivityAdminServiceClient;
import org.wso2.carbon.automation.api.clients.registry.PropertiesAdminServiceClient;
import org.wso2.carbon.automation.api.clients.registry.RelationAdminServiceClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.core.utils.fileutils.FileManager;
import org.wso2.carbon.registry.activities.stub.RegistryExceptionException;
import org.wso2.carbon.registry.activities.stub.beans.xsd.ActivityBean;
import org.wso2.carbon.registry.properties.stub.PropertiesAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.relations.stub.AddAssociationRegistryExceptionException;
import org.wso2.carbon.registry.relations.stub.beans.xsd.AssociationTreeBean;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceResourceServiceExceptionException;

import javax.activation.DataHandler;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Date;

public class CustomPermissionTests {

    private static final String LIFECYCLE_NAME = "CheckListLC";

    public static boolean canReadResource(ManageEnvironment manageEnvironment, String resourcePath)
            throws RemoteException, ResourceAdminServiceExceptionException {
        ResourceAdminServiceClient resourceClient =
                new ResourceAdminServiceClient(manageEnvironment.getGreg().getBackEndUrl(),
                                               manageEnvironment.getGreg().getSessionCookie());

        return resourceClient.getResource(resourcePath) != null;
    }

    public static boolean canAddAssociation(ManageEnvironment manageEnvironment)
            throws RemoteException, LoginAuthenticationExceptionException, MalformedURLException,
                   ResourceAdminServiceExceptionException,
                   AddAssociationRegistryExceptionException {
        ManageEnvironment adminEnvironment = new EnvironmentBuilder().greg(0).build();
        ResourceAdminServiceClient resourceAdminServiceClient =
                new ResourceAdminServiceClient(adminEnvironment.getGreg().getBackEndUrl(),
                                               adminEnvironment.getGreg().getSessionCookie());

        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator
                              + "GREG" + File.separator + "resource.txt";

        DataHandler dataHandler = new DataHandler(new URL("file:///" + resourcePath));
        String path1 = "/_system/config/resA";
        String path2 = "/_system/config/resB";
        resourceAdminServiceClient.addResource(path1, "text/plain", "", dataHandler);
        resourceAdminServiceClient.addResource(path2, "text/plain", "", dataHandler);
        RelationAdminServiceClient relationAdminServiceClient =
                new RelationAdminServiceClient(manageEnvironment.getGreg().getBackEndUrl(),
                                               manageEnvironment.getGreg().getSessionCookie());
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

    public static boolean canAddLifecycles(ManageEnvironment manageEnvironment) throws IOException {
        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
                              File.separator + "GREG" + File.separator + "lifecycle" + File.separator +
                              "CheckItemTickedValidatorLifeCycle.xml";
        String lifeCycleContent = FileManager.readFile(resourcePath);
        LifeCycleManagementClient lifeCycleManagementClient = new LifeCycleManagementClient(manageEnvironment.getGreg
                ().getBackEndUrl(), manageEnvironment.getGreg().getSessionCookie());
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

    public static boolean canSearchActivities(ManageEnvironment manageEnvironment, String username)
            throws RemoteException, LoginAuthenticationExceptionException, MalformedURLException,
                   ResourceAdminServiceExceptionException, RegistryExceptionException,
                   PropertiesAdminServiceRegistryExceptionException,
                   ResourceAdminServiceResourceServiceExceptionException, InterruptedException {

        ActivityAdminServiceClient activityAdminServiceClient =
                new ActivityAdminServiceClient(manageEnvironment.getGreg().getBackEndUrl(),
                                               manageEnvironment.getGreg().getSessionCookie());

        ManageEnvironment adminEnvironment = new EnvironmentBuilder().greg(0).build();

        ResourceAdminServiceClient resourceAdminServiceClient =
                new ResourceAdminServiceClient(adminEnvironment.getGreg().getBackEndUrl(),
                                               adminEnvironment.getGreg().getSessionCookie());

        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator
                              + "GREG" + File.separator + "resource.txt";

        DataHandler dataHandler = new DataHandler(new URL("file:///" + resourcePath));
        String resPath = "/_system/config/resAct";
        resourceAdminServiceClient.addResource(resPath, "text/plain", "", dataHandler);
        PropertiesAdminServiceClient propertiesAdminServiceClient = new PropertiesAdminServiceClient
                (manageEnvironment.getGreg().getBackEndUrl(), manageEnvironment.getGreg().getSessionCookie());
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
                    bean = activityAdminServiceClient.getActivities(manageEnvironment.getGreg().getSessionCookie
                            (), username, resPath, "1/1/1980", "1/1/2100", "", 0);

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
