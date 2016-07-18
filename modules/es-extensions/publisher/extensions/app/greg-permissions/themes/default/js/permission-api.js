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
var SELECT_CONTAINER = ".select-roles",
    SELECT_CONTAINER_CSS = "select-roles",
    authorizedRoles;

var showOtherRolePermissionsDiv = function () {
    var addId = "#step2";
    $(addId).removeClass("disabled-area");
    $(addId + " select").attr("disabled", false);

    $(addId).show();
    if ($(SELECT_CONTAINER + " option").length !== 0) {
        $("#addPermission").css("display", "initial");
    }
    else {
        $("#addPermission").hide();
    }
};

$(document).ready(function () {
    renderSelect2();

    if ($("#other-roles").prop("checked")) {
        showOtherRolePermissionsDiv();
    } else {
        $("#step2").hide();
    }

});

function renderSelect2() {
    $(SELECT_CONTAINER).select2({
        dropdownCssClass: SELECT_CONTAINER_CSS,
        containerCssClass: SELECT_CONTAINER_CSS
    });
}

$(function () {
    var invokePermissionListAPI = function () {
        var fromAssetId = store.publisher.assetId;
        var assetType = store.publisher.type;
        var url = caramel.context + "/apis/permissions?assetType=" + assetType + "&id=" + fromAssetId;
        $.ajax({
            url: url,
            async: false,
            type: "GET",
            success: function (data) {
                var roleNames = data.data.roleNames;
                authorizedRoles = data.data.authorizedRoles;
                for (var i in roleNames) {
                    if(roleNames.hasOwnProperty(i)){
                        var role = capitalizeRole(roleNames[i].toLowerCase());
                        addOptionsToSelect2(role);
                    }
                }
                renderSelect2();
                initSelect2Roles();
            }
        });

    };

    var isRoleAuthorized = function (role) {
        var status = false;
        for (var i in authorizedRoles) {
            if(authorizedRoles.hasOwnProperty(i)){
                if(authorizedRoles[i] == role){
                    status = true;
                }
            }
        }
        return status;
    };

    var invokePermissionAPI = function (data, role) {
        var url = caramel.context + "/apis/permissions";
        $.ajax({
            url: url,
            async: false,
            data: JSON.stringify(data),
            type: "POST",
            contentType: "application/json",
            success: function (data) {
                if (data.status) {
                    populateSuccessMessage("shared", role);
                } else {
                    populateErrorMessage("adding");
                }
                $("#new-version-loading-add").addClass("hide");
                $("#new-version-loading-save").addClass("hide");
            },
            error: function () {
                populateErrorMessage("adding");
                $("#new-version-loading-add").addClass("hide");
                $("#new-version-loading-save").addClass("hide");
            }
        });

    };

    var invokeRemovePermissionAPI = function (data, role) {
        var url = caramel.context + "/apis/permissions?assetType=" + data.assetType + "&assetId=" + data.assetId
            + "&pathWithVersion=" + data.pathWithVersion + "&roleToRemove=" + data.roleToRemove
            + "&permissionCheck=" + data.permissionCheck;

        $.ajax({
            url: url,
            async: false,
            type: "DELETE",
            success: function (data) {
                if (data.status) {
                    populateSuccessMessage("restricted", role);
                } else {
                    populateErrorMessage("removing");
                }
            },
            error: function () {
                populateErrorMessage("removing");
            }
        });
    };

    var invokeUpdatePermissionAPI = function (data, role) {
        var url = caramel.context + "/apis/permissions";
        $.ajax({
            url: url,
            async: false,
            data: JSON.stringify(data),
            type: "PUT",
            contentType: "application/json",
            success: function (data) {
                if (data.status) {
                    populateSuccessMessage("shared", role);
                } else {
                    populateErrorMessage("updating");
                }
                $("#new-version-loading-save").addClass("hide");
            },
            error: function () {
                populateErrorMessage("updating");
                $("#new-version-loading-save").addClass("hide");
            }
        });
    };

    var populateSuccessMessage = function (action, role) {
        $("#div-roles").load(location.href + " " + ".wr-permission");
        setTimeout(function () {
            if ($(".wr-permission").text().length <= 0) {
                $("#div-roles").html("<div class='clearfix wr-permission' align='center'>" +
                    "No permissions to show</div>");
            }
        }, 250);
        if(action == "restricted"){
            messages.alertSuccess("Resource successfully " + action + " from " + role);
        } else {
            messages.alertSuccess("Resource successfully " + action + " with " + role);
        }

        initDivPermissionLogic();
    };

    var populateErrorMessage = function (action) {
        messages.alertError("Error occurred while " + action + " permissions");
    };

    var renderRoles = function (role) {
        var roleLower = role.toLowerCase();
        var modifiedRole = "";
        switch (roleLower) {
            case "internal/everyone":
                modifiedRole = "All tenant users";
                break;
            case "system/wso2.anonymous.role":
                modifiedRole = "Public";
                break;
            default:
                if (roleLower.startsWith("internal/")) {
                    modifiedRole = capitalize(roleLower.split("/")[1]);
                    break;
                } else {
                    modifiedRole = capitalize(roleLower);
                    break;
                }
        }
        return modifiedRole;
    };

    var capitalize = function(role) {
        return role.substr(0, 1).toUpperCase() + role.substr(1);
    };

    var capitalizeRole = function(role) {
        if (role.indexOf("/") > -1) {
            return role.substr(0, role.indexOf("/")).toUpperCase() + role.substr(role.indexOf("/"));
        } else {
            return role;
        }
    };

    var addOptionsToSelect2 = function (role) {
        if ($(SELECT_CONTAINER + " option[value='" + role + "']").length <= 0) {
            $(SELECT_CONTAINER).append("<option value='" + role + "'>" + renderRoles(role) + "</option>");
        }
    };

    var initAddPermissionLogic = function () {
        var fromAssetId = store.publisher.assetId;
        var pathWithVersion = $("#pathWithVersion").val();
        var data = {};
        var assetType = store.publisher.type;
        $("#addPermission").on("click", function () {
            var roleToAuthorize = $(SELECT_CONTAINER).val();
            var formattedRoleName = $(SELECT_CONTAINER+" option:selected").text();
            if (roleToAuthorize != "0") {
                data.assetId = fromAssetId;
                data.assetType = assetType;
                data.actionToAuthorize = "2";
                data.roleToAuthorize = roleToAuthorize;
                data.pathWithVersion = pathWithVersion;
                data.permissionType = "1";
                data.permissionCheck = "other";
                if(!isRoleAuthorized(roleToAuthorize)){
                    $("#new-version-loading-add").removeClass("hide");
                    invokePermissionAPI(data, formattedRoleName);
                    $(SELECT_CONTAINER + " option[value='" + roleToAuthorize + "']").remove();
                    renderSelect2();
                } else {
                    messages.alertError("Unable to share resource to role. " +
                        "The role has authorize permission to this resource");
                }
            } else {
                messages.alertError("Please select a role to add");
            }
        });
    };

    var initUpdatePermissionLogic = function (role) {
        var data = {};
        var fromAssetId = store.publisher.assetId;
        var pathWithVersion = $("#pathWithVersion").val();
        var internalId = "#public-internal";
        var roleName;
        data.assetType = store.publisher.type;
        data.assetId = fromAssetId;
        data.pathWithVersion = pathWithVersion;
        if ($(internalId).prop("checked") || $("#public-everyone").prop("checked")) {
            data.actionToAuthorize = "2";
            data.permissionType = "1";
            if ($(internalId).prop("checked")) {
                data.roleToAuthorize = "INTERNAL/everyone";
                data.roleToDeny = "SYSTEM/wso2.anonymous.role";
                data.permissionTypeDeny = "2";
                data.permissionCheck = "internal";
                roleName = "All tenant users";
            } else {
                data.roleToAuthorize = "SYSTEM/wso2.anonymous.role";
                data.roleToDeny = "";
                data.permissionCheck = "public";
                roleName = "Public";
            }
            if(!isRoleAuthorized(data.roleToAuthorize)){
                $("#new-version-loading-save").removeClass("hide");
                invokePermissionAPI(data, roleName);
                setTimeout(function () {
                    initSelect2Roles();
                    if ($("#public-internal").prop("checked")) {
                        addOptionsToSelect2(data.roleToDeny);
                        renderSelect2();
                    }
                }, 500);
            } else {
                messages.alertError("Unable to share resource to role. " +
                    "The role has authorize permission to this resource");
            }
        } else {
            if ($(".wr-permission").text() == "No permissions to show") {
                messages.alertError("No permissions to share resource");
            } else {
                data.permissionObject = buildPermissionArray();
                data.permissionCheck = "other";
                $("#new-version-loading-save").removeClass("hide");
                invokeUpdatePermissionAPI(data, role);
            }
        }
    };

    var initDivPermissionLogic = function () {
        var REMOVE_PERMISSION_BUTTON_ID = ".wr-permission-operations [data-operation=delete]";
        var data = {};
        setTimeout(function () {
            $(REMOVE_PERMISSION_BUTTON_ID).on("click", function () {
                var roleToRemove = $(this).data("role-name");
                var formattedRoleName = $(this).data("formatted-name");
                var fromAssetId = store.publisher.assetId;
                var pathWithVersion = $("#pathWithVersion").val();
                var assetType = store.publisher.type;
                data.assetId = fromAssetId;
                data.assetType = assetType;
                data.pathWithVersion = pathWithVersion;
                data.roleToRemove = roleToRemove;
                data.permissionCheck = "other";
                $(this).attr("disabled", "disabled");
                invokeRemovePermissionAPI(data, formattedRoleName);
                addOptionsToSelect2(roleToRemove);
                renderSelect2();
            });

            $("select[name='roleActionToAuthorize']").change(function () {
                var roleName = $(this).data("formatted-name");
                initUpdatePermissionLogic(roleName);
            });

        }, 1000);
    };

    var initSelect2Roles = function () {
        var rolesDivId = "#div-roles";
        $(rolesDivId +" > div").each(function () {
            var roleName = $("input[id=roleName]", this).attr("value");
            $(SELECT_CONTAINER + " option[value='" + roleName + "']").remove();
        });
    };

    var init = function () {
        $("input:radio[name='roles']").change(function () {
            if ($(this).attr("id") == "other-roles") {
                showOtherRolePermissionsDiv();
            } else {
                $("#step2").hide();
                initUpdatePermissionLogic();
            }
        });
        if ($(".wr-permission").text().length <= 0) {
            $("#div-roles").html("<div class='clearfix wr-permission' align='center'>No permissions to show</div>");
        }

        invokePermissionListAPI();
        initAddPermissionLogic();
        initDivPermissionLogic();
    };

    /**
     * Builds the permission string which needs to be sent to the backend
     */
    var buildPermissionArray = function () {
        var permissionObject = {};
        var rolesDivId = "#div-roles";
        $(rolesDivId +" > div").each(function () {
            var roleName = $("input[id=roleName]", this).attr("value");
            permissionObject[roleName] = $("[data-role-id='" + roleName + "\^action']").val();
        });
        return permissionObject;
    };

    init();

});