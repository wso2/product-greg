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

var validate = function () {
    var password = $('#value').val();
    var passwordVerify = $('#valueVerify').val();

    if (password != passwordVerify) {
        messages.alertError("Re-entered password does not match");
        return false;
    } else {
        var ajaxURL = caramel.context + '/pages/password?type=server';
        $.ajax({
            url: ajaxURL,
            type: 'GET',
            async: false,
            success: function (data) {
                messages.alertSuccess("Property Saved Successfully");
            },
            error: function (data) {
                messages.alertSuccess("Property Could Not Be Saved Successfully");
            }
        });
        return true;
    }
}

var populate = function (serverName) {
    $('#key').val(serverName);
    $('#key').focus();
}