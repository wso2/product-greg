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

$(function () {

    var id = function(name) {
        return '#' + name;
    };

    var partial = function(name) {
        return '/extensions/app/greg-publisher-defaults/themes/' + caramel.themer + '/partials/' + name + '.hbs';
    };

    var renderPartial = function(partialKey,data, fn) {
        fn = fn || function() {};
        var partialName = partialKey;
        if (!partialName) {
            throw 'A template name has not been specified for template key ' + partialKey;
        }
        var obj = {};
        obj[partialName] = partial(partialName);
        caramel.partials(obj, function() {
            var template = Handlebars.partials[partialName](data);
            fn(template);
        });
    };

    /**
     * This is used to add a note.
     */
    $('#add-note').on('click', function () {
        var data = {};
        data.overview_resourcepath = store.publisher.assetPath;
        data.overview_comment = $('#add-note-content').val();
        data.overview_visibility = "public";
        data.overview_status = "Open";

        $.ajax({
            url: caramel.context + "/apis/assets?type=comments",
            type: 'POST',
            data: JSON.stringify(data),
            contentType: "application/json",
            dataType: 'json',
            success: function (response) {
                var input = {};
                input.comments = [];
                input.comments.push(response.data);
                    renderPartial('notes-comment',{},function(result){
                        renderPartial('notes-comment-container',input,function(result){
                            $('#collapseNotes .wr-panel-notes').append(result);
                        });
                    });
                    $('#add-note-content').val('');
            },
            error: function () {
                console.log("Error adding a note.");
            }
        });
    });

    /**
     * This function is used to get reply container Id.
     *
     * @param id    id of the root note.
     * @returns {string}
     */
    var replyContainerId = function (id) {
        return '#' + id + 'reply';
    };

    /**
     * This function is used to get reply notes.
     */
    $('.reply-note').on('click', function () {
        var id = $(this).data('id');
        var noteContainer = this;
        var data = {};
        data.overview_resourcepath = this.dataset.path;
        data.overview_comment = $(replyContainerId(id)).val();
        data.overview_replypath = this.dataset.path;
        data.overview_visibility = "public";
        data.overview_status = "Open";

        $.ajax({
            url: caramel.context + "/apis/assets?type=comments",
            type: 'POST',
            data: JSON.stringify(data),
            contentType: "application/json",
            dataType: 'json',
            success: function (response) {
                renderPartial('notes-comment',response.data,function(result){
                    $(noteContainer).closest('div.well').siblings('.wr-panel-sub-note').append(result);
                });
                $(replyContainerId(id)).val('');
            },
            error: function () {
                console.log("Error while adding reply");
            }
        });
    });

    /**
     * This function is used to get reply notes.
     */
    $('.wr-panel-note > .wr-panel-msg').on('click', function() {

        var path = $(this).closest('.wr-panel-note').data('path');
        var id = $(this).closest('.wr-panel-note').attr('href');

        $.ajax({
            url: caramel.context + '/apis/assets?type=comments&q="overview_replypath":"' + path + '"',
            type: 'GET',
            success: function (response) {
                $(id + "> .wr-panel-sub-note").html("");
                $.each(response.data, function(key, value){
                    renderPartial('notes-comment',value,function(result){
                        $(id + "> .wr-panel-sub-note").append(result);
                    });
                });
            },
            error: function () {
                console.log("Error loading note replies.");
            }
        });
    });
});
