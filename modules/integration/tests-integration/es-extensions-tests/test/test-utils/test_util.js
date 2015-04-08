/*
 * Copyright (c) 2015 WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
var conf = require("conf.json");
var username = conf.authConfiguration.username;
var password = conf.authConfiguration.password;
var server_url = conf.StoreConfigurations.url;

/**
 *
 * @return {{Cookie: string}}
 */
var obtainAuthorizedHeaderForAPICall = function () {
    var authenticate = post(server_url + '/authenticate', {"password": password, "username": username }, {}, 'json');
    var header = {'Cookie': "JSESSIONID=" + authenticate.data.data.sessionId + ";"};
    return header
};
/**
 * This function will send delete request for given asset id
 * @param id The uuid of asset to be deleted
 */
var deleteAssetWithID = function (id,type) {
    var url = server_url + '/assets/' + id + '?type=' + type;
    var header = obtainAuthorizedHeaderForAPICall();
    var response;
    try {
        response = del(url, {}, header, 'json');
    } catch (e) {
        log.debug(e);
    } finally {
        logoutAuthorizedUser(header);
        expect(response.data.data).toEqual('Asset Deleted Successfully');
    }
};
/**
 * The function to send logout request to publisher API
 * @param header
 */
var logoutAuthorizedUser = function (header) {
    post(server_url + '/logout', {}, header, 'json');
};

