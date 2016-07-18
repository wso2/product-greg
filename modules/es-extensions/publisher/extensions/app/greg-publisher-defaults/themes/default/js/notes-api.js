/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

    var id = function (name) {
        return '#' + name;
    };

    var partial = function (name) {
        return '/extensions/app/greg-publisher-defaults/themes/' + caramel.themer + '/partials/' + name + '.hbs';
    };

    var renderPartial = function (partialKey, data, fn) {
        fn = fn || function () {
        };
        var partialName = partialKey;
        if (!partialName) {
            throw 'A template name has not been specified for template key ' + partialKey;
        }
        var obj = {};
        obj[partialName] = partial(partialName);
        caramel.partials(obj, function () {
            Handlebars.registerHelper('if_equal', function (lvalue, rvalue, options) {
                if (arguments.length < 3)
                    throw new Error("Handlebars Helper equal needs 2 parameters");
                if (lvalue != rvalue) {
                    return options.inverse(this);
                } else {
                    return options.fn(this);
                }
            });
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
        data.overview_note = $('#add-note-content').val();
        data.overview_visibility = "public";
        data.overview_status = "Open";
        if(!data.overview_note){
            $('#notes-error-add').show();
            return;
        }

        $.ajax({
            url: caramel.context + "/apis/assets?type=note",
            type: 'POST',
            data: JSON.stringify(data),
            contentType: "application/json",
            dataType: 'json',
            success: function (response) {
                var input = {};
                input.notes = [];
                input.notes.push(response.data);
                renderPartial('notes-note', {}, function (result) {
                    renderPartial('notes-note-container', input, function (result) {
                        $('#collapseNotes .wr-panel-notes').append(result);
                    });
                });
                $('#add-note-content').val('');
                messages.alertSuccess("Note added successfully");
                setTimeout(function () {
                    location.reload(true);
                }, 3000);
            },
            error: function () {
                //console.log("Error adding a note.");
            }
        });
    });

    /**
     * This is used to delete a note.
     */
    $(document).on('click', '.delete-note', function () {
        var id = $(this).attr('data-id');
        $.ajax({
            url: caramel.context + "/apis/assets/".concat(id).concat("?type=note"),
            type: 'DELETE',
            contentType: "application/json",
            dataType: 'json',
            success: function (response) {
                $('#add-note-content').val('');
                messages.alertSuccess("Note deleted successfully");
                $('#note-' + id).hide();
                $('#' + id).hide();
            },
            error: function (e) {
                console.log("Error deleting a note.");
                console.log(e);
            }
        });
    });

    /**
     * This is used to delete a note reply.
     */
    $(document).on('click', '.delete-reply', function () {
        var id = $(this).attr('data-id');
        $.ajax({
            url: caramel.context + "/apis/assets/".concat(id).concat("?type=note"),
            type: 'DELETE',
            contentType: "application/json",
            dataType: 'json',
            success: function (response) {
                messages.alertSuccess("Reply deleted successfully");
                $('#note-reply-icon-' + id).remove();
                $('#note-reply-' + id).remove();
            },
            error: function (e) {
                console.log("Error deleting a note.");
                console.log(e);
            }
        });
    });

    /**
     * This is used to edit a note.
     */
    $(document).on('click', '.edit-note', function () {

        var id = $(this).attr('data-id');
        var note_content = $('#' + id + '_note').html();
        $('#' + id + '-edit-thread').show();
        $('#' + id + '-edit-note-content').empty();
        $('#' + id + '-edit-note-content').append(note_content);
    });

    /**
     * This is used to edit a note.
     */
    $(document).on('click', '.update-note', function () {

        var id = $(this).attr('data-id');
        var note_content = $('#' + id + '-edit-note-content').val();
        var resourcepath = $(this).attr('data-resourcepath');
        var overview_hash = $(this).attr('data-overview_hash');
        var data = {};
        data.id = id;
        data.overview_note = note_content;
        data.overview_resourcepath = resourcepath;
        data.overview_hash = overview_hash;
        data.overview_status = "Open";

        $.ajax({
            url: caramel.context + "/apis/assets/" + id + "?type=note",
            type: 'POST',
            data: JSON.stringify(data),
            contentType: "application/json",
            dataType: 'json',
            success: function (response) {
                $('#' + id + '-edit-note-content').empty();
                $('#' + id + '-edit-thread').hide();
                $('#' + id + '_note').empty();
                $('#' + id + '_note').append(note_content);
                messages.alertSuccess("Note Updated successfully");
                // setTimeout(function(){location.reload(true);},3000);
            },
            error: function () {
                //console.log("Error adding a note.");
            }
        });

    });


    /**
     * This is used to resolve a note.
     */
    $(document).on('click', '.resolve-thread', function () {

        var id = $(this).attr('data-id');
        var resourcepath = $(this).attr('data-resourcepath');
        var overview_hash = $(this).attr('data-overview_hash');
        var data = {};
        data.id = id;
        data.overview_note = $('#' + id + '_note').text();
        data.overview_resourcepath = resourcepath;
        data.overview_hash = overview_hash;
        data.overview_status = "Resolved";

        $.ajax({
            url: caramel.context + "/apis/assets/" + id + "?type=note",
            type: 'POST',
            data: JSON.stringify(data),
            contentType: "application/json",
            dataType: 'json',
            success: function (response) {
                messages.alertSuccess("Note Resolved successfully");
                setTimeout(function () {
                    location.reload(true);
                }, 3000);
            },
            error: function () {
                //console.log("Error adding a note.");
            }
        });

    });

    /**
     * This is used to re-open.
     */
    $(document).on('click', '.reopen-thread', function () {

        var id = $(this).attr('data-id');
        var resourcepath = $(this).attr('data-resourcepath');
        var overview_hash = $(this).attr('data-overview_hash');
        var data = {};
        data.id = id;
        data.overview_note = $('#' + id + '_note').text();
        data.overview_resourcepath = resourcepath;
        data.overview_hash = overview_hash;
        data.overview_status = "Open";

        $.ajax({
            url: caramel.context + "/apis/assets/" + id + "?type=note",
            type: 'POST',
            data: JSON.stringify(data),
            contentType: "application/json",
            dataType: 'json',
            success: function (response) {
                var input = {};
                input.notes = [];
                input.notes.push(response.data);
                renderPartial('notes-note', {}, function (result) {
                    renderPartial('notes-note-container', input, function (result) {
                        $('#collapseNotes .wr-panel-notes').append(result);
                    });
                });
                $('#add-note-content').val('');
                messages.alertSuccess("Note thread Re-opened successfully");
                setTimeout(function () {
                    location.reload(true);
                }, 3000);
            },
            error: function () {
                //console.log("Error adding a note.");
            }
        });

    });


    /**
     * This is used to edit a reply.
     */
    $(document).on('click', '.edit-reply', function () {

        var id = $(this).attr('data-id');
        var reply_content = $('#' + id + '_reply').html();
        $('#' + id + '-edit-thread').show();
        $('.reply-note-text-container').hide();
        $('#' + id + '-edit-reply-content').empty();
        $('#' + id + '-edit-reply-content').append(reply_content);
    });

    /**
     * This is used to edit a note.
     */
    $(document).on('click', '.update-reply', function () {

        var id = $(this).attr('data-id');
        var reply_content = $('#' + id + '-edit-reply-content').val();
        var resourcepath = $(this).attr('data-resourcepath');
        var overview_hash = $(this).attr('data-overview_hash');
        var overview_replypath = $(this).attr('data-replypath');
        var data = {};
        data.id = id;
        data.overview_note = reply_content;
        data.overview_resourcepath = resourcepath;
        data.overview_hash = overview_hash;
        data.overview_replypath = overview_replypath;
        data.overview_status = "Open";

        $.ajax({
            url: caramel.context + "/apis/assets/" + id + "?type=note",
            type: 'POST',
            data: JSON.stringify(data),
            contentType: "application/json",
            dataType: 'json',
            success: function (response) {
                var input = {};
                input.notes = [];
                input.notes.push(response.data);
                renderPartial('notes-note', {}, function (result) {
                    renderPartial('notes-note-container', input, function (result) {
                        $('#collapseNotes .wr-panel-notes').append(result);
                    });
                });
                $('#add-note-content').val('');
                $('#' + id + '-edit-reply-content').empty();
                $('#' + id + '-edit-thread').hide();
                $('#' + id + '_reply').empty();
                $('#' + id + '_reply').append(reply_content);
                $('#note-thread-textarea-' + id).show();
                messages.alertSuccess("Reply Updated successfully");
                setTimeout(function () {
                    location.reload(true);
                }, 3000);
            },
            error: function () {
                //console.log("Error adding a note.");
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
     * This function is used to get error message container Id.
     *
     * @param id    id of the root note.
     * @returns {string}
     */
    var errorContainerId = function (hash,id) {
        return   hash + id + 'error';
    };

    /**
     * This function is used to get reply notes.
     */
    $('.reply-note').on('click', function () {
        var id = $(this).data('id');
        var data = {};
        data.overview_resourcepath = this.dataset.path;
        data.overview_note = $(replyContainerId(id)).val();
        data.overview_replypath = this.dataset.path;
        data.overview_visibility = "public";
        data.overview_status = "Open";

        if(!data.overview_note){
            var errorId = errorContainerId("#",id);
            $(errorId).show();
            return;
        }

        $.ajax({
            url: caramel.context + "/apis/assets?type=note",
            type: 'POST',
            data: JSON.stringify(data),
            contentType: "application/json",
            dataType: 'json',
            success: function (response) {
                $(replyContainerId(id)).val('');
                messages.alertSuccess("Reply added successfully");
                setTimeout(function () {
                    location.reload(true);
                }, 2000);
            },
            error: function () {
                //console.log("Error while adding reply");
            }
        });
    });

    /**
     * This function is used to get reply notes.
     */
    $('.note-thread').on('click', function () {

        var path = $(this).closest('.wr-panel-note').data('path');
        var id = $(this).closest('.wr-panel-note').attr('href');
        var errorId = errorContainerId("", id);
        var replyId = replyContainerId(id.substring(1));
        $(errorId).hide();

        $(replyId).keyup(function(){
            if($(this).val().length !=0)
                $(errorId).hide();
            else
                $(errorId).show();
        });

        $.ajax({
            url: caramel.context + '/apis/assets?type=note&q="overview_replypath":"' + path + '"',
            type: 'GET',
            success: function (response) {
                $(id + "> .wr-panel-sub-note").html("");
                $.each(response.list, function (key, value) {
                    renderPartial('notes-note', value, function (result) {
                        $(id + "> .wr-panel-sub-note").append(result);
                    });
                });
            },
            error: function () {
                //console.log("Error loading note replies.");
            }
        });
    });

    /**
     * This is used to edit a reply.
     */
    $(document).on('click', '.note-thread', function () {
        var id = $(this).attr('data-id');
        $('#note-thread-textarea-' + id).show();
    });

});

/**
 * On DOM load if note are empty, button text changes to "Add Note" else "New".
 */
$('document').ready(function () {
    if ($('#collapseNotes .panel-group .panel > div').length == 0) {
        $('#newThreadBtn .btn-text').html('Add Note');
    }
    else {
        $('#newThreadBtn .btn-text').html('New');
    }
    $('#notes-error-add').hide();
    $('#add-note-content').keyup(function(){
        if($(this).val().length !=0)
            $('#notes-error-add').hide();
        else
            $('#notes-error-add').show();
    });
    $('#newThreadBtn').show();
});

/**
 * On button "Add Note"/"New" click, show text input field
 */
$('#newThreadBtn').click(function () {
    $('#newThread').fadeIn('fast');
});
