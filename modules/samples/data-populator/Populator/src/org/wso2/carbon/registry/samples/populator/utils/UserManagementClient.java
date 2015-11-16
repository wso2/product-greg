/*
 *  Copyright (c) 2015 WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.registry.samples.populator.utils;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.Stub;
import org.wso2.carbon.identity.user.profile.stub.UserProfileMgtServiceStub;
import org.wso2.carbon.identity.user.profile.stub.types.UserProfileDTO;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceStub;
import org.wso2.carbon.tenant.mgt.stub.TenantMgtAdminServiceStub;
import org.wso2.carbon.tenant.mgt.stub.beans.xsd.TenantInfoBean;
import org.wso2.carbon.user.mgt.stub.UserAdminStub;
import org.wso2.carbon.user.mgt.stub.types.carbon.ClaimValue;
import org.wso2.carbon.user.mgt.ui.Util;

import java.util.Calendar;


/**
 * User management admin service client
 */
public class UserManagementClient {

    private UserProfileMgtServiceStub profileMgtServiceStub;
    private UserAdminStub userAdminStub;
    private ResourceAdminServiceStub resourceAdminStub;
    private TenantMgtAdminServiceStub tenantAdminStub;

    public UserManagementClient(String cookie, String backendServerURL, ConfigurationContext configContext)
            throws RegistryException {
        try {
            String epr = backendServerURL + "UserProfileMgtService";
            profileMgtServiceStub = new UserProfileMgtServiceStub(configContext, epr);

            setCookie(profileMgtServiceStub, cookie);
        } catch (AxisFault e) {
            String msg = "Failed to initiate user profile management service client.";
            throw new RegistryException(msg, e);
        }

        try {
            String epr = backendServerURL + "UserAdmin";
            userAdminStub = new UserAdminStub(configContext, epr);

            setCookie(userAdminStub, cookie);
        } catch (AxisFault e) {
            String msg = "Failed to initiate user admin service client.";
            throw new RegistryException(msg, e);
        }

        try {
            String epr = backendServerURL + "ResourceAdminService";
            resourceAdminStub = new ResourceAdminServiceStub(configContext, epr);

            setCookie(resourceAdminStub, cookie);
        } catch (AxisFault e) {
            String msg = "Failed to initiate resource admin service client.";
            throw new RegistryException(msg, e);
        }

        try {
            String epr = backendServerURL + "TenantMgtAdminService";
            tenantAdminStub = new TenantMgtAdminServiceStub(configContext, epr);

            setCookie(tenantAdminStub, cookie);
        } catch (AxisFault e) {
            String msg = "Failed to initiate tenant management admin service client.";
            throw new RegistryException(msg, e);
        }
    }

    /**
     * Ger user profile
     *
     * @param username
     * @param profile
     * @return
     * @throws Exception
     */
    public UserProfileDTO getUserProfile(String username, String profile) throws Exception {
        return profileMgtServiceStub.getUserProfile(username, profile);
    }

    /**
     * Save user profile
     *
     * @param username
     * @param profile
     * @throws Exception
     */
    public void setUserProfile(String username, UserProfileDTO profile) throws Exception {
        profileMgtServiceStub.setUserProfile(username, profile);
    }

    /**
     * Adding a user role
     *
     * @param roleName
     * @param userList
     * @param permissions
     * @throws Exception
     */
    public void addRole(String roleName, String[] userList, String[] permissions) throws Exception {
        userAdminStub.addRole(roleName, userList, permissions, false);
    }

    /**
     * Adding a user
     *
     * @param userName
     * @param password
     * @param roles
     * @param claims
     * @param profileName
     * @throws Exception
     */
    public void addUser(String userName, String password, String[] roles, ClaimValue[] claims, String profileName) throws Exception {
        userAdminStub.addUser(userName, password, roles, claims, profileName);
    }

    /**
     * Set user role UI permission
     *
     * @param roleName
     * @param permissions
     * @throws Exception
     */
    public void setRoleUIPermission(String roleName, String[] permissions) throws Exception {
        userAdminStub.setRoleUIPermission(roleName, permissions);
    }

    /**
     * Set user role resource permission
     *
     * @param path
     * @param roleName
     * @param permissions
     * @throws Exception
     */
    public void setRoleResourcePermission(String path, String roleName, String[] permissions) throws Exception {
        String permissionString = "ra^false:rd^false:wa^false:wd^false:da^false:dd^false:aa^false:ad^false";
        for (String permission : permissions) {
            permissionString = permissionString.replace(permission + "^false", permission + "^true");
        }
        resourceAdminStub.addRolePermission(path, roleName, "2", "1");
        resourceAdminStub.changeRolePermissions(path, roleName + ":" + permissionString);
    }

    /**
     * Adding a new tenant
     *
     * @param adminUsername
     * @param adminPassword
     * @param adminEmail
     * @param firstName
     * @param lastName
     * @param tenantDomain
     * @throws Exception
     */
    public void addTenant(String adminUsername, String adminPassword, String adminEmail, String firstName,
            String lastName, String tenantDomain) throws Exception {
        TenantInfoBean tenantInfoBean = new TenantInfoBean();
        tenantInfoBean.setAdmin(adminUsername);
        tenantInfoBean.setAdminPassword(adminPassword);
        tenantInfoBean.setEmail(adminEmail);
        tenantInfoBean.setFirstname(firstName);
        tenantInfoBean.setLastname(lastName);
        tenantInfoBean.setTenantDomain(tenantDomain);
        tenantInfoBean.setUsagePlan("Demo");
        tenantInfoBean.setCreatedDate(Calendar.getInstance());
        tenantAdminStub.addTenant(tenantInfoBean);
    }

    /**
     * deleting a user
     *
     * @param userName
     * @throws Exception
     */
    public void deleteUser(String userName) throws Exception {
        userAdminStub.deleteUser(userName);
    }

    private static void setCookie(Stub stub, String cookie) {
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        if (cookie != null) {
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        }
    }
}
