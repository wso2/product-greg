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
    var TAG_SELECT_BOX = '#select-tags';
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
            //console.log("Error getting tags.");
        }
    });

    for (var i = 0; i < tags.length; i++) {
        var formattedTag = {};
        formattedTag.id = i;
        formattedTag.text = tags[i];
        formattedTags.push(formattedTag);
    }

    $(TAG_SELECT_BOX).select2({
        tags: true,
        data: tags,
        multiple: true,
        cache: true,
        createTag:function(term){
            //Prevent tags with spaces by replacing it with a dash (-)
            var modifiedTerm = term.term.trim();
            var formatted = modifiedTerm.split(' ').join('-');
            return {
                id:formatted,
                text:formatted
            };
        }
    });
    
    //Search from already available tags and suggest to the user when adding tags to assets.
    $(TAG_SELECT_BOX).select2({
        tags: true,
        ajax: {
            url: caramel.context + '/apis/tags?type=' + store.publisher.type,
            dataType: "json",
            delay: 250,
            data: function (params) {
                var query = '"name":"' + params.term + '"';
                return {
                    q: query
                };
            },
            processResults: function (data) {
                var results = [];
                for (var i = 0; i < data.length; i++) {
                    results.push({
                        id: data[i].name,
                        text: data[i].name
                    });
                }
                return {
                    results: results
                };
            },
            cache: true
        },
        minimumInputLength: 2,
        templateSelection: function (data) {
            return data.text;
        },
        createTag:function(term){
            //Prevent tags with spaces by replacing it with a dash (-)
            var modifiedTerm = term.term.trim();
            var formatted = modifiedTerm.split(' ').join('-');
            return {
                id:formatted,
                text:formatted
            };
        }
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
                //console.log("Error adding tags.");
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
                //console.log("Error removing tags.");
            }
        });
    }).select2("val", tags);
});