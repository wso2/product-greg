/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
var gregPermissionUtil = {};
(function (gregPermissionUtil) {
    var gregAPI = require('/modules/greg-publisher-api.js').gregAPI;

    gregPermissionUtil.permissions = {};

    gregPermissionUtil.permissions.list = function (am, assetId) {
        return gregAPI.permissions.list(am, assetId);
    };

    gregPermissionUtil.permissions.add = function (am, params) {
        var actionToAuthorize = params.actionToAuthorize;
        var roleToAuthorize = params.roleToAuthorize;
        var pathWithVersion = params.pathWithVersion;
        var permissionType = params.permissionType;
        var permissionCheck = params.permissionCheck;

        var results = gregAPI.permissions.add(am, pathWithVersion,
            roleToAuthorize, actionToAuthorize, permissionType, permissionCheck);

        if (params.roleToDeny) {
            results = gregAPI.permissions.add(am, pathWithVersion,
                params.roleToDeny, actionToAuthorize, params.permissionTypeDeny, permissionCheck);
        }

        return results;
    };

    gregPermissionUtil.permissions.modify = function (am, params) {
        var permissionObject = params.permissionObject;
        var pathWithVersion = params.pathWithVersion;
        var permissionCheck = params.permissionCheck;
        var newPermissionsString = "";
        var results;
        var readAllow = "ra";
        var writeAllow = "wa";
        var deleteAllow = "da";
        var writeDeny = "wd";
        var deleteDeny = "dd";

        for (var role in permissionObject) {
            if (permissionObject.hasOwnProperty(role)) {
                newPermissionsString = newPermissionsString + "|";
                var roleId = role;
                var rolePermissionType = permissionObject[role];
                var actions = ":";
                if (rolePermissionType == readAllow) {
                    actions = actions + readAllow + "^true:" + writeDeny + "^true:" + deleteDeny + "^true";
                } else if (rolePermissionType == writeAllow) {
                    actions = actions + readAllow + "^true:" + writeAllow + "^true:" + deleteDeny + "^true";
                } else if (rolePermissionType == deleteAllow) {
                    actions = actions + readAllow + "^true:" + writeAllow + "^true:" + deleteAllow + "^true";
                }
                newPermissionsString = newPermissionsString + roleId + actions;
            }
        }
        results = gregAPI.permissions.modify(am, pathWithVersion, newPermissionsString, permissionCheck);

        return results;
    };

    gregPermissionUtil.permissions.remove = function (am, params) {
        var permissionRemoveString = ":rd^true:wd^true:dd^true";
        var pathWithVersion = params.pathWithVersion;
        var roleToRemove = params.roleToRemove;
        var permissionCheck = params.permissionCheck;
        var permissionsString = "|" + roleToRemove + permissionRemoveString;
        return gregAPI.permissions.modify(am, pathWithVersion, permissionsString, permissionCheck);
    };

}(gregPermissionUtil));