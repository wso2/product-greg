var GregSubscriptionAPI = {};
$(function() {
    var resolveSubscriptionLabel = function(element){
        return $($(element).parent().parent().siblings()[0]).html();
    };
    var addSubscription = function(element, id, type, method, option) {
        var urlSub = caramel.context + '/apis/subscription/' + type + '/' + id;
        //alert('addSubscription');
        var data = {};
        data.notificationType = option;
        data.notificationMethod = method;
        $(element).unbind("change");
        $.ajax({
            url: urlSub,
            type: 'POST',
            data: JSON.stringify(data),
            contentType: 'application/json',
            success: function(data) {
                if (data.error != null) {
                    messages.alertError(data.error);
                    $(element).prop("checked", false);
                    $(element).change(function() {
                        addSubscription(element, id, type, method, option);
                    });
                } else {
                    messages.alertSuccess('You will now receive notifications for <strong>'+resolveSubscriptionLabel(element) + '</strong> events.');
                    var subcriptionid = data[0].id;
                    $(element).prop("checked", true);
                    $(element).change(function() {
                        removeSubscription(element, id, type, subcriptionid, method, option);
                    });
                }
            },
            error: function() {
                $(element).prop("checked", false);
                $(element).change(function() {
                    addSubscription(element, id, type, method, option);
                });
            }
        })
    };
    var removeSubscription = function(element, id, type, subcriptionid, method, option) {
        var urlSub = caramel.context + '/apis/subscription/' + type + '/' + id + '?subcriptionid=' + subcriptionid;
        //alert('removeSubscription');
        $(element).unbind("change");
        $.ajax({
            url: urlSub,
            type: 'DELETE',
            contentType: 'application/json',
            success: function(data) {
                if (data.error != null) {
                    messages.alertError(data.error);
                    $(element).prop("checked", true);
                    $(element).change(function() {
                        removeSubscription(element, id, type, subcriptionid, method, option);
                    });
                } else {
                    messages.alertSuccess('You will no longer receive notifications for <strong>'+resolveSubscriptionLabel(element) + '</strong> events.');
                    $(element).prop("checked", false);
                    $(element).change(function() {
                        addSubscription(element, id, type, method, option);
                    });
                }
            },
            error: function() {
                $(element).prop("checked", true);
                $(element).change(function() {
                    removeSubscription(element, id, type, subcriptionid, method, option);
                });
            }
        })
    };
    var loadNotifications = function() {
        var urlSub = caramel.context + '/apis/notification';
        //alert( 'sdasdad' );
        $.ajax({
            url: urlSub,
            success: function(data) {
                alert(data);
            },
            error: function() {
                alert('Error while loading notification');
            }
        })
    };
    var removeNotification = function(id, element) {
        var urlSub = caramel.context + '/apis/notification/' + id;
        //$(this).remove()
        //console.log(element);
        var notiCount = $('#notificationCount').html();
        notiCount = parseInt(notiCount);
        $.ajax({
            url: urlSub,
            type: 'DELETE',
            contentType: 'application/json',
            success: function(data) {
                if (data.error == null) {
                    var updatedNotiCount = notiCount - 1;
                    element.parent().remove();
                    $('#notificationCount').html(updatedNotiCount);
                    if(updatedNotiCount == 0){
                        $("#clearalldiv").hide();
                        toggleSidePanel('notifications',$('.wr-notification-toggle-btn'));
                    }
                }
            },
            error: function() {
                alert('Error while removing notification');
            }
        })
    };

    var clearAllNotifications = function() {
        var urlSub = caramel.context + '/apis/notification/';
        BootstrapDialog.show({
            type: BootstrapDialog.TYPE_WARNING,
            title: 'Warning!',
            message: '<div class="notification-message-dialog-box"><i class="fw fw-warning notification-message">' +
            '</i> Are you sure you want to clear all notifications?</div>',
            buttons: [{
                label: 'Yes',
                action: function (dialogItself) {
                    $.ajax({
                        url: urlSub,
                        type: 'DELETE',
                        contentType: 'application/json',
                        success: function (data) {
                            if (data.error == null) {
                                dialogItself.close();
                                $('#notificationCount').html(0);
                                $("#clearalldiv").hide();
                                $(".wr-notification-desc").hide();
                                toggleSidePanel('notifications',$('.wr-notification-toggle-btn'));
                            }
                        },
                        error: function () {
                            BootstrapDialog.show({
                                type: BootstrapDialog.TYPE_DANGER,
                                title: 'Error!',
                                message: '<div><i class="fa fa-warning"></i> Error while clearing notifications</div>',
                                buttons: [{
                                    label: 'Close',
                                    action: function (dialogItself) {
                                        dialogItself.close();
                                    }
                                }]
                            });
                        }
                    });
                }
            },
                {
                    label: 'No',
                    action: function (dialogItself) {
                        dialogItself.close();
                    }
                }]
        });
    };

    var containers = ['resourceUpdated', 'lifeCycleStateChanged', 'checkListItemChecked', 'checkListItemUnchecked'];
    var methods = ['Work', 'Email'];
    var map = function(container, method) {
        return '#' + container + '' + method;
    };
    var info = function(container) {
        var opts = $(container).data();
        return opts;
    };
    var containerName;
    var methodName;
    var id;
    var options;
    for (var index = 0; index < containers.length; index++) {
        for (var index2 = 0; index2 < methods.length; index2++) {
            containerName = containers[index];
            methodName = methods[index2];
            $(map(containerName, methodName)).on('change', function() {
                options = info(this);
                if ($(this)[0].checked) {
                    //addSubscription();
                    //alert(GregPageAPI.getAssetId());
                    //alert(GregPageAPI.getAssetType());
                    addSubscription(this, GregPageAPI.getAssetId(), GregPageAPI.getAssetType(), options.type, options.method);
                    //$(this)[0].checked
                    //removeSubscription();
                } else {
                    removeSubscription(this, GregPageAPI.getAssetId(), GregPageAPI.getAssetType(), options.id, options.type, options.method);
                }
                //alert($(this)[0].checked);
                //alert('Clicked me!!!'+this.id);
            });
        }
    }
    GregSubscriptionAPI.removeNotification = removeNotification;
    GregSubscriptionAPI.clearAllNotifications = clearAllNotifications;
});