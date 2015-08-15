/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

$(function () {

    var tags = [];
    var formattedTags = [];

    // Get Tags related to an asset.
    $.ajax({
        url: caramel.context + '/apis/asset/' + store.publisher.assetId + '/tags?type=' + store.publisher.type,
        type: 'GET',
        async: false,
        success: function (response) {
            tags = response.data;
        },
        error: function () {
            console.log("Error getting tags.");
        }
    });

    for (var i = 0; i < tags.length; i++) {
        var formattedTag = {};
        formattedTag.id = i;
        formattedTag.text = tags[i];
        formattedTags.push(formattedTag);
    }

    $('#select-tags').select2({
        tags: true,
        placeholder: 'NO TAGS FOUND',
        data: tags,
        multiple: true,
        cache: true
    }).on("select2:select", function (e) {
        var data = {};
        data.tags = e.params.data.text;
        $.ajax({
            url: caramel.context + '/apis/asset/' + store.publisher.assetId + '/add-tags?type=' + store.publisher.type,
            type: 'POST',
            async: false,
            data: JSON.stringify(data),
            contentType: 'application/json',
            error: function () {
                console.log("Error adding tags.");
            }
        });
    }).on("select2:unselect", function (e) {
        var data = {};
        data.tags = e.params.data.text;
        $.ajax({
            url: caramel.context + '/apis/asset/' + store.publisher.assetId + '/remove-tags?type=' + store.publisher.type,
            type: 'DELETE',
            contentType: 'application/json',
            data: JSON.stringify(data),
            error: function () {
                console.log("Error removing tags.");
            }
        });
    }).select2("val", tags);
});