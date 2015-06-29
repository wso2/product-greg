var GregSubscriptionAPI = {};
$(function() {
    
    var addSubscription = function(element, id, type, method, option) {
        var urlSub = caramel.context + '/apis/subscriptions/' + type + '/' + id;
        //alert('addSubscription');
        var data = {};
        data.notificationType = option;
        data.notificationMethod = method;
        $.ajax({
            url: urlSub,
            type: 'POST',
            data: JSON.stringify(data),
            contentType: 'application/json',
            success: function(data) {
                if (data.error != null) {
                    BootstrapDialog.show({
                        type: BootstrapDialog.TYPE_DANGER,
                        title: 'Error!',
                        message: '<div><i class="fa fa-warning"></i> ' + data.error + '</div>',
                        buttons: [{
                            label: 'Close',
                            action: function (dialogItself) {
                                $(element).prop("checked", false);
                                $(element).change(function() {
                                    addSubscription(element, id, type, method, option);
                                });
                                dialogItself.close();
                                location.reload(true);
                            }
                        }]
                    });
                } else {
                    var subcriptionid = data[0].id;
                    BootstrapDialog.show({
                        type: BootstrapDialog.TYPE_SUCCESS,
                        title: 'Success!',
                        message: '<div><i class="fa fa-check"></i> Subscriptions added successfully</div>',
                        buttons: [{
                            label: 'OK',
                            action: function (dialogItself) {
                                $(element).prop("checked", true);
                                $(element).change(function() {
                                    removeSubscription(element, id, subcriptionid, method, option);
                                });
                                dialogItself.close();
                                location.reload(true);
                            }
                        }]

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
        var urlSub = caramel.context + '/apis/subscriptions/' + type + '/' + id + '?subcriptionid=' + subcriptionid;
        //alert('removeSubscription');
        $.ajax({
            url: urlSub,
            type: 'DELETE',
            contentType: 'application/json',
            success: function(data) {
                if (data.error != null) {
                    BootstrapDialog.show({
                        type: BootstrapDialog.TYPE_DANGER,
                        title: 'Error!',
                        message: '<div><i class="fa fa-warning"></i> ' + data.error + '</div>',
                        buttons: [{
                            label: 'Close',
                            action: function (dialogItself) {
                                $(element).prop("checked", true);
                                $(element).change(function () {
                                    removeSubscription(element, id, type, subcriptionid, method, option);
                                });
                                dialogItself.close();
                                location.reload(true);
                            }
                        }]
                    });
                } else {
                    BootstrapDialog.show({
                        type: BootstrapDialog.TYPE_SUCCESS,
                        title: 'Success!',
                        message: '<div><i class="fa fa-check"></i> Subscriptions removed successfully</div>',
                        buttons: [{
                            label: 'OK',
                            action: function (dialogItself) {
                                $(element).prop("checked", false);
                                $(element).change(function () {
                                    addSubscription(element, id, type, method, option);
                                });
                                dialogItself.close();
                                location.reload(true);
                            }
                        }]
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
                BootstrapDialog.show({
                    type: BootstrapDialog.TYPE_SUCCESS,
                    title: 'Success!',
                    message: '<div><i class="fa fa-check"></i> ' + data + '</div>',
                    buttons: [{
                        label: 'OK',
                        action: function (dialogItself) {
                            $(element).prop("checked", false);
                            $(element).change(function () {
                                addSubscription(element, id, type, method, option);
                            });
                            dialogItself.close();
                        }
                    }]
                });
            },
            error: function() {
                BootstrapDialog.show({
                    type: BootstrapDialog.TYPE_DANGER,
                    title: 'Error!',
                    message: '<div><i class="fa fa-warning"></i> Error while loading notification</div>',
                    buttons: [{
                        label: 'Close',
                        action: function (dialogItself) {
                            dialogItself.close();
                        }
                    }]
                });
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
                    element.parent().remove();
                    $('#notificationCount').html((notiCount - 1));
                }
            },
            error: function() {
                BootstrapDialog.show({
                    type: BootstrapDialog.TYPE_DANGER,
                    title: 'Error!',
                    message: '<div><i class="fa fa-warning"></i> Error while removing notification</div>',
                    buttons: [{
                        label: 'Close',
                        action: function (dialogItself) {
                            dialogItself.close();
                        }
                    }]
                });
            }
        })
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
});