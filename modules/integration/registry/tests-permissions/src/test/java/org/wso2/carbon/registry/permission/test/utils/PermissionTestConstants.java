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

public class PermissionTestConstants {
    public static final String NON_ADMIN_ROLE = "testRole";
    public static final String NON_ADMIN_ROLE_2 = "testRole2";
    public static final String EVERYONE_ROLE = "internal/everyone";

    public static final String READ_ACTION = "2";
    public static final String WRITE_ACTION = "3";
    public static final String DELETE_ACTION = "4";
    public static final String AUTHORIZE_ACTION = "5";

    public static final String PERMISSION_ENABLED = "1";
    public static final String PERMISSION_DISABLED = "0";

    public static final String WEB_APP_RESOURCE_URL = "/registry/resource"; // used to test anonymous access

    public static final String[] NON_ADMIN_PERMISSION = {"/permission/admin/login",
                                                         "/permission/admin/manage/resources",
                                                         "/permission/admin/manage/extensions",
                                                         "/permission/admin/manage/resources/associations",
                                                         "/permission/admin/manage/resources/browse",
                                                         "/permission/admin/manage/resources/community-features",
                                                         "/permission/admin/manage/resources/govern",
                                                         "/permission/admin/manage/resources/govern/api",
                                                         "/permission/admin/manage/resources/govern/api/add",
                                                         "/permission/admin/manage/resources/govern/api/list",
                                                         "/permission/admin/manage/resources/govern/generic",
                                                         "/permission/admin/manage/resources/govern/generic/add",
                                                         "/permission/admin/manage/resources/govern/generic/list",
                                                         "/permission/admin/manage/resources/govern/impactanalysis",
                                                         "/permission/admin/manage/resources/govern/lifecycles",
                                                         "/permission/admin/manage/resources/govern/lifecyclestagemonitor",
                                                         "/permission/admin/manage/resources/govern/metadata",
                                                         "/permission/admin/manage/resources/govern/generic/add",
                                                         "/permission/admin/manage/resources/govern/service/add",
                                                         "/permission/admin/manage/resources/govern/wsdl/add",
                                                         "/permission/admin/manage/resources/govern/schema/add",
                                                         "/permission/admin/manage/resources/govern/policy/add",
                                                         "/permission/admin/manage/resources/govern/generic/list",
                                                         "/permission/admin/manage/resources/govern/service/list",
                                                         "/permission/admin/manage/resources/govern/wsdl/list",
                                                         "/permission/admin/manage/resources/govern/schema/list",
                                                         "/permission/admin/manage/resources/govern/policy/list",
                                                         "/permission/admin/manage/resources/govern/resourceimpact",
                                                         "/permission/admin/manage/resources/govern/uri",
                                                         "/permission/admin/manage/resources/govern/uri/add",
                                                         "/permission/admin/manage/resources/govern/uri/list",
                                                         "/permission/admin/manage/resources/notifications",
                                                         "/permission/admin/manage/resources/ws-api"};

    public static String[] NON_ADMIN_ROLE_2_USERS = {"testuser3"};

}
